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

package org.ramadda.repository;


import org.ramadda.repository.auth.AccessException;
import org.ramadda.repository.auth.AuthorizationMethod;
import org.ramadda.repository.auth.Permission;
import org.ramadda.repository.auth.User;
import org.ramadda.repository.database.Tables;
import org.ramadda.repository.harvester.Harvester;
import org.ramadda.repository.metadata.AdminMetadataHandler;
import org.ramadda.repository.metadata.ContentMetadataHandler;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.metadata.MetadataHandler;
import org.ramadda.repository.metadata.MetadataType;
import org.ramadda.repository.output.HtmlOutputHandler;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.repository.output.PageStyle;
import org.ramadda.repository.output.XmlOutputHandler;
import org.ramadda.repository.type.Column;
import org.ramadda.repository.type.ProcessFileTypeHandler;
import org.ramadda.repository.type.TypeHandler;
import org.ramadda.repository.type.TypeInsertInfo;
import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;
import org.ramadda.util.FormInfo;
import org.ramadda.util.HtmlTemplate;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.JQuery;
import org.ramadda.util.TTLCache;
import org.ramadda.util.TTLObject;
import org.ramadda.util.Utils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.JobManager;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlNodeList;
import ucar.unidata.xml.XmlUtil;


import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.Rectangle2D;



import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.net.URL;
import java.net.URLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;





/**
 * This class does most of the work of managing repository content
 */
public class EntryManager extends RepositoryManager {

    /** _more_ */
    public static final String[] PRELOAD_CATEGORIES = { "General",
            "Information", "Documents", "Collaboration", "Database" };

    /** _more_ */
    public static final String ENTRYID_PROCESS = "process";

    /** _more_ */
    private EntryUtil entryUtil;


    /** _more_ */
    public static boolean debug = false;

    //In sql

    /** _more_ */
    public static final int MAX_NAME_LENGTH = 200;

    /** _more_ */
    public static final int MAX_DESCRIPTION_LENGTH = 15000;


    /** How many entries to we keep in the cache */
    public static final int ENTRY_CACHE_LIMIT = 5000;


    /** _more_ */
    public static final String SESSION_FOLDERS = "folders";

    /** _more_ */
    private Object MUTEX_ENTRY = new Object();


    /** _more_ */
    private static final String GROUP_TOP = "Top";

    /** _more_ */
    private static final String ID_ROOT = "root";


    /** _more_ */
    public static final String ID_PREFIX_REMOTE = "remote:";


    /** _more_ */
    private TTLObject<Entry> rootCache;

    /** Caches sites */
    private TTLCache<String, Entry> entryCache;




    /**
     * _more_
     *
     * @param repository _more_
     */
    public EntryManager(Repository repository) {
        super(repository);
    }


