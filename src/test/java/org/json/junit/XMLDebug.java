package org.json.junit;

import static org.junit.Assert.fail;

import java.io.Reader;
import java.io.StringReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.json.JSONTokener;
import org.json.XML;
import org.json.XMLParserConfiguration;
import org.json.XMLXsiTypeConverter;


public class XMLDebug{
    String xmlStr = "<?xml version=\"1.0\"?>\n"+
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

    public XMLDebug(){}

    public void debugXMLtoJSON1(){
        
        try {
            Reader reader = new StringReader(xmlStr);
            JSONPointer path = new JSONPointer("/catalog/book/0");
            JSONObject jsonObject = XML.toJSONObject(reader, path);
            System.out.println(jsonObject.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Expecting a JSONException");
        }
    }

    public void debugXMLtoJSON2(){
        
        try {
            Reader reader = new StringReader(xmlStr);
            JSONPointer path = new JSONPointer("/catalog/book/0");
            JSONObject replacement = new JSONObject();
            replacement.put("newbook", "hemant");
            JSONObject jsonObject = XML.toJSONObject(reader, path, replacement);
            System.out.println(jsonObject.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Expecting a JSONException");
        }
    }

    public static void main(String [] args){
        XMLDebug debugger = new XMLDebug();
        debugger.debugXMLtoJSON1();
        debugger.debugXMLtoJSON2();
    }
}