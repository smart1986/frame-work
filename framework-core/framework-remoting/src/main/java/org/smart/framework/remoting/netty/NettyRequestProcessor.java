package org.smart.framework.remoting.netty;

import org.smart.framework.remoting.protocol.RemotingCommand;

import io.netty.channel.ChannelHandlerContext;

/**
 * Common remoting command processor
 */
public interface NettyRequestProcessor {
    RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
        throws Exception;

    boolean rejectRequest();
}
