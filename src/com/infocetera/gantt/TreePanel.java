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

package com.infocetera.gantt;


import com.infocetera.util.*;

import java.applet.*;


import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.net.*;

import java.util.*;



/**
 * Class TreePanel _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class TreePanel extends ScrollCanvas implements MouseListener,
        MouseMotionListener {

    /** _more_          */
    public static final String CMD_CLOSE = "close";

    /** _more_          */
    public static final String CMD_WEIGHTBY = "weightby";

    /** _more_          */
    public static final int TREE_DURATION = 0;

    /** _more_          */
    public static final int TREE_COMPLETE = 1;

    /** _more_          */
    public static final int TREE_LEFT = 2;

    /** _more_          */
    public static final int TREE_RANGE = 3;

    /** _more_          */
    static final String[] WEIGHT_TREE_BY = { "duration", "complete", "left",
                                             "range" };

    /** _more_          */
    int weightTreeBy = TREE_DURATION;


    /** _more_          */
    GanttView view;

    /** _more_          */
    Dimension lastSize;

    /** _more_          */
    NestedTreeNode treeHilite;

    /** _more_          */
    GanttTask hilite;

    /** _more_          */
    int mouseX;

    /** _more_          */
    int mouseY;

    /** _more_          */
    XmlUi xmlUi;

    /** _more_          */
    NestedTreeNode treeRoot;

    /** _more_          */
    Frame treeFrame;

    /** _more_          */
    int colorByIndex = GanttView.COLOR_RESOURCE;

    /** _more_          */
    Vector taskIds = new Vector();

    /** _more_          */
    int level = 2;


    /**
     * _more_
     *
     * @param view _more_
     */
    public TreePanel(GanttView view) {
        this.view = view;
        init(view.topTasks);
    }


    /**
     * _more_
     *
     * @param view _more_
     * @param task _more_
     */
    public TreePanel(GanttView view, GanttTask task) {
        this.view = view;
        init(GuiUtils.vector(task));
    }


    /**
     * _more_
     */
    public void stop() {
        treeFrame.dispose();
    }

    /**
     * _more_
     *
     * @param tasks _more_
     */
    private void init(Vector tasks) {
        if (tasks != null) {
            for (int i = 0; i < tasks.size(); i++) {
                taskIds.addElement(((GanttTask) tasks.elementAt(i)).getId());
            }
        }

        addMouseListener(this);
        addMouseMotionListener(this);
        treeFrame = new Frame("Task Tree Map");
        treeFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                doClose();
            }
        });

        treeFrame.setLayout(new BorderLayout());
        String skinPath = "/com/infocetera/gantt/treeskin.xml";
        String uiXml = new String(GuiUtils.readResource(skinPath, getClass(),
                           true));
        Hashtable comps = new Hashtable();
        comps.put("treepanel", this);
        xmlUi = new XmlUi(view.ganttApplet, XmlNode.parse(uiXml).get(0),
                          comps, this, this);
        treeFrame.add("Center", xmlUi.getContents());
        treeFrame.pack();
        treeFrame.setSize(new Dimension(600, 400));
        treeFrame.show();
        setBackground(Color.white);
        treeFrame.setBackground(view.ganttApplet.getBackground());
    }

    /**
     * _more_
     *
     * @param ae _more_
     */
    public void actionPerformed(ActionEvent ae) {
        String commands = ae.getActionCommand();
        Vector cmds     = GuiUtils.parseCommands(commands);
        for (int i = 0; i < cmds.size(); i++) {
            String[] sa     = (String[]) cmds.elementAt(i);
            String   func   = sa[0];
            String   params = sa[1];
            try {
                handleCommand(func, params, ae);
            } catch (Exception exc) {
                System.err.println("Error handling command: " + func);
                exc.printStackTrace();
            }
        }
    }

    /**
     * _more_
     */
    private void doClose() {
        treeFrame.dispose();
        treeFrame = null;
        view.removeTreePanel(this);
    }

    /**
     * _more_
     *
     * @param func _more_
     * @param params _more_
     * @param ae _more_
     *
     * @throws Exception _more_
     */
    private void handleCommand(String func, String params, ActionEvent ae)
            throws Exception {
        if (func.equals(CMD_WEIGHTBY)) {
            weightTreeBy = GuiUtils.getIndex(WEIGHT_TREE_BY, null, params,
                                             weightTreeBy);
            makeTree();
            return;
        }

        if (func.equals(CMD_CLOSE)) {
            doClose();
            return;
        }

        if (func.equals(GanttView.CMD_COLORBY)) {
            colorByIndex = GuiUtils.getIndex(GanttView.COLORBYS, null,
                                             params, colorByIndex);
            makeTree();
            return;
        }

        view.handleFunction(func, params, null, null, ae);

    }

    /**
     * _more_
     *
     * @param msg _more_
     */
    public void message(String msg) {
        xmlUi.setLabel("message", msg);
    }



    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintInner(Graphics g) {
        if (treeRoot == null) {
            makeTree();
            if (treeRoot == null) {
                return;
            }
        }
        Dimension size = getSize();
        if ((lastSize == null) || !lastSize.equals(size)) {
            lastSize = size;
            treeRoot.layout(new Rectangle(0, 0, size.width, size.height),
                            false, 5);
        }
        treeRoot.paintChildren(g);
        if ((treeHilite != null) && (treeHilite != treeRoot)) {
            treeHilite.paint(g, true);
            view.paintMouseOver(this, g, (GanttTask) (treeHilite.object),
                                mouseX, mouseY, size.width, size.height);
        }
    }

    /**
     * _more_
     */
    public void makeTree() {
        lastSize   = null;
        treeHilite = null;
        hilite     = null;

        treeRoot   = new NestedTreeNode("", null);
        for (int i = 0; i < taskIds.size(); i++) {
            GanttTask task = view.getTask((String) taskIds.elementAt(i));
            if (task != null) {
                treeRoot.add(task.getTreeNode(weightTreeBy, colorByIndex));
            }
        }
        repaint();
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

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mousePressed(MouseEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseDragged(MouseEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseClicked(MouseEvent e) {
        if ((e.getModifiers() & e.BUTTON1_MASK) == 0 && (hilite != null)) {
            view.showTaskMenu(hilite, true, this, e.getX(), e.getY());
            return;
        }
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseReleased(MouseEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseMoved(MouseEvent e) {
        view.clearMouseOver(this);
        if (treeRoot == null) {
            return;
        }
        NestedTreeNode closestTreeNode = treeRoot.find(e.getX(), e.getY());
        if (closestTreeNode != treeHilite) {
            message("");
            treeHilite = closestTreeNode;
            if (treeHilite != null) {
                hilite = (GanttTask) treeHilite.object;
                if (hilite != null) {
                    message(hilite.getMessage());
                }
            }
            mouseX = e.getX();
            mouseY = e.getY();

            repaint();
        }
    }




}

