package com.bonc.ftputil.test.util;

import java.util.List;

import org.junit.Test;

import com.bonc.ftputil.dao.FtpPathDao;
import com.bonc.ftputil.dao.impl.FtpPathDaoImpl;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.vo.FtpPath;

public class TestFtpPathDaoImpl {
	
	@Test
	public void testQueryPath(){
		
		JdbcUtils jdbcUtil = null;
		
		try {
			jdbcUtil = new JdbcUtils("src/main/resources/config.properties");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		FtpPathDao ftpPathDao = new FtpPathDaoImpl(jdbcUtil);
		
		List<FtpPath> list = ftpPathDao.queryFtpPath("g001");
		
		for(FtpPath path :list){
			
			System.out.println("f_id:"+path.getPkey());
			
			System.out.println("remote_path:"+path.getRemotePath());
			
		}
		
	}
	
}
