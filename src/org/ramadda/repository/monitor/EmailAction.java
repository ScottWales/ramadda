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

package org.ramadda.repository.monitor;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;

import org.ramadda.util.HtmlUtils;

import ucar.unidata.util.StringUtil;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.30 $
 */
public class EmailAction extends PasswordAction {


    /**
     * _more_
     */
    public EmailAction() {}

    /**
     * _more_
     *
     * @param id _more_
     */
    public EmailAction(String id) {
        super(id);
    }


    /**
     * _more_
     * @param id _more_
     * @param remoteUserId _more_
     */
    public EmailAction(String id, String remoteUserId) {
        super(id, remoteUserId, (String) null);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getActionLabel() {
        return "Email Action";
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getActionName() {
        return "email";
    }


    /**
     * _more_
     *
     *
     * @param entryMonitor _more_
     * @return _more_
     */
    public String getSummary(EntryMonitor entryMonitor) {
        return "Send an email to " + getRemoteUserId();
    }

    /**
     * _more_
     *
     * @param monitor _more_
     * @param sb _more_
     */
    public void addToEditForm(EntryMonitor monitor, StringBuffer sb) {
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.colspan("Send an email", 2));

        sb.append(
            HtmlUtils.formEntry(
                "Email address",
                HtmlUtils.input(
                    getArgId(ARG_ACTION_ID), getRemoteUserId(),
                    HtmlUtils.SIZE_60)));
        sb.append(
            HtmlUtils.formEntryTop(
                "Message",
                HtmlUtils.textArea(
                    getArgId(ARG_ACTION_MESSAGE), getMessageTemplate(), 5,
                    60)));
        sb.append(HtmlUtils.formTableClose());
    }


    /**
     * _more_
     *
     *
     * @param monitor _more_
     * @param entry _more_
     */
    protected void entryMatched(EntryMonitor monitor, Entry entry) {
        try {
            String from = monitor.getUser().getEmail();
            if ((from == null) || (from.trim().length() == 0)) {
                from = monitor.getRepository().getProperty(PROP_ADMIN_EMAIL,
                        "");
            }

            try {
                for (String to :
                        StringUtil.split(getRemoteUserId(), ",", true,
                                         true)) {
                    monitor.getRepository().getLogManager().logInfo(
                        "Monitor:" + this + " sending mail to: " + to);
                    String message = getMessage(monitor, entry);
                    monitor.getRepository().getMailManager().sendEmail(to, from,
                            "New Entry", message, false);
                }
            } catch (Exception exc) {
                monitor.handleError("Error sending email to "
                                    + getRemoteUserId() + " from:"
                                    + from, exc);
            }
        } catch (Exception exc2) {
            monitor.handleError("Error:", exc2);
        }
    }



}
