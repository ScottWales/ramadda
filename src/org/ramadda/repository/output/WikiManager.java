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
 */
public class WikiManager extends RepositoryManager implements WikiUtil
    .WikiPageHandler {

    /** wiki page type */
    public static String TYPE_WIKIPAGE = "wikipage";



    /** attribute in import tag */
    public static final String PROP_ENTRY = "entry";

    /** _more_ */
    public static final String PROP_ALT = "alt";

    /** _more_ */
    public static final String PROP_MESSAGE = "message";

    /** _more_ */
    public static final String PROP_ASSOCIATIONS = "associations";

    /** attribute in import tag */
    public static final String PROP_SHOWHIDE = "showhide";

    /** attribute in import tag */
    public static final String PROP_OUTPUT = "output";

    /** attribute in import tag */
    public static final String PROP_RANDOM = "random";

    /** attribute in import tag */
    public static final String PROP_CHILDREN = "children";

    /** attribute in import tag */
    public static final String PROP_FORMAT = "format";

    /** attribute in import tag */
    public static final String PROP_SHOWTOGGLE = "showtoggle";

    /** attribute in import tag */
    public static final String PROP_OPEN = "open";

    /** attribute in import tag */
    public static final String PROP_FILES = "files";

    /** attribute in import tag */
    public static final String PROP_FOLDERS = "folders";

    /** attribute in import tag */
    public static final String PROP_TYPE = "type";

    /** attribute in import tag */
    public static final String PROP_SEPARATOR = "separator";

    /** attribute in import tag */
    public static final String PROP_CLASS = "class";

    /** attribute in import tag */
    public static final String PROP_STYLE = "style";

    /** attribute in import tag */
    public static final String PROP_TAGOPEN = "tagopen";

    /** attribute in import tag */
    public static final String PROP_TAGCLOSE = "tagclose";

    /** attribute in import tag */
    public static final String PROP_TITLE = "title";

    /** attribute in import tag */
    public static final String PROP_LAYOUT = "layout";

    /** attribute in import tag */
    public static final String LAYOUT_HORIZONTAL = "hor";

    /** attribute in import tag */
    public static final String LAYOUT_VERTICAL = "vert";

    /** attribute in import tag */
    public static final String PROP_MENUS = "menus";

    /** attribute in import tag */
    public static final String PROP_REQUEST = "request";


    /** attribute in import tag */
    public static final String PROP_POPUP = "popup";

    /** attribute in import tag */
    public static final String PROP_LEVEL = "level";

    /** attribute in import tag */
    public static final String PROP_COUNT = "count";

    /** attribute in import tag */
    public static final String PROP_WIDTH = "width";

    /** _more_          */
    public static final String PROP_WIKIFY = "wikify";

    /** attribute in import tag */
    public static final String PROP_HEIGHT = "height";

    /** attribute in import tag */
    public static final String PROP_DAYS = "days";

    /** wiki import */
    public static final String WIKIPROP_IMPORT = "import";

    /** _more_          */
    public static final String WIKIPROP_FIELD = "field";

    /** _more_ */
    public static final String WIKIPROP_CALENDAR = "calendar";

    /** _more_ */
    public static final String WIKIPROP_TIMELINE = "timeline";

    /** wiki import */
    public static final String WIKIPROP_DATE = "date";

    /** wiki import */
    public static final String WIKIPROP_DATE_FROM = "fromdate";

    /** wiki import */
    public static final String WIKIPROP_DATE_TO = "todate";

    /** wiki import */
    public static final String WIKIPROP_MENU = "menu";

    /** wiki import */
    public static final String WIKIPROP_TREE = "tree";

    /** _more_          */
    public static final String WIKIPROP_TABLE = "table";

    /** wiki import */
    public static final String WIKIPROP_COMMENTS = "comments";

    /** wiki import */
    public static final String WIKIPROP_RECENT = "recent";

    /** wiki import */
    public static final String WIKIPROP_GALLERY = "gallery";

    /** _more_ */
    public static final String WIKIPROP_PLAYER = "player";

    /** wiki import */
    public static final String WIKIPROP_TABS = "tabs";

    /** wiki import */
    public static final String WIKIPROP_GRID = "grid";

    /** wiki import */
    public static final String WIKIPROP_TOOLBAR = "toolbar";

    /** wiki import */
    public static final String WIKIPROP_BREADCRUMBS = "breadcrumbs";

    /** wiki import */
    public static final String WIKIPROP_INFORMATION = "information";

    /** wiki import */
    public static final String WIKIPROP_IMAGE = "image";

    /** wiki import */
    public static final String WIKIPROP_NAME = "name";

    /** wiki import */
    public static final String WIKIPROP_MAP = "map";

    /** wiki import */
    public static final String WIKIPROP_EARTH = "earth";

    /** wiki import */
    public static final String WIKIPROP_HTML = "html";

    /** wiki import */
    public static final String WIKIPROP_MAPENTRY = "mapentry";

    /** wiki import */
    public static final String WIKIPROP_DESCRIPTION = "description";

    /** wiki import */
    public static final String WIKIPROP_PROPERTIES = "properties";

    /** wiki import */
    public static final String WIKIPROP_LINKS = "links";

    /** wiki import */
    public static final String WIKIPROP_ENTRYID = "entryid";

    /** wiki import */
    public static final String WIKIPROP_LAYOUT = "layout";

    /** wiki import */
    public static final String WIKIPROP_ = "";

    /** wiki import */
    public static final String WIKIPROP_CHILDREN_GROUPS = "subgroups";

    /** wiki import */
    public static final String WIKIPROP_CHILDREN_ENTRIES = "subentries";

    /** wiki import */
    public static final String WIKIPROP_CHILDREN = "children";

    /** wiki import */
    public static final String WIKIPROP_URL = "url";


    /** _more_ */
    public static final String ATTR_COLUMNS = "columns";

    /** _more_          */
    public static final String ATTR_FIELDNAME = "name";


    /** list of import items for the text editor menu */
    public static final String[] WIKIPROPS = {
        WIKIPROP_INFORMATION, WIKIPROP_NAME, WIKIPROP_DESCRIPTION,
        WIKIPROP_DATE_FROM, WIKIPROP_DATE_TO, WIKIPROP_FIELD, WIKIPROP_LAYOUT,
        WIKIPROP_PROPERTIES, WIKIPROP_HTML, WIKIPROP_MAP, WIKIPROP_MAPENTRY,
        WIKIPROP_EARTH, WIKIPROP_CALENDAR, WIKIPROP_TIMELINE,
        WIKIPROP_COMMENTS, WIKIPROP_BREADCRUMBS, WIKIPROP_TOOLBAR,
        WIKIPROP_IMAGE, WIKIPROP_MENU, WIKIPROP_RECENT, WIKIPROP_GALLERY,
        WIKIPROP_TABS, WIKIPROP_GRID, WIKIPROP_TREE, WIKIPROP_TABLE,
        WIKIPROP_LINKS, WIKIPROP_ENTRYID
    };

    /** _more_ */
    public static final String ID_THIS = "this";

    /** _more_ */
    public static final String ID_PARENT = "parent";

    /** _more_ */
    public static final String ID_GRANDPARENT = "grandparent";


    /** default label */
    public static final String LABEL_LINKS = "Actions";

    /** output type */
    public static final OutputType OUTPUT_WIKI = new OutputType("Wiki",
                                                     "wiki.view",
                                                     OutputType.TYPE_VIEW,
                                                     "", ICON_WIKI);



    /**
     * ctor
     *
     * @param repository the repository
     */
    public WikiManager(Repository repository) {
        super(repository);
    }



    /**
     * _more_
     *
     * @param wikiUtil The wiki util
     * @param property _more_
     *
     * @return _more_
     */
    public String getWikiPropertyValue(WikiUtil wikiUtil, String property) {

        try {
            Entry   entry   = (Entry) wikiUtil.getProperty(PROP_ENTRY);
            Request request = (Request) wikiUtil.getProperty(PROP_REQUEST);
            //Check for infinite loop
            property = property.trim();
            if (property.length() == 0) {
                return "";
            }

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
                //Old style
                if (remainder.indexOf("=") < 0) {
                    toks = StringUtil.splitUpTo(remainder, " ", 3);
                    if (toks.size() < 2) {
                        return "<b>Incorrect import specification:"
                               + property + "</b>";
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
                        return "<b>Could not find entry&lt;" + id
                               + "&gt;</b>";
                    }
                }
            }

            Hashtable props   = StringUtil.parseHtmlProperties(remainder);
            String    entryId = (String) props.get(PROP_ENTRY);

            if (entryId != null) {
                theEntry = null;
                int barIndex = entryId.indexOf("|");
                if (barIndex >= 0) {
                    entryId = entryId.substring(0, barIndex);
                }
                if (entryId.equals(ID_THIS)) {
                    theEntry = entry;
                }

                if (theEntry == null) {
                    if (entryId.equals(ID_PARENT)) {
                        theEntry = getEntryManager().getEntry(request,
                                entry.getParentEntryId());
                    }
                }

                if (theEntry == null) {
                    if (entryId.equals(ID_GRANDPARENT)) {
                        theEntry = getEntryManager().getEntry(request,
                                entry.getParentEntryId());
                        if (theEntry != null) {
                            theEntry = getEntryManager().getEntry(request,
                                    theEntry.getParentEntryId());
                        }
                    }
                }

                if (theEntry == null) {
                    theEntry = getEntryManager().getEntry(request, entryId);
                }
                if (theEntry == null) {
                    theEntry = findWikiEntry(request, wikiUtil, entryId,
                                             entry);
                }

                if (theEntry == null) {
                    return "Unknown entry:" + entryId;
                }
            }


            String propertyKey = theEntry.getId() + "_" + property;
            if (request.getExtraProperty(propertyKey) != null) {
                return "<b>Detected circular wiki import:" + property
                       + "</b>";
            }
            request.putExtraProperty(propertyKey, property);


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
     * @param wikiUtil The wiki util
     * @param request The request
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
        String alt   = (String) props.get(HtmlUtil.ATTR_ALT);
        String extra = "";

        if (width != null) {
            extra = extra + HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, width);
        }

        if (alt != null) {
            extra = extra + HtmlUtil.attr(HtmlUtil.ATTR_ALT, alt);
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
     * @param wikiUtil The wiki util
     * @param request The request
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
            return getMessage(props, msgLabel("Could not find src") + src);
        }

        return request.entryUrl(getRepository().URL_ENTRY_SHOW, srcEntry);

    }


    /**
     * _more_
     *
     * @param wikiUtil The wiki util
     * @param request The request
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
            return getMessage(props, msgLabel("Could not find src") + src);
        }
        if (attachment == null) {
            if ( !srcEntry.getResource().isImage()) {
                return getMessage(props, msg("Not an image"));
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

        return getMessage(props,
                          msgLabel("Could not find image attachment")
                          + attachment);
    }



    /**
     * _more_
     *
     * @param props _more_
     * @param message _more_
     *
     * @return _more_
     */
    public String getMessage(Hashtable props, String message) {
        return Misc.getProperty(props, PROP_MESSAGE, message);
    }



    /**
     * _more_
     *
     * @param wikiUtil The wiki util
     * @param request The request
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

        String hasChildren = (String) wikiUtil.getProperty(entry.getId()
                                 + "_haschildren");

        boolean hasOpenProperty = props.get(PROP_OPEN) != null;

        boolean open = Misc.getProperty(props, PROP_OPEN,
                                        ((hasChildren != null)
                                         ? hasChildren.equals("false")
                                         : true));
        boolean inBlock      = Misc.getProperty(props, PROP_SHOWTOGGLE, true);
        String  blockContent = null;
        String  blockTitle   = "";
        boolean doBG         = true;

        if (include.equals(WIKIPROP_INFORMATION)) {
            blockContent =
                getRepository().getHtmlOutputHandler().getInformationTabs(
                    request, entry, false, true);
            blockTitle = Misc.getProperty(props, PROP_TITLE,
                                          msg("Information"));
        } else if (include.equals(WIKIPROP_COMMENTS)) {
            return getHtmlOutputHandler().getCommentBlock(request, entry,
                    false).toString();
        } else if (include.equals(WIKIPROP_TOOLBAR)) {
            return getEntryManager().getEntryToolbar(request, entry);
        } else if (include.equals(WIKIPROP_BREADCRUMBS)) {
            return getEntryManager().getBreadCrumbs(request, entry);
        } else if (include.equals(WIKIPROP_DESCRIPTION)) {
            String desc = entry.getDescription();
            desc = desc.replaceAll("\r\n\r\n", "\n<p>\n");
            if (Misc.getProperty(props, PROP_WIKIFY, false)) {
                desc = new WikiUtil().wikify("{{noheading}}\n" + desc, null);
            }
            return desc;
        } else if (include.equals(WIKIPROP_LAYOUT)) {
            return getHtmlOutputHandler().makeHtmlHeader(request, entry,
                    Misc.getProperty(props, PROP_TITLE, "Layout"));
        } else if (include.equals(WIKIPROP_NAME)) {
            return entry.getName();
        } else if (include.equals(WIKIPROP_FIELD)) {
            String name = Misc.getProperty(props, ATTR_FIELDNAME,
                                           (String) null);
            if (name != null) {
                String fieldValue =
                    entry.getTypeHandler().getFieldHtml(request, entry, name);
                if (fieldValue != null) {
                    return fieldValue;
                }
                return "Could not find field: " + name;
            } else {
                return "No name=... specified in wiki tag";
            }
        } else if (include.equals(WIKIPROP_DATE_FROM)
                   || include.equals(WIKIPROP_DATE_TO)) {
            String format =
                Misc.getProperty(props, PROP_FORMAT,
                                 RepositoryBase.DEFAULT_TIME_FORMAT);
            Date date = new Date(include.equals(WIKIPROP_DATE_FROM)
                                 ? entry.getStartDate()
                                 : entry.getEndDate());
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(RepositoryUtil.TIMEZONE_DEFAULT);
            return dateFormat.format(date);
        } else if (include.equals(WIKIPROP_ENTRYID)) {
            return entry.getId();
        } else if (include.equals(WIKIPROP_PROPERTIES)) {
            return makeEntryTabs(request, entry);
        } else if (include.equals(WIKIPROP_IMAGE)) {
            return getWikiImage(wikiUtil, request, entry, props);
        } else if (include.equals(WIKIPROP_URL)) {
            return getWikiUrl(wikiUtil, request, entry, props);
        } else if (include.equals(WIKIPROP_HTML)) {
            if (Misc.getProperty(props, PROP_CHILDREN, false)) {
                List<Entry> children = getEntries(request, wikiUtil, entry,
                                           props);
                StringBuffer sb = new StringBuffer();
                for (Entry child : children) {
                    Result result =
                        getHtmlOutputHandler().getHtmlResult(request,
                            OutputHandler.OUTPUT_HTML, child);
                    sb.append(getEntryManager().getEntryLink(request, child));
                    sb.append(HtmlUtil.br());
                    sb.append(new String(result.getContent()));
                    sb.append(HtmlUtil.p());
                }
                return sb.toString();
            }

            Result result = getHtmlOutputHandler().getHtmlResult(request,
                                OutputHandler.OUTPUT_HTML, entry);
            return new String(result.getContent());

        } else if (include.equals(WIKIPROP_CALENDAR)) {
            StringBuffer calendarSB = new StringBuffer();
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            boolean doDay = Misc.getProperty(props, "day", false);
            getCalendarOutputHandler().outputCalendar(request,
                    getCalendarOutputHandler().makeCalendarEntries(request,
                        children), calendarSB, doDay);

            return calendarSB.toString();
        } else if (include.equals(WIKIPROP_TIMELINE)) {
            StringBuffer calendarSB = new StringBuffer();
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);

            int    height = Misc.getProperty(props, PROP_HEIGHT, 150);
            String style  = "height: " + height + "px;";
            String head = getHtmlOutputHandler().makeTimeline(request,
                              children, calendarSB, style);
            StringBuffer html = new StringBuffer();
            if (head != null) {
                request.putExtraProperty(PROP_HTML_HEAD, head);
            }
            html.append(calendarSB);
            return html.toString();
        } else if (include.equals(WIKIPROP_MAP)
                   || include.equals(WIKIPROP_EARTH)) {
            StringBuffer mapSB  = new StringBuffer();
            int          width  = Misc.getProperty(props, PROP_WIDTH, 400);
            int          height = Misc.getProperty(props, PROP_HEIGHT, 300);
            boolean justPoints  = Misc.getProperty(props, "justpoints",
                                      false);
            boolean listEntries = Misc.getProperty(props, "listentries",
                                      true);
            boolean googleEarth =
                include.equals(WIKIPROP_EARTH)
                && getMapManager().isGoogleEarthEnabled(request);

            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);

            if (googleEarth) {
                getMapManager().getGoogleEarth(request, children, mapSB,
                        Misc.getProperty(props, PROP_WIDTH, -1),
                        Misc.getProperty(props, PROP_HEIGHT, -1),
                        listEntries, justPoints);
            } else {
                MapOutputHandler mapOutputHandler =
                    (MapOutputHandler) getRepository().getOutputHandler(
                        MapOutputHandler.OUTPUT_MAP);
                if (mapOutputHandler == null) {
                    return "No maps";
                }
                boolean[] haveBearingLines = { false };
                MapInfo map = mapOutputHandler.getMap(request, children,
                                  mapSB, width, height, haveBearingLines);
            }
            return mapSB.toString();
        } else if (include.equals(WIKIPROP_MAPENTRY)) {
            StringBuffer mapSB = new StringBuffer();
            int          width = Misc.getProperty(props, PROP_WIDTH, 400);
            int height = Misc.getProperty(props, PROP_HEIGHT, 300);
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
        } else if (include.equals(WIKIPROP_MENU)) {
            boolean popup = Misc.getProperty(props, PROP_POPUP, false);
            String  menus = Misc.getProperty(props, PROP_MENUS, "");
            int type = OutputType.getTypeMask(StringUtil.split(menus, ",",
                           true, true));
            blockTitle = Misc.getProperty(props, PROP_TITLE,
                                          msg(LABEL_LINKS));
            blockContent = getEntryManager().getEntryActionsTable(request,
                    entry, type);
            if (popup) {
                return getRepository().makePopupLink(blockTitle,
                        blockContent);
            }
        } else if (include.equals(WIKIPROP_TABS)) {
            List        tabTitles   = new ArrayList<String>();
            List        tabContents = new ArrayList<String>();
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            boolean showDescription = Misc.getProperty(props, "showdescription", true);
            boolean wikify  = Misc.getProperty(props, PROP_WIKIFY, true);

            for (Entry child : children) {
                tabTitles.add(child.getName());
                String content;
                if(!showDescription)  {
                    content = 
                        getRepository().getHtmlOutputHandler().getInformationTabs(
                                                                                  request, child, false, true);
                } else {
                    content = child.getDescription();
                    if(wikify) {
                        content = new WikiUtil().wikify("{{noheading}}\n" + content, null);
                    }
                    if(child.getResource().isImage()) {
                        content = HtmlUtil.img(getRepository().getHtmlOutputHandler().getImageUrl(request, child)) +"<br>"+content;
                    }
                    
                }

                String href = HtmlUtil.href(
                                  request.entryUrl(
                                      getRepository().URL_ENTRY_SHOW,
                                      child), child.getName());
                tabContents.add(content + "<br>" + href);
            }
            return OutputHandler.makeTabs(tabTitles, tabContents, true);

        } else if (include.equals(WIKIPROP_GRID)) {
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            StringBuffer sb = new StringBuffer();
            getHtmlOutputHandler().makeGrid(request, children, sb);
            return sb.toString();
        } else if (include.equals(WIKIPROP_TABLE)) {
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            StringBuffer sb = new StringBuffer();
            getHtmlOutputHandler().makeTable(request, children, sb);
            return sb.toString();
        } else if (include.equals(WIKIPROP_RECENT)) {
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            int numDays = Misc.getProperty(props, PROP_DAYS, 3);
            StringBuffer        sb  = new StringBuffer();
            BufferMapList<Date> map = new BufferMapList<Date>();
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

        } else if (include.equals(WIKIPROP_PLAYER)) {
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            List<Entry> imageEntries = new ArrayList<Entry>();
            for (Entry child : children) {
                if ( !child.getResource().isImage()) {
                    continue;
                }
                imageEntries.add(child);
            }
            if (imageEntries.size() == 0) {
                return getMessage(props, "");
            }
            StringBuffer sb = new StringBuffer();
            ImageOutputHandler imageOutputHandler =
                (ImageOutputHandler) getRepository().getOutputHandler(
                    ImageOutputHandler.OUTPUT_PLAYER);
            imageOutputHandler.makePlayer(request, imageEntries, sb, false);
            return sb.toString();
        } else if (include.equals(WIKIPROP_GALLERY)) {
            int         count    = Misc.getProperty(props, PROP_COUNT, -1);
            int         width    = Misc.getProperty(props, PROP_WIDTH, -1);
            int         columns  = Misc.getProperty(props, ATTR_COLUMNS, 1);

            boolean     random   = Misc.getProperty(props, PROP_RANDOM,
                                       false);

            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            StringBuffer sb        = new StringBuffer();

            List<Entry>  onesToUse = new ArrayList<Entry>();
            for (Entry child : children) {
                if ( !child.getResource().isImage()) {
                    continue;
                }
                onesToUse.add(child);
            }
            int size = onesToUse.size();
            if (random && (size > 1)) {
                int randomIdx = (int) (Math.random() * size);
                if (randomIdx >= size) {
                    randomIdx = size;
                }
                Entry randomEntry = onesToUse.get(randomIdx);
                onesToUse = new ArrayList<Entry>();
                onesToUse.add(randomEntry);
            }


            int num    = 0;
            int colCnt = 0;
            sb.append("<table cellspacing=4>");
            sb.append("<tr valign=\"bottom\">");
            for (Entry child : onesToUse) {
                num++;
                if ((count > 0) && (num > count)) {
                    break;
                }
                if (colCnt >= columns) {
                    sb.append("</tr>");
                    sb.append("<tr valign=\"bottom\">");
                    colCnt = 0;
                }
                colCnt++;
                String url = HtmlUtil.url(
                                 request.url(repository.URL_ENTRY_GET) + "/"
                                 + getStorageManager().getFileTail(
                                     child), ARG_ENTRYID, child.getId());
                sb.append("<td align=\"center\">");
                if (width <= 0) {
                    sb.append(HtmlUtil.img(url));
                } else {
                    sb.append(HtmlUtil.img(url, "",
                                           HtmlUtil.attr(HtmlUtil.ATTR_WIDTH,
                                               "" + width)));
                }
                sb.append(HtmlUtil.br());
                if ( !random) {
                    sb.append("<b>");
                    sb.append(msg("Figure"));
                    sb.append(" " + num);
                    sb.append(" - ");
                    sb.append("</b>");
                }
                sb.append(getEntryManager().getAjaxLink(request, child,
                        child.getLabel()));
                sb.append("</td>");
                //              sb.append(HtmlUtil.br());
                //              sb.append(HtmlUtil.makeToggleInline("...", child.getDescription(),false));
            }
            sb.append("</table>");
            return sb.toString();
        } else if (include.equals(WIKIPROP_CHILDREN_GROUPS)) {
            if ( !hasOpenProperty) {
                open = true;
            }
            doBG = false;
            props.put(PROP_FOLDERS, "true");
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            if (children.size() == 0) {
                return getMessage(props, "");
            }
            StringBuffer sb = new StringBuffer();
            String link = getHtmlOutputHandler().getEntriesList(request, sb,
                              children, true, true, true, false);
            blockContent = sb.toString();
            blockTitle = Misc.getProperty(props, PROP_TITLE, msg("Folders"))
                         + link;
        } else if (include.equals(WIKIPROP_CHILDREN_ENTRIES)) {
            if ( !hasOpenProperty) {
                open = true;
            }
            doBG = false;
            props.put(PROP_FILES, "true");
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            if (children.size() == 0) {
                return getMessage(props, "");
            }

            StringBuffer sb = new StringBuffer();
            String link = getHtmlOutputHandler().getEntriesList(request, sb,
                              children, true, true, true, false);
            blockContent = sb.toString();
            blockTitle = Misc.getProperty(props, PROP_TITLE, msg("Folders"))
                         + link;
        } else if (include.equals(WIKIPROP_CHILDREN)
                   || include.equals(WIKIPROP_TREE)) {
            if ( !hasOpenProperty) {
                open = true;
            }
            doBG = false;
            StringBuffer sb      = new StringBuffer();
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            if (children.size() == 0) {
                return getMessage(props, "");
            }
            String link = getHtmlOutputHandler().getEntriesList(request, sb,
                              children, true, true, true, false);
            blockContent = sb.toString();
            blockTitle = Misc.getProperty(props, PROP_TITLE, msg("Links"))
                         + link;
        } else if (include.equals(WIKIPROP_LINKS)) {
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            if (children.size() == 0) {
                return getMessage(props, "");
            }

            String separator = Misc.getProperty(props, PROP_SEPARATOR,
                                   "&nbsp;|&nbsp;");
            String       cssClass = Misc.getProperty(props, PROP_CLASS, "");
            String       style = Misc.getProperty(props, PROP_STYLE, "style");
            String       tagOpen  = Misc.getProperty(props, PROP_TAGOPEN, "");
            String       tagClose = Misc.getProperty(props, PROP_TAGCLOSE,
                                        "");

            StringBuffer sb       = new StringBuffer();
            List<String> links    = new ArrayList<String>();
            for (Entry child : children) {
                String href = HtmlUtil.href(
                                  request.entryUrl(
                                      getRepository().URL_ENTRY_SHOW,
                                      child), child.getName(),
                                          HtmlUtil.cssClass(cssClass)
                                          + HtmlUtil.style(style));
                links.add(tagOpen + href + tagClose);
            }
            return StringUtil.join(separator, links);
        } else {
            return null;
        }

        if ( !inBlock) {
            return blockContent;
        }
        //        System.err.println(hasOpenProperty+ " " + open);
        if (doBG) {
            return HtmlUtil.makeShowHideBlock(blockTitle, blockContent, open,
                    HtmlUtil.cssClass("toggleblocklabel"), "");
            //            HtmlUtil.cssClass("wiki-tocheader"),  HtmlUtil.cssClass("wiki-toc"));
        } else {
            return HtmlUtil.makeShowHideBlock(blockTitle, blockContent, open);
        }

    }

    /**
     * _more_
     *
     * @param request The request
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String makeEntryTabs(Request request, Entry entry)
            throws Exception {
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
            return OutputHandler.makeTabs(tabTitles, tabContents, true);
        }
        return tabContents.get(0).toString();

    }

    /**
     * _more_
     *
     * @param request The request
     * @param wikiUtil The wiki util
     * @param entry _more_
     * @param props _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry> getEntries(Request request, WikiUtil wikiUtil,
                                  Entry entry, Hashtable props)
            throws Exception {
        boolean folders = Misc.getProperty(props, PROP_FOLDERS, false);
        boolean files   = Misc.getProperty(props, PROP_FILES, false);
        boolean doAssociations = Misc.getProperty(props, PROP_ASSOCIATIONS,
                                     false);

        if (doAssociations) {
            List<Association> associations =
                getRepository().getAssociationManager().getAssociations(
                    request, entry.getId());
            List<Entry> linkedEntries = new ArrayList<Entry>();
            for (Association association : associations) {
                String id = null;
                if ( !association.getFromId().equals(entry.getId())) {
                    id = association.getFromId();
                } else if ( !association.getToId().equals(entry.getId())) {
                    id = association.getToId();
                } else {
                    continue;
                }
                linkedEntries.add(getEntryManager().getEntry(request, id));
            }
            return linkedEntries;
        }

        String type  = (String) props.get(PROP_TYPE);
        int    level = Misc.getProperty(props, PROP_LEVEL, 1);
        List<Entry> children =
            (List<Entry>) wikiUtil.getProperty(entry.getId() + "_children");
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
        if (folders) {
            List<Entry> tmp = new ArrayList<Entry>();
            for (Entry child : children) {
                if (child.isGroup()) {
                    tmp.add(child);
                }
            }
            children = tmp;
        } else if (files) {
            List<Entry> tmp = new ArrayList<Entry>();
            for (Entry child : children) {
                if ( !child.isGroup()) {
                    tmp.add(child);
                }
            }
            children = tmp;
        }
        if (type != null) {
            List<Entry> tmp = new ArrayList<Entry>();
            for (Entry child : children) {
                if (Misc.equals(child.getType(), type)) {
                    tmp.add(child);
                }
            }
            children = tmp;
        }


        return children;

    }


    /**
     * utility to get the htmloutputhandler
     *
     * @return htmloutputhandler
     */
    public HtmlOutputHandler getHtmlOutputHandler() {
        return getRepository().getHtmlOutputHandler();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public CalendarOutputHandler getCalendarOutputHandler() {
        try {
            return (CalendarOutputHandler) getRepository().getOutputHandler(
                CalendarOutputHandler.OUTPUT_CALENDAR);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }



    /**
     * _more_
     *
     * @param wikiUtil The wiki util
     * @param request The request
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
            if ( !tag.equals(WIKIPROP_IMPORT)) {
                String include = getWikiInclude(wikiUtil, request,
                                     importEntry, tag, props);
                if (include != null) {
                    return include;
                }
            } else {
                tag = Misc.getProperty(props, PROP_OUTPUT,
                                       OutputHandler.OUTPUT_HTML.getId());
            }

            OutputHandler handler = getRepository().getOutputHandler(tag);
            if (handler == null) {
                return null;
            }
            OutputType outputType = handler.findOutputType(tag);

            String originalOutput = request.getString(ARG_OUTPUT,
                                        (String) "");
            String originalId = request.getString(ARG_ENTRYID, (String) "");

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

            myRequest.put(ARG_ENTRYID, importEntry.getId());
            myRequest.put(ARG_OUTPUT, outputType.getId());
            myRequest.put(ARG_EMBEDDED, "true");

            String title = null;
            String propertyValue;
            if ( !outputType.getIsView()) {
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
                title         = Misc.getProperty(props, PROP_TITLE, title);
            }

            boolean inBlock = Misc.getProperty(props, PROP_SHOWHIDE, true);
            boolean open    = Misc.getProperty(props, PROP_OPEN, true);

            if (inBlock && (title != null)) {
                return HtmlUtil.makeShowHideBlock(title, propertyValue, open,
                        HtmlUtil.cssClass(CSS_CLASS_HEADING_2), "");
                //                        HtmlUtil.cssClass("wiki-tocheader"),   HtmlUtil.cssClass("wiki-toc"));
            }
            return propertyValue;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * _more_
     *
     * @param request The request
     * @param wikiUtil The wiki util
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
        if (parent.isGroup()) {
            for (Entry child :
                    getEntryManager().getChildren(request, (Entry) parent)) {
                if (child.getName().trim().equalsIgnoreCase(name)) {
                    return child;
                }
            }
        }
        theEntry = getEntryManager().getEntry(request, name);
        if (theEntry != null) {
            return theEntry;
        }
        return theEntry;
    }


    /**
     * _more_
     *
     * @param request The request
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
            //            System.out.println(prop);
            String js = "javascript:insertTags("
                        + HtmlUtil.squote(textAreaId) + ","
                        + HtmlUtil.squote("{{") + ","
                        + HtmlUtil.squote(" }}") + ","
                        + HtmlUtil.squote(prop) + ");";
            propertyMenu.append(HtmlUtil.href(js, prop));
            propertyMenu.append(HtmlUtil.br());

            String js2 = "javascript:insertTags("
                         + HtmlUtil.squote(textAreaId) + ","
                         + HtmlUtil.squote("{{" + prop + " ") + ","
                         + HtmlUtil.squote("}}") + "," + HtmlUtil.squote("")
                         + ");";
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
        //        buttons.append(propertyButton);

        String importMenuLabel = msg("Add property");
        //            HtmlUtil.img(iconUrl("/icons/wiki/button_import.png"),
        //                         "Import Entry Property");
        String importButton = getRepository().makePopupLink(importMenuLabel,
                                  HtmlUtil.hbox(importMenu.toString(),
                                      importOutputMenu.toString()));
        String addEntry = OutputHandler.getSelect(request, textAreaId,
                              "Add entry id", true, "entryid", entry, false);

        String addLink = OutputHandler.getSelect(request, textAreaId,
                             "Add entry link", true, "wikilink", entry,
                             false);

        buttons.append(HtmlUtil.space(2));
        buttons.append(importButton);
        buttons.append(HtmlUtil.space(2));
        buttons.append(addEntry);
        buttons.append(HtmlUtil.space(2));
        buttons.append(addLink);

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
     * @param wikiUtil The wiki util
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
            theEntry = getEntryManager().findEntryWithName(request,
                    (Entry) entry, name);

            //If the entry is a group first check its children.
            if (theEntry == null) {
                if (entry.isGroup()) {
                    theEntry = findWikiEntry(request, wikiUtil, name,
                                             (Entry) entry);
                }
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
     * @param request The request
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
     * @param request The request
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
     * @param request The request
     * @param entry _more_
     * @param wikiUtil The wiki util
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
        List    children    = new ArrayList();
        boolean hasChildren = false;
        if (subGroups != null) {
            hasChildren = subGroups.size() > 0;
            wikiUtil.putProperty(entry.getId() + "_subgroups", subGroups);
            children.addAll(subGroups);
        }

        if (subEntries != null) {
            hasChildren |= subEntries.size() > 0;
            wikiUtil.putProperty(entry.getId() + "_subentries", subEntries);
            children.addAll(subEntries);
        }

        wikiUtil.putProperty(entry.getId() + "_children", children);
        wikiUtil.putProperty(entry.getId() + "_haschildren",
                             "" + hasChildren);


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
     * @param wikiUtil The wiki util
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
