package org.smart.framework.util.rank;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Lists;

public class Page {

	private int page;

	private long start;

	private long end;

	public Page(int page, long start, long end) {
		this.page = page;
		this.start = start;
		this.end = end;
	}

	public boolean inPage(long value) {
		return value >= start && page <= end;
	}

	public static int findPage(long value, int segment) {
		if (value % segment == 0) {
			return (int) (value / segment);
		}
		return (int) (value / segment + 1);
	}

	private static final int DEFAULT_SEGMENT = 50;

	private static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Page>> cache = new ConcurrentHashMap<>();

	static {
		ConcurrentHashMap<Integer, Page> pages = new ConcurrentHashMap<>();
		for (int i = 1; i <= 10; i++) {
			int start = (i - 1) * DEFAULT_SEGMENT + 1;
			long end = i * DEFAULT_SEGMENT;
			pages.put(i, new Page(i, start, end));
		}
		cache.put(DEFAULT_SEGMENT, pages);
	}

	public static List<Page> findBetweenPages(long from, long to, int segment) {
		List<Page> pages = Lists.newArrayList();
		int fromPage = findPage(from, segment);
		int toPage = findPage(to, segment);
		for (int i = 0, l = fromPage - toPage; i < l; i++) {
			pages.add(valueOf(fromPage - i, segment));
		}
		return pages;
	}

	public static Page valueOf(int page, int segment) {
		ConcurrentHashMap<Integer, Page> pages = cache.get(segment);
		if (pages == null) {
			pages = new ConcurrentHashMap<>();
			cache.put(segment, pages);
		}
		Page p = pages.get(page);
		if (p == null) {
			int start = (page - 1) * segment + 1;
			long end = page * segment;
			p = new Page(page, start, end);
			pages.put(page, p);
		}
		return p;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

}
