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


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class MetametaFieldTypeHandler extends MetametaFieldTypeHandlerBase {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public MetametaFieldTypeHandler(Repository repository, Element entryNode)
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
     * @param entry _more_
     * @param parent _more_
     * @param newEntry _more_
     *
     * @throws Exception _more_
     */
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        super.initializeEntryFromForm(request, entry, parent, newEntry);
        setSortOrder(request, entry, parent);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param column _more_
     * @param widget _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public String getFormWidget(Request request, Entry entry, Column column,
                                String widget)
            throws Exception {
        if ((entry != null) && column.getName().equals("properties")) {
            String suffix = "";
            Entry  parent = entry.getParentEntry();
            if ((parent != null)
                    && parent.getTypeHandler().isType(
                        MetametaDictionaryTypeHandler.TYPE)) {
                MetametaDictionaryTypeHandler mdth =
                    (MetametaDictionaryTypeHandler) parent.getTypeHandler();
                if (mdth.isPoint(request, parent)) {
                    suffix =
                        "format=<i>date format</i><br>timezone=<i>timezone</i><br>utcoffset=<i>utc offset</i><br>chartable/searchable=true<br>value=<i>default value</i><br>pattern=<i>header pattern</i><br>precision=<br>isdate/istime=true<br>";
                } else if (mdth.isEntry(request, parent)) {
                    suffix =
                        "cansearch,canshow,canlist=true|false<br>group=<i>Field display group</i><br>isindex=true<br>suffix=label to show after form<br>";
                }
            }

            return HtmlUtils.hbox(widget, HtmlUtils.inset(suffix, 5));
        }

        return super.getFormWidget(request, entry, column, widget);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parent _more_
     *
     * @throws Exception _more_
     */
    public void setSortOrder(Request request, Entry entry, Entry parent)
            throws Exception {
        Integer index = (Integer) getEntryValue(entry, INDEX_FIELD_INDEX);
        int     idx   = ((index == null)
                         ? -1
                         : index.intValue());
        if (idx < 0) {
            int maxIndex = -1;
            List<Entry> siblings = getEntryManager().getChildrenAll(request,
                                       parent);
            for (Entry sibling : siblings) {
                if (sibling.isType(TYPE)) {
                    int siblingIndex = ((Integer) getEntryValue(sibling,
                                           0)).intValue();
                    maxIndex = Math.max(maxIndex, siblingIndex);
                }
            }
            setEntryValue(entry, 0, new Integer(maxIndex + 1));
        }

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
        String s = (String) getEntryValue(entry, INDEX_PROPERTIES);
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
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void generateDbXml(Request request, StringBuffer xml, Entry entry)
            throws Exception {
        //   <column name="title" type="string" label="Title" cansearch="true"   canlist="true" required="true"/>
        Object[]  values = getEntryValues(entry);

        String    id     = (String) values[INDEX_FIELD_ID];
        String    type   = (String) values[INDEX_DATATYPE];
        String    enums  = (String) values[INDEX_ENUMERATION_VALUES];
        Hashtable props  = getProperties(entry);
        int       size   = ((values[INDEX_DATABASE_COLUMN_SIZE] != null)
                            ? ((Integer) values[INDEX_DATABASE_COLUMN_SIZE])
                                .intValue()
                            : 200);
        if (size <= 0) {
            size = 200;
        }

        StringBuffer attrs = new StringBuffer();
        StringBuffer inner = new StringBuffer();
        attrs.append(XmlUtil.attr("name", id));
        attrs.append(XmlUtil.attr("label", entry.getName()));
        attrs.append(XmlUtil.attr("type", type));

        if (Utils.stringDefined(enums)
                && (type.equals(DataTypes.DATATYPE_ENUMERATION)
                    || type.equals(DataTypes.DATATYPE_ENUMERATIONPLUS))) {
            if (enums.startsWith("file:")) {
                attrs.append(XmlUtil.attr("values", enums.trim()));
            } else {
                inner.append(XmlUtil.tag("values", "",
                                         XmlUtil.getCdata(enums)));
            }
        }


        String[] attrProps = {
            Column.ATTR_GROUP, Column.ATTR_DEFAULT, Column.ATTR_SUFFIX,
            Column.ATTR_FORMAT, Column.ATTR_ROWS, Column.ATTR_COLUMNS
        };
        for (String attrProp : attrProps) {
            String v = (String) props.get(attrProp);
            if (v != null) {
                props.remove(attrProp);
                attrs.append(XmlUtil.attr(attrProp, v));
            }
        }

        if (Misc.getProperty(props, Column.ATTR_CANSEARCH, false)) {
            attrs.append(XmlUtil.attr(Column.ATTR_CANSEARCH, "true"));
        }

        if ( !Misc.getProperty(props, Column.ATTR_CANLIST, true)) {
            attrs.append(XmlUtil.attr(Column.ATTR_CANLIST, "false"));
        }

        if (type.equals(Column.DATATYPE_STRING)
                || type.equals(Column.DATATYPE_LIST)) {
            attrs.append(XmlUtil.attr(Column.ATTR_SIZE, "" + size));
        }

        if (Misc.getProperty(props, Column.ATTR_ISCATEGORY, false)) {
            inner.append(propertyTag(Column.ATTR_ISCATEGORY, "true"));
        }

        if (Misc.getProperty(props, Column.ATTR_LABEL, false)) {
            inner.append(propertyTag(Column.ATTR_LABEL, "true"));
        }

        xml.append(XmlUtil.tag(TAG_COLUMN, attrs.toString(),
                               inner.toString()));

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


}
