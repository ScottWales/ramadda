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



package com.infocetera.glyph;


import com.infocetera.util.*;

import java.awt.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 *  Base (abstract) class for representing things that are drawn on
 *  the screen. Holds some basic attributes (but no positional attrs)
 *  and has a facility for recreating itself via xml.
 */

public abstract class Glyph implements Cloneable {

    /** _more_          */
    public static final String PT_CENTER = "MM";

    /** _more_          */
    public static final String PT_V_UPPER = "U";

    /** _more_          */
    public static final String PT_V_MIDDLE = "M";

    /** _more_          */
    public static final String PT_V_LOWER = "L";

    /** _more_          */
    public static final String PT_H_LEFT = "L";

    /** _more_          */
    public static final String PT_H_MIDDLE = "M";

    /** _more_          */
    public static final String PT_H_RIGHT = "R";

    /** _more_          */
    public static final String PT_UL = PT_V_UPPER + PT_H_LEFT;

    /** _more_          */
    public static final String PT_LR = PT_V_LOWER + PT_H_RIGHT;


    /** _more_          */
    public static final String PT_PREFIX = "P";

    /** _more_          */
    public static final String PT_P1 = PT_PREFIX + "1";

    /** _more_          */
    public static final String PT_P2 = PT_PREFIX + "2";


    /** _more_          */
    public static final String GROUP = "GROUP";

    /** _more_          */
    public static final String HTMLTEXT = "HTMLTEXT";

    /** _more_          */
    public static final String XML = "XML";

    /** _more_          */
    public static final String RECTANGLE = "RECTANGLE";

    /** _more_          */
    public static final String FRECTANGLE = "FRECTANGLE";

    /** _more_          */
    public static final String ROUNDRECT = "ROUNDRECT";

    /** _more_          */
    public static final String FROUNDRECT = "FROUNDRECT";

    /** _more_          */
    public static final String CIRCLE = "CIRCLE";

    /** _more_          */
    public static final String FCIRCLE = "FCIRCLE";

    /** _more_          */
    public static final String IMAGE = "IMAGE";

    /** _more_          */
    public static final String TEXT = "TEXT";

    /** _more_          */
    public static final String LINE = "LINE";

    /** _more_          */
    public static final String PLINE = "PLINE";


    /** _more_          */
    public static final String ATTR_PTS = "pts";

    /** _more_          */
    public static final String ATTR_COLOR = "color";

    /** _more_          */
    public static final String ATTR_BGCOLOR = "bgcolor";

    /** _more_          */
    public static final String ATTR_WIDTH = "width";

    /** _more_          */
    public static final String ATTR_FILL = "fill";

    /** _more_          */
    public static final String ATTR_TEXT = "text";

    /** _more_          */
    public static final String ATTR_DOHTML = "dohtml";

    /** _more_          */
    public static final String ATTR_CHILDREN = "children";

    /** _more_          */
    public static final String ATTR_PARENT = "parent";

    /** _more_          */
    public static final String ATTR_IMAGE = "image";

    /** _more_          */
    public static Hashtable idToGlyph = new Hashtable();


    /** _more_          */
    public String filter;

    /**
     *  Some global consts
     */
    public static final int MIN_DISTANCE_TO_STRETCH = 4;

    /** _more_          */
    public static final int SEL_WIDTH = 6;

    /** _more_          */
    public static final int H_SEL_WIDTH = 3;

    /** _more_          */
    public static final Color highlightColor = Color.yellow;

    /** _more_          */
    private Glyph parent;

    /** _more_          */
    private boolean filled = false;

    /** _more_          */
    private int width = 1;

    /** _more_          */
    private Color color = Color.black;

    /** _more_          */
    private Color bgColor = null;

    /** _more_          */
    public boolean underline = false;

    /** _more_          */
    public String url;

    /** _more_          */
    public int startAngle = 0;

    /** _more_          */
    public int lengthAngle = 360;

    /** _more_          */
    public int offsetX = 0;

    /** _more_          */
    public int offsetY = 0;


    /** _more_          */
    public int baseline;

    /** _more_          */
    private String id = "";

    /** _more_          */
    protected String typeName;



    /**
     * _more_
     *
     * @param type _more_
     */
    public Glyph(String type) {
        typeName = type;
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws CloneNotSupportedException _more_
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }




