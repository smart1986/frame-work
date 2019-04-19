package org.smart.framework.net.websocket.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;

public abstract class WebSocketClient {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Channel channel;
	
	public WebSocketClient() {
	}

	public void start(String url) {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
		final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
		final int port;
		if (uri.getPort() == -1) {
			if ("ws".equalsIgnoreCase(scheme)) {
				port = 80;
			} else if ("wss".equalsIgnoreCase(scheme)) {
				port = 443;
			} else {
				port = -1;
			}
		} else {
			port = uri.getPort();
		}

		if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
			System.err.println("Only WS(S) is supported.");
			return;
		}
		try {

			final boolean ssl = "wss".equalsIgnoreCase(scheme);
			final SslContext sslCtx;
			if (ssl) {
				sslCtx = SslContextBuilder.forClient()
						.trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			} else {
				sslCtx = null;
			}

			EventLoopGroup group = new NioEventLoopGroup();
			// Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
			// If you change it to V00, ping is not supported and remember to change
			// HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
			final WebSocketClientHandler handler = new WebSocketClientHandler(
					WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null,
							true, new DefaultHttpHeaders()));

			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) {
							ChannelPipeline p = ch.pipeline();
							if (sslCtx != null) {
								p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
							}
							p.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192),
									WebSocketClientCompressionHandler.INSTANCE, handler);
						}
					});

			channel = b.connect(uri.getHost(), port).sync().channel();
			handler.handshakeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

	    private final WebSocketClientHandshaker handshaker;
	    private ChannelPromise handshakeFuture;

	    public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
	        this.handshaker = handshaker;
	    }

	    public ChannelFuture handshakeFuture() {
	        return handshakeFuture;
	    }

	    @Override
	    public void handlerAdded(ChannelHandlerContext ctx) {
	        handshakeFuture = ctx.newPromise();
	    }

	    @Override
	    public void channelActive(ChannelHandlerContext ctx) {
	        handshaker.handshake(ctx.channel());
	    }

	    @Override
	    public void channelInactive(ChannelHandlerContext ctx) {
	    	logger.info("WebSocket Client disconnected!");
	    }

	    @Override
	    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
	        Channel ch = ctx.channel();
	        if (!handshaker.isHandshakeComplete()) {
	            try {
	                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
	                logger.info("WebSocket Client connected!");
	                handshakeFuture.setSuccess();
	            } catch (WebSocketHandshakeException e) {
	            	logger.info("WebSocket Client failed to connect");
	                handshakeFuture.setFailure(e);
	            }
	            return;
	        }

	        if (msg instanceof FullHttpResponse) {
	            FullHttpResponse response = (FullHttpResponse) msg;
	            throw new IllegalStateException(
	                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
	                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
	        }

	        WebSocketFrame frame = (WebSocketFrame) msg;
	        if (frame instanceof TextWebSocketFrame) {
	            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
	            onMessage(textFrame.text());
	        } else if (frame instanceof BinaryWebSocketFrame){
	        	BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
	        	onMessage(binaryFrame.content());
	        } else if (frame instanceof PongWebSocketFrame) {
	        	logger.info("WebSocket Client received pong");
	        } else if (frame instanceof CloseWebSocketFrame) {
	        	logger.info("WebSocket Client received closing");
	            ch.close();	
	        }
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	        cause.printStackTrace();
	        if (!handshakeFuture.isDone()) {
	            handshakeFuture.setFailure(cause);
	        }
	        ctx.close();
	    }
	}
	protected abstract void onMessage(String text);
	
	protected abstract void onMessage(ByteBuf byteBuf) ;
	public void send(String text) {
		if (channel != null && channel.isActive()) {
			WebSocketFrame frame = new TextWebSocketFrame(text);
			channel.writeAndFlush(frame);
		}
	}
	public void send(ByteBuf byteBuf) {
		if (channel != null && channel.isActive()) {
			WebSocketFrame frame = new BinaryWebSocketFrame(byteBuf);
			channel.writeAndFlush(frame);
		}
	}
	
	public void close() {
		if (channel != null) {
			channel.close();
		}
	}
	
	

}
