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


import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.binary.*;
import org.ramadda.data.record.filter.*;


import org.ramadda.repository.*;

import ucar.unidata.util.IOUtil;

import java.io.File;

import java.util.concurrent.*;


/**
 * This is a wrapper around  ramadda Entry and a RecordFile
 *
 *
 */
public class PointEntry extends RecordEntry {

    /** This points to the  short lat/lon/alt binary file ramadda creates on the fly */
    private PointFile binaryPointFile;



    /**
     * ctor
     *
     * @param outputHandler _more_
     * @param request the request
     * @param entry the entry
     */
    public PointEntry(PointOutputHandler outputHandler, Request request,
                      Entry entry) {
        super(outputHandler, request, entry);
    }



    public PointOutputHandler getPointOutputHandler() {
        return (PointOutputHandler) getPointOutputHandler();
    }

    public PointFile getPointFile() throws Exception {
        return (PointFile) getRecordFile();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public File getQuickScanFile() {
        File entryDir = getOutputHandler().getStorageManager().getEntryDir(
                            getEntry().getId(), true);
        File quickscanFile = new File(IOUtil.joinDir(entryDir,
                                 "lightweight.llab"));

        return quickscanFile;
    }



    /**
     * apply the visitor to the lidarfile
     *
     * @param visitor visitor
     * @param visitInfo visit info
     *
     * @throws Exception On badness
     */
    public void visit(RecordVisitor visitor, VisitInfo visitInfo)
            throws Exception {
        if ((visitInfo != null) && visitInfo.getQuickScan()) {
            PointFile quickscanFile = getBinaryPointFile();
            System.err.println("POINT: Using quick scan file #records = "
                               + quickscanFile.getNumRecords());
            quickscanFile.visit(visitor, visitInfo, getFilter());
            return;
        }
        super.visit(visitor, visitInfo);
    }



    /**
     * get the Lidar File for the short lat/lon/alt binary file
     *
     * @return short binary file
     *
     * @throws Exception On badness
     */
    public PointFile getBinaryPointFile() throws Exception {
        PointFile pointFile = getPointFile();
        if (pointFile instanceof DoubleLatLonAltBinaryFile) {
            return pointFile;
        }
        if (binaryPointFile == null) {
            File entryDir =
                getOutputHandler().getStorageManager().getEntryDir(
                    getEntry().getId(), true);
            File quickscanFile = getQuickScanFile();
            if ( !quickscanFile.exists()) {
                //Write to a tmp file and only move it over when we are done
                File tmpFile = new File(IOUtil.joinDir(entryDir,
                                   "lightweight.llab.tmp"));
                pointFile.setDefaultSkip(0);
                System.err.println("POINT: making quickscan file ");
                getPointOutputHandler().writeBinaryFile(quickscanFile,
                        pointFile);
                tmpFile.renameTo(quickscanFile);
                System.err.println("POINT: done making quickscan file");
            }

            binaryPointFile =
                new DoubleLatLonAltBinaryFile(quickscanFile.toString());
        }

        return binaryPointFile;
    }



}
