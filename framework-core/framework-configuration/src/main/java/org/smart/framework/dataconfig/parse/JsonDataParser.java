package org.smart.framework.dataconfig.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart.framework.dataconfig.IConfigBean;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		List<String> arr = JSON.parseArray(jsonString,String.class);
		for (int i = 0; i < arr.size(); i++) {
			String s = arr.get(i);
			T t = JSON.parseObject(s,className, Feature.SupportNonPublicField,Feature.SupportArrayToBean,Feature.UseObjectArray);
			if(t.findIdentifyKey() == null){
				throw new RuntimeException(String.format("null config key::%s, class:%s", t.findIdentifyKey(), className));
			}
			if(objList.containsKey(t.findIdentifyKey())){
				throw new RuntimeException(String.format("duplicate config key:%s, class:%s", t.findIdentifyKey().toString(), className));
			}
			objList.put(t.findIdentifyKey(), t);
		}
//		for (String s : arr) {
//			T t = null;
//			try {
//				t = JSON.parseObject(s,className, Feature.SupportNonPublicField,Feature.SupportArrayToBean,Feature.UseObjectArray);
//			} catch (Exception e) {
//				LOGGER.error("parse json error, className:{}, json:{}, index:{}", className.getName(),jsonString, index);
//				throw new RuntimeException(e);
//			}
//			if(t.findIdentifyKey() == null){
//				throw new RuntimeException(String.format("null config key::%s, class:%s", t.findIdentifyKey(), className));
//			}
//			if(objList.containsKey(t.findIdentifyKey())){
//				throw new RuntimeException(String.format("duplicate config key:%s, class:%s", t.findIdentifyKey().toString(), className));
//			}
//			objList.put(t.findIdentifyKey(), t);
//		}
		return objList;
	}
	

}
