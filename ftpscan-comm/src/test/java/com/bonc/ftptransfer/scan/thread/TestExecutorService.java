package com.bonc.ftptransfer.scan.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TestExecutorService {
	
	
	@Test
	public void testShutdownExecutorService(){
		
		ExecutorService pool = Executors.newFixedThreadPool(5);
		
		final long waitTime = 8 * 1000;
		
		final long awaitTime = 5 * 1000;
		
		Runnable task1 = new Runnable(){

			@Override
			public void run() {
				try {
					System.out.println("task1 start.......");
					Thread.sleep(waitTime);
					System.out.println("task1 end.......");
				} catch (InterruptedException e) {
					System.out.println("task interrupted："+e);
				}
			}
			
		};
		
		Runnable task2 = new Runnable(){

			@Override
			public void run() {
				try {
					System.out.println("task2 start.......");
					Thread.sleep(1000);
					System.out.println("task2 end.......");
				} catch (InterruptedException e) {
					System.out.println("task2 interrupted："+e);
				}
			}
			
		};
		
		//添加一个task1
		pool.execute(task1);
		
		//添加10个task2
		for(int i = 0 ; i < 10 ; i++){
			pool.execute(task2);
		}
		pool.shutdown();
//		try {
//			通知关闭
//			pool.shutdown();

//			if(!pool.awaitTermination(awaitTime, TimeUnit.MILLISECONDS)){
//				
//				pool.shutdownNow();
//				
//			}
//		} catch (InterruptedException e) {
			 // awaitTermination方法被中断的时候也中止线程池中全部的线程的执行。
//	        System.out.println("awaitTermination interrupted: " + e);
//	        pool.shutdownNow();		
//	    }
		
		System.out.println("end....");
		
		//再次添加一个task1，会提示拒绝，因为线程池已经关闭
//		pool.execute(task1);
		
		
	}
	
}
