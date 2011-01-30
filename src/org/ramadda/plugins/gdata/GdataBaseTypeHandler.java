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


import com.google.gdata.client.GoogleService;

import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.Category;

import com.google.gdata.data.Person;


import org.w3c.dom.*;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.type.*;

import java.io.File;

import java.net.URL;

import java.util.Hashtable;
import java.util.List;






import java.util.Set;


/**
 * Class TypeHandler _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GdataBaseTypeHandler extends GenericTypeHandler {

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public GdataBaseTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public Entry createEntry(String id) {
        //Make the top level entyr act like a group
        return new Entry(id, this, true);
    }


    /**
     * _more_
     *
     * @param newEntry _more_
     * @param baseEntry _more_
     *
     * @throws Exception _more_
     */
    public void addMetadata(Entry newEntry, BaseEntry baseEntry)
            throws Exception {
        addMetadata(newEntry, baseEntry, null);
    }


    /**
     * _more_
     *
     * @param newEntry _more_
     * @param baseEntry _more_
     * @param desc _more_
     *
     * @throws Exception _more_
     */
    public void addMetadata(Entry newEntry, BaseEntry baseEntry,
                            StringBuffer desc)
            throws Exception {
        if ((baseEntry.getSummary() != null) && (desc != null)) {
            desc.append(baseEntry.getSummary().getPlainText());
        }



        for (Category category : (Set<Category>) baseEntry.getCategories()) {
            if (category.getLabel() == null) {
                continue;
            }
            newEntry.addMetadata(new Metadata(getRepository().getGUID(),
                    newEntry.getId(), "enum_tag", false, category.getLabel(),
                    "", "", "", ""));
        }

        for (Person person : (List<Person>) baseEntry.getAuthors()) {
            newEntry.addMetadata(new Metadata(getRepository().getGUID(),
                    newEntry.getId(), "gdata.author", false,
                    person.getName(), person.getEmail(), "", "", ""));
        }
        for (Person person : (List<Person>) baseEntry.getContributors()) {
            newEntry.addMetadata(new Metadata(getRepository().getGUID(),
                    newEntry.getId(), "gdata.contributor", false,
                    person.getName(), person.getEmail(), "", "", ""));
        }

        if (baseEntry.getRights() != null) {
            String rights = baseEntry.getRights().getPlainText();
            if ((rights != null) && (rights.length() > 0)) {
                newEntry.addMetadata(new Metadata(getRepository().getGUID(),
                        newEntry.getId(), "gdata.rights", false, rights, "",
                        "", "", ""));

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






}

