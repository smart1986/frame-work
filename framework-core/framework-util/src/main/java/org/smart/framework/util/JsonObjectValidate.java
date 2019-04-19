package org.smart.framework.util;

import com.alibaba.fastjson.JSONObject;

public class JsonObjectValidate {
	/**
	 * 验证json中是否存在key
	 * @param jsonObject
	 * @param paramName
	 * @return
	 */
	public static boolean paramValidate(JSONObject jsonObject, String... paramName){
		if (jsonObject == null) {
			return false;
		}
		if (paramName.length == 0) {
			return false;
		}
		for (String name : paramName) {
			if (!jsonObject.containsKey(name)){
				return false;
			}
		}
		
		return true;
	}
}
