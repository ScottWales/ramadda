/*
* Copyright 2008-2013 Jeff McWhirter/ramadda.org
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

package org.ramadda.repository;


import org.ramadda.repository.server.JettyServer;
import org.ramadda.repository.server.RepositoryServlet;

import org.ramadda.repository.admin.Admin;
import org.ramadda.repository.auth.SessionManager;
import org.ramadda.repository.auth.User;
import org.ramadda.repository.auth.UserManager;
import org.ramadda.repository.database.DatabaseManager;
import org.ramadda.repository.database.Tables;


import org.ramadda.util.Utils;
import org.ramadda.util.HtmlUtils;


import org.ramadda.util.MyTrace;
import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;

import ucar.unidata.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;


import java.sql.ResultSet;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;




/**
 */
public class  LocalRepositoryManager extends RepositoryManager {

    public static final String ARG_LOCAL_NEW = "local.new";
    public static final String ARG_LOCAL_SURE = "local.sure";
    public static final String ARG_LOCAL_NAME = "local.name";
    public static final String ARG_LOCAL_CONTACT = "local.contact";
    public static final String ARG_LOCAL_ID = "local.id";
    public static final String ARG_LOCAL_CHANGE = "local.change";
    public static final String ARG_LOCAL_STATUS = "local.status";

    //    public static final String ARG_LOCAL_ = "local.id";

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_STOPPED = "stopped";
    public static final String STATUS_DELETED = "deleted";



    public LocalRepositoryManager(Repository repository) {
        super(repository);
    }

    public void initializeLocalRepositories() throws Exception {
        if(!getRepository().isMaster()) {
            return;
        }
        if(getRepository().getJettyServer()==null) {
            getLogManager().logError("RAMADDA is defined as a master but not running under Jetty", null);
            return;
        }
        for(Local local: readLocals()) {
            if(local.status.equals(STATUS_ACTIVE)) {
                Repository childRepository =  startLocalRepository(local.id, new Properties());
            }
        }
    }

    private List<Local> readLocals() throws Exception {
        List<Local> locals = new ArrayList<Local>();
        Statement stmt =
            getDatabaseManager().select(Tables.LOCALREPOSITORIES.COLUMNS,
                                        Tables.LOCALREPOSITORIES.NAME,
                                        (Clause) null);
        SqlUtil.Iterator iter     = getDatabaseManager().getIterator(stmt);
        ResultSet        results;
        while ((results = iter.getNext()) != null) {
            locals.add(new Local(results.getString(Tables.LOCALREPOSITORIES.COL_NODOT_ID),
                                 results.getString(Tables.LOCALREPOSITORIES.COL_NODOT_EMAIL),
                                 results.getString(Tables.LOCALREPOSITORIES.COL_NODOT_STATUS)));

       }
        return locals;
    }

    public boolean hasServer(String otherServer) throws Exception {
        return getDatabaseManager().tableContains(otherServer, Tables.LOCALREPOSITORIES.NAME, Tables.LOCALREPOSITORIES.COL_ID);
    }


    public void addChildRepository(Request request, StringBuffer sb, String repositoryId) throws Exception {
        repositoryId = repositoryId.trim();
        repositoryId= repositoryId.replaceAll("[^a-zA-Z_0-9]+","");
        if(repositoryId.length()==0) {
            throw new IllegalArgumentException("Bad id:" + repositoryId);
        }
        if(hasServer(repositoryId)) {
            throw new IllegalArgumentException("Already have a repository with id:" + repositoryId);
        }




        String name  = request.getString(ARG_LOCAL_NAME,"");
        String contact = request.getString(ARG_LOCAL_CONTACT,"").trim();
        Properties properties = new Properties();
        properties.put(PROP_REPOSITORY_NAME, name);
        File ramaddaHomeDir = getHomeDir(repositoryId);
        boolean existedBefore = new File(IOUtil.joinDir(ramaddaHomeDir,"storage")).exists();

        Repository childRepository =  startLocalRepository(repositoryId, properties);
        childRepository.writeGlobal(Admin.ARG_ADMIN_INSTALLCOMPLETE, "true");
        childRepository.writeGlobal(PROP_HOSTNAME, getProperty(PROP_HOSTNAME,""));

        sb.append(HtmlUtils.p());
        sb.append("Created repository: " + HtmlUtils.href("/" + repositoryId,"/" + repositoryId));
        sb.append("<br>");

        String userId = repositoryId+"_admin";

        String password = request.getString(UserManager.ARG_USER_PASSWORD1,"").trim();
        if(password.length()>0) {
            User user =         childRepository.getUserManager().findUser(userId);
            if(user ==null) {
                user = new User(repositoryId+"_admin",
                                "Administrator",
                                "", "", "",
                                childRepository.getUserManager().hashPassword(
                                                                              password), true, "", "",
                                false, null);
                childRepository.getUserManager().makeOrUpdateUser(user, false);
                sb.append("Created user: " + user.getId());
                sb.append(HtmlUtils.p());
            }
            if(!existedBefore) {
                childRepository.getAdmin().addInitEntries(user);
            }
        }

        getDatabaseManager().executeInsert(Tables.LOCALREPOSITORIES.INSERT, new Object[]{repositoryId, contact, STATUS_ACTIVE});
        getRepository().addChildRepository(childRepository);
    }

