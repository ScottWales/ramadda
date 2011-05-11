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

package org.ramadda.plugins.trip;


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
public class TripTypeHandler extends ExtensibleGroupTypeHandler {

    /** _more_ */
    private CalendarOutputHandler calendarOutputHandler;

    public static final String TYPE_HOTEL = "trip_hotel";
    public static final String TYPE_FLIGHT = "trip_flight";
    public static final String TYPE_TRAIN = "trip_train";


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public TripTypeHandler(Repository repository, Element entryNode)
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
    public Result getHtmlDisplay(Request request, Entry group,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        if(request.exists(ARG_OUTPUT)) return null;
        if (calendarOutputHandler == null) {
            calendarOutputHandler =
                (CalendarOutputHandler) getRepository().getOutputHandler(
                                                                         CalendarOutputHandler.OUTPUT_CALENDAR);
        }

        StringBuffer sb = new StringBuffer();
        appendHeader(request, group, sb);

        subGroups.addAll(entries);
        return calendarOutputHandler.outputCalendar(request,  group,
                                                    subGroups, sb);
    }


    public void appendHeader(Request request, Entry group, StringBuffer sb) throws Exception {
        boolean canAdd = getAccessManager().canDoAction(request, group,
                             Permission.ACTION_NEW);

        if (canAdd) {
            sb.append("<b>New:</b> ");
            String[]types = {TYPE_HOTEL,
                             TYPE_FLIGHT,
                             TYPE_TRAIN};
            String[]names = {"New Hotel Reservation",
                             "New Flight",
                             "New Train",};
            String[]icons = {"/trip/hotel.png",
                             "/trip/plane.png",
                             "/trip/train.gif",};
            for(int i=0;i<types.length;i++) {
                if(i>0)             sb.append("&nbsp;|&nbsp;");
                sb.append(HtmlUtil
                          .href(HtmlUtil
                                .url(request
                                     .entryUrl(getRepository().URL_ENTRY_FORM, group,
                                               ARG_GROUP), ARG_TYPE,
                                     types[i]), HtmlUtil
                                .img(getRepository().iconUrl(icons[i]),
                                     msg(names[i]))));
            }

            sb.append("<p>");
        }
    }

}
