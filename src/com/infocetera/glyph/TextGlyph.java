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



package com.infocetera.glyph;


import com.infocetera.util.*;


import java.awt.*;
import java.awt.event.KeyEvent;

import java.util.Vector;



/**
 * Class TextGlyph _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class TextGlyph extends RectangleGlyph {

    /** _more_ */
    protected ScrollCanvas canvas;

    /** _more_ */
    protected String text;

    /** _more_ */
    private Font myFont = new Font("Dialog", 0, 8);

    /** _more_ */
    private Font fixedFont;

    /** _more_ */
    boolean inserting = false;


    /** _more_ */
    Vector lines;

    /**
     * _more_
     *
     * @param canvas _more_
     * @param x _more_
     * @param y _more_
     * @param t _more_
     */
    public TextGlyph(ScrollCanvas canvas, int x, int y, String t) {
        this(canvas, x, y, t, null);
    }

    /**
     * _more_
     *
     * @param canvas _more_
     * @param x _more_
     * @param y _more_
     * @param t _more_
     * @param font _more_
     */
    public TextGlyph(ScrollCanvas canvas, int x, int y, String t, Font font) {
        this(Glyph.TEXT, canvas, x, y, t, font);
    }

    /**
     * _more_
     *
     * @param type _more_
     * @param canvas _more_
     * @param x _more_
     * @param y _more_
     * @param t _more_
     * @param font _more_
     */
    public TextGlyph(String type, ScrollCanvas canvas, int x, int y,
                     String t, Font font) {
        super(type, x, y, 1, 1);
        this.canvas = canvas;
        fixedFont   = font;
        this.text   = t;
        textChanged();
        setFilled(true);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getBoundsFromChildren() {
        return false;
    }

    /**
     * _more_
     *
     * @param b _more_
     */
    public void setInserting(boolean b) {
        inserting = b;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getText() {
        return text;
    }

    /**
     * _more_
     */
    protected void textChanged() {
        lines = GuiUtils.split(text, new String[] { "\n" });
        int         numRows = 0;
        FontMetrics fm      = canvas.getFontMetrics(getFontToUse());
        bounds.width = 0;
        for (int i = 0; i < lines.size(); i++) {
            numRows++;
            bounds.width =
                Math.max(fm.stringWidth(lines.elementAt(i).toString()),
                         bounds.width);
        }
        bounds.height = (fm.getMaxDescent() + fm.getMaxAscent()) * numRows;
        bounds.width  += 2;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return text;
    }

    /**
     * _more_
     *
     * @param t _more_
     */
    public void setText(String t) {
        text = t;
        textChanged();
    }

    /**
     * _more_
     *
     * @param c _more_
     * @param keyCode _more_
     */
    public void append(char c, int keyCode) {
        if (c == '\b') {
            if (text.length() > 0) {
                setText(text.substring(0, text.length() - 1));
            }
        } else if (keyCode == KeyEvent.VK_LEFT) {}
        else if (keyCode == KeyEvent.VK_RIGHT) {}
        else if (keyCode == KeyEvent.VK_UP) {}
        else if (keyCode == KeyEvent.VK_DOWN) {}
        else if (keyCode == KeyEvent.VK_PAGE_UP) {}
        else if (keyCode == KeyEvent.VK_PAGE_DOWN) {}
        else if (keyCode == KeyEvent.VK_HOME) {}
        else if (keyCode == KeyEvent.VK_END) {}
        else {
            setText(text + c);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Font getFontToUse() {
        Font theFont = fixedFont;
        if (theFont == null) {
            if (myFont.getSize() != 8 + getWidth() * 3) {
                myFont = new Font("Dialog", Font.PLAIN, 8 + getWidth() * 3);
                textChanged();
            }
            theFont = myFont;
        }

        return theFont;
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    public void paint(Graphics g, ScrollCanvas c) {
        paintText(g, c);
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    protected void paintText(Graphics g, ScrollCanvas c) {
        Font theFont = getFontToUse();
        /*
        if (c.scale != 1.0) {
          theFont = new Font ("Dialog", Font.PLAIN, (int) (c.scale*theFont.getSize ()));
          }*/
        g.setFont(theFont);

        FontMetrics fm         = g.getFontMetrics();
        Rectangle   sr         = c.scaleRect(bounds);
        int         lineHeight = fm.getMaxDescent() + fm.getMaxAscent();
        int         lineY      = sr.y + lineHeight;
        int         lineWidth  = 0;

        if (getBgColor() != null) {
            g.setColor(getBgColor());
            g.fillRect(sr.x, sr.y, sr.width, sr.height);
        }
        g.setColor(getColor());

        int maxWidth = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = (String) lines.elementAt(i);
            //      totalLength += lineLength;
            g.drawString(line, sr.x, lineY);
            lineWidth = fm.stringWidth(line);
            maxWidth  = Math.max(lineWidth, maxWidth);
            if (underline) {
                g.setColor(Color.blue);
                g.drawLine(sr.x, lineY + 1, sr.x + lineWidth, lineY + 1);
                g.setColor(getColor());
            }
            lineY += lineHeight;
        }

        /*
        if (c.scale != 1.0) {
          bounds.width = c.inverseScale (maxWidth);
          bounds.height = c.inverseScale (lineHeight*lines.size ());
          }*/


        if (inserting) {
            int caretBottom = lineY - lineHeight + 1;
            int caretTop    = caretBottom - lineHeight;
            drawCaret(g, caretTop, caretBottom, sr.x + lineWidth);
        }
    }

    /**
     * _more_
     *
     * @param g _more_
     * @param caretTop _more_
     * @param caretBottom _more_
     * @param x _more_
     */
    protected void drawCaret(Graphics g, int caretTop, int caretBottom,
                             int x) {
        g.setColor(Color.red);
        g.drawLine(x, caretBottom, x, caretTop);
        g.drawLine(x - 2, caretTop, x + 2, caretTop);
        g.drawLine(x - 2, caretBottom, x + 2, caretBottom);
    }

    /**
     * _more_
     *
     * @param match _more_
     *
     * @return _more_
     */
    public String getAttrs(String match) {
        return super.getAttrs(match) + makeAttr(match, ATTR_TEXT, text);
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     */
    public void setAttr(String name, String value) {
        if (ATTR_TEXT.equals(name)) {
            setText(value);
        } else {
            super.setAttr(name, value);
        }
    }




}
