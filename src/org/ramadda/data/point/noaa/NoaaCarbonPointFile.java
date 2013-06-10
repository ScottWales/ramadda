
package org.ramadda.data.point.noaa;


import java.text.SimpleDateFormat;


import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;



/**
 */

public  class NoaaCarbonPointFile extends NoaaPointFile  {


    private static int IDX = 1;
    public static final int IDX_SITE_CODE = IDX++;
    public static final int IDX_LATITUDE = IDX++;
    public static final int IDX_LONGITUDE = IDX++;
    public static final int IDX_YEAR = IDX++;
    public static final int IDX_MONTH = IDX++;
    public static final int IDX_DAY = IDX++;
    public static final int IDX_HOUR = IDX++;



    public static final String FIELD_NUMBER_OF_MEASUREMENTS = "number_of_measurements";
    public static final String FIELD_QC_FLAG = "qc_flag";

    public static final int TYPE_HOURLY = 1;
    public static final int TYPE_DAILY = 2;
    public static final int TYPE_MONTHLY = 3;

    int type  = TYPE_HOURLY;
    double latitude;
    double longitude;
    double elevation;
    String siteId; 
    String parameter;
    String project;
    String labIdNumber;
    String measurementGroup;

    private SimpleDateFormat sdf;

    /**
     * ctor
     */
    public NoaaCarbonPointFile() {
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
    public NoaaCarbonPointFile(String filename) throws IOException {
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
    public NoaaCarbonPointFile(String filename,
                               Hashtable properties)
        throws IOException {
        super(filename, properties);
    }



    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        super.prepareToVisit(visitInfo);
        String filename = getOriginalFilename(getFilename());

        if(filename.indexOf("_hour")>=0) type = TYPE_HOURLY;
        else if(filename.indexOf("_month")>=0) type = TYPE_MONTHLY;
        else if(filename.indexOf("_day")>=0) type = TYPE_DAILY;
        else throw new IllegalArgumentException("Unknown file:" + filename);

        //[parameter]_[site]_[project]_[lab ID number]_[measurement group]_[optional qualifiers].txt
        List<String> toks = StringUtil.split(filename,"_",true,true);
        String siteId =  toks.get(1);
        String parameter =  toks.get(0);
        String project=  toks.get(2);
        String labIdNumber =  toks.get(3);
        String measurementGroup =  toks.get(4);
        setLocation(siteId);
        setFileMetadata(new Object[]{
                siteId,
                parameter,
                project,
                labIdNumber,
                measurementGroup,
            });

        String fields;

        if(type ==TYPE_HOURLY) {
            sdf = makeDateFormat("yyyy-MM-dd HH");
            fields = makeFields(new String[]{
                makeField(FIELD_SITE_ID, attrType(TYPE_STRING)),
                makeField(FIELD_LATITUDE, attrValue(""+ latitude)),
                makeField(FIELD_LONGITUDE, attrValue(""+ longitude)),
                makeField(FIELD_YEAR,""),
                makeField(FIELD_MONTH,""),
                makeField(FIELD_DAY,""),
                makeField(FIELD_HOUR,attrType(TYPE_STRING)),
                makeField(parameter,  attrChartable(), attrMissing(-999.990)),
                makeField(FIELD_STANDARD_DEVIATION,  attrChartable(), attrMissing(-99.990)),
                makeField(FIELD_NUMBER_OF_MEASUREMENTS,  attrChartable()),
                makeField(FIELD_QC_FLAG,attrType(TYPE_STRING)),
                makeField("intake_height"),
                makeField("instrument",attrType(TYPE_STRING)),
            });

        } else if(type == TYPE_DAILY) {
            sdf = makeDateFormat("yyyy-MM-dd");
            fields = makeFields(new String[]{
                makeField(FIELD_SITE_ID, attrType(TYPE_STRING)),
                makeField(FIELD_LATITUDE, attrValue(""+ latitude)),
                makeField(FIELD_LONGITUDE, attrValue(""+ longitude)),
                makeField(FIELD_YEAR,""),
                makeField(FIELD_MONTH,""),
                makeField(FIELD_DAY,""),
                makeField(parameter,  attrChartable(), attrMissing(-999.990)),
                makeField(FIELD_STANDARD_DEVIATION,  attrChartable(), attrMissing(-99.990)),
                makeField(FIELD_NUMBER_OF_MEASUREMENTS,  attrChartable()),
                makeField(FIELD_QC_FLAG,attrType(TYPE_STRING)),
            });
        } else {
            sdf = makeDateFormat("yyyy-MM");
            fields = makeFields(new String[]{
                makeField(FIELD_SITE_ID, attrType(TYPE_STRING)),
                makeField(FIELD_LATITUDE, attrValue(""+ latitude)),
                makeField(FIELD_LONGITUDE, attrValue(""+ longitude)),
                makeField(FIELD_YEAR,""),
                makeField(FIELD_MONTH,""),
                makeField(parameter,  attrChartable(), attrMissing(-999.990)),
                makeField(FIELD_STANDARD_DEVIATION,  attrChartable(), attrMissing(-99.990)),
                makeField(FIELD_NUMBER_OF_MEASUREMENTS,  attrChartable()),
                makeField(FIELD_QC_FLAG,attrType(TYPE_STRING)),
            });
        }

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
        String dttm;
        if(type == TYPE_HOURLY) {
             dttm = ((int)textRecord.getValue(IDX_YEAR))+"-" + ((int)textRecord.getValue(IDX_MONTH)) +"-"+ 
                 ((int)textRecord.getValue(IDX_DAY)) + " " + textRecord.getStringValue(IDX_HOUR);
        } else if(type == TYPE_DAILY) {
             dttm = ((int)textRecord.getValue(IDX_YEAR))+"-" + ((int)textRecord.getValue(IDX_MONTH)) +"-"+ 
                 ((int)textRecord.getValue(IDX_DAY));
        } else {
            dttm = ((int)textRecord.getValue(IDX_YEAR))+"-" + ((int)textRecord.getValue(IDX_MONTH));
        }
        Date date = sdf.parse(dttm);
        record.setRecordTime(date.getTime());
        return true;
    }


    public static void main(String[]args) {
        PointFile.test(args, NoaaCarbonPointFile.class);
    }

}
