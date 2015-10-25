package org.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.StringWriter;

/*
Copyright (c) 2002 JSON.org

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

/**
 * Test class. This file is not formally a member of the org.json library.
 * It is just a test tool.
 * 
 * Issue: JSONObject does not specify the ordering of keys, so simple-minded
 * comparisons of .toString to a string literal are likely to fail.
 *
 * @author JSON.org
 * @version 2011-02-09
 */
public class Test {
	
    public void testXML() throws Exception {
        JSONObject jsonobject;
        String string;

        jsonobject = XML.toJSONObject("<![CDATA[This is a collection of test patterns and examples for org.json.]]>  Ignore the stuff past the end.  ");
        
        string = "<test><blank></blank><empty/></test>";
        jsonobject = XML.toJSONObject(string);
        
    }

    public void testNull() throws Exception {
        JSONObject jsonobject;

        jsonobject = new JSONObject("{\"message\":\"null\"}");
        

        jsonobject = new JSONObject("{\"message\":null}");
        
    }

    public void testJSON() throws Exception {
    	double       eps = 2.220446049250313e-16;
        Iterator     iterator;
        JSONArray    jsonarray;
        JSONObject   jsonobject;
        JSONStringer jsonstringer;
        Object       object;
        String       string;

        Beany beanie = new Beany("A beany object", 42, true);

        string = "[0.1]";
        jsonarray = new JSONArray(string);
        

        jsonobject = new JSONObject();
        object = null;
        jsonobject.put("booga", object);
        jsonobject.put("wooga", JSONObject.NULL);
        

        jsonobject = new JSONObject();
        jsonobject.increment("two");
        jsonobject.increment("two");
        

        string = "{     \"list of lists\" : [         [1, 2, 3],         [4, 5, 6],     ] }";
        jsonobject = new JSONObject(string);
       

        string = "<recipe name=\"bread\" prep_time=\"5 mins\" cook_time=\"3 hours\"> <title>Basic bread</title> <ingredient amount=\"8\" unit=\"dL\">Flour</ingredient> <ingredient amount=\"10\" unit=\"grams\">Yeast</ingredient> <ingredient amount=\"4\" unit=\"dL\" state=\"warm\">Water</ingredient> <ingredient amount=\"1\" unit=\"teaspoon\">Salt</ingredient> <instructions> <step>Mix all ingredients together.</step> <step>Knead thoroughly.</step> <step>Cover with a cloth, and leave for one hour in warm room.</step> <step>Knead again.</step> <step>Place in a bread baking tin.</step> <step>Cover with a cloth, and leave for one hour in warm room.</step> <step>Bake in the oven at 180(degrees)C for 30 minutes.</step> </instructions> </recipe> ";
        jsonobject = XML.toJSONObject(string);
        
        jsonobject = JSONML.toJSONObject(string);
        
        string = "<div id=\"demo\" class=\"JSONML\"><p>JSONML is a transformation between <b>JSON</b> and <b>XML</b> that preserves ordering of document features.</p><p>JSONML can work with JSON arrays or JSON objects.</p><p>Three<br/>little<br/>words</p></div>";
        jsonobject = JSONML.toJSONObject(string);
        
        jsonarray = JSONML.toJSONArray(string);
        
        string = "<person created=\"2006-11-11T19:23\" modified=\"2006-12-31T23:59\">\n <firstName>Robert</firstName>\n <lastName>Smith</lastName>\n <address type=\"home\">\n <street>12345 Sixth Ave</street>\n <city>Anytown</city>\n <state>CA</state>\n <postalCode>98765-4321</postalCode>\n </address>\n </person>";
        jsonobject = XML.toJSONObject(string);
        
        jsonobject = new JSONObject(beanie);
        
        string = "{ \"entity\": { \"imageURL\": \"\", \"name\": \"IXXXXXXXXXXXXX\", \"id\": 12336, \"ratingCount\": null, \"averageRating\": null } }";
        jsonobject = new JSONObject(string);
        
        jsonstringer = new JSONStringer();
        string = jsonstringer
                .object()
                .key("single")
                .value("MARIE HAA'S")
                .key("Johnny")
                .value("MARIE HAA\\'S")
                .key("foo")
                .value("bar")
                .key("baz")
                .array()
                .object()
                .key("quux")
                .value("Thanks, Josh!")
                .endObject()
                .endArray()
                .key("obj keys")
                .value(JSONObject.getNames(beanie))
                .endObject()
                .toString();
        
        jsonstringer = new JSONStringer();
        jsonstringer.array();
        jsonstringer.value(1);
        jsonstringer.array();
        jsonstringer.value(null);
        jsonstringer.array();
        jsonstringer.object();
        jsonstringer.key("empty-array").array().endArray();
        jsonstringer.key("answer").value(42);
        jsonstringer.key("null").value(null);
        jsonstringer.key("false").value(false);
        jsonstringer.key("true").value(true);
        jsonstringer.key("big").value(123456789e+88);
        jsonstringer.key("small").value(123456789e-88);
        jsonstringer.key("empty-object").object().endObject();
        jsonstringer.key("long");
        jsonstringer.value(9223372036854775807L);
        jsonstringer.endObject();
        jsonstringer.value("two");
        jsonstringer.endArray();
        jsonstringer.value(true);
        jsonstringer.endArray();
        jsonstringer.value(98.6);
        jsonstringer.value(-100.0);
        jsonstringer.object();
        jsonstringer.endObject();
        jsonstringer.object();
        jsonstringer.key("one");
        jsonstringer.value(1.00);
        jsonstringer.endObject();
        jsonstringer.value(beanie);
        jsonstringer.endArray();
        
        int ar[] = {1, 2, 3};
        JSONArray ja = new JSONArray(ar);
        
        String sa[] = {"aString", "aNumber", "aBoolean"};
        jsonobject = new JSONObject(beanie, sa);
        jsonobject.put("Testing JSONString interface", beanie);
        
        jsonobject = new JSONObject("{slashes: '///', closetag: '</script>', backslash:'\\\\', ei: {quotes: '\"\\''},eo: {a: '\"quoted\"', b:\"don't\"}, quotes: [\"'\", '\"']}");
        
        jsonobject = new JSONObject(
                "{foo: [true, false,9876543210,    0.0, 1.00000001,  1.000000000001, 1.00000000000000001," +
                        " .00000000000000001, 2.00, 0.1, 2e100, -32,[],{}, \"string\"], " +
                        "  to   : null, op : 'Good'," +
                        "ten:10} postfix comment");
        jsonobject.put("String", "98.6");
        jsonobject.put("JSONObject", new JSONObject());
        jsonobject.put("JSONArray", new JSONArray());
        jsonobject.put("int", 57);
        jsonobject.put("double", 123456789012345678901234567890.);
        jsonobject.put("true", true);
        jsonobject.put("false", false);
        jsonobject.put("null", JSONObject.NULL);
        jsonobject.put("bool", "true");
        jsonobject.put("zero", -0.0);
        jsonobject.put("\\u2028", "\u2028");
        jsonobject.put("\\u2029", "\u2029");
        jsonarray = jsonobject.getJSONArray("foo");
        jsonarray.put(666);
        jsonarray.put(2001.99);
        jsonarray.put("so \"fine\".");
        jsonarray.put("so <fine>.");
        jsonarray.put(true);
        jsonarray.put(false);
        jsonarray.put(new JSONArray());
        jsonarray.put(new JSONObject());
        jsonobject.put("keys", JSONObject.getNames(jsonobject));        
        
        string = "<xml one = 1 two=' \"2\" '><five></five>First \u0009&lt;content&gt;<five></five> This is \"content\". <three>  3  </three>JSON does not preserve the sequencing of elements and contents.<three>  III  </three>  <three>  T H R E E</three><four/>Content text is an implied structure in XML. <six content=\"6\"/>JSON does not have implied structure:<seven>7</seven>everything is explicit.<![CDATA[CDATA blocks<are><supported>!]]></xml>";
        jsonobject = XML.toJSONObject(string);
        
        ja = JSONML.toJSONArray(string);
        
        string = "<xml do='0'>uno<a re='1' mi='2'>dos<b fa='3'/>tres<c>true</c>quatro</a>cinqo<d>seis<e/></d></xml>";
        ja = JSONML.toJSONArray(string);
        
        string = "<mapping><empty/>   <class name = \"Customer\">      <field name = \"ID\" type = \"string\">         <bind-xml name=\"ID\" node=\"attribute\"/>      </field>      <field name = \"FirstName\" type = \"FirstName\"/>      <field name = \"MI\" type = \"MI\"/>      <field name = \"LastName\" type = \"LastName\"/>   </class>   <class name = \"FirstName\">      <field name = \"text\">         <bind-xml name = \"text\" node = \"text\"/>      </field>   </class>   <class name = \"MI\">      <field name = \"text\">         <bind-xml name = \"text\" node = \"text\"/>      </field>   </class>   <class name = \"LastName\">      <field name = \"text\">         <bind-xml name = \"text\" node = \"text\"/>      </field>   </class></mapping>";
        jsonobject = XML.toJSONObject(string);

        
        jsonobject = XML.toJSONObject("<?xml version=\"1.0\" ?><Book Author=\"Anonymous\"><Title>Sample Book</Title><Chapter id=\"1\">This is chapter 1. It is not very long or interesting.</Chapter><Chapter id=\"2\">This is chapter 2. Although it is longer than chapter 1, it is not any more interesting.</Chapter></Book>");
        
        jsonobject = XML.toJSONObject("<!DOCTYPE bCard 'http://www.cs.caltech.edu/~adam/schemas/bCard'><bCard><?xml default bCard        firstname = ''        lastname  = '' company   = '' email = '' homepage  = ''?><bCard        firstname = 'Rohit'        lastname  = 'Khare'        company   = 'MCI'        email     = 'khare@mci.net'        homepage  = 'http://pest.w3.org/'/><bCard        firstname = 'Adam'        lastname  = 'Rifkin'        company   = 'Caltech Infospheres Project'        email     = 'adam@cs.caltech.edu'        homepage  = 'http://www.cs.caltech.edu/~adam/'/></bCard>");
        
        jsonobject = XML.toJSONObject("<?xml version=\"1.0\"?><customer>    <firstName>        <text>Fred</text>    </firstName>    <ID>fbs0001</ID>    <lastName> <text>Scerbo</text>    </lastName>    <MI>        <text>B</text>    </MI></customer>");
        
        jsonobject = XML.toJSONObject("<!ENTITY tp-address PUBLIC '-//ABC University::Special Collections Library//TEXT (titlepage: name and address)//EN' 'tpspcoll.sgm'><list type='simple'><head>Repository Address </head><item>Special Collections Library</item><item>ABC University</item><item>Main Library, 40 Circle Drive</item><item>Ourtown, Pennsylvania</item><item>17654 USA</item></list>");
        
        jsonobject = XML.toJSONObject("<test intertag zero=0 status=ok><empty/>deluxe<blip sweet=true>&amp;&quot;toot&quot;&toot;&#x41;</blip><x>eks</x><w>bonus</w><w>bonus2</w></test>");
        
        jsonobject = HTTP.toJSONObject("GET / HTTP/1.0\nAccept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/vnd.ms-powerpoint, application/vnd.ms-excel, application/msword, */*\nAccept-Language: en-us\nUser-Agent: Mozilla/4.0 (compatible; MSIE 5.5; Windows 98; Win 9x 4.90; T312461; Q312461)\nHost: www.nokko.com\nConnection: keep-alive\nAccept-encoding: gzip, deflate\n");
        
        jsonobject = HTTP.toJSONObject("HTTP/1.1 200 Oki Doki\nDate: Sun, 26 May 2002 17:38:52 GMT\nServer: Apache/1.3.23 (Unix) mod_perl/1.26\nKeep-Alive: timeout=15, max=100\nConnection: Keep-Alive\nTransfer-Encoding: chunked\nContent-Type: text/html\n");
        
        jsonobject = new JSONObject("{nix: null, nux: false, null: 'null', 'Request-URI': '/', Method: 'GET', 'HTTP-Version': 'HTTP/1.0'}");
        
        jsonobject = XML.toJSONObject("<?xml version='1.0' encoding='UTF-8'?>" + "\n\n" + "<SOAP-ENV:Envelope" +
                " xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                " xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\"" +
                " xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\">" +
                "<SOAP-ENV:Body><ns1:doGoogleSearch" +
                " xmlns:ns1=\"urn:GoogleSearch\"" +
                " SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<key xsi:type=\"xsd:string\">GOOGLEKEY</key> <q" +
                " xsi:type=\"xsd:string\">'+search+'</q> <start" +
                " xsi:type=\"xsd:int\">0</start> <maxResults" +
                " xsi:type=\"xsd:int\">10</maxResults> <filter" +
                " xsi:type=\"xsd:boolean\">true</filter> <restrict" +
                " xsi:type=\"xsd:string\"></restrict> <safeSearch" +
                " xsi:type=\"xsd:boolean\">false</safeSearch> <lr" +
                " xsi:type=\"xsd:string\"></lr> <ie" +
                " xsi:type=\"xsd:string\">latin1</ie> <oe" +
                " xsi:type=\"xsd:string\">latin1</oe>" +
                "</ns1:doGoogleSearch>" +
                "</SOAP-ENV:Body></SOAP-ENV:Envelope>");

        
        jsonobject = new JSONObject("{Envelope: {Body: {\"ns1:doGoogleSearch\": {oe: \"latin1\", filter: true, q: \"'+search+'\", key: \"GOOGLEKEY\", maxResults: 10, \"SOAP-ENV:encodingStyle\": \"http://schemas.xmlsoap.org/soap/encoding/\", start: 0, ie: \"latin1\", safeSearch:false, \"xmlns:ns1\": \"urn:GoogleSearch\"}}}}");
        
        jsonobject = CookieList.toJSONObject("  f%oo = b+l=ah  ; o;n%40e = t.wo ");
        
        jsonobject = Cookie.toJSONObject("f%oo=blah; secure ;expires = April 24, 2002");
        
        jsonobject = new JSONObject("{script: 'It is not allowed in HTML to send a close script tag in a string<script>because it confuses browsers</script>so we insert a backslash before the /'}");
        
        JSONTokener jsontokener = new JSONTokener("{op:'test', to:'session', pre:1}{op:'test', to:'session', pre:2}");
        jsonobject = new JSONObject(jsontokener);
        
        int i = jsontokener.skipTo('{');
        
        jsonobject = new JSONObject(jsontokener);
        
        jsonarray = CDL.toJSONArray("Comma delimited list test, '\"Strip\"Quotes', 'quote, comma', No quotes, 'Single Quotes', \"Double Quotes\"\n1,'2',\"3\"\n,'It is \"good,\"', \"It works.\"\n\n");

        string = CDL.toString(jsonarray);
        
        jsonarray = CDL.toJSONArray(string);
        
        jsonarray = new JSONArray(" [\"<escape>\", next is an implied null , , ok,] ");
        
        jsonobject = new JSONObject("{ fun => with non-standard forms ; forgiving => This package can be used to parse formats that are similar to but not stricting conforming to JSON; why=To make it easier to migrate existing data to JSON,one = [[1.00]]; uno=[[{1=>1}]];'+':+6e66 ;pluses=+++;empty = '' , 'double':0.666,true: TRUE, false: FALSE, null=NULL;[true] = [[!,@;*]]; string=>  o. k. ; \r oct=0666; hex=0x666; dec=666; o=0999; noh=0x0x}");
       
        jsonobject = new JSONObject(jsonobject, new String[]{"dec", "oct", "hex", "missing"});
        
        jsonobject = new JSONObject("{string: \"98.6\", long: 2147483648, int: 2147483647, longer: 9223372036854775807, double: 9223372036854775808}");
        
        // getInt
        
        try {
            jsonobject.getInt("double");
            
        } catch (JSONException expected) {
        }
        try {
            jsonobject.getInt("string");
            
        } catch (JSONException expected) {
        }

        // getLong
        
        try {
            jsonobject.getLong("double");
            
        } catch (JSONException expected) {
        }
        try {
            jsonobject.getLong("string");
            
        } catch (JSONException expected) {
        }

        // getDouble
        
        jsonobject.put("good sized", 9223372036854775807L);
       
        jsonarray = new JSONArray("[2147483647, 2147483648, 9223372036854775807, 9223372036854775808]");
        
        List expectedKeys = new ArrayList(6);
        expectedKeys.add("int");
        expectedKeys.add("string");
        expectedKeys.add("longer");
        expectedKeys.add("good sized");
        expectedKeys.add("double");
        expectedKeys.add("long");

        iterator = jsonobject.keys();
        while (iterator.hasNext()) {
            string = (String) iterator.next();
            
        }
        
        // accumulate
        jsonobject = new JSONObject();
        jsonobject.accumulate("stooge", "Curly");
        jsonobject.accumulate("stooge", "Larry");
        jsonobject.accumulate("stooge", "Moe");
        jsonarray = jsonobject.getJSONArray("stooge");
        jsonarray.put(5, "Shemp");
        
        // write
        
        string = "<xml empty><a></a><a>1</a><a>22</a><a>333</a></xml>";
        jsonobject = XML.toJSONObject(string);
        
        string = "<book><chapter>Content of the first chapter</chapter><chapter>Content of the second chapter      <chapter>Content of the first subchapter</chapter>      <chapter>Content of the second subchapter</chapter></chapter><chapter>Third Chapter</chapter></book>";
        jsonobject = XML.toJSONObject(string);
        
        jsonarray = JSONML.toJSONArray(string);
       
        Collection collection = null;
        Map map = null;

        jsonobject = new JSONObject(map);
        jsonarray = new JSONArray(collection);
        jsonobject.append("stooge", "Joe DeRita");
        jsonobject.append("stooge", "Shemp");
        jsonobject.accumulate("stooges", "Curly");
        jsonobject.accumulate("stooges", "Larry");
        jsonobject.accumulate("stooges", "Moe");
        jsonobject.accumulate("stoogearray", jsonobject.get("stooges"));
        jsonobject.put("map", map);
        jsonobject.put("collection", collection);
        jsonobject.put("array", jsonarray);
        jsonarray.put(map);
        jsonarray.put(collection);
        
        string = "{plist=Apple; AnimalSmells = { pig = piggish; lamb = lambish; worm = wormy; }; AnimalSounds = { pig = oink; lamb = baa; worm = baa;  Lisa = \"Why is the worm talking like a lamb?\" } ; AnimalColors = { pig = pink; lamb = black; worm = pink; } } ";
        jsonobject = new JSONObject(string);
       
        string = " [\"San Francisco\", \"New York\", \"Seoul\", \"London\", \"Seattle\", \"Shanghai\"]";
        jsonarray = new JSONArray(string);
        
        string = "<a ichi='1' ni='2'><b>The content of b</b> and <c san='3'>The content of c</c><d>do</d><e></e><d>re</d><f/><d>mi</d></a>";
        jsonobject = XML.toJSONObject(string);
        
        ja = JSONML.toJSONArray(string);
        
        string = "<Root><MsgType type=\"node\"><BatchType type=\"string\">111111111111111</BatchType></MsgType></Root>";
        jsonobject = JSONML.toJSONObject(string);
        
        ja = JSONML.toJSONArray(string);
        
    }

