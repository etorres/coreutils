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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import okio.BufferedSink;

/**
 * HTTP+SPDY client.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.1.0
 */
public final class Http2Client {

	private final OkHttpClient client;

	/**
	 * Access to this constructor is restricted to the classes in the same package. A factory method should be used to 
	 * create new instances of this class.
	 */
	Http2Client(final OkHttpClient client) {
		this.client = requireNonNull(client, "A valid HTTP2 client expected");
	}

	/**
	 * Retrieve information from a server via a HTTP GET request.
	 * @param url - URL target of this request
	 * @param nocache - don't accept an invalidated cached response, and don't store the server's response in any cache
	 * @param callback - is called back when the response is readable
	 */
	public void asyncGet(final String url, final boolean nocache, final Callback callback) {		
		asyncGet(url, null, nocache, callback);
	}

	/**
	 * Retrieve information from a server via a HTTP GET request.
	 * @param url - URL target of this request
	 * @param nocache - don't accept an invalidated cached response, and don't store the server's response in any cache
	 * @param callback - is called back when the response is readable
	 */
	public void asyncGetJson(final String url, final boolean nocache, final Callback callback) {		
		asyncGet(url, newArrayList("application/json"), nocache, callback);
	}

	/**
	 * Retrieve information from a server via a HTTP GET request.
	 * @param url - URL target of this request
	 * @param acceptableMediaTypes - Content-Types that are acceptable for this request
	 * @param nocache - don't accept an invalidated cached response, and don't store the server's response in any cache
	 * @param callback - is called back when the response is readable
	 */
	public void asyncGet(final String url, final @Nullable List<String> acceptableMediaTypes, final boolean nocache, final Callback callback) {
		final String url2 = requireNonNull(trimToNull(url), "A non-empty URL expected");
		requireNonNull(callback, "A valid callback expected");
		// configure cache
		final CacheControl.Builder cacheControlBuilder = new CacheControl.Builder();
		if (nocache) cacheControlBuilder.noCache().noStore();
		else cacheControlBuilder.maxStale(3600, TimeUnit.SECONDS);
		// prepare request
		final Request.Builder requestBuilder = new Request.Builder().cacheControl(cacheControlBuilder.build()).url(url2);
		ofNullable(acceptableMediaTypes).orElse(emptyList()).stream().filter(Objects::nonNull).forEach(type -> requestBuilder.addHeader("Accept", type));
		// submit request
		client.newCall(requestBuilder.build()).enqueue(callback);
	}

	/**
	 * Posts data to a server via a HTTP POST request.
	 * @param url - URL target of this request
	 * @param mediaType - Content-Type header for this request
	 * @param supplier - supplies the content of this request
	 * @param callback - is called back when the response is readable
	 */
	public void asyncPost(final String url, final String mediaType, final Supplier<String> supplier, final Callback callback) {
		requireNonNull(supplier, "A valid supplier expected");
		asyncPostBytes(url, mediaType, () -> ofNullable(supplier.get()).orElse("").getBytes(), callback);
	}

	/**
	 * Posts the content of a buffer of bytes to a server via a HTTP POST request.
	 * @param url - URL target of this request
	 * @param mediaType - Content-Type header for this request
	 * @param supplier - supplies the content of this request
	 * @param callback - is called back when the response is readable
	 */
	public void asyncPostBytes(final String url, final String mediaType, final Supplier<byte[]> supplier, final Callback callback) {
		final String url2 = requireNonNull(trimToNull(url), "A non-empty URL expected");
		final String mediaType2 = requireNonNull(trimToNull(mediaType), "A non-empty media type expected");
		requireNonNull(supplier, "A valid supplier expected");
		requireNonNull(callback, "A valid callback expected");
		// prepare request
		final Request request = new Request.Builder().url(url2).post(new RequestBody() {
			@Override
			public MediaType contentType() {
				return MediaType.parse(mediaType2 + "; charset=utf-8");
			}
			@Override
			public void writeTo(final BufferedSink sink) throws IOException {				
				sink.write(supplier.get());
			}
		}).build();
		// submit request
		client.newCall(request).enqueue(callback);
	}

	/**
	 * Puts data to a server via a HTTP PUT request.
	 * @param url - URL target of this request
	 * @param mediaType - Content-Type header for this request
	 * @param supplier - supplies the content of this request
	 * @param callback - is called back when the response is readable
	 */
	public void asyncPut(final String url, final String mediaType, final Supplier<String> supplier, final Callback callback) {
		requireNonNull(supplier, "A valid supplier expected");
		asyncPutBytes(url, mediaType, () -> ofNullable(supplier.get()).orElse("").getBytes(), callback);
	}

	/**
	 * Puts the content of a buffer of bytes to a server via a HTTP PUT request.
	 * @param url - URL target of this request
	 * @param mediaType - Content-Type header for this request
	 * @param supplier - supplies the content of this request
	 * @param callback - is called back when the response is readable
	 */
	public void asyncPutBytes(final String url, final String mediaType, final Supplier<byte[]> supplier, final Callback callback) {
		final String url2 = requireNonNull(trimToNull(url), "A non-empty URL expected");
		final String mediaType2 = requireNonNull(trimToNull(mediaType), "A non-empty media type expected");
		requireNonNull(supplier, "A valid supplier expected");
		requireNonNull(callback, "A valid callback expected");
		// prepare request
		final Request request = new Request.Builder().url(url2).put(new RequestBody() {
			@Override
			public MediaType contentType() {
				return MediaType.parse(mediaType2 + "; charset=utf-8");
			}
			@Override
			public void writeTo(final BufferedSink sink) throws IOException {				
				sink.write(supplier.get());
			}
		}).build();
		// submit request
		client.newCall(request).enqueue(callback);
	}

	/**
	 * Delete HTTP method.
	 * @param supplier - supplies the content of this request
	 * @param callback - is called back when the response is readable
	 */
	public void asyncDelete(final String url, final Callback callback) {
		final String url2 = requireNonNull(trimToNull(url), "A non-empty URL expected");
		requireNonNull(callback, "A valid callback expected");
		// prepare request
		final Request request = new Request.Builder().url(url2).delete().build();
		// submit request
		client.newCall(request).enqueue(callback);
	}

}