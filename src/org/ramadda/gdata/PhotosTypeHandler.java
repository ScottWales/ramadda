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
import ucar.unidata.repository.type.*;







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
public class PhotosTypeHandler extends GdataTypeHandler {

    public static final String TYPE_ALBUM = "album";

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public PhotosTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isSynthType() {
        return true;
    }


    public List<String> getSynthIds(Request request, Entry mainEntry,
                                    Entry parentEntry, String synthId)
            throws Exception {
        List<String> ids    = new ArrayList<String>();
        String userId = getUserId(mainEntry);
        String password = getPassword(mainEntry);
        if (userId == null || password == null) {
            return ids;
        }

        PicasawebService myService = new PicasawebService("ramadda");
        myService.setUserCredentials(userId, password);
        URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/" +userId +"?kind=album");
        UserFeed myUserFeed = myService.getFeed(feedUrl, UserFeed.class);

        for (AlbumEntry album : myUserFeed.getAlbumEntries()) {
            System.out.println(album.getTitle().getPlainText());
            String id = getSynthId(mainEntry, TYPE_ALBUM, album.getId());
            ids.add(id);
        }


        return ids;

    }




    public Entry makeSynthEntry(Request request, Entry parentEntry, String id)
            throws Exception {
        System.err.println ("ID:" + id);

        String synthId = Repository.ID_PREFIX_SYNTH + parentEntry.getId()
                         + ":" + id;
        TypeHandler handler =  getRepository().getTypeHandler(TypeHandler.TYPE_GROUP);
        Entry entry =  new Entry(synthId, handler);
        if (entry instanceof Entry) {
            entry.setIcon("/gdata/icons/picasa.png");
        }

        String name = "album";
        Entry parent = parentEntry;
        Date dttm = new Date();
        //        entry.initEntry(name, "", parent, getUserManager().getLocalFileUser(),
        //                        null, "", dttm.getTime(),dttm.getTime(),dttm.getTime(),
        //                        null);
        return entry;
    }





    public static void main(String[]args) throws Exception {
        DocsService client = new DocsService("ramadda");
        client.setUserCredentials("jeff.mcwhirter@gmail.com", args[0]);

        //        DocumentQuery query = new DocumentQuery(new URL("https://docs.google.com/feeds/default/private/full/-/folder"));
        DocumentQuery query = new DocumentQuery(new URL("https://docs.google.com/feeds/default/private/full"));
        DocumentListFeed allEntries = new DocumentListFeed();
        DocumentListFeed tempFeed = client.getFeed(query, DocumentListFeed.class);
        do {
            allEntries.getEntries().addAll(tempFeed.getEntries());
            com.google.gdata.data.Link link  =tempFeed.getNextLink();
            if(link==null) break;
            tempFeed = client.getFeed(new URL(link.getHref()), DocumentListFeed.class);
        } while (tempFeed.getEntries().size() > 0);

        System.out.println("User has " + allEntries.getEntries().size() + " total entries");
        for (DocumentListEntry entry : allEntries.getEntries()) {
            System.out.println(entry.getType()+" " +entry.getTitle().getPlainText());
        }


    }


}