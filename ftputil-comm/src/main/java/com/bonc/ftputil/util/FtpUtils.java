package com.bonc.ftputil.util;  

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftputil.bean.FTPConnection;
import com.bonc.ftputil.bean.KafKaConsumer;

/**
 * ftp功能封装
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-11-27
 * @time 下午3:54:04 
 * 
 */
public class FtpUtils implements CommonFtp<FTPConnection> {
	
	
	private FileSystem fs = null;
	
	private Configuration configuration ; 
	
	public FtpUtils() {
		
		configuration = new Configuration();
		
		try {
			
			configuration.addResource(new FileInputStream(KafKaConsumer.hdfsConfFile));
			
			fs = FileSystem.get(configuration);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
	}
	

	private static final Logger logger = LoggerFactory.getLogger(FtpUtils.class);
	
	@Override
	public  FTPConnection getFTPClient(String ftpHost, String ftpUserName,
			String ftpPassword, int ftpPort) {
			FTPClient ftpClient = new FTPClient();
			try {
				ftpClient.connect(ftpHost, ftpPort);// 连接FTP服务器
				ftpClient.login(ftpUserName, ftpPassword);// 登陆FTP服务器
				ftpClient.setDataTimeout(60000);       	  //设置传输超时时间为60秒 
				ftpClient.setConnectTimeout(60000);       //连接超时为60秒
				
				ftpClient.setBufferSize(KafKaConsumer.ftpBufferSize);//缓冲字节大小
				
				ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
				if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
					logger.error("未连接到FTP，用户名或密码错误。");
					ftpClient.disconnect();
					return null ;
				} else {
					logger.info("FTP["+ftpHost+":"+ftpUserName+"]连接成功。");
				}
			} catch (SocketException e) {
				e.printStackTrace();
				logger.error("FTP的IP地址可能错误，请正确配置。",e.getCause());
				return null ;
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("FTP的端口错误,请正确配置。",e.getCause());
				return null ;
			}
		
		
		return new FTPConnection(ftpHost, ftpUserName, ftpPassword, ftpPort, ftpClient);
	}
	
