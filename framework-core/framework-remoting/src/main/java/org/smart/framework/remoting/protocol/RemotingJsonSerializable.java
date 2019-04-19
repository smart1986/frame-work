package org.smart.framework.remoting.protocol;

import java.nio.charset.Charset;

import com.alibaba.fastjson.JSON;

public abstract class RemotingJsonSerializable<T> implements RemotingSerializable<T> {
	private final static Charset CHARSET_UTF8 = Charset.forName("UTF-8");

	public static byte[] encode(final Object obj) {
		final String json = toJson(obj, false);
		if (json != null) {
			return json.getBytes(CHARSET_UTF8);
		}
		return null;
	}

	public static String toJson(final Object obj, boolean prettyFormat) {
		return JSON.toJSONString(obj, prettyFormat);
	}

	public static <T> T decode(final byte[] data, Class<T> classOfT) {
		final String json = new String(data, CHARSET_UTF8);
		return fromJson(json, classOfT);
	}

	public static <T> T fromJson(String json, Class<T> classOfT) {
		return JSON.parseObject(json, classOfT);
	}

	@Override
	public T decode(byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] encode() {
		final String json = this.toJson();
		if (json != null) {
			return json.getBytes(CHARSET_UTF8);
		}
		return null;
	}

	public String toJson() {
		return toJson(false);
	}

	public String toJson(final boolean prettyFormat) {
		return toJson(this, prettyFormat);
	}
}
