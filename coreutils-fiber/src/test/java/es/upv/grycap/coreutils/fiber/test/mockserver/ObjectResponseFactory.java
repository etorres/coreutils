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

/**
 * Creates objects using standard data formats. Despite the format, all objects include a recognizable identifier in the
 * form: coreutils -> objectId -> <tt>Integer</tt>. All objects are valid, well-formed documents that can be parsed
 * with standard tools.
 * @author Erik Torres <etserrano@gmail.com>
 * @since 0.1.0
 * @see <a href="http://json.org/example.html">JSON Example</a>
 */
public final class ObjectResponseFactory {

	/**
	 * Creates a JSON formatted string with object Id 1.
	 * @return JSON formatted string with object Id 1.
	 */
	public static String jsonObject1() {
		return new StringBuilder()
				.append("{")				
				.append("\"glossary\": {")
				.append("\"coreutils\": {")
				.append("\"objectId\": \"1\"")
				.append("},")
				.append("\"title\": \"example glossary\",")
				.append("\"GlossDiv\": {")
				.append("\"title\": \"S\",")
				.append("\"GlossList\": {")
				.append("\"GlossEntry\": {")
				.append("\"ID\": \"SGML\",")
				.append("\"SortAs\": \"SGML\",")
				.append("\"GlossTerm\": \"Standard Generalized Markup Language\",")
				.append("\"Acronym\": \"SGML\",")
				.append("\"Abbrev\": \"ISO 8879:1986\",")
				.append("\"GlossDef\": {")
				.append("\"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",")
				.append("\"GlossSeeAlso\": [\"GML\", \"XML\"]")
				.append("},")
				.append("\"GlossSee\": \"markup\"")
				.append("}")
				.append("}")
				.append("}")
				.append("}")
				.append("}")
				.toString();
	}

	/**
	 * Creates a JSON formatted string with object Id 2.
	 * @return JSON formatted string with object Id 2.
	 */
	public static String jsonObject2() {
		return new StringBuilder()
				.append("{\"menu\": {")
				.append("\"coreutils\": {")
				.append("\"objectId\": \"2\"")
				.append("},")
				.append("\"header\": \"SVG Viewer\",")
				.append("\"items\": [")
				.append("{\"id\": \"Open\"},")
				.append("{\"id\": \"OpenNew\", \"label\": \"Open New\"},")
				.append("null,")
				.append("{\"id\": \"ZoomIn\", \"label\": \"Zoom In\"},")
				.append("{\"id\": \"ZoomOut\", \"label\": \"Zoom Out\"},")
				.append("{\"id\": \"OriginalView\", \"label\": \"Original View\"},")
				.append("null,")
				.append("{\"id\": \"Quality\"},")
				.append("{\"id\": \"Pause\"},")
				.append("{\"id\": \"Mute\"},")
				.append("null,")
				.append("{\"id\": \"Find\", \"label\": \"Find...\"},")
				.append("{\"id\": \"FindAgain\", \"label\": \"Find Again\"},")
				.append("{\"id\": \"Copy\"},")
				.append("{\"id\": \"CopyAgain\", \"label\": \"Copy Again\"},")
				.append("{\"id\": \"CopySVG\", \"label\": \"Copy SVG\"},")
				.append("{\"id\": \"ViewSVG\", \"label\": \"View SVG\"},")
				.append("{\"id\": \"ViewSource\", \"label\": \"View Source\"},")
				.append("{\"id\": \"SaveAs\", \"label\": \"Save As\"},")
				.append("null,")
				.append("{\"id\": \"Help\"},")
				.append("{\"id\": \"About\", \"label\": \"About Adobe CVG Viewer...\"}")
				.append("]")
				.append("}}")
				.toString();
	}

	/**
	 * Creates a XML formatted string with object Id 1.
	 * @return XML formatted string with object Id 1.
	 */
	public static String xmlObject1() {
		return new StringBuilder()
				.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
				.append("<glossary><title>example glossary</title>")
				.append("<coreutils><objectId>1</objectId></coreutils>")
				.append("<GlossDiv><title>S</title>")
				.append("<GlossList>")
				.append("<GlossEntry ID=\"SGML\" SortAs=\"SGML\">")
				.append("<GlossTerm>Standard Generalized Markup Language</GlossTerm>")
				.append("<Acronym>SGML</Acronym>")
				.append("<Abbrev>ISO 8879:1986</Abbrev>")
				.append("<GlossDef>")
				.append("<para>A meta-markup language, used to create markup")
				.append("languages such as DocBook.</para>")
				.append("<GlossSeeAlso OtherTerm=\"GML\" />")
				.append("<GlossSeeAlso OtherTerm=\"XML\" />")
				.append("</GlossDef>")
				.append("<GlossSee OtherTerm=\"markup\" />")
				.append("</GlossEntry>")
				.append("</GlossList>")
				.append("</GlossDiv>")
				.append("</glossary>")
				.toString();
	}

	/**
	 * Creates a XML formatted string with object Id 2.
	 * @return XML formatted string with object Id 2.
	 */
	public static String xmlObject2() {
		return new StringBuilder()
				.append("<menu>")
				.append("<coreutils><objectId>2</objectId></coreutils>")
				.append("<header>Adobe SVG Viewer</header>")
				.append("<item action=\"Open\" id=\"Open\">Open</item>")
				.append("<item action=\"OpenNew\" id=\"OpenNew\">Open New</item>")
				.append("<separator/>")
				.append("<item action=\"ZoomIn\" id=\"ZoomIn\">Zoom In</item>")
				.append("<item action=\"ZoomOut\" id=\"ZoomOut\">Zoom Out</item>")
				.append("<item action=\"OriginalView\" id=\"OriginalView\">Original View</item>")
				.append("<separator/>")
				.append("<item action=\"Quality\" id=\"Quality\">Quality</item>")
				.append("<item action=\"Pause\" id=\"Pause\">Pause</item>")
				.append("<item action=\"Mute\" id=\"Mute\">Mute</item>")
				.append("<separator/>")
				.append("<item action=\"Find\" id=\"Find\">Find...</item>")
				.append("<item action=\"FindAgain\" id=\"FindAgain\">Find Again</item>")
				.append("<item action=\"Copy\" id=\"Copy\">Copy</item>")
				.append("<item action=\"CopyAgain\" id=\"CopyAgain\">Copy Again</item>")
				.append("<item action=\"CopySVG\" id=\"CopySVG\">Copy SVG</item>")
				.append("<item action=\"ViewSVG\" id=\"ViewSVG\">View SVG</item>")
				.append("<item action=\"ViewSource\" id=\"ViewSource\">View Source</item>")
				.append("<item action=\"SaveAs\" id=\"SaveAs\">Save As</item>")
				.append("<separator/>")
				.append("<item action=\"Help\" id=\"Help\">Help</item>")
				.append("<item action=\"About\" id=\"About\">About Adobe CVG Viewer...</item>")
				.append("</menu>")
				.toString();
	}

}