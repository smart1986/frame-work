package org.smart.framework.util.sign;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.smart.framework.util.SecurityUtils;

import com.alibaba.fastjson.JSONObject;

public class SignUtil {
	
	/**
	 * 签名
	 * 对参数进行降序key降序排列，并用"="连接key和value，"&"连接参数，最后跟上pwd做md5(降序)
	 * @param map
	 * @param pwd
	 * @return
	 */
	public static String signWithMd5(Map<String, Object> map, String pwd) {
		return signWithMd5(map,pwd, true);
	}
	
	/**
	 * 签名
	 * 对参数进行降序key降序排列，并用"="连接key和value，"&"连接参数，最后跟上pwd做md5
	 * @param map
	 * @param pwd
	 * @return
	 */
	public static String signWithMd5(Map<String, Object> map, String pwd, boolean desc) {
		Map<String, Object> tmp = new TreeMap<String, Object>(new Comparator<String>() {
			@Override
			public int compare(String obj1, String obj2) {
				// 降序排序
				if (desc) {
					return obj2.compareTo(obj1);
				} else {
					return obj2.compareTo(obj1) *(-1);
				}
			}
		});
		tmp.putAll(map);
		
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Object> entry : tmp.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		sb.append(pwd);
//		System.out.println(sb.toString());
		return SecurityUtils.md5(sb.toString());
	}
	public static Map<String, Object> sign2mapWithMd5(Map<String, Object> map, String pwd,boolean desc) {
		String sign = signWithMd5(map, pwd,desc);
		map.put("sign", sign);
		return map;
	}
	
	/**
	 * 检查签名
	 * @param request
	 * @return
	 */
	public static boolean checkSign(JSONObject request, String pwd,boolean desc){
		if (!request.containsKey("sign")) {return false;}
		String sign = request.getString("sign");
		Map<String,Object> tmp = new HashMap<>();
		for (Map.Entry<String, Object> entry : request.entrySet()) {
			if (!"sign".equals(entry.getKey())){
				tmp.put(entry.getKey(), entry.getValue());
			}
		}
		String my = SignUtil.signWithMd5(tmp, pwd,desc);
		return my.equalsIgnoreCase(sign);
	}
	/**
	 * 检查签名
	 * @param request
	 * @return
	 */
	public static boolean checkSign(Map<String,Object> request, String pwd,boolean desc){
		if (!request.containsKey("sign")) {return false;}
		String sign = (String) request.get("sign");
		Map<String,Object> tmp = new HashMap<>();
		for (Map.Entry<String, Object> entry : request.entrySet()) {
			if (!"sign".equals(entry.getKey())){
				tmp.put(entry.getKey(), entry.getValue());
			}
		}
		String my = SignUtil.signWithMd5(tmp, pwd,desc);
		return my.equalsIgnoreCase(sign);
	}
}
