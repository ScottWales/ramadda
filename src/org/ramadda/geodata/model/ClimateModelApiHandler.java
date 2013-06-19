/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
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


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;

import org.ramadda.repository.output.JsonOutputHandler;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.JQuery;
import org.ramadda.util.Json;

import org.ramadda.util.Utils;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.util.StringUtil;
import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;
import java.sql.*;



import java.io.*;


import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;

import java.util.regex.*;

import org.ramadda.util.TTLCache;

/**
 * Provides a top-level API
 *
 */
public class ClimateModelApiHandler extends RepositoryManager implements RequestHandler {

    private static final JQuery JQ = null;
    private String collectionType;

    private TTLCache<Object, Object> cache = new TTLCache<Object, Object>(60*60*1000);

    /**
     * ctor
     *
     * @param repository the repository
     * @param node xml from api.xml
     * @param props properties
     *
     * @throws Exception on badness
     */

    public ClimateModelApiHandler(Repository repository, Element node, Hashtable props)
            throws Exception {
        super(repository);
        collectionType  = Misc.getProperty(props, "collectiontype", "climate_collection");
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
    public Result processClimateModelRequest(Request request) throws Exception {
        if(getTypeHandler() == null) {
            throw new IllegalStateException("Unknown collection type:" +collectionType);
        }
        return processFormRequest(request);
    }

    public static final String ARG_COLLECTION1 = "collection1";
    public static final String ARG_COLLECTION2 = "collection2";
    private String selectArg = "select";



    private Result processFormRequest(Request request) throws Exception {
        String json = request.getString("json",(String) null);
        if(json!=null) {
            return  processJsonRequest(request, json) ;
        }

        CollectionTypeHandler typeHandler = getTypeHandler();
        List<Entry> collections =  getCollections(request);
        if(collections.size()==0) {
            return new Result("Climate Model Analysis", new StringBuffer(getPageHandler().showDialogWarning(msg("No climate collections found"))));
        }
        StringBuffer sb = new StringBuffer();

        String formId = "selectform" + HtmlUtils.blockCnt++;
        sb.append(HtmlUtils.comment("collection form"));

        sb.append(HtmlUtils.importJS(fileUrl("/analysis.js")));
        //        sb.append(HtmlUtils.importJS(fileUrl("/model/analysis.js")));

        sb.append(HtmlUtils.form(getUrlPath()));

        List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
        tfos.add(new TwoFacedObject("Select Climate Collection",""));
        for(Entry collection: collections) {
            tfos.add(new TwoFacedObject(collection.getLabel(), collection.getId()));
        }

        StringBuffer js  = new StringBuffer("\n//collection form initialization\n");
        js.append("var " + formId  + " = new " +
                  HtmlUtils.call("CollectionForm", HtmlUtils.squote(formId)));

        sb.append("<table><tr valign=top>\n");
        for(String prefix: new String[]{ARG_COLLECTION1,
                                        ARG_COLLECTION2}) {

            sb.append(HtmlUtils.open("td", "width=50%"));
            sb.append(HtmlUtils.formTable());
            String collectionSelectId = prefix;
            sb.append(HtmlUtils.formEntry(msgLabel("Collection"), 
                                          HtmlUtils.select(collectionSelectId, tfos, "", HtmlUtils.id(collectionSelectId))));

            sb.append("\n");
            js.append(JQ.change(JQ.id(collectionSelectId), "return " + HtmlUtils.call(formId +".collectionChanged" ,
                                                                                      HtmlUtils.squote(prefix),
                                                                                      HtmlUtils.squote(collectionSelectId))));
            Entry entry  = collections.get(0);
            List<Column> columns = typeHandler.getGranuleColumns();
            for(int selectIdx=0;selectIdx<columns.size();selectIdx++) {
                Column column = columns.get(selectIdx);
                String key = "values::" + entry.getId()+"::" +column.getName();
                List values = new ArrayList();
                String selectId = formId +"_"  + prefix + "_" + selectArg + (selectIdx+1);
                String selectedValue = request.getString(selectArg+selectIdx,"");
                String selectBox = HtmlUtils.select(selectArg + selectIdx ,values,selectedValue,
                                                    " style=\"min-width:250px;\" " +
                                                    HtmlUtils.attr("id",selectId));
                sb.append(HtmlUtils.formEntry(msgLabel(column.getLabel()), selectBox));
                sb.append("\n");
                js.append(JQ.change(JQ.id(selectId), "return " + HtmlUtils.call(formId +".fieldChanged" ,
                                                                                      HtmlUtils.squote(prefix),
                                                                                      HtmlUtils.squote(selectId))));

                //                js.append(JQ.change(JQ.id(selectId), "return " + HtmlUtils.call("collectionChanged" ,
                //                                                                                HtmlUtils.squote(formId),
                //                                                                                HtmlUtils.squote(ormId),
            }
            sb.append(HtmlUtils.formTableClose());
            sb.append("</td>\n");
        }

        sb.append("</tr></table>");

        sb.append("\n");
        sb.append(HtmlUtils.script(js.toString()));
        sb.append("\n");


        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.formClose());
        return new Result("Climate Model Analysis", sb);
    }

    private Result processJsonRequest(Request request, String what) throws Exception {
        Entry entry = getEntryManager().getEntry(request,request.getString("collection",""));
        int columnIdx = 1;
        List<String> values = new ArrayList<String>(((CollectionTypeHandler)entry.getTypeHandler()).getUniqueColumnValues(entry, columnIdx));
        values.add(0,"");
        StringBuffer sb = new StringBuffer();
        sb.append(Json.list(values, true));
        return new Result("", sb, "application/json");
    }



    /**
       return the main entry point URL
     */
    private String getUrlPath() {
        //Use the collection type in the path. This is defined in the api.xml file
        return getRepository().getUrlBase()+"/model/" + collectionType +"/analysis";
    }



    private ClimateCollectionTypeHandler  getTypeHandler() throws Exception {
        return (ClimateCollectionTypeHandler) getRepository().getTypeHandler(collectionType);
    }



    private List<Entry> getCollections(Request request) throws Exception {
        Request tmpRequest = new  Request(getRepository(), request.getUser());
      
        tmpRequest.put(ARG_TYPE, collectionType);

        List<Entry> collections = (List<Entry>) getEntryManager().getEntries(tmpRequest)[0];
        return collections;
    }


}
