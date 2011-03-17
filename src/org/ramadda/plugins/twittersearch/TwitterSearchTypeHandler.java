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

package org.ramadda.plugins.twittersearch;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.type.*;


import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.StringUtil;

import java.util.Date;
import java.util.List;


/**
 *
 *
 */
public class TwitterSearchTypeHandler extends GenericTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public TwitterSearchTypeHandler(Repository repository, Element entryNode)
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
        String template = getRepository().getResource("/org/ramadda/plugins/twittersearch/template.html");
        String string = entry.getValue(0,"");
        String width = entry.getValue(1,"350");
        String height = entry.getValue(2,"300");
        String orientation = entry.getValue(3,"vertical");

        String html = template;

        if(orientation.equals("horizontal")) {
            sb.append("<table><tr valign=top>");
        }
        for(String tok: StringUtil.split(string,"\n",true,true)) {
            html = html.replace("${string}", tok);
            html = html.replace("${title}", entry.getName());
            html = html.replace("${caption}", entry.getDescription());
            html = html.replace("${width}", width);
            html = html.replace("${height}", height);
            if(orientation.equals("horizontal")) {
                sb.append("<td>");
            }

            sb.append(HtmlUtil.href("http://twitter.com/#search?q=" + HtmlUtil.urlEncode(tok),tok));
            sb.append(HtmlUtil.br());
            sb.append(html);
            if(orientation.equals("horizontal")) {
                sb.append("</td>");
            } else {
                sb.append(HtmlUtil.p());
            }
        }
        if(orientation.equals("horizontal")) {
            sb.append("</table>");
        }
        return new Result(msg("Twitter Search"), sb);
    }




}