    public void testExceptions() throws Exception {
        JSONArray jsonarray = null;
        JSONObject jsonobject;
        String string;

        try {
            jsonarray = new JSONArray("[\n\r\n\r}");
            System.out.println(jsonarray.toString());
            
        } catch (JSONException jsone) {
            
        }

        try {
            jsonarray = new JSONArray("<\n\r\n\r      ");
            System.out.println(jsonarray.toString());
            
        } catch (JSONException jsone) {
            
        }

        try {
            jsonarray = new JSONArray();
            jsonarray.put(Double.NEGATIVE_INFINITY);
            jsonarray.put(Double.NaN);
            System.out.println(jsonarray.toString());
            
        } catch (JSONException jsone) {
            
        }

        jsonobject = new JSONObject();
        try {
            System.out.println(jsonobject.getDouble("stooge"));
            
        } catch (JSONException jsone) {
            
        }

        try {
            System.out.println(jsonobject.getDouble("howard"));
            
        } catch (JSONException jsone) {
            
        }

        try {
            System.out.println(jsonobject.put(null, "howard"));
            
        } catch (JSONException jsone) {
            
        }

        try {
            System.out.println(jsonarray.getDouble(0));
            
        } catch (JSONException jsone) {
            
        }

        try {
            System.out.println(jsonarray.get(-1));
            
        } catch (JSONException jsone) {
            
        }

        try {
            System.out.println(jsonarray.put(Double.NaN));
            
        } catch (JSONException jsone) {
            
        }

        try {
            jsonobject = XML.toJSONObject("<a><b>    ");
            
        } catch (JSONException jsone) {
            
        }

        try {
            jsonobject = XML.toJSONObject("<a></b>    ");
            
        } catch (JSONException jsone) {
            
        }

        try {
            jsonobject = XML.toJSONObject("<a></a    ");
            
        } catch (JSONException jsone) {
            
        }

        try {
            jsonarray = new JSONArray(new Object());
            System.out.println(jsonarray.toString());
            
        } catch (JSONException jsone) {
            
        }

        try {
            string = "[)";
            jsonarray = new JSONArray(string);
            System.out.println(jsonarray.toString());
            
        } catch (JSONException jsone) {
            
        }

        try {
            string = "<xml";
            jsonarray = JSONML.toJSONArray(string);
            System.out.println(jsonarray.toString(4));
            
        } catch (JSONException jsone) {
            
        }

        try {
            string = "<right></wrong>";
            jsonarray = JSONML.toJSONArray(string);
            System.out.println(jsonarray.toString(4));
            
        } catch (JSONException jsone) {
            
        }

        try {
            string = "{\"koda\": true, \"koda\": true}";
            jsonobject = new JSONObject(string);
            System.out.println(jsonobject.toString(4));
            
        } catch (JSONException jsone) {
            
        }

        try {
            JSONStringer jj = new JSONStringer();
            string = jj
                    .object()
                    .key("bosanda")
                    .value("MARIE HAA'S")
                    .key("bosanda")
                    .value("MARIE HAA\\'S")
                    .endObject()
                    .toString();
            System.out.println(jsonobject.toString(4));
            
        } catch (JSONException jsone) {
            
        }
    }

    /**
     * Beany is a typical class that implements JSONString. It also
     * provides some beany methods that can be used to
     * construct a JSONObject. It also demonstrates constructing
     * a JSONObject with an array of names.
     */
    class Beany implements JSONString {
        public String aString;
        public double aNumber;
        public boolean aBoolean;

        public Beany(String string, double d, boolean b) {
            this.aString = string;
            this.aNumber = d;
            this.aBoolean = b;
        }

        public double getNumber() {
            return this.aNumber;
        }

        public String getString() {
            return this.aString;
        }

        public boolean isBoolean() {
            return this.aBoolean;
        }

        public String getBENT() {
            return "All uppercase key";
        }

        public String getX() {
            return "x";
        }

        public String toJSONString() {
            return "{" + JSONObject.quote(this.aString) + ":" +
                    JSONObject.doubleToString(this.aNumber) + "}";
        }

        public String toString() {
            return this.getString() + " " + this.getNumber() + " " +
                    this.isBoolean() + "." + this.getBENT() + " " + this.getX();
        }
    }
}