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

package org.ramadda.geodata.idv;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.w3c.dom.*;

import ucar.unidata.xml.XmlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import org.ramadda.util.HtmlUtils;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import     ucar.unidata.gis.shapefile.EsriShapefile;

/**
 * A class to output the IDV bundles XML
 */
public class IdvBundlesOutputHandler extends OutputHandler {

    private static final String ATTR_CATEGORY = "category";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_URL = "url";
    private static final String TAG_BUNDLES = "bundles";
    private static final String TAG_BUNDLE = "bundle";

    /** Map output type */
    public static final OutputType OUTPUT_BUNDLES =
        new OutputType("IDV Bundles XML", "idv.bundles",
                       OutputType.TYPE_FEEDS, "",
                       "/idv/idv.gif");



    /**
     * Create a IdvBundlesOutputHandler
     *
     *
     * @param repository  the repository
     * @param element     the Element
     * @throws Exception  problem generating handler
     */
    public IdvBundlesOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_BUNDLES);
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
        for (Entry entry : state.getAllEntries()) {
            if (entry.hasLocationDefined() || entry.hasAreaDefined()) {
                links.add(makeLink(request, state.getEntry(), OUTPUT_BUNDLES));
                break;
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
        return outputBundlesXml(request, entry, entriesToUse);
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

        return outputBundlesXml(request, group, entriesToUse);
    }

    public Result outputBundlesXml(Request request, Entry entry,
                            List<Entry> entries)
            throws Exception {
        StringBuffer sb  = new StringBuffer();
        sb.append(XmlUtil.openTag(TAG_BUNDLES));

        for (Entry child : entries) {
            if (!isBundle(child)) {
                continue;
            }
            String name = child.getName();
            String group = "foo";
            sb.append(XmlUtil.tag(TAG_BUNDLE, 
                    XmlUtil.attrs(
                       new String[]{ATTR_CATEGORY, group, 
                                    ATTR_NAME, name, 
                                    ATTR_URL, child.getTypeHandler().getEntryResourceUrl(request, child)})));
        }
        sb.append(XmlUtil.closeTag(TAG_BUNDLES));

        Result result = new Result("bundles", sb, "text/xml");
        result.setReturnFilename(IOUtil.stripExtension(entry.getName())+".bundles.xml");
        return result;
    }

    private boolean isBundle(Entry entry) {
        return (entry.getResource().getPath().endsWith(".xidv")
                || entry.getResource().getPath().endsWith(".zidv"));
    }
}
