package com.bonc.ftputil.test.util;  

import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import junit.framework.TestCase;

/**
 * 功能描述
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-13
 * @time 下午9:20:53 
 * 
 */
public class HdfsTest extends TestCase{
	
	
	public void testHdfs(){
		
		String hdfsUrl = "hdfs://192.168.8.51:8020/";
		
		try{
			
			Configuration configuration = new Configuration();
			
			FileSystem hdfs = FileSystem.get(URI.create(hdfsUrl),configuration);
			
//			Path f = new Path("/");
//	        FileStatus[] status = hdfs.listStatus(f);
//	        
//	        System.out.println(f.getName() + " has all files:");
//	        
//	        for (int i = 0; i< status.length; i++) {
//	        	
//	            System.out.println(status[i].getPath().toString());
//	            
//	        }
	        
	        FileInputStream fis = new FileInputStream("C:/config.properties");
	        
	        
	        OutputStream os=hdfs.create(new Path("/itf/config.properties"));
	        //copy
	        IOUtils.copy(fis, os);
	        System.out.println("拷贝完成...");
	        
			
		}catch(Exception e){
			
			e.printStackTrace();
		}
		
		
		
		
		
	}

}
