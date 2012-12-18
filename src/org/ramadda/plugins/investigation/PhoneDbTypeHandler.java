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

package org.ramadda.plugins.investigation;


import org.ramadda.plugins.db.*;

import org.ramadda.repository.output.*;


import org.ramadda.repository.*;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;


import org.ramadda.repository.type.*;
import ucar.unidata.sql.*;
import ucar.unidata.xml.XmlUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;



import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Hashtable;
import java.util.HashSet;



import org.ramadda.util.Utils;
import org.apache.commons.lang.text.StrTokenizer;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;



/**
 *
 */

public class PhoneDbTypeHandler extends DbTypeHandler {

    public static final String VIEW_CALL_LISTING = "call.listing";
    public static final String VIEW_CALL_GRAPH = "call.graph";
    public static final String VIEW_CALL_GRAPH_DATA = "call.graphdata";

    private static final String ARROW = " &rarr; ";


    public static final String ARG_DB_NAMES = "db.names";
    public static final String ARG_IDS = "ids";

    Column fromNumberColumn;
    Column toNumberColumn;
    Column fromNameColumn;
    Column toNameColumn;
    Column dateColumn;


    /**
     * _more_
     *
     *
     * @param dbAdmin _more_
     * @param repository _more_
     * @param tableName _more_
     * @param tableNode _more_
     * @param desc _more_
     *
     * @throws Exception _more_
     */
    public PhoneDbTypeHandler(DbAdminHandler dbAdmin,
                              Repository repository, String tableName,
                              Element tableNode, String desc)
        throws Exception {
        super(dbAdmin, repository, tableName, tableNode, desc);
    }


    public void init(List<Element> columnNodes) throws Exception {
        super.init(columnNodes);
        viewList.add(1, new TwoFacedObject("Call Listing", VIEW_CALL_LISTING));
        viewList.add(1, new TwoFacedObject("Call Graph", VIEW_CALL_GRAPH));
    }


    public void addHeaderItems(Request request, Entry entry, String view,
                               List<String> headerToks, String baseUrl,
                               boolean[] addNext) {

        super.addHeaderItems(request, entry, view, headerToks, baseUrl, addNext);
        if (view.equals(VIEW_CALL_LISTING)) {
            addNext[0] = true;
            headerToks.add(HtmlUtils.b(msg("Call Listing")));
        } else {
            headerToks.add(HtmlUtils.href(baseUrl + "&" + ARG_DB_VIEW
                                          + "=" + VIEW_CALL_LISTING, msg("Call Listing")));
        }

        if (view.equals(VIEW_CALL_GRAPH)) {
            addNext[0] = true;
            headerToks.add(HtmlUtils.b(msg("Call Graph")));
        } else {
            headerToks.add(HtmlUtils.href(baseUrl + "&" + ARG_DB_VIEW
                                          + "=" + VIEW_CALL_GRAPH, msg("Call Graph")));
        }
    }

    @Override
        public void formatTableValue(Request request, Entry entry, StringBuffer sb, Column column, Object[]values, SimpleDateFormat sdf) throws Exception {
        if(column.equals(fromNumberColumn) || column.equals(toNumberColumn)) {
            sb.append(formatNumber(column.getString(values)));
        } else {
            super.formatTableValue(request, entry,  sb, column, values,sdf);
        }
    }




    public String formatNumber(String n) {
        n = n.trim();
        if(n.length()!=10) return n;
        return n.substring(0,3) +"-" + n.substring(3,6) +"-" +n.substring(6);
    }



