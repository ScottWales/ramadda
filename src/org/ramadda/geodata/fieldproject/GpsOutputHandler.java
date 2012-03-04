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

package org.ramadda.geodata.fieldproject;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.TempDir;

import org.w3c.dom.*;

import ucar.unidata.ui.HttpFormEntry;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.LogUtil;

import ucar.unidata.util.HtmlUtil;



import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;


import ucar.unidata.util.IOUtil;


import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.*;




/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GpsOutputHandler extends OutputHandler {

    /** _more_ */
    private static final String RINEX_SUFFIX = ".rinex";


    /** _more_          */
    private static final String ARG_OPUS_EMAIL = "email_address";

    /** _more_          */
    private static final String ARG_OPUS_ANTENNA = "ant_type";

    private static final String ARG_OPUS_RAPID = "opus.rapid";

    /** _more_          */
    private static final String ARG_OPUS_HEIGHT = "height";


    /** _more_ */
    private static final String ARG_RINEX_PROCESS = "rinex.process";

    /** _more_          */
    private static final String ARG_OPUS_PROCESS = "opus.process";

    /** _more_ */
    private static final String ARG_RINEX_FILE = "rinex.file";

    /** _more_          */
    private static final String ARG_GPS_FILE = "gps.file";



    /** _more_ */
    private static final String ARG_RINEX_DOWNLOAD = "rinex.download";

    /** _more_ */
    private String teqcPath;

    /** _more_ */
    private TempDir productDir;


    /** The output type */
    public static final OutputType OUTPUT_GPS_TEQC =
        new OutputType("Convert to RINEX", "fieldproject.gps.teqc",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       "/fieldproject/gps.png", "Field Project");

    /** _more_          */
    public static final OutputType OUTPUT_GPS_OPUS =
        new OutputType("Submit to OPUS", "fieldproject.gps.opus",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       "/fieldproject/gps.png", "Field Project");

    /**
     * ctor
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public GpsOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_GPS_TEQC);
        addType(OUTPUT_GPS_OPUS);
        teqcPath = getProperty("fieldproject.teqc", null);
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private File getProductDir() throws Exception {
        if (productDir == null) {
            TempDir tempDir = getStorageManager().makeTempDir("gpsproducts");
            //keep things around for 7 day  
            tempDir.setMaxAge(1000 * 60 * 60 * 24 * 7);
            productDir = tempDir;
        }
        return productDir.getDir();
    }

    /**
     * _more_
     *
     * @param jobId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private File getWorkDir(Object jobId) throws Exception {
        File theProductDir = new File(IOUtil.joinDir(getProductDir(),
                                 jobId.toString()));
        IOUtil.makeDir(theProductDir);
        return theProductDir;
    }



    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    private boolean isRawGps(Entry entry) {
        return entry.getType().equals("project_gps_raw");
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    private boolean isRinex(Entry entry) {
        return entry.getType().equals("project_gps_rinex");
    }


    /**
     * This method gets called to determine if the given entry or entries can be displays as las xml
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if (teqcPath == null) {
            return;
        }
        if (state.group != null) {
            for (Entry child : state.getAllEntries()) {
                if (isRawGps(child)) {
                    links.add(makeLink(request, state.group,
                                       OUTPUT_GPS_TEQC));
                    break;
                }
            }
            for (Entry child : state.getAllEntries()) {
                if (isRinex(child)) {
                    links.add(makeLink(request, state.group,
                                       OUTPUT_GPS_OPUS));
                    break;
                }
            }


        } else if (state.entry != null) {
            if (isRawGps(state.entry)) {
                links.add(makeLink(request, state.entry, OUTPUT_GPS_TEQC));
            }
            if (isRinex(state.entry)) {
                links.add(makeLink(request, state.entry, OUTPUT_GPS_OPUS));
            }
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        if (outputType.equals(OUTPUT_GPS_TEQC)) {
            return outputRinex(request, group, entries);
        }
        return outputOpus(request, group, entries);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);
        if (outputType.equals(OUTPUT_GPS_TEQC)) {
            return outputRinex(request, entry, entries);
        }
        return outputOpus(request, entry, entries);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result zipResults(Request request, Entry mainEntry)
            throws Exception {

        request.setReturnFilename("rinex.zip");
        OutputStream os = request.getHttpServletResponse().getOutputStream();
        request.getHttpServletResponse().setContentType("application/zip");
        ZipOutputStream zos = new ZipOutputStream(os);
        File[] files = getWorkDir(request.getString(ARG_RINEX_DOWNLOAD,
                           "bad")).listFiles();
        for (File f : files) {
            if ( !f.getName().endsWith(RINEX_SUFFIX) || (f.length() == 0)) {
                continue;
            }
            zos.putNextEntry(new ZipEntry(f.getName()));
            InputStream fis =
                getStorageManager().getFileInputStream(f.toString());
            IOUtil.writeTo(fis, zos);
            IOUtil.close(fis);
        }
        IOUtil.close(zos);
        Result result = new Result();
        result.setNeedToWrite(false);
        return result;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result outputRinex(Request request, Entry mainEntry,
                               List<Entry> entries)
            throws Exception {

        if (request.exists(ARG_RINEX_DOWNLOAD)) {
            return zipResults(request, mainEntry);
        }


        StringBuffer sb = new StringBuffer();
        if ( !request.get(ARG_RINEX_PROCESS, false)) {
            sb.append(HtmlUtil.p());
            sb.append(request.form(getRepository().URL_ENTRY_SHOW));
            sb.append(HtmlUtil.hidden(ARG_OUTPUT, OUTPUT_GPS_TEQC.getId()));
            sb.append(HtmlUtil.hidden(ARG_ENTRYID, mainEntry.getId()));
            sb.append(HtmlUtil.hidden(ARG_RINEX_PROCESS, "true"));

            if (entries.size() == 1) {
                sb.append(HtmlUtil.hidden(ARG_GPS_FILE,
                                          entries.get(0).getId()));
            } else {
                sb.append(msgHeader("Select entries"));
                for (Entry entry : entries) {
                    if ( !isRawGps(entry)) {
                        continue;
                    }
                    sb.append(HtmlUtil.checkbox(ARG_GPS_FILE, entry.getId(),
                            true));
                    sb.append(" ");
                    sb.append(entry.getName());
                    sb.append(HtmlUtil.br());
                }
            }


            sb.append(HtmlUtil.p());
            sb.append(HtmlUtil.formTable());
            addPublishWidget(request, mainEntry, sb,
                             msg("Select a folder to publish the RINEX to"),
                             false);
            sb.append(HtmlUtil.formTableClose());

            sb.append(HtmlUtil.submit("Make RINEX"));
            sb.append(HtmlUtil.formClose());
            return new Result("", sb);
        }


        List<String> entryIds = request.get(ARG_GPS_FILE,
                                            new ArrayList<String>());



        boolean anyOK = false;
        sb.append(msgHeader("Results"));
        sb.append("<ul>");
        String uniqueId = getRepository().getGUID();
        File   workDir  = getWorkDir(uniqueId);

        Hashtable<String, Entry> fileToEntryMap = new Hashtable<String,
                                                      Entry>();

        for (String entryId : entryIds) {
            Entry entry = getEntryManager().getEntry(request, entryId);
            if (entry == null) {
                throw new IllegalArgumentException("No entry:" + entryId);
            }

            if ( !isRawGps(entry)) {
                sb.append("<li>");
                sb.append("Skipping:" + entry.getName());
                sb.append(HtmlUtil.p());
                continue;
            }

            File f = entry.getFile();
            if ( !f.exists()) {
                throw new IllegalStateException("File does not exist:" + f);
            }

            File rinexFile = new File(
                                 IOUtil.joinDir(
                                     workDir,
                                     IOUtil.stripExtension(
                                         getStorageManager().getFileTail(
                                             entry)) + RINEX_SUFFIX));
            fileToEntryMap.put(rinexFile.toString(), entry);

            ProcessBuilder pb = new ProcessBuilder(teqcPath, "+out",
                                    rinexFile.toString(), f.toString());
            pb.directory(workDir);
            Process process = pb.start();
            String errorMsg =
                new String(IOUtil.readBytes(process.getErrorStream()));
            int result = process.waitFor();
            sb.append("<li>");
            sb.append(entry.getName());
            if (rinexFile.length() > 0) {
                if (errorMsg.length() > 0) {
                    sb.append(" ... RINEX file generated with warnings:");
                } else {
                    sb.append(" ... RINEX file generated");
                }
                anyOK = true;
            } else {
                sb.append(" ... Error:");
            }
            if (errorMsg.length() > 0) {
                sb.append(
                    "<pre style=\"  border: solid 1px #000; max-height: 150px;overflow-y: auto; \">");
                sb.append(errorMsg);
                sb.append("</pre>");
            }
        }

        sb.append("</ul>");
        if ( !anyOK) {
            return new Result("", sb);
        }

        if (doingPublish(request)) {
            Entry parent = getEntryManager().findGroup(request,
                               request.getString(ARG_PUBLISH_ENTRY
                                   + "_hidden", ""));
            if (parent == null) {
                throw new IllegalArgumentException("Could not find folder");
            }
            if ( !getAccessManager().canDoAction(request, parent,
                    Permission.ACTION_NEW)) {
                throw new AccessException("No access", request);
            }
            File[] files = workDir.listFiles();
            int    cnt   = 0;
            for (File f : files) {
                String originalFileLocation = f.toString();
                if ( !f.getName().endsWith(RINEX_SUFFIX)
                        || (f.length() == 0)) {
                    continue;
                }
                //Get the name first
                String name = f.getName();

                //Copy the tmp file to storage. Use the storage name 
                f = getStorageManager().copyToStorage(request, f,
                                                      getStorageManager().getStorageFileName(f.getName()));

                TypeHandler typeHandler =
                    getRepository().getTypeHandler("project_gps_rinex");
                Entry newEntry = getEntryManager().addFileEntry(request, f,
                                     parent, name, request.getUser(),
                                     typeHandler, null);

                if (cnt == 0) {
                    sb.append(msgHeader("Published Entries"));
                }
                cnt++;
                sb.append(
                    HtmlUtil.href(
                        HtmlUtil.url(
                            getRepository().URL_ENTRY_SHOW.toString(),
                            new String[] { ARG_ENTRYID,
                                           newEntry.getId() }), newEntry
                                           .getName()));

                sb.append("<br>");
                getRepository().addAuthToken(request);
                Entry fromEntry = fileToEntryMap.get(originalFileLocation);
                getRepository().getAssociationManager().addAssociation(
                    request, newEntry, fromEntry, "generated rinex",
                    "rinex generated from");
            }

            sb.append(HtmlUtil.p());
            sb.append(request.form(getRepository().URL_ENTRY_SHOW));
            sb.append(HtmlUtil.hidden(ARG_OUTPUT, OUTPUT_GPS_TEQC.getId()));
            sb.append(HtmlUtil.hidden(ARG_ENTRYID, mainEntry.getId()));
            sb.append(HtmlUtil.hidden(ARG_RINEX_DOWNLOAD, uniqueId));
            sb.append(HtmlUtil.submit(msg("Download Results")));
            sb.append(HtmlUtil.formClose());
        }
        return new Result("", sb);
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result outputOpus(Request request, Entry mainEntry,
                              List<Entry> entries)
        throws Exception {


        StringBuffer sb = new StringBuffer();
        if ( !request.get(ARG_OPUS_PROCESS, false)) {
            return outputOpusForm(request, mainEntry, entries, sb);
        }

        if(!request.defined(ARG_OPUS_EMAIL)) {
            sb.append(getRepository().showDialogWarning("No email specified"));
            return outputOpusForm(request, mainEntry, entries, sb);
        }

        List<String> entryIds = request.get(ARG_RINEX_FILE,
                                            new ArrayList<String>());


        sb.append(msgHeader("Results"));
        sb.append("<ul>");
        for (String entryId : entryIds) {
            Entry entry = getEntryManager().getEntry(request, entryId);
            if (entry == null) {
                throw new IllegalArgumentException("No entry:" + entryId);
            }
            sb.append("<li>");
            sb.append(entry.getName());

            if ( !isRinex(entry)) {
                sb.append(" ... skipping - not rinex");
                continue;
            }

            File f = entry.getFile();
            if ( !f.exists()) {
                sb.append(" ... skipping - file does not exist");
            }

            List<HttpFormEntry> postEntries = new ArrayList<HttpFormEntry>();
            postEntries.add(HttpFormEntry.hidden(ARG_OPUS_EMAIL,request.getString(ARG_OPUS_EMAIL,"").trim()));
            String antenna = request.getString(ARG_OPUS_ANTENNA,"");
            //            antenna = antenna.replaceAll(" ","+");
            postEntries.add(HttpFormEntry.hidden(ARG_OPUS_ANTENNA,antenna));
            postEntries.add(HttpFormEntry.hidden(ARG_OPUS_HEIGHT,request.getString(ARG_OPUS_HEIGHT,"").trim()));

            //            for data > 15 min. < 2 hrs. for data > 2 hrs. < 48 hrs.
            if(request.get(ARG_OPUS_RAPID, false)) {
                System.err.println("RAPID STATIC");
                postEntries.add(HttpFormEntry.hidden("Rapid-Static", "Upload to Rapid-Static"));
            } else {
                postEntries.add(HttpFormEntry.hidden("Static", "Static"));
            }
            postEntries.add(HttpFormEntry.hidden("theHost1","www.ngs.noaa.gov"));
            postEntries.add(HttpFormEntry.hidden("", ""));
            postEntries.add(HttpFormEntry.hidden("selectList1", ""));
            postEntries.add(HttpFormEntry.hidden("extend_code", "0"));
            postEntries.add(HttpFormEntry.hidden("xml_code", "0"));
            postEntries.add(HttpFormEntry.hidden("set_profile", "0"));
            postEntries.add(HttpFormEntry.hidden("delete_profile", "0"));
            postEntries.add(HttpFormEntry.hidden("share", "2"));
            postEntries.add(HttpFormEntry.hidden("submit_database", "2"));
            postEntries.add(HttpFormEntry.hidden("opusOption", "0"));
            postEntries.add(HttpFormEntry.hidden("frameValue", "2011"));
            postEntries.add(
                            new HttpFormEntry(
                                              "uploadfile", entry.getId()+".rinex",
                                              IOUtil.readBytes(
                                                               getStorageManager().getFileInputStream(f))));


            String url =
                "http://www.ngs.noaa.gov/OPUS-cgi/OPUS/prod/upload.prl";

            String[] result={"",""};
            String errorMsg = null;
            try {
                result = HttpFormEntry.doPost(postEntries, url);
                errorMsg = result[0];
            } catch(Exception exc) {
                errorMsg = LogUtil.getInnerException(exc).getMessage();
                int idx = errorMsg.indexOf("errorMessage=");
                if(idx>=0) {
                    errorMsg = errorMsg.substring(idx+"errorMessage=".length());
                }
            }

            String html = result[1];
            if (errorMsg != null) {
                //This is a hack since the httpformentry gets a redirect and tries to post again to the redirect url
                if(errorMsg.indexOf("uploadResults.jsp")>=0) {
                    System.err.println ("ERROR:" + errorMsg);
                    errorMsg = null;
                    html = "Upload successful";
                }
            }

            if (errorMsg != null) {
                sb.append(" ... Error:");
                sb.append(
                          "<pre style=\"  border: solid 1px #000; max-height: 150px;overflow-y: auto; \">");
                errorMsg = StringUtil.stripTags(errorMsg);
                sb.append(errorMsg);
                sb.append("</pre>");
                sb.append("<p>");
                sb.append(msgHeader("Upload Form"));
                return outputOpusForm(request, mainEntry, entries, sb);
            } else {
                if (html.indexOf("Upload successful") >= 0) {
                    sb.append(" ... Uploaded to OPUS");
                } else {
                    sb.append(" ... Error:");
                    System.out.println(result[1]);
                }
            }
        }

        sb.append("</ul>");
        return new Result("", sb);
    }


    private Result outputOpusForm(Request request, Entry mainEntry,
                                  List<Entry> entries,         StringBuffer sb)
        throws Exception {

        sb.append(HtmlUtil.p());
        sb.append(request.form(getRepository().URL_ENTRY_SHOW));
        sb.append(HtmlUtil.hidden(ARG_OUTPUT, OUTPUT_GPS_OPUS.getId()));
        sb.append(HtmlUtil.hidden(ARG_ENTRYID, mainEntry.getId()));
        sb.append(HtmlUtil.hidden(ARG_OPUS_PROCESS, "true"));

        sb.append(HtmlUtil.submit("Submit to OPUS"));

        sb.append(HtmlUtil.formTable());
        sb.append(HtmlUtil.formEntry(msg("Email"),
                                     HtmlUtil.input(ARG_OPUS_EMAIL,
                                                    request.getString(ARG_OPUS_EMAIL, request.getUser().getEmail()))));

        sb.append(HtmlUtil.formEntry(msg("Antenna Height"),
                                     HtmlUtil.input(ARG_OPUS_HEIGHT,
                                                    request.getString(ARG_OPUS_HEIGHT, "0.1")) + " "
                                     + msg("Meters above mark")));

        sb.append(HtmlUtil.formEntry(msg("Antenna"),
                                     HtmlUtil.select(ARG_OPUS_ANTENNA,
                                                     Antenna.getAntennas(), request.getString(ARG_OPUS_ANTENNA,""),
                                                     200)));

        sb.append(HtmlUtil.formEntry(msg("Rapid"),
                                     HtmlUtil.checkbox(ARG_OPUS_RAPID,
                                                       "true", request.get(ARG_OPUS_RAPID,false)) +" " +"for data &gt; 15 min. &lt; 2 hrs."));


        sb.append(HtmlUtil.formTableClose());

        if (entries.size() == 1) {
            sb.append(HtmlUtil.hidden(ARG_RINEX_FILE,
                                      entries.get(0).getId()));
        } else {
            sb.append(msgHeader("Select entries"));
            List<String> selectedIds = request.get(ARG_RINEX_FILE, new ArrayList<String>());
            for (Entry entry : entries) {
                if ( !isRinex(entry)) {
                    continue;
                }
                boolean selected = true;
                if(selectedIds.size()>0 && !selectedIds.contains(entry.getId())) selected = false;
                sb.append(HtmlUtil.checkbox(ARG_RINEX_FILE,
                                            entry.getId(), selected));
                sb.append(" ");
                sb.append(entry.getName());
                sb.append(HtmlUtil.br());
            }
        }
        sb.append(HtmlUtil.p());
        sb.append(HtmlUtil.submit("Submit to OPUS"));
        sb.append(HtmlUtil.formClose());
        return new Result("", sb);
    }





}
