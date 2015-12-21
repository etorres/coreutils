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

package org.grycap.coreutils.common;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.File;

import javax.annotation.Nullable;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

/**
 * Configuration loader.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.1.0
 */
public class Configurer {

	/**
	 * Loads and merges application configuration with default properties.
	 * @param confname - optional configuration filename
	 * @param rootPath - only load configuration properties underneath this path that this code
	 *                   module owns and understands
	 * @return Configuration loaded from the provided filename or from default properties.
	 */
	public Config loadConfig(final @Nullable String confname, final String rootPath) {
		// load configuration properties
		Config config;
		final String confname2 = trimToNull(confname);
		if (confname2 != null) {
			final ConfigParseOptions options = ConfigParseOptions.defaults().setAllowMissing(false);
			final Config customConfig = ConfigFactory.parseFileAnySyntax(new File(confname2), options);
			final Config regularConfig = ConfigFactory.load();
			final Config combined = customConfig.withFallback(regularConfig);
			config = ConfigFactory.load(combined);
		} else {
			config = ConfigFactory.load();
		}
		// validate
		config.checkValid(ConfigFactory.defaultReference(), rootPath);
		return config;
	}

}