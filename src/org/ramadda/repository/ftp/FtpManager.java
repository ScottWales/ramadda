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

package org.ramadda.repository.ftp;


import org.apache.ftpserver.*;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.listener.*;
import org.apache.ftpserver.ssl.*;
import org.apache.ftpserver.usermanager.*;
import org.apache.ftpserver.usermanager.impl.*;




import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;




import ucar.unidata.util.Misc;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class FtpManager extends RepositoryManager {

    /** _more_ */
    public static final String DFLT_PASSIVE_PORTS = "44001-44099";

    /** _more_          */
    private final LogManager.LogId LOGID =
        new LogManager.LogId("org.apache.ftpserver");




    /** _more_ */
    private FtpServer server;

    /** _more_ */
    private int port = -1;

    /** _more_ */
    private String passivePorts;

    /**
     * _more_
     *
     * @param repository _more_
     */
    public FtpManager(Repository repository) {
        super(repository);
        try {
            checkServer();
        } catch (Exception exc) {
            exc.printStackTrace();
            logError("Creating FTP server", exc);
        }
    }


    /**
     * _more_
     *
     * @param message _more_
     * @param exc _more_
     */
    public void logError(String message, Exception exc) {
        getRepository().getLogManager().logError(LOGID, "RAMADDA:" + message,
                exc);
    }


    /**
     * _more_
     *
     * @param message _more_
     */
    public void logInfo(String message) {
        getLogManager().logInfo(LOGID, "RAMADDA:" + message);
    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void checkServer() throws Exception {
        int newPort = getRepository().getProperty(PROP_FTP_PORT, -1);
        if (newPort < 0) {
            stop();

            return;
        }
        if (newPort != port) {
            stop();
        } else if ( !Misc.equals(
                passivePorts,
                getRepository().getProperty(
                    PROP_FTP_PASSIVEPORTS, DFLT_PASSIVE_PORTS))) {
            stop();
        }

        logInfo("Calling initFtpServer");
        port = newPort;
        if (server == null) {
            initFtpServer();
        }

    }


    /**
     * _more_
     */
    public void shutdown() {
        stop();
    }

    /**
     * _more_
     */
    private void stop() {
        if (server != null) {
            logInfo("Calling server.stop");
            server.stop();
        }
        server = null;
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void initFtpServer() throws Exception {

        FtpServerFactory serverFactory = new FtpServerFactory();
        Hashtable        ftplets = new Hashtable<java.lang.String, Ftplet>();
        ftplets.put("default", new RepositoryFtplet(this));
        serverFactory.setFtplets(ftplets);

        ListenerFactory factory = new ListenerFactory();
        logInfo("Setting port to " + port);
        // set the port of the listener
        factory.setPort(port);


        DataConnectionConfigurationFactory dccf =
            new DataConnectionConfigurationFactory();
        passivePorts = getRepository().getProperty(PROP_FTP_PASSIVEPORTS,
                DFLT_PASSIVE_PORTS);
        logInfo("Setting passive ports to:" + passivePorts);
        dccf.setPassivePorts(passivePorts);
        factory.setDataConnectionConfiguration(
            dccf.createDataConnectionConfiguration());

        File keystore =
            new File(getRepository().getPropertyValue(PROP_SSL_KEYSTORE,
                getRepository().getStorageManager().getRepositoryDir()
                + "/keystore", false));

        /*
        if (keystore.exists()) {
            logInfo("Using FTPS");
            String password =
                getRepository().getPropertyValue(PROP_SSL_PASSWORD,
                    (String) null, false);
            String keyPassword =
                getRepository().getPropertyValue(PROP_SSL_PASSWORD, password,
                    false);

            SslConfigurationFactory ssl = new SslConfigurationFactory();
            ssl.setKeystoreFile(keystore);
            ssl.setKeystorePassword(keyPassword);

            factory.setSslConfiguration(ssl.createSslConfiguration());
            factory.setImplicitSsl(true);
        }
        */


        // replace the default listener
        serverFactory.addListener("default", factory.createListener());


        serverFactory.setUserManager(new RepositoryFtpUserManager(this));

        // start the server
        server = serverFactory.createServer();
        logInfo("Calling server.start");
        server.start();
        logInfo("Starting server on port:" + port);
    }



}
