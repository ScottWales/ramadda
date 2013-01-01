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

import org.ramadda.util.XlsUtil;
import org.ramadda.util.HtmlUtils;
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
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public abstract class TextFile extends PointFile {

    public static final String FIELD_SITE_ID = "Site_Id";
    public static final String FIELD_LATITUDE ="Latitude";
    public static final String FIELD_LONGITUDE  = "Longitude";
    public static final String FIELD_YEAR = "Year";
    public static final String FIELD_DATE = "Date";

    public static final String ATTR_TYPE = "type";
    public static final String ATTR_VALUE = "value";
    public static final String ATTR_FORMAT = "format";

    public static final String ATTR_UNIT = "unit";
    public static final String ATTR_SEARCHABLE = "searchable";
    public static final String ATTR_CHARTABLE = "chartable";


    public static final String TYPE_STRING = "string";
    public static final String TYPE_DATE = "date";

    /** _more_          */
    public static final String PROP_SKIPLINES = "skiplines";

    /** _more_          */
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
        if(file.endsWith(".xls")) {
            reader= new StringReader(XlsUtil.xlsToCsv(file));
        } else {
            reader= new FileReader(getFilename());
        }
        return new RecordIO(
                            new BufferedReader(reader));
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

    public List<String> getHeaderLines() {
        return headerLines;
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
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        boolean haveReadHeader = headerLines.size()>0;
        int skipCnt = getSkipLines(visitInfo);
        for (int i = 0; i < skipCnt; i++) {
            String line = visitInfo.getRecordIO().readLine();
            if(!haveReadHeader) {
                headerLines.add(line);
            }
        }
        if(headerLines.size()!=skipCnt) {
            throw new IllegalArgumentException("Bad number of header lines:" + headerLines.size());
        }
        return visitInfo;
    }


    public String makeFields(String[] fields) {
        StringBuffer sb  = new StringBuffer();
        for(int i=0;i<fields.length;i++) { 
            if(i>0) sb.append(",");
            sb.append (fields[i]);
        }
        return sb.toString();
    }

    public String makeField(String id, String ... attrs) {
        StringBuffer asb = new StringBuffer();
        for(String attr: attrs) {
            asb.append(attr);
            asb.append(" ");
        }
        return id +"[" + asb +"]";
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

    public String attrValue(double d) {
        return attrValue(""+d);
    }

    public String attrValue(String v) {
        return HtmlUtils.attr(ATTR_VALUE, v);
    }

    public String attrType(String v) {
        return HtmlUtils.attr(ATTR_TYPE, v);
    }
    public String attrFormat(String v) {
        return HtmlUtils.attr(ATTR_FORMAT, v);
    }

    public String attrUnit(String v) {
        return HtmlUtils.attr(ATTR_UNIT, v);
    }

    public String attrChartable() {
        return HtmlUtils.attr(ATTR_CHARTABLE, "true");
    }

    public String attrSearchable() {
        return HtmlUtils.attr(ATTR_SEARCHABLE, "true");
    }
}
