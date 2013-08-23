/*
* Copyright 2008-2013 Geode Systems LLC
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.data.services;


import org.ramadda.data.point.*;
import org.ramadda.data.record.*;

import org.ramadda.repository.*;
import org.ramadda.repository.job.*;
import org.ramadda.util.Utils;


import ucar.ma2.DataType;

import ucar.nc2.Attribute;
//import ucar.nc2.ft.point.writer.CFPointObWriter;
//import ucar.nc2.ft.point.writer.PointObVar;
import ucar.nc2.dt.point.CFPointObWriter;
import ucar.nc2.dt.point.PointObVar;


import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 */
public class NetcdfVisitor extends BridgeRecordVisitor {

    /** _more_          */
    public static final int MAX_STRING_LENGTH = 100;

    /** _more_          */
    private int recordCnt = 0;

    /** _more_          */
    private PointDataRecord cacheRecord;

    /** _more_          */
    private List<PointObVar> dataVars;

    /** _more_          */
    private File tmpFile;

    /** _more_          */
    private RecordIO tmpFileIO;

    /** _more_          */
    private CFPointObWriter writer;

    /** _more_          */
    private CsvVisitor csvVisitor = null;

    /** _more_          */
    private List<RecordField> fields;

    /** _more_          */
    private double[] dvals;

    /** _more_          */
    private String[] svals;

    /** _more_          */
    private boolean hasTime = false;

    /** _more_          */
    private Date now;

    /** _more_          */
    int cnt = 0;


    /**
     * _more_
     *
     * @param handler _more_
     * @param request _more_
     * @param processId _more_
     * @param mainEntry _more_
     */
    public NetcdfVisitor(RecordOutputHandler handler, Request request,
                         Object processId, Entry mainEntry) {
        super(handler, request, processId, mainEntry, ".nc");
    }

    /**
     * _more_
     *
     * @param tmpFile _more_
     */
    public NetcdfVisitor(File tmpFile) {
        this.tmpFile = tmpFile;
    }



    /**
     * _more_
     *
     * @param file _more_
     * @param record _more_
     *
     * @throws Exception _more_
     */
    private void init(RecordFile file, Record record) throws Exception {
        now     = new Date();
        hasTime = record.hasRecordTime();
        fields  = new ArrayList<RecordField>();
        List<PointObVar> stringVars = new ArrayList<PointObVar>();
        dataVars = new ArrayList<PointObVar>();
        int numDouble = 0;
        int numString = 0;
        for (RecordField field : file.getFields()) {
            if ( !(field.isTypeNumeric() || field.isTypeString())) {
                continue;
            }
            //Having a field called time breaks the cfwriter
            if (field.getName().equals("time")) {
                continue;
            }


            fields.add(field);
            PointObVar pointObVar = new PointObVar();
            pointObVar.setName(field.getName());
            if (Utils.stringDefined(field.getUnit())) {
                pointObVar.setUnits(field.getUnit());
            }

            if (field.isTypeNumeric()) {
                numDouble++;
                pointObVar.setDataType(DataType.DOUBLE);
                dataVars.add(pointObVar);
            } else if (field.isTypeString()) {
                numString++;
                pointObVar.setDataType(DataType.STRING);
                pointObVar.setLen(MAX_STRING_LENGTH);
                stringVars.add(pointObVar);
            }
        }
        dataVars.addAll(stringVars);
        dvals       = new double[numDouble];
        svals       = new String[numString];
        cacheRecord = new PointDataRecord((RecordFile) null);
        if (tmpFile == null) {
            tmpFile = getHandler().getStorageManager().getTmpFile(null,
                    "tmp.nc");
        }
        tmpFileIO             = new RecordIO(new FileOutputStream(tmpFile));
        cacheRecord.dvalsSize = dvals.length;
        cacheRecord.svalsSize = svals.length;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    private boolean jobOK() {
        Object jobId = getProcessId();
        if ((jobId != null) && (getHandler() != null)) {
            return getHandler().jobOK(jobId);
        }

        return true;
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param visitInfo _more_
     * @param record _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean doVisitRecord(RecordFile file, VisitInfo visitInfo,
                                 Record record)
            throws Exception {
        if (tmpFileIO == null) {
            init(file, record);
        }
        if ( !jobOK()) {
            return false;
        }
        int         dcnt        = 0;
        int         scnt        = 0;
        PointRecord pointRecord = (PointRecord) record;
        for (RecordField field : fields) {
            if (field.isTypeNumeric()) {
                dvals[dcnt++] = record.getValue(field.getParamId());
            } else if (field.isTypeString()) {
                String s = record.getStringValue(field.getParamId());
                if (s.length() > MAX_STRING_LENGTH) {
                    s = s.substring(0, MAX_STRING_LENGTH);
                }
                svals[scnt++] = s;
            }
        }
        recordCnt++;
        cacheRecord.setLatitude(pointRecord.getLatitude());
        cacheRecord.setLongitude(pointRecord.getLongitude());
        cacheRecord.setAltitude(pointRecord.getAltitude());
        if (hasTime) {
            cacheRecord.setTime(record.getRecordTime());
        } else {
            cacheRecord.setTime(now.getTime());
        }
        cacheRecord.setDvals(dvals);
        cacheRecord.setSvals(svals);
        cacheRecord.write(tmpFileIO);

        return true;
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     */
    @Override
    public void close(VisitInfo visitInfo) {
        try {
            if (tmpFileIO == null) {
                return;
            }
            tmpFileIO.close();
            List<Attribute>  globalAttributes = new ArrayList<Attribute>();
            DataOutputStream dos              = getTheDataOutputStream();
            writer = new CFPointObWriter(dos, globalAttributes, "m",
                                         dataVars, recordCnt);
            tmpFileIO = new RecordIO(new FileInputStream(tmpFile));
            System.err.println("writing " + recordCnt);
            for (int i = 0; i < recordCnt; i++) {
                cacheRecord.read(tmpFileIO);
                writer.addPoint(cacheRecord.getLatitude(),
                                cacheRecord.getLongitude(),
                                cacheRecord.getAltitude(),
                                new Date(cacheRecord.getTime()),
                                cacheRecord.getDvals(),
                                cacheRecord.getSvals());
            }
            writer.finish();
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
        super.close(visitInfo);
    }
}
