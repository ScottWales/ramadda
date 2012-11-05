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
/*
 * Copyright 2010 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
 *
 */

package org.ramadda.data.point.text;



import org.ramadda.data.record.*;
import org.ramadda.data.point.*;

import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;

import java.text.SimpleDateFormat;
import java.awt.*;
import java.awt.image.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public class CsvFile extends TextFile {

    /** _more_          */
    public static final String PROP_FIELDS = "fields";

    /** _more_          */
    private List<RecordField> fields;

    /** _more_          */
    private String delimiter = null;


    /**
     * _more_
     */
    public CsvFile() {}


    /**
     * ctor
     *
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException _more_
     */
    public CsvFile(String filename) throws IOException {
        super(filename);
    }

    /**
     * _more_
     *
     * @param filename _more_
     * @param properties _more_
     *
     * @throws IOException _more_
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
        return (f.endsWith(".csv") || f.endsWith(".txt")
               || f.endsWith(".xyz")  || f.endsWith(".tsv"));
    }


    public String getDelimiter() {
        if(delimiter == null) {
            delimiter = getProperty("delimiter", ",");
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


    public List<RecordField> getFields() {
       if(fields == null) {
            fields = doMakeFields();
        }
        return fields;
    }

    public List<RecordField>doMakeFields() {
        String fieldString = getProperty(PROP_FIELDS, null);
        if (fieldString == null) {
            Record            record = makeRecord(new VisitInfo());
            List<RecordField> fields = record.getFields();
            System.err.println ("no fields property:" + fields);
            return fields;
        }

        if (fieldString == null) {
            throw new IllegalArgumentException("Properties must have a "
                                               + PROP_FIELDS + " value");
        }
        return    doMakeFields(fieldString);
    }


    public  List<RecordField>doMakeFields(String fieldString) {
        //x[unit="m"],y[unit="m"],z[unit="m"],red[],green[],blue[],amplitude[]
        //        System.err.println ("fields:" + fieldString);
        String[] toks = fieldString.split(",");
        List<RecordField>fields = new ArrayList<RecordField>();
        int paramId = 1;
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
            //            System.err.println("   attrs:" + attrs  + " " + properties);
            RecordField field = new RecordField(name, name, "", paramId++,
                                                getProperty(properties, "unit", ""));
            String fmt = getProperty(properties, "fmt", (String) null);
            if(fmt!=null) {
                field.setType(field.TYPE_DATE);
                System.err.println ("FORMAT:" + fmt);
                field.setDateFormat(new SimpleDateFormat(fmt));
            }

            String value = getProperty(properties,"value",(String)null);
            if(value!=null) {
                field.setDefaultDoubleValue(Double.parseDouble(value));
            }
            String type = getProperty(properties,"type",(String)null);
            if(type!=null) {
                field.setType(type);
            }
            if(getProperty(properties,"chartable","false").equals("true")) {
                field.setChartable(true);
            }
            if(getProperty(properties,"skip","false").equals("true")) {
                field.setSkip(true);
            }
            if(getProperty(properties,"searchable","false").equals("true")) {
                field.setSearchable(true);
            }
            if(getProperty(properties,"value","false").equals("true")) {
                field.setSearchable(true);
            }                
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
        String     f      = getFilename().toLowerCase();
        TextRecord record = new TextRecord(this, getFields());
        record.setDelimiter(getDelimiter());
        return record;
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(args[0]);
        System.err.println(sdf.parse(args[1]));
        if(true) return;


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
