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
public class BitmaskRecordFilter implements RecordFilter {

    /** _more_ */
    private int mask;

    /** _more_ */
    private boolean value;

    /** _more_ */
    private int attrId;

    /**
     * _more_
     *
     * @param operator _more_
     *
     * @param bitNumber _more_
     * @param attrId _more_
     * @param value _more_
     */
    public BitmaskRecordFilter(int bitNumber, boolean value, int attrId) {
        mask        = 1 << bitNumber;
        this.value  = value;
        this.attrId = attrId;
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
        int v = (int) record.getValue(attrId);
        if ((v & mask) == 0) {
            return value;
        }

        return !value;
    }



}
