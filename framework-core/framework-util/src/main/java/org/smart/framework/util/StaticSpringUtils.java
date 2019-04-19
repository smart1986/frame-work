package org.smart.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StaticSpringUtils{
	private static Logger logger = LoggerFactory.getLogger(StaticSpringUtils.class);
	private static AbstractApplicationContext context ;
	
	public static synchronized ApplicationContext getApplicationContext(){
		if (context == null){
			context = new ClassPathXmlApplicationContext("applicationContext.xml");
		}
		return context;
	}
	public static synchronized ApplicationContext getApplicationContext(String filename){
		if (context == null){
			context = new ClassPathXmlApplicationContext(filename);
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				
				@Override
				public void run() {
					context.close();
					logger.info("spring close complete...");
				}
			}));
		}
		return context;
	}

}
