package org.json.junit;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.json.JSONTokener;
import org.json.XML;
import org.json.XMLParserConfiguration;
import org.json.XMLXsiTypeConverter;

public class XMLDebug {
    static String xmlStr = "<?xml version=\"1.0\"?>\n" + "<catalog>\n" + "<book id=\"bk101\">\n"
            + "<author>Gambardella, Matthew </author>\n" + "<title>XML Developer's Guide</title>\n"
            + "<genre>Computer</genre>\n" + "<price>44.95</price>\n" + "<publish_date>2000-10-01</publish_date>\n"
            + "<description>An in-depth look at creating applications \n" + "with XML.</description>\n" + "</book>\n"
            + "<book id=\"bk102\">\n" + "<author>Ralls, Kim</author>\n" + "<title>Midnight Rain</title>\n"
            + "<genre>Fantasy</genre>\n" + "<price>5.95</price>\n" + "<publish_date>2000-12-16</publish_date>\n"
            + "<description>A former architect battles corporate zombies, \n"
            + "an evil sorceress, and her own childhood to become queen \n" + "of the world.</description>\n"
            + "</book>\n" + "</catalog>";

    public XMLDebug() {
    }

    public void debugXMLtoJSON1() {

        try {
            Reader reader = new StringReader(xmlStr);
            JSONPointer path = new JSONPointer("/catalog/book/0/author");
            JSONObject jsonObject = XML.toJSONObject(reader, path);
            System.out.println(jsonObject.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Expecting a JSONException");
        }
    }

    public void debugXMLtoJSON2() {

        try {
            Reader reader = new StringReader(xmlStr);
            JSONPointer path = new JSONPointer("/catalog/book/0/author/key");
            JSONObject replacement = new JSONObject();
            replacement.put("newbook", "hemant");
            JSONObject jsonObject = XML.toJSONObject(reader, path, replacement);
            System.out.println(jsonObject.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Expecting a JSONException");
        }
    }

    public void debugXMLtoJSONModule3() {
        try {
            Reader reader = new StringReader(xmlStr);
            String newjostr = XML.toJSONObject(reader, (str) -> "hello_" + str).toString(4);
            System.out.println(newjostr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void debugJSONStreaming() {
        try {
            Reader reader = new StringReader(Files.readAllLines(Path.of("C:\\Users\\heman\\OneDrive\\Desktop\\ProgramingStyles\\Milestone1\\Milestone1\\file1.xml")).stream().reduce("", (s1, s2)-> String.join("\n", s1, s2)));
            XML.toJSONObject(reader).toStream().forEach((element) -> {
                System.out.println(element);
                System.out.println("---------------------------------------");
            });
            // XML.toJSONObject(reader).toStream().filter((node)->
            // node.path.toString().equals("")).forEach((el)-> {System.out.println(el);
            // System.out.println("------------------");});
        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void debugAsynchXMLReading() {
        try {
            //Reader reader = new StringReader(xmlStr);
            Reader reader = new FileReader(new File("C:\\Users\\heman\\OneDrive\\Desktop\\ProgramingStyles\\Milestone1\\Milestone1\\veryLargeFile.xml"));
            String str = "<catalog";
            Reader nullreader = new StringReader(str);
            Future<JSONObject> futureJSONObject = XML.toFutureJSONObject(nullreader, true);
            System.out.println("returned future object .");
            // for (int i=0; i<100; i++){
            //     System.out.print(Integer.toString(i)+" ");
            // }
            // System.out.println("");
            while (!futureJSONObject.isDone()) {
                System.out.println("Still reading...");
                Thread.sleep(1000);
            }
            System.out.println("future work is done .");
            JSONObject jo = futureJSONObject.get();
            // System.out.println("Writing object to file .");
            // Files.writeString(Path.of("newTest.xml"), jo.toString(4), Charset.defaultCharset());
            // System.out.println("Written to file .");
            //System.out.println(jo.toString(4));
        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void shouldFinishSmallerFileFirst(){
        try {
            Reader bigReader = new FileReader(Path.of("C:\\Users\\heman\\OneDrive\\Documents\\gitrepos\\JSON-java\\JSON-java\\src\\test\\java\\org\\json\\junit","data", "BigXMLFileAsynchronousTest.XML").toFile());
            Reader smallReader = new StringReader("");
            long t1 = System.currentTimeMillis();
            Future<JSONObject> futureBigObject = XML.toFutureJSONObject(bigReader, true);
            Future<JSONObject> futureSmallObject = XML.toFutureJSONObject(smallReader, true);
            JSONObject smallJo = futureSmallObject.get();
            long t2 = System.currentTimeMillis();
            JSONObject bigJo = futureBigObject.get();
            long t3 = System.currentTimeMillis();
            System.out.println("big object returned in "+ (t3-t1));
            System.out.println("small object returned in "+ (t2-t1));
        } catch (FileNotFoundException e) {
            // could not test as file could not be found. so test is pass automatically
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void main(String [] args){
        XMLDebug debugger = new XMLDebug();
        //debugger.debugXMLtoJSON1();
     //   debugger.debugXMLtoJSON2();
        // debugger.debugXMLtoJSONModule3();
        //debugger.debugJSONStreaming();
        // debugger.debugAsynchXMLReading();
        debugger.shouldFinishSmallerFileFirst();
        //System.out.println("End of main");
    }
}