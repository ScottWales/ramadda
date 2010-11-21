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
import java.awt.image.ImageObserver;

import java.util.Vector;


/**
 * Class UserGlyph _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class UserGlyph extends RectangleGlyph implements ImageObserver {

    /** _more_ */
    private int scrollOffset = 0;

    /** _more_ */
    private boolean ignore = false;

    /** _more_ */
    private Vector messages = new Vector();

    /** _more_ */
    private int numRowsToPaint;

    /** _more_ */
    private ChatUser user;

    /** _more_ */
    private Image image = null;

    /** _more_ */
    private ScrollCanvas canvas;

    /** _more_ */
    private int minWidth = 0;

    /** _more_ */
    private int minHeight = 0;

    /** _more_ */
    private int imageCheckCnt = 0;

    /** _more_ */
    private int imageWidth = 0;

    /** _more_ */
    private int imageHeight = 0;


    /**
     * _more_
     *
     * @param canvas _more_
     * @param x _more_
     * @param y _more_
     * @param user _more_
     */
    public UserGlyph(ScrollCanvas canvas, int x, int y, ChatUser user) {
        super("USER", x, y, 150, 60);
        typeName    = "USER";
        this.canvas = canvas;
        this.user   = user;
        setFilled(true);
        setColor(new Color(255, 255, 204));
    }

    /**
     * _more_
     */
    public void goEnd() {
        scrollOffset = 0;
    }

    /**
     * _more_
     */
    public void clearMessages() {
        messages = new Vector();
    }

    /**
     * _more_
     *
     * @param message _more_
     */
    public void addMessage(String message) {
        Vector toks = HtmlGlyph.tokenizeLine(message);
        for (int i = 0; i < toks.size(); i++) {
            Object[] tokArray = (Object[]) toks.elementAt(i);
            String   tok      = (String) tokArray[0];
            if ( !tok.startsWith("<")) {
                messages.addElement(tok);
            }
        }
    }

    /**
     * _more_
     *
     * @param i _more_
     */
    public void setIgnore(boolean i) {
        ignore = i;
    }

    /**
     * _more_
     */
    public void goHome() {
        scrollOffset = messages.size();
    }

    /**
     * _more_
     */
    public void goDown() {
        delta(-1);
    }


    /**
     * _more_
     *
     * @param delta _more_
     */
    public void delta(int delta) {
        scrollOffset += delta;
        if (scrollOffset > (messages.size() - numRowsToPaint)) {
            scrollOffset = (messages.size() - numRowsToPaint);
        }
        if (scrollOffset < 0) {
            scrollOffset = 0;
        }
    }


    /**
     * _more_
     */
    public void goUp() {
        delta(1);
    }

    /**
     * _more_
     */
    public void goPageDown() {
        delta(-numRowsToPaint + 1);
    }

    /**
     * _more_
     */
    public void goPageUp() {
        delta(numRowsToPaint - 1);
    }


    /**
     * _more_
     *
     * @param i _more_
     */
    public void setImage(Image i) {
        image = i;
        checkImageBounds();
    }

    /**
     * _more_
     */
    public void checkImageBounds() {
        if (imageCheckCnt++ > 5) {
            return;
        }

        int ih = image.getHeight(this);
        int iw = image.getWidth(this);
        //    System.err.println("w/h = " + iw + "  " + ih);
        if ((ih != -1) && (iw != -1)) {
            imageWidth  = iw;
            imageHeight = ih;
            if (bounds.height < ih + 50 + 20) {
                bounds.height = ih + 50 + 20;
            }
        }
    }


    /**
     * _more_
     *
     * @param img _more_
     * @param infoflags _more_
     * @param x _more_
     * @param y _more_
     * @param width _more_
     * @param height _more_
     *
     * @return _more_
     */
    public boolean imageUpdate(Image img, int infoflags, int x, int y,
                               int width, int height) {
        checkImageBounds();
        canvas.repaint();
        return true;
    }


    /**
     * We don't want to save this glyph off
     *
     * @return _more_
     */
    public boolean getPersistent() {
        return false;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Rectangle getRepaintBounds() {
        Rectangle b = getBounds();
        return new Rectangle(b.x, b.y, Math.max(b.width, imageWidth),
                             b.height + imageHeight);
    }



    /**
     * _more_
     *
     * @param g _more_
     * @param canvas _more_
     */
    public void paint(Graphics g, ScrollCanvas canvas) {
        Rectangle r    = canvas.scaleRect(bounds);
        int       boxh = r.height - 30;
        int       b    = r.y + boxh;
        int[]     xs   = { r.x + 10, r.x + 20, r.x + 20 };
        int[]     ys   = { b - 2, b + 15, b - 2 };
        Color     c    = (ignore
                          ? Color.lightGray
                          : getColor());

        g.setColor(c);
        g.fillRoundRect(r.x, r.y, r.width, boxh, 10, 10);
        g.setColor(Color.black);
        g.drawRoundRect(r.x, r.y, r.width, boxh, 10, 10);
        g.setColor(c);
        g.fillPolygon(xs, ys, xs.length);
        g.setColor(Color.black);
        g.drawLine(xs[0], ys[0] + 2, xs[1], ys[1]);
        g.drawLine(xs[2], ys[2] + 2, xs[1], ys[1]);
        //    if(canvas.scale !=1.0) {
        //      g.setFont(canvas.getScaledFont(g));
        //    }
        int         nameX  = xs[2] + 3;
        FontMetrics fm     = g.getFontMetrics();
        int         ascent = fm.getMaxAscent();
        if (image != null) {
            g.drawImage(image, xs[1], ys[1], Color.white, null);
            int iw = image.getWidth(null);
            //      nameX = r.x+iw+25;
            minWidth  = iw + 25;
            minHeight = image.getHeight(null) + 10;
        }
        GuiUtils.drawClippedString(g, user.getName(), nameX, b + ascent,
                                   r.width - (nameX - r.x));

        int rowHeight = fm.getMaxDescent() + ascent;
        numRowsToPaint = boxh / rowHeight;
        int startIdx    = 0;
        int numMessages = messages.size();
        if (numRowsToPaint < numMessages) {
            startIdx = numMessages - numRowsToPaint - scrollOffset;
        } else {
            numRowsToPaint = numMessages;
        }

        if (startIdx < 0) {
            startIdx = 0;
        }
        int y = r.y;
        for (int i = 0; i < numRowsToPaint; i++) {
            String msg = (String) messages.elementAt(i + startIdx);
            GuiUtils.drawClippedString(g, msg, r.x + 2, y + ascent,
                                       r.width - 2);
            y = y + rowHeight;
        }

        /*
        Rectangle rpb = getRepaintBounds ();
        g.setColor (Color.black);
        g.drawRect (rpb.x,rpb.y,rpb.width,rpb.height);
        rpb = getBounds ();
        g.setColor (Color.red);
        g.drawRect (rpb.x,rpb.y,rpb.width,rpb.height);
        */

    }


    /**
     * _more_
     */
    public void boundsChanged() {
        if (bounds.width < minWidth) {
            bounds.width = minWidth;
        }
        if (bounds.height < minHeight) {
            bounds.height = minHeight;
        }
    }




}