    private File getHomeDir(String repositoryId) throws Exception {
        String repositoriesDir = getProperty("ramadda.master.dir", "%repositorydir%/repositories");
        repositoriesDir = repositoriesDir.replace("%repositorydir%",getStorageManager().getRepositoryDir().toString());
        return new File(IOUtil.joinDir(repositoriesDir, "repository_" + repositoryId));
    }

    private Repository startLocalRepository(String repositoryId,  Properties properties) throws Exception {
        System.err.println("RAMADDA: starting local repository:" + repositoryId);
        File ramaddaHomeDir = getHomeDir(repositoryId);
        ramaddaHomeDir.mkdirs();
        File otherPluginDir = new File(IOUtil.joinDir(ramaddaHomeDir, "plugins"));
        //TODO: Do we always copy the plugins on start up or just the first time
        //        if(!otherPluginDir.exists()) {
        otherPluginDir.mkdirs();
        for(File myPluginFile: getStorageManager().getPluginsDir().listFiles()) {
            if(!myPluginFile.isFile()) continue;
            IOUtil.copyFile(myPluginFile, otherPluginDir);
        }
        //}

        //Copy the keystore and the ssl.properties


        properties.put(PROP_HTML_URLBASE, "/" + repositoryId);
        properties.put(PROP_REPOSITORY_HOME,ramaddaHomeDir.toString());
        //TODO: do we let the children also be masters?
        properties.put(PROP_REPOSITORY_PRIMARY,"false");
        File propertiesFile = new File(IOUtil.joinDir(ramaddaHomeDir, "repository.properties"));
        properties.store(new FileOutputStream(propertiesFile),"Generated by RAMADDA");
        Repository childRepository =  new Repository(new String[]{}, getRepository().getJettyServer().getPort());
        childRepository.init(properties);
        getRepository().addChildRepository(childRepository);
        return childRepository;
    }

    public Result handleRepos(Request request) throws Exception {
        if (!getRepository().isMaster()) {
            throw new IllegalArgumentException("Not a master repo");
        }
        if(true) {
            throw new IllegalArgumentException("Not implemented");
        }
        String path   = request.getRequestPath();
        String prefix = getRepository().getUrlBase() +"/repos";
        if (path.length() > prefix.length()) {
            String suffix = path.substring(prefix.length());
            for(Repository childRepository: getRepository().getChildRepositories()) {
                if((childRepository.getUrlBase()+"/").startsWith(suffix) ||
                   suffix.equals(childRepository.getUrlBase())) {
                    request = request.cloneMe();
                    request.setRequestPath(suffix);
                    request.setUser(null);
                    request.setSessionId(null);
                    Result result =  childRepository.handleRequest(request);
                    result.setShouldDecorate(false);
                    return result;
                }
            }
        }
        return new Result("", new StringBuffer("Couldn't find repos"));
    }

    public Result adminLocal(Request request) throws Exception {
        if (!getRepository().isMaster()) {
            throw new IllegalArgumentException("Not a master repo");
        }
        
        StringBuffer    sb         = new StringBuffer();

        if(request.defined(ARG_LOCAL_NEW)) {
            request.ensureAuthToken();
            if(request.get(ARG_LOCAL_SURE, false)) {
                processLocalNew(request, sb);
            } else {
                sb.append(getRepository().showDialogNote("You didn't select 'Yes, I am sure'"));
            }
        }


        if(request.defined(ARG_LOCAL_CHANGE)) {
            request.ensureAuthToken();
            processLocalChange(request, sb);
           
        }

        sb.append(HtmlUtils.formTable(""));
        boolean didone = false;
        for(Local local: readLocals()) {
            if(!didone) {
                sb.append(HtmlUtils.row(HtmlUtils.cols("<b>Repository</b>"/*,"<b>Contact</b>"*/,"<b>Status</b>","<b>Action</b>")));
            }
            didone = true;
            StringBuffer statusSB = new StringBuffer();
            StringBuffer statusSB2 = new StringBuffer();

            request.formPostWithAuthToken(statusSB, getAdmin().URL_ADMIN_LOCAL);
            statusSB.append(HtmlUtils.hidden(ARG_LOCAL_ID, local.id));
            if(local.status.equals(STATUS_ACTIVE)) {
                statusSB.append(HtmlUtils.submit(msg("Stop Repository"), ARG_LOCAL_CHANGE));
                statusSB.append(HtmlUtils.hidden(ARG_LOCAL_STATUS, STATUS_STOPPED));
            } else if(local.status.equals(STATUS_STOPPED)) {
                statusSB.append(HtmlUtils.submit(msg("Start Repository"),ARG_LOCAL_CHANGE));
                statusSB.append(HtmlUtils.hidden(ARG_LOCAL_STATUS, STATUS_ACTIVE));
            }
            statusSB.append(HtmlUtils.formClose());

            if(local.status.equals(STATUS_STOPPED)) {
                request.formPostWithAuthToken(statusSB2, getAdmin().URL_ADMIN_LOCAL);
                statusSB2.append(HtmlUtils.space(1));
                statusSB2.append(HtmlUtils.hidden(ARG_LOCAL_ID, local.id));
                statusSB2.append(HtmlUtils.hidden(ARG_LOCAL_STATUS, STATUS_DELETED));
                statusSB2.append(HtmlUtils.submit(msg("Remove Repository"),ARG_LOCAL_CHANGE));
                statusSB2.append(HtmlUtils.checkbox(ARG_LOCAL_SURE, "true", false) + " " + msg("Yes, remove this repository"));
                statusSB2.append(HtmlUtils.formClose());
            }
            sb.append(HtmlUtils.rowTop(HtmlUtils.cols(HtmlUtils.href("/" + local.id,local.id)/*,contact*/,local.status, statusSB.toString(), statusSB2.toString())));
        }
        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.p());

