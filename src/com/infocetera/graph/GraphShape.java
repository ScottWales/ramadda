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


import com.infocetera.util.GuiUtils;
import com.infocetera.util.XmlNode;
import com.infocetera.util.XmlUi;

import java.awt.*;

import java.awt.image.ImageObserver;

import java.util.*;


/**
 */
public class GraphShape {



    /** _more_          */
    public static final int LAYOUT_NONE = 0;

    /** _more_          */
    public static final int LAYOUT_V = 1;

    /** _more_          */
    public static final int LAYOUT_H = 2;

    /** _more_          */
    public static final int LAYOUT_GRID = 3;

    /** _more_          */
    public static final String[] LAYOUTS = { "none", "v", "h", "grid" };


    /** _more_          */
    public static final Rectangle EMPTY_RECTANGLE = new Rectangle(0, 0, 0, 0);

    /** _more_          */
    double lastScale = 1.0;

    /** _more_          */
    Font myFont = null;

    /** _more_          */
    Font myScaledFont = null;

    /** _more_          */
    int fontSize = -1;

    /** _more_          */
    String fontFace = "Dialog";

    /** _more_          */
    int fontStyle = Font.PLAIN;


    /** _more_          */
    boolean okToDrawText = true;

    /** _more_          */
    int fromAnchor;

    /** _more_          */
    int toAnchor;

    /** _more_          */
    int dx = 0;

    /** _more_          */
    int dy = 0;


    /** _more_          */
    public String id;

    /** _more_          */
    private String[] flags;

    /** _more_          */
    private boolean[] flagIsProperty;

    /** _more_          */
    private boolean[] flagIsNot;

    /** _more_          */
    private boolean flagsAreOr;



    /** _more_          */
    public static final int SHAPE_NONE = 0;

    /** _more_          */
    public static final int SHAPE_PARENT = 1;

    /** _more_          */
    public static final int SHAPE_IMAGE = 2;

    /** _more_          */
    public static final int SHAPE_TEXT = 3;

    /** _more_          */
    public static final int SHAPE_RECT = 4;

    /** _more_          */
    public static final int SHAPE_RRECT = 5;

    /** _more_          */
    public static final int SHAPE_OVAL = 6;

    /** _more_          */
    public static final int SHAPE_CIRCLE = 7;

    /** _more_          */
    public static final int SHAPE_TRIANGLE = 8;

    /** _more_          */
    public static final int SHAPE_3DRECT = 9;

    /** _more_          */
    public static final int SHAPE_BARREL = 10;

    /** _more_          */
    public static String[] SHAPES = {
        "none", "parent", "image", "text", "rect", "rrect", "oval", "circle",
        "triangle", "3drect", "barrel"
    };


    /** _more_          */
    public static final String ATTR_FONTFACE = "fontface";

    /** _more_          */
    public static final String ATTR_FONTSIZE = "fontsize";

    /** _more_          */
    public static final String ATTR_FONTSTYLE = "fontstyle";

    /** _more_          */
    public static final int SRC_NODE = 0;

    /** _more_          */
    public static final int SRC_SIBLING = 1;

    /** _more_          */
    public static final int SRC_ID = 2;



    /** _more_          */
    private boolean connectable;

    /** _more_          */
    private boolean initVisible = true;

    /** _more_          */
    private boolean visible = true;

    /** _more_          */
    private Color color;

    /** _more_          */
    private Color fillColor;

    /** _more_          */
    private Color[] borderColors;

    /** _more_          */
    private Insets insets;

    /** _more_          */
    private int borderType;


    /** _more_          */
    private int layoutType = LAYOUT_NONE;

    /** _more_          */
    private int layoutRows = -1;

    /** _more_          */
    private int layoutCols = -1;

    /** _more_          */
    private int hGap = 0;

    /** _more_          */
    private int vGap = 0;


    /** _more_          */
    private int shapeType;

    /** _more_          */
    private String shapeName;

    /** _more_          */
    GraphShape parent;

    /** _more_          */
    GraphNode glyph;

    /** _more_          */
    GraphView gv;


    /** _more_          */
    int boundsSrc = SRC_NODE;

    /** _more_          */
    String boundsSrcId;


    /** _more_          */
    Image image;

    /** _more_          */
    String imageUrl;

    /** _more_          */
    Rectangle bounds = new Rectangle(0, 0, 0, 0);

