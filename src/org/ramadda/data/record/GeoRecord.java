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

package org.ramadda.data.record;


import org.ramadda.data.record.*;

import java.io.*;



/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public abstract class GeoRecord extends Record {


    /**
     * _more_
     *
     * @param recordFile _more_
     */
    public GeoRecord(RecordFile recordFile) {
        super(recordFile);
    }

    /**
     * _more_
     *
     * @param that _more_
     */
    public GeoRecord(GeoRecord that) {
        super(that);
    }

    /**
     * _more_
     *
     *
     * @param recordFile _more_
     * @param bigEndian _more_
     */
    public GeoRecord(RecordFile recordFile, boolean bigEndian) {
        super(recordFile, bigEndian);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public abstract double getLatitude();

    /**
     * _more_
     *
     * @return _more_
     */
    public abstract double getLongitude();

    /**
     * _more_
     *
     * @return _more_
     */
    public abstract double getAltitude();

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isValidPosition() {
        if ((getLatitude() <= 90) && (getLatitude() >= -90)
                && (getLongitude() >= -180) && (getLongitude() <= 360)) {
            return true;
        }
        return false;
    }




}
