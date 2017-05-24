package com.bonc.ftptransfer.scan.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bonc.ftptransfer.scan.service.LogFileService;
import com.bonc.ftputil.dao.LogFileDao;
import com.bonc.ftputil.dao.impl.LogFileDaoImpl;
import com.bonc.ftputil.eum.FtpFileStatus;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.vo.LogFile;
import com.bonc.ftputil.vo.LogFileStatus;

public class LogFileServiceImpl implements LogFileService {
	
	private LogFileDao logFileDao;
	
	
	public LogFileServiceImpl(JdbcUtils jdbcUtil) {
		
		this.logFileDao = new LogFileDaoImpl(jdbcUtil);
		
		
	}

	@Override
	public Map<String, LogFile> queryLogFileByFKey(String[] fkeys) {
		
		
		List<LogFile> logFileList = this.logFileDao.queryLogFileByFKey(fkeys);
		
		if(logFileList == null || logFileList.size() == 0){
			return null;
		}
		
		Map<String, LogFile> logFileMap = new HashMap<String,LogFile>();
		
		for(LogFile logFile : logFileList){
			logFileMap.put(logFile.getFile_name(), logFile);
		}
		
		return logFileMap;
		
	}

	@Override
	public boolean saveFileInfo(LogFile newLogFile, LogFileStatus logFileStatus) {
		
		
		return this.logFileDao.saveFileInfo(newLogFile,logFileStatus);
		
	}

	@Override
	public boolean updateFileInfo(String f_id, FtpFileStatus filesizeerror,
			LogFile logFile, LogFileStatus logFileStatus) {
		
		return this.logFileDao.updateFileInfo(f_id,filesizeerror,logFile,logFileStatus);
	}

	@Override
	public Map<String, LogFile> queryLogFileAndStatus(String[] fileKeyArray) {
		
		List<LogFile> logFileList = this.logFileDao.queryLogFileAndStatus(fileKeyArray);
		
		if(logFileList == null || logFileList.size() == 0){
			return null;
		}
		
		Map<String, LogFile> logFileMap = new HashMap<String,LogFile>();
		
		for(LogFile logFile : logFileList){
			logFileMap.put(logFile.getF_key(), logFile);
		}
		
		return logFileMap;
	}

	@Override
	public LogFile queryLogFileAndStatus(String fileKey) {
		
		
		return this.logFileDao.queryLogFileAndStatus(fileKey);
	}

	@Override
	public boolean saveFileInfo(LogFile logFile) {
		
		try {
			if(1 == logFileDao.saveFileInfo(logFile)){
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

}
