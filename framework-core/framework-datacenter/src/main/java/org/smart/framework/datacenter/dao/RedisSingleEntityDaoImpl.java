package org.smart.framework.datacenter.dao;

import javax.annotation.PostConstruct;

import org.smart.framework.datacenter.SingleEntity;
import org.smart.framework.datacenter.cache.RedisCacheImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public abstract class RedisSingleEntityDaoImpl<T extends SingleEntity> extends SingleEntityDaoImpl<T>{
	@Autowired
	private RedisTemplate<String, T> redisTemplate;
	
	private String keyPrefix = this.getClass().getName() + "_";
	
//	private long expire = -1;
	
	@PostConstruct
	protected void createCache() {
		dataCache = new RedisCacheImpl<>(this, this.cacheTime,redisTemplate,keyPrefix);
	}
	@Override
	public void init() {
		initMaxId();
	}


}
