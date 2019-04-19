package org.smart.framework.remoting.netty;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.smart.framework.remoting.ChannelEventListener;
import org.smart.framework.remoting.InvokeCallback;
import org.smart.framework.remoting.RemotingClient;
import org.smart.framework.remoting.common.Pair;
import org.smart.framework.remoting.common.RemotingHelper;
import org.smart.framework.remoting.common.RemotingUtil;
import org.smart.framework.remoting.exception.RemotingConnectException;
import org.smart.framework.remoting.exception.RemotingSendRequestException;
import org.smart.framework.remoting.exception.RemotingTimeoutException;
import org.smart.framework.remoting.exception.RemotingTooMuchRequestException;
import org.smart.framework.remoting.protocol.RemotingCommand;
import org.smart.framework.util.ThreadFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

public class NettyRemotingClient extends NettyRemotingAbstract implements RemotingClient {
	private static final Logger log = LoggerFactory.getLogger(NettyRemotingClient.class);

	private static final long LOCK_TIMEOUT_MILLIS = 3000;

	private final NettyClientConfig nettyClientConfig;
	private final Bootstrap bootstrap = new Bootstrap();
	private final EventLoopGroup eventLoopGroupWorker;
	private final Lock lockChannelTables = new ReentrantLock();
	private final ConcurrentMap<String /* addr */, ChannelWrapper> channelTables = new ConcurrentHashMap<String, ChannelWrapper>();

