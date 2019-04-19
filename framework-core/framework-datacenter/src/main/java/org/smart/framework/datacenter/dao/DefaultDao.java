package org.smart.framework.datacenter.dao;

import javax.annotation.PostConstruct;

import org.smart.framework.datacenter.BaseJdbcTemplate;
import org.smart.framework.datacenter.DBQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.google.common.cache.CacheLoader;

public abstract class DefaultDao<T> extends CacheLoader<Object, T> implements BaseDao{
	
	protected  Logger LOGGER = LoggerFactory.getLogger(getClass());
	@Autowired
	@Qualifier("jdbcTemplate")
	protected BaseJdbcTemplate jdbc;
	@Autowired
	protected DBQueue dbQueue;
	@Autowired(required=false)
	@Qualifier("cache.time")
	protected Integer cacheTime = 900;
	
	@Autowired(required=false)
	@Qualifier("cache.size")
	protected Long cacheSize = 100000L;
	@Autowired(required=false)
	@Qualifier("cache.maintain")
	protected Boolean cacheMaintain = false;
	
	@PostConstruct
	protected abstract void createCache() ;
	
	protected abstract T getFromCache(final Object key);
}
