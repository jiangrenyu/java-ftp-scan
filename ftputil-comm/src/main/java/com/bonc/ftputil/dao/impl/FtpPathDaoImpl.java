package com.bonc.ftputil.dao.impl;  

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftputil.dao.FtpPathDao;
import com.bonc.ftputil.eum.FileGetType;
import com.bonc.ftputil.eum.Valid;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.vo.FtpPath;
import com.bonc.ftputil.vo.Host;


/**
 *ftpPathDao实现
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-2
 * @time 下午9:12:40 
 * 
 */
public class FtpPathDaoImpl implements FtpPathDao {
	
	private Logger logger = LoggerFactory.getLogger(FtpPathDaoImpl.class);
	
	private JdbcUtils jdbcUtils ;
	
	public FtpPathDaoImpl(JdbcUtils jdbcUtils){
		
		this.jdbcUtils = jdbcUtils ;
		
	}
	

	@Override
	public List<FtpPath> queryFtpPath(String groupId) {
		
		return this.queryFtpPath(new String[]{groupId});
	}

	@Override
	public FtpPath queryFtpPathByKey(String pKey) {
		
		String sql = "select p_key,host_key,remote_path,remote_bk_path,local_path,file_regular,mv_flag,scan_group_id,down_group_id,topic,get_type,is_valid,remark from conf_path_info where p_key=?";
		
		Connection conn = null;
		
		FtpPath path = null;
		try {
			
			conn = jdbcUtils.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
			
			Map<String,Object> row = queryRunner.query(conn, sql, new MapHandler(), pKey);
			
			Host host = new Host();
			
			host.setIp((String)row.get("ip"));
			
			host.setHostName((String)row.get("host_name"));
			
			host.setFtpPort((Integer)row.get("ftp_port"));
			
			host.setFtpName((String)row.get("ftp_name"));
			
			host.setFtpPwd((String)row.get("ftp_pwd"));
			
			host.setDefaultPath((String)row.get("default_path"));
			
			host.setIsValid(Valid.VALID.getValue().equals(row.get("is_valid")) ? Valid.VALID:Valid.INVALID);
			
			host.setRemark((String)row.get("remark"));
			
			path = new FtpPath();
			path.setPkey((String)row.get("p_key"));
			
			path.setHostKey(host);
			
			path.setRemotePath((String)row.get("remote_path"));
			
			path.setRemoteBkPath((String)row.get("remote_bk_path"));
			
			path.setLocalPath((String)row.get("local_path"));
			
			String fileRegular = (String)row.get("file_regular");
			path.setFileRegular((fileRegular == null || fileRegular.equals("")) ? "*" : fileRegular);
			
			path.setMove("0".equals(row.get("mv_flag")) ? true : false);
			
			path.setScanGroupId((String)row.get("scan_group_id"));
			
			path.setDownGroupId((String)row.get("down_group_id"));
			
			path.setTopic((String)row.get("topic"));
			
			path.setGetType(FileGetType.FirmRealTime.getValue().equals(row.get("get_type")) ? FileGetType.FirmRealTime:null);
			
			path.setIsValid(Valid.VALID.getValue().equals(row.get("is_valid")) ? Valid.VALID:Valid.INVALID);
			
			path.setRemark((String)row.get("remark"));
			
			path.setTopicClientId((String)row.get("topic_group"));
			
			path.setTopicNumThread(row.get("topic_num_thread") != null ? (Integer)row.get("topic_num_thread"):1);
			
			return path;
			
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
		
		return path;
	}


	@SuppressWarnings("all")
	@Override
	public List<FtpPath> queryFtpPath(String[] groupIds) {
		
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT p.p_key,p.host_key, p.remote_path,p.remote_bk_path,p.local_path,p.file_regular,p.mv_flag,p.scan_group_id,p.down_group_id,p.topic,p.get_type, p.is_valid, p.remark ,p.topic_group,  p.thread_num, h.ip, h.host_name, h.ftp_port, h.ftp_name,h.ftp_pwd,h.default_path ,h.remark FROM conf_path_info p , conf_host_info h  WHERE p.host_key = h.host_key AND  p.is_valid = "+Valid.VALID+" AND p.scan_group_id IN(");
		
		for(String groupId : groupIds){
			sb.append("?,");
		}
		
		sb.deleteCharAt(sb.length()-1);
		
		sb.append(")");
		
		Connection conn = null;
		
		List<FtpPath> ftpPathList = new ArrayList<FtpPath>();
		try {
			
			conn = jdbcUtils.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
			
			List resultList = queryRunner.query(conn, sb.toString(), new MapListHandler() , groupIds);
			
			for(Object obj : resultList){
				Map<String, Object> row = (Map<String, Object>)obj;
				
				Host host = new Host();
				
				host.setHostKey((String)row.get("host_key")== null ? null: ((String)row.get("host_key")).trim());
//				logger.info("===============db->host_key"+(String)row.get("host_key"));
				host.setIp(row.get("ip").toString());
				
				host.setHostName((String)row.get("host_name"));
				
				host.setFtpPort((Integer)row.get("ftp_port"));
				
				host.setFtpName((String)row.get("ftp_name")== null ? null :((String)row.get("ftp_name")).trim());
				
				host.setFtpPwd((String)row.get("ftp_pwd") == null ? null :((String)row.get("ftp_pwd")).trim());
				
				String defaultPath = (String)row.get("default_path");
				
				host.setDefaultPath((defaultPath == null || defaultPath.endsWith("/") || defaultPath.length() == 0) ? defaultPath : defaultPath + "/");
				
				host.setIsValid(Valid.VALID.getValue().equals(row.get("is_valid")) ? Valid.VALID:Valid.INVALID);
				
				host.setRemark((String)row.get("remark"));
				
				FtpPath path = new FtpPath();
				path.setPkey((String)row.get("p_key"));
				
				path.setHostKey(host);
				
				String remotePath = (String)row.get("remote_path");
				
				if(remotePath != null){
					remotePath = remotePath.trim();
					remotePath = remotePath.endsWith("/") ? remotePath : remotePath + "/";
				}
				
				path.setRemotePath(remotePath);
				
				String remoteBkPath = (String)row.get("remote_bk_path");
				
				if(remoteBkPath != null){
					remoteBkPath = remoteBkPath.trim();
					remoteBkPath = remoteBkPath.endsWith("/") ? remoteBkPath : remoteBkPath + "/";
				}
				
				path.setRemoteBkPath(remoteBkPath);
				
				String localPath = (String)row.get("local_path");
				
				if(localPath != null){
					localPath = localPath.trim();
					localPath = localPath.endsWith("/") ? localPath : localPath + "/";
				}
				
				path.setLocalPath(localPath);
				
				String fileRegular = (String)row.get("file_regular");
				path.setFileRegular((fileRegular == null || fileRegular.equals("")) ? ".*" : fileRegular);
				
				path.setMove(0 == (Integer)row.get("mv_flag") ? true : false);
				
				path.setScanGroupId((String)row.get("scan_group_id"));
				
				path.setDownGroupId((String)row.get("down_group_id"));
				
				path.setTopic((String)row.get("topic"));
				
				path.setGetType(FileGetType.FirmRealTime.getValue().equals(row.get("get_type")) ? FileGetType.FirmRealTime:null);
				
				path.setIsValid(Valid.VALID.getValue().equals(row.get("is_valid")) ? Valid.VALID:Valid.INVALID);
				
				path.setRemark((String)row.get("remark"));
				
				path.setTopicClientId((String)row.get("topic_group"));
				
				path.setTopicNumThread(row.get("thread_num") != null ? (Integer)row.get("thread_num"):1);
				
				ftpPathList.add(path);
				
			}
			return ftpPathList;
			
		} catch (SQLException e) {
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
		
		
		return null;
	}


	@Override
	public FtpPath queryDownloadGroupId(String downloadGroupId) {
		
		String sql = "SELECT p.p_key,p.host_key, p.remote_path,p.remote_bk_path,p.local_path,p.file_regular,p.mv_flag,p.scan_group_id,p.down_group_id,p.topic,p.get_type, p.is_valid, p.remark ,p.topic_group,  p.thread_num, h.ip, h.host_name, h.ftp_port, h.ftp_name,h.ftp_pwd,h.default_path ,h.remark FROM conf_path_info p , conf_host_info h  WHERE p.host_key = h.host_key AND  p.is_valid = '0' AND p.down_group_id=? limit 1";
		
		Connection conn = null;
		
		FtpPath path = null;
		try {
			
			conn = jdbcUtils.getConnection();
			
			QueryRunner queryRunner = new QueryRunner();
			
			Map<String,Object> row = queryRunner.query(conn, sql, new MapHandler(), downloadGroupId);
			
			Host host = new Host();
			
			host.setIp((String)row.get("ip"));
			
			host.setHostName((String)row.get("host_name"));
			
			host.setFtpPort((Integer)row.get("ftp_port"));
			
			host.setFtpName((String)row.get("ftp_name"));
			
			host.setFtpPwd((String)row.get("ftp_pwd"));
			
			host.setDefaultPath((String)row.get("default_path"));
			
			host.setIsValid(Valid.VALID.getValue().equals(row.get("is_valid")) ? Valid.VALID:Valid.INVALID);
			
			host.setRemark((String)row.get("remark"));
			
			path = new FtpPath();
			path.setPkey((String)row.get("p_key"));
			
			path.setHostKey(host);
			
			path.setRemotePath((String)row.get("remote_path"));
			
			path.setRemoteBkPath((String)row.get("remote_bk_path"));
			
			path.setLocalPath((String)row.get("local_path"));
			
			String fileRegular = (String)row.get("file_regular");
			path.setFileRegular((fileRegular == null || fileRegular.equals("")) ? "*" : fileRegular);
			
			path.setMove("0".equals(row.get("mv_flag")) ? true : false);
			
			path.setScanGroupId((String)row.get("scan_group_id"));
			
			path.setDownGroupId((String)row.get("down_group_id"));
			
			path.setTopic((String)row.get("topic"));
			
			path.setGetType(FileGetType.FirmRealTime.getValue().equals(row.get("get_type")) ? FileGetType.FirmRealTime:null);
			
			path.setIsValid(Valid.VALID.getValue().equals(row.get("is_valid")) ? Valid.VALID:Valid.INVALID);
			
			path.setRemark((String)row.get("remark"));
			
			path.setTopicClientId((String)row.get("topic_group"));
			
			path.setTopicNumThread(row.get("topic_num_thread") != null ? (Integer)row.get("topic_num_thread"):1);
			
			return path;
			
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
		
		return path;
	}

}