    /**
     * _more_
     *
     * @param msg _more_
     */
    public void debug(String msg) {
        if (debug) {
            logInfo(msg);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public EntryUtil getEntryUtil() {
        if (entryUtil == null) {
            entryUtil = new EntryUtil(getRepository());
        }

        return entryUtil;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Entry getTopGroup() {
        try {
            if (rootCache == null) {
                initTopEntry();
            }
            Entry topEntry = rootCache.get();
            if (topEntry == null) {
                topEntry = initTopEntry();
            }

            return topEntry;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * _more_
     *
     *
     * @return _more_
     * @throws Exception _more_
     */
    protected Entry initTopEntry() throws Exception {
        String fixedTopId = getProperty(PROP_ENTRY_TOP, (String) null);
        Clause clause;
        if (fixedTopId != null) {
            clause = Clause.eq(Tables.ENTRIES.COL_ID, fixedTopId);
        } else {
            clause = Clause.isNull(Tables.ENTRIES.COL_PARENT_GROUP_ID);
        }

        Statement statement =
            getDatabaseManager().select(Tables.ENTRIES.COLUMNS,
                                        Tables.ENTRIES.NAME, clause);
        List<Entry> entries  = readEntries(statement);

        Entry       topEntry = null;
        if (entries.size() > 1) {
            System.err.println(
                "RAMADDA: there are more than one top-level entries");
            entries = getEntryUtil().sortEntriesOnCreateDate(entries, false);
            for (Entry entry : entries) {
                if (topEntry == null) {
                    if (entry.getType().equals(TypeHandler.TYPE_GROUP)) {
                        topEntry = entry;
                    }
                }
                System.err.println("entry:" + entry.getType() + " - "
                                   + entry.getName() + " " + entry.getId()
                                   + " - " + new Date(entry.getCreateDate()));
            }
        }


        if ((topEntry == null) && (entries.size() > 0)) {
            topEntry = (Entry) entries.get(0);
        }

        //Make the top group if needed
        if (topEntry == null) {
            topEntry = makeNewGroup(null, GROUP_TOP,
                                    getUserManager().getDefaultUser());
            getAccessManager().initTopEntry(topEntry);
        }


        if (rootCache == null) {
            int cacheTimeMinutes =
                getRepository().getProperty(PROP_CACHE_TTL, 60);
            //Convert to milliseconds
            rootCache = new TTLObject<Entry>(cacheTimeMinutes * 60 * 1000);
        }
        rootCache.put(topEntry);

        return topEntry;

    }


    /**
     * _more_
     *
     * @param descendent _more_
     *
     * @return _more_
     */
    public Entry getSecondToTopEntry(Entry descendent) {
        Entry topEntry = null;
        if (descendent != null) {
            topEntry = descendent;
            while (topEntry != null) {
                Entry parent = topEntry.getParentEntry();
                if (parent == null) {
                    break;
                }
                if (parent.isTopEntry()) {
                    break;
                }
                topEntry = parent;
            }
        }

        return topEntry;
    }





    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public String getFullEntryShowUrl(Request request) {
        return request.getAbsoluteUrl(getRepository().URL_ENTRY_SHOW);
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public String getFullEntryGetUrl(Request request) {
        return request.getAbsoluteUrl(getRepository().URL_ENTRY_GET);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param alias _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry getEntryFromAlias(Request request, String alias)
            throws Exception {
        return getEntryFromMetadata(request,
                                    ContentMetadataHandler.TYPE_ALIAS, alias,
                                    1);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param metadataType _more_
     * @param value _more_
     * @param attrIndex _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry getEntryFromMetadata(Request request, String metadataType,
                                      String value, int attrIndex)
            throws Exception {
        String column = ((attrIndex == 1)
                         ? Tables.METADATA.COL_ATTR1
                         : (attrIndex == 2)
                           ? Tables.METADATA.COL_ATTR2
                           : (attrIndex == 3)
                             ? Tables.METADATA.COL_ATTR3
                             : Tables.METADATA.COL_ATTR3);

        Statement statement =
            getDatabaseManager().select(
                Tables.ENTRIES.COL_ID,
                Misc.newList(Tables.ENTRIES.NAME, Tables.METADATA.NAME),
                Clause.and(
                    new Clause[] {
                        Clause.join(
                            Tables.ENTRIES.COL_ID,
                            Tables.METADATA.COL_ENTRY_ID),
                        Clause.eq(column, value),
                        Clause.eq(Tables.METADATA.COL_TYPE,
                                  metadataType) }), "", 1);

        SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
        ResultSet        results;
        while ((results = iter.getNext()) != null) {
            String id = results.getString(1);
            iter.close();

            return getEntry(request, id);
        }

        return null;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    private TTLCache<String, Entry> getEntryCache() {
        //Get a local copy because another thread could clear the cache while we're in the middle of this
        TTLCache<String, Entry> theCache = entryCache;
        if (theCache == null) {
            int cacheTimeMinutes =
                getRepository().getProperty(PROP_CACHE_TTL, 60);
            //Convert to milliseconds
            entryCache = theCache = new TTLCache<String,
                    Entry>(cacheTimeMinutes * 60 * 1000);
        } else if (theCache.size() > ENTRY_CACHE_LIMIT) {
            clearCache();
        }

        return theCache;
    }

    /**
     * _more_
     */
    @Override
    public void clearCache() {
        super.clearCache();
        entryCache = null;
        getEntryUtil().clearCache();
    }


    /**
     * _more_
     *
     * @param entry _more_
     */
    public void cacheEntry(Entry entry) {
        synchronized (MUTEX_ENTRY) {
            //Check if we are caching
            if ( !getRepository().doCache()) {
                return;
            }
            getEntryCache().put(entry.getId(), entry);
        }
    }


    /**
     * _more_
     *
     * @param entryId _more_
     *
     * @return _more_
     */
    protected Entry getEntryFromCache(String entryId) {
        return getEntryFromCache(entryId, true);
    }

    /**
     * _more_
     *
     * @param entryId _more_
     * @param isId _more_
     *
     * @return _more_
     */
    protected Entry getEntryFromCache(String entryId, boolean isId) {
        synchronized (MUTEX_ENTRY) {
            return getEntryCache().get(entryId);
        }
    }


    /**
     * _more_
     *
     * @param groupId _more_
     *
     * @return _more_
     */
    protected Entry getGroupFromCache(String groupId) {
        return getGroupFromCache(groupId, true);
    }

    /**
     * _more_
     *
     * @param groupId _more_
     * @param isId _more_
     *
     * @return _more_
     */
    protected Entry getGroupFromCache(String groupId, boolean isId) {
        Entry entry = getEntryFromCache(groupId, isId);
        if (entry == null) {
            return null;
        }

        return entry;
    }


    /**
     * _more_
     *
     * @param id _more_
     */
    protected void removeFromCache(String id) {
        synchronized (MUTEX_ENTRY) {
            getEntryCache().remove(id);
        }
    }


    /**
     * _more_
     *
     * @param entry _more_
     */
    protected void removeFromCache(Entry entry) {
        removeFromCache(entry.getId());
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param urlArg _more_
     * @param requestUrl _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry getEntryFromRequest(Request request, String urlArg,
                                     RequestUrl requestUrl)
            throws Exception {
        Entry entry = null;
        if (request.defined(urlArg)) {
            try {
                entry = getEntryFromArg(request, urlArg);
            } catch (Exception exc) {
                logError("", exc);

                throw exc;
            }
            if (entry == null) {
                String entryId = request.getString(urlArg, BLANK);
                Entry  tmp     = getEntry(request, entryId, false);
                if (tmp != null) {
                    logInfo("Cannot access entry:" + entryId + "  IP:"
                            + request.getIp());
                    logInfo("Request:" + request);

                    throw new IllegalArgumentException(
                        "You do not have access to this entry");
                }
            }
        } else {
            String path   = request.getRequestPath();
            String prefix = requestUrl.toString();
            if (path.length() > prefix.length()) {
                String suffix = path.substring(prefix.length());
                suffix = java.net.URLDecoder.decode(suffix, "UTF-8");
                entry = findEntryFromName(request, suffix, request.getUser(),
                                          false);
                if (entry == null) {
                    fatalError(request, "Could not find entry:" + suffix);
                }
            }
            if (entry == null) {
                entry = getTopGroup();
            }
        }

        getSessionManager().setLastEntry(request, entry);

        return entry;
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
    public Result processEntryShow(Request request) throws Exception {
        if (request.getCheckingAuthMethod()) {
            OutputHandler handler = getRepository().getOutputHandler(request);

            return new Result(handler.getAuthorizationMethod(request));
        }

        Entry entry = getEntryFromRequest(request, ARG_ENTRYID,
                                          getRepository().URL_ENTRY_SHOW);

        if (entry == null) {
            fatalError(request, "No entry specified");
        }


        addSessionFolder(request, entry);
        if (entry.getIsRemoteEntry()) {
            String redirectUrl = entry.getRemoteServer()
                                 + getRepository().URL_ENTRY_SHOW.getPath();
            String[] tuple = getRemoteEntryInfo(entry.getId());
            request.put(ARG_ENTRYID, tuple[1]);
            request.put(ARG_FULLURL, "true");


            redirectUrl = redirectUrl + "?" + request.getUrlArgs();
            //            System.err.println("routing remote request:" + redirectUrl);
            URL           url        = new URL(redirectUrl);
            URLConnection connection = url.openConnection();
            InputStream   is         = connection.getInputStream();

            return new Result("", is, connection.getContentType());
        }


        if (request.get(ARG_NEXT, false)
                || request.get(ARG_PREVIOUS, false)) {
            boolean next = request.get(ARG_NEXT, false);
            List<String> ids =
                getChildIds(request,
                            findGroup(request, entry.getParentEntryId()),
                            new ArrayList<Clause>());
            String nextId = null;
            int    index  = ids.indexOf(entry.getId());
            if (index >= 0) {
                if (next) {
                    index++;
                } else {
                    index--;
                }
                if (index < 0) {
                    index = ids.size() - 1;
                } else if (index >= ids.size()) {
                    index = 0;
                }
                nextId = ids.get(index);
            }
            //Do a redirect
            if (nextId != null) {
                request.put(ARG_ENTRYID, nextId);
                request.remove(ARG_NEXT);
                request.remove(ARG_PREVIOUS);

                return new Result(request.getUrl());
            }
        }

        Result result = processEntryShow(request, entry);

        return addEntryHeader(request, entry, result);
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
    public Result processEntryDump(Request request) throws Exception {
        OutputStream os = request.getHttpServletResponse().getOutputStream();
        final PrintWriter pw = new PrintWriter(os);
        request.setReturnFilename("entries.txt");
        Statement stmt =
            getDatabaseManager().select(SqlUtil.comma(new String[] {
                Tables.ENTRIES.COL_ID,
                Tables.ENTRIES.COL_TYPE,
                Tables.ENTRIES.COL_NAME }), Tables.ENTRIES.NAME, null, null);
        getDatabaseManager().iterate(stmt, new SqlUtil.ResultsHandler() {
            public boolean handleResults(ResultSet results) throws Exception {
                int col = 1;
                pw.append(results.getString(col++));
                pw.append(",");
                pw.append(results.getString(col++));
                pw.append(",");
                pw.append(results.getString(col++));
                pw.append("\n");

                return true;
            }
        });
        pw.close();

        return Result.makeNoOpResult();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param result _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result addEntryHeader(Request request, Entry entry, Result result)
            throws Exception {
        //For now don't add the entry header for the top-level entry
        //for pages like search, etc.
        if (entry == null) {
            return result;
        }
        if (entry == null) {
            entry = getTopGroup();
        }

        //If entry is a dummy that means its from search results
        if (result.getShouldDecorate() && !entry.isDummy()) {
            StringBuffer sb             = new StringBuffer();
            Entry        entryForHeader = entry;

            //If its a search result then use the top-level group for the header
            if (entry.isDummy()) {
                entryForHeader = getTopGroup();
            }
            String entryFooter = getPageHandler().entryFooter(request,
                                     entryForHeader);

            StringBuffer titleCrumbs = new StringBuffer();
            String crumbs = getPageHandler().getEntryHeader(request,
                                entryForHeader, titleCrumbs);
            sb.append(crumbs);
            //                result.setTitle(result.getTitle() + ": " + crumbs[0]);
            //                result.setTitle(result.getTitle());
            result.putProperty(PROP_ENTRY_HEADER, sb.toString());
            result.putProperty(PROP_ENTRY_BREADCRUMBS,
                               titleCrumbs.toString());
            result.putProperty(PROP_ENTRY_FOOTER, entryFooter);

            List<Metadata> metadataList =
                getMetadataManager().findMetadata(request, entry,
                    ContentMetadataHandler.TYPE_LOGO, true);
            if ((metadataList != null) && (metadataList.size() > 0)) {
                Metadata metadata = metadataList.get(0);
                MetadataHandler handler =
                    getMetadataManager().findMetadataHandler(
                        metadata.getType());
                MetadataType metadataType =
                    handler.findType(ContentMetadataHandler.TYPE_LOGO);

                String imageUrl = metadataType.getImageUrl(request, entry,
                                      metadata, null);
                if ((imageUrl != null) && (imageUrl.length() > 0)) {
                    result.putProperty(PROP_LOGO_IMAGE, imageUrl);
                }

                String logoUrl = metadata.getAttr2();
                if ((logoUrl != null) && (logoUrl.length() > 0)) {
                    result.putProperty(PROP_LOGO_URL, logoUrl);
                }

                String pageTitle = metadata.getAttr3();
                if ((pageTitle != null) && (pageTitle.length() > 0)) {
                    result.putProperty(PROP_REPOSITORY_NAME, pageTitle);
                }
            }

        }

        return result;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processEntryShow(Request request, Entry entry)
            throws Exception {
        Result result = null;
        OutputHandler outputHandler =
            getRepository().getOutputHandler(request);

        if (request.isRobot()) {
            if ( !outputHandler.allowRobots()) {
                return getRepository().getNoRobotsResult(request);
            }
        }


        outputHandler.incrNumberOfConnections();
        OutputType outputType = request.getOutput();
        outputType.incrNumberOfCalls();
        boolean handleAsGroup = handleEntryAsGroup(entry);

        try {
            if (handleAsGroup) {
                result = processGroupShow(request, outputHandler, outputType,
                                          entry);
            } else {
                OutputType dfltOutputType = getDefaultOutputType(request,
                                                entry, null, null);
                if (dfltOutputType != null) {
                    outputType = dfltOutputType;
                    outputHandler =
                        getRepository().getOutputHandler(outputType);
                }
                result = outputHandler.outputEntry(request, outputType,
                        entry);
            }
        } finally {
            outputHandler.decrNumberOfConnections();
        }

        return result;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputHandler _more_
     * @param outputType _more_
     * @param group _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processGroupShow(Request request,
                                   OutputHandler outputHandler,
                                   OutputType outputType, Entry group)
            throws Exception {
        boolean doLatest = request.get(ARG_LATEST, false);
        //not sure why we asked the repository for the type
        //TypeHandler  typeHandler = getRepository().getTypeHandler(request);
        TypeHandler  typeHandler = group.getTypeHandler();
        List<Clause> where       = typeHandler.assembleWhereClause(request);
        List<Entry>  entries     = new ArrayList<Entry>();
        List<Entry>  subGroups   = new ArrayList<Entry>();
        try {
            typeHandler.getChildrenEntries(request, group, entries,
                                           subGroups, where);
        } catch (Exception exc) {
            exc.printStackTrace();
            request.put(ARG_MESSAGE,
                        getRepository().translate(request,
                            "Error finding children") + ":"
                                + exc.getMessage());
        }

        if (doLatest) {
            if (entries.size() > 0) {
                entries = getEntryUtil().sortEntriesOnDate(entries, true);

                return outputHandler.outputEntry(request, outputType,
                        entries.get(0));
            }
        }

        group.setSubEntries(entries);
        group.setSubGroups(subGroups);

        OutputType dfltOutputType = getDefaultOutputType(request, group,
                                        subGroups, entries);
        if (dfltOutputType != null) {
            outputType    = dfltOutputType;
            outputHandler = getRepository().getOutputHandler(outputType);
        }
        Result result = outputHandler.outputGroup(request, outputType, group,
                            subGroups, entries);

        return result;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param subFolders _more_
     * @param subEntries _more_
     *
     * @return _more_
     */
    private OutputType getDefaultOutputType(Request request, Entry entry,
                                            List<Entry> subFolders,
                                            List<Entry> subEntries) {
        if ( !request.defined(ARG_OUTPUT)) {
            for (PageDecorator pageDecorator :
                    repository.getPluginManager().getPageDecorators()) {
                String defaultOutput =
                    pageDecorator.getDefaultOutputType(getRepository(),
                        request, entry, subFolders, subEntries);
                if (defaultOutput != null) {
                    OutputType outputType =
                        getRepository().findOutputType(defaultOutput);
                    request.put(ARG_OUTPUT, defaultOutput);

                    return outputType;
                }
            }
        }

        return null;
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
    public Result processEntryAccess(Request request) throws Exception {
        Entry entry = getEntry(request);

        return entry.getTypeHandler().processEntryAccess(request, entry);
    }


    /** _more_ */
    public static final String ARG_EXTEDIT_EDIT = "extedit.edit";

    /** _more_ */
    public static final String ARG_EXTEDIT_SPATIAL = "extedit.spatial";

    /** _more_ */
    public static final String ARG_EXTEDIT_TEMPORAL = "extedit.temporal";

    /** _more_ */
    public static final String ARG_EXTEDIT_MD5 = "extedit.md5";

    /** _more_ */
    public static final String ARG_EXTEDIT_REPORT = "extedit.report";

    /** _more_ */
    public static final String ARG_EXTEDIT_SETPARENTID =
        "extedit.setparentid";

    /** _more_ */
    public static final String ARG_EXTEDIT_NEWTYPE = "extedit.newtype";

    /** _more_ */
    public static final String ARG_EXTEDIT_RECURSE = "extedit.recurse";

    /** _more_ */
    public static final String ARG_EXTEDIT_CHANGETYPE = "extedit.changetype";


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processEntryExtEdit(final Request request)
            throws Exception {

        Entry              entry        = getEntry(request);
        final Entry        finalEntry   = entry;
        final boolean      recurse = request.get(ARG_EXTEDIT_RECURSE, false);
        final EntryManager entryManager = this;

        if (request.exists(ARG_EXTEDIT_EDIT)) {
            final boolean doMd5     = request.get(ARG_EXTEDIT_MD5, false);
            final boolean doSpatial = request.get(ARG_EXTEDIT_SPATIAL, false);
            final boolean doTemporal = request.get(ARG_EXTEDIT_TEMPORAL,
                                           false);
            ActionManager.Action action = new ActionManager.Action() {
                public void run(Object actionId) throws Exception {
                    EntryVisitor walker = new EntryVisitor(request,
                                              getRepository(), actionId) {
                        public boolean processEntry(Entry entry,
                                List<Entry> children)
                                throws Exception {
                            boolean changed = false;
                            if (doSpatial) {
                                Rectangle2D.Double rect = getBounds(children);
                                if (rect != null) {
                                    if ( !Misc.equals(rect,
                                            entry.getBounds())) {
                                        entry.setBounds(rect);
                                        changed = true;
                                    }
                                }
                            }
                            if (doTemporal) {
                                if (setTimeFromChildren(getRequest(), entry,
                                        children)) {
                                    changed = true;
                                }
                            }
                            if (changed) {
                                incrementProcessedCnt(1);
                                append(getPageHandler().getConfirmBreadCrumbs(
                                    getRequest(), entry));
                                append(HtmlUtils.br());
                                updateEntry(getRequest(), entry);
                            }

                            return true;
                        }
                    };
                    walker.walk(finalEntry);
                    getActionManager().setContinueHtml(actionId,
                            walker.getMessageBuffer().toString());
                }
            };

            return getActionManager().doAction(request, action,
                    "Walking the tree", "", entry);

        }


        /*
        if(request.exists(ARG_EXTEDIT_MD5)) {
            ActionManager.Action action = new ActionManager.Action() {
                public void run(Object actionId) throws Exception {
                    StringBuffer sb = new StringBuffer();
                    setMD5(request, actionId, sb, recurse, entry.getId(), new int[]{0}, new int[]{0});
                    if(sb.length()==0) sb.append("No checksums set");
                    getActionManager().setContinueHtml(actionId,
                                                       sb.toString());
                }
            };
            return getActionManager().doAction(request, action,
                                               "Setting MD5 Checksum", "", entry);

        }

        */

        StringBuffer sb = new StringBuffer();

        if (request.exists(ARG_EXTEDIT_CHANGETYPE)) {
            String newType = request.getString(ARG_EXTEDIT_NEWTYPE, "");
            TypeHandler newTypeHandler =
                getRepository().getTypeHandler(newType);
            entry = changeType(request, entry, newTypeHandler);
            sb.append(
                getPageHandler().showDialogNote(
                    msg("Entry type has been changed")));
        }


        /*
        if(request.exists(ARG_EXTEDIT_SETPARENTID)) {
            ActionManager.Action action = new ActionManager.Action() {
                public void run(Object actionId) throws Exception {
                    StringBuffer sb = new StringBuffer();
                    setParentId(request, actionId, sb, recurse, entry.getId(), new int[]{0}, new int[]{0});
                    getActionManager().setContinueHtml(actionId,
                                                       sb.toString());
                }
            };
            return getActionManager().doAction(request, action,
                                               "Setting parent ids", "", entry);

        }
        */

        if (request.exists(ARG_EXTEDIT_REPORT)) {
            final long[] size     = { 0 };
            final int[]  numFiles = { 0 };
            EntryVisitor walker = new EntryVisitor(request, getRepository(),
                                      null) {
                @Override
                public boolean processEntry(Entry entry, List<Entry> children)
                        throws Exception {
                    for (Entry child : children) {
                        String url =
                            request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                             child);
                        if (child.isFileType()) {
                            boolean exists = child.getResource().fileExists();
                            append("<tr><td>");
                            append(getPageHandler().getBreadCrumbs(request,
                                    child, entry));
                            append("</td><td align=right>");
                            if (exists) {
                                File file = child.getFile();
                                size[0] += file.length();
                                numFiles[0]++;
                                append("" + file.length());
                            } else {
                                append("Missing:" + child.getResource());
                            }
                            append("</td>");
                            append("<td>");
                            if (child.getResource().isStoredFile()) {
                                append("***");
                            }
                            append("</td>");
                            append("</tr>");
                        } else if (child.isGroup()) {}
                        else {}
                    }

                    return true;
                }
            };
            walker.walk(entry);
            sb.append("<table><tr><td><b>" + msg("File") + "</b></td><td><b>"
                      + msg("Size") + "</td><td></td></tr>");
            sb.append(walker.getMessageBuffer());
            sb.append("<tr><td><b>" + msgLabel("Total")
                      + "</td><td align=right>"
                      + HtmlUtils.b(formatFileLength(size[0]))
                      + "</td></tr>");
            sb.append("</table>");
            sb.append("**** - File managed by RAMADDA");

            return makeEntryEditResult(request, entry, "Entry Report", sb);
        }



        sb.append(request.form(getRepository().URL_ENTRY_EXTEDIT,
                               HtmlUtils.attr("name", "entryform")));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));

        sb.append(msgHeader("Group Edit"));
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.labeledCheckbox(ARG_EXTEDIT_SPATIAL, "true",
                                            false, "Set spatial metadata"));
        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.labeledCheckbox(ARG_EXTEDIT_TEMPORAL, "true",
                                            false, "Set temporal metadata"));
        sb.append(HtmlUtils.p());
        //        sb.append(HtmlUtils.labeledCheckbox(ARG_EXTEDIT_MD5,"true", false, "Set MD5 checksums"));
        //        sb.append(HtmlUtils.p());
        sb.append(HtmlUtils.submit(msg("Apply"), ARG_EXTEDIT_EDIT));
        sb.append(HtmlUtils.labeledCheckbox(ARG_EXTEDIT_RECURSE, "true",
                                            true, "Recurse"));

        sb.append(HtmlUtils.p());
        sb.append(msgHeader("Generate File Listing"));
        sb.append(HtmlUtils.submit(msg("Generate File Listing"),
                                   ARG_EXTEDIT_REPORT));


        //For now only support changing types for folders
        if (entry.isGroup()) {
            List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
            for (TypeHandler typeHandler :
                    getRepository().getTypeHandlers()) {
                if ( !typeHandler.isGroup()) {
                    continue;
                }
                if (typeHandler.equals(entry.getTypeHandler())) {
                    continue;
                }
                if ( !entry.getTypeHandler().canChangeTo(typeHandler)) {
                    continue;
                }
                tfos.add(
                    new TwoFacedObject(
                        typeHandler.getCategory() + " - "
                        + typeHandler.getLabel(), typeHandler.getType()));
            }
            TwoFacedObject.sort(tfos);
            sb.append(HtmlUtils.p());
            sb.append(msgHeader("Change Type"));
            sb.append(msgLabel("Change type to"));
            sb.append(HtmlUtils.space(1));
            sb.append(HtmlUtils.select(ARG_EXTEDIT_NEWTYPE, tfos));
            sb.append(HtmlUtils.p());
            List<Column> columns = entry.getTypeHandler().getColumns();
            if ((columns != null) && (columns.size() > 0)) {
                StringBuffer note = new StringBuffer();
                for (Column col : columns) {
                    if (note.length() > 0) {
                        note.append(", ");
                    }
                    note.append(col.getLabel());
                }
                sb.append(msgLabel("Note: this metadata would be lost")
                          + note);
            }



            sb.append(HtmlUtils.p());
            sb.append(HtmlUtils.submit(msg("Change Type"),
                                       ARG_EXTEDIT_CHANGETYPE));
        }


        //        sb.append(HtmlUtils.submit(msg("Set Parent ID"),ARG_EXTEDIT_SETPARENTID));
        sb.append(HtmlUtils.formClose());

        return makeEntryEditResult(request, entry, "Entry Walk", sb);

    }


    /*
    private void setMD5(Request request, StringBuffer sb, boolean recurse, String entryId, int []totalCnt, int[] setCnt) throws Exception {
        if(!getRepository().getActionManager().getActionOk(actionId)) {
            return;
        }
        Statement stmt = getDatabaseManager().select(SqlUtil.comma(new String[]{Tables.ENTRIES.COL_ID,
                                                                                Tables.ENTRIES.COL_TYPE,
                                                                                Tables.ENTRIES.COL_MD5,
                                                                                Tables.ENTRIES.COL_RESOURCE}),
            Tables.ENTRIES.NAME,
            Clause.eq(Tables.ENTRIES.COL_PARENT_GROUP_ID, entryId));
        SqlUtil.Iterator iter = getDatabaseManager().getIterator(stmt);
        ResultSet        results;

        while ((results = iter.getNext()) != null) {
            totalCnt[0]++;
            int col = 1;
            String id = results.getString(col++);
            String type= results.getString(col++);
            String md5 = results.getString(col++);
            String resource = results.getString(col++);
            if(new File(resource).exists() && !Utils.stringDefined(md5)) {
                setCnt[0]++;
                Entry entry = getEntry(request, id);
                if(!getAccessManager().canDoAction(request, entry,
                                                   Permission.ACTION_EDIT)) {
                    continue;
                }
                md5 = ucar.unidata.util.IOUtil.getMd5(resource);
                getDatabaseManager().update(Tables.ENTRIES.NAME,
                                            Tables.ENTRIES.COL_ID,
                                            id, new String[]{Tables.ENTRIES.COL_MD5},
                                            new String[]{md5});
                sb.append(getPageHandler().getConfirmBreadCrumbs(request, entry));
                sb.append(HtmlUtils.br());
            }
            getActionManager().setActionMessage(actionId,
                                                "Checked " + totalCnt[0] +" entries<br>Changed " + setCnt[0] +" entries");

            if(recurse) {
                TypeHandler typeHandler = getRepository().getTypeHandler(type);
                if(typeHandler.isGroup()) {
                    setMD5(request, actionId,  sb, recurse, id, totalCnt, setCnt);
                }
            }
        }
        getDatabaseManager().closeStatement(stmt);
    }
    */



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param newTypeHandler _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Entry changeType(Request request, Entry entry,
                             TypeHandler newTypeHandler)
            throws Exception {
        if ( !getAccessManager().canDoAction(request, entry,
                                             Permission.ACTION_EDIT)) {
            throw new AccessException("Cannot edit:" + entry.getLabel(),
                                      request);
        }

        Connection connection = getDatabaseManager().getConnection();
        try {
            Statement extraStmt = connection.createStatement();
            entry.getTypeHandler().deleteEntry(request, extraStmt,
                    entry.getId());
        } finally {
            getDatabaseManager().closeConnection(connection);
        }

        getDatabaseManager().update(Tables.ENTRIES.NAME,
                                    Tables.ENTRIES.COL_ID, entry.getId(),
                                    new String[] { Tables.ENTRIES.COL_TYPE },
                                    new String[] {
                                        newTypeHandler.getType() });

        entry.setTypeHandler(newTypeHandler);
        removeFromCache(entry);

        return getEntry(request, entry.getId());
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
    public Result processEntryForm(Request request) throws Exception {
        Entry entry = null;

        if (request.defined(ARG_ENTRYID)) {
            entry = getEntry(request);
        }
        StringBuffer sb    = new StringBuffer();
        Entry        group = addEntryForm(request, entry, sb);
        if (entry == null) {
            return addEntryHeader(request, group,
                                  new Result(msg("Add Entry"), sb));
        }

        return makeEntryEditResult(request, entry, msg("Edit Entry"), sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry addEntryForm(Request request, Entry entry, StringBuffer sb)
            throws Exception {

        String type  = null;
        Entry  group = null;

        if (entry != null) {
            type = entry.getTypeHandler().getType();
            if ( !entry.isTopEntry()) {
                group = findGroup(request, entry.getParentEntryId());
            }
        }

        boolean isEntryTop = ((entry != null) && entry.isTopEntry());
        if ( !isEntryTop && (group == null)) {
            group = findGroup(request);
        }


        if (type == null) {
            type = request.getString(ARG_TYPE, (String) null);
        }

        if (type != null) {
            if ( !type.equals(TYPE_FILE) && !type.equals(TYPE_GROUP)) {
                List<String> pastTypes =
                    (List<String>) getSessionManager().getSessionProperty(
                        request, ARG_TYPE);
                if (pastTypes == null) {
                    pastTypes = new ArrayList<String>();
                    getSessionManager().putSessionProperty(request, ARG_TYPE,
                            pastTypes);
                }
                pastTypes.remove(type);
                pastTypes.add(0, type);
                //cap it at 3 types
                if (pastTypes.size() > 3) {
                    pastTypes.remove(3);
                }
            }
        }


        if ((entry != null) && entry.getIsLocalFile()) {
            sb.append(msg("This is a local file and cannot be edited"));

            return group;
        }


        String formId = HtmlUtils.getUniqueId("entryform_");
        if (type == null) {
            sb.append(request.form(getRepository().URL_ENTRY_FORM,
                                   HtmlUtils.attr("name", "entryform")
                                   + HtmlUtils.id(formId)));
        } else {
            request.uploadFormWithAuthToken(
                sb, getRepository().URL_ENTRY_CHANGE,
                HtmlUtils.attr("name", "entryform") + HtmlUtils.id(formId));
        }


        sb.append(HtmlUtils.formTable());
        String title = BLANK;

        if (type == null) {
            sb.append(
                HtmlUtils.formEntry(
                    msgLabel("Type"),
                    getRepository().makeTypeSelect(
                        request, false, "", true, null)));

            sb.append(
                HtmlUtils.formEntry(
                    BLANK, HtmlUtils.submit(msg("Select Type to Add"))));
            sb.append(HtmlUtils.hidden(ARG_GROUP, group.getId()));
        } else {
            TypeHandler typeHandler = ((entry == null)
                                       ? getRepository().getTypeHandler(type)
                                       : entry.getTypeHandler());

            title = ((entry == null)
                     ? msg("Add Entry")
                     : msg("Edit Entry"));
            String submitButton = HtmlUtils.submit((entry == null)
                    ? "Add " + typeHandler.getLabel()
                    : msg("Save"), ARG_SUBMIT,
                                   makeButtonSubmitDialog(sb, ((entry == null)
                    ? msg("Creating Entry...")
                    : msg("Changing Entry..."))));

            String nextButton = ((entry == null)
                                 ? ""
                                 : HtmlUtils.submit("Save & Next",
                                     ARG_SAVENEXT));


            String deleteButton = (((entry != null) && entry.isTopEntry())
                                   ? ""
                                   : HtmlUtils.submit(msg("Delete"),
                                       ARG_DELETE,
                                       makeButtonSubmitDialog(sb,
                                           "Deleting Entry...")));



            String cancelButton = HtmlUtils.submit(msg("Cancel"), ARG_CANCEL);
            String buttons      = ((entry != null)
                                   ? HtmlUtils.buttons(submitButton,
                                       deleteButton, cancelButton)
                                   : HtmlUtils.buttons(submitButton,
                                       cancelButton));


            sb.append(HtmlUtils.row(HtmlUtils.colspan(buttons, 2)));
            if (entry != null) {
                sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
                sb.append(HtmlUtils.hidden(ARG_ENTRY_TIMESTAMP,
                                           getEntryTimestamp(entry)));
                if (isAnonymousUpload(entry)) {
                    List<Metadata> metadataList =
                        getMetadataManager().findMetadata(request, entry,
                            AdminMetadataHandler.TYPE_ANONYMOUS_UPLOAD,
                            false);
                    String extra = "";

                    if (metadataList != null) {
                        Metadata metadata = metadataList.get(0);
                        String   user     = metadata.getAttr1();
                        String   email    = metadata.getAttr4();
                        if (email == null) {
                            email = "";
                        }
                        extra = "<br><b>From user:</b> "
                                + metadata.getAttr1() + " <b>Email:</b> "
                                + email + " <b>IP:</b> "
                                + metadata.getAttr2();
                    }
                    String msg = HtmlUtils.space(2) + msg("Make public?")
                                 + extra;
                    sb.append(HtmlUtils.formEntry(msgLabel("Publish"),
                            HtmlUtils.checkbox(ARG_PUBLISH, "true", false)
                            + msg));
                }
            } else {
                sb.append(HtmlUtils.hidden(ARG_TYPE, type));
                sb.append(HtmlUtils.hidden(ARG_GROUP, group.getId()));
            }

            FormInfo formInfo = new FormInfo();
            typeHandler.addToEntryForm(request, sb, group, entry, formInfo);

            StringBuffer validateJavascript = new StringBuffer("");
            formInfo.addJavascriptValidation(validateJavascript);
            String script = JQuery.ready(JQuery.submit(JQuery.id(formId),
                                validateJavascript.toString()));
            sb.append(HtmlUtils.script(script));
            sb.append(HtmlUtils.row(HtmlUtils.colspan(buttons, 2)));

        }
        sb.append(HtmlUtils.formTableClose());

        return group;

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
    public List<Entry> getSessionFolders(Request request) throws Exception {
        List<String> list =
            (List<String>) getSessionManager().getSessionProperty(request,
                SESSION_FOLDERS);
        if (list == null) {
            list = new ArrayList<String>();
        }
        List<Entry> entries = new ArrayList<Entry>();
        for (String id : list) {
            Entry entry = getEntry(request, id);
            if (entry != null) {
                entries.add(entry);
            }
        }

        return entries;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void addSessionFolder(Request request, Entry entry)
            throws Exception {
        if (request.isAnonymous()) {
            return;
        }
        if (entry.isGroup()) {
            List<String> list =
                (List<String>) getSessionManager().getSessionProperty(
                    request, SESSION_FOLDERS);
            if (list == null) {
                list = new ArrayList<String>();
            }
            list.remove(entry.getId());
            list.add(0, entry.getId());
            //Cap the size at 5
            if (list.size() > 5) {
                list.remove(list.size() - 1);
            }
            getSessionManager().putSessionProperty(request, SESSION_FOLDERS,
                    list);
        }
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    private String getEntryTimestamp(Entry entry) {
        long changeDate = entry.getChangeDate();

        //        System.err.println("timestamp:" +changeDate +" " + new Date(changeDate));
        return "" + changeDate;
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
    public Result processEntryChange(final Request request) throws Exception {
        boolean download = request.get(ARG_RESOURCE_DOWNLOAD, false);
        request.ensureAuthToken();
        if (download) {
            ActionManager.Action action = new ActionManager.Action() {
                public void run(Object actionId) throws Exception {
                    Result result = doProcessEntryChange(request, false,
                                        actionId);
                    getActionManager().setContinueHtml(actionId,
                            HtmlUtils.href(result.getRedirectUrl(),
                                           msg("Continue")));
                }
            };

            return getActionManager().doAction(request, action,
                    "Downloading file", "");

        }

        return doProcessEntryChange(request, false, null);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String getEntryListName(Request request, Entry entry) {
        return entry.getTypeHandler().getEntryListName(request, entry);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param parent _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean canAddTo(Request request, Entry parent) throws Exception {
        return getRepository().getAccessManager().canDoAction(request,
                parent, Permission.ACTION_NEW);

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param typeHandler _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean canBeCreatedBy(Request request, TypeHandler typeHandler)
            throws Exception {
        if ( !typeHandler.canBeCreatedBy(request)) {
            return false;
        }

        String ips = getRepository().getProperty("ramadda.type."
                         + typeHandler.getType() + ".ips", null);
        if (ips != null) {
            String  requestIp = request.getIp();
            boolean ok        = false;
            for (String ip : StringUtil.split(ips, ";", true, true)) {
                if (requestIp.startsWith(ip)) {
                    ok = true;

                    break;
                }
            }
            if ( !ok) {
                return false;
            }
        }

        return true;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param forUpload _more_
     * @param actionId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result doProcessEntryChange(Request request, boolean forUpload,
                                        Object actionId)
            throws Exception {


        User user = request.getUser();
        if (forUpload) {
            logInfo("upload doProcessEntryChange user = " + user);
        }

        Entry       entry       = null;
        TypeHandler typeHandler = null;
        boolean     newEntry    = true;
        if (request.defined(ARG_ENTRYID)) {
            entry = getEntry(request);
            if (entry == null) {
                fatalError(request, "Cannot find entry");
            }
            if (forUpload) {
                fatalError(request, "Cannot edit when doing an upload");
            }
            if ( !getAccessManager().canDoAction(request, entry,
                    Permission.ACTION_EDIT)) {
                throw new AccessException("Cannot edit:" + entry.getLabel(),
                                          request);
            }
            if (entry.getIsLocalFile()) {
                return new Result(
                    request.entryUrl(
                        getRepository().URL_ENTRY_SHOW, entry, ARG_MESSAGE,
                        getRepository().translate(
                            request, "Cannot edit local files")));

            }


            //Remove this entry from the memory cache 
            //so edits don't show up for others
            removeFromCache(entry);

            typeHandler = entry.getTypeHandler();
            newEntry    = false;



            if (request.exists(ARG_CANCEL)) {
                return new Result(
                    request.entryUrl(getRepository().URL_ENTRY_SHOW, entry));
            }

            if ((entry != null) && isAnonymousUpload(entry)) {
                if (request.get(ARG_JUSTPUBLISH, false)) {
                    publishAnonymousEntry(request, entry);
                    List<Entry> entries = new ArrayList<Entry>();
                    entries.add(entry);
                    insertEntries(entries, newEntry);

                    return new Result(
                        request.entryUrl(
                            getRepository().URL_ENTRY_FORM, entry));
                }
            }



            //If we have a timestamp then check if the user 
            //was editing an up to date entry
            if (request.defined(ARG_ENTRY_TIMESTAMP)) {
                String formTimestamp = request.getString(ARG_ENTRY_TIMESTAMP,
                                           "0");
                String currentTimestamp = getEntryTimestamp(entry);
                if ( !Misc.equals(formTimestamp, currentTimestamp)) {
                    StringBuffer sb        = new StringBuffer();
                    String       dateRange = "";
                    try {
                        dateRange = new Date(formTimestamp) + ":"
                                    + new Date(currentTimestamp);
                    } catch (Exception ignore) {}
                    sb.append(
                        getPageHandler().showDialogError(
                            msg(
                            "Error: The entry you are editing has been edited since the time you began the edit:"
                            + dateRange)));

                    return addEntryHeader(request, entry,
                                          new Result(msg("Entry Edit Error"),
                                              sb));
                }
            }

            if (request.exists(ARG_DELETE_CONFIRM)) {
                if (entry.isTopEntry()) {
                    return new Result(
                        request.entryUrl(
                            getRepository().URL_ENTRY_SHOW, entry,
                            ARG_MESSAGE,
                            getRepository().translate(
                                request, "Cannot delete top-level folder")));
                }


                deleteEntry(request, entry);
                Entry group = findGroup(request, entry.getParentEntryId());

                return new Result(
                    request.entryUrl(
                        getRepository().URL_ENTRY_SHOW, group, ARG_MESSAGE,
                        getRepository().translate(
                            request, "Entry is deleted")));
            }

            if (request.exists(ARG_DELETE)) {
                return new Result(
                    request.entryUrl(
                        getRepository().URL_ENTRY_DELETE, entry));
            }
        } else if (forUpload) {
            typeHandler =
                getRepository().getTypeHandler(TypeHandler.TYPE_CONTRIBUTION);
        } else {
            typeHandler =
                getRepository().getTypeHandler(request.getString(ARG_TYPE,
                    TypeHandler.TYPE_ANY));
        }

        boolean     figureOutType = request.get(ARG_TYPE_GUESS, false);

        List<Entry> entries       = new ArrayList<Entry>();
        String      category      = "";
        if (request.defined(ARG_CATEGORY)) {
            category = request.getString(ARG_CATEGORY, "");
        } else {
            category = request.getString(ARG_CATEGORY_SELECT, "");
        }


        File serverFile = null;
        if (request.defined(ARG_SERVERFILE)) {
            //IMPORTANT:
            request.ensureAdmin();
            serverFile = new File(request.getString(ARG_SERVERFILE,
                    (String) null));
            getStorageManager().checkLocalFile(serverFile);
        }

        if (entry == null) {
            if (forUpload) {
                logInfo("Upload:creating a new entry");
            }
            String groupId = request.getString(ARG_GROUP, (String) null);
            if (groupId == null) {
                fatalError(request, "You must specify a parent folder");
            }
            Entry parentEntry = findGroup(request);
            if (forUpload) {
                logInfo("Upload:checking access");
            }
            boolean okToCreateNewEntry =
                getAccessManager().canDoAction(request, parentEntry,
                    (forUpload
                     ? Permission.ACTION_UPLOAD
                     : Permission.ACTION_NEW), forUpload);

            if (forUpload) {
                logInfo("Upload:is ok to create:" + okToCreateNewEntry);
            }
            if ( !okToCreateNewEntry) {
                throw new AccessException("Cannot add:" + entry.getLabel(),
                                          request);
            }


            List<String> resources = new ArrayList<String>();
            List<Entry>  parents   = new ArrayList<Entry>();
            List<String> origNames = new ArrayList<String>();


            String       resource  = "";
            String urlArgument = request.getAnonymousEncodedString(ARG_URL,
                                     BLANK);
            String  filename     = typeHandler.getUploadedFile(request);
            boolean unzipArchive = false;

            boolean isFile       = false;
            String  resourceName = request.getString(ARG_FILE, BLANK);

            if (serverFile != null) {
                filename = serverFile.toString();
            }


            if (resourceName.length() == 0) {
                resourceName = IOUtil.getFileTail(resource);
            }

            unzipArchive = (forUpload
                            ? false
                            : request.get(ARG_FILE_UNZIP, false));


            if (serverFile != null) {
                isFile   = true;
                resource = filename;
                if (forUpload) {
                    fatalError(request, "No filename specified");
                }
            } else if (filename != null) {
                //A File was uploaded
                isFile   = true;
                resource = filename;
            } else {
                if (forUpload) {
                    fatalError(request, "No filename specified");
                }

                //A URL was selected
                resource = urlArgument.trim();
                if ( !request.get(ARG_RESOURCE_DOWNLOAD, false)) {
                    unzipArchive = false;
                } else {
                    String url = resource;
                    if ( !url.toLowerCase().startsWith("http:")
                            && !url.toLowerCase().startsWith("https:")
                            && !url.toLowerCase().startsWith("ftp:")) {
                        fatalError(request, "Cannot download url:" + url);
                    }
                    getStorageManager().checkPath(url);
                    isFile = true;
                    String tail = IOUtil.getFileTail(resource);
                    File newFile = getStorageManager().getTmpFile(request,
                                       tail);
                    resourceName = tail;
                    resource     = newFile.toString();
                    URL           fromUrl    = new URL(url);
                    URLConnection connection = fromUrl.openConnection();
                    InputStream   fromStream = connection.getInputStream();
                    if (actionId != null) {
                        JobManager.getManager().startLoad("File copy",
                                actionId);
                    }
                    int length = connection.getContentLength();
                    if (length > 0 & actionId != null) {
                        getActionManager().setActionMessage(actionId,
                                msg("Downloading") + " " + length + " "
                                + msg("bytes"));
                    }
                    OutputStream toStream =
                        getStorageManager().getFileOutputStream(newFile);
                    try {
                        int bytes = IOUtil.writeTo(fromStream, toStream,
                                        actionId, length);
                        if (bytes < 0) {
                            return new Result(
                                request.entryUrl(
                                    getRepository().URL_ENTRY_SHOW,
                                    parentEntry));
                        }
                    } finally {
                        IOUtil.close(toStream);
                        IOUtil.close(fromStream);
                    }
                }
            }

            boolean isGzip = resource.endsWith(".gz");

            // check if it's a zp file
            if (unzipArchive && !resource.toLowerCase().endsWith(".zip")) {
                unzipArchive = false;
            }

            /*
            if (unzipArchive && !resource.toLowerCase().endsWith(".zip")) {
                if (!isGzip) {
                    unzipArchive = false;
                }
            }
            if (isGzip && unzipArchive) {
                //TODO: use GZIPInputStream to unzip the file
            }
            */

            boolean hasZip = false;

            if (serverFile != null) {
                if ( !serverFile.exists()) {
                    StringBuffer message =
                        new StringBuffer(
                            getPageHandler().showDialogError(
                                msg("File does not exist")));

                    return addEntryHeader(request, parentEntry,
                                          new Result("", message));
                }

                if (serverFile.isDirectory()) {
                    String pattern =
                        request.getString(ARG_SERVERFILE_PATTERN, null);
                    if (pattern.length() == 0) {
                        pattern = null;
                    }
                    if (pattern != null) {
                        pattern = StringUtil.wildcardToRegexp(pattern);
                    }
                    //TODO for Don - Walk the tree
                    final String thePattern = pattern;
                    File[] files = serverFile.listFiles(new FileFilter() {
                        public boolean accept(File f) {
                            if (thePattern == null) {
                                return true;
                            }
                            String name = f.getName();
                            if (name.matches(thePattern)) {
                                return true;
                            }

                            return false;
                        }
                    });
                    int fileCnt = 0;
                    for (File f : files) {
                        if (f.isFile()) {
                            resources.add(f.toString());
                            origNames.add(f.toString());
                            parents.add(parentEntry);
                            fileCnt++;
                        }
                    }
                    if (fileCnt == 0) {
                        StringBuffer message =
                            new StringBuffer(
                                getPageHandler().showDialogError(
                                    msg("No files found matching pattern")));

                        return addEntryHeader(request, parentEntry,
                                new Result("", message));
                    }
                } else {
                    resources.add(serverFile.toString());
                    origNames.add(resourceName);
                    parents.add(parentEntry);
                }
            } else if ( !unzipArchive) {
                resources.add(resource);
                origNames.add(resourceName);
                parents.add(parentEntry);
            } else {
                hasZip = true;
                Hashtable<String, Entry> nameToGroup = new Hashtable<String,
                                                           Entry>();
                InputStream fis =
                    getStorageManager().getFileInputStream(resource);
                OutputStream   fos = null;
                ZipInputStream zin = new ZipInputStream(fis);
                ZipEntry       ze  = null;
                try {
                    while ((ze = zin.getNextEntry()) != null) {
                        if (ze.isDirectory()) {
                            continue;
                        }
                        String path = ze.getName();
                        String name = IOUtil.getFileTail(path);
                        if (name.equals("MANIFEST.MF")) {
                            continue;
                        }
                        //Skip dot files as well
                        if (name.startsWith(".")) {
                            continue;
                        }
                        Entry parent = parentEntry;
                        if (request.get(ARG_FILE_PRESERVEDIRECTORY, false)) {
                            List<String> toks = StringUtil.split(path, "/",
                                                    true, true);
                            String ancestors = "";
                            //Remove the file name from the list of tokens
                            if (toks.size() > 0) {
                                toks.remove(toks.size() - 1);
                            }
                            for (String parentName : toks) {
                                parentName = parentName.replaceAll("_", " ");
                                ancestors  = ancestors + "/" + parentName;
                                Entry group = nameToGroup.get(ancestors);
                                if (group == null) {
                                    Request tmpRequest =
                                        getRepository().getTmpRequest();
                                    tmpRequest.setUser(user);
                                    group = findGroupUnder(tmpRequest,
                                            parent, parentName, user);
                                    nameToGroup.put(ancestors, group);
                                }
                                parent = group;
                            }
                        }
                        File f = getStorageManager().getTmpFile(request,
                                     name);
                        fos = getStorageManager().getFileOutputStream(f);
                        try {
                            IOUtil.writeTo(zin, fos);
                        } finally {
                            IOUtil.close(fos);
                        }
                        parents.add(parent);
                        resources.add(f.toString());
                        origNames.add(name);
                    }
                } finally {
                    IOUtil.close(fis);
                    IOUtil.close(zin);
                }
            }

            if (request.exists(ARG_CANCEL)) {
                return new Result(
                    request.entryUrl(
                        getRepository().URL_ENTRY_SHOW, parentEntry));
            }

            String description =
                request.getAnonymousEncodedString(ARG_DESCRIPTION, BLANK);

            Date createDate = new Date();
            Date[] dateRange = request.getDateRange(ARG_FROMDATE, ARG_TODATE,
                                   createDate);


            File originalFile = null;

            for (int resourceIdx = 0; resourceIdx < resources.size();
                    resourceIdx++) {
                Entry parent = parents.get(resourceIdx);
                resourceName = (String) resources.get(resourceIdx);
                String theResource = (String) resources.get(resourceIdx);
                String origName    = (String) origNames.get(resourceIdx);
                if (isFile && (serverFile == null)) {
                    if (forUpload) {
                        theResource =
                            getStorageManager().moveToAnonymousStorage(
                                request, new File(theResource),
                                "").toString();
                    } else {
                        theResource =
                            getStorageManager().moveToStorage(request,
                                originalFile =
                                    new File(theResource)).toString();
                    }
                }

                //If its an anon upload  or we're unzipping an archive then don't set the name
                String name = ((forUpload || hasZip)
                               ? ""
                               : request.getAnonymousEncodedString(ARG_NAME,
                                   BLANK));



                if (name.indexOf("${") >= 0) {}

                if (name.trim().length() == 0) {



                    name = IOUtil.getFileTail(origName);
                }


                Date[] theDateRange = { dateRange[0], dateRange[1] };

                if (request.defined(ARG_DATE_PATTERN)) {
                    String format = request.getUnsafeString(ARG_DATE_PATTERN,
                                        BLANK);
                    String pattern = format;
                    //swap out any of the date tokens with a decimal regexp
                    for (String s : new String[] {
                        "y", "m", "M", "d", "H", "m"
                    }) {
                        pattern = pattern.replaceAll(s, "_DIGIT_");
                    }
                    pattern = pattern.replaceAll("_DIGIT_", "\\\\d");
                    pattern = ".*(" + pattern + ").*";
                    //                    System.err.println("Pattern:" + pattern + " " + origName);
                    Matcher matcher =
                        Pattern.compile(pattern).matcher(origName);
                    if (matcher.find()) {
                        String dateString = matcher.group(1);
                        Date dttm = RepositoryUtil.makeDateFormat(
                                        format).parse(dateString);
                        theDateRange[0] = dttm;
                        theDateRange[1] = dttm;
                    } else {
                        System.err.println("no match");
                    }
                }

                String id           = getRepository().getGUID();
                String resourceType = Resource.TYPE_UNKNOWN;
                if (serverFile != null) {
                    resourceType = Resource.TYPE_LOCAL_FILE;
                } else if (isFile) {
                    resourceType = Resource.TYPE_STOREDFILE;
                } else {
                    try {
                        new URL(theResource);
                        resourceType = Resource.TYPE_URL;
                    } catch (Exception exc) {}
                }

                TypeHandler typeHandlerToUse = typeHandler;
                //See if we can figure out the type 
                if (figureOutType) {
                    TypeHandler tmp = findDefaultTypeHandler(theResource);
                    if (tmp != null) {
                        typeHandlerToUse = tmp;
                    }
                }



                if ( !canBeCreatedBy(request, typeHandlerToUse)) {
                    fatalError(request,
                               "Cannot create an entry of type "
                               + typeHandlerToUse.getDescription());
                }

                if (name.trim().length() == 0) {
                    name = typeHandlerToUse.getDefaultEntryName(resourceName);
                }
                entry = typeHandlerToUse.createEntry(id);




                if (theDateRange[0] == null) {
                    theDateRange[0] = theDateRange[1] =
                        Utils.extractDate(theResource);
                }

                //                System.err.println("date:" + theDateRange[0] + " " + theResource);

                if (theDateRange[0] == null) {
                    theDateRange[0] = ((theDateRange[1] == null)
                                       ? createDate
                                       : theDateRange[1]);
                }
                if (theDateRange[1] == null) {
                    theDateRange[1] = theDateRange[0];
                }



                entry.initEntry(name, description, parent, request.getUser(),
                                new Resource(theResource, resourceType),
                                category, createDate.getTime(),
                                createDate.getTime(),
                                theDateRange[0].getTime(),
                                theDateRange[1].getTime(), null);
                if (forUpload) {
                    initUploadedEntry(request, entry, parent);
                }

                setEntryState(request, entry, parent, newEntry);
                entries.add(entry);
            }
        } else {
            boolean fileUpload      = false;
            String  newResourceName = request.getUploadedFile(ARG_FILE);
            String  newResourceType = null;

            //TODO: If they select a URL to download we don't handle that now

            //Did they upload a new file???
            if (newResourceName != null) {
                newResourceName = getStorageManager().moveToStorage(request,
                        new File(newResourceName)).toString();
                newResourceType = Resource.TYPE_STOREDFILE;
            } else if (serverFile != null) {
                newResourceName = serverFile.toString();
                newResourceType = Resource.TYPE_LOCAL_FILE;
            } else if (request.defined(ARG_URL)) {
                newResourceName = request.getAnonymousEncodedString(ARG_URL,
                        null);
                newResourceType = Resource.TYPE_URL;
            }

            if (newResourceName != null) {
                //If it was a stored file then remove the old one
                if (entry.getResource().isStoredFile()) {
                    getStorageManager().removeFile(entry.getResource());
                }
                entry.setResource(new Resource(newResourceName,
                        newResourceType));
            }

            if (entry.isTopEntry()) {
                //fatalError(request,"Cannot edit top-level folder");
            }
            Date[] dateRange = request.getDateRange(ARG_FROMDATE, ARG_TODATE,
                                   new Date());
            String newName = request.getString(ARG_NAME, entry.getLabel());

            entry.setName(newName);
            String tmp = request.getString(ARG_DESCRIPTION,
                                           entry.getDescription());

            entry.setDescription(request.getString(ARG_DESCRIPTION,
                    entry.getDescription()));

            if (isAnonymousUpload(entry)) {
                if (request.get(ARG_PUBLISH, false)) {
                    publishAnonymousEntry(request, entry);
                }
            } else {
                entry.setCategory(category);
            }


            if (request.defined(ARG_URL)) {
                entry.setResource(new Resource(request.getString(ARG_URL,
                        BLANK), Resource.TYPE_URL,
                                request.getString(ARG_MD5, null),
                                request.get(ARG_FILESIZE, (long) -1)));
            }


            if (dateRange[0] != null) {
                entry.setStartDate(dateRange[0].getTime());
            }
            if (dateRange[1] == null) {
                dateRange[1] = dateRange[0];
            }

            if (dateRange[1] != null) {
                entry.setEndDate(dateRange[1].getTime());
            }
            setEntryState(request, entry, entry.getParentEntry(), newEntry);
            entries.add(entry);
        }


        if (request.getUser().getAdmin() && request.defined(ARG_USER_ID)) {

            User newUser =
                getUserManager().findUser(request.getString(ARG_USER_ID,
                    "").trim());
            if (newUser == null) {
                fatalError(request,
                           "Could not find user: "
                           + request.getString(ARG_USER_ID, ""));
            }
            for (Entry theEntry : entries) {
                theEntry.setUser(newUser);
            }
        }



        if (newEntry) {
            if (request.get(ARG_METADATA_ADD, false)) {
                addInitialMetadata(request, entries, newEntry, false);
            } else if (request.get(ARG_METADATA_ADDSHORT, false)) {
                addInitialMetadata(request, entries, newEntry, true);
            }
        }


        insertEntries(entries, newEntry);
        if (newEntry) {
            for (Entry theNewEntry : entries) {
                theNewEntry.getTypeHandler().doFinalEntryInitialization(
                    request, theNewEntry);
            }
        }



        if (forUpload) {
            entry = (Entry) entries.get(0);

            return new Result(
                request.entryUrl(
                    getRepository().URL_ENTRY_SHOW, entry.getParentEntry(),
                    ARG_MESSAGE,
                    getRepository().translate(
                        request, "File has been uploaded")));
        }

        if (entries.size() == 1) {
            entry = (Entry) entries.get(0);
            if (entry.getTypeHandler().returnToEditForm()) {
                return new Result(
                    request.entryUrl(getRepository().URL_ENTRY_FORM, entry));
            } else {
                return new Result(
                    request.entryUrl(getRepository().URL_ENTRY_SHOW, entry));
            }
        } else if (entries.size() > 1) {
            entry = (Entry) entries.get(0);

            return new Result(
                request.entryUrl(
                    getRepository().URL_ENTRY_SHOW, entry.getParentEntry(),
                    ARG_MESSAGE,
                    entries.size()
                    + HtmlUtils.pad(
                        getRepository().translate(
                            request, "files uploaded"))));
        } else {
            return new Result(BLANK,
                              new StringBuffer(msg("No entries created")));
        }

    }


    /**
     * _more_
     *
     * @param theResource _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public TypeHandler findDefaultTypeHandler(String theResource)
            throws Exception {
        File   newFile   = new File(theResource);
        String shortName = newFile.getName();
        for (TypeHandler otherTypeHandler :
                getRepository().getTypeHandlers()) {
            if (otherTypeHandler.canHandleResource(theResource.toLowerCase(),
                    shortName.toLowerCase())) {
                return otherTypeHandler;
            }
        }

        return null;
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param template _more_
     *
     * @return _more_
     */
    public String replaceMacros(Entry entry, String template) {
        Date createDate = new Date(entry.getCreateDate());
        Date fromDate   = new Date(entry.getStartDate());
        Date toDate     = new Date(entry.getEndDate());

        String url = HtmlUtils.url(getFullEntryShowUrl(null), ARG_ENTRYID,
                                   entry.getId());
        //j-
        String[] macros = {
            "entryid", entry.getId(), "parentid", entry.getParentEntryId(),
            "resourcepath", entry.getResource().getPath(), "resourcename",
            getStorageManager().getFileTail(entry.getResource().getPath()),
            "filename",
            getStorageManager().getFileTail(entry.getResource().getPath()),
            "fileextension",
            IOUtil.getFileExtension(entry.getResource().getPath()), "name",
            getEntryDisplayName(entry), "fullname", entry.getFullName(),
            "user", entry.getUser().getLabel(), "url", url
        };

        //j+
        String result = template;

        for (int i = 0; i < macros.length; i += 2) {
            String macro = "${" + macros[i] + "}";
            String value = macros[i + 1];
            result = result.replace(macro, value);
        }

        return replaceMacros(result, createDate, fromDate, toDate);
    }



    /**
     * _more_
     *
     * @param template _more_
     * @param createDate _more_
     * @param fromDate _more_
     * @param toDate _more_
     *
     * @return _more_
     */
    public String replaceMacros(String template, Date createDate,
                                Date fromDate, Date toDate) {
        GregorianCalendar fromCal =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        fromCal.setTime(fromDate);

        GregorianCalendar createCal =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        createCal.setTime(createDate);

        GregorianCalendar toCal =
            new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        toCal.setTime(toDate);


        int createDay        = createCal.get(GregorianCalendar.DAY_OF_MONTH);
        int fromDay          = fromCal.get(GregorianCalendar.DAY_OF_MONTH);
        int toDay            = toCal.get(GregorianCalendar.DAY_OF_MONTH);

        int createWeek       = createCal.get(GregorianCalendar.WEEK_OF_MONTH);
        int fromWeek         = fromCal.get(GregorianCalendar.WEEK_OF_MONTH);
        int toWeek           = toCal.get(GregorianCalendar.WEEK_OF_MONTH);

        int createWeekOfYear = createCal.get(GregorianCalendar.WEEK_OF_YEAR);
        int fromWeekOfYear   = fromCal.get(GregorianCalendar.WEEK_OF_YEAR);
        int toWeekOfYear     = toCal.get(GregorianCalendar.WEEK_OF_YEAR);


        int createMonth      = createCal.get(GregorianCalendar.MONTH) + 1;
        int fromMonth        = fromCal.get(GregorianCalendar.MONTH) + 1;
        int toMonth          = toCal.get(GregorianCalendar.MONTH) + 1;

        int createYear       = createCal.get(GregorianCalendar.YEAR);
        int fromYear         = fromCal.get(GregorianCalendar.YEAR);
        int toYear           = toCal.get(GregorianCalendar.YEAR);



        //j-
        String[] macros = {
            "day", padZero(fromDay), "week", fromWeek + "", "month",
            padZero(fromMonth), "year", fromYear + "", "date",
            getPageHandler().formatDate(fromDate), "fromdate",
            getPageHandler().formatDate(fromDate), "monthname",
            DateUtil.MONTH_NAMES[fromMonth - 1], "create_day",
            padZero(createDay), "from_day", padZero(fromDay), "to_day",
            padZero(toDay), "create_week", "" + createWeek, "from_week",
            "" + fromWeek, "to_week", "" + toWeek, "create_weekofyear",
            "" + createWeekOfYear, "from_weekofyear", "" + fromWeekOfYear,
            "to_weekofyear", "" + toWeekOfYear, "create_date",
            getPageHandler().formatDate(createDate), "from_date",
            getPageHandler().formatDate(fromDate), "to_date",
            getPageHandler().formatDate(toDate), "create_month",
            padZero(createMonth), "from_month", padZero(fromMonth),
            "to_month", padZero(toMonth), "create_year", createYear + "",
            "from_year", fromYear + "", "to_year", toYear + "",
            "create_monthname", DateUtil.MONTH_NAMES[createMonth - 1],
            "from_monthname", DateUtil.MONTH_NAMES[fromMonth - 1],
            "to_monthname", DateUtil.MONTH_NAMES[toMonth - 1]
        };

        //j+
        String result = template;

        for (int i = 0; i < macros.length; i += 2) {
            String macro = "${" + macros[i] + "}";
            String value = macros[i + 1];
            result = result.replace(macro, value);
        }

        return result;
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    private String padZero(int v) {
        return ((v < 10)
                ? "0"
                : "") + v;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parent _more_
     * @param newEntry _more_
     *
     * @throws Exception _more_
     */
    private void setEntryState(Request request, Entry entry, Entry parent,
                               boolean newEntry)
            throws Exception {
        if (request.defined(ARG_LOCATION_LATITUDE)
                && request.defined(ARG_LOCATION_LONGITUDE)) {
            entry.setLatitude(request.get(ARG_LOCATION_LATITUDE, 0.0));
            entry.setLongitude(request.get(ARG_LOCATION_LONGITUDE, 0.0));
        } else if (request.exists(ARG_AREA + "_south")) {
            boolean hasSouth = request.defined(ARG_AREA + "_south");
            boolean hasNorth = request.defined(ARG_AREA + "_north");
            boolean hasWest  = request.defined(ARG_AREA + "_west");
            boolean hasEast  = request.defined(ARG_AREA + "_east");

            if (hasNorth && hasWest && !hasSouth && !hasEast) {
                entry.setLatitude(request.getLatOrLonValue(ARG_AREA
                        + "_north", Entry.NONGEO));
                entry.setLongitude(request.getLatOrLonValue(ARG_AREA
                        + "_west", Entry.NONGEO));
            } else if (hasSouth && hasEast && !hasNorth && !hasWest) {
                entry.setLatitude(request.getLatOrLonValue(ARG_AREA
                        + "_south", Entry.NONGEO));
                entry.setLongitude(request.getLatOrLonValue(ARG_AREA
                        + "_east", Entry.NONGEO));
            } else {
                entry.setSouth(request.getLatOrLonValue(ARG_AREA + "_south",
                        Entry.NONGEO));
                entry.setNorth(request.getLatOrLonValue(ARG_AREA + "_north",
                        Entry.NONGEO));
                entry.setWest(request.getLatOrLonValue(ARG_AREA + "_west",
                        Entry.NONGEO));
                entry.setEast(request.getLatOrLonValue(ARG_AREA + "_east",
                        Entry.NONGEO));
            }
            getSessionManager().putSessionProperty(request, ARG_AREA,
                    entry.getNorth() + ";" + entry.getWest() + ";"
                    + entry.getSouth() + ";" + entry.getEast() + ";");
        }

        List<Entry> children       = null;


        double      altitudeTop    = Entry.NONGEO;
        double      altitudeBottom = Entry.NONGEO;
        if (request.defined(ARG_ALTITUDE)) {
            altitudeTop = altitudeBottom = request.get(ARG_ALTITUDE,
                    Entry.NONGEO);
        } else {
            if (request.defined(ARG_ALTITUDE_TOP)) {
                altitudeTop = request.get(ARG_ALTITUDE_TOP, Entry.NONGEO);
            }
            if (request.defined(ARG_ALTITUDE_BOTTOM)) {
                altitudeBottom = request.get(ARG_ALTITUDE_BOTTOM,
                                             Entry.NONGEO);
            }
        }
        entry.setAltitudeTop(altitudeTop);
        entry.setAltitudeBottom(altitudeBottom);

        entry.getTypeHandler().initializeEntryFromForm(request, entry,
                parent, newEntry);
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param children _more_
     *
     *
     * @return _more_
     * @throws Exception _more_
     */
    public boolean setTimeFromChildren(Request request, Entry entry,
                                       List<Entry> children)
            throws Exception {
        if (children == null) {
            children = getChildren(request, entry);
        }
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;
        for (Entry child : children) {
            minTime = Math.min(minTime, child.getStartDate());
            maxTime = Math.max(maxTime, child.getEndDate());
        }
        boolean changed = false;

        if (minTime != Long.MAX_VALUE) {
            long diffStart = minTime - entry.getStartDate();
            long diffEnd   = maxTime - entry.getEndDate();
            //We seem to lose some time resolution when we store so only assume a change
            //when the time differs by more than 5 seconds
            changed = (diffStart < -10000) || (diffStart > 10000)
                      || (diffEnd < -10000) || (diffEnd > 10000);
            entry.setStartDate(minTime);
            entry.setEndDate(maxTime);

        }

        return changed;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void setBoundsFromChildren(Request request, Entry entry)
            throws Exception {
        if (entry == null) {
            return;
        }
        Rectangle2D.Double rect = getBounds(getChildren(request, entry));
        if (rect != null) {
            entry.setBounds(rect);
            updateEntry(request, entry);
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
    public Result processEntryDelete(Request request) throws Exception {
        Entry        entry = getEntry(request);
        StringBuffer sb    = new StringBuffer();
        if (entry.isTopEntry()) {
            sb.append(
                getPageHandler().showDialogNote(
                    "Cannot delete top-level folder"));

            return makeEntryEditResult(request, entry, "Delete Entry", sb);
        }

        if (request.exists(ARG_CANCEL)) {
            return new Result(
                request.entryUrl(getRepository().URL_ENTRY_FORM, entry));
        }

        if (request.exists(ARG_DELETE_CONFIRM)) {
            request.ensureAuthToken();
            List<Entry> entries = new ArrayList<Entry>();
            entries.add(entry);
            Entry group = findGroup(request, entry.getParentEntryId());
            if (entry.isGroup()) {
                return asynchDeleteEntries(request, entries);
            } else {
                deleteEntries(request, entries, null);

                if (group == null) {
                    group = getTopGroup();
                }

                return new Result(
                    request.entryUrl(getRepository().URL_ENTRY_SHOW, group));
            }
        }

        String breadcrumbs = getPageHandler().getConfirmBreadCrumbs(request,
                                 entry);
        StringBuffer inner = new StringBuffer();
        if (entry.isGroup()) {
            inner.append(
                msg("Are you sure you want to delete the following folder?"));
            inner.append(
                HtmlUtils.div(
                    breadcrumbs,
                    HtmlUtils.cssClass("ramadda-confirm-entries")));
            inner.append(
                HtmlUtils.b(
                    msg(
                    "Note: This will also delete everything contained by this folder")));
        } else {
            inner.append(
                msg("Are you sure you want to delete the following entry?"));
            inner.append(
                HtmlUtils.div(
                    breadcrumbs,
                    HtmlUtils.cssClass("ramadda-confirm-entries")));
        }



        StringBuffer fb = new StringBuffer();
        fb.append(request.form(getRepository().URL_ENTRY_DELETE, BLANK));

        getRepository().addAuthToken(request, fb);
        fb.append(HtmlUtils.buttons(HtmlUtils.submit(msg("OK"),
                ARG_DELETE_CONFIRM), HtmlUtils.submit(msg("Cancel"),
                    ARG_CANCEL)));
        fb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        fb.append(HtmlUtils.formClose());
        sb.append(getPageHandler().showDialogQuestion(inner.toString(),
                fb.toString()));

        return makeEntryEditResult(request, entry,
                                   msg("Entry delete confirm"), sb);
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
    public Result processEntryListDelete(Request request) throws Exception {
        List<Entry> entries = new ArrayList<Entry>();
        for (String id :
                StringUtil.split(request.getString(ARG_ENTRYIDS, ""), ",",
                                 true, true)) {
            Entry entry = getEntry(request, id, false);
            if (entry == null) {
                throw new RepositoryUtil.MissingEntryException(
                    "Could not find entry:" + id);
            }
            if (entry.isTopEntry()) {
                StringBuffer sb = new StringBuffer();
                sb.append(
                    getPageHandler().showDialogNote(
                        msg("Cannot delete top-level folder")));

                return new Result(msg("Entry Delete"), sb);
            }
            entries.add(entry);
        }

        return processEntryListDelete(request, entries);

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processEntryListDelete(Request request, List<Entry> entries)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        if (request.exists(ARG_CANCEL)) {
            if (entries.size() == 0) {
                return new Result(
                    request.url(getRepository().URL_ENTRY_SHOW));
            }
            String id = entries.get(0).getParentEntryId();

            return new Result(request.url(getRepository().URL_ENTRY_SHOW,
                                          ARG_ENTRYID, id));
        }

        if (request.exists(ARG_DELETE_CONFIRM)) {
            request.ensureAuthToken();

            return asynchDeleteEntries(request, entries);
        }


        if (entries.size() == 0) {
            return new Result(
                "",
                new StringBuffer(
                    getPageHandler().showDialogWarning(
                        msg("No entries selected"))));
        }

        StringBuffer msgSB    = new StringBuffer();
        StringBuffer idBuffer = new StringBuffer();
        for (Entry entry : entries) {
            idBuffer.append(",");
            idBuffer.append(entry.getId());
        }
        boolean      anyFolders  = false;
        StringBuffer entryListSB = new StringBuffer();
        for (Entry toBeDeletedEntry : entries) {
            entryListSB.append(
                getPageHandler().getConfirmBreadCrumbs(
                    request, toBeDeletedEntry));
            entryListSB.append(HtmlUtils.br());
            if (toBeDeletedEntry.isGroup()) {
                anyFolders = true;
            }
        }

        if (entries.size() == 1) {
            msgSB.append(
                msg("Are you sure you want to delete the following entry?"));
        } else {
            msgSB.append(
                msg(
                "Are you sure you want to delete all of the following entries?"));
        }
        msgSB.append(
            HtmlUtils.div(
                entryListSB.toString(),
                HtmlUtils.cssClass("ramadda-confirm-entries")));

        if (anyFolders) {
            msgSB.append(
                HtmlUtils.div(
                    HtmlUtils.b(
                        msg(
                        "Note: This will also delete everything contained by the above "
                        + ((entries.size() == 1)
                           ? "folder"
                           : "folders")))));
        }
        request.formPostWithAuthToken(sb,
                                      getRepository().URL_ENTRY_DELETELIST);
        StringBuffer hidden = new StringBuffer(HtmlUtils.hidden(ARG_ENTRYIDS,
                                  idBuffer.toString()));
        String form = PageHandler.makeOkCancelForm(request,
                          getRepository().URL_ENTRY_DELETELIST,
                          ARG_DELETE_CONFIRM, hidden.toString());
        sb.append(getPageHandler().showDialogQuestion(msgSB.toString(),
                form));


        return new Result(msg("Delete Confirm"), sb);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     *
     * @return _more_
     */
    public Result asynchDeleteEntries(Request request,
                                      final List<Entry> entries) {
        final Request        theRequest = request;
        Entry                entry      = entries.get(0);
        Entry                group      = entries.get(0).getParentEntry();
        final String         groupId    = entries.get(0).getParentEntryId();

        ActionManager.Action action     = new ActionManager.Action() {
            public void run(Object actionId) throws Exception {
                deleteEntries(theRequest, entries, actionId);
            }
        };
        String href =
            HtmlUtils.href(request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                            group), "Continue");

        return getActionManager().doAction(request, action, "Deleting entry",
                                           "Continue: " + href, group);
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param title _more_
     * @param sb _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result makeEntryEditResult(Request request, Entry entry,
                                      String title, StringBuffer sb)
            throws Exception {
        Result result = new Result(title, sb);

        return addEntryHeader(request, entry, result);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    private void deleteEntry(Request request, Entry entry) throws Exception {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);
        deleteEntries(request, entries, null);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     * @param asynchId _more_
     *
     * @throws Exception _more_
     */
    public void deleteEntries(Request request, List<Entry> entries,
                              Object asynchId)
            throws Exception {

        if (entries.size() == 0) {
            return;
        }
        delCnt = 0;
        Connection connection = getDatabaseManager().getConnection();
        try {
            deleteEntriesInner(request, entries, connection, asynchId);
        } finally {
            getDatabaseManager().closeConnection(connection);
        }
    }




    /** _more_ */
    int delCnt = 0;



    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     * @param connection _more_
     * @param actionId _more_
     *
     * @throws Exception _more_
     */
    private void deleteEntriesInner(Request request, List<Entry> entries,
                                    Connection connection, Object actionId)
            throws Exception {

        //Exclude the synthetic entries
        List<Entry> okEntries = new ArrayList<Entry>();
        for (Entry entry : entries) {
            //we don't ask the type if its a synth type 
            if (

            /** entry.getTypeHandler().isSynthType()|| */
            isSynthEntry(entry.getId())) {
                continue;
            }
            okEntries.add(entry);
        }
        entries = okEntries;


        List<String[]> found = getDescendents(request, entries, connection,
                                   true, true);
        String query;

        query =
            SqlUtil.makeDelete(Tables.PERMISSIONS.NAME,
                               SqlUtil.eq(Tables.PERMISSIONS.COL_ENTRY_ID,
                                          "?"));
        PreparedStatement permissionsStmt =
            connection.prepareStatement(query);

        query = SqlUtil.makeDelete(
            Tables.ASSOCIATIONS.NAME,
            SqlUtil.makeOr(
                Misc.newList(
                    SqlUtil.eq(Tables.ASSOCIATIONS.COL_FROM_ENTRY_ID, "?"),
                    SqlUtil.eq(Tables.ASSOCIATIONS.COL_TO_ENTRY_ID, "?"))));

        PreparedStatement assocStmt = connection.prepareStatement(query);
        query = SqlUtil.makeDelete(Tables.COMMENTS.NAME,
                                   SqlUtil.eq(Tables.COMMENTS.COL_ENTRY_ID,
                                       "?"));
        PreparedStatement commentsStmt = connection.prepareStatement(query);


        query = SqlUtil.makeDelete(Tables.METADATA.NAME,
                                   SqlUtil.eq(Tables.METADATA.COL_ENTRY_ID,
                                       "?"));
        PreparedStatement metadataStmt = connection.prepareStatement(query);

        PreparedStatement entriesStmt = connection.prepareStatement(
                                            SqlUtil.makeDelete(
                                                Tables.ENTRIES.NAME,
                                                Tables.ENTRIES.COL_ID, "?"));

        PreparedStatement[] statements = { permissionsStmt, metadataStmt,
                                           commentsStmt, assocStmt,
                                           entriesStmt };

        connection.setAutoCommit(false);
        Statement extraStmt = connection.createStatement();
        try {
            int batchCnt       = 0;
            int totalDeleteCnt = 0;
            //Go backwards so we go up the tree and hit the children first
            List<String>   allIds            = new ArrayList<String>();
            List<Resource> resourcesToDelete = new ArrayList<Resource>();
            for (int i = found.size() - 1; i >= 0; i--) {
                String[] tuple = found.get(i);
                String   id    = tuple[0];
                removeFromCache(id);
                allIds.add(id);
                totalDeleteCnt++;
                if ((actionId != null)
                        && !getActionManager().getActionOk(actionId)) {
                    getActionManager().setActionMessage(actionId,
                            "Delete canceled");
                    connection.rollback();

                    return;
                }
                getActionManager().setActionMessage(actionId,
                        "Deleted:" + totalDeleteCnt + "/" + found.size()
                        + " entries");

                resourcesToDelete.add(new Resource(new File(tuple[2]),
                        tuple[3]));

                batchCnt++;
                assocStmt.setString(2, id);
                for (PreparedStatement stmt : statements) {
                    stmt.setString(1, id);
                    stmt.addBatch();
                }

                TypeHandler typeHandler =
                    getRepository().getTypeHandler(tuple[1]);
                typeHandler.deleteEntry(request, extraStmt, id);
                if (batchCnt > 100) {
                    for (PreparedStatement stmt : statements) {
                        stmt.executeBatch();
                    }
                    batchCnt = 0;
                }
            }
            for (PreparedStatement stmt : statements) {
                stmt.executeBatch();
            }
            connection.commit();
            connection.setAutoCommit(true);
            for (Resource resource : resourcesToDelete) {
                getStorageManager().removeFile(resource);
            }
            for (String id : allIds) {
                getStorageManager().deleteEntryDir(id);
            }
            Misc.run(getRepository(), "checkDeletedEntries", allIds);
        } finally {
            getDatabaseManager().closeStatement(extraStmt);
            for (PreparedStatement stmt : statements) {
                getDatabaseManager().closeStatement(stmt);
            }
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
    public Result processEntryUploadOk(Request request) throws Exception {
        StringBuffer sb    = new StringBuffer();
        Entry        entry = getEntry(request);
        //We use the category on the entry to flag the uploaded entries
        entry.setCategory("");
        addNewEntry(request, entry);

        return new Result(request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                           entry));
    }

    /** _more_ */
    public final static String CATEGORY_UPLOAD = "upload";

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    private void publishAnonymousEntry(Request request, Entry entry)
            throws Exception {
        List<Metadata> metadataList =
            getMetadataManager().findMetadata(request, entry,
                AdminMetadataHandler.TYPE_ANONYMOUS_UPLOAD, false);
        //Reset the category
        if (metadataList != null) {
            Metadata metadata = metadataList.get(0);
            User     newUser  =
                getUserManager().findUser(metadata.getAttr1());
            if (newUser != null) {
                entry.setUser(newUser);
            } else {
                entry.setUser(entry.getParentEntry().getUser());
            }
            entry.setCategory(metadata.getAttr3());
        } else {
            entry.setCategory("");
        }
        entry.setTypeHandler(
            getRepository().getTypeHandler(TypeHandler.TYPE_FILE));

        if (entry.isFile()) {
            File newFile = getStorageManager().moveToStorage(request,
                               entry.getResource().getTheFile());
            entry.getResource().setPath(newFile.toString());
        }

    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public boolean isAnonymousUpload(Entry entry) {
        return Misc.equals(entry.getCategory(), CATEGORY_UPLOAD);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parentEntry _more_
     *
     * @throws Exception _more_
     */
    private void initUploadedEntry(Request request, Entry entry,
                                   Entry parentEntry)
            throws Exception {


        String oldType = entry.getCategory();
        entry.setCategory(CATEGORY_UPLOAD);
        //Note: the name and description have already been encoded to prevent xss attacks
        //        entry.setName(RepositoryUtil.encodeUntrustedText(getEntryDisplayName(entry)));
        //        entry.setDescription(
        //            RepositoryUtil.encodeUntrustedText(entry.getDescription()));

        //        System.err.println ("after:" + entry.getDescription());
        String fromName = RepositoryUtil.encodeUntrustedText(
                              request.getString(
                                  ARG_CONTRIBUTION_FROMNAME, ""));
        String fromEmail = RepositoryUtil.encodeUntrustedText(
                               request.getString(
                                   ARG_CONTRIBUTION_FROMEMAIL, ""));
        String user = fromName;
        entry.addMetadata(
            new Metadata(
                getRepository().getGUID(), entry.getId(),
                AdminMetadataHandler.TYPE_ANONYMOUS_UPLOAD, false, user,
                request.getIp(), ((oldType != null)
                                  ? oldType
                                  : ""), fromEmail, ""));
        User parentUser = parentEntry.getUser();
        logInfo("upload: setting user to: " + parentUser.getName()
                + " from parent folder:" + parentEntry);
        entry.setUser(parentUser);

        if (getAdmin().isEmailCapable()) {
            StringBuffer contents =
                new StringBuffer(
                    "A new entry has been uploaded to the RAMADDA server under the folder: ");
            String url1 = HtmlUtils.url(getFullEntryShowUrl(request),
                                        ARG_ENTRYID, parentEntry.getId());

            contents.append(HtmlUtils.href(url1, parentEntry.getFullName()));
            contents.append("<p>\n\n");
            String url = HtmlUtils.url(getFullEntryShowUrl(request),
                                       ARG_ENTRYID, entry.getId());
            contents.append("Edit to confirm: ");
            contents.append(HtmlUtils.href(url, entry.getLabel()));
            boolean sentNotification = false;
            List<Metadata> metadataList =
                getMetadataManager().findMetadata(request, parentEntry,
                    ContentMetadataHandler.TYPE_CONTACT, true);
            if (metadataList != null) {
                for (Metadata metadata : metadataList) {
                    sentNotification = true;
                    getRepository().getMailManager().sendEmail(
                        metadata.getAttr2(), "Uploaded Entry",
                        contents.toString(), true);

                }

            }
            if ( !sentNotification) {
                getRepository().getMailManager().sendEmail(
                    parentUser.getEmail(), "Uploaded Entry",
                    contents.toString(), true);
            }
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
    public Result processEntryUpload(Request request) throws Exception {
        TypeHandler typeHandler =
            getRepository().getTypeHandler(TypeHandler.TYPE_CONTRIBUTION);
        Entry        group = findGroup(request);
        StringBuffer sb    = new StringBuffer();
        if ( !request.exists(ARG_CONTRIBUTION_FROMNAME)) {
            sb.append(request.uploadForm(getRepository().URL_ENTRY_UPLOAD,
                                         HtmlUtils.attr("name",
                                             "entryform")));
            sb.append(HtmlUtils.submit(msg("Upload")));
            sb.append(HtmlUtils.formTable());
            sb.append(HtmlUtils.hidden(ARG_GROUP, group.getId()));
            typeHandler.addToEntryForm(request, sb, group, null,
                                       new FormInfo());
            sb.append(HtmlUtils.formTableClose());
            sb.append(HtmlUtils.submit(msg("Upload")));
            sb.append(HtmlUtils.formClose());
        } else {
            return doProcessEntryChange(request, true, null);
        }

        return makeEntryEditResult(request, group, msg("Upload"), sb);
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
    public Result processEntryNew(Request request) throws Exception {

        Entry        group = findGroup(request);
        StringBuffer sb    = new StringBuffer();
        sb.append(HtmlUtils.p());
        sb.append(msgHeader("Choose entry type"));
        List<String> categories = new ArrayList<String>();
        Hashtable<String, StringBuffer> catMap = new Hashtable<String,
                                                     StringBuffer>();

        for (String preload : PRELOAD_CATEGORIES) {
            categories.add(preload);
            catMap.put(preload, new StringBuffer());
        }

        HashSet<String> exclude = new HashSet<String>();
        //        exclude.add(TYPE_FILE);
        //        exclude.add(TYPE_GROUP);
        List<TypeHandler> typeHandlers = getRepository().getTypeHandlers();

        List<String> sessionTypes =
            (List<String>) getSessionManager().getSessionProperty(request,
                ARG_TYPE);

        for (TypeHandler typeHandler : typeHandlers) {
            if ( !typeHandler.getForUser()) {
                continue;
            }
            if (typeHandler.isAnyHandler()) {
                continue;
            }
            if (exclude.contains(typeHandler.getType())) {
                continue;
            }
            if ( !typeHandler.canBeCreatedBy(request)) {
                continue;
            }
            String icon = typeHandler.getProperty("icon", (String) null);
            String img;
            if (icon == null) {
                icon = ICON_BLANK;
                img = HtmlUtils.img(typeHandler.iconUrl(icon), "",
                                    HtmlUtils.attr(HtmlUtils.ATTR_WIDTH,
                                        "16"));
            } else {
                img = HtmlUtils.img(typeHandler.iconUrl(icon));
            }




            boolean hasUsedType =
                ((sessionTypes != null)
                 && sessionTypes.contains(typeHandler.getType()));
            String       category = typeHandler.getCategory();
            StringBuffer buffer   = catMap.get(category);

            if (buffer == null) {
                catMap.put(category, buffer = new StringBuffer());
                if (hasUsedType) {
                    categories.add(0, category);
                } else {
                    categories.add(category);
                }
            } else if (hasUsedType) {
                categories.remove(category);
                categories.add(0, category);
            }

            buffer.append(HtmlUtils
                .href(request
                    .url(getRepository().URL_ENTRY_FORM, ARG_GROUP, group
                        .getId(), ARG_TYPE, typeHandler.getType()), img + " "
                            + msg(typeHandler.getLabel())));

            buffer.append(HtmlUtils.br());
        }
        sb.append("<table cellpadding=10><tr valign=top>");
        int colCnt = 0;
        for (String cat : categories) {
            StringBuffer catBuff = catMap.get(cat);
            if (catBuff.length() == 0) {
                continue;
            }
            sb.append(HtmlUtils.col(HtmlUtils.b(msg(cat))
                                    + HtmlUtils.insetDiv(catBuff.toString(),
                                        3, 15, 0, 0)));
            colCnt++;
            if (colCnt > 3) {
                sb.append("</tr><tr valign=top>");
                colCnt = 0;
            }
        }
        sb.append("</tr></table>");

        return makeEntryEditResult(request, group, "Create Entry", sb);
        //        return new Result("New Form", sb, Result.TYPE_HTML);
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
    public Result processEntryGetByFilename(Request request)
            throws Exception {
        if (request.getCheckingAuthMethod()) {
            return new Result(AuthorizationMethod.AUTH_HTTP);
        }

        String file = request.getString(ARG_FILESUFFIX, null);
        if (file == null) {
            file = IOUtil.getFileTail(request.getRequestPath());
            request.put(ARG_FILESUFFIX, file);
        }


        List[] groupAndEntries =
            getRepository().getEntryManager().getEntries(request);
        List<Entry> entries = (List<Entry>) groupAndEntries[1];
        if (entries.size() == 0) {
            throw new IllegalArgumentException(
                "Could not find entry with file");
        }

        return processEntryGet(request, entries.get(0));
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
    public Result processEntryShowPath(Request request) throws Exception {
        List<String> toks = StringUtil.split(request.getRequestPath(), "/",
                                             true, true);
        String id    = toks.get(toks.size() - 1);
        Entry  entry = getEntry(request, id);
        if (entry == null) {
            throw new IllegalArgumentException(
                "Could not find entry from id:" + id);
        }

        return processEntryShow(request, entry);
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
    public Result processEntryLinks(Request request) throws Exception {
        Entry entry = getEntry(request);
        if (entry == null) {
            throw new IllegalArgumentException("Unable to find entry:"
                    + request);
        }
        StringBuffer sb = new StringBuffer();

        sb.append(header("Entry Links"));


        sb.append(getEntryActionsTable(request, entry, OutputType.TYPE_ALL));

        //                                       OutputType.TYPE_FEEDS));
        return addEntryHeader(request, entry,
                              new Result(msg("Entry Links"), sb));
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
    public Result processEntryGet(Request request) throws Exception {
        if (request.getCheckingAuthMethod()) {
            return new Result(AuthorizationMethod.AUTH_HTTP);
        }

        Entry entry = getEntryFromRequest(request, ARG_ENTRYID,
                                          getRepository().URL_ENTRY_GET);

        return processEntryGet(request, entry);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processEntryGet(Request request, Entry entry)
            throws Exception {
        if (entry == null) {
            throw new RepositoryUtil.MissingEntryException(
                "Could not find entry");
        }


        if ( !getAccessManager().canAccessFile(request, entry)) {
            throw new AccessException("No access to file", request);
        }


        if ( !entry.getResource().isUrl()) {
            if ( !getAccessManager().canDownload(request, entry)) {
                fatalError(request, "Cannot download file");
            }
        }

        String path = entry.getResource().getPath();

        String mimeType = getRepository().getMimeTypeFromSuffix(
                              IOUtil.getFileExtension(path));

        boolean isImage = ImageUtils.isImage(path);
        if (request.defined(ARG_IMAGEWIDTH) && isImage) {
            int width = request.get(ARG_IMAGEWIDTH, 75);
            File thumb = getStorageManager().getThumbFile("entry"
                             + IOUtil.cleanFileName(entry.getId()) + "_"
                             + width + IOUtil.getFileExtension(path));
            if ( !thumb.exists()) {
                Image image =
                    ImageUtils.readImage(entry.getResource().getPath());
                image = ImageUtils.resize(image, width, -1);
                ImageUtils.waitOnImage(image);
                ImageUtils.writeImageToFile(image, thumb);
            }

            return new Result(BLANK,
                              getStorageManager().getFileInputStream(thumb),
                              mimeType);
        } else {
            File file   = entry.getFile();
            long length = file.length();
            if (request.isHeadRequest()) {
                System.err.println("got head request");
                Result result = new Result("", new StringBuffer());
                result.addHttpHeader(HtmlUtils.HTTP_CONTENT_LENGTH,
                                     "" + length);
                result.addHttpHeader("Connection", "close");
                result.setLastModified(new Date(file.lastModified()));

                return result;
            }

            InputStream inputStream =
                getStorageManager().getFileInputStream(file);
            Result result = new Result(BLANK, inputStream, mimeType);
            result.addHttpHeader(HtmlUtils.HTTP_CONTENT_LENGTH, "" + length);
            result.setLastModified(new Date(file.lastModified()));
            result.setCacheOk(true);

            return result;
        }

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
    public File getFileForEntry(Entry entry) throws Exception {
        return entry.getTypeHandler().getFileForEntry(entry);
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

    public Result processGetEntries(Request request) throws Exception {
        List<Entry> entries    = new ArrayList();
        boolean     doAll      = request.defined("getall");
        boolean     doSelected = request.defined("getselected");
        String      prefix     = (doAll
                                  ? "all_"
                                  : "entry_");

        for (Enumeration keys = request.keys(); keys.hasMoreElements(); ) {
            String id = (String) keys.nextElement();
            if (doSelected) {
                if ( !request.get(id, false)) {
                    continue;
                }
            }
            if ( !id.startsWith(prefix)) {
                continue;
            }
            id = id.substring(prefix.length());
            Entry entry = getEntry(request, id);
            if (entry != null) {
                entries.add(entry);
            }
        }
        String ids = request.getIds((String) null);
        if (ids != null) {
            List<String> idList = StringUtil.split(ids, ",", true, true);
            for (String id : idList) {
                Entry entry = getEntry(request, id);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }

        entries = getAccessManager().filterEntries(request, entries);
        Entry group = null;
        for (Entry entry : entries) {
            if (group == null) {
                group = entry.getParentEntry();
            } else if ( !group.equals(entry.getParentEntry())) {
                group = null;

                break;
            }
        }

        if (group != null) {
            request.put(ARG_ENTRYID, group.getId());
        }

        OutputHandler outputHandler =
            getRepository().getOutputHandler(request);
        Result result = outputHandler.outputGroup(request,
                            request.getOutput(), getDummyGroup(),
                            new ArrayList<Entry>(), entries);

        return addEntryHeader(request, (group != null)
                                       ? group
                                       : getTopGroup(), result);
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
    public Result processEntryCopy(Request request) throws Exception {

        String      fromIds = request.getString(ARG_FROM, "");
        List<Entry> entries = new ArrayList<Entry>();
        for (String id : StringUtil.split(fromIds, ",", true, true)) {
            Entry entry = getEntry(request, id, false);
            if (entry == null) {
                throw new RepositoryUtil.MissingEntryException(
                    "Could not find entry:" + id);
            }
            if (entry.isTopEntry()) {
                StringBuffer sb = new StringBuffer();
                sb.append(
                    getPageHandler().showDialogNote(
                        msg("Cannot copy top-level folder")));

                return new Result(msg("Entry Delete"), sb);
            }
            entries.add(entry);
        }


        if (entries.size() == 0) {
            throw new IllegalArgumentException("No entries specified");
        }

        if (request.exists(ARG_CANCEL)) {
            return new Result(
                request.entryUrl(
                    getRepository().URL_ENTRY_SHOW, entries.get(0)));
        }

        if ( !request.exists(ARG_TO) && !request.exists(ARG_TO + "_hidden")
                && !request.exists(ARG_TONAME)) {
            boolean      didOne = false;
            StringBuffer sb     = new StringBuffer();
            /*
            List<Entry>  cart      = getUserManager().getCart(request);
            List<Entry>  favorites = FavoriteEntry.getEntries(
                                        getUserManager().getFavorites(
                                            request, request.getUser()));
            List<Entry> groups = getGroups(cart);
            groups.addAll(getGroups(favorites));
            groups.addAll(getSessionFolders(request));
            HashSet seen = new HashSet();
            String separator = getPageHandler().getTemplateProperty(request,
                                                                    "ramadda.template.breadcrumbs.separator",
                                                                    BREADCRUMB_SEPARATOR);
            for (Entry group : groups) {
                if (seen.contains(group.getId())) {
                    continue;
                }
                seen.add(group.getId());
                if ( !getAccessManager().canDoAction(request, group,
                        Permission.ACTION_NEW)) {
                    continue;
                }
                boolean ok = true;
                for (Entry fromEntry : entries) {
                    if ( !okToMove(fromEntry, group)) {
                        ok = false;
                    }
                }
                if ( !ok) {
                    continue;
                }

                if ( !didOne) {
                    sb.append(
                        header("Please select a destination folder from the following list:"));
                    sb.append("<ul>");
                }
                sb.append(HtmlUtils.img(getPageHandler().getIconUrl(request, group)));
                sb.append(HtmlUtils.space(1));
                String label =group.getLabel();
                if(group.getParentEntry()!=null) {
                    label = group.getParentEntry().getLabel() + separator +
                        label;
                }
                sb.append(
                    HtmlUtils.href(
                        request.url(
                            getRepository().URL_ENTRY_COPY, ARG_FROM,
                            fromIds, ARG_TO,
                            group.getId()), label));
                sb.append(HtmlUtils.br());
                didOne = true;
            }
            */

            if (didOne) {
                sb.append("</ul>");
                sb.append(header("Or select a folder here:"));
            } else {
                sb.append(header("Please select a destination folder:"));
            }

            request.formPostWithAuthToken(sb, getRepository().URL_ENTRY_COPY);
            sb.append(HtmlUtils.hidden(ARG_FROM, fromIds));
            String select =
                getRepository().getHtmlOutputHandler().getSelect(request,
                    ARG_TO,
                    HtmlUtils.img(getRepository().iconUrl(ICON_FOLDER_OPEN))
                    + HtmlUtils.space(1) + msg("Select"), false, "", null,
                        false);

            sb.append(HtmlUtils.hidden(ARG_TO + "_hidden", "",
                                       HtmlUtils.id(ARG_TO + "_hidden")));

            sb.append(select);
            sb.append(HtmlUtils.space(1));
            sb.append(HtmlUtils.disabledInput(ARG_TO, "",
                    HtmlUtils.SIZE_60 + HtmlUtils.id(ARG_TO)));
            sb.append(HtmlUtils.submit(msg("Go")));
            sb.append(HtmlUtils.formClose());

            /*
            if(didOne) {
                sb.append(msgLabel("Or select one here"));
            }
            sb.append(HtmlUtils.br());
            sb.append(getTreeLink(request, getTopGroup(), ""));
            */
            return addEntryHeader(request, entries.get(0),
                                  new Result(msg("Entry Move/Copy"), sb));
        }


        String toId = request.getString(ARG_TO + "_hidden", (String) null);
        if (toId == null) {
            toId = request.getString(ARG_TO, (String) null);
        }

        String toName = request.getString(ARG_TONAME, (String) null);
        if ((toId == null) && (toName == null)) {
            throw new IllegalArgumentException(
                "No destination folder specified");
        }


        Entry toEntry;
        if (toId != null) {
            toEntry = getEntry(request, toId);
        } else {
            toEntry = findGroupFromName(request, toName, request.getUser(),
                                        false);
        }
        if (toEntry == null) {
            throw new RepositoryUtil.MissingEntryException(
                "Could not find entry: " + ((toId == null)
                                            ? toName
                                            : toId));
        }
        boolean isGroup = toEntry.isGroup();


        if (request.exists(ARG_ACTION_ASSOCIATE)) {
            if (entries.size() == 1) {
                return new Result(
                    request.url(
                        getRepository().URL_ASSOCIATION_ADD, ARG_FROM,
                        entries.get(0).getId(), ARG_TO, toEntry.getId()));
            }
        }


        if (request.exists(ARG_ACTION_MOVE)) {
            if ( !isGroup) {
                throw new IllegalArgumentException(
                    "Can only copy/move to a folder");
            }

            for (Entry fromEntry : entries) {
                if ( !getAccessManager().canDoAction(request, fromEntry,
                        Permission.ACTION_EDIT)) {
                    throw new AccessException("Cannot move:"
                            + fromEntry.getLabel(), request);
                }
            }
        } else if (request.exists(ARG_ACTION_COPY)) {
            if ( !isGroup) {
                throw new IllegalArgumentException(
                    "Can only copy/move to a folder");
            }
        }

        if ( !getAccessManager().canDoAction(request, toEntry,
                                             Permission.ACTION_NEW)) {
            throw new AccessException("Cannot copy/move to:"
                                      + toEntry.getLabel(), request);
        }


        StringBuffer fromList = new StringBuffer();
        for (Entry fromEntry : entries) {
            fromList.append(getPageHandler().getBreadCrumbs(request,
                    fromEntry));
            fromList.append(HtmlUtils.br());
        }


        if ( !(request.exists(ARG_ACTION_MOVE)
                || request.exists(ARG_ACTION_COPY)
                || request.exists(ARG_ACTION_ASSOCIATE))) {
            StringBuffer sb = new StringBuffer();
            if (entries.size() > 1) {
                sb.append(
                    msg(
                    "What do you want to do with the following entries?"));
            } else {
                sb.append(
                    msg("What do you want to do with the following entry?"));
            }
            sb.append(HtmlUtils.br());
            sb.append(HtmlUtils.insetDiv(fromList.toString(), 20, 20, 20, 0));
            StringBuffer fb = new StringBuffer();
            request.formPostWithAuthToken(fb, getRepository().URL_ENTRY_COPY);
            fb.append(HtmlUtils.hidden(ARG_TO, toEntry.getId()));
            fb.append(HtmlUtils.hidden(ARG_FROM, fromIds));

            String destName = getEntryDisplayName(toEntry);

            if (isGroup) {
                fb.append(HtmlUtils.submit(msg("Move to") + " " + destName,
                                           ARG_ACTION_MOVE));
                fb.append(HtmlUtils.buttonSpace());
                fb.append(HtmlUtils.submit(msg("Copy to") + " " + destName,
                                           ARG_ACTION_COPY));
            }

            if (entries.size() == 1) {
                fb.append(HtmlUtils.buttonSpace());
                fb.append(HtmlUtils.submit(msg("Link to") + " " + destName,
                                           ARG_ACTION_ASSOCIATE));
            }

            fb.append(HtmlUtils.buttonSpace());
            fb.append(HtmlUtils.submit(msg("Cancel"), ARG_CANCEL));
            fb.append(HtmlUtils.formClose());
            StringBuffer contents = new StringBuffer(
                                        getPageHandler().showDialogQuestion(
                                            sb.toString(), fb.toString()));
            Result result = new Result(msg("Move confirm"), contents);

            return addEntryHeader(request, toEntry, result);
        }


        for (Entry fromEntry : entries) {
            if ( !okToMove(fromEntry, toEntry)) {
                StringBuffer sb = new StringBuffer();
                sb.append(
                    getPageHandler().showDialogError(
                        msg("Cannot move a folder to its descendent")));

                return addEntryHeader(request, fromEntry, new Result("", sb));
            }
        }

        request.ensureAuthToken();
        if (request.exists(ARG_ACTION_MOVE)) {
            Entry toGroup = (Entry) toEntry;

            return processEntryMove(request, toGroup, entries);
        } else if (request.exists(ARG_ACTION_COPY)) {
            Entry toGroup = (Entry) toEntry;

            return processEntryCopy(request, toGroup, entries);
        } else if (request.exists(ARG_ACTION_ASSOCIATE)) {
            if (entries.size() == 1) {
                return new Result(
                    request.url(
                        getRepository().URL_ASSOCIATION_ADD, ARG_FROM,
                        entries.get(0).getId(), ARG_TO, toEntry.getId()));
            }
        }

        Result result = new Result(msg("Move"), new StringBuffer());

        return addEntryHeader(request, toEntry, result);

    }




    /**
     * _more_
     *
     * @param request _more_
     * @param toGroup _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processEntryCopy(Request request, Entry toGroup,
                                   List<Entry> entries)
            throws Exception {
        StringBuffer sb         = new StringBuffer();
        Connection   connection = getDatabaseManager().getConnection();
        connection.setAutoCommit(false);
        List<Entry> newEntries = new ArrayList<Entry>();
        try {
            List<String[]> ids = getDescendents(request, entries, connection,
                                     true, true);
            Hashtable<String, Entry> oldIdToNewEntry = new Hashtable<String,
                                                           Entry>();
            for (int i = 0; i < ids.size(); i++) {
                String[]    tuple          = ids.get(i);
                String      id             = tuple[0];
                Entry       oldEntry       = getEntry(request, id);
                String      newId          = getRepository().getGUID();
                TypeHandler oldTypeHandler = oldEntry.getTypeHandler();
                TypeHandler newTypeHandler =
                    oldTypeHandler.getTypeHandlerForCopy(oldEntry);
                Entry newEntry = newTypeHandler.createEntry(newId);
                oldIdToNewEntry.put(oldEntry.getId(), newEntry);
                //See if this new entry is somewhere down in the tree
                Entry newParent =
                    oldIdToNewEntry.get(oldEntry.getParentEntryId());
                if (newParent == null) {
                    newParent = toGroup;
                }
                Resource newResource =
                    oldTypeHandler.getResourceForCopy(request, oldEntry,
                        newEntry);
                newEntry.initEntry(oldEntry.getName(),
                                   oldEntry.getDescription(),
                                   (Entry) newParent, request.getUser(),
                                   newResource, oldEntry.getCategory(),
                                   oldEntry.getCreateDate(),
                                   new Date().getTime(),
                                   oldEntry.getStartDate(),
                                   oldEntry.getEndDate(),
                                   oldEntry.getValues());

                newEntry.setLocation(oldEntry);
                newTypeHandler.initializeCopiedEntry(newEntry, oldEntry);

                List<Metadata> newMetadata = new ArrayList<Metadata>();
                for (Metadata oldMetadata :
                        getMetadataManager().getMetadata(oldEntry)) {
                    newMetadata.add(
                        getMetadataManager().copyMetadata(
                            oldEntry, newEntry, oldMetadata));
                }
                newEntry.setMetadata(newMetadata);
                newEntries.add(newEntry);
            }
            addNewEntries(request, newEntries);

            return new Result(request.url(getRepository().URL_ENTRY_SHOW,
                                          ARG_ENTRYID, toGroup.getId(),
                                          ARG_MESSAGE,
                                          getRepository().translate(request,
                                              "Entries copied")));
        } finally {
            getDatabaseManager().closeConnection(connection);
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param toGroup _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result processEntryMove(Request request, Entry toGroup,
                                    List<Entry> entries)
            throws Exception {
        Connection connection = getDatabaseManager().getConnection();
        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();
        try {
            for (Entry fromEntry : entries) {
                fromEntry.setParentEntry(toGroup);
                String oldId = fromEntry.getId();
                String newId = oldId;
                String sql =
                    "UPDATE  " + Tables.ENTRIES.NAME + " SET "
                    + SqlUtil.unDot(Tables.ENTRIES.COL_PARENT_GROUP_ID)
                    + " = " + SqlUtil.quote(fromEntry.getParentEntryId())
                    + " WHERE "
                    + SqlUtil.eq(Tables.ENTRIES.COL_ID,
                                 SqlUtil.quote(fromEntry.getId()));
                statement.execute(sql);
                connection.commit();
            }
            getDatabaseManager().closeStatement(statement);
            connection.setAutoCommit(true);

            return new Result(request.url(getRepository().URL_ENTRY_SHOW,
                                          ARG_ENTRYID,
                                          entries.get(0).getId()));
        } finally {
            getDatabaseManager().closeConnection(connection);
        }
    }



    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void setEntryBounds(Entry entry) throws Exception {
        Connection connection = getDatabaseManager().getConnection();
        try {
            Statement statement = connection.createStatement();
            String sql =
                "UPDATE  " + Tables.ENTRIES.NAME + " SET "
                + columnSet(Tables.ENTRIES.COL_NORTH, entry.getNorth()) + ","
                + columnSet(Tables.ENTRIES.COL_SOUTH, entry.getSouth()) + ","
                + columnSet(Tables.ENTRIES.COL_EAST, entry.getEast()) + ","
                + columnSet(Tables.ENTRIES.COL_WEST, entry.getWest()) + ","
                + columnSet(
                    Tables.ENTRIES.COL_ALTITUDEBOTTOM,
                    entry.getAltitudeBottom()) + ","
                        + columnSet(
                            Tables.ENTRIES.COL_ALTITUDETOP,
                            entry.getAltitudeTop()) + " WHERE "
                                + SqlUtil.eq(
                                    Tables.ENTRIES.COL_ID,
                                    SqlUtil.quote(entry.getId()));
            statement.execute(sql);
            getDatabaseManager().closeStatement(statement);
        } finally {
            getDatabaseManager().closeConnection(connection);
        }
    }

    /**
     * _more_
     *
     * @param col _more_
     * @param value _more_
     *
     * @return _more_
     */
    private String columnSet(String col, double value) {
        return SqlUtil.unDot(col) + " = " + value;
    }


    /**
     * _more_
     *
     * @param parent _more_
     * @param children _more_
     */
    public void setBoundsOnEntry(final Entry parent, List<Entry> children) {
        try {
            Rectangle2D.Double rect = getBounds(children);
            if ((rect != null) && !rect.equals(parent.getBounds())) {
                parent.setBounds(rect);
                setEntryBounds(parent);
            }
        } catch (Exception exc) {
            logError("Updating parent's bounds", exc);
        }
    }


    /**
     * _more_
     *
     * @param children _more_
     *
     * @return _more_
     */
    public Rectangle2D.Double getBounds(List<Entry> children) {
        Rectangle2D.Double rect = null;

        for (Entry child : children) {
            if ( !child.hasAreaDefined() && !child.hasLocationDefined()) {
                continue;
            }


            if (rect == null) {
                rect = child.getBounds();
            } else {
                rect.add(child.getBounds());
            }
        }

        return rect;
    }




    /**
     * _more_
     *
     * @param fromEntry _more_
     * @param toEntry _more_
     *
     * @return _more_
     */
    protected boolean okToMove(Entry fromEntry, Entry toEntry) {
        if ( !toEntry.isGroup()) {
            return false;
        }

        if (toEntry.getId().equals(fromEntry.getId())) {
            return false;
        }
        if (toEntry.getParentEntry() == null) {
            return true;
        }

        return okToMove(fromEntry, toEntry.getParentEntry());
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
    public Result processEntryXmlCreate(Request request) throws Exception {
        try {
            request.ensureAuthToken();

            return processEntryXmlCreateInner(request);
        } catch (Exception exc) {
            if (request.getString(ARG_RESPONSE, "").equals(RESPONSE_XML)) {
                exc.printStackTrace();

                return new Result(XmlUtil.tag(TAG_RESPONSE,
                        XmlUtil.attr(ATTR_CODE, CODE_ERROR),
                        "" + exc.getMessage()), MIME_XML);
            }

            throw exc;
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
    public Result processEntryExport(Request request) throws Exception {
        Entry entry = getEntry(request);
        if (entry == null) {
            throw new IllegalArgumentException("Unable to find entry:"
                    + request);
        }
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);

        return getRepository().getZipOutputHandler().toZip(request, "",
                entries, true, true);
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
    public Result processEntryImport(Request request) throws Exception {
        Entry                group       = findGroup(request);
        StringBuffer         sb          = new StringBuffer();
        StringBuffer         extraForm   = new StringBuffer();
        List<TwoFacedObject> importTypes = new ArrayList<TwoFacedObject>();
        for (ImportHandler importHandler :
                getRepository().getImportHandlers()) {
            importHandler.addImportTypes(importTypes, extraForm);
        }
        sb.append(msgHeader("Import " + LABEL_ENTRIES));
        request.uploadFormWithAuthToken(sb,
                                        getRepository().URL_ENTRY_XMLCREATE,
                                        makeFormSubmitDialog(sb,
                                            msg("Importing "
                                                + LABEL_ENTRIES)));
        sb.append(HtmlUtils.hidden(ARG_GROUP, group.getId()));
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.formEntry(msgLabel("File"),
                                      HtmlUtils.fileInput(ARG_FILE,
                                          HtmlUtils.SIZE_70)));

        sb.append(HtmlUtils.formEntry(msgLabel("Or URL"),
                                      HtmlUtils.input(ARG_URL, "",
                                          HtmlUtils.SIZE_70)));
        if (importTypes.size() > 0) {
            importTypes.add(
                0, new TwoFacedObject("RAMADDA will figure it out", ""));
            sb.append(HtmlUtils.formEntry(msgLabel("Type"),
                                          HtmlUtils.select(ARG_IMPORT_TYPE,
                                              importTypes)));
        }

        sb.append(HtmlUtils.formEntry("", HtmlUtils.submit("Submit")));


        sb.append(extraForm);

        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.formClose());

        return makeEntryEditResult(request, group, "Entry Import", sb);
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
    private Result processEntryXmlCreateInner(Request request)
            throws Exception {

        Entry parent = null;
        if (request.exists(ARG_GROUP)) {
            parent = getEntryFromArg(request, ARG_GROUP);
            if (parent == null) {
                parent = findEntryFromName(request,
                                           request.getString(ARG_GROUP, ""),
                                           request.getUser(), false);
            }

            if (parent == null) {
                throw new IllegalArgumentException(
                    "Could not find parent entry:"
                    + request.getString(ARG_GROUP));
            } else if ( !parent.isGroup()) {
                throw new IllegalArgumentException("Entry is not a group:"
                        + parent);
            }
        }
        String file = null;

        //Fetch the URL
        String url = request.getString(ARG_URL, null);
        if (Utils.stringDefined(url)) {
            file = getStorageManager().fetchUrl(url).toString();
        }

        if (file == null) {
            file = request.getUploadedFile(ARG_FILE);
        }

        if (file == null) {
            throw new IllegalArgumentException("No file argument given");
        }

        //Check the import handlers
        for (ImportHandler importHandler :
                getRepository().getImportHandlers()) {
            Result result = importHandler.handleRequest(request,
                                getRepository(), file, parent);
            if (result != null) {
                return result;
            }
        }

        String entriesXml = null;
        Hashtable<String, File> origFileToStorage = new Hashtable<String,
                                                        File>();

        InputStream fis = getStorageManager().getFileInputStream(file);
        try {
            if (file.endsWith(".zip")) {
                ZipInputStream zin = new ZipInputStream(fis);
                ZipEntry       ze;
                while ((ze = zin.getNextEntry()) != null) {
                    String entryName = ze.getName();
                    //                System.err.println ("ZIP: " + ze.getName());
                    if (entryName.endsWith("entries.xml")) {
                        InputStream entriesStream = zin;
                        //Check the import handlers
                        for (ImportHandler importHandler :
                                getRepository().getImportHandlers()) {
                            InputStream newStream =
                                importHandler.getStream(request, entryName,
                                    entriesStream);
                            if (newStream != null) {
                                entriesStream = newStream;

                                break;
                            }
                        }

                        entriesXml =
                            new String(IOUtil.readBytes(entriesStream, null,
                                false));
                    } else {
                        String name = IOUtil.getFileTail(ze.getName());
                        File f = getStorageManager().getTmpFile(request,
                                     name);
                        OutputStream fos =
                            getStorageManager().getFileOutputStream(f);
                        IOUtil.writeTo(zin, fos);
                        fos.close();
                        //Add both the zip path and the filename in case we have dir/filename.txt in the zip
                        origFileToStorage.put(ze.getName(), f);
                        origFileToStorage.put(name, f);
                    }
                }
                if (entriesXml == null) {
                    throw new IllegalArgumentException(
                        "No entries.xml file provided");
                }
            }
            if (entriesXml == null) {
                InputStream entriesStream = fis;
                //Check the import handlers
                for (ImportHandler importHandler :
                        getRepository().getImportHandlers()) {
                    InputStream newStream = importHandler.getStream(request,
                                                file, entriesStream);
                    if ((newStream != null) && (newStream != entriesStream)) {
                        entriesStream = newStream;

                        break;
                    }
                }
                entriesXml = IOUtil.readInputStream(entriesStream);
            }
        } finally {
            IOUtil.close(fis);
            getStorageManager().deleteFile(new File(file));
        }


        Element root = XmlUtil.getRoot(entriesXml);
        for (ImportHandler importHandler :
                getRepository().getImportHandlers()) {
            Element newRoot = importHandler.getDOM(request, root);
            if ((newRoot != null) && (newRoot != root)) {
                root = newRoot;

                break;
            }
        }

        List<Entry> newEntries = processEntryXml(request, root, parent,
                                     origFileToStorage);


        for (Entry entry : newEntries) {
            entry.getTypeHandler().doFinalEntryInitialization(request, entry);
        }
        if (request.getString(ARG_RESPONSE, "").equals(RESPONSE_XML)) {
            //TODO: Return a list of the newly created entries
            Element resultRoot = XmlUtil.create(XmlUtil.makeDocument(),
                                     TAG_RESPONSE, null,
                                     new String[] { ATTR_CODE,
                    CODE_OK });

            for (Entry entry : newEntries) {
                XmlUtil.create(resultRoot.getOwnerDocument(), TAG_ENTRY,
                               resultRoot, new String[] { ATTR_ID,
                        entry.getId() });


            }
            String xml = XmlUtil.toString(resultRoot);

            return new Result(xml, MIME_XML);
        }

        StringBuffer sb = new StringBuffer();
        sb.append(msgHeader("Imported entries"));
        sb.append("<ul>");

        for (Entry entry : newEntries) {
            sb.append("<li> ");
            sb.append(getPageHandler().getBreadCrumbs(request, entry,
                    parent));
        }
        sb.append("</ul>");
        if (parent != null) {
            return makeEntryEditResult(request, parent,
                                       "Imported " + LABEL_ENTRIES, sb);
        }

        return new Result("", sb);

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param root _more_
     * @param parent _more_
     * @param origFileToStorage _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry> processEntryXml(Request request, Element root,
                                       Entry parent,
                                       Hashtable<String,
                                           File> origFileToStorage)
            throws Exception {
        Hashtable<String, Entry> entries = new Hashtable<String, Entry>();
        if (parent != null) {
            entries.put("", parent);
        }

        List<Entry>   newEntries       = new ArrayList<Entry>();
        List<Element> entryNodes       = new ArrayList<Element>();
        List<Element> associationNodes = new ArrayList<Element>();

        NodeList      children;
        if (root.getTagName().equals(TAG_ENTRY)) {
            children = new XmlNodeList();
            ((XmlNodeList) children).add(root);
        } else {
            children = XmlUtil.getElements(root);
        }


        for (int i = 0; i < children.getLength(); i++) {
            Element node = (Element) children.item(i);
            if (node.getTagName().equals(TAG_ENTRY)) {
                entryNodes.add(node);
            } else if (node.getTagName().equals(TAG_ASSOCIATION)) {
                associationNodes.add(node);
            } else {
                throw new IllegalArgumentException("Unknown tag:"
                        + node.getTagName());
            }
        }

        List<String[]> idList = new ArrayList<String[]>();
        for (Element node : entryNodes) {

            Entry entry = createEntryFromXml(request, node, entries,
                                             origFileToStorage, true, false);

            newEntries.add(entry);
            if (XmlUtil.hasAttribute(node, ATTR_ID)) {
                idList.add(new String[] {
                    XmlUtil.getAttribute(node, ATTR_ID, ""),
                    entry.getId() });
            }

            if (XmlUtil.getAttribute(node, ATTR_ADDMETADATA, false)) {
                addInitialMetadata(request,
                                   (List<Entry>) Misc.newList(entry), true,
                                   false);
            } else if (XmlUtil.getAttribute(node, ATTR_ADDSHORTMETADATA,
                                            false)) {
                addInitialMetadata(request,
                                   (List<Entry>) Misc.newList(entry), true,
                                   true);
            }
        }

        for (Element node : associationNodes) {
            String id =
                getAssociationManager().processAssociationXml(request, node,
                    entries, origFileToStorage);
        }

        //Replace any entry re
        for (Entry newEntry : newEntries) {
            newEntry.getTypeHandler().convertIdsFromImport(newEntry, idList);
        }


        addNewEntries(request, newEntries);

        return newEntries;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param node _more_
     * @param entries _more_
     * @param files _more_
     * @param checkAccess _more_
     * @param internal _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry createEntryFromXml(Request request, Element node,
                                    Hashtable<String, Entry> entries,
                                    Hashtable<String, File> files,
                                    boolean checkAccess, boolean internal)
            throws Exception {
        String parentId    = XmlUtil.getAttribute(node, ATTR_PARENT, "");
        Entry  parentEntry = (Entry) entries.get(parentId);
        if (parentEntry == null) {
            parentEntry = (Entry) getEntry(request, parentId);
            if (parentEntry == null) {
                parentEntry = (Entry) findEntryFromName(request, parentId,
                        request.getUser(), false);
            }
            if (parentEntry == null) {
                throw new RepositoryUtil.MissingEntryException(
                    "Could not find parent:" + parentId);
            }
        }
        Entry entry = createEntryFromXml(request, node, parentEntry, files,
                                         checkAccess, internal);
        String tmpid = XmlUtil.getAttribute(node, ATTR_ID, (String) null);
        if (tmpid != null) {
            entries.put(tmpid, entry);
        }

        return entry;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param node _more_
     * @param parentEntry _more_
     * @param files _more_
     * @param checkAccess _more_
     * @param internal _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry createEntryFromXml(Request request, Element node,
                                    Entry parentEntry,
                                    Hashtable<String, File> files,
                                    boolean checkAccess, boolean internal)
            throws Exception {


        boolean doAnonymousUpload = false;
        String  name              = XmlUtil.getAttribute(node, ATTR_NAME, "");
        if (name.length() > 200) {
            name = name.substring(0, 195) + "...";
        }






        String category = XmlUtil.getAttribute(node, ATTR_CATEGORY, "");
        String description = XmlUtil.getAttribute(node, ATTR_DESCRIPTION,
                                 (String) null);
        if (description == null) {
            Element descriptionNode = XmlUtil.findChild(node,
                                          TAG_DESCRIPTION);
            if (descriptionNode != null) {
                description = XmlUtil.getChildText(descriptionNode);
                if ((description != null)
                        && XmlUtil.getAttribute(descriptionNode, "encoded",
                            false)) {
                    description =
                        new String(RepositoryUtil.decodeBase64(description));
                }
            }
        }
        if (description == null) {
            description = "";
        }


        if (checkAccess) {
            if ( !getAccessManager().canDoAction(request, parentEntry,
                    Permission.ACTION_NEW)) {
                if (getAccessManager().canDoAction(request, parentEntry,
                        Permission.ACTION_UPLOAD)) {
                    doAnonymousUpload = true;
                } else {
                    throw new IllegalArgumentException(
                        "Cannot add to parent folder");
                }
            }
        }


        String file = XmlUtil.getAttribute(node, ATTR_FILE, (String) null);
        String fileName = XmlUtil.getAttribute(node, ATTR_FILENAME,
                              (String) null);
        if (file != null) {
            File tmp = ((files == null)
                        ? null
                        : files.get(file));
            if (doAnonymousUpload) {
                File newFile =
                    getStorageManager().moveToAnonymousStorage(request, tmp,
                        "");

                file = newFile.toString();
            } else {
                String targetName = fileName;
                if (targetName != null) {
                    targetName =
                        getStorageManager().getStorageFileName(targetName);
                }
                File newFile = getStorageManager().moveToStorage(request,
                                   tmp, targetName);
                file = newFile.toString();
            }
        }
        String url = XmlUtil.getAttribute(node, ATTR_URL, (String) null);
        if (url == null) {
            url = XmlUtil.getGrandChildText(node, ATTR_URL, (String) null);
        }
        String localFile = XmlUtil.getAttribute(node, ATTR_LOCALFILE,
                               (String) null);
        String localFileToMove = XmlUtil.getAttribute(node,
                                     ATTR_LOCALFILETOMOVE, (String) null);




        String   id = getRepository().getGUID();

        Resource resource;
        if (file != null) {
            resource = new Resource(file, Resource.TYPE_STOREDFILE);
        } else if (localFile != null) {
            if ( !request.getUser().getAdmin()) {
                throw new IllegalArgumentException(
                    "Only administrators can upload a local file");
            }
            resource = new Resource(localFile, Resource.TYPE_LOCAL_FILE);
        } else if (localFileToMove != null) {
            if ( !request.getUser().getAdmin()) {
                throw new IllegalArgumentException(
                    "Only administrators can upload a local file");
            }
            localFileToMove = getStorageManager().moveToStorage(request,
                    new File(localFileToMove)).toString();

            resource = new Resource(localFileToMove,
                                    Resource.TYPE_STOREDFILE);
        } else if (url != null) {
            resource = new Resource(url, Resource.TYPE_URL);
            int size = XmlUtil.getAttribute(node, ATTR_SIZE, 0);
            resource.setFileSize(size);
        } else {
            resource = new Resource("", Resource.TYPE_UNKNOWN);
        }



        String type = XmlUtil.getAttribute(node, ATTR_TYPE,
                                           TypeHandler.TYPE_FILE);

        TypeHandler typeHandler = null;


        if (type.equals(TypeHandler.TYPE_GUESS)) {
            typeHandler = findDefaultTypeHandler(resource.getPath());
        }


        if (typeHandler == null) {
            //Pass in false so we error if the repository does not find the type
            typeHandler = getRepository().getTypeHandler(type, false, false);
        }


        if (typeHandler == null) {
            throw new RepositoryUtil.MissingEntryException(
                "Could not find type:" + type);
        }



        Date createDate = new Date();


        //        System.err.println("node:" + XmlUtil.toString(node));
        if (XmlUtil.hasAttribute(node, ATTR_CREATEDATE)) {
            createDate =
                getPageHandler().parseDate(XmlUtil.getAttribute(node,
                    ATTR_CREATEDATE));
        }

        Date fromDate = createDate;
        if (XmlUtil.hasAttribute(node, ATTR_FROMDATE)) {
            fromDate = getPageHandler().parseDate(XmlUtil.getAttribute(node,
                    ATTR_FROMDATE));
        }
        Date toDate = fromDate;
        if (XmlUtil.hasAttribute(node, ATTR_TODATE)) {
            toDate = getPageHandler().parseDate(XmlUtil.getAttribute(node,
                    ATTR_TODATE));
        }
        if ( !canBeCreatedBy(request, typeHandler)) {
            throw new IllegalArgumentException(
                "Cannot create an entry of type "
                + typeHandler.getDescription());
        }
        Entry entry = typeHandler.createEntry(id);
        entry.initEntry(name, description, parentEntry, request.getUser(),
                        resource, category, createDate.getTime(),
                        new Date().getTime(), fromDate.getTime(),
                        toDate.getTime(), null);

        if (doAnonymousUpload) {
            initUploadedEntry(request, entry, parentEntry);
        }
        if (XmlUtil.hasAttribute(node, ATTR_LATITUDE)
                && XmlUtil.hasAttribute(node, ATTR_LONGITUDE)) {
            entry.setNorth(Misc.decodeLatLon(XmlUtil.getAttribute(node,
                    ATTR_LATITUDE, "")));
            entry.setSouth(entry.getNorth());
            entry.setWest(Misc.decodeLatLon(XmlUtil.getAttribute(node,
                    ATTR_LONGITUDE, "")));
            entry.setEast(entry.getWest());
        } else {
            entry.setNorth(Misc.decodeLatLon(XmlUtil.getAttribute(node,
                    ATTR_NORTH, entry.getNorth() + "")));
            entry.setSouth(Misc.decodeLatLon(XmlUtil.getAttribute(node,
                    ATTR_SOUTH, entry.getSouth() + "")));
            entry.setEast(Misc.decodeLatLon(XmlUtil.getAttribute(node,
                    ATTR_EAST, entry.getEast() + "")));
            entry.setWest(Misc.decodeLatLon(XmlUtil.getAttribute(node,
                    ATTR_WEST, entry.getWest() + "")));
        }

        entry.setAltitudeTop(XmlUtil.getAttribute(node, ATTR_ALTITUDE_TOP,
                entry.getAltitudeTop()));
        entry.setAltitudeBottom(XmlUtil.getAttribute(node,
                ATTR_ALTITUDE_BOTTOM, entry.getAltitudeBottom()));
        entry.setAltitudeTop(XmlUtil.getAttribute(node, ATTR_ALTITUDE,
                entry.getAltitudeTop()));
        entry.setAltitudeBottom(XmlUtil.getAttribute(node, ATTR_ALTITUDE,
                entry.getAltitudeBottom()));

        NodeList entryChildren = XmlUtil.getElements(node);
        for (Element entryChild : (List<Element>) entryChildren) {
            String tag = entryChild.getTagName();
            if (tag.equals(TAG_METADATA)) {
                getMetadataManager().processMetadataXml(entry, entryChild,
                        files, internal);
            } else if (tag.equals(TAG_DESCRIPTION)) {}
            else {
                //                throw new IllegalArgumentException("Unknown tag:"
                //                        + node.getTagName());
            }
        }
        entry.getTypeHandler().initializeEntryFromXml(request, entry, node);

        return entry;

    }








    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String getEntryLink(Request request, Entry entry) {
        return getEntryLink(request, entry, new ArrayList());
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param args _more_
     *
     * @return _more_
     */
    public String getEntryLink(Request request, Entry entry, List args) {
        return HtmlUtils.href(
            request.entryUrl(getRepository().URL_ENTRY_SHOW, entry, args),
            entry.getLabel());
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String getEntryURL(Request request, Entry entry) {
        return request.entryUrl(getRepository().URL_ENTRY_SHOW, entry);
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param linkText _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public EntryLink getAjaxLink(Request request, Entry entry,
                                 String linkText)
            throws Exception {
        return getAjaxLink(request, entry, linkText, null);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param linkText _more_
     * @param url _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public EntryLink getAjaxLink(Request request, Entry entry,
                                 String linkText, String url)
            throws Exception {
        return getAjaxLink(request, entry, linkText, url, true);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param linkText _more_
     * @param url _more_
     * @param forTreeNavigation _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public EntryLink getAjaxLink(Request request, Entry entry,
                                 String linkText, String url,
                                 boolean forTreeNavigation)
            throws Exception {
        return getAjaxLink(request, entry, linkText, url, forTreeNavigation,
                           null);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param linkText _more_
     * @param url _more_
     * @param forTreeNavigation _more_
     * @param textBeforeEntryLink _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public EntryLink getAjaxLink(Request request, Entry entry,
                                 String linkText, String url,
                                 boolean forTreeNavigation,
                                 String textBeforeEntryLink)
            throws Exception {
        return getAjaxLink(request, entry, linkText, url, forTreeNavigation,
                           textBeforeEntryLink,
                           request.get(ARG_DECORATE, true));
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param linkText _more_
     * @param url _more_
     * @param forTreeNavigation _more_
     * @param textBeforeEntryLink _more_
     * @param decorateMetadata _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public EntryLink getAjaxLink(Request request, Entry entry,
                                 String linkText, String url,
                                 boolean forTreeNavigation,
                                 String textBeforeEntryLink,
                                 boolean decorateMetadata)
            throws Exception {

        if (url == null) {
            url = request.entryUrl(getRepository().URL_ENTRY_SHOW, entry);
        }

        boolean forTreeView = request.get(ARG_TREEVIEW, false);
        if (forTreeView) {
            String label = getEntryListName(request, entry);
            url   = url.replace("%27", "'");
            url   = url.replace("'", "");
            label = label.replace("'", "\\'");
            url = "javascript:"
                  + HtmlUtils.call("treeViewClick",
                                   HtmlUtils.jsMakeArgs(new String[] {
                                       entry.getId(),
                                       url, label }, true));
        }
        boolean      showLink    = request.get(ARG_SHOWLINK, true);
        boolean      showDetails = request.get(ARG_DETAILS, true);

        StringBuffer sb          = new StringBuffer();
        String       entryId     = entry.getId();

        String       uid         = HtmlUtils.getUniqueId("link_");
        String       output      = "inline";

        String folderClickUrl =
            request.entryUrl(getRepository().URL_ENTRY_SHOW, entry) + "&"
            + HtmlUtils.args(new String[] {
            ARG_OUTPUT, output, ARG_DETAILS, "" + showDetails, ARG_SHOWLINK,
            "" + showLink
        });



        if (forTreeView) {
            folderClickUrl += "&" + ARG_TREEVIEW + "=true";
        }

        String  targetId = "targetspan_" + HtmlUtils.blockCnt++;

        boolean okToMove = !request.getUser().getAnonymous();



        String  compId   = "popup_" + HtmlUtils.blockCnt++;
        String  linkId   = "img_" + uid;
        String  prefix   = "";

        if (forTreeNavigation) {
            boolean showArrow = entry.isGroup() || true;
            String  message   = entry.isGroup()
                                ? "Click to open folder"
                                : "Click to view contents";
            if (showArrow) {
                prefix = HtmlUtils.img(
                    getRepository().iconUrl(ICON_TOGGLEARROWRIGHT),
                    msg(message),
                    HtmlUtils.id("img_" + uid)
                    + HtmlUtils.onMouseClick(
                        HtmlUtils.call(
                            "folderClick",
                            HtmlUtils.comma(
                                HtmlUtils.squote(uid),
                                HtmlUtils.squote(folderClickUrl),
                                HtmlUtils.squote(
                                    iconUrl(ICON_TOGGLEARROWDOWN))))));
            } else {
                prefix = HtmlUtils.img(getRepository().iconUrl(ICON_BLANK),
                                       "",
                                       HtmlUtils.attr(HtmlUtils.ATTR_WIDTH,
                                           "10"));
            }
            prefix = HtmlUtils.span(prefix,
                                    HtmlUtils.cssClass("entry-arrow"));

        }


        StringBuffer sourceEvent = new StringBuffer();
        StringBuffer targetEvent = new StringBuffer();
        String       entryIcon   = getPageHandler().getIconUrl(request,
                                       entry);
        String       iconId      = "img_" + uid;
        if (okToMove) {
            if (forTreeNavigation) {
                targetEvent.append(
                    HtmlUtils.onMouseOver(
                        HtmlUtils.call(
                            "mouseOverOnEntry",
                            HtmlUtils.comma(
                                "event", HtmlUtils.squote(entry.getId()),
                                HtmlUtils.squote(targetId)))));


                targetEvent.append(
                    HtmlUtils.onMouseUp(
                        HtmlUtils.call(
                            "mouseUpOnEntry",
                            HtmlUtils.comma(
                                "event", HtmlUtils.squote(entry.getId()),
                                HtmlUtils.squote(targetId)))));
            }
            targetEvent.append(
                HtmlUtils.onMouseOut(
                    HtmlUtils.call(
                        "mouseOutOnEntry",
                        HtmlUtils.comma(
                            "event", HtmlUtils.squote(entry.getId()),
                            HtmlUtils.squote(targetId)))));

            sourceEvent.append(
                HtmlUtils.onMouseDown(
                    HtmlUtils.call(
                        "mouseDownOnEntry",
                        HtmlUtils.comma(
                            "event", HtmlUtils.squote(entry.getId()),
                            HtmlUtils.squote(
                                entry.getLabel().replace(
                                    "'", "")), HtmlUtils.squote(iconId),
                                        HtmlUtils.squote(entryIcon)))));





        }


        String imgText = (okToMove
                          ? msg("Drag to move") + "; "
                          : "");
        String imgUrl  = null;
        if (entry.getResource().isUrl()) {
            imgUrl  = entry.getTypeHandler().getResourcePath(request, entry);
            imgText += msg("Click to view URL");
        } else if (entry.getResource().isFile()) {
            if (getAccessManager().canDownload(request, entry)) {
                imgUrl = entry.getTypeHandler().getEntryResourceUrl(request,
                        entry);
                imgText += msg("Click to download file");
            }
        }


        String img = HtmlUtils.img(entryIcon, (okToMove
                ? imgText
                : ""), HtmlUtils.id(iconId) + sourceEvent);

        if (imgUrl != null) {
            img = HtmlUtils.href(imgUrl, img);
        }
        img = prefix + img;

        sb.append(img);

        sb.append(HtmlUtils.space(1));
        if (textBeforeEntryLink != null) {
            sb.append(textBeforeEntryLink);
        }
        if (decorateMetadata) {
            getMetadataManager().decorateEntry(request, entry, sb, true);
        }

        if (showLink) {
            sb.append(HtmlUtils.span(getTooltipLink(request, entry, linkText,
                    url), HtmlUtils.cssClass("entry-link")));
        } else {
            sb.append(HtmlUtils.span(linkText,
                                     targetEvent.toString()
                                     + HtmlUtils.cssClass("entry-link")));
        }

        String link = HtmlUtils.span(sb.toString(),
                                     HtmlUtils.id(targetId) + targetEvent);


        String folderBlock = ( !forTreeNavigation
                               ? ""
                               : HtmlUtils.div("",
                                   HtmlUtils.attrs(HtmlUtils.ATTR_STYLE,
                                       "display:none;", HtmlUtils.ATTR_CLASS,
                                       CSS_CLASS_FOLDER_BLOCK,
                                       HtmlUtils.ATTR_ID, uid)));

        return new EntryLink(link, folderBlock, uid);
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param linkText _more_
     * @param url _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getTooltipLink(Request request, Entry entry,
                                 String linkText, String url)
            throws Exception {
        if (url == null) {
            url = request.entryUrl(getRepository().URL_ENTRY_SHOW, entry);
        }

        String elementId  = entry.getId();
        String qid        = HtmlUtils.squote(elementId);
        String linkId     = "link_" + (HtmlUtils.blockCnt++);
        String qlinkId    = HtmlUtils.squote(linkId);

        String target     = (request.defined(ARG_TARGET)
                             ? request.getString(ARG_TARGET, "")
                             : null);
        String targetAttr = ((target != null)
                             ? HtmlUtils.attr(HtmlUtils.ATTR_TARGET, target)
                             : "");

        return HtmlUtils.href(url, linkText,
                              HtmlUtils.id(linkId) + targetAttr);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Link> getEntryLinks(Request request, Entry entry)
            throws Exception {
        List<Link>          links = new ArrayList<Link>();
        OutputHandler.State state = new OutputHandler.State(entry);
        entry.getTypeHandler().getEntryLinks(request, entry, links);
        links.addAll(getRepository().getOutputLinks(request, state));

        return links;
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param file _more_
     * @param andInsert _more_
     *
     * @throws Exception _more_
     */
    public void addAttachment(Entry entry, File file, boolean andInsert)
            throws Exception {
        String theFile = getStorageManager().moveToEntryDir(entry,
                             file).getName();
        entry.addMetadata(
            new Metadata(
                getRepository().getGUID(), entry.getId(),
                ContentMetadataHandler.TYPE_ATTACHMENT, false, theFile, "",
                "", "", ""));
        if (andInsert) {
            updateEntry(null, entry);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param typeMask _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getEntryActionsTable(Request request, Entry entry,
                                       int typeMask)
            throws Exception {
        List<Link> links = getEntryLinks(request, entry);

        return getEntryActionsTable(request, entry, typeMask, links);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param typeMask _more_
     * @param links _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getEntryActionsTable(Request request, Entry entry,
                                       int typeMask, List<Link> links)
            throws Exception {
        return getEntryActionsTable(request, entry, typeMask, links, false,
                                    null);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param typeMask _more_
     * @param links _more_
     * @param return NullIfNoneMatch _more_
     * @param returnNullIfNoneMatch _more_
     * @param header _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getEntryActionsTable(Request request, Entry entry,
                                       int typeMask, List<Link> links,
                                       boolean returnNullIfNoneMatch,
                                       String header)
            throws Exception {




        StringBuffer
            htmlSB          = null,
            exportSB        = null,
            nonHtmlSB       = null,
            actionSB        = null,
            categorySB      = null,
            fileSB          = null;
        int     cnt         = 0;
        boolean needToAddHr = false;
        String  tableHeader = "<table cellspacing=\"0\" cellpadding=\"0\">";
        for (Link link : links) {
            if ( !link.isType(typeMask)) {
                continue;
            }
            StringBuffer sb;
            if (link.isType(OutputType.TYPE_VIEW)) {
                if (htmlSB == null) {
                    htmlSB = new StringBuffer(tableHeader);
                    //                    htmlSB.append("<tr><td class=entrymenulink>" + msg("View") +"</td></tr>");
                    cnt++;
                }
                sb = htmlSB;
                //} else if (link.isType(OutputType.TYPE_FEEDS)) {
                //if (nonHtmlSB == null) {
            } else if (link.isType(OutputType.TYPE_FEEDS)) {
                if (exportSB == null) {
                    cnt++;
                    exportSB = new StringBuffer(tableHeader);
                }
                sb = exportSB;
            } else if (link.isType(OutputType.TYPE_FILE)) {
                if (fileSB == null) {
                    cnt++;
                    fileSB = new StringBuffer(tableHeader);
                }
                sb = fileSB;
            } else if (link.isType(OutputType.TYPE_OTHER)) {
                if (categorySB == null) {
                    cnt++;
                    categorySB = new StringBuffer(tableHeader);
                }
                sb = categorySB;
            } else {
                if (actionSB == null) {
                    cnt++;
                    actionSB = new StringBuffer(tableHeader);
                    //                    actionSB.append("<tr><td class=entrymenulink>" + msg("Edit") +"</td></tr>");
                }
                sb = actionSB;
            }
            //Only add the hr if we have more things in the list
            if (needToAddHr && (sb.length() > tableHeader.length())) {
                sb.append("<tr><td colspan=2><hr "
                          + HtmlUtils.cssClass(CSS_CLASS_MENUITEM_SEPARATOR)
                          + "></td></tr>");
            }
            needToAddHr = link.getHr();
            if (needToAddHr) {
                continue;
            }

            sb.append(HtmlUtils
                .open(HtmlUtils.TAG_TR,
                      HtmlUtils.cssClass(CSS_CLASS_MENUITEM_ROW)) + HtmlUtils
                          .open(HtmlUtils.TAG_TD) + HtmlUtils
                          .open(HtmlUtils.TAG_DIV,
                                HtmlUtils.cssClass(CSS_CLASS_MENUITEM_TD)));
            if (link.getIcon() == null) {
                sb.append(HtmlUtils.space(1));
            } else {
                sb.append(HtmlUtils.href(link.getUrl(),
                                         HtmlUtils.img(link.getIcon())));
            }
            sb.append(HtmlUtils.space(1));
            sb.append("</div></td><td><div "
                      + HtmlUtils.cssClass(CSS_CLASS_MENUITEM_TD) + ">");
            sb.append(
                HtmlUtils.href(
                    link.getUrl(), msg(link.getLabel()),
                    HtmlUtils.cssClass(CSS_CLASS_MENUITEM_LINK)));
            sb.append("</div></td></tr>");
        }

        if (returnNullIfNoneMatch && (cnt == 0)) {
            return null;
        }


        StringBuffer menu = new StringBuffer();
        if (header != null) {
            menu.append(
                HtmlUtils.div(
                    header, HtmlUtils.cssClass("ramadda-entry-menu-title")));
        }
        menu.append("<table cellspacing=\"0\" cellpadding=\"4\">");
        menu.append(HtmlUtils.open(HtmlUtils.TAG_TR,
                                   HtmlUtils.attr(HtmlUtils.ATTR_VALIGN,
                                       "top")));
        if (fileSB != null) {
            fileSB.append("</table>");
            menu.append(HtmlUtils.tag(HtmlUtils.TAG_TD, "",
                                      HtmlUtils.b(msg("File")) + "<br>"
                                      + fileSB.toString()));
        }
        if (actionSB != null) {
            actionSB.append("</table>");
            menu.append(HtmlUtils.tag(HtmlUtils.TAG_TD, "",
                                      HtmlUtils.b(msg("Edit")) + "<br>"
                                      + actionSB.toString()));
        }

        if (htmlSB != null) {
            htmlSB.append("</table>");
            menu.append(HtmlUtils.tag(HtmlUtils.TAG_TD, "",
                                      HtmlUtils.b(msg("View")) + "<br>"
                                      + htmlSB.toString()));
        }


        if (exportSB != null) {
            exportSB.append("</table>");
            menu.append(HtmlUtils.tag(HtmlUtils.TAG_TD, "",
                                      HtmlUtils.b(msg("Links")) + "<br>"
                                      + exportSB.toString()));
        }

        if (categorySB != null) {
            categorySB.append("</table>");
            menu.append(HtmlUtils.tag(HtmlUtils.TAG_TD, "",
                                      HtmlUtils.b(msg("Data")) + "<br>"
                                      + categorySB.toString()));
        }

        menu.append(HtmlUtils.close(HtmlUtils.TAG_TR));
        menu.append(HtmlUtils.close(HtmlUtils.TAG_TABLE));

        return menu.toString();


    }







    /**
     *     _more_
     *
     *     @param request _more_
     *     @param entry _more_
     *
     *     @return _more_
     *
     *     @throws Exception _more_
     */
    public Entry getParent(Request request, Entry entry) throws Exception {
        return getEntry(request, entry.getParentEntryId());
    }



    /**
     * _more_
     *
     *
     * @param entryId _more_
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry getEntry(Request request, String entryId) throws Exception {
        return getEntry(request, entryId, true);
    }


    /**
     * _more_
     *
     * @param entryId _more_
     * @param request _more_
     * @param andFilter _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry getEntry(Request request, String entryId, boolean andFilter)
            throws Exception {
        return getEntry(request, entryId, andFilter, false);
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
    public Entry getEntry(Request request) throws Exception {
        return getEntryFromArg(request, ARG_ENTRYID);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param urlArg _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry getEntryFromArg(Request request, String urlArg)
            throws Exception {
        String entryId = request.getString(urlArg, BLANK);
        Entry  entry   = getEntry(request, entryId);
        if (entry == null) {
            entry = findEntryFromName(request, entryId, request.getUser(),
                                      false);
        }

        if (entry == null) {
            Entry tmp = getEntry(request, request.getString(urlArg, BLANK),
                                 false);
            if (tmp != null) {
                logInfo("Cannot access entry:" + entryId + "  IP:"
                        + request.getIp());

                logInfo("Request:" + request);

                throw new AccessException(
                    "You do not have access to this entry", request);
            }

            throw new RepositoryUtil.MissingEntryException(
                "Could not find entry:" + request.getString(urlArg, BLANK));
        }

        if (entry != null) {
            getSessionManager().setLastEntry(request, entry);
        }

        return entry;
    }




    /**
     * _more_
     *
     * @param server _more_
     * @param id _more_
     *
     * @return _more_
     */
    public String getRemoteEntryId(String server, String id) {
        return ID_PREFIX_REMOTE
               + RepositoryUtil.encodeBase64(server.getBytes()) + ":" + id;
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public String[] getRemoteEntryInfo(String id) {
        if (id.length() == 0) {
            return new String[] { "", "" };
        }
        id = id.substring(ID_PREFIX_REMOTE.length());
        String[] pair = StringUtil.split(id, ":", 2);
        if (pair == null) {
            return new String[] { "", "" };
        }
        pair[0] = new String(RepositoryUtil.decodeBase64(pair[0]));

        return pair;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param server _more_
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry getRemoteEntry(Request request, String server, String id)
            throws Exception {
        String remoteUrl = server + getRepository().URL_ENTRY_SHOW.getPath();
        remoteUrl = HtmlUtils.url(remoteUrl, ARG_ENTRYID, id, ARG_OUTPUT,
                                  XmlOutputHandler.OUTPUT_XMLENTRY);
        String entriesXml = getStorageManager().readSystemResource(remoteUrl);

        return null;
    }


    /**
     * If this entry is a harvested or local file (i.e., it is not a stored file
     * in the repository's own storage area) then check its file date. If its greater than the entry's date then change the entry.
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    private void checkEntryFileTime(Entry entry) throws Exception {

        if (true) {
            return;
        }


        /**
         *     Don't do this because many harvested files get a date range set for them
         *
         * if ((entry == null) || !entry.isFile()) {
         *   return;
         * }
         *
         * if (entry.getResource().isStoredFile()) {
         *   return;
         * }
         * File f = entry.getResource().getTheFile();
         * if ((f == null) || !f.exists()) {
         *   return;
         * }
         * if (entry.getStartDate() != entry.getEndDate()) {
         *   return;
         * }
         *
         * long fileTime = f.lastModified();
         * if (fileTime == entry.getStartDate()) {
         *   return;
         * }
         * entry.setStartDate(fileTime);
         * entry.setEndDate(fileTime);
         * updateEntry(null, entry);
         */
    }


    /**
     * _more_
     *
     * @param entryId _more_
     * @param request _more_
     * @param andFilter _more_
     * @param abbreviated _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */

    public Entry getEntry(Request request, String entryId, boolean andFilter,
                          boolean abbreviated)
            throws Exception {

        if (entryId == null) {
            debug("getEntry: id is null ");

            return null;
        }
        Entry topGroup = getTopGroup();
        if (entryId.equals(topGroup.getId()) || entryId.equals(ID_ROOT)) {
            debug("getEntry: returning top group");

            return topGroup;
        }

        //        synchronized (MUTEX_ENTRY) {
        Entry entry = getEntryFromCache(entryId);
        if (entry != null) {
            debug("getEntry: from cache:" + entry);
            checkEntryFileTime(entry);
            if ( !andFilter) {
                return entry;
            }
            entry = getAccessManager().filterEntry(request, entry);
            debug("getEntry: after filter:" + entry);

            return entry;
        }

        try {
            if (entryId.startsWith(ID_PREFIX_REMOTE)) {
                String[] tuple = getRemoteEntryInfo(entryId);

                return getRemoteEntry(request, tuple[0], tuple[1]);
            } else if (isSynthEntry(entryId)) {
                String[] pair          = getSynthId(entryId);
                String   parentEntryId = pair[0];
                String   syntheticPart = pair[1];
                Entry    parentEntry   = null;
                //                System.err.println("Parent:" + parentEntryId +" synth part:" + syntheticPart);

                TypeHandler typeHandler = null;

                if (parentEntryId.equals(ENTRYID_PROCESS)) {
                    parentEntry = getProcessEntry();
                    typeHandler = parentEntry.getTypeHandler();
                    if (syntheticPart == null) {
                        return parentEntry;
                    }
                }

                if (parentEntry == null) {
                    parentEntry = getEntry(request, parentEntryId, andFilter,
                                           abbreviated);
                }


                if (parentEntry == null) {
                    return null;
                }
                if (typeHandler == null) {
                    typeHandler = parentEntry.getTypeHandler();
                }



                entry = typeHandler.makeSynthEntry(request, parentEntry,
                        syntheticPart);
                //                System.err.println("process parent:" + parentEntry);
                //                System.err.println("process child:" + entry);

                if (entry == null) {
                    return null;
                }
            } else {
                entry = createEntryFromDatabase(entryId, abbreviated);
                debug("getEntry: from database:" + entry);
                /*
                for(int i=0;i<1000000;i++) {
                    entry = createEntryFromDatabase(entryId, abbreviated);
                    if ((i%10000)==0) {
                        Misc.gc();
                        getRepository().checkMemory("GC:" + i +" memory:");
                    }
                }
                */
            }
        } catch (Exception exc) {
            logError("creating entry:" + entryId, exc);

            return null;
        }

        if ( !abbreviated && (entry != null)) {
            cacheEntry(entry);
        }

        if (andFilter && (entry != null)) {
            entry = getAccessManager().filterEntry(request, entry);
            debug("getEntry: after filter 2:" + entry);
        }

        return entry;
        //    }

    }


    /**
     * _more_
     *
     * @param entryId _more_
     * @param abbreviated _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Entry createEntryFromDatabase(String entryId, boolean abbreviated)
            throws Exception {
        Entry entry = null;
        Statement entryStmt =
            getDatabaseManager().select(Tables.ENTRIES.COLUMNS,
                                        Tables.ENTRIES.NAME,
                                        Clause.eq(Tables.ENTRIES.COL_ID,
                                            entryId));
        try {
            ResultSet results = entryStmt.getResultSet();
            if ( !results.next()) {
                return null;
            }
            String entryType = results.getString(2);
            TypeHandler typeHandler =
                getRepository().getTypeHandler(entryType);
            entry = typeHandler.createEntryFromDatabase(results, abbreviated);
            checkEntryFileTime(entry);
        } finally {
            getDatabaseManager().closeAndReleaseConnection(entryStmt);
        }

        return entry;
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
    public List[] getEntries(Request request) throws Exception {
        return getEntries(request, new StringBuffer());
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param searchCriteriaSB _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List[] getEntries(Request request, StringBuffer searchCriteriaSB)
            throws Exception {
        return getEntries(request, searchCriteriaSB, null);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param searchCriteriaSB _more_
     * @param extraClauses _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List[] getEntries(Request request, StringBuffer searchCriteriaSB,
                             List<Clause> extraClauses)
            throws Exception {
        TypeHandler typeHandler = getRepository().getTypeHandler(request);
        List<Clause> where = typeHandler.assembleWhereClause(request,
                                 searchCriteriaSB);



        if (extraClauses != null) {
            where.addAll(extraClauses);
        }

        return getEntries(request, where, typeHandler);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param clauses _more_
     * @param typeHandler _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry>[] getEntries(Request request, List<Clause> clauses,
                                    TypeHandler typeHandler)
            throws Exception {
        int skipCnt = request.get(ARG_SKIP, 0);
        SqlUtil.debug = false;
        List<Entry> entries       = new ArrayList<Entry>();
        List<Entry> groups        = new ArrayList<Entry>();
        boolean canDoSelectOffset = getDatabaseManager().canDoSelectOffset();
        Hashtable   seen          = new Hashtable();
        List<Entry> allEntries    = new ArrayList<Entry>();


        Statement statement =
            typeHandler.select(request, Tables.ENTRIES.COLUMNS, clauses,
                               getRepository().getQueryOrderAndLimit(request,
                                   false));

        ResultSet        results;
        SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);

        long             t1   = System.currentTimeMillis();
        try {
            while ((results = iter.getNext()) != null) {
                if ( !canDoSelectOffset && (skipCnt-- > 0)) {
                    continue;
                }
                String id    = results.getString(1);
                Entry  entry = getEntryFromCache(id);
                if (entry == null) {
                    //id,type,name,desc,group,user,file,createdata,fromdate,todate
                    TypeHandler localTypeHandler =
                        getRepository().getTypeHandler(results.getString(2));
                    entry = localTypeHandler.createEntryFromDatabase(results);
                    cacheEntry(entry);
                }
                if (seen.get(entry.getId()) != null) {
                    continue;
                }
                seen.put(entry.getId(), BLANK);
                allEntries.add(entry);
            }
        } finally {
            long t2 = System.currentTimeMillis();
            if ((t2 - t1) > 60 * 1000) {
                getLogManager().logError("Select took a long time:"
                                         + (t2 - t1));
            }
            getDatabaseManager().closeAndReleaseConnection(statement);
        }


        for (Entry entry : allEntries) {
            if (entry.isGroup()) {
                groups.add(entry);
            } else {
                entries.add(entry);
            }
        }

        entries = getAccessManager().filterEntries(request, entries);
        groups  = getAccessManager().filterEntries(request, groups);

        return (List<Entry>[]) new List[] { groups, entries };
    }



    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public boolean isSynthEntry(String id) {
        return id.startsWith(ID_PREFIX_SYNTH);
    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public String[] getSynthId(String id) {
        id = id.substring(ID_PREFIX_SYNTH.length());
        String[] pair = StringUtil.split(id, ":", 2);
        if (pair == null) {
            return new String[] { id, null };
        }

        return pair;
    }



    /**
     * _more_
     */
    public void clearSeenResources() {
        seenResources = new HashSet();
    }




    /** _more_ */
    private HashSet seenResources = new HashSet();



    /**
     * _more_
     *
     * @param harvester _more_
     * @param typeHandler _more_
     * @param entries _more_
     * @param makeThemUnique _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean processEntries(Harvester harvester,
                                  TypeHandler typeHandler,
                                  List<Entry> entries, boolean makeThemUnique)
            throws Exception {
        if (makeThemUnique) {
            entries = getUniqueEntries(entries);
        }
        addNewEntries(null, entries);

        return true;
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param newFile _more_
     * @param group _more_
     * @param name _more_
     * @param user _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry addFileEntry(Request request, File newFile, Entry group,
                              String name, User user)
            throws Exception {
        return addFileEntry(request, newFile, group, name, user, null, null);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param newFile _more_
     * @param group _more_
     * @param name _more_
     * @param user _more_
     * @param typeHandler _more_
     * @param initializer _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry addFileEntry(Request request, File newFile, Entry group,
                              String name, User user,
                              TypeHandler typeHandler,
                              EntryInitializer initializer)
            throws Exception {
        String resourceType;

        //Is it a ramadda managed file?
        if (IOUtil.isADescendent(getStorageManager().getRepositoryDir(),
                                 newFile)) {
            resourceType = Resource.TYPE_STOREDFILE;
        } else {
            resourceType = Resource.TYPE_LOCAL_FILE;
        }


        if ( !getRepository().getAccessManager().canDoAction(request, group,
                Permission.ACTION_NEW)) {
            throw new AccessException("Cannot add to folder", request);
        }

        if (typeHandler == null) {
            typeHandler =
                getRepository().getTypeHandler(TypeHandler.TYPE_FILE);
        }

        Entry    entry    =
            typeHandler.createEntry(getRepository().getGUID());



        Resource resource = new Resource(newFile.toString(), resourceType);
        Date     dttm     = new Date();
        entry.initEntry(name, "", group, request.getUser(), resource, "",
                        dttm.getTime(), dttm.getTime(), dttm.getTime(),
                        dttm.getTime(), null);

        typeHandler.initializeNewEntry(entry);
        if (initializer != null) {
            initializer.initEntry(entry);
        }
        addNewEntry(request, entry);

        return entry;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param file _more_
     * @param entry _more_
     * @param associatedEntry _more_
     * @param associationType _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processEntryPublish(Request request, File file,
                                      Entry entry, Entry associatedEntry,
                                      String associationType)
            throws Exception {
        Entry parent = getEntryManager().findGroup(request,
                           request.getString(ARG_PUBLISH_ENTRY + "_hidden",
                                             ""));
        if (parent == null) {
            return new Result(
                "",
                new StringBuffer(
                    getPageHandler().showDialogError(
                        msg("Could not find folder"))));
        }

        if ( !canAddTo(request, parent)) {
            throw new IllegalArgumentException(
                "No permissions to add new entry");
        }

        File newFile =
            getRepository().getStorageManager().moveToStorage(request, file);

        TypeHandler typeHandler = findDefaultTypeHandler(newFile.toString());
        if (typeHandler == null) {
            typeHandler =
                getRepository().getTypeHandler(TypeHandler.TYPE_FILE);
        }

        Entry newEntry = ((entry != null)
                          ? entry
                          : new Entry(typeHandler, false));


        newEntry.setParentEntry(parent);
        newEntry.setResource(new Resource(newFile, Resource.TYPE_STOREDFILE));
        newEntry.setId(getRepository().getGUID());
        newEntry.setName(request.getString(ARG_PUBLISH_NAME,
                                           "subset_" + newEntry.getName()));
        newEntry.clearMetadata();
        newEntry.setUser(request.getUser());
        if (request.get(ARG_METADATA_ADD, false)) {
            newEntry.clearArea();
            List<Entry> entries = (List<Entry>) Misc.newList(newEntry);
            getEntryManager().addInitialMetadata(request, entries, false,
                    request.get(ARG_SHORT, false));
        }
        addNewEntry(request, newEntry);
        if ((associatedEntry != null)
                && !isSynthEntry(associatedEntry.getId())) {
            getRepository().addAuthToken(request);
            getAssociationManager().addAssociation(request, associatedEntry,
                    newEntry, "", associationType);
        }

        return new Result(request.entryUrl(getRepository().URL_ENTRY_FORM,
                                           newEntry));

    }


    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     * @param columnSize _more_
     *
     * @throws IllegalArgumentException _more_
     */
    public void checkColumnSize(String name, String value, int columnSize)
            throws IllegalArgumentException {
        if (value.length() > columnSize) {
            throw new IllegalArgumentException(name + " size:"
                    + value.length() + " is greater than column size:"
                    + columnSize);
        }
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param statement _more_
     * @param isNew _more_
     * @param typeHandler _more_
     *
     * @throws Exception _more_
     */
    private void setStatement(Entry entry, PreparedStatement statement,
                              boolean isNew, TypeHandler typeHandler)
            throws Exception {
        String description = entry.getDescription();
        checkColumnSize("name", entry.getName(), MAX_NAME_LENGTH);
        checkColumnSize("description", description, MAX_DESCRIPTION_LENGTH);


        int col = 1;
        //id,type,name,desc,group,user,file,createdata,fromdate,todate
        statement.setString(col++, entry.getId());
        statement.setString(col++, typeHandler.getType());
        statement.setString(col++, entry.getName());




        statement.setString(col++, description);
        statement.setString(col++, entry.getParentEntryId());
        //        statement.setString(col++, entry.getCollectionGroupId());
        statement.setString(col++, entry.getUser().getId());
        if (entry.getResource() == null) {
            entry.setResource(new Resource());
        }
        statement.setString(
            col++,
            getStorageManager().resourceToDB(entry.getResource().getPath()));
        statement.setString(col++, entry.getResource().getType());
        statement.setString(col++, entry.getResource().getMd5());
        statement.setLong(col++, entry.getResource().getFileSize());
        statement.setString(col++, entry.getCategory());
        //create date
        getDatabaseManager().setDate(statement, col++, entry.getCreateDate());

        long updateTime = entry.getChangeDate();
        getDatabaseManager().setDate(statement, col++, updateTime);
        try {
            getDatabaseManager().setDate(statement, col,
                                         entry.getStartDate());
            getDatabaseManager().setDate(statement, col + 1,
                                         entry.getEndDate());
        } catch (Exception exc) {
            getLogManager().logError("Error: Bad date " + entry.getResource()
                                     + " " + new Date(entry.getStartDate()));
            getDatabaseManager().setDate(statement, col, new Date());
            getDatabaseManager().setDate(statement, col + 1, new Date());
        }
        col += 2;


        //This cleans  up bad double values
        getDatabaseManager().setDouble(statement, col++, entry.getSouth(),
                                       Entry.NONGEO);
        getDatabaseManager().setDouble(statement, col++, entry.getNorth(),
                                       Entry.NONGEO);
        getDatabaseManager().setDouble(statement, col++, entry.getEast(),
                                       Entry.NONGEO);
        getDatabaseManager().setDouble(statement, col++, entry.getWest(),
                                       Entry.NONGEO);
        getDatabaseManager().setDouble(statement, col++,
                                       entry.getAltitudeTop(), Entry.NONGEO);
        getDatabaseManager().setDouble(statement, col++,
                                       entry.getAltitudeBottom(),
                                       Entry.NONGEO);

        if ( !isNew) {
            statement.setString(col++, entry.getId());
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void updateEntry(Request request, Entry entry) throws Exception {
        List<Entry> tmp = new ArrayList<Entry>();
        tmp.add(entry);
        updateEntries(request, tmp);
    }



    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_b
     */
    public void addNewEntry(Request request, Entry entry) throws Exception {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);
        addNewEntries(request, entries);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     *
     * @throws Exception _more_
     */
    public void addNewEntries(Request request, List<Entry> entries)
            throws Exception {
        insertEntries(entries, true);
        for (Entry theNewEntry : entries) {
            theNewEntry.getTypeHandler().doFinalEntryInitialization(request,
                    theNewEntry);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     *
     * @throws Exception _more_
     */
    public void updateEntries(Request request, List<Entry> entries)
            throws Exception {
        insertEntries(entries, false);
    }



    /**
     * _more_
     *
     * @param entries _more_
     * @param isNew _more_
     *
     * @throws Exception _more_
     */
    private void insertEntries(List<Entry> entries, boolean isNew)
            throws Exception {
        if (entries.size() == 0) {
            return;
        }

        //We have our own connection
        Connection connection = getDatabaseManager().getConnection();
        try {
            insertEntriesInner(entries, connection, isNew);
            if ( !isNew) {
                Misc.run(getRepository(), "checkModifiedEntries", entries);
            } else {}
        } finally {
            getDatabaseManager().closeConnection(connection);
        }
    }



    /**
     * _more_
     *
     * @param entries _more_
     * @param connection _more_
     * @param isNew _more_
     *
     * @throws Exception _more_
     */
    private void insertEntriesInner(List<Entry> entries,
                                    Connection connection, boolean isNew)
            throws Exception {

        if (entries.size() == 0) {
            return;
        }

        long              t1          = System.currentTimeMillis();
        int               cnt         = 0;
        int               metadataCnt = 0;


        PreparedStatement entryStmt   = connection.prepareStatement(isNew
                ? Tables.ENTRIES.INSERT
                : SqlUtil.makeUpdate(Tables.ENTRIES.NAME,
                                     Tables.ENTRIES.COL_ID,
                                     Tables.ENTRIES.ARRAY));

        PreparedStatement metadataStmt =
            connection.prepareStatement(Tables.METADATA.INSERT);


        Hashtable typeStatements = new Hashtable();
        int       batchCnt       = 0;
        connection.setAutoCommit(false);
        long updateTime = getRepository().currentTime();
        for (Entry entry : entries) {
            //Do we want to clear it from the cache???
            removeFromCache(entry);
            if ( !isNew) {
                entry.setChangeDate(updateTime);
            }

            TypeHandler          typeHandler = entry.getTypeHandler();
            List<TypeInsertInfo> typeInserts =
                new ArrayList<TypeInsertInfo>();
            typeHandler.getInsertSql(isNew, typeInserts);
            for (TypeInsertInfo tif : typeInserts) {
                PreparedStatement typeStatement =
                    (PreparedStatement) typeStatements.get(tif.getSql());
                if (typeStatement == null) {
                    typeStatement = connection.prepareStatement(tif.getSql());
                    typeStatements.put(tif.getSql(), typeStatement);
                }
                tif.setStatement(typeStatement);
            }
            //           System.err.println ("entry: " + entry.getId());
            setStatement(entry, entryStmt, isNew, typeHandler);
            batchCnt++;
            entryStmt.addBatch();

            for (TypeInsertInfo tif : typeInserts) {
                PreparedStatement typeStatement = tif.getStatement();
                batchCnt++;
                tif.getTypeHandler().setStatement(entry, typeStatement,
                        isNew);
                typeStatement.addBatch();
            }

            List<Metadata> metadataList = entry.getMetadata();
            if (metadataList != null) {
                if ( !isNew) {
                    getDatabaseManager().delete(Tables.METADATA.NAME,
                            Clause.eq(Tables.METADATA.COL_ENTRY_ID,
                                      entry.getId()));
                }
                for (Metadata metadata : metadataList) {
                    //                    System.err.println ("\tmetadata:" + metadata.getEntryId() +" " + metadata.getType() + " " + metadata.getAttr1());
                    int col = 1;
                    metadataCnt++;
                    metadataStmt.setString(col++, metadata.getId());
                    metadataStmt.setString(col++, entry.getId());
                    metadataStmt.setString(col++, metadata.getType());
                    metadataStmt.setInt(col++, metadata.getInherited()
                            ? 1
                            : 0);
                    metadataStmt.setString(col++, metadata.getAttr1());
                    metadataStmt.setString(col++, metadata.getAttr2());
                    metadataStmt.setString(col++, metadata.getAttr3());
                    metadataStmt.setString(col++, metadata.getAttr4());
                    metadataStmt.setString(col++, metadata.getExtra());
                    metadataStmt.addBatch();
                    batchCnt++;

                }
            }

            if (batchCnt > 1000) {
                //                    if(isNew)
                entryStmt.executeBatch();
                //                    else                        entryStmt.executeUpdate();
                if (metadataCnt > 0) {
                    metadataStmt.executeBatch();
                }
                for (Enumeration keys = typeStatements.keys();
                        keys.hasMoreElements(); ) {
                    PreparedStatement typeStatement =
                        (PreparedStatement) typeStatements.get(
                            keys.nextElement());
                    //                        if(isNew)
                    typeStatement.executeBatch();
                    //                        else                            typeStatement.executeUpdate();
                }
                batchCnt    = 0;
                metadataCnt = 0;
            }
        }
        if (batchCnt > 0) {
            entryStmt.executeBatch();
            metadataStmt.executeBatch();
            for (Enumeration keys = typeStatements.keys();
                    keys.hasMoreElements(); ) {
                Object key = keys.nextElement();
                PreparedStatement typeStatement =
                    (PreparedStatement) typeStatements.get(key);
                try {
                    typeStatement.executeBatch();
                } catch (Exception exc) {
                    System.err.println("ERROR: " + key);

                    throw exc;
                }
            }
        }
        connection.commit();
        connection.setAutoCommit(true);


        long t2 = System.currentTimeMillis();
        totalTime    += (t2 - t1);
        totalEntries += entries.size();
        for (Entry entry : entries) {
            Entry parentEntry = entry.getParentEntry();
            if (parentEntry != null) {
                parentEntry.getTypeHandler().childEntryChanged(entry, isNew);
            }
        }

        if (t2 > t1) {
            //System.err.println("added:" + entries.size() + " entries in " + (t2-t1) + " ms  Rate:" + (entries.size()/(t2-t1)));
            double seconds = totalTime / 1000.0;
            //            if ((totalEntries % 100 == 0) && (seconds > 0)) {
            if (seconds > 0) {
                //                System.err.println(totalEntries + " average rate:"
                //                 + (int) (totalEntries / seconds)
                //                 + "/second");
            }
        }

        getDatabaseManager().closeStatement(entryStmt);
        getDatabaseManager().closeStatement(metadataStmt);
        for (Enumeration keys =
                typeStatements.keys(); keys.hasMoreElements(); ) {
            PreparedStatement typeStatement =
                (PreparedStatement) typeStatements.get(keys.nextElement());
            getDatabaseManager().closeStatement(typeStatement);
        }


        Misc.run(getRepository(), "checkNewEntries", entries);
    }





    /** _more_ */
    long totalTime = 0;

    /** _more_ */
    int totalEntries = 0;



    /**
     * _more_
     *
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry> getUniqueEntries(List<Entry> entries)
            throws Exception {
        return getUniqueEntries(entries, new ArrayList<Entry>());
    }

    /**
     * _more_
     *
     * @param entries _more_
     * @param nonUniqueOnes _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */

    public List<Entry> getUniqueEntries(List<Entry> entries,
                                        List<Entry> nonUniqueOnes)
            throws Exception {
        List<Entry> needToAdd = new ArrayList();
        String      query     = BLANK;
        try {
            if (entries.size() == 0) {
                return needToAdd;
            }
            if (seenResources.size() > 10000) {
                seenResources = new HashSet();
            }
            Connection connection = getDatabaseManager().getConnection();
            PreparedStatement select =
                SqlUtil.getSelectStatement(
                    connection, "count(" + Tables.ENTRIES.COL_ID + ")",
                    Misc.newList(Tables.ENTRIES.NAME),
                    Clause.and(
                        Clause.eq(Tables.ENTRIES.COL_RESOURCE, ""),
                        Clause.eq(
                            Tables.ENTRIES.COL_PARENT_GROUP_ID, "?")), "");
            long t1 = System.currentTimeMillis();
            for (Entry entry : entries) {
                String path = getStorageManager().resourceToDB(
                                  entry.getResource().getPath());
                Entry parentEntry = entry.getParentEntry();
                if (parentEntry == null) {
                    needToAdd.add(entry);

                    continue;
                }
                String key = parentEntry.getId() + "_" + path;
                if (seenResources.contains(key)) {
                    nonUniqueOnes.add(entry);

                    //                    System.out.println("seen resource:" + path);
                    continue;
                }
                seenResources.add(key);

                select.setString(1, path);
                select.setString(2, entry.getParentEntry().getId());
                //                select.addBatch();
                ResultSet results = select.executeQuery();
                if (results.next()) {
                    int found = results.getInt(1);
                    if (found == 0) {
                        needToAdd.add(entry);
                    } else {
                        nonUniqueOnes.add(entry);
                    }
                } else {
                    needToAdd.add(entry);
                }

            }
            getDatabaseManager().closeStatement(select);
            getDatabaseManager().closeConnection(connection);
            long t2 = System.currentTimeMillis();
            //            System.err.println("Took:" + (t2 - t1) + "ms to check: "
            //                               + entries.size() + " entries");
        } catch (Exception exc) {
            logError("Processing:" + query, exc);

            throw exc;
        }

        return needToAdd;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String getEntryResourceUrl(Request request, Entry entry) {
        return getEntryResourceUrl(request, entry, false);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param full _more_
     *
     * @return _more_
     */
    public String getEntryResourceUrl(Request request, Entry entry,
                                      boolean full) {
        return getEntryResourceUrl(request, entry, full, true);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param full _more_
     * @param addPath _more_
     *
     * @return _more_
     */
    public String getEntryResourceUrl(Request request, Entry entry,
                                      boolean full, boolean addPath) {
        if (entry.getResource().isUrl()) {
            return entry.getResource().getPath();
        }

        String fileTail = getStorageManager().getFileTail(entry);
        fileTail = HtmlUtils.urlEncodeExceptSpace(fileTail);
        //For now use the full entry path ???? why though ???
        if (addPath && fileTail.equals(entry.getName())) {
            fileTail = entry.getFullName(true);
        }
        if (request.getMakeAbsoluteUrls() || full) {
            return HtmlUtils.url(getFullEntryGetUrl(request) + "/"
                                 + fileTail, ARG_ENTRYID, entry.getId());
        } else {
            return HtmlUtils.url(request.url(getRepository().URL_ENTRY_GET)
                                 + "/" + fileTail, ARG_ENTRYID,
                                     entry.getId());
        }
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry getDummyGroup() throws Exception {
        return getDummyGroup("Search Results");
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry getDummyGroup(String name) throws Exception {
        Entry dummyGroup = new Entry(getRepository().getGroupTypeHandler(),
                                     true, name);
        dummyGroup.setId(getRepository().getGUID());
        dummyGroup.setUser(getUserManager().getAnonymousUser());

        return dummyGroup;
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry> getChildrenGroups(Request request, Entry group)
            throws Exception {
        List<Entry> result = new ArrayList<Entry>();
        if ( !group.isGroup()) {
            return result;
        }
        for (Entry entry : getChildren(request, group)) {
            if (entry.isGroup()) {
                result.add(entry);
            }
        }

        return result;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry> getChildrenEntries(Request request, Entry group)
            throws Exception {
        List<Entry> result = new ArrayList<Entry>();
        if ( !group.isGroup()) {
            return result;
        }
        for (Entry entry : getChildren(request, group)) {
            if ( !entry.isGroup()) {
                result.add(entry);
            }
        }

        return result;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param parentEntry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry> getChildrenAll(Request request, Entry parentEntry)
            throws Exception {
        List<Entry> children = new ArrayList<Entry>();
        if ( !parentEntry.isGroup()) {
            return children;
        }
        List<Entry>  entries = new ArrayList<Entry>();
        List<String> ids     = getChildIds(request, parentEntry, null);
        for (String id : ids) {
            Entry entry = getEntry(request, id);
            if (entry == null) {
                continue;
            }
            if (entry.isGroup()) {
                children.add(entry);
            } else {
                entries.add(entry);
            }
        }
        children.addAll(entries);

        return parentEntry.getTypeHandler().postProcessEntries(request,
                children);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param parentEntry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry> getChildren(Request request, Entry parentEntry)
            throws Exception {
        List<Entry> children = new ArrayList<Entry>();
        if ( !parentEntry.isGroup()) {
            return children;
        }
        List<Entry>  entries = new ArrayList<Entry>();
        List<String> ids     = getChildIds(request, parentEntry, null);
        for (String id : ids) {
            Entry entry = getEntry(request, id);
            if (entry == null) {
                continue;
            }
            if (entry.isGroup()) {
                children.add(entry);
            } else {
                entries.add(entry);
            }
        }
        children.addAll(entries);

        return parentEntry.getTypeHandler().postProcessEntries(request,
                children);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param where _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<String> getChildIds(Request request, Entry group,
                                    List<Clause> where)
            throws Exception {

        //        System.err.println("get Child ids:" + group);
        List<String> ids          = new ArrayList<String>();
        boolean      isSynthEntry = isSynthEntry(group.getId());
        if (group.getTypeHandler().isSynthType() || isSynthEntry) {
            //            System.err.println(" is synth");

            Entry  mainEntry = group;
            String synthId   = null;
            if (isSynthEntry) {
                //                System.err.println(" is synth entry mainENtry.getId=" + mainEntry.getId());

                String[] pair    = getSynthId(mainEntry.getId());
                String   entryId = pair[0];
                synthId = pair[1];
                if (entryId.equals(ENTRYID_PROCESS)) {
                    mainEntry = getProcessEntry();
                } else {
                    mainEntry = (Entry) getEntry(request, entryId, false,
                            false);
                    //                    System.err.println(" getEntry == null? :" + (mainEntry == null) +" " + entryId);
                }
                //                System.err.println(" main entry:" + mainEntry);
                if (mainEntry == null) {
                    return ids;
                }
            }

            //            System.err.println("****  Get synthids:" + mainEntry.getTypeHandler().getSynthIds(request, mainEntry,
            //                                                                                        group, synthId));

            return mainEntry.getTypeHandler().getSynthIds(request, mainEntry,
                    group, synthId);
        }


        if (where != null) {
            where = new ArrayList<Clause>(where);
        } else {
            where = new ArrayList<Clause>();
        }
        where.add(Clause.eq(Tables.ENTRIES.COL_PARENT_GROUP_ID,
                            group.getId()));


        String orderBy = getRepository().getQueryOrderAndLimit(request, true,
                             group);

        TypeHandler typeHandler = getRepository().getTypeHandler(request);
        int         skipCnt     = request.get(ARG_SKIP, 0);
        Statement statement = typeHandler.select(request,
                                  Tables.ENTRIES.COL_ID, where, orderBy);
        SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
        ResultSet        results;
        boolean canDoSelectOffset = getDatabaseManager().canDoSelectOffset();

        while ((results = iter.getNext()) != null) {
            String id = results.getString(1);
            if ( !canDoSelectOffset && (skipCnt-- > 0)) {
                continue;
            }
            ids.add(id);
        }

        return ids;
    }









    /**
     * _more_
     *
     * @param request _more_
     * @param groups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result changeType(Request request, List<Entry> groups,
                             List<Entry> entries)
            throws Exception {
        /*
        if ( !request.getUser().getAdmin()) {
            return null;
        }
        TypeHandler typeHandler =
            getRepository().getTypeHandler(TypeHandler.TYPE_HOMEPAGE);


        List<Entry> changedEntries = new ArrayList<Entry>();

        entries.addAll(groups);

        for(Entry entry: entries) {
            if(entry.isGroup()) {
                entry.setTypeHandler(typeHandler);
                changedEntries.add(entry);
            }
        }
        insertEntries(changedEntries, false);*/
        return new Result("Metadata", new StringBuffer("OK"));
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result publishEntries(Request request, List<Entry> entries)
            throws Exception {
        StringBuffer sb               = new StringBuffer();
        List<Entry>  publishedEntries = new ArrayList<Entry>();
        boolean      didone           = false;
        for (Entry entry : entries) {
            if ( !getAccessManager().canDoAction(request, entry,
                    Permission.ACTION_EDIT)) {
                continue;
            }

            if ( !isAnonymousUpload(entry)) {
                continue;
            }
            publishAnonymousEntry(request, entry);
            publishedEntries.add(entry);
            if ( !didone) {
                sb.append(msgHeader("Published Entries"));
                didone = true;
            }
            sb.append(
                HtmlUtils.href(
                    request.entryUrl(getRepository().URL_ENTRY_SHOW, entry),
                    getEntryDisplayName(entry)));
            sb.append(HtmlUtils.br());
        }
        if ( !didone) {
            sb.append(
                getPageHandler().showDialogNote(
                    msg("No entries to publish")));
        }
        updateEntries(request, publishedEntries);

        return new Result("Publish Entries", sb);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     * @param shortForm _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result addInitialMetadataToEntries(Request request,
            List<Entry> entries, boolean shortForm)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        List<Entry> changedEntries = addInitialMetadata(request, entries,
                                         false, shortForm);
        if (changedEntries.size() == 0) {
            sb.append(getRepository().translate(request,
                    "No metadata added"));
        } else {
            sb.append(changedEntries.size() + " "
                      + getRepository().translate(request,
                          "entries changed"));
            updateEntries(request, changedEntries);
        }
        if (entries.size() > 0) {
            return new Result(
                request.entryUrl(
                    getRepository().URL_ENTRY_SHOW,
                    entries.get(0).getParentEntry(), ARG_MESSAGE,
                    sb.toString()));
        }

        return new Result("Metadata", sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     * @param newEntries _more_
     * @param shortForm _more_
     *
     *
     * @return _more_
     * @throws Exception _more_
     */
    public List<Entry> addInitialMetadata(Request request,
                                          List<Entry> entries,
                                          boolean newEntries,
                                          boolean shortForm)
            throws Exception {
        List<Entry> changedEntries = new ArrayList<Entry>();
        for (Entry theEntry : entries) {
            if ( !newEntries
                    && !getAccessManager().canDoAction(request, theEntry,
                        Permission.ACTION_EDIT)) {
                continue;
            }

            Hashtable extra = new Hashtable();
            getMetadataManager().getMetadata(theEntry);
            boolean changed =
                getMetadataManager().addInitialMetadata(request, theEntry,
                    extra, shortForm);
            if ( !theEntry.hasAreaDefined()
                    && (extra.get(ARG_MINLAT) != null)) {
                theEntry.setSouth(Misc.getProperty(extra, ARG_MINLAT, 0.0));
                theEntry.setNorth(Misc.getProperty(extra, ARG_MAXLAT, 0.0));
                theEntry.setWest(Misc.getProperty(extra, ARG_MINLON, 0.0));
                theEntry.setEast(Misc.getProperty(extra, ARG_MAXLON, 0.0));
                theEntry.trimAreaResolution();

                changed = true;
            }
            if (extra.get(ARG_FROMDATE) != null) {
                theEntry.setStartDate(
                    ((Date) extra.get(ARG_FROMDATE)).getTime());
                theEntry.setEndDate(((Date) extra.get(ARG_TODATE)).getTime());
                changed = true;
            }
            if (changed) {
                changedEntries.add(theEntry);
            }
        }

        return changedEntries;
    }


    /**
     * _more_
     *
     * @param xmlFile _more_
     * @param internal _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry parseEntryXml(File xmlFile, boolean internal)
            throws Exception {
        Element root =
            XmlUtil.getRoot(getStorageManager().readSystemResource(xmlFile));

        return createEntryFromXml(new Request(getRepository(),
                getUserManager().getDefaultUser()), root, new Hashtable(),
                    new Hashtable(), false, internal);
    }




    /**
     *  This writes
     *
     * @param request _more_
     * @param entry _more_
     * @param file _more_
     *
     * @throws Exception _more_
     */
    public void writeEntryXmlFile(Request request, Entry entry, File file)
            throws Exception {
        IOUtil.writeFile(
            file,
            getRepository().getXmlOutputHandler().getEntryXml(
                request, entry));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void writeEntryXmlFile(Request request, Entry entry)
            throws Exception {
        writeEntryXmlFile(request, entry, getEntryXmlFile(entry));
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
    public File getEntryXmlFile(Entry entry) throws Exception {
        return getEntryXmlFile(entry.getFile());
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public File getEntryXmlFile(File file) throws Exception {
        File   parent  = file.getParentFile();
        String name    = file.getName();
        String newName = "." + name + ".ramadda.xml";

        return new File(IOUtil.joinDir(parent, newName));
    }

    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry getTemplateEntry(File file) throws Exception {
        File    parent      = file.getParentFile();
        boolean isDirectory = file.isDirectory();
        String  type        = (isDirectory
                               ? "dir"
                               : "file");
        String  filename    = file.getName();
        String[] names = { "." + filename + ".ramadda.xml",
                           "." + type + ".ramadda.xml", ".ramadda.xml", };


        for (String name : names) {
            File f = new File(IOUtil.joinDir(parent, name));
            if (f.exists()) {
                return parseEntryXml(f, true);
            }
        }

        if (isDirectory) {
            File f = new File(IOUtil.joinDir(file, ".this.ramadda.xml"));
            if (f.exists()) {
                return parseEntryXml(f, true);
            }

        }

        return null;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param s _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getEntryText(Request request, Entry entry, String s)
            throws Exception {
        //<attachment name>
        if (s.indexOf("<attachment") >= 0) {
            List<Association> associations =
                getAssociationManager().getAssociations(request, entry);
            for (Association association : associations) {
                if ( !association.getFromId().equals(entry.getId())) {
                    continue;
                }
            }
        }

        return s;
    }




    /**
     * _more_
     *
     *
     * @param request _more_
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry findGroup(Request request, String id) throws Exception {
        if ((id == null) || (id.length() == 0)) {
            return null;
        }
        Entry group = getGroupFromCache(id);
        if (group != null) {
            return group;
        }

        if (isSynthEntry(id) || id.startsWith("catalog:")) {
            return (Entry) getEntry(request, id);
        }


        Statement statement =
            getDatabaseManager().select(Tables.ENTRIES.COLUMNS,
                                        Tables.ENTRIES.NAME,
                                        Clause.eq(Tables.ENTRIES.COL_ID, id));

        List<Entry> groups = readEntries(statement);
        for (Entry entry : groups) {
            if (entry.isGroup()) {
                return entry;
            }
        }

        return null;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param name _more_
     * @param user _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry findGroupUnder(Request request, Entry group, String name,
                                User user)
            throws Exception {
        //        synchronized (MUTEX_ENTRY) {
        List<String> toks = (List<String>) StringUtil.split(name,
                                Entry.PATHDELIMITER, true, true);

        for (String tok : toks) {
            Entry theChild = null;
            for (Entry child : getChildrenGroups(request, group)) {
                if (child.isGroup() && child.getName().equals(tok)) {
                    theChild = (Entry) child;

                    break;
                }
            }
            if (theChild == null) {
                theChild = makeNewGroup(group, tok, user);
            }
            group = theChild;
        }

        return group;
        //        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param parent _more_
     * @param path _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry findEntryFromPath(Request request, Entry parent, String path)
            throws Exception {
        Entry currentEntry = parent;
        List<String> toks = StringUtil.split(path, Entry.PATHDELIMITER, true,
                                             true);
        for (int i = 0; i < toks.size(); i++) {
            if (currentEntry.getTypeHandler().isSynthType()) {
                List<String> subset = toks.subList(i, toks.size());
                if (subset.size() == 0) {
                    break;
                }

                return currentEntry.getTypeHandler().makeSynthEntry(request,
                        currentEntry, subset);
            }


            String name       = toks.get(i);
            Entry  childEntry = findEntryWithName(request, currentEntry,
                                    name);
            //Try to decode any slashes
            if (childEntry == null) {
                name       = name.replaceAll("%2F", "/");
                childEntry = findEntryWithName(request, currentEntry, name);
            }
            if (childEntry == null) {
                return null;
            }

            currentEntry = childEntry;
        }

        return currentEntry;
    }





    /**
     * _more_
     *
     *
     * @param request _more_
     * @param parent _more_
     * @param name _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry findEntryWithName(Request request, Entry parent, String name)
            throws Exception {
        String fullPath = ((parent == null)
                           ? ""
                           : parent.getFullName()) + Entry.IDDELIMITER + name;
        Entry  group    = getGroupFromCache(fullPath, false);
        if (group != null) {
            return group;
        }
        String[] ids = findEntryIdsWithName(request, parent, name);
        if (ids.length == 0) {
            return null;
        }

        return getEntry(request, ids[0], false);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param parent _more_
     * @param name _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry> findEntriesWithName(Request request, Entry parent,
                                           String name)
            throws Exception {
        List<Entry> entries  = new ArrayList<Entry>();
        String      fullPath = ((parent == null)
                                ? ""
                                : parent.getFullName()) + Entry.IDDELIMITER
                                    + name;



        Entry       group    = getGroupFromCache(fullPath, false);
        if (group != null) {
            entries.add(group);

            return entries;
        }
        String[] ids = findEntryIdsWithName(request, parent, name);
        if (ids.length == 0) {
            return entries;
        }
        for (String id : ids) {
            entries.add(getEntry(request, id, false));
        }

        return entries;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param parent _more_
     * @param name _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String[] findEntryIdsWithName(Request request, Entry parent,
                                         String name)
            throws Exception {
        Clause clause = null;
        String[] ids =
            SqlUtil.readString(
                getDatabaseManager().getIterator(
                    getDatabaseManager().select(
                        Tables.ENTRIES.COL_ID, Tables.ENTRIES.NAME,
                        clause =
                            Clause.and(
                                Clause.eq(
                                    Tables.ENTRIES.COL_PARENT_GROUP_ID,
                                    parent.getId()), Clause.eq(
                                        Tables.ENTRIES.COL_NAME, name)))));


        return ids;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param parent _more_
     * @param resource _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String[] findEntryIdsWithResource(Request request, Entry parent,
                                             String resource)
            throws Exception {
        String[] ids =
            SqlUtil.readString(
                getDatabaseManager().getIterator(
                    getDatabaseManager().select(
                        Tables.ENTRIES.COL_ID, Tables.ENTRIES.NAME,
                        Clause.and(
                            Clause.eq(
                                Tables.ENTRIES.COL_PARENT_GROUP_ID,
                                parent.getId()), Clause.eq(
                                    Tables.ENTRIES.COL_RESOURCE,
                                    resource)))));

        return ids;
    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param name _more_
     * @param user _more_
     * @param createIfNeeded _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry findGroupFromName(Request request, String name, User user,
                                   boolean createIfNeeded)
            throws Exception {
        return findGroupFromName(request, name, user, createIfNeeded,
                                 TypeHandler.TYPE_GROUP);
    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param name _more_
     * @param user _more_
     * @param createIfNeeded _more_
     * @param lastGroupType _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry findGroupFromName(Request request, String name, User user,
                                   boolean createIfNeeded,
                                   String lastGroupType)
            throws Exception {
        Entry entry = findEntryFromName(request, name, user, createIfNeeded,
                                        lastGroupType);
        if ((entry != null) && (entry.isGroup())) {
            return (Entry) entry;
        }

        return null;
    }


    /**
     * _more_
     *
     *
     * @param request _more_
     * @param name _more_
     * @param user _more_
     * @param createIfNeeded _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry findEntryFromName(Request request, String name, User user,
                                   boolean createIfNeeded)
            throws Exception {
        return findEntryFromName(request, name, user, createIfNeeded, null);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param parent _more_
     * @param name _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry> findDescendants(Request request, Entry parent,
                                       String name)
            throws Exception {
        List<String> toks = (List<String>) StringUtil.split(name,
                                Entry.PATHDELIMITER, true, true);

        List<Entry> parents = new ArrayList<Entry>();
        parents.add(parent);
        for (int i = 0; i < toks.size(); i++) {
            String      tok     = toks.get(i);
            List<Entry> matched = new ArrayList<Entry>();
            for (Entry p : parents) {
                if ( !p.isGroup()) {
                    continue;
                }
                for (Entry child : getChildren(request, p)) {
                    String childName = child.getName();

                    if (childName.equals(tok)) {
                        matched.add(child);
                    } else if (StringUtil.stringMatch(childName, tok, false,
                            true)) {
                        matched.add(child);
                    } else {
                        if (child.isFile()) {
                            childName =
                                getStorageManager().getFileTail(child);
                            if (childName.equals(tok)) {
                                matched.add(child);
                            } else if (StringUtil.stringMatch(childName, tok,
                                    false, true)) {
                                matched.add(child);
                            }
                        }
                    }
                }
            }
            if (i == toks.size() - 1) {
                return matched;
            }
            parents = matched;
        }

        return null;
    }


    /**
     * _more_
     *
     *
     * @param request _more_
     * @param name _more_
     * @param user _more_
     * @param createIfNeeded _more_
     * @param isGroup _more_
     * @param isTop _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Entry xxxfindEntryFromName(Request request, String name,
                                       User user, boolean createIfNeeded,
                                       boolean isGroup, boolean isTop)
            throws Exception {
        //        synchronized (MUTEX_ENTRY) {
        Entry  topEntry     = getTopGroup();
        String topEntryName = ((topEntry != null)
                               ? topEntry.getName()
                               : GROUP_TOP);
        if ( !name.equals(topEntryName)
                && !name.startsWith(topEntryName + Entry.PATHDELIMITER)) {
            name = topEntryName + Entry.PATHDELIMITER + name;
        }
        Entry entry = null;

        List<String> toks = (List<String>) StringUtil.split(name,
                                Entry.PATHDELIMITER, true, true);
        Entry  parent = null;
        String lastName;
        if ((toks.size() == 0) || (toks.size() == 1)) {
            lastName = name;
        } else {
            lastName = toks.get(toks.size() - 1);
            toks.remove(toks.size() - 1);
            parent = findGroupFromName(request,
                                       StringUtil.join(Entry.PATHDELIMITER,
                                           toks), user, createIfNeeded);
            if (parent == null) {
                if ( !isTop) {
                    return null;
                }

                return getTopGroup();
            } else {}
        }
        List<Clause> clauses = new ArrayList<Clause>();
        if (parent != null) {
            clauses.add(Clause.eq(Tables.ENTRIES.COL_PARENT_GROUP_ID,
                                  parent.getId()));
        } else {
            clauses.add(Clause.isNull(Tables.ENTRIES.COL_PARENT_GROUP_ID));
        }

        clauses.add(Clause.eq(Tables.ENTRIES.COL_NAME, lastName));
        Statement statement =
            getDatabaseManager().select(Tables.ENTRIES.COLUMNS,
                                        Tables.ENTRIES.NAME, clauses);
        List<Entry> entries = readEntries(statement);
        getDatabaseManager().closeAndReleaseConnection(statement);

        if (entries.size() > 0) {
            entry = entries.get(0);
        } else {
            if ( !createIfNeeded) {
                return null;
            }

            return makeNewGroup(parent, lastName, user);
        }

        return entry;
        //        }
    }


    /**
     * _more_
     *
     *
     * @param request _more_
     * @param name _more_
     * @param user _more_
     * @param createIfNeeded _more_
     * @param lastGroupType _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry findEntryFromName(Request request, String name, User user,
                                   boolean createIfNeeded,
                                   String lastGroupType)
            throws Exception {
        return findEntryFromName(request, name, user, createIfNeeded,
                                 lastGroupType, null);
    }


    /**
     * _more_
     *
     *
     * @param request _more_
     * @param name _more_
     * @param user _more_
     * @param createIfNeeded _more_
     * @param lastGroupType _more_
     * @param initializer _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry findEntryFromName(Request request, String name, User user,
                                   boolean createIfNeeded,
                                   String lastGroupType,
                                   EntryInitializer initializer)
            throws Exception {
        if (name == null) {
            return null;
        }
        name = name.trim();
        if (name.startsWith(Entry.PATHDELIMITER)) {
            name = name.substring(1);
        }
        String topEntryName = getTopGroup().getName();
        if (name.equals(topEntryName)) {
            return getTopGroup();
        }
        //Tack on the top group name if its not there
        if ( !name.startsWith(topEntryName + Entry.PATHDELIMITER)) {
            name = topEntryName + Entry.PATHDELIMITER + name;
        }
        //split the list
        List<String> toks = (List<String>) StringUtil.split(name,
                                Entry.PATHDELIMITER, true, true);
        //Now remove the top group

        toks.remove(0);

        Entry currentEntry = getTopGroup();

        if (lastGroupType == null) {
            lastGroupType = TypeHandler.TYPE_GROUP;
        }
        String groupType = TypeHandler.TYPE_GROUP;

        for (int i = 0; i < toks.size(); i++) {
            boolean      lastOne   = (i == toks.size() - 1);
            String       childName = Entry.decodeName(toks.get(i));

            List<Clause> clauses   = new ArrayList<Clause>();
            clauses.add(Clause.eq(Tables.ENTRIES.COL_PARENT_GROUP_ID,
                                  currentEntry.getId()));
            clauses.add(Clause.eq(Tables.ENTRIES.COL_NAME, childName));
            List<Entry> entries = readEntries(
                                      getDatabaseManager().select(
                                          Tables.ENTRIES.COLUMNS,
                                          Tables.ENTRIES.NAME, clauses));
            if (entries.size() > 0) {
                currentEntry = entries.get(0);
            } else {
                if ( !createIfNeeded) {
                    return null;
                }
                currentEntry = makeNewGroup(currentEntry, childName, user,
                                            null, (lastOne
                        ? lastGroupType
                        : groupType), initializer);
            }

            if (currentEntry.getTypeHandler().isSynthType()) {
                List<String> subset = toks.subList(i + 1, toks.size());
                if (subset.size() == 0) {
                    break;
                }

                return currentEntry.getTypeHandler().makeSynthEntry(request,
                        currentEntry, subset);
            }
        }
        if (currentEntry == null) {
            return null;
        }

        Entry filteredEntry = getAccessManager().filterEntry(request,
                                  currentEntry);
        if (filteredEntry == null) {
            System.err.println("EntryManger.findEntryFromName:"
                               + " cannot view entry:" + currentEntry
                               + " user:" + request.getUser());
        }

        return filteredEntry;
    }


    /**
     * _more_
     *
     * @param parent _more_
     * @param name _more_
     * @param user _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry makeNewGroup(Entry parent, String name, User user)
            throws Exception {
        return makeNewGroup(parent, name, user, null);
    }



    /**
     * _more_
     *
     * @param parent _more_
     * @param name _more_
     * @param user _more_
     * @param template _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry makeNewGroup(Entry parent, String name, User user,
                              Entry template)
            throws Exception {
        String groupType = TypeHandler.TYPE_GROUP;
        if (template != null) {
            groupType = template.getTypeHandler().getType();
        }

        return makeNewGroup(parent, name, user, template, groupType);
    }



    /**
     * _more_
     *
     * @param parent _more_
     * @param name _more_
     * @param user _more_
     * @param template _more_
     * @param type _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry makeNewGroup(Entry parent, String name, User user,
                              Entry template, String type)
            throws Exception {
        return makeNewGroup(parent, name, user, template, type, null);
    }

    /**
     * _more_
     *
     * @param parent _more_
     * @param name _more_
     * @param user _more_
     * @param template _more_
     * @param type _more_
     * @param initializer _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry makeNewGroup(Entry parent, String name, User user,
                              Entry template, String type,
                              EntryInitializer initializer)
            throws Exception {
        //        synchronized (MUTEX_ENTRY) {
        Date date = Utils.extractDate(name);
        if (date == null) {
            date = new Date();
        }

        TypeHandler typeHandler = getRepository().getTypeHandler(type);
        Entry       group       = new Entry(getGroupId(parent), typeHandler);
        group.setName(name);
        group.setDate(date.getTime());
        if (template != null) {
            group.initWith(template);
            getRepository().getMetadataManager().newEntry(group);
        }
        group.setParentEntry(parent);
        group.setUser(user);
        if (initializer != null) {
            initializer.initEntry(group);
        }
        addNewEntry(null, group);
        cacheEntry(group);

        return group;
        //        }
    }


    /**
     * _more_
     *
     * @param parent _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getGroupId(Entry parent) throws Exception {
        //FOr now just use regular ids for groups
        if (true) {
            return getRepository().getGUID();
        }

        int    baseId = 0;
        Clause idClause;
        String idWhere;
        if (parent == null) {
            idClause = Clause.isNull(Tables.ENTRIES.COL_PARENT_GROUP_ID);
        } else {
            idClause = Clause.eq(Tables.ENTRIES.COL_PARENT_GROUP_ID,
                                 parent.getId());
        }
        String newId = null;
        while (true) {
            if (parent == null) {
                newId = BLANK + baseId;
            } else {
                newId = parent.getId() + Entry.IDDELIMITER + baseId;
            }

            Statement stmt =
                getDatabaseManager().select(Tables.ENTRIES.COL_ID,
                                            Tables.ENTRIES.NAME,
                                            new Clause[] { idClause,
                    Clause.eq(Tables.ENTRIES.COL_ID, newId) });
            ResultSet idResults = stmt.getResultSet();

            if ( !idResults.next()) {
                break;
            }
            baseId++;
        }

        return newId;

    }


    /**
     * _more_
     *
     *
     * @param request _more_
     *
     * @param clause _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry> getGroups(Request request, Clause clause)
            throws Exception {

        List<Clause> clauses = new ArrayList<Clause>();
        if (clause != null) {
            clauses.add(clause);
        }
        clauses.add(Clause.eq(Tables.ENTRIES.COL_TYPE,
                              TypeHandler.TYPE_GROUP));
        Statement statement =
            getDatabaseManager().select(Tables.ENTRIES.COL_ID,
                                        Tables.ENTRIES.NAME, clauses);

        return getGroups(
            request,
            SqlUtil.readString(
                getDatabaseManager().getIterator(statement), 1));
    }


    /**
     * _more_
     *
     * @param entries _more_
     *
     * @return _more_
     */
    public List<Entry> getGroups(List<Entry> entries) {
        List<Entry> groupList = new ArrayList<Entry>();
        for (Entry entry : entries) {
            if (entry.isGroup()) {
                groupList.add((Entry) entry);
            }
        }

        return groupList;
    }

    /**
     * _more_
     *
     *
     *
     * @param request _more_
     * @param groupIds _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry> getGroups(Request request, String[] groupIds)
            throws Exception {
        List<Entry> groupList = new ArrayList<Entry>();
        for (int i = 0; i < groupIds.length; i++) {
            Entry group = findGroup(request, groupIds[i]);
            if (group != null) {
                groupList.add(group);
            }
        }

        return groupList;
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
    public List<Entry> getTopGroups(Request request) throws Exception {
        List<Entry> topEntries = null;

        Statement statement = getDatabaseManager().select(
                                  Tables.ENTRIES.COL_ID, Tables.ENTRIES.NAME,
                                  Clause.eq(
                                      Tables.ENTRIES.COL_PARENT_GROUP_ID,
                                      getTopGroup().getId()));
        String[] ids =
            SqlUtil.readString(getDatabaseManager().getIterator(statement),
                               1);
        List<Entry> groups = new ArrayList<Entry>();
        for (int i = 0; i < ids.length; i++) {
            //Get the entry but don't check for access control
            try {
                Entry e = getEntry(request, ids[i], false);
                if (e == null) {
                    continue;
                }
                if ( !e.isGroup()) {
                    continue;
                }
                Entry g = (Entry) e;
                groups.add(g);
            } catch (Throwable exc) {
                logError("Error getting top folder", exc);
            }
        }

        //For now don't check for access control
        //        return topEntries = new ArrayList<Entry>(
        //            toGroupList(getAccessManager().filterEntries(request, groups)));
        return new ArrayList<Entry>(groups);
    }

    /**
     * _more_
     *
     * @param entries _more_
     *
     * @return _more_
     */
    private List<Entry> toGroupList(List<Entry> entries) {
        List<Entry> groups = new ArrayList<Entry>();
        for (Entry entry : entries) {
            groups.add((Entry) entry);
        }

        return groups;
    }



    /**
     * _more_
     *
     * @param statement _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private List<Entry> readEntries(Statement statement) throws Exception {
        List<Entry> entries = new ArrayList<Entry>();
        try {
            SqlUtil.Iterator iter =
                getDatabaseManager().getIterator(statement);
            ResultSet results;
            while ((results = iter.getNext()) != null) {
                String entryType = results.getString(2);
                TypeHandler typeHandler =
                    getRepository().getTypeHandler(entryType);
                Entry entry =
                    (Entry) typeHandler.createEntryFromDatabase(results);
                entries.add(entry);
                cacheEntry(entry);
            }
        } finally {
            getDatabaseManager().closeAndReleaseConnection(statement);
        }

        for (Entry entry : entries) {
            if (entry.getParentEntryId() != null) {
                Entry parentEntry = (Entry) findGroup(null,
                                        entry.getParentEntryId());
                entry.setParentEntry(parentEntry);
            }
        }

        return entries;
    }


    /**
     * _more_
     *
     * @param sb _more_
     */
    public void addStatusInfo(StringBuffer sb) {
        sb.append(HtmlUtils.formEntry(msgLabel("Entry Cache"),
                                      getEntryCache().size() / 2 + ""));
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
    public Entry findGroup(Request request) throws Exception {
        String groupNameOrId = (String) request.getString(ARG_GROUP,
                                   (String) null);
        if (groupNameOrId == null) {
            groupNameOrId = (String) request.getString(ARG_ENTRYID,
                    (String) null);
        }
        if (groupNameOrId == null) {
            throw new IllegalArgumentException("No folder specified");
        }
        Entry entry = getEntry(request, groupNameOrId, false);
        if (entry != null) {
            if ( !entry.isGroup()) {
                throw new IllegalArgumentException("Not a folder:"
                        + groupNameOrId);
            }

            return (Entry) entry;
        }

        throw new RepositoryUtil.MissingEntryException(
            "Could not find folder:" + groupNameOrId);
    }










    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entries _more_
     * @param connection _more_
     * @param firstCall _more_
     * @param ignoreSynth _more_
     *
     * @return _more_
     * @throws Exception _more_
     */
    protected List<String[]> getDescendents(Request request,
                                            List<Entry> entries,
                                            Connection connection,
                                            boolean firstCall,
                                            boolean ignoreSynth)
            throws Exception {
        List<String[]> children = new ArrayList();
        for (Entry entry : entries) {
            if (firstCall) {
                children.add(new String[] { entry.getId(),
                                            entry.getTypeHandler().getType(),
                                            entry.getResource().getPath(),
                                            entry.getResource().getType() });
            }
            if ( !entry.isGroup()) {
                continue;
            }

            if (entry.getTypeHandler().isSynthType()
                    || isSynthEntry(entry.getId())) {
                if (ignoreSynth) {
                    continue;
                }
                for (String childId :
                        getChildIds(request, (Entry) entry, null)) {
                    Entry childEntry = getEntry(request, childId);
                    if (childEntry == null) {
                        continue;
                    }
                    children.add(new String[] { childId, childEntry.getType(),
                            childEntry.getResource().getPath(),
                            childEntry.getResource().getType() });
                    if (childEntry.isGroup()) {
                        children.addAll(getDescendents(request,
                                (List<Entry>) Misc.newList(childEntry),
                                connection, false, ignoreSynth));
                    }
                }

                return children;
            }


            Statement stmt = SqlUtil.select(connection,
                                            SqlUtil.comma(new String[] {
                                                Tables.ENTRIES.COL_ID,
                    Tables.ENTRIES.COL_TYPE, Tables.ENTRIES.COL_RESOURCE,
                    Tables.ENTRIES.COL_RESOURCE_TYPE }), Misc.newList(
                        Tables.ENTRIES.NAME), Clause.eq(
                        Tables.ENTRIES.COL_PARENT_GROUP_ID, entry.getId()));

            SqlUtil.Iterator iter = getDatabaseManager().getIterator(stmt);
            //Don't close the statement because that ends up closing the connection
            iter.setShouldCloseStatement(false);
            ResultSet results;
            while ((results = iter.getNext()) != null) {
                int    col       = 1;
                String childId   = results.getString(col++);
                String childType = results.getString(col++);
                String resource = getStorageManager().resourceFromDB(
                                      results.getString(col++));
                String resourceType = results.getString(col++);
                children.add(new String[] { childId, childType, resource,
                                            resourceType });
                Entry childEntry = getEntry(request, childId);

                if (childEntry == null) {
                    //This happened when a previous delete tree went bad and a parent has a record of
                    //a child that does not exist
                    continue;
                }

                children.addAll(getDescendents(request,
                        (List<Entry>) Misc.newList(childEntry), connection,
                        false, ignoreSynth));
            }
            getDatabaseManager().closeStatement(stmt);
        }

        return children;
    }

    /** _more_ */
    private ProcessFileTypeHandler processFileTypeHandler;

    /** _more_ */
    private Entry processEntry;

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public ProcessFileTypeHandler getProcessFileTypeHandler()
            throws Exception {
        if (processFileTypeHandler == null) {
            ProcessFileTypeHandler tmp =
                new ProcessFileTypeHandler(getRepository(), null);
            tmp.setType("type_process");
            processFileTypeHandler = tmp;
        }

        return processFileTypeHandler;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry getProcessEntry() throws Exception {
        if (processEntry == null) {
            TypeHandler typeHandler = getProcessFileTypeHandler();
            //parentEntry = topGroup;
            Entry parentEntry = new Entry(typeHandler, true);
            parentEntry.setUser(getUserManager().getLocalFileUser());
            parentEntry.addMetadata(new Metadata(getRepository().getGUID(),
                    parentEntry.getId(),
                    ContentMetadataHandler.TYPE_PAGESTYLE, true, "", "false",
                    "", "", ""));

            Entry topGroup = getTopGroup();
            parentEntry.setName("Process Entries");
            parentEntry.setId(ID_PREFIX_SYNTH + ENTRYID_PROCESS);
            parentEntry.setParentEntry(topGroup);
            processEntry = parentEntry;
        }

        return processEntry;
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public boolean handleEntryAsGroup(Entry entry) {
        if ( !entry.isGroup()) {
            return false;
        }
        String type = entry.getType();
        if (type.equals(TYPE_GROUP)) {
            return true;
        }

        //TODO: look at type handler properties
        return true;
    }


    /** _more_ */
    private HashSet missingResources = new HashSet();

    /** _more_ */
    public static final String PROP_DELETE_ENTRY_FILE_IS_MISSING =
        "ramadda.delete_entry_when_file_is_missing";

    /**
     * This handles entries when their file is missing
     * If the property ramadda.delete_entry_when_file_is_missing is set to true
     * then the entry is deleted.
     * Else, if the user is not logged then the the entry isn't shown
     *
     *
     * @param request the request
     * @param entry the entry
     *
     * @return The entry or null if missing
     *
     * @throws Exception on badness
     */
    public Entry handleMissingFileEntry(Request request, Entry entry)
            throws Exception {

        //If its not a FILE then don't do anything
        if ( !entry.getResource().isServerSideFile()) {
            return entry;
        }

        File f = entry.getResource().getTheFile();
        if (f.exists()) {
            return entry;
        }

        if (getProperty(PROP_DELETE_ENTRY_FILE_IS_MISSING, false)) {
            deleteEntry(request, entry);
            logInfo("RAMADDA: Deleted entry with missing file: "
                    + entry.getName() + " File:" + f);

            return null;
        }

        //Don't show the bad files for regular folk
        if (request.isAnonymous()) {
            return null;
        }

        String path = entry.getResource().getPath();
        if ( !missingResources.contains(path)) {
            missingResources.add(path);
            logError("File for entry: " + entry.getId() + " does not exist:"
                     + path, null);
        }

        return entry;


    }

}
