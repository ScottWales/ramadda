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
import ucar.unidata.ui.ImageUtils;


import java.awt.Image;




import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import java.awt.Dimension;


/**
 *
 *
 */
public class BeforeAfterTypeHandler extends GenericTypeHandler {

    private Hashtable<String,Dimension> dimensions = new Hashtable<String,Dimension>();


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

    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {
        List<Entry> children =  getEntryManager().getChildren(request, entry);
        return getHtmlDisplay(request, entry, new ArrayList<Entry>(), children);
    }

    private static 	int cnt = 0;


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
	sb.append(HtmlUtil.importJS(getRepository().getUrlBase()+"/beforeandafter/jquery.beforeafter.js"));
        String template = getRepository().getResource("/org/ramadda/plugins/beforeafter/template.html");
	//	sb.append(template);

        List<Entry> entriesToUse = new ArrayList<Entry>();
	for (Entry child : entries) {
	    if ( !child.getResource().isImage()) {
		continue;
	    }
            entriesToUse.add(child);
        }

        for(int i=0;i<entriesToUse.size();i+=2) {
            if(i>=entriesToUse.size()-1) break;
            Entry entry1 = entriesToUse.get(i);
            Entry entry2 = entriesToUse.get(i+1);
            Dimension dim =  dimensions.get(entry1.getId());

            if(dim == null) {
               Image image =
                   ImageUtils.readImage(entry1.getResource().getPath());
               ImageUtils.waitOnImage(image);
               dim = new Dimension(image.getWidth(null),image.getHeight(null));
               if(dim.width>0 && dim.height>0) {
                   dimensions.put(entry1.getId(), dim);
               }
               System.err.println(dim);
            }

            int width =  600;
            int height =  366;

            if(dim.width>0 && dim.height>0) {
                width = dim.width;
                height = dim.height;
            }


            if(entry1.getCreateDate()>entry2.getCreateDate()) {
                Entry tmp = entry1;
                entry1 = entry2;
                entry2 = tmp;
            }
	    String id = "bandacontainer" + (cnt++);
            divs.append("<div id=\"" +  id +"\">\n");
	    String url1 = HtmlUtil.url(
				      request.url(repository.URL_ENTRY_GET) + "/"
				      + getStorageManager().getFileTail(
									entry1), ARG_ENTRYID, entry1.getId());
	    String url2 = HtmlUtil.url(
				      request.url(repository.URL_ENTRY_GET) + "/"
				      + getStorageManager().getFileTail(
									entry2), ARG_ENTRYID, entry2.getId());

	    divs.append(HtmlUtil.div("<img src=\"" + url1  +"\"" + 
				     HtmlUtil.attr(HtmlUtil.ATTR_WIDTH,""+width)+
				     HtmlUtil.attr(HtmlUtil.ATTR_HEIGHT,""+height) +">",""));

	    divs.append(HtmlUtil.div("<img src=\"" + url2  +"\"" + 
				     HtmlUtil.attr(HtmlUtil.ATTR_WIDTH,""+width)+
				     HtmlUtil.attr(HtmlUtil.ATTR_HEIGHT,""+height) +">",""));
            divs.append("</div>\n");
            String path = getRepository().getUrlBase() +"/beforeandafter/";
            String args = "{imagePath:'" + path +"'}";
            sb.append("\n");
            sb.append(HtmlUtil.script("\n$(function(){$('#" +  id +"').beforeAfter(" + args +");});\n"));
	}
	sb.append("\n");
	sb.append(divs);
        return new Result(msg("Before/After Image"), sb);
    }




}
