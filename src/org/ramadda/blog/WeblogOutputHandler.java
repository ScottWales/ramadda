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

package org.ramadda.blog;


import org.w3c.dom.*;

import ucar.unidata.repository.*;
import ucar.unidata.repository.auth.*;
import ucar.unidata.repository.output.*;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
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

    /** _more_          */
    private SimpleDateFormat sdf;


    /** _more_ */
    public static final OutputType OUTPUT_BLOG = new OutputType("Weblog",
                                                     "blog",
                                                     OutputType.TYPE_HTML,
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
                              Group group, List<Group> subGroups,
                              List<Entry> entries)
            throws Exception {
        boolean canAdd = getAccessManager().canDoAction(request, group,
                             Permission.ACTION_NEW);

        StringBuffer sb = new StringBuffer();
        sb.append(HtmlUtil.cssLink(getRepository().getUrlBase()
                                   + "/blog/blogstyle.css"));
        if (canAdd) {
            sb.append(HtmlUtil
                .href(HtmlUtil
                    .url(request
                        .entryUrl(getRepository().URL_ENTRY_FORM, group,
                            ARG_GROUP), ARG_TYPE,
                                BlogEntryTypeHandler.TYPE_BLOGENTRY), HtmlUtil
                                    .img(getRepository().iconUrl(ICON_NEW),
                                        msg("New Weblog Entry"))));
        }

        StringBuffer blogEntries = new StringBuffer();
        for (Entry entry : entries) {
            if ( !entry.getType().equals("blogentry")) {
                continue;
            }
            String blogEntry = getBlogEntry(request, entry);
            blogEntries.append(HtmlUtil.div(blogEntry,
                                            HtmlUtil.cssClass("blogentry")));
        }
        sb.append(HtmlUtil.div(blogEntries.toString(),
                               HtmlUtil.cssClass("blogentries")));

        return new Result("", sb);
    }

    private String formatDate(Date date) {
        synchronized(sdf) {
            return sdf.format(date);
        }
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
    public String getBlogEntry(Request request, Entry entry)
            throws Exception {
        StringBuffer blogEntry = new StringBuffer();
        EntryLink link = getEntryManager().getAjaxLink(request, entry,
                             entry.getName(), null, false);
        String subject = HtmlUtil.div(link.getLink(),
                                      HtmlUtil.cssClass("blogsubject"));
        String postingInfo =
            HtmlUtil.div("by" + " " + entry.getUser().getName() +" @ " +
                         formatDate(new Date(entry.getStartDate())),
                         HtmlUtil.cssClass("blogdate"));
        String header = HtmlUtil.leftRight(subject, postingInfo);
        blogEntry.append(HtmlUtil.div(header,
                                      HtmlUtil.cssClass("blogheader")));
        String desc = entry.getDescription();
        if (desc.startsWith("<p>")) {
            desc = desc.substring(3);
            if (desc.endsWith("</p>")) {
                desc = desc.substring(0, desc.length() - 4);
            }
        }
        StringBuffer blogBody = new StringBuffer(desc);
        Object[]     values   = entry.getValues();
        if (values[0] != null) {
            String extra = ((String) values[0]).trim();
            if (extra.length() > 0) {
                blogBody.append(HtmlUtil.makeShowHideBlock(msg("More..."),
                        extra, false));
            }
        }
        StringBuffer comments = getCommentBlock(request, entry, false);
        String commentsBlock = HtmlUtil.makeShowHideBlock(msg("Comments"),
                                                          HtmlUtil.insetDiv(comments.toString(), 0,30,0,0), false);

        blogBody.append(commentsBlock);
        blogEntry.append(HtmlUtil.div(HtmlUtil.makeToggleTable("",
                blogBody.toString(), true), HtmlUtil.cssClass("blogbody")));

        return blogEntry.toString();
    }

}
