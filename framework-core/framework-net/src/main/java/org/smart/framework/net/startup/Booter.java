package org.smart.framework.net.startup;

public interface Booter {
	/**
	 * 启动
	 * @return
	 */
	public boolean startup();
	
	/**
	 * 停止
	 * @return
	 */
	public boolean stop();
}
