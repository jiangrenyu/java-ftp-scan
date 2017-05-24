package com.bonc.ftptransfer.scan.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.bonc.ftptransfer.scan.service.FilePathService;
import com.bonc.ftputil.dao.FtpPathDao;
import com.bonc.ftputil.dao.impl.FtpPathDaoImpl;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.vo.FtpPath;

public class FilePathServiceImpl implements FilePathService {
	
	private FtpPathDao ftpPathDao;
	
	public FilePathServiceImpl(JdbcUtils jdbcUtil){

		this.ftpPathDao = new FtpPathDaoImpl(jdbcUtil);;
	}
	
	
	@Override
	public HashMap<String, List<FtpPath>> queryFtpPath(String[] groupIds) {
		
		List<FtpPath> ftpPathList = this.ftpPathDao.queryFtpPath(groupIds);
		
		HashMap<String, List<FtpPath>> ftpPathMap = new HashMap<String, List<FtpPath>>();
		
		if(ftpPathList != null){
			for(FtpPath ftpPath : ftpPathList){
				
				String userKey = ftpPath.getHostKey().getIp()+ftpPath.getHostKey().getFtpName();
				
				if(ftpPathMap.get(userKey) == null){
					List<FtpPath> list = new ArrayList<FtpPath>();
					list.add(ftpPath);
					
					ftpPathMap.put(userKey, list);
				}else{
					
					ftpPathMap.get(userKey).add(ftpPath);
				}
				
			}
		}
		
		return ftpPathMap;
		
	}

}
