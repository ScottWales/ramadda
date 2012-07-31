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

package org.ramadda.geodata.gps;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.io.*;
import java.io.File;

import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;


/**
 *
 *
 * @author Jeff McWhirter
 */
public class SolutionTypeHandler extends GenericTypeHandler {

    /** _more_ */
    public static final String TYPE_SOLUTION = "gps_solution";

    /** _more_ */
    private static int COLCNT = 0;

    /** _more_ */
    public static final int IDX_SITE_CODE = COLCNT++;

    /** _more_ */
    public static final int IDX_UTM_X = COLCNT++;

    /** _more_ */
    public static final int IDX_UTM_Y = COLCNT++;

    /** _more_ */
    public static final int IDX_ITRF_X = COLCNT++;

    /** _more_ */
    public static final int IDX_ITRF_Y = COLCNT++;

    /** _more_ */
    public static final int IDX_ITRF_Z = COLCNT++;

    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception On badness
     */
    public SolutionTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }



}
