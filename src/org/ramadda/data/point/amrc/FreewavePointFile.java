
package org.ramadda.data.point.amrc;


import java.text.SimpleDateFormat;


import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.util.Station;


import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;

import java.io.*;
import java.util.List;


/**
 */
public class FreewavePointFile extends CsvFile  {

    private static String header;


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

    public String getStationsPath() {
        return "/org/ramadda/data/point/amrc/freewavestations.txt";
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
        String line1 = headerLines.get(0);
        line1 = line1.replaceAll("\"","");

        List<String>toks = StringUtil.split(line1,",",true,true);
        String format =  toks.get(0);
        String siteId =  toks.get(1);
        String dataLoggerModel =  toks.get(2);
        String dataLoggerSerial =  toks.get(3);
        String firmware =  toks.get(4);

        Station station = getStation(siteId);
        if(station==null) {
            throw new IllegalArgumentException("Unable to find location for site:" + siteId);
        }

        setLocation(station.getLatitude(),station.getLongitude(),station.getElevation());

        //LOOK: this needs to be in the same order as the amrctypes.xml defines in the point plugin
        setFileMetadata(new Object[]{
                siteId,
                format,
                dataLoggerModel,
                dataLoggerSerial,
            });

        if(header==null) {
            header= IOUtil.readContents("/org/ramadda/data/point/amrc/freewaveheader.txt", getClass()).replaceAll("\n", " ");
        }
        String fields = makeFields(new String[]{
                makeField(FIELD_SITE_ID, attrType(TYPE_STRING), attrValue(siteId.trim())),
                makeField(FIELD_LATITUDE, attrValue(station.getLatitude())),
                makeField(FIELD_LONGITUDE, attrValue(station.getLongitude())),
                makeField(FIELD_ELEVATION, attrValue(station.getElevation()))});

        fields+=header;
        putProperty(PROP_FIELDS, fields);
        return visitInfo;
    }


    public static void main(String[]args) {
        PointFile.test(args, FreewavePointFile.class);
    }

}
