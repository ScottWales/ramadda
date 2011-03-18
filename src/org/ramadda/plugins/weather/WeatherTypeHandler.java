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

package org.ramadda.plugins.weather;


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
public class WeatherTypeHandler extends GenericTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public WeatherTypeHandler(Repository repository, Element entryNode)
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
        String string = entry.getValue(0,"");
        String orientation = entry.getValue(3,"horizontal");

        String template = getRepository().getResource("/org/ramadda/plugins/weather/template.html");

        if(orientation.equals("horizontal")) {
            sb.append("<table><tr valign=top>");
        }
        for(String tok: StringUtil.split(string,"\n",true,true)) {
            String html = template;
            if(orientation.equals("horizontal")) {
                sb.append("<td>");
            }

            html = html.replace("${zipcode}", tok);
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
        return new Result(msg("Weather"), sb);
    }




}
