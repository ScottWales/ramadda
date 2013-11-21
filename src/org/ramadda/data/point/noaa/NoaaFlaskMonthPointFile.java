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

package org.ramadda.data.point.noaa;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import org.ramadda.data.record.*;

import org.ramadda.util.Station;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.List;



/**
 */
public class NoaaFlaskMonthPointFile extends NoaaPointFile {

    /** _more_ */
    private static int IDX = 1;

    /** _more_ */
    public static final int IDX_SITE_CODE = IDX++;

    /** _more_ */
    public static final int IDX_LATITUDE = IDX++;

    /** _more_ */
    public static final int IDX_LONGITUDE = IDX++;

    /** _more_ */
    public static final int IDX_ELEVATION = IDX++;

    /** _more_ */
    public static final int IDX_YEAR = IDX++;

    /** _more_ */
    public static final int IDX_MONTH = IDX++;


    /**
     * ctor
     *
     * @param filename _more_
     * @throws IOException On badness
     */
    public NoaaFlaskMonthPointFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        super.prepareToVisit(visitInfo);
        dateIndices = new int[] { IDX_YEAR, IDX_MONTH };

        String fields   = getFieldsFileContents();
        String filename = getOriginalFilename(getFilename());
        //[parameter]_[site]_[project]_[lab ID number]_[measurement group]_[optional qualifiers].txt
        List<String> toks = StringUtil.split(filename, "_", true, true);
        String       siteId           = toks.get(1);
        String       parameter        = toks.get(0);
        String       project          = toks.get(2);
        String       labIdNumber      = toks.get(3);
        String       measurementGroup = toks.get(4);
        setFileMetadata(new Object[] { siteId, parameter, project,
                                       labIdNumber, measurementGroup, });
        fields = fields.replace("${parameter}", parameter);
        putProperty(PROP_FIELDS, fields);

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
        setLocation(record.getStringValue(1), (TextRecord) record);

        return true;
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, NoaaFlaskMonthPointFile.class);
    }


}
