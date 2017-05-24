package com.bonc.ftputil.test.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
	
	@Test
	public void testSendMultiMsg(){
		
		String topic = "file_info";
		
		List<String> msgList = new ArrayList<String>();
		
		msgList.add("test multi1");
		msgList.add("test multi2");
		msgList.add("test multi3");
		msgList.add("test multi4");
		
		producer.send(topic, msgList);
		
		
	}
	
	
}
