/* * Copyright 2008-2012 Jeff McWhirter/ramadda.org
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
import org.ramadda.util.TTLCache;


import org.w3c.dom.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;

import ucar.unidata.xml.XmlUtil;

import java.io.*;



import java.net.*;



import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;



/**
 */
public class PhoneHarvester extends Harvester  {


    public static final String CMD_LOGOUT = "logout";



    public static final String []CMDS_PASS = {"pass","password","login"};
    public static final String []CMDS_CD = {"cd", "go"};
    public static final String []CMDS_CLEAR = {"clear"};
    public static final String []CMDS_LS = {"ls","list", "dir"};
    public static final String []CMDS_URL = {"url","link"};
    public static final String []CMDS_GET = {"get"};
    public static final String []CMDS_COMMENTS = {"comments"};
    public static final String []CMDS_APPEND = {"append","add"};
    public static final String []CMDS_PWD= {"pwd","where","ur"};

    /** _more_          */
    public static final String ATTR_TYPE = "type";

    /** _more_          */
    public static final String ATTR_FROMPHONE = "fromphone";

    /** _more_          */
    public static final String ATTR_TOPHONE = "tophone";

    /** _more_          */
    public static final String ATTR_PASSWORD_VIEW = "password_view";
    public static final String ATTR_PASSWORD_ADD = "password_add";
    public static final String ATTR_PASSWORD_EDIT = "password_edit";

    public static final String ATTR_RESPONSE = "response";
    public static final String ATTR_VOICEMESSAGE = "voicemessage";

    /** _more_          */
    public static final String ATTR_ = "";


    /** _more_          */
    private String type = PhoneInfo.TYPE_SMS;

    /** _more_          */
    private String fromPhone;

    /** _more_          */
    private String toPhone;

    /** _more_          */
    private String passwordAdd;
    private String passwordView;
    private String passwordEdit;

    /** _more_          */
    private String response;


    /** _more_          */
    private String voiceMessage;

    private Hashtable<String,String> phoneToEntry = new Hashtable<String,String>();

