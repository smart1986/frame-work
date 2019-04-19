package org.smart.framework.util.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class HttpClientImpl implements IHttpClient {
	protected  Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected PoolingHttpClientConnectionManager cm;
	protected CloseableHttpClient httpclient;

	protected RequestConfig requestConfig;
	
	public HttpClientImpl() {
		this(5000,5000,128,5);
	}
	
	public HttpClientImpl(int socketTimeout, int connectionTimeout, int maxTotal, final int retryTimes) {
		this(socketTimeout,connectionTimeout,maxTotal,retryTimes,null);
	}
	public HttpClientImpl(int socketTimeout, int connectionTimeout, int maxTotal, final int retryTimes,SSLContext sslContext) {
		
		try {
			HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
				
				@Override
				public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
					if (executionCount >= retryTimes) {
						// Do not retry if over max retry count
						return false;
					}
					if (exception instanceof InterruptedIOException) {
						// Timeout
						return false;
					}
					if (exception instanceof UnknownHostException) {
						// Unknown host
						return false;
					}
					if (exception instanceof ConnectTimeoutException) {
						// Connection refused
						return false;
					}
					if (exception instanceof SSLException) {
						// SSL handshake exception
						return false;
					}
					HttpClientContext clientContext = HttpClientContext.adapt(context);
					HttpRequest request = clientContext.getRequest();
					boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
					if (idempotent) {
						// Retry if the request is considered idempotent
						return true;
					}
					return false;
				}
				
			};
			// SSL context for secure connections can be created either based on
			// system or application specific properties.
			SSLContext sslcontext = sslContext == null? SSLContexts.createSystemDefault() : sslContext;
			
			// Create a registry of custom connection socket factories for
			// supported
			// protocol schemes.
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
					.register("http", PlainConnectionSocketFactory.INSTANCE)
					.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
			
			cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			
			httpclient = HttpClients.custom().setConnectionManager(cm).setRetryHandler(myRetryHandler).build();
			cm.setMaxTotal(maxTotal);
			requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectionTimeout)
					.build();// 设置请求和传输超时时间
			
			
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}
	
	@Override
	public String sendGet(String url, Map<String, Object> params ) {
		return sendGet(url, params, false);
	}

	@Override
	public String sendGet(String url, Map<String, Object> params,boolean encodeParam) {
		return sendGet(url, map2Prefix(params, encodeParam));
	}

	@Override
	public String sendGet(String url, String prefix) {
		if (prefix != null && !prefix.isEmpty()) {
			if (url.indexOf("?") < 1) {
				url += "?";
			}
		}
		return sendGet(url.concat(prefix));
	}

	@Override
	public String sendGet(String urlPath) {
		try {
			HttpGet httpget = new HttpGet(urlPath);
			httpget.setConfig(requestConfig);
			logger.debug(String.format("httputil request url:[%s] ", urlPath));
			CloseableHttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = null;
			int statusCode = response.getStatusLine().getStatusCode();
			try {
				entity = response.getEntity();
				if (statusCode != HttpStatus.SC_OK) {
					logger.error(EntityUtils.toString(entity,Charset.forName("utf-8")));
					return "";
				}
				return EntityUtils.toString(entity,Charset.forName("utf-8"));
			} finally {
				logger.debug(String.format("url:[%s] status:[%s]", urlPath, statusCode));
				if (entity != null) {
					EntityUtils.consume(entity);
				}
				response.close();

			}
		} catch (Exception e) {
			logger.error("{}", e);
		}
		return "";
	}

	@Override
	public String sendPost(String url, String params) {
		return sendPost(url,params,null, null);
	}

	@Override
	public String sendPost(String url, Map<String, Object> paramsMaps) {
		return sendPost(url,paramsMaps,null,null);
	}

	@Override
	public String sendPost(String url, String params, String contentType, Map<String,String> headers) {
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.setConfig(requestConfig);
			ByteArrayEntity sendEntity = new ByteArrayEntity(params.getBytes("UTF-8"));
			if(contentType != null) {
				sendEntity.setContentType(contentType);
			}
			if (headers!=null&& !headers.isEmpty()){
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			
			httpPost.setEntity(sendEntity);
			CloseableHttpResponse response = httpclient.execute(httpPost);
			HttpEntity entity = null;
			int statusCode = response.getStatusLine().getStatusCode();
			try {
				entity = response.getEntity();
				if (statusCode != HttpStatus.SC_OK) {
					logger.error(EntityUtils.toString(entity,Charset.forName("utf-8")));
					return "";
				}
				return EntityUtils.toString(entity,Charset.forName("utf-8"));
			} finally {
				logger.debug(String.format("url:[%s] prefix:[%s] status:[%s]", url, params, statusCode));
				if (entity != null) {
					EntityUtils.consume(entity);
				}

				response.close();

			}
		} catch (Exception e) {
			logger.error("{}", e);
		}
		return "";
	}

	@Override
	public String sendPost(String url, Map<String, Object> paramsMaps, String contentType) {
		return sendPost(url,paramsMaps,contentType,null);
	}
	
	@Override
	public String sendPost(String url, Map<String, Object> paramsMaps, String contentType, Map<String, Object> headers) {
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.setConfig(requestConfig);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			for (Map.Entry<String, Object> entry : paramsMaps.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
			}
			UrlEncodedFormEntity sendEntity = new UrlEncodedFormEntity(nvps, "utf-8");
			if(contentType != null) {
				sendEntity.setContentType(contentType);
			}
			if (headers != null && !headers.isEmpty()){
				for (Map.Entry<String, Object> nameValuePair : headers.entrySet()) {
					httpPost.addHeader(nameValuePair.getKey(), nameValuePair.getValue().toString());
				}
			}
			httpPost.setEntity(sendEntity);
			CloseableHttpResponse response = httpclient.execute(httpPost);
			HttpEntity entity = null;
			int statusCode = response.getStatusLine().getStatusCode();
			try {
				entity = response.getEntity();
				if (statusCode != HttpStatus.SC_OK) {
					logger.error(EntityUtils.toString(entity,Charset.forName("utf-8")));
					return "";
				}
				return EntityUtils.toString(entity,Charset.forName("utf-8"));
			} finally {
				logger.debug(String.format("url:[%s] prefix:[%s] status:[%s]", url, map2Prefix(paramsMaps, false),
						statusCode));
				if (entity != null) {
					EntityUtils.consume(entity);
				}
				response.close();

			}
		} catch (Exception e) {
			logger.error("{}", e);
		}
		return "";
	}
	
	private String map2Prefix(Map<String, Object> data, boolean encodeParams) {
		StringBuilder sb = new StringBuilder();
		try {

			for (Entry<String, Object> entry : data.entrySet()) {
				if (encodeParams) {
					sb.append("&" + entry.getKey() + "=" + URLEncoder.encode(entry.getValue().toString(), "utf-8"));
				} else {
					sb.append("&" + entry.getKey() + "=" + entry.getValue());
				}
			}

			if (sb.length() > 1) {
				return sb.substring(1).toString();
			}
			return "";
		} catch (Exception ex) {
			logger.warn("{}", ex);
		}
		return "";
	}
	
	@Override
	public void close() {
		if (this.httpclient != null) {
			try {
				this.httpclient.close();
			} catch (IOException e) {
				logger.error("{}", e);
			}
		}
		
	}
	
	@Override
	public String sendPost(String url, JSONObject jsonObject) {
		return sendPost(url, jsonObject.toJSONString(), "application/json", null);
	}
	
	@Override
	public String sendPost(String url, JSONObject jsonObject, Map<String,String> headers) {
		return sendPost(url, jsonObject.toJSONString(), "application/json", headers);
	}
}
