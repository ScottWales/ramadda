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

package org.ramadda.data.point.geomag;


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
 */
public class IAGA2002PointFile extends CsvFile {


    /** _more_ */
    private SimpleDateFormat sdf = makeDateFormat("yyyy-MM-dd HH:mm:ss");

    /** _more_ */
    public static final String FIELD_DATE = "DATE";

    /** _more_ */
    public static final String FIELD_TIME = "TIME";

    /** _more_ */
    public static final String FIELD_DOY = "DOY";


    /**
     * The constructor
     *
     * @param filename file
     * @throws IOException On badness
     */
    public IAGA2002PointFile(String filename) throws IOException {
        super(filename);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isHeaderStandard() {
        return true;
    }

    /**
     * _more_
     *
     * @param line _more_
     *
     * @return _more_
     */
    public boolean isHeaderLine(String line) {
        return line.endsWith("|");
    }


    /**
     * This  gets called before the file is visited.
     * It reads the header and defines the fields
     *
     * @param visitInfo visit info
     * @return possible new visitinfo
     */
@Override
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        //Set the delimiter and how many lines in the header to skip
        putProperty(PROP_DELIMITER, "");
        super.prepareToVisit(visitInfo);

        //Read the header and make sure things are cool
        List<String> headerLines = getHeaderLines();
        /*
Format                 IAGA-2002                                    |
 Source of Data         Sveriges geologiska undersokning             |
 Station Name           Abisko                                       |
 IAGA CODE              ABK                                          |
 Geodetic Latitude      68.400                                       |
 Geodetic Longitude     18.800                                       |
 Elevation                                                           |
 Reported               XYZF                                         |
 Sensor Orientation                                                  |
 Digital Sampling                                                    |
 Data Interval Type     1-minute                                     |
 Data Type              variation
        */
        int    idx       = 0;
        String format    = getHeaderValue(headerLines.get(idx++));
        String source    = getHeaderValue(headerLines.get(idx++));
        String station   = getHeaderValue(headerLines.get(idx++));
        String iagaCode  = getHeaderValue(headerLines.get(idx++));
        String latitude  = getHeaderValue(headerLines.get(idx++));
        String longitude = getHeaderValue(headerLines.get(idx++));
        String elevation = getHeaderValue(headerLines.get(idx++));
        if (elevation.length() == 0) {
            elevation = "0";
        }

        String reported    = getHeaderValue(headerLines.get(idx++));
        String orientation = getHeaderValue(headerLines.get(idx++));
        String sampling    = getHeaderValue(headerLines.get(idx++));
        String interval    = getHeaderValue(headerLines.get(idx++));
        String dataType    = getHeaderValue(headerLines.get(idx++));

        setFileMetadata(new Object[] {
            iagaCode, station, source, orientation, sampling, interval,
            dataType
        });


        StringBuffer sb = new StringBuffer();
        sb.append(makeFields(new String[] {
            makeField(FIELD_DATE, attrType("string")),
            makeField(FIELD_TIME, attrType("string")), makeField(FIELD_DOY),
            makeField(FIELD_LATITUDE, attrValue(latitude)),
            makeField(FIELD_LONGITUDE, attrValue(longitude)),
            makeField(FIELD_ELEVATION, attrValue(elevation)),
        }));

        for (int i = 0; i < reported.length(); i++) {
            char c = reported.charAt(i);
            sb.append(",");
            sb.append(makeField("" + c, attrChartable(), attrSearchable(),
                                attrMissing(99999)));
        }

        putProperty(PROP_FIELDS, sb.toString());

        return visitInfo;
    }

    /**
     * _more_
     *
     * @param line _more_
     *
     * @return _more_
     */
    public String getHeaderValue(String line) {
        String s = line.substring(23).trim();
        s = s.substring(0, s.length() - 1);
        s = s.trim();

        return s;
    }


    /*
     * This gets called after a record has been read
     */

    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param record _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean processAfterReading(VisitInfo visitInfo, Record record)
            throws Exception {
        if ( !super.processAfterReading(visitInfo, record)) {
            return false;
        }
        TextRecord   textRecord = (TextRecord) record;
        String       dateString = textRecord.getStringValue(1);
        String       timeString = textRecord.getStringValue(2);
        StringBuffer dttm       = new StringBuffer();
        dttm.append(dateString);
        dttm.append(" ");
        dttm.append(timeString);
        Date date = sdf.parse(dttm.toString());
        record.setRecordTime(date.getTime());

        return true;
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, IAGA2002PointFile.class);
    }

}
