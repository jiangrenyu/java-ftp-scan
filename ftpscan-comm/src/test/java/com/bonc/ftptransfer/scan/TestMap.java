package com.bonc.ftptransfer.scan;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

public class TestMap {
	
	@Test
	public void testMapNull(){
		
		HashMap<String,List> map = new HashMap<String,List>();
		
		List list = map.get("xx");
		
		System.out.println(list.size());
		
	}
	
}
