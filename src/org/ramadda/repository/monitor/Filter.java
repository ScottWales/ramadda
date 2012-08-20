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

package org.ramadda.repository.monitor;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.30 $
 */
public class Filter implements Constants {

    /** _more_ */
    public static final String[] FIELD_TYPES = {
        ARG_TEXT, ARG_TYPE, ARG_USER, ARG_FILESUFFIX, ARG_ANCESTOR, ARG_AREA
    };


    /** _more_ */
    private String field;

    /** _more_ */
    private Object value;

    /** _more_ */
    private boolean doNot = false;

    /** _more_ */
    private Hashtable properties = new Hashtable();


    /**
     * _more_
     */
    public Filter() {}

    /**
     * _more_
     *
     * @param field _more_
     * @param value _more_
     */
    public Filter(String field, Object value) {
        this(field, value, false);
    }

    /**
     * _more_
     *
     * @param field _more_
     * @param value _more_
     * @param doNot _more_
     */
    public Filter(String field, Object value, boolean doNot) {
        this.field = field;
        this.value = value;
        this.doNot = doNot;
    }


    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public Object getProperty(Object key) {
        return properties.get(key);
    }

    /**
     * _more_
     *
     * @param key _more_
     * @param value _more_
     */
    public void putProperty(Object key, Object value) {
        properties.put(key, value);
    }

    /**
     * _more_
     */
    public void clearProperties() {
        properties = new Hashtable();
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return field + "=" + value;
    }


    /**
     * Set the Field property.
     *
     * @param value The new value for Field
     */
    public void setField(String value) {
        field = value;
    }

    /**
     * Get the Field property.
     *
     * @return The Field
     */
    public String getField() {
        return field;
    }

    /**
     * Set the Value property.
     *
     * @param value The new value for Value
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Get the Value property.
     *
     * @return The Value
     */
    public Object getValue() {
        return value;
    }


    /**
     * Set the DoNot property.
     *
     * @param value The new value for DoNot
     */
    public void setDoNot(boolean value) {
        doNot = value;
    }

    /**
     * Get the DoNot property.
     *
     * @return The DoNot
     */
    public boolean getDoNot() {
        return doNot;
    }



}
