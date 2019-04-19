package org.smart.framework.util;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityUtils {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SecurityUtils.class);
	private static final String SHA1_MAC_NAME = "HmacSHA1";
	private static final String SHA1_NAME = "SHA-1";
	private static final String ENCODING_CODE = "UTF-8";

	public static String DES = "AES"; // optional value AES/DES/DESede

	public static String CIPHER_ALGORITHM = "AES"; // optional value
													// AES/DES/DESede

	/**
	 * md5加密
	 * 
	 * @param src
	 * @return
	 */
	public static String md5(String src) {
		try {
			MessageDigest alg = MessageDigest.getInstance("MD5");
			byte[] md5Bytes = alg.digest(src.getBytes(ENCODING_CODE));
			return byte2Hex(md5Bytes);
		} catch (Exception ex) {
			LOGGER.error(src, ex);
		}
		return "";
	}

	/**
	 * 使用 HMAC - SHA1 签名方法对对 encryptText 进行签名
	 * 
	 * @param encryptText
	 * @param encryptKey
	 * @return
	 */
	public static String hmacSHA1Encrypt(String encryptText, String encryptKey) {
		try {
			byte[] data = encryptKey.getBytes(ENCODING_CODE);
			// 根据给定的字节数组构造一个密钥 , 第二参数指定一个密钥算法的名称
			SecretKey secretKey = new SecretKeySpec(data, SHA1_MAC_NAME);
			// 生成一个指定 Mac 算法 的 Mac 对象
			Mac mac = Mac.getInstance(SHA1_MAC_NAME);
			// 用给定密钥初始化 Mac 对象
			mac.init(secretKey);
			byte[] text = encryptText.getBytes(ENCODING_CODE);
			// 完成 Mac 操作
			byte[] digest = mac.doFinal(text);
			return byte2Hex(digest).toLowerCase();
		} catch (Exception ex) {
			LOGGER.warn(String.format("encryptText:[%s] encryptKey:[%s]",
					encryptText, encryptKey), ex);
		}
		return "";
	}

	public static String hmacSHA1Encrypt(byte[] encryptData, String encryptKey) {
		try {
			byte[] data = encryptKey.getBytes(ENCODING_CODE);
			// 根据给定的字节数组构造一个密钥 , 第二参数指定一个密钥算法的名称
			SecretKey secretKey = new SecretKeySpec(data, SHA1_MAC_NAME);
			// 生成一个指定 Mac 算法 的 Mac 对象
			Mac mac = Mac.getInstance(SHA1_MAC_NAME);
			// 用给定密钥初始化 Mac 对象
			mac.init(secretKey);
			// 完成 Mac 操作
			byte[] digest = mac.doFinal(encryptData);
			return byte2Hex(digest).toLowerCase();
		} catch (Exception ex) {
			LOGGER.warn(String.format("encryptText:[%s] encryptKey:[%s]",
					byte2Hex(encryptData), encryptKey), ex);
		}
		return "";
	}

	public static String byte2Hex(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		String stmp = "";
		for (int n = 0; (bytes != null) && (n < bytes.length); ++n) {
			stmp = Integer.toHexString(bytes[n] & 0xFF);
			if (stmp.length() == 1)
				builder.append("0").append(stmp);
			else {
				builder.append(stmp);
			}
		}
		return builder.toString().toLowerCase();
	}

	/**
	 * sha1签名
	 * 
	 * @param encryptText
	 * @return
	 */
	public static String sha1(String encryptText) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(SHA1_NAME);
			md.update(encryptText.getBytes(ENCODING_CODE));
		} catch (Exception e) {
			LOGGER.warn("{}", e);
		}
		byte[] result = md.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : result) {
			int i = b & 0xff;
			if (i < 0xf) {
				sb.append(0);
			}
			sb.append(Integer.toHexString(i));
		}
		return sb.toString();
	}

	/**
	 * 将BigIngeger转换为指定进制的字符串
	 * 
	 * @param num
	 *            BigInteger对象
	 * @param base
	 *            进制（2，8，10，。。。。）
	 * @return
	 */
	public static String hexadecimalConversion(BigInteger num, int base) {
		String str = "", digit = "#123456789ABCDEFGHIJKLMN@PQRSTUVWXYZ";
		if (num.shortValue() == 0) {
			return "";
		} else {
			BigInteger valueOf = BigInteger.valueOf(base);
			str = hexadecimalConversion(num.divide(valueOf), base);
			return str + digit.charAt(num.mod(valueOf).shortValue());
		}
	}

	/**
	 * 转换key
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static Key getSecretKey(byte[] key) throws Exception {
		SecretKey securekey = null;
		if (key == null) {
			key = "".getBytes();
		}
		KeyGenerator keyGenerator = KeyGenerator.getInstance(DES);
		keyGenerator.init(128);
		securekey = keyGenerator.generateKey();
		return securekey;
	}
	
	/**
	 * 加密
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, SecretKeySpec secretKeySpec) throws Exception {
		  
        Cipher cipher = Cipher.getInstance("AES");// 创建密码器   
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);// 初始化 
		byte[] bt = cipher.doFinal(data);
		return bt;
	}

	public static SecretKeySpec getSecretKeySpec(byte[] key) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");   
        kgen.init(128, new SecureRandom(key));   
        SecretKey secretKey = kgen.generateKey();   
        byte[] enCodeFormat = secretKey.getEncoded();   
        SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES"); 
        return secretKeySpec;
	}
	/**
	 * 解密
	 * @param message
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] message, SecretKeySpec secretKeySpec) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");// 创建密码器   
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);// 初始化 
		byte[] res = cipher.doFinal(message);
		return res;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(md5("htbb2016"));
	}
}
