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



import org.ramadda.repository.*;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;


import org.ramadda.repository.type.*;
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



import org.ramadda.util.Utils;
import org.apache.commons.lang.text.StrTokenizer;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;



/**
 *
 */

public class CellPhoneDbTypeHandler extends DbTypeHandler {

    public static final String ARG_FILE_TYPE = "filetype";

    public static final String TYPE_VERIZON_V1 = CellSite.CARRIER_VERIZON+"." + "v1";


    public static final String VIEW_CALL_LISTING = "call.listing";

    public static final int IDX_FROM = 0;
    public static final int IDX_TO = 1;
    public static final int IDX_DATE = 2;
    public static final int IDX_MINUTES = 3;
    public static final int IDX_DIRECTION = 4;
    public static final int IDX_LOCATION = 5;
    public static final int IDX_ADDRESS = 6;
    public static final int IDX_CITY = 7;
    public static final int IDX_STATE = 8;
    public static final int IDX_ZIPCODE = 9;


    private Column fromColumn;
    private    Column toColumn;
    private Column dateColumn;


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
    public CellPhoneDbTypeHandler(DbAdminHandler dbAdmin,
                                  Repository repository, String tableName,
                                  Element tableNode, String desc)
        throws Exception {
        super(dbAdmin, repository, tableName, tableNode, desc);
    }




    public void init(List<Element> columnNodes) throws Exception {
        super.init(columnNodes);
        viewList.add(1, new TwoFacedObject("Call Listing", VIEW_CALL_LISTING));
        fromColumn =  columnsToUse.get(IDX_FROM);
        toColumn =  columnsToUse.get(IDX_TO);
        dateColumn =  columnsToUse.get(IDX_DATE);
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
    }

@Override
    public void formatTableValue(Request request, Entry entry, StringBuffer sb, Column column, Object[]values, SimpleDateFormat sdf) throws Exception {
        if(column.equals(fromColumn) || column.equals(toColumn)) {
            sb.append(formatNumber(column.getString(values)));
        } else {
            super.formatTableValue(request, entry,  sb, column, values,sdf);
        }
    }



    private String formatNumber(String n) {
        n = n.trim();
        if(n.length()!=10) return n;
        return n.substring(0,3) +"-" + n.substring(3,6) +"-" +n.substring(6);
    }

    public Result makeListResults(Request request, Entry entry, String view,
                                  String action, boolean fromSearch,
                                  List<Object[]> valueList)
            throws Exception {
        if (view.equals(VIEW_CALL_LISTING)) {
            return handleListing(request, entry, valueList, fromSearch);
        }
        return  super.makeListResults(request,  entry,  view,
                                action,  fromSearch,
                                valueList);
    }


    private static class Number implements Comparable {
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

