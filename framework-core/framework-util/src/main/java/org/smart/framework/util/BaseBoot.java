package org.smart.framework.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.smart.framework.util.common.GameInit;
import org.smart.framework.util.common.InitIndex;
import org.smart.framework.util.schedule.ScheduleJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
/**
 * 游戏初始化过程
 * 第一步：配置初始化
 * 第二步: 存储初始化
 * 第三步：游戏业务初始化
 * 第四步：时间调度相关初始化
 * 第五步：网络初始化
 * @author jerry
 *
 */
public class BaseBoot implements ApplicationListener<ContextRefreshedEvent> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			logger.info("spring boot complete!");
			try {
				dataConfigInit(event);
				dbInit(event);
				gameInit(event);
				jobInit(event);
				netInit(event);

			} catch (Exception e) {
				logger.error("{}", e);
				System.exit(0);
			}

		}
	}

	/**
	 * 网络初始化
	 * @param event spring启动事件
	 * @throws Exception
	 */
	protected void netInit(ContextRefreshedEvent event) throws Exception{

	}

	/**
	 * 时间调度初始化
	 * @param event
	 */
	protected void jobInit(ContextRefreshedEvent event) {
		// 时间调度相关初始化
		Map<String, ScheduleJob> jobs = event.getApplicationContext().getBeansOfType(ScheduleJob.class);
		for (ScheduleJob job : jobs.values()) {
			job.ready();
		}

	}

	/**
	 * 游戏业务初始化
	 * @param event
	 */
	protected void gameInit(ContextRefreshedEvent event) {
		Map<String, GameInit> gameInits = event.getApplicationContext().getBeansOfType(GameInit.class);
		List<GameInit> list = new ArrayList<>();
		for (GameInit gameInit : gameInits.values()) {
			if (!gameInit.index().equals(InitIndex.INIT_FIVE)) {
				list.add(gameInit);
			}
		}
		Collections.sort(list, new Comparator<GameInit>() {

			@Override
			public int compare(GameInit o1, GameInit o2) {
				if (o1.index().ordinal() > o2.index().ordinal()) {
					return 1;
				} else if (o1.index().ordinal() < o2.index().ordinal()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		for (GameInit gameInit : list) {
			gameInit.gameInit();
		}
	}
	/**
	 * 网络初始化完成以后(例如需要向其他应用通讯此时执行)
	 * @param event
	 */
	protected void afterNetInit(ContextRefreshedEvent event) {
		Map<String, GameInit> gameInits = event.getApplicationContext().getBeansOfType(GameInit.class);
		List<GameInit> list = new ArrayList<>();
		for (GameInit gameInit : gameInits.values()) {
			if (gameInit.index().equals(InitIndex.INIT_FIVE)) {
				list.add(gameInit);
			}
		}
		
		for (GameInit gameInit : list) {
			gameInit.gameInit();
		}
	}
	/**
	 * 配置初始化
	 * @param event
	 * @throws Exception
	 */
	protected void dataConfigInit(ContextRefreshedEvent event) throws Exception{
	}

	/**
	 * 存储初始化
	 * @param event
	 * @throws Exception
	 */
	protected void dbInit(ContextRefreshedEvent event) throws Exception{

	}

}
