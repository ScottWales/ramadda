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

package org.ramadda.repository.auth;


import org.ramadda.repository.*;


import org.ramadda.repository.database.*;


import org.ramadda.sql.Clause;


import org.ramadda.sql.SqlUtil;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TTLCache;

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

    /** _more_ */
    private static boolean debugSession = false;


    /** The number of days a session is active in the database */
    private static final double SESSION_DAYS = 2.0;


    /** _more_ */
    public static final String COOKIE_NAME = "repositorysession";


    /** _more_ */
    private String cookieName;

    /** _more_ */
    private Hashtable<String, Session> sessionMap = new Hashtable<String,
                                                        Session>();

    //This holds sessions for anonymous users. The timeout is 24 hours. Max size is 1000

    /** _more_ */
    private TTLCache<String, Session> anonymousSessionMap =
        new TTLCache<String, Session>(1000 * 3600 * 24, 1000);




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
        debugSession = repository.getProperty("ramadda.debug.session", false);
        this.cookieName = "ramadda"
                          + repository.getUrlBase().replaceAll("/", "_")
                          + "_session";
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
     * @param msg _more_
     */
    public static void debugSession(String msg) {
        if (debugSession) {
            System.err.println(msg);
        }
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void deleteAllSessions() throws Exception {
        debugSession("RAMADDA.deleteAllSessions");
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
            debugSession("RAMADDA.cullSessions: removing old session:"
                         + session);
            removeSession(session.getId());
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
    public Entry getLastEntry(Request request) throws Exception {
        return (Entry) getSessionProperty(request, "lastentry");
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void setLastEntry(Request request, Entry entry) throws Exception {
        if ((entry != null) && (request != null)) {
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
        if (request == null) {
            return;
        }

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
        if (request == null) {
            return dflt;
        }

        //        System.err.println("getSession:" + key);
        String id = request.getSessionId();
        if (id == null) {
            Object obj = request.getExtraProperty(key);
            if (obj != null) {
                return obj;
            }

            return dflt;
        }
        Session session = getSession(id);
        if (session == null) {
            Object obj = request.getExtraProperty(key);
            if (obj != null) {
                return obj;
            }

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

    /**
     * _more_
     *
     * @param sessionId _more_
     * @param checkAnonymous _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Session getSession(String sessionId, boolean checkAnonymous)
            throws Exception {
        return getSession(sessionId, checkAnonymous, false);
    }

    /**
     * _more_
     *
     * @param sessionId _more_
     * @param checkAnonymous _more_
     * @param debug _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Session getSession(String sessionId, boolean checkAnonymous,
                              boolean debug)
            throws Exception {
        Session session = sessionMap.get(sessionId);
        if (session != null) {
            //            debugSession("RAMADDA.getSession got session from session map:" + session);
            return session;
        }
        session = anonymousSessionMap.get(sessionId);
        if (session != null) {
            debugSession(
                "RAMADDA.getSession got session from anonymous session map: "
                + session);

            return session;
        }

        Statement stmt = getDatabaseManager().select(Tables.SESSIONS.COLUMNS,
                             Tables.SESSIONS.NAME,
                             Clause.eq(Tables.SESSIONS.COL_SESSION_ID,
                                       sessionId));
        try {
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(stmt);
            ResultSet        results;
            while ((results = iter.getNext()) != null) {
                session = makeSession(results);
                debugSession("RAMADDA.getSession got session from database:"
                             + session);
                session.setLastActivity(new Date());
                //Remove it from the DB and then re-add it so we update the lastActivity
                removeSession(session.getId());
                addSession(session);

                break;
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
        debugSession("RAMADDA.removeSession:" + sessionId);
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
    private void addSession(Session session) throws Exception {
        debugSession("RAMADDA.addSession:" + session);
        sessionMap.put(session.getId(), session);
        getDatabaseManager().executeInsert(Tables.SESSIONS.INSERT,
                                           new Object[] { session.getId(),
                session.getUserId(), session.getCreateDate(),
                session.getLastActivity(),
                "" });
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
        List<String> cookies = getCookies(request);
        for (String cookieValue : cookies) {
            if (user == null) {
                Session session = getSession(cookieValue, false);
                if (session != null) {
                    session.setLastActivity(new Date());
                    user = getUserManager().getCurrentUser(session.getUser());
                    session.setUser(user);
                    request.setSessionId(cookieValue);

                    break;
                }
            }
        }

        //Check for the session id as a url argument
        if ((user == null) && request.hasParameter(ARG_SESSIONID)) {
            String sessionId = request.getString(ARG_SESSIONID);
            debugSession("RAMADDA: has sessionid argument:" + sessionId);
            Session session = getSession(sessionId, false, true);
            if (session != null) {
                session.setLastActivity(new Date());
                user = getUserManager().getCurrentUser(session.getUser());
                session.setUser(user);
                debugSession("RAMADDA: found sesssion user =" + user);
            } else {
                debugSession("RAMADDA: could not find session:" + sessionId);
                debugSession("RAMADDA: sessionMap:" + sessionMap);

                //Puke out of here
                throw new IllegalStateException("Invalid session:"
                        + sessionId);
                //                user = getUserManager().getAnonymousUser();
                //                session.setUser(user);
                //                request.setSessionId(createSessionId());
            }
        }

        //Check for url auth
        if ((user == null) && request.exists(ARG_AUTH_USER)
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
            createSession(request, user);
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
                        if (user == null) {}
                        else if ( !getUserManager().isPasswordValid(user,
                                toks[1])) {
                            user = null;
                        } else {}
                    }
                    if (user != null) {
                        createSession(request, user);
                    }
                }
            }
        }


        //Make sure we have the current user state
        user = getUserManager().getCurrentUser(user);

        if ((request.getSessionId() == null)
                && !request.defined(ARG_SESSIONID)) {
            request.setSessionId(createSessionId());
        }

        if (user == null) {
            user = getUserManager().getAnonymousUser();
            //Create a temporary session
            Session session = anonymousSessionMap.get(request.getSessionId());
            if (session == null) {
                session = new Session(request.getSessionId(), user,
                                      new Date());
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



    /**
     * _more_
     *
     * @return _more_
     */
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
     *
     * @return _more_
     * @throws Exception _more_
     */
    public Session createSession(Request request, User user)
            throws Exception {
        if (request.getSessionId() == null) {
            request.setSessionId(createSessionId());
        }
        Session session = new Session(request.getSessionId(), user,
                                      new Date());
        addSession(session);
        request.setUser(user);

        return session;
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
                    HtmlUtils.bold(msg("Last Activity")),
                    HtmlUtils.bold(msg("Session ID")))));
        for (Session session : sessions) {
            String url = request.url(getRepositoryBase().URL_USER_LIST,
                                     ARG_REMOVESESSIONID, session.getId());
            sessionHtml.append(HtmlUtils.row(HtmlUtils.cols(HtmlUtils.href(url,
                    HtmlUtils.img(iconUrl(ICON_DELETE))) + " "
                        + session.getUser().getLabel(), formatDate(request,
                            session.getCreateDate()), formatDate(request,
                                session.getLastActivity()), session.getId())));
        }
        sessionHtml.append(HtmlUtils.formTableClose());

        return sessionHtml;
    }






}
