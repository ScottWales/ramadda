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

package org.ramadda.plugins.faq;


import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Result;
import org.ramadda.repository.auth.Permission;
import org.ramadda.repository.type.ExtensibleGroupTypeHandler;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.Element;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 */
public class FaqTypeHandler extends ExtensibleGroupTypeHandler {


    /** the FAQ type id */
    public static String TYPE_FAQ = "faq";

    /**
     * Create a new FaqTypeHandler
     *
     * @param repository  the Repository
     * @param entryNode   the Entry node XML
     *
     * @throws Exception  problems creating handler
     */
    public FaqTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
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
    public Result getHtmlDisplay(Request request, Entry group,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {

        if ( !isDefaultHtmlOutput(request)) {
            return null;
        }

        StringBuffer sb     = new StringBuffer();

        boolean      canAdd = getAccessManager().canDoAction(request, group,
                             Permission.ACTION_NEW);

        if (canAdd) {
            sb.append(HtmlUtils
                .href(HtmlUtils
                    .url(request
                        .entryUrl(getRepository().URL_ENTRY_FORM, group,
                            ARG_GROUP), ARG_TYPE,
                                FaqEntryTypeHandler.TYPE_FAQENTRY), HtmlUtils
                                    .img(getRepository().iconUrl(ICON_NEW),
                                        msg("New FAQ Question"))));
        }



        sb.append(group.getDescription());
        Hashtable<String, StringBuffer> catQuestionMap =
            new Hashtable<String, StringBuffer>();
        Hashtable<String, StringBuffer> catAnswerMap = new Hashtable<String,
                                                           StringBuffer>();
        List<String> cats = new ArrayList<String>();
        subGroups.addAll(entries);
        sb.append(
            "<style type=\"text/css\">.faq_question {margin:0px;margin-bottom:5px;}\n</style>");
        for (Entry entry : subGroups) {
            String cat = "General";
            if (entry.getType().equals(FaqEntryTypeHandler.TYPE_FAQENTRY)) {
                cat = (String) entry.getValue(0, cat);
                if (cat == null) {
                    cat = "General";
                }
            }
            StringBuffer catQuestionSB = catQuestionMap.get(cat);
            StringBuffer catAnswerSB   = catAnswerMap.get(cat);
            if (catQuestionSB == null) {
                catQuestionSB = new StringBuffer();
                catQuestionSB.append("<ol>");
                catQuestionMap.put(cat, catQuestionSB);
                catAnswerSB = new StringBuffer();
                catAnswerSB.append("<ol>");
                catAnswerMap.put(cat, catAnswerSB);
                cats.add(cat);
            }
            catQuestionSB.append("<li class=\"faq_question\">");
            boolean includeLink = group.getValue(0, true);
            if (canAdd || includeLink) {
                String link = HtmlUtils.href(
                                  request.entryUrl(
                                      getRepository().URL_ENTRY_SHOW,
                                      entry), HtmlUtils.img(
                                          getRepository().iconUrl(
                                              ICON_ENTRY), msg(
                                              "View entry details")));
                //            catQuestionSB.append(" ");
                catQuestionSB.append(link);
                catQuestionSB.append(" ");
            }
            catQuestionSB.append(HtmlUtils.href("#" + entry.getId(),
                    entry.getName()));

            catAnswerSB.append("<a name=" + entry.getId() + "></a>");
            catAnswerSB.append("<li class=\"faq_question\">");
            catAnswerSB.append(" ");
            catAnswerSB.append(HtmlUtils.b(entry.getName()));
            catAnswerSB.append(HtmlUtils.br());
            String desc = entry.getDescription();
            desc = desc.replaceAll("\r\n\r\n", "\n<p>\n");
            catAnswerSB.append(desc);
            catAnswerSB.append(HtmlUtils.p());
        }


        //sort the categories and put the blank one at the end
        cats = new ArrayList<String>(ucar.unidata.util.Misc.sort(cats));
        if(cats.contains("")) {
            cats.remove("");
            cats.add("");
        }
        for (String cat : cats) {
            StringBuffer catQuestionSB = catQuestionMap.get(cat);
            catQuestionSB.append("</ol>");
            if (cats.size() > 1) {
                sb.append(HtmlUtils.h2(cat));
            }
            sb.append(catQuestionSB.toString());
            //            sb.append(HtmlUtils.makeToggleTable("",
            //                                                 catQuestionSB.toString(), true));
        }


        for (String cat : cats) {
            StringBuffer catAnswerSB = catAnswerMap.get(cat);
            sb.append("<hr>");
            catAnswerSB.append("</ol>");
            if (cats.size() > 1) {
                sb.append(HtmlUtils.h2(cat));
            }

            sb.append(catAnswerSB.toString());

            /*
            sb.append(HtmlUtils.makeToggleTable("",
                                                 catAnswerSB.toString(), true));
            */
            //            sb.append(catAnswerSB);

        }

        return new Result(msg("FAQ"), sb);

    }




}
