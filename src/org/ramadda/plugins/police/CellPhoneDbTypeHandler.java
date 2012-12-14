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

package org.ramadda.plugins.police;


import org.ramadda.plugins.db.*;



import org.ramadda.repository.*;


import org.w3c.dom.*;

import org.ramadda.repository.type.*;
import ucar.unidata.util.StringUtil;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */

public class CellPhoneDbTypeHandler extends DbTypeHandler {

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
    public CellPhoneDbTypeHandler(DbAdminHandler dbAdmin,
                                  Repository repository, String tableName,
                                  Element tableNode, String desc)
            throws Exception {
        super(dbAdmin, repository, tableName, tableNode, desc);
    }


    public Result handleBulkUpload(Request request, Entry entry, String bulk)
            throws Exception {
        System.err.println("bulk");
        List<Object[]> valueList = new ArrayList<Object[]>();
        for (String line : StringUtil.split(bulk, "\n", true, true)) {
            if (line.startsWith("#")) {
                continue;
            }
            List<String> toks   = StringUtil.split(line, ",", false, false);
            Object[]     values = tableHandler.makeEntryValueArray();
            initializeValueArray(request, null, values);
            if (toks.size() != columnsToUse.size()) {
                throw new IllegalArgumentException(
                    "Wrong number of values. Given line has: " + toks.size()
                    + " Expected:" + columnsToUse.size() + "<br>" + line);
            }
            for (int colIdx = 0; colIdx < toks.size(); colIdx++) {
                Column column = columnsToUse.get(colIdx);
                String value  = toks.get(colIdx).trim();
                value = value.replaceAll("_COMMA_", ",");
                value = value.replaceAll("_NEWLINE_", "\n");
                column.setValue(entry, values, value);
            }
            valueList.add(values);
        }
        for (Object[] tuple : valueList) {
            doStore(entry, tuple, true);
        }
        //Remove these so any links that get made with the request don't point to the BULK upload
        request.remove(ARG_DB_NEWFORM);
        request.remove(ARG_DB_BULK_TEXT);
        request.remove(ARG_DB_BULK_FILE);

        return handleListTable(request, entry, valueList, false, false);

    }

}
