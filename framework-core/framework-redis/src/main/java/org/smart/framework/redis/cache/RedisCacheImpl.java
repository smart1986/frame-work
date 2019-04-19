package org.smart.framework.redis.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.smart.framework.datacenter.cache.DataCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.google.common.cache.CacheLoader;

public class RedisCacheImpl<T> implements DataCache<T>{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private CacheLoader<Object, T> cacheLoader;
	private int expireTimeSeconds;
	
	private RedisTemplate<String, T> redisTemplate;
	
	private String keyPrefix;
	public RedisCacheImpl(CacheLoader<Object, T> cacheLoader, int expireTimeSeconds,
			RedisTemplate<String, T> redisTemplate,String keyPrefix) {
		super();
		this.cacheLoader = cacheLoader;
		this.expireTimeSeconds = expireTimeSeconds;
		this.redisTemplate = redisTemplate;
		this.keyPrefix = keyPrefix;
	}
	public RedisCacheImpl(int expireTimeSeconds,
			RedisTemplate<String, T> redisTemplate,String keyPrefix) {
		this(null,expireTimeSeconds,redisTemplate,keyPrefix);
	}
	
	
	

	@Override
	public T getFromCache(Object key) {
		T o = redisTemplate.opsForValue().get(keyPrefix + key);
		if (o == null) {
			if (cacheLoader == null) {
				return null;
			}
			try {
				T obj = cacheLoader.load(key);
				setToCache(key, obj);
				return obj;
			} catch (Exception e) {
				logger.error("{}",e);
				return null;
			}
		} else {
			return o;
		}
	}

	@Override
	public void setToCache(Object key, T value) {
		redisTemplate.opsForValue().set(keyPrefix + key, value, expireTimeSeconds, TimeUnit.SECONDS);
	}

	@Override
	public long size() {
		return -1;
	}

	@Override
	public boolean exist(Object key) {
		return redisTemplate.hasKey(keyPrefix + key);
	}

	@Override
	@Deprecated
	public Collection<?> all() {
		return null;
	}

	@Override
	@Deprecated
	public Map<Object, T> mapAll() {
		return null;
	}

	@Override
	public void remove(Object key) {
		redisTemplate.delete(keyPrefix + key);
		
	}
	@Override
	public void clean() {
		redisTemplate.delete("*");
	}

}
