package com.bonc.ftptransfer.scan.task;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftptransfer.scan.service.LogFileService;
import com.bonc.ftptransfer.scan.service.LogFileStatusService;
import com.bonc.ftptransfer.scan.service.impl.LogFileServiceImpl;
import com.bonc.ftptransfer.scan.service.impl.LogFileStatusServiceImpl;
import com.bonc.ftputil.bean.FTPServer;
import com.bonc.ftputil.bean.KafkaMessage;
import com.bonc.ftputil.bean.KafkaProducer;
import com.bonc.ftputil.bean.SFTPServer;
import com.bonc.ftputil.bean.SimpleFTPServer;
import com.bonc.ftputil.eum.FtpFileStatus;
import com.bonc.ftputil.eum.KafkaMessageType;
import com.bonc.ftputil.eum.Operator;
import com.bonc.ftputil.eum.Valid;
import com.bonc.ftputil.util.DateUtil;
import com.bonc.ftputil.util.EncryptUtil;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.util.JsonUtil;
import com.bonc.ftputil.util.keyUtil;
import com.bonc.ftputil.vo.FtpPath;
import com.bonc.ftputil.vo.LogFile;
import com.bonc.ftputil.vo.LogFileStatus;
import com.jcraft.jsch.SftpException;

public class DirectoryScanTaskByStatusV4 implements Runnable {
	
	private static Logger LOG = LoggerFactory.getLogger(DirectoryScanTaskByStatusV4.class);
	
	private FTPServer ftpServer ;
	
	private List<FtpPath> ftpPathList;
	
	private FtpPath ftpPath;
	
	private KafkaProducer producer;
	
	private LogFileService logFileService;
	
	private LogFileStatusService logFileStatusService;
	
	private String localIp = null;
	
	private String ftpUserName;
	
	private String ftpUserPassword;
	
	private String ftpIp;
	
	private int ftpPort;
	
	private int loginTimeout;
	
	private int queryBatchSize = 100;
	
	public DirectoryScanTaskByStatusV4(boolean isSFTP, int loginTimeout,int queryBatchSize, List<FtpPath> ftpPathList, JdbcUtils jdbcUtil,
			KafkaProducer producer) {
		
		if(isSFTP){
			this.ftpServer = new SFTPServer();
		}else{
			this.ftpServer = new SimpleFTPServer();
		}
		
		this.loginTimeout = loginTimeout;
		
		this.queryBatchSize = queryBatchSize;
		
		this.ftpPathList = ftpPathList;
		
		this.ftpIp = ftpPathList.get(0).getHostKey().getIp();
		
		this.ftpPort = ftpPathList.get(0).getHostKey().getFtpPort();
		
		this.ftpUserName = ftpPathList.get(0).getHostKey().getFtpName();
		
		this.ftpUserPassword = ftpPathList.get(0).getHostKey().getFtpPwd();
		
		this.logFileService = new LogFileServiceImpl(jdbcUtil);
		
		this.logFileStatusService = new LogFileStatusServiceImpl(jdbcUtil);
		
		this.producer = producer;
		
		localIp = keyUtil.getLocalIp();
		
	}
	
