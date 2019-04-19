package org.smart.framework.datacenter;

import java.util.Collection;

/**
 * 入库队列接口
 * @author ludd
 *
 */
public interface DBQueue {
	/**
	 * 初始化
	 */
	public void initialize();
	/**
	 * 数据更新队列
	 * @param entity
	 */
	void updateQueue(Entity... entity);
	
	void deleteQueue(Entity... entity);
	
	/**
	 * 数据更新队列
	 * @param entities
	 */
	void updateQueue(Collection<Entity> entities);
	/**
	 * 数据更新队列
	 * @param entity
	 */
	void insertQueue(Entity... entity);
	
	/**
	 * 数据更新队列
	 * @param entities
	 */
	void insertQueue(Collection<Entity> entities);
	
	/**
	 * 获取任务队列任务数
	 * @return
	 */
	public int getTaskSize();
	/**
	 * 获取未保存的普通实体数
	 * @return
	 */
	public int getNormalEntitySize();
	
	/**
	 * 获得角色未保存个数
	 * @return
	 */
	public int getActorSize();

	/**
	 * 提交队列
	 * @param flag:0使用系统配置
	 */
	void changeBlockTime(int flag); 
	
	/**
	 * 角色是否在队列中
	 * @param actorId
	 * @return
	 */
	boolean actorInQueue(Object actorId);
	
	void addShutdownBefore(Runnable r);

}