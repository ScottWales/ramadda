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

/**
 * (C) 1999-2002  WTS Systems, L.L.C.
 *   All rights reserved
 */



package com.infocetera.util;


import java.applet.*;


import java.awt.*;

import java.io.*;

import java.net.*;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;


/**
 * Class IfcApplet _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class IfcApplet extends Applet {

    /** _more_ */
    public static IfcApplet ifcApplet;


    /** _more_ */
    public static boolean debug=false;

    /** _more_ */
    static Image errorImage;

    /** _more_ */
    protected Hashtable properties = new Hashtable();

    /** _more_ */
    static Hashtable pathToImage = new Hashtable();

    /** _more_ */
    static Hashtable imageToPath = new Hashtable();

    /** _more_ */
    URL baseUrl;

    /** _more_ */
    String myHost;

    /** _more_ */
    int httpPort;

    /**
     * _more_
     */
    public IfcApplet() {
        ifcApplet = this;
    }


    /**
     * _more_
     */
    public void init() {
        baseUrl  = getDocumentBase();
        myHost   = baseUrl.getHost();
        httpPort = baseUrl.getPort();
        if (httpPort < 0) {
            httpPort = 80;
        }
        debug = getParameter("debug", false);
        //        debug = true;
        debug("IfcApplet.init: base url=" + baseUrl);
        initInner();
    }

    /**
     * _more_
     */
    public void initInner() {}

    /**
     * _more_
     *
     * @param p _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean getParameter(String p, boolean dflt) {
        String v = getParameter(p);
        return ((v == null)
                ? dflt
                : new Boolean(v).booleanValue());
    }

    /**
     * _more_
     *
     * @param p _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getParameter(String p, String dflt) {
        String v = getParameter(p);
        return ((v == null)
                ? dflt
                : v);
    }

    static JTextArea debugText;

    /**
     * _more_
     *
     * @param msg _more_
     */
    public static void debug(String msg) {
        if (debug) {
            if(debugText==null && false) {
                debugText = new JTextArea("",100,50);
                JFrame f = new JFrame("debug");
                f.getContentPane().add(new JScrollPane(debugText));
                f.pack();
                f.show();
            }

            if(debugText!=null) {
                debugText.setText(debugText.getText()+"\n" + msg);
            }
            System.err.println(msg);
        }
    }


    /**
     * _more_
     *
     * @param l1 _more_
     * @param exc _more_
     */
    public static void errorMsg(String l1, Throwable exc) {
        errorMsg(l1 + " " + exc);
        exc.printStackTrace();
    }


    /**
     * _more_
     *
     * @param l1 _more_
     */
    public static void errorMsg(String l1) {
        if (l1 == null) {
            return;
        }
        System.err.println(l1);
        if (errorImage == null) {
            errorImage =
                GuiUtils.getImageResource("/com/infocetera/images/error.gif",
                                          IfcApplet.class);
        }
        message(l1, errorImage);
    }

    /**
     * _more_
     *
     * @param l1 _more_
     */
    public static void message(String l1) {
        message(l1, null);
    }

    /**
     * _more_
     *
     * @param l1 _more_
     * @param image _more_
     */
    public static void message(String l1, Image image) {
        Component errorButton = null;

        if (image != null) {
            errorButton = new ImageButton(image, null, null,
                                          new Dimension(40, 40), false,
                                          false);
        }

        Component       theMessage;
        StringTokenizer st       = new StringTokenizer(l1, "\n");
        int             maxWidth = 50;
        int             rows     = 0;
        while (st.hasMoreTokens()) {
            int l = st.nextToken().length();
            if (l > maxWidth) {
                maxWidth = l;
            }
            rows++;
        }

        if (rows > 1) {
            TextArea t = new TextArea(l1, rows, maxWidth);
            t.setEditable(false);
            t.setBackground(Color.lightGray);
            theMessage = t;
        } else {
            theMessage = new Label(l1);
        }

        GuiUtils.tmpInsets = new Insets(4, 4, 4, 4);
        JPanel left = GuiUtils.topCenterBottom(new Label("           "),
                          ((errorButton != null)
                           ? (Component) errorButton
                           : (Component) new Label("  ")), null);

        JPanel outer = GuiUtils.leftCenterRight(left, theMessage, null);
        ifcApplet.show(new OkCancelDialog(null, outer, true, true));
    }


    /**
     *  Utility to create an image from a url or an internal resource
     *
     * @param imagePath _more_
     *
     * @return _more_
     */
    public static Image getImage(String imagePath) {
        if (imagePath == null) {
            return null;
        }
        try {
            imagePath = imagePath.trim();
            Image image = (Image) pathToImage.get(imagePath);
            if (image != null) {
                return image;
            }
            if (imagePath.startsWith("resource:")) {
                image = GuiUtils.getImageResource(imagePath.substring(9),
                        IfcApplet.class);
            } else {
                URL url = new URL(ifcApplet.getFullUrl(imagePath));
                byte[] bytes =
                    GuiUtils.readResource(ifcApplet.getFullUrl(imagePath),
                                          IfcApplet.class, true);
                image = Toolkit.getDefaultToolkit().createImage(bytes);
                //          image =  ifcApplet.getImage (url) ;
            }
            if (image != null) {
                pathToImage.put(imagePath, image);
                imageToPath.put(image, imagePath);
            }
            return image;
        } catch (MalformedURLException mfue) {
            debug("IfcApplet.getImage: failed to read:" + imagePath);
        } catch (Exception exc) {
            errorMsg("Error getting image: " + imagePath + "\n" + exc);
        }
        return null;
    }

    /**
     * _more_
     *
     * @param i _more_
     *
     * @return _more_
     */
    public static String getImagePath(Image i) {
        return ((i == null)
                ? "null image"
                : (String) imageToPath.get(i));
    }

    /**
     * _more_
     *
     * @param d _more_
     */
    public void show(OkCancelDialog d) {
        Point sp = GuiUtils.getScreenLocation(this, null);
        d.setLocation(sp.x + 20, sp.y + 50);
        d.init();
    }





    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public String processUrl(String url) {
        return url;
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
            if ((which != null) && (which.trim().length() > 0)) {
                getAppletContext().showDocument(url, "_blank");
            } else {
                getAppletContext().showDocument(url);
            }
        } else {
            System.err.println("Error: Malformed url: " + urlString);
        }
    }


    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public String readUrl(String url) {
        if (url == null) {
            return null;
        }
        url = getFullUrl(url);
        try {
            return GuiUtils.readUrl(url);
        } catch (Exception exc) {
            System.err.println("Error reading url:" + url + " " + exc);
        }
        return url;
    }


    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public String getFullUrl(String url) {
        if (url == null) {
            return null;
        }
        try {
            url = new URL(baseUrl, url).toString();
        } catch (MalformedURLException mfue) {
            debug("Error creating url:" + url + "\n" + mfue);
            return null;
        }
        return processUrl(url);
    }


}

