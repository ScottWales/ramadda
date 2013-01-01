
package org.ramadda.data.point.amrc;


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
public class AmrcFinalQCPointFile extends CsvFile  {

    public static final int IDX_SITE_ID = 1;
    public static final int IDX_LATITUDE = 2;
    public static final int IDX_LONGITUDE = 3;
    public static final int IDX_ELEVATION = 4;
    public static final int IDX_YEAR = 5;
    public static final int IDX_JULIAN_DAY = 6;
    public static final int IDX_MONTH = 7;
    public static final int IDX_DAY = 8;
    public static final int IDX_TIME = 9;
    public static final int IDX_TEMPERATURE =10;
    public static final int IDX_PRESSURE = 11;
    public static final int IDX_WIND_SPEED = 12;
    public static final int IDX_WIND_DIRECTION = 13;
    public static final int IDX_RELATIVE_HUMIDITY = 14;
    public static final int IDX_DELTA_T = 15;

    private SimpleDateFormat sdf = makeDateFormat("yyyy-MM-dd HHmm");


    /**
     * _more_
     */
    public AmrcFinalQCPointFile() {
    }

    /**
     * ctor
     *
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException _more_
     */
    public AmrcFinalQCPointFile(String filename) throws IOException {
        super(filename);
    }

    /**
     * _more_
     *
     * @param filename _more_
     * @param properties _more_
     *
     * @throws IOException _more_
     */
    public AmrcFinalQCPointFile(String filename,
                                Hashtable properties)
            throws IOException {
        super(filename, properties);
    }


    public boolean isCapable(String action) {
        if(action.equals(ACTION_BOUNDINGPOLYGON)) return false;
        if(action.equals(ACTION_GRID)) return false;
        return super.isCapable(action);
    }


    public String getDelimiter() {
        return " ";
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public int getSkipLines(VisitInfo visitInfo) {
        return 2;
    }

    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
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
        double elevation = Misc.decodeLatLon(elevString);

        setLocation(lat,lon,elevation);

        //LOOK: this needs to be in the same order as the amrctypes.xml defines in the point plugin
        setFileMetadata(new Object[]{
                siteId,
                siteName,
                argosId
            });


        String fields = makeFields(new String[]{
                makeField(FIELD_SITE_ID, attrType(TYPE_STRING), attrValue(siteId.trim())),
                makeField(FIELD_LATITUDE, attrValue(lat)),
                makeField(FIELD_LONGITUDE, attrValue(lon)),
                makeField(FIELD_ELEVATION, attrValue(elevString)),
                makeField(FIELD_YEAR,""),
                makeField(FIELD_JULIAN_DAY,""),
                makeField(FIELD_MONTH,""),
                makeField(FIELD_DAY,""),
                makeField(FIELD_TIME,attrType(TYPE_STRING)),
                makeField("Temperature", attrUnit("Celsius")),
                makeField("Pressure", attrUnit("hPa")),
                makeField("Wind_Speed", attrUnit("m/s")),
                makeField("Wind_Direction"),
                makeField("Relative_Humidity", attrUnit("%")),
                makeField("Delta_T", attrUnit("Celsius")),
            });
        putProperty(PROP_FIELDS, fields);
        return visitInfo;
    }


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
