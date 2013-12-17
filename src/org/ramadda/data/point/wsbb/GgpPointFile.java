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
 * A file reader for the Global Geodynamics GGP format
 */
public class GgpPointFile extends CsvFile {

    /** what to use for missing numeric metadata */
    public static final double DOUBLE_UNDEFINED = 0;

    /** header delimiter */
    public static final String HEADER_DELIMITER = "starts:C***********";

    /** skip this */
    public static final String BLOCK_START = "77777777";

    /** skip this */
    public static final String BLOCK_END = "88888888";

    /** skip this */
    public static final String FILE_END = "99999999";

    /** missing value */
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
     * In the GGP format there can be blocks of delimited data
     *
     * @param line line of text
     *
     * @return is this good data
     */
    @Override
    public boolean isLineValidData(String line) {
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
        //Set some of the properties
        putProperty(PROP_DELIMITER, " ");
        putProperty(PROP_HEADER_DELIMITER, HEADER_DELIMITER);
        putProperty(PROP_DATEFORMAT, "yyyyMMdd HHmmss");

        //Read the header
        super.prepareToVisit(visitInfo);

        //Pull the metadata from the header
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
                    //clean up the station
                    station = station.replaceAll(",", " - ");
                } else if (name.indexOf("Instrument") >= 0) {
                    instrument = value;
                    //clean up the instrument
                    instrument = instrument.replaceAll(",", " - ");
                } else if (name.indexOf("Author") >= 0) {
                    author = value;
                    author = author.replaceAll(",", " - ");
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
                    //System.err.println("Unknown:" + line);
                }
            } else {
                //I've seen some files with comment lines. 
                if (line.startsWith("#")) {
                    line = line.substring(1);
                    desc.append(line);
                    desc.append("\n");
                }
            }
        }

        setDescriptionFromFile(desc.toString());
        setLocation(latitude, longitude, elevation);

        //this needs to be in the same order as the wsbbtypes.xml in the point plugin
        setFileMetadata(new Object[] {
            station, instrument, author, new Double(timeDelay),
            new Double(gravityCalibration), new Double(pressureCalibration)
        });

        //Define the fields
        //Note: The first fields (site, lat, lon, elev) aren't in the data rows
        //We define that there are fields but they have a fixed value.

        putFields(new String[] {
            //Embed the values for site, lat, lon and elevation
            makeField(FIELD_STATION, attrType(TYPE_STRING),
                      attrValue(station.trim())),
            makeField(FIELD_LATITUDE, attrValue(latitude)),
            makeField(FIELD_LONGITUDE, attrValue(longitude)),
            makeField(FIELD_ELEVATION, attrValue(elevation)),
            makeField(FIELD_DATE,
                      attrType(TYPE_STRING) + attr("isdate", "true")),
            makeField(FIELD_TIME,
                      attrType(TYPE_STRING) + attr("istime", "true")),
            //TODO: What is the unit for gravity and pressure
            makeField("gravity", attrUnit("V"), attrChartable(),
                      attrMissing(MISSING)),
            makeField("pressure", attrUnit("hPa"), attrChartable(),
                      attrMissing(MISSING)),
        });

        return visitInfo;
    }


    /**
     * Override the base file reader method that gets the date-time string from the field values
     * We do this because some of the GGP files did not pad their hhmmss column with "0"
     * @param record data record that got read
     * @param dttm buffer to set the date string with
     * @param dateIndex the date column
     * @param timeIndex the time column
     *
     * @throws Exception on badness
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
     * utility to parse a double from the header
     *
     * @param s string value
     *
     * @return double value if s is defined. else the DOUBLE_DEFINED
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
     * Test main
     *
     * @param args cmd line args
     */
    public static void main(String[] args) {
        PointFile.test(args, GgpPointFile.class);
    }

}
