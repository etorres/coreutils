/*
 * Core Utils - Logging utilities.
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

package es.upv.grycap.coreutils.logging.test;

import static es.upv.grycap.coreutils.logging.LogManager.getLogManager;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;
import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import es.upv.grycap.coreutils.test.category.FunctionalTests;
import es.upv.grycap.coreutils.test.rules.TestPrinter;
import es.upv.grycap.coreutils.test.rules.TestWatcher2;

/**
 * Tests the log manager.
 * @author Erik Torres
 * @since 0.1.0
 */
@Category(FunctionalTests.class)
public class LogManagerTest {

	private static final String TESTLOG_FILENAME = new StringBuilder(System.getProperty("java.io.tmpdir")).append(System.getProperty("file.separator"))
			.append("coreutils-logging-test.log").toString();

	@Rule
	public TestPrinter pw = new TestPrinter();

	@Rule
	public TestRule watchman = new TestWatcher2(pw);

	@BeforeClass
	public static void cleanup() {
		deleteQuietly(new File(TESTLOG_FILENAME));
	}

	@AfterClass
	public static void reset() {
		getLogManager().reset();
	}

	@Test
	public void testFactory() throws Exception {
		// setup logging via the factory method that internally calls the initialization method
		final Logger logger = es.upv.grycap.coreutils.logging.ExtendedLoggerFactory.getLogger(DummyClass.class);
		assertThat("Logger is not null", logger, notNullValue());

		// j.u.l. logger
		String msg = "This message was generated with j.u.l., logged with Logback+SL4J";
		final java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(LogManagerTest.class.getCanonicalName());
		julLogger.setLevel(java.util.logging.Level.INFO);		
		julLogger.info(msg);
		assertThatLogfileContains(msg);

		// log4j logger
		msg = "This message was generated with log4j, logged with Logback+SL4J";
		final org.apache.log4j.Logger log4jLogger = org.apache.log4j.Logger.getLogger(LogManagerTest.class);
		log4jLogger.setLevel(org.apache.log4j.Level.INFO);
		log4jLogger.info(msg);
		assertThatLogfileContains(msg);

		// SLF4J logger
		msg = "This message was generated with SL4J, logged with Logback+SL4J";
		final org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(LogManagerTest.class);
		slf4jLogger.info(msg);
		assertThatLogfileContains(msg);

		// print logback internal state
		if (pw.isPrintEnabled()) {
			final LoggerContext loggerContext = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
			StatusPrinter.print(loggerContext);
		}
	}

	private void assertThatLogfileContains(final String msg) throws IOException {
		assertThat("Log file contains expected message", Files.lines(Paths.get(TESTLOG_FILENAME)).anyMatch(line -> line.contains(msg)));
	}

	/**
	 * Dummy class.
	 * @author Erik Torres
	 * @since 0.1.0
	 */
	public static class DummyClass { }

}