
package org.ramadda.data.point.nacp;


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

public class NoaaTowerNetworkFile extends CsvFile  {

    public static final int IDX_SITE_CODE  = 1;
    public static final int IDX_YEAR  = 2;
    public static final int IDX_MONTH  = 3;
    public static final int IDX_DAY  = 4;
    public static final int IDX_HOUR  = 5;
    public static final int IDX_MINUTE  = 6;
    public static final int IDX_SECOND  = 7;
    public static final int IDX_LATITUDE  = 8;
    public static final int IDX_LONGITUDE  = 9;
    public static final int IDX_ELEVATION  = 10;
    public static final int IDX_INTAKE_HEIGHT  = 11;
    public static final int IDX_MEASURED_VALUE  = 12;
    public static final int IDX_TOTAL_UNCERTAINTY_ESTIMATE  = 13;
    public static final int IDX_ATMOSPHERIC_VARIABILTY  = 14;
    public static final int IDX_MEASUREMENT_UNCERTAINTY  = 15;
    public static final int IDX_SCALE_UNCERTAINTY  = 16;
    public static final int IDX_QC_FLAG  = 17;



    private SimpleDateFormat sdf = makeDateFormat("yyyy-MM-dd HHmmss");

    public static final double MISSING = -999.0;

    /**
     * ctor
     */
    public NoaaTowerNetworkFile() {
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
    public NoaaTowerNetworkFile(String filename) throws IOException {
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
    public NoaaTowerNetworkFile(String filename,
                                Hashtable properties)
            throws IOException {
        super(filename, properties);
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

    /**
     * This  gets called before the file is visited. It reads the header and pulls out metadata
     *
     * @param visitInfo visit info
     *
     * @return possible new visitinfo
     *
     * @throws IOException On badness
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        super.prepareToVisit(visitInfo);
        String filename = new File(getFilename()).getName();
        String siteId =  StringUtil.findPattern(filename,"^(.*)_.*");
        //LOOK: this needs to be in the same order as the amrctypes.xml defines in the point plugin
        setFileMetadata(new Object[]{
                siteId,
            });

        String fields = makeFields(new String[]{
                makeField(FIELD_SITE_ID, attrType(TYPE_STRING)),
                makeField(FIELD_YEAR,""),
                makeField(FIELD_MONTH,""),
                makeField(FIELD_DAY,""),
                makeField(FIELD_HOUR,attrType(TYPE_STRING)),
                makeField(FIELD_MINUTE,attrType(TYPE_STRING)),
                makeField(FIELD_SECOND,attrType(TYPE_STRING)),
                makeField(FIELD_LATITUDE),
                makeField(FIELD_LONGITUDE),
                makeField("intake_height"),
                makeField("measured_value",  attrChartable(), attrMissing(MISSING)),
                makeField("total_uncertainty_estimate", attrChartable(), attrMissing(MISSING)),
                makeField("atmospheric_variablitility",  attrMissing(MISSING)),
                makeField("measurement_uncertainty",  attrChartable(), attrMissing(MISSING)),
                makeField("scale_uncertainty",  attrChartable(), attrMissing(MISSING)),
                makeField("qc_flag",attrType(TYPE_STRING)),
            });
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
        String dttm = ((int)textRecord.getValue(IDX_YEAR))+"-" + ((int)textRecord.getValue(IDX_MONTH)) +"-"+ 
           ((int)textRecord.getValue(IDX_DAY)) + " " + textRecord.getStringValue(IDX_HOUR) +
            textRecord.getStringValue(IDX_MINUTE) +
            textRecord.getStringValue(IDX_SECOND);
        Date date = sdf.parse(dttm);
        record.setRecordTime(date.getTime());
        return true;
    }


    public static void main(String[]args) {
        PointFile.test(args, NoaaTowerNetworkFile.class);
    }

}
