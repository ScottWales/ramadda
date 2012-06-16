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


package org.ramadda.geodata.data;


import org.ramadda.repository.*;
import org.ramadda.repository.admin.*;
import org.ramadda.repository.monitor.LdmAction;

import org.ramadda.util.HtmlUtils;
import ucar.unidata.xml.XmlUtil;

import java.util.List;
import java.io.File;


/**
 * And example class to add ldap configuration options to the admin screen
 *
 */
public class LdmAdminHandler extends AdminHandlerImpl {


    /**
     * ctor. 
     */
    public LdmAdminHandler(Repository repository) {
        super(repository);
    }


    /**
     * Used to uniquely identify this admin handler
     *
     * @return unique id for this admin handler
     */
    public String getId() {
        return "ldm";
    }

    /**
     * This adds the fields into the admin Settings->Access form section
     *
     * @param blockId which section
     * @param sb form buffer to append to
     */
    @Override
    public void addToAdminSettingsForm(String blockId, StringBuffer sb) {
        //Are we in the access section
        if ( !blockId.equals(Admin.BLOCK_ACCESS)) {
            return;
        }
        sb.append(
            HtmlUtils.colspan(
                msgHeader("Enable Unidata Local Data Manager (LDM) Access"),
                2));
        String pqinsertPath = getProperty(LdmAction.PROP_LDM_PQINSERT, "");
        String ldmExtra1    = "";
        if ((pqinsertPath.length() > 0) && !new File(pqinsertPath).exists()) {
            ldmExtra1 =
                HtmlUtils.space(2)
                + HtmlUtils.span("File does not exist!",
                                HtmlUtils.cssClass(CSS_CLASS_ERROR_LABEL));
        }

        sb.append(HtmlUtils.formEntry("Path to pqinsert:",
                                      HtmlUtils.input(LdmAction.PROP_LDM_PQINSERT,
                                          pqinsertPath,
                                          HtmlUtils.SIZE_60) + ldmExtra1));
        String ldmQueue  = getProperty(LdmAction.PROP_LDM_QUEUE, "");
        String ldmExtra2 = "";
        if ((ldmQueue.length() > 0) && !new File(ldmQueue).exists()) {
            ldmExtra2 =
                HtmlUtils.space(2)
                + HtmlUtils.span("File does not exist!",
                                HtmlUtils.cssClass(CSS_CLASS_ERROR_LABEL));
        }

        sb.append(HtmlUtils.formEntry("Queue Location:",
                                      HtmlUtils.input(LdmAction.PROP_LDM_QUEUE,
                                          ldmQueue,
                                          HtmlUtils.SIZE_60) + ldmExtra2));




    }




    /**
     * apply the form submit
     *
     * @param request the request
     *
     * @throws Exception On badness
     */
    @Override
    public void applyAdminSettingsForm(Request request) throws Exception {
        getRepository().writeGlobal(request, LdmAction.PROP_LDM_PQINSERT, true);
        getRepository().writeGlobal(request, LdmAction.PROP_LDM_QUEUE, true);
    }


}
