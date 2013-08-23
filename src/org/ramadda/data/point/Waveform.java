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
package org.ramadda.data.point;


import org.ramadda.data.record.*;

import java.io.*;



/**
 */
public class Waveform {

    /** _more_          */
    private float[] waveform;

    /** _more_          */
    private float threshold = 0.0f;

    /** _more_          */
    private float[] range;

    /** _more_          */
    private double altitude0 = Float.NaN;

    /** _more_          */
    private double altitudeN;

    /** _more_          */
    private double latitude0;

    /** _more_          */
    private double longitude0;

    /** _more_          */
    private double latitudeN;

    /** _more_          */
    private double longitudeN;

    /**
     * _more_
     *
     * @param waveform _more_
     * @param range _more_
     * @param threshold _more_
     * @param altitude0 _more_
     * @param altitudeN _more_
     * @param latitude0 _more_
     * @param longitude0 _more_
     * @param latitudeN _more_
     * @param longitudeN _more_
     */
    public Waveform(float[] waveform, float[] range, float threshold,
                    double altitude0, double altitudeN, double latitude0,
                    double longitude0, double latitudeN, double longitudeN) {
        this.waveform   = waveform;
        this.range      = range;
        this.threshold  = threshold;
        this.altitude0  = altitude0;
        this.altitudeN  = altitudeN;
        this.latitude0  = latitude0;
        this.longitude0 = longitude0;
        this.latitudeN  = latitudeN;
        this.longitudeN = longitudeN;
    }

    /**
     * _more_
     *
     * @param waveform _more_
     */
    public Waveform(float[] waveform) {
        this.waveform = waveform;
        range         = new float[] { waveform[0], waveform[0] };
        for (int i = 0; i < waveform.length; i++) {
            range[0] = Math.min(range[0], waveform[i]);
            range[1] = Math.max(range[1], waveform[i]);
        }
    }

    /**
     * _more_
     *
     * @param waveform _more_
     * @param range _more_
     * @param threshold _more_
     */
    public Waveform(float[] waveform, float[] range, float threshold) {
        this.waveform  = waveform;
        this.range     = range;
        this.threshold = threshold;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasAltitude() {
        return altitude0 == altitude0;
    }

    /**
     *  Set the Waveform property.
     *
     *  @param value The new value for Waveform
     */
    public void setWaveform(float[] value) {
        waveform = value;
    }

    /**
     *  Get the Waveform property.
     *
     *  @return The Waveform
     */
    public float[] getWaveform() {
        return waveform;
    }

    /**
     *  Get the Waveform property.
     *
     *  @return The Waveform
     */
    public float[] getRange() {
        return range;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public float getThreshold() {
        return threshold;
    }

    /**
     *  Set the Altitude0 property.
     *
     *  @param value The new value for Altitude0
     */
    public void setAltitude0(double value) {
        altitude0 = value;
    }

    /**
     *  Get the Altitude0 property.
     *
     *  @return The Altitude0
     */
    public double getAltitude0() {
        return altitude0;
    }

    /**
     *  Set the AltitudeN property.
     *
     *  @param value The new value for AltitudeN
     */
    public void setAltitudeN(double value) {
        altitudeN = value;
    }

    /**
     *  Get the AltitudeN property.
     *
     *  @return The AltitudeN
     */
    public double getAltitudeN() {
        return altitudeN;
    }

    /**
     *  Set the Latitude0 property.
     *
     *  @param value The new value for Latitude0
     */
    public void setLatitude0(double value) {
        latitude0 = value;
    }

    /**
     *  Get the Latitude0 property.
     *
     *  @return The Latitude0
     */
    public double getLatitude0() {
        return latitude0;
    }

    /**
     *  Set the Longitude0 property.
     *
     *  @param value The new value for Longitude0
     */
    public void setLongitude0(double value) {
        longitude0 = value;
    }

    /**
     *  Get the Longitude0 property.
     *
     *  @return The Longitude0
     */
    public double getLongitude0() {
        return longitude0;
    }

    /**
     *  Set the LatitudeN property.
     *
     *  @param value The new value for LatitudeN
     */
    public void setLatitudeN(double value) {
        latitudeN = value;
    }

    /**
     *  Get the LatitudeN property.
     *
     *  @return The LatitudeN
     */
    public double getLatitudeN() {
        return latitudeN;
    }

    /**
     *  Set the LongitudeN property.
     *
     *  @param value The new value for LongitudeN
     */
    public void setLongitudeN(double value) {
        longitudeN = value;
    }

    /**
     *  Get the LongitudeN property.
     *
     *  @return The LongitudeN
     */
    public double getLongitudeN() {
        return longitudeN;
    }

}
