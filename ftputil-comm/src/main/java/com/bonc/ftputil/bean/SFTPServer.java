package com.bonc.ftputil.bean;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftputil.util.RegUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelSftp.LsEntrySelector;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * 该实现为sftp的相关操作
 * @author yinglin
 *
 */
public class SFTPServer implements FTPServer{
	
	private static Logger LOG = LoggerFactory.getLogger(SFTPServer.class);
	
	private Session session = null;
	private Channel channel = null;
	private ChannelSftp sftpChannel = null;
	
	
	public boolean connectServer(String ip,int port,String username,String password, int loginTimeout){
		
		try {
			
			JSch jsch = new JSch();
			
			if(port <=0){
			    //连接服务器，采用默认端口
			    session = jsch.getSession(username, ip);
			}else{
			    //采用指定的端口连接服务器
			    session = jsch.getSession(username, ip ,port);
			}
 
			//如果服务器连接不上，则抛出异常
			if (session == null) {
				return false;
			}
			 
			//设置登陆主机的密码
			session.setPassword(password);  
			//设置第一次登陆的时候提示，可选值：(ask | yes | no)
			session.setConfig("StrictHostKeyChecking", "no");
			//设置登陆超时时间   
			session.connect(loginTimeout);
			     
			//创建sftp通信通道
			channel = (Channel) session.openChannel("sftp");
			channel.connect(loginTimeout);
			sftpChannel = (ChannelSftp) channel;
			
			return true;
			
		} catch (JSchException e) {
			LOG.info("连接ftp 服务器 "+ip+" 失败",e);
		}
		
		return false;
	}
	
	/**
	 * 列出某个路径下的所有文件,忽略文件夹，将结果以List<Map>返回，map中包含fileName,filePath,fileSize,lastModified
	 * @param dirPath	
	 * @return
	 * @throws IOException
	 * @throws SftpException 
	 */
	public List<HashMap<String,Object>>  listFile(String dirPath) throws SftpException{
		if(!dirPath.endsWith("/")){
			dirPath = dirPath + "/";
		}
		
		final String path = dirPath;
		
		final List<HashMap<String,Object>> resultList = new ArrayList<HashMap<String,Object>>();
		
		LsEntrySelector selector = new LsEntrySelector(){
		       public int select(LsEntry entry){
		    	   
		    	   if(!entry.getAttrs().isDir()  && entry.getAttrs().getSize() > 0){
		    		   	HashMap<String,Object> fileInfo = new HashMap<String,Object>();
		   			
			   			fileInfo.put("fileName", entry.getFilename());
			   			fileInfo.put("filePath", path);
			   			fileInfo.put("fileSize", entry.getAttrs().getSize());
    		   			fileInfo.put("lastModified", new Timestamp(entry.getAttrs().getMTime()*1000L));
			   			fileInfo.put("scanTime", new Timestamp(new Date().getTime()));
	
			   			resultList.add(fileInfo);
		    	   }
		    	   
		         return CONTINUE;
		       }
		     };
		
		sftpChannel.ls(dirPath, selector);
		
		return resultList;
		
	}
	
