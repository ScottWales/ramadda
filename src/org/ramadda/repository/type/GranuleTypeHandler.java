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
package org.ramadda.repository.type;


import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.type.GenericTypeHandler;

import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;

import java.util.Date;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Class description
 *
 *
 * @version        $version$, Fri, Aug 23, '13
 * @author         Enter your name here...    
 */
public class GranuleTypeHandler extends GenericTypeHandler {

    /** _more_          */
    String collectionId = null;

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public GranuleTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
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
    @Override
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        super.initializeEntryFromForm(request, entry, parent, newEntry);
        if ( !newEntry) {
            return;
        }
        if ( !entry.isFile()) {
            return;
        }
        initializeEntry(entry);
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
    public void initializeEntry(Entry entry) throws Exception {
        collectionId = "";
        Entry parent = entry.getParentEntry();
        while (parent != null) {
            if (parent.getTypeHandler().isType(getProperty("collection_type",
                    ""))) {
                collectionId = parent.getId();

                break;
            }
            parent = parent.getParentEntry();
        }
        if (collectionId.equals("")) {
            System.err.println("Could not find collection:" + entry);
        }

        Object[] values = getEntryValues(entry);
        values[0] = collectionId;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param column _more_
     * @param tmpSb _more_
     * @param values _more_
     *
     * @throws Exception _more_
     */
    public void formatColumnHtmlValue(Request request, Entry entry,
                                      Column column, StringBuffer tmpSb,
                                      Object[] values)
            throws Exception {
        Entry collection = null;
        if (column.isEnumeration() && (values != null)
                && (values[0] != null)) {  // get enum values from Collection
            collection = getRepository().getEntryManager().getEntry(request,
                    (String) values[0]);
        }
        if (collection != null) {
            CollectionTypeHandler th =
                (CollectionTypeHandler) collection.getTypeHandler();
            Hashtable enumMap = th.getColumnEnumTable(column);
            String    s       = column.toString(values, column.getOffset());
            String    label   = (String) enumMap.get(s);
            if (label != null) {
                s = label;
            }
            tmpSb.append(s);
        } else {
            column.formatValue(entry, tmpSb, Column.OUTPUT_HTML, values);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param granule _more_
     *
     * @return _more_
     */
    public static Entry getCollectionEntry(Request request, Entry granule) {
        if (granule == null) {
            return null;
        }
        if (granule.getTypeHandler() instanceof GranuleTypeHandler) {
            Object[] values = granule.getValues();
            if (values != null) {
                String collectionEntryId = (String) values[0];
                if (collectionEntryId != null) {
                    try {
                        Entry collection =
                            granule.getTypeHandler().getEntryManager()
                                .getEntry(request, collectionEntryId);

                        return collection;
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
        }

        return null;
    }

}
