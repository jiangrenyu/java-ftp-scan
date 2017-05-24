package com.bonc.ftputil.dao.impl;  

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftputil.beanconvert.BeanConverter;
import com.bonc.ftputil.dao.LogFileStatusDao;
import com.bonc.ftputil.eum.FtpFileStatus;
import com.bonc.ftputil.eum.Operator;
import com.bonc.ftputil.eum.Valid;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.vo.LogFileStatus;

/**
 * 文件状态DAOImpl
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-7
 * @time 下午11:24:48 
 * 
 */
public class LogFileStatusDaoImpl implements LogFileStatusDao {
	
	private Logger logger = LoggerFactory.getLogger(LogFileStatusDaoImpl.class);
	
	private JdbcUtils jdbcUtil;
	
	public LogFileStatusDaoImpl(JdbcUtils jdbcUtil) {
		this.jdbcUtil = jdbcUtil;
	}

	@Override
	public void updateLogFileStatus(String f_id,String f_key, FtpFileStatus ftpFileStatus)
			throws SQLException {
		
		String updateStatusSql = "update log_file_status set is_valid = "+Valid.INVALID+",oper_time=NOW() where f_id = '"+f_id+"' and is_valid = "+Valid.VALID;
		
		Connection conn = null;
		
		Operator operator = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
				
			int updateStatusCount = queryRunner.update(conn, updateStatusSql);
			
			logger.info("execute sql:"+updateStatusSql+"\r\n affect rows:"+updateStatusCount);
			
			if(updateStatusCount==0){
				
				operator = Operator.DOWNLOAD_ADD;
						
			}else{
				
				operator = Operator.DOWNLOAD_UPDATE ;
				
			}
			
			String logFileStatusSql = "insert into log_file_status(f_id,f_key,status,oper_time,is_valid,remark,operator) values('"+f_id+"','"+f_key+"',"+ftpFileStatus.getValue()+",NOW(),"+Valid.VALID.getValue()+",'',"+operator.getValue()+")";
			
			int statusCount = queryRunner.update(conn,logFileStatusSql);
			
			logger.info("execute sql:"+logFileStatusSql+"\r\n affect rows:"+statusCount);
			
		} catch (SQLException e) {
			logger.info("更新扫描文件状态失败，",e);
			
		}finally{
			
			try {
				jdbcUtil.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		
	}

	@Override
	public int updateLogFileValid(String f_id, Valid valid) throws SQLException {
		
		String updateSql = "update log_file_status set is_valid = "+Integer.parseInt(valid.getValue())+", oper_time = NOW() where f_id = '"+f_id+"' and is_valid ="+Valid.VALID;
		
		int updateFileCount = 0 ;
		
		Connection conn = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
				
			updateFileCount = queryRunner.update(conn, updateSql);
			
			logger.info("execute sql :"+updateSql+";\r\naffect rows:"+updateFileCount);
			
			if(updateFileCount != 1 ){
				logger.info("update log_file_status valid to "+valid+" f_id:"+f_id+" error");
			}
			
		} catch (SQLException e) {
			
			logger.info("更新失败，",e);
		
			
		}finally{
			
			try {
				jdbcUtil.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		
		return updateFileCount;
	}

	@Override
	public int updateLogFileValidAndFileStatus(String f_id, Valid valid,
			FtpFileStatus ftpFileStatus) throws SQLException {
		
		String updateSql = "update log_file_status set is_valid = "+Integer.parseInt(valid.getValue())+",status = "+Integer.parseInt(ftpFileStatus.getValue())+", oper_time = NOW() where f_id = '"+f_id+"' and is_valid ="+Valid.VALID;
		
		int updateFileCount = 0 ;
		
		Connection conn = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
				
			updateFileCount = queryRunner.update(conn, updateSql);
			
			logger.info("execute sql :"+updateSql+";\r\naffect rows:"+updateFileCount);
			
			
			if(updateFileCount != 1 ){
				logger.info("update log_file_status valid to "+valid+" f_id:"+f_id+" error");
			}
			
		} catch (SQLException e) {
			
			logger.info("更新失败，",e);
			
			
		}finally{
			
			try {
				jdbcUtil.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		
		return updateFileCount;
	}

	@Override
	public int saveLogFileStatus(LogFileStatus logFileStatus)
			throws SQLException {
		
		String logFileStatusSql = "insert into log_file_status(f_id,f_key,status,oper_time,is_valid,remark) values('"+logFileStatus.getF_id()+"','"+logFileStatus.getF_key()+"',"+logFileStatus.getStatus().getValue()+",NOW(),"+logFileStatus.getIs_valid().getValue()+",'"+(logFileStatus.getRemark()==null?"":logFileStatus.getRemark())+"')";
		
		Connection conn = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
				
			int statusCount = queryRunner.update(conn,logFileStatusSql);
			
			logger.info("execute sql :"+logFileStatusSql+";\r\naffect rows:"+statusCount);
			
			if(statusCount != 1){
				logger.info("insert log_file_status error");
			}
			
		} catch (SQLException e) {
			logger.info("insert error，",e);

			
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
	public int updateLogFileStatusByFkey(String f_key,
			FtpFileStatus ftpFileStatus) throws SQLException {
		
		String updateSql = "update log_file_status set status = "+Integer.parseInt(ftpFileStatus.getValue())+", oper_time = NOW() where f_key = '"+f_key+"' and is_valid ="+Valid.VALID;
		
		int updateFileCount = 0 ;
		
		Connection conn = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
				
			updateFileCount = queryRunner.update(conn, updateSql);
			
			logger.info("execute sql :"+updateSql+";\r\naffect rows:"+updateFileCount);
			
			
			if(updateFileCount != 1 ){
				logger.info("update log_file_status status to "+ftpFileStatus+" f_key:"+f_key+" error");
			}
			
		} catch (SQLException e) {
			
			logger.info("更新失败，",e);
			
		}finally{
			
			try {
				
				jdbcUtil.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		
		return updateFileCount;
	}
	
	@Override
	public List<LogFileStatus> queryLogFileStatus(String[] fileKeyArray) {
		
		if(fileKeyArray == null || fileKeyArray.length == 0){
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("select f_id , f_key , status ,oper_time, is_valid , remark ,operator from log_file_status where is_valid = "+Valid.VALID+" and f_key in (");
		
		for(String fileKey : fileKeyArray){
			
			sb.append("?,");
		}
		
		sb.deleteCharAt(sb.length() -1 );
		
		sb.append(") order by oper_time asc");
		
		Connection conn = null;
		 
		try {
			conn = jdbcUtil.getConnection();
				
			QueryRunner queryRunner = new QueryRunner();
			
			BeanConverter convert = new BeanConverter();
			
			BasicRowProcessor rowProcessor = new BasicRowProcessor(convert);
			
			List<LogFileStatus> list = queryRunner.query(conn, sb.toString(), new BeanListHandler<LogFileStatus>(LogFileStatus.class,rowProcessor), fileKeyArray);
			
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
	public boolean updateLogFileStatus(LogFileStatus logFileStatus) {
		
//		String updateStatusSql = "update log_file_status set is_valid = "+Valid.INVALID+",oper_time=NOW() where f_id = '"+logFileStatus.getF_id()+"' and is_valid = "+Valid.VALID;
		String updateStatusSql = "update log_file_status set is_valid = "+Valid.INVALID+" where f_id = '"+logFileStatus.getF_id()+"' and is_valid = "+Valid.VALID;
		
		String logFileStatusSql = "insert into log_file_status(f_id,f_key,status,oper_time,is_valid,remark,operator) values('"+logFileStatus.getF_id()+"','"+logFileStatus.getF_key()+"',"+logFileStatus.getStatus().getValue()+",NOW(),"+logFileStatus.getIs_valid().getValue()+",'"+(logFileStatus.getRemark()==null?"":logFileStatus.getRemark())+"',"+logFileStatus.getOperator().getValue()+")";
		
		Connection conn = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			conn.setAutoCommit(false);//设置手动提交
			
			QueryRunner queryRunner = new QueryRunner();
				
			int updateStatusCount = queryRunner.update(conn, updateStatusSql );
			
			logger.info("execute sql :"+updateStatusSql+";\r\naffect rows:"+updateStatusCount);
			
			int statusCount = queryRunner.update(conn,logFileStatusSql);
			
			logger.info("execute sql :"+logFileStatusSql+";\r\naffect rows:"+statusCount);
			
			conn.commit();
			
			if(updateStatusCount == 1 && statusCount == 1){
				return true;
			}else{
				conn.rollback();
				logger.info("更新扫描文件状态失败，回滚...");
			}
			
		} catch (SQLException e) {
			try {
				logger.info("更新扫描文件状态失败，",e);
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
	public int queryLogFileStatus(String f_id) {
		
		String sql = "select count(1) count from log_file_status where f_id = '"+f_id+"' and is_valid = "+Valid.VALID.getValue() ;
		
		int count  = 0 ;
		
		Connection conn = null;
		
		try {
			
			conn = jdbcUtil.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
				
			count = ((Long) queryRunner.query(conn, sql, new MapHandler()).get("count")).intValue();
			
			logger.info("execute sql :"+sql+";\r\naffect rows:"+count);
			
		} catch (SQLException e) {
			
			logger.error("查询扫描文件状态失败，",e);
			
		}finally{
			
			try {
				jdbcUtil.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		return count;
	}
	
	
}
