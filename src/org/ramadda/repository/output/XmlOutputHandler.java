/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
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

package org.ramadda.repository.output;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.sql.Clause;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.io.File;

import java.net.*;

import java.sql.ResultSet;
import java.sql.Statement;




import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class XmlOutputHandler extends OutputHandler {

    /** _more_ */
    public static final OutputType OUTPUT_XML =
        new OutputType("XML", "xml.xml",
                       OutputType.TYPE_FEEDS | OutputType.TYPE_FORSEARCH, "",
                       ICON_XML);


    /** _more_ */
    public static final OutputType OUTPUT_XMLENTRY =
        new OutputType("XML Entry", "xml.xmlentry",
                       OutputType.TYPE_FEEDS | OutputType.TYPE_FORSEARCH, "",
                       ICON_XML);



    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public XmlOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_XML);
        addType(OUTPUT_XMLENTRY);
    }









    /**
     * _more_
     *
     * @param output _more_
     *
     * @return _more_
     */
    public String getMimeType(OutputType output) {
        if (output.equals(OUTPUT_XML) || output.equals(OUTPUT_XMLENTRY)) {
            return repository.getMimeTypeFromSuffix(".xml");
        }

        return super.getMimeType(output);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        Document doc  = XmlUtil.makeDocument();
        Element  root = getEntryTag(request, entry, null, doc, null, false,
                                   true);
        StringBuffer sb = new StringBuffer(XmlUtil.toString(root));

        return new Result("", sb, repository.getMimeTypeFromSuffix(".xml"));
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {

        if (outputType.equals(OUTPUT_XMLENTRY)) {
            return outputEntry(request, outputType, group);
        }

        Document doc  = XmlUtil.makeDocument();
        Element  root = getGroupTag(request, group, doc, null);
        for (Entry subgroup : subGroups) {
            getGroupTag(request, subgroup, doc, root);
        }
        for (Entry entry : entries) {
            getEntryTag(request, entry, null, doc, root, false, true);
        }
        StringBuffer sb = new StringBuffer(XmlUtil.toString(root));

        return new Result("", sb, repository.getMimeTypeFromSuffix(".xml"));
    }



    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     * @param zos _more_
     * @param doc _more_
     * @param parent _more_
     * @param forExport _more_
     * @param includeParentId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Element getEntryTag(Request request, Entry entry,
                               ZipOutputStream zos, Document doc,
                               Element parent, boolean forExport,
                               boolean includeParentId)
            throws Exception {

        Element node = XmlUtil.create(doc, TAG_ENTRY, parent, new String[] {
            ATTR_ID, entry.getId(), ATTR_NAME, entry.getName(), ATTR_PARENT,
            (includeParentId
             ? entry.getParentEntryId()
             : ""), ATTR_TYPE, entry.getTypeHandler().getType(),
            ATTR_ISGROUP, "" + entry.isGroup(), ATTR_FROMDATE,
            getRepository().formatDate(new Date(entry.getStartDate())),
            ATTR_TODATE,
            getRepository().formatDate(new Date(entry.getEndDate())),
            ATTR_CREATEDATE,
            getRepository().formatDate(new Date(entry.getCreateDate()))
        });


        if (entry.hasAltitude()) {
            node.setAttribute(ATTR_ALTITUDE, "" + entry.getAltitude());
        } else {
            if (entry.hasAltitudeBottom()) {
                node.setAttribute(ATTR_ALTITUDE_BOTTOM,
                                  "" + entry.getAltitudeBottom());
            }
            if (entry.hasAltitudeTop()) {
                node.setAttribute(ATTR_ALTITUDE_TOP,
                                  "" + entry.getAltitudeTop());
            }
        }

        if (entry.hasNorth()) {
            node.setAttribute(ATTR_NORTH, "" + entry.getNorth());
        }
        if (entry.hasSouth()) {
            node.setAttribute(ATTR_SOUTH, "" + entry.getSouth());
        }
        if (entry.hasEast()) {
            node.setAttribute(ATTR_EAST, "" + entry.getEast());
        }
        if (entry.hasWest()) {
            node.setAttribute(ATTR_WEST, "" + entry.getWest());
        }

        if (entry.getResource().isDefined()) {
            Resource resource = entry.getResource();
            if (forExport) {
                if (resource.isUrl()) {
                    XmlUtil.setAttributes(node, new String[] { ATTR_URL,
                            resource.getPath() });
                }
            } else {
                XmlUtil.setAttributes(node, new String[] { ATTR_RESOURCE,
                        resource.getPath(), ATTR_RESOURCE_TYPE,
                        resource.getType() });
                String md5 = resource.getMd5();
                if (md5 != null) {
                    node.setAttribute(ATTR_MD5, md5);
                }
                long filesize = resource.getFileSize();
                if (filesize >= 0) {
                    node.setAttribute(ATTR_FILESIZE, "" + filesize);
                }
            }

            //Add the service nodes
            if ( !forExport) {
                for (OutputHandler outputHandler :
                        getRepository().getOutputHandlers()) {
                    outputHandler.addToEntryNode(request, entry, node);
                }

                if (getRepository().getAccessManager().canAccessFile(request,
                        entry)) {
                    node.setAttribute(ATTR_FILESIZE,
                                      "" + entry.getResource().getFileSize());
                    String url =
                        getRepository().getEntryManager().getEntryResourceUrl(
                            request, entry, true);
                    Element serviceNode = XmlUtil.create(TAG_SERVICE, node);
                    XmlUtil.setAttributes(serviceNode,
                                          new String[] { ATTR_TYPE,
                            SERVICE_FILE, ATTR_URL, url });
                }
            }
        }


        if ((entry.getDescription() != null)
                && (entry.getDescription().length() > 0)) {
            Element descNode = XmlUtil.create(doc, TAG_DESCRIPTION, node);
            descNode.setAttribute("encoded", "true");
            descNode.appendChild(XmlUtil.makeCDataNode(doc,
                    entry.getDescription(), true));
        }
        getMetadataManager().addMetadata(request, entry, zos, doc, node);
        entry.getTypeHandler().addToEntryNode(entry, node);

        return node;
    }


    /**
     * _more_
     *
     *
     * @param request _more_
     * @param group _more_
     * @param doc _more_
     * @param parent _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Element getGroupTag(Request request, Entry group, Document doc,
                                Element parent)
            throws Exception {
        Element node = getEntryTag(request, group, null, doc, parent, false,
                                   true);
        boolean canDoNew = getAccessManager().canDoAction(request, group,
                               Permission.ACTION_NEW);
        boolean canDoUpload = getAccessManager().canDoAction(request, group,
                                  Permission.ACTION_UPLOAD);
        node.setAttribute(ATTR_CANDONEW, "" + canDoNew);
        node.setAttribute(ATTR_CANDOUPLOAD, "" + canDoUpload);

        return node;

    }


}
