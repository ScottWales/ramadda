
package org.ramadda.data.point.nacp;


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

public abstract class NoaaCarbonPointFile extends CsvFile  {



    public static final String FIELD_NUMBER_OF_MEASUREMENTS = "number_of_measurements";
    public static final String FIELD_QC_FLAG = "qc_flag";


    double latitude;
    double longitude;
    double elevation;
    String siteId; 
    String parameter;
    String project;
    String labIdNumber;
    String measurementGroup;



    /**
     * ctor
     */
    public NoaaCarbonPointFile() {
    }

    /**
     * ctor
     *
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException On badness
     */
    public NoaaCarbonPointFile(String filename) throws IOException {
        super(filename);
    }

    /**
     * ctor
     *
     * @param filename filename
     * @param properties properties
     *
     * @throws IOException On badness
     */
    public NoaaCarbonPointFile(String filename,
                               Hashtable properties)
        throws IOException {
        super(filename, properties);
    }



    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        super.prepareToVisit(visitInfo);
        String filename = getOriginalFilename(getFilename());
        //[parameter]_[site]_[project]_[lab ID number]_[measurement group]_[optional qualifiers].txt
        //site year month day hour value unc n flag intake_ht inst
        List<String> toks = StringUtil.split(filename,"_",true,true);
        //[parameter]_[site]_[project]_[lab ID number]_[measurement group]
        String siteId =  toks.get(1);
        String parameter =  toks.get(0);
        String project=  toks.get(2);
        String labIdNumber =  toks.get(3);
        String measurementGroup =  toks.get(4);
        setLocation(siteId);
        setFileMetadata(new Object[]{
                siteId,
                parameter,
                project,
                labIdNumber,
                measurementGroup,
            });
        return visitInfo;
    }


    public void   setLocation(String siteId) {
        if(siteId.equals("brw")) {
            latitude = 71.323;
            longitude = -156.611;
            elevation = 11;
        } else if(siteId.equals("mlo")) {
            latitude = 19.536;
            longitude = -155.576;
            elevation = 3397;
        } else if(siteId.equals("smo")) {
            latitude = -14.247;
            longitude = -170.564;
            elevation = 42;
        } else if(siteId.equals("spo")) {
            latitude = -89.98;
            longitude = -24.8;
            elevation = 2810;
        } else {
            System.err.println("Unknwon site id:" + siteId);
        }
        setLocation(latitude, longitude,elevation);
    }

    /**
     * This is used by RAMADDA to determine what kind of services are available for this type of point IDX_data  = 1;
     * @return is this file capable of the action
     */
    public boolean isCapable(String action) {
        if(action.equals(ACTION_BOUNDINGPOLYGON)) return false;
        if(action.equals(ACTION_GRID)) return false;
        return super.isCapable(action);
    }


    /*
     * Get the delimiter (space)
     *      @return the column delimiter
     */
    public String getDelimiter() {
        return " ";
    }


    /**
     * There are  2 header lines
     *
     * @param visitInfo file visit info
     *
     * @return how many lines to skip
     */
    public int getSkipLines(VisitInfo visitInfo) {
        return 0;
    }

}