    private TTLCache<String, PhoneSession> sessions = new TTLCache<String,PhoneSession>(24*60 * 60 * 1000);





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
        fromPhone = normalizePhone(XmlUtil.getAttribute(element, ATTR_FROMPHONE, fromPhone));
        toPhone   = normalizePhone(XmlUtil.getAttribute(element, ATTR_TOPHONE, toPhone));
        passwordView  = XmlUtil.getAttribute(element, ATTR_PASSWORD_VIEW, passwordView);
        passwordEdit  = XmlUtil.getAttribute(element, ATTR_PASSWORD_EDIT, passwordEdit);
        passwordAdd  = XmlUtil.getAttribute(element, ATTR_PASSWORD_ADD, passwordAdd);
        response  = XmlUtil.getAttribute(element, ATTR_RESPONSE, response);
        voiceMessage = XmlUtil.getAttribute(element, ATTR_VOICEMESSAGE,  voiceMessage);
        type      = XmlUtil.getAttribute(element, ATTR_TYPE, type);
    }

    private String normalizePhone(String phone) {
        if(phone == null) return null;
        phone = phone.replaceAll(" ","");
        phone = phone.replaceAll("-","");
        return phone;
    }



    private PhoneSession getSession(PhoneInfo info)     {
        PhoneSession  session = sessions.get(info.getFromPhone());        
        if(session==null) {
            session = makeSession(info, "somedummypassword");
        }
        return setSessionState(session);
        
    }
    
          
    public boolean canEdit(PhoneInfo info)     {
        return getSession(info).getCanEdit();
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
    public boolean handleMessage(Request request, PhoneInfo info, StringBuffer msg)
        throws Exception {

        if (fromPhone!=null && fromPhone.length() > 0) {
            if ( info.getFromPhone().indexOf(normalizePhone(fromPhone))<0) {
                //                System.err.println ("handleMessage: skipping wrong from phone");
                return false;
            }
        }

        if (toPhone!= null && toPhone.length() > 0) {
            if ( info.getToPhone().indexOf(toPhone)<0) {
                //                System.err.println ("handleMessage: skipping wrong to phone");
                return false;
            }
        }

        System.err.println ("PhoneHarvester: handleMessage: from:" +info.getFromPhone() +" to:" +info.getToPhone() +" " + info.getMessage());
        Entry       baseGroup   = getBaseGroup();
        Entry currentEntry  = baseGroup;
        String pastEntry = phoneToEntry.get(info.getFromPhone());
        if(pastEntry!=null) {
            Entry entry = getEntryManager().getEntry(request, pastEntry, false);
            if (entry!=null) currentEntry = entry;
        }


        String      message        = info.getMessage();
        String      name        = "SMS Message";
        String toEntryName = null;
        int spaceIndex = message.indexOf(" ", 10);
        if(spaceIndex<0) {
            spaceIndex = message.indexOf("\n");
        }
        if(spaceIndex>0) {
            name = message.substring(0, spaceIndex);
        } else {
            //??
        }




        message = message.trim();


        String      type = null;
        String tmp;
        StringBuffer desc = new StringBuffer();
        boolean doAppend = false;
        boolean processedACommand = false;
        String sessionKey = info.getFromPhone();
        PhoneSession session = getSession(info);
        String lastMessage = session.lastMessage;
        session.lastMessage = message;
       
        if(message.equalsIgnoreCase("knock knock")) {
            msg.append("Who's there?");
            return true;
        }

        if(lastMessage.equalsIgnoreCase("knock knock")) {
            msg.append(message +" who?");
            return true;
        }

        if(message.equals("help") || message.equals("?")) {
            msg.append(getHelp());
            return true;
        }


        for(String line: StringUtil.split(message,"\n")) {
            line = line.trim();
            String tline = line.toLowerCase();

            if(tline.equals(CMD_LOGOUT)) {
                session.logout();
                processedACommand = true;
                continue;
            }


            String remainder;

            if((remainder = checkCommand(CMDS_PASS, line))!=null) {
                session.logout();
                session.password = remainder;
                setSessionState(session);
                if(!session.getCanView()) {
                    msg.append("Bad password\nEnter:\npass <password>");
                    return true;
                }
                processedACommand = true;
                continue;
            }

            if(!session.getCanView()) {
                msg.append("Access is not allowed without a password\nEnter:\npass <password>");
                return true;
            }







            if((remainder = checkCommand(CMDS_LS, line))!=null) {
                int cnt =0;
                for(Entry child: getEntryManager().getChildren(request, currentEntry)) {
                    String childName = child.getName().trim();
                    if(remainder.length()>0) {
                        if(childName.indexOf(remainder)<0) {
                            continue;
                        }
                    }
                    cnt++;
                    msg.append("#" + cnt+" ");
                    if(child.isGroup()) {
                        msg.append(">");
                        //msg.append(str);
                    } else {
                        msg.append(" ");
                    }
                    msg.append(getEntryName(child));
                    msg.append("\n");
                }
                if(msg.length()==0) {
                    msg.append("No entries found");
                }
                msg.append("\n");
                processedACommand = true;
                continue;
            }

            if((remainder = checkCommand(CMDS_CLEAR, line))!=null) {
                if(!session.getCanEdit()) {
                    msg.append("clear is not allowed\nYou need edit access");
                    return true;
                }
                currentEntry =  getEntry(request, remainder, currentEntry, msg);
                if(currentEntry == null) return true;
                currentEntry.setDescription("");
                List<Entry> entries = (List<Entry>) Misc.newList(currentEntry);
                getEntryManager().insertEntries(entries, true, true);
                processedACommand = true;
                continue;
            }



            if((remainder = checkCommand(CMDS_URL, line))!=null) {
                currentEntry =  getEntry(request, remainder, currentEntry, msg);
                if(currentEntry == null) return true;
                msg.append("entry:\n" + getEntryInfo(currentEntry));
                msg.append("\n");
                processedACommand = true;
                continue;
            }

            if((remainder = checkCommand(CMDS_GET, line))!=null) {
                currentEntry =  getEntry(request, remainder, currentEntry, msg);
                if(currentEntry == null) return true;
                String contents  = currentEntry.getDescription();
                contents  = contents.replaceAll("<br>","\n");
                contents  = contents.replaceAll("<p>","\n");
                msg.append(XmlUtil.encodeString(currentEntry.getName() +"\n" + contents.trim()));
                msg.append("\n");
                processedACommand = true;
                continue;
            }

            if((remainder = checkCommand(CMDS_COMMENTS, line))!=null) {
                currentEntry =  getEntry(request, remainder, currentEntry, msg);
                if(currentEntry == null) return true;
                List<org.ramadda.repository.Comment> comments  = getEntryManager().getComments(getRequest(),currentEntry);
                for(org.ramadda.repository.Comment comment: comments) {
                    msg.append(XmlUtil.encodeString("Comment:" + comment.getSubject() +"\n" + comment.getComment()+"\n"));
                }
                if(comments.size()==0) {
                    msg.append("No comments available\n");
                }
                processedACommand = true;
                continue;
            }


            if((remainder = checkCommand(CMDS_PWD, line))!=null) {
                currentEntry =  getEntry(request, remainder, currentEntry, msg);
                int cnt = 0;
                List<Entry> ancestors = new ArrayList<Entry>();
                ancestors.add(currentEntry);
                if(!currentEntry.equals(baseGroup)) {
                    Entry theEntry  = currentEntry;
                    while(ancestors.size()<4) {
                        theEntry = theEntry.getParentEntry();
                        if(theEntry == null) break;
                        ancestors.add(theEntry);
                        if(theEntry.equals(baseGroup)) {
                            break;
                        }
                    }
                }
                String tab = "";
                for(int i=ancestors.size()-1;i>=0;i--) {
                    msg.append(tab);
                    tab = tab + "  ";
                    msg.append(getEntryName(ancestors.get(i)));
                    msg.append("\n");
                }
                msg.append(getEntryUrl(currentEntry));
                msg.append("\n");
                processedACommand = true;
                continue;
            }


            if((remainder = checkCommand(CMDS_CD, line))!=null) {
                if(remainder.length()==0) {
                    currentEntry = baseGroup;
                } else if(remainder.startsWith("/")) {
                    currentEntry =  getEntry(request, remainder,  baseGroup, msg);
                } else if(remainder.startsWith("..")) {
                    boolean haveSeenBaseGroup = false;
                    for(String tok: StringUtil.split(remainder,"/", true, true)) {
                        if(currentEntry.equals(baseGroup)) {
                            haveSeenBaseGroup = true;
                        }
                        if(tok.equals("..")) {
                            if(haveSeenBaseGroup) break;
                            currentEntry =  currentEntry.getParentEntry();
                        } else {
                            Entry childEntry  = getEntryManager().findEntryWithName(request, currentEntry, tok);
                            if(childEntry ==null) {
                                msg.append("Pad path:" + tok);
                                return true;
                            }
                            currentEntry = childEntry;
                        }
                    }
                } else {
                    currentEntry =  getEntry(request, remainder, currentEntry, msg);
                    if(currentEntry == null) return true;
                }
                phoneToEntry.put(info.getFromPhone(), currentEntry.getId());
                processedACommand = true;
                continue;
            }


            if(!session.getCanAdd()) {
                msg.append("No permission to add\nEnter:\npass <password>");
                return true;
            }


            if((remainder = checkCommand(CMDS_APPEND, line))!=null) {
                currentEntry =  getEntry(request, remainder, currentEntry, msg);
                if(currentEntry == null) return true;
                doAppend = true;
                processedACommand = true;
                continue;
            }

            if(type == null) {
                String[]cmds = {"folder","mkdir","wiki","sms","note"};
                String[]types = {TypeHandler.TYPE_GROUP,TypeHandler.TYPE_GROUP,"wikipage","phone_sms","notes_note"};
                boolean didOne = false;
                for(int i=0;i<cmds.length;i++) {
                    if(tline.startsWith(cmds[i] +" ")) {
                        type =types[i] ;
                        name  = line.substring(cmds[i].length()).trim();
                        didOne = true;
                        break;
                    }
                }
                if(didOne) continue;
            }

            if(desc.length()>0)
                desc.append("\n");
            desc.append(line);
        }


        Entry  parent      = currentEntry;
        if(doAppend) {
            //TODO: handle wiki and update the entry better
            if(currentEntry.getTypeHandler().getType().equals("wikipage")) {
                Object[]    values      = currentEntry.getTypeHandler().getValues(currentEntry);
                if(values[0] == null) {
                    values[0] = desc;
                } else {
                    values[0] = values[0] +" " +desc;
                }
            } else {
                currentEntry.setDescription(currentEntry.getDescription() +"\n" + desc);
            }
            getEntryManager().updateEntry(currentEntry);
            msg.append("entry appended to\n" + getEntryInfo(currentEntry));
            return true;
        }
        if(type == null) {
            if(!processedACommand) {
                msg.append("No commands were given\n" + getHelp());
            } 
            return true;
        }

        if(!defined(name)) { 
            name  = "SMS Entry";
        }
        name = name.trim();

        if(!currentEntry.isGroup()) {
            msg.append("ERROR: Not a folder:\n" +currentEntry.getName());
            return true;
        }

        TypeHandler typeHandler = getRepository().getTypeHandler(type);
        Entry       entry = typeHandler.createEntry(getRepository().getGUID());
        Date        date        = new Date();
        Object[]    values      = typeHandler.makeValues(new Hashtable());
        if(type.equals("phone_sms")) {
            values[0] = info.getFromPhone();
            values[1] = info.getToPhone();
        } else if (type.equals("wikipage")) {
            values[0] = desc.toString().replaceAll("<br>","\n");
            desc = new StringBuffer();
        }

        entry.initEntry(name, desc.toString(), currentEntry, getUser(), new Resource(), "",
                        date.getTime(), date.getTime(), date.getTime(),
                        date.getTime(), values);


        double[] location = org.ramadda.util.GeoUtils.getLocationFromAddress(
                                                                             info.getFromZip());
        if (location != null) {
            entry.setLocation(location[0], location[1], 0);
        }

        List<Entry> entries = (List<Entry>) Misc.newList(entry);
        getEntryManager().insertEntries(entries, true, true);
        msg.append( "New entry:\n" + getEntryInfo(entry));
        return true;
    }

    private String getEntryUrl(Entry entry) {
        return  getRepository().URL_ENTRY.getFullUrl() +"/" + entry.getId();
    }

    private String getEntryInfo(Entry entry) {
        return  entry.getName() +"\n" + getEntryUrl(entry);
    }


    private boolean checkPhone(PhoneInfo info) {
        if (defined(fromPhone)) {
            if (info.getFromPhone().indexOf(fromPhone)<0) {
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
        return true;
    }


    public boolean handleVoice(Request request, PhoneInfo info,StringBuffer msg)
        throws Exception {
        if(getVoiceResponse(info)==null) {
            return false;
        }
        System.err.println ("handleVoice:" + fromPhone +":" +info.getFromPhone() +": to phone:" + toPhone +":" +
                            info.getToPhone());
        PhoneSession session = getSession(info);
        if(!session.getCanEdit()) {
            throw new IllegalAccessException("No edit permissions");
        }

        Entry       baseGroup   = getBaseGroup();
        Entry       parent      = baseGroup;
        String pastEntry = phoneToEntry.get(info.getFromPhone());
        if(pastEntry!=null) {
            Entry entry = getEntryManager().getEntry(request, pastEntry, false);
            if (entry!=null) parent = entry;
        }

        String      name        = "Voice Message  - " + getRepository().formatDate(request, new Date());
        String      type = "media_audiofile";
        TypeHandler typeHandler = getRepository().getTypeHandler(type);
        Entry       entry = typeHandler.createEntry(getRepository().getGUID());
        Date        date        = new Date();
        Object[]    values      = typeHandler.makeValues(new Hashtable());
        StringBuffer desc = new StringBuffer(info.getTranscription());
        File  voiceFile = fetchVoiceFile(request, new URL(info.getRecordingUrl()));

        if(voiceFile==null) {
            return false;
        }
        voiceFile =     getStorageManager().moveToStorage(request, voiceFile);
        Resource resource = new Resource(voiceFile.toString(), Resource.TYPE_STOREDFILE);
        entry.initEntry(name, desc.toString(), parent, getUser(), resource, "",
                        date.getTime(), date.getTime(), date.getTime(),
                        date.getTime(), values);


        double[] location = org.ramadda.util.GeoUtils.getLocationFromAddress(
                                                                             info.getFromZip());
        if (location != null) {
            entry.setLocation(location[0], location[1], 0);
        }

        List<Entry> entries = (List<Entry>) Misc.newList(entry);
        getEntryManager().insertEntries(entries, true, true);
        msg.append("New voice entry:\n" + getEntryUrl(entry));
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
        element.setAttribute(ATTR_PASSWORD_VIEW, passwordView);
        element.setAttribute(ATTR_PASSWORD_ADD, passwordAdd);
        element.setAttribute(ATTR_PASSWORD_EDIT, passwordEdit);
        element.setAttribute(ATTR_RESPONSE, response);
        element.setAttribute(ATTR_VOICEMESSAGE, voiceMessage);
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
        passwordView  = request.getString(ATTR_PASSWORD_VIEW, passwordView);
        passwordAdd  = request.getString(ATTR_PASSWORD_ADD, passwordAdd);
        passwordEdit  = request.getString(ATTR_PASSWORD_EDIT, passwordEdit);

        response  = request.getString(ATTR_RESPONSE, response);
        voiceMessage  = request.getString(ATTR_VOICEMESSAGE, voiceMessage);
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


        String suffix  = " no spaces, dashes, etc";
        sb.append(HtmlUtils.row(HtmlUtils.col("&nbsp;")));
        sb.append(
                  HtmlUtils.row(
                                HtmlUtils.colspan("Accept input when the following optional criteria is met", 2)));
        sb.append(HtmlUtils.formEntry(msgLabel("From Phone"),
                                      HtmlUtils.input(ATTR_FROMPHONE,
                                                      fromPhone, HtmlUtils.SIZE_15)+suffix));
        sb.append(HtmlUtils.formEntry(msgLabel("To Phone"),
                                      HtmlUtils.input(ATTR_TOPHONE, toPhone,
                                                      HtmlUtils.SIZE_15)+suffix));
        String msg1 = "  Entry \"any\" to allow for access without a password";
        
        sb.append(HtmlUtils.formEntry(msgLabel("View Password"),
                                      HtmlUtils.input(ATTR_PASSWORD_VIEW,
                                                      passwordView, HtmlUtils.SIZE_15)+msg1));
        sb.append(HtmlUtils.formEntry(msgLabel("Add Password"),
                                      HtmlUtils.input(ATTR_PASSWORD_ADD,
                                                      passwordAdd, HtmlUtils.SIZE_15)));
        sb.append(HtmlUtils.formEntry(msgLabel("Edit Password"),
                                      HtmlUtils.input(ATTR_PASSWORD_EDIT,
                                                      passwordEdit, HtmlUtils.SIZE_15)));

        /*
          sb.append(HtmlUtils.formEntryTop(msgLabel("SMS Response"),
          HtmlUtils.textArea(ATTR_RESPONSE,
          response==null?"":response,5,60) +"<br>" + "Use ${url} for the URL to the created entry"));
        */


        sb.append(HtmlUtils.row(HtmlUtils.col("&nbsp;")));
        sb.append(
                  HtmlUtils.row(
                                HtmlUtils.colspan("Specify a voice response to handle voice message", 2)));

        sb.append(HtmlUtils.formEntryTop(msgLabel("Voice Message"),
                                         HtmlUtils.input(ATTR_VOICEMESSAGE,voiceMessage,  HtmlUtils.SIZE_60)));
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


    public String getVoiceResponse(PhoneInfo info) {
        if(!checkPhone(info)) {
            return null;
        }
        if(voiceMessage!=null && voiceMessage.trim().length()==0) return null;
        return voiceMessage;
    }


    private File fetchVoiceFile(Request request, URL url) throws Exception {
        String tail    = "voicemessage.mp3";
        File   newFile = getStorageManager().getTmpFile(request,
                                                        tail);
        url = new URL(url.toString()+".mp3");
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

    private String  checkCommand(String[]cmds, String line) {
        String tline = line.toLowerCase();
        for(String cmd: cmds) {
            if(tline.equals(cmd) || tline.startsWith(cmd+" ")) {
                return line.substring(cmd.length()).trim();
            }
        }
        return null;
    }

    private Entry getEntry(Request request, String line,  Entry currentEntry, StringBuffer msg) throws Exception {
        for(String name: StringUtil.split(line,"/", true, true)) {

            Entry childEntry= null;
            if(name.matches("\\d+")) {
                int index = new Integer(name).intValue();
                index--;
                List<Entry> children =  getEntryManager().getChildren(request, currentEntry);
                if(index<0 || index>= children.size()) {
                    msg.append("Bad index:" + index);
                    return null;
                }
                childEntry = children.get(index);
            } else {
                childEntry  = getEntryManager().findEntryWithName(request, currentEntry, name);

            }
            if(childEntry==null) {
                msg.append("Could not find:\n" +name);
                return null;
            }
            currentEntry = childEntry;
        }
        return currentEntry;
    }


    private String getHelp() {
        return "pass <password>\n" +
            "ls,go,ur,url,get <path>\n"+
            "append\n" +
            "new:\n" +
            "folder,note <name>\n" +
            "<text>\n\n" +
            "http://ramadda.org/repository/phone/index.html";
    }


    private PhoneSession makeSession(PhoneInfo info, String password) {
        PhoneSession session = setSessionState(new PhoneSession(info.getFromPhone(), password));
        sessions.put(info.getFromPhone(), session);
        return session;
    }

    private PhoneSession setSessionState(PhoneSession session) {

        String password = session.password;
        
        session.canView = false;
        session.canAdd = false;
        session.canEdit = false;

        String viewPassword = passwordView;
        String addPassword = passwordAdd;
        String editPassword = passwordEdit;
        if(defined(editPassword)) {
            session.canEdit = password.equals(editPassword);
        }

        //password, "any", blank

        if(defined(addPassword)) {
            if(addPassword.equals("any")) {
                session.canAdd = true;
            } else {
                session.canAdd = password.equals(addPassword);
            }
        }

        if(defined(viewPassword)) {
            if(viewPassword.equals("any")) {
                session.canView = true;
            } else {
                session.canView = password.equals(viewPassword);
            }
        }

        if(!session.canAdd)
            session.canAdd = session.canEdit;
        if(!session.canView)
            session.canView= session.canAdd;

        return session;
    }


    private String getEntryName(Entry entry) {
        String entryName = entry.getName();
        if(entryName.length()>20) {
            entryName = entryName.substring(0,19) +"...";
        }
        return entryName;
    }


    public int getWeight() {
        int weight = 0;
        if(defined(fromPhone)) weight+=2;
        if(defined(toPhone)) weight+=2;
        if(defined(passwordView)) weight++;
        if(defined(passwordAdd)) weight++;
        if(defined(passwordEdit)) weight++;
        return weight;
    }




    private static class PhoneSession {
        String fromPhone;
        String password;
        boolean canView = false;
        boolean canAdd = false;
        boolean canEdit = false;
        String lastMessage = "";

        PhoneSession(String fromPhone, String password) {
            this.fromPhone = fromPhone;
            this.password = password;
        }

        public void logout() {
            canView = false;
            canAdd  = false;
            canEdit  = false;
            password = "";
        }
        public boolean getCanView() {
            return canView;
        }

        public boolean getCanAdd() {
            return canAdd;
        }
        public boolean getCanEdit() {
            return canEdit;
        }
    }



}
