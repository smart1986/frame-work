package org.smart.framework.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringTokenizerUtil {
	/**
	 * 字符串转换map
	 * 形如：a_b|c_d|e_f
	 * @param src
	 * @param firstToken 第一层分隔符
	 * @param secondToken 第二层分隔符
	 * @return
	 */
	public static  Map<String, String> string2Map(String src, String firstToken,String secondToken) {
		Map<String, String> map = new HashMap<>();
		if (StringUtils.isBlank(src)) {
			return map;
		}
		if (StringUtils.isBlank(firstToken)) {
			return map;
		}
		if (StringUtils.isBlank(secondToken)){
			return map;
		}
		if (firstToken.equals(secondToken)){
			return map;
		}
		String[] tmp = src.split(firstToken);
		for (String str : tmp) {
			String[] tmp1 = str.split(secondToken);
			if(tmp1.length == 2){
				map.put(tmp1[0], tmp1[1]);
			}
			
		}
		
		return map;
	}
	/**
	 * 字符串转list
	 * 形如:a_b_c_d|e_f_g_h|i_j_k_l
	 * @param src
	 * @param firstToken
	 * @param secondToken
	 * @return
	 */
	public List<List<String>> string2List(String src, String firstToken,String secondToken){
		List<List<String>> list = new ArrayList<>();
		if (StringUtils.isBlank(src)) {
			return list;
		}
		if (StringUtils.isBlank(firstToken)) {
			return list;
		}
		if (StringUtils.isBlank(secondToken)) {
			return list;
		}
		if (firstToken.equals(secondToken)){
			return list;
		}
		
		String[] tmp = src.split(firstToken);
		for (String str : tmp) {
			String[] tmp1 = str.split(secondToken);
			List<String> listTmp = new ArrayList<>();
			for (String string : tmp1) {
				listTmp.add(string);
			}
			list.add(listTmp);
		}
		
		return list;
		
	}
	/**
	 * map转number类型
	 * @param src
	 * @param keyClz
	 * @param valueClz
	 * @return
	 */
	public static <K extends Number, V extends Number> Map<K, V> toNumberMap(Map<String,String> src, Class<K> keyClz,Class<V> valueClz){
		Map<K,V> map = new HashMap<>();
		for (Map.Entry<String, String> entry : src.entrySet()) {
			map.put(StringUtils.string2Number(entry.getKey(), keyClz), StringUtils.string2Number(entry.getValue(), valueClz));
		}
		return map;
	}
	/**
	 * List转number类型
	 * @param src
	 * @param clz
	 * @return
	 */
	public static <T extends Number> List<T> toNumberMap(List<String> src, Class<T> clz){
		List<T> list = new ArrayList<>();
		for (String element : src) {
			list.add(StringUtils.string2Number(element, clz));
		}
		return list;
	}
	
	
	
}
