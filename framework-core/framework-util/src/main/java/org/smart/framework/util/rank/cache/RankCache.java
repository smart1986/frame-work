package org.smart.framework.util.rank.cache;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.smart.framework.util.rank.AbstractRank;
import org.smart.framework.util.rank.Page;

import com.google.common.collect.Lists;

/**
 * rank  cache
 * 
 * calculate rank in time
 * 
 * cache daily rank
 *
 * gamerId is default key
 * 
 * 
 *
 */
public class RankCache<K,V extends AbstractRank<K>> extends MapAbstractRankCache<K,V>{
	public RankCache(int pageSize) {
		super(pageSize);
	}
	public RankCache(int pageSize, int rankLimit) {
		super(pageSize, rankLimit);
	}


	/**
	 * cache top ranks
	 */
	private ConcurrentHashMap<K, V> topRanks = new ConcurrentHashMap<>();

	@Override
	public Collection<V> findRanks(int page) {
		if(page == 1){
			return this.topRanks.values();
		}
		else {
			List<V> list = Lists.newArrayList();
			Page p = Page.valueOf(page, findPageSegment());
			for(long i= p.getStart();i<=p.getEnd();i++){
				V v = findByRank(i);
				if(v == null){
					break;
				}
				else {
					list.add(v);
				}
			}
			return list;
		}
	}

	public V findTop(K key){
		if(key == null){
			return null;
		}
		return this.topRanks.get(key);
	}

	public void removeTop(K key){
		this.topRanks.remove(key);
	}

	public int topSize(){
		return topRanks.size();
	}
	@Override
	public boolean clean() {
		topRanks.clear();
		super.clean();
		return true;
	}


	@Override
	public boolean put(V value) {
		if(hasInTop(value.getRank())){
			this.topRanks.put(value.getKey(),value);
		}
		return super.put(value);
	}

	@Override
	public long findLastRank() {
		return 0;
	}
	@Override
	public boolean remove(V value) {
		super.remove(value);
		removeTop(value.getKey());
		return true;
	}

}
