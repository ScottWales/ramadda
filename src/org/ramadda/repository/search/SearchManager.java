/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for Atmospheric Research
 * Copyright 2010- Jeff McWhirter
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.ramadda.repository.search;


import org.apache.lucene.analysis.standard.StandardAnalyzer;


import org.apache.lucene.document.DateTools;

import org.apache.lucene.document.Field;

import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;


import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import org.ramadda.repository.*;
import org.ramadda.repository.admin.*;

import org.ramadda.repository.auth.*;

import org.ramadda.repository.metadata.*;

import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.OpenSearchUtil;


import org.w3c.dom.*;

import ucar.unidata.sql.Clause;

import ucar.unidata.sql.SqlUtil;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.PatternFileFilter;
import ucar.unidata.util.PluginClassLoader;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;



import java.io.*;

import java.io.File;

import java.lang.reflect.*;



import java.net.*;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import java.util.jar.*;



import java.util.regex.*;
import java.util.zip.*;




/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class SearchManager extends RepositoryManager implements EntryChecker, AdminHandler {

    /** _more_ */
    public static final String ARG_SEARCH_SUBMIT = "search.submit";

    /** _more_ */
    public static final String ARG_SEARCH_SUBSET = "search.subset";

    /** _more_ */
    public static final String ARG_SEARCH_SERVERS = "search.servers";



    /** _more_ */
    public final RequestUrl URL_SEARCH_FORM = new RequestUrl(this,
                                                  "/search/form",
                                                  "Advanced Search");

    /** _more_ */
    public final RequestUrl URL_SEARCH_ASSOCIATIONS =
        new RequestUrl(this, "/search/associations/do",
                       "Search Associations");

    /** _more_ */
    public final RequestUrl URL_SEARCH_ASSOCIATIONS_FORM =
        new RequestUrl(this, "/search/associations/form",
                       "Search Associations");

    /** _more_ */
    public final RequestUrl URL_SEARCH_TEXTFORM = new RequestUrl(this,
                                                      "/search/textform",
                                                      "Text Search");

    /** _more_ */
    public final RequestUrl URL_SEARCH_BROWSE = new RequestUrl(this,
                                                    "/search/browse",
                                                    "Browse");



    /** _more_ */
    public final RequestUrl URL_SEARCH_REMOTE_DO =
        new RequestUrl(this, "/search/remote/do", "Search Remote Servers");

    /** _more_ */
    public final RequestUrl URL_ENTRY_SEARCH = new RequestUrl(this,
                                                   "/search/do", "Search");

    /** _more_ */
    public final List<RequestUrl> searchUrls =
        RepositoryUtil.toList(new RequestUrl[] { URL_SEARCH_TEXTFORM,
            URL_SEARCH_FORM, URL_SEARCH_BROWSE,
            URL_SEARCH_ASSOCIATIONS_FORM });

    /** _more_ */
    public final List<RequestUrl> remoteSearchUrls =
        RepositoryUtil.toList(new RequestUrl[] { URL_SEARCH_TEXTFORM,
            URL_SEARCH_FORM, URL_SEARCH_BROWSE,
            URL_SEARCH_ASSOCIATIONS_FORM });



    /** _more_          */
    private static final String FIELD_ENTRYID = "entryid";

    /** _more_          */
    private static final String FIELD_PATH = "path";

    /** _more_          */
    private static final String FIELD_CONTENTS = "contents";

    /** _more_          */
    private static final String FIELD_MODIFIED = "modified";

    /** _more_          */
    private static final String FIELD_DESCRIPTION = "description";

    /** _more_          */
    private static final String FIELD_METADATA = "metadata";

    /** _more_          */
    private IndexSearcher luceneSearcher;

    /** _more_          */
    private IndexReader luceneReader;

    /** _more_          */
    private boolean isLuceneEnabled = true;

    /**
     * _more_
     *
     * @param repository _more_
     */
    public SearchManager(Repository repository) {
        super(repository);
        repository.addEntryChecker(this);
        isLuceneEnabled = getProperty(PROP_SEARCH_LUCENE_ENABLED, false);
        getAdmin().addAdminHandler(this);
    }


    public boolean includeMetadata() {
        return getProperty(PROP_SEARCH_SHOW_METADATA, true);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isLuceneEnabled() {
        return isLuceneEnabled;
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private IndexWriter getLuceneWriter() throws Exception {
        File indexFile = new File(getStorageManager().getIndexDir());
        IndexWriter writer =
            new IndexWriter(FSDirectory.open(indexFile),
                            new StandardAnalyzer(Version.LUCENE_CURRENT),
                            IndexWriter.MaxFieldLength.LIMITED);
        return writer;
    }


    public List<RequestUrl> getAdminUrls() {
        return null;
    }

    public void addToAdminSettingsForm(String block, StringBuffer asb) {
        if(!block.equals(Admin.BLOCK_ACCESS)) return;
        asb.append(HtmlUtil.colspan(msgHeader("Search"), 2));
        asb.append(HtmlUtil.formEntry("",
                                      HtmlUtil.checkbox(PROP_SEARCH_LUCENE_ENABLED,
                                          "true",
                                                        isLuceneEnabled())
                                      + HtmlUtil.space(2)
                                      + msg("Enable Lucene Indexing and Search")));
    }


    public void applyAdminSettingsForm(Request request) throws Exception {
        getRepository().writeGlobal(PROP_SEARCH_LUCENE_ENABLED,
                                    isLuceneEnabled = request.get(PROP_SEARCH_LUCENE_ENABLED, false));

    }

    public String getId() {
        return "searchmanager";
    }

    /**
     * _more_
     *
     * @param entries _more_
     *
     * @throws Exception _more_
     */
    public synchronized void indexEntries(List<Entry> entries)
            throws Exception {
        IndexWriter writer = getLuceneWriter();
        for (Entry entry : entries) {
            indexEntry(writer, entry);
        }
        writer.optimize();
        writer.close();
        luceneReader   = null;
        luceneSearcher = null;
    }


    /**
     * _more_
     *
     * @param writer _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    private void indexEntry(IndexWriter writer, Entry entry)
            throws Exception {

        org.apache.lucene.document.Document doc =
            new org.apache.lucene.document.Document();
        String path = entry.getResource().getPath();
        doc.add(new Field(FIELD_ENTRYID, entry.getId(), Field.Store.YES,
                          Field.Index.NOT_ANALYZED));
        if ((path != null) && (path.length() > 0)) {
            doc.add(new Field(FIELD_PATH, path, Field.Store.YES,
                              Field.Index.NOT_ANALYZED));
        }

        StringBuffer metadataSB = new StringBuffer();
        getRepository().getMetadataManager().getTextCorpus(entry, metadataSB);
        entry.getTypeHandler().getTextCorpus(entry, metadataSB);
        doc.add(new Field(FIELD_DESCRIPTION,
                          entry.getName() + " " + entry.getDescription(),
                          Field.Store.NO, Field.Index.ANALYZED));

        if(metadataSB.length()>0) {
            doc.add(new Field(FIELD_METADATA, metadataSB.toString(),
                              Field.Store.NO, Field.Index.ANALYZED));
        }

        doc.add(new Field(FIELD_MODIFIED,
                          DateTools.timeToString(entry.getStartDate(),
                              DateTools.Resolution.MINUTE), Field.Store.YES,
                                  Field.Index.NOT_ANALYZED));

        if (entry.isFile()) {
            addContentField(entry, doc, new File(path));
        }
        writer.addDocument(doc);
    }


    private void addContentField(Entry entry, org.apache.lucene.document.Document doc, File f) throws Exception {
        //org.apache.lucene.document.Document doc
        InputStream stream = getStorageManager().getFileInputStream(f);
        try {
            org.apache.tika.metadata.Metadata metadata = new org.apache.tika.metadata.Metadata();
            org.apache.tika.parser.AutoDetectParser parser = new org.apache.tika.parser.AutoDetectParser();
            org.apache.tika.sax.BodyContentHandler handler = new org.apache.tika.sax.BodyContentHandler();
            parser.parse(stream, handler, metadata);
            String contents = handler.toString();
            //            System.err.println("contents: " + contents);
            if(contents!=null && contents.length()>0) {
                doc.add(new Field(FIELD_CONTENTS, contents, Field.Store.NO,
                                  Field.Index.ANALYZED));
            }
 
            /*
            String[] names = metadata.names();
            for (String name : names) {
                String value = metadata.get(name);
                System.err.println(name +"=" + value);
                doc.add(new Field(name, value, Field.Store.YES,
                                  Field.Index.ANALYZED));
            }
            */
        } catch(Exception exc) {
            System.err.println("error harvesting corpus from:" + f);
            exc.printStackTrace();
        } finally {
            IOUtil.close(stream);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private IndexReader getLuceneReader() throws Exception {
        if (true) {
            return IndexReader.open(
                FSDirectory.open(
                    new File(getStorageManager().getIndexDir())), false);
        }
        if (luceneReader == null) {
            luceneReader = IndexReader.open(
                FSDirectory.open(
                    new File(getStorageManager().getIndexDir())), false);
        }
        return luceneReader;
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private IndexSearcher getLuceneSearcher() throws Exception {
        if (luceneSearcher == null) {
            luceneSearcher = new IndexSearcher(getLuceneReader());
        }
        return luceneSearcher;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param groups _more_
     * @param entries _more_
     *
     * @throws Exception _more_
     */
    public void processLuceneSearch(Request request, List<Entry> groups,
                                    List<Entry> entries)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        StandardAnalyzer analyzer =
            new StandardAnalyzer(Version.LUCENE_CURRENT);
        QueryParser qp = new MultiFieldQueryParser(Version.LUCENE_CURRENT,
                             new String[] { FIELD_DESCRIPTION,
                                            FIELD_METADATA,
                                            FIELD_CONTENTS }, analyzer);
        Query         query    = qp.parse(request.getString(ARG_TEXT, ""));
        IndexSearcher searcher = getLuceneSearcher();
        TopDocs       hits     = searcher.search(query, 100);
        ScoreDoc[]    docs     = hits.scoreDocs;
        for (int i = 0; i < docs.length; i++) {
            org.apache.lucene.document.Document doc =
                searcher.doc(docs[i].doc);
            String id = doc.get(FIELD_ENTRYID);
            if (id == null) {
                continue;
            }
            Entry entry = getEntryManager().getEntry(request, id);
            if (entry == null) {
                continue;
            }
            if (entry.isGroup()) {
                groups.add(entry);
            } else {
                entries.add(entry);
            }
        }
    }


    /**
     * _more_
     *
     * @param entries _more_
     */
    public void entriesCreated(List<Entry> entries) {
        if ( !isLuceneEnabled()) {
            return;
        }
        try {
            indexEntries(entries);
        } catch (Exception exc) {
            logError("Error indexing entries", exc);
        }
    }



    /**
     * _more_
     *
     * @param entries _more_
     */
    public void entriesModified(List<Entry> entries) {
        if ( !isLuceneEnabled()) {
            return;
        }
        try {
            List<String> ids = new ArrayList<String>();
            for (Entry entry : entries) {
                ids.add(entry.getId());

            }
            entriesDeleted(ids);
            indexEntries(entries);
        } catch (Exception exc) {
            logError("Error adding entries to Lucene index", exc);
        }
    }


    /**
     * _more_
     *
     * @param ids _more_
     */
    public synchronized void entriesDeleted(List<String> ids) {
        if ( !isLuceneEnabled()) {
            return;
        }
        try {
            IndexWriter writer = getLuceneWriter();
            for (String id : ids) {
                writer.deleteDocuments(new Term(FIELD_ENTRYID, id));
            }
            writer.close();
        } catch (Exception exc) {
            logError("Error deleting entries from Lucene index", exc);
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
    public Result processCapabilities(Request request) throws Exception {
        return new Result("", "text/xml");
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
    public Result processOpenSearch(Request request) throws Exception {

        Document doc  = XmlUtil.makeDocument();
        Element  root = OpenSearchUtil.getRoot();
        /*
   <ShortName>Web Search</ShortName>
   <Description>Use Example.com to search the Web.</Description>
   <Tags>example web</Tags>
   <Contact>admin@example.com</Contact>
        */
        OpenSearchUtil.addBasicTags(
            root, getRepository().getRepositoryName(),
            getRepository().getRepositoryDescription(),
            getRepository().getRepositoryEmail());
        ((Element) XmlUtil.create(
            OpenSearchUtil.TAG_IMAGE, root)).appendChild(
                XmlUtil.makeCDataNode(
                    root.getOwnerDocument(),
                    getRepository().getLogoImage(null), false));




        String url = getRepository().absoluteUrl(URL_ENTRY_SEARCH.toString());
        url = HtmlUtil.url(url, new String[] {
            ARG_OUTPUT, AtomOutputHandler.OUTPUT_ATOM.getId(), ARG_TEXT,
            OpenSearchUtil.MACRO_TEXT, ARG_BBOX, OpenSearchUtil.MACRO_BBOX,
            Constants.dataDate.getFromArg(), OpenSearchUtil.MACRO_TIME_START,
            Constants.dataDate.getToArg(), OpenSearchUtil.MACRO_TIME_END,
        }, false);


        XmlUtil.create(OpenSearchUtil.TAG_URL, root, "",
                       new String[] { OpenSearchUtil.ATTR_TYPE,
                                      "application/atom+xml",
                                      OpenSearchUtil.ATTR_TEMPLATE, url });
        return new Result(XmlUtil.toString(root), OpenSearchUtil.MIMETYPE);
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
    public Result processEntryTextSearchForm(Request request)
            throws Exception {
        return makeSearchForm(request, true, false);
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<RequestUrl> getSearchUrls() throws Exception {
        if (getRegistryManager().getSelectedRemoteServers().size() > 0) {
            //            return getRepository().remoteSearchUrls;
            return remoteSearchUrls;
        }
        return searchUrls;
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param justText _more_
     * @param typeSpecific _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result makeSearchForm(Request request, boolean justText,
                                 boolean typeSpecific)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        makeSearchForm(request, justText, typeSpecific, sb);

        return makeResult(request, msg("Search Form"), sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public String getSearchUrl(Request request) {
        return request.url(URL_ENTRY_SEARCH, ARG_NAME, WHAT_ENTRIES);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param justText _more_
     * @param typeSpecific _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    private void makeSearchForm(Request request, boolean justText,
                                boolean typeSpecific, StringBuffer sb)
            throws Exception {

        TypeHandler typeHandler = getRepository().getTypeHandler(request);
        sb.append(HtmlUtil.form(getSearchUrl(request),
                                " name=\"searchform\" "));

        if (justText) {
            sb.append(HtmlUtil.hidden(ARG_SEARCH_TYPE, SEARCH_TYPE_TEXT));
        }

        //Put in an empty submit button so when the user presses return 
        //it acts like a regular submit (not a submit to change the type)
        sb.append(HtmlUtil.submitImage(iconUrl(ICON_BLANK),
                                       ARG_SEARCH_SUBMIT));

        String what = (String) request.getWhat(BLANK);
        if (what.length() == 0) {
            what = WHAT_ENTRIES;
        }


        List<ServerInfo> servers =
            getRegistryManager().getSelectedRemoteServers();

        String buttons;


        if (servers.size() > 0) {
            buttons =
                RepositoryUtil.buttons(
                    HtmlUtil.submit(
                        msg("Search this Repository"),
                        ARG_SEARCH_SUBMIT), HtmlUtil.submit(
                            msg("Search Remote Repositories"),
                            ARG_SEARCH_SERVERS));
        } else {
            buttons = HtmlUtil.submit(msg("Search"), ARG_SEARCH_SUBMIT);
        }
        sb.append(HtmlUtil.p());
        if (!justText) {
            sb.append(buttons);
            sb.append(HtmlUtil.p());
        }

        if (justText) {
            String value           = (String) request.getString(ARG_TEXT, "");
            sb.append(HtmlUtil.span(msgLabel("Text"), HtmlUtil.cssClass("formlabel")) + " " + 
                      HtmlUtil.input(ARG_TEXT, value,
                                     HtmlUtil.SIZE_50
                                     + " autofocus ") + " " + buttons);
            /*            sb.append(
                "<table width=\"100%\" border=\"0\"><tr><td width=\"60\">");
            typeHandler.addTextSearch(request, sb);
            sb.append("</table>");
            sb.append(HtmlUtil.p());
            */
        } else {
            Object       oldValue = request.remove(ARG_RELATIVEDATE);
            List<Clause> where    = typeHandler.assembleWhereClause(request);
            if (oldValue != null) {
                request.put(ARG_RELATIVEDATE, oldValue);
            }

            typeHandler.addToSearchForm(request, sb, where, true);


            if(includeMetadata()) {
                StringBuffer metadataSB = new StringBuffer();
                metadataSB.append(HtmlUtil.formTable());
                getMetadataManager().addToSearchForm(request, metadataSB);
                metadataSB.append(HtmlUtil.formTableClose());
                sb.append(HtmlUtil.makeShowHideBlock(msg("Properties"),
                                                     metadataSB.toString(), false));
            }

            StringBuffer outputForm = new StringBuffer(HtmlUtil.formTable());
            /* Humm, we probably don't want to include this as it screws up setting the output in the form
            if (request.defined(ARG_OUTPUT)) {

                OutputType output = request.getOutput(BLANK);
                outputForm.append(HtmlUtil.hidden(ARG_OUTPUT,
                        output.getId().toString()));
            }
            */

            List orderByList = new ArrayList();
            orderByList.add(new TwoFacedObject(msg("None"), "none"));
            orderByList.add(new TwoFacedObject(msg("From Date"), "fromdate"));
            orderByList.add(new TwoFacedObject(msg("To Date"), "todate"));
            orderByList.add(new TwoFacedObject(msg("Create Date"),
                    "createdate"));
            orderByList.add(new TwoFacedObject(msg("Name"), "name"));

            String orderBy = HtmlUtil.select(
                                 ARG_ORDERBY, orderByList,
                                 request.getString(
                                     ARG_ORDERBY,
                                     "none")) + HtmlUtil.checkbox(
                                         ARG_ASCENDING, "true",
                                         request.get(
                                             ARG_ASCENDING,
                                             false)) + HtmlUtil.space(1)
                                                 + msg("ascending");
            outputForm.append(HtmlUtil.formEntry(msgLabel("Order By"),
                    orderBy));
            outputForm.append(HtmlUtil.formEntry(msgLabel("Output"),
                    HtmlUtil.select(ARG_OUTPUT, getOutputHandlerSelectList(),
                                    request.getString(ARG_OUTPUT, ""))));

            outputForm.append(HtmlUtil.formTableClose());




            sb.append(HtmlUtil.makeShowHideBlock(msg("Output"),
                    outputForm.toString(), false));

        }

        if (servers.size() > 0) {
            StringBuffer serverSB  = new StringBuffer();
            int          serverCnt = 0;
            String       cbxId;
            String       call;

            cbxId = ATTR_SERVER + (serverCnt++);
            call = HtmlUtil.attr(HtmlUtil.ATTR_ONCLICK,
                                 HtmlUtil.call("checkboxClicked",
                                     HtmlUtil.comma("event",
                                         HtmlUtil.squote(ATTR_SERVER),
                                         HtmlUtil.squote(cbxId))));

            serverSB.append(HtmlUtil.checkbox(ARG_DOFRAMES, "true",
                    request.get(ARG_DOFRAMES, false)));
            serverSB.append(msg("Do frames"));
            serverSB.append(HtmlUtil.br());
            serverSB.append(HtmlUtil.checkbox(ATTR_SERVER,
                    ServerInfo.ID_THIS, false, HtmlUtil.id(cbxId) + call));
            serverSB.append(msg("Include this repository"));
            serverSB.append(HtmlUtil.br());
            for (ServerInfo server : servers) {
                cbxId = ATTR_SERVER + (serverCnt++);
                call = HtmlUtil.attr(HtmlUtil.ATTR_ONCLICK,
                                     HtmlUtil.call("checkboxClicked",
                                         HtmlUtil.comma("event",
                                             HtmlUtil.squote(ATTR_SERVER),
                                             HtmlUtil.squote(cbxId))));
                serverSB.append(HtmlUtil.checkbox(ATTR_SERVER,
                        server.getId(), false, HtmlUtil.id(cbxId) + call));
                serverSB.append(HtmlUtil.space(1));
                serverSB.append(server.getHref(" target=\"server\" "));
                serverSB.append(HtmlUtil.br());
            }
            sb.append(
                HtmlUtil.makeShowHideBlock(
                    msg("Remote Search Settings"),
                    HtmlUtil.div(
                        serverSB.toString(),
                        HtmlUtil.cssClass("serverdiv")), false));
        }




        if(!justText) {
            sb.append(HtmlUtil.p());
            sb.append(buttons);
            sb.append(HtmlUtil.p());
        }
        sb.append(HtmlUtil.formClose());



    }



    /**
     * _more_
     *
     * @return _more_
     */
    public List getOutputHandlerSelectList() {
        List tfos = new ArrayList<TwoFacedObject>();
        for (OutputHandler outputHandler :
                getRepository().getOutputHandlers()) {
            for (OutputType type : outputHandler.getTypes()) {
                if (type.getIsForSearch()) {
                    tfos.add(new HtmlUtil.Selector(type.getLabel(),
                            type.getId(),
                            getRepository().iconUrl(type.getIcon())));
                }
            }
        }
        return tfos;
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
    public Result processEntrySearchForm(Request request) throws Exception {
        return makeSearchForm(request, false, false);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param includeThis _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<ServerInfo> findServers(Request request, boolean includeThis)
            throws Exception {
        List<ServerInfo> servers = new ArrayList<ServerInfo>();
        for (String id :
                (List<String>) request.get(ATTR_SERVER, new ArrayList())) {
            if (id.equals(ServerInfo.ID_THIS) && !includeThis) {
                continue;
            }
            ServerInfo server = getRegistryManager().findRemoteServer(id);
            if (server == null) {
                continue;
            }
            servers.add(server);
        }
        return servers;
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
    public Result processRemoteSearch(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        List<String> servers = (List<String>) request.get(ATTR_SERVER,
                                   new ArrayList());
        sb.append(HtmlUtil.p());
        request.remove(ATTR_SERVER);

        boolean      didone   = false;
        StringBuffer serverSB = new StringBuffer();
        for (String id : servers) {
            ServerInfo server = getRegistryManager().findRemoteServer(id);
            if (server == null) {
                continue;
            }
            if ( !didone) {
                sb.append(header(msg("Selected Servers")));
            }
            serverSB.append(server.getHref(" target=\"server\" "));
            serverSB.append(HtmlUtil.br());
            didone = true;
        }

        if ( !didone) {
            sb.append(
                getRepository().showDialogNote(msg("No servers selected")));
        } else {
            sb.append(HtmlUtil.div(serverSB.toString(),
                                   HtmlUtil.cssClass("serverblock")));
            sb.append(HtmlUtil.p());
        }
        sb.append(HtmlUtil.p());
        sb.append(header(msg("Search Results")));

        return makeResult(request, msg("Remote Form"), sb);

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
    public Result processEntryBrowseSearchForm(Request request)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        getMetadataManager().addToBrowseSearchForm(request, sb);
        return makeResult(request, msg("Search Form"), sb);
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
        headerSB.append(getRepository().makeHeader(request, getSearchUrls(),
                ""));
        headerSB.append(sb);
        sb = headerSB;
        Result result = new Result(title, sb);
        return  addHeaderToAncillaryPage(request, result);
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
    public Result processEntrySearch(Request request) throws Exception {


        if (request.get(ARG_WAIT, false)) {
            return getRepository().getMonitorManager().processEntryListen(
                request);
        }

        //        System.err.println("submit:" + request.getString("submit","YYY"));
        if (request.defined("submit_type.x")
                || request.defined(ARG_SEARCH_SUBSET)) {
            request.remove(ARG_OUTPUT);
            return processEntrySearchForm(request);
        }


        boolean textSearch = isLuceneEnabled() && request.getString(ARG_SEARCH_TYPE,"").equals(SEARCH_TYPE_TEXT);

        StringBuffer     searchCriteriaSB = new StringBuffer();
        boolean          searchThis       = true;
        List<ServerInfo> servers          = null;

        ServerInfo       thisServer       = getRepository().getServerInfo();
        boolean          doFrames         = request.get(ARG_DOFRAMES, false);

        if (request.exists(ARG_SEARCH_SERVERS)) {
            servers = findServers(request, true);
            if (servers.size() == 0) {
                servers = getRegistryManager().getSelectedRemoteServers();
            }
            if (request.defined(ATTR_SERVER)) {
                searchThis = servers.contains(thisServer);
                if ( !doFrames) {
                    servers.remove(thisServer);
                }
            }
            if (servers.size() > 100) {
                throw new IllegalArgumentException("Too many remote servers:"
                        + servers.size());
            }
        }

        List<Entry> groups  = new ArrayList<Entry>();
        List<Entry> entries = new ArrayList<Entry>();

        if (textSearch) {
            processLuceneSearch(request, groups, entries);
        } else if (searchThis) {
            List[] pair = getEntryManager().getEntries(request,
                              searchCriteriaSB);
            groups.addAll((List<Entry>) pair[0]);
            entries.addAll((List<Entry>) pair[1]);
        }



        if ((servers != null) && (servers.size() > 0)) {
            request.remove(ATTR_SERVER);
            request.remove(ARG_SEARCH_SERVERS);

            if (doFrames) {
                String linkUrl = request.getUrlArgs();
                request.put(ARG_DECORATE, "false");
                request.put(ATTR_TARGET, "_server");
                String       embeddedUrl = request.getUrlArgs();
                StringBuffer sb          = new StringBuffer();
                sb.append(msgHeader("Remote Server Search Results"));
                for (ServerInfo server : servers) {
                    String remoteSearchUrl = server.getUrl()
                                             + URL_ENTRY_SEARCH.getPath()
                                             + "?" + linkUrl;
                    sb.append("\n");
                    sb.append(HtmlUtil.p());
                    String link = HtmlUtil.href(remoteSearchUrl,
                                      server.getUrl());
                    String fullUrl = server.getUrl()
                                     + URL_ENTRY_SEARCH.getPath() + "?"
                                     + embeddedUrl;
                    String content =
                        HtmlUtil.tag(
                            HtmlUtil.TAG_IFRAME,
                            HtmlUtil.attrs(
                                HtmlUtil.ATTR_WIDTH, "100%",
                                HtmlUtil.ATTR_HEIGHT, "200",
                                HtmlUtil.ATTR_SRC,
                                fullUrl), "need to have iframe support");
                    sb.append(HtmlUtil.makeShowHideBlock(server.getLabel()
                            + HtmlUtil.space(2) + link, content, true));

                    sb.append("\n");
                }
                request.remove(ARG_DECORATE);
                request.remove(ARG_TARGET);
                return new Result("Remote Search Results", sb);
            }

            Entry tmpGroup = getEntryManager().getDummyGroup();
            doDistributedSearch(request, servers, tmpGroup, groups, entries);
            Result result = getRepository().getOutputHandler(
                                request).outputGroup(
                                request, request.getOutput(), tmpGroup,
                                groups, entries);
            return result;

        }

        Entry theGroup = null;

        if (request.defined(ARG_GROUP)) {
            String groupId = (String) request.getString(ARG_GROUP, "").trim();
            //            System.err.println("group:" + groupId);
            theGroup = getEntryManager().findGroup(request, groupId);
        }


        String s = searchCriteriaSB.toString();
        if (request.defined(ARG_TARGET)) {
            s = "";
        }


        //        if (s.length() > 0) {
        StringBuffer searchForm = new StringBuffer();
        request.remove(ARG_SEARCH_SUBMIT);
        String url = request.getUrl(URL_SEARCH_FORM);
        String searchLink = HtmlUtil.href(url,
                                          HtmlUtil.img(iconUrl(ICON_SEARCH),
                                              "Search Again"));
        //            searchForm.append(searchLink);
        if (s.length() > 0) {
            searchForm.append(msg("Search Criteria") + "<br><table>" + s
                              + "</table>");
        }
        boolean foundAny = groups.size()>0 || entries.size()>0;
        if(foundAny) {
            String searchUrl = request.getUrl();
            searchForm.append(HtmlUtil.href(searchUrl, msg("Search URL")));
            searchForm.append(HtmlUtil.br());
        }



        makeSearchForm(request, textSearch, true, searchForm);


        String form = HtmlUtil.makeShowHideBlock(
                          searchLink + msg("Search Again"),
                          RepositoryUtil.inset(
                              searchForm.toString(), 0, 20, 0, 0), false);
        StringBuffer header = new StringBuffer();
        header.append(getRepository().makeHeader(request, getSearchUrls(),
                ""));
        header.append(msgHeader("Search Results"));

        if(foundAny) {
            header.append(form);
        } else {
            header.append(searchForm);
        }

        request.setLeftMessage(header.toString());
        //        }
        if (theGroup == null) {
            theGroup = getEntryManager().getDummyGroup();
        }
        Result result =
            getRepository().getOutputHandler(request).outputGroup(request,
                                             request.getOutput(), theGroup,
                                             groups, entries);
        //        return makeResult(request, msg("Search Results"), sb);
        if(theGroup.isDummy()) {
            return addHeaderToAncillaryPage(request, result);
        } 
        return getEntryManager().addEntryHeader(request, theGroup, result);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param servers _more_
     * @param tmpEntry _more_
     * @param groups _more_
     * @param entries _more_
     *
     * @throws Exception _more_
     */
    private void doDistributedSearch(final Request request,
                                     List<ServerInfo> servers,
                                     Entry tmpEntry,
                                     final List<Entry> groups,
                                     final List<Entry> entries)
            throws Exception {

        String output = request.getString(ARG_OUTPUT, "");
        request.put(ARG_OUTPUT, XmlOutputHandler.OUTPUT_XML);
        final String    linkUrl     = request.getUrlArgs();
        ServerInfo      thisServer  = getRepository().getServerInfo();
        final int[]     runnableCnt = { 0 };
        final boolean[] running     = { true };
        //TODO: We need to cap the number of servers we're searching on
        List<Runnable> runnables = new ArrayList<Runnable>();
        for (ServerInfo server : servers) {
            if (server.equals(thisServer)) {
                continue;
            }
            final Entry parentEntry =
                new Entry(getRepository().getGroupTypeHandler(), true);
            parentEntry.setId(
                getEntryManager().getRemoteEntryId(server.getUrl(), ""));
            getEntryManager().cacheEntry(parentEntry);
            parentEntry.setRemoteServer(server.getUrl());
            parentEntry.setIsRemoteEntry(true);
            parentEntry.setUser(getUserManager().getAnonymousUser());
            parentEntry.setParentEntry(tmpEntry);
            parentEntry.setName(server.getUrl());
            final ServerInfo theServer = server;
            Runnable         runnable  = new Runnable() {
                public void run() {
                    String remoteSearchUrl = theServer.getUrl()
                                             + URL_ENTRY_SEARCH.getPath()
                                             + "?" + linkUrl;

                    try {
                        String entriesXml =
                            getStorageManager().readSystemResource(
                                new URL(remoteSearchUrl));
                        //                            System.err.println(entriesXml);
                        if ( !running[0]) {
                            return;
                        }
                        Element  root     = XmlUtil.getRoot(entriesXml);
                        NodeList children = XmlUtil.getElements(root);
                        //Synchronize on the groups list so only one thread at  a time adds its entries to it
                        synchronized (groups) {
                            for (int i = 0; i < children.getLength(); i++) {
                                Element node = (Element) children.item(i);
                                //                    if (!node.getTagName().equals(TAG_ENTRY)) {continue;}
                                Entry entry =
                                    getEntryManager().processEntryXml(
                                        request, node, parentEntry,
                                        new Hashtable(), false, false);

                                entry.setResource(
                                    new Resource(
                                        "remote:"
                                        + XmlUtil.getAttribute(
                                            node, ATTR_RESOURCE,
                                            ""), Resource.TYPE_REMOTE_FILE));
                                entry.setId(
                                    getEntryManager().getRemoteEntryId(
                                        theServer.getUrl(),
                                        XmlUtil.getAttribute(node, ATTR_ID)));
                                entry.setIsRemoteEntry(true);
                                entry.setRemoteServer(theServer.getUrl());
                                getEntryManager().cacheEntry(entry);
                                if (entry.isGroup()) {
                                    groups.add((Entry) entry);
                                } else {
                                    entries.add((Entry) entry);
                                }
                            }
                        }
                    } catch (Exception exc) {
                        logException("Error doing search:" + remoteSearchUrl,
                                     exc);
                    } finally {
                        synchronized (runnableCnt) {
                            runnableCnt[0]--;
                        }
                    }
                }

                public String toString() {
                    return "Runnable:" + theServer.getUrl();
                }
            };
            runnables.add(runnable);
        }


        runnableCnt[0] = runnables.size();
        for (Runnable runnable : runnables) {
            Misc.runInABit(0, runnable);
        }


        //Wait at most 10 seconds for all of the thread to finish
        long t1 = System.currentTimeMillis();
        while (true) {
            synchronized (runnableCnt) {
                if (runnableCnt[0] <= 0) {
                    break;
                }
            }
            //Busy loop
            Misc.sleep(100);
            long t2 = System.currentTimeMillis();
            //Wait at most 10 seconds
            if ((t2 - t1) > 20000) {
                logInfo("Remote search waited too long");
                break;
            }
        }
        running[0] = false;



        request.put(ARG_OUTPUT, output);


    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param what _more_
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected List getSearchFormLinks(Request request, String what)
            throws Exception {
        TypeHandler typeHandler = getRepository().getTypeHandler(request);
        List        links       = new ArrayList();
        String      extra1      = " class=subnavnolink ";
        String      extra2      = " class=subnavlink ";
        String[]    whats       = { WHAT_ENTRIES, WHAT_TAG,
                                    WHAT_ASSOCIATION };
        String[]    names       = { "Entries", "Tags", "Associations" };

        String      formType    = request.getString(ARG_FORM_TYPE, "basic");

        for (int i = 0; i < whats.length; i++) {
            String item;
            if (what.equals(whats[i])) {
                item = HtmlUtil.span(names[i], extra1);
            } else {
                item = HtmlUtil.href(request.url(URL_SEARCH_FORM, ARG_WHAT,
                        whats[i], ARG_FORM_TYPE, formType), names[i], extra2);
            }
            if (i == 0) {
                item = "<span " + extra1
                       + ">Search For:&nbsp;&nbsp;&nbsp; </span>" + item;
            }
            links.add(item);
        }

        List<TwoFacedObject> whatList = typeHandler.getListTypes(false);
        for (TwoFacedObject tfo : whatList) {
            if (tfo.getId().equals(what)) {
                links.add(HtmlUtil.span(tfo.toString(), extra1));
            } else {
                links.add(HtmlUtil.href(request.url(URL_SEARCH_FORM,
                        ARG_WHAT, BLANK + tfo.getId(), ARG_TYPE,
                        typeHandler.getType()), tfo.toString(), extra2));
            }
        }

        return links;
    }

}
