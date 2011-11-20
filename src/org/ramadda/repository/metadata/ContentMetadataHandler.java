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

package org.ramadda.repository.metadata;


import org.ramadda.repository.*;


import org.w3c.dom.*;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;


import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.awt.Image;


import java.io.*;
import java.io.File;
import java.io.FileInputStream;

import java.net.URL;
import java.net.URLConnection;


import java.sql.Statement;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class ContentMetadataHandler extends MetadataHandler {


    /** _more_ */
    public static final String TYPE_THUMBNAIL = "content.thumbnail";

    /** _more_ */
    public static final String TYPE_ATTACHMENT = "content.attachment";

    /** _more_ */
    public static final String TYPE_PAGESTYLE = "content.pagestyle";

    /** _more_ */
    public static final String TYPE_LOGO = "content.logo";

    /** _more_ */
    public static final String TYPE_JYTHON = "content.jython";

    /** _more_ */
    public static final String TYPE_CONTACT = "content.contact";

    /** _more_ */
    public static final String TYPE_SORT = "content.sort";

    /** _more_ */
    public static final String TYPE_TIMEZONE = "content.timezone";

    /** _more_ */
    public static final String TYPE_ALIAS = "content.alias";


    /**
     * _more_
     *
     * @param repository _more_
     *
     * @throws Exception _more_
     */
    public ContentMetadataHandler(Repository repository) throws Exception {
        super(repository);
    }


}
