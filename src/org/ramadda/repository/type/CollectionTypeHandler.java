package org.ramadda.repository.type;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import java.sql.*;


import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import org.ramadda.repository.*;
import org.ramadda.repository.output.JsonOutputHandler;
import org.ramadda.repository.output.ZipOutputHandler;
import org.ramadda.repository.database.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;
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
    
    public static final JQuery JQ = null;

    public static final String ARG_SEARCH =  "search";
    public static final String ARG_FIELD =  "field";
    public static final String ARG_REQUEST =  "request";
    public static final String REQUEST_METADATA =  "metadata";
    public static final String REQUEST_SEARCH=  "search";
    public static final String REQUEST_DOWNLOAD=  "download";

    public static final String PROP_GRANULE_TYPE  = "granule_type";

    private JsonOutputHandler jsonOutputHandler;
    private ZipOutputHandler zipOutputHandler;


    private String dbColumnCollectionId;

    private List<Column> columns;

    private String selectArg = "select";

    private TypeHandler granuleTypeHandler;

    private TTLCache<Object, Object> cache = new TTLCache<Object, Object>(60*60*1000);

    public CollectionTypeHandler(Repository repository, Element entryNode)
        throws Exception {
        super(repository, entryNode);
    }


    public JsonOutputHandler getJsonOutputHandler () {
        if(jsonOutputHandler == null) {
            jsonOutputHandler  =(JsonOutputHandler)getRepository().getOutputHandler(org.ramadda.repository.output.JsonOutputHandler.class);
        }
        return         jsonOutputHandler;
    }

    public ZipOutputHandler getZipOutputHandler () {
        if(zipOutputHandler == null) {
            zipOutputHandler  =(ZipOutputHandler)getRepository().getOutputHandler(org.ramadda.repository.output.ZipOutputHandler.class);
        }
        return         zipOutputHandler;
    }

    public TypeHandler getGranuleTypeHandler() throws Exception {
        if(granuleTypeHandler ==null) {
            granuleTypeHandler = getRepository().getTypeHandler(getProperty(PROP_GRANULE_TYPE,""));
            columns = new ArrayList<Column>(granuleTypeHandler.getColumns());
            dbColumnCollectionId = columns.get(0).getName();
            columns.remove(0);
        }
        return granuleTypeHandler;
    }



    public Result getMetadataJson(Request request, Entry entry)
        throws Exception {
        int field = 0;
        for(int i=0;i<10;i++) {
            if(request.defined(ARG_FIELD+i)) {
                field = i;
                break;
            }

        }


        StringBuffer key = new StringBuffer("json::" + entry.getId() + ":field:");
        List<Clause> clauses = getClauses(request, entry, key);
        StringBuffer json = (StringBuffer) cache.get(key);
        if(json == null) {
            Column column = columns.get(field);
            List<String> uniqueValues= getUniqueValues(entry, column, clauses);
            String nextColumnName = column.getLabel();
            String selectLabel = ":-- Select "  + Utils.getArticle(nextColumnName) +" " + nextColumnName + " --";
            uniqueValues.add(0,selectLabel);
            json = new StringBuffer();
            json.append(HtmlUtils.jsonMap(new String[]{
                        "values", HtmlUtils.jsonList(uniqueValues)},false));
            System.err.println(json);
            cache.put(key, json);
        }
        return new Result(BLANK, json,
                          getRepository().getMimeTypeFromSuffix(".json"));
    }


    public void makeMetadataTree(Request request, Entry entry, StringBuffer sb, int colIdx, List<Clause>clauses)     throws Exception {

    }


    public void makeMetadataTree(Request request, Entry entry)     throws Exception {
        StringBuffer tree = new StringBuffer();
        tree.append("<ul>");
        for(Column column: columns) {
            List<Clause> clauses = new ArrayList<Clause>();
            List<String> uniqueValues= getUniqueValues(entry, column, clauses);
            for(String v: uniqueValues) {
                tree.append("<li> " + v);
            }
        }
        tree.append("</ul>");
    }




    private List<String> getUniqueValues(Entry entry, Column column, List<Clause> clauses) throws Exception {
        clauses = new ArrayList<Clause>(clauses);
        List<String> uniqueValues= new ArrayList<String>();
        if(column!=null) {
            clauses.add(Clause.eq(dbColumnCollectionId, entry.getId()));
            Statement stmt = getDatabaseManager().select(
                                                         SqlUtil.distinct(column.getTableName()+"."+column.getName()),
                                                         column.getTableName(), Clause.and(clauses));
            List<String> dbValues = (List<String>)Misc.toList(SqlUtil.readString(getRepository().getDatabaseManager().getIterator(stmt), 1));
            for(TwoFacedObject tfo: getValueList(entry, dbValues, column)) {
                uniqueValues.add(tfo.getId()+":" + tfo.getLabel());
            }
        }
        return uniqueValues;
    }


    public List<Clause> getClauses(Request request, Entry entry, StringBuffer key) throws Exception {
        List<Clause> clauses = new ArrayList<Clause>();
        for(int selectIdx=0;selectIdx<columns.size();selectIdx++) {
            if(!request.defined(selectArg+selectIdx)) continue;
            String column=  columns.get(selectIdx).getName();
            String v = request.getString(selectArg+selectIdx,"");
            clauses.add(Clause.eq(column, v));
            if(key!=null) key.append(column+"="+v+";");
        }
        return clauses;
    }


    private Hashtable<String,Properties> labelCache = new Hashtable<String,Properties>();

    private List<TwoFacedObject> getValueList(Entry collectionEntry, List values, Column column) throws Exception {
        Hashtable map  = column.getEnumTable();
        String key  = column.getName()+".values";
        String vocabFile = getProperty(key,(String) null);
        if(vocabFile!=null) {
            Properties properties = labelCache.get(vocabFile);
            if(properties == null) {
                properties = new Properties();
                getRepository().loadProperties(properties, vocabFile);
                labelCache.put(vocabFile, properties);
            }
            map  = new Hashtable();
            map.putAll(properties);
        }
        List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
        for(String value: (List<String>)values) {
            String label = (String) map.get(value);
            if(label == null) label = value;
            tfos.add(new TwoFacedObject(label, value));
        }
        return tfos;
    }


    public void addSelectorsToForm(Request request, Entry entry,
                                   StringBuffer sb, String formId, StringBuffer js)
        throws Exception {

        for(int selectIdx=0;selectIdx<columns.size();selectIdx++) {
            Column column = columns.get(selectIdx);
            String key = "values::" + entry.getId()+"::" +column.getName();
            List values = (List)cache.get(key);
            if(values == null) {
                Statement stmt = getRepository().getDatabaseManager().select(
                                                                             SqlUtil.distinct(column.getTableName()+"."+column.getName()),
                                                                             column.getTableName(), Clause.eq(dbColumnCollectionId, entry.getId()));
                values = getValueList(entry, Misc.toList(SqlUtil.readString(getRepository().getDatabaseManager().getIterator(stmt), 1)), column);
                values.add(0, new TwoFacedObject("-- Select " + Utils.getArticle(column.getLabel()) +" " +column.getLabel() + " --",""));
                //                values.add(0, new TwoFacedObject("FOOBAR","foobar"));
                cache.put(key, values);
            }
            String selectId = formId +"_"  + selectArg + selectIdx;
            String selectedValue = request.getString(selectArg+selectIdx,"");
            String selectBox = HtmlUtils.select(selectArg + selectIdx ,values,selectedValue,
                                                " style=\"min-width:250px;\" " +
                                                HtmlUtils.attr("id",selectId));
            sb.append(HtmlUtils.formEntry(msgLabel(column.getLabel()), selectBox));
            js.append(JQ.change(JQ.id(selectId), "return " + HtmlUtils.call(formId +".select" ,HtmlUtils.squote("" + selectIdx))));
        }
    }


    public void addJsonSelectorsToForm(Request request, Entry entry,
                                       StringBuffer sb, String formId,StringBuffer js)
        throws Exception {


        List firstValues =  (List)cache.get("firstValues::" + entry.getId());
        if(firstValues == null) {
            Column column = columns.get(0);
            Statement stmt = getRepository().getDatabaseManager().select(
                                                                         SqlUtil.distinct(column.getTableName()+"."+column.getName()),
                                                                         column.getTableName(), Clause.eq(dbColumnCollectionId, entry.getId()));
            firstValues = getValueList(entry, Misc.toList(SqlUtil.readString(getRepository().getDatabaseManager().getIterator(stmt), 1)), columns.get(0));
            cache.put("firstValues::" + entry.getId(), firstValues);
        }


        for(int selectIdx=0;selectIdx<columns.size();selectIdx++) {
            String column=  columns.get(selectIdx).getName();
            String label=  columns.get(selectIdx).getLabel();
            List values = new ArrayList();
            if(selectIdx==0) {
                values.add(new TwoFacedObject("-- Select a " + label + " --",""));
                values.addAll(firstValues);
            }  else {
                values.add(new TwoFacedObject("--",""));
            }
            String selectId = formId +"_"  + selectArg + selectIdx;
            String selectedValue = request.getString(selectArg+selectIdx,"");

            String selectBox = HtmlUtils.select(selectArg + selectIdx ,values,selectedValue,
                                                " style=\"min-width:250px;\" " +
                                                HtmlUtils.attr("id",selectId));
            sb.append(HtmlUtils.formEntry(msgLabel(label), selectBox));
            js.append(JQ.change(JQ.id(selectId), "return " + HtmlUtils.call(formId +".select" ,HtmlUtils.squote("" + selectIdx))));
        }
    }


    public Result processRequest(Request request, Entry entry) throws Exception {
        String what = request.getString(ARG_REQUEST,(String) null);
        if(what == null) return null;
        if(what.equals(REQUEST_METADATA)) {
            return getMetadataJson(request, entry);
        }

        if(what.equals(REQUEST_SEARCH) || request.defined(ARG_SEARCH)) {
            StringBuffer sb = new StringBuffer();
            getJsonOutputHandler().makeJson(request, processSearch(request, entry),sb);
            return new Result("", sb, "application/json");
        }

        if(what.equals(REQUEST_DOWNLOAD)) {
            request.setReturnFilename(entry.getName()+".zip");
            return getZipOutputHandler().toZip(request,entry.getName(), processSearch(request, entry),false,false);
        }

        return null;
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
@Override
    public Result getHtmlDisplay(Request request, Entry entry,
                                 List<Entry> subGroups, List<Entry> entries)
        throws Exception {
        //Always call this to initialize things
        getGranuleTypeHandler();

        //Check if the user clicked on tree view, etc.
        if ( !isDefaultHtmlOutput(request)) {
            return null;
        }

        Result result = processRequest(request, entry);
        if(result!=null) return result;
        StringBuffer sb     = new StringBuffer();
        sb.append(entry.getDescription());
        String formId = "selectform" + HtmlUtils.blockCnt++;

        sb.append(HtmlUtils.form(request.entryUrl(getRepository().URL_ENTRY_SHOW, entry),
                                 HtmlUtils.attr("id", formId)));
        sb.append(HtmlUtils.formTable());

        StringBuffer js = new StringBuffer();
        js.append("var " + formId + " = new SelectForm(" + HtmlUtils.squote(formId)+"," + HtmlUtils.squote(entry.getId()) +");\n");
        addJsonSelectorsToForm(request, entry, sb, formId, js);
        sb.append(js);

        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit(ARG_SEARCH,"Submit", HtmlUtils.id(formId+".search")));
        js.append(JQ.submit(JQ.id(formId+".search"), "return " + HtmlUtils.call(formId +".search","")));
        sb.append(HtmlUtils.script(js.toString()));
        sb.append(HtmlUtils.formClose());


        return new Result(msg(getLabel()), sb);


    }


    public  void addClauses(Request request, Entry group, List<Clause> clauses) throws Exception {
        HashSet<String> seenTable = new HashSet<String>();
        for(int i=0;i<columns.size();i++) {
            Column column = columns.get(i);
            String dbTableName =  column.getTableName();
            if(!seenTable.contains(dbTableName)) {
                clauses.add(Clause.eq(dbTableName +"." + dbColumnCollectionId, group.getId()));
                clauses.add(Clause.join(Tables.ENTRIES.COL_ID,
                                        dbTableName + ".id"));
                seenTable.add(dbTableName);
            }
            String urlArg = selectArg + i;
            if(request.defined(urlArg)) {
                clauses.add(Clause.makeOrSplit(dbTableName +"." + column.getName(), request.getString(urlArg)));
            }
        }
    }


    public  List<Entry> processSearch(Request request, Entry group) throws Exception {
        List<Clause> clauses = new ArrayList<Clause>();
        addClauses(request, group, clauses);
        List[] pair = getEntryManager().getEntries(request, clauses, getGranuleTypeHandler());
        //        pair[0] is the folder entries. shouldn't have any here
        return (List<Entry>) pair[1];
    }

    public String openForm(Request request, Entry entry, StringBuffer sb, StringBuffer js) {
        String formId = "selectform" + HtmlUtils.blockCnt++;
        sb.append(HtmlUtils.form(request.entryUrl(getRepository().URL_ENTRY_SHOW, entry),
                                 HtmlUtils.id(formId)));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        js.append("var " + formId + " =  " + HtmlUtils.call("new  SelectForm",  HtmlUtils.jsMakeArgs(new String[]{formId,entry.getId(),"select", formId+"_output"}, true))+"\n");

        return formId;
    }

    public void appendSearchResults(Request request, Entry entry, StringBuffer sb) throws Exception {
        if(request.exists(ARG_SEARCH)) {
            sb.append(HtmlUtils.p());
            List<Entry>        entries =  processSearch(request, entry);
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
    }



}
