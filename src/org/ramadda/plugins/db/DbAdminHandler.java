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

package org.ramadda.plugins.db;


import org.ramadda.repository.*;
import org.ramadda.repository.admin.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.Misc;
import ucar.unidata.xml.XmlUtil;

import java.lang.reflect.*;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */

public class DbAdminHandler extends AdminHandlerImpl {

    /** _more_ */
    public static final String TAG_TABLES = "tables";

    /** _more_ */
    public static final String TAG_TABLE = "table";

    /** _more_ */
    public static final String TAG_COLUMN = "column";

    /** _more_ */
    public static final String TAG_PROPERTY = "property";

    /** _more_ */
    public static final String ATTR_HANDLER = "handler";

    /** _more_ */
    public static final String ATTR_ICON = "icon";

    /** _more_ */
    public static final String ATTR_ID = "id";

    /** _more_ */
    public static final String ATTR_NAME = "name";

    /** _more_ */
    public static final String ATTR_CANLIST = "canlist";

    /** _more_ */
    public static final String ATTR_LABEL = "label";

    /** _more_ */
    public static final String ATTR_TYPE = "type";

    /** _more_ */
    public static final String ATTR_CANSEARCH = "cansearch";

    /** _more_ */
    public static final String ATTR_VALUES = "values";

    /** _more_ */
    public static final String ATTR_VALUE = "value";

    /** _more_ */
    public static final String ATTR_ROWS = "rows";

    /** _more_ */
    public static final String ATTR_SIZE = "size";



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
        //        System.err.println("DbAdminHandler.init");
        for (String pluginFile :
                getRepository().getPluginManager().getPluginFiles()) {
            if ( !pluginFile.endsWith("db.xml")) {
                continue;
            }
            //            System.err.println("DbAdminHandler.init - plugin file:" + pluginFile);
            Element root     = XmlUtil.getRoot(pluginFile, getClass());
            List    children = XmlUtil.findChildren(root, TAG_TABLE);
            for (int i = 0; i < children.size(); i++) {
                Element tableNode = (Element) children.get(i);
                String  tableId   = XmlUtil.getAttribute(tableNode, ATTR_ID);
                Class handlerClass =
                    Misc.findClass(XmlUtil.getAttribute(tableNode,
                        ATTR_HANDLER,
                        "org.ramadda.plugins.db.DbTypeHandler"));
                Constructor ctor = Utils.findConstructor(handlerClass,
                                       new Class[] { this.getClass(),
                        Repository.class, String.class, tableNode.getClass(),
                        String.class });
                if (ctor == null) {
                    System.err.println("failed to get ctor:"
                                       + handlerClass.getName() + " "
                                       + XmlUtil.toString(tableNode));

                    throw new IllegalArgumentException(
                        "DbAdminHandler: could not find constructor");
                }

                DbTypeHandler typeHandler =
                    (DbTypeHandler) ctor.newInstance(new Object[] { this,
                        getRepository(), tableId, tableNode,
                        XmlUtil.getAttribute(tableNode, ATTR_NAME) });

                List<Element> columnNodes =
                    (List<Element>) XmlUtil.findChildren(tableNode,
                        TAG_COLUMN);
                Element idNode = XmlUtil.create(TAG_COLUMN, tableNode,
                                     new String[] {
                    "name", DbTypeHandler.COL_DBID, Column.ATTR_ISINDEX,
                    "true", Column.ATTR_TYPE, "string", Column.ATTR_ADDTOFORM,
                    "false"
                });
                Element userNode = XmlUtil.create(TAG_COLUMN, tableNode,
                                       new String[] {
                    "name", DbTypeHandler.COL_DBUSER, Column.ATTR_ISINDEX,
                    "true", Column.ATTR_TYPE, "string", Column.ATTR_ADDTOFORM,
                    "false", Column.ATTR_CANLIST, "false"
                });

                Element createDateNode = XmlUtil.create(TAG_COLUMN,
                                             tableNode, new String[] {
                    "name", DbTypeHandler.COL_DBCREATEDATE,
                    Column.ATTR_ISINDEX, "true", Column.ATTR_TYPE, "datetime",
                    Column.ATTR_ADDTOFORM, "false", Column.ATTR_CANLIST,
                    "false"
                });

                Element propsNode = XmlUtil.create(TAG_COLUMN, tableNode,
                                        new String[] {
                    "name", DbTypeHandler.COL_DBPROPS, Column.ATTR_ISINDEX,
                    "false", Column.ATTR_SIZE, "5000", Column.ATTR_TYPE,
                    "string", Column.ATTR_ADDTOFORM, "false",
                    Column.ATTR_CANLIST, "false"
                });


                columnNodes.add(0, propsNode);
                columnNodes.add(0, createDateNode);
                columnNodes.add(0, userNode);
                columnNodes.add(0, idNode);
                //                System.err.println ("DbAdminHandler: addingTypeHandler");
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
