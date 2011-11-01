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

package org.ramadda.plugins.blog;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;


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

    /** _more_ */
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


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public String getEntryText(Entry entry) {
        Object[] values = entry.getValues();
        if ((values != null) && (values.length > 0) && (values[0] != null)) {
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
        /*
        String js =
            "<script type=\"text/javascript\" src=\"/repository/tiny_mce/tiny_mce.js\"></script><script type=\"text/javascript\">  tinyMCE.init({          mode : \"textareas\",           theme : \"simple\"      });</script>";

            sb.append(js);
        */
        super.addToEntryForm(request, sb, entry);
    }








}
