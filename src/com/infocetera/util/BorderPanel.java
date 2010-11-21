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

