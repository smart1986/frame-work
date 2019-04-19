package org.smart.framework.net.router.impl;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.smart.framework.net.router.HttpRouter;
import org.smart.framework.net.router.RequestParser;

import com.google.common.base.Strings;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public class HttpRouterImpl extends HttpRouter {
	private String whileIp = "";

	public HttpRouterImpl() {
	}
	public HttpRouterImpl(String whileIp) {
		this.whileIp = whileIp;
	}
	@Override
	public Object forward(ChannelHandlerContext ctx, FullHttpRequest request) {
		String path;
		try {
			path = new URI(request.uri()).getPath();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			return new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
		}
		Object handler = handlerMap.get(path);
		Method method = mathodMap.get(path);
		if (handler == null || method == null) {
			return new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
		}
		String[] iplist = whileIp.split(",");
		boolean flag = false;//是否允许
		if (!Strings.isNullOrEmpty(whileIp) && checkMap.get(path) && iplist != null && iplist.length!= 0) {//需要检查
			flag = true;
			String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
			if (!ip.equals("127.0.0.1")){//其他机器
				for (String wIp : iplist) {
					if(wIp.equals(ip)){
						flag = false;
					}
				}
			} else {//本机
				flag = false;
			}
		}
		if (flag) {
			return new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
		}
		FullHttpResponse result;
		try {
			Map<String, Object> parmMap = RequestParser.parse(request);
			result = (FullHttpResponse) method.invoke(handler, ctx.channel(), parmMap);
			return result;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException e) {
//			e.printStackTrace();
			LOGGER.error("{}",e);
			return new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
		}

	}
	@Override
	public boolean forwardValidate(ChannelHandlerContext ctx, FullHttpRequest request) {
		// TODO Auto-generated method stub
		return false;
	}

}
