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

package org.grycap.coreutils.fiber.http;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.of;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Collections.synchronizedList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.grycap.coreutils.fiber.net.UrlBuilder.getUrlBuilder;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.client.methods.ZeroCopyConsumer;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.grycap.coreutils.fiber.net.UrlBuilder;
import org.slf4j.Logger;

import com.google.common.collect.Range;

import co.paralleluniverse.fibers.httpasyncclient.FiberCloseableHttpAsyncClient;

/**
 * High-concurrency data fetcher. Note that this class will name fetched files using a convention-over-configuration pattern 
 * where the suffix <tt>.partial</tt> is appended to the filenames to differentiate between files completely retrieved and 
 * files being fetched. Once the download complete, the file is moved to its final destination, by simply removing the suffix
 * from the filename and renaming the file. 
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.1.0
 */
public class HttpDataFetcher {

	private static final Logger LOGGER = getLogger(HttpDataFetcher.class);

	/**
	 * Maximum allowed concurrency. Values above this limit will be trimmed. 
	 */
	public static final int MAX_CONCURRENCY = 32;

	private final int concurrencyLevel;

	/**
	 * Convenient constructor that allows creating a new instance of this class setting the value of the concurrency level.
	 * Values above this {@link HttpDataFetcher#MAX_CONCURRENCY limit} will be trimmed.
	 * @param concurrencyLevel - desired concurrency level
	 */
	public HttpDataFetcher(final int concurrencyLevel) {
		this.concurrencyLevel = Range.closed(1, MAX_CONCURRENCY).contains(concurrencyLevel) ? concurrencyLevel : MAX_CONCURRENCY;
		LOGGER.info(new StringBuilder("Concurrency level: ").append(this.concurrencyLevel).toString());
	}

	/**
	 * Specialization of the method {@link HttpDataFetcher#fetchToDir(URL, String, List, String, String, File)} that allows fetching
	 * and saving a bunch of objects to the specified directory from a server that uses a REST or REST-like API where each object 
	 * is retrieved from the URL formed appending the object's identifier to the path of the the base URL.
	 * @param baseUrl - base URL from where the objects will be fetched
	 * @param ids - a list with the identifiers of the all requests that will be attempted
	 * @param outdir - directory where the files will be stored
	 * @return A {@link CompletableFuture} that allows cancellation. Once each fetch operation is completed, its status is updated
	 *         in the future with one of the possible values provided by the enumeration {@link FetchStatus}.
	 * @throws IOException If an error occurs during the execution of the method that prevents fetching or saving the files.
	 */
	public FecthFuture fetchToDir(final URL baseUrl, final List<String> ids, final File outdir) throws IOException {
		return fetchToDir(baseUrl, null, ids, null, null, outdir);
	}

