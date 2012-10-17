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


import org.ramadda.repository.Constants;
import org.ramadda.repository.output.*;

import java.awt.Color;



/**
 */
public interface RecordConstants extends Constants {

    /** bbox index */
    public static final int IDX_NORTH = 0;

    /** bbox index */
    public static final int IDX_SOUTH = 1;

    /** bbox index */
    public static final int IDX_EAST = 2;

    /** bbox index */
    public static final int IDX_WEST = 3;

    /** url arg */
    public static final String ARG_ASYNCH = "asynch";




    /** _more_ */
    public static final String ARG_GEOREFERENCE = "georeference";

    /** _more_ */
    public static final String ARG_FIELD_USE = "field_use";

    /** _more_ */
    public static final String ARG_TRACKS = "tracks";

    /** _more_ */
    public static final String ARG_BITFIELD = "bitfield";

    /** _more_ */
    public static final String ARG_CSV = "true";

    /** _more_ */
    public static final String ARG_ALL = "all";

    /** url argument */
    public static final String ARG_GETDATA = "getdata";

    /** _more_ */
    public static final String ARG_INCLUDEWAVEFORM = "includewaveform";


    /** url argument */
    public static final String ARG_GRID_PREFIX = "grid.";

    /** _more_ */
    public static final String ARG_FILLMISSING = "fillmissing";

    /** url argument */
    public static final String ARG_GRID_MIN = ARG_GRID_PREFIX + "min";

    /** url argument */
    public static final String ARG_GRID_MAX = ARG_GRID_PREFIX + "max";

    /** url argument */
    public static final String ARG_GRID_AVERAGE = ARG_GRID_PREFIX + "average";

    /** url argument */
    public static final String ARG_GRID_COUNT = ARG_GRID_PREFIX + "count";

    /** url argument */
    public static final String ARG_GRID_IDW = ARG_GRID_PREFIX + "idw";



    /** url argument */
    public static final String ARG_GRID_RADIUS_DEGREES = ARG_GRID_PREFIX
                                                         + "radius.degrees";

    /** _more_ */
    public static final String ARG_GRID_RADIUS_DEGREES_ORIG =
        ARG_GRID_RADIUS_DEGREES + ".orig";

    /** url argument */
    public static final String ARG_GRID_RADIUS_CELLS = ARG_GRID_PREFIX
                                                       + "radius.cells";

    /** _more_ */
    public static final String ARG_REQUEST_CLIENT = "request.client";

    /** _more_ */
    public static final String ARG_REQUEST_DOMAIN = "request.domain";

    /** _more_ */
    public static final String ARG_REQUEST_EMAIL = "request.email";

    /** _more_ */
    public static final String ARG_REQUEST_USER = "request.user";

    /** _more_ */
    public static final String ARG_REQUEST_IP = "request.ip";


    /** url argument */
    public static final String ARG_JOB_EMAIL = "job.email";

    /** url argument */
    public static final String ARG_JOB_ID = "job.id";

    /** url argument */
    public static final String ARG_JOB_NAME = "job.name";

    /** url argument */
    public static final String ARG_JOB_DESCRIPTION = "job.description";

    /** url argument */
    public static final String ARG_JOB_USER = "job.user";

    /** url arg */
    public static final String ARG_RECORDENTRY_CHECK = "recordrentrycheck";

    /** url arg */
    public static final String ARG_RECORDENTRY = "recordentry";



    /** url arg */
    public static final String ARG_CHART_WAVEFORM_COLORTABLE =
        "waveform.colortable";

    /** url arg */
    public static final String ARG_PROBABILITY = "probability";

    /** url argument */
    public static final String ARG_POINTCOUNT = "pointcount";


    /** url arg */
    public static final String ARG_SEARCH_PREFIX = "search_";



    /** url arg */
    public static final String ARG_AXIS_LOW = "axis.low";

    /** url arg */
    public static final String ARG_AXIS_HIGH = "axis.high";

    /** url arg */
    public static final String ARG_COLORTABLE = "colortable";

    /** url arg */
    public static final String ARG_CHART_SHOW = "chart.show.";

    /** url arg */
    public static final String ARG_CHART_COLOR = "chart.color.";


    /** url arg */
    public static final String ARG_SHOWURL = "showurl";

    /** url arg */
    public static final String ARG_KML_VISIBLE = "visible";


    /** url arg */
    public static final String ARG_HILLSHADE = "hillshade";

    /** url arg */
    public static final String ARG_HILLSHADE_AZIMUTH = "hillshade.azimuth";

    /** url arg */
    public static final String ARG_HILLSHADE_ANGLE = "hillshade.angle";

    /** url arg */
    public static final String ARG_WAVEFORM_DISPLAY = "waveform.display";

    /** url arg */
    public static final String ARG_WAVEFORM_AXIS_LOW = "waveform.axis.low";

    /** url arg */
    public static final String ARG_WAVEFORM_AXIS_HIGH = "waveform.axis.high";

    /** url arg */
    public static final String ARG_WAVEFORM_NAME = "waveform.name";


    /** url arg */
    public static final String ARG_MAP_SHOW = "map.show";

    /** url arg */
    public static final String ARG_POINTINDEX = "pointindex";





    /** defines the different gridding functions the user can choose */
    public static final String[] GRID_ARGS = { ARG_GRID_MIN, ARG_GRID_MAX,
            ARG_GRID_AVERAGE, ARG_GRID_COUNT, ARG_GRID_IDW, };

    /** corresponds toe the GRID_ARGS */
    public static final String[] GRID_HELP = { "Each grid cell holds the minimum value of all points within the cell",
            "Each grid cell holds the maximum value of all points within the cell",
            "Each grid cell holds the average value of all points within the cell",
            "Each grid cell holds the number of points within the cell",
            "Each grid cell Inverse Distance Weighted average of the point values within the radius around the cell", };


    /** corresponds toe the GRID_ARGS */
    public static final String[] GRID_LABELS = { "Minimum value",
            "Maximum value", "Average value", "Point count", "IDW average", };



    /** _more_ */
    public static final int DFLT_WIDTH = 1000;

    /** _more_ */
    public static final int DFLT_HEIGHT = 1000;

    /** constants */
    public static final int TIMESERIES_POINTS = 500;

    /** constants */
    public static final int WAVEFORM_THRESHOLD = 25;

    /** constants */
    public static final int TIMESERIES_HEIGHT = 400;

    /** constants */
    public static final int TIMESERIES_WIDTH = 800;

    /** _more_ */
    public static final int TIMESERIES_LEFT_WIDTH = 0;

    /** _more_ */
    public static final int TIMESERIES_AXIS_WIDTHPER = 75;


}
