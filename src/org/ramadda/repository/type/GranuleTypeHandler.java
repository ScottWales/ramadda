package org.ramadda.repository.type;

import java.util.Date;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.type.GenericTypeHandler;
import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;

public class GranuleTypeHandler extends GenericTypeHandler {
    
    String collectionId = null;

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
    public void initializeEntry(Entry entry)
            throws Exception {
        collectionId = "";
        Entry parent = entry.getParentEntry();
        while(parent!=null) {
            if(parent.getTypeHandler().isType(getProperty("collection_type",""))) {
                collectionId = parent.getId();
                break;
            }
            parent = parent.getParentEntry();
        }
        if(collectionId.equals("")) {
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
        if (column.isEnumeration() && values != null && values[0] != null) { // get enum values from Collection
            Entry collection = getRepository().getEntryManager().getEntry(request, (String) values[0]);
            CollectionTypeHandler th = (CollectionTypeHandler) collection.getTypeHandler();
            Hashtable enumMap = th.getColumnEnumTable(column);
            String s = column.toString(values, column.getOffset());
            String label = (String) enumMap.get(s);
            if (label != null) {
                s = label;
            }
            tmpSb.append(s);
        } else {
            column.formatValue(entry, tmpSb, Column.OUTPUT_HTML, values);
        }
    }

}
