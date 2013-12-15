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

package org.ramadda.data.record;


import org.ramadda.util.Utils;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


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

    /** _more_ */
    public static final String PROP_BITFIELDS = "bitfields";


    /** _more_ */
    public static final String TYPE_NUMERIC = "numeric";

    /** _more_ */
    public static final String TYPE_STRING = "string";

    /** _more_ */
    public static final String TYPE_DATE = "date";

    /** _more_ */
    public static final String TYPE_INTEGER = "integer";

    /** _more_ */
    private boolean isTypeNumeric = true;

    /** _more_ */
    private boolean isTypeString = false;

    /** _more_ */
    private boolean isTypeDate = false;

    /** _more_          */
    private boolean isDate = false;

    /** _more_          */
    private boolean isTime = false;

    /** _more_ */
    private SimpleDateFormat dateFormat;

    /** _more_ */
    private int utcOffset = 0;

    /** _more_ */
    private double roundingFactor = 0;

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

    /** _more_ */
    private boolean skip = false;

    /** _more_ */
    private boolean synthetic = false;

    /** _more_ */
    private double defaultDoubleValue = Double.NaN;

    /** _more_ */
    private String defaultStringValue = null;

    /** _more_ */
    private String headerPattern = null;

    /** _more_ */
    private String type = TYPE_NUMERIC;

    /** _more_ */
    private double missingValue = Double.NaN;


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
        return "field:" + name + " label: " + label + " param:" + paramId;
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
     * @param pw _more_
     * @param name _more_
     * @param value _more_
     */
    private void attr(PrintWriter pw, String name, String value) {
        pw.print(name);
        pw.append("=\"");
        pw.print(value);
        pw.print("\" ");
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
            attr(pw, "unit", unit);
        }
        if (arity > 1) {
            attr(pw, "size", "" + arity);
        }
        if (isTypeString) {
            attr(pw, "type", TYPE_STRING);
        } else if (isTypeDate) {
            attr(pw, "type", TYPE_DATE);
        } else {
            //Default is numeric
        }

        if (Utils.stringDefined(label)) {
            attr(pw, "label", label);
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
     * Set the RoundingFactor property.
     *
     * @param value The new value for RoundingFactor
     */
    public void setRoundingFactor(double value) {
        roundingFactor = value;
    }

    /**
     * Get the RoundingFactor property.
     *
     * @return The RoundingFactor
     */
    public double getRoundingFactor() {
        return roundingFactor;
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

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasDefaultValue() {
        return hasDefaultDoubleValue() || hasDefaultStringValue();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasDefaultDoubleValue() {
        return !Double.isNaN(defaultDoubleValue);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasDefaultStringValue() {
        return defaultStringValue != null;
    }


    /**
     *  Set the DefaultValue property.
     *
     *  @param value The new value for DefaultValue
     */
    public void setDefaultDoubleValue(double value) {
        defaultDoubleValue = value;
    }

    /**
     *  Get the DefaultValue property.
     *
     *  @return The DefaultValue
     */
    public double getDefaultDoubleValue() {
        return defaultDoubleValue;
    }

    /**
     *  Set the DefaultStringValue property.
     *
     *  @param value The new value for DefaultStringValue
     */
    public void setDefaultStringValue(String value) {
        defaultStringValue = value;
    }

    /**
     *  Get the DefaultStringValue property.
     *
     *  @return The DefaultStringValue
     */
    public String getDefaultStringValue() {
        return defaultStringValue;
    }




    /**
     *  Set the Type property.
     *
     *  @param value The new value for Type
     */
    public void setType(String value) {
        type = value;
        isTypeNumeric = value.equals(TYPE_NUMERIC)
                        || value.equals(TYPE_INTEGER);
        isTypeString = value.equals(TYPE_STRING);
        isTypeDate   = value.equals(TYPE_DATE);
    }

    /**
     *  Get the Type property.
     *
     *  @return The Type
     */
    public String getType() {
        return type;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isTypeString() {
        return isTypeString;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isTypeInteger() {
        return (isTypeNumeric && type.equals(TYPE_INTEGER));
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isTypeNumeric() {
        return isTypeNumeric;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isTypeDate() {
        return isTypeDate;
    }

    /**
     *  Set the DateFormat property.
     *
     *  @param value The new value for DateFormat
     */
    public void setDateFormat(SimpleDateFormat value) {
        dateFormat = value;
    }

    /**
     *  Get the DateFormat property.
     *
     *  @return The DateFormat
     */
    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * Set the UtcOffset property.
     *
     * @param value The new value for UtcOffset
     */
    public void setUtcOffset(int value) {
        utcOffset = value;
    }

    /**
     * Get the UtcOffset property.
     *
     * @return The UtcOffset
     */
    public int getUtcOffset() {
        return utcOffset;
    }




    /**
     *  Set the MissingValue property.
     *
     *  @param value The new value for MissingValue
     */
    public void setMissingValue(double value) {
        missingValue = value;
    }

    /**
     *  Get the MissingValue property.
     *
     *  @return The MissingValue
     */
    public double getMissingValue() {
        return missingValue;
    }

    /**
     *  Set the HeaderPattern property.
     *
     *  @param value The new value for HeaderPattern
     */
    public void setHeaderPattern(String value) {
        headerPattern = value;
    }

    /**
     *  Get the HeaderPattern property.
     *
     *  @return The HeaderPattern
     */
    public String getHeaderPattern() {
        return headerPattern;
    }

    /**
     * Set the IsDate property.
     *
     * @param value The new value for IsDate
     */
    public void setIsDate(boolean value) {
        isDate = value;
    }

    /**
     * Get the IsDate property.
     *
     * @return The IsDate
     */
    public boolean getIsDate() {
        return isDate;
    }

    /**
     * Set the IsTime property.
     *
     * @param value The new value for IsTime
     */
    public void setIsTime(boolean value) {
        isTime = value;
    }

    /**
     * Get the IsTime property.
     *
     * @return The IsTime
     */
    public boolean getIsTime() {
        return isTime;
    }



}
