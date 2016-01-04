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
import static com.google.common.collect.Lists.newArrayList;
import static es.upv.grycap.coreutils.fiber.http.Http2Clients.http2Client;
import static es.upv.grycap.coreutils.fiber.test.mockserver.FiberExpectationInitializer.MOCK_SERVER_BASE_URL;
import static es.upv.grycap.coreutils.fiber.test.mockserver.ObjectResponseValidator.isValidJson;
import static es.upv.grycap.coreutils.fiber.test.mockserver.ObjectResponseValidator.isValidXml;
import static java.util.Optional.ofNullable;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import es.upv.grycap.coreutils.fiber.http.Http2Client;
import es.upv.grycap.coreutils.test.category.IntegrationTests;
import es.upv.grycap.coreutils.test.rules.TestPrinter;
import es.upv.grycap.coreutils.test.rules.TestWatcher2;
import net.jodah.concurrentunit.Waiter;

/**
 * Tests the {@link Http2Client}.
 * @author Erik Torres
 * @since 0.1.0
 */
@RunWith(Parameterized.class)
@Category(IntegrationTests.class)
public class Http2ClientTest {

	@Rule
	public TestPrinter pw = new TestPrinter();

	@Rule
	public TestRule watchman = new TestWatcher2(pw);

	/**
	 * Provides an input dataset with different data formats (JSON, XML) and different access methods (URL fragment, 
	 * query parameter). Some tests will surpass the concurrency level.
	 * @return Parameters for the different test scenarios.
	 */
	@Parameters(name = "{index}: method={0}, path={1}, objectId={2}, contentType={3}, nocache={4}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			/* 0*/ { "GET",    "/test/json/1",   "1", of("application/json"), true },
			/* 1*/ { "GET",    "/test/json/2",   "2", of("application/json"), true },
			/* 2*/ { "GET",    "/test/xml/1",    "1", of("application/xml"),  true },
			/* 3*/ { "GET",    "/test/xml/2",    "2", of("application/xml"),  true },
			/* 4*/ { "GET",    "/test/json?q=1", "1", of("application/json"), true },
			/* 5*/ { "GET",    "/test/json?q=2", "2", of("application/json"), true },
			/* 6*/ { "GET",    "/test/xml?q=1",  "1", of("application/xml"),  true },
			/* 7*/ { "GET",    "/test/xml?q=2",  "2", of("application/xml"),  true },
			/* 8*/ { "GET",    "/test/json/1",   "1", null,                   true },
			/* 9*/ { "GET",    "/test/xml/1",    "1", null,                   true },
			/*10*/ { "GET",    "/test/json/1",   "1", null,                   false },
			/*11*/ { "GET",    "/test/xml/1",    "1", null,                   false },
			/*12*/ { "POST",   "/test/json",     "X", of("application/json"), true  },
			/*13*/ { "POST",   "/test/xml",      "Y", of("application/xml"),  true  },
			/*14*/ { "PUT",    "/test/json/1",   "1", of("application/json"), true  },
			/*15*/ { "PUT",    "/test/xml/2",    "2", of("application/xml"),  true  },
			/*16*/ { "DELETE", "/test/json/2",   "2", of("application/json"), true  },
			/*17*/ { "DELETE", "/test/xml/1",    "1", of("application/xml"),  true  }
		});
	}

	@Parameter(value = 0) public String method;
	@Parameter(value = 1) public String path;
	@Parameter(value = 2) public String objectId;
	@Parameter(value = 3) public List<String> contentType;
	@Parameter(value = 4) public boolean nocache;

	@Test
	public void test() throws Exception {
		// create the client
		final Http2Client client = http2Client();
		assertThat("HTTP+SPDY client was created", client, notNullValue());

		// prepare the test
		final String url = new StringBuilder(MOCK_SERVER_BASE_URL).append(path).toString();		
		final Waiter waiter = new Waiter();

		// submit request
		switch (method) {
		case "POST":
			submitPost(client, url, waiter);
			break;
		case "PUT":
			submitPut(client, url, waiter);
			break;
		case "DELETE":
			submitDelete(client, url, waiter);
			break;
		case "GET":
		default:
			submitGet(client, url, waiter);
			break;
		}
		waiter.await(30l, TimeUnit.SECONDS);
	}

	private void submitGet(final Http2Client client, final String url, final Waiter waiter) {
		client.asyncGet(url, contentType, nocache, new Callback() {
			@Override
			public void onResponse(final Response response) throws IOException {
				waiter.assertTrue(response.isSuccessful());
				final String payload = response.body().source().readUtf8();
				waiter.assertThat(payload, allOf(notNullValue(), not(equalTo(""))));
				if (path.contains("xml")) {
					isValidXml(payload, objectId);
				} else if (path.contains("json")) {
					isValidJson(payload, objectId);
				}
				waiter.resume();
			}
			@Override
			public void onFailure(final Request request, final IOException throwable) {
				waiter.fail(throwable);			
			}
		});
	}

	private void submitPost(final Http2Client client, final String url, final Waiter waiter) {
		Supplier<String> supplier = null;
		if (path.contains("xml")) {
			supplier = () -> "<object>Y</object>";
		} else if (path.contains("json")) {
			supplier = () -> "{ \"object\" : \"X\" }";
		}
		client.asyncPost(url, ofNullable(contentType).orElse(newArrayList("application/json")).get(0), supplier, new Callback() {
			@Override
			public void onResponse(final Response response) throws IOException {
				waiter.assertTrue(response.isSuccessful());
				waiter.resume();
			}
			@Override
			public void onFailure(final Request request, final IOException throwable) {
				waiter.fail(throwable);			
			}
		});
	}

	private void submitPut(final Http2Client client, final String url, final Waiter waiter) {
		Supplier<String> supplier = null;
		if (path.contains("xml")) {
			supplier = () -> "<object>Y</object>";
		} else if (path.contains("json")) {
			supplier = () -> "{ \"object\" : \"X\" }";
		}
		client.asyncPut(url, ofNullable(contentType).orElse(newArrayList("application/json")).get(0), supplier, new Callback() {
			@Override
			public void onResponse(final Response response) throws IOException {
				waiter.assertTrue(response.isSuccessful());
				waiter.resume();
			}
			@Override
			public void onFailure(final Request request, final IOException throwable) {
				waiter.fail(throwable);			
			}
		});
	}

	private void submitDelete(final Http2Client client, final String url, final Waiter waiter) {
		client.asyncDelete(url, new Callback() {
			@Override
			public void onResponse(final Response response) throws IOException {				
				waiter.assertTrue(response.isSuccessful());
				waiter.resume();
			}
			@Override
			public void onFailure(final Request request, final IOException throwable) {
				waiter.fail(throwable);			
			}
		});
	}

}