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


import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.metadata.MetadataHandler;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;



/**
 * A MapInfo class to hold map info
 */
public class MapInfo {

    /** default box color */
    public static final String DFLT_BOX_COLOR = "blue";

    /** default map width */
    public static final int DFLT_WIDTH = 600;

    /** default map height */
    public static final int DFLT_HEIGHT = 300;

    /** The associated repository */
    private Repository repository;

    /** the map count */
    private static int cnt = 0;

    /** the map variable name */
    private String mapVarName;

    /** the width */
    private int width = DFLT_WIDTH;

    /** the height */
    private int height = DFLT_HEIGHT;

    /** is the map for selection */
    private boolean forSelection = false;

    /** the javascript buffer? */
    private StringBuffer jsBuffer = null;

    /** the html */
    private StringBuffer html = new StringBuffer();


    /**
     * Create a MapInfo for the associated repository
     *
     * @param repository the associated repository
     */
    public MapInfo(Repository repository) {
        this(repository, DFLT_WIDTH, DFLT_HEIGHT);
    }

    /**
     * Create a MapInfo for the associated repository
     *
     * @param repository the associated repository
     * @param width  the width of the map
     * @param height  the height of the map
     */
    public MapInfo(Repository repository, int width, int height) {
        this(repository, width, height, false);
    }

    /**
     * Create a MapInfo for the associated repository
     *
     * @param repository the associated repository
     * @param width  the width of the map
     * @param height  the height of the map
     * @param forSelection  true if for selecting something
     */
    public MapInfo(Repository repository, int width, int height,
                   boolean forSelection) {
        this.repository   = repository;
        this.mapVarName   = "ramaddaMap" + (cnt++);
        this.width        = width;
        this.height       = height;
        this.forSelection = forSelection;
    }


    /**
     * Shortcut to repository.msg
     *
     * @param s  the string
     *
     * @return  the translated message
     */
    public String msg(String s) {
        return repository.msg(s);
    }


    /**
     * Shortcut to repository.msgLabel
     *
     * @param s  the string
     *
     * @return  the label
     */
    public String msgLabel(String s) {
        return repository.msgLabel(s);
    }

    /**
     * Show the maps
     *
     * @return  true if successful
     */
    public boolean showMaps() {
        return repository.getMapManager().showMaps();
    }


    /**
     * Add the html to the output
     *
     * @param s  the string
     */
    public void addHtml(String s) {
        html.append(s);
    }

    /**
     * Add JavaSript code to the output
     *
     * @param s  the JavaSript
     */
    public void addJS(String s) {
        getJS().append(s);
    }


    /**
     * Add spatial metadata
     *
     * @param entry  the entry
     * @param metadataList  the list of metatdata
     *
     * @return true if we added some metadata
     */
    public boolean addSpatialMetadata(Entry entry,
                                      List<Metadata> metadataList) {
        boolean didone = false;
        for (Metadata metadata : metadataList) {
            if (metadata.getType().equals(
                    MetadataHandler.TYPE_SPATIAL_POLYGON)) {
                List<double[]> points = new ArrayList<double[]>();
                String         s      = metadata.getAttr1();
                for (String pair : StringUtil.split(s, ";", true, true)) {
                    List<String> toks = StringUtil.splitUpTo(pair, ",", 2);
                    if (toks.size() != 2) {
                        continue;
                    }
                    double lat = Misc.decodeLatLon(toks.get(0));
                    double lon = Misc.decodeLatLon(toks.get(1));
                    points.add(new double[] { lat, lon });
                }
                this.addLines(entry.getId() + "_polygon", points);
                didone = true;
            }
        }
        return didone;
    }


    /**
     * Get the map div
     *
     * @param contents the contents
     *
     * @return  the div tag
     */
    private String getMapDiv(String contents) {
        StringBuffer result = new StringBuffer();
        result.append(
            HtmlUtil.div(
                contents,
                HtmlUtil.style(
                    "border:1px #888888 solid; background-color:#7391ad; width:"
                    + width + "px; height:" + height + "px") + " "
                        + HtmlUtil.id(mapVarName)));
        result.append("\n");
        return result.toString();
    }


