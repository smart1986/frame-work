package org.smart.framework.net.helper;

import javax.crypto.spec.SecretKeySpec;

import org.smart.framework.util.SecurityUtils;
/**
 * 加密key管理器
 * @author smart
 *
 */
public class CryptKeyManager {
	private static String keyStr = "qmax";
	
	private static SecretKeySpec key;
	
	/**
	 * 获取加密key
	 * @return
	 */
	public static SecretKeySpec getKey() {
		if (key == null) {
			try {
				key = SecurityUtils.getSecretKeySpec(keyStr.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return key;
	}
	
}