	/**
	 * 列出某个路径下的所有文件和一级文件夹下的文件，将结果以List<Map>返回，map中包含fileName,filePath,fileSize,lastModified,scanTime
	 * @param dirPath	
	 * @return
	 * @throws IOException
	 * @throws SftpException 
	 */
	public List<HashMap<String,Object>>  listFileAndDirectory(String dirPath) throws SftpException{
		
		if(!dirPath.endsWith("/")){
			dirPath = dirPath + "/";
		}
		
		List<HashMap<String,Object>> resultList = new ArrayList<HashMap<String,Object>>();
		
		Vector<LsEntry> vector = sftpChannel.ls(dirPath);
		
		for(LsEntry entry : vector){
			
			 if(!entry.getAttrs().isDir() && entry.getAttrs().getSize() > 0){
	    		   //是文件且大小大于0，未上传完的文件大小为0，暂时不入库
	    		   	HashMap<String,Object> fileInfo = new HashMap<String,Object>();
	   			
		   			fileInfo.put("fileName", entry.getFilename());
		   			fileInfo.put("filePath", dirPath);
		   			fileInfo.put("fileSize", entry.getAttrs().getSize());
		   			fileInfo.put("lastModified", new Timestamp(entry.getAttrs().getMTime()*1000L));
		   			fileInfo.put("scanTime", new Timestamp(new Date().getTime()));

		   			resultList.add(fileInfo);
	    	   }else if(!"..".equals(entry.getFilename()) && !".".equals(entry.getFilename()) && entry.getAttrs().isDir()){
	    		   //是文件夹
	    		   String subDirPath = dirPath + entry.getFilename() + "/";
	    		
	    		   Vector<LsEntry> subVector = sftpChannel.ls(subDirPath);
	    		   
	    		   for(LsEntry subEntry : subVector){
	    			   
	    			   if(!subEntry.getAttrs().isDir() && subEntry.getAttrs().getSize() > 0){
	    				   
	    					HashMap<String,Object> fileInfo = new HashMap<String,Object>();
	    		   			
	    		   			fileInfo.put("fileName", subEntry.getFilename());
	    		   			fileInfo.put("filePath", subDirPath);
	    		   			fileInfo.put("fileSize", subEntry.getAttrs().getSize());
	    		   			fileInfo.put("lastModified", new Timestamp(subEntry.getAttrs().getMTime()*1000L));
	    		   			fileInfo.put("scanTime", new Timestamp(new Date().getTime()));

	    		   			resultList.add(fileInfo);
	    			   }
	    			   
	    		   }
	    	   }
		}
		
		return resultList;
		
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
			
			int dirIndex = remoteFileName.lastIndexOf("/");
			
			if(dirIndex != -1){
				
				String dirPath = remoteFileName.substring(0, dirIndex+1);
					
				String fileName = remoteFileName.substring(dirIndex+1);
				
				if(localFileName != null && localFileName.length() > 0){
					
					sftpChannel.cd(dirPath);

					sftpChannel.get(fileName, localFileName);
					
					return true;
				}
			}
			
		} catch (SftpException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	/**
	 * 关闭通道
	 * @param channel
	 */
	private static void closeChannel(Channel channel) {  
        if (channel != null) {  
            if (channel.isConnected()) {  
                channel.disconnect();  
            }  
        }  
    }  
	
	/**
	 * 关闭会话
	 * @param session
	 */
    private static void closeSession(Session session) {  
        if (session != null) {  
            if (session.isConnected()) {  
                session.disconnect();  
            }  
        }  
    }
    
    
	/**
	 * 关闭连接
	 */
	public void closeFTPClient(){
		
		closeChannel(sftpChannel);
		closeChannel(channel);
		closeSession(session);
		
	}

	@Override
	public List<HashMap<String, Object>> listFileAndDirectory(
			String remotePath, String fileRegular) throws SftpException {
		if(!remotePath.endsWith("/")){
			remotePath = remotePath + "/";
		}
		
		List<HashMap<String,Object>> resultList = new ArrayList<HashMap<String,Object>>();
		
//		sftpChannel.cd(remotePath);
		Vector<LsEntry> vector = sftpChannel.ls(remotePath);
		
		for(LsEntry entry : vector){
			//entry.getAttrs().getSize()  测试发现不管是ftp还是sftp，文件上传过程中大小一直变化
			 if(!entry.getAttrs().isDir() && RegUtil.isMatch(entry.getFilename(), fileRegular)){
	    		   //是文件
	    		   	HashMap<String,Object> fileInfo = new HashMap<String,Object>();
	   			
		   			fileInfo.put("fileName", entry.getFilename());
		   			fileInfo.put("filePath", remotePath);
		   			fileInfo.put("fileSize", entry.getAttrs().getSize());
		   			fileInfo.put("lastModified", new Timestamp(entry.getAttrs().getMTime()*1000L));
		   			fileInfo.put("scanTime", new Timestamp(new Date().getTime()));

		   			resultList.add(fileInfo);
	    	   }else if(!"..".equals(entry.getFilename()) && !".".equals(entry.getFilename()) && entry.getAttrs().isDir()){
	    		   //是文件夹
	    		   String subDirPath = remotePath + entry.getFilename() + "/";
	    		
	    		   Vector<LsEntry> subVector = sftpChannel.ls(subDirPath);
	    		   
	    		   for(LsEntry subEntry : subVector){
	    			   
	    			   if(!subEntry.getAttrs().isDir() && RegUtil.isMatch(subEntry.getFilename(), fileRegular)){
	    				   
	    					HashMap<String,Object> fileInfo = new HashMap<String,Object>();
	    		   			
	    		   			fileInfo.put("fileName", subEntry.getFilename());
	    		   			fileInfo.put("filePath", subDirPath);
	    		   			fileInfo.put("fileSize", subEntry.getAttrs().getSize());
	    		   			fileInfo.put("lastModified", new Timestamp(subEntry.getAttrs().getMTime()*1000L));
	    		   			fileInfo.put("scanTime", new Timestamp(new Date().getTime()));

	    		   			resultList.add(fileInfo);
	    			   }
	    			   
	    		   }
	    	   }
		}
		
		return resultList;
	}
	
}
