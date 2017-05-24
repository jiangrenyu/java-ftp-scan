package com.bonc.ftputil.eum;  
/**
 * 文件获取类型
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-2
 * @time 下午7:59:59 
 * 
 */
public enum FileGetType {
	/**
	 * 准实时数据扫描
	 */
	FirmRealTime("0");
	
	
	/**
	 * 
	 */
	private String value ;
	
	
	private FileGetType(String value){
		
		
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
