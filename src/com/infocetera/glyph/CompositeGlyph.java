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

import java.awt.Color;
import java.awt.Graphics;

import java.awt.Point;
import java.awt.Rectangle;

import java.util.StringTokenizer;
import java.util.Vector;


/**
 * Class CompositeGlyph _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class CompositeGlyph extends RectangleGlyph {

    /** _more_          */
    protected Vector children = new Vector();

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     * @param newChildren _more_
     */
    public CompositeGlyph(int x, int y, Vector newChildren) {
        super(GROUP, x, y, 1, 1);
        if (newChildren != null) {
            for (int i = 0; i < newChildren.size(); i++) {
                addChild((Glyph) newChildren.elementAt(i));
            }
        }
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void childChanged(Glyph g) {
        calculateBounds();
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void removeChild(Glyph g) {
        children.removeElement(g);
        calculateBounds();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Vector getChildren() {
        return children;
    }

    /**
     * _more_
     */
    public void unGroup() {
        for (int i = 0; i < children.size(); i++) {
            Glyph child = (Glyph) children.elementAt(i);
            child.setParent(null);
        }
        children.removeAllElements();
    }



    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public double distance(int x, int y) {
        if ( !getBoundsFromChildren() || (children.size() == 0)) {
            return super.distance(x, y);
        }
        double min = Double.MAX_VALUE;
        for (int i = 0; i < children.size(); i++) {
            Glyph child = (Glyph) children.elementAt(i);
            min = Math.min(child.distance(x, y), min);
        }
        return min;
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void addChild(Glyph g) {
        addChild(g, true);
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param calculateBounds _more_
     */
    public void addChild(Glyph g, boolean calculateBounds) {
        children.addElement(g);
        g.setParent(this);
        if (calculateBounds) {
            calculateBounds();
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getBoundsFromChildren() {
        return true;
    }

    /**
     * _more_
     */
    public void calculateBounds() {
        if ( !getBoundsFromChildren() || (children.size() == 0)) {
            return;
        }
        bounds = super.calculateBounds(children);
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    public void paint(Graphics g, ScrollCanvas c) {}

    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    protected void paintChildren(Graphics g, ScrollCanvas c) {
        for (int i = 0; i < children.size(); i++) {
            Glyph child = (Glyph) children.elementAt(i);
            child.paint(g, c);
        }
    }

    /**
     * _more_
     *
     * @param c _more_
     */
    public void setColor(Color c) {
        super.setColor(c);
        if (remoteInit) {
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            Glyph child = (Glyph) children.elementAt(i);
            child.setColor(c);
        }
    }


    /**
     * _more_
     *
     * @param c _more_
     */
    public void setBgColor(Color c) {
        super.setBgColor(c);
        if (remoteInit) {
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            Glyph child = (Glyph) children.elementAt(i);
            child.setBgColor(c);
        }
    }


    /**
     * _more_
     *
     * @param c _more_
     */
    public void setWidth(int c) {
        super.setWidth(c);
        if (remoteInit) {
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            Glyph child = (Glyph) children.elementAt(i);
            child.setWidth(c);
        }
        calculateBounds();
    }

    /**
     * _more_
     *
     * @param c _more_
     */
    public void setFilled(boolean c) {
        super.setFilled(c);
        if (remoteInit) {
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            Glyph child = (Glyph) children.elementAt(i);
            child.setFilled(c);
        }
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
        super.stretchTo(x, y, pt, correct);
        for (int i = 0; i < children.size(); i++) {
            Glyph child = (Glyph) children.elementAt(i);
            child.stretchTo(x, y, pt, correct);
        }
        calculateBounds();
        return pt;
    }


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void moveTo(int x, int y) {
        Point delta = new Point(x - bounds.x, y - bounds.y);
        bounds.x = x;
        bounds.y = y;
        for (int i = 0; i < children.size(); i++) {
            Glyph child = (Glyph) children.elementAt(i);
            child.moveBy(delta.x, delta.y);
        }
        calculateBounds();
    }

    /**
     * _more_
     *
     * @param attr _more_
     *
     * @return _more_
     */
    public String getAttrs(String attr) {
        if ((attr == null) || attr.equals(ATTR_CHILDREN)) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < children.size(); i++) {
                Glyph child = (Glyph) children.elementAt(i);
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(child.getId());
            }
            return makeAttr(ATTR_CHILDREN, sb.toString())
                   + super.getAttrs(attr);
        }
        return super.getAttrs(attr);
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     */
    public void setAttr(String name, String value) {
        if (ATTR_CHILDREN.equals(name)) {
            StringTokenizer st = new StringTokenizer(value, ",");
            for (int i = 0; i < children.size(); i++) {
                Glyph child = (Glyph) children.elementAt(i);
                child.setParent(null);
            }
            children.removeAllElements();
            while (st.hasMoreTokens()) {
                String cId   = st.nextToken();
                Glyph  child = (Glyph) idToGlyph.get(cId);
                if (child != null) {
                    addChild(child, false);
                }
            }
            calculateBounds();
        } else {
            super.setAttr(name, value);
        }

    }



}

