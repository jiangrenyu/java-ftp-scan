package com.bonc.ftputil.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftputil.util.RegUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SimpleFTPServer implements FTPServer{
	
	private static Logger LOG = LoggerFactory.getLogger(SimpleFTPServer.class);
	
	private FTPClient ftpClient;
	
	
	/**
	 * 使用指定信息连接FTPServer
	 * @param hostname
	 * @param port
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean connectServer(String hostname,int port,String username,String password, int loginTimeout){
		
		ftpClient = new FTPClient();
//		FTPClientConfig conf = new FTPClientConfig(); 
//		conf.setServerTimeZoneId("UTC");
//		
//		ftpClient.configure(conf);
		try {
			
			ftpClient.setConnectTimeout(loginTimeout);

			ftpClient.connect(hostname, port);
			
			
			LOG.info(ftpClient.getReplyString());

			return ftpClient.login(username, password);
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 下载远程FTP服务器上文件到本地指定目录
	 * @param remoteFileName	远程文件绝对路径
	 * @param localFileName	        本地文件绝对路径
	 * @return
	 */
	public boolean downloadFile(String remoteFileName,String localFileName){
		
		OutputStream outputStream = null;
		try {
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

			int dirIndex = remoteFileName.lastIndexOf("/");
			
			if(dirIndex != -1){
				
				String dirPath = remoteFileName.substring(0, dirIndex+1);
					
				String fileName = remoteFileName.substring(dirIndex+1);
				
				if(localFileName != null && localFileName.length() > 0 && ftpClient.changeWorkingDirectory(dirPath)){
					
					ftpClient.enterLocalPassiveMode();//被动模式
					
					FTPFile[] fs = ftpClient.listFiles(); 
			        for(FTPFile ff:fs){

			            if(ff.getName().equals(fileName)){
			            	
							outputStream = new FileOutputStream(new File(localFileName));
							
							boolean result = ftpClient.retrieveFile(fileName, outputStream);
							
							outputStream.flush();
							outputStream.close();
							return result;
			            }
			        }
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 列出某个路径下的所有文件,忽略文件夹，将结果以List<Map>返回，map中包含fileName,filePath,fileSize,lastModified
	 * @param dirPath	
	 * @return
	 * @throws IOException
	 */
	public List<HashMap<String,Object>>  listFile(String dirPath) throws IOException{
		if(!dirPath.endsWith("/")){
			dirPath = dirPath + "/";
		}
		ftpClient.enterLocalPassiveMode();//被动模式
		
		FTPFile[] files = ftpClient.listFiles(dirPath);
		
		List<HashMap<String,Object>> resultList = new ArrayList<HashMap<String,Object>>();
		for(FTPFile file : files){
			
			if(file.isFile()){
				HashMap<String,Object> fileInfo = new HashMap<String,Object>();
				
				fileInfo.put("fileName", file.getName());
				fileInfo.put("filePath", dirPath);
				fileInfo.put("fileSize", file.getSize());
				fileInfo.put("lastModified", file.getTimestamp());
				fileInfo.put("scanTime", new Timestamp(new Date().getTime()));

				resultList.add(fileInfo);
			}
			
		}
		
		return resultList;
		
	}
	
	
	/**
	 * 列出某个路径下的所有文件和一级文件夹下的文件，将结果以List<Map>返回，map中包含fileName,filePath,fileSize,lastModified,scanTime
	 * @param dirPath	
	 * @return
	 * @throws IOException 
	 */
	public List<HashMap<String,Object>>  listFileAndDirectory(String dirPath) throws IOException{
		
		if(!dirPath.endsWith("/")){
			dirPath = dirPath + "/";
		}
		
		ftpClient.enterLocalPassiveMode();//被动模式
		
		FTPFile[] files = ftpClient.listFiles(dirPath);
		
		List<HashMap<String,Object>> resultList = new ArrayList<HashMap<String,Object>>();
		for(FTPFile file : files){
			
			if(file.isFile() && file.getSize() > 0){
				HashMap<String,Object> fileInfo = new HashMap<String,Object>();
				
				fileInfo.put("fileName", file.getName());
				fileInfo.put("filePath", dirPath);
				fileInfo.put("fileSize", file.getSize());
				fileInfo.put("lastModified", new Timestamp(file.getTimestamp().getTimeInMillis()+8*3600*1000));
				fileInfo.put("scanTime", new Timestamp(new Date().getTime()));
				resultList.add(fileInfo);
				
			}else if(file.isDirectory()){
				
				String path = dirPath+file.getName()+"/";
				
				FTPFile[] dirFiles  = ftpClient.listFiles(path);
				
				for(FTPFile dirFile : dirFiles){
					
					if(dirFile.isFile()  && dirFile.getSize() > 0){
						HashMap<String,Object> fileInfo = new HashMap<String,Object>();
						
						fileInfo.put("fileName", dirFile.getName());
						fileInfo.put("filePath", path);
						fileInfo.put("fileSize", dirFile.getSize());
						fileInfo.put("lastModified",new Timestamp(dirFile.getTimestamp().getTimeInMillis()+8*3600*1000));
						fileInfo.put("scanTime", new Timestamp(new Date().getTime()));

						resultList.add(fileInfo);
					}
					
				}
				
			}
			
		}
		
		return resultList;
		
	}
	
	
	
	/**
	 * 递归列出某个路径下的所有文件，将结果以List<Map>返回，map中包含fileName,filePath,fileSize,lastModified
	 * @param dirPath	
	 * @return
	 * @throws IOException
	 */
	public List<HashMap<String,Object>>  listFileRecursive(String dirPath) throws IOException{
		if(!dirPath.endsWith("/")){
			dirPath = dirPath + "/";
		}
		
		ftpClient.enterLocalPassiveMode();//被动模式
		
		FTPFile[] files = ftpClient.listFiles(dirPath);
		
		List<HashMap<String,Object>> resultList = new ArrayList<HashMap<String,Object>>();
		for(FTPFile file : files){
			
			if(file.isFile()){
				HashMap<String,Object> fileInfo = new HashMap<String,Object>();
				
				fileInfo.put("fileName", file.getName());
				fileInfo.put("filePath", dirPath);
				fileInfo.put("fileSize", file.getSize());
				fileInfo.put("lastModified", file.getTimestamp());
				fileInfo.put("scanTime", new Timestamp(new Date().getTime()));

				resultList.add(fileInfo);
				
			}else if(file.isDirectory()){
				
				resultList.addAll(listFileRecursive(dirPath+file.getName()+"/"));
			}
			
		}
		
		return resultList;
		
	}
	
	  
	/**
	 * 关闭连接
	 */
	public void closeFTPClient(){
		
		if(ftpClient != null){
			
			try {
				
				ftpClient.logout();
				
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				
				try {
					if(ftpClient.isConnected()){
						
						ftpClient.disconnect();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
		}
		
	}

	@Override
	public List<HashMap<String, Object>> listFileAndDirectory(
			String remotePath, String fileRegular) throws SftpException, IOException {
		if(!remotePath.endsWith("/")){
			remotePath = remotePath + "/";
		}
		
//		LOG.info("pwdReuslt:",ftpClient.sendCommand("pwd"));
//		LOG.info("pwd:{}",ftpClient.getReplyString());
		
		ftpClient.enterLocalPassiveMode();//被动模式
		
		FTPFile[] files = ftpClient.listFiles(remotePath);
		
		List<HashMap<String,Object>> resultList = new ArrayList<HashMap<String,Object>>();
		for(FTPFile file : files){
			
			if(file.isFile() && RegUtil.isMatch(file.getName(), fileRegular)){
				HashMap<String,Object> fileInfo = new HashMap<String,Object>();
				
				fileInfo.put("fileName", file.getName());
				fileInfo.put("filePath", remotePath);
				fileInfo.put("fileSize", file.getSize());
//				System.out.println(file.getName()+ "" +file.getTimestamp().getTimeZone());
				fileInfo.put("lastModified", new Timestamp(file.getTimestamp().getTimeInMillis()+8*3600*1000));
//				fileInfo.put("lastModified", new Timestamp(file.getTimestamp().getTimeInMillis()));
				fileInfo.put("scanTime", new Timestamp(new Date().getTime()));
				resultList.add(fileInfo);
				
			}else if(file.isDirectory()){
				
				String path = remotePath+file.getName()+"/";
				
				FTPFile[] dirFiles  = ftpClient.listFiles(path);
				
				for(FTPFile dirFile : dirFiles){
					
					if(dirFile.isFile()  && RegUtil.isMatch(dirFile.getName(), fileRegular)){
						HashMap<String,Object> fileInfo = new HashMap<String,Object>();
						
						fileInfo.put("fileName", dirFile.getName());
						fileInfo.put("filePath", path);
						fileInfo.put("fileSize", dirFile.getSize());
						fileInfo.put("lastModified",new Timestamp(dirFile.getTimestamp().getTimeInMillis()+8*3600*1000));
						fileInfo.put("scanTime", new Timestamp(new Date().getTime()));

						resultList.add(fileInfo);
					}
					
				}
				
			}
			
		}
		
		return resultList;
	}
	
	
	public static void main(String[] args) throws IOException, SftpException {
		testListDirectory();
	}
	public static void testListDirectory() throws IOException, SftpException{
		String hostname = "192.168.220.10";
		
		int port = 21;
		
		String username = "ftp";
		
		String password = "ftp";
		
		String remoteFileName = "/upload";
		
		int timeout = 30000;
		
//		String localFileName = "E:/magic.jpg";
		
		SimpleFTPServer ftpServer = new SimpleFTPServer();
		
		if(ftpServer.connectServer(hostname, port, username, password,timeout)){
			
			System.out.println("连接成功");
			
			List<HashMap<String,Object>> fileList = ftpServer.listFileAndDirectory(remoteFileName, ".*\\.txt");
			
			for(HashMap<String,Object> obj : fileList){
				System.out.println(obj);
			}
			
			
		}else{
			System.out.println("连接失败");
		}
		
	}
	
}
