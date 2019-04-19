package org.smart.framework.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂封装类
 * 
 * @author ludd
 * 
 */
public class NamedThreadFactory implements ThreadFactory {
	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	final String namePrefix;

	public NamedThreadFactory(ThreadGroup group, String name) {
		this.group = group;
		this.namePrefix = (group.getName() + ":" + name);
	}
	public NamedThreadFactory(String name) {
		this.group = new ThreadGroup(name + "Group");
		this.namePrefix = (group.getName() + ":" + name);
	}
	@Override
	public Thread newThread(Runnable r) {
		return new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
	}

	public Thread newThread(Runnable r, String title) {
		return new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement() + title, 0L);
	}
	
}