    @Override
        public void addToEditForm(Request request, Entry entry, StringBuffer formBuffer) {
        super.addToEditForm(request, entry, formBuffer);
        formBuffer.append(
                          HtmlUtils.row(
                                        HtmlUtils.colspan(
                                                          "Enter numbers and names", 2)));
        formBuffer.append(HtmlUtils.formEntry(msgLabel("Numbers and names"),
                                              HtmlUtils.textArea(ARG_DB_NAMES,"#number=name\n#e.g.:\n#303-555-1212= some name\n", 8, 50)));
    }

    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
        throws Exception {
        super.initializeEntryFromForm(request,  entry, parent,  newEntry);
        if(newEntry) return;
        List<String[]> numberToName = new ArrayList<String[]>();
        for(String line: StringUtil.split(request.getString(ARG_DB_NAMES,""), "\n", true, true)) {
            line  = line.trim();
            if(line.startsWith("#")) continue;
            List<String> numberAndName =  StringUtil.splitUpTo(line,"=", 2);
            if(numberAndName.size()<2) continue;
            String number = numberAndName.get(0).trim();
            String name = numberAndName.get(1).trim();
            number = cleanUpNumber(number);
            if(number.length()>0 && name.length()>0) {
                numberToName.add(new String[]{number, name});
            }
        }
        if(numberToName.size()>0) {
            for(String[] pair: numberToName) {
                String number = pair[0];
                String name = pair[1];
                List<Clause> fromWhere = new ArrayList<Clause>();
                fromWhere.add(Clause.eq(COL_ID, entry.getId()));
                fromWhere.add(Clause.eq(fromNumberColumn.getName(), number));
                Clause fromClause= Clause.and(fromWhere);
                getDatabaseManager().update(tableHandler.getTableName(), fromClause,new String[]{getFromNameColumn()},
                                            new Object[]{ name});


                List<Clause> toWhere = new ArrayList<Clause>();
                toWhere.add(Clause.eq(COL_ID, entry.getId()));
                toWhere.add(Clause.eq(toNumberColumn.getName(), number));
                Clause toClause= Clause.and(toWhere);
                getDatabaseManager().update(tableHandler.getTableName(), toClause,new String[]{getToNameColumn()},
                                            new Object[]{ name});
            }
        }
    }

    public String cleanUpNumber(String number) {
        return number;
    }

    public String getFromNameColumn() {
        return "from_name";
    }


    public String getToNameColumn() {
        return "to_name";
    }


    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {
        String       view     = request.getString(ARG_DB_VIEW, VIEW_TABLE);

        if (view.equals(VIEW_CALL_GRAPH_DATA)) {
            return handleCallGraphData(request, entry);
        }
        return super.getHtmlDisplay(request, entry);
    }

    private int cnt = 0;

    public Result handleCallGraphPage(Request request, Entry entry, List<Object[]> valueList, boolean fromSearch) throws Exception  {
        String graphAppletTemplate = getRepository().getResource("/org/ramadda/plugins/investigation/resources/graphapplet.html");
        String counter = "" + (cnt++);
        graphAppletTemplate = graphAppletTemplate.replace("${counter}",
                                                          counter);
        String html = StringUtil.replace(graphAppletTemplate, "${id}",
                                         HtmlUtils.urlEncode(entry.getId()));
        html = StringUtil.replace(html, "${root}",
                                  getRepository().getUrlBase());

        StringBuffer ids = new StringBuffer();
        HashSet seen = new HashSet();
        for(int i=0;i<valueList.size();i++) {
            Object[] values  = valueList.get(i);

            String fromNumber = fromNumberColumn.getString(values);
            if(seen.contains(fromNumber)) continue;
            seen.add(fromNumber);
            if(ids.length()>0) ids.append(",");
            ids.append(fromNumber);



            String toNumber = toNumberColumn.getString(values);
            if(seen.contains(toNumber)) continue;
            seen.add(toNumber);
            if(ids.length()>0) ids.append(",");
            ids.append(toNumber);

            //for now just get the first ID
            if(i>10) break;
        }
        html = StringUtil.replace(html, "${ids}", ids.toString());
        System.err.println (html);

        StringBuffer sb = new StringBuffer();
        addViewHeader(request, entry, sb, VIEW_CALL_GRAPH, valueList.size(),
                      fromSearch);
        sb.append(html);
        Result result = new Result(msg("Graph"), sb);
        return result;
    }




