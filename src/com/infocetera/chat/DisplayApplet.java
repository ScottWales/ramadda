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

