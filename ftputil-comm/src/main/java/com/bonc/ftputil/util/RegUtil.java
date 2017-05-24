package com.bonc.ftputil.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class RegUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(RegUtil.class);

	
	/**
	 * 判断字符是否全部匹配正则
	 * @param str	带匹配字符
	 * @param reg	正则
	 * @return	返回匹配结果
	 */
	public static boolean isMatchAll(String str, String reg) {
		
		if(str != null && reg != null){
			try {
				Pattern pattern = Pattern.compile(reg);
				
				Matcher matcher = pattern.matcher(str);
				
				return matcher.matches();
			} catch (Exception e) {
				LOG.error("使用正则 "+reg+" 匹配字符 "+ str+"异常：",e);
			}
		}
		
		return false;
	}
	
	/**
	 * 判断字符是否匹配正则
	 * @param str	带匹配字符
	 * @param reg	正则
	 * @return	返回匹配结果
	 */
	public static boolean isMatch(String str, String reg) {
		
		if(str != null && reg != null){
			try {
				Pattern pattern = Pattern.compile(reg);
				
				Matcher matcher = pattern.matcher(str);
				
				return matcher.find();
			} catch (Exception e) {
				LOG.error("使用正则 "+reg+" 匹配字符 "+ str+"异常：",e);
			}
		}
		
		return false;
	}
	
	/**
	 * 返回字符串str中第一个匹配正则 reg 的字符串
	 * @param str
	 * @param reg
	 * @return
	 */
	public static String find(String str, String reg) {
		
		if(str != null && reg != null){
			try {
				Pattern pattern = Pattern.compile(reg);
				
				Matcher matcher = pattern.matcher(str);
				
				if(matcher.find()){
					return matcher.group(1);
				}
			} catch (Exception e) {
				LOG.error("使用正则 "+reg+" 匹配字符 "+ str+"异常：",e);
			}
		}
		
		return null;
	}
	
	/**
	 * 将字符串中的reg字符替换成replacedStr
	 * @param str
	 * @param reg
	 * @param replacedStr
	 * @return
	 */
	public static String replace(String str ,String reg,String replacedStr){
		
		if(str != null && reg != null && replacedStr != null){
			
			return str.replace(reg, replacedStr);
		}
			
		return str;
	}
	
}
