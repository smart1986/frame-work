package org.smart.framework.datacenter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * db字段标注
 * @author ludd
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Column {

	/**
	 * 是否为主键。默认为false
	 * @return
	 */
	public boolean pk() default false;
	/**
	 * 是否为外键
	 * @return
	 */
	public boolean fk() default false;
	
	/**
	 * 字段别名
	 * @return
	 */
	public String alias() default "";

}
