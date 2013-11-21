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
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.Station;
import org.ramadda.util.XlsUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringUtil;

import java.awt.*;
import java.awt.image.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.*;


/**
 *
 */
public abstract class TextFile extends PointFile implements Fields {

    /** _more_ */
    static int cnt = 0;

    /** _more_ */
    int mycnt = cnt++;

    /** _more_ */
    public static final String PROP_FIELDS = "fields";



    /** _more_ */
    public static final String ATTR_TYPE = "type";

    /** _more_ */
    public static final String ATTR_MISSING = "missing";

    /** _more_ */
    public static final String ATTR_VALUE = "value";

    /** _more_ */
    public static final String ATTR_FORMAT = "format";

    /** _more_ */
    public static final String ATTR_UNIT = "unit";

    /** _more_ */
    public static final String ATTR_SEARCHABLE = "searchable";

    /** _more_ */
    public static final String ATTR_CHARTABLE = "chartable";


    /** _more_ */
    public static final String TYPE_STRING = "string";

    /** _more_ */
    public static final String TYPE_DATE = "date";

    public static final String DFLT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm Z";

    /** _more_ */
    public static final String PROP_SKIPLINES = "skiplines";

    /** _more_ */
    public static final String PROP_HEADER_DELIMITER = "header.delimiter";

    /** _more_ */
    public static final String PROP_DELIMITER = "delimiter";

    /** _more_ */
    protected String firstDataLine = null;


    /** _more_ */
    private List<String> headerLines = new ArrayList<String>();


    /**
     * _more_
     */
    public TextFile() {}

