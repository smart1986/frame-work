package org.smart.framework.net.router;

import io.netty.channel.ChannelHandlerContext;

public interface Router<T> {
	
	public void register(Object handler);
	
	public boolean forwardValidate(ChannelHandlerContext ctx, T request);
	
	/**
     * 路由转发
     */
    public  Object forward(ChannelHandlerContext ctx, T request);
}
