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

package org.grycap.coreutils.fiber.test.mockserver;

import static java.util.Collections.emptyMap;

import java.util.Map;

/**
 * Defines expectation responses for requests made with the HTTP POST method.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.1.0
 */
public class ExpectationHttpPostResponse extends AbstractExpectationResponse {

	/**
	 * Convenient constructor that can be used to create expectations where the server where the server responds 
	 * to all correct requests with the HTTP status code CREATED (201) to the proper requests and no query parameters 
	 * are submitted. This pattern is commonly used in REST and REST-like APIs. 
	 * @param path - the path where clients expect the server is listening for incoming requests
	 * @param contentType - the content type of the server response
	 * @param body - the content of the body sent with the server response
	 * @param delay - the server will wait this time before sending a response to the client
	 */
	public ExpectationHttpPostResponse(final String path, final String contentType, final String body, final long delay) {
		super(path, emptyMap(), 201, contentType, body, delay);
	}

	/**
	 * Convenient constructor that can be used to create expectations where the server responds 
	 * to all correct requests with the HTTP status code CREATED (201).
	 * @param path - the path where clients expect the server is listening for incoming requests
	 * @param parameters - query parameters accompanying the request
	 * @param contentType - the content type of the server response
	 * @param body - the content of the body sent with the server response
	 * @param delay - the server will wait this time before sending a response to the client
	 */
	public ExpectationHttpPostResponse(final String path, final Map<String, String> parameters, final String contentType, final String body, final long delay) {
		super(path, parameters, 201, contentType, body, delay);
	}

	/**
	 * Convenient constructor that receives initial values for all the parameters of this class.
	 * @param path - the path where clients expect the server is listening for incoming requests
	 * @param parameters - query parameters accompanying the request
	 * @param statusCode - the response status code after the request is processed in the server
	 * @param contentType - the content type of the server response
	 * @param body - the content of the body sent with the server response
	 * @param delay - the server will wait this time before sending a response to the client
	 */
	public ExpectationHttpPostResponse(final String path, final Map<String, String> parameters, final int statusCode, final String contentType, final String body, final long delay) {
		super(path, parameters, statusCode, contentType, body, delay);
	}

}
