package org.smart.framework.util.rank;

public abstract class LadderRank<K,T> extends AbstractRank<K> {


	protected K next;

	protected K previous;

	protected long timestamp;

	public LadderRank() {
		this.timestamp = System.currentTimeMillis();
	}

	public LadderRank(Long rank, K key, K next, K previous) {
		super(rank, key);
		this.next = next;
		this.previous = previous;
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * 超越
	 * 
	 * @param rank
	 * @return
	 */
	public abstract boolean outstrip(T rank);

	public abstract void achieveRank(T rank);

	public boolean hasNext() {
		if (next == null) {
			return false;
		}
		if (next instanceof Long) {
			return (Long) next != 0;
		} else if (next instanceof Integer) {
			return (Integer) next != 0;
		}
		// todo

		throw new RuntimeException();
	}

	public boolean hasPrevious() {
		if (previous == null) {
			return false;
		}
		if (previous instanceof Long) {
			return (Long) previous != 0;
		} else if (previous instanceof Integer) {
			return (Integer) previous != 0;
		}
		// todo.....

		throw new RuntimeException();
	}

	public void clearNext() {
		next = null;
	}

	public void clearPrevious() {
		previous = null;
	}

	public K getNext() {
		return next;
	}

	public void setNext(K next) {
		this.next = next;
	}

	public K getPrevious() {
		return previous;
	}

	public void setPrevious(K previous) {
		this.previous = previous;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "LadderRank{" + "next=" + next + ", previous=" + previous + ", timestamp=" + timestamp + "} "
				+ super.toString();
	}
}
