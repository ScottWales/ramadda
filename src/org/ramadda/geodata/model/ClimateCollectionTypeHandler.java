/*
* Copyright 2008-2013 Jeff McWhirter/ramadda.org
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

package org.ramadda.geodata.model;


import org.ramadda.data.process.DataProcess;
import org.ramadda.data.process.DataProcessInput;
import org.ramadda.data.process.DataProcessOperand;
import org.ramadda.data.process.DataProcessOutput;
//import org.ramadda.geodata.cdmdata.CDOOutputHandler;
import org.ramadda.geodata.cdmdata.NCOOutputHandler;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Result;
import org.ramadda.repository.type.CollectionTypeHandler;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import java.io.File;

import java.util.ArrayList;
import java.util.List;


/**
 * Class to handle climate model collections
 */
public class ClimateCollectionTypeHandler extends CollectionTypeHandler {


    /** data process id */
    public static final String ARG_DATA_PROCESS_ID = "data_process_id";

    /** list of data processes */
    private List<DataProcess> processes = new ArrayList<DataProcess>();

    /** NCL output handler */
    private NCLOutputHandler nclOutputHandler;

    /** image request id */
    public static final String REQUEST_IMAGE = "image";

    /** GoogleEarth kmz request id */
    public static final String REQUEST_KMZ = "kmz";

    /** Timeseries request id */
    public static final String REQUEST_TIMESERIES = "timeseries";

