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
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.RepositoryManager;
import org.ramadda.repository.Request;
import org.ramadda.repository.RequestHandler;
import org.ramadda.repository.Result;
import org.ramadda.repository.database.Tables;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.type.CollectionTypeHandler;
import org.ramadda.repository.type.Column;
import org.ramadda.sql.Clause;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.JQuery;
import org.ramadda.util.Json;
import org.ramadda.util.TTLCache;
import org.ramadda.util.Utils;

import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;


import java.io.File;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 * Provides a top-level API
 *
 */
public class ClimateModelApiHandler extends RepositoryManager implements RequestHandler {

    /** search action */
    public static final String ARG_ACTION_SEARCH = "action.search";

    /** compare action */
    public static final String ARG_ACTION_COMPARE = "action.compare";

    /** fixed collection id */
    public static final String ARG_COLLECTION = "collection";

    /** collection 1 id */
    public static final String ARG_COLLECTION1 = "collection1";

    /** collection 2 id */
    public static final String ARG_COLLECTION2 = "collection2";


    /** shortcut to JQuery class */
    private static final JQuery JQ = null;

    /** the collection type */
    private String collectionType;

    /** ttl cache */
    private TTLCache<Object, Object> cache = new TTLCache<Object,
                                                 Object>(60 * 60 * 1000);

    /**
     * ctor
     *
     * @param repository the repository
     * @param node xml from api.xml
     * @param props properties
     *
     * @throws Exception on badness
     */

    public ClimateModelApiHandler(Repository repository, Element node,
                                  Hashtable props)
            throws Exception {
        super(repository);
        collectionType = Misc.getProperty(props, "collectiontype",
                                          "climate_collection");
    }


    /**
     * Get the data processes for this request
     *
     * @param request  the Request
     *
     * @return  the list of data processes
     *
     * @throws Exception problem generating list
     */
    private List<DataProcess> getDataProcesses(Request request)
            throws Exception {
        //return getTypeHandler().getDataProcessesToRun(request);
        List<DataProcess> processes = new ArrayList<DataProcess>();
        processes.add(new CDOArealStatisticsProcess(repository));
        processes.add(new NCLModelPlotDataProcess(repository));

        return processes;
    }


