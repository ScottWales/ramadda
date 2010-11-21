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

import java.awt.Color;
import java.awt.Graphics;





import java.awt.Point;
import java.awt.Rectangle;


/**
 * Class RectangleGlyph _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class RectangleGlyph extends Glyph {

    /** _more_          */
    public Rectangle bounds;

    /** _more_          */
    public static final int TYPE_RECT = 0;

    /** _more_          */
    public static final int TYPE_RRECT = 1;

    /** _more_          */
    public static final int TYPE_CIRCLE = 2;

    /** _more_          */
    int type;







    /**
     * _more_
     *
     * @param t _more_
     * @param x _more_
     * @param y _more_
     * @param w _more_
     * @param h _more_
     */
    public RectangleGlyph(String t, int x, int y, int w, int h) {
        super("");
        bounds   = new Rectangle(x, y, w, h);
        typeName = t;
        if (typeName.equals(RECTANGLE)) {
            type = TYPE_RECT;
        } else if (typeName.equals(CIRCLE)) {
            type = TYPE_CIRCLE;
        } else if (typeName.equals(ROUNDRECT)) {
            type = TYPE_RRECT;
        } else if (typeName.equals(FRECTANGLE)) {
            type = TYPE_RECT;
            setFilled(true);
        } else if (typeName.equals(FCIRCLE)) {
            type = TYPE_CIRCLE;
            setFilled(true);
        } else if (typeName.equals(FROUNDRECT)) {
            type = TYPE_RRECT;
            setFilled(true);
        }

    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws CloneNotSupportedException _more_
     */
    public Object clone() throws CloneNotSupportedException {
        RectangleGlyph clonedObject = (RectangleGlyph) super.clone();
        clonedObject.bounds = new Rectangle(bounds.x, bounds.y, bounds.width,
                                            bounds.height);
        clonedObject.typeName = typeName;
        clonedObject.type     = type;
        return clonedObject;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getBottom() {
        return bounds.y + bounds.height;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getTop() {
        return bounds.y;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getLeft() {
        return bounds.x;
    }



    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    public void paint(Graphics g, ScrollCanvas c) {
        super.paint(g, c);
        Rectangle r = c.scaleRect(bounds);
        g.setColor(getColor());
        int newW = c.scaleCoord(getWidth());
        switch (type) {

          case TYPE_RECT : {
              if (getFilled()) {
                  g.setColor(getBgColor(getColor()));
                  g.fillRect(r.x, r.y, r.width + 1, r.height + 1);
                  g.setColor(getColor());
              }
              GuiUtils.drawRect(g, r.x, r.y, r.width, r.height, newW);
              break;
          }

          case TYPE_RRECT : {
              System.err.println("Rect glyph");
              int radius = c.scaleCoord(10);
              if (getFilled()) {
                  g.setColor(getBgColor(getColor()));
                  g.fillRoundRect(r.x, r.y, r.width + 1, r.height + 1,
                                  radius, radius);
                  g.setColor(getColor());
              }
              GuiUtils.drawRoundRect(g, r.x, r.y, r.width, r.height, radius,
                                     radius, newW);
              break;
          }

          case TYPE_CIRCLE : {
              if (getFilled()) {
                  g.setColor(getBgColor(getColor()));
                  g.fillArc(r.x + offsetX, r.y + offsetY, r.width + 1,
                            r.height + 1, startAngle, lengthAngle);
                  g.setColor(getColor());
              }
              GuiUtils.drawArc(g, r.x + offsetX, r.y + offsetY, r.width,
                               r.height, startAngle, lengthAngle, newW);
              break;
          }
        }
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
        boolean inx = ((x > bounds.x) && (x < bounds.x + bounds.width));
        boolean iny = ((y > bounds.y) && (y < bounds.y + bounds.height));

        if (inx && iny) {
            if (getFilled()) {
                return 0.0;
            }
            double d1 = (x - bounds.x);
            double d2 = (bounds.x + bounds.width - x);
            double d3 = (y - bounds.y);
            double d4 = (bounds.y + bounds.height - y);
            return Math.min(d1, Math.min(d2, Math.min(d3, d4)));
        }


        if (inx) {
            if (y < bounds.y) {
                return (bounds.y - y);
            }
            return (y - (bounds.y + bounds.height));
        }
        if (iny) {
            if (x < bounds.x) {
                return (bounds.x - x);
            }
            return (x - (bounds.x + bounds.width));
        }


        int cx = 0;
        int cy = 0;

        if (x < bounds.x) {
            cx = bounds.x;
        } else if (x > (bounds.x + bounds.width)) {
            cx = bounds.x + bounds.width;
        } else {
            cx = bounds.x + (bounds.width / 2);
        }

        if (y < bounds.y) {
            cy = bounds.y;
        } else if (y > (bounds.y + bounds.height)) {
            cy = bounds.y + bounds.height;
        } else {
            cy = bounds.y + (bounds.height / 2);
        }

        return (Math.sqrt((cx - x) * (cx - x) + (cy - y) * (cy - y)));
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
        String vs = pt.substring(0, 1);
        String hs = pt.substring(1, 2);

        if (PT_CENTER.equals(pt)) {
            bounds.x += x;
            bounds.y += y;
        } else {
            if (vs.equals(PT_V_UPPER)) {
                bounds.height += (bounds.y - y);
                bounds.y      = y;
            } else if (vs.equals(PT_V_LOWER)) {
                bounds.height = (y - bounds.y);
            }

            if (hs.equals(PT_H_LEFT)) {
                bounds.width += (bounds.x - x);
                bounds.x     = x;
            } else if (hs.equals(PT_H_RIGHT)) {
                bounds.width = (x - bounds.x);
            }
        }

        if (correct) {
            if (bounds.width <= 0) {
                if (PT_H_LEFT.equals(hs)) {
                    hs = PT_H_RIGHT;
                } else {
                    hs = PT_H_LEFT;
                }
                bounds.x     = bounds.x + bounds.width;
                bounds.width = -bounds.width;
            }
            if (bounds.height <= 0) {
                if (PT_V_UPPER.equals(vs)) {
                    vs = PT_V_LOWER;
                } else {
                    vs = PT_V_UPPER;
                }
                bounds.y      = bounds.y + bounds.height;
                bounds.height = -bounds.height;
            }
        }
        boundsChanged();
        return vs + hs;
    }


    /**
     * _more_
     *
     * @param pt _more_
     * @param b _more_
     *
     * @return _more_
     */
    public static Point getPoint(String pt, Rectangle b) {
        Point  p  = new Point(b.x, b.y);
        String vs = pt.substring(0, 1);
        String hs = pt.substring(1, 2);
        if (vs.equals(PT_V_UPPER)) {
            p.y = b.y;
        } else if (vs.equals(PT_V_MIDDLE)) {
            p.y = b.y + b.height / 2;
        } else if (vs.equals(PT_V_LOWER)) {
            p.y = b.y + b.height;
        }
        if (hs.equals(PT_H_LEFT)) {
            p.x = b.x;
        } else if (hs.equals(PT_H_MIDDLE)) {
            p.x = b.x + b.width / 2;
        } else if (hs.equals(PT_H_RIGHT)) {
            p.x = b.x + b.width;
        }

        return p;
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
        int    tmp;
        String vS   = PT_V_UPPER;

        int    vMin = Math.abs(bounds.y - y);
        tmp = Math.abs(y - (bounds.y + bounds.height / 2));
        if (tmp < vMin) {
            vMin = tmp;
            vS   = PT_V_MIDDLE;
        }
        tmp = Math.abs(y - (bounds.y + bounds.height));
        if (tmp < vMin) {
            vMin = tmp;
            vS   = PT_V_LOWER;
        }

        String hS   = PT_H_LEFT;
        int    hMin = Math.abs(bounds.x - x);
        tmp = Math.abs(x - (bounds.x + bounds.width / 2));
        if (tmp < hMin) {
            hMin = tmp;
            hS   = PT_H_MIDDLE;
        }
        tmp = Math.abs(x - (bounds.x + bounds.width));
        if (tmp < hMin) {
            hMin = tmp;
            hS   = PT_H_RIGHT;
        }
        if ( !(vS.equals(PT_V_MIDDLE) && hS.equals(PT_H_MIDDLE))
                && (vMin <= MIN_DISTANCE_TO_STRETCH)
                && (hMin <= MIN_DISTANCE_TO_STRETCH)) {
            return vS + hS;
        }
        return PT_CENTER;
    }


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void moveBy(int x, int y) {
        moveTo(x + bounds.x, y + bounds.y);
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void moveTo(int x, int y) {
        bounds.x = x;
        bounds.y = y;
    }


    /**
     * _more_
     *
     * @param p _more_
     * @param cnt _more_
     */
    public void setPoints(int[] p, int cnt) {
        moveBy(p[0] - bounds.x, p[1] - bounds.y);
        bounds.width  = p[2];
        bounds.height = p[3];
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getPositionAttr() {
        return makeAttr(null, ATTR_PTS,
                        bounds.x + " " + bounds.y + " " + bounds.width + " "
                        + bounds.height);

    }

}

