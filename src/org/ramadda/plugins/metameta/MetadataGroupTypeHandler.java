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
public abstract class MetadataGroupTypeHandler extends ExtensibleGroupTypeHandler {


    /** _more_ */
    public static final String ARG_METADATA_MOVE_UP = "metadata.move.up";

    /** _more_ */
    public static final String ARG_METADATA_MOVE_DOWN = "metadata.move.down";


    /** _more_ */
    public static final String ARG_METADATA_GENERATE_DB =
        "metadata.generate.db";

    /** _more_ */
    public static final String ARG_METADATA_GENERATE_ENTRY =
        "metadata.generate.entry";

    /** _more_ */
    public static final String ARG_METADATA_GENERATE_JAVA =
        "metadata.generate.java";

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public MetadataGroupTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param index _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Hashtable getProperties(Entry entry, int index) throws Exception {
        String s = (String) getEntryValue(entry, index);
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
     *
     * @return _more_
     */
    public boolean canBeCreatedBy(Request request) {
        return request.getUser().getAdmin();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public abstract String getChildType();

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
            getEntryManager().getEntryUtil().sortEntriesOnField(entries,
                false, getChildType(), 0);

        return sorted;
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param parent _more_
     * @param entries _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public void addListForm(Request request, Entry parent,
                            List<Entry> entries, StringBuffer sb)
            throws Exception {

        sb.append(request.form(getRepository().URL_ENTRY_ACCESS));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, parent.getId()));
        if (entries.size() > 0) {
            sb.append(msgHeader("Field Definitions"));
        }
        sb.append(HtmlUtils.formTable());
        int cnt = 0;
        for (Entry entry : entries) {
            sb.append("<tr valign=top><td>");
            if (cnt > 0) {
                sb.append(HtmlUtils.submitImage(iconUrl(ICON_UPARROW),
                        ARG_METADATA_MOVE_UP + "." + entry.getId(),
                        "Move up"));
            }
            sb.append("</td><td>");
            if (cnt < entries.size() - 1) {
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
        List<String> buttons = new ArrayList<String>();
        addEntryButtons(request, parent, buttons);
        buttons.add(HtmlUtils.submit("Generate db.xml",
                                     ARG_METADATA_GENERATE_DB));
        sb.append(HtmlUtils.buttons(buttons));
        sb.append(HtmlUtils.formClose());
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param buttons _more_
     */
    public void addEntryButtons(Request request, Entry entry,
                                List<String> buttons) {
        buttons.add(HtmlUtils.submit("Generate entries types.xml",
                                     ARG_METADATA_GENERATE_ENTRY));
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

        List<Entry> definitionFields = new ArrayList<Entry>();
        List<Entry> children = getEntryManager().getChildrenAll(request,
                                   entry);

        for (int i = 0; i < children.size(); i++) {
            Entry child = children.get(i);
            if ( !child.isType(getChildType())) {
                continue;
            }
            definitionFields.add(child);
        }

        children = definitionFields;

        boolean didMove = false;
        for (int i = 0; i < children.size(); i++) {
            Entry child = children.get(i);
            if ( !child.isType(getChildType())) {
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

            return new Result("Query Results", xml, "text/xml");
        }


        if (request.exists(ARG_METADATA_GENERATE_ENTRY)) {
            StringBuffer xml = new StringBuffer();
            generateEntryXml(request, xml, entry, children);
            String filename =
                IOUtil.stripExtension(IOUtil.getFileTail(entry.getName()))
                + "_types.xml";
            request.setReturnFilename(filename);

            return new Result("Query Results", xml, "text/xml");
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
    public abstract void generateDbXml(Request request, StringBuffer xml,
                                       Entry parent, List<Entry> children)
     throws Exception;



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
    public abstract void generateEntryXml(Request request, StringBuffer xml,
                                          Entry parent, List<Entry> children)
     throws Exception;





}
