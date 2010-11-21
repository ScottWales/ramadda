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





package com.infocetera.glyph;


import com.infocetera.util.*;




import java.awt.*;

import java.util.Vector;


/**
 * Class PolyGlyph _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class PolyGlyph extends Glyph {

    /** _more_          */
    Rectangle bounds = null;

    /** _more_          */
    Vector points = new Vector();

    /** _more_          */
    int[] xs = null;

    /** _more_          */
    int[] ys = null;

    /** _more_          */
    int[] sxs = null;

    /** _more_          */
    int[] sys = null;

    /** _more_          */
    double lastScale = 1.0;

    /** _more_          */
    public boolean isSingleLine = false;

    /**
     * _more_
     *
     * @param isSingleLine _more_
     */
    public PolyGlyph(boolean isSingleLine) {
        super("PLINE");
        this.isSingleLine = isSingleLine;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws CloneNotSupportedException _more_
     */
    public Object clone() throws CloneNotSupportedException {
        PolyGlyph clonedObject = (PolyGlyph) super.clone();
        clonedObject.points = new Vector();
        for (int i = 0; i < points.size(); i++) {
            Point p = (Point) points.elementAt(i);
            clonedObject.points.addElement(new Point(p.x, p.y));
        }
        clonedObject.invalidatePosition();
        return clonedObject;
    }



    /**
     * _more_
     */
    void makeArray() {
        if (xs != null) {
            return;
        }
        int size = points.size();
        xs = new int[size];
        ys = new int[size];

        for (int i = 0; i < size; i++) {
            Point p = (Point) points.elementAt(i);
            xs[i] = p.x;
            ys[i] = p.y;
        }

    }

    /**
     * _more_
     */
    void invalidatePosition() {
        bounds = null;
        xs     = null;
        ys     = null;
        sxs    = null;
        sys    = null;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Rectangle getBounds() {
        if (bounds == null) {
            bounds = new Rectangle();
            makeArray();
            if (xs.length == 0) {
                return bounds;
            }
            int minx = xs[0];
            int miny = ys[0];
            int maxx = xs[0];
            int maxy = ys[0];

            for (int i = 1; i < xs.length; i++) {
                if (xs[i] < minx) {
                    minx = xs[i];
                } else if (xs[i] > maxx) {
                    maxx = xs[i];
                }
                if (ys[i] < miny) {
                    miny = ys[i];
                } else if (ys[i] > maxy) {
                    maxy = ys[i];
                }
            }
            bounds.x      = minx;
            bounds.y      = miny;
            bounds.width  = maxx - minx;
            bounds.height = maxy - miny;
        }
        return bounds;
    }

    /**
     * _more_
     *
     * @param p _more_
     * @param cnt _more_
     */
    public void setPoints(int[] p, int cnt) {
        invalidatePosition();
        points = new Vector();
        for (int i = 0; i < cnt; i += 2) {
            points.addElement(new Point(p[i], p[i + 1]));
        }
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    public void paintHighlight(Graphics g, ScrollCanvas c) {
        makeArray();
        if (xs.length <= 1) {
            return;
        }
        g.setColor(highlightColor);
        paintLines(g, c, getWidth(), false);
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    public void paint(Graphics g, ScrollCanvas c) {
        makeArray();
        if (xs.length <= 1) {
            return;
        }
        Color bgColor = getBgColor(getColor());
        if (getFilled()) {
            g.setColor(bgColor);
            paintLines(g, c, getWidth(), true);
            if (bgColor.equals(getColor())) {
                return;
            }
        }
        g.setColor(getColor());
        paintLines(g, c, getWidth(), false);
    }



    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     * @param w _more_
     * @param fill _more_
     */
    public void paintLines(Graphics g, ScrollCanvas c, int w, boolean fill) {
        if (fill) {
            g.fillPolygon(xs, ys, xs.length);
        } else {
            //      try {
            //      g.drawPolyline(xs,ys,xs.length);
            //      } catch (Throwable exc){
            int[]  thex  = xs;
            int[]  they  = ys;
            double scale = 1.0;
            //      double scale = c.scale;
            if (scale != 1.0) {
                if ((lastScale != scale) || (sxs == null)) {
                    lastScale = scale;
                    if (sxs == null) {
                        sxs = new int[thex.length];
                        sys = new int[thex.length];
                    }
                    for (int i = 0; i < sxs.length; i++) {
                        sxs[i] = (int) (scale * xs[i]);
                        sys[i] = (int) (scale * ys[i]);
                    }
                }
                thex = sxs;
                they = sys;
            }



            int lastx = thex[0];
            int lasty = they[0];
            //      int sw = c.scaleCoord(w);
            int        sw     = w;
            Graphics2D g2d    = (Graphics2D) g;
            Stroke     stroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(sw));
            g.drawPolyline(thex, they, thex.length);
            /*      for(int i=0;i<thex.length;i++){
              //      GuiUtils.drawLine(g,lastx,lasty,thex[i],they[i],sw);
              lastx=thex[i];
              lasty=they[i];
              }*/
            g2d.setStroke(stroke);
            //      }
        }
    }


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void addPoint(int x, int y) {
        invalidatePosition();
        if (points.size() > 1) {
            Point   p1       = (Point) points.elementAt(points.size() - 1);
            Point   p2       = (Point) points.elementAt(points.size() - 2);
            boolean extended = false;

            if ((p1.x == p2.x) && (p2.x == x)) {
                p1.y     = y;
                extended = true;
            }
            if ((p1.y == p2.y) && (p2.y == y)) {
                p1.x     = x;
                extended = true;
            }
            if (extended) {
                return;
            }
        }

        points.addElement(new Point(x, y));
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getPositionAttr() {
        String ret  = "";
        int    size = points.size();
        for (int i = 0; i < size; i++) {
            Point p = (Point) points.elementAt(i);
            ret = ret + " " + p.x + " " + p.y + " ";
        }
        return makeAttr(ATTR_PTS, ret);
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
        int minIdx = findMinIndex(x, y, Double.MAX_VALUE);
        if (minIdx < 0) {
            return Double.MAX_VALUE;
        }
        Point p1 = (Point) points.elementAt(minIdx);
        return GuiUtils.distance(p1.x, p1.y, x, y);
    }




    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void moveBy(int x, int y) {
        invalidatePosition();
        int size = points.size();
        for (int i = 0; i < size; i++) {
            Point p = (Point) points.elementAt(i);
            p.x += x;
            p.y += y;
        }
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void moveTo(int x, int y) {
        //TODO
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     * @param threshold _more_
     *
     * @return _more_
     */
    public int findMinIndex(int x, int y, double threshold) {
        double min    = threshold;
        int    minIdx = -1;
        Point  fp     = new Point(x, y);
        for (int i = 0; i < points.size(); i++) {
            Point  p1  = (Point) points.elementAt(i);
            double tmp = GuiUtils.distance(p1.x, p1.y, fp.x, fp.y);
            if (tmp < min) {
                min    = tmp;
                minIdx = i;
            }
        }
        return minIdx;
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
        int minIdx = findMinIndex(x, y, 2.0);
        if (minIdx >= 0) {
            return PT_PREFIX + minIdx;
        }
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
        if (pt.equals(PT_CENTER)) {
            super.stretchTo(x, y, pt, correct);
        } else {
            int   index = new Integer(pt.substring(1)).intValue();
            Point p     = (Point) points.elementAt(index);
            p.x = x;
            p.y = y;
        }
        invalidatePosition();
        return pt;
    }


}

