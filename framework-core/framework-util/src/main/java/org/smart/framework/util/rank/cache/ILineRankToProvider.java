package org.smart.framework.util.rank.cache;

import org.smart.framework.util.rank.IRank;

public interface ILineRankToProvider<K,V extends IRank<K>> {
	V findLineRank(Object key);
	
	public void addRank(V rank);
}
