package org.smart.framework.net.startup.impl;

import org.smart.framework.net.startup.Booter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NetBooter implements Booter {

	protected Logger LOGGER = LoggerFactory.getLogger(getClass());
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	
	private ChannelFuture f = null;

//	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	private ChannelInitializer<SocketChannel> initializer;
	private int port = 10123;
	private String name;

	public NetBooter(String name, int port, ChannelInitializer<SocketChannel> initializer) {
		this(name,port,initializer,0,0);
	}
	public NetBooter(String name, int port, ChannelInitializer<SocketChannel> initializer,int bossThread, int workerThread) {
		this.initializer = initializer;
		this.port = port;
		this.name = name;
		
		bossGroup = new NioEventLoopGroup(bossThread); // (1)
		workerGroup = new NioEventLoopGroup(workerThread);
	}

	@Override
	public boolean startup() {

		try {
			ServerBootstrap b = new ServerBootstrap(); // (2)
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) // (3)
					.childHandler(initializer).option(ChannelOption.SO_BACKLOG, 128) // (5)
					.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

			// Bind and start to accept incoming connections.

			f = b.bind(port); // (7)
			LOGGER.info("{} socket bind to port:{}", name,port);
			return true;
		} catch (Exception e) {
			LOGGER.error("{}", e);
			return false;
		}
	}

	@Override
	public boolean stop() {
		if (f != null) {
			f.channel().closeFuture();
		}
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
		LOGGER.info("{} socket unbind to port:{}", name,port);
		return true;
	}

}
