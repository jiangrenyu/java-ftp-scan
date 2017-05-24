package com.bonc.ftptransfer.scan.service;

import java.util.Map;

import com.bonc.ftputil.eum.FtpFileStatus;
import com.bonc.ftputil.vo.LogFile;
import com.bonc.ftputil.vo.LogFileStatus;

public interface LogFileService {

	/**
	 * 查询出所有 fkeys 集合中的文件扫描信息
	 * @param fkeys
	 * @return
	 */
	public Map<String,LogFile> queryLogFileByFKey(String[] fkeys);
	
	
	/**
	 * 保存扫描的文件基本信息、状态信息
	 * @param newLogFile
	 * @param logFileStatus
	 * @return
	 */
	public boolean saveFileInfo(LogFile newLogFile, LogFileStatus logFileStatus);

	
	/**
	 * 先将f_id为指定值的log_file_info置为无效，log_file_status状态置为对应的状态，并插入新的文件信息及状态
	 * @param f_id
	 * @param filesizeerror 
	 * @param logFile
	 * @param logFileStatus
	 * @return
	 */
	public boolean updateFileInfo(String f_id, FtpFileStatus filesizeerror, LogFile logFile,
			LogFileStatus logFileStatus);
	
	/**
	 * 查询出指定 fileKey 的文件扫描及状态信息
	 * @param fileKey
	 * @return
	 */
	public LogFile queryLogFileAndStatus(String fileKey);

	/**
	 * 查询出集合 fileKey 的所有文件扫描及状态信息
	 * @param fileKeyArray
	 * @return
	 */
	public Map<String, LogFile> queryLogFileAndStatus(String[] fileKeyArray);

	
	/**
	 * 保存logFileInfo 信息
	 * @param newLogFile
	 * @return
	 */
	public boolean saveFileInfo(LogFile newLogFile);

}
