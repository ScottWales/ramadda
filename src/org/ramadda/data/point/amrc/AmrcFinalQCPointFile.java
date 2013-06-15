package org.ramadda.data.point.amrc;

import java.text.SimpleDateFormat;


import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.Date;
import java.util.List;



/**
 */
public class AmrcFinalQCPointFile extends CsvFile  {

    private static int IDX=1;
    public static final int IDX_SITE_ID = IDX++;
    public static final int IDX_LATITUDE = IDX++;
    public static final int IDX_LONGITUDE = IDX++;
    public static final int IDX_ELEVATION = IDX++;
    public static final int IDX_YEAR = IDX++;
    public static final int IDX_JULIAN_DAY = IDX++;
    public static final int IDX_MONTH = IDX++;
    public static final int IDX_DAY = IDX++;
    public static final int IDX_TIME = IDX++;
    public static final int IDX_TEMPERATURE =IDX++;
    public static final int IDX_PRESSURE = IDX++;
    public static final int IDX_WIND_SPEED = IDX++;
    public static final int IDX_WIND_DIRECTION = IDX++;
    public static final int IDX_RELATIVE_HUMIDITY = IDX++;
    public static final int IDX_DELTA_T = IDX++;

    private SimpleDateFormat sdf = makeDateFormat("yyyy-MM-dd HHmm");

    public static final double MISSING = 444.0;

    /**
     * ctor
     *
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException On badness
     */
    public AmrcFinalQCPointFile(String filename) throws IOException {
        super(filename);
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
        putProperty(PROP_DELIMITER, " ");
        putProperty(PROP_SKIPLINES, "2");
        super.prepareToVisit(visitInfo);
        List<String>headerLines = getHeaderLines();
        if(headerLines.size()!=getSkipLines(visitInfo)) {
            throw new IllegalArgumentException("Bad number of header lines:" + headerLines.size());
        }
        //        Year: 2012  Month: 01  ID: AG4  ARGOS:  8927  Name: AGO-4               
        //            Lat: 82.01S  Lon:  96.76E  Elev: 3597m
        String siteId =  StringUtil.findPattern(headerLines.get(0),"ID:\\s(.*)ARGOS:");
        String argosId =  StringUtil.findPattern(headerLines.get(0),"ARGOS:\\s*(.*)Name:");
        String siteName =  StringUtil.findPattern(headerLines.get(0),"Name:\\s(.*)");
        String latString =  StringUtil.findPattern(headerLines.get(1),"Lat:\\s(.*)Lon:");
        String lonString =  StringUtil.findPattern(headerLines.get(1),"Lon:\\s(.*)Elev:");
        String elevString =  StringUtil.findPattern(headerLines.get(1),"Elev:(.*)");
        if(latString == null || lonString == null ||
           siteName == null ||
           siteId == null) {
            throw new IllegalArgumentException("Could not read header:" + headerLines +" lat:"  + latString + " lon:" + lonString +" elev" +
                                               elevString +" siteName:" +
                                               siteName +" site:" + siteId);
            
        }
        if(elevString.endsWith("m")) {
            elevString = elevString.substring(0, elevString.length()-1);
        }
        double lat = Misc.decodeLatLon(latString);
        double lon = Misc.decodeLatLon(lonString);
        double elevation = Double.parseDouble(elevString);

        setLocation(lat,lon,elevation);

        //LOOK: this needs to be in the same order as the amrctypes.xml defines in the point plugin
        setFileMetadata(new Object[]{
                siteId,
                siteName,
                argosId
            });

        putFields(new String[]{
                makeField(FIELD_SITE_ID, attrType(TYPE_STRING), attrValue(siteId.trim())),
                makeField(FIELD_LATITUDE, attrValue(lat)),
                makeField(FIELD_LONGITUDE, attrValue(lon)),
                makeField(FIELD_ELEVATION, attrValue(elevString)),
                makeField(FIELD_YEAR,""),
                makeField(FIELD_JULIAN_DAY,""),
                makeField(FIELD_MONTH,""),
                makeField(FIELD_DAY,""),
                makeField(FIELD_TIME,attrType(TYPE_STRING)),
                makeField(FIELD_TEMPERATURE, attrUnit(UNIT_CELSIUS), attrChartable(), attrMissing(MISSING)),
                makeField(FIELD_PRESSURE, attrUnit(UNIT_HPA), attrChartable(), attrMissing(MISSING)),
                makeField(FIELD_WIND_SPEED, attrUnit(UNIT_M_S), attrChartable(), attrMissing(MISSING)),
                makeField(FIELD_WIND_DIRECTION, attrUnit(UNIT_DEGREES), attrMissing(MISSING)),
                makeField(FIELD_RELATIVE_HUMIDITY, attrUnit(UNIT_PERCENT), attrChartable(), attrMissing(MISSING)),
                makeField(FIELD_DELTA_T, attrUnit(UNIT_CELSIUS), attrChartable(), attrMissing(MISSING)),
            });

        return visitInfo;
    }


    /*
     * This gets called after a record has been read
     */
    public boolean processAfterReading(VisitInfo visitInfo, Record record) throws Exception {
        if(!super.processAfterReading(visitInfo, record)) return false;
        TextRecord textRecord = (TextRecord) record;
        String dttm = ((int)textRecord.getValue(IDX_YEAR))+"-" + ((int)textRecord.getValue(IDX_MONTH)) +"-"+ 
           ((int)textRecord.getValue(IDX_DAY)) + " " + textRecord.getStringValue(IDX_TIME);
        Date date = sdf.parse(dttm);
        record.setRecordTime(date.getTime());
        return true;
    }

    public static void main(String[]args) {
        PointFile.test(args, AmrcFinalQCPointFile.class);
    }

}
