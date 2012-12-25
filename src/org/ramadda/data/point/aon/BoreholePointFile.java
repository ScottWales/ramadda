
package org.ramadda.data.point.aon;

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
public class BoreholePointFile extends CsvFile  {

    /**
     * _more_
     */
    public BoreholePointFile() {
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
    public BoreholePointFile(String filename) throws IOException {
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
    public BoreholePointFile(String filename,
                                Hashtable properties)
            throws IOException {
        super(filename, properties);
    }


    public boolean isCapable(String action) {
        if(action.equals(ACTION_BOUNDINGPOLYGON)) return false;
        if(action.equals(ACTION_GRID)) return false;
        return super.isCapable(action);
    }


    /*
Soil temperature  at different depths,,,OPP-9721347 and OPP-9870635,,,,,,,,,,,
Vorkuta,,,PI/Data contact =Vladimir E. Romanovsky,,,,,,,,,,,
Location: N 67.41069 E 63.39578,,,Professor,,,,,,,,,,,
Elevation (meters): ,,,Geophysical Institute UAF      tel.: (907)474-7459,,,,,,,,,,,
Slope: flat,,,903 Koyukuk Drive              FAX : (907)474-7290 ,,,,,,,,,,,
Aspect: flat,,,P.O.Box 757320                 ,,,,,,,,,,,
,,,"Fairbanks, AK 99775-7320    e-mail: ffver@uaf.edu",,,,,,,,,,,
,,,"Data provided by N.Oberman, MIREKO",,,,,,,,,,,
Data dates: 09/08/08-04/14/09,,,,,,,,,,,,,,
"WARNING: ""999"" fields mean not valid data;  ""-999"" fields mean data are absent; all temperatures in grad C",,,,,,,,,,,,,,
,,,,,,,,,,,,,,
,,Temperature,Temperature,Temperature,Temperature,,,,,,,,,
YEAR,DATE,5 m,10 m,15 m,20 m,,,,,,,,,
2008,9/8/08,1.89,0.77,0.16,-0.17,,,,,,,,,
    */


    public String getDelimiter() {
        return ",";
    }

    /**
       Overwrite the record making method so we can check for empty lines
     */
@Override
    public Record doMakeRecord(VisitInfo visitInfo) {
        TextRecord record = new TextRecord(this, getFields()) {
                public boolean lineOk(String line) {
                    if(!super.lineOk(line)) return false;
                    if(line.startsWith(",")) return false;
                    return true;
                }

            };
        record.setDelimiter(getDelimiter());
        return record;
    }

    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     */
@Override
    public int getSkipLines(VisitInfo visitInfo) {
        return 13;
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
        List<String> toks;

        toks = StringUtil.split(headerLines.get(1),",");
        String siteId =  toks.get(0);
        //PI/Data contact =Vladimir E. Romanovsky
        String contact = toks.get(3);
        if(contact.indexOf("=")>0) {
            contact = StringUtil.findPattern(contact,".*=(.*)");
        }

        //Location: N 67.41069 E 63.39578,,,Professor,,,,,,,,,,,
        String locationString =  StringUtil.findPattern(headerLines.get(2),"Location:\\s(.*),");
        if(locationString==null) {
            throw new IllegalArgumentException("Could not read location from:" +headerLines.get(2));
        }

        toks = StringUtil.split(locationString," ", true, true);
        if(toks.size()!=4) {
            throw new IllegalArgumentException("Could not read location from:" +headerLines.get(2));
        }
        double lat =  Misc.decodeLatLon(toks.get(1)+toks.get(0));
        double lon =  Misc.decodeLatLon(toks.get(3)+toks.get(2));
        setLocation(lat,lon,0);

        //LOOK: this needs to be in the same order as the amrctypes.xml defines in the point plugin
        setFileMetadata(new Object[]{
                siteId,
                contact
            });
        String fields = "Site_Id[type=string value=\"" + siteId.trim()+"\"],Latitude[value=" + lat +"],Longitude[value=" + lon +"],Year,Date[type=date format=\"MM/dd/yy\"],Temperature_5_Meter[searchable=true chartable=true unit=\"Celsius\"],Temperature_10_Meter[searchable=true chartable=true unit=\"Celsius\"],Temperature_15_Meter[searchable=true chartable=true unit=\"Celsius\"],Temperature_20_Meter[searchable=true chartable=true unit=\"Celsius\"]";
        putProperty(PROP_FIELDS, fields);
        return visitInfo;
    }


    public static void main(String[]args) {
        PointFile.test(args, BoreholePointFile.class);
    }

}
