package com.bonc.ftputil.beanconvert;  

import java.lang.reflect.Type;

import com.bonc.ftputil.eum.KafkaMessageType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * enum转换器
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-8
 * @time 上午12:24:18 
 * 
 */
public class MsgTypeSerializer implements JsonSerializer<KafkaMessageType>,JsonDeserializer<KafkaMessageType> {

	@Override
	public JsonElement serialize(KafkaMessageType msgType, Type typeOfSrc,
			JsonSerializationContext context) {
		return new JsonPrimitive(msgType.ordinal());
	}

	@Override
	public KafkaMessageType deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		if (json.getAsInt() < KafkaMessageType.values().length)  
            return KafkaMessageType.values()[json.getAsInt()];  
        return null;  
	}
	
	
	

	

}
