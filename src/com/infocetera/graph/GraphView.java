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


import com.infocetera.glyph.HtmlGlyph;

import com.infocetera.util.*;

import com.infocetera.util.XmlNode;

//import com.infocetera.chart.GIFEncoder;


import java.applet.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.image.ImageObserver;

import java.io.*;

import java.net.*;



import java.util.*;
import java.util.List;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;


/**
 *  This is the primary class for the graph visualization applet.
 */
public class GraphView extends ScrollCanvas implements ListSelectionListener,
        ItemListener, KeyListener, ActionListener, MouseListener,
        MouseMotionListener, ImageObserver {


    /** _more_          */
    public static final Cursor DEFAULT_CURSOR =
        new Cursor(Cursor.DEFAULT_CURSOR);

    /** _more_          */
    public static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);

    /** _more_          */
    public static final String CMD_TITLEATTR = "setTitleAttribute";

    /** _more_          */
    public static final String CMD_MAXCONNECTIONS = "setMaxConnections";

    /** _more_          */
    public static final String CMD_TIGHTNESS = "setTightness";

    /** _more_          */
    public static final String CMD_DIRECTION = "setDirection";

    /** _more_          */
    public static final String CMD_LEVEL = "setLevel";

    /** _more_          */
    public static final String CMD_LAYOUT = "setLayout";

    /** _more_          */
    public static final String CMD_SHAPEFLAG = "setShapeFlag";

    /** _more_          */
    public static final String CMD_FLAG = "setFlag";

    /** _more_          */
    public static final String CMD_FLOAT = "float";

    /** _more_          */
    public static final String CMD_SHOWNODELIST = "showNodeList";

    /** _more_          */
    public static final String CMD_LOAD = "load";

    /** _more_          */
    public static final String CMD_NEW = "new";

    /** _more_          */
    public static final String CMD_RELOAD = "reload";

    /** _more_          */
    public static final String CMD_REMOVE = "remove";

    /** _more_          */
    public static final String CMD_URL = "url";

    /** _more_          */
    public static final String CMD_NAV_BACK = "navBack";

    /** _more_          */
    public static final String CMD_NAV_BACKBACK = "navBackBack";

    /** _more_          */
    public static final String CMD_NAV_FWD = "navFwd";

    /** _more_          */
    public static final String CMD_NAV_FWDFWD = "navFwdFwd";





    /** _more_          */
    public static final String TAG_GFX = "gfx";

    /** _more_          */
    public static final String TAG_SHAPE = "shape";

    /** _more_          */
    public static final String ATTR_SHAPETYPE = "type";



    /** _more_          */
    public static final String TAG_GRAPH = "graph";

    /** _more_          */
    public static final String TAG_MESSAGE = "message";

    /** _more_          */
    public static final String TAG_NODETYPE = "nodetype";

    /** _more_          */
    public static final String TAG_EDGETYPE = "edgetype";

    /** _more_          */
    public static final String TAG_CANVAS = "canvas";

    /** _more_          */
    public static final String TAG_EDGE = "edge";

    /** _more_          */
    public static final String TAG_NODE = "node";

    /** _more_          */
    public static final String TAG_COMMAND = "command";


    /** _more_          */
    public static final String ATTR_BGCOLOR = "bgcolor";

    /** _more_          */
    public static final String ATTR_BGIMAGE = "bgimage";

    /** _more_          */
    public static final String ATTR_COMMAND = "command";

    /** _more_          */
    public static final String ATTR_FONTSIZE = "fontsize";

    /** _more_          */
    public static final String ATTR_FONTFACE = "fontface";

    /** _more_          */
    public static final String ATTR_NAME = "name";

    /** _more_          */
    public static final String ATTR_LABEL = "label";

    /** _more_          */
    public static final String ATTR_LEVEL = "level";

    /** _more_          */
    public static final String ATTR_MERGE = "merge";

    /** _more_          */
    public static final String ATTR_MESSAGE = "message";

    /** _more_          */
    public static final String ATTR_SIMPLEGUI = "simplegui";

    /** _more_          */
    public static final String ATTR_TARGET = "target";

    /** _more_          */
    public static final String ATTR_URL = "url";

    /** _more_          */
    public static final String ATTR_FROM = "from";

    /** _more_          */
    public static final String ATTR_TO = "to";

    /** _more_          */
    public static final String ATTR_TYPE = "type";


    /** You can specify the id of the default center node as an applet param */
    public static final String PARAM_DFLTNODE = "dfltnode";

    /** _more_          */
    public static final String PARAM_SKINURL = "skinurl";

    /** _more_          */
    public static final String DEFAULTSKIN =
        "/com/infocetera/graph/defaultskin.xml";





    /**
     *  This parameter (if defined) specifies a url template which, when
     *  instantiated with a node,  returns the graph xml for that node.
     */
    public static final String PARAM_NODEURL = "nodeurl";

    /**
     *  Where we find the  UI skin xml file
     */
    String skinPath;


    /** _more_          */
    private String nodeUrl;

    /** _more_          */
    private String delimiter = ",";

    /**
     *  This holds a set of nodes that have to be incrementally loaded in.
     */
    private Vector nodesToLoad = new Vector();

    /** _more_          */
    private Hashtable shapeVisibility = new Hashtable();


    /** _more_          */
    private Hashtable dynamicallyLoadedNodes = new Hashtable();

    /** _more_          */
    public static final String DFLT_FONT = "Dialog";



    /**
     *  Reference back to the containing applet
     */
    GraphApplet graphApplet;

    /**
     *  If we had an error on initialization this holds the error message.
     */
    private String fatalErrorMsg = null;


    /** _more_          */
    private String message = null;

    /**
     *  Misc fonts
     */
    static Font labelFont = new Font(DFLT_FONT, Font.BOLD, 12);

    /** _more_          */
    static Font widgetFont = new Font(DFLT_FONT, 0, 12);

    /** _more_          */
    static Font smallWidgetFont = new Font(DFLT_FONT, Font.BOLD, 8);

    /** _more_          */
    static Font theFont = new Font(DFLT_FONT, 0, 14);


    /** _more_          */
    Vector titleAttributes = new Vector();

    /**
     *  Misc colors
     */
    public static Color mouseOverColor = new Color(255, 255, 204);

    /** _more_          */
    public static Color highlightColor = Color.yellow;

    /** _more_          */
    private XmlUi xmlUi;


    /** _more_          */
    private boolean haveInited = false;


    /**
     *  The <canvas> node from the xml
     */
    XmlNode canvasXml;

    /**
     *  The <graph> node from the xml
     */
    XmlNode graphXml;


    /**
     *  Do we have a simple gui? i.e., just the GraphView without the buttons, etc.
     */
    boolean simpleGui = false;


    /** _more_          */
    private boolean amLoading = false;

    /** _more_          */
    private String loadingString = "";


    /**
     *  How far (graph hops) do we layout from the center node
     */
    public int maxLevel = 2;


    /** _more_          */
    private int maxConnections = 500;


    /**
     *  How tight is the layout
     */
    double tightness = 0.8;

    /**
     *  background image from the canvas tag's bgimage attribute
     */
    Image bgImage = null;


    /**
     *  Gui components
     */

    Hashtable flags = new Hashtable();

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean getFlag(String name, boolean dflt) {
        Boolean b = (Boolean) flags.get(name);
        if (b != null) {
            return b.booleanValue();
        }
        return dflt;
    }

    /** _more_          */
    private int layoutDirection = GraphNode.DIR_BOTH;

    /** _more_          */
    private boolean scaleWithLevel = false;

    /** _more_          */
    private boolean showAllEdges = true;

    /** _more_          */
    private boolean showMouseOver = true;

    /** _more_          */
    private boolean pauseMouseOver = true;

    /** _more_          */
    private boolean relativeToLevel = false;

    /** _more_          */
    private boolean animateMoves = true;

    /** _more_          */
    private boolean edgeLabel = false;

    /** _more_          */
    private boolean nodeLabel = false;

    /** _more_          */
    private boolean showAllLabels = false;




    /** _more_          */
    Hashtable nodeTypeMap = new Hashtable();

    /** _more_          */
    Vector nodeTypes = new Vector();

    /** _more_          */
    Vector nodeHideButtons = new Vector();

    /** _more_          */
    Hashtable nodeHideMap = new Hashtable();

    /** _more_          */
    Panel nodePanel = new Panel(new GridLayout(0, 1));

    /** _more_          */
    Hashtable edgeTypeMap = new Hashtable();

    /** _more_          */
    Vector edgeTypes = new Vector();

    /** _more_          */
    Vector edgeHideButtons = new Vector();

    /** _more_          */
    Hashtable edgeHideMap = new Hashtable();

    /** _more_          */
    Panel edgePanel = new Panel(new GridLayout(0, 1));


    /** _more_          */
    public static final int LAYOUT_RADIAL = 0;

    /** _more_          */
    public static final int LAYOUT_RELAX1 = 1;

    /** _more_          */
    public static final int LAYOUT_RELAX2 = 2;

    /** _more_          */
    public static final int LAYOUT_VTREE = 3;

    /** _more_          */
    public static final int LAYOUT_HTREE = 4;

    /** _more_          */
    public static final int LAYOUT_CIRCULAR1 = 5;

    /** _more_          */
    public static final int LAYOUT_CIRCULAR2 = 6;

    /** _more_          */
    public static final int LAYOUT_VHIER = 7;

    /** _more_          */
    public static final int LAYOUT_HHIER = 8;

    /** _more_          */
    public static final int LAYOUT_RECTILINEAR = 9;

    /** _more_          */
    public static final int LAYOUT_HISTORY = 10;

    /** _more_          */
    public static final int LAYOUT_NONE = 11;

    /** _more_          */
    public static final String[] LAYOUT_NAMES = {
        "radial", "relax1", "relax2", "vtree", "htree", "circular1",
        "circular2", "vhier", "hhier", "rectilinear", "history", "none"
    };

    /** _more_          */
    public int layoutType = LAYOUT_RELAX1;

    /**
     *  Are we in need of laying out the graph
     */
    boolean needLayout = true;



    /**
     *  Holds a list of the central GraphNodes  we have visited.
     */
    Vector history = new Vector();

    /**
     *  The index into the history list we are currently viewing
     */
    int historyIdx = -1;

    /** _more_          */
    public static int TICKS = 4;

    /** _more_          */
    int animTicks = 0;

    /** _more_          */
    int totalTicks = 0;


    /**
     *  The list of currently displayed nodes
     */
    Vector displayNodes = new Vector();

    /**
     *  The list of currently displayed edge
     */
    Vector displayEdges = new Vector();

    /**
     *  All the nodes
     */
    Vector allNodes = new Vector();


    /**
     *  All the edges
     */
    Vector allEdges = new Vector();

    /**
     *  The string id of the default center node (from the param tag)
     */
    String centerId;

    /**
     *  The first center node
     */
    GraphNode dfltCenterNode = null;


    /**
     *  The current center node
     */
    GraphNode centerNode = null;

    /**
     *  The node currently under the cursor
     */
    GraphNode hilite = null;

    /** _more_          */
    GraphShape hiliteShape;

    /** _more_          */
    GraphNode popupNode = null;

    /** _more_          */
    GraphNode draggedNode = null;


    /**
     *  Contains all of the awt.List objects created for each node type
     */
    Vector nodeLists = new Vector();


    /**
     *  Holds a mapping from node type to the awt.List gui component
     *  that shows the nodes of that type.
     */
    Hashtable nodeTypeToList = new Hashtable();

    /**
     *  Mapping between the awt.List and the list of GraphNode-s it contains
     */
    Hashtable nodeTypeToVector = new Hashtable();

    /** _more_          */
    Hashtable listToNodeType = new Hashtable();


    /**
     *  Mapping between node ids (String) and the GraphNode object
     */
    Hashtable idToNode = new Hashtable();

    /**
     *  a map of tailid->headid->edgetype to GraphEdge
     */
    Hashtable idToEdge = new Hashtable();


    /** _more_          */
    Component contents;


    /** _more_          */
    Panel leftPanel;

    /** _more_          */
    Frame floatFrame;

    /** _more_          */
    Frame nodeListFrame;

    /** _more_          */
    boolean isMain;


    /**
     * _more_
     *
     * @param graphApplet _more_
     * @param isMain _more_
     */
    public GraphView(GraphApplet graphApplet, boolean isMain) {
        this.isMain      = isMain;

        this.graphApplet = graphApplet;

        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        centerId  = graphApplet.getParameter(PARAM_DFLTNODE);
        delimiter = graphApplet.getParameter("delimiter", ",");

        nodeUrl =
            graphApplet.getFullUrl(graphApplet.getParameter(PARAM_NODEURL));
        skinPath = graphApplet.getParameter(PARAM_SKINURL);
        if (skinPath == null) {
            skinPath = DEFAULTSKIN;
        }
        String value = graphApplet.getParameter(ATTR_BGIMAGE);
        if ((value != null) && (bgImage == null)) {
            bgImage = graphApplet.getImage(value);
        }

        if ( !isMain) {
            getContents();
            toggleFloat();
            for (int i = 0; i < graphApplet.graphs.size(); i++) {
                processGraphXml((String) graphApplet.graphs.elementAt(i));
            }
        }
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public Component getContents() {
        if (contents == null) {
            contents = doMakeContents();
            initializeState();
        }
        return contents;
    }

    /**
     * _more_
     */
    protected void initializeState() {
        history         = new Vector();
        historyIdx      = -1;
        nodeTypeMap     = new Hashtable();
        nodeTypes       = new Vector();
        nodeHideButtons = new Vector();
        nodeHideMap     = new Hashtable();
        nodePanel.removeAll();
        edgeTypeMap     = new Hashtable();
        edgeTypes       = new Vector();
        edgeHideButtons = new Vector();
        edgeHideMap     = new Hashtable();
        edgePanel.removeAll();
        centerNode             = null;
        dynamicallyLoadedNodes = new Hashtable();
        nodeLists              = new Vector();
        nodeTypeToList         = new Hashtable();
        nodeTypeToVector       = new Hashtable();
        listToNodeType         = new Hashtable();
        idToEdge               = new Hashtable();
        idToNode               = new Hashtable();
        allNodes               = new Vector();
        allEdges               = new Vector();
        enableNavButtons();
        repaint();
    }


    /**
     *  Make the gui components. If we are in simpleGui mode this
     *  method still creates all of the components. It just will return
     *  the GraphView (i.e., this).
     *
     * @return _more_
     */
    public Component doMakeContents() {
        String uiXml = "";
        try {
            byte[] skin = GuiUtils.readResource(skinPath, getClass(), false);
            if (skin == null) {
                skin = GuiUtils.readResource(
                    graphApplet.getFullUrl(skinPath), getClass(), true);
            }
            if (skin == null) {
                skin = GuiUtils.readResource(DEFAULTSKIN, getClass(), true);
            }
            if (skin == null) {
                uiXml =
                    "<panel><label label=\"Failed to user interface read skin\"/></panel>";
            } else {
                uiXml = new String(skin);
            }


            //Parse it once so we get the list of properties to swap
            XmlNode uiRoot     = XmlNode.parse(uiXml).get(0);
            XmlNode properties = uiRoot.getChild("properties");
            if (properties != null) {
                for (int i = 0; i < properties.size(); i++) {
                    XmlNode propNode = properties.get(i);
                    String  name     = propNode.getAttribute("name");
                    if (name == null) {
                        continue;
                    }
                    String value = graphApplet.getParameter(name,
                                       propNode.getAttribute("value", ""));
                    uiXml = GuiUtils.replace(uiXml, "$" + name + "$", value);
                }
                uiRoot = XmlNode.parse(uiXml).get(0);
            }





            Hashtable components = new Hashtable();
            components.put("edgepanel", edgePanel);
            components.put("nodepanel", nodePanel);
            components.put("canvas", this);

            xmlUi = new XmlUi(graphApplet, uiRoot, components, this, this);

            if (dfltCenterNode != null) {
                nodeSelect(dfltCenterNode);
            }
            hideNodesAndEdges();

            if (simpleGui) {
                return this;
            }
            return xmlUi.getContents();
        } catch (Exception exc) {
            System.err.println("Error parsing skin xml:" + uiXml);
            exc.printStackTrace();
            return new Label(exc.getMessage());

        }
    }


    /**
     *  Create the xml dom tree
     *
     * @param xml _more_
     */
    public void processGraphXml(String xml) {
        //System.err.println (xml);
        //The root isn't the "<canvas" tag, rather it represents the document
        //element which contains the canvas element
        XmlNode root = XmlNode.parse(xml);
        message = "";
        processXml(root.getChildren());
        if (canvasXml == null) {
            canvasXml = new XmlNode(TAG_CANVAS, new Hashtable());
        }
        if (graphXml == null) {
            graphXml = new XmlNode(TAG_GRAPH, new Hashtable());
        }

        //Once everything is created we make sure all of the edges are hooked
        //up to their tail and head nodes
        connectUpEdges();

        if (centerNode == null) {
            GraphNode ctr = null;
            if (centerId != null) {
                ctr = (GraphNode) idToNode.get(centerId);
            }
            if ((ctr == null) && (allNodes.size() > 0)) {
                ctr = (GraphNode) allNodes.elementAt(0);
            }
            if (ctr != null) {
                nodeSelect(ctr);
            }
        }
        haveInited = true;
        needLayout = true;
        amLoading  = false;
        hideNodesAndEdges();
        repaint();
    }


    /**
     * Run through  the given XmlNode-s
     *
     * @param children _more_
     */
    public void processXml(Vector children) {
        for (int i = 0; i < children.size(); i++) {
            XmlNode xmlNode = (XmlNode) children.elementAt(i);
            String  tag     = xmlNode.getTag();
            if (tag.equals(TAG_MESSAGE)) {
                message += xmlNode.getAttribute(ATTR_MESSAGE, "") + "\n";
            } else if (tag.equals(TAG_GRAPH)) {
                processGraph(xmlNode);
            } else if (tag.equals(TAG_NODETYPE)) {
                processType(true, xmlNode, nodeTypeMap, nodeTypes);
            } else if (tag.equals(TAG_EDGETYPE)) {
                processType(false, xmlNode, edgeTypeMap, edgeTypes);
            } else if (tag.equals(TAG_CANVAS)) {
                processCanvas(xmlNode);
            } else if (tag.equals(TAG_EDGE)) {
                processEdge(xmlNode);
            } else if (tag.equals(TAG_NODE)) {
                processNode(xmlNode);
            }
        }
    }

    /**
     *  Set the graphXml member to the given node and process the children
     *
     * @param node _more_
     */
    public void processGraph(XmlNode node) {
        if (graphXml == null) {
            graphXml = node;
        }
        processXml(node.getChildren());
    }

    /**
     * _more_
     *
     * @param edgeNode _more_
     */
    public void processEdge(XmlNode edgeNode) {
        String fromId = edgeNode.getAttribute(ATTR_FROM);
        String toId   = edgeNode.getAttribute(ATTR_TO);
        if ((fromId == null) || (toId == null)) {
            System.err.println("Error: Ill formed edge xml fromId:" + fromId
                               + " toId:" + toId);
            return;
        }


        XmlNode edgeType     = null;
        String  edgeTypeName = edgeNode.getAttribute(ATTR_TYPE);
        if ((edgeTypeName != null) && !edgeTypeName.equals("")) {
            edgeType = (XmlNode) edgeTypeMap.get(edgeTypeName);
            if (edgeType == null) {
                edgeTypes.addElement(edgeTypeName);
                edgeType = new XmlNode(TAG_EDGETYPE, new Hashtable());
                edgeTypeMap.put(edgeTypeName, edgeType);
            }
        }
        GraphEdge edge    = new GraphEdge(this, edgeNode, fromId, toId);
        String    edgeId  = edge.id;
        GraphEdge oldEdge = (GraphEdge) idToEdge.get(edgeId);
        //Do we already have this edge?
        if (oldEdge == null) {
            idToEdge.put(edgeId, edge);
            allEdges.addElement(edge);
        }
    }


    /**
     *  Process the node.
     *
     * @param xmlNode _more_
     */
    private void processNode(XmlNode xmlNode) {
        GraphNode theNode =
            (GraphNode) idToNode.get(xmlNode.getAttribute(GraphNode.ATTR_ID,
                "badid"));

        if (theNode != null) {
            if (xmlNode.getAttribute(ATTR_MERGE, false)) {
                String oldType = theNode.getTypeName();
                theNode.merge(xmlNode);
                if (oldType != theNode.getTypeName()) {
                    removeNodeFromList(theNode, oldType);
                    addNodeToList(theNode, theNode.getTypeName());
                }
            }
            return;
        }

        newNode(new GraphNode(this, xmlNode));
    }


    /**
     * _more_
     *
     * @param theNode _more_
     */
    protected void newNode(GraphNode theNode) {
        idToNode.put(theNode.getId(), theNode);
        allNodes.addElement(theNode);
        addNodeToList(theNode, theNode.getTypeName());
    }


    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public String getAttr(String name) {
        String value = null;
        if (graphXml != null) {
            value = (String) graphXml.getAttribute(name);
        }
        if ((value == null) && (canvasXml != null)) {
            value = (String) canvasXml.getAttribute(name);
        }
        return value;
    }

    /**
     *  Process the canvas node. This sets a variety of properties
     *  (if defined in the XmlNode), e.g., background color, simple gui, etc.
     *  It the recursively processes the children of the given canvas node.
     *
     * @param node _more_
     */
    private void processCanvas(XmlNode node) {
        if (canvasXml == null) {
            canvasXml = node;
        }
        processXml(node.getChildren());
        simpleGui = node.getAttribute(ATTR_SIMPLEGUI, simpleGui);


        //Set the background color of the canvas
        Color bgColor = node.getAttribute(ATTR_BGCOLOR, (Color) null);
        if (bgColor != null) {
            setBackground(bgColor);
        }

        //Set the background image if defined
        String value = node.getAttribute(ATTR_BGIMAGE);
        if ((value != null) && (bgImage == null)) {
            bgImage = graphApplet.getImage(value);
        }
    }


    /**
     *  We can define a node or edge type which is an xml element
     *  that holds attributes. A node or edge can then have a type specified
     *  which the node or edge uses to grab attributes.
     *
     * @param nodeType _more_
     * @param node _more_
     * @param types _more_
     * @param typeList _more_
     */
    private void processType(boolean nodeType, XmlNode node, Hashtable types,
                             Vector typeList) {
        String name = node.getAttribute(ATTR_NAME, (String) null);
        String id   = node.getAttribute("id", name);
        if (id == null) {
            return;
        }
        if (types.get(id) != null) {
            return;
        }
        types.put(id, node);
        typeList.addElement(id);
        if ( !nodeType) {
            Hashtable map     = node.getAttributeTable();
            boolean   visible = node.getAttribute("visible", true);
            JCheckBox cbx     = getCheckbox(name, visible, this);
            edgeHideButtons.addElement(cbx);
            Color bgColor =
                GuiUtils.getColor((String) map.get(GraphEdge.ATTR_COLOR),
                                  Color.white);
            cbx.setBackground(bgColor);
            cbx.setForeground(getFGColor(bgColor));
            edgeHideMap.put(name, new Boolean(true));
            edgePanel.add(GuiUtils.border(cbx, GuiUtils.BORDER_MATTE, 1, 1));
            GuiUtils.relayout(edgePanel);
        }
    }


    /**
     *  Go through all of the graph edges and find their from/to GraphNodes
     *  and connect  them up.
     */
    public void connectUpEdges() {
        for (int i = 0; i < allEdges.size(); i++) {
            GraphEdge edge = (GraphEdge) allEdges.elementAt(i);
            if ((edge.getTail() == null) && (edge.getTailId() != null)) {
                edge.setTail((GraphNode) idToNode.get(edge.getTailId()));
            }
            if ((edge.getHead() == null) && (edge.getHeadId() != null)) {
                edge.setHead((GraphNode) idToNode.get(edge.getHeadId()));
            }
        }
    }


    /**
     *  Create (if needed) and return the XmlNode that represents
     *  the given type.
     *
     * @param nodeType _more_
     * @param graphNode _more_
     *
     * @return _more_
     */
    public XmlNode getNodeType(String nodeType, GraphNode graphNode) {
        XmlNode node = null;

        if ((graphNode != null) && graphNode.getIsCenter()) {
            node = (XmlNode) nodeTypeMap.get(nodeType + "_center");
            if (node == null) {
                node = (XmlNode) nodeTypeMap.get("centernode");
            }
        }

        if (node == null) {
            node = (XmlNode) nodeTypeMap.get(nodeType);
        }

        if (node == null) {
            node = new XmlNode(TAG_NODETYPE, new Hashtable());
            nodeTypeMap.put(nodeType, node);
            nodeTypes.addElement(nodeType);
        }
        return node;
    }


    /**
     * _more_
     *
     * @param node _more_
     * @param nodeType _more_
     */
    public void removeNodeFromList(GraphNode node, String nodeType) {
        JList nodeList = (JList) nodeTypeToList.get(nodeType);
        if (nodeList == null) {
            return;
        }
        Vector nodesInList = (Vector) nodeTypeToVector.get(nodeType);
        int    index       = nodesInList.indexOf(node);
        if (index < 0) {
            return;
        }
        nodesInList.removeElementAt(index);
        nodeList.remove(index);
    }


    /**
     * _more_
     *
     * @param node _more_
     * @param nodeType _more_
     */
    public void addNodeToList(GraphNode node, String nodeType) {
        JList nodeList = (JList) nodeTypeToList.get(nodeType);
        if (nodeList == null) {
            nodeList = new JList();
            nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            nodeList.setVisibleRowCount(4);
            nodeList.setFont(widgetFont);
            nodeList.addListSelectionListener(this);
            nodeTypeToList.put(nodeType, nodeList);
            nodeLists.addElement(nodeList);
            XmlNode n = getNodeType(nodeType, node);
            JCheckBox typeCB = getCheckbox(nodeType,
                                           n.getAttribute("visible", true),
                                           this);
            nodeHideButtons.addElement(typeCB);
            Color bg = node.getAttr("fillcolor", (Color) null);
            if (bg != null) {
                typeCB.setBackground(bg);
            }
            GuiUtils.tmpInsets = new Insets(1, 0, 1, 0);
            Component comp = GuiUtils.border(typeCB, GuiUtils.BORDER_MATTE,
                                             1, 1);
            JScrollPane sp = new JScrollPane(nodeList);
            nodePanel.add(GuiUtils.doLayout(new Component[] { comp, sp }, 1,
                                            GuiUtils.DS_Y, GuiUtils.DS_NY));
            listToNodeType.put(nodeList, nodeType);
            nodeTypeToVector.put(nodeType, new Vector());
            GuiUtils.relayout(nodePanel);
        }

        Vector nodesInList = (Vector) nodeTypeToVector.get(nodeType);
        nodesInList.addElement(node);
        String title = getTitle(node);
        if (title.length() > 20) {
            title = title.substring(0, 19);
        }
        List items = getItems(nodeList);
        items.add(title);
        nodeList.setListData(new Vector(items));
    }

    /**
     * _more_
     *
     * @param list _more_
     *
     * @return _more_
     */
    public static List getItems(JList list) {
        List      items = new ArrayList();
        ListModel model = list.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            items.add(model.getElementAt(i));
        }
        return items;
    }



    /**
     * _more_
     *
     * @param node _more_
     *
     * @return _more_
     */
    protected String getTooltip(GraphNode node) {
        if (node == null) {
            return "";
        }
        String title = node.getAttr(GraphNode.ATTR_TOOLTIP);
        if (title == null) {
            title = getTitle(node);
        }
        return title;
    }

    /**
     * _more_
     *
     * @param node _more_
     *
     * @return _more_
     */
    protected String getTitle(GraphNode node) {
        String title = node.getAttr("title");
        if (title == null) {
            title = node.getAttr(GraphNode.ATTR_TOOLTIP);
        }
        if (title == null) {
            title = node.getAttr(GraphNode.ATTR_ID);
        }
        if (title == null) {
            System.err.println("no title found in:" + node.xmlNode);
            title = "no title";
        }
        int idx = title.indexOf("\n");
        if (idx >= 0) {
            title = title.substring(0, idx);
        }
        return title;
    }


    /**
     *  Lookup the XmlNode that represents the given edge type
     *
     * @param type _more_
     *
     * @return _more_
     */
    public XmlNode getEdgeType(String type) {
        if (type == null) {
            return null;
        }
        return (XmlNode) edgeTypeMap.get(type);
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void valueChanged(ListSelectionEvent e) {
        for (int i = 0; i < nodeLists.size(); i++) {
            JList nodeList = (JList) nodeLists.elementAt(i);
            if (e.getSource() != nodeList) {
                continue;
            }
            int idx = nodeList.getSelectedIndex();
            if (idx >= 0) {
                String    nodeType = (String) listToNodeType.get(nodeList);
                Vector    v        = (Vector) nodeTypeToVector.get(nodeType);
                GraphNode n        = (GraphNode) v.elementAt(idx);
                if (n != null) {
                    nodeSelect(n);
                }
                return;
            }
        }
        hideNodesAndEdges();
    }


    /**
     *  Handle the item changed event
     *
     * @param e _more_
     */
    public void itemStateChanged(ItemEvent e) {
        for (int i = 0; i < nodeLists.size(); i++) {
            JList nodeList = (JList) nodeLists.elementAt(i);
            if (e.getSource() != nodeList) {
                continue;
            }
            int idx = nodeList.getSelectedIndex();
            if (idx >= 0) {
                String    nodeType = (String) listToNodeType.get(nodeList);
                Vector    v        = (Vector) nodeTypeToVector.get(nodeType);
                GraphNode n        = (GraphNode) v.elementAt(idx);
                if (n != null) {
                    nodeSelect(n);
                }
                return;
            }
        }
        hideNodesAndEdges();
    }

    /**
     */
    public void hideNodesAndEdges() {
        for (int i = 0; i < nodeHideButtons.size(); i++) {
            JCheckBox cb = (JCheckBox) nodeHideButtons.elementAt(i);
            nodeHideMap.put(cb.getLabel(), new Boolean(cb.isSelected()));
        }
        for (int i = 0; i < allNodes.size(); i++) {
            GraphNode p = (GraphNode) allNodes.elementAt(i);
            p.elided = false;
            if (p.getTypeName() != null) {
                Boolean b = (Boolean) nodeHideMap.get(p.getTypeName());
                if (b != null) {
                    p.elided = !b.booleanValue();
                }
            }
        }
        for (int i = 0; i < edgeHideButtons.size(); i++) {
            JCheckBox cbx = (JCheckBox) edgeHideButtons.elementAt(i);
            edgeHideMap.put(cbx.getLabel(), new Boolean(cbx.isSelected()));
        }
        for (int i = 0; i < allEdges.size(); i++) {
            GraphEdge edge = (GraphEdge) allEdges.elementAt(i);
            edge.setVisible(okToShowEdge(edge));
        }
        needLayout = true;
        repaint();
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param value _more_
     *
     * @return _more_
     */
    public boolean getShapeVisibility(String id, boolean value) {
        Boolean b = (Boolean) shapeVisibility.get(id);
        if (b == null) {
            addShapeVisibility(id, value);
        }
        return ((b == null)
                ? value
                : b.booleanValue());
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param value _more_
     *
     * @return _more_
     */
    private boolean addShapeVisibility(String id, boolean value) {
        Boolean b = (Boolean) shapeVisibility.get(id);
        if (b == null) {
            b = new Boolean(value);
            shapeVisibility.put(id, b);
            Hashtable attrs = new Hashtable();
            attrs.put("label", id);
            attrs.put("action", "setShapeFlag(" + id + ",%value%)");
            attrs.put("value", "" + b);
            XmlNode cbmi = new XmlNode("cbmenuitem", attrs);
            xmlUi.addMenuItem("menu.shape", cbmi);
        }
        return b.booleanValue();
    }


    /**
     *  Do we draw the edge labels
     *
     * @return _more_
     */
    public boolean getDrawEdgeLabels() {
        return edgeLabel;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getShowMouseOver() {
        return showMouseOver;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getScaleWithLevel() {
        return scaleWithLevel;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getShowAllEdges() {
        return showAllEdges;
    }

    /**
     *  Do we draw the node labels
     *
     * @return _more_
     */
    public boolean getDrawNodeLabels() {
        return nodeLabel;
    }


    /**
     *
     *
     * @return _more_
     */
    public boolean getDrawAllLines() {
        return showAllLabels;
    }


    /**
     * _more_
     *
     * @param node _more_
     *
     * @return _more_
     */
    public boolean needToLoad(GraphNode node) {
        return ((nodeUrl != null) && !node.haveLoadedGraph);
    }


    /**
     * _more_
     *
     * @param node _more_
     *
     * @return _more_
     */
    private boolean needsToLoad(GraphNode node) {
        if ((nodeUrl == null) || node.haveLoadedGraph) {
            return false;
        }
        if (graphApplet.isNodeLoaded(node.id)) {
            node.haveLoadedGraph = true;
            return false;
        }
        return true;
    }

    /**
     * _more_
     *
     * @param node _more_
     */
    private void checkNodeForLoad(GraphNode node) {
        if ( !needsToLoad(node)) {
            return;
        }
        node.haveLoadedGraph = true;
        graphApplet.setNodeLoaded(node.id);
        addNodeToLoad(node.id, node, false);
    }

    /**
     * _more_
     *
     * @param nodeId _more_
     * @param node _more_
     * @param fromEdge _more_
     *
     * @return _more_
     */
    private boolean addNodeToLoad(String nodeId, GraphNode node,
                                  boolean fromEdge) {
        if ((nodeId == null) || (nodeUrl == null)) {
            return false;
        }
        if (fromEdge && (dynamicallyLoadedNodes.get(nodeId) != null)) {
            return false;
        }
        dynamicallyLoadedNodes.put(nodeId, nodeId);
        if ( !nodesToLoad.contains(nodeId)) {
            if (node != null) {
                nodesToLoad.addElement(node);
            } else {
                nodesToLoad.addElement(nodeId);
            }
            return true;
        }
        return false;
    }

    /**
     * _more_
     *
     * @param edges _more_
     */
    private void checkEdgesForNodeLoad(Vector edges) {
        for (int i = 0; i < edges.size(); i++) {
            GraphEdge edge = (GraphEdge) edges.elementAt(i);
            if (edge.getVisible()) {
                checkEdgeForNodeLoad(edge);
            }
        }
    }

    /**
     * _more_
     *
     * @param edge _more_
     */
    protected void checkEdgeForNodeLoad(GraphEdge edge) {
        if (nodeUrl == null) {
            return;
        }
        if (edge.getTail() == null) {
            if (addNodeToLoad(edge.getTailId(), null, true)) {}
        }
        if (edge.getHead() == null) {
            if (addNodeToLoad(edge.getHeadId(), null, true)) {}
        }
    }


    /**
     * _more_
     *
     * @param realUrl _more_
     */
    private void doLoadNodes(String realUrl) {
        graphApplet.debug("Loading nodes:" + realUrl);
        amLoading = true;
        String xml = null;
        try {
            message = "";
            xml     = GuiUtils.readUrl(realUrl);
            processGraphXml(xml);
            graphApplet.addGraph(xml, this);
            if (allNodes.size() == 0) {
                message += "No items found\n";
            }
        } catch (Exception exc) {
            haveInited = true;
            System.err.println("Error loading url:" + realUrl);
            exc.printStackTrace();
            System.err.println(xml);
            message = "Error loading data";
        }
        amLoading = false;
    }


    /**
     * _more_
     *
     * @param realUrl _more_
     */
    protected void loadNodes(final String realUrl) {
        amLoading = true;
        Runnable runnable = new Runnable() {
            public void run() {
                doLoadNodes(realUrl);
            }
        };
        Thread loadThread = new Thread(runnable);
        loadThread.start();
    }

    /**
     * _more_
     */
    private void loadNodes() {
        if (amLoading || (nodeUrl == null) || (nodesToLoad.size() == 0)) {
            return;
        }
        amLoading = true;
        StringBuffer tsb = new StringBuffer("");
        StringBuffer sb  = new StringBuffer("");
        for (int i = 0; i < nodesToLoad.size(); i++) {
            if (i != 0) {
                sb.append(delimiter);
                tsb.append(delimiter);
            }
            String nodeId;
            Object obj = nodesToLoad.elementAt(i);
            if (obj instanceof GraphNode) {
                tsb.append(((GraphNode) obj).getTypeName());
                sb.append(((GraphNode) obj).id);
            } else {
                tsb.append("");
                sb.append(obj.toString());
            }
        }

        nodesToLoad = new Vector();

        loadNodes(GuiUtils.replace(GuiUtils.replace(nodeUrl, "%nodeids%",
                encode(sb.toString())), "%nodetypes%",
                                        encode(tsb.toString())));
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    protected String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception exc) {
            return s;
        }
    }


    /**
     * _more_
     *
     * @param center _more_
     * @param nodesToLayout _more_
     * @param dir _more_
     * @param width _more_
     * @param radius _more_
     * @param degreesAvailable _more_
     * @param level _more_
     *
     * @return _more_
     */
    public Vector rotate(GraphNode center, Vector nodesToLayout, double dir,
                         int width, double radius, double degreesAvailable,
                         int level) {


        Point  middle  = new Point(getCenterX(center), getCenterY(center));
        double spacing = 0.0;
        double degreeSpacing;


        int    numConnects = nodesToLayout.size();
        if (numConnects > 0) {
            spacing = ((double) width) / ((double) (numConnects + 1));
        }

        degreeSpacing = degreesAvailable / ((double) (numConnects + 1));

        int baseX     = middle.x - width / 2;
        int x         = baseX + (int) spacing;
        int baselineY = -999;
        x = baseX + (int) spacing;
        for (int i = 0; i < nodesToLayout.size(); i++) {
            GraphNode p    = (GraphNode) nodesToLayout.elementAt(i);
            double radians = dir * GuiUtils.toRadian(degreeSpacing * (i + 1));
            int y = middle.y
                    + (int) (-radius * Math.sin(radians)
                             + 0 * Math.cos(radians) + 0.5);
            if (getLayoutRectilinear()) {
                if (baselineY == -999) {
                    baselineY = y;
                } else {
                    y = baselineY;
                }
            }
            setCenter(p, x, y);
            x += spacing;
        }

        return nodesToLayout;
    }


    /**
     * _more_
     *
     * @param node _more_
     */
    protected void setIsLaidOut(GraphNode node) {
        if (node.getNeedsLayout()) {
            node.setNeedsLayout(false);
            displayNodes.addElement(node);
        }
    }

    /**
     * _more_
     *
     * @param nodes _more_
     */
    protected void setIsLaidOut(Vector nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            setIsLaidOut((GraphNode) nodes.elementAt(i));
        }
    }


    /**
     * _more_
     *
     * @param nodesAndEdges _more_
     * @param level _more_
     *
     * @return _more_
     */
    private Vector pruneNodes(Vector[] nodesAndEdges, int level) {
        return pruneNodesAndEdges(nodesAndEdges, level)[0];
    }


    /**
     * _more_
     *
     * @param nodesAndEdges _more_
     * @param level _more_
     *
     * @return _more_
     */
    private Vector[] pruneNodesAndEdges(Vector[] nodesAndEdges, int level) {
        int    max   = maxConnections;
        Vector nodes = nodesAndEdges[0];
        Vector edges = nodesAndEdges[1];

        if (relativeToLevel) {
            max = ((level <= 1)
                   ? max
                   : (int) (max / (double) level + 0.5));
        }
        if (nodes.size() <= max) {
            return nodesAndEdges;
        }
        //    System.err.println ("before:" + nodes);
        Vector sortedNodes = new Vector();
        Vector sortedEdges = new Vector();
        for (int i = 0; i < nodes.size(); i++) {
            GraphNode node   = (GraphNode) nodes.elementAt(i);
            GraphEdge edge   = (GraphEdge) edges.elementAt(i);
            int       weight = node.getWeight();
            for (int j = 0; j < sortedNodes.size(); j++) {
                GraphNode other = (GraphNode) sortedNodes.elementAt(j);
                if (weight > other.getAllEdges().size()) {
                    sortedNodes.insertElementAt(node, j);
                    sortedEdges.insertElementAt(edge, j);
                    node = null;
                    break;
                }
            }
            if (node != null) {
                sortedNodes.addElement(node);
                sortedEdges.addElement(edge);
            }
        }
        //    System.err.println ("sorted:" + sortedNodes);

        Vector goodNodes = new Vector();
        Vector goodEdges = new Vector();
        for (int i = 0; i < max; i++) {
            goodNodes.addElement(sortedNodes.elementAt(i));
            goodEdges.addElement(sortedEdges.elementAt(i));
        }
        //    System.err.println ("pruned:" + goodNodes);
        for (int i = max; i < sortedNodes.size(); i++) {
            GraphNode node = (GraphNode) sortedNodes.elementAt(i);
            node.needsLayout = true;
            displayNodes.removeElement(node);
            ((GraphEdge) sortedEdges.elementAt(i)).setLevel(-1);
        }
        return new Vector[] { goodNodes, goodEdges };
    }

    /**
     * _more_
     *
     * @param center _more_
     * @param startAngle _more_
     * @param degreesAvailable _more_
     * @param radius _more_
     * @param level _more_
     */
    public void doLayoutRadial(GraphNode center, double startAngle,
                               double degreesAvailable, double radius,
                               int level) {

        if (++level > maxLevel) {
            return;
        }
        setIsLaidOut(center);

        Vector nodesToLayout = pruneNodes(center.getNodesToLayout(level,
                                   layoutDirection), level);
        setIsLaidOut(nodesToLayout);
        int numConnects = nodesToLayout.size();
        if (numConnects == 0) {
            return;
        }

        double[] percents = new double[numConnects];
        double   total    = 0.0;
        int      min      = 10;

        for (int i = 0; i < numConnects; i++) {
            GraphNode node       = (GraphNode) nodesToLayout.elementAt(i);
            int       connectCnt = 0;
            Vector    allEdges   = node.getAllEdges();
            for (int j = 0; j < allEdges.size(); j++) {
                GraphEdge edge = (GraphEdge) allEdges.elementAt(j);
                if ( !edge.getVisible()) {
                    continue;
                }
                GraphNode otherNode = edge.getOtherNode(node);
                if ((otherNode == null) || !otherNode.getNeedsLayout()
                        || otherNode.elided) {
                    continue;
                }
                connectCnt++;
            }
            percents[i] = Math.max(1, Math.min(min, connectCnt));
            total       += percents[i];
        }

        for (int i = 0; i < numConnects; i++) {
            percents[i] = percents[i] / total;
        }

        double degreeSpacing = degreesAvailable / ((double) (numConnects));
        double degree        = startAngle;
        int    centerWidth   = center.getRadius() / 2;
        int    maxNodeWidth  = 0;
        for (int i = 0; i < numConnects; i++) {
            GraphNode node      = (GraphNode) nodesToLayout.elementAt(i);
            int       nodeWidth = node.getRadius() / 2;
            if (nodeWidth > maxNodeWidth) {
                maxNodeWidth = nodeWidth;
            }
        }

        Point middle = new Point(getCenterX(center), getCenterY(center));
        for (int i = 0; i < numConnects; i++) {
            GraphNode node = (GraphNode) nodesToLayout.elementAt(i);
            //      int extraWidth= (radius>centerWidth+maxNodeWidth?0:maxNodeWidth+centerWidth);
            int    extraWidth = centerWidth + maxNodeWidth;

            double pieSize    = ((level >= maxLevel)
                                 ? degreeSpacing
                                 : percents[i] * degreesAvailable);
            degree += pieSize / 2.0;

            Point dest = GuiUtils.rotatePoint(new Point((int) -radius
                             - extraWidth, 0), GuiUtils.toRadian(degree));
            setCenter(node, dest.x + middle.x, dest.y + middle.y);
            doLayoutRadial(node, degree - pieSize / 2.0, pieSize,
                           radius * 0.75, level);
            degree += pieSize / 2.0;
        }

    }

    /**
     * _more_
     *
     * @param n _more_
     *
     * @return _more_
     */
    public int getCenterX(GraphNode n) {
        return (int) n.destX;
    }

    /**
     * _more_
     *
     * @param n _more_
     *
     * @return _more_
     */
    public int getCenterY(GraphNode n) {
        return (int) n.destY;
    }


    /**
     * _more_
     *
     * @param n _more_
     * @param x _more_
     * @param y _more_
     */
    public void setCenterOffset(GraphNode n, int x, int y) {
        if (n == draggedNode) {
            n.moveBy(x, y);
        } else {
            n.moveDestBy((double) x, (double) y);
        }
    }

    /**
     * _more_
     *
     * @param n _more_
     * @param x _more_
     * @param y _more_
     */
    public void setCenter(GraphNode n, int x, int y) {
        if (n == draggedNode) {
            n.setCenter(x, y);
        } else {
            n.setDest((double) x, (double) y);
        }
    }


    /**
     * _more_
     *
     * @param centerNode _more_
     * @param dfs _more_
     */
    private void doLayoutCircular(GraphNode centerNode, boolean dfs) {
        Vector results = new Vector();
        setIsLaidOut(centerNode);
        if (dfs) {
            doDfs(centerNode, results, GraphNode.DIR_BOTH, 0);
        } else {
            doBfs(centerNode, results, GraphNode.DIR_BOTH);
        }

        Rectangle b              = bounds();
        int       middleX        = b.width / 2;
        int       middleY        = b.height / 2;
        int       radius         = Math.min(b.width / 2, b.height / 2);
        int       offset         = (int) (((double) radius) * .8 * tightness);

        Point     pt             = new Point(middleX - offset, middleY);
        double    degreesPerStep = 360.0 / ((double) results.size());

        Point     origin         = new Point(middleX, middleY);
        for (int i = 0; i < results.size(); i++) {
            GraphNode node = (GraphNode) results.elementAt(i);
            checkEdgesForNodeLoad(node.getAllEdges());
            Point dest = GuiUtils.rotatePoint(pt, origin,
                             GuiUtils.toRadian(degreesPerStep * i));
            setCenter(node, dest.x, dest.y);
            setIsLaidOut(node);
        }
    }



    /**
     * _more_
     *
     * @param node _more_
     * @param results _more_
     * @param direction _more_
     * @param level _more_
     */
    private void doDfs(GraphNode node, Vector results, int direction,
                       int level) {
        results.addElement(node);
        Vector nodes = pruneNodes(node.getNodesToLayout(level, direction),
                                  level);
        for (int i = 0; i < nodes.size(); i++) {
            doDfs((GraphNode) nodes.elementAt(i), results, direction,
                  level + 1);
        }
    }


    /**
     * _more_
     *
     * @param node _more_
     * @param results _more_
     * @param direction _more_
     */
    private void doBfs(GraphNode node, Vector results, int direction) {
        int    cnt   = 0;
        Vector nodes = new Vector();
        nodes.addElement(node);
        int level = 1;
        while (nodes.size() > 0) {
            if (cnt++ > 1000) {
                System.err.println("Bad things");
                return;
            }
            node = (GraphNode) nodes.elementAt(0);
            nodes.removeElement(node);
            results.addElement(node);
            Vector others = pruneNodes(node.getNodesToLayout(level,
                                direction), level);
            for (int i = 0; i < others.size(); i++) {
                nodes.addElement(others.elementAt(i));
            }
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getLayoutRectilinear() {
        return layoutType == LAYOUT_RECTILINEAR;
    }



    /**
     * _more_
     *
     * @param node _more_
     * @param includeNonPathEdges _more_
     */
    private void doLayoutRelax(GraphNode node, boolean includeNonPathEdges) {
        Vector nodes = new Vector();

        //    if (!uniDirectional) {
        doBfs(node, nodes, layoutDirection);
        //      doBfs (node,  nodes, GraphNode.DIR_BOTH);
        //      doDfs (node, nodes, GraphNode.DIR_BOTH, 1);
        //    } else {
        //      doBfs (node,  nodes, GraphNode.DIR_OUT);
        //      doDfs (node, nodes, GraphNode.DIR_OUT,  1);
        //    }

        Vector    edges = new Vector();
        Hashtable seen  = new Hashtable();
        for (int test = 0; test < nodes.size(); test++) {
            GraphNode n = (GraphNode) nodes.elementAt(test);
            checkEdgesForNodeLoad(n.getAllEdges());
            Vector outEdges = n.getOutEdges();
            for (int i = 0; i < outEdges.size(); i++) {
                GraphEdge edge = (GraphEdge) outEdges.elementAt(i);
                if ((edge.getHead() == null)
                        || edge.getHead().getNeedsLayout()
                        || !edge.getVisible() || (seen.get(edge) != null)) {
                    continue;
                }
                seen.put(edge, edge);
                if (edge.getLevel() < 0) {
                    if ( !includeNonPathEdges) {
                        continue;
                    }
                    edge.setLevel(Math.max(edge.getHead().level,
                                           edge.getTail().level));
                }
                edges.addElement(edge);
            }
            Vector inEdges = n.getInEdges();
            for (int i = 0; i < inEdges.size(); i++) {
                GraphEdge edge = (GraphEdge) inEdges.elementAt(i);
                if ((edge.getTail() == null)
                        || edge.getTail().getNeedsLayout()
                        || !edge.getVisible() || (seen.get(edge) != null)) {
                    continue;
                }
                seen.put(edge, edge);
                if (edge.getLevel() < 0) {
                    if ( !includeNonPathEdges) {
                        continue;
                    }
                    edge.setLevel(Math.max(edge.getHead().level,
                                           edge.getTail().level));
                }
                edges.addElement(edge);
            }
        }

        //    System.err.println ("edges:" + edges);

        Dimension size = getSize();
        size.width  = translateInputX(size.width);
        size.height = translateInputY(size.height);
        double desiredLength = (0.20 * size.width) * tightness;
        int    cnt           = 50;

        for (int i = 1; i <= cnt; i++) {
            doRelax(i == cnt, nodes, edges, desiredLength);
        }
    }


    /**
     *  From Sun's example graph layout applet.
     *
     * @param last _more_
     * @param nodes _more_
     * @param edges _more_
     * @param desiredLength _more_
     */
    private void doRelax(boolean last, Vector nodes, Vector edges,
                         double desiredLength) {
        for (int i = 0; i < edges.size(); i++) {
            GraphEdge e    = (GraphEdge) edges.elementAt(i);
            GraphNode tail = e.getTail();
            GraphNode head = e.getHead();
            double    vx   = head.destX - tail.destX;
            double    vy   = head.destY - tail.destY;
            if (vx == 0.0) {
                vx = 1.0;
            }
            double len = Math.sqrt(vx * vx + vy * vy);
            if (len == 0.0) {
                len = 1.0;
            }
            int    radius = (tail.getRadius() + head.getRadius()) / 2;
            double force  = (desiredLength + radius - len) / (len * 3);
            double dx     = force * vx;
            double dy     = force * vy;
            head.dx += dx;
            head.dy += dy;
            tail.dx += -dx;
            tail.dy += -dy;
        }


        for (int i = 0; i < nodes.size(); i++) {
            GraphNode n1 = (GraphNode) nodes.elementAt(i);
            double    dx = 0.0;
            double    dy = 0.0;
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) {
                    continue;
                }
                GraphNode n2  = (GraphNode) nodes.elementAt(j);
                double    vx  = n1.destX - n2.destX;
                double    vy  = n1.destY - n2.destY;
                double    len = vx * vx + vy * vy;
                if (len == 0) {
                    dx += Math.random();
                    dy += Math.random();
                } else if (len < 10000) {
                    dx += vx / len;
                    dy += vy / len;
                }
            }
            double dlen = dx * dx + dy * dy;
            if (dlen > 0) {
                dlen  = Math.sqrt(dlen) / 2;
                n1.dx += dx / dlen;
                n1.dy += dy / dlen;
            }
        }

        for (int i = 0; i < nodes.size(); i++) {
            GraphNode n = (GraphNode) nodes.elementAt(i);
            if (n == draggedNode) {}
            else {
                double newX = Math.max(-5.0, Math.min(5.0, n.dx));
                double newY = Math.max(-5.0, Math.min(5.0, n.dy));
                n.destX += newX;
                n.destY += newY;
            }
            n.dx /= 2.0;
            n.dy /= 2.0;
        }
    }


    /**
     * _more_
     *
     * @param node _more_
     * @param vertical _more_
     */
    private void doLayoutHier(GraphNode node, boolean vertical) {
        boolean didOne = false;
        if ((layoutDirection == GraphNode.DIR_OUT)
                || (layoutDirection == GraphNode.DIR_BOTH)) {
            recurseHier(node, true, true, 40, 200, 1, vertical);
            didOne = true;

        }

        if ((layoutDirection == GraphNode.DIR_IN)
                || (layoutDirection == GraphNode.DIR_BOTH)) {
            recurseHier(node, false, !didOne, (vertical
                    ? getCenterX(node)
                    : getCenterY(node)), 200, 1, vertical);
        }
    }


    /**
     * _more_
     *
     * @param node _more_
     * @param down _more_
     * @param okToSetNode _more_
     * @param newX _more_
     * @param levelPosition _more_
     * @param level _more_
     * @param vertical _more_
     *
     * @return _more_
     */
    private int recurseHier(GraphNode node, boolean down,
                            boolean okToSetNode, int newX, int levelPosition,
                            int level, boolean vertical) {

        int currentX  = newX;
        int w         = node.getBounds().width;
        int h         = node.getBounds().height;
        int downCoeff = (down
                         ? 1
                         : -1);

        if (vertical) {
            int center = levelPosition + downCoeff * h / 2;
            if (okToSetNode) {
                setCenter(node, currentX, center);
            }
            levelPosition = center + downCoeff * h / 2;
        } else {
            int center = levelPosition + downCoeff * w / 2;
            if (okToSetNode) {
                setCenter(node, center, currentX);
            }
            levelPosition = center + downCoeff * w / 2;
        }
        levelPosition += (int) (tightness * downCoeff * 100);

        int minChildX = Integer.MAX_VALUE;
        int maxChildX = Integer.MIN_VALUE;

        int space     = (int) (25.0 * tightness);
        int cnt       = 0;
        if (Math.abs(level) > maxLevel) {
            return currentX + (vertical
                               ? w
                               : h) / 2;
        }
        Vector nodes = pruneNodes(node.getNodesToLayout(level, (down
                ? GraphNode.DIR_OUT
                : GraphNode.DIR_IN)), level);
        for (int i = 0; i < nodes.size(); i++) {
            GraphNode child = (GraphNode) nodes.elementAt(i);
            int       cw    = child.getBounds().width;
            int       ch    = child.getBounds().height;

            if (cnt++ > 0) {
                currentX += (vertical
                             ? cw
                             : ch) / 2 + space;
            }
            int rightSide = recurseHier(child, down, true, currentX,
                                        levelPosition, level + 1, vertical);
            if (vertical) {
                minChildX = Math.min(minChildX, getCenterX(child) - cw / 2);
                maxChildX = Math.max(maxChildX, getCenterX(child) + cw / 2);
            } else {
                minChildX = Math.min(minChildX, getCenterY(child) - ch / 2);
                maxChildX = Math.max(maxChildX, getCenterY(child) + ch / 2);
            }
            currentX = rightSide;
        }
        if (currentX == newX) {
            currentX += (vertical
                         ? w
                         : h) / 2;
        }
        if (cnt > 0) {
            if (vertical) {
                setCenter(node, minChildX + (maxChildX - minChildX) / 2,
                          getCenterY(node));
            } else {
                setCenter(node, getCenterX(node),
                          minChildX + (maxChildX - minChildX) / 2);
            }
        }
        return currentX;
    }

    /**
     * _more_
     *
     * @param node _more_
     * @param vertical _more_
     */
    private void doLayoutTree(GraphNode node, boolean vertical) {
        recurseTree(node, 20, 30, 1, vertical);
    }

    /**
     * _more_
     *
     * @param node _more_
     * @param currentX _more_
     * @param currentY _more_
     * @param level _more_
     * @param vertical _more_
     *
     * @return _more_
     */
    private int recurseTree(GraphNode node, int currentX, int currentY,
                            int level, boolean vertical) {


        int w = node.getBounds().width;
        int h = node.getBounds().height;
        if (vertical) {
            setCenter(node, currentX + w / 2, currentY - h / 2);
        } else {
            setCenter(node, currentY + w / 2, currentX - h / 2);
        }

        currentY += (vertical
                     ? h
                     : w);

        if (Math.abs(level) > maxLevel) {
            return currentY;
        }
        int space   = (int) (25.0 * tightness);
        int cnt     = 0;
        int padding = (int) (tightness * 20);
        Vector[] nodesAndEdges =
            pruneNodesAndEdges(node.getNodesToLayout(level, layoutDirection),
                               level);
        Vector nodes = nodesAndEdges[0];
        Vector edges = nodesAndEdges[1];
        currentX = currentX + (vertical
                               ? w
                               : h);
        int baseX = getCenterX(node);
        for (int i = 0; i < nodes.size(); i++) {
            currentY += padding;
            GraphNode child = (GraphNode) nodes.elementAt(i);
            currentY = recurseTree(child, currentX, currentY, level + 1,
                                   vertical);
            GraphEdge edge = (GraphEdge) edges.elementAt(i);
            if (edge.isTail(node)) {
                edge.joint = new Point(GraphEdge.TAILX, GraphEdge.HEADY);
            } else {
                edge.joint = new Point(GraphEdge.HEADX, GraphEdge.TAILY);
            }
        }
        return currentY;
    }





    /**
     *  This lays out the graph.
     *
     * @param g _more_
     */
    public void layoutGraph(Graphics g) {

        if (centerNode == null) {
            return;
        }

        if (draggedNode != null) {
            if (((layoutType != LAYOUT_RELAX1)
                    && (layoutType != LAYOUT_RELAX2)) || (layoutType == LAYOUT_NONE)) {
                needLayout = false;
                return;
            }
        }

        if (layoutType == LAYOUT_NONE) {
            return;
        }

        displayNodes = new Vector();
        //Prep all of the nodes, telling them they need to be laid out.
        for (int i = 0; i < allEdges.size(); i++) {
            GraphEdge edge = (GraphEdge) allEdges.elementAt(i);
            edge.setLevel(-1);
            edge.points = null;
            edge.joint  = null;
        }

        for (int i = 0; i < allNodes.size(); i++) {
            GraphNode node = (GraphNode) allNodes.elementAt(i);
            node.preLayout(g);
        }

        Rectangle b       = bounds();
        int       middleX = b.width / 2;
        int       middleY = b.height / 2;

        //Move the center node to the center of the canvas


        setIsLaidOut(centerNode);


        switch (layoutType) {

          case LAYOUT_RELAX1 :
          case LAYOUT_RELAX2 :
              checkNodeForLoad(centerNode);
              doLayoutRelax(centerNode, layoutType == LAYOUT_RELAX2);
              break;


          case LAYOUT_CIRCULAR1 :
              doLayoutCircular(centerNode, true);
              break;

          case LAYOUT_CIRCULAR2 :
              doLayoutCircular(centerNode, false);
              break;


          case LAYOUT_HTREE :
          case LAYOUT_VTREE :
              doLayoutTree(centerNode, layoutType == LAYOUT_VTREE);
              break;


          case LAYOUT_HHIER :
          case LAYOUT_VHIER :
              doLayoutHier(centerNode, layoutType == LAYOUT_VHIER);
              break;

          case LAYOUT_HISTORY : {
              int       x    = 10;
              int       y    = 20;
              Hashtable seen = new Hashtable();

              for (int i = 0; (i < history.size()) && (i <= historyIdx);
                      i++) {
                  GraphNode node = (GraphNode) history.elementAt(i);
                  if (seen.get(node) != null) {
                      continue;
                  }
                  seen.put(node, node);
                  setIsLaidOut(node);
                  setCenter(node, x + node.bounds.width / 2,
                            y + node.bounds.height / 2);
                  x += node.bounds.width + 10;
                  y += node.bounds.height + 10;
              }
              break;
          }


          case LAYOUT_RECTILINEAR :
          case LAYOUT_RADIAL : {
              setCenter(centerNode, middleX, middleY);
              setIsLaidOut(centerNode);
              centerNode.level = 0;
              //Calculate the radius from the current center node
              //and start the layout around the center node with full 360 deg. circle
              double radius = (double) ((0.7 * tightness
                                         * (double) Math.min(middleX,
                                             middleY)) - 10.0);
              doLayoutRadial(centerNode, 0.0, 360.0, radius, 0);
              break;
          }

        }

        //Hide  any nodes that aren't laid out
        for (int i = 0; i < allNodes.size(); i++) {
            GraphNode p = (GraphNode) allNodes.elementAt(i);
            if (p.getNeedsLayout() || p.elided) {
                int dy = Math.min(Math.abs(p.currentY),
                                  Math.abs(b.height - p.currentY));
                int dx = Math.min(Math.abs(p.currentX),
                                  Math.abs(b.width - p.currentX));
                if (dx < dy) {
                    p.currentX = ((p.currentX > middleX)
                                  ? b.width
                                  : 0);
                } else {
                    p.currentY = ((p.currentY > middleY)
                                  ? b.height
                                  : 0);
                }
                p.setVisible(false);
            } else {
                p.setVisible(true);
                p.postLayout(g);
            }
        }



        //Now go thru the displayed nodes and find the bounds
        for (int i = 0; i < displayNodes.size(); i++) {
            GraphNode p = (GraphNode) displayNodes.elementAt(i);
            //Check if this node has incident nodes that have not been laid out
            for (int j = 0; j < p.getOutEdges().size(); j++) {
                GraphEdge edge = p.getOutEdge(j);
                if (edge.getHead() == null) {
                    p.haveOthersNotLoaded = true;
                    p.haveOthers          = true;
                    continue;
                }
                if (edge.getHead().getNeedsLayout()) {
                    p.haveOthers = true;
                    break;
                }
            }

            for (int j = 0; j < p.inEdges.size(); j++) {
                GraphEdge edge = p.getInEdge(j);
                if (edge.getTail() == null) {
                    p.haveOthersNotLoaded = true;
                    p.haveOthers          = true;
                    continue;
                }
                if (edge.getTail().getNeedsLayout()) {
                    p.haveOthers = true;
                    break;
                }
            }
        }

        double totalDistance = 0.0;
        double maxDistance   = 0.0;
        for (int i = 0; i < displayNodes.size(); i++) {
            GraphNode node = (GraphNode) displayNodes.elementAt(i);
            double distance = distance(node.currentX, node.currentY,
                                       (int) node.destX, (int) node.destY);
            if ((node.currentX >= 0) && (node.currentX < b.width)
                    && (node.currentY >= 0) && (node.currentY < b.height)) {
                totalDistance += distance;
            }
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }
        if (centerNode != null) {
            maxDistance = distance(centerNode.currentX, centerNode.currentY,
                                   (int) centerNode.destX,
                                   (int) centerNode.destY);
        }
        double avgDistance       = ((displayNodes.size() > 0)
                                    ? totalDistance / displayNodes.size()
                                    : 0.0);

        double DISTANCE_PER_TICK = 20.0;
        animTicks = Math.min(15,
                             Math.max(3, (int) (avgDistance
                                 / DISTANCE_PER_TICK) + 1));
        totalTicks = animTicks;

        centerGraph();

        needLayout = false;
        repaint();


        Vector tmpList = displayNodes;
        displayNodes = new Vector();
        for (int i = 0; i < tmpList.size(); i++) {
            GraphNode p = (GraphNode) tmpList.elementAt(i);
            for (int j = 0; j < displayNodes.size(); j++) {
                GraphNode q = (GraphNode) displayNodes.elementAt(j);
                if (p.level >= q.level) {
                    displayNodes.insertElementAt(p, j);
                    p = null;
                    break;
                }
            }
            if (p != null) {
                displayNodes.addElement(p);
            }
        }

        displayEdges = new Vector();
        Hashtable checkedEdges = new Hashtable();


        for (int i = 0; i < displayNodes.size(); i++) {
            GraphNode p = (GraphNode) displayNodes.elementAt(i);
            for (int j = 0; j < p.inEdges.size(); j++) {
                GraphEdge edge = (GraphEdge) p.inEdges.elementAt(j);
                if (checkedEdges.get(edge) != null) {
                    continue;
                }
                checkedEdges.put(edge, edge);
                if (tailAndHeadVisible(edge) && okToShowEdge(edge)) {
                    displayEdges.addElement(edge);
                }
            }
            for (int j = 0; j < p.getOutEdges().size(); j++) {
                GraphEdge edge = (GraphEdge) p.getOutEdges().elementAt(j);
                if (checkedEdges.get(edge) != null) {
                    continue;
                }
                checkedEdges.put(edge, edge);
                if (tailAndHeadVisible(edge) && okToShowEdge(edge)) {
                    displayEdges.addElement(edge);
                }
            }
        }

    }

    /**
     * _more_
     */
    private void centerGraph() {
        boolean shiftX = true;
        boolean shiftY = true;
        switch (layoutType) {

          case LAYOUT_RELAX1 :
          case LAYOUT_RELAX2 :
              shiftX = (draggedNode == null);
              shiftY = (draggedNode == null);
              break;


          case LAYOUT_HISTORY :
          case LAYOUT_HTREE :
          case LAYOUT_VTREE :
              shiftX = false;
              shiftY = false;
              break;
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        //Now go thru the displayed nodes and find the bounds
        for (int i = 0; i < displayNodes.size(); i++) {
            GraphNode p = (GraphNode) displayNodes.elementAt(i);
            minX = Math.min(getCenterX(p), minX);
            minY = Math.min(getCenterY(p), minY);
            maxX = Math.max(getCenterX(p), maxX);
            maxY = Math.max(getCenterY(p), maxY);
        }


        int       centerX = minX + (maxX - minX) / 2;
        int       centerY = minY + (maxY - minY) / 2;


        Dimension size    = getSize();
        size.width  = translateInputX(size.width);
        size.height = translateInputY(size.height);


        int top    = 0;
        int bottom = size.height;
        int left   = 0;
        int right  = size.width;

        int dY     = (bottom / 2 - centerY);
        int dX     = (right / 2 - centerX);


        if ((getCenterY(centerNode) + dY) < top) {
            dY += (top - (getCenterY(centerNode) + dY));
        } else if ((getCenterY(centerNode) + dY) > bottom) {
            dY += ((getCenterY(centerNode) + dY) - bottom);
        }

        if ((getCenterX(centerNode) + dX) < left) {
            dX += (left - (getCenterX(centerNode) + dX));
        } else if ((getCenterX(centerNode) + dX) > right) {
            dX += ((getCenterX(centerNode) + dX) - right);
        }


        for (int i = 0; i < displayNodes.size(); i++) {
            GraphNode node = (GraphNode) displayNodes.elementAt(i);
            setCenterOffset(node, (shiftX
                                   ? dX
                                   : 0), (shiftY
                                          ? dY
                                          : 0));
        }
        if (animTicks == 0) {
            animTicks = 1;
        }
    }

    /**
     * _more_
     *
     * @param edge _more_
     *
     * @return _more_
     */
    public boolean tailAndHeadVisible(GraphEdge edge) {
        if (edge.getTail() == null) {
            return false;
        }
        if (edge.getHead() == null) {
            return false;
        }
        if ( !edge.getTail().getVisible()) {
            return false;
        }
        if ( !edge.getHead().getVisible()) {
            return false;
        }
        return true;
    }

    /**
     * _more_
     *
     * @param list _more_
     *
     * @return _more_
     */
    public Vector getVisibleNodes(Vector list) {
        Vector visible = new Vector();
        for (int i = 0; i < list.size(); i++) {
            GraphNode n = (GraphNode) list.elementAt(i);
            if ( !(n.elided)) {
                visible.addElement(n);
            }
        }
        return visible;
    }

    /**
     * _more_
     *
     * @param edge _more_
     *
     * @return _more_
     */
    public boolean okToShowEdge(GraphEdge edge) {
        if (edge.edgeType != null) {
            Boolean b = (Boolean) edgeHideMap.get(edge.edgeType);
            if (b != null) {
                return b.booleanValue();
            }
        }
        return true;
    }


    /**
     * _more_
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

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public GraphShape find(int x, int y) {
        for (int i = displayNodes.size() - 1; i >= 0; i--) {
            GraphNode  p             = (GraphNode) displayNodes.elementAt(i);
            GraphShape containsShape = p.contains(x, y);
            if (containsShape != null) {
                return containsShape;
            }
        }
        return null;
    }



    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseClicked(MouseEvent e) {
        //Right click?
        if ((e.getModifiers() & e.BUTTON1_MASK) == 0) {
            if (hilite == null) {
                JPopupMenu popup = new JPopupMenu();
                JMenu      menu;
                popup.add(menu = new JMenu("History"));
                menu.add(makeMenuItem("Back", CMD_NAV_BACK, historyIdx > 0));
                menu.add(makeMenuItem("Forward", CMD_NAV_FWD,
                                      historyIdx < history.size() - 1));
                popup.add(menu = new JMenu("File"));
                xmlUi.addMenuItems(menu, "filemenu");
                popup.add(menu = new JMenu("View"));
                xmlUi.addMenuItems(menu, "viewmenu");
                popup.add(menu = new JMenu("Layout"));
                xmlUi.addMenuItems(menu, "layoutmenu");
                doPopup(popup, this, e.getX(), e.getY());
            } else {
                popupNode = hilite;
                showNodeMenu(hilite, this, e.getX(), e.getY());
            }
        }
    }

    /**
     * _more_
     *
     * @param label _more_
     * @param command _more_
     * @param enabled _more_
     *
     * @return _more_
     */
    JMenuItem makeMenuItem(String label, String command, boolean enabled) {
        return makeMenuItem(label, command, enabled, null);
    }


    /**
     * _more_
     *
     * @param label _more_
     * @param command _more_
     * @param enabled _more_
     * @param node _more_
     *
     * @return _more_
     */
    JMenuItem makeMenuItem(String label, String command, boolean enabled,
                           GraphNode node) {
        if (node != null) {
            label = node.processTemplate(label);
        }
        JMenuItem mi = new JMenuItem(label);
        mi.setActionCommand(command);
        mi.addActionListener(this);
        mi.setEnabled(enabled);
        return mi;
    }

    /**
     * _more_
     *
     * @param node _more_
     * @param src _more_
     * @param x _more_
     * @param y _more_
     */
    public void showNodeMenu(GraphNode node, Component src, int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        if (needsToLoad(node)) {
            popup.add(makeMenuItem("Load", "menu:" + CMD_LOAD, true, node));
        }
        popup.add(makeMenuItem("Remove", CMD_REMOVE + "(" + node.id + ")",
                               true, node));

        popup.addSeparator();
        popup = doNodeMenu(node, canvasXml.getChildren(), popup);
        Vector types = node.getTypeList();
        for (int i = 0; i < types.size(); i++) {
            XmlNode tmpType = (XmlNode) types.elementAt(i);
            popup = doNodeMenu(node, tmpType.getChildren(), popup);
        }
        popup = doNodeMenu(node, node.getNodeXml().getChildren(), popup);
        if (popup != null) {
            doPopup(popup, src, x, y);
        }
    }


    /**
     * _more_
     *
     * @param node _more_
     * @param children _more_
     * @param popup _more_
     *
     * @return _more_
     */
    public JPopupMenu doNodeMenu(GraphNode node, Vector children,
                                 JPopupMenu popup) {
        if (children == null) {
            return popup;
        }
        for (int i = 0; i < children.size(); i++) {
            XmlNode child = (XmlNode) children.elementAt(i);
            if ( !child.getTag().equals(TAG_COMMAND)) {
                continue;
            }
            if (popup == null) {
                popup = new JPopupMenu();
            }
            String label   = child.getAttribute(ATTR_LABEL);
            String command = child.getAttribute(ATTR_COMMAND);
            popup.add(makeMenuItem(label, "menu:" + command, true, node));
        }
        return popup;
    }


    /**
     * _more_
     *
     * @param popup _more_
     * @param src _more_
     * @param x _more_
     * @param y _more_
     */
    public void doPopup(JPopupMenu popup, Component src, int x, int y) {
        //    this.add (popup);
        popup.show(src, x, y);
    }



    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseExited(MouseEvent e) {}

    /** _more_          */
    int clickX = 0;

    /** _more_          */
    int clickY = 0;

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mousePressed(MouseEvent e) {
        requestFocus();
        clickX = e.getX();
        clickY = e.getY();
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseDragged(MouseEvent e) {
        clearMouseOver(this);
        if (hilite == null) {
            draggedNode = null;
            return;
        }
        hilite.moveBy(scaleInputX(e.getX() - clickX),
                      scaleInputY(e.getY() - clickY));
        hilite.moveDestBy(scaleInputX(e.getX() - clickX),
                          scaleInputY(e.getY() - clickY));

        clickX      = e.getX();
        clickY      = e.getY();

        draggedNode = hilite;
        if ( !e.isControlDown()) {
            needLayout = true;
        }
        repaint();
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseReleased(MouseEvent e) {
        //If right mouse then do nothing
        if (draggedNode != null) {
            draggedNode = null;
            return;
        }
        draggedNode = null;
        if ((e.getModifiers() & e.BUTTON1_MASK) == 0) {
            return;
        }
        int x = translateInputX(e.getX());
        int y = translateInputY(e.getY());


        if (hilite != null) {
            if (e.isShiftDown()) {
                doClick(hilite, hiliteShape, "shift");
            } else if (e.isControlDown()) {
                doClick(hilite, hiliteShape, "control");
            } else {
                if (hiliteShape.href != null) {
                    handleCommands(hiliteShape.href, hilite, hiliteShape,
                                   null);
                } else {
                    nodeSelect(hilite);
                }
            }
        }
    }



    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseMoved(MouseEvent e) {
        if ( !showingMouseOver) {
            clearMouseOver(this);
        }
        int        x             = translateInputX(e.getX());
        int        y             = translateInputY(e.getY());
        GraphShape containsShape = find(x, y);
        GraphNode  closest       = ((containsShape != null)
                                    ? containsShape.glyph
                                    : null);

        if ((hiliteShape != containsShape) || (hilite != closest)) {
            if (hilite != closest) {
                clearMouseOver(this);
            }
            hiliteShape = containsShape;
            setCursor(DEFAULT_CURSOR);
            if (hilite != null) {
                hilite.setIsHilite(false);
            }
            hilite = closest;
            if (hilite != null) {
                hilite.setIsHilite(true);
            }
            repaint();
            if (hilite != null) {
                xmlUi.setLabel("message2", getTooltip(hilite));
                String msg = containsShape.getAttr("message");
                if (msg == null) {
                    msg = hilite.getAttr("message", "Click to center");
                }
                xmlUi.message(msg);
                if (containsShape.href != null) {
                    setCursor(HAND_CURSOR);
                }
            } else {
                xmlUi.setLabel("message2",
                               "Center node:" + getTooltip(centerNode));
                xmlUi.setLabel("message", "");
                //      xmlUi.message ("");
            }
        }
    }


    /**
     * _more_
     *
     * @param p _more_
     * @param shape _more_
     * @param prefix _more_
     */
    public void doClick(GraphNode p, GraphShape shape, String prefix) {
        XmlNode node = getNode(p, prefix + "click");
        if (node == null) {
            return;
        }
        String commands = node.getAttribute(ATTR_COMMAND);
        if (commands == null) {
            return;
        }
        handleCommands(commands, p, shape, null);
    }

    /**
     * _more_
     *
     * @param p _more_
     * @param tag _more_
     *
     * @return _more_
     */
    public XmlNode getNode(GraphNode p, String tag) {
        XmlNode xmlNode;
        xmlNode = p.getNodeXml().getChild(tag);
        if (xmlNode != null) {
            return xmlNode;
        }
        xmlNode = p.getTypeXml().getChild(tag);
        if (xmlNode != null) {
            return xmlNode;
        }
        if (graphXml != null) {
            xmlNode = graphXml.getChild(tag);
            if (xmlNode != null) {
                return xmlNode;
            }
        }
        if (canvasXml != null) {
            xmlNode = canvasXml.getChild(tag);
            if (xmlNode != null) {
                return xmlNode;
            }
        }
        return null;
    }




    /**
     * _more_
     *
     * @param commands _more_
     * @param node _more_
     * @param shape _more_
     * @param ae _more_
     */
    protected void handleCommands(String commands, GraphNode node,
                                  GraphShape shape, ActionEvent ae) {
        Vector cmds = GuiUtils.parseCommands(commands);
        for (int i = 0; i < cmds.size(); i++) {
            String[] sa     = (String[]) cmds.elementAt(i);
            String   func   = sa[0];
            String   params = sa[1];
            try {
                handleFunction(func, params, node, shape, ae);
            } catch (Exception exc) {
                print("Handling action:" + func + "\n" + exc);
                exc.printStackTrace();
            }
        }
    }

    /**
     * _more_
     */
    protected void setNeedLayout() {
        needLayout = true;
        repaint();
    }

    /**
     * _more_
     */
    protected void setNodesDirty() {
        for (int i = 0; i < allNodes.size(); i++) {
            ((GraphNode) allNodes.elementAt(i)).dirty = true;
        }
        setNeedLayout();
    }


    /**
     * _more_
     */
    protected void setShapeVisibility() {
        for (int i = 0; i < allNodes.size(); i++) {
            ((GraphNode) allNodes.elementAt(i)).setShapeVisibility();
        }
        setNeedLayout();
    }

    /**
     * _more_
     */
    protected void resetNodesDisplay() {
        for (int i = 0; i < allNodes.size(); i++) {
            ((GraphNode) allNodes.elementAt(i)).setDisplayAttributes();
        }
        setNeedLayout();
    }

    /**
     * _more_
     *
     * @param func _more_
     * @param params _more_
     * @param node _more_
     * @param shape _more_
     * @param ae _more_
     *
     * @throws Exception _more_
     */
    protected void handleFunction(String func, String params, GraphNode node,
                                  GraphShape shape, ActionEvent ae)
            throws Exception {

        params = params.trim();

        if (func.equals(CMD_REMOVE)) {
            GraphNode theNode = (GraphNode) idToNode.get(params);
            if (theNode != null) {
                removeNodeFromList(theNode, theNode.getTypeName());
                theNode.remove();
                if (hilite == theNode) {
                    hilite = null;
                }
                allNodes.removeElement(theNode);
                setNeedLayout();
            }
            return;
        }


        if (func.equals(CMD_LOAD)) {
            addNodeToLoad(node.id, node, false);
            repaint();
            return;
        }


        if (func.equals(CMD_URL)) {
            String[] args   = XmlUi.extractTwoArgs(params);
            String   window = args[0];
            String   url    = node.processTemplate(args[1]);
            graphApplet.showUrl(url, window);
            return;
        }

        if (func.equals(CMD_MAXCONNECTIONS)) {
            maxConnections = new Integer(params).intValue();
            setNeedLayout();
            return;
        }


        if (func.equals(CMD_TIGHTNESS)) {
            tightness = new Double(params).doubleValue();
            setNeedLayout();
            return;
        }

        if (func.equals(CMD_TITLEATTR)) {
            String[] args  = XmlUi.extractTwoArgs(params);
            String   attr  = args[0];
            boolean  value = new Boolean(args[1]).booleanValue();
            if (value) {
                titleAttributes.addElement(attr);
            } else {
                titleAttributes.removeElement(attr);
            }
            resetNodesDisplay();
            return;
        }

        if (func.equals(CMD_NEW)) {
            graphApplet.makeView();
            return;
        }

        if (func.equals(CMD_LAYOUT)) {
            layoutType = GuiUtils.getIndex(LAYOUT_NAMES, null, params,
                                           layoutType);
            setNeedLayout();
            return;
        }


        if (func.equals(CMD_LEVEL)) {
            maxLevel = new Integer(params).intValue();
            setNeedLayout();
            return;
        }

        //    System.err.println ("func:" + func);

        if (func.equals(CMD_RELOAD)) {
            graphApplet.reload();
            return;
        }


        if (func.equals(CMD_SHAPEFLAG)) {
            String[] args = XmlUi.extractTwoArgs(params);
            String   flag = args[0];
            Boolean  value;
            if (args[1].equals("toggle")) {
                value = (Boolean) shapeVisibility.get(flag);
                if (value == null) {
                    value = new Boolean(true);
                }
                value = new Boolean( !value.booleanValue());
            } else {
                value = new Boolean(args[1]);
            }
            shapeVisibility.put(flag, value);
            setShapeVisibility();
            return;
        }

        if (func.equals(CMD_FLAG)) {
            String[] args  = XmlUi.extractTwoArgs(params);
            String   flag  = args[0];
            boolean  value = new Boolean(args[1]).booleanValue();

            //      System.err.println ("setting flag: " + flag +" to:" + value);

            if (flag.equals("showMouseOver")) {
                showMouseOver = value;
            } else if (flag.equals("pauseMouseOver")) {
                pauseMouseOver = value;
            } else if (flag.equals("scaleWithLevel")) {
                scaleWithLevel = value;
                setNodesDirty();
            } else if (flag.equals("showAllEdges")) {
                showAllEdges = value;
                repaint();
            } else if (flag.equals("showAllLabels")) {
                showAllLabels = value;
                setNodesDirty();
            } else if (flag.equals("uniDirectional")) {
                //      uniDirectional = value;
                setNeedLayout();
            } else if (flag.equals("relativeToLevel")) {
                relativeToLevel = value;
                setNeedLayout();
            } else if (flag.equals("animateMoves")) {
                animateMoves = value;
            } else {
                System.err.println("Unknown flag:" + flag);
            }
            return;
        }



        if (func.equals(CMD_DIRECTION)) {
            if (params.equals("in")) {
                layoutDirection = GraphNode.DIR_IN;
            } else if (params.equals("out")) {
                layoutDirection = GraphNode.DIR_OUT;
            } else if (params.equals("both")) {
                layoutDirection = GraphNode.DIR_BOTH;
            } else {
                System.err.println("Bad layout direction:" + params);
            }
            setNeedLayout();
            return;
        }


        if (func.equals(CMD_SHOWNODELIST)) {
            if (nodeListFrame == null) {
                Container nodes = GuiUtils.doLayout(new Component[] {
                                      nodePanel,
                                      new Label("Connections"),
                                      edgePanel }, 1, GuiUtils.DS_Y,
                                          GuiUtils.DS_YNN);
                nodeListFrame = makeWindow(nodes, false,
                                           graphApplet.getBackground());
                nodeListFrame.setSize(150, 400);
                nodeListFrame.setLocation(10, 10);
            }
            nodeListFrame.show();
            return;
        }


        if (func.equals(CMD_FLOAT)) {
            needLayout = true;
            toggleFloat();
            return;
        }


        if (func.equals(CMD_NAV_BACK)) {
            doNav(historyIdx - 1);
        } else if (func.equals(CMD_NAV_FWD)) {
            doNav(historyIdx + 1);
        } else if (func.equals(CMD_NAV_FWDFWD)) {
            doNav(history.size() - 1);
        } else if (func.equals(CMD_NAV_BACKBACK)) {
            doNav(0);
        } else {
            super.handleFunction(func, params, null, ae);
        }

    }

    /**
     * _more_
     *
     * @param newIdx _more_
     */
    private void doNav(int newIdx) {
        if ((newIdx >= 0) && (newIdx < history.size())) {
            historyIdx = newIdx;
            GraphNode n = (GraphNode) history.elementAt(newIdx);
            nodeSelect(n, false, 0);
        }
    }

    /**
     * _more_
     */
    private void toggleFloat() {
        if (floatFrame != null) {
            if ( !isMain) {
                stop();
                graphApplet.removeView(this);
                return;
            }
            xmlUi.setLabel("floatbutton", "Float window");
            graphApplet.add("Center", getContents());
            floatFrame.dispose();
            floatFrame = null;
            this.invalidate();
            graphApplet.validate();
        } else {
            xmlUi.setLabel("floatbutton", "Close");
            floatFrame = makeWindow(getContents(), true,
                                    graphApplet.getBackground());
        }
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowClosing(WindowEvent e) {
        if (e.getWindow() == floatFrame) {
            toggleFloat();
        } else {
            super.windowClosing(e);
        }
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
        needLayout = true;
    }


    /**
     * _more_
     */
    public void stop() {
        if (floatFrame != null) {
            floatFrame.dispose();
            floatFrame = null;
        }
        if (nodeListFrame != null) {
            nodeListFrame.dispose();
        }

    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void actionPerformed(ActionEvent e) {
        String    cmd  = e.getActionCommand();
        GraphNode node = null;
        if (cmd.startsWith("menu:")) {
            cmd  = cmd.substring(5);
            node = popupNode;
        }
        handleCommands(cmd, node, null, e);
    }

    /**
     * _more_
     *
     * @param delta _more_
     */
    public void rescale(int delta) {
        super.rescale(delta);
        double scale = getScaleFactor();
        for (int i = 0; i < allNodes.size(); i++) {
            GraphNode node = (GraphNode) allNodes.elementAt(i);
            node.setScale(scale);
        }
        for (int i = 0; i < allEdges.size(); i++) {
            GraphEdge edge = (GraphEdge) allEdges.elementAt(i);
            edge.setScale(scale);
        }
        centerGraph();
    }


    /**
     * _more_
     *
     * @param n _more_
     */
    public void nodeSelect(GraphNode n) {
        nodeSelect(n, true, 0);
    }


    /**
     * _more_
     *
     * @param n _more_
     * @param doHistory _more_
     * @param dir _more_
     */
    public void nodeSelect(GraphNode n, boolean doHistory, int dir) {
        if (centerNode == n) {
            return;
        }

        if (centerNode != null) {
            centerNode.setIsCenterNode(false);
        }
        hilite = null;
        if (centerNode != null) {
            GraphNode tmp = centerNode;
            centerNode = null;
            tmp.setIsCenterNode(false);
        }
        centerNode = n;

        if (centerNode != null) {
            centerNode.setIsCenterNode(true);
            doClick(centerNode, null, "");
            checkNodeForLoad(centerNode);
            xmlUi.setLabel("message2",
                           "Center item:" + getTooltip(centerNode));
        } else {
            xmlUi.setLabel("message", "");
        }
        needLayout = true;
        repaint();
        JList  selectedList = (JList) nodeTypeToList.get(n.getTypeName());
        String nodeType     = (String) listToNodeType.get(selectedList);
        Vector inList       = (Vector) nodeTypeToVector.get(nodeType);
        for (int i = 0; i < nodeLists.size(); i++) {
            JList nodeList = (JList) nodeLists.elementAt(i);
            //      nodeList.deselect (nodeList.getSelectedIndex ());
            nodeList.clearSelection();
        }
        selectedList.setSelectedIndex(inList.indexOf(n));

        if (doHistory) {
            //Prune history
            int i = historyIdx + 1;
            try {
                while (i < history.size()) {
                    history.removeElementAt(i);
                }
            } catch (Exception exc) {}

            history.addElement(centerNode);
            historyIdx++;
        }
        enableNavButtons();

    }

    /**
     * _more_
     */
    private void enableNavButtons() {
        if (xmlUi != null) {
            xmlUi.enable("nav.backback", historyIdx > 0);
            xmlUi.enable("nav.back", historyIdx > 0);
            xmlUi.enable("nav.fwd", historyIdx < history.size() - 1);
            xmlUi.enable("nav.fwdfwd", historyIdx < history.size() - 1);
        }
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void keyPressed(KeyEvent e) {
        boolean changed = true;
        if (e.getKeyCode() == e.VK_UP) {
            vTrans += 10;
        } else if (e.getKeyCode() == e.VK_DOWN) {
            vTrans -= 10;
        } else if (e.getKeyCode() == e.VK_RIGHT) {
            hTrans -= 10;
        } else if (e.getKeyCode() == e.VK_LEFT) {
            hTrans += 10;
        } else if (e.getKeyChar() == '=') {
            hTrans = 0;
            vTrans = 0;
        } else if (e.getKeyCode() == e.VK_ENTER) {
            return;
        } else {
            changed = false;
        }
        if (changed) {
            repaint();
        }
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void keyReleased(KeyEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void keyTyped(KeyEvent e) {}




    /** _more_          */
    private boolean repaintPending = false;

    /**
     * _more_
     *
     * @param ms _more_
     */
    private void repaintInABit(final long ms) {
        repaintPending = true;
        Thread repaintThread = new Thread() {
            public void run() {
                try {
                    sleep(ms);
                    repaint();
                    repaintPending = false;
                } catch (Exception exc) {}
            }
        };
        repaintThread.start();
    }


    /** _more_          */
    long lastRepaintTime = 0;


    /**
     * _more_
     *
     * @param g _more_
     */
    private void paintLoading(Graphics g) {
        if (lastRepaintTime == 0) {
            lastRepaintTime = System.currentTimeMillis();
        } else {
            long now = System.currentTimeMillis();
            if ((now - lastRepaintTime) > 500) {
                lastRepaintTime = now;
                loadingString   += ".";
            }
        }
        g.setColor(Color.black);
        int y = 50;
        if (bgImage != null) {
            y = bgImage.getHeight(null) + 20;
        }
        g.setFont(labelFont);
        g.drawString("Loading" + loadingString, translateInputX(10),
                     translateInputY(y));
        if (loadingString.length() > 5) {
            loadingString = "";
        }
        if ( !repaintPending && ( !animateMoves || (animTicks <= 0))) {
            repaintInABit(750);
        }
    }

    /** _more_          */
    Hashtable mouseOverMap = new Hashtable();

    /** _more_          */
    boolean showingMouseOver = false;


    /**
     * _more_
     *
     * @param comp _more_
     */
    public void clearMouseOver(Component comp) {
        mouseOverMap.remove(comp);
        showingMouseOver = false;

    }


    /*
      private void test (Graphics g) {
        System.err.println ("test");
        try {
          Image image = createImage (40,10);
          Graphics imageGraphics =  image.getGraphics ();
          imageGraphics.drawLine (2,2,28,10);
          com.infocetera.chart.GIFEncoder encoder = new com.infocetera.chart.GIFEncoder (image, true);
          ByteArrayOutputStream os = new ByteArrayOutputStream ();
          encoder.Write (os);
          byte[] bytes= os.toByteArray();
          Image i2 =   Toolkit.getDefaultToolkit ().createImage (bytes);
          System.err.println ("Got:" + bytes.length + " w:" + i2.getWidth (null) +
              "X" +i2.getHeight (null));
          g.drawImage (i2, 0, 0, Color.red, null);
        } catch (Exception exc) {
          System.err.println ("An error has occurred:" + exc);
          exc.printStackTrace ();
        }
      }
    */


    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintInner(Graphics g) {

        //    test (g);
        showingMouseOver = false;

        if (fatalErrorMsg != null) {
            drawMouseOver(g, fatalErrorMsg, 10, 10);
            return;
        }

        Rectangle b = bounds();
        if (bgImage != null) {
            int x = (b.width / 2) - bgImage.getWidth(null) / 2;
            int y = (b.height / 2) - bgImage.getHeight(null) / 2;
            g.drawImage(bgImage, shiftInputX(2), shiftInputY(0), null, null);
        }


        if ((message != null) && (message.length() > 0)) {
            drawMouseOver(g, message, 10, 10);
        }


        if (amLoading || !haveInited) {
            paintLoading(g);
        } else {
            lastRepaintTime = 0;
            loadingString   = "";
        }



        if ( !haveInited || (centerNode == null)) {
            return;
        }

        if (needLayout) {
            layoutGraph(g);
            needLayout = false;
        }

        //Check for any incremental node loads
        loadNodes();

        if (animTicks > 0) {
            if ( !animateMoves) {
                animTicks = 1;
            }
            for (int i = 0; i < displayNodes.size(); i++) {
                GraphNode node = (GraphNode) displayNodes.elementAt(i);
                int       dx, dy;
                if ((animTicks == 1) || (totalTicks == 0)) {
                    dx = (int) (node.destX + 0.5) - node.currentX;
                    dy = (int) (node.destY + 0.5) - node.currentY;
                } else {
                    dx = ((int) (node.destX + 0.5) - node.originalX)
                         / totalTicks;
                    dy = ((int) (node.destY + 0.5) - node.originalY)
                         / totalTicks;
                }
                node.moveBy(dx, dy);
            }
        }


        for (int i = 0; i < displayNodes.size(); i++) {
            GraphNode node = (GraphNode) displayNodes.elementAt(i);
            node.cleanup(g);
        }


        GraphNode tmpHilite = hilite;

        if ((hiliteShape != null) && (hiliteShape.href != null)) {
            tmpHilite = null;
        }

        for (int i = 0; i < displayEdges.size(); i++) {
            GraphEdge edge = (GraphEdge) displayEdges.elementAt(i);
            edge.paint(this, g, centerNode, tmpHilite, false);
        }

        for (int i = 0; i < displayEdges.size(); i++) {
            GraphEdge edge = (GraphEdge) displayEdges.elementAt(i);
            edge.paint(this, g, centerNode, tmpHilite, true);
        }



        for (int i = 0; i < displayNodes.size(); i++) {
            GraphNode node = (GraphNode) displayNodes.elementAt(i);
            node.paint(this, g, centerNode == node, node == hilite);
        }

        if (animateMoves) {
            if (animTicks > 0) {
                repaintInABit(100);
                animTicks--;
            }
        } else {
            animTicks = 0;
        }

        if (hilite != null) {
            hilite.paint(this, g, centerNode == hilite, true);
            paintMouseOver(g);
        }


    }

    /**
     * _more_
     *
     * @param g _more_
     */
    private void paintMouseOver(Graphics g) {
        if (pauseMouseOver) {
            Long last     = (Long) mouseOverMap.get(this);
            long sleepFor = 0;
            long waitTime = 1000;
            if (last != null) {
                long then      = last.longValue();
                long waitedFor = System.currentTimeMillis() - then;
                if (waitedFor < waitTime) {
                    sleepFor = waitTime - waitedFor;
                }
            } else {
                mouseOverMap.put(this, new Long(System.currentTimeMillis()));
                sleepFor = waitTime;
            }
            if (sleepFor > 0) {
                final long sleepForFinal = sleepFor;
                Thread     t             = new Thread() {
                    public void run() {
                        try {
                            Thread.currentThread().sleep(sleepForFinal);
                            repaint();
                        } catch (Exception exc) {}
                    }
                };
                t.start();
                return;
            }
        }
        String    mouseOver = hilite.mouseOver;
        boolean   ok        = showMouseOver;
        Rectangle bounds    = hilite.getBounds();
        if ((hiliteShape != null) && (hiliteShape.href != null)
                && (hiliteShape.alt != null)) {
            mouseOver = hiliteShape.alt;
            ok        = true;
            bounds    = hiliteShape.searchBounds;
        }
        if ((mouseOver != null) && ok) {
            int x = bounds.x + bounds.width - 2;
            int y = bounds.y + bounds.height - 4;
            mouseOver        = hilite.processTemplate(mouseOver);
            showingMouseOver = true;
            Point bottom = drawMouseOver(g, mouseOver, x, y);
        }

    }




    /**
     * _more_
     *
     * @param g _more_
     * @param t _more_
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    private Point drawMouseOver(Graphics g, String t, int x, int y) {

        g.setFont(theFont);
        FontMetrics fm         = g.getFontMetrics();
        int         maxd       = fm.getMaxDescent();
        int         width      = 0;
        int         lineHeight = maxd + fm.getMaxAscent();

        //    System.err.println ("T:"+t+":");
        //    System.err.println ("xy:"+x +" " + y);

        HtmlGlyph glyph = new HtmlGlyph(this, t, false);
        glyph.doLayout(x, y, 350, 2);
        Rectangle gb = new Rectangle(glyph.getBounds());
        gb.x      = scale(gb.x);
        gb.y      = scale(gb.y);
        gb.width  = scale(gb.width);
        gb.height = scale(gb.height);

        //    System.err.println ("x y first:" + x + " " + y);

        Rectangle b = bounds();
        if (y + gb.height > b.height) {
            y -= ((y + gb.height) - b.height + 2);
        }
        if (x + gb.width > b.width) {
            x -= ((x + gb.width) - b.width + 2);
        }
        if (x < 0) {
            x = 0;
        }
        //    System.err.println ("x y second:" + x + " " + y);
        glyph.moveTo(x, y);
        gb.x      = x - 2;
        gb.y      = y - 2;
        gb.width  += 4;
        gb.height += 4;


        g.setColor(mouseOverColor);
        g.fillRect(gb.x, gb.y, gb.width, gb.height);
        g.setColor(Color.black);
        g.drawRect(gb.x, gb.y, gb.width, gb.height);
        glyph.paint(g, this);
        return new Point(x + gb.width, y + gb.height);
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
            graphApplet.debug("Image error:" + graphApplet.getImagePath(img));
            return false;
        }

        if ((flags & ImageObserver.ALLBITS) != 0) {
            repaint();
            return false;
        }
        return true;
    }





    /**
     *  Utility to print a string
     *
     * @param s _more_
     */
    public void print(String s) {
        System.err.println(s);
    }





    /**
     *  Utility method for creating a label
     *
     * @param l _more_
     *
     * @return _more_
     */
    public Label getLabel(String l) {
        Label label = new Label(l);
        label.setFont(labelFont);
        return label;
    }


    /**
     *  Get an appropriate foreground color for the given bg color
     *
     * @param bg _more_
     *
     * @return _more_
     */
    public Color getFGColor(Color bg) {
        if (bg.equals(Color.black)) {
            return Color.white;
        }
        if (bg.equals(Color.blue)) {
            return Color.white;
        }
        return Color.black;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean showImages() {
        return true;
    }

    /**
     *  Utility method for creating a checkbox
     *
     * @param l _more_
     * @param dflt _more_
     * @param listener _more_
     *
     * @return _more_
     */
    public JCheckBox getCheckbox(String l, boolean dflt,
                                 ItemListener listener) {
        JCheckBox w = new JCheckBox(l, dflt);
        if (listener != null) {
            w.addItemListener(listener);
        }
        w.setFont(widgetFont);
        return w;
    }




}

