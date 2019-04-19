package org.smart.framework.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CollectionUtils {
	/**
	 * 获取子列表
	 * 
	 * @param src
	 *            原列表
	 * @param start
	 *            开始索引
	 * @param num
	 *            数量
	 * @return
	 */
	public static <T> List<T> getSubList(List<T> src, int start, int num) {
		int begin = start;
		int end = num + start >= src.size() ? src.size() : num + start;
		return src.subList(begin, end);
	}

	/**
	 * 获取子列表
	 * @param src 原列表
	 * @param start 开始对象
	 * @param num 数量
	 * @return
	 */
	public static <T> List<T> getSubList(List<T> src, T start, int num) {
		int begin = 0;
		if (start != null) {
			int index = src.indexOf(start);
			if (index >= 0) {
				begin = index;
			}
		}
		int end = begin + num >= src.size() ? src.size() : num + begin;
		return src.subList(begin, end);
	}
	
	public static <K> Map<K, Integer> mergeIntegerMap(Map<K,Integer> ... maps){
		Map<K, Integer> result = new HashMap<>();
		for (Map<K, Integer> map : maps) {
			for (Entry<K, Integer> entry : map.entrySet()) {
				K key = entry.getKey();
				Integer value = entry.getValue();
				if (result.containsKey(key)) {
					result.put(key, result.get(key) + value);
				} else {
					result.put(key, value);
				}
			}
		}
		return result;
	}
}
