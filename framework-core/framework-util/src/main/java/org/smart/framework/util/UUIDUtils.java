package org.smart.framework.util;

import java.util.UUID;

/**
 * uuid生成类
 * 
 * @author smart
 *
 */
public class UUIDUtils {
	private static long sec = DateTimeUtils.getNow();
	private static int uid = 0;
	/**
	 * 获取uuid
	 * @param serverId 服务器id
	 * @return
	 */
	public synchronized static long getLongId(int serverId) {
		long hashId = 0L;
		uid += 1;
		
		if (uid > 0xfffff) {
			sec +=1;
			uid = 1;
		}
		
		assert(serverId >= 0 && serverId <= 0xfff);
		assert(uid >= 0 && uid <= 0xfffff);

		hashId = ((long)sec << 32);
		
		serverId &= 0xfff;
		hashId |= (long)serverId << 20;
		
		uid &= 0xfffff;
		hashId |= uid;
		return hashId ;
	}
	/**
	 * 设置uuid技术
	 * @param time
	 */
	public static void setBase(int time) {
		sec = time;
	}
	
	public static String getUUID(){
		String tmp = UUID.randomUUID().toString().replace("-", "");
		return tmp;
	}
	
	
}	
