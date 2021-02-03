package org.smart.framework.remoting.router.impl;


import java.lang.reflect.Method;

import org.smart.framework.net.router.RouterHandler;
import org.smart.framework.net.router.annotation.Cmd;
import org.smart.framework.remoting.protocol.BaseDataPacket;
import org.smart.framework.remoting.protocol.RemotingCommand;
import org.smart.framework.remoting.protocol.Response;
import org.smart.framework.remoting.protocol.StatusCode;
import org.smart.framework.remoting.router.SocketRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.reflectasm.MethodAccess;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

/**
 * 转发器实现
 * @author smart
 *
 */
public class ForwardSocketRouterImpl extends SocketRouter {
	protected Logger LOGGER = LoggerFactory.getLogger(getClass());
	@Override
	public RemotingCommand forward(ChannelHandlerContext ctx, BaseDataPacket dataPacket, int messageIndex) {
		RemotingCommand remotingCommand = RemotingCommand.createResponseCommand(messageIndex);
		byte module = dataPacket.getModule();
		byte cmd = dataPacket.getCmd();
		RouterHandler handler = MODULE_MAPS.get(module);
		Method method = handler.getMethod(cmd);
		MethodAccess methodAccess = handler.getMethodAccess();
		final Response response = Response.valueOf(dataPacket.getModule(), dataPacket.getCmd(),dataPacket.getValue());
		if (handler == null || method == null) {
			response.setStatusCode(StatusCode.MODULE_NOT_FOUND);
			remotingCommand.setBody(response.encode());
			return remotingCommand;
		}
		
		
		
		//增加  根据cmd标注判断 帐号是否登陆，角色是否登陆.
		Cmd annotation = handler.getCmd(dataPacket.getCmd());
		if(annotation.check()) {
			if (!check(ctx.channel())){
				response.setStatusCode(StatusCode.FORBIDEN);
				remotingCommand.setBody(response.encode());
				return remotingCommand;
			}
		}
		
		try {
			//Object result = method.invoke(handler.getHander(), channel, dataPacket.getValue(), response);
			Object result =  methodAccess.invoke(handler.getHandler(),method.getName(), ctx.channel(), dataPacket.getValue(), response);
			remotingCommand.setBody(response.encode());
			ChannelFuture f = ctx.writeAndFlush(remotingCommand);
			f.addListener(new ChannelFutureListener() {
				
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					LOGGER.debug("message send, module:{},cmd:{},length:{}",response.getModule(), response.getCmd(),response.getValue().length);
					
				}
			});
			ChannelFutureListener future = null;
			if (result instanceof ChannelFutureListener){
				future = (ChannelFutureListener) result;
			}
			if (future != null) {
				f.addListener(future);
			}
			
			
		} catch (Exception exception) {
			exception.printStackTrace();
			LOGGER.error("[method.invoke] methodName:{}, error:{}", method.getName(), exception.getCause());
		}
		
		return null;
	}
	
	protected boolean check(Channel channel){
		return true;
	}
	
	@Override
	public BaseDataPacket newInstance() {
		return new BaseDataPacket();
	}

}
