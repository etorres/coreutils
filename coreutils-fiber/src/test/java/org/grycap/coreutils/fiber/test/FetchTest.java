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

package org.grycap.coreutils.fiber.test;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections.ListUtils.union;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.grycap.coreutils.fiber.test.mockserver.FiberExpectationInitializer.MOCK_SERVER_BASE_URL;
import static org.grycap.coreutils.fiber.test.mockserver.ObjectResponseValidator.validateJson;
import static org.grycap.coreutils.fiber.test.mockserver.ObjectResponseValidator.validateXml;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.grycap.coreutils.fiber.http.HttpDataFetcher;
import org.grycap.coreutils.fiber.http.HttpDataFetcher.FecthFuture;
import org.grycap.coreutils.fiber.http.HttpDataFetcher.FetchStatus;
import org.grycap.coreutils.test.category.IntegrationTests;
import org.grycap.coreutils.test.rules.TestPrinter;
import org.grycap.coreutils.test.rules.TestWatcher2;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;

import co.paralleluniverse.fibers.futures.AsyncCompletionStage;

/**
 * Tests data fetching.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.1.0
 */
@RunWith(Parameterized.class)
@Category(IntegrationTests.class)
public class FetchTest {

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	@Rule
	public TestPrinter pw = new TestPrinter();

	@Rule
	public TestRule watchman = new TestWatcher2(pw);

	/**
	 * Provides an input dataset with different data formats (JSON, XML) and different access methods (URL fragment, 
	 * query parameter). Some tests will surpass the concurrency level. Invalid identifiers are included to test that
	 * the fetcher handles 404 status responses.
	 * @return Parameters for the different test scenarios.
	 */
	@Parameters(name = "{index}: path={0}, queryParam={1}, validIds={2}, invalidIds={3}, concurrencyLevel={4}, useFiber={5}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			/*0*/ { "/test/json", null, ImmutableList.of("1", "2"), emptyList(),                     4, true },
			/*1*/ { "/test/xml",  "q",  ImmutableList.of("1", "2"), emptyList(),                     8, true },
			/*2*/ { "/test/json", null, emptyList(),                ImmutableList.of("3", "4"),      4, true },
			/*3*/ { "/test/json", null, ImmutableList.of("1", "2"), ImmutableList.of("3", "4"),      4, true },
			/*4*/ { "/test/xml",  "q",  ImmutableList.of("1", "2"), ImmutableList.of("5", "6", "7"), 2, true },
			/*5*/ { "/test/json", null, ImmutableList.of("1", "2"), emptyList(),                     8, false }
		});
	}

	@Parameter(value = 0) public String path;
	@Parameter(value = 1) public String queryParam;
	@Parameter(value = 2) public List<String> validIds;
	@Parameter(value = 3) public List<String> invalidIds;
	@Parameter(value = 4) public int concurrencyLevel;
	@Parameter(value = 5) public boolean useFiber;

	@Test
	public void testFetch() throws Exception {
		// create fetcher
		final HttpDataFetcher fetcher = new HttpDataFetcher(concurrencyLevel);
		assertThat("Fetcher was created", fetcher, notNullValue());

		// create output folder
		final File outDir = tmpFolder.newFolder(randomAlphanumeric(12));
		assertThat("Output dir was created", outDir, notNullValue());
		assertThat("Output dir is writable", outDir.canWrite());

		// create test dataset
		@SuppressWarnings("unchecked")
		final List<String> ids = union(validIds, invalidIds);
		final URL url = new URL(MOCK_SERVER_BASE_URL + path);

		// fetch from server
		Map<String, FetchStatus> results = null;
		if (useFiber) {
			results = AsyncCompletionStage.get(fetcher.fetchToDir(url, queryParam, ids, "file-", ".tmp", outDir), 30l, TimeUnit.SECONDS);
		} else {
			final FecthFuture toBeCompleted = fetcher.fetchToDir(url, queryParam, ids, null, null, outDir);
			assertThat("Fetch task was created", toBeCompleted, notNullValue());
			results = toBeCompleted.get(30l, TimeUnit.SECONDS);	
		}
		assertThat("Results are available", results, notNullValue());
		assertThat("Result count coincides with expected", results.size(), equalTo(ids.size()));
		assertThat("Ids coincides with expected", results.keySet(), hasItems(ids.toArray(new String[ids.size()])));
		pw.println(" >> Results: " + results);
		if (!validIds.isEmpty()) {
			assertThat("Status coincides with expected", results.values(), allOf(hasItem(FetchStatus.COMPLETED), 
					not(hasItems(FetchStatus.PENDING, FetchStatus.CANCELLED))));
		}
		if (!invalidIds.isEmpty()) {
			assertThat("Status coincides with expected", results.values(), allOf(hasItem(FetchStatus.FAILED), 
					not(hasItems(FetchStatus.PENDING, FetchStatus.CANCELLED))));
		}

		// check files in the output directory
		for (final String id : validIds) {
			final File outFile = new File(outDir, useFiber ? new StringBuilder("file-").append(id).append(".tmp").toString() : id);
			assertThat("Output file was created", outDir, notNullValue());
			assertThat("Output file file exists and is redable", outFile.canRead(), equalTo(true));
			assertThat("Output file file is not empty", outFile.length(), greaterThan(0l));
			if (path.contains("xml")) {
				validateXml(outFile, id);
			} else if (path.contains("json")) {
				validateJson(outFile, id);
			}
		}
	}

}