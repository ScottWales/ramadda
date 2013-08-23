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

package org.ramadda.repository.util;


import ucar.unidata.xml.XmlUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 * Holds information for generating entries in entries.xml
 */
public class EntryInfo {

    /** _more_ */
    private String id;

    /** _more_ */
    private String parentId;

    /** _more_ */
    private String name;

    /** _more_ */
    private String type;

    /** _more_ */
    private String childXml;

    /**
     * _more_
     *
     * @param id _more_
     * @param name _more_
     * @param parentId _more_
     * @param type _more_
     * @param childXml _more_
     */
    public EntryInfo(String id, String name, String parentId, String type,
                     String childXml) {
        this.id       = id;
        this.name     = name;
        this.parentId = parentId;
        this.type     = type;
        this.childXml = childXml;
    }


    /**
     * _more_
     *
     * @param xml _more_
     * @param entries _more_
     * @param entryMap _more_
     * @param processed _more_
     */
    public static void appendEntries(StringBuffer xml,
                                     List<EntryInfo> entries,
                                     Hashtable<String, EntryInfo> entryMap,
                                     HashSet<String> processed) {
        List<EntryInfo> tmp = new ArrayList<EntryInfo>(entries);
        entries = new ArrayList<EntryInfo>();
        for (int i = 0; i < tmp.size(); i++) {
            EntryInfo entryInfo = tmp.get(i);
            EntryInfo parent    = entryMap.get(entryInfo.getParentId());
            if (parent == null) {
                if ( !processed.contains(entryInfo.getParentId())
                        && (entryInfo.getParentId().length() > 0)) {
                    System.err.println("No parent for entry:"
                                       + entryInfo.getName() + " parent="
                                       + entryInfo.getParentId());

                    continue;
                }
            }

            /*            System.err.println("Entry:" + entryInfo.getName() + " parentId:"
                               + entryInfo.getParentId() +" found:" + parent);
            */
            entries.add(entryInfo);
        }

        for (EntryInfo entryInfo : entries) {
            EntryInfo.appendEntries(xml, entryInfo, processed, entryMap);
        }
    }



    /**
     * _more_
     *
     * @param xml _more_
     * @param entryInfo _more_
     * @param processed _more_
     * @param entryMap _more_
     */
    public static void appendEntries(StringBuffer xml, EntryInfo entryInfo,
                                     HashSet<String> processed,
                                     Hashtable<String, EntryInfo> entryMap) {
        if (processed.contains(entryInfo.getId())) {
            return;
        }
        if ( !processed.contains(entryInfo.getParentId())) {
            EntryInfo parent = entryMap.get(entryInfo.getParentId());
            if (parent == null) {
                if (entryInfo.getParentId().length() > 0) {
                    return;
                }
            } else {
                appendEntries(xml, parent, processed, entryMap);
            }
        }
        processed.add(entryInfo.getId());

        xml.append(
            XmlUtil.tag(
                "entry",
                XmlUtil.attrs(
                    "type", entryInfo.getType(), "name", entryInfo.getName(),
                    "id", entryInfo.getId(), "parent",
                    entryInfo.getParentId()), entryInfo.getChildXml()));
    }




    /**
     *  Set the Id property.
     *
     *  @param value The new value for Id
     */
    public void setId(String value) {
        id = value;
    }

    /**
     *  Get the Id property.
     *
     *  @return The Id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the Type property.
     *
     * @param value The new value for Type
     */
    public void setType(String value) {
        type = value;
    }

    /**
     * Get the Type property.
     *
     * @return The Type
     */
    public String getType() {
        return type;
    }




    /**
     *  Set the ParentId property.
     *
     *  @param value The new value for ParentId
     */
    public void setParentId(String value) {
        parentId = value;
    }

    /**
     *  Get the ParentId property.
     *
     *  @return The ParentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     *  Set the Name property.
     *
     *  @param value The new value for Name
     */
    public void setName(String value) {
        name = value;
    }

    /**
     *  Get the Name property.
     *
     *  @return The Name
     */
    public String getName() {
        return name;
    }

    /**
     *  Set the ChildXml property.
     *
     *  @param value The new value for ChildXml
     */
    public void setChildXml(String value) {
        childXml = value;
    }

    /**
     *  Get the ChildXml property.
     *
     *  @return The ChildXml
     */
    public String getChildXml() {
        return childXml;
    }




}
