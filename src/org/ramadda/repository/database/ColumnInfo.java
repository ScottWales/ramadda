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


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class ColumnInfo {

    /** _more_ */
    public static final int TYPE_TIMESTAMP = 1;

    /** _more_ */
    public static final int TYPE_VARCHAR = 2;

    /** _more_ */
    public static final int TYPE_INTEGER = 3;

    /** _more_ */
    public static final int TYPE_DOUBLE = 4;

    /** _more_ */
    public static final int TYPE_CLOB = 5;


    /** _more_ */
    public static final int TYPE_BIGINT = 6;

    /** _more_ */
    private String name;

    /** _more_ */
    private String typeName;

    /** _more_ */
    private int type;

    /** _more_ */
    private int size;

    /**
     * _more_
     */
    public ColumnInfo() {}

    /**
     * _more_
     *
     * @param name _more_
     * @param typeName _more_
     * @param type _more_
     * @param size _more_
     */
    public ColumnInfo(String name, String typeName, int type, int size) {
        this.name     = name;
        this.typeName = typeName;
        this.type     = convertType(type);
        this.size     = size;
    }

    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public static int convertType(int type) {
        if (type == java.sql.Types.TIMESTAMP) {
            return TYPE_TIMESTAMP;
        } else if (type == java.sql.Types.VARCHAR) {
            return TYPE_VARCHAR;
        } else if (type == java.sql.Types.INTEGER) {
            return TYPE_INTEGER;
        } else if (type == java.sql.Types.DOUBLE) {
            return TYPE_DOUBLE;
        } else if (type == java.sql.Types.CLOB) {
            return TYPE_CLOB;
        } else if (type == java.sql.Types.BIGINT) {
            return TYPE_BIGINT;
        } else {
            throw new IllegalArgumentException("Unknown sqltype:" + type);
        }
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
     *  Set the TypeName property.
     *
     *  @param value The new value for TypeName
     */
    public void setTypeName(String value) {
        this.typeName = value;
    }

    /**
     *  Get the TypeName property.
     *
     *  @return The TypeName
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     *  Set the Type property.
     *
     *  @param value The new value for Type
     */
    public void setType(int value) {
        this.type = value;
    }

    /**
     *  Get the Type property.
     *
     *  @return The Type
     */
    public int getType() {
        return this.type;
    }

    /**
     *  Set the Size property.
     *
     *  @param value The new value for Size
     */
    public void setSize(int value) {
        this.size = value;
    }

    /**
     *  Get the Size property.
     *
     *  @return The Size
     */
    public int getSize() {
        return this.size;
    }



}
