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



import com.infocetera.util.*;



import java.awt.*;
import java.awt.event.*;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.*;


/**
 * Class UserList _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class UserList extends ScrollCanvas implements MouseListener,
        KeyListener {

    /** _more_ */
    Component contents;

    /** _more_ */
    int rowHeight;

    /** _more_ */
    int selectedIdx = -1;

    /** _more_ */
    Vector users = new Vector();

    /** _more_ */
    String user;

    /** _more_ */
    ChatApplet applet;

    /**
     * _more_
     *
     * @param user _more_
     * @param applet _more_
     */
    public UserList(String user, ChatApplet applet) {
        this.applet = applet;
        this.user   = user;
        addMouseListener(this);
        addKeyListener(this);
    }



    /**
     *  Create the gui
     *
     * @return _more_
     */
    Component doMakeContents() {
        if (contents == null) {
            BorderPanel p = new BorderPanel(GuiUtils.BORDER_SUNKEN);
            p.setLayout(new BorderLayout());
            p.add("Center", this);
            contents = GuiUtils.doLayout(new Component[] { p,
                    getVScroll(100, 500) }, 2, GuiUtils.DS_YN,
                                            GuiUtils.DS_YN);
        }
        return contents;

    }

    /**
     * _more_
     *
     * @param s _more_
     */
    public void addUser(ChatUser s) {
        users.addElement(s);
        repaint();
    }


    /**
     * _more_
     */
    public void clear() {
        users.removeAllElements();
        repaint();
    }


    /**
     * _more_
     *
     * @param idx _more_
     */
    public void removeUserAt(int idx) {
        users.removeElementAt(idx);
        repaint();
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintInner(Graphics g) {
        try {
            Rectangle b = getBounds();
            g.setColor(Color.white);
            g.fillRect(hTrans, vTrans, b.width, b.height);
            g.setColor(Color.black);
            FontMetrics fm           = g.getFontMetrics();
            int         ascent       = fm.getMaxAscent();
            int         newRowHeight = fm.getMaxDescent() + ascent + 2;
            if (rowHeight != newRowHeight) {
                rowHeight = newRowHeight;
                getVScroll(1, 1).setBlockIncrement(rowHeight);
            }

            int leftMargin = 2;
            int width      = b.width - 4;
            int offset     = 8 + leftMargin;
            int y          = leftMargin;
            for (int i = 0; i < users.size(); i++) {
                ChatUser s = (ChatUser) users.elementAt(i);
                if (i == selectedIdx) {
                    g.fillRect(offset + 2, y, width, rowHeight);
                    g.setColor(Color.white);
                    g.drawString(s.getName(), offset + 4, y + ascent);
                    g.setColor(Color.black);
                } else {
                    g.drawString(s.getName(), offset + 4, y + ascent);
                }

                if (s.equals(user)) {
                    int   my = y + (rowHeight) / 2;
                    int[] xs = { 0, offset, 0 };
                    int[] ys = { my - offset / 2, my, my + offset / 2 };
                    g.setColor(Color.gray);
                    g.fillPolygon(xs, ys, xs.length);
                    g.setColor(Color.black);
                } else if (s.getIgnored()) {
                    int by = y + (rowHeight - offset) / 2;
                    g.drawLine(leftMargin, by, offset, by + offset);
                    g.drawLine(offset, by, leftMargin, by + offset);
                }
                y = y + rowHeight;
            }
            GuiUtils.paintBorder(g, GuiUtils.BORDER_SUNKEN, b.width,
                                 b.height);
            g.setColor(Color.black);
            g.drawLine(offset + 1, 0, offset + 1, 2000);
        } catch (Throwable exc) {
            System.err.println("CAUGHT: " + exc);
        }
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseClicked(MouseEvent e) {}

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
    public void mouseReleased(MouseEvent e) {}

    /**
     *  Select the clicked on user. If it is button 2 or 3
     *  and there is a selected user then popup a menu.
     *
     * @param e _more_
     */
    public void mousePressed(MouseEvent e) {
        int x = translateInputX(e.getX());
        int y = translateInputY(e.getY());
        if (rowHeight > 0) {
            int row = y / rowHeight;
            if (row < users.size()) {
                if (selectedIdx != row) {
                    selectedIdx = row;
                    repaint();
                }
            }
        }

        ChatUser selectedUser = getSelectedItem();
        if (((e.getModifiers() & e.BUTTON1_MASK) == 0)
                && (selectedUser != null)) {
            final JPopupMenu pm = new JPopupMenu();
            final int        ex = e.getX();
            final int        ey = e.getY();
            add(pm);
            if (selectedUser.getIgnored()) {
                pm.add(applet.makeMenuItem("Don't ignore",
                                           ChatApplet.CMD_USERS_IGNORE));
            } else {
                pm.add(applet.makeMenuItem("Ignore",
                                           ChatApplet.CMD_USERS_IGNORE));
            }

            pm.add(applet.makeMenuItem("Private conversation",
                                       ChatApplet.CMD_USERS_PRIVATE));
            //Put the menu popup in a thread so this list will repaint if a user is
            //newly selected
            Thread t = new Thread() {
                public void run() {
                    pm.show(UserList.this, ex, ey);
                }
            };
            t.start();
        }
    }


    /**
     *  Return the currently selected user (or null).
     *
     * @return _more_
     */
    public ChatUser getSelectedItem() {
        if ((selectedIdx < 0) || (selectedIdx >= users.size())) {
            return null;
        }
        return (ChatUser) users.elementAt(selectedIdx);
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
     * _more_
     *
     * @param evt _more_
     */
    public void keyPressed(KeyEvent evt) {
        if (evt.isControlDown()) {
            char key = (char) (evt.getKeyChar() + 'a' - 1);
            if (key == 'p') {
                applet.createPrivateMessage(getSelectedItem());
            } else if (key == 'i') {
                toggleIgnore();
            }
        }
    }


    /**
     *  If there is a currently selected user then have the ChatApplet toggle
     *  whether it is ignoring the user or not.
     */
    public void toggleIgnore() {
        ChatUser user = getSelectedItem();
        if (user != null) {
            applet.setIgnoreUser(user, !user.getIgnored());
            repaint();
        }
    }
}

