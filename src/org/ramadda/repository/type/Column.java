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

package org.ramadda.repository.type;


import org.ramadda.repository.*;
import org.ramadda.repository.database.DatabaseManager;

import org.ramadda.repository.map.*;
import org.ramadda.repository.output.OutputType;


import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;
import org.ramadda.util.FormInfo;


import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.data.gis.KmlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;

import java.util.Hashtable;
import java.util.List;


/**
 */

public class Column implements DataTypes, Constants {

    /** _more_ */
    public static final String ARG_EDIT_PREFIX = "edit.";

    /** _more_ */
    public static final String ARG_SEARCH_PREFIX = "search.";

    /** _more_ */
    public static final String OUTPUT_HTML = "html";

    /** _more_ */
    public static final String OUTPUT_CSV = "csv";

    /** _more_ */
    private SimpleDateFormat dateTimeFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /** _more_ */
    private SimpleDateFormat fullDateTimeFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    /** _more_ */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /** _more_ */
    private SimpleDateFormat dateParser = null;

    /** _more_ */
    public static final String EXPR_EQUALS = "=";

    /** _more_ */
    public static final String EXPR_LE = "<=";

    /** _more_ */
    public static final String EXPR_GE = ">=";

    /** _more_ */
    public static final String EXPR_BETWEEN = "between";

    /** _more_ */
    public static final List EXPR_ITEMS =
        Misc.newList(new TwoFacedObject("=", EXPR_EQUALS),
                     new TwoFacedObject("<=", EXPR_LE),
                     new TwoFacedObject(">=", EXPR_GE),
                     new TwoFacedObject("between", EXPR_BETWEEN));

    /** _more_ */
    public static final String EXPR_PATTERN = EXPR_EQUALS + "|" + EXPR_LE
                                              + "|" + EXPR_GE + "|"
                                              + EXPR_BETWEEN;

    /** _more_ */
    public static final String SEARCHTYPE_TEXT = "text";

    /** _more_ */
    public static final String SEARCHTYPE_SELECT = "select";


    /** _more_ */
    public static final String TAG_COLUMN = "column";

    /** _more_ */
    public static final String ATTR_NAME = "name";

    /** _more_ */
    public static final String ATTR_FORMAT = "format";

    /** _more_ */
    public static final String ATTR_CHANGETYPE = "changetype";

    /** _more_ */
    public static final String ATTR_ADDTOFORM = "addtoform";

    /** _more_ */
    public static final String ATTR_GROUP = "group";

    /** _more_ */
    public static final String ATTR_UNIT = "unit";

    /** _more_ */
    public static final String ATTR_OLDNAMES = "oldnames";

    /** _more_ */
    public static final String ATTR_SUFFIX = "suffix";

    /** _more_ */
    public static final String ATTR_PROPERTIES = "properties";

    /** _more_ */
    public static final String ATTR_LABEL = "label";

    /** _more_ */
    public static final String ATTR_DESCRIPTION = "description";

    /** _more_ */
    public static final String ATTR_TYPE = "type";

    /** _more_ */
    public static final String ATTR_ISINDEX = "isindex";

    /** _more_ */
    public static final String ATTR_ISCATEGORY = "iscategory";

    /** _more_ */
    public static final String ATTR_CANSEARCH = "cansearch";

    /** _more_ */
    public static final String ATTR_ADVANCED = "advanced";

    /** _more_ */
    public static final String ATTR_CANLIST = "canlist";

    /** _more_ */
    public static final String ATTR_EDITABLE = "editable";

    /** _more_ */
    public static final String ATTR_VALUES = "values";

    /** _more_ */
    public static final String ATTR_DEFAULT = "default";

    /** _more_ */
    public static final String ATTR_SIZE = "size";

    /** _more_ */
    public static final String ATTR_MIN = "min";

    /** _more_ */
    public static final String ATTR_MAX = "max";

    /** _more_ */
    public static final String ATTR_REQUIRED = "required";

    /** _more_ */
    public static final String ATTR_ROWS = "rows";

    /** _more_ */
    public static final String ATTR_COLUMNS = "columns";

    /** _more_ */
    public static final String ATTR_SEARCHTYPE = "searchtype";

    /** _more_ */
    public static final String ATTR_SHOWINHTML = "showinhtml";

    /** _more_ */
    public static final String ATTR_SHOWLABEL = "showlabel";

    /** _more_ */
    public static final String ATTR_CANEXPORT = "canexport";




    /** Lat/Lon format */
    private DecimalFormat latLonFormat = new DecimalFormat("##0.00");


    /** _more_ */
    private TypeHandler typeHandler;


    /** _more_ */
    private String name;

    /** _more_ */
    private String group;

    /** _more_ */
    private List oldNames;

    /** _more_ */
    private String label;

    /** _more_ */
    private String description;


    /** _more_ */
    private String type;

    /** _more_ */
    private boolean changeType = false;

    /** _more_ */
    private boolean showEmpty = true;

    /** _more_ */
    private String suffix;

    /** _more_ */
    private String searchType = SEARCHTYPE_TEXT;

    /** _more_ */
    private boolean isIndex;

    /** _more_ */
    private boolean isCategory;

    /** _more_ */
    private boolean canSearch;



    /** _more_ */
    private boolean advancedSearch;

    /** _more_ */
    private boolean editable;

    /** _more_ */
    private boolean canList;

    /** _more_ */
    private List<TwoFacedObject> enumValues;

    /** _more_ */
    private Hashtable<String, String> enumMap = new Hashtable<String,
                                                    String>();



    /** _more_ */
    private String dflt;

    /** _more_ */
    private double dfltDouble = Double.NaN;

    /** _more_ */
    private int size = 200;

    /** _more_ */
    private double min = Double.NaN;

    /** _more_ */
    private double max = Double.NaN;

    /** _more_ */
    private boolean required = false;

    /** _more_ */
    private int rows = 1;

    /** _more_ */
    private int columns = 40;

    /** _more_ */
    private String propertiesFile;

    /** _more_ */
    private int offset;

    /** _more_ */
    private boolean canShow = true;

    /** _more_ */
    private boolean showLabel = true;

    /** _more_ */
    private boolean canExport = true;


    /** _more_ */
    private boolean addToForm = true;

    /** _more_ */
    private Hashtable<String, String> properties = new Hashtable<String,
                                                       String>();

    /**
     * _more_
     *
     * @param typeHandler _more_
     * @param name _more_
     * @param type _more_
     * @param offset _more_
     *
     * @throws Exception _more_
     */
    public Column(TypeHandler typeHandler, String name, String type,
                  int offset)
            throws Exception {
        this.typeHandler = typeHandler;
        this.name        = name;
        this.type        = type;
        this.offset      = offset;
    }


    /**
     * _more_
     *
     * @param typeHandler _more_
     * @param element _more_
     * @param offset _more_
     *
     * @throws Exception _more_
     */
    public Column(TypeHandler typeHandler, Element element, int offset)
            throws Exception {

        this.typeHandler = typeHandler;
        this.offset      = offset;

        name             = XmlUtil.getAttribute(element, ATTR_NAME);
        group = XmlUtil.getAttribute(element, ATTR_GROUP, (String) null);
        oldNames = StringUtil.split(XmlUtil.getAttribute(element,
                ATTR_OLDNAMES, ""), ",", true, true);
        suffix = XmlUtil.getAttribute(element, ATTR_SUFFIX, "");
        label  = XmlUtil.getAttribute(element, ATTR_LABEL, name);
        searchType = XmlUtil.getAttribute(element, ATTR_SEARCHTYPE,
                                          searchType);
        propertiesFile = XmlUtil.getAttribute(element, ATTR_PROPERTIES,
                (String) null);

        String dttmFormat = XmlUtil.getAttribute(element, ATTR_FORMAT,
                                (String) null);
        if (dttmFormat != null) {
            dateParser = new SimpleDateFormat(dttmFormat);
        }

        description    = getAttributeOrTag(element, ATTR_DESCRIPTION, label);

        type = Utils.getAttributeOrTag(element, ATTR_TYPE, DATATYPE_STRING);
        changeType     = getAttributeOrTag(element, ATTR_CHANGETYPE, false);

        showEmpty      = getAttributeOrTag(element, "showempty", true);
        dflt           = getAttributeOrTag(element, ATTR_DEFAULT, "").trim();
        isIndex        = getAttributeOrTag(element, ATTR_ISINDEX, false);
        isCategory     = getAttributeOrTag(element, ATTR_ISCATEGORY, false);
        canSearch      = getAttributeOrTag(element, ATTR_CANSEARCH, false);
        advancedSearch = getAttributeOrTag(element, ATTR_ADVANCED, false);
        editable       = getAttributeOrTag(element, ATTR_EDITABLE, true);
        addToForm      = getAttributeOrTag(element, ATTR_ADDTOFORM,
                                           addToForm);
        canShow        = getAttributeOrTag(element, ATTR_SHOWINHTML, canShow);
        showLabel      = getAttributeOrTag(element, ATTR_SHOWLABEL,
                                           showLabel);
        canExport      = getAttributeOrTag(element, ATTR_CANEXPORT,
                                           canExport);
        canList        = getAttributeOrTag(element, ATTR_CANLIST, true);
        size           = getAttributeOrTag(element, ATTR_SIZE, size);
        min            = getAttributeOrTag(element, ATTR_MIN, min);
        max            = getAttributeOrTag(element, ATTR_MAX, max);
        required       = getAttributeOrTag(element, ATTR_REQUIRED, required);
        rows           = getAttributeOrTag(element, ATTR_ROWS, rows);
        columns        = getAttributeOrTag(element, ATTR_COLUMNS, columns);

        List propNodes = XmlUtil.findChildren(element, "property");
        for (int i = 0; i < propNodes.size(); i++) {
            Element propNode = (Element) propNodes.get(i);
            properties.put(XmlUtil.getAttribute(propNode, "name"),
                           XmlUtil.getAttribute(propNode, "value"));
        }

        if (isEnumeration()) {
            String bulkEnums = null;
            String valueString = XmlUtil.getAttribute(element, ATTR_VALUES,
                                     (String) null);
            if (valueString != null) {
                setEnums(valueString, ",");
            } else {
                valueString = XmlUtil.getGrandChildText(element, ATTR_VALUES,
                        (String) null);
                if (valueString != null) {
                    setEnums(valueString, "\n");
                }
            }
            if (enumValues == null) {
                enumValues = new ArrayList<TwoFacedObject>();
            }
        }

        if (isNumeric() && Utils.stringDefined(dflt)) {
            dfltDouble = Double.parseDouble(dflt);
        }


    }

