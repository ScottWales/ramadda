package org.ramadda.data.record;



import org.ramadda.repository.*;
import org.ramadda.repository.job.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.output.*;


import org.ramadda.data.point.PointFile;



import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import ucar.unidata.geoloc.Bearing;
import ucar.unidata.geoloc.LatLonPointImpl;



import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.GuiUtils;
import org.ramadda.util.HtmlUtils;
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

 * @author         Jeff McWhirter
 */
public class PointFormHandler extends RecordFormHandler   {


    /**
     * ctor
     *
     * @param lidarOutputHandler output handler
     */
    public PointFormHandler(RecordOutputHandler recordOutputHandler) {
        super(recordOutputHandler);
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
     * create the polyline for the given lidarentry. This will cache it in the RAMADDA entry
     *
     * @param request the request
     * @param lidarEntry the Lidar Entry
     *
     * @return the map lines
     *
     * @throws Exception On badness
     */
    public List<double[]> getMapPolyline(Request request,
                                         RecordEntry recordEntry)
            throws Exception {
        long numRecords = recordEntry.getNumRecords();
        int skipFactor = (int) (numRecords
                                / request.get(ARG_NUMPOINTS, 1000));
        if (skipFactor == 0) {
            skipFactor = 1000;
        }



        String polylineProperty = "mapline" + skipFactor;

        List<double[]> polyLine =
            (List<double[]>) recordEntry.getEntry().getTransientProperty(
                polylineProperty);
        if (polyLine == null) {
            final List<double[]> pts         = new ArrayList<double[]>();
            final Bearing        workBearing = new Bearing();
            RecordVisitor visitor =
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

            getRecordJobManager().visitSequential(request, recordEntry, visitor,
                                            new VisitInfo(true, skipFactor));
            polyLine = pts;
            recordEntry.getEntry().putTransientProperty(polylineProperty,
                    polyLine);
        }
        return polyLine;
    }


}