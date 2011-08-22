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

package org.ramadda.repository.search;


import org.ramadda.repository.*;

import org.ramadda.repository.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.StringUtil;

import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author jeff mcwhirter
 * @version $Revision: 1.3 $
 */
public class SpecialSearch extends RepositoryManager implements RequestHandler {

    public static final String TAB_LIST = "list";
    public static final String TAB_MAP = "map";
    public static final String TAB_EARTH = "earth";
    public static final String TAB_TIMELINE = "timeline";


    /** _more_ */
    private RequestUrl URL_SEARCH;

    /** _more_ */
    public static final String ARG_SEARCH_SUBMIT = "search.submit";

    /** _more_ */
    private String theType;

    /** _more_ */
    private TypeHandler typeHandler;

    /** _more_ */
    private String searchUrl;

    /** _more_ */
    private String label;

    /** _more_ */
    private List<String> metadataTypes = new ArrayList<String>();

    private List<String> tabs=  new ArrayList<String>();

    public SpecialSearch(Repository repository) {
        super(repository);
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @param props _more_
     *
     * @throws Exception _more_
     */
    public SpecialSearch(Repository repository, Element node, Hashtable props)
            throws Exception {
        super(repository);
        init(node, props);
    }

    public void init(Element node, Hashtable props) throws Exception {
        String types = (String) props.get("metadatatypes");
        if (types != null) {
            for (String type : StringUtil.split(types, ",", true, true)) {
                metadataTypes.add(type);
            }
        }
        String tabsToUse = (String) props.get("tabs");
        if (tabsToUse != null) {
            tabs.addAll(StringUtil.split(tabsToUse,",",true,true));
        } else {
            tabs.add(TAB_LIST);
            tabs.add(TAB_MAP);
            tabs.add(TAB_EARTH);
            tabs.add(TAB_TIMELINE);
        }

        searchUrl   = (String) props.get("searchurl");
        label       = (String) props.get("label");
        theType     = (String) props.get("type");
        typeHandler = getRepository().getTypeHandler(theType);
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
    public Result processCapabilitiesRequest(Request request)
            throws Exception {
        request.put(ARG_OUTPUT, "atom");
        request.put(ARG_TYPE, theType);
        Result result =
            getRepository().getSearchManager().processEntrySearch(request);
        return result;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void makeHeader(Request request, StringBuffer sb)
            throws Exception {}


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processSearchRequest(Request request) throws Exception {

        int tabWidth  = 700;
        int tabHeight = 350;

        request.put(ARG_TYPE, theType);
        List[] groupAndEntries =
            getRepository().getEntryManager().getEntries(request);
        List<Entry> entries = (List<Entry>) groupAndEntries[0];

        if (request.exists("timelinexml")) {
            Entry group = getRepository().getEntryManager().getDummyGroup();
            return getRepository().getHtmlOutputHandler().outputTimelineXml(
                request, group, entries);
        }

        StringBuffer sb = new StringBuffer();
        StringBuffer js = new StringBuffer();
        if (URL_SEARCH == null) {
            URL_SEARCH = new RequestUrl(this, searchUrl);
        }


        makeHeader(request, sb);

        String[] nwse = new String[] { request.getString(ARG_AREA_NORTH, ""),
                                       request.getString(ARG_AREA_WEST, ""),
                                       request.getString(ARG_AREA_SOUTH, ""),
                                       request.getString(ARG_AREA_EAST,
                                           ""), };


        MapInfo map = getRepository().getMapManager().createMap(request,
                          tabWidth, tabHeight, true);
        MapOutputHandler mapOutputHandler =
            (MapOutputHandler) getRepository().getOutputHandler(
                MapOutputHandler.OUTPUT_MAP);
        if (mapOutputHandler != null) {
            mapOutputHandler.addToMap(request, map, entries, null, true);
        }
        Rectangle2D.Double bounds = getEntryManager().getBounds(entries);

        if ((bounds != null) && (bounds.getWidth() > 180) && false) {
            double cx = bounds.getX() + bounds.getWidth() / 2;
            double cy = bounds.getY() + bounds.getHeight() / 2;
            int    f  = 120;
            bounds = new Rectangle2D.Double(cx - f, cy - f / 2, f * 2, f);
        }
        map.centerOn(bounds);
        map.addJS(map.getVariableName() + ".initMap(true);\n");
        if (request.defined(ARG_AREA_NORTH) && request.defined(ARG_AREA_WEST)
                && request.defined(ARG_AREA_SOUTH)
                && request.defined(ARG_AREA_EAST)) {
            map.addJS(
                HtmlUtil.call(
                    map.getVariableName() + ".setSelectionBox",
                    request.get(ARG_AREA_NORTH, 0.0) + ","
                    + request.get(ARG_AREA_WEST, 0.0) + ","
                    + request.get(ARG_AREA_SOUTH, 0.0) + ","
                    + request.get(ARG_AREA_EAST, 0.0)));

        }



        String initParams = HtmlUtil.squote(ARG_AREA) + "," + true + ","
                            + "0";
        map.addJS(map.getVariableName() + ".setSelection(" + initParams
                  + ");\n");
        map.centerOn(bounds);






        StringBuffer formSB = new StringBuffer();
        formSB.append(request.form(URL_SEARCH,
                                   HtmlUtil.attr(HtmlUtil.ATTR_NAME,
                                       "apisearchform")));
        formSB.append(HtmlUtil.br());
        formSB.append(HtmlUtil.formTable());
        formSB.append(
            HtmlUtil.formEntryTop(
                msgLabel(typeHandler.getFormLabel(null, ARG_NAME, "Text")),
                HtmlUtil.input(
                    ARG_TEXT, request.getString(ARG_TEXT, ""),
                    HtmlUtil.SIZE_20 + " autofocus ")));

        String clearLink  = map.getSelectorClearLink(repository.msg("Clear"));
        String searchType = TypeHandler.getSpatialSearchTypeWidget(request);
        String widget     = map.getSelectorWidget(ARG_AREA, nwse);
        formSB.append(HtmlUtil.formEntry(msgLabel("Location"),
                                         HtmlUtil.table(new Object[] { widget,
                clearLink })));
        //        formSB.append(HtmlUtil.formEntry("", searchType));


        typeHandler.addToSpecialSearchForm(request, formSB);

        for (String type : metadataTypes) {
            MetadataType metadataType =
                getRepository().getMetadataManager().findType(type);
            if (metadataType != null) {
                metadataType.getHandler().addToSearchForm(request, formSB,
                        metadataType);
            }
        }

        formSB.append(HtmlUtil.formEntry("",
                                         HtmlUtil.submit(msg("Search"),
                                             ARG_SEARCH_SUBMIT)));
        formSB.append(HtmlUtil.formTableClose());
        formSB.append(HtmlUtil.formClose());


        List<String> tabContents = new ArrayList<String>();
        List<String> tabTitles   = new ArrayList<String>();
        StringBuffer timelineSB  = new StringBuffer();
        StringBuffer listSB      = new StringBuffer();


        makeEntryList(request, listSB, entries);

        String head =
            getRepository().getHtmlOutputHandler().makeTimeline(request,
                entries, timelineSB,
                "width:" + tabWidth + "px; height: " + tabHeight + "px;");


        StringBuffer mapSB =
            new StringBuffer(msg("Shift-drag to select region"));
        mapSB.append(map.getHtml());

        //Pad it out
        listSB.append(
            "&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>");


        for(String tab: tabs) {
            if(tab.equals(TAB_LIST)) {
                tabContents.add(listSB.toString());
                tabTitles.add(msg("List"));
            } else   if(tab.equals(TAB_MAP)) {
                tabContents.add(mapSB.toString());
                tabTitles.add(msg("Map"));
            } else   if(tab.equals(TAB_TIMELINE)) {
                tabContents.add(timelineSB.toString());
                tabTitles.add(msg("Timeline"));
            } else   if(tab.equals(TAB_EARTH) &&
                        getMapManager().isGoogleEarthEnabled(request)) {
                StringBuffer earthSB = new StringBuffer();
                getMapManager().getGoogleEarth(request,  
                                           entries, earthSB, 600,500);
                tabContents.add(earthSB.toString());
                tabTitles.add(msg("Earth"));
            }
        }

        String tabs = OutputHandler.makeTabs(tabTitles, tabContents, true,
                                             "tab_content");
        sb.append(
            "<table width=100% border=0 cellpadding=0 cellspacing=0><tr valign=top>");
        sb.append(HtmlUtil.col(formSB.toString(),
                               HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "30%")));
        sb.append(HtmlUtil.col(tabs,
                               HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "70%")));
        sb.append("</table>");

        sb.append(HtmlUtil.script(js.toString()));
        if (entries.size() == 0) {
            //            sb.append(getRepository().showDialogNote("No entries found"));
        }



        for (Entry entry : entries) {}

        Result result = new Result("Search", sb);
        result.putProperty(PROP_HTML_HEAD, head);

        return getRepository().getEntryManager().addEntryHeader(request,
                getRepository().getEntryManager().getTopGroup(), result);

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param entries _more_
     *
     * @throws Exception _more_
     */
    public void makeEntryList(Request request, StringBuffer sb,
                              List<Entry> entries)
            throws Exception {
        Result tmpResult =
            getRepository().getHtmlOutputHandler().outputGroup(request,
                HtmlOutputHandler.OUTPUT_HTML,
                getRepository().getEntryManager().getDummyGroup(), entries,
                new ArrayList<Entry>());

        sb.append(new String(tmpResult.getContent()));
    }



}
