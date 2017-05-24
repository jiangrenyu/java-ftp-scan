package com.bonc.ftputil.dao;  

import java.util.List;

import com.bonc.ftputil.vo.FtpPath;

/**
 * ftp相对路径DAO
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-2
 * @time 下午9:07:44 
 * 
 */
public interface FtpPathDao {

	/**
	 * 
	 * 
	 * 查询所有 groupIds 的ftp路径
	 * @param groupIds
	 * @return
	 * 
	 *
	 */
	public List<FtpPath> queryFtpPath(String[] groupIds) ;
	
	
	public List<FtpPath> queryFtpPath(String groupId);
	
	/**
	 * 
	 * 
	 * 通过Path key查询FtpPath
	 * @param pKey
	 * @return
	 * 
	 *
	 */
	public FtpPath queryFtpPathByKey(String pKey);
	
	
	
	/**
	 * 
	 * 
	 * 查询downloadGroupId对应的路径
	 * @param downloadGroupId
	 * @return
	 * 
	 *
	 */
	public FtpPath queryDownloadGroupId(String downloadGroupId) ;
	
	
}
