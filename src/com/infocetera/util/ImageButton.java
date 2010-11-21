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



package com.infocetera.util;


import com.infocetera.util.*;

import java.applet.Applet;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;

import java.util.Vector;

import javax.swing.*;


/**
 * Class ImageButton _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class ImageButton extends JPanel implements ImageObserver,
        MouseListener, MouseMotionListener {


    /** _more_ */
    public static final GuiUtils GU = null;

    /** _more_ */
    public static final int ON_BORDER = GU.BORDER_SUNKEN;

    /** _more_ */
    public static final int OFF_BORDER = GU.BORDER_RAISED;


    /** _more_ */
    private ActionListener actionListener;

    /** _more_ */
    private String action;

    /** _more_ */
    int padding = 0;

    /** _more_ */
    public boolean state = false;

    /** _more_ */
    Vector group = null;

    /** _more_ */
    public boolean isToggle = false;

    /** _more_ */
    int borderType = OFF_BORDER;

    /** _more_ */
    int defaultBorderType = GU.BORDER_EMPTY;

    /** _more_ */
    Image crntImage;

    /** _more_ */
    Image mainImage;

    /** _more_ */
    Image overImage;

    /** _more_ */
    Image downImage;

    /** _more_ */
    Image onImage;

    /** _more_ */
    Image offImage;

    /** _more_ */
    public Color color;

    /** _more_ */
    Dimension imageSize;

    /** _more_ */
    private boolean okToPaintBorder = true;

    /** _more_ */
    private boolean onPress = false;

    /**
     * _more_
     *
     * @param mainImage _more_
     * @param overImage _more_
     * @param downImage _more_
     * @param border _more_
     */
    public ImageButton(Image mainImage, Image overImage, Image downImage,
                       boolean border) {
        this(mainImage, overImage, downImage, null, border, false);
    }

    /**
     * _more_
     *
     * @param mainImage _more_
     * @param overImage _more_
     * @param downImage _more_
     * @param theImageSize _more_
     * @param border _more_
     * @param onPress _more_
     */
    public ImageButton(Image mainImage, Image overImage, Image downImage,
                       Dimension theImageSize, boolean border,
                       boolean onPress) {
        this.okToPaintBorder = border;
        this.onPress         = onPress;

        if (okToPaintBorder) {
            defaultBorderType = OFF_BORDER;
            padding           = 3;
        }
        borderType     = defaultBorderType;
        this.crntImage = mainImage;
        this.mainImage = mainImage;
        this.overImage = overImage;
        this.downImage = downImage;
        this.imageSize = theImageSize;
        if ((imageSize == null) && (crntImage != null)) {
            //Create the imageSize first because we use it in imageUpdate
            // which can get called because of the getWidth/getHeight calls
            imageSize = new Dimension(20, 20);
            int iw = crntImage.getWidth(this);
            int ih = crntImage.getHeight(this);
            if ((iw >= 0) || (ih >= 0)) {
                imageSize = new Dimension(iw, ih);
            }
        }
        setImageSize(imageSize);
        addMouseListener(this);
        addMouseMotionListener(this);
    }


    /**
     * _more_
     *
     * @param group _more_
     * @param onImagePath _more_
     * @param offImagePath _more_
     * @param bgColor _more_
     * @param padding _more_
     */
    public ImageButton(Vector group, String onImagePath, String offImagePath,
                       Color bgColor, int padding) {
        this.isToggle   = true;
        this.color      = bgColor;
        okToPaintBorder = true;
        this.padding    = padding;
        setGroup(group);
        if (onImagePath != null) {
            onImage = IfcApplet.getImage(onImagePath);
        }
        if (offImagePath != null) {
            offImage = IfcApplet.getImage(offImagePath);
        }


        setImageSize(new Dimension(16, 16));
        setBackground(color);
        addMouseListener(this);
    }


    /**
     * _more_
     *
     * @param dim _more_
     */
    public void setImageSize(Dimension dim) {
        imageSize = dim;
        if (padding > 0) {
            dim = new Dimension(dim.width + padding * 2,
                                dim.height + padding * 2);
        }
        super.setSize(dim);
        super.setPreferredSize(dim);
    }



    /**
     * _more_
     *
     * @param group _more_
     */
    public void setGroup(Vector group) {
        this.group = group;
        if (group != null) {
            group.addElement(this);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getAction() {
        return action;
    }

    /**
     * _more_
     *
     * @param action _more_
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * _more_
     *
     * @param listener _more_
     */
    public void setActionListener(ActionListener listener) {
        actionListener = listener;
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

        if ((flags & ImageObserver.ERROR) != 0) {
            setSize(20, 20);
            GuiUtils.relayout(this);
            System.err.println("ImageButton.imageUpdate: image error:"
                               + IfcApplet.getImagePath(img));
            return false;
        }

        if (imageSize != null) {
            if ((width > imageSize.width) || (height > imageSize.height)) {
                imageSize.width  = width;
                imageSize.height = height;
                setImageSize(imageSize);
            }
        }


        if ((flags & ImageObserver.ALLBITS) != 0) {
            GuiUtils.relayout(this);
            repaint();
        }
        return true;
    }

    /**
     * _more_
     *
     * @param newState _more_
     * @param notify _more_
     */
    public void setState(boolean newState, boolean notify) {
        state = newState;
        if ((group != null) && notify) {
            for (int i = 0; i < group.size(); i++) {
                ImageButton b = (ImageButton) group.elementAt(i);
                if (b != this) {
                    b.setState(false, false);
                }
            }
        }
        borderType = (state
                      ? ON_BORDER
                      : OFF_BORDER);
        repaint();
    }

    /**
     * _more_
     *
     * @param newState _more_
     */
    public void setState(boolean newState) {
        setState(newState, true);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getState() {
        return state;
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
    public void mouseMoved(MouseEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseEntered(MouseEvent e) {
        if (isToggle) {
            return;
        }
        if (overImage != null) {
            crntImage = overImage;
            repaint();
        }
        //    if (!state) {
        //     borderType= GU.BORDER_RAISED;
        //     repaint();
        //    }

    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseClicked(MouseEvent e) {
        if ( !onPress && (actionListener != null)) {
            actionListener.actionPerformed(new ActionEvent(this, 0, action));
        }
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseExited(MouseEvent e) {
        if (crntImage != mainImage) {
            crntImage = mainImage;
            repaint();
        }
        if ( !state) {
            borderType = OFF_BORDER;
        } else {
            borderType = ON_BORDER;
        }
        repaint();
    }

    /** _more_ */
    Color lastColor;

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseReleased(MouseEvent e) {
        if (crntImage != mainImage) {
            crntImage = mainImage;
            repaint();
        } else if (lastColor != null) {
            setBackground(lastColor);
        }
        if (actionListener != null) {
            borderType = defaultBorderType;
            repaint();
        }
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mousePressed(MouseEvent e) {
        if (downImage != null) {
            crntImage = downImage;
            repaint();
        } else {
            lastColor = getBackground();
            setBackground(Color.gray);
        }

        if (actionListener != null) {
            if (onPress) {
                actionListener.actionPerformed(new ActionEvent(this, 0,
                        action));
            }
            borderType = ON_BORDER;
            repaint();
        }

        if (isToggle) {
            if ( !state) {
                setState(true);
            }
        }

    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void paint(Graphics g) {
        super.paint(g);
        Dimension size   = getSize();
        int       width  = size.width - 1;
        int       height = size.height - 1;

        if (color != null) {
            g.setColor(color);
            g.fillRect(0, 0, width, height);
        }

        if (okToPaintBorder) {
            GuiUtils.paintBorder(g, borderType, size.width, size.height);
        }

        if (isToggle) {
            if (state && (onImage != null)) {
                g.drawImage(onImage, padding, padding, null, this);
            }
            if ( !state && (offImage != null)) {
                g.drawImage(offImage, padding, padding, null, this);
            }
        } else {
            if (crntImage != null) {
                g.drawImage(crntImage, padding, padding, null, this);
            }
        }
    }


}

