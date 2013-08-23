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
 *
 */
public class PointFileInfo {

    /** output filename */
    private File outputFile;

    /** The point file */
    private PointFile pointFile;

    /** what we write to */
    RecordIO recordOutput;

    /** track number of points */
    private int pointCnt = 0;

    /** _more_ */
    private double[] ranges;


    /**
     * ctor
     *
     * @param outputFile output file
     * @param pointFile point file
     *
     * @throws Exception On badness
     */
    public PointFileInfo(String outputFile, PointFile pointFile)
            throws Exception {
        this.outputFile = new File(outputFile);
        this.recordOutput = new RecordIO(
            new BufferedOutputStream(
                new FileOutputStream(outputFile), 100000));
        this.pointFile = pointFile;
        writeHeader();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double[] getRanges() {
        return ranges;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getPointCount() {
        return pointCnt;
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void writeHeader() throws Exception {
        pointFile.writeHeader(recordOutput);
    }

    /**
     * Write the record
     *
     * @param record record to write
     * @param lat _more_
     * @param lon _more_
     *
     * @throws Exception On badness
     */
    public void writeRecord(PointRecord record, double lat, double lon)
            throws Exception {
        if (ranges == null) {
            ranges = new double[] { lat, lat, lon, lon };
        }
        ranges[0] = Math.max(ranges[0], lat);
        ranges[1] = Math.min(ranges[1], lat);
        ranges[2] = Math.max(ranges[2], lon);
        ranges[3] = Math.min(ranges[3], lon);
        record.recontextualize(pointFile);
        record.write(recordOutput);
        pointCnt++;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double getMaxLatitude() {
        return ranges[0];
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double getMinLatitude() {
        return ranges[1];
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double getMaxLongitude() {
        return ranges[2];
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public double getMinLongitude() {
        return ranges[3];
    }


    /**
     * finish up
     */
    public void close() {
        if (recordOutput != null) {
            recordOutput.close();
        }
    }
}
