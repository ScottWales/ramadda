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

import ucar.unidata.util.StringUtil;

import java.io.*;


/**
 */
public class NoaaTowerPointFile extends NoaaPointFile {

    /** _more_ */
    private static int IDX = 1;

    /** _more_ */
    public static final int IDX_SITE_CODE = IDX++;

    /** _more_ */
    public static final int IDX_YEAR = IDX++;

    /** _more_ */
    public static final int IDX_MONTH = IDX++;

    /** _more_ */
    public static final int IDX_DAY = IDX++;

    /** _more_ */
    public static final int IDX_HOUR = IDX++;

    /** _more_ */
    public static final int IDX_MINUTE = IDX++;

    /** _more_ */
    public static final int IDX_SECOND = IDX++;

    /** _more_ */
    public static final int IDX_LATITUDE = IDX++;

    /** _more_ */
    public static final int IDX_LONGITUDE = IDX++;

    /** _more_ */
    public static final int IDX_ELEVATION = IDX++;

    /** _more_ */
    public static final int IDX_INTAKE_HEIGHT = IDX++;

    /** _more_ */
    public static final int IDX_MEASURED_VALUE = IDX++;

    /** _more_ */
    public static final int IDX_TOTAL_UNCERTAINTY_ESTIMATE = IDX++;

    /** _more_ */
    public static final int IDX_ATMOSPHERIC_VARIABILTY = IDX++;

    /** _more_ */
    public static final int IDX_MEASUREMENT_UNCERTAINTY = IDX++;

    /** _more_ */
    public static final int IDX_SCALE_UNCERTAINTY = IDX++;

    /** _more_ */
    public static final int IDX_QC_FLAG = IDX++;

    /** _more_ */
    public static final double MISSING1 = -999.0;

    /** _more_ */
    public static final double MISSING2 = -999.99;


    /**
     * ctor
     *
     * @param filename _more_
     * @throws IOException On badness
     */
    public NoaaTowerPointFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * This  gets called before the file is visited. It reads the header and pulls out metadata
     *
     * @param visitInfo visit info
     *
     * @return possible new visitinfo
     *
     * @throws IOException On badness
     *
     * @throws Exception _more_
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        super.prepareToVisit(visitInfo);
        String filename  = getOriginalFilename(getFilename());
        String siteId    = StringUtil.findPattern(filename, "^(.*)_.*");
        String parameter = StringUtil.findPattern(filename, ".*\\.(.*)");
        //LOOK: this needs to be in the same order as the amrctypes.xml defines in the point plugin
        setFileMetadata(new Object[] { siteId, });

        putFields(new String[] {
            makeField(FIELD_SITE_ID, attrType(TYPE_STRING)),
            makeField(FIELD_YEAR, ""), makeField(FIELD_MONTH, ""),
            makeField(FIELD_DAY, ""),
            makeField(FIELD_HOUR, attrType(TYPE_STRING)),
            makeField(FIELD_MINUTE, attrType(TYPE_STRING)),
            makeField(FIELD_SECOND, attrType(TYPE_STRING)),
            makeField(FIELD_LATITUDE), makeField(FIELD_LONGITUDE),
            makeField(FIELD_INTAKE_HEIGHT),
            makeField(parameter, attrChartable(), attrMissing(MISSING1)),
            makeField("total_uncertainty_estimate", attrChartable(),
                      attrMissing(MISSING1)),
            makeField("atmospheric_variablitility", attrMissing(MISSING2)),
            makeField("measurement_uncertainty", attrChartable(),
                      attrMissing(MISSING2)),
            makeField("scale_uncertainty", attrChartable(),
                      attrMissing(MISSING2)),
            makeField(FIELD_QC_FLAG, attrType(TYPE_STRING)),
        });
        setDateIndices(new int[] {
            IDX_YEAR, IDX_MONTH, IDX_DAY, IDX_HOUR, IDX_MINUTE, IDX_SECOND
        });

        return visitInfo;
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, NoaaTowerPointFile.class);
    }

}
