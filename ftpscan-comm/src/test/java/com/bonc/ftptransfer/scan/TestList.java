package com.bonc.ftptransfer.scan;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestList {
	
	public void testListContain(){
		
		List<String> list = new ArrayList<String>();
		
		list.add("test1");
		list.add("test2");
		list.add("test3");
		
		String test = "test2";
		
		System.out.println("contain:"+list.contains(test));
		
		
	}
	
	@Test
	public void testSubList(){
		List<String> list = new ArrayList<String>();
		
		list.add("test1");
		list.add("test2");
		list.add("test3");
		list.add("test4");
		list.add("test5");
		list.add("test6");
		list.add("test7");
		list.add("test8");
		
		int batchSize = 3;
		
		int batch = (int)Math.ceil(list.size() / (double)batchSize);
		
		List<String> subList = null;
		
		for(int i = 0 ; i < batch ;i++){
			
			if((i+1)* batchSize < list.size()){
				
				subList = list.subList(i*batchSize, (i+1)* batchSize);
			}else{
				subList = list.subList(i*batchSize, list.size());
			}
			
			System.out.println(subList);
		}
		
	}
	
}
