package org.smart.framework.datacenter;

import java.util.Collection;

/**
 * 入库队列接口
 * @author smart
 *
 */
public interface DataStore {
	/**
	 * 初始化
	 */
	void initialize();
	/**
	 * 数据更新队列
	 * @param entity
	 */
	void update(Entity... entity);
	
	void delete(Entity... entity);
	
	/**
	 * 数据更新队列
	 * @param entities
	 */
	void update(Collection<Entity> entities);
	/**
	 * 数据更新队列
	 * @param entity
	 */
	void insert(Entity... entity);
	
	/**
	 * 数据更新队列
	 * @param entities
	 */
	void insert(Collection<Entity> entities);

}