package org.smart.framework.util.rank.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.smart.framework.util.rank.LadderRank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * rank in time refresh rank when rank change
 *
 *
 * @param <K>
 * @param <V>
 */
public class ConsistentLadderRankCache<K, V extends LadderRank<K,V>> extends RankCache<K, V> {

	
	private static final Logger logger = LoggerFactory.getLogger(ConsistentLadderRankCache.class);

	private AtomicLong lastRank;

	protected ReentrantLock lock = new ReentrantLock(false);

	public ConsistentLadderRankCache(int pageSize) {
		super(pageSize);
	}
	
	public ConsistentLadderRankCache(int pageSize, int rankLimit) {
		super(pageSize, rankLimit);
	}

	private void updateRank(V rank, long r) {
		rank.setRank(r);
	}

	private void addRank(V rank) {
		long lastRank = findLastRank();
		long nextRank = findNextRank();

		if (nextRank != 1) {
			V previous = findByRank(lastRank);
			linkRank(previous, rank);
			put(previous);
		}
		updateRank(rank, nextRank);
		put(rank);
	}

	/**
	 * find achieve gamer,
	 * 
	 * @param rank
	 * @return
	 */
	private V findBeyond(V rank) {
		V previous = find(rank.getPrevious());
		V achieve = null;
		int loop = 0;
		for (;;) {
			loop++;
			if (loop > 1000000) {
				logger.error("---------------for loop over 100000---------------------------");
				for (int i = 0; i < 10000; i++) {
					logger.error("cache: {}", previous.toString());
					previous = find(previous.getPrevious());
				}
				break;
			}
			if (previous == null) {
				break;
			}
			if (rank.outstrip(previous)) {
				achieve = previous;
				if (previous.hasPrevious()) {
					previous = find(previous.getPrevious());
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return achieve;
	}

	private V findDown(V rank) {
		V next = find(rank.getNext());
		V achieve = null;
		int loop = 0;
		for (;;) {
			loop++;
			if (loop > 1000000) {
				logger.error("---------------for loop over 100000---------------------------");
				for (int i = 0; i < 10000; i++) {
					logger.error("cache: {}", next.toString());
					next = find(next.getNext());
				}
				break;
			}
			if (next == null) {
				break;
			}
			if (next.outstrip(rank)) {
				achieve = next;
				if (next.hasNext()) {
					next = find(next.getNext());
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return achieve;
	}

	private void rankChange(V beyond, V rank) {
		Map<K, V> map = Maps.newHashMap();
		long from = rank.getRank();
		long to = beyond.getRank();
		jump(map, beyond, rank);
		map.put(beyond.getKey(), beyond);
		map.put(rank.getKey(), rank);
		updateRank(rank, to);
		long change = from - to;
		K next = rank.getNext();
		for (long i = 1; i <= change; i++) {
			V changeRank = find(next);
			updateRank(changeRank, rank.getRank() + i);
			map.put(changeRank.getKey(), changeRank);
			next = changeRank.getNext();
		}
		for (Map.Entry<K, V> entry : map.entrySet()) {
			put(entry.getValue());
		}
		refreshTop();
	}

	public void refreshTop() {
		if (topSize() > findPageSegment()) {
			V out = findTop(findByRank(findPageSegment()).getNext());
			if (out != null) {
				removeTop(out.getKey());
			}
		}
	}

	private void downChange(V down, V rank) {
		Map<K, V> map = Maps.newHashMap();
		boolean outTop = false;
		if (!hasInTop(down.getRank())) {
			outTop = true;
		}
		long change = down.getRank() - rank.getRank();
		reverseJump(map, down, rank);
		map.put(down.getKey(), down);
		map.put(rank.getKey(), rank);
		updateRank(rank, down.getRank());
		K previous = rank.getPrevious();
		for (long i = 1; i <= change; i++) {
			V changeRank = find(previous);
			updateRank(changeRank, rank.getRank() - i);
			map.put(changeRank.getKey(), changeRank);
			previous = changeRank.getPrevious();
		}
		for (Map.Entry<K, V> entry : map.entrySet()) {
			put(entry.getValue());
		}
		if (outTop) {
			removeTop(rank.getKey());
		}
	}

	private void reverseJump(Map<K, V> map, V down, V rank) {
		if (rank.hasPrevious()) {
			V previous = find(rank.getPrevious());
			V next = find(rank.getNext());
			linkRank(previous, next);
			map.put(previous.getKey(), previous);
			map.put(next.getKey(), next);
		} else {
			V next = find(rank.getNext());
			next.clearPrevious();
			map.put(next.getKey(), next);
		}

		if (down.hasNext()) {
			V next = find(down.getNext());
			linkRank(rank, next);
			map.put(next.getKey(), next);
		} else {
			rank.clearNext();
		}
		linkRank(down, rank);
	}

	private void jump(Map<K, V> map, V beyond, V rank) {
		if (rank.hasNext()) {
			V previous = find(rank.getPrevious());
			V next = find(rank.getNext());

			linkRank(previous, next);
			map.put(previous.getKey(), previous);
			map.put(next.getKey(), next);
		} else {
			V previous = find(rank.getPrevious());
			previous.clearNext();
			map.put(previous.getKey(), previous);
		}

		if (beyond.hasPrevious()) {
			V previous = find(beyond.getPrevious());
			linkRank(previous, rank);
			map.put(previous.getKey(), previous);
		} else {
			rank.clearPrevious();
		}
		linkRank(rank, beyond);
	}

	private void linkRank(V previous, V next) {
		next.setPrevious(previous.getKey());
		previous.setNext(next.getKey());
	}

	@Override
	public long findLastRank() {
		if (this.lastRank == null) {
			this.lastRank = new AtomicLong(1);
		}
		return this.lastRank.get() - 1;
	}

	private long findNextRank() {
		return this.lastRank.getAndIncrement();
	}

	public void achieve(V v) {
		if (v == null) {
			return;
		}
		K key = v.getKey();
		if (key == null) {
			return;
		}
		if (canEnterRank(v)) {
			V old = find(key);
			if (old != null && v.getTimestamp() < old.getTimestamp()) {
				return;
			}
			try {
				lock.lock();
				if (!hasKey(key)) {
					if (hasRankLimit()) {
						V lastRank = findByRank(findLastRank());
						if (v.outstrip(lastRank)) {
							remove(lastRank);
							lastRank.setKey(key);
							V previous = find(lastRank.getPrevious());
							if (previous != null) {
								previous.setNext(key);
							}
							lastRank.achieveRank(v);
							put(lastRank);
						}
					} else {
						addRank(v);
					}
				}
				if (!hasKey(key)) {
					return;
				}
				V rank = find(key);
				rank.achieveRank(v);
				if (rank.getRank() == 1) {
					V down = findDown(rank);
					if (down != null) {
						downChange(down, rank);
					}
				} else {
					V beyond = findBeyond(rank);
					if (beyond == null) {
						V down = findDown(rank);
						if (down != null) {
							downChange(down, rank);
						}
					} else {
						rankChange(beyond, rank);
					}
				}
			} finally {
				lock.unlock();
			}
		}
	}
	@Override
	public boolean clean() {
		try {
			lock.lock();
			this.lastRank = new AtomicLong(1);
			return super.clean();
		} finally {
			lock.unlock();
		}
	}

	public List<V> findSettleRanks(long maxRank) {
		List<V> vs = Lists.newArrayListWithCapacity((int) maxRank);
		try {
			lock.lock();
			for (long i = 1; i <= maxRank; i++) {
				V v = findByRank(i);
				if (v == null) {
					break;
				} else {
					@SuppressWarnings("unchecked")
					V copy = (V) v.copy();
					vs.add(copy);
				}
			}
		} finally {
			lock.unlock();
		}
		return vs;
	}
	
	public void reSettleRanks(){
		try {
			lock.lock();
			List<V> list = findAll();
			this.lastRank = new AtomicLong(1);
			clean();
			for (V v : list) {
				achieve(v);
			}
		} finally {
			lock.unlock();
		}
	}

}
