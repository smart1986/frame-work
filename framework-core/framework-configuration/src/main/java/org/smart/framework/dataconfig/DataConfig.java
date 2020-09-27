package org.smart.framework.dataconfig;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

import org.smart.framework.dataconfig.parse.DataParser;
import org.smart.framework.util.IdentifyKey;

/**
 * 数据配置接口 
 * @author smart
 *
 */
public interface DataConfig {

	/**
	 * 初始化配置数据
	 */
	void initModelAdapterList() throws Exception;
	/**
	 * 根据类名获取数据配置列表
	 * @param modelClass	需要获取的Model类
	 * @return
	 */
	<T extends IConfigBean> Collection<T> listAll(Class<T> modelClass);

	/**
	 * 重载配置文件
	 * @param fileName	文件名
	 * @return
	 */
	boolean reload(String fileName)throws Exception;
	
	/**
	 * 重载配置文件
	 * @param fileName	文件名
	 * @return
	 */
	boolean reload(String fileName, URL url) throws Exception;
	
	/**
	 * 获取所有配置文件名
	 */
	Set<String> getAllConfigName();

	/**
	 * 校验配置文件流
	 * @param name
	 * @param inputStream
	 * @return
	 */
	boolean checkModelAdapter(String name, InputStream inputStream);
	
	/**
	 * 获取配置文件
	 * @param key
	 * @return
	 */
	<T extends IConfigBean> T getConfig(IdentifyKey key, Class<T> clz);



	public String getPath();
	void setPath(String path);
	String getPackageScan();
	void setPackageScan(String packageScan);
	String getExtension();
	void setExtension(String extension);

	DataParser getDataParser();

	void setDataParser(DataParser dataParser);
}
