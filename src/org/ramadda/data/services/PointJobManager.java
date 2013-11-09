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


import org.ramadda.data.record.*;
import org.ramadda.data.record.filter.*;
import org.ramadda.data.services.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.job.JobInfo;

import org.ramadda.repository.job.JobManager;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.TypeHandler;

import org.w3c.dom.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;



/**
 */
public class PointJobManager extends RecordJobManager {


    /**
     * ctor
     *
     * @param pointOutputHandler the output handler
     */
    public PointJobManager(PointOutputHandler pointOutputHandler) {
        super(pointOutputHandler);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public PointOutputHandler getPointOutputHandler() {
        return (PointOutputHandler) getRecordOutputHandler();
    }


    /**
     * _more_
     *
     * @param dummy _more_
     *
     * @return _more_
     */
    public String makeJobUrl(Request dummy) {
        dummy.remove(ARG_GETDATA);
        dummy.remove(ARG_RECORDENTRY);
        dummy.remove(ARG_RECORDENTRY_CHECK);
        dummy.remove("Boxes");
        List<String> products = (List<String>) dummy.get(ARG_PRODUCT,
                                    new ArrayList<String>());
        //If its just csv then get rid of the grid parameters
        if ((products.size() == 1) && (products.get(0).equals(
                getPointOutputHandler().OUTPUT_CSV.getId()) || products.get(
                0).equals(
                getPointOutputHandler().OUTPUT_LATLONALTCSV.getId()))) {
            for (String gridArg : new String[] {
                ARG_WIDTH, ARG_HEIGHT, ARG_COLORTABLE, ARG_GRID_RADIUS_CELLS,
                ARG_GRID_RADIUS_DEGREES, ARG_HILLSHADE_AZIMUTH,
                ARG_HILLSHADE_ANGLE, ARG_GRID_RADIUS_DEGREES_ORIG,
            }) {
                dummy.remove(gridArg);
            }
        }

        for (Enumeration keys = dummy.keys(); keys.hasMoreElements(); ) {
            String arg = (String) keys.nextElement();
            if (arg.startsWith("job.")) {
                dummy.remove(arg);
            }
            if (arg.startsWith("OpenLayers")) {
                dummy.remove(arg);
            }
        }

        return super.makeJobUrl(dummy);
    }



}
