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

package org.ramadda.util;


import org.w3c.dom.*;


import ucar.unidata.xml.XmlUtil;

import java.util.HashSet;
import java.util.List;


/**
 */
public class OpenSearchUtil {

    /** _more_          */
    public static final String XMLNS = "http://a9.com/-/spec/opensearch/1.1/";

    /** _more_          */
    public static final String MIMETYPE =
        "application/opensearchdescription+xml";

    /** _more_          */
    public static final String TAG_OPENSEARCHDESCRIPTION =
        "OpenSearchDescription";

    /** _more_          */
    public static final String TAG_SHORTNAME = "ShortName";

    /** _more_          */
    public static final String TAG_DESCRIPTION = "Description";

    /** _more_          */
    public static final String TAG_TAGS = "Tags";

    /** _more_          */
    public static final String TAG_CONTACT = "Contact";

    /** _more_          */
    public static final String TAG_URL = "Url";

    /** _more_          */
    public static final String TAG_LONGNAME = "LongName";

    /** _more_          */
    public static final String TAG_IMAGE = "Image";

    /** _more_          */
    public static final String TAG_QUERY = "Query";

    /** _more_          */
    public static final String TAG_DEVELOPER = "Developer";

    /** _more_          */
    public static final String TAG_ATTRIBUTION = "Attribution";

    /** _more_          */
    public static final String TAG_SYNDICATIONRIGHT = "SyndicationRight";

    /** _more_          */
    public static final String TAG_ADULTCONTENT = "AdultContent";

    /** _more_          */
    public static final String TAG_LANGUAGE = "Language";

    /** _more_          */
    public static final String TAG_OUTPUTENCODING = "OutputEncoding";

    /** _more_          */
    public static final String TAG_INPUTENCODING = "InputEncoding";

    /** _more_          */
    public static final String ATTR_XMLNS = "xmlns";

    /** _more_          */
    public static final String ATTR_TEMPLATE = "template";

    /** _more_          */
    public static final String ATTR_TYPE = "type";

    /** _more_          */
    public static final String ATTR_HEIGHT = "height";

    /** _more_          */
    public static final String ATTR_WIDTH = "width";

    /** _more_          */
    public static final String ATTR_ROLE = "role";

    /** _more_          */
    public static final String ATTR_SEARCHTERMS = "searchTerms";

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public static Element getRoot() throws Exception {
        Document doc = XmlUtil.makeDocument();
        Element root = XmlUtil.create(doc, TAG_OPENSEARCHDESCRIPTION, null,
                                      new String[] { ATTR_XMLNS,
                XMLNS });
        return root;
    }

    /*
<ShortName>Web Search</ShortName>
<Description>Use Example.com to search the Web.</Description>
<Tags>example web</Tags>
<Contact>admin@example.com</Contact>
<SyndicationRight>open</SyndicationRight>
<AdultContent>false</AdultContent>
<Language>en-us</Language>
<OutputEncoding>UTF-8</OutputEncoding>
<InputEncoding>UTF-8</InputEncoding>

    */

    /**
     * _more_
     *
     * @param root _more_
     * @param name _more_
     * @param description _more_
     * @param email _more_
     *
     * @throws Exception _more_
     */
    public static void addBasicTags(Element root, String name,
                                    String description, String email)
            throws Exception {
        if (name.length() > 16) {
            name = name.substring(0, 15);
        }
        String[] tags = new String[] {
            TAG_SHORTNAME, name, TAG_DESCRIPTION, description, TAG_CONTACT,
            email, TAG_SYNDICATIONRIGHT, "open", TAG_ADULTCONTENT, "false",
            TAG_LANGUAGE, "en-us", TAG_OUTPUTENCODING, "UTF-8",
            TAG_INPUTENCODING, "UTF-8"
        };
        for (int i = 0; i < tags.length; i += 2) {
            ((Element) XmlUtil.create(tags[i], root)).appendChild(
                XmlUtil.makeCDataNode(
                    root.getOwnerDocument(), tags[i + 1], false));
        }
    }





}
