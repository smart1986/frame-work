package org.smart.framework.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 字符串工具类
 * 
 * @author smart
 *
 */
public class StringUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);

	public static Pattern pattern = Pattern.compile("[0-9]*");

	/**
	 * 分隔符字符串转换为数组对象 默认分隔符为 "|" 和 ","
	 * 
	 * @param delimiterString
	 *            1,2,3,4,5|6,7,8,9
	 * @return
	 */
	public static List<String[]> delimiterString2Array(String delimiterString) {
		if ((delimiterString == null) || (delimiterString.trim().length() == 0)) {
			return new ArrayList<>();
		}

		String[] ss = split(delimiterString.trim(), Splitable.ELEMENT_SPLIT);
		if ((ss != null) && (ss.length > 0)) {
			List<String[]> list = new ArrayList<String[]>();
			for (int i = 0; i < ss.length; ++i) {
				if (isNotBlank(ss[i])) {
					String[] sss = split(ss[i], Splitable.BETWEEN_DOT);
					list.add(sss);
				}
			}
			return list;
		}
		return new ArrayList<>();
	}

	public static List<String[]> delimiterString2Array(String delimiterString, String element,
			String attribute) {
		if ((delimiterString == null) || (delimiterString.trim().length() == 0)) {
			return new ArrayList<>();
		}

		String[] ss = split(delimiterString.trim(), element);
		if ((ss != null) && (ss.length > 0)) {
			List<String[]> list = new ArrayList<String[]>();
			for (int i = 0; i < ss.length; ++i) {
				if (isNotBlank(ss[i])) {
					String[] sss = split(ss[i], attribute);
					list.add(sss);
				}
			}
			return list;
		}
		return new ArrayList<>();
	}

	public static List<int[]> delimiterString2IntArray(String delimiterString) {
		if ((delimiterString == null) || (delimiterString.trim().length() == 0)) {
			return new ArrayList<>();
		}

		String[] ss = split(delimiterString.trim(), Splitable.ELEMENT_SPLIT);
		List<int[]> list = new ArrayList<int[]>();
		if (ss != null) {
			for (String s : ss) {
				String subItems[] = split(s, Splitable.BETWEEN_DOT);
				int[] res = new int[subItems.length];
				int i = 0;
				for (String item : subItems) {
					res[i++] = Integer.valueOf(item);
				}
				list.add(res);
			}
		}
		return list;
	}

	/**
	 * 字符串转换Map<Integer,Integer>对象
	 * 
	 * <pre>
	 * key,value|key,value
	 * </pre>
	 * 
	 * @param delimiterString
	 * @return
	 */
	public static Map<Integer, Integer> delimiterString2IntMap(String delimiterString) {
		if ((delimiterString == null) || (delimiterString.trim().length() == 0)) {
			return new ConcurrentHashMap<>();
		}

		String[] ss = split(delimiterString.trim(), Splitable.ELEMENT_SPLIT);
		Map<Integer, Integer> map = new ConcurrentHashMap<Integer, Integer>();
		if ((ss != null) && (ss.length > 0)) {
			for (int i = 0; i < ss.length; ++i) {
				String[] vals = split(ss[i], Splitable.BETWEEN_DOT);
				if (vals.length < 1) {
					continue;
				}
				try {
					map.put(Integer.valueOf(vals[0]), Integer.valueOf(vals[1]));
				} catch (Exception e) {
					LOGGER.error("{}", e);
				}
			}
		}
		return map;
	}

	/**
	 * 字符串转换Map<Long,Integer>对象
	 * 
	 * <pre>
	 * key,value|key,value
	 * </pre>
	 * 
	 * @param delimiterString
	 * @return
	 */
	public static Map<Long, Integer> delimiterString2Long_IntMap(String delimiterString) {
		if ((delimiterString == null) || (delimiterString.trim().length() == 0)) {
			return new ConcurrentHashMap<>();
		}

		String[] ss = split(delimiterString.trim(), Splitable.ELEMENT_SPLIT);
		Map<Long, Integer> map = new HashMap<>();
		if ((ss != null) && (ss.length > 0)) {
			for (int i = 0; i < ss.length; ++i) {
				String[] vals = split(ss[i], Splitable.BETWEEN_DOT);
				if (vals.length < 1) {
					continue;
				}
				try {
					map.put(Long.valueOf(vals[0]), Integer.valueOf(vals[1]));
				} catch (Exception e) {
					LOGGER.error("{}", e);
				}
			}
		}
		return map;
	}

	/**
	 *
	 * @param keyValueString
	 * @return
	 */
	public static Map<Integer, String> delimiterString2StringMap(String keyValueString) {
		if (keyValueString == null || keyValueString.trim().length() == 0) {
			return new ConcurrentHashMap<>();
		}

		String[] itemArray = split(keyValueString.trim(), Splitable.ELEMENT_SPLIT);
		Map<Integer, String> map = new HashMap<>();
		if (itemArray != null && itemArray.length > 0) {
			for (int i = 0; i < itemArray.length; ++i) {
				String[] vals = split(itemArray[i], Splitable.BETWEEN_DOT);
				if (vals.length < 1) {
					continue;
				}
				map.put(Integer.valueOf(vals[0]), vals[1]);
			}
		}
		return map;
	}

	/**
	 * 字符串转换List对象
	 * 
	 * <pre>
	 * split参数建议用
	 * {@code Splitable.ELEMENT_SPLIT} 和{@code Splitable.ELEMENT_DELIMITER}
	 * </pre>
	 * 
	 * @param delimiterString
	 *            分隔字符串
	 * @param clazz
	 *            目标对象类型
	 * @param split
	 *            分隔符
	 * @return
	 */
	public static <K> List<K> delimiterString2List(String delimiterString, Class<K> clazz,
			String split) {
		if ((delimiterString == null) || (delimiterString.trim().length() == 0)) {
			return new ArrayList<>();
		}

		List<K> list = new ArrayList<K>();
		String[] ss = split(delimiterString.trim(), split);
		if (ss != null && ss.length > 0) {
			for (int i = 0; i < ss.length; ++i) {
				try {
					list.add(valueOf(ss[i], clazz));
				} catch (Exception e) {
					LOGGER.error("{}", e);
				}
			}
		}
		return list;
	}

	/**
	 * 字符串转List<String>
	 * 
	 * @param delimiterString
	 * @param split
	 * @return
	 */
	public static List<String> delimiterString2List(String delimiterString, String split) {
		if ((delimiterString == null) || (delimiterString.trim().length() == 0)) {
			return new ArrayList<>();
		}

		List<String> list = new ArrayList<String>();
		String[] ss = split(delimiterString.trim(), split);
		if (ss != null && ss.length > 0) {
			for (String str : ss) {
				list.add(new String(str));
			}
		}
		return list;
	}

	/**
	 * 字符串转List<Integer>
	 * 
	 * @param delimiterString
	 * @param split
	 * @return
	 */
	public static List<Integer> delimiterString2IntList(String delimiterString, String split) {
		if ((delimiterString == null) || (delimiterString.trim().length() == 0)) {
			return new ArrayList<>();
		}

		List<Integer> list = new ArrayList<Integer>();
		String[] ss = split(delimiterString.trim(), split);
		if (ss != null && ss.length > 0) {
			for (String str : ss) {
				list.add(Integer.valueOf(str));
			}
		}
		return list;
	}

	/**
	 * 字符串转Set<Long>
	 * 
	 * @param delimiterString
	 * @param split
	 * @return
	 */
	public static Set<Long> delimiterString2LongSet(String delimiterString, String split) {
		if ((delimiterString == null) || (delimiterString.trim().length() == 0)) {
			return Collections.synchronizedSet(new HashSet<Long>());
		}

		Set<Long> set = Collections.synchronizedSet(new HashSet<Long>());
		String[] ss = delimiterString.trim().split(split);
		if (ss != null && ss.length > 0) {
			for (String str : ss) {
				set.add(Long.parseLong(str));
			}
		}
		return set;
	}

	/**
	 * 字符串转Map<Long, Map<Integer, Integer>> 字符串的格式是:
	 * key,subKey,subVal,subKey,subVal|key,subKey,subVal,subKey,subVal
	 * 
	 * @param delimiterString
	 * @param split
	 * @return
	 */
	public static Map<Long, Map<Integer, Integer>> delimiterString2LongIntMap(
			String delimiterString) {
		if ((delimiterString == null) || (delimiterString.trim().length() == 0)) {
			return new ConcurrentHashMap<>();
		}

		Map<Long, Map<Integer, Integer>> map = new HashMap<>();
		String[] ss = split(delimiterString.trim(), Splitable.ELEMENT_SPLIT);
		if (ss != null && ss.length > 0) {
			for (String str : ss) {
				String subStrs[] = split(str, Splitable.BETWEEN_DOT);
				Long key = Long.parseLong(subStrs[0]);
				Map<Integer, Integer> subMap = new HashMap<>();
				for (int i = 1; i < subStrs.length; i += 2) {
					Integer subKey = Integer.valueOf(subStrs[i]);
					Integer subVal = Integer.valueOf(subStrs[i + 1]);
					subMap.put(subKey, subVal);
				}
				map.put(key, subMap);
			}
		}
		return map;
	}

	/**
	 * 将Map<Long, Map<Integer, Integer>> 转化成字符串 字符串的格式:
	 * key,subKey,subVal,subKey,subVal|key,subKey,subVal,subKey,subVal
	 * 
	 * @param map
	 * @return
	 */
	public static String longIntMap2delimiterString(Map<Long, Map<Integer, Integer>> map) {
		if (map == null || map.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (Long key : map.keySet()) {
			Map<Integer, Integer> subMap = map.get(key);
			if (subMap == null || subMap.isEmpty()) {
				continue;
			}
			sb.append(key).append(Splitable.BETWEEN_DOT);
			for (Entry<Integer, Integer> entry : subMap.entrySet()) {
				sb.append(entry.getKey()).append(Splitable.BETWEEN_DOT);
				sb.append(entry.getValue()).append(Splitable.BETWEEN_DOT);
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(Splitable.ELEMENT_DELIMITER);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	/**
	 *
	 * @param str
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T valueOf(String dataOrigin, Class<T> clazz) {
		try {
			Method valueOfMethod = clazz.getMethod("valueOf", new Class[] { String.class });
			if (valueOfMethod == null) {
				return null;
			}
			T val = (T) valueOfMethod.invoke(clazz, new Object[] { dataOrigin });
			return val;
		} catch (Exception e) {
			LOGGER.error("{}", e);
			return null;
		}
	}

	/**
	 * 集合转换为指定分隔符字符串 默认分隔符为 "_" 和 "|"
	 * 
	 * @param collection
	 * @return
	 */
	public static String delimiterCollection2String(Collection<String[]> collection) {
		if (collection == null || collection.isEmpty()) {
			return "";
		}

		StringBuffer subContent = new StringBuffer();
		for (String[] strings : collection) {
			if (strings == null) {
				continue;
			}
			if (strings.length == 0) {
				continue;
			}

			for (int i = 0; i < strings.length; ++i) {
				if (i == strings.length - 1) {
					subContent.append(strings[i]).append(Splitable.ELEMENT_DELIMITER);
				} else {
					subContent.append(strings[i]).append(Splitable.BETWEEN_DOT);
				}
			}
		}
		return subContent.toString();
	}

	/**
	 * 集合转换为指定分隔符字符串
	 * 
	 * @param collection<T>
	 * @return
	 */
	public static <T> String delimiterTCollection2String(Collection<T> collection, String split) {
		if (collection == null || collection.isEmpty()) {
			return "";
		}
		StringBuffer subContent = new StringBuffer();
		for (T clazz : collection) {
			String model = clazz.toString();
			if (model.isEmpty()) {
				continue;
			}
			subContent.append(model).append(split);
		}
		subContent.deleteCharAt(subContent.length() - 1);
		return subContent.toString();
	}

	/**
	 * 集合转换为指定分隔符字符串 默认分隔符为 "," 和 "|"
	 * 
	 * @param collection<T>
	 * @return
	 */
	public static <T> String delimiterTCollection2String(Collection<T> collection) {
		return delimiterTCollection2String(collection, Splitable.ELEMENT_DELIMITER);
	}

	/**
	 * 数组转换为字符串 默认分隔符为 "," 和 "|"
	 * 
	 * @param subArray
	 * @return
	 */
	public static String array2DelimiterString(String[] subArray) {
		if ((subArray == null) || (subArray.length == 0)) {
			return "";
		}

		StringBuffer subContent = new StringBuffer();

		for (int i = 0; i < subArray.length; ++i) {
			subContent.append(subArray[i]).append(Splitable.BETWEEN_DOT);
		}

		String tmp = subContent.toString().substring(0,
				subContent.lastIndexOf(Splitable.BETWEEN_DOT));

		return tmp + Splitable.ELEMENT_DELIMITER;
	}

	public static String intArray2DelimiterString(List<Integer> intList) {
		if (intList == null || intList.size() == 0) {
			return "";
		}

		StringBuffer subContent = new StringBuffer();

		for (int i = 0; i < intList.size(); ++i) {
			subContent.append(intList.get(i)).append(Splitable.BETWEEN_DOT);
		}

		return subContent.toString().substring(0, subContent.lastIndexOf(Splitable.BETWEEN_DOT));
	}

	public static String objectArray2DelimiterString(List<Object> list) {
		if (list == null || list.size() == 0) {
			return "";
		}

		StringBuffer subContent = new StringBuffer();

		for (int i = 0; i < list.size(); ++i) {
			subContent.append(list.get(i)).append(Splitable.BETWEEN_DOT);
		}

		return subContent.toString().substring(0, subContent.lastIndexOf(Splitable.BETWEEN_DOT));
	}

	/**
	 * map转换为字符串
	 * 
	 * @param map
	 * @return
	 */
	public static String map2DelimiterString(Map<? extends Object, ? extends Object> map) {
		return map2DelimiterString(map, Splitable.DELIMITER_ARGS, Splitable.BETWEEN_DOT);
	}

	/**
	 * map转换为字符串
	 * 
	 * @param map
	 *            map中实体toString方法
	 * @param attributeSplit
	 *            {@code Splitable.ATTRIBUTE_SPLIT}
	 * @param elementSplit
	 *            {@code Splitable.ELEMENT_DELIMITER}
	 * @return
	 */
	public static String map2DelimiterString(Map<? extends Object, ? extends Object> map,
			String attributeSplit, String elementSplit) {
		StringBuilder builder = new StringBuilder();

		if (map == null || map.isEmpty()) {
			return builder.toString();
		}

		for (Map.Entry<?, ?> entry : map.entrySet()) {
			builder.append(entry.getKey().toString()).append(attributeSplit)
					.append(entry.getValue().toString()).append(elementSplit);
		}

		builder.deleteCharAt(builder.length() - 1);

		return builder.toString();
	}

	/**
	 * 将map转成string, 只要map的key和value的类型都是Number的子类即可
	 * 
	 * @param map
	 * @return
	 */
	public static <T extends Number, S extends Number> String numberMap2String(Map<T, S> map) {
		StringBuilder builder = new StringBuilder();

		if (map == null || map.isEmpty()) {
			return builder.toString();
		}

		for (Map.Entry<T, S> entry : map.entrySet()) {
			builder.append(entry.getKey()).append(Splitable.BETWEEN_DOT).append(entry.getValue())
					.append(Splitable.ELEMENT_DELIMITER);
		}

		builder.deleteCharAt(builder.length() - 1);

		return builder.toString();
	}

	/**
	 * 列表转换为字符串 默认分隔符为 "|" 和 "_"
	 * 
	 * @param subArrayList
	 * @return
	 */
	public static String listArray2DelimiterString(List<String[]> subArrayList) {
		if ((subArrayList == null) || (subArrayList.isEmpty())) {
			return "";
		}

		StringBuffer subContent = new StringBuffer();
		for (String[] strings : subArrayList) {
			if (strings == null) {
				continue;
			}
			if (strings.length == 0) {
				continue;
			}

			for (int i = 0; i < strings.length; ++i) {
				if (i == strings.length - 1) {
					subContent.append(strings[i]).append(Splitable.ELEMENT_DELIMITER);
				} else {
					subContent.append(strings[i]).append(Splitable.BETWEEN_DOT);
				}
			}
		}
		return subContent.toString();
	}

	/**
	 * 分隔符字符串转换为Set<Integer>集合
	 * 
	 * <pre>
	 * 分隔符： _  {@code Splitable.ATTRIBUTE_SPLIT}
	 * </pre>
	 * 
	 * @param delimiterString
	 *            11,12,13,14
	 * @return
	 */
	public static Set<Integer> splitString2Set(String splitString) {
		Set<Integer> set = new HashSet<Integer>();
		if (isNotBlank(splitString)) {
			String[] stringArray = split(splitString, Splitable.BETWEEN_DOT);

			for (String str : stringArray) {
				if (isNotBlank(str)) {
					set.add(Integer.valueOf(str));
				}
			}
		}

		return set;
	}

	/**
	 * Set<Integer>集合转换为分隔符字符串
	 * 
	 * @param set
	 * @return
	 */
	public static String Set2SplitString(Set<? extends Number> set, String split) {
		StringBuilder builder = new StringBuilder("");
		for (Number i : set) {
			builder.append(i).append(split);
		}
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}

		return builder.toString();
	}

	/**
	 * 集合转换成字符串
	 * 
	 * @param collection
	 * @param splitString
	 *            {@code Splitable}}
	 * @return
	 */
	public static String collection2SplitString(Collection<? extends Object> collection,
			String splitString) {
		StringBuilder builder = new StringBuilder();
		if (collection == null || collection.isEmpty()) {
			return builder.toString();
		}
		for (Object obj : collection) {
			if (obj != null) {
				builder.append(obj.toString()).append(splitString);
			}
		}
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}

		return builder.toString();
	}

	public static boolean isBlank(String str) {
		if (str == null || "".equals(str.trim())) {
			return true;
		}
		return false;
	}

	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

	public static boolean isNumeric(String str) {
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	/**
	 * byte[] 转 String
	 * 
	 * @param bytes
	 * @return
	 */
	public static String inputStream2String(InputStream stream) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = -1;
		String returnString = "";
		try {
			while ((length = stream.read(buffer)) != -1) {
				bos.write(buffer, 0, length);
			}
			bos.close();
			stream.close();

			returnString = bos.toString().trim();
		} catch (Exception ex) {
			LOGGER.error("{}", ex);
		}

		return returnString;
	}

	/**
	 * 数组转字符串
	 * 
	 * @param arr
	 * @return
	 */
	public static String array2String(Object[] arr) {
		StringBuffer sb = new StringBuffer();
		for (Object object : arr) {
			sb.append(object.toString());
			sb.append(",");
		}
		if (sb.length() > 1) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * 填充指定数组到指定长度
	 * 
	 * @param src
	 *            源数组
	 * @param len
	 *            指定长度
	 * @param content
	 *            填充内容
	 * @return 生成的新数组 如果长度小于源数组长度，返回源数组
	 */
	public static String[] fillStringArray(String[] src, int len, String content) {
		if (src == null || src.length >= len) {
			return src;
		}

		if (content == null) {
			content = "";
		}
		String[] data = null;
		data = new String[len];
		for (int i = 0; i < len; i++) {
			if (i >= src.length) {
				data[i] = content;
			} else {
				if (src[i].isEmpty()) {
					src[i] = content;
				}
				data[i] = src[i];
			}
		}
		return data;
	}

	public static String[] split(String src, String splitable) {
		if (isBlank(src) || isBlank(splitable)) {
			return null;
		}

		String[] items = src.split(splitable);
		String[] result = new String[items.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = new String(items[i]);
		}

		return result;
	}

	/**
	 * <p>
	 * Gets the String that is nested in between two Strings. Only the first match
	 * is returned.
	 * </p>
	 *
	 * <p>
	 * A {@code null} input String returns {@code null}. A {@code null} open/close
	 * returns {@code null} (no match). An empty ("") open and close returns an
	 * empty string.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.substringBetween("wx[b]yz", "[", "]") = "b"
	 * StringUtils.substringBetween(null, *, *)          = null
	 * StringUtils.substringBetween(*, null, *)          = null
	 * StringUtils.substringBetween(*, *, null)          = null
	 * StringUtils.substringBetween("", "", "")          = ""
	 * StringUtils.substringBetween("", "", "]")         = null
	 * StringUtils.substringBetween("", "[", "]")        = null
	 * StringUtils.substringBetween("yabcz", "", "")     = ""
	 * StringUtils.substringBetween("yabcz", "y", "z")   = "abc"
	 * StringUtils.substringBetween("yabczyabcz", "y", "z")   = "abc"
	 * </pre>
	 *
	 * @param str
	 *            the String containing the substring, may be null
	 * @param open
	 *            the String before the substring, may be null
	 * @param close
	 *            the String after the substring, may be null
	 * @return the substring, {@code null} if no match
	 * @since 2.0
	 */
	public static String substringBetween(String str, String open, String close) {
		if (str == null || open == null || close == null) {
			return null;
		}
		int start = str.indexOf(open);
		if (start != -1) {
			int end = str.indexOf(close, start + open.length());
			if (end != -1) {
				return str.substring(start + open.length(), end);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <N extends Number> N string2Number(String src, Class<N> clz) {
		System.out.println(clz.getSimpleName());
		if (clz.getSimpleName().equals(Integer.class.getSimpleName())) {
			return (N) Integer.valueOf(src);
		}
		if (clz.getSimpleName().equals(Byte.class.getSimpleName())) {
			return (N) Byte.valueOf(src);
		}
		if (clz.getSimpleName().equals(Short.class.getSimpleName())) {
			return (N) Short.valueOf(src);
		}
		if (clz.getSimpleName().equals(Long.class.getSimpleName())) {
			return (N) Long.valueOf(src);
		}
		if (clz.getSimpleName().equals(Float.class.getSimpleName())) {
			return (N) Float.valueOf(src);
		}
		if (clz.getSimpleName().equals(Double.class.getSimpleName())) {
			return (N) Double.valueOf(src);
		}
		return null;
	}

	public static String randomUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

}
