package com.bonc.ftputil.bean;  

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftputil.dao.LogFileDao;
import com.bonc.ftputil.dao.LogFileStatusDao;

/**
 * 异步的按照时间去做下载的任务
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-6
 * @time 下午10:23:28 
 * 
 */
public class ScheduledDownloadTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ScheduledDownloadTask.class);
	
	
	private List<String> messageList;
	
	 /**
	  * ftp 文件操作DAO
	  */
	private LogFileDao logFileDao ;
	 
	 /**
	  * 文件状态操作DAO
	  */
	private LogFileStatusDao logFileStatusDao ;
	 
	 /**
	  * 生产者
	  */
	private KafkaProducer kafkaProducer;
	
	
	
	
	public ScheduledDownloadTask(List<String> messageList,LogFileDao logFileDao,LogFileStatusDao logFileStatusDao,KafkaProducer kafkaProducer) {
		
		this.messageList = messageList ;
		
		this.logFileDao = logFileDao ;
		
		this.logFileStatusDao = logFileStatusDao ;
		
		
	}
	
	@Override
	public void run() {
		
		logger.info("execute schedual download thread begin");
		synchronized (messageList) {
			final List<String> downloadList = new ArrayList<String>();
			
			Collections.addAll(downloadList, new String[messageList.size()]);
			Collections.copy(downloadList, messageList);
			messageList.clear();
//			KafkaConsumerMsgTask.download(downloadList, logFileStatusDao, logFileDao, kafkaProducer);
		}
		
		
	}

}
