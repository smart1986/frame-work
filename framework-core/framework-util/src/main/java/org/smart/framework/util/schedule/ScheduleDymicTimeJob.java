package org.smart.framework.util.schedule;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
/**
 * 动态时间调度
 * @author smart
 *
 */
public abstract class ScheduleDymicTimeJob implements ScheduleJob {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	protected TaskScheduler taskScheduler;
	private ScheduledFuture<?> future;
	@Override
	public void run() {
		
		try{
			logger.trace("ScheduleDymicTimeJob:{},execute start...",jobName());
			execute();
			logger.trace("ScheduleDymicTimeJob:{},execute complete...",jobName());
			ready();
		}catch (Exception e) {
			logger.info("ScheduleDymicTimeJob:{},execute error...",jobName());
			logger.error("{}",e);
		}
		
		
	}

	@Override
	public void ready() {
		Date nextTime = nextTime();
		future = taskScheduler.schedule(this, nextTime);
		logger.info("ScheduleDymicTimeJob:{},nextTime:{} ready!...",jobName(),nextTime);
	}
	
	/**
	 * 下次调度时间
	 * @return
	 */
	protected abstract Date nextTime();
	
	/**
	 * 执行任务
	 */
	public abstract void execute();
	
	@Override
	public void shutdown() {
		if (future != null){
			future.cancel(false);
			logger.info("ScheduleDymicTimeJob {} shutdown!!!!", jobName());
		}
	}


}
