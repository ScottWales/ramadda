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

import org.ramadda.util.Station;

import ucar.unidata.util.StringUtil;

import java.io.*;

import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;




/**
 * Class description
 *
 *
 * @version        $version$, Wed, Nov 20, '13
 * @author         Enter your name here...
 */
public class MultiMonthRecord extends TextRecord {

    // 1948 -99.90 -99.90 -99.90 -99.90 -99.90 -99.90 -99.90 -99.90 -99.90 -99.90 -99.90 -99.90

    /** _more_ */
    private SimpleDateFormat sdf;

    /** _more_ */
    List<String> toks;

    /** _more_ */
    List<String> noDataLines = new ArrayList<String>();

    /** _more_ */
    int currentMonth;

    /** _more_ */
    double missingValue = 0;

    private List<RecordField> fields;

    /**
     * _more_
     *
     * @param that _more_
     */
    public MultiMonthRecord(MultiMonthRecord that) {
        super(that);
    }


    /**
     * _more_
     *
     * @param file _more_
     * @param shortName _more_
     * @param longName _more_
     * @param unit _more_
     * @param missingValue _more_
     */
    public MultiMonthRecord(MultiMonthFile file, String shortName,
                            String longName, String unit,
                            double missingValue) {
        super(file);
        this.missingValue = missingValue;
        sdf               = file.makeDateFormat("yyyy-MM");
        fields = new ArrayList<RecordField>();
        RecordField dateField = new RecordField("date", "Date", "Date", 1,
                                    "");
        dateField.setType(RecordField.TYPE_DATE);
        DataRecord.initField(dateField);

        RecordField valueField = new RecordField(shortName, longName,
                                     longName, 2, unit);
        DataRecord.initField(valueField);
        valueField.setChartable(true);
        valueField.setSearchable(true);

        fields.add(dateField);
        fields.add(valueField);
        initFields(fields);
    }


    public  List<RecordField> getFields () {
        return fields;
    }

    /**
     * _more_
     */
    @Override
    public void checkIndices() {}


    /**
     * _more_
     *
     * @param recordIO _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public ReadStatus read(RecordIO recordIO) throws IOException {
        if ((toks == null) || (currentMonth >= 12)) {
            currentMonth = 0;
            String line = readNextLine(recordIO);
            if (line == null) {
                return ReadStatus.EOF;
            }
            //            System.err.println ("LINE:" + line);
            toks = StringUtil.split(line, " ", true, true);
            //If there aren't 13 columns (year and 12 months of data) then append the line and skip
            if (toks.size() != (12 + 1)) {
                toks = null;
                noDataLines.add(line);

                return ReadStatus.SKIP;
            }
        }
        try {
            Date dttm = sdf.parse(toks.get(0) + "-" + (currentMonth + 1));
            // Matias: Changed to 0 because the method setValue needs a double I wasn't able to compile (ask jeff)
            setValue(1, dttm);
            double value = Double.parseDouble(toks.get(currentMonth + 1));
            if (value == missingValue) {
                value = Double.NaN;
            }
            setValue(2, value);
            currentMonth++;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

        return ReadStatus.OK;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean needsValidPosition() {
        return false;
    }



}
