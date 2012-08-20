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

package org.ramadda.plugins.poll;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;


import ucar.unidata.sql.Clause;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HttpServer;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.util.WikiUtil;
import ucar.unidata.xml.XmlUtil;

import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.Statement;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 *
 *
 */
public class PollTypeHandler extends BlobTypeHandler {

    /** _more_ */
    public static final String ATTR_CHOICES = "choices";

    /** _more_ */
    public static final String ATTR_RESPONSETYPES = "responsetypes";

    /** _more_ */
    public static final String ATTR_SECRET = "secret";

    /** _more_ */
    public static final String ARG_COMMENT = "comment";

    /** _more_ */
    public static final String ATTR_RESPONSES = "responses";

    /** _more_ */
    public static final String ACTION_ADDRESPONSE = "addresponse";

    /** _more_ */
    public static final String ACTION_DELETERESPONSE = "deleteresponse";

    /** _more_ */
    public static final String ARG_RESPONSE = "response";


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public PollTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean returnToEditForm() {
        return true;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param formBuffer _more_
     * @param entry _more_
     */
    public void addColumnsToEntryForm(Request request,
                                      StringBuffer formBuffer, Entry entry) {
        try {
            Hashtable    props   = getProperties(entry);
            List<String> choices = (List<String>) props.get(ATTR_CHOICES);
            if (choices == null) {
                choices = new ArrayList<String>();
            }

            List<String> types = (List<String>) props.get(ATTR_RESPONSETYPES);
            if (types == null) {
                types = new ArrayList<String>();
            }
            if (types.size() == 0) {
                types.add("Yes");
            }
            formBuffer.append(HtmlUtils.formEntryTop(msgLabel("Choices"),
                    HtmlUtils.textArea(ATTR_CHOICES,
                                       StringUtil.join("\n", choices), 8,
                                       30) + " "
                                           + msg("One choice per line")));

            formBuffer.append(HtmlUtils.formEntryTop(msgLabel("Responses"),
                    HtmlUtils.textArea(ATTR_RESPONSETYPES,
                                       StringUtil.join("\n", types), 4,
                                       30) + " " + msg("One type per line")));
        } catch (Exception exc) {
            throw new RuntimeException(exc);

        }
    }

    /**
     * _more_
     *
     *
     * @param type _more_
     * @return _more_
     */
    public String getTypePermissionName(String type) {
        if (type.equals(Permission.ACTION_TYPE1)) {
            return "Who can add to poll";
        }

        return "Who can view poll results";
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parent _more_
     * @param newEntry _more_
     *
     * @throws Exception _more_
     */
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        String       choicesString = request.getString(ATTR_CHOICES, "");
        List<String> choices = StringUtil.split(choicesString, "\n", true,
                                   true);

        String       typesString = request.getString(ATTR_RESPONSETYPES, "");
        List<String> types = StringUtil.split(typesString, "\n", true, true);

        Hashtable    props       = getProperties(entry);
        String       secret      = (String) props.get(ATTR_SECRET);
        if (secret == null) {
            secret = getRepository().getGUID() + "_" + Math.random();
            props.put(ATTR_SECRET, secret);
        }
        props.put(ATTR_CHOICES, choices);
        props.put(ATTR_RESPONSETYPES, types);
        setProperties(entry, props);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {

        Hashtable props  = getProperties(entry);
        String    secret = (String) props.get(ATTR_SECRET);
        if (secret == null) {
            //Shoudln't happen
            secret = getRepository().getGUID() + "_" + Math.random();
            props.put(ATTR_SECRET, secret);
        }

        String  secretFromUrl = request.getString(ATTR_SECRET, "none");
        boolean hasSecret     = secret.equals(secretFromUrl);

        boolean canEditEntry  = getAccessManager().canEditEntry(request,
                                   entry);

        if ( !canEditEntry && !hasSecret) {
            return new Result(
                "Poll",
                new StringBuffer(
                    getRepository().showDialogError(
                        "No access to view poll")));
        }

        List<String> choices = (List<String>) props.get(ATTR_CHOICES);
        if (choices == null) {
            choices = new ArrayList<String>();
        }

        List<String> types = (List<String>) props.get(ATTR_RESPONSETYPES);
        if (types == null) {
            types = new ArrayList<String>();
        }



        List<PollResponse> responses =
            (List<PollResponse>) props.get(ATTR_RESPONSES);
        if (responses == null) {
            responses = new ArrayList<PollResponse>();
        }
        StringBuffer sb = new StringBuffer();

        if (canEditEntry) {
            sb.append(msgLabel("Use this link to allow others to edit"));
            sb.append(
                HtmlUtils.href(
                    request.entryUrl(
                        getRepository().URL_ENTRY_SHOW, entry, ATTR_SECRET,
                        secret), msg("Edit Link")));
        }

        sb.append(HtmlUtils.cssLink(getRepository().getUrlBase()
                                    + "/poll/style.css"));

        sb.append(HtmlUtils.p());
        sb.append(entry.getDescription());
        sb.append(HtmlUtils.p());
        boolean changed = false;


        //NOTICE: Use getEncodedString below which does an entity encoding of the possibly anonymous input
        if (request.exists(ACTION_ADDRESPONSE)) {
            if ( !canEditEntry && !hasSecret) {
                return new Result(
                    "Poll",
                    new StringBuffer(
                        getRepository().showDialogError(
                            "No access to change poll")));
            }
            PollResponse response =
                new PollResponse(request.getEncodedString(ARG_RESPONSE, ""),
                                 request.getEncodedString(ARG_COMMENT, ""));

            for (String choice : choices) {
                if (request.defined("response." + choice)) {
                    response.set(choice,
                                 request.getEncodedString("response."
                                     + choice, ""));
                }
            }
            responses.add(response);
            props.put(ATTR_RESPONSES, responses);
            setProperties(entry, props);
            getEntryManager().storeEntry(entry);
            changed = true;
        }

        if (canEditEntry && request.defined(ACTION_DELETERESPONSE)) {
            List<PollResponse> tmp = new ArrayList<PollResponse>();
            String deleteId = request.getString(ACTION_DELETERESPONSE, "");
            for (PollResponse response : responses) {
                if ( !response.getId().equals(deleteId)) {
                    tmp.add(response);
                }
            }
            responses = tmp;
            props.put(ATTR_RESPONSES, responses);
            setProperties(entry, props);
            getEntryManager().storeEntry(entry);
            changed = true;
        }

        //If there was a change then redirect back to here
        if (changed) {
            if (hasSecret) {
                return new Result(
                    request.entryUrl(
                        getRepository().URL_ENTRY_SHOW, entry, ATTR_SECRET,
                        secretFromUrl));
            } else {
                return new Result(
                    request.entryUrl(getRepository().URL_ENTRY_SHOW, entry));
            }
        }

        sb.append(request.form(getRepository().URL_ENTRY_SHOW,
                               HtmlUtils.attr("name", "entryform")));

        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit(msg("Add Response"), ""));
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.hidden(ACTION_ADDRESPONSE, ""));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        if (hasSecret) {
            sb.append(HtmlUtils.hidden(ATTR_SECRET, secretFromUrl));
        }

        sb.append(
            "<table class=\"poll-table\" border=1 cellpadding=0 cellspacing=0>");
        StringBuffer headerRow = new StringBuffer();
        headerRow.append("<tr>");
        if (canEditEntry) {
            headerRow.append(
                HtmlUtils.col("&nbsp;", HtmlUtils.cssClass("poll-header")));
        }
        headerRow.append(HtmlUtils.col(HtmlUtils.b(msg("What or who")),
                                       HtmlUtils.cssClass("poll-header")));
        for (String choice : choices) {
            headerRow.append(
                HtmlUtils.col(
                    HtmlUtils.b(choice), HtmlUtils.cssClass("poll-header")));
        }
        headerRow.append(HtmlUtils.col(HtmlUtils.b(msg("Comment")),
                                       HtmlUtils.cssClass("poll-header")));
        headerRow.append("</tr>");
        sb.append(headerRow);

        for (PollResponse response : responses) {
            sb.append("<tr>");
            if (canEditEntry) {
                String deleteHref = HtmlUtils.href(
                                        request.entryUrl(
                                            getRepository().URL_ENTRY_SHOW,
                                            entry, ACTION_DELETERESPONSE,
                                            response.getId()), HtmlUtils.img(
                                                getRepository().iconUrl(
                                                    ICON_DELETE)));

                sb.append(HtmlUtils.col(deleteHref));
            }
            sb.append(HtmlUtils.col(response.getWhat() + "&nbsp;"));
            for (String choice : choices) {
                String selected = response.get(choice);
                if (selected != null) {
                    sb.append(
                        HtmlUtils.col(
                            selected,
                            HtmlUtils.cssClass("poll-response-yes")));
                } else {
                    sb.append(
                        HtmlUtils.col(
                            "&nbsp;",
                            HtmlUtils.cssClass("poll-response-no")));
                }
            }
            sb.append(HtmlUtils.col(response.getComment() + "&nbsp;"));
            sb.append("</tr>");
        }


        String input = HtmlUtils.input(ARG_RESPONSE, "",
                                       HtmlUtils.SIZE_30
                                       + HtmlUtils.cssClass("poll-input"));
        String commentInput = HtmlUtils.input(ARG_COMMENT, "",
                                  HtmlUtils.SIZE_30
                                  + HtmlUtils.cssClass("poll-input"));
        sb.append("<tr>");

        if (canEditEntry) {
            sb.append(HtmlUtils.col("&nbsp;"));
        }
        sb.append(HtmlUtils.col(input));
        List typesPlus = new ArrayList(types);
        typesPlus.add(0, new TwoFacedObject("----", ""));
        for (String choice : choices) {
            if (types.size() == 1) {
                sb.append(HtmlUtils.col(HtmlUtils.checkbox("response."
                        + choice, types.get(0), false) + " " + types.get(0)));
            } else {
                sb.append(HtmlUtils.col(HtmlUtils.select("response."
                        + choice, typesPlus)));
            }
        }
        sb.append(HtmlUtils.col(commentInput));
        sb.append("</tr>");
        if (responses.size() > 0) {
            //            sb.append(headerRow);
        }


        sb.append("</table>");

        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit(msg("Add Response"), ""));
        sb.append("</form>");

        return new Result("Poll", sb);
    }



}
