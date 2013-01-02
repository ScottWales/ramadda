
/*
 * Copyright 2010 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
 * http://www.unavco.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */



package org.ramadda.data.services;

import org.ramadda.data.record.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;





/** This is generated code from generate.tcl. Do not edit it! */
public class PointDataRecord extends Record {
    public static final int ATTR_FIRST = Record.ATTR_LAST;
    public static final List<RecordField> FIELDS = new ArrayList<RecordField>();
    public static final int ATTR_LATITUDE =  ATTR_FIRST + 1;
    public static final RecordField RECORDATTR_LATITUDE;
    public static final int ATTR_LONGITUDE =  ATTR_FIRST + 2;
    public static final RecordField RECORDATTR_LONGITUDE;
    public static final int ATTR_ALTITUDE =  ATTR_FIRST + 3;
    public static final RecordField RECORDATTR_ALTITUDE;
    public static final int ATTR_TIME =  ATTR_FIRST + 4;
    public static final RecordField RECORDATTR_TIME;
    public static final int ATTR_DVALS =  ATTR_FIRST + 5;
    public static final RecordField RECORDATTR_DVALS;
    public static final int ATTR_SVALS =  ATTR_FIRST + 6;
    public static final RecordField RECORDATTR_SVALS;
    public static final int ATTR_LAST = ATTR_FIRST + 7;
    

