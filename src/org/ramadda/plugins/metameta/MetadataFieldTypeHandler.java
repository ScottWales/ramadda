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


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class MetadataFieldTypeHandler extends ExtensibleGroupTypeHandler {


    /** _more_ */
    public static final String TYPE_METADATA_FIELD = "type_metadata_field";



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public MetadataFieldTypeHandler(Repository repository, Element entryNode)
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


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parent _more_
     * @param newEntry _more_
     *
     * @throws Exception _more_
     */
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        super.initializeEntryFromForm(request, entry, parent, newEntry);
        setSortOrder(request, entry, parent);
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
    public Hashtable getProperties(Entry entry) throws Exception {
        String s = (String) getEntryValue(entry, 4);
        if (s == null) {
            s = "";
        }
        Properties props = new Properties();
        props.load(new ByteArrayInputStream(s.getBytes()));
        Hashtable table = new Hashtable();
        table.putAll(props);

        return table;
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parent _more_
     *
     * @throws Exception _more_
     */
    public void setSortOrder(Request request, Entry entry, Entry parent)
            throws Exception {
        System.err.println("VALUES:" + getEntryValue(entry, 1));
        Integer index = (Integer) getEntryValue(entry, 0);
        int     idx   = ((index == null)
                         ? -1
                         : index.intValue());
        if (idx < 0) {
            int maxIndex = -1;
            List<Entry> siblings = getEntryManager().getChildrenAll(request,
                                       parent);
            for (Entry sibling : siblings) {
                if (sibling.isType(TYPE_METADATA_FIELD)) {
                    int siblingIndex = ((Integer) getEntryValue(sibling,
                                           0)).intValue();
                    maxIndex = Math.max(maxIndex, siblingIndex);
                }
            }
            setEntryValue(entry, 0, new Integer(maxIndex + 1));
        }

    }


}
