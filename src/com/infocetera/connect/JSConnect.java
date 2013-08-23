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

package com.infocetera.connect;


import com.infocetera.util.*;

import netscape.javascript.JSObject;


import java.applet.*;

import java.awt.*;

import java.util.StringTokenizer;


/**
 * Class JSConnect _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class JSConnect extends SocketApplet {

    /** _more_ */
    String directory = "";

    /** _more_ */
    String user = "NO_USER";

    /** _more_ */
    String markupid = null;

    /** _more_ */
    String sessionId = "BADID";


    /** _more_ */
    JSObject js;

    /**
     * _more_
     */
    public JSConnect() {}


    /**
     * _more_
     */
    public void init() {
        super.init();
        shouldTokenizeMsg = false;
        js                = JSObject.getWindow(this);
        directory         = "directory";
        try {
            directory = getParameter("directory");
            sessionId = getParameter("sessionid");
            myid      = getParameter("id");
            user      = getParameter("user");
            markupid  = getParameter("markupid");
        } catch (Exception exc) {}
    }

    /**
     * _more_
     *
     * @param msg _more_
     */
    public void handleMessage(String msg) {
        processMessage(msg);
    }


    /**
     * _more_
     *
     * @param msg _more_
     */
    public void write2(String msg) {
        System.err.println("OK");
    }


    /**
     * _more_
     *
     * @param msg _more_
     */
    public void processMessage(String msg) {
        System.err.println("JSConnect:(" + myid + ") " + msg);
        if (msg.startsWith("JS:")) {
            StringTokenizer st = new StringTokenizer(msg, "\n");
            while (st.hasMoreTokens()) {
                String sub = st.nextToken();
                if (sub.startsWith("JS:")) {
                    sub = sub.substring(3).trim();
                }
                evalJS(sub);
            }
        } else if (msg.startsWith("URL")) {
            String urlString = msg.substring(3);
            urlString = urlString.replace('\n', ';');
            try {
                loadUrl(urlString);
                //      evalJS("loadUrl('"+urlString+"');");
            } catch (Exception exc) {
                System.err.println("Error: " + exc);
            }
        }
    }




    /**
     * _more_
     */
    public void initConnection() {
        super.initConnection();
        System.err.println("ID: " + sessionId);

        write("START <" + directory + "><" + user + "><" + sessionId + ">");
        write("ID " + myid);
        if (markupid != null) {
            System.err.println("Writing get ");
            write("GET " + markupid);
        }
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
