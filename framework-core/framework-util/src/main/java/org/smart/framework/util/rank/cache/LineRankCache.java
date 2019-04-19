package org.smart.framework.util.rank.cache;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.smart.framework.util.rank.IRank;
import org.smart.framework.util.rank.Page;

import com.google.common.collect.Lists;


public class LineRankCache<K,V extends IRank<K>> extends PageSegmentable{

	/**
	 * cache top ranks
	 */
	private ConcurrentHashMap<K, V> topRanks = new ConcurrentHashMap<>();

	private ReentrantLock lock = new ReentrantLock(false);
	
	private ILineRankToProvider<K,V> findLineRank;
	
	public LineRankCache(ILineRankToProvider<K,V> findLineRank) {
		this.findLineRank = findLineRank;
	}


	public Collection<V> findRanks(int page) {
		if(page == 1){
			if (this.topRanks.isEmpty()){
				List<V> list = Lists.newArrayList();
				Page p = Page.valueOf(page, findPageSegment());
				for(long i= p.getStart();i<=p.getEnd();i++){
					V v = findLineRank.findLineRank(i);
					if(v == null){
						break;
					}
					else {
						list.add(v);
					}
					topRanks.put(v.getKey(),v);
				}
				
				return this.topRanks.values();
			}
		}
		List<V> list = Lists.newArrayList();
		Page p = Page.valueOf(page, findPageSegment());
		for(long i= p.getStart();i<=p.getEnd();i++){
			V v = findLineRank.findLineRank(i);
			if(v == null){
				break;
			}
			else {
				list.add(v);
			}
		}
		return list;
	}

	public void addRank(V rank){
		if(hasInTop(rank.getRank())){
			this.topRanks.put(rank.getKey(),rank);
		}
//		put(rank);
		findLineRank.addRank(rank);
	}

	public void rankChange(V winnerRank,V loserRank){
		K winner = winnerRank.getKey();
		K loser = loserRank.getKey();
		try {
			lock.lock();
			loserRank.setKey(winner);
			winnerRank.setKey(loser);

//			put(winnerRank);
//			put(loserRank);
		}
		finally {
			lock.unlock();
		}
		if(hasInTop(loserRank.getRank())){
			this.topRanks.put(loserRank.getKey(),loserRank);
			if(hasInTop(winnerRank.getRank())){
				this.topRanks.put(winnerRank.getKey(),winnerRank);
			}
			else {
				this.topRanks.remove(loser);
			}
		}
	}
	

}
