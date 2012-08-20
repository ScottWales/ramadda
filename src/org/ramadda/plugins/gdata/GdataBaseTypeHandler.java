/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
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


import com.google.gdata.client.GoogleService;

import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.Category;

import com.google.gdata.data.Person;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;

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
