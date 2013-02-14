package gov.noaa.esrl.psd.repository.data.model;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import java.sql.*;


import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.ramadda.repository.*;
import org.ramadda.repository.database.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TTLCache;
import org.ramadda.util.JQuery;
import org.ramadda.repository.type.Column;
import org.ramadda.repository.type.GenericTypeHandler;
import org.ramadda.repository.type.ExtensibleGroupTypeHandler;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ucar.unidata.util.IOUtil;

public class ClimateCollectionTypeHandler extends ExtensibleGroupTypeHandler {
    
    private static final JQuery JQ = null;
    
    /** ClimateCollection type */
    public static final String TYPE_CLIMATE_COLLECTION = "noaa_climate_collection";

    public static final String DB_TABLE_NAME = "noaa_climate_modelfile";
    public static final String DB_COLUMN_COLLECTION_ID= DB_TABLE_NAME +".collection_id";

    public static final String ARG_PREFIX = "climate.";

    public static final String ARG_CLIMATE_SEARCH = ARG_PREFIX + "search";

    public static final String ARG_CLIMATE_VARIABLE = "variable";
    public static final String ARG_CLIMATE_MODEL = "model";
    public static final String ARG_CLIMATE_EXPERIMENT = "experiment";
    public static final String ARG_CLIMATE_ENSEMBLE = "ensemble";
    public static final String ARG_CLIMATE_FREQUENCY = "frequency";

    

    public static final String [] FIELDS  = {ARG_CLIMATE_MODEL,
                                             ARG_CLIMATE_EXPERIMENT,
                                             ARG_CLIMATE_ENSEMBLE,
                                             ARG_CLIMATE_FREQUENCY,
                                             ARG_CLIMATE_VARIABLE,
    };

    public static final String [] FIELD_NAMES  = {"Model",
                                                  "Experiment",
                                                  "Ensemble",
                                                  "Frequency","Variable",};




    public ClimateCollectionTypeHandler(Repository repository, Element entryNode)
        throws Exception {
        super(repository, entryNode);
    }


    
    private TTLCache<String, Object> cache = new TTLCache<String, Object>(60*60*1000);


