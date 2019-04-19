package org.smart.framework.util.rank;

import java.io.Serializable;


public class SegmentMark<K> implements Serializable {

	private static final long serialVersionUID = 4774791995306599451L;

	private int segment;

	private int size;

	private K key;

	private long rank;

	public SegmentMark(int segment, int size, K key) {
		this.segment = segment;
		this.size = size;
		this.key = key;
	}

	public SegmentMark(int segment, int size, K key, long rank) {
		this.segment = segment;
		this.size = size;
		this.key = key;
		this.rank = rank;
	}

	public void addSize(int add) {
		this.size += add;
	}

	public void mark(K key) {
		this.key = key;
	}

	public void mark(K key, long rank) {
		this.key = key;
		this.rank = rank;
	}

	public SegmentMark<K> copy() {
		SegmentMark<K> mark = new SegmentMark<>(this.segment, this.size, this.key, this.rank);
		return mark;
	}

	public int getSegment() {
		return segment;
	}

	public void setSegment(int segment) {
		this.segment = segment;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public K getKey() {
		return key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public long getRank() {
		return rank;
	}

	public void setRank(long rank) {
		this.rank = rank;
	}
}
