package org.smart.framework.datacenter.cache;

import java.util.Collection;
import java.util.Map;
/**
 * 数据库实体缓存接口
 * @author jerry
 *
 * @param <T>
 */
public interface DataCache<T> {
	/**
	 * 从缓存取
	 * 
	 * @param key
	 * @return
	 */
	T getFromCache(Object key);

	/**
	 * 放入缓存
	 * 
	 * @param key
	 * @param value
	 */
	void setToCache(Object key, T value);
	/**
	 * 缓存中总数量
	 * @return
	 */
	long size();
	/**
	 * 缓存中是否存在
	 * @param key
	 * @return
	 */
	boolean exist(Object key);
	
	Collection<?> all();

	Map<Object, T> mapAll();
	
	void remove(Object key);
	void clean();

}
