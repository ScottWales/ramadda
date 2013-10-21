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

// $Id: StringUtil.java,v 1.53 2007/06/01 17:02:44 jeffmc Exp $


package org.ramadda.util;


/**
 */

public class JQuery {

    /**
     * _more_
     *
     * @param selector _more_
     *
     * @return _more_
     */
    public static String id(String selector) {
        return "#" + selector;
    }


    /**
     * _more_
     *
     * @param selector _more_
     *
     * @return _more_
     */
    public static String select(String selector) {
        return "$(" + HtmlUtils.squote(selector) + ")";
    }


    public static String selectId(String id) {
        return select(id(id));
    }



    /**
     * _more_
     *
     * @param selector _more_
     * @param func _more_
     * @param code _more_
     *
     * @return _more_
     */
    public static String call(String selector, String func, String code) {
        return select(selector) + "." + func + "(function(event) {" + code
               + "});\n";
    }

    /**
     * _more_
     *
     * @param selector _more_
     * @param code _more_
     *
     * @return _more_
     */
    public static String submit(String selector, String code) {
        return call(selector, "submit", code);
    }

    /**
     * _more_
     *
     * @param selector _more_
     * @param code _more_
     *
     * @return _more_
     */
    public static String change(String selector, String code) {
        return call(selector, "change", code);
    }

    /**
     * _more_
     *
     * @param selector _more_
     * @param code _more_
     *
     * @return _more_
     */
    public static String click(String selector, String code) {
        return call(selector, "click", code);
    }

    /**
     * _more_
     *
     * @param label _more_
     * @param id _more_
     * @param js _more_
     * @param code _more_
     *
     * @return _more_
     */
    public static String button(String label, String id, StringBuffer js,
                                String code) {
        String html = HtmlUtils.tag("button", HtmlUtils.id(id), label);
        js.append(
            JQuery.select(JQuery.id(id))
            + ".button().click(function(event){event.preventDefault();\n"
            + code + "\n});\n");

        return html;
    }

    public static String makeButton(String label, String id, StringBuffer js, 
                                    String code) {
        String html = HtmlUtils.tag("button", HtmlUtils.id(id), label);
        js.append(
            JQuery.select(JQuery.id(id))
            + ".button().click(function(event){\n"
            + code + "\n});\n");

        return html;
    }

    public static String buttonize(String selector) {
        return  JQuery.select(selector)
            + ".button().click(function(event){});\n";
    }


    public static String ready(String js) {
        return "$(document).ready(function(){\n" + js +"\n});\n";

    }

}
