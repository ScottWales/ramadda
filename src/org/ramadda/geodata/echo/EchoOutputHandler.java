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

package org.ramadda.geodata.echo;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;

import org.w3c.dom.*;


import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;






/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class EchoOutputHandler extends OutputHandler {



    /** _more_ */
    public static final OutputType OUTPUT_ECHO_XML =
        new OutputType("Echo-XML", "echo.xml",
                       OutputType.TYPE_FEEDS | OutputType.TYPE_FORSEARCH, "",
                       "/echo/nasa.png");


    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public EchoOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_ECHO_XML);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if (state.isDummyGroup()) {
            return;
        }
        if (state.group != null) {
            links.add(makeLink(request, state.group, OUTPUT_ECHO_XML,
                               "/"
                               + IOUtil.stripExtension(state.group.getName())
                               + ".echo.zip"));
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        OutputStream os = request.getHttpServletResponse().getOutputStream();
        request.getHttpServletResponse().setContentType("application/zip");
        List<Entry> collections = new ArrayList<Entry>();
        collections.add(group);
        EchoPublisher publisher = new EchoPublisher(getRepository(), "dummy");
        publisher.writeCollections(collections, os, true);
        Result result = new Result();
        result.setNeedToWrite(false);
        return result;
    }


}
