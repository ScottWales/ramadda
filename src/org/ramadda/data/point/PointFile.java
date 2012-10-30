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
import org.ramadda.data.record.filter.*;


import org.ramadda.util.GeoUtils;

import ucar.unidata.geoloc.*;
import ucar.unidata.geoloc.projection.*;

import ucar.unidata.util.IOUtil;


import java.awt.geom.*;
import java.io.*;


import java.util.Hashtable;
import java.util.Properties;



/**
 */
public abstract class PointFile extends RecordFile implements Cloneable {

    public static final String DFLT_PROPERTIES_FILE = "point.properties";

    public static final String ACTION_GRID = "action.grid";
    public static final String ACTION_DECIMATE = "action.decimate";
    public static final String ACTION_TRACKS = "action.tracks";
    public static final String ACTION_WAVEFORM = "action.waveform";
    public static final String ACTION_BOUNDINGPOLYGON = "action.boundingpolygon";


    private static final org.ramadda.data.point.LatLonPointRecord dummyField1 = null;


    /** _more_          */
    public static final String CRS_GEOGRAPHIC = "geographic";

    /** _more_          */
    public static final String CRS_UTM = "utm";

    public static final String CRS_EPSG = "epsg:";

    /** _more_          */
    public static final String CRS_WGS84 = "wgs84";
    public static final String CRS_ECEF = "ecef";

    /** _more_          */
    public static final String PROP_CRS = "crs";

    /** _more_          */
    public static final String PROP_DESCRIPTION = "description";

    /** _more_          */
    public static final String PROP_UTM_ZONE = "utm.zone";

    /** _more_          */
    public static final String PROP_UTM_NORTH = "utm.north";

    /** _more_          */
    public static final int IDX_LAT = 0;

    /** _more_          */
    public static final int IDX_LON = 1;

    /** _more_          */
    public static final int IDX_ALT = 2;


    /** _more_          */
    private String crs = CRS_GEOGRAPHIC;


    /** _more_          */
    boolean isGeographic = true;

    /** _more_          */
    boolean isUtm = false;

    /** _more_          */
    boolean isWgs84 = false;


    /** _more_          */
    private Projection projection;

    private  com.jhlabs.map.proj.Projection jhProjection;


    /** _more_          */
    private String description = "";

    /**
     * _more_
     */
    public PointFile() {}


    @Override
    public String getPropertiesFileName() {
        return DFLT_PROPERTIES_FILE;
    }


    /**
     * _more_
     *
     * @param properties _more_
     */
    public  PointFile(Hashtable properties) {
        super(properties);
    }


    /**
     * ctor
     *
     *
     * @param filename point data file
     *
     * @throws Exception On badness
     *
     * @throws IOException _more_
     */
    public  PointFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * ctor
     *
     *
     * @param filename point data file
     * @param properties _more_
     *
     * @throws Exception On badness
     *
     * @throws IOException _more_
     */
    public  PointFile(String filename, Hashtable properties)
            throws IOException {
        super(filename, properties);
    }

    public boolean isCapable(String action) {
        if(action.equals(ACTION_BOUNDINGPOLYGON)) return true;
        if(action.equals(ACTION_WAVEFORM)) return hasWaveform();
        return super.isCapable(action);
    }

    public boolean sameDataType(PointFile that) {
        return getClass().equals(that.getClass());
    }


    /**
     * _more_
     *
     * @param properties _more_
     */
    public void setProperties(Hashtable properties) {
        super.setProperties(properties);
        initProperties();
    }

    static int  printCnt = 0;
    /**
     * _more_
     */
    protected void initProperties() {
        description  = getProperty(PROP_DESCRIPTION, description);
        crs          = getProperty(PROP_CRS, crs);
        isGeographic = crs.equals(CRS_GEOGRAPHIC);
        isUtm        = crs.equals(CRS_UTM);
        isWgs84      = crs.equals(CRS_WGS84) || crs.equals(CRS_ECEF);

        //        crs =  "epsg:32611";
        if(crs.startsWith("epsg:")) {
            jhProjection = com.jhlabs.map.proj.ProjectionFactory.getNamedPROJ4CoordinateSystem(crs.substring(5).trim());
        }

        //        String parameters = getProperty( PROP_PARAMETERS,null);
        //        if(parameters==null) {
        //            throw new IllegalArgumentException("No parameters given in file " + propertiesFile);
        //        }
        if (isUtm) {
            String zoneString = getProperty(PROP_UTM_ZONE, null);
            if (zoneString == null) {
                throw new IllegalArgumentException("No " + PROP_UTM_ZONE
                        + " property given");
            }
            boolean isNorth = getProperty(PROP_UTM_NORTH,
                                          "true").trim().equals("true");
            //+proj=utm +zone=11 +south +ellps=WGS72 +units=m +no_defs

            projection = new UtmProjection(Integer.parseInt(zoneString),
                                           isNorth);

        } else {
            //            System.err.println("Unknown crs:" + crs);
        }
    }


    /** _more_          */
    static int cnt = 0;

