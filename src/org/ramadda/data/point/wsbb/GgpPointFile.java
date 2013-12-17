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

package org.ramadda.data.point.wsbb;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import org.ramadda.data.record.*;
import org.ramadda.util.Utils;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.List;



/**
 */
public class GgpPointFile extends CsvFile {

    /** _more_ */
    public static final double DOUBLE_UNDEFINED = 0;

    /** _more_ */
    public static final String HEADER_DELIMITER = "starts:C***********";

    /** _more_ */
    public static final String BLOCK_START = "77777777";

    /** _more_ */
    public static final String BLOCK_END = "88888888";

    /** _more_ */
    public static final String FILE_END = "99999999";

    /** _more_ */
    public static final double MISSING = 99999.999;

    /**
     * The constructor
     *
     * @param filename file
     * @throws IOException On badness
     */
    public GgpPointFile(String filename) throws IOException {
        super(filename);
    }

    /**
     * _more_
     *
     * @param line _more_
     *
     * @return _more_
     */
    @Override
    public boolean isLineData(String line) {
        if (line.startsWith(BLOCK_START)) {
            return false;
        }
        if (line.startsWith(BLOCK_END)) {
            return false;
        }
        if (line.startsWith("INSTR")) {
            return false;
        }
        if (line.startsWith(FILE_END)) {
            return false;
        }

        return true;
    }

    /**
     * This  gets called before the file is visited. It reads the header and defines the fields
     *
     * @param visitInfo visit info
     * @return possible new visitinfo
     *
     * @throws Exception On badness
     */
    @Override
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        StringBuffer desc = new StringBuffer();
        putProperty(PROP_DELIMITER, " ");
        putProperty(PROP_HEADER_DELIMITER, HEADER_DELIMITER);
        putProperty(PROP_DATEFORMAT, "yyyyMMdd HHmmss");
        super.prepareToVisit(visitInfo);

        List<String> headerLines         = getHeaderLines();
        String       station             = "",
                     instrument          = "",
                     author              = "";
        double       timeDelay           = 0,
                     gravityCalibration  = 0,
                     pressureCalibration = 0;
        double       latitude            = 0,
                     longitude           = 0,
                     elevation           = 0;

        //The header lines can be in different order so look at each one
        for (String line : headerLines) {
            List<String> toks = StringUtil.splitUpTo(line, ":", 2);
            if (toks.size() == 2) {
                String name  = toks.get(0);
                String value = toks.get(1).trim();
                if (name.indexOf("Station") >= 0) {
                    station = value;
                } else if (name.indexOf("Instrument") >= 0) {
                    instrument = value;
                } else if (name.indexOf("Author") >= 0) {
                    author = value;
                } else if (name.indexOf("Latitude") >= 0) {
                    latitude = Misc.decodeLatLon(value);
                } else if (name.indexOf("Longitude") >= 0) {
                    longitude = Misc.decodeLatLon(value);
                } else if (name.indexOf("Gravity Cal") >= 0) {
                    gravityCalibration = parseDouble(value);
                } else if (name.indexOf("Pressure Cal") >= 0) {
                    pressureCalibration = parseDouble(value);
                } else if (name.indexOf("Height") >= 0) {
                    elevation = parseDouble(value);
                } else {
                    //System.err.println("NA:" + line);
                }
            } else {
                if (line.startsWith("#")) {
                    line = line.substring(1);
                    desc.append(line);
                    desc.append("\n");
                }
            }
        }


        setDescriptionFromFile(desc.toString());
        setLocation(latitude, longitude, elevation);

        //LOOK: this needs to be in the same order as the wsbbtypes.xml defines in the point plugin
        setFileMetadata(new Object[] {
            station, instrument, author, new Double(timeDelay),
            new Double(gravityCalibration), new Double(pressureCalibration)
        });

        //Define the fields
        //Note: The first fields (site, lat, lon, elev) aren't in the data rows
        //We define that there are fields but they have a fixed value.

        station = station.replaceAll(",", " - ");
        putFields(new String[] {
            //Embed the values for site, lat, lon and elevation
            makeField("station", attrType(TYPE_STRING),
                      attrValue(station.trim())),
            makeField(FIELD_LATITUDE, attrValue(latitude)),
            makeField(FIELD_LONGITUDE, attrValue(longitude)),
            makeField(FIELD_ELEVATION, attrValue(elevation)),
            makeField(FIELD_DATE, attrType(TYPE_STRING) + " isdate=true "),
            makeField(FIELD_TIME, attrType(TYPE_STRING) + " istime=true "),
            makeField("gravity", attrUnit("V"), attrChartable(),
                      attrMissing(MISSING)),
            makeField("pressure", attrUnit("hPa"), attrChartable(),
                      attrMissing(MISSING)),
        });

        return visitInfo;
    }


    /**
     * _more_
     *
     * @param record _more_
     * @param dttm _more_
     * @param dateIndex _more_
     * @param timeIndex _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void getDateTimeString(Record record, StringBuffer dttm,
                                  int dateIndex, int timeIndex)
            throws Exception {
        dttm.append(getString(record, dateIndex));
        dttm.append(" ");
        String timeField = getString(record, timeIndex);
        //Account for one of the non padded hhmmss formats
        while (timeField.length() < 6) {
            timeField = "0" + timeField;
        }
        dttm.append(timeField);
    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private double parseDouble(String s) {
        if (Utils.stringDefined(s)) {
            int index = s.indexOf(" ");
            if (index >= 0) {
                s = s.substring(0, index).trim();
            }

            return Double.parseDouble(s);
        }

        return DOUBLE_UNDEFINED;
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, GgpPointFile.class);
    }

}
