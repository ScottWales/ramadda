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

import java.net.URL;

import java.util.Hashtable;
import java.util.Vector;


/**
 *  This is a  canvas class that supports a set of avatars that
 *  represent Users in a chat session.
 *  It is part of a chat/whiteboard applet. It uses the jdk1.0 event model
 *  because the early 4.x browsers did not support full jdk1.1
 */


public class AvatarCanvas extends EditCanvas {

    /**
     * Each user in this chat session  has their own Glyph for the
     * avatar view of the chat. The Hashtable userGlyphs maps user name to Glyph
     */
    Hashtable ugs = new Hashtable();

    /** _more_ */
    int lastX = 20;

    /** _more_ */
    int lastY = 10;


    /**
     * _more_
     *
     * @param applet _more_
     */
    public AvatarCanvas(ChatApplet applet) {
        super(applet);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Container doMakeContents() {
        return GuiUtils.doLayout(new Component[] { this,
                getVScroll(300, 3000), getHScroll(500, 5000) }, 2,
                GuiUtils.DS_YN, GuiUtils.DS_YN);
    }


    /**
     *  Create a new UserGlyph if not created already for user name.
     *  Return the UserGlyph
     *
     * @param user _more_
     *
     * @return _more_
     */
    public UserGlyph addUser(ChatUser user) {
        UserGlyph ug = (UserGlyph) ugs.get(user);
        if (ug == null) {
            ug = new UserGlyph(this, lastX, lastY, user);
            Image image = socketApplet.getImage(user.getIconUrl());
            if (image == null) {
                image = socketApplet.getImage(
                    "resource:/com/infocetera/images/smile1.gif");
            }
            ug.setImage(image);
            addGlyph(ug);
            ugs.put(user, ug);
            lastX += ug.getBounds().width + 20;
            if (lastX > 400) {
                lastX = 20;
                lastY += ug.getBounds().height + 20;
            }
        }
        return ug;
    }

    /**
     * _more_
     *
     * @param user _more_
     */
    public void removeUser(ChatUser user) {
        UserGlyph ug = (UserGlyph) ugs.remove(user);
        if (ug != null) {
            removeGlyph(ug);
        }
    }

    /**
     * _more_
     *
     * @param user _more_
     * @param ignore _more_
     */
    public void setIgnore(ChatUser user, boolean ignore) {
        UserGlyph ug = addUser(user);
        ug.setIgnore(ignore);
        repaint(ug);
    }

    /**
     * _more_
     */
    public void clear() {
        for (int i = 0; i < glyphs.size(); i++) {
            Glyph glyph = (Glyph) glyphs.elementAt(i);
            if ( !(glyph instanceof UserGlyph)) {
                continue;
            }
            ((UserGlyph) glyph).clearMessages();
        }
        repaint();

    }

    /**
     * _more_
     *
     * @param user _more_
     * @param msg _more_
     */
    public void message(ChatUser user, String msg) {
        if (user.getName().equals("System")) {
            return;
        }
        UserGlyph ug = addUser(user);
        ug.addMessage(msg);
        repaint(ug);
    }

    /**
     * _more_
     *
     * @param evt _more_
     */
    public void keyPressed(KeyEvent evt) {
        if (haveCommand()) {
            super.keyPressed(evt);
            return;
        }
        int key = evt.getKeyCode();
        //    String text = KeyEvent.getkeyText (key);

        Object ug;
        boolean down = ((key == KeyEvent.VK_KP_DOWN)
                        || (key == KeyEvent.VK_DOWN));
        boolean up = ((key == KeyEvent.VK_KP_UP) || (key == KeyEvent.VK_UP));
        boolean pgdown = (key == KeyEvent.VK_PAGE_DOWN);
        boolean pgup   = (key == KeyEvent.VK_PAGE_UP);

        boolean home   = (key == KeyEvent.VK_HOME);
        boolean end    = (key == KeyEvent.VK_END);


        if (up || down || home || end || pgup || pgdown) {
            for (int i = 0; i < selectionSet.size(); i++) {
                ug = selectionSet.elementAt(i);
                if (ug instanceof UserGlyph) {
                    if (down) {
                        ((UserGlyph) ug).goDown();
                    } else if (up) {
                        ((UserGlyph) ug).goUp();
                    } else if (home) {
                        ((UserGlyph) ug).goHome();
                    } else if (end) {
                        ((UserGlyph) ug).goEnd();
                    } else if (pgup) {
                        ((UserGlyph) ug).goPageUp();
                    } else if (pgdown) {
                        ((UserGlyph) ug).goPageDown();
                    }
                }
            }
            repaint();
        }
        super.keyPressed(evt);

    }
}

