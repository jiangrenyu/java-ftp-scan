package com.bonc.ftptransfer.scan;

import java.util.Date;

import org.junit.Test;

import com.bonc.ftputil.util.DateUtil;

public class TestDate {
	
	
	@Test
	public void test(){
		
		
		System.out.println(DateUtil.formatDate(new Date(), "yyyyMMddHHmmssS"));
	}
}
