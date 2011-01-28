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

package org.ramadda.plugins.ontology;


import ucar.unidata.util.IOUtil;

import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Thu, Nov 25, '10
 * @author         Enter your name here...
 */
public class OboUtil {

    /** _more_ */
    public static final String TAG_FORMAT_VERSION = "format-version";

    /** _more_ */
    public static final String TAG_DATA_VERSION = "data-version";

    /** _more_ */
    public static final String TAG_DATE = "date";

    /** _more_ */
    public static final String TAG_SUBSETDEF = "subsetdef";

    /** _more_ */
    public static final String TAG_SYNONYMTYPEDEF = "synonymtypedef";

    /** _more_ */
    public static final String TAG_DEFAULT_NAMESPACE = "default-namespace";

    /** _more_ */
    public static final String TAG_REMARK = "remark";

    /** _more_ */
    public static final String TAG_ID = "id";

    /** _more_ */
    public static final String TAG_NAME = "name";

    /** _more_ */
    public static final String TAG_NAMESPACE = "namespace";

    /** _more_ */
    public static final String TAG_DEF = "def";

    /** _more_ */
    public static final String TAG_SYNONYM = "synonym";

    /** _more_ */
    public static final String TAG_IS_A = "is_a";

    /** _more_ */
    public static final String TAG_ALT_ID = "alt_id";

    /** _more_ */
    public static final String TAG_SUBSET = "subset";

    /** _more_ */
    public static final String TAG_XREF = "xref";

    /** _more_ */
    public static final String TAG_COMMENT = "comment";

    /** _more_ */
    public static final String TAG_IS_OBSOLETE = "is_obsolete";

    /** _more_ */
    public static final String TAG_CONSIDER = "consider";

    /** _more_ */
    public static final String TAG_RELATIONSHIP = "relationship";

    /** _more_ */
    public static final String TAG_PROPERTY_VALUE = "property_value";

    /** _more_ */
    public static final String TAG_REPLACED_BY = "replaced_by";

    /** _more_ */
    public static final String TAG_DISJOINT_FROM = "disjoint_from";

    /** _more_ */
    public static final String TAG_IS_TRANSITIVE = "is_transitive";

    /** _more_ */
    public static final String TAG_TRANSITIVE_OVER = "transitive_over";


}
