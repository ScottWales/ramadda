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

package org.ramadda.data.services;


import org.ramadda.data.record.RecordField;
import org.ramadda.data.services.*;


import org.ramadda.repository.*;
import org.ramadda.repository.map.MapInfo;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.AtomUtil;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.xml.XmlUtil;


import java.util.Date;
import java.util.HashSet;
import java.util.List;


/**
 *
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */
public class PointCollectionTypeHandler extends RecordCollectionTypeHandler {


    /**
     * ctor
     *
     * @param repository the repository
     * @param node the xml node from the types.xml file
     * @throws Exception On badness
     */
    public PointCollectionTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public RecordOutputHandler getRecordOutputHandler() {
        return (RecordOutputHandler) getRepository().getOutputHandler(
            org.ramadda.data.services.PointOutputHandler.class);
    }


}
