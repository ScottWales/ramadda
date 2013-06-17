/*
 * Copyright 2010 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
 * http://www.unavco.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */
package org.ramadda.data.point.text;




import org.ramadda.data.record.*;
import org.ramadda.data.point.*;

import org.ramadda.util.Station;
import org.ramadda.util.XlsUtil;
import org.ramadda.util.HtmlUtils;
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

    public static final String FIELD_SITE_ID = "Site_Id";
    public static final String FIELD_LATITUDE ="Latitude";
    public static final String FIELD_LONGITUDE  = "Longitude";
    public static final String FIELD_ELEVATION  = "Elevation";
    public static final String FIELD_DEPTH  = "Depth";
    public static final String FIELD_DATE = "Date";
    public static final String FIELD_TIME = "Time";
    public static final String FIELD_YEAR = "Year";
    public static final String FIELD_MONTH = "Month";
    public static final String FIELD_DAY = "Day";
    public static final String FIELD_JULIAN_DAY = "Julian_Day";
    public static final String FIELD_HOUR = "Hour";
    public static final String FIELD_MINUTE = "Minute";
    public static final String FIELD_SECOND = "Second";
    public static final String FIELD_STANDARD_DEVIATION = "Standard_Deviation";

    public static final String FIELD_NORTH = "North";
    public static final String FIELD_EAST = "East";
    public static final String FIELD_VERTICAL = "Vertical";
    public static final String FIELD_NORTH_STD_DEVIATION = "North_Std_Deviation";
    public static final String FIELD_EAST_STD_DEVIATION = "East_Std_Deviation";
    public static final String FIELD_VERTICAL_STD_DEVIATION = "East_Vertical_Deviation";

    public static final String FIELD_QUALITY = "Quality";


    public static final String FIELD_TEMPERATURE = "Temperature";
    public static final String FIELD_POTENTIAL_TEMPERATURE = "Potential_Temperature";
    public static final String FIELD_PRESSURE = "Pressure";
    public static final String FIELD_WIND_SPEED = "Wind_Speed";
    public static final String FIELD_WIND_DIRECTION = "Wind_Direction";
    public static final String FIELD_RELATIVE_HUMIDITY = "Relative_Humidity";
    public static final String FIELD_DELTA_T = "Delta_T";

    public static final String FIELD_CONDUCTIVITY = "Conductivity";

    public static final String FIELD_SALINITY = "Salinity";
    public static final String FIELD_SIGMA = "Sigma";



    public static final String UNIT_CELSIUS = "Celsius";
    public static final String UNIT_HPA = "hPa";
    public static final String UNIT_PERCENT = "%";
    public static final String UNIT_DEGREES = "degrees";
    public static final String UNIT_METERS = "m";
    public static final String UNIT_M_S = "m/s";
    public static final String UNIT_ = "";


}
