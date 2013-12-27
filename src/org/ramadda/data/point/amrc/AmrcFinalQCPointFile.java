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

package org.ramadda.data.point.amrc;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.data.record.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;


/**
 * This class reads the QC'ed meteorolgical data from the Antarctic Meteorology Research Center (AMRC).
 *
 * The AMRC data is of the form:
 * <pre>
 * Year: 2011  Month: 08  ID: KMS  ARGOS: 21364  Name: Kominko-Slade
 * Lat: 79.47S  Lon: 112.11W  Elev: 1801m
 * 2011 213  8  1 0000  -19.5  806.9  444.0  444.0   88.3   -0.2
 * </pre>
 *
 * This class overwrites prepareToVisit to read the metadata from the header
 * and define the fields (i.e.,  the data dictionary).
 * The data that the API produces looks like:
 * site_id,   latitude, longitude, elevation, year, julian day, month, day, ...
 *
 * It overwrites processAfterReading to set the Date/time of the observation
 *
 * You can run this from the command line with:
 * java org.ramadda.data.point.amrc.AmrcFinalQCFile file.txt
 *
 * There is a example data file in the source:
 * org/ramadda/geodata/point/amrc/exampleamrcqc.txt
 *
 * To make the data type available to RAMADDA define a type definition plugin file like:
 * org/ramadda/geodata/point/amrc/amrctypes.xml
 *
 * You should then be able to harvest or upload a point file of your choice.
 */
public class AmrcFinalQCPointFile extends CsvFile {

    //Corresponds to the fields in the data
    //We use the date field indices to get the obs date

    /** base index to count from */
    private static int IDX = 1;

    /** data index */
    public static final int IDX_SITE_ID = IDX++;

    /** data index */
    public static final int IDX_LATITUDE = IDX++;

    /** data index */
    public static final int IDX_LONGITUDE = IDX++;

    /** data index */
    public static final int IDX_ELEVATION = IDX++;

    /** data index */
    public static final int IDX_YEAR = IDX++;

    /** data index */
    public static final int IDX_JULIAN_DAY = IDX++;

    /** data index */
    public static final int IDX_MONTH = IDX++;

    /** data index */
    public static final int IDX_DAY = IDX++;

    /** data index */
    public static final int IDX_TIME = IDX++;


    /** date formatter */
    private SimpleDateFormat sdf = makeDateFormat("yyyy-MM-dd HHmm");

    /** missing value */
    public static final double MISSING = 444.0;

    /** buffer         */
    private StringBuffer dttm = new StringBuffer();

    /**
     * The constructor
     *
     * @param filename file
     * @throws IOException On badness
     */
    public AmrcFinalQCPointFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * This  gets called before the file is visited. It reads the header and defines the fields
     *
     * @param visitInfo visit info
     * @return possible new visitinfo
     *
     * @throws Exception on badness
     */
    @Override
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        //Set the delimiter and how many lines in the header to skip
        putProperty(PROP_DELIMITER, " ");
        putProperty(PROP_SKIPLINES, "2");

        //Process the header
        super.prepareToVisit(visitInfo);


        //Read the header and make sure things are cool
        List<String> headerLines = getHeaderLines();
        if (headerLines.size() != getSkipLines(visitInfo)) {
            throw new IllegalArgumentException("Bad number of header lines:"
                    + headerLines.size());
        }

        //The header looks like:
        //        Year: 2012  Month: 01  ID: AG4  ARGOS:  8927  Name: AGO-4               
        //            Lat: 82.01S  Lon:  96.76E  Elev: 3597m

        //Extract the metadata
        String siteId = StringUtil.findPattern(headerLines.get(0),
                            "ID:\\s(.*)ARGOS:");
        String argosId = StringUtil.findPattern(headerLines.get(0),
                             "ARGOS:\\s*(.*)Name:");
        String siteName = StringUtil.findPattern(headerLines.get(0),
                              "Name:\\s(.*)");
        String latString = StringUtil.findPattern(headerLines.get(1),
                                                  "Lat:\\s(.*)Lon:");
        String lonString = StringUtil.findPattern(headerLines.get(1),
                               "Lon:\\s(.*)Elev:");
        String elevString = StringUtil.findPattern(headerLines.get(1),
                                "Elev:(.*)");

