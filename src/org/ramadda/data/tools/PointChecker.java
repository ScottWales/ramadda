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

import org.ramadda.data.record.*;
import org.ramadda.data.tools.*;
import org.ramadda.data.point.*;

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

    public PointChecker(String[]args) throws Exception {
        super(null);
        //        super(Lidar2Csv.LIDAR_FACTORY_CLASS);
        long total = 0;
        List<String> argList = processArgs(args);
        for (int i = 0; i < argList.size(); i++) {
            String arg  = argList.get(i);
            PointFile file = (PointFile) getRecordFileFactory().doMakeRecordFile(arg);
            PointMetadataHarvester visitor = new PointMetadataHarvester();
            file.visit(visitor, new VisitInfo(), null);
            System.err.println(
                args[i] + /*" " + file.getClass().getName() +*/ " #points:"
                + visitor.getCount() + " bounds:" + visitor.getMaxLatitude()
                + " " + visitor.getMinLongitude() + " "
                + visitor.getMinLatitude() + " " + visitor.getMaxLongitude() +
		" elevation:" + visitor.getMinElevation() +" " + visitor.getMaxElevation());
	    StringBuffer buff = new StringBuffer();
	    file.getInfo(buff);
	    if(buff.length()>0) {
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
