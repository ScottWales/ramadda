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

package org.ramadda.repository.output;


import java.util.List;

import org.ramadda.repository.Entry;
import org.ramadda.repository.Link;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Result;
import org.ramadda.repository.auth.AuthorizationMethod;
import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;



/**
 * Produces a wget script to download files
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class WgetOutputHandler extends OutputHandler {


    /** The WGET output type */
    public static final OutputType OUTPUT_WGET =
        new OutputType("Wget Script", "wget.wget", OutputType.TYPE_FILE, "",
                       ICON_FETCH);


    /**
     * Create a wget output handler
     *
     * @param repository  the repository
     * @param element     the XML definition
     * @throws Exception  problem creating the handler
     */
    public WgetOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_WGET);
    }




    /**
     * Get the authorization method
     *
     * @param request  the request
     *
     * @return  the authorization method
     */
    public AuthorizationMethod getAuthorizationMethod(Request request) {
        return AuthorizationMethod.AUTH_HTTP;
    }

    /**
     * Get the entry links
     *
     * @param request  the Request
     * @param state    the State
     * @param links    the list of links to add to
     *
     * @throws Exception  problem generating links
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
                        + "_wget.sh"));
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
                            + "_wget.sh"));
                } else {
                    links.add(makeLink(request, state.group, OUTPUT_WGET));
                }
            }
        }
    }




    /**
     * Output the entry
     *
     * @param request   the request
     * @param outputType  the output type
     * @param entry     the entry
     *
     * @return the Result
     *
     * @throws Exception on badness
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        request.setReturnFilename(IOUtil.stripExtension(entry.getName())+"_wget.sh");
        return outputGroup(request, outputType, null, null,
                           (List<Entry>) Misc.newList(entry));
    }


    /**
     * Output a group of entries
     *
     * @param request    the Request
     * @param outputType the output type
     * @param group      the group (may be null)
     * @param subGroups  the list of subgroups (may be null)
     * @param entries    the list of entries
     *
     * @return  the result
     *
     * @throws Exception  problem creating the script
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        
        if(group != null && group.isDummy()) {
            request.setReturnFilename("Search_Results_wget.sh");
        }

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
     * Get the MIME type for this output handler
     *
     * @param output  the output type
     *
     * @return  the MIME type
     */
    public String getMimeType(OutputType output) {
        return "application/x-sh";
    }



}
