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

package org.ramadda.data.point.icebridge;


import org.ramadda.data.record.*;

import java.io.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;





/** This is generated code from generate.tcl. Do not edit it! */
public class QfitRecord extends org.ramadda.data.point.PointRecord {

    /** _more_          */
    long baseDate = 0;

    /** _more_          */
    int relativeTime;

    /** _more_          */
    int laserLatitude;

    /** _more_          */
    int laserLongitude;

    /** _more_          */
    int elevation;


    /**
     * _more_
     *
     * @param that _more_
     */
    public QfitRecord(QfitRecord that) {
        super(that);
        this.relativeTime   = that.relativeTime;
        this.laserLatitude  = that.laserLatitude;
        this.laserLongitude = that.laserLongitude;
        this.elevation      = that.elevation;
    }

    /**
     * _more_
     *
     * @param file _more_
     */
    public QfitRecord(RecordFile file) {
        super(file);
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param bigEndian _more_
     */
    public QfitRecord(RecordFile file, boolean bigEndian) {
        super(file, bigEndian);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public long getRecordTime() {
        if (baseDate == 0L) {
            return super.getRecordTime();
        }

        return baseDate + relativeTime;
    }

    /**
     * _more_
     *
     * @param l _more_
     */
    public void setBaseDate(long l) {
        baseDate = l;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public double getLatitude() {
        return laserLatitude / 1000000.0;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public double getLongitude() {
        return org.ramadda.util.GeoUtils.normalizeLongitude(laserLongitude
                / 1000000.0);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public double getAltitude() {
        return elevation / 1000.0;
    }



}
