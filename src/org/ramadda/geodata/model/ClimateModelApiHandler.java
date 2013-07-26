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


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

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


/**
 * Provides a top-level API
 *
 */
public class ClimateModelApiHandler extends RepositoryManager implements RequestHandler {

    /** _more_          */
    public static final String ARG_ACTION_SEARCH = "action.search";

    /** _more_          */
    public static final String ARG_ACTION_COMPARE = "action.compare";

    /** _more_          */
    public static final String ARG_COLLECTION1 = "collection1";

    /** _more_          */
    public static final String ARG_COLLECTION2 = "collection2";


    /** _more_          */
    private static final JQuery JQ = null;

    /** _more_          */
    private String collectionType;

    /** _more_          */
    private TTLCache<Object, Object> cache = new TTLCache<Object,
                                                 Object>(60 * 60 * 1000);

    /** NCL output handler */
    private NCLOutputHandler nclOutputHandler;
    
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
        nclOutputHandler = new NCLOutputHandler(repository);
    }




    private List<DataProcess> getDataProcesses(Request request) throws Exception {
    	//return getTypeHandler().getDataProcessesToRun(request);
    	List<DataProcess> processes = new ArrayList<DataProcess>();
    	processes.add(new CDOAreaStatisticsProcess(repository));
    	processes.add(new NCLMapPlotDataProcess(repository));
    	return processes;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param operands _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result doCompare(Request request, DataProcessInput dpi)
        throws Exception {

        //This finds the selected processes
        List<DataProcess> processesToRun = getDataProcesses(request);

        //This is the dir under <home>/process
       File processDir = null;
       processDir = dpi.getProcessDir();
       if (processDir == null) {
    	    processDir =  getStorageManager().createProcessDir();
       }
       
       

       List<DataProcessOutput> outputs = new ArrayList<DataProcessOutput>();
       DataProcessInput nextInput = dpi;
        for (DataProcess process : processesToRun) {
            System.err.println("MODEL: applying process: "
                               + process.getDataProcessLabel());
            DataProcessOutput output = process.processRequest(request, nextInput);
            outputs.add(output);

            //make a new input for the next process
            nextInput = new DataProcessInput(processDir, new DataProcessOperand(output.getEntries()));

            //Are we done? This should probably be a check to see if the output has a Result
            if (output.hasOutput()) {
                //break;
            }
          
        }

        List<File> files = new ArrayList<File>();
        File lastFile = null;
        Entry lastEntry = null;
        for (DataProcessOutput dpo : outputs)
        for (Entry granule : dpo.getEntries()) {
            if (granule.isFile()) {
            	lastFile = granule.getFile();
                files.add(lastFile);
            }
            lastEntry = granule;
        }



        //If no processing was done then return the raw files
        if (processesToRun.size()==0) {
            ClimateCollectionTypeHandler typeHandler = getTypeHandler();
            return typeHandler.zipFiles(request,
                            "results.zip",
                            files);
        }

        //Now we get the process entry id
        String processId = processDir.getName();
        String processEntryId = getStorageManager().getProcessDirEntryId(processId);

        //Return the redirect to the process dir for now
        if(false) {
            String       entryUrl = 
                HtmlUtils.url(request.getAbsoluteUrl(getRepository().URL_ENTRY_SHOW),
                              ARG_ENTRYID, processEntryId);
            return new Result(entryUrl);
        }


        //Make the image
        //File imageFile = nclOutputHandler.processRequest(request,
        //                     files.get(0));

        //And return the result
        //String extension = IOUtil.getFileExtension(imageFile.toString());
        //StringBuffer retSB = new StringBuffer();

        String       entryUrl = 
                HtmlUtils.url(request.getAbsoluteUrl(getRepository().URL_ENTRY_SHOW),
                              ARG_ENTRYID, processEntryId+"/"+IOUtil.getFileTail(lastFile.toString()));
        return new Result(entryUrl);
        //return new Result("",
        //                  getStorageManager().getFileInputStream(imageFile),
        //                  getRepository().getMimeTypeFromSuffix(extension));




        /*
        return new Result("Model Compare Results", new StringBuffer("TODO"));
        */

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

        String json = request.getString("json", (String) null);
        if (json != null) {
            return processJsonRequest(request, json);
        }

        Hashtable<String, StringBuffer> extra = new Hashtable<String,
                                                    StringBuffer>();
        List<DataProcessOperand> operands = new ArrayList<DataProcessOperand>();

        File processDir = getStorageManager().createProcessDir();

        //If we are searching or comparing then find the selected entries
        if (request.exists(ARG_ACTION_SEARCH)
                || request.exists(ARG_ACTION_COMPARE)) {
            for (String collection : new String[] { ARG_COLLECTION1,
                    ARG_COLLECTION2 }) {

                StringBuffer tmp = new StringBuffer();
                extra.put(collection, tmp);
                Entry collectionEntry =
                    getEntryManager().getEntry(request,
                        request.getString(getCollectionSelectArg(collection),
                                          ""));
                if (collectionEntry == null) {
                    tmp.append("No collection");

                    continue;
                }
                List<Entry> entries = findEntries(request, collection,
                                          collectionEntry);
                if (entries.isEmpty()) {
                	continue;
                }
                //TODO: fix this later 
                operands.add(new DataProcessOperand(collection, entries));

                tmp.append(getEntryManager().getEntryLink(request,
                        collectionEntry));
                tmp.append("<ul>");
                for (Entry granule : entries) {
                    tmp.append("<li>");
                    tmp.append(getEntryManager().getEntryLink(request,
                            granule));
                }
                tmp.append("</ul>");
            }
        }


        //Check to see if we at least 1 operand 
        boolean hasOperands = false;
        if (operands.size() >= 1) {
            hasOperands = (operands.get(0).getEntries().size() > 0)
                          || (operands.get(1).getEntries().size() > 0);
        }
        


        StringBuffer sb = new StringBuffer();
        DataProcessInput dpi = new DataProcessInput(processDir, operands);

        if (request.exists(ARG_ACTION_COMPARE)) {
            if (hasOperands) {
                try {
                    return doCompare(request, dpi);
                } catch(Exception exc) {
                    sb.append(
                              getPageHandler().showDialogError("An error occurred:<br>" + exc));
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
                "Climate Model Analysis",
                new StringBuffer(
                    getPageHandler().showDialogWarning(
                        msg("No climate collections found"))));
        }

        String formId = "selectform" + HtmlUtils.blockCnt++;
        sb.append(HtmlUtils.comment("collection form"));

        sb.append(HtmlUtils.importJS(fileUrl("/analysis.js")));
        //        sb.append(HtmlUtils.importJS(fileUrl("/model/analysis.js")));

        sb.append(HtmlUtils.form(getCompareUrlPath()));

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
            "Plot monthly maps from different climate model datasets as well as differences between datasets. Means, anomalies and climatologies are available.");

        sb.append("<table><tr valign=top>\n");
        int collectionNumber = 0;
        for (String collection : new String[] { ARG_COLLECTION1,
                ARG_COLLECTION2 }) {


            sb.append(HtmlUtils.open("td", "width=50%"));
            sb.append(HtmlUtils.formTable());
            if (collectionNumber == 0) {
                sb.append("<tr><td colspan=\"2\">Dataset 1</td></tr>\n");
            } else {
                sb.append(
                    "<tr><td colspan=\"2\">Dataset 2 (Optional)</td></tr>\n");
            }
            collectionNumber++;
            String arg = getCollectionSelectArg(collection);
            String collectionWidget =
                HtmlUtils.select(arg, tfos, request.getString(arg, ""),
                                 HtmlUtils.id(getCollectionSelectId(formId,
                                     collection)));

            sb.append(HtmlUtils.formEntry(msgLabel("Collection"),
                                          collectionWidget));

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
                sb.append("\n");
                String selectBox = HtmlUtils.select(arg, values,
                                       selectedValue,
                                       " style=\"min-width:250px;\" "
                                       + HtmlUtils.attr("id",
                                           getFieldSelectId(formId,
                                               collection, fieldIdx)));
                sb.append(HtmlUtils.formEntry(msgLabel(column.getLabel()),
                        selectBox));
                sb.append("\n");
            }
            StringBuffer results = extra.get(collection);
            if (results != null) {
                sb.append(HtmlUtils.formEntry("", results.toString()));
            }


            sb.append(HtmlUtils.formTableClose());
            sb.append("</td>\n");
        }

        sb.append("</tr></table>");
        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.p());




        if ( !hasOperands) {
            sb.append(HtmlUtils.submit("Select Data", ARG_ACTION_SEARCH,
                                       HtmlUtils.id(formId + ".submit")));
        } else {
            List<String>      processTabs   = new ArrayList<String>();
            List<String>      processTitles = new ArrayList<String>();

            boolean           first         = true;
            //List<DataProcess> processes     = typeHandler.getDataProcesses();
            List<DataProcess> processes     = getDataProcesses(request);
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
                process.addToForm(request, dpi, tmpSB);
                processTabs.add(HtmlUtils.div(tmpSB.toString(),
                        HtmlUtils.style("min-height:200px;")));
                processTitles.add(process.getDataProcessLabel());
                first = false;
            }

            sb.append(HtmlUtils.submit("Select New Data", ARG_ACTION_SEARCH,
                                       HtmlUtils.id(formId + ".submit")));

            if (processTitles.size() == 1) {
                sb.append(header(msg(processTitles.get(0))));
                sb.append(processTabs.get(0));
            } else {
                sb.append(header(msg("Process Selected Data")));
                StringBuffer processTable = new StringBuffer();
                processTable.append("<table><tr><td width=\"50%\">");
                HtmlUtils.makeAccordian(processTable, processTitles, processTabs);
                processTable.append("</td>");
                processTable.append("<td width=\"50%\">");
                processTable.append("</td></tr></table>");
                sb.append(processTable);
            }
            sb.append(HtmlUtils.submit("Make Plot", ARG_ACTION_COMPARE,
                                       HtmlUtils.id(formId + ".submit")));
        }


        sb.append("\n");
        sb.append(HtmlUtils.script(js.toString()));
        sb.append("\n");

        sb.append(HtmlUtils.formClose());





        return new Result("Climate Model Comparison", sb);

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param collection _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private List<Entry> findEntries(Request request, String collection,
                                    Entry entry)
            throws Exception {
        CollectionTypeHandler typeHandler =
            (CollectionTypeHandler) entry.getTypeHandler();
        List<Clause>    clauses   = new ArrayList<Clause>();
        List<Column>    columns   = typeHandler.getGranuleColumns();
        HashSet<String> seenTable = new HashSet<String>();
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

            String arg = getFieldSelectArg(collection, fieldIdx);
            String v   = request.getString(arg, "");
            if (v.length() > 0) {
                clauses.add(Clause.eq(column.getName(), v));
            }
        }
        List[] pair = getEntryManager().getEntries(request, clauses,
                          typeHandler.getGranuleTypeHandler());

        return pair[1];
    }

    /**
     * _more_
     *
     * @param collection _more_
     * @param fieldIdx _more_
     *
     * @return _more_
     */
    private String getFieldSelectArg(String collection, int fieldIdx) {
        return collection + "_field" + fieldIdx;

    }

    /**
     * _more_
     *
     * @param collection _more_
     *
     * @return _more_
     */
    private String getCollectionSelectArg(String collection) {
        return collection;
    }


    /**
     * _more_
     *
     * @param formId _more_
     * @param collection _more_
     * @param fieldIdx _more_
     *
     * @return _more_
     */
    private String getFieldSelectId(String formId, String collection,
                                    int fieldIdx) {
        return getCollectionSelectId(formId, collection) + "_field"
               + fieldIdx;
    }

    /**
     * _more_
     *
     * @param formId _more_
     * @param collection _more_
     *
     * @return _more_
     */
    private String getCollectionSelectId(String formId, String collection) {
        return formId + "_" + collection;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param what _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result processJsonRequest(Request request, String what)
            throws Exception {
        Entry entry = getEntryManager().getEntry(request,
                          request.getString("collection", ""));
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

        System.err.println("Clauses:" + clauses);
        int    columnIdx = request.get("field", 1);
        Column myColumn  = columns.get(columnIdx);
        List<String> values =
            new ArrayList<String>(((CollectionTypeHandler) entry
                .getTypeHandler())
                    .getUniqueColumnValues(entry, columnIdx, clauses, false));
        System.err.println("Values:" + values);
        StringBuffer sb = new StringBuffer();
        if (myColumn.isEnumeration() && false) {
            List<String> enumValues = new ArrayList<String>(values.size());
            enumValues.add(Json.attr("", "--", true));
            for (String value : values) {
                enumValues.add(Json.attr(value, myColumn.getEnumLabel(value),
                                         true));
            }
            sb.append(Json.list(enumValues));
        } else {
            values.add(0, "");
            sb.append(Json.list(values, true));
        }

        return new Result("", sb, Json.MIMETYPE);
    }



    /**
     *  return the main entry point URL
     *
     * @return _more_
     */
    private String getCompareUrlPath() {
        //Use the collection type in the path. This is defined in the api.xml file
        return getRepository().getUrlBase() + "/model/compare";
    }



    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private ClimateCollectionTypeHandler getTypeHandler() throws Exception {
        return (ClimateCollectionTypeHandler) getRepository().getTypeHandler(
            collectionType);
    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private List<Entry> getCollections(Request request) throws Exception {
        Request tmpRequest = new Request(getRepository(), request.getUser());

        tmpRequest.put(ARG_TYPE, collectionType);

        List<Entry> collections =
            (List<Entry>) getEntryManager().getEntries(tmpRequest)[0];

        return collections;
    }


}
