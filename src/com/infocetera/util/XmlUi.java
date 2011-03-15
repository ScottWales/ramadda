/*
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

import java.applet.Applet;

import java.awt.*;
import java.awt.event.*;

import java.lang.reflect.*;

import java.net.URL;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;


import javax.swing.*;
import javax.swing.text.*;


/**
 *  Can create a user interface from a "skin" xml. A good example
 *  of the xml this processes is in com/infocetera/chat/chatskin.xml
 *
 * The xml looks like:
 * <skin>
 *   <ui>
 *       Some ui xml
 *   </ui>
 *   <components>
 *        Multiple  ui xml
 *   </components>
 * </skin>
 *
 * The "ui xml", either under the ui tag or under the components tag,
 * contains a set of nested container and component tags:
 * <panel id="someid" layout="LAYOUT_VALUES">
 *      containers and components
 * </panel>
 *
 * Each top-level tag under the components tag has an id attribute. One can then
 * refer to this component in the ui tag with a
 * "<component idref="the component id">
 * This allows one to separate overall layout (defined in the ui) from that actual
 * components. e.g.:
 *
 * <skin>
 * <ui>
 * ...
 *  <component idref="id1"/>
 * ...
 *     <component idref="id2"/>
 * ...
 * </ui>
 * <components>
 *  <button id="id1"/>
 *  <panel id="id2"> .... </panel>
 * </components>
 *
 *
 * Note: any attributes defined in the component tag in the ui section
 * will overwrite the attributes in the actual tag in the components section.
 *
 * TAGS:
 * component
 * panel
 * button
 * checkbox
 * textinput
 * menu
 * image
 *
 *
 * All tags can have these attributes:
 * bgcolor, fgcolor - background and foreground color. The value can be a color
 * name, e.g.: red, blue, orange, white, etc. or a single numeric value
 * or a comma separated list of rgb values: e.g.: "250,50,10"
 *
 * fontsize - specify font size used.
 *
 * fontface - specify font face used.
 *
 *
 * Tag: component
 * The component tag can either have an idref, which points to
 * a component defined in the components section:
 *
 * <component idref="some id in the components section"
 *          (and any attributes)/>
 *
 * Or it can have an id which should be held within
 * the idToComponent Hashtable which the XmlUi is created with.
 * This allows the use of any application specific Component-s
 *
 * <component id="some id in idToComponent Hashtable"
 *          (and any attributes)/>
 *
 * Tag: panel
 * <panel layout="border|card|grid|gridbag|inset"
 *      hspace="int, hor. spacing "
 *      vspace="int, vert. spacing "
 *      rows="int"
 *      cols="int"
 *      colwidths="int,int,...,int"
 *      rowheights="int,int,...,int">
 *
 *
 * The panel tags can have any number of children tags.
 * The layout of the children is defined with a "layout" attribute
 * which can be one of: border, card, grid, gridbag, inset.
 *
 * layout="border"  - java.awt.BorderLayout. The children components of this tag should have a
 * "place" attribute which is one of the java.awt.BorderLayout places:
 * North, South, East, West, Center. e.g.:
 * <panel layout="border" >
 *  <label id="top" place="North" />
 *  <label id="bottom" place="South"/>
 *  <label id="left" place="West"/>
 *  ...
 * </panel>
 *
 *
 *
 * layout="card"  - This is a card layout that can take any number of children components
 * but only shows one at a time. You can "flip" through the children components
 * with an action called ui.flip. For example you can have a panel:
 * <panel layout="card" id="testpanel">
 *  <label id="child1"/>
 *  <label id="child2"/>
 *  <label id="child3"/>
 * </panel>
 *
 * Now if some component has an action ui.flip:
 * <button action="ui.flip(testpanel);" />
 * this will hide the current child (e.g., child1) and show the next child.
 * The action:
 * <button action="ui.flip(testpanel,child3);" />
 * Will show a specific child.
 *
 * layout="grid"  This is the java.awt.GridLayout. You can specify a number of rows and/or
 * columns:
 * <panel layout="grid" rows="2"  cols="3">
 * Will give 2 rows 1 columns.
 *
 * <panel layout="grid" rows="1">
 * Will give a single row multiple column layout.
 * The spacing used is  defined with: hspace=".." vspace="..." attributes.
 *
 * layout="gridbag"  This uses the java.awt.GridBagLayout in a column oriented way.
 * The spacing used is  defined with: hspace=".." vspace="..." attributes.
 * You can specify the number of columns in the grid. You can also specify
 * the column and row weights (As a comm separated string of numeric values)
 * that determine stretchiness. e.g.:
 * <panel layout="gridbag" cols="5" place="South" colwidths="0,0,0,1,0" >
 * Will give 5 columns the first three have no stretch, the 4th does, the 5th does
 * not.
 * <panel layout="gridbag" cols="2" place="South" colwidths="1,0"  rowheight="0,0"
 * >
 * Will give 2 columns by any number of rows. The first column has stretch in
 * width while none of the rows stretch in height.
 *
 *
 * layout="inset" - This is a simple way to wrap a single child component.
 * The spacing used is  defined with: hspace=".." vspace="..." attributes.
 *
 *
 * Tag: button
 * <button  action="some action"  label="label to use"/>
 * Creates a java.awt.Button. The action (like all actions) can be a semicolon
 * (";") separted list of actions.
 *
 *
 * Tag: checkbox
 * <checkbox  action="some action"  label="label to use"/>
 * Just like the button tag. However, we convert the itemStateChanged event
 * into an action event and pass it on to the actionListener.
 *
 *
 * Tag: textinput
 * <textinput rows="optional number of rows"
 *          cols="optional number of columns"
 *          value="initial text value"
 *          action="some action"/>
 * Provides either a TextField or a TextArea depending on the number
 * of rows (the default == 1, which gives a TextField).
 * For TextField-s we add an actionListener if the action attribute is defined.
 *
 *
 * Tag: menu
 * <menu label="Some menu label" image="some image url">
 *    <menuitem label="some menu item"  action="some action" />
 *    <menuitem label="some other menu item"  action="some other action" />
 *    <separator/>
 *    <menu label="some sub menu">
 *        <menuitem label="..."  action="..." />
 *        <menuitem label="..."  action="..." />
 *    </menu>
 * </menu>
 *
 * If image attribute  is defined creates an image button, else creates a
 * text button.
 * When the  button is clicked a menu of menuitems, separators and sub-menus is
 * popped up.
 *
 *
 * Tag: image
 * <image url="some url"
 *      width="Optional width of image"
 *      height="Optional height of image"
 *      action="If defined the image acts like a button"
 *      border="Optional true|false">
 * This provides a simple image label or an image button (if action is defined).
 * If it is a button and if border==true then the image is drawn with a border
 * (that changes when clicked).
 *
 */


public class XmlUi implements ActionListener, ItemListener, KeyListener {

    /** _more_ */
    IfcApplet applet;

    /** _more_ */
    Container myContents;

    /** _more_ */
    private XmlNode root;

    /** _more_ */
    private ActionListener actionListener;
    //  private ItemListener itemListener;

    /** _more_ */
    private Component keyComponent;


