/*
 * Core Utils - Common Utilities.
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

package es.upv.grycap.coreutils.common.test;

import static es.upv.grycap.coreutils.common.CoreutilsContext.COREUTILS_CONTEXT;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Objects;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;

import es.upv.grycap.coreutils.common.BaseShutdownListener;
import es.upv.grycap.coreutils.test.category.FunctionalTests;
import es.upv.grycap.coreutils.test.rules.TestPrinter;
import es.upv.grycap.coreutils.test.rules.TestWatcher2;

/**
 * Tests the context.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.2.0
 */
@Category(FunctionalTests.class)
public class CoreutilsContextTest {

	@Rule
	public TestPrinter pw = new TestPrinter(true);

	@Rule
	public TestRule watchman = new TestWatcher2(pw);

	@BeforeClass
	public static void setup() {
		COREUTILS_CONTEXT.addShutdownListener(new FakeShutdownListener(), FakeShutdownListener.class, "duplicate");
	}

	@AfterClass
	public static void cleanup() {
		COREUTILS_CONTEXT.removeShutdownListener(FakeShutdownListener.class, "duplicate");
	}

	@Test
	public void testRegistry() throws Exception {		
		assertThat("Client is null", COREUTILS_CONTEXT.getClient(FakeClient.class), nullValue());

		final FakeClient client = COREUTILS_CONTEXT.getClient(FakeClient.class, FakeClient::new);
		assertThat("Client is not null", client, notNullValue());

		FakeClient client2 = COREUTILS_CONTEXT.getClient(FakeClient.class, FakeClient::new);
		assertThat("Client coincides with expected", client2, allOf(notNullValue(), equalTo(client)));

		client2 = COREUTILS_CONTEXT.getClient(FakeClient.class, "classifier", FakeClient::new);
		assertThat("Client coincides with expected", client2, allOf(notNullValue(), not(equalTo(client))));
	}

	@Test
	public void testShutdownhook() throws Exception {
		COREUTILS_CONTEXT.addShutdownListener(new FakeShutdownListener(), FakeShutdownListener.class);		
		COREUTILS_CONTEXT.removeShutdownListener(FakeShutdownListener.class);
	}

	@Test(expected=IllegalStateException.class) 
	public void testDuplicateShutdownhook() {
		COREUTILS_CONTEXT.addShutdownListener(new FakeShutdownListener(), FakeShutdownListener.class, "duplicate");
	}

	/**
	 * Client mock-up.
	 * @author Erik Torres <etserrano@gmail.com>
	 * @since 0.2.0
	 */
	public static class FakeClient {

		private final int id = new Random().nextInt();

		public int getId() {
			return id;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof FakeClient)) {
				return false;
			}
			final FakeClient other = FakeClient.class.cast(obj);
			return Objects.equals(id, other.id);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id);
		}

	}

	/**
	 * Shutdown listener mock-up.
	 * @author Erik Torres <etserrano@gmail.com>
	 * @since 0.2.0
	 */
	public static class FakeShutdownListener extends BaseShutdownListener { }

}