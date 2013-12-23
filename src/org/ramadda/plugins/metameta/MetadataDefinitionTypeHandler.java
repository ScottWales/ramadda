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


/**
 * Class TypeHandler _more_
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class MetadataDefinitionTypeHandler extends ExtensibleGroupTypeHandler {

    /** _more_ */


    public static final String ARG_METADATA_BULK = "metadata.bulk";

    /** _more_          */
    public static final String ARG_METADATA_MOVE_UP = "metadata.move.up";

    /** _more_          */
    public static final String ARG_METADATA_MOVE_DOWN = "metadata.move.down";

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public MetadataDefinitionTypeHandler(Repository repository,
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
        List<Entry> sorted = sortEntries(entries, false);

        return sorted;
    }

    /**
     * _more_
     *
     * @param entries _more_
     * @param descending _more_
     *
     * @return _more_
     */
    private List<Entry> sortEntries(List<Entry> entries,
                                    final boolean descending) {
        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Entry e1 = (Entry) o1;
                Entry e2 = (Entry) o2;
                int   result;
                if (e1.isType(MetadataColumnTypeHandler.TYPE_METADATA_FIELD)
                        && e2.isType(
                            MetadataColumnTypeHandler.TYPE_METADATA_FIELD)) {
                    Integer i1 = (Integer) e1.getValue(0);
                    Integer i2 = (Integer) e2.getValue(0);
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
     * @param entry _more_
     * @param links _more_
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, Entry entry, List<Link> links)
            throws Exception {
        super.getEntryLinks(request, entry, links);
        /*
        links.add(
            new Link(
                request.entryUrl(
                    getRepository().URL_ENTRY_ACCESS, entry, "type",
                    "kml"), getRepository().iconUrl(ICON_KML),
                    "Convert GPX to KML", OutputType.TYPE_FILE));*/
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
            sb.append("<tr><td>");
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
        sb.append(HtmlUtils.formClose());

        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.hr());

        sb.append(getBulkForm(request, parent));

        return getEntryManager().addEntryHeader(request, parent,
                new Result("Metadata Definition", sb));
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

        if ( !getEntryManager().canAddTo(request, entry)) {
            return null;
        }


        boolean didMove = false;
        List<Entry> children = getEntryManager().getChildrenAll(request,
                                   entry);
        for (int i = 0; i < children.size(); i++) {
            Entry child = children.get(i);
            if ( !child.isType(
                    MetadataColumnTypeHandler.TYPE_METADATA_FIELD)) {
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
                if ( !child.isType(
                        MetadataColumnTypeHandler.TYPE_METADATA_FIELD)) {
                    continue;
                }
                index++;
                child.getTypeHandler().getValues(child)[0] =
                    new Integer(index);
            }
            getEntryManager().updateEntries(request, children);
        }


        if (request.exists(ARG_METADATA_BULK)) {
            StringBuffer xml = new StringBuffer("<entries>\n");
            for (String line :
                    StringUtil.split(request.getString(ARG_METADATA_BULK,
                        ""), "\n", true, true)) {
                if (line.startsWith("#")) {
                    continue;
                }
                List<String> toks = StringUtil.split(line, ",");
                if (toks.size() < 1) {
                    continue;
                }
                String id    = toks.get(0);
                String label = ((toks.size() > 1)
                                ? toks.get(1)
                                : id);
                String type  = ((toks.size() > 2)
                                ? toks.get(2)
                                : "string");
                xml.append(
                    XmlUtil.tag(
                        "entry",
                        XmlUtil.attrs(
                            ATTR_NAME, label, ATTR_TYPE,
                            MetadataColumnTypeHandler.TYPE_METADATA_FIELD,
                            ATTR_PARENT, entry.getId()), XmlUtil.tag(
                                "column_name", "",
                                XmlUtil.getCdata(id)) + XmlUtil.tag(
                                    "datatype", "", XmlUtil.getCdata(type))));


                xml.append("\n");
            }
            xml.append("</entries>\n");
            //Create them from XML
            List<Entry> newEntries =
                getEntryManager().processEntryXml(request,
                    XmlUtil.getRoot(xml.toString()), entry, null);

            //Now tell them to update again to update their sort order
            for (Entry newEntry : newEntries) {
                newEntry.getTypeHandler().initializeEntryFromForm(request,
                        newEntry, entry, false);
                //Insert the updates
                getEntryManager().updateEntry(request, newEntry);
            }

        }


        String url = request.entryUrl(getRepository().URL_ENTRY_SHOW, entry);

        return new Result(url);
        //        return getEntryManager().addEntryHeader(request, entry, new Result("Metadata Definition", sb));

    }






    /**
     * _more_
     *
     * @param wikiUtil _more_
     * @param request _more_
     * @param originalEntry _more_
     * @param entry _more_
     * @param tag _more_
     * @param props _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public String getWikiInclude(WikiUtil wikiUtil, Request request,
                                 Entry originalEntry, Entry entry,
                                 String tag, Hashtable props)
            throws Exception {

        if (tag.equals("metadata.bulkform")) {
            return getBulkForm(request, entry);
        }

        return super.getWikiInclude(wikiUtil, request, originalEntry, entry,
                                    tag, props);
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
    private String getBulkForm(Request request, Entry entry)
            throws Exception {
        if ( !getEntryManager().canAddTo(request, entry)) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(request.form(getRepository().URL_ENTRY_ACCESS));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtils.pre("column_id, label, type (e.g., string, int, double)"));
        sb.append(
            HtmlUtils.textArea(
                ARG_METADATA_BULK,
                "",5, 70));
        sb.append(HtmlUtils.br());
        sb.append(HtmlUtils.submit("Create columns", "submit"));
        sb.append(HtmlUtils.formClose());

        return sb.toString();
    }




}
