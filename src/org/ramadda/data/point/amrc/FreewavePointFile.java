
package org.ramadda.data.point.amrc;


import java.text.SimpleDateFormat;


import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



/**
 */
public class FreewavePointFile extends CsvFile  {

    private Hashtable<String,double[]> sites;
    private String fixedHeader;

    /**
     * _more_
     */
    public FreewavePointFile() {
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
    public FreewavePointFile(String filename) throws IOException {
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
    public FreewavePointFile(String filename,
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
        return ",";
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public int getSkipLines(VisitInfo visitInfo) {
        return 4;
    }

    private double[] getLocation(String stationName) throws IOException {
        if(sites == null) {
            sites = new Hashtable<String,double[]>();
            for(String line:  StringUtil.split(IOUtil.readContents("/org/ramadda/data/point/amrc/freewave_lat_lon_elev.txt", getClass()),"\n", true,true)) {
                //CapeBird=77.217S 166.439E    38
                List<String> toks = StringUtil.splitUpTo(line,"=",2);
                String site = toks.get(0).trim();
                List<String> toks2 = StringUtil.split(toks.get(1)," ",true,true);
                double[] loc = new double[]{
                    Misc.decodeLatLon(toks2.get(0)),
                    Misc.decodeLatLon(toks2.get(1)),
                    Double.parseDouble(toks2.get(2))};
                sites.put(site, loc);
            }
        }
        return sites.get(stationName.trim());
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
        List<String>header = getHeaderLines();
        if(header.size()!=getSkipLines(visitInfo)) {
            throw new IllegalArgumentException("Bad number of header lines:" + header.size());
        }
        /*
        TOA5 = Campbell Scientific data format, this is the ascii text format
CapeBird = station name as defined in the data logger support software
CR1000 = datalogger model
38966 = datalogger serial number
CR1000.Std.22 = this is the current firmware installed on the datalogger
            CPU:newaws . . . = the program that is running on the data logger, CPU indicates the program is stored on the data logger (it could be stored on a compact flash card)
42717 = datalogger program signature
OutCard = tablename in the program on the data logger where the data was collected
        */
        //"TOA5","CapeBird","CR1000","38966","CR1000.Std.22","CPU:newawsFWv31_CB.CR1","42171","OutCard"
        String line1 = header.get(0);
        line1 = line1.replaceAll("\"","");

        List<String>toks = StringUtil.split(line1,",",true,true);
        String format =  toks.get(0);
        String stationName =  toks.get(1);
        String dataLoggerModel =  toks.get(2);
        String dataLoggerSerial =  toks.get(3);
        String firmware =  toks.get(4);

        double[]loc = getLocation(stationName);
        if(loc==null) {
            throw new IllegalArgumentException("Unable to find location for site:" + stationName);
        }

        double lat = loc[0];
        double lon = loc[1];
        double elevation = loc[2];
        setLocation(lat,lon,elevation);

        //LOOK: this needs to be in the same order as the amrctypes.xml defines in the point plugin
        setFileMetadata(new Object[]{
                stationName,
                format,
                dataLoggerModel,
                dataLoggerSerial,
            });

        //TODO: pressure has an offset

        if(fixedHeader==null) {
            fixedHeader= IOUtil.readContents("/org/ramadda/data/point/amrc/freewaveheader.txt", getClass()).replaceAll("\n", " ");
        }
        String fields = "Station_Name[type=string value=\"" + stationName+"\"],Latitude[value=" + lat +"],Longitude[value=" + lon +"],Elevation[value=" + elevation+"]," + fixedHeader;
        putProperty(PROP_FIELDS, fields);
        return visitInfo;
    }


    public static void main(String[]args) {
        PointFile.test(args, FreewavePointFile.class);
    }

}