    private String makeXml(Request request, Entry entry, String fromNumber) throws Exception   {
        String id = fromNumber;
        //        String fromNumber = fromNumberColumn.getString(values);
        
        String imageAttr =
            XmlUtil.attrs("imagepath",
                          getEntryIcon(request, entry));
        String attrs = imageAttr
            + XmlUtil.attrs(ATTR_TYPE, "entry", 
                            ATTR_ID,
                            id, ATTR_TOOLTIP,
                            fromNumber, ATTR_TITLE,
                            fromNumber);
        return XmlUtil.tag(TAG_NODE, attrs);
    }
    
    public Result handleCallGraphData(Request request, Entry entry) throws Exception  {
        StringBuffer sb = new StringBuffer();
        List<String> ids = StringUtil.split(request.getString(ARG_IDS,""));
        HashSet seen = new HashSet();
        for(String number: ids) {
            sb.append(makeXml(request, entry,number));
            sb.append("\n");
            List<Clause> where;


            where = new ArrayList<Clause>();
            where.add(Clause.eq(COL_ID, entry.getId()));
            where.add(Clause.eq(fromNumberColumn.getFullName(), number));
            for(Object []tuple: readValues(request, entry, Clause.and(where))) {
                String otherNumber = toNumberColumn.getString(tuple);
                if(!seen.contains(otherNumber)) {
                    sb.append(makeXml(request, entry,otherNumber));
                    sb.append("\n");
                    seen.add(otherNumber);
                }
                sb.append(XmlUtil.tag(TAG_EDGE,
                                      XmlUtil.attrs(ATTR_TYPE, "link", ATTR_TO, otherNumber,
                                                    ATTR_FROM, number)));
            }


            where = new ArrayList<Clause>();
            where.add(Clause.eq(COL_ID, entry.getId()));
            where.add(Clause.eq(toNumberColumn.getFullName(), number));
            for(Object []tuple: readValues(request, entry, Clause.and(where))) {
                String otherNumber = fromNumberColumn.getString(tuple);
                if(!seen.contains(otherNumber)) {
                    sb.append(makeXml(request, entry,otherNumber));
                    sb.append("\n");
                    seen.add(otherNumber);
                }
                sb.append(XmlUtil.tag(TAG_EDGE,
                                      XmlUtil.attrs(ATTR_TYPE, "link", ATTR_TO, fromNumber,
                                                    ATTR_FROM, otherNumber)));
            }

            
        }

        String graphXmlTemplate =
            getRepository().getResource(PROP_HTML_GRAPHTEMPLATE);

        String xml = StringUtil.replace(graphXmlTemplate, "${content}",
                                        sb.toString());
        xml = StringUtil.replace(xml, "${root}",
                                 getRepository().getUrlBase());
        return new Result(BLANK, new StringBuffer(xml),
                          getRepository().getMimeTypeFromSuffix(".xml"));

    }

    public Result makeListResults(Request request, Entry entry, String view,
                                  String action, boolean fromSearch,
                                  List<Object[]> valueList)
        throws Exception {
        if (view.equals(VIEW_CALL_LISTING)) {
            return handleCallListing(request, entry, valueList, fromSearch);
        }
        if (view.equals(VIEW_CALL_GRAPH)) {
            return handleCallGraphPage(request, entry, valueList,fromSearch);
        }
        return  super.makeListResults(request,  entry,  view,
                                      action,  fromSearch,
                                      valueList);
    }



