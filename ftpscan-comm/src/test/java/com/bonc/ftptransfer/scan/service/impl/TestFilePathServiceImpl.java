package com.bonc.ftptransfer.scan.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

import com.bonc.ftptransfer.scan.service.FilePathService;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.vo.FtpPath;

public class TestFilePathServiceImpl {
	
	@Test
	public void testQueryFilePath() throws Exception{
		JdbcUtils jdbcUtil = new JdbcUtils("src/main/resources/dbcp.properties");
		
		FilePathService filePathService = new FilePathServiceImpl(jdbcUtil);
		
		HashMap<String, List<FtpPath>>  map = filePathService.queryFtpPath(new String[]{"g001"});
		
		Iterator<String> iterator = map.keySet().iterator();
		
		while(iterator.hasNext()){
			
			String key = iterator.next();
			
			System.out.println(key+":"+map.get(key).size());
			
		}
		
		
	}
	
	
	
	
	
	
}
