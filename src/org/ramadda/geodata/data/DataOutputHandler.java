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

package org.ramadda.geodata.data;


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
import org.ramadda.repository.auth.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;

import org.ramadda.repository.type.TypeHandler;
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
import org.ramadda.util.HtmlUtils;
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
public class DataOutputHandler extends OutputHandler {

    /** _more_ */
    public static final String ICON_OPENDAP = "/data/opendap.gif";

    /** CSV format */
    public static final String FORMAT_CSV = "csv";

    /** KML format */
    public static final String FORMAT_KML = "kml";

    /** NCML format */
    public static final String FORMAT_NCML = "ncml";

    /** NCML suffix */
    public static final String SUFFIX_NCML = ".ncml";

    /** GrADS CTL suffix */
    public static final String SUFFIX_CTL = ".ctl";

    /** bounding box argument */
    public static final String ARG_POINT_BBOX = "bbox";

    /** Variable prefix */
    public static final String VAR_PREFIX = ARG_VARIABLE + ".";

    /** add lat lon argument */
    public static final String ARG_ADDLATLON = "addlatlon";


    /** horizontal stride */
    public static final String ARG_HSTRIDE = "hstride";

    /** level */
    public static final String ARG_LEVEL = "level";

    /** format */
    public static final String ARG_FORMAT = "format";

    /** format */
    public static final String ARG_IMAGE_WIDTH = "image_width";

    /** format */
    public static final String ARG_IMAGE_HEIGHT = "image_height";

    /** spatial arguments */
    private static final String[] SPATIALARGS = new String[] { ARG_AREA_NORTH,
            ARG_AREA_WEST, ARG_AREA_SOUTH, ARG_AREA_EAST, };

    /** chart format */
    private static final String FORMAT_TIMESERIES = "timeseries";

    /** chart format */
    private static final String FORMAT_TIMESERIES_CHART = "timeserieschart";

    /** chart format */
    private static final String FORMAT_TIMESERIES_CHART_DATA =
        "timeserieschartdata";

    /** chart image format */
    private static final String FORMAT_TIMESERIES_IMAGE = "timeseriesimage";

    /** Data group */
    public static final String GROUP_DATA = "Data";

    /** CDM Type */
    public static final String TYPE_CDM = "cdm";

    /** GRID type */
    public static final String TYPE_GRID = "grid";

    /** TRAJECTORY type */
    public static final String TYPE_TRAJECTORY = "trajectory";

    /** POINT_TYPE */
    public static final String TYPE_POINT = "point";

    /** GrADS type*/
    public static final String TYPE_GRADS = "gradsbinary";

    /** set of suffixes */
    private HashSet<String> suffixSet;

    /** hash of patterns */
    private Hashtable<String, List<Pattern>> patterns;

    /** not patterns */
    private Hashtable<String, List<Pattern>> notPatterns;

    /** OPeNDAP Output Type */
    public static final OutputType OUTPUT_OPENDAP =
        new OutputType("OPeNDAP", "data.opendap", OutputType.TYPE_OTHER,
                       OutputType.SUFFIX_NONE, ICON_OPENDAP, GROUP_DATA);

    /** CDL Output Type */
    public static final OutputType OUTPUT_CDL =
        new OutputType("File Metadata", "data.cdl", OutputType.TYPE_OTHER,
                       OutputType.SUFFIX_NONE, "/data/page_white_text.png", GROUP_DATA);

    /** WCS Output Type */
    public static final OutputType OUTPUT_WCS = new OutputType("WCS",
                                                    "data.wcs",
                                                    OutputType.TYPE_FEEDS);

    /** Point map Output Type */
    public static final OutputType OUTPUT_POINT_MAP =
        new OutputType("Plot Points on a Map", "data.point.map",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       ICON_MAP, GROUP_DATA);

    /** CSV Output Type */
    public static final OutputType OUTPUT_POINT_SUBSET =
        new OutputType("CSV, KML Output", "data.point.subset",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       ICON_CSV, GROUP_DATA);


    /** Trajectory map Output Type */
    public static final OutputType OUTPUT_TRAJECTORY_MAP =
        new OutputType("Trajectory as Map", "data.trajectory.map",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       ICON_MAP, GROUP_DATA);

    /** Grid subset form Output Type */
    public static final OutputType OUTPUT_GRIDSUBSET_FORM =
        new OutputType("Subset Grid", "data.gridsubset.form",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       ICON_SUBSET, GROUP_DATA);

    /** Grid subset Output Type */
    public static final OutputType OUTPUT_GRIDSUBSET =
        new OutputType("data.gridsubset", OutputType.TYPE_FEEDS);

    /** Grid as point form Output Type */
    public static final OutputType OUTPUT_GRIDASPOINT_FORM =
        new OutputType("Extract Time Series", "data.gridaspoint.form",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       "/data/chart_line.png", GROUP_DATA);

    /** Grid as point Output Type */
    public static final OutputType OUTPUT_GRIDASPOINT =
        new OutputType("data.gridaspoint", OutputType.TYPE_FEEDS);

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
     * Create the GridDataset from the file
     *
     * @param path file path
     *
     * @return  the GridDataset
     */
    private GridDataset createGrid(String path) {
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
     * Create a new DataOutputHandler
     *
     * @param repository  the repository
     * @param name        the name of this handler
     *
     * @throws Exception problem creating class
     */
    public DataOutputHandler(Repository repository, String name)
            throws Exception {
        super(repository, name);
    }

    /**
     *     Create a DataOutputHandler
     *
     *     @param repository  the repository
     *     @param element     the element
     *     @throws Exception On badness
     */
    public DataOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);

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

