package org.smart.framework.dataconfig;

import org.smart.framework.util.IdentifyKey;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

public abstract class ConfigServiceAdapter<T extends IConfigBean> {

	private static Map<Class<? extends IConfigBean>, ConfigServiceAdapter<? extends IConfigBean>> map = new ConcurrentHashMap<>();

	@Resource
	public DataConfig dataConfig;

	public ConfigServiceAdapter() {
		initAdapter();
	}

	private void initAdapter() {
		Class<? extends IConfigBean> clz = forClass();
		map.put(clz, this);
	}

	public static ConfigServiceAdapter<?> get(Class<? extends IConfigBean> clz) {
		return map.get(clz);
	}

	/**
	 * 返回所服务的配置类
	 * 
	 * @return
	 */
	public abstract Class<T> forClass();

	public void refresh() {
		clear();
		init();
	}

	public  Collection<T> getAllConfig(Class<T> clz){
		return dataConfig.listAll(clz);
	}
	public  T getConfig(IdentifyKey key){
		return dataConfig.getConfig(key,forClass());
	}

	/**
	 * 初始化
	 */
	protected abstract void init();

	/**
	 * 清理
	 */
	protected abstract void clear();

	public DataConfig getDataConfig() {
		return dataConfig;
	}

	public void setDataConfig(DataConfig dataConfig) {
		this.dataConfig = dataConfig;
	}
}