	private final ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1,
			new ThreadFactoryImpl("ClientHouseKeepingService"));

	// private final AtomicReference<List<String>> namesrvAddrList = new
	// AtomicReference<List<String>>();
	// private final AtomicReference<String> namesrvAddrChoosed = new
	// AtomicReference<String>();
	// private final AtomicInteger namesrvIndex = new
	// AtomicInteger(initValueIndex());
	// private final Lock lockNamesrvChannel = new ReentrantLock();

	private final ExecutorService publicExecutor;

	/**
	 * Invoke the callback methods in this executor when process response.
	 */
	private ExecutorService callbackExecutor;
	private DefaultEventExecutorGroup defaultEventExecutorGroup;

	private final ChannelEventListener channelEventListener;

	protected final ConcurrentMap<Integer /* cmdIndex */, ResponseFuture> responseTable = new ConcurrentHashMap<Integer, ResponseFuture>(
			256);

	private Channel channel;
	private String remotingAddress = "127.0.0.1:8888";

	public NettyRemotingClient(final String remotingAddress,
			final NettyClientConfig nettyClientConfig, ChannelEventListener channelEventListener) {
		super(nettyClientConfig.getClientOnewaySemaphoreValue());
		this.remotingAddress = remotingAddress;
		this.nettyClientConfig = nettyClientConfig;
		this.channelEventListener = channelEventListener;

		int publicThreadNums = nettyClientConfig.getClientCallbackExecutorThreads();
		if (publicThreadNums <= 0) {
			publicThreadNums = 4;
		}

		this.publicExecutor = Executors.newFixedThreadPool(publicThreadNums, new ThreadFactory() {
			private AtomicInteger threadIndex = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r,
						"NettyClientPublicExecutor_" + this.threadIndex.incrementAndGet());
			}
		});

		this.eventLoopGroupWorker = new NioEventLoopGroup(1, new ThreadFactory() {
			private AtomicInteger threadIndex = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, String.format("NettyClientSelector_%d",
						this.threadIndex.incrementAndGet()));
			}
		});

	}

	// private static int initValueIndex() {
	// Random r = new Random();
	//
	// return Math.abs(r.nextInt() % 999) % 999;
	// }

	@Override
	public void start() {
		this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
				nettyClientConfig.getClientWorkerThreads(), new ThreadFactory() {

					private AtomicInteger threadIndex = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r,
								"NettyClientWorkerThread_" + this.threadIndex.incrementAndGet());
					}
				});

		this.bootstrap.group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, false)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
						nettyClientConfig.getConnectTimeoutMillis())
				.option(ChannelOption.SO_SNDBUF, nettyClientConfig.getClientSocketSndBufSize())
				.option(ChannelOption.SO_RCVBUF, nettyClientConfig.getClientSocketRcvBufSize())
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipeline = ch.pipeline();

						pipeline.addFirst(new ChannelInboundHandlerAdapter() {
							@Override
							public void channelInactive(ChannelHandlerContext ctx)
									throws Exception {
								super.channelInactive(ctx);
								ctx.channel().eventLoop().schedule(() -> doConnect(), 1,
										TimeUnit.SECONDS);
							}

							@Override
							public void channelActive(ChannelHandlerContext ctx) throws Exception {
								super.channelActive(ctx);
								if (NettyRemotingClient.this.channelEventListener != null) {
									final String remoteAddress = RemotingHelper
											.parseChannelRemoteAddr(ctx.channel());
									NettyRemotingClient.this.putNettyEvent(new NettyEvent(
											NettyEventType.ACTIVE, remoteAddress, ctx.channel()));
								}
							}

						});

						pipeline.addLast(defaultEventExecutorGroup, new NettyEncoder(),
								new NettyDecoder(),
								new IdleStateHandler(0, 0,
										nettyClientConfig.getClientChannelMaxIdleTimeSeconds()),
								new NettyConnectManageHandler(), new NettyClientHandler());
					}
				});

		doConnect();
		this.timer.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					NettyRemotingClient.this.scanResponseTable();
				} catch (Throwable e) {
					log.error("scanResponseTable exception", e);
				}
			}
		}, 1000 * 3, 1000, TimeUnit.MILLISECONDS);
		// this.heartBeatTimer.scheduleAtFixedRate(new TimerTask() {
		// @Override
		// public void run() {
		// try {
		// while(true) {
		//
		// boolean flag = NettyRemotingClient.this.doHeartBeat();
		// if (flag) break;
		// }
		// } catch (Throwable e) {
		// log.error("heartbeat exception", e);
		// }
		// }
		// }, 1000, 60000);
		if (this.channelEventListener != null) {
			this.nettyEventExecutor.start();
		}

	}

	// private boolean registClient() {
	// if (channel != null && channel.isActive()) {
	// RemotingCommand remotingCommand =
	// RemotingCommand.createRequestCommand(RequestCode.REGIST_CLIENT);
	// ByteBuffer buffer = ByteBuffer.allocate(4);
	// buffer.putInt(this.clientId);
	// buffer.flip();
	// remotingCommand.setBody(buffer.array());
	// try {
	//// invokeAsync(remotingCommand, 2000L, new InvokeCallback() {
	////
	//// @Override
	//// public void operationComplete(ResponseFuture responseFuture) {
	//// if (responseFuture.getResponseCommand().getCode() ==
	// RemotingSysResponseCode.REGIST_CLIENT_FAIL) {
	//// log.error("client regist fail,clientId:{}", clientId);
	//// channel.close();
	//// } else {
	//// log.info("client regist success,clientId:{}", clientId);
	//// }
	//// }
	//// });
	// RemotingCommand response = invokeSync(remotingCommand, 2000L);
	// if (response.getCode() == RemotingSysResponseCode.REGIST_CLIENT_FAIL) {
	// log.error("client regist fail,clientId:{}", clientId);
	// channel.close();
	// return false;
	// }
	// log.info("client regist success,clientId:{}", clientId);
	// return true;
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// log.error("client regist fail,clientId:{}", clientId);
	// return false;
	// }
	// private boolean doHeartBeat() {
	// if (channel != null && channel.isActive()) {
	// RemotingCommand remotingCommand =
	// RemotingCommand.createRequestCommand(RequestCode.HEART_BEAT);
	// try {
	// invokeAsync(remotingCommand, 2000L, null);
	// return true;
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// return false;
	// }

	private void doConnect() {
		ChannelFuture future = bootstrap
				.connect(RemotingHelper.string2SocketAddress(this.remotingAddress));

		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture f) throws Exception {
				if (f.isSuccess()) {
					channel = f.channel();
				} else {
					f.channel().eventLoop().schedule(() -> doConnect(), 1, TimeUnit.SECONDS);
				}
			}
		});
	}

	@Override
	public void shutdown() {
		try {
			this.timer.shutdown();

			for (ChannelWrapper cw : this.channelTables.values()) {
				this.closeChannel(cw.getChannel());
			}

			this.channelTables.clear();

			this.eventLoopGroupWorker.shutdownGracefully();

			if (this.nettyEventExecutor != null) {
				this.nettyEventExecutor.shutdown();
			}

			if (this.defaultEventExecutorGroup != null) {
				this.defaultEventExecutorGroup.shutdownGracefully();
			}
		} catch (Exception e) {
			log.error("NettyRemotingClient shutdown exception, ", e);
		}

		if (this.publicExecutor != null) {
			try {
				this.publicExecutor.shutdown();
			} catch (Exception e) {
				log.error("NettyRemotingServer shutdown exception, ", e);
			}
		}
	}

	public void closeChannel(final Channel channel) {
		if (null == channel) {
			return;
		}

		try {
			if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
				try {
					boolean removeItemFromTable = true;
					ChannelWrapper prevCW = null;
					String addrRemote = null;
					for (Map.Entry<String, ChannelWrapper> entry : channelTables.entrySet()) {
						String key = entry.getKey();
						ChannelWrapper prev = entry.getValue();
						if (prev.getChannel() != null) {
							if (prev.getChannel() == channel) {
								prevCW = prev;
								addrRemote = key;
								break;
							}
						}
					}

					if (null == prevCW) {
						log.info(
								"eventCloseChannel: the channel[{}] has been removed from the channel table before",
								addrRemote);
						removeItemFromTable = false;
					}

					if (removeItemFromTable) {
						this.channelTables.remove(addrRemote);
						log.info("closeChannel: the channel[{}] was removed from channel table",
								addrRemote);
						RemotingUtil.closeChannel(channel);
					}
				} catch (Exception e) {
					log.error("closeChannel: close the channel exception", e);
				} finally {
					this.lockChannelTables.unlock();
				}
			} else {
				log.warn("closeChannel: try to lock channel table, but timeout, {}ms",
						LOCK_TIMEOUT_MILLIS);
			}
		} catch (InterruptedException e) {
			log.error("closeChannel exception", e);
		}
	}

	@Override
	public RemotingCommand invokeSync(final RemotingCommand request, long timeoutMillis)
			throws InterruptedException, RemotingConnectException, RemotingSendRequestException,
			RemotingTimeoutException {
		final Channel channel = this.channel;
		if (channel != null && channel.isActive()) {
			try {
				RemotingCommand response = this.invokeSyncImpl(channel, request, timeoutMillis);
				return response;
			} catch (RemotingSendRequestException e) {
				log.warn("invokeSync: send request exception, so close the channel[{}]",
						remotingAddress);
				this.closeChannel(channel);
				throw e;
			} catch (RemotingTimeoutException e) {
				if (nettyClientConfig.isClientCloseSocketIfTimeout()) {
					this.closeChannel(channel);
					log.warn("invokeSync: close socket because of timeout, {}ms, {}", timeoutMillis,
							remotingAddress);
				}
				log.warn("invokeSync: wait response timeout exception, the channel[{}]",
						remotingAddress);
				throw e;
			}
		} else {
			this.closeChannel(channel);
			throw new RemotingConnectException(remotingAddress);
		}
	}

	@Override
	public void invokeAsync(RemotingCommand request, long timeoutMillis,
			InvokeCallback invokeCallback)
			throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
			RemotingTimeoutException, RemotingSendRequestException {
		final Channel channel = this.channel;
		if (channel != null && channel.isActive()) {
			try {
				this.invokeAsyncImpl(channel, request, timeoutMillis, invokeCallback);
			} catch (RemotingSendRequestException e) {
				log.warn("invokeAsync: send request exception, so close the channel[{}]",
						remotingAddress);
				this.closeChannel(channel);
				throw e;
			}
		} else {
			this.closeChannel(channel);
			throw new RemotingConnectException(remotingAddress);
		}
	}

	@Override
	public ExecutorService getCallbackExecutor() {
		return callbackExecutor != null ? callbackExecutor : publicExecutor;
	}

	@Override
	public void setCallbackExecutor(final ExecutorService callbackExecutor) {
		this.callbackExecutor = callbackExecutor;
	}

	static class ChannelWrapper {
		private final ChannelFuture channelFuture;

		public ChannelWrapper(ChannelFuture channelFuture) {
			this.channelFuture = channelFuture;
		}

		public boolean isOK() {
			return this.channelFuture.channel() != null && this.channelFuture.channel().isActive();
		}

		public boolean isWritable() {
			return this.channelFuture.channel().isWritable();
		}

		private Channel getChannel() {
			return this.channelFuture.channel();
		}

		public ChannelFuture getChannelFuture() {
			return channelFuture;
		}
	}

	class NettyClientHandler extends SimpleChannelInboundHandler<RemotingCommand> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg)
				throws Exception {
			processMessageReceived(ctx, msg);
		}
	}

	class NettyConnectManageHandler extends ChannelDuplexHandler {
		@Override
		public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
				SocketAddress localAddress, ChannelPromise promise) throws Exception {
			final String local = localAddress == null ? "UNKNOWN"
					: RemotingHelper.parseSocketAddressAddr(localAddress);
			final String remote = remoteAddress == null ? "UNKNOWN"
					: RemotingHelper.parseSocketAddressAddr(remoteAddress);
			log.info("NETTY CLIENT PIPELINE: CONNECT  {} => {}", local, remote);

			super.connect(ctx, remoteAddress, localAddress, promise);

			if (NettyRemotingClient.this.channelEventListener != null) {
				NettyRemotingClient.this.putNettyEvent(
						new NettyEvent(NettyEventType.CONNECT, remote, ctx.channel()));
			}
		}

		@Override
		public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
			log.info("NETTY CLIENT PIPELINE: DISCONNECT {}", remoteAddress);
			closeChannel(ctx.channel());
			super.disconnect(ctx, promise);

			if (NettyRemotingClient.this.channelEventListener != null) {
				NettyRemotingClient.this.putNettyEvent(
						new NettyEvent(NettyEventType.CLOSE, remoteAddress, ctx.channel()));
			}
		}

		@Override
		public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
			log.info("NETTY CLIENT PIPELINE: CLOSE {}", remoteAddress);
			closeChannel(ctx.channel());
			super.close(ctx, promise);

			if (NettyRemotingClient.this.channelEventListener != null) {
				NettyRemotingClient.this.putNettyEvent(
						new NettyEvent(NettyEventType.CLOSE, remoteAddress, ctx.channel()));
			}
		}

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			if (evt instanceof IdleStateEvent) {
				IdleStateEvent event = (IdleStateEvent) evt;
				if (event.state().equals(IdleState.ALL_IDLE)) {
					final String remoteAddress = RemotingHelper
							.parseChannelRemoteAddr(ctx.channel());
					log.warn("NETTY CLIENT PIPELINE: IDLE exception [{}]", remoteAddress);
					closeChannel(ctx.channel());
					if (NettyRemotingClient.this.channelEventListener != null) {
						NettyRemotingClient.this.putNettyEvent(
								new NettyEvent(NettyEventType.IDLE, remoteAddress, ctx.channel()));
					}
				}
			}

			ctx.fireUserEventTriggered(evt);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
			log.warn("NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress);
			log.warn("NETTY CLIENT PIPELINE: exceptionCaught exception.", cause);
			closeChannel(ctx.channel());
			if (NettyRemotingClient.this.channelEventListener != null) {
				NettyRemotingClient.this.putNettyEvent(
						new NettyEvent(NettyEventType.EXCEPTION, remoteAddress, ctx.channel()));
			}
		}
	}

	@Override
	public ChannelEventListener getChannelEventListener() {
		return channelEventListener;
	}

	@Override
	public void registerProcessor(short requestCode, NettyRequestProcessor processor,
			ExecutorService executor) {
		ExecutorService executorThis = executor;
		if (null == executor) {
			executorThis = this.publicExecutor;
		}

		Pair<NettyRequestProcessor, ExecutorService> pair = new Pair<NettyRequestProcessor, ExecutorService>(
				processor, executorThis);
		this.processorTable.put(requestCode, pair);
	}

	public Channel getChannel() {
		return channel;
	}
}
