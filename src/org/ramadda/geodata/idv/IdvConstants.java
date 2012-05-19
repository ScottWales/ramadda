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

package org.ramadda.geodata.idv;

import org.ramadda.repository.Constants;

import ucar.unidata.util.TwoFacedObject;

/**
 *
 *
 * @author Jeff McWhirter
 */
public interface IdvConstants {

    /** Visualization metadata type */
    public static final String METADATA_TYPE_VISUALIZATION =
        "data.visualization";

    /** product argument id */
    public static final String ARG_IDV_PRODUCT = "product";

    /** azimuth argument id */
    public static final String ARG_AZIMUTH = "azimuth";

    /** tilt argument id */
    public static final String ARG_TILT = "tilt";

    /** wireframe argument id */
    public static final String ARG_WIREFRAME = "wireframe";

    /** view direction argument id */
    public static final String ARG_VIEWDIR = "viewdir";

    /** lat/lon lines visible argument id */
    public static final String ARG_LATLON_VISIBLE = "latlon.visible";

    /** lat/lon lines spacing argument id */
    public static final String ARG_LATLON_SPACING = "latlon.spacing";

    /** Latitude 1 argument id */
    public static final String ARG_LAT1 = "lat1";

    /** Longitude 1 argument id */
    public static final String ARG_LON1 = "lon1";

    /** Latitude 2 argument id */
    public static final String ARG_LAT2 = "lat2";

    /** Longitude 2 argument id */
    public static final String ARG_LON2 = "lon2";

    /** submit/save argument id */
    public static final String ARG_SUBMIT_SAVE = "submit.save";

    /** save attach argument id */
    public static final String ARG_SAVE_ATTACH = "save.attach";

    /** Save name argument id */
    public static final String ARG_SAVE_NAME = "save.name";

    /** predefined argument id */
    public static final String ARG_PREDEFINED = "predefined";

    /** image product id */
    public static final String PRODUCT_IMAGE = "product.image";

    /** QuickTime movie product id */
    public static final String PRODUCT_MOV = "product.mov";

    /** KMZ product id */
    public static final String PRODUCT_KMZ = "product.kmz";

    /** GE Plugin product id */
    public static final String PRODUCT_GEPLUGIN = "product.geplugin";

    /** IDV product id */
    public static final String PRODUCT_IDV = "product.idv";

    /** ISL product id */
    public static final String PRODUCT_ISL = "product.isl";

    /** output products */
    public static TwoFacedObject[] products = { new TwoFacedObject("Image",
                                                   PRODUCT_IMAGE),
            new TwoFacedObject("Quicktime Movie", PRODUCT_MOV),
            new TwoFacedObject("Google Earth KMZ", PRODUCT_KMZ) };

    /** optional GE Plugin product */
    public static TwoFacedObject gePluginProduct =
        new TwoFacedObject("Google Earth Plugin", PRODUCT_GEPLUGIN);


    /** globe view argument id */
    public static final String ARG_VIEW_GLOBE = "globe";

    /** projection argument id */
    public static final String ARG_VIEW_PROJECTION = "proj";

    /** viewpoint argument id */
    public static final String ARG_VIEW_VIEWPOINT = "viewpoint";

    /** view bounds argument id */
    public static final String ARG_VIEW_BOUNDS = "bounds";

    /** "just clip" argument id */
    public static final String ARG_VIEW_JUSTCLIP = "justclip";

    /** background image argument id */
    public static final String ARG_VIEW_BACKGROUNDIMAGE = "backgroundimage";

    /** parameter argument id */
    public static final String ARG_PARAM = "param";

    /** target argument id */
    public static final String ARG_IDV_TARGET = "idv.target";

    /** target image id */
    public static final String TARGET_IMAGE = "image";

    /** target jnlp id */
    public static final String TARGET_JNLP = "jnlp";

    /** target isl id */
    public static final String TARGET_ISL = "isl";

    /** zoom argument id */
    public static final String ARG_ZOOM = "zoom";

    /** layoutmodel argument id */
    public static final String ARG_POINT_LAYOUTMODEL = "layoutmodel";

    /** animation argument id */
    public static final String ARG_POINT_DOANIMATION = "doanimation";

    /** display list label arg id */
    public static final String ARG_DISPLAYLISTLABEL = "dll";

    /** display list color arg id */
    public static final String ARG_DISPLAYCOLOR = "clr";

    /** colortable arg id */
    public static final String ARG_COLORTABLE = "ct";

    /** stride arg id */
    public static final String ARG_STRIDE = "stride";

    /** flow scale arg id */
    public static final String ARG_FLOW_SCALE = "f_s";

    /** flow density arg id */
    public static final String ARG_FLOW_DENSITY = "f_d";

