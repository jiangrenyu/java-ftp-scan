package com.bonc.ftptransfer.scan.service.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import com.bonc.ftptransfer.scan.service.LogFileStatusService;
import com.bonc.ftputil.eum.FtpFileStatus;
import com.bonc.ftputil.eum.Operator;
import com.bonc.ftputil.eum.Valid;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.vo.LogFileStatus;

public class TestLogFileStatusServiceImpl {
	
	
	public void testQueryLogFileStatus() throws Exception{
		
		JdbcUtils jdbcUtil = new JdbcUtils("src/main/resources/dbcp.properties");
		
		LogFileStatusService logFileStatusService = new LogFileStatusServiceImpl(jdbcUtil);
		
		Map<String, LogFileStatus>  map = logFileStatusService.queryLogFileStatus(new String[]{"BFA59567B98799AD158E5853E20F82AD"});
		
		Iterator<String> iterator = map.keySet().iterator();
		
		while(iterator.hasNext()){
			
			String key = iterator.next();
			
			System.out.println(key+":"+map.get(key));
			
		}
		
	}
	
	@Test
	public void testUpdateLogFileStatus() throws Exception{
		
		JdbcUtils jdbcUtil = new JdbcUtils("src/main/resources/dbcp.properties");
		
		LogFileStatusService logFileStatusService = new LogFileStatusServiceImpl(jdbcUtil);
		
		LogFileStatus logFileStatus = new LogFileStatus();
		
		logFileStatus.setF_id("58EE5FA84A4D0EFEBE81F51EBD1228A0");
		logFileStatus.setF_key("A4618FB1D98331081E6E4D89D13FB8C5");
		logFileStatus.setIs_valid(Valid.VALID);
		logFileStatus.setOper_time(new Timestamp(new Date().getTime()));
		logFileStatus.setOperator(Operator.SCAN);
		logFileStatus.setRemark("");
		logFileStatus.setStatus(FtpFileStatus.UNDOWNLOAD);
		
		System.out.println("result:"+logFileStatusService.updateLogFileStatus(logFileStatus));
		
		
	}
	
}