    /**
     * Get the HTML for this map
     *
     * @return  the HTML
     */
    public String getHtml() {
        if ( !showMaps()) {
            return getMapDiv("&nbsp;Maps not available");
        }
        StringBuffer result = new StringBuffer();
        result.append(html);
        result.append(getMapDiv(""));
        result.append(HtmlUtil.script(getJS().toString()));
        result.append("\n");
        return result.toString();
    }


    /**
     * Get the JavaScript for this map
     *
     * @return  the JavaScript
     */
    private StringBuffer getJS() {
        if (jsBuffer == null) {
            jsBuffer = new StringBuffer();
            jsBuffer.append("var " + mapVarName + " = new RepositoryMap("
                            + HtmlUtil.squote(mapVarName) + ");\n");
            jsBuffer.append("var theMap = " + mapVarName + ";\n");
            if ( !forSelection) {
                jsBuffer.append("theMap.initMap(" + forSelection + ");\n");
            }
        }
        return jsBuffer;
    }

    /**
     * Make a selector
     *
     * @param arg  the argument
     * @param popup  true to make a popup
     * @param nwse  the north, south, east and west ids
     *
     * @return  the corresponding code
     */
    public String makeSelector(String arg, boolean popup, String[] nwse) {

        return makeSelector(arg, popup, nwse, "", "");
    }

    /**
     * Make a selector
     *
     * @param arg  the argument
     * @param popup  true to make a popup
     * @param nwse  the north, south, east and west ids
     * @param extraLeft  extra left text
     * @param extraTop  extra top text
     *
     * @return  the corresponding code
     */
    public String makeSelector(String arg, boolean popup, String[] nwse,
                               String extraLeft, String extraTop) {
        boolean doRegion = true;
        if (nwse == null) {
            nwse = new String[] { "", "", "", "" };
        }

        if (nwse.length == 2) {
            doRegion = false;
        }
        String widget = getSelectorWidget(arg, nwse);
        if ( !showMaps()) {
            return widget;
        }

        String       msg = HtmlUtil.italics(doRegion
                                            ? msg("Shift-drag to select region")
                                            : msg("Click to select point"));

        StringBuffer sb  = new StringBuffer();
        sb.append(msg);
        sb.append(HtmlUtil.br());
        sb.append(getMapDiv(""));
        if ((extraLeft != null) && (extraLeft.length() > 0)) {
            widget = widget + HtmlUtil.br() + extraLeft;
        }

        String rightSide = null;
        String clearLink = getSelectorClearLink(msg("Clear"));
        String initParams = HtmlUtil.squote(arg) + "," + doRegion + ","
                            + (popup
                               ? "1"
                               : "0");

        if (popup) {
            rightSide = HtmlUtil.space(2)
                        + repository.makeStickyPopup(msg("Select"),
                            sb.toString(),
                            getVariableName()
                            + ".selectionPopupInit();") + HtmlUtil.space(2)
                                + clearLink + HtmlUtil.space(2)
                                + HtmlUtil.space(2) + extraTop;
        } else {
            rightSide = clearLink + HtmlUtil.space(2) + HtmlUtil.br()
                        + sb.toString();
        }

        addJS(getVariableName() + ".setSelection(" + initParams + ");\n");
        return HtmlUtil.table(new Object[] { widget, rightSide }) + html
               + HtmlUtil.script(getJS().toString());
    }

    /**
     * Get the selector clear link
     *
     * @param msg  name for the clear link
     *
     * @return  the link
     */
    public String getSelectorClearLink(String msg) {
        return HtmlUtil.mouseClickHref(getVariableName()
                                       + ".selectionClear();", msg);
    }


