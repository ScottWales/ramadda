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
import java.awt.event.*;

import java.util.Vector;

import javax.swing.*;


/**
 * Class BorderPanel _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class BorderPanel extends JPanel implements MouseListener {

    /** _more_ */
    public int borderType = GuiUtils.BORDER_EMPTY;

    /** _more_ */
    Insets insets;

    /** _more_ */
    Color[] colors = { null, null, null, null };

    /** _more_ */
    String filter;

    /** _more_ */
    boolean mouseIn = false;

    /** _more_ */
    boolean mouseDown = false;

    /**
     * _more_
     *
     * @param borderType _more_
     */
    public BorderPanel(int borderType) {
        this(borderType, null, null, null);
    }


    /**
     * _more_
     *
     * @param borderType _more_
     * @param insets _more_
     * @param colors _more_
     * @param filter _more_
     */
    public BorderPanel(int borderType, Insets insets, Color[] colors,
                       String filter) {
        this.filter = filter;
        if (filter != null) {
            addMouseListener(this);
        }
        this.borderType = borderType;
        this.insets     = insets;
        if (colors != null) {
            this.colors = colors;
        }
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseEntered(MouseEvent e) {
        mouseIn = true;
        repaint();
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseExited(MouseEvent e) {
        mouseIn = false;
        repaint();
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mousePressed(MouseEvent e) {
        mouseDown = true;
        repaint();
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseReleased(MouseEvent e) {
        mouseDown = false;
        repaint();
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (filter != null) {
            if (filter.equals("mousein") && !mouseIn) {
                return;
            }
            if (filter.equals("mouseout") && mouseIn) {
                return;
            }
            if (filter.equals("mousedown") && !mouseDown) {
                return;
            }
            if (filter.equals("mouseup") && mouseDown) {
                return;
            }
        }



        Dimension size = size();
        GuiUtils.paintBorder(g, borderType, insets, colors, size.width,
                             size.height);
    }


}
