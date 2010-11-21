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

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.*;


/**
 * Class WhiteBoardCanvas _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class WhiteBoardCanvas extends EditCanvas {

    /** _more_ */
    public static final String CMD_SHARE = "sendstate";

    /** _more_ */
    public static final String CMD_CURSOR_SHARE = "cursor.share";

    /** _more_ */
    public static final String CMD_CURSOR_ACCEPT = "cursor.accept";

    /** _more_ */
    public static final String CMD_SENDLIVE = "sendlive";

    /** _more_ */
    boolean editMode;


    /** _more_ */
    boolean shareCursor = false;

    /** _more_ */
    boolean acceptSharedCursor = false;

    /** _more_ */
    boolean sendLive = false;


    /**
     *  Tracks what glyph create messages we have sent
     */
    Hashtable glyphCreateSent = new Hashtable();



    /** _more_ */
    Image upCursor;

    /** _more_ */
    Image downCursor;

    /** _more_ */
    Point cursor = new Point(-100, -100);

    /** _more_ */
    boolean cursorDown = false;


    /** _more_ */
    public ChatApplet chatApplet;

    /**
     * _more_
     *
     * @param applet _more_
     */
    public WhiteBoardCanvas(ChatApplet applet) {
        super(applet);
        this.chatApplet = applet;
        upCursor = chatApplet.getImage(
            "resource:/com/infocetera/images/shared_up.gif");
        downCursor = chatApplet.getImage(
            "resource:/com/infocetera/images/shared_down.gif");
    }


    /**
     *  Write the message (or messages) to the server
     *
     * @param message _more_
     */
    public void write(String message) {
        //    System.err.println (message);
        //    ((new IllegalArgumentException (""))).printStackTrace ();
        chatApplet.writeGfx(message);
    }

    /**
     *  Create the whiteboard command with the given command name
     *  and attributes and write it to the server
     *
     * @param commandName _more_
     * @param attributes _more_
     */
    public void write(String commandName, String attributes) {
        write(makeCommandTag(commandName, attributes));
    }

    /**
     *  Write the glyph create command to the server. If checkIfSent is true
     *  then only send the write command if we have not already written this glyph.
     *
     * @param theGlyph _more_
     * @param checkIfSent _more_
     */
    public void writeCreate(Glyph theGlyph, boolean checkIfSent) {
        //Have we already sent it
        if (checkIfSent && (glyphCreateSent.get(theGlyph.getId()) != null)) {
            return;
        }
        glyphCreateSent.put(theGlyph.getId(), theGlyph.getId());

        write(CMD_CREATE,
              XmlNode.attr(ATTR_ID, theGlyph.getId())
              + XmlNode.attr(ATTR_TYPE, theGlyph.getType())
              + theGlyph.getCreateString());
    }

    /**
     *  Go through all of the Glyph-s and send the create command
     *  to the server. Used to update other users.
     */
    public void shareGlyphs() {
        for (int i = 0; i < glyphs.size(); i++) {
            writeCreate((Glyph) glyphs.elementAt(i), false);
        }
    }



    /**
     * _more_
     *
     * @return _more_
     */
    String getGlyphId() {
        if (uniqueifier == null) {
            uniqueifier = "" + System.currentTimeMillis();
        }
        return "ID" + chatApplet.myid + "-" + uniqueifier + "-"
               + (glyphCnt++);
    }




    /**
     * _more_
     *
     * @param glyph _more_
     * @param fromCommandProcessor _more_
     */
    public void moveToFront(Glyph glyph, boolean fromCommandProcessor) {
        super.moveToFront(glyph, fromCommandProcessor);
        if ( !fromCommandProcessor) {
            write(CMD_TOFRONT, XmlNode.attr(ATTR_ID, glyph.getId()));
        }
    }

    /**
     * _more_
     *
     * @param glyph _more_
     * @param fromCommandProcessor _more_
     */
    public void moveToBack(Glyph glyph, boolean fromCommandProcessor) {
        super.moveToBack(glyph, fromCommandProcessor);
        if ( !fromCommandProcessor) {
            write(CMD_TOBACK, XmlNode.attr(ATTR_ID, glyph.getId()));
        }
    }


    /**
     *  Handle gui commands
     *
     * @param event _more_
     */
    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        Object source = event.getSource();



        if (action.equals(CMD_CURSOR_SHARE)) {
            if (source instanceof JCheckBox) {
                setShareCursor(((JCheckBox) source).isSelected());
            }
        } else if (action.equals(CMD_SENDLIVE)) {
            if (source instanceof JCheckBox) {
                setSendLive(((JCheckBox) source).isSelected());
            }
        } else if (action.equals(CMD_CURSOR_ACCEPT)) {
            if (source instanceof JCheckBox) {
                setAcceptSharedCursor(((JCheckBox) source).isSelected());
            }
        } else if (action.equals(CMD_SHARE)) {
            shareGlyphs();
            if (backgroundImageUrl != null) {
                write(CMD_BACKGROUNDIMAGE,
                      XmlNode.attr("url", backgroundImageUrl));
            }
        } else {
            super.actionPerformed(event);
        }
    }


    /**
     *  Handle command from the chat server
     *
     * @param commandName _more_
     * @param node _more_
     */
    public void processCommand(String commandName, XmlNode node) {
        if (commandName.equals(CMD_CURSORPOS)) {
            if (getAcceptSharedCursor()) {
                int x = node.getAttribute(ATTR_X, cursor.x);
                int y = node.getAttribute(ATTR_Y, cursor.y);
                //      repaint (new Rectangle(cursor.x-2,cursor.y-2,20,20));
                repaint();
                cursor.x = x;
                cursor.y = y;
                repaint();
                //      repaint(new Rectangle(cursor.x-2,cursor.y-2,20,20));      
            }
        } else if (commandName.equals(CMD_CURSORDOWN)) {
            if (getAcceptSharedCursor()) {
                cursorDown = true;
                repaint();
                //      repaint (new Rectangle(cursor.x-2,cursor.y-2,20,20));           
            }
        } else if (commandName.equals(CMD_CURSORUP)) {
            if (getAcceptSharedCursor()) {
                cursorDown = false;
                repaint();
                //      repaint (new Rectangle(cursor.x-2,cursor.y-2,20,20));           
            }
        } else {
            super.processCommand(commandName, node);
        }
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public String getSelectedFile() {
        return chatApplet.getSelectedFile();
    }




    /**
     *  Paint this canvas. The base class does most of the work. This method
     *  just draws the cursor image when sharing the cursor.
     *
     * @param g _more_
     */
    public void paintInner(Graphics g) {
        super.paintInner(g);
        if (cursor != null) {
            g.setColor(Color.gray);
            if (cursorDown && (downCursor != null)) {
                g.drawImage(downCursor, cursor.x, cursor.y, null, null);
            } else if (upCursor != null) {
                g.drawImage(upCursor, cursor.x, cursor.y, null, null);
            }
        }
    }




    /**
     *  Sometime we should do bulk message sends
     *
     * @param glyphs _more_
     */
    public void notifyGlyphsChanged(Vector glyphs) {
        String message = "";
        for (int i = 0; i < glyphs.size(); i++) {
            Glyph theGlyph = (Glyph) glyphs.elementAt(i);
            //TODO
        }
    }


    /**
     *  Tell other whiteboards that the glyph has changed
     *
     * @param theGlyph _more_
     * @param attr _more_
     */
    public void notifyGlyphChangeDone(Glyph theGlyph, String attr) {
        notifyGlyphChangeDone(theGlyph, attr, false);
    }

    /**
     * _more_
     *
     * @param theGlyph _more_
     * @param attr _more_
     * @param live _more_
     */
    public void notifyGlyphChangeDone(Glyph theGlyph, String attr,
                                      boolean live) {
        if (theGlyph.getPersistent()) {
            write(CMD_CHANGE, XmlNode.attr(ATTR_ID, theGlyph.getId()) + (live
                    ? XmlNode.attr("live", "1")
                    : "") + theGlyph.getAttrs(attr));
        }
    }

    /**
     * Tell other whiteboards that the glyph is in the process of being changed
     *
     * @param theGlyph _more_
     * @param attr _more_
     */
    public void notifyGlyphChanged(Glyph theGlyph, String attr) {
        if (getSendLive()) {
            notifyGlyphChangeDone(theGlyph, attr, true);
        }
    }


    /**
     *  If we wanted to we could send out glyph moved commands
     *  to other whiteboards live as the glyph is being moved.
     *
     * @param theGlyph _more_
     */
    public void notifyGlyphMoved(Glyph theGlyph) {
        if (getSendLive()) {
            notifyGlyphMoveComplete(theGlyph, true);
        }
    }


    /**
     *  Tell other whiteboards that the glyph has moved
     *
     * @param theGlyph _more_
     */
    public void notifyGlyphMoveComplete(Glyph theGlyph) {
        notifyGlyphMoveComplete(theGlyph, false);
    }

    /**
     * _more_
     *
     * @param theGlyph _more_
     * @param live _more_
     */
    public void notifyGlyphMoveComplete(Glyph theGlyph, boolean live) {
        if (theGlyph.getPersistent()) {
            write(CMD_CHANGE, XmlNode.attr(ATTR_ID, theGlyph.getId()) + (live
                    ? XmlNode.attr("live", "1")
                    : "") + theGlyph.getPositionAttr());
        }
    }

    /**
     *  Tell other whiteboards that the glyph has been created
     *
     * @param theGlyph _more_
     * @param diddleSelection _more_
     */
    public void notifyGlyphCreateComplete(Glyph theGlyph,
                                          boolean diddleSelection) {
        super.notifyGlyphCreateComplete(theGlyph, diddleSelection);
        writeCreate(theGlyph, true);
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void addGlyph(Glyph g) {
        super.addGlyph(g);
        if (g.getPersistent()) {
            if (getSendLive()) {
                writeCreate(g, true);
            }
        }
    }

    /**
     *  Are we actively sharing the cursor position with others.
     *  We should actually have cursors associated with users so we could
     *  have more than one on the screen.
     *
     * @return _more_
     */
    public boolean getShareCursor() {
        return shareCursor;
    }

    /**
     * _more_
     *
     * @param v _more_
     */
    public void setShareCursor(boolean v) {
        shareCursor = v;
        if ( !v) {
            //??? DO we need this
            //      write (CMD_CURSORPOS, XmlNode.attr(ATTR_X, "-100") + XmlNode.attr (ATTR_Y, "-100"));
            //      write (CMD_CURSORUP, "");
        }
    }


    /**
     * _more_
     *
     * @param v _more_
     */
    public void setSendLive(boolean v) {
        sendLive = v;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getSendLive() {
        return sendLive;
    }

    /**
     *  Do we accept shared cursors?
     *
     * @return _more_
     */
    public boolean getAcceptSharedCursor() {
        return acceptSharedCursor;
    }

    /**
     * _more_
     *
     * @param v _more_
     */
    public void setAcceptSharedCursor(boolean v) {
        acceptSharedCursor = v;
        if ( !v) {
            cursor.x = -100;
            cursor.y = -100;
            repaint();
        }
    }



    /**
     *  Send cursorup command if we are sharing
     *
     * @param e _more_
     */
    public void mouseReleased(MouseEvent e) {
        if (getShareCursor()) {
            write(CMD_CURSORUP, "");
        }
        super.mouseReleased(e);
    }






    /**
     * _more_
     *
     * @param e _more_
     */
    public void mousePressed(MouseEvent e) {
        if (getShareCursor()) {
            write(CMD_CURSORDOWN, "");
        }
        super.mousePressed(e);
    }



    /**
     *  Send cursorpos commands if sharing
     *
     * @param e _more_
     */
    public void mouseDragged(MouseEvent e) {
        debug("WBC.mouseDragged");
        setHighlight(null);

        int x = e.getX();
        int y = e.getY();
        if (getShareCursor()) {
            int cx = translateInputX(x);
            int cy = translateInputY(y);
            if ((Math.abs(lastx - cx) > 2) || (Math.abs(lasty - cy) > 2)) {
                write(CMD_CURSORPOS,
                      XmlNode.attr(ATTR_X, "" + cx)
                      + XmlNode.attr(ATTR_Y, "" + cy));
                lastx = cx;
                lasty = cy;
            }
        }
        super.mouseDragged(e);

    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseMoved(MouseEvent e) {
        //    debug ("WBC.mouseMoved");
        int x = e.getX();
        int y = e.getY();

        if (getShareCursor()) {
            int cx = translateInputX(x);
            int cy = translateInputY(y);
            if ((Math.abs(lastx - cx) > 2) || (Math.abs(lasty - cy) > 2)) {
                write(CMD_CURSORPOS,
                      XmlNode.attr(ATTR_X, "" + cx)
                      + XmlNode.attr(ATTR_Y, "" + cy));
                lastx = cx;
                lasty = cy;
            }
        }
        super.mouseMoved(e);
    }





    /**
     *  Write the glyph remove command
     *
     * @param g _more_
     */
    public void removeGlyph(Glyph g) {
        super.removeGlyph(g);
        write(CMD_REMOVE, XmlNode.attr(ATTR_ID, g.getId()));
    }
}

