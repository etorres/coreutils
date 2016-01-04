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

package es.upv.grycap.coreutils.test.rules;

import static java.util.Optional.ofNullable;

import javax.annotation.Nullable;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Keeps a log of each passing and failing test.
 * @author Erik Torres
 * @since 0.1.0
 */
public class TestWatcher2 extends TestWatcher {	

	/**
	 * Default header that will be printed before the status messages.
	 */
	private static final String DEFAULT_HEADER = " >>> TestUtils >>> ";

	/**
	 * Default footer that will be printed after the status messages.
	 */
	private static final String DEFAULT_FOOTER = "";

	private final TestPrinter pw;
	private final String header;
	private final String footer;

	/**
	 * Convenient constructor that configures the class to use the default header and footer.
	 * @param pw - the printer that will be used to print the status messages
	 */
	public TestWatcher2(final TestPrinter pw) {
		this(pw, DEFAULT_HEADER, DEFAULT_FOOTER);
	}

	/**
	 * Convenient constructor that receives initial values of the class attributes as parameters.
	 * @param pw - the printer that will be used to print the status messages
	 * @param header - optional header that will be printed before the status messages or {@link #DEFAULT_HEADER} 
	 *                 when a <tt>null</tt> value is passed
	 * @param footer - optional footer that will be printed after the status messages or {@link #DEFAULT_FOOTER} 
	 *                 when a <tt>null</tt> value is passed
	 */
	public TestWatcher2(final TestPrinter pw, final @Nullable String header, final @Nullable String footer) {
		this.pw = pw;
		this.header = ofNullable(header).orElse(DEFAULT_HEADER);
		this.footer = ofNullable(footer).orElse(DEFAULT_FOOTER);;
	}

	@Override
	protected void succeeded(final Description description) {
		pw.println(new StringBuffer(header).append(description.getDisplayName()).append(" ").append("succeeded!").append(footer).toString());		
	}

	@Override
	protected void failed(final Throwable e, final Description description) {
		pw.println(new StringBuffer(header).append(description.getDisplayName()).append(" ").append("failed!").append(footer).toString());
	}

	@Override
	protected void skipped(final AssumptionViolatedException e, final Description description) {
		pw.println(new StringBuffer(header).append(description.getDisplayName()).append(" ").append("skipped!").append(footer).toString());
	}

	@Override
	protected void starting(final Description description) {
		pw.println(new StringBuffer(header).append(description.getDisplayName()).append(" ").append("starting...").append(footer).toString());
	}

}