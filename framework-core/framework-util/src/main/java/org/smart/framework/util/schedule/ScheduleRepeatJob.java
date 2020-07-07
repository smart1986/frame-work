package org.smart.framework.util.schedule;

import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;

/**
 * 一定频率时间调度
 * @author smart
 *
 */
public abstract class ScheduleRepeatJob implements ScheduleJob {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	protected TaskScheduler taskScheduler;
	
	private ScheduledFuture<?> future;
	
	@Override
	public void ready() {
		long period = getPeriod();
		future = taskScheduler.scheduleAtFixedRate(this, period);
		logger.info("ScheduleRepeatJob:{},period:{} ready!...",jobName(),period);
	}
	/**
	 * 调度间隔
	 * @return
	 */
	protected abstract long getPeriod();
	
	@Override
	public void run() {
		try{
			logger.trace("ScheduleRepeatJob:{},execute start...",jobName());
			execute();
			logger.trace("ScheduleRepeatJob:{},execute complete...",jobName());
		}catch (Exception e) {
			logger.error("ScheduleRepeatJob:{},execute error...",jobName());
			logger.error("{}",e);
		}
	}
	
	/**
	 * 执行任务
	 */
	public abstract void execute();
	
	@Override
	public void shutdown() {
		if (future != null){
			future.cancel(false);
			logger.info("ScheduleRepeatJob {} shutdown!!!!", jobName());
		}
	}
}
