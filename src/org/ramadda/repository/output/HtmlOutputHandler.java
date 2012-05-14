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
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;


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
public class HtmlOutputHandler extends OutputHandler {


    /** _more_ */
    public static final OutputType OUTPUT_TIMELINE =
        new OutputType("Timeline", "default.timeline",
    //OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, 
    OutputType.TYPE_FORSEARCH, "", ICON_TIMELINE);

    /** _more_ */
    public static final OutputType OUTPUT_GRID =
        new OutputType("Grid Layout", "html.grid",
    //                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
    OutputType.TYPE_FORSEARCH, "", ICON_DATA);


    /** _more_ */
    public static final OutputType OUTPUT_GRAPH =
        new OutputType("Graph", "default.graph",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_GRAPH);

    /** _more_ */
    public static final OutputType OUTPUT_TABLE =
        new OutputType("Tablular Layout", "html.table",
    //                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
    OutputType.TYPE_FORSEARCH, "", ICON_TABLE);

    /** _more_ */
    public static final OutputType OUTPUT_CLOUD = new OutputType("Cloud",
                                                      "default.cloud",
                                                      OutputType.TYPE_VIEW);

    /** _more_ */
    public static final OutputType OUTPUT_INLINE =
        new OutputType("inline", OutputType.TYPE_INTERNAL);

    /** _more_ */
    public static final OutputType OUTPUT_MAPINFO =
        new OutputType("mapinfo", OutputType.TYPE_INTERNAL);

    /** _more_ */
    public static final OutputType OUTPUT_SELECTXML =
        new OutputType("selectxml", OutputType.TYPE_INTERNAL);


    /** _more_ */
    public static final OutputType OUTPUT_METADATAXML =
        new OutputType("metadataxml", OutputType.TYPE_INTERNAL);

    /** _more_ */
    public static final OutputType OUTPUT_LINKSXML =
        new OutputType("linksxml", OutputType.TYPE_INTERNAL);


    /** _more_ */
    public static final String TAG_DATA = "data";

    /** _more_ */
    public static final String TAG_EVENT = "event";

    /** _more_ */
    public static final String ATTR_WIKI_SECTION = "wiki-section";

    /** _more_ */
    public static final String ATTR_WIKI_URL = "wiki-url";

    /** _more_ */
    public static final String ATTR_IMAGE = "image";

    /** _more_ */
    public static final String ATTR_LINK = "link";

    /** _more_ */
    public static final String ATTR_START = "start";

    /** _more_ */
    public static final String ATTR_TITLE = "title";

    /** _more_ */
    public static final String ATTR_END = "end";

    /** _more_ */
    public static final String ATTR_EARLIESTEND = "earliestEnd";

    /** _more_ */
    public static final String ATTR_ISDURATION = "isDuration";

    /** _more_ */
    public static final String ATTR_LATESTSTART = "latestStart";

    /** _more_ */
    public static final String ATTR_ICON = "icon";

