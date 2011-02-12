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

package org.ramadda.geodata.data;


import org.w3c.dom.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.HttpServer;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.lang.reflect.*;


import java.net.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;


/**
 * Class SqlUtil _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GridAggregationHarvester extends PatternHarvester {
    
    public static final String ATTR_AGGREGATIONTYPE = "aggregationtype";


    private String aggregationType = GridAggregationTypeHandler.TYPE_JOINEXISTING;

    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public GridAggregationHarvester(Repository repository, String id)
            throws Exception {
        super(repository, id);
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     *
     * @throws Exception _more_
     */
    public GridAggregationHarvester(Repository repository, Element element)
            throws Exception {
        super(repository, element);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getLastGroupType() {
        return GridAggregationTypeHandler.TYPE_GRIDAGGREGATION;
    }

    protected void init(Element element) throws Exception {
        super.init(element);

        aggregationType = XmlUtil.getAttribute(element, ATTR_AGGREGATIONTYPE,
                                               aggregationType);
    }

    public void applyState(Element element) throws Exception {
        super.applyState(element);
        element.setAttribute(ATTR_AGGREGATIONTYPE,aggregationType);
    }

    public void applyEditForm(Request request) throws Exception {
        super.applyEditForm(request);
        aggregationType = request.getString(ATTR_AGGREGATIONTYPE,aggregationType);
    }

    public void createEditForm(Request request, StringBuffer sb)
            throws Exception {
        super.createEditForm(request, sb);
        List<String> types = (List<String>)Misc.newList(GridAggregationTypeHandler.TYPE_JOINEXISTING, GridAggregationTypeHandler.TYPE_UNION);
        sb.append(HtmlUtil.formEntry(msgLabel("Aggregation type"),
                                     HtmlUtil.select(ATTR_AGGREGATIONTYPE, types, aggregationType)));
    }


    public void initEntry(Entry entry) {
        super.initEntry(entry);
        if(entry.getType().equals(GridAggregationTypeHandler.TYPE_GRIDAGGREGATION)) {
            entry.setValues(new Object[]{null,aggregationType});
        }

    }
    


    /**
     * _more_
     *
     * @param fileInfo _more_
     * @param originalFile _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public Entry initializeNewEntry(FileInfo fileInfo, File originalFile,
                                    Entry entry) {
        try {
            /*
    Object[] values = entry.getValues();
    if(values==null) values = new Object[2];
    values[1] = contents;*/
            return entry;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return "Grid Aggregation";
    }




}
