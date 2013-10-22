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

package org.ramadda.repository.output;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.type.*;

import org.ramadda.sql.Clause;


import org.ramadda.sql.SqlUtil;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.io.File;

import java.net.*;

import java.sql.ResultSet;
import java.sql.Statement;




import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class CsvOutputHandler extends OutputHandler {


    /** _more_ */
    public static final OutputType OUTPUT_CSV = new OutputType("CSV",
                                                    "default.csv",
                                                    OutputType.TYPE_FEEDS,
                                                    "", ICON_CSV);


    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public CsvOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_CSV);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if (state.getEntry() != null) {
            links.add(makeLink(request, state.getEntry(), OUTPUT_CSV));
        }
    }


    /** _more_ */
    public static final String ARG_FIELDS = "fields";

    /** _more_ */
    public static final String ARG_DELIMITER = "delimiter";


    /**
     * _more_
     *
     * @param request _more_
     * @param groups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected Result listEntries(Request request, List<Entry> entries)
            throws Exception {

        String delimiter = request.getString(ARG_DELIMITER, ",");
        String fieldsArg =
            request.getString(
                ARG_FIELDS,
                "name,id,type,entry_url,north,south,east,west,url,fields");
        StringBuffer sb     = new StringBuffer();
        List<String> fields = StringUtil.split(fieldsArg, ",", true, true);
        for (Entry entry : entries) {
            if (sb.length() == 0) {
                if (fields.contains("fields")) {
                    List<Column> columns =
                        entry.getTypeHandler().getColumns();
                    if (columns != null) {
                        String tmp = null;
                        for (Column column : columns) {
                            if ( !column.getCanExport()) {
                                continue;
                            }
                            if (tmp == null) {
                                tmp = ",";
                            } else {
                                tmp += ",";
                            }
                            tmp += column.getName();
                        }
                        fieldsArg = fieldsArg.replace(",fields", tmp);
                    }
                }
                //                sb.append("#fields=");
                sb.append(fieldsArg);
                sb.append("\n");
            }

            int colCnt = 0;
            for (String field : fields) {
                if (colCnt != 0) {
                    sb.append(delimiter);
                }
                colCnt++;
                if (field.equals("name")) {
                    sb.append(sanitize(entry.getName()));
                } else if (field.equals("fullname")) {
                    sb.append(sanitize(entry.getFullName()));
                } else if (field.equals("type")) {
                    sb.append(entry.getTypeHandler().getType());
                } else if (field.equals("id")) {
                    sb.append(entry.getId());
                } else if (field.equals("entry_url")) {
                    String url = request.url(repository.URL_ENTRY_SHOW,
                                             ARG_ENTRYID, entry.getId());


                    url = request.getAbsoluteUrl(url);
                    sb.append(url);
                } else if (field.equals("url")) {
                    if (entry.getResource().isUrl()) {
                        sb.append(
                            entry.getTypeHandler().getResourcePath(
                                request, entry));
                    } else if (entry.getResource().isFile()) {
                        sb.append(
                            entry.getTypeHandler().getEntryResourceUrl(
                                request, entry));
                    } else {}
                } else if (field.equals("latitude")) {
                    sb.append(entry.getLatitude());
                } else if (field.equals("longitude")) {
                    sb.append(entry.getLongitude());
                } else if (field.equals("north")) {
                    sb.append(entry.getNorth());
                } else if (field.equals("south")) {
                    sb.append(entry.getSouth());
                } else if (field.equals("east")) {
                    sb.append(entry.getEast());
                } else if (field.equals("west")) {
                    sb.append(entry.getWest());
                } else if (field.equals("description")) {
                    sb.append(sanitize(entry.getDescription()));
                } else if (field.equals("fields")) {
                    List<Column> columns =
                        entry.getTypeHandler().getColumns();
                    if (columns != null) {
                        Object[] values =
                            entry.getTypeHandler().getValues(entry);
                        int cnt = 0;
                        for (Column column : columns) {
                            if ( !column.getCanExport()) {
                                continue;
                            }
                            if (cnt > 0) {
                                sb.append(delimiter);
                            }
                            cnt++;
                            String s = sanitize(column.getString(values));
                            sb.append(s);
                        }
                    }
                } else {
                    sb.append("unknown:" + field);
                }
            }
            sb.append("\n");
        }

        return new Result("", sb, getMimeType(OUTPUT_CSV));
    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public String sanitize(String s) {
        if (s == null) {
            return "";
        }
        s = s.replaceAll("\r\n", " ");
        s = s.replaceAll("\r", " ");
        s = s.replaceAll("\n", " ");
        s = s.replaceAll(",", "%2C");

        return s;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param typeHandlers _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result listTypes(Request request, List<TypeHandler> typeHandlers)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        for (TypeHandler theTypeHandler : typeHandlers) {
            sb.append(SqlUtil.comma(theTypeHandler.getType(),
                                    theTypeHandler.getDescription()));
            sb.append("\n");
        }

        return new Result("", sb, getMimeType(OUTPUT_CSV));
    }





    /**
     * _more_
     *
     * @param output _more_
     *
     * @return _more_
     */
    public String getMimeType(OutputType output) {
        if (output.equals(OUTPUT_CSV)) {
            return repository.getMimeTypeFromSuffix(".csv");
        }

        return super.getMimeType(output);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        if (group.isDummy()) {
            request.setReturnFilename("Search_Results.csv");
        } else {
            request.setReturnFilename(group.getName() + ".csv");
        }
        subGroups.addAll(entries);

        return listEntries(request, subGroups);
    }



}
