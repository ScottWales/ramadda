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

package org.ramadda.plugins.wiki;


import org.incava.util.diff.*;

import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;

import org.ramadda.util.HtmlUtils;

import org.ramadda.util.WikiUtil;

import org.w3c.dom.*;




import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import ucar.unidata.xml.XmlUtil;

import java.sql.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;




import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 *
 */
public class WikiPageOutputHandler extends HtmlOutputHandler {


    /** _more_ */
    public static final String ARG_WIKI_CHANGEDESCRIPTION =
        "wiki.changedescription";




    /** _more_ */
    public static final String ARG_WIKI_COMPARE1 = "wiki.compare1";

    /** _more_ */
    public static final String ARG_WIKI_COMPARE2 = "wiki.compare2";

    /** _more_ */
    public static final String ARG_WIKI_CREATE = "wiki.create";

    /** _more_ */
    public static final String ARG_WIKI_DETAILS = "wiki.details";

    /** _more_ */
    public static final String ARG_WIKI_EDITWITH = "wiki.editwith";

    /** _more_ */
    public static final String ARG_WIKI_RAW = "wiki.raw";


    /** _more_ */
    public static final String ARG_WIKI_TEXT = "wiki.text";

    /** _more_ */
    public static final String ARG_WIKI_VERSION = "wiki.version";


    /** _more_          */
    public static final String GROUP_WIKI = "Wiki";


    /** _more_ */
    public static final OutputType OUTPUT_WIKI = new OutputType("Wiki",
                                                     "wiki.view",
                                                     OutputType.TYPE_OTHER,
                                                     "", ICON_WIKI,
                                                     GROUP_WIKI);

    /** _more_ */
    public static final OutputType OUTPUT_WIKI_HISTORY =
        new OutputType("Wiki History", "wiki.history", OutputType.TYPE_OTHER,
                       "", ICON_WIKI, GROUP_WIKI);

    /** _more_          */
    public static final OutputType OUTPUT_WIKI_DETAILS =
        new OutputType("Entry Details", "wiki.details",
                       OutputType.TYPE_OTHER, "", ICON_WIKI, GROUP_WIKI);

