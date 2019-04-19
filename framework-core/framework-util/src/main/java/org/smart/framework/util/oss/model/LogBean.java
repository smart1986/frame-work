package org.smart.framework.util.oss.model;

import java.util.HashMap;
import java.util.Map;

public class LogBean {
	/**
	 * 唯一标识
	 */
	private Object identify;
	/**
	 * 操作类型
	 */
	private Object operationType;

	/**
	 * 数据类型
	 */
	private Object type;
	/**
	 * 操作数据
	 */
	private Map<Object,Object> items = new HashMap<>();

	/**
	 * 操作时间
	 */
	private int operationTime;

	public LogBean() {
		Double d = new Double(System.currentTimeMillis() / 1000);
		operationTime = d.intValue();
	}


	public LogBean(Object identify, Object operationType) {
		this(identify, operationType, null);
	}
	public LogBean(Object identify, Object operationType, Object type) {
		this(identify, operationType, type, null);
	}
	public LogBean(Object identify, Object operationType, Map<Object, Object> items) {
		this(identify, operationType, null, items);
	}
	public LogBean(Object identify, Object operationType, Object type, Map<Object, Object> items) {
		this();
		if (null == identify) {
			throw new RuntimeException("identify not null");
		}
		this.identify = identify;
		if (null == operationType) {
			throw new RuntimeException("operationType not null");
		}
		this.operationType = operationType;
		this.type = type;
		if (items != null) {
			this.items = items;
		}
	}

    public void add2Items(Object key,  Object value) {
        items.put(key, value);
    }

	public Object getIdentify() {
		return identify;
	}


	public void setIdentify(Object identify) {
		this.identify = identify;
	}


	public Object getOperationType() {
		return operationType;
	}


	public void setOperationType(Object operationType) {
		this.operationType = operationType;
	}


	public Map<Object, Object> getItems() {
		return items;
	}


	public void setItems(Map<Object, Object> items) {
		this.items = items;
	}


	public int getOperationTime() {
		return operationTime;
	}


	public void setOperationTime(int operationTime) {
		this.operationTime = operationTime;
	}

	public Object getType() {
		return type;
	}

	public void setType(Object type) {
		this.type = type;
	}






}
