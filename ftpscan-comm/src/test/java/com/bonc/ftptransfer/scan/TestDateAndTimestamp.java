package com.bonc.ftptransfer.scan;


import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class TestDateAndTimestamp {
	
	@Test
	public void testLong(){
		
		System.out.println("Long:"+(new Date()).getTime());
		
		System.out.println("Long:"+Calendar.getInstance().getTimeInMillis());
		
		
	}
	
}
