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

package es.upv.grycap.coreutils.common.events;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;

/**
 * Listener waiting for events that were posted but not delivered to anyone.
 * @author Erik Torres
 * @since 0.2.0
 */
public final class DeadEventListener {

	private final static Logger LOGGER = getLogger(DeadEventListener.class);

	@Subscribe
	public void listen(final DeadEvent deadEvent) {
		LOGGER.warn(String.format("Undelivered event with no subscribers was caught: [type='%s', source='%s']", 
				getEventType(deadEvent), getSource(deadEvent)));
	}

	private static String getEventType(final DeadEvent deadEvent) {
		return (deadEvent != null && deadEvent.getEvent() != null ? deadEvent.getEvent().getClass().getCanonicalName() 
				: "(not available)");
	}

	private static String getSource(final DeadEvent deadEvent) {
		return (deadEvent != null && deadEvent.getSource() != null ? deadEvent.getSource().getClass().getCanonicalName() 
				: "(not available)");
	}

}