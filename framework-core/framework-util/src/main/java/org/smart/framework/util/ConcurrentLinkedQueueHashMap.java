package org.smart.framework.util;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 有队列和Map的对象
 * @author smart
 * @param <K>
 * @param <V>
 */
public class ConcurrentLinkedQueueHashMap<K,V> {

	private Queue<K> queue;
	private HashMap<K, V> hashMap;
	
	/**
	 * 对象同步锁
	 */
	private byte[] syncLock = new byte[0];
	
	/**
	 * Constructor.
	 * @param initialCapacity
	 */
	public ConcurrentLinkedQueueHashMap(int initialCapacity) {
		queue = new ConcurrentLinkedQueue<>();
		hashMap = new HashMap<>(initialCapacity);
	}
	
	/**
	 * 取出V对象
	 * @return
	 */
	public Map.Entry<K, V> poll() {
		synchronized (syncLock) {
			K key = queue.poll();
			if (key == null) {
				return null;
			}
			
			V value = hashMap.remove(key);
			return new AbstractMap.SimpleEntry<K, V>(key, value);
		}
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void add(K key, V value) {
		if (key == null || value == null) {
			return;
		}

		synchronized (syncLock) {
			queue.add(key);
			hashMap.put(key, value);
		}
	}
	
	/**
	 * set
	 * @param key
	 * @param value
	 */
	public void set(K key, V value) {
		if (key == null || value == null) {
			return;
		}
		synchronized (syncLock) {
			if (queue.contains(key) == false) {
				queue.add(key);
			}
			hashMap.put(key, value);
		}
	}
	
	/**
	 * 集合是否为
	 * @return
	 */
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	/**
	 * 获取Value对象
	 * @param key
	 * @return
	 */
	public V get(K key) {
		return hashMap.get(key);
	}
	
	/**
	 * 是否存在队列
	 * @param key
	 * @return
	 */
	public boolean contains(K key) {
		return queue.contains(key);
	}
	
	public int size() {
		return hashMap.size();
	}
	

}
