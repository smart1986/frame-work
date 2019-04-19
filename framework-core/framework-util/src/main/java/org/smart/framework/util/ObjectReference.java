package org.smart.framework.util;

/**
 * 
 * @author ludd
 *
 * @param <T>
 */
public class ObjectReference <T> {

	/**
	 * 软引用对象 
	 */
	private T content;

	public T get() {
		return content;
	}

	public void set(T content) {
		this.content = content;
	}

	
}
