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

package org.ramadda.repository.job;


import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;

import org.ramadda.repository.job.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.TypeHandler;

import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;


import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TTLCache;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;



import java.io.*;

import java.sql.ResultSet;
import java.sql.Statement;




import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.*;

import java.util.zip.*;



/**
 */
public class JobManager extends RepositoryManager {

    /** _more_ */
    private long myTime = System.currentTimeMillis();

    /** _more_ */
    private boolean running = true;

    /** xml tag */
    public static final String TAG_JOB = "job";

    /** xml tag */
    public static final String TAG_URL = "url";

    /** xml tag */
    public static final String TAG_PRODUCTS = "products";

    /** xml attribute */
    public static final String ATTR_STATUS = "status";

    /** _more_ */
    public static final String ATTR_NUMBEROFPOINTS = "numberofpoints";

    /** _more_ */
    public static final String ATTR_ELAPSEDTIME = "elapsedtime";

    /** xml attribute */
    public static final String ATTR_TYPE = "type";

    /** type */
    public static final String TYPE_STATUS = "status";

    /** type */
    public static final String TYPE_CANCEL = "cancel";

    /** status */
    public static final String STATUS_RUNNING = "running";

    /** status */
    public static final String STATUS_DONE = "done";

    /** status */
    public static final String STATUS_CANCELLED = "cancelled";


    /** Property name for the max number of threads to use */
    public static final String PROP_NUMTHREADS = "job.numberofthreads";


    /** The singleton thread pool */
    private ExecutorService executor;

    /** _more_ */
    private Object MUTEX = new Object();

    /** _more_ */
    protected int totalJobs = 0;

    /** _more_ */
    protected int currentJobs = 0;

    /** _more_ */
    private TTLCache<Object, JobInfo> jobCache = new TTLCache<Object,
                                                     JobInfo>(60 * 24 * 60
                                                         * 1000);

    /** _more_ */
    private Hashtable<Object, JobInfo> runningJobs = new Hashtable<Object,
                                                         JobInfo>();


