package org.json.junit;

/*
Copyright (c) 2020 JSON.org

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.json.XML;
import org.json.XMLParserConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for JSON-Java XML.java with XMLParserConfiguration.java
 */
public class XMLSubObjectAdvancedTest {

    private String booksXMLstr = "<?xml version=\"1.0\"?>\n"+
        "<catalog>\n"+
           "<book id=\"bk101\">\n"+
              "<author>Gambardella, Matthew</author>\n"+
              "<title>XML Developer's Guide</title>\n"+
              "<genre>Computer</genre>\n"+
              "<price>44.95</price>\n"+
              "<publish_date>2000-10-01</publish_date>\n"+
              "<description>An in-depth look at creating applications \n"+
              "with XML.</description>\n"+
           "</book>\n"+
           "<book id=\"bk102\">\n"+
              "<author>Ralls, Kim</author>\n"+
              "<title>Midnight Rain</title>\n"+
              "<genre>Fantasy</genre>\n"+
              "<price>5.95</price>\n"+
              "<publish_date>2000-12-16</publish_date>\n"+
              "<description>A former architect battles corporate zombies, \n"+
              "an evil sorceress, and her own childhood to become queen \n"+
              "of the world.</description>\n"+
           "</book>\n"+
        "</catalog>";

        private String booksXMLjsonStr = "{\"catalog\":{\"book\":[{\"author\":\"Gambardella, Matthew\",\"price\":44.95,\"genre\":\"Computer\",\"description\":\"An in-depth look at creating applications \\n"+"with XML.\",\"id\":\"bk101\",\"title\":\"XML Developer's Guide\",\"publish_date\":\"2000-10-01\"},{\"author\":\"Ralls, Kim\",\"price\":5.95,\"genre\":\"Fantasy\",\"description\":\"A former architect battles corporate zombies, \\n"+"an evil sorceress, and her own childhood to become queen \\n"+"of the world.\",\"id\":\"bk102\",\"title\":\"Midnight Rain\",\"publish_date\":\"2000-12-16\"}]}}";
        private String booksSMLjsonStr_catalog = "{\"book\":[{\"author\":\"Gambardella, Matthew\",\"price\":44.95,\"genre\":\"Computer\",\"description\":\"An in-depth look at creating applications \\n"+"with XML.\",\"id\":\"bk101\",\"title\":\"XML Developer's Guide\",\"publish_date\":\"2000-10-01\"},{\"author\":\"Ralls, Kim\",\"price\":5.95,\"genre\":\"Fantasy\",\"description\":\"A former architect battles corporate zombies, \\n"+"an evil sorceress, and her own childhood to become queen \\n"+"of the world.\",\"id\":\"bk102\",\"title\":\"Midnight Rain\",\"publish_date\":\"2000-12-16\"}]}";
        private String booksXMLjsonStr_catalog_book_0 = "{\"author\":\"Gambardella, Matthew\",\"price\":44.95,\"genre\":\"Computer\",\"description\":\"An in-depth look at creating applications \\n"+"with XML.\",\"id\":\"bk101\",\"title\":\"XML Developer's Guide\",\"publish_date\":\"2000-10-01\"}";
        private String booksXMLjsonStr_catalog_book_1 = "{\"author\":\"Ralls, Kim\",\"price\":5.95,\"genre\":\"Fantasy\",\"description\":\"A former architect battles corporate zombies, \\n"+"an evil sorceress, and her own childhood to become queen \\n"+"of the world.\",\"id\":\"bk102\",\"title\":\"Midnight Rain\",\"publish_date\":\"2000-12-16\"}";

        private String replacementJSONStr = "{\"hellokey\": [\"hellovalue1\", \"hellovalue2\", 3]}";
        private String booksXML_jsonStr_replaced = "{\"hellokey\": [\"hellovalue1\", \"hellovalue2\", 3]}";
        private String booksXML_jsonStr_catalog_book_0_replaced = "{\"catalog\":{\"book\":[{\"hellokey\": [\"hellovalue1\", \"hellovalue2\", 3]},{\"author\":\"Ralls, Kim\",\"price\":5.95,\"genre\":\"Fantasy\",\"description\":\"A former architect battles corporate zombies, \\n"+"an evil sorceress, and her own childhood to become queen \\n"+"of the world.\",\"id\":\"bk102\",\"title\":\"Midnight Rain\",\"publish_date\":\"2000-12-16\"}]}}";
        private String booksXML_jsonStr_catalog_book_replaced = "{\"catalog\":{\"book\":{\"hellokey\": [\"hellovalue1\", \"hellovalue2\", 3]}}}";
        private String booksXMLjsonStr_catalog_book_0_author_replaced_empty = "{\"catalog\":{\"book\":[{\"author\":\"\",\"price\":44.95,\"genre\":\"Computer\",\"description\":\"An in-depth look at creating applications \\n"+"with XML.\",\"id\":\"bk101\",\"title\":\"XML Developer's Guide\",\"publish_date\":\"2000-10-01\"},{\"author\":\"Ralls, Kim\",\"price\":5.95,\"genre\":\"Fantasy\",\"description\":\"A former architect battles corporate zombies, \\n"+"an evil sorceress, and her own childhood to become queen \\n"+"of the world.\",\"id\":\"bk102\",\"title\":\"Midnight Rain\",\"publish_date\":\"2000-12-16\"}]}}";

