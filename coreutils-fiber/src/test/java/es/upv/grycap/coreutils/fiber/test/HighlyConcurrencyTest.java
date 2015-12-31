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

package es.upv.grycap.coreutils.fiber.test;

import static com.google.common.collect.ImmutableList.of;
import static es.upv.grycap.coreutils.fiber.http.Http2Clients.http2Client;
import static es.upv.grycap.coreutils.fiber.http.Http2Clients.isolatedHttp2Client;
import static es.upv.grycap.coreutils.fiber.test.mockserver.FiberExpectationInitializer.MOCK_SERVER_BASE_URL;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import es.upv.grycap.coreutils.fiber.http.Http2Client;
import es.upv.grycap.coreutils.test.category.IntegrationTests;
import es.upv.grycap.coreutils.test.rules.TestPrinter;
import es.upv.grycap.coreutils.test.rules.TestWatcher2;
import net.jodah.concurrentunit.Waiter;

/**
 * Tests the {@link Http2Client} under highly concurrency conditions.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.1.0
 */
@Category(IntegrationTests.class)
public class HighlyConcurrencyTest {

	@Rule
	public TestPrinter pw = new TestPrinter();

	@Rule
	public TestRule watchman = new TestWatcher2(pw);

	@Test
	public void testManagedClient() throws Exception {
		final Http2Client client = http2Client();
		assertThat("HTTP+SPDY managed client was created", client, notNullValue());
		runTest(client);
	}

	@Test
	public void testIsolatedClient() throws Exception {
		final Http2Client client = isolatedHttp2Client();
		assertThat("HTTP+SPDY isolated client was created", client, notNullValue());
		runTest(client);		
	}

	private void runTest(final Http2Client client) throws Exception {
		// prepare the test
		final Waiter waiter = new Waiter();
		for (int i = 0; i < 100; i++) {
			final int id = i;
			client.asyncGet(new StringBuilder(MOCK_SERVER_BASE_URL).append("/test/concurrent/").append(i).toString(), of("application/json"), true, new Callback() {
				@Override
				public void onResponse(final Response response) throws IOException {
					waiter.assertTrue(response.isSuccessful());
					final String payload = response.body().source().readUtf8();
					waiter.assertThat(payload, allOf(notNullValue(), equalTo(new StringBuilder("Response-to-").append(id).toString())));
					waiter.resume();
				}
				@Override
				public void onFailure(final Request request, final IOException throwable) {
					waiter.fail(throwable);			
				}
			});
		}
		waiter.await(30l, TimeUnit.SECONDS, 100);
	}
}