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

package es.upv.grycap.coreutils.fiber.net;

import static java.net.URLDecoder.decode;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.stripEnd;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * Builder for {@link URL} instances.
 * @author Erik Torres
 * @since 0.1.0
 */
public class UrlBuilder {

	/**
	 * The base URL that the methods provided by this class will use to create new URLs.
	 */
	private final URL baseUrl;

	private UrlBuilder(final URL baseUrl) {		
		this.baseUrl = baseUrl;		
	}

	/**
	 * Convenient factory method that creates a new instance of this class.
	 * @param baseUrl - base URL that will be used to create new URLs
	 * @return A new instance of this class, allowing callers to use the methods of this class to create new URLs 
	 *         from the base URL provided as argument.
	 */
	public static UrlBuilder getUrlBuilder(final URL baseUrl) {
		URL url = null;
		try {			
			url = new URL(decode(requireNonNull(baseUrl).toString(), defaultCharset().name()));
		} catch (MalformedURLException | UnsupportedEncodingException e) {
			throw new IllegalArgumentException(new StringBuilder("Cannot create a URI from the provided URL: ")
					.append(baseUrl != null ? baseUrl.toString() : "null").toString(), e);
		}
		return new UrlBuilder(url);
	}

	/**
	 * Creates a new URL relative to the base URL provided in the constructor of this class. The new relative URL
	 * includes the path, query parameters and the internal reference of the {@link UrlBuilder#baseUrl base URL} 
	 * provided with this class. An additional fragment, as well as additional query parameters can be optionally 
	 * added to the new URL. In addition to the parameters passed to the method as an argument, the supplied 
	 * fragment can also include parameters that will be added to the created URL. The created URL is normalized 
	 * and unencoded before returning it to the caller. The current implementation has the following limitations:
	 * <ul>
	 * <li>Arrays are not supported: <tt>q=foo&amp;q=bar</tt> will produce an error.</li>
	 * <li>Internal references are only supported in the base URL. Any additional reference provided with the 
	 * fragment will be silently ignored: the fragment <tt>/rd#ref</tt> will be appended to the base URL as
	 * <tt>/rd</tt>, ignoring the internal reference.</li>
	 * </ul>
	 * @param fragment - optional URL fragment (may include parameters, but not references) that will be added 
	 *                   to the base URL
	 * @param params - optional query parameters that will be added to the base URL
	 * @return A relative URL created from the base URL provided in the constructor of this class and adding the
	 *         fragment and parameters passed as arguments to this method.
	 */
	public String buildRelativeUrl(final @Nullable String fragment, final @Nullable Map<String, String> params) {
		String url = null;		
		final Optional<String> fragment2 = ofNullable(trimToNull(fragment));
		try {
			final Optional<URL> fragmentUrl = ofNullable(fragment2.isPresent() ? new URL("http://example.com/" + fragment2.get()) : null);			
			final URIBuilder uriBuilder = new URIBuilder();
			// add path
			uriBuilder.setPath(new StringBuilder(ofNullable(trimToNull(baseUrl.getPath())).orElse("/"))
					.append(fragmentUrl.isPresent() ? "/" + stripEnd(fragmentUrl.get().getPath(), "/") : "")
					.toString().replaceAll("[/]{2,}", "/"));
			// add query parameters
			if (isNotBlank(baseUrl.getQuery())) {
				uriBuilder.setParameters(URLEncodedUtils.parse(baseUrl.getQuery(), defaultCharset()));
			}
			if (fragmentUrl.isPresent() && isNotBlank(fragmentUrl.get().getQuery())) {
				URLEncodedUtils.parse(fragmentUrl.get().getQuery(), defaultCharset()).stream().forEach(p -> {
					uriBuilder.addParameter(p.getName(), p.getValue());
				});
			}
			ofNullable(params).orElse(emptyMap()).entrySet().stream().forEach(p -> {
				uriBuilder.addParameter(p.getKey(), p.getValue());
			});
			// add internal reference
			uriBuilder.setFragment(baseUrl.getRef());
			// build relative URL
			url = uriBuilder.build().normalize().toString();
		} catch (MalformedURLException | URISyntaxException e) {
			throw new IllegalStateException(new StringBuilder("Failed to create relative URL from provided parameters: fragment=")
					.append(fragment2.orElse("null")).append(", params=").append(params != null ? params.toString() : "null").toString(), e);
		}
		return url;
	}

}