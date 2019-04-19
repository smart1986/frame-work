package org.smart.framework.util.rank;

public interface IRank<K> {
	Long getRank();
	
	K getKey();
	
	void setRank(Long rank);
	
	void setKey(K key);
	
}
