/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.gdata;


import org.w3c.dom.*;


import ucar.unidata.repository.*;
import ucar.unidata.repository.metadata.*;
import ucar.unidata.repository.type.*;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.HtmlUtil;







import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Date;
import java.io.File;
import java.net.URL;

import com.google.gdata.client.*;
import com.google.gdata.client.photos.*;
import com.google.gdata.data.BaseEntry;
//import com.google.gdata.data.*;
import com.google.gdata.data.Person;
import  com.google.gdata.data.TextContent;
import com.google.gdata.data.media.*;
import com.google.gdata.data.photos.*;
import com.google.gdata.client.*;
import com.google.gdata.client.calendar.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;

import com.google.gdata.client.docs.*;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.acl.*;
import com.google.gdata.data.docs.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;


/**
 * Class TypeHandler _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class DocsTypeHandler extends GdataTypeHandler {

    public static final String TYPE_FOLDER = "folder";
    public static final String TYPE_DOCUMENT = "document";
    public static final String TYPE_SPREADSHEET = "spreadsheet";
    public static final String TYPE_PDF = "pdf";
    public static final String TYPE_DRAWING = "drawing";
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


    protected GoogleService doMakeService(String userId, String password) throws Exception {
        DocsService service = new DocsService("ramadda");
        service.setUserCredentials(userId, password);
        return service;
    }



    public List<String> getSynthIds(Request request, Entry mainEntry,
                                    Entry parentEntry, String synthId)
        throws Exception {

        List<String> ids    = parentEntry.getChildIds();
        if(ids!=null) return ids;
        ids = new ArrayList<String>();
        if(synthId!=null)
            return ids;

        String url = "https://docs.google.com/feeds/default/private/full?showfolders=true";
        DocumentQuery query = new DocumentQuery(new URL(url));
        DocumentListFeed allEntries = new DocumentListFeed();
        DocumentListFeed tempFeed = getService(mainEntry).getFeed(query, DocumentListFeed.class);
        do {
            allEntries.getEntries().addAll(tempFeed.getEntries());
            com.google.gdata.data.Link link  =tempFeed.getNextLink();
            if(link==null) break;
            tempFeed = getService(mainEntry).getFeed(new URL(link.getHref()), DocumentListFeed.class);
        } while (tempFeed.getEntries().size() > 0);

        Hashtable<String,Entry> entryMap = new Hashtable<String,Entry>();
        List<Entry> newEntries = new ArrayList<Entry>();
        entryMap.put(mainEntry.getId(), mainEntry);
        for (DocumentListEntry docListEntry : allEntries.getEntries()) {
            java.util.List<com.google.gdata.data.Link> links = docListEntry.getParentLinks();
            Entry newEntry;
            String entryId = getSynthId(mainEntry, docListEntry.getType(), IOUtil.getFileTail(docListEntry.getId()));
            String parentId = (links.size()==0?mainEntry.getId():getSynthId(mainEntry, TYPE_FOLDER, IOUtil.getFileTail(links.get(0).getHref())));
            boolean isFolder = docListEntry.getType().equals(TYPE_FOLDER);
            System.err.println(docListEntry.getType() + " " + docListEntry.getTitle().getPlainText() + " " + isFolder +" " );
            Resource resource;
            if(isFolder) {
                resource = new Resource();
            } else {
                resource =  new Resource(docListEntry.getDocumentLink().getHref());
                resource.setFileSize(docListEntry.getQuotaBytesUsed().longValue());
            }
            StringBuffer desc = new StringBuffer();
            newEntry =  new Entry(entryId, this, isFolder);
            newEntries.add(newEntry);
            entryMap.put(newEntry.getId(), newEntry);
            newEntry.setParentEntryId(parentId);
            newEntry.addMetadata(new Metadata(getRepository().getGUID(), newEntry.getId(),"gdata.lastmodifiedby", false,
                                          docListEntry.getLastModifiedBy().getName(),
                                          docListEntry.getLastModifiedBy().getEmail(),
                                          "","",""));

            addMetadata(newEntry, docListEntry, desc);
            //            entries.add(newEntry);
            Date publishTime =  new Date(docListEntry.getPublished().getValue());
            Date lastViewedTime =  (docListEntry.getLastViewed()!=null?new Date(docListEntry.getLastViewed().getValue()):publishTime);
            Date editTime =  new Date(docListEntry.getUpdated().getValue());
            newEntry.initEntry(docListEntry.getTitle().getPlainText(), desc.toString(), mainEntry, mainEntry.getUser(),
                            resource, "", publishTime.getTime(),editTime.getTime(),publishTime.getTime(),lastViewedTime.getTime(),
                            null);

        }
        for(Entry newEntry: newEntries) {
            if(newEntry.getParentEntryId().equals(mainEntry.getId())) {
                System.err.println ("is top level:" + newEntry.getParentEntryId() + " " + newEntry.getName());
                ids.add(newEntry.getId());
                newEntry.setParentEntry(mainEntry);
            } else {
                Entry tmpParentEntry = entryMap.get(newEntry.getParentEntryId());
                if(tmpParentEntry==null) {
                    System.err.println ("null:" + newEntry.getParentEntryId() + " " + newEntry.getName());
                    continue;
                }
                System.err.println ("adding to parent:" + newEntry.getParentEntryId() + " " + newEntry.getName());
                if(tmpParentEntry.getChildIds()==null) {
                    tmpParentEntry.setChildIds(new ArrayList<String>());
                }
                tmpParentEntry.getChildIds().add(newEntry.getId());
                newEntry.setParentEntry(tmpParentEntry);
            }
            getEntryManager().cacheEntry(newEntry);
        }
        return ids;
    }

    public String getIconUrl(Request request, Entry entry) throws Exception {
        String id = entry.getId();
        if(!getEntryManager().isSynthEntry(id)) return super.getIconUrl(request, entry);
        if(id.indexOf(TYPE_FOLDER)>=0)  {
            return iconUrl("/icons/folder.png");
        }
        if(id.indexOf(TYPE_DOCUMENT)>=0)  {
            return iconUrl("/gdata/document.gif");
        }
        if(id.indexOf(TYPE_PRESENTATION)>=0)  {
            return iconUrl("/gdata/presentation.gif");
        }
        if(id.indexOf(TYPE_DRAWING)>=0)  {
            return iconUrl("/gdata/drawing.gif");
        }
        if(id.indexOf(TYPE_SPREADSHEET)>=0)  {
            return iconUrl("/gdata/spreadsheet.gif");
        }
        if(id.indexOf(TYPE_PDF)>=0)  {
            return iconUrl("/icons/pdf.png");
        }

        return  super.getIconUrl(request, entry);
    }


    public static void main(String[]args) throws Exception {
        DocsService client = new DocsService("ramadda");
        client.setUserCredentials("jeff.mcwhirter@gmail.com", args[0]);
        //        DocumentQuery query = new DocumentQuery(new URL("https://docs.google.com/feeds/default/private/full/-/folder"));
        String url = "https://docs.google.com/feeds/default/private/full/folder%3Aroot/contents?showfolders=true";
        url = "https://docs.google.com/feeds/default/private/full/folder%3Aroot/contents?showfolders=true";
        //        String url = "https://docs.google.com/feeds/default/private/full?showfolders=true";
        DocumentQuery query = new DocumentQuery(new URL(url));
        DocumentListFeed allEntries = new DocumentListFeed();
        DocumentListFeed tempFeed = client.getFeed(query, DocumentListFeed.class);
        do {
            allEntries.getEntries().addAll(tempFeed.getEntries());
            com.google.gdata.data.Link link  =tempFeed.getNextLink();
            if(link==null) break;
            if(true) break;
            tempFeed = client.getFeed(new URL(link.getHref()), DocumentListFeed.class);
        } while (tempFeed.getEntries().size() > 0);

        List<DocumentListEntry>topLevel = new ArrayList<DocumentListEntry>();
        System.out.println("query url:" + url);
        System.out.println("User has " + allEntries.getEntries().size() + " total entries");
        for (DocumentListEntry entry : allEntries.getEntries()) {
           java.util.List<com.google.gdata.data.Link> links = entry.getParentLinks();
           if(links.size()==0) {
               topLevel.add(entry);
               System.out.println("Top level:" +entry.getType()+" " +entry.getTitle().getPlainText() +" " + entry.getId());
           } else {
                System.out.println("Not top level " +entry.getType()+" " +entry.getTitle().getPlainText() +" " + entry.getId());
           }
           //            https://docs.google.com/feeds/default/private/full/folder%3Afolder_id/contents
           for(com.google.gdata.data.Link link: links) {
               System.out.println("\t" + link.getHref() +" " + link.getTitle());
           }
        }


    }


}