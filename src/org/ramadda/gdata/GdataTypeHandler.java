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

import com.google.gdata.data.Person;
import com.google.gdata.data.Category;


import ucar.unidata.repository.*;
import ucar.unidata.repository.metadata.Metadata;
import ucar.unidata.repository.type.*;






import java.util.Set;
import java.util.Hashtable;
import java.util.List;

import java.io.File;
import java.net.URL;

import com.google.gdata.client.GoogleService;

import com.google.gdata.data.BaseEntry;


/**
 * Class TypeHandler _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GdataTypeHandler extends GenericTypeHandler {

    private Hashtable<String,GoogleService> serviceMap = new Hashtable<String,GoogleService>();

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public GdataTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    protected GoogleService getService(Entry entry) throws Exception {
        String userId = getUserId(entry);
        String password = getPassword(entry);
        if (userId == null || password == null) {
            return null;
        }
        GoogleService service = serviceMap.get(userId);
        if(service!=null) return service;
        service = doMakeService(userId, password);
        if(service==null) return null;
        serviceMap.put(userId, service);
        return service;
    }


    protected GoogleService doMakeService(String userId, String password) throws Exception {
        return null;
    }


    public Entry createEntry(String id) {
        //Make the top level entyr act like a group
        return new Entry(id, this, true);
    }


    public void addMetadata(Entry newEntry, BaseEntry baseEntry) throws Exception {
        addMetadata(newEntry, baseEntry, null);
    }


    public void addMetadata(Entry newEntry, BaseEntry baseEntry, StringBuffer desc) throws Exception {
        if(baseEntry.getSummary()!=null && desc!=null) {
            desc.append(baseEntry.getSummary().getPlainText());
        }



        for(Category category: (Set<Category>)baseEntry.getCategories()) {
            if(category.getLabel()==null)continue;
            newEntry.addMetadata(new Metadata(getRepository().getGUID(), newEntry.getId(),"enum_tag", false,
                                              category.getLabel(),"",
                                              "","",""));
        }

        for(Person person: (List<Person>)baseEntry.getAuthors()) {
            newEntry.addMetadata(new Metadata(getRepository().getGUID(), newEntry.getId(),"gdata.author", false,
                                              person.getName(),
                                              person.getEmail(),
                                              "","",""));
        }
        for(Person person: (List<Person>)baseEntry.getContributors()) {
            newEntry.addMetadata(new Metadata(getRepository().getGUID(), newEntry.getId(),"gdata.contributor", false,
                                              person.getName(),
                                              person.getEmail(),
                                              "","",""));
        }

        if(baseEntry.getRights()!=null) {
            String rights = baseEntry.getRights().getPlainText();
            if(rights!=null &&rights.length()>0) {
                newEntry.addMetadata(new Metadata(getRepository().getGUID(), newEntry.getId(),"gdata.rights", false,
                                                  rights, "",
                                                  "","",""));
                
            }
        }
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isSynthType() {
        return true;
    }



    public String getUserId(Entry entry) {
        return entry.getValue(0,(String)null);
    }

    public String getPassword(Entry entry) {
        return entry.getValue(1,(String)null);
    }

    public String getSynthId(Entry parentEntry, String type, String subId) {
        return Repository.ID_PREFIX_SYNTH + parentEntry.getId() + ":" + type +":" +subId;
    }



}