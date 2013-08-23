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

package org.ramadda.data.record.filter;


import org.ramadda.data.record.*;

import java.io.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public class LatLonBoundsFilter implements RecordFilter {

    /** _more_ */
    public double north;

    /** _more_ */
    public double south;

    /** _more_ */
    public double east;

    /** _more_ */
    public double west;


    /**
     * _more_
     *
     * @param north _more_
     * @param south _more_
     * @param east _more_
     * @param west _more_
     */
    public LatLonBoundsFilter(double north, double west, double south,
                              double east) {
        this.north = north;
        this.west  = west;
        this.south = south;
        this.east  = east;
    }



    /**
     * _more_
     *
     * @param record _more_
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public boolean isRecordOk(Record record, VisitInfo visitInfo) {
        GeoRecord geoRecord = (GeoRecord) record;
        if ( !geoRecord.isValidPosition()) {
            return false;
        }
        double lat = geoRecord.getLatitude();
        double lon = geoRecord.getLongitude();

        boolean contains = (lat >= south) && (lat <= north) && (lon >= west)
                           && (lon <= east);

        return contains;

    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return "llb filter:" + " N:" + north + " W:" + west + " S:" + south
               + " E:" + east;
    }


}
