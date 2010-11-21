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



package com.infocetera.chat;


import com.infocetera.glyph.*;
import com.infocetera.util.*;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.*;


/**
 *
 */

public class EditCanvas extends DisplayCanvas implements MouseListener,
        MouseMotionListener, FocusListener {

    /** _more_ */
    public static final String CMD_EDIT_CUT = "edit.cut";

    /** _more_ */
    public static final String CMD_EDIT_COPY = "edit.copy";

    /** _more_ */
    public static final String CMD_EDIT_PASTE = "edit.paste";

    /** _more_ */
    public static final String CMD_EDIT_SELECTALL = "edit.selectall";

    /** _more_ */
    public static final String CMD_EDIT_GROUP = "edit.group";

    /** _more_ */
    public static final String CMD_EDIT_UNGROUP = "edit.ungroup";


    /**
     *  Gui commands
     */
    public static final String CMD_EDIT_STICKY = "edit.sticky";

    /** _more_ */
    public static final String CMD_ALIGN_PREFIX = "align.";

    /** _more_ */
    public static final String CMD_ALIGN_TOP = CMD_ALIGN_PREFIX + "top";

    /** _more_ */
    public static final String CMD_ALIGN_CENTER = CMD_ALIGN_PREFIX + "center";

    /** _more_ */
    public static final String CMD_ALIGN_BOTTOM = CMD_ALIGN_PREFIX + "bottom";

    /** _more_ */
    public static final String CMD_ALIGN_LEFT = CMD_ALIGN_PREFIX + "left";

    /** _more_ */
    public static final String CMD_ALIGN_MIDDLE = CMD_ALIGN_PREFIX + "middle";

    /** _more_ */
    public static final String CMD_ALIGN_RIGHT = CMD_ALIGN_PREFIX + "right";

    /** _more_ */
    public static final String CMD_SCALE_PREFIX = "scale.";

    /** _more_ */
    public static final String CMD_ZOOM_IN = "zoomin";

    /** _more_ */
    public static final String CMD_ZOOM_OUT = "zoomout";

    /** _more_ */
    public static final String CMD_GFX_PREFIX = "gfx.";

    /** _more_ */
    public static final String CMD_GFX_COLOR = CMD_GFX_PREFIX + "color";

    /** _more_ */
    public static final String CMD_GFX_BGCOLOR = CMD_GFX_PREFIX + "bgcolor";

    /** _more_ */
    public static final String CMD_GFX_FILL = CMD_GFX_PREFIX + "fill";

    /** _more_ */
    public static final String CMD_GFX_NOFILL = CMD_GFX_PREFIX + "nofill";

    /** _more_ */
    public static final String CMD_GFX_WIDTH = CMD_GFX_PREFIX + "width";

    /** _more_ */
    public static final String CMD_GFX_TOFRONT = CMD_GFX_PREFIX + "tofront";

    /** _more_ */
    public static final String CMD_GFX_TOBACK = CMD_GFX_PREFIX + "toback";

    /** _more_ */
    public static final String CMD_POPUP_EDIT = "popup.edit";

    /** _more_ */
    public static final String CMD_POPUP_GFX = "popup.gfx";

    /** _more_ */
    public static final String CMD_POPUP_ZOOM = "popup.zoom";



    /** _more_ */
    public static final Color[] colors = {
        Color.black, Color.white, Color.lightGray, Color.red, Color.orange,
        Color.yellow, Color.green, Color.blue, new Color(255, 153, 255),
        Color.cyan
    };

    /** _more_ */
    public static final String[] colorNames = {
        "Black", "White", "Light gray", "Red", "Orange", "Yellow", "Green",
        "Blue", "Purple", "Cyan"
    };




    /**
     *  List of the names of the shapes we use.
     */
    static String[] shapeNames = {
        "Select", "Rectangle", "R.Rect", "Circle", "Line", "Free-hand",
        "Image", "Text", "Html"
    };

    /**
     *  List of the types of shapes we use
     */
    static String[] shapeTypes = {
        "", Glyph.RECTANGLE, Glyph.ROUNDRECT, Glyph.CIRCLE, Glyph.LINE,
        Glyph.PLINE, Glyph.IMAGE, Glyph.TEXT, Glyph.HTMLTEXT
    };

    /**
     *  Icons for the shape palette
     */
    static String[] shapeGifs = {
        "resource:/com/infocetera/images/pointer.gif",
        "resource:/com/infocetera/images/rect.gif",
        "resource:/com/infocetera/images/rrect.gif",
        "resource:/com/infocetera/images/circle.gif",
        "resource:/com/infocetera/images/line.gif",
        "resource:/com/infocetera/images/poly.gif",
        "resource:/com/infocetera/images/image.gif",
        "resource:/com/infocetera/images/text.gif",
        "resource:/com/infocetera/images/html.gif"
    };



    /** _more_ */
    Choice widthList;

    /** _more_ */
    Vector shapeGroup = new Vector();

    /** _more_ */
    Vector colorGroup = new Vector();

    /** _more_ */
    boolean filled = false;

    /** _more_ */
    JCheckBox filledCbx;

    /** _more_ */
    boolean selectionSticky = true;

    /** _more_ */
    boolean canEdit = true;

    /**
     *  GUI created from the call to doMakeContents
     */
    private Container contents;



    /**
     *  Some local event state
     */
    int lastx;

    /** _more_ */
    int lasty;

    /** _more_ */
    int mousex;

    /** _more_ */
    int mousey;

    /** _more_ */
    boolean mouseWasPressed = false;

    /** _more_ */
    JToggleButton selectButton;

    /** _more_ */
    String currentUrl;


    /**
     *  The current command object. If this is non-null then all
     *  events are routed to it. The command member is set to the return
     *  of the routed-to call
     */
    CanvasCommand currentCommand;

    /** _more_ */
    ChatApplet socketApplet;


    /**
     * _more_
     *
     * @param applet _more_
     */
    public EditCanvas(ChatApplet applet) {
        super(applet);
        this.socketApplet = applet;
        addFocusListener(this);
        addMouseMotionListener(this);
        canEdit = socketApplet.getProperty("whiteboard.editable", true);
        this.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent event) {
                int notches = event.getWheelRotation();
                if (notches < 0) {
                    doZoomIn();
                } else {
                    doZoomOut();
                }
            }
        });

    }



    /**
     * _more_
     *
     * @return _more_
     */
    public SocketApplet getSocketApplet() {
        return socketApplet;
    }

    /**
     *  Create the Glyph creation palette
     *
     * @param imageUrl _more_
     *
     * @return _more_
     */

    public JToggleButton getToggleButton(String imageUrl) {
        Image         image = socketApplet.getImage(imageUrl);
        ImageIcon     icon  = new ImageIcon(image);
        JToggleButton b     = new JToggleButton(icon);
        //b.setBackground(null);
        //b.setContentAreaFilled(false);
        //       b.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        b.setPreferredSize(new Dimension(icon.getIconWidth() + 2,
                                         icon.getIconHeight() + 2));
        return b;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public Component doMakePalette() {
        Component[] shapeButtons = new Component[shapeNames.length];
        ButtonGroup bg           = new ButtonGroup();

        for (int i = 0; i < shapeNames.length; i++) {
            JToggleButton btn = getToggleButton(shapeGifs[i]);
            shapeButtons[i] = btn;
            bg.add(btn);
            shapeGroup.add(btn);
            if (i == 0) {
                selectButton = btn;
            }
        }
        selectButton.setSelected(true);
        GuiUtils.tmpInsets = GuiUtils.ZERO_INSETS;
        Component buttons = GuiUtils.doLayout(shapeButtons, 1, GuiUtils.DS_N,
                                GuiUtils.DS_N);
        GuiUtils.tmpAnchor = GridBagConstraints.EAST;
        GuiUtils.tmpFill   = GridBagConstraints.NONE;
        GuiUtils.tmpInsets = new Insets(2, 2, 2, 2);
        return GuiUtils.doLayout(new Component[] { buttons,
                new JLabel(" ") }, 1, GuiUtils.DS_N, GuiUtils.DS_NY);
    }


    /**
     *  Do we paint the higlighted Glyph/ Only do that if we are in glyph select state
     *
     * @return _more_
     */
    public boolean okToPaintHighlight() {
        if (selectButton == null) {
            return false;
        }
        return selectButton.isSelected();
    }


    /**
     * _more_
     */
    public void setDefaultCursor() {
        setCursor(DEFAULT_CURSOR);
    }





    /**
     *  Called from the base class. It calls the parent to paint the list of
     *  Glyphs and then it tells any highlighted glyph to paint. And then
     *  it tells the currentCommand to paint.
     *
     * @param g _more_
     */
    public void paintInner(Graphics g) {
        super.paintInner(g);
        g.setColor(Color.black);
        if ((highlightedGlyph != null) && okToPaintHighlight()) {
            highlightedGlyph.paintHighlight(g, this);
        }
        if (currentCommand != null) {
            currentCommand.doPaint(g);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean haveCommand() {
        return (currentCommand != null);
    }

    /**
     * _more_
     *
     * @param newCommand _more_
     */
    public void setCommand(CanvasCommand newCommand) {
        debug("EditCanvas.setCommand:" + newCommand);
        if ((currentCommand != null) && (currentCommand != newCommand)) {
            currentCommand.doComplete();
            setDefaultCursor();
        }
        currentCommand = newCommand;

        if (newCommand != null) {
            Cursor cursor = newCommand.getCursor();
            if (cursor != null) {
                setCursor(cursor);
            }

            /**
             *  Atomic commands do not receive any events
             *  we call setCommand with them for  when we have an undo/redo facility
             */
            if (newCommand.isAtomic()) {
                debug("EditCanvas.setCommand: isAtomic");
                setCommand(null);
            }

        }
    }




    /**
     *  We have these hooks here so that derived classes can do things based
     *  on glyph events (e.g., tell the whiteboard server that a Glyph moved
     *
     * @param theGlyph _more_
     */
    public void notifyGlyphMoved(Glyph theGlyph) {}

    /**
     * _more_
     *
     * @param theGlyph _more_
     */
    public void notifyGlyphMoveComplete(Glyph theGlyph) {}

    /**
     * _more_
     *
     * @param theGlyph _more_
     * @param attr _more_
     */
    public void notifyGlyphChanged(Glyph theGlyph, String attr) {}

    /**
     * _more_
     *
     * @param theGlyph _more_
     * @param attr _more_
     */
    public void notifyGlyphChangeDone(Glyph theGlyph, String attr) {}


    /**
     * _more_
     *
     * @param g _more_
     * @param diddleSelection _more_
     */
    public void notifyGlyphCreateComplete(Glyph g, boolean diddleSelection) {
        if (diddleSelection) {
            clearSelection();
            addSelection(g);
        }
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void focusGained(FocusEvent e) {
        if (currentCommand != null) {
            //      currentCommand.doFocusGained (e);
            return;
        }
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void focusLost(FocusEvent e) {
        if (currentCommand != null) {
            debug("EditCanvas.focusLost command = " + currentCommand);
            setCommand(currentCommand.doFocusLost(e));
            return;
        }
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void keyReleased(KeyEvent e) {
        if (currentCommand != null) {
            debug("EditCanvas.keyReleased calling setCommand");
            setCommand(currentCommand.doKeyReleased(e));
            return;
        }
    }


    /**
     * _more_
     *
     * @param evt _more_
     */
    public void keyTyped(KeyEvent evt) {
        if (isDebug()) {
            debug("EditCanvas.keyTyped " + evt);
        }
    }


    /**
     * _more_
     *
     * @param evt _more_
     */
    public void keyPressed(KeyEvent evt) {
        if (isDebug()) {
            debug("EditCanvas.keyPressed " + evt);
        }

        //If we have  a command then route to it.
        if (currentCommand != null) {
            debug("EditCanvas.keyPressed calling setCommand");
            setCommand(currentCommand.doKeyPress(evt));
            return;
        }
        int  code = evt.getKeyCode();
        char key  = evt.getKeyChar();
        if (evt.isControlDown()) {
            key = (char) (key + 'a' - 1);
            if (key == 'x') {
                doCut();
            } else if (code == evt.VK_PLUS) {
                doZoomIn();
            } else if (code == evt.VK_MINUS) {
                doZoomOut();
            } else if (code == evt.VK_EQUALS) {
                setScale(1.0);
            } else if (key == 'c') {
                doCopy();
            } else if (key == 'v') {
                doPaste();
            } else if (key == 'a') {
                selectAll();
            } else if (key == 'g') {
                group();
            } else if (key == 'u') {
                unGroup();
                //Hook for undo
            } else if ((key == 'z') && false) {
                if (cutBuffer != null) {}
            } else {
                super.keyPressed(evt);
            }
            return;
        }

        if ((key == 'f') || (key == 'b')) {
            for (int i = 0; i < selectionSet.size(); i++) {
                Glyph g = (Glyph) selectionSet.elementAt(i);
                if (key == 'f') {
                    moveToFront(g, false);
                } else {
                    moveToBack(g, false);
                }
            }
        }
    }

    /**
     *  Align the set of selected glyphs with the given command
     *  (e.g., align.top, align.bottom, etc.)
     *
     * @param cmd _more_
     */
    public void doAlign(String cmd) {
        int top    = Integer.MAX_VALUE;
        int vmid   = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;
        int left   = Integer.MAX_VALUE;
        int hmid   = Integer.MIN_VALUE;
        int right  = Integer.MIN_VALUE;

        for (int i = 0; i < selectionSet.size(); i++) {
            Glyph     g = (Glyph) selectionSet.elementAt(i);
            Rectangle b = g.getBounds();
            top    = Math.min(b.y, top);
            left   = Math.min(b.x, left);
            bottom = Math.max(b.y + b.height, bottom);
            right  = Math.max(b.x + b.width, right);
            vmid   = Math.max(b.y + b.height / 2, vmid);
            hmid   = Math.max(b.x + b.width / 2, hmid);
        }
        for (int i = 0; i < selectionSet.size(); i++) {
            Glyph     g = (Glyph) selectionSet.elementAt(i);
            Rectangle b = g.getBounds();
            if (cmd.equals(CMD_ALIGN_TOP)) {
                g.moveBy(0, top - b.y);
            } else if (cmd.equals(CMD_ALIGN_CENTER)) {
                g.moveBy(0, vmid - (b.y + b.height / 2));
            } else if (cmd.equals(CMD_ALIGN_BOTTOM)) {
                g.moveBy(0, bottom - (b.y + b.height));
            } else if (cmd.equals(CMD_ALIGN_LEFT)) {
                g.moveBy(left - b.x, 0);
            } else if (cmd.equals(CMD_ALIGN_MIDDLE)) {
                g.moveBy(hmid - (b.x + b.width / 2), 0);
            } else if (cmd.equals(CMD_ALIGN_RIGHT)) {
                g.moveBy(right - (b.x + b.width), 0);
            }
            notifyGlyphMoveComplete(g);
        }
        repaint();
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
        JMenuItem mi = makeMenuItem(label, command);
        if ( !enabled) {
            mi.setEnabled(false);
        }
        return mi;
    }

    /**
     * _more_
     *
     * @param label _more_
     * @param command _more_
     *
     * @return _more_
     */
    JMenuItem makeMenuItem(String label, String command) {
        JMenuItem mi = new JMenuItem(label);
        mi.setActionCommand(command);
        mi.addActionListener(this);
        return mi;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public JMenu makeEditMenu() {
        JMenu editMenu = new JMenu("Edit");
        return makeEditMenu(editMenu);
    }

    /**
     * _more_
     *
     * @param editMenu _more_
     *
     * @return _more_
     */
    public JMenu makeEditMenu(JMenu editMenu) {
        boolean hasSelection = hasSelection();
        boolean hasBuffer    = ((cutBuffer != null)
                                && (cutBuffer.size() > 0));
        editMenu.add(makeMenuItem("Cut         Ctrl+x", CMD_EDIT_CUT,
                                  hasSelection));
        editMenu.add(makeMenuItem("Copy        Ctrl+c", CMD_EDIT_COPY,
                                  hasSelection));
        editMenu.add(makeMenuItem("Paste       Ctrl+v", CMD_EDIT_PASTE,
                                  hasBuffer));
        editMenu.addSeparator();
        editMenu.add(makeMenuItem("Select all  Ctrl+a", CMD_EDIT_SELECTALL));
        editMenu.addSeparator();
        editMenu.add(makeMenuItem("Group       Ctrl+g", CMD_EDIT_GROUP,
                                  hasSelection));
        editMenu.add(makeMenuItem("Ungroup     Ctrl+u", CMD_EDIT_UNGROUP,
                                  hasSelection));
        return editMenu;
    }

    /**
     * _more_
     *
     * @param editMenu _more_
     *
     * @return _more_
     */
    public JPopupMenu makeEditMenu(JPopupMenu editMenu) {
        boolean hasSelection = hasSelection();
        boolean hasBuffer    = ((cutBuffer != null)
                                && (cutBuffer.size() > 0));
        editMenu.add(makeMenuItem("Cut         Ctrl+x", CMD_EDIT_CUT,
                                  hasSelection));
        editMenu.add(makeMenuItem("Copy        Ctrl+c", CMD_EDIT_COPY,
                                  hasSelection));
        editMenu.add(makeMenuItem("Paste       Ctrl+v", CMD_EDIT_PASTE,
                                  hasBuffer));
        editMenu.addSeparator();
        editMenu.add(makeMenuItem("Select all  Ctrl+a", CMD_EDIT_SELECTALL));
        editMenu.addSeparator();
        editMenu.add(makeMenuItem("Group       Ctrl+g", CMD_EDIT_GROUP,
                                  hasSelection));
        editMenu.add(makeMenuItem("Ungroup     Ctrl+u", CMD_EDIT_UNGROUP,
                                  hasSelection));
        return editMenu;
    }


    /**
     *  Create the display menu
     *
     * @return _more_
     */
    public JMenu makeDisplayMenu() {
        JMenu displayMenu = new JMenu("Display");
        displayMenu.add(makeMenuItem("Filled", CMD_GFX_FILL));
        displayMenu.add(makeMenuItem("Not filled", CMD_GFX_NOFILL));
        JMenu widthMenu = new JMenu("Width");
        for (int i = 1; i < 15; i++) {
            widthMenu.add(makeMenuItem("" + i, CMD_GFX_WIDTH + i));
        }
        displayMenu.add(widthMenu);

        JMenu colorMenu = new JMenu("Color");
        for (int i = 0; i < colorNames.length; i++) {
            colorMenu.add(makeMenuItem(colorNames[i], CMD_GFX_COLOR + i));
        }
        displayMenu.add(colorMenu);

        JMenu bgcolorMenu = new JMenu("Background color");
        for (int i = 0; i < colorNames.length; i++) {
            bgcolorMenu.add(makeMenuItem(colorNames[i], CMD_GFX_BGCOLOR + i));
        }
        bgcolorMenu.add(makeMenuItem("null",
                                     CMD_GFX_BGCOLOR + colorNames.length));
        displayMenu.add(bgcolorMenu);

        displayMenu.add(makeMenuItem("To front", CMD_GFX_TOFRONT));
        displayMenu.add(makeMenuItem("To back", CMD_GFX_TOBACK));
        return displayMenu;
    }


    /**
     *  What is the color of the selected color button in the color palette
     *
     * @return _more_
     */
    public Color getColor() {
        for (int i = 0; i < colorGroup.size(); i++) {
            ImageButton tb = (ImageButton) colorGroup.elementAt(i);
            if (tb.state) {
                return tb.color;
            }
        }
        return Color.black;
    }


    /**
     *  Create the alignment menu
     *
     * @return _more_
     */
    public JMenu makeAlignMenu() {
        JMenu alignMenu = new JMenu("Align");
        alignMenu.add(makeMenuItem("Top", CMD_ALIGN_TOP));
        alignMenu.add(makeMenuItem("Center", CMD_ALIGN_CENTER));
        alignMenu.add(makeMenuItem("Bottom", CMD_ALIGN_BOTTOM));
        alignMenu.add(makeMenuItem("Left", CMD_ALIGN_LEFT));
        alignMenu.add(makeMenuItem("Middle", CMD_ALIGN_MIDDLE));
        alignMenu.add(makeMenuItem("Right", CMD_ALIGN_RIGHT));
        return alignMenu;
    }




    /**
     *  Create the color choosing palette
     *
     * @param vertical _more_
     *
     * @return _more_
     */
    public Container doMakeColorPanel(boolean vertical) {
        Component[] colorCbx = new Component[colors.length];
        for (int i = 0; i < colors.length; i++) {
            ImageButton cbx =
                new ImageButton(colorGroup,
                                "resource:/com/infocetera/images/check.gif",
                                null, colors[i], 0);
            colorCbx[i] = cbx;
            if (i == 0) {
                cbx.setState(true);
            }
        }
        //    GuiUtils.tmpInsets = new Insets(1,1,0,0);
        return GuiUtils.doLayout(colorCbx, (vertical
                                            ? 1
                                            : colorCbx.length), GuiUtils
                                            .DS_N, GuiUtils.DS_N);

    }



    /**
     *  Create the gui
     *
     * @return _more_
     */
    public Container doMakeContents() {
        if (contents != null) {
            return contents;
        }

        if ( !canEdit) {
            return contents = super.doMakeContents();
        }

        Component palette    = doMakePalette();
        Component colorPanel = doMakeColorPanel(false);

        widthList = new Choice();
        for (int i = 1; i <= 15; i++) {
            widthList.addItem("" + i);
        }
        filledCbx = new JCheckBox("Filled");

        Container bottom   = null;


        JMenuBar  menuBar  = new JMenuBar();
        JMenu     viewMenu = new JMenu("View");
        viewMenu.add(makeMenuItem("Zoom In", CMD_ZOOM_IN, true));
        viewMenu.add(makeMenuItem("Zoom Out", CMD_ZOOM_OUT, true));
        viewMenu.addSeparator();
        viewMenu.add(makeDisplayMenu());
        viewMenu.add(makeAlignMenu());

        menuBar.add(makeEditMenu());
        menuBar.add(viewMenu);

        //    JButton viewButton = new JButton ("Zoom");
        //    viewButton.setActionCommand (CMD_POPUP_ZOOM);
        //    viewButton.addActionListener (this);    


        GuiUtils.tmpAnchor = GridBagConstraints.NORTH;

        Component ctrl1 = GuiUtils.doLayout(new Component[] { filledCbx,
                new JLabel("Width:"), widthList, new JLabel(" "),
                colorPanel }, 5, GuiUtils.DS_N, GuiUtils.DS_N);


        GuiUtils.tmpInsets = new Insets(0, 0, 0, 2);

        Component menus = menuBar;
        JPanel    top   = GuiUtils.leftCenterRight(menus, null, ctrl1);
        contents = GuiUtils.topCenterBottom(top, super.doMakeContents(),
                                            null);
        contents.add("West", palette);
        return contents;
    }





    /**
     * _more_
     *
     * @param event _more_
     */
    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals(CMD_EDIT_CUT)) {
            System.err.println(getGlyphXml());

            doCut();
        } else if (action.equals(CMD_EDIT_COPY)) {
            doCopy();
        } else if (action.equals(CMD_EDIT_PASTE)) {
            doPaste();
        } else if (action.equals(CMD_EDIT_SELECTALL)) {
            selectAll();
        } else if (action.equals(CMD_EDIT_GROUP)) {
            group();
        } else if (action.equals(CMD_EDIT_UNGROUP)) {
            unGroup();
        } else if (action.equals(CMD_POPUP_GFX)) {
            Component src = (Component) event.getSource();
            showGfxMenu(src, 0, src.getBounds().height);
        } else if (action.equals(CMD_POPUP_EDIT)) {
            Component src = (Component) event.getSource();
            showEditMenu(src, 0, src.getBounds().height);
        } else if (action.equals(CMD_EDIT_STICKY)) {
            selectionSticky = !selectionSticky;
        } else if (action.equals(CMD_POPUP_ZOOM)) {
            Component src = (Component) event.getSource();
            showScaleMenu(src, 0, src.getBounds().height);
        } else if (action.startsWith(CMD_ALIGN_PREFIX)) {
            doAlign(action);
        } else if (action.equals(CMD_ZOOM_OUT)) {
            doZoomOut();
        } else if (action.equals(CMD_ZOOM_IN)) {
            doZoomIn();
        } else if (action.startsWith(CMD_SCALE_PREFIX)) {
            doScale(action.substring(6));
        } else if (action.startsWith(CMD_GFX_PREFIX)) {
            doGfxCommand(action);
        } else {
            super.actionPerformed(event);
        }
    }

    /**
     * _more_
     */
    public void group() {
        if ( !hasSelection()) {
            return;
        }
        if ((selectionSet.size() == 1)
                && (selectionSet.elementAt(0) instanceof CompositeGlyph)) {
            return;
        }

        Glyph glyph = new CompositeGlyph(0, 0, selectionSet);
        glyph.setId(getGlyphId());
        addGlyph(glyph);
        notifyGlyphCreateComplete(glyph, true);
    }

    /**
     * _more_
     */
    public void unGroup() {
        for (int i = 0; i < selectionSet.size(); i++) {
            Glyph g = (Glyph) selectionSet.elementAt(i);
            if ( !(g instanceof CompositeGlyph)) {
                continue;
            }
            //First ungroup, then tell others of the change, then do the remove
            ((CompositeGlyph) g).unGroup();
            notifyGlyphChangeDone(g, Glyph.ATTR_CHILDREN);
            removeGlyph(g);
        }
        repaint();
    }

    /**
     *  Paste the given vector of glyphs.
     *  We find the upper left point of the set of glyphs
     *  to get an offset from the given x,y coords.
     *
     * @param l _more_
     * @param x _more_
     * @param y _more_
     */
    public void doPaste(Vector l, int x, int y) {
        if (l == null) {
            return;
        }
        clearSelection();
        int ox = Integer.MAX_VALUE;
        int oy = Integer.MAX_VALUE;
        for (int i = 0; i < l.size(); i++) {
            Glyph     g = (Glyph) l.elementAt(i);
            Rectangle b = g.getBounds();
            if (b.x < ox) {
                ox = b.x;
            }
            if (b.y < oy) {
                oy = b.y;
            }
        }

        for (int i = 0; i < l.size(); i++) {
            //Get a new id, shift position, add glyph to the glyph list and selection set
            Glyph g = (Glyph) l.elementAt(i);
            g.setId(getGlyphId());
            g.moveBy(x - ox, y - oy);
            addGlyph(g);
            addSelection(g);
            notifyGlyphCreateComplete(g, false);
        }
    }

    /**
     * _more_
     */
    public void doPaste() {
        doPaste(cloneGlyphs(cutBuffer), mousex, mousey);
    }


    /**
     * _more_
     *
     * @param doCopy _more_
     */
    public void newGlyphSet(boolean doCopy) {
        clearSelection();
        super.newGlyphSet(doCopy);
    }

    /**
     * _more_
     *
     * @param idx _more_
     */
    public void setGlyphSet(int idx) {
        clearSelection();
        super.setGlyphSet(idx);
    }

    /**
     * _more_
     */
    public void doCopy() {
        cutBuffer = cloneGlyphs(selectionSet);
    }


    /**
     * _more_
     */
    public void doCut() {
        cutBuffer    = selectionSet;
        selectionSet = new Vector();
        for (int i = 0; i < cutBuffer.size(); i++) {
            Glyph g = (Glyph) cutBuffer.elementAt(i);
            if (g.getPersistent()) {
                removeGlyph(g);
            }
        }
    }

    /**
     * _more_
     */
    public void selectAll() {
        selectionSet.removeAllElements();
        for (int i = 0; i < glyphs.size(); i++) {
            Glyph glyph = (Glyph) glyphs.elementAt(i);
            if (glyph.pickable()) {
                selectionSet.addElement(glyph);
            }
        }
        repaint();
    }




    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseClicked(MouseEvent e) {
        requestFocus();
        //Right click?
        if ((e.getModifiers() & e.BUTTON1_MASK) == 0) {
            showCanvasMenu(this, e.getX(), e.getY());
            return;
        }

        if (currentUrl != null) {
            socketApplet.showUrl(currentUrl, "CHAT.URL");
            return;
        }

        if ( !canEdit) {
            return;
        }

        //Did the user click in a TextGlyph?
        if (((highlightedGlyph != null)
                && (highlightedGlyph
                    instanceof TextGlyph)) || ((e.getClickCount() > 1)
                        && (lastClickedGlyph != null))) {
            TextGlyph textGlyph = (TextGlyph) highlightedGlyph;
            if (textGlyph == null) {
                textGlyph = lastClickedGlyph;
            } else {
                lastClickedGlyph = textGlyph;
            }
            setHighlight(null);

            if (e.isShiftDown() || (e.getClickCount() > 1)) {
                TextArea t = new TextArea(textGlyph.getText());
                Container body =
                    GuiUtils.topCenterBottom(new JLabel("Edit text:"), t,
                                             null);
                JFrame         myFrame   = GuiUtils.getFrame(socketApplet);
                OkCancelDialog theDialog = new OkCancelDialog(myFrame, body);
                Point          sp = GuiUtils.getScreenLocation(this, null);
                theDialog.setLocation(sp.x + e.getX(), sp.y + e.getY());
                theDialog.init();
                if (theDialog.okPressed) {
                    textGlyph.setText(t.getText());
                    notifyGlyphChangeDone(textGlyph, Glyph.ATTR_TEXT);
                    repaint();
                }
            } else {
                clearSelection();
                setCommand(new TextGlyphCreator(this, e, true, textGlyph,
                        e.getX(), e.getY()));
            }
        }

        if (e.getClickCount() == 2) {
            lastClickedGlyph = null;
        }
    }



    /**
     *  Handle mouse events
     */
    TextGlyph lastClickedGlyph = null;










    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseMoved(MouseEvent e) {
        //    debug ("mouseMoved");
        int x = e.getX();
        int y = e.getY();
        //Remember to translate coords from the ScrollCanvas class
        x      = translateInputX(x);
        y      = translateInputY(y);
        mousex = x;
        mousey = y;

        if (currentCommand != null) {
            return;
        }

        //We highlight the nearest glyph - only repaint what is neccessary
        Glyph   closestGlyph  = findGlyph(glyphs, x, y, 6.0);
        Glyph   lastHighlight = highlightedGlyph;
        boolean hadUrl        = (currentUrl != null);
        currentUrl = null;

        if ((closestGlyph != null) && (closestGlyph instanceof HtmlGlyph)) {
            Glyph subG = findGlyph(((HtmlGlyph) closestGlyph).children, x, y,
                                   1);
            if ((subG != null) && (subG.url != null)) {
                currentUrl = subG.url;
            }
        }
        if (hadUrl && (currentUrl == null)) {
            setCursor(DisplayCanvas.DEFAULT_CURSOR);
        } else if ( !hadUrl && (currentUrl != null)) {
            setCursor(DisplayCanvas.HAND_CURSOR);
        }

        if (closestGlyph != lastHighlight) {
            if (highlightedGlyph != null) {
                repaint(highlightedGlyph);
            }
            setHighlight(closestGlyph);
            if (highlightedGlyph != null) {
                repaint(highlightedGlyph);
            }
        }
    }



    /** _more_ */
    CanvasCommand dragCommand;


    /**
     * _more_
     *
     * @param e _more_
     */
    public void mousePressed(MouseEvent e) {
        int x   = e.getX();
        int y   = e.getY();

        int idx = -1;
        for (int i = 0; (idx < 0) && (i < shapeGroup.size()); i++) {
            JToggleButton tb = (JToggleButton) shapeGroup.elementAt(i);
            if (tb.isSelected()) {
                idx = i;
            }
        }

        if (idx <= 0) {
            mousePressedBase(e);
            return;
        }

        if ( !selectionSticky && (selectButton != null)) {
            selectButton.setSelected(true);
        }
        x = translateInputX(x);
        y = translateInputY(y);

        String currentGfx = shapeTypes[idx];
        Glyph  glyph      = createGlyph(currentGfx, x, y, true);

        debug("WBC.creating glyph");
        if (glyph != null) {
            glyph.setId(getGlyphId());
            if (currentGfx.equals(Glyph.IMAGE)) {
                debug("WBC.created image");
                if (selectButton != null) {
                    selectButton.setSelected(true);
                }
                notifyGlyphCreateComplete(glyph, true);
                addGlyph(glyph);
                return;
            }
            glyph.setWidth(widthList.getSelectedIndex() + 1);
            glyph.setColor(getColor());
            if (filledCbx.isSelected()) {
                glyph.setFilled(true);
            }
            clearSelection();
            if ((glyph instanceof PolyGlyph)
                    && !((PolyGlyph) glyph).isSingleLine) {
                setCommand(new PolyGlyphCreator(this, e, (PolyGlyph) glyph,
                        x, y));
            } else if (glyph instanceof TextGlyph) {
                setCommand(new TextGlyphCreator(this, e, (TextGlyph) glyph,
                        x, y));
                repaint(glyph);
            } else {
                debug("WBC.setting command to GlyphCreator");
                setCommand(new GlyphCreator(this, e, glyph, x, y));
            }
            addGlyph(glyph);
        }
    }


    /** _more_ */
    String lastFile = "";

    /**
     * _more_
     *
     * @return _more_
     */
    public String getSelectedFile() {
        return lastFile;
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public String getImageUrl(int x, int y) {
        ArrayList images       = socketApplet.getImageUrls();
        JComboBox imageList    = null;
        String    selectedFile = getSelectedFile();
        TextField urlFld       = new TextField(lastFile, 50);
        JLabel    urlLbl       = new JLabel("Image URL:", Label.RIGHT);
        Container entry;

        JLabel    topLbl2 = new JLabel("Enter an image url");
        GuiUtils.tmpInsets = new Insets(5, 5, 5, 5);
        if (images.size() > 0) {
            ArrayList tmp = new ArrayList(images);
            tmp.add(0, "Select an image");
            imageList = new JComboBox(tmp.toArray());
            entry     = GuiUtils.doLayout(new Component[] {
                urlLbl, urlFld, new JLabel("OR"), new JLabel(""),
                new JLabel("Image:", JLabel.RIGHT),
                GuiUtils.leftCenterRight(imageList, null, null)
            }, 2, GuiUtils.DS_NY, GuiUtils.DS_N);
        } else {
            entry = GuiUtils.doLayout(new Component[] { urlLbl, urlFld }, 2,
                                      GuiUtils.DS_NY, GuiUtils.DS_N);
        }
        Container body = GuiUtils.doLayout(new Component[] { topLbl2,
                             entry }, 1, GuiUtils.DS_Y, GuiUtils.DS_N);
        body = GuiUtils.inset(body, 5, 5);
        if (selectedFile != null) {
            urlFld.setText(selectedFile);
        } else {
            urlFld.setText(lastFile);
        }

        JFrame         myFrame   = GuiUtils.getFrame(socketApplet);
        OkCancelDialog theDialog = new OkCancelDialog(myFrame, body);
        Point          sp        = GuiUtils.getScreenLocation(this, null);
        theDialog.setLocation(sp.x, sp.y);
        theDialog.init();
        if ( !theDialog.okPressed) {
            return null;
        }
        if ((imageList != null) && (imageList.getSelectedIndex() > 0)) {
            ChatApplet.UrlEntry urlEntry =
                (ChatApplet.UrlEntry) images.get(imageList.getSelectedIndex()
                    - 1);
            selectedFile = urlEntry.getUrl();
        } else {
            selectedFile = urlFld.getText();
        }

        lastFile = selectedFile;
        if ( !selectedFile.startsWith("http:")) {
            if ( !selectedFile.startsWith("/")) {
                selectedFile = socketApplet.addFilePrefix(selectedFile);
            }
            selectedFile = socketApplet.getFullUrl(selectedFile);
        }
        return selectedFile;
    }


    /**
     * _more_
     *
     * @param s _more_
     * @param x _more_
     * @param y _more_
     * @param fromHere _more_
     *
     * @return _more_
     */
    public Glyph createGlyph(String s, int x, int y, boolean fromHere) {
        if (fromHere && s.equals(Glyph.IMAGE)) {
            String file = getImageUrl(x, y);
            if (file != null) {
                return new ImageGlyph(this, x, y, file);
            }
            return null;
        }
        return super.createGlyph(s, x, y, fromHere);
    }



    /**
     *  Handle all GUI commands dealing with the graphics
     *
     * @param action _more_
     */
    public void doGfxCommand(String action) {
        if (action.equals(CMD_GFX_FILL)) {
            for (int i = 0; i < selectionSet.size(); i++) {
                Glyph g = (Glyph) selectionSet.elementAt(i);
                g.setFilled(true);
                notifyGlyphChangeDone(g, Glyph.ATTR_FILL);
            }
        } else if (action.equals(CMD_GFX_NOFILL)) {
            for (int i = 0; i < selectionSet.size(); i++) {
                Glyph g = (Glyph) selectionSet.elementAt(i);
                g.setFilled(false);
                notifyGlyphChangeDone(g, Glyph.ATTR_FILL);
            }
        } else if (action.startsWith(CMD_GFX_COLOR)
                   || action.startsWith(CMD_GFX_BGCOLOR)) {
            boolean doColor = action.startsWith(CMD_GFX_COLOR);
            int     index   = new Integer(action.substring((doColor
                    ? CMD_GFX_COLOR.length()
                    : CMD_GFX_BGCOLOR.length()))).intValue();
            Color color = null;
            if (index < colors.length) {
                color = colors[index];
            }

            for (int i = 0; i < selectionSet.size(); i++) {
                Glyph g = (Glyph) selectionSet.elementAt(i);
                if (doColor) {
                    g.setColor(color);
                } else {
                    g.setBgColor(color);
                }
                notifyGlyphChangeDone(g, (doColor
                                          ? Glyph.ATTR_COLOR
                                          : Glyph.ATTR_BGCOLOR));
            }
            repaint();
        } else if (action.startsWith(CMD_GFX_WIDTH)) {
            int width = new Integer(
                            action.substring(
                                CMD_GFX_WIDTH.length())).intValue();

            for (int i = 0; i < selectionSet.size(); i++) {
                Glyph g = (Glyph) selectionSet.elementAt(i);
                g.setWidth(width);
                notifyGlyphChangeDone(g, Glyph.ATTR_WIDTH);
            }
            repaint();
        } else if (action.equals(CMD_GFX_TOFRONT)) {
            for (int i = 0; i < selectionSet.size(); i++) {
                Glyph theGlyph = (Glyph) selectionSet.elementAt(i);
                moveToFront(theGlyph, false);
            }
        } else if (action.equals(CMD_GFX_TOBACK)) {
            for (int i = 0; i < selectionSet.size(); i++) {
                Glyph theGlyph = (Glyph) selectionSet.elementAt(i);
                moveToBack(theGlyph, false);
            }
        } else {
            return;
        }
        repaint();
    }





    /**
     * _more_
     *
     * @param e _more_
     */
    private void mousePressedBase(MouseEvent e) {
        requestFocus();
        debug("EditCanvas.mousePressed currentCommand=" + currentCommand);
        dragCommand = null;
        int x = e.getX();
        int y = e.getY();


        //Remember to translate coords from the ScrollCanvas class
        x = translateInputX(x);
        y = translateInputY(y);

        //Route current currentCommand
        if (currentCommand != null) {
            setCommand(currentCommand.doMousePressed(e, x, y));
            repaint();
            return;
        }

        //If right mouse then do nothing
        if (e.isMetaDown()) {
            return;
        }

        //Are we near a glyph?
        Glyph nearestGlyph = (highlightedGlyph != null)
                             ? highlightedGlyph
                             : findGlyph(x, y);

        //If not then drag out a selection rectangle
        if (nearestGlyph == null) {
            clearSelection();
            setCommand(new DragRectCommand(this, e, x, y));
            return;
        }

        //We clicked on a glyph - screw around with the selection set
        boolean alreadySelected = isSelected(nearestGlyph);
        if ( !alreadySelected) {
            if ( !e.isShiftDown()) {
                clearSelection();
            }
            addSelection(nearestGlyph);
        } else {
            if (e.isShiftDown()) {
                removeSelection(nearestGlyph);
            }
        }
        //Move/stretch this glyph
        debug("Creating new GlyphStretcher");

        dragCommand = new GlyphStretcher(this, e, nearestGlyph, selectionSet,
                                         x, y);
    }



    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseDragged(MouseEvent e) {
        debug("EditCanvas.mouseDragged");
        setHighlight(null);

        if (dragCommand != null) {
            setCommand(dragCommand);
            dragCommand = null;
        }

        int x = e.getX();
        int y = e.getY();

        //If we have  a command then route to it.
        if (currentCommand != null) {
            mouseWasPressed = false;
            scrollToPoint(x, y);
            setCommand(currentCommand.doMouseDragged(e, translateInputX(x),
                    translateInputY(y)));
            return;
        }



    }



    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseReleased(MouseEvent e) {
        debug("mouseReleased");
        int x = e.getX();
        int y = e.getY();
        mouseWasPressed = false;
        if (currentCommand != null) {
            //Remember to translate coords from the ScrollCanvas class
            debug("EditCanvas.mouseReleased - routing to currentCommand:"
                  + currentCommand);
            setCommand(currentCommand.doMouseReleased(e, translateInputX(x),
                    translateInputY(y)));
        }
    }

    /**
     * Popup the menu at the location given with respect  to the src component
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
     *  Popup the menu that has the display and alignment menus.
     *  Called when user right clicks in canvas
     *
     * @param src _more_
     * @param x _more_
     * @param y _more_
     */
    public void showGfxMenu(Component src, int x, int y) {
        JPopupMenu gfxPopup = new JPopupMenu();
        gfxPopup.add(makeDisplayMenu());
        gfxPopup.add(makeAlignMenu());
        doPopup(gfxPopup, src, x, y);
    }

    /**
     *  Called from the menu button click
     *
     * @param src _more_
     * @param x _more_
     * @param y _more_
     */
    public void showCanvasMenu(Component src, int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(makeEditMenu());
        popup.add(makeDisplayMenu());
        popup.add(makeAlignMenu());
        doPopup(popup, src, x, y);
    }




    /**
     *  Show the edit menu
     *
     * @param src _more_
     * @param x _more_
     * @param y _more_
     */
    public void showEditMenu(Component src, int x, int y) {
        JPopupMenu editPopup = new JPopupMenu();
        makeEditMenu(editPopup);
        editPopup.addSeparator();
        if (selectionSticky) {
            editPopup.add(makeMenuItem("Make unsticky", CMD_EDIT_STICKY,
                                       true));
        } else {
            editPopup.add(makeMenuItem("Make sticky", CMD_EDIT_STICKY, true));
        }
        doPopup(editPopup, src, x, y);
    }


    /**
     *  Show the zoom menu
     *
     * @param src _more_
     * @param x _more_
     * @param y _more_
     */
    public void showScaleMenu(Component src, int x, int y) {
        JPopupMenu scaleMenu = new JPopupMenu();
        int        current   = getScaleIdx();
        for (int i = 0; i < scaleFactors.length; i++) {
            int sf = (int) (scaleFactors[i] * 100.0);
            if (current == i) {
                scaleMenu.add(makeMenuItem(">" + sf + "%",
                                           CMD_SCALE_PREFIX + i));
            } else {
                scaleMenu.add(makeMenuItem(" " + sf + "%",
                                           CMD_SCALE_PREFIX + i));
            }
        }
        //    this.add (scaleMenu);
        Rectangle bounds = src.getBounds();
        scaleMenu.show(src, 0, bounds.height);
    }


    /**
     * _more_
     *
     * @param sf _more_
     */
    public void doScale(String sf) {
        setScaleIdx(new Integer(sf).intValue());
    }







}


/**
 *  This class draws a rectangle on its canvas as the mouse is dragged.
 *  When done dragging it tells the canvas to select the Glyphs in the
 *  rectangle
 */

class DragRectCommand extends CanvasCommand {

    /** _more_ */
    int anchorX;

    /** _more_ */
    int anchorY;

    /** _more_ */
    int currentX;

    /** _more_ */
    int currentY;

    /**
     * _more_
     *
     * @param canvas _more_
     * @param firstEvent _more_
     * @param x _more_
     * @param y _more_
     */
    public DragRectCommand(EditCanvas canvas, AWTEvent firstEvent, int x,
                           int y) {
        super(canvas, firstEvent, x, y);
        anchorX  = x;
        anchorY  = y;
        currentX = x;
        currentY = y;
    }

    /**
     *  Draw the currently dragged rectangle
     *
     * @param g _more_
     */
    public void doPaint(Graphics g) {
        g.setColor(Color.lightGray);
        Rectangle r = canvas.scaleRect(getRect());
        GuiUtils.drawRect(g, r.x, r.y, r.width, r.height, 2);
    }

    /**
     *  Normalize the Rectangle
     *
     * @return _more_
     */
    Rectangle getRect() {
        return new Rectangle(((anchorX < currentX)
                              ? anchorX
                              : currentX), ((anchorY < currentY)
                                            ? anchorY
                                            : currentY), Math.abs(anchorX
                                            - currentX), Math.abs(anchorY
                                                - currentY));
    }


    /**
     *  Reset the dragged x,y location and tell the canvas to repaint
     *  Return this to denote that the command is still active
     *
     * @param e _more_
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public CanvasCommand doMouseDragged(MouseEvent e, int x, int y) {
        currentX = x;
        currentY = y;
        canvas.repaint();
        return this;
    }


    /**
     *  Done
     *  Return null to denote that the command is not active
     *
     * @param e _more_
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public CanvasCommand doMouseReleased(MouseEvent e, int x, int y) {
        canvas.select(getRect());
        canvas.repaint();
        return null;
    }

    /**
     * _more_
     */
    protected void doComplete() {}



}  //DragRectCommand


