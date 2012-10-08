
package org.ramadda.data.services;


import org.ramadda.repository.*;
import org.ramadda.data.record.*;

import org.ramadda.repository.job.JobManager;
import org.ramadda.repository.job.JobInfo;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.TypeHandler;


import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;


import org.w3c.dom.*;



import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;



import java.io.*;




import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.*;

import java.util.zip.*;



/**
 */
public class RecordJobManager extends JobManager  {


    private RecordOutputHandler recordOutputHandler;


       /**
     * ctor
     *
     */
    public RecordJobManager(RecordOutputHandler recordOutputHandler) {
        super(recordOutputHandler.getRepository());
    }


    public RecordOutputHandler getRecordOutputHandler() {
        return recordOutputHandler;
    }

    public RecordFormHandler getRecordFormHandler() {
        return  recordOutputHandler.getFormHandler();
    }


    /**
     * Apply the visitor to each record file in turn
     *
     * @param request the request
     * @param recordEntries entries to process
     * @param visitor The visitor to apply to the record file
     *
     * @throws Exception on badness
     */
    public void visitSequential(Request request,
                                List<? extends RecordEntry> recordEntries,
                                RecordVisitor visitor)
            throws Exception {
        visitSequential(request, recordEntries, visitor, null);
    }

    /**
     * Apply the visitor to each record file in turn
     *
     * @param request the request
     * @param recordEntries entries to process
     * @param visitor The visitor to apply to the record file
     * @param visitInfo visit state
     *
     * @throws Exception on badness
     */
    public void visitSequential(final Request request,
                                final List<? extends RecordEntry> recordEntries,
                                final RecordVisitor visitor,
                                final VisitInfo visitInfo)
            throws Exception {
        Callable<Boolean> callable = new Callable<Boolean>() {
            public Boolean call() {
                try {
                    System.err.println("NLAS: processing started");
                    long t1 = System.currentTimeMillis();
                    for (RecordEntry recordEntry : recordEntries) {
                        recordEntry.visit(visitor, visitInfo);
                    }
                    long t2 = System.currentTimeMillis();
                    visitor.close(visitInfo);
                    System.err.println("NLAS: processing done time:"
                                       + (t2 - t1));
                    return Boolean.TRUE;
                } catch (Exception exc) {
                    System.err.println("Badness:" + exc);
                    throw new RuntimeException(exc);
                }
            }

        };
        invokeAndWait(request, callable);
    }


    /**
     * Apply the visitor to to the record file
     *
     * @param request The request
     * @param recordEntry the record entry
     * @param visitor the visitor
     * @param visitInfo visit info
     *
     * @throws Exception On badness
     */
    public void visitSequential(Request request, RecordEntry recordEntry,
                                RecordVisitor visitor, VisitInfo visitInfo)
            throws Exception {
        List<RecordEntry> recordEntries = new ArrayList<RecordEntry>();
        recordEntries.add(recordEntry);
        visitSequential(request, recordEntries, visitor, visitInfo);
    }


    /**
     * This applies the visitor to each of the RecordEntries concurrently (well, using the executor service)
     *
     * @param request The request
     * @param recordEntries The entries to process
     * @param visitor the visitor
     * @param visitInfo visit info
     *
     * @throws Exception On badness
     */
    public void visitConcurrent(Request request,
                                List<? extends RecordEntry> recordEntries,
                                RecordVisitor visitor, VisitInfo visitInfo)
            throws Exception {
        for (RecordEntry recordEntry : recordEntries) {
            recordEntry.setVisitInfo(visitor, (visitInfo != null)
                                             ? new VisitInfo(visitInfo)
                                             : null);
        }
        invokeAndWait(request, makeCallables(recordEntries));
        visitor.close(visitInfo);
    }

    /**
     * utility to make a list of Callable objects for the record entries
     *
     * @param recordEntries The entries to process
     *
     * @return list of callables
     */
    private List<Callable<Boolean>> makeCallables(
            List<? extends RecordEntry> recordEntries) {
        List<Callable<Boolean>> callables =
            new ArrayList<Callable<Boolean>>();
        for (RecordEntry recordEntry : recordEntries) {
            callables.add(recordEntry);
        }
        return callables;
    }


}
