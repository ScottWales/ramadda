/*
 * Copyright 2008-2012 Jeff McWhirter/ramadda.org
 *                     Don Murray/CU-CIRES
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
import org.ramadda.repository.metadata.*;
import org.ramadda.util.Utils;


import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.ft.point.writer.CFPointObWriter;
import ucar.nc2.ft.point.writer.PointObVar;


/**
 */
public class NetcdfVisitor extends BridgeRecordVisitor {

    public static final int MAX_STRING_LENGTH = 100;

    private int  recordCnt= 0;
    private PointDataRecord cacheRecord;
    private List<PointObVar> dataVars;
    private File tmpFile;
    private RecordIO tmpFileIO;
    private CFPointObWriter writer;
    private CsvVisitor csvVisitor = null;
    private List<RecordField> fields;
    private double[] dvals;
    private String[] svals;
    private boolean hasTime=false;
    private Date now;
    int     cnt = 0;


    public NetcdfVisitor(RecordOutputHandler handler, Request request,
                         Object processId, Entry mainEntry) {
        super(handler, request, processId, mainEntry, ".nc");
    }
    

    private void init(RecordFile file, Record record)
        throws Exception {
            now =new Date();
            hasTime = record.hasRecordTime();
            fields = new ArrayList<RecordField>();
            List<PointObVar>stringVars = new ArrayList<PointObVar>();
            dataVars = new ArrayList<PointObVar>();
            int numDouble = 0;
            int numString = 0;
            for (RecordField field : file.getFields()) {
                if(!(field.isTypeNumeric() || field.isTypeString())) {
                    continue;
                }
                //Having a field called time breaks the cfwriter
                if(field.getName().equals("time")) continue;


                fields.add(field);
                PointObVar pointObVar = new PointObVar();
                pointObVar.setName(field.getName());
                if(Utils.stringDefined(field.getUnit())) {
                    pointObVar.setUnits(field.getUnit());
                }

                if(field.isTypeNumeric()) {
                    numDouble++;
                    pointObVar.setDataType(DataType.DOUBLE);
                    dataVars.add(pointObVar);
                } else if(field.isTypeString()) {
                    numString++;
                    pointObVar.setDataType(DataType.STRING);
                    pointObVar.setLen(MAX_STRING_LENGTH);
                    stringVars.add(pointObVar);
                }
            }
            dataVars.addAll(stringVars);
            dvals = new double[numDouble];
            svals = new String[numString];
            cacheRecord= new PointDataRecord((RecordFile)null);
            tmpFile = getHandler().getStorageManager().getTmpFile(null, "tmp.nc");
            tmpFileIO = new RecordIO(getHandler().getStorageManager().getFileOutputStream(tmpFile));
            cacheRecord.dvalsSize = dvals.length;
            cacheRecord.svalsSize = svals.length;
    }


    public boolean doVisitRecord(RecordFile file,
                                 VisitInfo visitInfo, Record record)
        throws Exception {
        if (tmpFileIO == null) {
            init(file, record);
        }
        if ( !getHandler().jobOK(getProcessId())) {
            return false;
        }
        int dcnt = 0;
        int scnt = 0;
        PointRecord pointRecord = (PointRecord) record;
        for (RecordField field : fields) {
            if(field.isTypeNumeric()) {
                dvals[dcnt++] = record.getValue(field.getParamId());
            } else if(field.isTypeString()) {
                String s = record.getStringValue(field.getParamId());
                if(s.length()>MAX_STRING_LENGTH) {
                    s = s.substring(0, MAX_STRING_LENGTH);
                }
                svals[scnt++] = s;
            }
        }
        recordCnt++;
        cacheRecord.setLatitude(pointRecord.getLatitude());
        cacheRecord.setLongitude(pointRecord.getLongitude());
        cacheRecord.setAltitude(pointRecord.getAltitude());
        if(hasTime) {
            cacheRecord.setTime(record.getRecordTime());
        } else {
            cacheRecord.setTime(now.getTime());
        }
        cacheRecord.setDvals(dvals);
        cacheRecord.setSvals(svals);
        cacheRecord.write(tmpFileIO);
        return true;
    }


    @Override
        public void close(VisitInfo visitInfo) {
        try {
            if(tmpFileIO==null) return; 
            tmpFileIO.close();
            List<Attribute> globalAttributes = new ArrayList<Attribute>();
            DataOutputStream dos = getTheDataOutputStream();
            writer = new CFPointObWriter(dos, globalAttributes,"m", dataVars, recordCnt);
            tmpFileIO = new RecordIO(getHandler().getStorageManager().getFileInputStream(tmpFile));
            System.err.println ("writing " + recordCnt);
            for(int i=0;i<recordCnt;i++) {
                cacheRecord.read(tmpFileIO);
                writer.addPoint(cacheRecord.getLatitude(),
                                cacheRecord.getLongitude(),
                                cacheRecord.getAltitude(),
                                new Date(cacheRecord.getTime()),
                                cacheRecord.getDvals(), 
                                cacheRecord.getSvals());
            }
            writer.finish();
        } catch(Exception exc) {
            throw new RuntimeException(exc);
        }
        super.close(visitInfo);
    }
}
