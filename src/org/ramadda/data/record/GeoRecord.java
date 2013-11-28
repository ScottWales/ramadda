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
     */
    public GeoRecord() {}


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


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean needsValidPosition() {
        return true;
    }



}