    public Result handleCallListing(Request request, Entry entry,
                                    List<Object[]> valueList, boolean fromSearch)
        throws Exception {

        Hashtable<String,Number> numberMap = new Hashtable<String,Number>();
        List<Number> numbers = new ArrayList<Number>();
        StringBuffer sb         = new StringBuffer();
        addViewHeader(request, entry, sb, VIEW_CALL_LISTING, valueList.size(),
                      fromSearch);
        
        for(Object[] tuple : valueList) {
            String fromNumber = fromNumberColumn.getString(tuple);
            Number from = numberMap.get(fromNumber);
            if(from==null)  {
                from = new Number(fromNumber);
                numbers.add(from);
                numberMap.put(from.number, from);
            }
            String toNumber = toNumberColumn.getString(tuple);
            Number to = numberMap.get(toNumber);
            if(to==null)  {
                to = new Number(toNumber);
                numbers.add(to);
                numberMap.put(to.number, to);
            }
            String date = dateColumn.getString(tuple);
            from.addOutbound(to, tuple);
            to.addInbound(from, tuple);
        }

        SimpleDateFormat sdf = getDateFormat(entry);

        Collections.sort(numbers);
        for(Number n: numbers) {
            //Only show numbers with outbounds
            if(n.outbound.size()<=1 && n.inbound.size()<=1) continue;

            sb.append(HtmlUtils.p());
            sb.append(HtmlUtils.div(formatNumber(n.number), HtmlUtils.cssClass("ramadda-heading-1")));

            if(n.outbound.size()>0) { 
                sb.append(HtmlUtils.open(HtmlUtils.TAG_DIV, HtmlUtils.style("margin-top:0px;margin-left:20px;")));
                StringBuffer numberSB = new StringBuffer("<table cellspacing=5 width=100%>");
                for(Number outbound:n.getSortedOutbound()) {
                    numberSB.append("<tr valign=top><td width=10%>");
                    String searchUrl =
                        HtmlUtils.url(request.url(getRepository().URL_ENTRY_SHOW),
                                      new String[] {
                                          ARG_ENTRYID, entry.getId(), ARG_DB_SEARCH, "true", 
                                          fromNumberColumn.getFullName(), n.number,
                                          toNumberColumn.getFullName(), outbound.number,
                                      });

                    numberSB.append(HtmlUtils.href(searchUrl, formatNumber(outbound.number)));
                    numberSB.append("</td>");

                    List<Object[]> calls = n.getOutboundCalls(outbound);
                    StringBuffer callSB  = new StringBuffer();
                    for(Object[]values: calls) {
                        StringBuffer dateSB = new StringBuffer();
                        dateColumn.formatValue(entry, dateSB, Column.OUTPUT_HTML, values, sdf);
                        StringBuffer html = new StringBuffer();
                        getHtml(request, html,entry,values);
                        callSB.append(HtmlUtils.makeShowHideBlock(dateSB.toString(), HtmlUtils.insetLeft(HtmlUtils.div(html.toString(),HtmlUtils.cssClass("ramadda-popup-box")), 10), false));
                    }
                    numberSB.append("<td width=5% align=right>");
                    numberSB.append(calls.size());
                    numberSB.append("</td><td width=85%>");
                    numberSB.append(HtmlUtils.makeShowHideBlock("Details", HtmlUtils.insetLeft(callSB.toString(),10), false));
                    numberSB.append("</td></tr>");
                }
                numberSB.append("</table>");
                sb.append(HtmlUtils.makeShowHideBlock("Outbound", HtmlUtils.insetLeft(numberSB.toString(), 10), true));
                sb.append(HtmlUtils.close(HtmlUtils.TAG_DIV));
            }


            if(n.inbound.size()>0) { 
                sb.append(HtmlUtils.open(HtmlUtils.TAG_DIV, HtmlUtils.style("margin-top:0px;margin-left:20px;")));
                StringBuffer numberSB = new StringBuffer("<table cellspacing=5 width=100%>");
                for(Number inbound:n.getSortedInbound()) {
                    numberSB.append("<tr valign=top><td width=10%>");
                    String searchUrl =
                        HtmlUtils.url(request.url(getRepository().URL_ENTRY_SHOW),
                                      new String[] {
                                          ARG_ENTRYID, entry.getId(), ARG_DB_SEARCH, "true", 
                                          fromNumberColumn.getFullName(), n.number,
                                          toNumberColumn.getFullName(), inbound.number,
                                      });

                    numberSB.append(HtmlUtils.href(searchUrl, formatNumber(inbound.number)));
                    numberSB.append("</td>");

                    List<Object[]> calls = n.getInboundCalls(inbound);
                    StringBuffer callSB  = new StringBuffer();
                    for(Object[]values: calls) {
                        StringBuffer dateSB = new StringBuffer();
                        dateColumn.formatValue(entry, dateSB, Column.OUTPUT_HTML, values, sdf);
                        StringBuffer html = new StringBuffer();
                        getHtml(request, html,entry,values);
                        callSB.append(HtmlUtils.makeShowHideBlock(dateSB.toString(), HtmlUtils.insetLeft(HtmlUtils.div(html.toString(),HtmlUtils.cssClass("ramadda-popup-box")), 10), false));
                    }
                    numberSB.append("<td width=5% align=right>");
                    numberSB.append(calls.size());
                    numberSB.append("</td><td width=85%>");
                    numberSB.append(HtmlUtils.makeShowHideBlock("Details", HtmlUtils.insetLeft(callSB.toString(),10), false));
                    numberSB.append("</td></tr>");
                }
                numberSB.append("</table>");
                sb.append(HtmlUtils.makeShowHideBlock("Inbound", HtmlUtils.insetLeft(numberSB.toString(), 10), true));
                sb.append(HtmlUtils.close(HtmlUtils.TAG_DIV));
            }





        }
        return new Result(getTitle(), sb);
    }



