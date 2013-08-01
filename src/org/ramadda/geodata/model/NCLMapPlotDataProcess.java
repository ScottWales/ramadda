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


import org.ramadda.data.process.DataProcess;
import org.ramadda.data.process.DataProcessInput;
import org.ramadda.data.process.DataProcessOperand;
import org.ramadda.data.process.DataProcessOutput;
import org.ramadda.geodata.cdmdata.CdmDataOutputHandler;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Resource;
import org.ramadda.repository.type.TypeHandler;
import org.ramadda.util.GeoUtils;
import org.ramadda.util.HtmlUtils;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;

import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import java.io.File;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;


/**
 * Class description
 *
 *
 * @version        $version$, Thu, Jul 18, '13
 * @author         Enter your name here...    
 */
public class NCLMapPlotDataProcess extends DataProcess {

    /** The nclOutputHandler */
    NCLOutputHandler nclOutputHandler;

    /** the repository */
    Repository repository;
    
    /** output type */
    public final static String ARG_NCL_OUTPUT = "ncl.output";

    /**
     * Create a new map process
     *
     * @param repository  the repository
     *
     * @throws Exception  badness
     */
    public NCLMapPlotDataProcess(Repository repository) throws Exception {
        this(repository, "NCLMap", "Plots");
    }

    /**
     * Create a new map process
     *
     * @param repository  the repository
     * @param id  an id for this process
     * @param label  a lable for this process
     *
     * @throws Exception  problem creating process
     */
    public NCLMapPlotDataProcess(Repository repository, String id,
                                 String label)
            throws Exception {
        super(id, label);
        this.repository  = repository;
        nclOutputHandler = new NCLOutputHandler(repository);
    }

