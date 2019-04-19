package org.smart.framework.util;

import org.smart.framework.util.thread.OrderTask;

public class TestOrderTask implements OrderTask {

	private String taskName;
	private String group;
	
	

	public TestOrderTask(String taskName, String group) {
		super();
		this.taskName = taskName;
		this.group = group;
	}

	@Override
	public void run() {
		System.out.println(identify() + "--" +taskName + " invoke " + Thread.currentThread().getName());
	}

	@Override
	public Object identify() {
		return group;
	}

}
