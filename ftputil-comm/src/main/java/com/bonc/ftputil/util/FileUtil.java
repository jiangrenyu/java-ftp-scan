package com.bonc.ftputil.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;


public class FileUtil {
	
	
	public static Properties getProperteis(String filePath) throws Exception{
		
		if(filePath !=  null){
			
			Properties pro = new Properties();
			
			InputStream inputStream = new FileInputStream(new File(filePath));
		
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));

			pro.load(reader);
			
			if(inputStream != null){
				inputStream.close();
			}
			
			if(reader != null){
				reader.close();
			}
			
			return pro;
			
		}else{
			throw new Exception("can't found file:"+filePath);
		}
		
		
		
	}
	
	
	
	
	
	public static String getStringFromInputStream(InputStream inputStream){
		

		if(inputStream != null){
			
			BufferedReader reader = null;
			StringBuilder sb = new StringBuilder();

			String line = null;
					
			try {
				reader = new BufferedReader(new InputStreamReader(inputStream));
				while((line = reader.readLine()) != null){
					sb.append(line);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				
				try {
					if(inputStream != null){
						inputStream.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return sb.toString();
		}
		
		
		return null;
	}
}
