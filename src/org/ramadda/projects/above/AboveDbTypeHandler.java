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

package org.ramadda.projects.above;


import org.ramadda.plugins.db.*;


import org.ramadda.repository.*;

import org.ramadda.repository.output.*;


import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Site;



import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;



import java.io.File;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;



/**
 *
 */

public class AboveDbTypeHandler extends DbTypeHandler {

    /**
     * _more_
     *
     *
     * @param dbAdmin _more_
     * @param repository _more_
     * @param tableName _more_
     * @param tableNode _more_
     * @param desc _more_
     *
     * @throws Exception _more_
     */
    public AboveDbTypeHandler(DbAdminHandler dbAdmin, Repository repository,
                              String tableName, Element tableNode,
                              String desc)
            throws Exception {
        super(dbAdmin, repository, tableName, tableNode, desc);
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param entryProps _more_
     * @param values _more_
     *
     * @return _more_
     */
    @Override
    public String getIconFor(Entry entry, Hashtable entryProps,
                             Object[] values) {
        Column iconColumn = columnsToUse.get(columnsToUse.size() - 1);
        String value      = iconColumn.getString(values);
        if (value == null) {
            return super.getIconFor(entry, entryProps, values);
        }
        if (value.equals("Fluxtower")) {
            return fileUrl("/above/tower.png");
        } else {
            return fileUrl("/icons/add.png");
        }
    }


}
