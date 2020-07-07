package org.smart.framework.util;

/**
 * 时间点定义
 * @author smart
 *
 */
public abstract interface TimeConstant {
	
	/**
	 * 1秒钟的豪秒总数
	 */
	public static final int ONE_SECOND_MILLISECOND = 1000;
	
	/**
	 * 1分钟的豪秒总数
	 */
	public static final int ONE_MINUTE_MILLISECOND = 60000;
	
	/**
	 * 1小时的豪秒总数
	 */
	public static final int ONE_HOUR_MILLISECOND = 3600000;

	/**
	 * 1天(24小时)的豪秒总数
	 */
	public static final int ONE_DAY_MILLISECOND = 86400000;
	
	/**
	 * 1分钟的秒总数
	 */
	public static final int ONE_MINUTE_SECOND = 60;
	
	/**
	 * 1小时的秒总数
	 */
	public static final long ONE_HOUR_SECOND = 3600L;
	
	/**
	 * 1天(24小时)的秒总数
	 */
	public static final long ONE_DAY_SECOND = 86400L;
}