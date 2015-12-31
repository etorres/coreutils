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

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Lightweight and unopinionated central class for providing configuration information to a coreutils-powered application.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.2.0
 */
@ThreadSafe
public enum CoreutilsContext {

	COREUTILS_CONTEXT;

	private static final long TIMEOUT_MILLISECS = 2000l;

	private final Map<ContextKey, Map<String, Object>> registry = new EnumMap<>(ContextKey.class);
	private final Lock mutex = new ReentrantLock();
	private final BooleanReference register = new BooleanReference(false);

	/**
	 * Gets a client from the registry.
	 * @param type - the expected type of client
	 * @return An instance that matches the specified type, or <tt>null</tt> if no instance with the specified properties is 
	 *         found in the registry.
	 */
	@Nullable
	public <T> T getClient(final Class<T> type) {
		return getClient(type, null, null);
	}

	/**
	 * Gets a client from the registry. Callers can optionally specify a classifier.
	 * @param type - the expected type of client
	 * @param classifier - (optional) only clients matching the classifier will be selected 
	 * @return An instance that matches the specified type and classifier, or <tt>null</tt> if no instance with the specified 
	 *         properties is found in the registry.
	 */
	@Nullable
	public <T> T getClient(final Class<T> type, final @Nullable String classifier) {
		return getClient(type, classifier, null);
	}

	/**
	 * Gets a client from the registry. In case that no instance with the specified properties is found in the registry, the
	 * optional supplier will be used to create a new instance that will be registered. 
	 * @param type - the expected type of client
	 * @param supplier - (optional) client factory
	 * @return An instance that matches the specified type, or a new instance created with the supplier if no instance with 
	 *         the specified properties is found in the registry.
	 */
	@Nullable
	public <T> T getClient(final Class<T> type, final @Nullable Supplier<T> supplier) {
		return getClient(type, null, supplier);
	}

	/**
	 * Gets a client from the registry. Callers can optionally specify a classifier. In case that no instance with the specified 
	 * properties is found in the registry, the optional supplier will be used to create a new instance that will be registered.
	 * @param type - the expected type of client
	 * @param classifier - (optional) only clients matching the classifier will be selected
	 * @param supplier - (optional) client factory
	 * @return An instance that matches the specified type and classifier, or a new instance created with the supplier if no 
	 *         instance with the specified properties is found in the registry.
	 */
	@Nullable
	public <T> T getClient(final Class<T> type, final @Nullable String classifier, final @Nullable Supplier<T> supplier) {
		return get(ContextKey.CLIENTS, type, classifier, supplier);
	}

	/**
	 * Registers a new shutdown listener for the specified type.
	 * @param listener - the listener that will be registered
	 * @param type - the type of the shutdown listener
	 */
	public <T extends ShutdownListener> void addShutdownListener(final T listener, final Class<T> type) {
		addShutdownListener(listener, type, null);
	}

	/**
	 * Registers a new shutdown listener for the specified type with an optional classifier.
	 * @param listener - the listener that will be registered
	 * @param type - the type of the shutdown listener
	 * @param classifier - (optional) add this classifier to the registry
	 */
	public <T extends ShutdownListener> void addShutdownListener(final T listener, final Class<T> type, final @Nullable String classifier) {
		requireNonNull(listener, "A non-null listener expected");
		final ShutdownListener listener2 = get(ContextKey.SHUTDOWN_LISTENERS, type, classifier, () -> listener);
		if (listener != listener2) {
			throw new IllegalStateException("A previous shutdown listener was registered, try with a different classifier");
		}
	}

	/**
	 * Unregister a shutdown listener.
	 * @param type - the type of the shutdown listener	
	 */
	public <T extends ShutdownListener> void removeShutdownListener(final Class<T> type) {
		removeShutdownListener(type, null);
	}

	/**
	 * Unregister a shutdown listener.
	 * @param type - the type of the shutdown listener
	 * @param classifier - (optional) classifier
	 */
	public <T extends ShutdownListener> void removeShutdownListener(final Class<T> type, final @Nullable String classifier) {
		remove(ContextKey.SHUTDOWN_LISTENERS, type, classifier);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <T> T get(final ContextKey key, final Class<T> type, final @Nullable String classifier, final @Nullable Supplier<T> supplier) {
		requireNonNull(type, "A non-null type expected");
		try {
			mutex.tryLock(TIMEOUT_MILLISECS, MILLISECONDS);
			try {
				// lazy initialization of the registry
				register.set(false);
				final Map<String, Object> map = ofNullable(registry.get(key)).orElseGet(() -> {
					register.set(true);
					return new HashMap<>(1);
				});
				if (register.get()) registry.put(key, map);
				// get or create the instance
				register.set(false);
				final String instanceKey = instanceKey(type, classifier);
				final T instance = (T)ofNullable(map.get(instanceKey)).orElseGet(() -> {
					final T tmp = ofNullable(supplier).orElse(() -> null).get();
					if (tmp != null) register.set(true);
					return tmp;
				});
				if (register.get()) map.put(instanceKey, instance);
				return instance;
			} finally {
				mutex.unlock();
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException("The operation was interrupted");
		}
	}

	private <T> void remove(final ContextKey key, final Class<T> type, final @Nullable String classifier) {
		requireNonNull(type, "A non-null type expected");
		try {
			mutex.tryLock(TIMEOUT_MILLISECS, MILLISECONDS);
			try {
				ofNullable(registry.get(key)).orElse(emptyMap()).remove(instanceKey(type, classifier));
			} finally {
				mutex.unlock();
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException("The operation was interrupted");
		}
	}

	private <T> String instanceKey(final Class<T> type, final @Nullable String classifier) {
		return String.format("%s@%s", type.getCanonicalName(), ofNullable(classifier).orElse(""));
	}

	/**
	 * The different types of objects that can be stored within this context.
	 * @author Erik Torres <etserrano@gmail.com>
	 * @since 0.2.0
	 */
	private enum ContextKey {
		CLIENTS,
		SHUTDOWN_LISTENERS
	}

	/**
	 * Provides a reference to a boolean variable but without the synchronization cost of a atomic type.
	 * @author Erik Torres <etserrano@gmail.com>
	 * @since 0.2.0
	 */
	private static class BooleanReference {

		private boolean value = false;

		public BooleanReference(final boolean value) {		
			this.value = value;
		}

		public boolean get() {
			return value;
		}

		public void set(final boolean value) {
			this.value = value;
		}

	}

}