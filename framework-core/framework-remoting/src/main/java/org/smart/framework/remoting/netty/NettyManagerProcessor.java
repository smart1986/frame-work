package org.smart.framework.remoting.netty;

import java.nio.ByteBuffer;

import org.smart.framework.remoting.protocol.RemotingCommand;
import org.smart.framework.remoting.protocol.RemotingSysResponseCode;
import org.smart.framework.remoting.protocol.RequestCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class NettyManagerProcessor implements NettyRequestProcessor {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
		switch (request.getCode()) {
		case RequestCode.HEART_BEAT:
			return this.heartBeat(ctx, request);
		case RequestCode.REGIST_CLIENT:
			return this.registClient(ctx, request);
		default:
			break;
		}
		return null;
	}
	
	private RemotingCommand heartBeat(ChannelHandlerContext ctx, RemotingCommand request) {
		RemotingCommand response = RemotingCommand.createResponseCommand(request.getCmdIndex());
		return response;
	}
	private RemotingCommand registClient(ChannelHandlerContext ctx, RemotingCommand request) {
		ByteBuffer buff = ByteBuffer.wrap(request.getBody());
		int clientId = buff.getInt();
		Channel c = NettyServerChannelHolder.findChannel(clientId);
		RemotingCommand response = RemotingCommand.createResponseCommand(request.getCmdIndex());
		if (c != null) {
			response.setCode(RemotingSysResponseCode.SYSTEM_ERROR);
			logger.error("remote client regist fail, clientId:{}",clientId);
		} else {
			NettyServerChannelHolder.regist(clientId, ctx.channel());
			logger.info("remote client regist success, clientId:{}",clientId);
		}
		return response;
	}

	@Override
	public boolean rejectRequest() {
		return false;
	}

}