    /**
     * _more_
     *
     * @param pointRecord _more_
     * @param y _more_
     * @param x _more_
     * @param z _more_
     * @param work _more_
     *
     * @return _more_
     */
    public double[] getLatLonAlt(PointRecord pointRecord, double y, double x,
                                 double z, double[] work) {
        if (work == null) {
            work = new double[3];
        }
        work[0] = y;
        work[1] = x;
        work[2] = z;
        if (isGeographic) {
            //Do nothing
        } else if(jhProjection !=null) {
            //TODO: keep src and dst around as class members?
            Point2D.Double src = new Point2D.Double(x,y);
            Point2D.Double dst = new Point2D.Double(0,0);
            dst = jhProjection.inverseTransform(src,dst);
            work[IDX_LON] = dst.getX();
            work[IDX_LAT] = dst.getY();
            /*
            if(printCnt==0) {
                System.out.println("x,y,lon,lat");
            }
            if(printCnt++<100) {
                System.out.println("" + x +", " +  y +", " + dst.getX() +", " + dst.getY());
            }
            */
        } else if (isUtm) {
            ProjectionPointImpl ppi  = pointRecord.getFromPoint();
            LatLonPointImpl     llpi = pointRecord.getToPoint();
            ppi.setLocation(x / 1000.0, y / 1000.0);
            llpi = (LatLonPointImpl) projection.projToLatLon(ppi, llpi);
            work[IDX_LON] = llpi.getLongitude();
            work[IDX_LAT] = llpi.getLatitude();
        } else if (isWgs84) {
            work = GeoUtils.wgs84XYZToLatLonAlt(x, y, z, work);
            if(cnt++<100) {
                //                System.err.println("elev:" + work[IDX_ALT]);
            }
        }
        work[IDX_LON] = GeoUtils.normalizeLongitude(work[IDX_LON]);
        return work;
    }


    public boolean isCRS3D() {
	return isWgs84;
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isGeographic() {
        return true;
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void printData() throws Exception {
        final PrintWriter pw = new PrintWriter(
                                   new BufferedOutputStream(
                                       new FileOutputStream("point.out"),
                                       100000));

        final int[]   cnt     = { 0 };
        RecordVisitor visitor = new RecordVisitor() {
            public boolean visitRecord(RecordFile file, VisitInfo visitInfo,
                                       Record record) {
                cnt[0]++;
                ((PointRecord) record).printCsv(visitInfo, pw);
                return true;
            }
        };

        visit(visitor);
        pw.close();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isTrajectory() {
        return false;
    }





    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {

        String epsg = "32610";
        epsg = "2955";
        epsg = "26711";
        //# NAD83 / UTM zone 11N
        epsg = "26911";
        com.jhlabs.map.proj.Projection    jhProjection = com.jhlabs.map.proj.ProjectionFactory.getNamedPROJ4CoordinateSystem(epsg);
        double x  = 414639.5382;
        double y = 4428236.0648;

        Point2D.Double src = new Point2D.Double(x,y);
        Point2D.Double dst =  jhProjection.inverseTransform(src,new Point2D.Double(0,0));
        UtmProjection projection = new UtmProjection(11, true);
        ProjectionPointImpl ppi  = new ProjectionPointImpl(x,  y);
        LatLonPointImpl     llpi = new LatLonPointImpl();
        ppi.setLocation(x / 1000.0, y / 1000.0);
        llpi = (LatLonPointImpl) projection.projToLatLon(ppi, llpi);
        System.err.println("result: " +llpi.getLatitude() + " " +
                           llpi.getLongitude());

        System.err.println("jhproj nad83 result: " +dst.getY() + " " + dst.getX());


        if(true) {
            return;
        }


        /*
        for (int argIdx = 0; argIdx < args.length; argIdx++) {
            String arg = args[argIdx];
            try {
                long        t1  = System.currentTimeMillis();
                final int[] cnt = { 0 };
                PointFile file = new PointFileFactory().doMakePointFile(arg,
                                     getPropertiesForFile(arg));
                final RecordVisitor metadata = new RecordVisitor() {
                    public boolean visitRecord(RecordFile file,
                            VisitInfo visitInfo, Record record) {
                        cnt[0]++;
                        PointRecord pointRecord = (PointRecord) record;
                        if ((pointRecord.getLatitude() < -90)
                                || (pointRecord.getLatitude() > 90)) {
                            System.err.println("Bad lat:"
                                    + pointRecord.getLatitude());
                        }
                        if ((cnt[0] % 100000) == 0) {
                            System.err.println(cnt[0] + " lat:"
                                    + pointRecord.getLatitude() + " "
                                    + pointRecord.getLongitude() + " "
                                    + pointRecord.getAltitude());

                        }
                        return true;
                    }
                };
                file.visit(metadata);
                long t2 = System.currentTimeMillis();
                System.err.println("time:" + (t2 - t1) / 1000.0
                                   + " # record:" + cnt[0]);
            } catch (Exception exc) {
                System.err.println("Error:" + exc);
                exc.printStackTrace();
            }
        }
        */

    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasWaveform() {
        String[] waveforms =  getWaveformNames();
        return waveforms!=null && waveforms.length>0;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String[] getWaveformNames() {
        return null;
    }

    /**
     * _more_
     *
     * @param pointIndex _more_
     * @param name _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Waveform getWaveform(int pointIndex, String name)
            throws Exception {
        PointRecord record = (PointRecord) getRecord(pointIndex);
        return record.getWaveform(name);
    }



}
