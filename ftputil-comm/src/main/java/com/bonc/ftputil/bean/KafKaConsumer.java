package com.bonc.ftputil.bean;  

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftputil.dao.FtpPathDao;
import com.bonc.ftputil.dao.impl.FtpPathDaoImpl;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.vo.FtpPath;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

/**
 * Kafka消费类
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-4
 * @time 下午11:01:10 
 * 
 */
public class KafKaConsumer {
	
	 private static final Logger logger = LoggerFactory.getLogger(KafKaConsumer.class); 
	 
	 /**
	  * 下载分组编号
	  */
	 private String downloadGroupId ;


	public void setDownloadGroupId(String downloadGroupId) {
		this.downloadGroupId = downloadGroupId;
	}


	/**
	  * ftp文件路径操作DAO
	  */
	 private FtpPathDao ftpPathDao ; 
	 
	 /**
	  * 配置文件
	  */
	 private Properties config ;
	 
	 
	 /**
	  * kafka消息生成者
	  */
	 private KafkaProducer kafkaProducer;
	 
	 
	 /**
	  * jdbcUtil
	  */
	 private JdbcUtils jdbcUtils ;
	 
	 
	 public static Map<FtpClientKey,ConcurrentLinkedQueue<Object>> ftpClients = new ConcurrentHashMap<FtpClientKey, ConcurrentLinkedQueue<Object>>() ;
	 
	 
	 
	 public static String downloadSucTopic;
	 
	 
	 public static boolean isOIDD ;
	 
	 /**
	  * 
	  */
	 public static File hdfsConfFile; 
	 
	 
	 /**
	  * ftp buffer size 
	  */
	 public static int ftpBufferSize ;
	 
	 /**
	  * sftp buffer size 
	  */
	 public static int sftpBufferSize ;
	 
	 
	 /**
	  * 下载的并发数
	  */
	 public static int downloadThreadCount ;
	 
	 
	 /**
	  * ftp下载超时时间(单位:秒)
	  */
	 public static int ftpDownloadTimeOut ;
	 
	 /**
	  * sftp下载超时时间(单位:秒)
	  */
	 public static int sftpDownloadTimeOut;
	 
	 
	 /**
	  * OIDD是否删除下载成功后的文件
	  */
	 public static boolean rm_flag ;
	 
	 
	 

     public KafKaConsumer(Properties config,String downloadGroupId,JdbcUtils jdbcUtils,boolean isOIDD) {
    	
    	this.downloadGroupId = downloadGroupId ;
		
    	this.config = config ;
		
		ftpPathDao = new FtpPathDaoImpl(jdbcUtils);
		
		this.kafkaProducer = new KafkaProducer(config);
		
		downloadSucTopic = config.getProperty("downloadSucTopic");
		
		KafKaConsumer.isOIDD =  isOIDD  ;
		
		ftpBufferSize = Integer.parseInt(config.getProperty("ftpBufferSize", 1024*1024+""));
		
		sftpBufferSize = Integer.parseInt(config.getProperty("sftpBufferSize", 1024*1024+""));
		
		downloadThreadCount = Integer.parseInt(config.getProperty("downloadThreadCount", "1"));
		
		ftpDownloadTimeOut = Integer.parseInt(config.getProperty("ftpDownloadTimeOut","60"));
		
		sftpDownloadTimeOut = Integer.parseInt(config.getProperty("sftpDownloadTimeOut","60"));
		
		this.jdbcUtils = jdbcUtils;
		
		rm_flag = Boolean.parseBoolean(config.getProperty("rm_flag", "false"));
		
	 }
	 
     
     public void consume(){
    	 
    	 /**
    	  * 根据groupId查询所有的ftp扫描路径
    	  */
    	 
    	 FtpPath ftpPath = ftpPathDao.queryDownloadGroupId(downloadGroupId);
    	 
    	 String topic = ftpPath.getTopic();
		 
		 String client_id = ftpPath.getTopicClientId();
    		 
		 /**
		  * 根据topic来进行消费，一个消费者对应一个topic
		  */
		
		 Map<String, Integer> topicMap = new HashMap<String, Integer>();
		 
		 int partitionCount = Integer.parseInt(config.getProperty("partitionCount", "7"));
		 
		 int consumerThreadCount = Integer.parseInt(config.getProperty("consumerThreadCount", "1"));
		 
		 topicMap.put(topic, partitionCount);
		 
		 config.put("group.id", client_id);
		 
		 ConsumerConnector consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(config));
		 
		 Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicMap);
		 
		 for (Iterator<Map.Entry<String,List<KafkaStream<byte[],byte[]>>>> iterator = consumerMap.entrySet().iterator(); iterator
				.hasNext();) {
			 Map.Entry<String,List<KafkaStream<byte[],byte[]>>> entry = iterator.next();
			 
			 List<KafkaStream<byte[], byte[]>> streams = entry.getValue();
   		  
             // now launch all the threads
//			 ExecutorService executor = Executors.newFixedThreadPool(consumerThreadCount);
      
             // now create an object to consume the messages
             //
             int threadNumber = 0;
             
             for (final KafkaStream<byte[], byte[]> stream : streams) {
            	 
//                 executor.submit();
            	 new KafkaConsumerMsgTask(stream,threadNumber,config,downloadGroupId,jdbcUtils).run();
                 
                 threadNumber++;
                 
             }
             
		}
    		 
    		 
    		 
    		 
    	 
    	 
    	 
//    	 int period =Integer.parseInt(this.config.getProperty("period", "10")); 
//     	 
//    	 int scheduledThreads = Integer.parseInt(this.config.getProperty("scheduledThreads", "2")); 
//    	 
//    	 ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(scheduledThreads);
//    	 
//    	 scheduledExecutorService.scheduleAtFixedRate(new ScheduledDownloadTask(this.messageList,logFileDao,logFileStatusDao,kafkaProducer), 0, period, TimeUnit.MINUTES);
    	 
     }
	
}
