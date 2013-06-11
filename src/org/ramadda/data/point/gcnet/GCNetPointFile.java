
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

    private static String header;


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


    @Override
    public boolean isHeaderBlankLineDelmited() {
        return true;
    }

    public String getStationsPath() {
        return "/org/ramadda/data/point/gcnet/stations.txt";
    }


    private String getHeader() throws IOException {
        if(header==null) {
            header = IOUtil.readContents("/org/ramadda/data/point/gcnet/header.txt", getClass()).trim();
            header = header.replaceAll("\n"," ");
        }
        return header;
    }

    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        super.prepareToVisit(visitInfo);
        putProperty(PROP_FIELDS, getHeader());
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
