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

