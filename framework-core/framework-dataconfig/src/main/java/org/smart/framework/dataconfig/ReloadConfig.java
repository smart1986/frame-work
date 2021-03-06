package org.smart.framework.dataconfig;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.smart.framework.util.ThreadFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ReloadConfig implements InitializingBean {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ReloadConfig.class);

	/**
	 * 配置文件路径
	 */
	@Autowired(required = false)
	@Qualifier("dataconfig.newconfig")
	private String path = "newconfig" + File.separator;

	/**
	 * 配置文件扩展名
	 */
	@Autowired(required = false)
	@Qualifier("dataconfig.extension")
	private String extension = ".xml";

	/**
	 * 扫描配置文件变更间隔(毫秒)
	 */
	@Autowired(required = false)
	@Qualifier("dataconfig.flush_time")
	private long flushTime = 10000L;

	/**
	 * 热刷备份文件扩展名
	 */
	private static final String bakExtension = ".bak";

	@Autowired
	DataConfig dataConfig;

	private boolean isRun = true;
	
	private ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1, new ThreadFactoryImpl("ReloadConfigThread_"));

	@Override
	public void afterPropertiesSet() throws Exception {
		URL resource = getClass().getClassLoader()
				.getResource("");
		LOGGER.info("fresh config root path:{}", resource.getPath());
		timer.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				if (isRun) {
					reloadConfig();
				}
			}
		}, 0, flushTime, TimeUnit.MILLISECONDS);

	}

	private void reloadConfig() {
		isRun = false;
		try {
			for (String name : dataConfig.getAllConfigName()) {
				String filePath = getPath(name);
				URL resource = getClass().getClassLoader()
						.getResource(filePath);
				if (resource != null) {
					boolean result = dataConfig.checkModelAdapter(name,
							resource.openStream());
					if (result) {
						dataConfig.reload(name, resource);
					}
					LOGGER.info(String.format("load file:[%s] is [%s]", name,
							result ? "success" : "fail"));

					File f = new File(URLDecoder.decode(resource.getPath(),
							"utf-8"));
					if (f.exists()) {
						f.delete();
					}
				}
			}
		} catch (Exception ex) {
			LOGGER.warn("{}", ex);
		} finally {
			isRun = true;
		}
	}

	private String getPath(String name) {
		return this.path + name + extension;
	}

	public boolean flushFile(String fileName, String data) {
		byte[] bytes = data.getBytes();
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		String filePath = "";
		try {
			URL resource = getClass().getClassLoader().getResource(path);
			if (resource == null) {
				resource = checkFolderExist();
			}
			filePath = resource.getPath();
			file = new File(filePath + fileName + bakExtension);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(bytes);
		} catch (Exception e) {
			LOGGER.error("write config error.", e);
			return false;
		} finally {
			try {
				if (bos != null) {
					bos.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e1) {
				LOGGER.error("write config error.", e1);
				return false;
			}
			boolean isSuccess = file.renameTo(new File(filePath + fileName
					+ extension));
			if (!isSuccess) {
				LOGGER.warn("rename[" + fileName + "]fail");
				return false;
			}
		}
		return true;
	}

	private URL checkFolderExist() {
		URL url = ClassLoader.getSystemResource("");
		File dir = new File(url.getPath() + path);
		if (!dir.exists() && !dir.isDirectory()) {// 判断文件目录是否存在
			boolean isSuccess = dir.mkdirs();
			if (isSuccess) {
				LOGGER.info("create newconfig folder success...");
			} else {
				LOGGER.warn("create newconfig folder fail");
			}
		}
		return url;
	}

}
