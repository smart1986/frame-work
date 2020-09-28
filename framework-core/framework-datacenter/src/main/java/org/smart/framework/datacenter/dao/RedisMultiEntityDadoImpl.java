package org.smart.framework.datacenter.dao;

import org.smart.framework.datacenter.MultiEntity;
import org.smart.framework.datacenter.cache.RedisCacheImpl;
import org.smart.framework.util.IdentifyKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

public abstract class RedisMultiEntityDadoImpl<FK,T extends MultiEntity<FK>> extends MultiEntityDaoImpl<FK,T> {
	@Autowired
	private RedisTemplate<String, Map<IdentifyKey, T>> redisTemplate;
	
	private String keyPrefix = this.getClass().getName() + "_";
	@Override
	protected void createCache() {
		dataCache = new RedisCacheImpl<>(this, this.cacheTime, redisTemplate, keyPrefix);
	}
	@Override
	public void init() {
		initMaxId();
	}
	
	@Override
	protected void set2Cache(T entity) {
		Map<IdentifyKey, T> map = dataCache.getFromCache(entity.findFkId());
		map.put(entity.findPkId(), entity);
		dataCache.setToCache(entity.findFkId(), map);
	}
	

}
