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

package com.infocetera.gantt;


import com.infocetera.util.*;

import java.applet.*;

import java.awt.*;

import java.io.*;

import java.net.*;

import java.util.*;




/**
 * Class GanttApplet _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GanttApplet extends IfcApplet {

    /** _more_ */
    GanttView ganttView;

    /**
     * _more_
     */
    public GanttApplet() {}

    /**
     * _more_
     */
    public void initInner() {
        IfcApplet.debug = true;
        setLayout(new BorderLayout());
        ganttView = new GanttView(this);
        add("Center", ganttView.getContents());
    }

    /**
     * _more_
     */
    public void stop() {
        super.stop();
        if (ganttView != null) {
            ganttView.stop();
        }
    }


}
