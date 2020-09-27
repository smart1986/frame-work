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
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 数据配置接口功能实现
 * @author smart
 *
 */
public class MultiKeyDataConfigImpl extends DataConfigImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger(MultiKeyDataConfigImpl.class);


	/**
	 * 所有数据配置存储集合 key:className value: extend ModelAdapter
	 */
	protected ConcurrentHashMap<String, Map<Object, Object>> KEY_MAPPING = new ConcurrentHashMap<>();


	/**
	 * 初始化
	 * @param clazz
	 * @throws Exception 
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

			InputStream input = null;
			input = resource.openStream();
			Map<Object, ? extends IConfigBean> list = dataParser.parse(input, clazz);
			input.close();

			if(list.size() < 1){
				return false;
			}
			list.values().parallelStream().forEach(obj -> obj.initialize());

			synchronized (MODEL_MAPS) {
				if (MODEL_MAPS.contains(clazz.getName())) {
					MODEL_MAPS.remove(clazz.getName());
				}
				MODEL_MAPS.put(clazz.getName(), list);
				Map<Object,Object> keyMap ;
				if (KEY_MAPPING.contains(clazz.getName())){
					keyMap = KEY_MAPPING.get(clazz.getName());
					keyMap.clear();
				} else {
					keyMap = new HashMap<>();
					KEY_MAPPING.put(clazz.getName(), keyMap);
				}
				for (Entry<Object, ? extends IConfigBean> entry : list.entrySet()) {
					if (entry.getValue() instanceof IMultiKeyConfigBean){
						IMultiKeyConfigBean value = (IMultiKeyConfigBean) entry.getValue();
						List<IdentifyKey> cacheKeys = value.findCacheKeys();
						if (cacheKeys != null && !cacheKeys.isEmpty()){
							for (IdentifyKey key : cacheKeys) {
								keyMap.put(key, value.findIdentifyKey());
							}
						}
					}
				}
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



	@SuppressWarnings("unchecked")
	@Override
	public <T extends IConfigBean> T getConfig(IdentifyKey key, Class<T> clz) {
		String name = clz.getName();
		Map<Object, ? extends IConfigBean> map = MODEL_MAPS.get(name);
		if (map == null) {
			return null;
		}
		Map<Object, Object> keymap = KEY_MAPPING.get(name);
		Object tmpKey = keymap.get(key);
		Object loadKey = key;
		if (tmpKey != null){
			loadKey = tmpKey;
		}

		if (!map.containsKey(loadKey)){
			return null;
		}

		return (T) map.get(loadKey);

	}

}
