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

package org.ramadda.repository.search;


import org.ramadda.repository.*;

import org.ramadda.repository.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
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

    /** _more_ */
    public static final String TAB_LIST = "list";

    /** _more_ */
    public static final String TAB_MAP = "map";

    /** _more_ */
    public static final String TAB_EARTH = "earth";

    /** _more_ */
    public static final String TAB_TIMELINE = "timeline";


    /** _more_ */
    private RequestUrl URL_SEARCH;

    /** _more_ */
    public static final String ARG_SEARCH_SUBMIT = "search.submit";

    public static final String ARG_SEARCH_REFINE = "search.refine";
    public static final String ARG_SEARCH_CLEAR = "search.clear";

    /** _more_ */
    private String theType;

    /** _more_ */
    private TypeHandler typeHandler;

    /** _more_ */
    private String searchUrl;

    /** _more_ */
    private boolean searchOpen = true;

    /** _more_          */
    private boolean showText = true;

    /** _more_          */
    private boolean showArea = true;

    /** _more_          */
    private boolean showDate = true;

    /** _more_ */
    private String label;

    /** _more_ */
    private List<String> metadataTypes = new ArrayList<String>();

    /** _more_ */
    private List<String> tabs = new ArrayList<String>();

    /**
     * _more_
     *
     * @param repository _more_
     */
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

    /**
     * _more_
     *
     * @param node _more_
     * @param props _more_
     *
     * @throws Exception _more_
     */
    public void init(Element node, Hashtable props) throws Exception {
        String types = (String) props.get("metadatatypes");
        if (types != null) {
            for (String type : StringUtil.split(types, ",", true, true)) {
                metadataTypes.add(type);
            }
        }
        String tabsToUse = (String) props.get("tabs");
        if (tabsToUse != null) {
            tabs.addAll(StringUtil.split(tabsToUse, ",", true, true));
        } else {
            tabs.add(TAB_LIST);
            tabs.add(TAB_MAP);
            tabs.add(TAB_EARTH);
            tabs.add(TAB_TIMELINE);
        }


        searchOpen  = Misc.getProperty(props, "searchopen", true);
        showText    = Misc.getProperty(props, "form.text.show", true);
        showArea    = Misc.getProperty(props, "form.area.show", true);
        showDate    = Misc.getProperty(props, "form.date.show", true);
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
        //        request.put("atom.id", theType);
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


        int contentsWidth  = 750;
        int contentsHeight = 450;
        int minWidth       = contentsWidth + 200;
        request.put(ARG_TYPE, theType);
        List<Entry> entries = new ArrayList<Entry>();
        boolean refinement =request.exists(ARG_SEARCH_REFINE);
        if(!refinement) {
            List[] groupAndEntries =
                getRepository().getEntryManager().getEntries(request);
            entries = (List<Entry>) groupAndEntries[0];
            entries.addAll(groupAndEntries[1]);
        }

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
                          contentsWidth, contentsHeight, true);

        getMapManager().addToMap(request, map, entries, false, null, true);
        Rectangle2D.Double bounds = getEntryManager().getBounds(entries);


        //shrink the bounds down
        if ((bounds != null) && (bounds.getWidth() > 180)) {
            double cx = bounds.getX() + bounds.getWidth() / 2;
            double cy = bounds.getY() + bounds.getHeight() / 2;
            int    f  = (int) (bounds.getWidth() / 3);
            bounds = new Rectangle2D.Double(cx - f, cy - f / 2, f * 2, f);
        }
        //        map.centerOn(bounds);
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
        formSB.append("<div style=\"min-width:200px;\">");
        formSB.append(request.form(URL_SEARCH,
                                   HtmlUtil.attr(HtmlUtil.ATTR_NAME,
                                       "apisearchform")));
        formSB.append(HtmlUtil.br());
        formSB.append(HtmlUtil.formTable());
        if (showText) {
            formSB.append(
                HtmlUtil.formEntry(
                    msgLabel(
                        typeHandler.getFormLabel(
                            null, ARG_NAME, "Text")), HtmlUtil.input(
                                ARG_TEXT, request.getString(ARG_TEXT, ""),
                                HtmlUtil.SIZE_15 + " autofocus ")));
        }

        if (showDate) {
            TypeHandler.addDateSearch(getRepository(), request, formSB,
                                      Constants.dataDate);

        }

        if (showArea) {
            String clearLink =
                map.getSelectorClearLink(repository.msg("Clear"));
            String searchType =
                TypeHandler.getSpatialSearchTypeWidget(request);
            String widget = map.getSelectorWidget(ARG_AREA, nwse);
            formSB.append(HtmlUtil.formEntry(msgLabel("Location"),
                                             HtmlUtil.table(new Object[] {
                                                 widget,
                    clearLink })));
        }
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
                                                         ARG_SEARCH_SUBMIT) +"  " +
                                         HtmlUtil.submit(msg("Refine"),
                                                         ARG_SEARCH_REFINE)));

        formSB.append(HtmlUtil.formTableClose());
        formSB.append(HtmlUtil.formClose());
        formSB.append("</div>");


        List<String> tabContents = new ArrayList<String>();
        List<String> tabTitles   = new ArrayList<String>();
        StringBuffer timelineSB  = new StringBuffer();
        StringBuffer listSB      = new StringBuffer();


        makeEntryList(request, listSB, entries);

        String head =
            getRepository().getHtmlOutputHandler().makeTimeline(request,
                entries, timelineSB,
                "width:" + contentsWidth + "px; height: " + contentsHeight
                + "px;");


        StringBuffer mapSB = new StringBuffer(
                                 HtmlUtil.italics(
                                     msg("Shift-drag to select region")));
        mapSB.append(map.getHtml());

        //Pad it out
        listSB.append(
            "&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>");

        if(refinement) {
            tabTitles.add(msg("Results"));
            tabContents.add(
                            HtmlUtil.div(
                                         getRepository().showDialogNote("Search criteria refined"),
                                         HtmlUtil.style("min-width:" + minWidth + "px")));
        } else {
            if (entries.size() == 0) {
                tabTitles.add(msg("Results"));
                tabContents.add(
                                HtmlUtil.div(
                                             getRepository().showDialogNote("No entries found"),
                                             HtmlUtil.style("min-width:" + minWidth + "px")));
            } else {
                for (String tab : tabs) {
                    if (tab.equals(TAB_LIST)) {
                        tabContents.add(HtmlUtil.div(listSB.toString(),
                                                     HtmlUtil.style("min-width:"
                                                                    + minWidth + "px")));
                        tabTitles.add(HtmlUtil.img(iconUrl(ICON_LIST)) + " "
                                      + msg("List"));
                    } else if (tab.equals(TAB_MAP)) {
                        tabContents.add(HtmlUtil.div(mapSB.toString(),
                                                     HtmlUtil.style("min-width:"
                                                                    + minWidth + "px")));
                        tabTitles.add(HtmlUtil.img(iconUrl(ICON_MAP)) + " "
                                      + msg("Map"));
                    } else if (tab.equals(TAB_TIMELINE)) {
                        tabContents.add(HtmlUtil.div(timelineSB.toString(),
                                                     HtmlUtil.style("min-width:"
                                                                    + minWidth + "px")));
                        tabTitles.add(HtmlUtil.img(iconUrl(ICON_TIMELINE)) + " "
                                      + msg("Timeline"));
                    } else if (tab.equals(TAB_EARTH)
                               && getMapManager().isGoogleEarthEnabled(request)) {
                        StringBuffer earthSB = new StringBuffer();
                        getMapManager().getGoogleEarth(request, entries, earthSB,
                                                       contentsWidth - MapManager.EARTH_ENTRIES_WIDTH,
                                                       contentsHeight, true, false);
                        tabContents.add(HtmlUtil.div(earthSB.toString(),
                                                     HtmlUtil.style("min-width:"
                                                                    + minWidth + "px")));
                        tabTitles.add(HtmlUtil.img(iconUrl(ICON_GOOGLEEARTH)) + " "
                                      + msg("Earth"));
                    }
                }
            }
        }
        String tabs = (tabContents.size() == 1)
                      ? tabContents.get(0)
                      : OutputHandler.makeTabs(tabTitles, tabContents, true);
        sb.append(
            "<table width=100% border=0 cellpadding=0 cellspacing=0><tr valign=top>");
        String searchHtml =
            HtmlUtil.makeShowHideBlock(HtmlUtil.img(iconUrl(ICON_SEARCH)),
                                       formSB.toString(), searchOpen);
        sb.append(HtmlUtil.col(searchHtml, ""
        /*HtmlUtil.attr(HtmlUtil.ATTR_WIDTH, "200")*/
        ));
        sb.append(HtmlUtil.col(tabs, HtmlUtil.style("min-width:" + minWidth
                + "px;") + HtmlUtil.attr(HtmlUtil.ATTR_ALIGN, "left")));
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
        getRepository().getHtmlOutputHandler().makeTable(request, entries,
                sb);
    }



}
