package org.json;

/*
Copyright (c) 2015 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * This provides static methods to convert an XML text into a JSONObject, and to
 * covert a JSONObject into an XML text.
 *
 * @author JSON.org
 * @version 2016-08-10
 */
@SuppressWarnings("boxing")
public class XML {

    /** The Character '&amp;'. */
    public static final Character AMP = '&';

    /** The Character '''. */
    public static final Character APOS = '\'';

    /** The Character '!'. */
    public static final Character BANG = '!';

    /** The Character '='. */
    public static final Character EQ = '=';

    /** The Character <pre>{@code '>'. }</pre>*/
    public static final Character GT = '>';

    /** The Character '&lt;'. */
    public static final Character LT = '<';

    /** The Character '?'. */
    public static final Character QUEST = '?';

    /** The Character '"'. */
    public static final Character QUOT = '"';

    /** The Character '/'. */
    public static final Character SLASH = '/';

    /**
     * Null attribute name
     */
    public static final String NULL_ATTR = "xsi:nil";

    public static final String TYPE_ATTR = "xsi:type";

    /**
     * Creates an iterator for navigating Code Points in a string instead of
     * characters. Once Java7 support is dropped, this can be replaced with
     * <code>
     * string.codePoints()
     * </code>
     * which is available in Java8 and above.
     *
     * @see <a href=
     *      "http://stackoverflow.com/a/21791059/6030888">http://stackoverflow.com/a/21791059/6030888</a>
     */
    private static Iterable<Integer> codePointIterator(final String string) {
        return new Iterable<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {
                    private int nextIndex = 0;
                    private int length = string.length();

                    @Override
                    public boolean hasNext() {
                        return this.nextIndex < this.length;
                    }

                    @Override
                    public Integer next() {
                        int result = string.codePointAt(this.nextIndex);
                        this.nextIndex += Character.charCount(result);
                        return result;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Replace special characters with XML escapes:
     *
     * <pre>{@code 
     * &amp; (ampersand) is replaced by &amp;amp;
     * &lt; (less than) is replaced by &amp;lt;
     * &gt; (greater than) is replaced by &amp;gt;
     * &quot; (double quote) is replaced by &amp;quot;
     * &apos; (single quote / apostrophe) is replaced by &amp;apos;
     * }</pre>
     *
     * @param string
     *            The string to be escaped.
     * @return The escaped string.
     */
    public static String escape(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        for (final int cp : codePointIterator(string)) {
            switch (cp) {
            case '&':
                sb.append("&amp;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            case '\'':
                sb.append("&apos;");
                break;
            default:
                if (mustEscape(cp)) {
                    sb.append("&#x");
                    sb.append(Integer.toHexString(cp));
                    sb.append(';');
                } else {
                    sb.appendCodePoint(cp);
                }
            }
        }
        return sb.toString();
    }

    /**
     * @param cp code point to test
     * @return true if the code point is not valid for an XML
     */
    private static boolean mustEscape(int cp) {
        /* Valid range from https://www.w3.org/TR/REC-xml/#charsets
         *
         * #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
         *
         * any Unicode character, excluding the surrogate blocks, FFFE, and FFFF.
         */
        // isISOControl is true when (cp >= 0 && cp <= 0x1F) || (cp >= 0x7F && cp <= 0x9F)
        // all ISO control characters are out of range except tabs and new lines
        return (Character.isISOControl(cp)
                && cp != 0x9
                && cp != 0xA
                && cp != 0xD
            ) || !(
                // valid the range of acceptable characters that aren't control
                (cp >= 0x20 && cp <= 0xD7FF)
                || (cp >= 0xE000 && cp <= 0xFFFD)
                || (cp >= 0x10000 && cp <= 0x10FFFF)
            )
        ;
    }

    /**
     * Removes XML escapes from the string.
     *
     * @param string
     *            string to remove escapes from
     * @return string with converted entities
     */
    public static String unescape(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        for (int i = 0, length = string.length(); i < length; i++) {
            char c = string.charAt(i);
            if (c == '&') {
                final int semic = string.indexOf(';', i);
                if (semic > i) {
                    final String entity = string.substring(i + 1, semic);
                    sb.append(XMLTokener.unescapeEntity(entity));
                    // skip past the entity we just parsed.
                    i += entity.length() + 1;
                } else {
                    // this shouldn't happen in most cases since the parser
                    // errors on unclosed entries.
                    sb.append(c);
                }
            } else {
                // not part of an entity
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Throw an exception if the string contains whitespace. Whitespace is not
     * allowed in tagNames and attributes.
     *
     * @param string
     *            A string.
     * @throws JSONException Thrown if the string contains whitespace or is empty.
     */
    public static void noSpace(String string) throws JSONException {
        int i, length = string.length();
        if (length == 0) {
            throw new JSONException("Empty string.");
        }
        for (i = 0; i < length; i += 1) {
            if (Character.isWhitespace(string.charAt(i))) {
                throw new JSONException("'" + string
                        + "' contains a space character.");
            }
        }
    }

    /**
     * Scan the content following the named tag, attaching it to the context.
     *
     * @param x
     *            The XMLTokener containing the source string.
     * @param context
     *            The JSONObject that will include the new material.
     * @param name
     *            The tag name.
     * @return true if the close tag is processed.
     * @throws JSONException
     */
    private static boolean parse(XMLTokener x, JSONObject context, String name, XMLParserConfiguration config)
            throws JSONException {
        char c;
        int i;
        JSONObject jsonObject = null;
        String string;
        String tagName;
        Object token;
        XMLXsiTypeConverter<?> xmlXsiTypeConverter;

        // Test for and skip past these forms:
        // <!-- ... -->
        // <! ... >
        // <![ ... ]]>
        // <? ... ?>
        // Report errors for these forms:
        // <>
        // <=
        // <<

        token = x.nextToken();

        // <!

        if (token == BANG) {
            c = x.next();
            if (c == '-') {
                if (x.next() == '-') {
                    x.skipPast("-->");
                    return false;
                }
                x.back();
            } else if (c == '[') {
                token = x.nextToken();
                if ("CDATA".equals(token)) {
                    if (x.next() == '[') {
                        string = x.nextCDATA();
                        if (string.length() > 0) {
                            context.accumulate(config.getcDataTagName(), string);
                        }
                        return false;
                    }
                }
                throw x.syntaxError("Expected 'CDATA['");
            }
            i = 1;
            do {
                token = x.nextMeta();
                if (token == null) {
                    throw x.syntaxError("Missing '>' after '<!'.");
                } else if (token == LT) {
                    i += 1;
                } else if (token == GT) {
                    i -= 1;
                }
            } while (i > 0);
            return false;
        } else if (token == QUEST) {

            // <?
            x.skipPast("?>");
            return false;
        } else if (token == SLASH) {

            // Close tag </

            token = x.nextToken();
            if (name == null) {
                throw x.syntaxError("Mismatched close tag " + token);
            }
            if (!token.equals(name)) {
                throw x.syntaxError("Mismatched " + name + " and " + token);
            }
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped close tag");
            }
            return true;

        } else if (token instanceof Character) {
            throw x.syntaxError("Misshaped tag");

            // Open tag <

        } else {
            tagName = (String) token;
            token = null;
            jsonObject = new JSONObject();
            boolean nilAttributeFound = false;
            xmlXsiTypeConverter = null;
            for (;;) {
                if (token == null) {
                    token = x.nextToken();
                }
                // attribute = value
                if (token instanceof String) {
                    string = (String) token;
                    token = x.nextToken();
                    if (token == EQ) {
                        token = x.nextToken();
                        if (!(token instanceof String)) {
                            throw x.syntaxError("Missing value");
                        }

                        if (config.isConvertNilAttributeToNull()
                                && NULL_ATTR.equals(string)
                                && Boolean.parseBoolean((String) token)) {
                            nilAttributeFound = true;
                        } else if(config.getXsiTypeMap() != null && !config.getXsiTypeMap().isEmpty()
                                && TYPE_ATTR.equals(string)) {
                            xmlXsiTypeConverter = config.getXsiTypeMap().get(token);
                        } else if (!nilAttributeFound) {
                            jsonObject.accumulate(string,
                                    config.isKeepStrings()
                                            ? ((String) token)
                                            : stringToValue((String) token));
                        }
                        token = null;
                    } else {
                        jsonObject.accumulate(string, "");
                    }


                } else if (token == SLASH) {
                    // Empty tag <.../>
                    if (x.nextToken() != GT) {
                        throw x.syntaxError("Misshaped tag");
                    }
                    if (nilAttributeFound) {
                        context.accumulate(tagName, JSONObject.NULL);
                    } else if (jsonObject.length() > 0) {
                        context.accumulate(tagName, jsonObject);
                    } else {
                        context.accumulate(tagName, "");
                    }
                    return false;

                } else if (token == GT) {
                    // Content, between <...> and </...>
                    for (;;) {
                        token = x.nextContent();
                        if (token == null) {
                            if (tagName != null) {
                                throw x.syntaxError("Unclosed tag " + tagName);
                            }
                            return false;
                        } else if (token instanceof String) {
                            string = (String) token;
                            if (string.length() > 0) {
                                if(xmlXsiTypeConverter != null) {
                                    jsonObject.accumulate(config.getcDataTagName(),
                                            stringToValue(string, xmlXsiTypeConverter));
                                } else {
                                    jsonObject.accumulate(config.getcDataTagName(),
                                            config.isKeepStrings() ? string : stringToValue(string));
                                }
                            }

                        } else if (token == LT) {
                            // Nested element
                            if (parse(x, jsonObject, tagName, config)) {
                                if (jsonObject.length() == 0) {
                                    context.accumulate(tagName, "");
                                } else if (jsonObject.length() == 1
                                        && jsonObject.opt(config.getcDataTagName()) != null) {
                                    context.accumulate(tagName, jsonObject.opt(config.getcDataTagName()));
                                } else {
                                    context.accumulate(tagName, jsonObject);
                                }
                                return false;
                            }
                        }
                    }
                } else {
                    throw x.syntaxError("Misshaped tag");
                }
            }
        }
    }

    /**
     * This method tries to convert the given string value to the target object
     * @param string String to convert
     * @param typeConverter value converter to convert string to integer, boolean e.t.c
     * @return JSON value of this string or the string
     */
    public static Object stringToValue(String string, XMLXsiTypeConverter<?> typeConverter) {
        if(typeConverter != null) {
            return typeConverter.convert(string);
        }
        return stringToValue(string);
    }

    /**
     * This method is the same as {@link JSONObject#stringToValue(String)}.
     *
     * @param string String to convert
     * @return JSON value of this string or the string
     */
    // To maintain compatibility with the Android API, this method is a direct copy of
    // the one in JSONObject. Changes made here should be reflected there.
    // This method should not make calls out of the XML object.
    public static Object stringToValue(String string) {
        if ("".equals(string)) {
            return string;
        }

        // check JSON key words true/false/null
        if ("true".equalsIgnoreCase(string)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(string)) {
            return Boolean.FALSE;
        }
        if ("null".equalsIgnoreCase(string)) {
            return JSONObject.NULL;
        }

        /*
         * If it might be a number, try converting it. If a number cannot be
         * produced, then the value will just be a string.
         */

        char initial = string.charAt(0);
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            try {
                return stringToNumber(string);
            } catch (Exception ignore) {
            }
        }
        return string;
    }
    
    /**
     * direct copy of {@link JSONObject#stringToNumber(String)} to maintain Android support.
     */
    private static Number stringToNumber(final String val) throws NumberFormatException {
        char initial = val.charAt(0);
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            // decimal representation
            if (isDecimalNotation(val)) {
                // Use a BigDecimal all the time so we keep the original
                // representation. BigDecimal doesn't support -0.0, ensure we
                // keep that by forcing a decimal.
                try {
                    BigDecimal bd = new BigDecimal(val);
                    if(initial == '-' && BigDecimal.ZERO.compareTo(bd)==0) {
                        return Double.valueOf(-0.0);
                    }
                    return bd;
                } catch (NumberFormatException retryAsDouble) {
                    // this is to support "Hex Floats" like this: 0x1.0P-1074
                    try {
                        Double d = Double.valueOf(val);
                        if(d.isNaN() || d.isInfinite()) {
                            throw new NumberFormatException("val ["+val+"] is not a valid number.");
                        }
                        return d;
                    } catch (NumberFormatException ignore) {
                        throw new NumberFormatException("val ["+val+"] is not a valid number.");
                    }
                }
            }
            // block items like 00 01 etc. Java number parsers treat these as Octal.
            if(initial == '0' && val.length() > 1) {
                char at1 = val.charAt(1);
                if(at1 >= '0' && at1 <= '9') {
                    throw new NumberFormatException("val ["+val+"] is not a valid number.");
                }
            } else if (initial == '-' && val.length() > 2) {
                char at1 = val.charAt(1);
                char at2 = val.charAt(2);
                if(at1 == '0' && at2 >= '0' && at2 <= '9') {
                    throw new NumberFormatException("val ["+val+"] is not a valid number.");
                }
            }
            // integer representation.
            // This will narrow any values to the smallest reasonable Object representation
            // (Integer, Long, or BigInteger)
            
            // BigInteger down conversion: We use a similar bitLenth compare as
            // BigInteger#intValueExact uses. Increases GC, but objects hold
            // only what they need. i.e. Less runtime overhead if the value is
            // long lived.
            BigInteger bi = new BigInteger(val);
            if(bi.bitLength() <= 31){
                return Integer.valueOf(bi.intValue());
            }
            if(bi.bitLength() <= 63){
                return Long.valueOf(bi.longValue());
            }
            return bi;
        }
        throw new NumberFormatException("val ["+val+"] is not a valid number.");
    }
    
    /**
     * direct copy of {@link JSONObject#isDecimalNotation(String)} to maintain Android support.
     */
    private static boolean isDecimalNotation(final String val) {
        return val.indexOf('.') > -1 || val.indexOf('e') > -1
                || val.indexOf('E') > -1 || "-0".equals(val);
    }


    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * JSONObject. Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code 
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * @param string
     *            The source string.
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject(String string) throws JSONException {
        return toJSONObject(string, XMLParserConfiguration.ORIGINAL);
    }

    /**
     * Convert a well-formed (but not necessarily valid) XML into a
     * JSONObject. Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code 
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * @param reader The XML source reader.
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject(Reader reader) throws JSONException {
        return toJSONObject(reader, XMLParserConfiguration.ORIGINAL);
    }

    /**
     * Convert a well-formed (but not necessarily valid) XML into a
     * JSONObject. Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * All values are converted as strings, for 1, 01, 29.0 will not be coerced to
     * numbers but will instead be the exact value as seen in the XML document.
     *
     * @param reader The XML source reader.
     * @param keepStrings If true, then values will not be coerced into boolean
     *  or numeric values and will instead be left as strings
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject(Reader reader, boolean keepStrings) throws JSONException {
        if(keepStrings) {
            return toJSONObject(reader, XMLParserConfiguration.KEEP_STRINGS);
        }
        return toJSONObject(reader, XMLParserConfiguration.ORIGINAL);
    }
/**
     * Find a JSONObject pointed to by a JSONPointer inside a well-formed 
     * (but not necessarily valid) XML, replace it with the replacement
     * JSONObject and return the whole XML as a JSONObject
     * Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * All values are converted as strings, for 1, 01, 29.0 will not be coerced to
     * numbers but will instead be the exact value as seen in the XML document.
     *
     * @param reader The XML source reader.
     * @param path A JSONPointer path, representing the JSONObject to be extracted
     * from the xml file
     * @return A JSONObject containing the structured data from the XML file.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject (Reader reader, JSONPointer path, JSONObject replacement){
        XMLParserConfiguration config = XMLParserConfiguration.ORIGINAL;
        List<String> pointerList = new ArrayList<String>();
        String [] pathStr;
        if (path.toString().equals("/")){
            pathStr = new String[0];
        }else{
            pathStr = path.toString().split("/");
        }
        // path should start with "/"
        if (pathStr.length >0 && !pathStr[0].equals("")){
            throw new JSONPointerException("Incorrectly formed JSON Pointer");
        }

        if (pathStr.length==0){
            if (!path.toString().equals("/")){
                throw new JSONPointerException("Incorrectly formed JSON Pointer");
            }
        }
        // start from second string because first should be empty because path should have started with "/"
        for (int i=1; i<pathStr.length; i++){
            pointerList.add(pathStr[i]); // add ea
        }

        JSONObject jo = new JSONObject();
        XMLTokener x = new XMLTokener(reader);
        boolean skipSaving = false; // don't skip saving
        boolean [] foundObj = {false};
        int recursionLevel = -1;
        // keep looking only while jo has nothing in it.
        // once jo has something, its time to return
        while (x.more() && jo.length()==0) {
            x.skipPast("<");
            if(x.more()) {
                 parseReplaceSubObject(x, jo, null, config, pointerList, foundObj, recursionLevel, replacement);
            }
        }
        return jo;
    }

    /**
     * parseReplaceSubObject().
     */
    private static boolean parseReplaceSubObject(XMLTokener x, JSONObject context, String name, XMLParserConfiguration config, List<String> pointerList, boolean [] foundObj, int recursionLevel, JSONObject replacement)
    throws JSONException {
char c;
int i;
JSONObject jsonObject = null;
String string;
String tagName;
Object token;
XMLXsiTypeConverter<?> xmlXsiTypeConverter;
String topPointer; // pointer token at the top of the pointerList
List<String> newPointerList; // new pointer list to pass on to the next level of recursion

// Test for and skip past these forms:
// <!-- ... -->
// <! ... >
// <![ ... ]]>
// <? ... ?>
// Report errors for these forms:
// <>
// <=
// <<

token = x.nextToken();

// <!

if (token == BANG) {
    c = x.next();
    if (c == '-') {
        if (x.next() == '-') {
            x.skipPast("-->");
            return false;
        }
        x.back();
    } else if (c == '[') {
        token = x.nextToken();
        if ("CDATA".equals(token)) {
            if (x.next() == '[') {
                    string = x.nextCDATA();
                    if (string.length() > 0) {
                        context.accumulate(config.getcDataTagName(), string);
                    }
                return false;
            }
        }
        throw x.syntaxError("Expected 'CDATA['");
    }
    i = 1;
    do {
        token = x.nextMeta();
        if (token == null) {
            throw x.syntaxError("Missing '>' after '<!'.");
        } else if (token == LT) {
            i += 1;
        } else if (token == GT) {
            i -= 1;
        }
    } while (i > 0);
    return false;
} else if (token == QUEST) {

    // <?
    x.skipPast("?>");
    return false;
} else if (token == SLASH) {

    // Close tag </

    token = x.nextToken();
    if (name == null) {
        throw x.syntaxError("Mismatched close tag " + token);
    }
    if (!token.equals(name)) {
        throw x.syntaxError("Mismatched " + name + " and " + token);
    }
    if (x.nextToken() != GT) {
        throw x.syntaxError("Misshaped close tag");
    }
    return true;

} else if (token instanceof Character) {
    throw x.syntaxError("Misshaped tag");

    // Open tag <

} else {
    tagName = (String) token;
    token = null;
    jsonObject = new JSONObject();
    boolean nilAttributeFound = false;
    xmlXsiTypeConverter = null;
    
    //new logic to check if pointerList has any matches
    newPointerList = new ArrayList<String>(); // initialize empty new pointer list
        // copy the modified pointerList to newPointerList
    newPointerList.addAll(pointerList); 

    // check if pointerList is already null then we should be getting recursionLevel of 0
    if (pointerList.isEmpty()){
        recursionLevel = 0;
    }

    if (!newPointerList.isEmpty() && tagName.equals(newPointerList.get(0)))
    {
        // we are on the right tag
        topPointer = newPointerList.get(0);
        // check case where a next pointer exists that is integer. In this case, don't remove this pointer instead
        // decrement the next integer pointer and keep looking. Otherwise remove this top pointer
        if (newPointerList.size()>1 && (newPointerList.get(1).matches("[0-9]+"))){
            // second pointer is integer. check if it is zero, then we have found the topPointer and the zeroth value
            // if more than 0, then we keep looking and decrement
            if (Integer.parseInt(newPointerList.get(1))>0){
                // decrement my own pointer. newPointer doesen't have a meaning as any downstream recursion is 
                // useless for us so we don't decrement it.
                pointerList.set(1,String.valueOf(Integer.parseInt(pointerList.get(1))-1));
            }// check if integer value is 0 and not negative i.e. the zeroth value hasn't already passed
            else if (Integer.parseInt(newPointerList.get(1))==0){
                // remove pointers from the list only if we have reached "/tag/0"
                    newPointerList.remove(1); //safe to remove the integer next pointer
                    newPointerList.remove(0); // safe to remove the top pointer
                // we have to make sure we only do this once and not again if the tag's value is an array
                // so we decrement the next integer to -1 from our original pointerList so the next time
                // we find the same tagName at this level, we don't recognize it as /tag/0
                    pointerList.set(1,String.valueOf(Integer.parseInt(pointerList.get(1))-1));
            }
            else{
                // Integer.parseInt(newPointerList.get(1)) is negative. Not interested in replacing this object
            }
        }else{
            //next pointer is not an integer. can safetly remove the top pointer
            newPointerList.remove(0); 
        }
    }
    else if (!newPointerList.isEmpty() && !tagName.equals(newPointerList.get(0))){
        // the next pointer does not match the current tag. I should add some impossible key 
        // to the top of list so that downstream recursions don't end up matching the JSONpointer
        // this will ensure the recursion returns successfully and I keep searching for the complete
        // JSONPointer. This prevents "/book/0" from replacing at "/catalog/book/0"
       
        newPointerList.add(0, ""); // empty key shouldn't match any tag in xml
    }

    // check if pointerList has become empty due to the above code or we inherited it empty
    if (newPointerList.isEmpty()){
        // when the pointerList becomes empty the first time, recursionLevel=0. We will stop saving when we return to 
        // recursionLevel=0
        recursionLevel +=1; 
    }

    for (;;) {
        if (token == null) {
            token = x.nextToken();
        }
        // attribute = value
        if (token instanceof String) {
            string = (String) token;
            token = x.nextToken();
            if (token == EQ) {
                token = x.nextToken();
                if (!(token instanceof String)) {
                    throw x.syntaxError("Missing value");
                }

                if (config.isConvertNilAttributeToNull()
                        && NULL_ATTR.equals(string)
                        && Boolean.parseBoolean((String) token)) {
                    nilAttributeFound = true;
                } else if(config.getXsiTypeMap() != null && !config.getXsiTypeMap().isEmpty()
                        && TYPE_ATTR.equals(string)) {
                    xmlXsiTypeConverter = config.getXsiTypeMap().get(token);
                } else if (!nilAttributeFound) {
                        jsonObject.accumulate(string,
                        config.isKeepStrings()
                                ? ((String) token)
                                : stringToValue((String) token));
                }
                token = null;
            } else {
                    jsonObject.accumulate(string, "");
            }


        } else if (token == SLASH) {
            // Empty tag <.../>
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped tag");
            }
                if (nilAttributeFound) {
                    context.accumulate(tagName, JSONObject.NULL);
                } else if (jsonObject.length() > 0) {
                    context.accumulate(tagName, jsonObject);
                } else {
                    context.accumulate(tagName, "");
                }    
            return false;

        } else if (token == GT) {
            // Content, between <...> and </...>
            for (;;) {
                token = x.nextContent();
                if (token == null) {
                    if (tagName != null) {
                        throw x.syntaxError("Unclosed tag " + tagName);
                    }
                    return false;
                } else if (token instanceof String) {
                    string = (String) token;
                    // if pointerList is empty, then only add to json otherwise skip adding, we're not 
                    // at the desired subObject yet
                        if (string.length() > 0) {
                            if(xmlXsiTypeConverter != null) {
                                jsonObject.accumulate(config.getcDataTagName(),
                                        stringToValue(string, xmlXsiTypeConverter));
                            } else {
                                jsonObject.accumulate(config.getcDataTagName(),
                                        config.isKeepStrings() ? string : stringToValue(string));
                            }
                        }
                } else if (token == LT) {
                    // Nested element

                    // if we are at recursion ==0 then try to skip until this tag closes and actually 
                    boolean returnedFlag = false;
                    if (recursionLevel ==1){
                        // we were given a call with "/"
                        // just replace context with replacement
                        Iterator<String> it = replacement.keys();
                            context.clear(); // clear anything in context
                            while (it.hasNext()){
                                String key = it.next();
                                context.put(key, replacement.get(key));
                            }
                            x.skipPast("/"+tagName+">"); // skip until recursion=0 tag has ended
                            return false;
                    }
                    else if (recursionLevel ==0){
                        if (!foundObj[0]){
                            jsonObject = replacement;
                            x.skipPast("/"+tagName+">"); // skip until recursion=0 tag has ended
                            returnedFlag = true; // okay to save    
                            //also turn on found flag
                            foundObj[0] = true;
                        }else{
                            //object has been previously found. should not come back to recursion =0
                            // otherwise an array will be built when we want single object
                            x.skipPast("/"+ tagName+ ">");
                            returnedFlag = false; // we don't even want empty values accumulated
                            return false;
                        }
                    }else{
                        returnedFlag = parseReplaceSubObject(x, jsonObject, tagName, config, newPointerList, foundObj, recursionLevel, replacement);
                    }
                    // call with updated newPointerList. This way on returning we maintain the original pointerList
                    if (returnedFlag) {
                        // Either the pointerList  or while we are still building the subObject,
                        // keep returning up the recursion
                        if (true){
                             
                            if (jsonObject.length() == 0) {
                                context.accumulate(tagName, "");
                            } else if (jsonObject.length() == 1
                                    && jsonObject.opt(config.getcDataTagName()) != null) {
                                context.accumulate(tagName, jsonObject.opt(config.getcDataTagName()));
                            } else {
                                context.accumulate(tagName, jsonObject);
                            } 
                            
                        }else{
                            // let's try if this works. I want upstream context to just pass through the underlying jsonObject
                            // because  I don't want any more tags be added.
                            // I'll try to copy jsonObject into context
                            Iterator<String> it = jsonObject.keys();
                            context.clear(); // clear anything in context
                            while (it.hasNext()){
                                String key = it.next();
                                context.put(key, jsonObject.get(key));
                            }
                            // if you have data in context, and you're not in recursion >0 then you can go forward
                            if (context.length() != 0){
                                //try this. skip to where end of this tag is. it's like fast forward
                                x.skipPast("/"+tagName);
                                return true; // after concatenating at rec level 0, it's time to return true
                            }
                        }
                        return false;
                    }
                }
            }
        } else {
            throw x.syntaxError("Misshaped tag");
        }
    }
}
}


    /**
     * Find a JSONObject pointed to by a JSONPointer inside a well-formed 
     * (but not necessarily valid) XML and convert it into JSONObject.
     * Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * All values are converted as strings, for 1, 01, 29.0 will not be coerced to
     * numbers but will instead be the exact value as seen in the XML document.
     *
     * @param reader The XML source reader.
     * @param path A JSONPointer path, representing the JSONObject to be extracted
     * from the xml file
     * @return A JSONObject containing the structured data from the XML file.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject(Reader reader, JSONPointer path) throws JSONException{
        // reads a xml file tag by tag
        // keeps going on until finds a tag given by first level of path
        // keeps going until finds the tag given by last level of path
        // as soon as the path is completed, the value of that xml tag represents the JSON object to return
        XMLParserConfiguration config = XMLParserConfiguration.ORIGINAL;
        List<String> pointerList = new ArrayList<String>();
        String [] pathStr;
        if (path.toString().equals("/")){
            pathStr = new String[0];
        }else{
            pathStr = path.toString().split("/");
        }
        // path should start with "/"
        if (pathStr.length >0 && !pathStr[0].equals("")){
            throw new JSONPointerException("Incorrectly formed JSON Pointer");
        }

        if (pathStr.length==0){
            if (!path.toString().equals("/")){
                throw new JSONPointerException("Incorrectly formed JSON Pointer");
            }
        }
        // start from second string because first should be empty because path should have started with "/"
        for (int i=1; i<pathStr.length; i++){
            pointerList.add(pathStr[i]); // add ea
        }

        JSONObject jo = new JSONObject();
        XMLTokener x = new XMLTokener(reader);
        boolean skipSaving = true;
        int recursionLevel = -1;
        // keep looking only while jo has nothing in it.
        // once jo has something, its time to return
        while (x.more() && jo.length()==0) {
            x.skipPast("<");
            if(x.more()) {
                parsesubObject(x, jo, null, config, pointerList, skipSaving, recursionLevel);
            }
        }
        return jo;
    }

    /**
     * This is a parse function that parses an XML string and looks for a subobject pointed to by the list of JSONPointer Strings
     * pointer. It skips over the name tags which are not equal to the next required named pointer (at the top of the list).
     * If the found tag is equal to the tag at top of the list then this tag is processed i.e. parse is called on that tag's value.
     * But if the next pointer is an integer, then we don't call parse again rather skip this tag and decrement next pointer's value by 1
     * We do this unless next pointer integer is 0 because we have to look for this named tag again (for e.g. if we find tag = book 
     * and next pointer is 1, then we skip book and decrement 1 to 0 since we are waiting for the next book tag which will have pointer=0)
     * Don't save into JSONObject if skipSaving is True. This is set true after subObject has been found and saved. Wed don't want
     * to save any more data.
     * Recursion level starts with -1 when we haven't found the subObject. It becomes 0 the first time we find the subObject
     * and keeps increasing by 1 each recursion after that. We reset skipSaving to false by checking when recursionLevel == 0.
     * @param reader
     * @param config
     * @return
     * @throws JSONException
     */

private static boolean parsesubObject(XMLTokener x, JSONObject context, String name, XMLParserConfiguration config, List<String> pointerList, boolean skipSaving, int recursionLevel)
    throws JSONException {
char c;
int i;
JSONObject jsonObject = null;
String string;
String tagName;
Object token;
XMLXsiTypeConverter<?> xmlXsiTypeConverter;
String topPointer; // pointer token at the top of the pointerList
List<String> newPointerList; // new pointer list to pass on to the next level of recursion

// Test for and skip past these forms:
// <!-- ... -->
// <! ... >
// <![ ... ]]>
// <? ... ?>
// Report errors for these forms:
// <>
// <=
// <<

token = x.nextToken();

// <!

if (token == BANG) {
    c = x.next();
    if (c == '-') {
        if (x.next() == '-') {
            x.skipPast("-->");
            return false;
        }
        x.back();
    } else if (c == '[') {
        token = x.nextToken();
        if ("CDATA".equals(token)) {
            if (x.next() == '[') {
                if (!skipSaving){
                    string = x.nextCDATA();
                    if (string.length() > 0) {
                        context.accumulate(config.getcDataTagName(), string);
                    }
                }
                return false;
            }
        }
        throw x.syntaxError("Expected 'CDATA['");
    }
    i = 1;
    do {
        token = x.nextMeta();
        if (token == null) {
            throw x.syntaxError("Missing '>' after '<!'.");
        } else if (token == LT) {
            i += 1;
        } else if (token == GT) {
            i -= 1;
        }
    } while (i > 0);
    return false;
} else if (token == QUEST) {

    // <?
    x.skipPast("?>");
    return false;
} else if (token == SLASH) {

    // Close tag </

    token = x.nextToken();
    if (name == null) {
        throw x.syntaxError("Mismatched close tag " + token);
    }
    if (!token.equals(name)) {
        throw x.syntaxError("Mismatched " + name + " and " + token);
    }
    if (x.nextToken() != GT) {
        throw x.syntaxError("Misshaped close tag");
    }
    return true;

} else if (token instanceof Character) {
    throw x.syntaxError("Misshaped tag");

    // Open tag <

} else {
    tagName = (String) token;
    token = null;
    jsonObject = new JSONObject();
    boolean nilAttributeFound = false;
    xmlXsiTypeConverter = null;
    
    //new logic to check if pointerList has any matches
    newPointerList = new ArrayList<String>(); // initialize empty new pointer list
        // copy the modified pointerList to newPointerList
    newPointerList.addAll(pointerList); 

    if (!newPointerList.isEmpty() && tagName.equals(newPointerList.get(0)))
    {
        // we are on the right tag
        topPointer = newPointerList.get(0);
        // check case where a next pointer exists that is integer. In this case, don't remove this pointer instead
        // decrement the next integer pointer and keep looking. Otherwise remove this top pointer
        if (newPointerList.size()>1 && (newPointerList.get(1).matches("[0-9]+"))){
            // second pointer is integer. check if it is zero, then we have found the topPointer and the zeroth value
            // if more than 0, then we keep looking and decrement
            if (Integer.parseInt(newPointerList.get(1))>0){
                // decrement my own pointer. newPointer doesen't have a meaning as any downstream recursion is 
                // useless for us so we don't decrement it.
                pointerList.set(1,String.valueOf(Integer.parseInt(pointerList.get(1))-1));
            }// check if integer value is 0 and not negative i.e. the zeroth value hasn't already passed
            else {
                //we have found zeroth element of an array with key = topPointer. safe to remove both pointers 
                // iff we don't have an existing context which would mean we have already found the desired subObject
                if (context.length()==0){
                    newPointerList.remove(1); //safe to remove the integer next pointer
                    newPointerList.remove(0); // safe to remove the top pointer
                }else{
                    // we don't have to find another JSONObject as two integer indices cannot occur one after another in xml
                }
            }
        }else{
            //next pointer is not an integer. can safetly remove the top pointer
            newPointerList.remove(0); 
        }
    }
    else if (!newPointerList.isEmpty() && !tagName.equals(newPointerList.get(0))){
        //tagname does not equal the tag we're looking for. WE can't ignore this otherwise 
        // /book may match /catalog/book in the nxt level
        //skip until we get to the end of this tag
        x.skipPast("</" + tagName + ">");
        return false; // we ended the newly found tag but not the "name" tag on which we are being called to investigate
    }

    // check if pointerList has become empty due to the above code or we inherited it empty
    if (newPointerList.isEmpty()){
        //we should start saving the jsonObject from here
        skipSaving = false;
        // when the pointerList becomes empty the first time, recursionLevel=0. We will stop saving when we return to 
        // recursionLevel=0
        recursionLevel +=1; 
    }

    for (;;) {
        if (token == null) {
            token = x.nextToken();
        }
        // attribute = value
        if (token instanceof String) {
            string = (String) token;
            token = x.nextToken();
            if (token == EQ) {
                token = x.nextToken();
                if (!(token instanceof String)) {
                    throw x.syntaxError("Missing value");
                }

                if (config.isConvertNilAttributeToNull()
                        && NULL_ATTR.equals(string)
                        && Boolean.parseBoolean((String) token)) {
                    nilAttributeFound = true;
                } else if(config.getXsiTypeMap() != null && !config.getXsiTypeMap().isEmpty()
                        && TYPE_ATTR.equals(string)) {
                    xmlXsiTypeConverter = config.getXsiTypeMap().get(token);
                } else if (!nilAttributeFound) {
                    if (!skipSaving){
                        jsonObject.accumulate(string,
                        config.isKeepStrings()
                                ? ((String) token)
                                : stringToValue((String) token));
                    }
                }
                token = null;
            } else {
                if (!skipSaving){
                    jsonObject.accumulate(string, "");
                }
            }


        } else if (token == SLASH) {
            // Empty tag <.../>
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped tag");
            }
            if (!skipSaving){
                if (nilAttributeFound) {
                    context.accumulate(tagName, JSONObject.NULL);
                } else if (jsonObject.length() > 0) {
                    context.accumulate(tagName, jsonObject);
                } else {
                    context.accumulate(tagName, "");
                }    
            }else{
                // do nothing. pointerList not yet empty
            }
            return false;

        } else if (token == GT) {
            // Content, between <...> and </...>
            for (;;) {
                token = x.nextContent();
                if (token == null) {
                    if (tagName != null) {
                        throw x.syntaxError("Unclosed tag " + tagName);
                    }
                    return false;
                } else if (token instanceof String) {
                    string = (String) token;
                    // if pointerList is empty, then only add to json otherwise skip adding, we're not 
                    // at the desired subObject yet
                    if (!skipSaving){
                        if (string.length() > 0) {
                            if(xmlXsiTypeConverter != null) {
                                jsonObject.accumulate(config.getcDataTagName(),
                                        stringToValue(string, xmlXsiTypeConverter));
                            } else {
                                jsonObject.accumulate(config.getcDataTagName(),
                                        config.isKeepStrings() ? string : stringToValue(string));
                            }
                        }
                    }else{
                        // still not found desired subObject. Let the loop continue and finish this tag
                    }
                } else if (token == LT) {
                    // Nested element
                    // call with updated newPointerList. This way on returning we maintain the original pointerList
                    if (parsesubObject(x, jsonObject, tagName, config, newPointerList, skipSaving, recursionLevel)) {
                        // pointerList empty means we are at inside the correct subObject, so we should 
                        // accumulate it into context for returning
                        // ideally the recursionLevel would be 1 or more here because recursionLevel=0 is the level
                        // at which the last tag is found. Usually for recursionLevel==0, we do not want to include its
                        // key in the returned jSONObject. 
                        // BUT, in case we get a call with an empty pointerList right from the start i.e., no JSONPointer, 
                        // just return the whole XML, then even at recursionLevel=0, we have to include its key.
                        // Hence the following if condition handles both these concepts with an && condition
                        // accumulate for returning up the recursion
                        if (pointerList.isEmpty() && recursionLevel>=0){
                            if (jsonObject.length() == 0) {
                                context.accumulate(tagName, "");
                            } else if (jsonObject.length() == 1
                                    && jsonObject.opt(config.getcDataTagName()) != null) {
                                context.accumulate(tagName, jsonObject.opt(config.getcDataTagName()));
                            } else {
                                context.accumulate(tagName, jsonObject);
                            }
                            // once skipSaving is off, we will keep returning true for all above recursion levels
                            // to signal end of tag
                            if (recursionLevel ==0 || recursionLevel ==-1){
                                // also time to return true now since we're back at recursionLevel==0. WE have
                                // found and filled the subObject. We return true so that upper recursion can stop
                                // adding new elements. returning true signals the end of a tag
                                return true;
                            }    
                            
                        }else{
                            // Here, we are either at recursionLevel== -1 i.e., haven't found the subObject, OR
                            // we are at recursionLevel==0, and we have just found the subObject inside jsonObject.
                            // In first case, jsonObject is empty so this code does nothing.
                            // In the second case, I I want upstream context to just pass through the underlying jsonObject
                            // because  I don't want any more upstream tags be added.
                            // I'll try to copy jsonObject into context
                            Iterator<String> it = jsonObject.keys();
                            context.clear(); // clear anything in context
                            while (it.hasNext()){
                                String key = it.next();
                                context.put(key, jsonObject.get(key));
                            }
                            // if you have data in context, and you're not in recursion >0 then you can go forward
                            // otherwise you got to keep looking
                            if (context.length() != 0){
                                //skip to where end of this tag is. it's like fast forward
                                x.skipPast("/"+tagName+">");
                                return true; // after concatenating at rec level 0, it's time to return true
                            }
                        }
                        return false;
                    }
                }
            }
        } else {
            throw x.syntaxError("Misshaped tag");
        }
    }
}
}


    /**
     * Convert a well-formed (but not necessarily valid) XML into a
     * JSONObject. Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * All values are converted as strings, for 1, 01, 29.0 will not be coerced to
     * numbers but will instead be the exact value as seen in the XML document.
     *
     * @param reader The XML source reader.
     * @param config Configuration options for the parser
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject(Reader reader, XMLParserConfiguration config) throws JSONException {
        JSONObject jo = new JSONObject();
        XMLTokener x = new XMLTokener(reader);
        while (x.more()) {
            x.skipPast("<");
            if(x.more()) {
                parse(x, jo, null, config);
            }
        }
        return jo;
    }

    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * JSONObject. Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code 
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * All values are converted as strings, for 1, 01, 29.0 will not be coerced to
     * numbers but will instead be the exact value as seen in the XML document.
     *
     * @param string
     *            The source string.
     * @param keepStrings If true, then values will not be coerced into boolean
     *  or numeric values and will instead be left as strings
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject(String string, boolean keepStrings) throws JSONException {
        return toJSONObject(new StringReader(string), keepStrings);
    }

    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * JSONObject. Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code 
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * All values are converted as strings, for 1, 01, 29.0 will not be coerced to
     * numbers but will instead be the exact value as seen in the XML document.
     *
     * @param string
     *            The source string.
     * @param config Configuration options for the parser.
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject(String string, XMLParserConfiguration config) throws JSONException {
        return toJSONObject(new StringReader(string), config);
    }

    /**
     * Convert a JSONObject into a well-formed, element-normal XML string.
     *
     * @param object
     *            A JSONObject.
     * @return A string.
     * @throws JSONException Thrown if there is an error parsing the string
     */
    public static String toString(Object object) throws JSONException {
        return toString(object, null, XMLParserConfiguration.ORIGINAL);
    }

    /**
     * Convert a JSONObject into a well-formed, element-normal XML string.
     *
     * @param object
     *            A JSONObject.
     * @param tagName
     *            The optional name of the enclosing tag.
     * @return A string.
     * @throws JSONException Thrown if there is an error parsing the string
     */
    public static String toString(final Object object, final String tagName) {
        return toString(object, tagName, XMLParserConfiguration.ORIGINAL);
    }

    /**
     * Convert a JSONObject into a well-formed, element-normal XML string.
     *
     * @param object
     *            A JSONObject.
     * @param tagName
     *            The optional name of the enclosing tag.
     * @param config
     *            Configuration that can control output to XML.
     * @return A string.
     * @throws JSONException Thrown if there is an error parsing the string
     */
    public static String toString(final Object object, final String tagName, final XMLParserConfiguration config)
            throws JSONException {
        StringBuilder sb = new StringBuilder();
        JSONArray ja;
        JSONObject jo;
        String string;

        if (object instanceof JSONObject) {

            // Emit <tagName>
            if (tagName != null) {
                sb.append('<');
                sb.append(tagName);
                sb.append('>');
            }

            // Loop thru the keys.
            // don't use the new entrySet accessor to maintain Android Support
            jo = (JSONObject) object;
            for (final String key : jo.keySet()) {
                Object value = jo.opt(key);
                if (value == null) {
                    value = "";
                } else if (value.getClass().isArray()) {
                    value = new JSONArray(value);
                }

                // Emit content in body
                if (key.equals(config.getcDataTagName())) {
                    if (value instanceof JSONArray) {
                        ja = (JSONArray) value;
                        int jaLength = ja.length();
                        // don't use the new iterator API to maintain support for Android
						for (int i = 0; i < jaLength; i++) {
                            if (i > 0) {
                                sb.append('\n');
                            }
                            Object val = ja.opt(i);
                            sb.append(escape(val.toString()));
                        }
                    } else {
                        sb.append(escape(value.toString()));
                    }

                    // Emit an array of similar keys

                } else if (value instanceof JSONArray) {
                    ja = (JSONArray) value;
                    int jaLength = ja.length();
                    // don't use the new iterator API to maintain support for Android
					for (int i = 0; i < jaLength; i++) {
                        Object val = ja.opt(i);
                        if (val instanceof JSONArray) {
                            sb.append('<');
                            sb.append(key);
                            sb.append('>');
                            sb.append(toString(val, null, config));
                            sb.append("</");
                            sb.append(key);
                            sb.append('>');
                        } else {
                            sb.append(toString(val, key, config));
                        }
                    }
                } else if ("".equals(value)) {
                    sb.append('<');
                    sb.append(key);
                    sb.append("/>");

                    // Emit a new tag <k>

                } else {
                    sb.append(toString(value, key, config));
                }
            }
            if (tagName != null) {

                // Emit the </tagName> close tag
                sb.append("</");
                sb.append(tagName);
                sb.append('>');
            }
            return sb.toString();

        }

        if (object != null && (object instanceof JSONArray ||  object.getClass().isArray())) {
            if(object.getClass().isArray()) {
                ja = new JSONArray(object);
            } else {
                ja = (JSONArray) object;
            }
            int jaLength = ja.length();
            // don't use the new iterator API to maintain support for Android
			for (int i = 0; i < jaLength; i++) {
                Object val = ja.opt(i);
                // XML does not have good support for arrays. If an array
                // appears in a place where XML is lacking, synthesize an
                // <array> element.
                sb.append(toString(val, tagName == null ? "array" : tagName, config));
            }
            return sb.toString();
        }

        string = (object == null) ? "null" : escape(object.toString());
        return (tagName == null) ? "\"" + string + "\""
                : (string.length() == 0) ? "<" + tagName + "/>" : "<" + tagName
                        + ">" + string + "</" + tagName + ">";

    }
}
