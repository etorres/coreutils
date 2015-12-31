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

import static java.util.Collections.synchronizedMap;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.LinkedHashMap;
import java.util.Map;
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

	private final int TIMEOUT_SECS = 8;
	private final Thread hook;

	/**
	 * Thread-safe map that preserves the insertion order of the entries.
	 */
	private final Map<String, ShutdownListener> listeners = synchronizedMap(new LinkedHashMap<String, ShutdownListener>());

	/**
	 * Creates an instance of this class and registers it with the JVM.
	 */
	public ShutdownHook() {
		hook = new Thread() {
			@Override
			public void run() {
				final ExecutorService executor = newSingleThreadExecutor();				
				for (final Map.Entry<String, ShutdownListener> entry : listeners.entrySet()) {
					final ShutdownListener listener = entry.getValue();
					if (listener != null) {
						executor.execute(new Runnable() {							
							@Override
							public void run() {
								try {
									listener.stop();
								} catch (Exception ignore) { }
							}
						});						
					}					
				}
				try {
					if (!executor.awaitTermination(TIMEOUT_SECS, SECONDS)) {
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
		requireNonNull(listener, "A non-null listener expected");
		final String name = listener.getClass().getCanonicalName();
		listeners.put(name, listener);
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