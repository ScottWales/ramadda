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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import org.ramadda.repository.Entry;
import org.ramadda.repository.Link;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Resource;
import org.ramadda.repository.Result;
import org.ramadda.repository.Service;
import org.ramadda.repository.auth.AccessException;
import org.ramadda.repository.auth.AuthorizationMethod;
import org.ramadda.repository.auth.Permission;
import org.ramadda.repository.map.MapInfo;
import org.ramadda.repository.map.MapProperties;
import org.ramadda.repository.metadata.ContentMetadataHandler;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputHandler.State;
import org.ramadda.repository.output.OutputType;
import org.ramadda.repository.type.TypeHandler;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TempDir;

import org.w3c.dom.Element;

import thredds.server.ncSubset.GridPointWriter;
import thredds.server.ncSubset.QueryParams;
import thredds.server.opendap.GuardedDatasetImpl;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.StructureData;
import ucar.ma2.StructureMembers;

import ucar.nc2.NetcdfFile;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableEnhanced;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.TrajectoryObsDataset;
import ucar.nc2.dt.TrajectoryObsDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.dt.grid.NetcdfCFWriter;
import ucar.nc2.ft.FeatureCollection;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.NestedPointFeatureCollection;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureCollection;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ncml.NcMLWriter;
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


import java.awt.Color;
import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;


/**
 * A class for handling CDM data output
 */
public class CdmDataOutputHandler extends OutputHandler {

    /** OPeNDAP icon */
    public static final String ICON_OPENDAP = "/cdmdata/opendap.gif";

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

    /** GrADS type */
    public static final String TYPE_GRADS = "gradsbinary";

    /** set of suffixes */
    private HashSet<String> suffixSet;

    /** hash of patterns */
    private Hashtable<String, List<Pattern>> patterns;

    /** not patterns */
    private Hashtable<String, List<Pattern>> notPatterns;

    /** OPeNDAP Output Type */
    public static final OutputType OUTPUT_OPENDAP =
        new OutputType("OPeNDAP", "data.opendap", OutputType.TYPE_FEEDS,
                       OutputType.SUFFIX_NONE, ICON_OPENDAP, GROUP_DATA);

    /** CDL Output Type */
    public static final OutputType OUTPUT_CDL =
        new OutputType("File Metadata", "data.cdl", OutputType.TYPE_OTHER,
                       OutputType.SUFFIX_NONE, "/cdmdata/page_white_text.png",
                       GROUP_DATA);

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
        new OutputType("Show track on Map", "data.trajectory.map",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       ICON_MAP, GROUP_DATA);

    /** Grid subset form Output Type */
    public static final OutputType OUTPUT_GRIDSUBSET_FORM =
        new OutputType("Subset Grid", "data.gridsubset.form",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       "/cdmdata/subsetgrid.png", GROUP_DATA);

    /** Grid subset Output Type */
    public static final OutputType OUTPUT_GRIDSUBSET =
        new OutputType("data.gridsubset", OutputType.TYPE_FEEDS);

    /** Grid as point form Output Type */
    public static final OutputType OUTPUT_GRIDASPOINT_FORM =
        new OutputType("Extract Time Series", "data.gridaspoint.form",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       "/cdmdata/chart_line.png", GROUP_DATA);

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

    /** the CDM manager */
    private static CdmManager cdmManager;

    /**
     * Get the CdmManager
     *
     * @return  the CDM data manager
     */
    public CdmManager getCdmManager() {
        if (cdmManager == null) {
            try {
                getRepository().addRepositoryManager(cdmManager =
                    new CdmManager(getRepository()));
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }

        return cdmManager;
    }



    /**
     * Create a new CdmDataOutputHandler
     *
     * @param repository  the repository
     * @param name        the name of this handler
     *
     * @throws Exception problem creating class
     */
    public CdmDataOutputHandler(Repository repository, String name)
            throws Exception {
        super(repository, name);
    }

    /**
     *     Create a CdmDataOutputHandler
     *
     *     @param repository  the repository
     *     @param element     the element
     *     @throws Exception On badness
     */
    public CdmDataOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        getCdmManager();
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
        getCdmManager().getSystemStats(sb);
    }


