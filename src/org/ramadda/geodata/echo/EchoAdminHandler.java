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

package org.ramadda.geodata.echo;


import org.w3c.dom.*;

import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.repository.type.*;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.xml.XmlUtil;



import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class EchoAdminHandler extends AdminHandlerImpl {

    /**
     * _more_
     *
     *
     * @throws Exception _more_
     */
    public EchoAdminHandler() {}


    public String getId() {
        return "echo";
    }


    /**
     * _more_
     *
     * @param repository _more_
     *
     * @throws Exception _more_
     */
    public void setRepository(Repository repository) throws Exception {
        super.setRepository(repository);
    }

    public static final String PROP_ACTIVE = "echo.active";
    public static final String PROP_FTP_PATH = "echo.ftp.path";
    public static final String PROP_FTP_USER = "echo.ftp.user";
    //    public static final String PROP_FTP_PATH = "echo.ftp.path";



    public void addToSettingsForm(String blockId, StringBuffer sb) {
        if(!Admin.BLOCK_ACCESS.equals(blockId)) return;

        sb.append("<tr>");
        sb.append(HtmlUtil.colspan(getRepository().getAdmin().msgHeader("ECHO Publishing"), 2));
        sb.append("</tr>");
        sb.append(HtmlUtil.formEntry(msg("Active"), HtmlUtil.checkbox(PROP_ACTIVE,"true",
                                                                      getRepository().getProperty(PROP_ACTIVE,false))));
                                                                      
    }

    public void applySettingsForm(Request request) throws Exception {
        getRepository().writeGlobal(PROP_ACTIVE,
                                    request.get(PROP_ACTIVE, false));

    }


}
