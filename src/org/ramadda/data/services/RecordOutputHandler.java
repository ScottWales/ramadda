/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
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

package org.ramadda.data.services;


import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;

import org.ramadda.repository.job.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.TypeHandler;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.SelectionRectangle;

import org.ramadda.util.TempDir;

import org.w3c.dom.*;

import ucar.unidata.data.gis.KmlUtil;


import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import java.awt.image.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


import java.util.zip.*;



/**
 */
public class RecordOutputHandler extends OutputHandler implements RecordConstants {

    /** output type */
    public OutputType OUTPUT_RESULTS;

    /** _more_ */
    public static final String ARG_SKIP = "skip";

    /** _more_ */
    public static final String ARG_PARAMETER = "parameter";

    /** _more_ */
    public static final String ARG_FIELD_USE = "field_use";

    /** _more_ */
    public static final String SESSION_PREFIX = "record.";

    /** _more_ */
    public static final String PROP_TTL = "record.files.ttl";

    /** Max number of points an anonymous user is allowed to access */
    public static final long POINT_LIMIT_ANONYMOUS = 200000000;

    /** Max number of points a non-anonymous user is allowed to access */
    public static final long POINT_LIMIT_USER = POINT_LIMIT_ANONYMOUS * 5;

    /** Where products get put */
    private TempDir productDir;


    /** _more_ */
    private RecordJobManager jobManager;


    /** _more_ */
    private RecordFormHandler formHandler;


