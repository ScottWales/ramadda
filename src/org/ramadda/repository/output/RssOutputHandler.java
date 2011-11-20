/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

import org.ramadda.util.RssUtil;


import org.w3c.dom.*;

import ucar.unidata.sql.SqlUtil;

import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringBufferCollection;


import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.io.File;



import java.net.*;



import java.text.SimpleDateFormat;

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
public class RssOutputHandler extends OutputHandler {

    /** _more_ */
    public static final String ATTR_RSS_VERSION = "version";

    /** _more_ */
    public static final String TAG_RSS_RSS = "rss";

    /** _more_ */
    public static final String TAG_RSS_GEOLAT = "georss:lat";

    /** _more_ */
    public static final String TAG_RSS_GEOLON = "georss:lon";

    /** _more_ */
    public static final String TAG_RSS_GEOBOX = "georss:box";

    /** _more_ */
    public static final String TAG_RSS_LINK = "link";

    /** _more_ */
    public static final String TAG_RSS_GUID = "guid";

    /** _more_ */
    public static final String TAG_RSS_CHANNEL = "channel";

    /** _more_ */
    public static final String TAG_RSS_ITEM = "item";

    /** _more_ */
    public static final String TAG_RSS_TITLE = "title";

    /** _more_ */
    public static final String TAG_RSS_PUBDATE = "pubDate";

    /** _more_ */
    public static final String TAG_RSS_DESCRIPTION = "description";


    /** _more_ */
    public static final String ICON_RSS = "ramadda.icon.rss";

    /** _more_ */
    public static String MIME_RSS = "application/rss+xml";


    /** _more_ */
    SimpleDateFormat rssSdf =
        new SimpleDateFormat("EEE dd, MMM yyyy HH:mm:ss Z");

    /** _more_ */
    public static final OutputType OUTPUT_RSS_FULL =
        new OutputType("Full RSS Feed", "rss.full",
                       OutputType.TYPE_FEEDS | OutputType.TYPE_TOOLBAR, "",
                       ICON_RSS);

    /** _more_ */
    public static final OutputType OUTPUT_RSS_SUMMARY =
        new OutputType("RSS Feed", "rss.summary",
                       OutputType.TYPE_FEEDS | OutputType.TYPE_FORSEARCH
                       | OutputType.TYPE_TOOLBAR, "", ICON_RSS);


    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public RssOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_RSS_FULL);
        addType(OUTPUT_RSS_SUMMARY);
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
            links.add(
                makeLink(
                    request, state.getEntry(), OUTPUT_RSS_SUMMARY,
                    "/" + IOUtil.stripExtension(state.getEntry().getName())
                    + ".rss"));
        }
    }





    /**
     * _more_
     *
     * @param output _more_
     *
     * @return _more_
     */
    public String getMimeType(OutputType output) {
        if (output.equals(OUTPUT_RSS_FULL)
                || output.equals(OUTPUT_RSS_SUMMARY)) {
            return repository.getMimeTypeFromSuffix(".rss");
        } else {
            return super.getMimeType(output);
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
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        entries.addAll(subGroups);
        return outputEntries(request, group, entries);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);
        return outputEntries(request, entry, entries);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param parentEntry _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Result outputEntries(Request request, Entry parentEntry,
                                 List<Entry> entries)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(XmlUtil.XML_HEADER + "\n");
        sb.append(XmlUtil.openTag(RssUtil.TAG_RSS,
                                  XmlUtil.attrs(ATTR_RSS_VERSION, "2.0")));
        sb.append(XmlUtil.openTag(RssUtil.TAG_CHANNEL));
        sb.append(XmlUtil.tag(RssUtil.TAG_TITLE, "", parentEntry.getName()));
        StringBufferCollection sbc    = new StringBufferCollection();
        OutputType             output = request.getOutput();
        request.put(ARG_OUTPUT, OutputHandler.OUTPUT_HTML);
        for (Entry entry : entries) {
            StringBuffer extra    = new StringBuffer();
            String       resource = entry.getResource().getPath();
            if (ImageUtils.isImage(resource)) {
                String imageUrl = request.getAbsoluteUrl(
                                      HtmlUtil.url(
                                          getRepository().URL_ENTRY_GET
                                          + entry.getId()
                                          + IOUtil.getFileExtension(
                                              resource), ARG_ENTRYID,
                                                  entry.getId()
                /*,                                                                    ARG_IMAGEWIDTH, "75"*/
                ));
                extra.append(HtmlUtil.br());
                extra.append(HtmlUtil.img(imageUrl));
            }

            sb.append(XmlUtil.openTag(RssUtil.TAG_ITEM));
            sb.append(
                XmlUtil.tag(
                    RssUtil.TAG_PUBDATE, "",
                    rssSdf.format(new Date(entry.getStartDate()))));
            sb.append(XmlUtil.tag(RssUtil.TAG_TITLE, "", entry.getName()));
            String url =
                request.getAbsoluteUrl(request.url(repository.URL_ENTRY_SHOW,
                    ARG_ENTRYID, entry.getId()));
            sb.append(XmlUtil.tag(RssUtil.TAG_LINK, "", url));
            sb.append(XmlUtil.tag(RssUtil.TAG_GUID, "", url));

            sb.append(XmlUtil.openTag(RssUtil.TAG_DESCRIPTION, ""));
            if (output.equals(OUTPUT_RSS_FULL)) {
                XmlUtil.appendCdata(
                    sb,
                    entry.getTypeHandler().getEntryContent(
                        entry, request, true, false).toString());
            } else {
                XmlUtil.appendCdata(
                    sb, entry.getTypeHandler().getEntryText(entry) + extra);
            }

            sb.append(XmlUtil.closeTag(RssUtil.TAG_DESCRIPTION));
            if (entry.hasLocationDefined()) {
                sb.append(XmlUtil.tag(RssUtil.TAG_GEOLAT, "",
                                      "" + entry.getSouth()));
                sb.append(XmlUtil.tag(RssUtil.TAG_GEOLON, "",
                                      "" + entry.getEast()));
            } else if (entry.hasAreaDefined()) {
                //For now just include the southeast point
                sb.append(XmlUtil.tag(RssUtil.TAG_GEOBOX, "",
                                      entry.getSouth() + ","
                                      + entry.getWest() + ","
                                      + entry.getNorth() + ","
                                      + entry.getEast()));
            }


            sb.append(XmlUtil.closeTag(RssUtil.TAG_ITEM));
        }

        request.put(ARG_OUTPUT, output);
        sb.append(XmlUtil.closeTag(RssUtil.TAG_CHANNEL));
        sb.append(XmlUtil.closeTag(RssUtil.TAG_RSS));
        Result result = new Result("Query Results", sb,
                                   getMimeType(OUTPUT_RSS_SUMMARY));




        return result;

    }


}
