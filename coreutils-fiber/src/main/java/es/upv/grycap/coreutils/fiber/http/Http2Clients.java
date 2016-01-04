/*
 * Core Utils - Fiber-enabled clients.
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

package es.upv.grycap.coreutils.fiber.http;

import static es.upv.grycap.coreutils.common.CoreutilsContext.COREUTILS_CONTEXT;
import static es.upv.grycap.coreutils.fiber.CoreutilsFiberLimits.CACHE_SIZE_RANGE;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.attribute.PosixFilePermissions.asFileAttribute;
import static java.nio.file.attribute.PosixFilePermissions.fromString;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.typesafe.config.Config;

import co.paralleluniverse.fibers.okhttp.FiberOkHttpClient;
import es.upv.grycap.coreutils.common.BaseShutdownListener;
import es.upv.grycap.coreutils.common.config.Configurer;

/**
 * Factory class that creates new {@link Http2Client} instances.
 * @author Erik Torres
 * @since 0.2.0
 */
public final class Http2Clients {

	private static final Logger LOGGER = getLogger(Http2Clients.class);

	/**
	 * Gets a HTTP2 client which is expected to integrate with other tasks managed by coreutils.
	 * @return An HTTP2 client managed by coreutils.
	 */
	public static Http2Client http2Client() {
		return new Http2Client(client());
	}

	/**
	 * Gets a new instance of the HTTP2 client that is not managed by coreutils in any way. Use this client if your application
	 * manages its own threads or if you use any other kind of concurrent execution outside coreutils.
	 * @return An unmanaged instance of the HTTP2 client.
	 */
	public static Http2Client isolatedHttp2Client() {
		return new Http2Client(new OkHttpClient());
	}

	/**
	 * Creates a {@link OkHttpClient} instance and configures it with a cache. The same instance is used across the application to
	 * benefit from a common cache storage and to prevent cache corruption. The cache directory is created private to the user who
	 * runs the application and its content is deleted when the JVM starts its shutting down sequence.
	 * @return A {@link OkHttpClient} instance that can be used everywhere in the application.
	 */
	private static OkHttpClient client() {
		return COREUTILS_CONTEXT.getClient(OkHttpClient.class, "coreutils-fiber", () -> {
			final OkHttpClient client = new FiberOkHttpClient();
			try {
				// load default configuration
				final Config config = new Configurer().loadConfig(null, "coreutils");
				final long tmp = config.getBytes("coreutils.clients.http2.cache-size");
				final long cacheSize = CACHE_SIZE_RANGE.contains(tmp) ? tmp : CACHE_SIZE_RANGE.lowerEndpoint();
				// create the cache directory				
				final File cacheDir = createTempDirectory("coreutils-okhttp-cache-", asFileAttribute(fromString("rwx------"))).toFile();
				final Http2ClientShutdownListener shutdownListener = new Http2ClientShutdownListener(cacheDir);
				COREUTILS_CONTEXT.addShutdownListener(shutdownListener, Http2ClientShutdownListener.class, "coreutils-fiber");
				final Cache cache = new Cache(cacheDir, cacheSize);					
				client.setCache(cache);				
			} catch (IOException e) {
				LOGGER.error("Failed to create directory cache", e);
			}
			return client;
		});
	}

	/**
	 * Shutdown listener to delete cache directory on application exit.
	 * @author Erik Torres
	 * @since 0.2.0
	 */
	public static class Http2ClientShutdownListener extends BaseShutdownListener {

		private final File cacheDir;

		public Http2ClientShutdownListener(final File cacheDir) {
			this.cacheDir = cacheDir;
		}

		@Override
		public void stop() {
			if (isRunning.getAndSet(false)) {
				deleteQuietly(cacheDir);
			}
		}

	}

}