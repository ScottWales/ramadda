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

package org.ramadda.repository.output;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.map.*;

import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.BufferMapList;

import org.ramadda.util.WikiUtil;


import org.w3c.dom.Element;


import ucar.unidata.sql.SqlUtil;
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


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 * Provides wiki text processing services
 * @version $Revision: 1.3 $
 */
public class WikiManager extends RepositoryManager implements WikiUtil
    .WikiPageHandler {

    /** _more_ */
    public static String TYPE_WIKIPAGE = "wikipage";

    /** _more_ */
    public static final String PROP_ENTRY = "entry";

    /** _more_ */
    public static final String PROP_REQUEST = "request";


    /** _more_          */
    public static final String LABEL_LINKS = "Actions";

    /** _more_ */
    public static final OutputType OUTPUT_WIKI = new OutputType("Wiki",
                                                     "wiki.view",
                                                     OutputType.TYPE_HTML,
                                                     "", ICON_WIKI);


    /** _more_          */
    public static final String PROP_LEVEL = "level";

    /** _more_          */
    public static final String PROP_COUNT = "count";

    /** _more_          */
    public static final String PROP_WIDTH = "width";

    /** _more_ */
    public static final String WIKIPROP_IMPORT = "import";

    /** _more_ */
    public static final String WIKIPROP_COMMENTS = "comments";

    /** _more_ */
    public static final String WIKIPROP_RECENT = "recent";


    /** _more_          */
    public static final String WIKIPROP_GALLERY = "gallery";

    /** _more_          */
    public static final String WIKIPROP_TABS = "tabs";

    public static final String WIKIPROP_GRID = "grid";

    /** _more_ */
    public static final String WIKIPROP_TOOLBAR = "toolbar";

    /** _more_ */
    public static final String WIKIPROP_BREADCRUMBS = "breadcrumbs";

    /** _more_ */
    public static final String WIKIPROP_INFORMATION = "information";

    /** _more_ */
    public static final String WIKIPROP_IMAGE = "image";

    /** _more_ */
    public static final String WIKIPROP_NAME = "name";

    /** _more_          */
    public static final String WIKIPROP_MAP = "map";

    public static final String WIKIPROP_HTML = "html";

    /** _more_          */
    public static final String WIKIPROP_MAPENTRY = "mapentry";

    /** _more_ */
    public static final String WIKIPROP_DESCRIPTION = "description";

    /** _more_          */
    public static final String WIKIPROP_PROPERTIES = "properties";

    /** _more_ */
    public static final String WIKIPROP_LINKS = "links";

    /** _more_ */
    public static final String WIKIPROP_ = "";

    /** _more_ */
    public static final String WIKIPROP_CHILDREN_GROUPS = "subgroups";

    /** _more_ */
    public static final String WIKIPROP_CHILDREN_ENTRIES = "subentries";

    /** _more_ */
    public static final String WIKIPROP_CHILDREN = "children";

    /** _more_ */
    public static final String WIKIPROP_URL = "url";


    /** _more_ */
    public static final String[] WIKIPROPS = {
        WIKIPROP_INFORMATION, WIKIPROP_NAME, WIKIPROP_DESCRIPTION,
        WIKIPROP_PROPERTIES, WIKIPROP_HTML, WIKIPROP_MAP, WIKIPROP_COMMENTS,
        WIKIPROP_BREADCRUMBS, WIKIPROP_TOOLBAR, WIKIPROP_IMAGE,
        WIKIPROP_LINKS, WIKIPROP_RECENT, WIKIPROP_MAPENTRY, WIKIPROP_MAPENTRY,
        WIKIPROP_GALLERY, 
        WIKIPROP_TABS,  WIKIPROP_TABS,
        WIKIPROP_GRID,
        /*,
                          WIKIPROP_CHILDREN_GROUPS,
                          WIKIPROP_CHILDREN_ENTRIES,
                          WIKIPROP_CHILDREN*/
    };




    /**
     * _more_
     *
     * @param repository _more_
     */
    public WikiManager(Repository repository) {
        super(repository);
    }





    /**
     * _more_
     *
     * @param wikiUtil _more_
     * @param property _more_
     *
     * @return _more_
     */
    public String getWikiPropertyValue(WikiUtil wikiUtil, String property) {
        try {
            /*
              {{type name="value" ...}}
              {{import "entry identifier" type name="value"}}
             */
            Entry   entry   = (Entry) wikiUtil.getProperty(PROP_ENTRY);
            Request request = (Request) wikiUtil.getProperty(PROP_REQUEST);
            //Check for infinite loop
            property = property.trim();
            if (property.length() == 0) {
                return "";
            }
            if (request.getExtraProperty(property) != null) {
                return "<b>Detected circular wiki import:" + property
                       + "</b>";
            }
            request.putExtraProperty(property, property);

            List<String> toks = StringUtil.splitUpTo(property, " ", 2);
            if (toks.size() == 0) {
                return "<b>Incorrect import specification:" + property
                       + "</b>";
            }
            String tag       = ((toks.size() == 0)
                                ? ""
                                : toks.get(0));
            String remainder = "";
            if (toks.size() > 1) {
                remainder = toks.get(1);
            }
            Entry theEntry = entry;
            if (tag.equals(WIKIPROP_IMPORT)) {
                toks = StringUtil.splitUpTo(remainder, " ", 3);
                if (toks.size() < 2) {
                    return "<b>Incorrect import specification:" + property
                           + "</b>";
                }
                String id = toks.get(0).trim();
                tag = toks.get(1).trim();
                if (toks.size() == 3) {
                    remainder = toks.get(2);
                } else {
                    remainder = "";
                }
                theEntry = findWikiEntry(request, wikiUtil, id, entry);
                if (theEntry == null) {
                    return "<b>Could not find entry&lt;" + id + "&gt;</b>";
                }
            }

            Hashtable props = new Hashtable();
            props = StringUtil.parseHtmlProperties(remainder);
            String entryId = (String) props.get(PROP_ENTRY);
            if (entryId != null) {
                theEntry = getEntryManager().getEntry(request, entryId);
                if (theEntry == null) {
                    return "Unknown entry:" + entryId;
                }
            }

            addWikiLink(wikiUtil, theEntry);
            String include = handleWikiImport(wikiUtil, request, theEntry,
                                 tag, props);
            if (include != null) {
                return include;
            }
            return wikiUtil.getPropertyValue(property);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }



    /**
     * _more_
     *
     *
     * @param wikiUtil _more_
     * @param request _more_
     * @param url _more_
     * @param entry _more_
     * @param props _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getWikiImage(WikiUtil wikiUtil, Request request,
                               String url, Entry entry, Hashtable props)
            throws Exception {
        String width = (String) props.get(HtmlUtil.ATTR_WIDTH);
        String extra = "";

        if (width != null) {
            extra = HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, width);
        }
        if (wikiUtil != null) {
            String imageClass = (String) wikiUtil.getProperty("image.class");
            if (imageClass != null) {
                extra = extra + HtmlUtil.cssClass(imageClass);
            }
        }


        String style = "";

        String left  = (String) props.get("left");
        if (left != null) {
            style = style + " left: " + left + ";";
        }

        String top = (String) props.get("top");
        if (top != null) {
            style = style + " top: " + top + ";";
        }

        if (style.length() > 0) {
            extra = extra + " style=\"position:absolute; " + style + "\" ";
        }



        String  img  = HtmlUtil.img(url, entry.getName(), extra);
        boolean link = Misc.equals("true", props.get("link"));
        if (link) {
            return HtmlUtil.href(
                request.entryUrl(getRepository().URL_ENTRY_SHOW, entry), img);

        }
        return img;
    }


    /**
     * _more_
     *
     * @param wikiUtil _more_
     * @param request _more_
     * @param entry _more_
     * @param props _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getWikiUrl(WikiUtil wikiUtil, Request request, Entry entry,
                             Hashtable props)
            throws Exception {

        String src      = (String) props.get("src");
        Entry  srcEntry = null;
        if (src == null) {
            srcEntry = entry;
        } else {
            src = src.trim();
            if ((src.length() == 0) || entry.getName().equals(src)) {
                srcEntry = entry;
            } else if (entry instanceof Entry) {
                srcEntry = getEntryManager().findEntryWithName(request,
                        (Entry) entry, src);
            }
        }
        if (srcEntry == null) {
            srcEntry = getEntryManager().getEntry(request, src);
        }

        if (srcEntry == null) {
            return msg("Could not find src:" + src);
        }

        return request.entryUrl(getRepository().URL_ENTRY_SHOW, srcEntry);

    }


    /**
     * _more_
     *
     * @param wikiUtil _more_
     * @param request _more_
     * @param entry _more_
     * @param props _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getWikiImage(WikiUtil wikiUtil, Request request,
                               Entry entry, Hashtable props)
            throws Exception {

        String src = (String) props.get("src");
        if (src == null) {
            if ( !entry.getResource().isImage()) {
                return msg("Not an image");
            }
            return getWikiImage(wikiUtil, request,
                                getHtmlOutputHandler().getImageUrl(request,
                                    entry), entry, props);
        }

        String attachment = null;
        int    idx        = src.indexOf("::");
        if (idx >= 0) {
            List<String> toks = StringUtil.splitUpTo(src, "::", 2);
            if (toks.size() == 2) {
                src        = toks.get(0);
                attachment = toks.get(1).substring(1);
            }
        }
        src = src.trim();
        Entry srcEntry = null;

        if ((src.length() == 0) || entry.getName().equals(src)) {
            srcEntry = entry;
        } else if (entry instanceof Entry) {
            srcEntry = getEntryManager().findEntryWithName(request,
                    (Entry) entry, src);
        }
        if (srcEntry == null) {
            return msg("Could not find src:" + src);
        }
        if (attachment == null) {
            if ( !srcEntry.getResource().isImage()) {
                return msg("Not an image");
            }
            return getWikiImage(wikiUtil, request,
                                getHtmlOutputHandler().getImageUrl(request,
                                    srcEntry), srcEntry, props);
        }


        for (Metadata metadata : getMetadataManager().getMetadata(srcEntry)) {
            MetadataType metadataType =
                getMetadataManager().findType(metadata.getType());
            String url = metadataType.getImageUrl(request, srcEntry,
                             metadata, attachment);
            if (url != null) {
                return getWikiImage(wikiUtil, request, url, srcEntry, props);
            }
        }

        return msg("Could not find image attachment:" + attachment);
    }



    /**
     * _more_
     *
     * @param wikiUtil _more_
     * @param request _more_
     * @param entry _more_
     * @param include _more_
     * @param props _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getWikiInclude(WikiUtil wikiUtil, Request request,
                                 Entry entry, String include, Hashtable props)
            throws Exception {

        boolean open         = Misc.getProperty(props, "open", true);
        boolean inBlock      = Misc.getProperty(props, "showtoggle", true);
        String  blockContent = null;
        String  blockTitle   = "";
        boolean doBG         = true;

        if (include.equals(WIKIPROP_INFORMATION)) {
            blockContent =
                getRepository().getHtmlOutputHandler().getInformationTabs(
                    request, entry, false, true);
            blockTitle = Misc.getProperty(props, "title", msg("Information"));
        } else if (include.equals(WIKIPROP_HTML)) {
            Result result = getHtmlOutputHandler().getHtmlResult(request, OutputHandler.OUTPUT_HTML,
                                                                 entry);
            return new String(result.getContent());
        } else if (include.equals(WIKIPROP_MAP)) {
            StringBuffer mapSB = new StringBuffer();
            int          width = Misc.getProperty(props, "width", 400);
            int          height = Misc.getProperty(props, "height", 300);
            MapOutputHandler mapOutputHandler =
                (MapOutputHandler) getRepository().getOutputHandler(
                    MapOutputHandler.OUTPUT_MAP);
            if (mapOutputHandler == null) {
                return "No maps";
            }

            List<Entry> children =
                (List<Entry>) wikiUtil.getProperty(entry.getId()
                    + "_children");
            if (children == null) {
                children = getEntryManager().getChildren(request, entry);
            }
            boolean[] haveBearingLines = { false };
            MapInfo map = mapOutputHandler.getMap(request, children, mapSB,
                              width, height, haveBearingLines);
            return mapSB.toString();
        } else if (include.equals(WIKIPROP_MAPENTRY)) {
            StringBuffer mapSB = new StringBuffer();
            int          width = Misc.getProperty(props, "width", 400);
            int          height = Misc.getProperty(props, "height", 300);
            MapOutputHandler mapOutputHandler =
                (MapOutputHandler) getRepository().getOutputHandler(
                    MapOutputHandler.OUTPUT_MAP);
            if (mapOutputHandler == null) {
                return "No maps";
            }

            List<Entry> children = new ArrayList<Entry>();
            children.add(entry);
            boolean[] haveBearingLines = { false };
            MapInfo map = mapOutputHandler.getMap(request, children, mapSB,
                              width, height, haveBearingLines);
            return mapSB.toString();
        } else if (include.equals(WIKIPROP_PROPERTIES)) {
            return makeEntryTabs(request, entry);
        } else if (include.equals(WIKIPROP_IMAGE)) {
            return getWikiImage(wikiUtil, request, entry, props);
        } else if (include.equals(WIKIPROP_URL)) {
            return getWikiUrl(wikiUtil, request, entry, props);
        } else if (include.equals(WIKIPROP_LINKS)) {
            blockTitle = Misc.getProperty(props, "title", msg(LABEL_LINKS));
            blockContent = getEntryManager().getEntryActionsTable(request,
                    entry, OutputType.TYPE_ALL);
        } else if (include.equals(WIKIPROP_TABS)) {
            List tabTitles   = new ArrayList<String>();
            List tabContents = new ArrayList<String>();
            List<Entry> children =  getEntries(request,  wikiUtil,  entry, props);
            for(Entry child: children) {
                tabTitles.add(child.getName());
                String content =
                    getRepository().getHtmlOutputHandler().getInformationTabs(
                                                                              request, child, false, true);

                
                tabContents.add(content);
            }
            return OutputHandler.makeTabs(tabTitles, tabContents, true,
                                          (true
                                           ? "tab_content_fixedheight"
                                           : "tab_content"));

        } else if (include.equals(WIKIPROP_GRID)) {
            List<Entry> children =  getEntries(request,  wikiUtil,  entry, props);
            StringBuffer sb = new StringBuffer();
            getHtmlOutputHandler().makeGrid( request,  children, sb);
            return sb.toString();
        } else if (include.equals(WIKIPROP_RECENT)) {
            List<Entry> children =  getEntries(request,  wikiUtil,  entry, props);
            int                 numDays = Misc.getProperty(props, "days", 3);
            StringBuffer        sb      = new StringBuffer();
            BufferMapList<Date> map     = new BufferMapList<Date>();
            SimpleDateFormat dateFormat =
                new SimpleDateFormat("EEEEE MMMMM d");
            dateFormat.setTimeZone(RepositoryUtil.TIMEZONE_DEFAULT);
            Date firstDay = ((children.size() > 0)
                             ? new Date(children.get(0).getChangeDate())
                             : new Date());
            GregorianCalendar cal1 =
                new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
            cal1.setTime(new Date(firstDay.getTime()));
            cal1.set(cal1.MILLISECOND, 0);
            cal1.set(cal1.SECOND, 0);
            cal1.set(cal1.MINUTE, 0);
            cal1.set(cal1.HOUR, 0);
            GregorianCalendar cal2 =
                new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
            cal2.setTime(cal1.getTime());
            cal2.roll(cal2.DAY_OF_YEAR, 1);

            for (int i = 0; i < numDays; i++) {
                Date date1 = cal1.getTime();
                Date date2 = cal2.getTime();
                cal2.setTime(cal1.getTime());
                cal1.roll(cal1.DAY_OF_YEAR, -1);
                for (Entry e : children) {
                    Date changeDate = new Date(e.getChangeDate());
                    changeDate = new Date(e.getStartDate());
                    if ((changeDate.getTime() < date1.getTime())
                            || (changeDate.getTime() > date2.getTime())) {
                        continue;
                    }
                    StringBuffer buff = map.get(date1);
                    buff.append("<tr><td width=75%>&nbsp;&nbsp;&nbsp;");
                    buff.append(getEntryManager().getAjaxLink(request, e,
                            e.getLabel()));
                    buff.append("</td><td width=25% align=right><i>");
                    buff.append(formatDate(request, changeDate));
                    buff.append("</i></td></tr>");
                }
            }
            for (Date date : map.getKeys()) {
                StringBuffer tmp = new StringBuffer();
                String msg = msg("New on") + " " + dateFormat.format(date);
                tmp.append("<table width=100% border=0>");
                tmp.append(map.get(date));
                tmp.append("</table>");
                sb.append(HtmlUtil.makeShowHideBlock(msg, tmp.toString(),
                        true));
            }
            return sb.toString();

        } else if (include.equals(WIKIPROP_GALLERY)) {
            int count = Misc.getProperty(props, PROP_COUNT, -1);
            int width = Misc.getProperty(props, PROP_WIDTH, -1);
            List<Entry> children =  getEntries(request,  wikiUtil,  entry, props);
            StringBuffer sb  = new StringBuffer();
            int          num = 0;
            for (Entry child : children) {
                if ( !child.getResource().isImage()) {
                    continue;
                }
                num++;
                if ((count > 0) && (num > count)) {
                    break;
                }
                String url = HtmlUtil.url(
                                 request.url(repository.URL_ENTRY_GET) + "/"
                                 + getStorageManager().getFileTail(
                                     child), ARG_ENTRYID, child.getId());
                if (width <= 0) {
                    sb.append(HtmlUtil.img(url));
                } else {
                    sb.append(HtmlUtil.img(url, "",
                                           HtmlUtil.attr(HtmlUtil.ATTR_WIDTH,
                                               "" + width)));
                }
                sb.append(HtmlUtil.br());
                sb.append("<b>");
                sb.append(msg("Figure"));
                sb.append(" " + num);
                sb.append(" - ");
                sb.append("</b>");
                sb.append(getEntryManager().getAjaxLink(request, child,
                        child.getLabel()));
                //              sb.append(HtmlUtil.br());
                //              sb.append(HtmlUtil.makeToggleInline("...", child.getDescription(),false));
            }
            return sb.toString();
        } else if (include.equals(WIKIPROP_COMMENTS)) {
            return getHtmlOutputHandler().getCommentBlock(request, entry,
                    false).toString();
        } else if (include.equals(WIKIPROP_TOOLBAR)) {
            return getEntryManager().getEntryToolbar(request, entry, false);
        } else if (include.equals(WIKIPROP_BREADCRUMBS)) {
            return getEntryManager().getBreadCrumbs(request, entry);
        } else if (include.equals(WIKIPROP_DESCRIPTION)) {
            return entry.getDescription();
        } else if (include.equals(WIKIPROP_NAME)) {
            return entry.getName();
        } else if (include.equals(WIKIPROP_CHILDREN_GROUPS)) {
            doBG = false;
            List<Entry> children =
                (List<Entry>) wikiUtil.getProperty(entry.getId()
                    + "_subgroups");
            if (children == null) {
                children = getEntryManager().getChildrenGroups(request,
                        entry);
            }
            if (children.size() == 0) {
                return "";
            }
            StringBuffer sb = new StringBuffer();
            String link = getHtmlOutputHandler().getEntriesList(request, sb,
                              children, true, true, true, false);
            blockContent = sb.toString();
            blockTitle = Misc.getProperty(props, "title", msg("Folders"))
                         + link;
        } else if (include.equals(WIKIPROP_CHILDREN_ENTRIES)) {
            doBG = false;
            List<Entry> children =
                (List<Entry>) wikiUtil.getProperty(entry.getId()
                    + "_subentries");
            if (children == null) {
                children = getEntryManager().getChildrenEntries(request,
                        entry);
            }
            if (children.size() == 0) {
                return "";
            }

            StringBuffer sb = new StringBuffer();
            String link = getHtmlOutputHandler().getEntriesList(request, sb,
                              children, true, true, true, false);
            blockContent = sb.toString();
            blockTitle = Misc.getProperty(props, "title", msg("Entries"))
                         + link;
            blockContent = sb.toString();
            blockTitle = Misc.getProperty(props, "title", msg("Folders"))
                         + link;
        } else if (include.equals(WIKIPROP_CHILDREN)) {
            doBG = false;
            StringBuffer sb = new StringBuffer();
            List<Entry> children =
                (List<Entry>) wikiUtil.getProperty(entry.getId()
                    + "_children");
            if (children == null) {
                children = getEntryManager().getChildren(request, entry);
            }
            if (children.size() == 0) {
                return "";
            }
            String link = getHtmlOutputHandler().getEntriesList(request, sb,
                              children, true, true, true, false);
            blockContent = sb.toString();
            blockTitle = Misc.getProperty(props, "title", msg("Links"))
                         + link;
        } else {
            return null;
        }

        if ( !inBlock) {
            return blockContent;
        }
        if (doBG) {
            return HtmlUtil.makeShowHideBlock(blockTitle, blockContent, open,
                    HtmlUtil.cssClass("wiki-tocheader"),
                    HtmlUtil.cssClass("wiki-toc"));
        } else {
            return HtmlUtil.makeShowHideBlock(blockTitle, blockContent, open);
        }

    }

    public String makeEntryTabs(Request request, Entry entry) throws Exception {
        List tabTitles   = new ArrayList<String>();
        List tabContents = new ArrayList<String>();
        for (TwoFacedObject tfo :
                 getRepository().getHtmlOutputHandler().getMetadataHtml(
                                                                        request, entry, true, false)) {
            tabTitles.add(tfo.toString());
            tabContents.add(tfo.getId());
        }
        if (tabTitles.size() == 0) {
            return "none";
        }
        if (tabTitles.size() > 1) {
            return OutputHandler.makeTabs(tabTitles, tabContents, true,
                                          (true
                                           ? "tab_content_fixedheight"
                                           : "tab_content"));

        }
        return tabContents.get(0).toString();

    }

    public List<Entry> getEntries(Request request, WikiUtil wikiUtil, Entry entry,
                                  Hashtable props) throws Exception {
            int level = Misc.getProperty(props, PROP_LEVEL, 1);
            List<Entry> children =
                (List<Entry>) wikiUtil.getProperty(entry.getId()
                    + "_children");
            if (children == null) {
                children = getEntryManager().getChildren(request, entry);
            }
            if (level == 2) {
                List<Entry> grandChildren = new ArrayList<Entry>();
                for (Entry child : children) {
                    if ( !child.isGroup()) {
                        grandChildren.add(child);
                    }
                }
                for (Entry child : children) {
                    if (child.isGroup()) {
                        grandChildren.addAll(
                            getEntryManager().getChildren(request, child));
                    }
                }
                grandChildren =
                    getEntryManager().sortEntriesOnDate(grandChildren, true);
                children = grandChildren;
            }
            return children;

    }


    /**
     * _more_
     *
     * @return _more_
     */
    public HtmlOutputHandler getHtmlOutputHandler() {
        return getRepository().getHtmlOutputHandler();
    }

    /**
     * _more_
     *
     * @param wikiUtil _more_
     * @param request _more_
     * @param importEntry _more_
     * @param tag _more_
     * @param props _more_
     *
     * @return _more_
     */
    public String handleWikiImport(WikiUtil wikiUtil, final Request request,
                                   Entry importEntry, String tag,
                                   Hashtable props) {
        try {
            Request myRequest =
                new Request(getRepository(), request.getUser(),
                            getRepository().URL_ENTRY_SHOW.toString()) {
                public void putExtraProperty(Object key, Object value) {
                    request.putExtraProperty(key, value);
                }
                public Object getExtraProperty(Object key) {
                    return request.getExtraProperty(key);
                }

            };



            for (Enumeration keys = props.keys(); keys.hasMoreElements(); ) {
                Object key = keys.nextElement();
                myRequest.put(key, props.get(key));
            }


            String include = getWikiInclude(wikiUtil, request, importEntry,
                                            tag, props);
            if (include != null) {
                return include;
            }

            OutputHandler handler = getRepository().getOutputHandler(tag);
            if (handler == null) {
                return null;
            }
            OutputType outputType = handler.findOutputType(tag);

            String originalOutput = request.getString(ARG_OUTPUT,
                                        (String) "");
            String originalId = request.getString(ARG_ENTRYID, (String) "");
            myRequest.put(ARG_ENTRYID, importEntry.getId());
            myRequest.put(ARG_OUTPUT, outputType.getId());
            myRequest.put(ARG_EMBEDDED, "true");

            String title = null;
            String propertyValue;
            if ( !outputType.getIsHtml()) {
                List<Link> links = new ArrayList<Link>();
                handler.getEntryLinks(myRequest,
                                      new OutputHandler.State(importEntry),
                                      links);
                Link theLink = null;
                for (Link link : links) {
                    if (Misc.equals(outputType, link.getOutputType())) {
                        theLink = link;
                        break;
                    }
                }

                String url = ((theLink != null)
                              ? theLink.getUrl()
                              : myRequest.entryUrl(
                                  getRepository().URL_ENTRY_SHOW,
                                  importEntry, ARG_OUTPUT,
                                  outputType.getId()));
                String label = importEntry.getName() + " - "
                               + ((theLink != null)
                                  ? theLink.getLabel()
                                  : outputType.getLabel());
                propertyValue = getEntryManager().getTooltipLink(myRequest,
                        importEntry, label, url);
            } else {
                Result result = getEntryManager().processEntryShow(myRequest,
                                    importEntry);
                propertyValue = new String(result.getContent());
                title         = result.getTitle();
                title         = Misc.getProperty(props, "title", title);
            }

            boolean inBlock = Misc.getProperty(props, "showhide", true);
            boolean open    = Misc.getProperty(props, "open", true);

            if (inBlock && (title != null)) {
                return HtmlUtil.makeShowHideBlock(title, propertyValue, open,
                        HtmlUtil.cssClass("wiki-tocheader"),
                        HtmlUtil.cssClass("wiki-toc"));
            }
            return propertyValue;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param wikiUtil _more_
     * @param name _more_
     * @param parent _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry findWikiEntry(Request request, WikiUtil wikiUtil,
                               String name, Entry parent)
            throws Exception {
        name = name.trim();
        Entry theEntry = null;
        theEntry = getEntryManager().getEntry(request, name);
        if ((theEntry == null) && parent.isGroup()) {
            for (Entry child :
                    getEntryManager().getChildren(request, (Entry) parent)) {
                if (child.getName().trim().equalsIgnoreCase(name)) {
                    theEntry = child;
                    break;
                }
            }
        }
        return theEntry;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param textAreaId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String makeWikiEditBar(Request request, Entry entry,
                                  String textAreaId)
            throws Exception {


        String select = OutputHandler.getSelect(request, textAreaId,
                            "Add link", true, "wikilink", entry,
                            false) + HtmlUtil.space(1) + "|"
                                   + HtmlUtil.space(1)
                                   + OutputHandler.getSelect(request,
                                       textAreaId, "Import entry", true,
                                       "entryid", entry, false);

        StringBuffer buttons = new StringBuffer();
        buttons.append(addWikiEditButton(textAreaId, "button_bold.png",
                                         "Bold text", "\\'\\'\\'",
                                         "\\'\\'\\'", "Bold text",
                                         "mw-editbutton-bold"));
        buttons.append(addWikiEditButton(textAreaId, "button_italic.png",
                                         "Italic text", "\\'\\'", "\\'\\'",
                                         "Italic text",
                                         "mw-editbutton-italic"));
        buttons.append(addWikiEditButton(textAreaId, "button_link.png",
                                         "Internal link", "[[", "]]",
                                         "Link title", "mw-editbutton-link"));
        buttons.append(
            addWikiEditButton(
                textAreaId, "button_extlink.png",
                "External link (remember http:// prefix)", "[", "]",
                "http://www.example.com link title",
                "mw-editbutton-extlink"));
        buttons.append(addWikiEditButton(textAreaId, "button_headline.png",
                                         "Level 2 headline", "\\n== ",
                                         " ==\\n", "Headline text",
                                         "mw-editbutton-headline"));
        buttons.append(addWikiEditButton(textAreaId, "button_linebreak.png",
                                         "Line break", "<br>", "", "",
                                         "mw-editbutton-headline"));
        buttons.append(addWikiEditButton(textAreaId, "button_strike.png",
                                         "Strike Through", "<s>", "</s>",
                                         "Strike-through text",
                                         "mw-editbutton-headline"));
        buttons.append(addWikiEditButton(textAreaId,
                                         "button_upper_letter.png",
                                         "Super Script", "<sup>", "</sup>",
                                         "Super script text",
                                         "mw-editbutton-headline"));
        buttons.append(addWikiEditButton(textAreaId,
                                         "button_lower_letter.png",
                                         "Sub Script", "<sub>", "</sub>",
                                         "Subscript script text",
                                         "mw-editbutton-headline"));
        buttons.append(addWikiEditButton(textAreaId, "button_small.png",
                                         "Small text", "<small>", "</small>",
                                         "Small text",
                                         "mw-editbutton-headline"));
        buttons.append(addWikiEditButton(textAreaId, "button_blockquote.png",
                                         "Insert block quote",
                                         "<blockquote>", "</blockquote>",
                                         "Quoted text",
                                         "mw-editbutton-headline"));
        //        buttons.append(addWikiEditButton(textAreaId,"button_image.png","Embedded file","[[File:","]]","Example.jpg","mw-editbutton-image"));
        //        buttons.append(addWikiEditButton(textAreaId,"button_media.png","File link","[[Media:","]]","Example.ogg","mw-editbutton-media"));
        //        buttons.append(addWikiEditButton(textAreaId,"button_nowiki.png","Ignore wiki formatting","\\x3cnowiki\\x3e","\\x3c/nowiki\\x3e","Insert non-formatted text here","mw-editbutton-nowiki"));
        //        buttons.append(addWikiEditButton(textAreaId,"button_sig.png","Your signature with timestamp","--~~~~","","","mw-editbutton-signature"));
        buttons.append(addWikiEditButton(textAreaId, "button_hr.png",
                                         "Horizontal line (use sparingly)",
                                         "\\n----\\n", "", "",
                                         "mw-editbutton-hr"));

        StringBuffer propertyMenu = new StringBuffer();
        StringBuffer importMenu   = new StringBuffer();
        for (int i = 0; i < WIKIPROPS.length; i++) {
            String prop = WIKIPROPS[i];
            String js = "javascript:insertTags("
                        + HtmlUtil.squote(textAreaId) + ","
                        + HtmlUtil.squote("{{") + "," + HtmlUtil.squote("}}")
                        + "," + HtmlUtil.squote(prop) + ");";
            propertyMenu.append(HtmlUtil.href(js, prop));
            propertyMenu.append(HtmlUtil.br());

            String js2 = "javascript:insertTags("
                         + HtmlUtil.squote(textAreaId) + ","
                         + HtmlUtil.squote("{{import ") + ","
                         + HtmlUtil.squote(" " + prop + "}}") + ","
                         + HtmlUtil.squote(" entryid ") + ");";
            importMenu.append(HtmlUtil.href(js2, prop));
            importMenu.append(HtmlUtil.br());
        }

        List<Link> links = getRepository().getOutputLinks(request,
                               new OutputHandler.State(entry));


        propertyMenu.append("<hr>");
        for (Link link : links) {
            if (link.getOutputType() == null) {
                continue;
            }

            String prop = link.getOutputType().getId();
            String js = "javascript:insertTags("
                        + HtmlUtil.squote(textAreaId) + ","
                        + HtmlUtil.squote("{{") + "," + HtmlUtil.squote("}}")
                        + "," + HtmlUtil.squote(prop) + ");";
            propertyMenu.append(HtmlUtil.href(js, link.getLabel()));
            propertyMenu.append(HtmlUtil.br());
        }



        StringBuffer importOutputMenu = new StringBuffer();
        /*
                List<OutputType> allTypes = getRepository().getOutputTypes();
                //        importMenu.append("<hr>");
                for(OutputType type: allTypes) {
                    String prop = type.getId();
                    String js = "javascript:insertTags(" + HtmlUtil.squote(textAreaId)+"," +
                        HtmlUtil.squote("{{import ") +","+
                        HtmlUtil.squote(" " + type.getId()+" }}") +","+
                        HtmlUtil.squote("entryid")+");";
                    importOutputMenu.append(HtmlUtil.href(js,type.getLabel()));
                    importOutputMenu.append(HtmlUtil.br());
                }
        */


        String propertyMenuLabel =
            HtmlUtil.img(iconUrl("/icons/wiki/button_property.png"),
                         "Add Entry Property");
        String propertyButton =
            getRepository().makePopupLink(propertyMenuLabel,
                                          propertyMenu.toString());
        buttons.append(propertyButton);
        String importMenuLabel =
            HtmlUtil.img(iconUrl("/icons/wiki/button_import.png"),
                         "Import Entry Property");
        String importButton = getRepository().makePopupLink(importMenuLabel,
                                  HtmlUtil.hbox(importMenu.toString(),
                                      importOutputMenu.toString()));
        buttons.append(importButton);
        buttons.append(HtmlUtil.space(2));
        buttons.append(select);

        return buttons.toString();
    }


    /**
     * _more_
     *
     *
     * @param textAreaId _more_
     * @param icon _more_
     * @param label _more_
     * @param prefix _more_
     * @param suffix _more_
     * @param example _more_
     * @param huh _more_
     *
     * @return _more_
     */
    private String addWikiEditButton(String textAreaId, String icon,
                                     String label, String prefix,
                                     String suffix, String example,
                                     String huh) {
        String prop = prefix + example + suffix;
        String js;
        if (suffix.length() == 0) {
            js = "javascript:insertText(" + HtmlUtil.squote(textAreaId) + ","
                 + HtmlUtil.squote(prop) + ");";
        } else {
            js = "javascript:insertTags(" + HtmlUtil.squote(textAreaId) + ","
                 + HtmlUtil.squote(prefix) + "," + HtmlUtil.squote(suffix)
                 + "," + HtmlUtil.squote(example) + ");";
        }
        return HtmlUtil.href(js,
                             HtmlUtil.img(iconUrl("/icons/wiki/" + icon),
                                          label));

    }




    /**
     * _more_
     *
     * @param wikiUtil _more_
     * @param name _more_
     * @param label _more_
     *
     * @return _more_
     */
    public String getWikiLink(WikiUtil wikiUtil, String name, String label) {
        try {
            Entry   entry   = (Entry) wikiUtil.getProperty(PROP_ENTRY);
            Request request = (Request) wikiUtil.getProperty(PROP_REQUEST);
            Entry   parent  = entry.getParentEntry();


            name = name.trim();
            if (name.startsWith("Category:")) {
                String category = name.substring("Category:".length());
                String url =
                    request.url(
                        getRepository().getSearchManager().URL_ENTRY_SEARCH,
                        ARG_METADATA_TYPE + ".wikicategory", "wikicategory",
                        ARG_METADATA_ATTR1 + ".wikicategory", category);
                wikiUtil.addCategoryLink(HtmlUtil.href(url, category));
                List categories =
                    (List) wikiUtil.getProperty("wikicategories");
                if (categories == null) {
                    wikiUtil.putProperty("wikicategories",
                                         categories = new ArrayList());
                }
                categories.add(category);
                return "";
            }

            Entry theEntry = null;
            //If the entry is a group first check its children.
            if (entry.isGroup()) {
                theEntry = findWikiEntry(request, wikiUtil, name,
                                         (Entry) entry);
            }
            if (theEntry == null) {
                theEntry = findWikiEntry(request, wikiUtil, name, parent);
            }

            if (theEntry != null) {
                addWikiLink(wikiUtil, theEntry);
                if (label.trim().length() == 0) {
                    label = theEntry.getName();
                }
                if (theEntry.getType().equals(TYPE_WIKIPAGE)) {
                    String url =
                        request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                         theEntry, ARG_OUTPUT, OUTPUT_WIKI);
                    return getEntryManager().getTooltipLink(request,
                            theEntry, label, url);

                } else {
                    return getEntryManager().getTooltipLink(request,
                            theEntry, label, null);
                }
            }


            String url = request.url(getRepository().URL_ENTRY_FORM,
                                     ARG_NAME, name, ARG_GROUP,
                                     (entry.isGroup()
                                      ? entry.getId()
                                      : parent.getId()), ARG_TYPE,
                                          TYPE_WIKIPAGE);

            return HtmlUtil.href(url, name,
                                 HtmlUtil.cssClass("wiki-link-noexist"));
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param wikiContent _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String wikifyEntry(Request request, Entry entry,
                              String wikiContent)
            throws Exception {
        return wikifyEntry(request, entry, wikiContent, true, null, null);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param wikiContent _more_
     * @param wrapInDiv _more_
     * @param subGroups _more_
     * @param subEntries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String wikifyEntry(Request request, Entry entry,
                              String wikiContent, boolean wrapInDiv,
                              List<Entry> subGroups, List<Entry> subEntries)
            throws Exception {
        WikiUtil wikiUtil = new WikiUtil(Misc.newHashtable(new Object[] {
                                PROP_REQUEST,
                                request, PROP_ENTRY, entry }));
        return wikifyEntry(request, entry, wikiUtil, wikiContent, wrapInDiv,
                           subGroups, subEntries);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param wikiUtil _more_
     * @param wikiContent _more_
     * @param wrapInDiv _more_
     * @param subGroups _more_
     * @param subEntries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String wikifyEntry(Request request, Entry entry,
                              WikiUtil wikiUtil, String wikiContent,
                              boolean wrapInDiv, List<Entry> subGroups,
                              List<Entry> subEntries)
            throws Exception {
        List children = new ArrayList();
        if (subGroups != null) {
            wikiUtil.putProperty(entry.getId() + "_subgroups", subGroups);
            children.addAll(subGroups);
        }

        if (subEntries != null) {
            wikiUtil.putProperty(entry.getId() + "_subentries", subEntries);
            children.addAll(subEntries);
        }

        wikiUtil.putProperty(entry.getId() + "_children", children);


        //TODO: We need to keep track of what is getting called so we prevent
        //infinite loops
        String content = wikiUtil.wikify(wikiContent, this);
        if ( !wrapInDiv) {
            return content;
        }
        return HtmlUtil.div(content, HtmlUtil.cssClass("wikicontent"));

    }

    /**
     * _more_
     *
     * @param wikiUtil _more_
     * @param toEntry _more_
     */
    public void addWikiLink(WikiUtil wikiUtil, Entry toEntry) {
        if (toEntry == null) {
            return;
        }
        Hashtable links = (Hashtable) wikiUtil.getProperty("wikilinks");
        if (links == null) {
            wikiUtil.putProperty("wikilinks", links = new Hashtable());
        }
        links.put(toEntry, toEntry);
    }




}
