/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
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

package org.ramadda.repository.output;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.JpegMetadataHandler;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.metadata.MetadataHandler;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;



import java.io.*;

import java.io.File;



import java.net.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class MapOutputHandler extends OutputHandler {


    /** Map output type */
    public static final OutputType OUTPUT_MAP =
        new OutputType("Map", "map.map",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_MAP);

    /** GoogleEarth output type */
    public static final OutputType OUTPUT_GEMAP =
        new OutputType("Google Earth", "map.gemap",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_GOOGLEEARTH);


    /**
     * Create a MapOutputHandler
     *
     *
     * @param repository  the repository
     * @param element     the Element
     * @throws Exception  problem generating handler
     */
    public MapOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        if (getMapManager().showMaps()) {
            addType(OUTPUT_MAP);
            addType(OUTPUT_GEMAP);
        }
    }



    /**
     * Get the entry links
     *
     * @param request  the Request
     * @param state    the repository State
     * @param links    the links
     *
     * @throws Exception  problem creating links
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        boolean ok = false;
        for (Entry entry : state.getAllEntries()) {
            if (entry.hasLocationDefined() || entry.hasAreaDefined()) {
                ok = true;

                break;
            }
        }
        if (ok) {
            links.add(makeLink(request, state.getEntry(), OUTPUT_MAP));
            if (getMapManager().isGoogleEarthEnabled(request)) {
                links.add(makeLink(request, state.getEntry(), OUTPUT_GEMAP));
            }

        }
    }



    /**
     * Output the entry
     *
     * @param request      the Request
     * @param outputType   the type of output
     * @param entry        the Entry to output
     *
     * @return  the Result
     *
     * @throws Exception  problem outputting entry
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        List<Entry> entriesToUse = new ArrayList<Entry>();
        entriesToUse.add(entry);
        StringBuffer sb = new StringBuffer();

        if (outputType.equals(OUTPUT_GEMAP)) {
            getMapManager().getGoogleEarth(request, entriesToUse, sb, -1, -1,
                                           true, false);

            return makeLinksResult(request, msg("Google Earth"), sb,
                                   new State(entry));
        }

        MapInfo map = getMapManager().getMap(request, entriesToUse, sb, 700,
                                             500, true,
                                             new boolean[] { false }, false);

        return makeLinksResult(request, msg("Map"), sb, new State(entry));
    }



    /**
     * Output a group
     *
     * @param request      The Request
     * @param outputType   the type of output
     * @param group        the group Entry
     * @param subGroups    the subgroups
     * @param entries      The list of Entrys
     *
     * @return  the resule
     *
     * @throws Exception    problem on output
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        List<Entry> entriesToUse = new ArrayList<Entry>(subGroups);
        entriesToUse.addAll(entries);
        StringBuffer sb = new StringBuffer();
        if (entriesToUse.size() == 0) {
            sb.append(HtmlUtils.b(msg("No entries")) + HtmlUtils.p());

            return makeLinksResult(request, msg("Map"), sb,
                                   new State(group, subGroups, entries));
        }

        showNext(request, subGroups, entries, sb);
        if (outputType.equals(OUTPUT_GEMAP)) {
            getMapManager().getGoogleEarth(request, entriesToUse, sb, -1, -1,
                                           true, false);

            return makeLinksResult(request, msg("Google Earth"), sb,
                                   new State(group));
        }


        boolean[] haveBearingLines = { false };
        MapInfo   map = getMapManager().getMap(request, entriesToUse, sb, 700,
                                             500, false, haveBearingLines,
                                             true);

        return makeLinksResult(request, msg("Map"), sb,
                               new State(group, subGroups, entries));
    }




}
