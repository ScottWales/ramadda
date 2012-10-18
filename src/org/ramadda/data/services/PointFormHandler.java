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


import org.ramadda.data.point.*;
import org.ramadda.data.point.*;

import org.ramadda.util.ColorTable;

import org.ramadda.data.point.PointFile;


import org.ramadda.data.record.*;



import org.ramadda.data.record.*;
import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.job.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.output.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.grid.*;

import ucar.unidata.geoloc.Bearing;
import ucar.unidata.geoloc.LatLonPointImpl;



import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.GuiUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

import java.io.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 *
 * @author         Jeff McWhirter
 */
public class PointFormHandler extends RecordFormHandler {

    /** _more_ */
    private static IdwGrid dummyField1 = null;

    /** _more_ */
    private static ObjectGrid dummyField2 = null;

    /** _more_ */
    private static GridUtils dummyField3 = null;

    /** _more_ */
    private static Gridder dummyField4 = null;

    /** _more_ */
    private static GridVisitor dummyField5 = null;

    private RecordFileFactory dummyField6 = null;

    private PointTypeHandler dummyField7 = null;
    private RecordCollectionTypeHandler dummyField8  = null;
    private RecordApiHandler dummyField9 = null;

    /** _more_ */
    public static final String LABEL_ALTITUDE = "Altitude";



    /**
     * ctor
     *
     * @param recordOutputHandler _more_
     */
    public PointFormHandler(PointOutputHandler recordOutputHandler) {
        super(recordOutputHandler);
    }

    public PointOutputHandler getPointOutputHandler() {
        return (PointOutputHandler) getOutputHandler();
    }

    /**
     * Adds the grid oriented output formats
     *
     * @param outputs List of html selectors (which hold id, label and icon)
     * @param forCollection Are the grid formats for a lidar collection
     */
    public void getGridFormats(List<HtmlUtils.Selector> outputs,
                               boolean forCollection) {
        outputs.add(getSelect(getPointOutputHandler().OUTPUT_IMAGE));
        outputs.add(getSelect(getPointOutputHandler().OUTPUT_HILLSHADE));
        outputs.add(getSelect(getPointOutputHandler().OUTPUT_KMZ));
        outputs.add(getSelect(getPointOutputHandler().OUTPUT_ASC));
        //        outputs.add(getSelect(getPointOutputHandler().OUTPUT_NC));
    }



    /**
     * make the map lines for the given ldiar entry
     *
     * @param request the request
     * @param recordEntry The entry
     * @param map the map to add the lines to
     * @param lineCnt how many
     *
     * @throws Exception on badness
     */
    public void makeMapLines(Request request, RecordEntry recordEntry,
                             MapInfo map, int lineCnt)
            throws Exception {
        map.addLines("", getMapPolyline(request, recordEntry));
    }