    /**
     * Add this process to the form
     *
     * @param request  the Request
     * @param input    the process input
     * @param sb       the form
     *
     * @throws Exception  problem getting the information for the form
     */
    @Override
    public void addToForm(Request request, DataProcessInput input,
                          StringBuffer sb)
            throws Exception {
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.formEntry(Repository.msg("Plot As"), 
        		HtmlUtils.radio(ARG_NCL_OUTPUT, "comp", false) + Repository.msg("Comparison") +
        		HtmlUtils.radio(ARG_NCL_OUTPUT, "diff", true) + Repository.msg("Difference")));
        sb.append(
            HtmlUtils.formEntry(
                Repository.msg("Output Type"),
                HtmlUtils.radio(
                    NCLOutputHandler.ARG_NCL_PLOTTYPE, "png",
                    true) + Repository.msg("Map")
                          + HtmlUtils.radio(
                              NCLOutputHandler.ARG_NCL_PLOTTYPE, "kmz",
                              false) + Repository.msg("Google Earth") +
                              HtmlUtils.radio(NCLOutputHandler.ARG_NCL_PLOTTYPE, 
                            		  "timeseries", false) + Repository.msg("Time Series")));
        sb.append(HtmlUtils.formTableClose());
    }

    /**
     * Process the request
     *
     * @param request  the request
     * @param input    the DataProcessInput
     *
     * @return  the output
     *
     * @throws Exception  problems generating the output
     */
    @Override
    public DataProcessOutput processRequest(Request request,
                                            DataProcessInput input)
            throws Exception {

        List<Entry>              outputEntries = new ArrayList<Entry>();
        List<DataProcessOperand> ops           = input.getOperands();
        StringBuffer fileList = new StringBuffer();
        Entry inputEntry = null;
        boolean haveOne = false;
        for (DataProcessOperand op : ops) {

            List<Entry> opEntries  = op.getEntries();
            inputEntry = opEntries.get(0);
            for (Entry entry : opEntries) {
            	if (haveOne) fileList.append(",");
            	//fileList.append("\"");
            	fileList.append(entry.getResource().toString());
            	//fileList.append("\"");
            	haveOne = true;
            }
        }
        
        String      wksName    = repository.getGUID();
        String plotType =
            request.getString(NCLOutputHandler.ARG_NCL_PLOTTYPE, "png");
        if (plotType.equals("image")) {
            plotType = "png";
        }
        String suffix = plotType;
        if (plotType.equals("timeseries")) {
            suffix = "png";
        }
        String outputType = 
            request.getString(ARG_NCL_OUTPUT, "comp");
        File outFile = new File(IOUtil.joinDir(input.getProcessDir(),
                           wksName) + "." + suffix);
        CdmDataOutputHandler dataOutputHandler =
            nclOutputHandler.getDataOutputHandler();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().createGrid(
                inputEntry.getResource().toString());
        if (dataset == null) {
            throw new Exception("Not a grid");
        }

        StringBuffer commandString = new StringBuffer();
        List<String> commands      = new ArrayList<String>();
        String       ncargRoot     = nclOutputHandler.getNcargRootDir();
        commands.add(IOUtil.joinDir(ncargRoot, "bin/ncl"));
        commands
            .add(IOUtil
                .joinDir(IOUtil
                    .joinDir(nclOutputHandler.getStorageManager()
                        .getResourceDir(), "ncl"), nclOutputHandler
                            .SCRIPT_MAPPLOT));
        Map<String, String> envMap = new HashMap<String, String>();
        envMap.put("NCARG_ROOT", ncargRoot);
        envMap.put("wks_name", wksName);
        envMap.put("ncfile", inputEntry.getResource().toString());
        envMap.put("ncfiles", fileList.toString());
        envMap.put("productdir", input.getProcessDir().toString());
        envMap.put("plot_type", plotType);
        envMap.put("output", outputType);

        Hashtable    args     = request.getArgs();
        List<String> varNames = new ArrayList<String>();
        for (Enumeration keys = args.keys(); keys.hasMoreElements(); ) {
            String arg = (String) keys.nextElement();
            if (arg.startsWith(CdmDataOutputHandler.VAR_PREFIX)
                    && request.get(arg, false)) {
                varNames.add(
                    arg.substring(
                        CdmDataOutputHandler.VAR_PREFIX.length()));
            }
        }
        String varname =
            request.getString(nclOutputHandler.ARG_NCL_VARIABLE, null);
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
        double lonMin = Double.parseDouble(
                            request.getString(
                                NCLOutputHandler.ARG_NCL_AREA_WEST,
                                String.valueOf(llb.getLonMin())));
        double lonMax = Double.parseDouble(
                            request.getString(
                                NCLOutputHandler.ARG_NCL_AREA_EAST,
                                String.valueOf(llb.getLonMax())));
        if (origLonMin < 0) {  // -180 to 180
            lonMin = GeoUtils.normalizeLongitude(lonMin);
            lonMax = GeoUtils.normalizeLongitude(lonMax);
        } else {               // 0-360
            lonMin = GeoUtils.normalizeLongitude360(lonMin);
            lonMax = GeoUtils.normalizeLongitude360(lonMax);
        }
        envMap.put("maxLat",
                   request.getString(NCLOutputHandler.ARG_NCL_AREA_NORTH,
                                     String.valueOf(llb.getLatMax())));
        envMap.put("minLat",
                   request.getString(NCLOutputHandler.ARG_NCL_AREA_SOUTH,
                                     String.valueOf(llb.getLatMin())));
        envMap.put("minLon", String.valueOf(lonMin));
        envMap.put("maxLon", String.valueOf(lonMax));

        boolean haveOriginalBounds = true;
        for (String spatialArg : NCLOutputHandler.NCL_SPATIALARGS) {
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
        String[] results = repository.executeCommand(commands, envMap,
                               input.getProcessDir());
        String errorMsg = results[1];
        String outMsg   = results[0];
        // Check the version
        if (suffix.equals("png")) {
            Matcher m = NCLOutputHandler.versionPattern.matcher(outMsg);
            if (m.find()) {
                String version = m.group(1);
                if (version.compareTo("6.0.0") < 0) {
                    String oldPath = outFile.toString();
                    outFile = new File(oldPath.replace(".png",
                            ".000001.png"));
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
        Resource resource = new Resource(outFile,
                                         Resource.TYPE_LOCAL_FILE);
        Entry outputEntry = new Entry(new TypeHandler(repository), true);
        outputEntry.setResource(resource);
        outputEntries.add(outputEntry);
        DataProcessOutput dpo = new DataProcessOutput(outputEntries);

        return dpo;

    }
    
    /**
     * Can we handle this type of DataProcessInput?
     * 
     * @return true if we can handle
     */
    public boolean canHandle(DataProcessInput dpi) {
    	if (!nclOutputHandler.isEnabled()) return false;
    	// TODO: Check the input
    	return true;
    }

}
