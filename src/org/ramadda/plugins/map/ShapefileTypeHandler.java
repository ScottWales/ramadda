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

    private static final int IDX_LON = 0;
    private static final int IDX_LAT = 1;


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



    /**
     */
    @Override
    public boolean addToMap(Request request, Entry entry, MapInfo map)     {
        try {
            if(!entry.isFile()) return true;
            //TODO: stream through the shapes
            EsriShapefile shapefile = new EsriShapefile(entry.getFile().toString());
            List features = shapefile.getFeatures();
            int totalPoints = 0;
            int MAX_POINTS = 10000;
            for(int i=0;i<features.size();i++) {
                if(totalPoints>MAX_POINTS) break;
                EsriShapefile.EsriFeature gf =
                    (EsriShapefile.EsriFeature) features.get(i);
                java.util.Iterator pi = gf.getGisParts();
                while (pi.hasNext()) {
                    if(totalPoints>MAX_POINTS) break;
                    GisPart   gp   = (GisPart) pi.next();
                    double[]  xx   = gp.getX();
                    double[]  yy   = gp.getY();
                    List<double[]> points = new ArrayList<double[]>();
                    for(int ptIdx=0;ptIdx<xx.length;ptIdx++) {
                        points.add(new double[]{yy[ptIdx],xx[ptIdx]});
                    }
                    totalPoints += points.size();
                    if(points.size()>1) {
                        map.addLines("", points);
                    } else if(points.size()==1) {
                        map.addMarker("id", points.get(0)[0],points.get(0)[1], null, "");
                    }
                }
            }
        } catch(Exception exc) {
            throw new RuntimeException(exc);
        }
        return false;
    }




}
