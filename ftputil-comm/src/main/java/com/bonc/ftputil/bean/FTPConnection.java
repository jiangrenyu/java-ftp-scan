package com.bonc.ftputil.bean;  

import org.apache.commons.net.ftp.FTPClient;

/**
 * 功能描述
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2016-1-18
 * @time 下午3:45:58 
 * 
 */
public class FTPConnection {

	/**
	 * ftp主机IP
	 */
	private String ftpHost;
	
	/**
	 * ftp用户名
	 */
	private String ftpUserName ;
	
	
	/**
	 * ftp密码
	 */
	private String ftpPassword;
	
	
	/**
	 * ftp端口
	 */
	private int ftpPort ;
	
	
	/**
	 * ftp client 
	 */
	private FTPClient ftpClient ;


	public String getFtpHost() {
		return ftpHost;
	}


	public void setFtpHost(String ftpHost) {
		this.ftpHost = ftpHost;
	}


	public String getFtpUserName() {
		return ftpUserName;
	}


	public void setFtpUserName(String ftpUserName) {
		this.ftpUserName = ftpUserName;
	}


	public String getFtpPassword() {
		return ftpPassword;
	}


	public void setFtpPassword(String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}


	public int getFtpPort() {
		return ftpPort;
	}


	public void setFtpPort(int ftpPort) {
		this.ftpPort = ftpPort;
	}


	public FTPClient getFtpClient() {
		return ftpClient;
	}


	public void setFtpClient(FTPClient ftpClient) {
		this.ftpClient = ftpClient;
	}


	public FTPConnection(String ftpHost, String ftpUserName,
			String ftpPassword, int ftpPort, FTPClient ftpClient) {
		this.ftpHost = ftpHost;
		this.ftpUserName = ftpUserName;
		this.ftpPassword = ftpPassword;
		this.ftpPort = ftpPort;
		this.ftpClient = ftpClient;
	}
	
	
	
}
