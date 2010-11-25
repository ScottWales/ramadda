/*
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

package com.infocetera.graph;


import com.infocetera.util.GuiUtils;
import com.infocetera.util.XmlNode;

import java.awt.*;

import java.util.*;


/**
 * Class GraphEdge _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GraphEdge extends GraphGlyph {

    private static Font labelFont;

    /** _more_          */
    public static final Point TAILPOINT = new Point(0, 0);

    /** _more_          */
    public static final Point HEADPOINT = new Point(0, 0);

    /** _more_          */
    public static final int HEADY = -9990;

    /** _more_          */
    public static final int HEADX = -9991;

    /** _more_          */
    public static final int TAILX = -9992;

    /** _more_          */
    public static final int TAILY = -9993;


    /** _more_          */
    public static final String ATTR_CIRCLE = "circle";

    /** _more_          */
    public static final String ATTR_ARROW = "arrow";

    /** _more_          */
    public static final String ATTR_COLOR = "color";

    /** _more_          */
    public static final String ATTR_WIDTH = "width";

    /** _more_          */
    private boolean visible = true;

    /** _more_          */
    public String edgeType = null;

    /** _more_          */
    private String tailId;

    /** _more_          */
    private String headId;

    /** _more_          */
    private int level = -1;

    /** _more_          */
    Point[] points;

    /** _more_          */
    Point joint;


    /** _more_          */
    private GraphNode tail;

    /** _more_          */
    private GraphNode head;

    /** _more_          */
    public Color myColor = null;

    /** _more_          */
    public int lineWidth = 1;

    /** _more_          */
    public int arrow = 0;

    /** _more_          */
    public int circle = 0;


    /**
     * _more_
     *
     * @param view _more_
     * @param node _more_
     * @param tailId _more_
     * @param headId _more_
     */
    public GraphEdge(GraphView view, XmlNode node, String tailId,
                     String headId) {
        super(view, node);
        this.tailId   = tailId;
        this.headId   = headId;

        this.edgeType = getAttr(GraphView.ATTR_TYPE, "Edge");
        setType(graphView.getEdgeType(edgeType));

        id = getAttr(ATTR_ID);
        if (id == null) {
            id = tailId + "->" + headId + "->" + edgeType;
        }

        myColor   = getAttr(ATTR_COLOR, (Color) null);
        lineWidth = getAttr(ATTR_WIDTH, lineWidth);
        arrow     = getAttr(ATTR_ARROW, arrow);
        circle    = getAttr(ATTR_CIRCLE, circle);
        visible   = getAttr("visible", true);
    }


    /**
     * _more_
     */
    public void remove() {
        if (tail != null) {
            tail.removeOut(this);
        }
        if (head != null) {
            head.removeIn(this);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isDirectional() {
        return ((arrow != 0) || (circle != 0));
    }

    /**
     * _more_
     *
     * @param l _more_
     */
    public void setLevel(int l) {
        level = l;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getLevel() {
        if (true) {
            return level;
        }
        if (level == -1) {
            if ((head == null) && (tail == null)) {
                return level;
            }
            if ((head != null) && (head.level != -1)) {
                level = Math.max(level, head.level);
            }
            if ((tail != null) && (tail.level != -1)) {
                level = Math.max(level, tail.level);
            }
        }
        return level;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getTailId() {
        return tailId;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getHeadId() {
        return headId;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public GraphNode getHead() {
        return head;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public GraphNode getTail() {
        return tail;
    }

    /**
     * _more_
     *
     * @param tail _more_
     */
    public void setTail(GraphNode tail) {
        this.tail = tail;
        if (tail != null) {
            tail.addOutEdge(this);
            tailId = tail.getId();
        }
    }

    /**
     * _more_
     *
     * @param head _more_
     */
    public void setHead(GraphNode head) {
        this.head = head;
        if (head != null) {
            head.addInEdge(this);
            headId = head.getId();
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getVisible() {
        return visible;
    }


    /**
     * _more_
     *
     * @param v _more_
     */
    public void setVisible(boolean v) {
        visible = v;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return ((tail != null)
                ? tail.toString()
                : "null") + "->" + ((head != null)
                                    ? head.toString()
                                    : "null");
    }


    /**
     * _more_
     *
     * @param t _more_
     *
     * @return _more_
     */
    public boolean isTail(GraphNode t) {
        return t == tail;
    }


    /**
     * _more_
     *
     * @param butNot _more_
     *
     * @return _more_
     */
    public GraphNode getOtherNode(GraphNode butNot) {
        if (tail != butNot) {
            return tail;
        }
        return head;
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public int getLineWidth() {
        return lineWidth;
    }


    /**
     * _more_
     *
     * @param c _more_
     *
     * @return _more_
     */
    public int getCoord(int c) {
        if (c == HEADX) {
            return head.getInEdgeAnchor().x;
        }
        if (c == HEADY) {
            return head.getInEdgeAnchor().y;
        }
        if (c == TAILX) {
            return tail.getInEdgeAnchor().x;
        }
        if (c == TAILY) {
            return tail.getInEdgeAnchor().y;
        }
        return c;
    }

    /**
     * _more_
     *
     * @param gv _more_
     * @param g _more_
     * @param centerNode _more_
     * @param highlightNode _more_
     * @param onlyHighlight _more_
     */
    public void paint(GraphView gv, Graphics g, GraphNode centerNode,
                      GraphNode highlightNode, boolean onlyHighlight) {
        if(labelFont == null) {
            labelFont =  GraphGlyph.getFont("Dialog", Font.PLAIN, 12);
        }
        if ((level < 0) && !gv.getShowAllEdges()) {
            return;
        }
        if ((tail == null) || (head == null)) {
            return;
        }
        boolean isHighlight = ((head == highlightNode)
                               || (tail == highlightNode));
        if (( !isHighlight && onlyHighlight)
                || (isHighlight && !onlyHighlight)) {
            return;
        }

        if (isHighlight) {
            g.setColor(GraphView.highlightColor);
        } else {
            if (level < 0) {
                g.setColor(Color.lightGray);
            } else {
                if (myColor != null) {
                    g.setColor(myColor);
                } else {
                    g.setColor(Color.black);
                }
            }
        }


        Point tp  = null;
        Point hp  = gv.scalePoint(head.getInEdgeAnchor());

        int   slw = gv.scale(getLineWidth());

        String label = getLabel();

        /*                g.setFont(labelFont);
                g.setColor(Color.BLACK);
                g.drawString(graphView.getTitle(this), p1.x, p1.y);
        */

        if (points != null) {
            //            IfcApplet.debug("draw1");
            for (int i = 1; i < points.length; i++) {
                tp = gv.scalePoint(points[i - 1]);
                hp = gv.scalePoint(points[i]);
                GuiUtils.drawLine(g, tp.x, tp.y, hp.x, hp.y, slw);
            }
        } else if (joint != null) {
            //        IfcApplet.debug("draw2");
            tp = gv.scalePoint(tail.getOutEdgeAnchor());
            int jx = gv.scale(getCoord(joint.x));
            int jy = gv.scale(getCoord(joint.y));
            GuiUtils.drawLine(g, tp.x, tp.y, jx, jy, slw);
            GuiUtils.drawLine(g, hp.x, hp.y, jx, jy, slw);
            tp.x = jx;
            tp.y = jy;
        } else if ( !graphView.getLayoutRectilinear()) {
            //        IfcApplet.debug("draw3");
            Rectangle hb = gv.scaleRect(head.bounds);
            tp = gv.scalePoint(tail.getOutEdgeAnchor());
            GuiUtils.drawLine(g, tp.x, tp.y, hp.x, hp.y, slw);
        } else {
            tp = gv.scalePoint(tail.getOutEdgeAnchor());
            GuiUtils.drawLine(g, tp.x, tp.y, hp.x, tp.y, slw);
            GuiUtils.drawLine(g, hp.x, tp.y, hp.x, hp.y, slw);
            g.fillOval(hp.x - slw, tp.y - slw, slw * 2, slw * 2);
            tp.x = hp.x;
        }

        if (arrow != 0) {
            int size = gv.scale(((arrow < 0)
                                 ? -arrow
                                 : arrow));
            Point ap = new Point(tp.x + (int) ((double) (hp.x - tp.x) * 0.5),
                                 tp.y + (int) ((double) (hp.y - tp.y) * 0.5));
            double angle = GuiUtils.pointAngle(hp, tp);

            ap = GuiUtils.rotatePoint(ap, tp, -angle);

            int offset = ((ap.x < tp.x)
                          ? -size
                          : size);
            Point p1 = GuiUtils.rotatePoint(new Point(ap.x - offset,
                           ap.y - offset), tp, angle);
            Point p2 = GuiUtils.rotatePoint(new Point(ap.x, ap.y), tp, angle);
            Point p3 = GuiUtils.rotatePoint(new Point(ap.x - offset,
                           ap.y + offset), tp, angle);

            
            if (arrow < 0) {
                int[] xs = { p1.x, p2.x, p3.x, p1.x };
                int[] ys = { p1.y, p2.y, p3.y, p1.y };
                g.fillPolygon(xs, ys, xs.length);
            } else {
                GuiUtils.drawLine(g, p1.x, p1.y, p2.x, p2.y, 1);
                GuiUtils.drawLine(g, p2.x, p2.y, p3.x, p3.y, 1);
            }
            if (isHighlight) {
                g.setFont(labelFont);
                g.setColor(Color.BLACK);
                g.drawString(graphView.getTitle(this), p1.x, p1.y);
            }
        }
    }




}

