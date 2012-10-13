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

package org.ramadda.plugins.doi;




import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;

import org.ramadda.repository.metadata.*;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;




import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.CatalogUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;




import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import visad.Unit;
import visad.UnitException;


import visad.data.units.NoSuchUnitException;

import visad.jmet.MetUnits;

import java.io.File;


import java.sql.Statement;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class DoiMetadataHandler extends MetadataHandler {



    /** _more_ */
    public static final String TYPE_DOI = "doi";


    /**
     * _more_
     *
     * @param repository _more_
     *
     * @throws Exception _more_
     */
    public DoiMetadataHandler(Repository repository) throws Exception {
        super(repository);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception _more_
     */
    public DoiMetadataHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }


    public String[] getHtml(Request request, Entry entry, Metadata metadata)
        throws Exception {
        return new String[]{"DOI",getHref(metadata.getAttr1())};
    }

    public static String getUrl(String doi) {
        return doi.replace("doi:", "http://dx.doi.org/");
    }

    public static String getHref(String doi) {
        return HtmlUtils.href(doi.replace("doi:", "http://dx.doi.org/"), doi);
    }


}
