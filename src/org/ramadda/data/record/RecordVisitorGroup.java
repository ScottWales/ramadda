/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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


import java.io.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Holds a list of visitors. Visits them in turn
 *
 */
public class RecordVisitorGroup extends RecordVisitor {

    /** list of visitors */
    private List<RecordVisitor> visitors = new ArrayList<RecordVisitor>();

    /** record count */
    private int count = 0;

    /**
     * ctor
     */
    public RecordVisitorGroup() {}

    /**
     * ctor
     *
     * @param visitors List of visitors
     */
    public RecordVisitorGroup(List<RecordVisitor> visitors) {
        this.visitors.addAll(visitors);
    }

    /**
     * add a new visitor
     *
     * @param visitor new visitor
     */
    public void addVisitor(RecordVisitor visitor) {
        visitors.add(visitor);
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param visitInfo _more_
     * @param record _more_
     *
     * @return _more_
     */
    public boolean visitRecord(RecordFile file, VisitInfo visitInfo,
                               Record record) {
        count++;
        for (RecordVisitor visitor : visitors) {
            if ( !visitor.visitRecord(file, visitInfo, record)) {
                return false;
            }
        }
        return true;
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param visitInfo _more_
     */
    @Override
    public void finished(RecordFile file, VisitInfo visitInfo) {
        super.finished(file, visitInfo);
        for (RecordVisitor visitor : visitors) {
            visitor.finished(file, visitInfo);
        }
    }

    /**
     * _more_
     *
     * @param visitInfo _more_
     */
    @Override
    public void close(VisitInfo visitInfo) {
        super.close(visitInfo);
        for (RecordVisitor visitor : visitors) {
            visitor.close(visitInfo);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getCount() {
        return count;
    }

}
