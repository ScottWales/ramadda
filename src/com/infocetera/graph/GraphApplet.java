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

package com.infocetera.graph;


import com.infocetera.util.GuiUtils;



import com.infocetera.util.IfcApplet;

import java.applet.*;

import java.awt.*;

import java.util.Hashtable;
import java.util.Vector;


/**
 *  Just a simple wrapper around a GraphView
 */
public class GraphApplet extends IfcApplet {

    /** _more_          */
    public static final String PARAM_DATAURL = "dataurl";

    /** _more_          */
    private String dataUrl;

    /** _more_          */
    GraphView mainView;

    /** _more_          */
    Vector views = new Vector();

    /** _more_          */
    Vector graphs = new Vector();

    /** _more_          */
    Hashtable loadedNodes = new Hashtable();

    /**
     * _more_
     */
    public GraphApplet() {}



    /**
     *  Initialize the GraphView. If there are any erros then show a Label
     *  with the error message.
     */
    public void initInner() {
        super.initInner();
        dataUrl = getFullUrl(getParameter(PARAM_DATAURL));
        System.err.println("DATA URL:" + dataUrl);
        if (dataUrl == null) {
            System.err.println("No data url provided");
        }
        Component inner = null;
        setLayout(new BorderLayout());
        try {
            mainView = new GraphView(this, true);
            views.addElement(mainView);
            inner = mainView.getContents();
            reload();
            if (getParameter("shownodelist", true)) {
                mainView.handleFunction(mainView.CMD_SHOWNODELIST, "", null,
                                        null, null);
            }
        } catch (Exception e) {
            System.err.println("Failed to create GraphView\n"
                               + e.getMessage());
            e.printStackTrace();
            inner = new Label("An error has occurred:" + e.getMessage());
        }
        add("Center", inner);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public GraphView makeView() {
        GraphView graphView = new GraphView(this, false);
        views.addElement(graphView);
        return graphView;
    }


    /**
     * _more_
     */
    public void reload() {
        graphs      = new Vector();
        loadedNodes = new Hashtable();
        for (int i = 0; i < views.size(); i++) {
            GraphView view = (GraphView) views.elementAt(i);
            view.initializeState();
        }
        mainView.loadNodes(dataUrl);
    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public boolean isNodeLoaded(String id) {
        return loadedNodes.get(id) != null;
    }

    /**
     * _more_
     *
     * @param id _more_
     */
    public void setNodeLoaded(String id) {
        loadedNodes.put(id, id);
    }

    /**
     * _more_
     *
     * @param xml _more_
     * @param fromView _more_
     */
    public void addGraph(String xml, GraphView fromView) {
        graphs.addElement(xml);
        for (int i = 0; i < views.size(); i++) {
            GraphView view = (GraphView) views.elementAt(i);
            if (view != fromView) {
                view.processGraphXml(xml);
            }
        }
    }

    /**
     * _more_
     *
     * @param view _more_
     */
    public void removeView(GraphView view) {
        views.removeElement(view);
    }

    /**
     * _more_
     */
    public void stop() {
        for (int i = 0; i < views.size(); i++) {
            ((GraphView) views.elementAt(i)).stop();
        }
        super.stop();
    }



}

