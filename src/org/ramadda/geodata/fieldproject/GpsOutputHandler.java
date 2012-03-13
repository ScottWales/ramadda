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



import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HttpFormField;

import org.ramadda.util.TempDir;

import org.w3c.dom.*;

import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;


import java.io.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
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
    private static final String TEQC_FLAG_QC = "+qcq";

    /** _more_          */
    private static final String TEQC_UNKNOWN = "-Unknown-";

    /** _more_ */
    private static final String TEQC_FLAG_META = "+meta";


    /** _more_ */
    public static final String OPUS_TITLE = "Add OPUS";

    /** _more_ */
    public static final String URL_ADDOPUS = "/fieldproject/addopus";

    /** _more_ */
    public static final String ARG_OPUS = "opus";

    /** _more_ */
    public static final String ARG_CONTROLPOINTS_COMMENT =
        "controlpoints.comment";

    /** _more_ */
    public static final int IDX_FORMAT = 0;

    /** _more_ */
    public static final int IDX_SITE_CODE = 1;

    /** _more_ */
    public static final int IDX_ANTENNA_TYPE = 2;

    /** _more_ */
    public static final int IDX_ANTENNA_HEIGHT = 3;


    /** _more_ */
    public static final String ASSOCIATION_TYPE_GENERATED_FROM =
        "generated_from";


    /** _more_ */
    private static final String RINEX_SUFFIX = ".rinex";


    /** _more_ */
    private static final String ARG_OPUS_EMAIL = "email_address";

    /** _more_ */
    private static final String ARG_OPUS_ANTENNA = "ant_type";

    /** _more_ */
    private static final String ARG_OPUS_RAPID = "opus.rapid";

    /** _more_ */
    private static final String ARG_OPUS_HEIGHT = "height";


    /** _more_ */
    private static final String ARG_RINEX_PROCESS = "rinex.process";

    /** _more_ */
    private static final String ARG_PROCESS = "gps.process";

    /** _more_ */
    private static final String ARG_OPUS_PROCESS = "opus.process";

    /** _more_ */
    private static final String ARG_RINEX_FILE = "rinex.file";

    /** _more_ */
    private static final String ARG_GPS_FILE = "gps.file";



    /** _more_ */
    private static final String ARG_RINEX_DOWNLOAD = "rinex.download";

    /** file path to the teqc executable */
    private String teqcPath;

    /** _more_ */
    private TempDir productDir;


    /** The output type */
    public static final OutputType OUTPUT_GPS_TORINEX =
        new OutputType("Convert to RINEX", "fieldproject.gps.torinex",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       "/fieldproject/gps.png", "Field Project");

    /** _more_ */
    public static final OutputType OUTPUT_GPS_METADATA =
        new OutputType("Show GPS Metadata", "fieldproject.gps.metadata",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       "/fieldproject/gps.png", "Field Project");

    /** _more_ */
    public static final OutputType OUTPUT_GPS_QC =
        new OutputType("Show GPS QC", "fieldproject.gps.qc",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       "/fieldproject/gps.png", "Field Project");

    /** _more_ */
    public static final OutputType OUTPUT_GPS_OPUS =
        new OutputType("Submit to OPUS", "fieldproject.gps.opus",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       "/fieldproject/opus.png", "Field Project");

    /** _more_ */
    public static final OutputType OUTPUT_GPS_CONTROLPOINTS =
        new OutputType("Make Control Points",
                       "fieldproject.gps.controlpoints",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       "/icons/csv.png", "Field Project");

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
        addType(OUTPUT_GPS_TORINEX);

        addType(OUTPUT_GPS_METADATA);
        addType(OUTPUT_GPS_QC);
        addType(OUTPUT_GPS_OPUS);
        addType(OUTPUT_GPS_CONTROLPOINTS);
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
        return entry.getTypeHandler().isType(GpsTypeHandler.TYPE_RAW);
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    private boolean isOpus(Entry entry) {
        return entry.getType().equals(OpusTypeHandler.TYPE_OPUS);
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    private boolean isRinex(Entry entry) {
        return entry.getType().equals(GpsTypeHandler.TYPE_RINEX);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean haveTeqc() {
        return teqcPath != null;
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
                                       OUTPUT_GPS_TORINEX));
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
            for (Entry child : state.getAllEntries()) {
                if (isOpus(child)) {
                    links.add(makeLink(request, state.group,
                                       OUTPUT_GPS_CONTROLPOINTS));
                    break;
                }
            }


        } else if (state.entry != null) {
            if (isRawGps(state.entry)) {
                links.add(makeLink(request, state.entry,
                                   OUTPUT_GPS_METADATA));
                links.add(makeLink(request, state.entry, OUTPUT_GPS_TORINEX));

            }
            if (isRinex(state.entry)) {
                links.add(makeLink(request, state.entry,
                                   OUTPUT_GPS_METADATA));
                links.add(makeLink(request, state.entry, OUTPUT_GPS_QC));
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
        if (outputType.equals(OUTPUT_GPS_TORINEX)) {
            return outputRinex(request, group, entries);
        }
        if (outputType.equals(OUTPUT_GPS_CONTROLPOINTS)) {
            return outputControlpoint(request, group, entries);
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
        if (outputType.equals(OUTPUT_GPS_TORINEX)) {
            return outputRinex(request, entry, entries);
        }
        if (outputType.equals(OUTPUT_GPS_METADATA)) {
            return outputMetadata(request, entry);
        }
        if (outputType.equals(OUTPUT_GPS_QC)) {
            return outputQC(request, entry);
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
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result outputMetadata(Request request, Entry entry)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(HtmlUtil.formTable());
        int cnt = 0;
        for (String line :
                StringUtil.split(extractGpsMetadata(entry.getFile(),
                    TEQC_FLAG_META), "\n", true, true)) {
            cnt++;
            //skip the filename
            if (cnt == 1) {
                continue;
            }
            List<String> toks = StringUtil.splitUpTo(line, ":", 2);
            if (toks.size() < 2) {
                continue;
            }
            sb.append(
                HtmlUtil.formEntry(
                    msgLabel(StringUtil.camelCase(toks.get(0))),
                    toks.get(1)));
            sb.append("</tr>");
        }
        sb.append(HtmlUtil.formTableClose());
        return new Result("GPS Metadata", sb);
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
    private Result outputQC(Request request, Entry entry) throws Exception {
        StringBuffer sb            = new StringBuffer();
        int          cnt           = 0;
        int          STATE_START   = 0;
        int          STATE_PLOT    = 1;
        int          STATE_PRELIST = 2;
        int          STATE_LIST    = 3;
        int          state         = STATE_START;

        for (String line :
                StringUtil.split(extractGpsMetadata(entry.getFile(),
                    TEQC_FLAG_QC), "\n", false, false)) {
            String trimmed = line.trim();
            if (trimmed.length() == 0) {
                continue;
            }
            if (state == STATE_START) {
                if (trimmed.startsWith("version:")) {
                    state = STATE_PLOT;
                    sb.append("<pre>");
                }
                continue;
            }
            if (state == STATE_PLOT) {
                if (trimmed.startsWith("*******")) {
                    state = STATE_PRELIST;
                    sb.append("</pre>");
                    sb.append(HtmlUtil.formTable());
                } else {
                    sb.append(line);
                    sb.append("\n");
                }
            }
            if (state == STATE_PRELIST) {
                if (trimmed.startsWith("*******")) {
                    state = STATE_LIST;
                }
                continue;
            }
            if (state == STATE_LIST) {
                List<String> toks = StringUtil.splitUpTo(line, ":", 2);
                if (toks.size() < 2) {
                    continue;
                }
                sb.append(
                    HtmlUtil.formEntry(
                        msgLabel(
                            toks.get(0).replaceAll("<", "&lt").replaceAll(
                                ">", "&gt;")), toks.get(1)));
            }
        }
        sb.append(HtmlUtil.formTableClose());
        return new Result("GPS Metadata", sb);
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
            sb.append(HtmlUtil.hidden(ARG_OUTPUT,
                                      OUTPUT_GPS_TORINEX.getId()));
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
            addPublishWidget(
                request, mainEntry, sb,
                msg("Optionally select a folder to publish the RINEX to"),
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
            Entry rawEntry = getEntryManager().getEntry(request, entryId);
            if (rawEntry == null) {
                throw new IllegalArgumentException("No entry:" + entryId);
            }

            if ( !isRawGps(rawEntry)) {
                sb.append("<li>");
                sb.append("Skipping:" + rawEntry.getName());
                sb.append(HtmlUtil.p());
                continue;
            }

            File f = rawEntry.getFile();
            if ( !f.exists()) {
                throw new IllegalStateException("File does not exist:" + f);
            }

            File rinexFile = new File(
                                 IOUtil.joinDir(
                                     workDir,
                                     IOUtil.stripExtension(
                                         getStorageManager().getFileTail(
                                             rawEntry)) + RINEX_SUFFIX));
            fileToEntryMap.put(rinexFile.toString(), rawEntry);

            ProcessBuilder pb = new ProcessBuilder(teqcPath, "+out",
                                    rinexFile.toString(), f.toString());
            pb.directory(workDir);
            Process process = pb.start();
            String errorMsg =
                new String(IOUtil.readBytes(process.getErrorStream()));
            String outMsg =
                new String(IOUtil.readBytes(process.getInputStream()));
            int result = process.waitFor();
            sb.append("<li>");
            sb.append(rawEntry.getName());
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
            if ((errorMsg.length() > 0) || (outMsg.length() > 0)) {
                sb.append(
                    "<pre style=\"  border: solid 1px #000; max-height: 150px;overflow-y: auto; \">");
                sb.append(errorMsg);
                sb.append(outMsg);
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
                Entry  rawEntry = fileToEntryMap.get(originalFileLocation);
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
                    getRepository().getTypeHandler(GpsTypeHandler.TYPE_RINEX);
                Object[] tmpValues = null;
                if (rawEntry.getValues() != null) {
                    tmpValues = (Object[]) rawEntry.getValues().clone();
                    tmpValues[IDX_FORMAT] = "RINEX";
                }
                final Object[]   values      = tmpValues;
                EntryInitializer initializer = new EntryInitializer() {
                    public void initEntry(Entry entry) {
                        entry.setValues(values);
                    }
                };

                Entry newEntry = getEntryManager().addFileEntry(request, f,
                                     parent, name, request.getUser(),
                                     typeHandler, initializer);

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
                getAssociationManager().addAssociation(request, newEntry,
                        rawEntry, "generated rinex",
                        ASSOCIATION_TYPE_GENERATED_FROM);
            }


            sb.append(HtmlUtil.p());
            sb.append(request.form(getRepository().URL_ENTRY_SHOW));
            sb.append(HtmlUtil.hidden(ARG_OUTPUT,
                                      OUTPUT_GPS_TORINEX.getId()));
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
    private Result outputControlpoint(Request request, Entry mainEntry,
                                      List<Entry> entries)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        if ( !request.get(ARG_PROCESS, false)) {
            sb.append(HtmlUtil.p());
            sb.append(request.form(getRepository().URL_ENTRY_SHOW));
            sb.append(HtmlUtil.hidden(ARG_OUTPUT,
                                      OUTPUT_GPS_CONTROLPOINTS.getId()));
            sb.append(HtmlUtil.hidden(ARG_ENTRYID, mainEntry.getId()));
            sb.append(HtmlUtil.hidden(ARG_PROCESS, "true"));
            sb.append(HtmlUtil.formTable());

            sb.append(
                HtmlUtil.formEntry(
                    msgLabel("Comment"),
                    HtmlUtil.input(
                        ARG_CONTROLPOINTS_COMMENT, "", HtmlUtil.SIZE_60)));


            StringBuffer entryTable = new StringBuffer();
            entryTable.append(
                "<table><tr><td align=center></td><td align=center><b>X</b></td><td align=center><b>Y</b></td><td align=center><b>Z</b></td></tr>");
            for (Entry entry : entries) {
                if ( !isOpus(entry)) {
                    continue;
                }
                entryTable.append("<tr><td>");
                entryTable.append(HtmlUtil.checkbox(ARG_GPS_FILE,
                        entry.getId(), true));
                entryTable.append(" ");
                entryTable.append(entry.getName());
                entryTable.append("</td><td align=right>");
                entryTable.append(entry.getValue(OpusTypeHandler.IDX_ITRF_X,
                        "NA"));
                entryTable.append("</td><td align=right>");
                entryTable.append(entry.getValue(OpusTypeHandler.IDX_ITRF_Y,
                        "NA"));
                entryTable.append("</td><td align=right>");
                entryTable.append(entry.getValue(OpusTypeHandler.IDX_ITRF_Z,
                        "NA"));
                entryTable.append("</td></tr>");

            }
            entryTable.append("</table>");
            sb.append(HtmlUtil.formEntryTop(msgLabel("Entries"),
                                            entryTable.toString()));
            addPublishWidget(
                request, mainEntry, sb,
                msg(
                "Optionally select a folder to publish the control point file to"), true);
            sb.append(HtmlUtil.formTableClose());

            sb.append(HtmlUtil.submit("Make Control Point File"));
            sb.append(HtmlUtil.formClose());
            return new Result("", sb);
        }

        List<String> entryIds = request.get(ARG_GPS_FILE,
                                            new ArrayList<String>());

        StringBuffer buff = new StringBuffer();
        if (request.defined(ARG_CONTROLPOINTS_COMMENT)) {
            buff.append("#");
            buff.append(request.getString(ARG_CONTROLPOINTS_COMMENT, ""));
            buff.append("\n");
        }
        boolean anyOK = false;
        sb.append(msgHeader("Results"));
        sb.append("<ul>");
        List<Entry> opusEntries = new ArrayList<Entry>();
        for (String entryId : entryIds) {
            Entry opusEntry = getEntryManager().getEntry(request, entryId);
            if (opusEntry == null) {
                throw new IllegalArgumentException("No entry:" + entryId);
            }

            if ( !isOpus(opusEntry)) {
                sb.append("<li>");
                sb.append("Skipping:" + opusEntry.getName());
                sb.append(HtmlUtil.p());
                continue;
            }

            opusEntries.add(opusEntry);
            anyOK = true;
            String siteCode =
                opusEntry.getValue(OpusTypeHandler.IDX_SITE_CODE, "");
            if (siteCode.length() == 0) {
                siteCode = opusEntry.getName();
            }
            buff.append(siteCode);
            buff.append(",");
            buff.append(opusEntry.getValue(OpusTypeHandler.IDX_ITRF_X, "NA"));
            buff.append(",");
            buff.append(opusEntry.getValue(OpusTypeHandler.IDX_ITRF_Y, "NA"));
            buff.append(",");
            buff.append(opusEntry.getValue(OpusTypeHandler.IDX_ITRF_Z, "NA"));
            buff.append("\n");
            //            buff.append(opusEntry.getValue(OpusTypeHandler.IDX_UTM_X,"NA"));
            //            buff.append(opusEntry.getValue(OpusTypeHandler.IDX_UTM_Y,"NA"));
            //            buff.append(opusEntry.getAltitude());
        }

        sb.append("</ul>");
        if ( !anyOK) {
            return new Result("", sb);
        }

        if ( !doingPublish(request)) {
            request.setReturnFilename("controlpoints.csv");
            return new Result("", buff, "text/csv");
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
            String fileName = request.getString(ARG_PUBLISH_NAME, "").trim();
            if (fileName.length() == 0) {
                fileName = "controlpoints.csv";
            }
            //Write the text out
            File f = getStorageManager().getTmpFile(request, fileName);
            FileOutputStream out = getStorageManager().getFileOutputStream(f);
            out.write(buff.toString().getBytes());
            out.flush();
            out.close();
            f = getStorageManager().copyToStorage(request, f, f.getName());

            TypeHandler typeHandler = getRepository().getTypeHandler(
                                          GpsTypeHandler.TYPE_CONTROLPOINTS);

            Entry newEntry = getEntryManager().addFileEntry(request, f,
                                 parent, fileName, request.getUser(),
                                 typeHandler, null);
            sb.append("Control points file created:");
            sb.append(
                HtmlUtil.href(
                    HtmlUtil.url(
                        getRepository().URL_ENTRY_SHOW.toString(),
                        new String[] { ARG_ENTRYID,
                                       newEntry.getId() }), newEntry
                                           .getName()));

            getRepository().addAuthToken(request);
            for (Entry opusEntry : opusEntries) {
                getAssociationManager().addAssociation(request, newEntry,
                        opusEntry, "", ASSOCIATION_TYPE_GENERATED_FROM);
            }
        }
        return new Result("", sb);
    }





    /**
     * _more_
     *
     * @param rinexFile _more_
     * @param flag _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private String extractGpsMetadata(File rinexFile, String flag)
            throws Exception {
        ProcessBuilder pb = new ProcessBuilder(teqcPath, flag,
                                rinexFile.toString());
        Process process = pb.start();
        String errorMsg =
            new String(IOUtil.readBytes(process.getErrorStream()));
        String outMsg =
            new String(IOUtil.readBytes(process.getInputStream()));
        int result = process.waitFor();
        return outMsg;
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param gpsTypeHandler _more_
     *
     * @throws Exception _more_
     */
    public void initializeGpsEntry(Entry entry, GpsTypeHandler gpsTypeHandler)
            throws Exception {
        //Check if we have teqc installed
        if ( !haveTeqc()) {
            return;
        }
        File gpsFile = entry.getFile();
        if ( !gpsFile.exists()) {
            return;
        }
        String   gpsMetadata = extractGpsMetadata(gpsFile, TEQC_FLAG_META);
        Object[] values      = gpsTypeHandler.getValues(entry);
        //   2011-05-04 18:23:00.000
        //format,site_code,antenna_type,antenna_height

        //Initialize the values
        values[IDX_FORMAT]         = "";
        values[IDX_SITE_CODE]      = "";
        values[IDX_ANTENNA_TYPE]   = "";
        values[IDX_ANTENNA_HEIGHT] = new Double(0);

        for (String line : StringUtil.split(gpsMetadata, "\n", true, true)) {
            List<String> toks = StringUtil.splitUpTo(line, ":", 2);
            if (toks.size() < 2) {
                continue;
            }
            String key   = toks.get(0);
            String value = toks.get(1);
            if (value.trim().equals(TEQC_UNKNOWN)) {
                value = "";
            }
            //            System.err.println("KEY:" + key+":");
            if (key.equals("file format")) {
                values[IDX_FORMAT] = value;
            } else if (key.startsWith("start date")) {
                Date dttm = parseDate(value);
                if (dttm != null) {
                    entry.setStartDate(dttm.getTime());
                }
            } else if (key.startsWith("final date")) {
                Date dttm = parseDate(value);
                if (dttm != null) {
                    entry.setEndDate(dttm.getTime());
                }
            } else if (key.startsWith("4-char")) {
                values[IDX_SITE_CODE] = value;
            } else if (key.equals("antenna type")) {
                values[IDX_ANTENNA_TYPE] = value;
            } else if (key.startsWith("antenna height")) {
                values[IDX_ANTENNA_HEIGHT] = new Double(value);
            } else if (key.startsWith("antenna latitude")) {
                entry.setLatitude(Double.parseDouble(value));
            } else if (key.startsWith("antenna longitude")) {
                entry.setLongitude(Double.parseDouble(value));
            } else if (key.startsWith("antenna elevation")) {
                entry.setAltitude(Double.parseDouble(value));
            } else if (key.equals("")) {}
            else {
                //                System.err.println("key?:" + key + "=" + value);
            }
        }
    }

    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    private Date parseDate(String date) {
        //        2011-05-04 18:23:00.000
        try {
            SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            sdf.setTimeZone(getRepository().TIMEZONE_UTC);
            Date dttm = sdf.parse(date);
            return dttm;
        } catch (Exception exc) {
            System.err.println("Error:" + exc);
            return null;
        }
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

        if ( !request.defined(ARG_OPUS_EMAIL)) {
            sb.append(
                getRepository().showDialogWarning("No email specified"));
            return outputOpusForm(request, mainEntry, entries, sb);
        }

        sb.append(msgHeader("Results"));
        sb.append("<ul>");
        StringBuffer extra = new StringBuffer();
        int          cnt   = 0;
        while (true) {
            String argSuffix = "_" + cnt;
            cnt++;
            if ( !request.exists(ARG_OPUS_ANTENNA + argSuffix)) {
                break;
            }
            String entryId = request.getString(ARG_RINEX_FILE + argSuffix,
                                 "");
            if (entryId.equals("")) {
                continue;
            }
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

            List<HttpFormField> postEntries = new ArrayList<HttpFormField>();
            postEntries.add(HttpFormField.hidden(ARG_OPUS_EMAIL,
                    request.getString(ARG_OPUS_EMAIL, "").trim()));
            String antenna = request.getString(ARG_OPUS_ANTENNA, "");
            if (antenna.equals(Antenna.NONE) || antenna.equals("")) {
                antenna = request.getString(ARG_OPUS_ANTENNA + argSuffix,
                                            Antenna.NONE);
            }
            postEntries.add(HttpFormField.hidden(ARG_OPUS_ANTENNA, antenna));
            String height = null;
            if (request.defined(ARG_OPUS_HEIGHT)) {
                height = request.getString(ARG_OPUS_HEIGHT, "");
            }
            if (height == null) {
                height = request.getString(ARG_OPUS_HEIGHT + argSuffix,
                                           "0.0");
            }
            postEntries.add(HttpFormField.hidden(ARG_OPUS_HEIGHT, height));

            //            for data > 15 min. < 2 hrs. for data > 2 hrs. < 48 hrs.
            if (request.get(ARG_OPUS_RAPID, false)) {
                System.err.println("RAPID STATIC");
                postEntries.add(HttpFormField.hidden("Rapid-Static",
                        "Upload to Rapid-Static"));
            } else {
                postEntries.add(HttpFormField.hidden("Static", "Static"));
            }
            postEntries.add(HttpFormField.hidden("theHost1",
                    "www.ngs.noaa.gov"));
            postEntries.add(HttpFormField.hidden("", ""));
            postEntries.add(HttpFormField.hidden("selectList1", ""));
            postEntries.add(HttpFormField.hidden("extend_code", "0"));
            postEntries.add(HttpFormField.hidden("xml_code", "0"));
            postEntries.add(HttpFormField.hidden("set_profile", "0"));
            postEntries.add(HttpFormField.hidden("delete_profile", "0"));
            postEntries.add(HttpFormField.hidden("share", "2"));
            postEntries.add(HttpFormField.hidden("submit_database", "2"));
            postEntries.add(HttpFormField.hidden("opusOption", "0"));
            postEntries.add(HttpFormField.hidden("frameValue", "2011"));
            //Use the entry id so when we get the opus back we can look up the original entry
            postEntries.add(
                new HttpFormField(
                    "uploadfile", entry.getId() + ".rinex",
                    IOUtil.readBytes(
                        getStorageManager().getFileInputStream(f))));


            String url =
                "http://www.ngs.noaa.gov/OPUS-cgi/OPUS/prod/upload.prl";

            String[] result   = { "", "" };
            String   errorMsg = null;
            try {
                result   = HttpFormField.doPost(postEntries, url, false);
                errorMsg = result[0];
            } catch (Exception exc) {
                errorMsg = LogUtil.getInnerException(exc).getMessage();
            }

            if (errorMsg != null) {
                int idx = errorMsg.indexOf("errorMessage=");
                if (idx >= 0) {
                    errorMsg = errorMsg.substring(idx
                            + "errorMessage=".length());
                }
            }
            String html = result[1];
            if (errorMsg != null) {
                //This is a hack since the httpformentry gets a redirect and tries to post again to the redirect url
                if (errorMsg.indexOf("uploadResults.jsp") >= 0) {
                    //                    System.err.println("ERROR:" + errorMsg);
                    errorMsg = null;
                    html     = "Upload successful";
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
                sb.append("</ul>");
                return outputOpusForm(request, mainEntry, entries, sb);
            } else {
                if (html.indexOf("Upload successful") >= 0) {
                    sb.append(" ... Uploaded to OPUS. Height:" + height);
                    if (antenna.equals(Antenna.NONE)) {
                        sb.append(" No antenna selected");
                    } else {
                        sb.append(" Antenna:" + antenna);
                    }
                } else {
                    sb.append(" ... Error:");
                    sb.append(result[1]);
                    //                    System.out.println(result[1]);
                }
            }
        }

        sb.append("</ul>");
        sb.append("When you get the results in your email click ");
        sb.append(HtmlUtil.href(getRepository().getUrlBase()
                                + "/fieldproject/addopus", "here"));
        sb.append(" to upload the OPUS solutions");
        return new Result("", sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param entries _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result outputOpusForm(Request request, Entry mainEntry,
                                  List<Entry> entries, StringBuffer sb)
            throws Exception {

        sb.append(HtmlUtil.p());
        sb.append(request.form(getRepository().URL_ENTRY_SHOW));
        sb.append(HtmlUtil.hidden(ARG_OUTPUT, OUTPUT_GPS_OPUS.getId()));
        sb.append(HtmlUtil.hidden(ARG_ENTRYID, mainEntry.getId()));
        sb.append(HtmlUtil.hidden(ARG_OPUS_PROCESS, "true"));

        sb.append(HtmlUtil.submit("Submit to OPUS"));

        sb.append(HtmlUtil.formTable());
        sb.append(HtmlUtil.formEntry(msgLabel("Email"),
                                     HtmlUtil.input(ARG_OPUS_EMAIL,
                                         request.getString(ARG_OPUS_EMAIL,
                                             request.getUser().getEmail()))));

        sb.append(HtmlUtil.formTableClose());
        //request.getString(ARG_OPUS_ANTENNA, entry.getValue(IDX_ANTENNA_TYPE,"")
        String selectedAntenna = "";
        for (Entry entry : entries) {
            if ( !isRinex(entry)) {
                continue;
            }
            selectedAntenna = (String) entry.getValue(IDX_ANTENNA_TYPE, "");
            break;
        }

        /*        sb.append(HtmlUtil.formEntry(msgLabel("Rapid"),
                  HtmlUtil.checkbox(ARG_OPUS_RAPID,
                  "true", request.get(ARG_OPUS_RAPID,false)) +" " +"for data &gt; 15 min. &lt; 2 hrs."));
        */

        StringBuffer entriesSB =
            new StringBuffer("<table cellpadding=3 cellspacing=3>");
        entriesSB.append(
            "<tr><td><b>Entry</b></td><td><b>Duration</b></td><td><b>Antenna Height</b></td></tr>");
        List<String> selectedIds = request.get(ARG_RINEX_FILE,
                                       new ArrayList<String>());
        int cnt = 0;
        for (Entry entry : entries) {
            if ( !isRinex(entry)) {
                continue;
            }
            int minutes = (int) ((entry.getEndDate() - entry.getStartDate())
                                 / 1000l / 60l);
            boolean selected = true;
            if ((selectedIds.size() > 0)
                    && !selectedIds.contains(entry.getId())) {
                selected = false;
            }
            StringBuffer comment = new StringBuffer("");
            if ((minutes > 0) && (minutes < 120)) {
                selected = false;
                comment.append(HtmlUtil.italics("&lt; 2 hours. "));
            }
            if (getEntryManager()
                    .getEntriesWithType(getAssociationManager()
                        .getTailEntriesWithAssociationType(request, entry,
                            GpsOutputHandler
                                .ASSOCIATION_TYPE_GENERATED_FROM), OpusTypeHandler
                                    .TYPE_OPUS).size() > 0) {
                selected = false;
                comment.append(HtmlUtil.italics("Already has an OPUS entry"));
            }

            String argSuffix = "_" + cnt;
            cnt++;

            entriesSB.append("<tr><td>");
            entriesSB.append(HtmlUtil.checkbox(ARG_RINEX_FILE + argSuffix,
                    entry.getId(), selected));
            entriesSB.append(" ");
            entriesSB.append(entry.getName());
            entriesSB.append(" ");
            entriesSB.append(comment);
            entriesSB.append("</td><td align=right>");
            if (minutes != 0) {
                int hours = minutes / 60;
                minutes = minutes % 60;
                if (hours > 0) {
                    entriesSB.append(hours + ":");
                }
                if (minutes < 10) {
                    entriesSB.append("0" + minutes);
                } else {
                    entriesSB.append(minutes);
                }
            } else {
                entriesSB.append("NA");
            }
            entriesSB.append("</td>");
            entriesSB.append("<td align=right>");
            selectedAntenna = (String) entry.getValue(IDX_ANTENNA_TYPE, "");
            entriesSB.append(HtmlUtil.select(ARG_OPUS_ANTENNA + argSuffix,
                                             Antenna.getAntennas(),
                                             selectedAntenna));
            entriesSB.append("</td>");
            entriesSB.append("<td align=right>");
            entriesSB.append(
                HtmlUtil.input(
                    ARG_OPUS_HEIGHT + argSuffix,
                    request.getString(
                        ARG_OPUS_HEIGHT + argSuffix,
                        entry.getValue(
                            IDX_ANTENNA_HEIGHT, "")), HtmlUtil.SIZE_5));
            entriesSB.append("</td>");

            entriesSB.append("</tr>");
        }
        entriesSB.append("</table>");
        sb.append(msgHeader("Select RINEX Files"));
        sb.append(entriesSB.toString());


        sb.append(msgHeader("Overrides"));
        sb.append(HtmlUtil.formTable());
        sb.append(
            HtmlUtil.formEntry(
                "", "If defined use these values for antenna or height"));
        sb.append(HtmlUtil.formEntry(msgLabel("Antenna"),
                                     HtmlUtil.select(ARG_OPUS_ANTENNA,
                                         Antenna.getAntennas(), "")));
        sb.append(HtmlUtil.formEntry(msgLabel("Antenna Height"),
                                     HtmlUtil.input(ARG_OPUS_HEIGHT,
                                         request.getString(ARG_OPUS_HEIGHT,
                                             ""), HtmlUtil.SIZE_5)));

        sb.append(HtmlUtil.formTableClose());
        sb.append(HtmlUtil.p());
        sb.append(HtmlUtil.submit("Submit to OPUS"));
        sb.append(HtmlUtil.formClose());
        return new Result("", sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processAddOpus(Request request) throws Exception {

        StringBuffer sb = new StringBuffer();
        if (request.isAnonymous()) {
            sb.append(
                getRepository().showDialogError(
                    "You need to be logged in to add OPUS"));
            return new Result(OPUS_TITLE, sb);
        }
        if (request.exists(ARG_OPUS)) {
            //Look for:
            //            FILE: 7655b430-7c46-4324-924f-57ec6c2075b5.rinex OP1330950241570
            String opus = request.getString(ARG_OPUS, "");
            String rinexEntryId = StringUtil.findPattern(opus,
                                      "FILE: *([^\\.]+).rinex");
            if (rinexEntryId == null) {
                sb.append(
                    getRepository().showDialogError(
                        "Could not find FILE name in the given OPUS"));
                return processOpusForm(request, sb);
            }
            Entry rinexEntry = getEntryManager().getEntry(request,
                                   rinexEntryId);
            if (rinexEntry == null) {
                sb.append(
                    getRepository().showDialogError(
                        "Could not find original RINEX entry"));
                return processOpusForm(request, sb);
            }
            Entry parentEntry = rinexEntry.getParentEntry();

            //Look for the OPUS sibling folder
            for (Entry child :
                    getEntryManager().getChildrenGroups(request,
                        parentEntry.getParentEntry())) {
                if (child.getName().toLowerCase().trim().equals("opus")) {
                    parentEntry = child;
                    break;
                }
            }

            if ( !getAccessManager().canDoAction(request, parentEntry,
                    Permission.ACTION_NEW)) {
                sb.append(
                    getRepository().showDialogError(
                        "You do not have permission to add to:"
                        + parentEntry.getName()));
                return new Result(OPUS_TITLE, sb);
            }

            String opusFileName = IOUtil.stripExtension(rinexEntry.getName())
                                  + ".opus";
            //Write the text out
            File f = getStorageManager().getTmpFile(request, opusFileName);
            FileOutputStream out = getStorageManager().getFileOutputStream(f);
            out.write(opus.getBytes());
            out.flush();
            out.close();
            f = getStorageManager().copyToStorage(request, f, f.getName());

            TypeHandler typeHandler =
                getRepository().getTypeHandler("project_gps_opus");

            final Object     siteCode = rinexEntry.getValue(IDX_SITE_CODE,
                                            "");
            EntryInitializer initializer = new EntryInitializer() {
                public void initEntry(Entry entry) {
                    entry.getTypeHandler().getValues(
                        entry)[OpusTypeHandler.IDX_SITE_CODE] = siteCode;
                }
            };
            Entry newEntry = getEntryManager().addFileEntry(request, f,
                                 parentEntry, opusFileName,
                                 request.getUser(), typeHandler, initializer);

            //If we figured out location from the opus file then set the rinex entry location
            if (newEntry.hasLocationDefined()) {
                if (getAccessManager().canDoAction(request, rinexEntry,
                        Permission.ACTION_EDIT)) {
                    rinexEntry.setLocation(newEntry.getLatitude(),
                                           newEntry.getLongitude(),
                                           newEntry.getAltitude());
                    getEntryManager().storeEntry(rinexEntry);
                }

                for (Entry rawEntry :
                        getEntryManager()
                            .getEntriesWithType(getAssociationManager()
                                .getTailEntriesWithAssociationType(request,
                                    rinexEntry,
                                    GpsOutputHandler
                                        .ASSOCIATION_TYPE_GENERATED_FROM), GpsTypeHandler
                                            .TYPE_RAW)) {
                    if (getAccessManager().canDoAction(request, rawEntry,
                            Permission.ACTION_EDIT)) {
                        rawEntry.setLocation(newEntry.getLatitude(),
                                             newEntry.getLongitude(),
                                             newEntry.getAltitude());
                        getEntryManager().storeEntry(rawEntry);
                    }
                }
            }

            sb.append(HtmlUtil.p());
            sb.append("OPUS entry created: ");
            sb.append(
                HtmlUtil.href(
                    HtmlUtil.url(
                        getRepository().URL_ENTRY_SHOW.toString(),
                        new String[] { ARG_ENTRYID,
                                       newEntry.getId() }), newEntry
                                           .getName()));

            getRepository().addAuthToken(request);
            getAssociationManager().addAssociation(request, newEntry,
                    rinexEntry, "generated rinex",
                    GpsOutputHandler.ASSOCIATION_TYPE_GENERATED_FROM);
            return new Result(OPUS_TITLE, sb);
        }

        return processOpusForm(request, sb);

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processOpusForm(Request request, StringBuffer sb)
            throws Exception {
        String base    = getRepository().getUrlBase();
        String formUrl = base + URL_ADDOPUS;
        sb.append(HtmlUtil.p());
        sb.append("Enter the OPUS solution text below");
        sb.append(HtmlUtil.formPost(formUrl));
        sb.append(HtmlUtil.submit("Add OPUS Solution"));
        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.textArea(ARG_OPUS, "", 20, 70));
        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.submit("Add OPUS Solution"));
        sb.append(HtmlUtil.formClose());
        return new Result(OPUS_TITLE, sb);
    }




}
