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

