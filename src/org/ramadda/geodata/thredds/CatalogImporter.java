/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
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

    /** _more_ */
    public RequestUrl URL_HARVESTERS_IMPORTCATALOG =
        new RequestUrl(this, "/harvester/importcatalog", "Import Catalog");

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
        //        addType(OUTPUT_CATALOG_EMBED);
    }




    /*
             links.add(new Link(request
                    .url(getHarvesterManager().URL_HARVESTERS_IMPORTCATALOG,
                        ARG_GROUP, entry.getId()), getRepository()
                            .iconUrl(ICON_CATALOG), "Import THREDDS Catalog",
                                OutputType.TYPE_FILE));


   protected String makeNewGroupForm(Request request, Entry parentEntry,
                                      String name) {
        StringBuffer sb = new StringBuffer();
        if ((parentEntry != null) && request.getUser().getAdmin()) {
            sb.append(
                request.form(
                    getHarvesterManager().URL_HARVESTERS_IMPORTCATALOG));
                        sb.append(HtmlUtil.hidden(ARG_GROUP, parentEntry.getId()));
        }
        return sb.toString();
    }




     */

    /*    public Result processImportCatalog(Request request) throws Exception {

        Entry group = getEntryManager().findGroup(request);
        if ( !request.exists(ARG_CATALOG)) {
            StringBuffer sb = new StringBuffer();
            sb.append(
                request.form(
                    getHarvesterManager().URL_HARVESTERS_IMPORTCATALOG));
            sb.append(HtmlUtil.hidden(ARG_GROUP, group.getId()));
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



        boolean      recurse     = request.get(ARG_RECURSE, false);
        boolean      addMetadata = request.get(ATTR_ADDMETADATA, false);
        boolean addShortMetadata = request.get(ATTR_ADDSHORTMETADATA, false);
        boolean      download    = request.get(ARG_RESOURCE_DOWNLOAD, false);
        StringBuffer sb          = new StringBuffer();
        //        sb.append(getEntryManager().makeEntryHeader(request, group));
        sb.append("<p>");
        String catalog = request.getString(ARG_CATALOG, "").trim();
        sb.append(request.form(URL_HARVESTERS_IMPORTCATALOG));
        sb.append(HtmlUtil.hidden(ARG_GROUP, group.getId()));
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
            sb = new StringBuffer();
            sb.append(msg("Catalog is being harvested"));
            sb.append(HtmlUtil.p());
            org.ramadda.geodata.thredds.CatalogHarvester harvester =
                new org.ramadda.geodata.thredds.CatalogHarvester(
                    getRepository(), group, catalog, request.getUser(),
                    recurse, download);
            harvester.setAddMetadata(addMetadata);
            harvester.setAddShortMetadata(addShortMetadata);
            harvesters.add(harvester);
            Misc.run(harvester, "run");
            //            makeHarvestersList(request, (List<Harvester>)Misc.newList(harvester),  sb);
            //            return  getEntryManager().addEntryHeader(request, group,
            //                                                     new Result("",sb));
            return getEntryManager().addEntryHeader(request, group,
                    new Result(request.url(URL_HARVESTERS_LIST, ARG_MESSAGE,
                                           "Catalog is being harvested")));
        }


        Result result = getEntryManager().addEntryHeader(request, group,
                            new Result(request.url(URL_HARVESTERS_LIST)));
        return result;

    }

    */


}
