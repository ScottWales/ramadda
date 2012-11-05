
package org.ramadda.data.point.idv;



import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;

import java.awt.*;
import java.awt.image.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public class IdvPointFile extends CsvFile  {

    /**
     * _more_
     */
    public IdvPointFile() {}

    /**
     * ctor
     *
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException _more_
     */
    public IdvPointFile(String filename) throws IOException {
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
    public IdvPointFile(String filename, Hashtable properties)
            throws IOException {
        super(filename, properties);
    }


    public void visitorFinished(RecordVisitor visitor, VisitInfo visitInfo) {
        super.visitorFinished(visitor, visitInfo);
        System.err.println ("visitorFinished: " +visitor);
        if(visitor instanceof PointMetadataHarvester) {
            //            ((PointMetadataHarvester)visitor).putProperty("","");
        }
    }



    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public int getSkipLines(VisitInfo visitInfo) {
        String skipFromProperties = getProperty(PROP_SKIPLINES, (String)null);
        if(skipFromProperties !=null) 
            return  Integer.parseInt(skipFromProperties);
        return 2;
    }

    public RecordIO readHeader(RecordIO recordIO) throws IOException {
        return recordIO;
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
        if(headerLines.size()!=2) {
            throw new IllegalArgumentException("Bad number of header lines:" + headerLines.size());
        }
        String fields = headerLines.get(1);
        putProperty(PROP_FIELDS, fields);
        return visitInfo;
    }

    public List<RecordField>doMakeFields() {
        String fieldString = getProperty(PROP_FIELDS, null);
        if (fieldString == null) {
            try {
            RecordIO recordIO = doMakeInputIO(true);
            VisitInfo visitInfo = new VisitInfo();
            visitInfo.setRecordIO(recordIO);
            visitInfo = prepareToVisit(visitInfo);
            } catch(Exception exc) {
                throw new RuntimeException(exc);
            }
        }
        return super.doMakeFields();

    }





    /**
     * _more_
     *
     * @param index _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public PointRecord getRecord(int index) throws Exception {
        throw new IllegalArgumentException("Not implemented");
    }

    public static void main(String[]args) {
        PointFile.test(args, IdvPointFile.class);
    }

}
