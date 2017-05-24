package com.bonc.ftputil.bean;  

import java.io.Serializable;

import com.bonc.ftputil.beanconvert.MsgTypeSerializer;
import com.bonc.ftputil.eum.KafkaMessageType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * kafka消息
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-7
 * @time 下午3:15:48 
 * 
 */
public class KafkaMessage implements Serializable,Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 消息类型
	 */
	private KafkaMessageType msg_type ;
	
	/**
	 * 文件主键
	 */
	private String f_id;
	
	/**
	 * 文件路径+文件名md5
	 */
	private String f_key;
	/**
	 * 路径key
	 */
	private String p_key;
	
	/**
	 * ftp主机key
	 */
	private String host_key;
	/**
	 * 文件名
	 */
	private String file_name;
	
	/**
	 * ftp主机地址
	 */
	private String remote_ip;
	
	/**
	 * ftp主机端口
	 */
	private int ftp_port;
	
	/**
	 * ftp主机用户名
	 */
	private String ftp_name;
	
	/**
	 * ftp主机用户密码
	 */
	private String ftp_pwd;
	
	/**
	 * 默认路径
	 */
	private String default_path;
	
	/**
	 * 文件所在路径
	 */
	private String remote_path;
	
	/**
	 * 本地存储路径
	 */
	private String local_path;
	
	/**
	 * 文件大小
	 */
	private long file_size;
	
	/**
	 * 文件ftp备份目录
	 */
	private String remote_bk_path;
	
	/**
	 * 时间
	 */
	private Long remote_time;
	
	/**
	 * 主题
	 */
	private String topic ;
	
	/**
	 * 下载错误次数
	 */
	private int downloadErrorCount ;
	
	
	/**
	 * 挪备份目录错误次数
	 */
	private int moveBakDirErrorCount;
	
	/**
	 * 是否需要move
	 */
	private boolean isMove;

	/**
	 * 下载groupId
	 */
	private String downGroupId;
	
	
	private String oper_time;
	
	public KafkaMessageType getMsg_type() {
		return msg_type;
	}

	public void setMsg_type(KafkaMessageType msg_type) {
		this.msg_type = msg_type;
	}

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

	public String getRemote_ip() {
		return remote_ip;
	}

	public void setRemote_ip(String remote_ip) {
		this.remote_ip = remote_ip;
	}

	public int getFtp_port() {
		return ftp_port;
	}

	public void setFtp_port(int ftp_port) {
		this.ftp_port = ftp_port;
	}

	public String getFtp_name() {
		return ftp_name;
	}

	public void setFtp_name(String ftp_name) {
		this.ftp_name = ftp_name;
	}

	public String getFtp_pwd() {
		return ftp_pwd;
	}

	public void setFtp_pwd(String ftp_pwd) {
		this.ftp_pwd = ftp_pwd;
	}

	public String getDefault_path() {
		return default_path;
	}

	public void setDefault_path(String default_path) {
		this.default_path = default_path;
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

	public String getRemote_bk_path() {
		return remote_bk_path;
	}

	public void setRemote_bk_path(String remote_bk_path) {
		this.remote_bk_path = remote_bk_path;
	}

	public Long getRemote_time() {
		return remote_time;
	}

	public void setRemote_time(Long remote_time) {
		this.remote_time = remote_time;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	
	
	public int getDownloadErrorCount() {
		return downloadErrorCount;
	}

	public void setDownloadErrorCount(int downloadErrorCount) {
		this.downloadErrorCount = downloadErrorCount;
	}

	public int getMoveBakDirErrorCount() {
		return moveBakDirErrorCount;
	}

	public void setMoveBakDirErrorCount(int moveBakDirErrorCount) {
		this.moveBakDirErrorCount = moveBakDirErrorCount;
	}
	
	
	
	public boolean isMove() {
		return isMove;
	}

	public void setMove(boolean isMove) {
		this.isMove = isMove;
	}
	
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	public String getDownGroupId() {
		return downGroupId;
	}

	public void setDownGroupId(String downGroupId) {
		this.downGroupId = downGroupId;
	}
	
	public String getOper_time() {
		return oper_time;
	}

	public void setOper_time(String oper_time) {
		this.oper_time = oper_time;
	}

	public static void main(String[] args) {
		KafkaMessage kafkaMessage = new KafkaMessage() ;
		
		kafkaMessage.setMsg_type(KafkaMessageType.MoveMessage);
		
		kafkaMessage.setF_id("abc");
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		
		gsonBuilder.registerTypeAdapter(KafkaMessageType.class, new MsgTypeSerializer());
		
		Gson gson = gsonBuilder.create();
		
		String str = gson.toJson(kafkaMessage);
		
		
		KafkaMessage newKafkaMsg = gson.fromJson(str, KafkaMessage.class);
		
		System.out.println(newKafkaMsg.getMsg_type());
		
		
	}
	
}
