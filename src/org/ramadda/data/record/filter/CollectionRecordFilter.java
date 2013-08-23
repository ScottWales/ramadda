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
public class CollectionRecordFilter implements RecordFilter {

    /** _more_ */
    public static final int OP_AND = 0;

    /** _more_ */
    public static final int OP_OR = 1;

    /** _more_ */
    private int operator = OP_AND;

    /** _more_ */
    private List<RecordFilter> filters = new ArrayList<RecordFilter>();

    /**
     * _more_
     *
     * @param operator _more_
     */
    public CollectionRecordFilter(int operator) {
        this.operator = operator;
    }

    /**
     * _more_
     *
     * @param filters _more_
     */
    public CollectionRecordFilter(List<RecordFilter> filters) {
        this(OP_AND, filters);
    }

    /**
     * _more_
     *
     * @param operator _more_
     * @param filters _more_
     */
    public CollectionRecordFilter(int operator, List<RecordFilter> filters) {
        this(operator);
        for (RecordFilter filter : filters) {
            addFilter(filter);
        }
    }

    /**
     * _more_
     *
     * @param operator _more_
     * @param filters _more_
     */
    public CollectionRecordFilter(int operator, RecordFilter[] filters) {
        this(operator);
        for (RecordFilter filter : filters) {
            addFilter(filter);
        }
    }


    /**
     * _more_
     *
     * @param filters _more_
     *
     * @return _more_
     */
    public static RecordFilter and(RecordFilter[] filters) {
        return new CollectionRecordFilter(OP_AND, filters);
    }

    /**
     * _more_
     *
     * @param filters _more_
     *
     * @return _more_
     */
    public static RecordFilter or(RecordFilter[] filters) {
        return new CollectionRecordFilter(OP_OR, filters);
    }

    /**
     * _more_
     *
     * @param filter _more_
     */
    public void addFilter(RecordFilter filter) {
        filters.add(filter);
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
        boolean anyTrue = false;
        for (RecordFilter filter : filters) {
            boolean v = filter.isRecordOk(record, visitInfo);
            if (v) {
                anyTrue = true;
            } else {
                if (operator == OP_AND) {
                    return false;
                }
            }
        }

        return anyTrue;
    }




}