    /**
     * ctor
     *
     *
     * @param filename _more_
     * @throws Exception On badness
     *
     * @throws IOException _more_
     */
    public TextFile(String filename) throws IOException {
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
    public TextFile(String filename, Hashtable properties)
            throws IOException {
        super(filename, properties);
    }



    /**
     * _more_
     *
     * @param buffered _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public RecordIO doMakeInputIO(boolean buffered) throws IOException {
        String file = getFilename();
        Reader reader;
        if (file.endsWith(".xls")) {
            reader = new StringReader(XlsUtil.xlsToCsv(file));
        } else {
            reader = new FileReader(getFilename());
        }

        return new RecordIO(new BufferedReader(reader));
    }


    /** _more_ */
    private static Hashtable<String, String> fieldsMap =
        new Hashtable<String, String>();

    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public String getFieldsFileContents(String path) throws IOException {
        String fields = fieldsMap.get(path);
        if (fields == null) {
            fields = IOUtil.readContents(path, getClass()).trim();
            fields = fields.replaceAll("\n", " ");
            fieldsMap.put(path, fields);
        }

        return fields;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public String getFieldsFileContents() throws IOException {
        String path = getClass().getCanonicalName();
        path = path.replaceAll("\\.", "/");
        path = "/" + path + ".fields.txt";

        //        System.err.println ("path:" + path);
        return getFieldsFileContents(path);
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public int getSkipLines(VisitInfo visitInfo) {
        int skipLines = Integer.parseInt(getProperty(PROP_SKIPLINES, "0"));

        return skipLines;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getHeaderDelimiter() {
        return getProperty(PROP_HEADER_DELIMITER, (String) null);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isHeaderStandard() {
        return false;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean readHeader() {
        return false;
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
    public RecordIO readHeader(RecordIO recordIO) throws IOException {
        return recordIO;
    }

    /**
     * _more_
     *
     * @param recordIO _more_
     *
     * @throws Exception _more_
     */
    public void writeHeader(RecordIO recordIO) throws Exception {
        for (String line : headerLines) {
            recordIO.getPrintWriter().println(line);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<String> getHeaderLines() {
        return headerLines;
    }

    /**
     * _more_
     *
     * @param siteId _more_
     * @param record _more_
     *
     * @return _more_
     */
    public Station setLocation(String siteId, TextRecord record) {
        Station station = setLocation(siteId);
        if (station != null) {
            record.setLocation(station);
        }

        return station;
    }



    /**
     * _more_
     *
     * @param lines _more_
     */
    public void setHeaderLines(List<String> lines) {
        headerLines = lines;
    }

    /**
     * _more_
     */
    public void initAfterClone() {
        super.initAfterClone();
        headerLines = new ArrayList<String>();
    }

    /**
     * _more_
     *
     * @param line _more_
     *
     * @return _more_
     */
    public boolean isHeaderLine(String line) {
        return line.startsWith("#");
    }

    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {

        boolean haveReadHeader  = headerLines.size() > 0;
        String  headerDelimiter = getHeaderDelimiter();
        if (headerDelimiter != null) {
            while (true) {
                String line = visitInfo.getRecordIO().readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.equals(headerDelimiter)) {
                    break;
                }
                if ( !haveReadHeader) {
                    headerLines.add(line);
                }
            }
        } else if (isHeaderStandard()) {
            while (true) {
                String line = visitInfo.getRecordIO().readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.length() == 0) {
                    break;
                }

                if ( !isHeaderLine(line)) {
                    firstDataLine = line;

                    break;
                }


                if ( !haveReadHeader) {
                    headerLines.add(line);
                    line = line.substring(1);
                    int idx = line.indexOf("=");
                    if (idx >= 0) {
                        List<String> toks = StringUtil.splitUpTo(line, "=",
                                                2);
                        putProperty(toks.get(0), toks.get(1));
                    }
                }
            }
        } else {
            int skipCnt = getSkipLines(visitInfo);
            for (int i = 0; i < skipCnt; i++) {
                String line = visitInfo.getRecordIO().readLine();
                if ( !haveReadHeader) {
                    headerLines.add(line);
                }
            }
            if (headerLines.size() != skipCnt) {
                throw new IllegalArgumentException(
                    "Bad number of header lines:" + headerLines.size());
            }
        }

        return visitInfo;
    }





    /**
     * _more_
     *
     * @param fields _more_
     */
    public void putFields(String[] fields) {
        putProperty(PROP_FIELDS, makeFields(fields));
    }


    /**
     * _more_
     *
     * @param fields _more_
     *
     * @return _more_
     */
    public String makeFields(String[] fields) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] == null) {
                continue;
            }
            if (i > 0) {
                sb.append(",");
            }
            sb.append(fields[i]);
        }

        return sb.toString();
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param attrs _more_
     *
     * @return _more_
     */
    public String makeField(String id, String... attrs) {
        StringBuffer asb = new StringBuffer();
        for (String attr : attrs) {
            asb.append(attr);
            asb.append(" ");
        }

        return id + "[" + asb + "]";
    }

    /**
     * _more_
     *
     * @param index _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public PointRecord getRecord(int index) throws Exception {
        throw new IllegalArgumentException("Not implemented");
    }

    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param record _more_
     * @param howMany _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public boolean skip(VisitInfo visitInfo, Record record, int howMany)
            throws IOException {
        BufferedReader in = visitInfo.getRecordIO().getBufferedReader();
        for (int i = 0; i < howMany; i++) {
            String line = in.readLine();
            if (line == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * _more_
     *
     * @param d _more_
     *
     * @return _more_
     */
    public String attrValue(double d) {
        return attrValue("" + d);
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public String attrValue(String v) {
        return HtmlUtils.attr(ATTR_VALUE, v);
    }


    public String attr(String n, String v) {
        return HtmlUtils.attr(n, v);
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public String attrType(String v) {
        return HtmlUtils.attr(ATTR_TYPE, v);
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public String attrMissing(double v) {
        return HtmlUtils.attr(ATTR_MISSING, "" + v);
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public String attrFormat(String v) {
        return HtmlUtils.attr(ATTR_FORMAT, v);
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public String attrUnit(String v) {
        return HtmlUtils.attr(ATTR_UNIT, v);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String attrChartable() {
        return HtmlUtils.attr(ATTR_CHARTABLE, "true");
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String attrSearchable() {
        return HtmlUtils.attr(ATTR_SEARCHABLE, "true");
    }
}