    /**
     */
    @Override
    public String getMapLabel(Entry entry, Object[] values, SimpleDateFormat sdf)
        throws Exception {
        StringBuffer sb = new StringBuffer();
        dateColumn.formatValue(entry, sb, Column.OUTPUT_HTML, values, sdf);
        sb.append(": ");
        sb.append(formatNumber(fromNumberColumn.getString(values)));
        sb.append(ARROW);
        sb.append(formatNumber(toNumberColumn.getString(values)));
        return sb.toString();
    }


    @Override
    public String getKmlLabel(Entry entry, Object[] values, SimpleDateFormat sdf)
        throws Exception {
        StringBuffer sb = new StringBuffer();
        //        dateColumn.formatValue(entry, sb, Column.OUTPUT_HTML, values, sdf);
        //        sb.append(": ");
        sb.append(formatNumber(fromNumberColumn.getString(values)));
        sb.append(" - ");
        sb.append(formatNumber(toNumberColumn.getString(values)));
        return sb.toString();
    }



    @Override
        public String getDefaultDateFormatString() {
        return  "yyyy/MM/dd HH:mm z";
    }

    @Override
        public String getCalendarLabel(Entry entry, Object[] values, SimpleDateFormat sdf)
        throws Exception {
        SimpleDateFormat timesdf = new SimpleDateFormat("HH:mm");
        timesdf.setTimeZone(sdf.getTimeZone());
        StringBuffer sb = new StringBuffer();
        dateColumn.formatValue(entry, sb, Column.OUTPUT_HTML, values, timesdf);
        sb.append(": ");
        sb.append(formatNumber(fromNumberColumn.getString(values)));
        sb.append(ARROW);
        sb.append(formatNumber(toNumberColumn.getString(values)));
        return sb.toString();
    }



    @Override
        public void createBulkForm(Request request, Entry entry, StringBuffer sb, StringBuffer formBuffer) {
        StringBuffer bulkSB = new StringBuffer();
        makeForm(request, entry, bulkSB);
        StringBuffer bulkButtons = new StringBuffer();
        bulkButtons.append(HtmlUtils.submit(msg("Create entries"),
                                            ARG_DB_CREATE));
        bulkButtons.append(HtmlUtils.submit(msg("Cancel"), ARG_DB_LIST));
        bulkSB.append(HtmlUtils.p());
        bulkSB.append(header("Upload a call log file"));
        bulkSB.append(HtmlUtils.p());
        bulkSB.append(msgLabel("File"));
        bulkSB.append(HtmlUtils.fileInput(ARG_DB_BULK_FILE,
                                          HtmlUtils.SIZE_60));

        bulkSB.append(HtmlUtils.p());
        bulkSB.append(msgLabel("Enter names for the numbers"));
        bulkSB.append(HtmlUtils.br());
        bulkSB.append(HtmlUtils.textArea(ARG_DB_NAMES,"#number=name\n#e.g.:\n#303-555-1212= some name\n", 8, 50));
        bulkSB.append(HtmlUtils.br());
        bulkSB.append(bulkButtons);
        bulkSB.append(HtmlUtils.formClose());
        sb.append(bulkSB);
    }



