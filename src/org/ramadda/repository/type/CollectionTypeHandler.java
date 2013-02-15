package org.ramadda.repository.type;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import java.sql.*;


import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import org.ramadda.repository.*;
import org.ramadda.repository.database.*;
import org.ramadda.repository.type.*;
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

public class CollectionTypeHandler extends ExtensibleGroupTypeHandler {
    
    private static final JQuery JQ = null;

    public static final String ARG_SEARCH =  "search";

    public static final String PROP_GRANULE_TYPE  = "granule_type";

    private String dbTableName;

    private String dbColumnCollectionId;

    private List<Column> columns;

    private String selectArg = "select";

    private TypeHandler granuleTypeHandler;

    private TTLCache<String, Object> cache = new TTLCache<String, Object>(60*60*1000);

    public CollectionTypeHandler(Repository repository, Element entryNode)
        throws Exception {
        super(repository, entryNode);
    }


    private TypeHandler getGranuleTypeHandler() throws Exception {
        if(granuleTypeHandler ==null) {
            dbTableName = getProperty(PROP_GRANULE_TYPE,"");
            granuleTypeHandler = getRepository().getTypeHandler(getProperty(PROP_GRANULE_TYPE,""));
            columns = new ArrayList<Column>(granuleTypeHandler.getColumns());
            dbColumnCollectionId = columns.get(0).getName();
            columns.remove(0);
        }
        return granuleTypeHandler;
    }


    public Result getMetadataJson(Request request, Entry entry,
                                 List<Entry> subGroups, List<Entry> entries)
        throws Exception {
        List<String> values = new ArrayList<String>();
        List<Clause> clauses = new ArrayList<Clause>();
        Column nextColumn = columns.get(0);
        String nextColumnName = nextColumn.getLabel();
        for(int selectIdx=0;selectIdx<columns.size();selectIdx++) {
            if(!request.defined(selectArg+selectIdx)) break;
            if(selectIdx<columns.size()-1) {
                nextColumn = columns.get(selectIdx+1);
            } else {
                nextColumn = null;
            }
            String column=  columns.get(selectIdx).getName();
            String v = request.getString(selectArg+selectIdx,"");
            clauses.add(Clause.eq(column, v));
            values.add(v);
        }
        String valueKey = "json::" + entry.getId() + "::" + StringUtil.join("::", values);
        StringBuffer json = (StringBuffer) cache.get(valueKey);
        if(json == null) {
            List<String> uniqueValues= new ArrayList<String>();
            if(nextColumn!=null) {
                clauses.add(Clause.eq(dbColumnCollectionId, entry.getId()));
                Statement stmt = getRepository().getDatabaseManager().select(
                                                                             SqlUtil.distinct(dbTableName+"."+nextColumn.getName()),
                                                                             dbTableName, Clause.and(clauses));
                List<String> dbValues = (List<String>)Misc.toList(SqlUtil.readString(getRepository().getDatabaseManager().getIterator(stmt), 1));
                for(String value: dbValues) {
                    String label = nextColumn.getEnumLabel(value);
                    uniqueValues.add(value+":" + label);
                }
            }
            String selectLabel;
            nextColumnName = nextColumnName.toLowerCase();
            if(nextColumnName.startsWith("a") || nextColumnName.startsWith("e") || nextColumnName.startsWith("i") ||
               nextColumnName.startsWith("o") || nextColumnName.startsWith("u")) {
                selectLabel = ":-- Select an " + nextColumnName +" --";
            } else {
                selectLabel = ":-- Select a " + nextColumnName +" --";
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

    public Result getForm(Request request, Entry entry,
                          List<Entry> subGroups, List<Entry> entries)
        throws Exception {
        StringBuffer sb     = new StringBuffer();
        sb.append(entry.getDescription());
        String formId = "selectform" + HtmlUtils.blockCnt++;
        sb.append(HtmlUtils.form(request.entryUrl(getRepository().URL_ENTRY_SHOW, entry),
                                 HtmlUtils.attr("id", formId)));
        sb.append(HtmlUtils.formTable());

        StringBuffer js = new StringBuffer();
        js.append("var " + formId + " = new SelectForm(" + HtmlUtils.squote(formId)+"," + HtmlUtils.squote(entry.getId()) +");\n");
        sb.append(request.form(getRepository().URL_ENTRY_FORM,
                               HtmlUtils.attr("id", formId)));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));

        List<String> firstValues = (List<String>)cache.get("firstValues::" + entry.getId());
        if(firstValues == null) {
            Statement stmt = getRepository().getDatabaseManager().select(
                                                                         SqlUtil.distinct(dbTableName+"."+columns.get(0).getName()),
                                                                         dbTableName, Clause.eq(dbColumnCollectionId, entry.getId()));
            firstValues = (List<String>)Misc.toList(SqlUtil.readString(getRepository().getDatabaseManager().getIterator(stmt), 1));
            cache.put("firstValues::" + entry.getId(), firstValues);
        }


        for(int selectIdx=0;selectIdx<columns.size();selectIdx++) {
            String column=  columns.get(selectIdx).getName();
            String label=  columns.get(selectIdx).getLabel();
            sb.append(HtmlUtils.p());
            List values = new ArrayList();
            if(selectIdx==0) {
                values.add(new TwoFacedObject("-- Select a " + label + " --",""));
                values.addAll(firstValues);
            }  else {
                values.add(new TwoFacedObject("--",""));
            }
            String selectId = formId +"_"  + selectArg + selectIdx;
            String selectBox = HtmlUtils.select(selectArg + selectIdx ,values,(String)null,
                                                " style=\"min-width:200px;\" " +
                                                HtmlUtils.attr("id",selectId));
            sb.append(HtmlUtils.formEntry(msgLabel(label), selectBox));
            js.append(JQ.change(JQ.id(selectId), "return " + HtmlUtils.call(formId +".select" ,HtmlUtils.squote("" + selectIdx))));
        }
        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit("submit","Submit"));

        js.append(JQ.submit(JQ.id(formId), "return " +  HtmlUtils.call(formId +".submit", "")));
        sb.append(HtmlUtils.script(js.toString()));
        sb.append(HtmlUtils.formClose());

        if(request.exists(ARG_SEARCH)) {
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
        return new Result(msg(getLabel()), sb);
    }

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
        //Always call this to initialize things
        getGranuleTypeHandler();

        //Check if the user clicked on tree view, etc.
        if ( !isDefaultHtmlOutput(request)) {
            return null;
        }
        if(request.get("metadata", false)) {
            return getMetadataJson(request, entry, subGroups, entries);
        }
        return getForm(request, entry, subGroups, entries);
    }


    private List<Entry> processSearch(Request request, Entry group) throws Exception {
        List<Clause> clauses = new ArrayList<Clause>();
        clauses.add(Clause.eq(dbColumnCollectionId, group.getId()));
        clauses.add(Clause.join(Tables.ENTRIES.COL_ID,
                                dbTableName + ".id"));
        for(int i=0;i<columns.size();i++) {
            String column=  columns.get(i).getName();
            String urlArg = selectArg + i;
            if(request.defined(urlArg)) {
                clauses.add(Clause.makeOrSplit(dbTableName +"." + column, request.getString(urlArg)));
            }
        }
        List[] pair = getEntryManager().getEntries(request, clauses, granuleTypeHandler);
        //        pair[0] is the folder entries. shouldn't have any here
        return (List<Entry>) pair[1];
    }


}
