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

package org.ramadda.plugins.phone;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import ucar.unidata.xml.XmlUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;



import java.net.*;



import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



/**
 */
public class PhoneHarvester extends Harvester {

    /** _more_          */
    public static final String ATTR_TYPE = "type";

    /** _more_          */
    public static final String ATTR_FROMPHONE = "fromphone";

    /** _more_          */
    public static final String ATTR_TOPHONE = "tophone";

    /** _more_          */
    public static final String ATTR_PASSCODE = "passcode";

    /** _more_          */
    public static final String ATTR_ = "";


    /** _more_          */
    private String type = PhoneInfo.TYPE_SMS;

    /** _more_          */
    private String fromPhone;

    /** _more_          */
    private String toPhone;

    /** _more_          */
    private String passCode;


    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public PhoneHarvester(Repository repository, String id) throws Exception {
        super(repository, id);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     *
     * @throws Exception _more_
     */
    public PhoneHarvester(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }



    /**
     * _more_
     *
     * @param element _more_
     *
     * @throws Exception _more_
     */
    protected void init(Element element) throws Exception {
        super.init(element);
        fromPhone = XmlUtil.getAttribute(element, ATTR_FROMPHONE, fromPhone);
        toPhone   = XmlUtil.getAttribute(element, ATTR_TOPHONE, toPhone);
        passCode  = XmlUtil.getAttribute(element, ATTR_PASSCODE, passCode);
        type      = XmlUtil.getAttribute(element, ATTR_TYPE, type);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param info _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean handleMessage(Request request, PhoneInfo info, StringBuffer returnMsg)
            throws Exception {

        System.err.println ("handleMessage:" + fromPhone +":" +info.getFromPhone() +": to phone:" + toPhone +":" +
                            info.getToPhone());
        if (fromPhone!=null && fromPhone.length() > 0) {
            if ( info.getFromPhone().indexOf(fromPhone)<0) {
                System.err.println ("handleMessage: skipping wrong from phone");
                return false;
            }
        }

        if (toPhone!= null && toPhone.length() > 0) {
            if ( info.getToPhone().indexOf(toPhone)<0) {
                System.err.println ("handleMessage: skipping wrong to phone");
                return false;
            }
        }

        Entry       baseGroup   = getBaseGroup();
        Entry       parent      = baseGroup;
        String      message        = info.getMessage();
        String      name        = "SMS Message";
        int spaceIndex = message.indexOf(" ", 10);
        if(spaceIndex<0) {
            spaceIndex = message.indexOf("\n");
        }
        if(spaceIndex>0) {
            name = message.substring(0, spaceIndex);
        } else {
            //??
        }

        System.err.println("msg:" + message);
        if(passCode!=null && passCode.length()>0) {
            if(message.indexOf(passCode)<0) {
                returnMsg.append("Message does not contain passcode");
                return false;
            }
            message = message.replace(passCode,"");
        }


        String      type = "phone_sms";
        
        String tmp;

        StringBuffer desc = new StringBuffer();
        int lineCnt = 0;
        for(String line: StringUtil.split(message,"\n")) {
            String tline = line.trim();
            System.err.println ("SMS: line:" + tline); 
            if((tmp =  StringUtil.findPattern(tline,"title\\s(.+)"))!=null) {
                name = tmp;
                continue;
            }
            if((tmp =  StringUtil.findPattern(tline,"name\\s(.+)"))!=null) {
                name = tmp;
                continue;
            }
            if((tmp =  StringUtil.findPattern(tline,"nm\\s(.+)"))!=null) {
                name = tmp;
                continue;
            }
            if(lineCnt!=0)
                desc.append("<br>");
            desc.append(line);
            lineCnt++;
        }


        TypeHandler typeHandler = getRepository().getTypeHandler(type);
        Entry       entry = typeHandler.createEntry(getRepository().getGUID());


        Date        date        = new Date();
        Object[]    values      = typeHandler.makeValues(new Hashtable());
        values[0] = info.getFromPhone();
        values[1] = info.getToPhone();
        entry.initEntry(name, desc.toString(), parent, getUser(), new Resource(), "",
                        date.getTime(), date.getTime(), date.getTime(),
                        date.getTime(), values);


        double[] location = org.ramadda.util.GeoUtils.getLocationFromAddress(
                                info.getFromZip());
        if (location != null) {
            entry.setLocation(location[0], location[1], 0);
        }

        List<Entry> entries = (List<Entry>) Misc.newList(entry);
        getEntryManager().insertEntries(entries, true, true);

        String entryUrl = 
            HtmlUtils.url(getRepository().URL_ENTRY_SHOW.getFullUrl(),
                          ARG_ENTRYID, entry.getId());
        returnMsg.append("Entry created:\n"+ entryUrl);
        return true;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return "Phone and SMS Harvester";
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     */
    public void makeRunSettings(Request request, StringBuffer sb) {
        StringBuffer runWidgets = new StringBuffer();
        runWidgets.append(
            HtmlUtils.checkbox(
                ATTR_ACTIVEONSTART, "true",
                getActiveOnStart()) + HtmlUtils.space(1) + msg("Active"));
        sb.append(HtmlUtils.formEntryTop("", runWidgets.toString()));
    }

    /**
     * _more_
     *
     * @param element _more_
     *
     * @throws Exception _more_
     */
    public void applyState(Element element) throws Exception {
        super.applyState(element);
        element.setAttribute(ATTR_FROMPHONE, fromPhone);
        element.setAttribute(ATTR_TOPHONE, toPhone);
        element.setAttribute(ATTR_PASSCODE, passCode);
        element.setAttribute(ATTR_TYPE, type);
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void applyEditForm(Request request) throws Exception {
        super.applyEditForm(request);
        fromPhone = request.getString(ATTR_FROMPHONE, fromPhone);
        toPhone   = request.getString(ATTR_TOPHONE, toPhone);
        passCode  = request.getString(ATTR_PASSCODE, passCode);
        type      = request.getString(ATTR_TYPE, type);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void createEditForm(Request request, StringBuffer sb)
            throws Exception {
        super.createEditForm(request, sb);
        addBaseGroupSelect(ATTR_BASEGROUP, sb);
        sb.append(HtmlUtils.formEntry(msgLabel("From Phone"),
                                      HtmlUtils.input(ATTR_FROMPHONE,
                                          fromPhone, HtmlUtils.SIZE_60)));
        sb.append(HtmlUtils.formEntry(msgLabel("To Phone"),
                                      HtmlUtils.input(ATTR_TOPHONE, toPhone,
                                          HtmlUtils.SIZE_60)));
        sb.append(HtmlUtils.formEntry(msgLabel("Pass Code"),
                                      HtmlUtils.input(ATTR_PASSCODE,
                                          passCode, HtmlUtils.SIZE_60)));
    }



    /**
     * _more_
     *
     *
     * @param timestamp _more_
     * @throws Exception _more_
     */
    protected void runInner(int timestamp) throws Exception {
        if ( !canContinueRunning(timestamp)) {
            //            return true;
        }
    }


}