    public String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        return sdf.format(date);
    }


    public static void  main(String []args) throws Exception {
        /*
          Hashtable<String,CellSite>   sites = CellSite.getSites(CellSite.CARRIER_VERIZON);
          for(String callFile: args) {
          String delimiter = "\r";
          String contents = IOUtil.readContents(callFile, CellSite.class);
          }
        */
    }

    public static class Number implements Comparable {
        String number;
        int count = 0;
        Hashtable<Number,List<Object[]>> outboundCalls  = new Hashtable<Number,List<Object[]>>();
        Hashtable<Number,List<Object[]>> inboundCalls  = new Hashtable<Number,List<Object[]>>();

        List<Number>outbound = new ArrayList<Number>();
        List<Number>inbound = new ArrayList<Number>();


        public Number(String number) {
            this.number= number;
        }
        
        public void addOutbound(Number n, Object[]tuple) {
            count++;
            List<Object[]> calls = outboundCalls.get(n);
            if(calls==null) {
                outbound.add(n);
                outboundCalls.put(n,calls = new ArrayList<Object[]>());
            }
            calls.add(tuple);
        }

        public void addInbound(Number n, Object[]tuple) {
            count++;
            List<Object[]> calls = inboundCalls.get(n);
            if(calls==null) {
                inbound.add(n);
                inboundCalls.put(n,calls = new ArrayList<Object[]>());
            }
            calls.add(tuple);
        }



        public List<Object[]> getOutboundCalls(Number n) {
            return outboundCalls.get(n);
        }
        public List<Object[]> getInboundCalls(Number n) {
            return inboundCalls.get(n);
        }

        public int getOutboundCount(Number n) {
            return outboundCalls.get(n).size();
        }

        public int getInboundCount(Number n) {
            return inboundCalls.get(n).size();
        }

        public List<Number> getSortedOutbound() {
            Comparator comp = new Comparator() {
                    public int compare(Object o1, Object o2) {
                        Number e1 = (Number) o1;
                        Number e2 = (Number) o2;
                        int c1 = getOutboundCount(e1);
                        int c2 = getOutboundCount(e2);
                        if (c1 < c2) {
                            return 1;
                        }
                        if (c1 > c2) {
                            return -1;
                        }
                        return 0;
                    }
                };
            Object[] array = outbound.toArray();
            Arrays.sort(array, comp);
            return (List<Number>) Misc.toList(array);
        }

        public List<Number> getSortedInbound() {
            Comparator comp = new Comparator() {
                    public int compare(Object o1, Object o2) {
                        Number e1 = (Number) o1;
                        Number e2 = (Number) o2;
                        int c1 = getInboundCount(e1);
                        int c2 = getInboundCount(e2);
                        if (c1 < c2) {
                            return 1;
                        }
                        if (c1 > c2) {
                            return -1;
                        }
                        return 0;
                    }
                };
            Object[] array = inbound.toArray();
            Arrays.sort(array, comp);
            return (List<Number>) Misc.toList(array);
        }



        public int compareTo(Object o) {
            Number that = (Number) o;
            if(this.count<that.count) return  1;
            if(this.count>that.count) return  -1;
            return 0;
        }

        @Override
            public int hashCode() {
            return number.hashCode();
        }

        @Override
            public boolean equals(Object o) {
            if(!(o instanceof Number)) return false;
            return number.equals(((Number)o).number);
        }
    }




}
