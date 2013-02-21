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

/**
 */

public class JQuery  {

    public static String id(String selector) {
        return "#"+ selector;
    }


    public static String select(String selector) {
        return "$(" + HtmlUtils.squote(selector) + ")";
    }



    public static String call(String selector, String func, String code) {
        return select(selector) +"." + func +"(function(event) {" + code +"});\n";
    }

    public static String submit(String selector,  String code) {
        return call(selector,"submit", code);
    }

    public static String change(String selector,  String code) {
        return call(selector,"change", code);
    }

    public static String click(String selector,  String code) {
        return call(selector,"click", code);
    }

    public static String button(String label, String id, StringBuffer js, String code ) {
        String html = HtmlUtils.tag("button", HtmlUtils.id(id),label);
        js.append (JQuery.select(JQuery.id(id)) +".button().click(function(event){event.preventDefault();\n" + code +"\n});\n");
        return html;
    }
}
