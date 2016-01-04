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

package es.upv.grycap.coreutils.common;

import com.google.common.collect.Range;

/**
 * Hard-coded configuration limits.
 * @author Erik Torres
 * @since 0.2.0
 */
public interface CoreutilsLimits {

	public static final int NUM_AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

	public static final Range<Long> TRY_LOCK_TIMEOUT_RANGE = Range.closed(1l, 2000l);
	public static final Range<Integer> MAX_POOL_SIZE_RANGE = Range.closed(Math.min(2, NUM_AVAILABLE_PROCESSORS), Math.max(128, NUM_AVAILABLE_PROCESSORS));
	public static final Range<Long> KEEP_ALIVE_TIME_RANGE = Range.closed(60000l, 3600000l);
	public static final Range<Long> WAIT_TERMINATION_TIMEOUT_RANGE = Range.closed(1000l, 60000l);

}