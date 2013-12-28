/*
* Copyright 2008-2013 Geode Systems LLC
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

package org.ramadda.plugins.metameta;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;

import org.w3c.dom.*;

import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.List;


/**
 * This represents some collection of metameta field entries
 *
 * @author RAMADDA Development Team
 */
public class MetametaCollectionTypeHandler extends MetametaGroupTypeHandler {

    /**
     * ctor
     *
     * @param repository repo
     * @param entryNode from types.xml
     *
     * @throws Exception on badness
     */
    public MetametaCollectionTypeHandler(Repository repository,
                                         Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getChildType() {
        return MetametaDefinitionTypeHandler.TYPE_METAMETA_DEFINITION;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param parent _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception on badness
     */
    @Override
    public Result getHtmlDisplay(Request request, Entry parent,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        if ( !getEntryManager().canAddTo(request, parent)) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        subGroups.addAll(entries);
        addListForm(request, parent, subGroups, sb);

        return getEntryManager().addEntryHeader(request, parent,
                new Result("Metameta Collection", sb));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param xml _more_
     * @param parent _more_
     * @param children _more_
     *
     * @throws Exception on badness
     */
    public void generateDbXml(Request request, StringBuffer xml,
                              Entry parent, List<Entry> children)
            throws Exception {
        xml.append(XmlUtil.openTag("tables", ""));
        for (Entry defEntry : children) {
            MetametaDefinitionTypeHandler defTypeHandler =
                (MetametaDefinitionTypeHandler) defEntry.getTypeHandler();
            List<Entry> fields = getEntryManager().getChildrenAll(request,
                                     defEntry);
            defTypeHandler.generateDbXml(request, xml, defEntry, fields);
        }
        xml.append(XmlUtil.closeTag("tables"));
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param xml _more_
     * @param parent _more_
     * @param children _more_
     *
     * @throws Exception on badness
     */
    public void generateEntryXml(Request request, StringBuffer xml,
                                 Entry parent, List<Entry> children)
            throws Exception {
        xml.append(XmlUtil.openTag(TAG_ENTRIES, ""));
        for (Entry defEntry : children) {
            MetametaDefinitionTypeHandler defTypeHandler =
                (MetametaDefinitionTypeHandler) defEntry.getTypeHandler();
            List<Entry> fields = getEntryManager().getChildrenAll(request,
                                     defEntry);
            defTypeHandler.generateEntryXml(request, xml, defEntry, fields);
        }
        xml.append(XmlUtil.closeTag(TAG_ENTRIES));
    }






}