        addType(OUTPUT_OPENDAP);
        addType(OUTPUT_CDL);
        addType(OUTPUT_WCS);
        addType(OUTPUT_TRAJECTORY_MAP);
        addType(OUTPUT_POINT_MAP);
        addType(OUTPUT_POINT_SUBSET);
        addType(OUTPUT_GRIDSUBSET);
        addType(OUTPUT_GRIDSUBSET_FORM);
        addType(OUTPUT_GRIDASPOINT);
        addType(OUTPUT_GRIDASPOINT_FORM);
    }


    /**
     * Get the system stats
     *
     * @param sb  the stats
     */
    public void getSystemStats(StringBuffer sb) {
        super.getSystemStats(sb);
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
        super.clearCache();
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
     * Add to an entry
     *
     * @param request the request
     * @param entry  the entry
     * @param node   the node
     *
     * @throws Exception  on badness
     */
    public void addToEntryNode(Request request, Entry entry, Element node)
            throws Exception {
        super.addToEntryNode(request, entry, node);
        if ( !canLoadAsCdm(entry)) {
            return;
        }
        if ( !getRepository().getAccessManager().canAccessFile(request,
                entry)) {
            return;
        }
        String  url         = getAbsoluteOpendapUrl(request, entry);
        Element serviceNode = XmlUtil.create(TAG_SERVICE, node);
        XmlUtil.setAttributes(serviceNode, new String[] { ATTR_TYPE,
                SERVICE_OPENDAP, ATTR_URL, url });

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
     * Get the Entry links
     *
     * @param request  the request
     * @param state    the state
     * @param links    the links
     *
     * @throws Exception on badness
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {

        Entry entry = state.entry;

        if ((state.group != null) && isAggregation(state.group)) {
            entry = state.group;
        }


        if (entry == null) {
            return;
        }

        if ( !getRepository().getAccessManager().canAccessFile(request,
                entry)) {
            return;
        }

        long    t1           = System.currentTimeMillis();
        boolean canLoadAsCdm = canLoadAsCdm(entry);

        if ( !canLoadAsCdm) {
            long t2 = System.currentTimeMillis();
            if ((t2 - t1) > 1) {
                //                System.err.println("DataOutputHandler (cdm) getEntryLinks  "
                //                                   + entry.getName() + " time:" + (t2 - t1));
            }

            return;
        }

        if (canLoadAsGrid(entry)) {
            addOutputLink(request, entry, links, OUTPUT_GRIDSUBSET_FORM);
            addOutputLink(request, entry, links, OUTPUT_GRIDASPOINT_FORM);
        } else if (canLoadAsTrajectory(entry)) {
            addOutputLink(request, entry, links, OUTPUT_TRAJECTORY_MAP);
        } else if (canLoadAsPoint(entry)) {
            addOutputLink(request, entry, links, OUTPUT_POINT_MAP);
            addOutputLink(request, entry, links, OUTPUT_POINT_SUBSET);
        }

        Object oldOutput = request.getOutput();
        request.put(ARG_OUTPUT, OUTPUT_OPENDAP);
        String opendapUrl = getOpendapUrl(entry);
        links.add(new Link(opendapUrl, getRepository().iconUrl(ICON_OPENDAP),
                           "OPeNDAP", OUTPUT_OPENDAP));
        request.put(ARG_OUTPUT, oldOutput);


        Link cdlLink = makeLink(request, entry, OUTPUT_CDL);
        //        cdlLink.setLinkType(OutputType.TYPE_ACTION);
        links.add(cdlLink);
        long t2 = System.currentTimeMillis();
        if ((t2 - t1) > 1) {
            //            System.err.println("DataOutputHandler  getEntryLinks  "
            //                               + entry.getName() + " time:" + (t2 - t1));
        }
    }




    /**
     * Get the OPeNDAP URL
     *
     * @param entry the Entry
     *
     * @return  the URL as a string
     */
    public String getOpendapUrl(Entry entry) {
        return getOpendapHandler().getOpendapUrl(entry);
    }


    /**
     * Get the absolute OPeNDAP URL
     *
     *
     * @param request  the request
     * @param entry    the entry
     *
     * @return  the URL as a String
     */
    public String getAbsoluteOpendapUrl(Request request, Entry entry) {
        return getOpendapHandler().getAbsoluteOpendapUrl(request, entry);
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
     * Output the CDL for the Entry
     *
     * @param request   the Request
     * @param entry     the Entry
     *
     * @return the CDL Result
     *
     * @throws Exception problems
     */
    public Result outputCdl(final Request request, Entry entry)
            throws Exception {
        String path = getPath(request, entry);
        if (request.getString(ARG_FORMAT, "").equals(FORMAT_NCML)) {

            /**
             *  This gets hung up calling back into the repository
             *  so for now don't do it and just use the file
             */
            path = getAbsoluteOpendapUrl(request, entry);

            NetcdfFile ncFile = NetcdfDataset.openFile(path, null);
            NcMLWriter writer = new NcMLWriter();
            String     xml    = writer.writeXML(ncFile);
            Result result = new Result("", new StringBuffer(xml), "text/xml");
            ncFile.close();

            return result;
        }


        StringBuffer sb = new StringBuffer();
        if (request.get(ARG_METADATA_ADD, false)) {
            if (getRepository().getAccessManager().canDoAction(request,
                    entry, Permission.ACTION_EDIT)) {
                sb.append(HtmlUtils.p());
                List<Entry> entries = (List<Entry>) Misc.newList(entry);
                getEntryManager().addInitialMetadata(request, entries, false,
                        request.get(ARG_SHORT, false));
                getEntryManager().insertEntries(entries, false);
                sb.append(getRepository().showDialogNote("Properties added"));
                sb.append(
                    getRepository().getHtmlOutputHandler().getInformationTabs(
                        request, entry, false, false));

            } else {
                sb.append("You cannot add properties");
            }

            return makeLinksResult(request, "CDL", sb, new State(entry));
        }


        if (getRepository().getAccessManager().canDoAction(request, entry,
                Permission.ACTION_EDIT)) {
            request.put(ARG_METADATA_ADD, HtmlUtils.VALUE_TRUE);
            sb.append(
                HtmlUtils.href(
                    request.getUrl() + "&"
                    + HtmlUtils.arg(ARG_SHORT, HtmlUtils.VALUE_TRUE), msg(
                        "Add temporal and spatial properties")));
            sb.append(HtmlUtils.span("&nbsp;|&nbsp;",
                                    HtmlUtils.cssClass(CSS_CLASS_SEPARATOR)));

            sb.append(HtmlUtils.href(request.getUrl(),
                                    msg("Add full properties")));
            sb.append(HtmlUtils.span("&nbsp;|&nbsp;",
                                    HtmlUtils.cssClass(CSS_CLASS_SEPARATOR)));
        }
        String tail =
            IOUtil.stripExtension(getStorageManager().getFileTail(entry));

        sb.append(HtmlUtils.href(HtmlUtils.url(getRepository().URL_ENTRY_SHOW
                                             + "/" + tail
                                             + SUFFIX_NCML, new String[] {
            ARG_ENTRYID, entry.getId(), ARG_OUTPUT, OUTPUT_CDL.getId(),
            ARG_FORMAT, FORMAT_NCML
        }), "NCML"));


        NetcdfDataset dataset = ncDatasetPool.get(path);
        if (dataset == null) {
            sb.append("Could not open dataset");
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ucar.nc2.NCdump.print(dataset, "", bos, null);
            sb.append("<pre>" + bos.toString() + "</pre>");
            ncDatasetPool.put(path, dataset);
        }

        return makeLinksResult(request, "CDL", sb, new State(entry));
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
     * Output the Entry as a WCS result
     *
     * @param request  the Request
     * @param entry    the Entry
     *
     * @return  the Result
     */
    public Result outputWcs(Request request, Entry entry) {
        return new Result("", new StringBuffer("TBD"));
    }


    /**
     * Process a grid as point request
     *
     * @param request  the request
     * @param entry    the entry
     * @param gds      the corresponding grid dataset
     * @param sb       the StringBuffer
     *
     * @return a Result
     *
     * @throws Exception problem doing what was asked
     */
    public Result outputGridAsPointProcess(Request request, Entry entry,
                                           GridDataset gds, StringBuffer sb)
            throws Exception {

        List      varNames = new ArrayList<String>();
        Hashtable args     = request.getArgs();
        for (Enumeration keys = args.keys(); keys.hasMoreElements(); ) {
            String arg = (String) keys.nextElement();
            if (arg.startsWith(VAR_PREFIX) && request.get(arg, false)) {
                varNames.add(arg.substring(VAR_PREFIX.length()));
            }
        }
        //            System.err.println(varNames);
        LatLonRect llr    = gds.getBoundingBox();
        double     deflat = 0;
        double     deflon = 0;
        if (llr != null) {
            deflat = llr.getLatMin() + llr.getHeight() / 2;
            deflon = llr.getCenterLon();
        }
        LatLonPointImpl llp = null;

        if (request.get(ARG_LOCATION, true)) {
            llp = new LatLonPointImpl(
                request.getLatOrLonValue(ARG_LOCATION + ".latitude", deflat),
                request.getLatOrLonValue(
                    ARG_LOCATION + ".longitude", deflon));
        }
        double levelVal   = request.get(ARG_LEVEL, Double.NaN);

        int    timeStride = 1;
        Date[] dates      = new Date[] { request.defined(ARG_FROMDATE)
                                         ? request.getDate(ARG_FROMDATE, null)
                                         : null,
                                         request.defined(ARG_TODATE)
                                         ? request.getDate(ARG_TODATE, null)
                                         : null };
        //have to have both dates
        if ((dates[0] != null) && (dates[1] == null)) {
            dates[0] = null;
        }
        if ((dates[1] != null) && (dates[0] == null)) {
            dates[1] = null;
        }

        if ((dates[0] != null) && (dates[1] != null)
                && (dates[0].getTime() > dates[1].getTime())) {
            sb.append(
                getRepository().showDialogWarning(
                    "From date is after to date"));
        } else if (varNames.size() == 0) {
            sb.append(
                getRepository().showDialogWarning("No variables selected"));
        } else {
            //                System.err.println ("varNames:" + varNames);

            QueryParams qp = new QueryParams();
            String format  = request.getString(ARG_FORMAT, QueryParams.NETCDF);
            qp.acceptType = (format.equals(FORMAT_TIMESERIES_CHART)
                             || format.equals(FORMAT_TIMESERIES_IMAGE))
                            ? QueryParams.CSV
                            : format;

            qp.vars           = varNames;

            qp.hasLatlonPoint = true;
            qp.lat            = llp.getLatitude();
            qp.lon            = llp.getLongitude();

            if (dates[0] != null) {
                qp.time_start = new DateType(false, dates[0]);
                if (dates[1] != null) {
                    qp.time_end     = new DateType(false, dates[1]);
                    qp.hasDateRange = true;
                } else {
                    qp.hasTimePoint = true;
                    qp.hasDateRange = false;
                    qp.time         = qp.time_start;
                }
            }
            if (levelVal == levelVal) {
                qp.hasVerticalCoord = true;
                qp.vertCoord        = levelVal;
            }
            String suffix = ".nc";
            if (qp.acceptType.equals(QueryParams.CSV)
                    || format.equals(FORMAT_TIMESERIES_CHART_DATA)
                    || format.equals(FORMAT_TIMESERIES_IMAGE)) {
                suffix = ".csv";
            } else if (qp.acceptType.equals(QueryParams.XML)) {
                suffix = ".xml";
            }

            String baseName = IOUtil.stripExtension(entry.getName());
            if (format.equals(FORMAT_TIMESERIES_CHART)) {
                StringBuffer buf           = new StringBuffer();
                String       chartTemplate =
                    getRepository().getResource(
                        "/org/ramadda/repository/resources/chart/dycharts.html");
                chartTemplate = chartTemplate.replaceAll("\\$\\{urlroot\\}",
                        getRepository().getUrlBase());
                //String title = request.getString(ARG_POINT_TIMESERIES_TITLE,
                //                   entry.getName());
                String title = "Data at: " + llp.toString();
                if (title.equals("")) {
                    title = entry.getName();
                }
                chartTemplate = chartTemplate.replace("${title}", title);
                StringBuffer vizsb =
                    new StringBuffer("visibility: [false, false");
                if (qp.hasVerticalCoord) {
                    vizsb.append(", false");
                }
                for (int var = 0; var < varNames.size(); var++) {
                    vizsb.append(", true");
                }
                vizsb.append("],");
                chartTemplate = chartTemplate.replace("${options}",
                        vizsb.toString());

                String html = chartTemplate;
                request.put(ARG_FORMAT, QueryParams.CSV);
                String dataUrl = request.getRequestPath() + "/" + baseName
                                 + suffix + "?" + request.getUrlArgs();
                html = html.replace("${dataurl}", dataUrl);

                buf.append(html);

                return new Result("Point As Grid Time Series", buf);
            }

            File tmpFile = getStorageManager().getTmpFile(request,
                               "pointsubset" + suffix);

            GridPointWriter writer =
                new GridPointWriter(gds,
                                    new DiskCache2(getRepository()
                                        .getStorageManager().getTmpDir()
                                        .toString(), false, 0, 0));
            OutputStream outStream =
                (qp.acceptType.equals(QueryParams.NETCDF))
                ? System.out
                : getStorageManager().getUncheckedFileOutputStream(tmpFile);

            PrintWriter pw = new PrintWriter(outStream);
            File        f  = writer.write(qp, pw);
            if (f == null) {
                outStream.close();
                f = tmpFile;
            }
            if (doingPublish(request)) {
                return getEntryManager().processEntryPublish(request, f,
                        (Entry) entry.clone(), entry, "point series of");
            }
            Result result = null;
            if (format.equals(FORMAT_TIMESERIES_IMAGE)) {
                result = outputTimeSeriesImage(request, entry, f);
            } else {
                result =
                    new Result(getStorageManager().getFileInputStream(f),
                               qp.acceptType);
                //Set return filename sets the Content-Disposition http header so the browser saves the file
                //with the correct name and suffix
                result.setReturnFilename(baseName + suffix);
            }

            return result;
        }

        return new Result("", sb);
    }


    /**
     * Output the grid as a point form
     *
     * @param request   the request
     * @param entry     the entry
     * @param dataset   the corresponding dataset
     * @param sb        the string buffer
     *
     * @return the result
     *
     * @throws Exception problem creating form
     */
    public Result outputGridAsPointForm(Request request, Entry entry,
                                        GridDataset dataset, StringBuffer sb)
            throws Exception {

        boolean canAdd =
            getRepository().getAccessManager().canDoAction(request,
                entry.getParentEntry(), Permission.ACTION_NEW);

        String formUrl  = request.url(getRepository().URL_ENTRY_SHOW);
        String fileName = IOUtil.stripExtension(entry.getName()) + "_point";

        sb.append(HtmlUtils.form(formUrl + "/" + fileName));
        sb.append(HtmlUtils.br());



        sb.append(HtmlUtils.submit("Get Point", ARG_SUBMIT));
        sb.append(HtmlUtils.br());
        sb.append(HtmlUtils.hidden(ARG_OUTPUT, OUTPUT_GRIDASPOINT));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtils.formTable());



        Date[]       dateRange = null;
        List<Date>   dates     = getGridDates(dataset);

        StringBuffer varSB     = getVariableForm(dataset, true);

        LatLonRect   llr       = dataset.getBoundingBox();
        String       lat       = "";
        String       lon       = "";
        if (llr != null) {
            lat = Misc.format(llr.getLatMin() + llr.getHeight() / 2);
            lon = Misc.format(llr.getCenterLon());
        }
        MapInfo map = getRepository().getMapManager().createMap(request,
                          true);
        map.addBox("", llr, new MapProperties("blue", false, true));
        String llb = map.makeSelector(ARG_LOCATION, true, new String[] { lat,
                lon });
        sb.append(HtmlUtils.formEntryTop(msgLabel("Location"), llb));

        if ((dates != null) && (dates.size() > 0)) {
            List formattedDates = new ArrayList();
            formattedDates.add(new TwoFacedObject("---", ""));
            for (Date date : dates) {
                formattedDates.add(getRepository().formatDate(request, date));
            }
            String fromDate = request.getUnsafeString(ARG_FROMDATE, "");
            String toDate   = request.getUnsafeString(ARG_TODATE, "");
            sb.append(
                HtmlUtils.formEntry(
                    msgLabel("Time Range"),
                    HtmlUtils.select(ARG_FROMDATE, formattedDates, fromDate)
                    + HtmlUtils.img(iconUrl(ICON_ARROW))
                    + HtmlUtils.select(ARG_TODATE, formattedDates, toDate)));
        }
        List formats = Misc.toList(new Object[] {
                           new TwoFacedObject("NetCDF", QueryParams.NETCDF),
                           new TwoFacedObject("Xml", QueryParams.XML),
                           new TwoFacedObject("Time Series Image",
                               FORMAT_TIMESERIES),
        // Comment out until it works better to handled dates
        //new TwoFacedObject("Interactive Time Series",
        //    FORMAT_TIMESERIES_CHART),
        new TwoFacedObject("Comma Separated Values (CSV)",
                           QueryParams.CSV) });

        String format = request.getString(ARG_FORMAT, QueryParams.NETCDF);

        sb.append(HtmlUtils.formEntry(msgLabel("Format"),
                                     HtmlUtils.select(ARG_FORMAT, formats,
                                         format)));


        addPublishWidget(request, entry, sb,
                         msg("Select a folder to publish the results to"));
        sb.append(HtmlUtils.formTableClose());
        sb.append("<hr>");
        sb.append(msgLabel("Select Variables"));
        sb.append(HtmlUtils.insetDiv(HtmlUtils.table(varSB.toString(),
                HtmlUtils.attrs(HtmlUtils.ATTR_CELLPADDING, "5",
                               HtmlUtils.ATTR_CELLSPACING, "0")), 0, 30, 0,
                                   0));

        sb.append(HtmlUtils.submit("Get Point"));
        //sb.append(submitExtra);
        sb.append(HtmlUtils.formClose());

        return makeLinksResult(request, msg("Grid As Point"), sb,
                               new State(entry));
    }

    /**
     * Get the grid dates
     *
     * @param dataset  the dataset
     *
     * @return  the dates or null
     */
    private List<Date> getGridDates(GridDataset dataset) {
        List<Date>                 gridDates = null;
        List<GridDatatype>         grids     = dataset.getGrids();
        HashSet<Date>              dateHash  = new HashSet<Date>();
        List<CoordinateAxis1DTime> timeAxes  =
            new ArrayList<CoordinateAxis1DTime>();

        for (GridDatatype grid : grids) {
            GridCoordSystem      gcs      = grid.getCoordinateSystem();
            CoordinateAxis1DTime timeAxis = gcs.getTimeAxis1D();
            if ((timeAxis != null) && !timeAxes.contains(timeAxis)) {
                timeAxes.add(timeAxis);

                Date[] timeDates = timeAxis.getTimeDates();
                for (Date timeDate : timeDates) {
                    dateHash.add(timeDate);
                }
            }
        }
        if ( !dateHash.isEmpty()) {
            gridDates =
                Arrays.asList(dateHash.toArray(new Date[dateHash.size()]));
            Collections.sort(gridDates);
        }

        return gridDates;
    }

    /**
     * Get the variable selector form
     *
     * @param dataset  the dataset
     * @param withLevelSelector  if true, include a level selector widget
     *
     * @return  the form
     */
    protected StringBuffer getVariableForm(GridDataset dataset,
                                           boolean withLevelSelector) {
        int                varCnt  = 0;
        StringBuffer       varSB   = new StringBuffer();
        StringBuffer       varSB2D = new StringBuffer();
        StringBuffer       varSB3D = new StringBuffer();
        List<GridDatatype> grids   = sortGrids(dataset);

        for (GridDatatype grid : grids) {
            String cbxId = "varcbx_" + (varCnt++);
            String call  = HtmlUtils.attr(
                              HtmlUtils.ATTR_ONCLICK,
                              HtmlUtils.call(
                                  "checkboxClicked",
                                  HtmlUtils.comma(
                                      "event", HtmlUtils.squote(ARG_VARIABLE),
                                      HtmlUtils.squote(cbxId))));
            VariableEnhanced var     = grid.getVariable();
            StringBuffer     sbToUse = (grid.getZDimension() == null)
                                       ? varSB2D
                                       : varSB3D;

            sbToUse.append(
                HtmlUtils.row(
                    HtmlUtils.cols(
                        HtmlUtils.checkbox(
                            ARG_VARIABLE + "." + var.getShortName(),
                            HtmlUtils.VALUE_TRUE, (grids.size() == 1),
                            HtmlUtils.id(cbxId) + call) + HtmlUtils.space(1)
                                + var.getName() + HtmlUtils.space(1)
                                + ((var.getUnitsString() != null)
                                   ? "(" + var.getUnitsString() + ")"
                                   : ""), "<i>" + var.getDescription()
                                          + "</i>")));

        }
        if (varSB2D.length() > 0) {
            if (varSB3D.length() > 0) {
                varSB.append(HtmlUtils.row(HtmlUtils.headerCols(new Object[] {
                    "2D Grids" })));
            }
            varSB.append(varSB2D);
        }
        if (varSB3D.length() > 0) {
            if ((varSB2D.length() > 0) || withLevelSelector) {
                String header = " 3D Grids";
                if (withLevelSelector) {
                    header += HtmlUtils.space(3) + "Level:"
                              + HtmlUtils.space(1)
                              + HtmlUtils.input(ARG_LEVEL, "");
                }
                varSB.append(HtmlUtils.row(HtmlUtils.headerCols(new Object[] {
                    header })));
            }
            varSB.append(varSB3D);
        }

        return varSB;
    }

    /**
     * Handle a grid as point request
     *
     * @param request  the request
     * @param entry    the entry
     *
     * @return  the result
     *
     * @throws Exception problems
     */
    public Result outputGridAsPoint(Request request, Entry entry)
            throws Exception {
        String format   = request.getString(ARG_FORMAT, QueryParams.NETCDF);
        String baseName = IOUtil.stripExtension(entry.getName());
        if (format.equals(FORMAT_TIMESERIES)) {
            request.put(ARG_FORMAT, FORMAT_TIMESERIES_IMAGE);
            String redirectUrl = request.getRequestPath() + "/" + baseName
                                 + ".png" + "?" + request.getUrlArgs();

            return new Result("Point As Grid Time Series Image",
                              new StringBuffer(HtmlUtils.img(redirectUrl,
                                  "Image is being processed...")));
        }
        StringBuffer sb     = new StringBuffer();
        String       path   = getPath(request, entry);

        GridDataset  gds    = getGridDataset(entry, path);
        OutputType   output = request.getOutput();
        try {
            if (output.equals(OUTPUT_GRIDASPOINT)) {
                Result result = outputGridAsPointProcess(request, entry, gds,
                                    sb);
                if (result != null) {
                    return result;
                }
            }

            return outputGridAsPointForm(request, entry, gds, sb);
        } finally {
            returnGridDataset(path, gds);
            //gridPool.put(path, gds);
        }
    }


    /**
     * Handle a grid subset request
     *
     * @param request the request
     * @param entry   the entry
     *
     * @return  a Result
     *
     * @throws Exception  problem handling the request
     */
    public Result outputGridSubset(Request request, Entry entry)
            throws Exception {

        boolean canAdd =
            getRepository().getAccessManager().canDoAction(request,
                entry.getParentEntry(), Permission.ACTION_NEW);


        String       path   = getPath(request, entry);
        StringBuffer sb     = new StringBuffer();

        OutputType   output = request.getOutput();
        if (output.equals(OUTPUT_GRIDSUBSET)) {
            List      varNames = new ArrayList();
            Hashtable args     = request.getArgs();
            for (Enumeration keys = args.keys(); keys.hasMoreElements(); ) {
                String arg = (String) keys.nextElement();
                if (arg.startsWith(VAR_PREFIX) && request.get(arg, false)) {
                    varNames.add(arg.substring(VAR_PREFIX.length()));
                }
            }
            //            System.err.println(varNames);
            LatLonRect llr                 = null;
            boolean    anySpatialDifferent = false;
            boolean    haveAllSpatialArgs  = true;

            for (String spatialArg : SPATIALARGS) {
                if ( !Misc.equals(request.getString(spatialArg, ""),
                                  request.getString(spatialArg + ".original",
                                      ""))) {
                    anySpatialDifferent = true;

                    break;
                }
            }
            for (String spatialArg : SPATIALARGS) {
                if ( !request.defined(spatialArg)) {
                    haveAllSpatialArgs = false;

                    break;
                }
            }

            if (haveAllSpatialArgs && anySpatialDifferent) {
                llr = new LatLonRect(
                    new LatLonPointImpl(
                        request.get(ARG_AREA_NORTH, 90.0), request.get(
                            ARG_AREA_WEST, -180.0)), new LatLonPointImpl(
                                request.get(ARG_AREA_SOUTH, 0.0), request.get(
                                    ARG_AREA_EAST, 180.0)));
                //                System.err.println("llr:" + llr);
            }
            int     hStride       = request.get(ARG_HSTRIDE, 1);
            int     zStride       = 1;
            boolean includeLatLon = request.get(ARG_ADDLATLON, false);
            int     timeStride    = 1;
            Date[]  dates = new Date[] { request.defined(ARG_FROMDATE)
                                         ? request.getDate(ARG_FROMDATE, null)
                                         : null,
                                         request.defined(ARG_TODATE)
                                         ? request.getDate(ARG_TODATE, null)
                                         : null };
            //have to have both dates
            if ((dates[0] != null) && (dates[1] == null)) {
                dates[0] = null;
            }
            if ((dates[1] != null) && (dates[0] == null)) {
                dates[1] = null;
            }
            if ((dates[0] != null) && (dates[1] != null)
                    && (dates[0].getTime() > dates[1].getTime())) {
                sb.append(
                    getRepository().showDialogWarning(
                        "From date is after to date"));
            } else if (varNames.size() == 0) {
                sb.append(
                    getRepository().showDialogWarning(
                        "No variables selected"));
            } else {
                NetcdfCFWriter writer = new NetcdfCFWriter();
                File           f      =
                    getRepository().getStorageManager().getTmpFile(request,
                        "subset.nc");
                GridDataset gds = getGridDataset(entry, path);
                writer.makeFile(f.toString(), gds, varNames, llr,
                                ((dates[0] == null)
                                 ? null
                                 : new ucar.nc2.units.DateRange(dates[0],
                                 dates[1])), includeLatLon, hStride, zStride,
                                             timeStride);
                returnGridDataset(path, gds);
                //                gridPool.put(path, gds);

                if (doingPublish(request)) {
                    TypeHandler typeHandler =
                        getRepository().getTypeHandler(TypeHandler.TYPE_FILE);
                    Entry newEntry =
                        typeHandler.createEntry(getRepository().getGUID());

                    return getEntryManager().processEntryPublish(request, f,
                            newEntry, entry, "subset of");
                }

                return new Result(entry.getName() + ".nc",
                                  getStorageManager().getFileInputStream(f),
                                  "application/x-netcdf");
            }
        }

        String formUrl  = request.url(getRepository().URL_ENTRY_SHOW);
        String fileName = IOUtil.stripExtension(entry.getName())
                          + "_subset.nc";

        sb.append(HtmlUtils.form(formUrl + "/" + fileName));
        sb.append(HtmlUtils.br());

        sb.append(HtmlUtils.submit("Subset Grid", ARG_SUBMIT));
        sb.append(HtmlUtils.br());
        sb.append(HtmlUtils.hidden(ARG_OUTPUT, OUTPUT_GRIDSUBSET));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtils.formTable());



        sb.append(HtmlUtils.formEntry(msgLabel("Horizontal Stride"),
                                     HtmlUtils.input(ARG_HSTRIDE,
                                         request.getString(ARG_HSTRIDE, "1"),
                                         HtmlUtils.SIZE_3)));

        GridDataset  dataset   = getGridDataset(entry, path);
        Date[]       dateRange = null;
        List<Date>   dates     = getGridDates(dataset);
        StringBuffer varSB     = getVariableForm(dataset, false);
        LatLonRect   llr       = dataset.getBoundingBox();
        if (llr != null) {
            MapInfo map = getRepository().getMapManager().createMap(request,
                              true);
            map.addBox("", llr, new MapProperties("blue", false, true));
            String[] points = new String[] { "" + llr.getLatMax(),
                                             "" + llr.getLonMin(),
                                             "" + llr.getLatMin(),
                                             "" + llr.getLonMax(), };

            for (int i = 0; i < points.length; i++) {
                sb.append(HtmlUtils.hidden(SPATIALARGS[i] + ".original",
                                          points[i]));
            }
            String llb = map.makeSelector(ARG_AREA, true, points);
            sb.append(HtmlUtils.formEntryTop(msgLabel("Subset Spatially"),
                                            llb));
        }

        if ((dates != null) && (dates.size() > 0)) {
            List formattedDates = new ArrayList();
            formattedDates.add(new TwoFacedObject("---", ""));
            for (Date date : dates) {
                formattedDates.add(getRepository().formatDate(request, date));
            }
            /*
              for now default to "" for dates
            String fromDate = request.getUnsafeString(ARG_FROMDATE,
                                  getRepository().formatDate(request,
                                      dates.get(0)));
            String toDate = request.getUnsafeString(ARG_TODATE,
                                getRepository().formatDate(request,
                                    dates.get(dates.size() - 1)));
            */
            String fromDate = request.getUnsafeString(ARG_FROMDATE, "");
            String toDate   = request.getUnsafeString(ARG_TODATE, "");
            sb.append(
                HtmlUtils.formEntry(
                    msgLabel("Time Range"),
                    HtmlUtils.select(ARG_FROMDATE, formattedDates, fromDate)
                    + HtmlUtils.img(iconUrl(ICON_ARROW))
                    + HtmlUtils.select(ARG_TODATE, formattedDates, toDate)));
        }

        sb.append(HtmlUtils.formEntry(msgLabel("Add Lat/Lon Variables"),
                                     HtmlUtils.checkbox(ARG_ADDLATLON,
                                         HtmlUtils.VALUE_TRUE,
                                         request.get(ARG_ADDLATLON, true))));

        addPublishWidget(request, entry, sb,
                         msg("Select a folder to publish the results to"));
        sb.append(HtmlUtils.formTableClose());
        sb.append("<hr>");
        sb.append(msgLabel("Select Variables"));
        sb.append("<ul>");
        sb.append("<table>");
        sb.append(varSB);
        sb.append("</table>");
        sb.append("</ul>");
        sb.append(HtmlUtils.br());
        sb.append(HtmlUtils.submit(msg("Subset Grid")));
        sb.append(HtmlUtils.formClose());
        returnGridDataset(path, dataset);

        //        gridPool.put(path, dataset);
        return makeLinksResult(request, msg("Grid Subset"), sb,
                               new State(entry));
    }


    /**
     * Sort the grids
     *
     * @param dataset  the grid dataset
     *
     * @return  the grids
     */
    public List<GridDatatype> sortGrids(GridDataset dataset) {
        List tuples = new ArrayList();
        for (GridDatatype grid : dataset.getGrids()) {
            VariableEnhanced var = grid.getVariable();
            tuples.add(new Object[] { var.getShortName().toLowerCase(),
                                      grid });
        }
        tuples = Misc.sortTuples(tuples, true);
        List<GridDatatype> result = new ArrayList<GridDatatype>();
        for (Object[] tuple : (List<Object[]>) tuples) {
            result.add((GridDatatype) tuple[1]);
        }

        return result;
    }






    /**
     * Get the PointFeatureIterator
     *
     * @param input  the dataset
     *
     * @return  the iterator
     *
     * @throws Exception  problem getting the iterator
     */
    public static PointFeatureIterator getPointIterator(
            FeatureDatasetPoint input)
            throws Exception {
        List<FeatureCollection> collectionList =
            input.getPointFeatureCollectionList();
        if (collectionList.size() > 1) {
            throw new IllegalArgumentException(
                "Can't handle point data with multiple collections");
        }
        FeatureCollection      fc         = collectionList.get(0);
        PointFeatureCollection collection = null;
        if (fc instanceof PointFeatureCollection) {
            collection = (PointFeatureCollection) fc;
        } else if (fc instanceof NestedPointFeatureCollection) {
            NestedPointFeatureCollection npfc =
                (NestedPointFeatureCollection) fc;
            collection = npfc.flatten(null, null);
        } else {
            throw new IllegalArgumentException(
                "Can't handle collection of type " + fc.getClass().getName());
        }

        return collection.getPointFeatureIterator(16384);
    }




    /**
     * Output a point map
     *
     * @param request  the request
     * @param entry  the entry
     *
     * @return  the Result
     *
     * @throws Exception  on badness
     */
    public Result outputPointMap(Request request, Entry entry)
            throws Exception {


        MapInfo map = getRepository().getMapManager().createMap(request,
                          false);
        String               path           = getPath(request, entry);
        FeatureDatasetPoint  pod            = pointPool.get(path);

        StringBuffer         sb             = new StringBuffer();
        List                 vars           = pod.getDataVariables();
        int                  skip           = request.get(ARG_SKIP, 0);
        int                  max            = request.get(ARG_MAX, 200);

        int                  cnt            = 0;
        int                  total          = 0;
        String               icon           = iconUrl("/icons/pointdata.gif");

        PointFeatureIterator dataIterator   = getPointIterator(pod);
        List                 columnDataList = new ArrayList();
        while (dataIterator.hasNext()) {
            PointFeature po = (PointFeature) dataIterator.next();
            //                ucar.unidata.geoloc.EarthLocation el = po.getLocation();
            ucar.unidata.geoloc.EarthLocation el = po.getLocation();
            if (el == null) {
                continue;
            }
            double lat = el.getLatitude();
            double lon = el.getLongitude();
            if ((lat != lat) || (lon != lon)) {
                continue;
            }
            if ((lat < -90) || (lat > 90) || (lon < -180) || (lon > 180)) {
                continue;
            }
            total++;
            if (total <= skip) {
                continue;
            }
            if (total > (max + skip)) {
                continue;
            }
            cnt++;
            List          columnData = new ArrayList();
            StructureData structure  = po.getData();
            StringBuffer  info       = new StringBuffer("");
            info.append("<b>Date:</b> " + po.getNominalTimeAsDate() + "<br>");
            for (VariableSimpleIF var : (List<VariableSimpleIF>) vars) {
                //{name:\"Ashley\",breed:\"German Shepherd\",age:12}
                StructureMembers.Member member =
                    structure.findMember(var.getShortName());
                if ((var.getDataType() == DataType.STRING)
                        || (var.getDataType() == DataType.CHAR)) {
                    String value = structure.getScalarString(member);
                    columnData.add(var.getShortName() + ":"
                                   + HtmlUtils.quote(value));
                    info.append("<b>" + var.getName() + ": </b>" + value
                                + "</br>");

                } else {
                    float value = structure.convertScalarFloat(member);
                    info.append("<b>" + var.getName() + ": </b>" + value
                                + "</br>");

                    columnData.add(var.getShortName() + ":" + value);
                }
            }
            columnDataList.add("{" + StringUtil.join(",", columnData)
                               + "}\n");
            map.addMarker("",
                          new LatLonPointImpl(el.getLatitude(),
                              el.getLongitude()), icon, info.toString());
        }



        List columnDefs  = new ArrayList();
        List columnNames = new ArrayList();
        for (VariableSimpleIF var : (List<VariableSimpleIF>) vars) {
            columnNames.add(HtmlUtils.quote(var.getShortName()));
            String label = var.getDescription();
            //            if(label.trim().length()==0)
            label = var.getName();
            columnDefs.add("{key:" + HtmlUtils.quote(var.getShortName()) + ","
                           + "sortable:true," + "label:"
                           + HtmlUtils.quote(label) + "}");
        }


        if (total > max) {
            sb.append((skip + 1) + "-" + (skip + cnt) + " of " + total + " ");
        } else {
            sb.append((skip + 1) + "-" + (skip + cnt));
        }
        if (total > max) {
            boolean didone = false;
            if (skip > 0) {
                sb.append(HtmlUtils.space(2));
                sb.append(
                    HtmlUtils.href(
                        HtmlUtils.url(request.getRequestPath(), new String[] {
                    ARG_OUTPUT, request.getOutput().toString(), ARG_ENTRYID,
                    entry.getId(), ARG_SKIP, "" + (skip - max), ARG_MAX,
                    "" + max
                }), msg("Previous")));
                didone = true;
            }
            if (total > (skip + cnt)) {
                sb.append(HtmlUtils.space(2));
                sb.append(
                    HtmlUtils.href(
                        HtmlUtils.url(request.getRequestPath(), new String[] {
                    ARG_OUTPUT, request.getOutput().toString(), ARG_ENTRYID,
                    entry.getId(), ARG_SKIP, "" + (skip + max), ARG_MAX,
                    "" + max
                }), msg("Next")));
                didone = true;
            }
            //Just come up with some max number
            if (didone && (total < 2000)) {
                sb.append(HtmlUtils.space(2));
                sb.append(
                    HtmlUtils.href(
                        HtmlUtils.url(request.getRequestPath(), new String[] {
                    ARG_OUTPUT, request.getOutput().toString(), ARG_ENTRYID,
                    entry.getId(), ARG_SKIP, "" + 0, ARG_MAX, "" + total
                }), msg("All")));

            }
        }
        map.center();
        sb.append(map.getHtml());
        pointPool.put(path, pod);

        return new Result(msg("Point Data Map"), sb);
    }


    /** Fixed var name for lat */
    public static final String VAR_LATITUDE = "Latitude";

    /** Fixed var name for lon */
    public static final String VAR_LONGITUDE = "Longitude";

    /** Fixed var name for alt */
    public static final String VAR_ALTITUDE = "Altitude";

    /** Fixed var name for time */
    public static final String VAR_TIME = "Time";



    /**
     * Get the 1D values for an array as floats.
     *
     * @param arr   Array of values
     * @return  float representation
     */
    public static float[] toFloatArray(Array arr) {
        Object dst       = arr.get1DJavaArray(float.class);
        Class  fromClass = dst.getClass().getComponentType();
        if (fromClass.equals(float.class)) {
            //It should always be a float
            return (float[]) dst;
        } else {
            float[] values = new float[(int) arr.getSize()];
            if (fromClass.equals(byte.class)) {
                byte[] fromArray = (byte[]) dst;
                for (int i = 0; i < fromArray.length; ++i) {
                    values[i] = fromArray[i];
                }
            } else if (fromClass.equals(short.class)) {
                short[] fromArray = (short[]) dst;
                for (int i = 0; i < fromArray.length; ++i) {
                    values[i] = fromArray[i];
                }
            } else if (fromClass.equals(int.class)) {
                int[] fromArray = (int[]) dst;
                for (int i = 0; i < fromArray.length; ++i) {
                    values[i] = fromArray[i];
                }
            } else if (fromClass.equals(double.class)) {
                double[] fromArray = (double[]) dst;
                for (int i = 0; i < fromArray.length; ++i) {
                    values[i] = (float) fromArray[i];
                }
            } else {
                throw new IllegalArgumentException("Unknown array type:"
                        + fromClass.getName());
            }

            return values;
        }

    }


    /**
     * Output a trajectory map
     *
     * @param request  the request
     * @param entry    the entry
     *
     * @return  the result
     *
     * @throws Exception  on badness
     */
    public Result outputTrajectoryMap(Request request, Entry entry)
            throws Exception {
        String               path = getPath(request, entry);
        TrajectoryObsDataset tod  = trajectoryPool.get(path);
        StringBuffer         sb   = new StringBuffer();

        MapInfo map = getRepository().getMapManager().createMap(request, 800,
                          600, false);
        List trajectories = tod.getTrajectories();
        //TODO: Use new openlayers map
        for (int i = 0; i < trajectories.size(); i++) {
            List                  allVariables = tod.getDataVariables();
            TrajectoryObsDatatype todt         =
                (TrajectoryObsDatatype) trajectories.get(i);
            float[] lats = toFloatArray(todt.getLatitude(null));
            float[] lons = toFloatArray(todt.getLongitude(null));
            for (int ptIdx = 0; ptIdx < lats.length; ptIdx++) {
                if (ptIdx > 0) {
                    if (ptIdx == lats.length - 1) {
                        map.addMarker("", lats[ptIdx], lons[ptIdx], null,
                                      "End time:" + todt.getEndDate());
                    }
                    //#FF0000
                    map.addLine("", lats[ptIdx - 1], lons[ptIdx - 1],
                                lats[ptIdx], lons[ptIdx]);
                } else {
                    map.addMarker("", lats[ptIdx], lons[ptIdx], null,
                                  "Start time:" + todt.getEndDate());
                }

            }
            StructureData    structure = todt.getData(0);
            VariableSimpleIF theVar    = null;
            for (int varIdx = 0; varIdx < allVariables.size(); varIdx++) {
                VariableSimpleIF var =
                    (VariableSimpleIF) allVariables.get(varIdx);
                if (var.getRank() != 0) {
                    continue;
                }
                theVar = var;

                break;
            }
            if (theVar == null) {
                continue;
            }
        }

        map.center();
        sb.append(map.getHtml());
        trajectoryPool.put(path, tod);

        return new Result(msg("Trajectory Map"), sb);


    }


    /*
    public Result outputTrajectoryChart(Request request, Entry entry)
            throws Exception {
        TrajectoryObsDataset tod = trajectoryPool.get(entry.getResource().getPath());
        StringBuffer sb         = new StringBuffer();
        StringBuffer head = new StringBuffer();
        head.append("<script type='text/javascript' src='http://www.google.com/jsapi'></script>\n");
        head.append("<script type='text/javascript'>\n");
        head.append("google.load('visualization', '1', {'packages':['annotatedtimeline']});\n");
        head.append("google.setOnLoadCallback(drawChart);\n");
        head.append("function drawChart() {");
        head.append("var data = new google.visualization.DataTable();\n");
        head.append("data.addColumn('date', 'Date');\n");
        head.append("data.addColumn('number', 'Value');\n");
        head.append("var chart = new google.visualization.AnnotatedTimeLine(document.getElementById('chart_div'));\n");
        sb.append("<div id='chart_div' style='width: 700px; height: 240px;'></div>\n");

        StringBuffer values  = new StringBuffer();
        int numRows = 0;
        synchronized (tod) {
            List         trajectories = tod.getTrajectories();
            List allVariables = tod.getDataVariables();
            TrajectoryObsDatatype todt =
                (TrajectoryObsDatatype) trajectories.get(0);
            float[]      lats     = toFloatArray(todt.getLatitude(null));
            numRows = lats.length;
            for (int ptIdx = 0; ptIdx < lats.length; ptIdx++) {
                StructureData    structure = todt.getData(0);
                VariableSimpleIF theVar    = null;
                for (int varIdx = 0; varIdx < allVariables.size(); varIdx++) {
                    VariableSimpleIF var =
                        (VariableSimpleIF) allVariables.get(varIdx);
                    if (var.getRank() != 0) {
                        continue;
                    }
                    theVar = var;
                    break;
               }
                if (theVar == null) {
                    continue;
                }
                values.append("data.setValue(0, 0, new Date(2008, 1 ,1));\n");
                values.append("data.setValue(0, 1, 30000);\n");
            }
            }

        head.append(values);
        head.append("data.addRows(" + numRows+");\n");
        head.append("chart.draw(data, {displayAnnotations: true});\n");
        head.append("}\n</script>\n");

        Result result =  new Result(msg("Trajectory Time Series"), sb);
        result.putProperty(PROP_HTML_HEAD,head.toString());
        return result;
    }
    */





    /**
     * Create a LatLonPoint string
     *
     * @param lat  the latitude
     * @param lon  the longitude
     *
     * @return  the string
     */
    public static String llp(double lat, double lon) {
        return "new LatLonPoint(" + lat + "," + lon + ")";
    }

    /**
     * Make the Point Subset form
     *
     * @param request   the Request
     * @param entry     the Entry
     * @param suffix    the type as a suffix
     *
     * @return the Result
     */
    private Result makePointSubsetForm(Request request, Entry entry,
                                       String suffix) {
        StringBuffer sb      = new StringBuffer();
        String       formUrl = request.url(getRepository().URL_ENTRY_SHOW);
        sb.append(HtmlUtils.form(formUrl + suffix));
        sb.append(HtmlUtils.submit("Subset Point Data", ARG_SUBMIT));
        sb.append(HtmlUtils.hidden(ARG_OUTPUT,
                                  request.getString(ARG_OUTPUT, "")));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtils.formTable());
        List<TwoFacedObject> formats = new ArrayList<TwoFacedObject>();
        formats.add(new TwoFacedObject("CSV", FORMAT_CSV));
        formats.add(new TwoFacedObject("KML", FORMAT_KML));
        String format = request.getString(ARG_FORMAT, FORMAT_CSV);
        sb.append(HtmlUtils.formEntry(msgLabel("Format"),
                                     HtmlUtils.select(ARG_FORMAT, formats,
                                         format)));

        MapInfo map = getRepository().getMapManager().createMap(request,
                          true);
        map.addBox(entry, new MapProperties("blue", false, true));
        map.centerOn(entry);
        String llb = map.makeSelector(ARG_POINT_BBOX, true, null);
        sb.append(HtmlUtils.formEntryTop(msgLabel("Location"), llb));


        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.submit("Subset Point Data", ARG_SUBMIT));
        sb.append(HtmlUtils.formClose());

        return new Result("", sb);
    }



    /**
     * Output a point subset
     *
     * @param request  the Request
     * @param entry    the Entry
     *
     * @return  the Result
     *
     * @throws Exception problem making subset
     */
    public Result outputPointSubset(Request request, Entry entry)
            throws Exception {
        if ( !request.defined(ARG_FORMAT)) {
            return makePointSubsetForm(request, entry, "");
        }
        String format = request.getString(ARG_FORMAT, FORMAT_CSV);

        if (format.equals(FORMAT_CSV)) {
            request.getHttpServletResponse().setContentType("text/csv");
            request.setReturnFilename(IOUtil.stripExtension(entry.getName())
                                      + ".csv");
        } else {
            request.getHttpServletResponse().setContentType(
                "application/vnd.google-earth.kml+xml");
            request.setReturnFilename(IOUtil.stripExtension(entry.getName())
                                      + ".kml");
        }
        System.out.println("name: " + request);


        OutputStream os = request.getHttpServletResponse().getOutputStream();
        PrintWriter  pw = new PrintWriter(os);

        if (format.equals(FORMAT_CSV)) {
            outputPointCsv(request, entry, pw);
        } else {
            outputPointKml(request, entry, pw);
        }

        pw.close();
        Result result = new Result();
        result.setNeedToWrite(false);
        return result;
    }


    /**
     * Output the point data as CSV
     *
     * @param request  the Request
     * @param entry    the Entry
     * @param pw       the PrintWriter
     *
     *
     * @throws Exception  problem getting data
     */
    private void outputPointCsv(Request request, Entry entry, PrintWriter pw)
            throws Exception {
        String               path         = getPath(request, entry);
        FeatureDatasetPoint  pod          = pointPool.get(path);
        List                 vars         = pod.getDataVariables();
        PointFeatureIterator dataIterator = getPointIterator(pod);
        int                  cnt          = 0;
        while (dataIterator.hasNext()) {
            PointFeature po = (PointFeature) dataIterator.next();
            ucar.unidata.geoloc.EarthLocation el = po.getLocation();
            if (el == null) {
                continue;
            }
            cnt++;

            double        lat       = el.getLatitude();
            double        lon       = el.getLongitude();
            StructureData structure = po.getData();

            if (cnt == 1) {
                pw.print(HtmlUtils.quote("Time"));
                pw.print(",");
                pw.print(HtmlUtils.quote("Latitude"));
                pw.print(",");
                pw.print(HtmlUtils.quote("Longitude"));
                for (VariableSimpleIF var : (List<VariableSimpleIF>) vars) {
                    pw.print(",");
                    String unit = var.getUnitsString();
                    if (unit != null) {
                        pw.print(HtmlUtils.quote(var.getShortName() + " ("
                                + unit + ")"));
                    } else {
                        pw.print(HtmlUtils.quote(var.getShortName()));
                    }
                }
                pw.print("\n");
            }

            pw.print(HtmlUtils.quote("" + po.getNominalTimeAsDate()));
            pw.print(",");
            pw.print(el.getLatitude());
            pw.print(",");
            pw.print(el.getLongitude());

            for (VariableSimpleIF var : (List<VariableSimpleIF>) vars) {
                StructureMembers.Member member =
                    structure.findMember(var.getShortName());
                pw.print(",");
                if ((var.getDataType() == DataType.STRING)
                        || (var.getDataType() == DataType.CHAR)) {
                    pw.print(
                        HtmlUtils.quote(structure.getScalarString(member)));
                } else {
                    pw.print(structure.convertScalarFloat(member));
                }
            }
            pw.print("\n");
        }
        pointPool.put(path, pod);

    }


    /**
     * Output the points as KML
     *
     * @param request  the Request
     * @param entry    the Entry
     * @param pw       the PrintWriter
     *
     *
     * @throws Exception problem generating KML
     */
    private void outputPointKml(Request request, Entry entry, PrintWriter pw)
            throws Exception {
        String               path         = getPath(request, entry);
        FeatureDatasetPoint  pod          = pointPool.get(path);
        List                 vars         = pod.getDataVariables();
        PointFeatureIterator dataIterator = getPointIterator(pod);

        Element              root         = KmlUtil.kml(entry.getName());
        Element              docNode = KmlUtil.document(root, entry.getName());

        while (dataIterator.hasNext()) {
            PointFeature po = (PointFeature) dataIterator.next();
            ucar.unidata.geoloc.EarthLocation el = po.getLocation();
            if (el == null) {
                continue;
            }
            double lat = el.getLatitude();
            double lon = el.getLongitude();
            double alt = 0;
            if ((lat != lat) || (lon != lon)) {
                continue;
            }

            StructureData structure = po.getData();
            StringBuffer  info      = new StringBuffer("");
            info.append("<b>Date:</b> " + po.getNominalTimeAsDate() + "<br>");
            for (VariableSimpleIF var : (List<VariableSimpleIF>) vars) {
                StructureMembers.Member member =
                    structure.findMember(var.getShortName());
                if ((var.getDataType() == DataType.STRING)
                        || (var.getDataType() == DataType.CHAR)) {
                    info.append("<b>" + var.getName() + ": </b>"
                                + structure.getScalarString(member) + "<br>");
                } else {
                    info.append("<b>" + var.getName() + ": </b>"
                                + structure.convertScalarFloat(member)
                                + "<br>");

                }
            }
            KmlUtil.placemark(docNode, "" + po.getNominalTimeAsDate(),
                              info.toString(), lat, lon, alt, null);
        }
        pw.print(XmlUtil.toString(root));
        pointPool.put(path, pod);
    }


    /**
     * Get the Authorization method
     *
     * @param request  the Request
     *
     * @return  the autorization method
     */
    public AuthorizationMethod getAuthorizationMethod(Request request) {
        OutputType output = request.getOutput();
        if (output.equals(OUTPUT_WCS) || output.equals(OUTPUT_OPENDAP)) {
            return AuthorizationMethod.AUTH_HTTP;
        }

        return super.getAuthorizationMethod(request);
    }




    /**
     * Output a group of entries
     *
     * @param request     the Request
     * @param outputType  the output type
     * @param group       the group
     * @param subGroups   the subgroups
     * @param entries     the List of Entrys
     *
     * @return  the Result
     *
     * @throws Exception  problem outputting group
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        if (isAggregation(group)) {
            return outputEntry(request, outputType, group);
        }

        //        System.err.println("group:" + group + " " + group.getType());
        return super.outputGroup(request, outputType, group, subGroups,
                                 entries);
    }


    /**
     * Serve up the entry
     *
     * @param request     the Request
     * @param outputType  the output type
     * @param entry       the Entry
     *
     * @return the Result
     *
     * @throws Exception On badness
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {

        if ( !getRepository().getAccessManager().canDoAction(request, entry,
                Permission.ACTION_FILE)) {
            throw new AccessException("Cannot access data", request);
        }

        if ( !getRepository().getAccessManager().canAccessFile(request,
                entry)) {
            throw new AccessException("Cannot access data", request);
        }

        if (outputType.equals(OUTPUT_CDL)) {
            return outputCdl(request, entry);
        }
        if (outputType.equals(OUTPUT_WCS)) {
            return outputWcs(request, entry);
        }
        if (outputType.equals(OUTPUT_GRIDSUBSET)
                || outputType.equals(OUTPUT_GRIDSUBSET_FORM)) {
            return outputGridSubset(request, entry);
        }

        if (outputType.equals(OUTPUT_GRIDASPOINT)
                || outputType.equals(OUTPUT_GRIDASPOINT_FORM)) {
            return outputGridAsPoint(request, entry);
        }

        if (outputType.equals(OUTPUT_TRAJECTORY_MAP)) {
            return outputTrajectoryMap(request, entry);
        }

        if (outputType.equals(OUTPUT_POINT_MAP)) {
            return outputPointMap(request, entry);
        }

        if (outputType.equals(OUTPUT_POINT_SUBSET)) {
            return outputPointSubset(request, entry);
        }
        if (outputType.equals(OUTPUT_OPENDAP)) {
            //If its a head request then just return the content description
            if (request.isHeadRequest()) {
                Result result = new Result("", new StringBuffer());
                result.addHttpHeader(HtmlUtils.HTTP_CONTENT_DESCRIPTION,
                                     "dods-dds");

                return result;
            }
            Result result = outputOpendap(request, entry);

            return result;
        }

        throw new IllegalArgumentException("Unknown output type:"
                                           + outputType);
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
     * Get the OPeNDAP handler
     *
     * @return  the handler
     */
    public OpendapApiHandler getOpendapHandler() {
        return (OpendapApiHandler) getRepository().getApiHandler(
            OpendapApiHandler.API_ID);
    }



    /**
     * Output OPeNDAP
     *
     * @param request   the Request
     * @param entry     the Entry
     *
     * @return the Result
     *
     * @throws Exception  problems
     */
    public Result outputOpendap(final Request request, final Entry entry)
            throws Exception {

        request.remove(ARG_ENTRYID);
        request.remove(ARG_OUTPUT);

        //Get the file location for the entry
        String location = getPath(request, entry);

        //Get the ncFile from the pool

        NetcdfFile ncFile = ncFilePool.get(location);
        opendapCounter.incr();

        //Bridge the ramadda servlet to the opendap servlet
        NcDODSServlet servlet = new NcDODSServlet(request, entry, ncFile) {
            public ServletConfig getServletConfig() {
                return request.getHttpServlet().getServletConfig();
            }
            public ServletContext getServletContext() {
                return request.getHttpServlet().getServletContext();
            }
            public String getServletInfo() {
                return request.getHttpServlet().getServletInfo();
            }
            public Enumeration getInitParameterNames() {
                return request.getHttpServlet().getInitParameterNames();
            }
        };

        //If we are running as a normal servlet then init the ncdods servlet with the servlet config info
        if ((request.getHttpServlet() != null)
                && (request.getHttpServlet().getServletConfig() != null)) {
            servlet.init(request.getHttpServlet().getServletConfig());
        }

        //Do the work
        servlet.doGet(request.getHttpServletRequest(),
                      request.getHttpServletResponse());
        //We have to pass back a result though we set needtowrite to false because the opendap servlet handles the writing
        Result result = new Result("");
        result.setNeedToWrite(false);
        opendapCounter.decr();
        ncFilePool.put(location, ncFile);

        return result;
    }


    /**
     * NcDODSServlet to wrap the OPeNDAP servelet
     *
     */
    public class NcDODSServlet extends opendap.servlet.AbstractServlet {


        /** repository request */
        Request repositoryRequest;

        /** the NetcdfFile object */
        NetcdfFile ncFile;

        /** the Entry */
        Entry entry;

        /**
         * Construct a new NcDODSServlet
         *
         * @param request the Request
         * @param entry   the Entry
         * @param ncFile  the NetcdfFile object
         */
        public NcDODSServlet(Request request, Entry entry,
                             NetcdfFile ncFile) {
            this.repositoryRequest = request;
            this.entry             = entry;
            this.ncFile            = ncFile;
        }

        /**
         * Make the dataset
         *
         * @param preq preq
         *
         * @return The dataset
         *
         * @throws DAP2Exception On badness
         * @throws IOException On badness
         */
        protected GuardedDataset getDataset(ReqState preq)
                throws DAP2Exception, IOException /*, ParseException*/ {
            HttpServletRequest request = preq.getRequest();
            String             reqPath = entry.getName();

            try {
                GuardedDatasetImpl guardedDataset =
                    new GuardedDatasetImpl(reqPath, ncFile, true);

                return guardedDataset;
            } catch (Exception exc) {
                throw new WrapperException(exc);
            }
        }

        /**
         * Get the server version
         *
         * @return  the version
         */
        public String getServerVersion() {
            return "opendap/3.7";
        }
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

        DataOutputHandler dop = new DataOutputHandler(repository, "test");
        String[] types = { TYPE_CDM, TYPE_GRID, TYPE_TRAJECTORY, TYPE_POINT };
        for (String f : args) {
            System.err.println("file:" + f);
            for (String type : types) {
                boolean ok      = dop.hasSuffixForType(f, type, false);
                boolean exclude = dop.hasSuffixForType(f, type, true);
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
     * Output the timeseries image
     *
     * @param request the request
     * @param entry  the entry
     * @param f  the file
     *
     * @return  the image
     *
     * @throws Exception  problem creating image
     */
    private Result outputTimeSeriesImage(Request request, Entry entry, File f)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        //sb.append(getHeader(request, entry));
        sb.append(header(msg("Chart")));

        TimeSeriesCollection            dummy     = new TimeSeriesCollection();
        JFreeChart chart = createChart(request, entry, dummy);
        XYPlot                          xyPlot    = (XYPlot) chart.getPlot();

        Hashtable<String, MyTimeSeries> seriesMap = new Hashtable<String,
                                                        MyTimeSeries>();
        List<MyTimeSeries> allSeries = new ArrayList<MyTimeSeries>();
        int                                paramCount = 0;
        int                                colorCount = 0;
        boolean                            axisLeft   = true;
        Hashtable<String, List<ValueAxis>> axisMap    = new Hashtable<String,
                                                         List<ValueAxis>>();
        Hashtable<String, double[]> rangeMap = new Hashtable<String,
                                                   double[]>();
        List<String> units      = new ArrayList<String>();
        List<String> paramUnits = new ArrayList<String>();
        List<String> paramNames = new ArrayList<String>();

        long         t1         = System.currentTimeMillis();
        String       contents   =
            IOUtil.readContents(getStorageManager().getFileInputStream(f));
        List<String> lines      = StringUtil.split(contents, "\n", true, true);
        String       header     = lines.get(0);
        String[]     headerToks = header.split(",");
        for (int i = 0; i < headerToks.length; i++) {
            paramNames.add(getParamName(headerToks[i]));
            paramUnits.add(getUnitFromName(headerToks[i]));
        }
        boolean hasLevel   = paramNames.get(3).equals("vertCoord");

        boolean readHeader = false;
        for (String line : lines) {
            if ( !readHeader) {
                readHeader = true;

                continue;
            }
            String[] lineTokes = line.split(",");
            Date     date      = DateUtil.parse(lineTokes[0]);
            int      startIdx  = hasLevel
                                 ? 4
                                 : 3;
            for (int i = startIdx; i < lineTokes.length; i++) {
                double value = Double.parseDouble(lineTokes[i]);
                if (value != value) {
                    continue;
                }
                List<ValueAxis> axises     = null;
                double[]        range      = null;
                String          u          = paramUnits.get(i);
                String          paramName  = paramNames.get(i);
                String          formatName = paramName.replaceAll("_", " ");
                String formatUnit = ((u == null) || (u.length() == 0))
                                    ? ""
                                    : "[" + u + "]";
                if (u != null) {
                    axises = axisMap.get(u);
                    range  = rangeMap.get(u);
                    if (axises == null) {
                        axises = new ArrayList<ValueAxis>();
                        range  = new double[] { value, value };
                        rangeMap.put(u, range);
                        axisMap.put(u, axises);
                        units.add(u);
                    }
                    range[0] = Math.min(range[0], value);
                    range[1] = Math.max(range[1], value);
                }
                MyTimeSeries series = seriesMap.get(paramName);
                if (series == null) {
                    paramCount++;
                    TimeSeriesCollection dataset = new TimeSeriesCollection();
                    series = new MyTimeSeries(formatName,
                            FixedMillisecond.class);
                    allSeries.add(series);
                    ValueAxis rangeAxis = new NumberAxis(formatName + " "
                                              + formatUnit);
                    if (axises != null) {
                        axises.add(rangeAxis);
                    }
                    XYItemRenderer renderer =
                        new XYAreaRenderer(XYAreaRenderer.LINES);
                    if (colorCount >= GuiUtils.COLORS.length) {
                        colorCount = 0;
                    }
                    renderer.setSeriesPaint(0, GuiUtils.COLORS[colorCount]);
                    colorCount++;
                    xyPlot.setRenderer(paramCount, renderer);
                    xyPlot.setRangeAxis(paramCount, rangeAxis, false);
                    AxisLocation side = (axisLeft
                                         ? AxisLocation.TOP_OR_LEFT
                                         : AxisLocation.BOTTOM_OR_RIGHT);
                    axisLeft = !axisLeft;
                    xyPlot.setRangeAxisLocation(paramCount, side);

                    dataset.setDomainIsPointsInTime(true);
                    dataset.addSeries(series);
                    seriesMap.put(paramNames.get(i), series);
                    xyPlot.setDataset(paramCount, dataset);
                    xyPlot.mapDatasetToRangeAxis(paramCount, paramCount);
                }
                //series.addOrUpdate(new FixedMillisecond(pointData.date),value);
                TimeSeriesDataItem item =
                    new TimeSeriesDataItem(new FixedMillisecond(date), value);
                series.addItem(item);
            }
        }



        for (MyTimeSeries timeSeries : allSeries) {
            timeSeries.finish();
        }

        for (String unit : units) {
            List<ValueAxis> axises = axisMap.get(unit);
            double[]        range  = rangeMap.get(unit);
            for (ValueAxis rangeAxis : axises) {
                rangeAxis.setRange(new org.jfree.data.Range(range[0],
                        range[1]));
            }
        }


        long          t2       = System.currentTimeMillis();

        BufferedImage newImage =
            chart.createBufferedImage(request.get(ARG_IMAGE_WIDTH, 1000),
                                      request.get(ARG_IMAGE_HEIGHT, 400));
        long t3 = System.currentTimeMillis();
        //System.err.println("timeseries image time:" + (t2 - t1) + " "
        //                   + (t3 - t2));

        File file = getStorageManager().getTmpFile(request, "point.png");
        ImageUtils.writeImageToFile(newImage, file);
        InputStream is     = getStorageManager().getFileInputStream(file);
        Result      result = new Result("", is, "image/png");

        return result;

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


    /**
     * A wrapper for TimeSeries
     *
     * @author RAMADDA Development Team
     */
    private static class MyTimeSeries extends TimeSeries {

        /** the items */
        List<TimeSeriesDataItem> items = new ArrayList<TimeSeriesDataItem>();

        /** seen items */
        HashSet<TimeSeriesDataItem> seen = new HashSet<TimeSeriesDataItem>();

        /**
         * Construct the time series
         *
         * @param name  the name
         * @param c     the class
         */
        public MyTimeSeries(String name, Class c) {
            super(name, c);
        }

        /**
         * Add an item to the timeseries
         *
         * @param item  the item to add
         */
        public void addItem(TimeSeriesDataItem item) {
            if (seen.contains(item)) {
                return;
            }
            seen.add(item);
            items.add(item);
        }

        /**
         * finish this
         */
        public void finish() {
            items = new ArrayList<TimeSeriesDataItem>(Misc.sort(items));

            for (TimeSeriesDataItem item : items) {
                this.data.add(item);
            }
            fireSeriesChanged();
        }


    }


    /**
     * Create the chart
     *
     *
     * @param request  the request
     * @param entry    the entry
     * @param dataset  the dataset
     *
     * @return the chart
     */
    private static JFreeChart createChart(Request request, Entry entry,
                                          XYDataset dataset) {
        LatLonPointImpl llp =
            new LatLonPointImpl(request.getLatOrLonValue(ARG_LOCATION
                + ".latitude", 0), request.getLatOrLonValue(ARG_LOCATION
                               + ".longitude", 0));
        String     title = entry.getName() + " at " + llp.toString();

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
        //entry.getName(),  // title
        title,    // title
        "Date",   // x-axis label
        "",       // y-axis label
        dataset,  // data
        true,     // create legend?
        true,     // generate tooltips?
        false     // generate URLs?
            );

        chart.setBackgroundPaint(Color.white);
        ValueAxis rangeAxis = new NumberAxis("");
        rangeAxis.setVisible(false);
        XYPlot plot = (XYPlot) chart.getPlot();
        if (request.get("gray", false)) {
            plot.setBackgroundPaint(Color.lightGray);
            plot.setDomainGridlinePaint(Color.white);
            plot.setRangeGridlinePaint(Color.white);
        } else {
            plot.setBackgroundPaint(Color.white);
            plot.setDomainGridlinePaint(Color.lightGray);
            plot.setRangeGridlinePaint(Color.lightGray);
        }
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setRangeAxis(0, rangeAxis, false);


        XYItemRenderer r    = plot.getRenderer();
        DateAxis       axis = (DateAxis) plot.getDomainAxis();
        //axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));

        return chart;

    }


}
