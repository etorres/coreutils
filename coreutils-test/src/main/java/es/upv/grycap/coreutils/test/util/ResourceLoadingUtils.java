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

package es.upv.grycap.coreutils.test.util;

import static java.io.File.separator;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Utilities to load test resources from class-path and file-system.
 * @author Erik Torres
 * @since 0.1.0
 */
public class ResourceLoadingUtils {

	/**
	 * Test resource directory is scanned for this file in order to find the directory where the application made available the
	 * files that contain test cases to validate the different test scenarios. A directory named {@link #TEST_DIRNAME} should be
	 * created at the same level of this file and the test files should be placed there.
	 */
	public static final String ANCHOR_FILENAME = "grycap-testfiles-m2";

	/**
	 * Name of the directory where the test files are located. Files can be organized as needed inside this directory (e.g. using
	 * subdirectories).
	 */
	public static final String TEST_DIRNAME = "testfiles";

	/**
	 * Finds the directory where the test files are located.
	 */
	public static final String TEST_RESOURCES_DIR;
	static {
		String candidateDir = null;
		File anchorFile = null;
		try {
			anchorFile = new File(ResourceLoadingUtils.class.getClassLoader().getResource(ANCHOR_FILENAME).toURI());
		} catch (Exception e) {
			anchorFile = new File(System.getProperty("user.dir"));
		} finally {
			final File resDir = new File(new StringBuffer(anchorFile.getParent()).append(separator).append(TEST_DIRNAME).toString());
			if (resDir != null && resDir.isDirectory() && resDir.canRead()) {
				try {
					candidateDir = resDir.getCanonicalPath();
				} catch (IOException e) {
					// nothing to do
				}
			}
		}
		TEST_RESOURCES_DIR = candidateDir;			
	}

	/**
	 * Lists the resources with filenames that end with one of the provided extensions. For each matching resource
	 * the path name is provided. Files are not created to prevent the limits that OS could impose on the simultaneous
	 * number of opened files.
	 * @param dirname - name of the target directory
	 * @param extensions - valid file extensions
	 * @return The resources with filenames that end with one of the provided extensions.
	 */
	public static List<String> getResourceFiles(final String dirname, final @Nullable String[] extensions) {
		checkResourcesDir();
		final File dir = new File(new StringBuffer(TEST_RESOURCES_DIR).append(separator).append(dirname).toString());
		return asList(ofNullable(dir.listFiles(new ExtensionFilter(extensions))).orElse(new File[]{})).stream()
				.map(File::getAbsolutePath)
				.collect(Collectors.toList());
	}

	private static void checkResourcesDir() {
		if (TEST_RESOURCES_DIR == null) {
			throw new IllegalStateException("Invalid test resources pathname: " + TEST_RESOURCES_DIR);		
		}
	}

	/**
	 * A filename filter that matches the files by their extension.
	 * @author Erik Torres
	 * @since 0.1.0
	 */
	public static class ExtensionFilter implements FilenameFilter {

		private final List<String> extensions;

		/**
		 * Convenient constructor to initialize the valid file extensions from an array.
		 * @param extensions - valid file extensions
		 */
		public ExtensionFilter(final @Nullable String[] extensions) {
			this(asList(ofNullable(extensions).orElse(new String[]{})));			
		}

		/**
		 * Convenient constructor to initialize the valid file extensions from a list.
		 * @param extensions - valid file extensions
		 */
		public ExtensionFilter(final @Nullable List<String> extensions) {
			this.extensions = ofNullable(extensions).orElse(new ArrayList<>()).stream()
					.map(e -> e != null ? e.trim().toLowerCase() : null)
					.filter(Objects::nonNull).collect(Collectors.toList());
		}

		@Override
		public boolean accept(final File file, final String name) {
			final String name2 = (name != null ? name.trim().toLowerCase() : null);
			if (name2 == null) return false;
			return extensions.stream().anyMatch(e -> name2.endsWith(e));
		}

	}

}