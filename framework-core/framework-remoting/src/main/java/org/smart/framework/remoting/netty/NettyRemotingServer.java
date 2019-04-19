package org.smart.framework.remoting.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.smart.framework.remoting.ChannelEventListener;
import org.smart.framework.remoting.InvokeCallback;
import org.smart.framework.remoting.RemotingServer;
import org.smart.framework.remoting.common.Pair;
import org.smart.framework.remoting.common.RemotingHelper;
import org.smart.framework.remoting.common.RemotingUtil;
import org.smart.framework.remoting.exception.RemotingSendRequestException;
import org.smart.framework.remoting.exception.RemotingTimeoutException;
import org.smart.framework.remoting.exception.RemotingTooMuchRequestException;
import org.smart.framework.remoting.protocol.RemotingCommand;
import org.smart.framework.util.ThreadFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

public class NettyRemotingServer extends NettyRemotingAbstract implements RemotingServer {
	private static final Logger log = LoggerFactory.getLogger(NettyRemotingServer.class);
	private final ServerBootstrap serverBootstrap;
	private final EventLoopGroup eventLoopGroupSelector;
	private final EventLoopGroup eventLoopGroupBoss;
	private final NettyServerConfig nettyServerConfig;

	private final ExecutorService publicExecutor;

	private final ChannelEventListener channelEventListener;
	 private final ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1,
				new ThreadFactoryImpl("ClientHouseKeepingService"));
	private DefaultEventExecutorGroup defaultEventExecutorGroup;

	private int port = 8888;

	public NettyRemotingServer(final int port,final NettyServerConfig nettyServerConfig,final ChannelEventListener channelEventListener) {
		super(nettyServerConfig.getServerAsyncSemaphoreValue());
		this.serverBootstrap = new ServerBootstrap();
		this.port = port;
		this.nettyServerConfig = nettyServerConfig;
		this.channelEventListener = channelEventListener;

		int publicThreadNums = nettyServerConfig.getServerCallbackExecutorThreads();
		if (publicThreadNums <= 0) {
			publicThreadNums = 4;
		}

		this.publicExecutor = Executors.newFixedThreadPool(publicThreadNums, new ThreadFactory() {
			private AtomicInteger threadIndex = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "NettyServerPublicExecutor_" + this.threadIndex.incrementAndGet());
			}
		});

		this.eventLoopGroupBoss = new NioEventLoopGroup(1, new ThreadFactory() {
			private AtomicInteger threadIndex = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, String.format("NettyBoss_%d", this.threadIndex.incrementAndGet()));
			}
		});

		if (useEpoll()) {
			this.eventLoopGroupSelector = new EpollEventLoopGroup(nettyServerConfig.getServerSelectorThreads(),
					new ThreadFactory() {
						private AtomicInteger threadIndex = new AtomicInteger(0);
						private int threadTotal = nettyServerConfig.getServerSelectorThreads();

						@Override
						public Thread newThread(Runnable r) {
							return new Thread(r, String.format("NettyServerEPOLLSelector_%d_%d", threadTotal,
									this.threadIndex.incrementAndGet()));
						}
					});
		} else {
			this.eventLoopGroupSelector = new NioEventLoopGroup(nettyServerConfig.getServerSelectorThreads(),
					new ThreadFactory() {
						private AtomicInteger threadIndex = new AtomicInteger(0);
						private int threadTotal = nettyServerConfig.getServerSelectorThreads();

						@Override
						public Thread newThread(Runnable r) {
							return new Thread(r, String.format("NettyServerNIOSelector_%d_%d", threadTotal,
									this.threadIndex.incrementAndGet()));
						}
					});
		}

	}

	private boolean useEpoll() {
		return RemotingUtil.isLinuxPlatform() && nettyServerConfig.isUseEpollNativeSelector() && Epoll.isAvailable();
	}

	@Override
	public void start() {
		this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(nettyServerConfig.getServerWorkerThreads(),
				new ThreadFactory() {

					private AtomicInteger threadIndex = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "NettyServerCodecThread_" + this.threadIndex.incrementAndGet());
					}
				});

		ServerBootstrap childHandler = this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector)
				.channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 1024).option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.SO_KEEPALIVE, false).childOption(ChannelOption.TCP_NODELAY, true)
				.childOption(ChannelOption.SO_SNDBUF, nettyServerConfig.getServerSocketSndBufSize())
				.childOption(ChannelOption.SO_RCVBUF, nettyServerConfig.getServerSocketRcvBufSize())
				.localAddress(new InetSocketAddress(port))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(defaultEventExecutorGroup, new NettyEncoder(), new NettyDecoder(),
								new IdleStateHandler(0, 0, nettyServerConfig.getServerChannelMaxIdleTimeSeconds()),
								new NettyConnectManageHandler(), new NettyServerHandler());
					}
				});

		if (nettyServerConfig.isServerPooledByteBufAllocatorEnable()) {
			childHandler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		}

		try {
			ChannelFuture sync = this.serverBootstrap.bind().sync();
			InetSocketAddress addr = (InetSocketAddress) sync.channel().localAddress();
			this.port = addr.getPort();
		} catch (InterruptedException e1) {
			throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
		}

		this.timer.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					NettyRemotingServer.this.scanResponseTable();
				} catch (Throwable e) {
					log.error("scanResponseTable exception", e);
				}
			}
		}, 1000 * 3, 1000, TimeUnit.MILLISECONDS);

		if (this.channelEventListener != null) {
			this.nettyEventExecutor.start();
		}
	}

	@Override
	public void shutdown() {
		try {
			if (this.timer != null) {
				this.timer.shutdown();
			}

			this.eventLoopGroupBoss.shutdownGracefully();

			this.eventLoopGroupSelector.shutdownGracefully();

			if (this.nettyEventExecutor != null) {
				this.nettyEventExecutor.shutdown();
			}

			if (this.defaultEventExecutorGroup != null) {
				this.defaultEventExecutorGroup.shutdownGracefully();
			}
		} catch (Exception e) {
			log.error("NettyRemotingServer shutdown exception, ", e);
		}

		if (this.publicExecutor != null) {
			try {
				this.publicExecutor.shutdown();
			} catch (Exception e) {
				log.error("NettyRemotingServer shutdown exception, ", e);
			}
		}
	}

	@Override
	public int localListenPort() {
		return this.port;
	}

	@Override
	public RemotingCommand invokeSync(final Channel channel, final RemotingCommand request, final long timeoutMillis)
			throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException {
		return this.invokeSyncImpl(channel, request, timeoutMillis);
	}

	@Override
	public void invokeAsync(Channel channel, RemotingCommand request, long timeoutMillis, InvokeCallback invokeCallback)
			throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException,
			RemotingSendRequestException {
		this.invokeAsyncImpl(channel, request, timeoutMillis, invokeCallback);
	}

	@Override
	public ExecutorService getCallbackExecutor() {
		return this.publicExecutor;
	}

	// class HandshakeHandler extends SimpleChannelInboundHandler<ByteBuf> {
	//
	// private final TlsMode tlsMode;
	//
	// private static final byte HANDSHAKE_MAGIC_CODE = 0x16;
	//
	// HandshakeHandler(TlsMode tlsMode) {
	// this.tlsMode = tlsMode;
	// }
	//
	// @Override
	// protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws
	// Exception {
	//
	// // mark the current position so that we can peek the first byte to determine
	// if the content is starting with
	// // TLS handshake
	// msg.markReaderIndex();
	//
	// byte b = msg.getByte(0);
	//
	// if (b == HANDSHAKE_MAGIC_CODE) {
	// switch (tlsMode) {
	// case DISABLED:
	// ctx.close();
	// log.warn("Clients intend to establish a SSL connection while this server is
	// running in SSL disabled mode");
	// break;
	// case PERMISSIVE:
	// case ENFORCING:
	// if (null != sslContext) {
	// ctx.pipeline()
	// .addAfter(defaultEventExecutorGroup, HANDSHAKE_HANDLER_NAME,
	// TLS_HANDLER_NAME, sslContext.newHandler(ctx.channel().alloc()))
	// .addAfter(defaultEventExecutorGroup, TLS_HANDLER_NAME,
	// FILE_REGION_ENCODER_NAME, new FileRegionEncoder());
	// log.info("Handlers prepended to channel pipeline to establish SSL
	// connection");
	// } else {
	// ctx.close();
	// log.error("Trying to establish a SSL connection but sslContext is null");
	// }
	// break;
	//
	// default:
	// log.warn("Unknown TLS mode");
	// break;
	// }
	// } else if (tlsMode == TlsMode.ENFORCING) {
	// ctx.close();
	// log.warn("Clients intend to establish an insecure connection while this
	// server is running in SSL enforcing mode");
	// }
	//
	// // reset the reader index so that handshake negotiation may proceed as
	// normal.
	// msg.resetReaderIndex();
	//
	// try {
	// // Remove this handler
	// ctx.pipeline().remove(this);
	// } catch (NoSuchElementException e) {
	// log.error("Error while removing HandshakeHandler", e);
	// }
	//
	// // Hand over this message to the next .
	// ctx.fireChannelRead(msg.retain());
	// }
	// }

	class NettyServerHandler extends SimpleChannelInboundHandler<RemotingCommand> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
			processMessageReceived(ctx, msg);
		}
	}

	class NettyConnectManageHandler extends ChannelDuplexHandler {
		@Override
		public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
			log.info("NETTY SERVER PIPELINE: channelRegistered {}", remoteAddress);
			super.channelRegistered(ctx);
		}

		@Override
		public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
			log.info("NETTY SERVER PIPELINE: channelUnregistered, the channel[{}]", remoteAddress);
			super.channelUnregistered(ctx);
			NettyServerChannelHolder.unregist(ctx.channel());
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
			log.info("NETTY SERVER PIPELINE: channelActive, the channel[{}]", remoteAddress);
			super.channelActive(ctx);

			if (NettyRemotingServer.this.channelEventListener != null) {
				NettyRemotingServer.this
						.putNettyEvent(new NettyEvent(NettyEventType.CONNECT, remoteAddress, ctx.channel()));
			}
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
			log.info("NETTY SERVER PIPELINE: channelInactive, the channel[{}]", remoteAddress);
			super.channelInactive(ctx);

			if (NettyRemotingServer.this.channelEventListener != null) {
				NettyRemotingServer.this
						.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress, ctx.channel()));
			}
		}

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			if (evt instanceof IdleStateEvent) {
				IdleStateEvent event = (IdleStateEvent) evt;
				if (event.state().equals(IdleState.ALL_IDLE)) {
					final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
					log.warn("NETTY SERVER PIPELINE: IDLE exception [{}]", remoteAddress);
					RemotingUtil.closeChannel(ctx.channel());
					if (NettyRemotingServer.this.channelEventListener != null) {
						NettyRemotingServer.this
								.putNettyEvent(new NettyEvent(NettyEventType.IDLE, remoteAddress, ctx.channel()));
					}
				}
			}

			ctx.fireUserEventTriggered(evt);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
			log.warn("NETTY SERVER PIPELINE: exceptionCaught {}", remoteAddress);
			log.warn("NETTY SERVER PIPELINE: exceptionCaught exception.", cause);

			if (NettyRemotingServer.this.channelEventListener != null) {
				NettyRemotingServer.this
						.putNettyEvent(new NettyEvent(NettyEventType.EXCEPTION, remoteAddress, ctx.channel()));
			}

			RemotingUtil.closeChannel(ctx.channel());
		}
	}

	@Override
	public ChannelEventListener getChannelEventListener() {
		return this.channelEventListener;
	}

	@Override
	public void registerDefaultProcessor(NettyRequestProcessor processor, ExecutorService executor) {
		this.defaultRequestProcessor = new Pair<NettyRequestProcessor, ExecutorService>(processor, executor);
	}

	@Override
	public void registerProcessor(short requestCode, NettyRequestProcessor processor, ExecutorService executor) {
		ExecutorService executorThis = executor;
		if (null == executor) {
			executorThis = this.publicExecutor;
		}

		Pair<NettyRequestProcessor, ExecutorService> pair = new Pair<NettyRequestProcessor, ExecutorService>(processor,
				executorThis);
		this.processorTable.put(requestCode, pair);
	}

}
