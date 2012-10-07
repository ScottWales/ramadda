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


import org.ramadda.repository.*;


import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;

import ucar.unidata.util.IOUtil;

import java.io.File;

import java.util.concurrent.*;


/**
 * This is a wrapper around  ramadda Entry and a RecordFile
 *
 *
 */
public class RecordEntry implements Runnable, Callable<Boolean> {


    /** the output handler */
    private RecordOutputHandler recordOutputHandler;

    /** the ramadda entry */
    private Entry entry;

    /** the record file */
   private RecordFile recordFile;

    /** the initial user request */
    private Request request;

    /** the visitor */
    private RecordVisitor visitor;

    /** the visit info */
    private VisitInfo visitInfo;

    /** the job id */
    private Object processId;

    /**
     * ctor
     *
     *
     * @param recordOutputHandler output handler
     * @param request the request
     * @param entry the entry
     */
    public RecordEntry(RecordOutputHandler recordOutputHandler, Request request,
                      Entry entry) {
        this.recordOutputHandler = recordOutputHandler;
        this.request            = request;
        this.entry              = entry;

    }

    /**
     * create the record filter from the request
     *
     * @return the filter
     *
     * @throws Exception on badness
     */
    public RecordFilter getFilter() throws Exception {
        return recordOutputHandler.getFilter(request, entry, getRecordFile());
    }

    /**
     *  Set the ProcessId property.
     *
     *  @param value The new value for ProcessId
     */
    public void setProcessId(Object value) {
        processId = value;
    }

    /**
     *  Get the ProcessId property.
     *
     *  @return The ProcessId
     */
    public Object getProcessId() {
        return processId;
    }


    /**
     * How many records in the record file
     *
     * @return number of records
     *
     * @throws Exception on badness
     */
    public long getNumRecords() throws Exception {
        long records = getNumRecordsFromEntry(-1);
        if (records < 0) {
            records = getRecordFile().getNumRecords();
        }
        return records;
    }

    /**
     * get the number of points in the record file
     *
     * @param dflt default value
     *
     * @return number of records
     *
     * @throws Exception On badness
     */
    public long getNumRecordsFromEntry(long dflt) throws Exception {
        Object[] values = entry.getValues();
        if ((values != null) && (values.length > 0) && (values[0] != null)) {
            return ((Integer) values[0]).intValue();
        }
        return dflt;
    }

    /**
     * create if needed and return the RecordFile
     *
     * @return the record file
     *
     * @throws Exception on badness
     */
    public RecordFile getRecordFile() throws Exception {
        return recordFile;
    }

    public void  setRecordFile(RecordFile file)  {
        this.recordFile = file;
    }


    public RecordOutputHandler getOutputHandler() {
        return recordOutputHandler;
    }


    /*
     * get the entry
     *
     * @return the entry
     */
    public Entry getEntry() {
        return entry;
    }


    /**
     * implement the callable interface
     *
     * @return OK
     *
     * @throws Exception _more_
     */
    public Boolean call() throws Exception {
        try {
            visit(visitor, visitInfo);
        } catch(Exception exc) {
            System.err.println ("RecordEntry: ERROR:" + exc);
            throw exc;
        }
        return Boolean.TRUE;
    }


    /**
     * apply the visitor to the record file
     */
    public void run() {
        try {
            visit(visitor, visitInfo);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * set the visit info
     *
     * @param visitor the visitor
     * @param visitInfo the visit info
     */
    public void setVisitInfo(RecordVisitor visitor, VisitInfo visitInfo) {
        this.visitor   = visitor;
        this.visitInfo = visitInfo;
    }

    /**
     * apply the visitor to the recordfile
     *
     * @param visitor visitor
     * @param visitInfo visit info
     *
     * @throws Exception On badness
     */
    public void visit(RecordVisitor visitor, VisitInfo visitInfo)
            throws Exception {
        getRecordFile().visit(visitor, visitInfo, getFilter());
    }



    public Request getRequest() {
        return request;
    }


}
