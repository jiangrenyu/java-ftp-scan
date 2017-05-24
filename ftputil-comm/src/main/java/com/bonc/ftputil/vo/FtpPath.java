package com.bonc.ftputil.vo;  

import java.io.Serializable;

import com.bonc.ftputil.eum.FileGetType;
import com.bonc.ftputil.eum.Valid;


/**
 * 文件抽取目录信息
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-2
 * @time 下午7:56:01 
 * 
 */
public class FtpPath implements Serializable {

	public FtpPath(){}

	public FtpPath(FtpPath ftpPath) {
		this.downGroupId = ftpPath.getDownGroupId();
		this.fileRegular = ftpPath.getFileRegular();
		this.hostKey = ftpPath.getHostKey();
		this.isMove = ftpPath.isMove();
		this.isValid = ftpPath.getIsValid();
		this.localPath = ftpPath.getLocalPath();
		this.pkey = ftpPath.getPkey();
		this.remark = ftpPath.getRemark();
		this.remoteBkPath = ftpPath.getRemoteBkPath();
		this.remotePath = ftpPath.getRemotePath();
		this.scanGroupId = ftpPath.getScanGroupId();
		this.topic = ftpPath.getTopic();
		this.topicClientId = ftpPath.getTopicClientId();
		this.topicNumThread = ftpPath.getTopicNumThread();
		this.getType = ftpPath.getGetType();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 目录主键
	 */
	private String pkey ;
	
	/**
	 * 主机主键
	 */
	private Host hostKey ;
	
	/**
	 * 对端绝对路径
	 */
	private String remotePath;
	
	/**
	 * 对端备份目录
	 */
	private String remoteBkPath ;
	
	/**
	 * 本地绝对路径
	 */
	private String localPath;
	
	/**
	 * 扫描groupId
	 */
	private String scanGroupId;
	
	/**
	 * 下载groupId
	 */
	private String downGroupId;
	
	/**
	 * 数据存放topic
	 */
	private String topic;
	
	/**
	 * 文件获取类型
	 */
	private FileGetType getType ;
	
	/**
	 * 是否可用
	 */
	private Valid isValid ;
	
	/**
	 * 备注
	 */
	private String remark ;
	
	/**
	 * topic的groupId
	 */
	private String topicClientId;
	
	/**
	 * 每个topic消费的线程数
	 */
	private int topicNumThread;
	
	/**
	 * 匹配文件名正则
	 */
	private String fileRegular;
	
	/**
	 * 下载完成后，是否需要move
	 */
	private boolean isMove;
	
	
	public String getPkey() {
		return pkey;
	}

	public void setPkey(String pkey) {
		this.pkey = pkey;
	}

	public Host getHostKey() {
		return hostKey;
	}

	public void setHostKey(Host hostKey) {
		this.hostKey = hostKey;
	}

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	public String getRemoteBkPath() {
		return remoteBkPath;
	}

	public void setRemoteBkPath(String remoteBkPath) {
		this.remoteBkPath = remoteBkPath;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}


	public String getScanGroupId() {
		return scanGroupId;
	}

	public void setScanGroupId(String scanGroupId) {
		this.scanGroupId = scanGroupId;
	}

	public String getDownGroupId() {
		return downGroupId;
	}

	public void setDownGroupId(String downGroupId) {
		this.downGroupId = downGroupId;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public FileGetType getGetType() {
		return getType;
	}

	public void setGetType(FileGetType getType) {
		this.getType = getType;
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

	public String getTopicClientId() {
		return topicClientId;
	}

	public void setTopicClientId(String topicClientId) {
		this.topicClientId = topicClientId;
	}

	public int getTopicNumThread() {
		return topicNumThread;
	}

	public void setTopicNumThread(int topicNumThread) {
		this.topicNumThread = topicNumThread;
	}

	public String getFileRegular() {
		return fileRegular;
	}

	public void setFileRegular(String fileRegular) {
		this.fileRegular = fileRegular;
	}

	public boolean isMove() {
		return isMove;
	}

	public void setMove(boolean isMove) {
		this.isMove = isMove;
	}

}
