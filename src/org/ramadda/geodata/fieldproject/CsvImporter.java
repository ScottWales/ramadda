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

package org.ramadda.geodata.fieldproject;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;

import ucar.unidata.util.IOUtil;


import ucar.unidata.util.StringUtil;

import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import java.util.List;


/**
 */
public class CsvImporter extends ImportHandler {

    /** _more_          */
    public static final String TYPE_CSV = "CSV";

    /** _more_          */
    public static final String ARG_CSV_TYPE = "csv.type";

    /**
     * ctor
     *
     * @param repository _more_
     */
    public CsvImporter(Repository repository) {
        super(repository);
    }


    /**
     * _more_
     *
     * @param importTypes _more_
     * @param formBuffer _more_
     */
    @Override
    public void addImportTypes(List<TwoFacedObject> importTypes,
                               StringBuffer formBuffer) {
        super.addImportTypes(importTypes, formBuffer);
        importTypes.add(new TwoFacedObject("Csv Site Import", TYPE_CSV));
        //        formBuffer.append(HtmlUtils.formEntry(msgLabel("CSV Type"), HtmlUtils.input(ARG_CSV_TYPE,"")));
    }


    /**
     *
     * @param request _more_
     * @param fileName _more_
     * @param stream _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public InputStream getStream(Request request, String fileName,
                                 InputStream stream)
            throws Exception {
        if ( !request.getString(ARG_IMPORT_TYPE, "").equals(TYPE_CSV)) {
            return null;
        }

        StringBuffer sb = new StringBuffer("<entries>\n");
        String csv = new String(
                         IOUtil.readBytes(
                             getStorageManager().getFileInputStream(
                                 fileName)));

        int    IDX_LAT         = 0;
        int    IDX_LON         = 1;
        int    IDX_NAME        = 2;
        int    IDX_DESCRIPTION = 3;

        String entryType       = "project_site";
        for (String line : StringUtil.split(csv, "\n", true, true)) {
            List<String> toks = StringUtil.split(line, ",");
            double       lat  = Double.parseDouble(getValue(IDX_LAT, toks));
            double       lon  = Double.parseDouble(getValue(IDX_LON, toks));
            String       name = getValue(IDX_NAME, toks);
            String       desc = getValue(IDX_DESCRIPTION, toks);
            sb.append(XmlUtil.tag("entry", XmlUtil.attrs(new String[] {
                ATTR_TYPE, entryType, ATTR_LATITUDE, "" + lat, ATTR_LONGITUDE,
                "" + lon, ATTR_NAME, name, ATTR_DESCRIPTION, desc
            })));
        }


        sb.append("</entries>");

        return new ByteArrayInputStream(sb.toString().getBytes());


    }

    /**
     * _more_
     *
     * @param idx _more_
     * @param toks _more_
     *
     * @return _more_
     */
    private String getValue(int idx, List<String> toks) {
        if (idx < toks.size()) {
            return toks.get(idx);
        }

        return "";
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param repository _more_
     * @param uploadedFile _more_
     * @param parentEntry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleRequest(Request request, Repository repository,
                                String uploadedFile, Entry parentEntry)
            throws Exception {
        if (true) {
            return null;
        }
        List<Entry> entries = new ArrayList<Entry>();
        String csv = new String(
                         IOUtil.readBytes(
                             getStorageManager().getFileInputStream(
                                 uploadedFile)));
        processCsv(request, parentEntry, csv, entries);

        StringBuffer sb = new StringBuffer();
        for (Entry entry : entries) {
            entry.setUser(request.getUser());
        }
        getEntryManager().addNewEntries(request, entries);
        sb.append(msgHeader("Imported entries"));
        sb.append("<ul>");
        for (Entry entry : entries) {
            sb.append("<li> ");
            sb.append(getEntryManager().getBreadCrumbs(request, entry, true,
                    parentEntry)[1]);
        }

        return getEntryManager().addEntryHeader(request, parentEntry,
                new Result("", sb));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param parentEntry _more_
     * @param csv _more_
     * @param entries _more_
     *
     * @throws Exception _more_
     */
    private void processCsv(Request request, Entry parentEntry, String csv,
                            List<Entry> entries)
            throws Exception {}

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        CsvImporter importer = new CsvImporter(null);
        for (String file : args) {
            List<File>   files = new ArrayList<File>();
            StringBuffer sb    = new StringBuffer();
            //            importer.processXml(null, "parent", IOUtil.readContents(file,(String)null),files, sb);
        }
    }

}