    /** _more_          */
    Rectangle searchBounds = new Rectangle(0, 0, 0, 0);

    /** _more_          */
    String text;

    /** _more_          */
    String href;

    /** _more_          */
    String alt;



    /** _more_          */
    String[] textLines;

    /** _more_          */
    int textWidth;

    /** _more_          */
    int maxTextWidth = 0;

    /** _more_          */
    int textMaxLines;

    /** _more_          */
    int align;

    /** _more_          */
    int width;

    /** _more_          */
    int height;

    /** _more_          */
    Vector childShapes = new Vector();

    /** _more_          */
    XmlNode xmlNode;


    /**
     * _more_
     *
     * @param glyph _more_
     * @param xmlNode _more_
     */
    public GraphShape(GraphNode glyph, XmlNode xmlNode) {
        this(null, glyph, xmlNode);
    }


    /**
     * _more_
     *
     * @param parent _more_
     * @param glyph _more_
     * @param xmlNode _more_
     */
    public GraphShape(GraphShape parent, GraphNode glyph, XmlNode xmlNode) {
        this.parent  = parent;
        this.glyph   = glyph;
        this.gv      = glyph.graphView;
        this.xmlNode = xmlNode;
        init();
    }


    /**
     * _more_
     *
     * @param n _more_
     *
     * @return _more_
     */
    public String getAttr(String n) {
        String v = xmlNode.getAttribute(n, (String) null);
        if (v != null) {
            v = glyph.processTemplate(v);
        }
        return v;
    }

