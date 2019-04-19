package org.smart.framework.net.handler;

import java.util.Collection;

import org.smart.framework.net.ext.LogoutLinstener;
import org.smart.framework.net.protocol.DataPacket;
import org.smart.framework.net.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class DefaultHandler extends ChannelInboundHandlerAdapter {
	protected Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	private Router<DataPacket> router;
	
	@SuppressWarnings("unchecked")
	public DefaultHandler(Router<? extends DataPacket> router,Collection<LogoutLinstener> logouts) {
		if (router == null) {
			throw new RuntimeException("router is null!");
		}
		this.router = (Router<DataPacket>) router;
	}
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
	}
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
	}
	
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        DataPacket dataPacket = (DataPacket) msg;
        boolean flag = router.forwardValidate(ctx, dataPacket);
        if (flag) {
        	router.forward(ctx, dataPacket);
        } 
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        cause.printStackTrace();
        ctx.close();
    }
}
