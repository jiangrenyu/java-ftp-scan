package com.bonc.ftputil.eum;  
/**
 * FTP文件状态
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-4
 * @time 下午3:24:54 
 * 
 */
public enum FtpFileStatus {

	/**
	 * 未下载
	 */
	UNDOWNLOAD("1"),
	
	/**
	 * 下载中
	 */
	DOWNLOADING("2"),
	
	/**
	 * 文件下载完成，但大小不一致（需重新下载）
	 */
	FILESIZEERROR("3"),
	/**
	 * 下载失败,网络原因
	 */
	DOWNLOADFAIL("4"),
	/**
	 * 文件不存在
	 */
	FILENOTFUND("5"),
	/**
	 * 下载成功，无需move
	 */
	DOWNLOADSUC("6"),
	
	/**
	 * 文件下载完成、move失败
	 */
	MOVEDIRERROR("7"),
	
	
	/**
	 * 文件下载完成、move完成
	 */
	MOVEDIRSUC("8"),
	
	
	/**
	 * 挪备份目录文件不存在
	 */
	MOVEBAKDIRFILENOTFOUND("9"),
	
	/**
	 * 手动更新文件状态，未下载
	 */
	UPDATENOTDOWNLOADMANUAL("10");
	
	
	private String value ;
	
	private FtpFileStatus(String value ) {
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