    /**
     * _more_
     *
     * @param newId _more_
     */
    public void setId(String newId) {
        if ( !id.equals("")) {
            idToGlyph.remove(id);
        }
        id = newId;
        if ( !id.equals("")) {
            idToGlyph.put(id, this);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getId() {
        return id;
    }




    /**
     *  A couple of methods so we don't have to
     *  compile CompositeGlyph if we don't need to.
     *
     * @param g _more_
     */
    public void removeChild(Glyph g) {}

    /**
     * _more_
     */
    public void calculateBounds() {}

    /**
     * _more_
     */
    public void doRemove() {
        if ( !id.equals("")) {
            idToGlyph.remove(id);
        }
        if (parent != null) {
            parent.removeChild(this);
        }
    }

    /**
     * _more_
     *
     * @param newParent _more_
     */
    public void setParent(Glyph newParent) {
        parent = newParent;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean pickable() {
        return parent == null;
    }

    /**
     * _more_
     */
    public void notifyChange() {
        if (parent != null) {
            parent.calculateBounds();
        }
    }




    //These methods used  to guide the behavior of the canvas editing

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getPersistent() {
        return true;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean canStretch() {
        return true;
    }

    /** Hook for when the size or position of this glyph is changed */
    public void boundsChanged() {}



    /**
     * Hook for setting points from the attribute line*
     *
     * @param p _more_
     * @param cnt _more_
     */
    public void setPoints(int[] p, int cnt) {}

    //Attribute sets and gets

    /**
     * _more_
     *
     * @param c _more_
     */
    public void setWidth(int c) {
        width = c;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getWidth() {
        return width;
    }

    /**
     * _more_
     *
     * @param c _more_
     */
    public void setColor(Color c) {
        color = c;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Color getColor() {
        return color;
    }

    /**
     * _more_
     *
     * @param c _more_
     */
    public void setBgColor(Color c) {
        bgColor = c;
    }

    /**
     * _more_
     *
     * @param dflt _more_
     *
     * @return _more_
     */
    public Color getBgColor(Color dflt) {
        return ((bgColor != null)
                ? bgColor
                : dflt);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Color getBgColor() {
        return bgColor;
    }

    /**
     * _more_
     *
     * @param c _more_
     */
    public void setFilled(boolean c) {
        filled = c;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getFilled() {
        return filled;
    }

    /**
     *  A Glyph can write out a persistent copy of itself as a set of
     *  attribute/value  pairs.
     *  This method parses the line of attr/values of the form:
     *  ATTR=VALUE;ATTR=VALUE;
     */
    boolean remoteInit = false;

    /**
     * _more_
     *
     * @param b _more_
     */
    public void setRemoteInit(boolean b) {
        remoteInit = b;
    }

    /**
     * _more_
     *
     * @param node _more_
     */
    public void processAttrs(XmlNode node) {
        for (Enumeration attrs = node.getAttributes();
                attrs.hasMoreElements(); ) {
            String attr  = (String) attrs.nextElement();
            String value = node.getAttribute(attr);
            //      System.err.println (attr +"=" + value);
            setAttr(attr.trim(), value);
        }
    }



    /**
     * _more_
     *
     * @param v _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public static int getInt(String v, int dflt) {
        if (v != null) {
            try {
                dflt = Integer.decode(v).intValue();
            } catch (Exception exc) {}
        }
        return dflt;
    }

    /**
     * _more_
     *
     * @param v _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public static double getDouble(String v, double dflt) {
        if (v != null) {
            try {
                dflt = new Double(v).doubleValue();
            } catch (Exception exc) {}
        }
        return dflt;
    }

    /**
     *  Set my named attribute to value
     *
     * @param name _more_
     * @param value _more_
     */
    public void setAttr(String name, String value) {
        if (ATTR_FILL.equals(name)) {
            setFilled(("true".equals(value)));
        } else if (ATTR_WIDTH.equals(name)) {
            setWidth(getInt(value, width));
        } else if (ATTR_BGCOLOR.equals(name)) {
            setBgColor(GuiUtils.getColor(value));
        } else if (ATTR_COLOR.equals(name)) {
            setColor(GuiUtils.getColor(value, color));
        } else if (ATTR_PTS.equals(name)) {
            StringTokenizer st  = new StringTokenizer(value);
            int[]           pts = new int[1000];
            int             i   = 0;
            try {
                while (st.hasMoreTokens() && (i < pts.length)) {
                    pts[i++] = Integer.decode(st.nextToken()).intValue();
                }
            } catch (Exception exc) {}
            setPoints(pts, i);
        } else {
            //      System.err.println("Unknown Glyph attribute:" + name);
        }

    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getType() {
        return typeName;
    }


    /**
     *  Return the string used to recreate this glyph
     *
     * @return _more_
     */
    public String getCreateString() {
        return getPositionAttr() + " " + getAttrs(null);
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     *
     * @return _more_
     */
    public String makeAttr(String name, String value) {
        return makeAttr(null, name, value);
    }

    /**
     * _more_
     *
     * @param match _more_
     * @param name _more_
     * @param value _more_
     *
     * @return _more_
     */
    public String makeAttr(String match, String name, String value) {
        if ((match == null) || match.equals(name)) {
            return XmlNode.attr(name, value);
        }
        return "";
    }


    /**
     * _more_
     *
     * @param attr _more_
     *
     * @return _more_
     */
    public String getAttrs(String attr) {

        return makeAttr(attr, ATTR_FILL, "" + filled)
               + makeAttr(attr, ATTR_WIDTH, "" + width)
               + makeAttr(attr, ATTR_BGCOLOR, ((bgColor != null)
                ? bgColor.getRed() + "," + bgColor.getGreen() + ","
                  + bgColor.getBlue()
                : "null")) + makeAttr(attr, ATTR_COLOR,
                                      color.getRed() + "," + color.getGreen()
                                      + "," + color.getBlue());
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    public void paint(Graphics g, ScrollCanvas c) {}


    /**
     *  By default paint little black rectangles at the corners and the sides
     *
     * @param g _more_
     * @param c _more_
     */

    public void paintSelection(Graphics g, ScrollCanvas c) {
        Rectangle r  = c.scaleRect(getBounds());
        int       L  = r.x - H_SEL_WIDTH;
        int       T  = r.y - H_SEL_WIDTH;
        int       R  = r.x + r.width - H_SEL_WIDTH + 1;
        int       B  = r.y + r.height - H_SEL_WIDTH + 1;
        int       MY = T + r.height / 2;
        int       MX = L + r.width / 2;
        /*    if (c.canvasBg== Color.black)
              g.setColor(Color.white);
              else*/
        g.setColor(Color.black);
        g.fillRect(L, T, SEL_WIDTH, SEL_WIDTH);
        g.fillRect(L, B, SEL_WIDTH, SEL_WIDTH);
        g.fillRect(R, T, SEL_WIDTH, SEL_WIDTH);
        g.fillRect(R, B, SEL_WIDTH, SEL_WIDTH);
        g.fillRect(L, MY, SEL_WIDTH, SEL_WIDTH);
        g.fillRect(R, MY, SEL_WIDTH, SEL_WIDTH);
        g.fillRect(MX, T, SEL_WIDTH, SEL_WIDTH);
        g.fillRect(MX, B, SEL_WIDTH, SEL_WIDTH);
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    public void paintHighlight(Graphics g, ScrollCanvas c) {
        Rectangle bounds = getBounds();
        Rectangle r      = c.scaleRect(bounds);
        g.setColor(highlightColor);
        g.drawRect(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
        g.drawRect(r.x - 2, r.y - 2, r.width + 4, r.height + 4);
    }

    /**
     *  These are all methods for manipulating and accessing screen position
     *  that derived classes have to implement
     *
     * @return _more_
     */
    public abstract String getPositionAttr();

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public abstract double distance(int x, int y);

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public abstract void moveBy(int x, int y);

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public abstract void moveTo(int x, int y);

    /**
     * _more_
     *
     * @return _more_
     */
    public abstract Rectangle getBounds();

    /**
     * _more_
     *
     * @return _more_
     */
    public Rectangle getRepaintBounds() {
        return getBounds();
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public static Rectangle calculateBounds(Vector v) {
        Rectangle bounds = null;
        for (int i = 0; i < v.size(); i++) {
            Glyph     child = (Glyph) v.elementAt(i);
            Rectangle cb    = child.getBounds();
            if (bounds == null) {
                bounds = new Rectangle(cb);
            } else {
                bounds.add(cb);
            }
        }
        if (bounds == null) {
            bounds = new Rectangle(0, 0, 0, 0);
        } else {
            bounds.x      -= 2;
            bounds.y      -= 2;
            bounds.width  += 4;
            bounds.height += 4;
        }
        return bounds;
    }



    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public String getStretchPoint(int x, int y) {
        return PT_CENTER;
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     * @param pt _more_
     * @param correct _more_
     *
     * @return _more_
     */
    public String stretchTo(int x, int y, String pt, boolean correct) {
        moveBy(x, y);
        return PT_CENTER;
    }




    /**
     * Utility method for distance between two points
     *
     * @param x1 _more_
     * @param y1 _more_
     * @param x2 _more_
     * @param y2 _more_
     *
     * @return _more_
     */
    public static double distance(int x1, int y1, int x2, int y2) {
        return (Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));
    }


}

