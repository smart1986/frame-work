package org.smart.framework.util.result;

import com.alibaba.fastjson.annotation.JSONField;
/**
 * 状态码结果集
 * @author jerry
 *
 * @param <T>
 */
public class TResult<T> {
	public short statusCode;
	public T item;
	
	public static <T> TResult<T> sucess(T result) {
		TResult<T> res = new TResult<T>();
		res.item = result;
		res.statusCode = 0;
		return res;
	}
	public static <T> TResult<T> sucess() {
		TResult<T> res = new TResult<T>();
		res.statusCode = 0;
		return res;
	}
	
	public static <T> TResult<T> valueOf(short statusCode) {
		TResult<T> result = new TResult<T>();
		result.statusCode = statusCode;
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
