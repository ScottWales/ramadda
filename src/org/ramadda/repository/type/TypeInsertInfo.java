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

package org.ramadda.repository.type;



import org.ramadda.repository.*;
import org.ramadda.repository.database.*;

import java.sql.PreparedStatement;
import java.sql.Statement;



/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class TypeInsertInfo {

    /** _more_ */
    private String sql;

    /** _more_ */
    private PreparedStatement statement;

    /** _more_ */
    private TypeHandler typeHandler;

    /**
     * _more_
     *
     * @param typeHandler _more_
     * @param sql _more_
     */
    public TypeInsertInfo(TypeHandler typeHandler, String sql) {
        this.sql         = sql;
        this.typeHandler = typeHandler;
    }


    /**
     * Set the Sql property.
     *
     * @param value The new value for Sql
     */
    public void setSql(String value) {
        sql = value;
    }

    /**
     * Get the Sql property.
     *
     * @return The Sql
     */
    public String getSql() {
        return sql;
    }

    /**
     * Set the Statement property.
     *
     * @param value The new value for Statement
     */
    public void setStatement(PreparedStatement value) {
        statement = value;
    }

    /**
     * Get the Statement property.
     *
     * @return The Statement
     */
    public PreparedStatement getStatement() {
        return statement;
    }

    /**
     * Set the TypeHandler property.
     *
     * @param value The new value for TypeHandler
     */
    public void setTypeHandler(TypeHandler value) {
        typeHandler = value;
    }

    /**
     * Get the TypeHandler property.
     *
     * @return The TypeHandler
     */
    public TypeHandler getTypeHandler() {
        return typeHandler;
    }




}
