package org.smart.framework.dataconfig.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据配置标注
 * @author ludd
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DataFile {

	/**
	 * 对应的配置文件名
	 * @return
	 */
	String fileName();
}
