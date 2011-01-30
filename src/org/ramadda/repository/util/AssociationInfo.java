/*
 * Copyright 2010- ramadda.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.ramadda.repository.util;


import ucar.unidata.xml.XmlUtil;

import java.util.HashSet;
import java.util.List;


/**
 * Holds information for generating associations in entries.xml
 */
public class AssociationInfo {

    /** _more_          */
    private String fromId;

    /** _more_          */
    private String toId;

    /** _more_          */
    private String type;

    /**
     * ctor
     *
     * @param fromId _more_
     * @param toId _more_
     * @param type _more_
     */
    public AssociationInfo(String fromId, String toId, String type) {
        this.fromId = fromId;
        this.toId   = toId;
        this.type   = type;
    }

    /**
     * _more_
     *
     * @param xml _more_
     * @param links _more_
     * @param entryMap _more_
     */
    public static void appendAssociations(StringBuffer xml,
                                          List<AssociationInfo> links,
                                          HashSet<String> entryMap) {
        for (AssociationInfo link : links) {
            String from = link.getFromId();
            String to   = link.getToId();
            String type = link.getType();
            if ( !entryMap.contains(to)) {
                System.err.println("Unknown to link:" + from + " " + to);
                continue;
            }
            if ( !entryMap.contains(from)) {
                System.err.println("Unknown from link:" + from + " " + to);
                continue;
            }
            xml.append(XmlUtil.tag("association",
                                   XmlUtil.attrs("from", from, "to", to,
                                       "type", type)));
            xml.append("\n");
        }

    }


    /**
     *  Set the FromId property.
     *
     *  @param value The new value for FromId
     */
    public void setFromId(String value) {
        fromId = value;
    }

    /**
     *  Get the FromId property.
     *
     *  @return The FromId
     */
    public String getFromId() {
        return fromId;
    }

    /**
     *  Set the ToId property.
     *
     *  @param value The new value for ToId
     */
    public void setToId(String value) {
        toId = value;
    }

    /**
     *  Get the ToId property.
     *
     *  @return The ToId
     */
    public String getToId() {
        return toId;
    }

    /**
     *  Set the Type property.
     *
     *  @param value The new value for Type
     */
    public void setType(String value) {
        type = value;
    }

    /**
     *  Get the Type property.
     *
     *  @return The Type
     */
    public String getType() {
        return type;
    }


}
