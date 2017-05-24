package com.bonc.ftputil.vo;  

import java.io.Serializable;
import java.sql.Timestamp;

import com.bonc.ftputil.eum.FtpFileStatus;
import com.bonc.ftputil.eum.Operator;
import com.bonc.ftputil.eum.Valid;

/**
 * 文件状态信息
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-4
 * @time 下午3:22:22 
 * 
 */
public class LogFileStatus implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 表id
	 */
	private String f_id;
	
	/**
	 * 表主键
	 */
	private String f_key;
	
	/**
	 * 文件状态
	 */
	private FtpFileStatus status;
	
	/**
	 * 操作时间
	 */
	private Timestamp oper_time;
	
	/**
	 * 是否有效
	 */
	private Valid is_valid;
	
	/**
	 * 备注
	 */
	private String remark;
	
	/**
	 * 操作者
	 */
	private Operator operator;
	
	
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
	public FtpFileStatus getStatus() {
		return status;
	}
	public void setStatus(FtpFileStatus status) {
		this.status = status;
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
	public Operator getOperator() {
		return operator;
	}
	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	
	@Override
	public String toString(){
		
		return "[f_id="+this.f_id+",f_key="+this.f_key+",status="+this.status+",oper_time="+oper_time.getTime()+",is_valid="+this.is_valid+",remark="+this.remark+",operator="+this.operator+"]";
		
	}
	
}
