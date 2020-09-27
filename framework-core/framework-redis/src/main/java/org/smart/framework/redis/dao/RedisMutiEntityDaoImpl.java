package org.smart.framework.redis.dao;

import java.util.Map;

import org.smart.framework.datacenter.MutiEntity;
import org.smart.framework.datacenter.dao.MutiEntityDaoImpl;
import org.smart.framework.redis.cache.RedisCacheImpl;
import org.smart.framework.util.IdentifyKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public abstract class RedisMutiEntityDaoImpl<FK,T extends MutiEntity<FK>> extends MutiEntityDaoImpl<FK,T>{
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
