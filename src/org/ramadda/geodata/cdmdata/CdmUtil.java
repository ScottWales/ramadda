/*
* Copyright 2008-2013 Geode Systems LLC
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
public class CdmUtil {

    /** _more_ */
    public static final String ATTR_MINLAT = "geospatial_lat_min";

    /** _more_ */
    public static final String ATTR_MAXLAT = "geospatial_lat_max";

    /** _more_ */
    public static final String ATTR_MINLON = "geospatial_lon_min";

    /** _more_ */
    public static final String ATTR_MAXLON = "geospatial_lon_max";

    /** _more_ */
    public static final String ATTR_KEYWORDS = "keywords";

    /** _more_ */
    public static final String ATTR_TITLE = "title";

    /** _more_ */
    public static final String ATTR_DESCRIPTION = "description";

    /** _more_ */
    public static final String ATTR_ABSTRACT = "abstract";

    /** _more_          */
    public static final String ATTR_SUMMARY = "summary";

    //level3

    /** _more_          */
    public static final String ATTR_RADAR_STATIONID = "ProductStation";

    /** _more_          */
    public static final String ATTR_RADAR_STATIONNAME = "ProductStationName";

    /** _more_          */
    public static final String ATTR_RADAR_LATITUDE = "RadarLatitude";

    /** _more_          */
    public static final String ATTR_RADAR_LONGITUDE = "RadarLongitude";

    /** _more_          */
    public static final String ATTR_RADAR_ALTITUDE = "RadarAltitude";

    /** _more_          */
    public static final String ATTR_KEYWORDS_VOCABULARY =
        "keywords_vocabulary";

    /** _more_          */
    public static final String ATTR_TIME_START = "time_coverage_start";

    //level2

    /** _more_          */
    public static final String ATTR_STATIONID = "Station";

    /** _more_          */
    public static final String ATTR_STATIONNAME = "StationName";

    /** _more_          */
    public static final String ATTR_STATION_LATITUDE = "StationLatitude";

    /** _more_          */
    public static final String ATTR_STATION_LONGITUDE = "StationLongitude";

    /** _more_          */
    public static final String ATTR_STATION_ALTITUDE =
        "StationElevationInMeters";
}
