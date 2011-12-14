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


import org.ramadda.repository.admin.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.util.MultiJarClassLoader;
import org.ramadda.util.TempDir;


import org.w3c.dom.*;

import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;



import ucar.unidata.util.PluginClassLoader;

import ucar.unidata.util.StringUtil;

import ucar.unidata.xml.XmlUtil;



import java.io.*;

import java.io.File;
import java.io.InputStream;



import java.lang.reflect.*;

import java.net.*;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import java.util.jar.*;

import java.util.regex.*;
import java.util.zip.*;
import java.util.zip.*;




/**
 * This class loads and manages the plugins
 */
public class PluginManager extends RepositoryManager {

    /** _more_ */
    private StringBuffer pluginSB = new StringBuffer();

    /** _more_ */
    private List<String> propertyFiles = new ArrayList<String>();

    /** _more_ */
    private List<String> templateFiles = new ArrayList<String>();

    /** _more_ */
    private List<String> sqlFiles = new ArrayList<String>();

    /** _more_ */
    private List<String> pluginFiles = new ArrayList<String>();

    /** _more_ */
    private List<MultiJarClassLoader> classLoaders =
        new ArrayList<MultiJarClassLoader>();

    /** _more_ */
    private MultiJarClassLoader classLoader;

    /** _more_ */
    private Properties properties;

    /** _more_ */
    private List<String> metadataDefFiles = new ArrayList<String>();

    /** _more_ */
    private List<String> typeDefFiles = new ArrayList<String>();

    /** _more_ */
    private List<String> apiDefFiles = new ArrayList<String>();

    /** _more_ */
    private List<String> outputDefFiles = new ArrayList<String>();


    /** _more_ */
    private List<String> pythonLibs = new ArrayList<String>();



    /** _more_ */
    private Hashtable<String, String> htdocsMap = new Hashtable<String,
                                                      String>();

    /** _more_ */
    private List<String[]> docUrls = new ArrayList<String[]>();

    /** _more_ */
    private List<Class> adminHandlerClasses = new ArrayList<Class>();


    /** _more_ */
    private List<PageDecorator> pageDecorators =
        new ArrayList<PageDecorator>();

    /** _more_ */
    private List<ImportHandler> importHandlers =
        new ArrayList<ImportHandler>();

    /** Keeps track of files we've seen */
    private HashSet seenThings = new HashSet();

    /**
     * _more_
     *
     * @param repository _more_
     */
    public PluginManager(Repository repository) {
        super(repository);
    }

    /**
     * _more_
     *
     * @param object _more_
     */
    public void markSeen(Object object) {
        seenThings.add(object);
    }

    /**
     * _more_
     *
     * @param object _more_
     *
     * @return _more_
     */
    public boolean haveSeen(Object object) {
        boolean contains = seenThings.contains(object);
        if ( !contains) {
            markSeen(object);
        }
        return contains;
    }


    /**
     * _more_
     *
     * @param properties _more_
     *
     * @throws Exception On badness
     */
    public void init(Properties properties) throws Exception {
        this.properties = properties;
        if (classLoader == null) {
            classLoader = new MyClassLoader(getClass().getClassLoader());
            classLoaders.add(classLoader);
            Misc.addClassLoader(classLoader);
        }

        loadPlugins();
        apiDefFiles.addAll(0, getRepository().getResourcePaths(PROP_API));
        typeDefFiles.addAll(0, getRepository().getResourcePaths(PROP_TYPES));
        outputDefFiles.addAll(
            0, getRepository().getResourcePaths(PROP_OUTPUTHANDLERS));
        metadataDefFiles.addAll(
            0, getRepository().getResourcePaths(PROP_METADATA));
    }


