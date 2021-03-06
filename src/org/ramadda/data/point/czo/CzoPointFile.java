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

package org.ramadda.data.point.czo;


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
public class CzoPointFile extends CsvFile {


    /**
     * The constructor
     *
     * @param filename file
     * @throws IOException On badness
     */
    public CzoPointFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * This  gets called before the file is visited. It reads the header and defines the fields
     *
     * @param visitInfo visit info
     * @return possible new visitinfo
     *
     * @throws Exception _more_
     */
    @Override
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        //Se the delimiter and how many lines in the header to skip
        putProperty(PROP_DELIMITER, ",");
        putProperty(PROP_SKIPLINES, "1");
        super.prepareToVisit(visitInfo);

        return visitInfo;
    }



    /**
     * This gets called after a record has been read
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
        /*
                //Get the date from the values
                String dttm = ((int) textRecord.getValue(IDX_YEAR)) + "-"
                              + ((int) textRecord.getValue(IDX_MONTH)) + "-"
                              + ((int) textRecord.getValue(IDX_DAY)) + " "
                              + textRecord.getStringValue(IDX_TIME);
                Date date = sdf.parse(dttm);
                record.setRecordTime(date.getTime());
        */

        return true;
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, CzoPointFile.class);
    }

}
