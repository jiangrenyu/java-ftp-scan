package com.bonc.ftputil.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Describe:配置文件读取工具类
 * User: houwei
 * Date: 2015-3-3
 * Time: 下午2:31:05
 */
public class PropertityUtils {
	
	private static Properties p ;
	
	private static final Logger log = LoggerFactory.getLogger(PropertityUtils.class);
	
	private static String configFileName = "config.properties";
	
	static{
		
		
		p = new Properties() ;
		
		try {
			p.load(PropertityUtils.class.getClassLoader().getResourceAsStream(configFileName));
			
			log.info("加载配置文件config.properties成功");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.info("加载配置文件config.properties失败，请检查配置文件是否存在");
			log.error(e.getMessage());
			
		}
	}
	
	
	/**
	 * 
	 * @param key 根据相应的key查询conf.properties中对应的value值
	 * @return
	 */
	public synchronized static String getValue(String key){
		
		try {
			
			File file = new File(PropertityUtils.class.getClassLoader().getResource(configFileName).getFile());
			
			p.load(new InputStreamReader(new FileInputStream(file), "utf-8"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		}
		
		return p.getProperty(key).trim();
	}
	
	/**
	 * 
	 * @param key 根据相应的key查询conf.properties中对应的value值
	 * @return
	 */
	public synchronized static String getValue(String fileName,String key){
		
		try {
			
			File file = new File(PropertityUtils.class.getClassLoader().getResource(fileName).getFile());
			
			p.load(new InputStreamReader(new FileInputStream(file), "utf-8"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		}
		
		return p.getProperty(key).trim();
	}
	
	
	/**
	 * 设置相应的key和value
	 * @param key
	 * @param value
	 */
	public synchronized static void setValue(String key ,String value){
		
		
		try {
			p.load(PropertityUtils.class.getClassLoader().getResourceAsStream(configFileName));
			p.setProperty(key, value);
			File file = new File(PropertityUtils.class.getClassLoader().getResource(configFileName).getFile());
			log.info("读取配置文件："+file.getAbsolutePath());
			p.store(new FileWriter(file), "");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		}
	}
	
}
