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

package org.ramadda.data.point;

import org.ramadda.data.record.*;

import java.io.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Jeff McWhirter
 */
public class LatLonFile extends RecordFile {

    private int numberOfExtraValues = 0;
    private boolean bigEndian = true;

    /**
     * ctor
     *
     *
     * @param filename lvis data file
     *
     * @throws Exception On badness
     *
     * @throws IOException _more_
     */
    public LatLonFile(String filename) throws IOException {
        super(filename);
    }

    public LatLonFile(String filename, int numberOfExtraValues) throws IOException {
        this(filename, 0,true);
    }

    public LatLonFile(String filename, int numberOfExtraValues, boolean bigEndian) throws IOException {
        super(filename);
        this.numberOfExtraValues = numberOfExtraValues;
        this.bigEndian = bigEndian;
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public Record doMakeRecord(VisitInfo visitInfo) {
	return null;
	//        return new LatLonPointRecord(true, numberOfExtraValues);
    }





    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
    }


}
