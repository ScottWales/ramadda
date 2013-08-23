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





package com.infocetera.chat;


import com.infocetera.util.*;

import java.awt.*;
import java.awt.event.*;

import java.util.Hashtable;

import javax.swing.*;


/**
 * Class PrivateMessageDialog _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class PrivateMessageDialog extends Frame implements ActionListener {

    /** _more_ */
    public static final String CMD_CLOSE = "close";

    /** _more_ */
    public static final String CMD_INPUT = "input";

    /** _more_ */
    ChatApplet applet;

    /** _more_ */
    ChatUser user;

    /** _more_ */
    public static Hashtable privateMessageDialogs = new Hashtable();

    /** _more_ */
    TextCanvas textCanvas;

    /** _more_ */
    JTextField inputFld;

    /**
     * _more_
     *
     * @param user _more_
     * @param applet _more_
     *
     * @return _more_
     */
    public static PrivateMessageDialog find(ChatUser user,
                                            ChatApplet applet) {
        PrivateMessageDialog d =
            (PrivateMessageDialog) privateMessageDialogs.get(user);
        if (d == null) {
            d = new PrivateMessageDialog(user, applet);
        } else {
            d.show();
        }

        return d;

    }

    /**
     * _more_
     *
     * @param msg _more_
     */
    public void message(String msg) {
        textCanvas.addMessage(user, msg);
    }


    /**
     * _more_
     *
     * @param user _more_
     * @param applet _more_
     */
    public PrivateMessageDialog(ChatUser user, ChatApplet applet) {
        super("Private dialog with " + user);



        this.applet = applet;
        this.user   = user;
        privateMessageDialogs.put(user, this);
        setBackground(Color.lightGray);
        Label l = new Label("Private dialog with " + user);

        textCanvas = applet.makeTextCanvas();
        textCanvas.setSize(new Dimension(400, 200));

        inputFld = new JTextField("");
        inputFld.addActionListener(this);

        JButton inputBtn = ChatApplet.makeButton("Send:", CMD_INPUT, this);
        JButton closeBtn = ChatApplet.makeButton("Close", CMD_CLOSE, this);

        GuiUtils.tmpInsets = new Insets(2, 4, 2, 2);
        Component inputPanel = GuiUtils.doLayout(new Component[] { inputBtn,
                inputFld }, 2, GuiUtils.DS_NY, GuiUtils.DS_N);
        GuiUtils.tmpInsets = new Insets(2, 4, 2, 2);
        Component body = GuiUtils.doLayout(new Component[] { l,
                textCanvas.doMakeContents(), inputPanel }, 1, GuiUtils.DS_Y,
                    GuiUtils.DS_NYN);


        setLayout(new BorderLayout());
        add("Center", body);
        add("South", GuiUtils.wrap(closeBtn));


        Point sp = GuiUtils.getScreenLocation(applet, null);
        setLocation(sp.x + 20, sp.y + 50);
        pack();
        show();
    }

    /**
     * _more_
     *
     * @param event _more_
     */
    public void actionPerformed(ActionEvent event) {
        Object obj = event.getSource();
        String cmd = event.getActionCommand();
        if (cmd.equals(CMD_CLOSE)) {
            dispose();
        } else if ((obj == inputFld) || cmd.equals(CMD_INPUT)) {
            applet.writePrivate(user, inputFld.getText());
            String msg = inputFld.getText();
            textCanvas.addMessage(user, msg);
            inputFld.setText("");
        }
    }


}
