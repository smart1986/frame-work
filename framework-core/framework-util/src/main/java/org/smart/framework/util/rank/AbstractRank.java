package org.smart.framework.util.rank;

public abstract class AbstractRank<K> implements  IRank<K> {


	protected Long rank;

	protected K key;

	public AbstractRank() {
	}

	public AbstractRank(Long rank, K key) {
		this.rank = rank;
		this.key = key;
	}

	public abstract AbstractRank<K> copy();
	@Override
	public Long getRank() {
		return rank;
	}
	@Override
	public void setRank(Long rank) {
		this.rank = rank;
	}
	@Override
	public K getKey() {
		return key;
	}
	@Override
	public void setKey(K key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return "AbstractRank{" + "rank=" + rank + ", key=" + key + '}';
	}

}
