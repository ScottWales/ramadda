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

import netscape.javascript.JSObject;

import java.applet.*;

import java.awt.*;


/**
 * Class NS_JSConnect _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class NS_JSConnect extends JSConnect {

    /** _more_ */
    JSObject js;

    /**
     * _more_
     */
    public void init() {
        js = JSObject.getWindow(this);
        super.init();
    }


    /**
     * _more_
     *
     * @param s _more_
     */
    public void evalJS(String s) {
        try {
            System.err.println("Evaluating javascript: " + s + "\n");
            js.eval(s);
        } catch (Exception exc) {
            System.err.println("** Error evaluating javascript: " + s + "\n"
                               + exc);
        }
    }

    /**
     * _more_
     *
     * @param urlString _more_
     */
    public void loadUrl(String urlString) {
        urlString = urlString.trim();
        String[] args = { urlString };
        try {
            //      showUrl(urlString,null);
            //      js.eval("location="+urlString);
            js.call("loadUrl", args);
        } catch (Exception exc) {
            System.err.println("** Error id= " + myid + " : " + urlString
                               + "\n" + exc);
        }
    }


}
