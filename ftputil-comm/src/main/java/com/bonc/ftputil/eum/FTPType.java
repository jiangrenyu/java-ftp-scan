package com.bonc.ftputil.eum;  
/**
 * ftp 类型
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-15
 * @time 下午10:46:47 
 * 
 */
public enum FTPType {

	/**
	 * sftp
	 */
	SFTP("sftp"), 
	
	/**
	 * ftp
	 */
	FTP("ftp");
	
	private String value ;
	
	private FTPType(String value ) {
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
