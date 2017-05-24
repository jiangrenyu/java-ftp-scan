package com.bonc.ftputil.eum;

/**
 * 操作者
 * @author yinglin
 *
 */
public enum Operator {
	
	/**
	 * 扫描插入
	 */
	SCAN("0"),
	
	/**
	 * 下载新增
	 */
	DOWNLOAD_ADD("1"),
	
	/**
	 * 下载更新并新增
	 */
	DOWNLOAD_UPDATE("2");

	private String value;
	
	private Operator(String value){
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString(){
		
		return this.value;
	}
	
	
}
