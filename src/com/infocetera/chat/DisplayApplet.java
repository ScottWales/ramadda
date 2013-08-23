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

/**
 * (C) 1999-2002  WTS Systems, L.L.C.
 *   All rights reserved
 */



package com.infocetera.chat;


import com.infocetera.util.*;



import java.applet.*;

import java.awt.*;

import java.util.StringTokenizer;


/**
 * Class DisplayApplet _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class DisplayApplet extends SocketApplet {

    /** _more_ */
    DisplayCanvas canvas;

    /**
     * _more_
     */
    public void init() {
        canvas = new DisplayCanvas(this);
        setLayout(new BorderLayout());
        setBackground(Color.gray);
        add("Center", GuiUtils.inset(canvas.doMakeContents(), 2, 2));
        //    canvas.processGfxList (getProperty ("GFXLIST"));
    }

    /** _more_ */
    public static String msg = "HELLO";

    /** _more_ */
    int cnt = 0;

    /**
     * _more_
     *
     * @return _more_
     */
    public String getMsg() {
        return "MSG: " + (cnt++);
    }


}
