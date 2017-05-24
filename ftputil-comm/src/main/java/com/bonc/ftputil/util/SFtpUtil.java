package com.bonc.ftputil.util;  

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftputil.bean.KafKaConsumer;
import com.bonc.ftputil.bean.SFTPConnection;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * 功能描述
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-15
 * @time 下午9:26:24 
 * 
 */
public class SFtpUtil implements CommonFtp<SFTPConnection> {
	
	private FileSystem fs = null;
	
	private Configuration configuration ;
	
	
	
	public SFtpUtil() {
		
		configuration = new Configuration();
		
		try {
			
			configuration.addResource(new FileInputStream(KafKaConsumer.hdfsConfFile));
			
			fs = FileSystem.get(configuration);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(SFtpUtil.class);
	
	@Override
	public SFTPConnection getFTPClient(String ftpHost, String ftpUserName,
			String ftpPassword, int ftpPort) {
		
		Session session = null;
		
		try {
			
			JSch jsch = new JSch();
			
			if(ftpPort <=0){
			    //连接服务器，采用默认端口
			    session = jsch.getSession(ftpUserName, ftpHost);
			}else{
			    //采用指定的端口连接服务器
			    session = jsch.getSession(ftpUserName, ftpHost ,ftpPort);
			}
 
			//如果服务器连接不上，则抛出异常
			if (session == null) {
				return null;
			}
			 
			//设置登陆主机的密码
			session.setPassword(ftpPassword);  
			//设置第一次登陆的时候提示，可选值：(ask | yes | no)
			session.setConfig("StrictHostKeyChecking", "no");
			//登陆
			session.connect();
			     
			//创建sftp通信通道
			ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
			
			sftpChannel.connect();
			
			return new SFTPConnection(ftpHost, ftpUserName, ftpPassword, ftpPort, sftpChannel) ;
			
			
		} catch (JSchException e) {
			logger.error("连接ftp 服务器 "+ftpHost+" 失败",e.getCause());
		}
		
		return null;
	}

	@Override
	public File downFile(SFTPConnection sftpConnection, String remotePath,String fileName, String localDirectoryPath) throws IOException {
		
		ChannelSftp ftpClient = sftpConnection.getChannelSftp();
		
		File localFile =new File(localDirectoryPath,fileName);
		
		String remoteFilePath = remotePath+fileName;
		
		try {
			
			if(!localFile.getParentFile().exists()){
				
				localFile.getParentFile().mkdirs();
			}
			
			OutputStream os = new FileOutputStream(localFile);

			// 将文件保存到输出流outputStream中
			
			if(KafKaConsumer.isOIDD){
				
				ftpClient.get(fileName, os);
				
			}else{
				
				ftpClient.get(remoteFilePath, os);
				
			}
			
			
			logger.info("下载文件["+remoteFilePath+"]:success");

			os.flush();

			os.close();
			
		} catch (SftpException e) {
			e.printStackTrace();
			
			logger.error("下载文件["+remoteFilePath+"]:fail",e.getCause());
		}
		
		
		return localFile;
	}

	@Override
	public FileStatus downFile2HDFS(SFTPConnection sftpConnection, String remotePath,
			String fileName, String localDirectoryPath) throws Exception
			 {
		
		logger.info("下载SFTP文件到HDFS开始");
		
		ChannelSftp ftpClient = sftpConnection.getChannelSftp() ;
		
		FileStatus fileStatus = null;
		
		OutputStream os = null;
		
		String ftpFilePath = remotePath + fileName ;
		
		String hdfsFilePath = localDirectoryPath+fileName;
		
		Path path = new Path(hdfsFilePath);
		
		try {
			logger.info("get hdfs file outputstream start!");
			os=fs.create(path);
			logger.info("get hdfs file outputstream end!");
			
			if(KafKaConsumer.isOIDD){
				logger.info("KafKaConsumer.isOIDD......");
				ftpClient.get(fileName, os);
			}else{
				logger.info("KafKaConsumer not OIDD......");
				ftpClient.get(ftpFilePath, os);
			}
			
			logger.info("下载文件"+sftpConnection.getFtpHost()+":"+sftpConnection.getFtpPort()+"["+ftpFilePath+"]到HDFS["+hdfsFilePath+"]:success");
			
		} catch (Exception e) {
			
			if(e.getMessage().equals("No such file")){
				throw e ;
			}
			
			logger.error(e.getClass().toString(), e);
			
			if(e instanceof IOException){
				
				fs = FileSystem.get(configuration);
				if(fs == null) {
					throw e;
				}
			}
			
			closeFTPClient(sftpConnection);
			
			sftpConnection = getFTPClient(sftpConnection.getFtpHost(), sftpConnection.getFtpUserName(), sftpConnection.getFtpPassword(), sftpConnection.getFtpPort());
			
			if(KafKaConsumer.isOIDD){
				
				sftpConnection.getChannelSftp().cd(remotePath);
				
			}
			
			
			for (int i = 0; i < 3; i++) {
				
				
				fileStatus = tribleDownload(sftpConnection, remotePath, fileName, localDirectoryPath);
				
				if(fileStatus!=null){
					
					break;
				}
			}
			
			
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
		return fileStatus;
	}

	@Override
	public void closeFTPClient(SFTPConnection sftpConnection) {
		
		ChannelSftp ftpClient = sftpConnection.getChannelSftp();
		
		
		 if (ftpClient != null) {
			 
            if (ftpClient.isConnected()) {  
            	ftpClient.disconnect();  
            }
            
            try {
            	
				if(ftpClient.getSession().isConnected()){
					
					ftpClient.getSession().disconnect();
				}
				
			} catch (JSchException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage(),e.getCause());
			}
            
         }  
		
	}

	@Override
	public boolean moveFtpFile2BakDirectory(SFTPConnection sftpConnection,
			String ftpDirectory, String bakDirectory, String fileName){
		
		try {
			
			ChannelSftp ftpClient = sftpConnection.getChannelSftp();
			
			if(KafKaConsumer.isOIDD){
				
				if(KafKaConsumer.rm_flag){
					
					ftpClient.rm(fileName);
					
				}else{
					
					ftpClient.rename(fileName, bakDirectory+fileName);
					
				}	
				
				
			}else{
				
				ftpClient.rename(ftpDirectory+fileName, bakDirectory+fileName);
				
			}
			
			logger.info("sftp rename src:"+ftpDirectory+fileName+" dest:"+bakDirectory+fileName +" result:true");
			
			return true ;
			
		}  catch (SftpException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e.getCause());
			
			return false;

		}
	}

	@Override
	public String getHome(SFTPConnection sftpConnection) {
		try {
			
			ChannelSftp ftpClient = sftpConnection.getChannelSftp();
			
			return ftpClient.getHome();
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null ;
	}

	@Override
	public String getPwd(SFTPConnection sftpConnection) {
		try {
			
			ChannelSftp ftpClient = sftpConnection.getChannelSftp();
			
			return ftpClient.pwd();
			
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	
	private FileStatus tribleDownload(SFTPConnection sftpConnection, String remotePath,
			String fileName, String localDirectoryPath) {
		
		logger.info("tribleDownload start");
		
		ChannelSftp ftpClient = sftpConnection.getChannelSftp() ;
		
		FileStatus fileStatus = null;
		
		OutputStream os = null;
		
		String ftpFilePath = remotePath + fileName ;
		
		String hdfsFilePath = localDirectoryPath+fileName;
		
		Path path = new Path(hdfsFilePath);
		
		try {
			
			os=fs.create(path);
			
			if(KafKaConsumer.isOIDD){
				ftpClient.get(fileName, os);
			}else{
				ftpClient.get(ftpFilePath, os);
			}
			
			logger.info("下载文件"+sftpConnection.getFtpHost()+":"+sftpConnection.getFtpPort()+"["+ftpFilePath+"]到HDFS["+hdfsFilePath+"]:success");
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			
			if(e.getMessage().equals("No such file")){
				
				return null;
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e);
			try {
				fs = FileSystem.get(configuration);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				logger.error(e1.getMessage(),e1);
			}
			
		}finally{
			
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
			
		return fileStatus;
	}
	
	
	
	

}
