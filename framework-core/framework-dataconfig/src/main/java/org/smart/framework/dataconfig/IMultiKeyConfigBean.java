package org.smart.framework.dataconfig;

import java.util.List;

import org.smart.framework.util.IdentifyKey;

public interface IMultiKeyConfigBean extends IConfigBean{
	/**
	 * 缓存key列表
	 * @return
	 */
	List<IdentifyKey> findCacheKeys();
}