    /**
     * Get the HTML display for this type
     *
     * @param request  the Request
     * @param group    the group
     * @param subGroups    the subgroups
     * @param entries      the Entries
     *
     * @return  the Result
     *
     * @throws Exception  problem getting the HTML
     */
    public Result getHtmlDisplay(Request request, Entry entry,
                                 List<Entry> subGroups, List<Entry> entries)
        throws Exception {

        //Check if the user clicked on tree view, etc.
        if ( !isDefaultHtmlOutput(request)) {
            return null;
        }
        StringBuffer sb     = new StringBuffer();

        String selectArg = "select";
        if(request.exists(selectArg+"1")) {
            List<String> values = new ArrayList<String>();
            List<Clause> clauses = new ArrayList<Clause>();
            String nextColumn = FIELDS[0];
            String nextColumnName = FIELD_NAMES[0];
            for(int selectIdx=0;selectIdx<FIELDS.length;selectIdx++) {
                if(!request.defined(selectArg+selectIdx)) break;
                if(selectIdx<FIELDS.length-1) {
                    nextColumn = FIELDS[selectIdx+1];
                    nextColumnName = FIELD_NAMES[selectIdx+1];
                } else {
                    nextColumn = null;
                }
                String column=  FIELDS[selectIdx];
                String v = request.getString(selectArg+selectIdx,"");
                clauses.add(Clause.eq(column, v));
                values.add(v);
            }
            String valueKey = "json::" + entry.getId() + "::" + StringUtil.join("::", values);
            StringBuffer json = (StringBuffer) cache.get(valueKey);
            if(json == null) {
                List<String> uniqueValues=null;
                if(nextColumn!=null) {
                    clauses.add(Clause.eq(DB_COLUMN_COLLECTION_ID, entry.getId()));
                    Statement stmt = getRepository().getDatabaseManager().select(
                                                                                 SqlUtil.distinct(DB_TABLE_NAME+"."+nextColumn),
                                                                                 DB_TABLE_NAME, Clause.and(clauses));
                    uniqueValues = (List<String>)Misc.toList(SqlUtil.readString(getRepository().getDatabaseManager().getIterator(stmt), 1));
                } else {
                    uniqueValues = new ArrayList<String>();
                }
                String selectLabel;
                nextColumnName = nextColumnName.toLowerCase();
                if(nextColumnName.startsWith("a") || nextColumnName.startsWith("e") || nextColumnName.startsWith("i") ||
                   nextColumnName.startsWith("o") || nextColumnName.startsWith("u")) {
                    selectLabel = "-- Select an " + nextColumnName +" --";
                } else {
                    selectLabel = "-- Select a " + nextColumnName +" --";
                }
                uniqueValues.add(0,selectLabel);
                json = new StringBuffer();
                json.append(HtmlUtils.jsonMap(new String[]{
                            "values", HtmlUtils.jsonList(uniqueValues)},false));
                System.err.println(json);
                cache.put(valueKey, json);
            }
            return new Result(BLANK, json,
                              getRepository().getMimeTypeFromSuffix(".json"));
        }





        sb.append(entry.getDescription());
        String formId = "form" + HtmlUtils.blockCnt++;
        sb.append(HtmlUtils.form(request.entryUrl(getRepository().URL_ENTRY_SHOW, entry),
                                 HtmlUtils.attr("id", formId)));
        sb.append(HtmlUtils.formTable());


        StringBuffer js = new StringBuffer();
        js.append("var " + formId + " = new Form(" + HtmlUtils.squote(formId)+"," + HtmlUtils.squote(entry.getId()) +");\n");
        sb.append(request.form(getRepository().URL_ENTRY_FORM,
                               HtmlUtils.attr("id", formId)));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));

        List<String> models = (List<String>)cache.get("models::" + entry.getId());
        if(models == null) {
            Statement stmt = getRepository().getDatabaseManager().select(
                                                                         SqlUtil.distinct(DB_TABLE_NAME+"."+FIELDS[0]),
                                                                         DB_TABLE_NAME, Clause.eq(DB_COLUMN_COLLECTION_ID, entry.getId()));
            models = (List<String>)Misc.toList(SqlUtil.readString(getRepository().getDatabaseManager().getIterator(stmt), 1));
            cache.put("models::" + entry.getId(), models);
        }


        for(int selectIdx=0;selectIdx<FIELDS.length;selectIdx++) {
            String column=  FIELDS[selectIdx];
            String label=  FIELD_NAMES[selectIdx];
            sb.append(HtmlUtils.p());
            List values = new ArrayList();
            if(selectIdx==0) {
                values.add(new TwoFacedObject("-- Select a model --",""));
                values.addAll(models);
            }  else {
                values.add(new TwoFacedObject("--",""));
            }
            String selectBox = HtmlUtils.select(selectArg + selectIdx ,values,(String)null,
                                                " style=\"min-width:200px;\" " +
                                                HtmlUtils.attr("id",formId +"_"  + selectArg + selectIdx));
            sb.append(HtmlUtils.formEntry(msgLabel(label), selectBox));
            js.append(JQ.change(JQ.id(formId+"_" + selectArg + selectIdx), "return " + HtmlUtils.call(formId +".select" ,HtmlUtils.squote("" + selectIdx))));
        }
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit("submit","Submit"));

        js.append(JQ.submit(JQ.id(formId), "return " +  HtmlUtils.call(formId +".submit", "")));
        sb.append(HtmlUtils.script(js.toString()));
        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.formClose());

        if(request.exists(ARG_CLIMATE_SEARCH)) {
            sb.append(HtmlUtils.p());
            entries =  processSearch(request, entry);
            if(entries.size()==0) {
                sb.append(msg("No entries found"));
            } else {
                sb.append("Found " + entries.size() +" results");
                sb.append(HtmlUtils.p());
                for(Entry child: entries) {
                    sb.append(getEntryManager().getBreadCrumbs(request,
                                                               child));
                    sb.append(HtmlUtils.br());
                }
            }
        }
        return new Result(msg("Climate Collection"), sb);
    }



    private List<Entry> processSearch(Request request, Entry group) throws Exception {
        List<Clause> clauses = new ArrayList<Clause>();
        clauses.add(Clause.eq(DB_COLUMN_COLLECTION_ID, group.getId()));
        clauses.add(Clause.join(Tables.ENTRIES.COL_ID,
                                DB_TABLE_NAME + ".id"));
        for(int i=0;i<FIELDS.length;i++) {
            String column=  FIELDS[i];
            String urlArg = ARG_PREFIX + column;
            if(request.defined(urlArg)) {
                clauses.add(Clause.makeOrSplit(DB_TABLE_NAME +"." + column, request.getString(urlArg)));
            }
        }
        List[] pair = getEntryManager().getEntries(request, clauses, getRepository().getTypeHandler(ClimateModelFileTypeHandler.TYPE_CLIMATE_MODELFILE));
        //        pair[0] is the folder entries. shouldn't have any here
        return (List<Entry>) pair[1];
    }


}