    static {
    FIELDS.add(RECORDATTR_LATITUDE = new RecordField("Latitude", "Latitude", "", ATTR_LATITUDE, "", "double", "double", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_LATITUDE.setValueGetter(new ValueGetter() {
    public double getValue(Record record, RecordField field, VisitInfo visitInfo) {
    return (double) ((PointDataRecord)record).Latitude;
    }
    public String getStringValue(Record record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((PointDataRecord)record).Latitude;
    }
    });
    FIELDS.add(RECORDATTR_LONGITUDE = new RecordField("Longitude", "Longitude", "", ATTR_LONGITUDE, "", "double", "double", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_LONGITUDE.setValueGetter(new ValueGetter() {
    public double getValue(Record record, RecordField field, VisitInfo visitInfo) {
    return (double) ((PointDataRecord)record).Longitude;
    }
    public String getStringValue(Record record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((PointDataRecord)record).Longitude;
    }
    });
    FIELDS.add(RECORDATTR_ALTITUDE = new RecordField("Altitude", "Altitude", "", ATTR_ALTITUDE, "", "double", "double", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_ALTITUDE.setValueGetter(new ValueGetter() {
    public double getValue(Record record, RecordField field, VisitInfo visitInfo) {
    return (double) ((PointDataRecord)record).Altitude;
    }
    public String getStringValue(Record record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((PointDataRecord)record).Altitude;
    }
    });
    FIELDS.add(RECORDATTR_TIME = new RecordField("Time", "Time", "", ATTR_TIME, "", "long", "long", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_TIME.setValueGetter(new ValueGetter() {
    public double getValue(Record record, RecordField field, VisitInfo visitInfo) {
    return (double) ((PointDataRecord)record).Time;
    }
    public String getStringValue(Record record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((PointDataRecord)record).Time;
    }
    });
    FIELDS.add(RECORDATTR_DVALS = new RecordField("Dvals", "Dvals", "", ATTR_DVALS, "", "double[getDvalsSize()]", "double", 0, SEARCHABLE_NO,CHARTABLE_NO));
    FIELDS.add(RECORDATTR_SVALS = new RecordField("Svals", "Svals", "", ATTR_SVALS, "", "String[getSvalsSize()]", "String", 0, SEARCHABLE_NO,CHARTABLE_NO));
    
    }
    

    double Latitude;
    double Longitude;
    double Altitude;
    long Time;
    double[] Dvals = null;
    String[] Svals = null;
    

    public  PointDataRecord(PointDataRecord that)  {
        super(that);
        this.Latitude = that.Latitude;
        this.Longitude = that.Longitude;
        this.Altitude = that.Altitude;
        this.Time = that.Time;
        this.Dvals = that.Dvals;
        this.Svals = that.Svals;
        
        
    }



    public  PointDataRecord(RecordFile file)  {
        super(file);
    }



    public  PointDataRecord(RecordFile file, boolean bigEndian)  {
        super(file, bigEndian);
    }



    public int getLastAttribute()  {
        return ATTR_LAST;
    }



    public  boolean equals(Object object)  {
        if(!super.equals(object)) {System.err.println("bad super"); return false;} if(!(object instanceof PointDataRecord)) return false;
        PointDataRecord that = (PointDataRecord ) object;
        if(this.Latitude!= that.Latitude) {System.err.println("bad Latitude");  return false;}
        if(this.Longitude!= that.Longitude) {System.err.println("bad Longitude");  return false;}
        if(this.Altitude!= that.Altitude) {System.err.println("bad Altitude");  return false;}
        if(this.Time!= that.Time) {System.err.println("bad Time");  return false;}
        if(!java.util.Arrays.equals(this.Dvals, that.Dvals)) {System.err.println("bad Dvals"); return false;}
        if(!java.util.Arrays.equals(this.Svals, that.Svals)) {System.err.println("bad Svals"); return false;}
        return true;
    }




    public int dvalsSize;
    public int svalsSize;
    public int getDvalsSize() {
        return dvalsSize;
    }
    public int getSvalsSize() {
        return svalsSize;
    }

    protected void addFields(List<RecordField> fields)  {
        super.addFields(fields);
        fields.addAll(FIELDS);
    }



    public double getValue(int attrId)  {
        if(attrId == ATTR_LATITUDE) return Latitude;
        if(attrId == ATTR_LONGITUDE) return Longitude;
        if(attrId == ATTR_ALTITUDE) return Altitude;
        if(attrId == ATTR_TIME) return Time;
        return super.getValue(attrId);
        
    }



    public int getRecordSize()  {
        return super.getRecordSize() + 32 + 0+0;
    }



    public ReadStatus read(RecordIO recordIO) throws IOException  {
        DataInputStream dis = recordIO.getDataInputStream();
        Latitude =  readDouble(dis);
        Longitude =  readDouble(dis);
        Altitude =  readDouble(dis);
        Time =  readLong(dis);
        if(Dvals==null || Dvals.length!=getDvalsSize()) Dvals = new double[getDvalsSize()];
        readDoubles(dis,Dvals);
        if(Svals==null || Svals.length!=getSvalsSize()) Svals = new String[getSvalsSize()];
        readStrings(dis,Svals);
        
        
        return ReadStatus.OK;
    }



    public void write(RecordIO recordIO) throws IOException  {
        DataOutputStream dos = recordIO.getDataOutputStream();
        writeDouble(dos, Latitude);
        writeDouble(dos, Longitude);
        writeDouble(dos, Altitude);
        writeLong(dos, Time);
        write(dos, Dvals);
        write(dos, Svals);
        
    }



    public int doPrintCsv(VisitInfo visitInfo,PrintWriter pw)  {
        boolean includeVector = visitInfo.getProperty(PROP_INCLUDEVECTOR, false);
        int superCnt = super.doPrintCsv(visitInfo,pw);
        int myCnt = 0;
        if(superCnt>0) pw.print(',');
        pw.print(Latitude);
        myCnt++;
        pw.print(',');
        pw.print(Longitude);
        myCnt++;
        pw.print(',');
        pw.print(Altitude);
        myCnt++;
        pw.print(',');
        pw.print(Time);
        myCnt++;
        if(includeVector) {
        for(int i=0;i<this.Dvals.length;i++) {pw.print(i==0?'|':',');pw.print(this.Dvals[i]);}
        myCnt++;
        }
        if(includeVector) {
        for(int i=0;i<this.Svals.length;i++) {pw.print(i==0?'|':',');pw.print(this.Svals[i]);}
        myCnt++;
        }
        return myCnt+superCnt;
        
    }



    public int doPrintCsvHeader(VisitInfo visitInfo,PrintWriter pw)  {
        int superCnt = super.doPrintCsvHeader(visitInfo,pw);
        int myCnt = 0;
        boolean includeVector = visitInfo.getProperty(PROP_INCLUDEVECTOR, false);
        if(superCnt>0) pw.print(',');
        RECORDATTR_LATITUDE.printCsvHeader(visitInfo,pw);
        myCnt++;
        pw.print(',');
        RECORDATTR_LONGITUDE.printCsvHeader(visitInfo,pw);
        myCnt++;
        pw.print(',');
        RECORDATTR_ALTITUDE.printCsvHeader(visitInfo,pw);
        myCnt++;
        pw.print(',');
        RECORDATTR_TIME.printCsvHeader(visitInfo,pw);
        myCnt++;
        if(includeVector) {
        pw.print(',');
        RECORDATTR_DVALS.printCsvHeader(visitInfo,pw);
        myCnt++;
        }
        if(includeVector) {
        pw.print(',');
        RECORDATTR_SVALS.printCsvHeader(visitInfo,pw);
        myCnt++;
        }
        return myCnt+superCnt;
        
    }



    public void print(Appendable buff)  throws Exception  {
        super.print(buff);
        buff.append(" Latitude: " + Latitude+" \n");
        buff.append(" Longitude: " + Longitude+" \n");
        buff.append(" Altitude: " + Altitude+" \n");
        buff.append(" Time: " + Time+" \n");
        
    }



    public double getLatitude()  {
        return Latitude;
    }


    public void setLatitude(double newValue)  {
        Latitude = newValue;
    }


    public double getLongitude()  {
        return Longitude;
    }


    public void setLongitude(double newValue)  {
        Longitude = newValue;
    }


    public double getAltitude()  {
        return Altitude;
    }


    public void setAltitude(double newValue)  {
        Altitude = newValue;
    }


    public long getTime()  {
        return Time;
    }


    public void setTime(long newValue)  {
        Time = newValue;
    }


    public double[] getDvals()  {
        return Dvals;
    }


    public void setDvals(double[] newValue)  {
        if(Dvals == null) Dvals = newValue; else copy(Dvals, newValue);
    }


    public String[] getSvals()  {
        return Svals;
    }


    public void setSvals(String[] newValue)  {
        if(Svals == null) Svals = newValue; else copy(Svals, newValue);
    }



}



