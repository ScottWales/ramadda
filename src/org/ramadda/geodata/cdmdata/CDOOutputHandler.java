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
import org.ramadda.repository.output.OutputHandler.State;
import org.ramadda.repository.output.OutputType;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TempDir;

import org.w3c.dom.Element;

import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;

import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;

import ucar.visad.UtcDate;

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
 * Class description
 *
 *
 * @version        $version$, Mon, Feb 11, '13
 * @author         Enter your name here...
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

    /** CDO Output Type */
    public static final OutputType OUTPUT_CDO =
        new OutputType("CDO Analysis", "cdo", OutputType.TYPE_OTHER,
                       OutputType.SUFFIX_NONE, "/cdmdata/cdo.png",
                       CdmDataOutputHandler.GROUP_DATA);

    /** info operator */
    private static final String OP_INFO = "info";

    /** short info operator */
    private static final String OP_SINFO = "sinfo";

    /** number of years operator */
    private static final String OP_NYEAR = "nyear";

    /** select years operator */
    private static final String OP_SELYEAR = "selyear";

    /** select months operator */
    private static final String OP_SELMON = "selmon";

    /** select seasons operator */
    private static final String OP_SELSEAS = "selseas";

    /** select date operator */
    private static final String OP_SELDATE = "seldate";

    /** select llbox operator */
    private static final String OP_SELLLBOX = "sellonlatbox";

    /** statistic mean */
    private static final String STAT_MEAN = "mean";

    /** statistic standard deviation */
    private static final String STAT_STD = "std";

    /** statistic max */
    private static final String STAT_MAX = "max";

    /** statistic min */
    private static final String STAT_MIN = "min";

    /** year period */
    private static final String PERIOD_YEAR = "year";

    /** month of year period */
    private static final String PERIOD_YMON = "ymon";

    /** month period */
    private static final String PERIOD_MON = "mon";

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
            new TwoFacedObject("Minimum", STAT_MIN) });

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
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
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
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    private void addForm(Request request, Entry entry, StringBuffer sb)
            throws Exception {

        String formUrl = request.url(getRepository().URL_ENTRY_SHOW);
        sb.append(HtmlUtils.form(formUrl,
                                 makeFormSubmitDialog(sb,
                                     msg("Apply CDO..."))));

        String buttons = HtmlUtils.submit("Extract Data", ARG_SUBMIT);
        //sb.append(buttons);

        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.hidden(ARG_OUTPUT, OUTPUT_CDO));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));

        //addInfoWidget(request, sb);
        CdmDataOutputHandler dataOutputHandler = getDataOutputHandler();
        //NetcdfDataset dataset =
        //    NetcdfDataset.openDataset(entry.getResource().getPath());
        //dataset.close();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().getGridDataset(entry,
                entry.getResource().getPath());
        addTimeWidget(request, sb, dataset);

        LatLonRect llr = dataset.getBoundingBox();
        if (llr != null) {
            addMapWidget(request, sb, llr);
        }



        addPublishWidget(
            request, entry, sb,
            msg("Select a folder to publish the generated NetCDF file to"));

        sb.append(HtmlUtils.formTableClose());
        sb.append(buttons);
        sb.append(" ");
        sb.append(
            HtmlUtils.href(
                "https://code.zmaw.de/projects/cdo/wiki/Cdo#Documentation",
                "CDO Documentation", " target=_external "));

    }

    /**
     * Get the grid dates
     *
     * @param dataset  the dataset
     *
     * @return  the dates or null
     */
    private List<Date> getGridDates(GridDataset dataset) {
        List<Date>         gridDates = null;
        List<GridDatatype> grids     = dataset.getGrids();
        HashSet<Date>      dateHash  = new HashSet<Date>();
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
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param dataset _more_
     */
    private void addTimeWidget(Request request, StringBuffer sb,
                               GridDataset dataset) {
        List<Date> dates = getGridDates(dataset);

        if ((dates != null) && (dates.size() > 0)) {
            addMonthsWidget(request, sb, dates);
            addYearsWidget(request, sb, dates);
            //addTimesWidget(request, sb, dates);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param dates _more_
     */
    private void addTimeWidget(Request request, StringBuffer sb,
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
     * @param dates _more_
     */
    private void addMonthsWidget(Request request, StringBuffer sb,
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
     * @param dates _more_
     */
    private void addYearsWidget(Request request, StringBuffer sb,
                                List<Date> dates) {
        SortedSet<String> uniqueYears =
            Collections.synchronizedSortedSet(new TreeSet<String>());
        for (Date d : dates) {
            try {  // shouldn't get an exception
                String year = new DateTime(d).formattedString("yyyy",
                                           DateTime.DEFAULT_TIMEZONE);
                uniqueYears.add(year);
            } catch (Exception e) {}
        }
        List<String> years = new ArrayList<String>(uniqueYears);
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Years"),
                msgLabel("Start") + HtmlUtils.select(ARG_STARTYEAR, years)
                + HtmlUtils.space(10) + msgLabel("End")
                + HtmlUtils.select(ARG_ENDYEAR, years)));
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param llr _more_
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
        File         outFile = new File(IOUtil.joinDir(getProductDir(),
                                   tail));
        List<String> commands            = new ArrayList<String>();
        boolean      anySpatialDifferent = false;
        boolean      haveAllSpatialArgs  = true;
        commands.add(cdoPath);
        commands.add("-L");
        commands.add("-s");
        commands.add("-O");
        String operation = request.getString(ARG_CDO_OPERATION, OP_INFO);
        //commands.add(operation);

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

        String llSelect = null;
        if (haveAllSpatialArgs && anySpatialDifferent) {
            llSelect = "-" + OP_SELLLBOX + ","
                       + request.getString(ARG_AREA_WEST, "0") + ","
                       + request.getString(ARG_AREA_EAST, "360.0") + ","
                       + request.getString(ARG_AREA_SOUTH, "-90.0") + ","
                       + request.getString(ARG_AREA_NORTH, "90.0");
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
                    dateSelect = "-" + OP_SELDATE + ","
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
                        "From year is after to year");
                } else {
                    dateSelect = "-" + OP_SELYEAR + "," + years[0] + "/"
                                 + years[1];
                }
            }

        }
        if (llSelect != null) {
            commands.add(llSelect);
        }
        if (dateSelect != null) {
            commands.add(dateSelect);
        }

        System.err.println("cmds:" + commands);

        commands.add(entry.getResource().getPath());
        commands.add(outFile.toString());
        ProcessBuilder pb = new ProcessBuilder(commands);


        pb.directory(getProductDir());
        Process process = pb.start();
        String errorMsg =
            new String(IOUtil.readBytes(process.getErrorStream()));
        String outMsg =
            new String(IOUtil.readBytes(process.getInputStream()));
        int result = process.waitFor();
        if (errorMsg.length() > 0) {
            return new Result(
                "CDO-Error",
                new StringBuffer(
                    getRepository().showDialogError(
                        "An error occurred:<br>" + errorMsg)));
        }

        //The jeff is here for when I have a fake cdo.sh
        boolean jeff = true;

        if ( !jeff) {
            if (outMsg.length() > 0) {
                return new Result(
                    "CDO-Error",
                    new StringBuffer(
                        getRepository().showDialogError(
                            "An error occurred:<br>" + outMsg)));
            }

            if ( !outFile.exists()) {
                return new Result(
                    "CDO-Error",
                    new StringBuffer(
                        getRepository().showDialogError(
                            "Humm, the CDO generation failed for some reason")));
            }
        }

        if (doingPublish(request)) {
            if ( !request.defined(ARG_PUBLISH_NAME)) {
                request.put(ARG_PUBLISH_NAME, newName);
            }

            return getEntryManager().processEntryPublish(request, outFile,
                    null, entry, "generated from");
        }

        //Assuming this is some text
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

}
