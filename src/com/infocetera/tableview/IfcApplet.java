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

package com.infocetera.common;


import com.infocetera.util.GuiUtils;

import java.applet.*;

import java.awt.*;

import java.io.*;

import java.net.*;



/**
 * Class IfcApplet _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class IfcApplet extends Applet {

    /** _more_          */
    public static final GuiUtils GU = null;

    /** _more_          */
    public Container contents;

    /** _more_          */
    String myHost;

    /** _more_          */
    int myPort;


    /**
     * _more_
     */
    public IfcApplet() {}

    /**
     * _more_
     */
    public void init() {
        URL myUrl = getCodeBase();
        myHost = myUrl.getHost();
        myPort = myUrl.getPort();

        Component inner;

        try {
            inner = doMakeContents();
        } catch (Exception exc) {
            exc.printStackTrace();
            inner = new Label("Error:" + exc);
        }
        setLayout(new BorderLayout());
        contents = GU.inset(inner, 2, 2);
        add("Center", contents);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Component doMakeContents() {
        return null;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getUrlPrefix() {
        if (myPort < 0) {
            return "http://" + myHost;
        }
        return "http://" + myHost + ":" + myPort;

    }



    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public String getFullUrl(String url) {
        return getUrlPrefix() + url;

    }


    /**
     * _more_
     *
     * @param urlString _more_
     * @param which _more_
     */
    public void showUrl(String urlString, String which) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (Throwable exc1) {
            try {
                url = new URL(getFullUrl(urlString));
            } catch (Throwable exc2) {}
        }

        if (url != null) {
            if ((which != null) && !which.equals("")) {
                getAppletContext().showDocument(url, which);
            } else {
                getAppletContext().showDocument(url);
            }
        }
    }





}

