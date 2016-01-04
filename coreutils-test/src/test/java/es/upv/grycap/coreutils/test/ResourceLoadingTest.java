/*
 * Core Utils - Testing utilities.
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

package es.upv.grycap.coreutils.test;

import static es.upv.grycap.coreutils.test.util.ResourceLoadingUtils.getResourceFiles;
import static java.io.File.separator;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;

import es.upv.grycap.coreutils.test.category.FunctionalTests;
import es.upv.grycap.coreutils.test.rules.TestPrinter;
import es.upv.grycap.coreutils.test.rules.TestWatcher2;
import es.upv.grycap.coreutils.test.util.ResourceLoadingUtils;

/**
 * Tests resource loading utilities provided by {@link ResourceLoadingUtils}.
 * @author Erik Torres
 * @since 0.1.0
 */
@Category(FunctionalTests.class)
public class ResourceLoadingTest {

	@Rule
	public TestPrinter pw = new TestPrinter();

	@Rule
	public TestRule watchman = new TestWatcher2(pw);

	@Test
	public void testSimpleDirectoryHierarchy() {
		// test simple directory hierarchy
		final List<String> files = getResourceFiles("examples", new String[]{ ".txt", ".log" });
		assertThat("list of files coincides with expected", files, allOf(notNullValue(), hasSize(3)));		
		final String filenames = files.stream().map(s -> s + "\n").reduce("", String::concat);
		assertThat("all expected files are included", filenames, allOf(containsString("example1.txt"),
				containsString("example2.txt"), containsString("example4.log")));
		pw.println("\n >> Files: " + files);
	}

	@Test
	public void testNestedDirectoryHierarchy() {
		// test complex directory hierarchy
		final List<String> files = getResourceFiles(new StringBuffer("examples").append(separator)
				.append("inner_examples").toString(), new String[]{ ".txt" });
		assertThat("list of files coincides with expected", files, allOf(notNullValue(), hasSize(1)));
		final String filenames = files.stream().map(s -> s + "\n").reduce("", String::concat);
		assertThat("all expected files are included", filenames, containsString("example.txt"));
		pw.println("\n >> Files: " + files);
	}

	@Test
	public void testInvalidDirectory() {
		// test empty directory
		final List<String> files = getResourceFiles("i_dont_exist", new String[]{ ".txt" });
		assertThat("list of files coincides with expected", files, allOf(notNullValue(), empty()));		
	}

	@Test
	public void testEmptyExtensionList() {
		// test empty extension list
		final List<String> files = getResourceFiles("examples", null);
		assertThat("list of files coincides with expected", files, allOf(notNullValue(), empty()));		
	}

}