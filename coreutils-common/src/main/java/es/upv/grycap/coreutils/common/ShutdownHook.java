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

package es.upv.grycap.coreutils.common;

import static es.upv.grycap.coreutils.common.CoreutilsLimits.WAIT_TERMINATION_TIMEOUT_RANGE;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Executes a shutdown sequence when the JVM shutdowns, calling the {@link ShutdownListener#stop()} method of the registered listeners 
 * and waiting until they finish or a timeout expires.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.2.0
 */
@ThreadSafe
public class ShutdownHook {	

	private final Thread hook;

	/**
	 * Thread-safe LIFO stack that preserves the insertion order of the entries.
	 */
	private final Deque<ShutdownListener> listeners = new ConcurrentLinkedDeque<ShutdownListener>();

	/**
	 * Creates an instance of this class and registers it with the JVM.
	 */
	public ShutdownHook(final long waitTerminationTimeout) {
		final long waitTerminationTimeout2 = WAIT_TERMINATION_TIMEOUT_RANGE.contains(waitTerminationTimeout) ? waitTerminationTimeout 
				: WAIT_TERMINATION_TIMEOUT_RANGE.lowerEndpoint();
		hook = new Thread() {
			@Override
			public void run() {
				final ExecutorService executor = newSingleThreadExecutor();				
				while (!listeners.isEmpty()) {
					final ShutdownListener listener = listeners.pop();
					executor.execute(new Runnable() {							
						@Override
						public void run() {
							try {
								listener.stop();
							} catch (Exception ignore) { }
						}
					});						
				}
				try {
					if (!executor.awaitTermination(waitTerminationTimeout2, MILLISECONDS)) {
						executor.shutdown();
					}
				} catch (Exception e) {
					// force shutdown if current thread also interrupted, preserving interrupt status
					executor.shutdown();
					if (e instanceof InterruptedException) {
						Thread.currentThread().interrupt();
					}
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(hook);
	}

	/**
	 * Registers a listener for shutdown.
	 * @param listener - the listener that will be added to the shutdown sequence
	 */
	public void register(final ShutdownListener listener) {
		listeners.push(requireNonNull(listener, "A non-null listener expected"));
	}

	public void deregister(final ShutdownListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Cancels the execution of the shutdown sequence specified in this class.
	 */
	public void cancel() {
		try {
			Runtime.getRuntime().removeShutdownHook(hook);
		} catch (Exception ignore) { }
		listeners.clear();
	}

}