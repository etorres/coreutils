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

import static com.google.common.collect.ImmutableMap.of;
import static es.upv.grycap.coreutils.fiber.net.UrlBuilder.getUrlBuilder;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import es.upv.grycap.coreutils.fiber.net.UrlBuilder;
import es.upv.grycap.coreutils.test.category.SanityTests;
import es.upv.grycap.coreutils.test.rules.TestPrinter;
import es.upv.grycap.coreutils.test.rules.TestWatcher2;

/**
 * Tests {@link UrlBuilder}.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.1.0
 */
@RunWith(Parameterized.class)
@Category(SanityTests.class)
public class UrlBuilderTest {

	@Rule
	public TestPrinter pw = new TestPrinter();

	@Rule
	public TestRule watchman = new TestWatcher2(pw);

	/**
	 * Provides different input datasets.
	 * @return Parameters for the different test scenarios.
	 */
	@Parameters(name = "{index}: baseUrl={0}, fragment={1}, query={2}, expected={3}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			/* 0*/ { "http://example.com",              "",                        emptyMap(),                   "/" },
			/* 1*/ { "http://example.com",              "rd",                      emptyMap(),                   "/rd" },
			/* 2*/ { "http://example.com",              "",                        of("q", "foo"),               "/?q=foo" },
			/* 3*/ { "http://example.com",              "rd",                      of("q", "foo"),               "/rd?q=foo" },
			/* 4*/ { "http://example.com",              "/rd",                     of("q", "foo"),               "/rd?q=foo" },
			/* 5*/ { "http://example.com",              "/rd1/rd2",                of("q", "foo"),               "/rd1/rd2?q=foo" },
			/* 6*/ { "http://example.com",              "/rd1/rd2#ref",            of("q1", "foo", "q2", "bar"), "/rd1/rd2?q1=foo&q2=bar" },
			/* 7*/ { "http://example.com",              "/rd?q1=foo#ref",          of("q2", "bar"),              "/rd?q1=foo&q2=bar" },
			/* 8*/ { "http://example.com",              "/rd?q1=foo&q2=bar#ref",   of("q3", "baz"),              "/rd?q1=foo&q2=bar&q3=baz" },
			/* 9*/ { "http://example.com/rd1",          "rd2#ref",                 of("q1", "foo"),              "/rd1/rd2?q1=foo" },
			/*10*/ { "http://example.com/rd1",          "/rd2#ref",                of("q1", "foo"),              "/rd1/rd2?q1=foo" },
			/*11*/ { "http://example.com/rd1",          "/rd2",                    of("q1", "foo"),              "/rd1/rd2?q1=foo" },
			/*12*/ { "http://example.com/rd1",          "/rd2",                    emptyMap(),                   "/rd1/rd2" },
			/*13*/ { "http://example.com?q1=foo",       "rd",                      of("q2", "bar"),              "/rd?q1=foo&q2=bar" },
			/*14*/ { "http://example.com?q1=foo#ref",   "rd",                      of("q2", "bar"),              "/rd?q1=foo&q2=bar#ref" },
			/*15*/ { "http://example.com#ref",          "rd",                      of("q", "foo"),               "/rd?q=foo#ref" },			
			/*16*/ { "http://example.com/rd?q=foo#ref", "",                        of("q", "bar"),               "/rd?q=foo&q=bar#ref" },
			/*17*/ { "http://example.com#foo=bar",      "rd",                      emptyMap(),                   "/rd#foo=bar" },
			/*18*/ { "http://example.com/rd1%3Fq1%3Dfoo%26q2%3Dbar%23ref", "/rd2", of("q3", "baz"), "/rd1/rd2?q1=foo&q2=bar&q3=baz#ref" },
			/*19*/ { "http://example.com/hello+world=foo%26bar",           "rd",   emptyMap(),      "/hello%20world=foo&bar/rd"}
			// unsupported: { "http://example.com/rd", "", of("q", "foo", "q", "bar"), "/rd?q=foo&q=bar" },			
		});
	}

	@Parameter(value = 0) public String baseUrl;
	@Parameter(value = 1) public String fragment;
	@Parameter(value = 2) public Map<String, String> query;
	@Parameter(value = 3) public String expected;

	@Test
	public void testUrlBuilder() throws Exception {
		// create URL builder
		final UrlBuilder urlBuilder = getUrlBuilder(new URL(baseUrl));
		assertThat("URL builder was created", urlBuilder, notNullValue());

		// test URL
		final String url = urlBuilder.buildRelativeUrl(fragment, query);
		assertThat("URL coincides with expected", trim(url), allOf(notNullValue(), equalTo(expected)));
		pw.println("URL: " + url);
	}

}