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

package org.ramadda.geodata.fieldproject;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.type.*;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.HtmlUtil;


import java.io.*;
import java.util.Hashtable;
import java.util.regex.*;


/**
 * Provides a top-level API 
 *
 */
public class FieldProjectApiHandler extends RepositoryManager implements RequestHandler {

    public static final String OPUS_TITLE = "Add OPUS";

    public static final String URL_ADDOPUS = "/fieldproject/addopus";
    public static final String ARG_OPUS = "opus";


    /**
     * ctor
     *
     * @param repository the repository
     * @param node xml from api.xml
     * @param props propertiesn
     *
     * @throws Exception on badness
     */
    public FieldProjectApiHandler(Repository repository, Element node,
                             Hashtable props)
            throws Exception {
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
    public Result processAddOpus(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        if(request.isAnonymous()) {
            sb.append(getRepository().showDialogError("You need to be logged in to add OPUS"));
            return new Result(OPUS_TITLE,sb);
        }
        if(request.exists(ARG_OPUS)) {
            //Look for:
            //            FILE: 7655b430-7c46-4324-924f-57ec6c2075b5.rinex OP1330950241570
            String opus = request.getString(ARG_OPUS,"");
            String rinexEntryId  = StringUtil.findPattern(opus, "FILE: *([^\\.]+).rinex");
            if (rinexEntryId ==null) {
                sb.append(getRepository().showDialogError("Could not find FILE name in the given OPUS"));
                return  processOpusForm(request,sb);
            }
            Entry rinexEntry = getEntryManager().getEntry(request, rinexEntryId);
            if(rinexEntry == null) {
                sb.append(getRepository().showDialogError("Could not find original RINEX entry"));
                return  processOpusForm(request,sb);
            }
            Entry parentEntry = rinexEntry.getParentEntry();
            //Look for the OPUS sibling folder
            for(Entry child: getEntryManager().getChildrenGroups( request, parentEntry)) {
                if(child.getName().toLowerCase().equals("opus")) {
                    parentEntry = child;
                    break;
                }
            }

            if (!getAccessManager().canDoAction(request, parentEntry, Permission.ACTION_NEW)) {
                sb.append(getRepository().showDialogError("You do not have permission to add to:" + parentEntry.getName()));
                return new Result(OPUS_TITLE,sb);
            }

            String opusFileName = IOUtil.stripExtension(rinexEntry.getName())+".opus";
            //Write the text out
            File             f = getStorageManager().getTmpFile(request,
                                                                opusFileName);
            FileOutputStream out = getStorageManager().getFileOutputStream(f);
            out.write(opus.getBytes());
            out.flush();
            out.close();
            f = getStorageManager().copyToStorage(request, f,
                                                  f.getName());

            TypeHandler typeHandler =
                getRepository().getTypeHandler("project_gps_opus");

            Entry newEntry = getEntryManager().addFileEntry(request, f,
                                                            parentEntry, opusFileName, request.getUser(),
                                                            typeHandler, null);

            //If we figured out location from the opus file then set the rinex entry location
            if(newEntry.hasLocationDefined() && !rinexEntry.hasLocationDefined()) {
                rinexEntry.setLocation(newEntry.getLatitude(),newEntry.getLongitude(),newEntry.getAltitude());
                if (getAccessManager().canDoAction(request, rinexEntry,
                                                   Permission.ACTION_EDIT)) {
                    getEntryManager().storeEntry(rinexEntry);
                }
            }

            sb.append(HtmlUtil.p());
            sb.append("OPUS entry created: ");
            sb.append(
                      HtmlUtil.href(
                                    HtmlUtil.url(
                                                 getRepository().URL_ENTRY_SHOW.toString(),
                                                 new String[] { ARG_ENTRYID,
                                                                newEntry.getId() }), newEntry
                                    .getName()));

            getRepository().addAuthToken(request);
            getRepository().getAssociationManager().addAssociation(
                                                                   request, newEntry, rinexEntry, "generated rinex",
                                                                   "opus generated from");
            return new Result(OPUS_TITLE,sb);
        }

        return  processOpusForm(request,sb);
    }


    public Result processOpusForm(Request request, StringBuffer sb ) throws Exception {
        String       base   = getRepository().getUrlBase();
        String formUrl = base + URL_ADDOPUS;
        sb.append(HtmlUtil.p());
        sb.append("Enter the OPUS solution text below");
        sb.append(HtmlUtil.formPost(formUrl));
        sb.append(HtmlUtil.submit("Add OPUS Solution"));
        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.textArea(ARG_OPUS,"",20,70));
        sb.append(HtmlUtil.br());
        sb.append(HtmlUtil.submit("Add OPUS Solution"));
        sb.append(HtmlUtil.formClose());
        return new Result(OPUS_TITLE,sb);
    }



}
