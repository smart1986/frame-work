package org.smart.framework.remoting.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.smart.framework.remoting.ChannelEventListener;
import org.smart.framework.remoting.InvokeCallback;
import org.smart.framework.remoting.netty.NettyManagerProcessor;
import org.smart.framework.remoting.netty.NettyRemotingServer;
import org.smart.framework.remoting.netty.NettyRequestProcessor;
import org.smart.framework.remoting.netty.NettyServerChannelHolder;
import org.smart.framework.remoting.netty.NettyServerConfig;
import org.smart.framework.remoting.protocol.DataPacket;
import org.smart.framework.remoting.protocol.RemotingCommand;
import org.smart.framework.remoting.protocol.RemotingSysResponseCode;
import org.smart.framework.remoting.protocol.RequestCode;
import org.smart.framework.remoting.router.Router;
import org.smart.framework.util.ThreadFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * @author 卢德迪
 *
 * @param <Q> call客户端参数类型
 * @param <R> 路由参数类型
 */
public class TcpServer<Q extends DataPacket<?>,R  extends DataPacket<?>> {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	protected NettyRemotingServer nettyRemotingServer;
	
	protected NettyServerConfig nettyServerConfig;
	private ExecutorService sendMessageExecutor = new ThreadPoolExecutor(1, 1, 1000 * 60, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(10000), new ThreadFactoryImpl("SendMessageThread_"));
	private ExecutorService mangerExecutor = new ThreadPoolExecutor(1, 1, 1000 * 60, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(10000), new ThreadFactoryImpl("ManageThread_"));

	protected Router<Q> requestRouter;
	protected NettyRequestProcessor sendMessageProcessor(){
		return new NettyRequestProcessor() {

			@Override
			public boolean rejectRequest() {
				return false;
			}

			@Override
			public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
				RemotingCommand response = null;
				
				try {
					Q dataPacket = requestRouter.newInstance();
					dataPacket.decode(request.getBody());
					boolean flag = requestRouter.forwardValidate(ctx, dataPacket, request.getCmdIndex());
					if (flag) {
						response = requestRouter.forward(ctx, dataPacket, request.getCmdIndex());
					} else {
						response = RemotingCommand.createResponseCommand(request.getCmdIndex());
					}
				} catch (Exception e) {
					e.printStackTrace();
					response = RemotingCommand.createResponseCommand(request.getCmdIndex());
					response.setCode(RemotingSysResponseCode.SYSTEM_ERROR);
				}
				return response;
			}
		};
	}
	public void start(String serverName, int port, NettyServerConfig nettyServerConfig, Router<Q> requestRouter, ChannelEventListener channelEventListener) {
		this.requestRouter = requestRouter;
		this.nettyServerConfig = nettyServerConfig;
		nettyRemotingServer = new NettyRemotingServer(port,nettyServerConfig, channelEventListener);
		nettyRemotingServer.registerProcessor(RequestCode.HEART_BEAT, new NettyManagerProcessor(), mangerExecutor);
		nettyRemotingServer.registerProcessor(RequestCode.REGIST_CLIENT, new NettyManagerProcessor(), mangerExecutor);
		nettyRemotingServer.registerProcessor(RequestCode.SEND_MESSAGE, sendMessageProcessor(), sendMessageExecutor);

		nettyRemotingServer.registerProcessor(RequestCode.FORWARD, new NettyRequestProcessor() {

			@Override
			public boolean rejectRequest() {
				return false;
			}

			@Override
			public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
				Channel channel = NettyServerChannelHolder.findChannel(request.getForward());
				if (channel == null) {
					RemotingCommand remotingCommand = RemotingCommand.createResponseCommand(request.getCmdIndex(),RemotingSysResponseCode.FORWARD_FAILED);
					return remotingCommand;
				}
				RemotingCommand r = RemotingCommand.createRequestCommand();
				r.setBody(request.getBody());
				RemotingCommand remotingCommand = RemotingCommand.createResponseCommand(request.getCmdIndex());
				RemotingCommand result = nettyRemotingServer.invokeSync(channel, r, nettyServerConfig.getServerInvokeTimeOut());
				remotingCommand.setBody(result.getBody());
				return remotingCommand;
			}
		}, sendMessageExecutor);
		nettyRemotingServer.start();
		logger.info("{} server bind port: {}", serverName, port);
	}

	public void stop() {
		nettyRemotingServer.shutdown();
	}
	
	public RemotingCommand callClientSync(Channel channel,R dataPacket) throws Exception{
		RemotingCommand remotingCommand = RemotingCommand.createRequestCommand();
        remotingCommand.setBody(dataPacket.encode());
		return nettyRemotingServer.invokeSync(channel, remotingCommand, nettyServerConfig.getServerInvokeTimeOut());
	}
	public void callClientASync(Channel channel, R dataPacket, InvokeCallback invokeCallback) throws Exception{
		RemotingCommand remotingCommand = RemotingCommand.createRequestCommand();
		remotingCommand.setBody(dataPacket.encode());
		nettyRemotingServer.invokeAsync(channel, remotingCommand, nettyServerConfig.getServerInvokeTimeOut(),invokeCallback);
	}
	
	public NettyRemotingServer getNettyRemotingServer() {
		return nettyRemotingServer;
	}

}
