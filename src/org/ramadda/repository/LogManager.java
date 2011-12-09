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

package org.ramadda.repository;


import org.apache.log4j.Logger;



import org.ramadda.repository.auth.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;

import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;





import ucar.unidata.util.StringUtil;

import java.io.*;

import java.io.FileNotFoundException;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;




/**
 *
 *
 *  @author RAMADDA Development Team
 *  @version $Revision: 1.3 $
 */
public class LogManager extends RepositoryManager {

    /** _more_ */
    private Logger LOGGER;

    /** _more_ */
    private boolean LOGGER_OK = true;

    /** _more_ */
    public static boolean debug = true;


    /** _more_ */
    private List<LogEntry> log = new ArrayList<LogEntry>();

    /** _more_ */
    private int requestCount = 0;

    /**
     * _more_
     *
     * @param repository _more_
     */
    public LogManager(Repository repository) {
        super(repository);
    }


    /**
     * _more_
     */
    public void init() {}


    /**
     * _more_
     *
     * @param request _more_
     */
    public void logRequest(Request request) {
        requestCount++;
        //Keep the size of the log at 200
        synchronized (log) {
            while (log.size() > 200) {
                log.remove(0);
            }
            log.add(new LogEntry(request));
        }
    }



    /**
     * Create if needed and return the logger
     *
     * @return _more_
     */
    private Logger getLogger() {
        //Check if we've already had an error
        if ( !LOGGER_OK) {
            return null;
        }
        if (LOGGER == null) {
            try {
                LOGGER = Logger.getLogger(LogManager.class);
            } catch (Exception exc) {
                LOGGER_OK = false;
                System.err.println("Error getting logger: " + exc);
                exc.printStackTrace();
            }
        }
        return LOGGER;
    }


