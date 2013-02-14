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
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.JQuery;


import org.ramadda.util.Utils;


import org.w3c.dom.*;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;
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

    public static final OutputType OUTPUT_TEST =
        new OutputType("test", "html.test",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_DATA);


    /** _more_ */
    public static final OutputType OUTPUT_GRID =
        new OutputType("Grid Layout", "html.grid",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_DATA);

    public static final OutputType OUTPUT_TREEVIEW =
        new OutputType("Tree View", "html.treeview",
                       OutputType.TYPE_VIEW , "",
                       "/icons/application_side_tree.png");



    /*
    public static final OutputType OUTPUT_LISTING =
        new OutputType("Tree View", "html.listing",
                       OutputType.TYPE_VIEW , "",
                       "/icons/application_side_tree.png");
    */


    /** _more_ */
    public static final OutputType OUTPUT_GRAPH =
        new OutputType("Graph", "default.graph",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_GRAPH);

    /** _more_ */
    public static final OutputType OUTPUT_TABLE =
        new OutputType("Tabular Layout", "html.table",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_TABLE);

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
    public static final String ATTR_WIKI_SECTION = "wiki-section";

    /** _more_ */
    public static final String ATTR_WIKI_URL = "wiki-url";






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
        addType(OUTPUT_TABLE);
        addType(OUTPUT_GRID);
        addType(OUTPUT_TREEVIEW);
        addType(OUTPUT_GRAPH);
        addType(OUTPUT_INLINE);
        addType(OUTPUT_MAPINFO);
        addType(OUTPUT_SELECTXML);
        addType(OUTPUT_METADATAXML);
        addType(OUTPUT_LINKSXML);
        addType(OUTPUT_TEST);
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

        //        return makeHtmlHeader(request, entry, "Layout");
        return makeHtmlHeader(request, entry, "");
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
        OutputType[] types = new OutputType[] { OUTPUT_TREE, OUTPUT_TABLE,
                                                /*OUTPUT_GRID,*/ OUTPUT_TREEVIEW, CalendarOutputHandler.OUTPUT_TIMELINE,
                CalendarOutputHandler.OUTPUT_CALENDAR };
        StringBuffer sb =
            new StringBuffer(
                "<table border=0 cellspacing=0 cellpadding=0><tr>");
        String selected = request.getString(ARG_OUTPUT, OUTPUT_TREE.getId());
        if (title.length() > 0) {
            sb.append("<td align=center>" + msgLabel(title) + "</td>");
        }
        for (OutputType output : types) {
            String link = HtmlUtils.href(
                              request.entryUrl(
                                  getRepository().URL_ENTRY_SHOW, entry,
                                  ARG_OUTPUT, output), HtmlUtils.img(
                                      iconUrl(output.getIcon()),
                                      output.getLabel()));
            sb.append("<td align=center>");
            if (output.getId().equals(selected)) {
                sb.append(
                    HtmlUtils.div(
                        link, HtmlUtils.cssClass("toolbar-selected")));
            } else {
                sb.append(HtmlUtils.div(link, HtmlUtils.cssClass("toolbar")));
            }
            sb.append(" ");
            sb.append("</td>");
        }
        sb.append("</table>");

        return "<table border=0 cellspacing=0 cellpadding=0 width=100%><tr><td align=right>"
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
                links.add(makeLink(request, state.getEntry(), OUTPUT_TABLE));
                links.add(makeLink(request, state.getEntry(), OUTPUT_TREEVIEW));
                links.add(makeLink(request, state.getEntry(), OUTPUT_TEST));
                //                links.add(makeLink(request, state.getEntry(), OUTPUT_GRID));
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
        String html       = null;
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
    private Result getMetadataXml(Request request, Entry entry, boolean showLinks)
            throws Exception {
/*
        StringBuffer sb = new StringBuffer();
        request.put(ARG_OUTPUT, OUTPUT_HTML);
        boolean didOne = false;
        sb.append(HtmlUtils.open(HtmlUtils.TAG_TABLE));
        sb.append(entry.getTypeHandler().getInnerEntryContent(entry, request,
                OutputHandler.OUTPUT_HTML, true, true, true));
        for (TwoFacedObject tfo :
                getMetadataHtml(request, entry, false, false)) {
            sb.append(tfo.getId().toString());
        }
        sb.append(HtmlUtils.close(HtmlUtils.TAG_TABLE));
*/

        String contents;
        if(showLinks) {
            int menuType = OutputType.TYPE_VIEW | OutputType.TYPE_FILE | OutputType.TYPE_EDIT | OutputType.TYPE_OTHER;
            contents =  getEntryManager().getEntryActionsTable(request, entry,
                                                               menuType);
        } else  {
            contents = getInformationTabs(request, entry, true, true);
        }
        StringBuffer xml = new StringBuffer("<content>\n");
        XmlUtil.appendCdata(xml,
                            getRepository().translate(request, contents));
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
    public Result getLinksXml(Request request, Entry entry) throws Exception {
        StringBuffer sb = new StringBuffer("<content>\n");
        String links = getEntryManager().getEntryActionsTable(request, entry,
                           OutputType.TYPE_ALL);
        StringBuffer inner = new StringBuffer();
        String       cLink =
            HtmlUtils.jsLink(HtmlUtils.onMouseClick("hidePopupObject();"),
                             HtmlUtils.img(iconUrl(ICON_CLOSE)), "");
        inner.append(cLink);
        inner.append(HtmlUtils.br());
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
            return getMetadataXml(request, entry, true);
        }
        if (outputType.equals(OUTPUT_TEST)) {
            return outputTest(request, entry);
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

            return getMetadataXml(request, entry, false);
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
        return getHtmlResult(request, outputType, entry, true);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     * @param checkType _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlResult(Request request, OutputType outputType,
                                Entry entry, boolean checkType)
            throws Exception {

        TypeHandler typeHandler = entry.getTypeHandler();
        if (checkType) {
            Result typeResult = typeHandler.getHtmlDisplay(request, entry);
            if (typeResult != null) {
                return typeResult;
            }
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
            //            sb.append(HtmlUtils.makeShowHideBlock(msg("Information"),
            //                    informationBlock, true));
            sb.append(informationBlock);
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
            return HtmlUtils.makeShowHideBlock(msg("Attachments"),
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
        if (output.equals(OUTPUT_GRID) || output.equals(OUTPUT_TREEVIEW) || output.equals(OUTPUT_TABLE)) {
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


        Hashtable             catMap           = new Hashtable();
        List<String>          cats             = new ArrayList<String>();
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
                blob     = new Object[] { new StringBuffer(), new Integer(1) };
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
            String theClass = HtmlUtils.cssClass("listrow" + rowNum);
            if (decorate && !isSimple) {
                String row =
                    " <tr  " + theClass
                    + " valign=\"top\"><td width=\"10%\" align=\"right\" valign=\"top\" class=\"formlabel\"><nobr>"
                    + html[0] + "</nobr></td><td>"
                //                    + HtmlUtils.makeToggleInline("", html[1], false)
                + HtmlUtils.makeToggleInline("", html[1],
                                             true) + "</td></tr>";
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
            sb.append(HtmlUtils.space(2));
            String url = request.getEntryUrl(
                             getRepository().URL_ENTRY_SHOW.toString(),
                             parent);
            url = HtmlUtils.url(url, ARG_ENTRYID, parent.getId());
            sb.append(HtmlUtils.href(url, msg("More...")));
            sb.append(HtmlUtils.br());
        }

        for (Entry subGroup : subGroups) {
            cnt++;
            addEntryCheckbox(request, subGroup, sb, jsSB);
        }


        if ( !onlyGroups) {
            for (Entry entry : entries) {
                cnt++;
                addEntryCheckbox(request, entry, sb, jsSB);
            }
        }


        if (cnt == 0) {
            parent.getTypeHandler().handleNoEntriesHtml(request, parent, sb);
            sb.append(tabs);
            //            sb.append(entry.getDescription());
            if (getAccessManager().hasPermissionSet(parent,
                    Permission.ACTION_VIEWCHILDREN)) {
                if ( !getAccessManager().canDoAction(request, parent,
                        Permission.ACTION_VIEWCHILDREN)) {
                    sb.append(HtmlUtils.space(1));
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
                        //indent = HtmlUtils.space(2);
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
            if (favoritesList.size() > 0) {
                sb.append(HtmlUtils.center(HtmlUtils.b(msg("Favorites"))));
                List favoriteLinks = new ArrayList();
                for (FavoriteEntry favorite : favoritesList) {
                    Entry favEntry = favorite.getEntry();
                    sb.append(getSelectLink(request, favEntry, target));
                }
                sb.append(
                    "<hr style=\"padding:0px;margin-bottom:2px;  margin:0px;\">");
            }


            List<Entry> recents = getEntryManager().getSessionFolders(request); 
            if (recents.size() > 0) {
                sb.append(HtmlUtils.center(HtmlUtils.b(msg("Recent"))));
                List favoriteLinks = new ArrayList();
                for (Entry recent : recents) {
                    sb.append(getSelectLink(request, recent, target));
                }
                sb.append(
                    "<hr style=\"padding:0px;margin-bottom:2px;  margin:0px;\">");
            }


            List<Entry> cartEntries = getUserManager().getCart(request);
            if (cartEntries.size() > 0) {
                sb.append(HtmlUtils.b(msg("Cart")));
                sb.append(HtmlUtils.br());
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

            //            sb.append(HtmlUtils.makeShowHideBlock(msg("Description"),
            //                    descSB.toString(), open));

            //            sb.append(HtmlUtils.makeToggleInline("",
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
        List   tabTitles   = new ArrayList<String>();
        List   tabContents = new ArrayList<String>();
        StringBuffer basicSB = new StringBuffer();
        String desc        = entry.getDescription();
        if (includeDescription && (desc.length() > 0)) {
            desc = getEntryManager().processText(request, entry, desc);
            basicSB.append(desc);
            basicSB.append("<br>");
        }
        basicSB.append(entry.getTypeHandler().getEntryContent(entry,
                                                              request, false, true));

        tabTitles.add("Basic");
        tabContents.add(basicSB.toString());

        for (TwoFacedObject tfo :
                getMetadataHtml(request, entry, true, true)) {
            tabTitles.add(tfo.toString());
            tabContents.add(tfo.getId());
        }
        entry.getTypeHandler().addToInformationTabs(request, entry,
                tabTitles, tabContents);


        StringBuffer comments = getCommentBlock(request, entry, true);
        if (comments.length() > 0) {
            tabTitles.add(msg("Comments"));
            //        System.out.println (comments);
            tabContents.add(comments);
        }

        String attachments = getAttachmentsHtml(request, entry);
        if (attachments.length() > 0) {
            tabTitles.add(msg("Attachments"));
            tabContents.add(attachments);
        }

        StringBuffer associationBlock =
            getAssociationManager().getAssociationBlock(request, entry);
        if (associationBlock.length() > 0) {
            if (request.get(ARG_SHOW_ASSOCIATIONS, false)) {
                tabTitles.add(0, msg("Links"));
                tabContents.add(0, associationBlock);
            } else {
                tabTitles.add(msg("Links"));
                tabContents.add(associationBlock);
            }
        }


        //        tabTitles.add(msg(LABEL_LINKS));
        //        tabContents.add(getEntryManager().getEntryActionsTable(request, entry,
        //                OutputType.TYPE_ALL));

        if (tabContents.size() == 1) {
            return tabContents.get(0).toString();
        }


        return OutputHandler.makeTabs(tabTitles, tabContents, true);
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


    public Result outputTreeView(Request request, Entry group,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        StringBuffer sb         = new StringBuffer();
        List<Entry>  allEntries = new ArrayList<Entry>();
        allEntries.addAll(subGroups);
        allEntries.addAll(entries);
        makeTreeView(request, allEntries, sb);
        return makeLinksResult(request, msg("Tree View"), sb,
                               new State(group, subGroups, entries));
    }


    public Result outputTest(Request request, Entry group,
                              List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        return outputTest(request, group);
    }




    public Result outputTest(Request request, Entry entry)
            throws Exception {
        StringBuffer sb         = new StringBuffer();
        if(request.exists("select1")) {
            List<String> values = new ArrayList<String>();
            for(int i=1;i<5;i++) {
                if(!request.exists("select"+i)) break;
                values.add(request.getString("select"+i,""));
            }
            System.err.println("Values:" + values);
            String valueKey = StringUtil.join("::", values);
            StringBuffer json = new StringBuffer();
            String lastValue = values.get(values.size()-1);

            json.append(HtmlUtils.jsonMap(new String[]{
                        "values", HtmlUtils.jsonList(new String[]{"--",lastValue +"-v1",lastValue +"-v2",lastValue +"-v3"})},false));
            System.err.println(json);
            return new Result(BLANK, json,
                              getRepository().getMimeTypeFromSuffix(".json"));
        } 

        String formId = "form" + HtmlUtils.blockCnt++;
        StringBuffer js = new StringBuffer();
        js.append("var " + formId + " = new Form(" + HtmlUtils.squote(formId)+"," + HtmlUtils.squote(entry.getId()) +");\n");
        sb.append(request.form(getRepository().URL_ENTRY_FORM,
                               HtmlUtils.attr("id", formId)));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtils.input("value" ,"",
                                  HtmlUtils.attr("id",formId +"_value")));
        js.append(JQ.submit(JQ.id(formId), "return " +  HtmlUtils.call(formId +".submit", "")));
        for(int selectIdx=1;selectIdx<10;selectIdx++) {
            sb.append(HtmlUtils.p());
            sb.append(HtmlUtils.select("select" + selectIdx ,Misc.toList(new String[]{"apple","banana"}),(String)null,
                                       HtmlUtils.attr("id",formId +"_select" + selectIdx)));
            js.append(JQ.change(JQ.id(formId+"_select" + selectIdx), "return " + HtmlUtils.call(formId +".select" ,HtmlUtils.squote("" + selectIdx))));
        }
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit("submit","Submit"));

        sb.append(HtmlUtils.hr());
        sb.append(HtmlUtils.img(iconUrl("/icons/arrow.gif"),"", HtmlUtils.attr("id", formId+"_image")));

        sb.append(HtmlUtils.script(js.toString()));
        System.err.println(sb);
        return new Result("test", sb);
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
                    HtmlUtils.href(
                        url,
                        HtmlUtils.img(
                            urls.get(0), "",
                            HtmlUtils.attr(HtmlUtils.ATTR_WIDTH, "100"))));
                sb.append(HtmlUtils.br());
            } else if (entry.getResource().isImage()) {
                String thumburl = HtmlUtils.url(
                                      request.url(repository.URL_ENTRY_GET)
                                      + "/"
                                      + getStorageManager().getFileTail(
                                          entry), ARG_ENTRYID, entry.getId(),
                                              ARG_IMAGEWIDTH, "" + 100);

                sb.append(HtmlUtils.href(url, HtmlUtils.img(thumburl)));
                sb.append(HtmlUtils.br());
            } else {
                sb.append(HtmlUtils.br());
                sb.append(HtmlUtils.space(1));
                sb.append(HtmlUtils.br());
            }
            String icon = getEntryManager().getIconUrl(request, entry);
            sb.append(HtmlUtils.href(url, HtmlUtils.img(icon)));
            sb.append(HtmlUtils.space(1));
            sb.append(getEntryManager().getTooltipLink(request, entry,
                    entry.getName(), url));
            sb.append(HtmlUtils.br());
            sb.append(getRepository().formatDateShort(request,
                    new Date(entry.getStartDate()),
                    getEntryManager().getTimezone(entry), ""));


            //            sb.append (getEntryManager().getAjaxLink( request,  entry,
            //                                                      "<br>"+entry.getName(),null, false));

            sb.append("</td>");
        }



        sb.append("</table>");

    }



    public void makeTreeView(Request request, List<Entry> children,
                             StringBuffer sb)
            throws Exception {
        request.put(ARG_TREEVIEW, "true");
        StringBuffer listSB = new StringBuffer();
        sb.append("<table width=\"100%\"><tr valign=\"top\">");
        String link = getEntriesList(request, listSB,
                                     children, true, true, true, false);
        sb.append(HtmlUtils.col(link, HtmlUtils.attr(HtmlUtils.ATTR_WIDTH,"350")));
        String gotoHtml = HtmlUtils.mouseClickHref("treeViewGoTo();","Go to","");
        sb.append(HtmlUtils.col(HtmlUtils.leftRight(
                                                    HtmlUtils.div("&nbsp;", HtmlUtils.id("treeview_header")),
                                                    gotoHtml)));
        sb.append("</tr><tr valign=\"top\">");
        sb.append(HtmlUtils.col(listSB.toString()));
        sb.append(HtmlUtils.col("<iframe id=\"treeview_view\" src=\"/repository/blank\" width=\"750\" height=\"500\"></iframe>"));
        sb.append("</tr></table>");
        request.remove(ARG_TREEVIEW);
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
    public void makeTable(Request request, List<Entry> allEntries,
                          StringBuffer sb)
            throws Exception {

        Hashtable<String, List<Entry>> map = new Hashtable<String,
                                                 List<Entry>>();
        List<String> types = new ArrayList<String>();
        for (Entry entry : allEntries) {
            TypeHandler  typeHandler = entry.getTypeHandler();
            String       type        = typeHandler.getType();
            List<Column> columns     = typeHandler.getColumns();
            boolean      hasFields   = false;
            if (columns != null) {
                for (Column column : columns) {
                    if (column.getCanList() && column.getCanShow() && column.getRows()<=1) {
                        hasFields = true;
                    }
                }
            }
            if ( !hasFields) {
                if (typeHandler.isGroup()) {
                    type = "Folders";
                } else if (entry.isFile()) {
                    type = "Files";
                }
            }
            List<Entry> entries = map.get(type);
            if (entries == null) {
                entries = new ArrayList<Entry>();
                map.put(type, entries);
                types.add(type);
            }
            entries.add(entry);
        }

        int typeCnt = 0;
        for (String type : types) {
            typeCnt++;
            int          numCols     = 0;
            List<Entry>  entries     = map.get(type);
            TypeHandler  typeHandler = entries.get(0).getTypeHandler();
            String       typeLabel   = type.equals("File")
                                       ? "File"
                                       : typeHandler.getLabel();
            List<Column> columns     = typeHandler.getColumns();
            StringBuffer tableSB     = new StringBuffer();
            tableSB.append("<div class=\"ramadda-entry-table\">");
            tableSB.append("<table width=100% cellspacing=2 cellpadding=2 border=0>");
            tableSB.append("<tr>");
            numCols++;
            //            tableSB.append(HtmlUtils.col("<b>" + msg("Name") +"</b>"));
            tableSB.append(HtmlUtils.col("&nbsp;"));
            numCols++;
            tableSB.append(HtmlUtils.col("&nbsp;"));
            //            tableSB.append(HtmlUtils.col("<b>" + msg("Date") +"</b>"));


            boolean isFile = false;
            for (Entry entry : entries) {
                if (entry.isFile()) {
                    isFile = true;
                    break;
                }
            }
            if (isFile) {
                numCols++;
                tableSB.append(HtmlUtils.col(""));
                numCols++;
                tableSB.append(HtmlUtils.col(HtmlUtils.b(msg("Size")),
                                             " align=right "));
            }
            //            tableSB.append(HtmlUtils.col("&nbsp;"));
            if (columns != null) {
                for (Column column : columns) {
                    if (column.getCanList() && column.getCanShow()  && column.getRows()<=1) {
                        numCols++;
                        tableSB.append(
                            HtmlUtils.col(HtmlUtils.b(column.getLabel())));
                    }
                }
            }
            tableSB.append("</tr>");

            String blank = HtmlUtils.img(getRepository().iconUrl(ICON_BLANK));
            for (Entry entry : entries) {
                tableSB.append(
                    "<tr valign=top style=\"border-bottom:1px #888 solid;\" >");

                EntryLink entryLink = getEntryManager().getAjaxLink(
                            request, entry,
                            entry.getLabel());
                tableSB.append(
                               HtmlUtils.col(entryLink.getLink(), " xxxwidth=50%  "));
                tableSB.append(
                    HtmlUtils.col(
                        getRepository().formatDateShort(
                            request, new Date(entry.getStartDate()),
                            getEntryManager().getTimezone(entry),
                            ""), " width=10% align=right "));

                if (entry.isFile()) {
                    tableSB.append(
                        HtmlUtils.col(
                            HtmlUtils.href(
                                entry.getTypeHandler().getEntryResourceUrl(
                                    request, entry), HtmlUtils.img(
                                    iconUrl(ICON_DOWNLOAD), msg("Download"),
                                    "")), " width=2% "));
                } else {
                    //                    tableSB.append(HtmlUtils.col(""));
                }

                if (isFile) {
                    if (entry.isFile()) {
                        tableSB.append(HtmlUtils
                            .col(formatFileLength(entry.getResource()
                                .getFileSize()), " align=right nowrap "));
                    } else {
                        tableSB.append(HtmlUtils.col("NA",
                                " align=right nowrap "));
                    }

                }
                Object[] values = entry.getValues();

                if (columns != null) {
                    for (Column column : columns) {
                        if (column.getCanList() && column.getCanShow()  && column.getRows()<=1) {
                            String s = column.getString(values);
                            if (s == null) {
                                s = "NA";
                            }
                            tableSB.append(HtmlUtils.col(s));
                        }
                    }
                }
                tableSB.append("</tr>");
                //                tableSB.append("<tr><td colspan=" + numCols
                //                               + " style=\"border-bottom:1px #eee solid;\" >"
                //                               + blank + "</td></tr>");

                tableSB.append("<tr><td colspan=" + numCols
                               + " style=\"border-bottom:1px #eee solid;\" >"
                               + entryLink.getFolderBlock() + "</td></tr>");

            }
            tableSB.append("</table>");

            if (typeCnt > 1) {
                sb.append("<p>");
            }

            tableSB.append("</div>");
            if (types.size() > 1) {
                sb.append(HtmlUtils.makeShowHideBlock(typeLabel,
                        HtmlUtils.insetLeft(tableSB.toString(), 10), true));
            } else {
                sb.append(tableSB.toString());
            }

        }

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


        //This is a terrible hack but check if the request is for the timeline xml. If it is let the 
        //CalendarOutputHandler handle it.
        if (request.get("timelinexml", false)) {
            Result timelineResult = getCalendarOutputHandler().handleIfTimelineXml(request,  group, subGroups, entries);
            return timelineResult;
        }



        boolean     isSearchResults = group.isDummy();
        TypeHandler typeHandler     =
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
            return getMetadataXml(request, group, true);
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
        if (outputType.equals(OUTPUT_TREEVIEW)) {
            return outputTreeView(request, group, subGroups, entries);
        }

        if (outputType.equals(OUTPUT_TEST)) {
            return outputTest(request, group, subGroups, entries);
        }

        if (outputType.equals(OUTPUT_TABLE)) {
            return outputTable(request, group, subGroups, entries);
        }



        //        Result typeResult = typeHandler.getHtmlDisplay(request, group, subGroups, entries);
        //        if (typeResult != null) {
        //            return typeResult;
        //        }


        boolean doSimpleListing = !request.exists(ARG_OUTPUT);

        //If no children then show the details of this group
        if ((subGroups.size() == 0) && (entries.size() == 0)) {
            //            doSimpleListing = false;
        }



        if (typeHandler != null) {
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
                    getRepository().showDialogNote(msg("No pages found")));
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

        if ((wikiTemplate == null) && !group.isDummy()) {

            //            sb.append(getHtmlHeader(request,  group));
            addDescription(request, group, sb, !hasChildren);
            //If its the default view of an entry then just show the children listing
            if ( !doSimpleListing) {
                String informationBlock = getInformationTabs(request, group,
                                              false, false);

                if (hasChildren) {
                    sb.append(HtmlUtils.makeShowHideBlock(msg("Information"),
                            informationBlock,
                            request.get(ARG_SHOW_ASSOCIATIONS,
                                        !hasChildren)));
                } else {
                    sb.append(informationBlock);
                }
            }


            StringBuffer metadataSB = new StringBuffer();
            getMetadataManager().decorateEntry(request, group, metadataSB,
                    false);
            String metataDataHtml = metadataSB.toString();
            if (metataDataHtml.length() > 0) {
                sb.append(HtmlUtils.makeShowHideBlock(msg("Attachments"),
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
                sb.append(HtmlUtils.br());
                sb.append(link);
                sb.append(HtmlUtils.br());
                sb.append(groupsSB.toString());
            } else {
                if(!Utils.stringDefined(group.getDescription())) {
                    sb.append(
                              getRepository().showDialogNote(msg(LABEL_EMPTY_FOLDER)));
                }
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
