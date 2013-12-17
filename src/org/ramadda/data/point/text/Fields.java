/*
* Copyright 2008-2013 Geode Systems LLC
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

package org.ramadda.data.point.text;


import org.ramadda.data.point.*;




import org.ramadda.data.record.*;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.Station;
import org.ramadda.util.XlsUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;

import java.awt.*;
import java.awt.image.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.*;


/**
 *
 */
public interface Fields {

    /** _more_ */
    public static final String FIELD_SITE_ID = "Site_Id";

    public static final String FIELD_STATION = "Station";

    /** _more_ */
    public static final String FIELD_LATITUDE = "Latitude";

    /** _more_ */
    public static final String FIELD_LONGITUDE = "Longitude";

    /** _more_ */
    public static final String FIELD_ELEVATION = "Elevation";

    /** _more_ */
    public static final String FIELD_DEPTH = "Depth";

    /** _more_ */
    public static final String FIELD_DATE = "Date";

    /** _more_ */
    public static final String FIELD_TIME = "Time";

    /** _more_ */
    public static final String FIELD_YEAR = "Year";

    /** _more_ */
    public static final String FIELD_MONTH = "Month";

    /** _more_ */
    public static final String FIELD_DAY = "Day";

    /** _more_ */
    public static final String FIELD_JULIAN_DAY = "Julian_Day";

    /** _more_ */
    public static final String FIELD_HOUR = "Hour";

    /** _more_ */
    public static final String FIELD_MINUTE = "Minute";

    /** _more_ */
    public static final String FIELD_SECOND = "Second";

    /** _more_ */
    public static final String FIELD_STANDARD_DEVIATION =
        "Standard_Deviation";

    /** _more_ */
    public static final String FIELD_NORTH = "North";

    /** _more_ */
    public static final String FIELD_EAST = "East";

    /** _more_ */
    public static final String FIELD_VERTICAL = "Vertical";

    /** _more_ */
    public static final String FIELD_NORTH_STD_DEVIATION =
        "North_Std_Deviation";

    /** _more_ */
    public static final String FIELD_EAST_STD_DEVIATION =
        "East_Std_Deviation";

    /** _more_ */
    public static final String FIELD_VERTICAL_STD_DEVIATION =
        "East_Vertical_Deviation";

    /** _more_ */
    public static final String FIELD_QUALITY = "Quality";


    /** _more_ */
    public static final String FIELD_TEMPERATURE = "Temperature";

    /** _more_ */
    public static final String FIELD_POTENTIAL_TEMPERATURE =
        "Potential_Temperature";

    /** _more_ */
    public static final String FIELD_PRESSURE = "Pressure";

    /** _more_ */
    public static final String FIELD_WIND_SPEED = "Wind_Speed";

    /** _more_ */
    public static final String FIELD_WIND_DIRECTION = "Wind_Direction";

    /** _more_ */
    public static final String FIELD_RELATIVE_HUMIDITY = "Relative_Humidity";

    /** _more_ */
    public static final String FIELD_DELTA_T = "Delta_T";

    /** _more_ */
    public static final String FIELD_CONDUCTIVITY = "Conductivity";

    /** _more_ */
    public static final String FIELD_SALINITY = "Salinity";

    /** _more_ */
    public static final String FIELD_SIGMA = "Sigma";



    /** _more_ */
    public static final String UNIT_CELSIUS = "Celsius";

    /** _more_ */
    public static final String UNIT_HPA = "hPa";

    /** _more_ */
    public static final String UNIT_PERCENT = "%";

    /** _more_ */
    public static final String UNIT_DEGREES = "degrees";

    /** _more_ */
    public static final String UNIT_METERS = "m";

    /** _more_ */
    public static final String UNIT_M_S = "m/s";

    /** _more_ */
    public static final String UNIT_ = "";


}
