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
 * 
 */

package org.ramadda.data.tools;

import org.ramadda.data.point.*;


import org.ramadda.data.record.*;
import org.ramadda.util.grid.LatLonGrid;
import org.ramadda.util.grid.ObjectGrid;

import ucar.unidata.util.IOUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Wed, Feb 15, '12
 * @author         Enter your name here...
 */
public class CsvFileInfo extends PointFileInfo {

    /** _more_ */
    boolean wroteHeader = false;

    /** _more_ */
    VisitInfo visitInfo;

    /**
     * _more_
     *
     * @param outputFile _more_
     *
     * @throws Exception _more_
     */
    public CsvFileInfo(String outputFile, PointFile pointFile)
            throws Exception {
        super(outputFile, pointFile);
        visitInfo = new VisitInfo();
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void writeHeader() throws Exception {}

    /**
     * _more_
     *
     * @param record _more_
     *
     * @throws Exception _more_
     */
    public void writeRecord(Record record) throws Exception {
        if ( !wroteHeader) {
            record.printCsvHeader(visitInfo, recordOutput.getPrintWriter());
            wroteHeader = true;
        }
        record.printCsv(visitInfo, recordOutput.getPrintWriter());
    }
}
