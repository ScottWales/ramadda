
package org.ramadda.data.point.gcnet;


import java.text.SimpleDateFormat;

import java.util.GregorianCalendar;

import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;



/**
 */

public  class GCNetPointFile extends CsvFile  {

    private static String header;
    private SimpleDateFormat sdf;

    /**
     * ctor
     */
    public GCNetPointFile() {
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
    public GCNetPointFile(String filename) throws IOException {
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
    public GCNetPointFile(String filename,
                               Hashtable properties)
        throws IOException {
        super(filename, properties);
    }


@Override
    public boolean isHeaderNewLineDelmited() {
        return true;
    }

    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        super.prepareToVisit(visitInfo);
        if(header== null) {
            header = IOUtil.readContents("/org/ramadda/data/point/gcnet/header.txt", getClass()).trim();
            header = header.replaceAll("\n",",");
        }
        sdf = makeDateFormat("yyyy-MM-dd HH");
        String fields = header;
        putProperty(PROP_FIELDS, fields);
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
        int year = (int)textRecord.getValue(4);
        double julianDay = textRecord.getValue(5);
        int day = (int) julianDay;
        double remainder =  julianDay-day;
        int hour = (int)remainder;
        remainder = remainder - hour;
        int minute = (int)(remainder*60);
        remainder = remainder - minute;
        GregorianCalendar gc = new GregorianCalendar();
        gc.set(GregorianCalendar.YEAR, year);
        gc.set(GregorianCalendar.DAY_OF_YEAR, day);
        gc.set(GregorianCalendar.HOUR, hour);
        gc.set(GregorianCalendar.MINUTE, minute);
        gc.set(GregorianCalendar.SECOND,0);
        //        System.out.println(gc.getTime() +" " + textRecord.getLine());
        record.setRecordTime(gc.getTime().getTime());
        return true;
    }


    public static void main(String[]args) {
        PointFile.test(args, GCNetPointFile.class);
    }



    /**
     * This is used by RAMADDA to determine what kind of services are available for this type of point IDX_data  = 1;
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
