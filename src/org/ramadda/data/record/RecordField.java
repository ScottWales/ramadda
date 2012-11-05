/*
 * Copyright 2010 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
 * http://www.unavco.org
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

package org.ramadda.data.record;


import java.io.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import java.text.SimpleDateFormat;


/**
 * Holds information about the record's parameters
 *
 *
 * @author Jeff McWhirter
 */
public class RecordField {


    /** _more_ */
    public static final String PROP_CHARTABLE = "chartable";

    /** _more_ */
    public static final String PROP_SEARCHABLE = "searchable";

    /** _more_ */
    public static final String PROP_SEARCH_SUFFIX = "search.suffix";

    /** _more_          */
    public static final String PROP_BITFIELDS = "bitfields";


    public static final String TYPE_NUMERIC = "numeric";
    public static final String TYPE_STRING = "string";
    public static final String TYPE_DATE = "date";

    private boolean isTypeNumeric = true;
    private boolean isTypeString = false;
    private boolean isTypeDate = false;
    private SimpleDateFormat dateFormat;

    /** _more_ */
    private String name;

    /** _more_ */
    private String label;

    /** _more_ */
    private String description;

    /** _more_ */
    private Hashtable properties = new Hashtable();

    /** _more_ */
    private int paramId;

    /** _more_ */
    private String unit;

    /** _more_ */
    private List<String[]> enumeratedValues;

    /** _more_ */
    private String rawType;

    /** _more_ */
    private String typeName;

    /** _more_ */
    private int arity = 1;

    /** _more_ */
    private ValueGetter valueGetter;

    /** _more_          */
    private boolean skip = false;

    /** _more_          */
    private boolean synthetic = false;

    private double defaultDoubleValue = Double.NaN;

    private String type = TYPE_NUMERIC;


    /**
     * _more_
     *
     * @param name _more_
     * @param label _more_
     * @param description _more_
     * @param paramId _more_
     * @param unit _more_
     */
    public RecordField(String name, String label, String description,
                       int paramId, String unit) {
        this.name        = name;
        this.label       = label;
        this.description = description;
        this.paramId     = paramId;
        this.unit        = unit;
        /*      this.rawType = rawType;
        this.typeName = typeName;
        this.arity = arity;
        this.searchable = searchable;
        this.chartable  = chartable;
        */
    }



    /**
     * _more_
     *
     * @param name _more_
     * @param label _more_
     * @param description _more_
     * @param paramId _more_
     * @param unit _more_
     * @param rawType _more_
     * @param typeName _more_
     * @param arity _more_
     * @param searchable _more_
     * @param chartable _more_
     */
    public RecordField(String name, String label, String description,
                       int paramId, String unit, String rawType,
                       String typeName, int arity, boolean searchable,
                       boolean chartable) {
        this.name        = name;
        this.label       = label;
        this.description = description;
        this.paramId     = paramId;
        this.unit        = unit;
        this.rawType     = rawType;
        this.typeName    = typeName;
        this.arity       = arity;
        if (searchable) {
            properties.put(PROP_SEARCHABLE, "true");
        }
        if (chartable) {
            properties.put(PROP_CHARTABLE, "true");
        }
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return name + " " + paramId;
    }


    /**
     *  Set the ValueGetter property.
     *
     *  @param value The new value for ValueGetter
     */
    public void setValueGetter(ValueGetter value) {
        valueGetter = value;
    }

    /**
     *  Get the ValueGetter property.
     *
     *  @return The ValueGetter
     */
    public ValueGetter getValueGetter() {
        return valueGetter;
    }



