package com.bonc.ftputil.bean;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.jcraft.jsch.SftpException;

public interface FTPServer {
	
	/**
	 * 使用指定信息连接FTPServer
	 * @param hostname
	 * @param port
	 * @param username
	 * @param password
	 * @param loginTimeout 
	 * @return
	 */
	public boolean connectServer(String hostname,int port,String username,String password, int loginTimeout);
	
	/**
	 * 下载远程FTP服务器上文件到本地指定目录
	 * @param remoteFileName	远程文件绝对路径
	 * @param localFileName	        本地文件绝对路径
	 * @return
	 */
	public boolean downloadFile(String remoteFileName,String localFileName);
	
	/**
	 * 列出某个路径下的所有文件,忽略文件夹，将结果以List<Map>返回，map中包含fileName,filePath,fileSize,lastModified
	 * @param dirPath	
	 * @return
	 * @throws IOException
	 * @throws SftpException 
	 */
	public List<HashMap<String,Object>>  listFile(String dirPath) throws IOException, SftpException;
	
	  
	/**
	 * 关闭连接
	 */
	public void closeFTPClient();
	
	
	/**
	 * 列出某个路径下的所有文件和一级文件夹下的文件，将结果以List<Map>返回，map中包含fileName,filePath,fileSize,lastModified,scanTime
	 * @param dirPath	
	 * @return
	 * @throws IOException 
	 * @throws SftpException 
	 */
	public List<HashMap<String, Object>> listFileAndDirectory(String remotePath) throws IOException, SftpException;
	
	
	/**
	 * 列出某个路径下的所有文件和一级文件夹下匹配正则表达式 fileRegular 的文件，将结果以List<Map>返回，map中包含fileName,filePath,fileSize,lastModified,scanTime
	 * @param remotePath
	 * @param fileRegular
	 * @return
	 * @throws SftpException 
	 * @throws IOException 
	 */
	public List<HashMap<String, Object>> listFileAndDirectory(
			String remotePath, String fileRegular) throws SftpException, IOException;
	
	
}
