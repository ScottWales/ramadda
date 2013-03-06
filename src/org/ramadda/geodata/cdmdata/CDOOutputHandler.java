/*
* Copyright 2008-2013 Jeff McWhirter/ramadda.org
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


import org.ramadda.repository.Entry;
import org.ramadda.repository.Link;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Result;
import org.ramadda.repository.map.MapInfo;
import org.ramadda.repository.map.MapProperties;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TempDir;

import org.w3c.dom.Element;

import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;

import visad.DateTime;


import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Interface to the Climate Data Operators (CDO) package
 */
public class CDOOutputHandler extends OutputHandler {

    /** CDO program path */
    private static final String PROP_CDO_PATH = "cdo.path";

    /** operation identifier */
    private static final String ARG_CDO_OPERATION = "cdo.operation";

    /** start month identifier */
    private static final String ARG_STARTMONTH = "startmonth";

    /** end month identifier */
    private static final String ARG_ENDMONTH = "endmonth";

    /** start month identifier */
    private static final String ARG_STARTYEAR = "startyear";

    /** end month identifier */
    private static final String ARG_ENDYEAR = "endyear";

    /** variable identifier */
    private static final String ARG_PARAM = "param";

    /** end month identifier */
    private static final String ARG_LEVEL = "level";

    /** statistic identifier */
    private static final String ARG_STAT = "stat";

    /** period identifier */
    private static final String ARG_PERIOD = "period";

    /** CDO Output Type */
    public static final OutputType OUTPUT_CDO =
        new OutputType("Data Analysis", "cdo", OutputType.TYPE_OTHER,
                       OutputType.SUFFIX_NONE, "/cdmdata/cdo.png",
                       CdmDataOutputHandler.GROUP_DATA);

    /** info operator */
    private static final String OP_INFO = "info";

    /** short info operator */
    private static final String OP_SINFO = "sinfo";

    /** number of years operator */
    private static final String OP_NYEAR = "nyear";

    /** select years operator */
    private static final String OP_SELYEAR = "-selyear";

    /** select months operator */
    private static final String OP_SELMON = "-selmon";

    /** select seasons operator */
    private static final String OP_SELSEAS = "-selseas";

    /** select date operator */
    private static final String OP_SELDATE = "-seldate";

    /** select llbox operator */
    private static final String OP_SELLLBOX = "-sellonlatbox";

    /** select level operator */
    private static final String OP_SELLEVEL = "-sellevel";

    /** statistic mean */
    private static final String STAT_MEAN = "mean";

    /** statistic standard deviation */
    private static final String STAT_STD = "std";

    /** statistic max */
    private static final String STAT_MAX = "max";

    /** statistic anomaly */
    private static final String STAT_ANOM = "anomaly";

    /** statistic min */
    private static final String STAT_MIN = "min";

    /** year period */
    private static final String PERIOD_YEAR = "year";

    /** month of year period */
    private static final String PERIOD_YMON = "ymon";

    /** month period */
    private static final String PERIOD_MON = "mon";

    /** day of year period */
    private static final String PERIOD_YDAY = "yday";

    /** day period */
    private static final String PERIOD_DAY = "day";

    /** start year */
    private int startYear = 1979;

    /** end year */
    private int endYear = 2011;

    /** info types */
    @SuppressWarnings("unchecked")
    private List<TwoFacedObject> INFO_TYPES = Misc.toList(new Object[] {
                                                  new TwoFacedObject("Info",
                                                      OP_INFO),
            new TwoFacedObject("Short Info", OP_SINFO),
            new TwoFacedObject("Number of Years", OP_NYEAR), });

    /** stat types */
    @SuppressWarnings("unchecked")
    private List<TwoFacedObject> STAT_TYPES = Misc.toList(new Object[] {
                                                  new TwoFacedObject("Mean",
                                                      STAT_MEAN),
            new TwoFacedObject("Std Deviation", STAT_STD),
            new TwoFacedObject("Maximum", STAT_MAX),
            new TwoFacedObject("Minimum", STAT_MIN),
            new TwoFacedObject("Minimum", STAT_ANOM) });

    /** period types */
    @SuppressWarnings("unchecked")
    private List<TwoFacedObject> PERIOD_TYPES =
        Misc.toList(new Object[] { new TwoFacedObject("Annual", PERIOD_YEAR),
                                   new TwoFacedObject("Monthly",
                                       PERIOD_YMON) });

