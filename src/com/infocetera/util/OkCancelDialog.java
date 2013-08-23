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


package com.infocetera.util;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * Class OkCancelDialog _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class OkCancelDialog extends JDialog implements ActionListener {

    /** _more_ */
    public String dialogText;

    /** _more_ */
    Component contents;

    /** _more_ */
    public boolean okPressed = false;

    /** _more_ */
    Button okBtn;

    /** _more_ */
    Button cancelBtn;

    /** _more_ */
    boolean doClose = false;

    /** _more_ */
    public boolean doCancel = true;

    /**
     * _more_
     *
     * @param f _more_
     * @param contents _more_
     */
    public OkCancelDialog(JFrame f, Component contents) {
        super(f, true);
        this.contents = contents;
    }

    /**
     * _more_
     *
     * @param f _more_
     * @param contents _more_
     * @param doClose _more_
     */
    public OkCancelDialog(JFrame f, Component contents, boolean doClose) {
        super(f, true);
        this.doClose  = doClose;
        this.contents = contents;
    }

    /**
     * _more_
     *
     * @param f _more_
     * @param contents _more_
     * @param doClose _more_
     * @param modal _more_
     */
    public OkCancelDialog(JFrame f, Component contents, boolean doClose,
                          boolean modal) {
        super(f, modal);
        this.doClose  = doClose;
        this.contents = contents;
    }


    /** _more_ */
    public Dimension size;

    /**
     * _more_
     */
    public void init() {
        Component bottom;
        if (doClose) {
            okBtn  = new Button("Close");
            bottom = okBtn;
        } else {
            okBtn     = new Button("Ok");
            cancelBtn = new Button("Cancel");
            cancelBtn.addActionListener(this);
            if (doCancel) {
                bottom = GuiUtils.doLayout(new Component[] { okBtn,
                        cancelBtn }, 2, GuiUtils.DS_YY, GuiUtils.DS_N);
            } else {
                bottom = GuiUtils.doLayout(new Component[] { okBtn }, 2,
                                           GuiUtils.DS_YY, GuiUtils.DS_N);
            }
        }
        okBtn.addActionListener(this);

        setLayout(new BorderLayout());
        if (contents != null) {
            add("Center", contents);
        }

        Panel wrapper = new Panel(new FlowLayout());
        wrapper.add(bottom);
        add("South", wrapper);
        pack();
        if (size != null) {
            setSize(size);
        }
        show();
    }



    /**
     * _more_
     *
     * @param event _more_
     */
    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == okBtn) {
            okPressed = true;
            dispose();
        } else if (src == cancelBtn) {
            dispose();
        }
    }

}
