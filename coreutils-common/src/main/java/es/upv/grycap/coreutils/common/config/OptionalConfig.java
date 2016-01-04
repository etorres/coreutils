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

package es.upv.grycap.coreutils.common.config;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.typesafe.config.Config;

/**
 * Extends configuration with optional settings.
 * @author Erik Torres
 * @since 0.2.0
 */
public class OptionalConfig {

	private final Config config;

	public OptionalConfig(final Config config) {
		this.config = requireNonNull(config, "A non-null configuration expected");
	}

	/**
	 * Description copied from the method {@link Config#getDuration(String, TimeUnit)}: Gets a value as a duration in a specified TimeUnit. If the value 
	 * is already a number, then it's taken as milliseconds and then converted to the requested TimeUnit; if it's a string, it's parsed understanding 
	 * units suffixes like "10m" or "5ns" as documented in <a href="https://github.com/typesafehub/config/blob/master/HOCON.md">the spec</a>.
	 * @param path - path expression
	 * @param unit - convert the return value to this time unit
	 * @return the duration value at the requested path, in the given TimeUnit
	 */
	public Optional<Long> getDuration(final String path, final TimeUnit unit) {		
		return config.hasPath(requireNonNull(path, "A non-null path expected")) 
				? ofNullable(config.getDuration(path, requireNonNull(unit, "A non-null unit expected"))) : empty();
	}

}