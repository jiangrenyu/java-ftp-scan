package com.bonc.ftptransfer.scan.task;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftptransfer.scan.service.LogFileService;
import com.bonc.ftptransfer.scan.service.impl.LogFileServiceImpl;
import com.bonc.ftputil.bean.FTPServer;
import com.bonc.ftputil.bean.KafkaMessage;
import com.bonc.ftputil.bean.KafkaProducer;
import com.bonc.ftputil.bean.SFTPServer;
import com.bonc.ftputil.bean.SimpleFTPServer;
import com.bonc.ftputil.eum.FtpFileStatus;
import com.bonc.ftputil.eum.KafkaMessageType;
import com.bonc.ftputil.eum.Valid;
import com.bonc.ftputil.util.EncryptUtil;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.util.JsonUtil;
import com.bonc.ftputil.util.keyUtil;
import com.bonc.ftputil.vo.FtpPath;
import com.bonc.ftputil.vo.LogFile;
import com.bonc.ftputil.vo.LogFileStatus;
import com.jcraft.jsch.SftpException;

public class DirectoryScanTask implements Runnable {
	
	private static Logger LOG = LoggerFactory.getLogger(DirectoryScanTask.class);
	
	private FTPServer ftpServer ;
	
	private List<FtpPath> ftpPathList;
	
	private FtpPath ftpPath;
	
	private KafkaProducer producer;
	
	private LogFileService logFileService;
	
	private String localIp = null;
	
	private String ftpUserName;
	
	private String ftpUserPassword;
	
	private String ftpIp;
	
	private int ftpPort;
	
	private int loginTimeout;
	
	private int queryBatchSize = 100;
	
