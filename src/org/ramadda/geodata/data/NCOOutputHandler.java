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
import org.ramadda.repository.output.*;


import org.w3c.dom.*;

import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.grid.GridDataset;

import ucar.unidata.xml.XmlUtil;


import java.util.ArrayList;
import java.util.List;




/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class NCOOutputHandler extends OutputHandler {


    public static final String PROP_NCO = "nco.path";

    /** OPeNDAP Output Type */
    public static final OutputType OUTPUT_NCO =
        new OutputType("NCO", "nco", OutputType.TYPE_OTHER,
                       OutputType.SUFFIX_NONE, DataOutputHandler.ICON_OPENDAP, DataOutputHandler.GROUP_DATA);


    private String ncoPath;

    /**
     * ctor
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public NCOOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_NCO);
        ncoPath = getProperty(PROP_NCO, "/opt/local/bin");
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean haveNco() {
        return ncoPath != null;
    }




    /**
     * This method gets called to determine if the given entry or entries can be displays as las xml
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
        if ( !haveNco()) {
            return;
        }

        if(state.entry!=null && state.entry.isFile() && state.entry.getResource().getPath().endsWith(".nc")) {
            links.add(makeLink(request, state.entry, OUTPUT_NCO));
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
        return new Result("test", new StringBuffer(""));
    }

}
