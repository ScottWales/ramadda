
package org.ramadda.data.point.noaa;

import org.ramadda.util.Station;

import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.List;



/**
 */
public  class NoaaFlaskMonthPointFile extends NoaaPointFile  {
    private static int IDX = 1;
    public static final int IDX_SITE_CODE = IDX++;
    public static final int IDX_LATITUDE = IDX++;
    public static final int IDX_LONGITUDE = IDX++;
    public static final int IDX_ELEVATION = IDX++;
    public static final int IDX_YEAR = IDX++;
    public static final int IDX_MONTH = IDX++;


    /**
     * ctor
     *
     * @param filename _more_
     * @throws IOException On badness
     */
    public NoaaFlaskMonthPointFile(String filename) throws IOException {
        super(filename);
    }


    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        super.prepareToVisit(visitInfo);
        dateIndices= new int[]{IDX_YEAR,IDX_MONTH};

        String fields =  getFieldsFileContents();
        String filename = getOriginalFilename(getFilename());
        //[parameter]_[site]_[project]_[lab ID number]_[measurement group]_[optional qualifiers].txt
        List<String> toks = StringUtil.split(filename,"_",true,true);
        String siteId =  toks.get(1);
        String parameter =  toks.get(0);
        String project=  toks.get(2);
        String labIdNumber =  toks.get(3);
        String measurementGroup =  toks.get(4);
        setFileMetadata(new Object[]{
                siteId,
                parameter,
                project,
                labIdNumber,
                measurementGroup,
            });
        fields = fields.replace("${parameter}", parameter);
        putProperty(PROP_FIELDS, fields);
        return visitInfo;
    }



    /*
     * This gets called after a record has been read
     */
    public boolean processAfterReading(VisitInfo visitInfo, Record record) throws Exception {
        if(!super.processAfterReading(visitInfo, record)) return false;
        setLocation(record.getStringValue(1), (TextRecord)record);
        return true;
    }


    public static void main(String[]args) {
        PointFile.test(args, NoaaFlaskMonthPointFile.class);
    }


}
