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
public class ProcessSweet {



    /** _more_ */
    static Properties names = new Properties();

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public static String getName(String name) {
        int idx = name.indexOf("#");
        if (idx > 0) {
            name = name.substring(idx + 1);
        }


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
            IOUtil.getInputStream(
                "/org/ramadda/ontology/toplevel.properties",
                ProcessSweet.class));
        names.load(
            IOUtil.getInputStream(
                "/org/ramadda/ontology/names.properties",
                ProcessSweet.class));

        File            f            = new File("owl");
        StringBuffer    xml          = new StringBuffer(XmlUtil.XML_HEADER);
        List<String[]>  links        = new ArrayList<String[]>();
        StringBuffer    associations = new StringBuffer(XmlUtil.XML_HEADER);
        HashSet<String> seen         = new HashSet<String>();
        xml.append("<entries>\n");

        List<String> files = StringUtil.split(
                                 IOUtil.readContents(
                                     "/org/ramadda/ontology/sweetfiles.txt",
                                     ProcessSweet.class), "\n", true, true);


        int             cnt             = 0;
        String          currentTopLevel = null;
        List<EntryInfo> entries         = new ArrayList<EntryInfo>();
        Hashtable<String, EntryInfo> entryMap = new Hashtable<String,
                                                    EntryInfo>();
        HashSet<String> processed = new HashSet<String>();

        for (String file : files) {
            file = "owl/" + IOUtil.getFileTail(file);

            System.err.println("file:" + file);
            String filePrefix = IOUtil.getFileTail(file.toString());
            //            xml.append("<!-- entries from " + filePrefix +" -->\n");
            cnt++;
            //            if(cnt>10) break;
            String group =
                IOUtil.stripExtension(IOUtil.getFileTail(file.toString()));
            //currentTopLevelGroup
            String topLevelLabel = (String) topLevelMap.get(group);
            if (topLevelLabel != null) {
                xml.append(XmlUtil.tag("entry",
                                       XmlUtil.attrs("type",
                                           RdfUtil.TYPE_GROUP, "name",
                                           getName(topLevelLabel), "id",
                                           group)));
                xml.append("\n");
                processed.add(group);
                currentTopLevel = group;
            } else {
                if ( !group.startsWith(currentTopLevel)) {
                    System.err.println("?:" + group + " " + currentTopLevel);
                }
                String name = getName(group.replace(currentTopLevel, ""));
                //                System.out.println(name+"="+name2.trim());
                xml.append(XmlUtil.tag("entry",
                                       XmlUtil.attrs("type",
                                           RdfUtil.TYPE_GROUP, "name", name,
                                           "id", group, "parent",
                                           currentTopLevel)));
                xml.append("\n");
                processed.add(group);
            }
            Element root = XmlUtil.getRoot(file.toString(),
                                           ProcessSweet.class);
            if (root == null) {
                System.err.println("failed to read:" + file);
                continue;
            }
            NodeList        children = XmlUtil.getElements(root);
            HashSet<String> cats     = new HashSet<String>();

            for (int i = 0; i < children.getLength(); i++) {
                String  parent      = group;
                Element node        = (Element) children.item(i);
                String  tag         = node.getTagName();
                boolean okToProcess = false;
                if (tag.equals(RdfUtil.TAG_OWL_ONTOLOGY)) {
                    continue;
                }
                if (tag.equals(RdfUtil.TAG_OWL_OBJECTPROPERTY)) {
                    continue;
                }

                if (tag.equals(RdfUtil.TAG_OWL_CLASS)) {
                    okToProcess = true;
                } else {
                    int idx = tag.indexOf(":");
                    if (idx >= 0) {
                        String[] toks = tag.split(":");
                        if (XmlUtil.hasAttribute(root, "xmlns:" + toks[0])) {
                            okToProcess = true;
                            parent = XmlUtil.getAttribute(root,
                                    "xmlns:" + toks[0]) + "" + toks[1];
                            parent = parent.replace(
                                "http://sweet.jpl.nasa.gov/2.1/", "");
                        }
                    }
                }

                if ( !okToProcess) {
                    System.err.println(" unknown:" + filePrefix + "::" + tag);
                }

                if (okToProcess) {
                    String id;
                    if (XmlUtil.hasAttribute(node, RdfUtil.ATTR_RDF_ABOUT)) {
                        id = XmlUtil.getAttribute(node,
                                RdfUtil.ATTR_RDF_ABOUT, "").trim();
                        id = id.replace("http://sweet.jpl.nasa.gov/2.1/", "");
                        if (id.startsWith("#")) {
                            id = filePrefix + id;
                        }
                    } else if (XmlUtil.hasAttribute(node,
                            RdfUtil.ATTR_RDF_ID)) {
                        id = XmlUtil.getAttribute(node, RdfUtil.ATTR_RDF_ID,
                                "").trim();
                        if (id.startsWith("#")) {
                            id = filePrefix + id;
                        } else {
                            id = filePrefix + "#" + id;
                        }
                    } else {
                        continue;
                    }

                    String desc = null;
                    seen.add(id);
                    NodeList children2 = XmlUtil.getElements(node);
                    int      linkCnt   = 0;
                    for (int j = 0; j < children2.getLength(); j++) {
                        Element child     = (Element) children2.item(j);
                        String  childName = child.getTagName();
                        if (childName
                                .equals(RdfUtil
                                    .TAG_RDFS_SUBCLASSOF) || childName
                                        .equals(RdfUtil
                                            .TAG_OWL_DISJOINTWITH) || childName
                                                .equals(RdfUtil
                                                    .TAG_OWL_EQUIVALENTCLASS)) {
                            if ( !XmlUtil.hasAttribute(child,
                                    RdfUtil.ATTR_RDF_RESOURCE)) {
                                continue;
                            }
                            String resource = XmlUtil.getAttribute(child,
                                                  RdfUtil.ATTR_RDF_RESOURCE);
                            resource = resource.replace(
                                "http://sweet.jpl.nasa.gov/2.1/", "");
                            if (resource.startsWith("#")) {
                                resource = filePrefix + resource;
                            }
                            linkCnt++;
                            links.add(new String[] { id, resource,
                                    childName });
                        } else if (childName.equals(
                                RdfUtil.TAG_RDFS_COMMENT)) {
                            desc = XmlUtil.getChildText(child);
                        } else if (childName.equals(
                                RdfUtil.TAG_RDFS_LABEL)) {}
                        else {
                            //                            System.err.println("   ??:" + childName);
                        }
                    }
                    if ((linkCnt == 0) || (linkCnt > 1)) {
                        System.err.println(id + " link Cnt:" + linkCnt);
                    }
                    StringBuffer childTags = new StringBuffer();
                    if (desc != null) {
                        childTags.append(XmlUtil.tag("description", "",
                                XmlUtil.getCdata(desc.toString())));
                    }
                    EntryInfo entryInfo = new EntryInfo(id, getName(id),
                                              parent, childTags.toString());
                    entries.add(entryInfo);
                    entryMap.put(id, entryInfo);
                }
            }
        }


