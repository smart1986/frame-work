package org.smart.framework.util.rank.cache;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.smart.framework.util.rank.LadderRank;
import org.smart.framework.util.rank.SegmentMark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * rank in time refresh rank when rank change
 *
 *
 * @param <K>
 * @param <V>
 */
public abstract class AbstractReverseSegmentConsistentLadderRankCache<K, V extends LadderRank<K,V>>
		extends RankCache<K, V> {

	public AbstractReverseSegmentConsistentLadderRankCache(int pageSize) {
		super(pageSize);
	}

	private static final Logger logger = LoggerFactory.getLogger(AbstractReverseSegmentConsistentLadderRankCache.class);

	private AtomicLong lastRank;

	protected ReentrantLock lock = new ReentrantLock(false);

	protected ConcurrentHashMap<Integer, SegmentMark<K>> segmentMarks = new ConcurrentHashMap<>();
	
	

	public abstract int findSegment(V v);

	protected int firstSegment() {
		return 1;
	}

	private void updateRank(V rank, long r) {
		rank.setRank(r);
	}

	private void markSegment(int segment, V rank) {
		SegmentMark<K> segmentMark = segmentMarks.get(segment);
		if (segmentMark == null) {
			segmentMark = new SegmentMark<>(segment, 0, rank.getKey(), rank.getRank());
			segmentMarks.put(segment, segmentMark);
		} else {
			segmentMark.mark(rank.getKey(), rank.getRank());
		}
	}

	protected boolean hasAchieveSegmentPoint(int segment, V rank) {
		if (!segmentMarks.containsKey(segment)) {
			return true;
		}
		SegmentMark<K> segmentMark = segmentMarks.get(segment);
		K segKey = segmentMark.getKey();
		if (segKey.equals(rank.getKey())) {
			return false;
		}
		V segRank = find(segKey);
		if (segRank.getRank() > rank.getRank()) {
			return true;
		}
		return false;
	}

	private void achieveSegment(V oldRank, V rank) {
		int segment = findSegment(rank);
		int oldSegment = findSegment(oldRank);
		boolean achieve = hasAchieveSegmentPoint(segment, rank);
		if (achieve) {
			markSegment(segment, rank);
		}
		if (segment != oldSegment) {
			SegmentMark<K> oldSegmentMark = segmentMarks.get(oldSegment);
			if (oldSegmentMark != null) {
				if (oldSegmentMark.getKey().equals(oldRank.getKey())) {
					if (oldRank.hasNext()) {
						V next = find(oldRank.getNext());
						int preSegment = findSegment(next);
						if (preSegment == oldSegment) {
							markSegment(oldSegment, next);
						} else {
							segmentMarks.remove(oldSegment);
						}
					} else {
						segmentMarks.remove(oldSegment);
					}
				}
			}
		}
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
		K achieve = null;
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
				achieve = previous.getKey();
				if (previous.hasPrevious()) {
					previous = find(previous.getPrevious());
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return find(achieve);
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
				// achieve
				if (!hasKey(key)) {
					if (hasRankLimit()) {
						V lastRank = findByRank(findLastRank());
						if (v.outstrip(lastRank)) {
							K lastKey = lastRank.getKey();
							int lastSegment = findSegment(lastRank);
							remove(lastRank);
							lastRank.setKey(key);
							lastRank.achieveRank(v);
							put(lastRank);
							if (segmentMarks.get(lastSegment).getKey().equals(lastKey)) {
								markSegment(lastSegment, lastRank);
							}
						}
					} else {
						addRank(v);
					}
				}
				if (!hasKey(key)) {
					return;
				}
				V rank = find(key);
				@SuppressWarnings("unchecked")
				V oldRank = (V) rank.copy();
				rank.achieveRank(v);
				if (rank.getRank() != 1) {
					V beyond = findBeyond(rank);
					if (beyond != null) {
						rankChange(beyond, rank);
					}
				}
				achieveSegment(oldRank, rank);
			} finally {
				lock.unlock();
			}
		}
	}

	public void cleanRank() {
		try {
			lock.lock();
			this.lastRank = new AtomicLong(1);
			this.segmentMarks.clear();
			clean();
		} finally {
			lock.unlock();
		}
	}

	protected abstract boolean hasPreSegment(int segment);

	protected abstract int findPreSegment(int segment);

	public List<K> findRandomMatch(K gamerId, int number, boolean downFirst) {
		List<K> list = Lists.newArrayListWithCapacity(number);
		V rank = find(gamerId);
		int segment;
		if (rank == null) {
			segment = firstSegment();
		} else {
			segment = findSegment(rank);
		}
		// copy value
		Map<Integer, SegmentMark<K>> map = Maps.newHashMap();
		for (Map.Entry<Integer, SegmentMark<K>> entry : this.segmentMarks.entrySet()) {
			if (entry.getKey() <= segment) {
				map.put(entry.getKey(), entry.getValue().copy());
			}
		}
		if (downFirst) {
			if (segment != firstSegment()) {
				segment = findPreSegment(segment);
			}
		}
		list.addAll(segmentMatch(map, list, gamerId, segment, number));
		while (list.size() < number) {
			if (!hasPreSegment(segment)) {
				break;
			}
			int num = number - list.size();
			int preSegment = findPreSegment(segment);
			list.addAll(segmentMatch(map, list, gamerId, preSegment, num));
			segment = preSegment;
		}
		return list;
	}

	public List<K> findRandomMatch(K gamerId, int number) {
		return findRandomMatch(gamerId, number, false);
	}

	private List<K> segmentMatch(Map<Integer, SegmentMark<K>> map, List<K> ks, K gamerId, int segment, int size) {
		List<K> list = Lists.newArrayListWithCapacity(size);
		if (!map.containsKey(segment)) {
			return list;
		}
		SegmentMark<K> segmentMark = map.get(segment);
		int preSeg = segment;
		if (hasPreSegment(segment)) {
			preSeg = findPreSegment(segment);
		}
		while (!map.containsKey(preSeg)) {
			if (!hasPreSegment(segment)) {
				break;
			}
			preSeg = findPreSegment(preSeg);
		}
		SegmentMark<K> preSegmentMark = map.get(preSeg);
		long preRank = find(preSegmentMark.getKey()).getRank();
		V segmentRank = find(segmentMark.getKey());
		if (segmentRank == null) {
			return list;
		}
		int segmentSize;
		long segRank = segmentRank.getRank();
		if (preSeg == segment) {
			segmentSize = (int) (findSize() - preRank);
		} else {
			segmentSize = (int) (preRank - segRank);
		}
		if (segmentSize <= 0) {
			return list;
		}
		int ms = 0;
		for (int i = 0, l = segmentSize; i < l; i++) {
			int add = ThreadLocalRandom.current().nextInt(segmentSize);
			long rank = segRank + add;
			V matchV = findByRank(rank);
			if (matchV == null) {
				for (Map.Entry<Integer, SegmentMark<K>> entry : segmentMarks.entrySet()) {
					logger.error("segments: seg: {} key : {} size: {} --- rank : {}", entry.getKey(),
							entry.getValue().getKey(), entry.getValue().getSize(), rank);
				}
				continue;
			}
			K match = matchV.getKey();
			if (!match.equals(gamerId) && !list.contains(match) && !ks.contains(match)) {
				list.add(match);
				ms++;
			}
			if (ms >= size) {
				break;
			}
		}
		return list;
	}

	public void logCacheData() {
		List<V> vs = findAll();
		Collections.sort(vs, new Comparator<V>() {
			@Override
			public int compare(V o1, V o2) {
				return o1.getRank() > o2.getRank() ? 1 : -1;
			}
		});

		for (V v : vs) {
			logger.warn(JSON.toJSONString(v));
		}
		List<Integer> keys = Lists.newArrayList(segmentMarks.keySet());
		Collections.sort(keys);

		for (int key : keys) {
			logger.warn("segments: seg: {} key : {}  size: {}", key, segmentMarks.get(key).getKey(),
					segmentMarks.get(key).getSize());
		}
		logger.warn("-------------------------------------------------------------------------");
		logger.warn("-------------------------------------------------------------------------");
	}

}
