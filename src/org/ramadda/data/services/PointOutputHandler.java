
package org.ramadda.data.services;

import org.ramadda.util.grid.*;

import org.ramadda.data.record.*;
import org.ramadda.data.point.*;

import org.ramadda.repository.*;
import org.ramadda.repository.job.*;
import org.ramadda.repository.auth.*;
import org.ramadda.util.SelectionRectangle;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.TypeHandler;

import org.ramadda.util.TempDir;


import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;

import org.w3c.dom.*;

import ucar.unidata.data.gis.KmlUtil;


import ucar.unidata.ui.ImageUtils;
import org.ramadda.util.HtmlUtils;
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

    public static final String ARG_FILLMISSING = "fillmissing";


    /**
     * constructor. This gets called by the Repository via reflection
     * This class is specified in outputhandlers.xml
     *
     *
     * @param repository the repository
     * @param element the xml from outputhandlers.xml
     * @throws Exception on badness
     */
    public PointOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
    }


/**
Get the FormHandler property.

@return The FormHandler
**/
public PointFormHandler getPointFormHandler () {
    return (PointFormHandler) super.getFormHandler();
}



    /**
     * Gets the approximate point count of the given lidar files. It figures out
     * the  area  of the of the clipping box intersection with each file.
     *
     * @param request request
     * @param subsetLidarEntries entries
     *
     * @return approximate point count in spatial subset
     *
     * @throws Exception On badness
     */
    public long getApproximatePointCount(Request request,
                                          List<?extends RecordEntry> subsetLidarEntries)
            throws Exception {
        long pointCount = 0;
        storeSession(request);
        double north = request.get(ARG_AREA_NORTH, 90.0);
        double south = request.get(ARG_AREA_SOUTH, -90.0);
        double east  = request.get(ARG_AREA_EAST, 180.0);
        double west  = request.get(ARG_AREA_WEST, -180.0);

        Rectangle2D.Double queryRect = new Rectangle2D.Double(west, south,
                                           east - west, north - south);
        for (RecordEntry lidarEntry : subsetLidarEntries) {
            Rectangle2D.Double entryBounds =
                lidarEntry.getEntry().getBounds();
            Rectangle2D intersection =
                entryBounds.createIntersection(queryRect);
            double percent =
                (intersection.getWidth() * intersection.getHeight())
                / (entryBounds.getWidth() * entryBounds.getHeight());
            pointCount += (long) (percent * lidarEntry.getNumRecords());
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
    public List<RecordEntry> doSubsetEntries(Request request,
                                             List<?extends RecordEntry> recordEntries)
            throws Exception {
        List<RecordEntry>result = new ArrayList<RecordEntry>();

        SelectionRectangle theBbox = TypeHandler.getSelectionBounds(request);
        if (!theBbox.anyDefined()) {
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
            for(SelectionRectangle bbox: bboxes) {
                Rectangle2D.Double queryRect = new Rectangle2D.Double(bbox.getWest(-180), bbox.getSouth(-90),
                                                                      bbox.getEast(180) - bbox.getWest(-180), 
                                                                      bbox.getNorth(90) - bbox.getSouth(-90));
                Rectangle2D.Double entryRect =
                    new Rectangle2D.Double(entry.getWest(), entry.getSouth(),
                                           entry.getEast() - entry.getWest(),
                                           entry.getNorth() - entry.getSouth());
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
     * _more_
     *
     * @param request The request
     * @param lidarEntries The entries to process
     * @param bounds _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public GridVisitor makeGridVisitor(Request request,
                                       List<? extends PointEntry> recordEntries,
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
                llg.setRadius(getFormHandler().getDefaultRadiusDegrees(request,
                        bounds));
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




}
