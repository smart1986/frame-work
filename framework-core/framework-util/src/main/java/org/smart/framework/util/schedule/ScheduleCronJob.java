package org.smart.framework.util.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.smart.framework.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

/**
 * 表达式时间调度
 * @author smart
 *
 */
public abstract class ScheduleCronJob implements ScheduleJob {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	protected TaskScheduler taskScheduler;
	
	private List<ScheduledFuture<?>> futures = new ArrayList<>();
	
	@Override
	public void ready() {
		String[] crons = getCronExpressions();
		for (String cron : crons) {
			Trigger trigger = new CronTrigger(cron);
			ScheduledFuture<?> f = taskScheduler.schedule(this, trigger );
			futures.add(f);
			logger.info("ScheduleCronJob:{},cron:{} ready!...",jobName(),cron);
		}
	}
	/**
	 * 调度时间
	 * @return
	 */
	protected abstract String[] getCronExpressions();
	
	@Override
	public void run() {
		try{
			logger.trace("ScheduleCronJob:{},execute start...",jobName());
			execute();
			logger.trace("ScheduleCronJob:{},execute complete...",jobName());
		}catch (Exception e) {
			logger.error("ScheduleCronJob:{},execute error... {}",jobName(), ExceptionUtil.getStackTrace(e));
			logger.error("{}",e);
		}
	}
	
	/**
	 * 执行任务
	 */
	public abstract void execute();
	
	@Override
	public void shutdown() {
		if (!this.futures.isEmpty()){
			for (ScheduledFuture<?> scheduledFuture : futures) {
				scheduledFuture.cancel(false);
				logger.info("ScheduleCronJob {} shutdown!!!!", jobName());
			}
		}
	}
}
