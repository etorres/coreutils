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

package es.upv.grycap.coreutils.logging;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;

/**
 * Wraps the SLF4J {@link org.slf4j.LoggerFactory}
 * @author Erik Torres
 * @since 0.1.0
 */
public final class ExtendedLoggerFactory {

	/**
	 * Uses the method {@link org.slf4j.LoggerFactory#getLogger(Class)} to create a logger named corresponding to the 
	 * class passed as parameter. In addition, this method will configure the logging system, installing the necessary
	 * bridges to unify logging across the entire application.
	 * @param clazz - the returned logger will be named after clazz
	 * @return A logger named corresponding to the class passed as parameter.
	 */
	public static Logger getLogger(final Class<?> clazz) {
		es.upv.grycap.coreutils.logging.LogManager.getLogManager().init();
		return org.slf4j.LoggerFactory.getLogger(requireNonNull(clazz, "A non-null class expected"));
	}

}