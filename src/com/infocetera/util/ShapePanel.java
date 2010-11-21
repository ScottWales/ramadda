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
 * (C) 1999-2004  WTS Systems, L.L.C.
 *   All rights reserved
 */



package com.infocetera.util;


import com.infocetera.glyph.*;

import java.awt.*;
import java.awt.event.*;

import java.util.Vector;


/**
 * Class ShapePanel _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class ShapePanel extends ScrollCanvas implements MouseListener {

    /** _more_ */
    XmlUi xmlUi;

    /** _more_ */
    Vector glyphs = new Vector();

    /** _more_ */
    boolean getWidthFromGlyphs = false;

    /** _more_ */
    boolean getHeightFromGlyphs = false;

    /** _more_ */
    Dimension dim;

    /** _more_ */
    XmlNode node;

    /** _more_ */
    boolean mouseIn = false;

    /** _more_ */
    boolean mouseDown = false;



    /** _more_ */
    boolean debug = false;


    /**
     * _more_
     *
     * @param xmlUi _more_
     * @param node _more_
     */
    public ShapePanel(XmlUi xmlUi, XmlNode node) {
        this.node = node;
        addMouseListener(this);
        this.xmlUi = xmlUi;
        dim = new Dimension(node.getAttribute("width", -1),
                            node.getAttribute("height", -1));

        getWidthFromGlyphs  = dim.width < 0;
        getHeightFromGlyphs = dim.height < 0;


        for (int i = 0; i < node.size(); i++) {
            XmlNode cnode  = node.get(i);

            int     width  = 0;
            int     height = 0;
            String  ws     = cnode.getAttribute("width");
            if ((ws == null) || !ws.equals("inherit")) {
                width = cnode.getAttribute("width", 10);
            }

            String hs = cnode.getAttribute("height");
            if ((hs == null) || !hs.equals("inherit")) {
                height = cnode.getAttribute("height", 10);
            }


            Glyph  glyph = null;
            String type  = cnode.getAttribute("type", Glyph.RECTANGLE);
            if (type.equals(Glyph.TEXT)) {
                glyph = new TextGlyph(this, cnode.getAttribute("x", 0),
                                      cnode.getAttribute("y", 0),
                                      cnode.getAttribute("text", ""));
            } else if (type.equals(Glyph.IMAGE)) {
                debug = true;
                glyph = new ImageGlyph(this, cnode.getAttribute("x", 0),
                                       cnode.getAttribute("y", 0),
                                       cnode.getAttribute("image", ""));
            } else if (type.equals(Glyph.LINE)) {
                glyph = new LineGlyph(cnode.getAttribute("x1", 0),
                                      cnode.getAttribute("y1", 0),
                                      cnode.getAttribute("x2", 0),
                                      cnode.getAttribute("y2", 0));
            } else if (type.equals(Glyph.HTMLTEXT)) {
                glyph = new HtmlGlyph(this, cnode.getAttribute("x", 0),
                                      cnode.getAttribute("y", 0),
                                      cnode.getAttribute("text", ""));
            } else {
                glyph = new RectangleGlyph(type, cnode.getAttribute("x", 0),
                                           cnode.getAttribute("y", 0), width,
                                           height);
                glyph.startAngle  = cnode.getAttribute("start", 0);
                glyph.lengthAngle = cnode.getAttribute("length", 360);
            }
            if (glyph == null) {
                continue;
            }
            glyph.setColor(cnode.getAttribute("color", Color.gray));
            glyph.filter = cnode.getAttribute("filter", (String) null);
            glyphs.addElement(glyph);
        }
        calculateBounds();
        setSize(dim);
        setPreferredSize(dim);
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void elementChanged(Object g) {
        //    System.err.println ("elementChanged");
        calculateBounds();
        if (isValid()) {
            GuiUtils.relayout(this);
        }
    }


    /**
     * _more_
     */
    public void calculateBounds() {
        int maxX = -1;
        int maxY = -1;
        //    if (debug) System.err.println ("calc");

        for (int i = 0; i < glyphs.size(); i++) {
            Glyph     glyph = (Glyph) glyphs.elementAt(i);
            Rectangle b     = glyph.getBounds();
            //      if (debug) System.err.println ("\tcalc:" +b);
            maxX = Math.max(maxX, b.x + b.width);
            maxY = Math.max(maxY, b.y + b.height);
        }
        if (getWidthFromGlyphs) {
            dim.width = maxX;
        }
        if (getHeightFromGlyphs) {
            dim.height = maxY;
        }
        setSizes();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Dimension getSize() {
        return dim;
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseClicked(MouseEvent e) {
        String onClick = node.getAttribute("mouseclick", (String) null);
        if (onClick != null) {
            xmlUi.processActions(onClick, this);
        }
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseEntered(MouseEvent e) {
        mouseIn = true;
        String cmd = node.getAttribute("mouseenter", (String) null);
        if (cmd != null) {
            xmlUi.processActions(cmd, this);
        }
        repaint();
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseExited(MouseEvent e) {
        mouseIn = false;
        String cmd = node.getAttribute("mouseexit", (String) null);
        if (cmd != null) {
            xmlUi.processActions(cmd, this);
        }
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
     * @param x _more_
     * @param y _more_
     * @param width _more_
     * @param height _more_
     */
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        setSizes();
    }

    /**
     * _more_
     */
    private void setSizes() {
        for (int i = 0; i < glyphs.size(); i++) {
            Glyph glyph = (Glyph) glyphs.elementAt(i);
            if ( !(glyph instanceof RectangleGlyph)) {
                continue;
            }
            XmlNode cnode = node.get(i);
            String  ws    = cnode.getAttribute("width", "");
            String  hs    = cnode.getAttribute("height", "");
            if (ws.equals("inherit")) {
                ((RectangleGlyph) glyph).bounds.width =
                    cnode.getAttribute("dwidth", 0) + dim.width;
            }
            if (hs.equals("inherit")) {
                ((RectangleGlyph) glyph).bounds.height =
                    cnode.getAttribute("dheight", 0) + dim.height;
            }
        }
    }




    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintInner(Graphics g) {
        for (int i = 0; i < glyphs.size(); i++) {
            Glyph glyph = (Glyph) glyphs.elementAt(i);
            if (glyph.filter != null) {
                if (glyph.filter.equals("mousein") && !mouseIn) {
                    continue;
                }
                if (glyph.filter.equals("mouseout") && mouseIn) {
                    continue;
                }
                if (glyph.filter.equals("mousedown") && !mouseDown) {
                    continue;
                }
                if (glyph.filter.equals("mouseup") && mouseDown) {
                    continue;
                }
            }
            glyph.paint(g, this);
        }
    }


}

