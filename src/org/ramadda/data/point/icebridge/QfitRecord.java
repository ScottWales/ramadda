
/*
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

import org.ramadda.data.record.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;





/** This is generated code from generate.tcl. Do not edit it! */
public class QfitRecord extends org.ramadda.data.point.PointRecord {

    long baseDate = 0;

    int relativeTime;
    int laserLatitude;
    int laserLongitude;
    int elevation;


    public  QfitRecord(QfitRecord that)  {
        super(that);
        this.relativeTime = that.relativeTime;
        this.laserLatitude = that.laserLatitude;
        this.laserLongitude = that.laserLongitude;
        this.elevation = that.elevation;
    }

    public  QfitRecord(RecordFile file)  {
        super(file);
    }

    public  QfitRecord(RecordFile file, boolean bigEndian)  {
        super(file, bigEndian);
    }

    @Override
    public long getRecordTime() {
        if(baseDate==0L) return super.getRecordTime();
        return baseDate+relativeTime;
    }

    public void setBaseDate(long l) {
        baseDate = l;
    }

    @Override
    public double getLatitude() {
        return laserLatitude/1000000.0;
    }

    @Override
    public double getLongitude() {
        return  org.ramadda.util.GeoUtils.normalizeLongitude(laserLongitude/1000000.0);
    }

    @Override
    public double getAltitude() {
        return elevation/1000.0;
    }



}



