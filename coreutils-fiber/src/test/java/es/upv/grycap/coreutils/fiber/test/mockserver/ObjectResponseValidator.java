/*
 * Core Utils - Fiber-enabled clients.
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

package es.upv.grycap.coreutils.fiber.test.mockserver;

import static java.nio.file.Files.readAllBytes;
import static java.util.Optional.ofNullable;
import static javax.xml.xpath.XPathConstants.STRING;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Validates object responses.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.1.0
 */
public final class ObjectResponseValidator {

	public static boolean isValidXml(final String payload, final String expectedId) {
		boolean isValid = false;
		try {
			final String objectId = readObjectIdFromXml(payload);
			isValid = expectedId.equals(trimToNull(objectId));
		} catch (Exception e) {
			isValid = false;
			e.printStackTrace();
		}
		return isValid;
	}

	public static void validateXml(final File file, final String expectedId) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {		
		final String objectId = readObjectIdFromXml(new String(readAllBytes(file.toPath())));				
		assertThat("Object Id coincides with expected", objectId, allOf(notNullValue(), not(equalTo("")), equalTo(expectedId)));
	}

	@Nullable
	private static String readObjectIdFromXml(final String payload) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
		final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(payload.getBytes()));
		final XPath xPath = XPathFactory.newInstance().newXPath();
		final XPathExpression xPathExpression = xPath.compile("//*/coreutils/objectId/text()");
		return trimToNull((String) xPathExpression.evaluate(document, STRING));
	}

	public static boolean isValidJson(final String payload, final String expectedId) {
		boolean isValid = false;
		try {
			final String objectId = readObjectIdFromJson(payload);
			isValid = expectedId.equals(trimToNull(objectId));
		} catch (Exception e) {
			isValid = false;
			e.printStackTrace();
		}
		return isValid;
	}

	public static void validateJson(final File file, final String expectedId) throws IOException, JSONException {
		final String objectId = readObjectIdFromJson(new String(readAllBytes(file.toPath())));
		assertThat("Object Id coincides with expected", objectId, allOf(notNullValue(), not(equalTo("")), equalTo(expectedId)));		
	}

	@Nullable
	private static String readObjectIdFromJson(final String payload) throws JSONException {
		String objectId = null;
		final JSONObject rootElem = new JSONObject(payload);
		final String[] names = JSONObject.getNames(rootElem);
		if (names != null) {
			boolean found = false;
			for (int i = 0; i < names.length && !found; i++) {			
				final JSONObject childElem = rootElem.getJSONObject(names[i]);
				if (Arrays.asList(ofNullable(JSONObject.getNames(childElem)).orElse(new String[]{})).contains("coreutils")) {
					objectId = trimToNull(childElem.getJSONObject("coreutils").getString("objectId"));
					found = true;
				}			
			}
		}
		return objectId;
	}

}