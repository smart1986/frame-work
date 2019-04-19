package org.smart.framework.util.rank.cache;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.smart.framework.util.rank.AbstractRank;

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
 * @author ryan
 *
 */
public abstract class MapAbstractRankCache<K,V extends AbstractRank<K>> extends PageSegmentable {

	private ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>(128,0.75f,64);
	// cache ranks,  K: rank   V gamerId
	private ConcurrentHashMap<Long, K> gamerRank = new ConcurrentHashMap<>(128,0.75f,64);
	private int rankLimit;
	public MapAbstractRankCache(int pageSize) {
		super(pageSize);
		this.rankLimit = 0;
	}
	public MapAbstractRankCache(int pageSize, int rankLimit) {
		super(pageSize);
		this.rankLimit = rankLimit;
	}
	

	public abstract Collection<V> findRanks(int page);


	public V findByRank(long rank){
		K key = gamerRank.get(rank);
		if(key == null){
			return null;
		}
		return cache.get(key);
	}

	protected boolean hasRankLimit(){
		if(findRankLimit() == 0){
			return false;
		}
		return findLastRank() >= findRankLimit();
	}

	public long findRankLimit(){
		return rankLimit;
	}


	public abstract long findLastRank();

	public boolean canEnterRank(V v){
		return true;
	}

	public int findSize(){
		return cache.size();
	}

	public int findPages(){
		int size = findSize();
		return size % findPageSegment() == 0 ? size / findPageSegment() : size / findPageSegment() + 1;
	}

	public V find(K key) {
		if(key == null){
			return null;
		}
		return cache.get(key);
	}

	public List<V> findByKeys(Collection<K> keys) {
		if(keys == null || keys.size() == 0){
			return null;
		}
		List<V> vs = Lists.newArrayList();
		for (K key : keys){
			V v = find(key);
			if(v != null){
				vs.add(v);
			}
		}
		return vs;
	}

	public List<V> findAll() {
		return Lists.newArrayList(cache.values());
	}

	public boolean put(V value) {
		if(value == null){
			return false;
		}
		gamerRank.put(value.getRank(),value.getKey());
		cache.put(value.getKey(),value);
		return true;
	}

	public boolean clean() {
		cache.clear();
		gamerRank.clear();
		return true;
	}

	public boolean hasKey(K key) {
		return cache.containsKey(key);
	}

	public boolean remove(V value) {
		if(value == null){
			return false;
		}
		cache.remove(value.getKey());
		gamerRank.remove(value.getRank());
		return true;
	}

}