	@Override
	public  File downFile(FTPConnection ftpConnection,String remotePath, String fileName,String localDirectoryPath) throws IOException{
		
		FTPClient ftpClient = ftpConnection.getFtpClient();
		
		File localFile =new File(localDirectoryPath,fileName);

		try {
			
			ftpClient.enterLocalPassiveMode();//被动模式
			
			if(!localFile.getParentFile().exists()){
				
				localFile.getParentFile().mkdirs();
			}
			
			OutputStream os = new FileOutputStream(localFile);
			
			// 将文件保存到输出流outputStream中
			
			String remoteFilePath = remotePath+fileName;
			
			boolean flag  ;
			
			if(KafKaConsumer.isOIDD){
				
				flag = ftpClient.retrieveFile(fileName, os);
				
			}else{
				
				flag = ftpClient.retrieveFile(remoteFilePath, os);
				
			}

			
			logger.info("下载文件["+remoteFilePath+"]:"+(flag?"success":"fail"));

			os.flush();

			os.close();
			
		} catch (FTPConnectionClosedException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e.getCause());
			
			throw e ;
		}catch (CopyStreamException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e.getCause());
			throw e ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e.getCause());
			throw e ;
		}
		
		return localFile ;
		

	}
	
	
	@Override
	public  FileStatus downFile2HDFS(FTPConnection ftpConnection,String remotePath, String fileName,String localDirectoryPath) throws IOException{
		FTPClient ftpClient = ftpConnection.getFtpClient();
		
		logger.info("下载FTP文件到HDFS开始");
		
		FileStatus fileStatus = null;
		
		OutputStream os =null;
		
		String ftpFilePath = remotePath + fileName ;
		
		String hdfsFilePath = localDirectoryPath+fileName;
		
		Path path = new Path(hdfsFilePath);
		
		boolean flag  = false;
		
		try {
		
			os=fs.create(path);
			
			ftpClient.enterLocalPassiveMode();//被动模式
			
			if(KafKaConsumer.isOIDD){
				
				flag = ftpClient.retrieveFile(fileName, os);
				
			}else{
				
				flag = ftpClient.retrieveFile(ftpFilePath, os);
				
			}
			
			logger.info("下载文件"+ftpClient.getRemoteAddress().getHostAddress()+":"+ftpClient.getRemotePort()+"["+ftpFilePath+"]到HDFS["+hdfsFilePath+"]:"+(flag?"success":"fail["+ftpClient.getReplyString()+"]"));
			
			if(!flag){
				
				throw new IOException("download ftp file error,detail:"+ftpClient.getReplyString());
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e);
			
			if(e instanceof IOException){
				
				fs = FileSystem.get(configuration);
				if(fs == null) {
					throw e;
				}
				
				closeFTPClient(ftpConnection);
				
				ftpConnection = getFTPClient(ftpConnection.getFtpHost(), ftpConnection.getFtpUserName(), ftpConnection.getFtpPassword(), ftpConnection.getFtpPort());
				
				if(KafKaConsumer.isOIDD){
					
					ftpConnection.getFtpClient().changeWorkingDirectory(remotePath);
					
				}
				
				
				
				for (int i = 0; i < 3; i++) {
					
					
					fileStatus = tribleDownload(ftpConnection, remotePath, fileName, localDirectoryPath);
					
					if(fileStatus!=null){
						
						break;
					}
				}
				
			}
			
			throw e ;
		} finally{
			
				
			if(os!=null){
				os.close();
			}
			
			logger.info("query hdfs file status begin!");
			
			if(fileStatus==null){
				
				fileStatus  = fs.getFileStatus(path);
				
			}
			
			logger.info("query hdfs file status end!");
				
			
		}
		 
		return fileStatus ;
		
		
	}
	
	
	private FileStatus tribleDownload(FTPConnection ftpConnection,
			String remotePath, String fileName, String localDirectoryPath) {
		
		logger.info("tribleDownload start");
		
		FTPClient ftpClient = ftpConnection.getFtpClient();
		
		FileStatus fileStatus = null;
		
		OutputStream os =null;
		
		String ftpFilePath = remotePath + fileName ;
		
		String hdfsFilePath = localDirectoryPath+fileName;
		
		Path path = new Path(hdfsFilePath);
		
		boolean flag  = false;
		
		try {
		
			os=fs.create(path);
			
			ftpClient.enterLocalPassiveMode();//被动模式
			
			if(KafKaConsumer.isOIDD){
				
				flag = ftpClient.retrieveFile(fileName, os);
				
			}else{
				
				flag = ftpClient.retrieveFile(ftpFilePath, os);
				
			}
			
			logger.info("下载文件"+ftpConnection.getFtpHost()+":"+ftpConnection.getFtpPort()+"["+ftpFilePath+"]到HDFS["+hdfsFilePath+"]:"+(flag?"success":"false["+ftpClient.getReplyString()+"]"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e);
			
		} finally{
				
			if(os!=null){
				
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage(),e);
				}
			}
			
			try {
				
				fileStatus  = fs.getFileStatus(path);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage(),e);
			}
			
			logger.info("tribleDownload end");
				
		}
		
		logger.info("tribleDownload end");
		 
		return fileStatus ;
		
	}

	@Override
	public  void closeFTPClient(FTPConnection ftpConnection){
		
		FTPClient ftpClient = ftpConnection.getFtpClient();
		
		if(ftpClient != null){
			
			try {
				
				ftpClient.logout();
				
			} catch (IOException e) {
				
				logger.error(e.getMessage(),e.getCause());
				
			}finally{
				
				try {
					if(ftpClient.isConnected()){
						
						ftpClient.disconnect();
					}
					
				} catch (IOException e) {
					
					logger.error(e.getMessage(),e.getCause());
					
					
				}
				
			}
			
		}
		
	}
	
	
	@Override
	public  boolean moveFtpFile2BakDirectory(FTPConnection ftpConnection,String ftpDirectory,String bakDirectory,String fileName) {
		
		try {
			
			FTPClient ftpClient = ftpConnection.getFtpClient();
			
			boolean flag ;
			
			if(KafKaConsumer.isOIDD){
				
				if(KafKaConsumer.rm_flag){
					
					flag = ftpClient.deleteFile(fileName);
					
				}else{
					
					flag = ftpClient.rename(fileName, bakDirectory+fileName);
					
				}
				
			}else{
				
				flag = ftpClient.rename(ftpDirectory+fileName, bakDirectory+fileName);
				
			}
			
			logger.info("ftp rename src:"+ftpDirectory+fileName+" dest:"+bakDirectory+fileName +" result:"+flag);
			
			return flag ;
			
		} catch (FTPConnectionClosedException e) {
			// TODO Auto-generated catch block
			
			logger.error(e.getMessage(),e.getCause());
			
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e.getCause());
			return false;
		}
		
		
	}

	@Override
	public String getHome(FTPConnection ftpConnection) {
		
		try {
			
			FTPClient ftpClient = ftpConnection.getFtpClient();
			
			if(FTPReply.isPositiveCompletion(ftpClient.sendCommand("pwd"))){
				
				String responseString  = ftpClient.getReplyString();
				
				logger.info("pwd response:"+responseString);
				
				String result  = responseString.split("\"")[1] ;
				
				logger.info("Home path:"+result);
				
				
				return result ;
				
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public String getPwd(FTPConnection ftpConnection) {
		String pwd = "";
		
		try {
			
			FTPClient ftpClient = ftpConnection.getFtpClient();
			
			int reply = ftpClient.pwd();
			
			if(FTPReply.isPositiveCompletion(reply)){
				
				pwd = ftpClient.getReplyString().split("\"")[1] ;
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return pwd;
	}
	
}
