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

package org.ramadda.plugins.pasteit;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;

import java.io.*;


/**
 *
 *
 */
public class PasteitEntryTypeHandler extends GenericTypeHandler {

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

    public void addSpecialToEntryForm(Request request, StringBuffer sb,
                                      Entry entry)
            throws Exception {
        super.addSpecialToEntryForm(request, sb, entry);
        //Only on a new entry
        if(entry != null) {
            return;
        }
        sb.append(formEntry(request, msgLabel("File suffix"),
                            HtmlUtil.input(ARG_SUFFIX, "txt", 10)));

        sb.append(
                  formEntryTop(request,
                               msgLabel("Paste text"),
                               HtmlUtil.textArea(
                                                 ARG_TEXT, "", 50, 60)));
    }

    public String getUploadedFile(Request request) {
        try {
            String name = request.getString(ARG_NAME,"").trim();
            if(name.length()==0) name = "file";
            if(name.indexOf(".")<0) {
                name = name + "." + request.getString(ARG_SUFFIX,"");
            }
            File f = getStorageManager().getTmpFile(request, name);
            FileOutputStream out = getStorageManager().getFileOutputStream(f);
            out.write(request.getString(ARG_TEXT,"").getBytes());
            out.flush();
            out.close();
            return f.toString();
        } catch(Exception exc) {
            throw new RuntimeException(exc);

        }
    }

}
