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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.function.Function;

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
 * Tests for different functionalities of the XML.toJSONObject(Reader, Function<String, String>)
 */
public class XMLKeyTransformerTest{

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
    private String booksXMLjsonStr_emptykey = "{\"\":{\"\":[{\"\":[\"bk101\",\"Gambardella, Matthew\",\"XML Developer's Guide\",\"Computer\",44.95,\"2000-10-01\",\"An in-depth look at creating applications \\n"+"with XML.\"]},{\"\":[\"bk102\",\"Ralls, Kim\",\"Midnight Rain\",\"Fantasy\",5.95,\"2000-12-16\",\"A former architect battles corporate zombies, \\n"+"an evil sorceress, and her own childhood to become queen \\n"+"of the world.\"]}]}}";
    private String booksXMLjsonStr_backslash = "{\"\\\\catalog\":{\"\\\\book\":[{\"\\\\author\":\"Gambardella, Matthew\",\"\\\\price\":44.95,\"\\\\genre\":\"Computer\",\"\\\\description\":\"An in-depth look at creating applications \\n"+"with XML.\",\"\\\\id\":\"bk101\",\"\\\\title\":\"XML Developer's Guide\",\"\\\\publish_date\":\"2000-10-01\"},{\"\\\\author\":\"Ralls, Kim\",\"\\\\price\":5.95,\"\\\\genre\":\"Fantasy\",\"\\\\description\":\"A former architect battles corporate zombies, \\n"+"an evil sorceress, and her own childhood to become queen \\n"+"of the world.\",\"\\\\id\":\"bk102\",\"\\\\title\":\"Midnight Rain\",\"\\\\publish_date\":\"2000-12-16\"}]}}";
    private String booksXMLjsonStr_withnewline = "{\"catalog\\nkey2\":{\"book\\nkey2\":[{\"author\\nkey2\":\"Gambardella, Matthew\",\"price\\nkey2\":44.95,\"genre\\nkey2\":\"Computer\",\"description\\nkey2\":\"An in-depth look at creating applications \\n"+"with XML.\",\"id\\nkey2\":\"bk101\",\"title\\nkey2\":\"XML Developer's Guide\",\"publish_date\\nkey2\":\"2000-10-01\"},{\"author\\nkey2\":\"Ralls, Kim\",\"price\\nkey2\":5.95,\"genre\\nkey2\":\"Fantasy\",\"description\\nkey2\":\"A former architect battles corporate zombies, \\n"+"an evil sorceress, and her own childhood to become queen \\n"+"of the world.\",\"id\\nkey2\":\"bk102\",\"title\\nkey2\":\"Midnight Rain\",\"publish_date\\nkey2\":\"2000-12-16\"}]}}";
    @Test
    public void ShouldHandleIdentifyFunction(){
        StringReader reader = new StringReader(booksXMLstr);
        JSONObject actual = XML.toJSONObject(reader, Function.<String>identity());
        System.out.println(actual.toString(4));
        JSONObject expectedJsonObject = new JSONObject(booksXMLjsonStr);
        Util.compareActualVsExpectedJsonObjects(actual, expectedJsonObject);
    }

    @Test
    public void ShouldHandleBackSlashFunction(){
      StringReader reader = new StringReader(booksXMLstr);
      //add double backslash to represent \ inside string
      JSONObject actual = XML.toJSONObject(reader, (str)-> "\\"+str);
      System.out.println(actual.toString(4));
      //expected JSONObject will escape the backslash to represent backslash inside key string hence we will have two backslashes. 
      // Our booksXMLjsonStr_backslash String then has four backslashes to represent the two backslashes inside key string
      JSONObject expectedJsonObject = new JSONObject(booksXMLjsonStr_backslash);
      Util.compareActualVsExpectedJsonObjects(actual, expectedJsonObject);
    }

    @Test
    public void NullFunctionThrowsException(){
      StringReader reader = new StringReader(booksXMLstr);
      boolean throwsEx = false;
      try{
         JSONObject actual = XML.toJSONObject(reader, (str)-> null);
         fail("Making a key null should throw JSONException");
      }catch(JSONException jex){
         throwsEx = true;
      }
      assertTrue(throwsEx);
    }

    
    @Test
    public void NonStringReturningFunctionThrowsException(){
      StringReader reader = new StringReader(booksXMLstr);
      boolean throwsEx = false;
      try{
      JSONObject actual = XML.toJSONObject(reader, (str)-> (String)(Object)Integer.valueOf(1));
      fail("Giving a function that returns a non-string key should throw JSONException");
      }catch(JSONException jex){
         throwsEx = true;
      }
      assertTrue(throwsEx);
    }

    @Test
    public void ErrorInFunctionThrowsException(){
      StringReader reader = new StringReader(booksXMLstr);
      boolean throwsEx = false;
      try{
         // our keys don't have space so index 1 doesen't exist
      JSONObject actual = XML.toJSONObject(reader, (number)-> number.split(" ")[1]);
      fail("Giving a function which throws an error should throw JSONException");
      }catch(JSONException jex){
         throwsEx = true;
      }
      assertTrue(throwsEx);
    }
    
    @Test
    public void ShouldHandleNewlineKeyTransformation(){
      StringReader reader = new StringReader(booksXMLstr);
      JSONObject actual = XML.toJSONObject(reader, (str)-> str+ "\n" + "key2");
      System.out.println(actual.toString(4));
      JSONObject expectedJsonObject = new JSONObject(booksXMLjsonStr_withnewline);
      Util.compareActualVsExpectedJsonObjects(actual, expectedJsonObject);
    }

    @Test
    public void ShouldHandleEmptyKeyTransformation(){
      StringReader reader = new StringReader(booksXMLstr);
      JSONObject actual = XML.toJSONObject(reader, (str)-> "");
      System.out.println(actual.toString(4));
      JSONObject expectedJsonObject = new JSONObject(booksXMLjsonStr_emptykey);
      Util.compareActualVsExpectedJsonObjects(actual, expectedJsonObject);
    }
}