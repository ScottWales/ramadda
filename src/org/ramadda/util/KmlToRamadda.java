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
import ucar.unidata.util.StringUtil;

import java.util.List;


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
    public static void process(Element element, String parentId, String category)
            throws Exception {
        NodeList elements = XmlUtil.getElements(element);
        for (int i = 0; i < elements.getLength(); i++) {
            Element child = (Element) elements.item(i);
            String  tag   = child.getTagName();
            if (tag.equals("Folder")) {
                String id   = getId();
                String name = XmlUtil.getGrandChildText(child, "name",
                                  "name");
                /*
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
                    }*/
                //                process(child, id, name);
                process(child, parentId, name);
            } else if (tag.equals("Placemark")) {
                StringBuffer extra  = new StringBuffer();
                Element point = XmlUtil.findChild(child, "Point");
                if(point!=null) {
                    String coords = XmlUtil.getGrandChildText(point, "coordinates",null);
                    if(coords!=null) {
                        List<String> toks  = StringUtil.split(coords, ",", true, true);
                        extra.append(XmlUtil.attrs(new String[]{
                                    "north", toks.get(1),
                                    "south", toks.get(1),
                                    "west", toks.get(0),
                                    "east", toks.get(0),
                                }));
                    }
                }

                String id   = getId();
                String name = XmlUtil.getGrandChildText(child, "name",
                                  "name");
                String desc = XmlUtil.getGrandChildText(child, "description",
                                  "");
                String descNode = XmlUtil.tag("description", "",
                                      XmlUtil.getCdata(desc));

                String attrs;
                if (parentId != null) {
                    attrs = XmlUtil.attrs(new String[] {
                            "id", id, "name", name, "parent", parentId, "type","article"});

                } else {
                    attrs =  XmlUtil.attrs(new String[] { "id",
                                                          id, "name", name, "type","article" });
                }
                if(category!=null) {
                    descNode = descNode+  XmlUtil.tag("category", "",
                                                      XmlUtil.getCdata(category));
                }
                System.out.println(XmlUtil.tag("entry", attrs+extra, descNode));
            } else if (tag.equals("Document")) {
                process(child, parentId, category);
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
        System.out.println(XmlUtil.XML_HEADER);
        System.out.println("<entries>");
        for (String arg : args) {
            process(XmlUtil.getRoot(arg, KmlToRamadda.class), null,null);
        }
        System.out.println("</entries>");
    }

}
