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

import java.applet.Applet;


import java.awt.*;
import java.awt.image.ImageObserver;

import java.net.URL;


/**
 * Class ImageGlyph _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class ImageGlyph extends RectangleGlyph implements ImageObserver {

    /** _more_ */
    String url = null;

    /** _more_ */
    Image image = null;

    /** _more_ */
    ScrollCanvas canvas;


    /**
     * _more_
     *
     * @param canvas _more_
     * @param x _more_
     * @param y _more_
     * @param url _more_
     */
    public ImageGlyph(ScrollCanvas canvas, int x, int y, String url) {
        super(Glyph.IMAGE, x, y, 0, 0);
        typeName = Glyph.IMAGE;
        setFilled(true);
        this.canvas = canvas;
        setImage(url);
    }

    /**
     * _more_
     *
     * @param canvas _more_
     * @param x _more_
     * @param y _more_
     * @param image _more_
     */
    public ImageGlyph(ScrollCanvas canvas, int x, int y, Image image) {
        super(Glyph.IMAGE, x, y, 0, 0);
        typeName = Glyph.IMAGE;
        setFilled(true);
        this.canvas = canvas;
        setImage(image);
    }


    /**
     * _more_
     *
     * @param url _more_
     */
    public void setImage(String url) {
        this.url = url;
        if (url == null) {
            return;
        }
        try {
            this.image = IfcApplet.getImage(url);
            if (image != null) {
                setImage(image);
            } else {
                IfcApplet.errorMsg("ImageGlyph.setImage " + url + "\n"
                                   + "Null image");
            }
        } catch (Exception exc) {
            IfcApplet.errorMsg("ImageGlyph.setImage " + url + "\n" + exc);
        }
    }


    /**
     * _more_
     *
     * @param image _more_
     */
    public void setImage(Image image) {
        this.image = image;
        if (image != null) {
            bounds.width  = image.getWidth(this);
            bounds.height = image.getHeight(this);
        }
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
        if (image == null) {
            return false;
        }
        if ((flags & ImageObserver.ERROR) != 0) {
            image = null;
            canvas.removeElement(this);
            IfcApplet.errorMsg("An error occured loading the image:" + url);

            return false;
        }

        bounds.width  = width;
        bounds.height = height;

        if ((flags & ImageObserver.ALLBITS) != 0) {
            canvas.elementChanged(this);
            canvas.repaintElement(this);
        }

        return true;
    }


    /**
     * _more_
     *
     * @param g _more_
     * @param c _more_
     */
    public void paint(Graphics g, ScrollCanvas c) {
        if (image == null) {
            return;
        }

        if (bounds.width < 0) {
            bounds.width = image.getWidth(this);
        }
        if (bounds.height < 0) {
            bounds.height = image.getHeight(this);
        }

        if ((bounds.width < 0) || (bounds.height < 0)) {
            return;
        }

        Rectangle r = c.scaleRect(bounds);
        g.setColor(Color.white);
        g.drawImage(image, r.x, r.y, r.width, r.height, null, this);
        if (underline) {
            if (getColor() != null) {
                g.setColor(getColor());
            }
            g.drawRect(r.x - 2, r.y - 2, r.width + 4, r.height + 4);
        }
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public String getStretchPoint(int x, int y) {
        return PT_CENTER;
    }


    /**
     * _more_
     *
     * @param match _more_
     *
     * @return _more_
     */
    public String getAttrs(String match) {
        return super.getAttrs(match) + makeAttr(match, ATTR_IMAGE, url);
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     */
    public void setAttr(String name, String value) {
        if (ATTR_IMAGE.equals(name)) {
            setImage(value);
        } else {
            super.setAttr(name, value);
        }
    }

}
