package org.smart.framework.dataconfig.parse;

import java.io.InputStream;
import java.util.Map;

import org.smart.framework.dataconfig.IConfigBean;

/**
 * 数据解析接口
 * 
 * @author smart
 * 
 */
public interface DataParser {

	/**
	 * 读取配置文件后进行解析
	 * 
	 * @param stream  文件流
	 * @param className 解析映射类文件
	 * @return
	 */
	<T extends IConfigBean> Map<Object,T> parse(InputStream stream, Class<T> className);
}
