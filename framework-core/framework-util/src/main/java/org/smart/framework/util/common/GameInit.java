package org.smart.framework.util.common;
/**
 * spring bean 装配完成后执行
 * @author jerry
 *
 */
public interface GameInit {
	/**
	 * 初始化方法
	 */
	public void gameInit();
	
	/**
	 * 初始化序号，数值越大越靠后
	 * @return
	 */
	public InitIndex index();
}
