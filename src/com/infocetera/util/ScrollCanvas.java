/*
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


import java.awt.*;
import java.awt.event.*;

import java.util.Hashtable;

import javax.swing.*;


/**
 * Class ScrollCanvas _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class ScrollCanvas extends JPanel implements KeyListener,
        AdjustmentListener, ActionListener, WindowListener {

    /** _more_ */
    public static final GuiUtils GU = null;

    /** _more_ */
    public int hTrans = 0;

    /** _more_ */
    public int vTrans = 0;

    /** _more_ */
    public JScrollBar hScroll;

    /** _more_ */
    public int hScrollVisible;

    /** _more_ */
    public int hScrollLength;

    /** _more_ */
    public JScrollBar vScroll;

    /** _more_ */
    public int vScrollVisible;

    /** _more_ */
    public int vScrollLength;

    /** _more_ */
    public Image bufferedImage = null;

    /** _more_ */
    public int bufferWidth;

    /** _more_ */
    public int bufferHeight;

    /** _more_ */
    private double scale = 1.0;

    /** _more_ */
    public static int DFLT_SCALE_IDX = 8;

    /** _more_ */
    public int scaleIdx = DFLT_SCALE_IDX;

    /** _more_ */
    public static final String CMD_ZOOM_IN = "zoomIn";

    /** _more_ */
    public static final String CMD_ZOOM_OUT = "zoomOut";

    /** _more_ */
    public static final String CMD_ZOOM_RESET = "zoomReset";

    /** _more_ */
    public static final String CMD_MESSAGE_CLOSE = "message.close";

    /** _more_ */
    public static final String CMD_MESSAGE_CLEAR = "message.clear";

    /** _more_ */
    public static final String CMD_SCROLL_LEFT = "scrollLeft";

    /** _more_ */
    public static final String CMD_SCROLL_RIGHT = "scrollRight";

    /** _more_ */
    public static final String CMD_SCROLL_UP = "scrollUp";

    /** _more_ */
    public static final String CMD_SCROLL_DOWN = "scrollDown";

    /** _more_ */
    public static final String CMD_SCROLL_RESET = "scrollReset";


    /** _more_ */
    Frame messageFrame;

    /** _more_ */
    TextArea messageText;


    /** _more_ */
    public static final double[] scaleFactors = {
        .2, .3, .4, .5, .6, .7, .8, .9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 2.0,
        2.5, 3.0
    };

    /** _more_ */
    public static int[] scaledFontSizes = {
        4, 4, 6, 7, 8, 9, 10, 11, 12, 12, 13, 14, 18, 20, 22, 24, 25
    };

    /** _more_ */
    public static Font[] scaledFonts = {
        null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null
    };

    /**
     * _more_
     */
    public ScrollCanvas() {
        addKeyListener(this);
    }

    /**
     * _more_
     *
     * @param g _more_
     *
     * @return _more_
     */
    public Font getScaledFont(Graphics g) {
        if (scaleIdx == DFLT_SCALE_IDX) {
            return g.getFont();
        }
        if (scaledFonts[scaleIdx] == null) {
            scaledFonts[scaleIdx] = new Font(g.getFont().getName(), 0,
                                             scaledFontSizes[scaleIdx]);
        }
        return scaledFonts[scaleIdx];
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getFontSize() {
        return scaledFontSizes[scaleIdx];
    }


    /**
     * _more_
     */
    public void stop() {
        if (messageFrame != null) {
            messageFrame.dispose();
            messageFrame = null;
        }
    }



    /**
     * _more_
     *
     * @param f _more_
     *
     * @return _more_
     */
    public final Font scaleFont(Font f) {
        //    if(scale==1.0)return f;
        return f;
        //    return f.deriveFont((int)(f.getSize()*scale));
    }


    /**
     * _more_
     */
    protected void doZoomIn() {
        scale = scale + 0.1;
        repaint();
    }

    /**
     * zoom out
     */
    protected void doZoomOut() {
        scale = scale - 0.1;
        if (scale < 0.1) {
            scale = 0.1;
        }
        repaint();
    }


    /**
     * _more_
     *
     * @param delta _more_
     */
    public void rescale(int delta) {
        int crntScale = getScaleIdx();

        if (delta == 0) {
            setScaleIdx(DFLT_SCALE_IDX);
        } else {
            setScaleIdx(crntScale + delta);
        }
        repaint();
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void actionPerformed(ActionEvent e) {
        //    String action = e.getActionCommand ();
        //    handleAction (action,e);
    }



    /**
     * _more_
     *
     * @param func _more_
     * @param params _more_
     * @param extra _more_
     * @param ae _more_
     *
     * @throws Exception _more_
     */
    protected void handleFunction(String func, String params,
                                  Hashtable extra, ActionEvent ae)
            throws Exception {

        if (func.equals(CMD_MESSAGE_CLOSE)) {
            if (messageFrame != null) {
                messageFrame.setVisible(false);
            }
            return;
        }

        if (func.equals(CMD_MESSAGE_CLEAR)) {
            messageText.setText("");
            return;
        }

        if (func.equals(CMD_SCROLL_RESET)) {
            hTrans = 0;
            vTrans = 0;
            repaint();
        } else if (func.equals(CMD_SCROLL_LEFT)) {
            hTrans += 10;
            repaint();
        } else if (func.equals(CMD_SCROLL_RIGHT)) {
            hTrans -= 10;
            repaint();
        } else if (func.equals(CMD_SCROLL_UP)) {
            vTrans += 10;
            repaint();
        } else if (func.equals(CMD_SCROLL_DOWN)) {
            vTrans -= 10;
            repaint();
        } else if (func.equals(CMD_ZOOM_IN)) {
            rescale(1);
        } else if (func.equals(CMD_ZOOM_RESET)) {
            rescale(0);
        } else if (func.equals(CMD_ZOOM_OUT)) {
            rescale(-1);
        } else {
            System.err.println("unknown function:" + func);
        }
    }

    /**
     * _more_
     */
    private void initMessageFrame() {
        if (messageFrame == null) {
            Frame     parentFrame = null;
            Component parent      = getParent();
            while ((parent != null) && (parentFrame == null)) {
                if (parent instanceof Frame) {
                    parentFrame = (Frame) parent;
                } else {
                    parent = parent.getParent();
                }
            }
            messageFrame = new Frame("Messages");
            messageFrame.setBackground(getParent().getBackground());
            messageText = new TextArea();
            messageText.setSize(300, 500);
            messageFrame.setLayout(new BorderLayout());
            messageFrame.add("Center", messageText);

            Component buttons = GuiUtils.flow(new Component[] {
                                    getButton("Clear", CMD_MESSAGE_CLEAR),
                                    getButton("Close",
                                        CMD_MESSAGE_CLOSE) }, 2);
            messageFrame.add("South", buttons);
            messageFrame.pack();
        }

    }


    /**
     * _more_
     *
     * @param message _more_
     */
    public void showMessage(String message) {
        initMessageFrame();
        messageText.append(message);
        messageFrame.show();
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void removeElement(Object g) {}

    /**
     * _more_
     *
     * @param g _more_
     */
    public void repaintElement(Object g) {
        repaint();
    }

    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     */
    public Image getImage(String path) {
        return null;
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void elementChanged(Object g) {}


    /**
     * _more_
     *
     * @param r _more_
     *
     * @return _more_
     */
    public final Rectangle scaleRect(Rectangle r) {
        if (true) {
            return r;
        }
        return ((scale == 1.0)
                ? new Rectangle(r.x, r.y, r.width, r.height)
                : new Rectangle((int) (r.x * scale), (int) (r.y * scale),
                                (int) (r.width * scale),
                                (int) (r.height * scale)));
    }

    /**
     * _more_
     *
     * @param x _more_
     *
     * @return _more_
     */
    public final int scaleCoord(int x) {
        if (true) {
            return x;
        }
        if (scale == 1.0) {
            return x;
        }
        int sx = (int) (scale * x);
        if (sx == 0) {
            sx = 1;
        }
        return sx;
    }

    /**
     * _more_
     *
     * @param x _more_
     *
     * @return _more_
     */
    public final int inverseScale(int x) {
        if (true) {
            return x;
        }

        if (scale == 1.0) {
            return x;
        }
        int sx = (int) (x / scale);
        if (sx == 0) {
            sx = 1;
        }
        return sx;
    }


    /**
     * _more_
     *
     * @param x _more_
     *
     * @return _more_
     */
    public final int scale(int x) {
        if (true) {
            return x;
        }
        if (scale == 1.0) {
            return x;
        }
        int sx = (int) (scale * x);
        if (sx == 0) {
            sx = 1;
        }
        return sx;
    }

    /**
     * _more_
     *
     * @param p _more_
     *
     * @return _more_
     */
    public final Point scalePoint(Point p) {
        if (true) {
            return p;
        }
        if (scale == 1.0) {
            return p;
        }
        return new Point((int) (scale * p.x), (int) (scale * p.y));
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public double getScale() {
        return scale;
    }

    /**
     * _more_
     *
     * @param scale _more_
     */
    public void setScale(double scale) {
        this.scale = scale;
        if (hScroll != null) {
            hScroll.setValues(hScroll.getValue(),
                              (int) (hScrollVisible / scale), 0,
                              hScrollLength);
        }
        if (vScroll != null) {
            vScroll.setValues(vScroll.getValue(),
                              (int) (vScrollVisible / scale), 0,
                              vScrollLength);
        }
        repaint();
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void scrollToPoint(int x, int y) {
        Rectangle b           = getBounds();
        boolean   needRepaint = false;

        int       dy          = 0;
        if (y < 0) {
            dy = y - 5;
        } else if (y > b.y + b.height) {
            dy = y - (b.y + b.height) + 5;
        }
        if ((dy != 0) && (vScroll != null)) {
            needRepaint = true;
            vScroll.setValue(vScroll.getValue() + dy);
        }

        int dx = 0;
        if (x < 0) {
            dx = x - 5;
        } else if (x > b.x + b.width) {
            dx = x - (b.x + b.width) + 5;
        }
        if ((dx != 0) && (hScroll != null)) {
            needRepaint = true;
            hScroll.setValue(hScroll.getValue() + dx);
        }

        if (needRepaint) {
            repaint();
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getScaleIdx() {
        return scaleIdx;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double getScaleFactor() {
        return scaleFactors[scaleIdx];
    }

    /**
     * _more_
     *
     * @param idx _more_
     */
    public void setScaleIdx(int idx) {
        if (idx < 0) {
            idx = 0;
        } else if (idx >= scaleFactors.length) {
            idx = scaleFactors.length - 1;
        }
        scaleIdx = idx;
        setScale(scaleFactors[idx]);
    }


    /**
     * _more_
     *
     * @param x _more_
     *
     * @return _more_
     */
    public int shiftInputX(int x) {
        return x + getHd();
    }

    /**
     * _more_
     *
     * @param y _more_
     *
     * @return _more_
     */
    public int shiftInputY(int y) {
        return y + getVd();
    }

    /**
     * _more_
     *
     * @param x _more_
     *
     * @return _more_
     */
    public int scaleInputX(int x) {
        return (int) ((x) / scale);
    }

    /**
     * _more_
     *
     * @param x _more_
     *
     * @return _more_
     */
    public int scaleInputY(int x) {
        return (int) ((x) / scale);
    }


    /**
     * _more_
     *
     * @param x _more_
     *
     * @return _more_
     */
    public int translateInputX(int x) {
        return (int) ((x + getHd()) / scale);
    }

    /**
     * _more_
     *
     * @param y _more_
     *
     * @return _more_
     */
    public int translateInputY(int y) {
        return (int) ((y + getVd()) / scale);
    }

    /**
     * _more_
     *
     * @param x _more_
     *
     * @return _more_
     */
    public int translateOutputX(int x) {
        if (true) {
            return x;
        }
        return (int) ((scaleCoord(x) - getHd()));
    }

    /**
     * _more_
     *
     * @param y _more_
     *
     * @return _more_
     */
    public int translateOutputY(int y) {
        if (true) {
            return y;
        }
        return (int) ((scaleCoord(y) - getVd()));
    }




    /**
     * _more_
     *
     * @param evt _more_
     *
     * @return _more_
     */
    public static boolean isScrollEvent(Event evt) {
        return ((evt.id == Event.SCROLL_ABSOLUTE)
                || (evt.id == Event.SCROLL_BEGIN)
                || (evt.id == Event.SCROLL_END)
                || (evt.id == Event.SCROLL_LINE_DOWN)
                || (evt.id == Event.SCROLL_LINE_UP)
                || (evt.id == Event.SCROLL_PAGE_DOWN)
                || (evt.id == Event.SCROLL_PAGE_UP));

    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void adjustmentValueChanged(AdjustmentEvent e) {
        repaint();
    }

    /**
     * _more_
     *
     * @param dir _more_
     * @param step _more_
     * @param l _more_
     *
     * @return _more_
     */
    public JScrollBar getScrollbar(int dir, int step, int l) {
        if (dir == Scrollbar.HORIZONTAL) {
            if (hScroll != null) {
                return hScroll;
            }
        } else {
            if (vScroll != null) {
                return vScroll;
            }
        }
        JScrollBar scroll = new JScrollBar(dir, 0, step, 0, l);
        scroll.addAdjustmentListener(this);


        scroll.setBlockIncrement(step);
        scroll.setUnitIncrement(step);
        if (dir == JScrollBar.HORIZONTAL) {
            hScroll        = scroll;
            hScrollLength  = l;
            hScrollVisible = step;
        } else {
            vScroll        = scroll;
            vScrollLength  = l;
            vScrollVisible = step;
        }
        return scroll;
    }


    /**
     * _more_
     *
     * @param step _more_
     * @param l _more_
     *
     * @return _more_
     */
    public JScrollBar getHScroll(int step, int l) {
        return getScrollbar(JScrollBar.HORIZONTAL, step, l);
    }

    /**
     * _more_
     *
     * @param step _more_
     * @param l _more_
     *
     * @return _more_
     */
    public JScrollBar getVScroll(int step, int l) {
        return getScrollbar(JScrollBar.VERTICAL, step, l);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isDoubleBuffered() {
        return true;
    }



    /**
     * _more_
     *
     * @param e _more_
     */
    public void keyReleased(KeyEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void keyTyped(KeyEvent e) {}

    /**
     * _more_
     *
     * @param evt _more_
     */
    public void keyPressed(KeyEvent evt) {
        if (evt.isControlDown()) {
            char key = evt.getKeyChar();
            key = (char) (key + 'a' - 1);
            if (key == 'z') {
                setScaleIdx(getScaleIdx() + 1);
            } else if (key == 'o') {
                setScaleIdx(getScaleIdx() - 1);
            }
        }
    }

    /**
     * _more_
     *
     * @param l _more_
     * @param cmd _more_
     *
     * @return _more_
     */
    public Button getButton(String l, String cmd) {
        Button b = new Button(l);
        b.addActionListener(this);
        if (cmd != null) {
            b.setActionCommand(cmd);
        }
        return b;
    }

    /**
     * _more_
     *
     * @param l _more_
     * @param cmd _more_
     *
     * @return _more_
     */
    public JMenuItem getMenuItem(String l, String cmd) {
        JMenuItem b = new JMenuItem(l);
        b.addActionListener(this);
        b.setActionCommand(cmd);
        return b;
    }




    /**
     * _more_
     */
    private void makeBufferedImage() {
        Rectangle b = getBounds();
        if ((b.width <= 0) || (b.height <= 0)) {
            return;
        }
        if ((bufferedImage == null) || (bufferWidth != b.width)
                || (bufferHeight != b.height)) {
            bufferWidth   = b.width;
            bufferHeight  = b.height;
            bufferedImage = createImage(b.width, b.height);
        }
    }

    /** _more_ */
    public Rectangle lastRect = null;

    /**
     * _more_
     *
     * @return _more_
     */
    public int getHd() {
        return hTrans + ((hScroll == null)
                         ? 0
                         : hScroll.getValue());
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getVd() {
        return vTrans + ((vScroll == null)
                         ? 0
                         : vScroll.getValue());
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void initGraphics(Graphics g) {
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).scale(scale, scale);
            ((Graphics2D) g).translate(-hTrans, -vTrans);
        }
        Color bgColor = getBackground();
        if (bgColor == null) {
            bgColor = Color.white;
        }
        Rectangle b = bounds();
        g.setColor(bgColor);
        g.fillRect(0, 0, b.width, b.height);
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public synchronized void xxxxupdate(Graphics g) {
        makeBufferedImage();
        Graphics bufferedGraphics = bufferedImage.getGraphics();
        initGraphics(bufferedGraphics);
        try {
            paintInner(bufferedGraphics);
        } catch (Exception exc) {
            System.err.println("Error: " + exc);
            exc.printStackTrace();
        }
        Color bgColor = getBackground();
        if (bgColor == null) {
            bgColor = Color.white;
        }
        g.drawImage(bufferedImage, 0, 0, bgColor, null);
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintInner(Graphics g) {}

    /**
     * _more_
     *
     * @param g _more_
     */
    public final void paint(Graphics g) {
        super.paint(g);
        try {
            initGraphics(g);
            paintInner(g);
        } catch (Exception exc) {
            System.err.println("Error: " + exc);
            exc.printStackTrace();
        }
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowActivated(WindowEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowClosed(WindowEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowClosing(WindowEvent e) {
        e.getWindow().dispose();
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowDeactivated(WindowEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowDeiconified(WindowEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowIconified(WindowEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void windowOpened(WindowEvent e) {}

    /**
     * _more_
     *
     * @param contents _more_
     * @param makeBig _more_
     * @param bgColor _more_
     *
     * @return _more_
     */
    public Frame makeWindow(Component contents, boolean makeBig,
                            Color bgColor) {
        Frame frame = new Frame();
        frame.addWindowListener(this);
        if (bgColor != null) {
            frame.setBackground(bgColor);
        }
        frame.setLayout(new BorderLayout());
        if (contents != null) {
            frame.add("Center", contents);
        }
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (makeBig) {
            screenSize.width  = (int) (0.8 * screenSize.width);
            screenSize.height = (int) (0.8 * screenSize.height);
            frame.setSize(screenSize);
        }
        frame.setLocation((int) (0.1 * screenSize.width),
                          (int) (0.1 * screenSize.height));
        frame.show();
        return frame;
    }



}

