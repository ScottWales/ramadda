
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

public class NoaaMonthlyCarbon extends NoaaCarbonPointFile  {


    private static int IDX = 1;
    public static final int IDX_SITE_CODE = IDX++;
    public static final int IDX_LATITUDE = IDX++;
    public static final int IDX_LONGITUDE = IDX++;
    public static final int IDX_YEAR = IDX++;
    public static final int IDX_MONTH = IDX++;
    public static final int IDX_MEAN_VALUE = IDX++;
    public static final int IDX_STANDARD_DEVIATION = IDX++;
    public static final int IDX_NUMBER_OF_MEASUREMENTS =IDX++ ;
    public static final int IDX_QC_FLAG = IDX++;


    private SimpleDateFormat sdf = makeDateFormat("yyyy-MM");


    /**
     * ctor
     */
    public NoaaMonthlyCarbon() {
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
    public NoaaMonthlyCarbon(String filename) throws IOException {
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
    public NoaaMonthlyCarbon(String filename,
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
        String filename = getOriginalFilename(getFilename());
        //[parameter]_[site]_[project]_[lab ID number]_[measurement group]_[optional qualifiers].txt

        List<String> toks = StringUtil.split(filename,"_",true,true);
        //[parameter]_[site]_[project]_[lab ID number]_[measurement group]
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

        //        # data_fields: site year month value unc n flag
        String fields = makeFields(new String[]{
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
        String dttm = ((int)textRecord.getValue(IDX_YEAR))+"-" + ((int)textRecord.getValue(IDX_MONTH));
        Date date = sdf.parse(dttm);
        record.setRecordTime(date.getTime());
        return true;
    }


    public static void main(String[]args) {
        PointFile.test(args, NoaaMonthlyCarbon.class);
    }

}
