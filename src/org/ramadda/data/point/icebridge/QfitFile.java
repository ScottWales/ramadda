/*
 * Copyright 2010 ramadda.org
 * http://ramadda.org
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


package org.ramadda.data.point.icebridge;

import org.ramadda.repository.RepositoryUtil;
import ucar.unidata.util.StringUtil;
import org.ramadda.data.record.*;
import java.io.*;
import java.util.Date;

import org.ramadda.data.point.PointFile;


public class QfitFile extends PointFile {

    public static final int TYPE_UNDEFINED = 0;
    public static final int TYPE_10WORD = 10;
    public static final int TYPE_12WORD = 12;
    public static final int TYPE_14WORD = 14;

    //Some files are little endian, some are big
    private boolean bigEndian = true;

    private int type = TYPE_UNDEFINED;

    //We extract this from the file name if possible
    private long  baseDate= 0L;

    public QfitFile()  {}

    public QfitFile(String filename) throws java.io.IOException {
        super(filename);
    }

    /**
     * This gets called before the file is visited. It determines the base date from the file name,
     * reads the record size values, figures out if the file is little endian and then reads through the
     * header.
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        //Get the date from the filename
        String dttm = StringUtil.findPattern(getFilename(),".*(\\d\\d\\d\\d\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d).*");
        if(dttm!=null) {
            try {
                Date date = RepositoryUtil.makeDateFormat("yyyyMMdd_HHmmss").parse(dttm);
                baseDate = date.getTime();
            } catch(Exception exc) {
                System.err.println("error:" + exc);
            }
        }

        DataInputStream dis = visitInfo.getRecordIO().getDataInputStream();
        //Check the type
        int recordSize = dis.readInt();
        type = recordSize/4;
        
        //If its unknown then see if its little endian
        if(type !=TYPE_10WORD  && type !=TYPE_12WORD && type !=TYPE_14WORD ) {
            visitInfo.getRecordIO().close();
            visitInfo.setRecordIO(doMakeInputIO(getSkip(visitInfo) == 0));
            dis = visitInfo.getRecordIO().getDataInputStream();
            bigEndian = false;
            recordSize = readInt(dis);
            type = recordSize/4;
            if(type !=TYPE_10WORD  && type !=TYPE_12WORD && type !=TYPE_14WORD ) {
                throw new IllegalArgumentException("Unknown record size:" + type +" Endian problem?");
            }
        }


        //Now read the rest of the header and count the header blocks
        int numHeaderRecords = 0;
        byte[] header = new byte[recordSize-4];
        dis.read(header);
        numHeaderRecords++;
        StringBuffer sb = new StringBuffer();
        while(true) {
            int size =  readInt(dis);
            if(size>=0) {
                dis.read(header);
                break;
            }
            dis.read(header);
            sb.append(new String(header));
            numHeaderRecords++;
        }

        //reset and read the header records
        visitInfo.setRecordIO(doMakeInputIO(getSkip(visitInfo) == 0));
        dis = visitInfo.getRecordIO().getDataInputStream();
        for(int i=0;i<numHeaderRecords;i++) {
            int size =  readInt(dis);
            dis.read(header);
        }
        return visitInfo;
    }


    /**
     * Create the record.
     */
    public Record doMakeRecord(VisitInfo visitInfo) {
        //If we haven't determined the type then explicitly call prepareToVisit
        if(type ==TYPE_UNDEFINED) {
            try {
                prepareToVisit(new VisitInfo(doMakeInputIO()));
            } catch(Exception exc) {
                throw new RuntimeException(exc);
            }
        }

        //Make the appropriate record, set its base date and return it
        QfitRecord record;
        if(type ==TYPE_10WORD) 
            record =  new QFit10WordRecord(this, bigEndian);
        else if(type ==TYPE_12WORD) 
            record = new QFit12WordRecord(this, bigEndian);
        else if(type ==TYPE_14WORD) 
            record =  new QFit14WordRecord(this, bigEndian);
        else 
            throw new IllegalArgumentException("Unknown type:" + type);
        record.setBaseDate(baseDate);
        return record;
    }


    public int readInt(DataInputStream dis) throws IOException {
        if(bigEndian) return dis.readInt();
        return readLEInt(dis);
    }

    public int readLEInt(DataInputStream dis) throws IOException {
        int accum = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
            accum |= (dis.readByte() & 0xff) << shiftBy;
        }
        return accum;
    }

    public static void main(String[]args) throws Exception {
        PointFile.test(args, QfitFile.class);
    }


}



