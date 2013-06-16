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
package org.ramadda.data.point.text;




import org.ramadda.data.record.*;
import org.ramadda.data.point.*;

import org.ramadda.util.Station;
import org.ramadda.util.XlsUtil;
import org.ramadda.util.HtmlUtils;
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
public abstract class TextFile extends PointFile {

    static int cnt =0;
    int mycnt = cnt++;

    /** _more_          */
    public static final String PROP_FIELDS = "fields";

    public static final String FIELD_SITE_ID = "Site_Id";
    public static final String FIELD_LATITUDE ="Latitude";
    public static final String FIELD_LONGITUDE  = "Longitude";
    public static final String FIELD_ELEVATION  = "Elevation";
    public static final String FIELD_DEPTH  = "Depth";
    public static final String FIELD_DATE = "Date";
    public static final String FIELD_TIME = "Time";
    public static final String FIELD_YEAR = "Year";
    public static final String FIELD_MONTH = "Month";
    public static final String FIELD_DAY = "Day";
    public static final String FIELD_JULIAN_DAY = "Julian_Day";
    public static final String FIELD_HOUR = "Hour";
    public static final String FIELD_MINUTE = "Minute";
    public static final String FIELD_SECOND = "Second";
    public static final String FIELD_STANDARD_DEVIATION = "Standard_Deviation";

    public static final String FIELD_NORTH = "North";
    public static final String FIELD_EAST = "East";
    public static final String FIELD_VERTICAL = "Vertical";
    public static final String FIELD_NORTH_STD_DEVIATION = "North_Std_Deviation";
    public static final String FIELD_EAST_STD_DEVIATION = "East_Std_Deviation";
    public static final String FIELD_VERTICAL_STD_DEVIATION = "East_Vertical_Deviation";

    public static final String FIELD_QUALITY = "Quality";


    public static final String FIELD_TEMPERATURE = "Temperature";
    public static final String FIELD_PRESSURE = "Pressure";
    public static final String FIELD_WIND_SPEED = "Wind_Speed";
    public static final String FIELD_WIND_DIRECTION = "Wind_Direction";
    public static final String FIELD_RELATIVE_HUMIDITY = "Relative_Humidity";
    public static final String FIELD_DELTA_T = "Delta_T";

    public static final String UNIT_CELSIUS = "Celsius";
    public static final String UNIT_HPA = "hPa";
    public static final String UNIT_PERCENT = "%";
    public static final String UNIT_DEGREES = "degrees";
    public static final String UNIT_M_S = "m/s";
    public static final String UNIT_ = "";



    public static final String ATTR_TYPE = "type";
    public static final String ATTR_MISSING = "missing";
    public static final String ATTR_VALUE = "value";
    public static final String ATTR_FORMAT = "format";

    public static final String ATTR_UNIT = "unit";
    public static final String ATTR_SEARCHABLE = "searchable";
    public static final String ATTR_CHARTABLE = "chartable";


    public static final String TYPE_STRING = "string";
    public static final String TYPE_DATE = "date";

    /** _more_          */
    public static final String PROP_SKIPLINES = "skiplines";
    public static final String PROP_HEADER_DELIMITER = "header.delimiter";
    public static final String PROP_DELIMITER = "delimiter";

    /** _more_          */
    private List<String> headerLines = new ArrayList<String>();


    /**
     * _more_
     */
    public TextFile() {
    }

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


    private static Hashtable<String,String> fieldsMap = new Hashtable<String,String>();

    public String getFieldsFileContents(String path) throws IOException {
        String fields = fieldsMap.get(path);
        if(fields==null) {
            fields = IOUtil.readContents(path, getClass()).trim();
            fields = fields.replaceAll("\n"," ");
            fieldsMap.put(path, fields);
        }
        return fields;
    }
    
    public String getFieldsFileContents() throws IOException {
        String path = getClass().getCanonicalName();
        path = path.replaceAll("\\.","/");
        path = "/" + path +".fields.txt";
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

    public String getHeaderDelimiter() {
        return getProperty(PROP_HEADER_DELIMITER,(String) null);
    }

    public boolean isHeaderStandard() {
        return false;
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

    public Station  setLocation(String siteId, TextRecord record) {
        Station station = setLocation(siteId);
        if(station!=null) {
            record.setLocation(station);
        }
        return station;
    }



    public void setHeaderLines(List<String> lines) {
        headerLines = lines;
    }

    public void initAfterClone() {
        super.initAfterClone();
        headerLines = new ArrayList<String>();
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
        String headerDelimiter = getHeaderDelimiter();
        if(headerDelimiter!=null) {
            while(true) {
                String line = visitInfo.getRecordIO().readLine();
                if(line == null) break;
                line  = line.trim();
                if(line.equals(headerDelimiter)) break;
                if(!haveReadHeader) {
                    headerLines.add(line);
                }
            }
        } else if(isHeaderStandard()) {
            while(true) {
                String line = visitInfo.getRecordIO().readLine().trim();
                if(line == null || line.length()==0) break;
                if(!line.startsWith("#")) {
                    throw new IllegalArgumentException("Bad header line:" + line);
                }
                if(!haveReadHeader) {
                    headerLines.add(line);
                    line  = line.substring(1);
                    int idx = line.indexOf("=");
                    if(idx>=0)  {
                        List<String> toks = StringUtil.splitUpTo(line,"=",2);
                        putProperty(toks.get(0),toks.get(1));
                    }
                }
            }

        } else {
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
        }
        return visitInfo;
    }





    public void putFields(String[] fields) {
        putProperty(PROP_FIELDS, makeFields(fields));
    }


    public String makeFields(String[] fields) {
        StringBuffer sb  = new StringBuffer();
        for(int i=0;i<fields.length;i++) { 
            if(fields[i] == null) continue;
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
    public String attrMissing(double v) {
        return HtmlUtils.attr(ATTR_MISSING, ""+v);
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