    /**
     * clear the cache
     */
    public void clearCache() {
        super.clearCache();
        getCdmManager().clearCache();
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
        if ( !getCdmManager().canLoadAsCdm(entry)) {
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

        if ((state.group != null)
                && getCdmManager().isAggregation(state.group)) {
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
        boolean canLoadAsCdm = getCdmManager().canLoadAsCdm(entry);

        if ( !canLoadAsCdm) {
            long t2 = System.currentTimeMillis();
            if ((t2 - t1) > 1) {
                //                System.err.println("CdmDataOutputHandler (cdm) getEntryLinks  "
                //                                   + entry.getName() + " time:" + (t2 - t1));
            }

            return;
        }

        if (getCdmManager().canLoadAsGrid(entry)) {
            addOutputLink(request, entry, links, OUTPUT_GRIDSUBSET_FORM);
            addOutputLink(request, entry, links, OUTPUT_GRIDASPOINT_FORM);
        } else if (getCdmManager().canLoadAsTrajectory(entry)) {
            addOutputLink(request, entry, links, OUTPUT_TRAJECTORY_MAP);
        } else if (getCdmManager().canLoadAsPoint(entry)) {
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
            //            System.err.println("CdmDataOutputHandler  getEntryLinks  "
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
        String path     = getPath(request, entry);
        String dodspath = getAbsoluteOpendapUrl(request, entry);
        if (request.getString(ARG_FORMAT, "").equals(FORMAT_NCML)) {

            /**
             *  This gets hung up calling back into the repository
             *  so for now don't do it and just use the file
             * path = getAbsoluteOpendapUrl(request, entry);
             */

            NetcdfFile ncFile = NetcdfDataset.openFile(path, null);
            NcMLWriter writer = new NcMLWriter();
            String     xml    = writer.writeXML(ncFile);
            xml = xml.replace("file:" + path, dodspath).replace(path,
                              dodspath);
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
            sb.append(
                HtmlUtils.span(
                    "&nbsp;|&nbsp;",
                    HtmlUtils.cssClass(CSS_CLASS_SEPARATOR)));

            sb.append(HtmlUtils.href(request.getUrl(),
                                     msg("Add full properties")));
            sb.append(
                HtmlUtils.span(
                    "&nbsp;|&nbsp;",
                    HtmlUtils.cssClass(CSS_CLASS_SEPARATOR)));
        }
        String tail =
            IOUtil.stripExtension(getStorageManager().getFileTail(entry));

        sb.append(HtmlUtils.href(HtmlUtils.url(getRepository().URL_ENTRY_SHOW
                + "/" + tail + SUFFIX_NCML, new String[] {
            ARG_ENTRYID, entry.getId(), ARG_OUTPUT, OUTPUT_CDL.getId(),
            ARG_FORMAT, FORMAT_NCML
        }), "NCML"));


        NetcdfDataset dataset = getCdmManager().createNetcdfDataset(path);
        if (dataset == null) {
            sb.append("Could not open dataset");
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ucar.nc2.NCdump.print(dataset, "", bos, null);
            String cdl = bos.toString();
            cdl = cdl.replace("file:" + path, dodspath).replace(path,
                              dodspath);
            sb.append("<pre>" + cdl + "</pre>");
            getCdmManager().returnNetcdfDataset(path, dataset);
        }

        return makeLinksResult(request, "CDL", sb, new State(entry));
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
                                        .getStorageManager().getScratchDir()
                                        .getDir().toString(), false, 0, 0));
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
        //                   FORMAT_TIMESERIES_CHART),
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
            String call  =
                HtmlUtils.attr(HtmlUtils.ATTR_ONCLICK,
                               HtmlUtils.call("checkboxClicked",
                                   HtmlUtils.comma("event",
                                       HtmlUtils.squote(ARG_VARIABLE),
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
                varSB.append(
                    HtmlUtils.row(
                        HtmlUtils.headerCols(new Object[] { "2D Grids" })));
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
                varSB.append(
                    HtmlUtils.row(
                        HtmlUtils.headerCols(new Object[] { header })));
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

        GridDataset  gds    = getCdmManager().getGridDataset(entry, path);
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
            getCdmManager().returnGridDataset(path, gds);
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
                GridDataset gds = getCdmManager().getGridDataset(entry, path);
                writer.makeFile(f.toString(), gds, varNames, llr,
                                ((dates[0] == null)
                                 ? null
                                 : new ucar.nc2.units.DateRange(dates[0],
                                 dates[1])), includeLatLon, hStride, zStride,
                                             timeStride);
                getCdmManager().returnGridDataset(path, gds);

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
                                          request.getString(ARG_HSTRIDE,
                                              "1"), HtmlUtils.SIZE_3)));

        GridDataset  dataset   = getCdmManager().getGridDataset(entry, path);
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
        getCdmManager().returnGridDataset(path, dataset);

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
        String              path = getPath(request, entry);
        FeatureDatasetPoint pod  = getCdmManager().getPointDataset(entry,
                                      path);

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
            columnDefs.add("{key:" + HtmlUtils.quote(var.getShortName())
                           + "," + "sortable:true," + "label:"
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
        getCdmManager().returnPointDataset(path, pod);

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
        TrajectoryObsDataset tod  = getCdmManager().getTrajectoryDataset(path);
        StringBuffer         sb   = new StringBuffer();

        MapInfo map = getRepository().getMapManager().createMap(request, 800,
                          600, false);
        List trajectories = tod.getTrajectories();
        //TODO: Use new openlayers map
        for (int i = 0; i < trajectories.size(); i++) {
            List                  allVariables = tod.getDataVariables();
            TrajectoryObsDatatype todt         =
                (TrajectoryObsDatatype) trajectories.get(i);
            float[] lats    = toFloatArray(todt.getLatitude(null));
            float[] lons    = toFloatArray(todt.getLongitude(null));
            float   lastLat = 0,
                    lastLon = 0;
            int     stride  = lats.length / 500;
            for (int ptIdx = 0; ptIdx < lats.length; ptIdx += stride) {
                float lat = lats[ptIdx];
                float lon = lons[ptIdx];
                if (ptIdx > 0) {
                    if (ptIdx + stride >= lats.length) {
                        map.addMarker("", lat, lon, null,
                                      "End time:" + todt.getEndDate());
                    }
                    //#FF0000
                    map.addLine("", lastLat, lastLon, lat, lon);
                } else {
                    map.addMarker("", lat, lon, null,
                                  "Start time:" + todt.getEndDate());
                }
                lastLat = lat;
                lastLon = lon;
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

        map.centerOn(entry);
        sb.append(map.getHtml());
        getCdmManager().returnTrajectoryDataset(path, tod);

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
     * Get the services for the request
     *
     * @param request  the Request
     * @param entry    the Entry
     * @param services  the list of services
     */
    @Override
    public void getServices(Request request, Entry entry,
                            List<Service> services) {
        super.getServices(request, entry, services);
        if ( !getCdmManager().canLoadAsCdm(entry)) {
            return;
        }
        String url = getAbsoluteOpendapUrl(request, entry);
        services.add(new Service("opendap", "OPeNDAP Link", url));
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
        String              path = getPath(request, entry);
        FeatureDatasetPoint pod  = getCdmManager().getPointDataset(entry,
                                      path);;
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
        getCdmManager().returnPointDataset(path, pod);

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
        String              path = getPath(request, entry);
        FeatureDatasetPoint pod  = getCdmManager().getPointDataset(entry,
                                      path);
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
        getCdmManager().returnPointDataset(path, pod);
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
        if (getCdmManager().isAggregation(group)) {
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
        return getCdmManager().getPath(request, entry);
        /*
        String location;
        if (entry.getType().equals(OpendapLinkTypeHandler.TYPE_OPENDAPLINK)) {
            Resource resource = entry.getResource();
            location = resource.getPath();
            String ext = IOUtil.getFileExtension(location).toLowerCase();
            if (ext.equals(".html") || ext.equals(".das")
                    || ext.equals(".dds")) {
                location = IOUtil.stripExtension(location);
            }
        } else if (getCdmManager().isAggregation(entry)) {
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
                        //ncml = ncml.replaceAll("(dset|DSET).*\n",
                        //        "nDSET " + location + "\n");
                        ncml = Pattern.compile(
                            "^dset.*$",
                            Pattern.MULTILINE
                            | Pattern.CASE_INSENSITIVE).matcher(
                                ncml).replaceAll("DSET " + location);
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
        */
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

        NetcdfFile ncFile = getCdmManager().createNetcdfFile(location);
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
        getCdmManager().returnNetcdfFile(location, ncFile);

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
