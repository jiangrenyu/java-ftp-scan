package com.bonc.ftputil.bean;  

import com.jcraft.jsch.ChannelSftp;

/**
 * SFTPclient代理
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2016-1-18
 * @time 上午11:01:49 
 * 
 */
public class SFTPConnection {

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
	 * ftp Channel
	 */
	private ChannelSftp channelSftp ;
	
	

	public SFTPConnection(String ftpHost, String ftpUserName,
			String ftpPassword,int ftpPort, ChannelSftp channelSftp) {
		this.ftpHost = ftpHost;
		this.ftpUserName = ftpUserName;
		this.ftpPassword = ftpPassword;
		this.ftpPort = ftpPort;
		this.channelSftp = channelSftp;
	}


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


	public ChannelSftp getChannelSftp() {
		return channelSftp;
	}


	public void setChannelSftp(ChannelSftp channelSftp) {
		this.channelSftp = channelSftp;
	}
	
	
	
	
	
	
}
