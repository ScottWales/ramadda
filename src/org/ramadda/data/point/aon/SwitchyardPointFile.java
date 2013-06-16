
package org.ramadda.data.point.aon;


import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import org.ramadda.util.Utils;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.regex.*;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 */
public class SwitchyardPointFile extends SingleSiteTextFile  {

    /**
     * _more_
     */
    public SwitchyardPointFile() {
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
    public SwitchyardPointFile(String filename) throws IOException {
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
    public SwitchyardPointFile(String filename,
                               Hashtable properties)
        throws IOException {
        super(filename, properties);
    }


    /*
      Freshwater Switchyard of the Arctic Ocean                                        2012 CTD-Oxygen Ocean Profile
      Spring Hydrographic Survey by Twin Otter skiplane from CFS Alert                        
                                                                                                              
      Cast 6  Station U0W6           85.8520 degrees North  _  028.140 degrees West            2012-5-5 / 1525 UTC
                                                                                                              
      In situ   Potential                                                                   
      Depth     Pres     Temp      Temp      Cond    Salinity   Sigma     |---------Dissolved Oxygen----------|
      (m)     (dbar)    (degC)    (degC)    (S/m)     (psu)     -theta    (ml/l)   (mg/l)    (%sat)  (Mmol/Kg)
      #
      4.471     4.519   -1.7154   -1.7159   2.49611   31.3683   25.2269   8.61038  12.30510  100.0937   376.749
    */


    /**
     *
     * @param visitInfo holds visit info
     *
     * @return visit info
     *
     * @throws IOException on badness
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        try {
            putProperty(PROP_DELIMITER, " ");
            putProperty(PROP_HEADER_DELIMITER,"#");
            visitInfo = super.prepareToVisit(visitInfo);

            List<String>header = getHeaderLines();
            String hdr = StringUtil.join("\n", header);


            String line;
            String cast =null;
            String siteId=null;
            String latString =  null;
            String lonString =  null;

            String [] datePatterns  = {".*(\\d?\\d/\\d\\d/\\d\\d\\d\\d\\s*(_|/\\s)\\s*\\d\\d\\d\\d)\\s*UTC.*",
                                       ".*(\\d?\\d/\\d?\\d/\\d\\d\\d\\d\\s+\\d\\d\\d\\d)\\s*UTC.*",
                                       ".*\\s+(\\d?\\d\\s+[^\\s]+\\s+\\d\\d\\d\\d\\s+\\d\\d\\d\\d\\s+UTC).*",
                                       //5/3/2009 _ 1542 UTC
                                       ".*\\s+(\\d+/\\d+/\\d+\\s*_\\s*\\d\\d\\d\\d\\s+UTC)",
                                       //2012-5-21/1801 UTC
                                       ".*(\\d\\d\\d\\d-\\d?\\d-\\d?\\d/\\d\\d\\d\\d\\s+UTC).*",
                                       //2011-5-4/1439
                                       ".*(\\d\\d\\d\\d-\\d?\\d-\\d?\\d/\\d\\d\\d\\d).*",
                                       //2010-5-5 / 1706 UTC
                                       ".*(\\d\\d\\d\\d-\\d?\\d-\\d?\\d\\s*/\\s*\\d\\d\\d\\d\\s*UTC).*"
            };


            String [] dateFormats  = {"MM/dd/yyyyHHmm",
                                      "MM/dd/yyyyHHmm",
                                      "dd MMMM yyyy HHmm Z",
                                      "MM/dd/yyyy HHmm Z",
                                      "yyyy-MM-dd/HHmm",
                                      "yyyy-MM-dd/HHmm",
                                      "yyyy-MM-dd/HHmm Z"
            };

            Date date = Utils.findDate(hdr, datePatterns, dateFormats);
            if(date == null) {
                throw new IllegalArgumentException("no date  in header");
            }

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(date);
            int year = cal.get(cal.YEAR);

            hdr = hdr.replaceAll(" deg ", " degrees ");
            String [] lats =   Utils.findPatterns(hdr,".*\\s+([\\d\\.]+)\\s*degrees\\s*([\\d\\.]+)\\s*min\\s*North.*");
            if(lats!=null) {
                latString = lats[0]+ ":" + lats[1];
            }
            String [] lons =   Utils.findPatterns(hdr,".*\\s+([\\d\\.]+)\\s*degrees\\s*([\\d\\.]+)\\s*min\\s*West.*");
            if(lons!=null) {
                lonString = lons[0]+ ":" + lons[1];
            }


            if(latString == null) {
                //  84.122 degrees North  _  058.008 degrees West      
                latString = StringUtil.findPattern(hdr,".*\\s+([0-9\\.]+)\\s+degrees North.*");
                lonString = StringUtil.findPattern(hdr,".*\\s+([0-9\\.]+)\\s+degrees West.*");
            }


            cast =  StringUtil.findPattern(hdr,"\\s+Cast\\s+(\\d+).*");
            siteId =  StringUtil.findPattern(hdr,"\\s+Station\\s+([^\\s]+)\\s+.*");

            if(siteId == null) {
                siteId = "";
            }

            if(cast == null) {
                cast = "";
            }

        
            if(latString==null || lonString ==null) {
                throw new IllegalArgumentException("Could not read location from:" +hdr);
            }

            double lat =  Misc.decodeLatLon(latString);
            double lon =  Misc.decodeLatLon(lonString);
            setLocation(lat,lon,0);

            //LOOK: this needs to be in the same order as the aontypes.xml defines in the point plugin
            setFileMetadata(new Object[]{
                    siteId,
                    cast,
                });
            String dttm  = makeDateFormat("yyyy-MM-dd HH:mm").format(date);
            boolean addPotentialTemp = year==2005;
            String attrs = attrChartable() + attrSearchable();
            String fields = makeFields(new String[]{
                    makeField(FIELD_SITE_ID, attrType(TYPE_STRING), attrValue(siteId.trim())),
                    makeField("Cast",attrValue(cast), attrType(TYPE_STRING)),
                    makeField(FIELD_LATITUDE, attrValue(lat)),
                    makeField(FIELD_LONGITUDE, attrValue(lon)),
                    makeField(FIELD_DATE, attrType(TYPE_DATE), attrValue(dttm), attrFormat("yyyy-MM-dd HH:mm")),
                    makeField(FIELD_DEPTH,  attrs +  attrUnit("m")),
                    makeField("Pressure",  attrs +  attrUnit("dbar")),
                    makeField("In_Situ_Temperature",  attrs +  attrUnit("Celsius")),
                    (!addPotentialTemp?null:makeField("Potential_Temperature",  attrs +  attrUnit("Celsius"))),
                    makeField("Cond",  attrs +  attrUnit("S/m")),
                    makeField("Salinity",  attrs +  attrUnit("psu")),
                    makeField("Sigma",  attrs +  attrUnit("-theta")),
                    /*
                    makeField("Dissolved_Oxygen_ML_L",  attrs +  attrUnit("ml/l")),
                    makeField("Dissolved_Oxygen_MG_L",  attrs +  attrUnit("mg/l")),
                    makeField("Dissolved_Oxygen_Sat",  attrs +  attrUnit("%sat")),
                    makeField("Dissolved_Oxygen_MMOL_KG",  attrs +  attrUnit("Mmol/kg")),*/
                });
            //            System.err.println("fields:" + fields);
            putProperty(PROP_FIELDS, fields);
            return visitInfo;
        } catch(Exception exc) {
            throw new RuntimeException(exc);
        }

    }


    public static void main(String[]args) throws Exception {
        PointFile.test(args, SwitchyardPointFile.class);
    }

}
