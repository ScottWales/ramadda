/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
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
 * 
 */

package org.ramadda.repository.data;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.awt.geom.Rectangle2D;

import java.io.File;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GridAggregationTypeHandler extends ExtensibleGroupTypeHandler {

    /** _more_          */
    public static final String TYPE_GRIDAGGREGATION = "gridaggregation";


    public static final String TYPE_JOINEXISTING ="joinExisting";
    public static final String TYPE_JOINNEW ="joinNew";
    public static final String TYPE_UNION ="union";

    
    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception _more_
     */
    public GridAggregationTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
        getRepository().getHarvesterManager().addHarvesterType(
            GridAggregationHarvester.class);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public File getNcmlFile(Request request, Entry entry) throws Exception {
        if(request == null) {
            request = getRepository().getTmpRequest();
        }
        StringBuffer sb = new StringBuffer();
        String type = entry.getValue(1, TYPE_JOINEXISTING);
        String typeToUse = TYPE_JOINEXISTING;
        if(type.equalsIgnoreCase(TYPE_UNION)) 
            typeToUse = TYPE_UNION;
        else if(type.equalsIgnoreCase(TYPE_JOINNEW)) 
            typeToUse = TYPE_JOINNEW;

        sb.append(
            "<netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\">\n");
        if(typeToUse.equals(TYPE_JOINEXISTING)) {
            sb.append("<aggregation type=\"joinExisting\" dimName=\""
                      + entry.getValue(0, "time")
                      + "\" timeUnitsChange=\"true\">\n");
        } else if(typeToUse.equals(TYPE_UNION)) {
            sb.append("<aggregation type=\"union\" >");
        } else {
            //TODO: figure this out.

        }
        List<String> sortedChillens = new ArrayList<String>();
        for (Entry child :
                getRepository().getEntryManager().getChildren(request,
                    entry)) {
            if (child.getType().equals(GridAggregationTypeHandler.TYPE_GRIDAGGREGATION)) {
                //TODO: aggregation of aggregations
                continue;
            }
            sortedChillens.add(child.getResource().getPath());
        }
        if (typeToUse.equals(TYPE_JOINEXISTING)) {
        	Collections.sort(sortedChillens);
        }
        for (String s : sortedChillens) {

            sb.append(XmlUtil.tag("netcdf",
                                  XmlUtil.attrs("location",
                                      IOUtil.getURL(s,
                                          getClass()).toString(), "enhance",
                                              "true"), ""));
        }
        sb.append("</aggregation>\n</netcdf>\n");
        System.err.println(sb);

        File tmpFile =
            getRepository().getStorageManager().getTmpFile(request,
                "grid.ncml");
        IOUtil.writeFile(tmpFile, sb.toString());
        return tmpFile;
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param isNew _more_
     *
     * @throws Exception _more_
     */
    public void childEntryChanged(Entry entry, boolean isNew)
            throws Exception {
        super.childEntryChanged(entry, isNew);
        Entry parent = entry.getParentEntry();
        List<Entry> children =
            getEntryManager().getChildren(getRepository().getTmpRequest(),
                                          parent);
        //For good measure
        children.add(entry);
        getEntryManager().setBoundsOnEntry(parent, children);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public List<Service> getServices(Request request, Entry entry) {
        List<Service> services = super.getServices(request, entry);
        /*
        String url =
            HtmlUtil.url(request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                          entry), new String[] {
            ARG_OUTPUT, LidarOutputHandler.OUTPUT_LATLONALTCSV.toString(),
            LidarOutputHandler.ARG_LIDAR_SKIP,
            macro(LidarOutputHandler.ARG_LIDAR_SKIP), ARG_BBOX,
            macro(ARG_BBOX),
        }, false);
        services.add(new Service("pointcloud", "Point Cloud",
                                 getRepository().absoluteUrl(url),
                                 getIconUrl(LidarOutputHandler.ICON_POINTS)));
        */
        return services;
    }



}
