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

package org.ramadda.plugins.beforeafter;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;


import ucar.unidata.sql.Clause;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.HttpServer;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WikiUtil;
import ucar.unidata.xml.XmlUtil;

import java.awt.Dimension;


import java.awt.Image;




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
public class BeforeAfterTypeHandler extends GenericTypeHandler {

    /** _more_          */
    private Hashtable<String, Dimension> dimensions = new Hashtable<String,
                                                          Dimension>();


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public BeforeAfterTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isGroup() {
        return true;
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
        if ( !isDefaultHtmlOutput(request)) {
            return null;
        }
        List<Entry> children = getEntryManager().getChildren(request, entry);
        return getHtmlDisplay(request, entry, new ArrayList<Entry>(),
                              children);
    }

    /** _more_          */
    private static int cnt = 0;


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
        StringBuffer sb   = new StringBuffer();
        StringBuffer divs = new StringBuffer();
        int          col  = 1;
        sb.append(
            HtmlUtil.importJS(
                getRepository().getUrlBase()
                + "/beforeandafter/jquery.beforeafter.js"));
        String template =
            getRepository().getResource(
                "/org/ramadda/plugins/beforeafter/template.html");
        //      sb.append(template);

        List<Entry> entriesToUse = new ArrayList<Entry>();
        for (Entry child : entries) {
            if ( !child.getResource().isImage()) {
                continue;
            }
            entriesToUse.add(child);
        }

        for (int i = 0; i < entriesToUse.size(); i += 2) {
            if (i >= entriesToUse.size() - 1) {
                break;
            }
            Entry     entry1 = entriesToUse.get(i);
            Entry     entry2 = entriesToUse.get(i + 1);
            Dimension dim    = dimensions.get(entry1.getId());

            if (dim == null) {
                Image image =
                    ImageUtils.readImage(entry1.getResource().getPath());
                ImageUtils.waitOnImage(image);
                dim = new Dimension(image.getWidth(null),
                                    image.getHeight(null));
                if ((dim.width > 0) && (dim.height > 0)) {
                    dimensions.put(entry1.getId(), dim);
                }
                System.err.println(dim);
            }

            int width  = 600;
            int height = 366;

            if ((dim.width > 0) && (dim.height > 0)) {
                width  = dim.width;
                height = dim.height;
            }


            if (entry1.getCreateDate() > entry2.getCreateDate()) {
                Entry tmp = entry1;
                entry1 = entry2;
                entry2 = tmp;
            }
            String id = "bandacontainer" + (cnt++);
            divs.append("<div id=\"" + id + "\">\n");
            String url1 = HtmlUtil.url(
                              request.url(repository.URL_ENTRY_GET) + "/"
                              + getStorageManager().getFileTail(
                                  entry1), ARG_ENTRYID, entry1.getId());
            String url2 = HtmlUtil.url(
                              request.url(repository.URL_ENTRY_GET) + "/"
                              + getStorageManager().getFileTail(
                                  entry2), ARG_ENTRYID, entry2.getId());

            divs.append(
                HtmlUtil.div(
                    "<img src=\"" + url1 + "\""
                    + HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "" + width)
                    + HtmlUtil.attr(HtmlUtil.ATTR_HEIGHT, "" + height)
                    + ">", ""));

            divs.append(
                HtmlUtil.div(
                    "<img src=\"" + url2 + "\""
                    + HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "" + width)
                    + HtmlUtil.attr(HtmlUtil.ATTR_HEIGHT, "" + height)
                    + ">", ""));
            divs.append("</div>\n");
            String path = getRepository().getUrlBase() + "/beforeandafter/";
            String args = "{imagePath:'" + path + "'}";
            sb.append("\n");
            sb.append(HtmlUtil.script("\n$(function(){$('#" + id
                                      + "').beforeAfter(" + args
                                      + ");});\n"));
        }
        sb.append("\n");
        sb.append(divs);
        return new Result(msg("Before/After Image"), sb);
    }




}
