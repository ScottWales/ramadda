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

package org.ramadda.plugins.faq;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;


import ucar.unidata.sql.Clause;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;

import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.HttpServer;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WikiUtil;
import ucar.unidata.xml.XmlUtil;

import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.Statement;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 *
 *
 */
public class FaqTypeHandler extends ExtensibleGroupTypeHandler {


    /** _more_ */
    public static String TYPE_FAQ = "faq";

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public FaqTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlDisplay(Request request, Entry group,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {

        if ( !isDefaultHtmlOutput(request)) {
            return null;
        }

        StringBuffer sb = new StringBuffer();

        boolean canAdd = getAccessManager().canDoAction(request, group,
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
            String link = HtmlUtils.href(
                              request.entryUrl(
                                  getRepository().URL_ENTRY_SHOW,
                                  entry), HtmlUtils.img(
                                      getRepository().iconUrl(ICON_ENTRY),
                                      msg("View entry details")));
            //            catQuestionSB.append(" ");
            catQuestionSB.append(link);

            catQuestionSB.append(" ");
            catQuestionSB.append(HtmlUtils.href("#" + entry.getId(),
                    entry.getName()));

            catAnswerSB.append("<a name=" + entry.getId() + "></a>");
            catAnswerSB.append("<li class=\"faq_question\">");
            catAnswerSB.append(" ");
            catAnswerSB.append(HtmlUtils.b(entry.getName()));
            catAnswerSB.append(HtmlUtils.br());
            catAnswerSB.append(entry.getDescription());
            catAnswerSB.append(HtmlUtils.p());
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
