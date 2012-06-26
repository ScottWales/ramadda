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

package org.ramadda.geodata.cdmdata;


import opendap.dap.DAP2Exception;

import opendap.servlet.GuardedDataset;
import opendap.servlet.ReqState;

//import ucar.nc2.dt.PointObsDataset;
//import ucar.nc2.dt.PointObsDatatype;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;

import org.jfree.chart.*;
import org.jfree.chart.annotations.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.entity.*;
import org.jfree.chart.event.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.*;
import org.jfree.data.general.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;
import org.jfree.ui.*;


import org.ramadda.repository.*;
import org.ramadda.repository.admin.AdminHandler;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;

import org.ramadda.repository.type.TypeHandler;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.ObjectPool;



import org.ramadda.util.TempDir;

import org.w3c.dom.*;



import thredds.server.ncSubset.GridPointWriter;
import thredds.server.ncSubset.QueryParams;

import thredds.server.opendap.GuardedDatasetImpl;



import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.StructureData;
import ucar.ma2.StructureMembers;

import ucar.nc2.Attribute;

import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;

import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dataset.VariableEnhanced;



import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.TrajectoryObsDataset;
import ucar.nc2.dt.TrajectoryObsDatatype;
import ucar.nc2.dt.TypedDatasetFactory;

import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.dt.grid.NetcdfCFWriter;
import ucar.nc2.dt.trajectory.TrajectoryObsDatasetFactory;

import ucar.nc2.ft.FeatureCollection;

import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.NestedPointFeatureCollection;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureCollection;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.point.*;

import ucar.nc2.ncml.NcMLWriter;
import ucar.nc2.units.DateFormatter;
import ucar.nc2.units.DateType;
import ucar.nc2.util.DiskCache2;

import ucar.unidata.data.gis.KmlUtil;
import ucar.unidata.geoloc.LatLonPointImpl;

import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.ui.ImageUtils;

import ucar.unidata.util.Cache;
import ucar.unidata.util.Counter;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.GuiUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.util.WrapperException;
import ucar.unidata.xml.XmlUtil;

import ucar.visad.Util;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.*;

import java.net.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.*;

import javax.servlet.*;