    /**
     * Do the compare
     *
     * @param request  the Request
     * @param dpi   the input
     *
     * @return  a Result
     *
     * @throws Exception  problems processing the input
     */
    public Result doCompare(Request request, DataProcessInput dpi)
            throws Exception {

        //This finds the selected processes
        List<DataProcess> processesToRun = getDataProcesses(request);

        //This is the dir under <home>/process
        File processDir = null;
        processDir = dpi.getProcessDir();
        if (processDir == null) {
            processDir = getStorageManager().createProcessDir();
        }
        /*
        String template =
            getStorageManager().readSystemResource(
                "/org/ramadda/geodata/model/resources/template.xml");
        template = template.replace("${name}",
                                    "Climate model comparison output");
        StringBuffer dpiDesc = new StringBuffer();
        if (dpi.getOperands().size() > 1) {
                dpiDesc.append("Comparison of ");
        } else {
                dpiDesc.append("Analysis of ");
        }
        int          cntr    = 0;
        for (DataProcessOperand dpo : dpi.getOperands()) {
            if (cntr > 0) {
                dpiDesc.append(" and ");
            }
            dpiDesc.append(dpo.getDescription());
            cntr++;
        }
        template = template.replace("${description}", dpiDesc.toString());
        IOUtil.writeFile(new File(IOUtil.joinDir(processDir,
                ".this.ramadda.xml")), template);
        */



        List<DataProcessOutput> outputs   =
            new ArrayList<DataProcessOutput>();
        DataProcessInput        nextInput = dpi;
        for (DataProcess process : processesToRun) {
            System.err.println("MODEL: applying process: "
                               + process.getDataProcessLabel());
            DataProcessOutput output = process.processRequest(request,
                                           nextInput);
            outputs.add(output);

            //make a new input for the next process
            nextInput = dpi.makeInput(output);

            //Are we done? This should probably be a check to see if the output has a Result
            //if (output.hasOutput()) {
            //    break;
            //}
        }

        boolean    anyKMZ    = false;
        List<File> files     = new ArrayList<File>();
        File       lastFile  = null;
        Entry      lastEntry = null;
        for (DataProcessOutput dpo : outputs) {
            for (DataProcessOperand op : dpo.getOperands()) {
                for (Entry granule : op.getEntries()) {
                    if (granule.isFile()) {
                        lastFile = granule.getFile();
                        if (IOUtil.hasSuffix(lastFile.toString(), "kmz")) {
                            anyKMZ = true;
                        }
                        files.add(lastFile);
                    }
                    lastEntry = granule;
                }
            }
        }

        String template =
            getStorageManager().readSystemResource(
                "/org/ramadda/geodata/model/resources/template.xml");
        template = template.replace("${name}",
                                    "Climate model comparison output");
        StringBuffer dpiDesc = new StringBuffer();
        if (dpi.getOperands().size() > 1) {
            dpiDesc.append("Comparison of ");
        } else {
            dpiDesc.append("Analysis of ");
        }
        int cntr = 0;
        for (DataProcessOperand dpo : dpi.getOperands()) {
            if (cntr > 0) {
                dpiDesc.append(" and ");
            }
            dpiDesc.append(dpo.getDescription());
            cntr++;
        }
        template = template.replace("${description}", dpiDesc.toString());
        if (anyKMZ) {
            template = template.replace(
                "${output}",
                "{{earth width=\"500\" name=\"*.kmz\" listentries=\"true\"}}");
        } else {
            template = template.replace(
                "${output}",
                "{{gallery prefix=\"'''Images'''\" message=\"\" width=\"500\"}}");
        }
        IOUtil.writeFile(new File(IOUtil.joinDir(processDir,
                ".this.ramadda.xml")), template);



        //If no processing was done then return the raw files
        if (processesToRun.size() == 0) {
            ClimateCollectionTypeHandler typeHandler = getTypeHandler();

            return typeHandler.zipFiles(request, "results.zip", files);
        }

        //Now we get the process entry id
        String processId = processDir.getName();
        String processEntryId =
            getStorageManager().getProcessDirEntryId(processId);

        String entryUrl =
            HtmlUtils.url(
                request.getAbsoluteUrl(getRepository().URL_ENTRY_SHOW),
                ARG_ENTRYID,
        // Use this if you want to return the process directory
        processEntryId);
        //processEntryId + "/" + IOUtil.getFileTail(lastFile.toString()));

        return new Result(entryUrl);
    }



