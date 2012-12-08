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
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;


import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.net.*;
import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.*;


/**
 * Provides a top-level API
 *
 */
public class TwilioApiHandler extends RepositoryManager implements RequestHandler {

    public static final String PROP_AUTHTOKEN = "twilio.authtoken";
    public static final String PROP_APPID = "twilio.appid";


    public static final String ARG_ACCOUNTSID = "AccountSid";
    public static final String ARG_RECORDINGSID = "RecordingSid";
    public static final String ARG_RECORDINGURL = "RecordingUrl";
    public static final String ARG_FROMZIP = "FromZip";


    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /** _more_          */
    public static final String TAG_RESPONSE = "Response";
    public static final String TAG_RECORD = "Record";
    public static final String TAG_TRANSCRIPTIONTEXT  = "TranscriptionText";
    public static final String TAG_STATUS = "Status";

    /** _more_          */
    public static final String TAG_SMS = "Sms";
    public static final String TAG_SAY = "Say";

    public static final String ATTR_VOICE = "voice";

    /** _more_          */
    public static final String ARG_FROM = "From";

    /** _more_          */
    public static final String ARG_TO = "To";

    /** _more_          */
    public static final String ARG_BODY = "Body";

    /** _more_          */
    public static final String ARG_ = "";
    //    public static final String ARG_  = ""; 

