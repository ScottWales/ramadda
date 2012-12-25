
package org.ramadda.data.point.netcdf;


import org.ramadda.data.record.*;
import org.ramadda.data.point.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.StringUtil;

import java.io.*;



public class NetcdfPointFile extends PointFile {

    /**
     * ctor
     */
    public NetcdfPointFile() {}




    /**
     * ctor
     *
     *
     *
     *
     * @throws IOException On badness
     */
    public NetcdfPointFile(String filename) throws IOException {
        super(filename);
    }


    @Override
    public boolean isCapable(String action) {
        if(action.equals(ACTION_TRACKS)) return false;
        //        if(action.equals(ACTION_BOUNDINGPOLYGON)) return false;
        return super.isCapable(action);
    }



    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    public boolean canLoad(String file) {
        if(true) return false;
        try {
            return file.endsWith(".nc");
        } catch (Exception exc) {
            return false;
        }
    }



    /**
     * This just passes through to FileType.doMakeRecord
     *
     *
     * @param visitInfo the visit info
     * @return the new record
     */
    public Record doMakeRecord(VisitInfo visitInfo) {
        return null;
    }



    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param record _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     *
     * @throws IOException _more_
     */
    public Record.ReadStatus readNextRecord(VisitInfo visitInfo,
                                            Record record)
        throws IOException {
        return Record.ReadStatus.OK;
    }



    /**
     * Overwrite the getRecord method so we just do the skip
     * and not the final read that the RecordFile class does
     *
     * @param index _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
/*
    public Record getRecord(int index) throws Exception {
        RecordIO recordIO = doMakeInputIO(false);
        Record   record   = (Record) makeRecord(new VisitInfo());
        skip(new VisitInfo(recordIO), record, index);
        return record;
    }
*/


    /**
     * _more_
     *
     * @param recordIO _more_
     * @param visitInfo _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        visitInfo.setRecordIO(readHeader(visitInfo.getRecordIO()));
        return visitInfo;
    }


    /**
     * _more_
     *
     * @param recordIO _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public RecordIO readHeader(RecordIO recordIO) throws IOException {
        //        recordIO.getDataInputStream().read(header);
        return recordIO;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public long getNumRecords() {
        if (super.getNumRecords() <= 0) {
            try {
                RecordCountVisitor visitor = new RecordCountVisitor();
                visit(visitor, new VisitInfo(true), null);
                setNumRecords(visitor.getCount());
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }
        return super.getNumRecords();
    }




    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
    }


}
