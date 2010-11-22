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

package com.infocetera.graph;


//import com.infocetera.util.ConfigurableGlyph;

import com.infocetera.util.GuiUtils;
import com.infocetera.util.XmlNode;
import com.infocetera.util.IfcApplet;

import java.awt.*;

import java.awt.image.ImageObserver;

import java.util.*;


/**
 */
public class GraphNode extends GraphGlyph implements ImageObserver {

    /** _more_          */
    private static XmlNode DFLTSHAPE;

    /** _more_          */
    public static final int DIR_OUT = 0;

    /** _more_          */
    public static final int DIR_IN = 1;

    /** _more_          */
    public static final int DIR_BOTH = 2;

    /** _more_          */
    public static final String ATTR_TOOLTIP = "tooltip";

    /** _more_          */
    public static final String ATTR_LOADED = "loaded";

    /** _more_          */
    public static final String ATTR_TITLE = "title";

    /** _more_          */
    public static final String ATTR_URL = "url";

    /** _more_          */
    public static final String ATTR_EDGES = "edges";

    /** _more_          */
    public static final String ATTR_MOUSEOVER = "mouse";

    /** _more_          */
    public static final String ATTR_TYPE = "type";

    /** _more_          */
    public boolean haveLoadedGraph = false;

    /** _more_          */
    private boolean isCenter = false;

    /** _more_          */
    private boolean beenCenter = false;

    /** _more_          */
    private boolean isHilite = false;

    /** _more_          */
    private boolean beenHilite = false;


    /** _more_          */
    private Vector shapes;



    /**
     *  Is this node in need of a layout
     */
    public boolean needsLayout = true;

    /**
     *  Is this being drawn, i.e., has it been laid out on the screen.
     */
    public boolean visible = false;




    /**
     *  Is this node currently hidden (based on node type)
     */
    public boolean elided = false;




    /** _more_          */
    public int level = 0;

    /** _more_          */
    private int prevLevel = 0;

    /** _more_          */
    public boolean haveOthers = false;

    /** _more_          */
    public boolean haveOthersNotLoaded = false;

    /** _more_          */
    public String nodeTypeName;

    /** _more_          */
    public String mouseOver;

    /** _more_          */
    Vector outEdges = new Vector();

    /** _more_          */
    Vector inEdges = new Vector();

    /** _more_          */
    Vector allEdges;

    /** _more_          */
    public Rectangle bounds = new Rectangle();

    /** _more_          */
    int currentX = 0;

    /** _more_          */
    int currentY = 0;

    /** _more_          */
    double destX = 0.0;

    /** _more_          */
    double destY = 0.0;

    /** _more_          */
    int originalX = 0;

    /** _more_          */
    int originalY = 0;

    /** _more_          */
    double dx = 0.0;

    /** _more_          */
    double dy = 0.0;

