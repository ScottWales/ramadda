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

package org.ramadda.repository.map;


import org.ramadda.repository.*;
import org.ramadda.repository.output.MapOutputHandler;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.LatLonPointImpl;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;




/**
 */
public class MapInfo {
    public static final String DFLT_BOX_COLOR = "blue";

    public static final int DFLT_WIDTH = 600;
    public static final int DFLT_HEIGHT = 450;

    private Repository repository;


    private static int cnt = 0;
    private String mapVarName;
    private int width = DFLT_WIDTH;
    private int height = DFLT_HEIGHT;
    private boolean forSelection = false;

    private StringBuffer jsBuffer = null;

    private StringBuffer html = new StringBuffer();

    
    public MapInfo(Repository repository) {
        this(repository, DFLT_WIDTH, DFLT_HEIGHT);
    }

    public MapInfo(Repository repository, int width, int height) {
        this(repository, width, height, false);
    }

    public MapInfo(Repository repository, int width, int height, boolean forSelection) {
        this.repository = repository;
        this.mapVarName = "ramaddaMap" + (cnt++);
        this.width = width;
        this.height = height;
        this.forSelection = forSelection;
    }


    public void addHtml(String s) {
        html.append(s);
    }

    public void addJS(String s) {
        getJS().append(s);
    }


    private String  getMapDiv() {
        StringBuffer result = new StringBuffer();
        result.append(HtmlUtil.div("",
                                 HtmlUtil.style("border:2px #888888 solid; width:" + width
                                                + "px; height:" + height + "px") + " "
                                 + HtmlUtil.id(mapVarName)));
        result.append("\n");
        return result.toString();
    }


    public String getHtml() {
        StringBuffer result = new StringBuffer();
        result.append(html);
        result.append(getMapDiv());
        result.append(HtmlUtil.script(getJS().toString()));
        result.append("\n");
        return result.toString();
    }



    private StringBuffer getJS() {
        if(jsBuffer == null) {
            jsBuffer =new StringBuffer();
            jsBuffer.append("var " + mapVarName +" = new RepositoryMap(" +HtmlUtil.squote(mapVarName) +");\n");
            jsBuffer.append("var theMap = " + mapVarName+";\n");
            if(!forSelection) {
                jsBuffer.append("theMap.initMap(" + forSelection+");\n");
            }
        }
        return jsBuffer;
    }

    public String makeSelector(String arg, 
                                     boolean popup,
                                     String[]nwse) {

        return makeSelector(arg, popup, nwse, "","");
    }

    public String makeSelector(String arg, 
                                     boolean popup,
                                     String[]nwse,
                                     String extraLeft,
                                     String extraTop) {
        boolean doRegion = true;
        if (nwse==null) {
            nwse = new String[]{"","","",""};
        }
        String widget;
        if(nwse.length==2) doRegion = false;

        if(doRegion) {
            widget = HtmlUtil.makeLatLonBox(arg, nwse[2], nwse[0], nwse[3],
                                            nwse[1]);
        } else  {
            widget = " " +
                repository.msgLabel("Latitude") +" " 
                     + HtmlUtil.input(arg + ".latitude", nwse[0],
                                      HtmlUtil.SIZE_5 + " "
                                      + HtmlUtil.id(arg + ".latitude")) + " " + repository.msgLabel("Longitude") +" " 
                                          + HtmlUtil.input(arg + ".longitude",
                                               nwse[1],
                                                  HtmlUtil.SIZE_5 + " "
                                                      + HtmlUtil.id(arg
                                                          + ".longitude"))+" ";

        }
        String msg = HtmlUtil.italics(doRegion?
                                      repository.msg("Shift-drag to select region"):
                                      repository.msg("Click to select point"));

        StringBuffer sb = new StringBuffer();
        sb.append(msg);
        sb.append(HtmlUtil.br());
        sb.append(getMapDiv());
        if ((extraLeft != null) && (extraLeft.length() > 0)) {
            widget = widget + HtmlUtil.br() + extraLeft;
        }

        String rightSide = null;
        String clearLink = HtmlUtil.mouseClickHref(getVariableName()+ ".selectionClear();",
                                                   repository.msg("Clear"));
        String initParams = HtmlUtil.squote(arg) + "," + doRegion +"," +
            (popup
                ? "1"
                : "0");

        if (popup) {
            rightSide = HtmlUtil.space(2)+repository.makeStickyPopup(repository.msg("Select"),
                                                                     sb.toString(),
                                                                     getVariableName() + ".selectionPopupInit();") + HtmlUtil.space(2) + clearLink
                                      + HtmlUtil.space(2)
                                      + HtmlUtil.space(2) + extraTop;
        } else {
            rightSide = clearLink + HtmlUtil.space(2) 
                + HtmlUtil.br() + sb.toString();
        }

        addJS(getVariableName()+".setSelection(" + initParams+ ");\n");
        return HtmlUtil.table(new Object[] { widget, rightSide }) +
            html +
            HtmlUtil.script(getJS().toString());
    }



