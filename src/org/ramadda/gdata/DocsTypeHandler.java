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






import java.io.File;
import java.net.URL;

import com.google.gdata.client.*;
import com.google.gdata.client.photos.*;
import com.google.gdata.data.*;
import com.google.gdata.data.media.*;
import com.google.gdata.data.photos.*;
import com.google.gdata.client.*;
import com.google.gdata.client.calendar.*;
import com.google.gdata.data.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;
import java.net.URL;

import com.google.gdata.client.*;
import com.google.gdata.client.docs.*;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.acl.*;
import com.google.gdata.data.docs.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;


public class DocsTypeHandler {
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