    /**
     * constructor. This gets called by the Repository via reflection
     * This class is specified in outputhandlers.xml
     *
     *
     * @param repository the repository
     * @param element the xml from outputhandlers.xml
     * @throws Exception on badness
     */
    public RecordOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        getProductDir();
    }


    /**
     * _more_
     *
     * @param jobManager _more_
     */
    protected void setRecordJobManager(RecordJobManager jobManager) {
        this.jobManager = jobManager;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public RecordEntry doMakeEntry(Request request, Entry entry) {
        return new RecordEntry(this, request, entry);
    }


    /**
     * This makes  a list of RecordEntry which is a wrapper around Entry.
     *
     * @param request the request
     * @param entries The entries to process
     * @param checkIfSelected If true then check if the request has a ARG_RECORDENTRY is for the given Entry
     *
     * @return List of RecordEntry
     */
    public List<RecordEntry> makeRecordEntries(Request request,
                                             List<Entry> entries,
                                             boolean checkIfSelected) {
        List<RecordEntry> recordEntries = new ArrayList<RecordEntry>();
        boolean          hasAnyArgs   = request.exists(ARG_RECORDENTRY_CHECK);
        List<String>     ids          = request.get(ARG_RECORDENTRY,
                                                    new ArrayList<String>());

        for (Entry entry : entries) {
            if ( !canHandleEntry(entry)) {
                continue;
            }
            if (checkIfSelected && hasAnyArgs
                && !ids.contains(entry.getId())) {
                continue;
            }
            if (entry.getTypeHandler() instanceof RecordTypeHandler) {
                recordEntries.add(doMakeEntry(request, entry));
            } else {
            }
        }
        return recordEntries;
    }


    /**
     * Is the given entry a point type or a point collection type
     *
     * @param entry The entry
     *
     * @return Is the entry some point type
     */
    public boolean canHandleEntry(Entry entry) {
        return false;
    }


    public List<RecordEntry> doSubsetEntries(
            Request request, List<? extends RecordEntry> recordEntries)
            throws Exception {
        List<RecordEntry>  result  = new ArrayList<RecordEntry>();
        for(RecordEntry recordEntry: recordEntries) {
            RecordTypeHandler recordType = (RecordTypeHandler) recordEntry.getEntry().getTypeHandler();
            if(recordType.includedInRequest(request, recordEntry)) {
                result.add(recordEntry);
            }
        }
        return result;
    }

    public void getFilters(Request request, Entry entry,
                           RecordFile recordFile, List<RecordFilter> filters) {
        RecordTypeHandler typeHandler = (RecordTypeHandler) entry.getTypeHandler();
        typeHandler.getFilters(request, entry, recordFile, filters);
    }

    /**
     */
    public void shutdown() {
        //        super.shutdown();
        if (jobManager != null) {
            jobManager.shutdown();
        }

    }






    /**
     * Creates the directory in the ramadda home dir where we write products to
     * It sets up a scour so files older than 7 days get removed
     *
     * @return The product dir
     *
     * @throws Exception On badness
     */
    public File getProductDir() throws Exception {
        if (productDir == null) {
            TempDir tempDir =
                getStorageManager().makeTempDir(getProductDirName());
            //keep things around for 7 day
            int days = getProductDirTTL();
            tempDir.setMaxAge(1000 * 60 * 60 * 24 * days);
            productDir = tempDir;
        }

        return productDir.getDir();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getProductDirName() {
        return getDomainBase() +"_products";
    }

    public String getDomainBase() {
        return "record";
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getProductDirTTL() {
        return getRepository().getProperty(PROP_TTL, 7);
    }


    /**
     * Get the job manager
     *
     * @return the job manager
     */
    public RecordJobManager getRecordJobManager() {
        return jobManager;
    }

    /**
     * Set the FormHandler property.
     *
     * @param value The new value for FormHandler
     */
    public void setFormHandler(RecordFormHandler value) {
        formHandler = value;
    }

    /**
     * Get the FormHandler property.
     *
     * @return The FormHandler
     */
    public RecordFormHandler getFormHandler() {
        if(formHandler == null) {
            formHandler = new RecordFormHandler(this);
        }
        return formHandler;
    }




    /**
     * Wrapper around JobManager.jobOK
     *
     * @param jobId processing job id
     *
     * @return is job running and ok
     */
    public boolean jobOK(Object jobId) {
        if (jobManager == null) {
            return false;
        }
        return jobManager.jobOK(jobId);
    }




    /**
     * This gets called to add links into the entry menus in the HTML views.
     *
     * @param request the request
     * @param state This holds the group, entry, children, etc.
     * @param links list to add to
     *
     *
     * @throws Exception on badness
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        /*

        Entry entry = state.getEntry();
        if (entry == null) {
            return;
        }
        if ( !canHandleEntry(entry)) {
            return;
        }

        if (entry.getTypeHandler() instanceof PointCollectionTypeHandler) {
            links.add(makeLink(request, state.getEntry(), OUTPUT_FORM));
            return;
        }

        if ( !state.entry.isFile()) {
            return;
        }


        if ( !getRepository().getAccessManager().canAccessFile(request,
                state.entry)) {
            return;
        }


        links.add(makeLink(request, state.getEntry(), OUTPUT_MAP));
        links.add(makeLink(request, state.getEntry(), OUTPUT_FORM));
        links.add(makeLink(request, state.getEntry(), OUTPUT_VIEW));
        links.add(makeLink(request, state.getEntry(), OUTPUT_METADATA));

        //Don't add these for now but these are the direct URLS to the data products
        if (false) {
            String path = "/"
                          + IOUtil.stripExtension(state.getEntry().getName());
            links.add(makeLink(request, state.getEntry(), OUTPUT_KMZ,
                               path + ".kmz"));

            links.add(makeLink(request, state.getEntry(), OUTPUT_IMAGE,
                               path + ".png"));
            links.add(makeLink(request, state.getEntry(), OUTPUT_HILLSHADE,
                               path + ".png"));


            links.add(makeLink(request, state.getEntry(), OUTPUT_CSV,
                               path + ".csv"));

            //Lets not have the 3d points  for now
            // if (hasWaveform(state.entry)) {
            if (false) {
                links.add(makeLink(request, state.getEntry(),
                                   OUTPUT_LATLONALT3DCSV, path + ".csv"));
            } else {
                links.add(makeLink(request, state.getEntry(),
                                   OUTPUT_LATLONALTCSV, path + ".csv"));

                links.add(makeLink(request, state.getEntry(),
                                   OUTPUT_LATLONALTBIN, path + ".llab"));
            }
            links.add(makeLink(request, state.getEntry(), OUTPUT_LAS,
                               path + ".las"));
            links.add(makeLink(request, state.getEntry(), OUTPUT_ASC,
                               path + ".asc"));
            //            links.add(makeLink(request, state.getEntry(), OUTPUT_NC,
            //                               path + ".nc"));
        }
        //TODO: What to do with waveforms?
        //            if(hasWaveform(state.entry)) {
        //                links.add(makeLink(request, state.getEntry(), OUTPUT_WAVEFORM));
        */
    }





    /**
     *
     * @param request the request
     * @param outputType The type of output
     * @param entry The entry
     *
     * @return the result
     *
     * @throws Exception on badness
     */
    public Result outputEntry(Request request, OutputType outputType,
                              final Entry entry)
            throws Exception {

        if (jobManager == null) {
            return null;
        }

        //Route any of the processing job requests to the JobManager
        if (request.defined(JobInfo.ARG_JOB_ID)) {
            return jobManager.handleJobStatusRequest(request, entry);
        }

        //Check for access to the file
        if ( !getRepository().getAccessManager().canAccessFile(request,
                entry)) {
            throw new AccessException("Cannot access data", request);
        }




        return null;

    }


    /**
     * @param request the request
     * @param outputType output type
     * @param group The group
     * @param subGroups groups
     * @param entries entries
     *
     * @return The result
     *
     * @throws Exception on badness
     */
    public Result outputGroup(final Request request,
                              final OutputType outputType, final Entry group,
                              final List<Entry> subGroups,
                              final List<Entry> entries)
            throws Exception {


        if (request.defined(JobInfo.ARG_JOB_ID)) {
            return jobManager.handleJobStatusRequest(request, group);
        }

        return null;
    }


    /**
     * If there was an error on a job request then this creates the appropriate
     * response. If its the NLAS API this creates the error xml. If its the browser
     * then this creates a web page
     *
     * @param request http request
     * @param message error message
     *
     * @return xml or html result
     */
    public Result makeRequestErrorResult(Request request, String message) {
        if (request.responseInXml()) {
            return new Result(
                XmlUtil.tag(
                    TAG_RESPONSE, XmlUtil.attr(ATTR_CODE, CODE_ERROR),
                    message), MIME_XML);

        }

        return new Result("", new StringBuffer(message));
    }

    /**
     * This creates the appropriate response for an NLAS API request.
     * If its the NLAS API this creates the response  xml. If its the browser
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


    /** _more_ */
    private int callCnt = 0;



    /**
     * _more_
     *
     * @param msg _more_
     */
    public void memoryCheck(String msg) {
        //        Runtime.getRuntime().gc();
        //        getLogManager().logInfoAndPrint(msg + ((int)(Misc.usedMemory()/1000000.0))+"MB");
    }


    /**
     * _more_
     *
     * @param request _more_
     */
    public void storeSession(Request request) {
        request.putSessionIfDefined(ARG_AREA_NORTH, SESSION_PREFIX);
        request.putSessionIfDefined(ARG_AREA_WEST, SESSION_PREFIX);
        request.putSessionIfDefined(ARG_AREA_SOUTH, SESSION_PREFIX);
        request.putSessionIfDefined(ARG_AREA_EAST, SESSION_PREFIX);
    }


    /**
     * This gets the selected product formats.
     *
     * @param request The request
     *
     * @return set of formats
     */
    public HashSet<String> getProductFormats(Request request) {
        HashSet<String> formats = new HashSet<String>();
        for (String format :
                (List<String>) request.get(ARG_PRODUCT,
                                           new ArrayList<String>())) {
            formats.add(format);
        }

        return formats;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param fields _more_
     *
     * @return _more_
     */
    public List<RecordField> getFields(Request request,
                                       List<RecordField> fields) {
        HashSet<String> selectedFields = new HashSet<String>();
        selectedFields.addAll((List<String>) request.get(ARG_FIELD_USE,
                new ArrayList<String>()));
        List<RecordField> fieldsToUse = new ArrayList<RecordField>();
        for (RecordField field : fields) {
            if (selectedFields.size() > 0) {
                if (selectedFields.contains(field.getName())) {
                    fieldsToUse.add(field);
                }
            } else {
                fieldsToUse.add(field);
            }
        }
        if (fieldsToUse.size() == 0) {
            //TODO: Do we default to all fields if none are selected
            return fields;
        } else {
            return fieldsToUse;
        }
    }



    /**
     * Create a record filter from the url args. This can make a spatial bounds filter,
     * a probabilistic filter, and a value range filter.
     *
     * @param request the request
     * @param entry The entry
     * @param recordFile _more_
     *
     * @return The record filter.
     */
    public RecordFilter getFilter(Request request, Entry entry,
                                  RecordFile recordFile) {
        List<RecordFilter> filters = new ArrayList<RecordFilter>();
        getFilters(request, entry, recordFile, filters);
        if (filters.size() == 0) {
            return null;
        }
        if (filters.size() == 1) {
            return filters.get(0);
        }
        return new CollectionRecordFilter(filters);
    }




    /**
     * Make if needed and return the directory to store products to for the given job id
     *
     * @param jobId The job ID
     *
     * @return product dir
     *
     * @throws Exception On badness
     */
    public File getProductDir(Object jobId) throws Exception {
        File theProductDir = new File(IOUtil.joinDir(getProductDir(),
                                 jobId.toString()));
        IOUtil.makeDir(theProductDir);

        return theProductDir;
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param ext _more_
     *
     * @return _more_
     */
    public String getOutputFilename(Entry entry, String ext) {
        return IOUtil.stripExtension(entry.getName()) + ext;
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
    public Result outputEntryBounds(Request request, Entry entry)
            throws Exception {
        return new Result(entry.getNorth() + "," + entry.getWest() + ","
                          + entry.getSouth() + "," + entry.getEast(), "text");
    }




    /**
     * _more_
     *
     * @param request The request
     * @param jobId The job ID
     * @param entry _more_
     * @param ext _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public OutputStream getOutputStream(Request request, Object jobId,
                                        Entry entry, String ext)
            throws Exception {
        if (entry == null) {
            System.err.println("BAD ENTRY");
        }
        String fileName = getOutputFilename(entry, ext);
        if (jobId == null) {
            //            System.err.println ("NLAS: writing directly " + request.getOutputStream().getClass().getName());
            return request.getOutputStream();
            //            return new BufferedOutputStream(request.getOutputStream(),
            //                                            10000);
        }
        //        System.err.println ("NLAS: writing to file");
        File file = new File(IOUtil.joinDir(getProductDir(jobId), fileName));

        //return  getStorageManager().getUncheckedFileOutputStream(file);
        return new BufferedOutputStream(
            getStorageManager().getUncheckedFileOutputStream(file), 100000);
    }


    /**
     * _more_
     *
     * @param request The request
     * @param jobId The job ID
     * @param entry _more_
     * @param ext _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public PrintWriter getPrintWriter(Request request, Object jobId,
                                      Entry entry, String ext)
            throws Exception {
        //        if (jobId == null) {
        //            return request.getHttpServletResponse().getWriter();
        //        }
        return new PrintWriter(getOutputStream(request, jobId, entry, ext));
    }

    /**
     * _more_
     *
     * @param request The request
     * @param jobId The job ID
     * @param entry _more_
     * @param ext _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public DataOutputStream getDataOutputStream(Request request,
            Object jobId, Entry entry, String ext)
            throws Exception {
        return new DataOutputStream(getOutputStream(request, jobId, entry,
                ext));
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Result getDummyResult() {
        Result result = new Result();
        result.setNeedToWrite(false);

        return result;
    }


    /**
     * _more_
     *
     * @param request the request
     * @param dflt _more_
     * @param arg _more_
     *
     * @return _more_
     */
    public int getSkip(Request request, int dflt, String arg) {
        String skip = request.getString(arg, "");
        if (skip.equals("${skip}")) {
            return dflt;
        }

        return request.get(arg, dflt);
    }

    /**
     * This subsets the point file. The return format is the same format as the input file
     *
     * @param request the request
     * @param mainEntry Either the Point Collection or File Entry
     * @param pointEntries _more_
     * @param jobInfo processing job
     *
     * @return result
     *
     * @throws Exception on badness
     */
    public Result outputEntrySubset(Request request, Entry mainEntry,
                                    List<?extends RecordEntry> recordEntries,
                                    JobInfo jobInfo)
            throws Exception {
        RecordEntry recordEntry = recordEntries.get(0);
        String     ext        =
            IOUtil.getFileExtension(recordEntry.getRecordFile().getFilename());
        VisitInfo    visitInfo = recordEntry.getRecordFile().doMakeVisitInfo();
        OutputStream outputStream = getOutputStream(request,
                                        jobInfo.getJobId(), mainEntry, ext);
        RecordIO recordOutput = new RecordIO(outputStream);
        recordEntry.getRecordFile().write(recordOutput, visitInfo,
                                        recordEntry.getFilter(), true);
        for (int i = 1; i < recordEntries.size(); i++) {
            recordEntry = recordEntries.get(i);
            recordEntry.getRecordFile().write(recordOutput, visitInfo,
                                            recordEntry.getFilter(), false);
        }
        recordOutput.close();
        jobInfo.setNumPoints(visitInfo.getCount());

        return getDummyResult();
    }

    /**
     *
     * @param entry The entry
     *
     * @return A new PointFile.
     *
     * @throws Exception on badness
     */
    public RecordFile doMakeRecordFile(Entry entry) throws Exception {
        RecordTypeHandler type = (RecordTypeHandler) entry.getTypeHandler();
        return (RecordFile)type.doMakeRecordFile(entry);
    }

    public void getServices(Request request, Entry entry,
                            List<Service> services) {
    }


    public String getIconUrl(Request request, String icon) {
        return request.getAbsoluteUrl(getRepository().iconUrl(icon));
    }



}
