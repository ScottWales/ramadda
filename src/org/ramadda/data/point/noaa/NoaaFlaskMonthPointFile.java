
package org.ramadda.data.point.noaa;


import java.text.SimpleDateFormat;


import org.ramadda.util.Station;

import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;



/**
 */

public  class NoaaFlaskMonthPointFile extends NoaaPointFile  {


    private static int IDX = 1;
    public static final int IDX_SITE_CODE = IDX++;
    public static final int IDX_LATITUDE = IDX++;
    public static final int IDX_LONGITUDE = IDX++;
    public static final int IDX_ELEVATION = IDX++;
    public static final int IDX_YEAR = IDX++;
    public static final int IDX_MONTH = IDX++;


    //    int type  = TYPE_HOURLY;

    private SimpleDateFormat sdf = makeDateFormat("yyyy-MM");

    /**
     * ctor
     */
    public NoaaFlaskMonthPointFile() {
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
    public NoaaFlaskMonthPointFile(String filename) throws IOException {
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
    public NoaaFlaskMonthPointFile(String filename,
                               Hashtable properties)
        throws IOException {
        super(filename, properties);
    }



    private static String header;

    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        super.prepareToVisit(visitInfo);
        if(header == null) {
            header = IOUtil.readContents("/org/ramadda/data/point/noaa/flaskmonthheader.txt", getClass()).trim();
            header = header.replaceAll("\n",",");
        }

        String fields = header;
        String filename = getOriginalFilename(getFilename());
        //[parameter]_[site]_[project]_[lab ID number]_[measurement group]_[optional qualifiers].txt
        List<String> toks = StringUtil.split(filename,"_",true,true);

        String siteId =  toks.get(1);
        String parameter =  toks.get(0);
        String project=  toks.get(2);
        String labIdNumber =  toks.get(3);
        String measurementGroup =  toks.get(4);
        setFileMetadata(new Object[]{
                siteId,
                parameter,
                project,
                labIdNumber,
                measurementGroup,
            });
        fields = fields.replace("${parameter}", parameter);


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
        String dttm = 
            ((int)textRecord.getValue(IDX_YEAR))+"-" + 
            textRecord.getStringValue(IDX_MONTH);
        String site =  textRecord.getStringValue(1);
        Station station = setLocation(site);
        if(station!=null) {
            textRecord.setValue(IDX_LATITUDE, station.getLatitude());
            textRecord.setValue(IDX_LONGITUDE, station.getLongitude());
            textRecord.setValue(IDX_ELEVATION, station.getElevation());
            textRecord.setLocation(station.getLatitude(),
                                   station.getLongitude(),
                                   station.getElevation());
        } else {
            //            System.err.println("NO station: " + site);
        }
        Date date = sdf.parse(dttm);
        record.setRecordTime(date.getTime());
        return true;
    }


    public static void main(String[]args) {
        PointFile.test(args, NoaaFlaskMonthPointFile.class);
    }


}
