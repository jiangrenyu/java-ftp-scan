package com.bonc.ftputil.util;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.fs.FileStatus;

import com.bonc.ftputil.bean.FtpClientKey;

/**
 * 功能描述
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-15
 * @time 下午9:12:22 
 * 
 */
public  interface CommonFtp<T> {
	
	
	/**
	 * 获取FTPClient对象
	 * @param ftpHost FTP主机服务器
	 * @param ftpUserName FTP登录用户名
	 * @param ftpPassword FTP 登录密码
	 * @param ftpPort FTP端口 默认为21
	 * @return
	 */
	public abstract T getFTPClient(String ftpHost, String ftpUserName,
			String ftpPassword, int ftpPort);

	/**
	 * 
	 * 
	 * 下载ftp上指定路径的文件
	 * @param remotePath      ftp文件路径
	 * @param fileName		  ftp文件名
	 * @param localDirectoryPath  本地文件路径（不包含文件名）	
	 * @return
	 * 
	 *
	 */
	public abstract File downFile(T ftpClient, String remotePath,
			String fileName, String localDirectoryPath) throws Exception;

	/**
	 * 
	 * 
	 * 下载ftp上指定路径的文件到HDFS
	 * @param remotePath 		 ftp路径
	 * @param fileName   		  文件名
	 * @param localDirectoryPath hdfs落地路径（不包含文件名）
	 * @param hdfsUrl 			 hdfs的连接url
	 * @return
	 * 
	 *
	 */
	public abstract FileStatus downFile2HDFS(T ftpClient, String remotePath,
			String fileName, String localDirectoryPath)
			throws Exception;

	/**
	 * 关闭连接
	 */
	public abstract void closeFTPClient(T ftpClient);

	/**
	 * 
	 * 
	 * 挪文件到备份目录
	 * @param ftpClient
	 * @param ftpDirectory
	 * @param bakDirectory
	 * @param fileName
	 * @throws FTPConnectionClosedException
	 * @throws Exception
	 * 
	 *
	 */
	public abstract boolean moveFtpFile2BakDirectory(T ftpClient,
			String ftpDirectory, String bakDirectory, String fileName);
	
	
	
	/**
	 * 
	 * 
	 * 获取用户的Home目录
	 * @param ftpClient
	 * @return
	 * 
	 *
	 */
	public String getHome(T ftpClient) ;
	/**
	 * 
	 * 
	 * 获取用户的当前目录
	 * @param ftpClient
	 * @return
	 * 
	 *
	 */
	public String getPwd(T ftpClient) ;

}