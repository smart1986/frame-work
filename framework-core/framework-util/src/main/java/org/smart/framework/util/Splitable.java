package org.smart.framework.util;

/**
 * 各种分隔符
 * @author smart
 *
 */
public interface Splitable {
	
	/**
	 * 分隔符说明
	 * 第一层:  |				1001|1002
	 * 第二层:  ,				1001,1002|1003,1004
	 * 
	 */

	/**
	 * ":"
	 */
	public static final String DELIMITER_ARGS = ":";
	
	/**
	 * ","
	 */
	public static final String BETWEEN_DOT = ",";
	public static final String BETWEEN_UNDERLINE = "_";
	
	/**
	 * "\\|"
	 */
	public static final String ELEMENT_SPLIT = "\\|";
	
	/**
	 * "|" 调用String.split时不能用ELEMENT_DELIMITER, 用ELEMENT_SPLIT即可
	 */
	public static final String ELEMENT_DELIMITER = "|";
	
	
	
}