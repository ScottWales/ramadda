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

package org.ramadda.plugins.community;


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
public class UserTypeHandler extends ExtensibleGroupTypeHandler {


    /** _more_ */
    public static String TYPE_USER = "users_user";

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public UserTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    public boolean shouldShowInHtml(Request requst, Entry entry, OutputType output) {
        return output.equals(OutputHandler.OUTPUT_HTML) || output.equals(HtmlOutputHandler.OUTPUT_INLINE);
    }


    public void handleNoEntriesHtml(Request request, Entry entry, StringBuffer sb) {
        //noop
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
        if(true) return null;
        StringBuffer sb = new StringBuffer();

        boolean canAdd = getAccessManager().canDoAction(request, group,
                             Permission.ACTION_NEW);

        if (canAdd) {
            /*
            sb.append(HtmlUtil
                .href(HtmlUtil
                    .url(request
                        .entryUrl(getRepository().URL_ENTRY_FORM, group,
                            ARG_GROUP), ARG_TYPE,
                         FaqEntryTypeHandler.TYPE_FAQENTRY), HtmlUtil
                      .img(getRepository().iconUrl(ICON_NEW),
                           msg("New Note"))));
            */
        }

        subGroups.addAll(entries);

        for(Entry entry: subGroups) {
            String link = 
                HtmlUtil.href(request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                               entry), HtmlUtil.img(getRepository().iconUrl(ICON_ENTRY),msg("View entry details")));
            sb.append("<a name=" + entry.getId() +"></a>");
            sb.append("<li>");
            sb.append(" ");
            sb.append(HtmlUtil.b(entry.getName()));
            sb.append(HtmlUtil.br());
            sb.append(entry.getDescription());
        }


        return new Result(msg("Missing Person"), sb);
    }




}
