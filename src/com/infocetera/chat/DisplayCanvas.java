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

import java.applet.Applet;

import java.awt.*;

import java.awt.event.*;

import java.util.Hashtable;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;



import javax.swing.*;


/**
 * Class DisplayCanvas _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class DisplayCanvas extends ScrollCanvas implements ActionListener,
        ItemListener, MouseListener {

    /** _more_ */
    public static boolean debug = false;

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isDebug() {
        return debug;
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



    /** _more_ */
    public static final String CMD_BACKGROUNDIMAGE = "backgroundimage";

    /** _more_ */
    public static final String CMD_CREATE = "create";

    /** _more_ */
    public static final String CMD_CURSORPOS = "cursorpos";

    /** _more_ */
    public static final String CMD_CURSORDOWN = "cursordown";

    /** _more_ */
    public static final String CMD_CURSORUP = "cursorup";

    /** _more_ */
    public static final String CMD_TOFRONT = "tofront";

    /** _more_ */
    public static final String CMD_TOBACK = "toback";

    /** _more_ */
    public static final String CMD_CHANGE = "change";

    /** _more_ */
    public static final String CMD_REMOVE = "remove";

    /** _more_ */
    public static final String CMD_MOVE = "move";


    /** _more_ */
    public static final String TAG_COMMAND = "command";

    /** _more_ */
    public static final String ATTR_NAME = "name";

    /** _more_ */
    public static final String ATTR_TYPE = "type";

    /** _more_ */
    public static final String ATTR_ID = "id";

    /** _more_ */
    public static final String ATTR_X = "x";

    /** _more_ */
    public static final String ATTR_Y = "y";

    /** _more_ */
    public static final Cursor DEFAULT_CURSOR =
        new Cursor(Cursor.DEFAULT_CURSOR);

    /** _more_ */
    public static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);

    /** _more_ */
    public static final Cursor MOVE_CURSOR = new Cursor(Cursor.MOVE_CURSOR);

    /** _more_ */
    public static final Cursor TEXT_CURSOR = new Cursor(Cursor.TEXT_CURSOR);

    /** _more_ */
    public static final Cursor NW_CURSOR =
        new Cursor(Cursor.NW_RESIZE_CURSOR);


    /** _more_ */
    Vector frames = new Vector();

    /** _more_ */
    int currentFrame = 0;

    //  Color canvasBg = Color.white;

    /** _more_ */
    Vector glyphs = new Vector();

    /** _more_ */
    public SocketApplet myApplet;

    /** _more_ */
    Vector selectionSet = new Vector();

    /** _more_ */
    Vector cutBuffer;

    /** _more_ */
    protected Glyph highlightedGlyph = null;

    /** _more_ */
    int glyphCnt = 0;

    /** _more_ */
    boolean showScroller = true;

    /** _more_ */
    Label frameLbl;

    /** _more_ */
    JButton newFrameBtn;

    /** _more_ */
    JButton prevBtn;

    /** _more_ */
    JButton nextBtn;

    /** _more_ */
    JCheckBox paintAllFramesCbx;

    /** _more_ */
    Image image = null;

    /** _more_ */
    String backgroundImageUrl = null;

    /**
     * _more_
     *
     * @param applet _more_
     */
    public DisplayCanvas(SocketApplet applet) {
        this(applet, true);
    }

    /**
     * _more_
     *
     * @param applet _more_
     * @param showScroller _more_
     */
    public DisplayCanvas(SocketApplet applet, boolean showScroller) {
        setBackground(Color.red);
        frames.addElement(glyphs);
        myApplet          = applet;
        this.showScroller = showScroller;
        addMouseListener(this);
    }


    /**
     * _more_
     *
     * @param image _more_
     * @param url _more_
     */
    public void setBackgroundImage(Image image, String url) {
        this.backgroundImageUrl = url;
        this.image              = image;
        repaint();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isFocusTraversable() {
        return true;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Applet getApplet() {
        return myApplet;
    }


    /** _more_ */
    JComponent guts;


    /**
     * _more_
     *
     * @return _more_
     */
    public Container doMakeContents() {
        return doMakeContents(true);

    }

    /**
     * _more_
     *
     * @param doBorder _more_
     *
     * @return _more_
     */
    public Container doMakeContents(boolean doBorder) {
        if (guts == null) {
            JPanel p;
            doBorder = true;
            if (doBorder) {
                p = new BorderPanel(GuiUtils.BORDER_SUNKEN);
            } else {
                p = new JPanel();
            }
            p.setLayout(new BorderLayout());
            p.add("Center", this);
            /*
            if (showScroller) {
              guts =
                  GuiUtils.doLayout(new Component[]{p,getVScroll(300,3000),getHScroll(500,5000)},2,GuiUtils.DS_YN,GuiUtils.DS_YN);
            } else {
              guts = p;
            }
            */
            p.setPreferredSize(new Dimension(5000, 5000));
            guts = new JScrollPane(p);
            guts.setPreferredSize(new Dimension(500, 500));
        }

        return guts;

    }


    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     */
    public void offsetGlyphs(int x, int y) {
        if ((x == 0) && (y == 0)) {
            return;
        }
        for (int listIdx = 0; listIdx < frames.size(); listIdx++) {
            Vector glyphs = (Vector) frames.elementAt(listIdx);
            for (int i = 0; i < glyphs.size(); i++) {
                Glyph glyph = (Glyph) glyphs.elementAt(i);
                glyph.moveBy(-x, -y);
            }
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Rectangle getGlyphBounds() {
        Rectangle bounds = null;
        for (int listIdx = 0; listIdx < frames.size(); listIdx++) {
            Vector glyphs = (Vector) frames.elementAt(listIdx);
            for (int i = 0; i < glyphs.size(); i++) {
                Glyph     glyph = (Glyph) glyphs.elementAt(i);
                Rectangle gb    = glyph.getBounds();
                if (bounds == null) {
                    bounds = new Rectangle(gb);
                } else {
                    bounds.add(gb);
                }
            }
        }
        return bounds;
    }


    /** _more_ */
    int cnt = 0;

    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintInner(Graphics g) {
        Rectangle b = getBounds();
        //    g.setColor (canvasBg);
        //    g.fillRect (hTrans, vTrans, b.width, b.height);
        Rectangle clip = g.getClipBounds();
        if (clip != null) {
            clip.x      -= 2;
            clip.y      -= 2;
            clip.width  += 4;
            clip.height += 4;
        }

        if (image != null) {
            //        if(cnt++<5)
            //        myApplet.errorMsg("painting image " + image.getWidth(null));
            g.drawImage(image, 0, 0, null, this);
        }

        boolean paintAllFrames = ((paintAllFramesCbx == null)
                                  ? false
                                  : paintAllFramesCbx.isSelected());
        if (paintAllFrames) {
            for (int frameIdx = 0; frameIdx < frames.size(); frameIdx++) {
                if (frameIdx == currentFrame) {
                    continue;
                }
                Vector fglyphs = (Vector) frames.elementAt(frameIdx);
                for (int i = 0; i < fglyphs.size(); i++) {
                    Glyph     glyph = (Glyph) fglyphs.elementAt(i);
                    Rectangle gb    = scaleRect(glyph.getBounds());
                    if ((clip == null) || clip.intersects(gb)) {
                        glyph.paint(g, this);
                    }
                }
            }
        }


        for (int i = 0; i < glyphs.size(); i++) {
            Glyph     glyph = (Glyph) glyphs.elementAt(i);
            Rectangle gb    = scaleRect(glyph.getBounds());
            if ((clip == null) || clip.intersects(gb)) {
                glyph.paint(g, this);
            }
        }

        for (int i = 0; i < selectionSet.size(); i++) {
            Glyph     glyph = (Glyph) selectionSet.elementAt(i);
            Rectangle gb    = scaleRect(glyph.getBounds());
            if ((clip == null) || clip.intersects(gb)) {
                glyph.paintSelection(g, this);
            }
        }

    }



    /**
     * _more_
     *
     * @param s _more_
     * @param x _more_
     * @param y _more_
     * @param fromHere _more_
     *
     * @return _more_
     */
    public Glyph createGlyph(String s, int x, int y, boolean fromHere) {

        if (s.equals(Glyph.GROUP)) {
            return new CompositeGlyph(0, 0, null);
        }
        if (s.equals(Glyph.RECTANGLE)) {
            return new RectangleGlyph(s, x, y, 1, 1);
        }
        if (s.equals(Glyph.ROUNDRECT)) {
            return new RectangleGlyph(s, x, y, 1, 1);
        }
        if (s.equals(Glyph.CIRCLE)) {
            return new RectangleGlyph(s, x, y, 1, 1);
        }
        if (s.equals(Glyph.IMAGE)) {
            return new ImageGlyph(this, x, y, (String) null);
        }

        if (s.equals(Glyph.TEXT)) {
            return new TextGlyph(this, x, y, "");
        }


        if (s.equals(Glyph.HTMLTEXT)) {
            //      return new HtmlGlyph (this, x,y, "", true);
            return new HtmlGlyph(this, x, y, "");
        }
        if (s.equals(Glyph.LINE)) {
            return new LineGlyph(x, y, x, y);
            /*
                    PolyGlyph pg =  new PolyGlyph (true);
                    pg.addPoint (x,y);
                    pg.addPoint (x,y);
                    return pg;*/

        }

        if (s.equals(Glyph.PLINE)) {
            return new PolyGlyph(false);
        }

        return null;
    }

    /**
     * _more_
     *
     * @param r _more_
     */
    public void xxxrepaint(Rectangle r) {
        super.repaint(translateOutputX(r.x) - 4, translateOutputY(r.y) - 4,
                      scaleCoord(r.width) + 8, scaleCoord(r.height) + 8);
    }


    /**
     * _more_
     *
     * @param r _more_
     * @param lineWidth _more_
     */
    public void repaint(Rectangle r, int lineWidth) {
        if (true) {
            super.repaint();
            //    super.repaint(r);
            return;
        }

        int offh = 4;
        int off  = 8;

        /*
              lastRect = new Rectangle (
              translateOutputX (r.x)-offh-lineWidth,
              translateOutputY (r.y)-offh-lineWidth,
              scaleCoord (r.width)+off+2*lineWidth,
              scaleCoord (r.height)+off+2*lineWidth);**/

        super.repaint(translateOutputX(r.x) - offh - lineWidth,
                      translateOutputY(r.y) - offh - lineWidth,
                      scaleCoord(r.width) + off + 2 * lineWidth,
                      scaleCoord(r.height) + off + 2 * lineWidth);
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void repaintElement(Glyph g) {
        repaint((Glyph) g);
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void repaint(Glyph g) {

        repaint();
        //      repaint (g.getRepaintBounds(), g.getWidth ());
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void addGlyph(Glyph g) {
        glyphs.addElement(g);
        repaint();
        //      repaint(g.getBounds());
    }



    /**
     * _more_
     *
     * @param r _more_
     */
    public void select(Rectangle r) {
        r = scaleRect(r);
        for (int i = 0; i < glyphs.size(); i++) {
            Glyph     glyph = (Glyph) glyphs.elementAt(i);
            Rectangle gb    = scaleRect(glyph.getBounds());
            if (r.contains(gb.x, gb.y)
                    && r.contains(gb.x + gb.width, gb.y + gb.height)) {
                addSelection(glyph);
            }
        }
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasSelection() {
        return (selectionSet.size() >= 0);
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void addSelection(Glyph g) {
        if ( !isSelected(g)) {
            selectionSet.addElement(g);
            repaint(g);
        }
    }

    /**
     * _more_
     *
     * @param g _more_
     *
     * @return _more_
     */
    public boolean isSelected(Glyph g) {
        return selectionSet.contains(g);
    }


    /**
     * _more_
     */
    public void clearSelection() {
        selectionSet.removeAllElements();
        repaint();
    }

    /**
     * _more_
     */
    public void clearAll() {
        glyphs.removeAllElements();
        highlightedGlyph = null;
        clearSelection();
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void removeSelection(Glyph g) {
        selectionSet.removeElement(g);
        repaint(g);
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void justRemoveGlyph(Glyph g) {
        doRemove(g);
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void removeElement(Object g) {
        removeGlyph((Glyph) g);

    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void removeGlyph(Glyph g) {
        doRemove(g);
    }

    /**
     * _more_
     *
     * @param g _more_
     */
    public void doRemove(Glyph g) {
        if (highlightedGlyph == g) {
            repaint(highlightedGlyph);
            setHighlight(null);
        }
        removeSelection(g);
        glyphs.removeElement(g);
        if (g instanceof CompositeGlyph) {
            Vector children =
                (Vector) ((CompositeGlyph) g).getChildren().clone();
            for (int i = 0; i < children.size(); i++) {
                doRemove((Glyph) children.elementAt(i));
            }
        }
        g.doRemove();
        repaint(g);
    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void setHighlight(Glyph g) {
        highlightedGlyph = g;
    }

    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     */
    public Image getImage(String path) {
        return myApplet.getImage(path);
    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public Glyph findGlyph(String id) {
        int num = glyphs.size();
        for (int i = 0; i < num; i++) {
            Glyph g = (Glyph) glyphs.elementAt(i);
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    /**
     * _more_
     *
     * @param x _more_
     * @param y _more_
     *
     * @return _more_
     */
    public Glyph findGlyph(int x, int y) {
        return findGlyph(glyphs, x, y, 10.0);
    }

    /**
     * _more_
     *
     * @param glyphs _more_
     * @param x _more_
     * @param y _more_
     * @param threshold _more_
     *
     * @return _more_
     */
    public static Glyph findGlyph(Vector glyphs, int x, int y,
                                  double threshold) {
        int    num         = glyphs.size();
        double minDistance = Double.MAX_VALUE;
        Glyph  minGlyph    = null;
        for (int i = 0; i < num; i++) {
            Glyph g = (Glyph) glyphs.elementAt(i);
            if ( !g.pickable()) {
                continue;
            }
            double distance = g.distance(x, y);
            if ((distance < threshold) && (distance <= minDistance)) {
                minDistance = distance;
                minGlyph    = g;
            }
        }
        return minGlyph;
    }

    /**
     *  Create the whiteboard command xml.
     *  <pre>&lt;command name="some wb command" attributes/&gt;</pre>
     *
     * @param commandName _more_
     * @param attributes _more_
     *
     * @return _more_
     */
    public String makeCommandTag(String commandName, String attributes) {
        return XmlNode.tag(TAG_COMMAND,
                           XmlNode.attr(ATTR_NAME, commandName) + attributes);
    }

    /**
     *  ???
     *
     * @return _more_
     */
    public int getGlyphCount() {
        return glyphs.size();
    }


    /**
     * _more_
     *
     * @param from _more_
     *
     * @return _more_
     */
    public Vector cloneGlyphs(Vector from) {
        Vector to = new Vector();
        for (int i = 0; i < from.size(); i++) {
            Glyph o = (Glyph) from.elementAt(i);
            try {
                Glyph newGlyph = (Glyph) o.clone();
                newGlyph.setId(getGlyphId());
                to.addElement(newGlyph);
            } catch (Exception exc) {}
        }
        return to;
    }

    /**
     *  Return a (sortof) unique identifier for this chat session based
     *  on the applet id  the time and the local number of newly created glyphs
     */
    String uniqueifier = null;

    /**
     * _more_
     *
     * @return _more_
     */
    String getGlyphId() {
        if (uniqueifier == null) {
            uniqueifier = "" + System.currentTimeMillis();
        }
        return "ID-" + uniqueifier + "-" + (glyphCnt++);
    }






    /**
     * _more_
     *
     * @param cloneCurrent _more_
     */
    public void newGlyphSet(boolean cloneCurrent) {
        if (cloneCurrent) {
            glyphs = cloneGlyphs(glyphs);
        } else {
            glyphs = new Vector();
        }
        currentFrame++;
        frames.insertElementAt(glyphs, currentFrame);
        //    currentFrame = frames.size()-1;
        enableFrameBtns();
        repaint();
    }


    /**
     * _more_
     */
    public void deleteGlyphSet() {
        frames.removeElementAt(currentFrame);
        currentFrame--;
        if (frames.size() == 0) {
            frames.addElement(new Vector());
        }
        if (currentFrame < 0) {
            currentFrame = 0;
        }
        glyphs = (Vector) frames.elementAt(currentFrame);
        enableFrameBtns();
        repaint();
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mousePressed(MouseEvent e) {
        //    super.mousePressed (e);
        requestFocus();
    }

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseEntered(MouseEvent e) {
        requestFocus();
    }

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
    public void mouseReleased(MouseEvent e) {}


    /**
     * _more_
     *
     * @param e _more_
     */
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        //    System.err.println ("Key:" + e);
        if ((keyCode == KeyEvent.VK_N) || (keyCode == KeyEvent.VK_ENTER)
                || (keyCode == KeyEvent.VK_RIGHT)) {
            frameNext();
        } else if (keyCode == KeyEvent.VK_HOME) {
            setGlyphSet(0);
        } else if (keyCode == KeyEvent.VK_END) {
            setGlyphSet(frames.size() - 1);
        } else if ((keyCode == KeyEvent.VK_P)
                   || (keyCode == KeyEvent.VK_LEFT)) {
            framePrev();
        } else if (e.getKeyChar() == '+') {
            animationSleep -= 100;
            if (animationSleep < 100) {
                animationSleep = 100;
            }
        } else if (e.getKeyChar() == '-') {
            animationSleep += 100;
        } else if (keyCode == KeyEvent.VK_S) {
            if (animating) {
                stopFrameAnimation();
            } else {
                startFrameAnimation();
            }
        } else {
            super.keyPressed(e);
        }
    }

    /** _more_ */
    boolean animating = false;

    /** _more_ */
    int animationTimeStamp = 0;

    /** _more_ */
    long animationSleep = 1000;


    /**
     * _more_
     */
    public void stopFrameAnimation() {
        animating = false;
    }


    /**
     * _more_
     */
    public void startFrameAnimation() {
        if (animating) {
            return;
        }
        Thread t = new Thread(new Runnable() {
            public void run() {
                startFrameAnimation(++animationTimeStamp);
            }
        });
        t.start();
    }


    /**
     * _more_
     *
     * @param timestamp _more_
     */
    private void startFrameAnimation(int timestamp) {
        animating = true;
        while (animating && (timestamp == animationTimeStamp)) {
            frameNext();
            try {
                Thread.currentThread().sleep(animationSleep);
            } catch (Exception exc) {}
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getFrameCnt() {
        return frames.size();
    }

    /**
     * _more_
     */
    public void frameNext() {
        if (currentFrame == frames.size() - 1) {
            setGlyphSet(0);
        } else {
            setGlyphSet(currentFrame + 1);
        }
        enableFrameBtns();
    }

    /**
     * _more_
     */
    public void framePrev() {
        if (currentFrame - 1 < 0) {
            setGlyphSet(frames.size() - 1);
        } else {
            setGlyphSet(currentFrame - 1);
        }
        enableFrameBtns();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean frameAtStart() {
        return currentFrame == 0;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean frameAtEnd() {
        return currentFrame >= (frames.size() - 1);
    }

    /**
     * _more_
     */
    public void enableFrameBtns() {
        if (prevBtn != null) {
            //      prevBtn.setEnabled (!frameAtStart());
            //      nextBtn.setEnabled (!frameAtEnd());
            frameLbl.setText("Frame: " + (currentFrame + 1) + " of "
                             + frames.size());
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Container getFramePanel() {

        JButton delFrameBtn = new JButton("Delete frame");
        delFrameBtn.setActionCommand("frame.delete");
        delFrameBtn.addActionListener(this);


        paintAllFramesCbx = new JCheckBox("Paint all frames", false);
        paintAllFramesCbx.addItemListener(this);




        JButton newFrameCloneBtn = new JButton("New frame (copy)");
        newFrameCloneBtn.setActionCommand("frame.newclone");
        newFrameCloneBtn.addActionListener(this);

        JButton newFrameBtn = new JButton("New frame (empty)");
        newFrameBtn.setActionCommand("frame.new");
        newFrameBtn.addActionListener(this);

        prevBtn = new JButton("Previous");
        prevBtn.setActionCommand("frame.prev");
        prevBtn.addActionListener(this);

        nextBtn = new JButton("Next");
        nextBtn.setActionCommand("frame.next");
        nextBtn.addActionListener(this);

        frameLbl = new Label("          ");
        enableFrameBtns();

        return GuiUtils.flow(new Component[] {
            paintAllFramesCbx, newFrameBtn, newFrameCloneBtn, delFrameBtn,
            prevBtn, nextBtn, frameLbl
        }, 4);
    }

    /**
     * _more_
     *
     * @param idx _more_
     */
    public void setGlyphSet(int idx) {
        if ((idx < 0) || (idx >= frames.size())) {
            return;
        }
        glyphs       = (Vector) frames.elementAt(idx);
        currentFrame = idx;
        enableFrameBtns();
        repaint();
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void itemStateChanged(ItemEvent e) {
        repaint();
    }


    /**
     * _more_
     *
     * @param event _more_
     */
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals("frame.next")) {
            frameNext();
        } else if (cmd.equals("frame.paintall")) {
            repaint();
        } else if (cmd.equals("frame.prev")) {
            framePrev();
        } else if (cmd.equals("frame.newclone")) {
            newGlyphSet(true);
        } else if (cmd.equals("frame.new")) {
            newGlyphSet(false);
        } else if (cmd.equals("frame.delete")) {
            deleteGlyphSet();
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getGlyphXml() {
        StringBuffer sb = new StringBuffer();
        for (int listIdx = 0; listIdx < frames.size(); listIdx++) {
            Vector glyphs = (Vector) frames.elementAt(listIdx);
            sb.append("<message type=\"GFX\">");
            for (int i = 0; i < glyphs.size(); i++) {
                Glyph theGlyph = (Glyph) glyphs.elementAt(i);
                sb.append(
                    makeCommandTag(
                        CMD_CREATE,
                        XmlNode.attr(ATTR_ID, theGlyph.getId())
                        + XmlNode.attr(ATTR_TYPE, theGlyph.getType())
                        + theGlyph.getCreateString()));
            }
            sb.append("</message>");
        }
        return sb.toString();
    }


    /**
     * _more_
     *
     * @param node _more_
     */
    public void processMsg(XmlNode node) {
        for (int i = 0; i < node.size(); i++) {
            XmlNode child = node.get(i);
            if ( !child.tagEquals(TAG_COMMAND)) {
                continue;
            }
            processCommand(child.getAttribute(ATTR_NAME), child);
        }
    }


    /**
     * _more_
     *
     * @param commandName _more_
     * @param node _more_
     */
    public void processCommand(String commandName, XmlNode node) {
        String id = node.getAttribute(ATTR_ID);
        //    System.err.print ("CMD:" + node);

        if (commandName.equals(CMD_CREATE)) {
            if (findGlyph(id) != null) {
                return;
            }
            String type  = node.getAttribute(ATTR_TYPE);
            Glyph  glyph = createGlyph(type, 0, 0, false);
            if (glyph != null) {
                glyphCnt++;
                glyph.setRemoteInit(true);
                glyph.setId(id);
                glyph.processAttrs(node);
                glyph.setRemoteInit(false);
                addGlyph(glyph);
            }
        } else if (commandName.equals(CMD_MOVE)) {
            String          body  = ChatApplet.getBody(node);
            StringTokenizer st    = new StringTokenizer(body);
            int[]           b     = { 0, 0, 0, 0 };
            Glyph           glyph = findGlyph(id);
            if (glyph == null) {
                return;
            }
            int ptcnt = 0;
            while (st.hasMoreTokens()) {
                b[ptcnt++] = Integer.decode(st.nextToken()).intValue();
            }
            glyph.setPoints(b, ptcnt);
            repaint();
        } else if (commandName.equals(CMD_TOFRONT)) {
            Glyph glyph = findGlyph(id);
            if (glyph != null) {
                moveToFront(glyph, true);
            }
        } else if (commandName.equals(CMD_TOBACK)) {
            Glyph glyph = findGlyph(id);
            if (glyph != null) {
                moveToBack(glyph, true);
            }
        } else if (commandName.equals(CMD_CHANGE)) {
            Glyph glyph = findGlyph(id);
            if (glyph == null) {
                return;
            }
            glyph.processAttrs(node);
            repaint();
        } else if (commandName.equals(CMD_REMOVE)) {
            Glyph glyph = findGlyph(id);
            if (glyph == null) {
                return;
            }
            removeGlyph(glyph);
        } else if (commandName.equals(CMD_BACKGROUNDIMAGE)) {
            String url = node.getAttribute("url");
            if (url != null) {
                Image image = myApplet.getImage(url);
                setBackgroundImage(image, url);
            }
        }
    }



    /**
     * _more_
     *
     * @param theGlyph _more_
     * @param fromCommandProcessor _more_
     */
    public void moveToFront(Glyph theGlyph, boolean fromCommandProcessor) {
        glyphs.removeElement(theGlyph);
        glyphs.addElement(theGlyph);
        repaint(theGlyph);
    }



    /**
     * _more_
     *
     * @param theGlyph _more_
     * @param fromCommandProcessor _more_
     */
    public void moveToBack(Glyph theGlyph, boolean fromCommandProcessor) {
        glyphs.removeElement(theGlyph);
        glyphs.insertElementAt(theGlyph, 0);
        repaint(theGlyph);
    }












}

