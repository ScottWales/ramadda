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

package org.ramadda.repository;


import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;


import org.ramadda.repository.admin.*;

import org.ramadda.repository.auth.*;


import org.ramadda.repository.database.*;

import org.ramadda.repository.ftp.FtpManager;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.monitor.*;

import org.ramadda.repository.output.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;
import org.ramadda.repository.util.*;
import org.ramadda.repository.util.ServerInfo;

import org.ramadda.util.HtmlTemplate;
import org.ramadda.util.PropertyProvider;

import org.ramadda.util.TempDir;


import org.w3c.dom.*;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;

import ucar.unidata.util.Cache;
import ucar.unidata.util.CacheManager;

import ucar.unidata.util.Counter;
import ucar.unidata.util.DateUtil;
import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlEncoder;

import ucar.unidata.xml.XmlUtil;



import java.io.*;

import java.io.File;
import java.io.InputStream;

import java.lang.reflect.*;

import java.net.*;


import java.sql.ResultSet;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;


import java.util.regex.*;
import java.util.zip.*;




/**
 * The main class.
 *
 */
public class Repository extends RepositoryBase implements RequestHandler,
        PropertyProvider {

    /** _more_ */
    private static final org.ramadda.util.HttpFormField dummyField1ToForceCompile =
        null;

    /** _more_          */
    private static final org.ramadda.util.ObjectPool dummyField2ToForceCompile =
        null;

    /** _more_          */
    private static final org.ramadda.util.EntryGroup dummyField3ToForceCompile =
        null;




    /** _more_ */
    public static final String PROP_CACHERESOURCES = "ramadda.cacheresources";



    /** _more_ */
    protected List<RequestUrl> entryEditUrls;

    /** _more_ */
    protected List<RequestUrl> groupEditUrls;

    /** _more_ */
    List<RequestUrl> initializedUrls = new ArrayList<RequestUrl>();

    /** _more_ */
    private static final int PAGE_CACHE_LIMIT = 100;


    /** _more_ */
    public static final OutputType OUTPUT_DELETER =
        new OutputType("Delete Entry", "repository.delete",
                       OutputType.TYPE_ACTION | OutputType.TYPE_EDIT, "",
                       ICON_DELETE);


    /** _more_ */
    public static final OutputType OUTPUT_TYPECHANGE =
        new OutputType("Change Type", "repository.typechange",
                       OutputType.TYPE_ACTION | OutputType.TYPE_EDIT, "",
                       null);

    /** _more_ */
    public static final OutputType OUTPUT_PUBLISH =
        new OutputType("Make Public", "repository.makepublic",
                       OutputType.TYPE_ACTION | OutputType.TYPE_EDIT, "",
                       ICON_PUBLISH);


    /** _more_ */
    public static final OutputType OUTPUT_METADATA_FULL =
        new OutputType("Add full metadata", "repository.metadata.full",
                       OutputType.TYPE_ACTION | OutputType.TYPE_EDIT, "",
                       ICON_METADATA_ADD);

    /** _more_ */
    public static final OutputType OUTPUT_METADATA_SHORT =
        new OutputType("Add short metadata", "repository.metadata.short",
                       OutputType.TYPE_ACTION | OutputType.TYPE_EDIT, "",
                       ICON_METADATA_ADD);


    /** _more_ */
    public static final OutputType OUTPUT_COPY =
        new OutputType("Copy/Move/Link", "repository.copymovelink",
                       OutputType.TYPE_ACTION | OutputType.TYPE_EDIT, "",
                       ICON_MOVE);

    /** _more_ */
    public static final OutputType OUTPUT_FILELISTING =
        new OutputType("File Listing", "repository.filelisting",
                       OutputType.TYPE_FILE, "", ICON_FILELISTING);


    /** _more_ */
    private UserManager userManager;

    /** _more_ */
    private MonitorManager monitorManager;

    /** _more_ */
    private SessionManager sessionManager;

    /** _more_ */
    private WikiManager wikiManager;

    /** _more_ */
    private LogManager logManager;

    /** _more_ */
    private EntryManager entryManager;

    /** _more_ */
    private PageHandler pageHandler;

    /** _more_ */
    private AssociationManager associationManager;

    /** _more_ */
    private SearchManager searchManager;

    /** _more_ */
    private MapManager mapManager;

    /** _more_ */
    private HarvesterManager harvesterManager;

    /** _more_ */
    private ActionManager actionManager;

    /** _more_ */
    private AccessManager accessManager;

    /** _more_ */
    private MetadataManager metadataManager;

    /** _more_ */
    private RegistryManager registryManager;

    /** _more_ */
    private StorageManager storageManager;

    /** _more_ */
    private PluginManager pluginManager;

    /** _more_ */
    private DatabaseManager databaseManager;

    /** _more_ */
    private FtpManager ftpManager;

    /** _more_ */
    private Admin admin;

    /** _more_ */
    private List<RepositoryManager> repositoryManagers =
        new ArrayList<RepositoryManager>();

    /** _more_ */
    private String cookieExpirationDate;

    /** _more_ */
    private Counter numberOfCurrentRequests = new Counter();

    /** _more_ */
    private Properties mimeTypes;


    /** _more_ */
    private Properties properties = new Properties();

    /** _more_ */
    private Properties cmdLineProperties = new Properties();

    /** _more_ */
    private Map<String, String> systemEnv;

    /** _more_ */
    private Properties dbProperties = new Properties();




    /** _more_ */
    private static XmlEncoder xmlEncoder;


    /** _more_ */
    private long baseTime = System.currentTimeMillis();

    /** _more_ */
    ucar.unidata.util.SocketConnection dummyConnection;

    /** _more_ */
    private List<String> sqlLoadFiles = new ArrayList<String>();

    /** _more_ */
    private List<EntryChecker> entryMonitors = new ArrayList<EntryChecker>();

    /** _more_ */
    private String dumpFile;


    /** _more_ */
    private Date startTime = new Date();



    /** _more_ */
    private Hashtable<String, TypeHandler> typeHandlersMap =
        new Hashtable<String, TypeHandler>();

    /** _more_ */
    private List<TypeHandler> allTypeHandlers = new ArrayList<TypeHandler>();

    /** _more_ */
    private List<OutputHandler> outputHandlers =
        new ArrayList<OutputHandler>();

    /** _more_ */
    private Hashtable<String, OutputType> outputTypeMap =
        new Hashtable<String, OutputType>();


    /** _more_ */
    private List<OutputHandler> allOutputHandlers =
        new ArrayList<OutputHandler>();



    /** _more_ */
    private Hashtable resources = new Hashtable();


    /** _more_ */
    private Hashtable namesHolder = new Hashtable();


    /** _more_ */
    private List<User> cmdLineUsers = new ArrayList();


    /** _more_ */
    String[] args;


    /** _more_ */
    public static boolean debug = true;


    /** _more_ */
    private GroupTypeHandler groupTypeHandler;



    /** _more_ */
    private List categoryList = null;

    /** _more_ */
    private List<String> htdocRoots = new ArrayList<String>();




    /** _more_ */
    private List<File> localFilePaths = new ArrayList<File>();


    /** _more_ */
    ApiMethod homeApi;

    /** _more_ */
    private Hashtable<String, RequestHandler> apiHandlers =
        new Hashtable<String, RequestHandler>();

    /** _more_ */
    Hashtable<String, ApiMethod> requestMap = new Hashtable();

    /** _more_ */
    ArrayList<ApiMethod> apiMethods = new ArrayList();

    /** _more_ */
    ArrayList<ApiMethod> wildCardApiMethods = new ArrayList();

    /** _more_ */
    ArrayList<ApiMethod> topLevelMethods = new ArrayList();


    /** _more_ */
    private HttpClient httpClient;


    /** _more_ */
    private boolean active = true;

    /** _more_ */
    private boolean readOnly = false;

    /** _more_ */
    private boolean doCache = true;




    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public Repository() throws Exception {}



    /**
     * _more_
     *
     * @param args _more_
     * @param port _more_
     *
     * @throws Exception _more_
     */
    public Repository(String[] args, int port) throws Exception {
        super(port);
        init(args, port);
    }

    /**
     * _more_
     *
     * @param args _more_
     * @param port _more_
     *
     * @throws Exception _more_
     */
    public void init(String[] args, int port) throws Exception {
        setPort(port);
        xmlEncoder = getEncoder();
        LogUtil.setTestMode(true);
        try {
            java.net.InetAddress localMachine =
                java.net.InetAddress.getLocalHost();
            setHostname(localMachine.getHostName());
            setIpAddress(localMachine.getHostAddress());
        } catch (Exception exc) {
            System.err.println("Got exception accessing local hostname");
            exc.printStackTrace();
            setHostname("unknown");
            setIpAddress("unknown");
        }
        this.args     = args;

        entryEditUrls = RepositoryUtil.toList(new RequestUrl[] {
            URL_ENTRY_FORM, getMetadataManager().URL_METADATA_FORM,
            getMetadataManager().URL_METADATA_ADDFORM,
            URL_ACCESS_FORM  //,
            //        URL_ENTRY_DELETE
            //        URL_ENTRY_SHOW
        });

        groupEditUrls = RepositoryUtil.toList(new RequestUrl[] {
            URL_ENTRY_NEW, URL_ENTRY_FORM,
            getMetadataManager().URL_METADATA_FORM,
            getMetadataManager().URL_METADATA_ADDFORM,
            URL_ACCESS_FORM  //,
            //        URL_ENTRY_DELETE
            //        URL_ENTRY_SHOW
        });


    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<RepositoryManager> getRepositoryManagers() {
        return repositoryManagers;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getHostname() {
        String hostname = getProperty(PROP_HOSTNAME, (String) null);
        if ((hostname != null) && (hostname.trim().length() > 0)) {
            if (hostname.equals("ipaddress")) {
                return getIpAddress();
            }

            return hostname;
        }

        return super.getHostname();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getPort() {
        String port = getProperty(PROP_PORT, (String) null);

        if (port != null) {
            port = port.trim();
            if (port.length() > 0) {
                return Integer.decode(port).intValue();
            }
        }

        return super.getPort();
    }


    /** _more_ */
    private boolean ignoreSSL = false;

    /**
     * _more_
     *
     *
     * @param request The request
     * @return _more_
     */
    public boolean isSSLEnabled(Request request) {
        if (ignoreSSL) {
            return false;
        }
        if (getProperty(PROP_SSL_IGNORE, false)) {
            return false;
        }

        return getHttpsPort() >= 0;
    }





    /**
     * _more_
     *
     * @param request The request
     * @param ms _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, long ms) {
        return formatDate(new Date(ms));
    }


    /**
     * _more_
     *
     * @param request The request
     * @param ms _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, long ms, String timezone) {
        return formatDate(new Date(ms), timezone);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param d _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, Date d) {
        return formatDate(d);
    }


    /**
     * _more_
     *
     * @param request The request
     * @param d _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, Date d, String timezone) {
        return formatDate(d, timezone);
    }


    /**
     * _more_
     *
     * @param request The request
     * @param d _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String formatDateShort(Request request, Date d, String timezone) {
        return formatDateShort(request, d, timezone, "");
    }

    /**
     * _more_
     *
     * @param request The request
     * @param d _more_
     * @param timezone _more_
     * @param extraAlt _more_
     *
     * @return _more_
     */
    public String formatDateShort(Request request, Date d, String timezone,
                                  String extraAlt) {
        SimpleDateFormat sdf = getSDF(getProperty(PROP_DATE_SHORTFORMAT,
                                   DEFAULT_TIME_SHORTFORMAT), timezone);
        if (d == null) {
            return BLANK;
        }

        Date   now      = new Date();
        long   diff     = now.getTime() - d.getTime();
        double minutes  = DateUtil.millisToMinutes(diff);
        String fullDate = formatDate(d, timezone);
        String result;
        if ((minutes > 0) && (minutes < 65) && (minutes > 55)) {
            result = "about an hour ago";
        } else if ((diff > 0) && (diff < DateUtil.minutesToMillis(1))) {
            result = (int) (diff / (1000)) + " seconds ago";
        } else if ((diff > 0) && (diff < DateUtil.hoursToMillis(1))) {
            int value = (int) DateUtil.millisToMinutes(diff);
            result = value + " minute" + ((value > 1)
                                          ? "s"
                                          : "") + " ago";
        } else if ((diff > 0) && (diff < DateUtil.hoursToMillis(24))) {
            int value = (int) (diff / (1000 * 60 * 60));
            result = value + " hour" + ((value > 1)
                                        ? "s"
                                        : "") + " ago";
        } else if ((diff > 0) && (diff < DateUtil.daysToMillis(6))) {
            int value = (int) (diff / (1000 * 60 * 60 * 24));
            result = value + " day" + ((value > 1)
                                       ? "s"
                                       : "") + " ago";
        } else {
            result = sdf.format(d);
        }

        return HtmlUtils.span(result,
                             HtmlUtils.cssClass(CSS_CLASS_DATETIME)
                             + HtmlUtils.attr(HtmlUtils.ATTR_TITLE,
                                             fullDate + extraAlt));
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean doCache() {
        //Don't cache even when we're in readonly mode
        //        if (readOnly) {
        //            return false;
        //        }
        return doCache;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getActive() {
        return active;
    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void close() throws Exception {
        shutdown();
    }

    /**
     * _more_
     */
    public void shutdown() {
        try {
            System.err.println("RAMADDA: shutting down");
            active = false;
            for (RepositoryManager repositoryManager : repositoryManagers) {
                try {
                    repositoryManager.shutdown();
                } catch (Throwable thr) {
                    System.err.println(
                        "RAMADDA: Error shutting down:"
                        + repositoryManager.getClass().getName() + " " + thr);
                }
            }
            repositoryManagers = null;
            userManager        = null;
            monitorManager     = null;
            sessionManager     = null;
            wikiManager        = null;
            logManager         = null;
            entryManager       = null;
            associationManager = null;
            searchManager      = null;
            mapManager         = null;
            harvesterManager   = null;
            actionManager      = null;
            accessManager      = null;
            metadataManager    = null;
            registryManager    = null;
            storageManager     = null;
            pluginManager      = null;
            databaseManager    = null;
            ftpManager         = null;
            admin              = null;
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }


    /**
     * _more_
     *
     * @param port _more_
     */
    public void setHttpsPort(int port) {
        super.setHttpsPort(port);
        reinitializeRequestUrls();
    }


    /**
     * _more_
     *
     *
     * @param properties _more_
     * @throws Exception _more_
     */
    public void init(Properties properties) throws Exception {


        /*
                final PrintStream oldErr = System.err;
                final PrintStream oldOut = System.out;
                System.setErr(new PrintStream(oldOut){
                        public void     println(String x) {
                            Misc.printStack("got it");
                            oldErr.println(x);
                        }
                    });
        */

        //This stops jython from processing jars and printing out its annoying message
        System.setProperty("python.cachedir.skip", "true");

        CacheManager.setDoCache(false);
        initProperties(properties);
        initServer();
        getLogManager().logInfoAndPrint("RAMADDA: repository started");
    }

    /**
     * _more_
     *
     * @param properties _more_
     * @param path _more_
     *
     * @throws Exception _more_
     */
    public void loadProperties(Properties properties, String path)
            throws Exception {
        //        System.err.println ("RAMADDA:  loading " + path);
        InputStream inputStream = IOUtil.getInputStream(path, getClass());
        if (inputStream == null) {
            System.err.println("RAMADDA:  null properties: " + path);

            return;
        }
        properties.load(inputStream);
        IOUtil.close(inputStream);
    }

    /**
     * _more_
     *
     *
     * @param contextProperties _more_
     * @throws Exception _more_
     */
    public void initProperties(Properties contextProperties)
            throws Exception {

        //        System.err.println("RAMADDA: initializing properties");
        /*
          order in which we load properties files
          system
          context (e.g., from tomcat web-inf)
          cmd line args (both -Dname=value and .properties files)
          (We load the above so we can define an alternate repository dir)
          local repository directory
          (Now load in the cmd line again because they have precedence over anything else);
          cmd line
         */

        properties = new Properties();
        loadProperties(
            properties,
            "/org/ramadda/repository/resources/repository.properties");

        try {
            loadProperties(
                properties,
                "/org/ramadda/repository/resources/build.properties");
        } catch (Exception exc) {}

        for (int i = 0; i < args.length; i++) {
            if (getPluginManager().checkFile(args[i])) {
                continue;
            }
            if (args[i].endsWith(".properties")) {
                loadProperties(cmdLineProperties, args[i]);
            } else if (args[i].equals("-dump")) {
                dumpFile = args[i + 1];
                i++;
            } else if (args[i].equals("-load")) {
                sqlLoadFiles.add(args[i + 1]);
                i++;
            } else if (args[i].equals("-admin")) {
                User user = new User(args[i + 1], true);
                user.setPasswords(args[i + 2],
                                  RepositoryUtil.hashPassword(args[i + 2]));
                cmdLineUsers.add(user);
                i += 2;
            } else if (args[i].equals("-port")) {
                //skip
                i++;
            } else if (args[i].startsWith("-D")) {
                String       s    = args[i].substring(2);
                List<String> toks = StringUtil.split(s, "=", true, true);
                if (toks.size() == 0) {
                    throw new IllegalArgumentException("Bad argument:"
                            + args[i]);
                } else if (toks.size() == 1) {
                    cmdLineProperties.put(toks.get(0), "");
                } else {
                    cmdLineProperties.put(toks.get(0), toks.get(1));
                }
            } else {
                usage("Unknown argument: " + args[i]);
            }
        }

        //Load the context and the command line properties now 
        //so the storage manager can get to them
        if (contextProperties != null) {
            properties.putAll(contextProperties);
        }


        //Now look around the tomcat environment                                          
        String catalinaBase = null;
        for (String arg : new String[] { "CATALINA_BASE", "catalina.base",
                                         "CATALINA_HOME", "catalina.home" }) {
            catalinaBase = getProperty(arg);
            if (catalinaBase != null) {
                break;
            }
        }
        if (catalinaBase != null) {
            File catalinaConfFile = new File(catalinaBase
                                             + "/conf/repository.properties");
            if (catalinaConfFile.exists()) {
                System.err.println("RAMADDA: loading:" + catalinaConfFile);
                loadProperties(properties, catalinaConfFile.toString());
            } else {
                //A hack to run on unavco facility server
                if (new File("/export/home/jeffmc/ramaddadev").exists()) {
                    System.err.println(
                        "RAMADDA:  Using /export/home/jeffmc/ramaddadev");
                    properties.put(PROP_REPOSITORY_HOME,
                                   "/export/home/jeffmc/ramaddadev");
                }
            }
        }

        //check for glassfish, e.g.:
        //$GLASSFISH_HOME/glassfish/config/repository.properties
        String glassfish = getProperty("GLASSFISH_HOME");
        if (glassfish != null) {
            File confFile =
                new File(glassfish
                         + "/glassfish/config/repository.properties");
            if (confFile.exists()) {
                System.err.println("RAMADDA: loading:" + confFile);
                loadProperties(properties, confFile.toString());
            }
        }


        //Call the storage manager so it can figure out the home dir
        getStorageManager();

        try {
            //Now load in the local properties file
            //First load in the repository.properties file
            String localPropertyFile =
                IOUtil.joinDir(getStorageManager().getRepositoryDir(),
                               "repository.properties");

            if (new File(localPropertyFile).exists()) {
                System.err.println("RAMADDA: loading local property file:"
                                   + localPropertyFile);
                loadProperties(properties, localPropertyFile);
            } else {}

            File[] localFiles =
                getStorageManager().getRepositoryDir().listFiles();
            for (File f : localFiles) {
                if ( !f.toString().endsWith(".properties")) {
                    continue;
                }
                if (f.getName().equals("repository.properties")) {
                    continue;
                }
                loadProperties(properties, f.toString());
            }

        } catch (Exception exc) {}

        //create the log dir
        getStorageManager().getLogDir();

        //initialize the plugin manager with the properties
        getPluginManager().init(properties);

        debug = getProperty(PROP_DEBUG, false);
        //        System.err.println ("debug:" + debug);

        readOnly = getProperty(PROP_READ_ONLY, false);
        doCache  = getProperty(PROP_DOCACHE, true);
        if (readOnly) {
            System.err.println("RAMADDA: running in readonly mode");
        }
        if ( !doCache) {
            System.err.println("RAMADDA: running with no in-memory cache");
        }

        setUrlBase((String) properties.get(PROP_HTML_URLBASE));
        if (getUrlBase() == null) {
            setUrlBase(BLANK);
        }

        String derbyHome = (String) properties.get(PROP_DB_DERBY_HOME);
        if (derbyHome != null) {
            derbyHome = getStorageManager().localizePath(derbyHome);
            File dir = new File(derbyHome);
            IOUtil.makeDirRecursive(dir);
            System.setProperty("derby.system.home", derbyHome);
        }

        mimeTypes = new Properties();
        for (String path : getResourcePaths(PROP_HTML_MIMEPROPERTIES)) {
            try {
                loadProperties(mimeTypes, path);
            } catch (Exception exc) {
                //noop
            }
        }

        sdf = RepositoryUtil.makeDateFormat(getProperty(PROP_DATEFORMAT,
                DEFAULT_TIME_FORMAT));
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(RepositoryUtil.TIMEZONE_DEFAULT);

        //This will end up being from the properties
        htdocRoots.addAll(
            StringUtil.split(
                getProperty("ramadda.html.htdocroots", BLANK), ";", true,
                true));

        initProxy();

    }



    /**
     * _more_
     */
    private void initProxy() {
        //First try the local ramadda properties
        //The default value is the system property 
        String proxyHost = getProperty(PROP_PROXY_HOST,
                                       getProperty("http.proxyHost",
                                           (String) null));
        String proxyPort = getProperty(PROP_PROXY_PORT,
                                       getProperty("http.proxyPort", "8080"));
        final String proxyUser = getProperty(PROP_PROXY_USER, (String) null);
        final String proxyPass = getProperty(PROP_PROXY_PASSWORD,
                                             (String) null);
        httpClient = new HttpClient();
        if (proxyHost != null) {
            getLogManager().logInfoAndPrint("Setting proxy server to:"
                                            + proxyHost + ":" + proxyPort);
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", proxyPort);
            System.setProperty("ftp.proxyHost", proxyHost);
            System.setProperty("ftp.proxyPort", proxyPort);
            httpClient.getHostConfiguration().setProxy(proxyHost,
                    Integer.parseInt(proxyPort));
            // Just if proxy has authentication credentials
            if (proxyUser != null) {
                getLogManager().logInfoAndPrint("Setting proxy user to:"
                        + proxyUser);
                httpClient.getParams().setAuthenticationPreemptive(true);
                Credentials defaultcreds =
                    new UsernamePasswordCredentials(proxyUser, proxyPass);
                httpClient.getState().setProxyCredentials(
                    new AuthScope(
                        proxyHost, Integer.parseInt(proxyPort),
                        AuthScope.ANY_REALM), defaultcreds);
                Authenticator.setDefault(new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(proxyUser,
                                proxyPass.toCharArray());
                    }
                });
            }
        }


    }


    /**
     * _more_
     *
     * @return _more_
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }



    /**
     * _more_
     *
     * @throws Exception _more_
     */
    protected void initServer() throws Exception {

        getDatabaseManager().init();
        initDefaultTypeHandlers();

        boolean loadedRdb = false;
        for (String sqlFile : (List<String>) sqlLoadFiles) {
            if (sqlFile.endsWith(".rdb")) {
                getDatabaseManager().loadRdbFile(sqlFile);
                loadedRdb = true;
            }
        }

        if ( !loadedRdb) {
            initSchema();
        }


        readGlobals();
        checkVersion();

        loadPlugins();

        initDefaultOutputHandlers();

        getRegistryManager().checkApi();

        //Load in any other sql files from the command line
        for (String sqlFile : (List<String>) sqlLoadFiles) {
            if ( !sqlFile.endsWith(".rdb")) {
                String sql =
                    getStorageManager().readUncheckedSystemResource(sqlFile);
                getDatabaseManager().loadSql(sql, false, true);
                readGlobals();
            }
        }


        getUserManager().initUsers(cmdLineUsers);

        //This finds or creates the top-level group
        getEntryManager().initTopEntry();


        setLocalFilePaths();

        if (dumpFile != null) {
            FileOutputStream fos = new FileOutputStream(dumpFile);
            getDatabaseManager().makeDatabaseCopy(fos, true, null);
            IOUtil.close(fos);
        }

        HtmlUtils.setBlockHideShowImage(iconUrl(ICON_MINUS),
                                       iconUrl(ICON_PLUS));
        HtmlUtils.setInlineHideShowImage(iconUrl(ICON_MINUS),
        //iconUrl(ICON_ELLIPSIS));
        iconUrl(ICON_PLUS));

        getLogManager().logInfo("RAMADDA started");


        getStorageManager().doFinalInitialization();
        if (getAdmin().getInstallationComplete()) {
            getRegistryManager().doFinalInitialization();
        }

        getAdmin().doFinalInitialization();

        if (loadedRdb) {
            getDatabaseManager().finishRdbLoad();
        }

        getHarvesterManager().initHarvesters();

        //Do this in a thread because (on macs) it hangs sometimes)
        Misc.run(this, "getFtpManager");
    }




    /**
     * _more_
     *
     * @param pluginPath _more_
     *
     *
     * @return _more_
     * @throws Exception _more_
     */
    public boolean installPlugin(String pluginPath) throws Exception {
        try {
            //Remove any ..._file_ prefix
            String tail          = RepositoryUtil.getFileTail(pluginPath);
            String newPluginFile =
                IOUtil.joinDir(getStorageManager().getPluginsDir(), tail);
            InputStream      inputStream = IOUtil.getInputStream(pluginPath);
            FileOutputStream fos         = new FileOutputStream(newPluginFile);
            IOUtil.writeTo(inputStream, fos);
            IOUtil.close(inputStream);
            IOUtil.close(fos);
            boolean haveLoadedBefore =
                getPluginManager().reloadFile(newPluginFile);
            loadPlugins();

            return haveLoadedBefore;
        } catch (Exception exc) {
            getLogManager().logError("Error installing plugin:" + pluginPath,
                                     exc);
        }

        return false;
    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void loadPlugins() throws Exception {
        getPluginManager().loadPlugins();
        getPageHandler().clearTemplates();
        loadTypeHandlers();
        loadOutputHandlers();
        getMetadataManager().loadMetadataHandlers(getPluginManager());
        loadApi();
        getPageHandler().loadLanguagePacks();
        loadSql();
        loadAdminHandlers();
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void loadTypeHandlers() throws Exception {
        for (String file : getPluginManager().getTypeDefFiles()) {
            file = getStorageManager().localizePath(file);
            if (getPluginManager().haveSeen(file)) {
                continue;
            }
            Element entriesRoot = XmlUtil.getRoot(file, getClass());
            if (entriesRoot == null) {
                continue;
            }
            List children = XmlUtil.findChildren(entriesRoot,
                                TypeHandler.TAG_TYPE);
            for (int i = 0; i < children.size(); i++) {
                Element entryNode = (Element) children.get(i);
                String  classPath =
                    XmlUtil.getAttribute(
                        entryNode, TypeHandler.TAG_HANDLER,
                        "org.ramadda.repository.type.GenericTypeHandler");

                //System.err.println ("RAMADDA: loading type handler:" + classPath);
                try {
                    Class       handlerClass = Misc.findClass(classPath);


                    Constructor ctor = Misc.findConstructor(handlerClass,
                                           new Class[] { Repository.class,
                            Element.class });
                    TypeHandler typeHandler =
                        (TypeHandler) ctor.newInstance(new Object[] { this,
                            entryNode });
                    addTypeHandler(typeHandler.getType(), typeHandler);
                } catch (Exception exc) {
                    System.err.println("RAMADDA: Error loading type handler:"
                                       + classPath);
                    exc.printStackTrace();

                    throw exc;
                }
            }
        }

    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void loadOutputHandlers() throws Exception {
        for (String file : getPluginManager().getOutputDefFiles()) {
            file = getStorageManager().localizePath(file);
            if (getPluginManager().haveSeen(file)) {
                continue;
            }
            Element root = XmlUtil.getRoot(file, getClass());
            if (root == null) {
                continue;
            }
            List children = XmlUtil.findChildren(root, TAG_OUTPUTHANDLER);
            for (int i = 0; i < children.size(); i++) {
                Element node     = (Element) children.get(i);
                boolean required = XmlUtil.getAttribute(node, ARG_REQUIRED,
                                       true);
                try {
                    Class c = Misc.findClass(XmlUtil.getAttribute(node,
                                  ATTR_CLASS));

                    Constructor ctor = Misc.findConstructor(c,
                                           new Class[] { Repository.class,
                            Element.class });
                    OutputHandler outputHandler =
                        (OutputHandler) ctor.newInstance(new Object[] { this,
                            node });
                    addOutputHandler(outputHandler);

                } catch (Exception exc) {
                    if ( !required) {
                        getLogManager().logWarning(
                            "Couldn't load optional output handler:"
                            + XmlUtil.toString(node));
                        getLogManager().logWarning(exc.toString());
                    } else {
                        getLogManager().logError(
                            "Error loading output handler file:" + file, exc);

                        throw exc;
                    }
                }
            }
        }
    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void loadSql() throws Exception {
        for (String sqlFile : getPluginManager().getSqlFiles()) {
            if (getPluginManager().haveSeen(sqlFile)) {
                continue;
            }
            String sql =
                getStorageManager().readUncheckedSystemResource(sqlFile);
            sql = getDatabaseManager().convertSql(sql);
            getDatabaseManager().loadSql(sql, true, false);
        }
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void loadAdminHandlers() throws Exception {
        for (Class adminHandlerClass :
                getPluginManager().getAdminHandlerClasses()) {
            if (getPluginManager().haveSeen(adminHandlerClass)) {
                continue;
            }
            Constructor ctor = Misc.findConstructor(adminHandlerClass,
                                   new Class[] { Repository.class });
            if (ctor != null) {
                getAdmin().addAdminHandler(
                    ((AdminHandler) ctor.newInstance(
                        new Object[] { Repository.this })));
            } else {
                getAdmin().addAdminHandler(
                    (AdminHandler) adminHandlerClass.newInstance());
            }
        }
        //        getAdmin().addAdminHandler(new LdapAdminHandler());
    }





    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void loadApi() throws Exception {
        for (String file : getPluginManager().getApiDefFiles()) {
            file = getStorageManager().localizePath(file);
            if (getPluginManager().haveSeen(file)) {
                continue;
            }
            Element   apiRoot = XmlUtil.getRoot(file, getClass());
            Hashtable props   = new Hashtable();
            processApiNode(apiRoot, apiHandlers, props, "repository");
        }
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public Repository getRepository() {
        return this;
    }

    /**
     * _more_
     *
     * @param repositoryManager _more_
     */
    public void addRepositoryManager(RepositoryManager repositoryManager) {
        repositoryManagers.add(repositoryManager);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    protected SessionManager doMakeSessionManager() {
        return new SessionManager(this);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    protected PageHandler doMakePageHandler() {
        return new PageHandler(this);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    protected WikiManager doMakeWikiManager() {
        return new WikiManager(this);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    protected EntryManager doMakeEntryManager() {
        return new EntryManager(this);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    protected AssociationManager doMakeAssociationManager() {
        return new AssociationManager(this);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    protected HarvesterManager doMakeHarvesterManager() {
        return new HarvesterManager(this);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    protected ActionManager doMakeActionManager() {
        return new ActionManager(this);
    }





    /**
     * _more_
     *
     * @return _more_
     */
    protected StorageManager doMakeStorageManager() {
        return new StorageManager(this);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    protected PluginManager doMakePluginManager() {
        return new PluginManager(this);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    protected DatabaseManager doMakeDatabaseManager() {
        return new DatabaseManager(this);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    protected FtpManager doMakeFtpManager() {
        return new FtpManager(this);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    protected Admin doMakeAdmin() {
        return new Admin(this);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    protected UserManager doMakeUserManager() {
        return new UserManager(this);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public UserManager getUserManager() {
        if (userManager == null) {
            userManager = doMakeUserManager();
        }

        return userManager;
    }




    /**
     * _more_
     *
     * @return _more_
     */
    protected MonitorManager doMakeMonitorManager() {
        return new MonitorManager(this);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public MonitorManager getMonitorManager() {
        if (monitorManager == null) {
            monitorManager = doMakeMonitorManager();
        }

        return monitorManager;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public SessionManager getSessionManager() {
        if (sessionManager == null) {
            sessionManager = doMakeSessionManager();
            sessionManager.init();
        }

        return sessionManager;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public PageHandler getPageHandler() {

        if (pageHandler == null) {
            pageHandler = doMakePageHandler();
        }

        return pageHandler;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public WikiManager getWikiManager() {
        if (wikiManager == null) {
            wikiManager = doMakeWikiManager();
        }

        return wikiManager;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public LogManager getLogManager() {
        if (logManager == null) {
            logManager = doMakeLogManager();
            logManager.init();
        }

        return logManager;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    protected LogManager doMakeLogManager() {
        return new LogManager(this);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public EntryManager getEntryManager() {
        if (entryManager == null) {
            entryManager = doMakeEntryManager();
        }

        return entryManager;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public AssociationManager getAssociationManager() {
        if (associationManager == null) {
            associationManager = doMakeAssociationManager();
        }

        return associationManager;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public HarvesterManager getHarvesterManager() {
        if (harvesterManager == null) {
            harvesterManager = doMakeHarvesterManager();
        }

        return harvesterManager;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public ActionManager getActionManager() {
        if (actionManager == null) {
            actionManager = doMakeActionManager();
        }

        return actionManager;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    protected AccessManager doMakeAccessManager() {
        return new AccessManager(this);
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public AccessManager getAccessManager() {
        if (accessManager == null) {
            accessManager = doMakeAccessManager();
        }

        return accessManager;
    }




    /**
     * _more_
     *
     * @return _more_
     */
    protected SearchManager doMakeSearchManager() {
        return new SearchManager(this);
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public SearchManager getSearchManager() {
        if (searchManager == null) {
            searchManager = doMakeSearchManager();
        }

        return searchManager;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    protected MapManager doMakeMapManager() {
        return new MapManager(this);
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public MapManager getMapManager() {
        if (mapManager == null) {
            mapManager = doMakeMapManager();
        }

        return mapManager;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    protected MetadataManager doMakeMetadataManager() {
        return new MetadataManager(this);
    }





    /**
     * _more_
     *
     * @return _more_
     */
    public MetadataManager getMetadataManager() {
        if (metadataManager == null) {
            metadataManager = doMakeMetadataManager();
        }

        return metadataManager;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    protected RegistryManager doMakeRegistryManager() {
        return new RegistryManager(this);
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public RegistryManager getRegistryManager() {
        if (registryManager == null) {
            registryManager = doMakeRegistryManager();
        }

        return registryManager;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public StorageManager getStorageManager() {
        if (storageManager == null) {
            storageManager = doMakeStorageManager();
            storageManager.init();
        }

        return storageManager;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public PluginManager getPluginManager() {
        if (pluginManager == null) {
            pluginManager = doMakePluginManager();
        }

        return pluginManager;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public DatabaseManager getDatabaseManager() {
        if (databaseManager == null) {
            databaseManager = doMakeDatabaseManager();
        }

        return databaseManager;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public FtpManager getFtpManager() {
        if (ftpManager == null) {
            ftpManager = doMakeFtpManager();
        }

        return ftpManager;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Admin getAdmin() {
        if (admin == null) {
            admin = doMakeAdmin();
        }

        return admin;
    }


    /** _more_ */
    public static final double VERSION = 1.0;

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void updateToVersion1_0() throws Exception {}

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void checkVersion() throws Exception {
        double version = getDbProperty(PROP_VERSION, 0.0);
        if (version == VERSION) {
            return;
        }
        updateToVersion1_0();
        //        writeGlobal(PROP_VERSION,""+VERSION);
    }


    /**
     * _more_
     *
     * @param path _more_
     * @param c _more_
     *
     * @return _more_
     */
    public  List<String> getListing(String path, Class c) {
        List<String> listing = new ArrayList<String>();
        File         f       = new File(path);
        //        getLogManager().logInfoAndPrint("RAMADDA: getListing:" + path);
        if (f.exists()) {
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                listing.add(files[i].toString());
            }
        } else {
            //try it as a java resource
            String contents = IOUtil.readContents(path, c, (String) null);
            if (contents == null) {
                contents = IOUtil.readContents(path + "/files.txt", c,
                        (String) null);
                //                getLogManager().logInfoAndPrint("RAMADDA: resourceList (2):" + contents);
            } else {
                //                getLogManager().logInfoAndPrint("RAMADDA: resourceList (1):" + contents);
            }
            if (contents != null) {
                List<String> lines = StringUtil.split(contents, "\n", true,
                                         true);
                for (String file : lines) {
                    listing.add(IOUtil.joinDir(path, file));
                }
            }
        }

        return listing;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<ImportHandler> getImportHandlers() {
        return getPluginManager().getImportHandlers();
    }




    /**
     * _more_
     *
     * @param message _more_
     */
    protected void usage(String message) {
        throw new IllegalArgumentException(
            message
            + "\nusage: repository\n\t-admin <admin name> <admin password>\n\t-port <http port>\n\t-Dname=value (e.g., -Dramadda.db=derby to specify the derby database)");
    }




    /**
     * _more_
     *
     * @param propertyName _more_
     *
     * @return _more_
     */
    public List<String> getResourcePaths(String propertyName) {
        List<String> tmp = StringUtil.split(getProperty(propertyName, BLANK),
                                            ";", true, true);
        List<String> paths = new ArrayList<String>();
        for (String path : tmp) {
            path = getStorageManager().localizePath(path);
            paths.add(path);
        }

        return paths;
    }




    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void readGlobals() throws Exception {
        Statement statement =
            getDatabaseManager().select(Tables.GLOBALS.COLUMNS,
                                        Tables.GLOBALS.NAME, new Clause[] {});
        dbProperties = new Properties();
        ResultSet results = statement.getResultSet();
        while (results.next()) {
            String name  = results.getString(1);
            String value = results.getString(2);
            if (name.equals(PROP_PROPERTIES)) {
                dbProperties.load(new ByteArrayInputStream(value.getBytes()));
            }
            dbProperties.put(name, value);
        }
        getDatabaseManager().closeAndReleaseConnection(statement);
    }



    /**
     * _more_
     */
    public void clearAllCaches() {
        for (OutputHandler outputHandler : outputHandlers) {
            outputHandler.clearCache();
        }
        clearCache();
    }

    /**
     * _more_
     */
    public void clearCache() {
        getEntryManager().clearCache();
        getAccessManager().clearCache();
        categoryList = null;
    }




    /**
     * _more_
     *
     * @param requestUrl _more_
     */
    public void initRequestUrl(RequestUrl requestUrl) {
        try {
            synchronized (initializedUrls) {
                if ( !initializedUrls.contains(requestUrl)) {
                    initializedUrls.add(requestUrl);
                }
            }
            Request request = new Request(this, null,
                                          getUrlBase()
                                          + requestUrl.getPath());
            super.initRequestUrl(requestUrl);
            ApiMethod apiMethod = findApiMethod(request);
            if (apiMethod == null) {
                getLogManager().logError("Could not find api for: "
                                         + requestUrl.getPath());

                return;
            }
            if (isSSLEnabled(null) && apiMethod.getNeedsSsl()) {
                requestUrl.setNeedsSsl(true);
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * _more_
     */
    protected void reinitializeRequestUrls() {
        synchronized (initializedUrls) {
            for (RequestUrl requestUrl : initializedUrls) {
                initRequestUrl(requestUrl);
            }
        }
    }


    /**
     * _more_
     *
     * @param requestUrl _more_
     *
     * @return _more_
     */
    public String getUrlPath(RequestUrl requestUrl) {
        if (requestUrl.getNeedsSsl()) {
            return httpsUrl(getUrlBase() + requestUrl.getPath());
        }

        return getUrlBase() + requestUrl.getPath();
    }

    /**
     * _more_
     *
     * @param node _more_
     * @param props _more_
     * @param handlers _more_
     * @param defaultHandler _more_
     *
     * @throws Exception _more_
     */
    protected void addRequest(Element node, Hashtable props,
                              Hashtable handlers, String defaultHandler)
            throws Exception {

        String  request = XmlUtil.getAttribute(node, ApiMethod.ATTR_REQUEST);

        String  methodName = XmlUtil.getAttribute(node, ApiMethod.ATTR_METHOD);
        boolean needsSsl   = XmlUtil.getAttributeFromTree(node,
                               ApiMethod.ATTR_NEEDS_SSL, false);
        boolean checkAuthMethod = XmlUtil.getAttributeFromTree(node,
                                      ApiMethod.ATTR_CHECKAUTHMETHOD, false);

        String authMethod = XmlUtil.getAttributeFromTree(node,
                                ApiMethod.ATTR_AUTHMETHOD, "");

        boolean admin = XmlUtil.getAttributeFromTree(node,
                            ApiMethod.ATTR_ADMIN,
                            Misc.getProperty(props, ApiMethod.ATTR_ADMIN,
                                             true));


        boolean requiresAuthToken = XmlUtil.getAttributeFromTree(node,
                                        ApiMethod.ATTR_REQUIRESAUTHTOKEN,
                                        Misc.getProperty(props,
                                            ApiMethod.ATTR_REQUIRESAUTHTOKEN,
                                            false));


        String handlerName = XmlUtil.getAttributeFromTree(node,
                                 ApiMethod.ATTR_HANDLER,
                                 Misc.getProperty(props,
                                     ApiMethod.ATTR_HANDLER, defaultHandler));

        String handlerId = XmlUtil.getAttributeFromTree(node,
                               ApiMethod.ATTR_ID, handlerName);
        RequestHandler handler = (RequestHandler) handlers.get(handlerId);

        if (handler == null) {
            handler = this;
            if (handlerName.equals("usermanager")) {
                handler = getUserManager();
            } else if (handlerName.equals("monitormanager")) {
                handler = getMonitorManager();
            } else if (handlerName.equals("admin")) {
                handler = getAdmin();
            } else if (handlerName.equals("logmanager")) {
                handler = getLogManager();
            } else if (handlerName.equals("harvestermanager")) {
                handler = getHarvesterManager();
            } else if (handlerName.equals("actionmanager")) {
                handler = getActionManager();
            } else if (handlerName.equals("graphmanager")) {
                handler = getOutputHandler(GraphOutputHandler.OUTPUT_GRAPH);
            } else if (handlerName.equals("accessmanager")) {
                handler = getAccessManager();
            } else if (handlerName.equals("searchmanager")) {
                handler = getSearchManager();
            } else if (handlerName.equals("entrymanager")) {
                handler = getEntryManager();
            } else if (handlerName.equals("associationmanager")) {
                handler = getAssociationManager();
            } else if (handlerName.equals("metadatamanager")) {
                handler = getMetadataManager();
            } else if (handlerName.equals("registrymanager")) {
                handler = getRegistryManager();
            } else if (handlerName.equals("repository")) {
                handler = this;
            } else {
                Class       handlerClass = Misc.findClass(handlerName);
                Constructor ctor         = null;
                Object[]    params       = null;

                ctor = Misc.findConstructor(handlerClass,
                                            new Class[] { Repository.class,
                        Element.class, Hashtable.class });
                params = new Object[] { this, node, props };

                if (ctor == null) {
                    ctor = Misc.findConstructor(handlerClass,
                            new Class[] { Repository.class,
                                          Element.class });
                    params = new Object[] { this, node };
                }

                if (ctor == null) {
                    ctor = Misc.findConstructor(handlerClass,
                            new Class[] { Repository.class });
                    params = new Object[] { this };
                }

                if (ctor == null) {
                    throw new IllegalStateException("Could not find ctor:"
                            + handlerClass.getName());
                }
                handler = (RequestHandler) ctor.newInstance(params);
            }
            if (handler == null) {
                getLogManager().logInfo("Could not find handler for:"
                                        + handlerName + ":");

                return;
            }
            handlers.put(handlerId, handler);
        }


        String    url       = request;
        ApiMethod oldMethod = requestMap.get(url);
        if (oldMethod != null) {
            requestMap.remove(url);
        }


        Class[] paramTypes = new Class[] { Request.class };
        Method  method     = Misc.findMethod(handler.getClass(), methodName,
                                        paramTypes);
        if (method == null) {
            throw new IllegalArgumentException("Unknown request method:"
                    + methodName + " in class:"
                    + handler.getClass().getName());
        }


        ApiMethod apiMethod =
            new ApiMethod(this, handler, request,
                          XmlUtil.getAttribute(node, ApiMethod.ATTR_NAME,
                              request), method, admin, requiresAuthToken,
                                        needsSsl, authMethod,
                                        checkAuthMethod,
                                        XmlUtil.getAttribute(node,
                                            ApiMethod.ATTR_TOPLEVEL, false));
        List actions = StringUtil.split(XmlUtil.getAttribute(node,
                           ApiMethod.ATTR_ACTIONS, BLANK), ",", true, true);
        if ( !Permission.isValidActions(actions)) {
            throw new IllegalArgumentException("Bad actions:" + actions
                    + " for api method:" + apiMethod.getName());
        }
        apiMethod.setActions(actions);
        if (XmlUtil.getAttribute(node, ApiMethod.ATTR_ISHOME, false)) {
            homeApi = apiMethod;
        }
        requestMap.put(url, apiMethod);
        if (oldMethod != null) {
            int index = apiMethods.indexOf(oldMethod);
            apiMethods.remove(index);
            apiMethods.add(index, apiMethod);
            if (apiMethod.isWildcard()) {
                index = wildCardApiMethods.indexOf(oldMethod);
                wildCardApiMethods.remove(index);
                wildCardApiMethods.add(index, apiMethod);
            }
        } else {
            apiMethods.add(apiMethod);
            if (apiMethod.isWildcard()) {
                wildCardApiMethods.add(apiMethod);
            }
        }
        if (apiMethod.getIsTopLevel()
                && !topLevelMethods.contains(apiMethod)) {
            topLevelMethods.add(apiMethod);
        }
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public RequestHandler getApiHandler(String id) {
        return apiHandlers.get(id);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<ApiMethod> getApiMethods() {
        return apiMethods;
    }



    /**
     * _more_
     *
     * @param type _more_
     */
    public void addOutputType(OutputType type) {
        outputTypeMap.put(type.getId(), type);
    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public OutputType findOutputType(String id) {
        if ((id == null) || (id.length() == 0)) {
            return OutputHandler.OUTPUT_HTML;
        }

        return outputTypeMap.get(id);
    }



    /**
     * _more_
     *
     * @param apiRoot _more_
     * @param handlers _more_
     * @param props _more_
     * @param defaultHandler _more_
     *
     * @throws Exception _more_
     */
    private void processApiNode(Element apiRoot, Hashtable handlers,
                                Hashtable props, String defaultHandler)
            throws Exception {
        if (apiRoot == null) {
            return;
        }
        NodeList children = XmlUtil.getElements(apiRoot);
        for (int i = 0; i < children.getLength(); i++) {
            Element node = (Element) children.item(i);
            String  tag  = node.getTagName();
            if (tag.equals(ApiMethod.TAG_PROPERTY)) {
                props.put(XmlUtil.getAttribute(node, ApiMethod.ATTR_NAME),
                          XmlUtil.getAttribute(node, ApiMethod.ATTR_VALUE));
            } else if (tag.equals(ApiMethod.TAG_API)) {
                addRequest(node, props, handlers, defaultHandler);
            } else if (tag.equals(ApiMethod.TAG_GROUP)) {
                processApiNode(node, handlers, props,
                               XmlUtil.getAttribute(node,
                                   ApiMethod.ATTR_HANDLER, defaultHandler));
            } else {
                throw new IllegalArgumentException("Unknown api.xml tag:"
                        + tag);
            }
        }
    }



    /**
     * _more_
     *
     * @throws Exception _more_
     */
    protected void initDefaultOutputHandlers() throws Exception {

        OutputHandler outputHandler = new OutputHandler(getRepository(),
                                          "Entry Deleter") {

            public boolean canHandleOutput(OutputType output) {
                return output.equals(OUTPUT_DELETER)
                /*                    || output.equals(OUTPUT_TYPECHANGE)*/
                || output.equals(OUTPUT_METADATA_SHORT) || output.equals(
                    OUTPUT_PUBLISH) || output.equals(OUTPUT_METADATA_FULL);
            }
            public void getEntryLinks(Request request, State state,
                                      List<Link> links)
                    throws Exception {
                if ( !state.isDummyGroup()) {
                    return;
                }

                /*                if(request.getUser().getAdmin()) {
                    links.add(makeLink(request, state.getEntry(),
                                       OUTPUT_TYPECHANGE));

                                       }*/
                for (Entry entry : state.getAllEntries()) {
                    if (getAccessManager().canDoAction(request, entry,
                            Permission.ACTION_EDIT)) {
                        if (getEntryManager().isAnonymousUpload(entry)) {
                            links.add(makeLink(request, state.getEntry(),
                                    OUTPUT_PUBLISH));

                            break;
                        }
                    }
                }
                boolean metadataOk = true;
                for (Entry entry : state.getAllEntries()) {
                    if ( !getAccessManager().canDoAction(request, entry,
                            Permission.ACTION_EDIT)) {
                        metadataOk = false;

                        break;
                    }
                }
                if (metadataOk) {
                    links.add(makeLink(request, state.getEntry(),
                                       OUTPUT_METADATA_SHORT));

                    links.add(makeLink(request, state.getEntry(),
                                       OUTPUT_METADATA_FULL));
                }

                boolean deleteOk = true;
                for (Entry entry : state.getAllEntries()) {
                    if ( !getAccessManager().canDoAction(request, entry,
                            Permission.ACTION_DELETE)) {
                        deleteOk = false;

                        break;
                    }
                }
                if (deleteOk) {
                    links.add(makeLink(request, state.getEntry(),
                                       OUTPUT_DELETER));
                }
            }


            public Result outputGroup(Request request, OutputType outputType,
                                      Entry group, List<Entry> subGroups,
                                      List<Entry> entries)
                    throws Exception {

                OutputType output = request.getOutput();
                if (output.equals(OUTPUT_PUBLISH)) {
                    return getEntryManager().publishEntries(request, entries);
                }


                if (output.equals(OUTPUT_METADATA_SHORT)) {
                    return getEntryManager().addInitialMetadataToEntries(
                        request, entries, true);
                }

                /*                if (output.equals(OUTPUT_TYPECHANGE)) {
                    return getEntryManager().changeType(
                                                        request, subGroups, entries);
                                                        }*/
                if (output.equals(OUTPUT_METADATA_FULL)) {
                    return getEntryManager().addInitialMetadataToEntries(
                        request, entries, false);
                }
                entries.addAll(subGroups);
                request.remove(ARG_DELETE_CONFIRM);

                return getEntryManager().processEntryListDelete(request,
                        entries);
            }
        };
        outputHandler.addType(OUTPUT_DELETER);
        addOutputHandler(outputHandler);



        OutputHandler copyHandler = new OutputHandler(getRepository(),
                                        "Entry Copier") {
            public boolean canHandleOutput(OutputType output) {
                return output.equals(OUTPUT_COPY);
            }
            public void getEntryLinks(Request request, State state,
                                      List<Link> links)
                    throws Exception {
                if ((request.getUser() == null)
                        || request.getUser().getAnonymous()) {
                    return;
                }
                if ( !state.isDummyGroup()) {
                    return;
                }
                links.add(makeLink(request, state.getEntry(), OUTPUT_COPY));
            }

            public Result outputEntry(Request request, OutputType outputType,
                                      Entry entry)
                    throws Exception {
                if (request.getUser().getAnonymous()) {
                    return new Result("", "");
                }

                return new Result(request.url(URL_ENTRY_COPY, ARG_FROM,
                        entry.getId()));
            }

            public String toString() {
                return "Copy handler";
            }

            public Result outputGroup(Request request, OutputType outputType,
                                      Entry group, List<Entry> subGroups,
                                      List<Entry> entries)
                    throws Exception {
                if (request.getUser().getAnonymous()) {
                    return new Result("", "");
                }
                if ( !group.isDummy()) {
                    return outputEntry(request, outputType, group);
                }
                StringBuffer idBuffer = new StringBuffer();
                entries.addAll(subGroups);
                for (Entry entry : entries) {
                    idBuffer.append(",");
                    idBuffer.append(entry.getId());
                }
                request.put(ARG_FROM, idBuffer);

                return getEntryManager().processEntryCopy(request);

                //                return new Result(request.url(URL_ENTRY_COPY, ARG_FROM,
                //                        idBuffer.toString()));
            }
        };
        copyHandler.addType(OUTPUT_COPY);
        addOutputHandler(copyHandler);


        OutputHandler fileListingHandler = new OutputHandler(getRepository(),
                                               "Entry Copier") {
            public boolean canHandleOutput(OutputType output) {
                return output.equals(OUTPUT_FILELISTING);
            }
            public void getEntryLinks(Request request, State state,
                                      List<Link> links)
                    throws Exception {
                if (fileListingOK(request)) {
                    for (Entry entry : state.getAllEntries()) {
                        if (entry.getResource().isFile()) {
                            links.add(makeLink(request, state.getEntry(),
                                    OUTPUT_FILELISTING));

                            break;
                        }
                    }
                }
            }

            private boolean fileListingOK(Request request) {
                return request.getUser().getAdmin()
                       || ( !request.getUser().getAnonymous()
                            && getProperty(PROP_ENABLE_FILE_LISTING, false));
            }

            public Result outputEntry(Request request, OutputType outputType,
                                      Entry entry)
                    throws Exception {
                return outputFileListing(request, entry,
                                         (List<Entry>) Misc.newList(entry));
            }

            public Result outputGroup(Request request, OutputType outputType,
                                      Entry group, List<Entry> subGroups,
                                      List<Entry> entries)
                    throws Exception {
                return outputFileListing(request, group, entries);

            }
            public Result outputFileListing(Request request, Entry entry,
                                            List<Entry> entries)
                    throws Exception {

                if ( !fileListingOK(request)) {
                    throw new AccessException("File listing not enabled",
                            request);
                }
                StringBuffer sb     = new StringBuffer();
                boolean      didOne = false;
                for (Entry child : entries) {
                    Resource resource = child.getResource();
                    if (resource == null) {
                        continue;
                    }
                    if ( !resource.isFile()) {
                        continue;
                    }
                    sb.append(resource.getTheFile().toString());
                    sb.append(HtmlUtils.br());
                    didOne = true;
                }
                if ( !didOne) {
                    sb.append(showDialogNote("No files available"));
                }

                return makeLinksResult(request, msg("File Listing"), sb,
                                       new State(entry));
            }

            public String toString() {
                return "File listing handler";
            }

        };
        fileListingHandler.addType(OUTPUT_FILELISTING);
        addOutputHandler(fileListingHandler);

        getUserManager().initOutputHandlers();

    }





    /**
     * _more_
     *
     * @param outputHandler _more_
     */
    public void addOutputHandler(OutputHandler outputHandler) {
        outputHandlers.add(outputHandler);
    }

    /**
     * _more_
     *
     * @param entryMonitor _more_
     */
    public void addEntryChecker(EntryChecker entryMonitor) {
        entryMonitors.add(entryMonitor);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Counter getNumberOfCurrentRequests() {
        return numberOfCurrentRequests;
    }

    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleRequest(Request request) throws Exception {
        numberOfCurrentRequests.incr(request.getRequestPath());
        try {
            return handleRequestInner(request);
        } finally {
            numberOfCurrentRequests.decr(request.getRequestPath());
        }
    }




    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result handleRequestInner(Request request) throws Exception {

        long   t1 = System.currentTimeMillis();
        Result result;
        if (getProperty(PROP_ACCESS_NOBOTS, false)) {
            if (request.isSpider()) {
                return new Result("", new StringBuffer("no bots for now"));
            }
        }


        if (debug) {
            getLogManager().debug("user:" + request.getUser() + " -- "
                                  + request.toString());
        }

        //        logInfo("request:" + request);
        try {
            getSessionManager().checkSession(request);
            result = getResult(request);
        } catch (Throwable exc) {
            //In case the session checking didn't set the user
            if (request.getUser() == null) {
                request.setUser(getUserManager().getAnonymousUser());
            }

            //TODO: For non-html outputs come up with some error format
            Throwable    inner     = LogUtil.getInnerException(exc);
            boolean      badAccess = inner instanceof AccessException;
            StringBuffer sb        = new StringBuffer();
            if ( !badAccess) {
                sb.append(showDialogError(getPageHandler().translate(request,
                        "An error has occurred") + ":" + HtmlUtils.p()
                            + inner.getMessage()));
            } else {
                AccessException     ae         = (AccessException) inner;
                AuthorizationMethod authMethod =
                    AuthorizationMethod.AUTH_HTML;
                if (request.getApiMethod() != null) {
                    ApiMethod apiMethod = request.getApiMethod();
                    if (apiMethod.getCheckAuthMethod()) {
                        request.setCheckingAuthMethod(true);
                        Result authResult =
                            (Result) apiMethod.invoke(request);
                        authMethod = authResult.getAuthorizationMethod();
                    } else {
                        authMethod = AuthorizationMethod.getMethod(
                            apiMethod.getAuthMethod());
                    }
                }
                //              System.err.println ("auth:" + authMethod);
                if (authMethod.equals(AuthorizationMethod.AUTH_HTML)) {
                    sb.append(showDialogError(inner.getMessage()));
                    String redirect = RepositoryUtil.encodeBase64(
                                          request.getUrl().getBytes());
                    sb.append(getUserManager().makeLoginForm(request,
                            HtmlUtils.hidden(ARG_REDIRECT, redirect)));
                } else {
                    sb.append(inner.getMessage());
                    //If the authmethod is basic http auth then, if ssl is enabled, we 
                    //want to have the authentication go over ssl. Else we do it clear text
                    if ( !request.getSecure() && isSSLEnabled(null)) {
                        /*
                        If ssl then we are a little tricky here. We redirect the request to the generic ssl based SSLREDIRCT url
                        passing the actual request as an argument. The processSslRedirect method checks for authentication. If
                        not authenticated then it throws an access exception which triggers a auth request back to the client
                        If authenticated then it redirects the client back to the original non ssl request
                        */
                        String redirectUrl = RepositoryUtil.encodeBase64(
                                                 request.getUrl().getBytes());
                        String url = HtmlUtils.url(URL_SSLREDIRECT.toString(),
                                         ARG_REDIRECT, redirectUrl);
                        result = new Result(url);
                    } else {
                        result = new Result("Error", sb);
                        result.addHttpHeader(HtmlUtils.HTTP_WWW_AUTHENTICATE,
                                             "Basic realm=\"ramadda\"");
                        result.setResponseCode(Result.RESPONSE_UNAUTHORIZED);
                    }

                    return result;
                }
            }

            if ((request.getUser() != null) && request.getUser().getAdmin()) {
                sb.append(
                    HtmlUtils.pre(
                        HtmlUtils.entityEncode(LogUtil.getStackTrace(inner))));
            }

            result = new Result(msg("Error"), sb);
            if (badAccess) {
                result.setResponseCode(Result.RESPONSE_UNAUTHORIZED);
                //                result.addHttpHeader(HtmlUtils.HTTP_WWW_AUTHENTICATE,"Basic realm=\"repository\"");
            } else {
                result.setResponseCode(Result.RESPONSE_INTERNALERROR);
                String userAgent = request.getHeaderArg("User-Agent");
                if (userAgent == null) {
                    userAgent = "Unknown";
                }
                getLogManager().logError("Error handling request:" + request
                                         + " ip:" + request.getIp(), inner);
            }
        }

        boolean okToAddCookie = false;


        if ((result != null) && (result.getInputStream() == null)
                && result.isHtml() && result.getShouldDecorate()
                && result.getNeedToWrite()) {
            result.putProperty(PROP_NAVLINKS, getNavLinks(request));
            okToAddCookie = result.getResponseCode() == Result.RESPONSE_OK;
            getPageHandler().decorateResult(request, result);
        }

        if (result.getRedirectUrl() != null) {
            okToAddCookie = true;
        }

        long t2 = System.currentTimeMillis();
        if ((result != null) && (t2 != t1)
                && (true || request.get("debug", false))) {
            if ((t2 - t1) > 100) {
                //                System.err.println("Time:" + request.getRequestPath() + " "
                //                                   + (t2 - t1));
            }
        }
        if (okToAddCookie
                && (request.getSessionIdWasSet()
                    || (request.getSessionId() == null)) && (result
                       != null)) {
            if (request.getSessionId() == null) {
                request.setSessionId(getSessionManager().createSessionId());
            }
            String sessionId = request.getSessionId();
            if (cookieExpirationDate == null) {
                //expire the cookie in 4 years year
                //Assume this ramadda doesn't run continuously for more than 4 years
                Date future = new Date(new Date().getTime()
                                       + DateUtil.daysToMillis(365 * 4));
                SimpleDateFormat sdf =
                    new SimpleDateFormat("EEE, dd-MMM-yyyy");
                cookieExpirationDate = sdf.format(future);
            }
            result.addCookie(SessionManager.COOKIE_NAME,
                             sessionId + "; path=" + getUrlBase()
                             + "; expires=" + cookieExpirationDate
                             + " 23:59:59 GMT");
        }

        if (request.get("gc", false) && (request.getUser() != null)
                && request.getUser().getAdmin()) {
            clearAllCaches();
            Misc.gc();
        }

        return result;
    }

    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected ApiMethod findApiMethod(Request request) throws Exception {
        String incoming = request.getRequestPath().trim();
        if (incoming.endsWith("/")) {
            incoming = incoming.substring(0, incoming.length() - 1);
        }
        String urlBase = getUrlBase();
        if (incoming.equals("/") || incoming.equals("")) {
            incoming = urlBase;
        }
        while (incoming.startsWith("//")) {
            incoming = incoming.substring(1);
        }
        if ( !incoming.startsWith(urlBase)) {
            return null;
        }
        incoming = incoming.substring(urlBase.length());
        if (incoming.length() == 0) {
            return homeApi;
        }



        ApiMethod apiMethod = (ApiMethod) requestMap.get(incoming);
        if (apiMethod == null) {
            for (ApiMethod tmp : wildCardApiMethods) {
                String path = tmp.getRequest();
                path = path.substring(0, path.length() - 2);
                if (incoming.startsWith(path)) {
                    apiMethod = tmp;

                    break;
                }
            }
        }
        if ((apiMethod == null) && incoming.equals(urlBase)) {
            apiMethod = homeApi;
        }

        return apiMethod;
    }


    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     */
    public ApiMethod getApiMethod(String path) {
        return requestMap.get(path);
    }



    /**
     * _more_
     *
     * @param msg _more_
     */
    public void checkMemory(String msg) {
        //        Misc.gc();
        Runtime.getRuntime().gc();
        double freeMemory    = (double) Runtime.getRuntime().freeMemory();
        double highWaterMark = (double) Runtime.getRuntime().totalMemory();
        double usedMemory    = (highWaterMark - freeMemory);
        usedMemory = usedMemory / 1000000.0;

        //        System.out.println("http://ramadda.org" +request);
        System.err.println(msg + ((int) usedMemory));

    }

    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected Result getResult(Request request) throws Exception {

        ApiMethod apiMethod = findApiMethod(request);

        if (apiMethod == null) {
            return getHtdocsFile(request);
        }
        //        checkMemory("memory:");
        //        System.err.println("request:"  + request);
        //        System.err.println("sslEnabled:" +sslEnabled + "  " + apiMethod.getNeedsSsl());
        Result sslRedirect = checkForSslRedirect(request, apiMethod);
        if (sslRedirect != null) {
            return sslRedirect;
        }
        //        System.out.println(absoluteUrl(request.getUrl()));

        request.setApiMethod(apiMethod);
        apiMethod.incrNumberOfCalls();

        String userAgent = request.getHeaderArg("User-Agent");
        if (userAgent == null) {
            userAgent = "Unknown";
        }
        //        System.err.println(request + " user-agent:" + userAgent +" ip:" + request.getIp());

        if ( !getAdmin().getInstallationComplete()) {
            return getAdmin().doInitialization(request);
        }

        if ( !getUserManager().isRequestOk(request)) {
            System.err.println("Access error:  user=" + request.getUser()
                               + " request=" + request);
            System.err.println("Admin Info  admin only= "
                               + getProperty(PROP_ACCESS_ADMINONLY, false)
                               + " user is admin="
                               + request.getUser().getAdmin()
                               + " require login="
                               + getProperty(PROP_ACCESS_REQUIRELOGIN,
                                             false));

            throw new AccessException(
                msg("You do not have permission to access this page"),
                request);
        }

        if ( !apiMethod.isRequestOk(request, this)) {
            System.err.println("Access error 2:  user=" + request.getUser()
                               + " request=" + request);
            System.err.println("Admin Info  admin only= "
                               + getProperty(PROP_ACCESS_ADMINONLY, false)
                               + " user is admin="
                               + request.getUser().getAdmin()
                               + " require login="
                               + getProperty(PROP_ACCESS_REQUIRELOGIN,
                                             false));
            apiMethod.printDebug(request);

            throw new AccessException(msg("Incorrect access"), request);
        }


        Result result = null;

        //TODO: how to handle when the DB is shutdown
        boolean hasConnection = true;
        //        hasConnection = getDatabaseManager().hasConnection();
        if ( !hasConnection) {
            //                && !incoming.startsWith(getUrlBase() + "/admin")) {
            result = new Result("No Database",
                                new StringBuffer("Database is shutdown"));
        } else {
            result = (Result) apiMethod.invoke(request);
        }
        if (result == null) {
            return null;
        }


        getLogManager().logRequest(request);


        return result;

    }



    /**
     *  Convert the sessionId into a authorization token that is used to verify form
     *  submissions, etc.
     *
     * @param sessionId _more_
     *
     * @return _more_
     */
    public String getAuthToken(String sessionId) {
        return RepositoryUtil.hashPassword(sessionId);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param sb _more_
     */
    public void addAuthToken(Request request, StringBuffer sb) {
        String sessionId = request.getSessionId();
        if (sessionId != null) {
            String authToken = getAuthToken(sessionId);
            sb.append(HtmlUtils.hidden(ARG_AUTHTOKEN, authToken));
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     */
    public void addAuthToken(Request request) {
        String sessionId = request.getSessionId();
        if (sessionId != null) {
            String authToken = getAuthToken(sessionId);
            request.put(ARG_AUTHTOKEN, authToken);
        }
    }



    /**
     * _more_
     *
     * @param request The request
     * @param apiMethod _more_
     *
     * @return _more_
     */
    private Result checkForSslRedirect(Request request, ApiMethod apiMethod) {
        boolean sslEnabled = isSSLEnabled(request);
        boolean allSsl     = false;
        if (sslEnabled) {
            allSsl = getProperty(PROP_ACCESS_ALLSSL, false);
            if (allSsl && !request.getSecure()) {
                return new Result(httpsUrl(request.getUrl()));
            }
        }

        if (sslEnabled) {
            if ( !request.get(ARG_NOREDIRECT, false)) {
                if (apiMethod.getNeedsSsl() && !request.getSecure()) {
                    return new Result(httpsUrl(request.getUrl()));
                } else if ( !allSsl && !apiMethod.getNeedsSsl()
                            && request.getSecure()) {
                    return new Result(
                        request.getAbsoluteUrl(request.getUrl()));
                }
            }
        }

        return null;
    }

    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected Result getHtdocsFile(Request request) throws Exception {

        String urlBase = getUrlBase();
        String path    = request.getRequestPath();
        if ( !path.startsWith(urlBase)) {
            path = urlBase + path;
        }

        //        System.err.println("path:" + path);
        /*
       if ((path.indexOf("/gantt") >= 0) && path.endsWith(".jar")) {
            path = "/repository/applets/gantt/gantt.jar";
        }
        */

        //Some hackery so we can reload applets when developing
        if ((path.indexOf("/graph") >= 0) && path.endsWith(".jar")) {
            path = "/repository/applets/graph.jar";
        }

        if ((path.indexOf("/chat") >= 0) && path.endsWith(".jar")) {
            path = "/repository/collab/chat.jar";
            //            System.err.println ("new path:" + path);
        }




        path = path.replaceAll("//", "/");
        //        System.err.println("path:" + path);
        if ( !path.startsWith(urlBase)) {
            getLogManager().log(request,
                                "Unknown request" + " \"" + path + "\"");
            Result result = new Result(
                                msg("Error"),
                                new StringBuffer(
                                    showDialogError(
                                        msgLabel("Unknown request") + "\""
                                        + path + "\"")));
            result.setResponseCode(Result.RESPONSE_NOTFOUND);

            return result;
        }


        int length = urlBase.length();
        //        path = StringUtil.replace(path, urlBase, BLANK);
        path = path.substring(length);
        String type = getMimeTypeFromSuffix(IOUtil.getFileExtension(path));


        //Go through all of the htdoc roots
        for (String root : htdocRoots) {
            root = getStorageManager().localizePath(root);
            String fullPath = root + path;
            try {
                InputStream inputStream =
                    getStorageManager().getInputStream(fullPath);
                if (path.endsWith(".js") || path.endsWith(".css")) {
                    String js = IOUtil.readInputStream(inputStream);
                    js          = js.replace("${urlroot}", urlBase);
                    inputStream = new ByteArrayInputStream(js.getBytes());
                } else if (path.endsWith(".html")) {
                    String html = IOUtil.readInputStream(inputStream);

                    return getEntryManager().addHeaderToAncillaryPage(
                        request, new Result(BLANK, new StringBuffer(html)));
                }
                Result result = new Result(BLANK, inputStream, type);
                result.setCacheOk(true);

                return result;
            } catch (IOException fnfe) {
                //noop
            }
        }

        String pluginPath = getPluginManager().getHtdocsMap().get(path);
        if (pluginPath != null) {
            InputStream inputStream =
                getStorageManager().getInputStream(pluginPath);
            if (pluginPath.endsWith(".js") || pluginPath.endsWith(".css")) {
                String js = IOUtil.readInputStream(inputStream);
                js          = js.replace("${urlroot}", getUrlBase());
                inputStream = new ByteArrayInputStream(js.getBytes());
            } else if (path.endsWith(".html")) {
                String html = IOUtil.readInputStream(inputStream);

                return getEntryManager().addHeaderToAncillaryPage(request,
                        new Result(BLANK, new StringBuffer(html)));
            }
            String mimeType =
                getMimeTypeFromSuffix(IOUtil.getFileExtension(path));

            Result result = new Result(BLANK, inputStream, mimeType);
            result.setCacheOk(true);

            return result;
        }



        if (path.startsWith("/alias/")) {
            String alias = path.substring("/alias/".length());
            if (alias.endsWith("/")) {
                alias = alias.substring(0, alias.length() - 1);
            }
            Entry entry = getEntryManager().getEntryFromAlias(request, alias);
            if (entry != null) {
                return new Result(request.url(URL_ENTRY_SHOW, ARG_ENTRYID,
                        entry.getId()));
            }
        }


        String userAgent = request.getHeaderArg(HtmlUtils.HTTP_USER_AGENT);
        if (userAgent == null) {
            userAgent = "Unknown";
        }

        getLogManager().log(request,
                            "Unknown request:" + request.getUrl()
                            + " user-agent:" + userAgent + " ip:"
                            + request.getIp());
        Result result = new Result(
                            msg("Error"),
                            new StringBuffer(
                                showDialogError(
                                    msgLabel("Unknown request") + path)));
        result.setResponseCode(Result.RESPONSE_NOTFOUND);

        return result;


    }







    /** _more_ */
    private Boolean cacheResources = null;

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean cacheResources() {
        //DEBUG
        //        if(true) return false;
        if (cacheResources == null) {
            String test = (String) cmdLineProperties.get(PROP_CACHERESOURCES);

            if (test == null) {
                test = (String) properties.get(PROP_CACHERESOURCES);
            }

            if (test == null) {
                test = "true";
            }
            cacheResources = new Boolean(test.equals("true"));
        }

        return cacheResources.booleanValue();
    }





    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        FileInputStream is     = new FileInputStream("testfr.html");
        final char[]    buffer = new char[0x10000];
        StringBuilder   out    = new StringBuilder();
        Reader          in     = new InputStreamReader(is, "UTF-8");
        int             read;
        do {
            read = in.read(buffer, 0, buffer.length);
            if (read > 0) {
                out.append(buffer, 0, read);
            }
        } while (read >= 0);

        FileOutputStream fos = new FileOutputStream("test.txt");
        PrintWriter      pw  = new PrintWriter(fos);
        pw.print(out.toString());
        pw.close();
        fos.close();
        //        String html = ":before:<include href=\"http://www.unavco.org/lib/uv-header-webapps.html\">:during:<include href=\"http://www.unavco.org/lib/uv-footer-webapps.html\">:after:";
        //        System.err.println(processTemplate(html));
    }



    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public String getProperty(String name) {
        return getPropertyValue(name, true);
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param checkDb _more_
     *
     * @return _more_
     */
    public String getPropertyValue(String name, boolean checkDb) {
        if (systemEnv == null) {
            systemEnv = System.getenv();
        }
        String prop = null;
        /*
        if ( !cacheResources()) {
            try {
            loadProperties(properties,
            "/org/ramadda/repository/resources/repository.properties");
            } catch (Exception exc) {}
            }*/

        String override = "override." + name;
        //Check if there is an override 
        if (prop == null) {
            prop = (String) cmdLineProperties.get(override);
        }

        if (prop == null) {
            prop = (String) properties.get(override);
        }

        //Then look at the command line
        if (prop == null) {
            prop = (String) cmdLineProperties.get(name);
        }


        //Then the  database properties  
        if (checkDb && (prop == null)) {
            prop = (String) dbProperties.get(name);
        }


        //then the  repository.properties 
        if (prop == null) {
            prop = (String) properties.get(name);
        }

        //Then look at system properties
        if (prop == null) {
            prop = System.getProperty(name);
        }

        //Then env vars
        if (prop == null) {
            prop = systemEnv.get(name);
        }

        return prop;
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getProperty(String name, String dflt) {
        return getPropertyValue(name, dflt, true);
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     * @param checkDb _more_
     *
     * @return _more_
     */
    public String getPropertyValue(String name, String dflt,
                                   boolean checkDb) {
        String prop = getPropertyValue(name, checkDb);
        if (prop != null) {
            return prop;
        }

        return dflt;
    }




    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean getProperty(String name, boolean dflt) {
        String prop = getProperty(name);
        if (prop != null) {
            return new Boolean(prop).booleanValue();
        }

        return dflt;
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public int getProperty(String name, int dflt) {
        String prop = getProperty(name);
        if (prop != null) {
            return new Integer(prop.trim()).intValue();
        }

        return dflt;
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public long getProperty(String name, long dflt) {
        String prop = getProperty(name);
        if (prop != null) {
            return new Long(prop).longValue();
        }

        return dflt;
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public double getProperty(String name, double dflt) {
        String prop = getProperty(name);
        if (prop != null) {
            return new Double(prop).doubleValue();
        }

        return dflt;
    }



    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean getDbProperty(String name, boolean dflt) {
        return Misc.getProperty(dbProperties, name, dflt);
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public double getDbProperty(String name, double dflt) {
        return Misc.getProperty(dbProperties, name, dflt);
    }






    /**
     * _more_
     *
     * @throws Exception _more_
     *
     */
    protected void initSchema() throws Exception {
        //Force a connection
        getDatabaseManager().init();
        String sql = getStorageManager().readUncheckedSystemResource(
                         getProperty(PROP_DB_SCRIPT));
        sql = getDatabaseManager().convertSql(sql);

        //        System.err.println("RAMADDA: loading schema");
        //        SqlUtil.showLoadingSql = true;
        getDatabaseManager().loadSql(sql, true, false);
        //        SqlUtil.showLoadingSql = false;
        //        System.err.println("RAMADDA: done loading schema");

        loadSql();
        getDatabaseManager().initComplete();
        readGlobals();
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     *
     * @throws Exception _more_
     */
    public void writeGlobal(String name, boolean value) throws Exception {
        writeGlobal(name, BLANK + value);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param propName _more_
     *
     * @throws Exception _more_
     */
    public void writeGlobal(Request request, String propName)
            throws Exception {
        writeGlobal(request, propName, false);
    }



    /**
     * _more_
     *
     * @param request The request
     * @param propName _more_
     * @param deleteIfNull _more_
     *
     * @throws Exception _more_
     */
    public void writeGlobal(Request request, String propName,
                            boolean deleteIfNull)
            throws Exception {
        String value = request.getString(propName, getProperty(propName, ""));
        if (deleteIfNull && (value.trim().length() == 0)) {
            getDatabaseManager().delete(Tables.GLOBALS.NAME,
                                        Clause.eq(Tables.GLOBALS.COL_NAME,
                                            propName));
            dbProperties.remove(propName);
        } else {
            writeGlobal(propName, value);
        }
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     *
     * @throws Exception _more_
     */
    public void writeGlobal(String name, String value) throws Exception {
        getDatabaseManager().delete(Tables.GLOBALS.NAME,
                                    Clause.eq(Tables.GLOBALS.COL_NAME, name));
        getDatabaseManager().executeInsert(Tables.GLOBALS.INSERT,
                                           new Object[] { name,
                value });

        if (name.equals(PROP_PROPERTIES)) {
            dbProperties.load(new ByteArrayInputStream(value.getBytes()));
            getPageHandler().clearTemplates();
        }
        dbProperties.put(name, value);
    }



    /**
     *  _more_
     *
     *  @param request The request
     * @param state _more_
     *
     *  @return _more_
     *
     *  @throws Exception _more_
     */
    public List<Link> getOutputLinks(Request request,
                                     OutputHandler.State state)
            throws Exception {
        boolean    isSpider = request.isSpider();
        List<Link> links    = new ArrayList<Link>();
        for (OutputHandler outputHandler : outputHandlers) {
            if (isSpider && !outputHandler.allowSpiders()) {
                continue;
            }
            outputHandler.getEntryLinks(request, state, links);
        }
        List<Link> okLinks = new ArrayList<Link>();


        for (Link link : links) {
            OutputType outputType = link.getOutputType();
            if (isOutputTypeOK(outputType)) {
                okLinks.add(link);
            } else {
                //                System.err.println ("NOT OK: " + outputType);
            }
        }

        return okLinks;
    }


    /**
     * _more_
     *
     * @param request The request
     * @param state _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Link> getLinksForHeader(Request request,
                                        OutputHandler.State state)
            throws Exception {
        List<Link> links   = getOutputLinks(request, state);

        List<Link> okLinks = new ArrayList<Link>();

        for (Link link : links) {
            if (link.isType(OutputType.TYPE_VIEW)) {
                okLinks.add(link);
            }
        }

        return okLinks;
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<OutputType> getOutputTypes() throws Exception {
        List<OutputType> allTypes = new ArrayList<OutputType>();
        for (OutputHandler outputHandler : outputHandlers) {
            allTypes.addAll(outputHandler.getTypes());
        }

        return allTypes;
    }


    /**
     * _more_
     *
     * @param outputType _more_
     *
     * @return _more_
     */
    public boolean isOutputTypeOK(OutputType outputType) {
        if ((outputType == null) || (outputType.getId() == null)) {
            return true;
        }
        String prop = getProperty(outputType.getId() + ".ok");
        if ((prop == null) || prop.equals("true")) {
            return true;
        }

        return false;
    }

    /**
     * _more_
     *
     * @param outputType _more_
     * @param ok _more_
     *
     * @throws Exception _more_
     */
    public void setOutputTypeOK(OutputType outputType, boolean ok)
            throws Exception {
        String prop = outputType.getId() + ".ok";
        writeGlobal(prop, "" + ok);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<OutputHandler> getOutputHandlers() {
        return new ArrayList<OutputHandler>(outputHandlers);
    }

    /**
     * _more_
     *
     * @return _more_
     *
     */
    public HtmlOutputHandler getHtmlOutputHandler() {
        try {
            return (HtmlOutputHandler) getOutputHandler(
                OutputHandler.OUTPUT_HTML);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    public CalendarOutputHandler getCalendarOutputHandler() {
        try {
            return (CalendarOutputHandler) getOutputHandler(
                OutputHandler.OUTPUT_HTML);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public ZipOutputHandler getZipOutputHandler() throws Exception {
        return (ZipOutputHandler) getOutputHandler(
            ZipOutputHandler.OUTPUT_ZIP);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public TypeHandler getGroupTypeHandler() {
        return groupTypeHandler;
    }



    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public XmlOutputHandler getXmlOutputHandler() throws Exception {
        return (XmlOutputHandler) getOutputHandler(
            XmlOutputHandler.OUTPUT_XML);
    }



    /**
     * _more_
     *
     * @param outputType _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public OutputHandler getOutputHandler(OutputType outputType)
            throws Exception {
        if ( !isOutputTypeOK(outputType)) {
            return null;
        }

        return getOutputHandler(outputType.getId());
    }


    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public OutputHandler getOutputHandler(Request request) throws Exception {
        OutputHandler handler = getOutputHandler(request.getOutput());
        if (handler != null) {
            return handler;
        }

        throw new IllegalArgumentException(
            msgLabel("Could not find output handler for")
            + request.getOutput());
    }


    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public OutputHandler getOutputHandler(String type) throws Exception {
        if ((type == null) || (type.length() == 0)) {
            type = OutputHandler.OUTPUT_HTML.getId();
        }
        OutputType output = new OutputType("", type, OutputType.TYPE_VIEW);

        for (OutputHandler outputHandler : outputHandlers) {
            if (outputHandler.canHandleOutput(output)) {
                return outputHandler;
            }
        }

        return null;
    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void initDefaultTypeHandlers() throws Exception {
        addTypeHandler(TypeHandler.TYPE_ANY,
                       new TypeHandler(this, TypeHandler.TYPE_ANY,
                                       "Any file type"));
        addTypeHandler(TypeHandler.TYPE_GROUP,
                       groupTypeHandler = new GroupTypeHandler(this));
        groupTypeHandler.putProperty("form.resource.show", "false");
        groupTypeHandler.putProperty("icon", ICON_FOLDER);
        TypeHandler typeHandler;
        addTypeHandler(TypeHandler.TYPE_FILE,
                       typeHandler = new TypeHandler(this, "file", "File"));
        typeHandler.putProperty("icon", ICON_FILE);
    }



    /**
     * _more_
     *
     * @param typeName _more_
     * @param typeHandler _more_
     */
    public void addTypeHandler(String typeName, TypeHandler typeHandler) {
        typeHandlersMap.put(typeName, typeHandler);
        allTypeHandlers.add(typeHandler);
    }


    /**
     * _more_
     *
     * @param typeHandler _more_
     */
    public void removeTypeHandler(TypeHandler typeHandler) {
        typeHandlersMap.remove(typeHandler.getType());
        allTypeHandlers.remove(typeHandler);
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<TypeHandler> getTypeHandlers() throws Exception {
        return new ArrayList<TypeHandler>(allTypeHandlers);
    }




    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public TypeHandler getTypeHandler(Request request) throws Exception {
        if (request != null) {
            String type = request.getString(ARG_TYPE,
                                            TypeHandler.TYPE_ANY).trim();

            return getTypeHandler(type, false, true);
        } else {
            return getTypeHandler(TypeHandler.TYPE_FILE, false, true);
        }
    }

    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public TypeHandler getTypeHandler(String type) throws Exception {
        return getTypeHandler(type, true, true);
    }

    /**
     * _more_
     *
     * @param type _more_
     * @param makeNewOneIfNeeded _more_
     * @param useDefaultIfNotFound _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public TypeHandler getTypeHandler(String type,
                                      boolean makeNewOneIfNeeded,
                                      boolean useDefaultIfNotFound)
            throws Exception {
        if (type.trim().length() == 0) {
            type = TypeHandler.TYPE_FILE;
        }
        TypeHandler typeHandler = (TypeHandler) typeHandlersMap.get(type);
        if (typeHandler == null) {
            if ( !useDefaultIfNotFound) {
                return null;
            }
            try {
                Class c = Misc.findClass("org.ramadda.repository." + type);
                Constructor ctor = Misc.findConstructor(c,
                                       new Class[] { Repository.class,
                        String.class });
                typeHandler = (TypeHandler) ctor.newInstance(new Object[] {
                    this,
                    type });
            } catch (Throwable cnfe) {}
        }

        if (typeHandler == null) {
            if ( !makeNewOneIfNeeded) {
                return getTypeHandler(TypeHandler.TYPE_ANY);
            }
            typeHandler = new TypeHandler(this, type);
            typeHandler.setForUser(false);
            addTypeHandler(type, typeHandler);
        }

        return typeHandler;
    }



    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processPing(Request request) throws Exception {
        if (request.getString(ARG_RESPONSE, "").equals(RESPONSE_XML)) {
            Document resultDoc  = XmlUtil.makeDocument();
            Element  resultRoot = XmlUtil.create(resultDoc, TAG_RESPONSE,
                                     null, new String[] { ATTR_CODE,
                    "ok" });
            String xml = XmlUtil.toString(resultRoot);

            return new Result(xml, MIME_XML);
        }
        StringBuffer sb = new StringBuffer("OK");

        return new Result("", sb);
    }





    /** _more_ */
    byte[] buffer = new byte[1048748];

    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processTestMemory(Request request) throws Exception {
        return new Result(
            BLANK, new BufferedInputStream(new ByteArrayInputStream(buffer)),
            "application/x-binary");
    }



    public Result processProxy(Request request) throws Exception {
        String url = request.getString(ARG_URL,"");
        getLogManager().logInfo("RAMADDA: processing proxy request:" + url);
        if(!url.startsWith("http:") && !url.startsWith("https:")) {
            throw new IllegalArgumentException("Bad URL:" + url);
        }
        //        System.err.println("url:" + url);

        //Check the whitelist
        boolean ok = false;
        for(String pattern: StringUtil.split(getProperty(PROP_PROXY_WHITELIST,""),",",true,true)) {
            //            System.err.println("pattern:" + pattern);
            if(url.matches(pattern)) {
                ok = true;
                break;
            }
        }
        if(!ok) {
            throw new IllegalArgumentException("URL not in whitelist:" + url);
        }

        URLConnection connection = new URL(url).openConnection();
        InputStream   is         = connection.getInputStream();
        return request.returnStream(is);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getRepositoryDescription() {
        return getProperty(PROP_REPOSITORY_DESCRIPTION, "");
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getRepositoryName() {
        return getProperty(PROP_REPOSITORY_NAME, "");
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getRepositoryEmail() {
        return getProperty(PROP_ADMIN_EMAIL, "");
    }

    /**
     * _more_
     *
     * @param result _more_
     *
     * @return _more_
     */
    public String getLogoImage(Result result) {
        String logoImage = null;
        if (result != null) {
            logoImage = (String) result.getProperty(PROP_LOGO_IMAGE);
        }
        if (logoImage == null) {
            logoImage = getProperty(PROP_LOGO_IMAGE, "").trim();
        }
        if (logoImage.length() == 0) {
            logoImage = "${root}/images/logo.png";
        }

        return logoImage;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public ServerInfo getServerInfo() {
        int sslPort = getHttpsPort();

        return new ServerInfo(getHostname(), getPort(), sslPort,
                              getUrlBase(), getRepositoryName(),
                              getRepositoryDescription(),
                              getRepositoryEmail(),
                              getRegistryManager().isEnabledAsServer(),
                              false);
    }



    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processInfo(Request request) throws Exception {
        if (request.getString(ARG_RESPONSE, "").equals(RESPONSE_XML)) {
            Document doc  = XmlUtil.makeDocument();
            Element  info = getServerInfo().toXml(this, doc);
            info.setAttribute(ATTR_CODE, CODE_OK);
            String xml = XmlUtil.toString(info);

            //            System.err.println("returning xml:" + xml);
            return new Result(xml, MIME_XML);
        }
        StringBuffer sb = new StringBuffer("");

        return new Result("", sb);
    }


    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processDocs(Request request) throws Exception {
        StringBuffer   sb      = new StringBuffer();
        List<String[]> docUrls = getPluginManager().getDocUrls();
        sb.append(msgHeader("Available documentation"));
        if (docUrls.size() == 0) {
            sb.append(showDialogNote(msg("No documentation available")));
        }
        sb.append("<ul>");
        for (String[] url : docUrls) {
            sb.append("<li>");
            String fullUrl = getUrlBase() + url[0];
            sb.append(HtmlUtils.href(fullUrl, url[1]));
            sb.append("<br>&nbsp;");
        }
        sb.append("</ul>");

        return new Result("Documentation", sb);
    }




    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processMessage(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        request.appendMessage(sb);

        return new Result(BLANK, sb);
    }

    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processDummy(Request request) throws Exception {
        return new Result(BLANK, new StringBuffer(BLANK));
    }


    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processSslRedirect(Request request) throws Exception {
        if (request.getCheckingAuthMethod()) {
            return new Result(AuthorizationMethod.AUTH_HTTP);
        }

        if (request.isAnonymous()) {
            throw new AccessException("Cannot access data", request);
        }
        String url = request.getString(ARG_REDIRECT, "");
        url = new String(RepositoryUtil.decodeBase64(url));

        return new Result(url);
    }



    /**
     * _more_
     *
     *
     * @param request The request
     * @param what _more_
     * @param includeExtra _more_
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List getListLinks(Request request, String what,
                             boolean includeExtra)
            throws Exception {
        List                 links       = new ArrayList();
        TypeHandler          typeHandler = getTypeHandler(request);
        List<TwoFacedObject> typeList    = typeHandler.getListTypes(false);
        String               extra1      = " class=subnavnolink ";
        String               extra2      = " class=subnavlink ";
        if ( !includeExtra) {
            extra1 = BLANK;
            extra2 = BLANK;
        }
        if (typeList.size() > 0) {
            for (TwoFacedObject tfo : typeList) {
                if (what.equals(tfo.getId())) {
                    links.add(HtmlUtils.span(tfo.toString(), extra1));
                } else {
                    links.add(HtmlUtils.href(request.url(URL_LIST_SHOW,
                            ARG_WHAT, (String) tfo.getId(), ARG_TYPE,
                            (String) typeHandler.getType()), tfo.toString(),
                                extra2));
                }
            }
        }
        String typeAttr = BLANK;
        if ( !typeHandler.getType().equals(TypeHandler.TYPE_ANY)) {
            typeAttr = "&type=" + typeHandler.getType();
        }


        String[] whats = { WHAT_TYPE, WHAT_TAG, WHAT_ASSOCIATION };
        String[] names = { "Types", "Tags", "Associations" };
        for (int i = 0; i < whats.length; i++) {
            if (what.equals(whats[i])) {
                links.add(HtmlUtils.span(names[i], extra1));
            } else {
                links.add(HtmlUtils.href(request.url(URL_LIST_SHOW, ARG_WHAT,
                        whats[i]) + typeAttr, names[i], extra2));
            }
        }

        return links;
    }










    /**
     * _more_
     *
     * @param request The request
     * @param urls _more_
     * @param arg _more_
     *
     * @return _more_
     */
    public String makeHeader(Request request, List<RequestUrl> urls,
                             String arg) {
        List<String> links = new ArrayList();
        String       type  = request.getRequestPath();
        for (RequestUrl requestUrl : urls) {
            String label = requestUrl.getLabel();
            label = msg(label);
            if (label == null) {
                label = requestUrl.toString();
            }
            String url = request.url(requestUrl) + arg;
            if (type.endsWith(requestUrl.getPath())) {
                links.add(HtmlUtils.span(label,
                                        HtmlUtils.cssClass("subheader-on")));
            } else {
                links.add(HtmlUtils.span(HtmlUtils.href(url, label),
                                        HtmlUtils.cssClass("subheader-off")));
            }
        }
        String header =
            StringUtil.join("<span class=\"subheader-sep\">|</span>", links);

        return HtmlUtils.tag(HtmlUtils.TAG_CENTER,
                            HtmlUtils.cssClass("subheader-container"),
                            HtmlUtils.tag(HtmlUtils.TAG_SPAN,
                                         HtmlUtils.cssClass("subheader"),
                                         header));
    }








    /**
     * _more_
     *
     *
     * @param request The request
     * @return _more_
     */
    public List getNavLinks(Request request) {
        List    links   = new ArrayList();
        boolean isAdmin = false;
        if (request != null) {
            User user = request.getUser();
            isAdmin = user.getAdmin();
        }

        String template = getPageHandler().getTemplateProperty(request,
                              "ramadda.template.link.wrapper", "");

        for (ApiMethod apiMethod : topLevelMethods) {
            if (apiMethod.getMustBeAdmin() && !isAdmin) {
                continue;
            }
            if ( !apiMethod.getIsTopLevel()) {
                continue;
            }
            String url;
            if (apiMethod == homeApi) {
                url = fileUrl(apiMethod.getRequest());
            } else {
                url = request.url(apiMethod.getUrl());
            }
            String html = template.replace("${url}", url);
            html = html.replace("${label}", msg(apiMethod.getName()));
            html = html.replace("${topgroup}",
                                getEntryManager().getTopGroup().getName());
            links.add(html);
        }

        return links;
    }





    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     */
    public int getMax(Request request) {
        if (request.defined(ARG_SKIP)) {
            return request.get(ARG_SKIP, 0)
                   + request.get(ARG_MAX, DB_MAX_ROWS);
        }

        return request.get(ARG_MAX, DB_MAX_ROWS);
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Request getTmpRequest() throws Exception {
        Request request = new Request(getRepository(), "", new Hashtable());
        request.setUser(getUserManager().getAnonymousUser());

        return request;
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Request getTmpRequest(Entry entry) throws Exception {
        Request request = getTmpRequest();
        request.setPageStyle(doMakePageStyle(request, entry));

        return request;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public PageStyle doMakePageStyle(Request request, Entry entry) {
        try {
            PageStyle pageStyle = new PageStyle();
            if (request.exists(PROP_NOSTYLE)
                    || getProperty(PROP_NOSTYLE, false)) {
                return pageStyle;
            }
            List<Metadata> metadataList =
                getMetadataManager().findMetadata(entry,
                    ContentMetadataHandler.TYPE_PAGESTYLE, true);
            if ((metadataList == null) || (metadataList.size() == 0)) {
                return pageStyle;
            }

            //menus -1, showbreadcrumbs-2, toolbar-3, entry header-4, layout toolbar-5, type-6,  apply to this-7, wiki-8
            Metadata theMetadata = null;
            for (Metadata metadata : metadataList) {
                String types = metadata.getAttr(6);
                if ((types == null) || (types.trim().length() == 0)) {
                    theMetadata = metadata;

                    break;
                }
                for (String type : StringUtil.split(types, ",", true, true)) {
                    if (type.equals("file") && !entry.isGroup()) {
                        theMetadata = metadata;

                        break;
                    }
                    if (type.equals("folder") && entry.isGroup()) {
                        theMetadata = metadata;

                        break;
                    }
                    if (entry.getTypeHandler().isType(type)) {
                        theMetadata = metadata;

                        break;
                    }
                }
            }

            if (theMetadata == null) {
                return pageStyle;
            }

            pageStyle.setShowBreadcrumbs(Misc.equals(theMetadata.getAttr2(),
                    "true"));
            pageStyle.setShowToolbar(Misc.equals(theMetadata.getAttr3(),
                    "true"));
            pageStyle.setShowEntryHeader(Misc.equals(theMetadata.getAttr4(),
                    "true"));
            pageStyle.setShowLayoutToolbar(
                Misc.equals(theMetadata.getAttr(5), "true"));

            boolean canEdit = getAccessManager().canDoAction(request, entry,
                                  Permission.ACTION_EDIT);
            if ( !canEdit) {
                String menus = theMetadata.getAttr1();
                if ((menus != null) && (menus.trim().length() > 0)) {
                    if (menus.equals("none")) {
                        pageStyle.setShowMenubar(false);
                    } else {
                        for (String menu :
                                StringUtil.split(menus, ",", true, true)) {
                            pageStyle.setMenu(menu);
                        }
                    }
                }
            }
            if ((theMetadata.getAttr(8) != null)
                    && (theMetadata.getAttr(8).trim().length() > 0)) {
                pageStyle.setWikiTemplate(theMetadata.getAttr(8));
            }

            return pageStyle;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


    /**
     * _more_
     *
     * @param user _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Request getRequest(User user) throws Exception {
        Request request = new Request(getRepository(), "", new Hashtable());
        request.setUser(user);

        return request;
    }



    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    public static String msg(String msg) {
        return PageHandler.msg(msg);
    }

    /**
     * _more_
     *
     * @param msg _more_
     *
     * @return _more_
     */
    public static String msgLabel(String msg) {
        return PageHandler.msgLabel(msg);
    }

    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public static String msgHeader(String h) {
        return PageHandler.msgHeader(h);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param s _more_
     *
     * @return _more_
     */
    public String translate(Request request, String s) {
        return getPageHandler().translate(request, s);
    }



    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public static String header(String h) {
        return HtmlUtils.div(h, HtmlUtils.cssClass(CSS_CLASS_HEADING_1));
    }


    /**
     * _more_
     *
     * @param sb _more_
     * @param date _more_
     * @param url _more_
     * @param dayLinks _more_
     */
    public void createMonthNav(StringBuffer sb, Date date, String url,
                               Hashtable dayLinks) {

        GregorianCalendar cal =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        cal.setTime(date);
        int[] theDate  = CalendarOutputHandler.getDayMonthYear(cal);
        int   theDay   = cal.get(cal.DAY_OF_MONTH);
        int   theMonth = cal.get(cal.MONTH);
        int   theYear  = cal.get(cal.YEAR);
        while (cal.get(cal.DAY_OF_MONTH) > 1) {
            cal.add(cal.DAY_OF_MONTH, -1);
        }
        GregorianCalendar prev =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        prev.setTime(date);
        prev.add(cal.MONTH, -1);
        GregorianCalendar next =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        next.setTime(date);
        next.add(cal.MONTH, 1);

        sb.append(HtmlUtils.open(HtmlUtils.TAG_TABLE,
                                HtmlUtils.attrs(HtmlUtils.ATTR_BORDER, "1",
                                    HtmlUtils.ATTR_CELLSPACING, "0",
                                    HtmlUtils.ATTR_CELLPADDING, "0")));
        String[] dayNames = {
            "Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"
        };
        String   prevUrl  =
            HtmlUtils.space(1)
            + HtmlUtils.href(url + "&"
                            + CalendarOutputHandler.getUrlArgs(prev), "&lt;");
        String nextUrl =
            HtmlUtils.href(url + "&" + CalendarOutputHandler.getUrlArgs(next),
                          HtmlUtils.ENTITY_GT) + HtmlUtils.space(1);
        sb.append(HtmlUtils.open(HtmlUtils.TAG_TR,
                                HtmlUtils.attr(HtmlUtils.ATTR_VALIGN,
                                    HtmlUtils.VALUE_TOP)));
        sb.append(HtmlUtils.open(HtmlUtils.TAG_TD,
                                HtmlUtils.attrs(HtmlUtils.ATTR_COLSPAN, "7",
                                    HtmlUtils.ATTR_ALIGN,
                                    HtmlUtils.VALUE_CENTER,
                                    HtmlUtils.ATTR_CLASS,
                                    "calnavmonthheader")));


        sb.append(HtmlUtils.open(HtmlUtils.TAG_TABLE,
                                HtmlUtils.cssClass("calnavtable")
                                + HtmlUtils.attrs(HtmlUtils.ATTR_CELLSPACING,
                                    "0", HtmlUtils.ATTR_CELLPADDING, "0",
                                    HtmlUtils.ATTR_WIDTH, "100%")));
        sb.append(HtmlUtils.open(HtmlUtils.TAG_TR));
        sb.append(HtmlUtils.col(prevUrl,
                               HtmlUtils.attrs(HtmlUtils.ATTR_WIDTH, "1",
                                   HtmlUtils.ATTR_CLASS,
                                   "calnavmonthheader")));
        sb.append(
            HtmlUtils.col(
                DateUtil.MONTH_NAMES[cal.get(cal.MONTH)] + HtmlUtils.space(1)
                + theYear, HtmlUtils.attr(
                    HtmlUtils.ATTR_CLASS, "calnavmonthheader")));



        sb.append(HtmlUtils.col(nextUrl,
                               HtmlUtils.attrs(HtmlUtils.ATTR_WIDTH, "1",
                                   HtmlUtils.ATTR_CLASS,
                                   "calnavmonthheader")));
        sb.append(HtmlUtils.close(HtmlUtils.TAG_TABLE));
        sb.append(HtmlUtils.close(HtmlUtils.TAG_TR));
        sb.append(HtmlUtils.open(HtmlUtils.TAG_TR));
        for (int colIdx = 0; colIdx < 7; colIdx++) {
            sb.append(HtmlUtils.col(dayNames[colIdx],
                                   HtmlUtils.attrs(HtmlUtils.ATTR_WIDTH, "14%",
                                       HtmlUtils.ATTR_CLASS,
                                       "calnavdayheader")));
        }
        sb.append(HtmlUtils.close(HtmlUtils.TAG_TR));
        int startDow = cal.get(cal.DAY_OF_WEEK);
        while (startDow > 1) {
            cal.add(cal.DAY_OF_MONTH, -1);
            startDow--;
        }
        for (int rowIdx = 0; rowIdx < 6; rowIdx++) {
            sb.append(HtmlUtils.open(HtmlUtils.TAG_TR,
                                    HtmlUtils.attrs(HtmlUtils.ATTR_VALIGN,
                                        HtmlUtils.VALUE_TOP)));
            for (int colIdx = 0; colIdx < 7; colIdx++) {
                int     thisDay    = cal.get(cal.DAY_OF_MONTH);
                int     thisMonth  = cal.get(cal.MONTH);
                int     thisYear   = cal.get(cal.YEAR);
                boolean currentDay = false;
                String  dayClass   = "calnavday";
                if (thisMonth != theMonth) {
                    dayClass = "calnavoffday";
                } else if ((theMonth == thisMonth) && (theYear == thisYear)
                           && (theDay == thisDay)) {
                    dayClass   = "calnavtheday";
                    currentDay = true;
                }
                String content;
                if (dayLinks != null) {
                    String key = thisYear + "/" + thisMonth + "/" + thisDay;
                    if (dayLinks.get(key) != null) {
                        content = HtmlUtils.href(url + "&"
                                + CalendarOutputHandler.getUrlArgs(cal), ""
                                    + thisDay);
                        if ( !currentDay) {
                            dayClass = "calnavoffday";
                        }
                    } else {
                        content  = "" + thisDay;
                        dayClass = "calnavday";
                    }
                } else {
                    content = HtmlUtils.href(
                        url + "&" + CalendarOutputHandler.getUrlArgs(cal),
                        "" + thisDay);
                }

                sb.append(HtmlUtils.col(content, HtmlUtils.cssClass(dayClass)));
                sb.append("\n");
                cal.add(cal.DAY_OF_MONTH, 1);
            }
            if (cal.get(cal.MONTH) > theMonth) {
                break;
            }
            if (cal.get(cal.YEAR) > theYear) {
                break;
            }
        }
        sb.append(HtmlUtils.close(HtmlUtils.TAG_TABLE));

    }



    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getResource(String id) throws Exception {
        return getResource(id, false);
    }


    /**
     * _more_
     *
     * @param id _more_
     * @param ignoreErrors _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getResource(String id, boolean ignoreErrors)
            throws Exception {
        String resource = (String) resources.get(id);
        if (resource != null) {
            return resource;
        }
        String fromProperties = getProperty(id);
        if (fromProperties != null) {
            List<String> paths = getResourcePaths(id);
            for (String path : paths) {
                try {
                    resource = getStorageManager().readSystemResource(path);
                } catch (Exception exc) {
                    //noop
                }
                if (resource != null) {
                    break;
                }
            }
        } else {
            try {
                resource = getStorageManager().readSystemResource(
                    getStorageManager().localizePath(id));
            } catch (Exception exc) {
                if ( !ignoreErrors) {
                    throw exc;
                }
            }
        }
        if (cacheResources() && (resource != null)) {
            //            resources.put(id, resource);
        }

        return resource;
    }





    /**
     * _more_
     *
     * @param request The request
     * @param includeAny _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String makeTypeSelect(Request request, boolean includeAny)
            throws Exception {
        return makeTypeSelect(request, includeAny, "", false, null);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param includeAny _more_
     * @param selected _more_
     * @param checkAddOk _more_
     * @param exclude _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String makeTypeSelect(Request request, boolean includeAny,
                                 String selected, boolean checkAddOk,
                                 HashSet<String> exclude)
            throws Exception {
        return makeTypeSelect(new ArrayList(), request, includeAny, selected,
                              checkAddOk, exclude);
    }

    /**
     * _more_
     *
     * @param items _more_
     * @param request _more_
     * @param includeAny _more_
     * @param selected _more_
     * @param checkAddOk _more_
     * @param exclude _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String makeTypeSelect(List items, Request request,
                                 boolean includeAny, String selected,
                                 boolean checkAddOk, HashSet<String> exclude)
            throws Exception {

        for (TypeHandler typeHandler : getTypeHandlers()) {
            if (typeHandler.isAnyHandler() && !includeAny) {
                continue;
            }
            if (exclude != null) {
                if (exclude.contains(typeHandler.getType())) {
                    continue;
                }
            }
            if ( !typeHandler.getForUser()) {
                continue;
            }

            if (checkAddOk && !typeHandler.canBeCreatedBy(request)) {
                continue;
            }
            //            System.err.println("type: " + typeHandler.getType()+" label:" + typeHandler.getLabel());
            items.add(new TwoFacedObject(typeHandler.getLabel(),
                                         typeHandler.getType()));
        }

        return HtmlUtils.select(ARG_TYPE, items, selected);
    }



    /**
     * _more_
     *
     * @param request The request
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<TypeHandler> getTypeHandlers(Request request)
            throws Exception {
        TypeHandler       typeHandler = getTypeHandler(request);
        List<TypeHandler> tmp         = new ArrayList<TypeHandler>();
        if ( !typeHandler.isAnyHandler()) {
            tmp.add(typeHandler);

            return tmp;
        }

        //For now don't do the db query to find the type handlers
        return getTypeHandlers();
        /*

        List<Clause> where = typeHandler.assembleWhereClause(request);
        Statement stmt =
            typeHandler.select(request,
                               SqlUtil.distinct(Tables.ENTRIES.COL_TYPE),
                               where, "");
        String[] types =
            SqlUtil.readString(getDatabaseManager().getIterator(stmt), 1);
        for (int i = 0; i < types.length; i++) {
            TypeHandler typeHandler = getTypeHandler(types[i]);

            if (types[i].equals(TypeHandler.TYPE_ANY)) {
                tmp.add(0, typeHandler);

            } else {
                tmp.add(typeHandler);
            }
        }
        return tmp;
        */
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List getDefaultCategorys() throws Exception {
        if (categoryList != null) {
            return categoryList;
        }
        Statement stmt = getDatabaseManager().select(
                             SqlUtil.distinct(Tables.ENTRIES.COL_DATATYPE),
                             Tables.ENTRIES.NAME, new Clause[] {});
        String[] types =
            SqlUtil.readString(getDatabaseManager().getIterator(stmt), 1);
        List      tmp  = new ArrayList();
        Hashtable seen = new Hashtable();
        for (TypeHandler typeHandler : getTypeHandlers()) {
            if (typeHandler.hasDefaultCategory()
                    && (seen.get(typeHandler.getDefaultCategory()) == null)) {
                tmp.add(typeHandler.getDefaultCategory());
                seen.put(typeHandler.getDefaultCategory(), "");
            }
        }

        for (int i = 0; i < types.length; i++) {
            if ((types[i] != null) && (types[i].length() > 0)
                    && (seen.get(types[i]) == null)) {
                tmp.add(types[i]);
            }
        }

        tmp.add(0, "");

        return categoryList = tmp;
    }



    /**
     * _more_
     *
     * @param suffix _more_
     *
     * @return _more_
     */
    public String getMimeTypeFromSuffix(String suffix) {
        String type = (String) mimeTypes.get(suffix);
        if (type == null) {
            if (suffix.startsWith(".")) {
                suffix = suffix.substring(1);
            }
            type = (String) mimeTypes.get(suffix);
        }
        if (type == null) {
            type = "unknown";
        }

        return type;
    }





    /**
     * _more_
     */
    public void setLocalFilePaths() {
        localFilePaths = (List<File>) Misc.toList(
            IOUtil.toFiles(
                (List<String>) StringUtil.split(
                    getProperty(PROP_LOCALFILEPATHS, ""), "\n", true, true)));
        //Add the ramadda dir as well
        localFilePaths.add(0, getStorageManager().getRepositoryDir());
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<File> getLocalFilePaths() {
        return localFilePaths;
    }



    /**
     * _more_
     *
     * @param request The request
     * @param addOrderBy _more_
     *
     * @return _more_
     */
    public String getQueryOrderAndLimit(Request request, boolean addOrderBy) {
        return getQueryOrderAndLimit(request, addOrderBy, null);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param addOrderBy _more_
     * @param forEntry _more_
     *
     * @return _more_
     */
    public String getQueryOrderAndLimit(Request request, boolean addOrderBy,
                                        Entry forEntry) {

        List<Metadata> metadataList = null;

        Metadata       sortMetadata = null;
        if ((forEntry != null) && !request.exists(ARG_ORDERBY)) {
            try {
                metadataList = getMetadataManager().findMetadata(forEntry,
                        ContentMetadataHandler.TYPE_SORT, true);
                if ((metadataList != null) && (metadataList.size() > 0)) {
                    sortMetadata = metadataList.get(0);
                }
            } catch (Exception ignore) {}
        }

        String  order     = " DESC ";
        boolean haveOrder = request.exists(ARG_ASCENDING);
        String  by        = null;
        int     max       = DB_MAX_ROWS;

        if (forEntry != null) {
            max = forEntry.getTypeHandler().getDefaultQueryLimit(request,
                    forEntry);
        }

        max = request.get(ARG_MAX, max);
        if (sortMetadata != null) {
            haveOrder = true;
            if (Misc.equals(sortMetadata.getAttr2(), "true")) {
                order = " ASC ";
            } else {
                order = " DESC ";
            }
            by = sortMetadata.getAttr1();
            /*            String tmp = sortMetadata.getAttr3();
            if(tmp!=null && tmp.length()>0) {
                max = Integer.parseInt(tmp.trim());
                }*/
        } else {
            by = request.getString(ARG_ORDERBY, (String) null);
            if (request.get(ARG_ASCENDING, false)) {
                order = " ASC ";
            }
        }

        String limitString = BLANK;
        limitString =
            getDatabaseManager().getLimitString(request.get(ARG_SKIP, 0),
                max);


        String orderBy = BLANK;
        if (addOrderBy) {
            orderBy = " ORDER BY " + Tables.ENTRIES.COL_FROMDATE + order;
        }
        //!!CAREFUL HERE!! - sql injection with the ARG_ORDERBY
        //Don't just use the by.
        if (by != null) {
            if (by.equals("fromdate")) {
                orderBy = " ORDER BY " + Tables.ENTRIES.COL_FROMDATE + order;
            } else if (by.equals("todate")) {
                orderBy = " ORDER BY " + Tables.ENTRIES.COL_TODATE + order;
            } else if (by.equals("createdate")) {
                orderBy = " ORDER BY " + Tables.ENTRIES.COL_CREATEDATE
                          + order;
            } else if (by.equals("name")) {
                if ( !haveOrder) {
                    order = " ASC ";
                }
                orderBy = " ORDER BY " + Tables.ENTRIES.COL_NAME + order;
            }
        }

        return orderBy + limitString;
    }




    /**
     * _more_
     *
     * @param fieldValue _more_
     * @param namesFile _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getFieldDescription(String fieldValue, String namesFile)
            throws Exception {
        return getFieldDescription(fieldValue, namesFile, null);
    }



    /**
     * _more_
     *
     * @param namesFile _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Properties getFieldProperties(String namesFile) throws Exception {
        if (namesFile == null) {
            return null;
        }
        Properties names = (Properties) namesHolder.get(namesFile);
        if (names == null) {
            try {
                names = new Properties();
                loadProperties(names, namesFile);
                namesHolder.put(namesFile, names);
            } catch (Exception exc) {
                getLogManager().logError("err:" + exc, exc);

                throw exc;
            }
        }

        return names;
    }


    /**
     * _more_
     *
     * @param fieldValue _more_
     * @param namesFile _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getFieldDescription(String fieldValue, String namesFile,
                                      String dflt)
            throws Exception {
        if (namesFile == null) {
            return dflt;
        }
        String s = (String) getFieldProperties(namesFile).get(fieldValue);
        if (s == null) {
            return dflt;
        }

        return s;
    }


    /**
     * _more_
     *
     * @param entries _more_
     */
    public void checkNewEntries(List<Entry> entries) {
        for (EntryChecker entryMonitor : entryMonitors) {
            entryMonitor.entriesCreated(entries);
        }
    }

    /**
     * _more_
     *
     * @param ids _more_
     */
    public void checkDeletedEntries(List<String> ids) {
        for (EntryChecker entryMonitor : entryMonitors) {
            entryMonitor.entriesDeleted(ids);
        }
    }


    /**
     * _more_
     *
     * @param entries _more_
     */
    public void checkModifiedEntries(List<Entry> entries) {
        for (EntryChecker entryMonitor : entryMonitors) {
            entryMonitor.entriesModified(entries);
        }
    }


    /**
     * _more_
     *
     * @param formName _more_
     * @param fieldName _more_
     *
     * @return _more_
     */
    public String getCalendarSelector(String formName, String fieldName) {
        String anchorName = "anchor." + fieldName;
        String divName    = "div." + fieldName;
        String call       = HtmlUtils.call("selectDate",
                                    HtmlUtils.comma(HtmlUtils.squote(divName),
        //                              "document.forms['"  + formName + "']." + fieldName, 
        "findFormElement('" + formName + "','" + fieldName
                            + "')", HtmlUtils.squote(anchorName),
                                    HtmlUtils.squote(
                                        "yyyy-MM-dd"))) + "return false;";

        return HtmlUtils
            .href("#", HtmlUtils
                .img(iconUrl(ICON_CALENDAR), " Choose date", HtmlUtils
                    .attr(HtmlUtils.ATTR_BORDER, "0")), HtmlUtils
                        .onMouseClick(call) + HtmlUtils
                        .attrs(HtmlUtils.ATTR_NAME, anchorName, HtmlUtils
                            .ATTR_ID, anchorName)) + HtmlUtils
                                .div("", HtmlUtils
                                    .attrs(HtmlUtils.ATTR_ID, divName, HtmlUtils
                                        .ATTR_STYLE, "position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"));
    }



    /**
     * _more_
     *
     * @param request The request
     * @param name _more_
     * @param formName _more_
     * @param date _more_
     *
     * @return _more_
     */
    public String makeDateInput(Request request, String name,
                                String formName, Date date) {
        return makeDateInput(request, name, formName, date, null);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param name _more_
     * @param formName _more_
     * @param date _more_
     * @param timezone _more_
     *
     * @return _more_
     */
    public String makeDateInput(Request request, String name,
                                String formName, Date date, String timezone) {
        return makeDateInput(request, name, formName, date, timezone, true);
    }

    /**
     * _more_
     *
     * @param request The request
     * @param name _more_
     * @param formName _more_
     * @param date _more_
     * @param timezone _more_
     * @param includeTime _more_
     *
     * @return _more_
     */
    public String makeDateInput(Request request, String name,
                                String formName, Date date, String timezone,
                                boolean includeTime) {
        String dateHelp = "e.g., yyyy-mm-dd,  now, -1 week, +3 days, etc.";
        String           timeHelp   = "hh:mm:ss Z, e.g. 20:15:00 MST";

        String           dateArg    = request.getString(name, "");
        String           timeArg    = request.getString(name + ".time", "");
        String           dateString = ((date == null)
                                       ? dateArg
                                       : dateSdf.format(date));
        SimpleDateFormat timeFormat = ((timezone == null)
                                       ? timeSdf
                                       : getSDF("HH:mm:ss z", timezone));
        String           timeString = ((date == null)
                                       ? timeArg
                                       : timeFormat.format(date));

        String           inputId    = "dateinput" + (HtmlUtils.blockCnt++);


        String           js         =
            "<script>jQuery(function() {$( " + HtmlUtils.squote("#" + inputId)
            + " ).datepicker({ dateFormat: 'yy-mm-dd',changeMonth: true, changeYear: true,constrainInput:false });});</script>";
        String extra = "";
        if (includeTime) {
            extra = " T:"
                    + HtmlUtils.input(name + ".time", timeString,
                                     HtmlUtils.sizeAttr(6)
                                     + HtmlUtils.attr(HtmlUtils.ATTR_TITLE,
                                         timeHelp));
        }

        return "\n" + js + "\n"
               + HtmlUtils.input(name, dateString,
                                HtmlUtils.SIZE_10 + HtmlUtils.id(inputId)
                                + HtmlUtils.title(dateHelp)) + extra;
    }



    /**
     * _more_
     *
     * @param link _more_
     * @param menuContents _more_
     *
     * @return _more_
     */
    public String makePopupLink(String link, String menuContents) {
        return makePopupLink(link, menuContents, false, false);
    }


    /**
     * _more_
     *
     * @param link _more_
     * @param menuContents _more_
     * @param makeClose _more_
     * @param alignLeft _more_
     *
     * @return _more_
     */
    public String makePopupLink(String link, String menuContents,
                                boolean makeClose, boolean alignLeft) {
        return makePopupLink(link, menuContents, "", makeClose, alignLeft);
    }


    public String makePopupLink(String link, String menuContents, String linkAttributes) {
        return makePopupLink(link, menuContents, linkAttributes, false, false);
    }

    public String makePopupLink(String link, String menuContents, String linkAttributes,
                                boolean makeClose, boolean alignLeft) {
        String compId   = "menu_" + HtmlUtils.blockCnt++;
        String linkId   = "menulink_" + HtmlUtils.blockCnt++;
        String contents = makePopupDiv(menuContents, compId, makeClose);
        String onClick  = HtmlUtils.onMouseClick(HtmlUtils.call("showPopup",
                             HtmlUtils.comma(new String[] { "event",
                HtmlUtils.squote(linkId), HtmlUtils.squote(compId), (alignLeft
                ? "1"
                : "0") })));
        String href = HtmlUtils.href("javascript:noop();", link,
                                    onClick + HtmlUtils.id(linkId) + linkAttributes);

        return href + contents;
    }



    /**
     * _more_
     *
     * @param link _more_
     * @param innerContents _more_
     * @param initCall _more_
     *
     * @return _more_
     */
    public String makeStickyPopup(String link, String innerContents,
                                  String initCall) {
        boolean alignLeft = true;
        String  compId    = "menu_" + HtmlUtils.blockCnt++;
        String  linkId    = "menulink_" + HtmlUtils.blockCnt++;
        String  contents  = makeStickyPopupDiv(innerContents, compId);
        String  onClick   =
            HtmlUtils.onMouseClick(HtmlUtils.call("showStickyPopup",
                HtmlUtils.comma(new String[] { "event",
                HtmlUtils.squote(linkId), HtmlUtils.squote(compId), (alignLeft
                ? "1"
                : "0") })) + initCall);
        String href = HtmlUtils.href("javascript:noop();", link,
                                    onClick + HtmlUtils.id(linkId));

        return href + contents;
    }



    /**
     * _more_
     *
     * @param contents _more_
     * @param compId _more_
     *
     * @return _more_
     */
    public String makeStickyPopupDiv(String contents, String compId) {
        StringBuffer menu  = new StringBuffer();
        String       cLink = HtmlUtils.jsLink(
                           HtmlUtils.onMouseClick(
                               HtmlUtils.call(
                                   "hideElementById",
                                   HtmlUtils.squote(compId))), HtmlUtils.img(
                                       iconUrl(ICON_CLOSE)), "");
        contents = cLink + HtmlUtils.br() + contents;

        menu.append(HtmlUtils.div(contents,
                                 HtmlUtils.id(compId)
                                 + HtmlUtils.cssClass(CSS_CLASS_POPUP)));

        return menu.toString();
    }




    /**
     * _more_
     *
     * @param contents _more_
     * @param compId _more_
     * @param makeClose _more_
     *
     * @return _more_
     */
    public String makePopupDiv(String contents, String compId,
                               boolean makeClose) {
        StringBuffer menu = new StringBuffer();
        if (makeClose) {
            String cLink =
                HtmlUtils.jsLink(HtmlUtils.onMouseClick("hidePopupObject();"),
                                HtmlUtils.img(iconUrl(ICON_CLOSE)), "");
            contents = cLink + HtmlUtils.br() + contents;
        }

        menu.append(HtmlUtils.div(contents,
                                 HtmlUtils.id(compId)
                                 + HtmlUtils.cssClass(CSS_CLASS_POPUP)));

        return menu.toString();
    }



    /**
     * _more_
     *
     * @param request The request
     * @param url _more_
     * @param okArg _more_
     * @param extra _more_
     *
     * @return _more_
     */
    public static String makeOkCancelForm(Request request, RequestUrl url,
                                          String okArg, String extra) {
        StringBuffer fb = new StringBuffer();
        fb.append(request.form(url));
        fb.append(extra);
        String okButton     = HtmlUtils.submit("OK", okArg);
        String cancelButton = HtmlUtils.submit("Cancel", Constants.ARG_CANCEL);
        String buttons      = RepositoryUtil.buttons(okButton, cancelButton);
        fb.append(buttons);
        fb.append(HtmlUtils.formClose());

        return fb.toString();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public XmlEncoder getEncoder() {
        XmlEncoder xmlEncoder = new XmlEncoder();
        xmlEncoder.addClassPatternReplacement("ucar.unidata.repository",
                "org.ramadda.repository");
        xmlEncoder.addClassPatternReplacement(
            "ucar.unidata.repository.data.Catalog",
            "org.ramadda.geodata.thredds.Catalog");

        return xmlEncoder;
    }

    /**
     * _more_
     *
     * @param object _more_
     *
     * @return _more_
     */
    public static String encodeObject(Object object) {
        return xmlEncoder.toXml(object);
    }

    /**
     * _more_
     *
     * @param xml _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static Object decodeObject(String xml) throws Exception {
        return xmlEncoder.toObject(xml);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<String> getPythonLibs() {
        return getPluginManager().getPythonLibs();
    }




}
