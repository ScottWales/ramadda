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
import org.ramadda.data.point.binary.*;

import org.ramadda.data.record.*;


import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;

import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.job.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.TypeHandler;
import org.ramadda.util.ColorTable;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.SelectionRectangle;

import org.ramadda.util.TempDir;

import org.ramadda.util.grid.*;

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
public class PointOutputHandler extends RecordOutputHandler {


    /** The category for the all of the output types */
    public static final String OUTPUT_CATEGORY = "Point Data";


    /** This is used to create a product for a lidar collection or a lidar file. */
    public OutputType OUTPUT_PRODUCT;

    /** output type */
    public OutputType OUTPUT_RESULTS;

    /** output type */
    public OutputType OUTPUT_VIEW;

    /** output type */
    public OutputType OUTPUT_METADATA;


    /** output type */
    public OutputType OUTPUT_GETPOINTINDEX;


    /** output type */
    public OutputType OUTPUT_GETLATLON;



    /** output type */
    public OutputType OUTPUT_IMAGE;


    /** _more_ */
    public OutputType OUTPUT_BOUNDS;

    /** output type */
    public OutputType OUTPUT_NC;

    /** output type */
    public OutputType OUTPUT_HILLSHADE;

    /** output type */
    public OutputType OUTPUT_KMZ;

    /** output type */
    public OutputType OUTPUT_KML_TRACK;


    /** output type */
    public OutputType OUTPUT_SUBSET;


    /** output type */
    public OutputType OUTPUT_KML;

    /** output type */
    public OutputType OUTPUT_LATLONALTBIN;



    /** output type */
    public OutputType OUTPUT_LATLONALTCSV;


    /** output type */
    public OutputType OUTPUT_CSV;



    /** output type */
    public OutputType OUTPUT_ASC;


