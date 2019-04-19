package org.smart.framework.redis.dao;

import javax.annotation.PostConstruct;

import org.smart.framework.datacenter.SingleEntity;
import org.smart.framework.datacenter.dao.SingleEntityDaoImpl;
import org.smart.framework.redis.cache.RedisCacheImpl;
import org.smart.framework.redis.cache.RedisTool;
import org.smart.framework.util.IdentiyKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public abstract class RedisSingleEntityDaoImpl<T extends SingleEntity> extends SingleEntityDaoImpl<T>{
	@Autowired
	private RedisTemplate<String, T> redisTemplate;
	
	private String keyPrefix = this.getClass().getName() + "_";
	
	private int retryTimes = 2;
	private long sleepMillis = 5000L;
	private long expire = 4000L;
	
	private RedisTool redisTool;
	@PostConstruct
	protected void createCache() {
		dataCache = new RedisCacheImpl<>(this, this.cacheTime,redisTemplate,keyPrefix);
		redisTool = new RedisTool(redisTemplate);
	}
	@Override
	public void init() {
		initMaxId();
	}
	
	public boolean tryLock(IdentiyKey key,String requestId) {
		return redisTool.lock(keyPrefix+key.toString()+"_1", requestId,expire,retryTimes,sleepMillis);
	}
	public boolean unlock(IdentiyKey key,String requestId) {
		return redisTool.releaseLock(keyPrefix+key.toString()+"_1", requestId);
	}
	

}
