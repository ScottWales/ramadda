/*
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
 */

package org.ramadda.geodata.thredds;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.data.*;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.CatalogUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


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
public class CatalogImporter extends OutputHandler {

    public static final String ARG_CATALOG = "catalog";

    /** _more_ */
    public static final OutputType OUTPUT_CATALOG_IMPORT =
        new OutputType("Import THREDDS Catalog", "thredds.import.catalog",
                       OutputType.TYPE_FILE,
                       "", CatalogOutputHandler.ICON_CATALOG);

    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public CatalogImporter(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_CATALOG_IMPORT);
    }


    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if (state.group != null) {
            links.add(makeLink(request, state.group, OUTPUT_CATALOG_IMPORT));
            Link hr = new Link(true);
            hr.setLinkType(OutputType.TYPE_FILE);
            links.add(hr);
        }
    }


    public Result outputGroup(final Request request, OutputType outputType,
                              final Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        if ( !request.exists(ARG_CATALOG)) {
            StringBuffer sb = new StringBuffer();
            sb.append(request.form(getRepository().URL_ENTRY_SHOW,""));
            sb.append(HtmlUtil.hidden(ARG_GROUP, group.getId()));
            sb.append(HtmlUtil.hidden(ARG_OUTPUT, OUTPUT_CATALOG_IMPORT.getId()));
            sb.append(msgHeader("Import a THREDDS catalog"));
            sb.append(HtmlUtil.formTable());
            sb.append(HtmlUtil.formEntry(msgLabel("URL"),
                                         HtmlUtil.input(ARG_CATALOG, BLANK,
                                             HtmlUtil.SIZE_70)));

            sb.append(
                HtmlUtil.formEntry(
                    "",
                    HtmlUtil.checkbox(ARG_RECURSE, "true", false)
                    + HtmlUtil.space(1) + msg("Recurse") + HtmlUtil.space(1)
                    + HtmlUtil.checkbox(ATTR_ADDMETADATA, "true", false)
                    + HtmlUtil.space(1) + msg("Add full metadata")
                    + HtmlUtil.space(1)
                    + HtmlUtil.checkbox(ATTR_ADDSHORTMETADATA, "true", false)
                    + HtmlUtil.space(1)
                    + msg("Just add spatial/temporal metadata")
                    + HtmlUtil.space(1)
                    + HtmlUtil.checkbox(ARG_RESOURCE_DOWNLOAD, "true", false)
                    + HtmlUtil.space(1) + msg("Download URLS")));
            sb.append(HtmlUtil.formEntry("", HtmlUtil.submit(msg("Go"))));
            sb.append(HtmlUtil.formTableClose());
            sb.append(HtmlUtil.formClose());

            return getEntryManager().makeEntryEditResult(request, group,
                    "Catalog Import", sb);
        }



        boolean  recurse     = request.get(ARG_RECURSE, false);
        boolean  addMetadata = request.get(ATTR_ADDMETADATA, false);
        boolean  addShortMetadata = request.get(ATTR_ADDSHORTMETADATA, false);
        boolean  download    = request.get(ARG_RESOURCE_DOWNLOAD, false);
        StringBuffer sb          = new StringBuffer();
        //        sb.append(getEntryManager().makeEntryHeader(request, group));
        sb.append("<p>");
        final String catalog = request.getString(ARG_CATALOG, "").trim();
        sb.append(request.form(getRepository().URL_ENTRY_SHOW,""));
        sb.append(HtmlUtil.hidden(ARG_GROUP, group.getId()));
        sb.append(HtmlUtil.hidden(ARG_OUTPUT, OUTPUT_CATALOG_IMPORT.getId()));
        sb.append(HtmlUtil.submit(msgLabel("Import catalog")));
        sb.append(HtmlUtil.space(1));
        sb.append(HtmlUtil.input(ARG_CATALOG, catalog, " size=\"75\""));

        sb.append(HtmlUtil.checkbox(ARG_RECURSE, "true", recurse));
        sb.append(HtmlUtil.space(1));
        sb.append(msg("Recurse"));



        sb.append(HtmlUtil.checkbox(ATTR_ADDMETADATA, "true", addMetadata));
        sb.append(HtmlUtil.space(1));
        sb.append(msg("Add Metadata"));

        sb.append(HtmlUtil.space(1));
        sb.append(HtmlUtil.checkbox(ATTR_ADDSHORTMETADATA, "true",
                                    addShortMetadata));
        sb.append(HtmlUtil.space(1));
        sb.append(msg("Just add spatial/temporal metadata"));


        sb.append(HtmlUtil.space(1));
        sb.append(HtmlUtil.checkbox(ARG_RESOURCE_DOWNLOAD, "true", download));
        sb.append(HtmlUtil.space(1));
        sb.append(msg("Download URLs"));
        sb.append("</form>");

        if (catalog.length() > 0) {
            ActionManager.Action action  = new ActionManager.Action() {
                    public void run(Object actionId) throws Exception {
                        boolean recurse     = request.get(ARG_RECURSE, false);
                        boolean addMetadata = request.get(ATTR_ADDMETADATA, false);
                        boolean addShortMetadata = request.get(ATTR_ADDSHORTMETADATA, false);
                        boolean download    = request.get(ARG_RESOURCE_DOWNLOAD, false);
                        CatalogHarvester harvester =
                            new CatalogHarvester(
                                                 getRepository(), group, catalog, request.getUser(),
                                                 recurse, download,actionId);
                        harvester.setAddMetadata(addMetadata);
                        harvester.setAddShortMetadata(addShortMetadata);
                        harvester.run();
                    }
                };
            String href =
                HtmlUtil.href(request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                               group), "Continue");
            return getActionManager().doAction(request, action, "Importing Catalog",
                                               "Continue: " + href);
        }
        return new Result("", new StringBuffer("Humm, probably shouldn't get here"));
    }



}
