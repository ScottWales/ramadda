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

package org.ramadda.data.point.ocean;


import org.ramadda.data.point.*;
import org.ramadda.data.point.netcdf.*;



import org.ramadda.data.record.*;
import org.ramadda.util.Utils;

import ucar.unidata.util.Misc;

import java.io.*;

import java.util.ArrayList;
import java.util.List;



/**
 * Class description
 *
 *
 * @version        $version$, Wed, Nov 13, '13
 * @author         Enter your name here...
 */
public class NetcdfOceanTrajectoryFile extends NetcdfTrajectoryFile {

    /**
     * ctor
     */
    public NetcdfOceanTrajectoryFile() {}




    /**
     * ctor
     *
     *
     *
     *
     *
     * @param filename _more_
     * @throws IOException On badness
     */
    public NetcdfOceanTrajectoryFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {
        super.prepareToVisit(visitInfo);
        //Need to find the platform or instrument property
        String platform = Misc.getProperty(getFileProperties(), "platform",
                                           "");

        //LOOK: this needs to be in the same order as the oceantypes.xml defines in the point plugin
        setFileMetadata(new Object[] { platform });

        return visitInfo;
    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        PointFile.test(args, NetcdfOceanTrajectoryFile.class);
    }





}
