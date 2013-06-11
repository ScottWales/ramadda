
package org.ramadda.data.point.noaa;



import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import org.ramadda.util.Station;

import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.List;



/**
 */

public  class NoaaPointFile extends CsvFile  {

    public static final String FIELD_NUMBER_OF_MEASUREMENTS = "number_of_measurements";
    public static final String FIELD_QC_FLAG = "qc_flag";
    public static final String FIELD_INTAKE_HEIGHT = "intake_height";
    public static final String FIELD_INSTRUMENT = "instrument";

    /**
     * ctor
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException On badness
     */
    public NoaaPointFile(String filename) throws IOException {
        super(filename);
    }


    public String getStationsPath() {
        return "/org/ramadda/data/point/noaa/stations.txt";
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