    public void addBox(Entry entry, MapProperties properties) {
        addBox(entry.getId(), properties,  entry.getNorth(),
               entry.getWest(),
               entry.getSouth(),
               entry.getEast());
    }

    public void addBox(String id, LatLonRect llr, MapProperties properties) {
        addBox(id, properties,  llr.getLatMax(), llr.getLonMin(), llr.getLatMin(),  llr.getLonMax());
    }


    public void addBox(String id, MapProperties properties,  double north, double west, double south, double east) {
        getJS().append("var mapBoxAttributes = {\"color\":\""+ properties.color +"\",\"selectable\": "+ properties.selectable +"};\n");
        getJS().append(mapVarName +".addBox(" + HtmlUtil.squote(id) +"," +
                       north +"," +
                       west +"," +
                       south +"," +
                       east+", mapBoxAttributes);\n");
    }


    public void addLine(String id, LatLonPointImpl fromPt, LatLonPointImpl toPt) {
        addLine(id, fromPt.getLatitude(),fromPt.getLongitude(),
                toPt.getLatitude(),toPt.getLongitude());
    }

    public void addLines(String id, double[][]pts) {
        for(int i=1;i<pts.length;i++) {
            addLine(id, pts[i-1][0], pts[i-1][1], pts[i][0],pts[i][1]);
        }
    }

    public void addLines(String id, List<double[]>pts) {
        for(int i=1;i<pts.size();i++) {
            addLine(id, pts.get(i-1)[0], pts.get(i-1)[1], pts.get(i)[0],pts.get(i)[1]);
        }
    }


    public void addLine(String id, double fromLat, double fromLon,
                        double toLat, double toLon) {
        getJS().append(mapVarName +".addLine(" + HtmlUtil.squote(id) +"," +
                       fromLat+"," + fromLon+ "," +
                       toLat+"," + toLon+");\n");
    }

    public void addMarker(String  id, LatLonPointImpl pt, String icon, String info) {
        addMarker(id, pt.getLatitude(), pt.getLongitude(), icon, info);
    }

    public void addMarker(String  id, double lat, double lon, String icon, String info) {
        getJS().append(mapVarName+".addMarker(" +
                  HtmlUtil.squote(id) +
                  "," +
                       llp(lat, lon) +
                           "," +
                       (icon==null?"null":HtmlUtil.squote(icon))+ 
                           "," +
                  HtmlUtil.squote(info) +
                  ");\n");
    }


    public void centerOn(Rectangle2D.Double bounds) {
        if(bounds!=null) {
            getJS().append("var bounds = new OpenLayers.Bounds(" +
                      bounds.getX() +"," + bounds.getY() +"," + (bounds.getX()+bounds.getWidth()) +"," +
                      (bounds.getY() + bounds.getHeight())+");\n");
            getJS().append(mapVarName+".centerOnMarkers(bounds);\n");
        } else {
            center();
        }

    }


    public void center() {
        getJS().append(mapVarName+".centerOnMarkers(null);\n");
    }

    public void centerOn(Entry entry) {
        if(entry == null) {
            center();
            return;
        }
        if(entry.hasAreaDefined()) {
            centerOn(entry.getNorth(), entry.getWest(), entry.getSouth(), entry.getEast());
        } else {
            center();
        }
    }


    public void centerOn(double north, double west, double south, double east) {
        getJS().append("var bounds = new OpenLayers.Bounds(" +
                       west +"," + south +"," + east +"," +
                       north+");\n");
        getJS().append(mapVarName+".centerOnMarkers(bounds);\n");
    }


    public String getHiliteHref(String id, String label) {
        return "<a href=\"javascript:" +getVariableName() +".hiliteMarker(" +HtmlUtil.squote(id) + ");\">"
            + label + "</a>";
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


    /**
       Set the Width property.

       @param value The new value for Width
    **/
    public void setWidth (int value) {
	width = value;
    }

    /**
       Get the Width property.

       @return The Width
    **/
    public int getWidth () {
	return width;
    }

    /**
       Set the Height property.

       @param value The new value for Height
    **/
    public void setHeight (int value) {
	height = value;
    }

    /**
       Get the Height property.

       @return The Height
    **/
    public int getHeight () {
	return height;
    }





    /**
       Set the MapVarName property.

       @param value The new value for MapVarName
    **/
    public void setMapVarName (String value) {
	mapVarName = value;
    }

    /**
       Get the MapVarName property.

       @return The MapVarName
    **/
    public String getVariableName () {
	return mapVarName;
    }


    public static class MapProperties {
        String color = DFLT_BOX_COLOR;
        boolean selectable = false;
        
        public MapProperties(String color, boolean selectable) {
            this.color = color;
            this.selectable = selectable;
        }

    }

}
