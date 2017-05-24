package com.bonc.ftputil.test.bean;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftputil.bean.SimpleFTPServer;




public class TestFTPServer {
	
	private static Logger LOG = LoggerFactory.getLogger(TestFTPServer.class);
	
	
	public void testDownloadFile(){
		
		String hostname = "192.168.8.53";
		
		int port = 2222;
		
		String username = "vascilpf";
		
		String password = "vasc-ilpf";
		
		String remoteFileName = "/itf/vasc/dpi/qixin/shanghai/storm-kafka-jdbc.log.2015-11-28";
		
		String localFileName = "E:/storm.log";
				
		int timeout = 30000;
//		String hostname = "192.168.1.100";
//		
//		int port = 21;
		
//		String username = "anonymous";
//		String username = "root";
//		
//		String password = "root@123";
//		
//		String remoteFileName = "/demo/index.html";
//		
//		String localFileName = "E:/test.html";
		
		
		SimpleFTPServer ftpServer = new SimpleFTPServer();
		
		if(ftpServer.connectServer(hostname, port, username, password,timeout)){
			
			if(ftpServer.downloadFile(remoteFileName, localFileName)){
				LOG.info("下载成功");
			}else{
				LOG.info("下载失败");
			}
			
		}else{
			LOG.info("连接失败");
		}
		
		
		
	}
	
	
	@Test
	public void testListDirectory() throws IOException{
		String hostname = "192.168.8.51";
		
		int port = 22;
		
		String username = "bonc";
		
		String password = "bonc";
		
		String remoteFileName = "/home/bonc/zhy_test";
		
		int timeout = 30000;
		
//		String localFileName = "E:/magic.jpg";
		
		SimpleFTPServer ftpServer = new SimpleFTPServer();
		
		if(ftpServer.connectServer(hostname, port, username, password,timeout)){
			
			System.out.println("连接成功");
			
			List<HashMap<String,Object>> fileList = ftpServer.listFile(remoteFileName);
			
			for(HashMap<String,Object> obj : fileList){
				System.out.println(obj);
			}
			
			
		}else{
			System.out.println("连接失败");
		}
		
	}
	
	
}

