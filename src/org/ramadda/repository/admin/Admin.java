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

package org.ramadda.repository.admin;


import org.ramadda.repository.*;

import org.ramadda.repository.auth.*;
import org.ramadda.repository.database.*;
import org.ramadda.repository.ftp.FtpManager;

import org.ramadda.repository.harvester.*;

import org.ramadda.repository.output.*;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.sql.Clause;

import ucar.unidata.sql.SqlUtil;

import ucar.unidata.util.Counter;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;



import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.lang.management.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;

import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;


import javax.mail.internet.MimeMessage;



/**
 * Class Admin
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class Admin extends RepositoryManager {

    /** _more_ */
    public static final String ARG_ADMIN_ADMINCREATED = "admin.admincreated";

    /** _more_ */
    public static final String ARG_ADMIN_INSTALLCOMPLETE =
        "admin.installcomplete";

    /** _more_ */
    public static final String ARG_ADMIN_INSTALLNOTICESHOWN =
        "admin.installnoticeshown";

    /** _more_ */
    public static final String ARG_ADMIN_INSTALLPLUGIN =
        "admin.installplugin";

    /** _more_ */
    public static final String ARG_ADMIN_LICENSEREAD = "admin.licenseread";




    /** _more_ */
    public RequestUrl URL_ADMIN_SQL = new RequestUrl(this, "/admin/sql",
                                          "SQL");



    /** _more_ */
    public RequestUrl URL_ADMIN_CLEANUP = new RequestUrl(this,
                                              "/admin/cleanup",
                                              "Maintenance");


    /** _more_ */
    public RequestUrl URL_ADMIN_STARTSTOP = new RequestUrl(this,
                                                "/admin/startstop",
                                                "Database");


    /** _more_ */
    public RequestUrl URL_ADMIN_SETTINGS = new RequestUrl(this,
                                               "/admin/settings", "Settings");


    /** _more_ */
    public RequestUrl URL_ADMIN_PLUGIN_UPLOAD = new RequestUrl(this,
                                                    "/admin/plugin/upload",
                                                    "Upload Plugin");

    /** _more_ */
    public RequestUrl URL_ADMIN_SETTINGS_DO = new RequestUrl(this,
                                                  "/admin/settings/do",
                                                  "Settings");

    /** _more_ */
    public RequestUrl URL_ADMIN_TABLES = new RequestUrl(this,
                                             "/admin/tables", "Database");


    /** _more_ */
    public RequestUrl URL_ADMIN_DUMPDB = new RequestUrl(this,
                                             "/admin/dumpdb",
                                             "Dump Database");

    /** _more_ */
    public RequestUrl URL_ADMIN_STATS = new RequestUrl(this, "/admin/stats",
                                            "System");

    /** _more_ */
    public RequestUrl URL_ADMIN_ACCESS = new RequestUrl(this,
                                             "/admin/access", "Access");


    /** _more_ */
    public RequestUrl URL_ADMIN_LOG = new RequestUrl(this, "/admin/log",
                                          "Logs");

    /** _more_ */
    public RequestUrl URL_ADMIN_STACK = new RequestUrl(this, "/admin/stack",
                                            "Stack");


    /** _more_ */
    public List<RequestUrl> adminUrls =
        RepositoryUtil.toList(new RequestUrl[] {
        URL_ADMIN_SETTINGS, getRepositoryBase().URL_USER_LIST,
        URL_ADMIN_STATS, URL_ADMIN_ACCESS,
        getHarvesterManager().URL_HARVESTERS_LIST,
        getRegistryManager().URL_REGISTRY_REMOTESERVERS,
        /*URL_ADMIN_STARTSTOP,*/
        /*URL_ADMIN_TABLES, */
        URL_ADMIN_LOG, URL_ADMIN_STACK, URL_ADMIN_CLEANUP
    });


    /** _more_ */
    public static final String BLOCK_SITE = "block.site";

    /** _more_ */
    public static final String BLOCK_ACCESS = "block.access";

    /** _more_ */
    public static final String BLOCK_DISPLAY = "block.display";



    /** _more_ */
    int cleanupTS = 0;

    /** _more_ */
    boolean runningCleanup = false;

    /** _more_ */
    StringBuffer cleanupStatus = new StringBuffer();

    /** _more_ */
    private List<AdminHandler> adminHandlers = new ArrayList<AdminHandler>();

    /** _more_ */
    private Hashtable<String, AdminHandler> adminHandlerMap =
        new Hashtable<String, AdminHandler>();


    /**
     * _more_
     *
     * @param repository _more_
     */
    public Admin(Repository repository) {
        super(repository);

    }


    /**
     * _more_
     */
    public void doFinalInitialization() {
        if (getRepository().getProperty(PROP_ADMIN_INCLUDESQL, false)) {
            adminUrls.add(URL_ADMIN_SQL);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getInstallationComplete() {
        return getRepository().getDbProperty(ARG_ADMIN_INSTALLCOMPLETE,
                                             false);
    }




    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public AdminHandler getAdminHandler(String id) {
        return adminHandlerMap.get(id);
    }

    /**
     * _more_
     *
     * @param adminHandler _more_
     */
    public void addAdminHandler(AdminHandler adminHandler) {
        if (adminHandlers.contains(adminHandler)) {
            return;
        }
        if (adminHandlerMap.get(adminHandler.getId()) != null) {
            return;
        }
        adminHandlers.add(adminHandler);
        adminHandlerMap.put(adminHandler.getId(), adminHandler);
        List<RequestUrl> urls = adminHandler.getAdminUrls();
        if (urls != null) {
            adminUrls.addAll(urls);
        }
    }




    /**
     * _more_
     *
     * @param what _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private boolean haveDone(String what) throws Exception {
        return getRepository().getDbProperty(what, false);
    }

    /**
     * _more_
     *
     * @param what _more_
     *
     * @throws Exception _more_
     */
    private void didIt(String what) throws Exception {
        getRepository().writeGlobal(what, "true");
    }





    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private StringBuffer getLicenseForm() throws Exception {
        StringBuffer sb      = new StringBuffer();
        String       license = getStorageManager().readSystemResource(
                             "/org/ramadda/repository/resources/license.txt");
        sb.append(HtmlUtils.textArea("", license, 20, 75));
        sb.append("<p>");
        sb.append(HtmlUtils.open(HtmlUtils.TAG_DIV,
                                 HtmlUtils.cssClass(CSS_CLASS_HIGHLIGHT)));
        sb.append(HtmlUtils.checkbox("agree", "1"));
        sb.append(HtmlUtils.space(1));
        sb.append(
            "I agree to the above terms and conditions of use of the RAMADDA software");
        sb.append(HtmlUtils.close(HtmlUtils.TAG_DIV));
        sb.append("<p>");

        return sb;
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
    public Result doInitialization(Request request) throws Exception {

        StringBuffer sb    = new StringBuffer();
        String       title = "";

        if (Misc.equals("1", request.getString("agree", ""))) {
            didIt(ARG_ADMIN_LICENSEREAD);
        }

        if ( !haveDone(ARG_ADMIN_INSTALLNOTICESHOWN)) {
            title = "Installation";
            sb.append(HtmlUtils.formTable());
            sb.append(
                "<p>Thank you for installing the RAMADDA Repository. <p>Listed below is the RAMADDA home directory and database information. If you want to change these settings please consult the <a target=\"other\" href=\"" + HELP_ROOT + "/userguide/installing.html\">documentation</a> before continuing with the installation process.");
            getStorageManager().addInfo(sb);
            getDatabaseManager().addInfo(sb);
            sb.append(HtmlUtils.formEntry("", HtmlUtils.submit(msg("Next"))));
            sb.append(HtmlUtils.formTableClose());
            didIt(ARG_ADMIN_INSTALLNOTICESHOWN);
        } else if ( !haveDone(ARG_ADMIN_LICENSEREAD)) {
            title = "License";
            sb.append(getLicenseForm());
            sb.append(HtmlUtils.submit(msg("Next")));
        } else if ( !haveDone(ARG_ADMIN_ADMINCREATED)) {
            title = "Administrator";
            String       id          = "admin";
            String       name        = "Administrator";

            boolean      triedOnce   = false;
            StringBuffer errorBuffer = new StringBuffer();
            if (request.exists(UserManager.ARG_USER_ID)) {
                triedOnce = true;
                id = request.getString(UserManager.ARG_USER_ID, "").trim();
                name      = request.getString(UserManager.ARG_USER_NAME,
                                         "").trim();
                String password1 =
                    request.getString(UserManager.ARG_USER_PASSWORD1,
                                      "").trim();
                String password2 =
                    request.getString(UserManager.ARG_USER_PASSWORD2,
                                      "").trim();
                boolean okToAdd = true;
                if (id.length() == 0) {
                    okToAdd = false;
                    errorBuffer.append(HtmlUtils.space(2));
                    errorBuffer.append(msg("Please enter an ID"));
                    errorBuffer.append(HtmlUtils.br());
                }

                if ((password1.length() == 0)
                        || !password1.equals(password2)) {
                    okToAdd = false;
                    errorBuffer.append(HtmlUtils.space(2));
                    errorBuffer.append(msg("Invalid password"));
                    errorBuffer.append(HtmlUtils.br());
                }


                if (okToAdd) {
                    User user = new User(
                                    id, name,
                                    request.getString(
                                        UserManager.ARG_USER_EMAIL,
                                        "").trim(), "", "",
                                            getUserManager().hashPassword(
                                                password1), true, "", "",
                                                    false, null);
                    getUserManager().makeOrUpdateUser(user, false);
                    didIt(ARG_ADMIN_ADMINCREATED);
                    didIt(ARG_ADMIN_INSTALLCOMPLETE);

                    String[] propArgs = new String[] { PROP_REPOSITORY_NAME,
                            PROP_HOSTNAME, PROP_PORT, PROP_REPOSITORY_NAME,
                            PROP_REPOSITORY_DESCRIPTION };


                    for (String propArg : propArgs) {
                        if (request.defined(propArg)) {
                            getRepository().writeGlobal(propArg,
                                    request.getString(propArg, "").trim());
                        }
                    }

                    if (request.defined(UserManager.ARG_USER_EMAIL)) {
                        getRepository().writeGlobal(PROP_ADMIN_EMAIL,
                                request.getString(UserManager.ARG_USER_EMAIL,
                                    ""));
                    }

                    //NOT NOW:
                    //                    getRegistryManager().applyInstallForm(request);

                    sb.append(
                        getRepository().showDialogNote(
                            "Initial configuration process is complete."));
                    sb.append(HtmlUtils.p());

                    Entry topEntry = getEntryManager().getTopGroup();
                    topEntry.setName(request.getString(PROP_REPOSITORY_NAME,
                            topEntry.getName()));
                    String description  = null;
                    File   initDescFile =
                        new File(
                            IOUtil.joinDir(
                                getStorageManager().getRepositoryDir(),
                                "initdescription.txt"));
                    if (initDescFile.exists()) {
                        FileInputStream fis =
                            new FileInputStream(initDescFile);
                        description = IOUtil.readContents(fis);
                        IOUtil.close(fis);
                    }


                    if (description == null) {
                        description = getRepository().getResource(
                            "/org/ramadda/repository/resources/examples/initdescription.txt");
                    }

                    description = description.replace("${topid}",
                            topEntry.getId());
                    description = description.replace("${root}",
                            getRepository().getUrlBase());
                    topEntry.setDescription(description);
                    getEntryManager().updateEntry(topEntry);

                    //NOT NOW
                    //getRegistryManager().doFinalInitialization();

                    //Make sure we do this now before we do the final init entries
                    if (request.get(ARG_ADMIN_INSTALLPLUGIN, false)) {
                        String plugin =
                            "/org/ramadda/repository/resources/plugins/allplugins.jar";
                        getRepository().installPlugin(plugin);
                    }

                    String initEntriesXml = null;
                    File   initFile       =
                        new File(
                            IOUtil.joinDir(
                                getStorageManager().getRepositoryDir(),
                                "initentries.xml"));
                    if (initFile.exists()) {
                        FileInputStream fis = new FileInputStream(initFile);
                        initEntriesXml = IOUtil.readContents(fis);
                        IOUtil.close(fis);
                    }

                    if (initEntriesXml == null) {
                        initEntriesXml = getRepository().getResource(
                            "/org/ramadda/repository/resources/examples/initentries.xml");
                    }
                    Element     root       = XmlUtil.getRoot(initEntriesXml);
                    Request tmpRequest = getRepository().getRequest(user);
                    System.err.println("processing initEntries");
                    List<Entry> newEntries =
                        getEntryManager().processEntryXml(
                                                          tmpRequest, root,
                            new Hashtable<String, Entry>(),
                            new Hashtable<String, String>(), null);

                    for (Entry entry : newEntries) {
                        entry.getTypeHandler().doFinalInitialization(tmpRequest, entry);
                    }
                    sb.append(getUserManager().makeLoginForm(request));


                    if (errorBuffer.length() > 0) {
                        sb.append(
                            getRepository().showDialogError(
                                msg("Error") + "<br>" + errorBuffer));
                    }

                    return new Result("", sb);
                }
            }

            if (errorBuffer.length() > 0) {
                sb.append(getRepository().showDialogError(msg("Error")
                        + "<br>" + errorBuffer));
            }
            sb.append("Please enter the following information.");
            sb.append(
                " This information is used to configure your RAMADDA server and is not sent anywhere.");
            String required1 =
                " <span class=\"ramadda-required-field\">* required</span>";
            String required2 =
                " <span class=\"ramadda-required-field\">*</span>";
            sb.append(request.form(getRepository().URL_INSTALL));
            sb.append(HtmlUtils.formTable());
            sb.append(HtmlUtils.colspan(msgHeader("Administrator Login"), 2));
            sb.append(
                HtmlUtils.formEntry(
                    msgLabel("ID"),
                    HtmlUtils.input(UserManager.ARG_USER_ID, id)
                    + required1));
            sb.append(
                HtmlUtils.formEntry(
                    msgLabel("Name"),
                    HtmlUtils.input(UserManager.ARG_USER_NAME, name)));
            sb.append(
                HtmlUtils.formEntry(
                    msgLabel("Email"),
                    HtmlUtils.input(
                        UserManager.ARG_USER_EMAIL,
                        request.getString(UserManager.ARG_USER_EMAIL, ""))));
            sb.append(
                HtmlUtils.formEntry(
                    msgLabel("Password"),
                    HtmlUtils.password(UserManager.ARG_USER_PASSWORD1)
                    + required2));
            sb.append(
                HtmlUtils.formEntry(
                    msgLabel("Password Again"),
                    HtmlUtils.password(UserManager.ARG_USER_PASSWORD2)
                    + required2));

            sb.append(HtmlUtils.colspan(msgHeader("Server Information"), 2));
            String hostname = "";
            String port     = "";
            if (request.getHttpServletRequest() != null) {
                hostname = request.getHttpServletRequest().getServerName();
                port = "" + request.getHttpServletRequest().getServerPort();
            }
            hostname = request.getString(PROP_HOSTNAME, hostname);
            port     = request.getString(PROP_PORT, port);

            sb.append(
                HtmlUtils.formEntry(
                    msgLabel("Repository Title"),
                    HtmlUtils.input(
                        PROP_REPOSITORY_NAME,
                        request.getString(
                            PROP_REPOSITORY_NAME,
                            getRepository().getProperty(
                                PROP_REPOSITORY_NAME,
                                "RAMADDA Repository")), HtmlUtils.SIZE_60)));
            sb.append(
                HtmlUtils.formEntryTop(
                    msgLabel("Description"),
                    HtmlUtils.textArea(
                        PROP_REPOSITORY_DESCRIPTION,
                        getProperty(PROP_REPOSITORY_DESCRIPTION, ""), 5,
                        60)));

            sb.append(HtmlUtils
                .formEntry(msgLabel("Hostname"), HtmlUtils
                    .input(PROP_HOSTNAME, hostname, HtmlUtils
                        .SIZE_60) + " (Use  &quot;ipaddress&quot; for dynamic IPS)"));
            sb.append(HtmlUtils.formEntry(msgLabel("Port"),
                                          HtmlUtils.input(PROP_PORT, port,
                                              HtmlUtils.SIZE_10)));

            sb.append(HtmlUtils.colspan(msgHeader("Plugins"), 2));

            sb.append(
                HtmlUtils.formEntry(
                    "",
                    "RAMADDA comes with a set of plugins that add functionality. You can install them now or later if you wish."));
            //TODO: read the plugins.xml file and offer more plugins
            //than the hard coded all plugin
            sb.append(
                HtmlUtils.formEntry(
                    "",
                    HtmlUtils.checkbox(ARG_ADMIN_INSTALLPLUGIN, "true", true)
                    + " " + "Install all plugins"));



            //NOT NOW
            //sb.append(HtmlUtils.colspan(msgHeader("Registry"), 2));
            //            getRegistryManager().addToInstallForm(request, sb);

            sb.append(HtmlUtils.formTableClose());
            sb.append(HtmlUtils.p());
            sb.append(HtmlUtils.submit(msg("Initialize Server")));
        }

        StringBuffer finalSB = new StringBuffer();
        finalSB.append(request.form(getRepository().URL_INSTALL));
        finalSB.append(msgHeader(title));
        finalSB.append(sb);
        finalSB.append(HtmlUtils.formClose());

        return new Result(msg(title), finalSB);

    }





    /**
     * _more_
     *
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected StringBuffer getDbMetaData() throws Exception {

        Connection connection = getDatabaseManager().getConnection();
        try {
            StringBuffer     sb       = new StringBuffer();
            DatabaseMetaData dbmd     = connection.getMetaData();
            ResultSet        catalogs = dbmd.getCatalogs();
            ResultSet        tables   = dbmd.getTables(null, null, null,
                                   new String[] { "TABLE" });

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");

                //Humm, not sure why I get this table name and its giving me an error
                if (tableName.equals("ENTRY") || tableName.equals("BASE")
                        || tableName.equals("AGGGREGATION")) {
                    continue;
                }


                String tableType = tables.getString("TABLE_TYPE");
                //            System.err.println("table type" + tableType);
                if (Misc.equals(tableType, "INDEX")) {
                    continue;
                }
                if (tableType == null) {
                    continue;
                }

                if ((tableType != null) && tableType.startsWith("SYSTEM")) {
                    continue;
                }




                ResultSet columns = dbmd.getColumns(null, null, tableName,
                                        null);
                String encoded =
                    new String(RepositoryUtil.encodeBase64(("text:?"
                        + tableName).getBytes()));

                int cnt = 0;
                if (tableName.toLowerCase().indexOf("_index_") < 0) {
                    //TODO                    cnt = getDatabaseManager().getCount(tableName,
                    //                            new Clause());
                }
                String tableVar  = null;
                String TABLENAME = tableName.toUpperCase();
                //TODO    sb.append("Table:" + tableName + " (#" + cnt + ")");
                sb.append("Table:" + tableName);
                sb.append("<ul>");
                List colVars = new ArrayList();

                while (columns.next()) {
                    String colName = columns.getString("COLUMN_NAME");
                    String colSize = columns.getString("COLUMN_SIZE");
                    sb.append("<li>");
                    sb.append(colName + " (" + columns.getString("TYPE_NAME")
                              + " " + colSize + ")");
                }

                ResultSet indices = dbmd.getIndexInfo(null, null, tableName,
                                        false, true);
                boolean didone = false;
                while (indices.next()) {
                    if ( !didone) {
                        //                            sb.append(
                        //                                "<br><b>Indices</b> (name,order,type,pages)<br>");
                        sb.append("<br><b>Indices</b><br>");
                    }
                    didone = true;
                    String indexName  = indices.getString("INDEX_NAME");
                    String asc        = indices.getString("ASC_OR_DESC");
                    int    type       = indices.getInt("TYPE");
                    String typeString = "" + type;
                    int    pages      = indices.getInt("PAGES");
                    if (type == DatabaseMetaData.tableIndexClustered) {
                        typeString = "clustered";
                    } else if (type == DatabaseMetaData.tableIndexHashed) {
                        typeString = "hashed";
                    } else if (type == DatabaseMetaData.tableIndexOther) {
                        typeString = "other";
                    }
                    //                        sb.append("Index:" + indexName + "  " + asc + " "
                    //                                  + typeString + " " + pages + "<br>");
                    sb.append("Index:" + indexName + "<br>");


                }

                sb.append("</ul>");
            }

            return sb;
        } finally {
            getDatabaseManager().closeConnection(connection);
        }

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
    private Result processShutdown(Request request) throws Exception {
        if ( !getRepository().getShutdownEnabled()) {
            throw new IllegalStateException("Shutdown not enabled");
        }

        Misc.runInABit(5000, new Runnable() {
            public void run() {
                getRepository().shutdown();
                System.err.println("RAMADDA: exiting");
                System.exit(0);
            }
        });

        return makeResult(request, "Administration",
                          new StringBuffer("Shutting down in 5 seconds"));
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
    public Result adminPluginUpload(Request request) throws Exception {
        request.ensureAuthToken();

        return getRepository().getPluginManager().adminPluginUpload(request);
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
    public Result adminDbStartStop(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(header("Database Administration"));
        String what = request.getString(ARG_ADMIN_WHAT, "nothing");
        if (what.equals("shutdown")) {
            if ( !getDatabaseManager().hasConnection()) {
                sb.append("Not connected to database");
            } else {
                getRepository().getDatabaseManager().shutdown();
                sb.append("Database is shut down");
            }
        } else if (what.equals("restart")) {
            if (getDatabaseManager().hasConnection()) {
                sb.append("Already connected to database");
            } else {
                //TODO:                getRepository().getDatabaseManager().makeConnection();
                sb.append("Database is restarted");
            }
        }
        sb.append("<p>");
        request.formPostWithAuthToken(sb, URL_ADMIN_STARTSTOP,
                                      " name=\"admin\"");

        if ( !getDatabaseManager().hasConnection()) {
            sb.append(HtmlUtils.hidden(ARG_ADMIN_WHAT, "restart"));
            sb.append(HtmlUtils.submit("Restart Database"));
        } else {
            sb.append(HtmlUtils.hidden(ARG_ADMIN_WHAT, "shutdown"));
            sb.append(HtmlUtils.submit("Shut Down Database"));
        }
        sb.append("</form>");

        return makeResult(request, "Administration", sb);

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
    public Result adminActions(Request request) throws Exception {
        StringBuffer    sb         = new StringBuffer();
        List<ApiMethod> apiMethods = getRepository().getApiManager().getApiMethods();
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.row(HtmlUtils.cols("Name", "Admin", "Actions")));
        for (ApiMethod apiMethod : apiMethods) {
            sb.append(HtmlUtils.row(HtmlUtils.cols(apiMethod.getName(),
                    "" + apiMethod.getMustBeAdmin(),
                    StringUtil.join(",", apiMethod.getActions()))));
        }
        sb.append(HtmlUtils.formTableClose());

        return makeResult(request, "Administration", sb);
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
    public Result adminDbTables(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(header("Database Tables"));
        sb.append(getDbMetaData());

        return makeResult(request, "Administration", sb);
    }

    /** _more_ */
    private boolean amDumpingDb = false;

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result adminDbDump(Request request) throws Exception {
        //Only do one at a time
        if (amDumpingDb) {
            StringBuffer sb = new StringBuffer(
                                  getRepository().showDialogWarning(
                                      "Currently exporting the database"));

            return makeResult(request, msg("Database export"), sb);
        }


        ActionManager.Action action = new ActionManager.Action() {
            public void run(Object actionId) throws Exception {
                dumpDatabase(actionId);
            }
        };
        String href = HtmlUtils.href(request.url(URL_ADMIN_CLEANUP),
                                     "Continue");

        Result result = getActionManager().doAction(request, action,
                            "Dumping database", href);

        return result;
    }

    /**
     * _more_
     *
     * @param actionId _more_
     *
     * @throws Exception _more_
     */
    private void dumpDatabase(Object actionId) throws Exception {
        amDumpingDb = true;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
            File tmp = new File(getStorageManager().getBackupsDir() + "/"
                                + "dbdump." + sdf.format(new Date())
                                + ".rdb");
            FileOutputStream     fos = new FileOutputStream(tmp);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            getDatabaseManager().makeDatabaseCopy(bos, true, actionId);
            IOUtil.close(bos);
            IOUtil.close(fos);

            StringBuffer sb = new StringBuffer(
                                  getRepository().showDialogNote(
                                      "Database has been exported to:<br>"
                                      + tmp));
            //            return makeResult(request, msg("Database export"), sb);
        } finally {
            if (actionId != null) {
                getActionManager().actionComplete(actionId);
            }
            amDumpingDb = false;
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param title _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result makeResult(Request request, String title, StringBuffer sb)
            throws Exception {
        StringBuffer headerSB = new StringBuffer();
        addHeader(request, headerSB);
        headerSB.append(sb);
        sb = headerSB;
        Result result = new Result(title, sb);

        return addHeaderToAncillaryPage(request, result);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     */
    public void addHeader(Request request, StringBuffer sb) {
        sb.append(getRepository().makeHeader(request, adminUrls, ""));
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
    public Result adminHome(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(header("Repository Administration"));
        sb.append("<ul>\n");
        sb.append("<li> ");
        sb.append(HtmlUtils.href(request.url(URL_ADMIN_STARTSTOP),
                                 "Administer Database"));
        sb.append("<li> ");
        sb.append(HtmlUtils.href(request.url(URL_ADMIN_TABLES),
                                 "Show Tables"));
        sb.append("<li> ");
        sb.append(HtmlUtils.href(request.url(URL_ADMIN_STATS), "System"));
        sb.append("<li> ");
        sb.append(HtmlUtils.href(request.url(URL_ADMIN_SQL), "Execute SQL"));
        sb.append("</ul>");

        return makeResult(request, "Administration", sb);

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
    public Result adminSettings(Request request) throws Exception {

        StringBuffer sb = new StringBuffer();
        request.formPostWithAuthToken(sb, URL_ADMIN_SETTINGS_DO, null);
        String size = HtmlUtils.SIZE_60;
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit(msg("Change Settings")));
        sb.append(HtmlUtils.br());
        StringBuffer csb = new StringBuffer();
        csb.append(HtmlUtils.formTable());


        csb.append(
            HtmlUtils.row(
                HtmlUtils.colspan(msgHeader("Site Information"), 2)));
        csb.append(HtmlUtils.formEntry(msgLabel("Hostname"),
                                       HtmlUtils.input(PROP_HOSTNAME,
                                           getProperty(PROP_HOSTNAME, ""),
                                           HtmlUtils.SIZE_40)));

        csb.append(HtmlUtils.formEntry(msgLabel("HTTP Port"),
                                       HtmlUtils.input(PROP_PORT,
                                           getProperty(PROP_PORT, ""),
                                           HtmlUtils.SIZE_5)));


        csb.append(
            HtmlUtils.formEntry(
                msgLabel("FTP Port"),
                HtmlUtils.input(
                    PROP_FTP_PORT,
                    getRepository().getProperty(PROP_FTP_PORT, "-1"),
                    HtmlUtils.SIZE_10)));

        csb.append(
            HtmlUtils.formEntry(
                msgLabel("FTP Passive Ports"),
                HtmlUtils.input(
                    PROP_FTP_PASSIVEPORTS,
                    getRepository().getProperty(
                        PROP_FTP_PASSIVEPORTS,
                        FtpManager.DFLT_PASSIVE_PORTS), HtmlUtils.SIZE_15)));




        String allSslCbx =
            HtmlUtils.space(3)
            + HtmlUtils.checkbox(
                PROP_ACCESS_ALLSSL, "true",
                getProperty(PROP_ACCESS_ALLSSL, false)) + " "
                    + msg("Force all connections to be secure");

        String sslMsg =
            "Note: To enable ssl see the <a href=\"http://ramadda.org/repository/userguide/installing.html#ssl\">installation guide</a>";
        csb.append(
            HtmlUtils.formEntryTop(
                msgLabel("SSL"),
                allSslCbx + HtmlUtils.br()
                + getRepository().showDialogNote(sslMsg)));





        csb.append(HtmlUtils.row(HtmlUtils.colspan(msgHeader("Email"), 2)));
        csb.append(HtmlUtils.formEntry(msgLabel("Administrator Email"),
                                       HtmlUtils.input(PROP_ADMIN_EMAIL,
                                           getProperty(PROP_ADMIN_EMAIL, ""),
                                           HtmlUtils.SIZE_40)));

        csb.append(
            HtmlUtils.formEntry(
                msgLabel("Mail Server"), HtmlUtils.input(
                    PROP_ADMIN_SMTP, getProperty(
                        PROP_ADMIN_SMTP, ""), HtmlUtils.SIZE_40) + " "
                            + msg("For sending password reset messages")));



        csb.append(
            HtmlUtils.row(
                HtmlUtils.colspan(msgHeader("Extra Properties"), 2)));
        csb.append(
            HtmlUtils.formEntryTop(
                msgLabel("Properties"),
                HtmlUtils.textArea(
                    PROP_PROPERTIES,
                    getProperty(
                        PROP_PROPERTIES,
                        "#add extra properties\n#name=value\n"), 5, 60)));

        getRepository().getRegistryManager().addAdminConfig(request, csb);


        StringBuffer dsb = new StringBuffer();

        dsb.append(HtmlUtils.formTable());
        dsb.append(HtmlUtils.formEntry(msgLabel("Title"),
                                       HtmlUtils.input(PROP_REPOSITORY_NAME,
                                           getProperty(PROP_REPOSITORY_NAME,
                                               "Repository"), size)));
        dsb.append(
            HtmlUtils.formEntryTop(
                msgLabel("Description"),
                HtmlUtils.textArea(
                    PROP_REPOSITORY_DESCRIPTION,
                    getProperty(PROP_REPOSITORY_DESCRIPTION, ""), 5, 60)));

        dsb.append(
            HtmlUtils.formEntryTop(
                msgLabel("Footer"),
                HtmlUtils.textArea(
                    PROP_HTML_FOOTER, getProperty(PROP_HTML_FOOTER, ""), 5,
                    60)));

        dsb.append(HtmlUtils.formEntry(msgLabel("Logo Image Location"),
                                       HtmlUtils.input(PROP_LOGO_IMAGE,
                                           getProperty(PROP_LOGO_IMAGE, ""),
                                           size)));
        dsb.append(HtmlUtils.formEntry(msgLabel("Logo URL"),
                                       HtmlUtils.input(PROP_LOGO_URL,
                                           getProperty(PROP_LOGO_URL, ""),
                                           size)));



        dsb.append(HtmlUtils.formEntry("", msg("System Message")));
        String systemMessage = getRepository().getSystemMessage();

        dsb.append(
            HtmlUtils.formEntry(
                msgLabel("Message"),
                HtmlUtils.textArea(
                    PROP_SYSTEM_MESSAGE, ((systemMessage == null)
                                          ? ""
                                          : systemMessage), 5, 60)));


        String phrases = getProperty(PROP_ADMIN_PHRASES, (String) null);
        if (phrases == null) {
            phrases = "#label=new label to use\n#e.g.: Foo=Bar";
        }

        dsb.append(HtmlUtils.formEntryTop(msgLabel("Ignore Page Styles"),
                                          HtmlUtils.checkbox(PROP_NOSTYLE,
                                              "true",
                                                  getProperty(PROP_NOSTYLE,
                                                      false))));


        dsb.append(
            HtmlUtils.formEntryTop(
                msgLabel("Translations"),
                HtmlUtils.textArea(PROP_ADMIN_PHRASES, phrases, 5, 60)));


        dsb.append(
            HtmlUtils.formEntryTop(
                msgLabel("Facebook Comments API Key"),
                HtmlUtils.input(
                    PROP_FACEBOOK_CONNECT_KEY,
                    getProperty(PROP_FACEBOOK_CONNECT_KEY, ""), size)));
        dsb.append(
            HtmlUtils.formEntryTop(
                msgLabel("Enable Ratings"),
                HtmlUtils.checkbox(
                    PROP_RATINGS_ENABLE, "true",
                    getProperty(PROP_RATINGS_ENABLE, false))));



        dsb.append(HtmlUtils.formEntryTop(msgLabel("Google Maps Keys"), "<table><tr valign=top><td>"
                + HtmlUtils.textArea(PROP_GOOGLEAPIKEYS, getProperty(PROP_GOOGLEAPIKEYS, ""), 5, 80)
                + "</td><td>One per line:<br><i>host domain;apikey</i><br>e.g.:<i>www.yoursite.edu;google api key</i></table>"));





        StringBuffer asb = new StringBuffer();
        asb.append(HtmlUtils.formTable());


        asb.append(HtmlUtils.row(HtmlUtils.colspan(msgHeader("Site Access"),
                2)));
        asb.append(
            HtmlUtils.formEntry(
                "",
                HtmlUtils.checkbox(
                    PROP_ACCESS_ADMINONLY, "true",
                    getProperty(
                        PROP_ACCESS_ADMINONLY, false)) + HtmlUtils.space(2)
                            + msg("Only allows administrators to access the site")));
        asb.append(
            HtmlUtils.formEntry(
                "",
                HtmlUtils.checkbox(
                    PROP_ACCESS_REQUIRELOGIN, "true",
                    getProperty(
                        PROP_ACCESS_REQUIRELOGIN, false)) + HtmlUtils.space(
                            2) + msg("Require login to access the site")));

        asb.append(HtmlUtils.formEntry("",
                                       HtmlUtils.checkbox(PROP_ACCESS_NOBOTS,
                                           "true",
                                           getProperty(PROP_ACCESS_NOBOTS,
                                               false)) + HtmlUtils.space(2)
                                                   + msg("Disallow robots")));



        asb.append(HtmlUtils.colspan(msgHeader("Anonymous Uploads"), 2));
        asb.append(
            HtmlUtils.formEntry(
                msgLabel("Max directory size"),
                HtmlUtils.input(
                    PROP_UPLOAD_MAXSIZEGB,
                    "" + getRepository().getProperty(
                        PROP_UPLOAD_MAXSIZEGB,
                        10.0), HtmlUtils.SIZE_10) + " (GBytes)"));


        asb.append(HtmlUtils.colspan(msgHeader("Cache Size"), 2));
        asb.append(
            HtmlUtils.formEntry(
                msgLabel("Size"),
                HtmlUtils.input(
                    PROP_CACHE_MAXSIZEGB,
                    "" + getRepository().getProperty(
                        PROP_CACHE_MAXSIZEGB,
                        10.0), HtmlUtils.SIZE_10) + " (GBytes)"));



        asb.append(HtmlUtils.colspan(msgHeader("File Access"), 2));
        String fileWidget = HtmlUtils.textArea(PROP_LOCALFILEPATHS,
                                getProperty(PROP_LOCALFILEPATHS, ""), 5, 40);
        String fileLabel =
            msg("Enter one server file system directory per line")
            + HtmlUtils.br()
            + msg("Directories that RAMADDA is allowed to serve files from")
            + " " + "(e.g., from harvesters or the server file view entries)";
        asb.append(HtmlUtils.formEntryTop(msgLabel("File system access"),
                                          "<table><tr valign=top><td>"
                                          + fileWidget + "</td><td>"
                                          + fileLabel
                                          + "</td></tr></table>"));









        for (AdminHandler adminHandler : adminHandlers) {
            adminHandler.addToAdminSettingsForm(BLOCK_SITE, csb);
            adminHandler.addToAdminSettingsForm(BLOCK_DISPLAY, dsb);
            adminHandler.addToAdminSettingsForm(BLOCK_ACCESS, asb);
        }
        csb.append(HtmlUtils.formTableClose());
        dsb.append(HtmlUtils.formTableClose());
        asb.append(HtmlUtils.formTableClose());


        StringBuffer osb = new StringBuffer();
        osb.append(HtmlUtils.formTable());


        StringBuffer     outputSB         = new StringBuffer();
        List<OutputType> types            = getRepository().getOutputTypes();
        String           lastCategoryName = null;
        for (OutputType type : types) {
            if ( !type.getForUser()) {
                continue;
            }
            boolean ok = getRepository().isOutputTypeOK(type);
            if ( !Misc.equals(lastCategoryName, type.getGroupName())) {
                if (lastCategoryName != null) {
                    outputSB.append("</div>\n");
                    outputSB.append(HtmlUtils.p());
                }
                lastCategoryName = type.getGroupName();
                outputSB.append(
                    HtmlUtils.div(
                        lastCategoryName, HtmlUtils.cssClass(
                            CSS_CLASS_HEADING_2)) + "\n<div style=\"margin-left:20px\">");
            }
            outputSB.append(HtmlUtils.checkbox("outputtype." + type.getId(),
                    "true", ok));
            outputSB.append(type.getLabel());
            outputSB.append(HtmlUtils.space(3));
        }
        outputSB.append("</div>\n");
        String outputDiv = HtmlUtils.div(outputSB.toString(),
                                         HtmlUtils.cssClass("scrollablediv"));
        osb.append("\n");
        String doAllOutput = HtmlUtils.checkbox("outputtype.all", "true",
                                 false) + HtmlUtils.space(1) + msg("Use all");
        osb.append(HtmlUtils.formEntryTop("", doAllOutput + outputDiv));
        osb.append("\n");
        StringBuffer        handlerSB      = new StringBuffer();
        List<OutputHandler> outputHandlers =
            getRepository().getOutputHandlers();
        for (OutputHandler outputHandler : outputHandlers) {
            outputHandler.addToSettingsForm(handlerSB);
        }

        String extra = handlerSB.toString();
        if (extra.length() > 0) {
            osb.append(tableSubHeader(msg("Output")));
            osb.append(extra);
        }

        osb.append(HtmlUtils.formEntry("&nbsp;<p>", ""));
        osb.append(HtmlUtils.formTableClose());


        sb.append(makeConfigBlock("Site and Contact Information",
                                  csb.toString()));
        sb.append(makeConfigBlock("Access", asb.toString()));
        sb.append(makeConfigBlock("Display", dsb.toString()));
        sb.append(makeConfigBlock("Available Output Types", osb.toString()));


        sb.append(HtmlUtils.submit(msg("Change Settings")));
        sb.append(HtmlUtils.formClose());

        return makeResult(request, msg("Settings"), sb);

    }

    /**
     * _more_
     *
     * @param title _more_
     * @param contents _more_
     *
     * @return _more_
     */
    private String makeConfigBlock(String title, String contents) {
        return HtmlUtils.makeShowHideBlock(
            msg(title),
            HtmlUtils.div(contents, HtmlUtils.cssClass("admin-block-inner")),
            false, HtmlUtils.cssClass(CSS_CLASS_HEADING_2),
            HtmlUtils.cssClass("admin-block"));
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEmailCapable() {
        String smtpServer = getRepository().getProperty(PROP_ADMIN_SMTP,
                                "").trim();
        String serverAdmin = getRepository().getProperty(PROP_ADMIN_EMAIL,
                                 "").trim();
        if ((serverAdmin.length() == 0) || (smtpServer.length() == 0)) {
            return false;
        }

        return true;
    }

    /**
     * _more_
     *
     * @param to _more_
     * @param subject _more_
     * @param contents _more_
     * @param asHtml _more_
     *
     * @throws Exception _more_
     */
    public void sendEmail(String to, String subject, String contents,
                          boolean asHtml)
            throws Exception {
        String from = getRepository().getProperty(PROP_ADMIN_EMAIL,
                          "").trim();
        sendEmail(to, from, subject, contents, asHtml);
    }


    /**
     * _more_
     *
     * @param to _more_
     * @param from _more_
     * @param subject _more_
     * @param contents _more_
     * @param asHtml _more_
     *
     * @throws Exception _more_
     */
    public void sendEmail(String to, String from, String subject,
                          String contents, boolean asHtml)
            throws Exception {
        sendEmail((List<Address>) Misc.newList(new InternetAddress(to)),
                  new InternetAddress(from), subject, contents, false,
                  asHtml);
    }


    /**
     * _more_
     *
     * @param to _more_
     * @param from _more_
     * @param subject _more_
     * @param contents _more_
     * @param bcc _more_
     * @param asHtml _more_
     *
     * @throws Exception _more_
     */
    public void sendEmail(List<Address> to, InternetAddress from,
                          String subject, String contents, boolean bcc,
                          boolean asHtml)
            throws Exception {
        if ( !isEmailCapable()) {
            throw new IllegalStateException(
                "This RAMADDA server has not been configured to send email");
        }

        //        System.err.println("subject:" + subject);
        //        System.err.println("contents:" + contents);
        String smtpServer = getRepository().getProperty(PROP_ADMIN_SMTP,
                                "").trim();

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.from", from.getAddress());
        javax.mail.Session session = javax.mail.Session.getInstance(props,
                                         null);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(from);
        Address[] array = new Address[to.size()];
        for (int i = 0; i < to.size(); i++) {
            array[i] = to.get(i);
        }
        msg.setRecipients((bcc
                           ? Message.RecipientType.BCC
                           : Message.RecipientType.TO), array);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setContent(contents, (asHtml
                                  ? "text/html"
                                  : "text/plain"));
        Transport.send(msg);
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
    public Result adminSettingsDo(Request request) throws Exception {

        request.ensureAuthToken();

        getRepository().getRegistryManager().applyAdminConfig(request);

        getRepository().writeGlobal(request, PROP_PROPERTIES, true);
        getRepository().writeGlobal(request, PROP_ADMIN_EMAIL, true);
        getRepository().writeGlobal(request, PROP_ADMIN_SMTP, true);
        getRepository().writeGlobal(request, PROP_LOGO_URL, true);
        getRepository().writeGlobal(request, PROP_LOGO_IMAGE, true);
        getRepository().writeGlobal(request, PROP_REPOSITORY_NAME, true);
        getRepository().writeGlobal(request, PROP_REPOSITORY_DESCRIPTION,
                                    true);
        getRepository().writeGlobal(request, PROP_ADMIN_PHRASES);
        getRepository().writeGlobal(request, PROP_HTML_FOOTER);


        getRepository().writeGlobal(request, PROP_GOOGLEAPIKEYS, true);
        getRepository().writeGlobal(request, PROP_FACEBOOK_CONNECT_KEY);
        getRepository().writeGlobal(PROP_NOSTYLE,
                                    "" + request.get(PROP_NOSTYLE, false));


        String ratings = "" + request.get(PROP_RATINGS_ENABLE, false);
        getRepository().writeGlobal(PROP_RATINGS_ENABLE, ratings);

        getRepository().writeGlobal(request, PROP_HOSTNAME);
        getRepository().writeGlobal(request, PROP_PORT);

        getRepository().writeGlobal(PROP_ACCESS_ALLSSL,
                                    "" + request.get(PROP_ACCESS_ALLSSL,
                                        false));

        getRepository().writeGlobal(PROP_FTP_PASSIVEPORTS,
                                    request.getString(PROP_FTP_PASSIVEPORTS,
                                        "").trim());

        if (request.defined(PROP_FTP_PORT)) {
            getRepository().writeGlobal(PROP_FTP_PORT,
                                        request.getString(PROP_FTP_PORT,
                                            "").trim());
            getRepository().getFtpManager().checkServer();
        }



        getRepository().writeGlobal(PROP_UPLOAD_MAXSIZEGB,
                                    request.getString(PROP_UPLOAD_MAXSIZEGB,
                                        "10").trim());


        getRepository().writeGlobal(PROP_CACHE_MAXSIZEGB,
                                    request.getString(PROP_CACHE_MAXSIZEGB,
                                        "10").trim());

        getRepository().writeGlobal(PROP_SYSTEM_MESSAGE,
                                    request.getString(PROP_SYSTEM_MESSAGE,
                                        ""));




        if (request.exists(PROP_LOCALFILEPATHS)) {
            getRepository().writeGlobal(
                PROP_LOCALFILEPATHS,
                request.getString(PROP_LOCALFILEPATHS, ""));
            getRepository().setLocalFilePaths();
            getRepository().clearCache();
        }

        getMapManager().applyAdminConfig(request);

        List<OutputHandler> outputHandlers =
            getRepository().getOutputHandlers();
        for (OutputHandler outputHandler : outputHandlers) {
            outputHandler.applySettings(request);
        }

        List<OutputType> types = getRepository().getOutputTypes();
        boolean          doAll = request.get("outputtype.all", false);
        for (OutputType type : types) {
            if ( !type.getForUser()) {
                continue;
            }
            boolean ok = doAll
                         || request.get("outputtype." + type.getId(), false);
            //            if(!ok)System.err.println("TYPE:" + type + " " + ok);
            getRepository().setOutputTypeOK(type, ok);
        }

        getRepository().writeGlobal(PROP_ACCESS_ADMINONLY,
                                    request.get(PROP_ACCESS_ADMINONLY,
                                        false));
        getRepository().writeGlobal(PROP_ACCESS_REQUIRELOGIN,
                                    request.get(PROP_ACCESS_REQUIRELOGIN,
                                        false));
        getRepository().writeGlobal(PROP_ACCESS_NOBOTS,
                                    request.get(PROP_ACCESS_NOBOTS, false));


        for (AdminHandler adminHandler : adminHandlers) {
            adminHandler.applyAdminSettingsForm(request);
        }

        //Tell the other repositoryManagers that the settings changed
        for (RepositoryManager repositoryManager :
                getRepository().getRepositoryManagers()) {
            repositoryManager.adminSettingsChanged();
        }

        return new Result(request.url(URL_ADMIN_SETTINGS));

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
    public Result adminAccess(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(msgHeader("Access Overview"));

        Statement statement =
            getDatabaseManager().execute(
                "select "
                + SqlUtil.comma(
                    Tables.PERMISSIONS.COL_ENTRY_ID,
                    Tables.PERMISSIONS.COL_ACTION,
                    Tables.PERMISSIONS.COL_ROLE) + " from "
                        + Tables.PERMISSIONS.NAME, 10000000, 0);

        Hashtable<String, List> idToPermissions = new Hashtable<String,
                                                      List>();

        SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
        ResultSet        results;
        List<String>     ids = new ArrayList<String>();
        while ((results = iter.getNext()) != null) {
            String id          = results.getString(1);
            String action      = results.getString(2);
            String role        = results.getString(3);
            List   permissions = idToPermissions.get(id);
            if (permissions == null) {
                idToPermissions.put(id, permissions = new ArrayList());
                ids.add(id);
            }
            permissions.add(new Permission(action, role));
        }

        sb.append("<table cellspacing=\"0\" cellpadding=\"0\">");
        sb.append(HtmlUtils.row(HtmlUtils.cols(HtmlUtils.space(10),
                HtmlUtils.b(msg("Action")) + HtmlUtils.space(3),
                HtmlUtils.b(msg("Role")))));
        for (String id : ids) {
            Entry entry = getEntryManager().getEntry(request, id);
            sb.append(
                HtmlUtils.row(
                    HtmlUtils.colspan(
                        getEntryManager().getBreadCrumbs(
                            request, entry,
                            getRepository().URL_ACCESS_FORM), 3)));
            List<Permission> permissions =
                (List<Permission>) idToPermissions.get(id);
            for (Permission permission : permissions) {
                sb.append(HtmlUtils.row(HtmlUtils.cols("",
                        permission.getAction(),
                        permission.getRoles().get(0))));

            }
            sb.append(HtmlUtils.row(HtmlUtils.colspan("<hr>", 3)));
        }
        sb.append("</table>");

        return makeResult(request, msg("Access Overview"), sb);
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
    public Result adminStats(Request request) throws Exception {

        DecimalFormat fmt     = new DecimalFormat("#0");


        StringBuffer  stateSB = new StringBuffer();
        stateSB.append(HtmlUtils.formTable());
        getStorageManager().addInfo(stateSB);
        getDatabaseManager().addInfo(stateSB);
        stateSB.append(HtmlUtils.formTableClose());



        StringBuffer statusSB    = new StringBuffer();
        double       totalMemory = (double) Runtime.getRuntime().maxMemory();
        double       freeMemory  = (double) Runtime.getRuntime().freeMemory();
        double highWaterMark     = (double) Runtime.getRuntime().totalMemory();
        double       usedMemory  = (highWaterMark - freeMemory);
        statusSB.append(HtmlUtils.formTable());
        totalMemory = totalMemory / 1000000.0;
        usedMemory  = usedMemory / 1000000.0;
        statusSB.append(
            HtmlUtils.formEntry(
                msgLabel("Version"),
                getRepository().getProperty(PROP_BUILD_VERSION, "1.0")));
        statusSB.append(
            HtmlUtils.formEntry(
                msgLabel("Build Date"),
                getRepository().getProperty(PROP_BUILD_DATE, "N/A")));


        statusSB.append(
            HtmlUtils.formEntry(
                msgLabel("Total Memory Available"),
                fmt.format(totalMemory) + " (MB)"));
        statusSB.append(HtmlUtils.formEntry(msgLabel("Used Memory"),
                                            fmt.format(usedMemory)
                                            + " (MB)"));

        long    uptime  = ManagementFactory.getRuntimeMXBean().getUptime();
        Counter counter = getRepository().getNumberOfCurrentRequests();
        /*
        statusSB.append(HtmlUtils.formEntry(msgLabel("Up Time"),
                                           fmt.format((double) (uptime / 1000
                                               / 60)) + " "
                                                   + msg("minutes")));
        */
        statusSB.append(HtmlUtils.formEntry(msgLabel("Total # Requests"),
                                            getLogManager().getRequestCount()
                                            + ""));
        statusSB.append(HtmlUtils.formEntryTop(msgLabel("Active Requests"),
                counter.getCount() + HtmlUtils.space(2)
                + HtmlUtils.makeShowHideBlock(msg("List"),
                    StringUtil.join("<br>", counter.getMessages()), false)));


        getEntryManager().addStatusInfo(statusSB);

        statusSB.append(HtmlUtils.formTableClose());



        StringBuffer outputSB = new StringBuffer();
        outputSB.append(HtmlUtils.formTable());
        List<OutputHandler> outputHandlers =
            getRepository().getOutputHandlers();

        for (OutputHandler outputHandler : outputHandlers) {
            outputHandler.getSystemStats(outputSB);
        }
        outputSB.append(HtmlUtils.formTableClose());


        StringBuffer   apiSB  = new StringBuffer();
        List<Object[]> tuples = new ArrayList<Object[]>();
        apiSB.append(HtmlUtils.formTable());
        for (ApiMethod apiMethod : getRepository().getApiManager().getApiMethods()) {
            if (apiMethod.getNumberOfCalls() < 1) {
                continue;
            }
            tuples.add(new Object[] {
                new Integer(apiMethod.getNumberOfCalls()),
                apiMethod });

        }
        tuples = (List<Object[]>) Misc.sortTuples(tuples, false);
        for (Object[] tuple : tuples) {
            ApiMethod apiMethod = (ApiMethod) tuple[1];
            apiSB.append(HtmlUtils.formEntry(apiMethod.getName(),
                                             "# " + msgLabel("Calls")
                                             + apiMethod.getNumberOfCalls()));
        }


        apiSB.append(HtmlUtils.formTableClose());


        StringBuffer dbSB = new StringBuffer();

        getDatabaseManager().addStatistics(request, dbSB);


        StringBuffer sb = new StringBuffer();
        sb.append(HtmlUtils.makeShowHideBlock(msg("System Status"),
                statusSB.toString(), false));

        StringBuffer pluginsSB = new StringBuffer();
        getRepository().getPluginManager().addStatusInfo(request, pluginsSB);
        sb.append(HtmlUtils.makeShowHideBlock(msg("Plugins"),
                pluginsSB.toString(), false));

        sb.append(HtmlUtils.makeShowHideBlock(msg("API"), apiSB.toString(),
                false));

        sb.append(HtmlUtils.makeShowHideBlock(msg("Output Handlers"),
                outputSB.toString(), false));

        sb.append(HtmlUtils.makeShowHideBlock(msg("Repository State"),
                stateSB.toString(), false));
        sb.append(HtmlUtils.makeShowHideBlock(msg("Database Statistics"),
                dbSB.toString(), false));

        return makeResult(request, msg("System"), sb);
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
    public Result adminSql(Request request) throws Exception {

        if ( !getRepository().getProperty(PROP_ADMIN_INCLUDESQL, false)) {
            return new Result("", new StringBuffer("Not enabled"));
        }

        boolean bulkLoad = false;
        String  query    = null;
        String  sqlFile  = request.getUploadedFile(ARG_SQLFILE);
        if ((sqlFile != null) && (sqlFile.length() > 0)
                && new File(sqlFile).exists()) {
            query = getStorageManager().readSystemResource(sqlFile);
            if ((query != null) && (query.trim().length() > 0)) {
                bulkLoad = true;
            }
        }
        if ( !bulkLoad) {
            query = (String) request.getUnsafeString(ARG_QUERY,
                    (String) null);
            if ((query != null) && query.trim().startsWith("file:")) {
                query = getStorageManager().readSystemResource(
                    query.trim().substring(5));
                bulkLoad = true;
            }
        }

        StringBuffer sb = new StringBuffer();
        //        sb.append(msgHeader("SQL"));
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.href(request.url(URL_ADMIN_TABLES),
                                 msg("View Schema")));
        sb.append(HtmlUtils.bold("&nbsp;|&nbsp;"));
        sb.append(HtmlUtils.href(request.url(URL_ADMIN_DUMPDB),
                                 msg("Dump Database")));
        sb.append(HtmlUtils.p());
        request.uploadFormWithAuthToken(sb, URL_ADMIN_SQL);

        sb.append(HtmlUtils.submit(msg("Execute")));
        sb.append(HtmlUtils.b("Note: be careful what you do here!"));
        sb.append(HtmlUtils.br());
        sb.append(HtmlUtils.textArea(ARG_QUERY, (bulkLoad
                ? ""
                : (query == null)
                  ? BLANK
                  : query), 10, 100));
        sb.append(HtmlUtils.p());
        sb.append("SQL File: ");
        sb.append(HtmlUtils.fileInput(ARG_SQLFILE, HtmlUtils.SIZE_60));
        sb.append(HtmlUtils.formClose());
        sb.append("<table>");
        if (query == null) {
            return makeResult(request, msg("SQL"), sb);
        }

        long t1 = System.currentTimeMillis();

        request.ensureAuthToken();
        if (bulkLoad) {
            getDatabaseManager().loadSql(query, false, true);

            return makeResult(request, msg("SQL"),
                              new StringBuffer("Executed SQL" + "<P>"
                                  + HtmlUtils.space(1) + sb.toString()));

        } else {
            Statement statement = null;
            try {
                statement = getDatabaseManager().execute(query, -1, 10000);
            } catch (Exception exc) {
                exc.printStackTrace();

                throw exc;
            }

            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
            ResultSet         results;
            int               cnt    = 0;
            Hashtable         map    = new Hashtable();
            int               unique = 0;
            ResultSetMetaData rsmd   = null;
            while ((results = iter.getNext()) != null) {
                if (rsmd == null) {
                    rsmd = results.getMetaData();
                }
                cnt++;
                if (cnt > 1000) {
                    continue;
                }
                int colcnt = 0;
                if (cnt == 1) {
                    sb.append("<table><tr>");
                    for (int i = 0; i < rsmd.getColumnCount(); i++) {
                        sb.append(
                            HtmlUtils.col(
                                HtmlUtils.bold(rsmd.getColumnLabel(i + 1))));
                    }
                    sb.append("</tr>");
                }
                sb.append("<tr valign=\"top\">");
                while (colcnt < rsmd.getColumnCount()) {
                    colcnt++;
                    if (rsmd.getColumnType(colcnt)
                            == java.sql.Types.TIMESTAMP) {
                        Date dttm = results.getTimestamp(colcnt,
                                        Repository.calendar);
                        sb.append(HtmlUtils.col(formatDate(request, dttm)));
                    } else {
                        String s = results.getString(colcnt);
                        if (s == null) {
                            s = "_null_";
                        }
                        s = HtmlUtils.entityEncode(s);
                        if (s.length() > 100) {
                            sb.append(
                                HtmlUtils.col(
                                    HtmlUtils.textArea("dummy", s, 5, 50)));
                        } else {
                            sb.append(HtmlUtils.col(HtmlUtils.pre(s)));
                        }
                    }
                }
                sb.append("</tr>\n");
                //                if (cnt++ > 1000) {
                //                    sb.append(HtmlUtils.row("..."));
                //                    break;
                //                }
            }
            sb.append("</table>");
            long t2 = System.currentTimeMillis();
            getRepository().clearCache();
            getRepository().readGlobals();

            return makeResult(request, msg("SQL"),
                              new StringBuffer(msgLabel("Fetched rows") + cnt
                                  + HtmlUtils.space(1) + msgLabel("in")
                                  + (t2 - t1) + "ms <p>" + sb.toString()));
        }

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
    public Result adminScanForBadParents(Request request) throws Exception {
        boolean      delete    = request.get("delete", false);
        StringBuffer sb        = new StringBuffer();
        Statement    statement = getDatabaseManager().execute("select "
                                  + Tables.ENTRIES.COL_ID + ","
                                  + Tables.ENTRIES.COL_PARENT_GROUP_ID
                                  + " from " + Tables.ENTRIES.NAME, 10000000,
                                      0);
        SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
        ResultSet        results;
        int              cnt        = 0;
        List<Entry>      badEntries = new ArrayList<Entry>();
        while ((results = iter.getNext()) != null) {
            String id       = results.getString(1);
            String parentId = results.getString(2);
            cnt++;
            if (parentId != null) {
                Entry parent = getEntryManager().findGroup(request, parentId);
                if (parent == null) {
                    Entry entry = getEntryManager().getEntry(request, id);
                    sb.append("bad parent:" + entry.getName() + " parent id="
                              + parentId + "<br>");
                    badEntries.add(entry);
                }
            }
        }
        sb.append("Scanned " + cnt + " entries");
        if (delete) {
            getEntryManager().deleteEntries(request, badEntries, null);

            return makeResult(request, msg("Scan"),
                              new StringBuffer("Deleted"));
        }

        return makeResult(request, msg("Scan"), sb);
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
    public Result adminPrintStack(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(HtmlUtils.formTable());
        DecimalFormat fmt         = new DecimalFormat("#0");
        double        totalMemory = (double) Runtime.getRuntime().maxMemory();
        double        freeMemory  = (double) Runtime.getRuntime().freeMemory();
        double highWaterMark = (double) Runtime.getRuntime().totalMemory();
        double        usedMemory  = (highWaterMark - freeMemory);
        totalMemory = totalMemory / 1000000.0;
        usedMemory  = usedMemory / 1000000.0;
        sb.append(HtmlUtils.formEntry("Total Memory Available:",
                                      fmt.format(totalMemory) + " (MB)"));
        sb.append(HtmlUtils.formEntry("Used Memory:",
                                      fmt.format(usedMemory) + " (MB)"));

        sb.append(
            HtmlUtils.formEntry(
                "# Requests:",
                "" + getRepository().getNumberOfCurrentRequests()));
        sb.append(HtmlUtils.formEntry("Start Time:",
                                      "" + getRepository().getStartTime()));


        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        //sb.append(HtmlUtils.formEntry("Up Time:",
        //                             fmt.format((double) (uptime / 1000
        //                                 / 60)) + " " + msg("minutes")));
        sb.append(HtmlUtils.formEntry("Up Time:", formatUptime(uptime)));

        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.makeShowHideBlock(msg("Stack"),
                "<pre>" + LogUtil.getStackDump(true) + "</pre>", false));

        return makeResult(request, msg("Stack Trace"), sb);
    }

    /**
     * Format the uptime
     *
     * @param milliseconds  time in milliseconds
     *
     * @return formatted time
     */
    private String formatUptime(long milliseconds) {
        StringBuffer buf     = new StringBuffer();
        int          seconds = (int) (milliseconds / 1000) % 60;
        int          minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int          hours   = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        int          days    = (int) ((milliseconds / (1000 * 60 * 60 * 24)));
        if (days > 0) {
            buf.append(days);
            buf.append(" day");
            if (days > 1) {
                buf.append("s");
            }
            buf.append(", ");
        }
        if (hours > 0) {
            buf.append(hours);
            buf.append(" hour");
            if (hours > 1) {
                buf.append("s");
            }
            buf.append(", ");
        }
        buf.append(minutes);
        buf.append(" minute");
        if (minutes != 1) {
            buf.append("s");
        }

        return buf.toString();
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
    public Result adminCleanup(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        if (request.defined(ACTION_STOP)) {
            runningCleanup = false;
            cleanupTS++;

            return new Result(request.url(URL_ADMIN_CLEANUP));
        } else if (request.defined(ACTION_START)) {
            //            Misc.run(this, "runDatabaseCleanUp", request);
            Misc.run(this, "runDatabaseOrphanCheck", request);

            return new Result(request.url(URL_ADMIN_CLEANUP));
        } else if (request.defined(ACTION_DUMPDB)) {
            return adminDbDump(request);
        } else if (request.defined(ACTION_NEWDB)) {
            getDatabaseManager().reInitialize();
            return new Result(request.url(URL_ADMIN_CLEANUP));
        } else if (request.defined(ACTION_CLEARCACHE)) {
            getRepository().clearAllCaches();
        } else if (request.defined(ACTION_SHUTDOWN)) {
            request.ensureAuthToken();
            if (getRepository().getShutdownEnabled()) {
                if (request.get(ARG_SHUTDOWN_CONFIRM, false)) {
                    return processShutdown(request);
                }
            }
        }

        request.formPostWithAuthToken(sb, URL_ADMIN_CLEANUP, "");
        String status = cleanupStatus.toString();
        if (runningCleanup) {
            sb.append(msg("Database clean up is running"));
            sb.append("<p>");
            sb.append(HtmlUtils.submit(msg("Stop cleanup"), ACTION_STOP));
        } else {
            sb.append(HtmlUtils.p());
            //            sb.append(
            //                msg(
            //                "Cleanup allows you to remove all file entries from the repository database that do not exist on the local file system"));
            //            sb.append("<p>");
            //            sb.append(HtmlUtils.submit(msg("Start cleanup"), ACTION_START));


            sb.append("<p>");
            sb.append(msg("Clear the entry cache"));
            sb.append("<br>");
            sb.append(HtmlUtils.submit(msg("Clear cache"),
                                       ACTION_CLEARCACHE));


            sb.append("<p>");
            sb.append(
                HtmlUtils.submit(
                    msg("Reinitialize Database Connection"), ACTION_NEWDB));

            if (getRepository().getShutdownEnabled()) {
                sb.append("<p>");
                sb.append(msg("Shutdown the server"));
                sb.append("<br>");
                sb.append(HtmlUtils.submit(msg("Shutdown server"),
                                           ACTION_SHUTDOWN));
                sb.append(HtmlUtils.space(2));
                sb.append(HtmlUtils.checkbox(ARG_SHUTDOWN_CONFIRM, "true",
                                             false));
                sb.append(HtmlUtils.space(1));
                sb.append(msg("Yes, I really want to shutdown the server"));
            }

            /*
            sb.append("<p>");
            sb.append(
                msg(
                "You can write out the database for backup or transfer to a new database"));
            sb.append("<p>");
            sb.append(HtmlUtils.submit(msg("Export the database"),
                                      ACTION_DUMPDB));
            */
        }
        sb.append("</form>");
        if (status.length() > 0) {
            sb.append(msgHeader("Cleanup Status"));
            sb.append(status);
        }

        //        sb.append(cnt +" files do not exist in " + (t2-t1) );
        return makeResult(request, msg("Cleanup"), sb);
    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void runDatabaseCleanUp(Request request) throws Exception {
        if (runningCleanup) {
            return;
        }
        runningCleanup = true;
        cleanupStatus  = new StringBuffer();
        int myTS = ++cleanupTS;
        try {
            Statement statement =
                getDatabaseManager().select(
                    SqlUtil.comma(
                        Tables.ENTRIES.COL_ID, Tables.ENTRIES.COL_RESOURCE,
                        Tables.ENTRIES.COL_TYPE), Tables.ENTRIES.NAME,
                            Clause.eq(
                                Tables.ENTRIES.COL_RESOURCE_TYPE,
                                Resource.TYPE_FILE));

            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
            ResultSet   results;
            int         cnt       = 0;
            int         deleteCnt = 0;
            long        t1        = System.currentTimeMillis();
            List<Entry> entries   = new ArrayList<Entry>();
            while ((results = iter.getNext()) != null) {
                if ((cleanupTS != myTS) || !runningCleanup) {
                    runningCleanup = false;

                    break;
                }
                int    col      = 1;
                String id       = results.getString(col++);
                String resource = getStorageManager().resourceFromDB(
                                      results.getString(col++));
                Entry entry = getRepository().getTypeHandler(
                                  results.getString(col++)).createEntry(id);
                File f = new File(resource);
                if (f.exists()) {
                    continue;
                }
                //TODO: differentiate the entries that are not files
                entries.add(entry);
                if (entries.size() % 1000 == 0) {
                    System.err.print(".");
                }
                if (entries.size() > 1000) {
                    getEntryManager().deleteEntries(request, entries, null);
                    entries       = new ArrayList<Entry>();
                    deleteCnt     += 1000;
                    cleanupStatus = new StringBuffer("Removed " + deleteCnt
                            + " entries from database");
                }
                if ((cleanupTS != myTS) || !runningCleanup) {
                    runningCleanup = false;

                    break;
                }
            }
            if (runningCleanup) {
                getEntryManager().deleteEntries(request, entries, null);
                deleteCnt     += entries.size();
                cleanupStatus = new StringBuffer(msg("Done running cleanup")
                        + "<br>" + msg("Removed") + HtmlUtils.space(1)
                        + deleteCnt + " entries from database");
            }
        } catch (Exception exc) {
            logError("Running cleanup", exc);
            cleanupStatus.append("An error occurred running cleanup<pre>");
            cleanupStatus.append(LogUtil.getStackTrace(exc));
            cleanupStatus.append("</pre>");
        }
        runningCleanup = false;
        long t2 = System.currentTimeMillis();
    }



    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void runDatabaseOrphanCheck(Request request) throws Exception {
        if (runningCleanup) {
            return;
        }
        runningCleanup = true;
        cleanupStatus  = new StringBuffer();
        int             myTS  = ++cleanupTS;
        List<String[]>  ids   = new ArrayList<String[]>();
        HashSet<String> idMap = new HashSet<String>();

        try {
            Statement statement =
                getDatabaseManager()
                    .select(SqlUtil
                        .comma(Tables.ENTRIES.COL_ID,
                               Tables.ENTRIES.COL_PARENT_GROUP_ID), Tables
                                   .ENTRIES.NAME, (Clause) null);
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
            ResultSet results;
            long      t1 = System.currentTimeMillis();
            while ((results = iter.getNext()) != null) {
                if ((cleanupTS != myTS) || !runningCleanup) {
                    runningCleanup = false;

                    break;
                }
                String id       = results.getString(1);
                String parentId = results.getString(2);
                ids.add(new String[] { id, parentId });
                idMap.add(id);
            }

            for (String[] tuples : ids) {
                String id       = tuples[0];
                String parentId = tuples[1];
                if (parentId == null) {
                    Entry entry = getEntryManager().getEntry(request, id);
                    System.out.println("root:" + id + " " + entry);

                    continue;
                }
                if ( !idMap.contains(parentId)) {
                    //                    System.out.println("bad parent:" + id +" " + parentId);
                }
            }


            if (runningCleanup) {
                cleanupStatus = new StringBuffer(msg("Done running cleanup"));
            }
        } catch (Exception exc) {
            logError("Running cleanup", exc);
            cleanupStatus.append("An error occurred running cleanup<pre>");
            cleanupStatus.append(LogUtil.getStackTrace(exc));
            cleanupStatus.append("</pre>");
        }
        runningCleanup = false;
        long t2 = System.currentTimeMillis();
    }



    /** _more_ */
    int ccnt = 0;





}