    /** month names */
    private static final String[] MONTH_NAMES = {
        "January", "Febuary", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    };

    /** month numbers */
    private static final int[] MONTH_NUMBERS = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
    };

    /** month list */
    private List<TwoFacedObject> MONTHS =
        TwoFacedObject.createList(MONTH_NUMBERS, MONTH_NAMES);

    /** spatial arguments */
    private static final String[] SPATIALARGS = new String[] { ARG_AREA_NORTH,
            ARG_AREA_WEST, ARG_AREA_SOUTH, ARG_AREA_EAST, };


    /** the product directory */
    private TempDir productDir;

    /** the path to cdo program */
    private String cdoPath;

    /**
     * Create a CDOOutputHandler
     *
     * @param repository  the repository
     *
     * @throws Exception problem during creation
     */
    public CDOOutputHandler(Repository repository) throws Exception {
        super(repository, "CDO");
        cdoPath = getProperty(PROP_CDO_PATH, null);
    }

    /**
     * Constructor
     *
     * @param repository   the Repository
     * @param element      the Element
     * @throws Exception   problem creating handler
     */
    public CDOOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_CDO);
        cdoPath = getProperty(PROP_CDO_PATH, null);
    }


    /**
     * Check to see if we have cdo installed
     *
     * @return  true if path to cdo is set
     */
    private boolean haveCdo() {
        return cdoPath != null;
    }


    /**
     * This method gets called to determine if the given entry or entries can be displays as las xml
     *
     * @param request  the Request
     * @param state    the State
     * @param links    the list of Links to add to
     *
     * @throws Exception Problem adding links
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if ( !haveCdo()) {
            return;
        }
        if ((state.entry != null) && state.entry.isFile()
                && state.entry.getResource().getPath().endsWith(".nc")) {
            links.add(makeLink(request, state.entry, OUTPUT_CDO));
        }
    }

    /**
     * Get the product directory
     *
     * @return  the directory
     *
     * @throws Exception  problem getting directory
     */
    private File getProductDir() throws Exception {
        if (productDir == null) {
            TempDir tempDir = getStorageManager().makeTempDir("cdoproducts");
            //keep things around for 1 hour
            tempDir.setMaxAge(1000 * 60 * 60 * 1);
            productDir = tempDir;
        }

        return productDir.getDir();
    }



    /**
     * Get the data output handler
     *
     * @return the handler
     *
     * @throws Exception Problem getting that
     */
    public CdmDataOutputHandler getDataOutputHandler() throws Exception {
        return (CdmDataOutputHandler) getRepository().getOutputHandler(
            CdmDataOutputHandler.OUTPUT_OPENDAP.toString());
    }




    /**
     * Create the entry display
     *
     * @param request   the Request
     * @param outputType  the output type
     * @param entry     the entry to output
     *
     * @return the entry or form
     *
     * @throws Exception problem making the form
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {


        if (request.defined(ARG_SUBMIT)) {
            return outputCDO(request, entry);
        }
        StringBuffer sb = new StringBuffer();
        addForm(request, entry, sb);

        return new Result("CDO Form", sb);
    }

    /**
     * Add the form
     *
     * @param request  the Request
     * @param entry    the Entry
     * @param sb       the HTML
     *
     * @throws Exception problems
     */
    private void addForm(Request request, Entry entry, StringBuffer sb)
            throws Exception {

        String formUrl = request.url(getRepository().URL_ENTRY_SHOW);
        sb.append(HtmlUtils.form(formUrl));
        /*
        sb.append(HtmlUtils.form(formUrl,
                                 makeFormSubmitDialog(sb,
                                     msg("Analyzing Data...."))));
        */

        sb.append(HtmlUtils.hidden(ARG_OUTPUT, OUTPUT_CDO));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        String buttons = HtmlUtils.submit("Extract Data", ARG_SUBMIT);
        //sb.append(buttons);
        sb.append(HtmlUtils.h2("Dataset Analysis"));
        sb.append(HtmlUtils.hr());
        addToForm(request, entry, sb);
        sb.append(buttons);
        sb.append(" ");
        addPublishWidget(
            request, entry, sb,
            msg("Select a folder to publish the generated NetCDF file to"));

        /*
        sb.append(
            HtmlUtils.href(
                "https://code.zmaw.de/projects/cdo/wiki/Cdo#Documentation",
                "CDO Documentation", " target=_external "));
                */

    }

    /**
     * Add this output handlers UI to the form
     *
     * @param request   the Request
     * @param entry     the Entry
     * @param sb        the form HTML
     *
     * @throws Exception  on badness
     */
    public void addToForm(Request request, Entry entry, StringBuffer sb)
            throws Exception {
        sb.append(HtmlUtils.formTable());
        if (entry.getType().equals("noaa_climate_modelfile")) {
            //values[1] = var;
            //values[2] = model;
            //values[3] = experiment;
            //values[4] = member;
            //values[5] = frequency;
            Object[]     values = entry.getValues();
            StringBuffer header = new StringBuffer();
            header.append("Model: ");
            header.append(values[2]);
            header.append(" Experiment: ");
            header.append(values[3]);
            header.append(" Ensemble: ");
            header.append(values[4]);
            header.append(" Frequency: ");
            header.append(values[5]);
            //sb.append(HtmlUtils.h3(header.toString()));
        }

        //addInfoWidget(request, sb);
        CdmDataOutputHandler dataOutputHandler = getDataOutputHandler();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().getGridDataset(entry,
                entry.getResource().getPath());

        if (dataset != null) {
            addVarLevelWidget(request, sb, dataset);
        }

        addStatsWidget(request, sb);

        //if(dataset != null)  {
        addTimeWidget(request, sb, dataset, true);
        //}

        LatLonRect llr = null;
        if (dataset != null) {
            llr = dataset.getBoundingBox();
        } else {
            llr = new LatLonRect(new LatLonPointImpl(90.0, 0.0),
                                 new LatLonPointImpl(-90.0, 360.0));
        }
        addMapWidget(request, sb, llr);
        sb.append(HtmlUtils.formTableClose());
    }

    /**
     * Add the statitics widget
     *
     * @param request  the Request
     * @param sb       the HTML
     */
    private void addStatsWidget(Request request, StringBuffer sb) {
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Statistic"),
                msgLabel("Period")
                + HtmlUtils.select(ARG_PERIOD, PERIOD_TYPES)
                + HtmlUtils.space(5) + msgLabel("Type")
                + HtmlUtils.select(ARG_STAT, STAT_TYPES)));
    }


    /**
     * Add the variable/level selector widget
     *
     * @param request  the Request
     * @param sb       the HTML
     * @param dataset  the dataset
     */
    private void addVarLevelWidget(Request request, StringBuffer sb,
                                   GridDataset dataset) {
        List<GridDatatype> grids = dataset.getGrids();
        StringBuffer       varsb = new StringBuffer();
        //TODO: handle multiple variables
        //List<TwoFacedObject> varList = new ArrayList<TwoFacedObject>(grids.size());
        //for (GridDatatype grid : dataset.getGrids()) {
        //    varList.add(new TwoFacedObject(grid.getDescription(), grid.getName()));
        //}
        //varsb.append(HtmlUtils.select(ARG_PARAM, varList));
        GridDatatype grid = grids.get(0);
        varsb.append(grid.getDescription());
        if (grid.getZDimension() != null) {
            varsb.append(HtmlUtils.space(5));
            varsb.append(msgLabel("Level"));
            GridCoordSystem      gcs    = grid.getCoordinateSystem();
            CoordinateAxis1D     zAxis  = gcs.getVerticalAxis();
            int                  sizeZ  = (int) zAxis.getSize();
            String               unit   =
                zAxis.getUnitsString().toLowerCase();
            List<TwoFacedObject> levels =
                new ArrayList<TwoFacedObject>(sizeZ);
            // TODO: Gotta be a better way to do this.
            for (int i = 0; i < sizeZ; i++) {
                int    lev   = (int) zAxis.getCoordValue(i);
                String label = String.valueOf(unit.startsWith("pa")
                        ? lev / 100
                        : lev);
                levels.add(new TwoFacedObject(label, String.valueOf(lev)));
            }
            varsb.append(HtmlUtils.select(ARG_LEVEL, levels));
            varsb.append(HtmlUtils.space(2));
            varsb.append("hPa");
        }
        sb.append(HtmlUtils.formEntry(msgLabel("Variable"),
                                      varsb.toString()));
    }


    /**
     * Get the grid dates
     *
     * @param dataset  the dataset
     *
     * @return  the dates or empty list if dataset is null
     */
    private List<Date> getGridDates(GridDataset dataset) {
        List<Date> gridDates = null;
        if (dataset == null) {
            return new ArrayList<Date>();
        }
        List<GridDatatype> grids    = dataset.getGrids();
        HashSet<Date>      dateHash = new HashSet<Date>();
        List<CoordinateAxis1DTime> timeAxes =
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
     * Add the months widget
     *
     * @param request  the Request
     * @param sb       the StringBuffer to add to
     */
    private void addInfoWidget(Request request, StringBuffer sb) {
        sb.append(HtmlUtils.formEntry(msgLabel("Months"),
                                      HtmlUtils.select(ARG_CDO_OPERATION,
                                          INFO_TYPES)));
    }

    /**
     * Add a time widget
     *
     * @param request  the Request
     * @param sb       the HTML page
     * @param dataset  the GridDataset
     * @param useYYMM  true to provide month/year widgets, otherwise straight dates
     */
    private void addTimeWidget(Request request, StringBuffer sb,
                               GridDataset dataset, boolean useYYMM) {
        List<Date> dates = getGridDates(dataset);

        //if ((dates != null) && (!dates.size() > 0)) {
        if (useYYMM) {
            makeMonthsWidget(request, sb, dates);
            makeYearsWidget(request, sb, dates);
        } else {
            makeTimesWidget(request, sb, dates);
        }
        //}
    }

    /**
     * Add at time widget
     *
     * @param request  the Request
     * @param sb       the HTML
     * @param dates    the list of Dates
     */
    private void makeTimesWidget(Request request, StringBuffer sb,
                                 List<Date> dates) {
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

    /**
     * Add the month selection widget
     *
     * @param request  the Request
     * @param sb       the StringBuffer to add to
     * @param dates    the list of dates (just in case)
     */
    private void makeMonthsWidget(Request request, StringBuffer sb,
                                  List<Date> dates) {
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Months"),
                msgLabel("Start") + HtmlUtils.select(ARG_STARTMONTH, MONTHS)
                + HtmlUtils.space(5) + msgLabel("End")
                + HtmlUtils.select(ARG_ENDMONTH, MONTHS)));
    }

    /**
     * Add the year selection widget
     *
     * @param request  the Request
     * @param sb       the StringBuffer to add to
     * @param dates    the list of dates
     */
    private void makeYearsWidget(Request request, StringBuffer sb,
                                 List<Date> dates) {
        SortedSet<String> uniqueYears =
            Collections.synchronizedSortedSet(new TreeSet<String>());
        if (dates != null) {
            for (Date d : dates) {
                try {  // shouldn't get an exception
                    String year = new DateTime(d).formattedString("yyyy",
                                      DateTime.DEFAULT_TIMEZONE);
                    uniqueYears.add(year);
                } catch (Exception e) {}
            }
        }
        List<String> years = new ArrayList<String>(uniqueYears);
        // TODO:  make a better list of years
        if (years.isEmpty()) {
            for (int i = startYear; i <= endYear; i++) {
                years.add(String.valueOf(i));
            }
        }

        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Years"),
                msgLabel("Start") + HtmlUtils.select(ARG_STARTYEAR, years)
                + HtmlUtils.space(10) + msgLabel("End")
                + HtmlUtils.select(ARG_ENDYEAR, years)));
    }

    /**
     * Add the map widget
     *
     * @param request   The request
     * @param sb        the HTML
     * @param llr       the lat/lon rectangle
     */
    private void addMapWidget(Request request, StringBuffer sb,
                              LatLonRect llr) {

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
        sb.append(HtmlUtils.formEntryTop(msgLabel("Region"), llb));
    }

    /**
     * Output the cdo request
     *
     * @param request  the request
     * @param entry    the entry
     *
     * @return  the output
     *
     * @throws Exception  problem executing the command
     */
    public Result outputCDO(Request request, Entry entry) throws Exception {

        String tail    = getStorageManager().getFileTail(entry);
        String newName = IOUtil.stripExtension(tail) + "_product.nc";
        tail = getStorageManager().getStorageFileName(tail);
        File outFile = new File(IOUtil.joinDir(getProductDir(), newName));
        List<String> commands = new ArrayList<String>();
        commands.add(cdoPath);
        commands.add("-L");
        commands.add("-s");
        commands.add("-O");
        String operation = request.getString(ARG_CDO_OPERATION, OP_INFO);
        //commands.add(operation);

        // Select order (left to right) - operations go right to left:
        //   - stats
        //   - level
        //   - region
        //   - month range
        //   - year or time range

        List<String> statCommands = createStatCommands(request, entry);
        for (String cmd : statCommands) {
            if ((cmd != null) && !cmd.isEmpty()) {
                commands.add(cmd);
            }
        }

        String levSelect = createLevelSelectCommand(request, entry);
        if ((levSelect != null) && !levSelect.isEmpty()) {
            commands.add(levSelect);
        }
        String areaSelect = createAreaSelectCommand(request, entry);
        if ((areaSelect != null) && !areaSelect.isEmpty()) {
            commands.add(areaSelect);
        }

        List<String> dateCmds = createDateSelectCommands(request, entry);
        for (String cmd : dateCmds) {
            if ((cmd != null) && !cmd.isEmpty()) {
                commands.add(cmd);
            }
        }

        System.err.println("cmds:" + commands);

        commands.add(entry.getResource().getPath());
        commands.add(outFile.toString());
        String[] results = getRepository().executeCommand(commands, null,
                               getProductDir());
        String errorMsg = results[1];
        String outMsg   = results[0];
        if ( !outFile.exists()) {
            if (outMsg.length() > 0) {
                return getErrorResult(request, "CDO-Error",
                                      "An error occurred:<br>" + outMsg);
            }
            if (errorMsg.length() > 0) {
                return getErrorResult(request, "CDO-Error",
                                      "An error occurred:<br>" + errorMsg);
            }
            if ( !outFile.exists()) {
                return getErrorResult(
                    request, "CDO-Error",
                    "Humm, the CDO analysis failed for some reason");
            }
        }

        //The jeff is here for when I have a fake cdo.sh
        boolean jeff = true;


        if (doingPublish(request)) {
            if ( !request.defined(ARG_PUBLISH_NAME)) {
                request.put(ARG_PUBLISH_NAME, newName);
            }

            return getEntryManager().processEntryPublish(request, outFile,
                    null, entry, "generated from");
        }

        //Assuming this is some text - DOESN'T HAPPEN anymore
        if (operation.equals(OP_INFO) && false) {
            String info;

            if ( !jeff) {
                info = IOUtil.readInputStream(
                    getStorageManager().getFileInputStream(outFile));
            } else {
                info = outMsg;
            }

            StringBuffer sb = new StringBuffer();
            addForm(request, entry, sb);
            sb.append(header(msg("CDO Information")));
            sb.append(HtmlUtils.pre(info));

            return new Result("CDO", sb);
        }



        return request.returnFile(
            outFile, getStorageManager().getFileTail(outFile.toString()));
    }

    /**
     * Set the start year
     *
     * @param start  start year
     */
    public void setStartYear(int start) {
        startYear = start;
    }

    /**
     * Set the end year
     *
     * @param end end year
     */
    public void setEndYear(int end) {
        endYear = end;
    }

    /**
     * Create the CDO command to select an area
     *
     * @param request  the Request
     * @param entry    the Entry
     *
     * @return the select command
     */
    public static String createAreaSelectCommand(Request request,
            Entry entry) {
        boolean anySpatialDifferent = false;
        boolean haveAllSpatialArgs  = true;
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

        String llSelect = "";
        if (haveAllSpatialArgs && anySpatialDifferent) {
            llSelect = OP_SELLLBOX + ","
                       + request.getString(ARG_AREA_WEST, "0") + ","
                       + request.getString(ARG_AREA_EAST, "360") + ","
                       + request.getString(ARG_AREA_SOUTH, "-90") + ","
                       + request.getString(ARG_AREA_NORTH, "90");
        }

        return llSelect;
    }

    /**
     * Create the region subset command
     * @param request  the Request
     * @param entry    the Entry
     * @return  the subset command.   Will be empty if no subset
     */
    public static String createLevelSelectCommand(Request request,
            Entry entry) {
        String levSelect = null;
        if (request.defined(ARG_LEVEL)) {
            String level = request.getString(ARG_LEVEL);
            if (level != null) {
                levSelect = OP_SELLEVEL + "," + level;
            }
        }

        return levSelect;
    }

    /**
     * Create the list of date/time select commands
     * @param request the Request
     * @param entry   the associated Entry
     * @return  a list of date select commands (may be empty list)
     *
     * @throws Exception  on badness
     */
    public List<String> createDateSelectCommands(Request request, Entry entry)
            throws Exception {

        List<String> commands = new ArrayList<String>();
        String       selMonth = null;
        if (request.defined(ARG_STARTMONTH)
                || request.defined(ARG_ENDMONTH)) {
            int startMonth = request.defined(ARG_STARTMONTH)
                             ? request.get(ARG_STARTMONTH, 1)
                             : 1;
            int endMonth   = request.defined(ARG_ENDMONTH)
                             ? request.get(ARG_ENDMONTH, startMonth)
                             : startMonth;
            if (endMonth < startMonth) {
                getRepository().showDialogWarning(
                    "Start month is after end month");
            }
            selMonth = OP_SELMON + "," + startMonth;
            if (endMonth != startMonth) {
                selMonth += "/" + endMonth;
            }
            commands.add(selMonth);
        }

        String dateSelect = null;
        if (request.defined(ARG_FROMDATE) || request.defined(ARG_TODATE)) {

            Date[] dates = new Date[] { request.defined(ARG_FROMDATE)
                                        ? request.getDate(ARG_FROMDATE, null)
                                        : null, request.defined(ARG_TODATE)
                    ? request.getDate(ARG_TODATE, null)
                    : null };
            //have to have both dates
            if ((dates[0] != null) && (dates[1] == null)) {
                dates[0] = null;
            }
            if ((dates[1] != null) && (dates[0] == null)) {
                dates[1] = null;
            }
            if ((dates[0] != null) && (dates[1] != null)) {
                if (dates[0].getTime() > dates[1].getTime()) {
                    getRepository().showDialogWarning(
                        "From date is after to date");
                } else {
                    dateSelect = OP_SELDATE + ","
                                 + DateUtil.getTimeAsISO8601(dates[0]) + ","
                                 + DateUtil.getTimeAsISO8601(dates[1]);
                }
            }
        } else if (request.defined(ARG_STARTYEAR)
                   || request.defined(ARG_ENDYEAR)) {
            String[] years = new String[] { request.defined(ARG_STARTYEAR)
                                            ? request.getString(
                                                ARG_STARTYEAR, null)
                                            : null, request.defined(
                                                ARG_ENDYEAR)
                    ? request.getString(ARG_ENDYEAR, null)
                    : null };
            //have to have both dates
            if ((years[0] != null) && (years[1] == null)) {
                years[0] = null;
            }
            if ((years[1] != null) && (years[0] == null)) {
                years[1] = null;
            }
            if ((years[0] != null) && (years[1] != null)) {
                if (years[0].compareTo(years[1]) > 0) {
                    getRepository().showDialogWarning(
                        "Start year is after end year");
                } else {
                    dateSelect = OP_SELYEAR + "," + years[0] + "/" + years[1];
                }
            }

        }
        if (dateSelect != null) {
            commands.add(dateSelect);
        }

        return commands;
    }

    /**
     * Creat the statistics command
     * @param request  the request
     * @param entry    the entry
     * @return         the list of commands
     */
    public List<String> createStatCommands(Request request, Entry entry) {
        List<String> commands = new ArrayList<String>();
        if (request.defined(ARG_PERIOD) && request.defined(ARG_STAT)) {
            String period = request.getString(ARG_PERIOD);
            String stat   = request.getString(ARG_STAT);
            if ((period == null) || (stat == null)) {
                return commands;
            }
            // TODO:  Handle anomaly
            if (stat.equals(STAT_ANOM)) {
                stat = STAT_MEAN;
            }
            if (period.equals(PERIOD_YEAR)) {
                commands.add("-" + PERIOD_YEAR + stat);
                commands.add("-" + PERIOD_YMON + stat);
            }
            if (period.equals(PERIOD_YMON)) {
                commands.add("-" + PERIOD_YMON + stat);
            }
        }

        return commands;
    }
}
