package org.smart.framework.net.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 命令注解，用于标识接收消息的方法
 * {@see ModuleHandler}
 * @author ludd
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Cmd {
	
	/**
	 * 命令id(该模块内唯一)
	 * @return
	 */
	public int id();
	
	/**
	 * 验证.默认true
	 * @return
	 */
	public boolean check() default true;
	/**
	 * 验证值
	 * @return
	 */
	public String checkValue() default "";
	
	public Class<?> requstClass() default Object.class;
	
	public boolean sync() default false;
	
}
