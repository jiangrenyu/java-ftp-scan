package com.bonc.ftputil.bean;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.*;


public class KafkaProducer {
	
	private Producer<String,String> producer;
	
	public KafkaProducer(Properties config){
		Map conf = new HashMap<>();
		conf.putAll(config);
		this.producer = new org.apache.kafka.clients.producer.KafkaProducer<String, String>(conf);
	}
	public KafkaProducer(Map<String ,Object> config){

		this.producer = new org.apache.kafka.clients.producer.KafkaProducer<String, String>(config);
	}
	
	public void send(String topic,String msg){
		
		ProducerRecord<String, String> message = new ProducerRecord<String, String>(topic, msg);
		this.producer.send(message);
	}
	
	
	public void send(String topic,List<String> msgList){

		for(String msg : msgList){
			ProducerRecord<String, String> message = new ProducerRecord<String, String>(topic, msg);
			this.producer.send(message);
		}
		
	}
	
	
}
