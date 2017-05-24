package com.bonc.ftputil.dao;  

import java.util.List;

import com.bonc.ftputil.eum.FtpFileStatus;
import com.bonc.ftputil.vo.LogFile;
import com.bonc.ftputil.vo.LogFileStatus;

/**
 * LogFile Dao
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-4
 * @time 下午3:38:40 
 * 
 */
public interface LogFileDao {

	
	/**
	 * 
	 * 
	 * 保存文件扫描信息留存
	 * @param logFile
	 * @return
	 * @throws Exception
	 * 
	 *
	 */
	public int saveFileInfo(LogFile logFile) throws Exception ;
	
	
	
	/**
	 * 
	 * 
	 * 将扫描信息置为无效状态
	 * @param f_id   文件扫描主键
	 * @return
	 * @throws Exception
	 * 
	 *
	 */
	public int setLogFileInValid(String f_id) throws Exception ;
	
	
	
	/**
	 * 
	 * 根据目录+文件名生成的md5去查询logFile
	 * 
	 * @param f_key
	 * @return
	 * @throws Exception
	 * 
	 *
	 */
	public LogFile queryLogFileByFKey(String f_key) throws Exception ;


	
	/***
	 * 根据目录+文件名生成的md5集合去查询logFile
	 * @param fkeys
	 * @return
	 */
	public List<LogFile> queryLogFileByFKey(String[] fkeys);


	/**
	 * 保存扫描到的文件信息及状态
	 * @param newLogFile
	 * @param logFileStatus
	 * @return
	 */
	public boolean saveFileInfo(LogFile newLogFile, LogFileStatus logFileStatus);


	/**
	 * 先将f_id为指定值的log_file_info置为无效，log_file_status状态置为大小不一致
	 * @param f_id
	 * @param filesizeerror
	 * @param logFile
	 * @param logFileStatus
	 * @return
	 */
	public boolean updateFileInfo(String f_id, FtpFileStatus filesizeerror,
			LogFile logFile, LogFileStatus logFileStatus);


	
	/***
	 * 根据目录+文件名生成的md5集合去查询logFile(包含状态信息)
	 * @param fkeys
	 * @return
	 */
	public List<LogFile> queryLogFileAndStatus(String[] fileKeyArray);


	/**
	 * 根据 fileKey 查询文件及状态信息
	 * @param fileKey
	 * @return
	 */
	public LogFile queryLogFileAndStatus(String fileKey);
	
	
	
}
