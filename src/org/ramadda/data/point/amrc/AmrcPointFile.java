
package org.ramadda.data.point.amrc;



import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;




/**
 */
public class AmrcPointFile extends CsvFile  {

    /**
     * _more_
     */
    public AmrcPointFile() {}

    /**
     * ctor
     *
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException _more_
     */
    public AmrcPointFile(String filename) throws IOException {
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
    public AmrcPointFile(String filename, Hashtable properties)
            throws IOException {
        super(filename, properties);
    }




    public Object[] getFileMetadata() {
        return null;
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
        if(headerLines.size()!=2) {
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
        setFileMetadata(new Object[]{
                siteId,
                siteName,
                argosId
            });


        String fields = "Site_Id[type=string value=\"" + siteId.trim()+"\"],Latitude[value=" + lat +"],Longitude[value=" + lon +"],Elevation[value=" + elevString+"],Year,Julian_Day,Month,Day,Time,Temperature[unit=\"Celsius\"],Pressure[unit=\"hPa\"], Wind_Speed[unit=\"m/s\"],Wind_Direction,Relative_Humidity[unit=\"%\"],Delta_T[unit=\"Celsius\"]";
        putProperty(PROP_FIELDS, fields);
        return visitInfo;
    }

    public List<RecordField>doMakeFields() {
        String fieldString = getProperty(PROP_FIELDS, null);
        if (fieldString == null) {
            try {
            RecordIO recordIO = doMakeInputIO(true);
            VisitInfo visitInfo = new VisitInfo();
            visitInfo.setRecordIO(recordIO);
            visitInfo = prepareToVisit(visitInfo);
            } catch(Exception exc) {
                throw new RuntimeException(exc);
            }
        }
        return super.doMakeFields();

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
     * @param index _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public PointRecord getRecord(int index) throws Exception {
        throw new IllegalArgumentException("Not implemented");
    }

    public static void main(String[]args) {
        PointFile.test(args, AmrcPointFile.class);
    }

}
