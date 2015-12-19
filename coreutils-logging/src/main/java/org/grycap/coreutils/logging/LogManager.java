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

package org.grycap.coreutils.logging;

import static org.slf4j.LoggerFactory.getLogger;
import static org.slf4j.bridge.SLF4JBridgeHandler.install;
import static org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger;
import static org.slf4j.bridge.SLF4JBridgeHandler.uninstall;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

/**
 * Manages loggers, installing the necessary bridges to unify logging across an entire library, service or application.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.1.0
 */
public class LogManager {

	private final static Logger LOGGER = getLogger(LogManager.class);

	private final static AtomicBoolean PRISTINE = new AtomicBoolean(true);

	private LogManager() { }
	
	public static LogManager getLogManager() {
		return new LogManager();
	}
	
	/**
	 * The logging system is automatically configured when the method {@link LogManager#getLogger(Class)} is called. 
	 * This method is conveniently provided to initialize the logging system before creating any logger with this class.
	 * Also, you can call this method to initialize the logging system without modifying your code (creating loggers 
	 * with SLF4J or other logging framework).
	 */
	public void init() {
		if (PRISTINE.getAndSet(false)) {
			// removes/unregisters/detaches all handlers currently attached to the root logger
			removeHandlersForRootLogger();
			// add SLF4JBridgeHandler to j.u.l's root logger (should be done once during the initialization phase of the application)
			install();
			LOGGER.info("Log manager was loaded.");
		}
	}

	/**
	 * Uninstall the logging bridges returning to the original configuration provided by the individual logging frameworks.
	 */
	public void reset() {
		LOGGER.info("About to reset log manager: no more coherent log messages guaranteed beyond this point.");
		// uninstall the SLF4JBridgeHandler instance attached to the jul's root logger
		uninstall();
		// removes/unregisters/detaches all handlers currently attached to the root logger
		removeHandlersForRootLogger();
	}	

}