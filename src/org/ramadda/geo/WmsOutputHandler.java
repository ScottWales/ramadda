/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
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

package org.ramadda.geo;


import org.w3c.dom.*;

import ucar.unidata.repository.*;
import ucar.unidata.repository.output.*;
import ucar.unidata.data.GeoLocationInfo;
import ucar.unidata.data.gis.WmsSelection;
import ucar.unidata.xml.XmlUtil;

import ucar.unidata.util.WmsUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;



/**
 * This class handles WMS Capabilities URLs. It loads the XML and generates a web page listing
 * each of the layers
 */
public class WmsOutputHandler extends OutputHandler {

    /** example1 */
    public static final OutputType OUTPUT_WMS_VIEWER =
        new OutputType("WMS Map View", "wms.viewer", OutputType.TYPE_HTML);



    /** 
        Caches the DOMS from the url
        TODO: Expire the cache after some time so we would pick up any changes to the wms xml
     */
    private Hashtable<String, Element> wmsCache = new Hashtable<String,
                                                    Element>();



    /**
     * Constructor
     *
     * @param repository The repository
     * @param element The xml element from outputhandlers.xml
     * @throws Exception On badness
     */
    public WmsOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        //add in the output types
        addType(OUTPUT_WMS_VIEWER);
    }





    /**
     * This method gets called to add in to the types list the OutputTypes that are applicable
     * to the given State.  The State can be viewing a single Entry (state.entry non-null),
     * viewing a Group (state.group non-null). These would show up along the top navigation bar.
     *
     * The Request holds all information about the request
     *
     * @param request The request
     * @param state The state
     * @param links _more_
     *
     *
     * @throws Exception On badness
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if (state.entry != null) {
            if(state.entry.getType().equals("wms.capabilities")) {
                links.add(makeLink(request, state.entry, OUTPUT_WMS_VIEWER));
            }
        }
    }



    /**
     * This reads the WMS capabilities for the given entry. It caches the DOM
     *
     * @param entry the entry
     *
     * @return The WMS DOM
     *
     * @throws Exception On badness
     */
    private Element getWmsRoot(Entry entry) throws Exception {
        String  wmsUrl = entry.getResource().getPath();
        Element root   = wmsCache.get(wmsUrl);
        if (root == null) {
            root = XmlUtil.getRoot(wmsUrl, getClass());
            if (wmsCache.size() > 10) {
                wmsCache = new Hashtable<String, Element>();
            }
            //TODO: Expire the cache after some time so we would pick up any changes to the wms xml
            wmsCache.put(wmsUrl, root);
        }
        return root;
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        StringBuffer sb   = new StringBuffer();
        Element      root = getWmsRoot(entry);
        Element capabilityNode = XmlUtil.findDescendant(root,
                                     WmsUtil.TAG_CAPABILITY);
        Element topLevelLayer = XmlUtil.findDescendant(capabilityNode,
                                    WmsUtil.TAG_LAYER);
        if (topLevelLayer == null) {
            sb.append("No top level layer found");
            return new Result("", sb);
        }
        String title = XmlUtil.getGrandChildText(topLevelLayer,
                           WmsUtil.TAG_TITLE);
        sb.append(header(title));
        List<Element> layerNodes =
            (List<Element>) XmlUtil.findChildren(topLevelLayer,
                WmsUtil.TAG_LAYER);
        String[] message = new String[] { null };
        for (Element layerNode : layerNodes) {
            List<Element> styles = XmlUtil.findChildren(layerNode,
                                       WmsUtil.TAG_STYLE);
            styles.add(0, layerNode);
            List<WmsSelection> layers = WmsUtil.processNode(root, styles,
                                            message, false);
            if (message[0] != null) {
                sb.append(message[0]);
                sb.append("<br>");
                continue;
            }
            StringBuffer layerSB = new StringBuffer("<ul>");
            for (int i = 1; i < layers.size(); i++) {
                WmsSelection wms = layers.get(i);
                layerSB.append("<li>");
                layerSB.append(getHref(wms));
            }
            layerSB.append("</ul>");
            sb.append(HtmlUtil.makeShowHideBlock(layers.get(0).getTitle(),
                    layerSB.toString(), false));
        }
        return new Result("", sb);
    }


    /**
     * _more_
     *
     * @param wms _more_
     *
     * @return _more_
     */
    public String getHref(WmsSelection wms) {
        String href = HtmlUtil.href(getUrl(wms), wms.getTitle());
        return href;
    }


    /**
     * _more_
     *
     * @param wms _more_
     *
     * @return _more_
     */
    public String getUrl(WmsSelection wms) {
        double width  = wms.getBounds().getDegreesX();
        double height = wms.getBounds().getDegreesY();
        String url = wms.assembleRequest(wms.getBounds(), 600,
                                         (int) (600 * height / width));
        return url;
    }



}
