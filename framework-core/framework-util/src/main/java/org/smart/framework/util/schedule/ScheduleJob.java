package org.smart.framework.util.schedule;
/**
 * 调度接口
 * @author smart
 *
 */
public interface ScheduleJob extends Runnable {
	public void ready();
	String jobName();
	
	public void shutdown();
}