    /**
     *  Create the XmlUi processor. Root should point to a "skin"
     *  node. idToComponent is a Hashtable with application specific collection
     *  of id->java.awt.Component pairs.
     *  actionListener - > route actions to it.
     *
     * @param applet _more_
     * @param root _more_
     * @param idToComponent _more_
     * @param actionListener _more_
     */

    public XmlUi(IfcApplet applet, XmlNode root, Hashtable idToComponent,
                 ActionListener actionListener) {
        this(applet, root, idToComponent, actionListener, null);
    }



    /**
     * _more_
     *
     * @param applet _more_
     * @param root _more_
     * @param idToComponent _more_
     * @param actionListener _more_
     * @param keyComponent _more_
     */
    public XmlUi(IfcApplet applet, XmlNode root, Hashtable idToComponent,
                 ActionListener actionListener, Component keyComponent) {
        this.applet         = applet;
        this.root           = root;
        this.idToComponent  = idToComponent;
        this.actionListener = actionListener;
        this.keyComponent   = keyComponent;
        if (keyComponent != null) {
            keyComponent.addKeyListener(this);
        }
    }

    /** xml attribute name */
    public static final String ATTR_ORIENTATION = "orientation";

    public static final String ATTR_BORDER = "border";

    /** xml attribute name */
    public static final String ATTR_DIVIDER = "divider";

    /** xml attribute name */
    public static final String ATTR_RESIZEWEIGHT = "resizeweight";

    /** xml attribute name */
    public static final String ATTR_CONTINUOUS = "continuous";

    /** xml attribute name */
    public static final String ATTR_ONETOUCHEXPANDABLE = "onetouchexpandable";

    /** split pane orientation */
    public static final String[] SPLITPANE_NAMES = { "h", "v" };

    /** corresponding split pane values */
    public static final int[] SPLITPANE_VALUES = { JSplitPane
                                                     .HORIZONTAL_SPLIT,
            JSplitPane.VERTICAL_SPLIT };


    /** _more_ */
    public static final String ACTION_WRITE = "ui.write";

    /** _more_ */
    public static final String ACTION_UI_FLIP = "ui.flip";

    /** _more_ */
    public static final String ACTION_MENUPOPUP = "popup";

    /**
     *  Tag and attribute names for the skin xml
     */
    public static final String TAG_SPLITPANE = "splitpane";

    /** _more_ */
    public static final String TAG_SKIN = "skin";

    /** _more_ */
    public static final String TAG_UI = "ui";

    /** _more_ */
    public static final String TAG_COMPONENTS = "components";

    /** _more_ */
    public static final String TAG_PANEL = "panel";

    /** _more_ */
    public static final String TAG_BORDER = "border";

    /** _more_ */
    public static final String TAG_BUTTON = "button";

    /** _more_ */
    public static final String TAG_CHECKBOX = "checkbox";

    /** _more_ */
    public static final String TAG_MENU = "menu";

    /** _more_ */
    public static final String TAG_MENUBAR = "menubar";

    /** _more_ */
    public static final String TAG_MENUITEM = "menuitem";

    /** _more_ */
    public static final String TAG_CBMENUITEM = "cbmenuitem";

    /** _more_ */
    public static final String TAG_SHAPEPANEL = "shapepanel";

    /** _more_ */
    public static final String TAG_SEPARATOR = "separator";

    /** _more_ */
    public static final String TAG_TEXTINPUT = "textinput";

    /** _more_ */
    public static final String TAG_LABEL = "label";

    /** _more_ */
    public static final String TAG_IMAGE = "image";

    /** _more_ */
    public static final String TAG_CHOICE = "choice";

    /** _more_ */
    public static final String TAG_ITEM = "item";


    /** _more_ */
    public static final String ATTR_KEY = "key";

    /** _more_ */
    public static final String ATTR_TYPE = "type";

    /** _more_ */
    public static final String ATTR_MARGIN = "margin";

    /** _more_ */
    public static final String ATTR_MARGINTOP = "margin-top";

    /** _more_ */
    public static final String ATTR_MARGINLEFT = "margin-left";

    /** _more_ */
    public static final String ATTR_MARGINBOTTOM = "margin-bottom";

    /** _more_ */
    public static final String ATTR_MARGINRIGHT = "margin-right";

    /** _more_ */
    public static final String ATTR_MOUSEENTER = "mouseenter";

    /** _more_ */
    public static final String ATTR_MOUSEEXIT = "mouseexit";

    /** _more_ */
    public static final String ATTR_ACTION = "action";

    /** _more_ */
    public static final String ATTR_ACTIONTEMPLATE = "actiontemplate";

    /** _more_ */
    public static final String ATTR_EVENT = "event";

    /** _more_ */
    public static final String ATTR_ALIGN = "align";



    /** _more_ */
    public static final String ATTR_IMAGE = "image";

    /** _more_ */
    public static final String ATTR_OVERIMAGE = "overimage";

    /** _more_ */
    public static final String ATTR_DOWNIMAGE = "downimage";

    /** _more_ */
    public static final String ATTR_BGCOLOR = "bgcolor";

    /** _more_ */
    public static final String ATTR_FGCOLOR = "fgcolor";

    /** _more_ */
    public static final String ATTR_FONTSIZE = "fontsize";

    /** _more_ */
    public static final String ATTR_FONTFACE = "fontface";

    /** _more_ */
    public static final String ATTR_FONTSTYLE = "fontstyle";

    /** _more_ */
    public static final String ATTR_URL = "url";

    /** _more_ */
    public static final String ATTR_ID = "id";

    /** _more_ */
    public static final String ATTR_IDREF = "idref";

    /** _more_ */
    public static final String ATTR_GROUP = "group";

    /** _more_ */
    public static final String ATTR_LABEL = "label";

    /** _more_ */
    public static final String ATTR_LAYOUT = "layout";

    /** _more_ */
    public static final String ATTR_PLACE = "place";

    /** _more_ */
    public static final String ATTR_ROWS = "rows";

    /** _more_ */
    public static final String ATTR_COLS = "cols";

    /** _more_ */
    public static final String ATTR_SELECTED = "selected";

    /** _more_ */
    public static final String ATTR_WIDTH = "width";

    /** _more_ */
    public static final String ATTR_HEIGHT = "height";

    /** _more_ */
    public static final String ATTR_VALUE = "value";

    /** _more_ */
    public static final String ATTR_HSPACE = "hspace";

    /** _more_ */
    public static final String ATTR_VSPACE = "vspace";

    /** _more_ */
    public static final String ATTR_ROWHEIGHTS = "rowheights";

    /** _more_ */
    public static final String ATTR_COLWIDTHS = "colwidths";

    /** _more_ */
    public static final String ATTR_DEFAULT = "default";


    /** _more_ */
    public static final String VALUE_LAYOUTBORDER = "border";

    public static final String VALUE_LAYOUTTAB = "tab";

    /** _more_ */
    public static final String VALUE_LAYOUTCARD = "card";

    /** _more_ */
    public static final String VALUE_LAYOUTGRID = "grid";

    /** _more_ */
    public static final String VALUE_LAYOUTGRIDBAG = "gridbag";

    /** _more_ */
    public static final String VALUE_LAYOUTINSET = "inset";

    /** _more_ */
    public static final String VALUE_LAYOUTWRAP = "wrap";

