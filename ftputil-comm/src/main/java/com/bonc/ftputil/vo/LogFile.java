package com.bonc.ftputil.vo;  

import java.io.Serializable;
import java.sql.Timestamp;

import com.bonc.ftputil.eum.FtpFileStatus;
import com.bonc.ftputil.eum.Valid;

/**
 * 文件扫描信息vo
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-4
 * @time 下午3:16:37 
 * 
 */
public class LogFile implements Serializable,Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 主键(UUID)
	 */
	private String f_id;
	
	/**
	 * 文件名称等的唯一码标示
	 */
	private String f_key;
	
	/**
	 *文件路径表主键
	 */
	private String p_key;
	
	/**
	 * 主机列表主键
	 */
	private String host_key;
	
	/**
	 * 文件名称
	 */
	private String file_name;
	
	/**
	 * 文件路径含文件名
	 */
	private String remote_path;
	
	/**
	 * 本地路径含文件名
	 */
	private String local_path;
	
	/**
	 * 文件大小
	 */
	private long file_size;
	
	/**
	 * 对端文件生成时间
	 */
	private Timestamp remote_time;
	
	/**
	 * 本地ip地址
	 */
	private String local_host;
	
	/**
	 * 操作时间
	 */
	private Timestamp oper_time;
	
	/**
	 * 是否可用
	 */
	private Valid is_valid;
	
	/**
	 * 备注
	 */
	private String remark;
	
	
	private LogFileStatus fileStatus;
	
	public String getF_id() {
		return f_id;
	}
	public void setF_id(String f_id) {
		this.f_id = f_id;
	}
	public String getF_key() {
		return f_key;
	}
	public void setF_key(String f_key) {
		this.f_key = f_key;
	}
	public String getP_key() {
		return p_key;
	}
	public void setP_key(String p_key) {
		this.p_key = p_key;
	}
	public String getHost_key() {
		return host_key;
	}
	public void setHost_key(String host_key) {
		this.host_key = host_key;
	}
	public String getFile_name() {
		return file_name;
	}
	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}
	public String getRemote_path() {
		return remote_path;
	}
	public void setRemote_path(String remote_path) {
		this.remote_path = remote_path;
	}
	public String getLocal_path() {
		return local_path;
	}
	public void setLocal_path(String local_path) {
		this.local_path = local_path;
	}
	public long getFile_size() {
		return file_size;
	}
	public void setFile_size(long file_size) {
		this.file_size = file_size;
	}
	public Timestamp getRemote_time() {
		return remote_time;
	}
	public void setRemote_time(Timestamp remote_time) {
		this.remote_time = remote_time;
	}
	public String getLocal_host() {
		return local_host;
	}
	public void setLocal_host(String local_host) {
		this.local_host = local_host;
	}
	public Timestamp getOper_time() {
		return oper_time;
	}
	public void setOper_time(Timestamp oper_time) {
		this.oper_time = oper_time;
	}
	public Valid getIs_valid() {
		return is_valid;
	}
	public void setIs_valid(Valid is_valid) {
		this.is_valid = is_valid;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public LogFileStatus getFileStatus() {
		return fileStatus;
	}
	public void setFileStatus(LogFileStatus fileStatus) {
		this.fileStatus = fileStatus;
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	
}
