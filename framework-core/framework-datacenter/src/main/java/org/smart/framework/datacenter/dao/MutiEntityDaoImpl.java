package org.smart.framework.datacenter.dao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.smart.framework.datacenter.EntityInfo;
import org.smart.framework.datacenter.MutiEntity;
import org.smart.framework.datacenter.cache.DataCache;
import org.smart.framework.datacenter.cache.GoogleCacheImpl;
import org.smart.framework.util.IdentiyKey;

import com.google.common.collect.Lists;

/**
 * 一对多dao基类
 * 
 * @author smart
 *
 */
public abstract class MutiEntityDaoImpl<FK,T extends MutiEntity<FK>> extends DefaultDao<Map<IdentiyKey, T>> {

	protected DataCache<Map<IdentiyKey, T>> dataCache;
	@PostConstruct
	@Override
	protected void createCache() {
		dataCache = new GoogleCacheImpl<Map<IdentiyKey, T>>(this, this.cacheTime, this.cacheSize,this.cacheMaintain);
	}
	@Override
	public void init() {
		
		initMaxId();
	}
	@Override
	public void cleanCache() {
		dataCache.clean();
		
	}

	@Override
	protected Map<IdentiyKey, T> getFromCache(Object key) {
		return dataCache.getFromCache(key);
	}
	/**
	 * 获取实体
	 * 
	 * @param actorId
	 * @param clz
	 * @return
	 */
	public Map<IdentiyKey, T> getByFk(FK fk) {
		return dataCache.getFromCache(fk);
	}

	public void updateQueue(T entity) {
		set2Cache(entity);
		dbQueue.updateQueue(entity);
	}

	protected void set2Cache(T entity) {
		Map<IdentiyKey, T> map = dataCache.getFromCache(entity.findFkId());
		map.put(entity.findPkId(), entity);
	}

	public DataCache<Map<IdentiyKey, T>> getDataCache() {
		return dataCache;
	}

	@Override
	public Map<IdentiyKey, T> load(Object key) throws Exception {
		List<T> entitys = loadFromDBWithFK(key);
		Map<IdentiyKey, T> map = new ConcurrentHashMap<>();
		for (T entity : entitys) {
			if (entity.findFkId() == null) {
				throw new RuntimeException("FK is null!");
			}
			map.put(entity.findPkId(), entity);

		}
		// LOGGER.debug("load from db, key:{}, size:{}", key.toString(),
		// entitys.size());
		return map;
	}
	

	/**
	 * 从数据库加载
	 * 
	 * @param condition
	 * @param clz
	 * @return
	 */
	protected List<T> loadFromDBWithFK(Object key) {
		LinkedHashMap<String, Object> condition = new LinkedHashMap<>();
		Class<? extends MutiEntity<?>> clz = forClass();
		EntityInfo info = jdbc.getEntityInfo(clz);
		if (info.fkName == null) {
			throw new RuntimeException("fk not exsit!");
		}
		condition.put(info.fkName, key);
		return loadFromDBWithFK(condition, forClass());
	}

	/**
	 * 从数据库加载
	 * 
	 * @param condition
	 * @param clz
	 * @return
	 */
	protected  List<T> loadFromDBWithFK(LinkedHashMap<String, Object> condition,
			Class<T> clz) {
		List<T> list = jdbc.getList(clz, condition);
		List<T> result = Lists.newArrayList();
		result.addAll(list);
		return result;
	}

	/**
	 * 删除实体
	 * 
	 * @param entity
	 */
	public void delete(T entity) {
		Map<IdentiyKey, T> map = dataCache.getFromCache(entity.findFkId());
		jdbc.delete(entity);
		map.remove(entity.findPkId());
	}

	@Deprecated
	public void deleteQueue(MutiEntity<?> entity) {
		Map<IdentiyKey, T> map = dataCache.getFromCache(entity.findFkId());
		map.remove(entity.findPkId());

		dbQueue.deleteQueue(entity);
	}

	/**
	 * 获取一个实体
	 * 
	 * @param fk
	 * @param pk
	 * @return
	 */
	public T getMutiEnity(FK fk, IdentiyKey pk) {
		Map<IdentiyKey, T> map = this.getByFk(fk);
		return  map.get(pk);
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

}
