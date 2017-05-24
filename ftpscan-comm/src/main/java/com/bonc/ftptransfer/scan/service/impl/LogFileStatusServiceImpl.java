package com.bonc.ftptransfer.scan.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bonc.ftptransfer.scan.service.LogFileStatusService;
import com.bonc.ftputil.dao.LogFileStatusDao;
import com.bonc.ftputil.dao.impl.LogFileStatusDaoImpl;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.vo.LogFileStatus;

public class LogFileStatusServiceImpl implements LogFileStatusService{
	
	private LogFileStatusDao logFileStatusDao;
	
	public LogFileStatusServiceImpl(JdbcUtils jdbcUtil) {
		
		this.logFileStatusDao = new LogFileStatusDaoImpl(jdbcUtil);
		
	}
	
	
	@Override
	public Map<String, LogFileStatus> queryLogFileStatus(String[] fileKeyArray) {
		
		List<LogFileStatus> statusList = this.logFileStatusDao.queryLogFileStatus(fileKeyArray);
		
		if(statusList == null || statusList.size() == 0 ){
			return null;
		}
		
		HashMap<String, LogFileStatus> statusMap = new HashMap<String, LogFileStatus>();
		
		for(LogFileStatus logFileStatus : statusList){
			statusMap.put(logFileStatus.getF_key(), logFileStatus);
		}
		
		return statusMap;
	}


	@Override
	public boolean updateLogFileStatus(LogFileStatus logFileStatus) {
		
		return this.logFileStatusDao.updateLogFileStatus(logFileStatus);
	}

}
