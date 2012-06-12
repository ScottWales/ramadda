/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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
import org.ramadda.repository.database.*;

import ucar.unidata.sql.Clause;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;

import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;


import ucar.unidata.xml.XmlUtil;

import java.io.File;


import java.io.UnsupportedEncodingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


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
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class MonitorManager extends RepositoryManager implements EntryChecker {

    /** _more_ */
    private List<EntryMonitor> monitors = new ArrayList<EntryMonitor>();




    /**
     * _more_
     *
     * @param repository _more_
     */
    public MonitorManager(Repository repository) {
        super(repository);
        repository.addEntryChecker(this);
        try {
            initMonitors();
        } catch (Exception exc) {
            exc.printStackTrace();
            //            throw new RuntimeException(exc);
        }
    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void initMonitors() throws Exception {
        Statement stmt =
            getDatabaseManager().select(Tables.MONITORS.COL_ENCODED_OBJECT,
                                        Tables.MONITORS.NAME, new Clause(),
                                        " order by "
                                        + Tables.MONITORS.COL_NAME);
        SqlUtil.Iterator iter = getDatabaseManager().getIterator(stmt);
        ResultSet        results;
        while ((results = iter.getNext()) != null) {
            String       xml     = results.getString(1);
            EntryMonitor monitor =
                (EntryMonitor) Repository.decodeObject(xml);
            if (monitor != null) {
                monitor.setRepository(getRepository());
                monitors.add(monitor);
            } else {
                /*
                System.err.println ("could not create monitor:" + xml);
                System.err.println ("messages:" + xmlEncoder.getErrorMessages());
                for(Exception exc: (List<Exception>)xmlEncoder.getExceptions()) {
                    exc.printStackTrace();
                }
                */
            }
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<EntryMonitor> getEntryMonitors() {
        return monitors;
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
    public Result processEntryListen(Request request) throws Exception {
        SynchronousEntryMonitor entryMonitor =
            new SynchronousEntryMonitor(getRepository(), request);
        synchronized (monitors) {
            monitors.add(entryMonitor);
        }
        synchronized (entryMonitor) {
            entryMonitor.wait();
            System.err.println("Done waiting");
        }
        Entry entry = entryMonitor.getEntry();
        if (entry == null) {
            System.err.println("No entry");
            return new Result(BLANK, new StringBuffer("No match"),
                              getRepository().getMimeTypeFromSuffix(".html"));
        }
        return getRepository().getOutputHandler(request).outputEntry(request,
                request.getOutput(), entry);
    }


    /**
     * _more_
     *
     * @param entries _more_
     */
    public void entriesModified(final List<Entry> entries) {}

    /**
     * _more_
     *
     * @param entryIds _more_
     */
    public void entriesDeleted(List<String> entryIds) {}


    /**
     * _more_
     *
     * @param entries _more_
     */
    public void entriesCreated(final List<Entry> entries) {
        Misc.run(new Runnable() {
            public void run() {
                checkNewEntriesInner(entries);
            }
        });
    }

    /**
     * _more_
     *
     * @param entries _more_
     */
    private void checkNewEntriesInner(List<Entry> entries) {
        try {
            List<EntryMonitor> tmpMonitors;
            synchronized (monitors) {
                tmpMonitors = new ArrayList<EntryMonitor>(monitors);
            }
            for (Entry entry : entries) {
                //                System.err.println("check entry: " + entry);
                for (EntryMonitor entryMonitor : tmpMonitors) {
                    entryMonitor.checkEntry(entry);
                }
            }
        } catch (Exception exc) {
            System.err.println("Error checking monitors:" + exc);
            exc.printStackTrace();
        }
    }



    /**
     * _more_
     *
     * @param monitor _more_
     *
     * @throws Exception _more_
     */
    private void deleteMonitor(EntryMonitor monitor) throws Exception {
        monitors.remove(monitor);
        if ( !monitor.getEditable()) {
            return;
        }
        getDatabaseManager().delete(Tables.MONITORS.NAME,
                                    Clause.eq(Tables.MONITORS.COL_MONITOR_ID,
                                        monitor.getId()));

    }

    /**
     * _more_
     *
     * @param monitor _more_
     *
     * @throws Exception _more_
     */
    private void insertMonitor(EntryMonitor monitor) throws Exception {
        String xml = Repository.encodeObject(monitor);
        getDatabaseManager().executeInsert(Tables.MONITORS.INSERT,
                                           new Object[] {
            monitor.getId(), monitor.getName(), monitor.getUser().getId(),
            monitor.getFromDate(), monitor.getToDate(), xml
        });
    }

    /**
     * _more_
     *
     * @param monitor _more_
     *
     * @throws Exception _more_
     */
    private void addNewMonitor(EntryMonitor monitor) throws Exception {
        monitors.add(monitor);
        if ( !monitor.getEditable()) {
            return;
        }
        insertMonitor(monitor);
    }

    /**
     * _more_
     *
     * @param monitor _more_
     *
     * @throws Exception _more_
     */
    private void updateMonitor(EntryMonitor monitor) throws Exception {
        if ( !monitor.getEditable()) {
            return;
        }
        getDatabaseManager().delete(Tables.MONITORS.NAME,
                                    Clause.eq(Tables.MONITORS.COL_MONITOR_ID,
                                        monitor.getId()));


        insertMonitor(monitor);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param monitor _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processMonitorEdit(Request request, EntryMonitor monitor)
            throws Exception {

        if (request.exists(ARG_MONITOR_DELETE_CONFIRM)) {
            request.ensureAuthToken();
            deleteMonitor(monitor);
            return new Result(
                request.url(
                    getRepositoryBase().URL_USER_MONITORS, ARG_MESSAGE,
                    getRepository().translate(request, "Monitor deleted")));
        }

        if (request.exists(ARG_MONITOR_CHANGE)) {
            request.ensureAuthToken();
            monitor.applyEditForm(request);
            updateMonitor(monitor);
            return new Result(
                HtmlUtil.url(
                    getRepositoryBase().URL_USER_MONITORS.toString(),
                    ARG_MONITOR_ID, monitor.getId()));
        }

        StringBuffer sb = new StringBuffer();
        String listLink =
            HtmlUtil.href(getRepositoryBase().URL_USER_MONITORS.toString(),
                          msg("Monitor List"));
        sb.append(HtmlUtil.p());
        sb.append(HtmlUtil.center(HtmlUtil.b(listLink)));

        sb.append(msgLabel("Monitor"));
        sb.append(HtmlUtil.space(1));
        sb.append(monitor.getName());
        request.formPostWithAuthToken(sb,
                                      getRepositoryBase().URL_USER_MONITORS,
                                      HtmlUtil.attr(HtmlUtil.ATTR_NAME,
                                          "monitorform"));
        sb.append(HtmlUtil.hidden(ARG_MONITOR_ID, monitor.getId()));

        if (request.exists(ARG_MONITOR_DELETE)) {
            StringBuffer fb = new StringBuffer();
            fb.append(
                RepositoryUtil.buttons(
                    HtmlUtil.submit(msg("OK"), ARG_MONITOR_DELETE_CONFIRM),
                    HtmlUtil.submit(msg("Cancel"), ARG_CANCEL)));
            sb.append(
                getRepository().showDialogQuestion(
                    msg("Are you sure you want to delete the monitor?"),
                    fb.toString()));
            sb.append(HtmlUtil.formClose());
            return getUserManager().makeResult(request,
                    msg("Monitor Delete"), sb);
        }


        StringBuffer buttons = new StringBuffer();
        buttons.append(HtmlUtil.submit(msg("Edit"), ARG_MONITOR_CHANGE));
        buttons.append(HtmlUtil.space(1));
        buttons.append(HtmlUtil.submit(msg("Delete"), ARG_MONITOR_DELETE));
        sb.append(buttons);
        sb.append(HtmlUtil.br());
        monitor.addToEditForm(request, sb);
        sb.append(buttons);

        sb.append(HtmlUtil.formClose());
        return getUserManager().makeResult(request,
                                           msg("Edit Entry Monitor"), sb);
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
    public Result processMonitorCreate(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        EntryMonitor monitor = new EntryMonitor(getRepository(),
                                   request.getUser(), "New Monitor", true);
        String type = request.getString(ARG_MONITOR_TYPE, "email");
        MonitorAction action = null;
        if (type.equals("email")) {
            action = new EmailAction(getRepository().getGUID());
        } else if (type.equals("twitter")) {
            action = new TwitterAction(getRepository().getGUID());
        } else if (type.equals("ftp")) {
            action = new FtpAction(getRepository().getGUID());
        } else if (type.equals("copy")) {
            action = new CopyAction(getRepository().getGUID());
        } else if (type.equals("ldm")) {
            action = new LdmAction(getRepository().getGUID());
        } else if (type.equals("exec")) {
            if ( !getRepository().getProperty(PROP_MONITOR_ENABLE_EXEC,
                                              false)) {
                throw new IllegalArgumentException(
                                                   "Exec action not enabled");
            }
            action = new ExecAction(getRepository().getGUID());
        } 

        if(action == null) {
            throw new IllegalArgumentException("unknown action type:" + type);
        }

        if(action.adminOnly() && !request.getUser().getAdmin()) {
            throw new IllegalArgumentException(
                                               "You need to be an admin to add an " + type + " action");
        }


        monitor.addAction(action);
        addNewMonitor(monitor);
        return new Result(
            HtmlUtil.url(
                getRepositoryBase().URL_USER_MONITORS.toString(),
                ARG_MONITOR_ID, monitor.getId()));
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param monitor _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean canView(Request request, EntryMonitor monitor)
            throws Exception {
        if (request.getUser().getAdmin()) {
            return true;
        }
        return Misc.equals(monitor.getUser(), request.getUser());
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param monitors _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<EntryMonitor> getEditableMonitors(Request request,
            List<EntryMonitor> monitors)
            throws Exception {
        List<EntryMonitor> result = new ArrayList<EntryMonitor>();
        for (EntryMonitor monitor : monitors) {
            if (monitor.getEditable() && canView(request, monitor)) {
                result.add(monitor);
            }
        }
        return result;
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
    public Result processMonitors(Request request) throws Exception {

        if (request.getUser().getAnonymous()
                || request.getUser().getIsGuest()) {
            throw new IllegalArgumentException("Cannot access monitors");
        }
        StringBuffer sb = new StringBuffer();
        List<EntryMonitor> monitors = getEditableMonitors(request,
                                          getEntryMonitors());
        if (request.exists(ARG_MONITOR_ID)) {
            EntryMonitor monitor = EntryMonitor.findMonitor(monitors,
                                       request.getString(ARG_MONITOR_ID, ""));
            if (monitor == null) {
                throw new IllegalArgumentException(
                    "Could not find entry monitor");
            }
            if ( !monitor.getEditable()) {
                throw new IllegalArgumentException(
                    "Entry monitor is not editable");
            }
            if ( !canView(request, monitor)) {
                throw new IllegalArgumentException(
                    "You are not allowed to edit thr monitor");
            }
            return processMonitorEdit(request, monitor);
        }

        if (request.exists(ARG_MONITOR_CREATE)) {
            return processMonitorCreate(request);
        }

        sb.append(HtmlUtil.br());
        String ldmCreate  = "";
        String execCreate = "";
        if (request.getUser().getAdmin()) {
            ldmCreate = request.form(getRepositoryBase().URL_USER_MONITORS)
                        + HtmlUtil.submit("LDM Action", ARG_MONITOR_CREATE)
                        + HtmlUtil.hidden(ARG_MONITOR_TYPE, "ldm")
                        + HtmlUtil.formClose();
            if (getRepository().getProperty(PROP_MONITOR_ENABLE_EXEC,
                                            false)) {
                execCreate =
                    request.form(getRepositoryBase().URL_USER_MONITORS)
                    + HtmlUtil.submit("Exec Action", ARG_MONITOR_CREATE)
                    + HtmlUtil.hidden(ARG_MONITOR_TYPE, "exec")
                    + HtmlUtil.formClose();
            }
        }

        String[] createTypesxxx = {
            "email", "Email Action", "twitter", "Twitter Action", "copy",
            "Copy Action", "ftp", "FTP Action"
        };


        String[] createTypes = { "email", "Email Action", "twitter",
                                 "Twitter Action" };

        sb.append(HtmlUtil.open(HtmlUtil.TAG_TABLE));
        sb.append(HtmlUtil.open(HtmlUtil.TAG_TR));
        for (int i = 0; i < createTypes.length; i += 2) {
            String form =
                request.form(getRepositoryBase().URL_USER_MONITORS)
                + HtmlUtil.submit(createTypes[i + 1], ARG_MONITOR_CREATE)
                + HtmlUtil.hidden(ARG_MONITOR_TYPE, createTypes[i])
                + HtmlUtil.formClose();
            sb.append(HtmlUtil.col(form));
        }
        sb.append(HtmlUtil.col(ldmCreate));
        sb.append(HtmlUtil.col(execCreate));

        sb.append(HtmlUtil.close(HtmlUtil.TAG_TR));
        sb.append(HtmlUtil.close(HtmlUtil.TAG_TABLE));

        sb.append(HtmlUtil.p());

        sb.append(HtmlUtil.open(HtmlUtil.TAG_TABLE,
                                HtmlUtil.attrs(HtmlUtil.ATTR_CELLPADDING,
                                    "4", HtmlUtil.ATTR_CELLSPACING, "0")));
        if (monitors.size() > 0) {
            sb.append(HtmlUtil.row(HtmlUtil.cols("", boldMsg("Monitor"),
                    boldMsg("User"), boldMsg("Search Criteria"),
                    boldMsg("Action"))));
        }
        for (EntryMonitor monitor : monitors) {
            sb.append(HtmlUtil.open(HtmlUtil.TAG_TR,
                                    HtmlUtil.attr(HtmlUtil.ATTR_VALIGN,
                                        "top") + ( !monitor.isActive()
                    ? HtmlUtil.attr(HtmlUtil.ATTR_BGCOLOR, "#cccccc")
                    : "")));
            sb.append(HtmlUtil.open(HtmlUtil.TAG_TD));
            sb.append(
                HtmlUtil.href(
                    HtmlUtil.url(
                        getRepositoryBase().URL_USER_MONITORS.toString(),
                        ARG_MONITOR_ID, monitor.getId()), HtmlUtil.img(
                            iconUrl(ICON_EDIT))));
            sb.append(HtmlUtil.space(1));
            sb.append(
                HtmlUtil.href(
                    HtmlUtil.url(
                        getRepositoryBase().URL_USER_MONITORS.toString(),
                        ARG_MONITOR_DELETE, "true", ARG_MONITOR_ID,
                        monitor.getId()), HtmlUtil.img(
                            iconUrl(ICON_DELETE))));
            if ( !monitor.isActive()) {
                sb.append(HtmlUtil.space(1));
                sb.append(msg("not active"));
            }
            sb.append(HtmlUtil.close(HtmlUtil.TAG_TD));


            sb.append(HtmlUtil.col(monitor.getName()));
            sb.append(HtmlUtil.col(monitor.getUser().getLabel()));
            sb.append(HtmlUtil.col(monitor.getSearchSummary()));
            sb.append(HtmlUtil.col(monitor.getActionSummary()));
            sb.append(HtmlUtil.close(HtmlUtil.TAG_TR));

            if ((monitor.getLastError() != null)
                    && (monitor.getLastError().length() > 0)) {
                String msg = HtmlUtil.makeShowHideBlock(
                                 HtmlUtil.span(
                                     msg("Error"),
                                     HtmlUtil.cssClass(
                                         "errorlabel")), HtmlUtil.pre(
                                             monitor.getLastError()), false);
                sb.append(HtmlUtil.row(HtmlUtil.colspan(msg, 5)));
            }

            sb.append(HtmlUtil.row(HtmlUtil.colspan(HtmlUtil.hr(), 5)));

        }
        sb.append(HtmlUtil.close(HtmlUtil.TAG_TABLE));

        return getUserManager().makeResult(request, msg("Entry Monitors"),
                                           sb);

    }




}
