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


import org.ramadda.data.process.CollectionOperand;
import org.ramadda.data.process.DataProcess;
import org.ramadda.data.process.DataProcessInput;
import org.ramadda.data.process.DataProcessOutput;
import org.ramadda.geodata.cdmdata.CDOOutputHandler;
import org.ramadda.geodata.cdmdata.CdmDataOutputHandler;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.sql.Clause;
import org.ramadda.util.HtmlUtils;

import ucar.nc2.dt.grid.GridDataset;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import java.io.File;

import java.util.ArrayList;
import java.util.List;


/**
 * DataProcess for area statistics using CDO
 */
public class CDOAreaStatisticsProcess implements DataProcess {

    /** the type handler associated with this */
    CDOOutputHandler typeHandler;

    /** the associated repository */
    Repository repository;

    /**
     * Area statistics DataProcess
     *
     * @param repository  the Repository
     *
     * @throws Exception  problems
     */
    public CDOAreaStatisticsProcess(Repository repository) throws Exception {
        this.repository = repository;
        typeHandler     = new CDOOutputHandler(repository);
    }

    /**
     * Get the DataProcess id
     *
     * @return  the ID
     */
    public String getDataProcessId() {
        return "CDO_AREA_STATS";
    }

    /**
     * Add to form
     *
     * @param request  the Request
     * @param inputs    the DataProcessInput
     * @param sb       the form
     *
     * @throws Exception  problem adding to the form
     */
    public void addToForm(Request request,
                          List<? extends DataProcessInput> inputs,
                          StringBuffer sb)
            throws Exception {
        sb.append(HtmlUtils.formTable());
        Entry first = inputs.get(0).getEntries().get(0);
        if (first.getType().equals("noaa_climate_modelfile")) {
            //values[1] = var;
            //values[2] = model;
            //values[3] = experiment;
            //values[4] = member;
            //values[5] = frequency;
            Object[]     values = first.getValues();
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
        CdmDataOutputHandler dataOutputHandler =
            typeHandler.getDataOutputHandler();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().getGridDataset(first,
                first.getResource().getPath());

        if (dataset != null) {
            typeHandler.addVarLevelWidget(request, sb, dataset);
        }

        typeHandler.addStatsWidget(request, sb);

        //if(dataset != null)  {
        typeHandler.addTimeWidget(request, sb, dataset, true);
        //}

        LatLonRect llr = null;
        if (dataset != null) {
            llr = dataset.getBoundingBox();
        } else {
            llr = new LatLonRect(new LatLonPointImpl(90.0, -180.0),
                                 new LatLonPointImpl(-90.0, 180.0));
        }
        typeHandler.addMapWidget(request, sb, llr);
        sb.append(HtmlUtils.formTableClose());
    }

    /**
     * Process the request
     *
     * @param request  The request
     * @param input   the input
     *
     * @return  the processed data
     *
     * @throws Exception  problem processing
     */
    public DataProcessOutput processRequest(Request request,
                                            DataProcessInput input)
            throws Exception {
        return processRequest(request,
                              (List<DataProcessInput>) Misc.newList(input));
    }

    /**
     * Process the request
     *
     * @param request  The request
     * @param inputs  the  data process inputs
     *
     * @return  the processed data
     *
     * @throws Exception  problem processing
     */
    public DataProcessOutput processRequest(
            Request request, List<? extends DataProcessInput> inputs)
            throws Exception {

        Entry  oneOfThem = inputs.get(0).getEntries().get(0);
        String tail = typeHandler.getStorageManager().getFileTail(oneOfThem);
        String id        = getRepository().getGUID();
        String newName   = IOUtil.stripExtension(tail) + "_" + id + ".nc";
        tail = typeHandler.getStorageManager().getStorageFileName(tail);
        File outFile = new File(IOUtil.joinDir(typeHandler.getProductDir(),
                           newName));
        List<String> commands = new ArrayList<String>();
        commands.add(typeHandler.getCDOPath());
        commands.add("-L");
        commands.add("-s");
        commands.add("-O");
        String operation =
            request.getString(CDOOutputHandler.ARG_CDO_OPERATION,
                              typeHandler.OP_INFO);
        //commands.add(operation);

        // Select order (left to right) - operations go right to left:
        //   - stats
        //   - level
        //   - region
        //   - month range
        //   - year or time range
        String stat   = request.getString(CDOOutputHandler.ARG_CDO_STAT);
        boolean wantClimo = false;
        if (stat.equals(CDOOutputHandler.STAT_ANOM)) {
            System.err.println("Looked for climo");
            Entry climo = findClimatology(inputs.get(0), oneOfThem);
            if (climo == null) { 
            	System.err.println("found squat");
            } else {
            	wantClimo = true;
            }
        }

        List<String> statCommands = typeHandler.createStatCommands(request,
                                        oneOfThem);
        for (String cmd : statCommands) {
            if ((cmd != null) && !cmd.isEmpty()) {
                commands.add(cmd);
            }
        }

        String levSelect = typeHandler.createLevelSelectCommand(request,
                               oneOfThem);
        if ((levSelect != null) && !levSelect.isEmpty()) {
            commands.add(levSelect);
        }
        String areaSelect = typeHandler.createAreaSelectCommand(request,
                                oneOfThem);
        if ((areaSelect != null) && !areaSelect.isEmpty()) {
            commands.add(areaSelect);
        }

        List<String> dateCmds = typeHandler.createDateSelectCommands(request,
                                    oneOfThem);
        for (String cmd : dateCmds) {
            if ((cmd != null) && !cmd.isEmpty()) {
                commands.add(cmd);
            }
        }

        System.err.println("cmds:" + commands);

        commands.add(oneOfThem.getResource().getPath());
        commands.add(outFile.toString());
        runProcess(commands, outFile);
        
        if (wantClimo) {
        	//TODO:  do stuff
        }

        if (typeHandler.doingPublish(request)) {
            return new DataProcessOutput(outFile);
        }

        return new DataProcessOutput(outFile);
    }

    private void runProcess(List<String> commands, File outFile) throws Exception {
        String[] results = getRepository().executeCommand(commands, null,
                               typeHandler.getProductDir());
        String errorMsg = results[1];
        String outMsg   = results[0];
        if ( !outFile.exists()) {
            if (outMsg.length() > 0) {
                throw new IllegalArgumentException(outMsg);
            }
            if (errorMsg.length() > 0) {
                throw new IllegalArgumentException(errorMsg);
            }
            if ( !outFile.exists()) {
                throw new IllegalArgumentException(
                    "Humm, the CDO processing failed for some reason");
            }
        }
    }
    
    private Entry findClimatology(DataProcessInput input, Entry oneOfThem) {
    	if (!(input instanceof CollectionOperand)) return null;
    	List<Clause> clauses = new ArrayList<Clause>();
		return null;
	}

	/**
     * Get the repository
     *
     * @return the repository
     */
    private Repository getRepository() {
        return repository;
    }

    /**
     * Get the label for this process
     *
     * @return the label
     */
    public String getDataProcessLabel() {
        return "Area Statistics";
    }

}
