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

package org.ramadda.data.record;


import org.ramadda.data.record.filter.*;

import ucar.unidata.util.IOUtil;

import java.io.*;

import java.net.URL;
import java.net.URLConnection;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.zip.*;


/**
 * The core class. This represents a file that can either be read or written.
 * It holds a filename and will create a RecordIO when dealing with files.
 * Derived classes need to overwrite the doMakeRecord method.
 *
 *
 * @author Jeff McWhirter
 */
public abstract class RecordFile {

    /** _more_          */
    private static final RecordVisitorGroup dummy1 = null;

    /** _more_          */
    private static final RecordCountVisitor dummy2 = null;

    /** _more_          */
    private static final GeoRecord dummy3 = null;



    /** debug */
    public static boolean debug = false;

    /** The file */
    private String filename;

    /** default skip factor */
    private int defaultSkip = -1;

    /** how many records in the file */
    private long numRecords = 0;

    /** general properties */
    private Hashtable properties;

    /** _more_          */
    private Object[] fileMetadata;


    /**
     * ctor
     */
    public RecordFile() {}

    /**
     * _more_
     *
     * @param properties _more_
     */
    public RecordFile(Hashtable properties) {
        this.properties = properties;
    }

    /**
     * _more_
     *
     * @param filename _more_
     * @param properties _more_
     */
    public RecordFile(String filename, Hashtable properties) {
        this.filename   = filename;
        this.properties = properties;
    }


