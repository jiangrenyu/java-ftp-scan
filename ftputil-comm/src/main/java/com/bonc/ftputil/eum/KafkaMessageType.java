package com.bonc.ftputil.eum;  
/**
 * kafka消息类型
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-7
 * @time 下午3:17:42 
 * 
 */
public enum KafkaMessageType {

	/**
	 * 下载
	 */
	DownloadMessage("1"),
	
	/**
	 * 移动
	 */
	MoveMessage("2");
	
	
	
	
	/**
	 * 
	 */
	private String value ;
	
	
	private KafkaMessageType(String value){
		
		
		this.value = value ;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
}
