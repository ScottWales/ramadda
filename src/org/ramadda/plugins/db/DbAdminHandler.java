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

package org.ramadda.plugins.db;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.admin.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.repository.type.*;

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
        for (String pluginFile : getRepository().getPluginFiles()) {
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
                    "true", Column.ATTR_TYPE, "string", Column.ATTR_ADDTOFORM,
                    "false"
                });

                Element createDateNode = XmlUtil.create("column", tableNode,
                                             new String[] {
                    "name", DbTypeHandler.COL_DBCREATEDATE,
                    Column.ATTR_ISINDEX, "true", Column.ATTR_TYPE, "datetime",
                    Column.ATTR_ADDTOFORM, "false"
                });

                Element propsNode = XmlUtil.create("column", tableNode,
                                        new String[] {
                    "name", DbTypeHandler.COL_DBPROPS, Column.ATTR_ISINDEX,
                    "false", Column.ATTR_SIZE, "5000", Column.ATTR_TYPE,
                    "string", Column.ATTR_ADDTOFORM, "false"
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