	@Override
	public void run() {
		
		boolean isLogin = ftpServer.connectServer(this.ftpIp, this.ftpPort, this.ftpUserName, this.ftpUserPassword,this.loginTimeout);
		
		if(isLogin){
			//登录成功
			LOG.info("登录FTP成功,IP为 "+this.ftpIp+",用户名为："+this.ftpUserName);
			
			//列出路径下的文件
			HashMap<String,List<HashMap<String, Object>>> fileMap = new HashMap<String,List<HashMap<String, Object>>>();
			for(FtpPath ftpPath : this.ftpPathList){
				try {
				
					LOG.info("开始扫描目录:{}",ftpPath.getRemotePath());
					fileMap.put(ftpPath.getPkey(), ftpServer.listFileAndDirectory(ftpPath.getRemotePath(),ftpPath.getFileRegular()));
				
				} catch (IOException | SftpException e) {
					LOG.info("扫描路径："+ftpPath.getRemotePath()+" 异常，",e);
				}
			}
			
			//关闭FTP连接
			ftpServer.closeFTPClient();
			
			LOG.info("关闭FTP连接");
			
			if(fileMap.size() > 0){
				//扫描到的文件map
				for(FtpPath ftpPath : this.ftpPathList){
					
					List<HashMap<String, Object>> scanedList = fileMap.get(ftpPath.getPkey());
					
					LOG.info("扫描到目录{} 下 {} 个文件,正则为 {}",ftpPath.getRemotePath(),scanedList == null ? 0 : scanedList.size(),ftpPath.getFileRegular());
					
					if(scanedList != null && scanedList.size() > 0){
						
						int batch = (int)Math.ceil(scanedList.size() / (double)queryBatchSize);
						
						List<HashMap<String, Object>> subList = null;

						for(int i = 0 ; i< batch ;i++){
							
							if((i+1)*queryBatchSize <= scanedList.size()){
								
								subList = scanedList.subList(i*queryBatchSize, (i+1)*queryBatchSize);
								
							}else{
								
								subList = scanedList.subList(i*queryBatchSize,scanedList.size());
							}
							
							String[] fileKeyArray = getFileKeys(ftpPath.getPkey(),ftpPath.getRemotePath(),subList);
							
							Map<String,LogFileStatus> mathedLogFileStatus = logFileStatusService.queryLogFileStatus(fileKeyArray);
							
							for(int j = 0 ; j < subList.size() ; j++){
								
								LogFileStatus logFileStatus = null;
								
								if(mathedLogFileStatus != null){
									logFileStatus = mathedLogFileStatus.get(fileKeyArray[j]);
								}
								
								//根据fkey、lastModified以及fileSize生成fid
								String fileId = generateFID(fileKeyArray[j],(Timestamp)subList.get(j).get("lastModified"),(Long)subList.get(j).get("fileSize"));
								
//								LogFile logFile = null;
								String fileName = (String)subList.get(j).get("fileName");
								
								if(logFileStatus == null){
									//文件不存在，先发送 topic消息，然后增加文件信息、状态信息
									
									LogFile newLogFile = createLogFile(fileId, fileKeyArray[j], subList.get(j), ftpPath);

									logFileStatus = new LogFileStatus();
									logFileStatus.setF_id(fileId);
									logFileStatus.setF_key(fileKeyArray[j]);
									logFileStatus.setIs_valid(Valid.VALID);
									logFileStatus.setOper_time((Timestamp)subList.get(j).get("scanTime"));
									logFileStatus.setRemark("");
									logFileStatus.setStatus(FtpFileStatus.UNDOWNLOAD);
									logFileStatus.setOperator(Operator.SCAN);
									
									String msg = generateMessage(newLogFile,ftpPath,KafkaMessageType.DownloadMessage);
									String topic = ftpPath.getTopic();
									try {
										//发送消息
										producer.send(topic,msg);
										LOG.info("向 Topic {} 发送消息：{}",ftpPath.getTopic(),msg);
										
										newLogFile.setRemote_path((String)subList.get(j).get("filePath")+fileName);//远端路径，有可能该文件位于扫描的某个目录下
										newLogFile.setLocal_path(ftpPath.getLocalPath()+fileName);
										
										if(logFileService.saveFileInfo(newLogFile,logFileStatus)){
											//保存成功
											LOG.info("保存扫描到的新文件 {} 信息成功",fileName);
										}else{
											//保存失败
											LOG.info("保存扫描到的新文件 {} 信息失败",fileName);
										}
										
									} catch (Exception e) {
										LOG.info("向 Topic "+ftpPath.getTopic()+" 发送消息失败，消息为:"+msg,e);
									}
									
								}else{
									//f_key在log_file_status 中存在
									
									String oldFileId = logFileStatus.getF_id();
									
									if(Operator.DOWNLOAD_ADD.equals(logFileStatus.getOperator())){
										//该条记录是下载程序插入的，说明对应log_file_info中不存在对应记录，则插入一条
										
										//注意：之前的文件大小已经无法获取，大小，修改时间 置空，因为f_id还是使用原来的；
										LogFile newLogFile = createLogFile(oldFileId, logFileStatus.getF_key(), subList.get(j), ftpPath);
										
										newLogFile.setFile_size(0);
										newLogFile.setRemote_time(null);
										
										newLogFile.setRemote_path((String)subList.get(j).get("filePath")+fileName);//远端路径，有可能该文件位于扫描的某个目录下
										newLogFile.setLocal_path(ftpPath.getLocalPath()+fileName);
										
										if(logFileService.saveFileInfo(newLogFile)){
											LOG.info("向 log_ftp_file 表中补插入一条记录成功，文件名为：{}，F_ID为：{}",fileName,newLogFile.getF_id());
										}else{
											LOG.info("向 log_ftp_file 表中补插入一条记录失败，文件名为：{}，F_ID为：{}",fileName,newLogFile.getF_id());
										}
										
									}
									
									//判断FID是否相同
									FtpFileStatus oldFileStatus = logFileStatus.getStatus();

									if(!fileId.equals(oldFileId)){
										//f_id不同，判断状态	
										
										//发下载消息,将oldFileId 置为无效，插入新的fileId
										
										logFileStatus.setF_id(fileId);//设置新的F_id
										logFileStatus.setOper_time(new Timestamp(new Date().getTime()));
										logFileStatus.setOperator(Operator.SCAN);
										logFileStatus.setStatus(FtpFileStatus.UNDOWNLOAD);//未下载
										
										LogFile newLogFile = createLogFile(fileId, logFileStatus.getF_key(), subList.get(j), ftpPath);
										
										String msg = null;
										String topic = null;
										try {
											
											if(!FtpFileStatus.UNDOWNLOAD.equals(oldFileStatus)){
												//状态不是未下载则都需要重新发送消息
												msg = generateMessage(newLogFile,ftpPath,KafkaMessageType.DownloadMessage);
												topic = ftpPath.getTopic();
												//发送消息
												producer.send(topic,msg);
												LOG.info("向 Topic {} 发送消息：{}",ftpPath.getTopic(),msg);
											}
											
											newLogFile.setRemote_path((String)subList.get(j).get("filePath")+fileName);//远端路径，有可能该文件位于扫描的某个目录下
											newLogFile.setLocal_path(ftpPath.getLocalPath()+fileName);
											
											if(logFileService.updateFileInfo(oldFileId, oldFileStatus, newLogFile, logFileStatus)){
												//保存成功
												LOG.info("将原状态为 {} 的记录置为无效并插入新未下载记录成功，文件名为： {}，新F_ID为 {}",oldFileStatus,fileName,fileId);
											}else{
												//保存失败
												LOG.info("将原状态为 {} 的记录置为无效并插入新未下载记录失败，文件名为： {}，新F_ID为 {}",oldFileStatus,fileName,fileId);
											}
											
										} catch (Exception e) {
											LOG.info("向 Topic "+ftpPath.getTopic()+" 发送消息失败，消息为:"+msg,e);
										}
										
									}else{
										//fid相同，判断状态
										if(FtpFileStatus.FILENOTFUND.equals(logFileStatus.getStatus())){
											//状态为文件不存在，则发下载消息，只将log_ftp_status中置为无效并插入新的记录，因为log_ftp_file中将fid作为主键
											
											LogFile newLogFile = createLogFile(fileId, fileKeyArray[j], subList.get(j), ftpPath);

											logFileStatus.setOper_time((Timestamp)subList.get(j).get("scanTime"));
											logFileStatus.setStatus(FtpFileStatus.UNDOWNLOAD);
											logFileStatus.setOperator(Operator.SCAN);
											
											String msg = generateMessage(newLogFile,ftpPath,KafkaMessageType.DownloadMessage);
											String topic = ftpPath.getTopic();
											try {
												//发送消息
												producer.send(topic,msg);
												LOG.info("向 Topic {} 发送消息：{}",ftpPath.getTopic(),msg);
												
												if(logFileStatusService.updateLogFileStatus(logFileStatus)){
													//更新并保存成功
													LOG.info("将log_ftp_status 中原状态为 {} 的记录置为无效并插入新未下载记录成功，文件名为： {}，F_ID为 {}",oldFileStatus,fileName,fileId);
												}else{
													//更新并保存失败
													LOG.info("将log_ftp_status 中原状态为 {} 的记录置为无效并插入新未下载记录失败，文件名为： {}，F_ID为 {}",oldFileStatus,fileName,fileId);
												}
												
											} catch (Exception e) {
												LOG.info("向 Topic "+ftpPath.getTopic()+" 发送消息失败，消息为:"+msg,e);
											}
											
										}else{
											
											LOG.info("状态为 {} ,F_ID({})不变则不进行操作 ",oldFileStatus,fileId);
										}
									}
									
								}
							}
							
						}
						
					}
					
				}
				
			}else{
				LOG.info("未扫描到 IP:{},用户:{} 下存在任何文件",this.ftpIp,this.ftpUserName);
			}
				
			
		}else{
			//登录失败
			LOG.info("登录FTP失败,IP为 "+this.ftpIp+",用户名为："+this.ftpUserName);
		}
		
	}
	
