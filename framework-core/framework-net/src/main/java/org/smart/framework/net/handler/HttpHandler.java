package org.smart.framework.net.handler;


import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.smart.framework.net.router.HttpRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

public class HttpHandler extends SimpleChannelInboundHandler<Object> {
	protected Logger LOGGER = LoggerFactory.getLogger(getClass());
	private HttpRouter httpRouter;

	public HttpHandler(HttpRouter httpRouter) {
		if (httpRouter == null) {
			throw new RuntimeException("router is null!");
		}
		this.httpRouter = httpRouter;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
//			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}
	
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
		// Handle a bad request.
		if (!req.decoderResult().isSuccess()) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}
		// Allow only GET methods.
//		if (req.method() != GET) {
//			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
//			return;
//		}

		// Send the demo page and favicon.ico
		if ("/".equals(req.uri())) {
			// ByteBuf content =
			// WebSocketServerBenchmarkPage.getContent(getWebSocketLocation(req));
			// FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
			// content);
			// res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;
			// charset=UTF-8");
			// HttpUtil.setContentLength(res, content.readableBytes());
			//
			// sendHttpResponse(ctx, req, res);
			// return;
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}
		if ("/favicon.ico".equals(req.uri())) {
			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
			sendHttpResponse(ctx, req, res);
			return;
		}
		FullHttpResponse res = (FullHttpResponse) httpRouter.forward(ctx, req);
		res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
		HttpUtil.setContentLength(res, res.content().readableBytes());
		sendHttpResponse(ctx, req, res);


//		// Handshake
//		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req),
//				null, true, 5 * 1024 * 1024);
//		handshaker = wsFactory.newHandshaker(req);
//		if (handshaker == null) {
//			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
//		} else {
//			handshaker.handshake(ctx.channel(), req);
//		}
	}
	
	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpUtil.setContentLength(res, res.content().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
	
}