    /**
     * GEt the selector widget
     *
     * @param arg  the argument
     * @param nwse the N,S,E and W labels
     *
     * @return  the widget
     */
    public String getSelectorWidget(String arg, String[] nwse) {
        boolean doRegion = true;
        if (nwse == null) {
            nwse = new String[] { "", "", "", "" };
        }
        String widget;
        if (nwse.length == 2) {
            doRegion = false;
        }

        if (doRegion) {
            widget = HtmlUtil.makeLatLonBox(arg, nwse[2], nwse[0], nwse[3],
                                            nwse[1]);
        } else {
            widget =
                " " + msgLabel("Latitude") + " "
                + HtmlUtil.input(arg + ".latitude", nwse[0],
                                 HtmlUtil.SIZE_5 + " "
                                 + HtmlUtil.id(arg + ".latitude")) + " "
                                     + msgLabel("Longitude") + " "
                                     + HtmlUtil.input(arg + ".longitude",
                                         nwse[1],
                                         HtmlUtil.SIZE_5 + " "
                                         + HtmlUtil.id(arg
                                             + ".longitude")) + " ";

        }

        return widget;
    }


    /**
     * Add a box to the map
     *
     * @param entry  the map entry
     * @param properties  the properties for the box
     */
    public void addBox(Entry entry, MapProperties properties) {
        addBox(entry.getId(), properties, entry.getNorth(), entry.getWest(),
               entry.getSouth(), entry.getEast());
    }

    /**
     * Add a box to the map
     *
     * @param id  the id
     * @param llr  the bounds
     * @param properties the box properties
     */
    public void addBox(String id, LatLonRect llr, MapProperties properties) {
        addBox(id, properties, llr.getLatMax(), llr.getLonMin(),
               llr.getLatMin(), llr.getLonMax());
    }


    /**
     * Add a box to the map
     *
     * @param id  the id
     * @param properties the box properties
     * @param north  north value
     * @param west   west value
     * @param south  south value
     * @param east   east value
     */
    public void addBox(String id, MapProperties properties, double north,
                       double west, double south, double east) {
        getJS().append("var mapBoxAttributes = {\"color\":\""
                       + properties.getColor() + "\",\"selectable\": "
                       + properties.getSelectable() + ",\"zoomToExtent\": "
                       + properties.getZoomToExtent() + "};\n");
        getJS().append(mapVarName + ".addBox(" + HtmlUtil.squote(id) + ","
                       + north + "," + west + "," + south + "," + east
                       + ", mapBoxAttributes);\n");
    }


    /**
     * Add a line to the map
     *
     * @param id  the line id
     * @param fromPt  starting point
     * @param toPt    ending point
     */
    public void addLine(String id, LatLonPointImpl fromPt,
                        LatLonPointImpl toPt) {
        addLine(id, fromPt.getLatitude(), fromPt.getLongitude(),
                toPt.getLatitude(), toPt.getLongitude());
    }

    /**
     * Add a set of lines
     *
     * @param id  the lines id
     * @param pts  the points
     */
    public void addLines(String id, double[][] pts) {
        for (int i = 1; i < pts.length; i++) {
            addLine(id, pts[i - 1][0], pts[i - 1][1], pts[i][0], pts[i][1]);
        }
    }

    /**
     * Add a set of lines
     *
     * @param id  the lines id
     * @param pts  the points
     */
    public void addLines(String id, List<double[]> pts) {
        if (pts.size() == 0) {
            return;
        }
        double[] lastGoodPoint = pts.get(0);
        for (int i = 1; i < pts.size(); i++) {
            double[] currentPoint = pts.get(i);
            if (Double.isNaN(currentPoint[0])
                    || Double.isNaN(currentPoint[0])) {
                lastGoodPoint = null;
                continue;
            }
            if (lastGoodPoint != null) {
                addLine(id, lastGoodPoint[0], lastGoodPoint[1],
                        currentPoint[0], currentPoint[1]);
            }
            lastGoodPoint = currentPoint;
        }
    }


    /**
     * Add a line
     *
     * @param id  the line id
     * @param fromLat  starting lat
     * @param fromLon  starting lon
     * @param toLat    ending lat
     * @param toLon    ending lon
     */
    public void addLine(String id, double fromLat, double fromLon,
                        double toLat, double toLon) {
        getJS().append(mapVarName + ".addLine(" + HtmlUtil.squote(id) + ","
                       + fromLat + "," + fromLon + "," + toLat + "," + toLon
                       + ");\n");
    }

