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
import java.util.HashSet;
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

    /** id counter */
    static int idCounter = 0;

    /** wiki page type */
    public static String TYPE_WIKIPAGE = "wikipage";

    /** attribute in import tag */
    public static final String ATTR_ENTRY = "entry";

    /** show the details attribute */
    public static final String ATTR_DETAILS = "details";


    /** linkresource attribute */
    public static final String ATTR_LINKRESOURCE = "linkresource";

    /** link attribute */
    public static final String ATTR_LINK = "link";

    /** attribute in the tabs tag */
    public static final String ATTR_USEDESCRIPTION = "usedescription";

    /** attribute in the tabs tag */
    public static final String ATTR_SHOWLINK = "showlink";


    /** include icon attribute */
    public static final String ATTR_INCLUDEICON = "includeicon";

    /** attribute in the tabs tag */
    public static final String ATTR_LINKLABEL = "linklabel";

    /** attribute in import tag */
    public static final String ATTR_ENTRIES = "entries";

    /** exclude attribute */
    public static final String ATTR_EXCLUDE = "exclude";

    /** the message attribute */
    public static final String ATTR_MESSAGE = "message";

    /** the associations attribute */
    public static final String ATTR_ASSOCIATIONS = "associations";

    /** attribute in import tag */
    public static final String ATTR_SHOWHIDE = "showhide";

    /** attribute in import tag */
    public static final String ATTR_OUTPUT = "output";

    /** attribute in import tag */
    public static final String ATTR_RANDOM = "random";

    /** attribute in import tag */
    public static final String ATTR_CHILDREN = "children";

    /** attribute in import tag */
    public static final String ATTR_FORMAT = "format";

    /** attribute in import tag */
    public static final String ATTR_SHOWTOGGLE = "showtoggle";

    /** attribute in import tag */
    public static final String ATTR_OPEN = "open";

    /** attribute in import tag */
    public static final String ATTR_FILES = "files";

    /** attribute in import tag */
    public static final String ATTR_FOLDERS = "folders";

    /** images only attribute */
    public static final String ATTR_IMAGES = "images";

    /** attribute in import tag */
    public static final String ATTR_TYPE = "type";

    /** attribute in import tag */
    public static final String ATTR_SEPARATOR = "separator";

    /** attribute in import tag */
    public static final String ATTR_CLASS = "class";

    /** attribute in import tag */
    public static final String ATTR_STYLE = "style";

    /** attribute in import tag */
    public static final String ATTR_TAGOPEN = "tagopen";

    /** attribute in import tag */
    public static final String ATTR_TAGCLOSE = "tagclose";

    /** attribute in import tag */
    public static final String ATTR_TITLE = "title";

    /** attribute in import tag */
    public static final String ATTR_LAYOUT = "layout";

    /** the columns attribute */
    public static final String ATTR_COLUMNS = "columns";

    /** the fieldname attribute */
    public static final String ATTR_FIELDNAME = "name";

    /** attribute in import tag */
    public static final String LAYOUT_HORIZONTAL = "hor";

    /** attribute in import tag */
    public static final String LAYOUT_VERTICAL = "vert";

    /** attribute in import tag */
    public static final String ATTR_MENUS = "menus";

    /** attribute in import tag */
    public static final String ATTR_REQUEST = "request";


    /** attribute in import tag */
    public static final String ATTR_POPUP = "popup";



    /** attribute in import tag */
    public static final String ATTR_LEVEL = "level";

    /** attribute in import tag */
    public static final String ATTR_COUNT = "count";

    /** attribute to wikify the content */
    public static final String ATTR_WIKIFY = "wikify";

    /** max image height attribute */
    public static final String ATTR_MAXIMAGEHEIGHT = "maximageheight";

    /** attribute in import tag */
    public static final String ATTR_DAYS = "days";


    /** wiki import */
    public static final String WIKI_PROP_IMPORT = "import";

    /** the field property */
    public static final String WIKI_PROP_FIELD = "field";

    /** the calendar property */
    public static final String WIKI_PROP_CALENDAR = "calendar";

    /** the timeline property */
    public static final String WIKI_PROP_TIMELINE = "timeline";

    /** wiki import */
    public static final String WIKI_PROP_DATE = "date";

    /** wiki import */
    public static final String WIKI_PROP_DATE_FROM = "fromdate";

    /** wiki import */
    public static final String WIKI_PROP_DATE_TO = "todate";

    /** wiki import */
    public static final String WIKI_PROP_MENU = "menu";

    /** wiki import */
    public static final String WIKI_PROP_TREE = "tree";

    /** the table property */
    public static final String WIKI_PROP_TABLE = "table";

    /** wiki import */
    public static final String WIKI_PROP_COMMENTS = "comments";

    /** wiki import */
    public static final String WIKI_PROP_RECENT = "recent";

    /** wiki import */
    public static final String WIKI_PROP_GALLERY = "gallery";

    /** the image player property */
    public static final String WIKI_PROP_PLAYER = "imageplayer";

    /** the old image player property */
    private static final String WIKI_PROP_PLAYER_OLD = "player";

    /** wiki import */
    public static final String WIKI_PROP_TABS = "tabs";

    /** accordian property */
    public static final String WIKI_PROP_ACCORDIAN = "accordian";

    /** the slideshow property */
    public static final String WIKI_PROP_SLIDESHOW = "slideshow";

    /** wiki import */
    public static final String WIKI_PROP_GRID = "grid";

    /** wiki import */
    public static final String WIKI_PROP_TOOLBAR = "toolbar";

    /** wiki import */
    public static final String WIKI_PROP_BREADCRUMBS = "breadcrumbs";

    /** wiki import */
    public static final String WIKI_PROP_INFORMATION = "information";

    /** wiki import */
    public static final String WIKI_PROP_IMAGE = "image";

    /** wiki import */
    public static final String WIKI_PROP_NAME = "name";

    /** wiki import */
    public static final String WIKI_PROP_MAP = "map";

    /** wiki import */
    public static final String WIKI_PROP_EARTH = "earth";

    /** wiki import */
    public static final String WIKI_PROP_HTML = "html";

    /** wiki import */
    public static final String WIKI_PROP_MAPENTRY = "mapentry";

    /** wiki import */
    public static final String WIKI_PROP_DESCRIPTION = "description";

    /** wiki import */
    public static final String WIKI_PROP_PROPERTIES = "properties";

    /** wiki import */
    public static final String WIKI_PROP_LINKS = "links";

    /** wiki import */
    public static final String WIKI_PROP_ENTRYID = "entryid";

    /** wiki import */
    public static final String WIKI_PROP_LAYOUT = "layout";

    /** wiki import */
    public static final String WIKI_PROP_CHILDREN_GROUPS = "subgroups";

    /** wiki import */
    public static final String WIKI_PROP_CHILDREN_ENTRIES = "subentries";

    /** wiki import */
    public static final String WIKI_PROP_CHILDREN = "children";

    /** wiki import */
    public static final String WIKI_PROP_URL = "url";


    /** list of import items for the text editor menu */
    public static final String[] WIKIPROPS = {
        WIKI_PROP_INFORMATION, WIKI_PROP_NAME, WIKI_PROP_DESCRIPTION,
        WIKI_PROP_DATE_FROM, WIKI_PROP_DATE_TO, WIKI_PROP_FIELD,
        WIKI_PROP_LAYOUT, WIKI_PROP_PROPERTIES, WIKI_PROP_HTML, WIKI_PROP_MAP,
        WIKI_PROP_MAPENTRY, WIKI_PROP_EARTH, WIKI_PROP_CALENDAR,
        WIKI_PROP_TIMELINE, WIKI_PROP_COMMENTS, WIKI_PROP_BREADCRUMBS,
        WIKI_PROP_TOOLBAR, WIKI_PROP_IMAGE, WIKI_PROP_MENU, WIKI_PROP_RECENT,
        WIKI_PROP_GALLERY, WIKI_PROP_SLIDESHOW, WIKI_PROP_TABS,
        WIKI_PROP_ACCORDIAN, WIKI_PROP_GRID, WIKI_PROP_TREE, WIKI_PROP_TABLE,
        WIKI_PROP_LINKS, WIKI_PROP_ENTRYID
    };

    /** the id for this */
    public static final String ID_THIS = "this";

    /** the id for my parent */
    public static final String ID_PARENT = "parent";

    /** the id for my grandparent */
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
     * Get a wiki property value
     *
     * @param wikiUtil The wiki util
     * @param property the property
     *
     * @return the value
     */
    public String getWikiPropertyValue(WikiUtil wikiUtil, String property) {

        try {
            Entry   entry   = (Entry) wikiUtil.getProperty(ATTR_ENTRY);
            Request request = (Request) wikiUtil.getProperty(ATTR_REQUEST);
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
            if (tag.equals(WIKI_PROP_IMPORT)) {
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
            String    entryId = (String) props.get(ATTR_ENTRY);

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
     * Get a wiki image link
     *
     *
     * @param wikiUtil The wiki util
     * @param request The request
     * @param url  the image url
     * @param entry  the entry
     * @param props  the properties
     *
     * @return the link
     *
     * @throws Exception problems
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

        if (alt == null) {
            String name = entry.getName();
            if ((name != null) && !name.isEmpty()) {
                alt = name;
            }
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

        String  img          = HtmlUtil.img(url, entry.getName(), extra);
        boolean link         = Misc.equals("true", props.get(ATTR_LINK));
        boolean linkResource = Misc.getProperty(props, ATTR_LINKRESOURCE,
                                   false);
        if (link) {
            return HtmlUtil.href(
                request.entryUrl(getRepository().URL_ENTRY_SHOW, entry), img);
        } else if (linkResource) {
            return HtmlUtil.href(
                entry.getTypeHandler().getEntryResourceUrl(request, entry),
                img);
        }

        return img;
    }


    /**
     * Get the wiki url
     *
     * @param wikiUtil The wiki util
     * @param request The request
     * @param entry  the entry
     * @param props  the properties
     *
     * @return  the url
     *
     * @throws Exception problems
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
     * Get the wiki image link
     *
     * @param wikiUtil The wiki util
     * @param request The request
     * @param entry  the entry
     * @param props  the properties
     *
     * @return  the link
     *
     * @throws Exception problems
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
     * Get the message
     *
     * @param props the properties
     * @param message the default
     *
     * @return  the message or the default
     */
    public String getMessage(Hashtable props, String message) {
        return Misc.getProperty(props, ATTR_MESSAGE, message);
    }



    /**
     * Get the text for the include
     *
     * @param wikiUtil The wiki util
     * @param request The request
     * @param entry the entry
     * @param include  the include
     * @param props    the properties
     *
     * @return  the include text
     *
     * @throws Exception  problems
     */
    public String getWikiInclude(WikiUtil wikiUtil, Request request,
                                 Entry entry, String include, Hashtable props)
            throws Exception {

        String hasChildren = (String) wikiUtil.getProperty(entry.getId()
                                 + "_haschildren");

        boolean hasOpenProperty = props.get(ATTR_OPEN) != null;

        boolean open            = Misc.getProperty(props, ATTR_OPEN,
                                        ((hasChildren != null)
                                         ? hasChildren.equals("false")
                                         : true));
        boolean      inBlock = Misc.getProperty(props, ATTR_SHOWTOGGLE, true);
        String       blockContent = null;
        String       blockTitle   = "";
        boolean      doBG         = true;

        boolean      wikify       = Misc.getProperty(props, ATTR_WIKIFY, true);

        StringBuffer sb           = new StringBuffer();
        if (include.equals(WIKI_PROP_INFORMATION)) {
            blockContent =
                getRepository().getHtmlOutputHandler().getInformationTabs(
                    request, entry, false, true);
            blockTitle = Misc.getProperty(props, ATTR_TITLE,
                                          msg("Information"));
        } else if (include.equals(WIKI_PROP_COMMENTS)) {
            return getHtmlOutputHandler().getCommentBlock(request, entry,
                    false).toString();
        } else if (include.equals(WIKI_PROP_TOOLBAR)) {
            return getEntryManager().getEntryToolbar(request, entry);
        } else if (include.equals(WIKI_PROP_BREADCRUMBS)) {
            return getEntryManager().getBreadCrumbs(request, entry);
        } else if (include.equals(WIKI_PROP_DESCRIPTION)) {
            String desc = entry.getDescription();
            desc = desc.replaceAll("\r\n\r\n", "\n<p>\n");
            if (wikify) {
                desc = new WikiUtil(false).wikify(desc, null);
            }

            return desc;
        } else if (include.equals(WIKI_PROP_LAYOUT)) {
            return getHtmlOutputHandler().makeHtmlHeader(request, entry,
                    Misc.getProperty(props, ATTR_TITLE, "Layout"));
        } else if (include.equals(WIKI_PROP_NAME)) {
            return entry.getName();
        } else if (include.equals(WIKI_PROP_FIELD)) {
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
        } else if (include.equals(WIKI_PROP_DATE_FROM)
                   || include.equals(WIKI_PROP_DATE_TO)) {
            String format =
                Misc.getProperty(props, ATTR_FORMAT,
                                 RepositoryBase.DEFAULT_TIME_FORMAT);
            Date date = new Date(include.equals(WIKI_PROP_DATE_FROM)
                                 ? entry.getStartDate()
                                 : entry.getEndDate());
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(RepositoryUtil.TIMEZONE_DEFAULT);

            return dateFormat.format(date);
        } else if (include.equals(WIKI_PROP_ENTRYID)) {
            return entry.getId();
        } else if (include.equals(WIKI_PROP_PROPERTIES)) {
            return makeEntryTabs(request, entry);
        } else if (include.equals(WIKI_PROP_IMAGE)) {
            return getWikiImage(wikiUtil, request, entry, props);
        } else if (include.equals(WIKI_PROP_URL)) {
            return getWikiUrl(wikiUtil, request, entry, props);
        } else if (include.equals(WIKI_PROP_HTML)) {
            if (Misc.getProperty(props, ATTR_CHILDREN, false)) {
                List<Entry> children = getEntries(request, wikiUtil, entry,
                                           props);
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

        } else if (include.equals(WIKI_PROP_CALENDAR)) {
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            boolean doDay = Misc.getProperty(props, "day", false);
            getCalendarOutputHandler().outputCalendar(request,
                    getCalendarOutputHandler().makeCalendarEntries(request,
                        children), sb, doDay);

            return sb.toString();
        } else if (include.equals(WIKI_PROP_TIMELINE)) {
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);

            int    height = Misc.getProperty(props, ATTR_HEIGHT, 150);
            String style  = "height: " + height + "px;";
            String head   = getHtmlOutputHandler().makeTimeline(request,
                              children, sb, style);
            if (head != null) {
                request.putExtraProperty(PROP_HTML_HEAD, head);
            }

            return sb.toString();
        } else if (include.equals(WIKI_PROP_MAP)
                   || include.equals(WIKI_PROP_EARTH)) {
            int     width       = Misc.getProperty(props, ATTR_WIDTH, 400);
            int     height      = Misc.getProperty(props, ATTR_HEIGHT, 300);
            boolean justPoints  = Misc.getProperty(props, "justpoints", false);
            boolean listEntries = Misc.getProperty(props, "listentries",
                                      false);
            boolean googleEarth =
                include.equals(WIKI_PROP_EARTH)
                && getMapManager().isGoogleEarthEnabled(request);

            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);

            if (googleEarth) {
                getMapManager().getGoogleEarth(request, children, sb,
                        Misc.getProperty(props, ATTR_WIDTH, -1),
                        Misc.getProperty(props, ATTR_HEIGHT, -1),
                        listEntries, justPoints);
            } else {
                MapOutputHandler mapOutputHandler =
                    (MapOutputHandler) getRepository().getOutputHandler(
                        MapOutputHandler.OUTPUT_MAP);
                if (mapOutputHandler == null) {
                    return "No maps";
                }
                boolean[] haveBearingLines = { false };
                Request   newRequest       = request.cloneMe();
                newRequest.putAll(props);
                MapInfo map = getMapManager().getMap(newRequest, children,
                                  sb, width, height, false, haveBearingLines,
                                  listEntries);
            }

            return sb.toString();
        } else if (include.equals(WIKI_PROP_MAPENTRY)) {
            int              width = Misc.getProperty(props, ATTR_WIDTH, 400);
            int height = Misc.getProperty(props, ATTR_HEIGHT, 300);
            MapOutputHandler mapOutputHandler =
                (MapOutputHandler) getRepository().getOutputHandler(
                    MapOutputHandler.OUTPUT_MAP);
            if (mapOutputHandler == null) {
                return "No maps";
            }

            List<Entry> children = new ArrayList<Entry>();
            boolean     details = Misc.getProperty(props, ATTR_DETAILS, false);
            children.add(entry);
            boolean[] haveBearingLines = { false };
            MapInfo   map = getMapManager().getMap(request, children, sb,
                              width, height, details, haveBearingLines);

            return sb.toString();
        } else if (include.equals(WIKI_PROP_MENU)) {
            boolean popup = Misc.getProperty(props, ATTR_POPUP, false);
            String  menus = Misc.getProperty(props, ATTR_MENUS, "");
            int     type  = OutputType.getTypeMask(StringUtil.split(menus, ",",
                           true, true));
            blockTitle = Misc.getProperty(props, ATTR_TITLE,
                                          msg(LABEL_LINKS));
            blockContent = getEntryManager().getEntryActionsTable(request,
                    entry, type);
            if (popup) {
                return getRepository().makePopupLink(blockTitle,
                        blockContent);
            }
        } else if (include.equals(WIKI_PROP_TABS)
                   || include.equals(WIKI_PROP_ACCORDIAN)
                   || include.equals(WIKI_PROP_SLIDESHOW)) {
            boolean      doingSlideshow = include.equals(WIKI_PROP_SLIDESHOW);
            List<String> titles         = new ArrayList<String>();
            List<String> contents       = new ArrayList<String>();
            List<Entry>  children       = getEntries(request, wikiUtil, entry,
                                       props);
            boolean useDescription = Misc.getProperty(props,
                                         ATTR_USEDESCRIPTION, true);
            boolean showLink    = Misc.getProperty(props, ATTR_SHOWLINK, true);
            boolean includeIcon = Misc.getProperty(props, ATTR_INCLUDEICON,
                                      false);
            String linklabel   = Misc.getProperty(props, ATTR_LINKLABEL, "");
            int    width       = Misc.getProperty(props, ATTR_WIDTH, 400);
            int    height      = Misc.getProperty(props, ATTR_HEIGHT, 270);
            int imageWidth = Misc.getProperty(props, ATTR_IMAGEWIDTH, width);
            int maxImageHeight = Misc.getProperty(props, ATTR_MAXIMAGEHEIGHT,
                                     height - 40);
            boolean linkResource = Misc.getProperty(props, ATTR_LINKRESOURCE,
                                       false);

            for (Entry child : children) {
                String title = child.getName();
                if (includeIcon) {
                    title =
                        HtmlUtil.img(getEntryManager().getIconUrl(request,
                            child)) + " " + title;
                }
                titles.add(title);
                String content;
                if ( !useDescription) {
                    Result result =
                        getHtmlOutputHandler().getHtmlResult(request,
                            OutputHandler.OUTPUT_HTML, child);
                    content = new String(result.getContent());
                } else {
                    if (child.getTypeHandler().isType(TYPE_WIKIPAGE)) {
                        String wikitext = child.getValue(0,
                                              child.getDescription());
                        content = wikifyEntry(request, entry, wikitext,
                                false, null, null);
                    } else {
                        content = child.getDescription();
                        if (wikify) {
                            content = new WikiUtil(false).wikify(content,
                                    null);
                        }
                    }
                    if (child.getResource().isImage()) {
                        if (doingSlideshow) {
                            props.put("imageclass", "slides_image");
                        }
                        Request newRequest = request.cloneMe();
                        newRequest.putAll(props);
                        content = getMapManager().makeInfoBubble(newRequest,
                                child);
                    }
                }

                if (showLink) {
                    String url;
                    if (linkResource
                            && (child.isFile()
                                || child.getResource().isUrl())) {
                        url = child.getTypeHandler().getEntryResourceUrl(
                            request, child);
                    } else {
                        url = request.entryUrl(
                            getRepository().URL_ENTRY_SHOW, child);
                    }
                    String href = HtmlUtil.href(url, linklabel.isEmpty()
                            ? child.getName()
                            : linklabel);

                    content = content + HtmlUtil.br()
                              + HtmlUtil.leftRight("", href);
                }
                contents.add(content);

            }


            if (include.equals(WIKI_PROP_ACCORDIAN)) {
                String accordianId = "accordion_" + (idCounter++);
                sb.append(
                    HtmlUtil.open(
                        HtmlUtil.TAG_DIV,
                        HtmlUtil.cssClass(
                            "ui-accordion ui-widget ui-helper-reset") + HtmlUtil.id(
                            accordianId)));
                for (int i = 0; i < titles.size(); i++) {
                    String title   = titles.get(i);
                    String content = contents.get(i);
                    sb.append(
                        HtmlUtil.open(
                            HtmlUtil.TAG_H3,
                            HtmlUtil.cssClass(
                                "ui-accordion-header ui-helper-reset ui-state-active ui-corner-top")));
                    sb.append("<a href=\"#\">");
                    sb.append(title);
                    sb.append("</a></h3>");
                    sb.append(HtmlUtil.div(content, ""));
                }
                sb.append("</div>");
                String args =
                    "autoHeight: false, navigation: true, collapsible: true";
                sb.append(HtmlUtil.script("$(function() {\n$(\"#"
                                          + accordianId + "\" ).accordion({"
                                          + args + "});});\n"));

                return sb.toString();
            } else if (doingSlideshow) {
                // for slideshow
                boolean shownav = Misc.getProperty(props, "shownav", false);
                boolean autoplay = Misc.getProperty(props, "autoplay", false);
                int     playSpeed   = Misc.getProperty(props, "speed", 5);

                String  arrowWidth  = "24";
                String  arrowHeight = "43";
                String  slideId     = "slides_" + (idCounter++);

                sb.append(HtmlUtil.open("style",
                                        HtmlUtil.attr("type", "text/css")));
                // need to set the height of the div to include the nav bar
                sb.append("#" + slideId + " { width: " + width
                          + "px; height: " + (height + 30) + "}\n");
                sb.append(".slides_image {max-height: " + maxImageHeight
                          + "px; overflow-x: none; overflow-y: auto;}\n");

                int    border      = Misc.getProperty(props, "border", 1);
                String borderColor = Misc.getProperty(props, "bordercolor",
                                         "#aaa");
                sb.append(
                    "#" + slideId + " .slides_container {border: " + border
                    + "px solid " + borderColor + "; width:" + width
                    + "px;overflow:hidden;position:relative;display:none;}\n.slides_container div.slide {width:"
                    + width + "px;height:" + height + "px;display:block;}\n");
                sb.append("</style>\n\n");
                sb.append(
                    "<link rel=\"stylesheet\" href=\"/slides/paginate.css\">\n");

                // user speed is seconds, script uses milliseconds - 0 == no play
                int    startSpeed  = (autoplay)
                                     ? playSpeed * 1000
                                     : 0;
                String slideParams =
                    "preload: false, preloadImage: "
                    + HtmlUtil
                        .squote(getRepository()
                            .fileUrl("/slides/img/loading.gif")) + ", play: "
                                + startSpeed
                                + ", pause: 2500, hoverPause: true"
                                + ", generatePagination: " + shownav + "\n";
                StringBuffer js = new StringBuffer();

                js.append(
                    "$(function(){\n$(" + HtmlUtil.squote("#" + slideId)
                    + ").slides({" + slideParams
                    + ",\nslidesLoaded: function() {$('.caption').animate({ bottom:0 },200); }\n});\n});\n");

                sb.append(HtmlUtil.open(HtmlUtil.TAG_DIV,
                                        HtmlUtil.id(slideId)));



                String prevImage =
                    HtmlUtil.href(
                        "#",
                        HtmlUtil.img(
                            getRepository().fileUrl(
                                "/slides/img/arrow-prev.png"), "Prev",
                                    " width=18 "), HtmlUtil.cssClass("next"));

                String nextImage =
                    HtmlUtil.href(
                        "#",
                        HtmlUtil.img(
                            getRepository().fileUrl(
                                "/slides/img/arrow-next.png"), "Next",
                                    " width=18 "), HtmlUtil.cssClass("next"));


                sb.append(
                    "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>\n");
                sb.append(HtmlUtil.col(prevImage, "width=1"));
                sb.append(HtmlUtil.open(HtmlUtil.TAG_TD,
                                        HtmlUtil.attr(HtmlUtil.ATTR_WIDTH,
                                            "" + width)));
                sb.append(
                    HtmlUtil.open(
                        HtmlUtil.TAG_DIV,
                        HtmlUtil.cssClass("slides_container")));
                for (int i = 0; i < titles.size(); i++) {
                    String title   = titles.get(i);
                    String content = contents.get(i);
                    sb.append("\n");
                    sb.append(HtmlUtil.open(HtmlUtil.TAG_DIV,
                                            HtmlUtil.cssClass("slide")));
                    sb.append(content);
                    //                    sb.append(HtmlUtil.br());
                    //                    sb.append(title);
                    sb.append(HtmlUtil.close(HtmlUtil.TAG_DIV));  // slide
                }
                sb.append(HtmlUtil.close(HtmlUtil.TAG_DIV));  // slides_container
                sb.append(HtmlUtil.close(HtmlUtil.TAG_TD));
                sb.append(HtmlUtil.col(nextImage, "width=1"));
                sb.append("</tr></table>");
                sb.append(HtmlUtil.close(HtmlUtil.TAG_DIV));  // slideId

                sb.append(
                    HtmlUtil.importJS(
                        getRepository().fileUrl(
                            "/slides/slides.min.jquery.js")));

                sb.append(HtmlUtil.script(js.toString()));

                return sb.toString();
            } else {
                return OutputHandler.makeTabs(titles, contents, true);
            }
        } else if (include.equals(WIKI_PROP_GRID)) {
            getHtmlOutputHandler().makeGrid(request,
                                            getEntries(request, wikiUtil,
                                                entry, props), sb);

            return sb.toString();
        } else if (include.equals(WIKI_PROP_TABLE)) {
            getHtmlOutputHandler().makeTable(request,
                                             getEntries(request, wikiUtil,
                                                 entry, props), sb);

            return sb.toString();
        } else if (include.equals(WIKI_PROP_RECENT)) {
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            int numDays = Misc.getProperty(props, ATTR_DAYS, 3);
            BufferMapList<Date> map        = new BufferMapList<Date>();
            SimpleDateFormat    dateFormat =
                new SimpleDateFormat("EEEEE MMMMM d");
            dateFormat.setTimeZone(RepositoryUtil.TIMEZONE_DEFAULT);
            Date              firstDay = ((children.size() > 0)
                                          ? new Date(children.get(0).getChangeDate())
                                          : new Date());
            GregorianCalendar cal1     =
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

        } else if (include.equals(WIKI_PROP_PLAYER)
                   || include.equals(WIKI_PROP_PLAYER_OLD)) {
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props, true);
            if (children.size() == 0) {
                return getMessage(props, "");
            }
            ImageOutputHandler imageOutputHandler =
                (ImageOutputHandler) getRepository().getOutputHandler(
                    ImageOutputHandler.OUTPUT_PLAYER);
            imageOutputHandler.makePlayer(request, children, sb, false);

            return sb.toString();
        } else if (include.equals(WIKI_PROP_GALLERY)) {
            int         width    = Misc.getProperty(props, ATTR_WIDTH, -1);
            int         columns  = Misc.getProperty(props, ATTR_COLUMNS, 1);
            boolean     random   = Misc.getProperty(props, ATTR_RANDOM, false);
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props, true);
            int size = children.size();
            if (random && (size > 1)) {
                int randomIdx = (int) (Math.random() * size);
                if (randomIdx >= size) {
                    randomIdx = size;
                }
                Entry randomEntry = children.get(randomIdx);
                children = new ArrayList<Entry>();
                children.add(randomEntry);
            }


            int num    = 0;
            int colCnt = 0;
            sb.append("<table cellspacing=4>");
            sb.append("<tr valign=\"bottom\">");
            for (Entry child : children) {
                num++;
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
        } else if (include.equals(WIKI_PROP_CHILDREN_GROUPS)) {
            if ( !hasOpenProperty) {
                open = true;
            }
            doBG = false;
            props.put(ATTR_FOLDERS, "true");
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            if (children.size() == 0) {
                return getMessage(props, "");
            }
            String link = getHtmlOutputHandler().getEntriesList(request, sb,
                              children, true, true, true, false);
            blockContent = sb.toString();
            blockTitle = Misc.getProperty(props, ATTR_TITLE, msg("Folders"))
                         + link;
        } else if (include.equals(WIKI_PROP_CHILDREN_ENTRIES)) {
            if ( !hasOpenProperty) {
                open = true;
            }
            doBG = false;
            props.put(ATTR_FILES, "true");
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            if (children.size() == 0) {
                return getMessage(props, "");
            }

            String link = getHtmlOutputHandler().getEntriesList(request, sb,
                              children, true, true, true, false);
            blockContent = sb.toString();
            blockTitle = Misc.getProperty(props, ATTR_TITLE, msg("Folders"))
                         + link;
        } else if (include.equals(WIKI_PROP_CHILDREN)
                   || include.equals(WIKI_PROP_TREE)) {
            if ( !hasOpenProperty) {
                open = true;
            }
            doBG = false;
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            if (children.size() == 0) {
                return getMessage(props, "");
            }
            String link = getHtmlOutputHandler().getEntriesList(request, sb,
                              children, true, true, true, false);
            blockContent = sb.toString();
            blockTitle   = Misc.getProperty(props, ATTR_TITLE, msg("Links"))
                         + link;
        } else if (include.equals(WIKI_PROP_LINKS)) {
            List<Entry> children = getEntries(request, wikiUtil, entry,
                                       props);
            if (children.size() == 0) {
                return getMessage(props, "");
            }

            boolean linkResource = Misc.getProperty(props, ATTR_LINKRESOURCE,
                                       false);
            String separator = Misc.getProperty(props, ATTR_SEPARATOR,
                                   "&nbsp;|&nbsp;");
            String       cssClass = Misc.getProperty(props, ATTR_CLASS, "");
            String       style = Misc.getProperty(props, ATTR_STYLE, "style");
            String       tagOpen  = Misc.getProperty(props, ATTR_TAGOPEN, "");
            String       tagClose = Misc.getProperty(props, ATTR_TAGCLOSE, "");

            List<String> links    = new ArrayList<String>();
            for (Entry child : children) {
                String url;
                if (linkResource
                        && (child.getTypeHandler().isType("link")
                            || child.isFile()
                            || child.getResource().isUrl())) {
                    url = child.getTypeHandler().getEntryResourceUrl(request,
                            child);
                } else {
                    url = request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                           child);
                }

                String href = HtmlUtil.href(url, child.getName(),
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

        if (doBG) {
            return HtmlUtil.makeShowHideBlock(blockTitle, blockContent, open,
                    HtmlUtil.cssClass("toggleblocklabel"), "");
            //            HtmlUtil.cssClass("wiki-tocheader"),  HtmlUtil.cssClass("wiki-toc"));
        } else {
            return HtmlUtil.makeShowHideBlock(blockTitle, blockContent, open);
        }

    }

    /**
     * Make entry tabs html
     *
     * @param request The request
     * @param entry  the entry
     *
     * @return the entry tabs html
     *
     * @throws Exception  problems
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
     * Get the entries for the request
     *
     * @param request The request
     * @param wikiUtil The wiki util
     * @param entry  the parent entry
     * @param props  properties
     *
     * @return the list of entries
     *
     * @throws Exception problems
     */
    public List<Entry> getEntries(Request request, WikiUtil wikiUtil,
                                  Entry entry, Hashtable props)
            throws Exception {

        return getEntries(request, wikiUtil, entry, props, false);
    }

    /**
     * Get the entries that are images
     *
     * @param entries  the list of entries
     *
     * @return  the list of entries that are images
     */
    public List<Entry> getImageEntries(List<Entry> entries) {
        List<Entry> imageEntries = new ArrayList<Entry>();
        for (Entry entry : entries) {
            if (entry.getResource().isImage()) {
                imageEntries.add(entry);
            }
        }

        return imageEntries;
    }


    /**
     * Get the entries for the request
     *
     * @param request  the request
     * @param wikiUtil the WikiUtil
     * @param entry    the entry
     * @param props    properties
     * @param onlyImages  true to only show images
     *
     * @return the list of Entry's
     *
     * @throws Exception  problems making list
     */
    public List<Entry> getEntries(Request request, WikiUtil wikiUtil,
                                  Entry entry, Hashtable props,
                                  boolean onlyImages)
            throws Exception {

        if ( !onlyImages) {
            onlyImages = Misc.getProperty(props, ATTR_IMAGES, onlyImages);
        }

        boolean folders        = Misc.getProperty(props, ATTR_FOLDERS, false);
        boolean files          = Misc.getProperty(props, ATTR_FILES, false);
        boolean doAssociations = Misc.getProperty(props, ATTR_ASSOCIATIONS,
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
            if (onlyImages) {
                return getImageEntries(linkedEntries);
            }

            return linkedEntries;
        }

        String      type     = (String) props.get(ATTR_TYPE);
        int         level    = Misc.getProperty(props, ATTR_LEVEL, 1);
        List<Entry> children =
            (List<Entry>) wikiUtil.getProperty(entry.getId() + "_children");
        if (children == null) {
            children = getEntryManager().getChildren(request, entry);
        }


        String userDefinedEntries = Misc.getProperty(props, ATTR_ENTRIES,
                                        (String) null);
        if (userDefinedEntries != null) {
            children = getEntries(request, userDefinedEntries);
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
                if (child.getTypeHandler().isType(type)) {
                    tmp.add(child);
                }
            }
            children = tmp;
        }

        if (onlyImages) {
            children = getImageEntries(children);
        }

        String excludeEntries = Misc.getProperty(props, ATTR_EXCLUDE,
                                    (String) null);

        if (excludeEntries != null) {
            HashSet seen = new HashSet();
            for (String id : StringUtil.split(excludeEntries, ",")) {
                seen.add(id);
            }
            List<Entry> okEntries = new ArrayList<Entry>();
            for (Entry e : children) {
                if ( !seen.contains(e.getId())) {
                    okEntries.add(e);
                }
            }
            children = okEntries;
        }



        int count = Misc.getProperty(props, ATTR_COUNT, -1);
        if (count > 0) {
            List<Entry> tmp = new ArrayList<Entry>();
            for (Entry child : children) {
                tmp.add(child);
                if (tmp.size() >= count) {
                    break;
                }
            }
            children = tmp;
        }


        return children;

    }


    /**
     * Get the entries corresponding to the ids
     *
     * @param request the Request
     * @param ids  list of comma separated ids
     *
     * @return List of Entrys
     *
     * @throws Exception problem getting entries
     */
    private List<Entry> getEntries(Request request, String ids)
            throws Exception {
        List<Entry> entries = new ArrayList<Entry>();
        for (String entryid : StringUtil.split(ids, ",", true, true)) {
            Entry entry = getEntryManager().getEntry(request, entryid);
            if (entry != null) {
                entries.add(entry);
            }
        }

        return entries;
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
     * Get the calendar output handler
     *
     * @return the calendar output handler
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
     * Handle the wiki import
     *
     * @param wikiUtil The wiki util
     * @param request The request
     * @param importEntry  the import entry
     * @param tag   the tag
     * @param props properties
     *
     * @return the include output
     */
    public String handleWikiImport(WikiUtil wikiUtil, final Request request,
                                   Entry importEntry, String tag,
                                   Hashtable props) {
        try {
            if ( !tag.equals(WIKI_PROP_IMPORT)) {
                String include = getWikiInclude(wikiUtil, request,
                                     importEntry, tag, props);
                if (include != null) {
                    return include;
                }
            } else {
                tag = Misc.getProperty(props, ATTR_OUTPUT,
                                       OutputHandler.OUTPUT_HTML.getId());
            }

            OutputHandler handler = getRepository().getOutputHandler(tag);
            if (handler == null) {
                return null;
            }
            OutputType outputType     = handler.findOutputType(tag);

            String     originalOutput = request.getString(ARG_OUTPUT,
                                        (String) "");
            String  originalId = request.getString(ARG_ENTRYID, (String) "");

            Request myRequest  =
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
                title         = Misc.getProperty(props, ATTR_TITLE, title);
            }

            boolean inBlock = Misc.getProperty(props, ATTR_SHOWHIDE, true);
            boolean open    = Misc.getProperty(props, ATTR_OPEN, true);

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
     * Find the wiki entry for the request
     *
     * @param request The request
     * @param wikiUtil The wiki util
     * @param name     the name
     * @param parent   the parent
     *
     * @return the Entry
     *
     * @throws Exception problem retreiving Entry
     */
    public Entry findWikiEntry(Request request, WikiUtil wikiUtil,
                               String name, Entry parent)
            throws Exception {
        name = name.trim();
        Entry theEntry = null;
        if ((parent != null  /* top group */
                ) && parent.isGroup()) {
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
     * Make the wiki edit bar
     *
     * @param request The request
     * @param entry   the Entry
     * @param textAreaId  the textAreaId
     *
     * @return  the edit bar
     *
     * @throws Exception problems
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
            String js   = "javascript:insertTags("
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
     * Add a wiki edit button
     *
     *
     * @param textAreaId  the TextArea
     * @param icon        the icon
     * @param label       the label
     * @param prefix      the prefix
     * @param suffix      the suffix
     * @param example     example string
     * @param huh         huh?
     *
     * @return  the html for the button
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
     * Get a wiki link
     *
     * @param wikiUtil The wiki util
     * @param name     the name
     * @param label    the label
     *
     * @return  the wiki link
     */
    public String getWikiLink(WikiUtil wikiUtil, String name, String label) {
        try {
            Entry   entry   = (Entry) wikiUtil.getProperty(ATTR_ENTRY);
            Request request = (Request) wikiUtil.getProperty(ATTR_REQUEST);
            Entry   parent  = entry.getParentEntry();


            name = name.trim();
            if (name.startsWith("Category:")) {
                String category = name.substring("Category:".length());
                String url      =
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
     * Wikify the entry
     *
     * @param request The request
     * @param entry   the Entry
     * @param wikiContent  the content
     *
     * @return wikified content
     *
     * @throws Exception  problem wikifying
     */
    public String wikifyEntry(Request request, Entry entry,
                              String wikiContent)
            throws Exception {
        return wikifyEntry(request, entry, wikiContent, true, null, null);
    }


    /**
     * Wikify the entry
     *
     * @param request The request
     * @param entry   the Entry
     * @param wikiContent  the content to wikify
     * @param wrapInDiv    true to wrap in a div tag
     * @param subGroups    the list of subgroups to include
     * @param subEntries   the list of subentries to include
     *
     * @return the wikified Entry
     *
     * @throws Exception  problem wikifying
     */
    public String wikifyEntry(Request request, Entry entry,
                              String wikiContent, boolean wrapInDiv,
                              List<Entry> subGroups, List<Entry> subEntries)
            throws Exception {
        WikiUtil wikiUtil = new WikiUtil(Misc.newHashtable(new Object[] {
                                ATTR_REQUEST,
                                request, ATTR_ENTRY, entry }));

        return wikifyEntry(request, entry, wikiUtil, wikiContent, wrapInDiv,
                           subGroups, subEntries);
    }


    /**
     * Wikify the entry
     *
     * @param request The request
     * @param entry   the Entry
     * @param wikiUtil The wiki util
     * @param wikiContent  the content to wikify
     * @param wrapInDiv    true to wrap in a div tag
     * @param subGroups    the list of subgroups to include
     * @param subEntries   the list of subentries to include
     *
     * @return the wikified Entry
     *
     * @throws Exception  problem wikifying
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
     * Add a wiki link
     *
     * @param wikiUtil The wiki util
     * @param toEntry  the entry to add to
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
