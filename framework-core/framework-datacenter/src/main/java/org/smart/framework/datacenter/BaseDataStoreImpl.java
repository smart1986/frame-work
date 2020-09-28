package org.smart.framework.datacenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.smart.framework.datacenter.annotation.DBQueueType;
import org.smart.framework.util.ConcurrentLinkedQueueHashMap;
import org.smart.framework.util.NamedThreadFactory;
import org.smart.framework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
/**
 * 队列入库实现
 * @author smart
 *
 */
public class BaseDataStoreImpl implements DataStore {
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	/**
	 * db队列日志记录于 logs/dbqueue.log
	 */
	private static final Logger DB_QUEUE_LOGGER = LoggerFactory.getLogger("dbqueue");

	@Autowired
	private BaseJdbcTemplate jdbcTemplate;

	@Autowired(required = false)
	@Qualifier("dbqueue.max_block_time_of_actor_cache")
	private Integer actorBlockTime = 5000;

	@Autowired(required = false)
	@Qualifier("dbqueue.actor_each_submit_num")
	private Integer actorEachSubmitNum = 1000;

	@Autowired(required = false)
	@Qualifier("dbqueue.max_block_time_of_normal_cache")
	private Integer normalBlockTime = 5000;

	@Autowired(required = false)
	@Qualifier("dbqueue.normal_each_submit_num")
	private Integer normalEachSubmitNum = 1000;
	@Autowired(required = false)
	@Qualifier("dbqueue.insert_each_submit_num")
	private Integer insertEachSubmitNum = 1000;

	/**
	 * db执行线程
	 */
	private static ThreadPoolExecutor EXECUTOR;

	/**
	 * 互斥同步锁
	 */
	private final ReentrantLock takeLockActor = new ReentrantLock();

	private final Condition notEmptyActor = this.takeLockActor.newCondition();

	/**
	 * 互斥同步锁
	 */
	private final ReentrantLock takeLockNormal = new ReentrantLock();

	private final Condition notEmptyNormal = this.takeLockNormal.newCondition();

	private final ReentrantLock takeLockInsert = new ReentrantLock();
	// private final ReentrantLock takeLockDelete = new ReentrantLock();

	private final Condition notEmptyInsert = this.takeLockInsert.newCondition();
	// private final Condition notEmptyDelete = this.takeLockDelete.newCondition();

	/**
	 * 角色保存队列
	 */
	private ConcurrentLinkedQueueHashMap<Object, List<Entity>> actorQueueMap = new ConcurrentLinkedQueueHashMap<>(
			Short.MAX_VALUE);

	/**
	 * 角色是否在队列/执行线程中的标识
	 */
	private Set<Object> queueFlagSet = Collections.synchronizedSet(new HashSet<Object>());

	/**
	 * 普通实体队列
	 */
	private ConcurrentLinkedQueue<Entity> normalEntityQueue = new ConcurrentLinkedQueue<>();
	/**
	 * 删除队列
	 */
	private ConcurrentLinkedQueue<Entity> deleteEntityQueue = new ConcurrentLinkedQueue<>();

	/**
	 * 保存实体队列
	 */
	private ConcurrentLinkedQueue<Entity> insertEntityQueue = new ConcurrentLinkedQueue<>();

	/**
	 * 同步锁对象
	 */
	private byte[] syncLock = new byte[0];

	private boolean batch = false;

	private LinkedBlockingQueue<Runnable> shutdownTask = new LinkedBlockingQueue<>();

	@Override
	public void initialize() {
		LOGGER.info("Initialize DB Daemon Thread...");

		ThreadGroup threadGroup = new ThreadGroup("executor service db queue thread group");

		NamedThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "db queue thread");
		NamedThreadFactory executethreadFactory = new NamedThreadFactory(threadGroup,
				"db execute queue thread");