    /**
     * handle the request
     *
     * @param request request
     *
     * @return result
     *
     * @throws Exception on badness
     */
    public Result processCompareRequest(Request request) throws Exception {

        String fixedCollectionId = request.getString(ARG_COLLECTION,
                                       (String) null);
        Entry fixedCollection = null;

        if (fixedCollectionId != null) {
            //            System.err.println ("Have fixed collection:" + fixedCollectionId);
        }

        if (fixedCollectionId != null) {
            request.put(ARG_COLLECTION1, fixedCollectionId);
            request.put(ARG_COLLECTION2, fixedCollectionId);
        }


        String json = request.getString("json", (String) null);
        if (json != null) {
            return processJsonRequest(request, json);
        }

        Hashtable<String, StringBuffer> extra = new Hashtable<String,
                                                    StringBuffer>();
        List<DataProcessOperand> operands =
            new ArrayList<DataProcessOperand>();

        File processDir = getStorageManager().createProcessDir();

        //If we are searching or comparing then find the selected entries
        if (request.exists(ARG_ACTION_SEARCH)
                || request.exists(ARG_ACTION_COMPARE)) {
            int collectionCnt = 0;
            for (String collection : new String[] { ARG_COLLECTION1,
                    ARG_COLLECTION2 }) {

                collectionCnt++;
                StringBuffer tmp = new StringBuffer();
                extra.put(collection, tmp);
                String selectArg = getCollectionSelectArg(collection);
                Entry collectionEntry = getEntryManager().getEntry(request,
                                            request.getString(selectArg, ""));
                //                System.err.println("collectionEntry:" + collectionEntry+" select: " +
                //                                    request.getString(selectArg,""));

                if (collectionEntry == null) {
                    tmp.append("No collection");

                    continue;
                }
                List<Entry> entries = findEntries(request, collection,
                                          collectionEntry, collectionCnt);
                if (entries.isEmpty()) {
                    continue;
                }
                //TODO: fix this later 
                operands.add(new DataProcessOperand(entries.get(0).getName(),
                        entries));

                tmp.append(
                    "<div style=\" margin-bottom:2px;  margin-top:2px; max-height: 150px; overflow-y: auto\">");
                if ( !request.defined(ARG_COLLECTION)) {
                    tmp.append(getEntryManager().getEntryLink(request,
                            collectionEntry));
                }
                tmp.append("<ul>");
                for (Entry granule : entries) {
                    tmp.append("<li>");
                    tmp.append(getEntryManager().getEntryLink(request,
                            granule));
                }
                tmp.append("</ul>");
                tmp.append("</div>");
            }
        }


        //Check to see if we at least 1 operand 
        boolean hasOperands = false;
        if (operands.size() >= 1) {
            hasOperands = (operands.get(0).getEntries().size() > 0)
                          || (operands.get(1).getEntries().size() > 0);
        }

        if ((fixedCollectionId != null) && (fixedCollection == null)) {
            fixedCollection = getEntryManager().getEntry(request,
                    fixedCollectionId);
            //            System.err.println("got it:" + fixedCollection);
        }



        StringBuffer     sb  = new StringBuffer();
        DataProcessInput dpi = new DataProcessInput(processDir, operands);

        if (request.exists(ARG_ACTION_COMPARE)) {
            if (hasOperands) {
                try {
                    return doCompare(request, dpi);
                } catch (Exception exc) {
                    sb.append(
                        getPageHandler().showDialogError(
                            "An error occurred:<br>" + exc.getMessage()));
                }
            } else {
                sb.append(
                    getPageHandler().showDialogWarning("No fields selected"));
            }
        }



        ClimateCollectionTypeHandler typeHandler = getTypeHandler();
        List<Entry>                  collections = getCollections(request);
        if (collections.size() == 0) {
            return new Result(
                "Climate Model Comparison",
                new StringBuffer(
                    getPageHandler().showDialogWarning(
                        msg("No climate collections found"))));
        }

        String formId = "selectform" + HtmlUtils.blockCnt++;
        sb.append(HtmlUtils.comment("collection form"));

        sb.append(HtmlUtils.importJS(fileUrl("/model/compare.js")));

        sb.append(HtmlUtils.form(getCompareUrlPath(),
                                 makeFormSubmitDialog(sb,
                                     msg("Making Plot..."))));

        List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
        tfos.add(new TwoFacedObject("Select Climate Collection", ""));
        for (Entry collection : collections) {
            tfos.add(new TwoFacedObject(collection.getLabel(),
                                        collection.getId()));
        }

        StringBuffer js =
            new StringBuffer("\n//collection form initialization\n");
        js.append("var " + formId + " = new "
                  + HtmlUtils.call("CollectionForm",
                                   HtmlUtils.squote(formId)));
        sb.append(HtmlUtils.h2("Climate Model Comparison"));
        sb.append(
            "Plot monthly maps from different climate model datasets as well as differences between datasets.");

        if (fixedCollection != null) {
            sb.append(HtmlUtils.p());
            sb.append(HtmlUtils.h2(msg("Collection: "
                                       + fixedCollection.getName())));
            sb.append(HtmlUtils.hidden(ARG_COLLECTION,
                                       fixedCollection.getId()));
        }

        sb.append(
            "<table width=\"810px\"><tr valign=\"top\" align=\"left\">\n");
        sb.append(HtmlUtils.open("td", "width=\"400px\"") + "\n");
        sb.append(header(msg("Select Data")));

        int          collectionNumber = 0;



        List<String> datasets         = new ArrayList<String>(2);
        List<String> datasetTitles    = new ArrayList<String>(2);
        for (String collection : new String[] { ARG_COLLECTION1,
                ARG_COLLECTION2 }) {
            StringBuffer dsb = new StringBuffer();


            //dsb.append("<table><tr valign=top>\n");
            //dsb.append(HtmlUtils.open("td"));
            dsb.append(HtmlUtils.formTable());
            if (collectionNumber == 0) {
                datasetTitles.add("Dataset 1");
                //sb.append("<tr><td colspan=\"2\">Dataset 1</td></tr>\n");
            } else {
                datasetTitles.add("Dataset 2 (Optional)");
                //sb.append("<tr><td colspan=\"2\">Dataset 2 (Optional)</td></tr>\n");
            }
            collectionNumber++;
            String arg = getCollectionSelectArg(collection);
            String id  = getCollectionSelectId(formId, collection);



            if (fixedCollection != null) {
                dsb.append("\n");
                dsb.append(HtmlUtils.hidden(arg, fixedCollection.getId(),
                                            HtmlUtils.id(id)));
                dsb.append(HtmlUtils.comment("test test"));
            } else {
                String collectionWidget = HtmlUtils.select(arg, tfos,
                                              request.getString(arg, ""),
                                              HtmlUtils.id(id));
                dsb.append(HtmlUtils.formEntry(msgLabel("Collection"),
                        collectionWidget));
            }

            Entry        entry   = collections.get(0);
            List<Column> columns = typeHandler.getGranuleColumns();
            for (int fieldIdx = 0; fieldIdx < columns.size(); fieldIdx++) {
                Column column = columns.get(fieldIdx);
                //String key = "values::" + entry.getId()+"::" +column.getName();
                List values = new ArrayList();
                values.add(new TwoFacedObject("--", ""));
                arg = getFieldSelectArg(collection, fieldIdx);
                String selectedValue = request.getString(arg, "");
                if (Utils.stringDefined(selectedValue)) {
                    values.add(selectedValue);
                }
                dsb.append("\n");
                // don't show variable selector for subsequent collections when we have a fixed
                // collection
                if (request.defined(ARG_COLLECTION) && (collectionNumber > 1)
                        && column.getName().equals("variable")) {
                    dsb.append(HtmlUtils.formEntry(msg(""),
                            "<span style=\"max-width:250px;min-width:250px;\">&nbsp;</span>"));
                } else {

                    String selectBox =
                        HtmlUtils.select(
                            arg, values, selectedValue,
                            " style=\"max-width:250px;min-width:250px;\" "
                            + HtmlUtils.attr(
                                "id",
                                getFieldSelectId(
                                    formId, collection, fieldIdx)));
                    dsb.append(
                        HtmlUtils.formEntry(
                            msgLabel(column.getLabel()), selectBox));
                    dsb.append("\n");
                }
            }
            dsb.append(HtmlUtils.formTableClose());

            StringBuffer results = extra.get(collection);
            if (results != null) {
                //dsb.append(HtmlUtils.formEntry("", results.toString()));
                dsb.append(results.toString());
            }


            datasets.add(dsb.toString());
        }

        sb.append(OutputHandler.makeTabs(datasetTitles, datasets, true,
                                         true));
        //sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.p());




        if ( !hasOperands) {
            sb.append(HtmlUtils.submit("Select Data", ARG_ACTION_SEARCH,
                                       HtmlUtils.id(formId + ".submit")));
            sb.append("</td>\n");
            // add an empty cell to keep the other in line
            sb.append("<td width=\"400px\">&nbsp;</td>");
        } else {
            sb.append(HtmlUtils.submit("Select Again", ARG_ACTION_SEARCH,
                                       HtmlUtils.id(formId + ".submit")));
            sb.append("</td>\n");
            sb.append("<td width=\"400px\">\n");
            List<String> processTabs   = new ArrayList<String>();
            List<String> processTitles = new ArrayList<String>();

            boolean      first         = true;
            //List<DataProcess> processes     = typeHandler.getDataProcesses();
            List<DataProcess> processes = getDataProcesses(request);
            for (DataProcess process : processes) {
                StringBuffer tmpSB = new StringBuffer();
                if (processes.size() > 1) {
                    /*
                tmpSB.append(
                    HtmlUtils.radio(
                        ClimateCollectionTypeHandler.ARG_DATA_PROCESS_ID,
                        process.getDataProcessId(), first));
                tmpSB.append(HtmlUtils.space(1));
                tmpSB.append(msg("Select"));
                tmpSB.append(HtmlUtils.br());
                */
                } else {
                    tmpSB.append(
                        HtmlUtils.hidden(
                            ClimateCollectionTypeHandler.ARG_DATA_PROCESS_ID,
                            process.getDataProcessId()));
                }
                if (process.canHandle(dpi)) {
                    process.addToForm(request, dpi, tmpSB);
                    //processTabs.add(HtmlUtils.div(tmpSB.toString(),
                    //        HtmlUtils.style("min-height:200px;")));
                    processTabs.add(tmpSB.toString());
                    processTitles.add(process.getDataProcessLabel());
                    first = false;
                }
            }


            /*
            if (processTitles.size() == 1) {
                sb.append(header(msg(processTitles.get(0))));
                sb.append(processTabs.get(0));
            } else {
                sb.append(header(msg("Process Data")));
                StringBuffer processTable = new StringBuffer();
                processTable.append("<table><tr><td>");
                HtmlUtils.makeAccordian(processTable, processTitles,
                                        processTabs);
                processTable.append("</td>");
                processTable.append("<td>");
                processTable.append(HtmlUtils.div("",
                        HtmlUtils.cssClass("entryoutput")
                        + HtmlUtils.id(formId + "_output_image")));

                processTable.append("</td></tr></table>");
                sb.append(processTable);
            }
            */
            sb.append(header(msg("Process Data")));
            for (int i = 0; i < processTitles.size(); i++) {
                //sb.append("<div class=\"shadow-box\">\n");
                sb.append(
                    "<div style=\"border:1px #ccc solid;margin: 0 0 .5em 0;\">");
                //sb.append("<div style=\"border: none; padding: 0.2em; "+
                //                "background: #CCFFFF; font-weight: bold; margin: 1em 0.5em 0 0;\">\n");
                sb.append(
                    "<div style=\"border: none; padding: 0.3em; "
                    + "background: #88FFFF; font-weight: bold; margin: 0 0 0 0;\">\n");
                sb.append(processTitles.get(i));
                sb.append("</div>\n");
                sb.append("<div style=\"padding:0.2em;\">\n");
                sb.append(processTabs.get(i));
                sb.append("</div>\n");
                sb.append("</div> <!-- shadow-box -->");
            }
            sb.append(HtmlUtils.p());
            sb.append(HtmlUtils.submit("Make Plot", ARG_ACTION_COMPARE,
                                       HtmlUtils.id(formId + ".submit")));
            sb.append("</td>");
        }
        sb.append("\n</tr></table>");


        sb.append("\n");
        sb.append(HtmlUtils.script(js.toString()));
        sb.append("\n");

        sb.append(HtmlUtils.formClose());





        return new Result("Climate Model Comparison", sb);


    }

