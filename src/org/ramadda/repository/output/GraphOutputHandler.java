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
import org.ramadda.repository.database.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;



import org.w3c.dom.*;

import ucar.unidata.sql.*;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
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
import java.util.HashSet;
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

    public static final String ATTR_NAME = "name";
    public static final String ATTR_LABEL = "label";
    public static final String ATTR_URL = "url";
    public static final String ATTR_GRAPHURL = "graphurl";
    public static final String ATTR_NODEID = "nodeid";
    public static final String ATTR_ICON = "icon";
    public static final String ATTR_SOURCE = "source";
    public static final String ATTR_TARGET = "target";
    public static final String ATTR_SOURCE_ID = "source_id";
    public static final String ATTR_TARGET_ID = "target_id";
    public static final String ATTR_TITLE = "title";

    private void addNode(Request request, Entry entry, List<String>nodes, HashSet<String>  seen) throws Exception {
        if(entry ==null) return;
        if(seen.contains(entry.getId())) return;
        seen.add(entry.getId());
        String iconUrl = getEntryManager().getIconUrl(request,entry);
        String url = getRepository().getUrlBase() +"/graph/get?entryid=" + entry.getId();
        String entryUrl = request.entryUrl(getRepository().URL_ENTRY_SHOW, entry);
        nodes.add(Json.map(new String[]{
                    ATTR_NAME, entry.getName(),
                    ATTR_NODEID, entry.getId(),
                    ATTR_URL, entryUrl,
                    ATTR_GRAPHURL,url,
                    ATTR_ICON,iconUrl}));
    }


    private void addLink(Request request, Entry from, Entry to, String title, List<String>links) throws Exception {
        if(from == null || to == null) return;
        links.add(Json.map(new String[]{
                    ATTR_SOURCE_ID, from.getId(),
                    ATTR_TARGET_ID, to.getId(),
                    ATTR_TITLE, title}));
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

        List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);
        return outputGraphEntries(request, entry, entries);

    }

    public Result outputGraphEntries(Request request, Entry mainEntry, List<Entry> entries)
        throws Exception {
        StringBuffer sb = new StringBuffer();
        getGraph(request, mainEntry, entries, sb, 960,500);
        Result result = new Result(msg("Graph"), sb);
        addLinks(request, result, new State(mainEntry));
        return result;

    }


    public void getGraph(Request request, Entry mainEntry, List<Entry> entries, StringBuffer sb, int width, int height)
        throws Exception {
        StringBuffer js = new StringBuffer();
        String id = addPrefixHtml(sb, js, width, height);
        HashSet<String>  seen = new HashSet<String>();
        List<String> nodes   = new ArrayList<String>();
        List<String> links   = new ArrayList<String>();
        for(Entry entry: entries) {
            addNode(request, entry, nodes, seen);
            addNode(request, entry.getParentEntry(), nodes, seen);
            addLink(request,  entry.getParentEntry(), entry, "", links);
            getAssociations(request, entry,  nodes, links, seen);
        }

        addSuffixHtml(sb,  js,  id, nodes,links, width, height);
    }


    private int graphCnt=0;

    public String addPrefixHtml(StringBuffer sb, StringBuffer js, int width, int height) {
        String divId = "graph_" + (graphCnt++) ;
        js.append("function createGraph" + divId +"() {\n");
        sb.append(HtmlUtils.importJS(fileUrl("/d3/d3.v3.min.js")));
        sb.append(HtmlUtils.importJS(fileUrl("/d3/d3graph.js")));
        sb.append(HtmlUtils.tag(HtmlUtils.TAG_DIV, HtmlUtils.style("width:" + width +";height:" + height) +HtmlUtils.id(divId) + HtmlUtils.cssClass("graph-div")));
        return divId;
    }

    public void addSuffixHtml(StringBuffer sb, StringBuffer js, String id, List<String> nodes, List<String> links, int width, int height) {
        js.append("var nodes  = [\n");
        js.append(StringUtil.join(",\n", nodes));
        js.append("];\n");
        js.append("var links = [\n");
        js.append(StringUtil.join(",", links));
        js.append("];\n");
        js.append("return new D3Graph(\"#" + id +"\", nodes,links," + width +"," + height +");\n}\n");
        js.append("var " + id +" = createGraph" + id +"();\n");
        sb.append(HtmlUtils.script(js.toString()));
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
        subGroups.add(0, group);
        subGroups.addAll(entries);
        return outputGraphEntries(request, group, subGroups);
        //        return outputEntry(request, outputType, group);
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
        if(entry == null) return;
        String imageUrl = null;
        if (ImageUtils.isImage(entry.getResource().getPath())) {
            imageUrl =
                HtmlUtils.url(
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

        String  id         = (String) request.getId((String) null);
        Entry entry = getEntryManager().getEntry(request, id);
        if (entry == null) {
            throw new IllegalArgumentException("Could not find entry:" + id);

        }

        StringBuffer js = new StringBuffer();
        StringBuffer linkJS = new StringBuffer();

        List<String> nodes   = new ArrayList<String>();
        List<String> links   = new ArrayList<String>();
        HashSet<String>  seen = new HashSet<String>();

        List<Entry> entries = new ArrayList<Entry>();
        if (entry.isGroup()) {
            entries.addAll(getEntryManager().getChildren(request,
                                                         entry));
        }

        addNode(request, entry.getParentEntry(), nodes, seen);
        addLink(request, entry.getParentEntry(), entry, "", links);

        for(Entry e: entries) {
            addNode(request, e, nodes, seen);
            addLink(request, entry, e, "", links);
        }


        getAssociations(request, entry,  nodes, links, seen);


        js.append("{\n");
        js.append("\"nodes\":[\n");
        js.append(StringUtil.join(",", nodes));
        js.append("]");
        js.append(",\n");
        js.append("\"links\":[\n");
        js.append(StringUtil.join(",", links));
        js.append("]\n");
        js.append("}\n");


        System.err.println(js);
        return new Result(BLANK, js,
                          getRepository().getMimeTypeFromSuffix(".json"));

    }


    private void  getAssociations(Request request, Entry entry, List<String> nodes, List<String> links, HashSet<String>  seen) throws Exception {
        List<Association> associations =
            getAssociationManager().getAssociations(request, entry.getId());
        for (Association association : associations) {
            Entry   from  = null;
            Entry   to  = null;
            if (association.getFromId().equals(entry.getId())) {
                from = getEntryManager().getEntry(request,
                                                  association.getToId());
                addNode(request, from, nodes, seen);
                addLink(request, from, entry, association.getType(), links);
            } else {
                to = getEntryManager().getEntry(request,
                                                association.getFromId());
                addNode(request, to, nodes, seen);
                addLink(request,  entry, from, association.getType(), links);
            }
        }

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
