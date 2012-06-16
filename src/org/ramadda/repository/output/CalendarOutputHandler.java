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
import org.ramadda.repository.type.*;


import org.w3c.dom.*;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;
import org.ramadda.util.HtmlUtils;
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


import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
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
public class CalendarOutputHandler extends OutputHandler {



    /** _more_ */
    public static final OutputType OUTPUT_GRID =
        new OutputType("Date Grid", "calendar.grid",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_DATEGRID);

    /** _more_ */
    public static final OutputType OUTPUT_CALENDAR =
        new OutputType("Calendar", "calendar.calendar",
                       OutputType.TYPE_VIEW | OutputType.TYPE_FORSEARCH, "",
                       ICON_CALENDAR);


    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public CalendarOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_GRID);
        addType(OUTPUT_CALENDAR);
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
        if (state.entry != null) {
            return;
        }
        if (state.getEntry() != null) {
            links.add(makeLink(request, state.getEntry(), OUTPUT_CALENDAR));
            links.add(makeLink(request, state.getEntry(), OUTPUT_GRID));
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

        StringBuffer sb = new StringBuffer();
        //        sb.append(getRepository().getHtmlOutputHandler().getHtmlHeader(request, group));
        showNext(request, subGroups, entries, sb);
        entries.addAll(subGroups);
        Result result;
        if (outputType.equals(OUTPUT_GRID)) {
            result = outputDateGrid(request, group, entries, sb);
        } else {
            result = outputCalendar(request, group, entries, sb);
        }
        addLinks(request, result, new State(group, subGroups, entries));
        return result;
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param entries _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result outputDateGrid(Request request, Entry group,
                                  List<Entry> entries, StringBuffer sb)
            throws Exception {
        List             types    = new ArrayList();
        List             days     = new ArrayList();
        Hashtable        dayMap   = new Hashtable();
        Hashtable        typeMap  = new Hashtable();
        Hashtable        contents = new Hashtable();

        SimpleDateFormat sdf      = new SimpleDateFormat();
        sdf.setTimeZone(RepositoryUtil.TIMEZONE_DEFAULT);
        sdf.applyPattern("yyyy/MM/dd");
        SimpleDateFormat timeSdf = new SimpleDateFormat();
        timeSdf.setTimeZone(RepositoryUtil.TIMEZONE_DEFAULT);
        timeSdf.applyPattern("HH:mm");
        SimpleDateFormat monthSdf = new SimpleDateFormat();
        monthSdf.setTimeZone(RepositoryUtil.TIMEZONE_DEFAULT);
        monthSdf.applyPattern("MM");
        StringBuffer header = new StringBuffer();
        header.append(HtmlUtils.cols(HtmlUtils.bold(msg("Date"))));
        for (Entry entry : entries) {
            String type = entry.getTypeHandler().getType();
            String day  = sdf.format(new Date(entry.getStartDate()));
            if (typeMap.get(type) == null) {
                types.add(entry.getTypeHandler());
                typeMap.put(type, type);
                header.append(
                    "<td>" + HtmlUtils.bold(entry.getTypeHandler().getLabel())
                    + "</td>");
            }
            if (dayMap.get(day) == null) {
                days.add(new Date(entry.getStartDate()));
                dayMap.put(day, day);
            }
            String       time =
                timeSdf.format(new Date(entry.getStartDate()));
            String       key   = type + "_" + day;
            StringBuffer colSB = (StringBuffer) contents.get(key);
            if (colSB == null) {
                colSB = new StringBuffer();
                contents.put(key, colSB);
            }
            colSB.append(getEntryManager().getAjaxLink(request, entry, time));
        }


        sb.append(
            "<table class=\"dategrid\" border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">");
        days = Misc.sort(days);
        String currentMonth = "";
        for (int dayIdx = 0; dayIdx < days.size(); dayIdx++) {
            Date   date  = (Date) days.get(dayIdx);
            String month = monthSdf.format(date);
            //Put the header in every month
            if ( !currentMonth.equals(month)) {
                currentMonth = month;
                sb.append("<tr class=\"calheader\">" + header + "</tr>");
            }

            String day = sdf.format(date);
            sb.append("<tr valign=\"top\">");
            sb.append("<td width=\"5%\">" + day + "</td>");
            for (int i = 0; i < types.size(); i++) {
                TypeHandler  typeHandler = (TypeHandler) types.get(i);
                String       type        = typeHandler.getType();
                String       key         = type + "_" + day;
                StringBuffer cb          = (StringBuffer) contents.get(key);
                if (cb == null) {
                    sb.append("<td>&nbsp;</td>");
                } else {
                    sb.append(
                        "<td><div style=\"max-height: 150px; overflow-y: auto;\">"
                        + cb + "</div></td>");
                }
            }
            sb.append("</tr>");
        }
        sb.append("</table>");
        return new Result(msg("Date Grid"), sb);

    }

    /**
     * _more_
     *
     * @param day _more_
     *
     * @return _more_
     */
    public static GregorianCalendar getCalendar(int[] day) {
        return getCalendar(day[IDX_DAY], day[IDX_MONTH], day[IDX_YEAR]);
    }

    /**
     * _more_
     *
     * @param day _more_
     * @param month _more_
     * @param year _more_
     *
     * @return _more_
     */
    public static GregorianCalendar getCalendar(int day, int month,
            int year) {
        GregorianCalendar cal =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        cal.set(cal.DAY_OF_MONTH, day);
        cal.set(cal.MONTH, month);
        cal.set(cal.YEAR, year);
        return cal;
    }

    /**
     * _more_
     *
     * @param cal _more_
     *
     * @return _more_
     */
    public static int[] getDayMonthYear(GregorianCalendar cal) {
        return new int[] { cal.get(cal.DAY_OF_MONTH), cal.get(cal.MONTH),
                           cal.get(cal.YEAR) };
    }

    /**
     * _more_
     *
     * @param cal _more_
     * @param what _more_
     * @param delta _more_
     *
     * @return _more_
     */
    private GregorianCalendar add(GregorianCalendar cal, int what,
                                  int delta) {
        cal.add(what, delta);
        return cal;
    }


    /** _more_ */
    private static final int IDX_DAY = 0;

    /** _more_ */
    private static final int IDX_MONTH = 1;

    /** _more_ */
    private static final int IDX_YEAR = 2;


    /**
     * _more_
     *
     * @param cal _more_
     *
     * @return _more_
     */
    public static String getUrlArgs(GregorianCalendar cal) {
        return getUrlArgs(getDayMonthYear(cal));
    }


    /**
     * _more_
     *
     * @param dayMonthYear _more_
     *
     * @return _more_
     */
    public static String getUrlArgs(int[] dayMonthYear) {
        return ARG_YEAR + "=" + dayMonthYear[IDX_YEAR] + "&" + ARG_MONTH
               + "=" + dayMonthYear[IDX_MONTH] + "&" + ARG_DAY + "="
               + dayMonthYear[IDX_DAY];
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param entries _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputCalendar(Request request, Entry group,
                                 List<Entry> entries, StringBuffer sb)
            throws Exception {

        if (entries.size() == 0) {
            sb.append(
                getRepository().showDialogNote(msg("No entries found")));
        }
        outputCalendar(request, makeCalendarEntries(request, entries), sb, request.defined(ARG_DAY));
        if (true) {
            return new Result(msg("Calendar"), sb);
        }


        boolean hadDateArgs = request.defined(ARG_YEAR)
                              || request.defined(ARG_MONTH)
                              || request.defined(ARG_DAY);

        int[] today = getDayMonthYear(
                          new GregorianCalendar(
                              RepositoryUtil.TIMEZONE_DEFAULT));

        int[] selected = new int[] { request.get(ARG_DAY, today[IDX_DAY]),
                                     request.get(ARG_MONTH, today[IDX_MONTH]),
                                     request.get(ARG_YEAR, today[IDX_YEAR]) };

        boolean doDay = request.defined(ARG_DAY);

        int[]   prev  = (doDay
                         ? getDayMonthYear(add(getCalendar(selected),
                             Calendar.DAY_OF_MONTH, -1))
                         : getDayMonthYear(add(getCalendar(selected),
                             Calendar.MONTH, -1)));
        int[] next = (doDay
                      ? getDayMonthYear(add(getCalendar(selected),
                                            Calendar.DAY_OF_MONTH, 1))
                      : getDayMonthYear(add(getCalendar(selected),
                                            Calendar.MONTH, 1)));

        int[] prevprev = (doDay
                          ? getDayMonthYear(add(getCalendar(selected),
                              Calendar.MONTH, -1))
                          : getDayMonthYear(add(getCalendar(selected),
                              Calendar.YEAR, -1)));
        int[] nextnext = (doDay
                          ? getDayMonthYear(add(getCalendar(selected),
                              Calendar.MONTH, 1))
                          : getDayMonthYear(add(getCalendar(selected),
                              Calendar.YEAR, 1)));

        int[]                   someDate = null;


        List                    dayItems = null;
        Hashtable               dates    = new Hashtable();
        Hashtable<String, List> map      = new Hashtable<String, List>();
        GregorianCalendar mapCal =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        boolean didone = false;
        for (int tries = 0; tries < 2; tries++) {
            dayItems = new ArrayList();
            for (Entry entry : entries) {
                Date entryDate = new Date(entry.getStartDate());
                mapCal.setTime(entryDate);
                int[] entryDay = getDayMonthYear(mapCal);
                String key = entryDay[IDX_YEAR] + "/" + entryDay[IDX_MONTH]
                             + "/" + entryDay[IDX_DAY];
                if (tries == 0) {
                    dates.put(key, key);
                }
                if (someDate == null) {
                    someDate = entryDay;
                }
                if (doDay) {
                    if ((entryDay[IDX_DAY] != selected[IDX_DAY])
                            || (entryDay[IDX_MONTH] != selected[IDX_MONTH])
                            || (entryDay[IDX_YEAR] != selected[IDX_YEAR])) {
                        continue;
                    }
                } else {
                    if ( !((entryDay[IDX_YEAR] == selected[IDX_YEAR])
                            && (entryDay[IDX_MONTH]
                                == selected[IDX_MONTH]))) {
                        continue;
                    }
                }

                List dayList = map.get(key);
                if (dayList == null) {
                    map.put(key, dayList = new ArrayList());
                }
                String label = entry.getLabel();
                if (doDay) {
                    dayItems.add(entry);
                } else {
                    if (label.length() > 20) {
                        label = label.substring(0, 19) + "...";
                    }
                    dayList.add(
                        HtmlUtils.nobr(
                            getEntryManager().getAjaxLink(
                                request, entry, label).toString()));
                }
                didone = true;
            }


            if (didone || hadDateArgs) {
                break;
            }
            if (someDate != null) {
                selected = someDate;
                prev     = (doDay
                            ? getDayMonthYear(add(getCalendar(selected),
                            Calendar.DAY_OF_MONTH, -1))
                            : getDayMonthYear(add(getCalendar(selected),
                            Calendar.MONTH, -1)));
                next = (doDay
                        ? getDayMonthYear(add(getCalendar(selected),
                        Calendar.DAY_OF_MONTH, 1))
                        : getDayMonthYear(add(getCalendar(selected),
                        Calendar.MONTH, 1)));
                prevprev = (doDay
                            ? getDayMonthYear(add(getCalendar(selected),
                            Calendar.MONTH, -1))
                            : getDayMonthYear(add(getCalendar(selected),
                            Calendar.YEAR, -1)));
                nextnext = (doDay
                            ? getDayMonthYear(add(getCalendar(selected),
                            Calendar.MONTH, 1))
                            : getDayMonthYear(add(getCalendar(selected),
                            Calendar.YEAR, 1)));
            }
        }


        String[] navIcons = { "/icons/prevprev.gif", "/icons/prev.gif",
                              "/icons/today.gif", "/icons/next.gif",
                              "/icons/nextnext.gif" };


        String[]          navLabels;
        SimpleDateFormat  headerSdf;
        GregorianCalendar cal;
        List<String>      navUrls = new ArrayList<String>();


        if (doDay) {
            headerSdf = new SimpleDateFormat("MMMMM, dd yyyy");
            navLabels = new String[] { "Previous Month", "Previous Day",
                                       "Today", "Next Day", "Next Month" };
            cal = getCalendar(selected);
        } else {
            headerSdf = new SimpleDateFormat("MMMMM yyyy");
            navLabels = new String[] { "Previous Year", "Previous Month",
                                       "Current Month", "Next Month",
                                       "Next Year" };
            cal = getCalendar(1, selected[IDX_MONTH], selected[IDX_YEAR]);
        }


        request.put(ARG_YEAR, "" + (prevprev[IDX_YEAR]));
        request.put(ARG_MONTH, "" + (prevprev[IDX_MONTH]));
        if (doDay) {
            request.put(ARG_DAY, "" + (prevprev[IDX_DAY]));
        }
        navUrls.add(request.getUrl());


        request.put(ARG_YEAR, "" + (prev[IDX_YEAR]));
        request.put(ARG_MONTH, "" + (prev[IDX_MONTH]));
        if (doDay) {
            request.put(ARG_DAY, "" + (prev[IDX_DAY]));
        }
        navUrls.add(request.getUrl());


        request.put(ARG_YEAR, "" + (today[IDX_YEAR]));
        request.put(ARG_MONTH, "" + (today[IDX_MONTH]));
        if (doDay) {
            request.put(ARG_DAY, "" + (today[IDX_DAY]));
        }
        navUrls.add(request.getUrl());


        request.put(ARG_YEAR, "" + next[IDX_YEAR]);
        request.put(ARG_MONTH, "" + next[IDX_MONTH]);
        if (doDay) {
            request.put(ARG_DAY, "" + (next[IDX_DAY]));
        }
        navUrls.add(request.getUrl());

        request.put(ARG_YEAR, "" + (nextnext[IDX_YEAR]));
        request.put(ARG_MONTH, "" + (nextnext[IDX_MONTH]));
        if (doDay) {
            request.put(ARG_DAY, "" + (nextnext[IDX_DAY]));
        }
        navUrls.add(request.getUrl());


        request.remove(ARG_DAY);
        request.remove(ARG_MONTH);
        request.remove(ARG_YEAR);


        List navList = new ArrayList();

        for (int i = 0; i < navLabels.length; i++) {
            navList.add(HtmlUtils.href(navUrls.get(i),
                                      HtmlUtils.img(iconUrl(navIcons[i]),
                                          navLabels[i], " border=\"0\"")));
        }




        if (doDay) {
            StringBuffer tmp = new StringBuffer();
            String link = getEntriesList(request, tmp, dayItems, true, true,
                                         true, false);

            request.remove(ARG_MONTH);
            request.remove(ARG_YEAR);
            request.remove(ARG_DAY);
            sb.append(HtmlUtils.p());
            sb.append("<table  cellpadding=10 border=0><tr valign=top><td>");
            getRepository().createMonthNav(sb, cal.getTime(),
                                           request.getUrl(), dates);
            sb.append("</td><td>");
            if (request.isMobile()) {
                sb.append("</td></tr><tr valign=top><td>");
            }

            request.put(ARG_MONTH, "" + selected[IDX_MONTH]);
            request.put(ARG_YEAR, "" + selected[IDX_YEAR]);
            String monthUrl = request.getUrl();
            request.put(ARG_DAY, selected[IDX_DAY]);
            //            sb.append(HtmlUtils.b(StringUtil.join(HtmlUtils.space(1),
            //                    navList)));
            sb.append(
                HtmlUtils.href(
                    monthUrl, HtmlUtils.b(headerSdf.format(cal.getTime()))));
            if (dayItems.size() == 0) {
                sb.append("<p>No Entries");
            } else {
                sb.append(link);
                sb.append(tmp);
            }
            sb.append("</table>");
        } else {
            sb.append(
                HtmlUtils.center(
                    HtmlUtils.b(StringUtil.join(HtmlUtils.space(1), navList))));
            sb.append(
                HtmlUtils.center(HtmlUtils.b(headerSdf.format(cal.getTime()))));
            sb.append(
                "<table class=\"calendartable\"  cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">");
            String[] dayNames = {
                "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
            };

            /**
             *  for phrase extraction
             *  msg("Sun")
             *  msg("Mon")
             *  msg("Tue")
             *  msg("Wed")
             *  msg("Thu")
             *  msg("Fri")
             *  msg("Sat")
             */
            sb.append("<tr>");
            for (int colIdx = 0; colIdx < 7; colIdx++) {
                sb.append("<td width=\"14%\" class=\"calheader\">"
                          + msg(dayNames[colIdx]) + "</td>");
            }
            sb.append("</tr>");
            int startDow = cal.get(cal.DAY_OF_WEEK);
            while (startDow > 1) {
                cal.add(cal.DAY_OF_MONTH, -1);
                startDow--;
            }
            for (int rowIdx = 0; rowIdx < 6; rowIdx++) {
                sb.append("<tr valign=top>");
                for (int colIdx = 0; colIdx < 7; colIdx++) {
                    String content   = HtmlUtils.space(1);
                    String bg        = "";
                    int    thisDay   = cal.get(cal.DAY_OF_MONTH);
                    int    thisMonth = cal.get(cal.MONTH);
                    int    thisYear  = cal.get(cal.YEAR);
                    String key = thisYear + "/" + thisMonth + "/" + thisDay;
                    List   inner     = map.get(key);
                    request.put(ARG_MONTH, "" + thisMonth);
                    request.put(ARG_YEAR, "" + thisYear);
                    request.put(ARG_DAY, "" + thisDay);
                    String dayUrl = request.getUrl();
                    if (thisMonth != selected[IDX_MONTH]) {
                        bg = " style=\"background-color:lightgray;\"";
                    } else if ((today[IDX_DAY] == thisDay)
                               && (today[IDX_MONTH] == thisMonth)
                               && (today[IDX_YEAR] == thisYear)) {
                        bg = " style=\"background-color:lightblue;\"";
                    }
                    String dayContents = "&nbsp;";
                    if (inner != null) {
                        dayContents = HtmlUtils.div(StringUtil.join("",
                                inner), HtmlUtils.cssClass("calcontents"));
                    }
                    content =
                        "<table border=0 cellspacing=\"0\" cellpadding=\"2\" width=100%><tr valign=top><td>"
                        + dayContents + "</td><td align=right class=calday>"
                        + HtmlUtils.href(dayUrl, "" + thisDay)
                        + "<br>&nbsp;</td></tr></table>";
                    sb.append("<td class=\"calentry\" " + bg + " >" + content
                              + "</td>");
                    cal.add(cal.DAY_OF_MONTH, 1);
                }
                if ((cal.get(cal.YEAR) >= selected[IDX_YEAR])
                        && (cal.get(cal.MONTH) > selected[IDX_MONTH])) {
                    break;
                }
            }

            sb.append("</table>");
        }

        request.remove(ARG_DAY);
        request.remove(ARG_MONTH);
        request.remove(ARG_YEAR);

        return new Result(msg("Calendar"), sb);
    }


    public List<CalendarEntry> makeCalendarEntries(Request request, List<Entry> entries) throws Exception {
        List<CalendarEntry> calEntries =  new ArrayList<CalendarEntry>();
        for (Entry entry : entries) {
            Date   entryDate = new Date(entry.getStartDate());
            String label     = entry.getLabel();
            if (label.length() > 20) {
                label = label.substring(0, 19) + "...";
            }
            String url = HtmlUtils.nobr(getEntryManager().getAjaxLink(request,
                             entry, label, null, true, null,
                             false).toString());


            calEntries.add(new CalendarEntry(entryDate, url, entry));
        }
        return calEntries;
    }


    /**
     * Class description
     *
     *
     * @version        Enter version here..., Mon, May 3, '10
     * @author         Enter your name here...
     */
    public static class CalendarEntry {

        /** _more_ */
        Date date;

        /** _more_ */
        String label;

        /** _more_ */
        Object dayObject;

        /**
         * _more_
         *
         * @param date _more_
         * @param label _more_
         * @param dayObject _more_
         */
        public CalendarEntry(Date date, String label, Object dayObject) {
            this.date      = date;
            this.label     = label;
            this.dayObject = dayObject;
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param entries _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public void outputCalendar(Request request, List<CalendarEntry> entries,
                               StringBuffer sb, boolean doDay)
            throws Exception {


        boolean hadDateArgs = request.defined(ARG_YEAR)
                              || request.defined(ARG_MONTH)
                              || request.defined(ARG_DAY);

        int[] today = getDayMonthYear(
                          new GregorianCalendar(
                              RepositoryUtil.TIMEZONE_DEFAULT));

        int[] selected = new int[] { request.get(ARG_DAY, today[IDX_DAY]),
                                     request.get(ARG_MONTH, today[IDX_MONTH]),
                                     request.get(ARG_YEAR, today[IDX_YEAR]) };


        int[]   prev  = (doDay
                         ? getDayMonthYear(add(getCalendar(selected),
                             Calendar.DAY_OF_MONTH, -1))
                         : getDayMonthYear(add(getCalendar(selected),
                             Calendar.MONTH, -1)));
        int[] next = (doDay
                      ? getDayMonthYear(add(getCalendar(selected),
                                            Calendar.DAY_OF_MONTH, 1))
                      : getDayMonthYear(add(getCalendar(selected),
                                            Calendar.MONTH, 1)));

        int[] prevprev = (doDay
                          ? getDayMonthYear(add(getCalendar(selected),
                              Calendar.MONTH, -1))
                          : getDayMonthYear(add(getCalendar(selected),
                              Calendar.YEAR, -1)));
        int[] nextnext = (doDay
                          ? getDayMonthYear(add(getCalendar(selected),
                              Calendar.MONTH, 1))
                          : getDayMonthYear(add(getCalendar(selected),
                              Calendar.YEAR, 1)));

        int[]                   someDate = null;


        List                    dayItems = null;
        Hashtable               dates    = new Hashtable();
        Hashtable<String, List> map      = new Hashtable<String, List>();
        GregorianCalendar mapCal =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        boolean didone = false;
        for (int tries = 0; tries < 2; tries++) {
            dayItems = new ArrayList();
            for (CalendarEntry entry : entries) {
                Date entryDate = entry.date;
                mapCal.setTime(entryDate);
                int[] entryDay = getDayMonthYear(mapCal);
                String key = entryDay[IDX_YEAR] + "/" + entryDay[IDX_MONTH]
                             + "/" + entryDay[IDX_DAY];
                if (tries == 0) {
                    dates.put(key, key);
                }
                if (someDate == null) {
                    someDate = entryDay;
                }
                if (doDay) {
                    if ((entryDay[IDX_DAY] != selected[IDX_DAY])
                            || (entryDay[IDX_MONTH] != selected[IDX_MONTH])
                            || (entryDay[IDX_YEAR] != selected[IDX_YEAR])) {
                        continue;
                    }
                } else {
                    if ( !((entryDay[IDX_YEAR] == selected[IDX_YEAR])
                            && (entryDay[IDX_MONTH]
                                == selected[IDX_MONTH]))) {
                        continue;
                    }
                }
                List dayList = map.get(key);
                if (dayList == null) {
                    map.put(key, dayList = new ArrayList());
                }
                if (doDay) {
                    dayItems.add(entry.dayObject);
                } else {
                    dayList.add(entry.label);
                }
                didone = true;
            }


            if (didone || hadDateArgs) {
                break;
            }
            if (someDate != null) {
                selected = someDate;
                prev     = (doDay
                            ? getDayMonthYear(add(getCalendar(selected),
                            Calendar.DAY_OF_MONTH, -1))
                            : getDayMonthYear(add(getCalendar(selected),
                            Calendar.MONTH, -1)));
                next = (doDay
                        ? getDayMonthYear(add(getCalendar(selected),
                        Calendar.DAY_OF_MONTH, 1))
                        : getDayMonthYear(add(getCalendar(selected),
                        Calendar.MONTH, 1)));
                prevprev = (doDay
                            ? getDayMonthYear(add(getCalendar(selected),
                            Calendar.MONTH, -1))
                            : getDayMonthYear(add(getCalendar(selected),
                            Calendar.YEAR, -1)));
                nextnext = (doDay
                            ? getDayMonthYear(add(getCalendar(selected),
                            Calendar.MONTH, 1))
                            : getDayMonthYear(add(getCalendar(selected),
                            Calendar.YEAR, 1)));
            }
        }


        String[] navIcons = { "/icons/prevprev.gif", "/icons/prev.gif",
                              "/icons/today.gif", "/icons/next.gif",
                              "/icons/nextnext.gif" };


        String[]          navLabels;
        SimpleDateFormat  headerSdf;
        GregorianCalendar cal;
        List<String>      navUrls = new ArrayList<String>();


        if (doDay) {
            headerSdf = new SimpleDateFormat("MMMMM, dd yyyy");
            navLabels = new String[] { "Previous Month", "Previous Day",
                                       "Today", "Next Day", "Next Month" };
            cal = getCalendar(selected);
        } else {
            headerSdf = new SimpleDateFormat("MMMMM yyyy");
            navLabels = new String[] { "Last Year", "Last Month",
                                       "Current Month", "Next Month",
                                       "Next Year" };
            cal = getCalendar(1, selected[IDX_MONTH], selected[IDX_YEAR]);
        }


        request.put(ARG_YEAR, "" + (prevprev[IDX_YEAR]));
        request.put(ARG_MONTH, "" + (prevprev[IDX_MONTH]));
        if (doDay) {
            request.put(ARG_DAY, "" + (prevprev[IDX_DAY]));
        }
        navUrls.add(request.getUrl());


        request.put(ARG_YEAR, "" + (prev[IDX_YEAR]));
        request.put(ARG_MONTH, "" + (prev[IDX_MONTH]));
        if (doDay) {
            request.put(ARG_DAY, "" + (prev[IDX_DAY]));
        }
        navUrls.add(request.getUrl());


        request.put(ARG_YEAR, "" + (today[IDX_YEAR]));
        request.put(ARG_MONTH, "" + (today[IDX_MONTH]));
        if (doDay) {
            request.put(ARG_DAY, "" + (today[IDX_DAY]));
        }
        navUrls.add(request.getUrl());


        request.put(ARG_YEAR, "" + next[IDX_YEAR]);
        request.put(ARG_MONTH, "" + next[IDX_MONTH]);
        if (doDay) {
            request.put(ARG_DAY, "" + (next[IDX_DAY]));
        }
        navUrls.add(request.getUrl());

        request.put(ARG_YEAR, "" + (nextnext[IDX_YEAR]));
        request.put(ARG_MONTH, "" + (nextnext[IDX_MONTH]));
        if (doDay) {
            request.put(ARG_DAY, "" + (nextnext[IDX_DAY]));
        }
        navUrls.add(request.getUrl());


        request.remove(ARG_DAY);
        request.remove(ARG_MONTH);
        request.remove(ARG_YEAR);


        List navList = new ArrayList();

        for (int i = 0; i < navLabels.length; i++) {
            navList.add(HtmlUtils.href(navUrls.get(i),
                                      HtmlUtils.img(iconUrl(navIcons[i]),
                                          navLabels[i], " border=\"0\"")));
        }


        if (doDay) {
            StringBuffer tmp  = new StringBuffer();
            String       link = "";
            if (dayItems.size() > 0) {
                if (dayItems.get(0) instanceof Entry) {
                    link = getEntriesList(request, tmp, dayItems, true, true,
                                          true, false);
                } else {
                    link = StringUtil.join(" ", dayItems);
                }
            }
            request.remove(ARG_MONTH);
            request.remove(ARG_YEAR);
            request.remove(ARG_DAY);
            sb.append(HtmlUtils.p());
            sb.append(
                "<table  width=100% border=0 cellpadding=10><tr valign=top><td width=200>");
            getRepository().createMonthNav(sb, cal.getTime(),
                                           request.getUrl(), dates);
            sb.append("</td><td>");
            if (request.isMobile()) {
                sb.append("</td></tr><tr valign=top><td>");
            }
            request.put(ARG_MONTH, "" + selected[IDX_MONTH]);
            request.put(ARG_YEAR, "" + selected[IDX_YEAR]);
            String monthUrl = request.getUrl();
            request.put(ARG_DAY, selected[IDX_DAY]);
            //            sb.append(HtmlUtils.b(StringUtil.join(HtmlUtils.space(1),
            //                    navList)));
            sb.append(
                HtmlUtils.href(
                    monthUrl, HtmlUtils.b(headerSdf.format(cal.getTime()))));
            if (dayItems.size() == 0) {
                sb.append("<p>No Entries");
            } else {
                sb.append(HtmlUtils.br());
                sb.append(link);
                sb.append(tmp);
            }
            sb.append("</table>");
        } else {
            sb.append(
                HtmlUtils.center(
                    HtmlUtils.b(StringUtil.join(HtmlUtils.space(1), navList))));
            sb.append(
                HtmlUtils.center(HtmlUtils.b(headerSdf.format(cal.getTime()))));
            sb.append(
                "<table class=\"calendartable\" border=\"1\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">");
            String[] dayNames = {
                "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
            };
            sb.append("<tr>");
            for (int colIdx = 0; colIdx < 7; colIdx++) {
                sb.append("<td width=\"14%\" class=\"calheader\">"
                          + msg(dayNames[colIdx]) + "</td>");
            }
            sb.append("</tr>");
            int startDow = cal.get(cal.DAY_OF_WEEK);
            while (startDow > 1) {
                cal.add(cal.DAY_OF_MONTH, -1);
                startDow--;
            }
            for (int rowIdx = 0; rowIdx < 6; rowIdx++) {
                sb.append("<tr valign=top>");
                for (int colIdx = 0; colIdx < 7; colIdx++) {
                    String content   = HtmlUtils.space(1);
                    String bg        = "";
                    int    thisDay   = cal.get(cal.DAY_OF_MONTH);
                    int    thisMonth = cal.get(cal.MONTH);
                    int    thisYear  = cal.get(cal.YEAR);
                    String key = thisYear + "/" + thisMonth + "/" + thisDay;
                    List   inner     = map.get(key);
                    request.put(ARG_MONTH, "" + thisMonth);
                    request.put(ARG_YEAR, "" + thisYear);
                    request.put(ARG_DAY, "" + thisDay);
                    String dayUrl = request.getUrl();
                    if (thisMonth != selected[IDX_MONTH]) {
                        bg = " style=\"background-color:lightgray;\"";
                    } else if ((today[IDX_DAY] == thisDay)
                               && (today[IDX_MONTH] == thisMonth)
                               && (today[IDX_YEAR] == thisYear)) {
                        bg = " style=\"background-color:lightblue;\"";
                    }
                    String dayContents = "&nbsp;";
                    if (inner != null) {
                        dayContents = HtmlUtils.div(StringUtil.join("",
                                inner), HtmlUtils.cssClass("calcontents"));
                    }
                    content =
                        "<table border=0 cellspacing=\"0\" cellpadding=\"2\" width=100%><tr valign=top><td>"
                        + dayContents
                        + "</td><td align=right class=calday width=5>"
                        + HtmlUtils.href(dayUrl, "" + thisDay)
                        + "<br>&nbsp;</td></tr></table>";
                    sb.append("<td class=\"calentry\" " + bg + " >" + content
                              + "</td>");
                    cal.add(cal.DAY_OF_MONTH, 1);
                }
                if ((cal.get(cal.YEAR) >= selected[IDX_YEAR])
                        && (cal.get(cal.MONTH) > selected[IDX_MONTH])) {
                    break;
                }
            }

            sb.append("</table>");
        }

        request.remove(ARG_DAY);
        request.remove(ARG_MONTH);
        request.remove(ARG_YEAR);

        //        return new Result(msg("Calendar"), sb);
    }



}
