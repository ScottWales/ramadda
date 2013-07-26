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

package org.ramadda.geodata.model;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ramadda.geodata.cdmdata.CdmDataOutputHandler;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Link;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Result;
import org.ramadda.repository.StorageManager;
import org.ramadda.repository.map.MapBoxProperties;
import org.ramadda.repository.map.MapInfo;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.repository.type.CollectionTypeHandler;
import org.ramadda.util.GeoUtils;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TempDir;
import org.w3c.dom.Element;

import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;


/**
 * An output handler to plot maps with NCL
 */
public class NCLOutputHandler extends OutputHandler {

    /** NCL program path */
    private static final String PROP_NCARG_ROOT = "ncl.ncarg_root";

    /** NCL program path */
    private static final String PROP_CONVERT_PATH = "ncl.convert.path";

    /** NCL map plot script */
    public static final String SCRIPT_MAPPLOT = "plot.data.ncl";

    /** NCL map plot script */
    private static final String SCRIPT_KML = "kml.ncl";
    
    private static final String[] SCRIPTS = {SCRIPT_MAPPLOT, SCRIPT_KML};

    /** NCL prefix string */
    private static final String ARG_NCL_PREFIX= "ncl.";

    /** NCL plot string */
    public static final String ARG_NCL_PLOTTYPE= ARG_NCL_PREFIX+"_plottype";

    public static final String ARG_NCL_AREA  = ARG_NCL_PREFIX +"area";
    public static final String ARG_NCL_AREA_NORTH  = ARG_NCL_AREA+"_north";
    public static final String ARG_NCL_AREA_SOUTH  = ARG_NCL_AREA+"_south";
    public static final String ARG_NCL_AREA_EAST  = ARG_NCL_AREA+"_east";
    public static final String ARG_NCL_AREA_WEST  = ARG_NCL_AREA+"_west";
    public static final String ARG_NCL_VARIABLE  = ARG_NCL_PREFIX+ARG_VARIABLE;
    
    /** spatial arguments */
    public static final String[] SPATIALARGS = new String[] { ARG_NCL_AREA_NORTH,
            ARG_NCL_AREA_WEST, ARG_NCL_AREA_SOUTH, ARG_NCL_AREA_EAST, };
    
    /** map plot output id */
    public static final OutputType OUTPUT_NCL_MAPPLOT =
        new OutputType("NCL Map Displays", "ncl.mapplot",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       "/model/ncl.gif", CdmDataOutputHandler.GROUP_DATA);

    /** the product directory */
    private TempDir productDir;

    /** the path to NCL program */
    private String ncargRoot;
    
    /** the path to convert program */
    private String convertPath;
    
    /** spatial arguments */
    public static final String[] NCL_SPATIALARGS = new String[] { ARG_NCL_AREA_NORTH,
            ARG_NCL_AREA_WEST, ARG_NCL_AREA_SOUTH, ARG_NCL_AREA_EAST, };
    
    /** NCL version regex */
    private static final String NCL_VERSION_REGEX = "NCAR Command Language Version (\\d+.\\d+.\\d+)";
    
    /** NCL version pattern */
    public static final Pattern versionPattern = Pattern.compile(NCL_VERSION_REGEX);

    /**
     * Construct a new NCLOutputHandler
     *
     * @param repository  the Repository
     *
     * @throws Exception  problem creating handler
     */
    public NCLOutputHandler(Repository repository) throws Exception {
        super(repository, "NCL");
        ncargRoot = getProperty(PROP_NCARG_ROOT, null);
        convertPath = getProperty(PROP_CONVERT_PATH, "convert");
    }