    /**
     * ctor
     *
     *
     * @param filename The file
     *
     */
    public RecordFile(String filename) {
        this.filename = filename;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public Object[] getFileMetadata() {
        return fileMetadata;
    }

    /**
     * _more_
     *
     * @param metadata _more_
     */
    public void setFileMetadata(Object[] metadata) {
        fileMetadata = metadata;
    }

    /**
     * _more_
     */
    public void initAfterClone() {}

    /**
     * _more_
     *
     * @param filename _more_
     * @param properties _more_
     *
     * @return _more_
     *
     * @throws CloneNotSupportedException _more_
     */
    public RecordFile cloneMe(String filename, Hashtable properties)
            throws CloneNotSupportedException {
        RecordFile that = cloneMe();
        that.initAfterClone();
        that.setFilename(filename);
        if (properties == null) {
            properties = getPropertiesForFile(filename,
                    that.getPropertiesFileName());
        }
        that.setProperties(properties);

        return that;
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
     * @return _more_
     */
    public String getPropertiesFileName() {
        return "record.properties";
    }



    /**
     * _more_
     *
     * @param files _more_
     *
     * @return _more_
     */
    public static Hashtable getProperties(File[] files) {
        Properties p = new Properties();
        for (File f : files) {
            if ( !f.exists()) {
                continue;
            }
            try {
                FileInputStream fis = new FileInputStream(f);
                p.load(fis);
                fis.close();
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }

        return p;
    }



    /**
     * _more_
     *
     * @param file _more_
     * @param defaultCommonFile _more_
     *
     * @return _more_
     */
    public static Hashtable getPropertiesForFile(String file,
            String defaultCommonFile) {
        File   f      = new File(file);
        File   parent = f.getParentFile();
        String commonFile;
        if (parent == null) {
            commonFile = defaultCommonFile;
        } else {
            commonFile = parent + File.separator + defaultCommonFile;
        }
        File[] propertiesFiles = new File[] { new File(commonFile),
                new File(IOUtil.stripExtension(file) + ".properties"),
                new File(file + ".properties"), };

        return getProperties(propertiesFiles);
    }



    /**
     * _more_
     *
     * @return _more_
     *
     * @throws CloneNotSupportedException _more_
     */
    public RecordFile cloneMe() throws CloneNotSupportedException {
        return (RecordFile) super.clone();
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @return _more_
     */
    public boolean canLoad(String file) {
        return false;
    }

    /**
     * _more_
     *
     * @param action _more_
     *
     * @return _more_
     */
    public boolean isCapable(String action) {
        String p = (String) getProperty(action);
        if (p != null) {
            return p.trim().equals("true");
        }

        return false;
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param suffixes _more_
     * @param checkForNumberSuffix _more_
     *
     * @return _more_
     */
    public boolean canLoad(String file, String[] suffixes,
                           boolean checkForNumberSuffix) {
        for (String suffix : suffixes) {
            if (file.endsWith(suffix)) {
                return true;
            }
        }
        if ( !checkForNumberSuffix) {
            return false;
        }
        file = file.trim();
        while (file.matches(".*\\.\\d+\\z")) {
            file = IOUtil.stripExtension(file);
            for (String suffix : suffixes) {
                if (file.endsWith(suffix)) {
                    return true;
                }
            }
        }

        return false;
    }



    /**
     * Get a property
     *
     * @param key property key
     *
     * @return property value
     */
    public Object getProperty(Object key) {
        if (properties == null) {
            return null;
        }

        return properties.get(key);
    }


    /**
     * _more_
     *
     * @param prop _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getProperty(String prop, String dflt) {
        return getProperty(properties, prop, dflt);
    }


    /**
     * _more_
     *
     * @param field _more_
     * @param properties _more_
     * @param prop _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getProperty(RecordField field, Hashtable properties,
                              String prop, String dflt) {
        return getProperty(properties, prop, dflt);
    }


    /**
     * _more_
     *
     * @param properties _more_
     * @param prop _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getProperty(Hashtable properties, String prop,
                              String dflt) {
        if (properties == null) {
            return dflt;
        }
        String value = (String) properties.get(prop);
        if (value != null) {
            return value;
        }

        return dflt;
    }



    /**
     * put a property
     *
     * @param key key
     * @param value value
     */
    public void putProperty(Object key, Object value) {
        if (properties == null) {
            properties = new Hashtable();
        }
        properties.put(key, value);
    }


    /**
     * _more_
     *
     * @param buff _more_
     *
     * @throws Exception _more_
     */
    public void getInfo(Appendable buff) throws Exception {}

    /**
     * Make the input io object
     *
     * @return The RecordIO for reading the file
     *
     * @throws IOException On badness
     */
    public RecordIO doMakeInputIO() throws IOException {
        return doMakeInputIO(false);
    }


    /**
     * Make the input io object
     *
     * @param buffered If true then make a bufferedinputstream
     *
     * @return The RecordIO
     *
     * @throws IOException On badness
     */
    public RecordIO doMakeInputIO(boolean buffered) throws IOException {
        return new RecordIO(doMakeInputStream(buffered));
    }


    /**
     * Make the input stream
     *
     * @param buffered If true then make a BufferedInputStream wrapper around the InputStream
     *
     * @return The input stream
     *
     * @throws IOException On badness
     */
    public InputStream doMakeInputStream(boolean buffered)
            throws IOException {
        int         size = 8000;
        InputStream is   = null;
        if (new File(filename).exists()) {
            is = new FileInputStream(filename);
        } else {
            //Try it as a url
            URL           url        = new URL(filename);
            URLConnection connection = url.openConnection();
            is = connection.getInputStream();
        }

        if (filename.toLowerCase().endsWith(".zip")) {
            ZipEntry       ze  = null;
            ZipInputStream zin = new ZipInputStream(is);
            //Read into the zip stream to the first entry
            while ((ze = zin.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }

                break;
                /*                String path = ze.getName();
                if(path.toLowerCase().endsWith(".las")) {
                    break;
                }
                */
            }
            is = zin;
        }


        if ( !buffered) {
            //            System.err.println("not buffered");
            //            return is;
            //            size = 8*3;
        }

        if (buffered) {
            size = 1000000;
        }

        //        System.err.println("buffer size:" + size);
        return new BufferedInputStream(is, size);
    }

    /**
     * Make the output stream. Note - this always makes a BufferedOutputStream wrapper around the
     * FileOutputStream
     *
     * @return The output stream
     *
     * @throws IOException On badness
     */
    public DataOutputStream doMakeOutputStream() throws IOException {
        return new DataOutputStream(
            new BufferedOutputStream(new FileOutputStream(filename), 10000));
    }

    /**
     * _more_
     *
     * @param properties _more_
     */
    public void setProperties(Hashtable properties) {
        this.properties = properties;
    }


    /**
     * Make a Record. This calls the doMakeRecord factory method to actually
     * make the record. It then sets the quickscan flag on the record if needed
     *
     * @param visitInfo Holds visit state
     *
     * @return The record
     */
    public Record makeRecord(VisitInfo visitInfo) {
        Record record = doMakeRecord(visitInfo);
        if (visitInfo.getQuickScan()) {
            //            System.err.println("quick scan");
            record.setQuickScan(true);
        } else {
            //            System.err.println("full scan");
        }

        return record;
    }



    /**
     * Factory method for creating a Record object.
     *
     *
     * @param visitInfo Visit state
     * @return The Record object
     */
    public abstract Record doMakeRecord(VisitInfo visitInfo);


    /**
     * Get the fields for the default record
     *
     * @return List of fields in the default record
     */
    public List<RecordField> getFields() {
        Record            record = makeRecord(new VisitInfo());
        List<RecordField> fields = record.getFields();

        return new ArrayList<RecordField>(fields);
    }


    /**
     * Get the fields that are marked as searchable
     *
     * @return List of searchable fields
     */
    public List<RecordField> getSearchableFields() {
        List<RecordField> fields = getFields();
        List<RecordField> result = new ArrayList<RecordField>();
        for (RecordField attr : fields) {
            if (attr.getSearchable()) {
                result.add(attr);
            }
        }

        return result;
    }




    /**
     * Get the fields that are marked as being chartable. Note - we should generalize this as finding
     * fields with a certain flag set.
     *
     * @return Chartable fields
     */
    public List<RecordField> getChartableFields() {
        List<RecordField> fields = getFields();
        List<RecordField> result = new ArrayList<RecordField>();
        for (RecordField attr : fields) {
            if (attr.getChartable()) {
                result.add(attr);
            }
        }

        return result;
    }

    /**
     * _more_
     *
     * @param currentRecord _more_
     *
     * @return _more_
     */
    public Record makeNextRecord(Record currentRecord) {
        return currentRecord;
    }

    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     *
     * @throws IOException On badness
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        return visitInfo;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public VisitInfo doMakeVisitInfo() {
        return new VisitInfo();
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public int getSkip(VisitInfo visitInfo) {

        if ((visitInfo != null) && (visitInfo.getSkip() >= 0)) {
            return visitInfo.getSkip();
        }
        if (defaultSkip >= 0) {
            return defaultSkip;
        }

        return 0;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getSkip() {
        return defaultSkip;
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param record _more_
     *
     * @return _more_
     *
     * @throws IOException On badness
     */
    public Record.ReadStatus readNextRecord(VisitInfo visitInfo,
                                            Record record)
            throws IOException {
        visitInfo.addRecordIndex(1);
        Record.ReadStatus status =
            record.readNextRecord(visitInfo.getRecordIO());

        return status;
    }

    /**
     * _more_
     *
     *
     * @param visitor _more_
     * @throws Exception On badness
     */
    public void visit(RecordVisitor visitor) throws Exception {
        visit(visitor, doMakeVisitInfo(), null);
    }


    /**
     * _more_
     *
     * @param visitor _more_
     * @param filter _more_
     *
     * @throws Exception On badness
     */
    public void visit(RecordVisitor visitor, RecordFilter filter)
            throws Exception {
        visit(visitor, doMakeVisitInfo(), filter);
    }



    /**
     * _more_
     *
     * @param visitor _more_
     * @param visitInfo _more_
     * @param filter _more_
     *
     * @throws Exception On badness
     */
    public void visit(RecordVisitor visitor, VisitInfo visitInfo,
                      RecordFilter filter)
            throws Exception {

        //        System.err.println("RecordFile: " + getClass().getName() + ".visit");
        if (visitInfo == null) {
            visitInfo = new VisitInfo();
        }
        int skip = getSkip(visitInfo);
        //        System.err.println("doMakeInputIO: " + skip);
        RecordIO recordIO = doMakeInputIO(skip == 0);
        visitInfo.setRecordIO(recordIO);
        visitInfo = prepareToVisit(visitInfo);
        //Start at -1 since we always increment below
        visitInfo.setRecordIndex(-1);
        boolean ok = true;
        try {
            Record record = makeRecord(visitInfo);
            if (visitInfo.getStart() > 0) {
                skip(visitInfo, record, visitInfo.getStart());
                visitInfo.setRecordIndex(visitInfo.getStart());
            }
            int  cnt = 0;
            long t1  = System.currentTimeMillis();

            while (true) {
                if ((visitInfo.getStop() > 0)
                        && (visitInfo.getRecordIndex()
                            > visitInfo.getStop())) {
                    break;
                }
                try {
                    //This sets the record index
                    Record.ReadStatus status = readNextRecord(visitInfo,
                                                   record);
                    if (status == Record.ReadStatus.EOF) {
                        break;
                    }
                    record.index = visitInfo.getRecordIndex();
                    if (status == Record.ReadStatus.OK) {
                        if ( !processAfterReading(visitInfo, record)) {
                            continue;
                        }
                        if ((filter == null)
                                || filter.isRecordOk(record, visitInfo)) {
                            cnt++;
                            if ((visitInfo.getMax() > 0)
                                    && (cnt >= visitInfo.getMax())) {
                                break;
                            }
                            if ( !visitor.visitRecord(this, visitInfo,
                                    record)) {
                                break;
                            }
                        }
                    }

                    record = makeNextRecord(record);
                    if (record == null) {
                        return;
                    }
                    skip = getSkip(visitInfo);
                    if (skip > 0) {
                        skip(visitInfo, record, skip);
                    }
                } catch (java.io.EOFException oef) {
                    //Bad form to catch an exception as logic but...
                    //                    oef.printStackTrace();
                    break;
                } catch (Exception exc) {
                    throw exc;
                }
            }
            if (ok) {
                visitorFinished(visitor, visitInfo);
            }
            long t2 = System.currentTimeMillis();
            if (debug) {
                System.err.println("RECORD: # visited:" + cnt + " in time:"
                                   + (t2 - t1) + "ms");
            }
        } finally {
            try {
                recordIO.close();
            } catch (Exception ignore) {}
        }
    }

    /**
     * _more_
     *
     * @param indices _more_
     */
    public void setDateIndices(int[] indices) {
        dateIndices = indices;
    }


    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param record _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public boolean processAfterReading(VisitInfo visitInfo, Record record)
            throws Exception {
        if (dateIndices != null) {
            setDateFromFields(record, dateIndices);
        }

        return true;
    }

    /**
     * _more_
     *
     * @param visitor _more_
     * @param visitInfo _more_
     */
    public void visitorFinished(RecordVisitor visitor, VisitInfo visitInfo) {
        visitor.finished(this, visitInfo);
    }


    /**
     * _more_
     *
     *
     * @param pw _more_
     * @throws Exception On badness
     */
    public void printCsv(final PrintWriter pw) throws Exception {
        final int[]   cnt     = { 0 };
        RecordVisitor visitor = new RecordVisitor() {
            public boolean visitRecord(RecordFile file, VisitInfo visitInfo,
                                       Record record) {
                if (cnt[0] == 0) {
                    record.printCsvHeader(visitInfo, pw);
                }
                cnt[0]++;
                record.printCsv(visitInfo, pw);

                return true;
            }
        };
        visit(visitor);
    }


    /**
     * _more_
     *
     * @param pw _more_
     * @param fields _more_
     *
     * @throws Exception _more_
     */
    public void printCsv(final PrintWriter pw, final List<RecordField> fields)
            throws Exception {
        RecordVisitor visitor = new CsvVisitor(pw, fields);
        visit(visitor);
    }


    /**
     * _more_
     *
     *
     * @param visitInfo _more_
     * @param record _more_
     * @param howMany _more_
     *
     * @return _more_
     *
     * @throws IOException On badness
     */
    public boolean skip(VisitInfo visitInfo, Record record, int howMany)
            throws IOException {
        visitInfo.addRecordIndex(howMany);
        visitInfo.getRecordIO().getDataInputStream().skipBytes(howMany
                * record.getRecordSize());

        return true;
    }


    /**
     * _more_
     *
     * @param index _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public Record getRecord(int index) throws Exception {
        RecordIO recordIO = doMakeInputIO(false);
        Record   record   = (Record) makeRecord(new VisitInfo());
        skip(new VisitInfo(recordIO), record, index);
        record.readNextRecord(recordIO);

        return record;
    }


    /**
     * _more_
     *
     * @param recordIO _more_
     *
     * @return _more_
     *
     * @throws IOException On badness
     */
    public RecordIO readHeader(RecordIO recordIO) throws IOException {
        return recordIO;
    }

    /**
     * _more_
     *
     * @param recordIO _more_
     *
     * @throws Exception On badness
     */
    public void writeHeader(RecordIO recordIO) throws Exception {}


    /**
     * _more_
     *
     * @param outputStream _more_
     *
     * @param recordOutput _more_
     * @param visitInfo _more_
     * @param filter _more_
     * @param writeHeader _more_
     *
     *
     * @return _more_
     * @throws Exception On badness
     */
    public VisitInfo write(RecordIO recordOutput, VisitInfo visitInfo,
                           RecordFilter filter, boolean writeHeader)
            throws Exception {
        if (visitInfo == null) {
            visitInfo = doMakeVisitInfo();
        }
        RecordIO recordInput = doMakeInputIO(getSkip(visitInfo) == 0);
        recordInput = readHeader(recordInput);
        visitInfo.setRecordIO(recordInput);
        if (writeHeader) {
            writeHeader(recordOutput);
        }
        try {
            writeRecords(recordInput, recordOutput, visitInfo, filter);
        } finally {
            try {
                recordInput.close();
            } catch (Exception ignore) {}
        }

        return visitInfo;
    }




    /**
     * _more_
     *
     * @param recordInput _more_
     * @param recordOutput _more_
     * @param visitInfo _more_
     * @param filter _more_
     *
     * @throws Exception _more_
     */
    public void writeRecords(RecordIO recordInput, RecordIO recordOutput,
                             VisitInfo visitInfo, RecordFilter filter)
            throws Exception {
        Record record = makeRecord(visitInfo);
        int    index  = 0;
        if (visitInfo.getStart() > 0) {
            skip(visitInfo, record, visitInfo.getStart());
            index = visitInfo.getStart();
        }
        long t1 = System.currentTimeMillis();
        while (true) {
            if ((visitInfo.getStop() > 0) && (index > visitInfo.getStop())) {
                break;
            }
            try {
                record.readNextRecord(recordInput);
                record.index = index;
                if ((filter == null)
                        || filter.isRecordOk(record, visitInfo)) {
                    visitInfo.incrCount();
                    record.write(recordOutput);
                }
                record = makeNextRecord(record);
                if (record == null) {
                    return;
                }
                int skip = getSkip(visitInfo);

                if (skip > 0) {
                    skip(visitInfo, record, skip);
                    index += skip;
                } else {
                    index++;
                }
            } catch (java.io.EOFException oef) {
                //Bad form to catch an exception as logic but...
                break;
            }
        }
        long t2 = System.currentTimeMillis();
        //            System.err.println("Time:" + (t2 - t1));
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return filename;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Set the filename
     *
     * @param filename filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * _more_
     *
     * @param skip _more_
     */
    public void setDefaultSkip(int skip) {
        defaultSkip = skip;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public long getNumRecords() {
        return numRecords;
    }


    /**
     * _more_
     *
     * @param n _more_
     */
    public void setNumRecords(long n) {
        numRecords = n;
    }




    /**
     * main
     *
     * @param args args
     *
     * @throws Exception On badness
     */
    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            InputStream inputStream =
                new BufferedInputStream(new FileInputStream(arg), 1000000);
            DataInputStream dis = new DataInputStream(inputStream);

        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getHtmlDescription() {
        return "";
    }

    /**
     * _more_
     *
     * @param format _more_
     *
     * @return _more_
     */
    public static SimpleDateFormat makeDateFormat(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return sdf;
    }

    /**
     * _more_
     *
     * @param record _more_
     * @param field _more_
     * @param v _more_
     *
     * @return _more_
     */
    public boolean isMissingValue(Record record, RecordField field,
                                  double v) {
        double missing = field.getMissingValue();

        return missing == v;
    }


    /**
     * _more_
     *
     * @param record _more_
     * @param field _more_
     * @param s _more_
     *
     * @return _more_
     */
    public boolean isMissingValue(Record record, RecordField field,
                                  String s) {
        return s.equals("---") || s.equals("n.v.") || (s.length() == 0)
               || s.equals("null") || s.equals("nan") || s.equals("NAN")
               || s.equals("NA") || s.equals("NaN");
    }


    /** _more_          */
    public static final String FILE_SEPARATOR = "_file_";

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public String getOriginalFilename(String name) {
        File f = new File(name);
        name = f.getName();
        int idx = name.indexOf(FILE_SEPARATOR);
        if (idx >= 0) {
            name = name.substring(idx + FILE_SEPARATOR.length());
        }

        return name;
    }


    /** _more_          */
    protected int[] dateIndices;

    /** _more_          */
    private static SimpleDateFormat[] sdfs = new SimpleDateFormat[] {
        makeDateFormat("yyyy"), makeDateFormat("yyyy-MM"),
        makeDateFormat("yyyy-MM-dd"), makeDateFormat("yyyy-MM-dd-HH"),
        makeDateFormat("yyyy-MM-dd-HH-mm"),
        makeDateFormat("yyyy-MM-dd-HH-mm-ss"),
    };

    /**
     * _more_
     *
     * @param record _more_
     * @param indices _more_
     *
     * @return _more_
     */
    public SimpleDateFormat getDateFormat(Record record, int[] indices) {
        int goodCnt = 0;
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] < 0) {
                break;
            }
            goodCnt++;
        }

        return sdfs[goodCnt - 1];
    }


    /*
     * This gets called after a record has been read
     * It extracts and creates the record date/time
     */

    /**
     * _more_
     *
     * @param record _more_
     * @param indices _more_
     *
     * @throws Exception _more_
     */
    public void setDateFromFields(Record record, int[] indices)
            throws Exception {
        StringBuffer     dttm = new StringBuffer();
        SimpleDateFormat sdf  = getDateFormat(record, indices);
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] < 0) {
                break;
            }
            if (i > 0) {
                dttm.append("-");
            }
            int v = (int) record.getValue(indices[i]);
            if (v < 10) {
                dttm.append("0");
            }
            dttm.append(v);
        }
        Date date = sdf.parse(dttm.toString());
        record.setRecordTime(date.getTime());
    }



}
