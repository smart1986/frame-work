package org.smart.framework.util.thread;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.smart.framework.util.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * 同一属性任务在同一个线程中执行
 * @author jerry
 *
 */
public class OrderThreadPool implements ExecutorService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private ExecutorService[] executors;

	private Map<Object, Integer> mapping = new ConcurrentHashMap<>();
	private AtomicInteger atomIndex = new AtomicInteger();

	public OrderThreadPool() {
		this(0, null);
	}

	public OrderThreadPool(int threadNum) {
		this(threadNum, null);
	}

	public OrderThreadPool(int threadNum, ThreadFactory threadFactory) {
		if (threadNum <= 0) {
			threadNum = Runtime.getRuntime().availableProcessors() * 2;
		}
		executors = new ExecutorService[threadNum];
		for (int i = 0; i < executors.length; i++) {
			if (threadFactory == null) {
				executors[i] = Executors.newSingleThreadExecutor();
			} else {
				executors[i] = Executors.newFixedThreadPool(1, threadFactory);
			}
		}
	}

	public OrderThreadPool(ThreadFactory threadFactory) {
		this(0, threadFactory);
	}

	public void clean(Object identify) {
		logger.debug("identify remove :{}.", identify);
		mapping.remove(identify);
	}

	public void clean() {
		mapping.clear();
	}
	@Override
	public void shutdown() {
		for (ExecutorService executor : executors) {
			executor.shutdown();
		}
	}

	@Override
	public void execute(Runnable command) {
		if (command instanceof OrderTask) {
			OrderTask orderTask = (OrderTask) command;
			if (orderTask.identify() == null) {
				executors[RandomUtils.nextIntIndex(executors.length)].execute(orderTask);
			} else {

				if (mapping.containsKey(orderTask.identify())) {
					int i = mapping.get(orderTask.identify());
					executors[i].execute(orderTask);
				} else {
					int index = atomIndex.get();
					if (index >= executors.length) {
						atomIndex.set(0);
						index = 0;
					}
					mapping.put(orderTask.identify(),  index);
					executors[index].execute(orderTask);
					atomIndex.incrementAndGet();
					logger.debug("task:{} bind thread:{}.", orderTask.identify(), atomIndex.get());
				}
			}
		} else {
			executors[RandomUtils.nextIntIndex(executors.length)].execute(command);
		}
		

	}

	@Override
	public List<Runnable> shutdownNow() {
		List<Runnable> result = Lists.newArrayList();
		for (ExecutorService executorService : executors) {
			result.addAll(executorService.shutdownNow());
		}
		return result;
	}

	@Override
	public boolean isShutdown() {
		for (ExecutorService executorService : executors) {
			if (!executorService.isShutdown()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isTerminated() {
		for (ExecutorService executorService : executors) {
			if (!executorService.isTerminated()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		for (ExecutorService executorService : executors) {
			if (!executorService.awaitTermination(timeout, unit)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return executors[RandomUtils.nextIntIndex(executors.length)].submit(task);
	}

	@Deprecated
	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return executors[RandomUtils.nextIntIndex(executors.length)].submit(task,result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		Future<?> f;
		if (task instanceof OrderTask) {
			OrderTask orderTask = (OrderTask) task;
			if (orderTask.identify() == null) {
				f = executors[RandomUtils.nextIntIndex(executors.length)].submit(orderTask);
			} else {
				
				if (mapping.containsKey(orderTask.identify())) {
					int i = mapping.get(orderTask.identify());
					f = executors[i].submit(orderTask);
				} else {
					int index = atomIndex.get();
					if (index >= executors.length) {
						atomIndex.set(0);
						index = 0;
					}
					mapping.put(orderTask.identify(),  index);
					f = executors[index].submit(orderTask);
					atomIndex.incrementAndGet();
					logger.debug("task:{} bind thread:{}.", orderTask.identify(), atomIndex.get());
				}
			}
		} else {
			f = executors[RandomUtils.nextIntIndex(executors.length)].submit(task);
		}
		return f;
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		return executors[RandomUtils.nextIntIndex(executors.length)].invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
			TimeUnit unit) throws InterruptedException {
		return executors[RandomUtils.nextIntIndex(executors.length)].invokeAll(tasks, timeout,unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		return executors[RandomUtils.nextIntIndex(executors.length)].invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return executors[RandomUtils.nextIntIndex(executors.length)].invokeAny(tasks,timeout,unit);
	}

}
