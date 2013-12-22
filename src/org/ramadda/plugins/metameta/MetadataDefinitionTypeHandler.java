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

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.WikiUtil;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;


/**
 * Class TypeHandler _more_
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class MetadataDefinitionTypeHandler extends ExtensibleGroupTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public MetadataDefinitionTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public boolean canBeCreatedBy(Request request) {
        return request.getUser().getAdmin();
    }


    @Override
    public List<Entry> postProcessEntries(Request request, List<Entry> entries) {
        List<Entry> sorted = sortEntries(entries, false);
        System.err.println ("sorted:" + sorted);
        return  sorted;
    }

    private List<Entry> sortEntries(List<Entry> entries, final boolean descending) {
        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Entry e1     = (Entry) o1;
                Entry e2     = (Entry) o2;
                int result;
                if(e1.isType(MetadataColumnTypeHandler.TYPE_METADATA_COLUMN) && e2.isType(MetadataColumnTypeHandler.TYPE_METADATA_COLUMN)) {
                    Integer i1 = (Integer) e1.getValue(0);
                    Integer i2 = (Integer) e2.getValue(0);
                    result =  i1.compareTo(i2);
                } else {
                    result = e1.getName().compareToIgnoreCase(e2.getName());
                }
                if (descending) {
                    if (result >= 1) {
                        return -1;
                    } else if (result <= -1) {
                        return 1;
                    }

                    return 0;
                }

                return result;
            }
            public boolean equals(Object obj) {
                return obj == this;
            }
        };
        Object[] array = entries.toArray();
        Arrays.sort(array, comp);
        return (List<Entry>) Misc.toList(array);
    }


@Override
    public String getWikiInclude(WikiUtil wikiUtil, Request request,
                                 Entry originalEntry, Entry entry,
                                 String tag, Hashtable props) throws Exception {

        if(tag.equals("metadata.bulkform")) {
            if(!getEntryManager().canAddTo(request, entry)) return null;
            //TODO:
            return null;
            //            return "XXX";
        }
        return super.getWikiInclude(wikiUtil, request,
                                    originalEntry, entry,
                                    tag, props);
    }



}
