package org.smart.framework.util.oss.client.impl;

import org.smart.framework.util.oss.client.OSSLogClient;
import org.smart.framework.util.oss.model.LogBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class LogbackImpl implements OSSLogClient {
	private Logger ossLogger = LoggerFactory.getLogger("oss");
	
	@Override
	public void putOssLog(LogBean logBean) {
		ossLogger.info(JSON.toJSONString(logBean));
	}

}
