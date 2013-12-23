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
public class MetadataDefinitionTypeHandler extends ExtensibleGroupTypeHandler {

    /** _more_ */
    public static final String TYPE_METADATA_DEFINITION =
        "type_metadata_definition";


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
        return MetadataCollectionTypeHandler.sortEntries(entries, false,
                MetadataFieldTypeHandler.TYPE_METADATA_FIELD);
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


        List<Entry> recordFields = new ArrayList<Entry>();


        boolean     didMove      = false;
        List<Entry> children = getEntryManager().getChildrenAll(request,
                                   entry);

        for (int i = 0; i < children.size(); i++) {
            Entry child = children.get(i);
            if ( !child.isType(
                    MetadataFieldTypeHandler.TYPE_METADATA_FIELD)) {
                continue;
            }
            recordFields.add(child);
        }

        children = recordFields;

        for (int i = 0; i < children.size(); i++) {
            Entry child = children.get(i);
            if ( !child.isType(
                    MetadataFieldTypeHandler.TYPE_METADATA_FIELD)) {
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


        if (request.exists(ARG_METADATA_BULK)) {

            StringBuffer xml = new StringBuffer(XmlUtil.openTag(TAG_ENTRIES));
            xml.append("\n");
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
                        TAG_ENTRY,
                        XmlUtil.attrs(
                            ATTR_NAME, label, ATTR_TYPE,
                            MetadataFieldTypeHandler.TYPE_METADATA_FIELD,
                            ATTR_PARENT, entry.getId()), XmlUtil.tag(
                                "field_id", "",
                                XmlUtil.getCdata(id)) + XmlUtil.tag(
                                    "datatype", "", XmlUtil.getCdata(type))));

                xml.append("\n");
            }
            xml.append(XmlUtil.closeTag(TAG_ENTRIES));
            System.out.println(xml);

            //Create them from XML
            List<Entry> newEntries =
                getEntryManager().processEntryXml(request,
                    XmlUtil.getRoot(xml.toString()), entry, null);

            //Now tell them to update again to update their sort order
            for (Entry newEntry : newEntries) {
                if (newEntry.getTypeHandler()
                        instanceof MetadataFieldTypeHandler) {
                    ((MetadataFieldTypeHandler) newEntry.getTypeHandler())
                        .setSortOrder(request, newEntry, entry);
                    //Insert the updates
                    getEntryManager().updateEntry(request, newEntry);
                }
            }

        }


        String url = request.entryUrl(getRepository().URL_ENTRY_SHOW, entry);

        return new Result(url);
        //        return getEntryManager().addEntryHeader(request, entry, new Result("Metadata Definition", sb));

    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Hashtable getProperties(Entry entry) throws Exception {
        String s = (String) getEntryValue(entry, 4);
        if (s == null) {
            s = "";
        }
        Properties props = new Properties();
        props.load(new ByteArrayInputStream(s.getBytes()));
        Hashtable table = new Hashtable();
        table.putAll(props);

        return table;
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
        String    shortName    = (String) getEntryValue(parent, 2);
        String    handlerClass = (String) getEntryValue(parent, 3);
        Hashtable props        = getProperties(parent);
        String    icon = Misc.getProperty(props, "icon", "/db/tasks.gif");

        if ( !Utils.stringDefined(shortName)) {
            shortName = parent.getName();
        }
        if ( !Utils.stringDefined(handlerClass)) {
            handlerClass = "org.ramadda.plugins.db.DbTypeHandler";
        }


        xml.append(XmlUtil.openTag("table",
                                   XmlUtil.attrs("id", shortName, "name",
                                       parent.getName(), "handler",
                                       handlerClass, "icon", icon)));

        //   <column name="title" type="string" label="Title" cansearch="true"   canlist="true" required="true"/>
        for (Entry recordFieldEntry : children) {
            MetadataFieldTypeHandler field =
                (MetadataFieldTypeHandler) recordFieldEntry.getTypeHandler();
            Object[]     values  = field.getEntryValues(recordFieldEntry);
            String       id      = (String) values[1];
            String       type    = (String) values[2];
            String       enums   = (String) values[3];
            Hashtable    fprops  = field.getProperties(recordFieldEntry);
            int          rows    = ((Integer) values[5]).intValue();
            int          columns = ((Integer) values[6]).intValue();
            int          size    = ((Integer) values[7]).intValue();
            StringBuffer attrs   = new StringBuffer();
            StringBuffer inner   = new StringBuffer();
            attrs.append(XmlUtil.attr("name", id));
            attrs.append(XmlUtil.attr("label", recordFieldEntry.getName()));
            attrs.append(XmlUtil.attr("type", type));
            attrs.append(XmlUtil.attr("cansearch",
                                      Misc.getProperty(fprops, "cansearch",
                                          "true")));
            attrs.append(XmlUtil.attr("canlist",
                                      Misc.getProperty(fprops, "canlist",
                                          "true")));
            attrs.append(XmlUtil.attr("rows", "" + rows));
            attrs.append(XmlUtil.attr("columns", "" + columns));
            attrs.append(XmlUtil.attr("size", "" + size));

            if (Misc.getProperty(fprops, "iscategory", false)) {
                inner.append(XmlUtil.tag("property",
                                         XmlUtil.attrs("name", "iscategory",
                                             "value", "true")));
            }

            if (Misc.getProperty(fprops, "label", false)) {
                inner.append(XmlUtil.tag("property",
                                         XmlUtil.attrs("name", "label",
                                             "value", "true")));
            }

            xml.append(XmlUtil.tag("column", attrs.toString(),
                                   inner.toString()));
        }
        xml.append(XmlUtil.closeTag("table"));
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
        sb.append(HtmlUtils.p());
        sb.append(
            HtmlUtils.italics(
                "column_id, label, type (e.g., string, int, double)"));
        sb.append(HtmlUtils.br());
        sb.append(HtmlUtils.textArea(ARG_METADATA_BULK, "", 5, 70));
        sb.append(HtmlUtils.br());
        sb.append(HtmlUtils.submit("Add fields", "submit"));
        sb.append(HtmlUtils.formClose());

        return sb.toString();
    }




}
