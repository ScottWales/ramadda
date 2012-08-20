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

package org.ramadda.plugins.map;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;


/**
 */
public class GpxUtil {

    /** _more_          */
    public static final String TAG_GPX = "gpx";

    /** _more_          */
    public static final String TAG_METADATA = "metadata";

    /** _more_          */
    public static final String TAG_LINK = "link";

    /** _more_          */
    public static final String TAG_TEXT = "text";

    /** _more_          */
    public static final String TAG_BOUNDS = "bounds";

    /** _more_          */
    public static final String TAG_EXTENSIONS = "extensions";

    /** _more_          */
    public static final String TAG_TIME = "time";

    /** _more_          */
    public static final String TAG_WPT = "wpt";

    /** _more_          */
    public static final String TAG_ELE = "ele";

    /** _more_          */
    public static final String TAG_NAME = "name";

    /** _more_          */
    public static final String TAG_CMT = "cmt";

    /** _more_          */
    public static final String TAG_DESC = "desc";

    /** _more_          */
    public static final String TAG_SYM = "sym";

    /** _more_          */
    public static final String TAG_LABEL = "label";

    /** _more_          */
    public static final String TAG_LABEL_TEXT = "label_text";

    /** _more_          */
    public static final String TAG_AUTHOR = "author";

    /** _more_          */
    public static final String TAG_EMAIL = "email";

    /** _more_          */
    public static final String TAG_URL = "url";

    /** _more_          */
    public static final String TAG_URLNAME = "urlname";

    /** _more_          */
    public static final String TAG_KEYWORDS = "keywords";


    /** _more_          */
    public static final String TAG_TYPE = "type";

    /** _more_          */
    public static final String TAG_TRK = "trk";

    /** _more_          */
    public static final String TAG_NUMBER = "number";

    /** _more_          */
    public static final String TAG_TRKSEG = "trkseg";

    /** _more_          */
    public static final String TAG_TRKPT = "trkpt";



    /** _more_          */
    public static final String ATTR_CREATOR = "creator";

    /** _more_          */
    public static final String ATTR_VERSION = "version";

    /** _more_          */
    public static final String ATTR_HREF = "href";

    /** _more_          */
    public static final String ATTR_MAXLAT = "maxlat";

    /** _more_          */
    public static final String ATTR_MAXLON = "maxlon";

    /** _more_          */
    public static final String ATTR_MINLAT = "minlat";

    /** _more_          */
    public static final String ATTR_MINLON = "minlon";

    /** _more_          */
    public static final String ATTR_LAT = "lat";

    /** _more_          */
    public static final String ATTR_LON = "lon";


}
