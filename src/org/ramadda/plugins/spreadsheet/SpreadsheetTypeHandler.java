/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.plugins.spreadsheet;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;

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

    /** _more_ */
    public static final String ARG_SPREADSHEET_GETXML = "spreadsheet.getxml";

    /** _more_ */
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
                             "View Slideshow", OutputType.TYPE_VIEW));
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
