package com.bonc.ftputil.test.bean;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftputil.bean.SFTPServer;
import com.jcraft.jsch.SftpException;

public class TestSFTPServer {
	
	private Logger LOG = LoggerFactory.getLogger(TestSFTPServer.class);
	
	private SFTPServer sftpServer = null;
	
	@Before
	public void login(){
		
		String ip = "192.168.8.51";
		
		int port = 22;
		
		String username = "bonc";
		
		String password = "bonc";
		
		int timeout = 30000;

		
		sftpServer = new SFTPServer();
		
		if(sftpServer.connectServer(ip, port, username, password,timeout)){
			LOG.info("登录成功");
			
		}else{
			//连接失败
			LOG.info("登录失败");
		}
		
	}
	
	public void testListFile() throws SftpException{
		
		List<HashMap<String,Object>> resultList =  sftpServer.listFile("/home/bonc/zhy_test");
//		List<HashMap<String,Object>> resultList =  sftpServer.listFile("/home/bonc/test");
		
		for(HashMap<String,Object> obj : resultList){
			System.out.println(obj);
		}
		
	}
	
	@Test
	public void testListFileAndDir() throws SftpException{
		
		List<HashMap<String,Object>> resultList =  sftpServer.listFileAndDirectory("/home/bonc/test_xyl");
//		List<HashMap<String,Object>> resultList =  sftpServer.listFile("/home/bonc/test");
		
		for(HashMap<String,Object> obj : resultList){
			System.out.println(obj);
		}
		//1449927750
		//1449977887935
		long l = 1449927750L * 1000;
		System.out.println(new Timestamp(l));
		
		System.out.println("current:"+(new Date()).getTime());
	}
	
	
	public void testDownloadFile(){
		
		sftpServer.downloadFile("/home/bonc/test_xyl/site-1.8.20.zip", "E:/site-1.8.20.zip");
		
	}
	
	
	@After
	public void closeClient(){
		sftpServer.closeFTPClient();
		
		LOG.info("关闭连接");
	}
	
}
