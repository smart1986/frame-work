package org.smart.framework.dataconfig.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.smart.framework.dataconfig.IConfigBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;

public class JsonDataParser2 implements DataParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonDataParser2.class);

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
		for (String s : arr) {
			T t = JSON.parseObject(s,className,Feature.SupportNonPublicField,Feature.SupportArrayToBean,Feature.UseObjectArray);
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
	

}
