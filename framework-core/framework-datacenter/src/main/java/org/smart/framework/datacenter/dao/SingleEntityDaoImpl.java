package org.smart.framework.datacenter.dao;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.smart.framework.datacenter.SingleEntity;
import org.smart.framework.datacenter.cache.DataCache;
import org.smart.framework.datacenter.cache.GoogleCacheImpl;
import org.smart.framework.util.IdentiyKey;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
/**
 * 一对一dao基类
 * @author jerry
 *
 */
public abstract class SingleEntityDaoImpl<T extends SingleEntity> extends DefaultDao<T>{
	
	private Cache<Object, Object> mappingList = CacheBuilder.newBuilder().expireAfterAccess(cacheTime, TimeUnit.SECONDS)
			.maximumSize(cacheSize).build();
	

	protected DataCache<T> dataCache;
	
	@Override
	protected void createCache() {
		dataCache = new GoogleCacheImpl<T>(this, this.cacheTime, this.cacheSize,this.cacheMaintain);
	}
	
	@Override
	public void init() {
		initMaxId();
	}
	@Override
	public void cleanCache() {
		dataCache.clean();
		
	}

	/**
	 * 从缓存获取实体
	 * @param actorId
	 * @param clz
	 * @return
	 */
	protected T getFromCache(final Object key) {
		return  dataCache.getFromCache(key);
	}
	/**
	 * 通过非主键key查找
	 * @param key
	 * @return
	 */
	protected T getFromCacheWithCacheOtherKey(final IdentiyKey key) {
		Object mKey = getMapping(key);
		if (mKey == null){
			T entity = loadFromDBOtherKey(key);
			if (entity != null){
				set2Cache(entity);
			}
			return (T) entity;
		}
		return dataCache.getFromCache(mKey);
	}
	
	/**
	 * 获取实体
	 * @param actorId
	 * @param clz
	 * @return
	 */
	public T get(IdentiyKey actorId) {
		return get(actorId, true);
	}
	/**
	 * 获取实体
	 * @param actorId
	 * @param clz
	 * @return
	 */
	public  T get(IdentiyKey actorId, boolean pk) {
		if (pk){
			return getFromCache(actorId);
		} else {
			return getFromCacheWithCacheOtherKey(actorId);
		}
	}
	
	
	public void updateQueue(T entity) {
		entity.setNewEntity(false);
		set2Cache(entity);
		dbQueue.updateQueue(entity);
	}
	
	private Object getMapping( Object key){
		return mappingList.getIfPresent(key);
	}
	
	private void setMapping(SingleEntity entity){
		List<IdentiyKey> keys = entity.keyLists();
		if (!keys.isEmpty()){
			for (int i = 0; i < keys.size(); i++) {
				mappingList.put(keys.get(i), entity.findPkId());
			}
		}
	}
	
	protected void set2Cache(T entity) {
		dataCache.setToCache(entity.findPkId(), entity);
		setMapping(entity);
	}
	public DataCache<T> getDataCache() {
		return dataCache;
	}
	
	@Override
	public T load(Object key) throws Exception {
		IdentiyKey pk = (IdentiyKey) key;
		T entity = loadFromDB(pk);
		if (entity == null){
			entity = newInstance(pk, forClass());
		} else {
			entity.setNewEntity(false);
			setMapping(entity);
		}
		return entity;
	}
	
	/**
	 * 从数据库加载
	 * 
	 * @param condition
	 * @param clz
	 * @return
	 */
	protected T loadFromDB(IdentiyKey key) {
		T entity = jdbc.get(forClass(), key);
		return entity;
	}
	protected T loadFromDBOtherKey(IdentiyKey key) {
		throw new RuntimeException("not implement!");
	}

	protected  T newInstance(IdentiyKey actorId, Class<T> clz){
		try {
			T entity = clz.newInstance();
//			LOGGER.debug(String.format("entity create new,class:[%s]",clz.getName()));
			entity.setPkId(actorId);
			entity.setNewEntity(true);
			return entity;
		} catch (Exception e) {
			LOGGER.error("{}", e);
		} 
		return null;
	}
	
	/**
	 * 对应class类
	 * 
	 * @return
	 */
	protected abstract Class<T> forClass();
	
	/**
	 * 初始化maxId
	 * 
	 * @return
	 */
	protected abstract void initMaxId();
	/**
	 * 判断实体是否存在
	 * @param key
	 * @param clz
	 * @return
	 */
	public boolean exsit(IdentiyKey key, Class<? extends SingleEntity> clz){
		if (dataCache.exist(key)){
			return true;
		}
		SingleEntity result = jdbc.get(clz, key);
		if (result != null){
			return true;
		}
		return false;
	}
	
	public void delete(SingleEntity entity){
		List<IdentiyKey> keys = entity.keyLists();
		if (keys.size() > 1){
			for (int i = 1; i < keys.size(); i++) {
				mappingList.invalidate(keys.get(i));
			}
		}
		jdbc.delete(entity);
		dataCache.remove(entity);
	}
	
}
