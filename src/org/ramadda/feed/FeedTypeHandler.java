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

package org.ramadda.feed;


import org.w3c.dom.*;


import ucar.unidata.repository.*;
import ucar.unidata.repository.metadata.*;
import ucar.unidata.repository.type.*;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;

import ucar.unidata.util.StringUtil;

import java.io.File;

import java.net.URL;







import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 */
public class FeedTypeHandler extends GenericTypeHandler {

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public FeedTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
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
        List<String> ids = mainEntry.getChildIds();
        if (ids != null) {
            //Don't cache for now
            //            return ids;
        }
        ids = new ArrayList<String>();
        for (Entry item : getFeedEntries(request, mainEntry)) {
            ids.add(item.getId());
        }
        mainEntry.setChildIds(ids);
        return ids;
    }

    public String getSynthId(Entry parentEntry, String subId) {
        return Repository.ID_PREFIX_SYNTH + parentEntry.getId() + ":" + subId;
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
        if ( !getEntryManager().isSynthEntry(entry.getId())) {
            return getRepository().getTypeHandler(TypeHandler.TYPE_GROUP);
        }
        return getRepository().getTypeHandler(TypeHandler.TYPE_FILE);
    }

    public List<Entry> getFeedEntries(Request request, Entry mainEntry) throws Exception {
        List<Entry> items = new ArrayList<Entry>();
        String url =  mainEntry.getValue(0, (String) null);
        if(url==null || url.trim().length()==0) return items;

        return items;
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
        for (Entry item : getFeedEntries(request, mainEntry)) {
            if(item.getId().equals(id)) return item;
        }
        return null;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isSynthType() {
        return true;
    }


    public String getIconUrl(Request request, Entry entry) throws Exception {
        return iconUrl("/feed/blog_icon.png");
    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public Entry createEntry(String id) {
        return new Entry(id, this, true);
    }


}