    public Result handleListing(Request request, Entry entry,
                                List<Object[]> valueList, boolean fromSearch)
            throws Exception {

        Hashtable<String,Number> numberMap = new Hashtable<String,Number>();
        List<Number> numbers = new ArrayList<Number>();
        StringBuffer sb         = new StringBuffer();
        addViewHeader(request, entry, sb, VIEW_CALL_LISTING, valueList.size(),
                      fromSearch);
        
        for(Object[] tuple : valueList) {
            String fromNumber = fromColumn.getString(tuple);
            Number from = numberMap.get(fromNumber);
            if(from==null)  {
                from = new Number(fromNumber);
                numbers.add(from);
                numberMap.put(from.number, from);
            }
            String toNumber = toColumn.getString(tuple);
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

        Collections.sort(numbers);
        for(Number n: numbers) {
            //Only show numbers with outbounds
            if(n.outbound.size()<=1 && n.inbound.size()<=1) continue;
            sb.append(HtmlUtils.div(formatNumber(n.number), HtmlUtils.cssClass("ramadda-heading-1")));

            if(n.outbound.size()>0) { 
                sb.append("<div style=\"margin-top:0px;margin-left:20px;\"><b>Outbound:</b><br><table cellspacing=5 width=100%>");
                for(Number outbound:n.getSortedOutbound()) {
                    sb.append("<tr valign=top><td width=10%>");
                    String searchUrl =
                        HtmlUtils.url(request.url(getRepository().URL_ENTRY_SHOW),
                                      new String[] {
                                          ARG_ENTRYID, entry.getId(), ARG_DB_SEARCH, "true", 
                                          fromColumn.getFullName(), n.number,
                                          toColumn.getFullName(), outbound.number,
                                      });

                    sb.append(HtmlUtils.href(searchUrl, formatNumber(outbound.number)));
                    sb.append("</td>");

                    List<Object[]> calls = n.getOutboundCalls(outbound);
                    StringBuffer callSB  = new StringBuffer();
                    for(Object[]values: calls) {
                        String date = dateColumn.getString(values);
                        StringBuffer html = new StringBuffer();
                        getHtml(request, html,entry,values);
                        callSB.append(HtmlUtils.makeShowHideBlock(date, HtmlUtils.insetLeft(HtmlUtils.div(html.toString(),HtmlUtils.cssClass("ramadda-popup-box")), 10), false));
                    }
                    sb.append("<td width=5% align=right>");
                    sb.append(calls.size());
                    sb.append("</td><td width=85%>");
                    sb.append(HtmlUtils.makeShowHideBlock("Details", HtmlUtils.insetLeft(callSB.toString(),10), false));
                    sb.append("</td></tr>");
                }
                sb.append("</table></div>");
            }

            if(n.inbound.size()>0) { 
                sb.append("<div style=\"margin-top:0px;margin-left:20px;\"><b>Inbound:</b><br><table cellspacing=5 width=100%>");
                for(Number inbound:n.getSortedInbound()) {
                    sb.append("<tr valign=top><td width=10%>");
                    String searchUrl =
                        HtmlUtils.url(request.url(getRepository().URL_ENTRY_SHOW),
                                      new String[] {
                                          ARG_ENTRYID, entry.getId(), ARG_DB_SEARCH, "true", 
                                          fromColumn.getFullName(), n.number,
                                          toColumn.getFullName(), inbound.number,
                                      });

                    sb.append(HtmlUtils.href(searchUrl, formatNumber(inbound.number)));
                    sb.append("</td>");

                    List<Object[]> calls = n.getInboundCalls(inbound);
                    StringBuffer callSB  = new StringBuffer();
                    for(Object[]values: calls) {
                        String date = dateColumn.getString(values);
                        StringBuffer html = new StringBuffer();
                        getHtml(request, html,entry,values);
                        callSB.append(HtmlUtils.makeShowHideBlock(date, HtmlUtils.insetLeft(HtmlUtils.div(html.toString(),HtmlUtils.cssClass("ramadda-popup-box")), 10), false));
                    }
                    sb.append("<td width=5% align=right>");
                    sb.append(calls.size());
                    sb.append("</td><td width=85%>");
                    sb.append(HtmlUtils.makeShowHideBlock("Details", HtmlUtils.insetLeft(callSB.toString(),10), false));
                    sb.append("</td></tr>");
                }
                sb.append("</table></div>");
            }




        }
        return new Result(getTitle(), sb);
    }


    @Override
    public String getMapIcon(Request request, Entry entry) {
        return getRepository().getUrlBase() + "/case/building.png";
    } 


    /**
     */
    @Override
     public String getMapLabel(Entry entry, Object[] values, SimpleDateFormat sdf)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        dateColumn.formatValue(entry, sb, Column.OUTPUT_HTML, values, sdf);
        sb.append(" -- ");
        fromColumn.formatValue(entry, sb, Column.OUTPUT_HTML, values);
        sb.append(" -&gt; ");
        toColumn.formatValue(entry, sb, Column.OUTPUT_HTML, values);
        return sb.toString();
    }



    @Override
    public String getCalendarLabel(Entry entry, Object[] values, SimpleDateFormat sdf)
            throws Exception {
        SimpleDateFormat timesdf = new SimpleDateFormat("HH:mm");
        timesdf.setTimeZone(sdf.getTimeZone());
        StringBuffer sb = new StringBuffer();
        dateColumn.formatValue(entry, sb, Column.OUTPUT_HTML, values, timesdf);
        sb.append(": ");
        fromColumn.formatValue(entry, sb, Column.OUTPUT_HTML, values);
        sb.append(" -&gt; ");
        toColumn.formatValue(entry, sb, Column.OUTPUT_HTML, values);

        return sb.toString();
    }


    @Override
    public void addToBulkUploadForm(Request request, StringBuffer bulk) {
        super.addToBulkUploadForm(request,  bulk);
        //TODO: add file type list when we have more than one type
        //        bulk.append
    }


