package org.smart.framework.datacenter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * db表名标注
 * @author smart
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

	/**
	 * 表名 在反射中直接用.Entity的类名即可。
	 */
	String name();
	/**
	 * 存储级别 {@code DBQueueType}
	 */
	DBQueueType type() default DBQueueType.IMPORTANT;
}
