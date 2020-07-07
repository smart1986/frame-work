package org.smart.framework.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @see Executors
 * @author smart
 *
 */
public class ExecutorFactory {
	public static ExecutorService newThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime,
			TimeUnit unit, String name) {
		ThreadFactory threadFactory = new NamedThreadFactory(name);
		return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
				new SynchronousQueue<Runnable>(), threadFactory);
	}

	public static ExecutorService newFixedThreadPool(int nThreads, String name) {
		ThreadFactory threadFactory = new NamedThreadFactory(name);
		return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), threadFactory);
	}

	public static ExecutorService newCachedThreadPool(String name) {
		ThreadFactory threadFactory = new NamedThreadFactory(name);
		return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
				threadFactory);
	}

	public static ExecutorService newSingleThreadExecutor(String name) {
		ThreadFactory threadFactory = new NamedThreadFactory(name);
		return new FinalizableDelegatedExecutorService(new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), threadFactory));
	}

	static class DelegatedExecutorService extends AbstractExecutorService {
		private final ExecutorService e;

		DelegatedExecutorService(ExecutorService executor) {
			e = executor;
		}

		@Override
		public void execute(Runnable command) {
			e.execute(command);
		}

		@Override
		public void shutdown() {
			e.shutdown();
		}

		@Override
		public List<Runnable> shutdownNow() {
			return e.shutdownNow();
		}

		@Override
		public boolean isShutdown() {
			return e.isShutdown();
		}

		@Override
		public boolean isTerminated() {
			return e.isTerminated();
		}

		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
			return e.awaitTermination(timeout, unit);
		}

		@Override
		public Future<?> submit(Runnable task) {
			return e.submit(task);
		}

		@Override
		public <T> Future<T> submit(Callable<T> task) {
			return e.submit(task);
		}

		@Override
		public <T> Future<T> submit(Runnable task, T result) {
			return e.submit(task, result);
		}

		@Override
		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
			return e.invokeAll(tasks);
		}

		@Override
		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
				throws InterruptedException {
			return e.invokeAll(tasks, timeout, unit);
		}

		@Override
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
				throws InterruptedException, ExecutionException {
			return e.invokeAny(tasks);
		}

		@Override
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			return e.invokeAny(tasks, timeout, unit);
		}
	}

	static class FinalizableDelegatedExecutorService extends DelegatedExecutorService {
		FinalizableDelegatedExecutorService(ExecutorService executor) {
			super(executor);
		}

		@Override
		protected void finalize() {
			super.shutdown();
		}
	}
}
