package com.bonc.ftputil.beanconvert;  

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Timestamp;

import org.apache.commons.dbutils.BeanProcessor;

import com.bonc.ftputil.eum.FileGetType;
import com.bonc.ftputil.eum.FtpFileStatus;
import com.bonc.ftputil.eum.Operator;
import com.bonc.ftputil.eum.Valid;
import com.bonc.ftputil.vo.LogFileStatus;


/**
 * bean转换器
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-2
 * @time 下午9:59:55 
 * 
 */
public class BeanConverter extends BeanProcessor {

    
	@Override
	protected Object processColumn(ResultSet rs, int index, Class<?> propType)
			throws SQLException {
		
			if ( !propType.isPrimitive() && rs.getObject(index) == null ) {
	            return null;
	        }
	
	        if (propType.equals(String.class)) {
	            return rs.getString(index);
	
	        } else if (
	            propType.equals(Integer.TYPE) || propType.equals(Integer.class)) {
	            return Integer.valueOf(rs.getInt(index));
	
	        } else if (
	            propType.equals(Boolean.TYPE) || propType.equals(Boolean.class)) {
	            return Boolean.valueOf(rs.getBoolean(index));
	
	        } else if (propType.equals(Long.TYPE) || propType.equals(Long.class)) {
	            return Long.valueOf(rs.getLong(index));
	
	        } else if (
	            propType.equals(Double.TYPE) || propType.equals(Double.class)) {
	            return Double.valueOf(rs.getDouble(index));
	
	        } else if (
	            propType.equals(Float.TYPE) || propType.equals(Float.class)) {
	            return Float.valueOf(rs.getFloat(index));
	
	        } else if (
	            propType.equals(Short.TYPE) || propType.equals(Short.class)) {
	            return Short.valueOf(rs.getShort(index));
	
	        } else if (propType.equals(Byte.TYPE) || propType.equals(Byte.class)) {
	            return Byte.valueOf(rs.getByte(index));
	
	        } else if (propType.equals(Timestamp.class)) {
	            return rs.getTimestamp(index);
	
	        } else if (propType.equals(SQLXML.class)) {
	            return rs.getSQLXML(index);
	
	        } else if(propType.equals(Valid.class)){
	            
	        	String valid =  rs.getObject(index)+"";
	        	
	        	return valid.equals(Valid.VALID.getValue())?Valid.VALID:Valid.INVALID;
	        	
	        } else if(propType.equals(Operator.class)){
	            
	        	String operator =  rs.getObject(index)+"";
	        	
	        	if(Operator.SCAN.getValue().equals(operator)){
	        		return Operator.SCAN;
	        	}else if(Operator.DOWNLOAD_ADD.getValue().equals(operator)){
	        		return Operator.DOWNLOAD_ADD;
	        	}else if(Operator.DOWNLOAD_UPDATE.getValue().equals(operator)){
	        		return Operator.DOWNLOAD_UPDATE;
	        	}
	        	
	        	return null;
	        	
	        }else if(propType.equals(FileGetType.class)){
	            
	        	String fileGetType =  rs.getObject(index)+"";
	        	
	        	if(fileGetType.equals(FileGetType.FirmRealTime.getValue())){
	        		
	        		return FileGetType.FirmRealTime;
	        	}
	        	return null;
	        }else if(propType.equals(FtpFileStatus.class)){
	            
	        	FtpFileStatus  fileStatus = null;
	        	
	        	String statusStr = rs.getInt("status")+"";
	        	
	        	if(FtpFileStatus.UNDOWNLOAD.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.UNDOWNLOAD;		
	        	}else if(FtpFileStatus.DOWNLOADING.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.DOWNLOADING;		
	        	}else if(FtpFileStatus.FILESIZEERROR.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.FILESIZEERROR;		

	        	}else if(FtpFileStatus.DOWNLOADFAIL.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.DOWNLOADFAIL;		

	        	}else if(FtpFileStatus.FILENOTFUND.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.FILENOTFUND;		

	        	}else if(FtpFileStatus.MOVEDIRERROR.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.MOVEDIRERROR;		

	        	}else if(FtpFileStatus.DOWNLOADSUC.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.DOWNLOADSUC;		

	        	}else if(FtpFileStatus.MOVEDIRSUC.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.MOVEDIRSUC;		

	        	}else if(FtpFileStatus.MOVEBAKDIRFILENOTFOUND.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.MOVEBAKDIRFILENOTFOUND;		

	        	}else if(FtpFileStatus.UPDATENOTDOWNLOADMANUAL.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.UPDATENOTDOWNLOADMANUAL;		
	        	}
	        	
	        	return fileStatus;
	        }else if(propType.equals(LogFileStatus.class)){
	            
	        	LogFileStatus status = new LogFileStatus();
	        	
	        	status.setF_id(rs.getString("f_id"));
	        	status.setF_key(rs.getString("f_key"));
	        	status.setIs_valid(Valid.VALID.getValue().equals(rs.getInt("is_valid")) ? Valid.VALID : Valid.INVALID);
	        	
	        	if(Operator.SCAN.getValue().equals(rs.getInt("operator")+"")){
	        		status.setOperator(Operator.SCAN);
	        	}else if(Operator.DOWNLOAD_ADD.getValue().equals(rs.getInt("operator")+"")){
	        		status.setOperator(Operator.DOWNLOAD_ADD);
	        	}else if(Operator.DOWNLOAD_UPDATE.getValue().equals(rs.getInt("operator")+"")){
	        		status.setOperator(Operator.DOWNLOAD_UPDATE);
	        	}else{
	        		status.setOperator(null);
	        	}
	        	
	        	status.setOper_time(rs.getTimestamp("status_oper_time"));
	        	status.setRemark(rs.getString("status_remark"));
	        	
	        	FtpFileStatus  fileStatus = null;
	        	
	        	String statusStr = rs.getInt("fileStatus")+"";
	        	
	        	if(FtpFileStatus.UNDOWNLOAD.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.UNDOWNLOAD;		
	        	}else if(FtpFileStatus.DOWNLOADING.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.DOWNLOADING;		
	        	}else if(FtpFileStatus.FILESIZEERROR.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.FILESIZEERROR;		

	        	}else if(FtpFileStatus.DOWNLOADFAIL.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.DOWNLOADFAIL;		

	        	}else if(FtpFileStatus.FILENOTFUND.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.FILENOTFUND;		

	        	}else if(FtpFileStatus.MOVEDIRERROR.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.MOVEDIRERROR;		

	        	}else if(FtpFileStatus.DOWNLOADSUC.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.MOVEDIRERROR;		

	        	}else if(FtpFileStatus.MOVEDIRSUC.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.MOVEDIRSUC;		

	        	}else if(FtpFileStatus.MOVEBAKDIRFILENOTFOUND.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.MOVEBAKDIRFILENOTFOUND;		

	        	}else if(FtpFileStatus.UPDATENOTDOWNLOADMANUAL.getValue().equals(statusStr)){
	        		fileStatus = FtpFileStatus.UPDATENOTDOWNLOADMANUAL;		
	        	}
	        	
	        	status.setStatus(fileStatus);
	        	
	        	return status;
	        } else{
	        	
	        	return rs.getObject(index);
	        }
	}

}
