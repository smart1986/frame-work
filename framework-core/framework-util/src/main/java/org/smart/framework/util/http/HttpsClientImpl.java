package org.smart.framework.util.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

public class HttpsClientImpl extends HttpClientImpl {

	public HttpsClientImpl() {
		this(5000, 5000, 128, 5, null);
	}

	public HttpsClientImpl(int socketTimeout, int connectionTimeout, int maxTotal, final int retryTimes, String keyPath,
			String pwd) {
		try {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			FileInputStream instream = new FileInputStream(new File(keyPath));
			try {
				keyStore.load(instream, pwd.toCharArray());
			} finally {
				instream.close();
			}

			// Trust own CA and all self-signed certs
			SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, pwd.toCharArray()).build();
			// Allow TLSv1 protocol only
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" },
					null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());

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

			// Create a registry of custom connection socket factories for
			// supported
			// protocol schemes.
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", sslsf).build();

			cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

			httpclient = HttpClients.custom().setConnectionManager(cm).setRetryHandler(myRetryHandler).build();
			cm.setMaxTotal(maxTotal);
			requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectionTimeout)
					.build();// 设置请求和传输超时时间

		} catch (Exception e) {
			logger.error("{}", e);
		}
	}

	public HttpsClientImpl(int socketTimeout, int connectionTimeout, int maxTotal, final int retryTimes,
			SSLConnectionSocketFactory factory) {
		if (factory == null) {
			SSLContext sslcontext = SSLContexts.createSystemDefault();
			factory = new SSLConnectionSocketFactory(sslcontext);
		}
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

			// Create a registry of custom connection socket factories for
			// supported
			// protocol schemes.
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", factory).build();

			cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

			httpclient = HttpClients.custom().setConnectionManager(cm).setRetryHandler(myRetryHandler).build();
			cm.setMaxTotal(maxTotal);
			requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectionTimeout)
					.build();// 设置请求和传输超时时间

		} catch (Exception e) {
			logger.error("{}", e);
		}
	}
}
