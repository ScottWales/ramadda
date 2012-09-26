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

import org.ramadda.repository.map.*;
import org.ramadda.repository.output.OutputType;


import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.data.gis.KmlUtil;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
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
    public static final String OUTPUT_HTML = "html";

    /** _more_ */
    public static final String OUTPUT_CSV = "csv";

    /** _more_ */
    private static SimpleDateFormat dateTimeFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /** _more_ */
    private static SimpleDateFormat fullDateTimeFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    /** _more_ */
    private static SimpleDateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd");

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
    public static final String ATTR_CHANGETYPE = "changetype";

    /** _more_ */
    public static final String ATTR_ADDTOFORM = "addtoform";

    /** _more_ */
    public static final String ATTR_GROUP = "group";

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
    public static final String ATTR_CANSEARCH = "cansearch";

    /** _more_ */
    public static final String ATTR_CANLIST = "canlist";

    /** _more_ */
    public static final String ATTR_VALUES = "values";

    /** _more_ */
    public static final String ATTR_DEFAULT = "default";

    /** _more_ */
    public static final String ATTR_SIZE = "size";

    /** _more_ */
    public static final String ATTR_ROWS = "rows";

    /** _more_ */
    public static final String ATTR_COLUMNS = "columns";

    /** _more_ */
    public static final String ATTR_SEARCHTYPE = "searchtype";

    /** _more_ */
    public static final String ATTR_SHOWINHTML = "showinhtml";


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
    private String suffix;

    /** _more_ */
    private String searchType = SEARCHTYPE_TEXT;

    /** _more_ */
    private boolean isIndex;

    /** _more_ */
    private boolean canSearch;

    /** _more_ */
    private boolean canList;

    /** _more_ */
    private List enumValues;

    /** _more_ */
    private Hashtable<String, String> enumMap = new Hashtable<String,
                                                    String>();



    /** _more_ */
    private String dflt;

    /** _more_ */
    private int size = 200;

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
        oldNames         = StringUtil.split(XmlUtil.getAttribute(element,
                ATTR_OLDNAMES, ""), ",", true, true);
        suffix     = XmlUtil.getAttribute(element, ATTR_SUFFIX, "");
        label      = XmlUtil.getAttribute(element, ATTR_LABEL, name);
        searchType = XmlUtil.getAttribute(element, ATTR_SEARCHTYPE,
                                          searchType);
        propertiesFile = XmlUtil.getAttribute(element, ATTR_PROPERTIES,
                (String) null);

        description = XmlUtil.getAttribute(element, ATTR_DESCRIPTION, label);
        type = XmlUtil.getAttribute(element, ATTR_TYPE, DATATYPE_STRING);
        changeType  = XmlUtil.getAttribute(element, ATTR_CHANGETYPE, false);
        dflt        = XmlUtil.getAttribute(element, ATTR_DEFAULT, "").trim();
        isIndex     = XmlUtil.getAttribute(element, ATTR_ISINDEX, false);
        canSearch   = XmlUtil.getAttribute(element, ATTR_CANSEARCH, false);
        addToForm   = XmlUtil.getAttribute(element, ATTR_ADDTOFORM, addToForm);
        canShow     = XmlUtil.getAttribute(element, ATTR_SHOWINHTML, canShow);
        canList     = XmlUtil.getAttribute(element, ATTR_CANLIST, true);
        size        = XmlUtil.getAttribute(element, ATTR_SIZE, size);
        rows        = XmlUtil.getAttribute(element, ATTR_ROWS, rows);
        columns     = XmlUtil.getAttribute(element, ATTR_COLUMNS, columns);

        List propNodes = XmlUtil.findChildren(element, "property");
        for (int i = 0; i < propNodes.size(); i++) {
            Element propNode = (Element) propNodes.get(i);
            properties.put(XmlUtil.getAttribute(propNode, "name"),
                           XmlUtil.getAttribute(propNode, "value"));
        }

        if (isEnumeration()) {
            String valueString = XmlUtil.getAttribute(element, ATTR_VALUES,
                                     (String) null);
            if (valueString != null) {
                if (valueString.startsWith("file:")) {
                    valueString =
                        typeHandler.getStorageManager().readSystemResource(
                            valueString.substring("file:".length()));
                    List<String> tmp = StringUtil.split(valueString, "\n",
                                           true, true);
                    enumValues = new ArrayList();
                    for (String tok : tmp) {
                        if (tok.startsWith("#")) {
                            continue;
                        }
                        if (tok.indexOf(":") >= 0) {
                            List<String> toks = StringUtil.splitUpTo(tok,
                                                    ":", 2);

                            enumValues.add(new TwoFacedObject(toks.get(1),
                                    toks.get(0)));
                            enumMap.put(toks.get(0), toks.get(1));
                        } else {
                            enumValues.add(new TwoFacedObject(tok,tok));
                        }
                    }

                } else {
                    enumValues = new ArrayList();
                    for(String tok:StringUtil.split(valueString, ",", true,
                                                    true)) {
                        enumValues.add(new TwoFacedObject(tok,tok));
                    }
                }
            }
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
        return isType(DATATYPE_STRING) || isType(DATATYPE_ENUMERATION)
               || isType(DATATYPE_ENUMERATIONPLUS) || isType(DATATYPE_ENTRY)
               || isType(DATATYPE_EMAIL) || isType(DATATYPE_URL);
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
            if (StringUtil.notEmpty(dflt)) {
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

        String delimiter = (Misc.equals(OUTPUT_CSV, output)
                            ? "|"
                            : ",");
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
            if (Misc.equals(output, OUTPUT_CSV)) {
                sb.append(toString(values, offset));
            } else {
                //                System.err.println("offset:" + offset +" values:");
                //                Misc.printArray("", values);
                double percent = (Double) values[offset];
                sb.append((int) (percent * 100) + "");
            }
        } else if (isType(DATATYPE_DATETIME)) {
            sb.append(dateTimeFormat.format((Date) values[offset]));
        } else if (isType(DATATYPE_DATE)) {
            sb.append(dateFormat.format((Date) values[offset]));
        } else if (isType(DATATYPE_ENTRY)) {
            String entryId  = toString(values, offset);
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
            if (Misc.equals(output, OUTPUT_CSV)) {
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
            if (Misc.equals(output, OUTPUT_CSV)) {
                sb.append(s);
            } else {
                sb.append("<a href=\"mailto:" + s + "\">" + s + "</a>");
            }
        } else if (isType(DATATYPE_URL)) {
            String s = toString(values, offset);
            if (Misc.equals(output, OUTPUT_CSV)) {
                sb.append(s);
            } else {
                sb.append("<a href=\"" + s + "\">" + s + "</a>");
            }
        } else {
            String s = toString(values, offset);
            if (rows > 1) {
                s = getRepository().getWikiManager().wikifyEntry(
                    getRepository().getTmpRequest(), entry, s, false, null,
                    null);
            } else if (isType(DATATYPE_ENUMERATION)
                       || isType(DATATYPE_ENUMERATIONPLUS)) {
                String label = enumMap.get(s);
                if (label != null) {
                    s = label;
                }
            }
            sb.append(s);
        }
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
                statement.setInt(statementIdx, 0);
            }
            statementIdx++;
        } else if (isDouble()) {
            if (values[offset] != null) {
                statement.setDouble(statementIdx,
                                    ((Double) values[offset]).doubleValue());
            } else {
                statement.setDouble(statementIdx, 0.0);
            }
            statementIdx++;
        } else if (isType(DATATYPE_BOOLEAN)) {
            if (values[offset] != null) {
                boolean v = ((Boolean) values[offset]).booleanValue();
                statement.setInt(statementIdx, (v
                        ? 1
                        : 0));
            } else {
                statement.setInt(statementIdx, 0);
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
                statement.setDouble(statementIdx, lat);
                double lon = ((Double) values[offset + 1]).doubleValue();
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
                statement.setString(statementIdx, toString(values, offset));
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
            if (typeHandler.getDatabaseManager().isDatabaseDerby()) {
                sql = "alter table " + getTableName() + "  alter column "
                      + name + "  set data type " + type + ";";
            } else {
                sql = "alter table " + getTableName() + " modify column "
                      + name + " " + type + ";";
            }
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
        } else if (isType(DATATYPE_CLOB)) {
            String clobType =
                getRepository().getDatabaseManager().convertType("clob",
                    size);
            defineColumn(statement, name, clobType);
        } else if (isType(DATATYPE_ENUMERATION)
                   || isType(DATATYPE_ENUMERATIONPLUS)) {
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

    public void addGeoExclusion(List<Clause> clauses) {
        if (isType(DATATYPE_LATLON)) {
            String id = getFullName();
            clauses.add(Clause.neq(id + "_lat",Entry.NONGEO));
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

        String id = getFullName();
        if (isType(DATATYPE_LATLON)) {
            double north = request.get(id + "_north", Double.NaN);
            double south = request.get(id + "_south", Double.NaN);
            double east  = request.get(id + "_east", Double.NaN);
            double west  = request.get(id + "_west", Double.NaN);
            if (latLonOk(north)) {
                where.add(Clause.le(id + "_lat", north));
            }
            if (latLonOk(south)) {
                where.add(Clause.ge(id + "_lat", south));
            }
            if (latLonOk(west)) {
                where.add(Clause.ge(id + "_lon", west));
            }
            if (latLonOk(east)) {
                where.add(Clause.le(id + "_lon", east));
            }
        } else if (isType(DATATYPE_LATLONBBOX)) {
            double north = request.get(id + "_north", Double.NaN);
            double south = request.get(id + "_south", Double.NaN);
            double east  = request.get(id + "_east", Double.NaN);
            double west  = request.get(id + "_west", Double.NaN);

            if (latLonOk(north)) {
                where.add(Clause.le(id + "_north", north));
            }
            if (latLonOk(south)) {
                where.add(Clause.ge(id + "_south", south));
            }
            if (latLonOk(west)) {
                where.add(Clause.ge(id + "_west", west));
            }
            if (latLonOk(east)) {
                where.add(Clause.le(id + "_east", east));
            }
        } else if (isNumeric()) {
            String expr = request.getCheckedString(id + "_expr", EXPR_EQUALS,
                              EXPR_PATTERN);
            double from  = request.get(id + "_from", Double.NaN);
            double to    = request.get(id + "_to", Double.NaN);
            double value = request.get(id, Double.NaN);

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
            if (request.defined(id)) {
                where.add(Clause.eq(getFullName(), (request.get(id, true)
                        ? 1
                        : 0)));
            }
        } else if (isDate()) {
            String relativeArg = id + "_relative";
            Date[] dateRange   = request.getDateRange(id + "_fromdate",
                                   id + "_todate", relativeArg, new Date());
            if (dateRange[0] != null) {
                where.add(Clause.ge(getFullName(), dateRange[0]));
            }

            if (dateRange[1] != null) {
                where.add(Clause.le(getFullName(), dateRange[1]));
            }
        } else if (isType(DATATYPE_ENTRY)) {
            String value = request.getString(id + "_hidden", "");
            if (value.length() > 0) {
                where.add(Clause.eq(getFullName(), value));
            }
        } else if (isEnumeration()) {
            String value = request.getString(id, null);
            if ((value != null) && (value.length() > 0)) {
                where.add(Clause.eq(getFullName(), value));
            }
        } else {
            String value = request.getString(id, null);
            if (value != null) {
                where.add(Clause.like(getFullName(), "%" + value + "%"));
            }
            //            typeHandler.addOrClause(getFullName(),
            //                                    value, where);
        }


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
     *
     * @throws Exception _more_
     */
    public void addToEntryForm(Request request, Entry entry,
                               StringBuffer formBuffer, Object[] values,
                               Hashtable state)
            throws Exception {
        if ( !addToForm) {
            return;
        }
        String widget = getFormWidget(request, entry, values);
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


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param values _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getFormWidget(Request request, Entry entry, Object[] values)
            throws Exception {

        String widget = "";
        String id     = getFullName();
        if (isType(DATATYPE_LATLON)) {
            double lat = 0;
            double lon = 0;
            if (values != null) {
                lat = ((Double) values[offset]).doubleValue();
                lon = ((Double) values[offset + 1]).doubleValue();
            }
            MapInfo map = getRepository().getMapManager().createMap(request,
                              true);
            widget = map.makeSelector(id, true, new String[] { latLonOk(lat)
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
            widget = map.makeSelector(id, true, nwse, "", "");
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
            //            widget = HtmlUtils.checkbox(id, "true", value);
            List<TwoFacedObject> items = new ArrayList<TwoFacedObject>();
            items.add(new TwoFacedObject("Yes", "true"));
            items.add(new TwoFacedObject("No", "false"));
            widget = HtmlUtils.select(id, items, value
                    ? "true"
                    : "false");
        } else if (isType(DATATYPE_DATETIME)) {
            Date date;
            if (values != null) {
                date = (Date) values[offset];
            } else {
                date = new Date();
            }
            widget = getRepository().makeDateInput(request, id, "", date,
                    null);
        } else if (isType(DATATYPE_DATE)) {
            Date date;
            if (values != null) {
                date = (Date) values[offset];
            } else {
                date = new Date();
            }
            widget = getRepository().makeDateInput(request, id, "", date,
                    null, false);
        } else if (isType(DATATYPE_ENUMERATION)) {
            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = (String) toString(values, offset);
            }
            widget = HtmlUtils.select(id, enumValues, value);
        } else if (isType(DATATYPE_ENUMERATIONPLUS)) {
            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = (String) toString(values, offset);
            }
            List enums = getEnumPlusValues(request, entry);
            widget = HtmlUtils.select(id, enums, value) + "  or:  "
                     + HtmlUtils.input(id + "_plus", "", HtmlUtils.SIZE_20);
        } else if (isType(DATATYPE_INT)) {
            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = "" + toString(values, offset);
            }
            widget = HtmlUtils.input(id, value, HtmlUtils.SIZE_10);
        } else if (isType(DATATYPE_DOUBLE)) {
            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = "" + toString(values, offset);
            }
            widget = HtmlUtils.input(id, value, HtmlUtils.SIZE_10);
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
            widget = HtmlUtils.input(id, percentage + "", HtmlUtils.SIZE_5)
                     + "%";
        } else if (isType(DATATYPE_PASSWORD)) {
            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = "" + toString(values, offset);
            }
            widget = HtmlUtils.password(id, value, HtmlUtils.SIZE_10);
        } else if (isType(DATATYPE_FILE)) {
            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = "" + toString(values, offset);
            }
            widget = HtmlUtils.fileInput(id, "");
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
            StringBuffer sb     = new StringBuffer();
            String       select =
                getRepository().getHtmlOutputHandler().getSelect(request, id,
                    "Select", true, null, entry);
            sb.append(HtmlUtils.hidden(id + "_hidden", value,
                                       HtmlUtils.id(id + "_hidden")));
            sb.append(HtmlUtils.disabledInput(id, ((theEntry != null)
                    ? theEntry.getFullName()
                    : ""), HtmlUtils.id(id) + HtmlUtils.SIZE_60) + select);

            widget = sb.toString();
        } else {
            String value = ((dflt != null)
                            ? dflt
                            : "");
            if (values != null) {
                value = toString(values, offset);
            } else if (request.defined(id)) {
                value = request.getString(id);
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
                    widget = HtmlUtils.input(id, value, " size=10 ");
                } else {

                    widget = HtmlUtils.select(id, tfos, value);
                }
            } else if (rows > 1) {
                widget = HtmlUtils.textArea(id, value, rows, columns);
            } else {
                widget = HtmlUtils.input(id, value,
                                         "size=\"" + columns + "\"");
            }
        }

        return HtmlUtils.hbox(widget, HtmlUtils.inset(suffix, 5));

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
        List enums = typeHandler.getEnumValues(request, this, entry);
        //        System.err.print("ENUMS: " + enums);
        //TODO: Check for Strings vs TwoFacedObjects
        if (enumValues != null) {
            List tmp = new ArrayList();
            for (Object o : enums) {
                if ( !TwoFacedObject.contains(enumValues, o)) {
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

    public boolean hasLatLon(Object[] values) {
        if(values[offset]==null || ((Double)values[offset]).doubleValue() == Entry.NONGEO) return false;
        if(values[offset+1]==null || ((Double)values[offset+1]).doubleValue() == Entry.NONGEO) return false;
        return true;
    }
    public boolean hasLatLonBox(Object[] values) {
        for(int i=0;i<4;i++) {
            if(values[offset+i]==null || ((Double)values[offset+i]).doubleValue() == Entry.NONGEO) return false;
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
            return;
        }

        String id = getFullName();

        if (isType(DATATYPE_LATLON)) {
            if (request.exists(id + "_latitude")) {
                values[offset] = new Double(request.getString(id
                        + "_latitude", "0").trim());
                values[offset + 1] = new Double(request.getString(id
                        + "_longitude", "0").trim());
            } else if (request.exists(id + ".latitude")) {
                values[offset] = new Double(request.getString(id
                        + ".latitude", "0").trim());
                values[offset + 1] = new Double(request.getString(id
                        + ".longitude", "0").trim());
            }

        } else if (isType(DATATYPE_LATLONBBOX)) {
            if (request.exists(id + "_north")) {
                values[offset] = new Double(request.get(id + "_north",
                        Entry.NONGEO));
                values[offset + 1] = new Double(request.get(id + "_west",
                        Entry.NONGEO));
                values[offset + 2] = new Double(request.get(id + "_south",
                        Entry.NONGEO));
                values[offset + 3] = new Double(request.get(id + "_east",
                        Entry.NONGEO));
            } else {
                values[offset] = new Double(request.get(id + ".north",
                        Entry.NONGEO));
                values[offset + 1] = new Double(request.get(id + ".west",
                        Entry.NONGEO));
                values[offset + 2] = new Double(request.get(id + ".south",
                        Entry.NONGEO));
                values[offset + 3] = new Double(request.get(id + ".east",
                        Entry.NONGEO));

            }
        } else if (isDate()) {
            values[offset] = request.getDate(id, new Date());
        } else if (isType(DATATYPE_BOOLEAN)) {
            //Note: using the default will not work if we use checkboxes for the widget
            //For now we are using a yes/no combobox
            String value = request.getString(id, (StringUtil.notEmpty(dflt)
                    ? dflt
                    : "true")).toLowerCase();
            //            String value = request.getString(id, "false");
            values[offset] = new Boolean(value);
        } else if (isType(DATATYPE_ENUMERATION)) {
            if (request.exists(id)) {
                values[offset] = request.getAnonymousEncodedString(id,
                        ((dflt != null)
                         ? dflt
                         : ""));
            } else {
                values[offset] = dflt;
            }
        } else if (isType(DATATYPE_ENUMERATIONPLUS)) {
            String theValue = "";
            if (request.defined(id + "_plus")) {
                theValue = request.getAnonymousEncodedString(id + "_plus",
                        ((dflt != null)
                         ? dflt
                         : ""));
            } else if (request.defined(id)) {
                theValue = request.getAnonymousEncodedString(id,
                        ((dflt != null)
                         ? dflt
                         : ""));

            } else {
                theValue = dflt;
            }
            values[offset] = theValue;
            typeHandler.addEnumValue(this, entry, theValue);
        } else if (isType(DATATYPE_INT)) {
            int dfltValue = (StringUtil.notEmpty(dflt)
                             ? new Integer(dflt).intValue()
                             : 0);
            if (request.exists(id)) {
                values[offset] = new Integer(request.get(id, dfltValue));
            } else {
                values[offset] = dfltValue;
            }
        } else if (isType(DATATYPE_PERCENTAGE)) {
            double dfltValue = (StringUtil.notEmpty(dflt)
                                ? new Double(dflt.trim()).doubleValue()
                                : 0);
            if (request.exists(id)) {
                values[offset] = new Double(request.get(id, dfltValue) / 100);
            } else {
                values[offset] = dfltValue;

            }
        } else if (isDouble()) {
            double dfltValue = (StringUtil.notEmpty(dflt)
                                ? new Double(dflt.trim()).doubleValue()
                                : 0);
            if (request.exists(id)) {
                values[offset] = new Double(request.get(id, dfltValue));
            } else {
                values[offset] = dfltValue;

            }
        } else if (isType(DATATYPE_ENTRY)) {
            values[offset] = request.getString(id + "_hidden", "");
        } else {
            if (request.exists(id)) {
                values[offset] = request.getAnonymousEncodedString(id,
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
            if(toks.size()==2) {
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
            values[offset] = fullDateTimeFormat.parse(value);
        } else if (isType(DATATYPE_BOOLEAN)) {
            values[offset] = new Boolean(value);
        } else if (isType(DATATYPE_ENUMERATION)
                   || isType(DATATYPE_ENUMERATIONPLUS)) {
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

        String       id     = getFullName();

        List<Clause> tmp    = new ArrayList<Clause>(where);
        String       widget = "";
        if (isType(DATATYPE_LATLON)) {
            //TODO: Use point selector
            MapInfo map = getRepository().getMapManager().createMap(request,
                              true);
            widget = map.makeSelector(id, true, null, "", "");
        } else if (isType(DATATYPE_LATLONBBOX)) {
            MapInfo map = getRepository().getMapManager().createMap(request,
                              true);
            widget = map.makeSelector(id, true, null, "", "");
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
            String relativeArg = id + "_relative";
            if (request.exists(relativeArg)) {
                dateSelectValue = request.getString(relativeArg, "");
            } else {
                dateSelectValue = "none";
            }

            String dateSelectInput = HtmlUtils.select(id + "_relative",
                                         dateSelect, dateSelectValue);

            widget = getRepository().makeDateInput(request, id + "_fromdate",
                    "searchform", null, null,
                    isType(DATATYPE_DATETIME)) + HtmlUtils.space(1)
                        + HtmlUtils.img(getRepository().iconUrl(ICON_RANGE))
                        + HtmlUtils.space(1)
                        + getRepository().makeDateInput(request,
                            id + "_todate", "searchform", null, null,
                            isType(DATATYPE_DATETIME)) + HtmlUtils.space(4)
                                + msgLabel("Or") + dateSelectInput;


        } else if (isType(DATATYPE_BOOLEAN)) {
            widget = HtmlUtils.select(id,
                                      Misc.newList(TypeHandler.ALL_OBJECT,
                                          "True",
                                          "False"), request.getString(id,
                                              ""));
            //        } else if (isType(DATATYPE_ENUMERATION)) {
            //            List tmpValues = Misc.newList(TypeHandler.ALL_OBJECT);
            //            tmpValues.addAll(enumValues);
            //            widget = HtmlUtils.select(id, tmpValues, request.getString(id));
        } else if (isType(DATATYPE_ENUMERATIONPLUS)
                   || isType(DATATYPE_ENUMERATION)) {
            List tmpValues   = Misc.newList(TypeHandler.ALL_OBJECT);
            List values      = typeHandler.getEnumValues(request, this, entry);
            List valuesToUse = new ArrayList();
            if (enumValues != null) {
                for (Object value : values) {
                    TwoFacedObject tfo = TwoFacedObject.findId(value,
                                             enumValues);
                    if (tfo != null) {
                        valuesToUse.add(tfo);
                    } else {
                        valuesToUse.add(value);
                    }

                }
            } else {
                valuesToUse = values;
            }
            tmpValues.addAll(valuesToUse);
            widget = HtmlUtils.select(id, tmpValues, request.getString(id));
        } else if (isNumeric()) {
            String expr = HtmlUtils.select(id + "_expr", EXPR_ITEMS,
                                           request.getString(id + "_expr",
                                               ""));
            widget = expr
                     + HtmlUtils.input(id + "_from",
                                       request.getString(id + "_from", ""),
                                       "size=\"10\"") + HtmlUtils.input(id
                                       + "_to", request.getString(id + "_to",
                                           ""), "size=\"10\"");
        } else if (isType(DATATYPE_ENTRY)) {


            String entryId  = request.getString(id + "_hidden", "");
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
                getRepository().getHtmlOutputHandler().getSelect(request, id,
                    "Select", true, null, entry);
            StringBuffer sb = new StringBuffer();
            sb.append(HtmlUtils.hidden(id + "_hidden", entryId,
                                       HtmlUtils.id(id + "_hidden")));
            sb.append(HtmlUtils.disabledInput(id, ((theEntry != null)
                    ? theEntry.getFullName()
                    : ""), HtmlUtils.id(id) + HtmlUtils.SIZE_60) + select);

            widget = sb.toString();
        } else {
            if (searchType.equals(SEARCHTYPE_SELECT)) {
                long      t1        = System.currentTimeMillis();
                Statement statement = typeHandler.select(request,
                                          SqlUtil.distinct(id), tmp, "");
                long     t2     = System.currentTimeMillis();
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
                    widget = HtmlUtils.hidden(id,
                            (String) list.get(0).getId()) + " "
                                + list.get(0).toString();
                } else {
                    list.add(0, TypeHandler.ALL_OBJECT);
                    widget = HtmlUtils.select(id, list);
                }
                //            } else if (rows > 1) {
                //                widget = HtmlUtils.textArea(id, request.getString(id, ""),
                //                                           rows, columns);
            } else {
                widget = HtmlUtils.input(id, request.getString(id, ""),
                                         "size=\"" + columns + "\"");
            }
        }
        formBuffer.append(typeHandler.formEntry(request, getLabel() + ":",
                "<table>" + HtmlUtils.row(HtmlUtils.cols(widget, suffix))
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
    public void setValues(List value) {
        enumValues = value;
    }

    /**
     * Get the Values property.
     *
     * @return The Values
     */
    public List getValues() {
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
        return name + ":" + offset;
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




}
