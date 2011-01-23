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







import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.io.File;
import java.net.URL;

import com.google.gdata.client.*;
import com.google.gdata.client.photos.*;
//import com.google.gdata.data.*;
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
    public static final String TYPE_FILE = "file";

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


    private DocsService getService(Entry entry) throws Exception {
        String userId = getUserId(entry);
        String password = getPassword(entry);
        if (userId == null || password == null) {
            return null;
        }
        DocsService service = new DocsService("ramadda");
        service.setUserCredentials(userId, password);
        return service;
    }


    /*
    public List<String> getAlbumIds(Request request, Entry entry)
            throws Exception {
        List<String> ids    = entry.getChildIds();
        if(ids!=null) return ids;
        ids = new ArrayList<String>();
        for(Entry album: getAlbumEntries(request, entry)) {
            ids.add(album.getId());
        }
        entry.setChildIds(ids);
        return ids;
    }


    public List<Entry> getAlbumEntries(Request request, Entry entry)
        throws Exception {
        List<Entry> entries    = new ArrayList<Entry>();
        System.err.println("getAlbumEntries from picasa");
        String userId = getUserId(entry);
        if(userId ==null) return entries;

        URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/" +userId +"?kind=album");
        UserFeed myUserFeed = getService(entry).getFeed(feedUrl, UserFeed.class);
        for (AlbumEntry album : myUserFeed.getAlbumEntries()) {
            String albumEntryId = getSynthId(entry, TYPE_ALBUM, album.getGphotoId());
            String title = album.getTitle().getPlainText();
            Entry newEntry =  new Entry(albumEntryId, this,true);
            entries.add(newEntry);
            newEntry.setIcon("/gdata/picasa.png");
            Date dttm = album.getDate();
            Date now = new Date();
            newEntry.initEntry(title, "", entry, getUserManager().getLocalFileUser(),
                            new Resource(), "", dttm.getTime(),dttm.getTime(),dttm.getTime(),dttm.getTime(),
                            null);
            getEntryManager().cacheEntry(newEntry);
        }
        return entries;
    }




    public List<String> getSynthIds(Request request, Entry mainEntry,
                                    Entry parentEntry, String synthId)
            throws Exception {
        if(synthId==null) {
            return getAlbumIds(request, mainEntry);
        }

        List<String> ids    = parentEntry.getChildIds();
        if(ids!=null) return ids;
        ids = new ArrayList<String>();


        List<String> toks = StringUtil.split(synthId,":");
        String type = toks.get(0);
        String albumId = toks.get(1);
        for(Entry photoEntry: getPhotoEntries(request, mainEntry, parentEntry, albumId)) {
            ids.add(photoEntry.getId());
        }
        parentEntry.setChildIds(ids);
        return ids;
    }

    public List<Entry> getPhotoEntries(Request request, Entry mainEntry,
                                       Entry parentEntry, String albumId)
        throws Exception {
        System.err.println("getPhotoEntries from picasa:" + albumId);
        List<Entry> entries = new ArrayList<Entry>();
        String userId = getUserId(mainEntry);
        URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/" + userId +"/albumid/" + albumId);
        AlbumFeed feed = getService(mainEntry).getFeed(feedUrl, AlbumFeed.class);
        for(PhotoEntry photo : feed.getPhotoEntries()) {
            String name = photo.getTitle().getPlainText();
            String newId = getSynthId(mainEntry, TYPE_PHOTO, photo.getAlbumId()+":"+photo.getGphotoId());
            Entry newEntry =  new Entry(newId, this);
            entries.add(newEntry);
            //            newEntry.setIcon("/gdata/picasa.png");
            Date dttm = new Date();
            Date timestamp = photo.getTimestamp();
            Resource resource = new Resource();
            java.util.List<com.google.gdata.data.media.mediarss.MediaContent> media = photo.getMediaContents();
            if(media.size()>0) {
                resource = new Resource(media.get(0).getUrl());
                resource.setFileSize(photo.getSize());
            }
            newEntry.initEntry(name, "", parentEntry, getUserManager().getLocalFileUser(),
                            resource, "", dttm.getTime(),dttm.getTime(), timestamp.getTime(),timestamp.getTime(),
                            null);
            com.google.gdata.data.geo.Point point = photo.getGeoLocation();
            if(point!=null) {
                newEntry.setNorth(point.getLatitude().doubleValue());
                newEntry.setSouth(point.getLatitude().doubleValue());
                newEntry.setWest(point.getLongitude().doubleValue());
                newEntry.setEast(point.getLongitude().doubleValue());
            }

            java.util.List<com.google.gdata.data.media.mediarss.MediaThumbnail> thumbs = photo.getMediaThumbnails() ;
            //            for(int i=0;i<thumbs.size();i++) {
            if(thumbs.size()>0) {
                Metadata thumbnailMetadata =                                                                                                
                    new Metadata(getRepository().getGUID(), newId, ContentMetadataHandler.TYPE_THUMBNAIL, false,                    
                                 thumbs.get(0).getUrl(),null,null,null,null);
                newEntry.addMetadata(thumbnailMetadata);
            }
            getEntryManager().cacheEntry(newEntry);
        }
        return entries;
    }


    public Entry makeSynthEntry(Request request, Entry mainEntry, String id)
        throws Exception {
        
        String userId = getUserId(mainEntry);
        System.err.println ("ID:" + id);
        List<String> toks = StringUtil.split(id,":");
        String type = toks.get(0);
        if(type.equals(TYPE_ALBUM)) {
            for(Entry album: getAlbumEntries(request, mainEntry)) {
                if(album.getId().endsWith(id)) {
                    return album;
                }
            }
            return null;
        }

        String albumId =  toks.get(1);
        String albumEntryId = getSynthId(mainEntry, TYPE_ALBUM, albumId);
        Entry albumEntry = getEntryManager().getEntry(request, albumEntryId);
        String photoEntryId =  getSynthId(mainEntry, TYPE_PHOTO, toks.get(1)+":" + toks.get(2));
        for(Entry photoEntry: getPhotoEntries(request, mainEntry, albumEntry, albumId)) {
            if(photoEntry.getId().equals(photoEntryId)) {
                return photoEntry;
            }
        }
        return null;
    }


    public String getIconUrl(Request request, Entry entry) throws Exception {
        if(entry.getId().indexOf(TYPE_PHOTO)>=0) 
            return iconUrl("/icons/jpg.png");
        return iconUrl("/gdata/picasa.png");
    }



    */

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