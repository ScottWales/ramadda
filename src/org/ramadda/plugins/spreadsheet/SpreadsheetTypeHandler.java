/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.ramadda.plugins.spreadsheet;


import org.w3c.dom.*;

import ucar.unidata.repository.*;
import ucar.unidata.repository.auth.*;
import ucar.unidata.repository.metadata.*;
import ucar.unidata.repository.output.OutputHandler;
import ucar.unidata.repository.output.OutputType;
import ucar.unidata.repository.type.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.xml.XmlUtil;



import java.util.ArrayList;
import java.util.List;


/**
 *
 */

public class SpreadsheetTypeHandler extends GenericTypeHandler {

    /** _more_ */
    public static final String ICON_SPREADSHEET = "ramadda.icon.spreadsheet";

    /** _more_ */
    public static final String ARG_SPREADSHEET_STOREDATA =
        "spreadsheet.storedata";

    /** _more_          */
    public static final String ARG_SPREADSHEET_GETXML = "spreadsheet.getxml";

    /** _more_          */
    public static final String ARG_SPREADSHEET_GETCSV = "spreadsheet.getcsv";




    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public SpreadsheetTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param links _more_
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, Entry entry, List<Link> links)
            throws Exception {

        super.getEntryLinks(request, entry, links);
        /*
        links.add(
            new Link(
                request.url(
                    getRepository().URL_ENTRY_SHOW, ARG_ENTRYID,
                    entry.getId(), ARG_SLIDESHOW_SHOW,
                    "true"), getRepository().iconUrl(ICON_SLIDESHOW),
                             "View Slideshow", OutputType.TYPE_HTML));
        */
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void addToEntryForm(Request request, StringBuffer sb, Entry entry)
            throws Exception {
        super.addToEntryForm(request, sb, entry);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param formBuffer _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void addColumnsToEntryForm(Request request,
                                      StringBuffer formBuffer, Entry entry)
            throws Exception {}



    /**
     *
     *
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {

        Object[] values  = ((entry == null)
                            ? null
                            : entry.getValues());
        String   content = null;
        if ((values == null) || (values[0] == null)
                || (values[0].toString().trim().length() == 0)) {}
        else {
            content = values[0].toString();
        }

        if (request.get(ARG_SPREADSHEET_GETXML, false)) {
            if (content == null) {
                content = "";
            }
            return getRepository().getHtmlOutputHandler().makeAjaxResult(
                request, content);
        }

        if (request.get(ARG_SPREADSHEET_GETCSV, false)) {
            if (content == null) {
                content = "";
            }
            Result result = new Result("", new StringBuffer(content),
                                       "text/csv");
            result.setShouldDecorate(false);
            return result;
        }

        if (request.defined(ARG_SPREADSHEET_STOREDATA)) {
            if ( !getAccessManager().canDoAction(request, entry,
                    Permission.ACTION_EDIT)) {
                throw new AccessException("Cannot edit:" + entry.getLabel(),
                                          request);
            }
            String ss = request.getString(ARG_SPREADSHEET_STOREDATA, "");
            entry.setValues(new Object[] { ss });
            List<Entry> entries = new ArrayList<Entry>();
            entries.add(entry);
            getEntryManager().insertEntries(entries, false);
            System.err.println("storing:" + ss);
            return getRepository().getHtmlOutputHandler().makeAjaxResult(
                request, "ok");
            //            String url = request.entryUrl(getRepository().URL_ENTRY_SHOW, entry);
            //            return new Result(url);
        }

        return getSpreadsheet(request, entry);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getSpreadsheet(Request request, Entry entry)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        //TODO: use real path
        sb.append(
            "<link media=\"all\" href=\"/repository/spreadsheet/styles.css\" rel=\"stylesheet\" type=\"text/css\" />\n");
        sb.append(
            HtmlUtil.importJS("/repository/spreadsheet/spreadsheet.js"));
        sb.append(
            HtmlUtil.importJS("/repository/spreadsheet/myspreadsheet.js"));
        sb.append(
            "<div class=\"data\" id=\"data\"></div><div id=\"source\" align=\"center\">");


        //        sb.append(HtmlUtil.script(HtmlUtil.call("loadSheetFromUrl", HtmlUtil.squote(spreadsheetUrl))));
        sb.append("</div>");
        sb.append(request.form(getRepository().URL_ENTRY_SHOW,
                               HtmlUtil.attr("name", "ssform")
                               + HtmlUtil.id("ssform")));

        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtil.hidden(ARG_SPREADSHEET_STOREDATA, "",
                                  HtmlUtil.id(ARG_SPREADSHEET_STOREDATA)));
        sb.append(HtmlUtil.formClose());

        StringBuffer js = new StringBuffer();
        js.append("var entryId = " + HtmlUtil.squote(entry.getId()) + ";\n");
        js.append(HtmlUtil.call("loadFromRamadda",
                                HtmlUtil.squote(entry.getId())));

        sb.append(HtmlUtil.script(js.toString()));
        //        System.err.println("******\n" + sb+"\n******");
        Result result = new Result("", sb);
        return result;
    }




}
