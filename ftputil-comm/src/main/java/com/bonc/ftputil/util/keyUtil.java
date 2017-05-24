package com.bonc.ftputil.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class keyUtil {
	
	/**
	 * 生成一个唯一的UUID编码
	 * @return
	 */
	public static String getUUID(){
		
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	
	/**
	 * 获取本机IP地址
	 * @return
	 */
	public static String getLocalIp(){
		
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
