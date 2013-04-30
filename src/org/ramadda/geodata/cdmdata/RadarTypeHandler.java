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
import ucar.nc2.units.DateUnit;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import java.io.File;
import java.util.Date;
import java.util.List;

import ucar.nc2.NetcdfFile;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class RadarTypeHandler extends GenericTypeHandler {
    public static final String TYPE_RADAR = "cdm_radar";
    public static final String TYPE_RADAR_LEVEL2 = "cdm_radar_level2";
    public static final String TYPE_RADAR_LEVEL3 = "cdm_radar_level3";
    public static final int IDX_STATION_ID = 0;
    public static final int IDX_STATION_NAME = 1;
    public static final int IDX_STATION_LAT = 2;
    public static final int IDX_STATION_LON = 3;
    public static final int IDX_STATION_PRODUCT = 4;

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public RadarTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param request _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, Entry entry, List<Link> links)
            throws Exception {
        super.getEntryLinks(request, entry, links);
        /*
        if (entry.getValues() == null) {
            return;
        }
        Object[] values = entry.getValues();
        if ((values.length >= 2) && (values[0] != null)
                && (values[1] != null)) {
            links.add(
                new Link(
                    HtmlUtils.url(
                        "http://radar.weather.gov/radar.php", "rid",
                        (String) entry.getValues()[0], "product",
                        (String) entry.getValues()[1]), iconUrl(
                            "/icons/radar.gif"), "Show NWS Radar Site"));
        }
        */
    }


@Override
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry) throws Exception {
        initializeNewEntry(entry);
    }



    public void initializeNewEntry(Entry entry) throws Exception {
        Object[] values = entry.getTypeHandler().getValues(entry);
        File f = entry.getFile();
        NetcdfFile ncf =  NetcdfFile.open(f.toString());

        String stationId = ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_RADAR_STATIONID, "XXX");
        String stationName =  ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_RADAR_STATIONNAME, "XXX, XX, XX");
        double radarLat = Double.parseDouble(ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_RADAR_LATITUDE, "0.0"));
        double radarLon = Double.parseDouble(ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_RADAR_LONGITUDE, "0.0"));
        String product = ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_KEYWORDS_VOCABULARY, "");


        String sdate = ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_TIME_START, (new Date()).toString());
        Date  startDate = DateUnit.getStandardOrISO(sdate);
        float lat_min = Float.parseFloat(ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_MINLAT, "0.0f"));
        float lat_max = Float.parseFloat(ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_MAXLAT, "0.0f"));
        float lon_min = Float.parseFloat(ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_MINLON, "0.0f"));
        float lon_max = Float.parseFloat(ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_MAXLON, "0.0f"));


        System.err.println ("Attribute value:" + CdmUtil.ATTR_MINLON +"=" + ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_MINLON, "NONE FOUND"));


        double altitude = Double.parseDouble(ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_RADAR_ALTITUDE, "0.0"));
        entry.setAltitude(altitude);

        System.err.println ("altitude:" + CdmUtil.ATTR_RADAR_ALTITUDE +"=" + ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_RADAR_ALTITUDE, "NONE FOUND"));


        if(!Utils.stringDefined(entry.getDescription())) {
            entry.setDescription(ncf.findAttValueIgnoreCase(null, CdmUtil.ATTR_SUMMARY, ""));
        }

        values[IDX_STATION_ID] = stationId;
        values[IDX_STATION_NAME] = stationName;
        values[IDX_STATION_LAT] = radarLat;
        values[IDX_STATION_LON] = radarLon;
        values[IDX_STATION_PRODUCT] = product;



        //The name should be the name of the file. If all entries are named with the station name then
        //then it will be hard to tell them apart
        //entry.setName(stationName);

        //Don't set the ID. The entry ID is a RAMADDA thing
        //        entry.setId(stId);


        entry.setStartDate(startDate.getTime());
        entry.setEndDate(startDate.getTime());
        //        entry.setLatitude(radarLat);
        //        entry.setLongitude(radarLon);
        entry.setSouth(lat_min);
        entry.setNorth(lat_max);
        entry.setEast(lon_max);
        entry.setWest(lon_min);

    }


}
