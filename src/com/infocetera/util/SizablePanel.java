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



package com.infocetera.util;


import java.awt.*;


/**
 * Class SizablePanel _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class SizablePanel extends Panel {

    /** _more_ */
    public int y;

    /** _more_ */
    public String msg = null;

    /** _more_ */
    Font myFont = null;

    /** _more_ */
    Color myColor;


    /**
     * _more_
     *
     * @param height _more_
     */
    public SizablePanel(int height) {
        this(height, null);
    }

    /**
     * _more_
     *
     * @param height _more_
     * @param msg _more_
     */
    public SizablePanel(int height, String msg) {
        this(height, msg, null);
    }

    /**
     * _more_
     *
     * @param height _more_
     * @param msg _more_
     * @param color _more_
     */
    public SizablePanel(int height, String msg, Color color) {
        this.msg     = msg;
        this.y       = height;
        this.myColor = color;
        if (myColor == null) {
            myColor = new Color(255, 204, 102);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Dimension getMinimumSize() {
        Dimension pd = super.getMinimumSize();
        pd.height = y;

        return pd;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }



    /**
     * _more_
     *
     * @param g _more_
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (msg != null) {
            if (myFont == null) {
                Font tmp = g.getFont();
                myFont = new Font(tmp.getName(), Font.BOLD, 16);
            }
            Font      oldFont = g.getFont();
            Rectangle b       = getBounds();
            g.setFont(myFont);
            FontMetrics fm     = g.getFontMetrics();
            int         ascent = fm.getMaxAscent();
            int         offset = 8;
            g.setColor(myColor);
            int arcW = b.height / 2;
            g.fillRect(arcW, 0, b.width - arcW, b.height);
            g.fillArc(0, 0, 2 * arcW, 2 * arcW, 180, -90);
            g.fillRect(0, arcW, arcW, arcW);
            g.setColor(Color.black);
            g.drawString(msg, arcW, ascent);
            g.setFont(oldFont);
        }

    }


}

;