import javax.servlet.http.*;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class CdmManager extends RepositoryManager {

    /** NCML suffix */
    public static final String SUFFIX_NCML = ".ncml";

    /** GrADS CTL suffix */
    public static final String SUFFIX_CTL = ".ctl";



    /** CDM Type */
    public static final String TYPE_CDM = "cdm";

    /** GRID type */
    public static final String TYPE_GRID = "grid";

    /** TRAJECTORY type */
    public static final String TYPE_TRAJECTORY = "trajectory";

    /** POINT_TYPE */
    public static final String TYPE_POINT = "point";

    /** GrADS type */
    public static final String TYPE_GRADS = "gradsbinary";

    /** set of suffixes */
    private HashSet<String> suffixSet;

    /** hash of patterns */
    private Hashtable<String, List<Pattern>> patterns;

    /** not patterns */
    private Hashtable<String, List<Pattern>> notPatterns;


    /** cdm cache */
    private Cache<String, Boolean> cdmEntries = new Cache<String,
                                                    Boolean>(5000);

    /** grid entries cache */
    private Cache<String, Boolean> gridEntries = new Cache<String,
                                                     Boolean>(5000);


    /** point entries cache */
    private Cache<String, Boolean> pointEntries = new Cache<String,
                                                      Boolean>(5000);

    /** trajectory entries cache */
    private Cache<String, Boolean> trajectoryEntries = new Cache<String,
                                                           Boolean>(5000);


    /** nj cache directory */
    private TempDir nj22Dir;

    /** data cache directory */
    private TempDir dataCacheDir;


    //TODO: When we close a ncfile some thread might be using it
    //Do we have to actually close it??

    /** nc counter */
    Counter ncCounter = new Counter();

    /** nc create counter */
    Counter ncCreateCounter = new Counter();

    /** nc remove counter */
    Counter ncRemoveCounter = new Counter();

    /** nc get counter */
    Counter ncGetCounter = new Counter();

    /** nc put counter */
    Counter ncPutCounter = new Counter();

    /** ext counter */
    Counter extCounter = new Counter();

    /** opendap counter */
    Counter opendapCounter = new Counter();

    /** grid open counter */
    Counter gridOpenCounter = new Counter();

    /** grid close counter */
    Counter gridCloseCounter = new Counter();


    /** point open counter */
    Counter pointOpenCounter = new Counter();

    /** point close counter */
    Counter pointCloseCounter = new Counter();

    /** _more_          */
    public static final String CDMMANAGER_ID = "cdmmanager";


    /**
     * Create a new CdmManager
     *
     * @param repository  the repository
     * @param name        the name of this handler
     *
     * @throws Exception problem creating class
     */
    public CdmManager(Repository repository) throws Exception {
        super(repository);
        //TODO: what other global configuration should be done?
        nj22Dir = getRepository().getStorageManager().makeTempDir("nj22");
        nj22Dir.setMaxFiles(500);

        // Apply settings for the NetcdfDataset
        //        ucar.nc2.dataset.NetcdfDataset.setHttpClient(getRepository().getHttpClient());


        // Apply settings for the opendap.dap
        //        opendap.dap.DConnect2.setHttpClient(getRepository().getHttpClient());

        //Set the temp file and the cache policy
        ucar.nc2.util.DiskCache.setRootDirectory(nj22Dir.getDir().toString());
        ucar.nc2.util.DiskCache.setCachePolicy(true);
        //        ucar.nc2.iosp.grib.GribServiceProvider.setIndexAlwaysInCache(true);
        ucar.nc2.iosp.grid.GridServiceProvider.setIndexAlwaysInCache(true);

        dataCacheDir =
            getRepository().getStorageManager().makeTempDir("visaddatacache");
        dataCacheDir.setMaxFiles(2000);

        NetcdfDataset.disableNetcdfFileCache();


        visad.SampledSet.setCacheSizeThreshold(10000);
        visad.util.ThreadManager.setGlobalMaxThreads(4);
        visad.data.DataCacheManager.getCacheManager().setCacheDir(
            dataCacheDir.getDir());
        visad.data.DataCacheManager.getCacheManager().setMemoryPercent(0.1);
    }


    /** nc dataset pool */
    private ObjectPool<String, NetcdfDataset> ncDatasetPool =
        new ObjectPool<String, NetcdfDataset>(10) {
        protected void removeValue(String key, NetcdfDataset dataset) {
            try {
                super.removeValue(key, dataset);
                ncRemoveCounter.incr();
                dataset.close();
            } catch (Exception exc) {
                System.err.println("Error closing:" + key);
                exc.printStackTrace();
            }
        }

        /*
          public synchronized void put(String key, NetcdfDataset file) {
            ncPutCounter.incr();
            super.put(key, file);
            }*/

        protected NetcdfDataset getFromPool(List<NetcdfDataset> list) {
            NetcdfDataset dataset = super.getFromPool(list);
            ncGetCounter.incr();
            try {
                dataset.sync();

                return dataset;
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }


        protected NetcdfDataset createValue(String path) {
            try {
                getStorageManager().dirTouched(nj22Dir, null);
                NetcdfDataset dataset = NetcdfDataset.openDataset(path);
                //                NetcdfDataset dataset = NetcdfDataset.openFile(path);
                ncCreateCounter.incr();

                return dataset;
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }
    };


    /** nc file pool */
    private ObjectPool<String, NetcdfFile> ncFilePool =
        new ObjectPool<String, NetcdfFile>(10) {
        protected void removeValue(String key, NetcdfFile ncFile) {
            try {
                super.removeValue(key, ncFile);
                ncRemoveCounter.incr();
                ncFile.close();
            } catch (Exception exc) {
                System.err.println("Error closing:" + key);
                exc.printStackTrace();
            }
        }

        /*
        public synchronized void put(String key, NetcdfFile ncFile) {
            ncPutCounter.incr();
            super.put(key, ncFile);
            }*/

        protected NetcdfFile getFromPool(List<NetcdfFile> list) {
            NetcdfFile ncFile = super.getFromPool(list);
            ncGetCounter.incr();
            try {
                ncFile.sync();

                return ncFile;
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }

        protected NetcdfFile createValue(String path) {
            try {
                getStorageManager().dirTouched(nj22Dir, null);
                //                NetcdfDataset dataset = NetcdfDataset.openDataset(path);
                long       t1     = System.currentTimeMillis();
                NetcdfFile ncFile = NetcdfDataset.openFile(path, null);
                long       t2     = System.currentTimeMillis();
                System.err.println("NetcdfDataset.openFile: time:"
                                   + (t2 - t1));




                ncCreateCounter.incr();

                return ncFile;
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }
    };

    /** grid pool flag */
    private boolean doGridPool = true;

    /** grid pool */
    private ObjectPool<String, GridDataset> gridPool = new ObjectPool<String,
                                                           GridDataset>(10) {
        protected void removeValue(String key, GridDataset dataset) {
            try {
                super.removeValue(key, dataset);
                gridCloseCounter.incr();
                dataset.close();
            } catch (Exception exc) {}
        }

        protected GridDataset getFromPool(List<GridDataset> list) {
            GridDataset dataset = super.getFromPool(list);
            try {
                dataset.sync();

                return dataset;
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }

        }


        protected GridDataset createValue(String path) {
            try {
                getStorageManager().dirTouched(nj22Dir, null);
                gridOpenCounter.incr();
                long        t1  = System.currentTimeMillis();
                GridDataset gds = GridDataset.open(path);
                long        t2  = System.currentTimeMillis();
                System.err.println("GridDataset.open  time:" + (t2 - t1));
                if (gds.getGrids().iterator().hasNext()) {
                    return gds;
                } else {
                    gridCloseCounter.incr();
                    gds.close();

                    return null;
                }
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }
    };

    /**
     * _more_
     *
     * @param location _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public NetcdfFile createNetcdfFile(String location) throws Exception {
        return ncFilePool.get(location);
    }

    /**
     * _more_
     *
     * @param location _more_
     * @param ncf _more_
     *
     * @throws Exception _more_
     */
    public void returnNetcdfFile(String location, NetcdfFile ncf)
            throws Exception {
        ncFilePool.put(location, ncf);
    }


    /**
     * Create the GridDataset from the file
     *
     * @param path file path
     *
     * @return  the GridDataset
     */
    public GridDataset createGrid(String path) {
        try {
            getStorageManager().dirTouched(nj22Dir, null);
            //            gridOpenCounter.incr();

            GridDataset gds = GridDataset.open(path);
            if (gds.getGrids().iterator().hasNext()) {
                return gds;
            } else {
                //                gridCloseCounter.incr();
                gds.close();

                return null;
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }



    /** point pool */
    private ObjectPool<String, FeatureDatasetPoint> pointPool =
        new ObjectPool<String, FeatureDatasetPoint>(10) {
        protected void removeValue(String key, FeatureDatasetPoint dataset) {
            try {
                super.removeValue(key, dataset);
                dataset.close();
            } catch (Exception exc) {}
        }

        /*
        protected  FeatureDatasetPoint getFromPool(List<FeatureDatasetPoint> list) {
            FeatureDatasetPoint dataset = super.getFromPool(list);
            dataset.sync();
            return dataset;
            }*/

        protected FeatureDatasetPoint createValue(String path) {
            try {
                Formatter buf = new Formatter();
                getStorageManager().dirTouched(nj22Dir, null);

                FeatureDatasetPoint pods =
                    (FeatureDatasetPoint) FeatureDatasetFactoryManager.open(
                        ucar.nc2.constants.FeatureType.POINT, path, null,
                        buf);
                if (pods == null) {  // try as ANY_POINT
                    pods = (FeatureDatasetPoint) FeatureDatasetFactoryManager
                        .open(ucar.nc2.constants.FeatureType.ANY_POINT, path,
                              null, buf);
                }

                return pods;
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }


    };


    /** trajectory pool */
    private ObjectPool<String, TrajectoryObsDataset> trajectoryPool =
        new ObjectPool<String, TrajectoryObsDataset>(10) {
        protected void removeValue(String key, TrajectoryObsDataset dataset) {
            try {
                super.removeValue(key, dataset);
                dataset.close();
            } catch (Exception exc) {}
        }

        /*
        protected  TrajectoryObsDataset getFromPool(List<TrajectoryObsDataset> list) {
            TrajectoryObsDataset dataset = super.getFromPool(list);
            dataset.sync();
            return dataset;
            }*/

        protected TrajectoryObsDataset createValue(String path) {
            try {
                getStorageManager().dirTouched(nj22Dir, null);

                //                System.err.println("track:" + path);
                TrajectoryObsDataset dataset =
                    (TrajectoryObsDataset) TypedDatasetFactory.open(
                        FeatureType.TRAJECTORY, path, null,
                        new StringBuilder());

                //                System.err.println("Create trajectoryPool: " + path);
                //                System.err.println("got it? " + (dataset!=null));
                return dataset;
            } catch (Exception exc) {
                //                System.err.println("oops");
                throw new RuntimeException(exc);
            }
        }


    };



    /**
     * Get the system stats
     *
     * @param sb  the stats
     */
    public void getSystemStats(StringBuffer sb) {
        //        super.getSystemStats(sb);
        StringBuffer poolStats = new StringBuffer("<pre>");
        ncFilePool.getStats(poolStats);
        ncDatasetPool.getStats(poolStats);
        poolStats.append("</pre>");
        sb.append(
            HtmlUtils.formEntryTop(
                "Data Cache Size:",
                "NC File Pool:" + ncFilePool.getSize()
                + " have ncfile cache:"
                + (NetcdfDataset.getNetcdfFileCache() != null) + " "
                + " Count:  Create:" + ncCreateCounter.getCount()
                + " Remove:" + ncRemoveCounter.getCount() + "<br>" + " Get:"
                + ncGetCounter.getCount() + " Put:" + ncPutCounter.getCount()
                + "<br>" + " Ext Count:" + extCounter.getCount()
                + " Dap Count:" + opendapCounter.getCount() + poolStats
                + HtmlUtils.br() + "Grid Pool:" + gridPool.getSize()
                + HtmlUtils.br() + "Point Pool:" + pointPool.getSize()
                + HtmlUtils.br() + "Trajectory Pool:"
                + trajectoryPool.getSize() + HtmlUtils.br()));

    }


    /**
     * clear the cache
     */
    public void clearCache() {
        ncFilePool.clear();
        ncDatasetPool.clear();
        gridPool.clear();
        pointPool.clear();
        trajectoryPool.clear();

        cdmEntries.clear();
        gridEntries.clear();
        pointEntries.clear();
        trajectoryEntries.clear();
    }

    /**
     * Check to see if an Entry is an aggregation
     *
     * @param entry  the Entry
     *
     * @return  true if an aggregation
     */
    public boolean isAggregation(Entry entry) {
        return entry.getType().equals(
            GridAggregationTypeHandler.TYPE_GRIDAGGREGATION);
    }



    /**
     * Check if we can load the Entry
     *
     * @param entry  the Entry
     *
     * @return true if we can load it
     */
    private boolean canLoadEntry(Entry entry) {
        String url = entry.getResource().getPath();
        if (url == null) {
            return false;
        }
        if (url.endsWith("~")) {
            return false;
        }
        if (url.endsWith("#")) {
            return false;
        }
        if (entry.isGroup()) {
            return false;
        }

        if (entry.getResource().isRemoteFile()) {
            return true;
        }

        if (entry.getResource().isFileType()) {
            return entry.getFile().exists();
        }
        if ( !entry.getResource().isUrl()) {
            return false;
        }
        if (url.indexOf("dods") >= 0) {
            return true;
        }

        return true;
    }


    /**
     * Can the given entry be served by the tds
     *
     *
     * @param entry The entry
     *
     * @return Can the given entry be served by the tds
     */
    public boolean canLoadAsCdm(Entry entry) {
        if (entry.isType(OpendapLinkTypeHandler.TYPE_OPENDAPLINK)) {
            return true;
        }

        if (isGrads(entry)) {
            return true;
        }

        if (isAggregation(entry)) {
            return true;
        }
        if ( !entry.isFile()) {
            return false;
        }
        if (excludedByPattern(entry, TYPE_CDM)) {
            return false;
        }

        String[] types = { TYPE_CDM, TYPE_GRID, TYPE_TRAJECTORY, TYPE_POINT };
        for (int i = 0; i < types.length; i++) {
            if (includedByPattern(entry, types[i])) {
                return true;
            }
        }

        if (entry.getResource().isRemoteFile()) {
            String path = entry.getResource().getPath();
            if (path.endsWith(".nc")) {
                return true;
            }
        }

        Boolean b = (Boolean) cdmEntries.get(entry.getId());
        if (b == null) {
            boolean ok = false;
            if (canLoadEntry(entry)) {
                try {
                    String path = entry.getFile().toString();
                    //Exclude zip files becase canOpen tries to unzip them (?)
                    if ( !(path.endsWith(".zip"))) {
                        ok = NetcdfDataset.canOpen(path);
                    }
                } catch (Exception ignoreThis) {
                    //                    System.err.println("   error:" + ignoreThis);
                    //                    System.err.println("error:" + ignoreThis);
                }
            }
            b = new Boolean(ok);
            cdmEntries.put(entry.getId(), b);
        }

        return b.booleanValue();
    }

    /**
     *  Is this a GrADS entry
     *
     * @param e the Entry
     *
     * @return true if GrADS type
     */
    private boolean isGrads(Entry e) {
        return e.getType().equals(TYPE_GRADS);
    }

    /**
     * Can the Entry be loaded a point data?
     *
     * @param entry  the Entry
     *
     * @return true if can load as point
     */
    public boolean canLoadAsPoint(Entry entry) {
        if (excludedByPattern(entry, TYPE_POINT)) {
            return false;
        }
        if (includedByPattern(entry, TYPE_POINT)) {
            return true;
        }
        if ( !canLoadAsCdm(entry)) {
            return false;
        }

        Boolean b = (Boolean) pointEntries.get(entry.getId());
        if (b == null) {
            boolean ok = false;
            if ( !canLoadEntry(entry)) {
                ok = false;
            } else {
                try {
                    ok = pointPool.containsOrCreate(getPath(entry));
                } catch (Exception ignore) {}
            }
            pointEntries.put(entry.getId(), b = new Boolean(ok));
        }

        return b.booleanValue();
    }


    /**
     * Can the Entry be loaded as a trajectory?
     *
     * @param entry  the Entry
     *
     * @return  true if trajectory supported
     */
    public boolean canLoadAsTrajectory(Entry entry) {
        if (excludedByPattern(entry, TYPE_TRAJECTORY)) {
            return false;
        }
        if (includedByPattern(entry, TYPE_TRAJECTORY)) {
            return true;
        }

        if ( !canLoadAsCdm(entry)) {
            return false;
        }

        Boolean b = (Boolean) trajectoryEntries.get(entry.getId());
        if (b == null) {
            boolean ok = false;
            if (canLoadEntry(entry)) {
                try {
                    ok = trajectoryPool.containsOrCreate(getPath(entry));
                } catch (Exception ignoreThis) {}
            }
            trajectoryEntries.put(entry.getId(), b = new Boolean(ok));
        }

        return b.booleanValue();
    }



    /**
     * See if an Entry is excluded by pattern for a type
     *
     * @param entry   the Entry
     * @param type    the type to check
     *
     * @return true if excluded
     */
    private boolean excludedByPattern(Entry entry, String type) {
        return hasSuffixForType(entry, type, true);
    }

    /**
     * See if an Entry is included by pattern for a type
     *
     * @param entry   the Entry
     * @param type    the type to check
     *
     * @return true if included
     */
    private boolean includedByPattern(Entry entry, String type) {
        return hasSuffixForType(entry, type, false);
    }

    /**
     * See if the Entry has a suffix for this type
     *
     * @param entry  the Entry
     * @param type   the type
     * @param forNot true if not for that type
     *
     * @return  true if has suffix
     */
    private boolean hasSuffixForType(Entry entry, String type,
                                     boolean forNot) {
        String url = entry.getResource().getPath();
        if (url == null) {
            return false;
        }

        return hasSuffixForType(url, type, forNot);
    }

    /**
     * See if the URL has a suffix for this type
     *
     * @param url    the URL
     * @param type   the type
     * @param forNot true if not for that type
     *
     * @return  true if has suffix
     */
    private boolean hasSuffixForType(String url, String type,
                                     boolean forNot) {
        if (suffixSet == null) {
            HashSet<String> tmpSuffixSet = new HashSet<String>();

            Hashtable<String, List<Pattern>> tmpPatterns =
                new Hashtable<String, List<Pattern>>();
            Hashtable<String, List<Pattern>> tmpNotPatterns =
                new Hashtable<String, List<Pattern>>();




            String[] types = { TYPE_CDM, TYPE_GRID, TYPE_TRAJECTORY,
                               TYPE_POINT };
            for (int i = 0; i < types.length; i++) {
                List toks = StringUtil.split(
                                getRepository().getProperty(
                                    "ramadda.data." + types[i] + ".suffixes",
                                    ""), ",", true, true);
                for (String tok : (List<String>) toks) {
                    if ((tok.length() == 0) || tok.equals("!")) {
                        continue;
                    }
                    String key = types[i] + "." + tok;
                    tmpSuffixSet.add(key);
                }
            }



            for (int i = 0; i < types.length; i++) {
                tmpPatterns.put(types[i], new ArrayList<Pattern>());
                tmpNotPatterns.put(types[i], new ArrayList<Pattern>());
                List patterns = StringUtil.split(
                                    getRepository().getProperty(
                                        "ramadda.data." + types[i]
                                        + ".patterns", ""), ",", true, true);
                for (String pattern : (List<String>) patterns) {
                    if ((pattern.length() == 0) || pattern.equals("!")) {
                        continue;
                    }
                    Hashtable<String, List<Pattern>> tmp;
                    if (pattern.startsWith("!")) {
                        tmp     = tmpNotPatterns;
                        pattern = pattern.substring(1);
                    } else {
                        tmp = tmpPatterns;
                    }
                    tmp.get(types[i]).add(Pattern.compile(pattern));
                }
            }

            patterns    = tmpPatterns;
            notPatterns = tmpNotPatterns;
            suffixSet   = tmpSuffixSet;

        }

        url = url.toLowerCase();



        //First check the patterns
        List<Pattern> patternList = (forNot
                                     ? notPatterns.get(type)
                                     : patterns.get(type));
        for (Pattern pattern : patternList) {
            if (pattern.matcher(url).find()) {
                return true;
            }
        }


        String ext    = IOUtil.getFileExtension(url);
        String key    = type + "." + ext;
        String notKey = type + ".!" + ext;
        if (forNot) {
            if (suffixSet.contains(notKey)) {
                return true;
            }
        } else {
            if (suffixSet.contains(key)) {
                return true;
            }
        }


        return false;
    }

    /**
     * Check if this Entry can load as a grid
     *
     * @param entry  the Entry
     *
     * @return true if grid is supported
     */
    public boolean canLoadAsGrid(Entry entry) {
        if (isAggregation(entry)) {
            return true;
        }
        if (isGrads(entry)) {
            return true;
        }
        if (excludedByPattern(entry, TYPE_GRID)) {
            return false;
        }
        if (includedByPattern(entry, TYPE_GRID)) {
            return true;
        }
        if ( !canLoadAsCdm(entry)) {
            return false;
        }


        Boolean b = (Boolean) gridEntries.get(entry.getId());
        if (b == null) {
            boolean ok = false;
            if ( !canLoadEntry(entry)) {
                ok = false;
            } else {
                try {
                    if (doGridPool) {
                        ok = gridPool.containsOrCreate(getPath(entry));
                    } else {
                        ok = (createGrid(getPath(entry)) != null);
                    }
                } catch (Exception ignoreThis) {}
            }
            b = new Boolean(ok);
            gridEntries.put(entry.getId(), b);
        }

        return b.booleanValue();
    }


    /**
     * Get the NetcdfDataset for the Entry
     *
     * @param entry  the Entry
     * @param path   the path
     *
     * @return  the NetcdfDataset
     */
    public NetcdfDataset getNetcdfDataset(Entry entry, String path) {
        if ( !canLoadAsCdm(entry)) {
            return null;
        }
        extCounter.incr();

        return ncDatasetPool.get(path);
    }

    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     */
    public NetcdfDataset createNetcdfDataset(String path) {
        return ncDatasetPool.get(path);
    }


    /**
     * Return the NetcdfDataset
     *
     * @param path  the path
     * @param ncd   the NetcdfDataset
     */
    public void returnNetcdfDataset(String path, NetcdfDataset ncd) {
        extCounter.decr();
        ncDatasetPool.put(path, ncd);
    }


    /**
     * Get the Entry as a GridDataset
     *
     * @param entry  the Entry
     * @param path   the path
     *
     * @return  the GridDataset
     *
     * @throws Exception problems making GridDataset
     */
    public GridDataset getGridDataset(Entry entry, String path)
            throws Exception {
        if ( !canLoadAsGrid(entry)) {
            return null;
        }
        //Don't cache the aggregations
        //Not now...
        //        if (isAggregation(entry)) {
        //            return GridDataset.open(path);
        //        }
        if (doGridPool) {
            return gridPool.get(path);
        } else {
            return createGrid(path);
        }
    }

    /**
     * Return the GridDataset back to the pool
     *
     * @param path  the path
     * @param ncd   The GridDataset
     */
    public void returnGridDataset(String path, GridDataset ncd) {
        if (doGridPool) {
            gridPool.put(path, ncd);
        }
    }

    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     */
    public TrajectoryObsDataset getTrajectoryDataset(String path) {
        return trajectoryPool.get(path);
    }

    /**
     * _more_
     *
     * @param path _more_
     * @param tod _more_
     */
    public void returnTrajectoryDataset(String path,
                                        TrajectoryObsDataset tod) {
        trajectoryPool.put(path, tod);
    }


    /**
     * Get the Entry as a point dataset
     *
     * @param entry  the Entry
     * @param path   the path
     *
     * @return  the point dataset
     */
    public FeatureDatasetPoint getPointDataset(Entry entry, String path) {
        if ( !canLoadAsPoint(entry)) {
            return null;
        }

        return pointPool.get(path);
    }

    /**
     * Return the point dataset to the pool
     *
     * @param path  the path
     * @param ncd   the point dataset
     */
    public void returnPointDataset(String path, FeatureDatasetPoint ncd) {
        pointPool.put(path, ncd);
    }


    /**
     * Get the path to the data
     *
     * @param entry  the Entry
     *
     * @return the path
     *
     * @throws Exception problemo
     */
    public String getPath(Entry entry) throws Exception {
        return getPath(null, entry);
    }


    /**
     * Get the path for the Entry
     *
     *
     * @param request the Request
     * @param entry   the Entry
     *
     * @return   the path
     *
     * @throws Exception problem getting the path
     */
    public String getPath(Request request, Entry entry) throws Exception {
        String location;
        if (entry.getType().equals(OpendapLinkTypeHandler.TYPE_OPENDAPLINK)) {
            Resource resource = entry.getResource();
            location = resource.getPath();
            String ext = IOUtil.getFileExtension(location).toLowerCase();
            if (ext.equals(".html") || ext.equals(".das")
                    || ext.equals(".dds")) {
                location = IOUtil.stripExtension(location);
            }
        } else if (isAggregation(entry)) {
            GridAggregationTypeHandler gridAggregation =
                (GridAggregationTypeHandler) entry.getTypeHandler();
            long[] timestamp = { 0 };
            location = gridAggregation.getNcmlFile(request, entry,
                    timestamp).toString();
            // Something must be fixed to check if its empty
        } else {
            location = getStorageManager().getFastResourcePath(entry);
        }
        getStorageManager().checkPath(location);

        List<Metadata> metadataList =
            getMetadataManager().findMetadata(entry,
                ContentMetadataHandler.TYPE_ATTACHMENT, true);
        //        System.err.println("getPath");
        if (metadataList == null) {
            return location;
        }
        //        System.err.println("nd:" + metadataList);
        for (Metadata metadata : metadataList) {
            String  fileAttachment = metadata.getAttr1();
            boolean isNcml         = fileAttachment.endsWith(SUFFIX_NCML);
            boolean isCtl          = fileAttachment.endsWith(SUFFIX_CTL);
            if (isNcml || isCtl) {
                File templateNcmlFile =
                    new File(
                        IOUtil.joinDir(
                            getRepository().getStorageManager().getEntryDir(
                                metadata.getEntryId(),
                                false), metadata.getAttr1()));
                String ncml =
                    getStorageManager().readSystemResource(templateNcmlFile);
                if (isNcml) {
                    ncml = ncml.replace("${location}", location);
                } else {  // CTL
                    int dsetIdx = ncml.indexOf("${location}");
                    if (dsetIdx >= 0) {
                        ncml = ncml.replace("${location}", location);
                    } else {
                        ncml = ncml.replaceAll("\n(dset|DSET).*\n",
                                "\nDSET " + location + "\n");
                    }
                }
                //                System.err.println("ncml:" + ncml);
                //Use the last modified time of the ncml file so we pick up any updated file
                String dttm     = templateNcmlFile.lastModified() + "";
                String fileName = dttm + "_" + entry.getId() + "_"
                                  + metadata.getId() + (isNcml
                        ? SUFFIX_NCML
                        : SUFFIX_CTL);
                File ncmlFile = getStorageManager().getScratchFile(fileName);
                IOUtil.writeBytes(ncmlFile, ncml.getBytes());
                location = ncmlFile.toString();

                break;
            }
        }

        return location;
    }

    /**
     * Main for testing
     *
     * @param args  arguments for testing
     *
     * @throws Exception  problems
     */
    public static void main(String[] args) throws Exception {
        Repository repository = new Repository(new String[] {}, 8080);
        repository.initProperties(null);
        CdmDataOutputHandler dop = new CdmDataOutputHandler(repository, "test");
        CdmManager        cdmManager = new CdmManager(repository);
        String[] types = { TYPE_CDM, TYPE_GRID, TYPE_TRAJECTORY, TYPE_POINT };
        for (String f : args) {
            System.err.println("file:" + f);
            for (String type : types) {
                boolean ok      = cdmManager.hasSuffixForType(f, type, false);
                boolean exclude = cdmManager.hasSuffixForType(f, type, true);
                if ( !ok && !exclude) {
                    System.err.println("\t" + type + ": " + "unknown");
                } else {
                    System.err.println("\t" + type + ": " + "ok? " + ok
                                       + " exclude:" + exclude);
                }
            }
        }
    }



    /**
     * get the parameter name from the raw name
     *
     * @param rawname the raw name
     *
     * @return  the parameter name
     */
    public String getParamName(String rawname) {
        String name  = rawname;
        int    index = rawname.indexOf("[unit=");
        if (index >= 0) {
            name = rawname.substring(0, index);
        }

        return name;
    }

    /**
     * Get the parameter unit from the raw name
     *
     * @param rawname  the raw name
     *
     * @return  the unit or null
     */
    private String getUnitFromName(String rawname) {
        String unit  = null;
        int    index = rawname.indexOf("[unit=");
        if (index >= 0) {
            unit = rawname.substring(index + 6, rawname.indexOf("]"));
            unit = unit.replaceAll("\"", "");
        }

        return unit;
    }




}