    /**
     * _more_
     *
     * @param gv _more_
     * @param node _more_
     */
    public GraphNode(GraphView gv, XmlNode node) {
        super(gv, node);
        this.graphView  = gv;

        haveLoadedGraph = node.getAttribute(ATTR_LOADED, false);
        setDisplayAttributes();

        elided = getAttr("visible", true);
        //Process any sub doms of the form:
        //<to node=\"node id\" type=\"edge type\" />
        Vector children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            XmlNode child = (XmlNode) children.elementAt(i);
            if ( !child.getTag().equals(GraphView.TAG_EDGE)) {
                continue;
            }
            child.addAttribute(GraphView.ATTR_FROM, id);
            graphView.processEdge(child);
        }
    }

    /**
     * _more_
     *
     * @param template _more_
     *
     * @return _more_
     */
    public String processTemplate(String template) {
        StringBuffer buff = new StringBuffer();
        while (true) {
            int idx1 = template.indexOf("%");
            if (idx1 < 0) {
                break;
            }
            int idx2 = template.indexOf("%", idx1 + 1);
            if (idx2 < 0) {
                break;
            }
            buff.append(template.substring(0, idx1));
            String macro = template.substring(idx1 + 1, idx2);
            buff.append(getAttr(macro, ""));
            template = template.substring(idx2 + 1);
        }
        buff.append(template);
        return buff.toString();
    }


    /**
     * _more_
     *
     * @param newNode _more_
     */
    public void merge(XmlNode newNode) {
        xmlNode.mergeAttributes(newNode);
        setDisplayAttributes();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Point getInEdgeAnchor() {
        return getOutEdgeAnchor();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Point getOutEdgeAnchor() {
        if(true) return new Point(currentX, currentY);
        for (int i = 0; i < shapes.size(); i++) {
            GraphShape shape = (GraphShape) shapes.elementAt(i);
            if (shape.getVisible() && shape.getConnectable()) {
                return GuiUtils.getPointOnRect(GuiUtils.PT_C, shape.bounds);
            }
        }
        return new Point(currentX, currentY);
    }

    /**
     * _more_
     *
     * @param v _more_
     */
    public void setIsHilite(boolean v) {
        if (v) {
            beenHilite = true;
        }
        if (isHilite != v) {
            isHilite = v;
            setDisplayAttributes();
        }
    }

    /**
     * _more_
     *
     * @param v _more_
     */
    public void setIsCenterNode(boolean v) {
        boolean doUpdate = false;
        beenCenter = true;
        if (isCenter != v) {
            isCenter = v;
            setDisplayAttributes();
        }
    }


    /**
     * _more_
     *
     * @param img _more_
     * @param flags _more_
     * @param x _more_
     * @param y _more_
     * @param width _more_
     * @param height _more_
     *
     * @return _more_
     */
    public boolean imageUpdate(Image img, int flags, int x, int y, int width,
                               int height) {
        if ((flags & ImageObserver.ERROR) != 0) {
            graphView.graphApplet.debug(
                "Image error:" + graphView.graphApplet.getImagePath(img));
            return false;
        }

        dirty = true;
        if ((flags & ImageObserver.ALLBITS) != 0) {
            graphView.repaint();
            return false;
        }
        return true;
    }

    /**
     * _more_
     *
     * @param s _more_
     */
    public void setScale(double s) {
        super.setScale(s);
        clearShapeFont();
    }

    /**
     * _more_
     */
    public void clearShapeFont() {
        for (int i = 0; i < shapes.size(); i++) {
            GraphShape shape = (GraphShape) shapes.elementAt(i);
            shape.clearFont();
        }
    }

    /**
     * _more_
     */
    protected void setShapeVisibility() {
        dirty = true;

        for (int i = 0; i < shapes.size(); i++) {
            GraphShape shape = (GraphShape) shapes.elementAt(i);
            shape.checkVisibility();
        }
        //    GraphShape.debug = false;
    }


    /**
     * _more_
     */
    public void setDisplayAttributes() {
        setAttr("iscenter", "" + isCenter);
        setAttr("beencenter", "" + beenCenter);
        setAttr("ishilite", "" + isHilite);
        setAttr("beenhilite", "" + beenHilite);
        setAttr("beenloaded", "" + haveLoadedGraph);

        nodeTypeName = getAttr(ATTR_TYPE, "Node");
        setType(graphView.getNodeType(nodeTypeName, this));
        id        = getAttr(ATTR_ID, "");
        mouseOver = getAttr(ATTR_MOUSEOVER);

        Vector shapeTypes = new Vector();
        for (int i = 0; i < types.size(); i++) {
            XmlNode type = (XmlNode) types.elementAt(i);
            GuiUtils.addAll(shapeTypes, GraphShape.getShapeChildren(type));
        }
        GuiUtils.addAll(shapeTypes, GraphShape.getShapeChildren(xmlNode));

        shapes = new Vector();
        for (int i = 0; i < shapeTypes.size(); i++) {
            XmlNode shapeNode = (XmlNode) shapeTypes.elementAt(i);
            shapes.addElement(new GraphShape(this, shapeNode));
        }
        if (shapes.size() == 0) {
            shapes.addElement(new GraphShape(this, getDefaultShapeXml()));
        }
        setShapeVisibility();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    static XmlNode getDefaultShapeXml() {
        if (DFLTSHAPE == null) {
            String xml =
                "<shape>"
                + "<text id=\"label\" flags=\"iscenter\" fontstyle=\"bold\"  textmaxlines=\"3\" text=\"%title%\" from=\"c\" to=\"c\" color=\"black\"  dy=\"2\"/>"
                + "<text id=\"label\" flags=\"!iscenter\"  text=\"%title%\" from=\"c\" to=\"c\" color=\"black\"  dy=\"2\"/>"
                + "<rrect src=\"label\" from=\"c\" to=\"c\" fillcolor=\"%fillcolor%\" color=\"black\"/>"
                + "</shape>";
            DFLTSHAPE = XmlNode.parse(xml).get(0);
        }
        return DFLTSHAPE.copy();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getIsCenter() {
        return isCenter;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getCenterX() {
        return bounds.x + bounds.width / 2;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getCenterY() {
        return bounds.y + bounds.height / 2;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getNeedsLayout() {
        return needsLayout;
    }

    /**
     * _more_
     *
     * @param v _more_
     */
    public void setNeedsLayout(boolean v) {
        needsLayout = v;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getTypeName() {
        return nodeTypeName;
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
     * _more_
     *
     * @return _more_
     */
    public Vector getOutEdges() {
        return outEdges;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Vector getInEdges() {
        return inEdges;
    }


    /**
     * _more_
     *
     * @param edge _more_
     */
    public void removeOut(GraphEdge edge) {
        outEdges.removeElement(edge);
        allEdges = null;
    }

    /**
     * _more_
     *
     * @param edge _more_
     */
    public void removeIn(GraphEdge edge) {
        inEdges.removeElement(edge);
        allEdges = null;
    }

    /**
     * _more_
     */
    public void remove() {
        Vector edges = getAllEdges();
        for (int i = 0; i < edges.size(); i++) {
            GraphEdge edge = (GraphEdge) edges.elementAt(i);
            edge.remove();
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getWeight() {
        return getAttr("weight", getNumEdges());
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getNumEdges() {
        return getAllEdges().size();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Vector getAllEdges() {
        if (allEdges == null) {
            allEdges = merge(outEdges, inEdges);
        }
        return allEdges;
    }

    /**
     * _more_
     *
     * @param v1 _more_
     * @param v2 _more_
     *
     * @return _more_
     */
    public Vector merge(Vector v1, Vector v2) {
        Vector merged = (Vector) v1.clone();
        for (int i = 0; i < v2.size(); i++) {
            merged.addElement(v2.elementAt(i));
        }
        return merged;
    }

    /**
     * _more_
     *
     * @param edge _more_
     */
    public void addOutEdge(GraphEdge edge) {
        if ( !outEdges.contains(edge)) {
            outEdges.addElement(edge);
            allEdges = null;
        }
    }

    /**
     * _more_
     *
     * @param edge _more_
     */
    public void addInEdge(GraphEdge edge) {
        if ( !inEdges.contains(edge)) {
            inEdges.addElement(edge);
            allEdges = null;
        }
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return graphView.getTitle(this) + "(" + getAllEdges().size() + ")";
    }


    /**
     * _more_
     *
     * @param level _more_
     * @param direction _more_
     *
     * @return _more_
     */
    public Vector[] getNodesToLayout(int level, int direction) {
        this.level = level;
        Vector nodes        = new Vector();
        Vector visitedEdges = new Vector();
        if (level > graphView.maxLevel) {
            return new Vector[] { nodes, visitedEdges };
        }
        Vector edges = ((direction == DIR_OUT)
                        ? outEdges
                        : ((direction == DIR_IN)
                           ? inEdges
                           : getAllEdges()));
        for (int i = 0; i < edges.size(); i++) {
            GraphEdge edge = (GraphEdge) edges.elementAt(i);
            if ( !edge.getVisible()) {
                continue;
            }
            graphView.checkEdgeForNodeLoad(edge);
            GraphNode other = edge.getOtherNode(this);
            if ((other == null) || !other.needsLayout || other.elided) {
                continue;
            }
            graphView.setIsLaidOut(other);
            edge.setLevel(level);
            other.level = level + 1;
            nodes.addElement(other);
            visitedEdges.addElement(edge);
        }
        return new Vector[] { nodes, visitedEdges };
    }


    /**
     * _more_
     *
     * @param i _more_
     *
     * @return _more_
     */
    public GraphEdge getInEdge(int i) {
        return (GraphEdge) inEdges.elementAt(i);
    }

    /**
     * _more_
     *
     * @param i _more_
     *
     * @return _more_
     */
    public GraphEdge getOutEdge(int i) {
        return (GraphEdge) outEdges.elementAt(i);
    }

    /**
     * _more_
     *
     * @param i _more_
     *
     * @return _more_
     */
    public GraphNode getTailX(int i) {
        return ((GraphEdge) inEdges.elementAt(i)).getTail();
    }

    /**
     * _more_
     *
     * @param i _more_
     *
     * @return _more_
     */
    public GraphNode getHeadX(int i) {
        return ((GraphEdge) outEdges.elementAt(i)).getHead();
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isRoot() {
        return inEdges.size() == 0;
    }


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public GraphShape contains(int x, int y) {
        for (int i = 0; i < shapes.size(); i++) {
            GraphShape shape         = (GraphShape) shapes.elementAt(i);
            GraphShape containsShape = shape.contains(x, y);
            if (containsShape != null) {
                return containsShape;
            }
        }
        return null;
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
     * @param v _more_
     */
    public void setVisible(boolean v) {
        if (v != visible) {
            dirty = true;
        }
        visible = v;
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
     * @param x _more_
     * @param y _more_
     */
    public void setCenter(int x, int y) {
        moveBy(x - this.currentX, y - this.currentY);
    }


    /**
     * _more_
     *
     * @param dx _more_
     * @param dy _more_
     */
    public void moveBy(int dx, int dy) {
        this.currentX += dx;
        this.currentY += dy;
        bounds.x      += dx;
        bounds.y      += dy;
        for (int i = 0; i < shapes.size(); i++) {
            GraphShape shape = (GraphShape) shapes.elementAt(i);
            shape.moveBy(dx, dy);
        }
    }


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void setDest(double x, double y) {
        this.destX = x;
        this.destY = y;
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void moveDestBy(double x, double y) {
        this.destX += x;
        this.destY += y;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getRadius() {
        if (bounds.width > bounds.height) {
            return bounds.width / 2;
        }
        return bounds.height / 2;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getRadiusY() {
        return bounds.height / 2;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getRadiusX() {
        return bounds.width / 2;
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void calculateBounds(Graphics g) {
        if ((level > 1) && graphView.getScaleWithLevel()) {
            double newLevelScale = 1.0 - .1 * level;
            if (newLevelScale != levelScale) {
                clearShapeFont();
                levelScale = newLevelScale;
            }
        } else {
            levelScale = 1.0;
        }

        bounds = new Rectangle(currentX, currentY, 1, 1);
        Rectangle tmpBounds = new Rectangle(currentX, currentY, 1, 1);
        int       cnt       = 0;
        Hashtable<String,Rectangle> boundsMap = new Hashtable<String,Rectangle>();

        //        IfcApplet.debug("graphNode: x=" +  currentX +" y=" + currentY);
        for (int i = 0; i < shapes.size(); i++) {
            GraphShape shape = (GraphShape) shapes.elementAt(i);
            if ( !shape.getVisible()) {
                continue;
            }
            shape.calculateBounds(g, boundsMap, bounds);
            if (cnt++ == 0) {
                tmpBounds = new Rectangle(shape.getBounds());
            } else {
                tmpBounds = bounds.union(shape.getBounds());
            }
        }
        bounds = new Rectangle(tmpBounds);
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void cleanup(Graphics g) {
        if (dirty) {
            dirty = false;
            calculateBounds(g);
        }
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void postLayout(Graphics g) {
        if (prevLevel != level) {
            dirty = true;
        }
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void preLayout(Graphics g) {
        setNeedsLayout(true);
        haveOthers          = false;
        haveOthersNotLoaded = false;
        cleanup(g);
        prevLevel = level;
        level     = -1;
        originalX = currentX;
        originalY = currentY;
    }

    /**
     * _more_
     *
     * @param ga _more_
     * @param g _more_
     * @param select _more_
     * @param isHighlight _more_
     */
    public void paint(GraphView ga, Graphics g, boolean select,
                      boolean isHighlight) {
        cleanup(g);

        for (int i = 0; i < shapes.size(); i++) {
            GraphShape shape = (GraphShape) shapes.elementAt(i);
            shape.paint(g, isHighlight);
        }

        //    g.setColor (Color.green);
        //    g.drawRect (currentX-4,currentY-4,8,8);

    }




}

