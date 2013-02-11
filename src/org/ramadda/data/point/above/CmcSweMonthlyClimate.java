
package org.ramadda.data.point.above;

import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;
import java.io.*;


import java.util.Hashtable;



/**
 */
public class CmcSweMonthlyClimate extends CsvFile  {


    /**
     * _more_
     */
    public CmcSweMonthlyClimate() {
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
    public CmcSweMonthlyClimate(String filename) throws IOException {
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
    public CmcSweMonthlyClimate(String filename,
                             Hashtable properties)
            throws IOException {
        super(filename, properties);
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
@Override
    public int getSkipLines(VisitInfo visitInfo) {
        return 1;
    }

    //LATITUDE,LONGITUDE,OCT,NOV,DEC,JAN,FEB,MAR,APR,MAY,JUN

    /**
     *
     * @param visitInfo holds visit info
     *
     * @return visit info
     *
     * @throws IOException on badness
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        super.prepareToVisit(visitInfo);
        String unit  =  attrUnit("feet");
        String fields = makeFields(new String[]{
                makeField(FIELD_LATITUDE),
                makeField(FIELD_LONGITUDE),
                makeField("october",unit),
                makeField("november",unit),
                makeField("december",unit),
                makeField("january",unit),
                makeField("february",unit),
                makeField("march",unit),
                makeField("april",unit),
                makeField("may",unit),
                makeField("june",unit),

            });
        putProperty(PROP_FIELDS, fields);
        return visitInfo;
    }


    public static void main(String[]args) {
        PointFile.test(args, CmcSweMonthlyClimate.class);
    }

}
