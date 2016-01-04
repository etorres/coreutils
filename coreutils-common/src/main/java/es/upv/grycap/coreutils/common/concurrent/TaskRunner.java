/*
 * Core Utils - Common Utilities.
 * Copyright 2015-2016 GRyCAP (Universitat Politecnica de Valencia)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * 
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */

package es.upv.grycap.coreutils.common.concurrent;

import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static es.upv.grycap.coreutils.common.CoreutilsLimits.KEEP_ALIVE_TIME_RANGE;
import static es.upv.grycap.coreutils.common.CoreutilsLimits.MAX_POOL_SIZE_RANGE;
import static es.upv.grycap.coreutils.common.CoreutilsLimits.WAIT_TERMINATION_TIMEOUT_RANGE;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import es.upv.grycap.coreutils.common.ShutdownListener;

/**
 * Runs tasks in a pool of threads that must be disposed as part of the application termination. Tasks are submitted to the pool of threads 
 * for execution and a {@link CompletableFuture} is returned to the caller.
 * @author Erik Torres
 * @since 0.2.0
 */
@ThreadSafe
public class TaskRunner implements ShutdownListener {

	private static final Logger LOGGER = getLogger(TaskRunner.class);

	private static final String THREAD_NAME_PATTERN = "coreutils-runner-%d";	

	private final AtomicBoolean isRunning = new AtomicBoolean(true);
	private final ExecutorService executor;	
	private final long waitTerminationTimeout;

	/**
	 * Reuse the code of {@link java.util.concurrent.Executors#newCachedThreadPool(java.util.concurrent.ThreadFactory)} to create the thread pool, 
	 * limiting the maximum number of threads in the pool to number of available processors or the value of the parameter.
	 * @param maxPoolSize - maximum number of threads in the pool
	 * @param keepAliveTime - when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait for 
	 *                        new tasks before terminating ()
	 * @param waitTerminationTimeout - will wait this time on {@link #stop()} before raising a timeout exception
	 */
	public TaskRunner(final int maxPoolSize, final long keepAliveTime, final long waitTerminationTimeout) {
		final int maxPoolSize2 = MAX_POOL_SIZE_RANGE.contains(maxPoolSize) ? maxPoolSize : MAX_POOL_SIZE_RANGE.lowerEndpoint();
		final long keepAliveTime2 = KEEP_ALIVE_TIME_RANGE.contains(keepAliveTime) ? keepAliveTime : KEEP_ALIVE_TIME_RANGE.lowerEndpoint();
		this.executor = new ThreadPoolExecutor(0, maxPoolSize2, keepAliveTime2, MILLISECONDS, new SynchronousQueue<Runnable>(), 
				new ThreadFactoryBuilder().setNameFormat(THREAD_NAME_PATTERN)
				.setDaemon(false)
				.setUncaughtExceptionHandler(new TaskUncaughtExceptionHandler())
				.build());
		this.waitTerminationTimeout = WAIT_TERMINATION_TIMEOUT_RANGE.contains(waitTerminationTimeout) ? waitTerminationTimeout 
				: WAIT_TERMINATION_TIMEOUT_RANGE.lowerEndpoint();
	}

	/**
	 * Submits a new task for execution to the pool of threads managed by this class.
	 * @param supplier - a function returning the value to be used to complete the returned {@link CompletableFuture}
	 * @return a {@link CompletableFuture} that the caller can use to track the execution of the task and to register a callback function.
	 */
	public <T> CompletableFuture<T> submit(final Supplier<T> supplier) {
		requireNonNull(supplier, "A non-null supplier expected");
		CompletableFuture<T> future = null;
		if (isRunning.get()) {
			future = supplyAsync(supplier, executor);
		} else {
			future = new CompletableFuture<>();
			future.completeExceptionally(new IllegalStateException("This task runner is not active"));
		}
		return future;		
	}

	public ExecutorService executorService() {
		return executor;
	}

	@Override
	public boolean isRunning() {
		return isRunning.get();
	}

	@Override
	public void init() {
		isRunning.compareAndSet(false, true);
	}

	@Override
	public void stop() {
		if (isRunning.getAndSet(false)) {
			try {
				if (!shutdownAndAwaitTermination(executor, waitTerminationTimeout, MILLISECONDS)) {
					executor.shutdownNow();
				}
			} catch (Exception e) {
				// force shutdown if current thread also interrupted, preserving interrupt status
				executor.shutdownNow();
				if (e instanceof InterruptedException) {
					Thread.currentThread().interrupt();
				}
			} finally {
				LOGGER.debug("Task runner shutdown successfully");	
			}
		}
	}

}