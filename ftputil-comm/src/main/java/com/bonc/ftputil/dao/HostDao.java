package com.bonc.ftputil.dao;  

import com.bonc.ftputil.vo.Host;

/**
 * Host操作DAO
 *
 * @author  hw
 * @version 1.0
 * @see     
 * @date 2015-12-2
 * @time 下午8:56:41 
 * 
 */
public interface HostDao {
	
	/**
	 * 
	 * 
	 * 保存主机信息
	 * @param host
	 * @return
	 * @throws Exception
	 * 
	 *
	 */
	public int saveHost(Host host) throws Exception ;
	
	
	/**
	 * 
	 * 
	 * 查询Host主机信息
	 * @param hostKey
	 * @return
	 * @throws Exception
	 * 
	 *
	 */
	public Host queryHostByKey(String hostKey) throws Exception ;
	
	
	
	
	
	
	

}
