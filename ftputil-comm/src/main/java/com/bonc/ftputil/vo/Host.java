package com.bonc.ftputil.vo;  

import java.io.Serializable;

import com.bonc.ftputil.eum.Valid;

/**
 * ftp主机配置信息
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-2
 * @time 下午7:41:15 
 * 
 */
public class Host implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 主机主键
	 */
	private String hostKey ; 
	
	/**
	 * ftp主机ip地址
	 */
	private String ip ;
	
	/**
	 * ftp主机文件名
	 */
	private String hostName;
	
	/**
	 * ftp端口
	 */
	private int ftpPort;
	
	/**
	 * ftp用户
	 */
	private String ftpName;
	
	/**
	 * ftp用户密码
	 */
	private String ftpPwd ;
	
	/**
	 * 用户默认路径
	 */
	private String defaultPath;
	
	/**
	 * 0 可用 1 不可用
	 */
	private Valid isValid ;
	
	/**
	 * 备注
	 */
	private String remark ;

	public String getHostKey() {
		return hostKey;
	}

	public void setHostKey(String hostKey) {
		this.hostKey = hostKey;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getFtpPort() {
		return ftpPort;
	}

	public void setFtpPort(int ftpPort) {
		this.ftpPort = ftpPort;
	}

	public String getFtpName() {
		return ftpName;
	}

	public void setFtpName(String ftpName) {
		this.ftpName = ftpName;
	}

	public String getFtpPwd() {
		return ftpPwd;
	}

	public void setFtpPwd(String ftpPwd) {
		this.ftpPwd = ftpPwd;
	}

	public String getDefaultPath() {
		return defaultPath;
	}

	public void setDefaultPath(String defaultPath) {
		this.defaultPath = defaultPath;
	}

	public Valid getIsValid() {
		return isValid;
	}

	public void setIsValid(Valid isValid) {
		this.isValid = isValid;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	
	
	
	

}
