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

package org.ramadda.geodata.fits;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;
import org.w3c.dom.*;

import java.util.List;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WmsUtil;
import ucar.unidata.xml.XmlUtil;




/**
 *
 *
 */
public class FitsOutputHandler extends OutputHandler {

    /** _more_ */
    public static final OutputType OUTPUT_VIEWER = new OutputType("FITS Viewer",
                                                                  "fits.viewer",
                                                                  OutputType.TYPE_VIEW,"","/fits/fits.gif");


    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public FitsOutputHandler(Repository repository, Element element)
        throws Exception {
        super(repository, element);
        addType(OUTPUT_VIEWER);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
        throws Exception {
        if (state.entry != null) {
            if (state.entry.getType().equals("fits_data")) {
                links.add(makeLink(request, state.entry, OUTPUT_VIEWER));
            }
        }
    }
    
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
        throws Exception {
        StringBuffer sb = new StringBuffer();
        String fileUrl = getEntryManager().getEntryResourceUrl(request, entry, false);
        //TODO: set the path right
        sb.append("<applet archive=\"/repository/fits/fits1.3.jar\" code=\"eap.fitsbrowser.BrowserApplet\" width=700 height=700 ><param name=\"FILE\" value=\"" + fileUrl +"\">Your browser is ignoring the applet tag</applet>");
        return new Result("", sb);
    }



}
