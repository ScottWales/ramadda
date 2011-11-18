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


import org.w3c.dom.*;

import ucar.unidata.sql.SqlUtil;
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



/**
 * Produces a wget script to download files
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class WgetOutputHandler extends OutputHandler {


    /** _more_ */
    public static final OutputType OUTPUT_WGET =
        new OutputType("Wget Script", "wget.wget", OutputType.TYPE_FILE, "",
                       ICON_FETCH);


    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public WgetOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_WGET);
    }




    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public AuthorizationMethod getAuthorizationMethod(Request request) {
        return AuthorizationMethod.AUTH_HTTP;
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
        if (state.entry != null) {
            if (state.entry.getResource().isUrl()
                    || getAccessManager().canDownload(request, state.entry)) {
                links.add(
                    makeLink(
                        request, state.entry, OUTPUT_WGET,
                        "/" + IOUtil.stripExtension(state.entry.getName())
                        + ".sh"));
            }
        } else {
            boolean ok = false;
            for (Entry child : state.getAllEntries()) {
                if (child.getResource().isUrl()
                        || getAccessManager().canDownload(request, child)) {
                    ok = true;
                    break;
                }
            }
            if (ok) {
                if (state.group != null) {
                    links.add(
                        makeLink(
                            request, state.group, OUTPUT_WGET,
                            "/"
                            + IOUtil.stripExtension(state.group.getName())
                            + ".sh"));
                } else {
                    links.add(makeLink(request, state.group, OUTPUT_WGET));
                }
            }
        }
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
        return outputGroup(request, outputType, null, null,
                           (List<Entry>) Misc.newList(entry));
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
        StringBuffer sb = new StringBuffer();
        for (Entry entry : entries) {
            if (entry.getResource().isUrl()) {
                sb.append("wget \"" + entry.getResource().getPath() + "\"");
                sb.append("\n");
                continue;
            } else if ( !getAccessManager().canDownload(request, entry)) {
                continue;
            }
            String tail = getStorageManager().getFileTail(entry);
            String path = request.getAbsoluteUrl(
                              getEntryManager().getEntryResourceUrl(
                                  request, entry));
            sb.append("wget -O \"" + tail + "\" \"" + path + "\"");
            sb.append("\n");
        }

        return new Result("", sb, getMimeType(OUTPUT_WGET));

    }




    /**
     * _more_
     *
     * @param output _more_
     *
     * @return _more_
     */
    public String getMimeType(OutputType output) {
        return "application/x-sh";
    }



}
