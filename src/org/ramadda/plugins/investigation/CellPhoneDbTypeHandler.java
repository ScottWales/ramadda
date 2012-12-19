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

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;



/**
 *
 */

public class CellPhoneDbTypeHandler extends PhoneDbTypeHandler {

    public static final String ARG_FILE_TYPE = "filetype";


    public static final String TYPE_VERIZON_V1 = CellSite.CARRIER_VERIZON+"." + "v1";


    public static final int IDX_FROM_NAME = 0;
    public static final int IDX_FROM_NUMBER = 1;
    public static final int IDX_TO_NAME = 2;
    public static final int IDX_TO_NUMBER = 3;
    public static final int IDX_DATE = 4;
    public static final int IDX_MINUTES = 5;
    public static final int IDX_DIRECTION = 6;
    public static final int IDX_LOCATION = 7;
    public static final int IDX_ADDRESS = 8;
    public static final int IDX_CITY = 9;
    public static final int IDX_STATE = 10;
    public static final int IDX_ZIPCODE = 11;


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
        fromNameColumn =  columnsToUse.get(IDX_FROM_NAME);
        fromNumberColumn =  columnsToUse.get(IDX_FROM_NUMBER);
        toNameColumn =  columnsToUse.get(IDX_TO_NAME);
        toNumberColumn =  columnsToUse.get(IDX_TO_NUMBER);
        dateColumn =  columnsToUse.get(IDX_DATE);
    }

    @Override
        public String getMapIcon(Request request, Entry entry) {
        return getRepository().getUrlBase() + "/investigation/building.png";
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


    public String cleanUpNumber(String number) {
        return  number.replaceAll("-","").trim();
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
            return getVerizonFields(request, sites,toks, msg);
        }
        throw new IllegalArgumentException("Unknown file type:" + fileType);
    }


    private String[]  getVerizonFields(Request request, Hashtable<String,CellSite>   sites,
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

        Hashtable<String,String> numberToName = new Hashtable<String,String>();
        for(String line: StringUtil.split(request.getString(ARG_DB_NAMES,""), "\n", true, true)) {
            line  = line.trim();
            if(line.startsWith("#")) continue;
            List<String> numberAndName =  StringUtil.splitUpTo(line,"=", 2);
            if(numberAndName.size()<2) continue;
            String number = numberAndName.get(0);
            String name = numberAndName.get(1);
            number = number.replaceAll("-","").trim();
            numberToName.put(number, name);
        }


        boolean outbound = true;
        String  tmpDirection = toks.get(3).trim();
        String  direction = "";

        String fromNumber = toks.get(10);
        String toNumber = toks.get(2);
        String fromName = numberToName.get(fromNumber);
        String toName = numberToName.get(toNumber);
        if(fromName == null) fromName = "";
        if(toName == null) toName = "";
        if(tmpDirection.equals("0") || 
           tmpDirection.equals("6")) {
            direction = "inbound";
            outbound = false;
        } else if(tmpDirection.equals("1") || 
                  tmpDirection.equals("3")) {
            direction = "outbound";
        } else if(tmpDirection.equals("F")) {
            direction = "voice";
            toNumber = "voice";
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
            fromName,
            fromNumber,
            toName,
            toNumber,
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



}
