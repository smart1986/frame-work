package org.smart.framework.util.oss.client.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.smart.framework.util.ThreadFactoryImpl;
import org.smart.framework.util.http.IHttpClient;
import org.smart.framework.util.oss.client.OSSLogClient;
import org.smart.framework.util.oss.model.LogBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class LogBackAndNetImpl implements OSSLogClient{

	private Logger ossLogger = LoggerFactory.getLogger("oss");
	
	private IHttpClient httpClient;
	
	private String url;
	
	private int maxThread;
	
	private ExecutorService executorService ;
	
	public LogBackAndNetImpl() {
		executorService = Executors.newFixedThreadPool(1, new ThreadFactoryImpl("logthread_"));
	}
	
	
	



	public LogBackAndNetImpl(IHttpClient httpClient, String url, int maxThread) {
		super();
		this.httpClient = httpClient;
		this.url = url;
		this.maxThread = maxThread;
		executorService = Executors.newFixedThreadPool(maxThread);
	}






	@Override
	public void putOssLog(LogBean logBean) {
		String str = JSON.toJSONString(logBean);
		ossLogger.info(str);
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					httpClient.sendPost(url, str);
				} catch (Exception e) {
				}
			}
		});
	}



	public IHttpClient getHttpClient() {
		return httpClient;
	}



	public void setHttpClient(IHttpClient httpClient) {
		this.httpClient = httpClient;
	}



	public String getUrl() {
		return url;
	}



	public void setUrl(String url) {
		this.url = url;
	}



	public int getMaxThread() {
		return maxThread;
	}



	public void setMaxThread(int maxThread) {
		this.maxThread = maxThread;
	}
	
	

}
