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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;




/** This is generated code from generate.tcl. Do not edit it! */
public class TextRecord extends DataRecord {

    /** _more_ */
    public static final int ATTR_FIRST =
        org.ramadda.data.point.PointRecord.ATTR_LAST;

    /** _more_ */
    private String delimiter = ",";

    /** _more_          */
    private boolean delimiterIsSpace = false;

    /** _more_          */
    protected String firstDataLine = null;


    /** _more_          */
    private String[] tokens;

    /** _more_          */
    private String line = "";

    /** _more_          */
    private boolean bePickyAboutTokens = true;


    /** _more_          */
    private int badCnt = 0;

    /** _more_          */
    private int goodCnt = 0;

    /**
     * _more_
     *
     * @param that _more_
     */
    public TextRecord(TextRecord that) {
        super(that);
        tokens       = null;
    }


    /**
     * _more_
     *
     * @param file _more_
     * @param fields _more_
     */
    public TextRecord(RecordFile file, List<RecordField> fields) {
        super(file, fields);
        initFields(fields);

    }

    @Override
    public void initFields(List<RecordField> fields) {
        super.initFields(fields);
        tokens = new String[this.fields.size()];
    }

    /**
     * _more_
     *
     * @param file _more_
     */
    public TextRecord(RecordFile file) {
        super(file);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getLine() {
        return line;
    }

    /**
     *  Set the Delimiter property.
     *
     *  @param value The new value for Delimiter
     */
    public void setDelimiter(String value) {
        delimiter = value;
        if ( !delimiter.equals("\t") && (delimiter.trim().length() == 0)) {
            delimiterIsSpace = true;
        } else {
            delimiterIsSpace = false;
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getFileDelimiter() {
        return delimiter;
    }


    /**
     *  Get the Delimiter property.
     *
     *  @return The Delimiter
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public boolean hasRecordTime() {
        if (super.hasRecordTime()) {
            return true;
        }

        return idxTime >= 0;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public long getRecordTime() {
        if (idxTime >= 0) {
            return ((Date) objectValues[idxTime]).getTime();
        }
        return super.getRecordTime();
    }


    /**
     * _more_
     *
     * @param attrId _more_
     *
     * @return _more_
     */
    public double getValue(int attrId) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx = idx - 1;
        if ((idx >= 0) && (idx < values.length)) {
            return values[idx];
        }

        return super.getValue(attrId);
    }


    /**
     * _more_
     *
     * @param attrId _more_
     * @param value _more_
     */
    public void setValue(int attrId, double value) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx         = idx - 1;
        values[idx] = value;
    }


    /**
     * _more_
     *
     * @param attrId _more_
     *
     * @return _more_
     */
    @Override
    public String getStringValue(int attrId) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx = idx - 1;
        if ((idx >= 0) && (idx < values.length)) {
            //Maybe just a number
            if (objectValues[idx] == null) {
                return "" + values[idx];
            }

            return objectValues[idx].toString();
        }

        return super.getStringValue(attrId);
    }


    /**
     * _more_
     *
     * @param attrId _more_
     *
     * @return _more_
     */
    @Override
    public Object getObjectValue(int attrId) {
        int idx = attrId - ATTR_FIRST;
        //Offset since the  field ids are 1 based not 0 based
        idx = idx - 1;
        if ((idx >= 0) && (idx < values.length)) {
            return objectValues[idx];
        }

        return super.getObjectValue(attrId);
    }





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

