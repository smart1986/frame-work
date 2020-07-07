package org.smart.framework.util.result;

import com.alibaba.fastjson.annotation.JSONField;
/**
 * 带字符串信息的结果集
 * @author smart
 *
 * @param <T>
 */
public class TMsgResult<T> extends TResult<T> {
	public short statusCode;
	public T item;
	public String message;
	
	public static <T> TMsgResult<T> sucess(T result) {
		TMsgResult<T> res = new TMsgResult<T>();
		res.item = result;
		res.statusCode = 0;
		return res;
	}
	public static <T> TMsgResult<T> sucess() {
		TMsgResult<T> res = new TMsgResult<T>();
		res.statusCode = 0;
		return res;
	}
	
	public static <T> TMsgResult<T> valueOf(short statusCode) {
		TMsgResult<T> result = new TMsgResult<T>();
		result.statusCode = statusCode;
		return result;
	}
	public static <T> TMsgResult<T> valueOf(short statusCode,String message) {
		TMsgResult<T> result = new TMsgResult<T>();
		result.statusCode = statusCode;
		result.message = message;
		return result;
	}
	@JSONField(serialize=false)  
	public boolean isFail() {
		return statusCode != 0;
	}
	@JSONField(serialize=false)  
	public boolean isOk() {
		return statusCode == 0;
	}
}
