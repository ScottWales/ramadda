
package org.ramadda.data.point.gcnet;


import java.text.SimpleDateFormat;

import java.util.GregorianCalendar;

import org.ramadda.util.Station;
import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.xml.XmlUtil;
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
    private static Hashtable<String,Station> stations;

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

    private void initHeader() throws IOException {
        if(header!=null) return;
        stations = new Hashtable<String,Station>();
        for(String line:StringUtil.split(IOUtil.readContents("/org/ramadda/data/point/gcnet/stations.txt", getClass()),"\n",true,true)) {
            List<String> toks   = StringUtil.split(line, ",",true,true);
            Station station  = new Station(toks.get(0),toks.get(1),
                                           Double.parseDouble(toks.get(2)),
                                           Double.parseDouble(toks.get(3)),
                                           Double.parseDouble(toks.get(4)));

            System.out.println("<entry " + 
                               XmlUtil.attr("name", station.getName()) +
                               XmlUtil.attr("type", "project_site") +
                               XmlUtil.attr("latitude", ""+station.getLatitude()) +
                               XmlUtil.attr("longitude", ""+station.getLongitude()) +
                               ">");
            System.out.println("<short_name>" +"GCNET-" +  toks.get(0) + "</short_name>");
            System.out.println("<status>active</status>");
            System.out.println("<network>GCNET</network>");
            System.out.println("<location>Greenland</location>");
            System.out.println("</entry>");
            stations.put(toks.get(0), station);
        }
        header = IOUtil.readContents("/org/ramadda/data/point/gcnet/header.txt", getClass()).trim();
        header = header.replaceAll("\n",",");
    }

    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        initHeader();
        super.prepareToVisit(visitInfo);
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
        String siteId = ""+site;
        
        Station station = stations.get(siteId);
        textRecord.setValue(2, station.getLatitude());
        textRecord.setValue(3, station.getLongitude());
        textRecord.setValue(4, station.getElevation());
        textRecord.setLocation(station.getLongitude(),
                               station.getLatitude(),
                               station.getElevation());


        int year = (int)textRecord.getValue(5);
        double julianDay = textRecord.getValue(6);
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


    public static void main(String[]args) {
        PointFile.test(args, GCNetPointFile.class);
    }




}
