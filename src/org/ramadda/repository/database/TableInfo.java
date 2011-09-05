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

package org.ramadda.repository.database;


import ucar.unidata.util.StringUtil;

import java.sql.PreparedStatement;

import java.util.ArrayList;
import java.util.List;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class TableInfo {

    /** _more_ */
    private String name;

    /** _more_ */
    private List<IndexInfo> indices;

    /** _more_ */
    private List<ColumnInfo> columns;

    /** _more_ */
    public PreparedStatement statement;

    /** _more_ */
    public int batchCnt = 0;

    /**
     * _more_
     */
    public TableInfo() {}

    /**
     * _more_
     *
     * @param name _more_
     * @param indices _more_
     * @param columns _more_
     */
    public TableInfo(String name, List<IndexInfo> indices,
                     List<ColumnInfo> columns) {
        this.name    = name;
        this.indices = indices;
        this.columns = columns;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String[] getColumnNames() {
        String[] names = new String[columns.size()];
        int      cnt   = 0;
        for (ColumnInfo columnInfo : columns) {
            names[cnt++] = columnInfo.getName();
        }
        return names;
    }

    /**
     *  Set the Name property.
     *
     *  @param value The new value for Name
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     *  Get the Name property.
     *
     *  @return The Name
     */
    public String getName() {
        return this.name;
    }


    /**
     *  Set the Columns property.
     *
     *  @param value The new value for Columns
     */
    public void setColumns(List<ColumnInfo> value) {
        this.columns = value;
    }

    /**
     *  Get the Columns property.
     *
     *  @return The Columns
     */
    public List<ColumnInfo> getColumns() {
        return this.columns;
    }


    /**
     *  Set the Indices property.
     *
     *  @param value The new value for Indices
     */
    public void setIndices(List<IndexInfo> value) {
        this.indices = value;
    }

    /**
     *  Get the Indices property.
     *
     *  @return The Indices
     */
    public List<IndexInfo> getIndices() {
        return this.indices;
    }


}
