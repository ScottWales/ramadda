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

package org.ramadda.repository.auth;


import org.ramadda.repository.*;


import org.ramadda.repository.database.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TTLCache;


import ucar.unidata.sql.Clause;


import ucar.unidata.sql.SqlUtil;

import ucar.unidata.util.Cache;
import ucar.unidata.util.DateUtil;

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
public class SessionManager extends RepositoryManager {

    /** The number of days a session is active in the database */
    private static final double SESSION_DAYS = 1.0;



    /** _more_ */
    public static final String COOKIE_NAME = "repositorysession";

    
    private String cookieName;

    /** _more_ */
    private Hashtable<String, Session> sessionMap = new Hashtable<String,
                                                        Session>();

    //This holds sessions for anonymous users. The timeout is 24 hours. Max size is 1000
    private TTLCache<String, Session> anonymousSessionMap = new TTLCache<String,
        Session>(1000*3600*24, 1000);




    /** _more_ */
    private Cache<Object, Object> sessionExtra = new Cache<Object,
                                                     Object>(5000);


    /**
     * _more_
     *
     * @param repository _more_
     */
    public SessionManager(Repository repository) {
        super(repository);
        this.cookieName = "ramadda" + repository.getUrlBase().replaceAll("/","_") +"_session";
    }

    /**
     * _more_
     */
    public void init() {
        Misc.run(new Runnable() {
            public void run() {
                cullSessions();
            }
        });
    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void deleteAllSessions() throws Exception {
        sessionMap = new Hashtable<String, Session>();
        anonymousSessionMap.clearCache();
    }


    /**
     * _more_
     *
     * @param value _more_
     *
     * @return _more_
     */
    public String putSessionExtra(Object value) {
        String id = "${" + getRepository().getGUID() + "}";
        putSessionExtra(id, value);

        return id;
    }


    /**
     * _more_
     *
     * @param key _more_
     * @param value _more_
     */
    public void putSessionExtra(Object key, Object value) {
        sessionExtra.put(key, value);
    }


    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public Object getSessionExtra(Object key) {
        return sessionExtra.get(key);
    }



    /**
     * _more_
     */
    private void cullSessions() {
        //Wait a while before starting
        Misc.sleepSeconds(60);
        //        Misc.sleepSeconds(5);
        while (true) {
            try {
                cullSessionsInner();
            } catch (Exception exc) {
                logException("Culling sessions", exc);

                return;
            }
            //Wake up every minute to see if we're shutdown
            //but do the cull every hour
            for (int minuteIdx = 0; minuteIdx < 60; minuteIdx++) {
                Misc.sleepSeconds(60);
                if ( !getActive()) {
                    return;
                }
            }
        }
    }



    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void cullSessionsInner() throws Exception {
        List<Session> sessionsToDelete = new ArrayList<Session>();
        long          now              = new Date().getTime();
        Statement stmt = getDatabaseManager().select(Tables.SESSIONS.COLUMNS,
                             Tables.SESSIONS.NAME, (Clause) null);
        SqlUtil.Iterator iter = getDatabaseManager().getIterator(stmt);
        ResultSet        results;
        double           timeDiff = DateUtil.daysToMillis(SESSION_DAYS);
        while ((results = iter.getNext()) != null) {
            Session session        = makeSession(results);
            Date    lastActiveDate = session.getLastActivity();
            //Check if the last activity was > 24 hours ago
            if ((now - lastActiveDate.getTime()) > timeDiff) {
                sessionsToDelete.add(session);
            } else {}
        }
        for (Session session : sessionsToDelete) {
            removeSession(session.getId());
        }

    }


    public Entry getLastEntry(Request request) 
            throws Exception {
        return (Entry) getSessionProperty(request,
                                          "lastentry");
    }

    public void setLastEntry(Request request, Entry entry) 
            throws Exception {
        if(entry !=null && request!=null) {
            putSessionProperty(request, "lastentry", entry);
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param key _more_
     * @param value _more_
     *
     * @throws Exception _more_
     */
    public void putSessionProperty(Request request, Object key, Object value)
            throws Exception {

        //JIC
        if(request == null) return;

        String id = request.getSessionId();
        if (id == null) {
            request.putExtraProperty(key, value);
            return;
        }
        Session session = getSession(id);
        if (session == null) {
            request.putExtraProperty(key, value);
            return;
        }
        session.putProperty(key, value);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param key _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Object getSessionProperty(Request request, Object key)
            throws Exception {
        return getSessionProperty(request, key, null);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Object getSessionProperty(Request request, Object key, Object dflt)
            throws Exception {
        //JIC
        if(request == null) return dflt;

        //        System.err.println("getSession:" + key);
        String id = request.getSessionId();
        if (id == null) {
            Object obj  = request.getExtraProperty(key);
            if(obj!=null) return obj;
            return dflt;
        }
        Session session = getSession(id);
        if (session == null) {
            Object obj  = request.getExtraProperty(key);
            if(obj!=null) return obj;
            return dflt;
        }
        return session.getProperty(key);
    }


    //TODO: we need to clean out old sessions every once in a while


    /**
     * _more_
     *
     * @param sessionId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Session getSession(String sessionId) throws Exception {
        return getSession(sessionId, true);
    }

    public Session getSession(String sessionId, boolean checkAnonymous) throws Exception {
        Session session =  sessionMap.get(sessionId);
        if(session!=null) {
            return session;
        }
        session = anonymousSessionMap.get(sessionId);
        if(session!=null) {
            return session;
        }

        Statement stmt = getDatabaseManager().select(
                                                     Tables.SESSIONS.COLUMNS,
                                                     Tables.SESSIONS.NAME,
                                                     Clause.eq(
                                                               Tables.SESSIONS.COL_SESSION_ID,
                                                               sessionId));
        try {
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(stmt);
            ResultSet results;
            //COL_SESSION_ID,COL_USER_ID,COL_CREATE_DATE,COL_LAST_ACTIVE_DATE,COL_EXTRA
            boolean ok = true;
            while ((results = iter.getNext()) != null && ok) {
                session = makeSession(results);
                //                    System.err.println ("Got session:" + session);
                session.setLastActivity(new Date());
                //Remove it from the DB and then readd it so we update the lastActivity
                removeSession(session.getId());
                addSession(session);
                sessionMap.put(sessionId, session);
                ok = false;
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(stmt);
        }
        return session;
    }

    /**
     * _more_
     *
     * @param results _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Session makeSession(ResultSet results) throws Exception {
        int    col       = 1;
        String sessionId = results.getString(col++);
        String userId    = results.getString(col++);
        User   user      = getUserManager().findUser(userId);
        if (user == null) {
            user = getUserManager().getAnonymousUser();
        }
        //See if we have it in the map
        Session session = sessionMap.get(sessionId);
        if (session != null) {
            return session;
        }
        Date createDate     = getDatabaseManager().getDate(results, col++);
        Date lastActiveDate = getDatabaseManager().getDate(results, col++);
        return new Session(sessionId, user, createDate, lastActiveDate);
    }


    /**
     * _more_
     *
     * @param sessionId _more_
     *
     * @throws Exception _more_
     */
    public void removeSession(String sessionId) throws Exception {
        sessionMap.remove(sessionId);
        anonymousSessionMap.remove(sessionId);
        getDatabaseManager().delete(Tables.SESSIONS.NAME,
                                    Clause.eq(Tables.SESSIONS.COL_SESSION_ID,
                                        sessionId));
    }

    /**
     * _more_
     *
     * @param session _more_
     *
     * @throws Exception _more_
     */
    public void addSession(Session session) throws Exception {
        sessionMap.put(session.getId(), session);
        //COL_SESSION_ID,COL_USER_ID,COL_CREATE_DATE,COL_LAST_ACTIVE_DATE,COL_EXTRA
        getDatabaseManager().executeInsert(Tables.SESSIONS.INSERT,
                                           new Object[] { session.getId(),
                session.getUserId(), new Date(), new Date(), "" });
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private List<Session> getSessions() throws Exception {
        List<Session> sessions = new ArrayList<Session>();
        Statement stmt = getDatabaseManager().select(Tables.SESSIONS.COLUMNS,
                             Tables.SESSIONS.NAME, (Clause) null);
        SqlUtil.Iterator iter = getDatabaseManager().getIterator(stmt);
        ResultSet        results;
        while ((results = iter.getNext()) != null) {
            sessions.add(makeSession(results));
        }

        return sessions;
    }




    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void checkSession(Request request) throws Exception {
        User         user    = request.getUser();
        List<String> cookies  = getCookies(request);
        for (String cookieValue : cookies) {
            request.setSessionId(cookieValue);
            if (user == null) {
                Session session = getSession(cookieValue, false);
                if (session != null) {
                    session.setLastActivity(new Date());
                    user = getUserManager().getCurrentUser(session.getUser());
                    session.setUser(user);
                    break;
                }
            }
        }

        //Check for the session id as a url argument
        if (user == null && request.hasParameter(ARG_SESSIONID)) {
            Session session = getSession(request.getString(ARG_SESSIONID), false);
            if (session != null) {
                session.setLastActivity(new Date());
                user = getUserManager().getCurrentUser(session.getUser());
                session.setUser(user);
            }
        }

        //Check for url auth
        if (user == null && request.exists(ARG_AUTH_USER)
            && request.exists(ARG_AUTH_PASSWORD)) {
            String userId   = request.getString(ARG_AUTH_USER, "");
            String password = request.getString(ARG_AUTH_PASSWORD, "");
            user = getUserManager().findUser(userId, false);
            if (user == null) {
                throw new IllegalArgumentException(msgLabel("Unknown user")
                        + userId);
            }
            if ( !getUserManager().isPasswordValid(user, password)) {
                throw new IllegalArgumentException(msg("Incorrect password"));
            }
            setUserSession(request, user);
        }

        //Check for basic auth
        if (user == null) {
            String auth =
                (String) request.getHttpHeaderArgs().get("Authorization");
            if (auth == null) {
                auth = (String) request.getHttpHeaderArgs().get(
                    "authorization");
            }

            if (auth != null) {
                auth = auth.trim();
                //Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
                if (auth.startsWith("Basic")) {
                    auth = new String(
                        RepositoryUtil.decodeBase64(
                            auth.substring(5).trim()));
                    String[] toks = StringUtil.split(auth, ":", 2);
                    if (toks.length == 2) {
                        user = getUserManager().findUser(toks[0], false);
                        if (user == null) {
                        } else if ( !getUserManager().isPasswordValid(user,
                                toks[1])) {
                            user = null;
                        } else {}
                    }
                    if (user != null) {
                        setUserSession(request, user);
                    }
                }
            }
        }

        if (request.getSessionId() == null && !request.defined(ARG_SESSIONID)) {
            request.setSessionId(createSessionId());
        }

        //Make sure we have the current user state
        user = getUserManager().getCurrentUser(user);

        if (user == null) {
            user = getUserManager().getAnonymousUser();
            //Create a temporary session
            Session session = anonymousSessionMap.get(request.getSessionId());
            if(session == null) {
                session  = new Session(request.getSessionId(), user, new Date());
                anonymousSessionMap.put(request.getSessionId(), session);
            }
        }

        request.setUser(user);
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
    private List<String> getCookies(Request request) throws Exception {
        List<String> cookies = new ArrayList<String>();
        String       cookie  = request.getHeaderArg("Cookie");
        if (cookie == null) {
            return cookies;
        }

        List toks = StringUtil.split(cookie, ";", true, true);
        for (int i = 0; i < toks.size(); i++) {
            String tok     = (String) toks.get(i);
            List   subtoks = StringUtil.split(tok, "=", true, true);
            if (subtoks.size() != 2) {
                continue;
            }
            String cookieName  = (String) subtoks.get(0);
            String cookieValue = (String) subtoks.get(1);
            if (cookieName.equals(getSessionCookieName())) {
                cookies.add(cookieValue);
            } else if (cookieName.equals(COOKIE_NAME)) {
                //For backwards compatability
                cookies.add(cookieValue);
            }
        }


        return cookies;
    }



    public String getSessionCookieName() {
        return cookieName;
        //        return COOKIE_NAME;
    }





    /**
     * _more_
     *
     * @return _more_
     */
    public String createSessionId() {
        return getRepository().getGUID() + "_" + Math.random();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param user _more_
     *
     * @throws Exception _more_
     */
    public void setUserSession(Request request, User user) throws Exception {
        if (request.getSessionId() == null) {
            request.setSessionId(createSessionId());
        }
        addSession(new Session(request.getSessionId(), user, new Date()));
        request.setUser(user);
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void removeUserSession(Request request) throws Exception {
        if (request.getSessionId() != null) {
            removeSession(request.getSessionId());
        }
        List<String> cookies = getCookies(request);
        for (String cookieValue : cookies) {
            removeSession(cookieValue);
        }
        request.setUser(getUserManager().getAnonymousUser());
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
    public StringBuffer getSessionList(Request request) throws Exception {
        List<Session> sessions    = getSessions();
        StringBuffer  sessionHtml = new StringBuffer(HtmlUtils.formTable());
        sessionHtml.append(
            HtmlUtils.row(
                HtmlUtils.cols(
                    HtmlUtils.bold(msg("User")),
                    HtmlUtils.bold(msg("Since")),
                    HtmlUtils.bold(msg("Last Activity")))));
        for (Session session : sessions) {
            String url = request.url(getRepositoryBase().URL_USER_LIST,
                                     ARG_REMOVESESSIONID, session.getId());
            sessionHtml.append(
                HtmlUtils.row(
                    HtmlUtils.cols(
                        HtmlUtils.href(
                            url, HtmlUtils.img(iconUrl(ICON_DELETE))) + " "
                                + session.user.getLabel(), formatDate(
                                    request, session.createDate), formatDate(
                                    request, session.getLastActivity()))));
        }
        sessionHtml.append(HtmlUtils.formTableClose());

        return sessionHtml;
    }





    /**
     * Class Session _more_
     *
     *
     * @author RAMADDA Development Team
     * @version $Revision: 1.3 $
     */
    public static class Session {

        /** _more_ */
        String id;

        /** _more_ */
        User user;

        /** _more_ */
        Date createDate;

        /** _more_ */
        Date lastActivity;

        /** _more_ */
        private Hashtable properties = new Hashtable();

        /**
         * _more_
         *
         * @param id _more_
         * @param user _more_
         * @param createDate _more_
         */
        public Session(String id, User user, Date createDate) {
            this(id, user, createDate, new Date());
        }

        /**
         * _more_
         *
         * @param id _more_
         * @param user _more_
         * @param createDate _more_
         * @param lastActivity _more_
         */
        public Session(String id, User user, Date createDate,
                       Date lastActivity) {
            this.id           = id;
            this.user         = user;
            this.createDate   = createDate;
            this.lastActivity = lastActivity;
        }

        /**
         * _more_
         *
         * @param key _more_
         * @param value _more_
         */
        public void putProperty(Object key, Object value) {
            properties.put(key, value);
        }

        /**
         * _more_
         *
         * @param key _more_
         *
         * @return _more_
         */
        public Object getProperty(Object key) {
            return properties.get(key);
        }

        /**
         *  Set the Id property.
         *
         *  @param value The new value for Id
         */
        public void setId(String value) {
            id = value;
        }

        /**
         *  Get the Id property.
         *
         *  @return The Id
         */
        public String getId() {
            return id;
        }

        /**
         *  Set the User property.
         *
         *  @param value The new value for User
         */
        public void setUser(User value) {
            user = value;
        }

        /**
         *  Get the User property.
         *
         *  @return The User
         */
        public User getUser() {
            return user;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public String getUserId() {
            if (user == null) {
                return "";
            }

            return user.getId();
        }

        /**
         *  Set the CreateDate property.
         *
         *  @param value The new value for CreateDate
         */
        public void setCreateDate(Date value) {
            createDate = value;
        }

        /**
         *  Get the CreateDate property.
         *
         *  @return The CreateDate
         */
        public Date getCreateDate() {
            return createDate;
        }

        /**
         *  Set the LastActivity property.
         *
         *  @param value The new value for LastActivity
         */
        public void setLastActivity(Date value) {
            lastActivity = value;
        }

        /**
         *  Get the LastActivity property.
         *
         *  @return The LastActivity
         */
        public Date getLastActivity() {
            return lastActivity;
        }



    }


}
