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

package org.ramadda.plugins.beforeafter;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;


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

    public boolean isGroup() {
        return true;
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
        StringBuffer sb = new StringBuffer();
	StringBuffer divs = new StringBuffer();
	int col =1;
	int cnt = 0;
	sb.append(HtmlUtil.importJS("/beforeandafter/jquery.beforeafter.js"));
        String template = getRepository().getResource("/org/ramadda/plugins/beforeafter/template.html");
	//	sb.append(template);

	for (Entry child : entries) {
	    if ( !child.getResource().isImage()) {
		continue;
	    }
	    String id = "container" + cnt;
	    divs.append("\n");
	    if(col==1) {
		divs.append("<div id=\"" +  id +"\">\n");
	    }
	    String url = HtmlUtil.url(
				      request.url(repository.URL_ENTRY_GET) + "/"
				      + getStorageManager().getFileTail(
									child), ARG_ENTRYID, child.getId());

	    divs.append(HtmlUtil.div("<img src=\"" + url  +"\"" + 
				     HtmlUtil.attr(HtmlUtil.ATTR_WIDTH,"600")+
				     HtmlUtil.attr(HtmlUtil.ATTR_HEIGHT,"366") +">",""));
	    divs.append("\n");
	    if(col==2) {
		divs.append("</div>\n");
		String path = getRepository().getUrlBase() +"/beforeandafter/";
		String args = "{imagePath:'" + path +"'}";
		divs.append("\n");
		sb.append("\n");
		sb.append(HtmlUtil.script("\n$(function(){$('#container" + (cnt-1) +"').beforeAfter(" + args +");});\n"));
		col=1;
	    }
	    col++;
	    cnt++;
	}
	sb.append("\n");
	sb.append(divs);
        return new Result(msg("Before/After Image"), sb);
    }




}
