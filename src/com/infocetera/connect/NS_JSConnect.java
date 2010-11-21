/*
 * 
 * 
 * 
 * 
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

    /** _more_          */
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

