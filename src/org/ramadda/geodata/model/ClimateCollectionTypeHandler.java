
package org.ramadda.geodata.model;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import java.sql.*;


import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import org.ramadda.repository.*;
import org.ramadda.repository.database.*;
import org.ramadda.geodata.cdmdata.CDOOutputHandler;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
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

public class ClimateCollectionTypeHandler extends CollectionTypeHandler  {

    private CDOOutputHandler cdoOutputHandler;

    public ClimateCollectionTypeHandler(Repository repository, Element entryNode)
        throws Exception {
        super(repository, entryNode);
        cdoOutputHandler = new CDOOutputHandler(repository);
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
        if(request.get("metadata", false)) {
            return getMetadataJson(request, entry, subGroups, entries);
        }


        StringBuffer sb     = new StringBuffer();
        sb.append(entry.getDescription());
        String formId = "selectform" + HtmlUtils.blockCnt++;

        sb.append(HtmlUtils.form(request.entryUrl(getRepository().URL_ENTRY_SHOW, entry),
                                 HtmlUtils.id(formId)));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));



        StringBuffer selectorSB = new StringBuffer();
        selectorSB.append(HtmlUtils.formTable());
        addSelectorsToForm(request, entry, subGroups, entries, selectorSB, formId);
        selectorSB.append(HtmlUtils.formTableClose());

        StringBuffer analysisSB = new StringBuffer();
        StringBuffer productSB = new StringBuffer();

        productSB.append(HtmlUtils.submit(msg("Search for files"),ARG_SEARCH));

        sb.append(header(msg("Select Data")));
        sb.append(selectorSB);

        cdoOutputHandler.addToForm(request, entry, analysisSB);
        sb.append(header(msg("Do Something to Data")));
        sb.append(analysisSB);

        sb.append(header(msg("Do Something with Data")));
        sb.append(productSB);



        StringBuffer js = new StringBuffer();
        //        js.append(JQ.submit(JQ.id(formId), "return " +  HtmlUtils.call(formId +".submit", "")));
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




}
