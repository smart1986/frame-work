package org.smart.framework.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机工具类
 * 
 * @author jerry
 *
 */
public class RandomUtils {

	// private static Random random = new Random();

	/**
	 * 随机随机范围的索引
	 * 
	 * @param size
	 *            大小
	 * @return
	 */
	public static int nextIntIndex(int size) {
		return nextInt(0, size - 1);
	}

	/**
	 * 随机范围值 minValue到 maxValue的闭区间
	 * 
	 * @param minValue
	 * @param maxValue
	 * @return
	 */
	public static int nextInt(int minValue, int maxValue) {
		// if (maxValue < minValue) {
		// maxValue = minValue;
		// }
		// return (int) Math.floor((Math.random() * (maxValue - minValue + 1) +
		// minValue));

		if (maxValue - minValue <= 0) {
			return minValue;
		}

		return minValue + ThreadLocalRandom.current().nextInt(maxValue - minValue + 1);
	}

	/**
	 * 是否命中
	 * 
	 * @param rate
	 *            概率
	 * @param maxValue
	 *            最大值
	 * @return
	 */
	public static boolean isHit(int rate, int maxValue) {
		return nextInt(0, maxValue) <= rate;
	}

	/**
	 * 0 - 100数值范围内(包含100)是否命中
	 * 
	 * @param rate
	 * @return
	 */
	public static boolean is100Hit(int rate) {
		return isHit(rate, 100);
	}

	/**
	 * 0 - 1000数值范围内(包含100)是否命中
	 * 
	 * @param rate
	 * @return
	 */
	public static boolean is1000Hit(int rate) {
		return isHit(rate, 1000);
	}

	/**
	 * 随机一个范围内[minValue,maxValue]不重复的数字
	 * 
	 * @param num
	 *            数字个数
	 * @param minValue
	 *            最小值
	 * @param maxValue
	 *            最大值
	 * @return 随机数组
	 */
	public static int[] uniqueRandom(int num, int minValue, int maxValue) {
		int length = maxValue - minValue + 1;
		int[] seed = new int[length];
		for (int i = 0; i < length; i++) {
			seed[i] = minValue + i;
		}

		num = Math.min(num, length);

		int[] ranArr = new int[num];
		for (int i = 0; i < num; i++) {
			int j = nextIntIndex(length - i);
			ranArr[i] = seed[j];
			seed[j] = seed[length - i - 1];
		}
		return ranArr;
	}

	/**
	 * 根据概率配置,返回随机命中的ID 比如: ID1_500|ID2_500 每个id都有50%概率出现,通过randomHit返回随机命中的ID
	 * 
	 * @param base
	 *            概率的最大值
	 * @param map
	 *            ID和对应的出现概率
	 * @return
	 */
	public static <ID> ID randomHit(int base, Map<ID, Integer> map) {
		int rate = nextInt(0, base);
		int total = 0;
		for (Entry<ID, Integer> entry : map.entrySet()) {
			if (entry.getValue() == 0) {
				continue;
			}
			total += entry.getValue();
			if (total >= rate) {
				return entry.getKey();
			}
		}
		throw new RuntimeException("概率总和不能到1");
	}

	public static <ID> ID randomWeightHit(Map<ID, Integer> map) {
		Collection<Integer> weight = map.values();
		Integer weightSum = 0;
		for (Integer w : weight) {
			weightSum += w;
		}
		Integer n = ThreadLocalRandom.current().nextInt(weightSum); // n in [0, weightSum)
		Integer m = 0;
		for (Map.Entry<ID, Integer> entry : map.entrySet()) {
			if (m <= n && n < m + entry.getValue()) {
				return entry.getKey();
			}
			m += entry.getValue();
		}
		return null;
	}

	public static <T> Collection<T> randomValue(Collection<T> targets, int num) {
		if (targets == null || targets.isEmpty()) {
			return null;
		}
		if (num <= 0) {
			return null;
		}
		if (num >= targets.size()) {
			return targets;
		}
		List<T> list = new ArrayList<T>();
		list.addAll(targets);
		List<T> result = new ArrayList<T>();
		for (int i = 0; i < num; i++) {
			int index = nextIntIndex(list.size());
			T r = list.remove(index);
			result.add(r);
		}
		return result;
	}

	public static <T> T randomValue(Collection<T> targets) {
		if (targets == null || targets.isEmpty()) {
			return null;
		}
		List<T> list = new ArrayList<T>();
		list.addAll(targets);
		int index = nextIntIndex(list.size());
		return list.get(index);
	}

	public static boolean nextBoolean() {
		return nextInt(0, 1) == 0 ? false : true;
	}

}