        /**
     * JUnit supports temporary files and folders that are cleaned up after the test.
     * https://garygregory.wordpress.com/2010/01/20/junit-tip-use-rules-to-manage-temporary-files-and-folders/ 
     */
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    /**
     * Test method to test whether the toJSONObject(Reader, JSONPointer) is working well
     */
 
    @Test
    public void shouldReturnCompleteXML() {
        String xmlStr = booksXMLstr;
        JSONPointer jp = new JSONPointer("/");
        Reader reader = new StringReader(xmlStr);
        JSONObject jsonObject = 
                XML.toJSONObject(reader, jp);
        JSONObject expectedJsonObject =  new JSONObject(booksXMLjsonStr);
        Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);
    }
    /**
     * Test method to test whether the toJSONObject(Reader, JSONPointer) returns
     * a subObject correctly
     */
 
    @Test
    public void shouldReturnSubObject(){
        String xmlStr = booksXMLstr;
        JSONPointer jp = new JSONPointer("/catalog");
        Reader reader = new StringReader(xmlStr);
        JSONObject jsonObject = 
                XML.toJSONObject(reader, jp);
        JSONObject expectedJsonObject = new JSONObject(booksSMLjsonStr_catalog);
        Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);
    }
    /**
     * Test method to test whether the toJSONObject(Reader, JSONPointer) works
     * with an array index
     */
  
    @Test
    public void shouldReturnSubObjectInArray(){
        String xmlStr = booksXMLstr;
        JSONPointer jp = new JSONPointer("/catalog/book/0");
        Reader reader = new StringReader(xmlStr);
        JSONObject jsonObject = 
                XML.toJSONObject(reader, jp);
        JSONObject expectedJsonObject = new JSONObject(booksXMLjsonStr_catalog_book_0);
        Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);

        //second element of array
        JSONPointer jp2 = new JSONPointer("/catalog/book/1");
        reader = new StringReader(xmlStr);
        JSONObject jsonObject2 = XML.toJSONObject(reader, jp2);
        JSONObject expectedJsonObject2 = new JSONObject(booksXMLjsonStr_catalog_book_1);
        Util.compareActualVsExpectedJsonObjects(jsonObject2,expectedJsonObject2);
    }
    /**
     * Test method to test whether the toJSONObject(Reader, JSONPointer) works
     * with an array index
     */
  
    @Test
    public void shouldReturnEmptyJSONObjectOnIncorrectJSONPointer(){
        String xmlStr = booksXMLstr;
        JSONPointer jp = new JSONPointer("/book/0");
        Reader reader = new StringReader(xmlStr);
        JSONObject jsonObject = XML.toJSONObject(reader, jp);
        assertTrue("An Incorrect JSONPointer should return an empty JSONObject", jsonObject.isEmpty());   
    }
    /**
     * should replace the whole xml
     */
    @Test
    public void shouldReplaceWholeXML(){
        String xmlStr = booksXMLstr;
        JSONPointer jp = new JSONPointer("/");
        JSONObject replacement = new JSONObject(replacementJSONStr);
        Reader reader = new StringReader(xmlStr);
        JSONObject jsonObject = XML.toJSONObject(reader, jp, replacement);
        JSONObject expectedJsonObject = new JSONObject(booksXML_jsonStr_replaced);
        Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);
    }

    /**
     * Should replace a subobject inside the XML whose value is not an array
     */
    @Test
    public void shouldReplaceSubObjectXML(){
        String xmlStr = booksXMLstr;
        JSONPointer jp = new JSONPointer("/catalog/book/0");
        JSONObject replacement = new JSONObject(replacementJSONStr);
        Reader reader = new StringReader(xmlStr);
        JSONObject jsonObject = XML.toJSONObject(reader, jp, replacement);
        JSONObject expectedJsonObject = new JSONObject(booksXML_jsonStr_catalog_book_0_replaced);
        Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);
    }

    @Test
    public void shouldNotAlterXMLWhenBadJSONPointer(){
        String xmlStr = booksXMLstr;
        //bad json pointer
        JSONPointer jp = new JSONPointer("/book/0/author");
        JSONObject replacement = new JSONObject(replacementJSONStr);
        Reader reader = new StringReader(xmlStr);
        JSONObject jsonObject = XML.toJSONObject(reader, jp, replacement);
        // original JSON
        JSONObject expectedJsonObject = new JSONObject(booksXMLjsonStr);
        Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);
    }

    @Test
    public void shouldHandleEmptyReplacement(){
        String xmlStr = booksXMLstr;
        JSONPointer jp = new JSONPointer("/catalog/book/0/author");
        JSONObject replacement = new JSONObject();
        System.out.println(replacement.toString());
        Reader reader = new StringReader(xmlStr);
        JSONObject jsonObject = XML.toJSONObject(reader, jp, replacement);
        JSONObject expectedJsonObject = new JSONObject(booksXMLjsonStr_catalog_book_0_author_replaced_empty);
        Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);
    }
    /**
     * Should replace a subobject inside the XML whose value IS AN array
     */
    @Test
    public void shouldReplaceSubArrayXML(){
        String xmlStr = booksXMLstr;
        JSONPointer jp = new JSONPointer("/catalog/book");
        JSONObject replacement = new JSONObject(replacementJSONStr);
        Reader reader = new StringReader(xmlStr);
        JSONObject jsonObject = XML.toJSONObject(reader, jp, replacement);
        JSONObject expectedJsonObject = new JSONObject(booksXML_jsonStr_catalog_book_replaced);
        Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);
    }




}