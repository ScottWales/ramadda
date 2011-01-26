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

import com.google.gdata.data.Feed;

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
public class BloggerTypeHandler extends GdataTypeHandler {

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public BloggerTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    protected GoogleService getService(Entry entry) throws Exception {
        GoogleService myService = new GoogleService("blogger", "exampleCo-exampleApp-1");
        //        myService.setUserCredentials("user@example.com", "secretPassword");
        return myService;
    }


    public List<String> getSynthIds(Request request, Entry mainEntry,
                                    Entry parentEntry, String synthId)
        throws Exception {
        List<String> ids    = parentEntry.getChildIds();
        if(synthId!=null) return ids;
        if(ids!=null) return ids;
        ids = new ArrayList<String>();
        List<Entry> entries= getBlogEntries(request, mainEntry, parentEntry, synthId);
        for(Entry entry: entries) {
            ids.add(entry.getId());
        }
        return ids;
    }


    public List<Entry> getBlogEntries(Request request, Entry mainEntry,
                                      Entry parentEntry, String synthId)
        throws Exception {
        List<Entry> entries= new ArrayList<Entry>();
        String blogId = mainEntry.getValue(2,(String)null);
        if(blogId==null) return entries;
        URL feedUrl = new URL("http://www.blogger.com/feeds/" + blogId +"/posts/default");
        System.err.println(feedUrl);
        Feed resultFeed = getService(mainEntry).getFeed(feedUrl, Feed.class);
        System.out.println(resultFeed.getTitle().getPlainText());
        for (int i = 0; i < resultFeed.getEntries().size(); i++) {
            com.google.gdata.data.Entry entry = resultFeed.getEntries().get(i);
            String entryId = getSynthId(mainEntry,  IOUtil.getFileTail(entry.getId()));
            String title = entry.getTitle().getPlainText();
            Entry newEntry =  new Entry(entryId, this, false);
            StringBuffer desc = new StringBuffer();
            if(entry.getContent()!=null && entry.getContent() instanceof TextContent) {
                desc.append(((TextContent)entry.getContent()).getContent().getPlainText());
            }

            addMetadata(newEntry, entry, desc);
            entries.add(newEntry);
            Resource resource = new Resource();

            Date publishTime =  new Date(entry.getPublished().getValue());
            Date editTime =  new Date(entry.getUpdated().getValue());
            newEntry.initEntry(title, desc.toString(), mainEntry, mainEntry.getUser(),
                            resource, "", publishTime.getTime(),editTime.getTime(),publishTime.getTime(),editTime.getTime(),
                            null);
            getEntryManager().cacheEntry(newEntry);
        }
        return entries;
    }

    /*
    public TypeHandler getTypeHandlerForCopy(Entry entry) throws Exception {
                if(entry.getId().indexOf(TYPE_FOLDER)>=0)  {
            return getRepository().getTypeHandler(TypeHandler.TYPE_GROUP);
        }
        if(!getEntryManager().isSynthEntry(entry.getId())) return this;
        return getRepository().getTypeHandler(TypeHandler.TYPE_FILE);
    }
    */




    public Entry makeSynthEntry(Request request, Entry mainEntry, String id)
        throws Exception {
        List<Entry> entries= getBlogEntries(request, mainEntry, null, id);
        for(Entry entry: entries) {
            if(entry.getId().endsWith(id)) {
                return entry;
            }
        }
        return null;
    }



    public String getIconUrl(Request request, Entry entry) throws Exception {
        String id = entry.getId();
        if(!getEntryManager().isSynthEntry(id)) return super.getIconUrl(request, entry);
        return super.getIconUrl(request, entry);
    }


    public static void main(String[]args) throws Exception {
    }


}