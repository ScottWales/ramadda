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
            entry  = getEntryManager().findEntryFromName(request, suffix,
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
