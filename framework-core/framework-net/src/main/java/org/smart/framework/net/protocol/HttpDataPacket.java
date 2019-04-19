package org.smart.framework.net.protocol;

import com.alibaba.fastjson.JSONObject;

public class HttpDataPacket {
	/**
	 * 模块id
	 */
	private String requestMapping;

	/**
	 * 命令id
	 */
	private String path;
	
	
	private JSONObject data;


	public HttpDataPacket() {
	}


	public HttpDataPacket(String requestMapping, String path, JSONObject data) {
		super();
		this.requestMapping = requestMapping;
		this.path = path;
		this.data = data;
	}


	public String getRequestMapping() {
		return requestMapping;
	}


	public void setRequestMapping(String requestMapping) {
		this.requestMapping = requestMapping;
	}


	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public JSONObject getData() {
		return data;
	}


	public void setData(JSONObject data) {
		this.data = data;
	}
	
	
	
	
	
	
}