        try {
            while (true) {
                if (firstDataLine != null) {
                    line          = firstDataLine;
                    firstDataLine = null;
                } else {
                    line = recordIO.readLine();
                }
                if (line == null) {
                    return ReadStatus.EOF;
                }
                line = line.trim();
                if ( !lineOk(line)) {
                    continue;
                }

                break;
            }

            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = "";
            }
            if ( !split(line)) {
                //throw new IllegalArgumentException("Could not tokenize line:" + line);
                return ReadStatus.SKIP;
            }
            int tokenCnt = 0;
            for (int fieldCnt = 0; fieldCnt < fields.size(); fieldCnt++) {
                RecordField field = fields.get(fieldCnt);
                if (skip[fieldCnt]) {
                    continue;
                }
                if (hasDefault[fieldCnt]) {
                    if (field.isTypeString()) {
                        objectValues[fieldCnt] =
                            field.getDefaultStringValue();
                    } else if (field.isTypeDate()) {
                        String dttm = field.getDefaultStringValue();
                        objectValues[fieldCnt] =
                            field.getDateFormat().parse(dttm);
                    } else {
                        values[fieldCnt] = field.getDefaultDoubleValue();
                    }

                    continue;
                }

                //                System.err.println ("field:" + field +" " + tok);
                if (synthetic[fieldCnt]) {
                    continue;
                }


                String tok = tokens[tokenCnt++];
                if (field.isTypeString()) {
                    objectValues[fieldCnt] = tok;

                    continue;
                }
                if (field.isTypeDate()) {
                    tok = tok.replaceAll("\"", "");
                    try {
                        objectValues[fieldCnt] =
                            field.getDateFormat().parse(tok);
                    } catch (java.text.ParseException ignore) {
                        objectValues[fieldCnt] =
                            field.getDateFormat().parse(tok + " UTC");
                    }

                    continue;
                }
                if (tok == null) {
                    System.err.println("tok null: " + tokenCnt + " " + line);
                }
                //Check for the riscan NaN
                if (isMissingValue(field, tok)) {
                    values[fieldCnt] = Double.NaN;
                } else {
                    values[fieldCnt] = (double) Double.parseDouble(tok);
                    if (isMissingValue(field, values[fieldCnt])) {
                        values[fieldCnt] = Double.NaN;
                    }

                }
            }

            setLocation(values[idxX], values[idxY], ((idxZ >= 0)
                    ? values[idxZ]
                    : 0));
            convertedXYZToLatLonAlt = true;

            if (idxTime >= 0) {
                setRecordTime(getRecordTime());
            }

