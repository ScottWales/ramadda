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

package org.ramadda.data.point.unavco;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import org.ramadda.data.record.*;

import ucar.unidata.util.StringUtil;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Fri, Aug 23, '13
 * @author         Enter your name here...
 */
public class PositionTimeSeriesPointFile extends CsvFile {

    /** _more_ */
    private SimpleDateFormat sdf = makeDateFormat("yyyyMMdd-HHmmss");

    /** _more_ */
    boolean isPos = false;

    /**
     * ctor
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException _more_
     */
    public PositionTimeSeriesPointFile(String filename) throws IOException {
        super(filename);
        isPos = filename.endsWith(".pos");
    }

    /*
      The header:
      PBO Station Position Time Series. Reference Frame : IGS08
      Format Version,1.0.4
      4-character ID,P101
      Station name,RandolphLLUT2005
      Begin Date, 2005-09-03
      End Date, 2012-09-29
      Release Date, 2012-09-30
      Reference position, 41.6922736024 North Latitude, -111.2360162488 East Longitude, 2016.12225 meters elevation,
      Date, North (mm), East (mm), Vertical (mm), North Std. Deviation (mm), East Std. Deviation (mm), Vertical Std. Deviation (mm), Quality,
      2005-09-03,22.68, 42.87, 15.8, 1.5, 1.31, 5.23, final,
    */

    /**
     * Gets called when first reading the file. Parses the header
     *
     * @param visitInfo visit info
     *
     * @return the visit info
     *
     * @throws IOException on badness
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        putProperty(PROP_DELIMITER, isPos
                                    ? " "
                                    : ",");
        putProperty(PROP_SKIPLINES, isPos
                                    ? "37"
                                    : "9");

        super.prepareToVisit(visitInfo);
        List<String> headerLines = getHeaderLines();
        if (headerLines.size() != getSkipLines(visitInfo)) {
            throw new IllegalArgumentException("Bad number of header lines:"
                    + headerLines.size());
        }


        if (isPos) {
            return preparePosFile(visitInfo, headerLines);
        }


        //PBO Station Position Time Series. Reference Frame : IGS08
        String referenceFrame = StringUtil.split(headerLines.get(0), ":",
                                    true, true).get(1);

        //Format Version,1.0.4
        String formatVersion = StringUtil.split(headerLines.get(1), ",",
                                   true, true).get(1);

        //4-character ID,P101
        String fourCharId = StringUtil.split(headerLines.get(2), ",", true,
                                             true).get(1);

        //Station name,RandolphLLUT2005
        String processingCenter =
            StringUtil.split(getOriginalFilename(getFilename()), ".", true,
                             true).get(1);
        List<String> toks = StringUtil.split(headerLines.get(3), ":", true,
                                             true);
        String stationName = (toks.size() > 1)
                             ? toks.get(1)
                             : fourCharId;

        //LOOK: this needs to be in the same order as the unavcotypes.xml defines in the point plugin
        setFileMetadata(new Object[] { fourCharId, stationName,
                                       referenceFrame, formatVersion,
                                       processingCenter });


        //Reference position, 41.6922736024 North Latitude, -111.2360162488 East Longitude, 2016.12225 meters elevation,
        String positionLine = headerLines.get(7);
        positionLine = positionLine.replaceAll(",", " ");
        List<String> positionToks = StringUtil.split(positionLine, " ", true,
                                        true);

        //TODO: Check the 'North' latitude part. I'm assuming this is always degrees north and east
        double lat       = Double.parseDouble(positionToks.get(2));
        double lon       = Double.parseDouble(positionToks.get(5));
        double elevation = Double.parseDouble(positionToks.get(8));
        setLocation(lat, lon, elevation);


        putFields(new String[] {
            makeField(FIELD_SITE_ID, attrType(TYPE_STRING),
                      attrValue(fourCharId.trim())),
            makeField(FIELD_LATITUDE, attrValue(lat)),
            makeField(FIELD_LONGITUDE, attrValue(lon)),
            makeField(FIELD_ELEVATION, attrValue(elevation)),
            makeField(FIELD_DATE, attrFormat("yyyy-MM-dd")),
            makeField(FIELD_NORTH, attrUnit("mm"), attrChartable()),
            makeField(FIELD_EAST, attrUnit("mm"), attrChartable()),
            makeField(FIELD_VERTICAL, attrUnit("mm"), attrChartable()),
            makeField(FIELD_NORTH_STD_DEVIATION, attrUnit("mm"),
                      attrChartable()),
            makeField(FIELD_EAST_STD_DEVIATION, attrUnit("mm"),
                      attrChartable()),
            makeField(FIELD_VERTICAL_STD_DEVIATION, attrUnit("mm"),
                      attrChartable()),
            makeField(FIELD_QUALITY, attrType("string"), attrChartable()),
            makeField("skip", "")
        });;

        return visitInfo;
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param headerLines _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    private VisitInfo preparePosFile(VisitInfo visitInfo,
                                     List<String> headerLines)
            throws IOException {

        /*
          PBO Station Position Time Series. Reference Frame : IGS05 (061102)
          Format Version: 1.1.0
          4-character ID: AB01
          Station name  : AtkaIslandAK2007
          First Epoch   : 20070518 120000
        */
        String referenceFrame = StringUtil.split(headerLines.get(0), ":",
                                    true, true).get(1);
        String formatVersion = StringUtil.split(headerLines.get(1), ":",
                                   true, true).get(1);
        String fourCharId = StringUtil.split(headerLines.get(2), ":", true,
                                             true).get(1);
        String processingCenter =
            StringUtil.split(getOriginalFilename(getFilename()), ".", true,
                             true).get(1);
        List<String> toks = StringUtil.split(headerLines.get(3), ":", true,
                                             true);
        String stationName = (toks.size() > 1)
                             ? toks.get(1)
                             : fourCharId;
        setFileMetadata(new Object[] { fourCharId, stationName,
                                       referenceFrame, formatVersion,
                                       processingCenter });

        putProperty(PROP_FIELDS, getFieldsFileContents());

        return visitInfo;
    }


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
    @Override
    public boolean processAfterReading(VisitInfo visitInfo, Record record)
            throws Exception {
        if ( !super.processAfterReading(visitInfo, record)) {
            return false;
        }
        if ( !isPos) {
            return true;
        }
        String dttm = record.getStringValue(1) + "-"
                      + record.getStringValue(2);
        record.setRecordTime(sdf.parse(dttm).getTime());

        return true;
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, PositionTimeSeriesPointFile.class);
    }

}
