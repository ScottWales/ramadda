/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.ramadda.plugins.poll;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;


import ucar.unidata.sql.Clause;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.HttpServer;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
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

    /** _more_          */
    public static final String ATTR_CHOICES = "choices";

    /** _more_          */
    public static final String ATTR_SECRET = "secret";

    /** _more_          */
    public static final String ARG_COMMENT = "comment";

    /** _more_          */
    public static final String ATTR_RESPONSES = "responses";

    /** _more_          */
    public static final String ACTION_ADDRESPONSE = "addresponse";

    public static final String ACTION_DELETERESPONSE = "deleteresponse";

    /** _more_          */
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
            String choicesString = StringUtil.join("\n", choices);
            formBuffer.append(HtmlUtil.formEntryTop(msgLabel("Choices"),
                    HtmlUtil.textArea(ATTR_CHOICES, choicesString, 8, 60)
                    + " " + msg("One choice per line")));
        } catch (Exception exc) {
            throw new RuntimeException(exc);

        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getApplication1PermissionName() {
        return "Who can add to poll";
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
        String choicesString = request.getString(ATTR_CHOICES, "");
        List<String> choices = StringUtil.split(choicesString, "\n", true,
                                   true);

        Hashtable props  = getProperties(entry);
        String    secret = (String) props.get(ATTR_SECRET);
        if (secret == null) {
            secret = getRepository().getGUID() + "_" + Math.random();
            props.put(ATTR_SECRET, secret);
        }
        props.put(ATTR_CHOICES, choices);
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

        boolean hasSecret = secret.equals(request.getString(ATTR_SECRET,""));
        
        boolean canEditEntry = getAccessManager().canEditEntry(request,
                                                               entry);

        if(!canEditEntry && !hasSecret) {
            return new Result("Poll", new StringBuffer(getRepository().showDialogError("No access to view poll")));
        }

        List<String> choices = (List<String>) props.get(ATTR_CHOICES);
        if (choices == null) {
            choices = new ArrayList<String>();
        }
        List<PollResponse> responses =
            (List<PollResponse>) props.get(ATTR_RESPONSES);
        if (responses == null) {
            responses = new ArrayList<PollResponse>();
        }
        StringBuffer sb      = new StringBuffer();

        if (canEditEntry) {
            sb.append(msgLabel("Use this link to allow others to edit"));
            sb.append(
                HtmlUtil.href(
                    request.entryUrl(
                        getRepository().URL_ENTRY_SHOW, entry, ATTR_SECRET,
                        secret), msg("Edit Link")));
        }

        sb.append(entry.getDescription());
        sb.append(HtmlUtil.p());
        boolean changed=  false;


        if (request.exists(ACTION_ADDRESPONSE)) {
            if (!canEditEntry && !hasSecret) {
                return new Result("Poll", new StringBuffer(getRepository().showDialogError("No access to change poll")));
            }
            PollResponse response =
                new PollResponse(request.getString(ARG_RESPONSE, ""),
                                 request.getString(ARG_COMMENT, ""));

            for (String choice : choices) {
                if (request.get("response." + choice, false)) {
                    response.set(choice);
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
            String deleteId = request.getString(ACTION_DELETERESPONSE,"");
            for(PollResponse response: responses) {
                if(!response.getId().equals(deleteId)) {
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
        if(changed) {
            return new Result(request.entryUrl(getRepository().URL_ENTRY_SHOW, entry));
        }

        sb.append(request.form(getRepository().URL_ENTRY_SHOW,
                               HtmlUtil.attr("name", "entryform")));

        sb.append(HtmlUtil.p());
        sb.append(HtmlUtil.submit(msg("Add Response"), ""));
        sb.append(HtmlUtil.p());
        sb.append(HtmlUtil.hidden(ACTION_ADDRESPONSE, ""));
        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(
            "\n<style type=\"text/css\">\n.poll-table td {padding:5px;padding-bottom:0px;}\n\n.poll-header {}\n.poll-input {border : 1px #ccc solid;}\n.poll-response-yes {background: #88C957;}\n.poll-response-no {background: #eee;}</style>\n");
        sb.append(
            "<table class=\"poll-table\" border=1 cellpadding=0 cellspacing=0>");
        StringBuffer headerRow = new StringBuffer();

        headerRow.append("<tr>");
        if (canEditEntry) {
            headerRow.append(HtmlUtil.col("&nbsp;"));
        }
        headerRow.append(HtmlUtil.col(HtmlUtil.b(msg("What/Who")),
                               HtmlUtil.cssClass("poll-header")));
        for (String choice : choices) {
            headerRow.append(HtmlUtil.col(HtmlUtil.b(choice),
                                   HtmlUtil.cssClass("poll-header")));
        }
        headerRow.append(HtmlUtil.col(HtmlUtil.b(msg("Comment")),
                               HtmlUtil.cssClass("poll-header")));
        headerRow.append("</tr>");
        sb.append(headerRow);

        for (PollResponse response : responses) {
            sb.append("<tr>");
            if (canEditEntry) {
                String deleteHref = HtmlUtil.href(
                                                  request.entryUrl(
                                                                   getRepository().URL_ENTRY_SHOW, entry, ACTION_DELETERESPONSE,
                                                                   response.getId()), HtmlUtil.img(getRepository().iconUrl(ICON_DELETE)));

                sb.append(HtmlUtil.col(deleteHref));
            }
            sb.append(HtmlUtil.col(response.getWhat()+"&nbsp;"));
            for (String choice : choices) {
                boolean selected = response.isSelected(choice);
                if (selected) {
                    sb.append(
                        HtmlUtil.col(
                            "yes", HtmlUtil.cssClass("poll-response-yes")));
                } else {
                    sb.append(
                        HtmlUtil.col(
                            "&nbsp;", HtmlUtil.cssClass("poll-response-no")));
                }
            }
            sb.append(HtmlUtil.col(response.getComment() + "&nbsp;"));
            sb.append("</tr>");
        }


        String input = HtmlUtil.input(ARG_RESPONSE, "",
                                      HtmlUtil.SIZE_30
                                      + HtmlUtil.cssClass("poll-input"));
        String commentInput =
            HtmlUtil.input(ARG_COMMENT, "",
                           HtmlUtil.SIZE_30
                           + HtmlUtil.cssClass("poll-input"));
        sb.append("<tr>");

        if (canEditEntry) {
            sb.append(HtmlUtil.col("&nbsp;"));
        }
        sb.append(HtmlUtil.col(input));
        for (String choice : choices) {
            sb.append(HtmlUtil.col(HtmlUtil.checkbox("response." + choice,
                    "true", false), " align=center "));
        }
        sb.append(HtmlUtil.col(commentInput));
        sb.append("</tr>");
        if(responses.size()>0) {
            sb.append(headerRow);
        }


        sb.append("</table>");

        sb.append(HtmlUtil.p());
        sb.append(HtmlUtil.submit(msg("Add Response"), ""));
        sb.append("</form>");
        return new Result("Poll", sb);
    }



}