    /**
     * _more_
     *
     * @param n _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getAttr(String n, String dflt) {
        String v = getAttr(n);
        return ((v == null)
                ? dflt
                : v);
    }


    /**
     * _more_
     *
     * @param n _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public int getAttr(String n, int dflt) {
        String v = getAttr(n);
        return ((v == null)
                ? dflt
                : new Integer(v).intValue());
    }

    /**
     * _more_
     *
     * @param n _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean getAttr(String n, boolean dflt) {
        String v = getAttr(n);
        return ((v == null)
                ? dflt
                : new Boolean(v).booleanValue());
    }

    /**
     * _more_
     *
     * @param n _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public Color getAttr(String n, Color dflt) {
        String v = getAttr(n);
        return ((v == null)
                ? dflt
                : GuiUtils.getColor(v, dflt));
    }


    /**
     * _more_
     */
    private void init() {

        fontSize  = getAttr("fontsize", -1);
        fontStyle = -1;
        fontFace  = getAttr("fontface");
        String sFontStyle = getAttr("fontstyle");
        if (sFontStyle != null) {
            sFontStyle = sFontStyle.toUpperCase();
            if (sFontStyle.equals("ITALIC")) {
                fontStyle = Font.ITALIC;
            } else if (sFontStyle.equals("BOLD")) {
                fontStyle = Font.BOLD;
            } else if (sFontStyle.equals("BOLDITALIC")) {
                fontStyle = Font.BOLD | Font.ITALIC;
            }
        }

        String src = getAttr("src");
        if ((src == null) || src.equals("node")) {
            boundsSrc = SRC_NODE;
        } else if (src.equals("sibling")) {
            boundsSrc = SRC_SIBLING;
        } else {
            boundsSrc   = SRC_ID;
            boundsSrcId = src;
        }

        Vector shapeNodes = xmlNode.getChildren();
        for (int i = 0; i < shapeNodes.size(); i++) {
            XmlNode child = (XmlNode) shapeNodes.elementAt(i);
            if (isShapeNode(child)) {
                childShapes.addElement(new GraphShape(this, glyph, child));
            }
        }

        String flagsString = getAttr("flags");
        if (flagsString != null) {
            flagsAreOr = flagsString.indexOf("|") >= 0;
            StringTokenizer tok = new StringTokenizer(flagsString, (flagsAreOr
                    ? "|"
                    : "&"));
            flags          = new String[tok.countTokens()];
            flagIsProperty = new boolean[tok.countTokens()];
            flagIsNot      = new boolean[tok.countTokens()];
            int cnt = 0;
            while (tok.hasMoreTokens()) {
                String flag = tok.nextToken();
                flagIsNot[cnt] = flag.startsWith("!");
                if (flagIsNot[cnt]) {
                    flag = flag.substring(1);
                }
                flagIsProperty[cnt] = flag.startsWith("prop:");
                if (flagIsProperty[cnt]) {
                    flag = flag.substring(5);
                }
                if (flag.equals("ishilite") || flag.equals("beenhilite")
                        || flag.equals("iscenter")
                        || flag.equals("beencenter")
                        || flag.equals("beenloaded")) {
                    flagIsProperty[cnt] = true;
                }
                flags[cnt] = flag;
                cnt++;
            }
        }

        connectable = getAttr("connectable", true);
        initVisible = visible = getAttr("visible", true);
        id          = getAttr("id");


        color       = getAttr("color", (Color) null);
        fillColor   = getAttr("fillcolor", (Color) null);




        borderType = GuiUtils.getIndex(GuiUtils.BORDERS, null,
                                       getAttr("border", "none"),
                                       GuiUtils.BORDER_EMPTY);

        int margin = 0;
        if ((borderType == GuiUtils.BORDER_RAISED)
                || (borderType == GuiUtils.BORDER_SUNKEN)
                || (borderType == GuiUtils.BORDER_ETCHED)) {
            margin = 2;
        }
        margin = getAttr("margin", margin);

        insets = new Insets(getAttr("margin-top", margin),
                            getAttr("margin-left", margin),
                            getAttr("margin-bottom", margin),
                            getAttr("margin-right", margin));

        Color borderColor = getAttr("border-color", (Color) null);
        borderColors = new Color[] { getAttr("border-color-top", borderColor),
                                     getAttr("border-color-left",
                                             borderColor),
                                     getAttr("border-color-bottom",
                                             borderColor),
                                     getAttr("border-color-right",
                                             borderColor) };



        fromAnchor = GuiUtils.getIndex(GuiUtils.PTS, null, getAttr("from"),
                                       GuiUtils.PT_NONE);
        toAnchor = GuiUtils.getIndex(GuiUtils.PTS, null, getAttr("to"),
                                     GuiUtils.PT_NONE);

        dx        = getAttr("dx", 0);
        dy        = getAttr("dy", 0);
        shapeName = getAttr("type", (String) null);
        if (shapeName == null) {
            shapeName = xmlNode.getTag();
        }


        layoutType = GuiUtils.getIndex(LAYOUTS, null, getAttr("layout"),
                                       LAYOUT_NONE);
        int gap = getAttr("gap", 0);
        hGap = getAttr("hgap", gap);
        vGap = getAttr("vgap", gap);
        if (layoutType == LAYOUT_GRID) {
            layoutRows = getAttr("rows", 0);
            layoutCols = getAttr("cols", 0);
        }

        align     = XmlUi.getAlign(getAttr("align", ""));
        shapeType = GuiUtils.getIndex(SHAPES, null, shapeName, SHAPE_NONE);
        width     = getAttr("width", -1);
        height    = getAttr("height", -1);
        href      = getAttr("href");
        alt       = getAttr("alt");


        if (shapeType == SHAPE_IMAGE) {
            imageUrl = getAttr("url");
        } else if (shapeType == SHAPE_TEXT) {
            textMaxLines = getAttr("textmaxlines", 1);
            textWidth    = getAttr("textwidth", -1);
            text         = getAttr("text", "");
        }

    }

    /**
     * _more_
     *
     * @param xmlNode _more_
     *
     * @return _more_
     */
    public static Vector getShapeChildren(XmlNode xmlNode) {
        Vector children   = new Vector();
        Vector shapeNodes = xmlNode.getChildren();
        for (int i = 0; i < shapeNodes.size(); i++) {
            XmlNode child = (XmlNode) shapeNodes.elementAt(i);
            if (isShapeNode(child)) {
                children.addElement(child);
            }
        }
        return children;
    }

