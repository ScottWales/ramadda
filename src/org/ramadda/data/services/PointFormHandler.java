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


import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;
import org.jfree.ui.*;

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

    public static List<Integer> xindices = new ArrayList<Integer>();

    /** _more_ */
    public     static int[] drawCnt = { 0 };

    /** _more_ */
    public     static boolean debugChart = false;


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

    public String getSessionPrefix() {
        return "points.";
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

    public void getPointFormats(List<HtmlUtils.Selector> outputs,
                                boolean forCollection) {
        outputs.add(getSelect(getPointOutputHandler().OUTPUT_SUBSET));
        outputs.add(getSelect(getPointOutputHandler().OUTPUT_CSV));
        outputs.add(getSelect(getPointOutputHandler().OUTPUT_LATLONALTCSV));
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
     * make the product/subset form
     *
     * @param request the request
     * @param entry The entry
     *
     * @return ramadda result
     *
     * @throws Exception on badness
     */
    public Result outputEntryForm(Request request, Entry entry)
            throws Exception {
        return outputEntryForm(request, entry, new StringBuffer());
    }



    /**
     * make the form
     *
     * @param request the request
     * @param entry The entry
     * @param sb buffer to append to
     *
     * @return ramadda result
     *
     * @throws Exception on badness
     */
    public Result outputEntryForm(Request request, Entry entry,
                                  StringBuffer sb)
            throws Exception {
        RecordEntry recordEntry = getPointOutputHandler().doMakeEntry(request, entry);
        sb.append(request.formPost(getRepository().URL_ENTRY_SHOW));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));

        addToEntryForm(request, entry, sb, recordEntry);
        sb.append("<tr><td>");
        sb.append(HtmlUtils.submit(msg("Get Data"), ARG_GETDATA));
        sb.append("</td><td></td></tr>");
        sb.append(HtmlUtils.formTableClose());
        return new Result("", sb);
    }


   public void addToEntryForm(Request request, Entry entry, StringBuffer sb,  RecordEntry recordEntry) throws Exception {
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



    /**
     * add to form
     *
     * @param request request
     * @param entry _more_
     * @param sb buffer
     * @param forGroup for group
     * @param recordEntry the entry
     * @param extraSubset _more_
     *
     * @throws Exception On badness
     */
    public void addSelectForm(Request request, Entry entry, StringBuffer sb,
                               boolean forGroup, RecordEntry recordEntry,
                               String extraSubset)
            throws Exception {

        long                     numRecords   = forGroup
                ? 0
                : recordEntry.getNumRecords();

        List<HtmlUtils.Selector> pointFormats =
            new ArrayList<HtmlUtils.Selector>();
        List<HtmlUtils.Selector> gridFormats =
            new ArrayList<HtmlUtils.Selector>();
        getPointFormats(pointFormats, forGroup);
        getGridFormats(gridFormats, forGroup);
        List<List<HtmlUtils.Selector>> formatLists =
            new ArrayList<List<HtmlUtils.Selector>>();
        formatLists.add(pointFormats);
        formatLists.add(gridFormats);

        sb.append(HtmlUtils.formTable(" width=100%  border=0 "));
        sb.append("<tr><td width=15%>");
        sb.append(HtmlUtils.submit(msg("Get Data"), ARG_GETDATA));
        sb.append("</td><td></td></tr>");
        sb.append(HtmlUtils.hidden(ARG_OUTPUT, getPointOutputHandler().OUTPUT_PRODUCT.getId()));


        StringBuffer    productSB      = new StringBuffer();
        HashSet<String> selectedFormat = getFormats(request);
        StringBuffer    formats        =
            new StringBuffer(
                "<table border=0 cellpadding=4 cellspacing=0><tr valign=top>");

        int          cnt = 0;
        StringBuffer formatCol;
        StringBuffer gridsCol = new StringBuffer();
        gridsCol.append(HtmlUtils.b(msg("Select Grids")));
        gridsCol.append(HtmlUtils.p());
        for (int i = 0; i < GRID_ARGS.length; i++) {
            String helpImg =
                HtmlUtils.img(getRepository().iconUrl(ICON_HELP),
                              GRID_HELP[i]);
            gridsCol.append(helpImg);
            gridsCol.append(HtmlUtils.checkbox(GRID_ARGS[i], "true", false));
            gridsCol.append(msg(GRID_LABELS[i]));
            gridsCol.append(HtmlUtils.p());
        }



        for (int i = 0; i < formatLists.size(); i++) {
            List<HtmlUtils.Selector> formatList = formatLists.get(i);
            formatCol = new StringBuffer();
            if (i == 0) {
                formatCol.append(HtmlUtils.b(msg("Point Products")));
            } else {
                if ( !recordEntry.getRecordFile().isCapable(
                        PointFile.ACTION_GRID)) {
                    continue;
                }
                formats.append(HtmlUtils.col(HtmlUtils.space(5)));
                formats.append(
                    HtmlUtils.col(
                        HtmlUtils.img(
                            getRepository().fileUrl(
                                "/icons/blank.gif")), HtmlUtils.style(
                                    "border-left:1px #000000 solid")));
                formats.append(HtmlUtils.col(HtmlUtils.space(4)));
                formats.append(HtmlUtils.col(gridsCol.toString()));
                String middle =
                    "<br><br><br>&nbsp;&nbsp;&nbsp;to make&nbsp;&nbsp;&nbsp;<br>"
                    + HtmlUtils.img(
                        getRepository().fileUrl(
                            "/nlas/icons/rightarrow.jpg"));
                formats.append(
                    HtmlUtils.col(
                        middle,
                        HtmlUtils.attr(HtmlUtils.ATTR_ALIGN, "center")));
                formatCol.append(HtmlUtils.b(msg("Select Products")));
            }
            formatCol.append(HtmlUtils.p());

            for (HtmlUtils.Selector selector : formatList) {
                formatCol.append(HtmlUtils.checkbox(ARG_PRODUCT,
                        selector.getId(),
                        selectedFormat.contains(selector.getId())));
                formatCol.append(" ");
                if (selector.getIcon() != null) {
                    formatCol.append(HtmlUtils.img(selector.getIcon()));
                    formatCol.append(" ");
                }
                formatCol.append(selector.getLabel());
                formatCol.append(HtmlUtils.p());
            }
            formats.append(HtmlUtils.col(formatCol.toString()));
        }
        formats.append("</tr></table>");
        productSB.append(HtmlUtils.row(HtmlUtils.colspan(formats.toString(),
                2)));

        StringBuffer subsetSB = new StringBuffer();
        if (numRecords > 0) {
            subsetSB.append(HtmlUtils.formEntry("# " + msgLabel("Points"),
                    formatPointCount(numRecords)));
        }

        MapInfo map = getRepository().getMapManager().createMap(request,
                          true);
        List<Metadata> metadataList = getMetadataManager().getMetadata(entry);
        boolean didMetadata = map.addSpatialMetadata(entry, metadataList);
        if ( !didMetadata) {
            map.addBox(entry,
                       new MapProperties(MapInfo.DFLT_BOX_COLOR, false,
                                         true));
        } else {
            map.centerOn(entry);
        }
        SessionManager sm          = getRepository().getSessionManager();
        String         mapSelector =
            map.makeSelector(ARG_AREA, true,
                             new String[] {
                                 request.getStringOrSession(ARG_AREA_NORTH,
                                     getSessionPrefix(), ""),
                                 request.getStringOrSession(ARG_AREA_WEST,
                                     getSessionPrefix(), ""),
                                 request.getStringOrSession(ARG_AREA_SOUTH,
                                     getSessionPrefix(), ""),
                                 request.getStringOrSession(ARG_AREA_EAST,
                                     getSessionPrefix(), ""), }, "", "");

        subsetSB.append(HtmlUtils.formEntryTop(msgLabel("Region"),
                mapSelector));

        if (recordEntry != null) {
            String help        = "Probablity a point will be included 0.-1.0";
            String probHelpImg =
                HtmlUtils.img(getRepository().iconUrl(ICON_HELP), help);
            String prob =
                HtmlUtils.space(3) + msgLabel("Or use probability") + " "
                + HtmlUtils.input(ARG_PROBABILITY,
                                  request.getString(ARG_PROBABILITY, ""),
                                  4) + probHelpImg;
            if (recordEntry.getRecordFile().isCapable(
                    PointFile.ACTION_DECIMATE)) {
                subsetSB.append(HtmlUtils.formEntry(msgLabel("Decimate"),
                        msgLabel("Skip every") + " "
                        + HtmlUtils.input(ARG_RECORD_SKIP,
                                          request.getString(ARG_RECORD_SKIP,
                                              ""), 4) + prob));
            }

            if (recordEntry.getRecordFile().isCapable(
                    PointFile.ACTION_TRACKS)) {
                subsetSB.append(
                    HtmlUtils.formEntry(
                        msgLabel("GLAS Tracks"),
                        HtmlUtils.input(
                            ARG_TRACKS, request.getString(ARG_TRACKS, ""),
                            20) + " "
                                + msg("Comma separated list of track numbers")));
            }
        }


        // Look for searchable fields
        List<RecordField> allFields        = null;
        List<RecordField> searchableFields = null;
        if (recordEntry != null) {
            searchableFields =
                recordEntry.getRecordFile().getSearchableFields();
            allFields = recordEntry.getRecordFile().getFields();
        } else if (forGroup
                   && (entry.getTypeHandler()
                       instanceof RecordCollectionTypeHandler)) {
            //Its a Collection
            RecordEntry childEntry =
                ((RecordCollectionTypeHandler) entry.getTypeHandler())
                    .getChildRecordEntry(entry);
            if (childEntry != null) {
                searchableFields =
                    childEntry.getRecordFile().getSearchableFields();
                allFields = childEntry.getRecordFile().getFields();
            }
        }

        if (allFields != null) {
            StringBuffer paramSB = null;
            for (RecordField attr : allFields) {

                //Skip arrays
                if (attr.getArity() > 1) {
                    continue;
                }
                if (paramSB == null) {
                    paramSB = new StringBuffer();
                    paramSB.append(HtmlUtils.formTable());
                }
                String label = attr.getName();
                if (attr.getDescription().length() > 0) {
                    label = label + " - " + attr.getDescription();
                }
                paramSB.append(HtmlUtils.formEntry("",
                        HtmlUtils.checkbox(ARG_FIELD_USE, attr.getName(),
                                           false) + " " + label));
            }
            if (paramSB != null) {
                paramSB.append(HtmlUtils.formTableClose());
                subsetSB.append(
                    HtmlUtils.formEntryTop(
                        msgLabel("Select Fields"),
                        HtmlUtils.makeShowHideBlock(
                            msg(""), paramSB.toString(), false)));

            }
        }


        if (searchableFields != null) {
            StringBuffer paramSB = null;
            for (RecordField field : searchableFields) {
                List<String[]> enums        = field.getEnumeratedValues();
                String         searchSuffix = field.getSearchSuffix();
                if (searchSuffix == null) {
                    searchSuffix = "";
                } else {
                    searchSuffix = "  " + searchSuffix;
                }
                if (field.isBitField()) {
                    if (paramSB == null) {
                        paramSB = new StringBuffer();
                        paramSB.append(HtmlUtils.formTable());
                    }
                    String[]     bitFields = field.getBitFields();
                    StringBuffer widgets   = new StringBuffer();
                    paramSB.append(
                        HtmlUtils.row(
                            HtmlUtils.colspan(
                                formHeader(field.getName()), 2)));
                    List values = new ArrayList();
                    values.add(new TwoFacedObject("--", ""));
                    values.add(new TwoFacedObject("true", "true"));
                    values.add(new TwoFacedObject("false", "false"));
                    String urlArgPrefix = ARG_SEARCH_PREFIX + field.getName()
                                          + "_" + ARG_BITFIELD + "_";
                    for (int bitIdx = 0; bitIdx < bitFields.length;
                            bitIdx++) {
                        String bitField = bitFields[bitIdx].trim();
                        if (bitField.length() == 0) {
                            continue;
                        }
                        String value = request.getString(urlArgPrefix
                                           + bitIdx, "");
                        paramSB.append(HtmlUtils.formEntry(bitField + ":",
                                HtmlUtils.select(urlArgPrefix + bitIdx,
                                    values, value, "")));
                    }
                } else if (enums != null) {
                    List values = new ArrayList();
                    values.add(new TwoFacedObject("--", ""));
                    for (String[] tuple : enums) {
                        values.add(new TwoFacedObject(tuple[1], tuple[0]));
                    }
                    String attrWidget = HtmlUtils.select(ARG_SEARCH_PREFIX
                                            + field.getName(), values, "",
                                                "");
                    if (paramSB == null) {
                        paramSB = new StringBuffer();
                        paramSB.append(HtmlUtils.formTable());
                    }
                    paramSB.append(
                        HtmlUtils.formEntry(
                            msgLabel(field.getLabel()),
                            attrWidget + searchSuffix));
                } else {
                    String attrWidget = HtmlUtils.input(
                                            ARG_SEARCH_PREFIX
                                            + field.getName() + "_min", "",
                                                HtmlUtils.SIZE_8) + " - "
                                                    + HtmlUtils.input(
                                                        ARG_SEARCH_PREFIX
                                                        + field.getName()
                                                        + "_max", "",
                                                            HtmlUtils.SIZE_8);
                    if (paramSB == null) {
                        paramSB = new StringBuffer();
                        paramSB.append(HtmlUtils.formTable());
                    }
                    paramSB.append(
                        HtmlUtils.formEntry(
                            msgLabel(field.getLabel() + " range"),
                            attrWidget + searchSuffix));
                }

            }
            if (paramSB != null) {
                paramSB.append(HtmlUtils.formTableClose());
                subsetSB.append(
                    HtmlUtils.formEntryTop(
                        msgLabel("Search Fields"),
                        HtmlUtils.makeShowHideBlock(
                            msg(""), paramSB.toString(), false)));


            }
        }



        subsetSB.append(extraSubset);
        sb.append(HtmlUtils.row(HtmlUtils.colspan(formHeader("Subset Data"),
                2)));
        sb.append(subsetSB);
        sb.append(
            HtmlUtils.row(
                HtmlUtils.colspan(formHeader("Select Products"), 2)));
        sb.append(productSB);

    }




    /**
     * create jfree chart
     *
     * @param request the request
     * @param entry The entry
     * @param dataset the dataset
     * @param backgroundImage background image
     *
     * @return the chart
     */
    public static JFreeChart createTimeseriesChart(Request request,
            Entry entry, XYDataset dataset, Image backgroundImage) {
        JFreeChart chart = ChartFactory.createXYLineChart("",  // chart title
            "",                                                // x axis label
            "Height",                                          // y axis label
            dataset,                                           // data
            PlotOrientation.VERTICAL, true,                    // include legend
            true,                                              // tooltips
            false                                              // urls
                );
        chart.setBackgroundPaint(Color.white);
        //        chart.setBackgroundPaint(Color.red);
        XYPlot plot = (XYPlot) chart.getPlot();

        if (backgroundImage != null) {
            plot.setBackgroundImage(backgroundImage);
            plot.setBackgroundImageAlignment(org.jfree.ui.Align.TOP_LEFT);
        }

        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setVisible(false);
        plot.setInsets(new RectangleInsets(0, 0, 0, 0));
        plot.setAxisOffset(new RectangleInsets(5, 0, 5, 0));

        return chart;
    }



    /**
     * Class description
     *
     *
     * @version        $version$, Mon, Aug 29, '11
     * @author         Enter your name here...
     */
    public static class PlotInfo {

        /** _more_ */
        public List<Double> alts;

        /** _more_ */
        public         int minX = Integer.MAX_VALUE;

        /** _more_ */
        public         int maxX = 0;

        /** _more_ */
        public         int minIndex = Integer.MAX_VALUE;

        /** _more_ */
        public         int maxIndex = 0;

        /**
         * _more_
         *
         * @param index _more_
         */
        public void setIndex(int index) {
            minIndex = Math.min(minIndex, index);
            maxIndex = Math.max(maxIndex, index);
        }

        /**
         * _more_
         *
         * @param x _more_
         */
        public void setX(int x) {
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
        }


    }




    /**
     * Class description
     *
     *
     * @version        $version$, Thu, Sep 15, '11
     * @author         Enter your name here...
     */
    public static class MyStandardXYItemRenderer extends StandardXYItemRenderer {

        /** _more_ */
        PlotInfo plotInfo;

        /**
         * _more_
         *
         * @param plotInfo _more_
         */
        public MyStandardXYItemRenderer(PlotInfo plotInfo) {
            this.plotInfo = plotInfo;
        }

        /**
         * _more_
         *
         * @param g2 _more_
         * @param state _more_
         * @param dataArea _more_
         * @param info _more_
         * @param plot _more_
         * @param domainAxis _more_
         * @param rangeAxis _more_
         * @param dataset _more_
         * @param series _more_
         * @param item _more_
         * @param crosshairState _more_
         * @param pass _more_
         */
        public void drawItem(java.awt.Graphics2D g2,
                             XYItemRendererState state,
                             java.awt.geom.Rectangle2D dataArea,
                             PlotRenderingInfo info, XYPlot plot,
                             ValueAxis domainAxis, ValueAxis rangeAxis,
                             XYDataset dataset, int series, int item,
                             CrosshairState crosshairState, int pass) {
            super.drawItem(g2, state, dataArea, info, plot, domainAxis,
                           rangeAxis, dataset, series, item, crosshairState,
                           pass);
            RectangleEdge domainEdge = plot.getDomainAxisEdge();
            int           x = (int) domainAxis.valueToJava2D(item, dataArea,
                        domainEdge);
            plotInfo.setX(x);
            drawCnt[0]++;
            if (debugChart && (drawCnt[0] % 10) == 0) {
                int index = xindices.get(item);
                g2.setColor(Color.red);
                g2.drawLine(x, 15, x, 500);
                g2.drawString("" + index, x, 20);
                //                g2.drawString("" + plotInfo.alts.get(item), x, 50);
                //                System.out.println("chart: " + item +" " + index +" " +plotInfo.alts.get(item));
            }
        }
    }

    ;






}