    /**
     * Find the entries
     *
     * @param request   the Request
     * @param collection  the collection
     * @param entry  the entry
     * @param collectionCnt  the collection count
     *
     * @return the list of entries (may be empty)
     *
     * @throws Exception  problem with search
     */
    private List<Entry> findEntries(Request request, String collection,
                                    Entry entry, int collectionCnt)
            throws Exception {
        CollectionTypeHandler typeHandler =
            (CollectionTypeHandler) entry.getTypeHandler();
        List<Clause>    clauses       = new ArrayList<Clause>();
        List<Column>    columns       = typeHandler.getGranuleColumns();
        HashSet<String> seenTable     = new HashSet<String>();
        boolean         haveAllFields = true;
        for (int fieldIdx = 0; fieldIdx < columns.size(); fieldIdx++) {
            Column column      = columns.get(fieldIdx);
            String dbTableName = column.getTableName();
            if ( !seenTable.contains(dbTableName)) {
                clauses.add(Clause.eq(typeHandler.getCollectionIdColumn(),
                                      entry.getId()));
                clauses.add(Clause.join(Tables.ENTRIES.COL_ID,
                                        dbTableName + ".id"));
                seenTable.add(dbTableName);
            }
            String arg = "";
            if (request.defined(ARG_COLLECTION)
                    && collection.equals(ARG_COLLECTION2)
                    && column.getName().equals("variable")) {
                arg = getFieldSelectArg(ARG_COLLECTION1, fieldIdx);
            } else {
                arg = getFieldSelectArg(collection, fieldIdx);
            }
            String v = request.getString(arg, "");
            if (v.length() > 0) {
                clauses.add(Clause.eq(column.getName(), v));
            } else {
                haveAllFields = false;
            }
        }
        //If we have a fixed collection then don't do the search if no fields were selected
        if ( !haveAllFields && request.defined(ARG_COLLECTION)) {
            return new ArrayList<Entry>();
        }


        List[] pair = getEntryManager().getEntries(request, clauses,
                          typeHandler.getGranuleTypeHandler());

        return pair[1];
    }