    /**
     *     ctor
     *    
     *     @param repository the repository
     *     @param node xml from api.xml
     *     @param props propertiesn
     *    
     *     @throws Exception on badness
     */
    public TwilioApiHandler(Repository repository) throws Exception {
        super(repository);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public List<PhoneHarvester> getHarvesters() {
        List<PhoneHarvester> harvesters = new ArrayList<PhoneHarvester>();
        for (Harvester harvester : getHarvesterManager().getHarvesters()) {
            if (harvester.getActiveOnStart()
                    && (harvester instanceof PhoneHarvester)) {
                harvesters.add((PhoneHarvester) harvester);
            }
        }

        return harvesters;
    }


    private boolean callOK(Request request) {
        String appId = getRepository().getProperty(PROP_APPID,null);
        if(appId == null) return false;
        return request.getString(ARG_ACCOUNTSID, "").equals(appId);
    }


    /**
     * handle the request
     *
     * @param request request
     *
     * @return result
     *
     * @throws Exception on badness
     */
    public Result processSms(Request request) throws Exception {
        PhoneInfo info = new PhoneInfo(PhoneInfo.TYPE_SMS,
                                       request.getString(ARG_FROM, ""),
                                       request.getString(ARG_TO, ""), null);
        System.err.println("TwilioApiHandler: request: " + request);
        StringBuffer sb =
            new StringBuffer(XML_HEADER);
        sb.append(XmlUtil.openTag(TAG_RESPONSE));
        info.setMessage(request.getString(ARG_BODY, ""));
        info.setFromZip(request.getString(ARG_FROMZIP, (String) null));
        boolean handledMessage = false;
        if(!callOK(request)) {
            sb.append(XmlUtil.tag(TAG_SMS, "", "Sorry, bad APPID property defined"));
        } else {
                StringBuffer returnMsg = new StringBuffer();

                for (PhoneHarvester harvester : getHarvesters()) {
                    if (harvester.handleMessage(request, info, returnMsg)) {
                        String response = returnMsg.toString();
                        if(response.length()==0) response = "Message handled";
                        else if(response.length()>120) {
                            response = response.substring(0,119);
                        }
                        sb.append(XmlUtil.tag(TAG_SMS, "", response));
                        handledMessage = true;
                        break;
                    }
                }


                if ( !handledMessage) {
                    String response = returnMsg.toString();
                    sb.append(XmlUtil.tag(TAG_SMS, "", "Sorry, RAMADDA was not able to process your message.\n" + response));
                }
            }
            sb.append(XmlUtil.closeTag(TAG_RESPONSE));
            return new Result("", sb, "text/xml");
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
    public Result processVoice(Request request) throws Exception {
        String authToken = getRepository().getProperty(PROP_AUTHTOKEN, null);
        String recordingUrl = request.getString(ARG_RECORDINGURL, null);
        System.err.println("processVoice:" + request.getUrlArgs());
        StringBuffer sb = new StringBuffer();
        sb.append(XML_HEADER);
        sb.append(XmlUtil.openTag(TAG_RESPONSE));
        PhoneInfo info = new PhoneInfo(PhoneInfo.TYPE_SMS,
                                       request.getString(ARG_FROM, ""),
                                       request.getString(ARG_TO, ""), null);
        if(!callOK(request)) {
            sb.append(XmlUtil.tag(TAG_SAY,XmlUtil.attr(ATTR_VOICE,"woman"),"Sorry, bad application identifier"));
        } else {
            if(recordingUrl==null) {
                String voiceResponse = null;
                for (PhoneHarvester harvester : getHarvesters()) {
                    String response = harvester.getVoiceResponse(info);
                    if(response!=null && response.trim().length()>0) {
                        voiceResponse = response;
                        break;
                    }
                }
                if(voiceResponse==null) {
                    sb.append(XmlUtil.tag(TAG_SAY,XmlUtil.attr(ATTR_VOICE,"woman"),"Sorry, this ramadda repository does not accept voice messages</Say>"));
                } else {
                    //<Gather timeout="10" finishOnKey="*">
                    //<Say>Please enter your pin number and then press star.</Say>
                    //</Gather>
                    sb.append(XmlUtil.tag(TAG_SAY,XmlUtil.attr(ATTR_VOICE,"woman"),voiceResponse));
                    sb.append(XmlUtil.tag(TAG_RECORD, XmlUtil.attrs(new String[]{
                                    "maxLength", "30",
                                    authToken!=null?"transcribe":"dummy", "true"
                                })));
                }
            } else {
                info.setRecordingUrl(recordingUrl);
                if(authToken!=null) {
                    String text = getTranscriptionText(request, authToken);
                    if(text == null) {
                        Misc.sleepSeconds(5);
                        text = getTranscriptionText(request, authToken);
                        if(text == null) {
                            Misc.sleepSeconds(5);
                            text = getTranscriptionText(request, authToken);
                        }
                    }
                    if(text!=null) {
                        info.setTranscription(text);
                    } else {
                        System.err.println("processVoice: failed to get text");
                    }
                }

                for (PhoneHarvester harvester : getHarvesters()) {
                    if (harvester.handleVoice(request, info)) {
                        break;
                    }
                }

            }
        }
        sb.append(XmlUtil.closeTag(TAG_RESPONSE));
        return new Result("", sb, "text/xml");
    }



    private String getTranscriptionText(Request request, String authToken) throws Exception {
        String transcriptionUrl = "https://api.twilio.com/2010-04-01/Accounts/" +
            request.getString(ARG_ACCOUNTSID, null)+"/Recordings/"+ 
            request.getString(ARG_RECORDINGSID,null)+"/Transcriptions";
        URL url        = new URL(transcriptionUrl);
        HttpURLConnection     huc = (HttpURLConnection) url.openConnection();            
        String auth  = request.getString(ARG_ACCOUNTSID, null)+":" + authToken;
        String encoding = RepositoryUtil.encodeBase64 (auth.getBytes());
        huc.addRequestProperty("Authorization",
                               "Basic " + encoding);
        String transcription = new String(IOUtil.readBytes(huc.getInputStream()));
        Element root = XmlUtil.getRoot(transcription);
        Element statusNode = XmlUtil.findDescendant(root,TAG_STATUS);
        if(statusNode==null) return null;
        if(!XmlUtil.getChildText(statusNode).equals("completed")) {
            return null;

        }

        Element textNode = XmlUtil.findDescendant(root,TAG_TRANSCRIPTIONTEXT);
        if(textNode!=null) {
            return  XmlUtil.getChildText(textNode);
        }
        return null;
    }


}
