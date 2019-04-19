package org.smart.framework.datacenter.cache;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.smart.framework.util.ThreadFactoryImpl;

public class CacheConst {
	public static ScheduledExecutorService scheduledExecutorService  = Executors.newScheduledThreadPool(1, new ThreadFactoryImpl("cache_maintain_"));
	public static int localCacheSeconds = 5;
}
