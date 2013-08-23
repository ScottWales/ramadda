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

package com.infocetera.chat;


import com.infocetera.glyph.*;

import com.infocetera.util.*;


import java.awt.*;
import java.awt.event.*;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;




/**
 * Class PolyGlyphCreator _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class PolyGlyphCreator extends CanvasCommand {

    /** _more_ */
    Glyph theGlyph;

    /** _more_ */
    int lastx;

    /** _more_ */
    int lasty;

    /**
     * _more_
     *
     * @param canvas _more_
     * @param firstEvent _more_
     * @param theGlyph _more_
     * @param x _more_
     * @param y _more_
     */
    public PolyGlyphCreator(EditCanvas canvas, AWTEvent firstEvent,
                            PolyGlyph theGlyph, int x, int y) {
        super(canvas, firstEvent, x, y);
        this.theGlyph = theGlyph;
        lastx         = x;
        lasty         = y;
    }

    /** _more_ */
    boolean shiftDown = false;


    /**
     * _more_
     *
     * @return _more_
     */
    public Cursor getCursor() {
        return DisplayCanvas.HAND_CURSOR;
    }

    /**
     * _more_
     *
     * @param e _more_
     *
     * @return _more_
     */
    public CanvasCommand doKeyReleased(KeyEvent e) {
        shiftDown = false;

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
        shiftDown = (e.getKeyCode() == KeyEvent.VK_SHIFT);

        return this;
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
        if (theGlyph == null) {
            return null;
        }

        if (Glyph.distance(lastx, lasty, x, y) > 3.0) {
            //Math.abs(lastx-x)>2 || Math.abs(lasty-y)>2) {      

            lastx = x;
            lasty = y;
            ((PolyGlyph) theGlyph).addPoint(x, y);
            canvas.notifyGlyphMoved(theGlyph);
            canvas.repaint(theGlyph);
        }

        return this;
    }

    /**
     * _more_
     */
    protected void doComplete() {
        canvas.notifyGlyphCreateComplete(theGlyph, true);
    }

}