    /**
     * _more_
     *
     * @param child _more_
     *
     * @return _more_
     */
    public static boolean isShapeNode(XmlNode child) {
        String tag = child.getTag();
        if (tag.equals("shape")) {
            return true;
        }
        for (int i = 0; i < SHAPES.length; i++) {
            if (tag.equals(SHAPES[i])) {
                return true;
            }
        }
        return false;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getFontStyle() {
        return (fontStyle >= 0)
               ? fontStyle
               : ((parent != null)
                  ? parent.getFontStyle()
                  : Font.PLAIN);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getFontSize() {
        return (fontSize >= 0)
               ? fontSize
               : ((parent != null)
                  ? parent.getFontSize()
                  : 12);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getFontFace() {
        return (fontFace != null)
               ? fontFace
               : ((parent != null)
                  ? parent.getFontFace()
                  : "Dialog");
    }



    /**
     * _more_
     */
    public void clearFont() {
        myFont       = null;
        myScaledFont = null;
        for (int i = 0; i < childShapes.size(); i++) {
            GraphShape childShape = (GraphShape) childShapes.elementAt(i);
            childShape.clearFont();
        }
    }



    /**
     * _more_
     *
     * @param g _more_
     *
     * @return _more_
     */
    public Font getFont(Graphics g) {
        if (myFont == null) {
            myFont = GraphGlyph.getFont(getFontFace(), getFontStyle(),
                                        getFontSize());
        }
        return myFont;
    }


    /**
     * _more_
     *
     * @param g _more_
     *
     * @return _more_
     */
    public Font getScaledFont(Graphics g) {
        double fontScale = glyph.levelScale * glyph.scale;
        if ((lastScale == fontScale) && (myScaledFont != null)) {
            return myScaledFont;
        }
        lastScale    = fontScale;
        myScaledFont = getFont(g);
        okToDrawText = true;
        if (fontScale == 1.0) {
            return myScaledFont;
        } else if (fontScale > 1.0) {
            return myScaledFont =
                GraphGlyph.getFont(myScaledFont.getFamily(),
                                   myScaledFont.getStyle(),
                                   (int) (myScaledFont.getSize()
                                          * fontScale));
        } else {
            String testString =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ";
            g.setFont(myScaledFont);
            FontMetrics fm   = g.getFontMetrics();
            int scaledWidth  = (int) (fontScale * fm.stringWidth(testString));
            int         size = myScaledFont.getSize();
            //      if (debugfont)
            //      System.err.println ("get font:");
            while (true) {
                if (--size < 6) {
                    okToDrawText = false;
                    break;
                }
                myScaledFont = GraphGlyph.getFont(myScaledFont.getFamily(),
                        myScaledFont.getStyle(), size);
                g.setFont(myFont);
                fm = g.getFontMetrics();
                //      if (debugfont)
                //        System.err.println ("\t"+ fm + " " +  fm.stringWidth (testString));
                if (fm.stringWidth(testString) <= scaledWidth) {
                    break;
                }
            }
            debugfont = false;
        }
        okToDrawText = (fontScale > 0.6);
        //    okToDrawText = true;
        return myScaledFont;
    }

    /** _more_          */
    static boolean debugfont = true;




    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getConnectable() {
        return connectable;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getVisible() {
        return visible;
    }

    /** _more_          */
    public static boolean debug = false;

    /**
     * _more_
     *
     * @return _more_
     */
    String tab() {
        if (parent != null) {
            return "  " + parent.tab();
        }
        return "";
    }

    /**
     * _more_
     *
     * @param s _more_
     */
    void pr(String s) {
        if (debug) {
            System.err.println(tab() + "(" + shapeName + "):" + s);
        }
    }

    /**
     * _more_
     */
    public void checkVisibility() {
        visible = initVisible;

        if (flags != null) {
            boolean ok = (flagsAreOr
                          ? false
                          : true);
            for (int i = 0; i < flags.length; i++) {
                boolean v = false;
                if (flagIsProperty[i]) {
                    v = glyph.getAttr(flags[i], initVisible);
                } else {
                    v = gv.getShapeVisibility(flags[i], initVisible);
                }
                if (flagIsNot[i]) {
                    v = !v;
                }
                if (flagsAreOr) {
                    if (v) {
                        ok = true;
                    }
                    break;
                } else {
                    if ( !v) {
                        ok = false;
                        break;
                    }
                }
            }
            visible = ok;
        }


        for (int i = 0; i < childShapes.size(); i++) {
            GraphShape childShape = (GraphShape) childShapes.elementAt(i);
            childShape.checkVisibility();
        }

        if (visible && (imageUrl != null) && (image == null)) {
            makeImage();
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isParent() {
        return childShapes.size() > 0;
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
        if ( !visible) {
            return null;
        }
        if ((shapeType == SHAPE_TEXT)
                || ((shapeType == SHAPE_IMAGE) && (image != null))
                || ((fillColor != null) && !isParent())) {
            if (searchBounds.contains(x, y)) {
                return this;
            }
        }
        for (int i = 0; i < childShapes.size(); i++) {
            GraphShape childShape    = (GraphShape) childShapes.elementAt(i);
            GraphShape containsShape = childShape.contains(x, y);
            if (containsShape != null) {
                return containsShape;
            }

        }
        if ((fillColor != null) && isParent()) {
            if (searchBounds.contains(x, y)) {
                return this;
            }
        }
        return null;
    }



    /**
     * _more_
     */
    private void makeImage() {
        if ((image != null) || (imageUrl == null)) {
            return;
        }

        image = glyph.graphView.graphApplet.getImage(imageUrl);
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void moveTo(int x, int y) {
        moveBy(x - bounds.x, y - bounds.y);
    }

    /**
     * _more_
     *
     * @param dx _more_
     * @param dy _more_
     */
    public void moveBy(int dx, int dy) {
        for (int i = 0; i < childShapes.size(); i++) {
            GraphShape childShape = (GraphShape) childShapes.elementAt(i);
            childShape.moveBy(dx, dy);
        }
        bounds.x       += dx;
        bounds.y       += dy;
        searchBounds.x += dx;
        searchBounds.y += dy;
    }


    /**
     * _more_
     */
    private void initBounds() {
        bounds.x      -= insets.left;
        bounds.y      -= insets.top;
        bounds.width  += 2 * (insets.right + insets.left);
        bounds.height += 2 * (insets.top + insets.bottom);
        if (glyph.levelScale != 1.0) {
            bounds.width  = (int) (glyph.levelScale * bounds.width + 0.5);
            bounds.height = (int) (glyph.levelScale * bounds.height + 0.5);
        }

        searchBounds = new Rectangle(bounds);
        if (shapeType == SHAPE_RRECT) {
            int radius = searchBounds.height / 2;
            searchBounds.x     -= radius;
            searchBounds.width += 2 * radius;
        }
    }


    /**
     * _more_
     *
     * @param base _more_
     * @param g _more_
     */
    private void setDimensions(Rectangle base, Graphics g) {

        bounds = new Rectangle(EMPTY_RECTANGLE);
        if (isParent()) {
            int cnt = 0;
            for (int i = 0; i < childShapes.size(); i++) {
                GraphShape childShape = (GraphShape) childShapes.elementAt(i);
                if ( !childShape.visible) {
                    continue;
                }
                if (cnt++ == 0) {
                    bounds = new Rectangle(childShape.searchBounds);
                } else {
                    bounds = bounds.union(childShape.searchBounds);
                }
            }
            return;
        }

        switch (shapeType) {

          case SHAPE_IMAGE :
              if (image == null) {
                  makeImage();
              }
              if (image != null) {
                  bounds = new Rectangle(0, 0, image.getWidth(glyph),
                                         image.getHeight(glyph));
              }
              break;

          case SHAPE_TEXT :
              g.setFont(getFont(g));
              Vector lines = new Vector();
              if (textWidth > 0) {
                  String       tmpText = text;
                  char[]       chars   = text.toCharArray();
                  StringBuffer sb      = null;
                  int          cnt     = 0;

                  for (int i = 0; i < chars.length; i++) {
                      if (sb == null) {
                          cnt = 0;
                          sb  = new StringBuffer();
                      }
                      if (chars[i] == '\n') {
                          lines.addElement(sb.toString());
                          sb = null;
                          continue;
                      }
                      sb.append(chars[i]);
                      if (i == chars.length - 1) {
                          lines.addElement(sb.toString());
                          continue;
                      }

                      cnt++;
                      if (cnt > textWidth) {
                          if ((chars[i] != ' ') && (chars[i + 1] != ' ')
                                  && (chars[i + 1] != '\n')) {
                              if ((i < chars.length - 2)
                                      && ((chars[i + 2] == ' ')
                                          || (chars[i + 2] == '\n'))) {
                                  sb.append(chars[i + 1]);
                                  i++;
                              } else {
                                  sb.append("-");
                              }
                          }
                          lines.addElement(sb.toString());
                          sb = null;
                      }
                  }
                  if (lines.size() == 0) {
                      lines.addElement(" ");
                  }
              } else {
                  StringTokenizer tok = new StringTokenizer(text, "\n");
                  while (tok.hasMoreTokens()) {
                      lines.addElement(tok.nextToken());
                  }
              }
              int    max  = ( !gv.getDrawAllLines()
                              ? Math.min(textMaxLines, lines.size())
                              : lines.size());

              Vector tmpV = new Vector();
              for (int i = 0; i < max; i++) {
                  String line = ((String) lines.elementAt(i)).trim();
                  if (line.length() == 0) {
                      line = " ";
                  }
                  tmpV.addElement(line);
              }
              lines          = tmpV;

              this.textLines = new String[lines.size()];
              FontMetrics fm = g.getFontMetrics();
              maxTextWidth = 0;
              for (int i = 0; i < textLines.length; i++) {
                  textLines[i] = (String) lines.elementAt(i);
                  maxTextWidth = Math.max(maxTextWidth,
                                          fm.stringWidth(textLines[i]));
              }
              bounds.width = maxTextWidth;
              bounds.height = textLines.length
                              * (fm.getMaxDescent() + fm.getMaxAscent());
              break;

          default :
              bounds = new Rectangle(base);
              if (width != -1) {
                  bounds.width = width;
              }
              if (height != -1) {
                  bounds.height = height;
              }
              break;
        }

    }

    /**
     * _more_
     *
     * @param g _more_
     * @param boundsMap _more_
     * @param siblingBounds _more_
     */
    public void calculateBounds(Graphics g, Hashtable boundsMap,
                                Rectangle siblingBounds) {
        int maxWidth  = 0;
        int maxHeight = 0;

        for (int i = 0; i < childShapes.size(); i++) {
            Rectangle  prevBounds = siblingBounds;
            GraphShape childShape = (GraphShape) childShapes.elementAt(i);
            if ( !childShape.visible) {
                continue;
            }
            childShape.calculateBounds(g, boundsMap, prevBounds);
            maxWidth  = Math.max(maxWidth, childShape.searchBounds.width);
            maxHeight = Math.max(maxHeight, childShape.searchBounds.height);
            if (childShape.id != null) {
                boundsMap.put(childShape.id, childShape.searchBounds);
            }
            prevBounds = childShape.searchBounds;
        }

        Rectangle base = null;
        switch (boundsSrc) {

          case SRC_ID :
              base = (Rectangle) boundsMap.get(boundsSrcId);
              break;

          case SRC_SIBLING :
              base = siblingBounds;
              break;
        }
        if (base == null) {
            base = glyph.getBounds();
        }

        if (layoutType != LAYOUT_NONE) {
            Rectangle previous = null;
            for (int i = 0; i < childShapes.size(); i++) {
                GraphShape childShape = (GraphShape) childShapes.elementAt(i);
                if ( !childShape.visible) {
                    continue;
                }
                if (previous != null) {
                    if (layoutType == LAYOUT_V) {
                        childShape.moveTo(previous.x + ((align == Label.RIGHT)
                                ? (maxWidth - childShape.bounds.width)
                                : ((align == Label.CENTER)
                                   ? (maxWidth - childShape.bounds.width) / 2
                                   : 0)), bottom(previous) + vGap);
                    } else if (layoutType == LAYOUT_H) {
                        childShape.moveTo(right(previous) + hGap, previous.y);
                    }
                }
                previous = childShape.searchBounds;
            }
        }

        setDimensions(base, g);
        initBounds();
        if ((fromAnchor != GuiUtils.PT_NONE)
                && (toAnchor != GuiUtils.PT_NONE)) {
            Point toPt   = GuiUtils.getPointOnRect(toAnchor, base);
            Point fromPt = GuiUtils.getPointOnRect(fromAnchor, bounds);
            moveBy(toPt.x + dx - fromPt.x, toPt.y + dy - fromPt.y);
        }
    }


    /**
     * _more_
     *
     * @param r _more_
     *
     * @return _more_
     */
    public int right(Rectangle r) {
        return r.x + r.width;
    }

    /**
     * _more_
     *
     * @param r _more_
     *
     * @return _more_
     */
    public int bottom(Rectangle r) {
        return r.y + r.height;
    }

    /**
     * _more_
     *
     * @param r _more_
     *
     * @return _more_
     */
    public static int midX(Rectangle r) {
        return r.x + r.width / 2;
    }

    /**
     * _more_
     *
     * @param r _more_
     *
     * @return _more_
     */
    public static int midY(Rectangle r) {
        return r.y + r.height / 2;
    }


    /**
     * _more_
     *
     * @param isHighlight _more_
     *
     * @return _more_
     */
    public Color getLineColor(boolean isHighlight) {
        if ( !isHighlight && (color != null)) {
            return color;
        }
        return (isHighlight
                ? GraphView.highlightColor
                : Color.black);
    }

    /**
     * _more_
     *
     * @param isHighlight _more_
     *
     * @return _more_
     */
    public Color getLineColor2(boolean isHighlight) {
        if ( !isHighlight && (color != null)) {
            return color;
        }
        return (isHighlight
                ? GraphView.highlightColor
                : Color.black);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Color getColor() {
        if (color != null) {
            return color;
        }
        return Color.black;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /*
      public Image getImage () {
        if (image != null) return image;
        if (imageUrl == null) return null;
        image = graphView.graphApplet.getImage (imageUrl);
        if (image!=null) {
          imageOk = image.getWidth (this)>0;
          if (imageOk) dirty = true;
          image.getWidth (graphView);
        }
        return image;
      }


      public boolean imageUpdate (Image img, int flags, int x, int y, int width, int height)  {
        if ((flags & ImageObserver.ERROR) != 0) {
          imageOk = false;
          return false;
        }
        if ((flags & ImageObserver.ALLBITS) != 0) {
          imageOk = true;
          return false;
        }
        dirty=true;
        graphView.repaint ();
        return true;
      }


      public boolean imageOk () {
        if (image==null ||  !graphView.getDrawNodeImages()) return false;
        if (imageOk) return imageOk;
        image.getWidth(this);
        return false;
      }

    */

    /**
     * _more_
     *
     * @param g _more_
     * @param isHighlight _more_
     */
    public void paint(Graphics g, boolean isHighlight) {

        if ( !visible) {
            return;
        }




        Rectangle sb = gv.scaleRect(bounds);
        if (isParent()) {
            if (fillColor != null) {
                g.setColor(fillColor);
                g.fillRect(sb.x, sb.y, sb.width, sb.height);
            }
            if (color != null) {
                g.setColor(color);
                g.drawRect(sb.x, sb.y, sb.width, sb.height);
            }
        }
        int left   = sb.x;
        int right  = left + sb.width;
        int top    = sb.y;
        int bottom = top + sb.height;

        int D1     = gv.scale(8);
        int D2     = gv.scale(4);

        if (borderType != GuiUtils.BORDER_EMPTY) {
            GuiUtils.paintBorder(g, borderType, insets, borderColors, sb.x,
                                 sb.y, sb.width, sb.height);
        }

        int posX  = gv.scale(bounds.x + insets.left);
        int posY  = gv.scale(bounds.y + insets.top);
        int baseW = gv.scale(bounds.width - insets.left - insets.right);
        int baseH = gv.scale(bounds.height - insets.top - insets.bottom);



        switch (shapeType) {

          case SHAPE_IMAGE :
              g.drawImage(image, posX, posY, baseW, baseH, null, null);
              break;

          case SHAPE_TEXT :
              g.setFont(getScaledFont(g));
              g.setColor(getColor());
              FontMetrics fm         = g.getFontMetrics();
              int         lineHeight = fm.getMaxDescent() + fm.getMaxAscent();
              int         textX      = posX;
              int         textY      = posY + lineHeight;

              if (okToDrawText) {
                  if (href != null) {
                      g.setColor(Color.blue);
                  }
                  for (int i = 0; i < textLines.length; i++) {
                      int lineWidth = fm.stringWidth(textLines[i]);
                      int x         = textX + ((align == Label.RIGHT)
                              ? (maxTextWidth - lineWidth)
                              : ((align == Label.CENTER)
                                 ? (maxTextWidth - lineWidth) / 2
                                 : 0));
                      g.drawString(textLines[i], x, textY);
                      if (href != null) {
                          g.drawLine(x, textY, x + lineWidth, textY);
                      }
                      textY += lineHeight;
                  }
              }
              break;

          case SHAPE_RECT :
          case SHAPE_3DRECT :
              if (fillColor != null) {
                  g.setColor(fillColor);
                  g.fillRect(sb.x, sb.y, sb.width, sb.height);
              }
              if (shapeType == SHAPE_3DRECT) {
                  int[] xs = { left, left + D1, right + D1, right, left };
                  int[] ys = { top, top - D1, top - D1, top, top };
                  g.fillPolygon(xs, ys, xs.length);
                  int[] xs2 = { right, right + D1, right + D1, right, right };
                  int[] ys2 = { top, top - D1, bottom - D1, bottom, top };
                  if (fillColor != null) {
                      g.setColor(fillColor.darker());
                      g.fillPolygon(xs2, ys2, xs2.length);
                  }

                  g.setColor(getLineColor(isHighlight));
                  g.drawPolygon(xs, ys, xs.length);
                  g.drawPolygon(xs2, ys2, xs2.length);
                  g.drawRect(sb.x, sb.y, sb.width, sb.height);
              } else {
                  g.setColor(getLineColor(isHighlight));
                  g.drawLine(left, top, right, top);
                  g.drawLine(left, top, left, bottom);
                  g.setColor(getLineColor2(isHighlight));
                  g.drawLine(right, top, right, bottom);
                  g.drawLine(left, bottom, right, bottom);
              }
              break;

          case SHAPE_RRECT :
              int radius  = (int) (sb.height / 2.0 + 0.5);
              int arcLeft = left - radius;
              int arcTop  = top;
              int arcH    = bottom - arcTop + 1;
              int arcW    = arcH;

              if (fillColor != null) {
                  g.setColor(fillColor);
                  g.fillRect(sb.x, sb.y, sb.width, sb.height + 1);
                  g.fillArc(arcLeft, arcTop, arcW, arcH, 90, 180);
                  g.fillArc(right - radius, arcTop, arcW, arcH, 90, -180);
              }

              g.setColor(getLineColor(isHighlight));
              g.drawLine(left, top, right, top);
              g.drawArc(arcLeft, arcTop, arcW, arcH, 90, 180);
              g.drawLine(left, bottom + 1, right, bottom + 1);
              g.drawArc(right - radius, arcTop, arcW, arcH, 90, -180);
              break;

          case SHAPE_OVAL :
              int offset = sb.height / 2;
              if (fillColor != null) {
                  g.setColor(fillColor);
                  g.fillOval(sb.x - offset, sb.y - offset,
                             sb.width + offset * 2, sb.height + offset * 2);
              }
              g.setColor(getLineColor(isHighlight));
              g.drawOval(sb.x - offset, sb.y - offset, sb.width + offset * 2,
                         sb.height + offset * 2);
              break;

          case SHAPE_CIRCLE :
              if (fillColor != null) {
                  g.setColor(fillColor);
                  g.fillOval(sb.x, midY(sb) - sb.width / 2, sb.width,
                             sb.width);
              }
              g.setColor(getLineColor(isHighlight));
              g.drawOval(sb.x, midY(sb) - sb.width / 2, sb.width, sb.width);
              break;

          case SHAPE_BARREL :
              sb.height += D2;
              g.setColor((fillColor != null)
                         ? fillColor
                         : getColor());
              g.fillRect(sb.x, sb.y, sb.width, sb.height);
              g.fillArc(sb.x, sb.y + sb.height - D2, sb.width, D2 * 2, 180,
                        180);
              g.drawArc(sb.x, sb.y + sb.height - D2, sb.width, D2 * 2, 0,
                        360);
              g.setColor(g.getColor().darker());
              g.fillArc(sb.x, sb.y - D2, sb.width, D2 * 2, 0, 360);
              g.setColor(getLineColor(isHighlight));
              g.drawArc(sb.x, sb.y - D2, sb.width, D2 * 2, 0, 360);
              g.drawArc(sb.x, sb.y + sb.height - D2, sb.width, D2 * 2, 180,
                        180);
              g.drawLine(sb.x, sb.y, sb.x, sb.y + sb.height);
              g.drawLine(right(sb), sb.y, right(sb), sb.y + sb.height);
              break;

          case SHAPE_TRIANGLE :
              g.setColor(getLineColor(isHighlight));
              int[] xs = { sb.x, midX(sb), right(sb), sb.x };
              int[] ys = { sb.y + sb.height, sb.y, sb.y + sb.height,
                           sb.y + sb.height };
              g.fillPolygon(xs, ys, xs.length);
              break;
        }

        for (int i = childShapes.size() - 1; i >= 0; i--) {
            GraphShape childShape = (GraphShape) childShapes.elementAt(i);
            childShape.paint(g, isHighlight);
        }

        //    g.setColor (Color.red);
        //    g.drawRect (sb.x, sb.y, sb.width, sb.height);


    }

}

