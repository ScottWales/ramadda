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

package org.ramadda.repository.output;


import org.w3c.dom.Element;

import org.ramadda.repository.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.auth.*;

import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.BufferMapList;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import org.ramadda.util.WikiUtil;

import java.io.*;

import java.io.File;


import java.net.*;


import java.text.SimpleDateFormat;

import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 * Class SqlUtil _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class OutputHandler extends RepositoryManager {

    /** _more_ */
    public static final String ATTR_MAXCONNECTIONS = "maxconnections";

    /** _more_ */
    public static final String LABEL_LINKS = "Actions";

    /** _more_ */
    public static final OutputType OUTPUT_HTML =
        new OutputType("Information", "default.html",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_INFORMATION);

    public static final OutputType OUTPUT_TREE =
        new OutputType("Information", "default.html",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_TREE);


    /** _more_ */
    private String name;

    /** _more_ */
    private List<OutputType> types = new ArrayList<OutputType>();

    /** _more_ */
    private Hashtable<String, OutputType> typeMap = new Hashtable<String,
                                                        OutputType>();


    /** _more_ */
    private int maxConnections = -1;

    /** _more_ */
    private int numberOfConnections = 0;

    /** _more_ */
    private int totalCalls = 0;

    /**
     * _more_
     *
     * @param repository _more_
     * @param name _more_
     *
     * @throws Exception _more_
     */
    public OutputHandler(Repository repository, String name)
            throws Exception {
        super(repository);
        this.name = name;
    }


    public boolean allowSpiders() {
        return false;
    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public OutputType findOutputType(String id) {
        return typeMap.get(id);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public OutputHandler(Repository repository, Element element)
            throws Exception {
        this(repository,
             XmlUtil.getAttribute(element, ATTR_NAME, (String) null));
        maxConnections = XmlUtil.getAttribute(element, ATTR_MAXCONNECTIONS,
                maxConnections);


    }

    /**
     * _more_
     */
    public void init() {}


    /**
     * _more_
     */
    public void clearCache() {}

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param node _more_
     *
     * @throws Exception _more_
     */
    public void addToEntryNode(Request request, Entry entry, Element node)
            throws Exception {}


    /**
     * _more_
     *
     * @param type _more_
     */
    public void addType(OutputType type) {
        type.setGroupName(name);
        types.add(type);
        typeMap.put(type.getId(), type);
        repository.addOutputType(type);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<OutputType> getTypes() {
        return types;
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
        if (name == null) {
            name = Misc.getClassName(getClass());
        }
        return name;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     */
    public boolean showingAll(Request request, List<Entry> subGroups,
                              List<Entry> entries) {
        int cnt = subGroups.size() + entries.size();
        int max = request.get(ARG_MAX, VIEW_MAX_ROWS);
        if ((cnt > 0) && ((cnt == max) || request.defined(ARG_SKIP))) {
            return false;
        }
        return true;
    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public AuthorizationMethod getAuthorizationMethod(Request request) {
        return AuthorizationMethod.AUTH_HTML;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param subGroups _more_
     * @param entries _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void showNext(Request request, List<Entry> subGroups,
                         List<Entry> entries, StringBuffer sb)
            throws Exception {
        int cnt = subGroups.size() + entries.size();
        showNext(request, cnt, sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param cnt _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void showNext(Request request, int cnt, StringBuffer sb)
            throws Exception {

        int max = request.get(ARG_MAX, VIEW_MAX_ROWS);
        //        System.err.println ("cnt:" + cnt + " " + max);
        if ((cnt > 0) && ((cnt == max) || request.defined(ARG_SKIP))) {
            int skip = Math.max(0, request.get(ARG_SKIP, 0));
            sb.append(msgLabel("Showing") + (skip + 1) + "-" + (skip + cnt));
            sb.append(HtmlUtil.space(4));
            List<String> toks = new ArrayList<String>();
            if (skip > 0) {
                toks.add(HtmlUtil.href(request.getUrl(ARG_SKIP) + "&"
                                       + ARG_SKIP + "="
                                       + (skip - max), msg("Previous...")));
            }
            if (cnt >= max) {
                toks.add(HtmlUtil.href(request.getUrl(ARG_SKIP) + "&"
                                       + ARG_SKIP + "="
                                       + (skip + max), msg("Next...")));
            }
            request.put(ARG_MAX, "" + (max + VIEW_MAX_ROWS));
            if (cnt >= max) {
                toks.add(HtmlUtil.href(request.getUrl(), msg("View More")));
                request.put(ARG_MAX, "" + (max / 2));
                toks.add(HtmlUtil.href(request.getUrl(), msg("View Less")));
            }
            if (toks.size() > 0) {
                sb.append(StringUtil.join(HtmlUtil.span("&nbsp;|&nbsp;",
                        HtmlUtil.cssClass("separator")), toks));
            }
            request.put(ARG_MAX, max);
        }

    }




    /**
     * _more_
     *
     *
     * @param output _more_
     *
     * @return _more_
     */
    public boolean canHandleOutput(OutputType output) {
        for (OutputType type : types) {
            if (type.equals(output)) {
                return true;
            }
        }
        return false;
    }

    /**
     * _more_
     *
     * @param sb _more_
     */
    public void getSystemStats(StringBuffer sb) {
        if (totalCalls > 0) {
            StringBuffer stats = new StringBuffer();
            for (OutputType outputType : types) {
                if (outputType.getNumberOfCalls() > 0) {
                    stats.append(outputType.getLabel() + " #"
                                 + msgLabel("calls")
                                 + outputType.getNumberOfCalls()
                                 + HtmlUtil.br());
                }
            }

            sb.append(HtmlUtil.formEntryTop(msgLabel(name),
                                            stats.toString()));

        }
    }



    /**
     * Class State _more_
     *
     *
     * @author IDV Development Team
     * @version $Revision: 1.3 $
     */
    public static class State {

        /** _more_ */
        public static final int FOR_UNKNOWN = 0;

        /** _more_ */
        public static final int FOR_HEADER = 1;

        /** _more_ */
        public int forWhat = FOR_UNKNOWN;

        /** _more_ */
        public Entry entry;

        /** _more_ */
        public Entry group;

        /** _more_ */
        public List<Entry> subGroups;

        /** _more_ */
        public List<Entry> entries;

        /** _more_ */
        public List<Entry> allEntries;

        /**
         * _more_
         *
         * @param entry _more_
         */
        public State(Entry entry) {
            if (entry != null) {
                if (entry.isGroup()) {
                    group          = (Entry) entry;
                    this.subGroups = group.getSubGroups();
                    this.entries   = group.getSubEntries();
                } else {
                    this.entry = entry;
                }
            }

        }

        /**
         * _more_
         *
         * @param group _more_
         * @param subGroups _more_
         * @param entries _more_
         */
        public State(Entry group, List<Entry> subGroups,
                     List<Entry> entries) {
            this.group     = group;
            this.entries   = entries;
            this.subGroups = subGroups;
        }


        /**
         * _more_
         *
         *
         * @param group _more_
         * @param entries _more_
         */
        public State(Entry group, List<Entry> entries) {
            this.group   = group;
            this.entries = entries;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean isDummyGroup() {
            Entry entry = getEntry();
            if (entry == null) {
                return false;
            }
            if ( !entry.isGroup()) {
                return false;
            }
            return entry.isDummy();
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public boolean forHeader() {
            return forWhat == FOR_HEADER;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public List<Entry> getAllEntries() {
            if (allEntries == null) {
                allEntries = new ArrayList();
                if (subGroups != null) {
                    allEntries.addAll(subGroups);
                }
                if (entries != null) {
                    allEntries.addAll(entries);
                }
                if (entry != null) {
                    allEntries.add(entry);
                }
            }
            return (List<Entry>) allEntries;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public Entry getEntry() {
            if (entry != null) {
                return entry;
            }
            return group;
        }

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param title _more_
     * @param sb _more_
     * @param state _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result makeLinksResult(Request request, String title,
                                  StringBuffer sb, State state)
            throws Exception {
        Result result = new Result(title, sb);
        addLinks(request, result, state);
        return result;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param result _more_
     * @param state _more_
     *
     * @throws Exception _more_
     */
    public void addLinks(Request request, Result result, State state)
            throws Exception {
        state.forWhat = State.FOR_HEADER;
        if (state.getEntry().getDescription().indexOf("<nolinks>") >= 0) {
            return;
        }
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
            throws Exception {}



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param outputType _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Link makeLink(Request request, Entry entry, OutputType outputType)
            throws Exception {
        return makeLink(request, entry, outputType, "");
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param outputType _more_
     * @param suffix _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Link makeLink(Request request, Entry entry, OutputType outputType,
                         String suffix)
            throws Exception {
        String url;
        if (entry == null) {
            url = HtmlUtil.url(getRepository().URL_ENTRY_SHOW + suffix,
                               ARG_OUTPUT, outputType.toString());
        } else {
            url = request.getEntryUrl(getRepository().URL_ENTRY_SHOW
                                      + suffix, entry);
            url = HtmlUtil.url(url, ARG_ENTRYID, entry.getId(), ARG_OUTPUT,
                               outputType.toString());
        }
        int linkType = OutputType.TYPE_ACTION;
        return new Link(url, (outputType.getIcon() == null)
                             ? null
                             : iconUrl(outputType.getIcon()), outputType
                                 .getLabel(), outputType);

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param links _more_
     * @param type _more_
     *
     * @throws Exception _more_
     */
    public void addOutputLink(Request request, Entry entry, List<Link> links,
                              OutputType type)
            throws Exception {
        links.add(new Link(request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                            entry, ARG_OUTPUT,
                                            type), iconUrl(type.getIcon()),
                                                type.getLabel(), type));

    }


    /**
     * _more_
     *
     *
     * @param method _more_
     * @return _more_
     */
    private Result notImplemented(String method) {
        throw new IllegalArgumentException("Method: " + method
                                           + " not implemented");
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
        return outputGroup(request, outputType,
                           getEntryManager().getDummyGroup(),
                           new ArrayList<Entry>(), entries);
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
        return notImplemented("outputGroup");
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public final Result xoutputGroup(Request request, Entry group,
                                     List<Entry> subGroups,
                                     List<Entry> entries)
            throws Exception {
        return null;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public final Result xoutputEntry(Request request, Entry entry)
            throws Exception {
        return null;
    }


    /**
     * _more_
     *
     * @param output _more_
     *
     * @return _more_
     */
    public String getMimeType(OutputType output) {
        return null;
    }






    /**
     * _more_
     *
     * @param request _more_
     * @param elementId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static String getGroupSelect(Request request, String elementId)
            throws Exception {
        return getSelect(request, elementId, "Select", false, "", null);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param elementId _more_
     * @param label _more_
     * @param allEntries _more_
     * @param type _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static String getSelect(Request request, String elementId,
                                   String label, boolean allEntries,
                                   String type)
            throws Exception {

        return getSelect(request, elementId, label, allEntries, type, null);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param elementId _more_
     * @param label _more_
     * @param allEntries _more_
     * @param type _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static String getSelect(Request request, String elementId,
                                   String label, boolean allEntries,
                                   String type, Entry entry)
            throws Exception {
        return getSelect(request, elementId, label, allEntries, type, entry,
                         true);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param elementId _more_
     * @param label _more_
     * @param allEntries _more_
     * @param type _more_
     * @param entry _more_
     * @param addClear _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static String getSelect(Request request, String elementId,
                                   String label, boolean allEntries,
                                   String type, Entry entry, boolean addClear)
            throws Exception {
        String event = HtmlUtil.call("selectInitialClick",
                                     "event," + HtmlUtil.squote(elementId)
                                     + "," + HtmlUtil.squote("" + allEntries)
                                     + "," + HtmlUtil.squote(type) + ","
                                     + ((entry != null)
                                        ? HtmlUtil.squote(entry.getId())
                                        : "null"));
        String clearEvent = HtmlUtil.call("clearSelect",
                                          HtmlUtil.squote(elementId));
        String link = HtmlUtil.mouseClickHref(event, label,
                          HtmlUtil.id(elementId + ".selectlink"));
        if (addClear) {
            link = link + " "
                   + HtmlUtil.mouseClickHref(clearEvent, "Clear",
                                             HtmlUtil.id(elementId
                                                 + ".selectlink"));
        }
        return link;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param target _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getSelectLink(Request request, Entry entry, String target)
            throws Exception {
        String       linkText = entry.getLabel();
        StringBuffer sb       = new StringBuffer();
        String       entryId  = entry.getId();
        String       icon     = getEntryManager().getIconUrl(request, entry);
        String       event;
        String       uid = "link_" + HtmlUtil.blockCnt++;
        String folderClickUrl =
            request.entryUrl(getRepository().URL_ENTRY_SHOW, entry) + "&"
            + HtmlUtil.args(new String[] {
            ARG_NOREDIRECT, "true", ARG_OUTPUT,
            request.getString(ARG_OUTPUT, "inline"), ATTR_TARGET, target,
            ARG_ALLENTRIES, request.getString(ARG_ALLENTRIES, "true"),
            ARG_SELECTTYPE, request.getString(ARG_SELECTTYPE, "")
        });

        String  message   = entry.isGroup()
                            ? "Click to open folder"
                            : "Click to view contents";
        boolean showArrow = true;
        String  prefix    = ( !showArrow
                              ? HtmlUtil.img(
                                  getRepository().iconUrl(ICON_BLANK), "",
                                  HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "10"))
                              : HtmlUtil.img(
                                  getRepository().iconUrl(
                                      ICON_TOGGLEARROWRIGHT), msg(message),
                                          HtmlUtil.id("img_" + uid)
                                          + HtmlUtil.onMouseClick(
                                              HtmlUtil.call(
                                                  "folderClick",
                                                  HtmlUtil.comma(
                                                      HtmlUtil.squote(uid),
                                                      HtmlUtil.squote(
                                                          folderClickUrl), HtmlUtil.squote(
                                                          iconUrl(
                                                              ICON_TOGGLEARROWDOWN)))))));


        String img = prefix + HtmlUtil.space(1) + HtmlUtil.img(icon);

        sb.append(img);
        sb.append(HtmlUtil.space(1));

        String type      = request.getString(ARG_SELECTTYPE, "");
        String elementId = entry.getId();
        String value     = (entry.isGroup()
                            ? ((Entry) entry).getFullName()
                            : entry.getName());
        value = value.replace("'", "\\'");


        sb.append(HtmlUtil.mouseClickHref(HtmlUtil.call("selectClick",
                HtmlUtil.comma(HtmlUtil.squote(target),
                               HtmlUtil.squote(entry.getId()),
                               HtmlUtil.squote(value),
                               HtmlUtil.squote(type))), linkText));

        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.div("",
                               HtmlUtil.attrs(HtmlUtil.ATTR_STYLE,
                                   "display:none;visibility:hidden",
                                   HtmlUtil.ATTR_CLASS, "folderblock",
                                   HtmlUtil.ATTR_ID, uid)));
        return sb.toString();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param contents _more_
     *
     * @return _more_
     */
    public Result makeAjaxResult(Request request, String contents) {
        StringBuffer xml = new StringBuffer("<content>\n");
        XmlUtil.appendCdata(xml, contents);
        xml.append("\n</content>");
        return new Result("", xml, "text/xml");
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param output _more_
     *
     * @return _more_
     */
    public List<Link> getNextPrevLinks(Request request, Entry entry,
                                       OutputType output) {
        Link       link;
        List<Link> links = new ArrayList<Link>();

        link = new Link(request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                         entry, ARG_OUTPUT, output,
                                         ARG_PREVIOUS,
                                         "true"), iconUrl(ICON_LEFT),
                                             "View Previous Entry");

        //        link.setLinkType(OutputType.TYPE_TOOLBAR);
        link.setLinkType(OutputType.TYPE_VIEW);
        links.add(link);
        link = new Link(request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                         entry, ARG_OUTPUT, output, ARG_NEXT,
                                         "true"), iconUrl(ICON_RIGHT),
                        "View Next Entry");
        link.setLinkType(OutputType.TYPE_VIEW);
        //        link.setLinkType(OutputType.TYPE_TOOLBAR);
        links.add(link);
        return links;
    }



    /**
     * _more_
     *
     * @param buffer _more_
     */
    public void addToSettingsForm(StringBuffer buffer) {}

    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void applySettings(Request request) throws Exception {}


    /** _more_ */
    public static int entryCnt = 0;


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public String getSortLinks(Request request) {
        StringBuffer sb           = new StringBuffer();
        String       oldOrderBy   = request.getString(ARG_ORDERBY,
                                        "fromdate");
        String       oldAscending = request.getString(ARG_ASCENDING, "false");
        String[]     order        = {
            "name", "true",
            msg("Name") + HtmlUtil.img(getRepository().iconUrl(ICON_UPARROW)),
            "Sort by name ascending", "name", "false",
            msg("Name")
            + HtmlUtil.img(getRepository().iconUrl(ICON_DOWNARROW)),
            "Sort by name descending", "fromdate", "true",
            msg("Date") + HtmlUtil.img(getRepository().iconUrl(ICON_UPARROW)),
            "Sort by date ascending", "fromdate", "false",
            msg("Date")
            + HtmlUtil.img(getRepository().iconUrl(ICON_DOWNARROW)),
            "Sort by date descending"
        };

        if(request.isMobile()) {
            sb.append(HtmlUtil.br());
        }
        sb.append(HtmlUtil.span(msgLabel("Sort"),
                                HtmlUtil.cssClass("sortlinkoff")));
        String entryIds = request.getString(ARG_ENTRYIDS, (String) null);
        //Swap out the long value
        if (entryIds != null) {
            String extraId = getRepository().getGUID();
            request.put(
                ARG_ENTRYIDS,
                getRepository().getSessionManager().putSessionExtra(
                    entryIds));
        }

        for (int i = 0; i < order.length; i += 4) {
            if (Misc.equals(order[i], oldOrderBy)
                    && Misc.equals(order[i + 1], oldAscending)) {
                sb.append(HtmlUtil.span(order[i + 2],
                                        HtmlUtil.cssClass("sortlinkon")));
            } else {
                request.put(ARG_ORDERBY, order[i]);
                request.put(ARG_ASCENDING, order[i + 1]);
                request.put(ARG_SHOWENTRYSELECTFORM, "true");
                String url = request.getUrl();
                sb.append(HtmlUtil.span(HtmlUtil.href(url, order[i + 2]),
                                        HtmlUtil.title(order[i + 3])
                                        + HtmlUtil.cssClass("sortlinkoff")));
            }
        }

        if (entryIds != null) {
            request.put(ARG_ENTRYIDS, entryIds);
        }

        request.remove(ARG_SHOWENTRYSELECTFORM);
        request.put(ARG_ORDERBY, oldOrderBy);
        request.put(ARG_ASCENDING, oldAscending);
        return sb.toString();

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     * @param hideIt _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String[] getEntryFormStart(Request request, List entries,
                                      boolean hideIt)
            throws Exception {
        if (hideIt) {
            hideIt = !request.get(ARG_SHOWENTRYSELECTFORM, false);
        }


        String       base   = "toggleentry" + (entryCnt++);
        String       formId = "entryform_" + (HtmlUtil.blockCnt++);
        StringBuffer formSB = new StringBuffer();
        formSB.append(request.formPost(getRepository().URL_ENTRY_GETENTRIES,
                                       HtmlUtil.id(formId)));


        List<Link> links = getRepository().getOutputLinks(request,
                               new State(getEntryManager().getDummyGroup(),
                                         entries));

        List<String> linkCategories = new ArrayList<String>();
        Hashtable<String, List<HtmlUtil.Selector>> linkMap =
            new Hashtable<String, List<HtmlUtil.Selector>>();
        linkCategories.add("View");
        linkMap.put("View", new ArrayList<HtmlUtil.Selector>());
        for (Link link : links) {
            OutputType outputType = link.getOutputType();
            if (outputType == null) {
                continue;
            }
            String category;
            if (outputType.getIsFile()) {
                category = "File";
            } else if (outputType.getIsEdit()) {
                category = "Edit";
            } else {
                category = "View";
            }
            List<HtmlUtil.Selector> linksForCategory = linkMap.get(category);

            if (linksForCategory == null) {
                linksForCategory = new ArrayList<HtmlUtil.Selector>();
                linkCategories.add(category);
                linkMap.put(category, linksForCategory);
            }

            String icon = link.getIcon();
            if (icon == null) {
                icon = getRepository().iconUrl(ICON_BLANK);
            }
            linksForCategory.add(new HtmlUtil.Selector(outputType.getLabel(),
                    outputType.getId(), icon, 20));
        }

        ArrayList<HtmlUtil.Selector> tfos =
            new ArrayList<HtmlUtil.Selector>();
        for (String category : linkCategories) {
            List<HtmlUtil.Selector> linksForCategory = linkMap.get(category);
            if (linksForCategory.size() == 0) {
                continue;
            }
            tfos.add(new HtmlUtil.Selector(category, "", null, 0, true));
            tfos.addAll(linksForCategory);
        }

        StringBuffer selectSB = new StringBuffer();
        //        selectSB.append(msgLabel("Do"));
        selectSB.append(msgLabel("Apply action to selected entries"));
        selectSB.append(HtmlUtil.select(ARG_OUTPUT, tfos));
        selectSB.append(HtmlUtil.submit(msg("Selected"), "getselected"));
        selectSB.append(HtmlUtil.submit(msg("All"), "getall"));
        selectSB.append(getSortLinks(request));


        String arrowImg = HtmlUtil.img(hideIt
                                       ? getRepository().iconUrl(
                                           ICON_RIGHTDART)
                                       : getRepository().iconUrl(
                                           ICON_DOWNDART), msg(
                                               "Show/Hide Form"), HtmlUtil.id(
                                               base + "img"));
        String link = HtmlUtil.space(2)
                      + HtmlUtil.jsLink(HtmlUtil.onMouseClick(base
                          + ".groupToggleVisibility()"), arrowImg);
        String selectId = base + "select";
        formSB.append(HtmlUtil.span(selectSB.toString(),
                                    HtmlUtil.cssClass("entrylistform")
                                    + HtmlUtil.id(selectId) + (hideIt
                ? HtmlUtil.style("display:none; visibility:hidden;")
                : "")));
        formSB.append(
            HtmlUtil.script(
                HtmlUtil.callln(
                    base + "= new EntryFormList",
                    HtmlUtil.comma(
                        HtmlUtil.squote(formId),
                        HtmlUtil.squote(base + "img"),
                        HtmlUtil.squote(selectId), (hideIt
                ? "0"
                : "1")))));
        return new String[] { link, base, formSB.toString() };
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param htmlSB _more_
     * @param jsSB _more_
     *
     * @throws Exception _more_
     */
    public void addEntryCheckbox(Request request, Entry entry,
                                 StringBuffer htmlSB, StringBuffer jsSB)
            throws Exception {
        String rowId        = "entryrow_" + (HtmlUtil.blockCnt++);
        String cbxId        = "entry_" + (HtmlUtil.blockCnt++);
        String cbxArgId     = "entry_" + entry.getId();
        String cbxWrapperId = "cbx_" + (HtmlUtil.blockCnt++);
        jsSB.append(
            HtmlUtil.callln(
                "new EntryRow",
                HtmlUtil.comma(
                    HtmlUtil.squote(entry.getId()), HtmlUtil.squote(rowId),
                    HtmlUtil.squote(cbxId), HtmlUtil.squote(cbxWrapperId))));

        String cbx =
            HtmlUtil.checkbox(
                cbxArgId, "true", false,
                HtmlUtil.id(cbxId) + " "
                + HtmlUtil.attr(
                    HtmlUtil.ATTR_TITLE,
                    msg(
                    "Shift-click: select range; Control-click: toggle all")) + HtmlUtil.attr(
                        HtmlUtil.ATTR_ONCLICK,
                        HtmlUtil.call(
                            "entryRowCheckboxClicked",
                            HtmlUtil.comma(
                                "event", HtmlUtil.squote(cbxId)))));
        decorateEntryRow(request, entry, htmlSB,
                         getEntryManager().getAjaxLink(request, entry,
                             entry.getLabel()), rowId, cbx);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param formId _more_
     *
     * @return _more_
     */
    public String getEntryFormEnd(Request request, String formId) {
        StringBuffer sb = new StringBuffer();
        sb.append(HtmlUtil.formClose());
        //        sb.append(HtmlUtil.script(HtmlUtil.callln("initEntryListForm",HtmlUtil.squote(formId))));
        return sb.toString();
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getBreadcrumbList(Request request, StringBuffer sb,
                                    List entries)
            throws Exception {
        return getEntriesList(request, sb, entries, true, true, false, true);
    }



    /**
     * _more_
     *
     * @param sb _more_
     * @param entries _more_
     * @param request _more_
     * @param doFormOpen _more_
     * @param doFormClose _more_
     * @param doCbx _more_
     * @param showCrumbs _more_
     *
     *
     * @return _more_
     * @throws Exception _more_
     */
    public String getEntriesList(Request request, StringBuffer sb,
                                 List entries, boolean doFormOpen,
                                 boolean doFormClose, boolean doCbx,
                                 boolean showCrumbs)
            throws Exception {
        return getEntriesList(request, sb, entries, null, doFormOpen,
                              doFormClose, doCbx, showCrumbs, false);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param entries _more_
     * @param entriesToCheck _more_
     * @param doFormOpen _more_
     * @param doFormClose _more_
     * @param doCbx _more_
     * @param showCrumbs _more_
     * @param hideParents _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getEntriesList(Request request, StringBuffer sb,
                                 List entries, List<Entry> entriesToCheck,
                                 boolean doFormOpen, boolean doFormClose,
                                 boolean doCbx, boolean showCrumbs,
                                 boolean hideParents)
            throws Exception {

        String link = "";
        String base = "";
        if (doFormOpen) {
            String[] tuple = getEntryFormStart(request,
                                 ((entriesToCheck != null)
                                  ? entriesToCheck
                                  : entries), true);
            link = tuple[0];
            base = tuple[1];
            sb.append(tuple[2]);
        }
        sb.append(HtmlUtil.open(HtmlUtil.TAG_DIV,
                                HtmlUtil.cssClass("folderblock")));
        sb.append("\n\n");
        int          cnt  = 0;
        StringBuffer jsSB = new StringBuffer();
        for (Entry entry : (List<Entry>) entries) {
            StringBuffer cbxSB        = new StringBuffer();
            String       rowId        = base + (cnt++);
            String       cbxId        = "entry_" + entry.getId();
            String       cbxWrapperId = "checkboxwrapper_" + (cnt++);
            jsSB.append(
                HtmlUtil.callln(
                    "new EntryRow",
                    HtmlUtil.comma(
                        HtmlUtil.squote(entry.getId()),
                        HtmlUtil.squote(rowId), HtmlUtil.squote(cbxId),
                        HtmlUtil.squote(cbxWrapperId))));
            if (doCbx) {
                cbxSB.append(HtmlUtil.hidden("all_" + entry.getId(), "1"));
                String cbx =
                    HtmlUtil.checkbox(
                        cbxId, "true", false,
                        HtmlUtil.id(cbxId) + " "
                        + HtmlUtil.style("display:none; visibility:hidden;")
                        + HtmlUtil.attr(
                            HtmlUtil.ATTR_TITLE,
                            msg(
                            "Shift-click: select range; Control-click: toggle all")) + HtmlUtil.attr(
                                HtmlUtil.ATTR_ONCLICK,
                                HtmlUtil.call(
                                    "entryRowCheckboxClicked",
                                    HtmlUtil.comma(
                                        "event", HtmlUtil.squote(cbxId)))));


                cbxSB.append(HtmlUtil.span(cbx, HtmlUtil.id(cbxWrapperId)));
            }

            String crumbs = "";
            if (showCrumbs) {
                crumbs = getEntryManager().getBreadCrumbs(request,
                                                          (hideParents||true
                                                           ? entry.getParentEntry()
                                                           : entry), null, 60);
                if (hideParents) {
                    crumbs = HtmlUtil.makeToggleInline("",
                                                       crumbs + HtmlUtil.pad(Repository.BREADCRUMB_SEPARATOR), false);
                } else {
                    crumbs = crumbs + HtmlUtil.pad(Repository.BREADCRUMB_SEPARATOR);
                }
                
            }

            EntryLink entryLink = getEntryManager().getAjaxLink(request,
                                                                entry, entry.getLabel(), null,
                                                                true, crumbs);
            entryLink.setLink(cbxSB + entryLink.getLink());
            decorateEntryRow(request, entry, sb, entryLink, rowId, "");
        }
        sb.append(HtmlUtil.close(HtmlUtil.TAG_DIV));
        sb.append(HtmlUtil.script(jsSB.toString()));
        sb.append("\n\n");
        if (doFormClose) {
            sb.append(getEntryFormEnd(request, base));
        }
        return link;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     * @param link _more_
     * @param rowId _more_
     * @param extra _more_
     */
    protected void decorateEntryRow(Request request, Entry entry,
                                    StringBuffer sb, EntryLink link,
                                    String rowId, String extra) {
        if (rowId == null) {
            rowId = "entryrow_" + (HtmlUtil.blockCnt++);
        }


        sb.append(
            HtmlUtil.open(
                HtmlUtil.TAG_DIV,
                HtmlUtil.id(rowId) + HtmlUtil.cssClass("entryrow")
                + HtmlUtil.onMouseClick(
                    HtmlUtil.call(
                        "entryRowClick",
                        "event, "
                        + HtmlUtil.squote(rowId))) + HtmlUtil.onMouseOver(
                            HtmlUtil.call(
                                "entryRowOver",
                                HtmlUtil.squote(
                                    rowId))) + HtmlUtil.onMouseOut(
                                        HtmlUtil.call(
                                            "entryRowOut",
                                            HtmlUtil.squote(rowId)))));
        sb.append(
            "<table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"><tr>");


        sb.append("<td>");
        sb.append(extra);
        //        sb.append(crumbs);
        sb.append(link.getLink());
        sb.append("</td>");


        /*
        String desc = entry.getDescription();
        StringBuffer descSB = new StringBuffer();
        String toggleJS = HtmlUtil.makeToggleBlock(desc,
                                                   descSB, false);
        sb.append(descSB);
        */


        StringBuffer extraAlt  = new StringBuffer();
        String       userLabel = "";
        extraAlt.append(", ");
        extraAlt.append(entry.getUser().getId());
        if (entry.getResource().isFile()) {
            extraAlt.append(", ");
            extraAlt.append(
                formatFileLength(entry.getResource().getFileSize()));

        }

        if(request.isMobile()) {
            sb.append("<td align=right><div class=entryrowlabel>");
        } else {
            sb.append("<td align=right width=200><div class=entryrowlabel>");
        }
        sb.append(getRepository().formatDateShort(request,
                new Date(entry.getStartDate()),
                getEntryManager().getTimezone(entry), extraAlt.toString()));
        sb.append(
            "</div></td><td width=\"1%\" align=right class=entryrowlabel>");
        sb.append(HtmlUtil.space(1));

        //      sb.append(HtmlUtil.jsLink(toggleJS,"X"));
        /*        String userSearchLink =
            HtmlUtil.href(
                HtmlUtil.url(
                    request.url(getRepository().URL_USER_PROFILE),
                    ARG_USER_ID, entry.getUser().getId()), userLabel,
                        "title=\"View user profile\"");

                        sb.append(userSearchLink);*/
        sb.append("  ");
        sb.append(
            HtmlUtil.div(
                HtmlUtil.img(
                    getRepository().iconUrl(ICON_BLANK), "",
                    HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "10")
                    + HtmlUtil.id(
                        "entrymenuarrow_" + rowId)), HtmlUtil.cssClass(
                            "entrymenuarrow")));

        sb.append("</td></tr></table>");
        sb.append(HtmlUtil.close(HtmlUtil.TAG_DIV));
        sb.append(link.getFolderBlock());

    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getEntryLink(Request request, Entry entry)
            throws Exception {
        return getEntryManager().getTooltipLink(request, entry,
                entry.getLabel(), null);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param output _more_
     * @param links _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected List getHeader(Request request, OutputType output,
                             List<Link> links)
            throws Exception {

        List   items          = new ArrayList();
        Object initialMessage = request.remove(ARG_MESSAGE);
        String onLinkTemplate = getRepository().getTemplateProperty(request,
                                    "ramadda.template.sublink.on", "");
        String offLinkTemplate = getRepository().getTemplateProperty(request,
                                     "ramadda.template.sublink.off", "");
        for (Link link : links) {
            OutputType outputType = link.getOutputType();
            String     url        = link.getUrl();
            String     template;
            if (Misc.equals(outputType, output)) {
                template = onLinkTemplate;
            } else {
                template = offLinkTemplate;
            }
            String html = template.replace("${label}", link.getLabel());
            html = html.replace("${url}", url);
            html = html.replace("${root}", getRepository().getUrlBase());
            items.add(html);
        }
        if (initialMessage != null) {
            request.put(ARG_MESSAGE, initialMessage);
        }
        return items;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param typeHandlers _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result listTypes(Request request, List<TypeHandler> typeHandlers)
            throws Exception {
        return notImplemented("listTypes");
    }





    /**
     * protected Result listTags(Request request, List<Tag> tags)
     *       throws Exception {
     *   return notImplemented("listTags");
     * }
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */



    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result listAssociations(Request request) throws Exception {
        return notImplemented("listAssociations");
    }





    /** _more_ */
    public static final String RESOURCE_ENTRYTEMPLATE = "entrytemplate.txt";

    /** _more_ */
    public static final String RESOURCE_GROUPTEMPLATE = "grouptemplate.txt";


    /** _more_ */
    public static final String PROP_ENTRY = "entry";

    /** _more_ */
    public static final String PROP_REQUEST = "request";



    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String getImageUrl(Request request, Entry entry) {
        return getImageUrl(request, entry, false);
    }


    /** _more_ */
    private static int imageVersionCnt = 0;

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param addVersion _more_
     *
     * @return _more_
     */
    public String getImageUrl(Request request, Entry entry,
                              boolean addVersion) {
        if ( !entry.getResource().isImage()) {
            if (true) {
                return null;
            }
            /*
            if (entry.hasAreaDefined()) {
                return request.url(repository.URL_GETMAP, ARG_SOUTH,
                                   "" + entry.getSouth(), ARG_WEST,
                                   "" + entry.getWest(), ARG_NORTH,
                                   "" + entry.getNorth(), ARG_EAST,
                                   "" + entry.getEast());
                                   }*/
            return null;
        }

        String url = entry.getResource().getPath();
        if(url!=null) {
            if(url.startsWith("ftp:") || url.startsWith("http:")) {
                return url;
            }
        }


        return HtmlUtil.url(request.url(repository.URL_ENTRY_GET) + "/"
                            + (addVersion
                               ? ("v" + (imageVersionCnt++))
                               : "") + getStorageManager().getFileTail(
                                   entry), ARG_ENTRYID, entry.getId());
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param onlyIfWeHaveThem _more_
     *
     *
     * @return _more_
     * @throws Exception _more_
     */
    public StringBuffer getCommentBlock(Request request, Entry entry,
                                        boolean onlyIfWeHaveThem)
            throws Exception {
        StringBuffer  sb       = new StringBuffer();
        List<Comment> comments = getEntryManager().getComments(request,
                                                               entry);
        if ( !onlyIfWeHaveThem || (comments.size() > 0)) {
            sb.append(getEntryManager().getCommentHtml(request, entry));
        }
        return sb;

    }




    /**
     *  Set the MaxConnections property.
     *
     *  @param value The new value for MaxConnections
     */
    public void setMaxConnections(int value) {
        this.maxConnections = value;
    }

    /**
     *  Get the MaxConnections property.
     *
     *  @return The MaxConnections
     */
    public int getMaxConnections() {
        return this.maxConnections;
    }

    /**
     *  Set the NumberOfConnections property.
     *
     *  @param value The new value for NumberOfConnections
     */
    public void setNumberOfConnections(int value) {
        this.numberOfConnections = value;
    }

    /**
     *  Get the NumberOfConnections property.
     *
     *  @return The NumberOfConnections
     */
    public int getNumberOfConnections() {
        return this.numberOfConnections;
    }

    /**
     * _more_
     */
    public void incrNumberOfConnections() {
        numberOfConnections++;
        totalCalls++;
    }



    /**
     * _more_
     */
    public void decrNumberOfConnections() {
        numberOfConnections--;
        if (numberOfConnections < 0) {
            numberOfConnections = 0;
        }
    }

    public static final String CLASS_TAB_CONTENT = "tab_content";
    public static final String CLASS_TAB_CONTENTS = "tab_contents";
    private static int tabCnt=0;

    public static String makeTabs(List titles, List contents,
                                  boolean skipEmpty) {
        return makeTabs(titles, contents, skipEmpty, CLASS_TAB_CONTENT);
    }

    /**
     * _more_
     *
     * @param titles _more_
     * @param contents _more_
     * @param skipEmpty _more_
     * @param tabContentClass _more_
     *
     * @return _more_
     */
    public static String makeTabs(List titles, List contents,
                                  boolean skipEmpty, String tabContentClass) {
        return makeTabs(titles, contents, skipEmpty, tabContentClass,
                        CLASS_TAB_CONTENTS);
    }

    public static String makeTabs(List titles, List tabs,
                                  boolean skipEmpty, String tabContentClass,
                                  String wrapperClass) {
        StringBuffer tabHtml = new StringBuffer();
        String       tabId   = "tabId" + (tabCnt++);
        tabHtml.append("\n\n");
        tabHtml.append(HtmlUtil.open(HtmlUtil.TAG_DIV,
                                     HtmlUtil.id(tabId)));
        tabHtml.append(HtmlUtil.open(HtmlUtil.TAG_UL));
        int cnt = 1;
        for(int i=0;i<titles.size();i++) {
            String title = titles.get(i).toString();
            String tabContents = tabs.get(i).toString();
            if(skipEmpty && (tabContents==null || tabContents.length()==0)) continue;
            tabHtml.append("<li><a href=\"#" + tabId + "-" + (cnt++)
                           + "\">" + title + "</a></li>");
        }
        tabHtml.append(HtmlUtil.close(HtmlUtil.TAG_UL));
        cnt = 1;
        for(int i=0;i<titles.size();i++) {
            String tabContents = tabs.get(i).toString();
            if(skipEmpty && (tabContents==null || tabContents.length()==0)) continue;
            tabHtml.append(HtmlUtil.div(tabContents,
                                        HtmlUtil.id(tabId + "-" + (cnt++))));
            tabHtml.append("\n");
        }

        tabHtml.append(HtmlUtil.close(HtmlUtil.TAG_DIV));
        tabHtml.append("\n");
        tabHtml.append(
                       HtmlUtil.script(
                        "\njQuery(function(){\njQuery('#" + tabId
                        + "').tabs();\n});\n"));
        tabHtml.append("\n\n");
        return tabHtml.toString();
    }



    /**
     * _more_
     *
     * @param titles _more_
     * @param contents _more_
     * @param skipEmpty _more_
     * @param tabContentClass _more_
     * @param wrapperClass _more_
     *
     * @return _more_
     */
    public static String makeTabsx(List titles, List contents,
                                  boolean skipEmpty, String tabContentClass,
                                  String wrapperClass) {

        String       id        = "tab_" + (tabCnt++);
        String       ids       = "tab_" + (tabCnt++) + "_ids";
        StringBuffer titleSB   = new StringBuffer("");
        StringBuffer contentSB = new StringBuffer();
        StringBuffer idArray   = new StringBuffer("new Array(");
        int          cnt       = 0;
        for (int i = 0; i < titles.size(); i++) {
            String content = contents.get(i).toString();
            if (skipEmpty && (content.length() == 0)) {
                continue;
            }

            String tabId = id + "_" + i;
            if (cnt > 0) {
                idArray.append(",");
            }
            cnt++;
            idArray.append(HtmlUtil.squote(tabId));
        }
        if ((cnt == 1) && skipEmpty) {
            return contents.get(0).toString();
        }

        idArray.append(")");

        String selectedOne = null;
        for (int i = 0; i < titles.size(); i++) {
            String content = contents.get(i).toString();
            if (skipEmpty && (content.length() == 0)) {
                continue;
            }
            String title = titles.get(i).toString();
            if (title.startsWith("selected:")) {
                selectedOne = title;
                break;
            }
        }

        boolean didone = false;
        for (int i = 0; i < titles.size(); i++) {
            String content = contents.get(i).toString();
            if (skipEmpty && (content.length() == 0)) {
                continue;
            }
            String title = titles.get(i).toString();
            String tabId = id + "_" + i;
            contentSB.append("\n");
            boolean selected = ((selectedOne == null)
                                ? !didone
                                : Misc.equals(title, selectedOne));
            if (selected && (selectedOne != null)) {
                title = title.substring("selected:".length());
            }
            contentSB.append(HtmlUtil.div(content,
                                          HtmlUtil.cssClass(tabContentClass
                                              + (selected
                    ? "_on"
                    : "_off")) + HtmlUtil.id("content_" + tabId)
                               + HtmlUtil.style("display:" + (selected
                    ? "block"
                    : "none") + ";visibility:" + (selected
                    ? "visible"
                    : "hidden"))));
            String link = HtmlUtil.href("javascript:" + "tabPress("
                                        + HtmlUtil.squote(id) + "," + idArray
                                        + "," + HtmlUtil.squote(tabId)
                                        + ")", title);
            titleSB.append(HtmlUtil.span(link, (selected
                    ? HtmlUtil.cssClass("tab_title_on")
                    : HtmlUtil.cssClass("tab_title_off")) + HtmlUtil.id(
                        "title_" + tabId)));
            didone = true;
        }

        return HtmlUtil.div(
            titleSB.toString(),
            HtmlUtil.cssClass("tab_titles")) + HtmlUtil.div(
                contentSB.toString(), HtmlUtil.cssClass(wrapperClass));
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param arg _more_
     * @param dflt _more_
     * @param width _more_
     *
     * @return _more_
     */
    public String htmlInput(Request request, String arg, String dflt,
                             int width) {
        return HtmlUtil.input(arg, request.getString(arg, dflt),
                              HtmlUtil.attr(HtmlUtil.ATTR_SIZE, "" + width));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param arg _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String htmlInput(Request request, String arg, String dflt) {
        return htmlInput(request, arg, dflt, 5);
    }

    public boolean canAddTo(Request request, Entry parent) throws Exception {
        return getEntryManager().canAddTo(request, parent);
    }


    /**
     * Did the user choose an entry to publish to 
     */
    public boolean doingPublish(Request request) {
        return request.defined(ARG_PUBLISH_ENTRY + "_hidden");
    }

    /**
     * If the user is not anonymous then add the "Publish to" widget.
     */
    public void addPublishWidget(Request request, Entry entry, StringBuffer sb, String header) throws Exception { 
        if ( !request.getUser().getAnonymous()) {
            StringBuffer publishSB = new StringBuffer();
            sb.append(HtmlUtil.hidden(ARG_PUBLISH_ENTRY + "_hidden",
                                             "",
                                             HtmlUtil.id(ARG_PUBLISH_ENTRY
                                                 + "_hidden")));
            sb.append(
                      HtmlUtil.row(HtmlUtil.cols("",
                                                 header)));

            String select = OutputHandler.getSelect(request,
                                ARG_PUBLISH_ENTRY, "Select folder", false,
                                null, entry);
            String addMetadata=  HtmlUtil.checkbox(
                                                   ARG_METADATA_ADD, HtmlUtil.VALUE_TRUE,
                                                   request.get(ARG_METADATA_ADD, false)) + 
                msg("Add properties");
            sb.append(HtmlUtil.formEntry(msgLabel("Folder"),
                    HtmlUtil.disabledInput(ARG_PUBLISH_ENTRY, "",
                                           HtmlUtil.id(ARG_PUBLISH_ENTRY)
                                           + HtmlUtil.SIZE_60) + select+HtmlUtil.space(2) +addMetadata));

            sb.append(HtmlUtil.formEntry(msgLabel("Name"),
                    htmlInput(request, ARG_PUBLISH_NAME, "", 30)));
            
        }
    }


}
