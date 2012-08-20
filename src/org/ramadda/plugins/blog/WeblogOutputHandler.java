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

package org.ramadda.plugins.blog;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WmsUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.io.File;


import java.net.*;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class WeblogOutputHandler extends OutputHandler {

    /** _more_ */
    private SimpleDateFormat sdf;


    /** _more_ */
    public static final OutputType OUTPUT_BLOG = new OutputType("Weblog",
                                                     "blog",
                                                     OutputType.TYPE_VIEW,
                                                     "", "blog.image");


    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public WeblogOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_BLOG);
        sdf = new SimpleDateFormat("MMMMM d, yyyy");
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if (true) {
            return;
        }
        if (state.group == null) {
            return;
        }
        List<Entry> entries = state.getAllEntries();
        if (entries.size() == 0) {
            return;
        }
        boolean ok = false;
        for (Entry entry : entries) {
            if (entry.getType().equals("blogentry")) {
                ok = true;

                break;
            }
        }
        if ( !ok) {
            return;
        }
        links.add(makeLink(request, state.getEntry(), OUTPUT_BLOG));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        boolean canAdd = getAccessManager().canDoAction(request, group,
                             Permission.ACTION_NEW);

        StringBuffer sb = new StringBuffer();
        sb.append(HtmlUtils.cssLink(getRepository().getUrlBase()
                                    + "/blog/blogstyle.css"));
        if (canAdd && !request.get(ARG_EMBEDDED, false)) {
            sb.append(
                HtmlUtils
                    .href(HtmlUtils
                        .url(request
                            .entryUrl(
                                getRepository().URL_ENTRY_FORM, group,
                                    ARG_GROUP), ARG_TYPE,
                                        BlogEntryTypeHandler
                                            .TYPE_BLOGENTRY), HtmlUtils
                                                .img(getRepository()
                                                    .iconUrl(ICON_NEW), msg(
                                                        "New Weblog Entry"))));
        }

        StringBuffer blogEntries = new StringBuffer();
        for (Entry entry : entries) {
            if ( !entry.getType().equals("blogentry")) {
                continue;
            }
            String blogEntry = getBlogEntry(request, entry, false);
            blogEntries.append(
                HtmlUtils.div(blogEntry, HtmlUtils.cssClass("blogentry")));
        }
        sb.append(HtmlUtils.div(blogEntries.toString(),
                                HtmlUtils.cssClass("blogentries")));

        return new Result("", sb);
    }

    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    private String formatDate(Date date) {
        synchronized (sdf) {
            return sdf.format(date);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param single _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getBlogEntry(Request request, Entry entry, boolean single)
            throws Exception {
        StringBuffer blogEntry = new StringBuffer();
        String entryUrl = request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                           entry);
        String subject;
        if (single) {
            subject = HtmlUtils.div(entry.getLabel(),
                                    HtmlUtils.cssClass("blogsubject"));
        } else {
            subject = HtmlUtils.div(
                HtmlUtils.href(
                    entryUrl, entry.getLabel(), HtmlUtils.cssClass(
                        "blogsubject")), HtmlUtils.cssClass("blogsubject"));
        }
        String postingInfo =
            HtmlUtils.div(
                "by" + " " + entry.getUser().getName() + " @ "
                + formatDate(
                    new Date(entry.getStartDate())), HtmlUtils.cssClass(
                    "blogdate"));

        String header = HtmlUtils.leftRightBottom(subject, postingInfo, "");
        blogEntry.append(HtmlUtils.div(header,
                                       HtmlUtils.cssClass("blogheader")));
        String desc = entry.getDescription();
        if (desc.startsWith("<p>")) {
            desc = desc.substring(3);
            if (desc.endsWith("</p>")) {
                desc = desc.substring(0, desc.length() - 4);
            }
        }
        desc = getWikiManager().wikifyEntry(request, entry, desc);


        StringBuffer blogBody = new StringBuffer(desc);
        Object[]     values   = entry.getValues();
        if (values[0] != null) {
            String extra = ((String) values[0]).trim();
            if (extra.length() > 0) {
                extra = getWikiManager().wikifyEntry(request, entry, extra);
                blogBody.append(HtmlUtils.makeShowHideBlock(msg("More..."),
                        extra, false));
            }
        }
        StringBuffer comments = getCommentBlock(request, entry, false);
        String commentsBlock  = HtmlUtils.makeShowHideBlock(msg("Comments"),
                                   HtmlUtils.insetDiv(comments.toString(), 0,
                                       30, 0, 0), false);

        blogBody.append(commentsBlock);
        blogEntry.append(HtmlUtils.div(blogBody.toString(),
                                       HtmlUtils.cssClass("blogbody")));

        return blogEntry.toString();
    }

}
