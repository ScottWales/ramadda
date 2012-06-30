/*
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

import com.infocetera.util.GuiUtils;

import java.applet.*;

import java.awt.*;

import java.io.*;

import java.net.*;




/**
 * Class TableViewApplet _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class TableViewApplet extends IfcApplet {

    /** _more_          */
    TableView tableView;

    /**
     * _more_
     */
    public TableViewApplet() {
        System.err.println("IN THERE");
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Component doMakeContents() {
        try {
            tableView = new TableView(this);
            return tableView.doMakeContents();
        } catch (Exception e) {
            e.printStackTrace();
            return new Label("An error has occurred (234):" + e);
        }
    }



}

