package com.bonc.ftputil.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bonc.ftputil.vo.FtpPath;

/**
 * 
 * @author xiabaike
 * @date 2016年10月17日
 */
public class OpTimeDirectoryAdapter {

	private HashMap<String, List<FtpPath>> ftpPath = null;

	public OpTimeDirectoryAdapter(HashMap<String, List<FtpPath>> ftpPath) {
		this.ftpPath = ftpPath;
	}
	
	public HashMap<String, List<FtpPath>> getOpTimeDirectory(String[] opTimes) {
		List<FtpPath> paths = null;
		FtpPath p = null;
		String separator = System.getProperty("file.separator");
		System.out.println(Arrays.toString(opTimes));
		HashMap<String, List<FtpPath>> ftppath = new HashMap<String, List<FtpPath>>();
		if(opTimes != null && opTimes.length != 0) {
			try{
				for(Map.Entry<String, List<FtpPath>> entry : ftpPath.entrySet()) {
					List<FtpPath> pathList = entry.getValue();
					paths = new ArrayList<FtpPath>();
					for(FtpPath path : pathList) {
						String remote = path.getRemotePath();
						remote = remote.endsWith(separator) ? remote : (remote + separator);
						for(String opTime : opTimes) {
							p = new FtpPath(path);
							p.setRemotePath(remote + opTime + separator);
							System.out.println(remote + opTime + separator);
							paths.add(p);
						}
					}
					ftppath.put(entry.getKey(), paths);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			return null;
		}
		
		return ftppath;
	}
	
	public static void main(String[] args) {
		HashMap<String, List<FtpPath>> ftpPath = new HashMap<String, List<FtpPath>>();
		List<FtpPath> pathList = new ArrayList<FtpPath>();
		FtpPath path = new FtpPath();
		path.setRemotePath("D:\\library\\Maven");
		pathList.add(path);
		path = new FtpPath();
		path.setRemotePath("D:\\library\\Maven2");
		pathList.add(path);
		ftpPath.put("1", pathList);

		pathList = new ArrayList<FtpPath>();
		path = new FtpPath();
		path.setRemotePath("D:\\library\\Maven3");
		pathList.add(path);
		path = new FtpPath();
		path.setRemotePath("D:\\library\\Maven4");
		pathList.add(path);
		ftpPath.put("2", pathList);
		
		OpTimeDirectoryAdapter adapter = new OpTimeDirectoryAdapter(ftpPath);
		String[] optimes = DateUtil.getDistanceHours("2016101809", "");
		System.out.println(adapter.getOpTimeDirectory(optimes).toString());
	}
}
