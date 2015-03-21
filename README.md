# JSON-Java-unit-test

Unit tests to validate the JSON-Java GitHub project code (https://github.com/douglascrockford/JSON-java).<br>

Test harness: http://junit.org<br>
Coverage: http://www.eclemma.org/<br>

Eclipse is the recommended development environment.
Run individual tests or <b>JunitTestSuite</b> using *EclEmma Coverage*, or execute the <b>TestRunner<b> application directly.<br>

Test filenames should consist of the name of the module being tested, with the suffix "Test". 
For example, <b>Cookie.java</b> is tested by <b>CookieTest.java</b>.
When adding a new unit test, don't forget to update <b>JunitTestSuite.java</b>.

The fundamental issues with JSON-Java testing are:
* <b>JSONObjects</b> are unordered, making simple string comparison ineffective. 
* Comparisons via **equals()** is not currently supported. Neither <b>JSONArray</b> nor <b>JSONObject</b> overrride <b>hashCode()</b> or <b>equals()</b>, so comparison defaults to the <b>Object</b> equals(), which is not useful.
* Access to the <b>JSONArray</b> and <b>JSONObject</b> internal containers for comparison is not currently available.
* <b>JSONObject</b> sometimes wraps entries in quotes, other times does not, complicating comparisons.

When you start working on a test, add the empty file to the repository and update the readme, so that others will know that test is taken.

A unit test is considered complete when the coverage is >= 90% as measured by EclEmma.

| Test file name  | Coverage | Comments |
| ------------- | ------------- | ---- |
| Total coverage | 19% | | | 
| | | | 
| CDLTest.java | 94.8% | Relies too much on string tests, needs to be reworked  |
| CookieTest.java  | 97.5%   | Completed |
| CookieList.java |0% | |
| HTTP.java | 0%| | 
| HTTPTokener.java |0% | | 
| JSONArray.java |15.3% | | 
|JSONException.java | 26.7% | |
| JSONML.java | 0%| | 
| JSONObject.Null | 8.9% | | | 
| JSONStringer.java | 0%| | 
| JSONTokener.java |59.8% | | 
| JSONWriter.java | 0% | | 
| PropertyTest.java  | 94.8%  | Completed |
| XMLTest.java | 0% | Just started - stleary |
| XMLTokener.java| 0%| | 


