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
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;

import com.google.common.collect.ImmutableList;

import co.paralleluniverse.fibers.futures.AsyncCompletionStage;
import es.upv.grycap.coreutils.fiber.http.HttpDataFetcher;
import es.upv.grycap.coreutils.fiber.http.HttpDataFetcher.FetchStatus;
import es.upv.grycap.coreutils.test.category.IntegrationTests;
import es.upv.grycap.coreutils.test.rules.TestPrinter;
import es.upv.grycap.coreutils.test.rules.TestWatcher2;

/**
 * Tests data fetching with cancellation.
 * @author Erik Torres
 * @since 0.1.0
 */
@Category(IntegrationTests.class)
public class FetchCancellationTest {

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	@Rule
	public TestPrinter pw = new TestPrinter();

	@Rule
	public TestRule watchman = new TestWatcher2(pw);

	@Test
	public void testFetch() throws Exception {
		// create fetcher
		final HttpDataFetcher fetcher = new HttpDataFetcher(2);
		assertThat("Fetcher was created", fetcher, notNullValue());

		// create output folder
		final File outDir = tmpFolder.newFolder(randomAlphanumeric(12));
		assertThat("Output dir was created", outDir, notNullValue());
		assertThat("Output dir is writable", outDir.canWrite());

		// submit request and cancel
		final ExecutorService executorService = Executors.newFixedThreadPool(2);
		final Future<Map<String, FetchStatus>> future = executorService.submit(new Callable<Map<String, FetchStatus>>() {
			@Override
			public Map<String, FetchStatus> call() throws Exception {
				return AsyncCompletionStage.get(fetcher.fetchToDir(new URL(MOCK_SERVER_BASE_URL + "/fetch/long-waiting"), 
						ImmutableList.of("1"), outDir), 120000l, TimeUnit.SECONDS);
			}
		});
		assertThat("Request was cancelled", future.cancel(true));
		assertThat("File does not exist", not(new File(outDir, "1").exists()));
	}

}