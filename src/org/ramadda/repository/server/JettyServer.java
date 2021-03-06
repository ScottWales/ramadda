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

package org.ramadda.repository.server;


import org.eclipse.jetty.server.Server;
// Jetty 8 
import org.eclipse.jetty.server.ssl.SslSocketConnector;
// Jetty 9
//import org.eclipse.jetty.server.ServerConnector;
//import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import org.ramadda.repository.Constants;
import org.ramadda.repository.Repository;

import ucar.unidata.util.LogUtil;
//import org.eclipse.jetty.servlet.Context;




import java.io.File;

import java.util.Hashtable;
import java.util.Properties;




/**
 */
public class JettyServer implements Constants {

    /** _more_ */
    private String[] args;

    /** _more_ */
    private int port;

    /** _more_ */
    private int sslPort = -1;

    /** _more_ */
    private Server server;

    /** _more_ */
    private RepositoryServlet baseServlet;

    /** _more_ */
    private ServletContextHandler context;

    /** _more_ */
    private Repository baseRepository;

    /** _more_ */
    private Hashtable<RepositoryServlet, ServletHolder> servletToHolder =
        new Hashtable<RepositoryServlet, ServletHolder>();

    /**
     * _more_
     *
     * @param args _more_
     * @throws Throwable _more_
     */
    public JettyServer(String[] args) throws Throwable {
        this.args = args;
        port      = 8080;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-port")) {
                port = new Integer(args[i + 1]).intValue();
                //Keep looping so we get the last -port in the arg list
            }
        }

        server  = new Server(port);
        context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        baseServlet = addServlet();
        context.addServlet(new ServletHolder(baseServlet), "/");
        try {
            initSsl(server, baseServlet.getRepository());
        } catch (Throwable exc) {
            baseServlet.getRepository().getLogManager().logError(
                "SSL: error opening ssl connection", exc);
        }
        baseRepository = baseServlet.getRepository();
        server.start();
        server.join();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getPort() {
        return port;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public RepositoryServlet addServlet() throws Exception {
        Properties properties  = new Properties();
        String[]   cmdLineArgs = args;

        return addServlet(new RepositoryServlet(this, cmdLineArgs, port,
                properties));
    }


    /**
     * _more_
     *
     * @param servlet _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public RepositoryServlet addServlet(RepositoryServlet servlet)
            throws Exception {
        Repository    repository = servlet.getRepository();
        String        path       = repository.getUrlBase();
        ServletHolder holder     = new ServletHolder(servlet);
        context.addServlet(holder, path + "/*");
        servletToHolder.put(servlet, holder);
        repository.setJettyServer(this);

        return servlet;
    }


    /**
     * _more_
     *
     * @param servlet _more_
     *
     * @throws Exception _more_
     */
    public void removeServlet(RepositoryServlet servlet) throws Exception {
        ServletHolder holder = servletToHolder.get(servlet);
        //TODO: Remove the servlet from the server
    }

    /**
     * _more_
     *
     * @param server _more_
     * @param repository _more_
     *
     * @throws Throwable _more_
     */
    protected void initSsl(Server server, Repository repository)
            throws Throwable {


        File keystore =
            new File(repository.getPropertyValue(PROP_SSL_KEYSTORE,
                repository.getStorageManager().getRepositoryDir()
                + "/keystore", false));
        if ( !keystore.exists()) {
            return;
        }

        if (repository.getProperty(PROP_SSL_IGNORE, false)) {
            repository.getLogManager().logInfo("SSL: ssl.ignore is set.");

            return;
        }
        repository.getLogManager().logInfo("SSL: using keystore: "
                                           + keystore);

        String password = repository.getPropertyValue(PROP_SSL_PASSWORD,
                              (String) null, false);
        String keyPassword = repository.getPropertyValue(PROP_SSL_PASSWORD,
                                 password, false);
        if (password == null) {
            repository.getLogManager().logInfo(
                "SSL: no password and keypassword property defined");

            /*
            repository.getLogManager().logInfoAndPrint(
                "SSL: define the properties:\n\t" + PROP_SSL_PASSWORD
                + "=<the ssl password>\n" + "\t" + PROP_SSL_KEYPASSWORD
                + "=<the key password>\n"
                + "in some .properties file (e.g., \"ssl.properties\") in the RAMADDA directory:"
                + repository.getStorageManager().getRepositoryDir()
                + "\nor as a System property on the java command line:"
                + "-D" + PROP_SSL_PASSWORD + "=<the ssl password>  " + "-D"
                + PROP_SSL_KEYPASSWORD + "=<the key password>");
            */
            return;
        }


        sslPort = -1;
        String ssls = repository.getPropertyValue(PROP_SSL_PORT,
                          (String) null, false);
        if ((ssls != null) && (ssls.trim().length() > 0)) {
            sslPort = new Integer(ssls.trim());

        }

        if (sslPort < 0) {
            repository.getLogManager().logInfoAndPrint(
                "SSL: no ssl port defined. not creating ssl connection");
            repository.getLogManager().logInfoAndPrint(
                "SSL: define the property:\n\t" + PROP_SSL_PORT
                + "=<the ssl port>\n"
                + "in some .properties file (e.g., \"ssl.properties\") in the RAMADDA directory:"
                + repository.getStorageManager().getRepositoryDir()
                + "\nor as a System property on the java command line:"
                + "-D" + PROP_SSL_PORT + "=<the ssl port>");

            return;
        }

        repository.getLogManager().logInfo(
            "SSL: creating ssl connection on port:" + sslPort);
        /*
        // Jetty <7
        SslSocketConnector sslSocketConnector = new SslSocketConnector();
        sslSocketConnector.setKeystore(keystore.toString());
        // The password for the key store
        sslSocketConnector.setPassword(password);
        // The password (if any) for the specific key within the key store
        sslSocketConnector.setKeyPassword(keyPassword);
        sslSocketConnector.setTrustPassword(password);
        sslSocketConnector.setPort(sslPort);
        server.addConnector(sslSocketConnector);
        */
        // Jetty 7,8,&9
        SslContextFactory sslContext = new SslContextFactory();
        sslContext.setKeyStorePath(keystore.toString());
        // The password for the key store
        sslContext.setKeyStorePassword(password);
        // The password (if any) for the specific key within the key store
        sslContext.setKeyManagerPassword(keyPassword);
        sslContext.setTrustStorePassword(password);
        // Jetty 7&8
        SslSocketConnector sslSocketConnector =
            new SslSocketConnector(sslContext);
        sslSocketConnector.setPort(sslPort);
        server.addConnector(sslSocketConnector);
        /* Jetty 9 - not sure this is correct
        SslConnectionFactory sslFactory = new SslConnectionFactory(sslContext, "http/1.1");
        ServerConnector sslConnector = new ServerConnector(server, sslFactory);
        sslConnector.setPort(sslPort);
        server.addConnector(sslConnector);
        */

        repository.setHttpsPort(sslPort);
    }




    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Throwable _more_
     */
    public static void main(String[] args) throws Throwable {
        try {
            JettyServer mds = new JettyServer(args);
        } catch (Exception exc) {
            LogUtil.printExceptionNoGui(null, "Error in main",
                                        LogUtil.getInnerException(exc));
            System.exit(1);
        }
    }



}
