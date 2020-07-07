package org.smart.framework.net.protocol;

/**
 * 响应消息的状态码
 * 
 * @author smart
 * 
 */
public interface StatusCode {
		
	/**
	 * 成功
	 */
	public static short SUCCESS = 0;
	
	/**
	 * 无返回结果
	 */
	public static short NO_RESULTS = 1;
		
	/**
	 * 数据值内容错误
	 */
	public static short DATA_VALUE_ERROR = 2;
	
	/**
	 * 模块不存在
	 */
	public static short MODULE_NOT_FOUND = 3;
	
	/**
	 * 数据包验证错误
	 */
	public  static short DATA_VALIDATE_ERROR = 4;
	/**
	 * 禁止访问
	 */
	public  static short FORBIDEN = 5;
	
	public  static short SYSTEM_EXCEPTION = 6;
	
}