package org.smart.framework.net.ext;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.io.UnsupportedEncodingException;

public class HttpResponseUtil {


	/**
	 * 构建跨域访问的httpresponse
	 * @param content
	 * @param contentType
	 * @return
	 */
	public static FullHttpResponse buildAccessAllowHttpResponse(String content,String contentType){
		byte[] bytes;
		try {
			bytes = content.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
				Unpooled.wrappedBuffer(bytes));
		response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Origin, X-Requested-With, Content-Type, Accept");
		response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT,DELETE");
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
		return response;
	}
}
