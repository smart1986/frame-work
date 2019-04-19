package org.smart.framework.net.constant;

import org.smart.framework.util.NamedThreadFactory;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public interface NetConst {
	/**
	 * 包头标识
	 */
	int HEADER_FLAG = -1860108940;
	
	NamedThreadFactory HTTP_THREAD_FACTORY = new NamedThreadFactory("admin http thread");
	NamedThreadFactory LOGIC_THREAD_FACTORY = new NamedThreadFactory("logic thread");
	EventExecutorGroup HTTP_EVENT_EXECUTOR_GROUP = new DefaultEventExecutorGroup(1,HTTP_THREAD_FACTORY);
	EventExecutorGroup LOGIC_EVENT_EXECUTOR_GROUP = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2,LOGIC_THREAD_FACTORY);
	
}
