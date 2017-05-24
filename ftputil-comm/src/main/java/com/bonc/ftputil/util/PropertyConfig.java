package com.bonc.ftputil.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class PropertyConfig {
	
	private static final Logger LOG = LoggerFactory.getLogger(PropertyConfig.class);

	private Properties properties = new Properties();
	
	
	public PropertyConfig(String filePath) throws Exception{
		
		if(filePath !=  null){
			
			InputStream inputStream = new FileInputStream(new File(filePath));
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
			
			properties.load(reader);
			
			if(inputStream != null){
				inputStream.close();
			}
			
			if(reader != null){
				reader.close();
			}
			
			
		}else{
			throw new Exception("can't found file:"+filePath);
		}
		
	}
	
	/**
	 * 从config.properties配置文件中获取属性key的值
	 * @param key
	 * @return
	 */
	public  String getValue(String key){
		
		if(properties != null){
			try {
				String val = properties.getProperty(key);
				if(val != null && val.length() > 0){
					return val.trim();
				}
			} catch (Exception e) {
				LOG.info("获取属性 "+key+" 的值失败；",e);
				return null;
			}
		}
		return null;
	}
	
	public  String  getUnicodeValue(String value){  
		
		String unicodeStr = getValue(value);
		
		if(unicodeStr != null){
			StringBuilder buf = new StringBuilder();  
	        //因为java转义和正则转义，所以u要这么写  
	        String[] cc = unicodeStr.split("\\\\u");  
	        for (String c : cc) {  
	            if(c.equals(""))  
	                continue;  
	            int cInt = Integer.parseInt(c, 16);  
	            char cChar = (char)cInt;  
	            buf.append(cChar);  
	        }  
	        return buf.toString();  
		}
		
		return unicodeStr;
        
    }

	public Properties getProperties() {
		return properties;
	}
}
