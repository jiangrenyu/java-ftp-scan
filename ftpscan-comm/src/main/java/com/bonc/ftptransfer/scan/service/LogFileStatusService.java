package com.bonc.ftptransfer.scan.service;

import java.util.Map;

import com.bonc.ftputil.vo.LogFileStatus;

public interface LogFileStatusService {
	
	/**
	 * 查询出集合 fileKey 的所有文件状态信息
	 * @param fileKeyArray
	 * @return
	 */
	public Map<String, LogFileStatus> queryLogFileStatus(String[] fileKeyArray);

	
	/**
	 * 根据f_id更新log_ftp_status中的有效记录，并重新插入一条
	 * @param logFileStatus
	 * @return
	 */
	public boolean updateLogFileStatus(LogFileStatus logFileStatus);
}
