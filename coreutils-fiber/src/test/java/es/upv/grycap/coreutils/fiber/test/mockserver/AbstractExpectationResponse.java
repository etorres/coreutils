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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mockserver.model.Parameter;

/**
 * Base definition of response expectations.
 * @author Erik Torres
 * @since 0.1.0
 */
public class AbstractExpectationResponse {

	private final String path;
	private final List<Parameter> parameters;
	private final int statusCode;
	private final String contentType;
	private final String body;
	private final long delay;

	/**
	 * Convenient constructor that receives initial values for all the parameters of this class.
	 * @param path - the path where clients expect the server is listening for incoming requests
	 * @param parameters - query parameters accompanying the request
	 * @param statusCode - the response status code after the request is processed in the server
	 * @param contentType - the content type of the server response
	 * @param body - the content of the body sent with the server response
	 * @param delay - the server will wait this time before sending a response to the client
	 */
	public AbstractExpectationResponse(final String path, final Map<String, String> parameters, final int statusCode, final String contentType, final String body, final long delay) {
		this.path = path;
		this.parameters = parameters.entrySet().stream().map(p -> {
			return new Parameter(p.getKey(), p.getValue());
		}).collect(Collectors.toList());
		this.statusCode = statusCode;
		this.contentType = contentType;
		this.body = body;
		this.delay = delay;
	}

	public String getPath() {
		return path;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getContentType() {
		return contentType;
	}

	public String getBody() {
		return body;
	}

	public long getDelay() {
		return delay;
	}

}