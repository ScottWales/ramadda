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

package org.ramadda.plugins.multisearch;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.w3c.dom.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.StringUtil;

import java.util.List;
import java.util.ArrayList;


/**
 *
 *
 */
public class MultiSearchTypeHandler extends GenericTypeHandler {

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
        String query = request.getString(ARG_QUERY,"");
        sb.append(HtmlUtil.form(formUrl,""));
        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(msg("Search across multiple search engines"));
        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.input(ARG_QUERY, query, 40));
        sb.append(HtmlUtil.submit("Search"));
        sb.append(HtmlUtil.formClose());
        if(request.defined(ARG_QUERY)) {
            List<String> tabTitles = new ArrayList<String>();
            List<String> tabs = new ArrayList<String>();
            String[]urls = {"Google","http://www.google.com/search?hl=en&ie=ISO-8859-1&q=${query}&btnG=Search",
                            "Bing","http://www.bing.com/search?q=${query}&go=&form=QBLH&qs=n&sk=&sc=8-5",
                            "Yahoo", "http://search.yahoo.com/search?p=${query}&ei=UTF-8&fr=moz35",
                            "DuckDuckGo","http://duckduckgo.com/?q=${query}",
            };
            for(int i=0;i<urls.length;i+=2) {
                String title = urls[i];
                String url = urls[i+1];
                tabTitles.add(title);
                url = url.replace("${query}", query);
                StringBuffer tmp = new StringBuffer();
                tmp.append(HtmlUtil.href(url, "Go to " + title));
                tmp.append(HtmlUtil.tag(HtmlUtil.TAG_IFRAME,
                                       HtmlUtil.attr(HtmlUtil.ATTR_SRC, url) + 
                                       HtmlUtil.attr(HtmlUtil.ATTR_WIDTH,"100%") +
                                       HtmlUtil.attr(HtmlUtil.ATTR_HEIGHT,"300"),"Need frames"));
                tabs.add(tmp.toString());
            }
            sb.append(OutputHandler.makeTabs(tabTitles, tabs,true));
        }

        return new Result(msg("MultiSearch"), sb);
    }




}