    /**
     * constructor. This gets called by the Repository via reflection
     *
     * @param repository the repository
     * @param element the xml from outputhandlers.xml
     * @throws Exception on badness
     */
    public PointOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        doMakeOutputTypes();
    }


    protected void doMakeOutputTypes() {
        String category = getOutputCategory();
        String base= getDomainBase();

        OUTPUT_PRODUCT =
            new OutputType("Results", base +".product", OutputType.TYPE_OTHER);

        OUTPUT_RESULTS =
            new OutputType("Results", base +".results", OutputType.TYPE_OTHER);

        OUTPUT_VIEW = new OutputType("View Data",
                                     base +".view",
                                     OutputType.TYPE_OTHER,
                                     "", ICON_DATA,
                                     category);

        OUTPUT_METADATA =
            new OutputType("Metadata ", base +".metadata",
                           OutputType.TYPE_OTHER, "", ICON_METADATA,
                           category);


        OUTPUT_GETPOINTINDEX =
            new OutputType("LiDAR Point index query", base +".getpointindex",
                           OutputType.TYPE_OTHER, "", ICON_DATA,
                           category);

        OUTPUT_GETLATLON =
            new OutputType("LiDAR Lat/Lon query", base +".getlatlon",
                           OutputType.TYPE_OTHER, "", ICON_DATA,
                           category);



        OUTPUT_IMAGE = new OutputType("Image",
                                      base +".image",
                                      OutputType.TYPE_OTHER,
                                      "png", ICON_IMAGE,
                                      category);

        OUTPUT_BOUNDS =
            new OutputType("Point Bounds", base +".bounds",
                           OutputType.TYPE_OTHER);

        OUTPUT_NC = new OutputType("NetCDF Grid",
                                   base +".nc",
                                   OutputType.TYPE_OTHER,
                                   "nc", ICON_DATA,
                                   category);

        OUTPUT_HILLSHADE =
        new OutputType("Hill Shade Image", base +".hillshade",
                       OutputType.TYPE_OTHER, "png", ICON_IMAGE,
                       category);

        OUTPUT_KMZ =
        new OutputType("Google Earth", base +".kmz", OutputType.TYPE_OTHER,
                       "kmz", ICON_KML, category);

        OUTPUT_KML_TRACK =
            new OutputType("Google Earth", base +".kml.track",
                           OutputType.TYPE_OTHER, "kml", ICON_KML,
                           category);

        OUTPUT_SUBSET =
            new OutputType("Native format", base +".subset",
                           OutputType.TYPE_OTHER, "", ICON_DATA,
                           category);


        OUTPUT_KML =
            new OutputType("Google Earth", base +".kml", OutputType.TYPE_OTHER,
                           "kml", ICON_KML, category);


        OUTPUT_LATLONALTBIN =
            new OutputType("Binary - Lat/Lon/Alt", base +".latlonaltbin",
                           OutputType.TYPE_OTHER, "llab", ICON_CSV,
                           category);

        OUTPUT_LATLONALTCSV =
            new OutputType("CSV - Lat/Lon/Alt", base +".latlonaltcsv",
                           OutputType.TYPE_OTHER, "csv", ICON_CSV,
                           category);

        OUTPUT_CSV =
        new OutputType("CSV - all fields", base +".csv",
                       OutputType.TYPE_OTHER, "csv", ICON_CSV,
                       category);


        OUTPUT_ASC =
            new OutputType("ARC ASCII Grid", base +".asc", OutputType.TYPE_OTHER,
                           "asc", ICON_DATA, category);

    }

    public String getOutputCategory() {
        return OUTPUT_CATEGORY;
    }

    public String getDomainBase() {
        return "points";
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
        return new PointEntry(this, request, entry);
    }



    /**
     * Not implemented yet. This will get the point index of a given lat/lon
     *
     * @param request request
     * @param entry the entry
     *
     *
     * @return _more_
     * @throws Exception On badness
     */
    public Result outputEntryGetPointIndex(Request request, Entry entry)
            throws Exception {
        //TODO: find the closest index to the lat/lon in the request                                                              
        int          index = 1;
        StringBuffer sb    = new StringBuffer("<result>");
        sb.append("{\"index\":" + index + "}");
        sb.append("</result>");

        return new Result("", sb, "text/xml");
    }



    /**
     * Get the FormHandler property.
     *
     * @return The FormHandler
     */
    public PointFormHandler getPointFormHandler() {
        return (PointFormHandler) super.getFormHandler();
    }



    /**
     * Gets the approximate point count of the given record files. It figures out
     * the  area  of the of the clipping box intersection with each file.
     *
     * @param request request
     * @param subsetEntries entries
     *
     * @return approximate point count in spatial subset
     *
     * @throws Exception On badness
     */
    public long getApproximatePointCount(
            Request request, List<? extends RecordEntry> subsetEntries)
            throws Exception {
        long pointCount = 0;
        storeSession(request);
        double             north     = request.get(ARG_AREA_NORTH, 90.0);
        double             south     = request.get(ARG_AREA_SOUTH, -90.0);
        double             east      = request.get(ARG_AREA_EAST, 180.0);
        double             west      = request.get(ARG_AREA_WEST, -180.0);

        Rectangle2D.Double queryRect = new Rectangle2D.Double(west, south,
                                           east - west, north - south);
        for (RecordEntry entry : subsetEntries) {
            Rectangle2D.Double entryBounds  = entry.getEntry().getBounds();
            Rectangle2D        intersection =
                entryBounds.createIntersection(queryRect);
            double percent =
                (intersection.getWidth() * intersection.getHeight())
                / (entryBounds.getWidth() * entryBounds.getHeight());
            pointCount += (long) (percent * entry.getNumRecords());
        }

        return pointCount;
    }

    /**
     * Checks for  any spatial bounds URL arguments. If defined then only returns
     * the RecordEntry objects that intersect the bounds
     *
     * @param request The request
     * @param recordEntries The entries to process
     *
     * @return spatially subsetting ReordEntry-s
     *
     * @throws Exception On badness
     */
    public List<RecordEntry> doSubsetEntries(
            Request request, List<? extends RecordEntry> recordEntries)
            throws Exception {
        List<RecordEntry>  result  = new ArrayList<RecordEntry>();

        SelectionRectangle theBbox = TypeHandler.getSelectionBounds(request);
        if ( !theBbox.anyDefined()) {
            result.addAll(recordEntries);

            return result;
        }


        storeSession(request);

        theBbox.normalizeLongitude();
        SelectionRectangle[] bboxes = theBbox.splitOnDateLine();

        for (RecordEntry recordEntry : recordEntries) {
            Entry entry = recordEntry.getEntry();
            if ( !entry.hasAreaDefined()) {
                continue;
            }
            for (SelectionRectangle bbox : bboxes) {
                Rectangle2D.Double queryRect = new Rectangle2D.Double(
                                                   bbox.getWest(-180),
                                                   bbox.getSouth(-90),
                                                   bbox.getEast(180)
                                                   - bbox.getWest(
                                                       -180), bbox.getNorth(
                                                       90) - bbox.getSouth(
                                                       -90));
                Rectangle2D.Double entryRect =
                    new Rectangle2D.Double(entry.getWest(), entry.getSouth(),
                                           entry.getEast() - entry.getWest(),
                                           entry.getNorth()
                                           - entry.getSouth());
                if (entryRect.intersects(queryRect)
                        || entryRect.contains(queryRect)
                        || queryRect.contains(entryRect)) {
                    result.add(recordEntry);

                    break;
                }
            }
        }

        return result;
    }


    /**
     * Finally, this does the real work of extracting data and generating products
     *
     * @param request The request
     * @param entry the entry
     * @param asynch Is this an asynchronous request
     * @param pointEntries List of entries to process
     * @param jobId The job ID
     *
     * @return the result
     *
     * @throws Exception On badness
     */
    public Result processEntries(Request request, Entry entry,
                                 boolean asynch,
                                 List<? extends RecordEntry> recordEntries, Object jobId)
            throws Exception {

        List<PointEntry> pointEntries  = PointEntry.toPointEntryList(recordEntries);
        if ( !getRecordJobManager().canAcceptJob()) {
            return makeRequestErrorResult(request,
                                          "Too many processing requests");
        }

        //Get the product formats
        HashSet<String> formats = getProductFormats(request);

        //If nothing selected then flake out
        if (formats.size() == 0) {
            return makeRequestErrorResult(request,
                                          "No product formats were selected");
        }

        //If more than one format is selected and this is a synchronous call then raise an error
        if ((formats.size() > 1) && !asynch) {
            return makeRequestErrorResult(
                                   request,
                "Cannot have more than one product format selected when doing an aysnchronous request");
        }


        List<RecordVisitor> visitors    = new ArrayList<RecordVisitor>();
        GridVisitor         gridVisitor = null;
        boolean             quickScan   = false;
        boolean             needFull    = false;
        Result              result      = null;

        JobInfo             info        = null;
        if (jobId != null) {
            info = getRecordJobManager().getJobInfo(jobId);
        }
        if (info == null) {
            info = new JobInfo();
        }
        final JobInfo theJobInfo = info;

        try {
            //Make a RecordVisitor for each point product type
            if (formats.contains(OUTPUT_CSV.getId())) {
                needFull = true;
                visitors.add(makeCsvVisitor(request, entry, pointEntries,
                                            jobId));
            }
            if (formats.contains(OUTPUT_LATLONALTCSV.getId())) {
                quickScan = true;
                visitors.add(makeLatLonAltCsvVisitor(request, entry,
                        pointEntries, jobId));
            }
            if (formats.contains(OUTPUT_LATLONALTBIN.getId())) {
                quickScan = true;
                visitors.add(makeLatLonAltBinVisitor(request, entry,
                        pointEntries, jobId, null));
            }

            if (formats.contains(OUTPUT_NC.getId())) {
                needFull = true;
                visitors.add(makeNcVisitor(request, entry, pointEntries,
                                           jobId));

            }


            if (formats.contains(OUTPUT_NC.getId())) {
                //            result = outputEntryNc(request, entry,  pointEntries,
                //                                    jobId);
            }

            //Tracks just do them
            if (formats.contains(OUTPUT_KML_TRACK.getId())) {
                result = outputEntryKmlTrack(request, entry, pointEntries,
                                             jobId);
            }

            //TODO: Subset we just do directly
            //We need to do a visitor based approach
            if (formats.contains(OUTPUT_SUBSET.getId())) {
                //This is the subset to the original format
                info.setCurrentStatus("Creating Point file...");
                result = outputEntrySubset(request, entry, pointEntries,
                                           info);
                info.addStatusItem("Point file created");
                if ( !jobOK(jobId)) {
                    return result;
                }
            }


            //Check if we need to make a grid
            if (formats.contains(OUTPUT_ASC.getId())
                    || formats.contains(OUTPUT_IMAGE.getId())
                    || formats.contains(OUTPUT_KMZ.getId())
                    || formats.contains(OUTPUT_HILLSHADE.getId())) {
                needFull = true;
                if (gridVisitor == null) {
                    gridVisitor = makeGridVisitor(request, pointEntries,
                            getBounds(request, pointEntries));
                    visitors.add(gridVisitor);
                }
            }

            if (needFull) {
                quickScan = false;
            }

            //For now don't use the quickscan file as things get screwed up with the Glas track search
            quickScan = false;

            if ( !jobOK(jobId)) {
                return result;
            }


            //Run through the visitors
            if (visitors.size() > 0) {
                RecordVisitorGroup groupVisitor =
                    new RecordVisitorGroup(visitors) {
                    public boolean visitRecord(RecordFile file,
                            VisitInfo visitInfo, Record record) {
                        if ( !super.visitRecord(file, visitInfo, record)) {
                            return false;
                        }
                        if (getCount() == 1) {
                            theJobInfo.setCurrentStatus("Reading points...");
                        } else if ((getCount() % 100000) == 0) {
                            theJobInfo.setCurrentStatus("Read "
                                    + (getCount() / 1000) + "K points");
                        }

                        return true;
                    }

                };
                info.setCurrentStatus("Staging request...");

                memoryCheck("NLAS: memory before:");
                getRecordJobManager().visitSequential(request, pointEntries,
                        groupVisitor, new VisitInfo(quickScan));
                if ( !jobOK(jobId)) {
                    return result;
                }

                info.addStatusItem("Point reading complete");
                info.setNumPoints(groupVisitor.getCount());

                visitors     = null;
                groupVisitor = null;
                pointEntries = null;
                memoryCheck("NLAS: memory after visit:");

                info.setCurrentStatus("Processing products...");

                //If doing a grid then go and make the grid and image products
                if (gridVisitor != null) {
                    gridVisitor.finishedWithAllFiles();
                    outputEntryGrid(request, entry, gridVisitor.getGrid(),
                                    formats, jobId);
                    gridVisitor = null;
                    memoryCheck("NLAS: memory after grid:");
                }
                info.addStatusItem("Product processing complete");
            }
            if (request.responseInXml()) {
                return new Result(XmlUtil.tag(TAG_RESPONSE,
                        XmlUtil.attr(ATTR_CODE, CODE_OK), "OK"), MIME_XML);
            }
            return getDummyResult();
        } catch (Exception exc) {
            try {
                getRecordJobManager().setError(info, exc.toString());
            } catch (Exception noop) {}
            getLogManager().logError("processing point request", exc);
            return makeRequestErrorResult(request, exc.getMessage());
        }
    }


    /**
     * Main entry point for Point Files. This methods handles things like the point map, the lat/lon web services,
     * the product form and product requests.
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

        Result parentResult = super.outputEntry(request, outputType, entry);
        if (parentResult != null) {
            return parentResult;
        }

        if (outputType.equals(OUTPUT_BOUNDS)) {
            return outputEntryBounds(request, entry);
        }

        if (outputType.equals(OUTPUT_GETPOINTINDEX)) {
            return outputEntryGetPointIndex(request, entry);
        }

        if (outputType.equals(OUTPUT_GETLATLON)) {
            return outputEntryGetLatLon(request,
                                        (PointEntry) doMakeEntry(request,
                                            entry));
        }

        if (outputType.equals(OUTPUT_VIEW)) {
            return getFormHandler().outputEntryView(request, outputType,
                                                    doMakeEntry(request, entry));
        }

        if (outputType.equals(OUTPUT_METADATA)) {
            return getFormHandler().outputEntryMetadata(request, outputType,
                                                        doMakeEntry(request, entry));
        }

        return null;
    }


    public Result outputGroup(final Request request,
                              final OutputType outputType, final Entry group,
                              final List<Entry> subGroups,
                              final List<Entry> entries)
            throws Exception {
        Result parentResult  = super.outputGroup(request, outputType, group, subGroups, entries);
        if(parentResult!=null) {
            return parentResult;
        }

        if (outputType.equals(OUTPUT_BOUNDS)) {
            return outputEntryBounds(request, group);
        }


        return null;
    }


    public Result createVisitors(Request request, Entry entry,
                                 boolean asynch,
                                 List<? extends PointEntry> pointEntries, JobInfo jobInfo,
                                 HashSet<String> formats,
                                 List<RecordVisitor> visitors)
        throws Exception {
        GridVisitor         gridVisitor = null;
        Result              result      = null;
            //Make a RecordVisitor for each point product type
            if (formats.contains(OUTPUT_CSV.getId())) {
                visitors.add(makeCsvVisitor(request, entry, pointEntries,
                                            jobInfo.getJobId()));
            }
            if (formats.contains(OUTPUT_LATLONALTCSV.getId())) {
                visitors.add(makeLatLonAltCsvVisitor(request, entry,
                                                     pointEntries, jobInfo.getJobId()));
            }
            if (formats.contains(OUTPUT_LATLONALTBIN.getId())) {
                visitors.add(makeLatLonAltBinVisitor(request, entry,
                                                     pointEntries, jobInfo.getJobId(), null));
            }

            if (formats.contains(OUTPUT_NC.getId())) {
                visitors.add(makeNcVisitor(request, entry, pointEntries,
                                           jobInfo.getJobId()));

            }

            if (formats.contains(OUTPUT_NC.getId())) {
                //            result = outputEntryNc(request, entry,  pointEntries,
                //                                    jobInfo.getJobId());
            }

            //Tracks just do them
            if (formats.contains(OUTPUT_KML_TRACK.getId())) {
                result = outputEntryKmlTrack(request, entry, pointEntries,
                                             jobInfo.getJobId());
            }

            //TODO: Subset we just do directly
            //We need to do a visitor based approach
            if (formats.contains(OUTPUT_SUBSET.getId())) {
                //This is the subset to the original format
                jobInfo.setCurrentStatus("Creating Point file...");
                result = outputEntrySubset(request, entry, pointEntries,
                                           jobInfo);
                jobInfo.addStatusItem("Point file created");
                if ( !jobOK(jobInfo.getJobId())) {
                    return result;
                }
            }


            //Check if we need to make a grid
            if (formats.contains(OUTPUT_ASC.getId())
                || formats.contains(OUTPUT_IMAGE.getId())
                || formats.contains(OUTPUT_KMZ.getId())
                || formats.contains(OUTPUT_HILLSHADE.getId())) {
                if (gridVisitor == null) {
                    gridVisitor = makeGridVisitor(request, pointEntries,
                                                  getBounds(request, pointEntries));
                    visitors.add(gridVisitor);
                }
            }
            return null;
        }



    /**
     * Make a record visitor that creates a CSV file
     *
     * @param request the request
     * @param mainEntry Either the Point Collection or File Entry
     * @param pointEntries entries to process
     * @param jobId The job ID
     *
     * @return visitor
     *
     * @throws Exception on badness
     */
    public RecordVisitor makeCsvVisitor(final Request request,
                                        Entry mainEntry,
                                        List<? extends PointEntry> pointEntries,
                                        final Object jobId)
            throws Exception {

        RecordVisitor visitor = new BridgeRecordVisitor(this, request, jobId,
                                    mainEntry, ".csv") {
            private CsvVisitor csvVisitor;
            int                cnt = 0;
            public boolean doVisitRecord(RecordFile file,
                                         VisitInfo visitInfo, Record record)
                    throws Exception {
                if (csvVisitor == null) {
                    //Set the georeference flag
                    if (request.get(ARG_GEOREFERENCE, false)) {
                        visitInfo.putProperty("georeference",
                                              new Boolean(true));
                    }
                    csvVisitor = new CsvVisitor(getThePrintWriter(),
                            getFields(request, record.getFields()));
                }
                if ( !jobOK(jobId)) {
                    return false;
                }
                synchronized (visitInfo) {
                    if (visitInfo.getCount() == 0) {
                        visitInfo.putProperty(Record.PROP_INCLUDEVECTOR,
                                new Boolean(request.get(ARG_INCLUDEWAVEFORM,
                                    false)));
                    }
                    try {
                        csvVisitor.visitRecord(file, visitInfo, record);
                    } catch (Exception exc) {
                        System.err.println("ERROR:" + exc);
                        throw exc;
                    }
                }
                return true;
            }
        };
        return visitor;
    }



    /**
     * _more_
     *
     * @param request The request
     * @param recordEntries _more_
     * @param bounds _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public GridVisitor makeGridVisitor(
            Request request, List<? extends PointEntry> recordEntries,
            Rectangle2D.Double bounds)
            throws Exception {
        int imageWidth  = request.get(ARG_WIDTH, DFLT_WIDTH);
        int imageHeight = request.get(ARG_HEIGHT, DFLT_HEIGHT);

        if ((imageWidth > 2500) || (imageHeight > 2500)) {
            throw new IllegalArgumentException("Too large image dimension: "
                    + imageWidth + " X " + imageHeight);
        }
        //        System.err.println("Grid BOUNDS: " + bounds);

        IdwGrid llg = new IdwGrid(imageWidth, imageHeight, bounds.y,
                                  bounds.x, bounds.y + bounds.height,
                                  bounds.x + bounds.width);

        //        System.err.println("NLAS Request:" + request.getFullUrl());
        //llg.fillValue(Double.NaN);
        //If nothing specified then default to 2 grid cells radius
        if ( !request.defined(ARG_GRID_RADIUS_DEGREES)
                && !request.defined(ARG_GRID_RADIUS_CELLS)) {
            llg.setRadius(0.0);
            llg.setNumCells(2);
        } else {
            //If the user did not change the degrees radius then get the default radius from the bounds
            if (request.getString(ARG_GRID_RADIUS_DEGREES, "").equals(
                    request.getString(ARG_GRID_RADIUS_DEGREES_ORIG, ""))) {
                //                System.err.println("getting default:" +
                //                                   getFormHandler().getDefaultRadiusDegrees(request, bounds));
                llg.setRadius(
                    getFormHandler().getDefaultRadiusDegrees(
                        request, bounds));
            } else {
                //                System.err.println("using arg:" +
                //                                   request.get(ARG_GRID_RADIUS_DEGREES, 0.0));
                llg.setRadius(request.get(ARG_GRID_RADIUS_DEGREES, 0.0));
            }
            llg.setNumCells(request.get(ARG_GRID_RADIUS_CELLS, 0));
        }
        if (llg.getCellIndexDelta() > 100) {
            System.err.println("NLAS bad grid neighborhood size: "
                               + llg.getCellIndexDelta());
            System.err.println("NLAS llg: " + llg);
            System.err.println("NLAS request:" + request.getFullUrl());

            throw new IllegalArgumentException("bad grid neighborhood size: "
                    + llg.getCellIndexDelta());
        }

        GridVisitor visitor = new GridVisitor(this, request, llg);

        return visitor;
    }



    /**
     * make the visitor for latlonalt binary formats
     *
     * @param request the request
     * @param mainEntry Either the Point Collection or File Entry
     * @param pointEntries entries to process
     * @param jobId The job ID
     * @param inputDos _more_
     *
     * @return the visitor
     *
     * @throws Exception on badness
     */
    public RecordVisitor makeLatLonAltBinVisitor(Request request,
            Entry mainEntry, List<? extends PointEntry> pointEntries,
            final Object jobId, final DataOutputStream inputDos)
            throws Exception {


        RecordVisitor visitor = new BridgeRecordVisitor(this, jobId) {
            public boolean doVisitRecord(RecordFile file,
                                         VisitInfo visitInfo, Record record) {
                try {
                    if ( !jobOK(jobId)) {
                        return false;
                    }
                    GeoRecord geoRecord = (GeoRecord) record;
                    synchronized (MUTEX) {
                        DataOutputStream dos = inputDos;
                        if (dos == null) {
                            dos = getTheDataOutputStream();
                        }
                        dos.writeDouble(geoRecord.getLatitude());
                        dos.writeDouble(geoRecord.getLongitude());
                        dos.writeDouble(geoRecord.getAltitude());
                    }

                    return true;
                } catch (Exception exc) {
                    throw new RuntimeException(exc);
                }
            }
        };

        return visitor;
    }






    /**
     * Make a record visitor that creates a CSV file
     *
     * @param request the request
     * @param mainEntry Either the  Collection or File Entry
     * @param entries entries to process
     * @param jobId The job ID
     *
     * @return visitor
     *
     * @throws Exception on badness
     */
    public RecordVisitor makeLatLonAltCsvVisitor(Request request,
            Entry mainEntry, List<? extends PointEntry> entries,
            final Object jobId)
            throws Exception {
        RecordVisitor visitor = new BridgeRecordVisitor(this, request, jobId,
                                    mainEntry, "latlonalt.csv") {
            public boolean doVisitRecord(RecordFile file,
                                         VisitInfo visitInfo, Record record)
                    throws Exception {
                if ( !jobOK(jobId)) {
                    return false;
                }
                StringBuffer buffer      = getBuffer(file);
                PointRecord  pointRecord = (PointRecord) record;
                float[]      altitudes   = pointRecord.getAltitudes();
                if (altitudes != null) {
                    for (int i = 0; i < altitudes.length; i++) {
                        buffer.append(pointRecord.getLatitude());
                        buffer.append(',');
                        buffer.append(pointRecord.getLongitude());
                        buffer.append(',');
                        buffer.append(altitudes[i]);
                        buffer.append("\n");
                    }
                } else {
                    ((PointRecord) record).printLatLonAltCsv(visitInfo,
                            buffer);
                }
                if (buffer.length() > 100000) {
                    write(buffer);
                }

                return true;
            }
            private void write(StringBuffer buffer) {
                synchronized (MUTEX) {
                    try {
                        byte[] bytes = buffer.toString().getBytes();
                        getTheOutputStream().write(bytes, 0, bytes.length);
                        buffer.setLength(0);
                    } catch (Exception exc) {
                        throw new RuntimeException(exc);
                    }
                }
            }

            public void finished(RecordFile file, VisitInfo visitInfo) {
                write(getBuffer(file));
                super.finished(file, visitInfo);
            }
        };

        return visitor;
    }





    /**
     * make the visitor that creates netcdf point files
     *
     * @param request The request
     * @param mainEntry Either the Point Collection or File Entry
     * @param pointEntries The entries to process
     * @param jobId The job ID
     *
     * @return the visitor
     *
     * @throws Exception On badness
     */
    public RecordVisitor makeNcVisitor(Request request, Entry mainEntry,
                                       List<?extends PointEntry> pointEntries,
                                       final Object jobId)
            throws Exception {

        final OutputStream outputStream = getOutputStream(request, jobId,
                                              mainEntry, ".nc");

        //      List<Attribute> globalAtts = new ArrayList<Attribute>();
        //      List<PointObVar> dataVars = new ArrayList<PointObVar>();
        //      final CFPointObWriter writer;

        RecordVisitor visitor = new BridgeRecordVisitor(this, jobId) {

            public boolean doVisitRecord(RecordFile file,
                                         VisitInfo visitInfo, Record record) {
                try {
                    if ( !jobOK(jobId)) {
                        return false;
                    }
                    GeoRecord geoRecord = (GeoRecord) record;
                    synchronized (MUTEX) {}

                    return true;
                } catch (Exception exc) {
                    throw new RuntimeException(exc);
                }
            }
            public void finished(RecordFile file, VisitInfo visitInfo) {
                super.finished(file, visitInfo);
                try {
                    //                      if(writer!=null) {

                    //  writer.finish();
                    //                      }
                    outputStream.close();
                } catch (Exception exc) {
                    throw new RuntimeException(exc);
                }
            }
        };

        return visitor;
    }


    /**
     * _more_
     *
     * @param request the request
     * @param mainEntry Either the Point Collection or File Entry
     * @param llg latlongrid
     * @param formats _more_
     * @param jobId The job ID
     *
     * @return _more_
     *
     * @throws Exception on badness
     */
    public Result outputEntryGrid(Request request, Entry mainEntry,
                                  IdwGrid llg, HashSet<String> formats,
                                  Object jobId)
            throws Exception {

        boolean doKmz          = formats.contains(OUTPUT_KMZ.getId());
        boolean doImage        = formats.contains(OUTPUT_IMAGE.getId());
        boolean doHillshade    = formats.contains(OUTPUT_HILLSHADE.getId());
        boolean doAsc          = formats.contains(OUTPUT_ASC.getId());
        boolean forceHillshade = false;

        if (doKmz && !doImage && !doHillshade) {
            forceHillshade = true;
        }

        Element         root   = null;
        Element         folder = null;
        String          desc   = mainEntry.getDescription();
        ZipOutputStream zos    = null;
        if (doKmz) {
            zos = new ZipOutputStream(getOutputStream(request, jobId,
                    mainEntry, ".kmz"));
            root   = KmlUtil.kml(mainEntry.getName());
            folder = KmlUtil.folder(root, mainEntry.getName(), true);
            if (desc.length() > 0) {
                KmlUtil.description(folder, desc);
            }

            /*            String trackUrl = request.getAbsoluteUrl(
                                  request.entryUrl(
                                      getRepository().URL_ENTRY_SHOW,
                                      mainEntry, new String[] { ARG_OUTPUT,
                    OUTPUT_KML_TRACK.toString() }));
            Element trackNode = KmlUtil.networkLink(folder, "Track",
                                    trackUrl);
            KmlUtil.open(trackNode, false);
            KmlUtil.visible(trackNode, false);
            */
        }


        int     imageWidth      = llg.getWidth();
        int     imageHeight     = llg.getHeight();


        boolean anyGridsDefined = false;
        for (int i = 0; i < GRID_ARGS.length; i++) {
            if (request.get(GRID_ARGS[i], false)) {
                anyGridsDefined = true;

                break;
            }
        }

        if ( !anyGridsDefined) {
            request.put(ARG_GRID_IDW, "true");
        }
        for (int i = 0; i < GRID_ARGS.length; i++) {
            String whatGrid = GRID_ARGS[i];
            if ( !request.get(whatGrid, false)) {
                continue;
            }
            double     missingValue    = Double.NaN;
            double[][] grid            = null;
            boolean    isAltitudeValue = true;
            if (whatGrid.equals(ARG_GRID_MIN)) {
                grid = llg.getMinGrid();
            } else if (whatGrid.equals(ARG_GRID_MAX)) {
                grid = llg.getMaxGrid();
            } else if (whatGrid.equals(ARG_GRID_AVERAGE)) {
                grid = llg.getValueGrid();
            } else if (whatGrid.equals(ARG_GRID_COUNT)) {
                isAltitudeValue = false;
                grid            = Misc.toDouble(llg.getCountGrid());
                //                missingValue = 0;
                missingValue = Double.NaN;
            } else if (whatGrid.equals(ARG_GRID_IDW)) {
                grid = llg.getWeightedValueGrid();
            }
            if (grid == null) {
                System.err.println("NLAS: No grid found for:" + whatGrid);

                continue;
            }

            String fileSuffix  = "." + whatGrid.replace(ARG_GRID_PREFIX, "");
            String imageSuffix = fileSuffix + ".png";
            String imageLabel  = GRID_LABELS[i];

            if (doAsc) {
                writeAsciiArcGrid(request, jobId, mainEntry, llg, grid,
                                  missingValue, fileSuffix + ".asc");
            }

            if (doImage) {
                File imageFile =
                    getRepository().getStorageManager().getTmpFile(request,
                        "pointimage.png");
                writeImage(request, imageFile, llg, grid, missingValue);
                InputStream imageInputStream =
                    getStorageManager().getFileInputStream(imageFile);
                OutputStream os = getOutputStream(request, jobId, mainEntry,
                                      imageSuffix);
                int bytes = IOUtil.writeTo(imageInputStream, os);
                IOUtil.close(os);
                IOUtil.close(imageInputStream);
                if (doKmz) {
                    String  imageFileName = imageSuffix;
                    Element groundOverlay = KmlUtil.groundOverlay(folder,
                                                imageLabel, desc,
                                                imageFileName,
                                                llg.getNorth(),
                                                llg.getSouth(),
                                                llg.getEast(), llg.getWest());
                    if (request.get(ARG_KML_VISIBLE, true)) {
                        KmlUtil.visible(groundOverlay, true);
                    }
                    imageInputStream =
                        getStorageManager().getFileInputStream(imageFile);
                    zos.putNextEntry(new ZipEntry(imageFileName));
                    IOUtil.writeTo(imageInputStream, zos);
                    IOUtil.close(imageInputStream);
                    zos.closeEntry();
                }
            }

            //Only do hillshade for altitude values
            if (isAltitudeValue && (doHillshade || forceHillshade)) {
                File imageFile =
                    getRepository().getStorageManager().getTmpFile(request,
                        "pointimage.png");
                LatLonGrid hillshadeGrid =
                    org.ramadda.util.grid.Gridder.doHillShade(llg, grid,
                        (float) request.get(ARG_HILLSHADE_AZIMUTH, 315.0f),
                        (float) request.get(ARG_HILLSHADE_ANGLE, 45.0f));
                String destFileName = "hillshade" + imageSuffix;
                if (forceHillshade) {
                    request.putExtraProperty(getOutputFilename(mainEntry,
                            destFileName), new Boolean(false));
                }
                writeImage(request, imageFile, hillshadeGrid,
                           hillshadeGrid.getValueGrid(), missingValue);
                InputStream imageInputStream =
                    getStorageManager().getFileInputStream(imageFile);
                OutputStream os = getOutputStream(request, jobId, mainEntry,
                                      destFileName);
                int bytes = IOUtil.writeTo(imageInputStream, os);
                IOUtil.close(os);
                IOUtil.close(imageInputStream);

                if (doKmz) {
                    String  imageFileName = "hillshade" + imageSuffix;
                    Element groundOverlay = KmlUtil.groundOverlay(folder,
                                                "Hill shaded  image "
                                                + imageLabel, desc,
                                                    imageFileName,
                                                    llg.getNorth(),
                                                    llg.getSouth(),
                                                    llg.getEast(),
                                                    llg.getWest());
                    if (request.get(ARG_KML_VISIBLE, true)) {
                        KmlUtil.visible(groundOverlay, true);
                    }
                    imageInputStream =
                        getStorageManager().getFileInputStream(imageFile);
                    zos.putNextEntry(new ZipEntry(imageFileName));
                    IOUtil.writeTo(imageInputStream, zos);
                    IOUtil.close(imageInputStream);
                    zos.closeEntry();
                }
            }
        }


        if (doKmz) {
            request.getHttpServletResponse().setContentType(
                "application/vnd.google-earth.kmz");
            zos.putNextEntry(new ZipEntry("points.kml"));
            String xml   = XmlUtil.toString(root);
            byte[] bytes = xml.getBytes();
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
            IOUtil.close(zos);
        }

        return getDummyResult();
    }




    /**
     * Gets called from the main RAMADDA map view to add extra marking to the map
     *
     * @param request The request
     * @param entry The entry
     * @param map The map
     */
    public void addToMap(Request request, Entry entry, MapInfo map) {
        try {
            //Don't include the tracks if it has  polygon metadata
            List<Metadata> metadataList =
                getMetadataManager().findMetadata(entry,
                    MetadataHandler.TYPE_SPATIAL_POLYGON, true);

            if ((metadataList == null) || (metadataList.size() > 0)) {
                return;
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
        getPointFormHandler().addToMap(request, entry, map);
    }



    /**
     * _more_
     *
     * @param request The request
     * @param jobId The job ID
     * @param mainEntry Either the  Collection or File Entry
     * @param llg latlongrid
     * @param grid _more_
     * @param missingValue _more_
     * @param fileSuffix _more_
     *
     * @throws Exception On badness
     */
    public void writeAsciiArcGrid(Request request, Object jobId,
                                  Entry mainEntry, IdwGrid llg,
                                  double[][] grid, double missingValue,
                                  String fileSuffix)
            throws Exception {
        boolean     haveMissingValue = !Double.isNaN(missingValue);
        final int   imageWidth       = llg.getWidth();
        final int   imageHeight      = llg.getHeight();
        PrintWriter pw = getPrintWriter(request, jobId, mainEntry,
                                        fileSuffix);
        pw.println("ncols " + imageWidth);
        pw.println("nrows " + imageHeight);
        pw.println("xllcorner " + llg.getWest());
        pw.println("yllcorner " + llg.getSouth());
        pw.println("cellsize "
                   + (llg.getEast() - llg.getWest()) / imageWidth);
        pw.println("nodata_value " + LatLonGrid.GRID_MISSING);
        System.err.println("NLAS: writing ARC ASCII grid " + imageWidth
                           + " X " + imageHeight);
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                double value = grid[y][x];
                if ((value != value) || (value == LatLonGrid.GRID_MISSING)
                        || (haveMissingValue && (value == missingValue))) {
                    value = LatLonGrid.GRID_MISSING;
                }
                pw.print(value);
                pw.print(" ");
            }
            pw.print("\n");
        }
        System.err.println("NLAS: done writing ARC ASCII grid ");
        pw.close();
    }


    /**
     * _more_
     *
     * @param request The request
     * @param imageFile _more_
     * @param llg latlongrid
     * @param grid _more_
     * @param missingValue _more_
     *
     * @throws Exception On badness
     */
    public void writeImage(Request request, File imageFile, LatLonGrid llg,
                           double[][] grid, double missingValue)
            throws Exception {
        int     imageWidth       = llg.getWidth();
        int     imageHeight      = llg.getHeight();

        boolean haveMissingValue = !Double.isNaN(missingValue);
        Color   defaultColor     = Color.CYAN;
        int[]   pixels           = new int[imageWidth * imageHeight];
        int     index            = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                pixels[index++] = ((0xff << 24)
                                   | (defaultColor.getRed() << 16)
                                   | (defaultColor.getRed() << 8)
                                   | defaultColor.getRed());
            }
        }


        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                double value = grid[y][x];
                if (haveMissingValue && (value == missingValue)) {
                    continue;
                }
                if (Double.isNaN(value)) {
                    continue;
                }
                max = Math.max(max, value);
                min = Math.min(min, value);
            }
        }

        int[][] colorTable =
            ColorTable.getColorTable(request.getString(ARG_COLORTABLE, ""));
        double[] range =
            ColorTable.getRange(request.getString(ARG_COLORTABLE, ""), min,
                                max);
        min   = range[0];
        max   = range[1];
        index = 0;
        double colorRange    = max - min;
        double colorRangeMin = min;
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                double value = grid[y][x];
                if (Double.isNaN(value) || (value == LatLonGrid.GRID_MISSING)
                        || (haveMissingValue && (value == missingValue))) {
                    //Set missing to transparent
                    pixels[index] = (0x00 << 24);
                } else {
                    //TODO: Check range for DBZ exception
                    double percent = (value - colorRangeMin) / colorRange;
                    pixels[index] = ColorTable.getPixelValue(colorTable,
                            percent);
                }
                index++;
            }
        }


        Image newImage = Toolkit.getDefaultToolkit().createImage(
                             new MemoryImageSource(
                                 imageWidth, imageHeight, pixels, 0,
                                 imageWidth));

        ImageUtils.writeImageToFile(newImage, imageFile);
    }


    /**
     * _more_
     *
     * @param request the request
     * @param mainEntry Either the  Collection or File Entry
     * @param entries entries to process
     * @param jobId The job ID
     *
     * @return _more_
     *
     * @throws Exception on badness
     */
    public Result outputEntryKmlTrack(Request request, Entry mainEntry,
                                      List<? extends PointEntry> entries,
                                      Object jobId)
            throws Exception {
        Element root      = KmlUtil.kml(mainEntry.getName() + " Tracks");
        Element topFolder = KmlUtil.folder(root,
                                           mainEntry.getName() + " Tracks",
                                           false);

        for (PointEntry pointEntry : entries) {
            Entry             entry    = pointEntry.getEntry();
            final int[]       pointCnt = { 0 };
            final float[][][] coords   = {
                new float[3][1000]
            };
            RecordVisitor     visitor  = new BridgeRecordVisitor(this) {
                public boolean doVisitRecord(RecordFile file,
                                             VisitInfo visitInfo,
                                             Record record) {
                    PointRecord pointRecord = (PointRecord) record;
                    float[][]   kmlCoords   = coords[0];
                    if (pointCnt[0] >= kmlCoords[0].length) {
                        kmlCoords = coords[0] = Misc.expand(kmlCoords);
                    }
                    kmlCoords[0][pointCnt[0]] =
                        (float) pointRecord.getLatitude();
                    kmlCoords[1][pointCnt[0]] =
                        (float) pointRecord.getLongitude();
                    kmlCoords[2][pointCnt[0]] =
                        (float) pointRecord.getAltitude();
                    pointCnt[0]++;

                    return true;
                }
                public void finished(RecordFile file, VisitInfo visitInfo) {
                    super.finished(file, visitInfo);
                }
            };
            long numRecords = pointEntry.getNumRecords();
            int  skip       = (int) (numRecords / 1000);
            getRecordJobManager().visitSequential(request, pointEntry,
                    visitor, new VisitInfo(true, skip));

            coords[0] = Misc.copy(coords[0], pointCnt[0]);
            Element folder = KmlUtil.folder(topFolder, entry.getName(),
                                            false);
            if (entry.getDescription().length() > 0) {
                KmlUtil.description(folder, entry.getDescription());
            }
            KmlUtil.placemark(folder, "Track", "", coords[0], Color.red, 2);
        }

        PrintWriter pw = getPrintWriter(request, jobId, mainEntry, ".kml");
        XmlUtil.toString(root, pw);
        pw.close();

        return getDummyResult();
    }

    /**
     * gets the bounds of the given entries
     *
     * @param request the request
     * @param entries _more_
     *
     * @return the bounds - north, west,south,east
     */
    public Rectangle2D.Double getBounds(Request request,
                                        List<? extends PointEntry> entries) {


        SelectionRectangle theBbox = TypeHandler.getSelectionBounds(request);
        theBbox.normalizeLongitude();

        //TODO: handle date line
        SelectionRectangle[] bboxes = theBbox.splitOnDateLine();
        double               north  = 0;
        double               south  = 0;
        double               east   = 0;
        double               west   = 0;
        SelectionRectangle   bbox   = bboxes[0];


        for (int i = 0; i < entries.size(); i++) {
            PointEntry pointEntry = entries.get(i);
            Entry      entry      = pointEntry.getEntry();
            double     tmpnorth   = bbox.getNorth(entry.getNorth());
            double     tmpsouth   = bbox.getSouth(entry.getSouth());
            double     tmpeast    = bbox.getEast(entry.getEast());
            double     tmpwest    = bbox.getWest(entry.getWest());
            if (i == 0) {
                north = tmpnorth;
                south = tmpsouth;
                east  = tmpeast;
                west  = tmpwest;
            } else {
                north = Math.max(tmpnorth, north);
                south = Math.min(tmpsouth, south);
                east  = Math.max(tmpeast, east);
                west  = Math.min(tmpwest, west);
            }

            /**
             * north = entry.getNorth();
             * south =  entry.getSouth();
             * east = entry.getEast();
             * west = entry.getWest();
             */

        }





        //TODO: is this right?
        if (Math.abs(north - south) > Math.abs(east - west)) {
            //            east = west + Math.abs(north - south);
        } else {
            //            north = south + Math.abs(east - west);
        }

        //        System.err.println("BOUNDS: lat:" + north +" " + south + " lon:" + west +" " + east);
        return new Rectangle2D.Double(west, north, east - west,
                                      south - north);
    }



    /**
     * _more_
     *
     * @param outputFile file to write to
     * @param pointFile file to read from
     *
     * @throws Exception On badness
     */
    public void writeBinaryFile(File outputFile, PointFile pointFile)
            throws Exception {
        DoubleLatLonAltBinaryFile.writeBinaryFile(pointFile,
                getStorageManager().getUncheckedFileOutputStream(outputFile),
                null);
    }



    /**
     * This handles the ajax request for the geolocation of an index in the given point file
     *
     * @param request the request
     * @param pointEntry The entry
     *
     * @return result
     *
     * @throws Exception on badness
     */
    public Result outputEntryGetLatLon(Request request, PointEntry pointEntry)
            throws Exception {
        long numRecords = pointEntry.getNumRecords();
        int  index      = (int) Math.min(numRecords - 1,
                                   request.get(ARG_POINTINDEX, 0));
        //Use the extra short binary file
        PointRecord record =
            (PointRecord) pointEntry.getBinaryPointFile().getRecord(index);
        StringBuffer sb = new StringBuffer("<result>");
        sb.append("{\"latitude\":" + record.getLatitude() + ",\"longitude\":"
                  + record.getLongitude() + "}");
        sb.append("</result>");
        Result result = new Result("", sb, "text/xml");

        return result;
    }




}
