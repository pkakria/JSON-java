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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
public class XMLAsynchronousReadingTest {
    /**
     * JUnit supports temporary files and folders that are cleaned up after the test.
     * https://garygregory.wordpress.com/2010/01/20/junit-tip-use-rules-to-manage-temporary-files-and-folders/ 
     */
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

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

    
    /**
     * Test method to test whether the asycnrhonous method toFutureJSONObject(Reader, keepStrings) is throwing an executionException
     * when called with an unformed xml. This illustrates that the code was run concurrently.
     * Expects a ExecutionException
     * @throws InterruptedException
     * @throws ExecutionException
     */ 
    @Test(expected=ExecutionException.class)
    public void shouldThrowExecutionException() throws InterruptedException, ExecutionException {
        String str = "<catalog";
        Reader nullreader = new StringReader(str);
        Future<JSONObject> futureJSONObject = XML.toFutureJSONObject(nullreader, true);
        while (!futureJSONObject.isDone()) {
            Thread.sleep(1000);
        }
        JSONObject jo = futureJSONObject.get();
        assertTrue("JSONObject should be empty since exception is thrown", jo.isEmpty());
    }

     /**
     * This test tests that a small empty xml file that is started to be processed asynchronously after a big xml file has 
     * started to be processed asynchronously, finishes first. In other words, asynchronous processing allows shorter workloads
     * to finish first unlike sequential processing where the order of calling methods determines the order of finishing of processing.
     */
    @Test
    public void shouldFinishSmallerFileFirst(){
        try {
            Reader bigReader = new FileReader(Path.of("data", "BigXMLFileAsynchronousTest.xml").toFile());
            Reader smallReader = new StringReader("");
            Future<JSONObject> futureBigObject = XML.toFutureJSONObject(bigReader, true);
            Future<JSONObject> futureSmallObject = XML.toFutureJSONObject(smallReader, true);
            JSONObject smallJo = futureSmallObject.get();
            assertFalse("big object should not be done yet", futureBigObject.isDone());
            JSONObject bigJo = futureBigObject.get();
        } catch (FileNotFoundException e) {
            // could not test as file could not be found. so test is pass automatically
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void shouldReturnCorrectJSONObject(){
        try{
        Reader reader = new StringReader(booksXMLstr);
        // do not keep strings
        Future<JSONObject> futureJSONObject = XML.toFutureJSONObject(reader, false);
        JSONObject actual = futureJSONObject.get();
        JSONObject expected = new JSONObject(booksXMLjsonStr);
        Util.compareActualVsExpectedJsonObjects(actual,expected);
    }catch(InterruptedException ex){
        // can't do mmuch got interruped
    } catch (ExecutionException e) {
        fail("Should not throw an ExecutionException");
    }
    }

}