    public Result handleBulkUpload(Request request, Entry entry, String contents)
        throws Exception {
        StringBuffer msg = new StringBuffer();

        String fileType = request.getString(ARG_FILE_TYPE, TYPE_VERIZON_V1);
        String carrier = null;
        if(fileType.startsWith(CellSite.CARRIER_VERIZON)) {
            carrier = CellSite.CARRIER_VERIZON;
        } else {
            throw new IllegalArgumentException("Unknown file type:" + fileType);
        }

        File sitesDir = new File(getStorageManager().getResourceDir() +"/investigation/sites");
        Hashtable<String,CellSite>   sites = CellSite.getSites(sitesDir, carrier);
        List<Object[]> valueList = new ArrayList<Object[]>();
        for(List<String>toks: tokenize(request, fileType, contents)) {
            String[] fields = getFields(request, fileType, sites, toks, msg);
            if(fields==null) continue;
            Object[]     values = tableHandler.makeEntryValueArray();
            initializeValueArray(request, null, values);
            for (int colIdx = 0; colIdx < fields.length; colIdx++) {
                Column column = columnsToUse.get(colIdx);
                String value  = fields[colIdx].trim();
                value = value.replaceAll("_COMMA_", ",");
                value = value.replaceAll("_NEWLINE_", "\n");
                column.setValue(entry, values, value);
            }
            valueList.add(values);
        }

        for (Object[] tuple : valueList) {
            doStore(entry, tuple, true);
        }
        //Remove these so any links that get made with the request don't point to the BULK upload
        request.remove(ARG_DB_NEWFORM);
        request.remove(ARG_DB_BULK_TEXT);
        request.remove(ARG_DB_BULK_FILE);
        StringBuffer sb = new StringBuffer();
        if(msg.length()>0) {
            sb.append(HtmlUtils.b("Errors:"));
            sb.append(HtmlUtils.div(msg.toString(),HtmlUtils.cssClass("browseblock")));
        }
        return handleListTable(request, entry, valueList, false, false,sb);
    }


    private List<List<String>>  tokenize(Request request, String fileType, String contents) throws Exception {
        if(fileType.equals(TYPE_VERIZON_V1)) {
            return Utils.tokenize(contents, "\r", ",",1);
        }
        throw new IllegalArgumentException("Unknown file type:" + fileType);
    }

    private String[]  getFields(Request request, String fileType, Hashtable<String,CellSite>   sites,
                                List<String>toks, StringBuffer msg) throws Exception {
        if(fileType.equals(TYPE_VERIZON_V1)) {
            return getVerizonFields(sites,toks, msg);
        }
        throw new IllegalArgumentException("Unknown file type:" + fileType);
    }


    private String[]  getVerizonFields(Hashtable<String,CellSite>   sites,
                                       List<String>toks, StringBuffer msg) throws Exception {
        if(toks.size()!=11) {
            msg.append("wrong number of tokens:" + StringUtil.join(",",toks)+"<br>");
            return null;
        }
        CellSite site =  sites.get(toks.get(6));
        if(site==null) {
            msg.append("No location for site:" +toks.get(6) +"\nline:" + StringUtil.join(",", toks)+"<br>");
            return null;
        } 
        //Network Element Name 0
        //Mobile Directory Number 1
        //Dialed Digit Number 2
        //Call Direction 3
        //Seizure Dt Tm 4 
        //Seizure Duration 5
        //First Serving Cell Site 6
        //First Serving Cell Face 7
        //Last Serving Cell Site
        //Last Serving Cell Face
        //Calling Party Number

        boolean outbound = true;
        String  tmpDirection = toks.get(3).trim();
        String  direction = "";
        String from = toks.get(10);
        String to = toks.get(2);
        if(tmpDirection.equals("0") || 
           tmpDirection.equals("6")) {
            direction = "inbound";
            outbound = false;
        } else if(tmpDirection.equals("1") || 
                  tmpDirection.equals("3")) {
            direction = "outbound";
        } else if(tmpDirection.equals("F")) {
            direction = "voice";
            to = "voice";
        } else if(tmpDirection.equals("2")) {
            direction = "mobiletomobile";
        }  else  {
            return null;
        }

        //Pleasanton_2,7076715244,7076715244,6,8/17/2011 15:25,58,335,2,335,2,4439263852
        //0           , 1         , 2       ,3,4              ,5 ,6  ,7,8  ,9,10

        String time = toks.get(4);
        //duration is in seconds in the file but minutes in the db
        int seconds = Integer.parseInt(toks.get(5));
        DecimalFormat minutesFormat = new DecimalFormat("##0.00");
        double minutes = seconds/60.0;

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Date date = sdf.parse(time);

        String[] fields = new String[]{
            from,
            to,
            formatDate(date), 
            minutesFormat.format(minutes),
            direction,
            site.getLatitude()+";" + site.getLongitude(), site.getAddress(),
            site.getCity(),
            site.getState(),
            site.getZipCode(),
            "default",
            ""
        };

        return fields;
    }

    private String formatDate(Date date) {
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


}
