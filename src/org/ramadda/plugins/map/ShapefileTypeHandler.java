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


import ucar.unidata.gis.*;
import ucar.unidata.gis.shapefile.*;

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

    private static final int IDX_LON = 0;
    private static final int IDX_LAT = 1;

    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
        throws Exception {
        if(!entry.isFile()) return;
        EsriShapefile shapefile = new EsriShapefile(entry.getFile().toString());
        Rectangle2D bounds = shapefile.getBoundingBox();
        double[][] lonlat = new double[][]{{bounds.getX()},
                                           {bounds.getY()+bounds.getHeight()}};
        ProjFile projFile  = shapefile.getProjFile();
        if(projFile!=null)
            lonlat = projFile.convertToLonLat(lonlat);
        entry.setNorth(lonlat[IDX_LAT][0]);
        entry.setWest(lonlat[IDX_LON][0]);
        lonlat[IDX_LAT][0] = bounds.getY();
        lonlat[IDX_LON][0] = bounds.getX()+bounds.getWidth();
        if(projFile!=null)
            lonlat = projFile.convertToLonLat(lonlat);
        entry.setSouth(lonlat[IDX_LAT][0]);
        entry.setEast(lonlat[IDX_LON][0]);
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


    /**
     */
    @Override
        public boolean addToMap(Request request, Entry entry, MapInfo map)     {
        try {
            if(!entry.isFile()) return true;
            //TODO: stream through the shapes
            EsriShapefile shapefile = new EsriShapefile(entry.getFile().toString());
            List features = shapefile.getFeatures();

            for(int i=0;i<features.size();i++) {
                EsriShapefile.EsriFeature gf =
                    (EsriShapefile.EsriFeature) features.get(i);
                java.util.Iterator pi = gf.getGisParts();
                while (pi.hasNext()) {
                    GisPart   gp   = (GisPart) pi.next();
                    int       np   = gp.getNumPoints();
                    double[]  xx   = gp.getX();
                    double[]  yy   = gp.getY();
                    //              map.addMarker("id", lat, lon, null, info);
                    List<double[]> points = new ArrayList<double[]>();
                    for(int ptIdx=0;ptIdx<xx.length;ptIdx++) {
                        points.add(new double[]{yy[ptIdx],xx[ptIdx]});
                    }
                    //                double lat = XmlUtil.getAttribute(trackPoint, GpxUtil.ATTR_LAT,0.0);
                    //                double lon = XmlUtil.getAttribute(trackPoint, GpxUtil.ATTR_LON,0.0);
                    //                points.add(new double[]{lat,lon});
                    if(points.size()>1) {
                        System.err.println("points:" + points.size());
                        map.addLines("", points);
                    }
                }
            }
        } catch(Exception exc) {
            throw new RuntimeException(exc);
        }
        return false;
    }




}
