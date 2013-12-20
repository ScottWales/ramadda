/*
* Copyright 2008-2013 Geode Systems LLC
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.util;


import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;


import java.text.StringCharacterIterator;

import java.util.ArrayList;
import java.util.List;


/**
 * JSON Utility class
 */
public class Json {

    /** JSON MIME type */
    public static final String MIMETYPE = "application/json";

    /** the null string identifier */
    public static final String NULL = "null";

    /** default quote value */
    public static final boolean DFLT_QUOTE = false;

    /**
     * Create a JSON map
     *
     * @param values  key/value pairs { key1,value1,key2,value2 }
     *
     * @return  the map object { key1:value1, key2:value2 }
     */
    public static String map(String[] values) {
        return map(values, DFLT_QUOTE);
    }

    /**
     * Create a JSON map
     *
     * @param values  key/value pairs [ key1,value1,key2,value2 ]
     *
     * @return  the map object { key1:value1, key2:value2 }
     */
    public static String map(List<String> values) {
        return map(values, DFLT_QUOTE);
    }

    /**
     * Create a JSON map
     *
     * @param values  key/value pairs { key1,value1,key2,value2 }
     * @param quoteValue  true to quote the values
     *
     * @return  the map object { key1:value1, key2:value2 }
     */
    public static String map(String[] values, boolean quoteValue) {
        return map((List<String>) Misc.toList(values), quoteValue);
    }

    /**
     * Create a JSON map
     *
     * @param values  key/value pairs [ key1,value1,key2,value2 ]
     * @param quoteValue  true to quote the values
     *
     * @return  the map object { key1:value1, key2:value2 }
     */
    public static String map(List<String> values, boolean quoteValue) {
        StringBuffer row = new StringBuffer();
        row.append("{");
        for (int i = 0; i < values.size(); i += 2) {
            if (i > 0) {
                row.append(",\n");
            }
            String name  = values.get(i);
            String value = values.get(i + 1);
            row.append(attr(name, value, quoteValue));
        }
        row.append("}");

        return row.toString();
    }

    /**
     * Create a JSON list from the array of strings
     *
     * @param values  list of values { value1,value2,value3,value4 }
     *
     * @return  the values as a JSON array [ value1,value2,value3,value4 ]
     */
    public static String list(String[] values) {
        return list(Misc.toList(values));
    }

    /**
     * Create a JSON list from the array of strings
     *
     * @param values  list of values [ value1,value2,value3,value4 ]
     *
     * @return  the values as a JSON array [ value1,value2,value3,value4 ]
     */
    public static String list(List values) {
        return list(values, DFLT_QUOTE);
    }

    /**
     * Create a JSON list from the array of strings
     *
     * @param values  list of values [ value1,value2,value3,value4 ]
     * @param quoteValue  true to quote the values
     *
     * @return  the values as a JSON array [ value1,value2,value3,value4 ]
     */
    public static String list(List values, boolean quoteValue) {
        StringBuffer row = new StringBuffer();
        row.append("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                row.append(",\n");
            }
            if (quoteValue) {
                row.append(quote(values.get(i).toString()));
            } else {
                row.append(values.get(i).toString());
            }
        }
        row.append("]");

        return row.toString();
    }

    /**
     * Quote a string
     *
     * @param s the string
     *
     * @return  the quoted string
     */
    public static String quote(String s) {
        if (s == null) {
            return NULL;
        }

        return "\"" + s + "\"";
    }

    /**
     * Create a list of JSON object from a list of TwoFacedObjects
     *
     * @param values  the values
     *
     * @return  the list [ {id:id1,label:label1},{id:id2,label:label2} ]
     */
    public static String tfoList(List<TwoFacedObject> values) {
        return tfoList(values, "id", "label");
    }

    /**
     * Create a list of JSON object from a list of TwoFacedObjects
     *
     * @param values  the values
     * @param idKey   the key for the TwoFacedObject ID
     * @param labelKey   the key for the TwoFacedObject label
     *
     * @return  the list [ {id:id1,label:label1},{id:id2,label:label2} ]
     */
    public static String tfoList(List<TwoFacedObject> values, String idKey,
                                 String labelKey) {
        List<String> arrayVals = new ArrayList<String>();
        for (TwoFacedObject tfo : values) {
            List<String> mapValues = new ArrayList<String>();
            String       id        = TwoFacedObject.getIdString(tfo);
            String       label     = tfo.toString();
            mapValues.add(idKey);
            mapValues.add((id == null)
                          ? label
                          : id);
            mapValues.add(labelKey);
            mapValues.add(label);
            arrayVals.add(map(mapValues, true));
        }

        return list(arrayVals);
    }

    /**
     * Get a string
     *
     * @param s  the string
     * @param quote  true to quote
     *
     * @return the string
     */
    public static String getString(String s, boolean quote) {
        if (s == null) {
            return NULL;
        }
        if (quote) {
            return quote(s);
        }

        return s;
    }


    /**
     * Create a JSON object attribute
     *
     * @param name  the attribute name
     * @param value  the attribute value
     *
     * @return  the attribute as name:value
     */
    public static String attr(String name, String value) {
        return attr(name, value, DFLT_QUOTE);
    }

    /**
     * Create a JSON object attribute
     *
     * @param name  the attribute name
     * @param value  the attribute value
     * @param quoteValue true to quote the name and value
     *
     * @return  the attribute as name:value
     */
    public static String attr(String name, String value, boolean quoteValue) {
        return quote(name) + ":" + getString(value, quoteValue);
    }

    /**
     * quote the attribute value and add it to the list
     *
     * @param items the list of items
     * @param name  the attribute name
     * @param value the attribute value
     */
    public static void quoteAttr(List<String> items, String name,
                                 String value) {
        items.add(name);
        items.add(getString(value, true));
    }

    /**
     * Make an attribute and add it to the list
     *
     * @param items  the list of name/value pairs
     * @param name   the attribute name
     * @param value  the attribute value
     */
    public static void attr(List<String> items, String name, String value) {
        items.add(name);
        items.add(getString(value, false));
    }


    public static String formatNumber(double d) {
        if(Double.isNaN(d)) return "null";
        return ""+d;
    }


    /**
     * Clean and quote some text
     *
     * @param aText the text
     *
     * @return  the cleaned and quoted text
     */
    public static String cleanAndQuote(String aText) {
        return quote(cleanString(aText));
    }


    /**
     * Clean a string of illegal JSON characters
     *
     * @param aText  the string
     *
     * @return  the cleaned string
     */
    public static String cleanString(String aText) {
        if ( !Utils.stringDefined(aText)) {
            return "";
        }
        final StringBuilder     result    = new StringBuilder();

        StringCharacterIterator iterator  =
            new StringCharacterIterator(aText);
        char                    character = iterator.current();
        while (character != StringCharacterIterator.DONE) {
            if (character == '\"') {
                result.append("\\\"");
            } else if (character == '\\') {
                result.append("\\\\");
            } else if (character == '/') {
                result.append("\\/");
            } else if (character == '\b') {
                result.append("\\b");
            } else if (character == '\f') {
                result.append("\\f");
            } else if (character == '\n') {
                result.append("\\n");
            } else if (character == '\r') {
                result.append("\\r");
            } else if (character == '\t') {
                result.append("\\t");
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }

        return result.toString();
    }



}
