package org.smart.framework.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

/**
 * 序列化对象转换类
 * 
 * @author smart
 * 
 */
public class PackageScanner {
	private static final Logger LOGGER = LoggerFactory.getLogger(PackageScanner.class);

	private static final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private static final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
	private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	/**
	 * 包扫描
	 * 
	 * @param packageNames	包名
	 * @return 返回类对象集合
	 */
	@SuppressWarnings("unchecked")
	public static <T> Collection<Class<T>> scanPackages(String... packageNames) {
		Collection<Class<T>> clazzCollection = new HashSet<Class<T>>();

		String[] arrayOfString = packageNames;
		int j = packageNames.length;
		for (int i = 0; i < j; ++i) {
			String packageName = arrayOfString[i];
			try {
				String packageSearchPath = "classpath*:" + resolveBasePackage(packageName) + "/" + DEFAULT_RESOURCE_PATTERN;
				Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
				for (Resource resource : resources) {
					String className = "";
					try {
						if (resource.isReadable()) {
							MetadataReader metaReader = metadataReaderFactory.getMetadataReader(resource);
							className = metaReader.getClassMetadata().getClassName();

							Class<T> clazz = (Class<T>) Class.forName(className);
							clazzCollection.add(clazz);
						}
					} catch (ClassNotFoundException e) {
						LOGGER.error("class {} not exists!", className);
					}
				}
			} catch (IOException e) {
				LOGGER.error("package scanner {} error!", packageName);
			}
		}

		return clazzCollection;
	}

	/**
	 * 
	 * @param basePackage
	 * @return
	 */
	private static String resolveBasePackage(String basePackage) {
		String placeHolderReplace = SystemPropertyUtils.resolvePlaceholders(basePackage);
		return ClassUtils.convertClassNameToResourcePath(placeHolderReplace);
	}
}