
package org.ramadda.data.point.noaa;


import java.text.SimpleDateFormat;


import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;



/**
 */

public  class NoaaFlaskEventPointFile extends NoaaPointFile  {


    private static int IDX = 1;
    public static final int IDX_SITE_CODE = IDX++;
    public static final int IDX_YEAR = IDX++;
    public static final int IDX_MONTH = IDX++;
    public static final int IDX_DAY = IDX++;
    public static final int IDX_HOUR = IDX++;
    public static final int IDX_MINUTE = IDX++;
    public static final int IDX_SECOND = IDX++;


    //    int type  = TYPE_HOURLY;

    private SimpleDateFormat sdf = makeDateFormat("yyyy-MM-dd HHmmss");

    /**
     * ctor
     */
    public NoaaFlaskEventPointFile() {
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
    public NoaaFlaskEventPointFile(String filename) throws IOException {
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
    public NoaaFlaskEventPointFile(String filename,
                               Hashtable properties)
        throws IOException {
        super(filename, properties);
    }



    private static String header;

    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        super.prepareToVisit(visitInfo);
        if(header == null) {
            header = IOUtil.readContents("/org/ramadda/data/point/noaa/flaskeventheader.txt", getClass()).trim();
            header = header.replaceAll("\n",",");
        }

        String fields = header;
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
     * It extracts and creates the record date/time
     */
    public boolean processAfterReading(VisitInfo visitInfo, Record record) throws Exception {
        if(!super.processAfterReading(visitInfo, record)) return false;
        TextRecord textRecord = (TextRecord) record;
        String dttm = 
            ((int)textRecord.getValue(IDX_YEAR))+"-" + 
            textRecord.getStringValue(IDX_MONTH) +"-"+ 
            textRecord.getStringValue(IDX_DAY) +" "+ 
            textRecord.getStringValue(IDX_HOUR) +""+ 
                textRecord.getStringValue(IDX_MINUTE) +""+ 
                textRecord.getStringValue(IDX_SECOND);

        Date date = sdf.parse(dttm);
        record.setRecordTime(date.getTime());
        return true;
    }


    public static void main(String[]args) {
        PointFile.test(args, NoaaFlaskEventPointFile.class);
    }


}
