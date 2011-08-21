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

package org.ramadda.repository.output;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.metadata.MetadataHandler;
import org.ramadda.repository.metadata.JpegMetadataHandler;
import org.ramadda.repository.type.*;

import ucar.unidata.geoloc.Bearing;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.io.File;



import java.net.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import java.awt.geom.Rectangle2D;


import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class MapOutputHandler extends OutputHandler {


    /** _more_ */
    public static final OutputType OUTPUT_MAP =
        new OutputType("Coverage Map", "map.map",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_MAP);

    public static final OutputType OUTPUT_GEMAP =
        new OutputType("Google Earth", "map.gemap",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_MAP);


    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public MapOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        if(getMapManager().showMaps()) {
            addType(OUTPUT_MAP);
            addType(OUTPUT_GEMAP);
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        boolean ok = false;
        for (Entry entry : state.getAllEntries()) {
            if (entry.hasLocationDefined() || entry.hasAreaDefined()) {
                ok = true;
                break;
            }
        }
        if (ok) {
            links.add(makeLink(request, state.getEntry(), OUTPUT_MAP));
            if(getMapManager().isGoogleEarthEnabled(request)) {
                links.add(makeLink(request, state.getEntry(), OUTPUT_GEMAP));
            }

        }
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
        List<Entry> entriesToUse = new ArrayList<Entry>();
        entriesToUse.add(entry);
        StringBuffer sb = new StringBuffer();
        MapInfo map = getMap(request, entriesToUse, sb, 700, 500, new boolean[]{false});
        return makeLinksResult(request, msg("Map"), sb, new State(entry));
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        List<Entry> entriesToUse = new ArrayList<Entry>(subGroups);
        entriesToUse.addAll(entries);
        StringBuffer sb = new StringBuffer();
        if (entriesToUse.size() == 0) {
            sb.append("<b>Nothing Found</b><p>");
            return makeLinksResult(request, msg("Map"), sb,
                                   new State(group, subGroups, entries));
        }

        if(outputType.equals(OUTPUT_GEMAP)) {
            getMapManager().getGoogleEarth(request,  
                                           entriesToUse, sb,700,400);
            return makeLinksResult(request, msg("Google Earth"), sb, new State(group));
        }

        sb.append(
            "<table border=\"0\" width=\"100%\"><tr valign=\"top\"><td width=700>");
        boolean [] haveBearingLines = {false};
        MapInfo map =  getMap(request, entriesToUse, sb, 700, 500, haveBearingLines);
        sb.append("</td><td>");


        for (Entry entry : entriesToUse) {
            if (entry.hasLocationDefined() || entry.hasAreaDefined()) {
                sb.append(HtmlUtil.img(getEntryManager().getIconUrl(request,
                        entry)));
                sb.append(HtmlUtil.space(1));
                sb.append("<a href=\"javascript:" + map.getVariableName() +".hiliteMarker("+sqt(entry.getId()) + ");\">"
                          + entry.getName() + "</a><br>");
            }
        }
        sb.append("</td></tr></table>");
        return makeLinksResult(request, msg("Map"), sb,
                               new State(group, subGroups, entries));
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entriesToUse _more_
     * @param sb _more_
     * @param width _more_
     * @param height _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public MapInfo getMap(Request request, List<Entry> entriesToUse,
                         StringBuffer sb, int width, int height,
                          boolean []haveBearingLines)
            throws Exception {
        MapInfo map = getRepository().getMapManager().createMap(request, width, height, false);
        if(map == null) return map;
        addToMap(request, map, entriesToUse, haveBearingLines, true);

        Rectangle2D.Double bounds = getEntryManager().getBounds(entriesToUse);
        map.centerOn(bounds);
        sb.append(map.getHtml());
        return map;
    }

    public void addToMap(Request request, MapInfo map, List<Entry> entriesToUse,
                         boolean []haveBearingLines, boolean screenBigRects)
            throws Exception {
        int cnt = 0;
        for (Entry entry : entriesToUse) {
            if (entry.hasAreaDefined()) {
                cnt++;
            }
        }


        boolean makeRectangles = cnt <= 20;
        MapProperties mapProperties = new MapProperties("blue", true);

        for (Entry entry : entriesToUse) {
            String idBase = entry.getId();
            List<Metadata> metadataList = getMetadataManager().getMetadata(entry);

            if(makeRectangles) {
                boolean didMetadata= map.addSpatialMetadata(entry, metadataList);
                entry.getTypeHandler().addToMap(request, entry, map);
                if (entry.hasAreaDefined() && !didMetadata) {
                    if(!screenBigRects || Math.abs(entry.getEast()-entry.getWest()) < 90) {
                        map.addBox(entry, mapProperties);
                    }
                }
            }


            if (entry.hasLocationDefined() || entry.hasAreaDefined()) {
                StringBuffer info = new StringBuffer("<table>");
                info.append(entry.getTypeHandler().getInnerEntryContent(entry,
                                                                        request, 
                                                                        OutputHandler.OUTPUT_HTML, 
                                                                        true, false,false));

                List<String> urls = new ArrayList<String>();
                getMetadataManager().getThumbnailUrls(request,  entry, urls);
                if(urls.size()>0) {
                    info.append("<tr><td colspan=2>" +HtmlUtil.img(urls.get(0), "", " width=300 ") +"</td></tr>");
                } 
                info.append("</table>");

                double[]location;
                if (makeRectangles || !entry.hasAreaDefined()) {
                    location = entry.getLocation();
                } else {
                    location = entry.getCenter();
                }

                if (entry.getResource().isImage()) {
                    String thumbUrl = getRepository().absoluteUrl(HtmlUtil.url(
                                      request.url(repository.URL_ENTRY_GET)
                                      + "/"
                                      + getStorageManager().getFileTail(
                                          entry), ARG_ENTRYID, entry.getId(),
                                      ARG_IMAGEWIDTH, "300"));
                    info.append(HtmlUtil.img(thumbUrl,"",""));

                }
                for(Metadata metadata: metadataList) {
                    if(metadata.getType().equals(JpegMetadataHandler.TYPE_CAMERA_DIRECTION)) {
                        double dir = Double.parseDouble(metadata.getAttr1());
                        LatLonPointImpl fromPt = new LatLonPointImpl(location[0],location[1]);
                        LatLonPointImpl pt = Bearing.findPoint(fromPt,dir,0.25,null);
                        map.addLine(entry.getId(), fromPt, pt);
                        if(haveBearingLines!=null)haveBearingLines[0] = true;
                        break;
                    } 
                }


                String infoHtml= info.toString();
                infoHtml = infoHtml.replace("\r", " ");
                infoHtml = infoHtml.replace("\n", " ");
                infoHtml = infoHtml.replace("\"", "\\\"");
                infoHtml = infoHtml.replace("'", "\\'");
                String icon = getEntryManager().getIconUrl(request, entry);
                map.addMarker(entry.getId(), new LatLonPointImpl(location[0],location[1]), icon, infoHtml);
            }
        }
    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private static String qt(String s) {
        return "\"" + s + "\"";
    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private static String sqt(String s) {
        return "'" + s + "'";
    }



    public static String llp(double lat, double lon) {
        if (lat < -90) {
            lat = -90;
        }
        if (lat > 90) {
            lat = 90;
        }
        if (lon < -180) {
            lon = -180;
        }
        if (lon > 180) {
            lon = 180;
        }
        return "new OpenLayers.LonLat(" + lon + "," + lat + ")";
    }

    

}