    /**
     * add the lines to the map
     *
     * @param request the request
     * @param entry the entry
     * @param map the map
     */
    public void addToMap(Request request, Entry entry, MapInfo map) {
        try {
            RecordEntry recordEntry = new RecordEntry(getOutputHandler(),
                                          request, entry);
            List<double[]> polyLine = getMapPolyline(request, recordEntry);
            map.addLines("", polyLine);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * create the polyline for the given entry. This will cache it in the RAMADDA entry
     *
     * @param request the request
     * @param recordEntry _more_
     *
     * @return the map lines
     *
     * @throws Exception On badness
     */
    public List<double[]> getMapPolyline(Request request,
                                         RecordEntry recordEntry)
            throws Exception {
        long numRecords = recordEntry.getNumRecords();
        int  skipFactor = (int) (numRecords
                                / request.get(ARG_NUMPOINTS, 1000));
        if (skipFactor == 0) {
            skipFactor = 1000;
        }



        String         polylineProperty = "mapline" + skipFactor;

        List<double[]> polyLine         =
            (List<double[]>) recordEntry.getEntry().getTransientProperty(
                polylineProperty);
        if (polyLine == null) {
            final List<double[]> pts         = new ArrayList<double[]>();
            final Bearing        workBearing = new Bearing();
            RecordVisitor        visitor     =
                new BridgeRecordVisitor(getOutputHandler()) {
                double[] lastPoint;
                double   maxDistance   = 0;
                double   totalDistance = 0;
                int      cnt           = 0;
                public boolean doVisitRecord(RecordFile file,
                                             VisitInfo visitInfo,
                                             Record record) {
                    PointRecord pointRecord = (PointRecord) record;
                    double[] pt = new double[] { pointRecord.getLatitude(),
                            pointRecord.getLongitude() };
                    //Keep track of the distances we've seen and put a nan to break the line
                    if (lastPoint != null) {
                        //If there is more than a 2 degree difference then put a break;
                        if ((Math.abs(pt[0] - lastPoint[0]) > 2)
                                || (Math.abs(pt[1] - lastPoint[1]) > 2)) {
                            pts.add(new double[] { Double.NaN, Double.NaN });
                        } else {
                            LatLonPointImpl p1 = new LatLonPointImpl(pt[0],
                                                     pt[1]);
                            LatLonPointImpl p2 =
                                new LatLonPointImpl(lastPoint[0],
                                    lastPoint[1]);
                            double distance = Bearing.calculateBearing(p1,
                                                  p2, null).getDistance();
                            //System.err.println(pt[0] +" " + pt[1] + " distance:" + distance +" max:" + maxDistance);
                            if ((maxDistance != 0)
                                    && (distance > maxDistance * 5)) {
                                //                                    System.err.println("BREAK");
                                pts.add(new double[] { Double.NaN,
                                        Double.NaN });
                                distance = 0;
                            }
                            maxDistance = Math.max(maxDistance, distance);
                        }
                    }
                    pts.add(pt);
                    lastPoint = pt;
                    cnt++;
                    if (cnt > 100) {
                        cnt         = 0;
                        maxDistance = 0;
                    }

                    return true;
                }
            };

            getRecordJobManager().visitSequential(request, recordEntry,
                    visitor, new VisitInfo(true, skipFactor));
            polyLine = pts;
            recordEntry.getEntry().putTransientProperty(polylineProperty,
                    polyLine);
        }

        return polyLine;
    }

    /**
     * Show the products form
     *
     * @param request the request
     * @param group the entry group
     * @param subGroups sub groups
     * @param entries sub entries
     *
     * @return the ramadda result
     *
     * @throws Exception on badness
     */
    public Result outputGroupForm(Request request, Entry group,
                                  List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        return outputGroupForm(request, group, subGroups, entries,
                               new StringBuffer());
    }


    /**
     * Show the products form
     *
     * @param request the request
     * @param group the group
     * @param subGroups sub groups
     * @param entries sub entries
     * @param sb buffer
     *
     * @return the ramadda result
     *
     * @throws Exception on badness
     */
    public Result outputGroupForm(Request request, Entry group,
                                  List<Entry> subGroups, List<Entry> entries,
                                  StringBuffer sb)
            throws Exception {
        boolean showUrl = request.get(ARG_SHOWURL, false);

        sb.append(request.formPost(getRepository().URL_ENTRY_SHOW));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, group.getId()));
        sb.append(HtmlUtils.hidden(ARG_RECORDENTRY_CHECK, "true"));

        List<? extends RecordEntry> recordEntries =
            getOutputHandler().makeRecordEntries(request, entries, false);

        StringBuffer entrySB = new StringBuffer();
        entrySB.append("<table width=100%>");
        entrySB.append(
            "<tr><td><b>File</b></td><td><b># Points</b></td></tr>");
        long totalSize = 0;

        for (RecordEntry recordEntry : recordEntries) {
            Entry entry = recordEntry.getEntry();
            entrySB.append("<tr><td>");

            entrySB.append(HtmlUtils.checkbox(ARG_RECORDENTRY, entry.getId(),
                    true));
            entrySB.append(getOutputHandler().getEntryLink(request, entry));
            entrySB.append("</td><td align=right>");
            long numRecords = recordEntry.getNumRecords();
            totalSize += numRecords;
            entrySB.append(formatPointCount(numRecords));
            entrySB.append("</td></tr>");
        }
        if (recordEntries.size() > 1) {
            entrySB.append("<tr><td>" + msgLabel("Total")
                           + "</td><td align=right>"
                           + formatPointCount(totalSize));
            entrySB.append("</td></tr>");
        }
        entrySB.append("</table>");

        if (recordEntries.size() == 0) {
            sb.append(getRepository().showDialogNote(msg("No data files")));

            return new Result("", sb);
        }


        String files;
        if (recordEntries.size() == 1) {
            files = "<table width=100%><tr><td width=75%>"
                    + entrySB.toString()
                    + "</td><td width=25%>&nbsp;</td><tr></table>";

        } else {
            files =
                "<table width=100%><tr><td width=75%>"
                + HtmlUtils.div(entrySB.toString(), HtmlUtils.style("max-height:100px;  overflow-y: auto; border: 1px #999999 solid;"))
                + "</td><td width=25%>&nbsp;</td><tr></table>";
        }

        String extra = HtmlUtils.formEntryTop((recordEntries.size() == 1)
                ? ""
                : msgLabel("Files"), files);

        addToGroupForm(request, group, sb, recordEntries, extra);
        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.submit(msg("Get Data"), ARG_GETDATA));
        sb.append(HtmlUtils.formClose());
        return new Result("", sb);
    }

    public void addToGroupForm(Request request, Entry group, StringBuffer sb, List<? extends RecordEntry> recordEntries, String extra) throws Exception  {

    }

    /**
     * add the Settings
     *
     * @param request request
     * @param entry the entry
     * @param sb buffer to append to
     * @param recordEntry the recordentry
     *
     * @throws Exception On badness
     */
    public void addSettingsForm(Request request, Entry entry,
                                 StringBuffer sb, RecordEntry recordEntry)
            throws Exception {

        boolean      showUrl = request.get(ARG_SHOWURL, false);
        StringBuffer extra   = new StringBuffer();
        extra.append(HtmlUtils.formTable());
        String initialDegrees = "" + getDefaultRadiusDegrees(request,
                                    entry.getBounds());

        if (request.defined(ARG_GRID_RADIUS_DEGREES)) {
            initialDegrees = request.getString(ARG_GRID_RADIUS_DEGREES, "");
        }

        String initialCells = "0";
        if (request.defined(ARG_GRID_RADIUS_CELLS)) {
            initialCells = request.getString(ARG_GRID_RADIUS_CELLS, "");
        }
        extra.append(HtmlUtils.hidden(ARG_GRID_RADIUS_DEGREES_ORIG,
                                      initialDegrees));
        extra.append(
            HtmlUtils.formEntry(
                msgLabel("Grid radius for IDW"),
                msgLabel("Degrees")
                + HtmlUtils.input(
                    ARG_GRID_RADIUS_DEGREES, initialDegrees,
                    12) + HtmlUtils.space(4) + msgLabel("or # of grid cells")
                        + HtmlUtils.input(
                            ARG_GRID_RADIUS_CELLS, initialCells, 4)));


        extra.append(
            HtmlUtils.formEntry(
                msgLabel("Fill missing"),
                HtmlUtils.checkbox(
                    PointOutputHandler.ARG_FILLMISSING, "true", false)));


        extra.append(
            HtmlUtils.formEntry(
                msgLabel("Hill shading"),
                msgLabel("Azimuth")
                + HtmlUtils.input(ARG_HILLSHADE_AZIMUTH, "315", 4)
                + HtmlUtils.space(4) + msgLabel("Angle")
                + HtmlUtils.input(ARG_HILLSHADE_ANGLE, "45", 4)));
        extra.append(
            HtmlUtils.formEntry(
                msgLabel("Image Dimensions"),
                HtmlUtils.input(
                    ARG_WIDTH, request.getString(ARG_WIDTH, "" + DFLT_WIDTH),
                    5) + " X "
                       + HtmlUtils.input(
                           ARG_HEIGHT,
                           request.getString(ARG_HEIGHT, "" + DFLT_HEIGHT),
                           5)));


        String paramWidget = null;
        List   params      = new ArrayList();
        params.add(new TwoFacedObject(msg(LABEL_ALTITUDE), ""));
        if (recordEntry != null) {
            for (RecordField attr :
                    recordEntry.getRecordFile().getChartableFields()) {
                params.add(new TwoFacedObject(attr.getLabel(),
                        "" + attr.getParamId()));
            }
        }

        if (params.size() > 1) {
            extra.append(
                HtmlUtils.formEntry(
                    msgLabel("Parameter for Image and Grid"),
                    HtmlUtils.select(
                        RecordOutputHandler.ARG_PARAMETER, params,
                        request.getString(
                            RecordOutputHandler.ARG_PARAMETER,
                            (String) null))));
        }


        extra.append(
            HtmlUtils.formEntry(
                msgLabel("Color Table"),
                HtmlUtils.select(
                    ARG_COLORTABLE, ColorTable.getColorTableNames(),
                    request.getString(ARG_COLORTABLE, (String) null))));


        extra.append(HtmlUtils.formTableClose());

        sb.append(
            HtmlUtils.row(
                HtmlUtils.colspan(formHeader("Advanced Settings"), 2)));

        if (recordEntry.getRecordFile().isCapable(PointFile.ACTION_GRID)) {
            sb.append(
                HtmlUtils.formEntryTop(
                    msgLabel("Gridding"),
                    HtmlUtils.makeShowHideBlock(
                        msg(""), extra.toString(), showUrl)));
        }


        StringBuffer points = new StringBuffer();
        points.append(HtmlUtils.formTable());
        points.append(
            HtmlUtils.formEntry(
                "",
                HtmlUtils.checkbox(ARG_GEOREFERENCE, "true", false) + " "
                + msg("Convert coordinates to lat/lon if needed")));

        points.append(
            HtmlUtils.formEntry(
                "",
                HtmlUtils.checkbox(ARG_INCLUDEWAVEFORM, "true", false) + " "
                + msg("Include waveforms")));


        points.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.formEntryTop(msgLabel("Points"),
                                         HtmlUtils.makeShowHideBlock(msg(""),
                                             points.toString(), false)));




        StringBuffer processSB = new StringBuffer();
        processSB.append(HtmlUtils.formTable());
        processSB.append(HtmlUtils.formEntry("",
                                             HtmlUtils.checkbox(ARG_ASYNCH,
                                                 "true", true) + " "
                                                     + msg("Asynchronous")));

        processSB.append(
            HtmlUtils.formEntry(
                "",
                HtmlUtils.checkbox(ARG_RESPONSE, RESPONSE_XML, false)
                + " Return response in XML"));

        processSB.append(
            HtmlUtils.formEntry(
                "",
                HtmlUtils.checkbox(ARG_POINTCOUNT, "true", false)
                + " Just return the estimated point count"));



        getOutputHandler().addPublishWidget(
            request, entry, processSB,
            msg("Select a folder to publish the product to"));

        processSB.append(HtmlUtils.formTableClose());


        sb.append(HtmlUtils.formEntryTop(msgLabel("Processing"),
                                         HtmlUtils.makeShowHideBlock(msg(""),
                                             processSB.toString(), false)));


        sb.append(
            HtmlUtils.row(
                HtmlUtils.colspan(formHeader("Job Information"), 2)));

        User user = request.getUser();
        if (getAdmin().isEmailCapable()) {
            sb.append(HtmlUtils.formEntry(msgLabel("Send email to"),
                                          HtmlUtils.input(ARG_JOB_EMAIL,
                                              user.getEmail(), 40)));
        }
        sb.append(HtmlUtils.formEntry(msgLabel("Your name"),
                                      HtmlUtils.input(ARG_JOB_USER,
                                          user.getName(), 40)));

        sb.append(HtmlUtils.formEntry(msgLabel("Job name"),
                                      HtmlUtils.input(ARG_JOB_NAME, "", 40)));

        sb.append(
            HtmlUtils.formEntryTop(
                msgLabel("Description"),
                HtmlUtils.textArea(ARG_JOB_DESCRIPTION, "", 5, 40)));



    }




}
