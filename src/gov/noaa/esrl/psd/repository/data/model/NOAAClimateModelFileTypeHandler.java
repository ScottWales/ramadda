/*
* Copyright 2008-2013 Jeff McWhirter/ramadda.org
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
package gov.noaa.esrl.psd.repository.data.model;


import org.ramadda.geodata.model.ClimateModelFileTypeHandler;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;

import org.w3c.dom.Element;


/**
 * Class description
 *
 *
 */
public class NOAAClimateModelFileTypeHandler extends ClimateModelFileTypeHandler {

    /** type identifier */
    public final static String TYPE_NOAA_FACTS_CLIMATE_MODELFILE =
        "noaa_facts_climate_modelfile";

    /**
     * Create a new NOAAClimateModelFileTypeHandler
     *
     * @param repository   the Repository
     * @param entryNode    the XML definition
     *
     * @throws Exception  problem creating the handler
     */
    public NOAAClimateModelFileTypeHandler(Repository repository,
                                           Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

}
