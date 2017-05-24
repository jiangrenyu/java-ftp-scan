package com.bonc.ftputil.dao.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftputil.beanconvert.BeanConverter;
import com.bonc.ftputil.dao.LogFileDao;
import com.bonc.ftputil.eum.FtpFileStatus;
import com.bonc.ftputil.eum.Valid;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.vo.LogFile;
import com.bonc.ftputil.vo.LogFileStatus;

public class LogFileDaoImpl implements LogFileDao{
	
	private Logger logger = LoggerFactory.getLogger(LogFileDaoImpl.class);

	
	private JdbcUtils jdbcUtil;
	
	
	public LogFileDaoImpl(JdbcUtils jdbcUtil) {

		this.jdbcUtil = jdbcUtil;
		
	}

	@Override
	public int saveFileInfo(LogFile logFile) throws Exception {
		
		String logFileSql = "insert into log_file_info (f_id , f_key, p_key, host_key, file_name ,remote_path , local_path , file_size ,remote_time,local_host, is_valid ,remark,oper_time) values(?,?,?,?,?,?,?,?,?,?,?,?,NOW())";
		
		Connection conn = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
				
			int logCount = queryRunner.update(conn, logFileSql, logFile.getF_id(),logFile.getF_key(),logFile.getP_key(),logFile.getHost_key(),logFile.getFile_name(),logFile.getRemote_path(),logFile.getLocal_path(),logFile.getFile_size(),logFile.getRemote_time(),logFile.getLocal_host(),Integer.parseInt(logFile.getIs_valid().getValue()),logFile.getRemark());
			
			return logCount;
			
		} catch (SQLException e) {
			
			logger.info("插入扫描文件信息失败，",e);
			
		}finally{
			
			try {
				jdbcUtil.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		return 0;
	}

	@Override
	public int setLogFileInValid(String f_id) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public LogFile queryLogFileByFKey(String f_key) throws Exception {
		// TODO Auto-generated method stub
		if(StringUtils.isEmpty(f_key)){
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("select f_id , f_key, p_key, host_key, file_name ,remote_path , local_path , file_size ,remote_time,local_host, oper_time, is_valid ,remark from log_file_info where f_key = ? and is_valid = ? order by oper_time desc");
		
		Connection conn = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
			
			BeanConverter convert = new BeanConverter();
			
			BasicRowProcessor rowProcessor = new BasicRowProcessor(convert);
			
			List<LogFile> logListFile = queryRunner.query(conn, sb.toString(), new BeanListHandler<LogFile>(LogFile.class,rowProcessor), f_key,Integer.parseInt(Valid.VALID.getValue()));
			
			return (logListFile==null||logListFile.size()==0)?null:logListFile.get(0);
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			
			try {
				jdbcUtil.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		return null;
	}

	@Override
	public List<LogFile> queryLogFileByFKey(String[] fkeys) {
		
		if(fkeys == null || fkeys.length == 0){
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("select f_id , f_key, p_key, host_key, file_name ,remote_path , local_path , file_size ,remote_time,local_host, oper_time, is_valid ,remark from log_file_info where f_key in ( ");
		
		for(String groupId : fkeys){
			sb.append("?,");
		}
		
		sb.deleteCharAt(sb.length()-1);
		
		sb.append(") and is_valid = ? order by oper_time desc");
		
		Connection conn = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
			
			BeanConverter convert = new BeanConverter();
			
			BasicRowProcessor rowProcessor = new BasicRowProcessor(convert);
			
			List<LogFile> list = queryRunner.query(conn, sb.toString(), new BeanListHandler<LogFile>(LogFile.class,rowProcessor), fkeys,Integer.parseInt(Valid.VALID.getValue()));
			
			return list;
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			
			try {
				jdbcUtil.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		return null;
	}

	@Override
	public boolean saveFileInfo(LogFile newLogFile, LogFileStatus logFileStatus) {
		
		String logFileSql = "insert into log_file_info (f_id , f_key, p_key, host_key, file_name ,remote_path , local_path , file_size ,remote_time,local_host, is_valid ,remark,oper_time) values(?,?,?,?,?,?,?,?,?,?,?,?,NOW())";
		
		String logFileStatusSql = "insert into log_file_status(f_id,f_key,status,oper_time,is_valid,remark,operator) values(?,?,?,NOW(),?,?,?)";
		
		Connection conn = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			conn.setAutoCommit(false);//设置手动提交
			
			QueryRunner queryRunner = new QueryRunner();
				
			int logCount = queryRunner.update(conn, logFileSql, newLogFile.getF_id(),newLogFile.getF_key(),newLogFile.getP_key(),newLogFile.getHost_key(),newLogFile.getFile_name(),newLogFile.getRemote_path(),newLogFile.getLocal_path(),newLogFile.getFile_size(),newLogFile.getRemote_time(),newLogFile.getLocal_host(),Integer.parseInt(newLogFile.getIs_valid().getValue()),newLogFile.getRemark());
			
			int statusCount = queryRunner.update(conn,logFileStatusSql,logFileStatus.getF_id(),logFileStatus.getF_key(),Integer.parseInt(logFileStatus.getStatus().getValue()),Integer.parseInt(logFileStatus.getIs_valid().getValue()),logFileStatus.getRemark(),Integer.parseInt(logFileStatus.getOperator().getValue()));
			
			conn.commit();
			
			if(logCount == 1 && statusCount == 1){
				return true;
			}
			
		} catch (SQLException e) {
			try {
				logger.info("插入扫描文件信息及状态失败，",e);
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
		}finally{
			
			try {
				jdbcUtil.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		return false;
	}

	@Override
	public boolean updateFileInfo(String f_id, FtpFileStatus filesizeerror,
			LogFile logFile, LogFileStatus logFileStatus) {
		
//		String updateFileSql = "update log_file_info set is_valid = ?,oper_time=NOW() where f_id = ? and is_valid = ?";
		String updateFileSql = "update log_file_info set is_valid = ? where f_id = ? and is_valid = ?";
		
//		String updateStatusSql = "update log_file_status set status = ? , is_valid = ?,oper_time=NOW() where f_id = ? and is_valid = ?";
		String updateStatusSql = "update log_file_status set status = ? , is_valid = ? where f_id = ? and is_valid = ?";
		
		String logFileSql = "insert into log_file_info (f_id , f_key, p_key, host_key, file_name ,remote_path , local_path , file_size ,remote_time,local_host, is_valid ,remark,oper_time) values(?,?,?,?,?,?,?,?,?,?,?,?,NOW())";
		
		String logFileStatusSql = "insert into log_file_status(f_id,f_key,status,oper_time,is_valid,remark,operator) values(?,?,?,NOW(),?,?,?)";
		
		Connection conn = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			conn.setAutoCommit(false);//设置手动提交
			
			QueryRunner queryRunner = new QueryRunner();
				
			int updateFileCount = queryRunner.update(conn, updateFileSql, Integer.parseInt(Valid.INVALID.getValue()),f_id,Integer.parseInt(Valid.VALID.getValue()));
			
			int updateStatusCount = queryRunner.update(conn, updateStatusSql, Integer.parseInt(filesizeerror.getValue()),Integer.parseInt(Valid.INVALID.getValue()),f_id,Integer.parseInt(Valid.VALID.getValue()));
			
			int logCount = queryRunner.update(conn, logFileSql, logFile.getF_id(),logFile.getF_key(),logFile.getP_key(),logFile.getHost_key(),logFile.getFile_name(),logFile.getRemote_path(),logFile.getLocal_path(),logFile.getFile_size(),logFile.getRemote_time(),logFile.getLocal_host(),Integer.parseInt(logFile.getIs_valid().getValue()),logFile.getRemark());
			
			int statusCount = queryRunner.update(conn,logFileStatusSql,logFileStatus.getF_id(),logFileStatus.getF_key(),Integer.parseInt(logFileStatus.getStatus().getValue()),Integer.parseInt(logFileStatus.getIs_valid().getValue()),logFileStatus.getRemark(),Integer.parseInt(logFileStatus.getOperator().getValue()));
			
			conn.commit();
			
			if(updateFileCount == 1 && updateStatusCount == 1 && logCount == 1 && statusCount == 1){
				return true;
			}else{
				conn.rollback();
				logger.info("更新扫描文件信息及状态失败，回滚...");
			}
			
		} catch (SQLException e) {
			try {
				logger.info("更新扫描文件信息及状态失败，",e);
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
		}finally{
			
			try {
				jdbcUtil.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		
		return false;
	}

	@Override
	public List<LogFile> queryLogFileAndStatus(String[] fileKeyArray) {
		
		if(fileKeyArray == null || fileKeyArray.length == 0){
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT f.f_id ,f.f_key,f.p_key, f.host_key,f.file_name , f.remote_path ,  f.local_path , f.file_size , f.remote_time,f.local_host,f.oper_time, f.is_valid , f.remark,  s.status    fileStatus, s.oper_time status_oper_time,s.remark    status_remark FROM  log_file_info f , log_file_status s  WHERE  f.f_id = s.f_id   AND f.is_valid = ? AND  f.f_key IN (");
		
		for(String groupId : fileKeyArray){
			sb.append("?,");
		}
		
		sb.deleteCharAt(sb.length()-1);
		
		sb.append(")");
		
		Connection conn = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
			
			BeanConverter convert = new BeanConverter();
			
			BasicRowProcessor rowProcessor = new BasicRowProcessor(convert);
			
			List<LogFile> list = queryRunner.query(conn, sb.toString(), new BeanListHandler<LogFile>(LogFile.class,rowProcessor), Integer.parseInt(Valid.VALID.getValue()),fileKeyArray);
			
			return list;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			
			try {
				jdbcUtil.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		return null;
	}

	@Override
	public LogFile queryLogFileAndStatus(String fileKey) {
		
		if(fileKey == null){
			return null;
		}
		
		String sql = "SELECT f.f_id ,f.f_key,f.p_key, f.host_key,f.file_name , f.remote_path ,  f.local_path , f.file_size , f.remote_time,f.local_host,f.oper_time, f.is_valid , f.remark,  s.status    fileStatus, s.oper_time status_oper_time,s.remark    status_remark FROM  log_file_info f , log_file_status s  WHERE  f.f_id = s.f_id   AND f.is_valid = ? AND  f.f_key = ? ";
		
		Connection conn = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
			
			BeanConverter convert = new BeanConverter();
			
			BasicRowProcessor rowProcessor = new BasicRowProcessor(convert);
			
			LogFile logFile = queryRunner.query(conn, sql, new BeanHandler<LogFile>(LogFile.class,rowProcessor), Integer.parseInt(Valid.VALID.getValue()),fileKey);
			
			return logFile;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			
			try {
				jdbcUtil.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		return null;
	}

}
