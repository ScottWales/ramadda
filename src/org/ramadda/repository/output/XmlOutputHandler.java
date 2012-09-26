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


import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Resource;
import org.ramadda.repository.Result;
import org.ramadda.repository.auth.Permission;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ucar.unidata.xml.XmlUtil;


/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class XmlOutputHandler extends OutputHandler {

    /** XML Output type */
    public static final OutputType OUTPUT_XML =
        new OutputType("XML", "xml.xml",
                       OutputType.TYPE_FEEDS | OutputType.TYPE_FORSEARCH, "",
                       ICON_XML);


    /** XML Entry output type */
    public static final OutputType OUTPUT_XMLENTRY =
        new OutputType("XML Entry", "xml.xmlentry",
                       OutputType.TYPE_FEEDS | OutputType.TYPE_FORSEARCH, "",
                       ICON_XML);



    /**
     * Create an XML output handler
     *
     * @param repository   the Repository
     * @param element      the Element
     * @throws Exception   problem creating the handler
     */
    public XmlOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_XML);
        addType(OUTPUT_XMLENTRY);
    }


    /**
     * Get the MIME type for the output type
     *
     * @param output  the output type
     *
     * @return  the mimetype
     */
    public String getMimeType(OutputType output) {
        if (output.equals(OUTPUT_XML) || output.equals(OUTPUT_XMLENTRY)) {
            return repository.getMimeTypeFromSuffix(".xml");
        }

        return super.getMimeType(output);
    }


    /**
     * Output the entry
     *
     * @param request   the Request
     * @param outputType the outputType
     * @param entry      the Entry
     *
     * @return  the Result
     *
     * @throws Exception problem creating the result
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
     * Output a group of entries
     *
     * @param request      the Request
     * @param outputType   the output type
     * @param group        the group Entry
     * @param subGroups    the subgroups
     * @param entries      Entrys in the group
     *
     * @return  the Result
     *
     * @throws Exception  couldn't generate the Result
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
     * Get the entry element as XML
     *
     *
     * @param request   the Request
     * @param entry     the Entry
     * @param zos       the output
     * @param doc       the document to add to
     * @param parent    the parent Entry
     * @param forExport true for export
     * @param includeParentId  true to include the parent ID
     *
     * @return  the XML
     *
     * @throws Exception problem creating the tag
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
     * Get the tag for the group
     *
     *
     * @param request   the Request
     * @param group     the group Entry
     * @param doc       the document to append to
     * @param parent    the parent Element
     *
     * @return the XML Element
     *
     * @throws Exception  unable to create group tag
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
