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

package es.upv.grycap.coreutils.fiber.test.mockserver;

import static com.google.common.collect.ImmutableMap.of;
import static es.upv.grycap.coreutils.fiber.test.mockserver.ObjectResponseFactory.jsonObject1;
import static es.upv.grycap.coreutils.fiber.test.mockserver.ObjectResponseFactory.jsonObject2;
import static es.upv.grycap.coreutils.fiber.test.mockserver.ObjectResponseFactory.xmlObject1;
import static es.upv.grycap.coreutils.fiber.test.mockserver.ObjectResponseFactory.xmlObject2;
import static java.util.Collections.emptyMap;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;

import java.util.concurrent.TimeUnit;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.initialize.ExpectationInitializer;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;

import com.google.common.collect.ImmutableList;

/**
 * Sets Mock Server expectations for fetching tests.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.1.0
 */
public class FiberExpectationInitializer implements ExpectationInitializer {

	public static final String MOCK_SERVER_BASE_URL = "http://localhost:9080";

	@Override
	public void initializeExpectations(final MockServerClient client) {
		// add GET operations
		new ImmutableList.Builder<ExpectationHttpGetResponse>()
		// REST + JSON
		.add(new ExpectationHttpGetResponse("/test/json/ids", "text/plain",       "1,2",           0l))		
		.add(new ExpectationHttpGetResponse("/test/json/1",   "application/json", jsonObject1(), 300l))
		.add(new ExpectationHttpGetResponse("/test/json/2",   "application/json", jsonObject2(), 500l))
		// REST + XML
		.add(new ExpectationHttpGetResponse("/test/xml/ids",  "text/plain",       "1,2",           0l))
		.add(new ExpectationHttpGetResponse("/test/xml/1",    "application/xml",  xmlObject1(),    0l))
		.add(new ExpectationHttpGetResponse("/test/xml/2",    "application/xml",  xmlObject2(),    0l))
		// Query parameter + JSON
		.add(new ExpectationHttpGetResponse("/test/json", of("q", "1"), "application/json", jsonObject1(),  100l))
		.add(new ExpectationHttpGetResponse("/test/json", of("q", "2"), "application/json", jsonObject2(),    0l))
		// Query parameter + XML
		.add(new ExpectationHttpGetResponse("/test/xml",  of("q", "1"), "application/xml",  xmlObject1(),   300l))
		.add(new ExpectationHttpGetResponse("/test/xml",  of("q", "2"), "application/xml",  xmlObject2(),   120l))
		// Very long awaiting time
		.add(new ExpectationHttpGetResponse("/test/long-waiting/1", "text/plain", "Hello World!", 20000l))
		.build().stream().forEach(r -> {
			client.when(request().withMethod("GET").withPath(r.getPath()).withQueryStringParameters(r.getParameters()))
			.respond(response().withStatusCode(r.getStatusCode())
					.withHeaders(new Header("Content-Type", r.getContentType() + "; charset=utf-8"),
							new Header("Cache-Control", "public, max-age=86400"))
					.withBody(r.getBody())
					.withDelay(new Delay(TimeUnit.MILLISECONDS, r.getDelay())));
		});
		// add POST operations
		new ImmutableList.Builder<ExpectationHttpPostResponse>()
		.add(new ExpectationHttpPostResponse("/test/json", "application/json", "{ \"object\" : \"X\" }", 0l))
		.add(new ExpectationHttpPostResponse("/test/xml", "application/xml", "<object>Y</object>", 0l))
		.build().stream().forEach(r -> {
			client.when(request().withMethod("POST").withPath(r.getPath()).withQueryStringParameters(r.getParameters())
					.withBody(exact(r.getBody())), exactly(1))
			.respond(response().withStatusCode(r.getStatusCode())
					.withHeaders(new Header("Cache-Control", "public, max-age=86400"),
							new Header("Location", r.getPath() + "/createdId"))
					.withDelay(new Delay(TimeUnit.MILLISECONDS, r.getDelay())));			
		});
		// add PUT operations
		new ImmutableList.Builder<AbstractExpectationResponse>()
		.add(new AbstractExpectationResponse("/test/json/1", emptyMap(), 204, "application/json", "{ \"object\" : \"X\" }", 0l){ })
		.add(new AbstractExpectationResponse("/test/json/2", emptyMap(), 204, "application/json", "{ \"object\" : \"X\" }", 0l){ })
		.add(new AbstractExpectationResponse("/test/xml/1",  emptyMap(), 204, "application/xml",  "<object>Y</object>",     0l){ })
		.add(new AbstractExpectationResponse("/test/xml/2",  emptyMap(), 204, "application/xml",  "<object>Y</object>",     0l){ })		
		.build().stream().forEach(r -> {
			client.when(request().withMethod("PUT").withPath(r.getPath()).withQueryStringParameters(r.getParameters())
					.withBody(exact(r.getBody())), exactly(1))
			.respond(response().withStatusCode(r.getStatusCode())
					.withHeaders(new Header("Cache-Control", "public, max-age=86400"))
					.withDelay(new Delay(TimeUnit.MILLISECONDS, r.getDelay())));
		});
		// add DELETE operations
		new ImmutableList.Builder<AbstractExpectationResponse>()
		.add(new AbstractExpectationResponse("/test/json/1", emptyMap(), 204, "application/json", jsonObject1(), 0l){ })
		.add(new AbstractExpectationResponse("/test/json/2", emptyMap(), 204, "application/json", jsonObject2(), 0l){ })
		.add(new AbstractExpectationResponse("/test/xml/1",  emptyMap(), 204, "application/xml",  xmlObject1(),  0l){ })
		.add(new AbstractExpectationResponse("/test/xml/2",  emptyMap(), 204, "application/xml",  xmlObject2(),  0l){ })		
		.build().stream().forEach(r -> {
			client.when(request().withMethod("DELETE").withPath(r.getPath()).withQueryStringParameters(r.getParameters()),
					exactly(1))
			.respond(response().withStatusCode(r.getStatusCode())
					.withHeaders(new Header("Cache-Control", "public, max-age=86400"))
					.withDelay(new Delay(TimeUnit.MILLISECONDS, r.getDelay())));
		});
		// highly concurrent
		for (int i = 0; i < 100; i++) {
			client.when(request().withMethod("GET").withPath(String.format("/test/concurrent/%d", i)))
			.respond(response().withStatusCode(200)
					.withHeaders(new Header("Content-Type", "text/plain; charset=utf-8"),
							new Header("Cache-Control", "public, max-age=86400"))
					.withBody(new StringBuilder("Response-to-").append(i).toString()));
		}
	}

}