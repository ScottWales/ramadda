/*
* Copyright 2008-2013 Geode Systems LLC
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
import org.ramadda.repository.type.*;

import org.ramadda.sql.SqlUtil;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;



import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.io.File;



import java.net.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;


import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class TemplateOutputHandler extends OutputHandler {

    /** _more_ */
    public static final String ATTR_ICON = "icon";

    /** _more_ */
    public static final String ATTR_ID = "id";

    /** _more_ */
    public static final String ATTR_EMBED = "embed";

    /** _more_ */
    public static final String ATTR_NAME = "name";

    /** _more_ */
    public static final String ATTR_TYPES = "types";

    /** _more_ */
    public static final String TAG_WIKI = "wiki";

    /** _more_ */
    public static final String TAG_WIKI_FOLDER = "wiki.folder";

    /** _more_ */
    public static final String TAG_WIKI_FILE = "wiki.file";

    /** _more_ */
    private boolean forGroups = true;

    /** _more_ */
    private boolean forFiles = true;

    /** _more_ */
    private List<String> types;

    /** _more_ */
    private String folderWikiTemplate;

    /** _more_ */
    private String fileWikiTemplate;


    /** _more_ */
    private OutputType outputType;

    /** _more_ */
    private boolean embed;


    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public TemplateOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        init(element);
    }


    /**
     * _more_
     *
     * @param element _more_
     *
     * @throws Exception _more_
     */
    private void init(Element element) throws Exception {
        embed = XmlUtil.getAttribute(element, ATTR_EMBED, false);
        String id = XmlUtil.getAttribute(element, ATTR_ID);
        String wikiTemplate = XmlUtil.getGrandChildText(element, TAG_WIKI,
                                  "no wiki");


        folderWikiTemplate = XmlUtil.getGrandChildText(element,
                TAG_WIKI_FOLDER, wikiTemplate);
        fileWikiTemplate = XmlUtil.getGrandChildText(element, TAG_WIKI_FILE,
                wikiTemplate);
        types = StringUtil.split(XmlUtil.getAttribute(element, ATTR_TYPES,
                "file,folder"), ",", true, true);

        forGroups = types.contains("folder");
        forFiles  = types.contains("file");

        outputType = new OutputType(XmlUtil.getAttribute(element, ATTR_NAME,
                id), id, OutputType.TYPE_VIEW, "",
                     XmlUtil.getAttribute(element, ATTR_ICON,
                                          "/icons/file.gif"));

        addType(outputType);
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
        if (state.group != null) {
            if (forGroups) {
                if (state.getAllEntries().size() > 1) {
                    links.add(makeLink(request, state.getEntry(),
                                       outputType));

                    return;
                }
            }
            for (String type : types) {
                if (state.group.getTypeHandler().isType(type)) {
                    if (state.getAllEntries().size() > 1) {
                        links.add(makeLink(request, state.getEntry(),
                                           outputType));
                    }

                    return;
                }
            }
        }

        if (state.entry != null) {
            if (forFiles) {
                links.add(makeLink(request, state.entry, outputType));

                return;
            }
            for (String type : types) {
                if (state.entry.getTypeHandler().isType(type)) {
                    links.add(makeLink(request, state.entry, outputType));

                    return;
                }
            }
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
    @Override
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        String wiki = folderWikiTemplate;
        if (embed) {
            String outerTemplate = getPageHandler().getWikiTemplate(request,
                                       group, PageHandler.TEMPLATE_DEFAULT);
            wiki = outerTemplate.replace("${innercontent}", wiki);
        }

        wiki = getWikiManager().wikifyEntry(request, group, wiki, false,
                                            subGroups, entries);

        return new Result("", new StringBuffer(wiki));

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        String wiki = fileWikiTemplate;
        if (embed) {
            String outerTemplate = getPageHandler().getWikiTemplate(request,
                                       entry, PageHandler.TEMPLATE_DEFAULT);
            wiki = outerTemplate.replace("${innercontent}", wiki);
        }
        wiki = getWikiManager().wikifyEntry(request, entry, wiki);

        return new Result("", new StringBuffer(wiki));
    }




}
