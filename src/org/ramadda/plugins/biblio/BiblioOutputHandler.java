/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
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

package org.ramadda.plugins.biblio;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WmsUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;


import java.util.GregorianCalendar;
import java.util.Date;


import java.io.File;


import java.net.*;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;



import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class BiblioOutputHandler extends OutputHandler implements BiblioConstants {




    /** _more_ */
    public static final OutputType OUTPUT_BIBLIO_EXPORT =
        new OutputType("Export Bibliography", "biblio_export", OutputType.TYPE_VIEW,
                       "", "/biblio/book.png");


    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public BiblioOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_BIBLIO_EXPORT);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        for(Entry entry:state.getAllEntries()) {
            if (entry.getTypeHandler().getType().equals("biblio")) {
                links.add(makeLink(request, state.getEntry(),
                                   OUTPUT_BIBLIO_EXPORT));
                return;
            }
        }

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);
        return outputEntries(request, entries);
    }

    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        return outputEntries(request, entries);
    }


    public Result outputEntries(Request request, 
                                List<Entry> entries)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        for(Entry entry: entries) {
            if (!entry.getTypeHandler().getType().equals("biblio")) {
                continue;
            }
            appendExport(request, entry, sb);
        }
        request.setReturnFilename("bibliography.txt");
        Result result =  new Result("", sb);
        result.setShouldDecorate(false);
        result.setMimeType("text/plain");
        return result;
    }



    /*
     <column name="type" type="enumerationplus"  label="Type" values="Generic,Journal Article,Report" />
     <column name="primary_author" type="string" size="500" changetype="true"  label="Primary Author"  cansearch="true"/>
     <column name="institution" type="string"  label="Institution"  cansearch="true"/>
     <column name="other_authors" type="list"  changetype="true" size="5000" label="Other Authors"  rows="5"/>
     <column name="publication" type="enumerationplus"  label="Publication"  />
     <column name="volume_number" type="string"  label="Volume"  />
     <column name="issue_number" type="string"  label="Issue"  />
     <column name="pages" type="string"  label="Pages"  />
     <column name="doi" type="string"  label="DOI"  />
     <column name="link" type="url"  label="Link"  />
    */

    private void  appendExport(Request request, 
                               Entry entry, StringBuffer sb)
            throws Exception {
        GregorianCalendar cal =  new GregorianCalendar(RepositoryUtil.TIMEZONE_DEFAULT);
        cal.setTime(new Date(entry.getStartDate()));
        Object[] values = entry.getTypeHandler().getValues(entry);
        int idx =0;
        appendTag(sb, TAG_BIBLIO_AUTHOR, values[idx++]);
        appendTag(sb, TAG_BIBLIO_TYPE, values[idx++]);
        appendTag(sb, TAG_BIBLIO_TITLE, entry.getName());
        appendTag(sb, TAG_BIBLIO_INSTITUTION, values[idx++]);
        if(values[idx]!=null) {
            for(String otherAuthor: StringUtil.split(values[idx].toString(),"\n", true, true)) {
                appendTag(sb, TAG_BIBLIO_AUTHOR, otherAuthor);
            }
        }
        idx++;

        appendTag(sb, TAG_BIBLIO_DATE, ""+cal.get(GregorianCalendar.YEAR));
        appendTag(sb, TAG_BIBLIO_PUBLICATION, values[idx++]);
        appendTag(sb, TAG_BIBLIO_VOLUME, values[idx++]);
        appendTag(sb, TAG_BIBLIO_NUMBER, values[idx++]);
        appendTag(sb, TAG_BIBLIO_PAGE, values[idx++]);
        appendTag(sb, TAG_BIBLIO_DOI, values[idx++]);
        appendTag(sb, TAG_BIBLIO_URL, values[idx++]);


        List<Metadata> metadataList =
            getMetadataManager().getMetadata(entry);
        if (metadataList != null) {
            boolean firstMetadata = true;
            for (Metadata metadata:metadataList) {
                if(!metadata.getType().equals("enum_tag")) {
                    continue;
                } 
                if(firstMetadata) {
                    sb.append(TAG_BIBLIO_TAG);
                    sb.append(" ");
                }
                sb.append(metadata.getAttr1());
                sb.append("\n");
                firstMetadata = false;
            }
        }
        appendTag(sb, TAG_BIBLIO_DESCRIPTION, entry.getDescription());
        sb.append("\n");
    }

    private void  appendTag(StringBuffer sb, String tag, Object value) {
        if(value == null) return;
        String s = value.toString();
        if(Utils.stringDefined(s)) {
            s = s.replaceAll("\n"," ");
            sb.append(tag);
            sb.append(" ");
            sb.append(s);
            sb.append("\n");
        }
    }




}
