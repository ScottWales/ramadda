package org.ramadda.data.point.geomag;

import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;



/**
*/
public class M88PointFile extends CsvFile  {


    private SimpleDateFormat sdfShort = makeDateFormat("yyyyMMdd");
    private SimpleDateFormat sdfLong = makeDateFormat("yyyyMMdd HHmmss S");

    public static final double MISSING = 444.0;

    public static final String FIELD_SURVEY_ID = "SURVEY_ID";
    public static final String FIELD_DATE = "DATE";
    public static final String FIELD_TIME  = "TIME";
    public static final String FIELD_LAT  = "LAT";
    public static final String FIELD_LON  = "LON";
    public static final String FIELD_ALT_BAROM  = "ALT_BAROM";
    public static final String FIELD_ALT_GPS  = "ALT_GPS";
    public static final String FIELD_ALT_RADAR  = "ALT_RADAR";
    public static final String FIELD_POS_TYPE  = "POS_TYPE";
    public static final String FIELD_LINEID  = "LINEID";
    public static final String FIELD_FIDUCIAL  = "FIDUCIAL";
    public static final String FIELD_TRK_DIR  = "TRK_DIR";
    public static final String FIELD_NAV_QUALCO  = "NAV_QUALCO";
    public static final String FIELD_MAG_TOTOBS  = "MAG_TOTOBS";
    public static final String FIELD_MAG_TOTCOR  = "MAG_TOTCOR";
    public static final String FIELD_MAG_RES  = "MAG_RES";
    public static final String FIELD_MAG_DECLIN  = "MAG_DECLIN";
    public static final String FIELD_MAG_HORIZ  = "MAG_HORIZ";
    public static final String FIELD_MAG_X_NRTH  = "MAG_X_NRTH";
    public static final String FIELD_MAG_Y_EAST  = "MAG_Y_EAST";
    public static final String FIELD_MAG_Z_VERT  = "MAG_Z_VERT";
    public static final String FIELD_MAG_INCLIN  = "MAG_INCLIN";
    public static final String FIELD_MAG_DICORR  = "MAG_DICORR";
    public static final String FIELD_IGRF_CORR  = "IGRF_CORR";
    public static final String FIELD_MAG_QUALCO = "MAG_QUALCO";

    public static final String[] STRING_FIELDS = {FIELD_SURVEY_ID, FIELD_LINEID,FIELD_FIDUCIAL};

    private int dateIdx=-1;
    private int timeIdx=-1;

    /**
     * The constructor


    /**
     * The constructor
     *
     * @param filename file
     * @throws IOException On badness
     */
    public M88PointFile(String filename) throws IOException {
        super(filename);
    }

    public Record doMakeRecord(VisitInfo visitInfo) {
        TextRecord record = (TextRecord) super.doMakeRecord(visitInfo);
        record.setBePickyAboutTokens(false);
        return record;
    }


    /**
     * This  gets called before the file is visited. It reads the header and defines the fields
     *
     * @param visitInfo visit info
     * @return possible new visitinfo
     * @throws IOException On badness
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        //Se the delimiter and how many lines in the header to skip
        putProperty(PROP_DELIMITER, "tab");
        putProperty(PROP_SKIPLINES, "1");
        super.prepareToVisit(visitInfo);


        //Read the header and make sure things are cool
        List<String>headerLines = getHeaderLines();
        if(headerLines.size()!=getSkipLines(visitInfo)) {
            throw new IllegalArgumentException("Bad number of header lines:" + headerLines.size());
        }

        List<String> fields = StringUtil.split(headerLines.get(0),"\t",true, false);
        System.err.println("tokens:" + fields);
        /*
        setFileMetadata(new Object[]{
                siteId,
                siteName,
                argosId
            });
        */

        StringBuffer sb  = new StringBuffer();


        int fieldCnt = 0;
        for(String field: fields) {

            if(field.equals(FIELD_DATE)) dateIdx = fieldCnt+1;
            else if(field.equals(FIELD_TIME)) timeIdx = fieldCnt+1;
            fieldCnt++;
            if(sb.length()>0) sb.append(",");
            sb.append(field);
            sb.append("[");
            for(String stringField:STRING_FIELDS) {
                if(field.equals(stringField)) {
                    sb.append(" type=string ");
                    break;
                }
            }
            sb.append(field);
            sb.append("]");
        }

        putProperty(PROP_FIELDS, sb.toString());
        return visitInfo;
    }


    /*
     * This gets called after a record has been read
     */
    public boolean processAfterReading(VisitInfo visitInfo, Record record) throws Exception {
        if(!super.processAfterReading(visitInfo, record)) return false;
        if(dateIdx<0) return true;
        TextRecord textRecord = (TextRecord) record;

        double value = textRecord.getValue(dateIdx);

        if(Double.isNaN(value)) return true;
        StringBuffer dttm = new StringBuffer();
        dttm.append((int)value);
        SimpleDateFormat sdf = sdfShort;
        if(timeIdx>=0)  {
            value = textRecord.getValue(timeIdx);
            if(!Double.isNaN(value)) {
                System.err.println (value);
                int hhmmss = (int) value;
                double rem  = value-hhmmss;
                dttm.append(" ");
                dttm.append(Misc.padLeft(""+hhmmss,6,"0"));
                dttm.append(" ");
                dttm.append((int)(rem*1000));
                sdf = sdfLong;
            }
        }

        Date date = sdfLong.parse(dttm.toString());



        record.setRecordTime(date.getTime());
        return true;
    }

    public static void main(String[]args) {
        PointFile.test(args, M88PointFile.class);
    }

}
