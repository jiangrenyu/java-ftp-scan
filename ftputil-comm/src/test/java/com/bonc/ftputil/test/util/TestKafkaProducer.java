package com.bonc.ftputil.test.util;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Before;
import org.junit.Test;

import com.bonc.ftputil.bean.KafkaProducer;
import com.bonc.ftputil.util.FileUtil;

public class TestKafkaProducer {
	
	private KafkaProducer producer; 
	
	@Before
	public void setup() throws Exception{
		
		Properties pro = FileUtil.getProperteis("src/main/resources/config.properties");
		
		producer = new KafkaProducer(pro);
		
	}
	
	
	public void testSendOneMsg(){
		
		String topic = "file_info";
		
		String msg = "test one";
		
		producer.send(topic, msg);
	}
	
	public void testSendMultiMsg(){
		
		String topic = "file_info";
		
		List<String> msgList = new ArrayList<String>();
		
		msgList.add("test multi1");
		msgList.add("test multi2");
		msgList.add("test multi3");
		msgList.add("test multi4");
		
		producer.send(topic, msgList);
		
		
	}
	
	@Test
	public void test(){
		
//		Properties pro = null;
//		try {
//			pro = FileUtil.getProperteis("src/main/resources/config.properties");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		ProducerConfig config = new ProducerConfig(pro);
//		int events=100;
//        // 创建producer
//        Producer<String, String> producer = new Producer<String, String>(config);
//        // 产生并发送消息
//        long start=System.currentTimeMillis();
//        for (long i = 0; i < events; i++) {
//            long runtime = new Date().getTime();
//            String ip = "192.168.2." + i;//rnd.nextInt(255);
//            String msg = runtime + ",www.example.com," + ip;
//            //如果topic不存在，则会自动创建，默认replication-factor为1，partitions为0
//            KeyedMessage<String, String> data = new KeyedMessage<String, String>(
//                    "file_info", ip, msg);
//            producer.send(data);
//        }
//        System.out.println("耗时:" + (System.currentTimeMillis() - start));
//        // 关闭producer
//        producer.close();
//
	}
	
	
	public static void main(String[] args) {
		FTPClient ftpClient = new FTPClient();
		try {
//			ftpClient.connect("192.168.8.53", 2222);// 连接FTP服务器
			ftpClient.login("vascilpf", "vasc-ilpf");// 登陆FTP服务器
			ftpClient.setDataTimeout(60000);       	  //设置传输超时时间为60秒 
			ftpClient.setConnectTimeout(60000);       //连接超时为60秒
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			
			
			int i = ftpClient.pwd();
			
			System.out.println(i);
			
			
		} catch (SocketException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}
	}
	
}