    /**
     * _more_
     *
     *
     * @param visitInfo _more_
     * @param pw _more_
     */
    public void printCsvHeader(VisitInfo visitInfo, PrintWriter pw) {
        pw.print(getName());
        pw.print("[");
        if ((unit != null) && (unit.length() > 0)) {
            pw.print("unit=\"");
            pw.print(unit);
            pw.print("\"");
        }
        if (arity > 1) {
            pw.print("size=\"");
            pw.print(arity);
            pw.print("\"");
        }
        pw.print("]");

    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<String[]> getEnumeratedValues() {
        return enumeratedValues;
    }


    /**
     * _more_
     *
     * @param enums _more_
     */
    public void setEnumeratedValues(List<String[]> enums) {
        this.enumeratedValues = enums;
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
     *  Set the Label property.
     *
     *  @param value The new value for Label
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     *  Get the Label property.
     *
     *  @return The Label
     */
    public String getLabel() {
        return this.label;
    }


    /**
     *  Set the Description property.
     *
     *  @param value The new value for Description
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     *  Get the Description property.
     *
     *  @return The Description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     *  Set the Unit property.
     *
     *  @param value The new value for Unit
     */
    public void setUnit(String value) {
        this.unit = value;
    }

    /**
     *  Get the Unit property.
     *
     *  @return The Unit
     */
    public String getUnit() {
        return this.unit;
    }



    /**
     *  Set the ParamId property.
     *
     *  @param value The new value for ParamId
     */
    public void setParamId(int value) {
        this.paramId = value;
    }

    /**
     *  Get the ParamId property.
     *
     *  @return The ParamId
     */
    public int getParamId() {
        return this.paramId;
    }

    /**
     *  Set the Searchable property.
     *
     *  @param value The new value for Searchable
     */
    public void setSearchable(boolean value) {
        properties.put(PROP_SEARCHABLE, value + "");
    }

    /**
     *  Get the Searchable property.
     *
     *  @return The Searchable
     */
    public boolean getSearchable() {
        String v = (String) properties.get(PROP_SEARCHABLE);
        if ((v == null) || !v.equals("true")) {
            return false;
        }
        return true;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isBitField() {
        return properties.get(PROP_BITFIELDS) != null;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String[] getBitFields() {
        String s = (String) properties.get(PROP_BITFIELDS);
        if (s == null) {
            return null;
        }
        return s.split(",");
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getSearchSuffix() {
        return (String) properties.get(PROP_SEARCH_SUFFIX);
    }

    /**
     *  Set the Chartable property.
     *
     *  @param value The new value for Chartable
     */
    public void setChartable(boolean value) {
        properties.put(PROP_CHARTABLE, value + "");
    }

    /**
     * _more_
     *
     * @param key _more_
     * @param value _more_
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getChartable() {
        String v = (String) properties.get(PROP_CHARTABLE);
        if ((v == null) || !v.equals("true")) {
            return false;
        }
        return true;
    }



    /**
     * Set the RawType property.
     *
     * @param value The new value for RawType
     */
    public void setRawType(String value) {
        rawType = value;
    }

    /**
     * Get the RawType property.
     *
     * @return The RawType
     */
    public String getRawType() {
        return rawType;
    }



    /**
     * Set the TypeName property.
     *
     * @param value The new value for TypeName
     */
    public void setTypeName(String value) {
        typeName = value;
    }

    /**
     * Get the TypeName property.
     *
     * @return The TypeName
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Set the Arity property.
     *
     * @param value The new value for Arity
     */
    public void setArity(int value) {
        arity = value;
    }

    /**
     * Get the Arity property.
     *
     * @return The Arity
     */
    public int getArity() {
        return arity;
    }

    /**
     *  Set the Skip property.
     *
     *  @param value The new value for Skip
     */
    public void setSkip(boolean value) {
        skip = value;
    }

    /**
     *  Get the Skip property.
     *
     *  @return The Skip
     */
    public boolean getSkip() {
        return skip;
    }

    /**
     *  Set the Synthetic property.
     *
     *  @param value The new value for Synthetic
     */
    public void setSynthetic(boolean value) {
        synthetic = value;
    }

    /**
     *  Get the Synthetic property.
     *
     *  @return The Synthetic
     */
    public boolean getSynthetic() {
        return synthetic;
    }

    public boolean hasDefaultDoubleValue() {
        return !Double.isNaN(defaultDoubleValue);
    }


    /**
       Set the DefaultValue property.

       @param value The new value for DefaultValue
    **/
    public void setDefaultDoubleValue (double value) {
	defaultDoubleValue = value;
    }

    /**
       Get the DefaultValue property.

       @return The DefaultValue
    **/
    public double getDefaultDoubleValue () {
	return defaultDoubleValue;
    }



    /**
       Set the Type property.

       @param value The new value for Type
    **/
    public void setType (String value) {
	type = value;
        isTypeNumeric = value.equals(TYPE_NUMERIC);
        isTypeString = value.equals(TYPE_STRING);
        isTypeDate = value.equals(TYPE_DATE);
    }

    /**
       Get the Type property.

       @return The Type
    **/
    public String getType () {
	return type;
    }

    public boolean isTypeString() {
        return isTypeString;
    }

    public boolean isTypeNumeric() {
        return isTypeNumeric;
    }

    public boolean isTypeDate() {
        return isTypeDate;
    }

    /**
       Set the DateFormat property.

       @param value The new value for DateFormat
    **/
    public void setDateFormat (SimpleDateFormat value) {
	dateFormat = value;
    }

    /**
       Get the DateFormat property.

       @return The DateFormat
    **/
    public SimpleDateFormat getDateFormat () {
	return dateFormat;
    }


}
