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

package org.ramadda.plugins.mail;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.IOUtil;
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
public class MailHarvester extends Harvester {

    /** _more_          */

    public static final String ATTR_IMAP_URL = "imapurl";
    public static final String ATTR_FROM = "from";
    public static final String ATTR_SUBJECT = "subject";
    public static final String ATTR_BODY = "body";

    public static final String ATTR_RESPONSE = "response";


    private String imapUrl;


    /** _more_          */
    private String from;

    /** _more_          */
    private String subject;

    /** _more_          */
    private String body;

    /** _more_          */
    private String response;




    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public MailHarvester(Repository repository, String id) throws Exception {
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
    public MailHarvester(Repository repository, Element node)
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
        imapUrl = XmlUtil.getAttribute(element, ATTR_IMAP_URL, imapUrl);
        from = XmlUtil.getAttribute(element, ATTR_FROM, from);
        subject = XmlUtil.getAttribute(element, ATTR_SUBJECT, subject);
        body= XmlUtil.getAttribute(element, ATTR_BODY, body);
        response  = XmlUtil.getAttribute(element, ATTR_RESPONSE, response);
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
    public void checkEmail() throws Exception {
        /*
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


        if(passCode!=null && passCode.length()>0) {
            if(message.indexOf(passCode)<0) {
                returnMsg.append("Message does not contain passcode");
                return false;
            }
            message = message.replace(passCode,"");
        }

        message = message.trim();
        if(message.equals("help") || message.equals("?")) {
            returnMsg.append("Use:\nname &lt;entry name&gt;\ntype &lt;wiki or note&gt;");
            return true;
        }

        String      type = "phone_sms";
        String tmp;
        StringBuffer desc = new StringBuffer();
        int lineCnt = 0;
        for(String line: StringUtil.split(message,"\n")) {
            String tline = line.trim();
            boolean skipLine = false;

            for(String prefix: new String[]{
                    "title","Title","name","Name","nm","Nm"
                }) {
                if((tmp =  StringUtil.findPattern(tline,prefix+"\\s(.+)"))!=null) {
                    name = tmp;
                    skipLine = true;
                    break;
                }
            }

            if(skipLine) continue;

            if(tline.equalsIgnoreCase("wiki")) {
                type = "wikipage";
                continue;
            }
            if(tline.equalsIgnoreCase("note")) {
                type = "notes_note";
                continue;
            }

            if((tmp =  StringUtil.findPattern(tline,"type\\s(.+)"))!=null) {
                tmp = tmp.trim();
                if(tmp.equals("note")) {
                    type = "notes_note";
                } else if(tmp.equals("wiki")) {
                    type = "wikipage";
                } else {
                    returnMsg.append("Unknown type:" + tmp);
                    //                    return false;
                }
                continue;
            }
            if(lineCnt!=0)
                desc.append("<br>");
            desc.append(line);
            lineCnt++;
        }


        TypeHandler typeHandler = getRepository().getTypeHandler(type);
        Entry       entry = typeHandler.createEntry(getRepository().getGUID());


        File  voiceFile = fetchVoiceFile(request, new URL(info.getRecordingUrl()));
        if(voiceFile==null) {
            return false;
        }
        voiceFile =     getStorageManager().moveToStorage(request, voiceFile);
        Resource resource = new Resource(voiceFile.toString(), Resource.TYPE_STOREDFILE);



        Date        date        = new Date();
        Object[]    values      = typeHandler.makeValues(new Hashtable());
        if(type.equals("phone_sms")) {
            values[0] = info.getFromPhone();
            values[1] = info.getToPhone();
        } else if (type.equals("wikipage")) {
            values[0] = desc.toString();
            desc = new StringBuffer();
        }
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
        String template = response;
        if(template == null || template.trim().length()==0) template = "Entry created:\n${url}";
        template = template.replace("${url}", entryUrl);
        returnMsg.append(template);
        return true;
        */

    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return "Email Harvester";
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
        element.setAttribute(ATTR_IMAP_URL, imapUrl);
        element.setAttribute(ATTR_FROM, from);
        element.setAttribute(ATTR_SUBJECT, subject);
        element.setAttribute(ATTR_BODY, body);
        element.setAttribute(ATTR_RESPONSE, response);
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
        imapUrl = request.getString(ATTR_IMAP_URL, imapUrl);
        from = request.getString(ATTR_FROM, from);
        subject   = request.getString(ATTR_SUBJECT, subject);
        body   = request.getString(ATTR_BODY, body);
        response  = request.getString(ATTR_RESPONSE, response);
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
        


        sb.append(HtmlUtils.formEntry(msgLabel("Email URL"),
                                      HtmlUtils.input(ATTR_IMAP_URL,
                                                      imapUrl, HtmlUtils.SIZE_60)
                                      + " " + "e.g. <i>imaps://&lt;email address&gt;:&lt;email password&gt;@&gt;email server&gt;</i>"));


        sb.append(HtmlUtils.formEntry(msgLabel("Sender Contains"),
                                      HtmlUtils.input(ATTR_FROM,
                                                      from, HtmlUtils.SIZE_60)));
        sb.append(HtmlUtils.formEntry(msgLabel("Subject Contains"),
                                      HtmlUtils.input(ATTR_SUBJECT, subject,
                                          HtmlUtils.SIZE_60)));
        sb.append(HtmlUtils.formEntry(msgLabel("Body Contains"),
                                      HtmlUtils.input(ATTR_BODY, body,
                                          HtmlUtils.SIZE_60)));
        sb.append(HtmlUtils.formEntryTop(msgLabel("Email Response"),
                                      HtmlUtils.textArea(ATTR_RESPONSE,
                                                         response==null?"":response,5,60) +"<br>" + "Use ${url} for the URL to the created entry"));

    }



    /**
     * _more_
     *
     *
     * @param timestamp _more_
     * @throws Exception _more_
     */
    protected void runInner(int timestamp) throws Exception {
        while (canContinueRunning(timestamp)) {
            long t1 = System.currentTimeMillis();
            logHarvesterInfo("Checking email");
            checkEmail();
            if ( !getMonitor()) {
                logHarvesterInfo("Ran one time only. Exiting loop");
                break;
            }

            logHarvesterInfo("Sleeping for " + getSleepMinutes()
                             + " minutes");
            doPause();
        }
    }



    private File fetchVoiceFile(Request request, URL url) throws Exception {
        String tail    = "voicemessage.wav";
        File   newFile = getStorageManager().getTmpFile(request,
                                                        tail);
        URLConnection connection = url.openConnection();
        InputStream   fromStream = connection.getInputStream();
        FileOutputStream toStream =
            getStorageManager().getFileOutputStream(newFile);
        try {
            int bytes = IOUtil.writeTo(fromStream, toStream);
            if (bytes < 0) {
                System.err.println("PhoneHarvester: failed to read voice URL:" + url);
                return null;
            }
        } finally {
            IOUtil.close(toStream);
            IOUtil.close(fromStream);
        }
        return newFile;
    }


}