    /** _more_          */
    public static final OutputType OUTPUT_WIKI_TEXT =
        new OutputType("Wiki Text", "wiki.text", OutputType.TYPE_OTHER, "",
                       ICON_WIKI, GROUP_WIKI);


    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public WikiPageOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_WIKI);
        addType(OUTPUT_WIKI_HISTORY);
        addType(OUTPUT_WIKI_DETAILS);
        addType(OUTPUT_WIKI_TEXT);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {

        if (state.entry == null) {
            return;
        }
        if (canAccessDetails(request)) {
            if (state.entry.getType().equals(
                    WikiPageTypeHandler.TYPE_WIKIPAGE)) {
                links.add(makeLink(request, state.entry, OUTPUT_WIKI));
                links.add(makeLink(request, state.entry,
                                   OUTPUT_WIKI_DETAILS));
                links.add(makeLink(request, state.entry,
                                   OUTPUT_WIKI_HISTORY));
                links.add(makeLink(request, state.entry, OUTPUT_WIKI_TEXT));
            }
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    private boolean canAccessDetails(Request request) {
        return !request.isAnonymous();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {

        boolean okToAccessWiki = canAccessDetails(request);

        if (okToAccessWiki) {
            if (outputType.equals(OUTPUT_WIKI_DETAILS)) {
                return super.getHtmlResult(request, outputType, entry, false);
            }

            if (outputType.equals(OUTPUT_WIKI_HISTORY)) {
                return outputWikiHistory(request, entry);
            }

            if (outputType.equals(OUTPUT_WIKI_DETAILS)) {
                request.put(ARG_WIKI_DETAILS, "true");
            }

            if (outputType.equals(OUTPUT_WIKI_TEXT)) {
                request.put(ARG_WIKI_RAW, "true");
            }
        }

        String wikiText = "";
        String header   = "";
        if (request.defined(ARG_WIKI_VERSION)) {
            if ( !okToAccessWiki) {
                throw new AccessException("Not allowed", request);
            }
            Date dttm = new Date((long) request.get(ARG_WIKI_VERSION, 0.0));
            WikiPageHistory wph =
                ((WikiPageTypeHandler) entry.getTypeHandler()).getHistory(
                    entry, dttm);
            if (wph == null) {
                throw new IllegalArgumentException(
                    "Could not find wiki history");
            }
            wikiText = wph.getText();
            header   =
                getRepository().showDialogNote(msgLabel("Text from version")
                    + getRepository().formatDate(wph.getDate()));
        } else {
            Object[] values = entry.getValues();
            if ((values != null) && (values.length > 0)
                    && (values[0] != null)) {
                wikiText = (String) values[0];
            }
        }

        if (okToAccessWiki) {
            if (request.get(ARG_WIKI_RAW, false)) {
                StringBuffer sb = new StringBuffer();
                sb.append(HtmlUtils.form(""));
                sb.append(HtmlUtils.textArea(ARG_WIKI_TEXT, wikiText, 250,
                                             60,
                                             HtmlUtils.id(ARG_WIKI_TEXT)));
                sb.append(HtmlUtils.formClose());

                return makeLinksResult(request, msg("Wiki"), sb,
                                       new State(entry));
            }
        }


        /*
        String detailsView =
            HtmlUtils.href(request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                           entry, ARG_WIKI_DETAILS,
                                           "" + true), msg("Details"));

        String rawLink =
            HtmlUtils.href(request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                           entry, ARG_WIKI_RAW,
                                           "" + true), msg("Text"));

        header = HtmlUtils.leftRight(header,
                                    HtmlUtils.div(detailsView + " " + rawLink,
                                        HtmlUtils.cssClass(CSS_CLASS_SMALLLINK)));
        */
        WikiUtil wikiUtil = new WikiUtil(Misc.newHashtable(new Object[] {
                                OutputHandler.PROP_REQUEST,
                                request, OutputHandler.PROP_ENTRY, entry }));
        StringBuffer sb = new StringBuffer();
        sb.append(header);
        sb.append(getRepository().getWikiManager().wikifyEntry(request,
                entry, wikiUtil, wikiText, true, null, null));
        Hashtable links = (Hashtable) wikiUtil.getProperty("wikilinks");
        if (links != null) {
            List<Association> associations =
                getAssociationManager().getAssociations(request, entry);
        }

        return makeLinksResult(request, msg("Wiki"), sb, new State(entry));
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputWikiCompare(Request request, Entry entry)
            throws Exception {
        StringBuffer    sb   = new StringBuffer();

        Date dttm1 = new Date((long) request.get(ARG_WIKI_COMPARE1, 0.0));
        WikiPageHistory wph1 =
            ((WikiPageTypeHandler) entry.getTypeHandler()).getHistory(entry,
                dttm1);
        if (wph1 == null) {
            throw new IllegalArgumentException("Could not find wiki history");
        }

        Date dttm2 = new Date((long) request.get(ARG_WIKI_COMPARE2, 0.0));
        WikiPageHistory wph2 =
            ((WikiPageTypeHandler) entry.getTypeHandler()).getHistory(entry,
                dttm2);
        if (wph2 == null) {
            throw new IllegalArgumentException("Could not find wiki history");
        }

        String lbl1 = "Revision as of "
                      + getRepository().formatDate(wph1.getDate())
                      + HtmlUtils.br() + wph1.getUser() + HtmlUtils.br()
                      + wph1.getDescription();
        String lbl2 = "Revision as of "
                      + getRepository().formatDate(wph2.getDate())
                      + HtmlUtils.br() + wph2.getUser() + HtmlUtils.br()
                      + wph2.getDescription();
        sb.append("<table width=100% border=0 cellspacing=5 cellpadding=4>");
        sb.append(HtmlUtils.row(HtmlUtils.cols("", lbl1, "", lbl2)));

        getDiff(wph1.getText(), wph2.getText(), sb);
        sb.append("</table>");

        return makeLinksResult(request, msg("Wiki Comparison"), sb,
                               new State(entry));
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputWikiHistory(Request request, Entry entry)
            throws Exception {
        StringBuffer sb      = new StringBuffer();
        boolean      canEdit = getAccessManager().canEditEntry(request, entry);

        if (request.exists(ARG_WIKI_COMPARE1)
                && request.exists(ARG_WIKI_COMPARE2)) {
            return outputWikiCompare(request, entry);
        }




        sb.append(request.form(getRepository().URL_ENTRY_SHOW));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtils.hidden(ARG_OUTPUT, OUTPUT_WIKI_HISTORY));

        List<WikiPageHistory> history =
            ((WikiPageTypeHandler) entry.getTypeHandler()).getHistoryList(
                entry, null, false);
        sb.append(HtmlUtils.open(HtmlUtils.TAG_TABLE,
                                 " cellspacing=5 cellpadding=5 "));
        sb.append(HtmlUtils.row(HtmlUtils.cols(new Object[] {
            HtmlUtils.b(msg("Version")), "", "", "", HtmlUtils.b(msg("User")),
            HtmlUtils.b(msg("Date")), HtmlUtils.b(msg("Description"))
        })));
        int version = 1;
        for (int i = history.size() - 1; i >= 0; i--) {
            WikiPageHistory wph  = history.get(i);
            String          edit = "";
            if (canEdit) {
                edit = HtmlUtils
                    .href(request
                        .entryUrl(
                            getRepository().URL_ENTRY_FORM, entry,
                            ARG_WIKI_EDITWITH,
                            wph.getDate().getTime() + ""), HtmlUtils
                                .img(getRepository().iconUrl(ICON_EDIT),
                                     msg("Edit with this version")));
            }
            String view = HtmlUtils.href(
                              request.entryUrl(
                                  getRepository().URL_ENTRY_SHOW, entry,
                                  ARG_WIKI_VERSION,
                                  wph.getDate().getTime()
                                  + ""), HtmlUtils.img(
                                      getRepository().iconUrl(ICON_WIKI),
                                      msg("View this page")));
            String btns =
                HtmlUtils.radio(ARG_WIKI_COMPARE1,
                                "" + wph.getDate().getTime(),
                                false) + HtmlUtils.radio(ARG_WIKI_COMPARE2,
                                    "" + wph.getDate().getTime(), false);
            String versionLabel;
            if (i == history.size() - 1) {
                versionLabel = msg("Current");
            } else {
                versionLabel = "" + wph.getVersion();
            }
            sb.append(HtmlUtils.row(HtmlUtils.cols(new Object[] {
                versionLabel, btns, edit, view, wph.getUser().getLabel(),
                getRepository().formatDate(wph.getDate()),
                wph.getDescription()
            })));
        }

        sb.append(HtmlUtils.close(HtmlUtils.TAG_TABLE));

        sb.append(HtmlUtils.submit("Compare Selected Versions"));
        sb.append(HtmlUtils.formClose());

        return makeLinksResult(request, msg("Wiki History"), sb,
                               new State(entry));
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        return outputEntry(request, outputType, group);
    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {}


    /**
     * _more_
     *
     * @param text1 _more_
     * @param text2 _more_
     * @param sb _more_
     */
    public static void getDiff(String text1, String text2, StringBuffer sb) {
        String[] aLines = Misc.listToStringArray(StringUtil.split(text1,
                              "\n", false, false));
        String[] bLines = Misc.listToStringArray(StringUtil.split(text2,
                              "\n", false, false));
        List     diffs    = (new Diff(aLines, bLines)).diff();

        Iterator it       = diffs.iterator();
        int      leftIdx  = 0;
        int      rightIdx = 0;
        int      context;
        while (it.hasNext()) {
            Difference diff     = (Difference) it.next();
            int        delStart = diff.getDeletedStart();
            int        delEnd   = diff.getDeletedEnd();
            int        addStart = diff.getAddedStart();
            int        addEnd   = diff.getAddedEnd();
            String     from     = toString(delStart, delEnd);
            String     to       = toString(addStart, addEnd);
            String     type     = ((delEnd != Difference.NONE)
                           && (addEnd != Difference.NONE))
                                  ? "c"
                                  : ((delEnd == Difference.NONE)
                                     ? "a"
                                     : "d");


            if (delEnd != Difference.NONE) {
                context = Math.max(leftIdx, delStart - 4);
                if (context < delStart) {
                    sb.append(
                        "<tr valign=top><td></td><td width=50% class=wikicompare-context>");
                    appendLines(context, delStart - 1, true, aLines, sb);
                }
                if (addEnd != Difference.NONE) {
                    context = Math.max(rightIdx, addStart - 4);
                    if (context < addStart) {
                        sb.append(
                            "<td></td><td width=50% class=wikicompare-context>");
                        appendLines(context, addStart - 1, true, bLines, sb);
                        sb.append("</td>");
                    }
                }

                sb.append(
                    "<tr valign=top><td colspan=2 class=\"wikicompare-lineheader\">Line:"
                    + delStart
                    + "</td><td colspan=2 class=\"wikicompare-lineheader\">");
                if (addEnd != Difference.NONE) {
                    sb.append("Line:" + addStart);
                }
                sb.append("</td></tr>");

                sb.append("<tr valign=top>");
                sb.append("<td valign=center>-</td>");
                sb.append("<td width=50% class=wikicompare-deleted>");
                appendLines(delStart, delEnd, false, aLines, sb);
                leftIdx = delEnd + 1;
                sb.append("</td>");
                if (addEnd != Difference.NONE) {
                    sb.append(
                        "<td>+</td><td width=50% class=wikicompare-added>");
                    appendLines(addStart, addEnd, false, bLines, sb);
                    rightIdx = addEnd + 1;
                    sb.append("</td></tr>");

                    continue;
                }
                sb.append("<td></td><td width=50%>&nbsp;</td></tr>");
            }
            if (addEnd != Difference.NONE) {

                context = leftIdx + 4;
                sb.append(
                    "<tr valign=top><td></td><td width=50% class=wikicompare-context>");
                appendLines(leftIdx, leftIdx + 4, true, aLines, sb);
                context = Math.max(rightIdx, addStart - 4);
                if (context < addStart) {
                    sb.append(
                        "<td></td><td width=50% class=wikicompare-context>");
                    appendLines(context, addStart - 1, true, bLines, sb);
                    sb.append("</td>");
                }


                sb.append(
                    "<tr><td colspan=2></td><td colspan=2 class=\"wikicompare-lineheader\">");
                sb.append("Line:" + addStart);
                sb.append("</td></tr>");

                sb.append(
                    "<tr valign=top><td></td><td width=50%></td><td>+</td><td width=50% class=wikicompare-added>");
                appendLines(addStart, addEnd, false, bLines, sb);
                rightIdx = addEnd + 1;
                sb.append("</td></tr>");
            }

        }
    }

    /**
     * _more_
     *
     * @param start _more_
     * @param end _more_
     * @param includeLineNumber _more_
     * @param lines _more_
     * @param sb _more_
     */
    protected static void appendLines(int start, int end,
                                      boolean includeLineNumber,
                                      String[] lines, StringBuffer sb) {
        includeLineNumber = false;
        for (int lnum = start; lnum <= end; ++lnum) {
            if (lnum < 0) {
                continue;
            }
            if (lnum >= lines.length) {
                break;
            }
            String line = lines[lnum];
            line = HtmlUtils.entityEncode(line);
            if (includeLineNumber) {
                sb.append("<b>" + lnum + ": </b>");
            }
            sb.append(line + "<br>");

        }
    }

    /**
     * _more_
     *
     * @param start _more_
     * @param end _more_
     *
     * @return _more_
     */
    protected static String toString(int start, int end) {
        // adjusted, because file lines are one-indexed, not zero.

        StringBuffer buf = new StringBuffer();

        // match the line numbering from diff(1):
        buf.append((end == Difference.NONE)
                   ? start
                   : (1 + start));

        if ((end != Difference.NONE) && (start != end)) {
            buf.append(",").append(1 + end);
        }

        return buf.toString();
    }



}
