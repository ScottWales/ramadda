
package org.ramadda.data.point.gcnet;


import org.ramadda.util.Station;
import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;


import ucar.unidata.util.IOUtil;

import java.io.*;

import java.util.Date;

/**
 */

public  class GCNetPointFile extends CsvFile  {


    /**
     * ctor
     *
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException On badness
     */
    public GCNetPointFile(String filename) throws IOException {
        super(filename);
    }


    public String getStationsPath() {
        return "/org/ramadda/data/point/gcnet/stations.txt";
    }



    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        //Put the delimiter first so we can read the header in the parent method
        putProperty(PROP_HEADER_DELIMITER, "");
        super.prepareToVisit(visitInfo);
        putProperty(PROP_FIELDS, getFieldsFileContents());
        return visitInfo;
    }


    /*
     * This gets called after a record has been read
     * It extracts and creates the record date/time
     */
    public boolean processAfterReading(VisitInfo visitInfo, Record record) throws Exception {
        if(!super.processAfterReading(visitInfo, record)) return false;
        TextRecord textRecord = (TextRecord) record;
        int site = (int)textRecord.getValue(1);
        setLocation(""+site, textRecord);

        int year = (int)textRecord.getValue(5);
        double julianDay = textRecord.getValue(6);
        record.setRecordTime(getDateFromJulianDay(year, julianDay).getTime());
        return true;
    }


    /*
     * Get the delimiter (space)
     *  @return the column delimiter
     */
    public String getDelimiter() {
        return " ";
    }


    public static void main(String[]args) {
        PointFile.test(args, GCNetPointFile.class);
    }




}