    /**
     * Construct a new NCL output handler
     *
     * @param repository  the Repository
     * @param element     the XML definition
     *
     * @throws Exception  problem creating handler
     */
    public NCLOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_NCL_MAPPLOT);
        ncargRoot = getProperty(PROP_NCARG_ROOT, null);
        convertPath = getProperty(PROP_CONVERT_PATH, "convert");
        if (ncargRoot != null) {
            // write out the templates
            for (int i = 0; i < SCRIPTS.length; i++) {
                String nclScript =
                    getStorageManager().readSystemResource(
                        "/org/ramadda/geodata/model/resources/ncl/"
                        + SCRIPTS[i]);
                //nclScript = nclScript.replaceAll("\\$NCARG_ROOT", ncargRoot);
                String outdir =
                    IOUtil.joinDir(getStorageManager().getResourceDir(), "ncl");
                nclScript = nclScript.replaceAll("\\$NCL_RESOURCES", outdir);
                nclScript = nclScript.replaceAll("%convert%", convertPath);
                StorageManager.makeDir(outdir);
                File outputFile = new File(IOUtil.joinDir(outdir,
                                      SCRIPTS[i]));
                InputStream is = new ByteArrayInputStream(nclScript.getBytes());
                OutputStream os =
                    getStorageManager().getUncheckedFileOutputStream(outputFile);
                IOUtil.writeTo(is, os);
            }
        }

    }

    /**
     * Check to see if we have NCL installed
     *
     * @return  true if path to NCL is set
     */
    public boolean isEnabled() {
        return ncargRoot != null;
    }

    public String getNcargRootDir() {
    	return ncargRoot;
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
        if ( !isEnabled()) {
            return;
        }
        if ((state.entry != null) && state.entry.isFile()
                && state.entry.getResource().getPath().endsWith(".nc")) {
            links.add(makeLink(request, state.entry, OUTPUT_NCL_MAPPLOT));
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
            TempDir tempDir = getStorageManager().makeTempDir("nclproducts");
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
            return outputNCL(request, entry);
        }
        StringBuffer sb = new StringBuffer();
        addForm(request, entry, sb);

        return new Result("NCL Form", sb);
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
                             msg("Plotting Data...."))));
                             */

        sb.append(HtmlUtils.formTable());

        sb.append(HtmlUtils.hidden(ARG_OUTPUT, OUTPUT_NCL_MAPPLOT));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        String buttons = HtmlUtils.submit("Plot Data", ARG_SUBMIT);
        //sb.append(buttons);
        sb.append(HtmlUtils.h2("Plot Dataset"));
        sb.append(HtmlUtils.hr());
        addToForm(request, entry, sb);
        sb.append(HtmlUtils.p());
        addPublishWidget(
            request, entry, sb,
            msg("Select a folder to publish the generated image file to"));
        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.p());
        sb.append(buttons);

    }

    /**
     * Add the necessary components to the form
     *
     * @param request  the Request
     * @param entry    the Entry
     * @param sb       the HTML string
     *
     * @throws Exception  problem making form
     */
    public void addToForm(Request request, Entry entry, StringBuffer sb)
            throws Exception {
        sb.append(HtmlUtils.hidden(ARG_OUTPUT, OUTPUT_NCL_MAPPLOT));
        CdmDataOutputHandler dataOutputHandler = getDataOutputHandler();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().getGridDataset(entry,
                entry.getResource().getPath());
        List<GridDatatype> grids = dataset.getGrids();
        GridDatatype       var   = grids.get(0);
        sb.append(HtmlUtils.hidden(ARG_NCL_VARIABLE, var));
        GridCoordSystem gcs = var.getCoordinateSystem();
        sb.append(HtmlUtils.formEntry(msgLabel("Variable"),
                                      var.getName() + HtmlUtils.space(1)
                                      + ((var.getUnitsString() != null)
                                         ? "(" + var.getUnitsString() + ")"
                                         : "") + HtmlUtils.space(3)
                                         + HtmlUtils.italics(
                                             var.getDescription())));

        if (gcs.getVerticalAxis() != null) {
            CoordinateAxis1D     zAxis  = gcs.getVerticalAxis();
            int                  sizeZ  = (int) zAxis.getSize();
            List<TwoFacedObject> levels =
                new ArrayList<TwoFacedObject>(sizeZ);
            for (int k = 0; k < sizeZ; k++) {
                int level = (int) zAxis.getCoordValue(k);
                levels.add(new TwoFacedObject(String.valueOf(level), level));
            }
            sb.append(
                HtmlUtils.formEntry(
                    msgLabel("Level"),
                    HtmlUtils.select(CdmDataOutputHandler.ARG_LEVEL, levels)
                    + HtmlUtils.space(1) + "(" + zAxis.getUnitsString()
                    + ")"));
        }
        LatLonRect llr = dataset.getBoundingBox();
        if (llr != null) {
            MapInfo map = getRepository().getMapManager().createMap(request,
                              true);
            map.addBox("", llr, new MapBoxProperties("blue", false, true));
            String[] points = new String[] { "" + llr.getLatMax(),
                                             "" + llr.getLonMin(),
                                             "" + llr.getLatMin(),
                                             "" + llr.getLonMax(), };

            for (int i = 0; i < points.length; i++) {
                sb.append(HtmlUtils.hidden(NCL_SPATIALARGS[i] + ".original",
                                           points[i]));
            }
            String llb = map.makeSelector(ARG_NCL_AREA, true, points);
            sb.append(HtmlUtils.formEntryTop(msgLabel("Area"), llb));
        }
        /*  TODO: Figure out how to do time series
        sb.append(
            HtmlUtils.formEntry(
                Repository.msg("Plot Type"),
                HtmlUtils.radio(
                    ARG_NCL_PLOTTYPE, "png",
                    true) + Repository.msg("Image")
                          + HtmlUtils.radio(
                              ARG_NCL_PLOTTYPE, "kmz",
                              false) + Repository.msg("Google Earth") +
                              HtmlUtils.radio(ARG_NCL_PLOTTYPE, 
                            		  "timeseries", false) + Repository.msg("Time Series")));
        */
    }

    /**
     * Output the NCL request
     *
     * @param request  the request
     * @param entry    the entry
     *
     * @return  the output
     *
     * @throws Exception  problem executing the command
     */
    public Result outputNCL(Request request, Entry entry) throws Exception {

        try {
            File input   = entry.getTypeHandler().getFileForEntry(entry);
            File outFile = processRequest(request, input);
            if (doingPublish(request)) {
                if ( !request.defined(ARG_PUBLISH_NAME)) {
                    request.put(ARG_PUBLISH_NAME, outFile.getName());
                }

                return getEntryManager().processEntryPublish(request,
                        outFile, null, entry, "generated from");
            }

            return request.returnFile(
                outFile, getStorageManager().getFileTail(outFile.toString()));
        } catch (IllegalArgumentException iae) {
            return getErrorResult(request, "NCL-Error",
                                  "An error occurred:<br>" + iae);
        }

    }

    /**
     * Process the request
     *
     * @param request  the request
     * @param input    the file to work on
     *
     * @return  the output
     *
     * @throws Exception problem generating output
     */
    public File processRequest(Request request, File input) throws Exception {

        String wksName = getRepository().getGUID();
        String plotType = request.getString(CollectionTypeHandler.ARG_REQUEST,"png");
        //String plotType = request.getString(ARG_NCL_PLOTTYPE,"png");
        if (plotType.equals("image")) {
            plotType = "png";
        }
        String suffix = plotType;
        if (plotType.equals("timeseries")) {
            suffix = "png";
        }
        File outFile = new File(IOUtil.joinDir(getProductDir(), wksName)
                                + "." + suffix);
        //String wksName = IOUtil.joinDir(getProductDir(),
        //                                getRepository().getGUID());
        //File outFile = new File(wksName+".png");
        CdmDataOutputHandler dataOutputHandler = getDataOutputHandler();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().createGrid(input.getPath());
        if (dataset == null) {
            throw new Exception("Not a grid");
        }

        StringBuffer commandString = new StringBuffer();
        List<String> commands      = new ArrayList<String>();
        commands.add(IOUtil.joinDir(ncargRoot, "bin/ncl"));
        commands.add(
            IOUtil.joinDir(
                IOUtil.joinDir(getStorageManager().getResourceDir(), "ncl"),
                SCRIPT_MAPPLOT));
        Map<String, String> envMap = new HashMap<String, String>();
        envMap.put("NCARG_ROOT", ncargRoot);
        envMap.put("wks_name", wksName);
        envMap.put("ncfiles", input.toString());
        envMap.put("productdir", getProductDir().toString());
        envMap.put("plot_type", plotType);

        Hashtable    args     = request.getArgs();
        List<String> varNames = new ArrayList<String>();
        for (Enumeration keys = args.keys(); keys.hasMoreElements(); ) {
            String arg = (String) keys.nextElement();
            if (arg.startsWith(CdmDataOutputHandler.VAR_PREFIX)
                    && request.get(arg, false)) {
                varNames.add(
                    arg.substring(CdmDataOutputHandler.VAR_PREFIX.length()));
            }
        }
        String varname = request.getString(ARG_NCL_VARIABLE, null);
        if (varname == null) {
            List<GridDatatype> grids = dataset.getGrids();
            GridDatatype       var   = grids.get(0);
            varname = var.getName();
        }
        envMap.put("variable", varname);
        String level = request.getString(CdmDataOutputHandler.ARG_LEVEL,
                                         null);
        if ((level != null) && !level.isEmpty()) {
            envMap.put(CdmDataOutputHandler.ARG_LEVEL, level);
        }
        LatLonRect llb = dataset.getBoundingBox();
        // Normalize longitude bounds to the data
        double origLonMin = llb.getLonMin();
        double lonMin = Double.parseDouble(request.getString(ARG_NCL_AREA_WEST, String.valueOf(llb.getLonMin())));
        double lonMax = Double.parseDouble(request.getString(ARG_NCL_AREA_EAST, String.valueOf(llb.getLonMax())));
        if (origLonMin < 0) { // -180 to 180
            lonMin = GeoUtils.normalizeLongitude(lonMin);
            lonMax = GeoUtils.normalizeLongitude(lonMax);
        } else {  // 0-360
            lonMin = GeoUtils.normalizeLongitude360(lonMin);
            lonMax = GeoUtils.normalizeLongitude360(lonMax);
        }
        envMap.put("maxLat", request.getString(ARG_NCL_AREA_NORTH, String.valueOf(llb.getLatMax())));
        envMap.put("minLat", request.getString(ARG_NCL_AREA_SOUTH, String.valueOf(llb.getLatMin())));
        envMap.put("minLon", String.valueOf(lonMin));
        envMap.put("maxLon", String.valueOf(lonMax));

        boolean haveOriginalBounds = true;
        for (String spatialArg : NCL_SPATIALARGS) {
            if ( !Misc.equals(request.getString(spatialArg, ""),
                              request.getString(spatialArg + ".original",
                                  ""))) {
                haveOriginalBounds = false;

                break;
            }
        }
        envMap.put("addCyclic", Boolean.toString(haveOriginalBounds));


        System.err.println("cmds:" + commands);
        System.err.println("env:" + envMap);

        //Use new repository method to execute. This gets back [stdout,stderr]
        String[] results = getRepository().executeCommand(commands, envMap,
                               getProductDir());
        String errorMsg = results[1];
        String outMsg   = results[0];
        // Check the version
        if (suffix.equals("png")) {
            Matcher m = versionPattern.matcher(outMsg);
            if (m.find()) {
                String version = m.group(1);
                if (version.compareTo("6.0.0") < 0) {
            	    String oldPath = outFile.toString();
            	    outFile = new File(oldPath.replace(".png", ".000001.png"));
                }
            }
        }

        if ( !outFile.exists()) {
            if (outMsg.length() > 0) {
                throw new IllegalArgumentException(outMsg);
            }
            if (errorMsg.length() > 0) {
                throw new IllegalArgumentException(errorMsg);
            }
            if ( !outFile.exists()) {
                throw new IllegalArgumentException(
                    "Humm, the NCL image generation failed for some reason");
            }
        }
        return outFile;
    }

}
