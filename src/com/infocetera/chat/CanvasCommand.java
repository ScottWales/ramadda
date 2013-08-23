/*
* Copyright 2008-2013 Geode Systems LLC
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
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


/**
 * Class CanvasCommand _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class CanvasCommand {

    /** _more_ */
    public EditCanvas canvas;

    /** _more_ */
    public AWTEvent firstEvent;

    /** _more_ */
    public Point originalPoint;

    /** _more_ */
    public static boolean debug = false;


    /**
     * _more_
     *
     * @param canvas _more_
     * @param firstEvent _more_
     * @param x _more_
     * @param y _more_
     */
    public CanvasCommand(EditCanvas canvas, AWTEvent firstEvent, int x,
                         int y) {
        this.canvas        = canvas;
        this.firstEvent    = firstEvent;
        this.originalPoint = new Point(x, y);
    }

    /**
     * _more_
     *
     * @param msg _more_
     */
    public void debug(String msg) {
        if (debug) {
            System.err.println(msg);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return " ";
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isAtomic() {
        return false;
    }

    /**
     * _more_
     */
    public void doAbort() {}

    /**
     * _more_
     */
    protected void doComplete() {}

    /**
     * _more_
     *
     * @return _more_
     */
    public Cursor getCursor() {
        return null;
    }


    /**
     * _more_
     *
     * @param e _more_
     *
     * @return _more_
     */
    public CanvasCommand doFocusGained(FocusEvent e) {
        debug("CanvasCommand.doFocusGained");

        //    doComplete ();
        return null;
    }

    /**
     * _more_
     *
     * @param e _more_
     *
     * @return _more_
     */
    public CanvasCommand doFocusLost(FocusEvent e) {
        debug("CanvasCommand.doFocusLost");

        //    doComplete ();
        return null;
    }



    /**
     * _more_
     *
     * @param graphics _more_
     */
    public void doPaint(Graphics graphics) {}


    /**
     * _more_
     *
     * @param e _more_
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public CanvasCommand doMouseClicked(MouseEvent e, int x, int y) {
        debug("CanvasCommand.doMouseClicked");

        //    doComplete ();
        return null;
    }


    /**
     * _more_
     *
     * @param e _more_
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public CanvasCommand doMousePressed(MouseEvent e, int x, int y) {
        debug("CanvasCommand.doMousePressed");

        //    doComplete ();
        return null;
    }


    /**
     * _more_
     *
     * @param e _more_
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public CanvasCommand doMouseReleased(MouseEvent e, int x, int y) {
        debug("CanvasCommand.doMouseReleased");

        return null;
    }


    /**
     * _more_
     *
     * @param e _more_
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public CanvasCommand doMouseDragged(MouseEvent e, int x, int y) {
        debug("CanvasCommand.doMouseDragged");

        //    doComplete();
        return null;
    }

    /**
     * _more_
     *
     * @param e _more_
     *
     * @return _more_
     */
    public CanvasCommand doKeyReleased(KeyEvent e) {
        return this;
    }

    /**
     * _more_
     *
     * @param e _more_
     *
     * @return _more_
     */
    public CanvasCommand doKeyPress(KeyEvent e) {
        debug("CanvasCommand.doKeyPress");

        //    doComplete ();
        return null;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return getDescription();
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void paint(Graphics g) {}

    /**
     * _more_
     */
    public void repaint() {
        canvas.repaint();
    }


    /**
     * _more_
     *
     * @param r _more_
     */
    public void repaint(Rectangle r) {
        canvas.repaint(r.x - 2, r.y - 2, r.width + 4, r.height + 4);
    }

}
