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

package es.upv.grycap.coreutils.common.concurrent;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;

/**
 * Handles possible uncaught {@link RuntimeException} thrown by threaded tasks. When a thread terminates abnormally class simply 
 * writes a message to the logging system, which will not hang the JVM.
 * @author Erik Torres
 * @since 0.2.0
 */
public class TaskUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private final static Logger LOGGER = getLogger(TaskUncaughtExceptionHandler.class);

	@Override
	public void uncaughtException(final Thread thread, final Throwable cause) {
		LOGGER.error(String.format("Thread %s terminates abnormally", thread.getName()), cause);
	}

}