    /**
     * Add a marker
     *
     * @param id  the marker id
     * @param pt  the position
     * @param icon  the icon
     * @param info  the associated text
     */
    public void addMarker(String id, LatLonPointImpl pt, String icon,
                          String info) {
        addMarker(id, pt.getLatitude(), pt.getLongitude(), icon, info);
    }

    /**
     * Add a marker
     *
     * @param id  the marker id
     * @param lat  the latitude
     * @param lon  the longitude
     * @param icon  the icon
     * @param info  the associated text
     */
    public void addMarker(String id, double lat, double lon, String icon,
                          String info) {
        getJS().append(mapVarName + ".addMarker(" + HtmlUtil.squote(id) + ","
                       + llp(lat, lon) + "," + ((icon == null)
                ? "null"
                : HtmlUtil.squote(icon)) + "," + HtmlUtil.squote(info)
                                         + ");\n");
    }


    /**
     * Center the map on the bounds
     *
     * @param bounds  the bounds
     */
    public void centerOn(Rectangle2D.Double bounds) {
        if (bounds != null) {
            getJS().append("var bounds = new OpenLayers.Bounds("
                           + bounds.getX() + "," + bounds.getY() + ","
                           + (bounds.getX() + bounds.getWidth()) + ","
                           + (bounds.getY() + bounds.getHeight()) + ");\n");
            getJS().append(mapVarName + ".centerOnMarkers(bounds);\n");
        } else {
            center();
        }

    }


    /**
     * Center the map
     */
    public void center() {
        getJS().append(mapVarName + ".centerOnMarkers(null);\n");
    }

    /**
     * Center on the entry bounds
     *
     * @param entry  the entry
     */
    public void centerOn(Entry entry) {
        if (entry == null) {
            center();
            return;
        }
        if (entry.hasAreaDefined()) {
            centerOn(entry.getNorth(), entry.getWest(), entry.getSouth(),
                     entry.getEast());
        } else {
            center();
        }
    }


    /**
     * Center on the box defined by the N,S,E,W coords
     *
     * @param north  north edge
     * @param west   west edge
     * @param south  south edge
     * @param east   east edge
     */
    public void centerOn(double north, double west, double south,
                         double east) {
        getJS().append("var bounds = new OpenLayers.Bounds(" + west + ","
                       + south + "," + east + "," + north + ");\n");
        getJS().append(mapVarName + ".centerOnMarkers(bounds);\n");
    }


    /**
     * Get the highlight  href tag
     *
     * @param id  the id
     * @param label  the label
     *
     * @return  the href tag
     */
    public String getHiliteHref(String id, String label) {
        return "<a href=\"javascript:" + getVariableName() + ".hiliteMarker("
               + HtmlUtil.squote(id) + ");\">" + label + "</a>";
    }

    /**
     * Create a OpenLayers.LonLat string representation from a lat/lon point
     *
     * @param lat  the latitude
     * @param lon  the longitude
     *
     * @return the OpenLayer.LonLat
     */
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
     *  Set the Width property.
     *
     *  @param value The new value for Width
     */
    public void setWidth(int value) {
        width = value;
    }

    /**
     *  Get the Width property.
     *
     *  @return The Width
     */
    public int getWidth() {
        return width;
    }

    /**
     *  Set the Height property.
     *
     *  @param value The new value for Height
     */
    public void setHeight(int value) {
        height = value;
    }

    /**
     *  Get the Height property.
     *
     *  @return The Height
     */
    public int getHeight() {
        return height;
    }





    /**
     *  Set the MapVarName property.
     *
     *  @param value The new value for MapVarName
     */
    public void setMapVarName(String value) {
        mapVarName = value;
    }

    /**
     *  Get the MapVarName property.
     *
     *  @return The MapVarName
     */
    public String getVariableName() {
        return mapVarName;
    }

}
