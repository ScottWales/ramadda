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

package org.ramadda.data.point.gcnet;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.data.record.*;


import org.ramadda.util.Station;

import java.io.*;


/**
 */

public class GCNetPointFile extends CsvFile {


    /**
     * ctor
     *
     * @param filename filename
     * @throws IOException On badness
     */
    public GCNetPointFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
@Override
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        //Put the delimiter first so we can read the header in the parent method
        putProperty(PROP_HEADER_DELIMITER, "");
        putProperty(PROP_DELIMITER, " ");
        super.prepareToVisit(visitInfo);
        putProperty(PROP_FIELDS, getFieldsFileContents());

        return visitInfo;
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
        TextRecord textRecord = (TextRecord) record;
        setLocation("" + (int) textRecord.getValue(1), textRecord);
        int    year      = (int) textRecord.getValue(5);
        double julianDay = textRecord.getValue(6);
        record.setRecordTime(getDateFromJulianDay(year, julianDay).getTime());

        return true;
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, GCNetPointFile.class);
    }

}
