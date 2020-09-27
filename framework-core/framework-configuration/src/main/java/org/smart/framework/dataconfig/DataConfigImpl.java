package org.smart.framework.dataconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart.framework.dataconfig.annotation.DataFile;
import org.smart.framework.dataconfig.parse.DataParser;
import org.smart.framework.util.IdentifyKey;
import org.smart.framework.util.PackageScanner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 数据配置接口功能实现
 * @author smart
 *
 */
public class DataConfigImpl implements DataConfig {
	protected Logger LOGGER = LoggerFactory.getLogger(DataConfigImpl.class);

	protected DataConfiguration dataConfiguration;
	protected DataParser dataParser;

	/**
	 * 所有数据配置存储集合 key:className value: extend ModelAdapter
	 */
	protected  ConcurrentHashMap<String, Map<Object, ? extends IConfigBean>> MODEL_MAPS = new ConcurrentHashMap<>();

	/**
	 * model类与名称的映射
	 * key:DataFile.fileName() value:Class
	 */
	protected ConcurrentHashMap<String, Class<? extends IConfigBean>> MODEL_CLASS_MAPS = new ConcurrentHashMap<>();



	@Override
	@SuppressWarnings("unchecked")
	public <T extends IConfigBean> Collection<T> listAll(Class<T> modelClass) {
		String name = modelClass.getName();
		Map<Object, ? extends IConfigBean> map = MODEL_MAPS.get(name);

		if (map == null) {
			LOGGER.warn("{} config not found",name);
			return Collections.emptyList();
		}
		return  (Collection<T>) map.values();
	}

	@Override
	public boolean reload(String fileName) throws Exception {
		Class<? extends IConfigBean> clazz = MODEL_CLASS_MAPS.get(fileName);
		if (clazz == null) {
			return false;
		}

		// 重载model
		if (initModelAdapter(clazz)) {
			LOGGER.info(String.format("reload file:[%s]", fileName));
			return true;
		}

		return false;
	}

	@Override
	public boolean reload(String fileName, URL url) {
		if (fileName.isEmpty() || url == null) {
			return false;
		}

		String filePath = getFullPath(fileName);
		URL resource;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			resource = getClass().getClassLoader().getResource(filePath);
			inputStream = url.openStream();
			outputStream = new FileOutputStream(URLDecoder.decode(resource.getPath(), "utf-8"));
			byte[] buffer = new byte[1024];
			int readed = 0; // 一次读多个，readed代表当前已读的数据总数
			while ((readed = inputStream.read(buffer)) != -1) {
				// 从第0位写，reader代表读写几位
				outputStream.write(buffer, 0, readed);
			}

			return reload(fileName);
		} catch (Exception e) {
			LOGGER.error("{}", e);
		} finally {
			try {
				inputStream.close();
				outputStream.close();
			} catch (Exception e) {
				LOGGER.error("{}", e);
			}
		}
		return false;
	}

	/**
	 * 获取所有配置文件名
	 */
	@Override
	public Set<String> getAllConfigName() {
		return MODEL_CLASS_MAPS.keySet();
	}

	@Override
	public boolean checkModelAdapter(String fileName, InputStream inputStream) {
		Class<? extends IConfigBean> clazz = MODEL_CLASS_MAPS.get(fileName);
		if (clazz == null) {
			return false;
		}

		try {
			DataFile df = clazz.getAnnotation(DataFile.class);
			if (df == null) {
				return false;
			}

			Map<Object, ? extends IConfigBean> list = dataParser.parse(inputStream, clazz);

			if (list.size() < 1) {
				return false;
			}
			return true;
		} catch (Exception e) {
			LOGGER.error(String.format("file: [%s] read error!", clazz.getName()), e);
			return false;
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				LOGGER.error("{}", e);
			}
		}
	}

	/**
	 * 初始化ModelAdapter
	 */
	@Override
	public void initModelAdapterList(DataConfiguration dataConfiguration, DataParser dataParser)throws Exception {
		this.dataConfiguration = dataConfiguration;
		this.dataParser = dataParser;
		LOGGER.info("dataconfig path:{}, packageScan:{}, extension:{}, dataParser:{}",dataConfiguration.getPath(), dataConfiguration.getPackageScan()
				, dataConfiguration.getExtension(),dataParser.getClass().getName());
		String[] temp = dataConfiguration.getPackageScan().split(",");
		// 通过包名扫描获取对应的类集合
		Collection<Class<IConfigBean>> collection = PackageScanner.scanPackages(temp);
		if (collection == null || collection.isEmpty()) {
			LOGGER.error(String.format("在 [%s]包下没有扫描到实体类!", dataConfiguration.getPackageScan()));
			return;
		}
		AtomicBoolean flag = new AtomicBoolean(true);
		AtomicBoolean flagException = new AtomicBoolean(false);
		collection.parallelStream().forEach(clazz1 -> {
			try {
				boolean f = initModelAdapter(clazz1);
				if (!f){
					flag.set(false);
				}
			} catch (Exception e) {
				LOGGER.error("", e);
				flagException.set(true);
			}
		});
		if (flagException.get()){
			throw new RuntimeException("parse data config error!");
		}
		if (flag.get()){
			LOGGER.info("all data config file load complete!");
		} else {
			LOGGER.info("part of data config file load complete!");
		}
	}

	/**
	 * 初始化
	 */
	public boolean initModelAdapter(Class<? extends IConfigBean> clazz) throws Exception {
		try {
			DataFile df = clazz.getAnnotation(DataFile.class);
			if (df == null) {
				return false;
			}

			String fullPath = getFullPath(df.fileName());
			URL resource = getClass().getClassLoader().getResource(fullPath);
			if (resource == null) {
				LOGGER.error(String.format("load data config file [%s] error. file name [%s] not exists!", clazz.getName(), fullPath));
				return false;
			}

			InputStream input = resource.openStream();
			Map<Object, ? extends IConfigBean> list = dataParser.parse(input, clazz);
			input.close();

			if(list.size() < 1){
				return false;
			}

			list.values().parallelStream().forEach(obj -> {obj.initialize();});

			synchronized (MODEL_MAPS) {
				if (MODEL_MAPS.contains(clazz.getName())) {
					MODEL_MAPS.remove(clazz.getName());
				}
				MODEL_MAPS.put(clazz.getName(), list);
			}
			MODEL_CLASS_MAPS.put(df.fileName(), clazz);

			ConfigServiceAdapter<?> cfgAdapter = ConfigServiceAdapter.get(clazz);
			if (cfgAdapter != null) {
				cfgAdapter.refresh();
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("[%s] file load complete!", fullPath));
			}
			return true;
		} catch (Exception e) {
			LOGGER.error(String.format("file: [%s] read error!", clazz.getName()), e);
			throw new RuntimeException(e);
		}
	}



	/**
	 * 根据文件名获取全路径
	 * @param fileName
	 * @return
	 */
	protected String getFullPath(String fileName) {
		return this.dataConfiguration.getPath() + fileName + this.dataConfiguration.getExtension();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IConfigBean> T getConfig(IdentifyKey key, Class<T> clz) {
		String name = clz.getName();
		Map<Object, ? extends IConfigBean> map = MODEL_MAPS.get(name);

		if (map == null) {
			return null;
		}
		if (!map.containsKey(key)){
			return null;
		}

		return (T) map.get(key);

	}

}
