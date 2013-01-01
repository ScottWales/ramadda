
package org.ramadda.data.point.aon;

import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
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
@Override
    public int getSkipLines(VisitInfo visitInfo) {
        return 9;
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
        boolean haveHeader = headerLines.size() > 0;
        while(true) {
            String line = visitInfo.getRecordIO().readLine();
            if(line.trim().equals("#")) break;
            if(!haveHeader) {
                headerLines.add(line);
            }
        }
        List<String>header = headerLines;


        String line;
        String cast;
        String siteId;
        String dttm;
        String latString =  null;
        String lonString =  null;


        boolean oldFormat = false;
        

        if(header.size() == 8) {
            //Cast 6  Station U0W6           85.8520 degrees North  _  028.140 degrees West            2012-5-5 / 1525 UTC
            line =  header.get(3);
            cast =  StringUtil.findPattern(line,"^(.*)Station");
            siteId =  StringUtil.findPattern(line,"Station\\s*([^\\s]+)\\s");
            List<String> toks = StringUtil.split(line, " ", true, true);
            int ntoks = toks.size();
            dttm = toks.get(ntoks-4) +" " + toks.get(ntoks-2);
            try {
                Date  date = makeDateFormat("yyyy-MM-dd HHmm").parse(dttm);
                dttm  = makeDateFormat("yyyy-MM-dd HH:mm").format(date);
            } catch(Exception exc) {
                throw new RuntimeException(exc);
            }
            latString =  StringUtil.findPattern(line,"([\\d\\.]+)\\s*degrees\\s*North");
            lonString =  StringUtil.findPattern(line,"([\\d\\.]+)\\s*degrees\\s*West");
        } else if(header.size() == 6) {
            oldFormat = true;
            //Station 1 (Cast 2)              84deg 01.763min N   65deg 09.247min W              2003-5-6/1730 GMT
            line =  header.get(1);
            siteId =  StringUtil.findPattern(line,"^(.*)\\(");
            cast =  StringUtil.findPattern(line,"\\((.*)\\)");
            List<String> toks = StringUtil.split(line, " ", true, true);
            int ntoks = toks.size();
            dttm =  toks.get(ntoks-2);
            try {
                Date  date = makeDateFormat("yyyy-MM-dd/HHmm").parse(dttm);
                dttm  = makeDateFormat("yyyy-MM-dd HH:mm").format(date);
            } catch(Exception exc) {
                throw new RuntimeException(exc);
            }
            Pattern p = Pattern.compile(".*\\s+([0-9]+)deg\\s+([0-9\\.]+)min.*\\s+([0-9]+)deg\\s+([0-9\\.]+)min.*");
            
            Matcher m = p.matcher(line);
            if(!m.matches()) {
                throw new IllegalArgumentException("Could not read location" + line);
            }
            latString = ""+(Double.parseDouble(m.group(1)) +  Double.parseDouble(m.group(2))/60.0);
            lonString = ""+(Double.parseDouble(m.group(3)) +  Double.parseDouble(m.group(4))/60.0);
            //            System.err.println ("line:" + line);
            //            System.err.println ("lat:" + latString);
            //            System.err.println ("lon:" + lonString);
        } else {
            throw new IllegalArgumentException("unknown header:" + header);
        }

        
        if(latString==null || lonString ==null) {
            throw new IllegalArgumentException("Could not read location from:" +line);
        }


        double lat =  Misc.decodeLatLon(latString);
        double lon =  Misc.decodeLatLon(lonString);
        setLocation(lat,lon,0);


        //LOOK: this needs to be in the same order as the aontypes.xml defines in the point plugin
        setFileMetadata(new Object[]{
                siteId,
                cast,
            });
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
                (oldFormat?null:makeField("Potential_Temperature",  attrs +  attrUnit("Celsius"))),
                makeField("Cond",  attrs +  attrUnit("S/m")),
                makeField("Salinity",  attrs +  attrUnit("psu")),
                makeField("Sigma",  attrs +  attrUnit("-theta")),
                makeField("Dissolved_Oxygen_ML_L",  attrs +  attrUnit("ml/l")),
                makeField("Dissolved_Oxygen_MG_L",  attrs +  attrUnit("mg/l")),
                makeField("Dissolved_Oxygen_Sat",  attrs +  attrUnit("%sat")),
                makeField("Dissolved_Oxygen_MMOL_KG",  attrs +  attrUnit("Mmol/kg")),
            });
        putProperty(PROP_FIELDS, fields);
        return visitInfo;
    }


    public static void main(String[]args) {
        PointFile.test(args, SwitchyardPointFile.class);
    }

}
