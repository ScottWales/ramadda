package org.ramadda.data.point.noaa;
import java.text.SimpleDateFormat;

import org.ramadda.util.Station;
import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.StringUtil;

import java.io.*;
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
    String siteId; 
    String parameter;
    String project;
    String labIdNumber;
    String measurementGroup;

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
        Station station = setLocation(toks.get(1));
        String parameter =  toks.get(0);
        String measurementGroup =  toks.get(4);
        setFileMetadata(new Object[]{
                station.getId(),
                parameter,
                toks.get(2), //project
                toks.get(3), //lab id number
                measurementGroup,
            });
        if(type ==TYPE_HOURLY) {
            dateIndices =   new int[]{IDX_YEAR, IDX_MONTH, IDX_DAY, IDX_HOUR};
            putFields(new String[]{
                makeField(FIELD_SITE_ID, attrType(TYPE_STRING)),
                makeField(FIELD_LATITUDE, attrValue(""+ station.getLatitude())),
                makeField(FIELD_LONGITUDE, attrValue(""+ station.getLongitude())),
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
            dateIndices =   new int[]{IDX_YEAR, IDX_MONTH, IDX_DAY};
            putFields(new String[]{
                makeField(FIELD_SITE_ID, attrType(TYPE_STRING)),
                makeField(FIELD_LATITUDE, attrValue(""+ station.getLatitude())),
                makeField(FIELD_LONGITUDE, attrValue(""+ station.getLongitude())),
                makeField(FIELD_YEAR,""),
                makeField(FIELD_MONTH,""),
                makeField(FIELD_DAY,""),
                makeField(parameter,  attrChartable(), attrMissing(-999.990)),
                makeField(FIELD_STANDARD_DEVIATION,  attrChartable(), attrMissing(-99.990)),
                makeField(FIELD_NUMBER_OF_MEASUREMENTS,  attrChartable()),
                makeField(FIELD_QC_FLAG,attrType(TYPE_STRING)),
            });
        } else {
            dateIndices =   new int[]{IDX_YEAR, IDX_MONTH};
            putFields(new String[]{
                makeField(FIELD_SITE_ID, attrType(TYPE_STRING)),
                makeField(FIELD_LATITUDE, attrValue(""+ station.getLatitude())),
                makeField(FIELD_LONGITUDE, attrValue(""+ station.getLongitude())),
                makeField(FIELD_YEAR,""),
                makeField(FIELD_MONTH,""),
                makeField(parameter,  attrChartable(), attrMissing(-999.990)),
                makeField(FIELD_STANDARD_DEVIATION,  attrChartable(), attrMissing(-99.990)),
                makeField(FIELD_NUMBER_OF_MEASUREMENTS,  attrChartable()),
                makeField(FIELD_QC_FLAG,attrType(TYPE_STRING)),
            });
        }
        return visitInfo;
    }

    public static void main(String[]args) {
        PointFile.test(args, NoaaCarbonPointFile.class);
    }

}
