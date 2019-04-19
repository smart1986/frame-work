package org.smart.framework.redis.dao;

import java.util.Map;

import org.smart.framework.datacenter.MutiEntity;
import org.smart.framework.datacenter.dao.MutiEntityDaoImpl;
import org.smart.framework.redis.cache.RedisCacheImpl;
import org.smart.framework.redis.cache.RedisTool;
import org.smart.framework.util.IdentiyKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public abstract class RedisMutiEntityDaoImpl<FK,T extends MutiEntity<FK>> extends MutiEntityDaoImpl<FK,T>{
	@Autowired
	private RedisTemplate<String, Map<IdentiyKey, T>> redisTemplate;
	
	private String keyPrefix = this.getClass().getName() + "_";
	
	private int retryTimes = 2;
	private long sleepMillis = 5000L;
	private long expire = 4000L;
	
	private RedisTool redisTool;
	
	@Override
	protected void createCache() {
		dataCache = new RedisCacheImpl<>(this, this.cacheTime, redisTemplate, keyPrefix);
		redisTool = new RedisTool(redisTemplate);
	}
	@Override
	public void init() {
		initMaxId();
	}
	
	public boolean tryLock(IdentiyKey key,String requestId) {
		return redisTool.lock(keyPrefix+key.toString(), requestId,expire,retryTimes,sleepMillis);
	}
	public boolean unlock(IdentiyKey key,String requestId) {
		return redisTool.releaseLock(keyPrefix+key.toString(), requestId);
	}
	@Override
	protected void set2Cache(T entity) {
		Map<IdentiyKey, T> map = dataCache.getFromCache(entity.findFkId());
		map.put(entity.findPkId(), entity);
		dataCache.setToCache(entity.findFkId(), map);
	}
	

}
