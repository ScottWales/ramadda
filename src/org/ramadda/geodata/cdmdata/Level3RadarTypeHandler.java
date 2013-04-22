/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
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

package org.ramadda.geodata.cdmdata;


import org.ramadda.repository.*;

import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;


import ucar.nc2.NetcdfFile;
import ucar.nc2.units.DateUnit;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.xml.XmlUtil;


import java.io.File;



import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class Level3RadarTypeHandler extends RadarTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public Level3RadarTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }



    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
@Override
    public void initializeNewEntry(Entry entry) throws Exception {
        Object[] values = entry.getTypeHandler().getValues(entry);


        File f = entry.getFile();
        System.err.println("Initialize new entry:"+  entry);
        System.err.println("File:" + f);


        NetcdfFile ncf =  NetcdfFile.open(f.toString());

        String stId = ncf.findAttValueIgnoreCase(null, "ProductStation", "XXX");
        String stName =  ncf.findAttValueIgnoreCase(null, "ProductStationName", "XXX, XX, XX");
        double radarLat = Double.parseDouble(ncf.findAttValueIgnoreCase(null, "RadarLatitude", "0.0"));
        double radarLon = Double.parseDouble(ncf.findAttValueIgnoreCase(null, "RadarLongitude", "0.0"));

        String station = "station";
        String product = ncf.findAttValueIgnoreCase(null, "keywords_vocabulary", "XXX");


        String sdate = ncf.findAttValueIgnoreCase(null, "time_coverage_start", "XXX, XX, XX");
        Date  startDate = DateUnit.getStandardOrISO(sdate);
        //Crack open the file and set metadata
        //...
        float lat_min = Float.parseFloat(ncf.findAttValueIgnoreCase(null, "geospatial_lat_min", "0.0f"));
        float lat_max = Float.parseFloat(ncf.findAttValueIgnoreCase(null, "geospatial_lat_max", "0.0f"));
        float lon_min = Float.parseFloat(ncf.findAttValueIgnoreCase(null, "geospatial_lon_min", "0.0f"));
        float lon_max = Float.parseFloat(ncf.findAttValueIgnoreCase(null, "geospatial_lon_max", "0.0f"));

        values[0] = station;
        values[1] = product;
        values[2] = stId;
        values[3] = stName;
        values[4] = radarLat;
        values[5] = radarLon;
        values[6] = stId;
        entry.setStartDate(startDate.getTime());
        entry.setEndDate(startDate.getTime());
        entry.setSouth(lat_min);
        entry.setNorth(lat_max);
        entry.setEast(lon_max);
        entry.setWest(lon_min);

    }




}
