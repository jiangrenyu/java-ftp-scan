package com.bonc.ftputil.eum;  
/**
 * 
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-2
 * @time 下午7:46:47 
 * 
 */
public enum Valid {
	/**
	 * 不可用
	 */
	INVALID( "1"), 
	
	/**
	 * 可用
	 */
	VALID("0");
	
	private String value ;
	
	private Valid(String value ) {
       this.value = value;
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