    /** _more_ */
    public static final String VALUE_LAYOUTFLOW = "flow";


    /**
     *  Return the java.awt.Label align value that corresponds to
     *  the "align" attribute in the given XmlNode
     *
     * @param node _more_
     *
     * @return _more_
     */
    int getAlign(XmlNode node) {
        return getAlign(node.getAttribute(ATTR_ALIGN, "CENTER"));
    }

    /**
     * _more_
     *
     * @param align _more_
     *
     * @return _more_
     */
    public static int getAlign(String align) {
        if (align != null) {
            align = align.toLowerCase();
            if (align.equals("center")) {
                return JLabel.CENTER;
            }
            if (align.equals("right")) {
                return JLabel.RIGHT;
            }
        }
        return JLabel.LEFT;
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void keyPressed(KeyEvent e) {
        String key = "" + e.getKeyText(e.getKeyCode());
        if (e.isShiftDown()) {
            key = "Shift+" + key;
        }
        if (e.isControlDown()) {
            key = "Ctrl+" + key;
        }

        Object comp = keyToComponent.get(key.toLowerCase());
        //    System.err.println (key +" comp:" + comp);
        if (comp == null) {
            return;
        }

        if (comp instanceof JCheckBoxMenuItem) {
            JCheckBoxMenuItem cbmi = (JCheckBoxMenuItem) comp;
            cbmi.setState( !cbmi.getState());
            itemStateChanged(new ItemEvent(cbmi, 0, cbmi,
                                           ItemEvent.ITEM_STATE_CHANGED));
        } else if (comp instanceof JMenuItem) {
            actionPerformed(
                new ActionEvent(
                    comp, 0, ((JMenuItem) comp).getActionCommand()));
        } else if (comp instanceof ImageButton) {
            actionPerformed(
                new ActionEvent(comp, 0, ((ImageButton) comp).getAction()));
        } else if (comp instanceof String) {
            String group = (String) comp;
            Vector v     = (Vector) groupIdToList.get(group);
            if ((v == null) || (v.size() == 0)) {
                return;
            }
            int onIdx = 0;
            for (int i = 0; i < v.size(); i++) {
                ItemSelectable s = (ItemSelectable) v.elementAt(i);
                if (getState(s)) {
                    onIdx = i;
                    break;
                }
            }
            onIdx++;
            if (onIdx >= v.size()) {
                onIdx = 0;
            }
            ItemSelectable s = (ItemSelectable) v.elementAt(onIdx);
            setState(s, true);
            itemStateChanged(new ItemEvent(s, 0, s,
                                           ItemEvent.ITEM_STATE_CHANGED));
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



    /**
     *  Set bgcolor, fgcolor and font attributes, defined in the XmlNode,
     *  on the given component.
     *
     * @param comp _more_
     * @param node _more_
     */
    void setAttrs(final Component comp, XmlNode node) {
        final String mouseEnter = node.getAttribute(ATTR_MOUSEENTER,
                                      (String) null);
        final String mouseExit = node.getAttribute(ATTR_MOUSEEXIT,
                                     (String) null);
        if ((mouseEnter != null) || (mouseExit != null)) {
            comp.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (mouseEnter != null) {
                        processActions(mouseEnter, comp);
                    }
                }

                public void mouseExited(MouseEvent e) {
                    if (mouseExit != null) {
                        processActions(mouseExit, comp);
                    }
                }
            });
        }



        Color bgColor = node.getAttribute(ATTR_BGCOLOR, (Color) null);
        if (bgColor != null) {
            comp.setBackground(bgColor);
        } else {
            //            comp.setBackground(Color.red);
        }
        Color fgColor = node.getAttribute(ATTR_FGCOLOR, (Color) null);
        if (fgColor != null) {
            comp.setForeground(fgColor);
        }


        int    fontsize  = node.getAttributeFromTree(ATTR_FONTSIZE, -1);
        String fontface  = node.getAttributeFromTree(ATTR_FONTFACE);
        String fontStyle = node.getAttributeFromTree(ATTR_FONTSTYLE);

        if ((fontsize > 0) || (fontface != null) || (fontStyle != null)) {
            int style = Font.PLAIN;
            if (fontStyle != null) {
                if (fontStyle.equals("bold")) {
                    style = Font.BOLD;
                } else if (fontStyle.equals("italics")) {
                    style = Font.ITALIC;
                } else if (fontStyle.equals("none")) {
                    style = Font.PLAIN;
                }
            }
            if (fontsize <= 0) {
                fontsize = 12;
            }
            if (fontface == null) {
                fontface = "Dialog";
            }
            Font f = new Font(fontface, style, fontsize);
            comp.setFont(f);
        }

        int    margin = node.getAttribute(ATTR_MARGIN, 1);
        int    top    = node.getAttribute(ATTR_MARGINTOP, margin);
        int    left   = node.getAttribute(ATTR_MARGINLEFT, margin);
        int    bottom = node.getAttribute(ATTR_MARGINBOTTOM, margin);
        int    right  = node.getAttribute(ATTR_MARGINRIGHT, margin);
        Color color = node.getAttribute("color", Color.black);
        String border =  node.getAttribute(ATTR_BORDER, "");


        if(comp instanceof JComponent) {
            JComponent jcomp = (JComponent) comp;
            if(border.equals("matte")) {
                jcomp.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, color));
            }
        }
    }


    /**
     *  This is an identifier user to give unique ids for skin components
     *  that do not have an id attribute.
     */
    private int xmlNodeId = 0;

    /**
     *  Maps id->XmlNode
     */
    private Hashtable idToXmlNode = new Hashtable();

    /**
     *  Skin id to created component
     */
    Hashtable idToComponent = new Hashtable();


    /** _more_ */
    private Hashtable keyToComponent = new Hashtable();

    /**
     *  Skin id to created menu item
     */
    Hashtable idToMenuItem = new Hashtable();

    /** _more_ */
    Hashtable idToListOfMenuItems = new Hashtable();




    /**
     *  Panel component to the list of XmlNode that define its children components.
     */
    private Hashtable containerToNodeList = new Hashtable();

    /**
     *  java.awt.Component to XmlNode
     */
    private Hashtable compToNode = new Hashtable();

    /**
     *  XmlNode to awt.Component
     */
    private Hashtable nodeToComponent = new Hashtable();

    /**
     *  Map the awt Component to its parent. We keep this here
     *  because some of these components get removed from their parent.
     */
    private Hashtable componentToParent = new Hashtable();

    /**
     *  Maps the awt Component to an action command String
     *  defined in the skin xml. Mostly used for text fields
     */
    private Hashtable componentToAction = new Hashtable();

    /** _more_ */
    private Hashtable groupIdToList = new Hashtable();

    /** _more_ */
    private Hashtable componentToGroupList = new Hashtable();

    /** _more_ */
    private boolean makingUi = false;

    /**
     * _more_
     *
     * @return _more_
     */
    public Container getContents() {
        if (myContents == null) {
            makingUi   = true;
            myContents = doMakeContents();
            makingUi   = false;
            //      myContents.invalidate();
        }
        return myContents;
    }


    /**
     * _more_
     *
     * @param node _more_
     *
     * @return _more_
     */
    public static XmlNode wrap(XmlNode node) {
        XmlNode root = new XmlNode(TAG_PANEL, new Hashtable());
        root.appendChild(node);
        return root;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    private Container doMakeContents() {
        if (root.getTag().equals(TAG_PANEL)) {
            return GuiUtils.inset(xmlToUi(root), 0, 0);
        }
        XmlNode uiNode         = root.getChild(TAG_UI);
        XmlNode componentsNode = root.getChild(TAG_COMPONENTS);
        if (componentsNode != null) {
            for (int j = 0; j < componentsNode.size(); j++) {
                XmlNode component = componentsNode.get(j);
                String  id = component.getAttribute(ATTR_ID, (String) null);
                if (id != null) {
                    if (component.getTag().equals(TAG_MENU)) {
                        Vector items = new Vector();
                        idToListOfMenuItems.put(id, items);
                        processMenu(
                            null, items, component,
                            component.getAttribute(ATTR_ACTIONTEMPLATE));
                    }
                    idToXmlNode.put(id, component);
                }
            }
        }

        if (uiNode == null) {
            System.err.println("No ui tag found in:" + root);
            return GuiUtils.inset(new JLabel("Error: No <ui> tag found"), 4,
                                  4);
        }
        if (uiNode.size() != 1) {
            return GuiUtils.inset(
                new JLabel("Error: <ui> tag must have only one child"), 4, 4);
        }
        return GuiUtils.inset(xmlToUi(uiNode.get(0)), 0, 0);
    }

    /**
     * _more_
     *
     * @param node _more_
     *
     * @return _more_
     */
    private XmlNode getReffedNode(XmlNode node) {
        XmlNode reffedNode = getReffedNode(node.getAttribute(ATTR_IDREF,
                                 (String) null));
        if (reffedNode == null) {
            return node;
        }
        for (Enumeration attrs = node.getAttributes();
                attrs.hasMoreElements(); ) {
            String attr = attrs.nextElement().toString();
            if ( !attr.equals(ATTR_IDREF)) {
                reffedNode.addAttribute(attr, node.getAttribute(attr));
            }
        }
        return reffedNode;
    }

    /**
     * _more_
     *
     * @param idRef _more_
     *
     * @return _more_
     */
    public XmlNode getReffedNode(String idRef) {
        if (idRef == null) {
            return null;
        }

        XmlNode reffedNode = (XmlNode) idToXmlNode.get(idRef);
        if (reffedNode == null) {
            return null;
        }

        reffedNode = reffedNode.copy();
        return reffedNode;
    }




    /**
     * _more_
     *
     * @param node _more_
     *
     * @return _more_
     */
    private Component xmlToUi(XmlNode node) {


        String id = node.getAttribute(ATTR_ID, (String) null);
        if (id == null) {
            id = "nodeid" + (xmlNodeId++);
            node.addAttribute(ATTR_ID, id);
        }

        Component comp = (Component) nodeToComponent.get(node);
        if (comp != null) {
            return comp;
        }
        if (id != null) {
            comp = (Component) idToComponent.get(id);
        }
        if (comp == null) {
            comp = createComponent(node, id);
            if (comp != null) {
                compToNode.put(comp, node);
                nodeToComponent.put(node, comp);
                if (id != null) {
                    idToComponent.put(id, comp);
                }
            }
        }
        if (comp != null) {
            setAttrs(comp, node);
        }
        return comp;

    }



    /**
     * _more_
     *
     * @param node _more_
     * @param children _more_
     *
     * @return _more_
     */
   private Container layoutBorder(XmlNode node, Vector children) {
        if (children.size() == 0) {
            return new JPanel();
        }
        XmlNode childXmlNode = getReffedNode((XmlNode) children.elementAt(0));
        Component childComponent = xmlToUi(childXmlNode);
        if (childComponent == null) {
            return new JPanel();
        }
        int    margin = node.getAttribute(ATTR_MARGIN, 2);
        int    top    = node.getAttribute(ATTR_MARGINTOP, margin);
        int    left   = node.getAttribute(ATTR_MARGINLEFT, margin);
        int    bottom = node.getAttribute(ATTR_MARGINBOTTOM, margin);
        int    right  = node.getAttribute(ATTR_MARGINRIGHT, margin);
        Insets insets = new Insets(top, left, bottom, right);
        int borderType =
            GuiUtils.getIndex(GuiUtils.BORDERS, null,
                              node.getAttribute(ATTR_TYPE, "none"),
                              GuiUtils.BORDER_EMPTY);
        Color dfltColor = node.getAttribute("color", (Color) null);
        Color[] colors = { node.getAttribute("color-top", dfltColor),
                           node.getAttribute("color-left", dfltColor),
                           node.getAttribute("color-bottom", dfltColor),
                           node.getAttribute("color-right", dfltColor) };

        JPanel panel = new BorderPanel(borderType, insets, colors,
                                       node.getAttribute("filter"));
        GuiUtils.tmpInsets = insets;

        GuiUtils.doLayout(panel, new Component[] { childComponent }, 1,
                          GuiUtils.DS_Y, GuiUtils.DS_Y);
        return panel;
    }


    /**
     *  Layout (or relayout) the children of the given Container.
     *
     * @param panel _more_
     * @param node _more_
     * @param xmlChildren _more_
     *
     * @return _more_
     */
    private Container layoutContainer(Container panel, XmlNode node,
                                      Vector xmlChildren) {
        panel.removeAll();
        String panelId     = node.getAttribute(ATTR_ID);
        String layout      = node.getAttribute(ATTR_LAYOUT, "");
        Vector children    = new Vector();
        Vector nodes       = new Vector();
        int    hspace      = node.getAttribute(ATTR_HSPACE, 0);
        int    vspace      = node.getAttribute(ATTR_VSPACE, 0);
        int    rows        = node.getAttribute(ATTR_ROWS, 0);
        int    cols        = node.getAttribute(ATTR_COLS, 1);
        String defaultComp = node.getAttribute(ATTR_DEFAULT, "nocomp");
        JTabbedPane tabs=null;

        if (layout.equals(VALUE_LAYOUTBORDER)) {
            panel.setLayout(new BorderLayout());
        } else if (layout.equals(VALUE_LAYOUTCARD)) {
            panel.setLayout(new CardLayout());
        } else if (layout.equals(VALUE_LAYOUTTAB)) {
            tabs = new JTabbedPane();
            panel.setLayout(new BorderLayout());
            panel.add("Center", tabs);
        } else if (layout.equals(VALUE_LAYOUTFLOW)) {
            panel.setLayout(new FlowLayout(FlowLayout.LEFT, hspace, vspace));
        } else if (layout.equals(VALUE_LAYOUTGRID)) {
            panel.setLayout(new GridLayout(rows, cols, hspace, vspace));
        }
        for (int i = 0; i < xmlChildren.size(); i++) {
            XmlNode childXmlNode =
                getReffedNode((XmlNode) xmlChildren.elementAt(i));
            Component childComponent = xmlToUi(childXmlNode);
            if (childComponent == null) {
                continue;
            }
            componentToParent.put(childComponent, panel);
            children.addElement(childComponent);
            nodes.addElement(childXmlNode);
            if ( !childComponent.isVisible()) {
                continue;
            }
            if (layout.equals(VALUE_LAYOUTBORDER)) {
                String place = childXmlNode.getAttribute(ATTR_PLACE,
                                   "Center");   
                panel.add(place, childComponent);
            } else if (layout.equals(VALUE_LAYOUTTAB)) {
                tabs.add(childXmlNode.getAttribute(ATTR_LABEL), childComponent);
            } else if (layout.equals(VALUE_LAYOUTCARD)) {
                String childId = childXmlNode.getAttribute(ATTR_ID);
                panel.add(childId, childComponent);
                if (defaultComp.equals(childId)) {
                    ((CardLayout) panel.getLayout()).show(panel, childId);
                }
            } else if (layout.equals(VALUE_LAYOUTINSET)) {
                GuiUtils.tmpInsets = new Insets(vspace, hspace, vspace,
                        hspace);
                GuiUtils.doLayout(panel, new Component[] { childComponent },
                                  1, GuiUtils.DS_Y, GuiUtils.DS_Y);
                break;
            } else if (layout.equals(VALUE_LAYOUTWRAP)) {
                GuiUtils.tmpInsets = new Insets(vspace, hspace, vspace,
                        hspace);
                GuiUtils.doLayout(panel, new Component[] { childComponent },
                                  1, GuiUtils.DS_N, GuiUtils.DS_N);
                break;
            } else if ( !layout.equals(VALUE_LAYOUTGRIDBAG)) {
                panel.add(childComponent);
            }
        }
        if (layout.equals(VALUE_LAYOUTGRIDBAG)) {
            double[] cw =
                GuiUtils.parseDoubles(node.getAttribute(ATTR_COLWIDTHS));
            double[] rh =
                GuiUtils.parseDoubles(node.getAttribute(ATTR_ROWHEIGHTS));
            if (cw == null) {
                cw = GuiUtils.DS_Y;
            }
            if (rh == null) {
                rh = GuiUtils.DS_N;
            }
            GuiUtils.tmpInsets = new Insets(vspace, hspace, vspace, hspace);
            GuiUtils.doLayout(panel, GuiUtils.getCompArray(children),
                              node.getAttribute(ATTR_COLS, 1), cw, rh);
        }

        containerToNodeList.put(panel, nodes);
        return panel;
    }


    /**
     *  Create an image label or button from the given XmlNode
     *
     * @param node _more_
     * @param imagePath _more_
     * @param action _more_
     *
     * @return _more_
     */
    private ImageButton makeImageButton(XmlNode node, String imagePath,
                                        String action) {
        if (imagePath == null) {
            imagePath = node.getAttribute(ATTR_IMAGE, "no path");
        }
        try {
            Image image = IfcApplet.getImage(imagePath);
            Image overImage =
                IfcApplet.getImage(node.getAttribute(ATTR_OVERIMAGE,
                    (String) null));
            Image downImage =
                IfcApplet.getImage(node.getAttribute(ATTR_DOWNIMAGE,
                    (String) null));
            int     w      = node.getAttribute(ATTR_WIDTH, -1);
            int     h      = node.getAttribute(ATTR_HEIGHT, -1);
            boolean border = node.getAttribute(ATTR_BORDER, false);
            boolean onPress = node.getAttribute(ATTR_EVENT,
                                  "onclick").equals("onpress");
            Dimension dim = null;

            if ((w > 0) && (h > 0)) {
                dim = new Dimension(w, h);
            }
            ImageButton tb = new ImageButton(image, overImage, downImage,
                                             dim, border, onPress);
            String key = node.getAttribute(ATTR_KEY);
            if (key != null) {
                keyToComponent.put(key.toLowerCase(), tb);
            }

            if (action == null) {
                action = node.getAttribute(ATTR_ACTION, (String) null);
            }
            if (action != null) {
                tb.setActionListener(this);
                tb.setAction(action);
            }
            return tb;
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return null;
    }

    /**
     *  Create the awt Component defined by the given skin node.
     *
     * @param node _more_
     * @param id _more_
     *
     * @return _more_
     */
    private Component createComponent(XmlNode node, String id) {

        Component comp = null;
        String    tag  = node.getTag();

        if (tag.equals(TAG_PANEL)) {
            comp = layoutContainer(new JPanel(), node, node.getChildren());
        } else if (tag.equals(TAG_BORDER)) {
            comp = layoutBorder(node, node.getChildren());
        } else if (tag.equals(TAG_TEXTINPUT)) {
            int            cols  = node.getAttribute(ATTR_COLS, -1);
            int            rows  = node.getAttribute(ATTR_ROWS, -1);
            String         value = node.getAttribute(ATTR_VALUE, "");

            JTextComponent textComp;
            if (rows > 1) {
                if (cols < 0) {
                    cols = 30;
                }
                textComp = new JTextArea(value, rows, cols);
            } else {
                if (cols == -1) {
                    textComp = new JTextField(value);
                } else {
                    textComp = new JTextField(value, cols);
                }
                ((JTextField) textComp).addActionListener(this);
            }
            comp = textComp;
            String action = node.getAttribute(ATTR_ACTION, (String) null);
            if (action != null) {
                componentToAction.put(textComp, action);
            }
        } else if (tag.equals(TAG_MENUITEM) || tag.equals(TAG_CBMENUITEM)) {
            String    actionTemplate = null;
            JMenuItem mi;
            String    label  = node.getAttribute(ATTR_LABEL, "");
            String    action = node.getAttribute(ATTR_ACTION);
            String    value  = node.getAttribute(ATTR_VALUE);
            String    key    = node.getAttribute(ATTR_KEY);
            if ((action == null) && (actionTemplate != null)
                    && (value != null)) {
                action = GuiUtils.replace(actionTemplate, "%value%", value);
            }
            if ((key != null) && !key.startsWith("group:")) {
                label = label + "     " + key;
            }

            if (node.getTag().equals(TAG_CBMENUITEM)) {
                boolean initValue = node.getAttribute(ATTR_VALUE, true);
                JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(label,
                                             initValue);
                String group = node.getAttribute(ATTR_GROUP, (String) null);
                addToGroup(group, cbmi);
                mi = cbmi;
                if (action != null) {
                    Hashtable actions = new Hashtable();
                    actions.put(ATTR_ACTION, action);
                    actions.put(ATTR_VALUE, new Boolean(initValue));
                    componentToAction.put(cbmi, actions);
                    cbmi.addItemListener(this);
                    if ((group == null) || initValue) {
                        itemStateChanged(new ItemEvent(cbmi, 0, cbmi,
                                ItemEvent.ITEM_STATE_CHANGED));
                    }
                }

                if (key != null) {
                    if (key.startsWith("group:")) {
                        if (group != null) {
                            key = key.substring(6);
                            keyToComponent.put(key.toLowerCase(), group);
                        }
                    } else {
                        keyToComponent.put(key.toLowerCase(), mi);
                    }
                }
            } else {
                mi = new JMenuItem(label);
                if (action != null) {
                    mi.setActionCommand(action);
                    mi.addActionListener(this);
                    if (key != null) {
                        keyToComponent.put(key.toLowerCase(), mi);
                    }
                }
            }
            if (id != null) {
                idToMenuItem.put(id, mi);
            }
            comp = mi;
        } else if (tag.equals(TAG_MENU)) {
            Vector children = node.getChildren();
            JMenu  menu     = new JMenu(node.getAttribute(ATTR_LABEL, ""));
            comp = menu;
            for (int i = 0; i < children.size(); i++) {
                XmlNode childElement = (XmlNode) children.get(i);
                if (childElement.getTag().equals(TAG_SEPARATOR)) {
                    menu.addSeparator();
                    continue;
                }
                Component childComponent = xmlToUi(childElement);
                if (childComponent == null) {
                    continue;
                }
                menu.add(childComponent);
            }
        } else if (tag.equals(TAG_MENUBAR)) {
            Vector   children = node.getChildren();
            JMenuBar menuBar  = new JMenuBar();
            comp = menuBar;
            for (int i = 0; i < children.size(); i++) {
                XmlNode   childElement   = (XmlNode) children.get(i);
                Component childComponent = xmlToUi(childElement);
                if (childComponent == null) {
                    continue;
                }
                menuBar.add(childComponent);
            }
        } else if (tag.equals(TAG_SPLITPANE)) {
            Vector xmlChildren = node.getChildren();
            if (xmlChildren.size() != 2) {
                throw new IllegalArgumentException(
                    "splitpane tag needs to  have 2 children " + node);
            }
            XmlNode   leftNode = getReffedNode((XmlNode) xmlChildren.get(0));
            XmlNode   rightNode = getReffedNode((XmlNode) xmlChildren.get(1));
            Component leftComponent  = xmlToUi(leftNode);
            Component rightComponent = xmlToUi(rightNode);
            boolean   continuous = node.getAttribute(ATTR_CONTINUOUS, true);
            int orientation = findValue(node.getAttribute(ATTR_ORIENTATION,
                                  (String) null), SPLITPANE_NAMES,
                                      SPLITPANE_VALUES);

            JSplitPane split = new JSplitPane(orientation, continuous,
                                   leftComponent, rightComponent);
            int divider = node.getAttribute(ATTR_DIVIDER, -1);
            if (divider != -1) {
                split.setDividerLocation(divider);
            }
            split.setOneTouchExpandable(
                node.getAttribute(ATTR_ONETOUCHEXPANDABLE, true));
            double resizeweight = node.getAttribute(ATTR_RESIZEWEIGHT, -1.0);
            if (resizeweight != -1.0) {
                split.setResizeWeight(resizeweight);
            }
            comp = split;
        } else if (tag.equals(TAG_LABEL)) {
            String label = node.getAttribute(ATTR_LABEL, "");
            comp = new JLabel(label, getAlign(node));
        } else if (tag.equals(TAG_IMAGE)) {
            comp = makeImageButton(node, null, null);
        } else if (tag.equals(TAG_SHAPEPANEL)) {
            comp = new ShapePanel(this, node);
        } else if (tag.equals(TAG_BUTTON)) {
            JButton b = new JButton(node.getAttribute(ATTR_LABEL, ""));
            b.setActionCommand(node.getAttribute(ATTR_ACTION, "No action"));
            b.addActionListener(this);
            comp = b;
        } else if (tag.equals(TAG_CHOICE)) {
            Choice    b        = new java.awt.Choice();
            String    action   = node.getAttribute(ATTR_ACTION,
                                     (String) null);
            int       selected = node.getAttribute(ATTR_SELECTED, 0);
            Hashtable actions  = new Hashtable();
            for (int i = 0; i < node.size(); i++) {
                XmlNode child = getReffedNode(node.get(i));
                if ( !child.getTag().equals(TAG_ITEM)) {
                    throw new IllegalArgumentException("Bad choice item:"
                            + child);
                }
                b.add(child.getAttribute(ATTR_LABEL, ""));
                String value = child.getAttribute(ATTR_VALUE, (String) null);
                if (value != null) {
                    actions.put(ATTR_VALUE + i, value);
                }
                String subAction = child.getAttribute(ATTR_ACTION,
                                       (String) null);
                if (subAction != null) {
                    actions.put(ATTR_ACTION + i, subAction);
                }
            }
            comp = b;
            if (action != null) {
                actions.put(ATTR_ACTION, action);
                componentToAction.put(b, actions);
                b.select(selected);
                b.addItemListener(this);
                itemStateChanged(new ItemEvent(b, 0, b,
                        ItemEvent.ITEM_STATE_CHANGED));
            }
        } else if (tag.equals(TAG_CHECKBOX)) {
            JCheckBox b = new JCheckBox(node.getAttribute(ATTR_LABEL, ""),
                                        node.getAttribute(ATTR_VALUE, false));
            String action = node.getAttribute(ATTR_ACTION, (String) null);
            comp = b;
            if (action != null) {
                componentToAction.put(comp, action);
                b.addItemListener(this);
                itemStateChanged(new ItemEvent(b, 0, b,
                        ItemEvent.ITEM_STATE_CHANGED));
            }
        } else {
            comp = new JLabel("Unknown tag:" + tag);
            System.err.println("Unknown tag:" + node);
        }
        return comp;

    }

    /**
     * _more_
     *
     * @param v _more_
     * @param names _more_
     * @param values _more_
     *
     * @return _more_
     */
    private int findValue(String v, String[] names, int[] values) {
        return findValue(v, names, values, values[0]);
    }


    /**
     * Find the corresponding int value in the values array
     * at the index of the v value in the names array
     *
     * @param v value
     * @param names value names
     * @param values values
     * @param dflt
     * @return The value or, if none found, the dflt
     */
    private int findValue(String v, String[] names, int[] values, int dflt) {
        if (v == null) {
            return dflt;
        }
        v = v.toLowerCase();
        for (int fidx = 0; fidx < names.length; fidx++) {
            if (v.equals(names[fidx])) {
                return values[fidx];
            }
        }
        return dflt;

    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public JPopupMenu findMenu(String id) {
        return (JPopupMenu) idToListOfMenuItems.get(id);
    }


    /** _more_ */
    boolean inItemStateChanged = false;

    /**
     * _more_
     *
     * @param event _more_
     */
    public void itemStateChanged(ItemEvent event) {
        if (inItemStateChanged) {
            return;
        }
        inItemStateChanged = true;
        itemStateChangedInner(event);
        inItemStateChanged = false;

    }

    /**
     * _more_
     *
     * @param group _more_
     * @param selectable _more_
     */
    private void addToGroup(String group, ItemSelectable selectable) {
        if (group == null) {
            return;
        }
        Vector v = (Vector) groupIdToList.get(group);
        if (v == null) {
            v = new Vector();
            groupIdToList.put(group, v);
        }
        v.addElement(selectable);
        componentToGroupList.put(selectable, v);
    }


    /**
     * _more_
     *
     * @param selectable _more_
     *
     * @return _more_
     */
    private boolean isInGroup(ItemSelectable selectable) {
        return (componentToGroupList.get(selectable) != null);
    }


    /**
     * _more_
     *
     * @param selectable _more_
     *
     * @return _more_
     */
    private boolean getState(ItemSelectable selectable) {
        if (selectable instanceof JCheckBoxMenuItem) {
            return ((JCheckBoxMenuItem) selectable).getState();
        } else if (selectable instanceof Checkbox) {
            return ((Checkbox) selectable).getState();
        }
        return false;
    }

    /**
     * _more_
     *
     * @param s _more_
     * @param value _more_
     */
    private void setState(ItemSelectable s, boolean value) {
        if (s instanceof JCheckBoxMenuItem) {
            ((JCheckBoxMenuItem) s).setState(value);
        } else if (s instanceof Checkbox) {
            ((Checkbox) s).setState(value);
        }

    }

    /**
     * _more_
     *
     * @param selectable _more_
     */
    private void checkGroup(ItemSelectable selectable) {
        if (makingUi) {
            return;
        }

        Vector v = (Vector) componentToGroupList.get(selectable);
        if (v == null) {
            return;
        }
        boolean value = !getState(selectable);
        for (int i = 0; i < v.size(); i++) {
            ItemSelectable s = (ItemSelectable) v.elementAt(i);
            if (s == selectable) {
                continue;
            }
            setState(s, value);
        }
    }


    /**
     * _more_
     *
     * @param event _more_
     */
    private void itemStateChangedInner(ItemEvent event) {
        Object source = event.getSource();
        Object object = componentToAction.get(source);
        if (object == null) {
            return;
        }
        Hashtable extra  = null;
        String    action = null;
        if (object instanceof String) {
            action = (String) object;
        } else if (object instanceof Hashtable) {
            extra  = (Hashtable) object;
            action = (String) extra.get(ATTR_ACTION);
        }


        if (action == null) {
            return;
        }
        ItemSelectable selectable =
            (ItemSelectable) event.getItemSelectable();

        //    System.err.println ("action=" + action);

        if ( !makingUi && isInGroup(selectable) && !getState(selectable)) {
            setState(selectable, true);
            return;
        }


        if (selectable instanceof Checkbox) {
            action = GuiUtils.replace(action, "%value%",
                                      ((Checkbox) selectable).getState()
                                      + "");
        } else if (selectable instanceof JCheckBoxMenuItem) {
            boolean value = ((JCheckBoxMenuItem) selectable).getState();
            action = GuiUtils.replace(action, "%value%", "" + value);
            //            IfcApplet.debug("action: " + action);
            //      System.err.println ("action 2" + action);
            checkGroup(selectable);
        } else if (event.getItemSelectable() instanceof Choice) {
            Choice choice   = (Choice) event.getItemSelectable();
            int    selected = choice.getSelectedIndex();
            String value    = (String) extra.get(ATTR_VALUE + selected);
            if (value == null) {
                value = "" + selected;
            }
            String subAction = (String) extra.get(ATTR_ACTION + selected);
            if (subAction != null) {
                action = subAction;
            }
            if (value != null) {
                action = GuiUtils.replace(action, "%value%", value);
            }
        }

        StringTokenizer tok = new StringTokenizer(action, ";");
        while (tok.hasMoreTokens()) {
            processAction(tok.nextToken(), event.getItemSelectable());
        }
    }

    /**
     * _more_
     *
     * @param event _more_
     */
    public void actionPerformed(ActionEvent event) {
        Object source      = event.getSource();
        String cmd         = event.getActionCommand();
        String otherAction = (String) componentToAction.get(source);
        //    System.err.println ("action:" + cmd + " o:" + otherAction);
        if (otherAction != null) {
            cmd = otherAction;
        }
        if (cmd == null) {
            return;
        }
        processActions(cmd, source);
    }

    /**
     * _more_
     *
     * @param cmd _more_
     * @param source _more_
     */
    public void processActions(String cmd, Object source) {
        StringTokenizer tok = new StringTokenizer(cmd, ";");
        while (tok.hasMoreTokens()) {
            processAction(tok.nextToken(), source);
        }
    }

    /**
     * _more_
     *
     * @param cmd _more_
     *
     * @return _more_
     */
    public Component argToComponent(String cmd) {
        String compId = extractOneArg(cmd);
        if (compId == null) {
            return null;
        }
        Component comp = (Component) getComponentFromId(compId);
        if (comp == null) {
            System.err.println("Unable to find: " + compId);
        }
        return comp;
    }

    /**
     * _more_
     *
     * @param cmd _more_
     *
     * @return _more_
     */
    public static String extractOneArg(String cmd) {
        int idx1 = cmd.indexOf("(");
        int idx2 = cmd.indexOf(")");
        if ((idx1 < 0) || (idx2 < 0)) {
            return null;
        }
        return cmd.substring(idx1 + 1, idx2);
    }

    /**
     * _more_
     *
     * @param cmd _more_
     *
     * @return _more_
     */
    public static String[] extractTwoArgs(String cmd) {
        int idx = cmd.indexOf(",");
        if (idx < 0) {
            return new String[] { null, null };
        }
        return new String[] { cmd.substring(0, idx), cmd.substring(idx + 1) };
    }

    /**
     * _more_
     *
     * @param cmd _more_
     *
     * @return _more_
     */
    public static String[] extractTwoArgsFromCmd(String cmd) {
        int idx1 = cmd.indexOf("(");
        int idx2 = cmd.indexOf(",");
        int idx3 = cmd.indexOf(")");
        if ((idx1 < 0) || (idx2 < 0) || (idx3 < 0)) {
            return null;
        }
        if ( !((idx1 < idx2) && (idx2 < idx3))) {
            return null;
        }
        return new String[] { cmd.substring(idx1 + 1, idx2),
                              cmd.substring(idx2 + 1, idx3) };
    }

    /**
     * _more_
     *
     * @param cmd _more_
     * @param source _more_
     */
    private void processAction(String cmd, Object source) {
        cmd = cmd.trim();

        if (cmd.startsWith("ui.message")) {
            String msg = extractOneArg(cmd);
            message(msg);
            return;
        }


        if (cmd.startsWith("ui.toggle")) {
            String    compId = extractOneArg(cmd);
            Component comp   = (Component) idToComponent.get(compId);
            if (comp == null) {
                System.err.println("Unable to find: " + compId);
                return;
            }
            Container parent = (Container) componentToParent.get(comp);

            if (parent == null) {
                return;
            }
            XmlNode parentNode = (XmlNode) compToNode.get(parent);
            if (parentNode == null) {
                return;
            }
            comp.setVisible( !comp.isVisible());
            layoutContainer(parent, parentNode,
                            (Vector) containerToNodeList.get(parent));
            myContents.invalidate();
            myContents.validate();
        } else if (cmd.startsWith(ACTION_MENUPOPUP)) {
            String id    = extractOneArg(cmd);
            Vector items = (Vector) idToListOfMenuItems.get(id);
            if (items != null) {
                JPopupMenu popupMenu = new JPopupMenu();
                addMenuItems(popupMenu, items);
                myContents.add(popupMenu);
                Component mb = (Component) source;
                popupMenu.show(mb, 0, mb.getBounds().height);
            }
            return;
        } else if (cmd.startsWith(ACTION_WRITE)) {
            String[] args = extractTwoArgsFromCmd(cmd);
            if (args == null) {
                System.err.println("Failed to read args " + cmd);
                return;
            }
            Component comp = (Component) idToComponent.get(args[0]);
            if (comp == null) {
                System.err.println("Component not found ");
                return;
            }
            if ( !(comp instanceof JTextComponent)) {
                System.err.println("Bad component type: " + comp);
                return;
            }
            ((JTextComponent) comp).setText(((JTextComponent) comp).getText()
                                            + args[1]);
        } else if (cmd.startsWith(ACTION_UI_FLIP)) {
            //flip (panel,subcomponent)
            String[] args = extractTwoArgsFromCmd(cmd);
            if (args != null) {
                Container panel = (Container) idToComponent.get(args[0]);
                Component comp  = (Component) idToComponent.get(args[1]);
                if ((panel == null) || (comp == null)) {
                    System.err.println("Unable to find: " + args[0] + " or "
                                       + args[1]);
                    return;
                }
                CardLayout layout = (CardLayout) panel.getLayout();
                layout.show(panel, args[0]);
            } else {
                String arg = extractOneArg(cmd);
                if (arg == null) {
                    return;
                }
                Container panel = (Container) idToComponent.get(arg);
                if (panel == null) {
                    System.err.println("Unable to find: " + arg);
                    return;
                }
                CardLayout layout = (CardLayout) panel.getLayout();
                layout.next(panel);
            }
        } else {
            actionListener.actionPerformed(new ActionEvent(source, 0, cmd));
        }
    }



    /**
     * _more_
     *
     * @param menu _more_
     * @param id _more_
     */
    public void addMenuItems(JMenu menu, String id) {
        addMenuItems(menu, (Vector) idToListOfMenuItems.get(id));
    }

    /**
     * _more_
     *
     * @param menu _more_
     * @param items _more_
     */
    public void addMenuItems(JMenu menu, Vector items) {
        if (items == null) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            Object item = items.elementAt(i);
            if (item instanceof JMenuItem) {
                menu.add((JMenuItem) item);
            } else {
                menu.addSeparator();
            }
        }
    }

    /**
     * _more_
     *
     * @param menu _more_
     * @param id _more_
     */
    public void addMenuItems(JPopupMenu menu, String id) {
        addMenuItems(menu, (Vector) idToListOfMenuItems.get(id));
    }

    /**
     * _more_
     *
     * @param menu _more_
     * @param items _more_
     */
    public void addMenuItems(JPopupMenu menu, Vector items) {
        if (items == null) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            Object item = items.elementAt(i);
            if (item instanceof JMenuItem) {
                menu.add((JMenuItem) item);
            } else {
                menu.addSeparator();
            }
        }
    }

    /**
     * _more_
     *
     * @param menuId _more_
     * @param cbmi _more_
     */
    public void addMenuItem(String menuId, XmlNode cbmi) {
        JMenu m = (JMenu) idToComponent.get(menuId);
        if (m == null) {
            return;
        }
        addMenuItem(m, null, cbmi, null);
    }

    /**
     * _more_
     *
     * @param m _more_
     * @param items _more_
     * @param node _more_
     * @param actionTemplate _more_
     */
    private void processMenu(JMenu m, Vector items, XmlNode node,
                             String actionTemplate) {
        for (int i = 0; i < node.size(); i++) {
            XmlNode child = getReffedNode(node.get(i));
            addMenuItem(m, items, child, actionTemplate);
        }
    }


    /**
     * _more_
     *
     * @param m _more_
     * @param items _more_
     * @param child _more_
     * @param actionTemplate _more_
     */
    private void addMenuItem(JMenu m, Vector items, XmlNode child,
                             String actionTemplate) {
        if (child.getTag().equals(TAG_SEPARATOR)) {
            if (m != null) {
                m.addSeparator();
            } else {
                items.addElement("separator");
            }
        } else if (child.getTag().equals(TAG_MENUITEM)
                   || child.getTag().equals(TAG_CBMENUITEM)) {

            JMenuItem mi;
            String    id     = child.getAttribute(ATTR_ID, (String) null);
            String    label  = child.getAttribute(ATTR_LABEL, "");
            String    action = child.getAttribute(ATTR_ACTION);
            String    value  = child.getAttribute(ATTR_VALUE);
            String    key    = child.getAttribute(ATTR_KEY);
            if ((action == null) && (actionTemplate != null)
                    && (value != null)) {
                action = GuiUtils.replace(actionTemplate, "%value%", value);
            }
            if ((key != null) && !key.startsWith("group:")) {
                label = label + "     " + key;
            }

            if (child.getTag().equals(TAG_CBMENUITEM)) {
                boolean initValue = child.getAttribute(ATTR_VALUE, true);
                JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(label,
                                             initValue);
                String group = child.getAttribute(ATTR_GROUP, (String) null);
                addToGroup(group, cbmi);
                mi = cbmi;
                if (action != null) {
                    Hashtable actions = new Hashtable();
                    actions.put(ATTR_ACTION, action);
                    actions.put(ATTR_VALUE, new Boolean(initValue));
                    componentToAction.put(cbmi, actions);
                    cbmi.addItemListener(this);
                    if ((group == null) || initValue) {
                        itemStateChanged(new ItemEvent(cbmi, 0, cbmi,
                                ItemEvent.ITEM_STATE_CHANGED));
                    }
                }

                if (key != null) {
                    if (key.startsWith("group:")) {
                        if (group != null) {
                            key = key.substring(6);
                            keyToComponent.put(key.toLowerCase(), group);
                        }
                    } else {
                        keyToComponent.put(key.toLowerCase(), mi);
                    }
                }
            } else {
                mi = new JMenuItem(label);
                if (action != null) {
                    mi.setActionCommand(action);
                    mi.addActionListener(this);
                    if (key != null) {
                        keyToComponent.put(key.toLowerCase(), mi);
                    }
                }
            }
            if (id != null) {
                idToMenuItem.put(id, mi);
            }
            if (m != null) {
                m.add(mi);
            } else {
                items.addElement(mi);
            }

        } else if (child.getTag().equals(TAG_MENU)) {
            JMenu  childMenu = new JMenu(child.getAttribute(ATTR_LABEL, ""));
            String id        = child.getAttribute(ATTR_ID, (String) null);
            if (id != null) {
                idToComponent.put(id, childMenu);
            }
            if (m != null) {
                m.add(childMenu);
            } else {
                items.addElement(childMenu);
            }

            processMenu(childMenu, null, child, actionTemplate);
        }
    }



    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public Component getComponentFromId(String id) {
        return (Component) idToComponent.get(id);
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param enabled _more_
     */
    public void enable(String id, boolean enabled) {
        Component comp = getComponentFromId(id);
        if (comp != null) {
            comp.setEnabled(enabled);
        }
    }

    /**
     * _more_
     *
     * @param msg _more_
     */
    public void message(String msg) {
        setLabel("message", msg);
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param label _more_
     */
    public void setLabel(String id, String label) {
        Component comp = getComponentFromId(id);
        if (comp == null) {
            JMenuItem mi = (JMenuItem) idToMenuItem.get(id);
            if (mi != null) {
                mi.setLabel(label);
            }
        }
        if (comp instanceof JLabel) {
            ((JLabel) comp).setText(label);
        } else if (comp instanceof JButton) {
            ((JButton) comp).setLabel(label);
        }
    }



}