            return ReadStatus.OK;
        } catch (Exception exc) {
            System.err.println("Line:" + line);

            throw new RuntimeException(exc);
        }


    }


    /**
     * _more_
     *
     * @param station _more_
     */
    public void setLocation(Station station) {
        this.setLocation(station.getLongitude(), station.getLatitude(),
                         station.getElevation());
        if (idxX >= 0) {
            this.setValue(idxX, station.getLongitude());
        }
        if (idxY >= 0) {
            this.setValue(idxY, station.getLatitude());
        }
        if (idxZ >= 0) {
            this.setValue(idxZ, station.getElevation());
        }

    }



    /**
     * _more_
     *
     * @param line _more_
     *
     * @return _more_
     */
    public boolean lineOk(String line) {
        if ((line.length() == 0) || line.startsWith("#")) {
            return false;
        }

        return true;
    }



    /**
     * _more_
     *
     * @param sourceString _more_
     *
     * @return _more_
     */
    public boolean split(String sourceString) {
        int length        = 1;
        int fullTokenCnt  = 0;
        int numTokensRead = 0;
        int fromIndex     = 0;
        int sourceLength  = sourceString.length();
        //        System.err.println ("line:" + sourceString);
        while (true) {
            int idx = sourceString.indexOf(delimiter, fromIndex);
            //            System.err.println ("\tidx:" + idx +" delimiter:" + delimiter +":  str:" + sourceString);
            String theString;
            if (idx < 0) {
                theString = sourceString.substring(fromIndex);
            } else {
                theString = sourceString.substring(fromIndex, idx);
                if (delimiterIsSpace) {
                    while ((sourceString.charAt(idx) == ' ')
                            && (idx < sourceLength)) {
                        idx++;
                    }
                    fromIndex = idx;
                } else {
                    fromIndex = idx + length;
                }
            }
            //            System.err.println ("\ttokens[" + numTokensRead +"] = " + theString);
            tokens[numTokensRead++] = theString.trim();
            if ((idx < 0) || (numTokensRead == tokens.length)) {
                break;
            }
        }
        if (bePickyAboutTokens && (numTokensRead != tokens.length)) {
            System.err.println("bad token cnt: expected:" + tokens.length
                               + " read:" + numTokensRead + " delimiter:"
                               + delimiter + " is space:" + delimiterIsSpace
                               + "\nLine:" + line);

            throw new IllegalArgumentException("Could not tokenize line:\n"
                    + line + "\n");
        }
        badCnt = 0;
        goodCnt++;

        return true;

        /*
        System.err.println("line:" + sourceString);
        for(String tok: tokens) {
            System.err.println("tok:" + tok);
        }
        */
    }


    /** _more_ */
    private boolean convertedXYZToLatLonAlt = false;

    /**
     * _more_
     */
    public void convertXYZToLatLonAlt() {
        convertedXYZToLatLonAlt = true;
        if (idxX >= 0) {
            values[idxX] = getLongitude();
        }
        if (idxY >= 0) {
            values[idxY] = getLatitude();
        }
        if (idxZ >= 0) {
            values[idxZ] = getAltitude();
        }
    }

    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param pw _more_
     *
     * @return _more_
     */
    public int doPrintCsv(VisitInfo visitInfo, PrintWriter pw) {
        int superCnt = super.doPrintCsv(visitInfo, pw);
        if (superCnt > 0) {
            pw.print(',');
        }
        int cnt = 0;
        for (int fieldCnt = 0; fieldCnt < values.length; fieldCnt++) {
            RecordField recordField = fields.get(fieldCnt);
            if (recordField.getSkip()) {
                continue;
            }

            if (cnt > 0) {
                pw.print(',');
            }
            cnt++;

            if (recordField.isTypeString()) {
                pw.print(getStringValue(recordField.getParamId()));

                continue;
            }

            double value = values[fieldCnt];



            if (recordField.isTypeInteger()) {
                int v = (int) value;
                pw.print(v);

                continue;
            }


            if (fieldCnt == idxX) {
                value = getLongitude();
            } else if (fieldCnt == idxY) {
                value = getLatitude();
            } else if (fieldCnt == idxZ) {
                value = getAltitude();
            }


            double roundingFactor = recordField.getRoundingFactor();
            if (roundingFactor > 0) {
                double nv = Math.round(value * roundingFactor)
                            / roundingFactor;
                value = nv;
            }


            pw.print(value);
        }

        return fields.size() + superCnt;
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        int    precision = 4;
        double value     = 1.23456789;
        //        double nv = Math.round(value * factor) / factor;
        double factor = Math.pow(10, precision);
        double nv     = Math.round(value * factor) / factor;
        System.err.println(factor);
        System.err.println(nv);
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param pw _more_
     *
     * @return _more_
     */
    public int doPrintCsvHeader(VisitInfo visitInfo, PrintWriter pw) {
        int superCnt = super.doPrintCsvHeader(visitInfo, pw);
        int myCnt    = 0;
        if (superCnt > 0) {
            pw.print(',');
        }
        for (int i = 0; i < fields.size(); i++) {
            int         cnt         = 0;
            RecordField recordField = fields.get(i);
            if (recordField.getSkip()) {
                continue;
            }
            if (cnt > 0) {
                pw.print(',');
            }
            cnt++;
            if (convertedXYZToLatLonAlt) {
                if (i == idxX) {
                    pw.append("longitude[unit=\"degrees\"]");

                    continue;
                }
                if (i == idxY) {
                    pw.append("latitude[unit=\"degrees\"]");

                    continue;
                }
                if (i == idxZ) {
                    pw.append("altitude[unit=\"m\"]");

                    continue;
                }
            }
            fields.get(i).printCsvHeader(visitInfo, pw);
        }

        return fields.size() + superCnt;
    }



    /**
     * _more_
     *
     * @param buff _more_
     *
     * @throws Exception _more_
     */
    public void print(Appendable buff) throws Exception {
        super.print(buff);
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getSkip()) {
                continue;
            }
            System.out.println(fields.get(i).getName() + ":" + values[i]
                               + " ");
        }
    }


    /**
     * _more_
     *
     * @param picky _more_
     */
    public void setBePickyAboutTokens(boolean picky) {
        bePickyAboutTokens = picky;
    }

    /**
     * _more_
     *
     * @param line _more_
     */
    public void setFirstDataLine(String line) {
        firstDataLine = line;
    }


}
