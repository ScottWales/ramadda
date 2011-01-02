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

package com.infocetera.tableview;

import com.infocetera.util.*;

import java.applet.*;

import java.awt.*;
import java.awt.List;

import java.awt.event.*;

import java.io.*;

import java.net.*;

import java.util.*;



/**
 * Class TableView _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class TableView extends ScrollCanvas implements ItemListener,
        ActionListener, KeyListener, MouseListener, MouseMotionListener {

    /** _more_          */
    public int topMargin = 20;

    /** _more_          */
    public int bottomMargin = 10;

    /** _more_          */
    public int leftMargin = 1;

    /** _more_          */
    public int rightMargin = 1;

    /** _more_          */
    public int rowCloseSize = 6;

    /** _more_          */
    public int rowOpenSize = 25;

    /** _more_          */
    public int columns;

    /** _more_          */
    String[] headers;


    /** _more_          */
    public String error;

    /** _more_          */
    public static final GuiUtils GU = null;

    /** _more_          */
    TableRow hilite;

    /** _more_          */
    int hiliteColumn = -1;

    /** _more_          */
    Vector rows = new Vector();


    /** _more_          */
    static Font labelFont = new Font("Dialog", Font.BOLD, 12);

    /** _more_          */
    static Font widgetFont = new Font("Dialog", 0, 12);

    /** _more_          */
    static Font smallWidgetFont = new Font("Dialog", Font.BOLD, 8);

    /** _more_          */
    public Color bgColor = new Color(51, 204, 255);

    /** _more_          */
    String dirPath;

    /** _more_          */
    TableViewApplet tableViewApplet;

    /** _more_          */
    Button floatBtn;

    /** _more_          */
    Frame floatFrame;

    /** _more_          */
    boolean floating = false;

    /** _more_          */
    Container oldParent;

    /** _more_          */
    boolean needLayout = true;



    /**
     * _more_
     *
     * @param ga _more_
     */
    public TableView(TableViewApplet ga) {
        tableViewApplet = ga;
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        dirPath = tableViewApplet.getParameter("dirpath");
        readTableData();
    }

    /**
     * _more_
     *
     * @param l _more_
     *
     * @return _more_
     */
    public Button getButton(String l) {
        Button b = new Button(l);
        b.addActionListener(this);
        b.setFont(smallWidgetFont);
        return b;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Component doMakeContents() {
        Panel mainPanel = new Panel();
        mainPanel.setLayout(new BorderLayout());
        floatBtn = getButton("Float");
        mainPanel.add("North", floatBtn);
        mainPanel.add("Center", this);
        return mainPanel;
    }


    /**
     * _more_
     *
     * @param v _more_
     * @param delimit _more_
     *
     * @return _more_
     */
    public static Vector tokenize(String v, char delimit) {
        Vector  tokens     = new Vector();
        int     crntIdx    = 0;
        boolean lastEscape = false;
        int     len        = v.length();
        int     lastIdx    = 0;
        String  tok        = "";
        for (int i = 0; i < len; i++) {
            char c = v.charAt(i);
            if ((c == delimit) && !lastEscape) {
                lastIdx = i + 1;
                tokens.addElement(tok);
                tok = "";
            } else {
                if (lastEscape && (c == delimit)) {
                    tok = tok.substring(0, tok.length() - 1);
                }
                lastEscape = (c == '\\');
                tok        += c;
            }
        }
        tokens.addElement(tok);
        return tokens;
    }


    /**
     * _more_
     */
    public void readTableData() {
        try {
            URL dataUrl = new URL(
                              tableViewApplet.getFullUrl(
                                  tableViewApplet.getParameter("dataurl")));
            URLConnection connection = dataUrl.openConnection();
            InputStream   s          = connection.getInputStream();
            byte[]        bytes      = new byte[10000];
            String        content    = "";
            while (s.available() > 0) {
                int howMany = s.read(bytes);
                content += new String(bytes, 0, howMany);
            }
            readTableData(content);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading data: " + e);
        }
    }

    /**
     * _more_
     *
     * @param content _more_
     */
    public void readTableData(String content) {
        Vector lines     = tokenize(content, ';');
        String header    = (String) lines.elementAt(0);
        Vector headerTok = tokenize(header, ',');
        headers = new String[headerTok.size()];
        columns = headers.length;
        for (int i = 0; i < headerTok.size(); i++) {
            headers[i] = (String) headerTok.elementAt(i);
        }

        for (int i = 1; i < lines.size(); i++) {
            String line = (String) lines.elementAt(i);
            line = line.trim();
            System.err.println("Line:" + line);
            Vector rowTok = tokenize(line, ',');
            if (rowTok.size() == 0) {
                continue;
            }
            TableRow row = new TableRow();
            rows.addElement(row);
            row.cols = new Object[rowTok.size()];
            for (int c = 0; c < rowTok.size(); c++) {
                row.cols[c] = rowTok.elementAt(c);
            }
        }
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void itemStateChanged(ItemEvent e) {
        //    if (e.getSource () == edgeLabelCB) {
    }

    /**
     * _more_
     */
    public void layoutRows() {
        needLayout = false;

        int       crntY          = topMargin;
        int       crntX          = leftMargin;
        Rectangle b              = bounds();
        int       availableWidth = b.width - leftMargin - rightMargin;
        for (int i = rows.size() - 1; i >= 0; i--) {
            TableRow row = (TableRow) rows.elementAt(i);
            row.x      = crntX;
            row.y      = crntY;
            row.width  = availableWidth;
            row.height = (row.open
                          ? rowOpenSize
                          : rowCloseSize);
            crntY      += row.height;
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
    public TableRow find(int x, int y) {
        TableRow closest = null;
        for (int i = 0; i < rows.size(); i++) {
            TableRow p = (TableRow) rows.elementAt(i);
            if (p.contains(x, y)) {
                closest = p;
                break;
            }
        }
        return closest;
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
    public void mouseEntered(MouseEvent e) {}

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
    public void mousePressed(MouseEvent e) {}

    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseReleased(MouseEvent e) {
        int      x       = translateInputX(e.getX());
        int      y       = translateInputY(e.getY());

        TableRow closest = find(x, y);
        if (closest != null) {
            closest.open = !closest.open;
            needLayout   = true;

            repaint();
        }

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
     * @return _more_
     */
    public int getRowWidth() {
        return bounds().width - leftMargin - rightMargin;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getColumnWidth() {
        return getRowWidth() / columns;
    }


    /**
     * _more_
     *
     * @param x _more_
     *
     * @return _more_
     */
    public int xToColumn(int x) {
        return (int) (x - leftMargin) / getColumnWidth();
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void mouseMoved(MouseEvent e) {
        int      x               = translateInputX(e.getX());
        int      y               = translateInputY(e.getY());
        TableRow closest         = find(x, y);
        int      newHiliteColumn = xToColumn(x);
        if ((hilite != closest) || (newHiliteColumn != hiliteColumn)) {
            hilite       = closest;
            hiliteColumn = newHiliteColumn;
            repaint();
        }
    }









    /**
     * _more_
     *
     * @param e _more_
     */
    public void actionPerformed(ActionEvent e) {
        try {
            Container contents = null;
            if (e.getSource() == floatBtn) {
                //      print ("btn:" + floating);
                if (floating) {
                    floatBtn.setLabel("Float");
                    oldParent.add("Center", contents);
                    floatFrame.dispose();
                    invalidate();
                    oldParent.validate();
                    floating = false;
                } else {
                    try {
                        oldParent = contents.getParent();
                        floatBtn.setLabel("Embed");
                        floatFrame = new Frame();
                        floatFrame.setLayout(new BorderLayout());
                        floatFrame.add("Center", contents);
                        floatFrame.pack();
                        floatFrame.show();
                        floating = true;
                    } catch (Exception exc) {
                        print("err:" + exc);
                    }
                }
                return;
            }

            /**
             *     if (e.getSource () == bBtn) {
             *       GraphNode n = (GraphNode) history.elementAt (historyIdx-1);
             *       nodeSelect (n, false, -1);
             *     }    else  if (e.getSource () == fBtn) {
             *       GraphNode n = (GraphNode) history.elementAt (historyIdx+1);
             *       nodeSelect (n, false, 1);
             *     } else  if (e.getSource () == ffBtn) {
             *       historyIdx = history.size () -1;
             *       GraphNode n = (GraphNode) history.elementAt (historyIdx);
             *       nodeSelect (n, false, 0);
             *     } else  if (e.getSource () == bbBtn) {
             *       historyIdx = 0;
             *       GraphNode n = (GraphNode) history.elementAt (0);
             *       nodeSelect (n, false, 0);
             *     } else if (e.getSource () == scrEqBtn) {
             *       hTrans =0;
             *       vTrans =0;
             *       repaint ();
             *     } else if (e.getSource () == scrLtBtn) {
             *       hTrans +=10;
             *       repaint ();
             *     } else if (e.getSource () == scrRtBtn) {
             *       hTrans -=10;
             *       repaint ();
             *     } else if (e.getSource () == scrUpBtn) {
             *       vTrans +=10;
             *       repaint ();
             *     } else if (e.getSource () == scrDnBtn) {
             *       vTrans -=10;
             *       repaint ();
             *     } else if (e.getSource () == zoomInBtn) {
             *       rescale (1);
             *     } else if (e.getSource () == zoomEqBtn) {
             *       rescale (0);
             *     } else if (e.getSource () == zoomOutBtn) {
             *       rescale (-1);
             *     }
             */
        } catch (Exception exc) {}
    }


    /**
     * _more_
     *
     * @param e _more_
     */
    public void keyPressed(KeyEvent e) {
        boolean changed = true;
        if (e.getKeyCode() == e.VK_UP) {
            vTrans += 10;
        } else if (e.getKeyCode() == e.VK_DOWN) {
            vTrans -= 10;
            //    else if (e.getKeyCode () == e.VK_RIGHT)
            //      hTrans -= 10;
            //    else if (e.getKeyCode () == e.VK_LEFT)
            //      hTrans += 10;
        } else if (e.getKeyChar() == '=') {
            hTrans = 0;
            vTrans = 0;
        } else if (e.getKeyCode() == e.VK_ENTER) {
            return;
        } else {
            changed = false;
        }
        if (changed) {
            repaint();
        }

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




    /** _more_          */
    public Image bufferedImage = null;

    /** _more_          */
    public int bufferWidth;

    /** _more_          */
    public int bufferHeight;

    /**
     * _more_
     *
     * @param g _more_
     */
    public synchronized void update(Graphics g) {
        Rectangle b = bounds();
        if ((bufferedImage == null) || (bufferWidth != b.width)
                || (bufferHeight != b.height)) {
            if ((b.width <= 0) || (b.height <= 0)) {
                return;
            }
            bufferWidth   = b.width;
            bufferHeight  = b.height;
            bufferedImage = createImage(b.width, b.height);
        }

        Graphics bufferedGraphics = bufferedImage.getGraphics();
        bufferedGraphics.setColor(bgColor);
        bufferedGraphics.fillRect(0, 0, bufferWidth + 4, bufferHeight + 4);
        bufferedGraphics.translate(hTrans, vTrans);
        paint(bufferedGraphics);
        g.drawImage(bufferedImage, 0, 0, Color.white, null);
    }



    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintHeader(Graphics g) {
        int columnWidth = getColumnWidth();
        int myWidth     = columnWidth - 1;
        int myHeight    = topMargin - 1;
        int crntX       = leftMargin;
        for (int c = 0; c < headers.length; c++) {
            g.setColor(Color.lightGray);
            g.fillRect(crntX, 0, myWidth, myHeight);
            g.setColor(Color.black);
            g.drawLine(crntX + myWidth, 0, crntX + myWidth, myHeight);
            g.drawLine(crntX, myHeight, crntX + myWidth, myHeight);
            g.setColor(Color.white);
            g.drawLine(crntX, 0, crntX, myHeight);
            g.drawLine(crntX, 0, crntX + myWidth, 0);
            g.setColor(Color.black);
            g.drawString(headers[c], crntX + 2, myHeight - 2);
            crntX += columnWidth;
        }

    }


    /**
     * _more_
     *
     * @param g _more_
     */
    public void paintIt(Graphics g) {
        if (needLayout) {
            layoutRows();
        }
        if (error != null) {
            g.setColor(Color.red);
            g.drawString(error, 20, 20);
            return;
        }

        int       crntY = topMargin;
        int       crntX = leftMargin;
        Rectangle b     = bounds();
        g.setColor(Color.white);
        g.fillRect(0, 0, b.width, b.height);

        paintHeader(g);
        int rowWidth    = getRowWidth();
        int columnWidth = getColumnWidth();
        for (int i = 0; i < rows.size(); i++) {
            TableRow row = (TableRow) rows.elementAt(i);
            row.paint(g, hilite, hiliteColumn, columnWidth);
        }
    }

    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     */
    public Image getImage(String url) {
        try {
            //            url = tableViewApplet.getUrlPrefix() + url;
            System.err.println(url);
            URL imageUrl = new URL(url);
            return tableViewApplet.getImage(imageUrl);
        } catch (Exception exc) {
            System.err.println("setImage " + url + "\n" + exc);
        }
        return null;

    }


    /**
     * _more_
     *
     * @param gif _more_
     * @param c _more_
     *
     * @return _more_
     */
    public Image getImage(String gif, Class c) {
        try {
            InputStream imageStream = c.getResourceAsStream(gif);
            if (imageStream == null) {
                return null;
            }
            int    length           = imageStream.available();
            byte[] thanksToNetscape = new byte[length];
            imageStream.read(thanksToNetscape);
            return Toolkit.getDefaultToolkit().createImage(thanksToNetscape);
        } catch (Exception exc) {
            print(exc + " getting resource ");
        }
        return null;
    }



    /**
     * _more_
     *
     * @param s _more_
     */
    public void print(String s) {
        System.err.println(s);
    }






}

