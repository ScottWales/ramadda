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

package org.ramadda.plugins.gdata;


import com.google.gdata.client.*;
import com.google.gdata.client.*;
import com.google.gdata.client.calendar.*;

import com.google.gdata.client.docs.*;
import com.google.gdata.client.photos.*;
import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.MediaContent;
//import com.google.gdata.data.*;
import com.google.gdata.data.Person;
import com.google.gdata.data.TextContent;
import com.google.gdata.data.acl.*;
import com.google.gdata.data.docs.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.data.media.*;
import com.google.gdata.data.photos.*;
import com.google.gdata.util.*;
import com.google.gdata.util.*;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.StringUtil;

import java.io.File;

import java.net.URL;







import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 * Class TypeHandler _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class DocsTypeHandler extends GdataTypeHandler {

    /** _more_ */
    public static final String TYPE_FOLDER = "folder";

    /** _more_ */
    public static final String TYPE_DOCUMENT = "document";

    /** _more_ */
    public static final String TYPE_SPREADSHEET = "spreadsheet";

    /** _more_ */
    public static final String TYPE_PDF = "pdf";

    /** _more_ */
    public static final String TYPE_DRAWING = "drawing";

    /** _more_ */
    public static final String TYPE_PRESENTATION = "presentation";


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public DocsTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param userId _more_
     * @param password _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected GoogleService doMakeService(String userId, String password)
            throws Exception {
        DocsService service = new DocsService("ramadda");
        service.setUserCredentials(userId, password);

        return service;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param parentEntry _more_
     * @param synthId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<String> getSynthIds(Request request, Entry mainEntry,
                                    Entry parentEntry, String synthId)
            throws Exception {
        Hashtable<String, Entry> entryMap = new Hashtable<String, Entry>();

        return getSynthIds(request, mainEntry, parentEntry, synthId,
                           entryMap);
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
    public TypeHandler getTypeHandlerForCopy(Entry entry) throws Exception {
        if ((entry.getId().indexOf(TYPE_FOLDER) >= 0)
                || !getEntryManager().isSynthEntry(entry.getId())) {
            return getRepository().getTypeHandler(TypeHandler.TYPE_GROUP);
        }

        return getRepository().getTypeHandler(TypeHandler.TYPE_FILE);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param parentEntry _more_
     * @param synthId _more_
     * @param entryMap _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<String> getSynthIds(Request request, Entry mainEntry,
                                    Entry parentEntry, String synthId,
                                    Hashtable<String, Entry> entryMap)
            throws Exception {

        List<String> ids = ((parentEntry != null)
                            ? parentEntry.getChildIds()
                            : null);
        if (ids != null) {
            return ids;
        }
        ids = new ArrayList<String>();
        if (mainEntry == null) {
            return ids;
        }

        String url =
            "https://docs.google.com/feeds/default/private/full?showfolders=true";
        DocumentQuery    query      = new DocumentQuery(new URL(url));
        DocumentListFeed allEntries = new DocumentListFeed();
        GoogleService    service    = getService(mainEntry);
        if (service == null) {
            return ids;
        }
        DocumentListFeed tempFeed = service.getFeed(query,
                                        DocumentListFeed.class);
        do {
            allEntries.getEntries().addAll(tempFeed.getEntries());
            com.google.gdata.data.Link link = tempFeed.getNextLink();
            if (link == null) {
                break;
            }
            tempFeed = service.getFeed(new URL(link.getHref()),
                                       DocumentListFeed.class);
        } while (tempFeed.getEntries().size() > 0);

        List<Entry> newEntries = new ArrayList<Entry>();
        entryMap.put(mainEntry.getId(), mainEntry);
        for (DocumentListEntry docListEntry : allEntries.getEntries()) {
            java.util.List<com.google.gdata.data.Link> links =
                docListEntry.getParentLinks();
            Entry newEntry;
            String entryId =
                getSynthId(mainEntry,
                           IOUtil.getFileTail(docListEntry.getId()));
            String parentId = ((links.size() == 0)
                               ? mainEntry.getId()
                               : getSynthId(
                                   mainEntry,
                                   IOUtil.getFileTail(
                                       links.get(0).getHref())));
            boolean isFolder = docListEntry.getType().equals(TYPE_FOLDER);
            //            System.err.println(docListEntry.getType() + " " + docListEntry.getTitle().getPlainText() + " " + isFolder +" " );
            Resource resource;
            if (isFolder) {
                resource = new Resource();
            } else {
                resource =
                    new Resource(docListEntry.getDocumentLink().getHref());
                resource.setFileSize(
                    docListEntry.getQuotaBytesUsed().longValue());
            }
            StringBuffer desc = new StringBuffer();
            newEntry = new Entry(entryId, this, isFolder);
            newEntries.add(newEntry);
            System.err.println("ID:" + newEntry.getId());
            entryMap.put(newEntry.getId(), newEntry);
            newEntry.addMetadata(new Metadata(getRepository().getGUID(),
                    newEntry.getId(), "gdata.lastmodifiedby", false,
                    docListEntry.getLastModifiedBy().getName(),
                    docListEntry.getLastModifiedBy().getEmail(), "", "", ""));

            addMetadata(newEntry, docListEntry, desc);
            //            entries.add(newEntry);
            Date publishTime =
                new Date(docListEntry.getPublished().getValue());
            Date lastViewedTime = ((docListEntry.getLastViewed() != null)
                                   ? new Date(docListEntry.getLastViewed()
                                       .getValue())
                                   : publishTime);
            Date editTime = new Date(docListEntry.getUpdated().getValue());
            newEntry.initEntry(docListEntry.getTitle().getPlainText(),
                               desc.toString(), null, mainEntry.getUser(),
                               resource, "", publishTime.getTime(),
                               editTime.getTime(), publishTime.getTime(),
                               lastViewedTime.getTime(), null);

            newEntry.setParentEntryId(parentId);
        }
        for (Entry newEntry : newEntries) {
            if (newEntry.getParentEntryId().equals(mainEntry.getId())) {
                //                System.err.println ("is top level:" + newEntry.getParentEntryId() + " " + newEntry.getName());
                ids.add(newEntry.getId());
                newEntry.setParentEntry(mainEntry);
            } else {
                Entry tmpParentEntry =
                    entryMap.get(newEntry.getParentEntryId());
                if (tmpParentEntry == null) {
                    continue;
                }
                //                System.err.println ("adding to parent:" + newEntry.getParentEntryId() + " " + newEntry.getName());
                if (tmpParentEntry.getChildIds() == null) {
                    tmpParentEntry.setChildIds(new ArrayList<String>());
                }
                tmpParentEntry.getChildIds().add(newEntry.getId());
                newEntry.setParentEntry(tmpParentEntry);
            }
            getEntryManager().cacheEntry(newEntry);
        }

        return ids;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry makeSynthEntry(Request request, Entry mainEntry, String id)
            throws Exception {
        id = getSynthId(mainEntry, id);
        Hashtable<String, Entry> entryMap = new Hashtable<String, Entry>();
        getSynthIds(request, mainEntry, null, id, entryMap);
        Entry newEntry = entryMap.get(id);
        System.err.println("newEntry:" + newEntry + " " + id);

        return newEntry;
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
    public String getIconUrl(Request request, Entry entry) throws Exception {
        String id = entry.getId();
        if ( !getEntryManager().isSynthEntry(id)) {
            return super.getIconUrl(request, entry);
        }
        if (id.indexOf(TYPE_FOLDER) >= 0) {
            return iconUrl("/icons/folder.png");
        }
        if (id.indexOf(TYPE_DOCUMENT) >= 0) {
            return iconUrl("/gdata/document.gif");
        }
        if (id.indexOf(TYPE_PRESENTATION) >= 0) {
            return iconUrl("/gdata/presentation.gif");
        }
        if (id.indexOf(TYPE_DRAWING) >= 0) {
            return iconUrl("/gdata/drawing.gif");
        }
        if (id.indexOf(TYPE_SPREADSHEET) >= 0) {
            return iconUrl("/gdata/spreadsheet.gif");
        }
        if (id.indexOf(TYPE_PDF) >= 0) {
            return iconUrl("/icons/pdf.png");
        }

        return super.getIconUrl(request, entry);
    }




    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        DocsService client = new DocsService("ramadda");
        client.setUserCredentials("jeff.mcwhirter@gmail.com", args[0]);
        //        DocumentQuery query = new DocumentQuery(new URL("https://docs.google.com/feeds/default/private/full/-/folder"));
        String url =
            "https://docs.google.com/feeds/default/private/full/folder%3Aroot/contents?showfolders=true";
        url = "https://docs.google.com/feeds/default/private/full/folder%3Aroot/contents?showfolders=true";
        //        String url = "https://docs.google.com/feeds/default/private/full?showfolders=true";
        DocumentQuery    query      = new DocumentQuery(new URL(url));
        DocumentListFeed allEntries = new DocumentListFeed();
        DocumentListFeed tempFeed = client.getFeed(query,
                                        DocumentListFeed.class);
        do {
            allEntries.getEntries().addAll(tempFeed.getEntries());
            com.google.gdata.data.Link link = tempFeed.getNextLink();
            if (link == null) {
                break;
            }
            if (true) {
                break;
            }
            tempFeed = client.getFeed(new URL(link.getHref()),
                                      DocumentListFeed.class);
        } while (tempFeed.getEntries().size() > 0);

        List<DocumentListEntry> topLevel = new ArrayList<DocumentListEntry>();
        System.out.println("query url:" + url);
        System.out.println("User has " + allEntries.getEntries().size()
                           + " total entries");
        for (DocumentListEntry entry : allEntries.getEntries()) {
            java.util.List<com.google.gdata.data.Link> links =
                entry.getParentLinks();
            if (links.size() == 0) {
                topLevel.add(entry);
                System.out.println("Top level:" + entry.getType() + " "
                                   + entry.getTitle().getPlainText() + " "
                                   + entry.getId());
            } else {
                System.out.println("Not top level " + entry.getType() + " "
                                   + entry.getTitle().getPlainText() + " "
                                   + entry.getId());
            }
            //            https://docs.google.com/feeds/default/private/full/folder%3Afolder_id/contents
            for (com.google.gdata.data.Link link : links) {
                System.out.println("\t" + link.getHref() + " "
                                   + link.getTitle());
            }
        }


    }


}
