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

package org.ramadda.plugins.doi;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;


import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.*;


/**
 * Provides a top-level API
 *
 */
public class DoiApiHandler extends RepositoryManager implements RequestHandler {


    public static final String ARG_DOI = "doi";

    /**
     *     ctor
     *    
     *     @param repository the repository
     *     @param node xml from api.xml
     *     @param props propertiesn
     *    
     *     @throws Exception on badness
     */
    public DoiApiHandler(Repository repository) throws Exception {
        super(repository);
    }



    /**
     * handle the request
     *
     * @param request request
     *
     * @return result
     *
     * @throws Exception on badness
     */
    public Result processDoi(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(HtmlUtils.p());
        if(!request.defined(ARG_DOI)) {
            makeForm(request, sb);
            return new Result("", sb);
        }
        
        Entry entry =  getEntryManager().getEntryFromMetadata(request, "doi", request.getString(ARG_DOI,""));
        if(entry==null) {
            sb.append("Could not find DOI:" + request.getString(ARG_DOI, ""));
            sb.append(HtmlUtils.p());
            makeForm(request, sb);
            return new Result("",sb);
        }
        request.put(ARG_ENTRYID, entry.getId());
        return getEntryManager().processEntryShow(request);
    }

    private void makeForm(Request request, StringBuffer sb) {
        String       base   = getRepository().getUrlBase();
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.form(base + "/doi"));
        sb.append(HtmlUtils.formEntry("DOI",
                                      HtmlUtils.input(ARG_DOI,request.getString(ARG_DOI, ""))));

        sb.append(HtmlUtils.formEntry("", HtmlUtils.submit("Find DOI","")));
        sb.append(HtmlUtils.formClose());
        sb.append(HtmlUtils.formTableClose());
    }

}