    /**
     * ctor
     *
     *
     * @param repository _more_
     */
    public JobManager(Repository repository) {
        super(repository);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getNumberOfJobs() {
        return currentJobs;
    }

    /**
     * _more_
     */
    public void shutdown() {
        running = false;
        if (executor != null) {
            System.err.println("RAMADDA: Shutting down the executor");
            executor.shutdownNow();
            executor    = null;
            currentJobs = 0;
        }
    }


    /**
     * Get the singleton thread pooler
     *
     * @return thread  pool
     */
    public ExecutorService getExecutor() {
        if (executor == null) {
            synchronized (MUTEX) {
                if (executor != null) {
                    return executor;
                }
                //Take up to half the processors to a max 4
                int numThreads =
                    getRepository().getProperty(PROP_NUMTHREADS,
                        Runtime.getRuntime().availableProcessors() / 2);
                if (numThreads < 1) {
                    numThreads = 1;
                } else if (numThreads > 4) {
                    numThreads = 4;
                }
                //NOTE: for now set this to 1 
                numThreads = 1;

                System.err.println(
                    "RAMADDA: #threads: " + numThreads + " available cores:"
                    + Runtime.getRuntime().availableProcessors());
                executor = Executors.newFixedThreadPool(numThreads);
            }
        }

        return executor;
    }


    /**
     * create a JobInfo from the database for the given job id
     *
     * @param jobId The job ID
     *
     * @return the job id
     *
     * @throws Exception On badness
     */
    public JobInfo doMakeJobInfo(Object jobId) throws Exception {
        Statement stmt =
            getDatabaseManager().select(JobInfo.DB_COL_JOB_INFO_BLOB,
                                        JobInfo.DB_TABLE,
                                        Clause.eq(JobInfo.DB_COL_ID, jobId));

        String[] values =
            SqlUtil.readString(getDatabaseManager().getIterator(stmt), 1);
        if ((values == null) || (values.length == 0)) {
            return null;
        }

        return makeJobInfo(values[0]);
    }


    /**
     * _more_
     *
     * @param blob _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private JobInfo makeJobInfo(String blob) throws Exception {
        blob = blob.replaceAll("org.unavco.projects.nlas.ramadda.JobInfo",
                               "org.ramadda.repository.job.JobInfo");
        JobInfo jobInfo = (JobInfo) getRepository().decodeObject(blob);

        return jobInfo;
    }


    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<JobInfo> readJobs(String type) throws Exception {
        return readJobs(Clause.eq(JobInfo.DB_COL_TYPE, type));
    }


    /**
     * _more_
     *
     * @param clause _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<JobInfo> readJobs(Clause clause) throws Exception {
        List<JobInfo> jobInfos = new ArrayList<JobInfo>();
        Statement stmt =
            getDatabaseManager().select(JobInfo.DB_COL_JOB_INFO_BLOB,
                                        JobInfo.DB_TABLE, clause);
        SqlUtil.Iterator iter = getDatabaseManager().getIterator(stmt);
        ResultSet        results;
        while ((results = iter.getNext()) != null) {
            jobInfos.add(makeJobInfo(results.getString(1)));
        }

        return jobInfos;

    }


    /**
     * save the job info to the database. This either writes the job to the db if its not there or overwrites the row if it is there
     *
     * @param jobInfo job info to write
     */
    public void writeJobInfo(JobInfo jobInfo) {
        writeJobInfo(jobInfo, false);
    }

    /**
     * write job info to db
     *
     * @param jobInfo job info
     * @param newOne is this a new job
     */
    public void writeJobInfo(JobInfo jobInfo, boolean newOne) {
        try {
            jobCache.put(jobInfo.getJobId(), jobInfo);
            String blob =
                getRepository().getRepository().encodeObject(jobInfo);
            if (newOne) {
                String insert = SqlUtil.makeInsert(JobInfo.DB_TABLE,
                                    JobInfo.DB_COLUMNS);
                getDatabaseManager().executeInsert(insert, new Object[] {
                    jobInfo.getJobId(), jobInfo.getEntryId(), new Date(),
                    jobInfo.getUser(), jobInfo.getType(), blob
                });
            } else {
                getDatabaseManager().update(
                    JobInfo.DB_TABLE, JobInfo.DB_COL_ID,
                    jobInfo.getJobId().toString(),
                    new String[] { JobInfo.DB_COL_JOB_INFO_BLOB },
                    new Object[] { blob });
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc);

        }
    }


    /**
     * find the jobinfo for the given job. This looks in the runningJobs and if its not there looks at the database
     *
     * @param jobId The job ID
     *
     * @return job id
     */
    public JobInfo getJobInfo(Object jobId) {
        try {
            JobInfo jobInfo = jobCache.get(jobId);
            if (jobInfo == null) {
                jobInfo = doMakeJobInfo(jobId);
                jobCache.put(jobInfo.getJobId(), jobInfo);

                return jobInfo;
            }

            return jobInfo;
        } catch (Exception exc) {
            logError("RAMADDA: Could not read processing job: " + jobId, exc);
        }

        return null;
    }



    /**
     * _more_
     *
     * @param jobInfo _more_
     * @param error _more_
     */
    public void setError(JobInfo jobInfo, String error) {
        jobInfo.setError(error);
        writeJobInfo(jobInfo);
    }





    /**
     * Is the job still running
     *
     * @param jobId The job ID
     *
     * @return is the job OK
     */
    public boolean jobOK(Object jobId) {
        if (jobId == null) {
            return true;
        }

        return runningJobs.get(jobId) != null;
    }

    /**
     * _more_
     *
     * @param jobInfo _more_
     */
    public void jobHasStarted(JobInfo jobInfo) {
        runningJobs.put(jobInfo.getJobId(), jobInfo);
        writeJobInfo(jobInfo, true);
    }


    /**
     * _more_
     *
     * @param jobInfo _more_
     */
    public void jobHasFinished(JobInfo jobInfo) {
        removeJob(jobInfo);
        jobInfo.setStatus(jobInfo.STATUS_DONE);
        jobInfo.setEndDate(new Date());
        writeJobInfo(jobInfo);
    }


    /**
     * _more_
     *
     * @param jobInfo _more_
     */
    public void jobWasCancelled(JobInfo jobInfo) {
        removeJob(jobInfo);
        jobInfo.setStatus(jobInfo.STATUS_CANCELLED);
        writeJobInfo(jobInfo);
    }

    /**
     * _more_
     *
     * @param jobInfo _more_
     */
    public void removeJob(JobInfo jobInfo) {
        runningJobs.remove(jobInfo.getJobId());
    }


    /**
     * utility to execute the list of callable objects
     *
     *
     * @param request _more_
     * @param callable callable object
     *
     * @throws Exception On badness
     */
    public void invokeAndWait(Request request, Callable<Boolean> callable)
            throws Exception {
        List<Callable<Boolean>> callables =
            new ArrayList<Callable<Boolean>>();
        callables.add(callable);
        invokeAndWait(request, callables);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return "current jobs:" + currentJobs + " completed jobs:" + totalJobs;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean canAcceptJob() {
        return currentJobs <= 10;
    }

    /**
     * _more_
     */
    public void checkNewJobOK() {
        if ( !canAcceptJob()) {
            throw new IllegalStateException(
                "RAMADDA: Too many outstanding processing jobs");
        }
    }

    /**
     * execute the list of callables in the executor thread pool
     *
     *
     * @param request _more_
     * @param callables callables to execute
     *
     * @throws Exception On badness
     */
    public void invokeAndWait(Request request,
                              List<Callable<Boolean>> callables)
            throws Exception {
        checkNewJobOK();

        long t1 = System.currentTimeMillis();
        try {
            synchronized (MUTEX) {
                currentJobs++;
                System.err.println("RAMADDA: job queued: " + this);
            }
            List<Future<Boolean>> results =
                getExecutor().invokeAll(callables);
            for (Future future : results) {
                try {
                    future.get();
                } catch (ExecutionException ex) {
                    throw (Exception) ex.getCause();
                }
            }
        } catch (Exception exc) {
            System.err.println("RAMADDA: error: " + exc);
            exc.printStackTrace();

            throw exc;
        } finally {
            synchronized (MUTEX) {
                long t2 = System.currentTimeMillis();
                currentJobs--;
                totalJobs++;
                System.err.println("RAMADDA: job end time:" + (t2 - t1)
                                   + ": " + this);
            }
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleJobStatusRequest(Request request, Entry entry)
            throws Exception {
        String jobId = request.getString(JobInfo.ARG_JOB_ID, (String) null);
        StringBuffer sb  = new StringBuffer();
        StringBuffer xml = new StringBuffer();
        addHtmlHeader(request, sb);
        JobInfo jobInfo = getJobInfo(jobId);
        if (jobInfo == null) {
            return makeRequestErrorResult(request,
                                          "No job found with id = " + jobId);
        }

        if (jobInfo.isCancelled()) {
            if (request.responseInXml()) {
                xml.append(XmlUtil.tag(TAG_JOB,
                                       XmlUtil.attrs(new String[] {
                                           JobManager.ATTR_STATUS,
                                           STATUS_CANCELLED })));

                return makeRequestOKResult(request, xml.toString());
            }

            return makeRequestErrorResult(request,
                                          "The job has been cancelled.");
        }

        if (jobInfo.isInError() && request.responseInXml()) {
            return makeRequestErrorResult(request,
                                          "An error has occurred:"
                                          + jobInfo.getError());
        }


        if (request.get(ARG_CANCEL, false)) {
            runningJobs.remove(jobId);
            jobInfo.setStatus(jobInfo.STATUS_CANCELLED);
            writeJobInfo(jobInfo);

            return makeRequestOKResult(request,
                                       "The job has been cancelled.");
        }

        return null;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param message _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result makeRequestErrorResult(Request request, String message)
            throws Exception {
        if (request.responseInXml()) {
            return makeRequestErrorResult(request, message);
        }
        StringBuffer sb = new StringBuffer();
        addHtmlHeader(request, sb);
        sb.append(getPageHandler().showDialogNote(message));

        return new Result("", sb);
    }

    /**
     * This creates the appropriate response for a Point data API request.
     * If its the  API this creates the response  xml. If its the browser
     * then this creates a web page
     *
     * @param request http request
     * @param message error message
     *
     * @return xml or html result
     */
    public Result makeRequestOKResult(Request request, String message) {
        if (request.responseInXml()) {
            return new Result(XmlUtil.tag(TAG_RESPONSE,
                                          XmlUtil.attr(ATTR_CODE, CODE_OK),
                                          message), MIME_XML);

        }
        if (request.responseInText()) {
            return new Result(message, "text");
        }

        return new Result("", new StringBuffer(message));
    }







    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     */
    public void addHtmlHeader(Request request, StringBuffer sb) {}




}
