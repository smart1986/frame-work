package org.smart.framework.dataconfig;

import org.smart.framework.util.IdentiyKey;

/**
 * model包中的类继承于此
 * resource.dataconfig包中的文件名必需和model类名一样(忽略大小写)
 * @author ludd
 */
public interface IConfigBean {

	/**
	 * 初始化处理方法(用于model初始化时做一些自定义处理)
	 */
	public void initialize();
	
	/**
	 * 获取唯一标识
	 * @return
	 */
	public IdentiyKey findIdentiyKey();

}
