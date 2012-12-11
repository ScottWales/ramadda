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
    public long minTime=Record.UNDEFINED_TIME;

    /** _more_ */
    public long maxTime  = Record.UNDEFINED_TIME;


    public TimeFilter(Date  minTime, Date maxTime) {
        if(minTime!=null)
           this.minTime = minTime.getTime();
        if(maxTime!=null)
           this.maxTime = maxTime.getTime();

    }


    /**
     * _more_
     *
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
        if(time==Record.UNDEFINED_TIME) return false;
        if(minTime != Record.UNDEFINED_TIME && time<minTime) return false;
        if(maxTime != Record.UNDEFINED_TIME && time>maxTime) return false;
        return true;

    }

}
