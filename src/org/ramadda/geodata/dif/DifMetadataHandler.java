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

package org.ramadda.geodata.dif;



import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;




import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;




import java.io.File;


import java.sql.Statement;


import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class DifMetadataHandler extends MetadataHandler {

    /** _more_ */
    public static final String TYPE_ENTRY_TITLE = "dif.entry_title";

    /** _more_ */
    public static final String TYPE_DATA_SET_CITATION =
        "dif.data_set_citation";

    /** _more_ */
    public static final String TYPE_PERSONNEL = "dif.personnel";

    /** _more_ */
    public static final String TYPE_PARAMETERS = "dif.parameters";

    /** _more_ */
    public static final String TYPE_ISO_TOPIC_CATEGORY =
        "dif.iso_topic_category";

    /** _more_ */
    public static final String TYPE_KEYWORD = "dif.keyword";

    /** _more_ */
    public static final String TYPE_DATA_SET_PROGRESS =
        "dif.data_set_progress";

    /** _more_ */
    public static final String TYPE_LOCATION = "dif.location";

    /** _more_ */
    public static final String TYPE_QUALITY = "dif.quality";

    /** _more_ */
    public static final String TYPE_DATA_SET_LANGUAGE =
        "dif.data_set_language";

    /** _more_ */
    public static final String TYPE_ORIGINATING_CENTER =
        "dif.originating_center";

    /** _more_ */
    public static final String TYPE_DATA_CENTER = "dif.data_center";

    /** _more_ */
    public static final String TYPE_DISTRIBUTION = "dif.distribution";

    /** _more_ */
    public static final String TYPE_REFERENCE = "dif.reference";

    /** _more_ */
    public static final String TYPE_SUMMARY = "dif.summary";

    /** _more_ */
    public static final String TYPE_RELATED_URL = "dif.related_url";

    /** _more_ */
    public static final String TYPE_METADATA_NAME = "dif.metadata_name";

    /** _more_ */
    public static final String TYPE_METADATA_VERSION = "dif.metadata_version";

    /** _more_ */
    public static final String TYPE_ = "dif.";


    /**
     * _more_
     *
     * @param repository _more_
     *
     * @throws Exception _more_
     */
    public DifMetadataHandler(Repository repository) throws Exception {
        super(repository);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception _more_
     */
    public DifMetadataHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }




}
