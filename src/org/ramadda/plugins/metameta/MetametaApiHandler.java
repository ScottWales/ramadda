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
import org.ramadda.util.Utils;

import org.w3c.dom.Element;

import ucar.unidata.util.StringUtil;

import ucar.unidata.xml.XmlUtil;

import java.util.Hashtable;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Sat, Dec 28, '13
 * @author         Enter your name here...
 */
public class MetametaApiHandler extends RepositoryManager implements RequestHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @param props _more_
     *
     * @throws Exception _more_
     */
    public MetametaApiHandler(Repository repository, Element node,
                              Hashtable props)
            throws Exception {
        super(repository);
    }


    /**
     *  This will be the entry point for querying on the types
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processTypeRequest(Request request) throws Exception {
        List<String> toks = StringUtil.split(request.getRequestPath(), "/",
                                             true, true);
        String       type        = toks.get(toks.size() - 1);
        TypeHandler  typeHandler = getRepository().getTypeHandler(type);
        StringBuffer xml = new StringBuffer(XmlUtil.openTag(TAG_ENTRIES));
        xml.append("\n");
        StringBuffer inner = new StringBuffer();
        inner.append(
            XmlUtil.tag(
                "properties", "",
                XmlUtil.getCdata(
                    Utils.makeProperties(typeHandler.getProperties()))));
        xml.append(
            XmlUtil.tag(
                TAG_ENTRY,
                XmlUtil.attrs(
                    ATTR_NAME, typeHandler.getLabel(), ATTR_TYPE,
                    "type_metadata_definition", ATTR_ID,
                    "definition"), inner.toString()));

        List<Column> columns = typeHandler.getColumns();
        if (columns != null) {
            for (Column column : columns) {
                inner = new StringBuffer();
                StringBuffer attrs = new StringBuffer();
                attrs.append(XmlUtil.attrs(ATTR_NAME, column.getLabel(),
                                           ATTR_TYPE, "type_metadata_field",
                                           ATTR_PARENT, "definition"));
                TypeHandler.addPropertyTags(column.getProperties(), inner);
                xml.append(XmlUtil.tag(TAG_ENTRY, attrs.toString(),
                                       inner.toString()));

            }
        }

        xml.append(XmlUtil.closeTag(TAG_ENTRIES));

        return new Result("Type", xml, "text/xml");
    }

}
