package com.bonc.ftptransfer.scan.service;

import java.util.HashMap;
import java.util.List;

import com.bonc.ftputil.vo.FtpPath;

public interface FilePathService {
	
	public HashMap<String, List<FtpPath>> queryFtpPath(String[] groupIds) ;
	
	
}