    /**
     * _more_
     *
     * @param message _more_
     */
    public void debug(String message) {
        Logger logger = getLogger();
        if (logger != null) {
            logger.debug(message);
        } else {
            System.err.println("RAMADDA DEBUG:" + message);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<LogEntry> getLog() {
        synchronized (log) {
            return new ArrayList<LogEntry>(log);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getRequestCount() {
        return requestCount;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param message _more_
     */
    public void log(Request request, String message) {
        logInfo("user:" + request.getUser() + " -- " + message);
    }


    /**
     * _more_
     *
     * @param message _more_
     */
    public void logInfoAndPrint(String message) {
        logInfo(message);
        System.err.println(message);
    }


    /**
     * _more_
     *
     * @param message _more_
     */
    public void logInfo(String message) {
        Logger logger = getLogger();
        if (logger != null) {
            logger.info(message);
        } else {
            System.err.println("RAMADDA INFO:" + message);
        }
    }


    /**
     * _more_
     *
     * @param message _more_
     */
    public void logError(String message) {
        Logger logger = getLogger();
        if (logger != null) {
            logger.error(message);
        } else {
            System.err.println("RAMADDA ERROR:" + message);
        }
    }


    /**
     * _more_
     *
     * @param message _more_
     */
    public void logWarning(String message) {
        Logger logger = getLogger();
        if (logger != null) {
            logger.warn(message);
        } else {
            System.err.println("RAMADDA WARNING:" + message);
        }
    }



    /**
     * _more_
     *
     * @param message _more_
     * @param exc _more_
     */
    public void logErrorAndPrint(String message, Throwable exc) {
        logError(message, exc);
        System.err.println(message);
        exc.printStackTrace();
    }


    /**
     * _more_
     *
     * @param message _more_
     * @param exc _more_
     */
    public void logError(String message, Throwable exc) {
        logError(getLogger(), message, exc);
    }

    /**
     * _more_
     *
     * @param log _more_
     * @param message _more_
     * @param exc _more_
     */
    public void logError(Logger log, String message, Throwable exc) {
        message = encode(message);
        Throwable thr = null;
        if (exc != null) {
            thr = LogUtil.getInnerException(exc);
        }


        if (log == null) {
            System.err.println("RAMADDA ERROR:" + message + " " + thr);
        }


        if (thr != null) {
            if ((thr instanceof RepositoryUtil.MissingEntryException)
                    || (thr instanceof AccessException)) {
                log.error(message + " " + thr);
            } else {
                log.error(message + "\n<stack>\n" + thr + "\n"
                          + LogUtil.getStackTrace(thr) + "\n</stack>");

                System.err.println("ERROR:" + message);
                thr.printStackTrace();
            }
        }

    }



    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private String encode(String s) {
        //If we do an entityEncode then the log can only be shown through the web
        s = s.replaceAll("([sS][cC][rR][iI][pP][tT])", "_$1_");
        s = s.replace("<", "&lt;");
        s = s.replace(">", "&gt;");
        return s;
    }


    /**
     * Class LogEntry _more_
     *
     *
     * @author RAMADDA Development Team
     */
    public class LogEntry {

        /** _more_ */
        User user;

        /** _more_ */
        Date date;

        /** _more_ */
        String path;

        /** _more_ */
        String ip;

        /** _more_ */
        String userAgent;

        /** _more_ */
        String url;

        /**
         * _more_
         *
         * @param request _more_
         */
        public LogEntry(Request request) {
            this.user = request.getUser();
            this.path = request.getRequestPath();

            String entryPrefix = getRepository().URL_ENTRY_SHOW.toString();
            if (this.path.startsWith(entryPrefix)) {
                url       = request.getUrl();
                this.path = this.path.substring(entryPrefix.length());
                if (path.trim().length() == 0) {
                    path = "/entry/show";
                }

            }

            this.date      = new Date();
            this.ip        = request.getIp();
            this.userAgent = request.getUserAgent();
        }


        /**
         *  Set the Ip property.
         *
         *  @param value The new value for Ip
         */
        public void setIp(String value) {
            ip = value;
        }

        /**
         *  Get the Ip property.
         *
         *  @return The Ip
         */
        public String getIp() {
            return ip;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public String getUrl() {
            return url;
        }

        /**
         *  Set the UserAgent property.
         *
         *  @param value The new value for UserAgent
         */
        public void setUserAgent(String value) {
            userAgent = value;
        }

        /**
         *  Get the UserAgent property.
         *
         *  @return The UserAgent
         */
        public String getUserAgent() {
            return userAgent;
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
         *  Set the Date property.
         *
         *  @param value The new value for Date
         */
        public void setDate(Date value) {
            date = value;
        }

        /**
         *  Get the Date property.
         *
         *  @return The Date
         */
        public Date getDate() {
            return date;
        }

        /**
         *  Set the Path property.
         *
         *  @param value The new value for Path
         */
        public void setPath(String value) {
            path = value;
        }

        /**
         *  Get the Path property.
         *
         *  @return The Path
         */
        public String getPath() {
            return path;
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
    public Result adminLog(Request request) throws Exception {
        StringBuffer sb       = new StringBuffer();
        List<String> header   = new ArrayList();
        File         f        = getStorageManager().getLogDir();
        File[]       logFiles = f.listFiles();
        String       log      = request.getString(ARG_LOG, "access");
        File         theFile  = null;
        boolean      didOne   = false;

        sb.append("Logs are in: " + HtmlUtil.italics(f.toString()));
        sb.append(HtmlUtil.p());

        if (log.equals("access")) {
            header.add(HtmlUtil.bold("Recent Access"));
        } else {
            header.add(
                HtmlUtil.href(
                    HtmlUtil.url(
                        getAdmin().URL_ADMIN_LOG.toString(), ARG_LOG,
                        "access"), "Recent Access"));
        }

        for (File logFile : logFiles) {
            if ( !logFile.toString().endsWith(".log")) {
                continue;
            }
            if (logFile.length() == 0) {
                continue;
            }
            String name  = logFile.getName();
            String label = IOUtil.stripExtension(name);
            label = StringUtil.camelCase(label);
            if (log.equals(name)) {
                header.add(HtmlUtil.bold(label));
                theFile = logFile;
            } else {
                header.add(
                    HtmlUtil.href(
                        HtmlUtil.url(
                            getAdmin().URL_ADMIN_LOG.toString(), ARG_LOG,
                            name), label));
            }
        }


        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.space(10));
        sb.append(StringUtil.join(HtmlUtil.span("&nbsp;|&nbsp;",
                HtmlUtil.cssClass(CSS_CLASS_SEPARATOR)), header));
        sb.append(HtmlUtil.hr());

        if (log.equals("access")) {
            getAccessLog(request, sb);
        } else {
            getErrorLog(request, sb, theFile);
        }

        return getAdmin().makeResult(request, msg("Logs"), sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param logFile _more_
     *
     * @throws Exception _more_
     */
    private void getErrorLog(Request request, StringBuffer sb, File logFile)
            throws Exception {
        FileInputStream fis = getStorageManager().getFileInputStream(logFile);
        try {
            String log      = request.getString(ARG_LOG, "error");
            int    numBytes = request.get(ARG_BYTES, 10000);
            if (numBytes < 0) {
                numBytes = 100;
            }
            long length = logFile.length();
            long offset = length - numBytes;
            if (numBytes < length) {
                sb.append(
                    HtmlUtil.href(
                        HtmlUtil.url(
                            getAdmin().URL_ADMIN_LOG.toString(), ARG_LOG,
                            log, ARG_BYTES, numBytes + 2000), "More..."));
            }
            sb.append(HtmlUtil.space(2));
            sb.append(
                HtmlUtil.href(
                    HtmlUtil.url(
                        getAdmin().URL_ADMIN_LOG.toString(), ARG_LOG, log,
                        ARG_BYTES, numBytes - 2000), "Less..."));

            sb.append(HtmlUtil.br());
            if (offset > 0) {
                fis.skip(offset);
            } else {
                numBytes = (int) length;
            }
            byte[] bytes = new byte[numBytes];
            fis.read(bytes);
            String       logString    = new String(bytes);
            boolean      didOne       = false;
            StringBuffer stackSB      = null;
            boolean      lastOneBlank = false;
            for (String line :
                    StringUtil.split(logString, "\n", false, false)) {
                if ( !didOne) {
                    didOne = true;
                    continue;
                }
                line = line.trim();
                if (line.length() == 0) {
                    if (lastOneBlank) {
                        continue;
                    }
                    lastOneBlank = true;
                } else {
                    lastOneBlank = false;
                }
                if (line.startsWith("</stack>") && (stackSB != null)) {
                    sb.append(
                        HtmlUtil.insetLeft(
                            HtmlUtil.makeShowHideBlock(
                                "Stack trace",
                                HtmlUtil.div(
                                    stackSB.toString(),
                                    HtmlUtil.cssClass(
                                        CSS_CLASS_STACK)), false), 10));
                    sb.append("<br>");
                    stackSB = null;
                } else if (stackSB != null) {
                    line = HtmlUtil.entityEncode(line);
                    line = line.replaceAll("\t", "&nbsp;");
                    stackSB.append(line);
                    stackSB.append("<br>");
                } else if (line.startsWith("<stack>")) {
                    stackSB = new StringBuffer();
                } else {
                    line = HtmlUtil.entityEncode(line);
                    line = line.replaceAll("\t", "&nbsp;");
                    sb.append(line);
                    sb.append("<br>");
                    sb.append("\n");
                }
            }
            if (stackSB != null) {
                sb.append(
                    HtmlUtil.makeShowHideBlock(
                        "Stack trace",
                        HtmlUtil.div(
                            stackSB.toString(),
                            HtmlUtil.cssClass(CSS_CLASS_STACK)), false));
            }

            //        sb.append("</pre>");
        } finally {
            IOUtil.close(fis);
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    private void getAccessLog(Request request, StringBuffer sb)
            throws Exception {

        sb.append(HtmlUtil.open(HtmlUtil.TAG_TABLE));
        sb.append(HtmlUtil.row(HtmlUtil.cols(HtmlUtil.b(msg("User")),
                                             HtmlUtil.b(msg("Date")),
                                             HtmlUtil.b(msg("Path")),
                                             HtmlUtil.b(msg("IP")),
                                             HtmlUtil.b(msg("User agent")))));
        List<LogManager.LogEntry> log = getLogManager().getLog();
        for (int i = log.size() - 1; i >= 0; i--) {
            LogManager.LogEntry logEntry = log.get(i);
            //Encode the path just in case the user does a XSS attack
            String path = logEntry.getPath();
            if (path.length() > 50) {
                path = path.substring(0, 49) + "...";
            }
            path = HtmlUtil.entityEncode(path);
            if (logEntry.getUrl() != null) {
                path = HtmlUtil.href(logEntry.getUrl(), path);
            }
            String  userAgent = logEntry.getUserAgent();
            boolean isBot     = true;
            if (userAgent.indexOf("Googlebot") >= 0) {
                userAgent = "Googlebot";
            } else if (userAgent.indexOf("Slurp") >= 0) {
                userAgent = "Yahoobot";
            } else if (userAgent.indexOf("msnbot") >= 0) {
                userAgent = "Msnbot";
            } else {
                isBot = false;
                String full = userAgent;
                int    idx  = userAgent.indexOf("(");
                if (idx > 0) {
                    userAgent = userAgent.substring(0, idx);
                    userAgent = HtmlUtil.makeShowHideBlock(
                        HtmlUtil.entityEncode(userAgent), full, false);
                }



            }

            String dttm = getRepository().formatDate(logEntry.getDate());
            dttm = dttm.replace(" ", "&nbsp;");
            String user = logEntry.getUser().getLabel();
            user = user.replace(" ", "&nbsp;");
            String cols = HtmlUtil.cols(user, dttm, path, logEntry.getIp(),
                                        userAgent);
            sb.append(HtmlUtil.row(cols,
                                   HtmlUtil.attr(HtmlUtil.ATTR_VALIGN, "top")
                                   + ( !isBot
                                       ? ""
                                       : HtmlUtil.attr(HtmlUtil.ATTR_BGCOLOR,
                                       "#eeeeee"))));

        }
        sb.append(HtmlUtil.close(HtmlUtil.TAG_TABLE));

    }


}
