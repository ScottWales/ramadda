/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
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
package org.ramadda.ontology;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;


import java.util.List;
import java.util.Properties;


/**
 * Class description
 *
 *
 * @version        $version$, Thu, Nov 25, '10
 * @author         Enter your name here...    
 */
public class ProcessSweet implements SweetTags {

    /** _more_          */
    static Properties names = new Properties();

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public static String getName(String name) {
        String newName = (String) names.get(name);
        if (newName != null) {
            return newName;
        }

        String[] ltrs = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        };
        for (String ltr : ltrs) {
            name = name.replaceAll(ltr, " " + ltr);
        }
        return name.trim();
    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {

        Properties topLevelMap = new Properties();
        topLevelMap.load(
            new FileInputStream(new File("toplevel.properties")));
        names.load(new FileInputStream(new File("names.properties")));

        File            f            = new File("owl");
        StringBuffer    xml          = new StringBuffer(XmlUtil.XML_HEADER);
        List<String[]>  links        = new ArrayList<String[]>();
        StringBuffer    associations = new StringBuffer(XmlUtil.XML_HEADER);
        HashSet<String> seen         = new HashSet<String>();
        xml.append("<entries>\n");

        int    cnt             = 0;
        String currentTopLevel = null;
        for (File file : f.listFiles()) {
            if ( !file.toString().endsWith(".owl")) {
                continue;
            }
            cnt++;
            //            if(cnt>20) break;
            String group =
                IOUtil.stripExtension(IOUtil.getFileTail(file.toString()));
            //currentTopLevelGroup
            String topLevelLabel = (String) topLevelMap.get(group);
            if (topLevelLabel != null) {
                xml.append(XmlUtil.tag("entry",
                                       XmlUtil.attrs("type",
                                           "ontology.owl.group", "name",
                                           getName(topLevelLabel), "id",
                                           group)));
                currentTopLevel = group;
            } else {
                if ( !group.startsWith(currentTopLevel)) {
                    System.err.println("?:" + group + " " + currentTopLevel);
                }
                String name = getName(group.replace(currentTopLevel, ""));
                //                System.out.println(name+"="+name2.trim());
                xml.append(XmlUtil.tag("entry",
                                       XmlUtil.attrs("type",
                                           "ontology.owl.group", "name",
                                           name, "id", group, "parent",
                                           currentTopLevel)));
            }
            xml.append("\n");
            //            if(true)continue;
            //            System.err.println ("processing:" + file);
            Element root = XmlUtil.getRoot(file.toString(),
                                           ProcessSweet.class);
            if (root == null) {
                System.err.println("failed to read:" + file);
                continue;
            }
            NodeList children = XmlUtil.getElements(root);
            for (int i = 0; i < children.getLength(); i++) {
                Element node = (Element) children.item(i);
                String  tag  = node.getTagName();
                if (tag.equals(TAG_OWL_CLASS)) {
                    if ( !XmlUtil.hasAttribute(node, ATTR_RDF_ABOUT)) {
                        continue;
                    }
                    String about = XmlUtil.getAttribute(node, ATTR_RDF_ABOUT,
                                       "");
                    about = about.replace("#", "");
                    String desc = null;
                    seen.add(about);
                    NodeList children2 = XmlUtil.getElements(node);
                    for (int j = 0; j < children2.getLength(); j++) {
                        Element child     = (Element) children2.item(j);
                        String  childName = child.getTagName();
                        if (childName.equals(TAG_RDFS_SUBCLASSOF)
                                || childName.equals(TAG_OWL_DISJOINTWITH)
                                || childName.equals(
                                    TAG_OWL_EQUIVALENTCLASS)) {
                            if ( !XmlUtil.hasAttribute(child,
                                    ATTR_RDF_RESOURCE)) {
                                continue;
                            }
                            String resource = XmlUtil.getAttribute(child,
                                                  ATTR_RDF_RESOURCE);
                            int idx = resource.indexOf("#");
                            if (idx >= 0) {
                                resource = resource.substring(idx + 1);
                            }
                            links.add(new String[] { about, resource,
                                    childName });
                        } else if (childName.equals(TAG_RDFS_COMMENT)) {
                            desc = XmlUtil.getChildText(child);
                        } else if (childName.equals(TAG_RDFS_LABEL)) {}
                        else {
                            System.err.println("n/a:" + childName);
                        }
                    }
                    StringBuffer childTags = new StringBuffer();
                    if (desc != null) {
                        childTags.append(XmlUtil.tag("description", "",
                                XmlUtil.getCdata(desc.toString())));
                    }
                    xml.append(
                        XmlUtil.tag(
                            "entry",
                            XmlUtil.attrs(
                                "type", "ontology.owl.class", "name",
                                getName(about), "id", about, "parent",
                                group), childTags.toString()));
                    xml.append("\n");
                }
            }
        }
        for (String[] tuple : links) {
            String from = tuple[0];
            String to   = tuple[1];
            String type = tuple[2];
            if ( !seen.contains(to)) {
                continue;
            }
            xml.append(XmlUtil.tag("association",
                                   XmlUtil.attrs("from", from, "to", to,
                                       "type", type)));
            xml.append("\n");
        }
        xml.append("</entries>\n");
        IOUtil.writeFile("entries.xml", xml.toString());

    }

}
