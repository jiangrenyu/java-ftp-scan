package com.bonc.ftputil.bean;  

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.hadoop.fs.FileStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonc.ftputil.dao.LogFileDao;
import com.bonc.ftputil.dao.LogFileStatusDao;
import com.bonc.ftputil.dao.impl.LogFileDaoImpl;
import com.bonc.ftputil.dao.impl.LogFileStatusDaoImpl;
import com.bonc.ftputil.eum.FTPType;
import com.bonc.ftputil.eum.FtpFileStatus;
import com.bonc.ftputil.eum.KafkaMessageType;
import com.bonc.ftputil.eum.Operator;
import com.bonc.ftputil.eum.Valid;
import com.bonc.ftputil.util.CommonFtp;
import com.bonc.ftputil.util.DateUtil;
import com.bonc.ftputil.util.FtpUtils;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.util.JsonUtil;
import com.bonc.ftputil.util.SFtpUtil;
import com.bonc.ftputil.util.keyUtil;
import com.bonc.ftputil.vo.LogFile;
import com.bonc.ftputil.vo.LogFileStatus;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

/**
 * 消息消费者
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-5
 * @time 下午8:46:32 
 * 
 */
public class KafkaConsumerMsgTask  {
	
	private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerMsgTask.class);
	
	private KafkaStream<byte[], byte[]> messageStream;
	 
	private int threadNumber;
	
	private  LogFileStatusDao logFileStatusDao;
	
	private  LogFileDao logFileDao;
	
	private static  KafkaProducer kafkaProducer;
	
	private  FTPType ftpServerType ;
	
	private  String downloadGroupId ;
	
	private  FtpUtils ftpUtils ;
	
	private  SFtpUtil sftpUtils ;
	
	public KafkaConsumerMsgTask(KafkaStream<byte[], byte[]> mStream ,int threadNumber,Properties config,String downloadGroupId,JdbcUtils jdbcUtil){
		
		this.messageStream = mStream ;
		
		this.threadNumber = threadNumber ;
		
		if(logFileStatusDao==null){
			
			logFileStatusDao = new LogFileStatusDaoImpl(jdbcUtil);
			
		}
		
		if(logFileDao == null){
			
			logFileDao = new LogFileDaoImpl(jdbcUtil);
			
		}
		
		this.downloadGroupId = downloadGroupId ;
		
		if(kafkaProducer==null){
			
			kafkaProducer = new KafkaProducer(config);
			
		}
		
		if(ftpServerType==null){
			
				String ftpType =  config.getProperty("ftpType");
				
				if(StringUtils.isEmpty(ftpType)){
					
					throw new IllegalArgumentException("ftpType is null");
					
				}else{
					
					if(!(ftpType.equals(FTPType.FTP.getValue())||ftpType.equals(FTPType.SFTP.getValue()))){
						
						throw new IllegalArgumentException("ftpType only can be ftp or sftp");
					}else{
						
						if(ftpType.equals(FTPType.FTP.getValue())){
							
							ftpServerType = FTPType.FTP;
							
						}else{
							
							ftpServerType = FTPType.SFTP;
							
						}
						
					}
				}
		}
	}
	
	
	public void run() {
		
		 ConsumerIterator<byte[], byte[]> it = messageStream.iterator();
		 
	        while (it.hasNext()){
	        	
	        	final String message = new String(it.next().message());
	        	
	        	final KafkaMessage  kafkaMessage = JsonUtil.parseJsonKafkaMessage(message);
	        	
	        	if(downloadGroupId.equals(kafkaMessage.getDownGroupId())){
	        		
	        		try {
						
						logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.DOWNLOADING);
						
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						
						logger.error(e.getMessage(),e.getCause());
					}
	        		
	        		
	        		logger.info("Thread " + Thread.currentThread().getName() + ":" + message);

					if(ftpServerType==FTPType.FTP){
						
						if(ftpUtils==null){
							
							ftpUtils = new FtpUtils();
						}
						
						FTPDeal(kafkaMessage, ftpUtils, logFileStatusDao, logFileDao, kafkaProducer);
						
					}else{
						
						if(sftpUtils==null){
							
							sftpUtils = new SFtpUtil();
						}
						
						SFTPDeal(kafkaMessage, sftpUtils, logFileStatusDao, logFileDao, kafkaProducer);
						
					}
	        		 
	        		
	        	}
	        	
	        }
	        logger.info("Shutting down Thread: " + threadNumber);
	}
	
	
	/**
	 * 
	 * 
	 * SFTP下载处理
	 * @param kafkaMessage
	 * @param ftpUtils
	 * @param logFileStatusDao
	 * @param logFileDao
	 * @param kafkaProducer
	 * 
	 *
	 */
	public static void SFTPDeal(KafkaMessage kafkaMessage,SFtpUtil ftpUtils,LogFileStatusDao logFileStatusDao,LogFileDao logFileDao,KafkaProducer kafkaProducer){
		
		String ftpHost = kafkaMessage.getRemote_ip();//ftpHost地址
		
		int ftpPort = kafkaMessage.getFtp_port();//ftp端口地址
		
		String ftpUserName = kafkaMessage.getFtp_name();//ftp用户名
		
		String ftpUserPwd = kafkaMessage.getFtp_pwd();//ftp用户密码
		
		FtpClientKey ftpClientKey = new FtpClientKey();
		
		ftpClientKey.setFtp_ip(ftpHost);
		
		ftpClientKey.setFtp_userName(ftpUserName);
		
		SFTPConnection sftpConnection = null ;
		
		ConcurrentLinkedQueue<Object> queue =  KafKaConsumer.ftpClients.get(ftpClientKey);
		
		if(queue==null){
			
			queue = new ConcurrentLinkedQueue<Object>();
			
			KafKaConsumer.ftpClients.put(ftpClientKey, queue);
		}
		
		logger.info("current ftpclient queue size:"+queue.size());
		
		sftpConnection = (SFTPConnection) queue.poll() ;
		
		if(sftpConnection==null){
			
			sftpConnection = ftpUtils.getFTPClient(ftpHost, ftpUserName, ftpUserPwd, ftpPort);
			
			if(sftpConnection == null){
				
				try {
					
					logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.FILENOTFUND);
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					
					logger.error(e.getMessage(),e.getCause());
				}
				
				
				return ;
				
			}
			
			
			if(KafKaConsumer.isOIDD){
				
				String ftpRemotePath = kafkaMessage.getRemote_path();//ftp文件对应的路径(相对路径,ftp登陆后的根路径)
				
				boolean flag = SFTPConnectionChangeWorkSpace(ftpUtils, sftpConnection, ftpClientKey, ftpRemotePath);
				
				
				if(!flag){
					
					try {
						
						logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.FILENOTFUND);
						
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						
						logger.error(e.getMessage(),e.getCause());
					}
					
					
					ftpUtils.closeFTPClient(sftpConnection);
					
					return ;
				}
				
			}
			
		}else{
			
			try {
				
				String returnPwd = sftpConnection.getChannelSftp().pwd();
				
				if(StringUtils.isEmpty(returnPwd)){
					
					ftpUtils.closeFTPClient(sftpConnection);
					
					sftpConnection = ftpUtils.getFTPClient(ftpHost, ftpUserName, ftpUserPwd, ftpPort);
					
					if(sftpConnection == null){
						
						try {
							
							logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.FILENOTFUND);
							
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							
							logger.error(e.getMessage(),e.getCause());
						}
						
						return ;
						
					}
						
					if(KafKaConsumer.isOIDD){
						
						String ftpRemotePath = kafkaMessage.getRemote_path();//ftp文件对应的路径(相对路径,ftp登陆后的根路径)
						
						boolean flag = SFTPConnectionChangeWorkSpace(ftpUtils, sftpConnection, ftpClientKey, ftpRemotePath);
						
						
						if(!flag){
							
							try {
								
								logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.FILENOTFUND);
								
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								
								logger.error(e.getMessage(),e.getCause());
							}
							
							ftpUtils.closeFTPClient(sftpConnection);
							return ;
						}
						
					}
				}
				
			} catch (SftpException e) {
				
				ftpUtils.closeFTPClient(sftpConnection);
				
				sftpConnection = ftpUtils.getFTPClient(ftpHost, ftpUserName, ftpUserPwd, ftpPort);
				
				if(sftpConnection == null){
					
					try {
						
						logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.FILENOTFUND);
						
					} catch (SQLException e2) {
						// TODO Auto-generated catch block
						
						logger.error(e2.getMessage(),e2.getCause());
					}
					
					return ;
					
				}
				
				if(KafKaConsumer.isOIDD){
					
					String ftpRemotePath = kafkaMessage.getRemote_path();//ftp文件对应的路径(相对路径,ftp登陆后的根路径)
					
					boolean flag = SFTPConnectionChangeWorkSpace(ftpUtils, sftpConnection, ftpClientKey, ftpRemotePath);
					
					if(!flag){
						
						try {
							
							logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.FILENOTFUND);
							
						} catch (SQLException e2) {
							// TODO Auto-generated catch block
							
							logger.error(e2.getMessage(),e2.getCause());
						}
						
						ftpUtils.closeFTPClient(sftpConnection);
						
						return ;
					}
					
				}
					
				
			} 
			
		}
		
		if(kafkaMessage.getDownloadErrorCount()>=3){
			
			try {
				logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.DOWNLOADFAIL);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				
				logger.error(e.getMessage(),e.getCause());
			}
			
			queue.offer(sftpConnection);
			
			return ;
		}
		
		if(kafkaMessage.getMoveBakDirErrorCount()>=3){
			
			try {
				logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.MOVEDIRERROR);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage(),e.getCause());
			}
			queue.offer(sftpConnection);
			
			return ;
		}
		
		
		if(kafkaMessage.isMove()){
			//需要挪ftp上已下载的文件到备份目录
			
			if(kafkaMessage.getMsg_type()==KafkaMessageType.MoveMessage){
				
				logger.info("move message !!");
				
				moveSFtpFile2BakDirectory(sftpConnection, logFileStatusDao, kafkaMessage.getF_id(), kafkaMessage, kafkaProducer,ftpUtils);
				
				
			}else{
				
				logger.info("download message !!");
				
				downloadSFTPMessage2Hdfs(sftpConnection, kafkaMessage, ftpUtils,logFileStatusDao,logFileDao);	
				
			}
			
		}else{
			//无需挪ftp上已下载的文件到备份目录
			
			downloadSFTPMessage2Hdfs(sftpConnection, kafkaMessage, ftpUtils,logFileStatusDao,logFileDao);	
		}
		
		KafKaConsumer.ftpClients.get(ftpClientKey).offer(sftpConnection);
		
		
	}
	
	
	
	
	
	public static void FTPDeal(KafkaMessage kafkaMessage,FtpUtils ftpUtils,LogFileStatusDao logFileStatusDao,LogFileDao logFileDao,KafkaProducer kafkaProducer){
		
		String ftpHost = kafkaMessage.getRemote_ip();//ftpHost地址
		
		int ftpPort = kafkaMessage.getFtp_port();//ftp端口地址
		
		String ftpUserName = kafkaMessage.getFtp_name();//ftp用户名
		
		String ftpUserPwd = kafkaMessage.getFtp_pwd();//ftp用户密码
		
		FtpClientKey ftpClientKey = new FtpClientKey();
		
		ftpClientKey.setFtp_ip(ftpHost);
		
		ftpClientKey.setFtp_userName(ftpUserName);
		
		FTPConnection ftpConnection = null ;
		
		ConcurrentLinkedQueue<Object> queue =  KafKaConsumer.ftpClients.get(ftpClientKey);
		
		if(queue==null){
			
			queue = new ConcurrentLinkedQueue<Object>();
			
			KafKaConsumer.ftpClients.put(ftpClientKey, queue);
		}
		
		logger.info("current ftpclient queue size:"+queue.size());
		
		ftpConnection = (FTPConnection) queue.poll() ;
		
		
		if(ftpConnection==null){
			
			ftpConnection = ftpUtils.getFTPClient(ftpHost, ftpUserName, ftpUserPwd, ftpPort);
			
			if(ftpConnection == null){
				
				try {
					
					logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.FILENOTFUND);
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					
					logger.error(e.getMessage(),e.getCause());
				}
				
				return ;
				
			}
			
			if(KafKaConsumer.isOIDD){
				
				String ftpRemotePath = kafkaMessage.getRemote_path();//ftp文件对应的路径(相对路径,ftp登陆后的根路径)
				
				boolean flag = FTPConnectionChangeWorkSpace(ftpUtils, ftpConnection, ftpClientKey, ftpRemotePath);
				
				if(!flag){
					
					try {
						
						logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.FILENOTFUND);
						
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						
						logger.error(e.getMessage(),e.getCause());
					}
					
					ftpUtils.closeFTPClient(ftpConnection);
					
					
					return ;
				}
				
			}
			
			
			
		}else{
			
			try {
				int returnCode = ftpConnection.getFtpClient().pwd();
				
				if(returnCode==503){
					
					ftpUtils.closeFTPClient(ftpConnection);
					
					ftpConnection = ftpUtils.getFTPClient(ftpHost, ftpUserName, ftpUserPwd, ftpPort);
					
					if(ftpConnection == null){
						
						try {
							
							logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.FILENOTFUND);
							
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							
							logger.error(e.getMessage(),e.getCause());
						}
						
						return ;
						
					}
						
					if(KafKaConsumer.isOIDD){
						
						String ftpRemotePath = kafkaMessage.getRemote_path();//ftp文件对应的路径(相对路径,ftp登陆后的根路径)
						
						boolean flag = FTPConnectionChangeWorkSpace(ftpUtils, ftpConnection, ftpClientKey, ftpRemotePath);
						
						if(!flag){
							
							try {
								
								logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.FILENOTFUND);
								
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								logger.error(e.getMessage(),e.getCause());
							}
							
							ftpUtils.closeFTPClient(ftpConnection);
							
							return ;
						}
						
					}
					
				}
				
			} catch (IOException e) {
				
				ftpUtils.closeFTPClient(ftpConnection);
				
				ftpConnection = ftpUtils.getFTPClient(ftpHost, ftpUserName, ftpUserPwd, ftpPort);
				
				if(ftpConnection == null){
					
					try {
						
						logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.FILENOTFUND);
						
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						logger.error(e1.getMessage(),e1.getCause());
					}
					
					
					return ;
					
				}
				
				if(KafKaConsumer.isOIDD){
					
					String ftpRemotePath = kafkaMessage.getRemote_path();//ftp文件对应的路径(相对路径,ftp登陆后的根路径)
					
					boolean flag = FTPConnectionChangeWorkSpace(ftpUtils, ftpConnection, ftpClientKey, ftpRemotePath);
					
					if(!flag){
						
						try {
							
							logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.FILENOTFUND);
							
						} catch (SQLException e2) {
							// TODO Auto-generated catch block
							logger.error(e2.getMessage(),e2.getCause());
						}
						
						ftpUtils.closeFTPClient(ftpConnection);
						
						return ;
					}
					
				}
					
			}
			
		}
		
		if(kafkaMessage.getDownloadErrorCount()>=3){
			
			try {
				logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.DOWNLOADFAIL);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				
				logger.error(e.getMessage(),e.getCause());
			}
			
			queue.offer(ftpConnection);
			
			return ;
		}
		
		if(kafkaMessage.getMoveBakDirErrorCount()>=3){
			
			try {
				logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.MOVEDIRERROR);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage(),e.getCause());
			}
			
			queue.offer(ftpConnection);
			
			return ;
		}
		
		
		if(kafkaMessage.isMove()){
			//需要挪ftp上已下载的文件到备份目录
			
			if(kafkaMessage.getMsg_type()==KafkaMessageType.MoveMessage){
				
				logger.info("move message !!");
				
				moveFtpFile2BakDirectory(ftpConnection, logFileStatusDao, kafkaMessage.getF_id(), kafkaMessage, kafkaProducer,ftpUtils);
				
			}else{
				
				logger.info("download message !!");
				
				downloadFTPMessage2Hdfs(ftpConnection, kafkaMessage, ftpUtils,logFileStatusDao,logFileDao);	
				
			}
			
		}else{
			//无需挪ftp上已下载的文件到备份目录
			
			downloadFTPMessage2Hdfs(ftpConnection, kafkaMessage, ftpUtils,logFileStatusDao,logFileDao);	
		}
		
		
		KafKaConsumer.ftpClients.get(ftpClientKey).offer(ftpConnection);
		
	}
	
	/**
	 * 
	 * 
	 * 将文件从ftp下载到hdfs
	 * @param ftpClient
	 * @param kafkaMessage
	 * @param ftpUtils
	 * @param logFileStatusDao
	 * @param logFileDao
	 * 
	 *
	 */
	public static void downloadSFTPMessage2Hdfs(SFTPConnection sftpConnection,KafkaMessage kafkaMessage,final SFtpUtil ftpUtils,LogFileStatusDao logFileStatusDao,LogFileDao logFileDao){
		
		final String remotefileDirectory = kafkaMessage.getRemote_path();//ftp远端目录
		
		final String remoteFileName = kafkaMessage.getFile_name();//ftp文件名
		
		final String localDirectoryPath = kafkaMessage.getLocal_path();//本地下载路径
		
		FileStatus file = null;
		
		String beginTime = null ;
		
		String  endTime = null ;
		
		ChannelSftp ftpClient = sftpConnection.getChannelSftp();
		
		
		try {
			
			beginTime = DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") ;
			
			file = ftpUtils.downFile2HDFS(sftpConnection, remotefileDirectory, remoteFileName, localDirectoryPath);
			
			endTime = DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") ;
			
			logger.info("download sftp file end!");
			
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e.getCause());
			
			if(e.getMessage().equals("No such file")){
				
				try {
					
					int updateCount = logFileStatusDao.updateLogFileStatusByFkey(kafkaMessage.getF_key(), FtpFileStatus.FILENOTFUND);
					
					if(updateCount==0){
						
						logger.error("f_key:["+kafkaMessage.getF_key()+"] error,can not fetch a valid log_file_info record!!!!!!!!");
						
						LogFileStatus fileStatus = new LogFileStatus();
						
						fileStatus.setF_id(kafkaMessage.getF_id());
						
						fileStatus.setF_key(kafkaMessage.getF_key());
						
						fileStatus.setIs_valid(Valid.VALID);
						
						fileStatus.setOper_time(new Timestamp(new Date().getTime()));
						
						fileStatus.setStatus(FtpFileStatus.FILENOTFUND);
						
						try {
							
							logFileStatusDao.saveLogFileStatus(fileStatus);
							
						} catch (SQLException e2) {
							// TODO Auto-generated catch block
							logger.error(e2.getMessage(),e2.getCause());
						}
						
						
						return ;
					}
					
					
					
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					logger.error(e1.getMessage(),e1.getCause());
				}
				
			}else{
				
				try {
					logger.error("download ftp file["+ftpClient.getSession().getHost()+":"+ftpClient.getSession().getPort()+" "+remotefileDirectory+remoteFileName+"] error");
					
					kafkaMessage.setDownloadErrorCount(kafkaMessage.getDownloadErrorCount()+1);
					
					if(kafkaMessage.getDownloadErrorCount()>=3){
						
						int updateCount = logFileStatusDao.updateLogFileStatusByFkey(kafkaMessage.getF_key(), FtpFileStatus.DOWNLOADFAIL);
						
						if(updateCount==0){
							
							logger.error("f_key:["+kafkaMessage.getF_key()+"] error,can not fetch a valid log_file_status record!!!!!!!!");
							
							LogFileStatus fileStatus = new LogFileStatus();
							
							fileStatus.setF_id(kafkaMessage.getF_id());
							
							fileStatus.setF_key(kafkaMessage.getF_key());
							
							fileStatus.setIs_valid(Valid.VALID);
							
							fileStatus.setOper_time(new Timestamp(new Date().getTime()));
							
							fileStatus.setStatus(FtpFileStatus.FILENOTFUND);
							
							try {
								
								logFileStatusDao.saveLogFileStatus(fileStatus);
								
							} catch (SQLException e1) {
								// TODO Auto-generated catch block
								logger.error(e1.getMessage(),e1.getCause());
							}
							
						}
						
						
					}else{
						
						kafkaMessage.setMsg_type(KafkaMessageType.DownloadMessage);
						kafkaMessage.setOper_time(DateUtil.formatDate(new Date(), "yyyyMMddHHmmssSSS"));
						kafkaProducer.send(kafkaMessage.getTopic(), JsonUtil.objectToString(kafkaMessage));
						
					}
					
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					logger.error(e1.getMessage(),e1.getCause());
				} catch (JSchException e2) {
					// TODO Auto-generated catch block
					logger.error(e2.getMessage(),e2.getCause());
				}
				
				
			}
			
			return ;//下载失败
			
		} 
		
		
		if(file.getLen()==kafkaMessage.getFile_size()){//判断下载的文件大小与消息中的是否一致
			
			sendDownloadSFTPFileSucKafkaMessage(kafkaMessage, ftpClient, beginTime, endTime, file.getLen());
			
			logger.info("file download ok ,send success message to topic end !");
			
			
			if(kafkaMessage.isMove()){//判断ftp文件下载完后是否需要挪到ftp备份目录
				
				moveSFtpFile2BakDirectory(sftpConnection, logFileStatusDao, kafkaMessage.getF_id(),  kafkaMessage, kafkaProducer,ftpUtils);
				
				
				logger.info("file download ok ,move file 2 bak directory end !");
			}else{
				
				try {
					logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.DOWNLOADSUC);//下载成功
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage(),e.getCause());
				}
					
				logger.info("file download ok ,update status to FtpFileStatus.DOWNLOADSUC !");	
				
			}
			
			
			
		}else{
			
			LogFile logFile = null;
			
			try {
				
				logFile = logFileDao.queryLogFileByFKey(kafkaMessage.getF_key());
				
				logger.info("kafka Message not equal download file size ,query db logFile end !");
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				logger.error(e1.getMessage(),e1.getCause());
			}
			
			if(logFile==null){
				
				logger.error("f_key:["+kafkaMessage.getF_key()+"] error,can not fetch a valid log_file_info record!!!!!!!!");
				
				LogFileStatus fileStatus = new LogFileStatus();
				
				fileStatus.setF_id(kafkaMessage.getF_id());
				
				fileStatus.setF_key(kafkaMessage.getF_key());
				
				fileStatus.setIs_valid(Valid.VALID);
				
				fileStatus.setOper_time(new Timestamp(new Date().getTime()));
				
				fileStatus.setStatus(FtpFileStatus.FILESIZEERROR);
				
				fileStatus.setOperator(Operator.DOWNLOAD_ADD);
				
				try {
					
					logFileStatusDao.saveLogFileStatus(fileStatus);
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage(),e.getCause());
				}
				
				return ;
			}
			
			if(file.getLen()==logFile.getFile_size()){
				
				kafkaMessage.setF_id(logFile.getF_id());
				
				kafkaMessage.setFile_size(file.getLen());
				
				kafkaMessage.setRemote_time(logFile.getRemote_time().getTime());
				
				sendDownloadSFTPFileSucKafkaMessage(kafkaMessage, ftpClient, beginTime, endTime, file.getLen());
				
				logger.info("db file size equal download file size ,send success message to topic end !");
				
				if(kafkaMessage.isMove()){//判断ftp文件下载完后是否需要挪到ftp备份目录
					
					moveSFtpFile2BakDirectory(sftpConnection, logFileStatusDao, logFile.getF_id(),  kafkaMessage, kafkaProducer,ftpUtils);
					
					logger.info("download success ,move file to bak directory end !");
					
				}else{
					
					try {
						logFileStatusDao.updateLogFileStatus(logFile.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.DOWNLOADSUC);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						logger.error(e.getMessage(),e.getCause());
					}//下载成功
						
					logger.info("download success ,update file status to FtpFileStatus.DOWNLOADSUC end !");
				}
				
				
			}else{
				
				kafkaMessage.setDownloadErrorCount(kafkaMessage.getDownloadErrorCount()+1);
				
				if(kafkaMessage.getDownloadErrorCount()>=3){
					
					try{
						
						logFileStatusDao.updateLogFileStatus(logFile.getF_id(),logFile.getF_key(), FtpFileStatus.FILESIZEERROR);
						
						
					}
					catch (Exception e) {
						// TODO Auto-generated catch block
						logger.error(e.getMessage(),e.getCause());
					}
					
				}else{
					
					try {
						
						KafkaMessage newKafkaMessage = (KafkaMessage) kafkaMessage.clone();
						
						newKafkaMessage.setF_id(logFile.getF_id());
						
						newKafkaMessage.setFile_size(logFile.getFile_size());
						
						newKafkaMessage.setRemote_time(logFile.getRemote_time().getTime());
						
						kafkaProducer.send(kafkaMessage.getTopic(), JsonUtil.objectToString(newKafkaMessage));
						
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						
						logger.error(e.getMessage(),e.getCause());
					}
					
				}
				
			}
			
		}
		
		
	}
	
	
	/**
	 * 
	 * 
	 * 将FTP文件下载并推送到HDFS
	 * @param ftpClient
	 * @param kafkaMessage
	 * @param ftpUtils
	 * @param logFileStatusDao
	 * @param logFileDao
	 * 
	 *
	 */
	public static void downloadFTPMessage2Hdfs(FTPConnection ftpConnection,KafkaMessage kafkaMessage,final FtpUtils ftpUtils,LogFileStatusDao logFileStatusDao,LogFileDao logFileDao){
		
		final String remotefileDirectory = kafkaMessage.getRemote_path();//ftp远端目录
		
		final String remoteFileName = kafkaMessage.getFile_name();//ftp文件名
		
		final String localDirectoryPath = kafkaMessage.getLocal_path();//本地下载路径
		
		FileStatus file = null;
		
		String beginTime = null ;
		
		String  endTime = null ;
		
		
		try {
			
			logger.info("file download to hdfs begin!");
			
			beginTime = DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") ;
		
		    file = ftpUtils.downFile2HDFS(ftpConnection, remotefileDirectory, remoteFileName, localDirectoryPath);
		    
			endTime = DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") ;
			
			if(file==null){
				
				logger.info("ftp file["+remotefileDirectory+remoteFileName+"] not exgist,download return !");
				
				try {
					
					LogFile logFile = null;
					
					try {
						
						logFile = logFileDao.queryLogFileByFKey(kafkaMessage.getF_key());
						
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						logger.error(e1.getMessage(),e1.getCause());
					}
					
					if(logFile==null){
						
						logger.error("f_key:["+kafkaMessage.getF_key()+"] error,can not fetch a valid log_file_info record!!!!!!!!");
						
						LogFileStatus fileStatus = new LogFileStatus();
						
						fileStatus.setF_id(kafkaMessage.getF_id());
						
						fileStatus.setF_key(kafkaMessage.getF_key());
						
						fileStatus.setIs_valid(Valid.VALID);
						
						fileStatus.setOper_time(new Timestamp(new Date().getTime()));
						
						fileStatus.setStatus(FtpFileStatus.FILENOTFUND);
						
						fileStatus.setOperator(Operator.DOWNLOAD_ADD);
						
						try {
							
							logFileStatusDao.saveLogFileStatus(fileStatus);
							
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							
							logger.error(e.getMessage(),e.getCause());
						}
						
						return ;
					}
					
					
					logFileStatusDao.updateLogFileStatus(logFile.getF_id(),logFile.getF_key(), FtpFileStatus.FILENOTFUND);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage(),e.getCause());
				}
				
				logger.info("file download to hdfs end!");
				
				return ;
				
			}
			
			logger.info("file download to hdfs end!");

		} catch (Exception e) {
			
			logger.error(e.getMessage(),e.getCause());
			
			try {
				
				kafkaMessage.setDownloadErrorCount(kafkaMessage.getDownloadErrorCount()+1);
				
				if(kafkaMessage.getDownloadErrorCount()>=3){
					
					LogFile logFile = null;
					
					try {
						
						logFile = logFileDao.queryLogFileByFKey(kafkaMessage.getF_key());
						
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						logger.error(e1.getMessage(),e1.getCause());
					}
					
					if(logFile==null){
						
						logger.error("f_key:["+kafkaMessage.getF_key()+"] error,can not fetch a valid log_file_info record!!!!!!!!");
						
						LogFileStatus fileStatus = new LogFileStatus();
						
						fileStatus.setF_id(kafkaMessage.getF_id());
						
						fileStatus.setF_key(kafkaMessage.getF_key());
						
						fileStatus.setIs_valid(Valid.VALID);
						
						fileStatus.setOper_time(new Timestamp(new Date().getTime()));
						
						fileStatus.setStatus(FtpFileStatus.DOWNLOADFAIL);
						
						fileStatus.setOperator(Operator.DOWNLOAD_ADD);
						
						try {
							
							logFileStatusDao.saveLogFileStatus(fileStatus);
							
						} catch (SQLException sqlE) {
							// TODO Auto-generated catch block
							logger.error(sqlE.getMessage(),sqlE.getCause());
						}
						
						return ;
						
					}
					
					logFileStatusDao.updateLogFileStatus(logFile.getF_id(),logFile.getF_key(), FtpFileStatus.DOWNLOADFAIL);
					
				}else{
					
					kafkaMessage.setMsg_type(KafkaMessageType.DownloadMessage);
					
					kafkaMessage.setOper_time(DateUtil.formatDate(new Date(), "yyyyMMddHHmmssSSS"));
					
					kafkaProducer.send(kafkaMessage.getTopic(), JsonUtil.objectToString(kafkaMessage));
					
				}
				
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				logger.error(e1.getMessage(),e1.getCause());
			}
			
			return ;//下载失败
			
		}
		
			
		logger.info("download file size compare start!");
		
		if(file.getLen()==kafkaMessage.getFile_size()){//判断下载的文件大小与消息中的是否一致
			
			logger.info("file size compare ok!");
			
			sendDownloadFTPFileSucKafkaMessage(kafkaMessage, ftpConnection, beginTime, endTime, file.getLen());
			
			logger.info("send success topic message!");
			
			if(kafkaMessage.isMove()){//判断ftp文件下载完后是否需要挪到ftp备份目录
				
				
				moveFtpFile2BakDirectory(ftpConnection, logFileStatusDao, kafkaMessage.getF_id(),  kafkaMessage, kafkaProducer,ftpUtils);
				
				logger.info("move to bak directory end!");
			}else{
				
				try {
					
					logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.DOWNLOADSUC);//下载成功
					
					logger.info("update file status to download suc!");
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage(),e.getCause());
				}
				
			}
			
			
			
		}else{
			
			
			
			logger.info("download file size not equal kafkamessage query db begin!");
			
			LogFile logFile = null;
			
			try {
				
				logFile = logFileDao.queryLogFileByFKey(kafkaMessage.getF_key());
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				logger.error(e1.getMessage(),e1.getCause());
			}
			
			logger.info("download file size not equal kafkamessage query db end!");
			
			if(logFile==null){
				
				logger.error("f_key:["+kafkaMessage.getF_key()+"] error,can not fetch a valid log_file_info record!!!!!!!!");
				
				LogFileStatus fileStatus = new LogFileStatus();
				
				fileStatus.setF_id(kafkaMessage.getF_id());
				
				fileStatus.setF_key(kafkaMessage.getF_key());
				
				fileStatus.setIs_valid(Valid.VALID);
				
				fileStatus.setOper_time(new Timestamp(new Date().getTime()));
				
				fileStatus.setStatus(FtpFileStatus.FILESIZEERROR);
				
				fileStatus.setOperator(Operator.DOWNLOAD_ADD);
				
				try {
					
					logFileStatusDao.saveLogFileStatus(fileStatus);
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("f_key:["+kafkaMessage.getF_key()+"] error,insert record complete!",e.getCause());
				}
				logger.error("f_key:["+kafkaMessage.getF_key()+"] error,insert record complete!");
				
				return ;
			}
			
			if(file.getLen()==logFile.getFile_size()){
				
				
				kafkaMessage.setF_id(logFile.getF_id());
				
				kafkaMessage.setFile_size(file.getLen());
				
				kafkaMessage.setRemote_time(logFile.getRemote_time().getTime());
				
				logger.info("download file size equal to db file size, send success message to topic start!");
				
				sendDownloadFTPFileSucKafkaMessage(kafkaMessage, ftpConnection, beginTime, endTime, file.getLen());
				
				logger.info("download file size equal to db file size, send success message to topic end!");
				
				if(kafkaMessage.isMove()){//判断ftp文件下载完后是否需要挪到ftp备份目录
					
					moveFtpFile2BakDirectory(ftpConnection, logFileStatusDao, logFile.getF_id(),  kafkaMessage, kafkaProducer,ftpUtils);
					
					logger.info("move to bak directory end!");
					
				}else{
					
					try {
						
						logFileStatusDao.updateLogFileStatus(kafkaMessage.getF_id(),kafkaMessage.getF_key(), FtpFileStatus.DOWNLOADSUC);//下载成功
						
						
						logger.info("update file status to download success!");
						
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						logger.error(e.getMessage(),e.getCause());
					}
					
				}
				
				
			}else{
				
				kafkaMessage.setDownloadErrorCount(kafkaMessage.getDownloadErrorCount()+1);
				
				if(kafkaMessage.getDownloadErrorCount()>=3){
					
					try{
						
						logFileStatusDao.updateLogFileStatus(logFile.getF_id(),logFile.getF_key(), FtpFileStatus.FILESIZEERROR);
						
					}
					catch (Exception e) {
						// TODO Auto-generated catch block
						logger.error(e.getMessage(),e.getCause());
					}
					
				}else{
					
					try {
						
						KafkaMessage newKafkaMessage = (KafkaMessage) kafkaMessage.clone();
						
						newKafkaMessage.setF_id(logFile.getF_id());
						
						newKafkaMessage.setFile_size(logFile.getFile_size());
						
						newKafkaMessage.setRemote_time(logFile.getRemote_time().getTime());
						
						kafkaProducer.send(kafkaMessage.getTopic(), JsonUtil.objectToString(newKafkaMessage));
						
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						logger.error(e.getMessage(),e.getCause());
					}
					
				}
				
			}
			
		}
		
		
	}
	
	
	
	
	
	
	
	
	/**
	 * 
	 * 
	 * 将下载完成ftp文件move到备份目录
	 * @param ftpClient
	 * @param logFileStatusDao
	 * @param logFile
	 * @param kafkaMessage
	 * @param kafkaProducer
	 * 
	 *
	 */
	private static void moveFtpFile2BakDirectory(FTPConnection ftpConnection,LogFileStatusDao logFileStatusDao,String f_id,KafkaMessage kafkaMessage,KafkaProducer kafkaProducer,CommonFtp ftpUtils){
		
		try {
			
			FTPClient ftpClient = ftpConnection.getFtpClient();
			
			String remotefileDirectory = kafkaMessage.getRemote_path();
			
			String remoteFileName = kafkaMessage.getFile_name();
			
			String bakDirectory = kafkaMessage.getRemote_bk_path();
			
			boolean flag = ftpUtils.moveFtpFile2BakDirectory(ftpConnection, remotefileDirectory, bakDirectory, remoteFileName);
			
			if(flag){
				
				logFileStatusDao.updateLogFileStatus(f_id,kafkaMessage.getF_key(), FtpFileStatus.MOVEDIRSUC);//下载完成Move成功
				
				
			}else{
				
				ftpClient.enterLocalPassiveMode();//被动模式
				
				FTPFile[] ftps = null ;
				
				try {
					
					if(KafKaConsumer.isOIDD){
						
						ftps = ftpClient.listFiles(remoteFileName);
						
					}else{
						
						ftps = ftpClient.listFiles(remotefileDirectory+remoteFileName);
						
					}
					
					if(ftps.length==0){
						
						try {
							
							logFileStatusDao.updateLogFileStatus(f_id,kafkaMessage.getF_key(), FtpFileStatus.MOVEBAKDIRFILENOTFOUND);//挪备份目录文件不存在
							
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							logger.error(e1.getMessage(),e1.getCause());
						}
						
						
						return ;
						
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage(),e.getCause());
				}
				
				kafkaMessage.setMoveBakDirErrorCount(kafkaMessage.getMoveBakDirErrorCount()+1);
				
				if(kafkaMessage.getMoveBakDirErrorCount()>=3){
					
					//下载完成Move失败
					try {
						
						logFileStatusDao.updateLogFileStatus(f_id,kafkaMessage.getF_key(), FtpFileStatus.MOVEDIRERROR);//下载完成Move失败
						
						
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						logger.error(e1.getMessage(),e1.getCause());
					}
					
					
				}else{
					
					kafkaMessage.setMsg_type(KafkaMessageType.MoveMessage);
					
					kafkaMessage.setOper_time(DateUtil.formatDate(new Date(), "yyyyMMddHHmmssSSS"));
					kafkaProducer.send(kafkaMessage.getTopic(), JsonUtil.objectToString(kafkaMessage));
					
				}
				
			}
			
			
		}  catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e.getCause());
		}
		
		
	}
	/**
	 * 
	 * 
	 * 将下载完成ftp文件move到备份目录
	 * @param ftpClient
	 * @param logFileStatusDao
	 * @param logFile
	 * @param kafkaMessage
	 * @param kafkaProducer
	 * 
	 *
	 */
	private static void moveSFtpFile2BakDirectory(SFTPConnection sftpConnection,LogFileStatusDao logFileStatusDao,String f_id,KafkaMessage kafkaMessage,KafkaProducer kafkaProducer,CommonFtp ftpUtils){
		
		try {
			
			ChannelSftp ftpClient = sftpConnection.getChannelSftp();
			
			String remotefileDirectory = kafkaMessage.getRemote_path();
			
			String remoteFileName = kafkaMessage.getFile_name();
			
			String bakDirectory = kafkaMessage.getRemote_bk_path();
			
			boolean flag = ftpUtils.moveFtpFile2BakDirectory(sftpConnection, remotefileDirectory, bakDirectory, remoteFileName);
			
			if(flag){
				
				logFileStatusDao.updateLogFileStatus(f_id,kafkaMessage.getF_key(), FtpFileStatus.MOVEDIRSUC);//下载完成Move成功
				
			}else{
				
				kafkaMessage.setMoveBakDirErrorCount(kafkaMessage.getMoveBakDirErrorCount()+1);
				
				if(kafkaMessage.getMoveBakDirErrorCount()>=3){
					
					//下载完成Move失败
					try {
						
						logFileStatusDao.updateLogFileStatus(f_id,kafkaMessage.getF_key(), FtpFileStatus.MOVEDIRERROR);//下载完成Move失败
						
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						logger.error(e1.getMessage(),e1.getCause());
					}
					
					
				}else{
					
					kafkaMessage.setMsg_type(KafkaMessageType.MoveMessage);
					
					kafkaMessage.setOper_time(DateUtil.formatDate(new Date(), "yyyyMMddHHmmssSSS"));
					kafkaProducer.send(kafkaMessage.getTopic(), JsonUtil.objectToString(kafkaMessage));
					
				}
				
			}
			
			
		}  catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e.getCause());
		}
		
		
	}
	
	private static void sendDownloadFTPFileSucKafkaMessage(KafkaMessage kafkaMessage,FTPConnection ftpConnection,String beginTime,String endTime,long fileSize){
		
		Map<String,String> kafkaMsgMap = new HashMap<String,String>();
		
		kafkaMsgMap.put("file_name", kafkaMessage.getFile_name());
		kafkaMsgMap.put("operate_type", "1");
		kafkaMsgMap.put("client_address", ftpConnection.getFtpClient().getLocalAddress().getHostAddress());
		kafkaMsgMap.put("server_address", ftpConnection.getFtpHost());
		kafkaMsgMap.put("ftp_account", kafkaMessage.getFtp_name());
		kafkaMsgMap.put("file_path", kafkaMessage.getLocal_path()+kafkaMessage.getFile_name());
		kafkaMsgMap.put("version", "V2.2");
		kafkaMsgMap.put("file_size", fileSize+"");
		kafkaMsgMap.put("begin_time", beginTime);
		kafkaMsgMap.put("end_time", endTime);
		kafkaMsgMap.put("oper_time", DateUtil.formatDate(new Date(), "yyyyMMddHHmmssSSS"));
		
		String messsage = JsonUtil.objectToString(kafkaMsgMap);
		
		kafkaProducer.send(KafKaConsumer.downloadSucTopic, messsage);
		
		logger.info("suc message  send ok:"+messsage);
	}
	
	private static void sendDownloadSFTPFileSucKafkaMessage(KafkaMessage kafkaMessage,ChannelSftp ftpClient,String beginTime,String endTime,long fileSize){
		
		Map<String,String> kafkaMsgMap = new HashMap<String,String>();
		
		kafkaMsgMap.put("file_name", kafkaMessage.getFile_name());
		kafkaMsgMap.put("operate_type", "1");
		kafkaMsgMap.put("client_address", keyUtil.getLocalIp());
		try {
			kafkaMsgMap.put("server_address", ftpClient.getSession().getHost());
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(),e.getCause());
		}
		kafkaMsgMap.put("ftp_account", kafkaMessage.getFtp_name());
		kafkaMsgMap.put("file_path", kafkaMessage.getLocal_path()+kafkaMessage.getFile_name());
		kafkaMsgMap.put("version", "V2.2");
		kafkaMsgMap.put("file_size", fileSize+"");
		kafkaMsgMap.put("begin_time", beginTime);
		kafkaMsgMap.put("end_time", endTime);
		kafkaMsgMap.put("oper_time", DateUtil.formatDate(new Date(), "yyyyMMddHHmmssSSS"));
		
		String messsage = JsonUtil.objectToString(kafkaMsgMap);
		
		kafkaProducer.send(KafKaConsumer.downloadSucTopic, messsage);
		
		logger.info("suc message send ok:"+messsage);
	}
	
	
	
	/**
	 * 
	 * 
	 * FTP切换下载目录
	 * @param ftpUtils
	 * @param ftpClient
	 * @param ftpClientKey
	 * @param ftpRemotePath
	 * @return
	 * 
	 *
	 */
	private static boolean FTPConnectionChangeWorkSpace(FtpUtils ftpUtils,FTPConnection ftpConnection,FtpClientKey ftpClientKey,String ftpRemotePath){
		
		
		FTPClient ftpClient = ftpConnection.getFtpClient();
		
		String pwd = ftpUtils.getPwd(ftpConnection);
		
		try {
			
			boolean flag = ftpClient.changeWorkingDirectory(ftpRemotePath);
			
			if(!flag){
				
				ftpUtils.closeFTPClient(ftpConnection);
				
				KafKaConsumer.ftpClients.get(ftpClientKey).remove(ftpConnection);
				
				logger.error("change workspace ["+pwd+"] to ["+pwd+ftpRemotePath+"] error");
				
			}
			
			return flag ;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			ftpUtils.closeFTPClient(ftpConnection);
			
			KafKaConsumer.ftpClients.get(ftpClientKey).remove(ftpConnection);
			
			logger.error("change workspace ["+pwd+"] to ["+pwd+ftpRemotePath+"] error",e.getCause());
			
			return false;
		}
		
		
	}
	
	/**
	 * 
	 * 
	 * SFTP连接切换下载目录(适用于OIDD的下载)
	 * @param ftpUtils
	 * @param ftpClient
	 * @param ftpClientKey
	 * @param ftpRemotePath
	 * @return
	 * 
	 *
	 */
	private static boolean SFTPConnectionChangeWorkSpace(SFtpUtil ftpUtils,SFTPConnection sftpConnection,FtpClientKey ftpClientKey,String ftpRemotePath){
		
		
		ChannelSftp ftpClient = sftpConnection.getChannelSftp();
		
		String pwd = ftpUtils.getPwd(sftpConnection);
		
		try {
			
			ftpClient.cd(ftpRemotePath);
			
			logger.error("change workspace ["+pwd+"] to ["+pwd+ftpRemotePath+"] success");
			
			return true ;
			
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			
			ftpUtils.closeFTPClient(sftpConnection);
			
			KafKaConsumer.ftpClients.get(ftpClientKey).remove(ftpClient);
			
			logger.error("change workspace ["+pwd+"] to ["+pwd+ftpRemotePath+"] error",e.getCause());
			
			return false;
		}
		
		
	}
	
	
	

}
