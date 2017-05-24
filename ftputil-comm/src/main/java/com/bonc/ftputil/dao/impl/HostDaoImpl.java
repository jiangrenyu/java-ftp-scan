package com.bonc.ftputil.dao.impl;  

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftputil.beanconvert.BeanConverter;
import com.bonc.ftputil.dao.HostDao;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.vo.Host;


/**
 * HostDao实现
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-2
 * @time 下午10:19:31 
 * 
 */
public class HostDaoImpl implements HostDao {
	
	private Logger logger = LoggerFactory.getLogger(HostDaoImpl.class);
	
	private JdbcUtils jdbcUtils ;
	
	public HostDaoImpl(JdbcUtils jdbcUtils){
		
		this.jdbcUtils = jdbcUtils ;
		
	}

	@Override
	public int saveHost(Host host) throws Exception {
		
		String sql = "insert into conf_host_info(host_key,ip,host_name,ftp_port,ftp_name,ftp_pwd,default_path,is_valid,remark) values(?,?,?,?,?,?,?,?,?)";
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		int updateCount = 0 ;
		
		try {
			
			conn = jdbcUtils.getConnection();
			
			pstmt = conn.prepareStatement(sql);
			
			QueryRunner queryRunner = new QueryRunner();
			
//			String[] props = new String[]{"hostKey","ip","hostName","ftpPort","ftpName","ftpPwd","defaultPath","isValid","remark"};
			
//			queryRunner.fillStatementWithBean(pstmt, host,props);
			
//			updateCount = pstmt.executeUpdate();
			
			updateCount = queryRunner.update(conn, sql, host.getHostKey(),host.getIp(),host.getHostName(),host.getFtpPort(),host.getFtpName(),host.getFtpPwd(),host.getDefaultPath(),Integer.parseInt(host.getIsValid().getValue()),host.getRemark());
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		} finally{
			
			try {
				if(pstmt != null){
					pstmt.close();
				}
				jdbcUtils.closeConnection(conn);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} 
		
		return updateCount;
	}

	@Override
	public Host queryHostByKey(String hostKey) throws Exception {
		
		String sql = "select host_key,ip,host_name,ftp_port,ftp_name,ftp_pwd,default_path,is_valid,remark from conf_host_info where host_key=?";
		
		Connection conn = null;
		
		Host host = null;
		try {
			
			conn = jdbcUtils.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
			
			BeanConverter convert = new BeanConverter(); 
			
			host = queryRunner.query(conn, sql, new BeanHandler<>(Host.class, new BasicRowProcessor(convert)), hostKey);
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		} finally{
			
			try {
				jdbcUtils.closeConnection(conn);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} 
		
		
		return host;
	}

}
