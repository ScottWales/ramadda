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

package org.ramadda.data.tools;


import org.ramadda.data.point.*;


import org.ramadda.data.record.*;
import org.ramadda.util.grid.LatLonGrid;
import org.ramadda.util.grid.ObjectGrid;

import ucar.unidata.util.IOUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Wed, Feb 15, '12
 * @author         Enter your name here...
 */
public class CsvFileInfo extends PointFileInfo {

    /** _more_ */
    boolean wroteHeader = false;

    /** _more_ */
    VisitInfo visitInfo;

    /**
     * _more_
     *
     * @param outputFile _more_
     * @param pointFile _more_
     *
     * @throws Exception _more_
     */
    public CsvFileInfo(String outputFile, PointFile pointFile)
            throws Exception {
        super(outputFile, pointFile);
        visitInfo = new VisitInfo();
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void writeHeader() throws Exception {}

    /**
     * _more_
     *
     * @param record _more_
     *
     * @throws Exception _more_
     */
    public void writeRecord(Record record) throws Exception {
        if ( !wroteHeader) {
            record.printCsvHeader(visitInfo, recordOutput.getPrintWriter());
            wroteHeader = true;
        }
        record.printCsv(visitInfo, recordOutput.getPrintWriter());
    }
}
