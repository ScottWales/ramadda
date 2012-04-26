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

package org.ramadda.util;


import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import java.io.*;

import java.net.*;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import java.util.zip.*;


/**
 *
 */
public class SelectionRectangle {

    /** _more_          */
    private double north = Double.NaN;

    /** _more_          */
    private double west = Double.NaN;

    /** _more_          */
    private double south = Double.NaN;

    /** _more_          */
    private double east = Double.NaN;

    /**
     * _more_
     */
    public SelectionRectangle() {}


    /**
     * _more_
     *
     * @param north _more_
     * @param west _more_
     * @param south _more_
     * @param east _more_
     */
    public SelectionRectangle(double north, double west, double south,
                              double east) {
        this.north = north;
        this.west  = west;
        this.south = south;
        this.east  = east;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean crossesDateLine() {
        boolean haveLongitudeRange = !Double.isNaN(getWest())
                                     && !Double.isNaN(getEast());
        if (haveLongitudeRange && (getWest() > getEast())) {
            return true;
        }
        return false;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean allDefined() {
        return !Double.isNaN(north) && !Double.isNaN(west)
               && !Double.isNaN(south) && !Double.isNaN(east);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double[] getValues() {
        return new double[] { north, west, south, east };
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String[] getStringArray() {
        double[] values = getValues();
        String[] nwse   = { "", "", "", "" };
        for (int i = 0; i < nwse.length; i++) {
            if ( !Double.isNaN(values[i])) {
                nwse[i] = "" + values[i];
            }
        }
        return nwse;
    }

    /**
     * _more_
     */
    public void normalizeLongitude() {
        if ( !Double.isNaN(west)) {
            west = Misc.normalizeLongitude(west);
        }
        if ( !Double.isNaN(east)) {
            east = Misc.normalizeLongitude(east);
        }

    }

    /**
     * _more_
     *
     * @param idx _more_
     *
     * @return _more_
     */
    public double get(int idx) {
        if (idx == 0) {
            return north;
        }
        if (idx == 1) {
            return west;
        }
        if (idx == 2) {
            return south;
        }
        if (idx == 3) {
            return east;
        }
        throw new IllegalArgumentException("Bad index:" + idx);
    }


    /**
     * Set the North property.
     *
     * @param value The new value for North
     */
    public void setNorth(double value) {
        north = value;
    }

    public boolean hasNorth() {
        return !Double.isNaN(north);
    }


    public boolean hasSouth() {
        return !Double.isNaN(south);
    }

    public boolean hasWest() {
        return !Double.isNaN(west);
    }

    public boolean hasEast() {
        return !Double.isNaN(east);
    }




    /**
     * Get the North property.
     *
     * @return The North
     */
    public double getNorth() {
        return north;
    }

    /**
     * Set the West property.
     *
     * @param value The new value for West
     */
    public void setWest(double value) {
        west = value;
    }

    /**
     * Get the West property.
     *
     * @return The West
     */
    public double getWest() {
        return west;
    }

    /**
     * Set the South property.
     *
     * @param value The new value for South
     */
    public void setSouth(double value) {
        south = value;
    }

    /**
     * Get the South property.
     *
     * @return The South
     */
    public double getSouth() {
        return south;
    }

    /**
     * Set the East property.
     *
     * @param value The new value for East
     */
    public void setEast(double value) {
        east = value;
    }

    /**
     * Get the East property.
     *
     * @return The East
     */
    public double getEast() {
        return east;
    }



}
