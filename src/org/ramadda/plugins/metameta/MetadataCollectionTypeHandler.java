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

package org.ramadda.plugins.metameta;




import org.ramadda.repository.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.Utils;
import org.ramadda.util.WikiUtil;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 * Class TypeHandler _more_
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class MetadataCollectionTypeHandler extends ExtensibleGroupTypeHandler {

    /** _more_ */
    public static final String ARG_METADATA_BULK = "metadata.bulk";

    /** _more_ */
    public static final String ARG_METADATA_MOVE_UP = "metadata.move.up";

    /** _more_ */
    public static final String ARG_METADATA_MOVE_DOWN = "metadata.move.down";


    /** _more_ */
    public static final String ARG_METADATA_GENERATE_DB =
        "metadata.generate.db";

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public MetadataCollectionTypeHandler(Repository repository,
                                         Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public boolean canBeCreatedBy(Request request) {
        return request.getUser().getAdmin();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     *
     * @return _more_
     */
    @Override
    public List<Entry> postProcessEntries(Request request,
                                          List<Entry> entries) {
        List<Entry> sorted =
            sortEntries(
                entries, false,
                MetadataDefinitionTypeHandler.TYPE_METADATA_DEFINITION);

        return sorted;
    }

    /**
     * _more_
     *
     * @param entries _more_
     * @param descending _more_
     * @param type _more_
     *
     * @return _more_
     */
    public static List<Entry> sortEntries(List<Entry> entries,
                                          final boolean descending,
                                          final String type) {
        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Entry e1 = (Entry) o1;
                Entry e2 = (Entry) o2;
                int   result;
                if (e1.isType(type) && e2.isType(type)) {
                    Integer i1 =
                        (Integer) e1.getTypeHandler().getEntryValue(e1, 0);
                    Integer i2 =
                        (Integer) e2.getTypeHandler().getEntryValue(e2, 0);
                    result = i1.compareTo(i2);
                } else {
                    result = e1.getName().compareToIgnoreCase(e2.getName());
                }
                if (descending) {
                    if (result >= 1) {
                        return -1;
                    } else if (result <= -1) {
                        return 1;
                    }

                    return 0;
                }

                return result;
            }
            public boolean equals(Object obj) {
                return obj == this;
            }
        };
        Object[] array = entries.toArray();
        Arrays.sort(array, comp);

        return (List<Entry>) Misc.toList(array);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param parent _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Result getHtmlDisplay(Request request, Entry parent,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        if ( !getEntryManager().canAddTo(request, parent)) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(request.form(getRepository().URL_ENTRY_ACCESS));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, parent.getId()));
        sb.append(HtmlUtils.formTable());
        subGroups.addAll(entries);
        int cnt = 0;
        for (Entry entry : subGroups) {
            sb.append("<tr valign=top><td>");
            if (cnt > 0) {
                sb.append(HtmlUtils.submitImage(iconUrl(ICON_UPARROW),
                        ARG_METADATA_MOVE_UP + "." + entry.getId(),
                        "Move up"));
            }
            sb.append("</td><td>");
            if (cnt < subGroups.size() - 1) {
                sb.append(HtmlUtils.submitImage(iconUrl(ICON_DOWNARROW),
                        ARG_METADATA_MOVE_DOWN + "." + entry.getId(),
                        "Move down"));
            }
            sb.append("</td><td>");
            cnt++;
            EntryLink link = getEntryManager().getAjaxLink(request, entry,
                                 entry.getName());
            sb.append(link.getLink());
            sb.append(link.getFolderBlock());
            sb.append("</td></tr>");
        }

        sb.append(HtmlUtils.formTableClose());

        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit("Generate db.xml",
                                   ARG_METADATA_GENERATE_DB));
        sb.append(HtmlUtils.formClose());

        return getEntryManager().addEntryHeader(request, parent,
                new Result("Metadata Collection", sb));
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Result processEntryAccess(Request request, Entry entry)
            throws Exception {

        String childType =
            MetadataDefinitionTypeHandler.TYPE_METADATA_DEFINITION;

        if ( !getEntryManager().canAddTo(request, entry)) {
            return null;
        }

        List<Entry> definitionFields = new ArrayList<Entry>();
        List<Entry> children = getEntryManager().getChildrenAll(request,
                                   entry);

        for (int i = 0; i < children.size(); i++) {
            Entry child = children.get(i);
            if ( !child.isType(childType)) {
                continue;
            }
            definitionFields.add(child);
        }

        children = definitionFields;

        boolean didMove = false;
        for (int i = 0; i < children.size(); i++) {
            Entry child = children.get(i);
            if ( !child.isType(childType)) {
                continue;
            }
            if (request.exists(ARG_METADATA_MOVE_UP + "." + child.getId()
                               + ".x")) {
                didMove = true;
                if (i > 0) {
                    children.remove(child);
                    children.add(i - 1, child);
                }

                break;
            }
            if (request.exists(ARG_METADATA_MOVE_DOWN + "." + child.getId()
                               + ".x")) {
                didMove = true;
                if (i < children.size() - 1) {
                    children.remove(child);
                    children.add(i + 1, child);
                }

                break;
            }
        }

        if (didMove) {
            int index = 0;
            for (int i = 0; i < children.size(); i++) {
                Entry child = children.get(i);
                index++;
                child.getTypeHandler().setEntryValue(child, 0,
                        new Integer(index));
            }
            getEntryManager().updateEntries(request, children);
        }


        if (request.exists(ARG_METADATA_GENERATE_DB)) {
            StringBuffer xml = new StringBuffer();
            generateDbXml(request, xml, entry, children);
            String filename =
                IOUtil.stripExtension(IOUtil.getFileTail(entry.getName()))
                + "_db.xml";
            request.setReturnFilename(filename);
            Result result = new Result("Query Results", xml, "text/xml");

            return result;
        }
        String url = request.entryUrl(getRepository().URL_ENTRY_SHOW, entry);

        return new Result(url);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param xml _more_
     * @param parent _more_
     * @param children _more_
     *
     * @throws Exception _more_
     */
    public void generateDbXml(Request request, StringBuffer xml,
                              Entry parent, List<Entry> children)
            throws Exception {
        xml.append(XmlUtil.openTag("tables", ""));
        for (Entry defEntry : children) {
            MetadataDefinitionTypeHandler defTypeHandler =
                (MetadataDefinitionTypeHandler) defEntry.getTypeHandler();
            List<Entry> fields = getEntryManager().getChildrenAll(request,
                                     defEntry);
            defTypeHandler.generateDbXml(request, xml, defEntry, fields);
        }
        xml.append(XmlUtil.closeTag("tables"));
    }






}
