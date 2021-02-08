JSON in Java [package org.json]
===============================

[![Maven Central](https://img.shields.io/maven-central/v/org.json/json.svg)](https://mvnrepository.com/artifact/org.json/json)

**[Click here if you just want the latest release jar file.](https://repo1.maven.org/maven2/org/json/json/20201115/json-20201115.jar)**


## Latst code Updates:
# Milestone3
Newly added methods
* Added a new public method in [XML.java](https://github.com/pkakria/JSON-java/blob/master/src/main/java/org/json/XML.java) `static JSONObject toJSONObject(Reader reader, Function<String, String> fun)`
* Added a new private method in [XML.java](https://github.com/pkakria/JSON-java/blob/master/src/main/java/org/json/XML.java) `static boolean parseTransformKey(XMLTokener x, JSONObject context, String name, XMLParserConfiguration config, Function<String, String> fun) throws JSONException`

Explanation of newly added methods:
* The method `toJSONObject(Reader reader, Function<String, String> fun)` allows a client to provide a function that transforms a String key into another String key and returns the XML file as a JSONObject. The `parseTransformKey(...)` method almost replicates the `parse(...)` method in XML.java except that an overall try-catch block has been added and the client supplied function is applied on the key before accumulating it to the JSONObject.

Testing code added: 
* Added a new file [XMLKeyTransformerTest.java](https://github.com/pkakria/JSON-java/blob/master/src/test/java/org/json/junit/XMLKeyTransformerTest.java) in test/java/org/json/junit that tests for different corner cases, exception handling, and correct operation of the newly added methods.

Performance Improvements Versus Milestone 1
* Adding prefix to keys is much faster when done inside the library compared to when done outside the library. 
For e.g., a comparison of time taken for prefixing all keys of books.xml with "swe262_", using milestone 1 method outside the library and milestone 3 method inside the library is shown in Table I. 
* The reason for this is that converting XML to JSON object takes a long time and is the bottleneck. Outside the library, an XML must first be converted to a JSONObject and then it's keys are prefixed iteratively.
Inside the library, the JSONObject can be constructed with prefixed keys from the start itself. We measured the time to construct a JSONObject from XML for books.xml to be **47.2 msec** on average (over 10 runs). Hence, doing prefixing outside the library incurs this much overhead compared to prefixing inside the library. 

Table I: Time to transform keys of books.XML
|Using Method | Time to Run (msec)|
|-------------| -----------|
|Outside Library | **50.7**|
|Library method| **5.6**|

Build Script
* Do `.\gradlew.bat build` in the JSON-java folder on your windows machine
* jar file is created inside JSON-java\build\libs folder. You can use this jar file to run any code.

# Milestone2
Two new public functions have been added in XML.java
* static JSONObject toJSONObject(Reader reader, JSONPointer path) 
* static JSONObject toJSONObject(Reader reader, JSONPointer path, JSONObject replacement) 

Two new private functions have been added in XML.java
*  private static boolean parseReplaceSubObject(XMLTokener x, JSONObject context, String name, XMLParserConfiguration config, List<String> pointerList, boolean [] foundObj, int recursionLevel, JSONObject replacement) throws JSONException
* private static boolean parsesubObject(XMLTokener x, JSONObject context, String name, XMLParserConfiguration config, List<String> pointerList, boolean skipSaving, int recursionLevel) throws JSONException

Explanation of code flow:
* The toJSONObject() method responsible for finding and returning a JSONObject corresponding to a JSONPointer calls the parsesubObject()
method for its job. 
* The toJSONObject() method responsible for finding and replacing a JSONObject corresponding to a JSONPointer and returning the complete XML file as a JSONObject  calls the parseReplaceSubObject() method for its job. 

Testing code added:
* Two new files have been added XMLSubObjectBasicTest.java, XMLSubObjectAdvancedTest.java.

PErformance optimizations done:
* parsesubObject() recursively looks for the subObject desired. Once it finds the subObject, it does not parse the XML file further and 
skips the Reader to the end of XML tags and eventually returns.
* parseReplaceSubObject() recursively looks for the object to be replaced. On finding the desired subObject, it does not parse that object but rather skips to the end of that object's location. This is another performance optimization.

Steps to build:
* Do .\gradlew.bat build in the JSON-java folder on your windows machine
* jar file is created inside JSON-java\build\libs folder. You can use this jar file to run any code.

More information about the two functions behaviour:
* static JSONObject toJSONObject(Reader reader, JSONPointer path) 
1. If a JSONPointer points to a path in XML file that does not exist, then an empty JSONObject {} is returned.
2. If the JSONPointer points to a JSONArray, then the method behaviour is not specified. It may return the first element of the array as a JSONObject.
3. If the JSONPointer includes an index such as 0, then the first occurence of the tag preceding 0, is taken as the zeroth element of a JSONArray and the method tries to return it value as a JSONObject. For e.g., for books.xml if you specify JSONPointer path = "/catalog/0", then the method will only read the first occurence of <catalog> tag and return its value. It will not check whether occurences of <catalog> occur at the same level or not (i.e., whether "catalog" key really points to a JSONArray or not).
4. If the JSONPointer points to an object which is neither a JSONObject nor a JSONArray, then the behaviour of the method is undefined. The method may return a JSONObject with the key="content" and value=object.

* static JSONObject toJSONObject(Reader reader, JSONPointer path, JSONObject replacement) 
1. If the JSONPointer points to a valid JSONObject, this object is replaced with replacement and the resulting complete XML file is returned as a JSONObject.
2. If JSONPointer points to a JSONArray, the behaviour of this method is undefined. It may replace each element of the JSONArray with replacement.
3. If the JSONPointer points to a set of tags, at least one of which do not exist in the given order, then the behaviour is undefined. The method may not replace anything.
4. If the JSONPOinter points to a set of tags that are a set of valid tags but really one of the tags is a JSONArray, then the behaviour is undefined. The output may replace all occurences of these set of tags in the XML file. For e.g., if user specifies /catalog/book/price then there are actually two books and user is not specifying which book index. Hence, our method's behaviour is undefined. We may replace both price tags with the same object.
5. If the JSONPointer points to anything that's not a JSONObject/JSONArray, this value is still replaced with the JSONObject replacement and the resulting XML is returned as a JSONObject
# Overview

[JSON](http://www.JSON.org/) is a light-weight language-independent data interchange format.

The JSON-Java package is a reference implementation that demonstrates how to parse JSON documents into Java objects and how to generate new JSON documents from the Java classes.

Project goals include:
* Reliable and consistent results
* Adherence to the JSON specification 
* Easy to build, use, and include in other projects
* No external dependencies
* Fast execution and low memory footprint
* Maintain backward compatibility
* Designed and tested to use on Java versions 1.6 - 1.11

The files in this package implement JSON encoders and decoders. The package can also convert between JSON and XML, HTTP headers, Cookies, and CDL.

The license includes this restriction: ["The software shall be used for good, not evil."](https://en.wikipedia.org/wiki/Douglas_Crockford#%22Good,_not_Evil%22) If your conscience cannot live with that, then choose a different package.

**If you would like to contribute to this project**

Bug fixes, code improvements, and unit test coverage changes are welcome! Because this project is currently in the maintenance phase, the kinds of changes that can be accepted are limited. For more information, please read the [FAQ](https://github.com/stleary/JSON-java/wiki/FAQ).

# Build Instructions

The org.json package can be built from the command line, Maven, and Gradle. The unit tests can be executed from Maven, Gradle, or individually in an IDE e.g. Eclipse.
 
**Building from the command line**

*Build the class files from the package root directory src/main/java*
````
javac org\json\*.java
````

*Create the jar file in the current directory*
````
jar cf json-java.jar org/json/*.class
````

*Compile a program that uses the jar (see example code below)*
````
javac -cp .;json-java.jar Test.java 
````

*Test file contents*

````
import org.json.JSONObject;
public class Test {
    public static void main(String args[]){
       JSONObject jo = new JSONObject("{ \"abc\" : \"def\" }");
       System.out.println(jo.toString());
    }
}
````

*Execute the Test file*
```` 
java -cp .;json-java.jar Test
````

*Expected output*

````
{"abc":"def"}
````

 
**Tools to build the package and execute the unit tests**

Execute the test suite with Maven:
```
mvn clean test
```

Execute the test suite with Gradlew:

```
gradlew clean build test
```

# Notes

**Recent directory structure change**

_Due to a recent commit - [#515 Merge tests and pom and code](https://github.com/stleary/JSON-java/pull/515) - the structure of the project has changed from a flat directory containing all of the Java files to a directory structure that includes unit tests and several tools used to build the project jar and run the unit tests. If you have difficulty using the new structure, please open an issue so we can work through it._

**Implementation notes**

Numeric types in this package comply with
[ECMA-404: The JSON Data Interchange Format](http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf) and
[RFC 8259: The JavaScript Object Notation (JSON) Data Interchange Format](https://tools.ietf.org/html/rfc8259#section-6).
This package fully supports `Integer`, `Long`, and `Double` Java types. Partial support
for `BigInteger` and `BigDecimal` values in `JSONObject` and `JSONArray` objects is provided
in the form of `get()`, `opt()`, and `put()` API methods.

Although 1.6 compatibility is currently supported, it is not a project goal and might be
removed in some future release.

In compliance with RFC8259 page 10 section 9, the parser is more lax with what is valid
JSON then the Generator. For Example, the tab character (U+0009) is allowed when reading
JSON Text strings, but when output by the Generator, the tab is properly converted to \t in
the string. Other instances may occur where reading invalid JSON text does not cause an
error to be generated. Malformed JSON Texts such as missing end " (quote) on strings or
invalid number formats (1.2e6.3) will cause errors as such documents can not be read
reliably.

Some notable exceptions that the JSON Parser in this library accepts are:
* Unquoted keys `{ key: "value" }`
* Unquoted values `{ "key": value }`
* Unescaped literals like "tab" in string values `{ "key": "value   with an unescaped tab" }`
* Numbers out of range for `Double` or `Long` are parsed as strings

Recent pull requests added a new method `putAll` on the JSONArray. The `putAll` method
works similarly to other `put` methods in that it does not call `JSONObject.wrap` for items
added. This can lead to inconsistent object representation in JSONArray structures.

For example, code like this will create a mixed JSONArray, some items wrapped, others
not:

```java
SomeBean[] myArr = new SomeBean[]{ new SomeBean(1), new SomeBean(2) };
// these will be wrapped
JSONArray jArr = new JSONArray(myArr);
// these will not be wrapped
jArr.putAll(new SomeBean[]{ new SomeBean(3), new SomeBean(4) });
```

For structure consistency, it would be recommended that the above code is changed
to look like 1 of 2 ways.

Option 1:
```Java
SomeBean[] myArr = new SomeBean[]{ new SomeBean(1), new SomeBean(2) };
JSONArray jArr = new JSONArray();
// these will not be wrapped
jArr.putAll(myArr);
// these will not be wrapped
jArr.putAll(new SomeBean[]{ new SomeBean(3), new SomeBean(4) });
// our jArr is now consistent.
```

Option 2:
```Java
SomeBean[] myArr = new SomeBean[]{ new SomeBean(1), new SomeBean(2) };
// these will be wrapped
JSONArray jArr = new JSONArray(myArr);
// these will be wrapped
jArr.putAll(new JSONArray(new SomeBean[]{ new SomeBean(3), new SomeBean(4) }));
// our jArr is now consistent.
```

**Unit Test Conventions**

Test filenames should consist of the name of the module being tested, with the suffix "Test". 
For example, <b>Cookie.java</b> is tested by <b>CookieTest.java</b>.

<b>The fundamental issues with JSON-Java testing are:</b><br>
* <b>JSONObjects</b> are unordered, making simple string comparison ineffective. 
* Comparisons via **equals()** is not currently supported. Neither <b>JSONArray</b> nor <b>JSONObject</b> override <b>hashCode()</b> or <b>equals()</b>, so comparison defaults to the <b>Object</b> equals(), which is not useful.
* Access to the <b>JSONArray</b> and <b>JSONObject</b> internal containers for comparison is not currently available.

<b>General issues with unit testing are:</b><br>
* Just writing tests to make coverage goals tends to result in poor tests. 
* Unit tests are a form of documentation - how a given method works is demonstrated by the test. So for a code reviewer or future developer looking at code a good test helps explain how a function is supposed to work according to the original author. This can be difficult if you are not the original developer.
*   It is difficult to evaluate unit tests in a vacuum. You also need to see the code being tested to understand if a test is good. 
* Without unit tests, it is hard to feel confident about the quality of the code, especially when fixing bugs or refactoring. Good tests prevent regressions and keep the intent of the code correct.
* If you have unit test results along with pull requests, the reviewer has an easier time understanding your code and determining if it works as intended.


# Files

**JSONObject.java**: The `JSONObject` can parse text from a `String` or a `JSONTokener`
to produce a map-like object. The object provides methods for manipulating its
contents, and for producing a JSON compliant object serialization.

**JSONArray.java**: The `JSONArray` can parse text from a String or a `JSONTokener`
to produce a vector-like object. The object provides methods for manipulating
its contents, and for producing a JSON compliant array serialization.

**JSONTokener.java**: The `JSONTokener` breaks a text into a sequence of individual
tokens. It can be constructed from a `String`, `Reader`, or `InputStream`. It also can 
parse text from a `String`, `Number`, `Boolean` or `null` like `"hello"`, `42`, `true`, 
`null` to produce a simple json object.

**JSONException.java**: The `JSONException` is the standard exception type thrown
by this package.

**JSONPointer.java**: Implementation of
[JSON Pointer (RFC 6901)](https://tools.ietf.org/html/rfc6901). Supports
JSON Pointers both in the form of string representation and URI fragment
representation.

**JSONPropertyIgnore.java**: Annotation class that can be used on Java Bean getter methods.
When used on a bean method that would normally be serialized into a `JSONObject`, it
overrides the getter-to-key-name logic and forces the property to be excluded from the
resulting `JSONObject`.

**JSONPropertyName.java**: Annotation class that can be used on Java Bean getter methods.
When used on a bean method that would normally be serialized into a `JSONObject`, it
overrides the getter-to-key-name logic and uses the value of the annotation. The Bean
processor will look through the class hierarchy. This means you can use the annotation on
a base class or interface and the value of the annotation will be used even if the getter
is overridden in a child class.   

**JSONString.java**: The `JSONString` interface requires a `toJSONString` method,
allowing an object to provide its own serialization.

**JSONStringer.java**: The `JSONStringer` provides a convenient facility for
building JSON strings.

**JSONWriter.java**: The `JSONWriter` provides a convenient facility for building
JSON text through a writer.


**CDL.java**: `CDL` provides support for converting between JSON and comma
delimited lists.

**Cookie.java**: `Cookie` provides support for converting between JSON and cookies.

**CookieList.java**: `CookieList` provides support for converting between JSON and
cookie lists.

**HTTP.java**: `HTTP` provides support for converting between JSON and HTTP headers.

**HTTPTokener.java**: `HTTPTokener` extends `JSONTokener` for parsing HTTP headers.

**XML.java**: `XML` provides support for converting between JSON and XML.

**JSONML.java**: `JSONML` provides support for converting between JSONML and XML.

**XMLTokener.java**: `XMLTokener` extends `JSONTokener` for parsing XML text.


# Release history:

JSON-java releases can be found by searching the Maven repository for groupId "org.json"
and artifactId "json". For example:
[https://search.maven.org/search?q=g:org.json%20AND%20a:json&core=gav](https://search.maven.org/search?q=g:org.json%20AND%20a:json&core=gav)

~~~
20201115    Recent commits and first release after project structure change

20200518    Recent commits and snapshot before project structure change

20190722    Recent commits

20180813    POM change to include Automatic-Module-Name (#431)

20180130    Recent commits

20171018    Checkpoint for recent commits.

20170516    Roll up recent commits.

20160810    Revert code that was breaking opt*() methods.

20160807    This release contains a bug in the JSONObject.opt*() and JSONArray.opt*() methods,
it is not recommended for use.
Java 1.6 compatability fixed, JSONArray.toList() and JSONObject.toMap(),
RFC4180 compatibility, JSONPointer, some exception fixes, optional XML type conversion.
Contains the latest code as of 7 Aug 2016

20160212    Java 1.6 compatibility, OSGi bundle. Contains the latest code as of 12 Feb 2016.

20151123    JSONObject and JSONArray initialization with generics. Contains the latest code as of 23 Nov 2015.

20150729    Checkpoint for Maven central repository release. Contains the latest code
as of 29 July 2015.
~~~