    /**
     * _more_
     *
     * @throws Exception On badness
     */
    public void loadPlugins() throws Exception {
        //The false says not to scour
        TempDir tempDir = getStorageManager().makeTempDir("tmpplugins",
                              false);
        File   tmpPluginsDir = tempDir.getDir();
        File   dir           = getStorageManager().getPluginsDir();
        File[] plugins       = dir.listFiles();
        Arrays.sort(plugins);
        for (int i = 0; i < plugins.length; i++) {
            if (plugins[i].isDirectory()) {
                continue;
            }
            String pluginFile = plugins[i].toString();
            //            System.err.println ("plugin:" + pluginFile);
            if (haveSeen(pluginFile)) {
                continue;
            }
            processPluginFile(pluginFile, pluginSB, classLoader,
                              tmpPluginsDir);
        }
        loadPropertyFiles();
    }

    /**
     * _more_
     *
     * @throws Exception On badness
     */
    public void loadPropertyFiles() throws Exception {
        for (String f : propertyFiles) {
            //            if (haveSeen(f)) {
            //                continue;
            //            }
            getRepository().loadProperties(properties, f);
        }
    }



    /**
     * _more_
     *
     * @param pluginFile _more_
     * @param pluginSB _more_
     * @param classLoader _more_
     * @param tmpPluginsDir _more_
     *
     * @throws Exception On badness
     */
    private void processPluginFile(String pluginFile, StringBuffer pluginSB,
                                   MultiJarClassLoader classLoader,
                                   File tmpPluginsDir)
            throws Exception {

        File tmpPluginFile = new File(pluginFile);
        if (pluginFile.toLowerCase().endsWith(".zip")) {
            ZipInputStream zin =
                new ZipInputStream(new FileInputStream(pluginFile));
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                String path = ze.getName();

                //Turn the path into a filename
                path = path.replaceAll("/", "_");
                File tmpFile = new File(IOUtil.joinDir(tmpPluginsDir, path));
                //Skip the manifest
                if (tmpFile.toString().indexOf("MANIFEST") >= 0) {
                    continue;
                }
                //Write out the zipped file and load it as a plugin
                FileOutputStream fos =
                    getStorageManager().getFileOutputStream(tmpFile);
                IOUtil.writeTo(zin, fos);
                IOUtil.close(fos);
                processPluginFile(tmpFile.toString(), pluginSB, classLoader,
                                  tmpPluginsDir);
            }
            zin.close();
        } else if (pluginFile.toLowerCase().endsWith(".jar")) {
            pluginSB.append(
                "<tr><td><b>Plugin file</b></td><td colspan=2><i>"
                + pluginFile + "</i> "
                + new Date(tmpPluginFile.lastModified()) + " Length:"
                + tmpPluginFile.length() + "</td></tr>");
            classLoader.addJar(pluginFile);
        } else {
            pluginSB.append(
                "<tr><td><b>Plugin file</b></td><td colspan=2><i>"
                + pluginFile + "</i>   "
                + new Date(tmpPluginFile.lastModified()) + " Length:"
                + tmpPluginFile.length() + "</td></tr>");
            checkFile(pluginFile, true);
        }
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    protected boolean reloadFile(String file) {
        boolean contains = seenThings.contains(file);
        seenThings.remove(file);
        checkFile(file);
        return contains;
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    protected boolean checkFile(String file) {
        return checkFile(file, false);
    }

    /**
     * _more_
     *
     * @param desc _more_
     * @param what _more_
     */
    private void pluginStat(String desc, Object what) {
        pluginSB.append("<tr><td></td><td><b>" + desc + "</b></td><td><i>"
                        + what + "</i></td></tr>");
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public Result adminPluginUpload(Request request) throws Exception {
        String pluginFile = request.getUploadedFile(ARG_PLUGIN_FILE);
        if ((pluginFile == null) || !new File(pluginFile).exists()) {
            return getAdmin().makeResult(
                request, "Administration",
                new StringBuffer("No plugin file provided"));
        }
        if (getRepository().installPlugin(pluginFile)) {
            return getAdmin().makeResult(
                request, "Administration",
                new StringBuffer(
                    "Plugin has been re-installed.<br><b>Note: Reinstalling a plugin can lead to odd behavior. It is probably best to restart RAMADDA</b>"));
        } else {
            return getAdmin().makeResult(
                request, "Administration",
                new StringBuffer("Plugin installed"));
        }
    }


    /**
     * _more_
     *
     *
     * @param request _more_
     * @param sb _more_
     */
    public void addStatusInfo(Request request, StringBuffer sb) {
        StringBuffer formBuffer = new StringBuffer();
        request.uploadFormWithAuthToken(formBuffer,
                                        getAdmin().URL_ADMIN_PLUGIN_UPLOAD,
                                        "");
        formBuffer.append(msgLabel("Plugin File"));
        formBuffer.append(HtmlUtil.fileInput(ARG_PLUGIN_FILE,
                                             HtmlUtil.SIZE_60));
        formBuffer.append(HtmlUtil.submit("Upload new plugin file"));
        formBuffer.append(HtmlUtil.formClose());
        formBuffer.append(HtmlUtil.br());
        formBuffer.append(msg("Installed Plugins"));
        formBuffer.append("<table>");
        formBuffer.append(pluginSB);
        formBuffer.append("</table>");
        sb.append(formBuffer);
    }


    /**
     * _more_
     *
     * @param file _more_
     * @param fromPlugin _more_
     *
     * @return _more_
     */
    protected boolean checkFile(String file, boolean fromPlugin) {
        if (file.indexOf("api.xml") >= 0) {
            if (fromPlugin) {
                pluginStat("Api", file);
            }
            apiDefFiles.add(file);
        } else if ((file.indexOf("types.xml") >= 0)
                   || (file.indexOf("type.xml") >= 0)) {
            if (fromPlugin) {
                pluginStat("Types", file);
            }
            typeDefFiles.add(file);
        } else if (file.indexOf("outputhandlers.xml") >= 0) {
            if (fromPlugin) {
                pluginStat("Output", file);
            }
            outputDefFiles.add(file);
        } else if (file.indexOf("metadata.xml") >= 0) {
            if (fromPlugin) {
                pluginStat("Metadata", file);
            }
            metadataDefFiles.add(file);
        } else if (file.endsWith(".py")) {
            if (fromPlugin) {
                pluginStat("Python", file);
            }
            pythonLibs.add(file);
        } else if (file.endsWith(".sql")) {
            if (fromPlugin) {
                pluginStat("Sql", file);
            }
            sqlFiles.add(file);
        } else if (file.endsWith("template.html")) {
            if (fromPlugin) {
                pluginStat("Template", file);
            }
            templateFiles.add(file);
        } else if (file.endsWith(".properties")) {
            if (fromPlugin) {
                pluginStat("Properties", file);
                propertyFiles.add(file);
            }
        } else {
            //            if (fromPlugin) 
            //                pluginStat("Unknown", file);
            pluginFiles.add(file);
            return false;
        }
        return true;
    }


    /**
     * Class MyClassLoader provides a hook into the MultiJarClassLoader routines
     *
     */
    private class MyClassLoader extends MultiJarClassLoader {

        /**
         * _more_
         *
         * @param parent _more_
         *
         * @throws Exception On badness
         */
        public MyClassLoader(ClassLoader parent) throws Exception {
            super(parent);
        }


        /**
         * _more_
         *
         * @param name _more_
         *
         * @return _more_
         *
         * @throws ClassNotFoundException On badness
         */
        public Class xxxloadClass(String name) throws ClassNotFoundException {
            try {
                Class clazz = super.loadClass(name);
                if (clazz != null) {
                    return clazz;
                }
                return clazz;
            } catch (ClassNotFoundException cnfe) {
                for (MultiJarClassLoader loader : classLoaders) {
                    Class clazz = loader.getClassFromPlugin(name);
                    if (clazz != null) {
                        return clazz;
                    }
                }
                throw cnfe;
            }
        }


        /**
         * Check if this class is one of the special classes, e.g., ImportHandler, PageDecorator, etc.
         *
         * @param c the class
         *
         * @throws Exception On badness
         */
        protected void checkClass(Class c) throws Exception {
            if (ImportHandler.class.isAssignableFrom(c)) {
                pluginStat("Import handler", c.getName());
                Constructor ctor = Misc.findConstructor(c,
                                       new Class[] { Repository.class });
                if (ctor != null) {
                    importHandlers.add(
                        (ImportHandler) ctor.newInstance(
                            new Object[] { getRepository() }));
                } else {
                    importHandlers.add((ImportHandler) c.newInstance());
                }
                return;
            }


            if (UserAuthenticator.class.isAssignableFrom(c)) {
                pluginStat("Authenticator", c.getName());
                Constructor ctor = Misc.findConstructor(c,
                                       new Class[] { Repository.class });
                if (ctor != null) {
                    getUserManager().addUserAuthenticator(
                        (UserAuthenticator) ctor.newInstance(
                            new Object[] { getRepository() }));

                } else {
                    getUserManager().addUserAuthenticator(
                        (UserAuthenticator) c.newInstance());
                }
            } else if (PageDecorator.class.isAssignableFrom(c)) {
                pluginStat("Page decorator", c.getName());
                PageDecorator pageDecorator = (PageDecorator) c.newInstance();
                pageDecorators.add(pageDecorator);
            } else if (AdminHandler.class.isAssignableFrom(c)) {
                pluginStat("Admin handler", c.getName());
                adminHandlerClasses.add(c);
            } else if (Harvester.class.isAssignableFrom(c)) {
                pluginStat("Harvester", c.getName());
                getHarvesterManager().addHarvesterType(c);
            }

            super.checkClass(c);
        }

        /**
         * _more_
         *
         *
         * @param jarFilePath _more_
         * @param jarEntry _more_
         *
         * @return _more_
         */
        protected String defineResource(String jarFilePath,
                                        JarEntry jarEntry) {
            String path = super.defineResource(jarFilePath, jarEntry);
            checkFile(path, true);
            String entryName = jarEntry.getName();
            int    idx       = entryName.indexOf("htdocs/");

            if (idx >= 0) {
                String htpath = entryName.substring(idx + "htdocs".length());
                htdocsMap.put(htpath, path);

                if (htpath.matches("/[^/]+/index.html")) {
                    try {
                        String contents =
                            getStorageManager().readSystemResource(path);
                        Pattern pattern =
                            Pattern.compile("(?s).*<title>(.*)</title>");
                        Matcher matcher = pattern.matcher(contents);
                        String  title   = htpath;
                        if (matcher.find()) {
                            title = matcher.group(1);
                        }
                        String url = getRepository().getUrlBase() + htpath;
                        if (htpath.startsWith("/userguide")) {
                            docUrls.add(0, new String[] { url, title });
                        } else {
                            docUrls.add(new String[] { url, title });
                        }
                    } catch (Exception exc) {
                        throw new RuntimeException(exc);
                    }
                }
            }
            return path;
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<String[]> getDocUrls() {
        return docUrls;
    }

    /**
     * Get the MetadataDefFiles property.
     *
     * @return The MetadataDefFiles
     */
    public List<String> getMetadataDefFiles() {
        return metadataDefFiles;
    }


    /**
     * Get the TypeDefFiles property.
     *
     * @return The TypeDefFiles
     */
    public List<String> getTypeDefFiles() {
        return typeDefFiles;
    }


    /**
     * Get the ApiDefFiles property.
     *
     * @return The ApiDefFiles
     */
    public List<String> getApiDefFiles() {
        return apiDefFiles;
    }



    /**
     * Get the OutputDefFiles property.
     *
     * @return The OutputDefFiles
     */
    public List<String> getOutputDefFiles() {
        return outputDefFiles;
    }

    /**
     * Get the PythonLibs property.
     *
     * @return The PythonLibs
     */
    public List<String> getPythonLibs() {
        return pythonLibs;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<String> getPluginFiles() {
        return pluginFiles;
    }

    /**
     * Get the SqlFiles property.
     *
     * @return The SqlFiles
     */
    public List<String> getSqlFiles() {
        return sqlFiles;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<PageDecorator> getPageDecorators() {
        return pageDecorators;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<ImportHandler> getImportHandlers() {
        return importHandlers;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Hashtable<String, String> getHtdocsMap() {
        return htdocsMap;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<String> getTemplateFiles() {
        return templateFiles;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public List<Class> getAdminHandlerClasses() {
        return adminHandlerClasses;
    }
}
