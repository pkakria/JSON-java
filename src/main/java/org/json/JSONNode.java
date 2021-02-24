package org.json;
/**
 * A class that encapsulates the object returned by toStream() method in JSONObject. Each JSONNode contains
 * two parameters
 * (1) value: A value of a key in JSON which can be one of a few types - JSONObject/ JSONArray/ String/ Null/ Boolean (True/False)
 * value is represented by the generic type Object
 * (2) path: A JSONPointer at which the value is located inside the JSONObject whose stream was returned. The complete JSONObject 
 * is represented by the JSONPointer jp = new JSONPointer("")
 */
public class JSONNode {
    // path to this node
    public JSONPointer path;
    // Object can be one of 
    // JSONObject
    // JSONArray
    // String
    // Null
    // Boolean (True/False)
    public Object value;
    
    public JSONNode(JSONPointer path, Object obj){
        this.path = path;
        this.value = obj;
    }

    public String toString(){
        if (value instanceof JSONObject){
            return ((JSONObject)value).toString(4);
        }else if (value instanceof JSONArray){
            return ((JSONArray)value).toString(4);
        }else {
            return String.valueOf(value);
        }
    }
    /**
     * Method that helps find out if two JSONNodes are equal. Returns true iff the path and value hold the exact same contents when converted to String
     */
    public boolean equals(Object node){
        if (node instanceof JSONNode){
            return this.path.toString().equals(((JSONNode)node).path.toString()) && this.value.toString().equals(((JSONNode)node).value.toString());
        }else{
            return false;
        }
    }
}