package org.smart.framework.datacenter.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * googlecache 框架内实现
 * @param <T>
 */
public class GoogleCacheImpl<T> implements DataCache<T> {

	protected Logger LOGGER = LoggerFactory.getLogger(getClass());

	private Cache<Object, T> cache;

	public GoogleCacheImpl(int expireTimeSeconds, long maxCacheSize) {
		this(expireTimeSeconds,maxCacheSize, false);
	}
	public GoogleCacheImpl(int expireTimeSeconds, long maxCacheSize,boolean maintain) {
		this(null,expireTimeSeconds,maxCacheSize, maintain);
	}
	public GoogleCacheImpl(CacheLoader<Object, T> cacheLoader, int expireTimeSeconds, long maxCacheSize) {
		this(cacheLoader,expireTimeSeconds,maxCacheSize, false);
	}
	public GoogleCacheImpl(CacheLoader<Object, T> cacheLoader, int expireTimeSeconds, long maxCacheSize, boolean maintain) {
		if (cacheLoader == null) {
			cache = CacheBuilder.newBuilder().expireAfterAccess(expireTimeSeconds, TimeUnit.SECONDS)
					.maximumSize(maxCacheSize).build();
		} else {
			cache = CacheBuilder.newBuilder().expireAfterAccess(expireTimeSeconds, TimeUnit.SECONDS)
					.maximumSize(maxCacheSize).build(cacheLoader);
		}
		if (maintain) {
			CacheConst.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
				
				@Override
				public void run() {
					cache.cleanUp();
				}
			}, 0, 1, TimeUnit.MINUTES);
		}
		
	}


	@Override
	public T getFromCache(Object key) {
		try {
			if (cache instanceof LoadingCache) {
				LoadingCache<Object, T> loadingCache = (LoadingCache<Object, T>) cache;
				return loadingCache.get(key);
			} else {
				return cache.getIfPresent(key);
			}
		} catch (ExecutionException e) {
			LOGGER.error("{}", e);
		}
		return null;
	}

	@Override
	public void setToCache(Object key, T value) {
		cache.put(key, value);
	}

	@Override
	public long size() {
		return cache.size();
	}

	@Override
	public boolean exist(Object key) {
		return cache.asMap().containsKey(key);
	}
	@Override
	public Collection<T> all() {
		return cache.asMap().values();
	}

	@Override
	public Map<Object, T> mapAll(){
		return cache.asMap();
	}

	@Override
	public void remove(Object key) {
		cache.invalidate(key);
	}
	@Override
	public void clean() {
		cache.invalidateAll();
	}
}
