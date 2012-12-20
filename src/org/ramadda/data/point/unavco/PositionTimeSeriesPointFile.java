package org.ramadda.data.point.unavco;

import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.StringUtil;

import java.io.*;
import java.util.Hashtable;
import java.util.List;


public class PositionTimeSeriesPointFile extends CsvFile  {

    /**
     * ctor
     */
    public PositionTimeSeriesPointFile() {
    }

    /**
     * ctor
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException _more_
     */
    public PositionTimeSeriesPointFile(String filename) throws IOException {
        super(filename);
    }

    /**
     * ctor
     *
     * @param filename _more_
     * @param properties _more_
     *
     * @throws IOException _more_
     */
    public PositionTimeSeriesPointFile(String filename,
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
     * How many lines in the header
     *
     * @param visitInfo the visit info
     *
     * @return number of lines to skip
     */
    public int getSkipLines(VisitInfo visitInfo) {
        return 9;
    }

    /*
The header:
PBO Station Position Time Series. Reference Frame : IGS08
Format Version,1.0.4
4-character ID,P101
Station name,RandolphLLUT2005
Begin Date, 2005-09-03
End Date, 2012-09-29
Release Date, 2012-09-30
Reference position, 41.6922736024 North Latitude, -111.2360162488 East Longitude, 2016.12225 meters elevation,
Date, North (mm), East (mm), Vertical (mm), North Std. Deviation (mm), East Std. Deviation (mm), Vertical Std. Deviation (mm), Quality,
2005-09-03,22.68, 42.87, 15.8, 1.5, 1.31, 5.23, final,
    */

    /**
     * Gets called when first reading the file. Parses the header
     *
     * @param visitInfo visit info
     *
     * @return the visit info
     *
     * @throws IOException on badness
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        super.prepareToVisit(visitInfo);
        List<String>headerLines = getHeaderLines();
        if(headerLines.size()!=getSkipLines(visitInfo)) {
            throw new IllegalArgumentException("Bad number of header lines:" + headerLines.size());
        }

        //Reference position, 41.6922736024 North Latitude, -111.2360162488 East Longitude, 2016.12225 meters elevation,
        String positionLine = headerLines.get(7);
        positionLine = positionLine.replaceAll(","," ");
        List<String>positionToks = StringUtil.split(positionLine," ",true,true);

        //TODO: Check the 'North' latitude part. I'm assuming this is always degrees north and east
        double lat = Double.parseDouble(positionToks.get(2));
        double lon = Double.parseDouble(positionToks.get(5));
        double elevation = Double.parseDouble(positionToks.get(8));

        //PBO Station Position Time Series. Reference Frame : IGS08
        String referenceFrame = StringUtil.split(headerLines.get(0), ":",true,true).get(1);

        //Format Version,1.0.4
        String formatVersion = StringUtil.split(headerLines.get(1), ",",true,true).get(1);

        //4-character ID,P101
        String fourCharId = StringUtil.split(headerLines.get(2), ",",true,true).get(1);

        //Station name,RandolphLLUT2005
        String stationName =  StringUtil.split(headerLines.get(3), ",",true,true).get(1);
        setLocation(lat,lon,elevation);
        //LOOK: this needs to be in the same order as the unavcotypes.xml defines in the point plugin
        setFileMetadata(new Object[]{
                fourCharId,
                stationName,
                referenceFrame,
                formatVersion,
            });

        String fields = "Four_Char_ID[type=string value=\"" + fourCharId.trim()+"\"],Latitude[value=" + lat +"],Longitude[value=" + lon +"],Elevation[value=" + elevation+"],Date[type=date format=yyyy-MM-dd], North [unit=mm chartable=true], East [unit=mm chartable=true], Vertical [unit=mm chartable=true], North_Std_Deviation [unit=mm chartable=true], East_Std_Deviation [unit=mm chartable=true], Vertical_Std_Deviation [unit=mm chartable=true], Quality[type=string chartable=true],skip";
        putProperty(PROP_FIELDS, fields);
        return visitInfo;
    }


    public static void main(String[]args) {
        PointFile.test(args, PositionTimeSeriesPointFile.class);
    }

}
