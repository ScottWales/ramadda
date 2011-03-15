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

package org.ramadda.plugins.gdata;

import com.google.gdata.client.*;
import com.google.gdata.client.*;
import com.google.gdata.client.calendar.*;

import com.google.gdata.client.docs.*;
import com.google.gdata.client.photos.*;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.acl.*;
import com.google.gdata.data.docs.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.data.media.*;
import com.google.gdata.data.photos.*;
import com.google.gdata.util.*;
import com.google.gdata.util.*;

import ucar.unidata.util.IOUtil;

import java.net.URL;

import java.util.Hashtable;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Wed, Jan 26, '11
 * @author         Enter your name here...    
 */
public class DocsTest {


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: DocsTest <userid> <password>");
            return;
        }
        DocsService client = new DocsService("ramadda");
        client.setUserCredentials(args[0], args[1]);

        String[] urls = {
            //            "https://docs.google.com/feeds/default/private/full/folder%3Aroot/contents?showfolders=true",
            "https://docs.google.com/feeds/default/private/full?showfolders=true"
        };
        for (String url : urls) {
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
                tempFeed = client.getFeed(new URL(link.getHref()),
                                          DocumentListFeed.class);
            } while (tempFeed.getEntries().size() > 0);

            System.out.println("query url:" + url);
            System.out.println("Found " + allEntries.getEntries().size()
                               + " entries");
            Hashtable<String, DocumentListEntry> map =
                new Hashtable<String, DocumentListEntry>();
            for (DocumentListEntry entry : allEntries.getEntries()) {
                String id = IOUtil.getFileTail(entry.getId());
                map.put(id, entry);
            }
            for (DocumentListEntry entry : allEntries.getEntries()) {
                String id = IOUtil.getFileTail(entry.getId());
                java.util.List<com.google.gdata.data.Link> links =
                    entry.getParentLinks();
                if (links.size() == 0) {
                    System.out.println("Top level:"
                                       + entry.getTitle().getPlainText());
                } else {
                    String parentId =
                        IOUtil.getFileTail(links.get(0).getHref());
                    DocumentListEntry parent = map.get(parentId);
                    System.out.println(entry.getTitle().getPlainText()
                                       + " parent:"
                                       + parent.getTitle().getPlainText());
                }
            }
        }
    }


}

