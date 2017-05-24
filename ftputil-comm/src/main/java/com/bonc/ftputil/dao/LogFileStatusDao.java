package com.bonc.ftputil.dao;  

import java.sql.SQLException;
import java.util.List;

import com.bonc.ftputil.eum.FtpFileStatus;
import com.bonc.ftputil.eum.Valid;
import com.bonc.ftputil.vo.LogFileStatus;

/**
 * 文件状态DAO
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-4
 * @time 下午3:58:15 
 * 
 */
public interface LogFileStatusDao {

	
	/**
	 * 
	 * 
	 * 更改文件状态信息
	 * @param f_id   文件扫描主键
	 * @return
	 * @throws Exception
	 * 
	 *
	 */
	public void updateLogFileStatus(String f_id,String f_key,FtpFileStatus ftpFileStatus) throws SQLException ;
	/**
	 * 
	 * 
	 * 更改文件状态信息
	 * @param f_id   文件扫描主键
	 * @param valid  是否有效
	 * @return
	 * @throws Exception
	 * 
	 *
	 */
	public int updateLogFileValid(String f_id,Valid valid) throws SQLException ;
	/**
	 * 
	 * 
	 * 更改文件状态信息
	 * @param f_id   文件扫描主键
	 * @param valid  是否有效
	 * @return
	 * @throws Exception
	 * 
	 *
	 */
	public int updateLogFileValidAndFileStatus(String f_id,Valid valid,FtpFileStatus ftpFileStatus) throws SQLException ;
	
	
	
	/**
	 * 
	 * 
	 * 保存文件状态
	 * @param logFileStatus
	 * @return
	 * @throws Exception
	 * 
	 *
	 */
	public int saveLogFileStatus(LogFileStatus logFileStatus ) throws SQLException ;
	
	
	
	/**
	 * 
	 * 
	 * 通过f_key来更新文件状态
	 * @param f_key
	 * @param ftpFileStatus
	 * @return
	 * @throws SQLException
	 * 
	 *
	 */
	public int updateLogFileStatusByFkey(String f_key,FtpFileStatus ftpFileStatus) throws SQLException ;
	
	/**
	 * 根据f_id更新log_ftp_status中的有效记录，并重新插入一条
	 * @param logFileStatus
	 * @return
	 */
	public boolean updateLogFileStatus(LogFileStatus logFileStatus);

	/**
	 * 根据F_key稽核查询有效的文件状态信息
	 * @param fileKeyArray
	 * @return
	 */
	public List<LogFileStatus> queryLogFileStatus(String[] fileKeyArray);
	/**
	 * 根据F_key稽核查询有效的文件状态信息
	 * @param fileKeyArray
	 * @return
	 */
	public int queryLogFileStatus(String f_id);
}
