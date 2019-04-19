package org.smart.framework.remoting.client;

import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.smart.framework.net.callback.InitClientCallBack;
import org.smart.framework.remoting.ChannelEventListener;
import org.smart.framework.remoting.InvokeCallback;
import org.smart.framework.remoting.common.RemotingHelper;
import org.smart.framework.remoting.exception.RemotingException;
import org.smart.framework.remoting.netty.NettyClientConfig;
import org.smart.framework.remoting.netty.NettyRemotingClient;
import org.smart.framework.remoting.netty.NettyRequestProcessor;
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
 * @param <T>
 *            请求参数类型
 * @param <R>
 *            路由参数类型
 */
public class TcpClient<T extends DataPacket<?>, R extends DataPacket<?>> {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected NettyRemotingClient client;

	private int clientId;

	private final ScheduledExecutorService heartBeatTimer = new ScheduledThreadPoolExecutor(1,
			new ThreadFactoryImpl("ClientHeartService"));
	protected NettyClientConfig nettyClientConfig;

	public TcpClient() {

	}

	public boolean registerClient() throws RemotingException, InterruptedException {
		RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.REGIST_CLIENT);
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(this.clientId);
		buffer.flip();
		request.setBody(buffer.array());
		RemotingCommand response = this.client.invokeSync(request,
				nettyClientConfig.getNettyCleintInvokeTimeOut());
		return response.getCode() == RemotingSysResponseCode.SUCCESS;
	}

	public boolean heartbeat() throws RemotingException, InterruptedException {
		RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.HEART_BEAT);
		RemotingCommand response = this.client.invokeSync(request,
				nettyClientConfig.getNettyCleintInvokeTimeOut());
		return response.getCode() == RemotingSysResponseCode.SUCCESS;
	}

	public void init(int clientId, String remoteHost, int remotePort,NettyClientConfig nettyClientConfig, Router<R> requestRouter) {
		init(clientId, remoteHost, remotePort, nettyClientConfig, requestRouter,null);
	}

	protected void onChannelClose() {
	}

	public void init(int clientId, String remoteHost,int remotePort,NettyClientConfig nettyClientConfig, Router<R> requestRouter,
			InitClientCallBack initClientCallBack) {
		this.clientId = clientId;
		this.nettyClientConfig = nettyClientConfig;
		client = new NettyRemotingClient(remoteHost + ":" + remotePort, nettyClientConfig,
				new ChannelEventListener() {

					@Override
					public void onChannelIdle(String remoteAddr, Channel channel) {

					}

					@Override
					public void onChannelException(String remoteAddr, Channel channel) {

					}

					@Override
					public void onChannelConnect(String remoteAddr, Channel channel) {
						
					}

					@Override
					public void onChannelClose(String remoteAddr, Channel channel) {
						TcpClient.this.onChannelClose();
					}

					@Override
					public void onActive(String remoteAddr, Channel channel) {
						try {
							boolean flag = false;
							flag = registerClient();
							if (flag) {
								logger.info("client regist success,clientId:{}", clientId);
								TcpClient.this.heartBeatTimer.scheduleAtFixedRate(new Runnable() {
									@Override
									public void run() {
										try {
											while (true) {

												boolean flag = TcpClient.this.heartbeat();
												if (flag) {
													break;
												}
											}
										} catch (Throwable e) {
											logger.error("heartbeat exception", e);
										}
									}
								}, 60000,60000, TimeUnit.MILLISECONDS);
								if (initClientCallBack != null) {
									initClientCallBack.clientIntiComplete();
								}
							}
						} catch (RemotingException | InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							// RemotingUtil.closeChannel(channel);
							logger.info("client regist fail,clientId:{}", clientId);
							System.exit(0);
						}

					}
				});
		if (requestRouter != null) {
			client.registerProcessor(RequestCode.SEND_MESSAGE, new NettyRequestProcessor() {

				@Override
				public boolean rejectRequest() {
					return false;
				}

				@Override
				public RemotingCommand processRequest(ChannelHandlerContext ctx,
						RemotingCommand request) throws Exception {
					RemotingCommand response = null;
					R dataPacket;
					try {
						dataPacket = requestRouter.newInstance();
						dataPacket.decode(request.getBody());
						boolean flag = requestRouter.forwardValidate(ctx, dataPacket,
								request.getCmdIndex());
						if (flag) {
							response = requestRouter.forward(ctx, dataPacket,
									request.getCmdIndex());
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
			}, null);
		}

		client.start();
	}

	protected RemotingCommand sendMessageSync(T dataPacket) throws Exception {
		RemotingCommand remotingCommand = RemotingCommand.createRequestCommand();
		remotingCommand.setBody(dataPacket.encode());
		logger.debug("message send:{}, module:{},cmd:{},length:{}",
				RemotingHelper.parseChannelRemoteAddr(client.getChannel()), dataPacket.getModule(),
				dataPacket.getCmd(),
				dataPacket.getValue() == null ? 0 : dataPacket.getValue().length);
		return client.invokeSync(remotingCommand, nettyClientConfig.getNettyCleintInvokeTimeOut());
	}

	protected void sendMessageAsync(T dataPacket, InvokeCallback invokeCallback) throws Exception {
		RemotingCommand remotingCommand = RemotingCommand.createRequestCommand();
		remotingCommand.setBody(dataPacket.encode());
		logger.debug("message send:{}, module:{},cmd:{},length:{}",
				RemotingHelper.parseChannelRemoteAddr(client.getChannel()), dataPacket.getModule(),
				dataPacket.getCmd(),
				dataPacket.getValue() == null ? 0 : dataPacket.getValue().length);
		client.invokeAsync(remotingCommand, nettyClientConfig.getNettyCleintInvokeTimeOut(),
				invokeCallback);
	}

}
