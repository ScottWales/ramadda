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

package org.ramadda.repository.map;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.MapOutputHandler;
import org.ramadda.repository.output.OutputHandler;

import ucar.unidata.geoloc.LatLonRect;


import ucar.unidata.util.Counter;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.PatternFileFilter;
import ucar.unidata.util.PluginClassLoader;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;





/**
 * This class provides a variety of mapping services, e.g., map display and map form selector
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */
public class MapManager extends RepositoryManager {

    /**
     * _more_
     *
     * @param repository _more_
     */
    public MapManager(Repository repository) {
        super(repository);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean shouldShowMaps() {
        return showMaps();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean showMaps() {
        return getRepository().getProperty(PROP_SHOWMAP, true);
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param forSelection _more_
     *
     * @return _more_
     */
    public MapInfo createMap(Request request, boolean forSelection) {
        return createMap(request, MapInfo.DFLT_WIDTH, MapInfo.DFLT_HEIGHT,
                         forSelection);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param width _more_
     * @param height _more_
     * @param forSelection _more_
     *
     * @return _more_
     */
    public MapInfo createMap(Request request, int width, int height,
                             boolean forSelection) {


        MapInfo mapInfo = new MapInfo(getRepository(), width, height,
                                      forSelection);

        if ( !showMaps()) {
            return mapInfo;
        }

        if (request.getExtraProperty("initmap") == null) {
            mapInfo.addHtml(
                HtmlUtil.cssLink(
                    fileUrl("/openlayers/theme/default/style.css")));
            mapInfo.addHtml("\n");
            mapInfo.addHtml(
                HtmlUtil.importJS(fileUrl("/openlayers/OpenLayers.js")));
            mapInfo.addHtml("\n");
            mapInfo.addHtml(
                HtmlUtil.importJS(
                    "http://api.maps.yahoo.com/ajaxymap?v=3.0&appid=euzuro-openlayers"));
            mapInfo.addHtml("\n");
            //            mapInfo.addHtml(HtmlUtil.importJS("http://dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=6.1"));
            //            mapInfo.addHtml(HtmlUtil.importJS("http://maps.google.com/maps/api/js?v=3.2&amp;sensor=false"));
            mapInfo.addHtml(HtmlUtil.importJS(fileUrl("/ramaddamap.js")));
            mapInfo.addHtml("\n");
            request.putExtraProperty("initmap", "");
        }

        return mapInfo;
    }


    /** _more_ */
    private List<List<String>> geKeys;

    /**
     * _more_
     *
     * @param request _more_
     */
    public void applyAdminConfig(Request request) {
        geKeys = null;
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public boolean isGoogleEarthEnabled(Request request) {
        return getGoogleMapsKey(request) != null;
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public String[] getGoogleMapsKey(Request request) {
        if (geKeys == null) {
            String geAPIKeys = getProperty(PROP_GOOGLEAPIKEYS, null);
            if ((geAPIKeys == null) || (geAPIKeys.trim().length() == 0)) {
                return null;
            }
            List<List<String>> tmpKeys = new ArrayList<List<String>>();
            for (String line :
                    StringUtil.split(geAPIKeys, "\n", true, true)) {
                List<String> toks = StringUtil.split(line, ";", true, false);
                if (toks.size() > 1) {
                    tmpKeys.add(toks);
                }
            }
            geKeys = tmpKeys;
        }
        String hostname = request.getServerName();
        int    port     = request.getServerPort();
        String hostnameWithPort = hostname + ":" + port;
        for (String h : new String[] {hostnameWithPort, hostname}) {
            // System.err.println("hostname:" + hostname);
            for (List<String> tuple : geKeys) {
                String server = tuple.get(0);
                // check to see if this matches me 
                //            System.err.println("    server:" + server);
                if (server.equals("*") || hostname.endsWith(h)) {
                    String mapsKey = tuple.get(1);
                    if (tuple.size() > 2) {
                        return new String[] { mapsKey, tuple.get(2) };
                    } else {
                        return new String[] { mapsKey, null };
                    }
                }
            }
        }
        return null;
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param width _more_
     * @param height _more_
     * @param url _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getGoogleEarthPlugin(Request request, Appendable sb,
                                       int width, int height, String url)
            throws Exception {


        String[] keyAndOther = getGoogleMapsKey(request);
        if (keyAndOther == null) {
            sb.append("Google Earth is not enabled");
            return null;
        }

        String otherOpts = "";
        String mapsKey   = "";
        if ( !keyAndOther[0].isEmpty()) {
            mapsKey = "?key=" + keyAndOther[0];
        }
        if (keyAndOther[1] != null) {
            otherOpts = ", {\"other_params\":\"" + keyAndOther[1] + "\"}";
        }
        Integer currentId = (Integer) request.getExtraProperty("ge.id");
        int     nextNum   = 1;
        if (currentId != null) {
            nextNum = currentId.intValue() + 1;
        }
        request.putExtraProperty("ge.id", new Integer(nextNum));
        String id = "map3d" + nextNum;

        if (request.getExtraProperty("initgooglearth") == null) {
            sb.append(HtmlUtil.importJS("http://www.google.com/jsapi"
                                        + mapsKey));
            sb.append(HtmlUtil.importJS(fileUrl("/googleearth.js")));
            sb.append(HtmlUtil.script("google.load(\"earth\", \"1\""
                                      + otherOpts + ");"));
            request.putExtraProperty("initgooglearth", "");
        }


        String template =
            "<div id=\"${id}_container\" style=\"border: 1px solid #888; width: ${width}px; height: ${height}px;\"><div id=\"${id}\" style=\"height: 100%;\"></div></div>";

        template = template.replace("${width}", width + "");
        template = template.replace("${height}", height + "");
        template = template.replace("${id}", id);
        template = template.replace("${id}", id);

        sb.append(HtmlUtil.checkbox("tmp", "true", false,
                                    HtmlUtil.id("googleearth.showdetails")));
        sb.append(" ");
        sb.append(msg("Show details"));

        sb.append(template);
        sb.append(HtmlUtil.script("var  " + id + " = new GoogleEarth("
                                  + HtmlUtil.squote(id) + ", "
                                  + ((url == null)
                                     ? "null"
                                     : HtmlUtil.squote(url)) + ");\n"));
        return id;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     * @param sb _more_
     * @param width _more_
     * @param height _more_
     *
     * @throws Exception _more_
     */
    public void getGoogleEarth(Request request, List<Entry> entries,
                               StringBuffer sb, int width, int height)
            throws Exception {
        sb.append(
            "<table border=\"0\" width=\"100%\"><tr valign=\"top\"><td>");

        StringBuffer mapSB = new StringBuffer();

        String id = getMapManager().getGoogleEarthPlugin(request, mapSB,
                        width, height, null);

        StringBuffer js         = new StringBuffer();
        List<String> categories = new ArrayList<String>();
        Hashtable<String, StringBuffer> catMap = new Hashtable<String,
                                                     StringBuffer>();
        for (Entry entry : entries) {
            if ( !(entry.hasLocationDefined() || entry.hasAreaDefined())) {
                continue;
            }
            String       category = entry.getTypeHandler().getCategory(entry);
            StringBuffer catSB    = catMap.get(category);
            if (catSB == null) {
                catMap.put(category, catSB = new StringBuffer());
                categories.add(category);
            }
            catSB.append("&nbsp;&nbsp;");
            catSB.append(HtmlUtil.img(getEntryManager().getIconUrl(request,
                    entry)));
            catSB.append(HtmlUtil.space(1));
            double lat = entry.getSouth();
            double lon = entry.getEast();
            catSB.append("<a href=\"javascript:" + id + ".placemarkClick("
                         + HtmlUtil.squote(entry.getId()) + ");\">"
                         + entry.getName() + "</a><br>");
            String icon = getRepository().absoluteUrl(
                              getEntryManager().getIconUrl(request, entry));
            String  pointsString = "null";
            boolean hasPolygon   = false;
            List<Metadata> metadataList =
                getMetadataManager().getMetadata(entry);
            for (Metadata metadata : metadataList) {
                if ( !metadata.getType().equals(
                        MetadataHandler.TYPE_SPATIAL_POLYGON)) {
                    continue;
                }
                List<double[]> points   = new ArrayList<double[]>();
                String         s        = metadata.getAttr1();
                StringBuffer   pointsSB = new StringBuffer();
                for (String pair : StringUtil.split(s, ";", true, true)) {
                    List<String> toks = StringUtil.splitUpTo(pair, ",", 2);
                    if (toks.size() != 2) {
                        continue;
                    }
                    double polyLat = Misc.decodeLatLon(toks.get(0));
                    double polyLon = Misc.decodeLatLon(toks.get(1));
                    if (pointsSB.length() == 0) {
                        pointsSB.append("new Array(");
                    } else {
                        pointsSB.append(",");
                    }
                    pointsSB.append(polyLat);
                    pointsSB.append(",");
                    pointsSB.append(polyLon);
                }
                hasPolygon = true;
                pointsSB.append(")");
                pointsString = pointsSB.toString();
            }

            if ( !hasPolygon && entry.hasAreaDefined()) {
                pointsString = "new Array(" + entry.getNorth() + ","
                               + entry.getWest() + "," + entry.getNorth()
                               + "," + entry.getEast() + ","
                               + entry.getSouth() + "," + entry.getEast()
                               + "," + entry.getSouth() + ","
                               + entry.getWest() + "," + entry.getNorth()
                               + "," + entry.getWest() + ")";
            }
            String desc = makeInfoBubble(request, entry);
            js.append(
                HtmlUtil.call(
                    id + ".addPlacemark",
                    HtmlUtil.comma(
                        HtmlUtil.squote(entry.getId()),
                        HtmlUtil.squote(entry.getName()),
                        HtmlUtil.squote(desc), "" + lat, "" + lon) + ","
                            + HtmlUtil.squote(icon) + "," + pointsString));
            js.append("\n");
        }

        sb.append(HtmlUtil.open(HtmlUtil.TAG_DIV, 
                                HtmlUtil.cssClass("ramadda-earth-entries")+
                                HtmlUtil.style("max-height:" + height +"px; overflow-y: auto;")));
        for (String category : categories) {
            StringBuffer catSB = catMap.get(category);
            sb.append(HtmlUtil.b(category));
            sb.append(HtmlUtil.br());
            sb.append(catSB);
        }
        sb.append(HtmlUtil.close(HtmlUtil.TAG_DIV));


        sb.append("</td><td>");
        sb.append(mapSB);
        sb.append(HtmlUtil.script(js.toString()));
        sb.append("</td></tr></table>");
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
    public String makeInfoBubble(Request request, Entry entry)
            throws Exception {
        String fromEntry = entry.getTypeHandler().getMapInfoBubble(request,
                               entry);
        if (fromEntry != null) {
            return fromEntry;
        }
        StringBuffer info = new StringBuffer("<table>");
        info.append(entry.getTypeHandler().getInnerEntryContent(entry,
                request, OutputHandler.OUTPUT_HTML, true, false, false));

        List<String> urls = new ArrayList<String>();
        getMetadataManager().getThumbnailUrls(request, entry, urls);
        if (urls.size() > 0) {
            info.append("<tr><td colspan=2>"
                        + HtmlUtil.img(urls.get(0), "", " width=300 ")
                        + "</td></tr>");
        }
        info.append("</table>");

        if (entry.getResource().isImage()) {
            String thumbUrl = getRepository().absoluteUrl(
                                  HtmlUtil.url(
                                      request.url(repository.URL_ENTRY_GET)
                                      + "/"
                                      + getStorageManager().getFileTail(
                                          entry), ARG_ENTRYID, entry.getId(),
                                              ARG_IMAGEWIDTH, "300"));
            info.append(HtmlUtil.img(thumbUrl, "", ""));

        }


        String infoHtml = info.toString();
        infoHtml = infoHtml.replace("\r", " ");
        infoHtml = infoHtml.replace("\n", " ");
        infoHtml = infoHtml.replace("\"", "\\\"");
        infoHtml = infoHtml.replace("'", "\\'");
        return infoHtml;
    }


}