		EXECUTOR = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(), executethreadFactory);

		LOGGER.info("submit entity thread start...");

		// 定时添加保存角色对象至队列
		createPollCachedObjThread(threadFactory, takeLockActor, notEmptyActor, actorBlockTime, 1);

		// 定时添加保存普通对象至队列
		createPollCachedObjThread(threadFactory, takeLockNormal, notEmptyNormal, normalBlockTime,
				2);
		// 定时保存insert对象至队列
		createPollCachedObjThread(threadFactory, takeLockInsert, notEmptyInsert,
				insertEachSubmitNum, 3);
		// createPollCachedObjThread(threadFactory, takeLockDelete, notEmptyDelete,
		// insertEachSubmitNum, 4);

		LOGGER.info("shutdown hook thread ready...");

		try {
			shutdownTask.put(closeDBTask());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Runtime.getRuntime().addShutdownHook(threadFactory.newThread(shutdownHook()));

		LOGGER.info("EntityDBQueue startup complete！.....");
	}

	private void createPollCachedObjThread(ThreadFactory threadFactory,
			final ReentrantLock reentrantLock, final Condition condition, final int blockTime,
			final int type) {
		Thread pollCachedObjThread = threadFactory.newThread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						if (blockTime > 0) {
							reentrantLock.lockInterruptibly();
							try {
								condition.await(blockTime, TimeUnit.MILLISECONDS);
							} catch (Exception e) {
								LOGGER.error("{}", e);
							} finally {
								reentrantLock.unlock();
							}
						}
						if (type == 1) {
							submitActorEntity();
						} else if (type == 2) {
							submitNormalEntity();
						} else if (type == 3) {
							submitInsertEntity();
						} else {
							submitDeleteEntity();
						}
					} catch (Exception ex) {
						LOGGER.error("{}", ex);
					}
				}
			}
		});
		pollCachedObjThread.setDaemon(true);
		pollCachedObjThread.start();
	}

	@Override
	public void update(Entity... entities) {
		for (Entity entity : entities) {
			entity = entity.depthClone();
			EntityInfo info = jdbcTemplate.getEntityInfo(entity.getClass());
			if (info.tableType.equals(DBQueueType.IMPORTANT)) {
				putActorEntity(entity); // 强转Import队列的都为Long型主键
			} else if (info.tableType.equals(DBQueueType.DEFAULT)) {
				if (normalEntityQueue.contains(entity) == false) {
					normalEntityQueue.add(entity);
				} else {
					normalEntityQueue.remove(entity);
					normalEntityQueue.add(entity);

				}
			} else {
				LOGGER.warn("entity not in queue, tableName:[%s]", info.tableName);
				jdbcTemplate.update(entity);
			}
		}
	}

	@Override
	public void update(Collection<Entity> entities) {
		for (Entity entity : entities) {
			update(entity);
		}
	}

	/**
	 * 提交未保存map中的实体到保存队列
	 * 
	 * @param true:使用配置时间，false:使用1秒提交一次
	 * @return 返回提交个数
	 */
	public void changeBlockTime(int flag) {
		// 设置阻塞时间为flag ms
		// if (flag <= 1000) {
		// normalBlockTime = (Integer)
		// SpringContext.getBean("dbqueue.max_block_time_of_normal_cache");
		// actorBlockTime = (Integer)
		// SpringContext.getBean("dbqueue.max_block_time_of_actor_cache");
		// LOGGER.info(String.format("还原系统提交实体设置,normalBlockTime:[%s]ms,
		// actorBlockTime:[%s]ms", normalBlockTime, actorBlockTime));
		// } else {
		// normalBlockTime = flag;
		// actorBlockTime = flag;
		// LOGGER.info(String.format("更改系统提交实体设置，间隔为:[%s]ms", flag));
		// }
	}

	/**
	 * 放置核心实体到队列
	 * 
	 * @param entity
	 */
	private void putActorEntity(Entity entity) {
		Object actorId = null;
		if (entity instanceof MultiEntity<?>) {
			MultiEntity<?> multiEntity = (MultiEntity<?>) entity;
			actorId = multiEntity.findFkId();
		} else {
			actorId = entity.findPkId();
		}
		if (actorId == null) {
			EntityInfo info = jdbcTemplate.getEntityInfo(entity.getClass());
			LOGGER.warn(String.format("entity pk is null tableName:[%s]", info.tableName));
			return;
		}

		synchronized (syncLock) {
			List<Entity> entitySet = actorQueueMap.get(actorId);
			if (entitySet == null) {
				entitySet = new ArrayList<Entity>();
				actorQueueMap.add(actorId, entitySet);
			}
			int index = entitySet.indexOf(entity);
			if (index >= 0) {
				entitySet.set(index, entity);
			} else {
				entitySet.add(entity);
			}
//			if (entitySet.contains(entity)) {
//				entitySet.remove(entity);
//			}
			
			
			queueFlagSet.add(actorId);
		}
	}

	/**
	 * 提交核心实体
	 * 
	 * @return
	 */
	private void submitActorEntity() {
		synchronized (syncLock) {
			if (actorQueueMap.isEmpty()) {
				return;
			}

			for (int i = 0; i < actorEachSubmitNum; i++) {
				// 单人数据集合进入一个线程处理
				Map.Entry<Object, List<Entity>> entry = actorQueueMap.poll();
				if (entry == null) {
					break;
				}

				if (entry.getValue() == null) {
					LOGGER.warn("actorId:[%s] actorEntityMap is null.");
					continue;
				}

				if (entry.getValue().isEmpty()) {
					continue;
				}
				Runnable updateTask = createActorUpdateTask(entry.getKey(), entry.getValue());
				EXECUTOR.submit(updateTask);
				DB_QUEUE_LOGGER.debug(String.format("submit actorId:[%s], entity num:[%s].",
						entry.getKey(), entry.getValue().size()));
			}
		}
	}

	/**
	 * 提交普通实体
	 * 
	 * @return
	 */
	private void submitNormalEntity() {
		if (normalEntityQueue.isEmpty()) {
			return;
		}

		Map<String, Integer> entityInfo = new HashMap<>();
		List<Entity> list = new ArrayList<>();
		for (int i = 0; i < normalEachSubmitNum; i++) {
			Entity entity = normalEntityQueue.poll();
			if (entity != null) {
				EntityInfo info = jdbcTemplate.getEntityInfo(entity.getClass());
				int num = 0;
				if (entityInfo.containsKey(info.tableName)) {
					num = entityInfo.get(info.tableName);
					num += 1;
				} else {
					num = 1;
				}
				entityInfo.put(info.tableName, num);
				list.add(entity);
			}
		}
		Runnable updateTask = createUpdateTask(list);
		EXECUTOR.submit(updateTask);

		DB_QUEUE_LOGGER.debug(String.format("submit normal entity :[%s]",
				StringUtils.map2DelimiterString(entityInfo)));
	}

	private void submitDeleteEntity() {
		if (deleteEntityQueue.isEmpty()) {
			return;
		}

		Map<String, Integer> entityInfo = new HashMap<>();
		for (int i = 0; i < normalEachSubmitNum; i++) {
			Entity entity = deleteEntityQueue.poll();
			if (entity != null) {
				EntityInfo info = jdbcTemplate.getEntityInfo(entity.getClass());
				int num = 0;
				if (entityInfo.containsKey(info.tableName)) {
					num = entityInfo.get(info.tableName);
					num += 1;
				} else {
					num = 1;
				}
				entityInfo.put(info.tableName, num);
				Runnable updateTask = createActorDeleteTask(entity);
				EXECUTOR.submit(updateTask);
			}
		}

		DB_QUEUE_LOGGER.debug(String.format("submit normal entity :[%s]",
				StringUtils.map2DelimiterString(entityInfo)));
	}

	/**
	 * 创建实体更新任务
	 * 
	 * @param updateList
	 *            实体列表
	 * @return
	 */
	private Runnable createActorUpdateTask(final Object actorId,
			final Collection<Entity> updateList) {
		return new Runnable() {
			@Override
			public void run() {
				if (batch) {
					jdbcTemplate.update(updateList);
				} else {

					List<Entity> failList = new ArrayList<>();
					for (Entity entity : updateList) {
						try {
							jdbcTemplate.update(entity);
						} catch (Exception e) {
							EntityInfo info = jdbcTemplate.getEntityInfo(entity.getClass());
							if (e instanceof DataAccessException) { // 数据库访问异常重新加入队列
								failList.add(entity);
								LOGGER.error(String.format(
										"save db error. actorId:%s, tableName:[%s], entity add to updateQueue.",
										actorId, info.tableName), e);
							} else {
								LOGGER.error(String.format(
										"save db error. actorId:%s, tableName:[%s], entity drop.",
										actorId, info.tableName), e);
							}
						}
					}

					if (failList.size() > 0) {
						update(failList);
					}

				}
			}
		};
	}

	private Runnable createActorDeleteTask(final Entity entity) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					jdbcTemplate.delete(entity);
				} catch (Exception e) {
					EntityInfo info = jdbcTemplate.getEntityInfo(entity.getClass());
					if (e instanceof DataAccessException) { // 数据库访问异常重新加入队列
						LOGGER.error(String.format(
								"save db error. tableName:[%s], entity add to deleteQueue.",
								info.tableName), e);
					} else {
						LOGGER.error(String.format("save db error. tableName:[%s], entity drop.",
								info.tableName), e);
					}
				}
			}

		};
	}

	/**
	 * 创建实体更新任务
	 * 
	 * @param updateList
	 *            实体列表
	 * @return
	 */
	private Runnable createUpdateTask(final List<Entity> updateEntitys) {
		return new Runnable() {
			@Override
			public void run() {
				if (batch) {
					jdbcTemplate.update(updateEntitys);
				} else {
					for (Entity updateEntity : updateEntitys) {

						try {
							jdbcTemplate.update(updateEntity);
						} catch (Exception e) {
							EntityInfo info = jdbcTemplate.getEntityInfo(updateEntity.getClass());
							if (e instanceof DataAccessException) { // 数据库访问异常重新加入队列
								update(updateEntity);
								LOGGER.error(String.format(
										"save db error. pk:[%s], tableName:[%s], entity add to updateQueue.",
										updateEntity.findPkId(), info.tableName), e);
							} else {
								LOGGER.error(String.format(
										"save db error. pk:[%s], tableName:[%s], entity drop.",
										updateEntity.findPkId(), info.tableName), e);
							}
						}
					}
				}
			}
		};
	}

	/**
	 * 获取任务队列任务数
	 * 
	 * @return
	 */
	public int getTaskSize() {
		int num = 0;
		if (EXECUTOR instanceof ThreadPoolExecutor) {
			num += EXECUTOR.getQueue().size();
		}
		return num;
	}

	public int getNormalEntitySize() {
		return this.normalEntityQueue.size();
	}

	public int getActorSize() {
		return actorQueueMap.size();
	}

	private long getEntitySize() {
		return actorQueueMap.size() + normalEntityQueue.size() + deleteEntityQueue.size()
				+ insertEntityQueue.size();
	}

	/**
	 * 服务器关闭勾子执行逻辑
	 * 
	 * @return
	 */
	private Runnable shutdownHook() {
		return new Runnable() {
			@Override
			public void run() {
				for (Runnable r : shutdownTask) {
					r.run();
				}
			}
		};
	}

	private Runnable closeDBTask() {
		return new Runnable() {
			@Override
			public void run() {

				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				while (EXECUTOR != null) {
					if (EXECUTOR.isShutdown()) {
						break;
					}

					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						LOGGER.error("{}", e);
					}

					long entitySize = getEntitySize();
					LOGGER.info(String.format("executor has task num: [%s], db cache size:[%s]",
							getTaskSize(), entitySize));
					if (getTaskSize() == 0 && entitySize == 0) {
						try {
							EXECUTOR.shutdown();
							EXECUTOR.awaitTermination(30L, TimeUnit.SECONDS);
						} catch (InterruptedException e) {
							LOGGER.error("{}", e);
						}
					}
				}

				LOGGER.info("EntityDBQueue shutdown complete!.....");
			}
		};
	}

	public boolean actorInQueue(Object actorId) {
		synchronized (syncLock) {

			return queueFlagSet.contains(actorId);
		}
	}

	@Override
	public void insert(Collection<Entity> entities) {
		for (Entity entity : entities) {
			insert(entity);
		}
	}

	@Override
	public void insert(Entity... entities) {
		for (Entity entity : entities) {
			insertEntityQueue.add(entity);
		}
	}

	/**
	 * 提交普通实体
	 * 
	 * @return
	 */
	private void submitInsertEntity() {
		if (insertEntityQueue.isEmpty()) {
			return;
		}

		Map<String, Integer> entityInfo = new HashMap<>();
		for (int i = 0; i < insertEachSubmitNum; i++) {
			Entity entity = insertEntityQueue.poll();
			if (entity != null) {
				EntityInfo info = jdbcTemplate.getEntityInfo(entity.getClass());
				int num = 0;
				if (entityInfo.containsKey(info.tableName)) {
					num = entityInfo.get(info.tableName);
					num += 1;
				} else {
					num = 1;
				}
				entityInfo.put(info.tableName, num);
				Runnable updateTask = createInsertTask(entity);
				EXECUTOR.submit(updateTask);
			}
		}

		DB_QUEUE_LOGGER.debug(String.format("submit normal entity :[%s]",
				StringUtils.map2DelimiterString(entityInfo)));
	}

	/**
	 * 创建实体更新任务
	 * 
	 * @param updateList
	 *            实体列表
	 * @return
	 */
	protected Runnable createInsertTask(final Entity entity) {
		return new Runnable() {
			@Override
			public void run() {
				Entity en = entity;
				try {
					jdbcTemplate.update(entity);
				} catch (Exception e) {
					EntityInfo info = jdbcTemplate.getEntityInfo(en.getClass());
					LOGGER.error(
							String.format("save db error. pk:[%s], tableName:[%s], entity drop.",
									en.findPkId(), info.tableName),
							e);
				}
			}
		};
	}

	@Override
	public void delete(Entity... entitys) {
		for (Entity entity : entitys) {
			deleteEntityQueue.add(entity);
		}
	}

	public void addShutdownBefore(Runnable r) {
		shutdownTask.add(r);

	}

}
