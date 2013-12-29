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

package org.ramadda.data.point.text;


import org.ramadda.data.point.*;

import org.ramadda.data.record.*;

import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;

import java.awt.*;
import java.awt.image.*;

import java.io.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.*;


/**
 * CSV file supports any form of column delimited files - comma, tab, space, etc
 *
 */
public class CsvFile extends TextFile {

    /** column delimiter */
    private String delimiter = null;


    /**
     * ctor
     */
    public CsvFile() {}


    /**
     * ctor
     *
     *
     * @param filename _more_
     *
     * @throws IOException on badness
     */
    public CsvFile(String filename) throws IOException {
        super(filename);
    }

    /**
     * ctor
     *
     * @param filename _more_
     * @param properties _more_
     *
     * @throws IOException on badness
     */
    public CsvFile(String filename, Hashtable properties) throws IOException {
        super(filename, properties);
    }


    /**
     * _more_
     *
     * @param filename _more_
     *
     * @return _more_
     */
    public boolean canLoad(String filename) {
        String f = filename.toLowerCase();
        //A hack to not include lidar coordinate txt files
        if ((f.indexOf("coords") >= 0) || (f.indexOf("coordinates") >= 0)) {
            return false;
        }
        if (f.indexOf("target") >= 0) {
            return false;
        }

        return (f.endsWith(".csv") || f.endsWith(".txt")
                || f.endsWith(".xyz") || f.endsWith(".tsv"));
    }

