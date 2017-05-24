package com.bonc.ftputil.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class JdbcUtils {
	
	
//	private Logger logger = LoggerFactory.getLogger(JdbcUtils.class);
	
	private static DataSource ds;
	
	private static ThreadLocal<Connection> tl = new ThreadLocal<Connection>();  //map
	
	private String propertyPath ;
	
	public JdbcUtils(String propertyPath) throws Exception {
		
		this.propertyPath = propertyPath;
		
		Properties prop = new Properties();
		
		InputStream in = new FileInputStream(new File(propertyPath));
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		
		prop.load(bufferedReader);
		
		ds = BasicDataSourceFactory.createDataSource(prop);
			
		
	}
	
	public Connection getConnection() throws SQLException{
		try{
			//得到当前线程上绑定的连接
//			Connection conn = tl.get();
//			if(conn==null||conn.isClosed()){  //代表线程上没有绑定连接
//				conn = ds.getConnection();
//				tl.set(conn);
//			}
//			return conn;
			
			if(ds != null){
				return ds.getConnection();
			}
			throw new SQLException("获取数据库链接失败，数据源为空");
		}catch (Exception e) {
			throw new SQLException(e);
		}
	}
	
	public  void closeConnection(Connection conn) throws SQLException{
		try{
			if(conn!=null){
				conn.close();
			}
		}catch (Exception e) {
			throw new SQLException(e);
		}
	}
}