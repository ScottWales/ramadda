/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
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

// $Id: StringUtil.java,v 1.53 2007/06/01 17:02:44 jeffmc Exp $


package org.ramadda.util;
import ucar.unidata.util.Misc;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


import java.text.StringCharacterIterator;

/**
 */

public class Json {

    public static final String MIMETYPE = "application/json";
    public static final String NULL = "null";
    public static final boolean DFLT_QUOTE = false;

    public static String map(String[] values) {
        return map(values, DFLT_QUOTE);
    }

    public static String map(List<String> values) {
        return map(values, DFLT_QUOTE);
    }

    public static String map(String[] values, boolean quoteValue) {
        return map((List<String>)Misc.toList(values), quoteValue);
    }

    public static String map(List<String> values, boolean quoteValue) {
        StringBuffer row = new StringBuffer();
        row.append("{");
        for(int i=0;i<values.size();i+=2) {
            if(i>0)  row.append(",\n");
            String name =  values.get(i);
            String value = values.get(i+1);
            row.append(attr(name, value, quoteValue));
        }
        row.append("}");
        return row.toString();
    }

    public static String list(String[] values) {
        return list(Misc.toList(values));
    }

    public static String list(List values) {
        return list(values, DFLT_QUOTE);
    }

    public static String list(List values, boolean quoteValue) {
        StringBuffer row = new StringBuffer();
        row.append("[");
        for(int i=0;i<values.size();i++) {
            if(i>0)  row.append(",\n");
            if(quoteValue) 
                row.append(quote(values.get(i).toString()));
            else
                row.append(values.get(i).toString());
        }
        row.append("]");
        return row.toString();
    }

    public static String quote(String s) {
        if(s==null) return NULL;
        return "\"" + s + "\"";
    }


    public static String getString(String s, boolean quote) {
        if(s==null) return NULL;
        if(quote) return quote(s);
        return s;
    }


    public static String attr(String name, String value) {
        return attr(name, value, DFLT_QUOTE);
    }


    public static String attr(String name, String value, boolean quoteValue) {
        return quote(name)+":" + getString(value, quoteValue);
    }

    public static void quoteAttr(List<String>items,String name, String value) {
        items.add(name);
        items.add(getString(value, true));
    }

    public static void attr(List<String>items,String name, String value) {
        items.add(name);
        items.add(getString(value, false));
    }


    public static String cleanAndQuote(String aText) {
        return quote(cleanString(aText));
    }


    /**
     * _more_
     *
     * @param aText _more_
     *
     * @return _more_
     */
    public static String cleanString(String aText) {
        if (!Utils.stringDefined(aText)) {
            return "";
        }
        final StringBuilder     result    = new StringBuilder();

        StringCharacterIterator iterator  = new StringCharacterIterator(aText);
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