        request.formPostWithAuthToken(sb, getAdmin().URL_ADMIN_LOCAL);
        sb.append(formHeader(msg("New repository")));
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.formEntry(msgLabel("ID"),  HtmlUtils.input(ARG_LOCAL_ID, request.getString(ARG_LOCAL_ID,""))));
        sb.append(HtmlUtils.formEntry(msgLabel("Repository Name"),  HtmlUtils.input(ARG_LOCAL_NAME, request.getString(ARG_LOCAL_NAME,""))));
        sb.append(HtmlUtils.formEntry(msgLabel("Contact"),  HtmlUtils.input(ARG_LOCAL_CONTACT, request.getString(ARG_LOCAL_CONTACT,""))));
        sb.append(HtmlUtils.formEntry(msgLabel("Admin Password"),  HtmlUtils.input(UserManager.ARG_USER_PASSWORD1)));

        sb.append(HtmlUtils.formEntry("", HtmlUtils.submit(msg("Create new repository"), ARG_LOCAL_NEW)+
                                      " " + HtmlUtils.checkbox(ARG_LOCAL_SURE, "true", false) + " " + msg("Yes, I am sure")));

        sb.append(HtmlUtils.formTableClose());

        sb.append(HtmlUtils.formClose());


        return getAdmin().makeResult(request, "Administration", sb);
    }

    private void processLocalChange(Request request, StringBuffer sb) throws Exception {
        String status = request.getString(ARG_LOCAL_STATUS,"");
        String id = request.getString(ARG_LOCAL_ID,"");
        if(status.equals(STATUS_ACTIVE)) {
            Repository childRepository =  startLocalRepository(id, new Properties());
        }  else if(status.equals(STATUS_STOPPED)) {
            RepositoryServlet servlet = getRepository().getServlet("/" + id);
            if(servlet  == null) {
                sb.append(getRepository().showDialogError("Could not find running server"));
                return;
            }
            servlet.getRepository().close();
            getRepository().removeChildRepository(servlet.getRepository());
        }  else if(status.equals(STATUS_DELETED)) {
            if(!request.get(ARG_LOCAL_SURE, false)) {
                sb.append(getRepository().showDialogError("Check on the 'Yes, remove this repository' box"));
                return;
            }
            //TODO: make sure status is stopped
            getDatabaseManager().delete(Tables.LOCALREPOSITORIES.NAME, Clause.eq(Tables.LOCALREPOSITORIES.COL_ID,  id));
            return;
        } else  {
            sb.append(getRepository().showDialogError("Unknown status:" + status));
            return;
        }
        getDatabaseManager().update(Tables.LOCALREPOSITORIES.NAME, Tables.LOCALREPOSITORIES.COL_ID,  id, new String[] {
                Tables.LOCALREPOSITORIES.COL_STATUS},
            new String[]{status});
    }

    private void processLocalNew(Request request, StringBuffer sb) throws Exception {
        String id  = request.getString(ARG_LOCAL_ID,"");
        if(!Utils.stringDefined(id)) {
            sb.append(getRepository().showDialogError("No ID given"));
            return;
        }

        if(hasServer(id)) {
            sb.append(getRepository().showDialogError("Server with id already exists"));
            return;
        }
        try {
            addChildRepository(request,sb, id);
        } catch(Exception exc) {
            sb.append(getRepository().showDialogError("Error:" + exc));

        }

    }


    private static class Local {
        String id;
        String contact;
        String status;
        public Local(String id,   String contact,  String status) {
            this.id = id;
            this.contact = contact;
            this.status = status;
        }
    }


}
