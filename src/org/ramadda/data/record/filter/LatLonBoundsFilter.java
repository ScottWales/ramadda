/*
 * Copyright 2010 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
 * http://www.unavco.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
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
    public LatLonBoundsFilter(double north, double west, double south,  double east) {
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
        return "llb filter:" + " N:" +north + " W:" + west + " S:" + south + " E:" + east;
    }


}
