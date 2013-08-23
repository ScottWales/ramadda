/*
* Copyright 2008-2013 Geode Systems LLC
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
import org.ramadda.repository.database.Tables;
import org.ramadda.repository.type.CollectionTypeHandler;
import org.ramadda.repository.type.Column;
import org.ramadda.repository.type.GranuleTypeHandler;
import org.ramadda.repository.type.TypeHandler;
import org.ramadda.sql.Clause;
import org.ramadda.util.HtmlUtils;

import ucar.nc2.dt.grid.GridDataset;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.IOUtil;


import java.io.File;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * DataProcess for area statistics using CDO
 */
public class CDOArealStatisticsProcess extends DataProcess {

    /** the type handler associated with this */
    CDOOutputHandler typeHandler;

    /** the associated repository */
    Repository repository;

    /** months */
    private static final String[] MONTHS = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
        "Nov", "Dec"
    };

    /**
     * Area statistics DataProcess
     *
     * @param repository  the Repository
     *
     * @throws Exception  problems
     */
    public CDOArealStatisticsProcess(Repository repository) throws Exception {
        super("CDO_AREA_STATS", "Area Statistics");
        this.repository = repository;
        typeHandler     = new CDOOutputHandler(repository);
    }

    /**
     * Add to form
     *
     * @param request  the Request
     * @param input    the DataProcessInput
     * @param sb       the form
     *
     * @throws Exception  problem adding to the form
     */
    public void addToForm(Request request, DataProcessInput input,
                          StringBuffer sb)
            throws Exception {
        sb.append(HtmlUtils.formTable());
        makeInputForm(request, input, sb);
        sb.append(HtmlUtils.formTableClose());
    }

    /**
     * Make the input form
     *
     * @param request  the Request
     * @param input    the DataProcessInput
     * @param sb       the StringBuffer
     *
     * @throws Exception  problem making stuff
     */
    private void makeInputForm(Request request, DataProcessInput input,
                               StringBuffer sb)
            throws Exception {
        Entry first = input.getOperands().get(0).getEntries().get(0);

        CdmDataOutputHandler dataOutputHandler =
            typeHandler.getDataOutputHandler();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().getGridDataset(first,
                first.getResource().getPath());

        if (dataset != null) {
            typeHandler.addVarLevelWidget(request, sb, dataset,
                                          CdmDataOutputHandler.ARG_LEVEL);
        }

        typeHandler.addStatsWidget(request, sb);

        typeHandler.addTimeWidget(request, sb, dataset, true);

        LatLonRect llr = null;
        if (dataset != null) {
            llr = dataset.getBoundingBox();
        } else {
            llr = new LatLonRect(new LatLonPointImpl(90.0, -180.0),
                                 new LatLonPointImpl(-90.0, 180.0));
        }
        typeHandler.addMapWidget(request, sb, llr, false);
    }

    /**
     * Process the request
     *
     * @param request  The request
     * @param input  the  data process input
     *
     * @return  the processed data
     *
     * @throws Exception  problem processing
     */
    public DataProcessOutput processRequest(Request request,
                                            DataProcessInput input)
            throws Exception {

        if ( !canHandle(input)) {
            throw new Exception("Illegal data type");
        }

        List<DataProcessOperand> outputEntries =
            new ArrayList<DataProcessOperand>();
        for (DataProcessOperand op : input.getOperands()) {
            Entry oneOfThem = op.getEntries().get(0);
            Entry collection = GranuleTypeHandler.getCollectionEntry(request,
                                   oneOfThem);
            String frequency = "Monthly";
            if (collection != null) {
                frequency = collection.getValues()[0].toString();
            }
            if (frequency.toLowerCase().indexOf("mon") >= 0) {
                outputEntries.add(processMonthlyRequest(request, input, op));
            }
        }

        return new DataProcessOutput(outputEntries);
    }

    /**
     * Process the daily data request
     *
     * @param request  the request
     * @param dpi      the DataProcessInput
     *
     * @return  some output
     *
     * @throws Exception problem processing the daily data
     */
    private Entry processDailyRequest(Request request, DataProcessInput dpi)
            throws Exception {
        throw new Exception("can't handle daily data yet");
    }

    /**
     * Process the monthly request
     *
     * @param request  the request
     * @param dpi      the DataProcessInput
     * @param op       the operand
     *
     * @return  some output
     *
     * @throws Exception Problem processing the monthly request
     */
    private DataProcessOperand processMonthlyRequest(Request request,
            DataProcessInput dpi, DataProcessOperand op)
            throws Exception {

        Entry        oneOfThem = op.getEntries().get(0);
        String tail = typeHandler.getStorageManager().getFileTail(oneOfThem);
        String       id        = getRepository().getGUID();
        String       newName = IOUtil.stripExtension(tail) + "_" + id + ".nc";
        File outFile = new File(IOUtil.joinDir(dpi.getProcessDir(), newName));
        List<String> commands  = initCDOCommand();

        String       stat = request.getString(CDOOutputHandler.ARG_CDO_STAT);
        Entry        climEntry = null;
        if (stat.equals(CDOOutputHandler.STAT_ANOM)) {
            System.err.println("Looking for climo");
            List<Entry> climo = findClimatology(request, oneOfThem);
            if (climo == null) {
                System.err.println("found squat");
            } else if (climo.size() > 1) {
                System.err.println("found too many");

            } else {
                climEntry = climo.get(0);
                System.err.println("found climo: " + climEntry);
            }
        }

        // Select order (left to right) - operations go right to left:
        //   - stats
        //   - level
        //   - region
        //   - month range
        //   - year or time range
        typeHandler.addStatCommands(request, oneOfThem, commands);
        typeHandler.addLevelSelectCommands(request, oneOfThem, commands,
                                           CdmDataOutputHandler.ARG_LEVEL);
        typeHandler.addAreaSelectCommands(request, oneOfThem, commands);
        typeHandler.addDateSelectCommands(request, oneOfThem, commands);

        //System.err.println("cmds:" + commands);

        commands.add(oneOfThem.getResource().getPath());
        commands.add(outFile.toString());
        runProcess(commands, dpi.getProcessDir(), outFile);

        if (climEntry != null) {
            //TODO:  do stuff
            String climName = IOUtil.stripExtension(tail) + "_" + id
                              + "_clim.nc";
            File climFile = new File(IOUtil.joinDir(dpi.getProcessDir(),
                                climName));
            commands = initCDOCommand();

            // Select order (left to right) - operations go right to left:
            //   - level
            //   - region
            //   - month range
            typeHandler.addStatCommands(request, climEntry, commands);
            typeHandler.addLevelSelectCommands(request, climEntry, commands,
                    CdmDataOutputHandler.ARG_LEVEL);
            typeHandler.addAreaSelectCommands(request, climEntry, commands);
            typeHandler.addMonthSelectCommands(request, climEntry, commands);

            //System.err.println("clim cmds:" + commands);

            commands.add(climEntry.getResource().getPath());
            commands.add(climFile.toString());
            runProcess(commands, dpi.getProcessDir(), climFile);

            // now subtract them
            String anomName = IOUtil.stripExtension(tail) + "_" + id
                              + "_anom.nc";
            File anomFile = new File(IOUtil.joinDir(dpi.getProcessDir(),
                                anomName));
            commands = initCDOCommand();
            commands.add("-ymonsub");
            commands.add(outFile.toString());
            commands.add(climFile.toString());
            commands.add(anomFile.toString());
            runProcess(commands, dpi.getProcessDir(), anomFile);
            outFile = anomFile;
        }

        StringBuffer outputName = new StringBuffer();
        Object[]     values     = oneOfThem.getValues();
        // values = collection,model,experiment,ens,var
        // model
        outputName.append(values[1].toString().toUpperCase());
        outputName.append(" ");
        // experiment
        outputName.append(values[2]);
        outputName.append(" ");
        // ens
        String ens = values[3].toString();
        if (ens.equals("mean") || ens.equals("sprd") || ens.equals("clim")) {
            outputName.append("ens");
        }
        outputName.append(ens);
        outputName.append(" ");
        // var
        outputName.append(values[4]);
        outputName.append(" ");
        outputName.append(stat);
        outputName.append(" ");

        int startMonth = request.defined(CDOOutputHandler.ARG_CDO_STARTMONTH)
                         ? request.get(CDOOutputHandler.ARG_CDO_STARTMONTH, 1)
                         : 1;
        int endMonth = request.defined(CDOOutputHandler.ARG_CDO_ENDMONTH)
                       ? request.get(CDOOutputHandler.ARG_CDO_ENDMONTH,
                                     startMonth)
                       : startMonth;
        if (startMonth == endMonth) {
            outputName.append(MONTHS[startMonth - 1]);
        } else {
            outputName.append(MONTHS[startMonth - 1]);
            outputName.append("-");
            outputName.append(MONTHS[endMonth - 1]);
        }
        outputName.append(" ");
        int startYear = request.defined(CDOOutputHandler.ARG_CDO_STARTYEAR)
                        ? request.get(CDOOutputHandler.ARG_CDO_STARTYEAR, 1)
                        : 1979;
        int endYear   = request.defined(CDOOutputHandler.ARG_CDO_ENDYEAR)
                        ? request.get(CDOOutputHandler.ARG_CDO_ENDYEAR,
                                      startMonth)
                        : startMonth;
        if (startMonth == endMonth) {
            outputName.append(startYear);
        } else {
            outputName.append(startYear);
            outputName.append("-");
            outputName.append(endYear);
        }
        //System.out.println("Name: " + outputName.toString());

        Resource resource    = new Resource(outFile,
                                            Resource.TYPE_LOCAL_FILE);
        Entry    outputEntry = new Entry(new TypeHandler(repository), true);
        outputEntry.setResource(resource);

        //return new DataProcessOperand(outputEntry.getName(), outputEntry);
        return new DataProcessOperand(outputName.toString(), outputEntry);
    }

    /**
     * Can we handle this input
     *
     * @param input  the input
     *
     * @return true if we can, otherwise false
     */
    public boolean canHandle(DataProcessInput input) {
        if ( !typeHandler.isEnabled()) {
            return false;
        }
        for (DataProcessOperand op : input.getOperands()) {
            List<Entry> entries = op.getEntries();
            // TODO: change this when we can handle more than one entry (e.g. daily data)
            if (entries.isEmpty() || (entries.size() > 1)) {
                return false;
            }
            Entry firstEntry = entries.get(0);
            if ( !(firstEntry.getTypeHandler()
                    instanceof ClimateModelFileTypeHandler)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Initialize the CDO command list
     *
     * @return  the initial list of CDO commands
     */
    private List<String> initCDOCommand() {
        List<String> newCommands = new ArrayList<String>();
        newCommands.add(typeHandler.getCDOPath());
        newCommands.add("-L");
        newCommands.add("-s");
        newCommands.add("-O");

        return newCommands;
    }

    /**
     * Run the process
     *
     * @param commands  the list of commands to run
     * @param processDir  the processing directory
     * @param outFile     the outfile
     *
     * @throws Exception problem running commands
     */
    private void runProcess(List<String> commands, File processDir,
                            File outFile)
            throws Exception {
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

    /**
     * Find the associated climatology for the input
     *
     * @param request  the Request
     * @param granule  the entry
     *
     * @return the climatology entry or null
     *
     * @throws Exception  problems
     */
    private List<Entry> findClimatology(Request request, Entry granule)
            throws Exception {
        if ( !(granule.getTypeHandler()
                instanceof ClimateModelFileTypeHandler)) {
            return null;
        }
        Entry collection = GranuleTypeHandler.getCollectionEntry(request,
                               granule);
        CollectionTypeHandler ctypeHandler =
            (CollectionTypeHandler) collection.getTypeHandler();
        List<Clause>    clauses   = new ArrayList<Clause>();
        List<Column>    columns   = ctypeHandler.getGranuleColumns();
        HashSet<String> seenTable = new HashSet<String>();
        Object[]        values    = granule.getValues();
        for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
            Column column = columns.get(colIdx);
            // first column is the collection ID
            int    valIdx      = colIdx + 1;
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
        List[] pair = typeHandler.getEntryManager().getEntries(request,
                          clauses, ctypeHandler.getGranuleTypeHandler());

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
