/*
* Copyright 2008-2013 Geode Systems LLC
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

package org.ramadda.repository;


import org.ramadda.repository.database.Tables;


import org.ramadda.repository.output.OutputHandler;
import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;



import ucar.unidata.util.DateUtil;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 * Handles entry comments
 */
public class CommentManager extends RepositoryManager {


    /**
     * ctor
     *
     * @param repository the repository
     */
    public CommentManager(Repository repository) {
        super(repository);
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
    public Result processCommentsShow(Request request) throws Exception {
        Entry        entry = getEntryManager().getEntry(request);
        StringBuffer sb    = new StringBuffer();
        request.appendMessage(sb);
        String entryUrl =
            HtmlUtils.url(getEntryManager().getFullEntryShowUrl(request),
                          ARG_ENTRYID, entry.getId());
        String title = getEntryDisplayName(entry);
        String share =
            "<script type=\"text/javascript\">var addthis_disable_flash=\"true\" addthis_pub=\"jeffmc\";</script><a href=\"http://www.addthis.com/bookmark.php?v=20\" onmouseover=\"return addthis_open(this, '', '" + entryUrl + "', '" + title + "')\" onmouseout=\"addthis_close()\" onclick=\"return addthis_sendto()\"><img src=\"http://s7.addthis.com/static/btn/lg-share-en.gif\" width=\"125\" height=\"16\" alt=\"Bookmark and Share\" style=\"border:0\"/></a><script type=\"text/javascript\" src=\"http://s7.addthis.com/js/200/addthis_widget.js\"></script>";

        sb.append(share);

        boolean doRatings = getRepository().getProperty(PROP_RATINGS_ENABLE,
                                false);
        if (doRatings) {
            String link = request.url(getRepository().URL_COMMENTS_SHOW,
                                      ARG_ENTRYID, entry.getId());
            String ratings = HtmlUtils.div(
                                 "",
                                 HtmlUtils.cssClass("js-kit-rating")
                                 + HtmlUtils.attr(
                                     HtmlUtils.ATTR_TITLE,
                                     entry.getFullName()) + HtmlUtils.attr(
                                         "permalink",
                                         link)) + HtmlUtils.importJS(
                                             "http://js-kit.com/ratings.js");

            sb.append(
                HtmlUtils.table(
                    HtmlUtils.row(
                        HtmlUtils.col(
                            ratings,
                            HtmlUtils.attr(
                                HtmlUtils.ATTR_ALIGN,
                                HtmlUtils.VALUE_RIGHT)), HtmlUtils.attr(
                                    HtmlUtils.ATTR_VALIGN,
                                    HtmlUtils.VALUE_TOP)), HtmlUtils.attr(
                                        HtmlUtils.ATTR_WIDTH, "100%")));
        }





        if ( !doRatings) {
            sb.append(HtmlUtils.p());
        }
        sb.append(getPageHandler().getCommentHtml(request, entry));


        return getEntryManager().addEntryHeader(request, entry,
                new OutputHandler(getRepository(),
                                  "tmp").makeLinksResult(request,
                                      msg("Entry Comments"), sb,
                                      new OutputHandler.State(entry)));
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
    public List<Comment> getComments(Request request, final Entry entry)
            throws Exception {
        if (entry.getComments() != null) {
            return entry.getComments();
        }
        if (entry.isDummy()) {
            return new ArrayList<Comment>();
        }
        List<Comment> comments = entry.getTypeHandler().getComments(request,
                                     entry);
        if (comments != null) {
            return comments;
        }
        final List<Comment> theComments = new ArrayList<Comment>();
        comments = theComments;

        Statement stmt =
            getDatabaseManager().select(
                Tables.COMMENTS.COLUMNS, Tables.COMMENTS.NAME,
                Clause.eq(Tables.COMMENTS.COL_ENTRY_ID, entry.getId()),
                getDatabaseManager().makeOrderBy(Tables.COMMENTS.COL_DATE));
        getDatabaseManager().iterate(stmt, new SqlUtil.ResultsHandler() {
            public boolean handleResults(ResultSet results) throws Exception {
                theComments.add(
                    new Comment(
                        results.getString(1), entry,
                        getUserManager().findUser(
                            results.getString(3),
                            true), getDatabaseManager().getDate(results, 4),
                                   results.getString(5),
                                   results.getString(6)));

                return true;
            }

        });
        entry.setComments(comments);

        return comments;
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
    public Result processCommentsEdit(Request request) throws Exception {
        Entry entry = getEntryManager().getEntry(request);
        //TODO: actually support comment editing
        request.ensureAuthToken();
        getDatabaseManager().delete(
            Tables.COMMENTS.NAME,
            Clause.eq(
                Tables.COMMENTS.COL_ID,
                request.getUnsafeString(ARG_COMMENT_ID, BLANK)));
        entry.setComments(null);

        return new Result(request.url(getRepository().URL_COMMENTS_SHOW,
                                      ARG_ENTRYID, entry.getId(),
                                      ARG_MESSAGE,
                                      getRepository().translate(request,
                                          "Comment deleted")));
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
    public Result processCommentsAdd(Request request) throws Exception {
        Entry entry = getEntryManager().getEntry(request);
        if (request.exists(ARG_CANCEL)) {
            return new Result(request.url(getRepository().URL_COMMENTS_SHOW,
                                          ARG_ENTRYID, entry.getId()));
        }

        StringBuffer sb = new StringBuffer();
        request.appendMessage(sb);


        String subject = BLANK;
        String comment = BLANK;
        subject = request.getEncodedString(ARG_SUBJECT, BLANK).trim();
        comment = request.getEncodedString(ARG_COMMENT, BLANK).trim();
        if (comment.length() == 0) {
            sb.append(
                getPageHandler().showDialogNote(
                    msg("Please enter a comment")));
        } else {
            request.ensureAuthToken();
            getDatabaseManager().executeInsert(Tables.COMMENTS.INSERT,
                    new Object[] {
                getRepository().getGUID(), entry.getId(),
                request.getUser().getId(), new Date(), subject, comment
            });
            //Now clear out the comments in the cached entry
            entry.setComments(null);

            return getEntryManager().addEntryHeader(request, entry,
                    new Result(request.url(getRepository().URL_COMMENTS_SHOW,
                                           ARG_ENTRYID, entry.getId(),
                                           ARG_MESSAGE,
                                           getRepository().translate(request,
                                               "Comment added"))));
        }

        sb.append(msgLabel("Add comment for")
                  + getEntryManager().getEntryLink(request, entry));
        //        sb.append(request.form(getRepository().URL_COMMENTS_ADD, BLANK));
        sb.append(request.form(getRepository().URL_COMMENTS_ADD, BLANK));
        getRepository().addAuthToken(request, sb);
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.formEntry(msgLabel("Subject"),
                                      HtmlUtils.input(ARG_SUBJECT, subject,
                                          HtmlUtils.SIZE_40)));
        sb.append(HtmlUtils.formEntryTop(msgLabel("Comment"),
                                         HtmlUtils.textArea(ARG_COMMENT,
                                             comment, 5, 40)));
        sb.append(
            HtmlUtils.formEntry(
                BLANK,
                HtmlUtils.buttons(
                    HtmlUtils.submit(msg("Add Comment")),
                    HtmlUtils.submit(msg("Cancel"), ARG_CANCEL))));
        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.formClose());

        return getEntryManager().addEntryHeader(request, entry,
                new OutputHandler(getRepository(),
                                  "tmp").makeLinksResult(request,
                                      msg("Entry Comments"), sb,
                                      new OutputHandler.State(entry)));
    }



}
