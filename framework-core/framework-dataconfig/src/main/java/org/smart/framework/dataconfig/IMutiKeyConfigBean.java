package org.smart.framework.dataconfig;

import java.util.List;

import org.smart.framework.util.IdentiyKey;

public interface IMutiKeyConfigBean extends IConfigBean{
	/**
	 * 缓存key列表
	 * @return
	 */
	List<IdentiyKey> findCacheKeys();
}
