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
import java.util.Date;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public class TimeFilter implements RecordFilter {



    /** _more_ */
    public long minTime = Record.UNDEFINED_TIME;

    /** _more_ */
    public long maxTime = Record.UNDEFINED_TIME;


    /**
     * _more_
     *
     * @param minTime _more_
     * @param maxTime _more_
     */
    public TimeFilter(Date minTime, Date maxTime) {
        if (minTime != null) {
            this.minTime = minTime.getTime();
        }
        if (maxTime != null) {
            this.maxTime = maxTime.getTime();
        }

    }


    /**
     * _more_
     *
     *
     * @param minTime _more_
     * @param maxTime _more_
     */
    public TimeFilter(long minTime, long maxTime) {
        this.minTime = minTime;
        this.maxTime = maxTime;

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
        long time = record.getRecordTime();
        if (time == Record.UNDEFINED_TIME) {
            return false;
        }
        if ((minTime != Record.UNDEFINED_TIME) && (time < minTime)) {
            return false;
        }
        if ((maxTime != Record.UNDEFINED_TIME) && (time > maxTime)) {
            return false;
        }

        return true;

    }

}