	/**
	 * Allows fetching and saving a bunch of objects to the specified directory from a server that uses a REST or REST-like API 
	 * where each object is retrieved from the URL formed appending the object's identifier to the path of the the base URL, and 
	 * optionally from a server that uses a parameter to identify the objects. Supports additional configuration options to name
	 * the fetched objects.
	 * @param baseUrl - base URL from where the objects will be fetched
	 * @param queryParam - if defined, a query parameter will be appended to the base URL with the identifier of the request
	 * @param ids - a list with the identifiers of the all requests that will be attempted
	 * @param prefix - optionally prepend this prefix to the filenames of the saved files
	 * @param suffix - optionally append this suffix to the filenames of the saved files
	 * @param outdir - directory where the files will be stored
	 * @return A {@link CompletableFuture} that allows cancellation. Once each fetch operation is completed, its status is updated
	 *         in the future with one of the possible values provided by the enumeration {@link FetchStatus}.
	 * @throws IOException If an error occurs during the execution of the method that prevents fetching or saving the files.
	 */
	public FecthFuture fetchToDir(final URL baseUrl, final @Nullable String queryParam, final List<String> ids, 
			final @Nullable String prefix, final @Nullable String suffix, final File outdir) throws IOException {
		// check mandatory parameters
		requireNonNull(baseUrl, "A valid URL expected");
		final FecthFuture toBeCompleted = new FecthFuture(requireNonNull(ids, "A valid list of identifiers expected").stream()
				.map(StringUtils::trimToNull).filter(Objects::nonNull).distinct().collect(Collectors.toList()));		
		requireNonNull(outdir, "A valid output directory expected");
		checkArgument((outdir.isDirectory() && outdir.canWrite()) || outdir.mkdirs(), new StringBuilder("Cannot write to the output directory: ")
				.append(outdir.getAbsolutePath()).toString());
		// get optional parameters
		final Optional<String> queryParam2 = ofNullable(trimToNull(queryParam));
		final String prefix2 = ofNullable(prefix).orElse("");
		final String suffix2 = ofNullable(suffix).orElse("");
		try (final CloseableHttpAsyncClient asyncHttpClient = createFiberCloseableHttpAsyncClient()) {
			asyncHttpClient.start();			
			final UrlBuilder urlBuilder = getUrlBuilder(baseUrl);
			// an explanation is needed since this code is instrumented by Quasar and Comsat: requests are created during the first part of
			// this lambda expression (map), but they are not executed until the get() method is called in the second part of the expression
			// (forEach). Here that parallel stream is used to block and wait for the requests to complete. In case that a single stream is
			// used, each request will be created and executed sequentially. Therefore, the alternative to parallel stream is to separate
			// the lambda expression in two loops, creating the requests in the first loop and calling get() in the second one.
			toBeCompleted.monList.parallelStream().map(m -> {
				try {
					// create output file
					final File outfile = new File(outdir, new StringBuilder(prefix2).append(m.id).append(suffix2).append(".partial").toString());
					checkState(outfile.createNewFile(), new StringBuilder("Cannot create the output file: ")
							.append(outfile.getAbsolutePath()).toString());
					// create the HTTP request					
					final HttpHost target = URIUtils.extractHost(baseUrl.toURI());
					final HttpRequest request = new BasicHttpRequest("GET", urlBuilder.buildRelativeUrl(queryParam2.isPresent() ? null : m.id, 
							queryParam2.isPresent() ? of(queryParam2.get(), m.id) : null));					
					final HttpAsyncRequestProducer producer = new BasicAsyncRequestProducer(target, request);
					// create the consumer
					final ZeroCopyConsumer<File> consumer = new ZeroCopyConsumer<File>(outfile) {
						@Override
						protected File process(final HttpResponse response, final File file, final ContentType contentType) throws Exception {
							final StatusLine status = response.getStatusLine();							
							if (LOGGER.isDebugEnabled())
								LOGGER.debug(new StringBuilder("Got file: statusCode=").append(status.getStatusCode())
										.append(", file=").append(file.getAbsolutePath()).toString());
							if (status.getStatusCode() != HttpStatus.SC_OK)
								throw new ClientProtocolException(new StringBuilder("Object fetch failed: ").append(status).toString());
							return file;
						}
					};
					// prepare request
					m.future = asyncHttpClient.execute(producer, consumer, new FutureCallback<File>() {
						@Override
						public void cancelled() {
							toBeCompleted.update(m.id, FetchStatus.CANCELLED);							
							LOGGER.info("Task cancelled");
						}
						@Override
						public void completed(final File result) {							
							try {
								final Path path = result.toPath();
								Files.move(path, path.resolveSibling(removeEnd(result.getName(), ".partial")), REPLACE_EXISTING);
								toBeCompleted.update(m.id, FetchStatus.COMPLETED);
							} catch (IOException ex) {
								toBeCompleted.update(m.id, FetchStatus.FAILED);
								LOGGER.error("Fecth failed to move file to its final destination with error", ex);
							}
						}
						@Override
						public void failed(final Exception ex) {
							toBeCompleted.update(m.id, FetchStatus.FAILED);
							LOGGER.error("Fecth failed with error", ex);
						}						
					});
				} catch (Exception e) {
					LOGGER.error(new StringBuilder("Failed to fetch object with id: ").append(m.id).toString(), e);
				}
				return m;
			}).forEach(m -> {
				try {
					// submit requests and wait for completion
					m.future.get();
				} catch (Exception ignore) { /* exceptions are handled in the callback functions */ }
			});
		}	
		return toBeCompleted;
	}

	private CloseableHttpAsyncClient createFiberCloseableHttpAsyncClient() {
		return FiberCloseableHttpAsyncClient.wrap(HttpAsyncClients
				.custom()
				.setMaxConnPerRoute(concurrencyLevel)
				.setMaxConnTotal(concurrencyLevel)
				.build());
	}

	/**
	 * Extends Java 8 {@link CompletableFuture} with real cancellation that will attempt to cancel the requests managed by this future.
	 * @author Erik Torres <etserrano@gmail.com>
	 * @since 0.0.1
	 * @see <a href="https://dzone.com/articles/completablefuture-cant-be">CompletableFuture can't be Interrupted</a>
	 */
	public static class FecthFuture extends CompletableFuture<Map<String, FetchStatus>> {

		private final List<FetchMonitor> monList;

		public FecthFuture(final List<String> ids) {
			this.monList = synchronizedList(requireNonNull(ids, "A non-empty list expected").stream()
					.map(FetchMonitor::new).collect(Collectors.toList()));			
			checkArgument(!monList.isEmpty(), "A non-empty list expected");			
		}

		public void update(final String id, final FetchStatus newStatus) {
			monList.stream().filter(m -> m.id.equals(id)).findFirst().ifPresent(m -> {
				m.status = requireNonNull(newStatus, "A non-empty status expected");
			});
			if (!monList.stream().anyMatch(m -> FetchStatus.PENDING.equals(m.status))) {
				complete(monList.stream().collect(Collectors.toMap(FetchMonitor::getId, FetchMonitor::getStatus)));
			}
		}

		@Override
		public boolean cancel(final boolean mayInterruptIfRunning) {
			monList.stream().forEach(m -> { try { m.future.cancel(true); } catch (Exception ignore) { } });			
			return super.cancel(mayInterruptIfRunning);
		}

	}

	/**
	 * Monitors fetch operation status.
	 * @author Erik Torres <etserrano@gmail.com>
	 * @since 0.0.1
	 */
	private static class FetchMonitor {

		private final String id;
		private Future<File> future;
		private FetchStatus status = FetchStatus.PENDING;		

		public FetchMonitor(final String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public FetchStatus getStatus() {
			return status;
		}

	}

	/**
	 * Possible status after object fetching.
	 * @author Erik Torres <etserrano@gmail.com>
	 * @since 0.0.1
	 */
	public enum FetchStatus {
		PENDING,
		COMPLETED,
		CANCELLED,
		FAILED
	}

}