        if ((latString == null) || (lonString == null) || (siteName == null)
                || (siteId == null)) {
            throw new IllegalArgumentException("Could not read header:"
                    + headerLines + " lat:" + latString + " lon:" + lonString
                    + " elev" + elevString + " siteName:" + siteName
                    + " site:" + siteId);

        }
        if (elevString.endsWith("m")) {
            elevString = elevString.substring(0, elevString.length() - 1);
        }
        double lat       = Misc.decodeLatLon(latString);
        double lon       = Misc.decodeLatLon(lonString);
        double elevation = Double.parseDouble(elevString);

        setLocation(lat, lon, elevation);

        //This needs to be in the same order as the amrctypes.xml defines in the point plugin
        setFileMetadata(new Object[] { siteId, siteName, argosId });

        //Define the fields
        //Note: The first fields (site, lat, lon, elev) aren't in the data rows
        //We define that there are fields but they have a fixed value.

        putFields(new String[] {
            //Embed the values for site, lat, lon and elevation
            makeField(FIELD_SITE_ID, attrType(TYPE_STRING),
                      attrValue(siteId.trim())),
            makeField(FIELD_LATITUDE, attrValue(lat)),
            makeField(FIELD_LONGITUDE, attrValue(lon)),
            makeField(FIELD_ELEVATION, attrValue(elevString)),
            makeField(FIELD_YEAR, ""), makeField(FIELD_JULIAN_DAY, ""),
            makeField(FIELD_MONTH, ""), makeField(FIELD_DAY, ""),
            makeField(FIELD_TIME, attrType(TYPE_STRING)),
            makeField(FIELD_TEMPERATURE, attrUnit(UNIT_CELSIUS),
                      attrChartable(), attrMissing(MISSING)),
            makeField(FIELD_PRESSURE, attrUnit(UNIT_HPA), attrChartable(),
                      attrMissing(MISSING)),
            makeField(FIELD_WIND_SPEED, attrUnit(UNIT_M_S), attrChartable(),
                      attrMissing(MISSING)),
            makeField(FIELD_WIND_DIRECTION, attrUnit(UNIT_DEGREES),
                      attrMissing(MISSING)),
            makeField(FIELD_RELATIVE_HUMIDITY, attrUnit(UNIT_PERCENT),
                      attrChartable(), attrMissing(MISSING)),
            makeField(FIELD_DELTA_T, attrUnit(UNIT_CELSIUS), attrChartable(),
                      attrMissing(MISSING)),
        });

        return visitInfo;
    }


    /**
     * This gets called after a record has been read.
     * The base TextRecord/CsvFile can handle [date,time] and [year,month,day,hour,minute,second] columns.
     * Just not [year,month,day,hhmm] for now
     *
     * @param visitInfo Contains record visit information
     * @param record The record
     *
     * @return Should we continue processing the file
     *
     * @throws Exception on badness
     */
    public boolean processAfterReading(VisitInfo visitInfo, Record record)
            throws Exception {
        if ( !super.processAfterReading(visitInfo, record)) {
            return false;
        }
        TextRecord textRecord = (TextRecord) record;
        dttm.setLength(0);

        //Get the date from the values
        dttm.append((int) textRecord.getValue(IDX_YEAR));
        dttm.append("-");
        dttm.append((int) textRecord.getValue(IDX_MONTH));
        dttm.append("-");
        dttm.append((int) textRecord.getValue(IDX_DAY));
        dttm.append(" ");
        dttm.append(textRecord.getStringValue(IDX_TIME));
        Date date = sdf.parse(dttm.toString());
        record.setRecordTime(date.getTime());

        return true;
    }

    /**
     * command line test
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        PointFile.test(args, AmrcFinalQCPointFile.class);
    }

}
