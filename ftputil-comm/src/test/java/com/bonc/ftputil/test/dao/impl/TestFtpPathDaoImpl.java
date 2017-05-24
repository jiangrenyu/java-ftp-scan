package com.bonc.ftputil.test.dao.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.bonc.ftputil.bean.KafKaConsumer;
import com.bonc.ftputil.dao.FtpPathDao;
import com.bonc.ftputil.dao.impl.FtpPathDaoImpl;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.vo.FtpPath;

public class TestFtpPathDaoImpl {
	
	private JdbcUtils jdbcUtil = null;
	
	private Properties prop  = null ;
	
	@Before
	public void init(){
		
		try {
			
			String propertyPath = "src/main/resources/config.properties" ;
			
			jdbcUtil = new JdbcUtils(propertyPath);
			
			prop = new Properties();
			
			InputStream in = new FileInputStream(new File(propertyPath));
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			
			prop.load(bufferedReader);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	
	
	public void testQueryPath(){
		
		
		
		FtpPathDao ftpPathDao = new FtpPathDaoImpl(jdbcUtil);
		
		List<FtpPath> list = ftpPathDao.queryFtpPath("g001");
		
		for(FtpPath path :list){
			
			System.out.println("f_id:"+path.getPkey());
			
			System.out.println("remote_path:"+path.getRemotePath());
			
		}
		
	}
	
}