    /**
     * Create a ClimateCollectionTypeHandler
     *
     * @param repository the Repository
     * @param entryNode  the entry Element
     *
     * @throws Exception Problem creating handler
     */
    public ClimateCollectionTypeHandler(Repository repository,
                                        Element entryNode)
            throws Exception {
        super(repository, entryNode);
        //processes.addAll(new CDOOutputHandler(repository).getDataProcesses());
        processes.add(new CDOAreaStatisticsProcess(repository));
        //        processes.addAll(new NCOOutputHandler(repository).getDataProcesses());
        nclOutputHandler = new NCLOutputHandler(repository);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<DataProcess> getDataProcesses() {
        return processes;
    }

    /**
     * Get the HTML display for this type
     *
     * @param request  the Request
     * @param entry    the entry
     * @param subGroups    the subgroups
     * @param entries      the Entries
     *
     * @return  the Result
     *
     * @throws Exception  problem getting the HTML
     */
    @Override
    public Result getHtmlDisplay(Request request, Entry entry,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        //Always call this to initialize things
        getGranuleTypeHandler();

        //Check if the user clicked on tree view, etc.
        if ( !isDefaultHtmlOutput(request)) {
            return null;
        }
        Result result = processRequest(request, entry);
        if (result != null) {
            return result;
        }


        StringBuffer sb = new StringBuffer();
        sb.append(entry.getDescription());
        StringBuffer js     = new StringBuffer();
        String       formId = openForm(request, entry, sb, js);

        addSelectorWidgets(request, entry, sb, js, formId);
        addProcessWidgets(request, entry, sb, js, formId);

        sb.append(HtmlUtils.script(js.toString()));
        sb.append(HtmlUtils.formClose());

        //        appendSearchResults(request, entry, sb);
        return new Result(msg(getLabel()), sb);
    }

    /**
     * Add the selector widgets
     *
     * @param request the Request
     * @param entry   the Entry
     * @param sb      the HTML buffer
     * @param js      the JavaScript buffer
     * @param formId  the form identifier
     *
     * @throws Exception  problem creating the form
     */
    protected void addSelectorWidgets(Request request, Entry entry,
                                      StringBuffer sb, StringBuffer js,
                                      String formId)
            throws Exception {

        StringBuffer selectorSB = new StringBuffer();
        selectorSB.append(HtmlUtils.formTable());
        addSelectorsToForm(request, entry, selectorSB, formId, js);
        String searchButton = JQ.button("Search", formId + "_search", js,
                                        HtmlUtils.call(formId + ".search",
                                            "event"));
        String downloadButton = JQ.button("Download Data",
                                          formId + "_do_download", js,
                                          HtmlUtils.call(formId
                                              + ".download", "event"));
        selectorSB.append(HtmlUtils.formEntry("",
                searchButton + HtmlUtils.space(4) + downloadButton));
        selectorSB.append(HtmlUtils.formTableClose());


        sb.append(
            "<table width=100% border=0 cellspacing=0 cellpadding=0><tr valign=top>");
        sb.append("<td width=30%>");
        sb.append(header(msg("Select Data")));
        sb.append(HtmlUtils.div(selectorSB.toString(),
                                HtmlUtils.cssClass("entryselect")));
        sb.append("</td><td>");
        sb.append(HtmlUtils.div("",
                                HtmlUtils.cssClass("entryoutput")
                                + HtmlUtils.id(formId + "_output_list")));
        sb.append("</td></tr>");
        sb.append("</table>");
    }

    /**
     * Add the processing widgets
     *
     * @param request the Request
     * @param entry   the Entry
     * @param sb      the HTML buffer
     * @param js      the JavaScript buffer
     * @param formId  the form identifier
     *
     * @throws Exception Problem creating widgets
     */
    protected void addProcessWidgets(Request request, Entry entry,
                                     StringBuffer sb, StringBuffer js,
                                     String formId)
            throws Exception {
        // for now, don't add in the process widgets - just do a search/download.
        if (true) {
            return;
        }
        String processButtons =
        //JQ.button("Download Data", formId+"_do_download",js, HtmlUtils.call(formId +".download","event"));
        /*
JQ.button(
    "Download Data", formId + "_do_download", js,
    HtmlUtils.call(formId + ".download", "event")) + " "
        +
        */
        JQ.button(
            "Plot Map", formId + "_do_image", js,
            HtmlUtils.call(formId + ".makeImage", "event")) + " "
                + JQ.button(
                    "Google Earth", formId + "_do_kmz", js,
                    HtmlUtils.call(formId + ".makeKMZ", "event")) + " "
                        + JQ.button(
                            "Time Series", formId + "_do_timeseries", js,
                            HtmlUtils.call(
                                formId + ".makeTimeSeries", "event"));
        List<String> processTabs   = new ArrayList<String>();
        List<String> processTitles = new ArrayList<String>();

        StringBuffer settingsSB    = new StringBuffer();
        settingsSB.append(HtmlUtils.radio(ARG_DATA_PROCESS_ID, "none", true));
        settingsSB.append(HtmlUtils.space(1));
        settingsSB.append(msg("No Processing"));
        settingsSB.append(HtmlUtils.br());

        processTitles.add(msg("Settings"));
        processTabs.add(HtmlUtils.div(settingsSB.toString(),
                                      HtmlUtils.style("min-height:200px;")));
        for (DataProcess process : processes) {
            //TODO: add radio buttons
            StringBuffer tmpSB = new StringBuffer();
            tmpSB.append(HtmlUtils.radio(ARG_DATA_PROCESS_ID,
                                         process.getDataProcessId(), false));
            tmpSB.append(HtmlUtils.space(1));
            tmpSB.append(msg("Select"));
            tmpSB.append(HtmlUtils.br());
            DataProcessOperand op = new DataProcessOperand(entry);
            process.addToForm(request, new DataProcessInput(op), tmpSB);
            processTabs.add(
                HtmlUtils.div(
                    tmpSB.toString(), HtmlUtils.style("min-height:200px;")));
            processTitles.add(process.getDataProcessLabel());
        }

        sb.append(
            "<table width=100% border=0 cellspacing=0 cellpadding=0><tr valign=top>");
        sb.append("<td width=30%>");
        sb.append(header(msg("Process Selected Data")));
        HtmlUtils.makeAccordian(sb, processTitles, processTabs);
        sb.append(processButtons);
        sb.append("</td><td>");
        sb.append(HtmlUtils.div("",
                                HtmlUtils.cssClass("entryoutput")
                                + HtmlUtils.id(formId + "_output_image")));
        sb.append("</td></tr></table>");
    }

    /**
     * Process the request
     *
     * @param request  the Request
     * @param entry    the Entry
     *
     * @return the Result
     *
     * @throws Exception problems arose
     */
    public Result processRequest(Request request, Entry entry)
            throws Exception {
        Result result = super.processRequest(request, entry);
        if (result != null) {
            return result;
        }
        String what = request.getString(ARG_REQUEST, (String) null);
        if (what == null) {
            return null;
        }
        if (what.equals(REQUEST_IMAGE) || what.equals(REQUEST_KMZ)
                || what.equals(REQUEST_TIMESERIES)) {
            return processDataRequest(request, entry, false);
        }

        return null;
    }


    public List<DataProcess> getDataProcessesToRun(Request request) throws Exception {
        List<DataProcess> processesToRun = new ArrayList<DataProcess>();
        String selectedProcess = request.getString(ARG_DATA_PROCESS_ID,
                                                   (String) null);
        if (selectedProcess != null) {
            for (DataProcess process : processes) {
                if (process.getDataProcessId().equals(selectedProcess)) {
                    processesToRun.add(process);
                }
            }
        }
        return processesToRun;
    }


    /**
     * Process the data request
     *
     * @param request  the Request
     * @param entry    the Entry
     * @param doDownload  true to download the result
     *
     * @return  the Result of the Request
     *
     * @throws Exception  on badness
     */
    public Result processDataRequest(Request request, Entry entry,
                                     boolean doDownload)
            throws Exception {
        //        File   imageFile =     getStorageManager().getTmpFile(request, "test.png");
        //        BufferedImage image = new BufferedImage(600,400,BufferedImage.TYPE_INT_RGB);
        //        Graphics2D g = (Graphics2D) image.getGraphics();

        //Get the entries
        List<Entry> entries = processSearch(request, entry, true);

        List<File>  files   = new ArrayList<File>();
        //Process each one in turn
        boolean didProcess = false;
        List<DataProcess> processesToRun = getDataProcessesToRun(request);
        File processDir = getStorageManager().createProcessDir();
        for(DataProcess process: processesToRun) {
            System.err.println("MODEL: applying process: "
                               + process.getDataProcessLabel());
            DataProcessOperand op = new DataProcessOperand(entries);
            DataProcessInput dpi = new DataProcessInput(processDir, op);
            didProcess = true;
            DataProcessOutput output = process.processRequest(request, dpi);
            if (output.hasOutput()) {
                for (Entry outFile : output.getEntries()) {
                    if (entry.getResource().isFile()) {
                        files.add(entry.getResource().getTheFile());
                    }
                }
            }
        }

        String processId = processDir.getName();
        String processEntryId = getStorageManager().getProcessDirEntryId(processId);

        if(false) {
            String       entryUrl = 
                HtmlUtils.url(request.getAbsoluteUrl(getRepository().URL_ENTRY_SHOW),
                              ARG_ENTRYID, processEntryId);
            return new Result(entryUrl);
        }


        if ( !didProcess) {
            for (Entry granule : entries) {
                if (granule.isFile()) {
                    files.add(granule.getFile());
                }
            }
        }

        if (doDownload) {
            return zipFiles(request,
                            IOUtil.stripExtension(entry.getName()) + ".zip",
                            files);
        }

        //Make the image
        File imageFile = nclOutputHandler.processRequest(request,
                             files.get(0));

        //And return the result
        String extension = IOUtil.getFileExtension(imageFile.toString());

        return new Result("",
                          getStorageManager().getFileInputStream(imageFile),
                          getRepository().getMimeTypeFromSuffix(extension));
    }


    /**
     *  Overwrite the base class and route it through processImageRequest
     *
     * @param request process a download request
     * @param entry   the Entry
     *
     * @return the Result
     *
     * @throws Exception problem downloading data
     */
    @Override
    public Result processDownloadRequest(Request request, Entry entry)
            throws Exception {
        return processDataRequest(request, entry, true);
    }

}
