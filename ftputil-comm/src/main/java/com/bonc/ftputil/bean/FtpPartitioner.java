package com.bonc.ftputil.bean;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;
import java.util.Random;



public class FtpPartitioner implements Partitioner {
	
	private int l = 0;
	
	public FtpPartitioner( ){
	}
	
	@Override
	public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
//		System.out.println("num:"+l);
//		return (l++)%numPartitions;
		
	   Random random = new Random();
	   
	   return random.nextInt(cluster.partitionCountForTopic(topic));
		 
//		if (key == null) {
//            Random random = new Random();
//            return Math.abs(random.nextInt())%numPartitions;
//        }else {
//            int result = Math.abs(key.hashCode())%numPartitions; 
//            return result;
//        }
		
	}



	@Override
	public void close() {

	}

	@Override
	public void configure(Map<String, ?> configs) {

	}
}
