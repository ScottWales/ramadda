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

package org.ramadda.geodata.thredds;



import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.TypeHandler;

import org.w3c.dom.*;


import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;

import java.util.Hashtable;



/**
 *
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */

public class ThreddsApiHandler extends RepositoryManager implements RequestHandler {

    /**
     * ctor
     *
     * @param repository the repository
     * @param node xml from api.xml
     * @param props properties from xml
     *
     * @throws Exception On badness
     */
    public ThreddsApiHandler(Repository repository, Element node,
                             Hashtable props)
            throws Exception {
        super(repository);
    }


    /**
     * api hook
     *
     * @param request the request
     *
     * @return the thredd catalog response
     *
     * @throws Exception On badness
     */
    public Result processThreddsRequest(Request request) throws Exception {
        CatalogOutputHandler catalogOutputHandler =
            (CatalogOutputHandler) getRepository().getOutputHandler(
                CatalogOutputHandler.OUTPUT_CATALOG.getId());

        String path   = request.getRequestPath();
        String prefix = getRepository().getUrlBase() + "/thredds";
        Entry  entry  = null;
        if (path.length() > prefix.length()) {
            String suffix = path.substring(prefix.length());
            suffix = java.net.URLDecoder.decode(suffix, "UTF-8");
            suffix = IOUtil.stripExtension(suffix);
            entry = getEntryManager().findEntryFromName(suffix,
                    request.getUser(), false);
            if (entry == null) {
                getEntryManager().fatalError(request,
                                             "Could not find entry:"
                                             + suffix);
            }
        }
        if (entry == null) {
            entry = getEntryManager().getTopGroup();
        }

        request.put(ARG_OUTPUT, CatalogOutputHandler.OUTPUT_CATALOG.getId());
        return getEntryManager().processEntryShow(request, entry);
    }
}
