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

import static es.upv.grycap.coreutils.fiber.test.mockserver.FiberExpectationInitializer.MOCK_SERVER_BASE_URL;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;

import com.google.common.collect.ImmutableList;

import es.upv.grycap.coreutils.fiber.http.HttpDataFetcher;
import es.upv.grycap.coreutils.test.category.SanityTests;
import es.upv.grycap.coreutils.test.rules.TestPrinter;
import es.upv.grycap.coreutils.test.rules.TestWatcher2;

/**
 * Tests fetcher with invalid inputs.
 * @author Erik Torres
 * @since 0.1.0
 */
@Category(SanityTests.class)
public class FetchValidationTest {

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	@Rule
	public TestPrinter pw = new TestPrinter();

	@Rule
	public TestRule watchman = new TestWatcher2(pw);

	@Test(expected=NullPointerException.class)
	public void testFetchWithoutUrl() throws Exception {
		final HttpDataFetcher fetcher = new HttpDataFetcher(2);
		assertThat("Fetcher was created", fetcher, notNullValue());
		fetcher.fetchToDir(null, ImmutableList.of("1"), tmpFolder.newFolder(randomAlphanumeric(12)));
	}

	@Test(expected=NullPointerException.class)
	public void testFetchWithoutList() throws Exception {
		final HttpDataFetcher fetcher = new HttpDataFetcher(2);
		assertThat("Fetcher was created", fetcher, notNullValue());		
		fetcher.fetchToDir(new URL(MOCK_SERVER_BASE_URL + "/test/json"), null, tmpFolder.newFolder(randomAlphanumeric(12)));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFetchWithEmptyList() throws Exception {
		final HttpDataFetcher fetcher = new HttpDataFetcher(2);
		assertThat("Fetcher was created", fetcher, notNullValue());		
		fetcher.fetchToDir(new URL(MOCK_SERVER_BASE_URL + "/test/json"), emptyList(), tmpFolder.newFolder(randomAlphanumeric(12)));
	}

	@Test(expected=NullPointerException.class)
	public void testFetchWithoutOutputDir() throws Exception {
		final HttpDataFetcher fetcher = new HttpDataFetcher(2);
		assertThat("Fetcher was created", fetcher, notNullValue());		
		fetcher.fetchToDir(new URL(MOCK_SERVER_BASE_URL + "/test/json"), ImmutableList.of("1"), null);
	}

}