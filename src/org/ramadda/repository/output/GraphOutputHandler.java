/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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
import org.ramadda.repository.database.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;

import ucar.unidata.sql.*;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
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
public class GraphOutputHandler extends OutputHandler {

    /** _more_ */
    public static final OutputType OUTPUT_GRAPH = new OutputType("Graph",
                                                      "graph.graph",
                                                      OutputType.TYPE_VIEW,
                                                      "", ICON_GRAPH);



    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public GraphOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_GRAPH);
    }






    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if (state.getEntry() != null) {
            links.add(makeLink(request, state.getEntry(), OUTPUT_GRAPH));
        }
    }



    /** _more_ */
    static long cnt = System.currentTimeMillis();

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
        String graphAppletTemplate =
            getRepository().getResource(PROP_HTML_GRAPHAPPLET);

        String counter = "" + (cnt++);
        //        counter = "_newjar";
        graphAppletTemplate = graphAppletTemplate.replace("${counter}",
                counter);
        String type = request.getString(ARG_NODETYPE, (String) null);
        if (type == null) {
            type = entry.getTypeHandler().getNodeType();
        }
        String html = StringUtil.replace(graphAppletTemplate, "${id}",
                                         HtmlUtil.urlEncode(entry.getId()));
        html = StringUtil.replace(html, "${root}",
                                  getRepository().getUrlBase());
        html = StringUtil.replace(html, "${type}", HtmlUtil.urlEncode(type));
        StringBuffer sb = new StringBuffer();
        sb.append(html);
        Result result = new Result(msg("Graph"), sb);
        addLinks(request, result, new State(entry));
        return result;

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
        return outputEntry(request, outputType, group);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param id _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    protected void getAssociationsGraph(Request request, String id,
                                        StringBuffer sb)
            throws Exception {
        List<Association> associations =
            getAssociationManager().getAssociations(request, id);
        for (Association association : associations) {
            Entry   other  = null;
            boolean isTail = true;
            if (association.getFromId().equals(id)) {
                other = getEntryManager().getEntry(request,
                        association.getToId());
                isTail = true;
            } else {
                other = getEntryManager().getEntry(request,
                        association.getFromId());
                isTail = false;
            }

            if (other != null) {
                String imageAttr = XmlUtil.attrs("imagepath",
                                       getEntryManager().getIconUrl(request,
                                           other));

                sb.append(
                    XmlUtil.tag(
                        TAG_NODE,
                        imageAttr
                        + XmlUtil.attrs(
                            ATTR_TYPE, other.getTypeHandler().getNodeType(),
                            ATTR_ID, other.getId(), ATTR_TOOLTIP,
                            getTooltip(other), ATTR_TITLE,
                            getGraphNodeTitle(other.getName()))));
                String fromId = association.getFromId();
                String toId   = association.getToId();

                sb.append(XmlUtil.tag(TAG_EDGE,
                                      XmlUtil.attrs(ATTR_TITLE,
                                          association.getType(), ATTR_TYPE,
                                          "link", ATTR_FROM, fromId, ATTR_TO,
                                          toId)));

            }
        }

        //        System.err.println(sb);

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    private void addNodeTag(Request request, StringBuffer sb, Entry entry)
            throws Exception {
        String imageUrl = null;
        if (ImageUtils.isImage(entry.getResource().getPath())) {
            imageUrl =
                HtmlUtil.url(
                    getRepository().URL_ENTRY_GET + entry.getId()
                    + IOUtil.getFileExtension(
                        entry.getResource().getPath()), ARG_ENTRYID,
                            entry.getId(), ARG_IMAGEWIDTH, "75");
        } else {
            List<String> urls = new ArrayList<String>();
            getMetadataManager().getThumbnailUrls(request, entry, urls);
            if (urls.size() > 0) {
                imageUrl = urls.get(0) + "&thumbnail=true";
            }
        }

        String imageAttr =
            XmlUtil.attrs("imagepath",
                          getEntryManager().getIconUrl(request, entry));

        String nodeType = entry.getTypeHandler().getNodeType();
        if (imageUrl != null) {
            nodeType = "imageentry";
        }
        String attrs = imageAttr
                       + XmlUtil.attrs(ATTR_TYPE, nodeType, ATTR_ID,
                                       entry.getId(), ATTR_TOOLTIP,
                                       getTooltip(entry), ATTR_TITLE,
                                       getGraphNodeTitle(entry.getName()));

        if (imageUrl != null) {
            attrs = attrs + " " + XmlUtil.attr("image", imageUrl);
        }
        //        System.err.println(entry.getName() + " " + attrs);
        sb.append(XmlUtil.tag(TAG_NODE, attrs));
        sb.append("\n");

    }


    /**
     * _more_
     *
     * @param sb _more_
     * @param from _more_
     * @param to _more_
     * @param type _more_
     */
    private void addEdgeTag(StringBuffer sb, Entry from, Entry to,
                            String type) {
        addEdgeTag(sb, from.getId(), to.getId(), type);
    }

    /**
     * _more_
     *
     * @param sb _more_
     * @param from _more_
     * @param to _more_
     * @param type _more_
     */
    private void addEdgeTag(StringBuffer sb, String from, String to,
                            String type) {
        sb.append(XmlUtil.tag(TAG_EDGE,
                              XmlUtil.attrs(ATTR_TYPE, type, ATTR_FROM, from,
                                            ATTR_TO, to)));
        sb.append("\n");

    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processGraphGet(Request request) throws Exception {

        String graphXmlTemplate =
            getRepository().getResource(PROP_HTML_GRAPHTEMPLATE);
        String  id         = (String) request.getId((String) null);
        String  originalId = id;
        String  type = (String) request.getString(ARG_NODETYPE,
                           (String) null);
        int     cnt        = 0;
        int     actualCnt  = 0;

        int     skip       = request.get(ARG_SKIP, 0);
        boolean haveSkip   = false;

        if (id.startsWith("skip_")) {
            haveSkip = true;
            //skip_tag_" +(cnt+skip)+"_"+id;
            List toks = StringUtil.split(id, "_", true, true);
            type = (String) toks.get(1);
            skip = new Integer((String) toks.get(2)).intValue();
            toks.remove(0);
            toks.remove(0);
            toks.remove(0);
            id = StringUtil.join("_", toks);
        }

        int MAX_EDGES = 15;
        if (id == null) {
            throw new IllegalArgumentException("Could not find id:"
                    + request);
        }

        Entry entry = getEntryManager().getEntry(request, id);
        if (entry == null) {
            throw new IllegalArgumentException("Could not find entry:" + id);

        }

        TypeHandler typeHandler = entry.getTypeHandler();
        if (type == null) {
            type = typeHandler.getNodeType();
        }
        StringBuffer sb = new StringBuffer();
        addNodeTag(request, sb, entry);
        getAssociationsGraph(request, entry.getId(), sb);
        Entry parent = entry.getParentEntry();
        if (parent != null) {
            addNodeTag(request, sb, parent);
            addEdgeTag(sb, parent, entry, "groupedby");
        }

        if (entry.isGroup()) {
            List<Entry> subGroups =
                getEntryManager().getGroups(
                    request,
                    Clause.eq(
                        Tables.ENTRIES.COL_PARENT_GROUP_ID, entry.getId()));

            cnt       = 0;
            actualCnt = 0;
            for (Entry subGroup : subGroups) {
                if (++cnt <= skip) {
                    continue;
                }
                actualCnt++;

                String imageAttr = XmlUtil.attrs("imagepath",
                                       getEntryManager().getIconUrl(request,
                                           subGroup));

                sb.append(
                    XmlUtil.tag(
                        TAG_NODE,
                        imageAttr
                        + XmlUtil.attrs(
                            ATTR_TYPE, NODETYPE_GROUP, ATTR_ID,
                            subGroup.getId(), ATTR_TOOLTIP,
                            getTooltip(subGroup), ATTR_TITLE,
                            getGraphNodeTitle(subGroup.getName()))));

                addEdgeTag(sb, (haveSkip
                                ? originalId
                                : entry.getId()), subGroup.getId(),
                                "groupedby");
                if (actualCnt >= MAX_EDGES) {
                    String skipId = "skip_" + type + "_" + (actualCnt + skip)
                                    + "_" + id;
                    sb.append(XmlUtil.tag(TAG_NODE,
                                          XmlUtil.attrs(ATTR_TYPE, "skip",
                                              ATTR_ID, skipId, ATTR_TITLE,
                                                  "...")));
                    addEdgeTag(sb, "etc", originalId, skipId);
                    break;
                }
            }


            List<Entry> children = getEntryManager().getChildren(request,
                                       entry);
            cnt       = 0;
            actualCnt = 0;
            for (Entry child : children) {
                cnt++;
                if (cnt <= skip) {
                    continue;
                }
                actualCnt++;
                addNodeTag(request, sb, child);
                addEdgeTag(sb, (haveSkip
                                ? originalId
                                : entry.getId()), child.getId(), "groupedby");
                if (actualCnt >= MAX_EDGES) {
                    String skipId = "skip_" + type + "_" + (actualCnt + skip)
                                    + "_" + id;
                    sb.append(XmlUtil.tag(TAG_NODE,
                                          XmlUtil.attrs(ATTR_TYPE, "skip",
                                              ATTR_ID, skipId, ATTR_TITLE,
                                                  "...")));
                    addEdgeTag(sb, originalId, skipId, "etc");
                    break;
                }
            }
        }

        String xml = StringUtil.replace(graphXmlTemplate, "${content}",
                                        sb.toString());
        xml = StringUtil.replace(xml, "${root}",
                                 getRepository().getUrlBase());
        //        System.err.println(xml);
        return new Result(BLANK, new StringBuffer(xml),
                          getRepository().getMimeTypeFromSuffix(".xml"));

    }



    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private String getGraphNodeTitle(String s) {
        if (s.length() > 40) {
            s = s.substring(0, 39) + "...";
        }
        return s;
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    private String getTooltip(Entry entry) {
        if (true) {
            return entry.getName();
        }
        String desc = entry.getDescription();
        if ((desc == null) || (desc.length() == 0)) {
            desc = entry.getName();
        }
        return desc;
    }





}
