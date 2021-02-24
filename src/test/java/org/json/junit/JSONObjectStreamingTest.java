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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONNode;
import org.json.JSONPointer;
import org.json.JSONObject;
import org.json.XML;
import org.junit.Test;

import java.lang.NullPointerException;

/**
 * Test the streaming method toStream() inside JSONObject
 */

 public class JSONObjectStreamingTest{
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

    private String movieXMLstr = "<?xml version=\"1.0\"?>\n"+
    "<movies>\n"+
       "<movie id=\"mov1\">\n"+
          "<name>Die Hard</name>\n"+
          "<director>John McTiernan</director>\n"+
          "<release_date>07/20/1988</release_date>\n"+
          "<grossing>141.6</grossing>\n"+
          "<leading_cast>Bruce Willis</leading_cast>\n"+
          "<leading_cast>Alan Rickman</leading_cast>\n"+
       "</movie>\n"+
    "</movies>";

    private String booksXMLjsonStr = "{\"catalog\":{\"book\":[{\"author\":\"Gambardella, Matthew\",\"price\":44.95,\"genre\":\"Computer\",\"description\":\"An in-depth look at creating applications \\n"+"with XML.\",\"id\":\"bk101\",\"title\":\"XML Developer's Guide\",\"publish_date\":\"2000-10-01\"},{\"author\":\"Ralls, Kim\",\"price\":5.95,\"genre\":\"Fantasy\",\"description\":\"A former architect battles corporate zombies, \\n"+"an evil sorceress, and her own childhood to become queen \\n"+"of the world.\",\"id\":\"bk102\",\"title\":\"Midnight Rain\",\"publish_date\":\"2000-12-16\"}]}}";
    private String movieXMLjsonStr = "{\"movies\":{\"movie\":{\"release_date\":\"07/20/1988\",\"director\":\"John McTiernan\",\"name\":\"Die Hard\",\"leading_cast\":[\"Bruce Willis\",\"Alan Rickman\"],\"id\":\"mov1\",\"grossing\":141.6}}}";
    private JSONNode[] movieStreamArray = {
        new JSONNode(new JSONPointer(""), "{\"movies\":{\"movie\":{\"release_date\":\"07/20/1988\",\"director\":\"John McTiernan\",\"name\":\"Die Hard\",\"leading_cast\":[\"Bruce Willis\",\"Alan Rickman\"],\"id\":\"mov1\",\"grossing\":141.6}}}"),
        new JSONNode(new JSONPointer("/movies"), "{\"movie\":{\"release_date\":\"07/20/1988\",\"director\":\"John McTiernan\",\"name\":\"Die Hard\",\"leading_cast\":[\"Bruce Willis\",\"Alan Rickman\"],\"id\":\"mov1\",\"grossing\":141.6}}"),
        new JSONNode(new JSONPointer("/movies/movie"), "{\"release_date\":\"07/20/1988\",\"director\":\"John McTiernan\",\"name\":\"Die Hard\",\"leading_cast\":[\"Bruce Willis\",\"Alan Rickman\"],\"id\":\"mov1\",\"grossing\":141.6}"),
        new JSONNode(new JSONPointer("/movies/movie/release_date"), "07/20/1988"),
        new JSONNode(new JSONPointer("/movies/movie/director"), "John McTiernan"),
        new JSONNode(new JSONPointer("/movies/movie/name"), "Die Hard"),
        new JSONNode(new JSONPointer("/movies/movie/leading_cast"), "[\"Bruce Willis\",\"Alan Rickman\"]"),
        new JSONNode(new JSONPointer("/movies/movie/id"), "mov1"),
        new JSONNode(new JSONPointer("/movies/movie/grossing"), "141.6"),
        new JSONNode(new JSONPointer("/movies/movie/leading_cast/0"), "Bruce Willis"),
        new JSONNode(new JSONPointer("/movies/movie/leading_cast/1"), "Alan Rickman")       
};
    private JSONNode expectedMovie = new JSONNode(new JSONPointer("/movies/movie"), "{\"release_date\":\"07/20/1988\",\"director\":\"John McTiernan\",\"name\":\"Die Hard\",\"leading_cast\":[\"Bruce Willis\",\"Alan Rickman\"],\"id\":\"mov1\",\"grossing\":141.6}");

    //wrapper method for the lengthy name of System..println
    public void sout(Object obj){
       System.out.println(obj);
    }

    @Test
    public void shouldPrintEachElementOfXML(){
        Reader reader = new StringReader(movieXMLstr);
        List<JSONNode> actual = XML.toJSONObject(reader).toStream().collect(Collectors.toList());
        int i=0;
        for (JSONNode node: actual){
            assertEquals(node, movieStreamArray[i]);
            i++;
        }
        assertEquals(actual, Arrays.asList(movieStreamArray));
    }

    @Test
    public void shouldBeFilterableByPath(){
       Reader reader = new StringReader(movieXMLstr);
       // return the JSONObject with the details of the movie "Die Hard". It should only return one Object so we do findFirst()
       JSONNode actualMovie = XML.toJSONObject(reader).toStream().filter(node -> node.path.toString().equals("/movies/movie") && ((JSONObject)(node.value)).get("name").equals("Die Hard")).findFirst().orElse(null);
       assertEquals(expectedMovie, actualMovie);
    }

    @Test
    public void countOfStreamedNodesShouldBeCorrect(){
       Reader reader = new StringReader(booksXMLstr);
       long actualcount = XML.toJSONObject(reader).toStream().collect(Collectors.counting());
       assertEquals(19, actualcount);
    }

    /**
     * Expected behaviour with empty XML is that only one JSONObject should be returned that must be empty
     * This is intentionally different from returning an empty stream because empty stream may point to a 
     * non-existent or null JSONObject. Rather, an empty XML generates a valid JSONObject of size 0. This is
     * what is returned
     */
    @Test
    public void shouldReturnEmptyObjectOnEmptyXML(){
       Reader reader = new StringReader("");
       List<JSONNode> receivedList = XML.toJSONObject(reader).toStream().collect(Collectors.toList());
       assertEquals(receivedList.size(), 1);
       assertTrue(receivedList.get(0).value instanceof JSONObject);
       assertTrue("Received object must be empty", ((JSONObject)(receivedList.get(0).value)).isEmpty());
    }

    /**
     * Since toStream() method is a class method and it takes no argument, it will not be called on a null object.
     * Client code will throw an exception if client tries to call jo.toStream() where jo==null. Hence this exception
     * is to be handled by the client code itself, not JSON-java library. We confirm that client code throws this error.
     */
    @Test
    public void shouldReturnEmptyStreamOnNullObject(){
       JSONObject jo_empty = new JSONObject();
       // non-existing key. JSONObject.query() returns a null object
       JSONObject nullObject = (JSONObject)jo_empty.query("/hello");
         assertThrows(NullPointerException.class, ()-> nullObject.toStream().collect(Collectors.counting()));
    }
    
 }
