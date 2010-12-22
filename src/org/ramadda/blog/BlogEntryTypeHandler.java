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
import ucar.unidata.repository.metadata.*;
import ucar.unidata.repository.output.OutputHandler;
import ucar.unidata.repository.type.*;


import ucar.unidata.sql.Clause;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;

import ucar.unidata.util.HtmlUtil;
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
public class BlogEntryTypeHandler extends GenericTypeHandler {


    /** _more_ */
    public static String TYPE_BLOGENTRY = "blogentry";

    /** _more_ */
    public static final String ARG_BLOG_TEXT = "blogentry.blogtext";

    /** _more_          */
    private WeblogOutputHandler weblogOutputHandler;

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public BlogEntryTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    public String getEntryText(Entry entry) {
        Object[]     values   = entry.getValues();
        if (values!=null && values.length>0 && values[0] != null) {
            String extra = ((String) values[0]).trim();
            return entry.getDescription() + extra;
        }
        return entry.getDescription();
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
    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {
        if (weblogOutputHandler == null) {
            weblogOutputHandler =
                (WeblogOutputHandler) getRepository().getOutputHandler(
                    WeblogOutputHandler.OUTPUT_BLOG);
        }
        StringBuffer sb = new StringBuffer();
        sb.append(
            HtmlUtil.cssLink(getRepository().fileUrl("/blog/blogstyle.css")));
        sb.append(weblogOutputHandler.getBlogEntry(request, entry));
        return new Result("", sb);
    }


    /**
     * add the tinymce javascript to add html editing to text areas
     *
     * @param request _more_
     * @param sb _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void addToEntryForm(Request request, StringBuffer sb, Entry entry)
            throws Exception {
        String js =
            "<script type=\"text/javascript\" src=\"/repository/blog/tiny_mce/tiny_mce.js\"></script><script type=\"text/javascript\">  tinyMCE.init({          mode : \"textareas\",           theme : \"simple\"      });</script>";

        sb.append(js);
        super.addToEntryForm(request, sb, entry);
    }








}