    /**
     * _more_
     *
     * @param valueString _more_
     * @param delimiter _more_
     *
     * @throws Exception _more_
     */
    private void setEnums(String valueString, String delimiter)
            throws Exception {

        if (valueString.startsWith("file:")) {
            valueString = typeHandler.getStorageManager().readSystemResource(
                valueString.substring("file:".length()));
            delimiter = "\n";
        }


        List<String> tmp = StringUtil.split(valueString, delimiter, true,
                                            true);
        enumValues = new ArrayList<TwoFacedObject>();
        for (String tok : tmp) {
            if (tok.startsWith("#")) {
                continue;
            }
            String label = tok;
            String value = tok;
            if (tok.indexOf(":") >= 0) {
                List<String> toks = StringUtil.splitUpTo(tok, ":", 2);

                value = toks.get(0);
                label = toks.get(1);
            } else if (tok.indexOf("=") >= 0) {
                List<String> toks = StringUtil.splitUpTo(tok, "=", 2);

                value = toks.get(0);
                label = toks.get(1);
            }
            enumValues.add(new TwoFacedObject(label, value));
            enumMap.put(value, label);
        }
    }



    /**
     * _more_
     *
     * @param t _more_
     *
     * @return _more_
     */
    public boolean isType(String t) {
        return type.equals(t);
    }

    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Hashtable getProperties() {
        return properties;
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public String msg(String s) {
        return typeHandler.msg(s);
    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public String msgLabel(String s) {
        return typeHandler.msgLabel(s);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Repository getRepository() {
        return typeHandler.getRepository();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isNumeric() {
        return isType(DATATYPE_INT) || isDouble();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isBoolean() {
        return isType(DATATYPE_BOOLEAN);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEnumeration() {
        return isType(DATATYPE_ENUMERATION)
               || isType(DATATYPE_ENUMERATIONPLUS);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isDate() {
        return isType(DATATYPE_DATETIME) || isType(DATATYPE_DATE);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isDouble() {
        return isType(DATATYPE_DOUBLE) || isType(DATATYPE_PERCENTAGE);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isString() {
        return isType(DATATYPE_STRING) || isEnumeration()
               || isType(DATATYPE_ENTRY) || isType(DATATYPE_EMAIL)
               || isType(DATATYPE_URL) || isType(DATATYPE_LIST);
    }

    /**
     * _more_
     *
     * @param values _more_
     *
     * @return _more_
     */
    public Object getObject(Object[] values) {
        if (values == null) {
            return null;
        }
        int idx = getOffset();
        if (idx >= values.length) {
            return null;
        }
        if (values[idx] == null) {
            return null;
        }

        return values[idx];
    }


    /**
     * _more_
     *
     * @param values _more_
     *
     * @return _more_
     */
    public String getString(Object[] values) {
        if (values == null) {
            return null;
        }
        int idx = getOffset();
        if (idx >= values.length) {
            return null;
        }
        if (values[idx] == null) {
            return null;
        }
        if (isType(DATATYPE_PASSWORD)) {
            return null;
        }

        return values[idx].toString();
    }


    /**
     * _more_
     *
     * @param values _more_
     * @param idx _more_
     *
     * @return _more_
     */
    public String toString(Object[] values, int idx) {
        if (values == null) {
            return ((dflt != null)
                    ? dflt
                    : "");
        }
        if (values[idx] == null) {
            return ((dflt != null)
                    ? dflt
                    : "");
        }

        return values[idx].toString();
    }


    /**
     * _more_
     *
     * @param values _more_
     * @param idx _more_
     *
     * @return _more_
     */
    private String toLatLonString(Object[] values, int idx) {
        if (values == null) {
            return ((dflt != null)
                    ? dflt
                    : "NA");
        }
        if (values[idx] == null) {
            return ((dflt != null)
                    ? dflt
                    : "NA");
        }
        if ( !latLonOk(values[idx])) {
            return "NA";
        }
        double d = ((Double) values[idx]).doubleValue();

        return latLonFormat.format(d);
    }

    /**
     * _more_
     *
     * @param values _more_
     * @param idx _more_
     *
     * @return _more_
     */
    private boolean toBoolean(Object[] values, int idx) {
        if (values[idx] == null) {
            if (Utils.stringDefined(dflt)) {
                return new Boolean(dflt).booleanValue();
            }

            return true;
        }

        return ((Boolean) values[idx]).booleanValue();
    }


    /**
     * _more_
     *
     *
     * @param entry _more_
     * @param sb _more_
     * @param output _more_
     * @param values _more_
     *
     * @throws Exception _more_
     */
    public void formatValue(Entry entry, StringBuffer sb, String output,
                            Object[] values)
            throws Exception {
        formatValue(entry, sb, output, values, null);
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param sb _more_
     * @param output _more_
     * @param values _more_
     * @param sdf _more_
     *
     * @throws Exception _more_
     */
    public void formatValue(Entry entry, StringBuffer sb, String output,
                            Object[] values, SimpleDateFormat sdf)
            throws Exception {

        boolean csv       = Misc.equals(output, OUTPUT_CSV);
        String  delimiter = csv
                            ? "|"
                            : ",";
        if (isType(DATATYPE_LATLON)) {
            sb.append(toLatLonString(values, offset));
            sb.append(delimiter);
            sb.append(toLatLonString(values, offset + 1));
        } else if (isType(DATATYPE_LATLONBBOX)) {
            sb.append(toLatLonString(values, offset));
            sb.append(delimiter);
            sb.append(toLatLonString(values, offset + 1));
            sb.append(delimiter);
            sb.append(toLatLonString(values, offset + 2));
            sb.append(delimiter);
            sb.append(toLatLonString(values, offset + 3));
        } else if (isType(DATATYPE_PERCENTAGE)) {
            if (csv) {
                sb.append(toString(values, offset));
            } else {
                //                System.err.println("offset:" + offset +" values:");
                //                Misc.printArray("", values);
                double percent = (Double) values[offset];
                sb.append((int) (percent * 100) + "");
            }

        } else if (isType(DATATYPE_DOUBLE)) {
            if (csv) {
                sb.append(toString(values, offset));
            } else {
                double v = (Double) values[offset];
                if ((v == dfltDouble) && !getShowEmpty()) {
                    return;
                }
                sb.append(v);
            }
        } else if (isType(DATATYPE_DATETIME)) {
            if (sdf != null) {
                sb.append(sdf.format((Date) values[offset]));
            } else {
                sb.append(dateTimeFormat.format((Date) values[offset]));
            }
        } else if (isType(DATATYPE_DATE)) {
            if (sdf != null) {
                sb.append(sdf.format((Date) values[offset]));
            } else {
                sb.append(dateFormat.format((Date) values[offset]));
            }
        } else if (isType(DATATYPE_ENTRY)) {
            String entryId  = toString(values, offset);
            Entry  theEntry = null;
            if (Utils.stringDefined(entryId)) {
                try {
                    theEntry =
                        getRepository().getEntryManager().getEntry(null,
                            entryId);
                } catch (Exception exc) {
                    throw new RuntimeException(exc);
                }
            }
            if (csv) {
                sb.append(entryId);
            } else {
                if (theEntry != null) {
                    try {
                        String link =
                            getRepository().getEntryManager().getAjaxLink(
                                getRepository().getTmpRequest(), theEntry,
                                theEntry.getName()).toString();
                        sb.append(link);
                    } catch (Exception exc) {
                        throw new RuntimeException(exc);
                    }

                } else {
                    sb.append("---");
                }

            }
        } else if (isType(DATATYPE_EMAIL)) {
            String s = toString(values, offset);
            if (csv) {
                sb.append(s);
            } else {
                sb.append("<a href=\"mailto:" + s + "\">" + s + "</a>");
            }
        } else if (isType(DATATYPE_URL)) {
            String       s    = toString(values, offset);
            List<String> urls = StringUtil.split(s, "\n");
            if (csv) {
                s = StringUtil.join(delimiter, urls);
                sb.append(s);
            } else {
                int cnt = 0;
                for (String url : urls) {
                    if (cnt > 0) {
                        sb.append("<br>");
                    }
                    cnt++;
                    sb.append("<a href=\"" + url + "\">" + url + "</a>");
                }
            }
        } else {
            String s = toString(values, offset);
            if (csv) {
                s = s.replaceAll(",", "_COMMA_");
                s = s.replaceAll("\n", " ");
            }
            if (s.length() == 0) {
                if ( !getShowEmpty()) {
                    return;
                }
            }

            if (rows > 1) {
                s = getRepository().getWikiManager().wikifyEntry(
                    getRepository().getTmpRequest(), entry, s, false, null,
                    null);
            } else if (isEnumeration()) {
                //                String label = enumMap.get(s);
                //                if (label != null) {
                //                    s = label;
                //                }
            }
            sb.append(s);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Hashtable<String, String> getEnumTable() {
        return enumMap;
    }


    /**
     * _more_
     *
     * @param value _more_
     *
     * @return _more_
     */
    public String getEnumLabel(String value) {
        String label = enumMap.get(value);
        if (label == null) {
            return value;
        }

        return label;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getOffset() {
        return offset;
    }

    /**
     * _more_
     *
     * @param statement _more_
     * @param values _more_
     * @param statementIdx _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected int setValues(PreparedStatement statement, Object[] values,
                            int statementIdx)
            throws Exception {

        if (offset >= values.length) {
            return 0;
        }
        if (isType(DATATYPE_INT)) {
            if (values[offset] != null) {
                statement.setInt(statementIdx,
                                 ((Integer) values[offset]).intValue());
            } else {
                int value = 0;
                if (Utils.stringDefined(dflt)) {
                    value = Integer.parseInt(dflt);
                }
                statement.setInt(statementIdx, value);
            }
            statementIdx++;
        } else if (isDouble()) {
            if (values[offset] != null) {
                double value = ((Double) values[offset]).doubleValue();
                if ( !Double.isNaN(value)) {
                    if ( !Double.isNaN(min)) {
                        if (value < min) {
                            throw new IllegalArgumentException(
                                "Invalid value for " + getLabel() + " "
                                + value + " < " + min);
                        }
                    }
                    if ( !Double.isNaN(max)) {
                        if (value > max) {
                            throw new IllegalArgumentException(
                                "Invalid value for " + getLabel() + " "
                                + value + " > " + max);
                        }
                    }
                }
                statement.setDouble(statementIdx, value);
            } else {
                //                double value = Double.NaN;
                double value = 0;
                if (Utils.stringDefined(dflt)) {
                    value = Double.parseDouble(dflt);
                }
                statement.setDouble(statementIdx, value);
            }
            statementIdx++;
        } else if (isType(DATATYPE_BOOLEAN)) {
            if (values[offset] != null) {
                boolean v = ((Boolean) values[offset]).booleanValue();
                statement.setInt(statementIdx, (v
                        ? 1
                        : 0));
            } else {
                int value = 0;
                if (Utils.stringDefined(dflt)) {
                    value = dflt.equals("true")
                            ? 1
                            : 0;
                }
                statement.setInt(statementIdx, value);
            }
            statementIdx++;
        } else if (isDate()) {
            Date dttm = (Date) values[offset];
            getRepository().getDatabaseManager().setDate(statement,
                    statementIdx, dttm);
            statementIdx++;
        } else if (isType(DATATYPE_LATLON)) {
            if (values[offset] != null) {
                double lat = ((Double) values[offset]).doubleValue();
                double lon = ((Double) values[offset + 1]).doubleValue();
                if (Double.isNaN(lat)) {
                    lat = Entry.NONGEO;
                }
                if (Double.isNaN(lon)) {
                    lon = Entry.NONGEO;
                }
                statement.setDouble(statementIdx, lat);
                statement.setDouble(statementIdx + 1, lon);
            } else {
                statement.setDouble(statementIdx, Entry.NONGEO);
                statement.setDouble(statementIdx + 1, Entry.NONGEO);
            }
            statementIdx += 2;
        } else if (isType(DATATYPE_LATLONBBOX)) {
            for (int i = 0; i < 4; i++) {
                if (values[offset + i] != null) {
                    statement.setDouble(
                        statementIdx++,
                        ((Double) values[offset + i]).doubleValue());
                } else {
                    statement.setDouble(statementIdx++, Entry.NONGEO);
                }
            }
        } else if (isType(DATATYPE_PASSWORD)) {
            if (values[offset] != null) {
                String value =
                    new String(RepositoryUtil.encodeBase64(toString(values,
                        offset).getBytes()).getBytes());
                statement.setString(statementIdx, value);
            } else {
                statement.setString(statementIdx, null);
            }
            statementIdx++;
        } else {
            //            System.err.println("\tset statement:" + offset + " " + values[offset]);
            if (values[offset] != null) {
                String value = toString(values, offset);
                //Check the value
                if (size > 0) {
                    getRepository().getEntryManager().checkColumnSize(
                        getName(), value, size);
                }
                if (required) {
                    if (value.trim().length() == 0) {
                        throw new IllegalArgumentException("Value "
                                + getLabel() + " is required");
                    }
                }
                statement.setString(statementIdx, value);
            } else {
                statement.setString(statementIdx, null);
            }
            statementIdx++;
        }

        return statementIdx;

    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param values _more_
     * @param node _more_
     *
     * @throws Exception _more_
     */
    public void addToEntryNode(Entry entry, Object[] values, Element node)
            throws Exception {
        if (values[offset] == null) {
            return;
        }
        String stringValue = null;
        //Don't export the password
        if (isType(DATATYPE_PASSWORD)) {
            return;
        }
        if (isType(DATATYPE_LATLON)) {
            stringValue = values[offset] + ";" + values[offset + 1];
        } else if (isType(DATATYPE_LATLONBBOX)) {
            stringValue = values[offset] + ";" + values[offset + 1] + ";"
                          + values[offset + 2] + ";" + values[offset + 3];
        } else if (isDate()) {
            fullDateTimeFormat.setTimeZone(RepositoryBase.TIMEZONE_UTC);
            stringValue = fullDateTimeFormat.format((Date) values[offset]);
        } else {
            stringValue = values[offset].toString();
        }
        Element valueNode = XmlUtil.create(node.getOwnerDocument(), name);
        node.appendChild(valueNode);
        valueNode.setAttribute("encoded", "true");
        valueNode.appendChild(XmlUtil.makeCDataNode(node.getOwnerDocument(),
                stringValue, true));
    }


    /**
     * _more_
     *
     * @param results _more_
     * @param values _more_
     * @param valueIdx _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public int readValues(ResultSet results, Object[] values, int valueIdx)
            throws Exception {
        if (isType(DATATYPE_INT)) {
            values[offset] = new Integer(results.getInt(valueIdx));
            valueIdx++;
        } else if (isType(DATATYPE_PERCENTAGE)) {
            values[offset] = new Double(results.getDouble(valueIdx));
            valueIdx++;
        } else if (isDouble()) {
            values[offset] = new Double(results.getDouble(valueIdx));
            valueIdx++;
        } else if (isType(DATATYPE_BOOLEAN)) {
            values[offset] = new Boolean(results.getInt(valueIdx) == 1);
            valueIdx++;
        } else if (isDate()) {
            values[offset] =
                typeHandler.getDatabaseManager().getTimestamp(results,
                    valueIdx);
            valueIdx++;
        } else if (isType(DATATYPE_LATLON)) {
            values[offset] = new Double(results.getDouble(valueIdx));
            valueIdx++;
            values[offset + 1] = new Double(results.getDouble(valueIdx));
            valueIdx++;
        } else if (isType(DATATYPE_LATLONBBOX)) {
            values[offset]     = new Double(results.getDouble(valueIdx++));
            values[offset + 1] = new Double(results.getDouble(valueIdx++));
            values[offset + 2] = new Double(results.getDouble(valueIdx++));
            values[offset + 3] = new Double(results.getDouble(valueIdx++));
        } else if (isType(DATATYPE_PASSWORD)) {
            String value = results.getString(valueIdx);
            if (value != null) {
                byte[] bytes = RepositoryUtil.decodeBase64(value);
                if (bytes != null) {
                    value = new String(bytes);
                }
            }
            values[offset] = value;
            valueIdx++;
        } else {
            values[offset] = results.getString(valueIdx);
            valueIdx++;
        }

        return valueIdx;
    }


    /**
     * _more_
     *
     * @param statement _more_
     * @param name _more_
     * @param type _more_
     *
     * @throws Exception _more_
     */
    private void defineColumn(Statement statement, String name, String type)
            throws Exception {


        String sql = "alter table " + getTableName() + " add column " + name
                     + " " + type;
        SqlUtil.loadSql(sql, statement, true);

        if (changeType) {
            sql = typeHandler.getDatabaseManager().getAlterTableSql(
                getTableName(), name, type);
            //            System.err.println("altering table: " + sql);
            SqlUtil.loadSql(sql, statement, true);
        }
    }


    /**
     * _more_
     *
     *
     * @param statement _more_
     *
     * @throws Exception _more_
     */
    public void createTable(Statement statement) throws Exception {
        if (isType(DATATYPE_STRING) || isType(DATATYPE_PASSWORD)
                || isType(DATATYPE_EMAIL) || isType(DATATYPE_URL)
                || isType(DATATYPE_FILE) || isType(DATATYPE_ENTRY)) {
            defineColumn(statement, name, "varchar(" + size + ") ");
        } else if (isType(DATATYPE_LIST)) {
            defineColumn(statement, name, "varchar(" + size + ") ");
        } else if (isType(DATATYPE_CLOB)) {
            String clobType =
                getRepository().getDatabaseManager().convertType("clob",
                    size);
            defineColumn(statement, name, clobType);
        } else if (isEnumeration()) {
            defineColumn(statement, name, "varchar(" + size + ") ");
        } else if (isType(DATATYPE_INT)) {
            defineColumn(statement, name, "int");
        } else if (isDouble()) {
            defineColumn(
                statement, name,
                getRepository().getDatabaseManager().convertType("double"));
        } else if (isType(DATATYPE_BOOLEAN)) {
            //use int as boolean for database compatibility
            defineColumn(statement, name, "int");

        } else if (isDate()) {
            defineColumn(
                statement, name,
                typeHandler.getDatabaseManager().convertSql(
                    "ramadda.datetime"));
        } else if (isType(DATATYPE_LATLON)) {
            defineColumn(
                statement, name + "_lat",
                getRepository().getDatabaseManager().convertType("double"));
            defineColumn(
                statement, name + "_lon",
                getRepository().getDatabaseManager().convertType("double"));
        } else if (isType(DATATYPE_LATLONBBOX)) {
            defineColumn(
                statement, name + "_north",
                getRepository().getDatabaseManager().convertType("double"));
            defineColumn(
                statement, name + "_west",
                getRepository().getDatabaseManager().convertType("double"));
            defineColumn(
                statement, name + "_south",
                getRepository().getDatabaseManager().convertType("double"));
            defineColumn(
                statement, name + "_east",
                getRepository().getDatabaseManager().convertType("double"));

        } else {
            throw new IllegalArgumentException("Unknown column type:" + type
                    + " for " + name);
        }


        if (oldNames != null) {
            for (int i = 0; i < oldNames.size(); i++) {
                String sql = "update " + getTableName() + " set " + name
                             + " = " + oldNames.get(i);
                SqlUtil.loadSql(sql, statement, true);
                sql = "alter table " + getTableName() + " drop "
                      + oldNames.get(i);
                SqlUtil.loadSql(sql, statement, true);
            }
        }

        if (isIndex) {
            SqlUtil.loadSql("CREATE INDEX " + getTableName() + "_INDEX_"
                            + name + "  ON " + getTableName() + " (" + name
                            + ")", statement, true);
        }

    }


    /**
     * _more_
     *
     * @param value _more_
     *
     * @return _more_
     */
    public Object convert(String value) {
        if (isType(DATATYPE_INT)) {
            return new Integer(value);
        } else if (isDouble()) {
            return new Double(value);
        } else if (isType(DATATYPE_BOOLEAN)) {
            return new Boolean(value);
        } else if (isType(DATATYPE_DATETIME)) {
            //TODO
        } else if (isType(DATATYPE_DATE)) {
            //TODO
        } else if (isType(DATATYPE_LATLON)) {
            //TODO
        } else if (isType(DATATYPE_LATLONBBOX)) {
            //TODO
        }

        return value;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getTableName() {
        return typeHandler.getTableName();
    }



    /**
     * _more_
     *
     * @param o _more_
     *
     * @return _more_
     */
    private boolean latLonOk(Object o) {
        if (o == null) {
            return false;
        }
        Double d = (Double) o;

        return latLonOk(d.doubleValue());
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    private boolean latLonOk(double v) {
        return ((v == v) && (v != Entry.NONGEO));
    }

    /**
     * _more_
     *
     * @param clauses _more_
     */
    public void addGeoExclusion(List<Clause> clauses) {
        if (isType(DATATYPE_LATLON)) {
            String id = getFullName();
            clauses.add(Clause.neq(id + "_lat", Entry.NONGEO));
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param where _more_
     * @param searchCriteria _more_
     *
     * @throws Exception _more_
     */
    public void assembleWhereClause(Request request, List<Clause> where,
                                    StringBuffer searchCriteria)
            throws Exception {

        String          searchArg  = getSearchArg();
        String          columnName = getFullName();

        DatabaseManager dbm        = getRepository().getDatabaseManager();

        if (isType(DATATYPE_LATLON)) {
            double north = request.get(searchArg + "_north", Double.NaN);
            double south = request.get(searchArg + "_south", Double.NaN);
            double east  = request.get(searchArg + "_east", Double.NaN);
            double west  = request.get(searchArg + "_west", Double.NaN);
            if (latLonOk(north)) {
                where.add(Clause.le(columnName + "_lat", north));
            }
            if (latLonOk(south)) {
                where.add(Clause.ge(columnName + "_lat", south));
            }
            if (latLonOk(west)) {
                where.add(Clause.ge(columnName + "_lon", west));
            }
            if (latLonOk(east)) {
                where.add(Clause.le(columnName + "_lon", east));
            }
        } else if (isType(DATATYPE_LATLONBBOX)) {
            double north = request.get(searchArg + "_north", Double.NaN);
            double south = request.get(searchArg + "_south", Double.NaN);
            double east  = request.get(searchArg + "_east", Double.NaN);
            double west  = request.get(searchArg + "_west", Double.NaN);

            if (latLonOk(north)) {
                where.add(Clause.le(columnName + "_north", north));
            }
            if (latLonOk(south)) {
                where.add(Clause.ge(columnName + "_south", south));
            }
            if (latLonOk(west)) {
                where.add(Clause.ge(columnName + "_west", west));
            }
            if (latLonOk(east)) {
                where.add(Clause.le(columnName + "_east", east));
            }
        } else if (isNumeric()) {
            String expr = request.getCheckedString(searchArg + "_expr",
                              EXPR_EQUALS, EXPR_PATTERN);
            double from  = request.get(searchArg + "_from", Double.NaN);
            double to    = request.get(searchArg + "_to", Double.NaN);
            double value = request.get(searchArg, Double.NaN);

            if (isType(DATATYPE_PERCENTAGE)) {
                from  = from / 100.0;
                to    = to / 100.0;
                value = value / 100.0;
            }
            if ((from == from) && (to != to)) {
                to = value;
            } else if ((from != from) && (to == to)) {
                from = value;
            } else if ((from != from) && (to != to)) {
                from = value;
                to   = value;
            }
            if (from == from) {
                if (expr.equals(EXPR_EQUALS)) {
                    where.add(Clause.eq(getFullName(), from));
                } else if (expr.equals(EXPR_LE)) {
                    where.add(Clause.le(getFullName(), from));
                } else if (expr.equals(EXPR_GE)) {
                    where.add(Clause.ge(getFullName(), from));
                } else if (expr.equals(EXPR_BETWEEN)) {
                    where.add(Clause.ge(getFullName(), from));
                    where.add(Clause.le(getFullName(), to));
                } else if (expr.length() > 0) {
                    throw new IllegalArgumentException("Unknown expression:"
                            + expr);
                }
            }
        } else if (isType(DATATYPE_BOOLEAN)) {
            if (request.defined(searchArg)) {
                where.add(Clause.eq(columnName, (request.get(searchArg, true)
                        ? 1
                        : 0)));
            }
        } else if (isDate()) {
            String relativeArg = searchArg + "_relative";
            Date[] dateRange = request.getDateRange(searchArg + "_fromdate",
                                   searchArg + "_todate", relativeArg,
                                   new Date());
            if (dateRange[0] != null) {
                where.add(Clause.ge(columnName, dateRange[0]));
            }

            if (dateRange[1] != null) {
                where.add(Clause.le(columnName, dateRange[1]));
            }
        } else if (isType(DATATYPE_ENTRY)) {
            String value = request.getString(searchArg + "_hidden", "");
            if (Utils.stringDefined(value)) {
                where.add(Clause.eq(columnName, value));
            }
        } else if (isType(DATATYPE_LIST)) {
            String value = getSearchValue(request, null);
            if (Utils.stringDefined(value)) {

                //value
                //value,...
                //....,value
                //....,value,...
                List<Clause> ors = new ArrayList<Clause>();
                ors.add(Clause.eq(columnName, value));
                ors.add(dbm.makeLikeTextClause(columnName, "%" + value + "%",
                        false));
                //                ors.add(Clause.like(columnName, value+",%"));
                //                ors.add(Clause.like(columnName, "%," + value));
                //                ors.add(Clause.like(columnName, "%," + value+",%"));
                where.add(Clause.or(ors));

                //                System.err.println("ORS:" + Clause.or(ors));
            }
        } else if (isEnumeration()) {
            List<String> values = getSearchValues(request);
            if ((values != null) && (values.size() > 0)) {
                List<Clause> subClauses = new ArrayList<Clause>();
                for (String value : values) {
                    if (value.startsWith("!")) {
                        subClauses.add(Clause.neq(columnName,
                                value.substring(1)));
                    } else {
                        subClauses.add(Clause.eq(columnName, value));
                    }
                }
                where.add(Clause.or(subClauses));
            }
        } else {
            String value = getSearchValue(request, null);
            if (Utils.stringDefined(value)) {
                addTextSearch(value, where);
            }
            //            typeHandler.addOrClause(columnName,
            //                                    value, where);
        }



    }


    /**
     * _more_
     *
     * @param value _more_
     * @param where _more_
     */
    public void addTextSearch(String value, List<Clause> where) {
        if (value.startsWith("!")) {
            value = value.substring(1);
            where.add(Clause.notLike(getFullName(), "%" + value + "%"));
        } else {
            DatabaseManager dbm = getRepository().getDatabaseManager();
            where.add(dbm.makeLikeTextClause(getFullName(),
                                             "%" + value + "%", false));
        }

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    private String getSearchValue(Request request, String dflt) {
        String searchArg = getSearchArg();
        if (request.defined(searchArg)) {
            return request.getString(searchArg, dflt);
        }

        return dflt;
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    private List<String> getSearchValues(Request request) {
        List<String> result    = new ArrayList<String>();
        String       searchArg = getSearchArg();
        if (request.defined(searchArg)) {
            for (String arg : (List<String>) request.get(searchArg, result)) {
                result.addAll(StringUtil.split(arg, ",", true, true));
            }
        }

        return result;
    }


    /**
     * _more_
     *
     * @param arg _more_
     * @param value _more_
     * @param values _more_
     *
     * @return _more_
     */
    public int matchValue(String arg, Object value, Object[] values) {
        if (isType(DATATYPE_LATLON)) {
            //TODO:
        } else if (isType(DATATYPE_LATLONBBOX)) {
            //TODO:
        } else if (isType(DATATYPE_BOOLEAN)) {
            if (arg.equals(getFullName())) {
                if (values[offset].toString().equals(value)) {
                    return TypeHandler.MATCH_TRUE;
                }

                return TypeHandler.MATCH_FALSE;
            }
        } else if (isNumeric()) {
            //
        } else {
            if (arg.equals(getFullName())) {
                if (values[offset].equals(value)) {
                    return TypeHandler.MATCH_TRUE;
                }

                return TypeHandler.MATCH_FALSE;
            }
        }

        return TypeHandler.MATCH_UNKNOWN;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param formBuffer _more_
     * @param entry _more_
     * @param values _more_
     * @param state _more_
     * @param formInfo _more_
     *
     * @throws Exception _more_
     */
    public void addToEntryForm(Request request, Entry entry,
                               StringBuffer formBuffer, Object[] values,
                               Hashtable state, FormInfo formInfo)
            throws Exception {
        if ( !addToForm) {
            return;
        }
        String widget = getFormWidget(request, entry, values, formInfo);
        //        formBuffer.append(HtmlUtils.formEntry(getLabel() + ":",
        //                                             HtmlUtils.hbox(widget, suffix)));
        if ((group != null) && (state.get(group) == null)) {
            formBuffer.append(
                HtmlUtils.row(
                    HtmlUtils.colspan(
                        HtmlUtils.div(group, " class=\"formgroupheader\" "),
                        2)));
            state.put(group, group);
        }
        if (rows > 1) {
            formBuffer.append(typeHandler.formEntryTop(request,
                    getLabel() + ":", widget));
        } else {
            formBuffer.append(typeHandler.formEntry(request,
                    getLabel() + ":", widget));
        }
        formBuffer.append("\n");
    }




    //For now just change the edit argument by adding a edit. prefix

    /**
     * _more_
     *
     * @return _more_
     */
    public String getEditArg() {
        return ARG_EDIT_PREFIX + getFullName();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getSearchArg() {
        return ARG_SEARCH_PREFIX + getFullName();
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param values _more_
     * @param formInfo _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getFormWidget(Request request, Entry entry,
                                Object[] values, FormInfo formInfo)
            throws Exception {

        String widget = "";

        String urlArg = getEditArg();

        if (isType(DATATYPE_LATLON)) {
            double lat = 0;
            double lon = 0;
            if (values != null) {
                lat = ((Double) values[offset]).doubleValue();
                lon = ((Double) values[offset + 1]).doubleValue();
            }
            MapInfo map = getRepository().getMapManager().createMap(request,
                              true);
            widget = map.makeSelector(urlArg, true,
                                      new String[] { latLonOk(lat)
                    ? lat + ""
                    : "", latLonOk(lon)
                          ? lon + ""
                          : "" });
        } else if (isType(DATATYPE_LATLONBBOX)) {
            String[] nwse = null;
            if (values != null) {
                nwse = new String[] { latLonOk(values[offset + 0])
                                      ? values[offset + 0] + ""
                                      : "", latLonOk(values[offset + 1])
                                            ? values[offset + 1] + ""
                                            : "", latLonOk(values[offset + 2])
                        ? values[offset + 2] + ""
                        : "", latLonOk(values[offset + 3])
                              ? values[offset + 3] + ""
                              : "", };
            }
            MapInfo map = getRepository().getMapManager().createMap(request,
                              true);
            widget = map.makeSelector(urlArg, true, nwse, "", "");
        } else if (isType(DATATYPE_BOOLEAN)) {
            boolean value = true;
            if (values != null) {
                if (toBoolean(values, offset)) {
                    value = true;
                } else {
                    value = false;
                }
            } else {
                value = Misc.equals(dflt, "true");
            }
            //            widget = HtmlUtils.checkbox(urlArg, "true", value);
            List<TwoFacedObject> items = new ArrayList<TwoFacedObject>();
            items.add(new TwoFacedObject("Yes", "true"));
            items.add(new TwoFacedObject("No", "false"));
            widget = HtmlUtils.select(urlArg, items, value
                    ? "true"
                    : "false");
        } else if (isType(DATATYPE_DATETIME)) {
            Date date;
            if (values != null) {
                date = (Date) values[offset];
            } else {
                date = new Date();
            }
            widget = getRepository().getPageHandler().makeDateInput(request,
                    urlArg, "", date, null);
        } else if (isType(DATATYPE_DATE)) {
            Date date;
            if (values != null) {
                date = (Date) values[offset];
            } else {
                date = new Date();
            }
            widget = getRepository().getPageHandler().makeDateInput(request,
                    urlArg, "", date, null, false);
        } else if (isType(DATATYPE_ENUMERATION)) {
            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = (String) toString(values, offset);
            }
            widget = HtmlUtils.select(urlArg, enumValues, value,
                                      HtmlUtils.cssClass("column-select"));
        } else if (isType(DATATYPE_ENUMERATIONPLUS)) {
            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = (String) toString(values, offset);
            }
            List enums = getEnumPlusValues(request, entry);
            widget = HtmlUtils.select(
                urlArg, enums, value,
                HtmlUtils.cssClass("column-select")) + "  or:  "
                    + HtmlUtils.input(
                        urlArg + "_plus", "", HtmlUtils.SIZE_20);
        } else if (isType(DATATYPE_INT)) {
            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = "" + toString(values, offset);
            }
            widget = HtmlUtils.input(urlArg, value, HtmlUtils.SIZE_10);
        } else if (isType(DATATYPE_DOUBLE)) {
            String domId = HtmlUtils.getUniqueId("input_");
            if ( !Double.isNaN(max)) {
                formInfo.addMaxValidation(getLabel(), domId, max);
            }
            if ( !Double.isNaN(min)) {
                formInfo.addMinValidation(getLabel(), domId, min);
            }

            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = "" + toString(values, offset);
            }
            widget = HtmlUtils.input(urlArg, value,
                                     HtmlUtils.SIZE_10 + HtmlUtils.id(domId));
        } else if (isType(DATATYPE_PERCENTAGE)) {
            String value = ((dflt != null)
                            ? dflt
                            : "0");

            if (values != null) {
                value = "" + toString(values, offset);
            }
            if (value.trim().length() == 0) {
                value = "0";
            }
            double d          = new Double(value).doubleValue();
            int    percentage = (int) (d * 100);
            widget = HtmlUtils.input(urlArg, percentage + "",
                                     HtmlUtils.SIZE_5) + "%";
        } else if (isType(DATATYPE_PASSWORD)) {
            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = "" + toString(values, offset);
            }
            widget = HtmlUtils.password(urlArg, value, HtmlUtils.SIZE_10);
        } else if (isType(DATATYPE_FILE)) {
            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = "" + toString(values, offset);
            }
            widget = HtmlUtils.fileInput(urlArg, "");
        } else if (isType(DATATYPE_ENTRY)) {
            String value = "";
            if (values != null) {
                value = toString(values, offset);
            }

            Entry theEntry = null;
            if (value.length() > 0) {
                theEntry =
                    getRepository().getEntryManager().getEntry(request,
                        value);
            }
            StringBuffer sb = new StringBuffer();
            String select =
                getRepository().getHtmlOutputHandler().getSelect(request,
                    urlArg, "Select", true, null, entry);
            sb.append(HtmlUtils.hidden(urlArg + "_hidden", value,
                                       HtmlUtils.id(urlArg + "_hidden")));
            sb.append(HtmlUtils.disabledInput(urlArg, ((theEntry != null)
                    ? theEntry.getFullName()
                    : ""), HtmlUtils.id(urlArg)
                           + HtmlUtils.SIZE_60) + select);

            widget = sb.toString();
        } else {
            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = toString(values, offset);
            } else if (request.defined(urlArg)) {
                value = request.getString(urlArg);
            }
            if (searchType.equals(SEARCHTYPE_SELECT)) {
                Hashtable props =
                    getRepository().getFieldProperties(propertiesFile);
                List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
                if (props != null) {
                    for (Enumeration keys = props.keys();
                            keys.hasMoreElements(); ) {
                        String xid = (String) keys.nextElement();
                        if (xid.endsWith(".label")) {
                            xid = xid.substring(0,
                                    xid.length() - ".label".length());
                            tfos.add(new TwoFacedObject(getLabel(xid), xid));
                        }
                    }
                }

                tfos = (List<TwoFacedObject>) Misc.sort(tfos);
                if (tfos.size() == 0) {
                    widget = HtmlUtils.input(urlArg, value, " size=10 ");
                } else {
                    widget = HtmlUtils.select(urlArg, tfos, value);
                }
            } else {
                String domId = HtmlUtils.getUniqueId("input_");
                if (rows > 1) {
                    if (isType(DATATYPE_LIST)) {
                        value = StringUtil.join("\n",
                                StringUtil.split(value, ",", true, true));
                    }
                    widget = HtmlUtils.textArea(urlArg, value, rows, columns,
                            HtmlUtils.id(domId));
                } else {
                    widget = HtmlUtils.input(urlArg, value,
                                             HtmlUtils.id(domId) + " size=\""
                                             + columns + "\"");
                }
                if (size > 0) {
                    formInfo.addSizeValidation(getLabel(), domId, size);
                }
                if (required) {
                    formInfo.addRequiredValidation(getLabel(), domId);
                }
            }
        }

        return typeHandler.getFormWidget(request, entry, this, widget);
    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private List getEnumPlusValues(Request request, Entry entry)
            throws Exception {
        List<TwoFacedObject> enums = typeHandler.getEnumValues(request, this,
                                         entry);
        //TODO: Check for Strings vs TwoFacedObjects
        if (enumValues != null) {
            List tmp = new ArrayList();
            for (Object o : enums) {
                if ( !(o instanceof TwoFacedObject)) {
                    o = new TwoFacedObject(o);
                }
                //                if ( !TwoFacedObject.contains(enumValues, o)) {
                if ( !enumValues.contains(o)) {
                    tmp.add(o);
                }
            }
            tmp.addAll(enumValues);
            enums = tmp;
            //            System.err.print("TMPS: " + enums);
        }

        return enums;
    }

    /**
     * _more_
     *
     * @param values _more_
     *
     * @return _more_
     */
    public double[] getLatLonBbox(Object[] values) {
        return new double[] { (Double) values[offset],
                              (Double) values[offset + 1],
                              (Double) values[offset + 2],
                              (Double) values[offset + 3] };
    }


    /**
     * _more_
     *
     * @param values _more_
     *
     * @return _more_
     */
    public double[] getLatLon(Object[] values) {
        return new double[] { (Double) values[offset],
                              (Double) values[offset + 1] };
    }

    /**
     * _more_
     *
     * @param values _more_
     *
     * @return _more_
     */
    public boolean hasLatLon(Object[] values) {
        if ((values[offset] == null)
                || ((Double) values[offset]).doubleValue() == Entry.NONGEO) {
            return false;
        }
        if ((values[offset + 1] == null)
                || ((Double) values[offset + 1]).doubleValue()
                   == Entry.NONGEO) {
            return false;
        }

        return true;
    }

    /**
     * _more_
     *
     * @param values _more_
     *
     * @return _more_
     */
    public boolean hasLatLonBox(Object[] values) {
        for (int i = 0; i < 4; i++) {
            if ((values[offset + i] == null)
                    || ((Double) values[offset + i]).doubleValue()
                       == Entry.NONGEO) {
                return false;
            }
        }

        return true;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param values _more_
     *
     * @throws Exception _more_
     */
    public void setValue(Request request, Entry entry, Object[] values)
            throws Exception {

        if ( !addToForm) {
            //            System.err.println ("not adding to form" );
            return;
        }

        String urlArg = getEditArg();


        if (isType(DATATYPE_LATLON)) {
            if (request.exists(urlArg + "_latitude")) {
                values[offset] = new Double(request.getString(urlArg
                        + "_latitude", "0").trim());
                values[offset + 1] = new Double(request.getString(urlArg
                        + "_longitude", "0").trim());
            } else if (request.exists(urlArg + ".latitude")) {
                String latString = request.getString(urlArg + ".latitude",
                                       "0").trim();
                String lonString = request.getString(urlArg + ".longitude",
                                       "0").trim();
                double lat = Entry.NONGEO;
                double lon = Entry.NONGEO;
                if (Utils.stringDefined(latString)) {
                    lat = Misc.decodeLatLon(latString);
                }
                if (Utils.stringDefined(lonString)) {
                    lon = Misc.decodeLatLon(lonString);
                }

                values[offset]     = lat;
                values[offset + 1] = lon;
            }

        } else if (isType(DATATYPE_LATLONBBOX)) {
            if (request.exists(urlArg + "_north")) {
                values[offset] = new Double(request.get(urlArg + "_north",
                        Entry.NONGEO));
                values[offset + 1] = new Double(request.get(urlArg + "_west",
                        Entry.NONGEO));
                values[offset + 2] = new Double(request.get(urlArg
                        + "_south", Entry.NONGEO));
                values[offset + 3] = new Double(request.get(urlArg + "_east",
                        Entry.NONGEO));
            } else {
                values[offset] = new Double(request.get(urlArg + ".north",
                        Entry.NONGEO));
                values[offset + 1] = new Double(request.get(urlArg + ".west",
                        Entry.NONGEO));
                values[offset + 2] = new Double(request.get(urlArg
                        + ".south", Entry.NONGEO));
                values[offset + 3] = new Double(request.get(urlArg + ".east",
                        Entry.NONGEO));

            }
        } else if (isDate()) {
            values[offset] = request.getDate(urlArg, new Date());
        } else if (isType(DATATYPE_BOOLEAN)) {
            //Note: using the default will not work if we use checkboxes for the widget
            //For now we are using a yes/no combobox
            String value = request.getString(urlArg,
                                             (Utils.stringDefined(dflt)
                    ? dflt
                    : "true")).toLowerCase();
            //            String value = request.getString(urlArg, "false");
            values[offset] = new Boolean(value);
        } else if (isType(DATATYPE_ENUMERATION)) {
            if (request.exists(urlArg)) {
                values[offset] = request.getAnonymousEncodedString(urlArg,
                        ((dflt != null)
                         ? dflt
                         : ""));
            } else {
                values[offset] = dflt;
            }
        } else if (isType(DATATYPE_LIST)) {
            if (request.exists(urlArg)) {
                String value = request.getAnonymousEncodedString(urlArg,
                                   ((dflt != null)
                                    ? dflt
                                    : ""));
                value = StringUtil.join(",",
                                        StringUtil.split(value, "\n", true,
                                            true));
                values[offset] = value;
            } else {
                values[offset] = dflt;
            }

        } else if (isType(DATATYPE_ENUMERATIONPLUS)) {


            String theValue = "";
            if (request.defined(urlArg + "_plus")) {
                theValue = request.getAnonymousEncodedString(urlArg
                        + "_plus", ((dflt != null)
                                    ? dflt
                                    : ""));
            } else if (request.defined(urlArg)) {
                theValue = request.getAnonymousEncodedString(urlArg,
                        ((dflt != null)
                         ? dflt
                         : ""));

            } else {
                theValue = dflt;
            }
            values[offset] = theValue;
            typeHandler.addEnumValue(this, entry, theValue);
        } else if (isType(DATATYPE_INT)) {
            int dfltValue = (Utils.stringDefined(dflt)
                             ? new Integer(dflt).intValue()
                             : 0);
            if (request.exists(urlArg)) {
                values[offset] = new Integer(request.get(urlArg, dfltValue));
            } else {
                values[offset] = dfltValue;
            }
        } else if (isType(DATATYPE_PERCENTAGE)) {
            double dfltValue = (Utils.stringDefined(dflt)
                                ? new Double(dflt.trim()).doubleValue()
                                : 0);
            if (request.exists(urlArg)) {
                values[offset] = new Double(request.get(urlArg, dfltValue)
                                            / 100);
            } else {
                values[offset] = dfltValue;

            }
        } else if (isDouble()) {
            double dfltValue = (Utils.stringDefined(dflt)
                                ? new Double(dflt.trim()).doubleValue()
                                : 0);
            if (request.exists(urlArg)) {
                values[offset] = new Double(request.get(urlArg, dfltValue));
            } else {
                values[offset] = dfltValue;

            }
        } else if (isType(DATATYPE_ENTRY)) {
            values[offset] = request.getString(urlArg + "_hidden", "");
        } else {
            //string
            if (request.exists(urlArg)) {
                values[offset] = request.getAnonymousEncodedString(urlArg,
                        ((dflt != null)
                         ? dflt
                         : ""));
            } else {
                values[offset] = dflt;
            }
        }

    }



    /**
     * _more_
     *
     * @param entry _more_
     * @param values _more_
     * @param value _more_
     *
     * @throws Exception _more_
     */
    public void setValue(Entry entry, Object[] values, String value)
            throws Exception {

        if (isType(DATATYPE_LATLON)) {
            List<String> toks = StringUtil.split(value, ";", true, true);
            if (toks.size() == 2) {
                values[offset]     = new Double(toks.get(0));
                values[offset + 1] = new Double(toks.get(1));
            } else {
                //What to do here
            }
        } else if (isType(DATATYPE_LATLONBBOX)) {
            List<String> toks = StringUtil.split(value, ";", true, true);
            values[offset]     = new Double(toks.get(0));
            values[offset + 1] = new Double(toks.get(1));
            values[offset + 2] = new Double(toks.get(2));
            values[offset + 3] = new Double(toks.get(3));
        } else if (isDate()) {
            fullDateTimeFormat.setTimeZone(RepositoryBase.TIMEZONE_UTC);
            values[offset] = parseDate(value);
        } else if (isType(DATATYPE_BOOLEAN)) {
            values[offset] = new Boolean(value);
        } else if (isEnumeration()) {
            values[offset] = value;
        } else if (isType(DATATYPE_INT)) {
            values[offset] = new Integer(value);
        } else if (isType(DATATYPE_PERCENTAGE) || isDouble()) {
            values[offset] = new Double(value);
        } else if (isType(DATATYPE_ENTRY)) {
            values[offset] = value;
        } else {
            values[offset] = value;
        }
    }


    /**
     * _more_
     *
     * @param value _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Date parseDate(String value) throws Exception {
        if (dateParser != null) {
            return dateParser.parse(value);
        }

        return DateUtil.parse(value);
    }

    /**
     * _more_
     *
     * @param formBuffer _more_
     * @param request _more_
     * @param where _more_
     *
     * @throws Exception _more_
     */
    public void addToSearchForm(Request request, StringBuffer formBuffer,
                                List<Clause> where)
            throws Exception {
        addToSearchForm(request, formBuffer, where, null);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param formBuffer _more_
     * @param where _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void addToSearchForm(Request request, StringBuffer formBuffer,
                                List<Clause> where, Entry entry)
            throws Exception {

        if ( !getCanSearch()) {
            return;
        }



        String       searchArg  = getSearchArg();
        String       columnName = getFullName();

        List<Clause> tmp        = new ArrayList<Clause>(where);
        String       widget     = "";
        if (isType(DATATYPE_LATLON)) {
            String[] nwse = new String[] {
                                request.getString(searchArg + "_north", ""),
                                request.getString(searchArg + "_west", ""),
                                request.getString(searchArg + "_south", ""),
                                request.getString(searchArg + "_east", ""), };
            MapInfo map = getRepository().getMapManager().createMap(request,
                              true);
            widget = map.makeSelector(searchArg, true, nwse, "", "");
        } else if (isType(DATATYPE_LATLONBBOX)) {
            String[] nwse = new String[] {
                                request.getString(searchArg + "_north", ""),
                                request.getString(searchArg + "_west", ""),
                                request.getString(searchArg + "_south", ""),
                                request.getString(searchArg + "_east", ""), };
            MapInfo map = getRepository().getMapManager().createMap(request,
                              true);
            widget = map.makeSelector(searchArg, true, nwse, "", "");
        } else if (isDate()) {
            List dateSelect = new ArrayList();
            dateSelect.add(new TwoFacedObject(msg("Relative Date"), "none"));
            dateSelect.add(new TwoFacedObject(msg("Last hour"), "-1 hour"));
            dateSelect.add(new TwoFacedObject(msg("Last 3 hours"),
                    "-3 hours"));
            dateSelect.add(new TwoFacedObject(msg("Last 6 hours"),
                    "-6 hours"));
            dateSelect.add(new TwoFacedObject(msg("Last 12 hours"),
                    "-12 hours"));
            dateSelect.add(new TwoFacedObject(msg("Last day"), "-1 day"));
            dateSelect.add(new TwoFacedObject(msg("Last 7 days"), "-7 days"));
            String dateSelectValue;
            String relativeArg = searchArg + "_relative";
            if (request.exists(relativeArg)) {
                dateSelectValue = request.getString(relativeArg, "");
            } else {
                dateSelectValue = "none";
            }

            String dateSelectInput = HtmlUtils.select(searchArg
                                         + "_relative", dateSelect,
                                             dateSelectValue);

            widget =
                getRepository().getPageHandler().makeDateInput(
                    request, searchArg + "_fromdate", "searchform", null,
                    null, isType(DATATYPE_DATETIME)) + HtmlUtils.space(1)
                        + HtmlUtils.img(getRepository().iconUrl(ICON_RANGE))
                        + HtmlUtils.space(1)
                        + getRepository().getPageHandler().makeDateInput(
                            request, searchArg + "_todate", "searchform",
                            null, null,
                            isType(DATATYPE_DATETIME)) + HtmlUtils.space(4)
                                + msgLabel("Or") + dateSelectInput;
        } else if (isType(DATATYPE_BOOLEAN)) {
            widget =
                HtmlUtils.select(searchArg,
                                 Misc.newList(TypeHandler.ALL_OBJECT, "True",
                                     "False"), request.getString(searchArg,
                                         ""));
            //        } else if (isType(DATATYPE_ENUMERATION)) {
            //            List tmpValues = Misc.newList(TypeHandler.ALL_OBJECT);
            //            tmpValues.addAll(enumValues);
            //            widget = HtmlUtils.select(searchArg, tmpValues, request.getString(searchArg));
        } else if (isType(DATATYPE_ENUMERATIONPLUS)
                   || isType(DATATYPE_ENUMERATION)) {
            List tmpValues = Misc.newList(TypeHandler.ALL_OBJECT);
            List<TwoFacedObject> values = typeHandler.getEnumValues(request,
                                              this, entry);
            if (enumValues != null) {
                for (TwoFacedObject o : values) {
                    TwoFacedObject tfo = TwoFacedObject.findId(o.getId(),
                                             enumValues);
                    if (tfo != null) {
                        tmpValues.add(tfo);
                    } else {
                        tmpValues.add(o);
                    }
                }
            } else {
                tmpValues.addAll(values);
            }
            widget = HtmlUtils.select(searchArg, tmpValues,
                                      request.getString(searchArg),
                                      HtmlUtils.cssClass("search-select"));
        } else if (isNumeric()) {
            String expr = HtmlUtils.select(searchArg + "_expr", EXPR_ITEMS,
                                           request.getString(searchArg
                                               + "_expr", ""));
            widget =
                expr
                + HtmlUtils.input(searchArg + "_from",
                                  request.getString(searchArg + "_from", ""),
                                  "size=\"10\"") + HtmlUtils.input(searchArg
                                  + "_to", request.getString(searchArg
                                      + "_to", ""), "size=\"10\"");
        } else if (isType(DATATYPE_ENTRY)) {
            String entryId  = request.getString(searchArg + "_hidden", "");
            Entry  theEntry = null;
            if ((entryId != null) && (entryId.length() > 0)) {
                try {
                    theEntry =
                        getRepository().getEntryManager().getEntry(null,
                            entryId);
                } catch (Exception exc) {
                    throw new RuntimeException(exc);
                }
            }

            String select =
                getRepository().getHtmlOutputHandler().getSelect(request,
                    searchArg, "Select", true, null, entry);
            StringBuffer sb = new StringBuffer();
            sb.append(HtmlUtils.hidden(searchArg + "_hidden", entryId,
                                       HtmlUtils.id(searchArg + "_hidden")));
            sb.append(HtmlUtils.disabledInput(searchArg, ((theEntry != null)
                    ? theEntry.getFullName()
                    : ""), HtmlUtils.id(searchArg)
                           + HtmlUtils.SIZE_60) + select);

            widget = sb.toString();
        } else {
            if (searchType.equals(SEARCHTYPE_SELECT)) {
                long t1 = System.currentTimeMillis();
                Statement statement = typeHandler.select(request,
                                          SqlUtil.distinct(columnName), tmp,
                                          "");
                long t2 = System.currentTimeMillis();
                String[] values =
                    SqlUtil.readString(
                        typeHandler.getDatabaseManager().getIterator(
                            statement), 1);
                long t3 = System.currentTimeMillis();
                //                System.err.println("TIME:" + (t2-t1) + " " + (t3-t2));
                List<TwoFacedObject> list = new ArrayList();
                for (int i = 0; i < values.length; i++) {
                    if (values[i] == null) {
                        continue;
                    }
                    list.add(new TwoFacedObject(getLabel(values[i]),
                            values[i]));
                }

                List sorted = Misc.sort(list);
                list = new ArrayList<TwoFacedObject>();
                list.addAll(sorted);
                if (list.size() == 1) {
                    widget = HtmlUtils.hidden(searchArg,
                            (String) list.get(0).getId()) + " "
                                + list.get(0).toString();
                } else {
                    list.add(0, TypeHandler.ALL_OBJECT);
                    widget = HtmlUtils.select(searchArg, list);
                }
                //            } else if (rows > 1) {
                //                widget = HtmlUtils.textArea(searchArg, request.getString(searchArg, ""),
                //                                           rows, columns);
            } else {
                widget = HtmlUtils.input(searchArg,
                                         request.getString(searchArg, ""),
                                         HtmlUtils.SIZE_20);
            }
        }
        formBuffer.append(typeHandler.formEntry(request, getLabel() + ":",
                "<table cellspacing=0 cellpadding=0 border=0>"
                + HtmlUtils.row(HtmlUtils.cols(widget, suffix))
                + "</table>"));
        formBuffer.append("\n");
    }


    /**
     * _more_
     *
     * @param value _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected String getLabel(String value) throws Exception {
        String desc = getRepository().getFieldDescription(value + ".label",
                          propertiesFile);
        if (desc == null) {
            desc = value;
        } else {
            if (desc.indexOf("${value}") >= 0) {
                desc = desc.replace("${value}", value);
            }
        }

        return desc;

    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getFullName() {
        return typeHandler.getTableName() + "." + name;
    }


    /**
     * Set the Name property.
     *
     * @param value The new value for Name
     */
    public void setName(String value) {
        name = value;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<String> getColumnNames() {
        List<String> names = new ArrayList<String>();
        if (isType(DATATYPE_LATLON)) {
            names.add(name + "_lat");
            names.add(name + "_lon");
        } else if (isType(DATATYPE_LATLONBBOX)) {
            names.add(name + "_north");
            names.add(name + "_west");
            names.add(name + "_south");
            names.add(name + "_east");
        } else {
            names.add(name);
        }

        return names;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getSortByColumn() {
        if (isType(DATATYPE_LATLON)) {
            return name + "_lat";
        }
        if (isType(DATATYPE_LATLONBBOX)) {
            return name + "_north";
        }

        return name;
    }


    /**
     * Get the Name property.
     *
     * @return The Name
     */
    public String getName() {
        return name;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getGroup() {
        return group;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getPropertiesFile() {
        return propertiesFile;
    }

    /**
     * Set the Label property.
     *
     * @param value The new value for Label
     */
    public void setLabel(String value) {
        label = value;
    }

    /**
     * Get the Label property.
     *
     * @return The Label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the Description property.
     *
     * @param value The new value for Description
     */
    public void setDescription(String value) {
        description = value;
    }

    /**
     * Get the Description property.
     *
     * @return The Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the Type property.
     *
     * @param value The new value for Type
     */
    public void setType(String value) {
        type = value;
    }

    /**
     * Get the Type property.
     *
     * @return The Type
     */
    public String getType() {
        return type;
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public boolean isField(String name) {
        return Misc.equals(this.name, name) || Misc.equals(this.label, name);
    }

    /**
     * Set the IsIndex property.
     *
     * @param value The new value for IsIndex
     */
    public void setIsIndex(boolean value) {
        isIndex = value;
    }

    /**
     * Get the IsIndex property.
     *
     * @return The IsIndex
     */
    public boolean getIsIndex() {
        return isIndex;
    }


    /**
     * Set the IsCategory property.
     *
     * @param value The new value for IsCategory
     */
    public void setIsCategory(boolean value) {
        isCategory = value;
    }

    /**
     * Get the IsCategory property.
     *
     * @return The IsCategory
     */
    public boolean getIsCategory() {
        return isCategory;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getShowEmpty() {
        return showEmpty;
    }



    /**
     *  Set the CanShow property.
     *
     *  @param value The new value for CanShow
     */
    public void setCanShow(boolean value) {
        canShow = value;
    }

    /**
     *  Get the CanShow property.
     *
     *  @return The CanShow
     */
    public boolean getCanShow() {
        if (isType(DATATYPE_PASSWORD)) {
            return false;
        }

        return canShow;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getCanExport() {
        return canExport;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getShowLabel() {
        return showLabel;
    }


    /**
     * Set the IsSearchable property.
     *
     * @param value The new value for IsSearchable
     */
    public void setCanSearch(boolean value) {
        canSearch = value;
    }

    /**
     * Get the IsSearchable property.
     *
     * @return The IsSearchable
     */
    public boolean getCanSearch() {
        return canSearch;
    }

    /**
     * Get the IsSearchable property.
     *
     * @return The IsSearchable
     */
    public boolean getAdvancedSearch() {
        return advancedSearch;
    }




    /**
     * Set the IsListable property.
     *
     * @param value The new value for IsListable
     */
    public void setCanList(boolean value) {
        canList = value;
    }

    /**
     * Get the IsListable property.
     *
     * @return The IsListable
     */
    public boolean getCanList() {
        return canList;
    }



    /**
     * Set the Values property.
     *
     * @param value The new value for Values
     */
    public void setValues(List<TwoFacedObject> value) {
        enumValues = value;
    }

    /**
     * Get the Values property.
     *
     * @return The Values
     */
    public List<TwoFacedObject> getValues() {
        return enumValues;
    }

    /**
     * Set the Dflt property.
     *
     * @param value The new value for Dflt
     */
    public void setDflt(String value) {
        dflt = value;
    }

    /**
     * Get the Dflt property.
     *
     * @return The Dflt
     */
    public String getDflt() {
        return dflt;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return name;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getRows() {
        return rows;
    }

    /**
     *  Set the Size property.
     *
     *  @param value The new value for Size
     */
    public void setSize(int value) {
        size = value;
    }

    /**
     *  Get the Size property.
     *
     *  @return The Size
     */
    public int getSize() {
        return size;
    }


    /**
     *  Set the Editable property.
     *
     *  @param value The new value for Editable
     */
    public void setEditable(boolean value) {
        editable = value;
    }

    /**
     *  Get the Editable property.
     *
     *  @return The Editable
     */
    public boolean getEditable() {
        return editable;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getSuffix() {
        return suffix;
    }



    /**
     * _more_
     *
     * @param node _more_
     * @param attrOrTag _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getAttributeOrTag(Element node, String attrOrTag,
                                    String dflt)
            throws Exception {
        String attrValue = Utils.getAttributeOrTag(node, attrOrTag,
                               (String) null);
        if (attrValue != null) {
            properties.put(attrOrTag, attrValue);

            return attrValue;
        }

        return dflt;
    }


    /**
     * _more_
     *
     * @param node _more_
     * @param attrOrTag _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private boolean getAttributeOrTag(Element node, String attrOrTag,
                                      boolean dflt)
            throws Exception {
        String attrValue = getAttributeOrTag(node, attrOrTag, (String) null);
        if (attrValue == null) {
            return dflt;
        }

        return attrValue.equals("true");
    }


    /**
     * _more_
     *
     * @param node _more_
     * @param attrOrTag _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private int getAttributeOrTag(Element node, String attrOrTag, int dflt)
            throws Exception {
        String attrValue = getAttributeOrTag(node, attrOrTag, (String) null);
        if (attrValue == null) {
            return dflt;
        }

        return new Integer(attrValue).intValue();
    }


    /**
     * _more_
     *
     * @param node _more_
     * @param attrOrTag _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public double getAttributeOrTag(Element node, String attrOrTag,
                                    double dflt)
            throws Exception {
        String attrValue = getAttributeOrTag(node, attrOrTag, (String) null);
        if (attrValue == null) {
            return dflt;
        }

        return new Double(attrValue).doubleValue();
    }



}
