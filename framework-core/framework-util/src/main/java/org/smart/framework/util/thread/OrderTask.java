package org.smart.framework.util.thread;
/**
 * 带特定属性任务
 * @author jerry
 *
 */
public interface OrderTask extends Runnable {
	Object identify();
}
