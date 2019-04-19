package org.smart.framework.util.http;

import java.io.Closeable;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface IHttpClient extends Closeable{
	public String sendGet(String url, Map<String, Object> params, boolean encodeParams) ;

	public String sendGet(String url, Map<String, Object> params);

	public String sendGet(String url, String prefix) ;

	/**
	 * 向指定URL发送GET方法的请求
	 * 
	 * @param urlPath
	 *            已拼合好的url地址
	 * @return
	 */
	public String sendGet(String urlPath) ;
	public String sendPost(String url, String params);
	public String sendPost(String url, Map<String, Object> paramsMaps);

	/**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param url
	 *            发送请求的 URL
	 * @param params
	 *            post的字符串
	 * @return
	 */
	public String sendPost(String url, String params, String contentType, Map<String,String> headers) ;

	/**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param url
	 *            发送请求的 URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	public String sendPost(String url, Map<String, Object> paramsMaps, String contentType);
	
	public String sendPost(String url, Map<String, Object> paramsMaps, String contentType, Map<String, Object> header);
	
	public String sendPost(String url, JSONObject jsonObject);
	public String sendPost(String url, JSONObject jsonObject,Map<String,String> headers);
	
}
