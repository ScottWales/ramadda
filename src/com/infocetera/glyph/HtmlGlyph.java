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
 * (C) 1999-2004  WTS Systems, L.L.C.
 *   All rights reserved
 */

package com.infocetera.glyph;


import com.infocetera.util.*;

import java.awt.*;

import java.awt.image.ImageObserver;


import java.util.Hashtable;
import java.util.Vector;


/**
 * Class HtmlGlyph _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class HtmlGlyph extends TextGlyph implements ImageObserver {

    /** _more_          */
    public static final String[] WHITESPACE = { " ", "\n", "\t" };

    /** _more_          */
    public static final int DFLT_FONT_SIZE = 12;

    /** _more_          */
    public static final int LI_SIZE = 7;

    /** _more_          */
    public static final int UL_SIZE = 16;


    /** _more_          */
    public static final String TAG_DIV = "div";

    /** _more_          */
    public static final String TAG_SPAN = "span";

    /** _more_          */
    public static final String TAG_CENTER = "center";

    /** _more_          */
    public static final String TAG_H1 = "h1";

    /** _more_          */
    public static final String TAG_H2 = "h2";

    /** _more_          */
    public static final String TAG_H3 = "h3";

    /** _more_          */
    public static final String TAG_H4 = "h4";

    /** _more_          */
    public static final String TAG_HR = "hr";

    /** _more_          */
    public static final String TAG_LI = "li";

    /** _more_          */
    public static final String TAG_UL = "ul";

    /** _more_          */
    public static final String TAG_BOLD = "b";

    /** _more_          */
    public static final String TAG_IT = "i";

    /** _more_          */
    public static final String TAG_P = "p";

    /** _more_          */
    public static final String TAG_BR = "br";

    /** _more_          */
    public static final String TAG_FONT = "font";

    /** _more_          */
    public static final String TAG_IMG = "img";

    /** _more_          */
    public static final String TAG_A = "a";

    /** _more_          */
    public static final String[] BLOCKTAGS = {
        TAG_DIV, TAG_H1, TAG_H2, TAG_H3, TAG_H4, TAG_CENTER
    };

    /** _more_          */
    public static final String[] OPENTAGS = {
        TAG_DIV, TAG_H1, TAG_H2, TAG_H3, TAG_H4, TAG_CENTER, TAG_SPAN,
        TAG_FONT
    };



    /** _more_          */
    private boolean treatNewLineAsBreak = true;

    /** _more_          */
    private static int baseRowHeight = 1;

    /** _more_          */
    private static int baseWidth;

    /** _more_          */
    private static int maxWidth;

    /** _more_          */
    private static int crntRowHeight;


    /** _more_          */
    private static boolean nextTimeNewRow = false;

    /** _more_          */
    private static int numRows;

    /** _more_          */
    private static String crntFontName = "Dialog";

    /** _more_          */
    private static Font crntFont;

    /** _more_          */
    private static FontMetrics crntFm;

    /** _more_          */
    private static int crntFontHeight;

    /** _more_          */
    private static int crntFontSize;

    /** _more_          */
    private static int crntBoldOpen;

    /** _more_          */
    private static int crntItalicOpen;


    /** _more_          */
    private static Vector alignStack;

    /** _more_          */
    private static int crntAlign;

    /** _more_          */
    private static Vector bgColorStack;

    /** _more_          */
    private static Color crntBgColor;

    /** _more_          */
    private static Vector colorStack;

    /** _more_          */
    private static Color crntColor;

    /** _more_          */
    private static Vector sizeStack;

    /** _more_          */
    private static Vector hrefStack;

    /** _more_          */
    private static String crntHref;

    /** _more_          */
    private static Vector hrList = new Vector();

    /** _more_          */
    private static Vector rowsToBeAligned = new Vector();

    /** _more_          */
    private static Vector alignments = new Vector();

    /** _more_          */
    private static int numUls;


    /** _more_          */
    private static int crntLeft;

    /** _more_          */
    private static Vector rowGlyphs;

    /** _more_          */
    private static int crntX;

    /** _more_          */
    private static int crntY;

    /** _more_          */
    Hashtable crntAttrs;


    /** _more_          */
    protected boolean fixedWidth = false;

    /** _more_          */
    private Vector tokens = null;

    /** _more_          */
    private boolean addedExtraToken = false;

    /** _more_          */
    public Vector children = new Vector();

    /**
     * _more_
     *
     * @param canvas _more_
     * @param text _more_
     */
    public HtmlGlyph(ScrollCanvas canvas, String text) {
        this(canvas, text, false);
    }


    /**
     * _more_
     *
     * @param canvas _more_
     * @param text _more_
     * @param treatNewLineAsBreak _more_
     */
    public HtmlGlyph(ScrollCanvas canvas, String text,
                     boolean treatNewLineAsBreak) {
        this(canvas, 0, 0, text);
        this.treatNewLineAsBreak = treatNewLineAsBreak;
    }

    /**
     * _more_
     *
     * @param canvas _more_
     * @param x _more_
     * @param y _more_
     * @param text _more_
     */
    public HtmlGlyph(ScrollCanvas canvas, int x, int y, String text) {
        super(HTMLTEXT, canvas, x, y, text, null);
    }


    /**
     * _more_
     */
    protected void textChanged() {
        super.textChanged();
        checkTokens(true);
    }


    /**
     * _more_
     */
    private void makeTokens() {
        String t = text;
        if (treatNewLineAsBreak) {
            t = GuiUtils.replace(text, "\n", "<br>");
        }
        tokens          = tokenizeLine(t);

        addedExtraToken = false;
        if (tokens.size() == 0) {
            addedExtraToken = true;
        } else {
            addedExtraToken =
                ((Boolean) ((Object[]) tokens.elementAt(tokens.size()
                    - 1))[1]).booleanValue();
        }
        if (addedExtraToken) {
            tokens.addElement(new Object[] { " ", Boolean.FALSE, null });
        }
    }

    /**
     * _more_
     *
     * @param force _more_
     */
    private synchronized void checkTokens(boolean force) {
        if ((tokens == null) || force) {
            makeTokens();
            doLayout(bounds.x, bounds.y, Math.max(bounds.width, 2000), 0);
        }
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    public void paint(Graphics g, ScrollCanvas c) {
        checkTokens(false);
        for (int i = 0; i < children.size(); i++) {
            Glyph child = (Glyph) children.elementAt(i);
            child.paint(g, c);
        }
        if (inserting) {
            if (children.size() > 0) {
                Glyph lastGlyph = (Glyph) children.elementAt(children.size()
                                      - 1);
                Rectangle b = lastGlyph.getBounds();
                drawCaret(g, b.y, b.y + b.height, (addedExtraToken
                        ? b.x
                        : b.x + b.width));
            }
        }
    }


    /** _more_          */
    String tmp = "";

    /**
     * _more_
     *
     * @param msg _more_
     */
    public void log(String msg) {
        tmp = tmp + msg + "\n";
    }

    /**
     * _more_
     */
    public void calculateBounds() {
        bounds = super.calculateBounds(children);
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     * @param width _more_
     * @param padding _more_
     */
    public synchronized void doLayout(int x, int y, int width, int padding) {
        baseWidth     = width;
        bounds.x      = x;
        bounds.y      = y;
        maxWidth      = 0;
        bounds.width  = width;
        crntY         = y;
        this.children = new Vector();
        //Make sure we have processed the tokens.
        if (tokens == null) {
            makeTokens();
        }
        hrList.removeAllElements();
        rowsToBeAligned.removeAllElements();
        alignments.removeAllElements();

        createGlyphs(tokens);

        //Set the widths of the <hr> tags
        bounds.height += padding;
        bounds.width  += padding;
        if (fixedWidth) {
            bounds.height = (crntY - bounds.y);
        } else {
            calculateBounds();
            int lrx = bounds.x + bounds.width;
            int lry = bounds.y + bounds.height;
            bounds.x      = x;
            bounds.y      = y;
            bounds.width  = lrx - bounds.x;
            bounds.height = lry - bounds.y;
        }
        int rhs = bounds.x + bounds.width;
        for (int i = 0; i < hrList.size(); i++) {
            LineGlyph lg = (LineGlyph) hrList.elementAt(i);
            lg.p2.x = rhs;
            if (lg.width != null) {
                double dw;
                int    w        = (lg.p2.x - lg.p1.x);
                int    midPoint = lg.p1.x + w / 2;
                if (lg.width.endsWith("%")) {
                    dw = getDouble(lg.width.substring(0,
                            lg.width.length() - 1), 100.0);
                    lg.p1.x = (int) (midPoint - dw / 100.0 / 2.0 * w);
                    lg.p2.x = (int) (midPoint + dw / 100.0 / 2.0 * w);
                } else {
                    dw      = getDouble(lg.width, 1.0);
                    lg.p1.x = (int) (midPoint - dw / 2);
                    lg.p2.x = (int) (midPoint + dw / 2);
                }
            }
        }
        for (int i = 0; i < alignments.size(); i++) {
            alignRow((Vector) rowsToBeAligned.elementAt(i),
                     ((Integer) alignments.elementAt(i)).intValue(), rhs);
        }
    }


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     * @param pt _more_
     * @param correct _more_
     *
     * @return _more_
     */
    public String stretchTo(int x, int y, String pt, boolean correct) {
        moveBy(x, y);
        return pt;
    }


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void moveTo(int x, int y) {
        moveBy(x - bounds.x, y - bounds.y);
    }


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void moveBy(int x, int y) {
        bounds.x += x;
        bounds.y += y;
        for (int i = 0; i < children.size(); i++) {
            Glyph child = (Glyph) children.elementAt(i);
            child.moveBy(x, y);
        }
    }


    /**
     * _more_
     *
     * @param tokens _more_
     */
    private void createGlyphs(Vector tokens) {

        rowGlyphs      = new Vector();
        numRows        = 0;
        crntLeft       = getLeft();
        crntX          = crntLeft;
        crntColor      = Color.black;
        crntBgColor    = null;
        numUls         = 0;
        crntAlign      = Label.LEFT;
        crntFontSize   = DFLT_FONT_SIZE;
        crntBoldOpen   = 0;
        crntItalicOpen = 0;
        alignStack     = null;
        colorStack     = null;
        sizeStack      = null;
        hrefStack      = null;
        crntHref       = null;
        nextTimeNewRow = false;

        resetFont();
        baseRowHeight = crntFontHeight + 2;
        crntRowHeight = baseRowHeight;
        crntX         = crntLeft;

        String  tok   = "";
        boolean isTag = false;

        for (int i = 0; i < tokens.size(); i++) {
            Object[] tokArray = (Object[]) tokens.elementAt(i);
            tok       = (String) tokArray[0];
            isTag     = ((Boolean) tokArray[1]).booleanValue();
            crntAttrs = (Hashtable) tokArray[2];
            //      System.err.println ("TOK:" + tok+":");
            if (crntX > (bounds.x + baseWidth - 10)) {
                newRow();
            }

            if ( !isTag) {
                Vector       words = GuiUtils.split(tok, WHITESPACE);
                StringBuffer sb    = new StringBuffer();
                for (int wordIdx = 0; wordIdx < words.size(); wordIdx++) {
                    sb.append((String) words.elementAt(wordIdx));
                    boolean lastWord = (wordIdx == words.size() - 1);
                    if ( !lastWord) {
                        sb.append(" ");
                    }
                    String tmpS = sb.toString();
                    int    sw   = crntFm.stringWidth(tmpS);
                    if (lastWord || (crntX + sw > (bounds.x + baseWidth))) {
                        TextGlyph textGlyph =
                            new TextGlyph(canvas, crntX,
                                          crntY
                                          + (crntRowHeight - crntFontHeight)
                                          - 2, XmlNode.decode(tmpS),
                                              crntFont);
                        addGlyph(textGlyph, tmpS);
                        crntX += sw;
                        sb.setLength(0);
                        if (crntX > (bounds.x + baseWidth)) {
                            newRow();
                        }
                    }
                }
                continue;
            }

            boolean close = tok.indexOf("/") == 0;
            if (close) {
                tok = tok.substring(1);
            } else {
                colorStack   = pushStack(colorStack, crntColor);
                crntColor    = GuiUtils.getColor(getAttr("color"), crntColor);
                bgColorStack = pushStack(bgColorStack, crntBgColor);
                crntBgColor = GuiUtils.getColor(getAttr("bgcolor"),
                        crntBgColor);
                sizeStack    = pushStack(sizeStack,
                                         new Integer(crntFontSize));
                crntFontSize = getInt(getAttr("size"), crntFontSize);
                resetFont();
            }

            boolean needToPop = true;

            if (tok.equals(TAG_BR)) {
                newRow();
            } else if (tok.equals(TAG_P)) {
                newRow();
                newRow();
            } else if (tok.equals(TAG_UL)) {
                if ( !close) {
                    numUls++;
                    newRowIfNeeded();
                    crntX = crntLeft = (crntLeft + UL_SIZE);
                } else {
                    newRowIfNeeded();
                    numUls = Math.max(0, numUls - 1);
                    crntX = crntLeft = Math.max(getLeft(),
                            crntLeft - UL_SIZE);
                }
            } else if (tok.equals(TAG_LI)) {
                newRowIfNeeded();
                int liY = crntY + crntRowHeight - LI_SIZE - 2;
                if (numUls <= 1) {
                    addGlyph(new RectangleGlyph(Glyph.FCIRCLE, crntX, liY,
                            LI_SIZE, LI_SIZE), tok);
                } else if (numUls <= 2) {
                    addGlyph(new RectangleGlyph(Glyph.CIRCLE, crntX, liY,
                            LI_SIZE, LI_SIZE), tok);
                } else if (numUls <= 3) {
                    addGlyph(new RectangleGlyph(Glyph.FRECTANGLE, crntX, liY,
                            LI_SIZE, LI_SIZE), tok);
                } else {
                    addGlyph(new RectangleGlyph(Glyph.RECTANGLE, crntX, liY,
                            LI_SIZE, LI_SIZE), tok);
                }
                crntX += LI_SIZE + 2;
            } else if (tok.equals(TAG_HR)) {
                newRowIfNeeded();
                LineGlyph lg = new LineGlyph(crntX, crntY + 2, (fixedWidth
                        ? getLeft() + baseWidth
                        : crntX), crntY + 2);
                lg.width = getAttr("width");
                hrList.addElement(lg);
                addGlyph(lg, tok);
                newRow(4);
            } else if (tok.equals(TAG_BOLD)) {
                incBold(close
                        ? -1
                        : 1);
            } else if (tok.equals(TAG_IT)) {
                incItalic(close
                          ? -1
                          : 1);
            } else if (tok.equals(TAG_A)) {
                if ( !close) {
                    if (crntHref != null) {
                        hrefStack = pushStack(hrefStack, crntHref);
                    }
                    crntHref = getAttr("href");
                } else {
                    crntHref = (String) popStack(hrefStack, (String) null);
                }
            } else if (tok.equals(TAG_IMG)) {
                String imageUrl = getAttr("src");
                if (imageUrl == null) {
                    continue;
                }
                Image image = canvas.getImage(imageUrl);
                if (image != null) {
                    crntX += 2;
                    int imageHeight = image.getHeight(this) + 5;
                    if (imageHeight > crntRowHeight) {
                        crntRowHeight = imageHeight;
                    }
                    addGlyph(new ImageGlyph(canvas, crntX,
                                            crntY + crntRowHeight
                                            - imageHeight + 3, image), tok);
                    crntX += image.getWidth(this) + 2;
                }
            } else if (isOpenTag(tok)) {
                if (close) {
                    if (isBlockTag(tok)) {
                        newRow();
                        popAlign();
                    }
                } else {
                    if (isBlockTag(tok)) {
                        newRowIfNeeded();
                        if (tok.equals(TAG_CENTER)) {
                            pushAlign("center");
                        } else {
                            pushAlign(getAttr("align"));
                        }
                    }
                    needToPop = false;
                }
            }
            if (needToPop) {
                crntColor   = (Color) popStack(colorStack, crntColor);
                crntBgColor = (Color) popStack(bgColorStack, null);
                crntFontSize = ((Integer) popStack(sizeStack,
                        new Integer(DFLT_FONT_SIZE))).intValue();
                resetFont();
            }
        }

        if (tok.equals(TAG_BR) || tok.equals(TAG_P) || tok.equals(TAG_HR)) {
            TextGlyph textGlyph = new TextGlyph(canvas, crntX, crntY
                                      + (crntRowHeight - crntFontHeight)
                                      - 2, "", crntFont);
            addGlyph(textGlyph, "");
        }
        if (anythingOnRow() || (numRows == 0)) {
            newRow();
        }
        prok = false;


    }

    /** _more_          */
    static boolean prok = true;

    /**
     * _more_
     *
     * @param s _more_
     */
    static void pr(String s) {
        if (prok) {
            System.err.println(s);
        }
    }


    /**
     * _more_
     *
     * @param tag _more_
     * @param a _more_
     *
     * @return _more_
     */
    private boolean equals(String tag, String[] a) {
        for (int i = 0; i < a.length; i++) {
            if (tag.equals(a[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * _more_
     *
     * @param tag _more_
     *
     * @return _more_
     */
    private boolean isOpenTag(String tag) {
        return equals(tag, OPENTAGS);
    }

    /**
     * _more_
     *
     * @param tag _more_
     *
     * @return _more_
     */
    private boolean isBlockTag(String tag) {
        return equals(tag, BLOCKTAGS);
    }

    /**
     * _more_
     *
     * @param attr _more_
     *
     * @return _more_
     */
    private String getAttr(String attr) {
        if (crntAttrs == null) {
            return null;
        }
        return (String) crntAttrs.get(attr);
    }


    /**
     * _more_
     */
    private void nextTimeNewRow() {
        nextTimeNewRow = true;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    private boolean anythingOnRow() {
        return rowGlyphs.size() > 0;
    }

    /**
     * _more_
     */
    private void newRowIfNeeded() {
        if (anythingOnRow()) {
            newRow();
        }
    }

    /**
     * _more_
     */
    private void newRow() {
        newRow(crntRowHeight);
    }

    /**
     * _more_
     *
     * @param rowHeightToUse _more_
     */
    private void newRow(int rowHeightToUse) {
        flushRow();
        int width = (crntX - bounds.x);
        if (width > maxWidth) {
            maxWidth = width;
        }
        crntX         = crntLeft;
        crntY         += rowHeightToUse + 1;
        crntRowHeight = baseRowHeight;
        numRows++;
    }

    /**
     * _more_
     */
    private void flushRow() {
        int rowWidth = 0;
        for (int i = 0; i < rowGlyphs.size(); i++) {
            Glyph glyph = (Glyph) rowGlyphs.elementAt(i);
            if (glyph.baseline != crntRowHeight) {
                glyph.moveBy(0, crntRowHeight - glyph.baseline);
            }
        }
        if ((crntAlign != Label.LEFT) && (rowGlyphs.size() > 0)) {
            if (fixedWidth) {
                alignRow(rowGlyphs, crntAlign, bounds.x + baseWidth);
            } else {
                rowsToBeAligned.addElement((Vector) rowGlyphs.clone());
                alignments.addElement(new Integer(crntAlign));
            }
        }
        rowGlyphs.removeAllElements();
    }

    /**
     * _more_
     *
     * @param rowGlyphs _more_
     * @param align _more_
     * @param rhs _more_
     */
    private void alignRow(Vector rowGlyphs, int align, int rhs) {
        if (rowGlyphs.size() == 0) {
            return;
        }
        Rectangle rightBounds = ((Glyph) rowGlyphs.elementAt(rowGlyphs.size()
                                    - 1)).getBounds();
        int delta = rhs - (rightBounds.x + rightBounds.width);
        if (align == Label.CENTER) {
            delta = delta / 2;
        }
        for (int i = 0; i < rowGlyphs.size(); i++) {
            Glyph glyph = (Glyph) rowGlyphs.elementAt(i);
            glyph.moveBy(delta, 0);
        }
    }

    /**
     * _more_
     *
     * @param glyph _more_
     * @param tok _more_
     */
    private void addGlyph(Glyph glyph, String tok) {
        //    System.err.println ("addGlyph:" + glyph);
        if (nextTimeNewRow) {
            nextTimeNewRow = false;
            newRow();
        }

        rowGlyphs.addElement(glyph);
        glyph.baseline = crntRowHeight;

        children.addElement(glyph);
        glyph.setColor(crntColor);
        glyph.setBgColor(crntBgColor);

        if (crntHref != null) {
            boolean isText  = glyph instanceof TextGlyph;
            boolean isImage = glyph instanceof ImageGlyph;

            if (isText || isImage) {
                glyph.url = crntHref;
                if (isText) {
                    if (((TextGlyph) glyph).getText().trim().length() > 0) {
                        glyph.underline = true;
                    }
                }
                if (isImage) {
                    String border = getAttr("border");
                    if ( !((border == null)
                           ? false
                           : border.equals("0"))) {
                        glyph.underline = true;
                    }
                }
                glyph.setColor(Color.blue);
            }
        }
    }


    /**
     * _more_
     *
     * @param align _more_
     */
    private void pushAlign(String align) {
        alignStack = pushStack(alignStack, new Integer(crntAlign));
        crntAlign  = ((align == null)
                      ? crntAlign
                      : XmlUi.getAlign(align));
    }

    /**
     * _more_
     */
    private void popAlign() {
        Integer prevAlign = (Integer) popStack(alignStack, null);
        if (prevAlign != null) {
            crntAlign = prevAlign.intValue();
        } else {
            crntAlign = Label.LEFT;
        }
    }


    /**
     * _more_
     */
    private void resetFont() {
        crntFont       = getFont(crntBoldOpen > 0, crntItalicOpen > 0);
        crntFm         = canvas.getFontMetrics(crntFont);
        crntFontHeight = crntFm.getMaxDescent() + crntFm.getMaxAscent();
        int newRowHeight = crntFontHeight + 2;
        if (newRowHeight > crntRowHeight) {
            crntRowHeight = newRowHeight;
        }
    }


    /** _more_          */
    static Hashtable fonts = new Hashtable();

    /**
     * _more_
     *
     * @param bold _more_
     * @param italic _more_
     *
     * @return _more_
     */
    static Font getFont(boolean bold, boolean italic) {
        String key  = "-" + bold + "-" + italic + "-" + crntFontSize;
        Font   font = (Font) fonts.get(key);
        if (font == null) {
            int style = Font.PLAIN;
            if (bold) {
                style |= Font.BOLD;
            }
            if (italic) {
                style |= Font.ITALIC;
            }
            font = new Font(crntFontName, style, crntFontSize);
            fonts.put(key, font);
        }
        return font;
    }


    /**
     * _more_
     *
     * @param delta _more_
     */
    private void incBold(int delta) {
        crntBoldOpen = Math.max(0, crntBoldOpen + delta);
        resetFont();
    }

    /**
     * _more_
     *
     * @param delta _more_
     */
    private void incItalic(int delta) {
        crntItalicOpen = Math.max(0, crntItalicOpen + delta);
        resetFont();
    }


    /**
     * _more_
     *
     * @param img _more_
     * @param flags _more_
     * @param x _more_
     * @param y _more_
     * @param width _more_
     * @param height _more_
     *
     * @return _more_
     */
    public boolean imageUpdate(Image img, int flags, int x, int y, int width,
                               int height) {
        if ((flags & ImageObserver.ALLBITS) != 0) {
            if ( !fixedWidth) {
                checkTokens(true);
                canvas.repaintElement(this);
            } else {
                canvas.elementChanged(this);
            }
        }
        return true;
    }


    /**
     * _more_
     *
     * @param stack _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public static Object popStack(Vector stack, Object dflt) {
        if ((stack != null) && (stack.size() > 0)) {
            int lastIdx = stack.size() - 1;
            dflt = stack.elementAt(lastIdx);
            stack.removeElementAt(lastIdx);
        }
        return dflt;
    }

    /**
     * _more_
     *
     * @param stack _more_
     * @param v _more_
     *
     * @return _more_
     */
    public static Vector pushStack(Vector stack, Object v) {
        if (stack == null) {
            stack = new Vector();
        }
        stack.addElement(v);
        return stack;
    }

    /**
     * _more_
     *
     * @param line _more_
     *
     * @return _more_
     */
    public static Vector tokenizeLine(String line) {

        /**
         *   for (int i=0;i<ChatApplet.replaceFrom.size (); i++) {
         *     line = GuiUtils.replace (line,
         *         ChatApplet.replaceFrom.elementAt(i).toString(),
         *         ChatApplet.replaceTo.elementAt(i).toString(),
         *         false);
         *         }**
         */

        Vector       isTag     = new Vector();
        Vector       attrs     = new Vector();
        Vector       tokenList = new Vector();
        StringBuffer prefix    = new StringBuffer();


        while (line.length() > 0) {
            int idx = line.indexOf("<");
            if (idx < 0) {
                prefix.append(line);
                break;
            }
            if (idx > 0) {
                prefix.append(line.substring(0, idx));
                line = line.substring(idx);
            }
            int idx2 = line.indexOf(">");
            if (idx2 < 0) {
                prefix.append(line);
                break;
            }
            if (prefix.length() > 0) {
                tokenList.addElement(XmlNode.decode(prefix.toString()));
                isTag.addElement(Boolean.FALSE);
                attrs.addElement(null);
                prefix.setLength(0);
            }
            String tag = line.substring(1, idx2).trim();
            line = line.substring(idx2 + 1);
            int spaceIdx = tag.indexOf(" ");
            if (spaceIdx >= 0) {
                String attrString = tag.substring(spaceIdx);
                attrs.addElement(XmlNode.parseAttributes(attrString));
                tag = tag.substring(0, spaceIdx).trim();
            } else {
                attrs.addElement(new Hashtable());
            }

            tokenList.addElement(tag.toLowerCase());
            isTag.addElement(Boolean.TRUE);
        }


        //Add in the text if we have any dangling
        if (prefix.length() > 0) {
            tokenList.addElement(XmlNode.decode(prefix.toString()));
            isTag.addElement(Boolean.FALSE);
            attrs.addElement(null);
        }

        Vector  realTokens = new Vector();
        boolean prevTag    = false;


        //Now strip out any whitespace that is between two tags
        for (int i = 0; i < tokenList.size(); i++) {
            String  value   = (String) tokenList.elementAt(i);
            boolean isATag  = isTag.elementAt(i).equals(Boolean.TRUE);
            boolean notLast = (i < isTag.size() - 1);
            boolean nextTag = (notLast
                               ? isTag.elementAt(i + 1).equals(Boolean.TRUE)
                               : false);
            if ( !isATag) {
                if (prevTag && nextTag) {
                    prevTag = false;
                    if (value.trim().length() == 0) {
                        continue;
                    }
                }
                prevTag = false;
            } else {
                prevTag = true;
            }
            realTokens.addElement(new Object[] { value, isTag.elementAt(i),
                    attrs.elementAt(i) });

        }
        return realTokens;
    }


}

