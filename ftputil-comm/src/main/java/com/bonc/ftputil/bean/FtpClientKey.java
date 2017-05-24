package com.bonc.ftputil.bean;  

import java.util.HashMap;
import java.util.Map;


/**
 * ftpclient key ，用于优化下载时多次创建ftpclient的问题
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-11
 * @time 下午3:50:38 
 * 
 */
public class FtpClientKey {
	/**
	 * ftp主机ip地址
	 */
	private String ftp_ip ;
	
	/**
	 * ftp用户名
	 */
	private String ftp_userName ;

	public String getFtp_ip() {
		return ftp_ip;
	}

	public void setFtp_ip(String ftp_ip) {
		this.ftp_ip = ftp_ip;
	}

	public String getFtp_userName() {
		return ftp_userName;
	}

	public void setFtp_userName(String ftp_userName) {
		this.ftp_userName = ftp_userName;
	}
	
	
	
	
	@Override
	public int hashCode() {
		 int ret = (ftp_ip+"").hashCode() ^ (ftp_userName+"").hashCode();
		 
		 return ret ;
	}

	@Override
	public boolean equals(Object obj) {
		FtpClientKey ftpClientKey = (FtpClientKey) obj ;
		
		if(this.getFtp_ip().equals(ftpClientKey.getFtp_ip())&&this.getFtp_userName().equals(ftpClientKey.getFtp_userName())){
			
			return true ;
		}
		
		return false;
	}

	
	public static void main(String[] args) {
		
		Map<FtpClientKey,String> map = new HashMap<>();
		
		FtpClientKey clientKey = new FtpClientKey();
		
		clientKey.setFtp_ip("192.168.8.20");
		
		clientKey.setFtp_userName("user");
		
		
		map.put(clientKey, "abc");
		
		
		FtpClientKey clientKey1 = new FtpClientKey();
		
		clientKey1.setFtp_ip("192.168.8.20");
		
		clientKey1.setFtp_userName("user");
		
		
		
		System.out.println(map.get(clientKey1));
		
		
		
		
	}

	
	
	
	
}