    /**
     * is this file capable of certain actions - gridding, decimation, etc
     *
     * @param action action type
     *
     * @return is capable
     */
    public boolean isCapable(String action) {
        if (action.equals(ACTION_GRID)) {
            return true;
        }
        if (action.equals(ACTION_DECIMATE)) {
            return true;
        }

        return super.isCapable(action);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getDelimiter() {
        if (delimiter == null) {
            delimiter = getProperty(PROP_DELIMITER, ",");
            if (delimiter.length() == 0) {
                delimiter = " ";
            } else if (delimiter.equals("\\t")) {
                delimiter = "\t";
            } else if (delimiter.equals("tab")) {
                delimiter = "\t";
            }
        }

        return delimiter;
    }


    /**
     * _more_
     */
    public void initAfterClone() {
        super.initAfterClone();
        delimiter = null;
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public List<RecordField> doMakeFields() {
        String fieldString = getProperty(PROP_FIELDS, null);
        if (fieldString == null) {
            doQuickVisit();
            fieldString = getProperty(PROP_FIELDS, null);
        }

        if (fieldString == null) {
            setIsHeaderStandard(true);
            doQuickVisit();
            fieldString = getProperty(PROP_FIELDS, null);
        }

        if (fieldString == null) {
            throw new IllegalArgumentException("Properties must have a "
                    + PROP_FIELDS + " value");
        }

        return doMakeFields(fieldString);
    }


    /**
     * _more_
     *
     * @param fieldString _more_
     *
     * @return _more_
     */
    public List<RecordField> doMakeFields(String fieldString) {

        //x[unit="m"],y[unit="m"],z[unit="m"],red[],green[],blue[],amplitude[]
        //        System.err.println ("fields:" + fieldString);
        String defaultMissing     = getProperty("missing", (String) null);
        String[]          toks    = fieldString.split(",");
        List<RecordField> fields  = new ArrayList<RecordField>();
        int               paramId = 1;
        for (String tok : toks) {
            List<String> pair  = StringUtil.splitUpTo(tok, "[", 2);
            String       name  = pair.get(0).trim();
            String       attrs = ((pair.size() > 1)
                                  ? pair.get(1)
                                  : "").trim();
            if (attrs.startsWith("[")) {
                attrs = attrs.substring(1);
            }
            if (attrs.endsWith("]")) {
                attrs = attrs.substring(0, attrs.length() - 1);
            }
            Hashtable properties = parseAttributes(attrs);
            //            System.err.println ("props:" + properties);
            RecordField field = new RecordField(name, name, "", paramId++,
                                    getProperty(properties, "unit", ""));



            field.setIsDate(getProperty(field, properties,
                                        RecordField.PROP_ISDATE,
                                        "false").equals("true"));
            field.setIsTime(getProperty(field, properties,
                                        RecordField.PROP_ISTIME,
                                        "false").equals("true"));

            String utcoffset = getProperty(field, properties, PROP_UTCOFFSET,
                                           (String) null);
            if (utcoffset != null) {
                field.setUtcOffset(new Integer(utcoffset).intValue());
            }
            String precision = getProperty(field, properties, PROP_PRECISION,
                                           (String) null);
            if (precision != null) {
                field.setRoundingFactor(Math.pow(10,
                        Integer.parseInt(precision)));
            }

            String missing = getProperty(field, properties, "missing",
                                         defaultMissing);
            if (missing != null) {
                field.setMissingValue(Double.parseDouble(missing));
            }

            String timezone = getProperty(field, properties, "timezone",
                                          (String) null);
            String fmt = getProperty(field, properties, "fmt", (String) null);
            if (fmt == null) {
                fmt = getProperty(field, properties, PROP_FORMAT,
                                  (String) null);
            }

            if (fmt != null) {
                field.setType(field.TYPE_DATE);
                field.setDateFormat(new SimpleDateFormat(fmt));
            }

            String type = getProperty(field, properties, ATTR_TYPE,
                                      (String) null);
            if (type != null) {
                field.setType(type);
            }
            //Check for a default fixed value
            String value = getProperty(field, properties, ATTR_VALUE,
                                       (String) null);

            if (value == null) {
                String pattern = getProperty(field, properties, ATTR_PATTERN,
                                             (String) null);
                if (pattern != null) {
                    //                    System.err.println("pattern:" + pattern);
                    String header = StringUtil.join("\n", getHeaderLines());
                    String patternMatch = StringUtil.findPattern(header,
                                              pattern);
                    if (patternMatch == null) {
                        throw new IllegalArgumentException(
                            "No match. pattern=" + pattern + " field="
                            + field);
                    }

                    if (name.equalsIgnoreCase(FIELD_LATITUDE)
                            || name.equalsIgnoreCase(FIELD_LONGITUDE)) {
                        value = "" + decodeLatLon(patternMatch);
                    } else if (name.equalsIgnoreCase(FIELD_ELEVATION)) {
                        value = "" + decodeElevation(patternMatch);
                    } else {
                        value = patternMatch;
                    }
                }
            }

            if (value != null) {
                if (field.isTypeString()) {
                    field.setDefaultStringValue(value);
                } else if (field.isTypeDate()) {
                    field.setDefaultStringValue(value);
                } else {
                    field.setDefaultDoubleValue(Double.parseDouble(value));
                }
            }
            if (getProperty(field, properties, "chartable",
                            "false").equals("true")) {
                field.setChartable(true);
            }
            if (getProperty(field, properties, "skip",
                            "false").equals("true")) {
                field.setSkip(true);
            }
            if (getProperty(field, properties, "synthetic",
                            "false").equals("true")) {
                field.setSynthetic(true);
            }
            if (getProperty(field, properties, "searchable",
                            "false").equals("true")) {
                field.setSearchable(true);
            }
            if (getProperty(field, properties, "value",
                            "false").equals("true")) {
                field.setSearchable(true);
            }
            String label = getProperty(field, properties, "label",
                                       (String) null);
            if (label == null) {
                label = getProperty(field, properties, "description",
                                    (String) null);
            }
            if (label != null) {
                field.setLabel(label);
            }
            DataRecord.initField(field);
            fields.add(field);
        }

        return fields;

    }


    /**
     * _more_
     *
     * @param attrs _more_
     *
     * @return _more_
     */
    public static Hashtable parseAttributes(String attrs) {
        String    attrName              = "";
        String    attrValue             = "";
        Hashtable ht                    = new Hashtable();
        final int STATE_LOOKINGFORNAME  = 0;
        final int STATE_INNAME          = 1;
        final int STATE_LOOKINGFORVALUE = 2;
        final int STATE_INVALUE         = 2;
        int       state                 = STATE_LOOKINGFORNAME;
        attrs = attrs + " ";
        char[]  chars          = attrs.toCharArray();
        boolean gotDblQuote    = false;
        boolean gotSingleQuote = false;
        boolean gotEquals      = false;


        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (state) {

              case STATE_LOOKINGFORNAME : {
                  if ((c == ' ') || (c == '\t')) {
                      break;
                  }
                  attrName  = "" + c;
                  state     = STATE_INNAME;
                  gotEquals = false;

                  break;
              }

              case STATE_INNAME : {
                  //Are we at the end of the name?
                  if ((c == ' ') || (c == '\t') || (c == '=')) {
                      if ( !gotEquals) {
                          gotEquals = (c == '=');
                      }

                      break;
                  }
                  if ((c == '\"') || (c == '\'')) {
                      gotDblQuote    = (c == '\"');
                      gotSingleQuote = (c == '\'');
                      state          = STATE_INVALUE;

                      break;
                  }
                  if (gotEquals) {
                      attrValue += c;
                      state     = STATE_INVALUE;

                      break;
                  }

                  attrName += c;

                  break;
              }

              case STATE_INVALUE : {
                  if ((gotDblQuote && (c == '\"'))
                          || (gotSingleQuote && (c == '\''))
                          || ( !gotDblQuote && !gotSingleQuote
                               && (c == ' '))) {
                      ht.put(attrName.toLowerCase().trim(), attrValue);
                      state     = STATE_LOOKINGFORNAME;
                      attrName  = "";
                      attrValue = "";

                      break;
                  }
                  attrValue += c;

                  break;
              }
            }
        }
        if (attrName.length() > 0) {
            ht.put(attrName.toLowerCase().trim(), attrValue.trim());
        }

        return ht;
    }




    /**
     * _more_
     *
     *
     * @param visitInfo _more_
     * @return _more_
     */
    public Record doMakeRecord(VisitInfo visitInfo) {
        TextRecord record = new TextRecord(this, getFields());
        record.setFirstDataLine(firstDataLine);
        record.setDelimiter(getDelimiter());

        return record;
    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception on badness
     */
    public static void main(String[] args) throws Exception {
        if (true) {
            PointFile.test(args, CsvFile.class);

            return;
        }


        for (int argIdx = 0; argIdx < args.length; argIdx++) {
            String arg = args[argIdx];
            try {
                long                t1       = System.currentTimeMillis();
                final int[]         cnt      = { 0 };
                CsvFile             file     = new CsvFile(arg);
                final RecordVisitor metadata = new RecordVisitor() {
                    public boolean visitRecord(RecordFile file,
                            VisitInfo visitInfo, Record record) {
                        cnt[0]++;
                        PointRecord pointRecord = (PointRecord) record;
                        if ((pointRecord.getLatitude() < -90)
                                || (pointRecord.getLatitude() > 90)) {
                            System.err.println("Bad lat:"
                                    + pointRecord.getLatitude());
                        }
                        if ((cnt[0] % 100000) == 0) {
                            System.err.println(cnt[0] + " lat:"
                                    + pointRecord.getLatitude() + " "
                                    + pointRecord.getLongitude() + " "
                                    + pointRecord.getAltitude());

                        }

                        return true;
                    }
                };
                file.visit(metadata);
                long t2 = System.currentTimeMillis();
                System.err.println("time:" + (t2 - t1) / 1000.0
                                   + " # record:" + cnt[0]);
            } catch (Exception exc) {
                System.err.println("Error:" + exc);
                exc.printStackTrace();
            }
        }
    }



}
