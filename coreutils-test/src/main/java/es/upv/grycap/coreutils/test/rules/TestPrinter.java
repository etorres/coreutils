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

import org.junit.rules.ExternalResource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Provides common methods for printing objects during tests execution. This class can be configured to silently ignore
 * print operations, producing no output.
 * @author Erik Torres
 * @since 0.1.0
 */
public class TestPrinter extends ExternalResource {

	private final boolean print;

	/**
	 * Default constructor gets its configuration from the system environment.
	 */
	public TestPrinter() {
		this(System.getProperty("grycap.tests.print.out", "false").equals("true"));
	}

	/**
	 * Convenient constructor that receives configuration parameters during class creation.
	 * @param print - flag to enable/disable message printing to the STDOUT
	 */
	public TestPrinter(final boolean print) {
		this.print = print;
	}

	/**
	 * Checks whether the printer is configured to print the messages or not.
	 * @return <tt>true</tt> when the printer is configured to print the messages, otherwise <tt>false</tt>.
	 */
	public boolean isPrintEnabled() {
		return print;
	}

	/**
	 * Prints a new line character.
	 */
	public void println() {
		println("", OutputFormat.PLAIN);
	}

	/**
	 * Obtains a {@link String} representation of the provided object by calling the {@link Object#toString()} method and
	 * print its content to the standard output.
	 * @param obj - object that will be printed
	 */
	public void println(final @Nullable Object obj) {
		println(obj, OutputFormat.PLAIN);
	}

	/**
	 * Prints a {@link String} to the standard output.
	 * @param msg - message that will be printed
	 */
	public void println(final String msg) {
		println(msg, OutputFormat.PLAIN);
	}

	/**
	 * Obtains a JSON representation of the provided {@link String} and prints its content to the standard output.
	 * @param msg - message that will be printed
	 */
	public void printlnJson(final String msg) {
		println(msg, OutputFormat.JSON);
	}

	/**
	 * Transform the provided object to one of the supported data formats and print the object's content to the standard output.
	 * @param obj - object that will be printed
	 * @param format - data format that will be used to print the object
	 */
	private void println(final @Nullable Object obj, final @Nullable OutputFormat format) {
		switch (ofNullable(format).orElse(OutputFormat.PLAIN)) {
		case JSON:
			if (print) System.out.println(GsonHelper.gson.toJson(obj));
			break;
		case PLAIN:
		default:
			if (print) System.out.println(obj);
			break;
		}
	}

	/**
	 * Supported output formats.
	 * @author Erik Torres
	 */
	public enum OutputFormat {
		JSON,
		PLAIN
	}

	/**
	 * Lazy initialization of Gson-based JSON processor.
	 * @author Erik Torres
	 */
	private static class GsonHelper {		
		public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	}

}