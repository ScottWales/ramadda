/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.util;


import org.w3c.dom.*;

import ucar.unidata.xml.XmlUtil;


/**
 * Class description
 *
 *
 * @version        $version$, Wed, Aug 31, '11
 * @author         Enter your name here...    
 */
public class KmlToRamadda {

    /** _more_          */
    static int counter = 0;

    /**
     * _more_
     *
     * @return _more_
     */
    public static String getId() {
        return "id" + (counter++);
    }


    /**
     * _more_
     *
     * @param element _more_
     * @param parentId _more_
     *
     * @throws Exception _more_
     */
    public static void process(Element element, String parentId)
            throws Exception {
        NodeList elements = XmlUtil.getElements(element);
        for (int i = 0; i < elements.getLength(); i++) {
            Element child = (Element) elements.item(i);
            String  tag   = child.getTagName();
            if (tag.equals("Folder")) {
                String id   = getId();
                String name = XmlUtil.getGrandChildText(child, "name",
                                  "name");
                if (parentId != null) {
                    System.out.println(XmlUtil.tag("entry",
                            XmlUtil.attrs(new String[] {
                        "id", id, "name", name, "parent", parentId, "type",
                        "group"
                    })));
                } else {
                    System.out.println(XmlUtil.tag("entry",
                            XmlUtil.attrs(new String[] {
                        "id", id, "name", name, "type", "group"
                    })));
                }
                process(child, id);
            } else if (tag.equals("Placemark")) {
                String id   = getId();
                String name = XmlUtil.getGrandChildText(child, "name",
                                  "name");
                String desc = XmlUtil.getGrandChildText(child, "description",
                                  "");
                String descNode = XmlUtil.tag("description", "",
                                      XmlUtil.getCdata(desc));

                if (parentId != null) {
                    System.out.println(XmlUtil.tag("entry",
                            XmlUtil.attrs(new String[] {
                        "id", id, "name", name, "parent", parentId
                    }), descNode));
                } else {
                    System.out.println(XmlUtil.tag("entry",
                            XmlUtil.attrs(new String[] { "id",
                            id, "name", name }), descNode));
                }
            } else if (tag.equals("Document")) {
                process(child, parentId);
            } else {
                //                System.err.println(tag);
            }
        }
    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        System.out.println("<entries>");
        for (String arg : args) {
            process(XmlUtil.getRoot(arg, KmlToRamadda.class), null);
        }
        System.out.println("</entries>");
    }

}