    /** _more_ */
    public static final String ATTR_COLOR = "color";




    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public HtmlOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_HTML);
        addType(OUTPUT_TREE);
        addType(OUTPUT_TIMELINE);
        addType(OUTPUT_TABLE);
        addType(OUTPUT_GRID);
        addType(OUTPUT_GRAPH);
        addType(OUTPUT_INLINE);
        addType(OUTPUT_MAPINFO);
        addType(OUTPUT_SELECTXML);
        addType(OUTPUT_METADATAXML);
        addType(OUTPUT_LINKSXML);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean allowSpiders() {
        return true;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String getHtmlHeader(Request request, Entry entry) {
        if (entry.isDummy() || !entry.isGroup()) {
            return "";
        }
        return makeHtmlHeader(request, entry, "Change layout");
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param title _more_
     *
     * @return _more_
     */
    public String makeHtmlHeader(Request request, Entry entry, String title) {
        OutputType[] types = new OutputType[] { OUTPUT_TREE, OUTPUT_TABLE, OUTPUT_GRID,
                OUTPUT_TIMELINE, CalendarOutputHandler.OUTPUT_CALENDAR };
        StringBuffer sb =
            new StringBuffer("<table cellspacing=0 cellpadding=0><tr>");
        String selected = request.getString(ARG_OUTPUT, OUTPUT_TREE.getId());
        sb.append("<td align=center>" + msgLabel(title) + "</td>");
        for (OutputType output : types) {
            String link = HtmlUtil.href(
                              request.entryUrl(
                                  getRepository().URL_ENTRY_SHOW, entry,
                                  ARG_OUTPUT, output), HtmlUtil.img(
                                      iconUrl(output.getIcon()),
                                      output.getLabel()));
            sb.append("<td align=center>");
            if (output.getId().equals(selected)) {
                sb.append(
                    HtmlUtil.div(
                        link, HtmlUtil.cssClass("toolbar-selected")));
            } else {
                sb.append(HtmlUtil.div(link, HtmlUtil.cssClass("toolbar")));
            }
            sb.append(" ");
            sb.append("</td>");
        }
        sb.append("</table>");
        return "<table cellspacing=0 cellpadding=0 width=100%><tr><td align=right>"
               + sb.toString() + "</td></tr></table>";
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        List<Entry> entries = state.getAllEntries();
        if (state.getEntry() != null) {
            links.add(makeLink(request, state.getEntry(), OUTPUT_HTML));
            if (entries.size() > 1) {
                links.add(makeLink(request, state.getEntry(),
                                   OUTPUT_TIMELINE));
                links.add(makeLink(request, state.getEntry(), OUTPUT_TABLE));
                links.add(makeLink(request, state.getEntry(), OUTPUT_GRID));


            }
        }
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
    public Result getMapInfo(Request request, Entry entry) throws Exception {
        String html = null;
        Result typeResult = entry.getTypeHandler().getHtmlDisplay(request,
                                entry);
        if (typeResult != null) {
            byte[] content = typeResult.getContent();
            if (content != null) {
                html = new String(content);
            }
        }

        if (html == null) {
            String wikiTemplate = getWikiText(request, entry);
            if (wikiTemplate != null) {
                String wiki = getWikiManager().wikifyEntry(request, entry,
                                  wikiTemplate);
                html = getRepository().translate(request, wiki);
            } else {
                html = getMapManager().makeInfoBubble(request, entry);
            }
        }
        html = getRepository().translate(request, html);

        StringBuffer xml = new StringBuffer(XmlUtil.XML_HEADER);
        xml.append("\n<content>\n");
        XmlUtil.appendCdata(xml, html);
        xml.append("\n</content>");
        return new Result("", xml, "text/xml");
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
    public Result getMetadataXml(Request request, Entry entry)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        request.put(ARG_OUTPUT, OUTPUT_HTML);
        boolean didOne = false;
        sb.append(HtmlUtil.open(HtmlUtil.TAG_TABLE));
        sb.append(entry.getTypeHandler().getInnerEntryContent(entry, request,
                OutputHandler.OUTPUT_HTML, true, true, true));
        for (TwoFacedObject tfo :
                getMetadataHtml(request, entry, false, false)) {
            sb.append(tfo.getId().toString());
        }
        sb.append(HtmlUtil.close(HtmlUtil.TAG_TABLE));

        String links = getEntryManager().getEntryActionsTable(request, entry,
                           OutputType.TYPE_ALL);
        String contents =
            OutputHandler.makeTabs(Misc.newList(msg("Information"),
                msg(LABEL_LINKS)), Misc.newList(sb.toString(), links), true);

        contents = getInformationTabs(request, entry, true, true);
        //        String       contents = sb.toString();

        StringBuffer xml = new StringBuffer("<content>\n");
        XmlUtil.appendCdata(xml,
                            getRepository().translate(request, contents));
        xml.append("\n</content>");
        //        System.err.println(xml);
        return new Result("", xml, "text/xml");

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
    public Result getLinksXml(Request request, Entry entry) throws Exception {
        StringBuffer sb = new StringBuffer("<content>\n");
        String links = getEntryManager().getEntryActionsTable(request, entry,
                           OutputType.TYPE_ALL);
        StringBuffer inner = new StringBuffer();
        String cLink =
            HtmlUtil.jsLink(HtmlUtil.onMouseClick("hidePopupObject();"),
                            HtmlUtil.img(iconUrl(ICON_CLOSE)), "");
        inner.append(cLink);
        inner.append(HtmlUtil.br());
        inner.append(links);
        XmlUtil.appendCdata(sb, inner.toString());
        sb.append("\n</content>");
        return new Result("", sb, "text/xml");
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
        TypeHandler typeHandler =
            getRepository().getTypeHandler(entry.getType());
        if (outputType.equals(OUTPUT_METADATAXML)) {
            return getMetadataXml(request, entry);
        }
        if (outputType.equals(OUTPUT_LINKSXML)) {
            return getLinksXml(request, entry);
        }
        if (outputType.equals(OUTPUT_MAPINFO)) {
            return getMapInfo(request, entry);
        }
        if (outputType.equals(OUTPUT_INLINE)) {
            String inline = typeHandler.getInlineHtml(request, entry);
            if (inline != null) {
                inline = getRepository().translate(request, inline);
                StringBuffer xml = new StringBuffer("<content>\n");
                XmlUtil.appendCdata(xml,
                                    "<div class=inline>" + inline + "</div>");
                xml.append("\n</content>");
                return new Result("", xml, "text/xml");
            }
            String wikiTemplate = getWikiText(request, entry);
            if (wikiTemplate != null) {
                String wiki = getWikiManager().wikifyEntry(request, entry,
                                  wikiTemplate);
                wiki = getRepository().translate(request, wiki);
                StringBuffer xml = new StringBuffer("<content>\n");
                XmlUtil.appendCdata(xml,
                                    "<div class=inline>" + wiki + "</div>");
                xml.append("\n</content>");
                return new Result("", xml, "text/xml");
            }
            return getMetadataXml(request, entry);
        }

        return getHtmlResult(request, outputType, entry);
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
    public Result getHtmlResult(Request request, OutputType outputType,
                                Entry entry)
            throws Exception {
        TypeHandler typeHandler =
            getRepository().getTypeHandler(entry.getType());


        Result typeResult = typeHandler.getHtmlDisplay(request, entry);
        if (typeResult != null) {
            return typeResult;
        }

        StringBuffer sb           = new StringBuffer();

        String       wikiTemplate = getWikiText(request, entry);
        if (wikiTemplate != null) {
            sb.append(getWikiManager().wikifyEntry(request, entry,
                    wikiTemplate));
        } else {
            addDescription(request, entry, sb, true);
            String informationBlock = getInformationTabs(request, entry,
                                          false, false);
            sb.append(HtmlUtil.makeShowHideBlock(msg("Information"),
                    informationBlock, true));

            //            sb.append(getAttachmentsHtml(request, entry));
        }

        return makeLinksResult(request, msg("Entry"), sb, new State(entry));
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
    public String getAttachmentsHtml(Request request, Entry entry)
            throws Exception {
        StringBuffer metadataSB = new StringBuffer();
        getMetadataManager().decorateEntry(request, entry, metadataSB, false);
        String metataDataHtml = metadataSB.toString();
        if (metataDataHtml.length() > 0) {
            return HtmlUtil.makeShowHideBlock(msg("Attachments"),
                    "<div class=\"description\">" + metadataSB + "</div>",
                    false);
        }
        return "";
    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String getEntryLink(Request request, Entry entry) {
        return getEntryManager().getEntryLink(request, entry);
    }







    /**
     * _more_
     *
     * @param output _more_
     *
     * @return _more_
     */
    public String getMimeType(OutputType output) {
        if (output.equals(OUTPUT_TIMELINE)) {
            return getRepository().getMimeTypeFromSuffix(".html");
        }
        if (output.equals(OUTPUT_GRID)||output.equals(OUTPUT_TABLE)) {
            return getRepository().getMimeTypeFromSuffix(".html");
        } else if (output.equals(OUTPUT_GRAPH)) {
            return getRepository().getMimeTypeFromSuffix(".xml");
        } else if (output.equals(OUTPUT_HTML) || output.equals(OUTPUT_TREE)) {
            return getRepository().getMimeTypeFromSuffix(".html");
        } else {
            return super.getMimeType(output);
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param decorate _more_
     * @param addLink _more_
     *
     *
     * @return _more_
     * @throws Exception _more_
     */
    public List<TwoFacedObject> getMetadataHtml(Request request, Entry entry,
            boolean decorate, boolean addLink)
            throws Exception {

        List<TwoFacedObject> result = new ArrayList<TwoFacedObject>();
        boolean showMetadata        = request.get(ARG_SHOWMETADATA, false);
        List<Metadata> metadataList = getMetadataManager().getMetadata(entry);
        if (metadataList.size() == 0) {
            return result;
        }


        Hashtable    catMap = new Hashtable();
        List<String> cats   = new ArrayList<String>();
        List<MetadataHandler> metadataHandlers =
            getMetadataManager().getMetadataHandlers();

        boolean canEdit = getAccessManager().canDoAction(request, entry,
                              Permission.ACTION_EDIT);

        boolean didone = false;
        for (Metadata metadata : metadataList) {
            MetadataType type = getRepository().getMetadataManager().findType(
                                    metadata.getType());
            if (type == null) {
                continue;
            }
            MetadataHandler metadataHandler = type.getHandler();
            String[] html = metadataHandler.getHtml(request, entry, metadata);
            if (html == null) {
                continue;
            }
            boolean isSimple = metadataHandler.isSimple(metadata);
            String  cat      = type.getDisplayCategory();
            if ( !decorate) {
                cat = "Properties";
            }
            Object[] blob     = (Object[]) catMap.get(cat);
            boolean  firstOne = false;
            if (blob == null) {
                firstOne = true;
                blob     = new Object[] { new StringBuffer(),
                                          new Integer(1) };
                catMap.put(cat, blob);
                cats.add(cat);
            }
            StringBuffer sb     = (StringBuffer) blob[0];
            int          rowNum = ((Integer) blob[1]).intValue();

            if (firstOne) {
                if (decorate) {
                    sb.append(
                        "<table width=\"100%\" border=0 cellspacing=\"0\" cellpadding=\"3\">\n");
                }
                if (addLink && canEdit) {
                    if (decorate) {
                        sb.append("<tr><td></td><td>");
                    }
                    sb.append(
                        new Link(
                            request.entryUrl(
                                getMetadataManager().URL_METADATA_FORM,
                                entry), iconUrl(ICON_METADATA_EDIT),
                                        msg("Edit Metadata")));
                    sb.append(
                        new Link(
                            request.entryUrl(
                                getRepository().getMetadataManager()
                                    .URL_METADATA_ADDFORM, entry), iconUrl(
                                        ICON_METADATA_ADD), msg(
                                        "Add Property")));
                    if (decorate) {
                        sb.append("</td></tr>");
                    }
                }
            }
            String theClass = HtmlUtil.cssClass("listrow" + rowNum);
            if (decorate && !isSimple) {
                String row =
                    " <tr  " + theClass
                    + " valign=\"top\"><td width=\"10%\" align=\"right\" valign=\"top\" class=\"formlabel\"><nobr>"
                    + html[0] + "</nobr></td><td>"
                //                    + HtmlUtil.makeToggleInline("", html[1], false)
                + HtmlUtil.makeToggleInline("", html[1], true) + "</td></tr>";
                sb.append(row);
            } else {
                String row =
                    " <tr  valign=\"top\"><td width=\"10%\" align=\"right\" valign=\"top\" class=\"formlabel\"><nobr>"
                    + html[0] + "</nobr></td><td>" + html[1] + "</td></tr>";
                sb.append(row);
            }
            if (++rowNum > 2) {
                rowNum = 1;
            }
            blob[1] = new Integer(rowNum);
        }


        for (String cat : cats) {
            Object[]     blob = (Object[]) catMap.get(cat);
            StringBuffer sb   = (StringBuffer) blob[0];
            if (decorate) {
                sb.append("</table>\n");
            }
            result.add(new TwoFacedObject(cat, sb));
        }

        return result;

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
    public Result getActionXml(Request request, Entry entry)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(getEntryManager().getEntryActionsTable(request, entry,
                OutputType.TYPE_ALL));

        StringBuffer xml = new StringBuffer("<content>\n");
        XmlUtil.appendCdata(xml,
                            getRepository().translate(request,
                                sb.toString()));
        xml.append("\n</content>");
        return new Result("", xml, "text/xml");
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param parent _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getChildrenXml(Request request, Entry parent,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        StringBuffer sb         = new StringBuffer();
        String       folder     = iconUrl(ICON_FOLDER_CLOSED);
        boolean      showLink   = request.get(ARG_SHOWLINK, true);
        boolean      onlyGroups = request.get(ARG_ONLYGROUPS, false);

        int          cnt        = 0;
        StringBuffer jsSB       = new StringBuffer();
        String       rowId;
        String       cbxId;
        String       cbxWrapperId;

        String       tabs = getInformationTabs(request, parent, true, true);
        if ( !showingAll(request, subGroups, entries)) {
            sb.append(msgLabel("Showing") + " 1.."
                      + (subGroups.size() + entries.size()));
            sb.append(HtmlUtil.space(2));
            String url = request.getEntryUrl(
                             getRepository().URL_ENTRY_SHOW.toString(),
                             parent);
            url = HtmlUtil.url(url, ARG_ENTRYID, parent.getId());
            sb.append(HtmlUtil.href(url, msg("More...")));
            sb.append(HtmlUtil.br());
        }

        for (Entry subGroup : subGroups) {
            if (cnt == 0) {
                //                sb.append(HtmlUtil.makeToggleInline("...", tabs, false));
            }
            cnt++;
            addEntryCheckbox(request, subGroup, sb, jsSB);
        }


        if ( !onlyGroups) {
            for (Entry entry : entries) {
                if (cnt == 0) {
                    //                    sb.append(HtmlUtil.makeToggleInline("...", tabs, false));
                }
                cnt++;
                addEntryCheckbox(request, entry, sb, jsSB);
            }
        }


        //            sb.append(getInformationTabs(request, parent, true, true));
        if (cnt == 0) {
            parent.getTypeHandler().handleNoEntriesHtml(request, parent, sb);
            sb.append(tabs);
            //            sb.append(entry.getDescription());
            if (getAccessManager().hasPermissionSet(parent,
                    Permission.ACTION_VIEWCHILDREN)) {
                if ( !getAccessManager().canDoAction(request, parent,
                        Permission.ACTION_VIEWCHILDREN)) {
                    sb.append(HtmlUtil.space(1));
                    sb.append(
                        msg(
                        "You do not have permission to view the sub-folders of this entry"));
                }
            }
        }

        StringBuffer xml = new StringBuffer("<response><content>\n");
        XmlUtil.appendCdata(xml,
                            getRepository().translate(request,
                                sb.toString()));
        xml.append("\n</content>");

        xml.append("<javascript>");
        XmlUtil.appendCdata(xml,
                            getRepository().translate(request,
                                jsSB.toString()));
        xml.append("</javascript>");
        xml.append("\n</response>");
        return new Result("", xml, "text/xml");
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getSelectXml(Request request, List<Entry> subGroups,
                               List<Entry> entries)
            throws Exception {
        String       localeId = request.getString(ARG_LOCALEID, null);

        String       target   = request.getString(ATTR_TARGET, "");
        StringBuffer sb       = new StringBuffer();



        //If we have a localeid that means this is the first call
        if (localeId != null) {
            Entry localeEntry = getEntryManager().getEntry(request, localeId);
            if (localeEntry != null) {
                if ( !localeEntry.isGroup()) {
                    localeEntry = getEntryManager().getParent(request,
                            localeEntry);
                }
                if (localeEntry != null) {
                    Entry grandParent = getEntryManager().getParent(request,
                                            localeEntry);
                    String indent = "";
                    if (grandParent != null) {
                        sb.append(getSelectLink(request, grandParent,
                                target));
                        //indent = HtmlUtil.space(2);
                    }
                    sb.append(indent);
                    sb.append(getSelectLink(request, localeEntry, target));
                    localeId = localeEntry.getId();
                    sb.append(
                        "<hr style=\"padding:0px;margin-bottom:2px;  margin:0px;\">");
                }
            }


            List<FavoriteEntry> favoritesList =
                getUserManager().getFavorites(request, request.getUser());
            StringBuffer favorites = new StringBuffer();
            if (favoritesList.size() > 0) {
                sb.append(HtmlUtil.b(msg("Favorites")));
                sb.append(HtmlUtil.br());
                List favoriteLinks = new ArrayList();
                for (FavoriteEntry favorite : favoritesList) {
                    Entry favEntry = favorite.getEntry();
                    sb.append(getSelectLink(request, favEntry, target));
                }
                sb.append(
                    "<hr style=\"padding:0px;margin-bottom:2px;  margin:0px;\">");
            }


            List<Entry> cartEntries = getUserManager().getCart(request);
            if (cartEntries.size() > 0) {
                sb.append(HtmlUtil.b(msg("Cart")));
                sb.append(HtmlUtil.br());
                for (Entry cartEntry : cartEntries) {
                    sb.append(getSelectLink(request, cartEntry, target));
                }
                sb.append(
                    "<hr style=\"padding:0px;margin-bottom:2px;  margin:0px;\">");
            }
        }


        for (Entry subGroup : subGroups) {
            if (Misc.equals(localeId, subGroup.getId())) {
                continue;
            }
            sb.append(getSelectLink(request, subGroup, target));
        }

        if (request.get(ARG_ALLENTRIES, false)) {
            for (Entry entry : entries) {
                sb.append(getSelectLink(request, entry, target));
            }
        }
        return makeAjaxResult(request,
                              getRepository().translate(request,
                                  sb.toString()));
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     * @param open _more_
     */
    private void addDescription(Request request, Entry entry,
                                StringBuffer sb, boolean open) {
        String desc = entry.getDescription().trim();
        if ((desc.length() > 0) && !TypeHandler.isWikiText(desc)
                && !desc.equals("<nolinks>")) {
            desc = getEntryManager().processText(request, entry, desc);
            StringBuffer descSB =
                new StringBuffer("\n<div class=\"description\">\n");
            descSB.append(desc);
            descSB.append("</div>\n");

            //            sb.append(HtmlUtil.makeShowHideBlock(msg("Description"),
            //                    descSB.toString(), open));

            //            sb.append(HtmlUtil.makeToggleInline("",
            //                                                desc, true));
            sb.append(desc);
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param includeDescription _more_
     * @param fixedHeight _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getInformationTabs(Request request, Entry entry,
                                     boolean includeDescription,
                                     boolean fixedHeight)
            throws Exception {
        String desc        = entry.getDescription();
        List   tabTitles   = new ArrayList<String>();
        List   tabContents = new ArrayList<String>();
        if (includeDescription && (desc.length() > 0)) {
            tabTitles.add("Description");
            desc = getEntryManager().processText(request, entry, desc);
            tabContents.add(desc);
        }

        tabTitles.add("Basic");
        Object basic;
        tabContents.add(basic = entry.getTypeHandler().getEntryContent(entry,
                request, false, true));

        for (TwoFacedObject tfo :
                getMetadataHtml(request, entry, true, true)) {
            tabTitles.add(tfo.toString());
            tabContents.add(tfo.getId());
        }
        entry.getTypeHandler().addToInformationTabs(request, entry,
                tabTitles, tabContents);

        tabTitles.add(msg("Comments"));
        StringBuffer comments = getCommentBlock(request, entry, true);
        //        System.out.println (comments);
        tabContents.add(comments);

        String attachments = getAttachmentsHtml(request, entry);
        if (attachments.length() > 0) {
            tabTitles.add(msg("Attachments"));
            tabContents.add(attachments);
        }

        StringBuffer associationBlock =
            getAssociationManager().getAssociationBlock(request, entry);
        if (request.get(ARG_SHOW_ASSOCIATIONS, false)) {
            tabTitles.add(0, msg("Links"));
            tabContents.add(0, associationBlock);
        } else {
            tabTitles.add(msg("Links"));
            tabContents.add(associationBlock);
        }


        //        tabTitles.add(msg(LABEL_LINKS));
        //        tabContents.add(getEntryManager().getEntryActionsTable(request, entry,
        //                OutputType.TYPE_ALL));


        return OutputHandler.makeTabs(tabTitles, tabContents, true);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param allEntries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputTimelineXml(Request request, Entry group,
                                    List<Entry> allEntries)
            throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy HH:mm:ss Z");
        StringBuffer     sb  = new StringBuffer();
        sb.append(XmlUtil.openTag(TAG_DATA));


        for (Entry entry : allEntries) {
            String icon = getEntryManager().getIconUrl(request, entry);
            StringBuffer attrs = new StringBuffer(XmlUtil.attrs(ATTR_TITLE,
                                     " " + entry.getName(), ATTR_ICON, icon));

            List<String> urls = new ArrayList<String>();
            getMetadataManager().getThumbnailUrls(request, entry, urls);
            if (urls.size() > 0) {
                attrs.append(XmlUtil.attrs(ATTR_IMAGE, urls.get(0)));
            }
            String entryUrl =
                request.entryUrl(getRepository().URL_ENTRY_SHOW, entry);
            attrs.append(XmlUtil.attrs(ATTR_LINK, entryUrl));

            attrs.append(
                XmlUtil.attrs(
                    ATTR_START, sdf.format(new Date(entry.getStartDate()))));
            if (entry.getStartDate() != entry.getEndDate()) {
                attrs.append(
                    XmlUtil.attrs(
                        ATTR_END, sdf.format(new Date(entry.getEndDate()))));
            }
            sb.append(XmlUtil.openTag(TAG_EVENT, attrs.toString()));
            if (entry.getDescription().length() > 0) {
                sb.append(XmlUtil.getCdata(entry.getDescription()));
            }
            sb.append(XmlUtil.closeTag(TAG_EVENT));
            sb.append("\n");
        }

        sb.append(XmlUtil.closeTag(TAG_DATA));
        //        System.err.println(sb);
        return new Result("", sb, "text/xml");
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
    public Result outputGrid(Request request, Entry group,
                             List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        StringBuffer sb         = new StringBuffer();
        List<Entry>  allEntries = new ArrayList<Entry>();
        allEntries.addAll(subGroups);
        allEntries.addAll(entries);
        makeGrid(request, allEntries, sb);
        return makeLinksResult(request, msg("Grid"), sb,
                               new State(group, subGroups, entries));
    }


    public Result outputTable(Request request, Entry group,
                             List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        StringBuffer sb         = new StringBuffer();
        List<Entry>  allEntries = new ArrayList<Entry>();
        allEntries.addAll(subGroups);
        allEntries.addAll(entries);
        makeTable(request, allEntries, sb);
        return makeLinksResult(request, msg("Table"), sb,
                               new State(group, subGroups, entries));
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param allEntries _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void makeGrid(Request request, List<Entry> allEntries,
                         StringBuffer sb)
            throws Exception {
        int cols = request.get(ARG_COLUMNS, 4);
        sb.append("<table width=100% border=0 cellpadding=10>");
        int     col           = 0;
        boolean needToOpenRow = true;
        int     width         = (int) (100 * 1.0 / (float) cols);
        for (Entry entry : allEntries) {
            if (col >= cols) {
                sb.append("</tr>");
                needToOpenRow = true;
                col           = 0;
            }
            if (needToOpenRow) {
                sb.append("<tr align=bottom>");
                needToOpenRow = false;
            }
            col++;
            sb.append("<td valign=bottom align=center width=" + width
                      + "% >");
            List<String> urls = new ArrayList<String>();
            getMetadataManager().getThumbnailUrls(request, entry, urls);
            String url = request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                          entry, ARG_OUTPUT, OUTPUT_GRID);
            if (urls.size() > 0) {
                sb.append(
                    HtmlUtil.href(
                        url,
                        HtmlUtil.img(
                            urls.get(0), "",
                            HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "100"))));
                sb.append(HtmlUtil.br());
            } else if (entry.getResource().isImage()) {
                String thumburl = HtmlUtil.url(
                                      request.url(repository.URL_ENTRY_GET)
                                      + "/"
                                      + getStorageManager().getFileTail(
                                          entry), ARG_ENTRYID, entry.getId(),
                                              ARG_IMAGEWIDTH, "" + 100);

                sb.append(HtmlUtil.href(url, HtmlUtil.img(thumburl)));
                sb.append(HtmlUtil.br());
            } else {
                sb.append(HtmlUtil.br());
                sb.append(HtmlUtil.space(1));
                sb.append(HtmlUtil.br());
            }
            String icon = getEntryManager().getIconUrl(request, entry);
            sb.append(HtmlUtil.href(url, HtmlUtil.img(icon)));
            sb.append(HtmlUtil.space(1));
            sb.append(getEntryManager().getTooltipLink(request, entry,
                    entry.getName(), url));
            sb.append(HtmlUtil.br());
            sb.append(getRepository().formatDateShort(request,
                    new Date(entry.getStartDate()),
                    getEntryManager().getTimezone(entry), ""));


            //            sb.append (getEntryManager().getAjaxLink( request,  entry,
            //                                                      "<br>"+entry.getName(),null, false));

            sb.append("</td>");
        }



        sb.append("</table>");

    }


    public void makeTable(Request request, List<Entry> allEntries,
                         StringBuffer sb)
            throws Exception {
        Hashtable<String,List<Entry>> map = new Hashtable<String,List<Entry>>();
        List<String> types = new ArrayList<String>();
        for (Entry entry : allEntries) {
            TypeHandler typeHandler = entry.getTypeHandler();
            String type = typeHandler.getType();
            List<Column> columns = typeHandler.getColumns();
            boolean hasFields = false;
            if(columns!=null) {
                for(Column column: columns) {
                    if(column.getCanList() && column.getCanShow()) {
                        hasFields=true;
                    }
                }
            }
            if(!hasFields) {
                if(typeHandler.isGroup()) {
                    type = "Folders";
                } else if(entry.isFile()) {
                    type = "Files";
                }
            }
            List<Entry> entries = map.get(type);
            if(entries==null) {
                entries = new ArrayList<Entry>();
                map.put(type, entries);
                types.add(type);
            }
            entries.add(entry);
        }

        int typeCnt = 0;
        for(String type: types) {
            typeCnt++;
            int numCols = 0;
            List<Entry> entries = map.get(type);
            TypeHandler typeHandler = entries.get(0).getTypeHandler();
            String typeLabel = type.equals("File")?"File":typeHandler.getLabel();
            List<Column> columns = typeHandler.getColumns();
            StringBuffer tableSB = new StringBuffer();
            tableSB.append("<div class=\"ramadda-entry-table\">");
            tableSB.append("<table width=100% cellspacing=2 cellpadding=2>");
            tableSB.append("<tr>");
            numCols++;
            //            tableSB.append(HtmlUtil.col("<b>" + msg("Name") +"</b>"));
            tableSB.append(HtmlUtil.col("&nbsp;"));
            numCols++;
            tableSB.append(HtmlUtil.col("&nbsp;"));
            //            tableSB.append(HtmlUtil.col("<b>" + msg("Date") +"</b>"));


            boolean isFile = false;
            for(Entry entry: entries) {
                if(entry.isFile()) {
                    isFile = true;
                    break;
                }
            }
            if(isFile) {
                numCols++;
                tableSB.append(HtmlUtil.col(""));
                numCols++;
                tableSB.append(HtmlUtil.col(HtmlUtil.b(msg("Size"))," align=right "));
            }
            //            tableSB.append(HtmlUtil.col("&nbsp;"));
            if(columns!=null) {
                for(Column column: columns) {
                    if(column.getCanList() && column.getCanShow()) {
                        numCols++;
                        tableSB.append(HtmlUtil.col(HtmlUtil.b(column.getLabel())));
                    }
                }
            }
            tableSB.append("</tr>");

            String blank  = HtmlUtil.img(getRepository().iconUrl(ICON_BLANK));
            for(Entry entry: entries) {
                tableSB.append("<tr valign=top style=\"border-bottom:1px #888 solid;\" >");
                tableSB.append(HtmlUtil.col(getEntryManager().getAjaxLink(request,entry,entry.getLabel()).toString()," nowrap "));
                tableSB.append(HtmlUtil.col(getRepository().formatDateShort(request,
                                                               new Date(entry.getStartDate()),
                                                                            getEntryManager().getTimezone(entry), "")," width=10% align=right "));

                if(entry.isFile()) {
                    tableSB.append(HtmlUtil.col(HtmlUtil.href(entry.getTypeHandler().getEntryResourceUrl(request,
                                                                                       entry), HtmlUtil.img(iconUrl(ICON_DOWNLOAD),
                                                                                                            msg("Download"), ""))," width=2% "));
                } else {
                    tableSB.append(HtmlUtil.col(""));
                }

                if(isFile) {
                    if(entry.isFile()) {
                        tableSB.append(HtmlUtil.col(formatFileLength(entry.getResource().getFileSize()), " align=right nowrap "));
                    } else {
                        tableSB.append(HtmlUtil.col("NA"," align=right nowrap "));
                    }

                }
                Object[]values = entry.getValues();

                if(columns!=null) {
                    for(Column column: columns) {
                        if(column.getCanList()) {
                            String s = column.getString(values);
                            if(s==null) s = "NA";
                            tableSB.append(HtmlUtil.col(s));
                        }
                    }
                }
                tableSB.append("</tr>");
                tableSB.append("<tr><td colspan=" + numCols +" style=\"border-bottom:1px #eee solid;\" >"  + blank +"</td></tr>");
            }
            tableSB.append("</table>");

            if(typeCnt>1) {
                sb.append("<p>");
            } 

            tableSB.append("</div>");
            if(types.size()>1) {
                sb.append(HtmlUtil.makeShowHideBlock(typeLabel, HtmlUtil.insetLeft(tableSB.toString(),10), true));
            } else {
                sb.append(tableSB.toString());
            }

        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     * @param sb _more_
     * @param style _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String makeTimeline(Request request, List<Entry> entries,
                               StringBuffer sb, String style)
            throws Exception {
        String           head    = "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy HH:mm:ss Z");
        long             minDate = 0;
        long             maxDate = 0;
        for (Entry entry : (List<Entry>) entries) {
            if ((minDate == 0) || (entry.getStartDate() < minDate)) {
                minDate = entry.getStartDate();
            }
            if ((maxDate == 0) || (entry.getEndDate() > maxDate)) {
                maxDate = entry.getEndDate();
            }
        }
        long diffDays = (maxDate - minDate) / 1000 / 3600 / 24;
        //            System.err.println("HOURS:" + diffDays +" " + new Date(minDate) + " " + new Date(maxDate));
        String interval = "Timeline.DateTime.MONTH";
        if (diffDays < 3) {
            interval = "Timeline.DateTime.HOUR";
        } else if (diffDays < 7) {
            interval = "Timeline.DateTime.DAY";
        } else if (diffDays < 30) {
            interval = "Timeline.DateTime.WEEK";
        } else if (diffDays < 150) {
            interval = "Timeline.DateTime.MONTH";
        } else if (diffDays < 10 * 365) {
            interval = "Timeline.DateTime.YEAR";
        } else {
            interval = "Timeline.DateTime.DECADE";
        }


        //        System.err.println(diffDays+ " " + interval+" min date:" +sdf.format(new Date(minDate)));

        //            sb.append(getTimelineApplet(request, allEntries));
        head = "<script>var Timeline_urlPrefix='${root}/timeline/timeline_js/';\nvar Timeline_ajax_url = '${root}/timeline/timeline_ajax/simile-ajax-api.js?bundle=true';\nTimeline_parameters='bundle=true';\n</script>\n<script src='${root}/timeline/timeline_js/timeline-api.js?bundle=true' type='text/javascript'></script>\n<link rel='stylesheet' href='${root}/timeline/timeline_js/timeline-bundle.css' type='text/css' />";
        head = head.replace("${root}", getRepository().getUrlBase());
        String timelineApplet =
            getRepository().getResource(
                "/org/ramadda/repository/resources/timeline.html");
        String url = request.getUrl();
        url = url + "&timelinexml=true";
        //            timelineApplet = timelineApplet.replace("${timelineurl}", "${root}/monet.xml");

        timelineApplet = timelineApplet.replace("${timelineurl}", url);
        timelineApplet = timelineApplet.replace("${basedate}",
                sdf.format(new Date(minDate)));
        timelineApplet = timelineApplet.replace("${intervalUnit}", interval);
        timelineApplet = timelineApplet.replace("${style}", style);


        sb.append(timelineApplet);
        return head;

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


        boolean isSearchResults = group.isDummy();
        TypeHandler typeHandler =
            getRepository().getTypeHandler(group.getType());

        if (outputType.equals(OUTPUT_INLINE)) {
            /*
            String wikiTemplate = getWikiText(request, group);
            if (wikiTemplate != null) {
                String wiki = getWikiManager().wikifyEntry(request, group, wikiTemplate, true, subGroups,
                                          entries);
                wiki = getRepository().translate(request, wiki);
                StringBuffer xml = new StringBuffer("<content>\n");
                XmlUtil.appendCdata(xml,
                                    "<div class=inline>" +wiki+"</div>");
                xml.append("\n</content>");
                return new Result("", xml, "text/xml");
                }*/

            return getChildrenXml(request, group, subGroups, entries);
        }

        if (outputType.equals(OUTPUT_SELECTXML)) {
            return getSelectXml(request, subGroups, entries);
        }
        if (outputType.equals(OUTPUT_METADATAXML)) {
            return getMetadataXml(request, group);
        }
        if (outputType.equals(OUTPUT_MAPINFO)) {
            return getMapInfo(request, group);
        }

        if (outputType.equals(OUTPUT_LINKSXML)) {
            return getLinksXml(request, group);
        }

        if (outputType.equals(OUTPUT_GRID)) {
            return outputGrid(request, group, subGroups, entries);
        }

        if (outputType.equals(OUTPUT_TABLE)) {
            return outputTable(request, group, subGroups, entries);
        }

        if (request.get("timelinexml", false)) {
            List<Entry> allEntries = new ArrayList<Entry>();
            allEntries.addAll(subGroups);
            allEntries.addAll(entries);
            return outputTimelineXml(request, group, allEntries);
        }

        //        Result typeResult = typeHandler.getHtmlDisplay(request, group, subGroups, entries);
        //        if (typeResult != null) {
        //            return typeResult;
        //        }

        boolean showTimeline = outputType.equals(OUTPUT_TIMELINE);
        if ( !showTimeline && (typeHandler != null)) {
            Result typeResult = typeHandler.getHtmlDisplay(request, group,
                                    subGroups, entries);

            if (typeResult != null) {
                return typeResult;
            }
        }


        StringBuffer sb = new StringBuffer();
        request.appendMessage(sb);

        String messageLeft = request.getLeftMessage();
        if (messageLeft != null) {
            sb.append(messageLeft);
        }



        showNext(request, subGroups, entries, sb);



        boolean hasChildren = ((subGroups.size() != 0)
                               || (entries.size() != 0));


        if (isSearchResults) {
            if ( !hasChildren) {
                sb.append(
                    getRepository().showDialogNote(msg("No entries found")));
            }
        }


        String wikiTemplate = null;
        //If the user specifically selected an output listing then don't do the wiki text
        if ( !request.exists(ARG_OUTPUT)
                || Misc.equals(request.getString(ARG_OUTPUT, ""),
                               OUTPUT_HTML.getId())) {
            wikiTemplate = getWikiText(request, group);
        }

        String head = null;

        if (showTimeline) {

            //            sb.append(getHtmlHeader(request,  group));
            List allEntries = new ArrayList(entries);
            allEntries.addAll(subGroups);


            head = makeTimeline(request, allEntries, sb, "height: 300px;");

            Result result = makeLinksResult(request, msg("Timeline"), sb,
                                            new State(group, subGroups,
                                                entries));
            if (head != null) {
                result.putProperty(PROP_HTML_HEAD, head);
            }
            return result;

        } else if ((wikiTemplate == null) && !group.isDummy()) {

            //            sb.append(getHtmlHeader(request,  group));

            addDescription(request, group, sb, !hasChildren);
            String informationBlock = getInformationTabs(request, group,
                                          false, false);
            sb.append(HtmlUtil.makeShowHideBlock(msg("Information"),
                    informationBlock,
                    request.get(ARG_SHOW_ASSOCIATIONS, !hasChildren)));

            StringBuffer metadataSB = new StringBuffer();
            getMetadataManager().decorateEntry(request, group, metadataSB,
                    false);
            String metataDataHtml = metadataSB.toString();
            if (metataDataHtml.length() > 0) {
                sb.append(HtmlUtil.makeShowHideBlock(msg("Attachments"),
                        "<div class=\"description\">" + metadataSB
                        + "</div>", false));
            }
        }

        if (wikiTemplate != null) {
            sb.append(getWikiManager().wikifyEntry(request, group,
                    wikiTemplate, true, subGroups, entries));
        } else {
            List<Entry> allEntries = new ArrayList<Entry>();
            allEntries.addAll(subGroups);
            allEntries.addAll(entries);
            if (allEntries.size() > 0) {
                StringBuffer groupsSB = new StringBuffer();
                String link = getEntriesList(request, groupsSB, allEntries,
                                             allEntries, true, false, true,
                                             group.isDummy(),
                                             group.isDummy());
                sb.append(HtmlUtil.makeShowHideBlock(msg("Entries") + link,
                        groupsSB.toString(), true));
            }

            if ( !group.isDummy() && (subGroups.size() == 0)
                    && (entries.size() == 0)) {
                if (getAccessManager().hasPermissionSet(group,
                        Permission.ACTION_VIEWCHILDREN)) {
                    if ( !getAccessManager().canDoAction(request, group,
                            Permission.ACTION_VIEWCHILDREN)) {
                        sb.append(
                            getRepository().showDialogWarning(
                                "You do not have permission to view the sub-folders of this entry"));
                    }
                }
            }

        }

        Result result = makeLinksResult(request, msg("Folder"), sb,
                                        new State(group, subGroups, entries));
        if (head != null) {
            result.putProperty(PROP_HTML_HEAD, head);
        }


        return result;
    }



    /** _more_ */
    private static boolean checkedTemplates = false;

    /** _more_ */
    private static String entryTemplate;

    /** _more_ */
    private static String groupTemplate;

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
    private String getWikiText(Request request, Entry entry)
            throws Exception {
        String description = entry.getDescription();
        //If it begins with <wiki> then it overrides anything else
        if (TypeHandler.isWikiText(description)) {
            return description;
        }

        String wikiTemplate = entry.getTypeHandler().getWikiTemplate(request,
                                  entry);
        if (wikiTemplate != null) {
            return wikiTemplate;
        }

        PageStyle pageStyle = request.getPageStyle(entry);
        //        if (TypeHandler.isWikiText(entry.getDescription())) {
        //            return entry.getDescription();
        //        }
        wikiTemplate = pageStyle.getWikiTemplate(entry);
        if (wikiTemplate != null) {
            return wikiTemplate;
        }
        return null;
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getTimelineApplet(Request request, List<Entry> entries)
            throws Exception {
        String timelineAppletTemplate =
            getRepository().getResource(PROP_HTML_TIMELINEAPPLET);
        List times  = new ArrayList();
        List labels = new ArrayList();
        List ids    = new ArrayList();
        for (Entry entry : entries) {
            String label = entry.getLabel();
            label = label.replaceAll(",", " ");
            times.add(SqlUtil.format(new Date(entry.getStartDate())));
            labels.add(label);
            ids.add(entry.getId());
        }
        String tmp = StringUtil.replace(timelineAppletTemplate, "${times}",
                                        StringUtil.join(",", times));
        tmp = StringUtil.replace(tmp, "${root}",
                                 getRepository().getUrlBase());
        tmp = StringUtil.replace(tmp, "${labels}",
                                 StringUtil.join(",", labels));
        tmp = StringUtil.replace(tmp, "${ids}", StringUtil.join(",", ids));
        tmp = StringUtil.replace(
            tmp, "${loadurl}",
            request.url(
                getRepository().URL_ENTRY_GETENTRIES, ARG_ENTRYIDS, "%ids%",
                ARG_OUTPUT, OUTPUT_HTML));
        return tmp;

    }







}
