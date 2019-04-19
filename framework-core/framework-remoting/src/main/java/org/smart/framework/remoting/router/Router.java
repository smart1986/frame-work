package org.smart.framework.remoting.router;

import org.smart.framework.remoting.protocol.RemotingCommand;

import io.netty.channel.ChannelHandlerContext;

public interface Router<T> {
	
	public void register(Object handler);
	
	public boolean forwardValidate(ChannelHandlerContext ctx, T request,int messageIndex);
	
	/**
     * 路由转发
     */
    public RemotingCommand forward(ChannelHandlerContext ctx, T request, int messageIndex);
    
    public T newInstance();
}
