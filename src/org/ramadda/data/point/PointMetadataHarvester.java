
package org.ramadda.data.point;


import org.ramadda.data.record.*;
import org.ramadda.util.grid.LatLonGrid;

import java.io.*;
import java.util.Date;
import java.util.Properties;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public class PointMetadataHarvester extends RecordVisitor {

    /** _more_ */
    private int cnt = 0;

    private double minElevation = Double.NaN;
    private double maxElevation = Double.NaN;

    /** _more_ */
    private double minLatitude = Double.NaN;

    /** _more_ */
    private double maxLatitude = Double.NaN;

    /** _more_ */
    private double minLongitude = Double.NaN;

    /** _more_ */
    private double maxLongitude = Double.NaN;

    /** _more_          */
    private long minTime = Long.MAX_VALUE;

    /** _more_          */
    private long maxTime = Long.MIN_VALUE;

    /** _more_ */
    private LatLonGrid llg;

    private Properties properties;

    /**
     * _more_
     */
    public PointMetadataHarvester() {}


    /**
     * _more_
     *
     * @param llg _more_
     */
    public PointMetadataHarvester(LatLonGrid llg) {
        this.llg = llg;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasTimeRange() {
        return minTime != Long.MAX_VALUE;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public long getMinTime() {
        return minTime;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public long getMaxTime() {
        return maxTime;
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param visitInfo _more_
     * @param record _more_
     *
     * @return _more_
     */
    public boolean visitRecord(RecordFile file, VisitInfo visitInfo,
                               Record record) {
        PointRecord pointRecord = (PointRecord) record;
        double      lat         = pointRecord.getLatitude();
        double      lon         = pointRecord.getLongitude();

        //Skip this if it doesn't have a valid position
        if ( !pointRecord.isValidPosition()) {
            System.err.println("  not valid position ");
            return true;
        }

        cnt++;
        if (llg != null) {
            llg.incrementCount(lat, lon);
        }

        if (pointRecord.hasRecordTime()) {
            long time = pointRecord.getRecordTime();
            minTime = Math.min(minTime, time);
            maxTime = Math.max(maxTime, time);
        }

        minLatitude  = getMin(minLatitude, pointRecord.getLatitude());
        maxLatitude  = getMax(maxLatitude, pointRecord.getLatitude());
        minLongitude = getMin(minLongitude, pointRecord.getLongitude());
        maxLongitude = getMax(maxLongitude, pointRecord.getLongitude());
	minElevation  = getMin(minElevation, pointRecord.getAltitude());
	maxElevation  = getMax(maxElevation, pointRecord.getAltitude());
        return true;
    }

    

    private double getMin(double value1, double value2) {
        if(Double.isNaN(value1)) return value2;
        if(Double.isNaN(value2)) return value1;
        return Math.min(value1, value2);
    }

    private double getMax(double value1, double value2) {
        if(Double.isNaN(value1)) return value2;
        if(Double.isNaN(value2)) return value1;
        return Math.max(value1, value2);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getCount() {
        return cnt;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return "latitude:" + minLatitude + " - " + maxLatitude
            + "  longitude:" + minLongitude + " - " + maxLongitude +" has time:" + hasTimeRange() + " " + new Date(getMinTime());
    }




    /**
     * Get the MinLatitude property.
     *
     * @return The MinLatitude
     */
    public double getMinLatitude() {
        return this.minLatitude;
    }



    /**
     * Get the MaxLatitude property.
     *
     * @return The MaxLatitude
     */
    public double getMaxLatitude() {
        return this.maxLatitude;
    }


    /**
     * Get the MinLongitude property.
     *
     * @return The MinLongitude
     */
    public double getMinLongitude() {
        return this.minLongitude;
    }


    /**
     * Get the MaxLongitude property.
     *
     * @return The MaxLongitude
     */
    public double getMaxLongitude() {
        return this.maxLongitude;
    }

    public double getMinElevation() {
	return this.minElevation;
    }


    public double getMaxElevation() {
	return this.maxElevation;
    }


    /**
       Get the Properties property.

       @return The Properties
    **/
    public Properties getProperties () {
	return properties;
    }



}