	public DirectoryScanTask(boolean isSFTP, int loginTimeout,int queryBatchSize, List<FtpPath> ftpPathList, JdbcUtils jdbcUtil,
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
							
							Map<String,LogFile> mathedLogFile = logFileService.queryLogFileAndStatus(fileKeyArray);
							
							for(int j = 0 ; j < subList.size() ; j++){
								
								String fileName = (String)subList.get(j).get("fileName");
								
								LogFile logFile = null;
								
								if(mathedLogFile != null){
									logFile = mathedLogFile.get(fileKeyArray[j]);
								}
								
								//根据fkey和lastModified 生成fid
								String fileId = generateFID(fileKeyArray[j],(Timestamp)subList.get(j).get("lastModified"),(Long)subList.get(j).get("fileSize"));
								
								LogFileStatus logFileStatus = new LogFileStatus();
								logFileStatus.setF_id(fileId);
								logFileStatus.setF_key(fileKeyArray[j]);
								logFileStatus.setIs_valid(Valid.VALID);
								logFileStatus.setOper_time((Timestamp)subList.get(j).get("scanTime"));
								logFileStatus.setRemark("");
								logFileStatus.setStatus(FtpFileStatus.UNDOWNLOAD);
								
								if(logFile == null){
									//文件不存在，先发送 topic消息，然后增加文件信息、状态信息
									
									LogFile newLogFile = new LogFile();
									newLogFile.setF_id(fileId);
									newLogFile.setF_key(fileKeyArray[j]);
									newLogFile.setP_key(ftpPath.getPkey());
									newLogFile.setHost_key(ftpPath.getHostKey().getHostKey());
									newLogFile.setFile_name(fileName);
									newLogFile.setRemote_path((String)subList.get(j).get("filePath"));//远端路径，有可能该文件位于扫描的某个目录下
									newLogFile.setLocal_path(ftpPath.getLocalPath());
									newLogFile.setFile_size((Long)subList.get(j).get("fileSize"));
									newLogFile.setRemote_time((Timestamp)subList.get(j).get("lastModified"));
									newLogFile.setLocal_host(localIp);//本机地址
									newLogFile.setOper_time((Timestamp)subList.get(j).get("scanTime"));
									newLogFile.setIs_valid(Valid.VALID);
									newLogFile.setRemark("");
									
									String msg = generateMessage(newLogFile,ftpPath);
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
									
								}else if((Long)subList.get(j).get("fileSize") != logFile.getFile_size()){
									//文件存在，大小不一样，先判断文件状态
									
									String oldFileId = logFile.getF_id();
									logFile.setF_id(fileId);
									logFile.setRemote_path((String)subList.get(j).get("filePath"));//远端路径，有可能该文件位于扫描的某个目录下
									logFile.setLocal_path(ftpPath.getLocalPath());
									logFile.setRemote_time((Timestamp)subList.get(j).get("lastModified"));
									logFile.setOper_time((Timestamp)subList.get(j).get("scanTime"));
									logFile.setFile_size((Long)subList.get(j).get("fileSize"));
									
									if(FtpFileStatus.UNDOWNLOAD.equals(logFile.getFileStatus().getStatus())){
										//未下载，则将原来的扫描信息置为无效，添加新的扫描信息
										logFile.setRemote_path((String)subList.get(j).get("filePath")+fileName);
										logFile.setLocal_path(ftpPath.getLocalPath()+fileName);
										
										logFileService.updateFileInfo(oldFileId,FtpFileStatus.UNDOWNLOAD,logFile,logFileStatus);
										
										LOG.info("更新文件 {} 大小为 {} ，状态为 {} 成功",fileName,logFile.getFile_size(),"未下载");
										
									}else if(FtpFileStatus.MOVEDIRSUC.equals(logFile.getFileStatus().getStatus()) || FtpFileStatus.DOWNLOADSUC.equals(logFile.getFileStatus().getStatus())){
										//下载完成，move完成或无需move，先发送topic 消息，然后将原来的扫描信息置为无效，添加新的扫描信息
										
										String msg = generateMessage(logFile,ftpPath);
										String topic = ftpPath.getTopic();
										try {
											
											producer.send(topic,msg);
											LOG.info("向 Topic {} 发送消息：{}",ftpPath.getTopic(),msg);
											
											logFile.setRemote_path((String)subList.get(j).get("filePath")+fileName);
											logFile.setLocal_path(ftpPath.getLocalPath()+fileName);
											
											if(logFileService.updateFileInfo(oldFileId,FtpFileStatus.MOVEDIRSUC,logFile,logFileStatus)){
												//更新成功
												LOG.info("更新文件 {} 大小为 {} ，状态为 {} 成功",fileName,logFile.getFile_size(),"下载完成，move完成");

											}else{
												//将原来的文件信息置为无效失败
												LOG.info("更新文件 {} 大小为 {} ，状态为 {} 失败",fileName,logFile.getFile_size(),"下载完成，move完成");
											}
										} catch (Exception e) {
											LOG.info("向 Topic "+ftpPath.getTopic()+" 发送消息失败，消息为:"+msg,e);
										}
										
									}
									
								}else{
									//文件名称、大小一致
									LOG.info((String)subList.get(j).get("filePath")+fileName + "文件名称大小一致，则不进行操作");
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
	 * v1.1每个用户一个线程,一次性查询所有的目录文件是否存在
	 */
	public void run_per_user() {
		
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
						String[] fileKeyArray = getFileKeys(ftpPath.getPkey(),ftpPath.getRemotePath(),scanedList);
						
//						Map<String,LogFile> mathedLogFile = logFileService.queryLogFileAndStatus(fileKeyArray);
						
						for(int i = 0 ; i < scanedList.size() ; i++){
							
							String fileName = (String)scanedList.get(i).get("fileName");
							
							LogFile logFile = logFileService.queryLogFileAndStatus(fileKeyArray[i]);
							
//							LogFile logFile = null;
//							
//							if(mathedLogFile != null){
//								logFile = mathedLogFile.get(fileKeyArray[i]);
//							}
							
//							String fileId = keyUtil.getUUID();
							//根据fkey和lastModified 生成fid
							String fileId = generateFID(fileKeyArray[i],(Timestamp)scanedList.get(i).get("lastModified"),(Long)scanedList.get(i).get("fileSize"));
							
							LogFileStatus logFileStatus = new LogFileStatus();
							logFileStatus.setF_id(fileId);
							logFileStatus.setF_key(fileKeyArray[i]);
							logFileStatus.setIs_valid(Valid.VALID);
							logFileStatus.setOper_time((Timestamp)scanedList.get(i).get("scanTime"));
							logFileStatus.setRemark("");
							logFileStatus.setStatus(FtpFileStatus.UNDOWNLOAD);
							
							if(logFile == null){
								//文件不存在，先发送 topic消息，然后增加文件信息、状态信息
								
								LogFile newLogFile = new LogFile();
								newLogFile.setF_id(fileId);
								newLogFile.setF_key(fileKeyArray[i]);
								newLogFile.setP_key(ftpPath.getPkey());
								newLogFile.setHost_key(ftpPath.getHostKey().getHostKey());
								newLogFile.setFile_name(fileName);
								newLogFile.setRemote_path((String)scanedList.get(i).get("filePath"));//远端路径，有可能该文件位于扫描的某个目录下
								newLogFile.setLocal_path(ftpPath.getLocalPath());
								newLogFile.setFile_size((Long)scanedList.get(i).get("fileSize"));
								newLogFile.setRemote_time((Timestamp)scanedList.get(i).get("lastModified"));
								newLogFile.setLocal_host(localIp);//本机地址
								newLogFile.setOper_time((Timestamp)scanedList.get(i).get("scanTime"));
								newLogFile.setIs_valid(Valid.VALID);
								newLogFile.setRemark("");
								
								String msg = generateMessage(newLogFile,ftpPath);
								String topic = ftpPath.getTopic();
								try {
									//发送消息
									producer.send(topic,msg);
									LOG.info("向 Topic {} 发送消息：{}",ftpPath.getTopic(),msg);
									
									newLogFile.setRemote_path((String)scanedList.get(i).get("filePath")+fileName);//远端路径，有可能该文件位于扫描的某个目录下
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
								
							}else if((Long)scanedList.get(i).get("fileSize") != logFile.getFile_size()){
								//文件存在，大小不一样，先判断文件状态
								
								String oldFileId = logFile.getF_id();
								logFile.setF_id(fileId);
								logFile.setRemote_path((String)scanedList.get(i).get("filePath"));//远端路径，有可能该文件位于扫描的某个目录下
								logFile.setLocal_path(ftpPath.getLocalPath());
								logFile.setRemote_time((Timestamp)scanedList.get(i).get("lastModified"));
								logFile.setOper_time((Timestamp)scanedList.get(i).get("scanTime"));
								logFile.setFile_size((Long)scanedList.get(i).get("fileSize"));
								
								if(FtpFileStatus.UNDOWNLOAD.equals(logFile.getFileStatus().getStatus())){
									//未下载，则将原来的扫描信息置为无效，添加新的扫描信息
									logFile.setRemote_path((String)scanedList.get(i).get("filePath")+fileName);
									logFile.setLocal_path(ftpPath.getLocalPath()+fileName);
									
									logFileService.updateFileInfo(oldFileId,FtpFileStatus.UNDOWNLOAD,logFile,logFileStatus);
									
									LOG.info("更新文件 {} 大小为 {} ，状态为 {} 成功",fileName,logFile.getFile_size(),"未下载");
									
								}else if(FtpFileStatus.MOVEDIRSUC.equals(logFile.getFileStatus().getStatus())){
									//下载完成，move完成，先发送topic 消息，然后将原来的扫描信息置为无效，添加新的扫描信息
									
									String msg = generateMessage(logFile,ftpPath);
									String topic = ftpPath.getTopic();
									try {
										
										producer.send(topic,msg);
										LOG.info("向 Topic {} 发送消息：{}",ftpPath.getTopic(),msg);
										
										logFile.setRemote_path((String)scanedList.get(i).get("filePath")+fileName);
										logFile.setLocal_path(ftpPath.getLocalPath()+fileName);
										
										if(logFileService.updateFileInfo(oldFileId,FtpFileStatus.MOVEDIRSUC,logFile,logFileStatus)){
											//更新成功
											LOG.info("更新文件 {} 大小为 {} ，状态为 {} 成功",fileName,logFile.getFile_size(),"下载完成，move完成");

										}else{
											//将原来的文件信息置为无效失败
											LOG.info("更新文件 {} 大小为 {} ，状态为 {} 失败",fileName,logFile.getFile_size(),"下载完成，move完成");
										}
									} catch (Exception e) {
										LOG.info("向 Topic "+ftpPath.getTopic()+" 发送消息失败，消息为:"+msg,e);
									}
									
								}
								
							}else{
								//文件名称、大小一致
								LOG.info((String)scanedList.get(i).get("filePath")+fileName + "文件名称大小一致，则不进行操作");
							}
						}
					}
					
				}
				
			}else{
				LOG.info("未扫描IP:{},用户:{} 下到任何文件",this.ftpIp,this.ftpUserName);
			}
				
			
		}else{
			//登录失败
			LOG.info("登录FTP失败,IP为 "+this.ftpIp+",用户名为："+this.ftpUserName);
		}
		
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
	 * v1.0，每个目录一个进程
	 */
	public void run_per_path() {
		
		boolean isLogin = ftpServer.connectServer(ftpPath.getHostKey().getIp(), ftpPath.getHostKey().getFtpPort(), ftpPath.getHostKey().getFtpName(), ftpPath.getHostKey().getFtpPwd(),this.loginTimeout);
		
		if(isLogin){
			//登录成功
			LOG.info("登录FTP成功,IP为 "+ftpPath.getHostKey().getIp()+",用户名为："+ftpPath.getHostKey().getFtpName());
			
			try {
				//列出路径下的文件
				List<HashMap<String, Object>> fileList = ftpServer.listFileAndDirectory(ftpPath.getRemotePath());
				
				//关闭FTP连接
				ftpServer.closeFTPClient();
				
				LOG.info("关闭FTP连接");
				
				if(fileList != null && fileList.size() > 0){
					
					String[] fileKeyArray = getFileKeys(ftpPath.getPkey(),ftpPath.getRemotePath(),fileList);
					
					Map<String,LogFile> mathedLogFile = logFileService.queryLogFileByFKey(fileKeyArray);
					
					LOG.info("扫描到路径 "+ftpPath.getRemotePath()+" 下 "+fileList.size()+" 个文件");
					
					for(int i = 0 ; i < fileList.size() ; i++){
						
						String fileName = (String)fileList.get(i).get("fileName");
						
						LogFile logFile = null;
						
						if(mathedLogFile != null){
							logFile = mathedLogFile.get(fileName);
						}
						
						String fileId = keyUtil.getUUID();
						
						LogFileStatus logFileStatus = new LogFileStatus();
						logFileStatus.setF_id(fileId);
						logFileStatus.setF_key(fileKeyArray[i]);
						logFileStatus.setIs_valid(Valid.VALID);
						logFileStatus.setOper_time((Timestamp)fileList.get(i).get("scanTime"));
						logFileStatus.setRemark("");
						logFileStatus.setStatus(FtpFileStatus.UNDOWNLOAD);
						
						if(logFile == null){
							//文件不存在，增加文件信息、状态信息,发送 topic消息
							
							LogFile newLogFile = new LogFile();
							newLogFile.setF_id(fileId);
							newLogFile.setF_key(fileKeyArray[i]);
							newLogFile.setP_key(ftpPath.getPkey());
							newLogFile.setHost_key(ftpPath.getHostKey().getHostKey());
							LOG.info("host_key:"+ftpPath.getHostKey().getHostKey());
							newLogFile.setFile_name(fileName);
							newLogFile.setRemote_path((String)fileList.get(i).get("filePath")+fileName);//远端路径，有可能该文件位于扫描的某个目录下
							newLogFile.setLocal_path(ftpPath.getLocalPath()+fileName);
							newLogFile.setFile_size((Long)fileList.get(i).get("fileSize"));
							newLogFile.setRemote_time((Timestamp)fileList.get(i).get("lastModified"));
							newLogFile.setLocal_host(localIp);//本机地址
							newLogFile.setOper_time((Timestamp)fileList.get(i).get("scanTime"));
							newLogFile.setIs_valid(Valid.VALID);
							newLogFile.setRemark("");
							
							if(logFileService.saveFileInfo(newLogFile,logFileStatus)){
								//成功保存，然后发送 topic消息
								String msg = generateMessage(newLogFile,ftpPath);
								String topic = ftpPath.getTopic();
								try {
									
									producer.send(topic,msg);
									LOG.info("向 Topic "+ftpPath.getTopic()+" 发送消息："+msg);
								} catch (Exception e) {
									LOG.info("向 Topic "+ftpPath.getTopic()+" 发送消息失败，消息为:"+msg,e);
								}
							}else{
								//保存失败,不发送 topic 消息
								LOG.info("保存扫描到的新文件 "+fileName +"信息失败,则不发送下载消息");
							}
							
						}else if((Long)fileList.get(i).get("fileSize") != logFile.getFile_size()){
							//文件存在，大小不一样，将原来的扫描文件状态为大小不一致，记录无效，并添加新的扫描信息、发送新的topic消息
							String oldFileId = logFile.getF_id();
							logFile.setF_id(fileId);
							logFile.setRemote_path((String)fileList.get(i).get("filePath")+fileName);
							logFile.setFile_size((Long)fileList.get(i).get("fileSize"));
							logFile.setRemote_time((Timestamp)fileList.get(i).get("lastModified"));
							logFile.setOper_time((Timestamp)fileList.get(i).get("scanTime"));
							
							if(logFileService.updateFileInfo(oldFileId,FtpFileStatus.FILESIZEERROR,logFile,logFileStatus)){
								//成功修改，然后发送 topic消息
								String msg = generateMessage(logFile,ftpPath);
								String topic = ftpPath.getTopic();
								try {
									
									producer.send(topic,msg);
									LOG.info("向 Topic "+ftpPath.getTopic()+" 发送消息："+msg);
								} catch (Exception e) {
									LOG.info("向 Topic "+ftpPath.getTopic()+" 发送消息失败，消息为:"+msg,e);
								}
								
							}else{
								//将原来的文件信息置为无效失败，则不发送消息
								LOG.info("将文件 "+fileName + "("+logFile.getF_id()+") 置为无效失败，则不向topic发送消息。");
							}
							
						}else{
							//文件名称、大小一致
							LOG.info((String)fileList.get(i).get("filePath")+fileName + "文件名称大小一致，则不进行操作");
						}
					}
				}else{
					LOG.info("扫描到路径 "+ftpPath.getRemotePath()+" 下 0 个文件");
				}
				
			} catch (IOException | SftpException e) {
				LOG.info("扫描路径："+ftpPath.getRemotePath()+" 异常，",e);
			}
			
		}else{
			//登录失败
			LOG.info("登录FTP失败,IP为 "+this.ftpIp+",用户名为："+this.ftpUserName);
		}
		
	}
	
	/**
	 * 根据LogFile,FtpPath对象生成kafka topic中的下载消息
	 * @param newLogFile
	 * @param ftpPath 
	 * @return
	 */
	private String generateMessage(LogFile newLogFile, FtpPath ftpPath) {
		
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
		msg.setMsg_type(KafkaMessageType.DownloadMessage);
		msg.setP_key(newLogFile.getP_key());
		msg.setRemote_bk_path(ftpPath.getRemoteBkPath());
		msg.setRemote_ip(ftpPath.getHostKey().getIp());
		msg.setRemote_path(newLogFile.getRemote_path());
		msg.setRemote_time(newLogFile.getRemote_time().getTime());
		msg.setTopic(ftpPath.getTopic());
		msg.setMove(ftpPath.isMove());
		msg.setDownGroupId(ftpPath.getDownGroupId());
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