    /** flow skip arg id */
    public static final String ARG_FLOW_SKIP = "f_sk";

    /** display unit arg id */
    public static final String ARG_DISPLAYUNIT = "unit";

    /** isosurface value arg id */
    public static final String ARG_ISOSURFACEVALUE = "iso_value";

    /** contour width arg id */
    public static final String ARG_CONTOUR_WIDTH = "c_w";

    /** contour min arg id */
    public static final String ARG_CONTOUR_MIN = "c_mn";

    /** contour max arg id */
    public static final String ARG_CONTOUR_MAX = "c_mx";

    /** contour interval arg id */
    public static final String ARG_CONTOUR_INTERVAL = "c_int";

    /** contour base arg id */
    public static final String ARG_CONTOUR_BASE = "c_b";

    /** contour dash arg id */
    public static final String ARG_CONTOUR_DASHED = "c_d";

    /** contour labels arg id */
    public static final String ARG_CONTOUR_LABELS = "c_l";

    /** scale visible arg id */
    public static final String ARG_SCALE_VISIBLE = "s_v";

    /** scale orientation arg id */
    public static final String ARG_SCALE_ORIENTATION = "s_o";

    /** scale placement arg id */
    public static final String ARG_SCALE_PLACEMENT = "s_p";

    /** _more_ */
    public static final String ARG_RANGE_MIN = "r_mn";

    /** _more_ */
    public static final String ARG_RANGE_MAX = "r_mx";

    /** _more_ */
    public static final String ARG_DISPLAY = "dsp";

    /** _more_ */
    public static final String ARG_IDV_ACTION = "idv.action";

    /** _more_ */
    public static final String ARG_TIMES = "times";

    /** _more_ */
    public static final String ARG_MAPS = "maps";

    /** _more_ */
    public static final String ARG_MAPWIDTH = "mapwidth";

    /** _more_ */
    public static final String ARG_MAPCOLOR = "mapcolor";


    /** _more_ */
    public static final String ARG_CLIP = "clip";

    /** _more_ */
    public static final String ARG_VIEW_BACKGROUND = "bg";

    /** _more_ */
    public static final String ARG_LEVELS = "levels";

    /** _more_ */
    public static final String ARG_IMAGE_WIDTH = "width";

    /** _more_ */
    public static final String ARG_IMAGE_HEIGHT = "height";

    /** _more_ */
    public static final String ARG_BACKGROUND_TRANSPARENT = "bgTrans";


    /** _more_ */
    public static final String[] NOTARGS = {
        ARG_SUBMIT_SAVE, Constants.ARG_SUBMIT_PUBLISH, Constants.ARG_PUBLISH_NAME,
        Constants.ARG_PUBLISH_ENTRY, Constants.ARG_PUBLISH_ENTRY + "_hidden",
        Constants.ARG_PUBLISH_DESCRIPTION, ARG_SAVE_ATTACH, ARG_SAVE_NAME, ARG_IDV_ACTION
    };
    /** _more_ */
    public static final String ACTION_ERROR = "action.error";


    /** _more_ */
    public static final String ACTION_MAKEINITFORM = "action.makeinitform";

    /** _more_ */
    public static final String ACTION_MAKEFORM = "action.makeform";


    /** _more_ */
    public static final String ACTION_MAKEPAGE = "action.makepage";

    /** _more_ */
    public static final String ACTION_MAKEIMAGE = "action.makeimage";


    /** _more_ */
    public static final String ACTION_POINT_MAKEPAGE =
        "action.point.makepage";

    /** _more_ */
    public static final String ACTION_POINT_MAKEIMAGE =
        "action.point.makeimage";



    /** _more_ */
    public static final String DISPLAY_XS_CONTOUR = "contourxs";

    /** _more_ */
    public static final String DISPLAY_XS_COLOR = "colorxs";

    /** _more_ */
    public static final String DISPLAY_XS_FILLEDCONTOUR = "contourxsfilled";



    /** _more_ */
    public static final String DISPLAY_PLANVIEWFLOW = "planviewflow";

    /** _more_ */
    public static final String DISPLAY_STREAMLINES = "streamlines";

    /** _more_ */
    public static final String DISPLAY_WINDBARBPLAN = "windbarbplan";

    /** _more_ */
    public static final String DISPLAY_PLANVIEWCONTOUR = "planviewcontour";

    /** _more_ */
    public static final String DISPLAY_PLANVIEWCONTOURFILLED =
        "planviewcontourfilled";

    /** _more_ */
    public static final String DISPLAY_PLANVIEWCOLOR = "planviewcolor";

    /** _more_ */
    public static final String DISPLAY_ISOSURFACE = "isosurface";


    /** _more_ */
    public static final String GROUP_DATA = "Data";


}
