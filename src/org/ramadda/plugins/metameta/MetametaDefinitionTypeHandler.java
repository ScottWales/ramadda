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
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.Utils;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


/**
 * Holds a collection of fields
 *
 *
 * @author RAMADDA Development Team
 */
public class MetametaDefinitionTypeHandler extends MetametaDefinitionTypeHandlerBase {


    /** _more_ */
    public static final String ARG_METAMETA_BULK = "metameta.bulk";



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public MetametaDefinitionTypeHandler(Repository repository,
                                         Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getChildType() {
        return MetametaFieldTypeHandler.TYPE;
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

        List<String> titles   = new ArrayList<String>();
        List<String> contents = new ArrayList<String>();

        StringBuffer sb       = new StringBuffer();
        subGroups.addAll(entries);
        addListForm(request, parent, subGroups, sb);

        titles.add(msg("Fields"));
        contents.add(sb.toString());

        sb.setLength(0);
        sb.append(getBulkForm(request, parent));
        titles.add(msg("Create new fields"));
        contents.add(sb.toString());

        StringBuffer formSB = new StringBuffer();
        getEntryManager().addEntryForm(request, parent, formSB);
        titles.add(msg("Settings"));
        contents.add(formSB.toString());

        sb.setLength(0);
        sb.append(getWikiManager().wikifyEntry(request, parent,"<div class=wiki-h2>{{name}}</div><p>{{description}} <p>\n"));
        sb.append(OutputHandler.makeTabs(titles, contents, false));
        return getEntryManager().addEntryHeader(request, parent,
                new Result("Metameta Definition", sb));
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

        if (request.exists(ARG_METAMETA_GENERATE_JAVA)) {
            return handleGenerateEntryJava(request, entry);

        }

        if (request.exists(ARG_METAMETA_BULK)) {
            return handleBulkCreate(request, entry);
        }


        return super.processEntryAccess(request, entry);
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
    public Result handleBulkCreate(Request request, Entry entry)
            throws Exception {
        StringBuffer xml = new StringBuffer(XmlUtil.openTag(TAG_ENTRIES));
        xml.append("\n");
        for (String line :
                StringUtil.split(request.getString(ARG_METAMETA_BULK, ""),
                                 "\n", true, true)) {
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
            label = label.replace("_", " ");

            StringBuffer properties = new StringBuffer();
            for (int i = 3; i < toks.size(); i++) {
                properties.append(toks.get(i));
                properties.append("\n");
            }

            StringBuffer inner = new StringBuffer();
            inner.append(XmlUtil.tag(MetametaFieldTypeHandler.FIELD_FIELD_ID,
                                     "", XmlUtil.getCdata(id)));
            inner.append(
                XmlUtil.tag(
                    FIELD_PROPERTIES, "",
                    XmlUtil.getCdata(properties.toString())));
            inner.append(XmlUtil.tag(MetametaFieldTypeHandler.FIELD_DATATYPE,
                                     "", XmlUtil.getCdata(type)));
            xml.append(XmlUtil.tag(TAG_ENTRY,
                                   XmlUtil.attrs(ATTR_NAME, label, ATTR_TYPE,
                                       MetametaFieldTypeHandler.TYPE,
                                       ATTR_PARENT,
                                       entry.getId()), inner.toString()));
            xml.append("\n");
        }
        xml.append(XmlUtil.closeTag(TAG_ENTRIES));
        System.out.println(xml);

        //Create them from XML
        List<Entry> newEntries = getEntryManager().processEntryXml(request,
                                     XmlUtil.getRoot(xml.toString()), entry,
                                     null);

        //Now tell them to update again to update their sort order
        for (Entry newEntry : newEntries) {
            if (newEntry.getTypeHandler()
                    instanceof MetametaFieldTypeHandler) {
                ((MetametaFieldTypeHandler) newEntry.getTypeHandler())
                    .setSortOrder(request, newEntry, entry);
                //Insert the updates
                getEntryManager().updateEntry(request, newEntry);
            }
        }

        //Redirect
        String url = request.entryUrl(getRepository().URL_ENTRY_SHOW, entry);

        return new Result(url);

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
    public Result handleGenerateEntryJava(Request request, Entry entry)
            throws Exception {
        String java =
            getStorageManager().readSystemResource(
                "/org/ramadda/plugins/metameta/TypeHandler.template");
        StringBuffer defines = new StringBuffer();
        StringBuffer methods = new StringBuffer();
        String handlerClass = (String) getEntryValue(entry,
                                  INDEX_HANDLER_CLASS);
        String shortName = (String) getEntryValue(entry, INDEX_SHORT_NAME);
        boolean isGroup = ((Boolean) getEntryValue(entry,
                              INDEX_ISGROUP)).booleanValue();
        int    idx       = handlerClass.lastIndexOf('.');
        String pkg       = handlerClass.substring(0, idx);
        String className = handlerClass.substring(idx + 1) + "Base";

        java = java.replace("${package}", pkg);
        java = java.replace("${classname}", className);
        java = java.replace("${parentclassname}", isGroup
                ? "ExtensibleGroupTypeHandler"
                : "GenericTypeHandler");


        defines.append("\tpublic static final String TYPE = "
                       + HtmlUtils.quote(shortName) + ";\n");

        defines.append("\tprivate static int INDEX_BASE = 0;\n");
        int cnt = 0;
        for (Entry child : getChildrenEntries(request, entry)) {
            MetametaFieldTypeHandler field =
                (MetametaFieldTypeHandler) child.getTypeHandler();
            String fieldId = (String) field.getEntryValue(child,
                                 field.INDEX_FIELD_ID);
            String FIELDID = fieldId.toUpperCase();
            defines.append("\tpublic static final int INDEX_" + FIELDID
                           + " = INDEX_BASE + " + cnt + ";\n");
            defines.append("\tpublic static final String FIELD_" + FIELDID
                           + " = " + HtmlUtils.quote(fieldId) + ";\n");
            //            methods.append("\tprivate static INDEX_" + FIELDID +" = INDEX_BASE + " + cnt +";\n");
            cnt++;
        }

        java = java.replace("${defines}", defines.toString());
        java = java.replace("${methods}", methods.toString());




        request.setReturnFilename(className + ".java");

        return new Result("Java", new StringBuffer(java), "text/java");
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
        boolean first = xml.length() == 0;
        if (first) {
            xml.append(XmlUtil.openTag("tables", ""));
        }
        String shortName = (String) getEntryValue(parent, INDEX_SHORT_NAME);
        String handlerClass = (String) getEntryValue(parent,
                                  INDEX_HANDLER_CLASS);

        Hashtable props = getProperties(parent, INDEX_PROPERTIES);
        String    icon  = Misc.getProperty(props, "icon", "/db/tasks.gif");

        if ( !Utils.stringDefined(shortName)) {
            shortName = parent.getName();
        }
        if ( !Utils.stringDefined(handlerClass)) {
            handlerClass = "org.ramadda.plugins.db.DbTypeHandler";
        }


        xml.append(XmlUtil.openTag("table",
                                   XmlUtil.attrs("id", shortName, "name",
                                       parent.getName(), ATTR_HANDLER,
                                       handlerClass, "icon", icon)));
        for (Entry recordFieldEntry : children) {
            MetametaFieldTypeHandler field =
                (MetametaFieldTypeHandler) recordFieldEntry.getTypeHandler();
            field.generateDbXml(request, xml, recordFieldEntry);
        }
        xml.append(XmlUtil.closeTag("table"));

        if (first) {
            xml.append(XmlUtil.closeTag("tables"));
        }
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
    public void generateEntryXml(Request request, StringBuffer xml,
                                 Entry parent, List<Entry> children)
            throws Exception {

        boolean first = xml.length() == 0;
        if (first) {
            xml.append(XmlUtil.openTag(TAG_TYPES, ""));
        }


        String shortName = (String) getEntryValue(parent, INDEX_SHORT_NAME);
        String superType = (String) getEntryValue(parent, INDEX_SUPER_TYPE);
        String wikiText  = (String) getEntryValue(parent, INDEX_WIKI_TEXT);
        String handlerClass = (String) getEntryValue(parent,
                                  INDEX_HANDLER_CLASS);
        boolean isGroup = ((Boolean) getEntryValue(parent,
                              INDEX_ISGROUP)).booleanValue();
        String propertiesString = (String) getEntryValue(parent,
                                      INDEX_PROPERTIES);
        Hashtable props = getProperties(parent, INDEX_PROPERTIES);
        if ( !Utils.stringDefined(shortName)) {
            shortName = parent.getName();
        }
        if ( !Utils.stringDefined(handlerClass)) {
            if ( !isGroup) {
                handlerClass = "org.ramadda.repository.type.TypeHandler";
            } else {
                handlerClass =
                    "org.ramadda.repository.type.ExtensibleGroupTypeHandler";
            }
        }

        StringBuffer attrs = new StringBuffer();
        StringBuffer inner = new StringBuffer();
        if (Utils.stringDefined(wikiText)) {
            inner.append(XmlUtil.tag("wiki", "", XmlUtil.getCdata(wikiText)));
            inner.append("\n");
        }


        attrs.append(XmlUtil.attrs(ATTR_NAME, shortName, ATTR_DESCRIPTION,
                                   parent.getName(), ATTR_HANDLER,
                                   handlerClass));

        String[] attrProps = { ATTR_CHILDTYPES, };
        for (String attrProp : attrProps) {
            String v = (String) getAndRemoveProperty(props, attrProp, null);
            if (v != null) {
                attrs.append(XmlUtil.attr(attrProp, v));
            }
        }




        xml.append(XmlUtil.openTag(TAG_TYPE, attrs.toString()));

        for (Enumeration keys = props.keys(); keys.hasMoreElements(); ) {
            String key   = (String) keys.nextElement();
            String value = (String) props.get(key);
            xml.append(propertyTag(key, value));
        }
        xml.append(inner);


        for (Entry recordFieldEntry : children) {
            MetametaFieldTypeHandler field =
                (MetametaFieldTypeHandler) recordFieldEntry.getTypeHandler();
            field.generateDbXml(request, xml, recordFieldEntry);
            xml.append("\n");
        }
        xml.append(XmlUtil.closeTag(TAG_TYPE));

        if (first) {
            xml.append(XmlUtil.closeTag(TAG_TYPES));
        }

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param buttons _more_
     */
    @Override
    public void addEntryButtons(Request request, Entry entry,
                                List<String> buttons) {
        super.addEntryButtons(request, entry, buttons);
        String handlerClass = (String) getEntryValue(entry,
                                  INDEX_HANDLER_CLASS);
        if (Utils.stringDefined(handlerClass)) {
            buttons.add(HtmlUtils.submit("Generate Java base class",
                                         ARG_METAMETA_GENERATE_JAVA));
        }
    }



    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     *
     * @return _more_
     */
    public String propertyTag(String name, String value) {
        return XmlUtil.tag(TAG_PROPERTY,
                           XmlUtil.attrs(ATTR_NAME, name, ATTR_VALUE, value));
    }

    /**
     * _more_
     *
     * @param props _more_
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    private String getAndRemoveProperty(Hashtable props, String key,
                                        String dflt) {
        String value = Misc.getProperty(props, key, dflt);
        props.remove(key);

        return value;
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
        sb.append(HtmlUtils.textArea(ARG_METAMETA_BULK, "", 5, 70));
        sb.append(HtmlUtils.br());
        sb.append(HtmlUtils.submit("Add Fields", "submit"));
        sb.append(HtmlUtils.formClose());

        return sb.toString();
    }




}
