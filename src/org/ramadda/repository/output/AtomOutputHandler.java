/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.repository.output;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.type.*;
import org.ramadda.repository.metadata.*;

import org.ramadda.util.AtomUtil;


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
 * Class SqlUtil _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class AtomOutputHandler extends OutputHandler {

    /** _more_          */
    public static final String MIME_ATOM = "application/atom+xml";

    /** _more_ */
    SimpleDateFormat sdf =
        new SimpleDateFormat("EEE dd, MMM yyyy HH:mm:ss Z");

    /** _more_ */
    public static final OutputType OUTPUT_ATOM = new OutputType("ATOM Feed",
                                                     "atom",
                                                     OutputType.TYPE_NONHTML,
                                                     "", ICON_ATOM);



    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public AtomOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_ATOM);
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
                    request, state.getEntry(), OUTPUT_ATOM,
                    "/" + IOUtil.stripExtension(state.getEntry().getName())
                    + ".xml"));
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
        sb.append(AtomUtil.openFeed());
        sb.append("\n");
        sb.append(AtomUtil.makeTitle(parentEntry.getName()
                                     + " ATOM Site Feed"));
        sb.append("\n");
        sb.append(
            AtomUtil.makeLink(
                AtomUtil.REL_SELF,
                getRepository().absoluteUrl(request.getUrl())));
        sb.append("\n");
        for (Entry entry : entries) {
            List<AtomUtil.Link> links = new ArrayList<AtomUtil.Link>();
            String selfUrl =
                repository.absoluteUrl(request.url(repository.URL_ENTRY_SHOW,
                    ARG_ENTRYID, entry.getId()));
            links.add(new AtomUtil.Link("text/html", selfUrl,  "Web page"));
            String resource = entry.getResource().getPath();
            if (ImageUtils.isImage(resource)) {
                String imageUrl = repository.absoluteUrl(
                                      HtmlUtil.url(
                                          getRepository().URL_ENTRY_GET
                                          + entry.getId()
                                          + IOUtil.getFileExtension(
                                              resource), ARG_ENTRYID,
                                                  entry.getId()));
                links.add(new AtomUtil.Link(AtomUtil.REL_IMAGE, imageUrl,
                                            "Image"));
            }
            List<Service> services =
                entry.getTypeHandler().getServices(request, entry);
            for (Service service : services) {
                String url  = service.getUrl();
                String type = service.getType();
                String name = service.getName();

                links.add(new AtomUtil.Link(type, url, name));
            }

            List<String> urls = new ArrayList<String>();
            getMetadataManager().getThumbnailUrls(request,  entry,urls);

            for(String url: urls) {
                links.add(new AtomUtil.Link("thumbnail", getRepository().absoluteUrl(url), "Thumbnail"));
            }

            Document doc = XmlUtil.getDocument("<metadata></metadata>");
            Element root = doc.getDocumentElement();
            List<Metadata> metadataList = getMetadataManager().getMetadata(entry);
            List<MetadataHandler> metadataHandlers =
                repository.getMetadataManager().getMetadataHandlers();
            for (Metadata metadata : metadataList) {
                for (MetadataHandler metadataHandler : metadataHandlers) {
                    if (metadataHandler.canHandle(metadata)) {
                        if(!metadataHandler.addMetadataToXml(request,
                                                             "dif", entry,
                                                             metadata, doc, root)){
                            metadataHandler.addMetadataToXml(request,
                                                             "atom", entry,
                                                             metadata, doc, root);

                        }
                        break;
                    }
                }
            }
            StringBuffer extra =new StringBuffer();

            if(entry.hasAreaDefined()) {
                extra.append("<georss:box>" + entry.getSouth() + " " + entry.getWest() +" "  +
                             entry.getNorth() +" " + entry.getEast() +"</georss:box>\n");
            }


            extra.append(XmlUtil.toString(root));

            sb.append(AtomUtil.makeEntry(entry.getName(), 
                                         selfUrl,
                                         new Date(entry.getEndDate()),
                                         entry.getDescription(), null,
                                         links, extra.toString()));
        }
        sb.append(AtomUtil.closeFeed());
        return new Result("", sb, MIME_ATOM);
    }


}
