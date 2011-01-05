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

package ucar.unidata.repository;



import ucar.unidata.util.Counter;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.HttpServer;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.PatternFileFilter;
import ucar.unidata.util.PluginClassLoader;

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
            sb.append(HtmlUtil.cssLink(fileUrl("/openlayers/theme/default/google.css")));
            sb.append("\n");
            sb.append(HtmlUtil.cssLink(fileUrl("/openlayers/theme/default/style.css")));
            sb.append("\n");
            sb.append(HtmlUtil.importJS(fileUrl("/openlayers/OpenLayers.js")));
            sb.append("\n");
            sb.append(HtmlUtil.importJS(fileUrl("/repositorymap.js")));
            sb.append("\n");
            sb.append(HtmlUtil.importJS("http://api.maps.yahoo.com/ajaxymap?v=3.0&appid=euzuro-openlayers"));
            sb.append("\n");
            sb.append(HtmlUtil.importJS("http://dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=6.1"));
            sb.append("\n");
            sb.append(HtmlUtil.importJS("http://maps.google.com/maps/api/js?v=3.2&amp;sensor=false"));
            sb.append("\n");
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








    /**
     * _more_
     *
     * @param request _more_
     * @param arg _more_
     * @param popup _more_
     * @param extraLeft _more_
     * @param extraTop _more_
     *
     * @return _more_
     */
    public String makeMapSelector(Request request, String arg, boolean popup,
                                  String extraLeft, String extraTop) {

        return makeMapSelector(request, arg, popup, extraLeft, extraTop, null);
    }

    public String makeMapSelector(Request request, String arg, boolean popup,
                                  String extraLeft, String extraTop, double[][]marker) {
        return makeMapSelector(arg, popup, extraLeft, extraTop,
                               new String[]{
                                   request.getString(arg + "_south", ""),
                                   request.getString(arg + "_north", ""),
                                   request.getString(arg + "_east", ""),
                                   request.getString(arg + "_west", "")},marker);
    }


    /**
     * _more_
     *
     * @param arg _more_
     * @param popup _more_
     *
     * @return _more_
     */
    public String makeMapSelector(String arg, boolean popup, 
                                  String[] snew) {
        return makeMapSelector(arg, popup, "", "", snew);
    }

    /**
     * _more_
     *
     * @param arg _more_
     * @param popup _more_
     * @param extraLeft _more_
     * @param extraTop _more_
     *
     * @return _more_
     */
    public String makeMapSelector(String arg, boolean popup,
                                  String extraLeft, String extraTop,
                                  String[]snew) {

        return makeMapSelector(arg,popup, extraLeft, extraTop, snew, null);
    }


    public String makeMapSelector(String arg, boolean popup,
                                  String extraLeft, String extraTop,
                                  String[]snew,
                                  double[][]markerLatLons) {

        StringBuffer sb = new StringBuffer();
        String       widget;
        boolean doRegion = true;
        if (snew==null) {
            widget = HtmlUtil.makeLatLonBox(arg, "","","","");
        } else if (snew.length == 4) {
            widget = HtmlUtil.makeLatLonBox(arg, snew[0], snew[1], snew[2],
                                            snew[3]);
        } else {
            doRegion = false;
            widget = " Latitude: "
                     + HtmlUtil.input(arg + ".latitude", snew[0],
                                      HtmlUtil.SIZE_5 + " "
                                      + HtmlUtil.id(arg + ".latitude")) + " Longitude: "
                                          + HtmlUtil.input(arg + ".longitude",
                                              snew[1],
                                                  HtmlUtil.SIZE_5 + " "
                                                      + HtmlUtil.id(arg
                                                          + ".longitude"))+" ";
        }

        String msg = HtmlUtil.italics(doRegion?
                                      msg("Shift-drag to select region"):
                                      msg("Click to select point"));
        sb.append(msg);
        sb.append(HtmlUtil.br());


        if(!shouldShowMaps()) {
            return widget;
        }


        if ((extraLeft != null) && (extraLeft.length() > 0)) {
            widget = widget + HtmlUtil.br() + extraLeft;
        }


        String mapVarName = "selectormap";
        String rightSide = null;
        String clearLink = HtmlUtil.mouseClickHref(mapVarName + ".selectionClear();",
                               msg("Clear"));
        String initParams = HtmlUtil.squote(arg) + "," + doRegion +"," +
            (popup
                ? "1"
                : "0");

        try {
            initMap(getRepository().getTmpRequest(), mapVarName, sb, 500, 300,true);
        } catch(Exception exc) {}

        if (popup) {
            rightSide = getRepository().makeStickyPopup(msg("Select"),
                                                        sb.toString(),
                                                        mapVarName + ".selectionPopupInit();") + HtmlUtil.space(2) + clearLink
                                      + HtmlUtil.space(2)
                                      + HtmlUtil.space(2) + extraTop;
        } else {
            rightSide = clearLink + HtmlUtil.space(2) 
                + HtmlUtil.br() + sb.toString();
        }

        StringBuffer script = new StringBuffer();
        script.append(mapVarName+".setSelection(" + initParams+ ");\n");
        if(markerLatLons!=null) {
            /*
            script.append("var markerLine = new Polyline([");
            for(int i=0;i<markerLatLons[0].length;i++) {
                if(i>0)
                    script.append(",");
                script.append("new LatLonPoint(" + markerLatLons[0][i]+"," +
                              markerLatLons[1][i]+")");
            }
            script.append("]);\n");
            script.append("markerLine.setColor(\"#00FF00\");\n");
            script.append("markerLine.setWidth(3);\n");
            script.append(mapVarName +".addPolyline(markerLine);\n");
            script.append(mapVarName +".autoCenterAndZoom();\n");
            */
        }

        return HtmlUtil.table(new Object[] { widget, rightSide }) + "\n"
            + HtmlUtil.script(script.toString());

    }


}
