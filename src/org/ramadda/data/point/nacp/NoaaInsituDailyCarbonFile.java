
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

public class NoaaInsituDailyCarbonFile extends CsvFile  {


    public static final int IDX_SITE_CODE = 0;
    public static final int IDX_YEAR = 1;
    public static final int IDX_MONTH = 2;
    public static final int IDX_DAY = 3;
    public static final int IDX_HOUR = 4;
    public static final int IDX_MEAN_VALUE = 5;
    public static final int IDX_STANDARD_DEVIATION = 6;
    public static final int IDX_NUMBER_OF_MEASUREMENTS =7 ;
    public static final int IDX_QC_FLAG = 8;
    public static final int IDX_INTAKE_HEIGHT = 9;
    public static final int IDX_INSTRUMENT = 10;


    private SimpleDateFormat sdf = makeDateFormat("yyyy-MM-dd HH");


    /**
     * ctor
     */
    public NoaaInsituDailyCarbonFile() {
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
    public NoaaInsituDailyCarbonFile(String filename) throws IOException {
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
    public NoaaInsituDailyCarbonFile(String filename,
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
        String filename = getOriginalFilename(getFilename());
        //[parameter]_[site]_[project]_[lab ID number]_[measurement group]_[optional qualifiers].txt
        //site year month day hour value unc n flag intake_ht inst
        //BRW 1973 01 01 00   -999.990    -99.990   0 I..       0.00      N/A

        //co2_brw_surface
        /*
          co2      Carbon dioxide
          ch4      Methane
          co2c13   d13C (co2)
          merge
        */

        List<String> toks = StringUtil.split(filename,"_",true,true);
        //[parameter]_[site]_[project]_[lab ID number]_[measurement group]
        String siteId =  toks.get(1);
        String parameter =  toks.get(0);
        String project=  toks.get(2);
        String labIdNumber =  toks.get(3);
        String measurementGroup =  toks.get(4);
        //LOOK: this needs to be in the same order as the amrctypes.xml defines in the point plugin
        double latitude=0.0;
        double longitude=0.0;
        
        if(siteId.equals("brw")) {
            latitude = 71.3003;
            longitude = -156.7358;
        } else if(siteId.equals("mlo")) {
            latitude = 19.5391667;
            longitude = -155.5788889;
        } else if(siteId.equals("smo")) {
            latitude = -14.3000;
            longitude = -170.7000;
        } else if(siteId.equals("spo")) {
            latitude = -90;
            longitude = 0;
        } else {
            System.err.println("Unknwon site id:" + siteId);
        }
        setLocation(latitude, longitude,0);

        setFileMetadata(new Object[]{
                siteId,
                parameter,
                project,
                labIdNumber,
                measurementGroup,
            });

        String fields = makeFields(new String[]{
                makeField(FIELD_SITE_ID, attrType(TYPE_STRING)),
                makeField(FIELD_LATITUDE, attrValue(""+ latitude)),
                makeField(FIELD_LONGITUDE, attrValue(""+ longitude)),
                makeField(FIELD_YEAR,""),
                makeField(FIELD_MONTH,""),
                makeField(FIELD_DAY,""),
                makeField(FIELD_HOUR,attrType(TYPE_STRING)),
                makeField(parameter,  attrChartable(), attrMissing(-999.990)),
                makeField(FIELD_STANDARD_DEVIATION,  attrChartable(), attrMissing(-99.990)),
                makeField("number_of_measurements",  attrChartable()),
                makeField("qc_flag",attrType(TYPE_STRING)),
                makeField("intake_height"),
                makeField("instrument",attrType(TYPE_STRING)),
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
            ((int)textRecord.getValue(IDX_DAY)) + " " + textRecord.getStringValue(IDX_HOUR);

        Date date = sdf.parse(dttm);
        record.setRecordTime(date.getTime());
        return true;
    }


    public static void main(String[]args) {
        PointFile.test(args, NoaaInsituDailyCarbonFile.class);
    }

}