	/**
	 * 构造新的LogFtpFile
	 * @param fId
	 * @param fKey
	 * @param scanedFile
	 * @param ftpPath
	 * @return
	 */
	public LogFile createLogFile(String fId,String fKey,HashMap<String, Object> scanedFile,FtpPath ftpPath){
		
		LogFile newLogFile = new LogFile();
		newLogFile.setF_id(fId);
		newLogFile.setF_key(fKey);
		newLogFile.setP_key(ftpPath.getPkey());
		newLogFile.setHost_key(ftpPath.getHostKey().getHostKey());
		newLogFile.setFile_name((String)scanedFile.get("fileName"));
		newLogFile.setRemote_path((String)scanedFile.get("filePath"));//远端路径，有可能该文件位于扫描的某个目录下
		newLogFile.setLocal_path(ftpPath.getLocalPath());
		newLogFile.setFile_size((Long)scanedFile.get("fileSize"));
		newLogFile.setRemote_time((Timestamp)scanedFile.get("lastModified"));
		newLogFile.setLocal_host(localIp);//本机地址
		newLogFile.setOper_time((Timestamp)scanedFile.get("scanTime"));
		newLogFile.setIs_valid(Valid.VALID);
		newLogFile.setRemark("");
		
		return newLogFile;
	}
	
	
	/**
	 * 根据fkey和lastModified 生成fkey
	 * @param fileSize 
	 * @param string
	 * @param timestamp
	 * @return
	 */
	private String generateFID(String fkey, Timestamp lastModified, Long fileSize) {
		
		return EncryptUtil.md5(fkey+lastModified.getTime()+fileSize);
	}
	
	
	
