/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.plugins.db;


import org.ramadda.repository.*;
import org.ramadda.repository.admin.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.xml.XmlUtil;



import java.util.ArrayList;
import java.util.List;


/**
 *
 */

public class DbAdminHandler extends AdminHandlerImpl {

    /**
     * _more_
     *
     *
     *
     * @param repository _more_
     * @throws Exception _more_
     */
    public DbAdminHandler(Repository repository) throws Exception {
        super(repository);
        init();
    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void init() throws Exception {
        for (String pluginFile :
                getRepository().getPluginManager().getPluginFiles()) {
            if ( !pluginFile.endsWith("db.xml")) {
                continue;
            }

            Element root     = XmlUtil.getRoot(pluginFile, getClass());
            List    children = XmlUtil.findChildren(root, "table");
            for (int i = 0; i < children.size(); i++) {
                Element tableNode = (Element) children.get(i);
                String  tableId = XmlUtil.getAttribute(tableNode, "id");
                DbTypeHandler typeHandler =
                    new DbTypeHandler(this, getRepository(), tableId,
                                      tableNode,
                                      XmlUtil.getAttribute(tableNode,
                                          "name"));


                List<Element> columnNodes =
                    (List<Element>) XmlUtil.findChildren(tableNode, "column");
                Element idNode = XmlUtil.create("column", tableNode,
                                     new String[] {
                    "name", DbTypeHandler.COL_DBID, Column.ATTR_ISINDEX,
                    "true", Column.ATTR_TYPE, "string", Column.ATTR_ADDTOFORM,
                    "false"
                });
                Element userNode = XmlUtil.create("column", tableNode,
                                       new String[] {
                    "name", DbTypeHandler.COL_DBUSER, Column.ATTR_ISINDEX,
                    "true", Column.ATTR_TYPE, "string", 
                    Column.ATTR_ADDTOFORM,               "false",
                    Column.ATTR_CANLIST,               "false"
                });

                Element createDateNode = XmlUtil.create("column", tableNode,
                                             new String[] {
                    "name", DbTypeHandler.COL_DBCREATEDATE,
                    Column.ATTR_ISINDEX, "true", Column.ATTR_TYPE, "datetime",
                    Column.ATTR_ADDTOFORM, "false",
                    Column.ATTR_CANLIST,               "false"
                });

                Element propsNode = XmlUtil.create("column", tableNode,
                                        new String[] {
                    "name", DbTypeHandler.COL_DBPROPS, Column.ATTR_ISINDEX,
                    "false", Column.ATTR_SIZE, "5000", Column.ATTR_TYPE,
                    "string", Column.ATTR_ADDTOFORM, "false",
                    Column.ATTR_CANLIST,               "false"
                });


                columnNodes.add(0, propsNode);
                columnNodes.add(0, createDateNode);
                columnNodes.add(0, userNode);
                columnNodes.add(0, idNode);
                getRepository().addTypeHandler(tableId, typeHandler);
                typeHandler.init(columnNodes);
            }
        }
    }


    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public String getTableName(String type) {
        return "db_" + type;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getId() {
        return "dbadmin";
    }

}
