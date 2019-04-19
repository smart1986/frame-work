package org.smart.framework.datacenter;

import java.util.ArrayList;
import java.util.List;

public class DataKeyValue<K,V> {
	private List<K> keys = new ArrayList<>();
	private List<V> values = new ArrayList<>();
	
	public void add(K key, V value){
		keys.add(key);
		values.add(value);
	}
	
	public List<K> getKeys() {
		return keys;
	}
	
	public List<V> getValues() {
		return values;
	}
	public V remove(K key) {
		int index = this.keys.indexOf(key);
		this.keys.remove(key);
		return values.remove(index);
	}
}
