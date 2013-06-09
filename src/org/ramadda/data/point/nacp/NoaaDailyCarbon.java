
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

public class NoaaDailyCarbon extends NoaaCarbonPointFile  {

    private static int IDX = 1;
    public static final int IDX_SITE_CODE = IDX++;
    public static final int IDX_LATITUDE = IDX++;
    public static final int IDX_LONGITUDE = IDX++;
    public static final int IDX_YEAR = IDX++;
    public static final int IDX_MONTH = IDX++;
    public static final int IDX_DAY = IDX++;
    public static final int IDX_HOUR = IDX++;
    public static final int IDX_MEAN_VALUE = IDX++;
    public static final int IDX_STANDARD_DEVIATION = IDX++;
    public static final int IDX_NUMBER_OF_MEASUREMENTS =IDX++ ;
    public static final int IDX_QC_FLAG = IDX++;
    public static final int IDX_INTAKE_HEIGHT =IDX++;
    public static final int IDX_INSTRUMENT = IDX++;


    private SimpleDateFormat sdf = makeDateFormat("yyyy-MM-dd");


    /**
     * ctor
     */
    public NoaaDailyCarbon() {
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
    public NoaaDailyCarbon(String filename) throws IOException {
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
    public NoaaDailyCarbon(String filename,
                                     Hashtable properties)
        throws IOException {
        super(filename, properties);
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
        String fields = makeFields(new String[]{
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
            ((int)textRecord.getValue(IDX_DAY));

        Date date = sdf.parse(dttm);
        record.setRecordTime(date.getTime());
        return true;
    }


    public static void main(String[]args) {
        PointFile.test(args, NoaaDailyCarbon.class);
    }

}
