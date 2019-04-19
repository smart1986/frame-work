package org.smart.framework.dataconfig.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.smart.framework.dataconfig.IConfigBean;
import org.smart.framework.dataconfig.annotation.FieldIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@Deprecated
public class JsonDataParser implements DataParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonDataParser.class);

	@Override
	public <T extends IConfigBean> Map<Object, T> parse(InputStream stream, Class<T> className) {
		StringBuilder sb = new StringBuilder();
		Map<Object, T> objList = new HashMap<Object, T>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"));
			while (true) {
				String str = br.readLine();
				if (str == null) {
					break;
				}
				sb.append(str);
			}
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("{}", e);
		} catch (IOException e) {
			LOGGER.error("{}", e);
		}

		String jsonString = sb.toString();
		JSONArray jsonArray = JSON.parseArray(jsonString);
		Map<String, Field> fiedlList = getFieldList(className);

		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObj = jsonArray.getJSONObject(i);
				T t;
				try {
					t = className.newInstance();
				} catch (InstantiationException e) {
					LOGGER.error("{}",e);
					continue;
				} catch (IllegalAccessException e) {
					LOGGER.error("{}",e);
					continue;
				}
				
				Set<String> keySet = jsonObj.keySet();
				for (String string : keySet) {
					if (!jsonObj.containsKey(string)){
						LOGGER.warn(String.format("[%s]->[%s] column not exists in datafile!", className.getName(), string));
						continue;
					}
					if (fiedlList.containsKey(string)&&jsonObj.containsKey(string)){
						Field f = fiedlList.get(string);
						f.setAccessible(true);
						Object value = null;
						if (f.getType() == byte.class || f.getType() == Byte.class){
							try {
								value = jsonObj.getByteValue(string);
							} catch (Exception e) {
								value = (byte)0;
//								LOGGER.warn(String.format("[%s]->[%s] column not data null!", className.getName(), string));
							}
						}
						else if (f.getType() == short.class || f.getType() == Short.class){
							try {
								value = jsonObj.getShortValue(string);
							} catch (Exception e) {
								value = (short)0;
//								LOGGER.warn(String.format("[%s]->[%s] column not data null!", className.getName(), string));
							}
						}
						else if (f.getType() == int.class || f.getType() == Integer.class){
							try {
								value = jsonObj.getIntValue(string);
							} catch (Exception e) {
								value = 0;
//								LOGGER.warn(String.format("[%s]->[%s] column not data null!", className.getName(), string));
							}
						}
						else if (f.getType() == long.class || f.getType() == Long.class){
							try {
								value = jsonObj.getLongValue(string);
							} catch (Exception e) {
								value = 0L;
//								LOGGER.warn(String.format("[%s]->[%s] column not data null!", className.getName(), string));
							}
						}
						else if (f.getType() == float.class || f.getType() == Float.class){
							try {
								value = jsonObj.getFloatValue(string);
							} catch (Exception e) {
								value = 0.0f;
//								LOGGER.warn(String.format("[%s]->[%s] column not data null!", className.getName(), string));
							}
						}
						else if (f.getType() == double.class || f.getType() == Double.class){
							try {
								value = jsonObj.getDoubleValue(string);
							} catch (Exception e) {
								value = 0.0d;
//								LOGGER.warn(String.format("[%s]->[%s] column not data null!", className.getName(), string));
							}
						} 
						else if(f.getType() == String.class){
							try {
								value = jsonObj.getString(string);
							} catch (Exception e) {
//								LOGGER.warn(String.format("[%s]->[%s] column not data null!", className.getName(), string));
							}
						}
						else if(f.getType() == Boolean.class || f.getType() == boolean.class){
							try {
								value = jsonObj.getBoolean(string);
							} catch (Exception e) {
								value = false;
//								LOGGER.warn(String.format("[%s]->[%s] column not data null!", className.getName(), string));
							}
						}
						else {
							LOGGER.warn(String.format("[%s]->[%s] column not support type!", className.getName(), string));
						}
						
						if (value != null) {
							try {
								f.set(t, value);
							} catch (IllegalArgumentException e) {
								LOGGER.error("{}",e);
							} catch (IllegalAccessException e) {
								LOGGER.error("{}",e);
							}
						}
					} else {
						LOGGER.warn(String.format("[%s]->[%s] column not exists in class!", className.getName(), string));
					}
				}
				if(t.findIdentiyKey() == null){
					throw new RuntimeException(String.format("null config key::%s, class:%s", t.findIdentiyKey(), className));
				}
				if(objList.containsKey(t.findIdentiyKey())){
					throw new RuntimeException(String.format("duplicate config key:%s, class:%s", t.findIdentiyKey().toString(), className));
				}
				objList.put(t.findIdentiyKey(), t);
		}
		return objList;
	}
	
	private static Map<String, Field> getFieldList(Class<?> clazz) {
		Map<String, Field> fieldList = new HashMap<>();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if (field.getName().equals("serialVersionUID") || field.isAnnotationPresent(FieldIgnore.class) == false) {
				fieldList.put(field.getName(), field);
			}
		}
		return fieldList;
	}

}
