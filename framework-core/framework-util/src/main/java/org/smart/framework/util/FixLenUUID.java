package org.smart.framework.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.smart.framework.util.sign.Caesar;

/**
 * 根据序列号生成加密不重复id
 * @author smart
 *
 */
public class FixLenUUID {
	private Random random;
	private String table;
	
	private String strLen;

	public FixLenUUID() {
		this(8);
	}
	public FixLenUUID(int len) {
		if(len < 4){
			len = 4;
		}
		random = new Random();
		table = "ABCDEFGHIJKLMNPQRSTUVWXYZ0123456789";
		strLen = "%0"+(len-3)+"d";
	}
	
	/**
	 * 根据序号生成唯一加密id
	 * @param id
	 * @return
	 */
	public String randomId(long id) {
		String ret = null, num = String.format(strLen, id);
		int key = random.nextInt(10), seed = random.nextInt(100);
		Caesar caesar = new Caesar(table,seed);
		num = caesar.encode(key, num);
		ret = num + String.format("%01d", key) + String.format("%02d", seed);

		return ret;
	}

	public static void main(String[] args) {
		FixLenUUID r = new FixLenUUID(8);
		Set<String> set = new HashSet<>();
		for (int i = 0; i < 1000; i += 1) {
			String tmp = r.randomId(i);
			System.out.println(tmp);
			set.add(tmp);
		}
		System.out.println(set.size());
	}
}
