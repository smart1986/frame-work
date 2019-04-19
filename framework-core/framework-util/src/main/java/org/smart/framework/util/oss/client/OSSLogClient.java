package org.smart.framework.util.oss.client;

import org.smart.framework.util.oss.model.LogBean;

public interface OSSLogClient {
	/**
	 * 写入日志信息
	 * @param logBean
	 */
	void putOssLog(LogBean logBean);
}