        List<EntryInfo> tmp = new ArrayList<EntryInfo>(entries);
        entries = new ArrayList<EntryInfo>();
        for (int i = 0; i < tmp.size(); i++) {
            EntryInfo entryInfo = tmp.get(i);
            EntryInfo parent    = entryMap.get(entryInfo.parentId);
            if (parent == null) {
                if ( !processed.contains(entryInfo.parentId)) {
                    System.err.println("No parent for entry:"
                                       + entryInfo.name + " parent="
                                       + entryInfo.parentId);
                    continue;
                }
            }
            entries.add(entryInfo);
        }


        for (EntryInfo entryInfo : entries) {
            process(xml, entryInfo, processed, entryMap);
        }


        for (String[] tuple : links) {
            String from = tuple[0];
            String to   = tuple[1];
            String type = tuple[2];
            if ( !seen.contains(to)) {
                System.err.println("Unknown to link:" + from + " " + to);
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

    /**
     * _more_
     *
     * @param xml _more_
     * @param entryInfo _more_
     * @param processed _more_
     * @param entryMap _more_
     */
    private static void process(StringBuffer xml, EntryInfo entryInfo,
                                HashSet<String> processed,
                                Hashtable<String, EntryInfo> entryMap) {
        if (processed.contains(entryInfo.id)) {
            return;
        }
        if ( !processed.contains(entryInfo.parentId)) {
            EntryInfo parent = entryMap.get(entryInfo.parentId);
            process(xml, parent, processed, entryMap);
        }
        processed.add(entryInfo.id);
        xml.append(
            XmlUtil.tag(
                "entry",
                XmlUtil.attrs(
                    "type", RdfUtil.TYPE_CLASS, "name", entryInfo.name, "id",
                    entryInfo.id, "parent",
                    entryInfo.parentId), entryInfo.childXml));
    }

    /**
     * Class description
     *
     *
     * @version        $version$, Sat, Nov 27, '10
     * @author         Enter your name here...    
     */
    public static class EntryInfo {

        /** _more_          */
        String id;

        /** _more_          */
        String parentId;

        /** _more_          */
        String name;

        /** _more_          */
        String childXml;

        /**
         * _more_
         *
         * @param id _more_
         * @param name _more_
         * @param parentId _more_
         * @param childXml _more_
         */
        public EntryInfo(String id, String name, String parentId,
                         String childXml) {
            this.id       = id;
            this.name     = name;
            this.parentId = parentId;
            this.childXml = childXml;
        }
    }

}