    /**
     * Get the field select argument
     *
     * @param collection  the collection
     * @param fieldIdx  the field index
     *
     * @return  the argument
     */
    private String getFieldSelectArg(String collection, int fieldIdx) {
        return collection + "_field" + fieldIdx;

    }

    /**
     * Get the collection select argument
     *
     * @param collection  the collection
     *
     * @return  the collection
     */
    private String getCollectionSelectArg(String collection) {
        return collection;
    }


    /**
     * Get the field select id
     *
     * @param formId   the form id
     * @param collection  the collection
     * @param fieldIdx  the field index
     *
     * @return  the field id
     */
    private String getFieldSelectId(String formId, String collection,
                                    int fieldIdx) {
        return getCollectionSelectId(formId, collection) + "_field"
               + fieldIdx;
    }

    /**
     * Get the collection select id
     *
     * @param formId   the form id
     * @param collection  the collection
     *
     * @return  the collection select id
     */
    private String getCollectionSelectId(String formId, String collection) {
        return formId + "_" + collection;
    }



    /**
     * Process the JSON request
     *
     * @param request  the request
     * @param what     what to search for
     *
     * @return  the JSON string
     *
     * @throws Exception  problem with processing
     */
    private Result processJsonRequest(Request request, String what)
            throws Exception {


        //        System.err.println("Request:" + request);
        Entry entry = getEntryManager().getEntry(request,
                          request.getString("thecollection", ""));
        //        System.err.println("Entry:" + entry);
        CollectionTypeHandler typeHandler =
            (CollectionTypeHandler) entry.getTypeHandler();
        List<Clause> clauses = new ArrayList<Clause>();
        List<Column> columns = typeHandler.getGranuleColumns();
        for (int fieldIdx = 0; fieldIdx < columns.size(); fieldIdx++) {
            String arg = "field" + fieldIdx;
            String v   = request.getString(arg, "");
            if (v.length() > 0) {
                String column = columns.get(fieldIdx).getName();
                clauses.add(Clause.eq(column, v));
            }
        }

        int columnIdx = request.get("field", 1);
        if (columnIdx >= columns.size()) {
            return new Result("", new StringBuffer(), Json.MIMETYPE);
        }
        Column myColumn = columns.get(columnIdx);
        List<String> values =
            new ArrayList<String>(((CollectionTypeHandler) entry
                .getTypeHandler())
                    .getUniqueColumnValues(entry, columnIdx, clauses, false));
        StringBuffer sb = new StringBuffer();
        if (myColumn.isEnumeration()) {
            List<TwoFacedObject> tfos = typeHandler.getValueList(entry,
                                            values, myColumn);
            tfos.add(0, new TwoFacedObject(""));
            String json = Json.tfoList(tfos);
            //            System.out.println("json:" + json);
            sb.append(json);
        } else {
            values.add(0, "");
            String json = Json.list(values, true);
            //            System.out.println("json:" + json);
            sb.append(json);
        }

        return new Result("", sb, Json.MIMETYPE);
    }



    /**
     *  return the main entry point URL
     *
     * @return  the main entry point
     */
    private String getCompareUrlPath() {
        //Use the collection type in the path. This is defined in the api.xml file
        return getRepository().getUrlBase() + "/model/compare";
    }



    /**
     * Get the type handler
     *
     * @return  the climate collection type handler
     *
     * @throws Exception  problem finding one
     */
    private ClimateCollectionTypeHandler getTypeHandler() throws Exception {
        return (ClimateCollectionTypeHandler) getRepository().getTypeHandler(
            collectionType);
    }



    /**
     * Get the list of collections
     *
     * @param request  the request
     *
     * @return  the list of collections
     *
     * @throws Exception  problem generating list
     */
    private List<Entry> getCollections(Request request) throws Exception {
        Request tmpRequest = new Request(getRepository(), request.getUser());

        tmpRequest.put(ARG_TYPE, collectionType);

        List<Entry> collections =
            (List<Entry>) getEntryManager().getEntries(tmpRequest)[0];

        return collections;
    }


}
