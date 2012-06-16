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

package org.ramadda.plugins.pasteit;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;

import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.IOUtil;

import java.io.*;


/**
 *
 *
 */
public class PasteitEntryTypeHandler extends GenericTypeHandler {

    /** _more_          */
    public static final String ARG_SUFFIX = "suffix";

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public PasteitEntryTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void addSpecialToEntryForm(Request request, StringBuffer sb,
                                      Entry entry)
            throws Exception {
        super.addSpecialToEntryForm(request, sb, entry);
        //Only on a new entry
        if (entry != null) {
            return;
        }
        sb.append(formEntry(request, msgLabel("File suffix"),
                            HtmlUtils.input(ARG_SUFFIX, "txt", 10)));

        sb.append(formEntryTop(request, msgLabel("Paste text"),
                               HtmlUtils.textArea(ARG_TEXT, "", 50, 60)));
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public String getUploadedFile(Request request) {
        try {
            String name = request.getString(ARG_NAME, "").trim();
            if (name.length() == 0) {
                name = "file";
            }
            if (name.indexOf(".") < 0) {
                name = name + "." + request.getString(ARG_SUFFIX, "");
            }
            File             f = getStorageManager().getTmpFile(request,
                                     name);
            FileOutputStream out = getStorageManager().getFileOutputStream(f);
            out.write(request.getString(ARG_TEXT, "").getBytes());
            out.flush();
            out.close();
            return f.toString();
        } catch (Exception exc) {
            throw new RuntimeException(exc);

        }
    }

}
