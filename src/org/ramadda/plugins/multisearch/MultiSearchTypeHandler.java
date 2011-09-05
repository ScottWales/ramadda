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

package org.ramadda.plugins.multisearch;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.w3c.dom.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.StringUtil;

import java.util.ArrayList;

import java.util.List;


/**
 *
 *
 */
public class MultiSearchTypeHandler extends GenericTypeHandler {

    /** _more_          */
    public static String ARG_QUERY = "query";

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public MultiSearchTypeHandler(Repository repository, Element entryNode)
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
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        String formUrl = request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                          entry);
        String query = request.getString(ARG_QUERY, "");
        sb.append(HtmlUtil.form(formUrl, ""));
        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(msg("Search across multiple search engines"));
        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.input(ARG_QUERY, query, 40));
        sb.append(HtmlUtil.submit("Search"));
        sb.append(HtmlUtil.formClose());
        if (request.defined(ARG_QUERY)) {
            List<String> tabTitles = new ArrayList<String>();
            List<String> tabs      = new ArrayList<String>();
            String[]     urls      = {
                "Google",
                "http://www.google.com/search?hl=en&ie=ISO-8859-1&q=${query}&btnG=Search",
                "Bing",
                "http://www.bing.com/search?q=${query}&go=&form=QBLH&qs=n&sk=&sc=8-5",
                "Yahoo",
                "http://search.yahoo.com/search?p=${query}&ei=UTF-8&fr=moz35",
                "DuckDuckGo", "http://duckduckgo.com/?q=${query}",
            };
            for (int i = 0; i < urls.length; i += 2) {
                String title = urls[i];
                String url   = urls[i + 1];
                tabTitles.add(title);
                url = url.replace("${query}", query);
                StringBuffer tmp = new StringBuffer();
                tmp.append(HtmlUtil.href(url, "Go to " + title));
                tmp.append(
                    HtmlUtil.tag(
                        HtmlUtil.TAG_IFRAME,
                        HtmlUtil.attr(HtmlUtil.ATTR_SRC, url)
                        + HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "100%")
                        + HtmlUtil.attr(
                            HtmlUtil.ATTR_HEIGHT, "300"), "Need frames"));
                tabs.add(tmp.toString());
            }
            sb.append(OutputHandler.makeTabs(tabTitles, tabs, true));
        }

        return new Result(msg("MultiSearch"), sb);
    }




}
