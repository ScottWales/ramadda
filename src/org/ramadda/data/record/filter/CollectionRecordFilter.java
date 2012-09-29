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
