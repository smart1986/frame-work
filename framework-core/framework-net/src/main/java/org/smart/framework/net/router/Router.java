package org.smart.framework.net.router;

import io.netty.channel.ChannelHandlerContext;

public interface Router<T> {
	
	void register(Object handler);
	
	boolean forwardValidate(ChannelHandlerContext ctx, T request);
	
	/**
     * 路由转发
     */
    Object forward(ChannelHandlerContext ctx, T request);
}
