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

package org.ramadda.data.tools;


import org.ramadda.data.point.*;

import org.ramadda.data.record.*;
import org.ramadda.data.tools.*;

import java.io.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public class PointChecker extends RecordTool {

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public PointChecker(String[] args) throws Exception {
        super(null);
        //        super(Lidar2Csv.LIDAR_FACTORY_CLASS);
        long         total   = 0;
        List<String> argList = processArgs(args);
        for (int i = 0; i < argList.size(); i++) {
            String arg = argList.get(i);
            PointFile file =
                (PointFile) getRecordFileFactory().doMakeRecordFile(arg);
            PointMetadataHarvester visitor = new PointMetadataHarvester();
            file.visit(visitor, new VisitInfo(), null);
            System.err.println(
                args[i] + /*" " + file.getClass().getName() +*/ " #points:"
                + visitor.getCount() + " bounds:" + visitor.getMaxLatitude()
                + " " + visitor.getMinLongitude() + " "
                + visitor.getMinLatitude() + " " + visitor.getMaxLongitude()
                + " elevation:" + visitor.getMinElevation() + " "
                + visitor.getMaxElevation());
            StringBuffer buff = new StringBuffer();
            file.getInfo(buff);
            if (buff.length() > 0) {
                System.err.println(buff);
            }
            total += visitor.getCount();
        }
        System.err.println("total #points:" + total);
    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        new PointChecker(args);
    }
}
