package com.bonc.ftputil.util;

import com.bonc.ftputil.bean.KafkaMessage;
import com.bonc.ftputil.beanconvert.MsgTypeSerializer;
import com.bonc.ftputil.eum.KafkaMessageType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sf.json.JSONObject;

public class JsonUtil {
	
	/**
	 * 将java对象转换为json字符串
	 * @param obj
	 * @return
	 */
	public static String objectToString(Object obj){
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		
		gsonBuilder.registerTypeAdapter(KafkaMessageType.class, new MsgTypeSerializer());
		
		Gson gson = gsonBuilder.create();
		
		String str = gson.toJson(obj);
		
		return str ;
		
		
	}
	
	/**
	 * 将字符串转换为JSONObject
	 * @param str
	 * @return
	 */
	public static JSONObject parseStrToJson(String str){
		try {
			return JSONObject.fromObject(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	/**
	 * 
	 * 
	 * 将kafka中的消息转换为KafkaMessage对象
	 * @param str
	 * @return
	 * 
	 *
	 */
	public static KafkaMessage parseJsonKafkaMessage(String str){
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		
		gsonBuilder.registerTypeAdapter(KafkaMessageType.class, new MsgTypeSerializer());
		
		Gson gson = gsonBuilder.create();
		
		KafkaMessage newKafkaMsg = gson.fromJson(str, KafkaMessage.class);
		
		return newKafkaMsg ;
	}
	
}
