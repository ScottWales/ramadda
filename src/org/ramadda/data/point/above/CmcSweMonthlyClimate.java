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

package org.ramadda.data.point.above;


import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import org.ramadda.data.record.*;

import java.io.*;


import java.util.Hashtable;



/**
 */
public class CmcSweMonthlyClimate extends CsvFile {


    /**
     * _more_
     */
    public CmcSweMonthlyClimate() {}

    /**
     * ctor
     *
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException _more_
     */
    public CmcSweMonthlyClimate(String filename) throws IOException {
        super(filename);
    }

    /**
     * _more_
     *
     * @param filename _more_
     * @param properties _more_
     *
     * @throws IOException _more_
     */
    public CmcSweMonthlyClimate(String filename, Hashtable properties)
            throws IOException {
        super(filename, properties);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getDelimiter() {
        return ",";
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     */
    @Override
    public int getSkipLines(VisitInfo visitInfo) {
        return 1;
    }

    //LATITUDE,LONGITUDE,OCT,NOV,DEC,JAN,FEB,MAR,APR,MAY,JUN

    /**
     *
     * @param visitInfo holds visit info
     *
     * @return visit info
     *
     * @throws IOException on badness
     */
@Override
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        super.prepareToVisit(visitInfo);
        String unit   = attrUnit("feet");
        String fields = makeFields(new String[] {
            makeField(FIELD_LATITUDE), makeField(FIELD_LONGITUDE),
            makeField("october", unit), makeField("november", unit),
            makeField("december", unit), makeField("january", unit),
            makeField("february", unit), makeField("march", unit),
            makeField("april", unit), makeField("may", unit),
            makeField("june", unit),
        });
        putProperty(PROP_FIELDS, fields);

        return visitInfo;
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        PointFile.test(args, CmcSweMonthlyClimate.class);
    }

}