	/**
	 * 根据LogFile,FtpPath对象生成kafka topic中的下载消息
	 * @param newLogFile
	 * @param ftpPath 
	 * @param messageType 
	 * @return
	 */
	private String generateMessage(LogFile newLogFile, FtpPath ftpPath, KafkaMessageType messageType) {
		
		KafkaMessage msg = new KafkaMessage();
		
		msg.setDefault_path(ftpPath.getHostKey().getDefaultPath());
		msg.setF_id(newLogFile.getF_id());
		msg.setF_key(newLogFile.getF_key());
		msg.setFile_name(newLogFile.getFile_name());
		msg.setFile_size(newLogFile.getFile_size());
		msg.setFtp_name(ftpPath.getHostKey().getFtpName());
		msg.setFtp_port(ftpPath.getHostKey().getFtpPort());
		msg.setFtp_pwd(ftpPath.getHostKey().getFtpPwd());
		msg.setHost_key(newLogFile.getHost_key());
		msg.setLocal_path(ftpPath.getLocalPath());
		msg.setMsg_type(messageType);
		msg.setP_key(newLogFile.getP_key());
		msg.setRemote_bk_path(ftpPath.getRemoteBkPath());
		msg.setRemote_ip(ftpPath.getHostKey().getIp());
		msg.setRemote_path(newLogFile.getRemote_path());
		msg.setRemote_time(newLogFile.getRemote_time().getTime());
		msg.setTopic(ftpPath.getTopic());
		msg.setMove(ftpPath.isMove());
		msg.setDownGroupId(ftpPath.getDownGroupId());
		msg.setOper_time(DateUtil.formatDate(new Date(), "yyyyMMddHHmmssS"));//新增操作时间字段
		return JsonUtil.objectToString(msg);
	}

	/**
	 * 获取每个文件的路径p_key和文件名的MD5加密
	 * @param pkey
	 * @param remotePath 
	 * @param fileList
	 * @return
	 */
	private String[] getFileKeys(String pkey,
			String remotePath, List<HashMap<String, Object>> fileList) {
		
		String[] fileKeys = new String[fileList.size()];
		
		for(int i = 0 ; i < fileList.size(); i++){
			
			String actualPath = (String)fileList.get(i).get("filePath");
			
			String fileName = (String)fileList.get(i).get("fileName");
			
			if(remotePath.equals(actualPath) || (remotePath + "/").equals(actualPath)){
				
				fileKeys[i] = EncryptUtil.md5(pkey+fileName);
				
			}else{
				
				fileName =  actualPath.substring(actualPath.indexOf(remotePath)+remotePath.length()) + fileName;
				
				fileKeys[i] = EncryptUtil.md5(pkey+fileName);
			}
		}
		
		
		return fileKeys;
	}
	
	
	
}	
