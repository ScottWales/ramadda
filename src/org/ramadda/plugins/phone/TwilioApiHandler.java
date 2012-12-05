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

    /** _more_          */
    public static final String TAG_RESPONSE = "Response";

    /** _more_          */
    public static final String TAG_SMS = "Sms";


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
        System.err.println("TwilioApiHandler: Phone: " + info);
        System.err.println("TwilioApiHandler: request: " + request);
        StringBuffer sb =
            new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append(XmlUtil.openTag(TAG_RESPONSE));
        info.setMessage(request.getString(ARG_BODY, ""));
        info.setFromZip(request.getString("FromZip", (String) null));
        boolean handledMessage = false;
        StringBuffer returnMsg = new StringBuffer();
        for (PhoneHarvester harvester : getHarvesters()) {
            System.err.println ("Checking harvester:" + harvester);
            if (harvester.handleMessage(request, info, returnMsg)) {
                String response = returnMsg.toString();
                if(response.length()==0) response = "Message handled";
                sb.append(XmlUtil.tag(TAG_SMS, "", response));
                handledMessage = true;
                break;
            }
        }

        if ( !handledMessage) {
            String response = returnMsg.toString();
            sb.append(XmlUtil.tag(TAG_SMS, "", "Sorry, RAMADDA was not able to process your message.\n" + response));
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
        String recordingUrl = request.getString("RecordingUrl", null);
        System.err.println("processVoice:" + request.getUrlArgs());
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<Response>");
        PhoneInfo info = new PhoneInfo(PhoneInfo.TYPE_SMS,
                                       request.getString(ARG_FROM, ""),
                                       request.getString(ARG_TO, ""), null);
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
                sb.append(
                          "<Say voice=\"woman\">Sorry, this ramadda repository does not accept voice messages</Say>");
            } else {
                sb.append(
                          "<Say voice=\"woman\">" + voiceResponse +"</Say>");
                sb.append("<Record maxLength=\"30\" />");
            }
        } else {
            info.setRecordingUrl(recordingUrl);
            System.err.println("recording url:" + recordingUrl);
            for (PhoneHarvester harvester : getHarvesters()) {
                System.err.println ("Checking harvester:" + harvester);
                if (harvester.handleVoice(request, info)) {
                    break;
                }
            }

        }
        sb.append("</Response>");

        return new Result("", sb, "text/xml");
    }



}
