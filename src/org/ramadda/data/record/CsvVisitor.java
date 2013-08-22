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


import org.ramadda.data.record.filter.*;
import org.ramadda.util.IsoUtil;

import java.io.*;

import java.util.Date;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.*;



/**
 * Class description
 *
 *
 * @version        $version$, Tue, Feb 14, '12
 * @author         Enter your name here...
 */
public class CsvVisitor extends RecordVisitor {

    /** _more_ */
    private PrintWriter pw;

    /** _more_ */
    private List<RecordField> fields;

    /** _more_ */
    private boolean printedHeader = false;

    /**
     * _more_
     *
     * @param pw _more_
     * @param fields _more_
     */
    public CsvVisitor(PrintWriter pw, List<RecordField> fields) {
        this.pw     = pw;
        this.fields = fields;
    }

    public static final String PROP_FIELDS = "fields";
    public static final String PROP_GENERATOR = "generator";
    public static final String PROP_CREATE_DATE = "create_date";
    public static final String PROP_CRS = "crs";
    public static final String PROP_DELIMITER = "delimiter";
    public static final String PROP_MISSING = "missing";
    public static final String PROP_SOURCE = "source";

    private static final String LINE_DELIMITER = "\n";
    private static final String COLUMN_DELIMITER = ",";
    private static final String MISSING = "NaN";

    private void comment(String comment) {
        pw.append("#");
        pw.append(comment);
        pw.append(LINE_DELIMITER);
    }

    private void property(String name, String value) {
        pw.append("#");
        pw.append(name);
        pw.append("=");
        pw.append(value);
        pw.append(LINE_DELIMITER);
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param visitInfo _more_
     * @param record _more_
     *
     * @return _more_
     */
    public boolean visitRecord(RecordFile file, VisitInfo visitInfo,
                               Record record) throws Exception {
        if (fields == null) {
            fields = record.getFields();
        }
        int cnt = 0;
        if ( !printedHeader) {
            printedHeader = true;
            cnt           = 0;
            comment("");
            pw.append("#" + PROP_FIELDS+"=");
            for (RecordField field : fields) {
                //Skip the fake ones
                if (field.getSynthetic()) {
                    continue;
                }
                //Skip the arrays
                if (field.getArity() > 1) {
                    continue;
                }
                if (cnt > 0) {
                    pw.append(COLUMN_DELIMITER);
                }
                cnt++;
                field.printCsvHeader(visitInfo, pw);
            }
            pw.append("\n");
            String source = (String) visitInfo.getProperty(PROP_SOURCE);
            if(source!=null) {
                property(PROP_SOURCE, source);
            }
            property(PROP_GENERATOR, "Ramadda http://ramadda.org");
            property(PROP_CREATE_DATE, IsoUtil.format(new Date()));
            property(PROP_DELIMITER, COLUMN_DELIMITER);
            property(PROP_MISSING, MISSING);
            comment("Here comes the data");
        }
        cnt = 0;
        for (RecordField field : fields) {

            //Skip the fake ones
            if (field.getSynthetic()) {
                continue;
            }

            //Skip the arrays
            if (field.getArity() > 1) {
                continue;
            }
            if (cnt > 0) {
                pw.append(COLUMN_DELIMITER);
            }
            cnt++;
            ValueGetter getter = field.getValueGetter();
            if (getter == null) {
                if(field.isTypeString()) {
                    String svalue = record.getStringValue(field.getParamId());
                    pw.append(svalue);
                } else {
                    double value = record.getValue(field.getParamId());
                    pw.append("" + value);
                }
            } else {
                pw.append(getter.getStringValue(record, field, visitInfo));
            }
        }
        pw.append("\n");
        return true;
    }

}
