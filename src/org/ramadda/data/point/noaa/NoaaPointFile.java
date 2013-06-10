
package org.ramadda.data.point.noaa;


import java.text.SimpleDateFormat;


import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import org.ramadda.util.Station;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;



/**
 */

public  class NoaaPointFile extends CsvFile  {

    private static Hashtable<String,Station> stations;


    /**
     * ctor
     */
    public NoaaPointFile() {
    }

    /**
     * ctor
     *
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException On badness
     */
    public NoaaPointFile(String filename) throws IOException {
        super(filename);
    }


   /**
     * ctor
     *
     * @param filename filename
     * @param properties properties
     *
     * @throws IOException On badness
     */
    public NoaaPointFile(String filename,
                               Hashtable properties)
        throws IOException {
        super(filename, properties);
    }


    public Hashtable<String,Station> getStationMap() {
        if(stations == null) {
            stations = readStations("/org/ramadda/data/point/noaa/stations.txt");
        }
        return stations;
    }



    /**
     * This is used by RAMADDA to determine what kind of services are available for this type of point data
     * @return is this file capable of the action
     */
    public boolean isCapable(String action) {
        if(action.equals(ACTION_BOUNDINGPOLYGON)) return false;
        if(action.equals(ACTION_GRID)) return false;
        return super.isCapable(action);
    }


    /*
     * Get the delimiter (space)
     *      @return the column delimiter
     */
    public String getDelimiter() {
        return " ";
    }


    /**
     * There are  2 header lines
     *
     * @param visitInfo file visit info
     *
     * @return how many lines to skip
     */
    public int getSkipLines(VisitInfo visitInfo) {
        return 0;
    }

}
