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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;



/**
 * Class TextCanvas _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class TextCanvas extends DisplayCanvas implements MouseListener,
        MouseMotionListener {

    /** _more_ */
    private Glyph nearestGlyph = null;

    /** _more_ */
    private boolean mouseDown = false;

    /** _more_ */
    private boolean havePainted = false;

    /** _more_ */
    private Container contents;


    /** _more_ */
    public static final int LEFTMARGIN = 2;

    /** _more_ */
    public static final int TOPMARGIN = 2;

    /** _more_ */
    public static final int TEXTLEFTMARGIN = 4;

    /**
     *  This is the width of the left column showing user names
     */
    private int offset = -1;

    /**
     *  This is the beginning of the text area (offset+TEXTLEFTMARGIN)
     */
    private int baseTextOffset = 0;

    /** _more_ */
    private int crntY = TOPMARGIN;

    /** _more_ */
    private Font defaultFont = new Font("Dialog", 0, 10);

    /** _more_ */
    Color line1color = null;

    /** _more_ */
    Color line2color = null;


    /**
     *  List of ChatMessage-s
     */
    Vector msgs = new Vector();


    /** _more_ */
    ChatApplet applet;

    /** _more_ */
    ChatUser crntUser;


    /**
     * _more_
     *
     * @param applet _more_
     * @param user _more_
     */
    public TextCanvas(ChatApplet applet, ChatUser user) {
        super(applet);
        this.crntUser = user;
        this.applet   = applet;
        addMouseListener(this);
        addMouseMotionListener(this);
    }

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
    public void mouseMoved(MouseEvent e) {
        int   x               = translateInputX(e.getX());
        int   y               = translateInputY(e.getY());
        Glyph oldNearestGlyph = nearestGlyph;
        nearestGlyph = null;
        Glyph newNearestGlyph = findClosest(x, y);
        if (newNearestGlyph == null) {
            if (oldNearestGlyph != null) {
                setCursor(DisplayCanvas.DEFAULT_CURSOR);
            }
            return;
        }
        String url = newNearestGlyph.url;
        if (url == null) {
            return;
        }
        nearestGlyph = newNearestGlyph;
        setCursor(DisplayCanvas.HAND_CURSOR);
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public Glyph findClosest(int x, int y) {
        Glyph nearest = null;
        for (int i = 0; i < msgs.size(); i++) {
            ChatMessage cm = (ChatMessage) msgs.elementAt(i);
            if (y < cm.getTop()) {
                continue;
            }
            if (y > cm.getBottom()) {
                continue;
            }
            nearest = findGlyph(cm.children, x, y, 2.0);
            break;
        }
        return nearest;
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseClicked(MouseEvent e) {
        int x = translateInputX(e.getX());
        int y = translateInputY(e.getY());
        nearestGlyph = findClosest(x, y);
        if (nearestGlyph == null) {
            return;
        }
        String url = nearestGlyph.url;

        if (url == null) {
            nearestGlyph = null;
            return;
        }
        applet.showUrl(url, "CHAT.URL");
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
    public void mouseReleased(MouseEvent e) {
        mouseDown = false;
        if (nearestGlyph != null) {
            repaint();
        }
    }

    /**
     *  Select the clicked on user. If it is button 2 or 3
     *  and there is a selected user then popup a menu.
     *
     * @param e _more_
     */
    public void mousePressed(MouseEvent e) {
        int x = translateInputX(e.getX());
        int y = translateInputY(e.getY());
        mouseDown = true;
        if (nearestGlyph != null) {
            repaint();
        }
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getTextXml() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < msgs.size(); i++) {
            ChatMessage cm = (ChatMessage) msgs.elementAt(i);
            if (cm.isFromChat()) {
                continue;
            }
            sb.append(
                "<message "
                + ChatApplet.attr(ChatApplet.ATTR_TYPE, ChatApplet.MSG_TEXT)
                + ChatApplet.attr(ChatApplet.ATTR_FROM, cm.getUserName())
                + ">" + XmlNode.encode(cm.getText()) + "</message>");
        }
        return sb.toString();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < msgs.size(); i++) {
            ChatMessage cm = (ChatMessage) msgs.elementAt(i);
            sb.append(cm.getFormattedName() + " ");
            sb.append(cm.getText());
            sb.append("\n");
        }
        return sb.toString();
    }



    /**
     * _more_
     */
    private void messagesChanged() {
        if ( !havePainted) {
            return;
        }
        JScrollBar vScroll = getVScroll(1, 1);
        int        max     = crntY + 2;
        if (vScroll.getMaximum() != max) {
            Rectangle b = getBounds();
            vScroll.setValues(max - b.height, b.height, 0, max);
        }
        repaint();
    }

    /**
     * _more_
     *
     * @param cm _more_
     * @param fm _more_
     *
     * @return _more_
     */
    private boolean checkOffset(ChatMessage cm, FontMetrics fm) {
        int senderWidth = fm.stringWidth(cm.getFrom().getName()) + 4;
        if (senderWidth > offset) {
            offset         = senderWidth;
            baseTextOffset = offset + TEXTLEFTMARGIN;
            relayoutAll();
            return true;
        }
        return false;
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void elementChanged(Object g) {
        super.elementChanged(g);
        if ( !(g instanceof ChatMessage)) {
            return;
        }
        relayoutAll();
    }

    /**
     * _more_
     */
    private void relayoutAll() {
        crntY = TOPMARGIN;
        FontMetrics defaultFm = getFontMetrics(defaultFont);
        for (int i = 0; i < msgs.size(); i++) {
            layoutChatMessage((ChatMessage) msgs.elementAt(i));
        }
        messagesChanged();
    }


    /**
     * _more_
     *
     * @param cm _more_
     */
    private void layoutChatMessage(ChatMessage cm) {
        //Lay the cm out - the 4 says to pad the bottom by 4
        cm.doLayout(baseTextOffset, crntY,
                    getBounds().width - baseTextOffset - TEXTLEFTMARGIN, 4);
        crntY = cm.getBottom();
    }

    /**
     * _more_
     *
     * @param from _more_
     * @param msg _more_
     */
    public void addMessage(ChatUser from, String msg) {
        ChatMessage cm = new ChatMessage(this, from, msg);
        msgs.addElement(cm);
        if ( !checkOffset(cm, getFontMetrics(defaultFont))) {
            layoutChatMessage(cm);
        }
        messagesChanged();
    }


    /**
     * _more_
     */
    public void clear() {
        crntY = TOPMARGIN;
        msgs  = new Vector();
        messagesChanged();
    }

    /**
     *  Create the gui
     *
     * @return _more_
     */
    public Container doMakeContents() {
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
     * @param g _more_
     */
    public void paintInner(Graphics g) {
        //We might have had some messages added before we are displayed
        //If so we want to call messagesChanged () to set the vScroll
        //and repaint
        if ( !havePainted) {
            havePainted = true;
            if (msgs.size() > 0) {
                messagesChanged();
                return;
            }
        }

        Rectangle b    = getBounds();
        int       yMin = vTrans;
        int       yMax = yMin + b.height;
        g.setFont(defaultFont);
        FontMetrics fm = g.getFontMetrics();
        if (offset < 0) {
            offset         = fm.stringWidth("1234") + 4;
            baseTextOffset = offset + TEXTLEFTMARGIN;
        }

        int        ascent       = fm.getMaxAscent();
        int        newRowHeight = fm.getMaxDescent() + ascent + 4;
        JScrollBar vScroll      = getVScroll(1, 1);

        //    if (baseRowHeight != newRowHeight) {
        //      baseRowHeight = newRowHeight;
        //      vScroll.setBlockIncrement (baseRowHeight);
        //    }

        Color bg = getBackground();
        if (bg != null) {
            g.setColor(bg);
            g.fillRect(hTrans, vTrans, b.width, b.height);
            g.setColor(Color.black);
        }

        for (int i = 0; i < msgs.size(); i++) {
            ChatMessage cm = (ChatMessage) msgs.elementAt(i);
            if (cm.getBottom() < yMin) {
                continue;
            }
            if (cm.getTop() > yMax) {
                break;
            }

            Color bgColor = null;
            if (cm.getFrom().getIgnored()) {
                bgColor = Color.lightGray;
            }
            if (bgColor != null) {
                g.setColor(bgColor);
                //      g.fillRect (0, cm.getTop (), offset,  baseRowHeight);
            }
            g.setColor(Color.black);
            g.setFont(defaultFont);
            g.drawString(cm.getUserName(), LEFTMARGIN + 2,
                         cm.getTop() + fm.getMaxAscent());


            cm.paint(g, this);


            int tmpY = cm.getBottom();
            if (line1color != null) {
                g.setColor(line1color);
                g.drawLine(baseTextOffset - TEXTLEFTMARGIN + 1, tmpY,
                           b.width, tmpY);
                tmpY++;
            }
            if (line2color != null) {
                g.setColor(line2color);
                g.drawLine(baseTextOffset - TEXTLEFTMARGIN + 1, tmpY,
                           b.width, tmpY);
            }

        }

        if ((nearestGlyph != null) && mouseDown) {
            g.setColor(Color.lightGray);
            Rectangle r = new Rectangle(nearestGlyph.getBounds());
            if (nearestGlyph instanceof TextGlyph) {
                r.y      += 2;
                r.height -= 1;
            }
            g.drawRect(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
        }

        GuiUtils.paintBorder(g, GuiUtils.BORDER_SUNKEN, b.width, b.height);
        g.setColor(Color.gray);
        g.drawLine(offset + 1, 0, offset + 1, 2000);

    }









}

