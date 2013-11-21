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

package org.ramadda.data.point.aon;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;


import org.ramadda.data.record.*;
import org.ramadda.util.Utils;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.Date;
import java.util.List;


/**
 */
public class SwitchyardPointFile extends SingleSiteTextFile {

    /**
     * ctor
     *
     * @param filename The file
     * @throws IOException
     */
    public SwitchyardPointFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * @param visitInfo holds visit info
     * @return visit info
     * @throws IOException on badness
     *
     * @throws Exception _more_
     */
    @Override
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {

        try {
            putProperty(PROP_DELIMITER, " ");
            putProperty(PROP_HEADER_DELIMITER, "#");
            visitInfo = super.prepareToVisit(visitInfo);

            String   hdr          = StringUtil.join("\n", getHeaderLines());

            String   latString    = null;
            String   lonString    = null;

            String[] datePatterns = {
                ".*(\\d?\\d/\\d\\d/\\d\\d\\d\\d\\s*(_|/\\s)\\s*\\d\\d\\d\\d)\\s*UTC.*",
                ".*(\\d?\\d/\\d?\\d/\\d\\d\\d\\d\\s+\\d\\d\\d\\d)\\s*UTC.*",
                ".*\\s+(\\d?\\d\\s+[^\\s]+\\s+\\d\\d\\d\\d\\s+\\d\\d\\d\\d\\s+UTC).*",
                //5/3/2009 _ 1542 UTC
                ".*\\s+(\\d+/\\d+/\\d+\\s*_\\s*\\d\\d\\d\\d\\s+UTC)",
                //2012-5-21/1801 UTC
                ".*(\\d\\d\\d\\d-\\d?\\d-\\d?\\d/\\d\\d\\d\\d\\s+UTC).*",
                //2011-5-4/1439
                ".*(\\d\\d\\d\\d-\\d?\\d-\\d?\\d/\\d\\d\\d\\d).*",
                //2010-5-5 / 1706 UTC
                ".*(\\d\\d\\d\\d-\\d?\\d-\\d?\\d\\s*/\\s*\\d\\d\\d\\d\\s*UTC).*"
            };


            String[] dateFormats = {
                "MM/dd/yyyyHHmm", "MM/dd/yyyyHHmm", "dd MMMM yyyy HHmm Z",
                "MM/dd/yyyy HHmm Z", "yyyy-MM-dd/HHmm", "yyyy-MM-dd/HHmm",
                "yyyy-MM-dd/HHmm Z"
            };

            Date date = Utils.findDate(hdr, datePatterns, dateFormats);
            if (date == null) {
                throw new IllegalArgumentException("no date  in header");
            }

            hdr = hdr.replaceAll(" deg ", " degrees ");
            String[] lats =
                Utils.findPatterns(
                    hdr,
                    ".*\\s+([\\d\\.]+)\\s*degrees\\s*([\\d\\.]+)\\s*min\\s*North.*");
            if (lats != null) {
                latString = lats[0] + ":" + lats[1];
            }
            String[] lons =
                Utils.findPatterns(
                    hdr,
                    ".*\\s+([\\d\\.]+)\\s*degrees\\s*([\\d\\.]+)\\s*min\\s*West.*");
            if (lons != null) {
                lonString = lons[0] + ":" + lons[1];
            }

            if (latString == null) {
                //  84.122 degrees North  _  058.008 degrees West      
                latString = StringUtil.findPattern(hdr,
                        ".*\\s+([0-9\\.]+)\\s+degrees North.*");
                lonString = StringUtil.findPattern(hdr,
                        ".*\\s+([0-9\\.]+)\\s+degrees West.*");
            }

            if ((latString == null) || (lonString == null)) {
                throw new IllegalArgumentException(
                    "Could not read location from:" + hdr);
            }

            double lat = Misc.decodeLatLon(latString);
            double lon = Misc.decodeLatLon(lonString);
            setLocation(lat, lon, 0);

            String cast = StringUtil.findPattern(hdr, "\\s+Cast\\s+(\\d+).*");
            String siteId = StringUtil.findPattern(hdr,
                                "\\s+Station\\s+([^\\s]+)\\s+.*");

            if (siteId == null) {
                siteId = "";
            }

            if (cast == null) {
                cast = "";
            }

            //LOOK: this needs to be in the same order as the aontypes.xml defines in the point plugin
            setFileMetadata(new Object[] { siteId, cast, });
            String  dttm = makeDateFormat("yyyy-MM-dd HH:mm").format(date);
            int     year             = Utils.getYear(date);
            boolean addPotentialTemp = year == 2005;
            String  attrs            = attrChartable() + attrSearchable();
            putFields(new String[] {
                makeField(FIELD_SITE_ID, attrType(TYPE_STRING),
                          attrValue(siteId.trim())),
                makeField("Cast", attrValue(cast), attrType(TYPE_STRING)),
                makeField(FIELD_LATITUDE, attrValue(lat)),
                makeField(FIELD_LONGITUDE, attrValue(lon)),
                makeField(FIELD_DATE, attrType(TYPE_DATE), attrValue(dttm),
                          attrFormat("yyyy-MM-dd HH:mm")),
                makeField(FIELD_DEPTH, attrs + attrUnit(UNIT_METERS)),
                makeField(FIELD_PRESSURE, attrs + attrUnit("dbar")),
                makeField("In_Situ_Temperature",
                          attrs + attrUnit(UNIT_CELSIUS)),
                ( !addPotentialTemp
                  ? null
                  : makeField(FIELD_POTENTIAL_TEMPERATURE,
                              attrs + attrUnit(UNIT_CELSIUS))),
                makeField(FIELD_CONDUCTIVITY, attrs + attrUnit("S/m")),
                makeField(FIELD_SALINITY, attrs + attrUnit("psu")),
                makeField(FIELD_SIGMA, attrs + attrUnit("-theta")),
                /*
                makeField("Dissolved_Oxygen_ML_L",  attrs +  attrUnit("ml/l")),
                makeField("Dissolved_Oxygen_MG_L",  attrs +  attrUnit("mg/l")),
                makeField("Dissolved_Oxygen_Sat",  attrs +  attrUnit("%sat")),
                makeField("Dissolved_Oxygen_MMOL_KG",  attrs +  attrUnit("Mmol/kg")),*/
            });

            return visitInfo;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }


    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        PointFile.test(args, SwitchyardPointFile.class);
    }

}
