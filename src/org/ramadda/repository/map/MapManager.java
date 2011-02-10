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

package org.ramadda.repository.map;


import org.ramadda.repository.*;
import org.ramadda.repository.output.MapOutputHandler;


import ucar.unidata.util.Counter;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.HttpServer;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.PatternFileFilter;
import ucar.unidata.util.PluginClassLoader;

import ucar.unidata.geoloc.LatLonRect;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;

import java.util.ArrayList;
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



    public boolean shouldShowMaps() {
        return  getRepository().getProperty(PROP_SHOWMAP, true);
    }


    public void initMap(Request request, String mapVarName,
                          StringBuffer sb, int width, int height,
                          boolean forSelection) {
        if(!shouldShowMaps()) {
            return;
        }

        if (request.getExtraProperty("initmap") == null) {
            sb.append("\n");
            //            sb.append(HtmlUtil.cssLink(fileUrl("/openlayers/theme/default/google.css")));
            //            sb.append("\n");
            sb.append(HtmlUtil.cssLink(fileUrl("/openlayers/theme/default/style.css")));
            sb.append("\n");
            sb.append(HtmlUtil.importJS(fileUrl("/openlayers/OpenLayers.js")));
            sb.append("\n");
            sb.append(HtmlUtil.importJS(fileUrl("/ramaddamap.js")));
            sb.append("\n");
            sb.append(HtmlUtil.importJS("http://api.maps.yahoo.com/ajaxymap?v=3.0&appid=euzuro-openlayers"));
            sb.append("\n");
            //            sb.append(HtmlUtil.importJS("http://dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=6.1"));
            //            sb.append("\n");
            //            sb.append(HtmlUtil.importJS("http://maps.google.com/maps/api/js?v=3.2&amp;sensor=false"));
            //            sb.append("\n");
            request.putExtraProperty("initmap", "");
        }

        sb.append(HtmlUtil.div("",
                               HtmlUtil.style("border:2px #888888 solid; width:" + width
                                   + "px; height:" + height + "px") + " "
                                       + HtmlUtil.id(mapVarName)));
        sb.append("\n");
        StringBuffer js = new StringBuffer();
        js.append("var " + mapVarName +" = new RepositoryMap('" +mapVarName +"');\n");
        js.append("var map = " + mapVarName+";\n");
        if(!forSelection) {
            js.append("map.initMap(" + forSelection+");\n");
        }
        sb.append(HtmlUtil.script(js.toString()));
        sb.append("\n");
    }



    public MapInfo createMap(Request request, boolean forSelection) {
        return createMap(request, MapInfo.DFLT_WIDTH, MapInfo.DFLT_HEIGHT, forSelection);
    }


    public MapInfo createMap(Request request, int width, int height,
                             boolean forSelection) {
        if(!shouldShowMaps()) {
            //            return null;
        }

        MapInfo mapInfo = new MapInfo(getRepository(), width, height, forSelection);
        
        if (request.getExtraProperty("initmap") == null) {
            mapInfo.addHtml(HtmlUtil.cssLink(fileUrl("/openlayers/theme/default/style.css")));
            mapInfo.addHtml("\n");
            mapInfo.addHtml(HtmlUtil.importJS(fileUrl("/openlayers/OpenLayers.js")));
            mapInfo.addHtml("\n");
            mapInfo.addHtml(HtmlUtil.importJS(fileUrl("/ramaddamap.js")));
            mapInfo.addHtml("\n");
            mapInfo.addHtml(HtmlUtil.importJS("http://api.maps.yahoo.com/ajaxymap?v=3.0&appid=euzuro-openlayers"));
            mapInfo.addHtml("\n");
            //mapInfo.addHtml(HtmlUtil.importJS("http://dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=6.1"));
            //mapInfo.addHtml("\n");
            //mapInfo.addHtml(HtmlUtil.importJS("http://maps.google.com/maps/api/js?v=3.2&amp;sensor=false"));
            request.putExtraProperty("initmap", "");
        }

        return mapInfo;
    }

}
