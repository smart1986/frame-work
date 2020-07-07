package org.smart.framework.util.thread;
/**
 * 带特定属性任务
 * @author smart
 *
 */
public interface OrderTask extends Runnable {
	Object identify();
}
