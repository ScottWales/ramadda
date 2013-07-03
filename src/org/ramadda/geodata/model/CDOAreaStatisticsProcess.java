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
import org.ramadda.repository.Resource;
import org.ramadda.repository.database.Tables;
import org.ramadda.repository.type.CollectionTypeHandler;
import org.ramadda.repository.type.Column;
import org.ramadda.sql.Clause;
import org.ramadda.util.HtmlUtils;

import ucar.nc2.dt.grid.GridDataset;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import java.io.File;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * DataProcess for area statistics using CDO
 */
public class CDOAreaStatisticsProcess extends DataProcess {

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
    	super("CDO_AREA_STATS", "Area Statistics");
        this.repository = repository;
        typeHandler     = new CDOOutputHandler(repository);
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
     * @param inputs  the  data process inputs
     *
     * @return  the processed data
     *
     * @throws Exception  problem processing
     */
    public DataProcessOutput processRequest(
            Request request, List<? extends DataProcessInput> inputs)
            throws Exception {

        DataProcessInput dpi = inputs.get(0);
        Entry  oneOfThem = dpi.getEntries().get(0);
        String tail = typeHandler.getStorageManager().getFileTail(oneOfThem);
        String id        = getRepository().getGUID();
        String newName   = IOUtil.stripExtension(tail) + "_" + id + ".nc";
        tail = typeHandler.getStorageManager().getStorageFileName(tail);
        File outFile = new File(IOUtil.joinDir(dpi.getProcessDir(),
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
        Entry climEntry = null;
        if (stat.equals(CDOOutputHandler.STAT_ANOM)) {
            System.err.println("Looking for climo");
            List<Entry> climo = findClimatology(request, inputs.get(0), oneOfThem);
            if (climo == null) {
            	System.err.println("found squat");
            } else if (climo.size() > 1) { 
            	System.err.println("found too many");
            	
            } else {
            	climEntry = climo.get(0);
            	System.err.println("found climo: " + climEntry);
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
        runProcess(commands, dpi.getProcessDir(), outFile);
        
        if (climEntry != null) {
        	//TODO:  do stuff
        }
        
        Resource r = new Resource(outFile, Resource.TYPE_LOCAL_FILE);
        Entry outputEntry = new Entry();
        outputEntry.setResource(r);

        if (typeHandler.doingPublish(request)) {
            return new DataProcessOutput(outputEntry);
        }

        return new DataProcessOutput(outputEntry);
    }

    private void runProcess(List<String> commands, File processDir, File outFile) throws Exception {
        String[] results = getRepository().executeCommand(commands, null,
                                                          processDir);
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
    
    private List<Entry> findClimatology(Request request, DataProcessInput input, Entry granule) throws Exception {
    	if (!(input instanceof CollectionOperand)) return null;
    	Entry collection = ((CollectionOperand) input).getCollectionEntry();
        CollectionTypeHandler ctypeHandler =
            (CollectionTypeHandler) collection.getTypeHandler();
        List<Clause>    clauses   = new ArrayList<Clause>();
        List<Column>    columns   = ctypeHandler.getGranuleColumns();
        HashSet<String> seenTable = new HashSet<String>();
        Object[] values = granule.getValues();
        for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
            Column column = columns.get(colIdx);
            // first column is the collection ID
            int valIdx = colIdx+1;
            String dbTableName = column.getTableName();
            if ( !seenTable.contains(dbTableName)) {
                clauses.add(Clause.eq(ctypeHandler.getCollectionIdColumn(),
                                      collection.getId()));
                clauses.add(Clause.join(Tables.ENTRIES.COL_ID,
                                        dbTableName + ".id"));
                seenTable.add(dbTableName);
            }
            String v = values[valIdx].toString();
            if (column.getName().equals("ensemble")) {
            	clauses.add(Clause.eq(column.getName(), "clim"));
            } else {
                if (v.length() > 0) {
                    clauses.add(Clause.eq(column.getName(), v));
                }
            }

        }
        List[] pair = typeHandler.getEntryManager().getEntries(request, clauses,
                          ctypeHandler.getGranuleTypeHandler());

        return pair[1];
	}

	/**
     * Get the repository
     *
     * @return the repository
     */
    private Repository getRepository() {
        return repository;
    }
}
