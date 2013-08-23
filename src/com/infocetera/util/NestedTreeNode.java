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

package com.infocetera.util;


import java.awt.*;

import java.util.*;



/**
 */
public class NestedTreeNode {


    /** _more_ */
    public Color hiliteColor = Color.yellow;

    /** _more_ */
    Color lineColor = Color.black;

    /** _more_ */
    Color fillColor = null;

    /** _more_ */
    public Object object;

    /** _more_ */
    Rectangle bounds;

    /** _more_ */
    String label;

    /** _more_ */
    double weight;

    /** _more_ */
    Vector children = new Vector();

    /**
     * _more_
     *
     * @param label _more_
     * @param object _more_
     */
    public NestedTreeNode(String label, Object object) {
        this(label, 0.0, object, Color.black, null);
    }

    /**
     * _more_
     *
     * @param label _more_
     * @param weight _more_
     * @param object _more_
     * @param lineColor _more_
     * @param fillColor _more_
     */
    public NestedTreeNode(String label, double weight, Object object,
                          Color lineColor, Color fillColor) {
        this.label     = label;
        this.weight    = weight;
        this.object    = object;
        this.lineColor = lineColor;
        this.fillColor = fillColor;
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public NestedTreeNode find(int x, int y) {
        if (bounds == null) {
            return null;
        }
        if ( !bounds.contains(x, y)) {
            return null;
        }
        for (int i = 0; i < children.size(); i++) {
            NestedTreeNode child = (NestedTreeNode) children.elementAt(i);
            child = child.find(x, y);
            if (child != null) {
                return child;
            }
        }

        return this;
    }


    /**
     * _more_
     *
     * @param child _more_
     */
    public void add(NestedTreeNode child) {
        if (child != null) {
            children.addElement(child);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public double getChildrenWeight() {
        double childrenWeight = 0;
        for (int i = 0; i < children.size(); i++) {
            NestedTreeNode child = (NestedTreeNode) children.elementAt(i);
            childrenWeight += child.getWeight();
        }

        return childrenWeight;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double getWeight() {
        if (children.size() == 0) {
            return weight;
        }

        return getChildrenWeight();
    }


    /**
     * _more_
     *
     * @param b _more_
     * @param hor _more_
     * @param pad _more_
     */
    public void layout(Rectangle b, boolean hor, int pad) {
        double childrenWeight = getChildrenWeight();
        bounds = new Rectangle(b.x + pad, b.y + pad, b.width - 2 * pad,
                               b.height - 2 * pad);


        int childPlace = (hor
                          ? bounds.x
                          : bounds.y);
        int dimension  = (hor
                          ? bounds.width
                          : bounds.height);
        for (int i = 0; i < children.size(); i++) {
            NestedTreeNode child = (NestedTreeNode) children.elementAt(i);
            double         childWeight  = child.getWeight();
            double         spacePercent = ((childrenWeight == 0.0)
                                           ? 0.0
                                           : childWeight / childrenWeight);
            int            childSpace   = (int) (dimension * spacePercent);
            //      System.err.println ("\t" + childWeight + " " + childSpace);

            if (hor) {
                child.layout(new Rectangle(childPlace, bounds.y, childSpace,
                                           bounds.height), !hor, pad);
            } else {
                child.layout(new Rectangle(bounds.x, childPlace,
                                           bounds.width, childSpace), !hor,
                                               pad);
            }
            childPlace += childSpace;
        }
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintChildren(Graphics g) {
        for (int i = 0; i < children.size(); i++) {
            NestedTreeNode child = (NestedTreeNode) children.elementAt(i);
            child.paint(g, false);
        }
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param hilite _more_
     */
    public void paint(Graphics g, boolean hilite) {
        if (fillColor != null) {
            g.setColor(fillColor);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        g.setColor((hilite
                    ? hiliteColor
                    : lineColor));
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(Color.black);
        if (children.size() == 0) {
            GuiUtils.drawStringAt(g, GuiUtils.clipString(g, label,
                    bounds.width), bounds.x + 2, bounds.y, GuiUtils.PT_NW);
        }
        paintChildren(g);
    }


}
