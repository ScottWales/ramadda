/**
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.plugins.map;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.type.*;


import java.awt.geom.Rectangle2D;

import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.Misc;
import ucar.unidata.data.gis.KmlUtil;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;


import java.text.SimpleDateFormat;


import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;


import ucar.unidata.gis.shapefile.EsriShapefile;

/**
 */
public class ShapefileTypeHandler extends GenericTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception _more_
     */
    public ShapefileTypeHandler(Repository repository, Element node)
        throws Exception {
        super(repository, node);
    }


    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
        throws Exception {
        if(!entry.isFile()) return;
        /*
        EsriShapefile shapefile = new EsriShapefile(entry.getFile().toString());
        Rectangle2D bounds = shapefile.getBoundingBox();
        entry.setNorth(bounds.getY()+bounds.getHeight());
        entry.setSouth(bounds.getY());
        entry.setWest(bounds.getX());
        entry.setEast(bounds.getX()+bounds.getWidth());
        */
    }



    public void getEntryLinks(Request request, Entry entry, List<Link> links)
            throws Exception {
        super.getEntryLinks(request, entry, links);
        /*        links.add(
                  new Link(
                           request.entryUrl(getRepository().URL_ENTRY_ACCESS, entry,"type","kml"),
                           getRepository().iconUrl(ICON_KML), "Convert Shapefile to KML",
                           OutputType.TYPE_FILE));
        */
    }


    public Result processEntryAccess(Request request, Entry entry) throws Exception {
        return null;
    }


    @Override
    public boolean addToMap(Request request, Entry entry, MapInfo map)     {
            /*
        try {

            Element root =readXml(entry);
            int cnt = 0;
            for(Element child: ((List<Element>)XmlUtil.findChildren(root, GpxUtil.TAG_WPT))) {
                if(cnt++>500) break;
                String name = XmlUtil.getGrandChildText(child, GpxUtil.TAG_NAME,"");
                String desc = XmlUtil.getGrandChildText(child, GpxUtil.TAG_DESC,"");
                String sym = XmlUtil.getGrandChildText(child, GpxUtil.TAG_SYM,"");
                double lat = XmlUtil.getAttribute(child, GpxUtil.ATTR_LAT,0.0);
                double lon = XmlUtil.getAttribute(child, GpxUtil.ATTR_LON,0.0);
                String info = name+"<br>" + desc;
                info = info.replaceAll("\n","<br>");
                info = info.replaceAll("'","\\'");
                map.addMarker("id", lat, lon, null, info);
            }

            for(Element track: ((List<Element>)XmlUtil.findChildren(root, GpxUtil.TAG_TRK))) {
                for(Element trackSeg: ((List<Element>)XmlUtil.findChildren(track, GpxUtil.TAG_TRKSEG))) {
                    List<double[]> points = new ArrayList<double[]>();
                    for(Element trackPoint: ((List<Element>)XmlUtil.findChildren(trackSeg, GpxUtil.TAG_TRKPT))) {
                        double lat = XmlUtil.getAttribute(trackPoint, GpxUtil.ATTR_LAT,0.0);
                        double lon = XmlUtil.getAttribute(trackPoint, GpxUtil.ATTR_LON,0.0);
                        points.add(new double[]{lat,lon});
                    }
                    if(points.size()>1) {
                        map.addLines("", points);
                    }
                }
            }
        } catch(Exception exc) {
            throw new RuntimeException(exc);
        }
        return false;
            */
            return true;
    }



}
